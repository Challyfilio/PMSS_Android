package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HealthyDataActivity extends AppCompatActivity {
    public static final int TRANS = 1;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_date_hd)
    TextView tv_date_hd;
    @BindView(R.id.tv_time_hd)
    TextView tv_time_hd;
    @BindView(R.id.tv_hddisplay1)
    TextView tv_hddisplay1;
    @BindView(R.id.tv_hddisplay2)
    TextView tv_hddisplay2;
    @BindView(R.id.et_hd_weight)
    EditText et_weight;
    @BindView(R.id.et_hd_glucose)
    EditText et_glucose;
    @BindView(R.id.et_hd_pressure_h)
    EditText et_pressure_h;
    @BindView(R.id.et_hd_pressure_l)
    EditText et_pressure_l;
    @BindView(R.id.bt_hdrecord)
    Button hdrecord;
    @BindView(R.id.bt_hdline)
    Button hdline;
    private ArrayList<String> WeightList = new ArrayList<String>();
    private ArrayList<String> GlucoseList = new ArrayList<String>();
    private ArrayList<Integer> PhList = new ArrayList<Integer>();
    private ArrayList<Integer> PlList = new ArrayList<Integer>();
    String tempaccount, weight, glucose, pressure_h, pressure_l, date, time;
    Connection conn;
    Statement stat;
    ResultSet rs1, rs2;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRANS:
                    tv_date_hd.setText(date);
                    tv_time_hd.setText(time);
                    tv_hddisplay1.setText("体重：" + weight + " kg 血糖：" + glucose + "mmol/L");
                    tv_hddisplay2.setText("收缩压：" + pressure_h + "mmHg 舒张压：" + pressure_l + "mmHg");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthy_data);
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
        tempaccount = intent.getStringExtra("o");

        SelectTen();
        SelectOne();

        hdrecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date_now = new Date();
                SimpleDateFormat d1 = new SimpleDateFormat("yyyy年MM月dd日");
                SimpleDateFormat d2 = new SimpleDateFormat("HH时mm分");
                SimpleDateFormat d3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = d1.format(date_now);
                String time = d2.format(date_now);
                String t = d3.format(date_now);
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        conn = DatabaseHelper.openConnection();
                        if (conn == null) {
                            Toast.makeText(HealthyDataActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            String sql = "insert into healthydata values('" + tempaccount + "'," +
                                    "" + et_weight.getText().toString() + "," +
                                    "" + et_glucose.getText().toString() + "," +
                                    "" + et_pressure_h.getText().toString() + "," +
                                    "" + et_pressure_l.getText().toString() + "," +
                                    "'" + date + "','" + time + "','" + t + "')";
                            try {
                                DatabaseHelper.exeStat(conn, sql);
                                Toast.makeText(HealthyDataActivity.this, "记录成功", Toast.LENGTH_SHORT).show();
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
                SelectOne();//插入后立即更新
            }
        });

        hdline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HealthyDataActivity.this, ChartDataActivity.class);
                intent.putStringArrayListExtra("key1", WeightList);
                intent.putStringArrayListExtra("key2", GlucoseList);
                intent.putIntegerArrayListExtra("key3", PhList);
                intent.putIntegerArrayListExtra("key4", PlList);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SelectOne();
    }

    //查询最近十次数据
    public void SelectTen() {
        new Thread() {
            public void run() {
                Looper.prepare();
                conn = DatabaseHelper.openConnection();
                if (conn == null) {
                    Toast.makeText(HealthyDataActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                } else {
                    String sql = "select * from healthydata where patientid = '" + tempaccount + "' order by t desc limit 10";
                    rs1 = DatabaseHelper.getResult(conn, sql);
                    try {
                        while (rs1.next()) {
                            String lweight = rs1.getString("weight");
                            WeightList.add(lweight);
                            Log.e("hdaw", lweight);
                            String lglucose = rs1.getString("glucose");
                            GlucoseList.add(lglucose);
                            Log.e("hdag", lglucose);
                            int lph = rs1.getInt("pressure_h");
                            PhList.add(lph);
                            Log.e("hdah", String.valueOf(lph));
                            int lpl = rs1.getInt("pressure_l");
                            PlList.add(lpl);
                            Log.e("hdal", String.valueOf(lpl));
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if (rs1 != null) {
                            try {
                                rs1.close();
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
                Looper.loop();
            }
        }.start();
    }

    //查询最近一次
    public void SelectOne() {
        new Thread() {
            public void run() {//最近一次数据
                Looper.prepare();
                conn = DatabaseHelper.openConnection();
                if (conn == null) {
                    Toast.makeText(HealthyDataActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                } else {
                    String sql = "select * from healthydata where patientid = '" + tempaccount + "' order by t desc limit 1";
                    rs2 = DatabaseHelper.getResult(conn, sql);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (rs2.next()) {
                                    weight = rs2.getString("weight");
                                    glucose = rs2.getString("glucose");
                                    pressure_h = rs2.getString("pressure_h");
                                    pressure_l = rs2.getString("pressure_l");
                                    date = rs2.getString("date");
                                    time = rs2.getString("time");
                                }
                                Message message = new Message();
                                message.what = TRANS;
                                handler.sendMessage(message);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            } finally {
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
                    });
                }
                Looper.loop();
            }
        }.start();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_data, menu); /* R.menu/toolbar_data.xml */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_data:
                SelectOne();
                break;
            default:
        }
        return true;
    }
}