package com.akingyin.clusterer;



import com.baidu.mapapi.map.MarkerOptions;

/**
 * Created by mrm on 7/4/15.
 */
public interface OnPaintingClusterableMarkerListener<T> {
    MarkerOptions onCreateMarkerOptions(T item);

    void onMarkerCreated(MarkerOptions marker, T item);
}