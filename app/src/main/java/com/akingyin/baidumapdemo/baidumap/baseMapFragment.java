package com.akingyin.baidumapdemo.baidumap;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;


import com.akingyin.baidumapdemo.R;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

/**
 * Created by Administrator on 2016/1/20.
 */
public   abstract  class baseMapFragment  extends Fragment  implements ReceiveLocListion {

      private MapView mMapView;
      private BaiduMap mBaiduMap;

    private ImageView  location_icon;//地图模式（正常，跟随，罗盘）
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;

    private ImageButton  zoom_in,zoom_out;
    private ImageButton  road_condition;//交通
    private ImageButton  map_layers;//地图类型（普通2d,普通3d,卫星）
    private ImageButton  map_street;//全景


    public MyLocationData locdata = null; // 定位数据
    public MyLocationConfiguration locConfig = null;



    public MapView getmMapView() {
        return mMapView;
    }


    public BaiduMap getmBaiduMap() {
        return mBaiduMap;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View  view = onCreateView(inflater);
        mMapView = (MapView)view.findViewById(R.id.map_content);
        mBaiduMap = mMapView.getMap();

        location_icon = (ImageView)view.findViewById(R.id.location_icon);
        zoom_out = (ImageButton)view.findViewById(R.id.zoom_out);
        zoom_out = (ImageButton)view.findViewById(R.id.zoom_in);
        road_condition = (ImageButton)view.findViewById(R.id.road_condition);
        map_layers = (ImageButton)view.findViewById(R.id.map_layers);
        map_street = (ImageButton)view.findViewById(R.id.map_street);
        baseInitialization(getArguments());
        return view;
    }

    private void baseInitialization(Bundle  bundle){
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开始交通地图
        mBaiduMap.setTrafficEnabled(false);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        locConfig = new MyLocationConfiguration(mCurrentMode, true, null);
        mBaiduMap.setMyLocationConfigeration(locConfig);
        double  lat = bundle.getDouble("lat",0);
        double  lng = bundle.getDouble("lng",0);
        LatLng   latLng = null;
        if(lat >0 && lng >0 ){
            latLng = new LatLng(lat,lng);
            MapStatus mMapStatus = new MapStatus.Builder().zoom(15).target(latLng).build();
            MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

            mBaiduMap.animateMapStatus(statusUpdate);
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
                map_layers.setImageResource(R.drawable.main_icon_close);

            }
        });

    }




    public   abstract   void    initialization();
    public  abstract    View    onCreateView(LayoutInflater inflater);

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
}
