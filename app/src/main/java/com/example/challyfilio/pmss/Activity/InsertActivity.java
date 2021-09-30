package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InsertActivity extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener {
    public String Rid_ins,//a
            patientid_ins,//b
            patientname_ins,//c
            patientage_ins,//d
            patientsex_ins = "男",//e
            patienttel_ins,//f
            thisdate_ins,//g
            nextdate_ins = "",//h
            detail_ins,//l
            opinion_ins,//m
            tempname,//n
            tempaccount,//o
            tempidentity,//p
            doctor_wholog,//q
            doctor_department;//r
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.ins_patientid)
    EditText patientidIns;
    @BindView(R.id.ins_patientname)
    EditText patientnameIns;
    @BindView(R.id.ins_patientage)
    EditText patientageIns;
    @BindView(R.id.rbt_male)
    RadioButton male;
    @BindView(R.id.rbt_female)
    RadioButton female;
    @BindView(R.id.sex)
    RadioGroup sex;
    @BindView(R.id.ins_patienttel)
    EditText patienttelIns;
    @BindView(R.id.ins_thisdate)
    TextView thisdateIns;
    @BindView(R.id.ins_nextdate)
    TextView nextdateIns;
    @BindView(R.id.bt_selectnext)
    Button nextdate;
    @BindView(R.id.ins_department)
    TextView departmentIns;
    @BindView(R.id.ins_doctorid)
    TextView doctoridIns;
    @BindView(R.id.ins_doctorname)
    TextView doctornameIns;
    @BindView(R.id.ins_detail)
    EditText detailIns;
    @BindView(R.id.ins_opinion)
    EditText opinionIns;
    @BindView(R.id.bt_insert)
    Button insert;
    Connection conn;
    Statement stat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);
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

        sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (male.getId() == i) {
                    patientsex_ins = "男";//e
                }
                if (female.getId() == i) {
                    patientsex_ins = "女";//e
                }
            }
        });

        final Intent intent = getIntent();
        tempname = intent.getStringExtra("n");//医生姓名//人类之光//k
        tempaccount = intent.getStringExtra("o");//人类之光
        tempidentity = intent.getStringExtra("p");//人类之光
        doctor_wholog = intent.getStringExtra("q");//医生工号//j
        doctor_department = intent.getStringExtra("r");//医生科室//i
        Date date = new Date();
        SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat d2 = new SimpleDateFormat("yyyyMMddHHmm");
        thisdate_ins = d1.format(date);//g
        Rid_ins = d2.format(date) + doctor_wholog;//a/ /生成病历号：时间yyyyMMddHHmm+医生工号

        thisdateIns.setText("看诊日期：      " + thisdate_ins);
        departmentIns.setText("看诊科室：      " + doctor_department);
        doctoridIns.setText("医生工号：      " + doctor_wholog);
        doctornameIns.setText("医生姓名：      " + tempname);

        nextdate.setOnClickListener(this);

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                patientid_ins = patientidIns.getText().toString();//b
                patientname_ins = patientnameIns.getText().toString();//c
                patientage_ins = patientageIns.getText().toString();//d
                patienttel_ins = patienttelIns.getText().toString();//f
                detail_ins = detailIns.getText().toString();//l
                opinion_ins = opinionIns.getText().toString();//m
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        conn = DatabaseHelper.openConnection();
                        if (conn == null) {
                            Toast.makeText(InsertActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            String sql = "insert into recordinfo(Rid,patientid,patientname,patientage,patientsex," +
                                    "patienttel,thisdate,nextdate,department,doctorid,doctorname,detail,opinion) values(" +
                                    "'" + Rid_ins + "','" + patientid_ins + "','" + patientname_ins + "'," + patientage_ins + ",'" + patientsex_ins + "'," +
                                    "'" + patienttel_ins + "','" + thisdate_ins + "','" + nextdate_ins + "','" + doctor_department + "','" + doctor_wholog + "'," +
                                    "'" + tempname + "','" + detail_ins + "','" + opinion_ins + "')";
                            Log.e("insee", sql);
                            try {
                                DatabaseHelper.exeStat(conn, sql);
                                Toast.makeText(InsertActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
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
                        Looper.loop();
                    }
                }.start();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_selectnext:
                // 获取日历的一个实例，里面包含了当前的年月日
                Calendar calendar = Calendar.getInstance();
                // 构建一个日期对话框，该对话框已经集成了日期选择器。
                // DatePickerDialog的第二个构造参数指定了日期监听器
                DatePickerDialog dialog = new DatePickerDialog(this, (DatePickerDialog.OnDateSetListener) this,
                        calendar.get(Calendar.YEAR), // 年份
                        calendar.get(Calendar.MONTH), // 月份
                        calendar.get(Calendar.DAY_OF_MONTH)); // 日子
                // 把日期对话框显示在界面上
                dialog.show();
                break;
            default:
        }
    }

    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // 获取日期对话框设定的年月份
        nextdate_ins = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);//h
        nextdateIns.setText("复诊日期：      " + nextdate_ins);
    }
}