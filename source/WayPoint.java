/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
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
