package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.sql.*;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeCodeActivity extends AppCompatActivity {
    public static final int CHOOSE_PHOTO = 2;
    public static final int UPDATE = 1;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.decode_qr_iv)
    ImageView imageView;
    @BindView(R.id.tv_decode_de)
    TextView Decode_show;
    @BindView(R.id.tv_Rid_de)
    TextView tv_Rid_show;
    @BindView(R.id.tv_patientid_de)
    TextView tv_patientid_show;
    @BindView(R.id.tv_patientname_de)
    TextView tv_patientname_show;
    @BindView(R.id.tv_patientage_de)
    TextView tv_patientage_show;
    @BindView(R.id.tv_patientsex_de)
    TextView tv_patientsex_show;
    @BindView(R.id.tv_patienttel_de)
    TextView tv_patienttel_show;
    @BindView(R.id.tv_thisdate_de)
    TextView tv_thisdate_show;
    @BindView(R.id.tv_nextdate_de)
    TextView tv_nextdate_show;
    @BindView(R.id.tv_department_de)
    TextView tv_department_show;
    @BindView(R.id.tv_doctorid_de)
    TextView tv_doctorid_show;
    @BindView(R.id.tv_doctorname_de)
    TextView tv_doctorname_show;
    @BindView(R.id.tv_detail_de)
    TextView tv_detail_show;
    @BindView(R.id.tv_doc_de)
    TextView tv_doc_show;
    @BindView(R.id.tv_opinion_de)
    TextView tv_opinion_show;
    String imagePath, key, recordid;
    Connection conn;
    Statement stat;
    ResultSet rs;

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
            opinion_show;//m

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE:
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
                    tv_doc_show.setText("医生建议：");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_de_code);
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

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View viewm) {
                Bitmap obmp = ((BitmapDrawable) (imageView).getDrawable()).getBitmap();
                int width = obmp.getWidth();
                int height = obmp.getHeight();
                int[] data = new int[width * height];
                obmp.getPixels(data, 0, width, 0, 0, width, height);
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
                BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
                QRCodeReader reader = new QRCodeReader();
                Result re = null;
                try {
                    re = reader.decode(bitmap1);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                } catch (ChecksumException e) {
                    e.printStackTrace();
                } catch (FormatException e) {
                    e.printStackTrace();
                }
                if (re == null) {
                    Toast.makeText(DeCodeActivity.this, "这个不是二维码", Toast.LENGTH_SHORT).show();
                } else {
                    showSelectAlert(re.getText());
                }
                return false;
            }
        });
    }

    private void showSelectAlert(final String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择");
        String str[] = {"扫描二维码"};
        builder.setItems(str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterfacem, int i) {
                switch (i) {
                    case 0:
                        if (url.contains("$aEi3bVr0")) {
                            recordid = url.substring(0, url.indexOf("$"));
                            key = url.substring(url.indexOf("$") + 1);
                            Decode_show.setText("扫描的结果如下：");
                            new Thread() {
                                public void run() {
                                    Looper.prepare();
                                    conn = DatabaseHelper.openConnection();
                                    if (conn == null) {
                                        Toast.makeText(DeCodeActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                                    } else {
                                        rs = DatabaseHelper.getResult(conn, "select * from recordinfo where Rid = '" + recordid + "'");
                                        try {
                                            while (rs.next()) {
                                                Rid_show = rs.getString("Rid");//a
                                                patientid_show = rs.getString("patientid");//b
                                                patientname_show = rs.getString("patientname");//c
                                                patientage_show = rs.getString("patientage");//d
                                                patientsex_show = rs.getString("patientsex");//e
                                                patienttel_show = rs.getString("patienttel");//f
                                                thisdate_show = rs.getString("thisdate");//g
                                                nextdate_show = rs.getString("nextdate");//h
                                                department_show = rs.getString("department");//i
                                                doctorid_show = rs.getString("doctorid");//j
                                                doctorname_show = rs.getString("doctorname");//k
                                                detail_show = rs.getString("detail");//l
                                                opinion_show = rs.getString("opinion");//m
                                            }
                                            Message message = new Message();
                                            message.what = UPDATE;
                                            handler.sendMessage(message);
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
                        } else {
                            Decode_show.setText("扫描的结果如下：\n" + url);
                            tv_Rid_show.setText("");//a
                            tv_patientid_show.setText("");//b
                            tv_patientname_show.setText("");//c
                            tv_patientage_show.setText("");//d
                            tv_patientsex_show.setText("");//e
                            tv_patienttel_show.setText("");//f
                            tv_thisdate_show.setText("");//g
                            tv_nextdate_show.setText("");//h
                            tv_department_show.setText("");//i
                            tv_doctorid_show.setText("");//j
                            tv_doctorname_show.setText("");//k
                            tv_detail_show.setText("");//l
                            tv_opinion_show.setText("");//m
                            tv_doc_show.setText("");
                        }
                        break;
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterfacem, int i) {
            }
        });
        builder.show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_qrcode, menu); /* R.menu/toolbar_qrcode.xml */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.qrcode:
                if (ContextCompat.checkSelfPermission(DeCodeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DeCodeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
                break;
            default:
        }
        return true;
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "拒绝授权", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        }
    }
}