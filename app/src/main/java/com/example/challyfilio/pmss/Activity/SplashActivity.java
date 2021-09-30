package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        String tempname = pref.getString("tempname", "");
        String tempaccount = pref.getString("tempaccount", "");
        String tempidentity = pref.getString("tempidentity", "");
        boolean auto = pref.getBoolean("autologin", false);
        Log.e("splash", tempname + " " + tempaccount + " " + tempidentity + " " + auto);

        //利用timer让此界面延迟3秒后跳转，timer有一个线程，该线程不断执行task
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (auto) {
                    Intent intent = new Intent(SplashActivity.this, ListActivity.class);
                    intent.putExtra("tempname", tempname);
                    intent.putExtra("tempaccount", tempaccount);
                    intent.putExtra("tempidentity", tempidentity);
                    startActivity(intent);
                    //跳转后关闭当前欢迎页面
                    SplashActivity.this.finish();
                } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    SplashActivity.this.finish();
                }
            }
        };
        //调度执行timerTask，第二个参数传入延迟时间（毫秒）
        timer.schedule(timerTask, 3000);
    }
}