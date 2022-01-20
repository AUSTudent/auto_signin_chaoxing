package com.example.fuck_chaoxing;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

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

    public void activities_list() {
        Log.i("activities_list函数：", "程序开始执行");
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

        try {
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


            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("获取课程表", "失败" + e);
        }
    }
}