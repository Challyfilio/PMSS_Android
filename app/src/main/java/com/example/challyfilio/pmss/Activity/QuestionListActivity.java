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
import android.widget.ListView;
import android.widget.Toast;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuestionListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.record_view_q)
    ListView questionView;
    @BindView(R.id.sr_refresh_q)
    SwipeRefreshLayout refresh;
    private List<String> QuestionList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    String tempname, tempaccount, tempidentity, sql1, sql2, sql3, doctor_wholog, doctor_department, questionid, patientid, question_c, t;
    Connection conn;
    Statement stat;
    ResultSet rs1, rs2, rs3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);
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
        tempname = intent.getStringExtra("n");
        tempaccount = intent.getStringExtra("o");
        tempidentity = intent.getStringExtra("p");

        if (tempidentity.equals("patient")) {
            sql1 = "select * from question where patientid = '" + tempaccount + "'";
        }
        if (tempidentity.equals("doctor")) {
            sql1 = "select * from question";
            sql2 = "select * from sysuser where didentity = 'doctor' and patientid ='" + tempaccount + "'";
            new Thread() {
                public void run() {
                    Looper.prepare();
                    conn = DatabaseHelper.openConnection();
                    if (conn == null) {
                        Toast.makeText(QuestionListActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                    } else {
                        rs1 = DatabaseHelper.getResult(conn, sql2);
                        try {
                            while (rs1.next()) {
                                doctor_wholog = rs1.getString("doctorid");//医生工号//h
                                doctor_department = rs1.getString("department");//医生科室//i
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

        //设置 listview,读取数据
        setListView();

        questionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        conn = DatabaseHelper.openConnection();
                        if (conn == null) {
                            Toast.makeText(QuestionListActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            rs2 = DatabaseHelper.getResult(conn, sql1);
                            try {
                                //执行查询
                                for (int i = 0; i <= position; i++) {
                                    rs2.next();
                                }
                                String tempqid = rs2.getString("questionid");
                                sql3 = "select * from question where questionid = '" + tempqid + "'";
                                rs3 = DatabaseHelper.getResult(conn, sql3);
                                while (rs3.next()) {
                                    questionid = rs3.getString("questionid");//a
                                    patientid = rs3.getString("patientid");//b
                                    question_c = rs3.getString("question_c");//c
                                    t = rs3.getString("t");//d
                                }
                                Intent intent = new Intent(QuestionListActivity.this, AnswerListActivity.class);
                                intent.putExtra("a", questionid);
                                intent.putExtra("b", patientid);
                                intent.putExtra("c", question_c);
                                intent.putExtra("d", t);
                                intent.putExtra("e", tempname);//人类之光
                                intent.putExtra("f", tempaccount);//人类之光
                                intent.putExtra("g", tempidentity);//人类之光
                                intent.putExtra("h", doctor_wholog);//当前登录的医生工号
                                intent.putExtra("i", doctor_department);//当前医生科室
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
    }

    @Override
    public void onResume() {
        super.onResume();
        QuestionList.clear();
        setListView();
    }

    public void onRefresh() {
        QuestionList.clear();
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
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, QuestionList);
        new Thread() {
            public void run() {
                Looper.prepare();
                conn = DatabaseHelper.openConnection();
                if (conn == null) {
                    Toast.makeText(QuestionListActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                } else {
                    rs2 = DatabaseHelper.getResult(conn, sql1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (rs2.next()) {
                                    String question_c = rs2.getString("question_c");
                                    String t = rs2.getString("t");
                                    QuestionList.add(question_c + "\n" + t);
                                }
                                questionView.setAdapter(adapter);
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
        getMenuInflater().inflate(R.menu.toolbar_qa, menu); /* R.menu/toolbar_qa.xml */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_qa:
                if (tempidentity.equals("doctor")) {
                    Toast.makeText(QuestionListActivity.this, "您无权提问", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(QuestionListActivity.this, WriteQuestionActivity.class);
                    i.putExtra("f", tempaccount);//人类之光
                    startActivity(i);
                }
                break;
            default:
        }
        return true;
    }
}