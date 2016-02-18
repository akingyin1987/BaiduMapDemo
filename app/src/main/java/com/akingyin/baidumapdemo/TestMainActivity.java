package com.akingyin.baidumapdemo;

import android.view.LayoutInflater;
import android.view.View;

import com.akingyin.baidumap.BaseMapActivity;

/**
 * Created by zlcd on 2016/2/18.
 */
public class TestMainActivity extends BaseMapActivity {

    @Override
    public void initialization() {
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("2222222222222222");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.activity_testmain,null);
    }
}
