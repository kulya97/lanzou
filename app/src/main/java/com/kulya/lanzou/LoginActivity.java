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
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends baseactivity {

    @BindView(R.id.login_id)
    EditText loginId;
    @BindView(R.id.login_key)
    EditText loginKey;
    @BindView(R.id.login_button)
    Button loginButton;
    private SharedPreferences.Editor editor;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
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
        loginId.setText(username);
        loginKey.setText(password);

    }


    //登录事件
    private void submit() {
        String id = loginId.getText().toString().trim();
        if (TextUtils.isEmpty(id)) {
            Toast.makeText(this, "id不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = loginKey.getText().toString().trim();
        if (TextUtils.isEmpty(key)) {
            Toast.makeText(this, "key不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        saveKey(id, key);
        login(id, key);
    }

    //登录
    private void login(final String username, final String password) {
        final QMUITipDialog mdialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .create();
        mdialog.show();
        HttpWorker.Login(username, password, new HttpWorker.loginCallbackListener() {
            @Override
            public void onError(Exception e) {
                mdialog.dismiss();
                Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                mdialog.dismiss();
                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }


    @OnClick(R.id.login_button)
    public void onClick() {
        submit();
    }
}
