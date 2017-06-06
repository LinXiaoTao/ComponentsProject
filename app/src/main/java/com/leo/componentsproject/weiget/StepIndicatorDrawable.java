package com.leo.componentsproject.weiget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 步骤指示器
 * Created on 2017/5/15 上午11:20.
 * leo linxiaotao1993@vip.qq.com
 */
@SuppressWarnings("unused")
public class StepIndicatorDrawable extends Drawable {

    private final List<StepItem> mStepItems;
    private final Context mContext;
    @ColorInt
    private int mUndoneTextColor;
    @ColorInt
    private int mDoneTextColor;
    @ColorInt
    private int mUndoneGraphColor;
    @ColorInt
    private int[] mDoneGraphColors;
    @ColorInt
    private int mSeparatorColor;
    private int mSeparatorWidth;
    private int mGraphPadding;
    private int mTextPadding;
    private float mUndoneGraphRadius;
    private float mDoneGraphRadius;
    private float mTextSize;
    private int mDoneIndex = -1;
    private int mStepLineWidth;
    @ColorInt
    private int mStepLineColor;

    private int mIntrinsicWidth = -1;
    private int mIntrinsicHeight = -1;
    private float mLineStartX, mLineStartY, mLineStopX, mLineStopY;
    private static final Pools.SimplePool<Rect> RECT_SIMPLE_POOL = new Pools.SimplePool<>(12);
    private Rect mGroupRect;
    private Rect mItemRect;
    private Paint mTextPaint;
    private Paint mGraphPaint;
    private List<DrawItem> mDrawItems;


    public StepIndicatorDrawable(@NonNull Context context, @NonNull List<StepItem> stepItems, int doneIndex) {
        mContext = context;
        mDoneIndex = doneIndex;
        mStepItems = stepItems;
        init();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        calculate();
        if (!isEmpty(mDrawItems)) {

            //画连线
            mGraphPaint.setColor(mStepLineColor);
            canvas.drawLine(mLineStartX, mLineStartY, mLineStopX, mLineStopY, mGraphPaint);

            for (DrawItem drawItem : mDrawItems) {
                drawItem.draw(canvas);
            }
        }

    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public StepIndicatorDrawable setUndoneTextColor(int undoneTextColor) {
        mUndoneTextColor = undoneTextColor;
        return this;
    }

    public StepIndicatorDrawable setDoneTextColor(int doneTextColor) {
        mDoneTextColor = doneTextColor;
        return this;
    }

    public StepIndicatorDrawable setUndoneGraphColor(int undoneGraphColor) {
        mUndoneGraphColor = undoneGraphColor;
        return this;
    }

    public StepIndicatorDrawable setDoneGraphColors(int[] doneGraphColors) {
        if (doneGraphColors == null || doneGraphColors.length < 2)
            throw new IllegalArgumentException("颜色数组不能为null，且大小必须大于1");
        mDoneGraphColors = doneGraphColors;
        return this;
    }

    public StepIndicatorDrawable setSeparatorColor(int separatorColor) {
        mSeparatorColor = separatorColor;
        return this;
    }

    public StepIndicatorDrawable setSeparatorWidth(int separatorWidth) {
        mSeparatorWidth = separatorWidth;
        calculateAdaptation();
        return this;
    }

    public StepIndicatorDrawable setGraphPadding(int graphPadding) {
        mGraphPadding = graphPadding;
        calculateAdaptation();
        return this;
    }

    public StepIndicatorDrawable setTextPadding(int textPadding) {
        mTextPadding = textPadding;
        calculateAdaptation();
        return this;
    }

    public StepIndicatorDrawable setUndoneGraphRadius(float undoneGraphRadius) {
        mUndoneGraphRadius = undoneGraphRadius;
        calculateAdaptation();
        return this;
    }

    public StepIndicatorDrawable setDoneGraphRadius(float doneGraphRadius) {
        mDoneGraphRadius = doneGraphRadius;
        calculateAdaptation();
        return this;
    }

    public StepIndicatorDrawable setTextSize(float textSize) {
        mTextSize = textSize;
        calculateAdaptation();
        return this;
    }

    public StepIndicatorDrawable setDoneIndex(int doneIndex) {
        mDoneIndex = doneIndex;
        return this;
    }

    public StepIndicatorDrawable setStepLineWidth(int stepLineWidth) {
        mStepLineWidth = stepLineWidth;
        calculateAdaptation();
        return this;
    }

    public StepIndicatorDrawable setStepLineColor(int stepLineColor) {
        mStepLineColor = stepLineColor;
        return this;
    }

    private void calculate() {

        if (!isEmpty(mDrawItems)) {
            for (DrawItem item : mDrawItems) {
                releaseTmepRect(item.drawRect);
            }
            mDrawItems.clear();
        }

        if (!isEmpty(mStepItems)) {

            if (mDrawItems == null)
                mDrawItems = new ArrayList<>();

            mGroupRect = getBounds();

            System.out.println("获得的 Bounds：" + mGroupRect.toString());

            int itemCount = mStepItems.size();
            int itemHeight = mGroupRect.height() / itemCount;
            for (int i = 0; i < itemCount; i++) {
                StepItem stepItem = mStepItems.get(i);
                Rect itemRect = acquireTempRect();
                itemRect.set(mGroupRect);

                itemRect.top = i * itemHeight + mGroupRect.top;
                itemRect.bottom = itemRect.top + itemHeight;

                DrawItem drawItem;
                if (mDoneIndex == i) {
                    drawItem = new DrawItem(itemRect, stepItem, true);
                } else {
                    drawItem = new DrawItem(itemRect, stepItem, false);
                }
                mDrawItems.add(drawItem);
            }

            //计算连线
            mLineStartX = mGroupRect.left + mDoneGraphRadius;
            mLineStopX = mLineStartX;
            mLineStartY = mGroupRect.top + itemHeight / 2 + mDoneGraphRadius + dpTopx(2f);
            mLineStopY = mGroupRect.bottom;
        }
    }

    private void init() {

        mGraphPadding = dpTopx(10f);
        mTextPadding = dpTopx(5f);
        mUndoneGraphRadius = dpTopx(5f);
        mDoneGraphRadius = dpTopx(10f);
        mTextSize = spTopx(12f);
        mSeparatorWidth = dpTopx(1f);
        mStepLineWidth = dpTopx(2f);

        mSeparatorColor = Color.LTGRAY;
        mDoneGraphColors = new int[]{Color.WHITE, Color.CYAN};
        mUndoneGraphColor = Color.GRAY;
        mDoneTextColor = Color.CYAN;
        mUndoneTextColor = Color.LTGRAY;
        mStepLineColor = Color.GRAY;

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mUndoneTextColor);
        mTextPaint.setTextSize(mTextSize);

        mGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGraphPaint.setColor(mUndoneGraphColor);

        calculateAdaptation();
    }

    /** 计算自适应 */
    private void calculateAdaptation() {
        if (!isEmpty(mStepItems)) {
            int itemPadding = dpTopx(10f);
            mIntrinsicWidth = 0;
            mIntrinsicHeight = 0;

            int maxItemWidth = 0;
            for (StepItem stepItem : mStepItems) {
                int maxTextWidth = 0;
                int itemHeight = 0;
                int itemWidth = 0;
                for (String text : stepItem.texts) {
                    //文字
                    Rect rect = acquireTempRect();
                    mTextPaint.getTextBounds(text, 0, text.length(), rect);
                    if (rect.width() > maxTextWidth) {
                        maxTextWidth = rect.width();
                    }
                    itemHeight = itemHeight + rect.height() + mTextPadding;
                    releaseTmepRect(rect);
                }

                itemWidth += maxTextWidth + mGraphPadding + mDoneGraphRadius * 2f;

                itemHeight += mSeparatorWidth + itemPadding;
                mIntrinsicHeight += itemHeight;
                if (itemWidth > maxItemWidth) {
                    maxItemWidth = itemWidth;
                }
            }
            mIntrinsicWidth = maxItemWidth;

            System.out.println("计算自适应结果：(" + mIntrinsicWidth + "," + mIntrinsicHeight + ")");

        }
    }

    private boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
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

    public static class StepItem {
        private final List<String> texts;

        public StepItem(List<String> texts) {
            this.texts = texts;
        }
    }

    private class DrawItem {
        private final Rect drawRect;
        private final StepItem texts;
        private final boolean done;
        private float cx, cy;
        private float x, y;
        private float startX, startY, stopX, stopY;
        private int textHeight;

        DrawItem(Rect drawRect, StepItem texts, boolean done) {
            this.drawRect = drawRect;
            this.texts = texts;
            this.done = done;
            calculate();
        }

        void draw(Canvas canvas) {

            //画圆

            if (done) {
                mGraphPaint.setColor(mDoneGraphColors[1]);
                canvas.drawCircle(cx, cy, mDoneGraphRadius, mGraphPaint);
            }

            if (done) {
                mGraphPaint.setColor(mDoneGraphColors[0]);
            } else {
                mGraphPaint.setColor(mUndoneGraphColor);
            }
            canvas.drawCircle(cx, cy, mUndoneGraphRadius, mGraphPaint);

            //画文字
            if (done) {
                mTextPaint.setColor(mDoneTextColor);
            } else {
                mTextPaint.setColor(mUndoneTextColor);
            }
            for (int i = 0; i < texts.texts.size(); i++) {
                String text = texts.texts.get(i);
                canvas.drawText(text, x, (textHeight + mTextPadding) * i + y, mTextPaint);
            }

            //画分隔符
            mGraphPaint.setColor(mSeparatorColor);
            canvas.drawLine(startX, startY, stopX, stopY, mGraphPaint);
        }

        private void calculate() {
            cx = drawRect.left + mDoneGraphRadius;
            cy = drawRect.centerY();

            float textLeft = mDoneGraphRadius * 2 + mGraphPadding;
            x = drawRect.left + textLeft;

            textHeight = 0;
            int textSize = texts.texts.size();
            for (String text : texts.texts) {
                Rect rect = acquireTempRect();
                mTextPaint.getTextBounds(text, 0, text.length(), rect);
                textHeight = Math.max(textHeight, rect.height());
                releaseTmepRect(rect);
            }
            //计算文字垂直居中
            int useheight = (textSize * textHeight) + ((textSize - 1) * mTextPadding);
            int paddingTop = (drawRect.height() - useheight) / 2;
            y = drawRect.top + paddingTop + textHeight;
            System.out.println("当前文字的最大高度为：" + textHeight);

            startX = x;
            startY = drawRect.bottom - mSeparatorWidth;
            stopX = drawRect.right;
            stopY = drawRect.bottom;

            System.out.println("Item 圆：(" + cx + "," + cy + ")");
            System.out.println("Item 文字：(" + x + "," + y + ")");
            System.out.println("Item 分隔符：(" + startX + "," + startY + "),(" + stopX + "," + stopY + ")");

        }
    }
}
