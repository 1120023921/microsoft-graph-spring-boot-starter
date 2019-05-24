package com.wingice.service;

import com.microsoft.graph.models.extensions.Event;
import com.wingice.modal.UserEventParams;

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
}
