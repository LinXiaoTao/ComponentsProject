package com.leo.componentsproject.weiget.chart;

/**
 * Created on 2017/5/25 下午4:50.
 * leo linxiaotao1993@vip.qq.com
 */

public class ChartData {

    private int x;
    private int y;
    private String value;

    public int getX() {
        return x;
    }

    public ChartData setX(int x) {
        this.x = x;
        return this;
    }

    public int getY() {
        return y;
    }

    public ChartData setY(int y) {
        this.y = y;
        return this;
    }

    public String getValue() {
        return value;
    }

    public ChartData setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return String.format("x：%d,y：%d", x, y);
    }
}
