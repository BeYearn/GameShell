package com.beyearn.gameshell.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.beyearn.gameshell.EmaSDKListener;
import com.beyearn.gameshell.GoogleUtils;
import com.beyearn.gameshell.R;
import com.beyearn.gameshell.permission.DefaultRationale;
import com.beyearn.gameshell.permission.LocationUtils;
import com.beyearn.gameshell.permission.PermissionSetting;
import com.beyearn.gameshell.utils.EmaCallBackConst;
import com.beyearn.gameshell.utils.ToastHelper;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.HashMap;
import java.util.List;

/**
 * Created by beyearn on 2018/3/12.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btStart1;
    private TextView longitudeTv;
    private TextView latitudeTv;
    private TextView addressTv;
    private Button btGetLoacation;
    Button btLoginPage;
    private Button btgetFbLoginInfo;
    private Button btgetGlLoginInfo;
    private Button btFbLogout;
    private Button btGlLogout;
    private Button btGlPay;
    private Button btGlConsume;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        longitudeTv = (TextView) findViewById(R.id.tv_longitude);
        latitudeTv = (TextView) findViewById(R.id.tv_latitude);
        addressTv = (TextView) findViewById(R.id.tv_address);
        btLoginPage = (Button) findViewById(R.id.bt_login_page);
        btgetFbLoginInfo = (Button) findViewById(R.id.bt_get_fb_info);
        btgetGlLoginInfo = (Button) findViewById(R.id.bt_get_gl_info);
        btFbLogout = (Button) findViewById(R.id.bt_fb_logout);
        btGlLogout = (Button) findViewById(R.id.bt_gl_logout);
        btGlPay = (Button) findViewById(R.id.bt_gl_pay);
        btGlConsume = (Button) findViewById(R.id.bt_gl_consume);

        btStart1 = (Button) findViewById(R.id.bt_start1);
        btGetLoacation = (Button) findViewById(R.id.bt_get_location);
        btGetLoacation.setOnClickListener(this);
        btgetFbLoginInfo.setOnClickListener(this);
        btgetGlLoginInfo.setOnClickListener(this);
        btStart1.setOnClickListener(this);
        btLoginPage.setOnClickListener(this);
        btFbLogout.setOnClickListener(this);
        btGlLogout.setOnClickListener(this);
        btGlPay.setOnClickListener(this);
        btGlConsume.setOnClickListener(this);

        GoogleUtils.getInstance().init(this);
    }


    private void requestPermission() {

        //这个方法不会阻塞,异步的
        AndPermission.with(MainActivity.this)
                .permission(Permission.Group.LOCATION)
                .rationale(new DefaultRationale())
                .onGranted(new Action() {
                    @Override
                    public void onAction(List<String> list) {
                        Toast.makeText(MainActivity.this, "location permission get", Toast.LENGTH_LONG).show();
                    }
                })
                .onDenied(new Action() {
                    @Override
                    public void onAction(List<String> list) {
                        Toast.makeText(MainActivity.this, "location permission did not get", Toast.LENGTH_LONG).show();
                        if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, Permission.Group.LOCATION)) {
                            new PermissionSetting(MainActivity.this).showSetting(list);
                        }
                    }
                })
                .start();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_start1:
                startActivity(new Intent(this, FirstWebActivity.class));
                break;
            case R.id.bt_get_location:
                getLoaction();
                break;
            case R.id.bt_get_fb_info:
                boolean loggedIn = AccessToken.getCurrentAccessToken() != null;
                Log.e("facebook", "loggedIn:" + loggedIn);
                if (loggedIn) {
                    Profile currentProfile = Profile.getCurrentProfile();
                    String id = currentProfile.getId();
                    String name = currentProfile.getName();
                    String firstName = currentProfile.getFirstName();
                    Uri linkUri = currentProfile.getLinkUri();

                    Log.e("facebook", id + "  " + name + "  " + firstName + "  " + linkUri);
                }
                break;
            case R.id.bt_fb_logout:
                LoginManager manager = LoginManager.getInstance();
                manager.logOut();
                break;
            case R.id.bt_get_gl_info:
                // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                boolean loggedInGl = account != null;
                Log.e("google", "loggedIn:" + loggedInGl);
                if (loggedInGl) {
                    String email = account.getEmail();
                    String id = account.getId();

                    Log.e("google", id + "  " + email);
                }

                break;
            case R.id.bt_gl_logout:
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MainActivity.this, "google logout", Toast.LENGTH_LONG).show();
                            }
                        });
                break;
            case R.id.bt_login_page:
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            case R.id.bt_gl_pay:

                HashMap<String, String> payParams = new HashMap<>();
                payParams.put("server_id", "168000100001");
                payParams.put("quantity", "1");
                payParams.put("product_id", "com.emagroups.wol.40");
                payParams.put("role_id", "test001");
                payParams.put("area_id", "xxxx");
                payParams.put("consume_now", "true");  // 是否立刻消耗


                GoogleUtils.getInstance().pay(payParams, new EmaSDKListener() {
                    @Override
                    public void onCallBack(int resultCode, String decr) {
                        ToastHelper.toast(MainActivity.this, decr);
                        switch (resultCode) {
                            case EmaCallBackConst.PAYSUCCESS:

                                break;
                            case EmaCallBackConst.PAYFALIED:

                                break;
                            case EmaCallBackConst.PAYCANCEl:

                                break;
                        }
                    }
                });
                break;
            case R.id.bt_gl_consume:
                GoogleUtils.getInstance().consumeHad();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        GoogleUtils.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GoogleUtils.getInstance().onDestory();
    }

    private void getLoaction() {
        requestPermission();

        Log.e("getloaction", "getlocation");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int isPermission = MainActivity.this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (isPermission == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(MainActivity.this, "permision is forbidden", Toast.LENGTH_LONG).show();
                return;
            }
        }

        LocationUtils.getInstance(this).getLocation(new LocationUtils.LocationCallBack() {
            @Override
            public void setLocation(Location location) {
                if (location != null) {
                    longitudeTv.setText("经度:" + String.valueOf(location.getLongitude()));
                    latitudeTv.setText("纬度:" + String.valueOf(location.getLatitude()));

                    //引发下面的调用
                    LocationUtils.getInstance(MainActivity.this).getAddress(location.getLatitude(),
                            location.getLongitude());
                }
            }

            @Override
            public void setAddress(Address address) {
                if (address != null) {
                    addressTv.setText(
                            "国家code:" + address.getCountryCode() + "\n"
                                    + "城市名:" + address.getLocality() + "\n"
                                    + "周边信息:" + LocationUtils.getInstance(MainActivity.this).
                                    getAddressLine(address));
                }
            }
        });
    }
}
