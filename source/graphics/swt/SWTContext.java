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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;


/**
 * @author sba
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SWTContext extends Context
{
	private Display display;
	private GC gc;
	
	public SWTContext(Display display)
	{
		this.display = display;
	}
	
	public void setGC(GC gc)
	{
		this.gc = gc;
	}
			
	public void dispose()
	{
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

        /* Create the image */
		Rectangle bounds = new Rectangle(minx,miny,maxx-minx+1,maxy-miny+1);
		ImageData imageData = SWTUtil.getRectangleGradient(bounds.width,bounds.height,angle,startRGB,endRGB);
		
		org.eclipse.swt.graphics.RGB [] maskPaletteData = new org.eclipse.swt.graphics.RGB[]{new org.eclipse.swt.graphics.RGB(0,0,0),new org.eclipse.swt.graphics.RGB(255,255,255)};
		ImageData maskImageData = new ImageData(bounds.width,bounds.height,8,new PaletteData(maskPaletteData));
		maskImageData.transparentPixel = 0;
		Image maskImage = new Image(display,maskImageData);
		GC maskGC = new GC(maskImage);
		org.eclipse.swt.graphics.Color white = new org.eclipse.swt.graphics.Color(display,255,255,255);
		maskGC.setBackground(white);

		for (i=0;i<newData.length;i+=2)
		{
			newData[i] -= minx;
			newData[i+1] -= miny;
		}

		maskGC.fillPolygon(newData);

		maskGC.dispose();
		white.dispose();

		// ----------------
		maskImageData = maskImage.getImageData();
		final int h = maxy - miny + 1;
		final int w = maxx - minx + 1;
		int p = 0;
		
		byte [] alphaData = new byte[w*h];
		byte [] maskData = maskImageData.data;		

		for (int y=0;y<h;y++)
		{
			int o = p;
			for (int x=0;x<w;x++)
			{
				if (maskData[o++]!=0) alphaData[x] = -1;
				else alphaData[x] = 0;
			}
			imageData.setAlphas(0,y,w,alphaData,0);
			p += maskImageData.bytesPerLine;
		}
		Image fillImage = new Image(display,imageData);
		// ----------------

/*		maskImageData = maskImage.getImageData();
		final int h = maxy - miny + 1;
		final int w = maxx - minx + 1;
		int p = 0, q = 0;
		
		byte [] newMaskData = new byte[((w+7)/8) *h];
		byte [] maskData = maskImageData.data;		

		for (int y=0;y<h;y++)
		{
			int o = p;
			byte maskBit = (byte)0x80;
			byte maskByte = 0;

			for (int x=0;x<w;x++)
			{
				if (maskData[o++]!=0) maskByte |= maskBit;
				maskBit = (byte) (maskBit >> 1);
				if (maskBit == 0)
				{
					maskBit = (byte)0x80;
					newMaskData[q++] = maskByte;
					maskByte = 0;
				} 
			}
			if (maskByte != 0) newMaskData[q++] = maskByte;
			p += maskImageData.bytesPerLine;
		}
		imageData.maskData = newMaskData;
		Image fillImage = new Image(getDisplay(),imageData);*/

		// ----------------

/*
		maskImageData = maskImage.getImageData();
		maskImageData.transparentPixel = 0;
		maskImageData = maskImageData.getTransparencyMask();
		Image fillImage = new Image(getDisplay(),imageData,maskImageData);
*/
		// ----------------

		gc.drawImage(fillImage,bounds.x,bounds.y);
		
		maskImage.dispose();
		fillImage.dispose();
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

}
