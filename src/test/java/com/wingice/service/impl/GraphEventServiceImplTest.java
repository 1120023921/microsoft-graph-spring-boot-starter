package com.wingice.service.impl;

import com.microsoft.graph.models.extensions.Event;
import com.wingice.modal.UserEventParams;
import com.wingice.service.IGraphEventService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.*;

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
//        params.setStart(1558319400000L);
//        params.setEnd(1558578600000L);
        params.setPageNum(1);
        params.setPageSize(2);
        params.setTimezone(ZoneId.systemDefault().getId());
        params.setContentType("TEXT");
//        params.setIsOrganizer("true");
        List<Event> eventList = graphEventService.getUserEvent(params);
        eventList.forEach(event -> System.out.println(event.subject));
    }
}
