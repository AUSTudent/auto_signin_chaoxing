package com.example.fuck_chaoxing;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
        //允许网络活动在当前线程执行
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        get_course_list();


    }

    public void activities_list(String courseId, String classId, String uid, String cpi) {
        Log.i("activities_list函数", "程序开始执行");
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_2_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 com.ssreader.ChaoXingStudy/ChaoXingStudy_3_4.8_ios_phone_202012052220_56 (@Kalimdor)_12787186548451577248")
                .addHeader("Cookie", MainActivity.cookies)
                .url("https://mobilelearn.chaoxing.com/ppt/activeAPI/taskactivelist?courseId=" + courseId + "&classId=" + classId + "&uid=" + uid + "&cpi=" + cpi)
                .build();//获取签到活动
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();//获取当前课程所有签到活动
            //取返回值之后分析代码中是否有活动，如果有则启动自动签到功能
            JSONObject jsonObject = JSON.parseObject(response.body().string());
            String activeList_str = jsonObject.getString("activeList");
            JSONArray jsonArray = JSONArray.parseArray(activeList_str);
            int len = jsonArray.size();
            for (int i = 0; i < len; i++) {//FIXME:多个签到活动同时存在且未过期时只能完成时间较早的一个，疑似循环控制问题或者active_id获取重合
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);//获取单个签到活动
                Log.i("签到活动状态", jsonObject1.getString("status"));
                if (jsonObject1.getString("status").equals("2")) {//因为学习通会自动排序，所以当扫描到一个过期活动时，说明所有未过期活动都已经遍历完成，故结束循环
                    if (i == 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = findViewById(R.id.textView4);
                                textView.setText("没有需要签的签到");
                            }
                        });
                    }
                    break;
                }
                //在循环中，还没有遍历到过期活动，则继续循环，并执行签到程序
                //根据签到类型进行签到操作，并改变文字提示
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = findViewById(R.id.textView4);
                        textView.setText("签到活动获取成功");
                        LinearLayout linearLayout = findViewById(R.id.set_name);
                        linearLayout.setVisibility(View.VISIBLE);
                        Button button = findViewById(R.id.signin_button);
                        button.setVisibility(View.VISIBLE);
                        String activeType_self = get_active_type(jsonObject1.getString("id"));
                        if (activeType_self.equals("3") || activeType_self.equals("4")){
                            LinearLayout linearLayout1 = findViewById(R.id.set_enc);
                            linearLayout1.setVisibility(View.VISIBLE);
                        }
                        if (activeType_self.equals("5")){
                            LinearLayout linearLayout1 = findViewById(R.id.set_addr);
                            linearLayout1.setVisibility(View.VISIBLE);
                            LinearLayout linearLayout2= findViewById(R.id.set_long);
                            linearLayout2.setVisibility(View.VISIBLE);
                            LinearLayout linearLayout3 = findViewById(R.id.set_latitude);
                            linearLayout3.setVisibility(View.VISIBLE);
                        }
                    }
                });

                Button button = findViewById(R.id.signin_button);
                button.setOnClickListener(view -> {
                    EditText editText = findViewById(R.id.user_name);
                    String user_name = editText.getText().toString();
                    Log.i("一键签到按钮", "开始执行一键签到程序");
                    signin_entrance(courseId, classId, uid, cpi, user_name,get_active_type(jsonObject1.getString("id")),jsonObject1.getString("id"));//点击按钮触发签到

                });
            }
            //TODO:后期根据需求设置一次性扫描的课程数量的上限
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

        try {//TODO:这里后期可以增加查询到的活动来自于哪个课堂的提示
            Response response = call.execute();
            String return_course = response.body().string();
            JSONObject jsonObject = JSON.parseObject(return_course);
            JSONArray jsonArray = jsonObject.getJSONArray("channelList");
            int json_array_len = jsonArray.size();
            for (int i = 0; i < json_array_len; i++) {//这一个循环用于遍历每一个课程
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

//                获取签到任务函数实现
                System.out.println("调试结果" + classId + "\t" + cpi + "\t" + courseId);
                activities_list(courseId, classId, MainActivity.uid, cpi);

            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("获取课程表", "失败" + e);
        }
    }

    public void signin_entrance(String courseId, String classId, String uid, String cpi, String uname,String activeType,String active_id) {//此处的activeType还不是最终的，还需要进行一次处理才能得出签到请求类型，在这里只做定义变量的作用
        //此处参数中active_id,uid,uname为必须，其他部分为拓展预留变量，可以根据需求修改删除

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.textView4);
                textView.setText("正在执行签到");
            }
        });
        if (activeType.equals("1")){
            int result = signin_common(active_id,uid,uname,"");
        }
        else if (activeType.equals("2") || activeType.equals("6")){
            int result = signin_common(active_id,uid,uname,"");
        }
        else if (activeType.equals("4") || activeType.equals("3")){
            EditText editText = findViewById(R.id.enc_code);
            String enc_code = editText.getText().toString();
            signin_by_QRcode(active_id,uid,uname,enc_code);
        }
        else if (activeType.equals("5")){
            int result = signin_by_position(active_id,uid,uname);
        }
//        else if (activeType.equals("6")){
//            signin_by_photo();
//        }
    }
//    public void signin_by_photo() {//对应签到activeType=1
//        Log.i("signin_by_photo", "开始拍照签到");
//    }

    public int signin_common(String active_id,String uid,String uname,String extra_url) {//对应签到activeType=2 / 6（手势）
        //负责完成普通签到功能
        Log.i("signin_by_photo", "开始签到");
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .addHeader("Cookie",MainActivity.cookies)
                .url("https://mobilelearn.chaoxing.com/pptSign/stuSignajax?activeId="+active_id+"&uid="+uid+"&clientip=&latitude=-1&longitude=-1&appType=15&fid=0&name="+uname+extra_url)
                .build();
        Log.i("签到请求的URL为", "https://mobilelearn.chaoxing.com/pptSign/stuSignajax?activeId="+active_id+"&uid="+uid+"&clientip=&latitude=-1&longitude=-1&appType=15&fid=0&name="+uname+extra_url);
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            String signin_result = response.body().string();
            Log.i("签到结果返回", signin_result);
            if (signin_result.equals("success") ||signin_result.equals("您已签到过了")){
                runOnUiThread(new Runnable() {//将文本改为签到成功
                    @Override
                    public void run() {
                        TextView textView = findViewById(R.id.textView4);
                        textView.setText("签到成功");
                    }
                });
                return 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int signin_by_QRcode(String active_id,String uid,String uname,String enc) {//对应签到activeType=4
        Log.i("signin_by_photo", "开始静态二维码签到");
        int result = signin_common(active_id,uid,uname,("&enc="+enc));
        return result;
    }

//    public void signin_by_dynamic_QRcode() {//对应签到activeType=3
    //此函数已废弃
//        Log.i("signin_by_photo", "开始动态二维码签到");
//    }

    public int signin_by_position(String active_id,String uid,String uname) {//对应签到activeType=5
        Log.i("signin_by_photo", "开始定位签到");
        EditText editText = findViewById(R.id.address_name);
        EditText editText2 = findViewById(R.id.long_);
        EditText editText3 = findViewById(R.id.latitude);
        int result = signin_common(active_id,uid,uname,"&address="+editText.getText()+"&latitude="+editText3.getText()+"&longitude="+editText2.getText());
        return result;
    }

    public String get_active_type(String active_id){
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        String activeType_self = "";
        Request request = new Request.Builder()
                .url("https://mobilelearn.chaoxing.com/newsign/signDetail?activePrimaryId="+active_id+"&type=1&")
                .addHeader("Cookie", MainActivity.cookies)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            JSONObject jsonObject = JSONObject.parseObject(response.body().string());
            String otherId = jsonObject.getString("otherId");
            String ifPhoto = jsonObject.getString("ifPhoto");
            String ifRefreshEwm = jsonObject.getString("ifRefreshEwm");
            if (otherId.equals("0")){
                if (ifPhoto.equals("1")){
                    activeType_self = "1";
                }
                else {
                    activeType_self = "2";
                }
            }
            else if (otherId.equals("2")){
                if (ifRefreshEwm.equals("1")){
                    activeType_self = "3";
                }
                else {
                    activeType_self = "4";
                }
            }
            else if (otherId.equals("3")){
                activeType_self = "6";
            }
            else if (otherId.equals("4")){
                activeType_self = "5";
            }


            Log.i("获取到的签到类型是", activeType_self);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return activeType_self;
    }

}