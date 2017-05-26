package com.leo.componentsproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        System.out.println("屏幕尺寸：" + displayMetrics.widthPixels + "," + displayMetrics.heightPixels);

//        View view = findViewById(R.id.recyangle);
//        RectangleProgressDrawable rectangleProgressDrawable = new RectangleProgressDrawable(this);
//        view.setBackground(rectangleProgressDrawable);
//        rectangleProgressDrawable
//                .setValueGravity(Gravity.RIGHT)
//                .setValueUnit("万元")
//                .setCurrentValue(80f)
//                .setMaxValue(100f)
//                .invalidateSelf();

    }
}
