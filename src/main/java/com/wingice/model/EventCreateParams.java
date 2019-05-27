package com.wingice.model;


import com.microsoft.graph.models.extensions.Attendee;
import com.microsoft.graph.models.extensions.ItemBody;
import com.microsoft.graph.models.extensions.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡昊
 * Description: 创建事件参数
 * Date: 2019/5/26
 * Time: 15:32
 * Create: DoubleH
 */
public class EventCreateParams {

    private String userPrincipalName;
    private String subject;
    private ItemBody body;
    private Long start;
    private Long end;
    private String timeZone;
    private Location location;
    private List<Attendee> attendees = new ArrayList<>();

    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    public void setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public ItemBody getBody() {
        return body;
    }

    public void setBody(ItemBody body) {
        this.body = body;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Attendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<Attendee> attendees) {
        this.attendees = attendees;
    }
}
