package com.example.challyfilio.pmss.Alarm;

import com.example.challyfilio.pmss.R;
import com.example.challyfilio.pmss.Activity.TomatoActivity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class AlarmService extends Service {
    static Timer timer = null;
    public String ticker, title, Content;
    public int number = 0;  //记录次数
    AlarmManager manager;
    PendingIntent pi;

    // 添加通知
    public static void addNotification(Context context, long delayTime, String tickerText, String contentTitle, String contentText, int tag) {
        Intent intent = new Intent(context, AlarmService.class);
        intent.putExtra("delayTime", delayTime);
        intent.putExtra("tickerText", tickerText);
        intent.putExtra("contentTitle", contentTitle);
        intent.putExtra("contentText", contentText);
        intent.putExtra("tag", tag);
        context.startService(intent);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            NotificationManager mn = (NotificationManager) AlarmService.this.getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder builder = new Notification.Builder(AlarmService.this);
            switch (msg.what) {
                case 1:
                    builder.setSmallIcon(R.mipmap.ecology)// 图标
                            .setTicker(ticker)// 测试通知栏标题
                            .setContentTitle(title)// 标题
                            .setContentText(Content)// 内容
                            .setAutoCancel(false)// 点击弹出的通知后,让通知将自动取消
                            .setDefaults(Notification.DEFAULT_ALL);// 设置使用系统默认声音
                    break;
                case 2:
                    Intent notificationIntent = new Intent(AlarmService.this, TomatoActivity.class);// 点击跳转位置
                    PendingIntent contentIntent = PendingIntent.getActivity(AlarmService.this, 0, notificationIntent, 0);
                    builder.setContentIntent(contentIntent);
                    builder.setSmallIcon(R.mipmap.ecology)// 图标
                            .setTicker("自定义提醒")// 测试通知栏标题
                            .setContentTitle("自定义提醒")// 标题
                            .setContentText("时间到啦 " + (number - 1))// 内容
                            .setAutoCancel(false)// 点击弹出的通知后,让通知将自动取消
                            .setDefaults(Notification.DEFAULT_ALL);// 设置使用系统默认声音
                    break;
                default:
            }
            Notification notification = builder.build();
            mn.notify((int) System.currentTimeMillis(), notification);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        long period = 24 * 60 * 60 * 1000; // 24小时一个周期
        long delay = intent.getLongExtra("delayTime", 1);
        ticker = intent.getStringExtra("tickerText");
        title = intent.getStringExtra("contentTitle");
        Content = intent.getStringExtra("contentText");
        int tag = intent.getIntExtra("tag", 2);

        if (tag == 1) {
            if (null == timer) {
                timer = new Timer();
            }
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(1);
                }
            }, delay, period);
        } else if (tag == 2) {
            if (number != 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mHandler.sendEmptyMessage(2);
                    }
                }).start();
            }
            manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            long anHour = delay * 60 * 100;
            //Toast.makeText(AlarmService.this, "Remind On " + delay + " mins", Toast.LENGTH_SHORT).show();
            long triggerAtTime = SystemClock.elapsedRealtime() + (anHour);
            Intent i = new Intent(AlarmService.this, AlarmReceiver.class);
            pi = PendingIntent.getBroadcast(AlarmService.this, 0, i, 0);
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
            number++;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        manager.cancel(pi);
    }
}