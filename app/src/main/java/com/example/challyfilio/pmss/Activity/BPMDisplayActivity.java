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
import android.widget.TextView;
import android.widget.Toast;

import java.sql.*;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BPMDisplayActivity extends AppCompatActivity {
    public static final int TRANS = 1;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_date)
    TextView tv_date;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.tv_bpm)
    TextView tv_hr;
    @BindView(R.id.bt_hrgo)
    Button bt_hr;
    @BindView(R.id.bt_hrline)
    Button bt_bpm;
    private ArrayList<Integer> BPM = new ArrayList<Integer>();
    String date, time, hr, tempaccount;
    Connection conn;
    Statement stat;
    ResultSet rs, rs1;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRANS:
                    tv_date.setText(date);
                    tv_time.setText(time);
                    tv_hr.setText("❤ " + hr + " BPM");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bpmdisplay);
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

        SelectTen();//只能调用一次
        SelectOne();

        bt_hr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BPMDisplayActivity.this, HeartRateActivity.class);
                intent.putExtra("o", tempaccount);//人类之光
                startActivity(intent);
            }
        });

        bt_bpm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BPMDisplayActivity.this, ChartHeartActivity.class);
                intent.putIntegerArrayListExtra("key", BPM);
                startActivity(intent);
            }
        });
    }//onCreate

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
                    Toast.makeText(BPMDisplayActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                } else {
                    rs = DatabaseHelper.getResult(conn, "select * from heartrate where patientid = '" + tempaccount + "' order by t desc limit 10");
                    try {
                        while (rs.next()) {
                            int heartrate = rs.getInt("bpm");
                            Log.d("lineeee", String.valueOf(heartrate));
                            BPM.add(heartrate);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if (rs != null) {
                            try {
                                rs.close();
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

    //查询心率数据
    public void SelectOne() {
        new Thread() {
            public void run() {
                Looper.prepare();
                conn = DatabaseHelper.openConnection();
                if (conn == null) {
                    Toast.makeText(BPMDisplayActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                } else {
                    rs1 = DatabaseHelper.getResult(conn, "select * from heartrate where patientid = '" + tempaccount + "' order by t desc limit 1");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (rs1.next()) {
                                    hr = rs1.getString("bpm");
                                    Log.d("bpmee", String.valueOf(hr));
                                    date = rs1.getString("date");
                                    time = rs1.getString("time");
                                }
                                Message message = new Message();
                                message.what = TRANS;
                                handler.sendMessage(message);
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