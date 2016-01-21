package com.akingyin.baidumapdemo.baidumap;

import android.os.Bundle;
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
        double  lat = getIntent().getDoubleExtra("lat",0);
        double  lng = getIntent().getDoubleExtra("lng",0);
        imageMarker = new ImageMarker();
        imageMarker.setMarkerPosition(new Point(lat,lng));
        imageMarker.setMarkerHeight(2.6f);
        imageMarker.setMarker(getResources().getDrawable(R.drawable.icon_marka));
        mPanoView.addMarker(imageMarker);
        mPanoView.setShowTopoLink(true);
        mPanoView.setPanorama(lng, lat);
        mPanoView.setPanoramaViewListener(new PanoramaViewListener() {
            @Override
            public void onLoadPanoramaBegin() {

            }

            @Override
            public void onLoadPanoramaEnd(String s) {

            }

            @Override
            public void onLoadPanoramaError(String s) {
                Toast.makeText(BaiduPanoramaActivity.this,s,Toast.LENGTH_SHORT).show();
            }
        });
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
