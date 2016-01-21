package com.akingyin.baidumapdemo;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Administrator on 2016/1/20.
 */
public class DemoApp  extends Application {

    public BMapManager mBMapManager = null;

    private static DemoApp mInstance = null;

    //百度地图Key是否验证成功
    public   static   boolean   AuthBaiduMapKey=false;

    public static DemoApp getInstance() {
        return mInstance;
    }

    public class SDKReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                // key 验证失败，相应处理
                Toast.makeText(context, "地图KEY验证失败", Toast.LENGTH_SHORT).show();
                AuthBaiduMapKey = false;

            } else if (action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                // 网络出错，相应处理
                Toast.makeText(context, "网络出错无法验证", Toast.LENGTH_SHORT).show();
                AuthBaiduMapKey = false;
            }else if(action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)){
                AuthBaiduMapKey = true;
            }else if(action.equals(SDKInitializer.	SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE)){
                AuthBaiduMapKey = false;
            }
        }

    }
    private  SDKReceiver  mReceiver;

    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(new MyGeneralListener())) {
            Toast.makeText(DemoApp.getInstance().getApplicationContext(), "BMapManager  初始化错误!",
                    Toast.LENGTH_LONG).show();
        }
        Log.d("ljx", "initEngineManager");
    }

    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    public static class MyGeneralListener implements MKGeneralListener {

        @Override
        public void onGetPermissionState(int iError) {
            // 非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
                Toast.makeText(DemoApp.getInstance().getApplicationContext(),
                        "请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: " + iError, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(DemoApp.getInstance().getApplicationContext(), "key认证成功", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
        SDKInitializer.initialize(getApplicationContext());
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);
        initEngineManager(this);
    }



    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mReceiver);
    }
}
