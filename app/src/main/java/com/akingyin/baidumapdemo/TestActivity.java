package com.akingyin.baidumapdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.akingyin.baidumap.ReceiveLocListion;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * Created by Administrator on 2016/1/21.
 */
public class TestActivity extends AppCompatActivity  implements BDLocationListener{

    private ReceiveLocListion   listion;
    LocationClient  mLocClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        TestFragment   testFragment = TestFragment.newInstance(0,0);
        listion = testFragment;
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        option.setLocationMode(com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy);
        option.setProdName("watersys");
        option.SetIgnoreCacheException(true);
        mLocClient.setLocOption(option);
        getSupportFragmentManager().beginTransaction().add(R.id.frame_content,testFragment).commit();

    }


    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
         if(null != listion){
             System.out.println(bdLocation.getLatitude()+":"+bdLocation.getLongitude());
             listion.onReceiveLocation(bdLocation);
         }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocClient.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocClient.stop();
    }
}
