package com.wingice.service;

import com.microsoft.graph.models.extensions.IGraphServiceClient;

/**
 * @author 胡昊
 * Description:
 * Date: 2019/5/24
 * Time: 10:14
 * Create: DoubleH
 */
public interface IAuthenticatedClientService {

    /**
     * 获取accessToken
     *
     * @return accessToken
     */
    String getAccessToken();

    /**
     * 获取1.0终结点的Client
     *
     * @return client
     */
    IGraphServiceClient getClient();

    /**
     * 获取Beta终结点的Client
     *
     * @return client
     */
    IGraphServiceClient getBetaClient();
}
