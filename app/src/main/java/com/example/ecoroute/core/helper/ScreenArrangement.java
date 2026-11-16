package com.example.ecoroute.core.helper;

import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.ViewGroup;
import androidx.core.graphics.Insets;
public class ScreenArrangement {

    public static void adjustStatusBarHeight(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());

            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = statusBarInsets.top;
            v.setLayoutParams(params);

            return insets;
        });

        ViewCompat.requestApplyInsets(view);
    }


    public static void adjustNavigationBarHeight(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bottomNavInserts = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = bottomNavInserts.bottom;
            v.setLayoutParams(params);
            return insets;
        });

        ViewCompat.requestApplyInsets(view);
    }
}