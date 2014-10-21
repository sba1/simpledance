package de.sonumina.simpledance;

import org.teavm.dom.canvas.CanvasRenderingContext2D;

import de.sonumina.simpledance.core.graphics.Color;
import de.sonumina.simpledance.core.graphics.Context;
import de.sonumina.simpledance.core.graphics.Point;
import de.sonumina.simpledance.core.graphics.RGB;

public class CanvasContext extends Context
{
	private CanvasRenderingContext2D context;

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
		// TODO Auto-generated method stub
	}

	@Override
	public void drawPolygon(int[] data)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void fillPolygon(int[] data)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void drawLine(int x0, int y0, int x1, int y1)
	{
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void gradientPolygon(int[] newData, RGB startRGB, RGB endRGB, int angle)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Color allocateColor(int r, int g, int b)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deallocateColor(Color color)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void applyRotateTransformation(float angle)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void applyTranslationTransformation(float x, float y)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void applyScaleTransformation(float scale)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void applyScaleXTransformation(float f)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void printCurrentTransform()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void pushCurrentTransform()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void popCurrentTransform()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void applyTransformation(int[] sourcePointArray, int[] destPointArray)
	{
		// TODO Auto-generated method stub
	}
}
