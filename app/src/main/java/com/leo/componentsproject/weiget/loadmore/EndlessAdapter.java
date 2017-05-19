package com.leo.componentsproject.weiget.loadmore;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 加载更多 Adapter
 * Created on 2017/5/19 上午10:24.
 * leo linxiaotao1993@vip.qq.com
 */

public abstract class EndlessAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter {

    //加载更多
    private static final int VIEW_TYPE_LOADING = 100;
    private static final int VIEW_ID_LOADING = 100;

    private final LoadMoreListener mLoadMoreListener;
    private EndlessViewScrollListener mEndlessScrollListener;


    public EndlessAdapter(@NonNull RecyclerView recyclerView, @NonNull LoadMoreListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
        initRecyclerView(recyclerView);
        setHasStableIds(true);
    }

    public EndlessAdapter(@NonNull NestedScrollView nestedScrollView, @NonNull RecyclerView recyclerView, @NonNull LoadMoreListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
        initNestedScrollView(nestedScrollView, recyclerView);
        setHasStableIds(true);
    }


    @Override
    public int getItemCount() {
        return getActualItemCount() + 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOADING) {
            //加载更多视图
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            LinearLayout linearLayout = new LinearLayout(parent.getContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setVisibility(View.GONE);
            int padding = dpTopx(10f, parent.getContext());
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setPadding(padding, padding, padding, padding);

            ProgressBar progressBar = new ProgressBar(parent.getContext());
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            linearLayout.addView(progressBar, layoutParams);

            TextView textView = new TextView(parent.getContext());
            textView.setText("加载中...");
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ((LinearLayout.LayoutParams) layoutParams).leftMargin = padding;
            textView.setGravity(Gravity.CENTER);
            linearLayout.addView(textView, layoutParams);


            return new EndlessViewHolder(linearLayout);
        }
        return onActualCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof EndlessViewHolder)) {
            onActualBindViewHolder((VH) holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == (getItemCount() - 1)) {
            return VIEW_TYPE_LOADING;
        }
        int actual = getActualItemViewType(position);
        if (actual == VIEW_TYPE_LOADING)
            throw new IllegalArgumentException("ViewType 不能设置为 " + VIEW_TYPE_LOADING);
        return actual;
    }

    @Override
    public long getItemId(int position) {
        if (position == (getItemCount() - 1)) {
            //当前为 load more viewholder
            return VIEW_ID_LOADING;
        }
        return position;
    }

    public abstract int getActualItemViewType(int position);

    public abstract int getActualItemCount();

    public abstract void onActualBindViewHolder(VH holder, int position);

    public abstract VH onActualCreateViewHolder(ViewGroup parent, int viewType);

    public void loadMoreComplete(@NonNull final RecyclerView recyclerView) {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                int preItemCount = mEndlessScrollListener.getPreviousTotalItemCount();
                int addItemCount = getItemCount() - preItemCount;
                System.out.println("上次已有的数据数量：" + preItemCount + ",这次新增的数据数量：" + addItemCount);
                notifyItemRangeInserted(preItemCount, addItemCount);
                mEndlessScrollListener.loadMoreComplete();

                //hide load more view
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForItemId(VIEW_ID_LOADING);
                if (viewHolder != null) {
                    viewHolder.itemView.setVisibility(View.GONE);
                    System.out.println("设置 load more view 隐藏");
                }

                System.out.println("load more 结束");
            }
        });
    }

    private void initNestedScrollView(NestedScrollView nestedScrollView, final RecyclerView recyclerView) {

        EndlessNestedScrollViewScrollListener endlessNestedScrollViewScrollListener = new EndlessNestedScrollViewScrollListener(recyclerView, nestedScrollView) {
            @Override
            public void onLoadMore(int pager, int totalItemsCount, RecyclerView recyclerView) {
                loadMore(pager, totalItemsCount, recyclerView);
            }
        };

        mEndlessScrollListener = endlessNestedScrollViewScrollListener;

        nestedScrollView.setOnScrollChangeListener(endlessNestedScrollViewScrollListener);
    }

    private void initRecyclerView(RecyclerView recyclerView) {

        if (recyclerView.getLayoutManager() == null) {
            throw new IllegalArgumentException("请先设置 LayoutManager");
        }

        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {

            @Override
            public void onLoadMore(int pager, int totalItemsCount, RecyclerView recyclerView) {
                loadMore(pager, totalItemsCount, recyclerView);
            }
        };

        mEndlessScrollListener = endlessRecyclerViewScrollListener;

        recyclerView.addOnScrollListener(endlessRecyclerViewScrollListener);

    }

    private void loadMore(int pager, int totalItemsCount, RecyclerView recyclerView) {
        // show load more view
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(getItemCount() - 1);
        if (viewHolder != null) {
            viewHolder.itemView.setVisibility(View.VISIBLE);
            System.out.println("设置 load more view 显示");
        }

        System.out.println("load more 开始");
        //notify
        mLoadMoreListener.onLoadMore(pager, totalItemsCount, recyclerView);
    }

    private static class EndlessViewHolder extends RecyclerView.ViewHolder {

        EndlessViewHolder(View itemView) {
            super(itemView);
        }
    }

    private int dpTopx(float dp, Context context) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()));
    }

}
