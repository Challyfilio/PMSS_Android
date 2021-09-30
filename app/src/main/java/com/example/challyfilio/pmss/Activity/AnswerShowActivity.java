package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnswerShowActivity extends AppCompatActivity {
    String doctorname, department, doctorid, answer_c, ta, question_c, patientid, t;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_answer_show0)
    TextView as0;
    @BindView(R.id.tv_answer_show1)
    TextView as1;
    @BindView(R.id.tv_answer_show2)
    TextView as2;
    @BindView(R.id.tv_answer_show3)
    TextView as3;
    @BindView(R.id.tv_answer_show4)
    TextView as4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_show);
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

        final Intent intent = getIntent();
        doctorname = intent.getStringExtra("a");
        department = intent.getStringExtra("b");
        doctorid = intent.getStringExtra("c");
        answer_c = intent.getStringExtra("d");
        ta = intent.getStringExtra("e");
        question_c = intent.getStringExtra("f");
        patientid = intent.getStringExtra("g");
        t = intent.getStringExtra("h");

        as0.setText("问题描述如下：\n" + question_c + "\n\n来自患者" + patientid + "\n时间 " + t + "\n");
        as1.setText(answer_c);
        as2.setText(department + "医生" + doctorname);
        as3.setText("工号" + doctorid);
        as4.setText(ta);
    }
}
