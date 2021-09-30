package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.Alarm.AlarmService;
import com.example.challyfilio.pmss.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TomatoActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_mins)
    EditText r_mins;
    @BindView(R.id.bt_remindon)
    Button on;
    @BindView(R.id.bt_remindoff)
    Button off;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomato);
        ButterKnife.bind(this);

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

        on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (r_mins.getText().toString().isEmpty()) {
                    Toast.makeText(TomatoActivity.this, "填写时间", Toast.LENGTH_SHORT).show();
                } else {
                    long delay = Long.valueOf(r_mins.getText().toString()).longValue();
                    AlarmService.addNotification(TomatoActivity.this, delay, null, null, null, 2);
                    Toast.makeText(TomatoActivity.this, "Remind On", Toast.LENGTH_SHORT).show();
                }
            }
        });

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent stopIntent = new Intent(TomatoActivity.this, AlarmService.class);
                Toast.makeText(TomatoActivity.this, "Remind Off", Toast.LENGTH_SHORT).show();
                stopService(stopIntent);
            }
        });
    }
}