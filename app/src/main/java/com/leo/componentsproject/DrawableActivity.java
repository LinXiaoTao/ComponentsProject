package com.leo.componentsproject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.leo.componentsproject.weiget.BuidingManageDrawable;
import com.leo.componentsproject.weiget.RectangleProgressDrawable;
import com.leo.componentsproject.weiget.StepIndicatorDrawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 2017/6/6 下午10:21.
 * leo linxiaotao1993@vip.qq.com
 */

public class DrawableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawable);

        View step = findViewById(R.id.step);
        List<StepIndicatorDrawable.StepItem> stepItems = new ArrayList<>();
        for (int i = 0; i < 3; i++) {

            StepIndicatorDrawable.StepItem stepItem = new StepIndicatorDrawable.StepItem(Arrays.asList("测试", "测试"));
            stepItems.add(stepItem);
        }
        StepIndicatorDrawable stepIndicatorDrawable = new StepIndicatorDrawable(this, stepItems, 2);
        step.setBackground(stepIndicatorDrawable);

        View rectangle = findViewById(R.id.rectangle);
        RectangleProgressDrawable rectangleProgressDrawable = new RectangleProgressDrawable(this);
        rectangleProgressDrawable.setMaxValue(1000);
        rectangleProgressDrawable.startCurrentValue(500);
        rectangle.setBackground(rectangleProgressDrawable);

        View cicle = findViewById(R.id.cicle);
        BuidingManageDrawable buidingManageDrawable = new BuidingManageDrawable(this);
        buidingManageDrawable.setFirst(0.5f)
                .setSecond(0.8f)
                .setThird(0.7f)
                .invalidateSelf();
        cicle.setBackground(buidingManageDrawable);
    }
}
