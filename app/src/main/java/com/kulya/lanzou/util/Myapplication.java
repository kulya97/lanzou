package com.kulya.lanzou.util;

import android.app.Application;
import android.content.Context;


/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/11 9:12
*/
public class Myapplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

    }

    public static Context getContext() {
        return context;
    }
}
