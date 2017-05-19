package com.leo.componentsproject.weiget.loadmore;

import android.support.v7.widget.RecyclerView;

/**
 * Created on 2017/5/19 下午3:53.
 * leo linxiaotao1993@vip.qq.com
 */

 interface EndlessViewScrollListener {

    void loadMoreComplete();

    int getPreviousTotalItemCount();

    void onLoadMore(int pager, int totalItemsCount, RecyclerView recyclerView);

}
