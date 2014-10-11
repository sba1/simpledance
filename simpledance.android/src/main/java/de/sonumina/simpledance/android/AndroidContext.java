package de.sonumina.simpledance.android;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import de.sonumina.simpledance.core.graphics.Color;
import de.sonumina.simpledance.core.graphics.Context;
import de.sonumina.simpledance.core.graphics.Point;
import de.sonumina.simpledance.core.graphics.RGB;

public class AndroidContext extends Context {
	Canvas canvas;

	public AndroidContext(Canvas canvas) {
		this.canvas = canvas;
	}

	@Override
	public void putPixel(int x, int y) {
	}

	@Override
	public void drawPolyline(int[] data) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(android.graphics.Color.BLUE);
        canvas.drawPaint(paint);
	}

	private void polygon(Paint paint, int [] data) {
		Path p = new Path();
		p.reset();
		p.moveTo(data[0], data[1]);
		for (int i=2; i < data.length; i+=2)
			p.lineTo(data[i], data[i+1]);
		p.close();
		canvas.drawPath(p, paint);
	}

	@Override
	public void drawPolygon(int[] data) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		int color = android.graphics.Color.rgb(foreground.getRed(), foreground.getGreen(), foreground.getBlue());
		paint.setColor(color);
		polygon(paint, data);
	}

	@Override
	public void fillPolygon(int[] data) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		int color = android.graphics.Color.rgb(background.getRed(), background.getGreen(), background.getBlue());
		paint.setColor(color);
		polygon(paint, data);
	}

	@Override
	public void drawLine(int x0, int y0, int x1, int y1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void drawOval(int x, int y, int mx, int my) {
		// TODO Auto-generated method stub
	}

	@Override
	public void drawText(String string, int x, int y, boolean transparent) {
		// TODO Auto-generated method stub
	}

	@Override
	public Point stringExtent(String string) {
		// TODO Implement this correctly.
		return new Point(1,1);
	}

	@Override
	public void gradientPolygon(int[] newData, RGB startRGB, RGB endRGB,
			int angle) {
		// TODO Auto-generated method stub
	}

	@Override
	public Color allocateColor(int r, int g, int b) {
		return new Color(r,g,b) {
		};
	}

	@Override
	public void deallocateColor(Color color) {
	}

	@Override
	public void applyRotateTransformation(float angle) {
		canvas.rotate(angle);
	}

	@Override
	public void applyTranslationTransformation(float x, float y) {
		canvas.translate(x, y);
	}

	@Override
	public void applyScaleTransformation(float scale) {
		canvas.scale(scale,  scale);
	}

	@Override
	public void applyScaleXTransformation(float f) {
		canvas.scale(f, 1);
	}

	@Override
	public void printCurrentTransform() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pushCurrentTransform() {
		canvas.save();
	}

	@Override
	public void popCurrentTransform() {
		canvas.restore();
	}

	@Override
	public void applyTransformation(int[] sourcePointArray, int[] destPointArray) {
		// TODO Auto-generated method stub
	}
}