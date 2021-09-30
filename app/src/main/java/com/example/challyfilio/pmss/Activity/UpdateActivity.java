package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.*;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdateActivity extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.up_Rid)
    TextView RidUp;
    @BindView(R.id.up_patientid)
    EditText patientidUp;
    @BindView(R.id.up_patientname)
    EditText patientnameUp;
    @BindView(R.id.up_patientage)
    EditText patientageUp;
    @BindView(R.id.up_patientsex)
    TextView patientsexUp;
    @BindView(R.id.rbt_upmale)
    RadioButton Upmale;
    @BindView(R.id.rbt_upfemale)
    RadioButton Upfemale;
    @BindView(R.id.up_sex)
    RadioGroup Upsex;
    @BindView(R.id.up_patienttel)
    EditText patienttelUp;
    @BindView(R.id.up_thisdate)
    TextView thisdateUp;
    @BindView(R.id.up_nextdate)
    TextView nextdateUp;
    @BindView(R.id.bt_upnext)
    Button Upnext;
    @BindView(R.id.up_department)
    TextView departmentUp;
    @BindView(R.id.up_doctorid)
    TextView doctoridUp;
    @BindView(R.id.up_doctorname)
    TextView doctornameUp;
    @BindView(R.id.up_detail)
    EditText detailUp;
    @BindView(R.id.up_opinion)
    EditText opinionUp;
    @BindView(R.id.bt_update)
    Button update;
    public String Rid_show,//a
            patientid_show,//b
            patientname_show,//c
            patientage_show,//d
            patientsex_show,//e
            patienttel_show,//f
            thisdate_show,//g
            nextdate_show,//h
            department_show,//i
            doctorid_show,//j
            doctorname_show,//k
            detail_show,//l
            opinion_show,//m
            tempname,//n
            tempaccount,//o
            tempidentity;//p
    Connection conn;
    Statement stat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
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

        Upnext.setOnClickListener(this);

        final Intent intent = getIntent();
        Rid_show = intent.getStringExtra("a");
        patientid_show = intent.getStringExtra("b");
        patientname_show = intent.getStringExtra("c");
        patientage_show = intent.getStringExtra("d");
        patientsex_show = intent.getStringExtra("e");
        patienttel_show = intent.getStringExtra("f");
        thisdate_show = intent.getStringExtra("g");
        nextdate_show = intent.getStringExtra("h");
        department_show = intent.getStringExtra("i");
        doctorid_show = intent.getStringExtra("j");
        doctorname_show = intent.getStringExtra("k");
        detail_show = intent.getStringExtra("l");
        opinion_show = intent.getStringExtra("m");
        tempname = intent.getStringExtra("n");//人类之光
        tempaccount = intent.getStringExtra("o");//人类之光
        tempidentity = intent.getStringExtra("p");//人类之光

        RidUp.setText("病历编号：      " + Rid_show);
        patientidUp.setText(patientid_show);
        patientnameUp.setText(patientname_show);
        patientageUp.setText(patientage_show);
        patientsexUp.setText("患者性别：      " + patientsex_show);
        patienttelUp.setText(patienttel_show);
        thisdateUp.setText("看诊日期：      " + thisdate_show);
        nextdateUp.setText("复诊日期：      " + nextdate_show);
        departmentUp.setText("看诊科室：      " + department_show);
        doctoridUp.setText("医生工号：      " + doctorid_show);
        doctornameUp.setText("医生姓名：      " + doctorname_show);
        detailUp.setText(detail_show);
        opinionUp.setText(opinion_show);

        Upsex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (Upmale.getId() == i) {
                    patientsex_show = "男";
                    patientsexUp.setText("患者性别：      " + patientsex_show);
                }
                if (Upfemale.getId() == i) {
                    patientsex_show = "女";
                    patientsexUp.setText("患者性别：      " + patientsex_show);
                }
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        conn = DatabaseHelper.openConnection();
                        if (conn == null) {
                            Toast.makeText(UpdateActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            String sql = "update recordinfo set " +
                                    "patientid = '" + patientidUp.getText().toString() + "'," +
                                    "patientname = '" + patientnameUp.getText().toString() + "'," +
                                    "patientage = " + patientageUp.getText().toString() + "," +
                                    "patientsex = '" + patientsex_show + "'," +
                                    "patienttel = '" + patienttelUp.getText().toString() + "'," +
                                    "nextdate = '" + nextdate_show + "'," +
                                    "detail = '" + detailUp.getText().toString() + "'," +
                                    "opinion = '" + opinionUp.getText().toString() + "'" +
                                    "where Rid = '" + Rid_show + "'";
                            try {
                                DatabaseHelper.exeStat(conn, sql);
                                Toast.makeText(UpdateActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
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
            case R.id.bt_upnext:
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
        nextdate_show = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);//h
        nextdateUp.setText("复诊日期：      " + nextdate_show);
    }
}