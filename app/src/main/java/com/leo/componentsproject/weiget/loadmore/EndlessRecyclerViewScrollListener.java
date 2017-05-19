package com.leo.componentsproject.weiget.loadmore;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created on 2017/5/19 上午9:36.
 * leo linxiaotao1993@vip.qq.com
 */

abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener implements EndlessViewScrollListener {

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
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean mDelay = false;


    EndlessRecyclerViewScrollListener(RecyclerView.LayoutManager layoutManager) {
        mLayoutManager = layoutManager;
        init();
        if (layoutManager instanceof GridLayoutManager) {
            mVisibleThreshold = mVisibleThreshold * cast(GridLayoutManager.class, layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            mVisibleThreshold = cast(StaggeredGridLayoutManager.class, layoutManager).getSpanCount();
        }
    }


    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE && mDelay) {
            //延迟加载更多
            mDelay = false;
            mLoading = true;
            onLoadMore(mCurrentPager, mLayoutManager.getItemCount(), mRecyclerView);
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

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
            mRecyclerView = recyclerView;
            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                onLoadMore(mCurrentPager, totalItemCount, recyclerView);
                mLoading = true;
                mDelay = false;
            } else {
                mDelay = true;
            }
        }


    }

    @Override
    public int getPreviousTotalItemCount() {
        return mPreviousTotalItemCount;
    }

    //加载完成
    @Override
    public void loadMoreComplete() {
        if (mLoading) {
            mPreviousTotalItemCount = mLayoutManager.getItemCount();
            mLoading = false;
            mDelay = false;

        }
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
            mDelay = false;
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
        mDelay = false;
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

