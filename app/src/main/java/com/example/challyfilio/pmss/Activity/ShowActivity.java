package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.Alarm.AlarmService;
import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;
import com.example.challyfilio.pmss.Util.CalendarUtil;
import com.example.challyfilio.pmss.Util.QRCodeUtil;
import com.example.challyfilio.pmss.Util.STGalleryUtil;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_Rid_show)
    TextView tv_Rid_show;
    @BindView(R.id.tv_patientid_show)
    TextView tv_patientid_show;
    @BindView(R.id.tv_patientname_show)
    TextView tv_patientname_show;
    @BindView(R.id.tv_patientage_show)
    TextView tv_patientage_show;
    @BindView(R.id.tv_patientsex_show)
    TextView tv_patientsex_show;
    @BindView(R.id.tv_patienttel_show)
    TextView tv_patienttel_show;
    @BindView(R.id.tv_thisdate_show)
    TextView tv_thisdate_show;
    @BindView(R.id.tv_nextdate_show)
    TextView tv_nextdate_show;
    @BindView(R.id.tv_department_show)
    TextView tv_department_show;
    @BindView(R.id.tv_doctorid_show)
    TextView tv_doctorid_show;
    @BindView(R.id.tv_doctorname_show)
    TextView tv_doctorname_show;
    @BindView(R.id.tv_detail_show)
    TextView tv_detail_show;
    @BindView(R.id.tv_opinion_show)
    TextView tv_opinion_show;
    @BindView(R.id.bt_change)
    Button change;
    @BindView(R.id.bt_delete)
    Button delete;
    @BindView(R.id.bt_addschedule)
    Button schedule;
    @BindView(R.id.bt_addremind)
    Button remind;
    @BindView(R.id.create_qr_iv)
    ImageView imageView;
    String filePath;
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
            tempidentity,//p
            doctor_wholog;//q //当前医生工号 验证是否有权删除修改病历
    Connection conn;
    Statement stat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
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

        delete.setOnClickListener(this);
        change.setOnClickListener(this);
        schedule.setOnClickListener(this);
        remind.setOnClickListener(this);

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
        doctor_wholog = intent.getStringExtra("q");//当前医生工号 验证是否有权删除修改病历

        tv_Rid_show.setText("病历编号：" + Rid_show);//a
        tv_patientid_show.setText("社保账号：" + patientid_show);//b
        tv_patientname_show.setText("患者姓名：" + patientname_show);//c
        tv_patientage_show.setText("患者年龄：" + patientage_show);//d
        tv_patientsex_show.setText("患者性别：" + patientsex_show);//e
        tv_patienttel_show.setText("联系电话：" + patienttel_show);//f
        tv_thisdate_show.setText("看诊日期：" + thisdate_show);//g
        tv_nextdate_show.setText("复诊日期：" + nextdate_show);//h
        tv_department_show.setText("看诊科室：" + department_show);//i
        tv_doctorid_show.setText("医生工号：" + doctorid_show);//j
        tv_doctorname_show.setText("医生姓名：" + doctorname_show);//k
        tv_detail_show.setText("诊断结果：" + detail_show);//l
        tv_opinion_show.setText(opinion_show);//m

        tv_patienttel_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + patienttel_show));
                startActivity(intent);
            }
        });

        //长按图片监听器
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Bitmap obmp = ((BitmapDrawable) (imageView).getDrawable()).getBitmap();
                int width = obmp.getWidth();
                int height = obmp.getHeight();
                int[] data = new int[width * height];
                obmp.getPixels(data, 0, width, 0, 0, width, height);
                showSelectAlert(obmp);
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_change:
                if (tempidentity.equals("patient")) {
                    Toast.makeText(ShowActivity.this, "您无权修改病历，患者", Toast.LENGTH_SHORT).show();
                } else {
                    if (doctor_wholog.equals(doctorid_show)) {
                        Intent i = new Intent(ShowActivity.this, UpdateActivity.class);
                        i.putExtra("a", Rid_show);
                        i.putExtra("b", patientid_show);
                        i.putExtra("c", patientname_show);
                        i.putExtra("d", patientage_show);
                        i.putExtra("e", patientsex_show);
                        i.putExtra("f", patienttel_show);
                        i.putExtra("g", thisdate_show);
                        i.putExtra("h", nextdate_show);
                        i.putExtra("i", department_show);
                        i.putExtra("j", doctorid_show);
                        i.putExtra("k", doctorname_show);
                        i.putExtra("l", detail_show);
                        i.putExtra("m", opinion_show);
                        i.putExtra("n", tempname);//人类之光
                        i.putExtra("o", tempaccount);//人类之光
                        i.putExtra("p", tempidentity);//人类之光
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(ShowActivity.this, "您无权修改病历，医生", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.bt_delete:
                if (tempidentity.equals("patient")) {
                    Toast.makeText(ShowActivity.this, "您无权删除病历，患者", Toast.LENGTH_SHORT).show();
                } else {
                    if (doctor_wholog.equals(doctorid_show)) {
                        new Thread() {
                            public void run() {
                                Looper.prepare();
                                conn = DatabaseHelper.openConnection();
                                if (conn == null) {
                                    Toast.makeText(ShowActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                                } else {
                                    String sql = "delete from recordinfo where Rid='" + Rid_show + "'";
                                    try {
                                        DatabaseHelper.exeStat(conn, sql);
                                        Toast.makeText(ShowActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
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
                    } else {
                        Toast.makeText(ShowActivity.this, "您无权删除病历，医生", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.bt_addschedule:
                if (tempidentity.equals("patient")) {
                    if (nextdate_show.equals("")) {
                        Toast.makeText(ShowActivity.this, "无复诊日期不可添加", Toast.LENGTH_SHORT).show();
                    } else {
                        CalendarUtil.addCalendarEvent(ShowActivity.this, "复诊", "到宇霸霸私立黑诊所" + department_show + "复诊", nextdate_show + " " + "08:00:00");
                    }
                } else {
                    Toast.makeText(ShowActivity.this, "仅患者可添加复诊日程", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_addremind:
                if (tempidentity.equals("patient")) {
                    if (nextdate_show.equals("")) {
                        Toast.makeText(ShowActivity.this, "无复诊日期不可添加", Toast.LENGTH_SHORT).show();
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date;
                        long value_set = 0;
                        String str_date = nextdate_show + " 12:00:00";
                        Log.e("Showee", str_date);
                        try {
                            date = sdf.parse(str_date);
                            value_set = date.getTime();//设置时间
                            Log.e("Showee", "设置时间:" + value_set);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        long value_now = System.currentTimeMillis();//系统时间
                        Log.e("Showee", "当前时间:" + value_now);
                        if (value_set <= value_now) {
                            Toast.makeText(getApplicationContext(), "复诊时间已过，不可添加", Toast.LENGTH_SHORT).show();
                        } else {
                            long delaytime = value_set - value_now - 24 * 60 * 60 * 1000;//提前一天的12点提醒
                            if (delaytime <= 0) {
                                AlarmService.addNotification(ShowActivity.this, 0, "复诊提醒", "复诊提醒", "明天到宇霸霸私立黑诊所" + department_show + "复诊", 1);
                                Toast.makeText(getApplicationContext(), "Remind On", Toast.LENGTH_SHORT).show();
                            } else {
                                AlarmService.addNotification(ShowActivity.this, delaytime, "复诊提醒", "复诊提醒", "明天到宇霸霸私立黑诊所" + department_show + "复诊", 1);
                                Toast.makeText(getApplicationContext(), "Remind On", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(ShowActivity.this, "仅患者可添加复诊提醒", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_show, menu); /* R.menu/toolbar_show.xml */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.QRshare:
                filePath = STGalleryUtil.getFileRoot(ShowActivity.this) + File.separator
                        + "qr_" + System.currentTimeMillis() + ".jpg";
                //二维码图片较大时，生成图片、保存文件的时间可能较长，因此放在新线程中
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        boolean success = QRCodeUtil.createQRImage(Rid_show + "$aEi3bVr0", 800, 800, filePath);
                        if (success) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
                                }
                            });
                        }
                        Looper.loop();
                    }
                }).start();
                break;
            default:
        }
        return true;
    }

    //弹出框
    private void showSelectAlert(final Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择");
        String str[] = {"保存图片"};
        builder.setItems(str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterfacem, int i) {
                switch (i) {
                    case 0:
                        STGalleryUtil.saveImageToGallery(ShowActivity.this, bitmap, filePath);
                        Toast.makeText(ShowActivity.this, "已保存", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterfacem, int i) {
            }
        });
        builder.show();
    }//showSelectAlert
}