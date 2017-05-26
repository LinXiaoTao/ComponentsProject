package com.leo.componentsproject.weiget.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created on 2017/5/26 下午12:08.
 * leo linxiaotao1993@vip.qq.com
 */

public class ChartScrollView extends HorizontalScrollView {

    private LineChartView mLineChartView;

    public ChartScrollView(Context context) {
        this(context, null);
    }

    public ChartScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ChartScrollView setLineChartView(LineChartView lineChartView) {
        mLineChartView = lineChartView;
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mLineChartView, layoutParams);
        return this;
    }

    private void init() {
    }
}
