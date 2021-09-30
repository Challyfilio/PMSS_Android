package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.R;
import com.example.challyfilio.pmss.Widget.CurveView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChartHeartActivity extends AppCompatActivity {
    private static final String[] HORIZONTAL_AXIS = {"1", "2", "3", "4",
            "5", "6", "7", "8", "9", "10", "11", "12"};
    private static double[] DATA = {12, 24, 45, 56};
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.curve_view_hr)
    CurveView hr_CurveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartchart);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        ArrayList<Integer> BPM = (ArrayList<Integer>) intent.getIntegerArrayListExtra("key");
        DATA = BPM.stream().mapToDouble(Double::valueOf).toArray();

        hr_CurveView.setDataList(DATA, 150, Color.RED);
        hr_CurveView.setHorizontalAxis(HORIZONTAL_AXIS);

        /*Toolbar*/
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//添加返回键
        //设置返回键监听器
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}