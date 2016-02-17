package com.akingyin.clusterer;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.overlayutil.OverlayManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlcd on 2016/2/17.
 */
public class MyMarkerManager extends OverlayManager {

    private   List<OverlayOptions>   overlayOptionses = new ArrayList<>();

    public    void   addMarker(OverlayOptions  options){
        overlayOptionses.add(options);
    }

    public   void   addMarkers(List<OverlayOptions> optionses){
        overlayOptionses.addAll(optionses);
    }

    public    void  cleanMarker(OverlayOptions  options){
        overlayOptionses.remove(options);
    }

    public   void   cleanAllMarker(){
        overlayOptionses.clear();
    }


    public MyMarkerManager(BaiduMap baiduMap) {
        super(baiduMap);
    }

    @Override
    public List<OverlayOptions> getOverlayOptions() {
        return overlayOptionses;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        return false;
    }
}
