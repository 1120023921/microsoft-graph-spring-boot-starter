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
import com.wingice.model.EventParams;
import com.wingice.model.EventUpdateParams;
import com.wingice.model.UserEventParams;
import com.wingice.service.IAuthenticatedClientService;
import com.wingice.service.IGraphEventService;
import com.wingice.utils.datetime.DateTimeUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
        String filterStr = null;
        if (null != params.getStart() && null != params.getEnd()) {
            String zoneId = ZoneId.SHORT_IDS.get(params.getTimezone());
            zoneId = (zoneId != null ? zoneId : ZoneId.systemDefault().getId());
            filterStr = "((start/dateTime ge '" +
                    DateTimeUtils.longToString(params.getStart(), ZoneId.of(zoneId), "yyyy-MM-dd'T'HH:mm:ss") +
                    "' and start/dateTime lt '" +
                    DateTimeUtils.longToString(params.getEnd(), ZoneId.of(zoneId), "yyyy-MM-dd'T'HH:mm:ss") +
                    "') or (end/dateTime gt '" +
                    DateTimeUtils.longToString(params.getStart(), ZoneId.of(zoneId), "yyyy-MM-dd'T'HH:mm:ss") +
                    "' and end/dateTime le '" +
                    DateTimeUtils.longToString(params.getEnd(), ZoneId.of(zoneId), "yyyy-MM-dd'T'HH:mm:ss") +
                    "'))";
        }
        if (null != params.getIsOrganizer() && !"".equals(params.getIsOrganizer().trim())) {
            if (null != filterStr && !"".equals(filterStr)) {
                filterStr += " and ";
            }
            filterStr += "isOrganizer eq ";
            filterStr += params.getIsOrganizer();
        }
        if (null != params.getIsCancelled() && !"".equals(params.getIsCancelled().trim())) {
            if (null != filterStr && !"".equals(filterStr)) {
                filterStr += " and ";
            }
            filterStr += ("isCancelled eq ");
            filterStr += params.getIsCancelled();
        }
        if (null != filterStr && !"".equals(filterStr)) {
            final QueryOption filter = new QueryOption("$filter", filterStr);
            optionList.add(filter);
        }
        if (null != params.getPageNum() && null != params.getPageSize()) {
            final QueryOption top = new QueryOption("$top", params.getPageSize());
            final QueryOption skip = new QueryOption("$skip", (params.getPageNum() - 1) * params.getPageSize());
            optionList.add(top);
            optionList.add(skip);
        }
        if (null != params.getOrderBy() && !"".equals(params.getOrderBy())) {
            final QueryOption orderby = new QueryOption("$orderby", params.getOrderBy());
            optionList.add(orderby);
        }
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
        Event event = transferEvent(userPrincipalName, id);
        JsonObject body = new JsonObject();
        body.addProperty("Comment", comment);
        authenticatedClientService.getBetaClient()
                .customRequest("/users/" + event.organizer.emailAddress.address + "/events/" + event.id + "/cancel")
                .buildRequest()
                .post(body);
    }

    @Override
    public Event createEvent(EventCreateParams params) {
        final Event event = buildEvent(params, null);
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
        final List<Option> optionList = new ArrayList<>();
        String filterStr = "((start/dateTime ge '" +
                DateTimeUtils.longToString(start, ZoneId.of(zoneId), "yyyy-MM-dd'T'HH:mm:ss") +
                "' and start/dateTime lt '" +
                DateTimeUtils.longToString(end, ZoneId.of(zoneId), "yyyy-MM-dd'T'HH:mm:ss") +
                "') or (end/dateTime gt '" +
                DateTimeUtils.longToString(start, ZoneId.of(zoneId), "yyyy-MM-dd'T'HH:mm:ss") +
                "' and end/dateTime le '" +
                DateTimeUtils.longToString(end, ZoneId.of(zoneId), "yyyy-MM-dd'T'HH:mm:ss") +
                "'))";
        final QueryOption filter = new QueryOption("$filter", filterStr);
        optionList.add(filter);
        final String finalZoneId = zoneId;
        final IGraphServiceClient client = GraphServiceClient.builder().authenticationProvider(request -> {
            request.addHeader("Authorization", "Bearer " + authenticatedClientService.getAccessToken());
            request.addHeader("Prefer", "outlook.timezone=\"" + finalZoneId + "\"");
        }).buildClient();
        IEventCollectionPage eventCollectionPage = client.users(userPrincipalName).events().buildRequest(optionList).get();
        return eventCollectionPage.getCurrentPage().size() > 0;
    }

    @Override
    public Event updateEvent(EventUpdateParams params) {
        final Event userEvent = transferEvent(params.getUserPrincipalName(), params.getId());
        final Event event = buildEvent(params, userEvent);
        return authenticatedClientService.getClient()
                .users(userEvent.organizer.emailAddress.address)
                .events(userEvent.id)
                .buildRequest()
                .patch(event);
    }

    /**
     * 会议室中事件id和组织者事件id不一致 转化事件
     *
     * @param userPrincipalName 用户名
     * @param id                事件id
     * @return 用户事件
     */
    private Event transferEvent(String userPrincipalName, String id) {
        Event event = authenticatedClientService.getClient()
                .users(userPrincipalName)
                .events(id)
                .buildRequest()
                .get();
        //会议室中事件id和组织者事件id不一致
        final QueryOption filter = new QueryOption("$filter", "isCancelled eq false and subject eq '" + event.subject + "'");
        return authenticatedClientService.getClient()
                .users(event.organizer.emailAddress.address)
                .events()
                .buildRequest(Collections.singletonList(filter))
                .get().getCurrentPage().get(0);
    }

    /**
     * 参数组装Event对象
     *
     * @param params 参数
     * @return event对象
     */
    private <T extends EventParams> Event buildEvent(T params, Event event) {
        if (null == event) {
            event = new Event();
        }
        //设置是否响应
        event.responseRequested = params.getResponseRequested();
        //设置标题
        if (!StringUtils.isEmpty(params.getSubject())) {
            event.subject = params.getSubject();
        }
        if (!StringUtils.isEmpty(params.getBody())) {
            event.body = params.getBody();
        }
        //设置时间 时区不存在采用系统环境时区
        String id = ZoneId.SHORT_IDS.get(params.getTimeZone());
        id = (id != null ? id : ZoneId.systemDefault().getId());
        DateTimeTimeZone start = new DateTimeTimeZone();
        start.timeZone = id;
        start.dateTime = DateTimeUtils.longToString(params.getStart(), ZoneId.of(id), "yyyy-MM-dd'T'HH:mm:ss");
        event.start = start;
        DateTimeTimeZone end = new DateTimeTimeZone();
        end.timeZone = id;
        end.dateTime = DateTimeUtils.longToString(params.getEnd(), ZoneId.of(id), "yyyy-MM-dd'T'HH:mm:ss");
        event.end = end;
        //设置地点
        if (null != params.getLocation()) {
            event.location = params.getLocation();
        }
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
        if (!CollectionUtils.isEmpty(params.getAttendees())) {
            event.attendees = params.getAttendees();
        }
        return event;
    }
}
