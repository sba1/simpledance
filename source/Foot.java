import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Foot
{
	static final int NOT_SPECIFIED = 0;
	static final int FOOT_NOT_MOVED = 1;
	static final int HEEL_STEP = 2;
	static final int BALL_STEP = 3;
	static final int BALL_STEP_STAY = 4;
	static final int BALL_STAY = 5;
	static final int FOOT_IN_THE_AIR = 6;
	static final int HEEL_TURN = 7;
	static final int STAND_ON_FOOT = 8;
	static final int STAND_IN_BALL = 9;
	static final int KICK = 10;
	static final int APELL = 11;
	static final int TAP = 12;
	static final int WHOLE_FOOT = 13;
	static final int HEEL_TAP = 14;
	static final int TOE_TAP = 15;
	static final int JUMP = 16;
	static final int WITHOUT_WEIGHT = 17;
	
	private LinkedList wayPointList = new LinkedList();
	private int type = NOT_SPECIFIED;
	private boolean female;
	private boolean left;

	/**
	 * Method Foot.
	 * @param x
	 * @param y
	 * @param a
	 * @param left
	 * @param female
	 */
	public Foot(int x, int y, int a, boolean left, boolean female)
	{
		WayPoint feetCoord = new WayPoint(x,y,a);
		wayPointList.add(feetCoord);
		
		this.female = female;
		this.left = left;
	}
	
	/**
	 * Method getFinalFeetCoord.
	 * @return FeetCoord
	 */
//	public WayPoint getFinalFeetCoord()
//	{
//		return (WayPoint)wayPointList.get(wayPointList.size()-1);
//	}

	public WayPoint getStartingWayPoint()
	{
		return (WayPoint)wayPointList.getFirst();
	}
	
	public WayPoint getFeetCoord(int i)
	{
		if (i >= wayPointList.size()) return null;
		return (WayPoint)wayPointList.get(i);
	}
	
	public int getNumOfWayPoints()
	{
		return wayPointList.size();
	}
	
	public void removeAllWayPoints()
	{
		WayPoint feetCoord = getStartingWayPoint();
		wayPointList.clear();
		wayPointList.add(feetCoord);
	}

	/**
	 * Returns the female.
	 * @return boolean
	 */
	public boolean isFemale() {
		return female;
	}

	/**
	 * Sets the female.
	 * @param female The female to set
	 */
	public void setFemale(boolean female) {
		this.female = female;
	}
	/**
	 * Returns the left.
	 * @return boolean
	 */
	public boolean isLeft() {
		return left;
	}

	/**
	 * Sets the left.
	 * @param left The left to set
	 */
	public void setLeft(boolean left) {
		this.left = left;
	}

	public void addWayPoint(WayPoint wayPoint, int index)
	{
		wayPointList.add(index,wayPoint);
	}

	public double getRelDistanceOfT(double t)
	{
		if (t<0.5) return 0;
		return (t-0.5)/0.5;
	}

	public WayPoint getInterpolatedWayPoint(WayPoint to, int step, int steps)
	{
		int distance = 0;
		
		LinkedList wayPointList = new LinkedList();
		
		for (int i=0;i<getNumOfWayPoints();i++)
			wayPointList.add(getFeetCoord(i));
		wayPointList.add(to);
		
		/* calculate the distance */
		ListIterator iter = wayPointList.listIterator();
		WayPoint prevWayPoint = (WayPoint)iter.next();
		while (iter.hasNext())
		{
			WayPoint wayPoint = (WayPoint)iter.next(); 	
			int diffX = prevWayPoint.x - wayPoint.x;
			int diffY = prevWayPoint.y - wayPoint.y; 
			distance += Math.sqrt(diffX * diffX + diffY * diffY);
			prevWayPoint = wayPoint;
		}

		int completeDistance = distance;
		int interDistance = (int)(distance * getRelDistanceOfT((double)step / steps));
		int completeInterDistance = interDistance;
		
		/* next step is to find to current section */
		iter = wayPointList.listIterator();
		distance = 0;
		prevWayPoint = (WayPoint)iter.next();
		WayPoint wayPoint = null;
		for (int i=0;i<getNumOfWayPoints();i++)
		{
			wayPoint = (WayPoint)iter.next(); 	
			int diffX = prevWayPoint.x - wayPoint.x;
			int diffY = prevWayPoint.y - wayPoint.y;
			int newDistance = distance + (int)Math.sqrt(diffX * diffX + diffY * diffY);

			if (newDistance > interDistance)
			{
				interDistance = interDistance - distance;
				distance = (int)Math.sqrt(diffX * diffX + diffY * diffY);
				break;
			} 

			distance = newDistance;  
			prevWayPoint = wayPoint;
		}

		int x = 0;
		int y = 0;
		int a = 0;

		int da = -getStartingWayPoint().a + to.a;
		if (da > 180) da -= 360;
		else if (da < -180) da += 360;

		if (distance != 0)
		{
			double diffX = prevWayPoint.x - wayPoint.x;
			double diffY = prevWayPoint.y - wayPoint.y;
		
			diffX /= distance;
			diffY /= distance;
			
			x = prevWayPoint.x - (int)(diffX * interDistance);
			y = prevWayPoint.y - (int)(diffY * interDistance);
			a = getStartingWayPoint().a + da * completeInterDistance / completeDistance;
		} else
		{
			x = prevWayPoint.x;
			y = prevWayPoint.y;
			a = getStartingWayPoint().a + da * step/steps; 
		}

	
		WayPoint feetCoord = new WayPoint(x,y,a);
		return feetCoord;
	}

	/**
	 * Returns the type.
	 * @return int
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(int type)
	{
		this.type = type;
	}

}
