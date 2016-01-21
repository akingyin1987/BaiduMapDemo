package com.akingyin.baidumapdemo.baidumap;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.akingyin.baidumapdemo.DemoApp;
import com.akingyin.baidumapdemo.R;
import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.panoramaview.ImageMarker;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;
import com.baidu.lbsapi.tools.Point;

/**
 * Created by Administrator on 2016/1/21.
 */
public class BaiduPanoramaActivity extends AppCompatActivity {

    private PanoramaView mPanoView;
    ImageMarker   imageMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_panorama);
        mPanoView = (PanoramaView)findViewById(R.id.panorama);
        mPanoView.setShowTopoLink(true);
        double  lat = getIntent().getDoubleExtra("lat",0);
        double  lng = getIntent().getDoubleExtra("lng",0);


        imageMarker = new ImageMarker();
        imageMarker.setMarkerPosition(new Point(lat, lng));
        imageMarker.setMarkerHeight(2.6f);
        imageMarker.setMarker(getResources().getDrawable(R.drawable.icon_marka));
        mPanoView.addMarker(imageMarker);

        mPanoView.setPanoramaViewListener(new PanoramaViewListener() {
            @Override
            public void onLoadPanoramaBegin() {
                System.out.println("onLoadPanoramaBegin");
            }

            @Override
            public void onLoadPanoramaEnd(String s) {
                System.out.println("s=" + s);
            }

            @Override
            public void onLoadPanoramaError(final String s) {
                System.out.println("onLoadPanoramaError="+s);
            }
        });
        loadLatlng(lat, lng);
    }

    public   void  loadLatlng(final  double lat,final  double lng){
        new Thread(){
            @Override
            public void run() {
                super.run();
                mPanoView.setPanorama(lng, lat);
            }
        }.start();
    }


    private void initBMapManager() {
        DemoApp app = (DemoApp) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(app);
            app.mBMapManager.init(new DemoApp.MyGeneralListener());
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(null != mPanoView){
            mPanoView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(null != mPanoView){
            mPanoView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mPanoView){
            mPanoView.destroy();
            mPanoView = null;
        }
    }
}
