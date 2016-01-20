package com.akingyin.baidumapdemo;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Administrator on 2016/1/20.
 */
public class DemoApp  extends Application {

    //百度地图Key是否验证成功
    public   static   boolean   AuthBaiduMapKey=false;

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

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(getApplicationContext());
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);
    }



    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mReceiver);
    }
}
