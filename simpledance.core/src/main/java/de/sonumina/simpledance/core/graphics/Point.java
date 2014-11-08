package de.sonumina.simpledance.core.graphics;

/**
 * This is a simple point or two dimensional vector together with some
 * simple operations in Euclidean space.
 *
 * @author Sebastian Bauer
 */
public class Point
{
	public int x,y;

	private final static Point ORIGIN = new Point(0,0);

	public Point(int x, int y){this.x = x;this.y = y;};

	@Override
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}

	/**
	 * Add the coordinates of the given point to the this one.
	 *
	 * @param p
	 * @return
	 */
	public Point add(Point p)
	{
		return new Point(x + p.x, y + p.y);
	}

	/**
	 * Sub the coordinates of the given point from this one.
	 *
	 * @param p
	 * @return
	 */
	public Point sub(Point p)
	{
		return new Point(x - p.x, y - p.y);
	}

	/**
	 * Multiply the point (vector) with a given scalar.
	 *
	 * @param factor
	 * @return
	 */
	public Point mult(int factor)
	{
		return new Point(x * factor, y * factor);
	}

	/**
	 * Divide the point (vector) by a given scalar.
	 *
	 * @param factor
	 * @return
	 */
	public Point div(int divident)
	{
		return new Point(x / divident, y / divident);
	}

	/**
	 * Return the middle of this point and the given point.
	 *
	 * @param p
	 * @return
	 */
	public Point center(Point p)
	{
		return new Point((x + p.x)/2, (y+p.y)/2);
	}

	/**
	 * Calculates the Euclidean distance form this point to the given one.
	 *
	 * @param p
	 * @return
	 */
	public int distance(Point p)
	{
		return (int)Math.sqrt((double)(x - p.x)*(x - p.x) + (double)(y - p.y)*(y - p.y));
	}

	/**
	 * Rotate this point against a given origin with the given angle.
	 *
	 * @param angle
	 * @param origin
	 * @return
	 */
	public Point rotate(int angle, Point origin)
	{
		double cosa = Math.cos(Math.toRadians(angle));
		double sina = Math.sin(Math.toRadians(angle));
		int ax = x - origin.x;
		int ay = y - origin.y;
		int newx = (int)(ax * cosa - ay * sina) + origin.x;
		int newy = (int)(ax * sina + ay * cosa) + origin.y;
		return new Point(newx, newy);
	}

	/**
	 * Rotate this point against a (0,0) origin with the given angle.
	 *
	 * @param angle
	 * @return
	 */
	public Point rotate(int angle)
	{
		return rotate(angle, ORIGIN);
	}

	/**
	 * Checks whether ray starting at (xa, ya) with constant ya crosses (xb,yb) to (xc,yc)
	 *
	 * @param xa
	 * @param ya
	 * @param xb
	 * @param yb
	 * @param xc
	 * @param yc
	 * @return
	 * @see https://de.wikipedia.org/wiki/Punkt-in-Polygon-Test_nach_Jordan
	 */
	private int crossTest(int xa, int ya, int xb, int yb, int xc, int yc)
	{
		if (ya == yb && yb == yc)
		{
			if (xb <= xa && xa <= xc  || xc <= xa && xa <= xb)
			{
				return 0;
			}
			return 1;
		}
		/* Make sure that yb is not larger than yc */
		if (yb > yc)
		{
			int t = yb;
			yb = yc;
			yc = t;
			t = xb;
			xb = xc;
			xc = t;
		}
		if (ya == yb && xa == xb)
			return 0;
		if (ya <= yb || ya > yc)
			return 1;
		double delta = (xb - xa)*(yc - ya) - (yb - ya)*(xc - xa);

		if (delta > 0) return -1;
		if (delta < 0) return 1;
		return 0;
	}

	/**
	 * Checks whether the point is contained in the given polygon.
	 *
	 * @param data
	 * @return
	 */
	public boolean isContainedIn(int data [])
	{
		double t = -1;
		int i;
		for (i=0; i < data.length - 2; i+=2)
			t *= crossTest(x,y,data[i],data[i+1],data[i+2],data[i+3]);
		t *= crossTest(x,y,data[i],data[i+1],data[0],data[1]);
		return t > 0;
	}
}
