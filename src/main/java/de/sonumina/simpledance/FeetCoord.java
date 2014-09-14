package de.sonumina.simpledance;
/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FeetCoord
{
	public int x,y,a;
	
	/**
	 * Constructor FeetCoord.
	 * @param x defines the x coordinate
	 * @param y defines the y coordinate
	 * @param a defines the angle
	 */
	public FeetCoord(int x, int y, int a)
	{
		this.x = x;
		this.y = y;
		this.a = a;
	}
	
	FeetCoord duplicate()
	{
		return new FeetCoord(x,y,a);
	}
	
/*	static public FeetCoord getInterpolatedFeetCoord(FeetCoord a, FeetCoord b, int step, int steps)
	{
		int nx = a.x + (b.x - a.x)*step/steps;
		int ny = a.y + (b.y - a.y)*step/steps;
		int na;
		
		int da = b.a - a.a;

		if (da > 180) da -= 360;
		else if (da < -180) da += 360;

		na = a.a + da*step/steps;
		if (na > 360) na -= 360;
		if (na < 0) na += 360;
		return new FeetCoord(nx,ny,na);
	}*/
	
}
