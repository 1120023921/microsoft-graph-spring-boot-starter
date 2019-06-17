package com.wingice.service;

import com.microsoft.graph.models.extensions.Event;
import com.wingice.enums.UserEventModeEnum;
import com.wingice.model.EventCreateParams;
import com.wingice.model.EventUpdateParams;
import com.wingice.model.UserEventParams;

import java.util.List;
import java.util.Set;

/**
 * @author 胡昊
 * Description: 事件服务接口
 * Date: 2019/5/23
 * Time: 21:22
 * Create: DoubleH
 */
public interface IGraphEventService {

    /**
     * 获取用户事件信息
     *
     * @param params 查询条件
     * @return 事件列表
     */
    List<Event> getUserEvent(UserEventParams params);

    /**
     * 取消事件
     *
     * @param userPrincipalName 用户名
     * @param id                事件id
     * @param comment           取消通知信息
     */
    void cancelEvent(String userPrincipalName, String id, String comment);

    /**
     * 创建事件
     *
     * @param params 事件参数
     * @return 相应对象
     */
    Event createEvent(EventCreateParams params);

    /**
     * 创建事件检测时间冲突
     *
     * @param userPrincipalName 用户
     * @param start             开始时间
     * @param end               结束时间
     * @param timezone          时区 null为系统默认时区
     * @return 是否冲突 true冲突 false无冲突
     */
    Boolean checkConflict(String userPrincipalName, Long start, Long end, String timezone);

    /**
     * 更新事件
     *
     * @param params 事件参数
     * @return 更新的 event 对象
     */
    Event updateEvent(EventUpdateParams params);

    /**
     * 获取用户普通事件
     *
     * @param params 查询条件
     * @return 事件列表
     */
    List<Event> getUserNormalEvent(UserEventParams params);
}
