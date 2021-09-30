package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WriteQuestionActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_question)
    EditText question_insert;
    @BindView(R.id.bt_ins_question)
    Button ins_question;
    String tempaccount, t, questionid;
    Connection conn;
    Statement stat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_question);
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
        tempaccount = intent.getStringExtra("f");

        Date date = new Date();
        SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat d2 = new SimpleDateFormat("yyyyMMddHHmm");
        t = d1.format(date);
        questionid = d2.format(date) + tempaccount;//生成问题编号：时间yyyyMMddHHmm+患者id

        ins_question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        conn = DatabaseHelper.openConnection();
                        if (conn == null) {
                            Toast.makeText(WriteQuestionActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            if (question_insert.getText().toString().isEmpty()) {
                                Toast.makeText(WriteQuestionActivity.this, "不能为空", Toast.LENGTH_SHORT).show();
                            } else {
                                String sql = "insert into question values(" +
                                        "'" + questionid + "','" + tempaccount + "','" + question_insert.getText().toString() + "','" + t + "')";
                                try {
                                    DatabaseHelper.exeStat(conn, sql);
                                    Toast.makeText(WriteQuestionActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                                    finish();
                                } finally {
                                    if (stat != null) {
                                        try {
                                            stat.close();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (conn != null) {
                                        try {
                                            conn.close();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                        Looper.loop();
                    }
                }.start();
            }
        });
    }
}