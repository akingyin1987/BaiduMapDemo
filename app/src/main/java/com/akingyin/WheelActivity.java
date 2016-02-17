package com.akingyin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.akingyin.baidumapdemo.R;
import com.akingyin.widget.WheelView;
import com.google.common.base.Strings;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlcd on 2016/1/25.
 */
public class WheelActivity extends AppCompatActivity {
     WheelView   wheelView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_wheel);
        List<String>   items = new ArrayList<>();
        for(int i=0;i<10;i++){
            items.add( RandomStringUtils.random(5,"我是中国人，我爱中国"));
        }
        wheelView= (WheelView)findViewById(R.id.map_wheelview);
        wheelView.setItems(items);
        wheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                System.out.println(selectedIndex + "=" + item);
            }
        });
        findViewById(R.id.sort_plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wheelView.setSeletion(wheelView.getSeletedIndex() + 1);
            }
        });
        findViewById(R.id.sort_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wheelView.setSeletion(wheelView.getSeletedIndex()-1);
            }
        });
    }
}
