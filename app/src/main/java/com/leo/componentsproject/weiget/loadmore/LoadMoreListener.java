package com.leo.componentsproject.weiget.loadmore;

import android.support.v7.widget.RecyclerView;

/**
 * Created on 2017/5/19 上午10:58.
 * leo linxiaotao1993@vip.qq.com
 */

public interface LoadMoreListener {

    void onLoadMore(int pager, int totalItemsCount, RecyclerView recyclerView);
}
