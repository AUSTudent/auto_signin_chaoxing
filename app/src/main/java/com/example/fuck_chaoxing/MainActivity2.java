package com.example.fuck_chaoxing;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        get_course_list();


    }

    public void activities_list(String courseId, String classId, String uid, String cpi) {
        Log.i("activities_list函数：", "程序开始执行");
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_2_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 com.ssreader.ChaoXingStudy/ChaoXingStudy_3_4.8_ios_phone_202012052220_56 (@Kalimdor)_12787186548451577248")
                .addHeader("Cookie", MainActivity.cookies)
                .url("https://mobilelearn.chaoxing.com/ppt/activeAPI/taskactivelist?courseId=" + courseId + "&classId=" + classId + "&uid=" + uid + "&cpi=" + cpi)
                .build();//获取签到活动
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            Log.i("activities_list函数：", response.body().string());
                    //TODO:获取返回值之后分析代码中是否有活动
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void get_course_list() {
        Log.i("get_course_list函数：", "程序开始执行");
        System.out.println("开始获取课程表");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://mooc1-api.chaoxing.com/mycourse/backclazzdata?view=json&rss=1")
                .addHeader("Cookie", MainActivity.cookies)
                .build();
        Call call = okHttpClient.newCall(request);

        try {//TODO:这里可以增加查询到的活动来自于哪个课堂的提示
            Response response = call.execute();
            String return_course = response.body().string();
            JSONObject jsonObject = JSON.parseObject(return_course);
            JSONArray jsonArray = jsonObject.getJSONArray("channelList");
            int json_array_len = jsonArray.size();
            for (int i = 0; i < json_array_len; i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i); //遍历所有课表
                String jsonObject_tmp = jsonObject1.getString("content");
                JSONObject jsonObject2 = JSONObject.parseObject(jsonObject_tmp); //jsonObject2即为返回的JSON数组中content中的内容
                String roletype = jsonObject2.getString("roletype");
                if (!roletype.equals("3")) {
                    continue;
                }
                String classId = jsonObject2.getString("id");
                String cpi = jsonObject2.getString("cpi");
                jsonObject_tmp = jsonObject2.getString("course");
                jsonObject2 = JSONObject.parseObject(jsonObject_tmp);//jsonObject2即为返回的JSON数组中[content][course]中的内容
                jsonObject_tmp = jsonObject2.getString("data");
                JSONArray jsonArray2 = JSONArray.parseArray(jsonObject_tmp);//jsonObject2即为返回的JSON数组中[content][course][data]中的内容
                jsonObject2 = jsonArray2.getJSONObject(0);
                String courseId = jsonObject2.getString("id");

//                TODO:获取签到任务函数实现
                System.out.println("调试结果" + classId + "\t" + cpi + "\t" + courseId);
                activities_list(courseId, classId, MainActivity.uid, cpi);

            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("获取课程表", "失败" + e);
        }
    }
}