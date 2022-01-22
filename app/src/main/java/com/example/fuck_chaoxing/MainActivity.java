package com.example.fuck_chaoxing;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//function_store.java文件中定义并实现全部功能

public class MainActivity extends AppCompatActivity {
    public static String cookies = "0";
    public static String uid = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button1 = findViewById(R.id.button1);

        //允许网络活动在当前线程执行
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        button1.setOnClickListener(view -> {
            EditText user_name_edit_text = findViewById(R.id.editTextTextPersonName);
            EditText pwd_edit_text = findViewById(R.id.editTextTextPassword);
            String str1 = user_name_edit_text.getText().toString();
            String str2 = pwd_edit_text.getText().toString();
//            登录并在AS中输出日志
            Log.i("用户名", str1);
            Log.i("密码", str2);
            Log.i("登录按钮", "被点击");
            String cookies = login(str1, str2);
            Log.i("login函数执行完毕，返回的cookies是", cookies + "");
            if (cookies.length() > 10) {
                String uid = cookies.split(";")[0].split("=")[1];
                Log.i("login函数执行完毕，返回的uid是", uid + "");
//                跳转到活动页面
                Intent intent = new Intent(MainActivity.this , MainActivity2.class);
                startActivity(intent);
            } else {
                show_Dialog("登录失败");
            }
        });
    }

    private void show_Dialog(String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher).setTitle("提示").setMessage(str).create().show();

    }

    public static String login(String user_account, String user_password) {
        Log.i("登录函数:", "开始尝试登录");

        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(@NonNull HttpUrl httpUrl, @NonNull List<Cookie> list) {
                cookies = "";
                for (Cookie cookie : list) {
                    String cookies_chip = cookie.toString().split(";")[0] + ";";
                    System.out.println("cookies: " + cookies_chip);
                    if (cookies_chip.contains("_uid") || cookies_chip.contains("vc3") || cookies_chip.contains("_d")|| cookies_chip.contains("UID")|| cookies_chip.contains("uf")) {
                        cookies += cookie.toString().split(";")[0] + ";";
                    }
                }
            }

            @NonNull
            @Override
            public List<Cookie> loadForRequest(@NonNull HttpUrl httpUrl) {

                return new ArrayList<>();
            }
        }).build();

        Request request = new Request.Builder().url("https://passport2-api.chaoxing.com/v11/loginregister?code=" + user_password + "&cx_xxt_passport=json&uname=" + user_account + "&loginType=1&roleSelect=true").build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            Log.i("同步获取", response.body() + "");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cookies;
    }
}