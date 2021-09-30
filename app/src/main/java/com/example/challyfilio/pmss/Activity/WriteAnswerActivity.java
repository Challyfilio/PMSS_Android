package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WriteAnswerActivity extends AppCompatActivity {
    String questionid, doctor_wholog, doctor_department, tempname, tempaccount, tempidentity, t, answerid, question_c;
    Connection conn;
    Statement stat;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_question_show)
    TextView questionshow;
    @BindView(R.id.et_answer)
    EditText answer_insert;
    @BindView(R.id.bt_ins_answer)
    Button ins_answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_answer);
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
        questionid = intent.getStringExtra("a");
        question_c = intent.getStringExtra("b");
        doctor_wholog = intent.getStringExtra("h");
        doctor_department = intent.getStringExtra("i");
        tempname = intent.getStringExtra("e");//人类之光
        tempaccount = intent.getStringExtra("f");//人类之光
        tempidentity = intent.getStringExtra("g");//人类之光
        questionshow.setText(question_c);

        Date date = new Date();
        SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat d2 = new SimpleDateFormat("yyyyMMddHHmm");
        t = d1.format(date);
        answerid = doctor_wholog + d2.format(date);//生成回答编号：医生工号+时间yyyyMMddHHmm

        ins_answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        conn = DatabaseHelper.openConnection();
                        if (conn == null) {
                            Toast.makeText(WriteAnswerActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            if (answer_insert.getText().toString().isEmpty()) {
                                Toast.makeText(WriteAnswerActivity.this, "不能为空", Toast.LENGTH_SHORT).show();
                            } else {
                                String sql = "insert into answer values(" +
                                        "'" + questionid + "','" + answerid + "','" + tempname + "','" + doctor_department + "','" + doctor_wholog + "'," +
                                        "'" + answer_insert.getText().toString() + "','" + t + "')";
                                Log.e("waa", sql);
                                try {
                                    DatabaseHelper.exeStat(conn, sql);
                                    Toast.makeText(WriteAnswerActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
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