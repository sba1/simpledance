package de.sonumina.simpledance.core;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Represents a single foot.
 *
 * @author Sebastian Bauer
 */
public class Foot
{
	static public final int NOT_SPECIFIED = 0;
	static public final int FOOT_NOT_MOVED = 1;
	static public final int HEEL_STEP = 2;
	static public final int BALL_STEP = 3;
	static public final int BALL_STEP_STAY = 4;
	static public final int BALL_STAY = 5;
	static public final int FOOT_IN_THE_AIR = 6;
	static public final int HEEL_TURN = 7;
	static public final int STAND_ON_FOOT = 8;
	static public final int STAND_IN_BALL = 9;
	static public final int KICK = 10;
	static public final int APELL = 11;
	static public final int TAP = 12;
	static public final int WHOLE_FOOT = 13;
	static public final int HEEL_TAP = 14;
	static public final int TOE_TAP = 15;
	static public final int JUMP = 16;
	static public final int WITHOUT_WEIGHT = 17;
	
	private LinkedList<WayPoint> wayPointList = new LinkedList<>();
	private int type = NOT_SPECIFIED;
	private boolean female;
	private boolean left;
	private boolean longRotation;

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

	public WayPoint getStartingWayPoint()
	{
		return (WayPoint)wayPointList.getFirst();
	}

	public WayPoint getLastWayPoint()
	{
		return (WayPoint)wayPointList.getLast();
	}

	
	public WayPoint getWayPoint(int i)
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

	/**
	 * Returns the distance factor given the current time progress.
	 * Also called easing function. Here, a simple linear approach is
	 * chosen.
	 *
	 * @param t the fractional time. Should be between 0 and 1.
	 *
	 * @return the distance factor.
	 */
	private double getDistanceFactor(double t)
	{
		if (t<0.5) return 0;
		return (t-0.5)/0.5;
	}

	public WayPoint getInterpolatedWayPoint(WayPoint to, boolean longAngle, int step, int steps)
	{
		int distance = 0;
		
		LinkedList<WayPoint> wayPointList = new LinkedList<>();
		
		for (int i=0;i<getNumOfWayPoints();i++)
			wayPointList.add(getWayPoint(i));
		wayPointList.add(to);
		
		/* calculate the distance */
		ListIterator<WayPoint> iter = wayPointList.listIterator();
		WayPoint prevWayPoint = iter.next();
		while (iter.hasNext())
		{
			WayPoint wayPoint = iter.next();
			int diffX = prevWayPoint.x - wayPoint.x;
			int diffY = prevWayPoint.y - wayPoint.y; 
			distance += Math.sqrt(diffX * diffX + diffY * diffY);
			prevWayPoint = wayPoint;
		}

		int completeDistance = distance;
		int interDistance = (int)(distance * getDistanceFactor((double)step / steps));
		int completeInterDistance = interDistance;
		
		/* next step is to find to current section */
		iter = wayPointList.listIterator();
		distance = 0;
		prevWayPoint = iter.next();
		WayPoint wayPoint = null;
		for (int i=0;i<getNumOfWayPoints();i++)
		{
			wayPoint = iter.next();
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
		
		if (longAngle)
		{
			if (da <= 0) da = 360 + da;
			else if (da >= 0) da = -360 + da;
		}

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

	public void setLongRotation(boolean longRotation)
	{
		this.longRotation = longRotation;
	}

	/**
	 * @return boolean
	 */
	public boolean isLongRotation()
	{
		return longRotation;
	}
}
