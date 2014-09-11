/**
 * A waypoint consists of a location and an angle. 
 *
 * @author Sebastian Bauer
 */
public class WayPoint
{
	public int x,y,a;
	
	/**
	 * Constructor WayPoint.
	 * @param x defines the x coordinate
	 * @param y defines the y coordinate
	 * @param a defines the angle
	 */
	public WayPoint(int x, int y, int a)
	{
		this.x = x;
		this.y = y;
		this.a = a;
	}
	
	WayPoint duplicate()
	{
		return new WayPoint(x,y,a);
	}
}
