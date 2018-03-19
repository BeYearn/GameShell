package com.beyearn.gameshell;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.beyearn.gameshell.utils.EmaCallBackConst;
import com.beyearn.gameshell.utils.ThreadUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by beyearn on 2018/3/16.
 * <p>
 * 目前主要是支付功能
 *
 * 三部: 1. init  2. pay  3. onActivityResult 4.onDestroy
 *
 *
 */

public class GoogleUtils {

    private static GoogleUtils instance;

    private IInAppBillingService mService;
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };
    private Activity mActivity;
    private EmaSDKListener mPayListener;


    public static GoogleUtils getInstance() {
        if (instance == null) {
            instance = new GoogleUtils();
        }
        return instance;
    }

    public void init(Activity activity) {
        this.mActivity = activity;

        //绑定googleplay购买结算服务
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        activity.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }


    public void pay(final Map<String, String> payParams, EmaSDKListener payListener) {
        this.mPayListener = payListener;

        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, mActivity.getPackageName(),
                    "com.ema.wishes",//EmaUser.getInstance().getUserOrderInfo().getProduct_id(),
                    "inapp",
                    "id" + System.currentTimeMillis()/10000);//EmaUser.getInstance().getUserOrderInfo().getOrder_id());


            int responseCode = buyIntentBundle.getInt("RESPONSE_CODE ");
            Log.e("buyIntentBundle", "responseCode:" + responseCode);

            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            //在onActivityResult中获得响应
            if (pendingIntent.getIntentSender() == null) {
                Log.e("google pay", "the product is not consumed");
            } else {
                mActivity.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001,
                        new Intent(), Integer.valueOf(0),
                        Integer.valueOf(0), Integer.valueOf(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        switch (requestCode) {
            case 1001:
                if (intent == null) {
                    Log.e("google play", "onActivityResult's intent is null");
                    return;
                }

                int responseCode = intent.getIntExtra("RESPONSE_CODE", 0);
                final String purchaseData = intent.getStringExtra("INAPP_PURCHASE_DATA");        //购买信息
                final String dataSignature = intent.getStringExtra("INAPP_DATA_SIGNATURE");     //那个购买信息的签名后的内容

                if (resultCode == Activity.RESULT_OK) {

                    try {
                        JSONObject jsonObject = new JSONObject(purchaseData);
                        String sdkOrderId = jsonObject.getString("developerPayload");    //getBuyIntent 的最后一个参数
                        String productId = jsonObject.getString("productId");
                        String purchaseToken = jsonObject.getString("purchaseToken");


                        Log.e("googlepay", sdkOrderId + "  " + productId + "  " + purchaseToken);

                        mPayListener.onCallBack(EmaCallBackConst.PAYSUCCESS, "purchase successful");

                        //立即消耗了
                        consumePurchase(purchaseToken);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else if(resultCode == Activity.RESULT_CANCELED){
                    mPayListener.onCallBack(EmaCallBackConst.PAYCANCEl, "purchase cancel");
                }
                break;
        }
    }

    public void onDestory() {
        if (mService != null) {
            mActivity.unbindService(mServiceConn);
        }
    }


    /**
     * 查询可供购买的商品
     * 请不要在主线程上调用 getSkuDetails 方法。 调用此方法会触发网络请求，进而阻塞主线程。 请创建单独的线程并从该线程内部调用 getSkuDetails 方法。
     */
    public String getSkuDetail(String productId) {

        ArrayList<String> skuList = new ArrayList<String>();
        //skuList.add("gas");
        skuList.add("com.emagroups.wol.40");   //商品id
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
        try {
            Bundle skuDetails = mService.getSkuDetails(3, mActivity.getPackageName(), "inapp", querySkus);

            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

                JSONObject object = new JSONObject(responseList.get(0));
                String sku = object.getString("productId");
                String type = object.getString("type");
                String price = object.getString("price");
                String price_amount_micros = object.getString("price_amount_micros");
                String price_currency_code = object.getString("price_currency_code");
                String title = object.getString("title");
                String description = object.getString("description");

                return responseList.get(0);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询已购买的商品
     * 此方法将返回当前归用户拥有但未消耗的商品，包括购买的商品和通过兑换促销代码获得的商品。
     */
    public List<String> getPurchases() {
        try {
            Bundle ownedItems = mService.getPurchases(3, mActivity.getPackageName(), "inapp", null);

            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");//用于检索用户拥有的下一组应用内商品的继续令牌的字符串

                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    String signature = signatureList.get(i);
                    String sku = ownedSkus.get(i);
                    // do something with this purchase information
                    // e.g. display the updated list of products owned by user
                }
                return purchaseDataList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 测试  专门用于消耗已有商品
     */
    public void consumeHad() {

        List<String> purchases = getPurchases();
        for (String data : purchases) {
            try {
                JSONObject dataObj = new JSONObject(data);
                String purchaseToken = dataObj.getString("purchaseToken");
                consumePurchase(purchaseToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 消耗购买
     */
    public void consumePurchase(final String purchaseToken) {
        ThreadUtil.runInSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int response = mService.consumePurchase(3, mActivity.getPackageName(), purchaseToken);
                    Log.e("consumePurchase", "response: " + response);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

    }


}
