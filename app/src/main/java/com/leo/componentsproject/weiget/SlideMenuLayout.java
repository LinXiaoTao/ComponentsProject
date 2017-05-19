package com.leo.componentsproject.weiget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * 侧滑菜单控件
 * 参考
 *
 * @see <a href="https://github.com/mcxtzhang/SwipeDelMenuLayout">SwipeDelMenuLayout</a>
 * Created on 2017/5/12 下午7:32.
 * leo linxiaotao1993@vip.qq.com
 */

public class SlideMenuLayout extends ViewGroup {

    private int mScaleTouchSlop;
    private int mMaxVelocity;

    private PointF mLastP;
    private PointF mFirstP;
    private static boolean isTouching;
    @SuppressLint("StaticFieldLeak")
    private static SlideMenuLayout sViewCache;
    private ValueAnimator mCloseAnim;
    private ValueAnimator mExpandAnim;
    private boolean isUserSwiped;
    private boolean interceptFlag;
    private VelocityTracker mVelocityTracker;
    private int mPointerId;
    private int mLimit;
    private int mHeight;
    private int mRightMenuWidth;
    private View mContentView;

    public SlideMenuLayout(Context context) {
        this(context, null);
    }

    public SlideMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideMenuLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int left = getPaddingLeft();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                if (i == 0) {
                    childView.layout(left, getPaddingTop(), left + childView.getMeasuredWidth(), getPaddingTop() + childView.getMeasuredHeight());
                    left += childView.getMeasuredWidth();
                } else {
                    childView.layout(left, getPaddingTop(), left + childView.getMeasuredWidth(), getPaddingTop() + childView.getMeasuredHeight());
                }
            }
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mRightMenuWidth = 0;
        mHeight = 0;

        int contentWdith = 0;
        int childChount = getChildCount();

        //为了支持 ChildView MATCH_PARENT
        //如果当前 ViewGroup 不是精确值，需要在当前 ViewGroup 测量后，重新测量 ChildView
        final boolean measureMatchParentChildren = MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        boolean isNeedMeasureChildHeight = false;

        for (int i = 0; i < childChount; i++) {
            View childView = getChildAt(i);
            childView.setClickable(true);

            if (childView.getVisibility() != GONE) {
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                final MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
                //获取 ChildView 最大高度
                mHeight = Math.max(mHeight, childView.getMeasuredHeight());
                if (measureMatchParentChildren && layoutParams.height == LayoutParams.MATCH_PARENT) {
                    isNeedMeasureChildHeight = true;
                }
                if (i > 0) {
                    mRightMenuWidth += childView.getMeasuredWidth();
                } else {
                    mContentView = childView;
                    contentWdith = childView.getMeasuredWidth();
                }
            }
        }

        setMeasuredDimension(getPaddingLeft() + getPaddingRight() + contentWdith
                , mHeight + getPaddingTop() + getPaddingBottom());
        mLimit = mRightMenuWidth * 4 / 10;

        if (isNeedMeasureChildHeight) {
            forceUniformHeight(childChount, widthMeasureSpec);
        }

    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        acquireVelocityTracker(ev);
        final VelocityTracker velocityTracker = mVelocityTracker;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                isUserSwiped = false;
                interceptFlag = false;

                //处理多指
                if (isTouching) {
                    return false;
                } else {
                    isTouching = true;
                }

                mLastP.set(ev.getRawX(), ev.getRawY());
                mFirstP.set(ev.getRawX(), ev.getRawY());

                if (sViewCache != null) {
                    if (sViewCache != this) {
                        sViewCache.smoothClose();
                        //已一项打开而且不是当前项
                        //关闭已打开，屏蔽下来所有判断
                        interceptFlag = true;
                    }

                    getParent().requestDisallowInterceptTouchEvent(true);

                }

                mPointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!interceptFlag) {
                    float gap = mLastP.x - ev.getRawX();
                    if (Math.abs(gap) > 10 || Math.abs(getScaleX()) > 10) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    if (Math.abs(gap) > mScaleTouchSlop) {
                        isUserSwiped = true;
                    }
                    int scroll = (int) gap;
                    int scrollX = getScrollX();
                    //边界检查
                    if (scrollX + scroll < 0) {
                        scroll = -scrollX;
                    } else if (scrollX + scroll > mRightMenuWidth) {
                        scroll = mRightMenuWidth - scrollX;
                    }
                    scrollBy(scroll, 0);
                    mLastP.set(ev.getRawX(), ev.getRawY());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (Math.abs(ev.getRawX() - mFirstP.x) > mScaleTouchSlop) {
                    isUserSwiped = true;
                }
                if (!interceptFlag) {
                    velocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                    final float velocityX = velocityTracker.getXVelocity(mPointerId);
                    if (Math.abs(velocityX) > 1000) {
                        if (velocityX < -1000) {
                            smoothExpand();
                        } else if (velocityX > 1000) {
                            smoothClose();
                        }
                    } else {
                        if (Math.abs(getScrollX()) > mLimit) {
                            smoothExpand();
                        } else {
                            smoothClose();
                        }
                    }
                }
                releaseVelocityTracker();
                isTouching = false;
                break;

        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //如果移动了，则拦截子视图的事件
                if (Math.abs(ev.getRawX() - mFirstP.x) > mScaleTouchSlop){
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                //在 onInterceptTouchEvent(MotionEvent) 中的 ACTION_MOVE 中拦截了事件，会将触摸目标置为 NULL
                // ，之后不会再调用 onInterceptTouchEvent
                if (interceptFlag) {
                    smoothClose();
                    return true;
                } else if (getScrollX() > mScaleTouchSlop) {
                    if (ev.getX() < getWidth() - getScrollX()) {
                        smoothClose();
                        return true;
                    }
                }
                if (isUserSwiped) {
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (sViewCache == this) {
            sViewCache.smoothClose();
            sViewCache = null;
        }
    }

    @Override
    public boolean performLongClick() {
        if (Math.abs(getScrollX()) > mScaleTouchSlop)
            return false;
        return super.performLongClick();
    }


    public void smoothClose() {
        sViewCache = null;

        if (mContentView != null)
            mContentView.setLongClickable(false);

        cancelAnim();
        mCloseAnim = ValueAnimator.ofInt(getScrollX(), 0);
        mCloseAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo((Integer) animation.getAnimatedValue(), 0);
            }
        });
        mCloseAnim.setInterpolator(new AccelerateInterpolator());
        mCloseAnim.setDuration(300).start();

    }

    public void smoothExpand() {
        cancelAnim();
        sViewCache = this;
        if (mContentView != null) {
            mContentView.setLongClickable(false);
        }
        mExpandAnim = ValueAnimator.ofInt(getScrollX(), mRightMenuWidth)
                .setDuration(300);
        mExpandAnim.setInterpolator(new OvershootInterpolator());
        mExpandAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo((Integer) animation.getAnimatedValue(), 0);
            }
        });
        mExpandAnim.start();

    }

    private void cancelAnim() {
        if (mCloseAnim != null && mCloseAnim.isRunning()) {
            mCloseAnim.cancel();

        }
        if (mExpandAnim != null && mExpandAnim.isRunning()) {
            mExpandAnim.cancel();
        }
    }

    private void acquireVelocityTracker(MotionEvent motionEvent) {
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(motionEvent);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void init() {
        mScaleTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mMaxVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        mLastP = new PointF();
        mFirstP = new PointF();
        setClickable(true);
    }

    private void forceUniformHeight(int childCount, int widthMeasureSpec) {
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
                if (layoutParams.height == LayoutParams.MATCH_PARENT) {
                    int oldWdith = layoutParams.width;
                    layoutParams.width = child.getMeasuredWidth();
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    layoutParams.width = oldWdith;
                }
            }
        }
    }
}
