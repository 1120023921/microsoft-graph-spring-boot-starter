package com.wingice.service.impl;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.models.extensions.Event;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IEventCollectionPage;
import com.wingice.modal.UserEventParams;
import com.wingice.service.IAuthenticatedClientService;
import com.wingice.service.IGraphEventService;
import com.wingice.utils.datetime.DateTimeUtils;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedList;
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
        final IAuthenticationProvider mAuthenticationProvider = request -> {
            request.addHeader("Authorization", "Bearer " + authenticatedClientService.getAccessToken());
            request.addHeader("Prefer", "outlook.timezone=\"" + params.getTimezone().replaceAll("\"", "") + "\",outlook.body-content-type=\"" + params.getContentType().replaceAll("\"", "") + "\"");
        };
        final List<Option> optionList = new ArrayList<>();
        final StringBuilder filterStr = new StringBuilder();
        if (null != params.getStart() && null != params.getEnd()) {
            filterStr.append("start/dateTime ge '")
                    .append(DateTimeUtils.longToString(params.getStart(), ZoneId.of(params.getTimezone(), ZoneId.SHORT_IDS), "yyyy-MM-dd'T'HH:mm:ss"))
                    .append("' and end/dateTime le '")
                    .append(DateTimeUtils.longToString(params.getEnd(), ZoneId.of(params.getTimezone(), ZoneId.SHORT_IDS), "yyyy-MM-dd'T'HH:mm:ss"))
                    .append("'");
        }
        if (filterStr.length() > 0) {
            filterStr.append(" and ");
        }
        filterStr.append("isOrganizer eq ").append(params.getIsOrganizer());
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
        final IGraphServiceClient client = GraphServiceClient.fromConfig(DefaultClientConfig.createWithAuthenticationProvider(mAuthenticationProvider));
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
}
