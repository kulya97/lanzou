package com.kulya.lanzou.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/6 11:52
*/
public class HttpUtil {
    public static void loginPost(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("formhash","0af1aa15")
                .add("username","17802531301")
                .add("password","1140576864.")
                .add("action","login")
                .add("task","login")
                .add("ref","mydisk.php")
                .build();
        Request request = new Request.Builder()
                .post(formBody)
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public static void loginGet(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
