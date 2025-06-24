package com.kongzue.baseokhttpx.demo;

import static com.kongzue.baseokhttp.x.Get.getRequest;
import static com.kongzue.baseokhttp.x.Post.postRequest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.kongzue.baseokhttp.util.JsonList;
import com.kongzue.baseokhttp.util.JsonMap;
import com.kongzue.baseokhttp.x.BaseOkHttpX;
import com.kongzue.baseokhttp.x.Get;
import com.kongzue.baseokhttp.x.Post;
import com.kongzue.baseokhttp.x.interfaces.DownloadListener;
import com.kongzue.baseokhttp.x.interfaces.JsonResponseListener;
import com.kongzue.baseokhttp.x.interfaces.OpenAIAPIResponseListener;
import com.kongzue.baseokhttp.x.util.BaseHttpRequest;
import com.kongzue.baseokhttp.x.util.LockLog;
import com.kongzue.baseokhttpx.demo.databinding.ActivityMainBinding;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    String historyLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BaseOkHttpX.serviceUrl = "https://api.apiopen.top/";
        BaseOkHttpX.reserveServiceUrls = new String[]{"https://api.apiopen2.top/", "https://api.apiopen3.top/", "https://api.apiopen4.top/"};
        BaseOkHttpX.disallowSameRequest = true;
        //BaseOkHttpX.globalParameter = new Parameter().add("t1", "v1");

        // 日志拦截器测试
        LockLog.logListener = new LockLog.LogListener() {

            @Override
            public void log(LockLog.LogBody.LEVEL level, String logs) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (historyLogs == null) {
                            historyLogs = logs;
                        } else {
                            historyLogs = historyLogs + "\n" + logs;
                        }
                        binding.txtLogs.setText(historyLogs);
                    }
                });
            }
        };

        // 参数拦截器测试
//        BaseOkHttpX.parameterInterceptListener = new ParameterInterceptListener() {
//            @Override
//            public Object onIntercept(BaseHttpRequest httpRequest, String url, Object parameter) {
//                if (parameter == null) parameter = new Parameter();
//                if (parameter instanceof Parameter) {
//                    ((Parameter) parameter).add("customKey", "customValue");
//                }
//                return parameter;
//            }
//        };

        binding.btnGetTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Get.create("/api/sentences")
                        .addParameter("ids[]", 1, 2, 3, 4, 5)
                        .setCallback(new JsonResponseListener() {
                            @Override
                            public void onResponse(BaseHttpRequest httpRequest, JsonMap main, Exception error) {
                                binding.txtResult.setText(main.toString(4));
                            }
                        })
                        .go();
            }
        });

        binding.btnPostTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postRequest(MainActivity.this, "/api/login")
                        .setParameter(new JsonMap()
                                .set("account", "username")
                                .set("password", "123456")
                        )
                        .go(new JsonResponseListener() {
                            @Override
                            public void onResponse(BaseHttpRequest httpRequest, JsonMap main, Exception error) {
                                binding.txtResult.setText(main.toString(4));
                            }
                        });
            }
        });

        binding.btnDownloadTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File cacheFile = new File(getCacheDir(), "downloadTest.apk");
                getRequest(MainActivity.this, "https://dl.coolapk.com/down?pn=com.coolapk.market&id=NDU5OQ&h=46bb9d98&from=from-web")
                        .downloadToFile(cacheFile, new DownloadListener() {
                            @Override
                            public void onDownload(BaseHttpRequest httpRequest, File downloadFile, float progress, long current, long total, boolean done) {

                            }
                        })
                        .go();
            }
        });

        binding.btnGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://github.com/kongzue/BaseOkHttpX");
                Intent intent = new Intent("android.intent.action.VIEW", uri);
                startActivity(intent);
            }
        });

        binding.btnCleanLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyLogs = null;
                binding.txtLogs.setText("");
            }
        });

        binding.btnGptTest.setOnClickListener(new View.OnClickListener() {

            String deepSeekAPIKey = "apiKeys";

            @Override
            public void onClick(View v) {
                Post.create("https://api.deepseek.com/chat/completions")
                        .setStreamRequest(true)
                        .addHeader("Authorization", "Bearer " + deepSeekAPIKey)
                        .addHeader("Content-Type", "application/json")
                        .setParameter(new JsonMap()
                                .set("model", "deepseek-chat")
                                .set("messages", new JsonList()
                                        .set(new JsonMap()
                                                .set("role", "user")
                                                .set("content", "你是什么模型？能为我提供什么帮助？")
                                        )
                                )
                                .set("stream", true)
                                .set("temperature", 0.7))
                        .go(new OpenAIAPIResponseListener() {
                            @Override
                            public void onResponse(BaseHttpRequest httpRequest, String subText, String fullResponseText, Exception error, boolean isFinish) {
                                binding.txtResult.setText(fullResponseText);
                            }
                        });
            }
        });
    }
}