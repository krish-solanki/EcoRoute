package com.example.ecoroute.core.helper;

import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ScreenArrangement {

    public static void adjustStatusBarHeight(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int height = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(v.getPaddingLeft(), height, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        ViewCompat.requestApplyInsets(view);
    }

    public static void adjustNavigationBarHeight(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int height = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            v.getLayoutParams().height = height;
            v.requestLayout();
            return insets;
        });

        ViewCompat.requestApplyInsets(view);
    }
}