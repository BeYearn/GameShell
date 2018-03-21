package com.beyearn.gameshell.activity;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.beyearn.gameshell.utils.ToastHelper;
import com.just.agentweb.AgentWeb;

/**
 * Created by cenxiaozhong on 2017/5/14.
 * source code  https://github.com/Justson/AgentWeb
 */

public class AndroidInterface {

    private Handler deliver = new Handler(Looper.getMainLooper());
    private AgentWeb agent;
    private Context context;

    public AndroidInterface(AgentWeb agent, Context context) {
        this.agent = agent;
        this.context = context;
    }


    @JavascriptInterface
    public void callAndroid(final String msg) {    //js那边调用必须遵循此处的方法签名
        deliver.post(new Runnable() {
            @Override
            public void run() {

                Log.i("Info", "main Thread:" + Thread.currentThread());
                ToastHelper.toast(context, "来自 js 的调用:" + msg);
            }
        });
        Log.i("Info", "Thread:" + Thread.currentThread());
    }

}
