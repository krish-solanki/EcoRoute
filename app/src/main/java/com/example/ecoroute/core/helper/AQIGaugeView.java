package com.example.ecoroute.core.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class AQIGaugeView extends View{

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private int aqi = 65;

    public AQIGaugeView(Context context) {
        super(context);
        init();
    }

    public AQIGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setAQI(int value) {
        aqi = Math.max(0, Math.min(value, 500));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float stroke = 10f;
        float left = stroke;
        float top = stroke;
        float right = getWidth() - stroke;
        float bottom = getHeight() * 1.65f;

        rect.set(left, top, right, bottom);

        float[] angles = {90f, 45f, 25f, 10f};
        int[] colors = {
                0xFF42A64D,
                0xFFFFC928,
                0xFFFF8A24,
                0xFFE53935
        };

        float start = 180f;

        for (int i = 0; i < angles.length; i++) {
            paint.setColor(colors[i]);
            canvas.drawArc(rect, start, angles[i], false, paint);
            start += angles[i];
        }

        // AQI indicator position
        float angle = 180f - (aqi / 500f) * 180f;
        double radians = Math.toRadians(angle);

        float cx = rect.centerX();
        float cy = rect.centerY();

        float radius = rect.width() / 2f;

        float x = cx + radius * (float) Math.cos(radians);
        float y = cy - radius * (float) Math.sin(radians);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF17652C);

        canvas.drawCircle(x, y, 9f, paint);

        paint.setTextSize(12f);
        paint.setColor(0xFF66796D);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("0", 5f, getHeight() - 2f, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("500", getWidth() - 5f, getHeight() - 2f, paint);
    }

}