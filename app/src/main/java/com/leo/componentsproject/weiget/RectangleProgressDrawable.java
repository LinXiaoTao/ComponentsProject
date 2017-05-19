package com.leo.componentsproject.weiget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.animation.OvershootInterpolator;

import com.leo.componentsproject.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * 矩形值进度条
 * Created on 2017/5/13 下午1:24.
 * leo linxiaotao1993@vip.qq.com
 */
@SuppressWarnings("unused")
public class RectangleProgressDrawable extends Drawable {

    private int mBackColor;
    private int mProgressColor;
    private float mMaxValue;
    private float mCurrentValue;
    private String mTitle;
    private int mTitleColor;
    private String mValueUnit;
    private int mUnitColor;
    private float mTextPadding;
    private int mTitleSize;
    private int mUnixTextSize;
    private int mMinTextSize;
    private int mDuration;

    private OvershootInterpolator mOvershootInterpolator;
    private int mCurrentState = STATUE_NONE;
    private long mStartTimeMillis;
    private float mFrom;
    private float mTo;
    private String mBottomText;
    private final Context mContext;
    private static final Pools.SimplePool<Rect> RECT_SIMPLE_POOL = new Pools.SimplePool<>(12);
    private float mTitleHeight;
    private float mBottomHeight;
    private Paint mBackPaint;
    private Paint mProgressPaint;
    private Paint mTextPaint;
    private Rect mBackRect;
    private Rect mProgressRect;


    private static final int STATUE_NONE = 0;
    private static final int STATUE_START = 1;
    private static final int STATUE_RUN = 2;

    public RectangleProgressDrawable(@NonNull Context context) {
        mContext = context;
        init();
    }

    @Override
    public void inflate(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Resources.Theme theme)
            throws XmlPullParserException, IOException {
        super.inflate(r, parser, attrs, theme);
        final TypedArray a = r.obtainAttributes(attrs, R.styleable.RectangleProgressDrawable);
        updateStateFromTypedArray(a);
        a.recycle();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        mBottomText = mCurrentValue + mValueUnit;
        calculate();

        //绘制背景
        canvas.drawRect(mBackRect, mBackPaint);
        canvas.drawRect(mProgressRect, mProgressPaint);

        //绘制标题
        if (!TextUtils.isEmpty(mTitle)) {
            mTextPaint.setTextSize(mTitleSize);
            mTextPaint.setColor(mTitleColor);
            canvas.drawText(mTitle, 0, mBackRect.centerY() + mTitleHeight / 2f, mTextPaint);
        }

        //绘制单位
        if (!TextUtils.isEmpty(mBottomText)) {
            mTextPaint.setTextSize(mUnixTextSize);
            mTextPaint.setColor(mUnitColor);
            canvas.drawText(mBottomText, mProgressRect.right + mTextPadding, mProgressRect.centerY() + mBottomHeight / 2f, mTextPaint);
        }


    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


    public void setBackColor(int backColor) {
        mBackColor = backColor;
        mBackPaint.setColor(mBackColor);
        invalidateSelf();
    }

    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
        mProgressPaint.setColor(mProgressColor);
        invalidateSelf();
    }

    public void setMaxValue(float maxValue) {
        mMaxValue = maxValue;
        invalidateSelf();
    }

    public void setCurrentValue(float currentValue) {
        mCurrentValue = currentValue;
        invalidateSelf();
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }


    public void startCurrentValue(float currentValue) {
        mFrom = 0f;
        mTo = currentValue;
        mCurrentValue = mFrom;
        mCurrentState = STATUE_START;
        invalidateSelf();
    }

    public void setTitle(String title) {
        mTitle = title;
        invalidateSelf();
    }

    public void setValueUnit(String valueUnit) {
        mValueUnit = valueUnit;
        invalidateSelf();
    }

    public void setTitleColor(int titleColor) {
        mTitleColor = titleColor;
        mTextPaint.setColor(mTitleColor);
        invalidateSelf();
    }

    public void setUnitColor(int unitColor) {
        mUnitColor = unitColor;
        mTextPaint.setColor(mUnitColor);
        invalidateSelf();
    }

    public void setTextPadding(float textPadding) {
        mTextPadding = textPadding;
        invalidateSelf();
    }

    public void setTitleSize(int titleSize) {
        mTitleSize = titleSize;
        invalidateSelf();
    }

    public void setUnixTextSize(int unixTextSize) {
        mUnixTextSize = unixTextSize;
        invalidateSelf();
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

    private void calculate() {


        Rect bounds = getBounds();

        Rect textBounds = acquireTempRect();
        mTextPaint.setTextSize(mTitleSize);
        mTextPaint.getTextBounds(mTitle, 0, mTitle.length(), textBounds);
        float textWidth = textBounds.width();
        mTitleHeight = textBounds.height();
        releaseTmepRect(textBounds);

        textBounds = acquireTempRect();
        mTextPaint.setTextSize(mUnixTextSize);
        mTextPaint.getTextBounds(mBottomText, 0, mBottomText.length(), textBounds);
        mBottomHeight = textBounds.height();
        releaseTmepRect(textBounds);


        boolean done = true;
        switch (mCurrentState) {
            case STATUE_START:
                mStartTimeMillis = SystemClock.uptimeMillis();
                mCurrentState = STATUE_RUN;
                done = false;
                break;
            case STATUE_RUN:
                if (mStartTimeMillis > 0) {
                    float normalized = (float) (SystemClock.uptimeMillis() - mStartTimeMillis) / mDuration;
                    done = normalized >= 1.0f;
                    normalized = Math.min(normalized, 1.0f);
                    mCurrentValue = mOvershootInterpolator.getInterpolation(normalized) * mTo;
                }
                break;
        }

        if (mTextPadding == 0)
            mTextPadding = textWidth;
        mBackRect.set((int) (bounds.left + textWidth + mTextPadding), bounds.top, bounds.right, bounds.bottom);
        int right = (int) (mBackRect.width() * (mCurrentValue / mMaxValue) + mBackRect.left);
        mProgressRect.set(mBackRect.left, mBackRect.top, right, mBackRect.bottom);

        if (!done) {
            invalidateSelf();
        }
//        System.out.println("backrect：" + mBackRect.toString());
//        System.out.println("progressrect：" + mProgressRect.toString());

//        int canUseWidth = (int) (mBackRect.width() - mProgressRect.width() - mTextPadding);
//        resetTextSize(canUseWidth, mTextPaint);


    }


    private void init() {
        mOvershootInterpolator = new OvershootInterpolator();
        mDuration = 900;
        mMaxValue = 1000f;
        mCurrentValue = 500f;
        mTitle = "标题";
        mBottomText = String.valueOf(mCurrentValue);
        mBackColor = Color.LTGRAY;
        mProgressColor = Color.CYAN;
        mTitleColor = Color.GRAY;
        mUnitColor = Color.GRAY;
        mBackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackPaint.setColor(mBackColor);
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mProgressColor);
        mBackRect = new Rect();
        mProgressRect = new Rect();
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinTextSize = spTopx(10f);
        mTitleSize = spTopx(14f);
        mUnixTextSize = spTopx(14f);
    }

    private void resetTextSize(int width, Paint paint) {
        float textWidth = paint.measureText(mBottomText);
        boolean ok = textWidth <= width;
        while (!ok) {
            mUnixTextSize -= 2;
            textWidth = paint.measureText(mBottomText);
            ok = textWidth <= width || mUnixTextSize <= mMinTextSize;
        }
    }

    private void updateStateFromTypedArray(TypedArray a) {
        mMaxValue = a.getFloat(R.styleable.RectangleProgressDrawable_maxvalue, mMaxValue);
        mCurrentValue = a.getFloat(R.styleable.RectangleProgressDrawable_currentvalue, mCurrentValue);
        mBackColor = a.getColor(R.styleable.RectangleProgressDrawable_backcolor, mBackColor);
        mProgressColor = a.getColor(R.styleable.RectangleProgressDrawable_valuecolor, mProgressColor);
        mTitle = a.getString(R.styleable.RectangleProgressDrawable_title);
        mValueUnit = a.getString(R.styleable.RectangleProgressDrawable_valueunit);
        mUnitColor = a.getColor(R.styleable.RectangleProgressDrawable_unitcolor, mUnitColor);
        mTitleColor = a.getColor(R.styleable.RectangleProgressDrawable_titlecolor, mTitleColor);
        mTitleSize = a.getDimensionPixelSize(R.styleable.RectangleProgressDrawable_titlesize, mTitleSize);
        mUnixTextSize = a.getDimensionPixelSize(R.styleable.RectangleProgressDrawable_unixsize, mUnixTextSize);
        mDuration = a.getInt(R.styleable.RectangleProgressDrawable_duration, mDuration);
    }

    private int dpTopx(float dp){
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,mContext.getResources().getDisplayMetrics()));
    }

    private int spTopx(float sp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, mContext.getResources().getDisplayMetrics()));
    }
}
