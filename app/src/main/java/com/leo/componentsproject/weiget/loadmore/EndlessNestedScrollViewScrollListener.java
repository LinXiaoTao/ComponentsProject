package com.leo.componentsproject.weiget.loadmore;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created on 2017/5/19 下午4:05.
 * leo linxiaotao1993@vip.qq.com
 */

abstract class EndlessNestedScrollViewScrollListener implements NestedScrollView.OnScrollChangeListener, EndlessViewScrollListener {

    //自动加载更多阈值
    private int mVisibleThreshold;
    //当前页数
    private int mCurrentPager;
    //上一次总数目
    private int mPreviousTotalItemCount;
    //是否正在加载中
    private boolean mLoading;
    //起始页数
    private int mStartPageIndex;
    //当前 RecyclerView
    private final RecyclerView mRecyclerView;
    private final NestedScrollView mNestedScrollView;
    private final RecyclerView.LayoutManager mLayoutManager;


    public EndlessNestedScrollViewScrollListener(RecyclerView recyclerView, NestedScrollView nestedScrollView) {
        mRecyclerView = recyclerView;
        mNestedScrollView = nestedScrollView;

        if (mRecyclerView.getLayoutManager() == null) {
            throw new IllegalArgumentException("请先设置 LayoutManager");
        }
        mLayoutManager = mRecyclerView.getLayoutManager();
        init();
        if (mLayoutManager instanceof GridLayoutManager) {
            mVisibleThreshold = mVisibleThreshold * cast(GridLayoutManager.class, mLayoutManager).getSpanCount();
        } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            mVisibleThreshold = cast(StaggeredGridLayoutManager.class, mLayoutManager).getSpanCount();
        }
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

        int dy = scrollY - oldScrollY;

        int lastVisibleItemPosition = 0;
        int totalItemCount = mLayoutManager.getItemCount();

        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastVisibleItemPositions = cast(StaggeredGridLayoutManager.class, mLayoutManager)
                    .findLastVisibleItemPositions(null);
            lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
        } else if (mLayoutManager instanceof LinearLayoutManager) {
            lastVisibleItemPosition = cast(LinearLayoutManager.class, mLayoutManager).findLastVisibleItemPosition();
        }


        checkTotalCount(totalItemCount);

        //当前不是加载中，且可见 Item 到达阈值，且向下滚动
        if (!mLoading && (lastVisibleItemPosition + mVisibleThreshold) > totalItemCount && dy > 0) {
            mCurrentPager++;
            onLoadMore(mCurrentPager, totalItemCount, mRecyclerView);
            mLoading = true;
        }

    }

    @Override
    public void loadMoreComplete() {
        if (mLoading) {
            mPreviousTotalItemCount = mLayoutManager.getItemCount();
            mLoading = false;

        }
    }

    @Override
    public int getPreviousTotalItemCount() {
        return mPreviousTotalItemCount;
    }

    /**
     * check total count
     *
     * @param totalCount total
     */
    private void checkTotalCount(int totalCount) {
        if (totalCount < mPreviousTotalItemCount) {
            //可能进行了重新刷新数据
            mPreviousTotalItemCount = totalCount;
            mCurrentPager = mStartPageIndex;
            mLoading = false;
        } else if (totalCount > mPreviousTotalItemCount && !mLoading) {
            //第一次
            mPreviousTotalItemCount = totalCount;
        }
    }

    private void init() {

        mVisibleThreshold = 5;
        mCurrentPager = 0;
        mPreviousTotalItemCount = 0;
        mLoading = false;
        mStartPageIndex = 0;
    }

    private <T> T cast(Class<T> clazz, Object object) {
        return clazz.cast(object);
    }

    private int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[0];
            } else {
                maxSize = Math.max(maxSize, lastVisibleItemPositions[i]);
            }
        }

        return maxSize;
    }
}
