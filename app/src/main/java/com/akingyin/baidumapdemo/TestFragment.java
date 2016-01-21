package com.akingyin.baidumapdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;

import com.akingyin.baidumapdemo.baidumap.baseMapFragment;
import com.baidu.location.BDLocationListener;

/**
 * Created by Administrator on 2016/1/21.
 */
public class TestFragment  extends baseMapFragment {

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater) {

        return  inflater.inflate(R.layout.base_baidumap,null);
    }


}
