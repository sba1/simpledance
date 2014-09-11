/*
 * Created on 01.04.2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package graphics.swt;

import graphics.Color;
import graphics.Context;
import graphics.Point;
import graphics.RGB;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;


class Transformation
{
	double angle;
};

/**
 * @author sba
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SWTContext extends Context
{
	/** Used for the workaround for the 45 degree bug, see https://bugs.eclipse.org/bugs/attachment.cgi?id=214841 */
	private static final LineAttributes lineAttributes = new LineAttributes(1, SWT.CAP_FLAT, SWT.JOIN_MITER);

	private Display display;
	private GC gc;
	
	private Transform currentTransform;
	private LinkedList<float[]> transformationList = new LinkedList<float[]>();
	
	public SWTContext(Display display)
	{
		this.display = display;
		currentTransform = new Transform(display);
	}
	
	public void setGC(GC gc)
	{
		this.gc = gc;
		gc.setAntialias(SWT.ON);
		gc.setTransform(currentTransform);
	}
			
	public void dispose()
	{
		if (!gc.isDisposed())
			gc.setTransform(null);
		currentTransform.dispose();
	}
	
	public void putPixel(int x, int y)
	{
		gc.drawPoint(x,y);
	}

	public void drawLine(int x0, int y0, int x1, int y1)
	{
		gc.drawLine(x0,y0,x1,y1);
	}

	public void drawPolyline(int[] data)
	{
		gc.drawPolyline(data);
	}

	public void drawPolygon(int[] data)
	{
		gc.setLineAttributes(lineAttributes);
		gc.drawPolygon(data);
	}
	
	public void fillPolygon(int [] data)
	{
		gc.fillPolygon(data);
	}

	public void gradientPolygon(int [] newData, RGB startRGB, RGB endRGB, int angle)
	{
		int minx,miny,maxx,maxy;
		int i;

        /* Find minx, miny, maxx, maxy */
		minx = maxx = newData[0];
		miny = maxy = newData[1];

		if ((newData.length & 0x02) == 0) /* even number of pairs */
		{
			if (newData[2] < minx) minx = newData[2];
			else maxx = newData[2];
			if (newData[3] < miny) miny = newData[3];
			else maxy = newData[3];
			i = 4;
		} else i = 2;

		while (i<newData.length)
		{
			if (newData[i] < newData[i+2])
			{
				if (newData[i] < minx) minx = newData[i];
				else if (newData[i+2] > maxx) maxx = newData[i+2];
			} else
			{
				if (newData[i+2] < minx) minx = newData[i+2];
				else if (newData[i] > maxx) maxx = newData[i];
			}
			i++;
			if (newData[i] < newData[i+2])
			{
				if (newData[i] < miny) miny = newData[i];
				else if (newData[i+2] > maxy) maxy = newData[i+2];
			} else
			{
				if (newData[i+2] < miny) miny = newData[i+2];
				else if (newData[i] > maxy) maxy = newData[i];
			}
			i+=3;
		}

		org.eclipse.swt.graphics.Color startColor = new org.eclipse.swt.graphics.Color(display, startRGB.r,startRGB.g,startRGB.b);
		org.eclipse.swt.graphics.Color endColor = new org.eclipse.swt.graphics.Color(display, endRGB.r,endRGB.g,endRGB.b);

		Pattern pat = new Pattern(display,minx,miny,maxx,maxy,startColor,endColor);
		Pattern oldPat = gc.getBackgroundPattern();

		gc.setBackgroundPattern(pat);
		gc.fillPolygon(newData);
		gc.setBackgroundPattern(oldPat);

		pat.dispose();
		startColor.dispose();
		endColor.dispose();
	}

	
	public void drawOval(int x, int y, int mx, int my)
	{
		gc.drawOval(x,y,mx,my);
	}

	public void drawText(String string, int x, int y, boolean transparent)
	{
		gc.drawText(string,x,y,transparent);
	}
	
	public Point stringExtent(String string)
	{
		org.eclipse.swt.graphics.Point p = gc.stringExtent(string);
		return new Point(p.x,p.y);
	}
	
	public void setForeground(Color color)
	{
		super.setForeground(color);
		gc.setForeground(((SWTColor)color).col);
	}

	public void setBackground(Color color)
	{
		super.setBackground(color);
		gc.setBackground(((SWTColor)color).col);
	}
	
	public void setLineWidth(int width)
	{
		super.setLineWidth(width);
		gc.setLineWidth(width);
	}

	public Color allocateColor(int r, int g, int b)
	{
		return new SWTColor(display,r,g,b);
	}

	public void deallocateColor(Color color)
	{	
		((SWTColor)color).dispose();
	}
	
	public void applyRotateTransformation(float angle)
	{
		currentTransform.rotate(angle);
		gc.setTransform(currentTransform);
	}
	
	@Override
	public void applyTranslationTransformation(float x, float y)
	{
		currentTransform.translate(x, y);
		gc.setTransform(currentTransform);
	}
	
	public void applyScaleTransformation(float scale)
	{
		currentTransform.scale(scale, scale);
		gc.setTransform(currentTransform);
	}

	@Override
	public void applyScaleXTransformation(float f)
	{
		currentTransform.scale(f, 1.0f);
		gc.setTransform(currentTransform);
	}

	@Override
	public void printCurrentTransform()
	{
		float [] elem = new float[6];
		currentTransform.getElements(elem);
		for (int i=0;i<6;i++) System.out.print(elem[i] + " ");
		System.out.println();
	}

	@Override
	public void pushCurrentTransform()
	{
		float [] elements = new float[6];
		currentTransform.getElements(elements);
		transformationList.push(elements);
	}

	@Override
	public void popCurrentTransform()
	{
		float [] elements = transformationList.pop();
		currentTransform.setElements(elements[0],  elements[1],  elements[2],  elements[3],  elements[4], elements[5]);
		gc.setTransform(currentTransform);
	}
}
