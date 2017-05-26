package com.leo.componentsproject.weiget.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2017/5/26 上午9:00.
 * leo linxiaotao1993@vip.qq.com
 */

public class LineChartView extends View {

    private List<ChartLayout.AxisInfo> mAxisInfos;
    private List<ChartData> mChartDatas;
    @ColorInt
    private int mAxisColor;
    private Paint mTextPaint;
    private Paint mChartPaint;
    private Paint mPathPaint;
    private int mAxisPadding;
    private Context mContext;
    private static final Pools.SimplePool<Rect> RECT_SIMPLE_POOL = new Pools.SimplePool<>(12);
    private Rect mChartRect;
    private Rect mCoordinateRect;
    private ChartLayout.AxisInfo mUnixText;
    private int mTextSize;
    private int mXmaxCount;
    private int mItemHeight;
    private int mItemWdith;
    private ChartLayout.LineInfo mLineInfo;
    private float mAxisOffset;
    private float mInternalRadius, mExternalRadius;
    private List<ChartLayout.Point> mPoints;
    private int mYdiff;
    private Path mPath;

    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAxisInfos != null && !mAxisInfos.isEmpty()) {
            int needWidth = mItemWdith * mAxisInfos.size();
            System.out.println("X 轴需要宽度：" + needWidth);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(needWidth, MeasureSpec.EXACTLY);
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        System.out.println("宽，高：" + w + "," + h);
        mChartRect.set(0, 0, w, h);

        calculationAxis();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        mChartPaint.setColor(Color.LTGRAY);
//        canvas.drawRect(mCoordinateRect, mChartPaint);

        drawLine(canvas);
        drawAxis(canvas);
        drawPoints(canvas);

    }

    private void drawPoints(Canvas canvas) {
        if (!mPoints.isEmpty()) {

            //连线
            mPath.reset();
            mPath.moveTo(mPoints.get(0).centX, mPoints.get(0).centY);
            for (int i = 1; i < mPoints.size(); i++) {
                mPath.lineTo(mPoints.get(i).centX, mPoints.get(i).centY);
            }
            canvas.drawPath(mPath, mPathPaint);


            mChartPaint.setColor(Color.WHITE);
            for (ChartLayout.Point point : mPoints) {
                canvas.drawCircle(point.centX, point.centY, mExternalRadius, mChartPaint);
            }

            mChartPaint.setColor(Color.RED);
            for (ChartLayout.Point point : mPoints) {
                canvas.drawCircle(point.centX, point.centY, mInternalRadius, mChartPaint);
            }





        }
    }

    private void drawLine(Canvas canvas) {
        mChartPaint.setColor(mAxisColor);
        canvas.drawLine(mLineInfo.startX, mLineInfo.startY, mLineInfo.endX, mLineInfo.endY, mChartPaint);
    }


    private void drawAxis(Canvas canvas) {
        if (mAxisInfos != null && !mAxisInfos.isEmpty()) {
            mTextPaint.setTextSize(mTextSize);
            mTextPaint.setColor(mAxisColor);
            for (ChartLayout.AxisInfo axisInfo : mAxisInfos) {
                canvas.drawText(axisInfo.value, axisInfo.x, axisInfo.y, mTextPaint);
            }
        }
    }

    private void init() {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mChartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (mAxisColor == 0) {
            mAxisColor = Color.DKGRAY;
        }
        if (mAxisPadding == 0) {
            mAxisPadding = dpTopx(10f);
        }
        if (mTextSize == 0) {
            mTextSize = spTopx(10f);
        }
        if (mXmaxCount == 0) {
            mXmaxCount = 13;
        }
        mChartRect = new Rect();
        mCoordinateRect = new Rect();
        mLineInfo = new ChartLayout.LineInfo();
        mExternalRadius = dpTopx(7f);
        mInternalRadius = dpTopx(4f);
        mPoints = new ArrayList<>();
        mPath = new Path();
        mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeWidth(dpTopx(3f));
        mPathPaint.setColor(Color.RED);
    }


    private void calculationAxis() {
        if (mAxisInfos != null && !mAxisInfos.isEmpty()) {
            for (int i = 0; i < mAxisInfos.size(); i++) {
                ChartLayout.AxisInfo axisInfo = mAxisInfos.get(i);
                axisInfo.x = mChartRect.left + i * mItemWdith;
                axisInfo.y = mChartRect.bottom;

            }
            System.out.println("X 轴：" + mAxisInfos);

            mLineInfo.startX = mChartRect.left;
            mLineInfo.startY = mChartRect.bottom - mItemHeight - mAxisOffset;
            mLineInfo.endX = mChartRect.right;
            mLineInfo.endY = mLineInfo.startY;

            mCoordinateRect.set(mChartRect);
            mCoordinateRect.bottom = (int) mLineInfo.startY;


        }

        if (mChartDatas != null && !mChartDatas.isEmpty()) {
            for (ChartData chartData : mChartDatas) {
                ChartLayout.Point point = new ChartLayout.Point();
                point.centX = getPointCentX(chartData.getX());
                point.centY = getPointCentY(chartData.getY());
                mPoints.add(point);
            }

            System.out.println("数据位置：" + mPoints);
        }


    }

    private float getPointCentY(int y) {
        return mCoordinateRect.bottom - (y * 1.0f / mYdiff * mCoordinateRect.height());
    }

    private float getPointCentX(int x) {
        for (ChartLayout.AxisInfo axisInfo : mAxisInfos) {
            if (axisInfo.value.equals(String.valueOf(x))) {
                return axisInfo.x + axisInfo.w / 2f;
            }
        }
        return 0f;
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

    static class Builder {
        private final List<ChartLayout.AxisInfo> mAxisInfos;
        private int mAxisColor;
        private int mAxisPadding;
        private ChartLayout.AxisInfo mUnixText;
        private int mTextSize;
        private int mXmaxCount;
        private int mItemWdith;
        private float mAxisOffset;
        private int mItemHeight;
        private int mYdiff;
        private List<ChartData> mChartDatas;

        Builder(List<ChartLayout.AxisInfo> axisInfos) {
            mAxisInfos = axisInfos;
        }

        Builder setAxisColor(int axisColor) {
            mAxisColor = axisColor;
            return this;
        }

        Builder setAxisPadding(int axisPadding) {
            mAxisPadding = axisPadding;
            return this;
        }

        Builder setUnixText(ChartLayout.AxisInfo unixText) {
            mUnixText = unixText;
            return this;
        }

        Builder setTextSize(int textSize) {
            mTextSize = textSize;
            return this;
        }

        Builder setXmaxCount(int xmaxCount) {
            mXmaxCount = xmaxCount;
            return this;
        }

        Builder setItemWdith(int itemWdith) {
            mItemWdith = itemWdith;
            return this;
        }

        Builder setAxisOffset(float axisOffset) {
            mAxisOffset = axisOffset;
            return this;
        }

        Builder setItemHeight(int itemHeight) {
            mItemHeight = itemHeight;
            return this;
        }

        Builder setYdiff(int ydiff) {
            mYdiff = ydiff;
            return this;
        }

        public Builder setChartDatas(List<ChartData> chartDatas) {
            mChartDatas = chartDatas;
            return this;
        }

        LineChartView build(@NonNull Context context) {
            LineChartView lineChartView = new LineChartView(context);
            lineChartView.mAxisInfos = mAxisInfos;
            lineChartView.mAxisColor = mAxisColor;
            lineChartView.mAxisPadding = mAxisPadding;
            lineChartView.mUnixText = mUnixText;
            lineChartView.mTextSize = mTextSize;
            lineChartView.mXmaxCount = mXmaxCount;
            lineChartView.mItemWdith = mItemWdith;
            lineChartView.mAxisOffset = mAxisOffset;
            lineChartView.mItemHeight = mItemHeight;
            lineChartView.mYdiff = mYdiff;
            lineChartView.mChartDatas = mChartDatas;
            return lineChartView;
        }
    }

}
