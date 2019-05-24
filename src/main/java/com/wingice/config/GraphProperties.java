package com.wingice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author 胡昊
 * Description: Graph配置信息
 * Date: 2019/5/23
 * Time: 21:12
 * Create: DoubleH
 */
@ConfigurationProperties(prefix = "com.wingice.graph")
public class GraphProperties {

    /**
     * 客户端id
     */
    private String clientId;
    /**
     * 密钥
     */
    private String secret;
    /**
     * 验证方式
     */
    private String grantType;
    /**
     * token终结点
     */
    private String tokenEndpoint;
    /**
     * 权限域
     */
    private String scope;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
