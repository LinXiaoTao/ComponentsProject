package com.leo.componentsproject.weiget.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created on 2017/5/25 下午4:39.
 * leo linxiaotao1993@vip.qq.com
 */

public class ChartLayout extends ViewGroup {


    private List<ChartData> mChartDatas;
    private int mXmin, mXmax;
    private int mYmin, mYmax;
    private int mXmaxCount;
    private String mXunix;
    private String mYunix;
    private int mXinterval;
    private int mYinterval;
    private String mTag;
    private float mTagRadius;
    private int mTagSize;
    @ColorInt
    private int mTagColor;
    @ColorInt
    private int mAxisColor;

    private int mAxisPadding;
    private AxisInfo mXunixInfo, mYunixInfo;
    private int mXunixWidth, mYunixHeight;
    private Rect mCoordinateRect;
    private Rect mTagRect;
    private Rect mChartRect;
    private Paint mChartPaint;
    private Paint mTextPaint;
    private float mTagCentX, mTagCentY;
    private float mTagx, mTagy;
    private static final Pools.SimplePool<Rect> RECT_SIMPLE_POOL = new Pools.SimplePool<>(12);
    private Context mContext;
    private int mNeedWidth;
    private int mNeedHeight;
    private LineInfo mYaxisLine;
    private List<AxisInfo> mXaxis, mYaxis;
    private List<LineInfo> mLineInfos;
    private int mChildTop;

    public ChartLayout(Context context) {
        this(context, null);
    }

    public ChartLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mChartRect.set(0, 0, w, h);

        float scale = 6f;
        mTagRect.set(mChartRect);
        mTagRect.top = (int) (h * (scale / (scale + 1)));

        mCoordinateRect.set(mChartRect);
        mCoordinateRect.bottom = mTagRect.top;

        calculationAxis();
        calculationTag();


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (mNeedHeight > 0 && mNeedWidth > 0) {
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height - mNeedHeight, MeasureSpec.EXACTLY);
            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width - mNeedWidth, MeasureSpec.EXACTLY);
            measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() > 0) {
            System.out.println("ChartLayout onLayout：" + l + "," + t + "," + r + "," + b);
            View childView = getChildAt(0);
            childView.layout(mNeedWidth, mChildTop, getMeasuredWidth(), getMeasuredHeight() - mTagRect.height());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawTag(canvas);
        drawAxis(canvas);
        drawUnix(canvas);
        drawLine(canvas);
        drawYlines(canvas);

    }

    private void drawYlines(Canvas canvas) {
        if (!mLineInfos.isEmpty()) {
            mChartPaint.setColor(mAxisColor);
            for (LineInfo lineInfo : mLineInfos) {
                canvas.drawLine(lineInfo.startX, lineInfo.startY, lineInfo.endX, lineInfo.endY, mChartPaint);
            }
        }
    }

    private void drawLine(Canvas canvas) {
        mChartPaint.setColor(mAxisColor);
        canvas.drawLine(mYaxisLine.startX, mYaxisLine.startY, mYaxisLine.endX, mYaxisLine.endY, mChartPaint);
    }

    private void drawUnix(Canvas canvas) {
        if (!TextUtils.isEmpty(mYunix)) {
            mTextPaint.setTextSize(mTagSize);
            canvas.drawText(mYunix, mYunixInfo.x, mYunixInfo.y, mTextPaint);
        }
    }

    private void drawAxis(Canvas canvas) {

        mTextPaint.setTextSize(mTagSize);
        mTextPaint.setColor(mAxisColor);

        for (AxisInfo axisInfo : mYaxis) {
            canvas.drawText(axisInfo.value, axisInfo.x, axisInfo.y, mTextPaint);
        }
    }

    private void drawTag(Canvas canvas) {
        if (!TextUtils.isEmpty(mTag)) {
            mTextPaint.setTextSize(mTagSize);
            mTextPaint.setColor(mTagColor);
            canvas.drawCircle(mTagCentX, mTagCentY, mTagRadius, mTextPaint);
            canvas.drawText(mTag, mTagx, mTagy, mTextPaint);
        }
    }

    private void init() {

        setWillNotDraw(false);

        mXmaxCount = 13;
        mXinterval = 1;
        mYinterval = 20;
        mXmin = 0;
        mXmax = 12;
        mYmin = 0;
        mYmax = 100;
        mXunix = "(个)";
        mYunix = "(月)";

        mXaxis = new ArrayList<>();
        mYaxis = new ArrayList<>();

        mTagSize = spTopx(15f);
        mTagColor = Color.BLUE;
        mTagRadius = dpTopx(5f);

        mTag = "测试";
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mChartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mChartRect = new Rect();
        mTagRect = new Rect();
        mCoordinateRect = new Rect();
        mAxisPadding = dpTopx(10f);

        mYaxisLine = new LineInfo();
        mAxisColor = Color.DKGRAY;

        ChartScrollView horizontalScrollView = new ChartScrollView(mContext);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(horizontalScrollView, layoutParams);

        mLineInfos = new ArrayList<>();

        mChartDatas = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 13; i++) {
            ChartData chartData = new ChartData();
            chartData.setX(i);
            chartData.setY((int) (random.nextFloat() * mYmax));
            mChartDatas.add(chartData);
        }
        System.out.println("测试数据：" + mChartDatas);

    }


    private void calculationAxis() {

        int xCount = (mXmax - mXmin) / mXinterval + 1;
        for (int i = 0; i < xCount; i++) {
            AxisInfo axisInfo = new AxisInfo();
            axisInfo.value = String.valueOf(i);
            mXaxis.add(axisInfo);
        }
        int yCount = (mYmax - mYmin) / mYinterval + 1;
        for (int i = 0; i < yCount; i++) {
            AxisInfo axisInfo = new AxisInfo();
            axisInfo.value = String.valueOf(mYmax - i * mYinterval);
            mYaxis.add(axisInfo);
        }

        int yItemWidth = calculationYitemWidth();
        final int xItemHeight = calculationXitemHeight();

        int yItemHeight = (mCoordinateRect.height() - xItemHeight - mYunixHeight) / (yCount - 1);
        final int xItemWidth = (mCoordinateRect.width() - yItemWidth - mAxisPadding) / (Math.min(mXmaxCount, mXaxis.size()) - 1);

        System.out.println("X 轴 Item width：" + xItemWidth);

        int yTop = mCoordinateRect.top + mYunixHeight;
        int yLeft = mChartRect.left;
        for (int i = 0; i < mYaxis.size(); i++) {
            AxisInfo axisInfo = mYaxis.get(i);
            axisInfo.x = yLeft + (yItemWidth - axisInfo.w);
            if (i == mYaxis.size() - 1) {
                axisInfo.y = yTop + yItemHeight * i;
            } else {
                axisInfo.y = yTop + axisInfo.h + yItemHeight * i;
            }
        }
        System.out.println("Y 轴：" + mYaxis);

        if (!TextUtils.isEmpty(mYunix)) {
            mYunixInfo.x = yLeft + (yItemWidth - mYunixInfo.w);
            mYunixInfo.y = mChartRect.top + mYunixInfo.h;
        }

        final AxisInfo lastAxis = mYaxis.get(mYaxis.size() - 1);
        AxisInfo fistAxis = mYaxis.get(0);

        mYaxisLine.startX = yLeft + yItemWidth + mAxisPadding;
        mYaxisLine.startY = lastAxis.y - lastAxis.h / 2f;
        mYaxisLine.endX = mYaxisLine.startX;
        mYaxisLine.endY = fistAxis.y - lastAxis.h / 2f;

        System.out.println("Y 轴：" + mYaxisLine);

        mNeedWidth = yItemWidth + mAxisPadding;
        mChildTop = (int) (mYunixHeight + fistAxis.h / 2f);
        mNeedHeight = mTagRect.height() + mChildTop;
        System.out.println("需要 width：" + mNeedWidth);

        for (int i = 0; i < yCount - 1; i++) {
            LineInfo info = new LineInfo();
            info.startX = mYaxisLine.startX;
            AxisInfo axisInfo = mYaxis.get(i);
            info.startY = axisInfo.y - axisInfo.h / 2f;
            info.endY = info.startY;
            info.endX = mCoordinateRect.right;
            mLineInfos.add(info);
        }

        post(new Runnable() {
            @Override
            public void run() {
                //添加 Child View
                LineChartView lineChartView = new LineChartView.Builder(mXaxis)
                        .setAxisColor(mAxisColor)
                        .setAxisPadding(mAxisPadding)
                        .setTextSize(mTagSize)
                        .setUnixText(mXunixInfo)
                        .setXmaxCount(mXmaxCount)
                        .setItemHeight(xItemHeight)
                        .setItemWdith(xItemWidth)
                        .setAxisOffset(lastAxis.h / 2f)
                        .setYdiff(mYmax - mYmin)
                        .setChartDatas(mChartDatas)
                        .build(mContext);
                ((ChartScrollView) getChildAt(0)).setLineChartView(lineChartView);
            }
        });

    }


    /**
     * 计算 X 轴 Item Height
     *
     * @return item height
     */
    private int calculationXitemHeight() {
        int xItemHeight = 0;
        for (AxisInfo axisInfo : mXaxis) {
            Rect rect = acquireTempRect();
            mTextPaint.setTextSize(mTagSize);
            mTextPaint.getTextBounds(axisInfo.value, 0, axisInfo.value.length(), rect);
            axisInfo.h = rect.height();
            axisInfo.w = rect.width();
            xItemHeight = Math.max(rect.height(), xItemHeight);
            releaseTmepRect(rect);
        }
        if (!TextUtils.isEmpty(mXunix)) {
            Rect rect = acquireTempRect();
            mTextPaint.setTextSize(mTagSize);
            mTextPaint.getTextBounds(mXunix, 0, mXunix.length(), rect);
            mXunixInfo = new AxisInfo();
            mXunixInfo.w = rect.width();
            mXunixInfo.h = rect.height();
            mXunixWidth = rect.width();
            xItemHeight = Math.max(rect.height(), xItemHeight);
            releaseTmepRect(rect);
        }
        System.out.println("X 轴 Item Height：" + xItemHeight);

        return xItemHeight;
    }

    /**
     * 计算 Y 轴 Item Wdith
     *
     * @return 返回 item widith
     */
    private int calculationYitemWidth() {
        int yItemWidth = 0;
        for (AxisInfo axisInfo : mYaxis) {
            Rect rect = acquireTempRect();
            mTextPaint.setTextSize(mTagSize);
            mTextPaint.getTextBounds(axisInfo.value, 0, axisInfo.value.length(), rect);
            axisInfo.w = rect.width();
            axisInfo.h = rect.height();
            yItemWidth = Math.max(rect.width(), yItemWidth);
            releaseTmepRect(rect);
        }
        if (!TextUtils.isEmpty(mYunix)) {
            mYunixInfo = new AxisInfo();
            Rect rect = acquireTempRect();
            mTextPaint.setTextSize(mTagSize);
            mTextPaint.getTextBounds(mYunix, 0, mYunix.length(), rect);
            mYunixInfo.w = rect.width();
            mYunixInfo.h = rect.height();
            mYunixHeight = rect.height() + dpTopx(10f);
            System.out.println("Y 轴单位高度：" + mYunixHeight);
            yItemWidth = Math.max(rect.width(), yItemWidth);
            releaseTmepRect(rect);
        }
        System.out.println("Y 轴 Item Width：" + yItemWidth);

        return yItemWidth;
    }

    private void calculationTag() {
        if (!TextUtils.isEmpty(mTag)) {
            Rect rect = acquireTempRect();
            mTextPaint.setTextSize(mTagSize);
            mTextPaint.getTextBounds(mTag, 0, mTag.length(), rect);
            mTagx = mTagRect.centerX() - rect.width() / 2f;
            mTagy = mTagRect.centerY() + rect.height() / 2f;
            mTagCentX = mTagx - rect.width() / 2f;
            mTagCentY = mTagRect.centerY();
            releaseTmepRect(rect);
        }
    }

    static class LineInfo {
        float startX, startY;
        float endX, endY;

        @Override
        public String toString() {
            return String.format("(%f,%f)-(%f,%f)", startX, startY, endX, endY);
        }
    }

    static class AxisInfo {
        float x, y;
        String value;
        int w, h;

        @Override
        public String toString() {
            return String.format("x：%f,y：%f", x, y);
        }
    }

    static class Point {
        float centX, centY;

        @Override
        public String toString() {
            return String.format("centX：%f,centY：%f", centX, centY);
        }
    }

    @NonNull
    private static Rect acquireTempRect() {
        Rect rect = RECT_SIMPLE_POOL.acquire();
        if (rect == null)
            rect = new Rect();
        return rect;
    }

    private static void releaseTmepRect(@NonNull Rect rect) {
        rect.setEmpty();
        RECT_SIMPLE_POOL.release(rect);
    }

    private int dpTopx(float dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics()));
    }

    private int spTopx(float sp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, mContext.getResources().getDisplayMetrics()));
    }
}
