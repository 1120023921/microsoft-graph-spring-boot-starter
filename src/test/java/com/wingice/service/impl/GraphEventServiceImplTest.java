package com.wingice.service.impl;

import com.microsoft.graph.models.extensions.Event;
import com.wingice.modal.UserEventParams;
import com.wingice.service.IGraphEventService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author 胡昊
 * Description:
 * Date: 2019/5/24
 * Time: 10:48
 * Create: DoubleH
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class GraphEventServiceImplTest {

    @Autowired
    private IGraphEventService graphEventService;

    @Test
    public void getUserEvent() {
        UserEventParams params = new UserEventParams();
        params.setUserPrincipalName("MeetingRoom101@wingice.com");
        params.setStart(1558319400000L);
        params.setEnd(1558837800000L);
        params.setPageNum(1);
        params.setPageSize(5);
//        params.setTimezone(ZoneId.systemDefault().getId());
        params.setContentType("TEXT");
//        params.setIsOrganizer("true");
        params.setIsCancelled("false");
        List<Event> eventList = graphEventService.getUserEvent(params);
        eventList.forEach(event -> System.out.println(event.subject));
    }

    @Test
    public void cancelEvent() {
        String userPrincipalName = "MeetingRoom101@wingice.com";
        String id = "AAMkAGY3MDY2N2NkLTk4OGItNDJjZi05OTY0LTk0YzMwMGI4MDI4OABGAAAAAADWVU_aJ1IaTZud6pgzdPobBwAmSWOGEKZCSo09voE4tB53AAAAAAENAAAmSWOGEKZCSo09voE4tB53AAABm88rAAA=";
        String comment = "ww";
        graphEventService.cancelEvent(userPrincipalName, id, comment);
    }
}
