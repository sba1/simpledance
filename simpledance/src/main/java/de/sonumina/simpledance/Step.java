package de.sonumina.simpledance;

import static de.sonumina.simpledance.core.I18n.N_;
import de.sonumina.simpledance.core.Foot;
import de.sonumina.simpledance.core.WayPoint;

/**
 * This class represents a single step within a dance pattern.
 *
 * @author Sebastian Bauer
 */
public class Step
{
	private Foot [] feet = new Foot[4];
	
	static String [] stepTypes = new String []
	{
		N_("Unspecified"),
		N_("Foot doesn't move"),
		N_("Heel step"),
		N_("Ball step"),
		N_("Ball step (stay)"),
		N_("Stay on ball"),
		N_("Foot in the air"),
		N_("Heel turn"),
		N_("Stand on foot"),
		N_("Stand on bale"),
		N_("Kick"),
		N_("Appell"),
		N_("Bale Tap"),
		N_("Whole foot"),
		N_("Heel Tap"),
		N_("Toe Tap"),
		N_("Jump"),
		N_("Without weight"),
	};

	
	private String count;
	private String description;
	private String duration = "slow";

	/**
	 * Constructs a new step for which all feet will have sensible defaults.
	 */
	public Step()
	{
		this(false);
	}

	/**
	 * Constructs a new step.
	 *
	 * @param zero initialize all coordinates to zero. Otherwise, some sensible default is used.
	 */
	public Step(boolean zero)
	{
		if (zero)
		{
			feet[0] = new Foot(0,0,0,true,false);
			feet[1] = new Foot(0,0,0,false,false);
			feet[2] = new Foot(0,0,0,true,true);
			feet[3] = new Foot(0,0,0,false,true);
		} else
		{
			feet[0] = new Foot(590,580,0,true,false);
			feet[1] = new Foot(610,580,0,false,false);
			feet[2] = new Foot(610,620,180,true,true);
			feet[3] = new Foot(590,620,180,false,true);
		}
	}
	
	WayPoint getStartingWayPoint(int feetNum)
	{
		return feet[feetNum].getStartingWayPoint();
	}
	
	public boolean isFemaleFoot(int footNum)
	{
		return feet[footNum].isFemale();
	}
	
	public boolean isFeetLeft(int feetNum)
	{
		return feet[feetNum].isLeft();
	}
	
	public int getNumberOfFeets()
	{
		return 4;
	}
	/**
	 * Method getFeet.
	 * @param i
	 * @return Foot
	 */
	public Foot getFoot(int i)
	{
		if (i<0 || i>4) return null;
		return feet[i];
	}

	public int [] getStepBounds()
	{
		int [] bounds = new int[4];

		bounds[0] = bounds[3] = 0x7fff;

		for (int i=0;i<4;i++)
		{
			for (int j=0;j<feet[i].getNumOfWayPoints();j++)
			{
				WayPoint feetCoord = feet[i].getWayPoint(j);
				if (feetCoord.x < bounds[0]) bounds[0] = feetCoord.x;
				if (feetCoord.x > bounds[2]) bounds[2] = feetCoord.x;
				if (feetCoord.y > bounds[1]) bounds[1] = feetCoord.y;
				if (feetCoord.y < bounds[3]) bounds[3] = feetCoord.y;
			}
		}
		return bounds;
	}

	/**
	 * Method setDescription.
	 * @param string
	 */
	public void setDescription(String string)
	{
		description = new String(string);
	}
	/**
	 * Returns the description.
	 * @return String
	 */
	public String getDescription()
	{
		if (description == null) description = "";
		return description;
	}
	/**
	 * Method setCount.
	 * @param string
	 */
	public void setCount(String string)
	{
		count = new String(string);
	}

	public String getCount()
	{
		if (count == null) count = "";
		return count;
	}

	public boolean isQuick()
	{
		return duration.equalsIgnoreCase("quick");
	}

	public boolean isSlow()
	{
		return duration.equalsIgnoreCase("slow");
	}

	public Step duplicate(boolean wayPoints)
	{
		Step step = new Step();

		for (int i=0;i<4;i++)
		{
			Foot foot = step.getFoot(i);
			WayPoint wp = foot.getStartingWayPoint();
			wp.x = feet[i].getStartingWayPoint().x;
			wp.y = feet[i].getStartingWayPoint().y;
			wp.a = feet[i].getStartingWayPoint().a;
			step.count = new String(count);
			step.duration = new String(duration);
			step.description = new String(description);
		}
		return step;
	}
	/**
	 * Returns the duration.
	 * @return String
	 */
	public String getDuration()
	{
		return duration;
	}

	/**
	 * Sets the duration.
	 * @param duration The duration to set
	 */
	public void setDuration(String duration)
	{
		this.duration = new String(duration);
	}
}
