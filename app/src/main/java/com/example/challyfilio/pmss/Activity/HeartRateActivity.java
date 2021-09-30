package com.example.challyfilio.pmss.Activity;

import com.example.challyfilio.pmss.DatabaseHelper;
import com.example.challyfilio.pmss.R;
import com.example.challyfilio.pmss.Util.ImageProcessUtil;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HeartRateActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.iv_heart)
    ImageView iv;
    @BindView(R.id.linearLayout)
    LinearLayout layout;
    @BindView(R.id.bt_hrrecord)
    Button bt_hrrecord;
    //曲线
    private Timer timer = new Timer();
    private TimerTask task;//Timer任务，与Timer配套使用
    private static double flag = 1;
    private boolean tag = true;//手指是否离开摄像头
    private Handler handler;
    private String title = "";
    private XYSeries series;
    private GraphicalView chart;
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer renderer;
    private Context context;
    private int addX = -1;
    double addY;
    int[] xv = new int[200];
    int[] yv = new int[200];
    public static int beatsAvg;//心率
    public static int imgAvg;//平均像素值

    private static final AtomicBoolean processing = new AtomicBoolean(false);
    private static SurfaceView preview = null;//预览控件
    private static SurfaceHolder previewHolder = null;//预览设置信息
    private static Camera camera = null;//相机句柄
    private static TextView text = null;
    private static WakeLock wakeLock = null;
    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];

    //类型枚举
    public enum TYPE {
        GREEN, RED
    }

    //设置默认类型
    private static TYPE currentType = TYPE.GREEN;
    //心跳下标值
    private static int beatsIndex = 0;
    //心跳数组的大小
    private static final int beatsArraySize = 3;
    //心跳数组
    private static final int[] beatsArray = new int[beatsArraySize];
    //心跳脉冲
    private static double beats = 0;
    //开始时间
    private static long startTime = 0;

    String tempaccount;
    Connection conn;
    Statement stat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);
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

        //心跳动画
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(iv, "scaleX", 1.2f, 0.8f);//缩放X，1.2-0.8
        anim1.setRepeatCount(-1);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(iv, "scaleY", 1.2f, 0.8f);
        anim2.setRepeatCount(-1);//ValueAnimator.INFINITE
        AnimatorSet set = new AnimatorSet();
        set.play(anim1).with(anim2);
        set.setDuration(1000);
        set.start();

        initConfig();

        bt_hrrecord.setOnClickListener(new View.OnClickListener() {
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
                            Toast.makeText(HeartRateActivity.this, "网络出问题啦", Toast.LENGTH_SHORT).show();
                        } else {
                            String sql = "insert into heartrate values('" + tempaccount + "'," + beatsAvg + ",'" + date + "','" + time + "','" + t + "')";
                            try {
                                DatabaseHelper.exeStat(conn, sql);
                                Toast.makeText(HeartRateActivity.this, "记录成功", Toast.LENGTH_SHORT).show();
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
        });
    }

    //初始化配置
    @SuppressLint("InvalidWakeLockTag")
    private void initConfig() {
        /*曲线*/
        context = getApplicationContext();
        series = new XYSeries(title);//曲线上所有点的点集
        mDataset = new XYMultipleSeriesDataset();//数据集将被用来创建图表
        mDataset.addSeries(series);//将点集添加到数据集中

        renderer = buildRenderer();//渲染图表，曲线样式
        setChartSettings(renderer, "X", "Y", 0, 200, 30, 180, Color.WHITE);//设置图表样式
        chart = ChartFactory.getLineChartView(context, mDataset, renderer);//生成图表
        layout.addView(chart, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));//将图表添加到布局中

        //Handler实例将配合下面的Timer实例
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                updateChart();//刷新图表
            }
        };

        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };

        timer.schedule(task, 0, 100);//隔0.1秒更新一次
        text = (TextView) findViewById(R.id.tv_heartrate);
        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);//回调方法
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //当结束程序时关掉Timer
        timer.cancel();
    }

    //创建图表
    protected XYMultipleSeriesRenderer buildRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        //设置图表中曲线样式
        XYSeriesRenderer r = new XYSeriesRenderer();//渲染器
        r.setColor(Color.RED);
        r.setLineWidth(2);
        renderer.addSeriesRenderer(r);
        return renderer;
    }

    //设置图表样式
    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle,
                                    double xMin, double xMax, double yMin, double yMax, int labelsColor) {
        renderer.setChartTitle(title);
        renderer.setXTitle(xTitle);//x轴
        renderer.setYTitle(yTitle);//y
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setLabelsColor(labelsColor);
        renderer.setShowGrid(false);//网格
        //renderer.setGridColor(Color.BLUE);
        renderer.setXLabels(10);
        renderer.setYLabels(5);
        renderer.setXTitle("Time");
        renderer.setYTitle("Heart Rate");
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setPointSize((float) 3);
        renderer.setShowLegend(false);//图例
    }

    //更新图表
    private void updateChart() {
        //设置好下一个需要增加的节点
        if (flag == 1)
            addY = beatsAvg;
        else {
            flag = 1;
            if (imgAvg < 200) {
                //if (tag) {
                    Toast.makeText(HeartRateActivity.this, "请用您的指尖盖住摄像头镜头！", Toast.LENGTH_SHORT).show();
            //         tag = false;
            //     }
            //     tag = true;
            //     return;
            // } else
            //     tag = true;
            }
        }
        addY = beatsAvg;

        //移除数据集中旧的点集
        mDataset.removeSeries(series);

        //判断当前点集中到底有多少点，因为屏幕总共只能容纳200个，所以当点数超过200时，长度设为200
        int length = series.getItemCount();
        int bz = 0;
        if (length > 200) {
            length = 200;
            bz = 1;
        } else {
            bz = 1;
        }
        addX = length;
        //将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
        for (int i = 0; i < length; i++) {
            xv[i] = (int) series.getX(i) - bz;
            yv[i] = (int) series.getY(i);
        }
        //点集先清空，为了做成新的点集而准备
        series.clear();
        mDataset.addSeries(series);
        //将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
        series.add(addX, addY);
        for (int k = 0; k < length; k++) {
            series.add(xv[k], yv[k]);
        }
        chart.invalidate();//视图更新
    } //曲线

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        camera = Camera.open();
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    /**
     * 相机预览方法
     * 这个方法中实现动态更新界面UI的功能，
     * 通过获取手机摄像头的参数来实时动态计算平均像素值、脉冲数，从而实时动态计算心率值。
     */
    private static PreviewCallback previewCallback = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera cam) {//摄像头帧预览
            if (data == null)
                throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();//获取尺寸
            camera.setDisplayOrientation(90);
            if (size == null)
                throw new NullPointerException();
            if (!processing.compareAndSet(false, true))
                return;
            int width = size.width;
            int height = size.height;
            //图像处理
            imgAvg = ImageProcessUtil.decodeYUV420SPtoRedAvg(data.clone(), height, width);
            Log.e("HRA", "平均像素值：" + imgAvg);
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false);
                return;
            }
            //计算平均值
            int averageArrayAvg = 0;
            int averageArrayCnt = 0;
            for (int i = 0; i < averageArray.length; i++) {
                if (averageArray[i] > 0) {//只考虑正特性
                    averageArrayAvg += averageArray[i];
                    averageArrayCnt++;
                }
            }
            //计算滚动平均值 一段时间内的均值
            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
            TYPE newType = currentType;
            if (imgAvg < rollingAverage) {
                newType = TYPE.RED;
                if (newType != currentType) {
                    beats++;//脉冲
                    flag = 0;
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN;
            }

            if (averageIndex == averageArraySize)
                averageIndex = 0;
            averageArray[averageIndex] = imgAvg;
            averageIndex++;

            // Transitioned from one state to another to the same
            if (newType != currentType) {
                currentType = newType;
            }

            //获取系统结束时间（ms）
            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d;
            if (totalTimeInSecs >= 2) {
                double bps = (beats / totalTimeInSecs);
                Log.e("tag_bps", String.valueOf(bps));
                int bpm = (int) (bps * 60d);
                Log.e("tag_bpm", String.valueOf(bpm));
                if (bpm < 30 || bpm > 180 || imgAvg < 200) {
                    //获取系统开始时间（ms）
                    startTime = System.currentTimeMillis();
                    //beats心跳总数
                    beats = 0;
                    processing.set(false);
                    return;
                }
                //求瞬时心跳的滚动平均值
                if (beatsIndex == beatsArraySize)
                    beatsIndex = 0;
                beatsArray[beatsIndex] = bpm;
                beatsIndex++;
                int beatsArrayAvg = 0;
                int beatsArrayCnt = 0;
                for (int i = 0; i < beatsArray.length; i++) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i];
                        beatsArrayCnt++;
                    }
                }
                beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                text.setText("❤ HEART RATE：" + String.valueOf(beatsAvg) + " BPM");
                //获取系统时间（ms）
                startTime = System.currentTimeMillis();
                beats = 0;
            }
            processing.set(false);
        }
    };

    //预览回调接口
    private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        //创建时调用
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);//连接到SurfaceView,实时预览
                camera.setPreviewCallback(previewCallback);//添加回调方法
            } catch (Throwable t) {
                Log.e("Callback", "setPreviewDisplay()：", t);
            }
        }

        //当预览改变的时候回调此方法
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();//获得相机设置参数
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//开闪光灯
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }

        //销毁的时候调用
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    //获取相机最小的预览尺寸
    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea < resultArea)
                        result = size;
                }
            }
        }
        return result;
    }
}