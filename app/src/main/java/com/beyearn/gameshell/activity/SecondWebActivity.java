package com.beyearn.gameshell.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.beyearn.gameshell.R;

import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by cenxiaozhong on 2017/7/22.
 * <p>
 */
public class SecondWebActivity extends BaseAgentWebActivity {

    //private TextView mTitleTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        //Toolbar mToolbar = (Toolbar) this.findViewById(R.id.toolbar);
        //mToolbar.setTitleTextColor(Color.WHITE);
        //mToolbar.setTitle("");
        //mTitleTextView = (TextView) this.findViewById(R.id.toolbar_title);
        //this.setSupportActionBar(mToolbar);
        /*if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecondWebActivity.this.finish();
            }
        });*/
        ButterKnife.bind(this);

        //增加js调用的方法
        mAgentWeb.getJsInterfaceHolder().addJavaObject("android", new AndroidInterface(mAgentWeb, this));
    }

    @NonNull
    @Override
    protected ViewGroup getAgentWebParent() {
        return (ViewGroup) this.findViewById(R.id.container);
    }

    @OnClick(R.id.bt_call_js)
    public void click1() {
        mAgentWeb.getJsAccessEntrace().quickCallJs("funcByAndroid");
    }

    @OnClick(R.id.bt_call_js＿param)
    public void click2() {
        mAgentWeb.getJsAccessEntrace().quickCallJs("funcByAndroidParam", "你好 js!");
    }

    @OnClick(R.id.bt_call_js_callback)
    public void click3() {
        mAgentWeb.getJsAccessEntrace().quickCallJs("funcByAndroidCallback", new ValueCallback<String>() {  //有回调的话和前面名字一样在js那边不算参数
            @Override
            public void onReceiveValue(String value) {
                Log.e("onReceiveValue",value);
            }
        }, getJson(),"xxxxxxx");
    }
    private String getJson(){
        String result="";
        try {
            JSONObject mJSONObject=new JSONObject();
            mJSONObject.put("id",1);
            mJSONObject.put("name","Agentweb");
            mJSONObject.put("age",18);
            result= mJSONObject.toString();
        }catch (Exception e){

        }
        return result;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAgentWeb != null && mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void setTitle(WebView view, String title) {
        //mTitleTextView.setText(title);
    }

    @Override
    protected int getIndicatorColor() {
        return Color.parseColor("#0000ff");
    }

    @Override
    protected int getIndicatorHeight() {
        return 30;
    }

    @Nullable
    @Override
    protected String getUrl() {
        //return "http://www.baidu.com";
        //return "http://emafish.lemonade-game.com/login";
        return "file:///android_asset/js_android.html";
    }
}
