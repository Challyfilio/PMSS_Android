package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.Util.MD5Util;
import com.example.challyfilio.pmss.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.sql.*;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.new_edit_account)
    EditText newaccountEdit;
    @BindView(R.id.new_edit_name)
    EditText newnameEdit;
    @BindView(R.id.new_edit_password)
    EditText newpasswordEdit;
    @BindView(R.id.new_edit_password_again)
    EditText newpasswordagainEdit;
    @BindView(R.id.rbt_doctor)
    RadioButton doctor;
    @BindView(R.id.rbt_patient)
    RadioButton patient;
    @BindView(R.id.identity)
    RadioGroup identity;
    @BindView(R.id.register_go)
    Button register_go;
    String register_identity = "doctor";
    String sql1, sql2, sql3, al_patientid, ma_patientid, ma_name, ma_didentity;
    Connection conn;
    Statement stat;
    ResultSet rs1, rs2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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

        identity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (doctor.getId() == i) {
                    register_identity = "doctor";
                }
                if (patient.getId() == i) {
                    register_identity = "patient";
                }
            }
        });

        register_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sql1 = "select * from user where identity = '" + register_identity + "'";
                sql2 = "select * from sysuser";
                sql3 = "insert into user(patientid,name,passwd,identity) values('" + newaccountEdit.getText().toString() + "'," +
                        "'" + newnameEdit.getText().toString() + "','" + MD5Util.encrypt(newpasswordEdit.getText().toString()) + "','" + register_identity + "');";
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        boolean flag_already = false;//用户是否已注册
                        boolean flag_match = false;//信息与预存表是否匹配
                        String newpassword = newpasswordEdit.getText().toString();
                        String newpasswordagain = newpasswordagainEdit.getText().toString();
                        conn = DatabaseHelper.openConnection();
                        if (conn == null) {
                            Toast.makeText(RegisterActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            rs1 = DatabaseHelper.getResult(conn, sql1);
                            rs2 = DatabaseHelper.getResult(conn, sql2);
                            if ((newaccountEdit.getText().toString().isEmpty()) || (newnameEdit.getText().toString().isEmpty())
                                    || (newpasswordEdit.getText().toString().isEmpty()) || (newpasswordagainEdit.getText().toString().isEmpty())) {
                                Toast.makeText(RegisterActivity.this, "不能为空,请重新输入", Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    while (rs1.next()) {//检查是否已注册
                                        al_patientid = rs1.getString("patientid");
                                        if ((newaccountEdit.getText().toString().equals(al_patientid))) {
                                            flag_already = true;
                                            Toast.makeText(RegisterActivity.this, "已注册,请登录", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    if (!flag_already) {//如果用户不存在
                                        while (rs2.next()) {
                                            ma_patientid = rs2.getString("patientid");
                                            ma_name = rs2.getString("name");
                                            ma_didentity = rs2.getString("didentity");
                                            if (newaccountEdit.getText().toString().equals(ma_patientid) && newnameEdit.getText().toString().equals(ma_name)) {
                                                if (register_identity.equals("doctor")) {
                                                    if (ma_didentity.equals("doctor")) {//医生匹配
                                                        flag_match = true;
                                                        if (!newpassword.equals(newpasswordagain)) {
                                                            Toast.makeText(RegisterActivity.this, "密码不一致,请重新输入", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            DatabaseHelper.exeStat(conn, sql3);
                                                            Toast.makeText(RegisterActivity.this, "医生：注册成功", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    } else {
                                                        flag_match = true;
                                                        Toast.makeText(RegisterActivity.this, "不是本院医生", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {//患者匹配
                                                    flag_match = true;
                                                    if (!newpassword.equals(newpasswordagain)) {
                                                        Toast.makeText(RegisterActivity.this, "密码不一致,请重新输入", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        DatabaseHelper.exeStat(conn, sql3);
                                                        Toast.makeText(RegisterActivity.this, "患者：注册成功", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            }
                                        }
                                        if (!flag_match) {//如果不匹配
                                            Toast.makeText(RegisterActivity.this, "账号姓名不匹配", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } catch (SQLException e) {
                                    e.getMessage();
                                } finally {
                                    if (rs1 != null) {
                                        try {
                                            rs1.close();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (rs2 != null) {
                                        try {
                                            rs2.close();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
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