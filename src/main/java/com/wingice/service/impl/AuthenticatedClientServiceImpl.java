package com.wingice.service.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.wingice.config.GraphProperties;
import com.wingice.service.IAuthenticatedClientService;
import com.wingice.utils.HttpClientUtils;
import com.wingice.utils.jwt.JwtTokenUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 胡昊
 * Description:
 * Date: 2019/5/24
 * Time: 10:18
 * Create: DoubleH
 */
public class AuthenticatedClientServiceImpl implements IAuthenticatedClientService {

    private final GraphProperties properties;
    private static String accessToken;

    public AuthenticatedClientServiceImpl(GraphProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getAccessToken() {
        if (null == accessToken || JwtTokenUtil.isTokenExpired(accessToken)) {
            accessToken = getNewAccessToken().replace("\"", "");
        }
        return accessToken;
    }

    @Override
    public IGraphServiceClient getAuthenticatedClient() {
        try {
            getAccessToken();
            IAuthenticationProvider mAuthenticationProvider = request -> request.addHeader("Authorization",
                    "Bearer " + accessToken);
            IClientConfig mClientConfig = DefaultClientConfig.createWithAuthenticationProvider(mAuthenticationProvider);
            return GraphServiceClient.fromConfig(mClientConfig);
        } catch (Exception e) {
            throw new Error("Could not create a graph client: " + e.getLocalizedMessage());
        }
    }

    private String getNewAccessToken() {
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/x-www-form-urlencoded");
        Map<String, String> params = new HashMap<>();
        params.put("client_id", properties.getClientId());
        params.put("scope", properties.getScope());
        params.put("client_secret", properties.getSecret());
        params.put("grant_type", properties.getGrantType());
        try {
            String result = HttpClientUtils.doPost(properties.getTokenEndpoint(), params, header);
            JsonObject res = new GsonBuilder().create().fromJson(result, JsonObject.class);
            return res.get("access_token").toString().replaceAll("\"", "");
        } catch (Exception e) {
            throw new Error("Error retrieving access token: " + e.getLocalizedMessage());
        }
    }
}
