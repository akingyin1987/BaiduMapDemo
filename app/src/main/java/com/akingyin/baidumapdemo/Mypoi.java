package com.akingyin.baidumapdemo;


import com.akingyin.clusterer.Clusterable;
import com.baidu.mapapi.model.LatLng;

/**
 * Created by zlcd on 2016/2/18.
 */
public class Mypoi implements Clusterable {

    private   String  name;

    private   LatLng   latLng;

    public Mypoi(String name, LatLng latLng) {
        this.name = name;
        this.latLng = latLng;
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }
}
