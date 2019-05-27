package com.wingice.service.impl;

import com.google.gson.JsonObject;
import com.microsoft.graph.models.extensions.*;
import com.microsoft.graph.models.generated.AttendeeType;
import com.microsoft.graph.models.generated.LocationType;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IEventCollectionPage;
import com.wingice.model.EventCreateParams;
import com.wingice.model.UserEventParams;
import com.wingice.service.IAuthenticatedClientService;
import com.wingice.service.IGraphEventService;
import com.wingice.utils.datetime.DateTimeUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.List;

/**
 * @author 胡昊
 * Description: 事件服务实现
 * Date: 2019/5/23
 * Time: 21:25
 * Create: DoubleH
 */
public class GraphEventServiceImpl implements IGraphEventService {

    private final IAuthenticatedClientService authenticatedClientService;

    public GraphEventServiceImpl(IAuthenticatedClientService authenticatedClientService) {
        this.authenticatedClientService = authenticatedClientService;
    }

    @Override
    public List<Event> getUserEvent(UserEventParams params) {
        final List<Option> optionList = new ArrayList<>();
        final StringBuilder filterStr = new StringBuilder();
        if (null != params.getStart() && null != params.getEnd()) {
            filterStr.append("start/dateTime ge '")
                    .append(DateTimeUtils.longToString(params.getStart(), ZoneId.of(params.getTimezone(), ZoneId.SHORT_IDS), "yyyy-MM-dd'T'HH:mm:ss"))
                    .append("' and end/dateTime le '")
                    .append(DateTimeUtils.longToString(params.getEnd(), ZoneId.of(params.getTimezone(), ZoneId.SHORT_IDS), "yyyy-MM-dd'T'HH:mm:ss"))
                    .append("'");
        }
        if (null != params.getIsOrganizer() && !"".equals(params.getIsOrganizer().trim())) {
            if (filterStr.length() > 0) {
                filterStr.append(" and ");
            }
            filterStr.append("isOrganizer eq ").append(params.getIsOrganizer());
        }
        if (null != params.getIsCancelled() && !"".equals(params.getIsCancelled().trim())) {
            if (filterStr.length() > 0) {
                filterStr.append(" and ");
            }
            filterStr.append("isCancelled eq ").append(params.getIsCancelled());
        }
        if (filterStr.length() > 0) {
            final QueryOption filter = new QueryOption("$filter", filterStr.toString());
            optionList.add(filter);
        }
        if (null != params.getPageNum() && null != params.getPageSize()) {
            final QueryOption top = new QueryOption("$top", params.getPageSize());
            final QueryOption skip = new QueryOption("$skip", (params.getPageNum() - 1) * params.getPageSize());
            optionList.add(top);
            optionList.add(skip);
        }
        final QueryOption orderby = new QueryOption("$orderby", "start/dateTime desc");
        optionList.add(orderby);
        final IGraphServiceClient client = GraphServiceClient.builder().authenticationProvider(request -> {
            request.addHeader("Authorization", "Bearer " + authenticatedClientService.getAccessToken());
            request.addHeader("Prefer", "outlook.timezone=\"" + params.getTimezone().replaceAll("\"", "") + "\",outlook.body-content-type=\"" + params.getContentType().replaceAll("\"", "") + "\"");
        }).buildClient();
        IEventCollectionPage eventCollectionPage = client.users(params.getUserPrincipalName()).events().buildRequest(optionList).get();
        final List<Event> eventList = new LinkedList<>(eventCollectionPage.getCurrentPage());
        if (null == params.getPageNum() && null == params.getPageSize()) {
            while (null != eventCollectionPage.getNextPage()) {
                eventCollectionPage = eventCollectionPage.getNextPage().buildRequest().get();
                eventList.addAll(eventCollectionPage.getCurrentPage());
            }
        }
        return eventList;
    }

    @Override
    public void cancelEvent(String userPrincipalName, String id, String comment) {
        Event event = authenticatedClientService.getClient()
                .users(userPrincipalName)
                .events(id)
                .buildRequest()
                .get();
        //会议室中事件id和组织者事件id不一致
        final QueryOption filter = new QueryOption("$filter", "isCancelled eq false and subject eq '" + event.subject + "'");
        event = authenticatedClientService.getClient()
                .users(event.organizer.emailAddress.address)
                .events()
                .buildRequest(Collections.singletonList(filter))
                .get().getCurrentPage().get(0);
        JsonObject body = new JsonObject();
        body.addProperty("Comment", comment);
        authenticatedClientService.getBetaClient()
                .customRequest("/users/" + event.organizer.emailAddress.address + "/events/" + event.id + "/cancel")
                .buildRequest()
                .post(body);
    }

    @Override
    public Event createEvent(EventCreateParams params) {
        final Event event = new Event();
        //设置标题
        event.subject = params.getSubject();
        event.body = params.getBody();
        //设置时间 时区不存在采用系统环境时区
        String id = ZoneId.SHORT_IDS.get(params.getTimeZone());
        id = (id != null ? id : ZoneId.systemDefault().getId());
        DateTimeTimeZone start = new DateTimeTimeZone();
        start.timeZone = id;
        start.dateTime = DateTimeUtils.longToString(params.getStart(), ZoneId.of(id), "yyyy-MM-dd'T'HH:mm:ss");
        event.start = start;
        DateTimeTimeZone end = new DateTimeTimeZone();
        end.timeZone = id;
        end.dateTime = DateTimeUtils.longToString(params.getStart(), ZoneId.of(id), "yyyy-MM-dd'T'HH:mm:ss");
        event.end = end;
        //设置地点
        event.location = params.getLocation();
        //与会人添加会议室信息（如果会议地点已设置为资源）
        if (null != params.getLocation() && null != params.getLocation().locationEmailAddress && !"".equals(params.getLocation().locationEmailAddress)) {
            //设置地点类型
            event.location.locationType = LocationType.CONFERENCE_ROOM;
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.address = params.getLocation().locationEmailAddress;
            Attendee attendee = new Attendee();
            attendee.emailAddress = emailAddress;
            attendee.type = AttendeeType.RESOURCE;
            params.getAttendees().add(attendee);
        }
        event.attendees = params.getAttendees();
        //发送创建事件请求
        return authenticatedClientService.getBetaClient()
                .users(params.getUserPrincipalName())
                .events()
                .buildRequest()
                .post(event);
    }

    @Override
    public Boolean checkConflict(String userPrincipalName, Long start, Long end, String timezone) {
        String zoneId;
        if (null != timezone && !"".equals(timezone)) {
            zoneId = ZoneId.SHORT_IDS.get(timezone);
            zoneId = (zoneId != null ? zoneId : ZoneId.systemDefault().getId());
        } else {
            zoneId = ZoneId.systemDefault().getId();
        }
        final UserEventParams params = new UserEventParams();
        //开始时间+1分钟 结束时间-1分钟 处理边界冲突
        params.setStart(start + 60 * 1000);
        params.setEnd(end - 60 * 1000);
        params.setTimezone(zoneId);
        params.setUserPrincipalName(userPrincipalName);
        return getUserEvent(params).size() > 0;
    }
}
