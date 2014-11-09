package de.sonumina.simpledance;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.LinkedList;

import org.teavm.dom.canvas.CanvasRenderingContext2D;

import de.sonumina.simpledance.core.graphics.Color;
import de.sonumina.simpledance.core.graphics.Context;
import de.sonumina.simpledance.core.graphics.Point;
import de.sonumina.simpledance.core.graphics.RGB;

public class CanvasContext extends Context
{
	private CanvasRenderingContext2D context;

	/**
	 * A simple class representing transformation matrices.
	 *
	 * @author Sebastian Bauer
	 */
	private static class Transform
	{
		public double [] m = new double[6];

		public Transform()
		{
			m[0] = 1; m[1] = 0;
			m[2] = 0; m[3] = 1;
		}

		public void translate(float x, float y)
		{
			m[4] += m[0] * x + m[2] * y;
			m[5] += m[1] * x + m[3] * y;
		}

		public void rotate(float degree)
		{
			double rad = degree * PI / 180.0;

			m[0] = m[0] * cos(rad) + m[2] * sin(rad);
			m[1] = m[1] * cos(rad) + m[3] * sin(rad);
			m[2] = - m[0] * sin(rad) + m[2] * cos(rad);
			m[3] = - m[1] * sin(rad) + m[3] * cos(rad);
		}

		public void scale(float sx, float sy)
		{
			m[0] *= sx;
			m[1] *= sx;
			m[2] *= sy;
			m[3] *= sy;
		}

		public Transform clone()
		{
			Transform cloned = new Transform();
			for (int i=0; i < m.length; i++)
				cloned.m[i] = m[i];
			return cloned;
		}
	}

	private LinkedList<Transform> transformList = new LinkedList<Transform>();

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
		transformList.add(new Transform());
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

	/**
	 * Set the current transform to the context.
	 */
	private void setCurrentTransform()
	{
		Transform current = transformList.peekFirst();
		context.setTransform(current.m[0], current.m[1], current.m[2], current.m[3], current.m[4], current.m[5]);
	}

	@Override
	public void applyRotateTransformation(float angle)
	{
		transformList.peekFirst().rotate(angle);
		setCurrentTransform();
	}

	@Override
	public void applyTranslationTransformation(float x, float y)
	{
		transformList.peekFirst().translate(x, y);
		setCurrentTransform();
	}

	@Override
	public void applyScaleTransformation(float scale)
	{
		transformList.peekFirst().scale(scale,  scale);
		setCurrentTransform();
	}

	@Override
	public void applyScaleXTransformation(float f)
	{
		transformList.peekFirst().scale(f, 1);
		setCurrentTransform();
	}

	@Override
	public void printCurrentTransform()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void pushCurrentTransform()
	{
		transformList.push(transformList.peekFirst().clone());
		setCurrentTransform();
	}

	@Override
	public void popCurrentTransform()
	{
		transformList.remove(); /* Cannot use pop() in TeaVM 0.2.1, it pops the wrong end */
		setCurrentTransform();
	}

	@Override
	public void applyTransformation(int[] sourcePointArray, int[] destPointArray)
	{
		// TODO Auto-generated method stub
	}
}
