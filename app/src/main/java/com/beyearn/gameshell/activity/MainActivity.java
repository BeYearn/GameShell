package com.beyearn.gameshell.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.beyearn.gameshell.R;
import com.beyearn.gameshell.activity.permission.DefaultRationale;
import com.beyearn.gameshell.activity.permission.LocationUtils;
import com.beyearn.gameshell.activity.permission.PermissionSetting;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
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
    CallbackManager fbCallbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        longitudeTv = (TextView) findViewById(R.id.tv_longitude);
        latitudeTv = (TextView) findViewById(R.id.tv_latitude);
        addressTv = (TextView) findViewById(R.id.tv_address);


        btStart1 = (Button) findViewById(R.id.bt_start1);
        btGetLoacation = (Button) findViewById(R.id.bt_get_location);
        btGetLoacation.setOnClickListener(this);
        btStart1.setOnClickListener(this);

        getfacebookKeyHash();
        facebookLogin();
    }

    private void getfacebookKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.beyearn.gameshell", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("facebook KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NoSuchAlgorithmException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void facebookLogin() {
        findViewById(R.id.bt_get_fb_info).setOnClickListener(this);

        fbCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "user_friends"));

        // If using in a fragment
        //loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("facebook", "onSuccess :" + loginResult.getAccessToken().getToken());
                Log.e("facebook", "onSuccess :" + loginResult.getAccessToken().getUserId());
            }

            @Override
            public void onCancel() {
                Log.e("facebook", "onCancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("facebook", "onError");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
        }
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
