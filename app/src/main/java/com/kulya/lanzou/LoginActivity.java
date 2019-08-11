package com.kulya.lanzou;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kulya.lanzou.http.HttpUtil;
import com.kulya.lanzou.http.MyCookieJar;
import com.kulya.lanzou.http.OkHttpUtil;
import com.kulya.lanzou.http.UriUtil;
import com.kulya.lanzou.util.baseactivity;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.select.Elements;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends baseactivity implements View.OnClickListener {

    private EditText login_id;
    private EditText login_key;
    private Button login_button;
    private SharedPreferences.Editor editor;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        login_id = (EditText) findViewById(R.id.login_id);
        login_key = (EditText) findViewById(R.id.login_key);
        login_button = (Button) findViewById(R.id.login_button);
        login_button.setOnClickListener(this);
        setKey();
    }

    //保存密码
    private void saveKey(String id, String key) {
        editor = pref.edit();
        editor.putString("id", id);
        editor.putString("key", key);
        editor.apply();
    }

    //自动填写密码
    private void setKey() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        String username = pref.getString("id", "17802531301");
        String password = pref.getString("key", "1140576864.");
        login_id.setText(username);
        login_key.setText(password);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                submit();
                break;
        }
    }

    //登录事件
    private void submit() {
        String id = login_id.getText().toString().trim();
        if (TextUtils.isEmpty(id)) {
            Toast.makeText(this, "id不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = login_key.getText().toString().trim();
        if (TextUtils.isEmpty(key)) {
            Toast.makeText(this, "key不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        saveKey(id, key);
        getFormHash(id, key);
    }

    //获取formhash
    private void getFormHash(final String username, final String password) {
        HttpUtil.loginGet(UriUtil.GETFORMHASH, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String data = response.body().string();
                Document document = Jsoup.parse(data);
                Elements element = document.select("input[name=formhash]");
                String formhash = element.attr("value");
                login(formhash, username, password);
            }
        });
    }

    //登录
    private void login(String formhash, final String username, final String password) {
        OkHttpUtil.RequestData[] rs = new OkHttpUtil.RequestData[6];
        rs[0] = new OkHttpUtil.RequestData("formhash", formhash);
        rs[1] = new OkHttpUtil.RequestData("username", username);
        rs[2] = new OkHttpUtil.RequestData("password", password);
        rs[3] = new OkHttpUtil.RequestData("action", "login");
        rs[4] = new OkHttpUtil.RequestData("task", "login");
        rs[5] = new OkHttpUtil.RequestData("ref", "https://up.woozooo.com/");
        OkHttpUtil.postAsync(UriUtil.LOGIN, new OkHttpUtil.ResultCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                Log.d("222222", "bug");
            }

            @Override
            public void onResponse(byte[] response) {
                String data = new String(response);
                Log.d("2222223", data);
                Document document = Jsoup.parse(data);
                Elements element = document.getElementsByClass("info_b2");
                String linkText = element.text();
                Log.d("2222224", linkText);
                if (linkText.equals("登录成功，欢迎您回来。")) {
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else if (document.getElementsByClass("e_u").text().equals("账号不正确 密码不正确")) {
                    Toast.makeText(LoginActivity.this, "请检查账号或密码！", Toast.LENGTH_SHORT).show();
                    MyCookieJar.resetCookies();
                }
            }
        }, rs);
    }
}
