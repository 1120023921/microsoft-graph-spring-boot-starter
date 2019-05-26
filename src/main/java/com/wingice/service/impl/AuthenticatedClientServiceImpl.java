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
import com.wingice.utils.http.HttpRequestUtil;
import com.wingice.utils.jwt.JwtTokenUtil;

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
    public IGraphServiceClient getClient() {
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

    @Override
    public IGraphServiceClient getBetaClient() {
        try {
            getAccessToken();
            IAuthenticationProvider mAuthenticationProvider = request -> request.addHeader("Authorization",
                    "Bearer " + accessToken);
            IClientConfig mClientConfig = DefaultClientConfig.createWithAuthenticationProvider(mAuthenticationProvider);
            IGraphServiceClient betaClient = GraphServiceClient.fromConfig(mClientConfig);
            betaClient.setServiceRoot("https://graph.microsoft.com/beta");
            return betaClient;
        } catch (Exception e) {
            throw new Error("Could not create a graph client: " + e.getLocalizedMessage());
        }
    }

    private String getNewAccessToken() {
        String params = "client_id=" + properties.getClientId() + "&scope=" + properties.getScope() + "&client_secret=" + properties.getSecret() + "&grant_type=" + properties.getGrantType();
        String result = HttpRequestUtil.sendPost(properties.getTokenEndpoint(), params, null);
        return new GsonBuilder().create().fromJson(result, JsonObject.class).get("access_token").toString().replaceAll("\"", "");
    }
}
