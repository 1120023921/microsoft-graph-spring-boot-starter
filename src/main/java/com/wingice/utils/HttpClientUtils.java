package com.wingice.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HttpClient工具类
 *
 * @author 胡昊
 * @date 2018-02-21
 */
public class HttpClientUtils {


    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static SSLConnectionSocketFactory sslsf = null;
    private static PoolingHttpClientConnectionManager cm = null;
    private static SSLContextBuilder builder = null;

//    static {
//        try {
//            builder = new SSLContextBuilder();
//            // 全部信任 不做身份鉴定
//            builder.loadTrustMaterial(null, new TrustStrategy() {
//                @Override
//                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
//                    return true;
//                }
//            });
//            sslsf = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
//            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
//                    .register(HTTP, new PlainConnectionSocketFactory())
//                    .register(HTTPS, sslsf)
//                    .build();
//            cm = new PoolingHttpClientConnectionManager(registry);
//            cm.setMaxTotal(200);//max connection
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Get请求
     *
     * @param url     请求地址
     * @param params  参数map
     * @param headers 请求头map
     * @return
     * @throws Exception
     */
    public static String doGet(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
        // 创建一个httpclient对象
        CloseableHttpClient httpClient = null;
        // 执行请求
        CloseableHttpResponse response = null;

        try {
            httpClient = HttpClients.createDefault();
            // 创建一个uri对象
            URIBuilder uriBuilder = new URIBuilder(url);

            // 设置参数
            if (null != params) {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    uriBuilder.addParameter(param.getKey(), param.getValue());
                }
            }

            HttpGet get = new HttpGet(uriBuilder.build());

            // 设置头信息
            if (null != headers) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    get.addHeader(header.getKey(), header.getValue());
                }
            }

            response = httpClient.execute(get);
            // 取响应的结果
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println(statusCode);
            if (HttpStatus.SC_OK == statusCode) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity, "utf-8");
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            // 关闭httpclient
            response.close();
            httpClient.close();
        }
    }

    /**
     * Post请求
     *
     * @param url     请求地址
     * @param params  参数map
     * @param headers 请求头map
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<String, String> params, Map<String, String> headers) throws Exception {

        CloseableHttpClient httpClient = null;
        // 执行post请求
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.createDefault();
            // 创建一个post对象
            HttpPost post = new HttpPost(url);

            // 设置头信息
            if (null != headers) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    post.addHeader(header.getKey(), header.getValue());
                }
            }

            // 创建一个Entity。模拟一个表单
            List<NameValuePair> kvList = new ArrayList<>();

            // 设置参数
            if (null != params) {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    kvList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
            }

            // 包装成一个Entity对象
            StringEntity entity = new UrlEncodedFormEntity(kvList, "utf-8");
            // 设置请求的内容
            post.setEntity(entity);
            response = httpClient.execute(post);
            String result = EntityUtils.toString(response.getEntity());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            response.close();
            httpClient.close();
        }
    }

    /**
     * 创建信任所有证书的HttpClient
     *
     * @return
     * @throws Exception
     */
    public static CloseableHttpClient getHttpClient() throws Exception {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManager(cm)
                .setConnectionManagerShared(true)
                .build();
        return httpClient;
    }

    /**
     * SSLPost请求
     *
     * @param url     请求地址
     * @param params  参数map
     * @param headers 请求头map
     * @return
     * @throws Exception
     */
    public static String doSSLPost(String url, Map<String, String> params, Map<String, String> headers) throws Exception {

        CloseableHttpClient httpClient = null;
        // 执行post请求
        CloseableHttpResponse response = null;
        try {
            httpClient = getHttpClient();
            // 创建一个post对象
            HttpPost post = new HttpPost(url);

            // 设置头信息
            if (null != headers) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    post.addHeader(header.getKey(), header.getValue());
                }
            }

            // 创建一个Entity。模拟一个表单
            List<NameValuePair> kvList = new ArrayList<>();

            // 设置参数
            if (null != params) {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    kvList.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                }
            }

            // 包装成一个Entity对象
            StringEntity entity = new UrlEncodedFormEntity(kvList, "utf-8");
            // 设置请求的内容
            post.setEntity(entity);
            response = httpClient.execute(post);
            String result = EntityUtils.toString(response.getEntity());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            response.close();
            httpClient.close();
        }
    }

    /**
     * json参数
     *
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws Exception
     */
    public static String doSSLPostString(String url, String params, Map<String, String> headers) throws Exception {

        CloseableHttpClient httpClient = null;
        // 执行post请求
        CloseableHttpResponse response = null;
        try {
            httpClient = getHttpClient();
            // 创建一个post对象
            HttpPost post = new HttpPost(url);

            // 设置头信息
            if (null != headers) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    post.addHeader(header.getKey(), header.getValue());
                }
            }

            // 包装成一个Entity对象
            StringEntity reqEntity = new StringEntity(params, "utf-8");
            reqEntity.setContentType("application/json");
            // 设置请求的内容
            post.setEntity(reqEntity);
            response = httpClient.execute(post);
            String result = EntityUtils.toString(response.getEntity());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            response.close();
            httpClient.close();
        }
    }

    public static String doSSLPatchString(String url, String params, Map<String, String> headers) throws Exception {

        CloseableHttpClient httpClient = null;
        // 执行post请求
        CloseableHttpResponse response = null;
        try {
            httpClient = getHttpClient();
            // 创建一个post对象
            HttpPatch post = new HttpPatch(url);

            // 设置头信息
            if (null != headers) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    post.addHeader(header.getKey(), header.getValue());
                }
            }

            // 包装成一个Entity对象
            StringEntity reqEntity = new StringEntity(params, "utf-8");
            reqEntity.setContentType("application/json");
            // 设置请求的内容
            post.setEntity(reqEntity);
            response = httpClient.execute(post);
            String result = EntityUtils.toString(response.getEntity());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            response.close();
            httpClient.close();
        }
    }
}
