package org.willisson.wapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.util.AttributeSet;

public class GraphView extends View {
    public int width;
    public int height;
    private Bitmap bitmap;
    private Canvas canvas;
    Context context;

    private Paint white_paint;
    private Paint green_paint;

    public float x;
    public float y;

    public GraphView(Context c, AttributeSet attrs) {
	super(c, attrs);

	context = c;

	white_paint = new Paint();
	white_paint.setARGB(255, 255, 255, 255);

	green_paint = new Paint();
	green_paint.setARGB(255, 0, 255, 0);

	x = 0;
	y = 0;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	super.onSizeChanged(w, h, oldw, oldh);

	bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	canvas = new Canvas(bitmap);
    }

    protected void onDraw(Canvas c) {
	super.onDraw(c);
	c.drawRGB(0, 0, 0);

	c.drawLine(0, 200, 400, 200, white_paint);
	c.drawLine(200, 0, 200, 400, white_paint);
	c.drawLine(200, 200, 200 + x*2, 200 + y*2, green_paint);
    }
}

