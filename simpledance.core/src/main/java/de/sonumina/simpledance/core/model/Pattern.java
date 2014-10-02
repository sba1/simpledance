package de.sonumina.simpledance.core.model;

import static de.sonumina.simpledance.core.I18n.N_;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.util.LinkedList;

import de.sonumina.simpledance.core.Fraction;

/**
 * This class represents a single dance pattern.
 *
 * @author Sebastian Bauer
 */
public class Pattern
{
	private String filename;

	static public final int DANCE_OTHER = 0;
	static public final int DANCE_SLOW_WALZ = 1;
	static public final int DANCE_VIENESSE_WALZ = 2;
	static public final int DANCE_TANGO = 3;
	static public final int DANCE_FOXTROTT = 4;
	static public final int DANCE_QUICKSTEP = 5;
	static public final int DANCE_SLOWFOX = 6;
	static public final int DANCE_CHA_CHA_CHA = 7;
	static public final int DANCE_RUMBA = 8;
	static public final int DANCE_JIVE = 9;
	static public final int DANCE_SAMBA = 10;
	static public final int DANCE_PASO_DOBLE = 11;
	static public final int DANCE_DISCO_FOX = 12;
	static public final int DANCE_MAX = 13;
	
	static final String typeNames[] =
	{
		N_("Other"),
		N_("Slow Walz"),
		N_("Vienesse Walz"),
		N_("Tango"),
		N_("Foxtrott"),
		N_("Quickstep"),
		N_("Slowfox"),
		N_("Cha Cha Cha"),
		N_("Rumba"),
		N_("Jive"),
		N_("Samba"),
		N_("Paso Doble"),
		N_("Disco Fox"),
	};
	
	static final public String getTypeName(int type)
	{
		if (type >= DANCE_OTHER && type < DANCE_MAX)
			return typeNames[type];
		return typeNames[0];
	}

	private LinkedList<Step> stepList = new LinkedList<>();
	private int currentStepNum = 0;
	
	private int timeSignatureBars = 4;
	private int timeSignatureBeats = 4;
	private int barsPerMinute = 32;
	private int beatsPerSlowStep = 1;
	private int type;
	private String name = "";
	
	static public class PatternInfo
	{
		public int type;
		public String name;
		public Object data;
	};

	static public class AnimationInfo
	{
		public int framesperbeat;

		/**
		 * The frames per 1000 seconds (is more precisly then fps because
		 * we use integers
		 */
		public int fp1000s;
	};
	
	public Pattern()
	{
		Step step = new Step();
		stepList.add(step);
	}
	
	public Step getPreviousStep()
	{
		if (currentStepNum == 0) return null; 
		return stepList.get(currentStepNum-1);
	}

	public Step getNextStep()
	{
		if (currentStepNum == stepList.size()-1) return null; 
		return stepList.get(currentStepNum+1);
	}
	
	public Step getCurrentStep()
	{
		return stepList.get(currentStepNum);
	}
	
	public Step getStep(int stepNum)
	{
		return stepList.get(stepNum);
	}
	
	public int getStepLength()
	{
		return stepList.size();
	}
	
	public AnimationInfo getAnimationInfo(int fps)
	{
		AnimationInfo ti = new AnimationInfo();
		
		int bpm = getBarsPerMinute() * getTimeSignatureBars();
		int framesperbeat = fps * 60 / bpm;
		if (framesperbeat % 2 == 1) framesperbeat++; 
		int fp1000s = framesperbeat * bpm * 1000 / 60;
		
		ti.fp1000s = fp1000s;
		ti.framesperbeat = framesperbeat;
		
		return ti;
	}
	
	public Fraction getStepTempo(int stepNo)
	{
		Step step = getStep(stepNo);
		
		int nominator;
		int denominator;
		
		if (step.isQuick())
		{
			nominator = getBeatsPerSlowStep();
			denominator = timeSignatureBeats * 2;
		}
		else if (step.isSlow())
		{
			nominator = getBeatsPerSlowStep();
			denominator = timeSignatureBeats;
		}
		else
		{
			try
			{
				String duration = step.getDuration();
				String nominatorString = duration.substring(0,duration.indexOf('/'));
				String denominatorString = duration.substring(duration.indexOf('/')+1);
				nominator = Integer.parseInt(nominatorString);
				denominator = Integer.parseInt(denominatorString);
			} catch(Exception e)
			{
				nominator = 1;
				denominator = 4;
			}
		}
		return new Fraction(nominator,denominator);
	}
	
	public Step addStep()
	{
		Step step = new Step();
		Step lastStep = stepList.getLast();
		if (lastStep != null)
		{
			for (int i=0;i<lastStep.getNumberOfFeets();i++)
			{
				WayPoint destFeetCoord = step.getStartingWayPoint(i);
				WayPoint srcFeetCoord = lastStep.getStartingWayPoint(i);
				destFeetCoord.x = srcFeetCoord.x;
				destFeetCoord.y = srcFeetCoord.y;
				destFeetCoord.a = srcFeetCoord.a;
			}
		}
		
		stepList.add(step);
		currentStepNum = stepList.size() - 1;
		return step;
	}
	
	public void insertStep(Step step)
	{
		stepList.add(currentStepNum,step);
	}

	/**
	 * Method removeCurrentStep.
	 * @return boolean
	 */
	public boolean removeCurrentStep()
	{
		if (currentStepNum == 0) return false;
		stepList.remove(currentStepNum);
		if (currentStepNum >= stepList.size()) currentStepNum = stepList.size()-1;
		return true;
	}
	
	
	/**
	 * Returns the currentStep.
	 * @return int
	 */
	public int getCurrentStepNum()
	{
		return currentStepNum;
	}

	/**
	 * Sets the currentStep.
	 * @param currentStep The currentStep to set
	 */
	public void setCurrentStepNum(int currentStepNum)
	{
		this.currentStepNum = currentStepNum;
		emitEvent(0);
	}

	public void addWayPoint(int footNum, int wayPointIndex)
	{
		Step prevStep = getPreviousStep();
		if (prevStep == null) return;
		Step step = getCurrentStep();
		if (step == null) return;
		
		WayPoint prevFeetCoord;
		WayPoint thisFeetCoord;

		if (wayPointIndex == 0)
		{
			prevFeetCoord = prevStep.getFoot(footNum).getLastWayPoint();
			thisFeetCoord = step.getStartingWayPoint(footNum);
		} else
		{
			prevFeetCoord = prevStep.getFoot(footNum).getWayPoint(wayPointIndex-1);
			thisFeetCoord = prevStep.getFoot(footNum).getWayPoint(wayPointIndex);
		}
		
		WayPoint newFeetCoord = new WayPoint(
			(prevFeetCoord.x + thisFeetCoord.x)/2,
			(prevFeetCoord.y + thisFeetCoord.y)/2,0);
			
		/* if wayPointIndex equals 0 it means that the waypoint has
		 * to be inserted as last waypoint of the previous step */
		if (wayPointIndex == 0) wayPointIndex = prevStep.getFoot(footNum).getNumOfWayPoints();
		prevStep.getFoot(footNum).addWayPoint(newFeetCoord,wayPointIndex);
	}

	public void removeAllWayPoints(int footNum)
	{
		Step prevStep = getPreviousStep();
		if (prevStep == null) return;
		
		prevStep.getFoot(footNum).removeAllWayPoints();
	}

	/**
	 * Rotate the feet as specified in the toRotate field by the given angle around their center.
	 *
	 * @param angle
	 * @param toRotate
	 */
	public void rotateFeet(int angle, boolean [] toRotate)
	{
		int numSelected = 0;
		int rotationCenterX = 0;
		int rotationCenterY = 0;

		Step step = getCurrentStep();
		if (step == null) return;

		for (int i=0;i<toRotate.length;i++)
		{
			if (toRotate[i])
			{
				WayPoint feetCoord = step.getStartingWayPoint(i);
				rotationCenterX += feetCoord.x;
				rotationCenterY += feetCoord.y;
				numSelected++;
			}
		}

		if (numSelected == 0) return;

		rotationCenterX = (rotationCenterX) / numSelected;
		rotationCenterY = (rotationCenterY) / numSelected;

		for (int i=0;i<toRotate.length;i++)
		{
			if (toRotate[i])
			{
				WayPoint feetCoord = step.getStartingWayPoint(i);

				int px =  feetCoord.x - rotationCenterX;
				int py =  feetCoord.y - rotationCenterY;

				double cosa = cos(toRadians(-angle));
				double sina = sin(toRadians(-angle));

				double newx = (px * cosa + py * sina);
				double newy = (-px * sina + py * cosa);

				feetCoord.x = (int)round(newx) + rotationCenterX;
				feetCoord.y = (int)round(newy) + rotationCenterY;
				feetCoord.a += angle;
				if (feetCoord.a < 0) feetCoord.a += 360;
				if (feetCoord.a > 359) feetCoord.a -= 360;
			}
		}
	}

	public int [] getPatternBounds() 
	{
		int [] bounds = new int[4];
		bounds[0] = 0x7fff;
		bounds[3] = 0x7fff;

		for (int i=0;i<getStepLength();i++)
		{
			int [] newBounds = getStep(i).getStepBounds();
			if (newBounds[0] < bounds[0]) bounds[0] = newBounds[0];
			if (newBounds[1] > bounds[1]) bounds[1] = newBounds[1];
			if (newBounds[2] > bounds[2]) bounds[2] = newBounds[2];
			if (newBounds[3] < bounds[3]) bounds[3] = newBounds[3];
		}
		return bounds;
	}

	static private String [] parseLine(String line)
	{
		int arrayLen = 0;
		char [] inArray = line.toCharArray();
		
		for (int i=0;i<line.length();i++)
		{
			if (inArray[i] == '.') arrayLen++;
			if (inArray[i] == '=')
			{
				arrayLen++;
				break;
			}
		}

		String [] array = new String[arrayLen+1];

		String str = "";
		int j = 0;
		for (int i=0;i<line.length();i++)
		{
			if (inArray[i] == '.')
			{
				array[j++]=str;
				str="";
				continue;
			} 

			if (inArray[i] == '=')
			{
				array[j++]=str;
				array[j]=line.substring(i+1,line.length()).trim();
				break;
			}
			str += inArray[i];
		}

		return array;
	}
	
	static private boolean checkArg(String [] array, int num, String str)
	{
		if (array.length < num) return false;
		return str.equalsIgnoreCase(array[num]);
	}

	/**
	 * Return the info of a pattern serialized by the given string.
	 *
	 * @param str the serialized pattern.
	 *
	 * @return the pattern info
	 */
	static public PatternInfo getPatternInfo(String str)
	{
		PatternInfo pi = new PatternInfo();
		pi.name = "";
		
		int len = str.length();
		int pos = 0;
		
		while (pos < len)
		{
			String line;
			int endPos = str.indexOf("\n",pos);
			line = str.substring(pos,endPos);

			String [] array = parseLine(line);
			if (array != null)
			{
				if (checkArg(array,0,"type"))
				{
					for (int i=0;i<DANCE_MAX;i++)
					{
						if (typeNames[i].equalsIgnoreCase(array[1]))
						{
							pi.type = i;
							break;
						}
					}
				} else
				if (checkArg(array,0,"name"))
				{
					pi.name = array[1];
				}
			}
			pos = endPos + 1;
		}
		return pi;
	}
	
	static public Pattern fromString(String str)
	{
		Pattern pattern = new Pattern();
		int len = str.length();
		int pos = 0;
		
		while (pos < len)
		{
			String line;
			int endPos = str.indexOf("\n",pos);
			line = str.substring(pos,endPos);

			String [] array = parseLine(line);
			if (array != null)
			{
				if (checkArg(array,0,"step"))
				{
					int stepNo = Integer.parseInt(array[1]);
					while (pattern.stepList.size() <= stepNo)
						pattern.addStep();
					Step step = pattern.getStep(stepNo);
					
					if (checkArg(array,2,"feet"))
					{
						int feetNo = Integer.parseInt(array[3]);
						int arrayNum;
						WayPoint feetCoord;

						if (checkArg(array,4,"type"))
						{
							step.getFoot(feetNo).setType(Integer.parseInt(array[5]));
						}
						
						if (checkArg(array,4,"longRotation"))
						{
							if (array[5].equalsIgnoreCase("true")) step.getFoot(feetNo).setLongRotation(true);
							else step.getFoot(feetNo).setLongRotation(false);
						}
						if (checkArg(array,4,"wp"))
						{
							int waypointNo = Integer.parseInt(array[5]);
							Foot feet = step.getFoot(feetNo);
							while (feet.getNumOfWayPoints() <= waypointNo)
							{
								feet.addWayPoint(new WayPoint(0,0,0),feet.getNumOfWayPoints());
							}
							feetCoord = feet.getWayPoint(waypointNo);
							arrayNum = 6;
						} else
						{
							feetCoord = step.getStartingWayPoint(feetNo);
							arrayNum = 4;
						} 

						if (checkArg(array,arrayNum,"x"))
						{
							feetCoord.x = Integer.parseInt(array[arrayNum+1]);
						} else
						if (checkArg(array,arrayNum,"y"))
						{
							feetCoord.y = Integer.parseInt(array[arrayNum+1]);
						} else
						if (checkArg(array,arrayNum,"a"))
						{
							feetCoord.a = Integer.parseInt(array[arrayNum+1]);
						}
					} else
					{
						if (checkArg(array,2,"count"))
						{
							step.setCount(array[3]);
						} else
						if (checkArg(array,2,"measure"))
						{
							step.setDuration(array[3]);
						} else
						if (checkArg(array,2,"tempo"))
						{
							step.setDuration(array[3]);
						} else
						if (checkArg(array,2,"desc"))
						{
							String desc = step.getDescription();
							desc += array[3] + "\n";
							step.setDescription(desc);
						}
					}
				} else /* step */
				{
					if (checkArg(array,0,"type"))
					{
						for (int i=0;i<DANCE_MAX;i++)
						{
							if (typeNames[i].equalsIgnoreCase(array[1]))
							{
								pattern.type = i;
								break;
							}
						}
					} else
					if (checkArg(array,0,"name"))
					{
						pattern.name = array[1];
					} else
					if (checkArg(array,0,"barNominator"))
					{
						pattern.timeSignatureBars = Integer.parseInt(array[1]);
					} else
					if (checkArg(array,0,"barDenominator"))
					{
						pattern.timeSignatureBeats = Integer.parseInt(array[1]);
					} else
					if (checkArg(array,0,"barsPerMinute"))
					{
						pattern.barsPerMinute = Integer.parseInt(array[1]);
					} else
					if (checkArg(array,0,"beatsPerSlowStep"))
					{
						pattern.beatsPerSlowStep = Integer.parseInt(array[1]);
					}
				}
			}

			pos = endPos + 1;
			
		}
		pattern.currentStepNum = 0;
		return pattern;
	}
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("simpledance.version=1\n");
		buf.append("type="+typeNames[type]+"\n");
		buf.append("name="+name+"\n");
		buf.append("barNominator="+timeSignatureBars+"\n");
		buf.append("barDenominator="+timeSignatureBeats+"\n");
		buf.append("barsPerMinute="+barsPerMinute+"\n");
		buf.append("beatsPerSlowStep="+beatsPerSlowStep+"\n");

		for (int i=0;i<stepList.size();i++)
		{
			Step step = (Step)stepList.get(i);
			if (!step.getCount().equals(""))
			{
				buf.append("step."+i+".count="+step.getCount() + "\n");
			}
			if (!step.getDescription().equals(""))
			{
				char [] array = step.getDescription().toCharArray();
				boolean linefeed = true;
				for (int j=0;j<array.length;j++)
				{
					if (array[j]==10 || array[j]==13)
					{
						if (!linefeed)
						{
							buf.append("\n"); 
							linefeed = true;
						}
					} else
					{
						if (linefeed)
						{
							buf.append("step."+i+".desc=");
							linefeed = false;
						}
						buf.append(array[j]);
					}
				}
				if (!linefeed) buf.append("\n");
			}
			buf.append("step."+i+".tempo=" + step.getDuration() + "\n");
			for (int j=0;j<step.getNumberOfFeets();j++)
			{
				Foot foot = step.getFoot(j);
				WayPoint footCoord;

				footCoord = foot.getStartingWayPoint();
				buf.append("step."+i+".feet."+j+ ".x="+footCoord.x + "\n");
				buf.append("step."+i+".feet."+j+ ".y="+footCoord.y + "\n");
				buf.append("step."+i+".feet."+j+ ".a="+footCoord.a + "\n");
				buf.append("step."+i+".feet."+j+ ".type="+foot.getType() + "\n");
				buf.append("step."+i+".feet."+j+ ".longRotation="+foot.isLongRotation() + "\n");

				int maxWaypoints = foot.getNumOfWayPoints();
				
				for (int k=1;k<maxWaypoints;k++)
				{
					footCoord = foot.getWayPoint(k);
					buf.append("step."+i+".feet."+j+".wp." + k + ".x="+footCoord.x + "\n");
					buf.append("step."+i+".feet."+j+".wp." + k + ".y="+footCoord.y + "\n");
				}
			}
		}
		
		return buf.toString();
	}
	/**
	 * Returns the timeSignatureBeatNum.
	 * @return int
	 */
	public int getTimeSignatureBars()
	{
		return timeSignatureBars;
	}

	/**
	 * Returns the timeSignatureBeatType.
	 * @return int
	 */
	public int getTimeSignatureBeats()
	{
		return timeSignatureBeats;
	}

	/**
	 * Sets the timeSignatureBeatNum.
	 * @param timeSignatureBeatNum The timeSignatureBeatNum to set
	 */
	public void setTimeSignatureBars(int timeSignatureBeatNum)
	{
		this.timeSignatureBars = timeSignatureBeatNum;
	}

	/**
	 * Sets the timeSignatureBeatType.
	 * @param timeSignatureBeatType The timeSignatureBeatType to set
	 */
	public void setTimeSignatureBeats(int timeSignatureBeatType)
	{
		this.timeSignatureBeats = timeSignatureBeatType;
	}

	/**
	 * Returns the barsPerMinute.
	 * @return int
	 */
	public int getBarsPerMinute()
	{
		return barsPerMinute;
	}

	/**
	 * Sets the barsPerMinute.
	 * @param barsPerMinute The barsPerMinute to set
	 */
	public void setBarsPerMinute(int barsPerMinute)
	{
		this.barsPerMinute = barsPerMinute;
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

	/**
	 * Returns the beatsPerSlowStep.
	 * @return int
	 */
	public int getBeatsPerSlowStep()
	{
		return beatsPerSlowStep;
	}

	/**
	 * Sets the beatsPerSlowStep.
	 * @param beatsPerSlowStep The beatsPerSlowStep to set
	 */
	public void setBeatsPerSlowStep(int beatsPerSlowStep)
	{
		this.beatsPerSlowStep = beatsPerSlowStep;
	}

	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName()
	{
		if (name == null) return "";
		return name;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name)
	{
		this.name = new String(name);
	}

	/**
	 * Returns the filename.
	 * @return String
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 * Sets the filename.
	 * @param filename The filename to set
	 */
	public void setFilename(String filename)
	{
		this.filename = filename;
	}


	/* Notification/eventhandling */

	/** The list which keeps the listeners */
	private LinkedList<PatternListener> patternListenerList = new LinkedList<>();

	private void emitEvent(int type)
	{
		for (PatternListener pl : patternListenerList)
			pl.newStepActive(this,currentStepNum);
	}
	
	/**
	 * Adds a new pattern listener.
	 * 
	 * @param pn defines the listener object to add.
	 */
	public void addPatternListener(PatternListener pn)
	{
		patternListenerList.add(pn);
	}

	/**
	 * Removes a given pattern listener.
	 * 
	 * @param pn defines the listerner object which should be removed. The object
	 *         must have been added before via @see {addPatternListener}.
	 */
	public void removePatternListener(PatternListener pn)
	{
		patternListenerList.remove(pn);
	}
}
