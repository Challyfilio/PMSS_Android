package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.R;
import com.example.challyfilio.pmss.Widget.CurveView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChartDataActivity extends AppCompatActivity {

    private static final String[] HORIZONTAL_AXIS = {"1", "2", "3", "4",
            "5", "6", "7", "8", "9", "10", "11", "12"};
    private static double[] DATA1 = {12.1, 24, 45.2, 56.4, 89, 70.8, 49, 22.5, 23, 10.4};
    private static double[] DATA2 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private static double[] DATA3 = {12.1, 24, 45.2, 56.4, 89, 70.8, 49, 22.5, 23, 10.4};
    private static double[] DATA4 = {22.1, 54, 45.5, 76.4, 140, 75.8, 99, 67.5, 63, 105.4};
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.curve_view1)
    CurveView curveView1;
    @BindView(R.id.curve_view2)
    CurveView curveView2;
    @BindView(R.id.curve_view3)
    CurveView curveView3;
    @BindView(R.id.curve_view4)
    CurveView curveView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datachart);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        ArrayList<String> WeightList = (ArrayList<String>) intent.getStringArrayListExtra("key1");
        ArrayList<String> GlucoseList = (ArrayList<String>) intent.getStringArrayListExtra("key2");
        ArrayList<Integer> PhList = (ArrayList<Integer>) intent.getIntegerArrayListExtra("key3");
        ArrayList<Integer> PlList = (ArrayList<Integer>) intent.getIntegerArrayListExtra("key4");

        DATA1 = WeightList.stream().mapToDouble(Double::valueOf).toArray();
        Log.e("dca1", String.valueOf(DATA1));
        DATA2 = GlucoseList.stream().mapToDouble(Double::valueOf).toArray();
        Log.e("dca2", String.valueOf(DATA2));
        DATA3 = PhList.stream().mapToDouble(Double::valueOf).toArray();
        Log.e("dca3", String.valueOf(DATA3));
        DATA4 = PlList.stream().mapToDouble(Double::valueOf).toArray();
        Log.e("dca4", String.valueOf(DATA4));

        curveView1.setDataList(DATA1, 100, Color.YELLOW);
        curveView2.setDataList(DATA2, 12, Color.GREEN);
        curveView3.setDataList(DATA3, 150, Color.RED);
        curveView4.setDataList(DATA4, 150, Color.BLUE);
        curveView1.setHorizontalAxis(HORIZONTAL_AXIS);
        curveView2.setHorizontalAxis(HORIZONTAL_AXIS);
        curveView3.setHorizontalAxis(HORIZONTAL_AXIS);
        curveView4.setHorizontalAxis(HORIZONTAL_AXIS);

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