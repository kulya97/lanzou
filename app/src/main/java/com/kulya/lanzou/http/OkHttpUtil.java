package com.kulya.lanzou.http;

/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/6 17:05
*/

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {
    private static OkHttpUtil mHttpUtil;
    private OkHttpClient.Builder mOkHttpClientBuilder;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;
    private MyCookieJar cookieJar;

    private OkHttpUtil() {
        cookieJar = new MyCookieJar();
        mOkHttpClientBuilder = new OkHttpClient.Builder();
        mOkHttpClientBuilder.cookieJar(cookieJar);
        mOkHttpClientBuilder.sslSocketFactory(createSSLSocketFactory(), new TrustAllCerts());
        mOkHttpClientBuilder.hostnameVerifier(new TrustAllHostnameVerifier());
        mOkHttpClient = mOkHttpClientBuilder.build();
        mDelivery = new Handler(Looper.getMainLooper());
    }

    //获取实例
    private static OkHttpUtil getInstance() {
        if (mHttpUtil == null) {
            synchronized (OkHttpUtil.class) {
                if (mHttpUtil == null) {
                    mHttpUtil = new OkHttpUtil();
                }
            }
        }
        return mHttpUtil;
    }

    private OkHttpClient getmOkHttpClient() {
        return this.mOkHttpClient;
    }

    //信任证书
    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }

    //信任证书
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }

    //信任证书
    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    /**
     * 同步的Get请求, 返回Response对象
     */
    private Response _getSync(String url) throws IOException {
        final Request request = new Request.Builder().url(url).build();
        Call call = mOkHttpClient.newCall(request);
        Response response = call.execute();
        return response;
    }

    private Response _getSync(String url, RequestData... headers) throws IOException {

        if (headers == null) {
            headers = new RequestData[0];
        }
        final Request.Builder builder = new Request.Builder().url(url);
        for (RequestData header : headers) {
            builder.addHeader(header.key, header.value);
        }
        Request request = builder.build();
        Call call = mOkHttpClient.newCall(request);
        Response response = call.execute();
        return response;
    }

    /**
     * 同步的Get请求, 返回string
     */
    private String _getSyncString(String url) throws IOException {
        Response response = _getSync(url);
        return response.body().string();
    }

    /**
     * 异步的Get请求
     */
    private void _getAsync(String url, final ResultCallback callback) {
        final Request request = new Request.Builder().url(url).build();
        deliveryResult(callback, request);
    }

    /**
     * 同步的Post请求, 返回Response对象
     */
    private Response _postSync(String url, RequestData[] params, RequestData... headers) throws IOException {
        Request request = buildPostRequest(url, params, headers);
        Response response = mOkHttpClient.newCall(request).execute();
        return response;
    }

    /**
     * 同步的Post请求, 返回string
     */
    private String _postSyncString(String url, RequestData[] params, RequestData... headers) throws IOException {
        Response response = _postSync(url, params, headers);
        return response.body().string();
    }

    /**
     * 异步的post请求
     */
    private void _postAsync(String url, final ResultCallback callback, RequestData[] params, RequestData... headers) {
        Request request = buildPostRequest(url, params, headers);
        deliveryResult(callback, request);
    }

    /**
     * 异步的post请求
     */
    private void _postAsync(String url, final ResultCallback callback, Map<String, String> params, Map<String, String> headers) {
        RequestData[] paramsArr = mapToRequestDatas(params);
        RequestData[] headersArr = mapToRequestDatas(headers);
        Request request = buildPostRequest(url, paramsArr, headersArr);
        deliveryResult(callback, request);
    }

    /**
     * 将Map键值对数据转化为RequestData数组
     */
    private RequestData[] mapToRequestDatas(Map<String, String> params) {
        int index = 0;

        if (params == null) {
            return new RequestData[0];
        }
        int size = params.size();

        RequestData[] res = new RequestData[size];
        Set<Map.Entry<String, String>> entries = params.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            res[index++] = new RequestData(entry.getKey(), entry.getValue());
        }
        return res;
    }

    /**
     * 构建post请求参数
     */
    private Request buildPostRequest(String url, RequestData[] params, RequestData... headers) {
        if (headers == null) {
            headers = new RequestData[0];
        }
        Headers.Builder headersBuilder = new Headers.Builder();
        for (RequestData header : headers) {
            headersBuilder.add(header.key, header.value);
        }
        Headers requestHeaders = headersBuilder.build();

        if (params == null) {
            params = new RequestData[0];
        }
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (RequestData param : params) {
            formBodyBuilder.add(param.key, param.value);
        }
        RequestBody requestBody = formBodyBuilder.build();
        return new Request.Builder()
                .url(url)
                .headers(requestHeaders)
                .post(requestBody)
                .build();
    }

    /**
     * 调用call.enqueue，将call加入调度队列，执行完成后在callback中得到结果
     */
    private void deliveryResult(final ResultCallback callback, Request request) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                sendFailedStringCallback(call, e, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    switch (response.code()) {
                        case 200:
                            final byte[] bytes = response.body().bytes();
                            sendSuccessResultCallback(bytes, callback);
                            break;
                        case 500:
                            sendSuccessResultCallback(null, callback);
                            break;
                        default:
                            throw new IOException();
                    }

                } catch (IOException e) {
                    sendFailedStringCallback(call, e, callback);
                }
            }
        });
    }

    /**
     * 调用请求失败对应的回调方法，利用handler.post使得回调方法在UI线程中执行
     */
    private void sendFailedStringCallback(final Call call, final Exception e, final ResultCallback callback) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null)
                    callback.onError(call, e);
            }
        });
    }

    /**
     * 调用请求成功对应的回调方法，利用handler.post使得回调方法在UI线程中执行
     */
    private void sendSuccessResultCallback(final byte[] bytes, final ResultCallback callback) {
        mDelivery.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onResponse(bytes);
                }
            }
        });
    }

    public static abstract class ResultCallback {
        public abstract void onError(Call call, Exception e);

        public abstract void onResponse(byte[] response);
    }

    /************************ 以下为外部可以调用的方法 ************************/

    public static class RequestData {
        String key;
        String value;

        public RequestData() {

        }

        public RequestData(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static OkHttpClient getmOkHttpClient2() {
        return getInstance().getmOkHttpClient();
    }

    public static Response getSync(String url) throws IOException {
        return getInstance()._getSync(url);
    }

    public static Response getSync(String url, RequestData... headers) throws IOException {
        return getInstance()._getSync(url, headers);
    }

    public static String getSyncString(String url) throws IOException {
        return getInstance()._getSyncString(url);
    }

    public static void getAsync(String url, ResultCallback callback) {
        getInstance()._getAsync(url, callback);
    }

    public static Response postSync(String url, RequestData[] params,
                                    RequestData... headers) throws IOException {
        return getInstance()._postSync(url, params, headers);
    }

    public static String postSyncString(String url, RequestData[] params,
                                        RequestData... headers) throws IOException {
        return getInstance()._postSyncString(url, params, headers);
    }

    public static void postAsync(String url, final ResultCallback callback,
                                 RequestData[] params, RequestData... headers) {
        getInstance()._postAsync(url, callback, params, headers);
    }

    public static void postAsync(String url, final ResultCallback callback,
                                 Map<String, String> params,
                                 Map<String, String> headers) {
        getInstance()._postAsync(url, callback, params, headers);
    }
}
