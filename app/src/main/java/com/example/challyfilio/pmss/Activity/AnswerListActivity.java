package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnswerListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_question_show)
    TextView question_show;
    @BindView(R.id.bt_delquestion)
    Button questiondel;
    @BindView(R.id.record_view_a)
    ListView answerView;
    @BindView(R.id.sr_refresh_a)
    SwipeRefreshLayout refresh;
    private List<String> AnswerList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    String questionid, patientid, question_c, t, tempname, tempaccount, tempidentity, doctor_wholog, doctor_department;
    String doctorname, department, doctorid, answer_c, ta;
    Connection conn;
    Statement stat;
    ResultSet rs1, rs2, rs3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_list);
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

        refresh.setOnRefreshListener(this);

        final Intent intent = getIntent();
        questionid = intent.getStringExtra("a");
        patientid = intent.getStringExtra("b");
        question_c = intent.getStringExtra("c");
        t = intent.getStringExtra("d");
        tempname = intent.getStringExtra("e");//人类之光
        tempaccount = intent.getStringExtra("f");//人类之光
        tempidentity = intent.getStringExtra("g");//人类之光
        doctor_wholog = intent.getStringExtra("h");//当前登录的医生工号
        doctor_department = intent.getStringExtra("i");//当前医生科室
        question_show.setText("问题描述如下：\n" + question_c + "\n\n来自患者" + patientid + "\n时间 " + t);

        setListView();

        answerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        conn = DatabaseHelper.openConnection();
                        if (conn == null) {
                            Toast.makeText(AnswerListActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            String sql3 = "select * from answer where questionid = '" + questionid + "'";
                            rs2 = DatabaseHelper.getResult(conn, sql3);
                            try {
                                //执行查询
                                for (int i = 0; i <= position; i++) {
                                    rs2.next();
                                }
                                String tempaid = rs2.getString("answerid");
                                String sql2 = "select * from answer where answerid = '" + tempaid + "'";
                                rs3 = DatabaseHelper.getResult(conn, sql2);
                                while (rs3.next()) {
                                    doctorname = rs3.getString("doctorname");
                                    department = rs3.getString("department");
                                    doctorid = rs3.getString("doctorid");
                                    answer_c = rs3.getString("answer_c");
                                    ta = rs3.getString("t");
                                }
                                Intent intent = new Intent(AnswerListActivity.this, AnswerShowActivity.class);
                                intent.putExtra("a", doctorname);
                                intent.putExtra("b", department);
                                intent.putExtra("c", doctorid);
                                intent.putExtra("d", answer_c);
                                intent.putExtra("e", ta);
                                intent.putExtra("f", question_c);
                                intent.putExtra("g", patientid);
                                intent.putExtra("h", t);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
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
                                if (rs3 != null) {
                                    try {
                                        rs3.close();
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
        });

        questiondel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tempidentity.equals("doctor")) {
                    Toast.makeText(AnswerListActivity.this, "您没有权限", Toast.LENGTH_SHORT).show();
                } else {
                    new Thread() {
                        public void run() {
                            Looper.prepare();
                            conn = DatabaseHelper.openConnection();
                            if (conn == null) {
                                Toast.makeText(AnswerListActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                            } else {
                                String sql1 = "delete from question where questionid='" + questionid + "'";
                                String sql2 = "delete from answer where questionid='" + questionid + "'";
                                try {
                                    DatabaseHelper.exeStat(conn, sql1);
                                    DatabaseHelper.exeStat(conn, sql2);
                                    Toast.makeText(AnswerListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
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
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        AnswerList.clear();
        setListView();
    }

    public void onRefresh() {
        AnswerList.clear();
        setListView();
        mHandler.postDelayed(mRefresh, 200);
    }

    private Handler mHandler = new Handler();
    private Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            refresh.setRefreshing(false);
        }
    };

    public void setListView() {
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, AnswerList);
        new Thread() {
            public void run() {
                Looper.prepare();
                conn = DatabaseHelper.openConnection();
                if (conn == null) {
                    Toast.makeText(AnswerListActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                } else {
                    String sql1 = "select * from answer where questionid = '" + questionid + "'";
                    rs1 = DatabaseHelper.getResult(conn, sql1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (rs1.next()) {
                                    String doctorname = rs1.getString("doctorname");
                                    String department = rs1.getString("department");
                                    String ta = rs1.getString("t");
                                    AnswerList.add("医生" + doctorname + "\n" + department + "\n" + ta);
                                }
                                answerView.setAdapter(adapter);
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
        getMenuInflater().inflate(R.menu.toolbar_qa, menu); /* R.menu/toolbar_qa.xml */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_qa:
                if (tempidentity.equals("patient")) {
                    Toast.makeText(AnswerListActivity.this, "您无权回答", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(AnswerListActivity.this, WriteAnswerActivity.class);
                    i.putExtra("a", questionid);
                    i.putExtra("b", question_c);
                    i.putExtra("h", doctor_wholog);
                    i.putExtra("i", doctor_department);
                    i.putExtra("e", tempname);//人类之光
                    i.putExtra("f", tempaccount);//人类之光
                    i.putExtra("g", tempidentity);//人类之光
                    startActivity(i);
                }
                break;
            default:
        }
        return true;
    }
}