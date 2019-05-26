package com.wingice.service;

import com.microsoft.graph.models.extensions.Event;
import com.wingice.model.EventCreateParams;
import com.wingice.model.UserEventParams;

import java.util.List;

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
    Event createEvent(String userPrincipalName, EventCreateParams params);
}
