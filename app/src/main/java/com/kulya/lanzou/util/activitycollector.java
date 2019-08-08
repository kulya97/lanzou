package com.kulya.lanzou.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/7 22:35
*/
public class activitycollector {
    public static List<Activity> list=new ArrayList<>();
    public static void addactivity(Activity activity){
        list.add(activity);
    }
    public static void removeactivity(Activity activity){
        list.remove(activity);
    }
    public static void finishall(){
        for(Activity a:list){
            if(!a.isFinishing()){
                a.finish();
            }
        }
        list.clear();
    }
}
