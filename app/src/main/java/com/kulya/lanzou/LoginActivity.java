package com.kulya.lanzou;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kulya.lanzou.http.HttpWorker;
import com.kulya.lanzou.util.baseactivity;

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
        String username = pref.getString("id", "");
        String password = pref.getString("key", "");
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
        login(id, key);
    }

    //登录
    private void login(final String username, final String password) {
        HttpWorker.Login(username, password, new HttpWorker.loginCallbackListener() {
            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });

            }
        });


    }
}
