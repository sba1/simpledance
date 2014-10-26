package de.sonumina.simpledance;

import org.teavm.dom.canvas.CanvasRenderingContext2D;

import de.sonumina.simpledance.core.graphics.Color;
import de.sonumina.simpledance.core.graphics.Context;
import de.sonumina.simpledance.core.graphics.Point;
import de.sonumina.simpledance.core.graphics.RGB;

public class CanvasContext extends Context
{
	private CanvasRenderingContext2D context;

	/**
	 * Convert the given color into a html one.
	 *
	 * @param color
	 * @return
	 */
	private String htmlColor(Color color)
	{
		if (color == null)
			return "black";
		/* TeaVM doesn't provide sprintf() for now so we have to manually convert
		 * the string. This "one"-liner has been found in
		 *  https://stackoverflow.com/questions/8689526/integer-to-two-digits-hex-in-java
		 */
		String redString = Integer.toHexString(color.getRed() | 0x100).substring(1);
		String greenString = Integer.toHexString(color.getGreen() | 0x100).substring(1);
		String blueString = Integer.toHexString(color.getBlue() | 0x100).substring(1);
		return "#" + redString + greenString + blueString;
	}

	public CanvasContext(CanvasRenderingContext2D context)
	{
		this.context = context;
	}

	@Override
	public void putPixel(int x, int y)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void drawPolyline(int[] data)
	{
		if (data.length < 2) return;
		context.setStrokeStyle(htmlColor(foreground));
		preparePolygon(data);
		context.stroke();
	}

	@Override
	public void drawPolygon(int[] data)
	{
		if (data.length < 2) return;
		context.setStrokeStyle(htmlColor(foreground));
		preparePolygon(data);
		context.closePath();
		context.stroke();
	}

	@Override
	public void fillPolygon(int[] data)
	{
		if (data.length < 2) return;
		context.setFillStyle(htmlColor(background));
		preparePolygon(data);
		context.fill();
	}

	private void preparePolygon(int[] data)
	{
		context.beginPath();
		context.moveTo(data[0], data[1]);
		for (int i=2; i < data.length; i+=2)
			context.lineTo(data[i], data[i+1]);
	}

	@Override
	public void drawLine(int x0, int y0, int x1, int y1)
	{
		drawPolyline(new int[]{x0,y0,x1,y1});
	}

	@Override
	public void drawOval(int x, int y, int mx, int my)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void drawText(String string, int x, int y, boolean transparent)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Point stringExtent(String string)
	{
		// TODO Implement this correctly.
		return new Point(1,1);
	}

	@Override
	public void gradientPolygon(int[] newData, RGB startRGB, RGB endRGB, int angle)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Color allocateColor(int r, int g, int b)
	{
		return new Color(r, g, b)
		{
		};
	}

	@Override
	public void deallocateColor(Color color)
	{
	}

	@Override
	public void applyRotateTransformation(float angle)
	{
		context.rotate(angle);
	}

	@Override
	public void applyTranslationTransformation(float x, float y)
	{
		context.translate(x, y);
	}

	@Override
	public void applyScaleTransformation(float scale)
	{
		context.scale(scale, scale);
	}

	@Override
	public void applyScaleXTransformation(float f)
	{
		context.scale(f, 1);
	}

	@Override
	public void printCurrentTransform()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void pushCurrentTransform()
	{
		context.save();
	}

	@Override
	public void popCurrentTransform()
	{
		context.restore();
	}

	@Override
	public void applyTransformation(int[] sourcePointArray, int[] destPointArray)
	{
		// TODO Auto-generated method stub
	}
}
