/*
 * Created on 01.04.2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package graphics.swt;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

/**
 * @author sba
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SWTUtil
{
	static public ImageData getRectangleGradient(int width, int height, int angle, graphics.RGB startRGB, graphics.RGB endRGB)
	{
		/* The basic idea of this algorithm is to calc the intersection between the
		 * diagonal of the rectangle (xs,ys) with dimension (xw,yw) a with the line starting
		 * at (x,y) (every pixel inside the rectangle) and angle angle with direction vector (vx,vy).
		 * 
		 * Having the intersection point we then know the color of the pixel.
		 * 
		 * TODO: Turn the algorithm into a incremental one
		 *       Remove the use of floating point variables
		 */
		 
		ImageData imageData = new ImageData(width,height,24,new PaletteData(0xff,0xff00,0xff0000));
		byte [] data = imageData.data;
		int p = 0;
		double rad = Math.toRadians(angle);
		double cosarc = Math.cos(rad);
		double sinarc = Math.sin(rad);

		/* Normalize the angle */
		if (angle < 0) angle = 360 - ((-angle)%360);
		if (angle >= 0) angle = angle % 360;

		final int diffR = endRGB.r - startRGB.r;
		final int diffG = endRGB.g - startRGB.g;
		final int diffB = endRGB.b - startRGB.b;

		int xs,ys,xw,yw;

		int vx = (int)(-cosarc*0x100);
		int vy = (int)(sinarc*0x100);

		if (angle <= 90 || (angle > 180 && angle <= 270))
		{
			/* The to be intersected diagonal goes from the top left edge to the bottom right edge */
			xs = 0;
			ys = 0;
			xw = width;
			yw = height;
		} else
		{
			/* The to be intersected diagonal goes from the bottom left edge to the top right edge */
			xs = 0;
			ys = height;
			xw = width;
			yw = -height;
		}
		
		final int xadd,ystart,yadd;
		
		if (angle > 90 && angle <= 270)
		{
			/* for these angle we have y1 = height - y1. Instead of
			 * 
			 *  y1 = height - (-vy*(yw*  xs -xw*  ys)         + yw*(vy*  x -vx*  y))        /(-yw*vx + xw*vy);
			 * 
			 * we have
			 * 
             *  y1 =          (-vy*(yw*(-xs)-xw*(-ys+height)) + yw*(vy*(-x)-vx*(-y+height)))/(-yw*vx + xw*vy);
             * 
             * so height - y1 can be expressed with the normal formular adapting some parameters.
			 * 
			 * Note that if one would exchanging startRGB/endRGB the values would only work
			 * for linear color gradients
			 */
			xadd = -1;
			yadd = -1;
			ystart = height;

			xs = -xs;
			ys = -ys + height;
		} else
		{
			xadd = 1;
			yadd = 1;
			ystart = 0;
		}

		int x1,y1;

		/* The formular as shown above is
		 * 
		 * 	 y1 = ((-vy*(yw*xs-xw*ys) + yw*(vy*x-vx*y)) /(-yw*vx + xw*vy));
		 * 
		 * We see that only yw*(vy*x-vx*y) changes during the loop.
		 * 
		 * We write
		 *   
		 *   y1(x,y) = (r + yw*(vy*x-vx*y))/t = r/t + yw*(vy*x-vx*y)/t
		 *   y1(x+1,y) = (r + vw*(vy*(x+1)-vx*y))/t 
		 *   t*(y1(x+1,y) - y1(x,y)) = yw*(vy*(x+1)-vx*y) - yw*(vy*x-vx*y) = yw*vy;
		 * 
		 */

		int r = -vy*(yw*xs-xw*ys); 
		int t = -yw*vx + xw*vy;
		int incr_y1 = yw*vy*xadd;
		
		int height_square = height*height;

		for (int l = 0, y = ystart; l < height; l++, y+=yadd)
		{
			int o = p;
			int y1_mul_t_accu = r - yw*vx*y;

			for (int c = 0, x = 0; c < width; c++, x+=xadd)
			{
				int red,green,blue;

				/* Calculate the intersection of two lines, this is not the fastet way to do but
				 * it is intuitive. Will be optimzed later */
//				y1 = (int)((r + yw*(vy*x-vx*y))/t);
				y1 = y1_mul_t_accu / t;
				
				int e = y1 * y1 / height * y1;

				red = startRGB.r + (int)(diffR*e/height_square);
				green = startRGB.g + (int)(diffG*e/height_square);
				blue = startRGB.b + (int)(diffB*e/height_square);

				data[o++] = (byte)blue;
				data[o++] = (byte)green;
				data[o++] = (byte)red;
				
				y1_mul_t_accu += incr_y1;
			}
			p += imageData.bytesPerLine;
		}
		
		return imageData;
	}

}
