package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final int TRANS = 1;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.record_view)
    ListView recordView;
    @BindView(R.id.sr_refresh)
    SwipeRefreshLayout refresh;
    @BindView(R.id.nav)
    NavigationView navigationView;
    @BindView(R.id.activity_drawlayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.et_search)
    EditText serach;
    @BindView(R.id.bt_search)
    Button serach_go;
    @BindView(R.id.ll_search)
    LinearLayout ll_search;
    private TextView acclogin, accnum;
    private List<String> RecordList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    Connection conn;
    PreparedStatement preparedStatement;
    Statement stat;
    ResultSet rs1, rs2, rs3, rs4;
    String sql1 = "select * from recordinfo", sql2, sql3, doctor_wholog = "", doctor_department = "", tempRid;
    String sql4 = "select * from recordinfo where patientid LIKE ? " +
            "OR patientname LIKE ? OR thisdate LIKE ? OR department LIKE ?";

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
    int flag = 0;//区别搜索与不搜索的list，默认不搜索
    boolean search_hidden = true;//隐藏搜索框

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRANS:
                    recordView.setAdapter(adapter);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

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
        tempname = intent.getStringExtra("tempname");
        tempaccount = intent.getStringExtra("tempaccount");
        tempidentity = intent.getStringExtra("tempidentity");
        Log.e("Listee", tempidentity);

        if (tempidentity.equals("patient")) {
            sql1 = "select * from recordinfo where patientid = '" + tempaccount + "'";
            sql4 = "select * from (select * from recordinfo where patientid = '"+tempaccount+"') as temp" +
                    " where patientid LIKE ? OR patientname LIKE ? OR thisdate LIKE ? OR department LIKE ?";
        }
        if (tempidentity.equals("doctor")) {
            sql1 = "select * from recordinfo";
            sql4 = "select * from recordinfo where patientid LIKE ? " +
                    "OR patientname LIKE ? OR thisdate LIKE ? OR department LIKE ?";
            sql3 = "select * from sysuser where didentity = 'doctor' and patientid ='" + tempaccount + "'";
            new Thread() {
                public void run() {
                    Looper.prepare();
                    conn = DatabaseHelper.openConnection();
                    if (conn == null) {
                        Toast.makeText(ListActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                    } else {
                        rs3 = DatabaseHelper.getResult(conn, sql3);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (rs3.next()) {
                                        doctor_wholog = rs3.getString("doctorid");//医生工号
                                        doctor_department = rs3.getString("department");//医生科室
                                        Log.e("Lee", doctor_department);
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } finally {
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
                        });
                    }
                    Looper.loop();
                }
            }.start();
        }

        //设置 listview,读取数据
        setListView();

        recordView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        conn = DatabaseHelper.openConnection();
                        if (conn == null) {
                            Toast.makeText(ListActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                if (flag == 0) {
                                    rs1 = DatabaseHelper.getResult(conn, sql1);
                                    //执行查询
                                    for (int i = 0; i <= position; i++) {
                                        rs1.next();
                                    }
                                    tempRid = rs1.getString("Rid");
                                } else {
                                    preparedStatement = conn.prepareStatement(sql4);
                                    preparedStatement.setString(1, "%" + serach.getText().toString() + "%");
                                    preparedStatement.setString(2, "%" + serach.getText().toString() + "%");
                                    preparedStatement.setString(3, "%" + serach.getText().toString() + "%");
                                    preparedStatement.setString(4, "%" + serach.getText().toString() + "%");
                                    rs4 = preparedStatement.executeQuery();
                                    for (int i = 0; i <= position; i++) {
                                        rs4.next();
                                    }
                                    tempRid = rs4.getString("Rid");
                                }
                                /**/
                                sql2 = "select * from recordinfo where Rid = '" + tempRid + "'";
                                rs2 = DatabaseHelper.getResult(conn, sql2);
                                while (rs2.next()) {
                                    Rid_show = rs2.getString("Rid");//a
                                    patientid_show = rs2.getString("patientid");//b
                                    patientname_show = rs2.getString("patientname");//c
                                    patientage_show = rs2.getString("patientage");//d
                                    patientsex_show = rs2.getString("patientsex");//e
                                    patienttel_show = rs2.getString("patienttel");//f
                                    thisdate_show = rs2.getString("thisdate");//g
                                    nextdate_show = rs2.getString("nextdate");//h
                                    department_show = rs2.getString("department");//i
                                    doctorid_show = rs2.getString("doctorid");//j
                                    doctorname_show = rs2.getString("doctorname");//k
                                    detail_show = rs2.getString("detail");//l
                                    opinion_show = rs2.getString("opinion");//m
                                }
                                Intent intent = new Intent(ListActivity.this, ShowActivity.class);
                                intent.putExtra("a", Rid_show);
                                intent.putExtra("b", patientid_show);
                                intent.putExtra("c", patientname_show);
                                intent.putExtra("d", patientage_show);
                                intent.putExtra("e", patientsex_show);
                                intent.putExtra("f", patienttel_show);
                                intent.putExtra("g", thisdate_show);
                                intent.putExtra("h", nextdate_show);
                                intent.putExtra("i", department_show);
                                intent.putExtra("j", doctorid_show);
                                intent.putExtra("k", doctorname_show);
                                intent.putExtra("l", detail_show);
                                intent.putExtra("m", opinion_show);
                                intent.putExtra("n", tempname);//人类之光
                                intent.putExtra("o", tempaccount);//人类之光
                                intent.putExtra("p", tempidentity);//人类之光
                                intent.putExtra("q", doctor_wholog);//当前登录的医生工号
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                /**/
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
                        Looper.loop();
                    }
                }.start();
            }
        });

        serach_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serach.getText().toString().isEmpty()) {
                    setListView();
                } else {
                    new Thread() {
                        public void run() {
                            Looper.prepare();
                            conn = DatabaseHelper.openConnection();
                            if (conn == null) {
                                Toast.makeText(ListActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    preparedStatement = conn.prepareStatement(sql4);
                                    Log.e("Oreooooooooooooooo",sql4);
                                    preparedStatement.setString(1, "%" + serach.getText().toString() + "%");
                                    preparedStatement.setString(2, "%" + serach.getText().toString() + "%");
                                    preparedStatement.setString(3, "%" + serach.getText().toString() + "%");
                                    preparedStatement.setString(4, "%" + serach.getText().toString() + "%");
                                    rs4 = preparedStatement.executeQuery();
                                    RecordList.clear();
                                    while (rs4.next()) {
                                        String Pid = rs4.getString("patientid");
                                        String name = rs4.getString("patientname");
                                        String department = rs4.getString("department");
                                        String date = rs4.getString("thisdate");
                                        RecordList.add(Pid + "\n" + name + "\n" + department + "\n" + date);
                                        Log.e("Listeee", Pid + "\n" + name + "\n" + department + "\n" + date);
                                    }
                                    Message message = new Message();
                                    message.what = TRANS;
                                    handler.sendMessage(message);
                                    flag = 1;
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (rs4 != null) {
                                        try {
                                            rs4.close();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (preparedStatement != null) {
                                        try {
                                            preparedStatement.close();
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

        /*抽屉那点事儿*/
        View headView = navigationView.getHeaderView(0);//获取HeadView
        acclogin = headView.findViewById(R.id.tv_accountlogin);
        accnum = headView.findViewById(R.id.tv_accountnum);
        acclogin.setText(tempname);//预留
        accnum.setText(tempaccount);//预留

        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.itme_1://二维码
                        Intent intent1 = new Intent(ListActivity.this, DeCodeActivity.class);
                        startActivity(intent1);
                        drawerLayout.closeDrawer(navigationView);
                        break;
                    case R.id.itme_2://番茄钟
                        Intent intent2 = new Intent(ListActivity.this, TomatoActivity.class);
                        startActivity(intent2);
                        drawerLayout.closeDrawer(navigationView);
                        break;
                    case R.id.itme_3://心率测量
                        Intent intent3 = new Intent(ListActivity.this, HeartRateActivity.class);
                        intent3.putExtra("o", tempaccount);//人类之光
                        startActivity(intent3);
                        drawerLayout.closeDrawer(navigationView);
                        break;
                    case R.id.itme_4://心率数据
                        Intent intent4 = new Intent(ListActivity.this, BPMDisplayActivity.class);
                        intent4.putExtra("o", tempaccount);//人类之光
                        startActivity(intent4);
                        drawerLayout.closeDrawer(navigationView);
                        break;
                    case R.id.itme_5://健康数据
                        Intent intent5 = new Intent(ListActivity.this, HealthyDataActivity.class);
                        intent5.putExtra("o", tempaccount);//人类之光
                        startActivity(intent5);
                        drawerLayout.closeDrawer(navigationView);
                        break;
                    case R.id.itme_6:
                        Intent intent6 = new Intent(ListActivity.this, QuestionListActivity.class);
                        intent6.putExtra("n", tempname);//人类之光
                        intent6.putExtra("o", tempaccount);//人类之光
                        intent6.putExtra("p", tempidentity);//人类之光
                        startActivity(intent6);
                        drawerLayout.closeDrawer(navigationView);
                        break;
                    case R.id.itme_7:
                        Intent intent7 = new Intent(ListActivity.this, LoginActivity.class);
                        //删除pref文件自动登录信息
                        editor.putBoolean("autologin", false);
                        editor.putString("tempname", "");
                        editor.putString("tempaccount", "");
                        editor.putString("tempidentity", "");
                        editor.apply();
                        startActivity(intent7);
                        drawerLayout.closeDrawer(navigationView);
                        finish();
                        break;
                    default:
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        RecordList.clear();
        setListView();
    }

    public void onRefresh() {
        RecordList.clear();
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
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordList);
        new Thread() {
            public void run() {
                Looper.prepare();
                conn = DatabaseHelper.openConnection();
                if (conn == null) {
                    Toast.makeText(ListActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                } else {
                    rs1 = DatabaseHelper.getResult(conn, sql1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (rs1.next()) {
                                    String Pid = rs1.getString("patientid");
                                    String name = rs1.getString("patientname");
                                    String department = rs1.getString("department");
                                    String date = rs1.getString("thisdate");
                                    RecordList.add(Pid + "\n" + name + "\n" + department + "\n" + date);
                                    Log.e("Listeee", Pid + "\n" + name + "\n" + department + "\n" + date);
                                }
                                recordView.setAdapter(adapter);
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
        getMenuInflater().inflate(R.menu.toolbar_list, menu); /* R.menu/toolbar_list.xml */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sidemenu:
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    drawerLayout.openDrawer(navigationView);
                }
                break;
            case R.id.insert:
                if (tempidentity.equals("patient")) {
                    Toast.makeText(ListActivity.this, "您无权添加病历", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(ListActivity.this, InsertActivity.class);
                    i.putExtra("n", tempname);//人类之光
                    i.putExtra("o", tempaccount);//人类之光
                    i.putExtra("p", tempidentity);//人类之光
                    i.putExtra("q", doctor_wholog);//当前登录的医生工号
                    i.putExtra("r", doctor_department);//当前登录的医生科室
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
                break;
            case R.id.serach:
                if(search_hidden){
                    ll_search.setVisibility(View.VISIBLE);
                    search_hidden = false;
                }else{
                    ll_search.setVisibility(View.GONE);
                    search_hidden = true;
                }
                break;
            default:
        }
        return true;
    }
}