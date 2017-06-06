package com.leo.componentsproject.weiget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.LinearInterpolator;

/**
 * 楼宇管理
 * Created on 2017/6/5 下午3:52.
 * leo linxiaotao1993@vip.qq.com
 */

public final class BuidingManageDrawable extends Drawable {

    @ColorInt
    private int mFirstColor;
    private float mFirst;
    @ColorInt
    private int mSecondColor;
    private float mSecond;
    @ColorInt
    private int mThirdColor;
    private float mThird;
    @ColorInt
    private int mBackColor;
    private int mDuration;

    private RectF mFirstRectF;
    private RectF mSecondRectF;
    private RectF mThirdRectF;

    private int mCurrentState = STATUE_NONE;
    private long mStartTimeMillis;
    private LinearInterpolator mLinearInterpolator;
    private float mFirstToValue;
    private float mSecondToValue;
    private float mThirdToValue;
    private float mFirstValue;
    private float mSecondValue;
    private float mThirdValue;
    private float mFirstRadiu;
    private float mSecondRadiu;
    private float mThirdRadiu;
    private Rect mRect;
    private float mWidthProportion = 0.1f;
    private float mPaddingProportion = 0.23f;
    private Context mContext;
    private Paint mDrawPaint;

    private static final int STATUE_NONE = 0;
    private static final int STATUE_START = 1;
    private static final int STATUE_RUN = 2;

    public BuidingManageDrawable(Context context) {
        mContext = context;
        init();
    }


    public BuidingManageDrawable setDuration(int duration) {
        mDuration = duration;
        return this;
    }

    public BuidingManageDrawable setFirstColor(int firstColor) {
        mFirstColor = firstColor;
        return this;
    }

    public BuidingManageDrawable setFirst(float first) {
        if (mFirst != first) {
            mFirst = first;
            mFirstToValue = 360 * mFirst;
            mCurrentState = STATUE_START;
        }
        return this;
    }

    public BuidingManageDrawable setSecondColor(int secondColor) {
        mSecondColor = secondColor;
        return this;
    }

    public BuidingManageDrawable setSecond(float second) {
        if (mSecond != second) {
            mSecond = second;
            mSecondToValue = 360 * mSecond;
            mCurrentState = STATUE_START;
        }
        return this;
    }

    public BuidingManageDrawable setThirdColor(int thirdColor) {
        mThirdColor = thirdColor;
        return this;
    }

    public BuidingManageDrawable setThird(float third) {
        if (mThird != third) {
            mThird = third;
            mThirdToValue = 360 * mThird;
            mCurrentState = STATUE_START;
        }
        return this;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        calculation();
        mDrawPaint.setColor(mBackColor);
        canvas.drawCircle(mRect.centerX(), mRect.centerY(), mFirstRadiu, mDrawPaint);
        canvas.drawCircle(mRect.centerX(), mRect.centerY(), mSecondRadiu, mDrawPaint);
        canvas.drawCircle(mRect.centerX(), mRect.centerY(), mThirdRadiu, mDrawPaint);

        mDrawPaint.setColor(mFirstColor);
        canvas.drawArc(mFirstRectF, -90, mFirstValue, false, mDrawPaint);
        mDrawPaint.setColor(mSecondColor);
        canvas.drawArc(mSecondRectF, -90, mSecondValue, false, mDrawPaint);
        mDrawPaint.setColor(mThirdColor);
        canvas.drawArc(mThirdRectF, -90, mThirdValue, false, mDrawPaint);

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

    private void init() {

        mLinearInterpolator = new LinearInterpolator();
        mDuration = 400;

        mDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mFirstColor = Color.parseColor("#f96868");
        mSecondColor = Color.parseColor("#1bcba0");
        mThirdColor = Color.parseColor("#62a8ea");
        mBackColor = Color.LTGRAY;
        mFirstRectF = new RectF();
        mSecondRectF = new RectF();
        mThirdRectF = new RectF();
    }


    private void calculation() {
        mRect = getBounds();

        float arcWidth = mRect.width() * mWidthProportion;
        mDrawPaint.setStrokeWidth(arcWidth);
        float mainPadding = mRect.width() * mPaddingProportion / 2f;
        float widthPadding = (mRect.width() / 2f - mainPadding - 3 * arcWidth) / 2f;


        mFirstRadiu = mainPadding + arcWidth / 2f;
        mSecondRadiu = mFirstRadiu + widthPadding + arcWidth;
        mThirdRadiu = mRect.width() / 2f - arcWidth / 2f;


        mFirstRectF.set(mRect.centerX() - mFirstRadiu, mRect.centerY() - mFirstRadiu
                , mRect.centerX() + mFirstRadiu, mRect.centerY() + mFirstRadiu);

        mSecondRectF.set(mRect.centerX() - mSecondRadiu, mRect.centerY() - mSecondRadiu
                , mRect.centerX() + mSecondRadiu, mRect.centerY() + mSecondRadiu);

        mThirdRectF.set(mRect.centerX() - mThirdRadiu, mRect.centerY() - mThirdRadiu
                , mRect.centerX() + mThirdRadiu, mRect.centerY() + mThirdRadiu);


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
                    if (mFirstValue != mFirstToValue) {
                        mFirstValue = mLinearInterpolator.getInterpolation(normalized) * mFirstToValue;
                    }
                    if (mSecondValue != mSecondToValue) {
                        mSecondValue = mLinearInterpolator.getInterpolation(normalized) * mSecondToValue;
                    }
                    if (mThirdValue != mThirdToValue) {
                        mThirdValue = mLinearInterpolator.getInterpolation(normalized) * mThirdToValue;
                    }
                }
                break;
        }

        if (!done) {
            invalidateSelf();
        } else {
            mCurrentState = STATUE_NONE;
        }


    }


}
