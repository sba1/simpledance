package de.sonumina.simpledance.core.graphics;

import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import de.sonumina.simpledance.core.graphics.InputContext.Drag;
import de.sonumina.simpledance.core.model.Foot;
import de.sonumina.simpledance.core.model.Pattern;
import de.sonumina.simpledance.core.model.Step;
import de.sonumina.simpledance.core.model.WayPoint;

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
	public void dispose()
	{
		context.deallocateColor(femaleLeftColor);
		context.deallocateColor(femaleRightColor);
		context.deallocateColor(maleLeftColor);
		context.deallocateColor(maleRightColor);
		context.deallocateColor(darkGreyColor);
		context.deallocateColor(shineGreyColor);
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
		public Point rotationCenterBallroomPoint;
		public int feetIndex = -1;
		public FootPart feetPart = FootPart.NO;
		public int waypoint = -1;
		public int distance;
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
		public boolean showGrid;
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
		int visibleWidth = rsa.visibleLeftTop.distance(rsa.visibleRightTop);
		float scale = (float)rsa.pixelWidth / pixSize / (float)visibleWidth * ballroomSize;
		context.applyTranslationTransformation(transFeetCoord.x, transFeetCoord.y);
		context.applyRotateTransformation(-center.a);
		context.applyRotateTransformation(rsa.visibleRotation);
		context.applyScaleTransformation(scale);
		if (mirror) context.applyScaleXTransformation(-1.f);

		int [] transformedData = new int[data.length];
		context.applyTransformation(data, transformedData);

		context.popCurrentTransform();

		return new Point(tx, ty).isContainedIn(transformedData);
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
	 * Render the grid.
	 *
	 * @param rsa
	 */
	private void drawGrid(RenderSceneArgs rsa)
	{
		if (rsa.showGrid)
		{
			/* Draw the grid, this is very unoptimized */
			context.setForeground(gridColor);
			context.setLineStyle(LineStyle.DOT);

			for (int y = 1200; y > 0; y -= 50)
			{
				Point p1 = transformBallroomToPixel(rsa, 0, y);
				Point p2 = transformBallroomToPixel(rsa, 1200, y);
				context.drawLine(p1.x, p1.y, p2.x, p2.y);
			}

			for (int x = 0; x < 1200; x += 50)
			{
				Point p1 = transformBallroomToPixel(rsa, x, 1200);
				Point p2 = transformBallroomToPixel(rsa, x, 0);
				context.drawLine(p1.x, p1.y, p2.x, p2.y);

			}

			context.setLineStyle(LineStyle.NORMAL);
		}
	}

	/**
	 * Render the scene according to the given rsa
	 * 
	 * @param rsa
	 */
	public void renderScence(RenderSceneArgs rsa)
	{
		context.setBackground(ballroomColor);
		context.fillPolygon(new int[]
		{
			0, 0,
			rsa.pixelWidth-1, 0,
			rsa.pixelWidth-1, rsa.pixelHeight-1,
			0, rsa.pixelHeight-1
		});

		drawGrid(rsa);

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
				for (int i=0;i<previousStep.getNumberOfFeet();i++)
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
					for (int i=0;i<previousStep.getNumberOfFeet();i++)
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
		for (int i=0;i<step.getNumberOfFeet();i++)
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
	 * Transform current pixel coordinates to ballroom coordinates.
	 *
	 * @param x
	 * @param y
	 * @return Point
	 */
	public Point transformPixToBallroom(RenderSceneArgs rsa, int x, int y)
	{
		Point r = new Point(x * 100, y * 100).rotate(-rsa.visibleRotation);
		Point visible = rsa.visibleRightBottom.sub(rsa.visibleLeftTop).rotate(-rsa.visibleRotation);
		int visibleWidth = visible.x;
		int zoomFactor = rsa.pixelWidth * 100 / visibleWidth;
		x = r.x / zoomFactor + rsa.visibleLeftTop.x;
		y = - r.y / zoomFactor + rsa.visibleLeftTop.y;
		return new Point(x,y);
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

		for (int i=0;i<step.getNumberOfFeet();i++)
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

	static public class ZoomViewResult
	{
		public int visibleLeft;
		public int visibleTop;
		public int zoomFactor;
	}

	public ZoomViewResult zoomInView(RenderSceneArgs rsa)
	{
		ZoomViewResult zoomViewResult = zoomView(rsa, 3, 2);
		return zoomViewResult;
	}

	public ZoomViewResult zoomOutView(RenderSceneArgs rsa)
	{
		ZoomViewResult zoomViewResult = zoomView(rsa, 2, 3);
		return zoomViewResult;
	}

	private ZoomViewResult zoomView(RenderSceneArgs rsa, int current, int next) {
		ZoomViewResult zoomViewResult = new ZoomViewResult();

		Point center = rsa.visibleLeftTop.center(rsa.visibleRightBottom);
		Point direction = center.sub(rsa.visibleLeftTop).mult(next).div(current);
		Point newVisibleLeftTop = center.sub(direction);

		Point visible = rsa.visibleRightBottom.sub(rsa.visibleLeftTop).rotate(-rsa.visibleRotation);
		int visibleWidth = visible.x;
		int zoomFactor = rsa.pixelWidth * 100 / visibleWidth * current / next;
		if (zoomFactor < 25) zoomFactor = 25;

		zoomViewResult.visibleLeft = newVisibleLeftTop.x;
		zoomViewResult.visibleTop = newVisibleLeftTop.y;
		zoomViewResult.zoomFactor = zoomFactor;
		return zoomViewResult;
	}

	static public class RotateViewResult
	{
		public int visibleLeft;
		public int visibleTop;
		public int rotation;
	}

	/**
	 * Rotate the view and return the new state.
	 *
	 * @param rsa
	 * @param angle
	 * @return
	 */
	public RotateViewResult rotateView(RenderSceneArgs rsa, int angle)
	{
		RotateViewResult rvr = new RotateViewResult();
		Point center = rsa.visibleLeftTop.center(rsa.visibleRightBottom);
		Point newVisibleLeftTop = rsa.visibleLeftTop.rotate(angle, center);
		rvr.visibleLeft = newVisibleLeftTop.x;
		rvr.visibleTop = newVisibleLeftTop.y;
		rvr.rotation = rsa.visibleRotation + angle;
		return rvr;
	}

	/**
	 * The result of viewWholePattern().
	 *
	 * @author Sebastian Bauer
	 */
	static public class ViewWholePatternResult
	{
		public boolean valid;
		public int zoomFactor;
		public int visibleLeft;
		public int visibleTop;
	}

	/**
	 * Calculate the view settings such that the whole pattern that is specified in
	 * is visible. The rotation itself will be not be changed.
	 *
	 * @param rsa the input parameter that specifies among other thing which pattern to use.
	 *
	 * @return the new view settings.
	 */
	public ViewWholePatternResult viewWholePattern(RenderSceneArgs rsa)
	{
		ViewWholePatternResult result = new ViewWholePatternResult();

		if (rsa.pattern == null) return result;
		int bounds[] = rsa.pattern.getPatternBounds();

		Point leftTop = new Point(bounds[0] - 10, bounds[1] + 20);
		Point rightBottom = new Point(bounds[2] + 10, bounds[3] - 20);
		Point leftBottom = new Point(leftTop.x, rightBottom.y);
		Point rightTop = new Point(rightBottom.x, leftTop.y);

		/* Rotate */
		Point center = leftTop.center(rightBottom);
		leftTop = leftTop.rotate(rsa.visibleRotation, center);
		rightBottom = rightBottom.rotate(rsa.visibleRotation, center);
		leftBottom = leftBottom.rotate(rsa.visibleRotation, center);
		rightTop = rightTop.rotate(rsa.visibleRotation, center);

		/* Determine extent the current step covers after rotation */
		int maxX = max(max(max(leftTop.x, rightBottom.x), leftBottom.x), rightTop.x);
		int minX = min(min(min(leftTop.x, rightBottom.x), leftBottom.x), rightTop.x);
		int maxY = max(max(max(leftTop.y, rightBottom.y), leftBottom.y), rightTop.y);
		int minY = min(min(min(leftTop.y, rightBottom.y), leftBottom.y), rightTop.y);
		int visibleWidth = maxX -  minX + 1;
		int visibleHeight = maxY - minY + 1;

		int clientWidth = rsa.pixelWidth * 100;
		int clientHeight = rsa.pixelHeight * 100;

		/* Calculate the zoom factor such that we cover the full extent of the step */
		int zoomFactor = max(1, min(clientWidth / visibleWidth, clientHeight / visibleHeight));

		/* Determine the lengths within the ballroom in each dimension that is covered by the current
		 * render view in accordance to the rotation */
		Point ballroomRotated = new Point(clientWidth, clientHeight).rotate(-rsa.visibleRotation);
		int ballroomWidth = ballroomRotated.x / zoomFactor;
		int ballroomHeight = ballroomRotated.y / zoomFactor;

		/* Determine left top ballroom coordinates based on the center of the step. We move the center
		 * such that we hit the center of the render view */
		result.visibleLeft = center.x - ballroomWidth / 2;
		result.visibleTop = center.y + ballroomHeight / 2;

		/* Also return the zoom factor */
		result.zoomFactor = zoomFactor;

		/* Yes, and it is valid */
		result.valid = true;

		return result;
	}

	private int calculateBallroomAngle(int mx, int my, int x, int y)
	{
		int angle;

		if (x < mx)
		{
			if (my != y)
			{
				double t = (mx - x)/(double)(y - my);
				angle = (int)toDegrees(atan(t));
				if (my > y) angle = 180 + angle;
			} else angle = 90;
		} else
		if (x > mx)
		{
			if (my != y)
			{
				double t = (mx - x)/(double)(my - y);
				angle = (int)toDegrees(atan(t));
				if (my > y) angle = 180 - angle;
				else angle = 360 - angle;
			} else angle = 270;
		} else
		{
			if (y < my) angle = 180;
			else angle = 0;
		}

		if (angle >= 360) angle -= 360;
		else if (angle < 0) angle += 360 * ((-angle + 359)/360);

		return angle;
	}

	public boolean mouseDown(RenderSceneArgs rsa, InputContext inputContext, int x, int y)
	{
		final Pattern pattern = rsa.pattern;

		boolean rejectWayPointRequest = false;
		Step step = pattern.getCurrentStep();
		Step previousStep = pattern.getPreviousStep();

		inputContext.selectedFoot = -1;

		Render.CoordinateInfo ci = getPixCoordinateInfo(rsa, x, y, step);

		if (ci.feetIndex != -1)
		{
			if (ci.feetPart != Render.FootPart.NO || ci.waypoint == 0)
			{
				inputContext.selectedWaypoint = ci.waypoint;
				inputContext.selectedStep = pattern.getCurrentStepNum();
				inputContext.selectedFoot = ci.feetIndex;
				inputContext.dragOperation = ci.feetPart == Render.FootPart.BALE?Drag.ROTATE_BALE:Drag.ROTATE_HEEL;
				if (ci.waypoint != -1)	inputContext.dragOperation = Drag.MOVE_WAYPOINT;
				inputContext.distance = ci.distance;
				inputContext.rotationCenterBallroomPoint = ci.rotationCenterBallroomPoint;
			}
		} else
		{
			if (previousStep != null)
			{
				ci = getPixCoordinateInfo(rsa, x, y, previousStep);
				if (ci.waypoint > 0)
				{
					inputContext.selectedWaypoint = ci.waypoint;
					inputContext.selectedStep = pattern.getCurrentStepNum() - 1;
					inputContext.selectedFoot = ci.feetIndex;
					inputContext.dragOperation = Drag.MOVE_WAYPOINT;
				} else rejectWayPointRequest = true;
			}
		}

		if (ci.feetIndex != -1)
			inputContext.selectedArray[ci.feetIndex] = true;
		return rejectWayPointRequest;
	}

	public void mouseMove(RenderSceneArgs rsa, InputContext inputContext, int x, int y)
	{
		if (inputContext.selectedFoot != -1)
		{
			switch (inputContext.dragOperation)
			{
				case	ROTATE_BALE:
						{
							Point p = transformPixToBallroom(rsa, x, y);
							int angle = calculateBallroomAngle(
								inputContext.rotationCenterBallroomPoint.x,
								inputContext.rotationCenterBallroomPoint.y,
								p.x,p.y);
							WayPoint feetCoord = rsa.pattern.getStep(inputContext.selectedStep).getFoot(inputContext.selectedFoot).getStartingWayPoint();
							feetCoord.x = inputContext.rotationCenterBallroomPoint.x - (int)((inputContext.distance * sin(toRadians(angle))));
							feetCoord.y = inputContext.rotationCenterBallroomPoint.y + (int)((inputContext.distance * cos(toRadians(angle))));
							feetCoord.a = angle;
						}
						break;

				case	ROTATE_HEEL:
						{
							Point p = transformPixToBallroom(rsa, x, y );
							int angle = calculateBallroomAngle(
								inputContext.rotationCenterBallroomPoint.x,
								inputContext.rotationCenterBallroomPoint.y,
								p.x,p.y) - 180;
							if (angle < 0) angle += 360;
							WayPoint feetCoord = rsa.pattern.getStep(inputContext.selectedStep).getFoot(inputContext.selectedFoot).getStartingWayPoint();
							feetCoord.x = inputContext.rotationCenterBallroomPoint.x + (int)((inputContext.distance * sin(toRadians(angle))));
							feetCoord.y = inputContext.rotationCenterBallroomPoint.y - (int)((inputContext.distance * cos(toRadians(angle))));
							feetCoord.a = angle;
						}
						break;

				case	MOVE_WAYPOINT:
						{
							Point p = transformPixToBallroom(rsa, x, y);
							WayPoint feetCoord = rsa.pattern.getStep(inputContext.selectedStep).getFoot(inputContext.selectedFoot).getWayPoint(inputContext.selectedWaypoint);
							feetCoord.x = p.x;
							feetCoord.y = p.y;
						}
						break;

				default:
						break;
			}
		}
	}
}
