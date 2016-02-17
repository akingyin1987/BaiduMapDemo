package com.akingyin.clusterer;



import com.baidu.mapapi.map.MarkerOptions;

public interface OnPaintingClusterListener<T extends Clusterable> {
    MarkerOptions onCreateClusterMarkerOptions(Cluster<T> cluster);

    void onMarkerCreated(MarkerOptions marker, Cluster<T> cluster);
}