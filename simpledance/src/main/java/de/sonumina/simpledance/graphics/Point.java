/*
 * Created on 01.04.2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.sonumina.simpledance.graphics;

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
	 * Add the coordiantes of the given point to the this one.
	 *
	 * @param p
	 * @return
	 */
	public Point add(Point p)
	{
		return new Point(x + p.x, y + p.y);
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
}
