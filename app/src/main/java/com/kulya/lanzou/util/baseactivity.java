package com.kulya.lanzou.util;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/7 22:35
*/
public class baseactivity extends AppCompatActivity {
    private  long lastClickTime;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitycollector.addactivity(this);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        activitycollector.removeactivity(this);
    }

}
