package com.example.fuck_chaoxing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
//        TODO:获取课程列表
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://mooc1-api.chaoxing.com/mycourse/backclazzdata?view=json&rss=1")
                .addHeader("Cookie",MainActivity.cookies)
                .build();
        Call call = okHttpClient.newCall(request);

        try {
            Response response = call.execute();
            String return_course = response.body().string();
            JSONObject jsonObject = JSON.parseObject(return_course);
//            String course_list = jsonObject.getString("channelList");
//            Log.i("获取课程表", course_list + "");
            JSONArray jsonArray = jsonObject.getJSONArray("channelList");
            for (int i=0;i<jsonArray.size();i++){
                JSONObject jsonObject1 = jsonArray.getJSONObject(i); //遍历所有课表
                String jsonObject_tmp = jsonObject1.getString("content");
                JSONObject jsonObject2 = JSONObject.parseObject(jsonObject_tmp); //jsonObject2即为返回的JSON数组中content中的内容
                if (jsonObject2.getString("roletype")!="3"){
                    continue;
                }

////                TODO:获取签到任务函数实现
//                Log.i("零食调试", jsonObject2.getString("roletype")+"");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("获取课程表", "失败"+e);
        }


    }

    public void get_signin_task(){
        Log.i("get_signin_task函数：", "程序开始执行");
    }
}