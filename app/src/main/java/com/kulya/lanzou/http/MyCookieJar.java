package com.kulya.lanzou.http;

/*
项目名称： lanzou
创建人：黄大神
类描述：cookie管理
创建时间：2019/8/6 17:06
*/

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class MyCookieJar implements CookieJar {


    static List<Cookie> cache = new ArrayList<>();

    //Http请求结束，Response中有Cookie时候回调
    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        //内存中缓存Cookie
        cache.addAll(cookies);
    }

    //Http发送请求前回调，Request中设置Cookie
    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        //过期的Cookie
        List<Cookie> invalidCookies = new ArrayList<>();
        //有效的Cookie
        List<Cookie> validCookies = new ArrayList<>();

        for (Cookie cookie : cache) {

            if (cookie.expiresAt() < System.currentTimeMillis()) {
                //判断是否过期
                invalidCookies.add(cookie);
            } else if (cookie.matches(url)) {
                //匹配Cookie对应url
                validCookies.add(cookie);
            }
        }

        //缓存中移除过期的Cookie
        cache.removeAll(invalidCookies);

        //返回List<Cookie>让Request进行设置
        return validCookies;
    }


    public static void resetCookies() {
        cache.clear();
    }

    public static void print() {
        Log.d("cookie", "null");
        Iterator<Cookie> it = cache.iterator();
        while (it.hasNext()) {
            Cookie s = it.next();
            Log.d("cookie", s.toString());
        }
    }

}