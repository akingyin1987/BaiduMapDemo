package com.akingyin.baidumap;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.akingyin.baidumapdemo.R;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

/**
 * Created by zlcd on 2016/2/18.
 */
public abstract class BaseMapActivity  extends AppCompatActivity implements BDLocationListener {
    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private ImageView location_icon;//地图模式（正常，跟随，罗盘）
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;

    private ImageButton zoom_in,zoom_out;
    private ImageButton  road_condition;//交通
    private ImageButton  map_layers;//地图类型（普通2d,普通3d,卫星）
    private ImageButton  map_street;//全景

    private MyLocationData locdata = null; // 定位数据
    private MyLocationConfiguration locConfig = null;

    private View maplayer;//地图类型


    public MapView getmMapView() {
        return mMapView;
    }


    public BaiduMap getmBaiduMap() {
        return mBaiduMap;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View   view  =  onCreateView(LayoutInflater.from(this));
        setContentView(view);
        initView(savedInstanceState);
        initLoc();
        initialization();
    }


    public   void   initView(Bundle savedInstanceState ){
        mMapView = (MapView)findViewById(R.id.map_content);
        mBaiduMap = mMapView.getMap();

        location_icon = (ImageView)findViewById(R.id.location_icon);
        zoom_out = (ImageButton)findViewById(R.id.zoom_out);
        zoom_in = (ImageButton)findViewById(R.id.zoom_in);
        road_condition = (ImageButton)findViewById(R.id.road_condition);
        map_layers = (ImageButton)findViewById(R.id.map_layers);
        map_street = (ImageButton)findViewById(R.id.map_street);
        baseInitialization(savedInstanceState);
        initialization();
    }

    private  LocationClient  mLocClient;
    private  void  initLoc(){
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
        mLocClient.start();
    }



    private void baseInitialization(Bundle  bundle){
        mMapView.showZoomControls(true);//隐藏比例尺

        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开始交通地图
        mBaiduMap.setTrafficEnabled(false);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        locConfig = new MyLocationConfiguration(mCurrentMode, true, null);
        mBaiduMap.setMyLocationConfigeration(locConfig);
        if(null != bundle){
            double  lat = bundle.getDouble("lat",0);
            double  lng = bundle.getDouble("lng",0);
            LatLng latLng = null;
            if(lat >0 && lng >0 ){
                latLng = new LatLng(lat,lng);
                MapStatus mMapStatus = new MapStatus.Builder().zoom(15).target(latLng).build();
                MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

                mBaiduMap.animateMapStatus(statusUpdate);
            }
        }

        location_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        location_icon.setImageResource(R.drawable.main_icon_follow);
                        break;
                    case COMPASS:
                        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                        location_icon.setImageResource(R.drawable.main_icon_location);
                        break;
                    case FOLLOWING:
                        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        location_icon.setImageResource(R.drawable.main_icon_compass);
                        break;
                    default:
                        break;
                }
                mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, null));
            }
        });

        road_condition.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mBaiduMap.isTrafficEnabled()) {
                    road_condition.setImageResource(R.drawable.main_icon_roadcondition_off);
                    mBaiduMap.setTrafficEnabled(false);
                } else {
                    road_condition.setImageResource(R.drawable.main_icon_roadcondition_on);
                    mBaiduMap.setTrafficEnabled(true);
                }

            }
        });
        map_layers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showMapLayerDialog(v,500,-5);
            }
        });

        maplayer = LayoutInflater.from(this).inflate(R.layout.map_layer,null);
        layer_selector = (RadioGroup)maplayer.findViewById(R.id.layer_selector);
        layer_satellite = (RadioButton)maplayer.findViewById(R.id.layer_satellite);
        layer_2d = (RadioButton)maplayer.findViewById(R.id.layer_2d);
        layer_3d = (RadioButton)maplayer.findViewById(R.id.layer_3d);
        layer_2d.setChecked(true);
        layer_selector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.layer_satellite) {
                    //卫星
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                } else if (checkedId == R.id.layer_2d) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    MapStatus ms = new MapStatus.Builder(mBaiduMap.getMapStatus()).overlook(0).build();
                    MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
                    mBaiduMap.animateMapStatus(u);
                } else if (checkedId == R.id.layer_3d) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                    MapStatus ms = new MapStatus.Builder(mBaiduMap.getMapStatus()).overlook(-45).build();
                    MapStatusUpdate u = MapStatusUpdateFactory.newMapStatus(ms);
                    mBaiduMap.animateMapStatus(u);
                }
            }
        });
        zoom_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float supportmax = mBaiduMap.getMaxZoomLevel();
                float localzoom = mBaiduMap.getMapStatus().zoom;

                if (localzoom == supportmax) {
                    zoom_in.setEnabled(false);
                    Toast.makeText(BaseMapActivity.this, "已到支持最大级别", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!zoom_out.isEnabled()) {
                    zoom_out.setEnabled(true);
                }
                MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(localzoom + 0.5f);
                mBaiduMap.animateMapStatus(u);
            }
        });
        zoom_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float supportmin = mBaiduMap.getMinZoomLevel();
                float localzoom = mBaiduMap.getMapStatus().zoom;
                if (localzoom == supportmin) {
                    zoom_out.setEnabled(false);
                    Toast.makeText(BaseMapActivity.this, "已到支持最小级别", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!zoom_in.isEnabled()){
                    zoom_in.setEnabled(true);
                }
                MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(localzoom - 0.5f);
                mBaiduMap.animateMapStatus(u);
            }
        });

        map_street.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
    }



    private PopupWindow mPopupWindow;
    private RadioGroup  layer_selector;
    private RadioButton  layer_satellite,layer_2d,layer_3d;
    public   void    showMapLayerDialog(View v, int xoff, int yoff){

        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(maplayer, RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT, true);
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        }
        if (mPopupWindow.isShowing()) {
            mPopupWindow.setAnimationStyle(R.anim.layer_pop_out);

            mPopupWindow.dismiss();
        } else {
            mPopupWindow.setAnimationStyle(R.anim.layer_pop_in);
            mPopupWindow.showAsDropDown(v, xoff, yoff);

        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != mBaiduMap){
            //关闭定位层
            mBaiduMap.setMyLocationEnabled(false);
        }
        if(null != mMapView){
            mMapView.onDestroy();
            mMapView = null;

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(null != mMapView){
            mMapView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(null != mMapView){
            mMapView.onResume();
        }
    }
    private   boolean isFirstLoc = true;
    @Override
    public void onReceiveLocation(BDLocation location) {
        // map view 销毁后不在处理新接收的位置
        if (location == null || mMapView == null) {
            return;
        }
        MyLocationData locData = new MyLocationData.Builder()
            .accuracy(location.getRadius())
            // 此处设置开发者获取到的方向信息，顺时针0-360
            .direction(location.getDirection()).latitude(location.getLatitude())
            .longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);
        if (isFirstLoc) {
            isFirstLoc = false;
            LatLng ll = new LatLng(location.getLatitude(),
                location.getLongitude());
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);
        }
    }

    public   abstract   void    initialization();
    public  abstract    View    onCreateView(LayoutInflater inflater);

}
