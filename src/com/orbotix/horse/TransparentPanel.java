package com.orbotix.horse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class TransparentPanel extends LinearLayout {
    private Paint innerPaint;

    public TransparentPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TransparentPanel(Context context) {
        super(context);
        init();
    }

    private void init() {
        innerPaint = new Paint();
        innerPaint.setARGB(180, 75, 75, 75);
    }

    public void setInnerPaint(Paint innerPaint) {
        this.innerPaint = innerPaint;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        RectF drawRect = new RectF();
        drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

        canvas.drawRoundRect(drawRect, 5, 5, innerPaint);

        super.dispatchDraw(canvas);
    }
}