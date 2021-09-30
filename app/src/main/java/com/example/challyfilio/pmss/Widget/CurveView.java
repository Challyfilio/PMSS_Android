package com.example.challyfilio.pmss.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CurveView extends View {
    private final Paint mAxisPaint;
    private Paint mDotPaint;
    private Paint mPaint;

    private final int mGap;//图和横轴的间距
    private final int mRadius;//点半径
    private final int mClickRadius;//触摸半径
    private int mStep;

    private double[] mDataList;
    private int mMax;
    private String[] mHorizontalAxis;//横轴

    private int mSelectedDotIndex = -1;
    private int mNormalDotColor;
    private int mSelectedDotColor = Color.GRAY;

    private List<Dot> mDots = new ArrayList<Dot>();
    private Rect mTextRect;
    private Path mCurvePath;

    private static final float SMOOTHNESS_RATIO = 0.16f;

    private float[][] mControlDots = new float[2][2];//存储控制点的二位数组

    public void setDataList(double[] dataList, int max, int mColor) {
        mDataList = dataList;
        mMax = max;
        mNormalDotColor = mColor;
    }

    public CurveView(Context context) {
        this(context, null);
    }

    public CurveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mAxisPaint = new Paint();
        mAxisPaint.setAntiAlias(true);//抗锯齿
        mAxisPaint.setTextSize(30);
        mAxisPaint.setTextAlign(Paint.Align.CENTER);

        mDotPaint = new Paint();
        mDotPaint.setAntiAlias(true);

        mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        mClickRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        mGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        mTextRect = new Rect();
        mCurvePath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mDots.clear();
        //减去上下左右的padding计算绘制区宽高
        int width = w - getPaddingLeft() - getPaddingRight();
        int height = h - getPaddingTop() - getPaddingBottom();
        mStep = width / (mDataList.length - 1);//平分宽度

        mAxisPaint.getTextBounds(mHorizontalAxis[0], 0, mHorizontalAxis[0].length(), mTextRect);
        int maxBarHeight = height - mTextRect.height() - mGap;
        float heightRatio = maxBarHeight / mMax;//计算点的高度与最大值的比值

        for (int i = 0; i < mDataList.length; i++) {
            Dot dot = new Dot();
            dot.value = mDataList[i];
            dot.transformedValue = dot.value * heightRatio;

            dot.x = mStep * i + getPaddingLeft();
            dot.y = (float) (getPaddingTop() + maxBarHeight - dot.transformedValue);

            mDots.add(dot);
        }
        //规划曲线路径
        for (int i = 0; i < mDataList.length - 1; i++) {
            //如果是第一个点，就将路径移动到该点
            if (i == 0) {
                mCurvePath.moveTo(mDots.get(0).x, mDots.get(0).y);
            }
            //计算三阶贝塞尔曲线的控制点
            calculateControlPoints(i);
            //使用三阶贝塞尔曲线连接下一个点
            mCurvePath.cubicTo(mControlDots[0][0], mControlDots[0][1],
                    mControlDots[1][0], mControlDots[1][1],
                    mDots.get(i + 1).x, mDots.get(i + 1).y);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(mNormalDotColor);
        canvas.drawPath(mCurvePath, mPaint);
        //绘制点和底部坐标文本
        for (int i = 0; i < mDots.size(); i++) {
            //绘制坐标文本
            String axis = mHorizontalAxis[i];
            int x = getPaddingLeft() + i * mStep;
            int y = getHeight() - getPaddingBottom();
            canvas.drawText(axis, x, y, mAxisPaint);
            Dot dot = mDots.get(i);
            if (i == mSelectedDotIndex) {//选中点的绘制
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

    public void setHorizontalAxis(String[] horizontalAxis) {
        mHorizontalAxis = horizontalAxis;
    }

    //计算三阶贝塞尔曲线的控制点
    public void calculateControlPoints(int i) {
        //控制点
        float x1, y1;
        float x2, y2;
        Dot currentDot = mDots.get(i);//当前点
        Dot nextDot;//下一个点
        Dot nextNextDot;//下下一个点
        Dot previousDot;//上一个点

        if (i > 0) {
            //当i>0，即不是第一个点时，获取上一个点
            previousDot = mDots.get(i - 1);
        } else {
            //当i=0，即为第一个点时，没有上一个点，就用第一个点代替
            previousDot = currentDot;
        }

        if (i < mDots.size() - 1) {
            //当i没有遍历到最后一个点时，获取下一个点
            nextDot = mDots.get(i + 1);
        } else {
            //当i=mDots.size()-1，即为最后一个点时，没有下一个点，就用最后一个点代替
            nextDot = currentDot;
        }

        if (i < mDots.size() - 2) {
            //当i没有遍历到倒数第二个点时，获取下下个点
            nextNextDot = mDots.get(i + 2);
        } else {
            //当i遍历到倒数第二个点，下下个点不存在，就用下个点代替
            nextNextDot = nextDot;
        }
        //利用公式计算两个控制点，参数取a=b=SMOOTHNESS_RATIO=0.16
        x1 = currentDot.x + SMOOTHNESS_RATIO * (nextDot.x - previousDot.x);
        y1 = currentDot.y + SMOOTHNESS_RATIO * (nextDot.y - previousDot.y);
        x2 = nextDot.x - SMOOTHNESS_RATIO * (nextNextDot.x - currentDot.x);
        y2 = nextDot.y - SMOOTHNESS_RATIO * (nextNextDot.y - currentDot.y);
        //保存计算结果到数组中，规划mCurvePath中使用
        mControlDots[0][0] = x1;
        mControlDots[0][1] = y1;
        mControlDots[1][0] = x2;
        mControlDots[1][1] = y2;
    }
    //初始化点
    private class Dot {
        float x;
        float y;
        double value;
        double transformedValue;//点值对应高度的像素大小
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mSelectedDotIndex = getClickDotIndex(event.getX(), event.getY());
                invalidate();//更新View
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
            float left = dot.x - mClickRadius;
            float bottom = dot.y - mClickRadius;
            float right = dot.x + mClickRadius;
            float top = dot.y + mClickRadius;
            //判断点是否在矩形区域内
            if (x > left && x < right && y > bottom && y < top) {
                index = i;
                break;
            }
        }
        return index;
    }
}