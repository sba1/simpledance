package de.sonumina.simpledance;

import static java.lang.Math.max;

import java.awt.Polygon;

import de.sonumina.simpledance.graphics.Color;
import de.sonumina.simpledance.graphics.Context;
import de.sonumina.simpledance.graphics.Point;
import de.sonumina.simpledance.graphics.RGB;

/**
 * This class is responsible for actually drawing the scene
 * on a given context.
 *
 * @author Sebastian Bauer
 */
public class Render
{
	private Color leftFeetColor;
	private Color leftFeetSelectedColor;
	private Color leftFeetBorderColor;
	private Color rightFeetColor;
	private Color rightFeetSelectedColor;
	private Color rightFeetBorderColor;
	private Color femaleLeftColor;
	private Color femaleRightColor;
	private Color maleLeftColor;
	private Color maleRightColor;
	private Color darkGreyColor;
	private Color shineGreyColor;
	private Color ballroomColor;
	private Color lineColor;
	private Color animationColor;
	private Color animationSelectedColor;
	private Color countColor;
	private Color gridColor;
	private Color yellowColor;
	private Color redColor;

	private Context context;

	/** Graphics data for male and femals feet */
	static private GraphicsData [] graphicsData = new GraphicsData []
	{
		new GraphicsData(0),
		new GraphicsData(1),
	};

	private GraphicsData getGraphicsData(Step step, int footNumber)
	{
		int graphicsNum;
		if (step.isFemaleFoot(footNumber)) graphicsNum = 1;
		else graphicsNum = 0;
		return graphicsData[graphicsNum];		
	}

	/**
	 * Construct a new render object with the context being the target
	 * of all operations.
	 *
	 * @param context
	 */
	public Render(Context context)
	{
		this.context = context;

		leftFeetColor = context.allocateColor(5,5,5);
		leftFeetSelectedColor = context.allocateColor(250,250,170);
		leftFeetBorderColor = context.allocateColor(15,15,15);

		rightFeetColor = context.allocateColor(5,5,5);
		rightFeetSelectedColor = context.allocateColor(80,80,0);
		rightFeetBorderColor = context.allocateColor(15,15,15);

		darkGreyColor = context.allocateColor(70,70,70); 
		shineGreyColor = context.allocateColor(180,180,180);
		ballroomColor = context.allocateColor(240,240,200);
		
		lineColor = context.allocateColor(200,200,0);
		
		yellowColor = context.allocateColor(240,240,0);
		redColor = context.allocateColor(240,0,0);

		animationColor = context.allocateColor(200,200,54);
		animationSelectedColor = context.allocateColor(200,200,54);
		gridColor = context.allocateColor(0,0,0);

		countColor = context.allocateColor(255,255,255);

		femaleLeftColor = context.allocateColor(244,231,240);
		femaleRightColor = context.allocateColor(244,61,195);
		maleLeftColor = context.allocateColor(228,229,240);
		maleRightColor = context.allocateColor(61,78,240);
	}
	
	/**
	 * Free all resources
	 */
	void dispose()
	{
		context.deallocateColor(femaleLeftColor);
		context.deallocateColor(femaleRightColor);
		context.deallocateColor(maleLeftColor);
		context.deallocateColor(maleRightColor);
		context.deallocateColor(leftFeetColor);
		context.deallocateColor(darkGreyColor);
		context.deallocateColor(shineGreyColor);
		context.deallocateColor(rightFeetColor);
		context.deallocateColor(ballroomColor);
		context.deallocateColor(lineColor);
		context.deallocateColor(animationColor);
		context.deallocateColor(animationSelectedColor);
		context.deallocateColor(yellowColor);
		context.deallocateColor(redColor);
		context.deallocateColor(gridColor);
		context.deallocateColor(countColor);
//		context.deallocateColor(countFont);
		context.deallocateColor(leftFeetSelectedColor);
		context.deallocateColor(leftFeetColor);
		context.deallocateColor(leftFeetBorderColor);
		context.deallocateColor(rightFeetSelectedColor);
		context.deallocateColor(rightFeetColor);
		context.deallocateColor(rightFeetBorderColor);
	}

	static public enum FootPart
	{
		NO,
		BALE,
		HEEL
	}

	static public class CoordinateInfo
	{
		Point rotationCenterBallroomPoint;
		int feetIndex = -1;
		FootPart feetPart = FootPart.NO;
		int waypoint = -1;
		int distance;
	};

	static public class RenderSceneArgs
	{
		/** Coordinates of the ballroom coord system */
		public Point visibleLeftTop;
		public Point visibleRightTop;
		public Point visibleLeftBottom;
		public Point visibleRightBottom;
		public int visibleRotation;

		public int pixelWidth;
		public int pixelHeight;

		public Pattern pattern;
		public int stepNumber;

		public boolean showGradients;
		public boolean showPrevStep;
		public boolean insideAnimation;
		public boolean showLady;
		public boolean showGent;
		public boolean showAnimationOutline;
		public boolean selectedArray[] = new boolean[4];
		public int animationMaxNumber;
		public int animationNumber;
	};

	/**
	 * Transform given ballroom waypoint to a waypoint representing pixel positions.
	 *
	 * @param rsa
	 * @param feetCoord
	 * @return
	 */
	private WayPoint transformBallroomToPixel(RenderSceneArgs rsa, WayPoint feetCoord)
	{
		Point p = transformBallroomToPixel(rsa, feetCoord.x, feetCoord.y);
		return new WayPoint(p.x,  p.y,  feetCoord.a);
	}

	/**
	 * Transform given ballroom coordinates to the actual view coordinates.
	 *
	 * @param rsa
	 * @param x
	 * @param y
	 * @return
	 */
	private Point transformBallroomToPixel(RenderSceneArgs rsa, int x, int y)
	{
		int visibleWidth = rsa.visibleLeftTop.distance(rsa.visibleRightTop);
		int visibleHeight = rsa.visibleLeftTop.distance(rsa.visibleLeftBottom);

		Point p = new Point(x, y);

		/* Avoid division by zero */
		visibleWidth = max(1, visibleWidth);
		visibleHeight = max(1, visibleHeight);

		/* Apply the view rotation */
		Point pRotated = p.rotate(-rsa.visibleRotation, rsa.visibleLeftBottom);

		/* Scale and consider the fact that y is mirrored */
		x = (pRotated.x - rsa.visibleLeftBottom.x) * rsa.pixelWidth / visibleWidth;
		y = (visibleHeight - (pRotated.y - rsa.visibleLeftBottom.y)) * rsa.pixelHeight / visibleHeight;
		return new Point(x,y);
	}

	/**
	 * 
	 * @param x0
	 * @param y0
	 * @param angle
	 * @param px defines the x coordinate of the to be transformed point
	 * @param py defines the y coordinate of the to be transformed point
	 * @return
	 */
	private Point transformCoords(int x0, int y0, int angle, int px, int py)
	{
		int x = x0;
		int y = y0;
		int a = angle;

		double cosa = Math.cos(Math.toRadians(a));
		double sina = Math.sin(Math.toRadians(a));
		
		int newx = (int)(px * cosa - py * sina) + x;
		int newy = (int)(-px * sina + py * cosa) + y;
		Point p = new Point(newx,newy);
		return p;
	}

    private void myDrawPolygon(RenderSceneArgs rsa, WayPoint feetCoord, boolean mirror, int [] data, int pixSize, int ballroomSize, boolean closed)
    {
    	context.pushCurrentTransform();

    	WayPoint transFeetCoord = transformBallroomToPixel(rsa, feetCoord);
		int visibleWidth = rsa.visibleLeftTop.distance(rsa.visibleRightTop);
		float scale = (float)rsa.pixelWidth / pixSize / (float)visibleWidth * ballroomSize;
    	context.applyTranslationTransformation(transFeetCoord.x, transFeetCoord.y);
    	context.applyRotateTransformation(-feetCoord.a);
    	context.applyRotateTransformation(rsa.visibleRotation);
    	context.applyScaleTransformation(scale);
    	if (mirror) context.applyScaleXTransformation(-1.f);

    	if (closed) context.drawPolygon(data);
    	else context.drawPolyline(data);

    	context.popCurrentTransform();
    }

    private void myFillPolygon(RenderSceneArgs rsa, WayPoint feetCoord, boolean mirror, int [] data, int pixSize, int ballroomSize)
	{
    	context.pushCurrentTransform();

    	WayPoint transFeetCoord = transformBallroomToPixel(rsa, feetCoord);
		int visibleWidth = rsa.visibleLeftTop.distance(rsa.visibleRightTop);
		float scale = (float)rsa.pixelWidth / pixSize / (float)visibleWidth * ballroomSize;
    	context.applyTranslationTransformation(transFeetCoord.x, transFeetCoord.y);
    	context.applyRotateTransformation(-feetCoord.a);
    	context.applyRotateTransformation(rsa.visibleRotation);
    	context.applyScaleTransformation(scale);
    	if (mirror) context.applyScaleXTransformation(-1.f);

    	context.fillPolygon(data);

    	context.popCurrentTransform();
	}

	private void myGradientPolygon(RenderSceneArgs rsa, RGB startRGB, RGB endRGB, WayPoint feetCoord, boolean mirror, int [] data, int pixSize, int ballroomSize)
	{
    	context.pushCurrentTransform();

    	WayPoint transFeetCoord = transformBallroomToPixel(rsa, feetCoord);
		int visibleWidth = rsa.visibleLeftTop.distance(rsa.visibleRightTop);
		float scale = (float)rsa.pixelWidth / pixSize / (float)visibleWidth * ballroomSize;
    	context.applyTranslationTransformation(transFeetCoord.x, transFeetCoord.y);
    	context.applyRotateTransformation(-feetCoord.a);
    	context.applyRotateTransformation(rsa.visibleRotation);
    	context.applyScaleTransformation(scale);
    	if (mirror) context.applyScaleXTransformation(-1.f);

		context.gradientPolygon(data,startRGB,endRGB,feetCoord.a);

    	context.popCurrentTransform();
	}
	
	private void myDrawText(RenderSceneArgs rsa, WayPoint feetCoord, String text)
	{
		feetCoord = transformBallroomToPixel(rsa,feetCoord);
		int x = feetCoord.x;
		int y = feetCoord.y;
		
		context.setFont(null);

		x -= context.stringExtent(text).x/2;
		y -= context.stringExtent(text).y/2;
		
		context.drawText(text,x,y,true);
	}

	/**
	 * Draws an oval around ballroomCoord
	 * 
	 * @param rsa
	 * @param ballroomCoord
	 * @param mx (in ballroom coordinates)
	 * @param my (in ballroom coordinates)
	 */
    private void myDrawOval(RenderSceneArgs rsa, WayPoint ballroomCoord, int mx, int my)
    {
    	Point p = transformCoords(ballroomCoord.x,ballroomCoord.y,ballroomCoord.a,mx,my);
    	p = transformBallroomToPixel(rsa,p.x,p.y);
		context.drawOval(p.x,p.y,2,2);
    }


	/**
	 * Checks whether the given pixel coordinates (tx, ty)  are  in a given polygon subject to specified coordinate transformation.
	 *
	 * @param rsa render scene arguments.
	 * @param center defines the center of the shape in ballroom space.
	 * @param mirror whether the polygon should be additionally mirrored.
	 * @param data the data that defines the polygon.
	 * @param pixSize the size of the object in pixels (e.g., the y extend)
	 * @param ballroomSize the equivalent size of the object in ballroom space.
	 * @param tx the x part of the location to check
	 * @param ty the y part of the location to check
	 * @return whether (tx,ty) is inside the polygon.
	 */
	private boolean myPolygonTest(RenderSceneArgs rsa, WayPoint center, boolean mirror, int [] data, int pixSize, int ballroomSize, int tx, int ty)
	{
		context.pushCurrentTransform();

		WayPoint transFeetCoord = transformBallroomToPixel(rsa, center);
		int visibleLeft = rsa.visibleLeftTop.x;
		int visibleWidth = rsa.visibleRightBottom.x - visibleLeft + 1;
		float scale = (float)rsa.pixelWidth / pixSize / (float)visibleWidth * ballroomSize;
		context.applyTranslationTransformation(transFeetCoord.x, transFeetCoord.y);
		context.applyRotateTransformation(-center.a);
		context.applyScaleTransformation(scale);
		if (mirror) context.applyScaleXTransformation(-1.f);

		int [] transformedData = new int[data.length];
		context.applyTransformation(data, transformedData);

		context.popCurrentTransform();

		Polygon polygon = new Polygon();
		for (int i=0; i<data.length; i+=2)
			polygon.addPoint(transformedData[i], transformedData[i+1]);
		return polygon.contains(tx,ty);
	}

	/**
	 * Tests whether a given point (px, py) realtive to a given center is nearby by a
	 * point (tx, ty) that given in the view space.
	 *
	 * @param rsa
	 * @param center
	 * @param px
	 * @param py
	 * @param tx
	 * @param ty
	 * @return
	 */
	private boolean myPointRangeTest(RenderSceneArgs rsa, WayPoint center, int px, int py, int tx, int ty)
	{
		Point p = transformCoords(center.x, center.y, center.a, px, py);
		p = transformBallroomToPixel(rsa, p.x, p.y);

		if (Math.abs(p.x - tx) < 5 && Math.abs(p.y - ty) < 5)
			return true;

		return false;
	}

	/**
	 * Draw a given foot
	 * 
	 * @param rsa
	 * @param step
	 * @param pixelCoord
	 * @param footNumber
	 * @param isSelected
	 */
	private void drawFoot(RenderSceneArgs rsa, Step step, WayPoint ballroomCoord, int footNumber, boolean isSelected)
	{
		boolean fillBale = true;
		boolean fillHeel = true;
		boolean showGradients = rsa.showGradients;

		Color backgroundColor;
		Color borderColor;
		if (step.isFeetLeft(footNumber))
		{
			if (isSelected) backgroundColor = leftFeetSelectedColor;
			else backgroundColor = leftFeetColor;
			borderColor = leftFeetBorderColor;
		} 
		else
		{
			if (isSelected) backgroundColor = rightFeetSelectedColor;
			else backgroundColor = rightFeetColor;
			borderColor = rightFeetBorderColor;
		} 
			
		GraphicsData graphicsData = getGraphicsData(step,footNumber);

		int lw = context.getLineWidth();
		int type = step.getFoot(footNumber).getType();

		if (type == Foot.STAND_ON_FOOT)
		{
			context.setForeground(yellowColor);
			context.setLineWidth(2);
		} else
		{
			context.setForeground(borderColor);
		}

		if (type == Foot.BALL_STEP_STAY || type == Foot.BALL_STAY || type == Foot.TAP)
		{
			fillBale = false;
			fillHeel = false;
		}

		context.setBackground(backgroundColor);
		
		Color heelColor;
		
		if (step.isFemaleFoot(footNumber))
		{
			if (step.isFeetLeft(footNumber)) heelColor = femaleLeftColor;
			else heelColor = femaleRightColor;
		} else
		{
			if (step.isFeetLeft(footNumber)) heelColor = maleLeftColor;
			else heelColor = maleRightColor;
		}

		context.setBackground(heelColor);
		if (fillBale)
		{
			if (showGradients) myGradientPolygon(rsa,heelColor.getRGB(),new RGB(0,0,0),ballroomCoord,step.isFeetLeft(footNumber),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize);
			else myFillPolygon(rsa,ballroomCoord,step.isFeetLeft(footNumber),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize);
		} 
		myDrawPolygon(rsa,ballroomCoord,step.isFeetLeft(footNumber),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize,true);

		context.setBackground(heelColor);
		if (fillHeel) myFillPolygon(rsa,ballroomCoord,step.isFeetLeft(footNumber),graphicsData.heelData,graphicsData.feetDataYSize,graphicsData.realYSize);
		myDrawPolygon(rsa,ballroomCoord,step.isFeetLeft(footNumber),graphicsData.heelData,graphicsData.feetDataYSize,graphicsData.realYSize,true);

		if (type == Foot.BALL_STEP || type == Foot.BALL_STEP_STAY || type == Foot.BALL_STAY)
		{
			if (type != Foot.BALL_STAY) 
			{
				context.setForeground(redColor);
				context.setLineWidth(2);
			}
			if (type != Foot.BALL_STEP) myFillPolygon(rsa,ballroomCoord,step.isFeetLeft(footNumber),graphicsData.getBale(),graphicsData.feetDataYSize,graphicsData.realYSize);
			myDrawPolygon(rsa,ballroomCoord,step.isFeetLeft(footNumber),graphicsData.getBale(),graphicsData.feetDataYSize,graphicsData.realYSize,false);
		}
		
		if (type == Foot.HEEL_STEP)
		{
			context.setForeground(redColor);
			context.setLineWidth(2);
			myDrawPolygon(rsa,ballroomCoord,step.isFeetLeft(footNumber),graphicsData.getHeel(),graphicsData.feetDataYSize,graphicsData.realYSize,false);
		}
		
		if (type == Foot.TAP)
		{
			context.setBackground(redColor);
			context.setLineWidth(2);
			myFillPolygon(rsa,ballroomCoord,step.isFeetLeft(footNumber),graphicsData.getBaleTap(),graphicsData.feetDataYSize,graphicsData.realYSize);
		}

		context.setLineWidth(lw);

		context.setForeground(borderColor);
		if (step.isFeetLeft(footNumber)) myDrawText(rsa,ballroomCoord,"L");
		else myDrawText(rsa,ballroomCoord,"R");

		myDrawOval(rsa,ballroomCoord,0,0);
		int ballroomBaleX = graphicsData.baleX * graphicsData.realYSize / graphicsData.feetDataYSize; 
		int ballroomBaleY = -graphicsData.baleY * graphicsData.realYSize / graphicsData.feetDataYSize; 
		int ballroomHeelX = graphicsData.heelX * graphicsData.realYSize / graphicsData.feetDataYSize; 
		int ballroomHeelY = -graphicsData.heelY * graphicsData.realYSize / graphicsData.feetDataYSize; 
		myDrawOval(rsa,ballroomCoord,ballroomBaleX,ballroomBaleY);
		myDrawOval(rsa,ballroomCoord,ballroomHeelX,ballroomHeelY);
	}
	
	/**
	 * Render the scene according to the given rsa
	 * 
	 * @param rsa
	 */
	public void renderScence(RenderSceneArgs rsa)
	{
		Pattern pattern = rsa.pattern;
		if (pattern == null) return;

		Step step = pattern.getStep(rsa.stepNumber);
		if (step == null) return;
		
		Step previousStep;
		if (rsa.stepNumber > 0) previousStep = pattern.getStep(rsa.stepNumber-1);
		else previousStep = null;
		
		Step nextStep;
		if (rsa.stepNumber < pattern.getStepLength()-1) nextStep = pattern.getStep(rsa.stepNumber+1);
		else nextStep = null;

		/* Show the previous step if not inside an animation */
		if (previousStep != null && !rsa.insideAnimation)
		{
			if (rsa.showPrevStep)
			{
				for (int i=0;i<previousStep.getNumberOfFeets();i++)
				{
					if (previousStep.isFemaleFoot(i) && !rsa.showLady) continue;
					if (!previousStep.isFemaleFoot(i) && !rsa.showGent) continue;

					GraphicsData graphicsData = getGraphicsData(step,i);
					WayPoint wayPoint = previousStep.getStartingWayPoint(i); 
		
					if (previousStep.getFoot(i).isLeft()) context.setBackground(shineGreyColor);
					else context.setBackground(darkGreyColor);

					myFillPolygon(rsa,wayPoint,step.isFeetLeft(i),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize);
					myFillPolygon(rsa,wayPoint,step.isFeetLeft(i),graphicsData.heelData,graphicsData.feetDataYSize,graphicsData.realYSize);
				}
			}

			/* Show the animation outline */
			if (rsa.showAnimationOutline)
			{
				for (int j=1;j<6;j++)
				{
					for (int i=0;i<previousStep.getNumberOfFeets();i++)
					{
						if (previousStep.isFemaleFoot(i) && !rsa.showLady) continue;
						if (!previousStep.isFemaleFoot(i) && !rsa.showGent) continue;

						if (rsa.selectedArray[i]) context.setForeground(animationSelectedColor);
						else context.setForeground(animationColor);

 						GraphicsData graphicsData = getGraphicsData(previousStep,i);
						WayPoint feetCoord = previousStep.getFoot(i).getInterpolatedWayPoint(step.getStartingWayPoint(i),step.getFoot(i).isLongRotation(),j,6);

						myDrawPolygon(rsa,feetCoord,previousStep.isFeetLeft(i),graphicsData.heelData,graphicsData.feetDataYSize,graphicsData.realYSize, true);
						myDrawPolygon(rsa,feetCoord,previousStep.isFeetLeft(i),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize, true);
					}
				}
			}
		}

		/* Show every foot */
		for (int i=0;i<step.getNumberOfFeets();i++)
		{
			boolean isSelected = rsa.selectedArray[i];

			if (step.isFemaleFoot(i) && !rsa.showLady) continue;
			if (!step.isFemaleFoot(i) && !rsa.showGent) continue;

			WayPoint ballroomCoord;
			
			/* If inside an animation interpolate the position of the foot */
			if (rsa.insideAnimation && nextStep != null)
				ballroomCoord = step.getFoot(i).getInterpolatedWayPoint(nextStep.getStartingWayPoint(i),nextStep.getFoot(i).isLongRotation(),rsa.animationNumber,rsa.animationMaxNumber);
			else
				ballroomCoord = step.getStartingWayPoint(i);
			
			drawFoot(rsa,step,ballroomCoord,i,isSelected);
			
			/* Draw the move line of the foot */
			if (rsa.showPrevStep && previousStep != null && !rsa.insideAnimation)
			{
				context.setForeground(lineColor);
				ballroomCoord = previousStep.getStartingWayPoint(i);
				Point p1 = transformBallroomToPixel(rsa,ballroomCoord.x,ballroomCoord.y);
				Point p2;
				WayPoint wayPoint;
				
				for (int k=1;k<previousStep.getFoot(i).getNumOfWayPoints();k++)
				{
					wayPoint = previousStep.getFoot(i).getWayPoint(k);
					p2 = transformBallroomToPixel(rsa,wayPoint.x,wayPoint.y);
					context.drawLine(p1.x,p1.y,p2.x,p2.y);
					context.drawOval(p1.x-1,p1.y-1,2,2);
					p1 = p2;
				}
				wayPoint = step.getFoot(i).getStartingWayPoint();
				p2 = transformBallroomToPixel(rsa,wayPoint.x,wayPoint.y);
				context.drawLine(p1.x,p1.y,p2.x,p2.y);
				context.drawOval(p1.x-1,p1.y-1,2,2);
			}
		}
	}

	/**
	 * Return the coordinate info for a point given in view space.
	 *
	 * @param rsa
	 * @param x
	 * @param y
	 * @param step
	 * @return
	 */
	public CoordinateInfo getPixCoordinateInfo(RenderSceneArgs rsa, int x, int y, Step step)
	{
		CoordinateInfo ci = new CoordinateInfo();

		for (int i=0;i<step.getNumberOfFeets();i++)
		{
			GraphicsData graphicsData = getGraphicsData(step,i);
			WayPoint feetCoord = step.getStartingWayPoint(i);

			int ballroomBaleX = graphicsData.baleX * graphicsData.realYSize / graphicsData.feetDataYSize;
			int ballroomBaleY = -graphicsData.baleY * graphicsData.realYSize / graphicsData.feetDataYSize;
			int ballroomHeelX = graphicsData.heelX * graphicsData.realYSize / graphicsData.feetDataYSize;
			int ballroomHeelY = -graphicsData.heelY * graphicsData.realYSize / graphicsData.feetDataYSize;

			if (myPolygonTest(rsa, feetCoord,step.isFeetLeft(i),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize,x,y))
			{
				if (myPointRangeTest(rsa, feetCoord, ballroomBaleX, ballroomBaleY, x, y))
				{
					Point p = transformCoords(feetCoord.x, feetCoord.y, feetCoord.a, ballroomHeelX, ballroomHeelY);
					Point p2 = transformCoords(feetCoord.x, feetCoord.y, feetCoord.a, 0, 0);

					p2.x -= p.x;
					p2.y -= p.y;

					ci.feetPart = FootPart.BALE;
					ci.rotationCenterBallroomPoint = p;
					ci.distance = Math.abs(ballroomBaleY);
					ci.feetIndex = i;
					break;
				}
			} else
			if (myPolygonTest(rsa, feetCoord,step.isFeetLeft(i),graphicsData.heelData,graphicsData.feetDataYSize,graphicsData.realYSize,x,y))
			{
				if (myPointRangeTest(rsa, feetCoord,ballroomHeelX,ballroomHeelY,x,y))
				{
					Point p = transformCoords(feetCoord.x, feetCoord.y, feetCoord.a, ballroomBaleX, ballroomBaleY);
					Point p2 = transformCoords(feetCoord.x, feetCoord.y, feetCoord.a, 0, 0);

					p2.x -= p.x;
					p2.y -= p.y;

					ci.feetPart = FootPart.HEEL;
					ci.rotationCenterBallroomPoint = p;
					ci.distance = Math.abs(ballroomHeelY);
					ci.feetIndex = i;
					break;
				}
			}

			for (int j=0;j<step.getFoot(i).getNumOfWayPoints();j++)
			{
				WayPoint waypoint = step.getFoot(i).getWayPoint(j);

				if (myPointRangeTest(rsa, waypoint, 0, 0, x, y))
				{
					ci.feetIndex = i;
					ci.waypoint = j;
					break;
				}
			}
		}

		return ci;
	}
}
