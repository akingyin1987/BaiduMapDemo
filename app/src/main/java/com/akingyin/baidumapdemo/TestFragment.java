package com.akingyin.baidumapdemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.akingyin.baidumap.BaseMapFragment;
import com.akingyin.clusterer.Cluster;
import com.akingyin.clusterer.Clusterer;
import com.akingyin.clusterer.OnPaintingClusterListener;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/21.
 */
public class TestFragment  extends BaseMapFragment implements OnPaintingClusterListener {

    private Clusterer<Mypoi> clusterer;

    public static TestFragment newInstance(double lat,double lng) {
        Bundle args = new Bundle();
        TestFragment fragment = new TestFragment();
        args.putDouble("lat", lat);
        args.putDouble("lng", lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initialization() {
        clusterer = new Clusterer<>(getContext(),getmBaiduMap());
        List<Mypoi>   mypois = new ArrayList<>();
        for(int i=0;i<1000;i++){
            double[]  lalng = TestUtil.Latlng();
            mypois.add(new Mypoi("test"+i,new LatLng(lalng[0],lalng[1])));
        }
        clusterer.setOnPaintingClusterListener(this);
        clusterer.setClustererListener(new Clusterer.ClustererClickListener<Mypoi>() {
            @Override
            public void markerClicked(Mypoi marker) {
                System.out.println("marker.click");
            }

            @Override
            public void clusterClicked(Cluster position) {
                System.out.println("cluster.click");
            }
        });
        clusterer.addAll(mypois);
        clusterer.forceUpdate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater) {

        return  inflater.inflate(R.layout.base_baidumap,null);
    }

    @Override
    public MarkerOptions onCreateClusterMarkerOptions(Cluster cluster) {
       MarkerOptions  marker= new MarkerOptions().position(cluster.getCenter());

        marker.title(Integer.valueOf(cluster.getWeight()).toString())

            .animateType(MarkerOptions.MarkerAnimateType.drop);
        if(cluster.getWeight()<=10){
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cluster_10));
        }else if(cluster.getWeight()>10 && cluster.getWeight()<20){
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cluster_20));
        }else if(cluster.getWeight()>20 && cluster.getWeight()<30){
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cluster_30));
        }else if(cluster.getWeight()>30 && cluster.getWeight()<50){
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cluster_50));
        }else{
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_cluster_100));
        }
        return marker;
    }

    @Override
    public void onMarkerCreated(MarkerOptions marker, Cluster cluster) {
         System.out.println("onMarkerCreated");
    }
}
