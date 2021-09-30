package com.example.challyfilio.pmss.Widget;

import android.view.View;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.example.challyfilio.pmss.R;

import java.util.ArrayList;
import java.util.List;

public class LineChartView extends View {

    private Paint mAxisPaint;
    private Paint mDotPaint;
    private Paint mLinePaint;
    private Paint mGradientPaint;

    private static int[] DEFAULT_GRADIENT_COLORS = {Color.RED, Color.WHITE};//渐变
    private int[] mDataList;
    private int mMax;
    private String[] mHorizontalAxis;

    private final int mRadius;
    private final int mClickRadius;

    private List<Dot> mDots = new ArrayList<Dot>();
    private Rect mTextRect;
    private int mGap;
    private Path mPath;
    private Path mGradientPath;
    private int mStep;

    private int mSelectedDotIndex = -1;
    private int mSelectedDotColor;
    private int mNormalDotColor;
    private int mLineColor;

    public LineChartView(Context context) {
        this(context, null);
    }

    //初始化
    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LineChartView);
        mLineColor = typedArray.getColor(R.styleable.LineChartView_line_color, Color.BLACK);//获取线的颜色
        mNormalDotColor = typedArray.getColor(R.styleable.LineChartView_dot_normal_color, Color.BLACK);//获取点的颜色
        mSelectedDotColor = typedArray.getColor(R.styleable.LineChartView_dot_selected_color, Color.RED);//获取选中的颜色
        typedArray.recycle();//回收

        initPaint();

        mPath = new Path();
        mGradientPath = new Path();

        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        mClickRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        mTextRect = new Rect();
        mGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
    }

    private void initPaint() {
        mAxisPaint = new Paint();
        mAxisPaint.setAntiAlias(true);
        mAxisPaint.setTextSize(20);
        mAxisPaint.setTextAlign(Paint.Align.CENTER);

        mDotPaint = new Paint();
        mDotPaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(mLineColor);

        mGradientPaint = new Paint();
        mGradientPaint.setAntiAlias(true);
    }

    //设置数据
    public void setDataList(int[] dataList, int max) {
        mDataList = dataList;
        mMax = max;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mDots.clear();//清空点集
        //去除Padding 计算绘制区域宽高
        int width = w - getPaddingLeft() - getPaddingRight();
        int height = h - getPaddingTop() - getPaddingBottom();
        mStep = width / (mDataList.length - 1);//根据点的个数平分宽度

        //为计算maxBarHeight提供数据，maxBarHeight为折线最大高度
        mAxisPaint.getTextBounds(mHorizontalAxis[0], 0, mHorizontalAxis[0].length(), mTextRect);
        int barHeight = height - mTextRect.height() - mGap;
        float heightRatio = barHeight / mMax;

        //遍历所有点
        for (int i = 0; i < mDataList.length; i++) {
            //初始化对应位置的点
            Dot dot = new Dot();
            dot.value = mDataList[i];
            dot.transformedValue = (int) (dot.value * heightRatio);
            dot.x = mStep * i + getPaddingLeft();
            dot.y = getPaddingTop() + barHeight - dot.transformedValue;
            //第一个点时，路径移至该点
            if (i == 0) {
                mPath.moveTo(dot.x, dot.y);
                mGradientPath.moveTo(dot.x, dot.y);
            } else {
                //连线
                mPath.lineTo(dot.x, dot.y);
                mGradientPath.lineTo(dot.x, dot.y);
            }
            //最后一个点
            if (i == mDataList.length - 1) {
                int bottom = getPaddingTop() + barHeight;
                mGradientPath.lineTo(dot.x, bottom);//将路径连到底部
                Dot firstDot = mDots.get(0);
                mGradientPath.lineTo(firstDot.x, bottom);//连接第一个点的底部
                mGradientPath.lineTo(firstDot.x, firstDot.y);//连接到第一个点，形成闭合区域
            }
            mDots.add(dot);
        }
        Shader shader = new LinearGradient(0, 0, 0, getHeight(), DEFAULT_GRADIENT_COLORS, null, Shader.TileMode.CLAMP);
        mGradientPaint.setShader(shader);
    }

    //绘制曲线图
    @Override
    protected void onDraw(Canvas canvas) {
        //绘制折线路径
        canvas.drawPath(mPath, mLinePaint);
        canvas.drawPath(mGradientPath, mGradientPaint);
        for (int i = 0; i < mDots.size(); i++) {
            //绘制坐标文本
            String axis = mHorizontalAxis[i];
            int x = getPaddingLeft() + i * mStep;
            int y = getHeight() - getPaddingBottom();
            canvas.drawText(axis, x, y, mAxisPaint);
            Dot dot = mDots.get(i);
            if (i == mSelectedDotIndex) {
                //设置点击时的颜色
                mDotPaint.setColor(mSelectedDotColor);
                //绘制数据文本
                canvas.drawText(String.valueOf(mDataList[i]), dot.x, dot.y - mRadius - mGap, mAxisPaint);
            } else {
                //设置其他点的颜色
                mDotPaint.setColor(mNormalDotColor);
            }
            //绘制点
            canvas.drawCircle(dot.x, dot.y, mRadius, mDotPaint);
        }
    }

    //横轴
    public void setHorizontalAxis(String[] horizontalAxis) {
        mHorizontalAxis = horizontalAxis;
    }

    //初始化点
    private class Dot {
        //点坐标
        int x;
        int y;
        int value;//点值
        int transformedValue;//点值对应高度的像素大小
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mSelectedDotIndex = getClickDotIndex(event.getX(), event.getY());
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mSelectedDotIndex = -1;
                invalidate();
                break;
        }
        return true;
    }

    private int getClickDotIndex(float x, float y) {
        int index = -1;
        for (int i = 0; i < mDots.size(); i++) {
            Dot dot = mDots.get(i);
            //初始化接受点击事件的矩形区域
            int left = dot.x - mClickRadius;
            int top = dot.y - mClickRadius;
            int right = dot.x + mClickRadius;
            int bottom = dot.y + mClickRadius;
            //判断点是否在矩形区域内
            if (x > left && x < right && y > top && y < bottom) {
                index = i;
                break;
            }
        }
        return index;
    }
}