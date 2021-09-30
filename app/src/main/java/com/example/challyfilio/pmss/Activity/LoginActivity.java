package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.Util.MD5Util;
import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.sql.*;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.edit_name)
    EditText accountEdit;
    @BindView(R.id.edit_password)
    EditText passwordEdit;
    @BindView(R.id.bt_login)
    Button Login;
    @BindView(R.id.bt_register)
    Button Register;
    @BindView(R.id.remember_pass)
    CheckBox rememberPass;
    @BindView(R.id.auto_login)
    CheckBox autoLogin;
    @BindView(R.id.rbt_doctor)
    RadioButton doctor;
    @BindView(R.id.rbt_patient)
    RadioButton patient;
    @BindView(R.id.identity)
    RadioGroup identity;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    String login_identity = "doctor";
    String sql, account, password, name;
    Connection conn;
    Statement stat;
    ResultSet rs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

        autoLogin.setChecked(false);
        //记住密码
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemember = pref.getBoolean("remember_password", false);
        if (isRemember) {
            //将账号密码设置到文本框
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }

        //RadioButton监听器
        identity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (doctor.getId() == i) {
                    login_identity = "doctor";
                }
                if (patient.getId() == i) {
                    login_identity = "patient";
                }
            }
        });

        /*Boardcast*/
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sql = "select * from user where identity = '" + login_identity + "'";
                Log.e("Loginee", sql);
                new Thread() {
                    public void run() {
                        Looper.prepare();
                        boolean flag_log = false;
                        conn = DatabaseHelper.openConnection();
                        Log.e("loge", String.valueOf(conn));
                        if (conn == null) {
                            Toast.makeText(LoginActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            rs = DatabaseHelper.getResult(conn, sql);
                            if ((accountEdit.getText().toString().isEmpty()) || (passwordEdit.getText().toString().isEmpty())) {
                                Toast.makeText(LoginActivity.this, "不能为空,请重新输入", Toast.LENGTH_SHORT).show();
                            } else {
                                try {
                                    while (rs.next()) {
                                        account = rs.getString("patientid");//社保账号
                                        password = rs.getString("passwd");//MD5加密后的密码
                                        name = rs.getString("name");
                                        if ((accountEdit.getText().toString().equals(account)) && (MD5Util.encrypt(passwordEdit.getText().toString()).equals(password))) {
                                            editor = pref.edit();
                                            //记住密码
                                            if (rememberPass.isChecked()) {
                                                editor.putBoolean("remember_password", true);
                                                editor.putString("account", accountEdit.getText().toString());
                                                editor.putString("password", passwordEdit.getText().toString());
                                            } else {
                                                editor.putBoolean("remember_password", false);
                                                editor.putString("account", "");
                                                editor.putString("password", "");
                                            }
                                            //自动登录
                                            if (autoLogin.isChecked()) {
                                                editor.putBoolean("autologin", true);
                                                editor.putString("tempname", name);
                                                editor.putString("tempaccount", accountEdit.getText().toString());
                                                editor.putString("tempidentity", login_identity);
                                            } else {
                                                editor.putBoolean("autologin", false);
                                                editor.putString("tempname", "");
                                                editor.putString("tempaccount", "");
                                                editor.putString("tempidentity", "");
                                            }
                                            editor.apply();
                                            flag_log = true;
                                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, ListActivity.class);
                                            intent.putExtra("tempname", name);
                                            intent.putExtra("tempaccount", accountEdit.getText().toString());
                                            intent.putExtra("tempidentity", login_identity);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                } catch (SQLException e) {
                                    e.getMessage();
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
                                if (!flag_log) {
                                    Toast.makeText(LoginActivity.this, "账号或密码错误,请重新输入", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                        Looper.loop();
                    }
                }.start();
            }
        });

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }//OnCreate

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                Toast.makeText(context, "Network Is Available", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Network Is Unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }
}