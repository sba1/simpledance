import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.*;

import Pattern.AnimationInfo;

import gnu.gettext.GettextResource;

import java.awt.Polygon;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ResourceBundle;

/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Ballroom extends Canvas
{
	// *** BEGIN I18N
	static private ResourceBundle localeResource;
	static
	{
		try
		{
			localeResource = GettextResource.getBundle("SimpleDanceBundle");
		} catch(Exception e){};
	};
	static final private String _(String str)
	{
		if (localeResource == null) return str;
		return GettextResource.gettext(localeResource,str);
	}
	// *** END I18N

	static final int DRAG_NO = 0;
	static final int DRAG_ROTATE_BALE = 1;
	static final int DRAG_ROTATE_HEEL = 2;
	static final int DRAG_MOVE_WAYPOINT = 3;

	static final int FEETPART_NO = 0;
	static final int FEETPART_BALE = 1;
	static final int FEETPART_HEEL = 2;

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
	private Cursor cursor;
	private Menu contextMenu;
	private Font countFont;

	private int bufferWidth = -1;
	private int bufferHeight = -1;
	private Image bufferImage;
	private GC bufferGC;

	private Pattern pattern;
	private int zoomFactor = 300;
	private int zoomLeft = 570;
	private int zoomTop = 650;
	private int coordinatesX = -1;
	private int coordinatesY = -1;
	private boolean showAnimation = false;
	private boolean showNextStep = false;
	private boolean showPrevStep = false;
	private boolean showGent = true;
	private boolean showLady = true;
	private boolean showCoordinates = true;
	private boolean showGrid = true;

	class CoordinateInfo
	{
		Point rotationCenterBallroomPoint;
		int feetIndex = -1;
		int feetPart;
		int waypoint = -1;
		int distance;
	};

	private boolean mousePressed = false;
	private Point rotationCenterBallroomPoint;
	private Point rotationCenterPixelPoint;
	private int distance;
	private int pixelDistance;
	private int dragOperation;
	private int contextFeetIndex;
	private int contextStepIndex;
	private int contextWayPoint;

	private int lastSelectedStepIndex;
	private int lastSelectedFootIndex;
	private int lastSelectedWaypoint;
	
	private boolean [] selectedArray = new boolean[4];
	
	/* attributes used when painting */
	private boolean animation;
	private boolean animationSingleStep;
	private boolean animationBackward;
	private int animationDrawnSteps;
	private int animationNumber;
	private int animationMaxNumber;

	static private GraphicsData [] graphicsData = new GraphicsData []
	{
		new GraphicsData(0),
		new GraphicsData(1),
	};
	
	private LinkedList ballroomListenerList = new LinkedList();

	public int calculateBallroomAngle(int mx, int my, int x, int y)
	{
		int angle;
		
		if (x < mx)
		{
			if (my != y)
			{
				double t = (mx - x)/(double)(y - my);
				angle = (int)Math.toDegrees(Math.atan(t));
				if (my > y) angle = 180 + angle; 				
			} else angle = 90;
		} else
		if (x > mx)
		{
			if (my != y)
			{
				double t = (mx - x)/(double)(my - y);
				angle = (int)Math.toDegrees(Math.atan(t));
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

	public int calculatePixelAngle(int mx, int my, int x, int y)
	{
		int angle = 180 - calculateBallroomAngle(mx,my,x,y);
		if (angle >= 360) angle -= 360;
		else if (angle < 0) angle += 360 * ((-angle + 359)/360);
		return angle;
	}
	
	GraphicsData getGraphicsData(Step step, int stepNumber)
	{
		int graphicsNum;
		if (step.isFeetFemale(stepNumber)) graphicsNum = 1;
		else graphicsNum = 0;
		return graphicsData[graphicsNum];		
	}
	
	WayPoint transformFeedCoordToPix(WayPoint feetCoord)
	{
		int x = (feetCoord.x - zoomLeft) * zoomFactor / 100; // * 6
		int y = (zoomTop - feetCoord.y) * zoomFactor / 100; // * 6;
		int a = feetCoord.a;

		WayPoint newFeetCoord = new WayPoint(x,y,a);
		return newFeetCoord;
	}
	
	/**
	 * This transforms the given coordinates relative to the middle point
	 * of the given feetCoord coordinate system  
	 * 	 * @param feetCoord	 * @param px	 * @param py	 * @return Point	 */
	Point transformCoords(WayPoint feetCoord, int px, int py)
	{
		int x = feetCoord.x;
		int y = feetCoord.y;
		int a = feetCoord.a;

		double cosa = Math.cos(Math.toRadians(a));
		double sina = Math.sin(Math.toRadians(a));
		
		int newx = (int)(px * cosa - py * sina) + x;
		int newy = (int)(-px * sina + py * cosa) + y;
		Point p = new Point(newx,newy);
		return p;
	}
	
	/**
	 * Transform current pixel coordinates to ballroom coordinates
	 * 	 * @param x	 * @param y	 * @return Point	 */
	Point transformPixToBallroom(int x, int y)
	{
		x = x * 100 / zoomFactor + zoomLeft;
		y = - y * 100 / zoomFactor + zoomTop;
		return new Point(x,y);
	}
	
	
	/**
	 * Transforms ballroom Coordinates to pixel coordinates
	 * 	 * @param x	 * @param y	 * @return Point	 */
	Point transformBallroomToPix(int x, int y)
	{
		x = (x - zoomLeft) * zoomFactor / 100;
		y = (zoomTop - y) * zoomFactor / 100;
		return new Point(x,y);
	}
	
	public CoordinateInfo getPixCoordinateInfo(int x, int y, Step step)
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

			if (myPolygonTest(feetCoord,step.isFeetLeft(i),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize,x,y))
			{
				if (myPointRangeTest(feetCoord,ballroomBaleX,ballroomBaleY,x,y))
				{
					Point p = transformCoords(feetCoord,ballroomHeelX,ballroomHeelY);
					Point p2 = transformCoords(feetCoord,0,0);

					p2.x -= p.x;
					p2.y -= p.y;

					ci.feetPart = FEETPART_BALE;
					ci.rotationCenterBallroomPoint = p;
					ci.distance = Math.abs(ballroomBaleY);
					ci.feetIndex = i;
					break;
				}
			} else
			if (myPolygonTest(feetCoord,step.isFeetLeft(i),graphicsData.heelData,graphicsData.feetDataYSize,graphicsData.realYSize,x,y))
			{
				if (myPointRangeTest(feetCoord,ballroomHeelX,ballroomHeelY,x,y))
				{
					Point p = transformCoords(feetCoord,ballroomBaleX,ballroomBaleY);
					Point p2 = transformCoords(feetCoord,0,0);

					p2.x -= p.x;
					p2.y -= p.y;

					ci.feetPart = FEETPART_HEEL;
					ci.rotationCenterBallroomPoint = p;
					ci.distance = Math.abs(ballroomHeelY);
					ci.feetIndex = i;
					break;
				}
			}

			for (int j=0;j<step.getFeet(i).getNumOfWayPoints();j++)
			{
				WayPoint waypoint = step.getFeet(i).getFeetCoord(j);
					
				if (myPointRangeTest(waypoint,0,0,x,y))
				{
					ci.feetIndex = i;
					ci.waypoint = j;
					break;
				}
			}
		}

		return ci;
	}

	private void createImageBuffer()
	{
		if (bufferImage != null)
		{
			if (bufferWidth != getClientArea().width || 
				bufferHeight != getClientArea().height)
			{
				bufferGC.dispose();
				bufferGC = null;
				bufferImage.dispose();
				bufferImage = null;
			}
		}
				
		if (bufferImage == null)
		{
			bufferImage = new Image(getDisplay(),getClientArea().width,getClientArea().height);
			bufferGC = new GC(bufferImage);
			bufferWidth = getClientArea().width;
			bufferHeight = getClientArea().height;
		}
	} 
	
	
	
	public void dispose()
	{
		super.dispose();

		femaleLeftColor.dispose();
		femaleRightColor.dispose();
		maleLeftColor.dispose();
		maleRightColor.dispose();
		leftFeetColor.dispose();
		darkGreyColor.dispose();
		shineGreyColor.dispose();
		rightFeetColor.dispose();
		ballroomColor.dispose();
		lineColor.dispose();
		animationColor.dispose();
		animationSelectedColor.dispose();
		yellowColor.dispose();
		redColor.dispose();
		gridColor.dispose();
		countFont.dispose();
		countColor.dispose();
		leftFeetSelectedColor.dispose();
		leftFeetColor.dispose();
		leftFeetBorderColor.dispose();
		rightFeetSelectedColor.dispose();
		rightFeetColor.dispose();
		rightFeetBorderColor.dispose();
		bufferGC.dispose();
		bufferImage.dispose();
	}
	
	public Ballroom(Composite comp, int style)
	{
		/* Note since we use a border our client area is proably different */
		super(comp,style|SWT.NO_BACKGROUND|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);

		leftFeetColor = new Color(getDisplay(),5,5,5);
		leftFeetSelectedColor = new Color(getDisplay(),250,250,170);
		leftFeetBorderColor = new Color(getDisplay(),15,15,15);

		rightFeetColor = new Color(getDisplay(),5,5,5);
		rightFeetSelectedColor = new Color(getDisplay(),80,80,0);
		rightFeetBorderColor = new Color(getDisplay(),15,15,15);

		darkGreyColor = new Color(getDisplay(),70,70,70); 
		shineGreyColor = new Color(getDisplay(),180,180,180);
		ballroomColor = new Color(getDisplay(),240,240,200);
		
		lineColor = new Color(getDisplay(),200,200,0);
		
		yellowColor = new Color(getDisplay(),240,240,0);
		redColor = new Color(getDisplay(),240,0,0);

		animationColor = new Color(getDisplay(),230,230,230);
		animationSelectedColor = new Color(getDisplay(),255,255,255);
		gridColor = new Color(getDisplay(),0,0,0);

		countColor = new Color(getDisplay(),255,255,255);
		countFont = new Font(getDisplay(),"Thorndale",20,0);

		femaleLeftColor = new Color(getDisplay(),244,231,240);
		femaleRightColor = new Color(getDisplay(),244,61,195);
		maleLeftColor = new Color(getDisplay(),228,229,240);
		maleRightColor = new Color(getDisplay(),61,78,240);

		SelectionListener selectionListener = new SelectionListener()
		{
			public void widgetSelected(SelectionEvent event)
			{
				ScrollBar scrollbar = (ScrollBar)(event.widget);
				if ((scrollbar.getStyle() & SWT.V_SCROLL) != 0)
				{
					zoomTop = 1200 - scrollbar.getSelection();
				} else
				{
					zoomLeft = scrollbar.getSelection();
				}
				redraw();
				update();
			}
			public void widgetDefaultSelected(SelectionEvent event) {}
		};
		
		
		ScrollBar scrollbar = getVerticalBar();
		scrollbar.setMaximum(1199); 
		scrollbar.setMinimum(0);
		scrollbar.setIncrement(1);
		scrollbar.setSelection(1200 - zoomTop);
		scrollbar.addSelectionListener(selectionListener);
		
		scrollbar = getHorizontalBar();
		scrollbar.setMaximum(1199); 
		scrollbar.setMinimum(0);
		scrollbar.setIncrement(1);
		scrollbar.setSelection(zoomLeft);
		scrollbar.addSelectionListener(selectionListener);
		
		addControlListener(new ControlAdapter()
		{
			public void controlResized(ControlEvent event)
			{
				refreshScrollBars();
			}
		});

		addPaintListener(new PaintListener()
		{
			public void paintControl(PaintEvent e)
			{
				Rectangle bounds = getClientArea();
				createImageBuffer();
				GC gc = bufferGC;

				gc.setBackground(ballroomColor);
				gc.fillRectangle(bounds);
				
				drawGrid(gc);
				drawStep(gc);
				e.gc.drawImage(bufferImage,getClientArea().x,getClientArea().y);
			}
		});
		
		
		addMouseMoveListener(new MouseMoveListener()
		{
			public void mouseMove(MouseEvent event)
			{
				Point ballroomPoint = transformPixToBallroom(event.x,event.y);
				if (coordinatesX != ballroomPoint.x || coordinatesY != ballroomPoint.y)
				{
					coordinatesX = ballroomPoint.x;
					coordinatesY = ballroomPoint.y;
					redraw();
					update();
				}

				if (mousePressed && dragOperation != DRAG_NO)
				{
					if (lastSelectedFootIndex != -1)
					{
						if (dragOperation == DRAG_ROTATE_BALE)
						{
							Point p = transformPixToBallroom(event.x,event.y);
							int winkel = calculateBallroomAngle(
								rotationCenterBallroomPoint.x,
								rotationCenterBallroomPoint.y,
								p.x,p.y);

							WayPoint feetCoord = pattern.getStep(lastSelectedStepIndex).getFeet(lastSelectedFootIndex).getStartingWayPoint();
							feetCoord.x = rotationCenterBallroomPoint.x - (int)((distance * Math.sin(Math.toRadians(winkel))));
							feetCoord.y = rotationCenterBallroomPoint.y + (int)((distance * Math.cos(Math.toRadians(winkel))));
							feetCoord.a = winkel;
						} else
						if (dragOperation == DRAG_ROTATE_HEEL)
						{
							Point p = transformPixToBallroom(event.x,event.y);
							int winkel = calculateBallroomAngle(
								rotationCenterBallroomPoint.x,
								rotationCenterBallroomPoint.y,
								p.x,p.y) - 180;
							if (winkel < 0) winkel += 360;

							WayPoint feetCoord = pattern.getStep(lastSelectedStepIndex).getFeet(lastSelectedFootIndex).getStartingWayPoint();
							feetCoord.x = rotationCenterBallroomPoint.x + (int)((distance * Math.sin(Math.toRadians(winkel))));
							feetCoord.y = rotationCenterBallroomPoint.y - (int)((distance * Math.cos(Math.toRadians(winkel))));
							feetCoord.a = winkel;
						} else
						if (dragOperation == DRAG_MOVE_WAYPOINT)
						{
							Point p = transformPixToBallroom(event.x,event.y);
							WayPoint feetCoord = pattern.getStep(lastSelectedStepIndex).getFeet(lastSelectedFootIndex).getFeetCoord(lastSelectedWaypoint);
							feetCoord.x = p.x;
							feetCoord.y = p.y;
						}
					}

					redraw();
					update();

					BallroomEvent be = new BallroomEvent();
					be.feetCoordinatesChanged = true;
					emitEvent(be);					
				}

				if (!mousePressed)
				{
					boolean disposeCursor = true;

					if (pattern == null) return;
					Step step = pattern.getCurrentStep();
					if (step == null) return;
					Step previousStep = pattern.getPreviousStep();

					CoordinateInfo ci = getPixCoordinateInfo(event.x,event.y,step);
					if (ci.feetIndex != -1)
					{
						if (ci.feetPart != FEETPART_NO || ci.waypoint == 0)
						{
							if (cursor == null)
							{
								cursor = new Cursor(getDisplay(),SWT.CURSOR_HAND);
								setCursor(cursor);
							}
							disposeCursor = false;
						}						
					}
					
					if (disposeCursor && previousStep != null)
					{
						ci = getPixCoordinateInfo(event.x,event.y,previousStep);
						if (ci.waypoint > 0)
						{
							if (cursor == null)
							{
								cursor = new Cursor(getDisplay(),SWT.CURSOR_HAND);
								setCursor(cursor);
							}

							disposeCursor = false;
						} 
					}
	
					if (disposeCursor && cursor != null)
					{				
						setCursor(null);
						cursor.dispose();
						cursor = null;
					}
				}
			}
		});
		
		addMouseListener(new MouseListener()
		{
			public void mouseDoubleClick(MouseEvent arg0)
			{
			}
			
			public void mouseDown(MouseEvent ev)
			{
				boolean rejectWayPointRequest = false;
				if (pattern == null) return;
				Step step = pattern.getCurrentStep();
				if (step == null) return;
				Step previousStep = pattern.getPreviousStep();

				lastSelectedFootIndex = -1;
				CoordinateInfo ci = getPixCoordinateInfo(ev.x,ev.y,step);

				if (ci.feetIndex != -1)
				{
					if (ci.feetPart != FEETPART_NO || ci.waypoint == 0)
					{
						lastSelectedWaypoint = ci.waypoint;
						lastSelectedStepIndex = pattern.getCurrentStepNum();
						lastSelectedFootIndex = ci.feetIndex;
						dragOperation = ci.feetPart;
						if (ci.waypoint != -1)	dragOperation = DRAG_MOVE_WAYPOINT;
						distance = ci.distance;
						rotationCenterBallroomPoint = ci.rotationCenterBallroomPoint;
					}						
				} else
				{
					if (previousStep != null)
					{
						ci = getPixCoordinateInfo(ev.x,ev.y,previousStep);
						if (ci.waypoint > 0)
						{
							lastSelectedWaypoint = ci.waypoint;
							lastSelectedStepIndex = pattern.getCurrentStepNum() - 1;
							lastSelectedFootIndex = ci.feetIndex;
							dragOperation = DRAG_MOVE_WAYPOINT;
						} else rejectWayPointRequest = true;
					}
				}

				boolean wasSomethingSelected = false;

				for (int i=0;i<selectedArray.length;i++)
				{
					if (selectedArray[i])
					{
						wasSomethingSelected = true;
						selectedArray[i] = false;
					}
				}

				if (ci.feetIndex != -1)
					selectedArray[ci.feetIndex] = true;

				redraw();
				update();

				if ((ci.feetIndex == -1 && wasSomethingSelected) || ci.feetIndex != -1)
				{
					BallroomEvent be = new BallroomEvent();
					be.selectionChanged = true;
					emitEvent(be);
				}

				if (ev.button != 3) mousePressed = true;
				else
				{
					if (contextMenu != null)
					{
						contextMenu.dispose();
						contextMenu = null;
					}

					contextMenu = new Menu(getShell());
					MenuItem menuItem;
					
					if (ci.feetIndex != -1)
					{
						contextFeetIndex = ci.feetIndex;
						contextStepIndex = lastSelectedStepIndex;
						contextWayPoint = ci.waypoint;
						
						menuItem = new MenuItem(contextMenu, SWT.CHECK);
						menuItem.setText(_("Rotate around long angle"));
						menuItem.setSelection(pattern.getStep(contextStepIndex).getFeet(contextFeetIndex).isLongRotation());
						menuItem.addSelectionListener(new SelectionAdapter()
						{
							public void widgetSelected(SelectionEvent event)
							{
								pattern.getStep(contextStepIndex).getFeet(contextFeetIndex).setLongRotation(((MenuItem)event.widget).getSelection());
								redraw();
							}
						});
						new MenuItem(contextMenu, SWT.BAR);
						if (!rejectWayPointRequest)
						{
							menuItem = new MenuItem(contextMenu, 0);
							menuItem.setText(_("Insert way point"));
							menuItem.addSelectionListener(new SelectionAdapter()
							{
								public void widgetSelected(SelectionEvent e) {
									contextMenu.setVisible(false);
									pattern.addWayPoint(contextFeetIndex,lastSelectedWaypoint);
									redraw();
								}
							});
							menuItem = new MenuItem(contextMenu, 0);
							menuItem.setText(_("Remove all way points"));
							menuItem.addSelectionListener(new SelectionAdapter()
							{
								public void widgetSelected(SelectionEvent e) {
									contextMenu.setVisible(false);
									pattern.removeAllWayPoints(contextFeetIndex);
									redraw();
								}
							});
							menuItem = new MenuItem(contextMenu,SWT.SEPARATOR);
						}
					}
					contextMenu.setVisible(true);
					
					menuItem = new MenuItem(contextMenu,0);
					menuItem.setText(_("View whole Pattern"));
					menuItem.addSelectionListener(new SelectionAdapter()
					{
						public void widgetSelected(SelectionEvent e) {
							contextMenu.setVisible(false);
							viewWholePattern();
						}
					});
				}
				
			}
			
			public void mouseUp(MouseEvent arg0)
			{
				mousePressed = false;
			}
		});
	}
	
	private boolean myPointRangeTest(WayPoint feetCoord, int px, int py, int tx, int ty)
	{
    	Point p = transformCoords(feetCoord,px,py);
    	p = transformBallroomToPix(p.x,p.y);

		if (Math.abs(p.x - tx) < 5 && Math.abs(p.y - ty) < 5)
			return true;

		return false;
	}

    /* mx and my must be ballroom coordinates */
    private void myDrawCircle(GC gc, WayPoint feetCoord, int mx, int my)
    {
    	Point p = transformCoords(feetCoord,mx,my);
    	p = transformBallroomToPix(p.x,p.y);
		gc.drawOval(p.x,p.y,2,2);
    }

	private boolean myPolygonTest(WayPoint feetCoord, boolean mirror, int [] data, int pixSize, int ballroomSize, int tx, int ty)
	{
		feetCoord = transformFeedCoordToPix(feetCoord);
		Polygon polygon = new Polygon();
		int x = feetCoord.x;
		int y = feetCoord.y;
		int a = feetCoord.a;

		for (int i=0;i<data.length;i+=2)
		{
			int px = data[i];
			int py = data[i+1];
    		
			if (mirror) px = -px;

    		px = px * ballroomSize  * zoomFactor / 100 / pixSize;
    		py = py * ballroomSize  * zoomFactor / 100 / pixSize;
    		
			double cosa = Math.cos(Math.toRadians(a));
			double sina = Math.sin(Math.toRadians(a));
    		
			int newx = (int) (px * cosa + py * sina) + x;
			int newy = (int)(- px * sina + py * cosa) + y;
			
			polygon.addPoint(newx,newy);
		}
		return polygon.contains(tx,ty);
	}
	
    private void myDrawPolygon(GC gc, WayPoint feetCoord, boolean mirror, int [] data, int pixSize, int ballroomSize, boolean closed)
    {
		feetCoord = transformFeedCoordToPix(feetCoord);
		int x = feetCoord.x;
		int y = feetCoord.y;
		int a = feetCoord.a;
		
    	int [] newData = new int[data.length];
    	for (int i=0;i<data.length;i+=2)
    	{
    		int px = data[i];
    		int py = data[i+1];
    		
    		if (mirror) px = -px;
    		
    		px = px * ballroomSize  * zoomFactor / 100 / pixSize;
    		py = py * ballroomSize  * zoomFactor / 100 / pixSize;
    		
    		double cosa = Math.cos(Math.toRadians(a));
    		double sina = Math.sin(Math.toRadians(a));
    		
    		newData[i] = (int)(px * cosa + py * sina) + x;
    		newData[i+1] = (int)(- px * sina + py * cosa) + y;
    	}
    	if (closed) gc.drawPolygon(newData);
    	else gc.drawPolyline(newData);
    }

	private int [] calcPolygon(WayPoint feetCoord, boolean mirror, int [] data, int pixSize, int ballroomSize)
	{
		feetCoord = transformFeedCoordToPix(feetCoord);
		int x = feetCoord.x;
		int y = feetCoord.y;
		int a = feetCoord.a;
		
		int [] newData = new int[data.length];
		for (int i=0;i<data.length;i+=2)
		{
			int px = data[i];
			int py = data[i+1];
    		
			if (mirror) px = -px;
    		
			px = px * ballroomSize  * zoomFactor / 100 / pixSize;
			py = py * ballroomSize  * zoomFactor / 100 / pixSize;
    		
			double cosa = Math.cos(Math.toRadians(a));
			double sina = Math.sin(Math.toRadians(a));
    		
			newData[i] = (int)(px * cosa + py * sina) + x;
			newData[i+1] = (int)(- px * sina + py * cosa) + y;
		}
		return newData;
	}

	private void myFillPolygon(GC gc, WayPoint feetCoord, boolean mirror, int [] data, int pixSize, int ballroomSize)
	{
		int [] newData = calcPolygon(feetCoord,mirror,data,pixSize,ballroomSize);
		gc.fillPolygon(newData);
	}
	
	private void myGradientPolygon(GC gc, RGB startRGB, RGB endRGB, WayPoint feetCoord, boolean mirror, int [] data, int pixSize, int ballroomSize)
	{
		int minx,miny,maxx,maxy;
		int [] newData = calcPolygon(feetCoord,mirror,data,pixSize,ballroomSize);
		
		minx = maxx = newData[0];
		miny = maxy = newData[1];
		
		/* TODO: Optimize via algorithmn shown in "Introduction to algorithms */
		for (int i=2;i<newData.length;i+=2)
		{
			if (newData[i] < minx) minx = newData[i];
			else if (newData[i] > maxx) maxx = newData[i];
			if (newData[i+1] < miny) miny = newData[i+1];
			else if (newData[i+1] > maxy) maxy = newData[i+1];
		}
		
		Rectangle bounds = new Rectangle(minx,miny,maxx-minx+1,maxy-miny+1);
		ImageData imageData = getRectangleGradient(bounds.width,bounds.height,feetCoord.a,startRGB,endRGB);
		

		RGB [] maskPaletteData = new RGB[]{new RGB(0,0,0),new RGB(255,255,255)};
		ImageData maskImageData = new ImageData(bounds.width,bounds.height,8,new PaletteData(maskPaletteData));
		Image maskImage = new Image(getDisplay(),maskImageData);
		GC maskGC = new GC(maskImage);
		Color white = new Color(getDisplay(),255,255,255);
		maskGC.setBackground(white);

		for (int i=0;i<newData.length;i+=2)
		{
			newData[i] -= minx;
			newData[i+1] -= miny;
		}

		maskGC.fillPolygon(newData);

		maskGC.dispose();
		white.dispose();

		maskImageData = maskImage.getImageData();
		
		final int h = maxy - miny + 1;
		final int w = maxx - minx + 1;
		int p = 0;
		
		byte [] alphaData = new byte[w*h];
		byte [] maskData = maskImageData.data;		
		
		for (int y=0;y<h;y++)
		{
			int o = p;
			for (int x=0;x<w;x++)
			{
				if (maskData[o++]!=0) alphaData[x] = -1;
				else alphaData[x] = 0;
			}
			imageData.setAlphas(0,y,w,alphaData,0);
			p += maskImageData.bytesPerLine;
		}
		
		Image fillImage = new Image(getDisplay(),imageData);
		gc.drawImage(fillImage,bounds.x,bounds.y);
		
		maskImage.dispose();
		fillImage.dispose();
	}
	
	private void myDrawText(GC gc, WayPoint feetCoord, String text)
	{
		feetCoord = transformFeedCoordToPix(feetCoord);
		int x = feetCoord.x;
		int y = feetCoord.y;
		int a = feetCoord.a;
		
		gc.setFont(null);

		x -= gc.textExtent(text).x/2;
		y -= gc.textExtent(text).y/2;
		
		gc.drawText(text,x,y,true);
	}

	private ImageData getRectangleGradient(int width, int height, int angle, RGB startRGB, RGB endRGB)
	{
		/* The basic idea of this algorithm is to calc the intersection between the
		 * diagonal of the rectangle (xs,ys) with dimension (xw,yw) a with the line starting
		 * at (x,y) (every pixel inside the rectangle) and angle angle with direction vector (vx,vy).
		 * 
		 * Having the intersection point we then know the color of the pixel.
		 * 
		 * TODO: Turn the algorithm into a incremental one
		 *       Remove the use of floating point variables
		 */
		 
		ImageData imageData = new ImageData(width,height,24,new PaletteData(0xff,0xff00,0xff0000));
		byte [] data = imageData.data;
		int p = 0;
		double rad = Math.toRadians(angle);
		double cosarc = Math.cos(rad);
		double sinarc = Math.sin(rad);

		/* Normalize the angle */
		if (angle < 0) angle = 360 - ((-angle)%360);
		if (angle >= 0) angle = angle % 360;

		final int diffR = endRGB.red - startRGB.red;
		final int diffG = endRGB.green - startRGB.green;
		final int diffB = endRGB.blue - startRGB.blue;

		int xs,ys,xw,yw;

		int vx = (int)(-cosarc*0x100);
		int vy = (int)(sinarc*0x100);

		if (angle <= 90 || (angle > 180 && angle <= 270))
		{
			/* The to be intersected diagonal goes from the top left edge to the bottom right edge */
			xs = 0;
			ys = 0;
			xw = width;
			yw = height;
		} else
		{
			/* The to be intersected diagonal goes from the bottom left edge to the top right edge */
			xs = 0;
			ys = height;
			xw = width;
			yw = -height;
		}
		
		final int xadd,ystart,yadd;
		
		if (angle > 90 && angle <= 270)
		{
			/* for these angle we have y1 = height - y1. Instead of
			 * 
			 *  y1 = height - (-vy*(yw*  xs -xw*  ys)         + yw*(vy*  x -vx*  y))        /(-yw*vx + xw*vy);
			 * 
			 * we have
			 * 
             *  y1 =          (-vy*(yw*(-xs)-xw*(-ys+height)) + yw*(vy*(-x)-vx*(-y+height)))/(-yw*vx + xw*vy);
             * 
             * so height - y1 can be expressed with the normal formular adapting some parameters.
			 * 
			 * Note that if one would exchanging startRGB/endRGB the values would only work
			 * for linear color gradients
			 */
			xadd = -1;
			yadd = -1;
			ystart = height;

			xs = -xs;
			ys = -ys + height;
		} else
		{
			xadd = 1;
			yadd = 1;
			ystart = 0;
		}

		int x1,y1;

		/* The formular as shown above is
		 * 
		 * 	 y1 = ((-vy*(yw*xs-xw*ys) + yw*(vy*x-vx*y)) /(-yw*vx + xw*vy));
		 * 
		 * We see that only yw*(vy*x-vx*y) changes during the loop.
		 * 
		 * We write
		 *   
		 *   y1(x,y) = (r + yw*(vy*x-vx*y))/t = r/t + yw*(vy*x-vx*y)/t
		 *   y1(x+1,y) = (r + vw*(vy*(x+1)-vx*y))/t 
		 *   t*(y1(x+1,y) - y1(x,y)) = yw*(vy*(x+1)-vx*y) - yw*(vy*x-vx*y) = yw*vy;
		 * 
		 */

		int r = -vy*(yw*xs-xw*ys); 
		int t = -yw*vx + xw*vy;
		int incr_y1 = yw*vy*xadd;
		
		int height_square = height*height;

		for (int l = 0, y = ystart; l < height; l++, y+=yadd)
		{
			int o = p;
			int y1_mul_t_accu = r - yw*vx*y;

			for (int c = 0, x = 0; c < width; c++, x+=xadd)
			{
				int red,green,blue;

				/* Calculate the intersection of two lines, this is not the fastet way to do but
				 * it is intuitive. Will be optimzed later */
//				y1 = (int)((r + yw*(vy*x-vx*y))/t);
				y1 = y1_mul_t_accu / t;
				
				int e = y1 * y1 / height * y1;

				red = startRGB.red + (int)(diffR*e/height_square);
				green = startRGB.green + (int)(diffG*e/height_square);
				blue = startRGB.blue + (int)(diffB*e/height_square);

				data[o++] = (byte)blue;
				data[o++] = (byte)green;
				data[o++] = (byte)red;
				
				y1_mul_t_accu += incr_y1;
			}
			p += imageData.bytesPerLine;
		}
		
		return imageData;
	}

	private void fillRectangleGradient(GC gc, Rectangle bounds, int a, RGB startRGB, RGB endRGB)
	{
		ImageData imageData = getRectangleGradient(bounds.width,bounds.height,a,startRGB,endRGB);
		Image image = new Image(getDisplay(),imageData);
		gc.drawImage(image,bounds.x,bounds.y);
		image.dispose();
	}
	
	private void drawGrid(GC gc)
	{
		if (showGrid)
		{
			int lineStyle = gc.getLineStyle();
			gc.setForeground(gridColor);
			gc.setLineStyle(SWT.LINE_DOT);
			for (int y = (zoomTop)/50*50;y>0;y-=50)
			{
				Point p = transformBallroomToPix(0,y);
				gc.drawLine(0,p.y,getClientArea().width-1,p.y);
			}

			for (int x = (zoomLeft + 49)/50*50;x<1200;x+=50)
			{
				Point p = transformBallroomToPix(x,0);
				gc.drawLine(p.x,0,p.x,getClientArea().height-1);
			}
			gc.setLineStyle(lineStyle);
		}
	}

	private void drawFoot(GC gc, Step step, WayPoint feetCoord, int footNum, boolean isSelected)
	{
		boolean fillBale = true;
		boolean fillHeel = true;

		Color backgroundColor;
		Color borderColor;
		if (step.isFeetLeft(footNum))
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
			
		GraphicsData graphicsData = getGraphicsData(step,footNum);

		int lw = gc.getLineWidth();
		int type = step.getFeet(footNum).getType(); 

		if (type == Foot.STAND_ON_FOOT)
		{
			gc.setForeground(yellowColor);
			gc.setLineWidth(2);
		} else
		{
			gc.setForeground(borderColor);
		}

		if (type == Foot.BALL_STEP_STAY || type == Foot.BALL_STAY || type == Foot.TAP)
		{
			fillBale = false;
			fillHeel = false;
		}

		gc.setBackground(backgroundColor);
		
		RGB rgb1;
		RGB rgb2 = new RGB(0,0,0);
		Color heelColor;
		
		if (step.isFeetFemale(footNum))
		{
			if (step.isFeetLeft(footNum)) heelColor = femaleLeftColor;
			else heelColor = femaleRightColor;
		} else
		{
			if (step.isFeetLeft(footNum)) heelColor = maleLeftColor;
			else heelColor = maleRightColor;
		}
		rgb1 = heelColor.getRGB();

		if (fillBale) myGradientPolygon(gc,rgb1,rgb2,feetCoord,step.isFeetLeft(footNum),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize);
		myDrawPolygon(gc,feetCoord,step.isFeetLeft(footNum),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize,true);

		gc.setBackground(heelColor);
		if (fillHeel) myFillPolygon(gc,feetCoord,step.isFeetLeft(footNum),graphicsData.heelData,graphicsData.feetDataYSize,graphicsData.realYSize);
		myDrawPolygon(gc,feetCoord,step.isFeetLeft(footNum),graphicsData.heelData,graphicsData.feetDataYSize,graphicsData.realYSize,true);

		if (type == Foot.BALL_STEP || type == Foot.BALL_STEP_STAY || type == Foot.BALL_STAY)
		{
			if (type != Foot.BALL_STAY) 
			{
				gc.setForeground(redColor);
				gc.setLineWidth(2);
			}
			if (type != Foot.BALL_STEP) myFillPolygon(gc,feetCoord,step.isFeetLeft(footNum),graphicsData.getBale(),graphicsData.feetDataYSize,graphicsData.realYSize);
			myDrawPolygon(gc,feetCoord,step.isFeetLeft(footNum),graphicsData.getBale(),graphicsData.feetDataYSize,graphicsData.realYSize,false);
		}
		
		if (type == Foot.HEEL_STEP)
		{
			gc.setForeground(redColor);
			gc.setLineWidth(2);
			myDrawPolygon(gc,feetCoord,step.isFeetLeft(footNum),graphicsData.getHeel(),graphicsData.feetDataYSize,graphicsData.realYSize,false);
		}
		
		if (type == Foot.TAP)
		{
			gc.setBackground(redColor);
			gc.setLineWidth(2);
			myFillPolygon(gc,feetCoord,step.isFeetLeft(footNum),graphicsData.getBaleTap(),graphicsData.feetDataYSize,graphicsData.realYSize);
		}

		gc.setLineWidth(lw);

		gc.setForeground(borderColor);
		if (step.isFeetLeft(footNum)) myDrawText(gc,feetCoord,"L");
		else myDrawText(gc,feetCoord,"R");

		myDrawCircle(gc,feetCoord,0,0);
		int ballroomBaleX = graphicsData.baleX * graphicsData.realYSize / graphicsData.feetDataYSize; 
		int ballroomBaleY = -graphicsData.baleY * graphicsData.realYSize / graphicsData.feetDataYSize; 
		int ballroomHeelX = graphicsData.heelX * graphicsData.realYSize / graphicsData.feetDataYSize; 
		int ballroomHeelY = -graphicsData.heelY * graphicsData.realYSize / graphicsData.feetDataYSize; 
		myDrawCircle(gc,feetCoord,ballroomBaleX,ballroomBaleY);
		myDrawCircle(gc,feetCoord,ballroomHeelX,ballroomHeelY);
	}

	private void drawStep(GC gc)
	{
		if (showCoordinates)
		{
			if (coordinatesX != -1 && coordinatesY != -1)
			{
				StringBuffer buf = new StringBuffer();
				buf.append(coordinatesX);
				buf.append(" ");
				buf.append(coordinatesY);
				buf.append("   ");
				gc.setBackground(ballroomColor);
				gc.setFont(null);
				gc.drawText(buf.toString(),4,2);
			}
		}

		if (pattern == null) return;
		Step step = pattern.getCurrentStep();
		if (step == null) return;
		Step previousStep = pattern.getPreviousStep();
		Step nextStep = pattern.getNextStep();

		if (animation)
		{
			String count = step.getCount();
			if (count != null)
			{
				gc.setBackground(ballroomColor);
				gc.setFont(countFont);
				Point p = gc.textExtent(count);
				gc.setForeground(countColor);
				gc.drawText(count,getClientArea().x + getClientArea().width - 1 - p.x - 20,2);
			}
		}

		if (previousStep != null && !animation)
		{
			if (showPrevStep)
			{
				for (int i=0;i<previousStep.getNumberOfFeets();i++)
				{
					if (previousStep.isFeetFemale(i) && !showLady) continue;
					if (!previousStep.isFeetFemale(i) && !showGent) continue;

					GraphicsData graphicsData = getGraphicsData(step,i);
					WayPoint wayPoint = previousStep.getStartingWayPoint(i); 
		
					if (previousStep.getFeet(i).isLeft()) gc.setBackground(shineGreyColor);
					else gc.setBackground(darkGreyColor);

					myFillPolygon(gc,wayPoint,step.isFeetLeft(i),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize);
					myFillPolygon(gc,wayPoint,step.isFeetLeft(i),graphicsData.heelData,graphicsData.feetDataYSize,graphicsData.realYSize);
				}
			}

			/* Show the animation outline */
			if (showAnimation)
			{
				for (int j=1;j<6;j++)
				{
					for (int i=0;i<previousStep.getNumberOfFeets();i++)
					{
						if (previousStep.isFeetFemale(i) && !showLady) continue;
						if (!previousStep.isFeetFemale(i) && !showGent) continue;

						if (selectedArray[i]) gc.setForeground(animationSelectedColor);
						else gc.setForeground(animationColor);

 						GraphicsData graphicsData = getGraphicsData(previousStep,i);
						WayPoint feetCoord = previousStep.getFeet(i).getInterpolatedWayPoint(step.getStartingWayPoint(i),step.getFeet(i).isLongRotation(),j,6);
						myDrawPolygon(gc,feetCoord,previousStep.isFeetLeft(i),graphicsData.heelData,graphicsData.feetDataYSize,graphicsData.realYSize, true);
						myDrawPolygon(gc,feetCoord,previousStep.isFeetLeft(i),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize, true);
					}
				}
			}
		}
	
		for (int i=0;i<step.getNumberOfFeets();i++)
		{
			boolean isSelected = selectedArray[i];

			if (step.isFeetFemale(i) && !showLady) continue;
			if (!step.isFeetFemale(i) && !showGent) continue;

			WayPoint feetCoord;
			
			if (animation && nextStep != null)
			{
				feetCoord = step.getFeet(i).getInterpolatedWayPoint(nextStep.getStartingWayPoint(i),nextStep.getFeet(i).isLongRotation(),animationNumber,animationMaxNumber);
			}
			else
			{
				feetCoord = step.getStartingWayPoint(i);
			}

			if (animation)
			{
				/* Use the next step if possible, because when animation runs it looks better to display the type of the next step
				 * TODO: Maybe it would better to change this so that the next step type is only used if the foot has been moved? */

/*				if (animationNumber > 2 && nextStep != null) drawFoot(gc,nextStep,feetCoord,i,isSelected);
				else */drawFoot(gc,step,feetCoord,i,isSelected);
			}
			else drawFoot(gc,step,feetCoord,i,isSelected);
			
			if (showPrevStep && previousStep != null && !animation)
			{
				gc.setForeground(lineColor);
				feetCoord = previousStep.getStartingWayPoint(i);
				Point p1 = transformBallroomToPix(feetCoord.x,feetCoord.y);
				Point p2;
				WayPoint wayPoint;
				
				for (int k=1;k<previousStep.getFeet(i).getNumOfWayPoints();k++)
				{
					wayPoint = previousStep.getFeet(i).getFeetCoord(k);
					p2 = transformBallroomToPix(wayPoint.x,wayPoint.y);
					gc.drawLine(p1.x,p1.y,p2.x,p2.y);
					gc.drawOval(p1.x-1,p1.y-1,2,2);
					p1 = p2;
				}
				wayPoint = step.getFeet(i).getStartingWayPoint();
				p2 = transformBallroomToPix(wayPoint.x,wayPoint.y);
				gc.drawLine(p1.x,p1.y,p2.x,p2.y);
				gc.drawOval(p1.x-1,p1.y-1,2,2);
			}
		}
	}
	
	/**
	 * Returns the pattern.
	 * @return Pattern
	 */
	public Pattern getPattern() {
		return pattern;
	}

	/**
	 * Sets the pattern.
	 * @param pattern The pattern to set
	 */
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
		viewWholePattern();
	}
	
	public void zoomIn()
	{
		zoomFactor = zoomFactor * 3 / 2;
		redraw();
		refreshScrollBars();
	}

	public void zoomOut()
	{
		if (zoomFactor == 25) return;
		zoomFactor = zoomFactor * 2 / 3;
		if (zoomFactor < 25) zoomFactor = 25;
		redraw();
		refreshScrollBars();
	}

	/**
	 * Returns the showAnimation.
	 * @return boolean
	 */
	public boolean isShowAnimation()
	{
		return showAnimation;
	}

	/**
	 * Returns the showNextStep.
	 * @return boolean
	 */
	public boolean isShowNextStep()
	{
		return showNextStep;
	}

	/**
	 * Returns the showPrevStep.
	 * @return boolean
	 */
	public boolean isShowPrevStep()
	{
		return showPrevStep;
	}

	/**
	 * Sets the showNextStep.
	 * @param showNextStep The showNextStep to set
	 */
	public void setShowNextStep(boolean showNextStep)
	{
		this.showNextStep = showNextStep;
		redraw();
	}

	/**
	 * Sets the showPrevStep.
	 * @param showPrevStep The showPrevStep to set
	 */
	public void setShowPrevStep(boolean showPrevStep)
	{
		this.showPrevStep = showPrevStep;
		redraw();
		update();
	}

	/**
	 * Sets the showAnimation.
	 * @param showAnimation The showAnimation to set
	 */
	public void setShowAnimation(boolean showAnimation)
	{
		this.showAnimation = showAnimation;
		redraw();
		update();
	}

	public void setShowGent(boolean showGent)
	{
		this.showGent = showGent;
		redraw();
		update();
	}

	public void setShowLady(boolean showLady)
	{
		this.showLady = showLady;
		redraw();
		update();
	}
	
	public void setShowGrid(boolean showGrid)
	{
		this.showGrid = showGrid;
		redraw();
		update();
	}
	
	public void moveSelectedFeets(int dx, int dy)
	{
		boolean redraw = false;
		for (int i=0;i<selectedArray.length;i++)
		{
			if (selectedArray[i])
			{
				if (pattern == null) return;
				Step step = pattern.getCurrentStep();
				if (step == null) return;

				WayPoint feetCoord = step.getStartingWayPoint(i);
				feetCoord.x += dx;
				feetCoord.y += dy;
				
				if (feetCoord.x < 0) feetCoord.x = 0;
				else if (feetCoord.x > 1199) feetCoord.x = 1199;
				if (feetCoord.y < 0) feetCoord.y = 0;
				else if (feetCoord.y > 1199) feetCoord.y = 1199;
				redraw = true;
			}
		}
		if (redraw) redraw();
	}

	public void rotateSelectedFeets(int da)
	{
		int numSelected = 0;
		int rotationCenterX = 0;
		int rotationCenterY = 0;

		if (pattern == null) return;
		Step step = pattern.getCurrentStep();
		if (step == null) return;

		for (int i=0;i<selectedArray.length;i++)
		{
			if (selectedArray[i])
			{
				WayPoint feetCoord = step.getStartingWayPoint(i);
				rotationCenterX += feetCoord.x;
				rotationCenterY += feetCoord.y;
				numSelected++;
			}
		}
		
		if (numSelected > 0)
		{
			rotationCenterX = (rotationCenterX) / numSelected;
			rotationCenterY = (rotationCenterY) / numSelected;
			
			for (int i=0;i<selectedArray.length;i++)
			{		
				if (selectedArray[i])
				{
					WayPoint feetCoord = step.getStartingWayPoint(i);

					int px =  feetCoord.x - rotationCenterX;
					int py =  feetCoord.y - rotationCenterY;

					double cosa = Math.cos(Math.toRadians(-da));
					double sina = Math.sin(Math.toRadians(-da));
    		
					double newx = (px * cosa + py * sina);
					double newy = (-px * sina + py * cosa);
					
					feetCoord.x = (int)Math.round(newx) + rotationCenterX;
					feetCoord.y = (int)Math.round(newy) + rotationCenterY;
					feetCoord.a += da;
					if (feetCoord.a < 0) feetCoord.a += 360;
					if (feetCoord.a > 359) feetCoord.a -= 360;
				}
			}
			redraw();
		} 
	}

	public final boolean [] getSelectionArray()
	{
		return selectedArray;
	}
	
	public void setSelectionArray(boolean [] array)
	{
		for (int i=0;i<selectedArray.length;i++)
			selectedArray[i] = array[i];
		redraw();
	}
	
	private void refreshScrollBars()
	{
		Rectangle rect = getClientArea();
		ScrollBar scrollbar = getVerticalBar();
		int visible = rect.height * 100 / zoomFactor;
		scrollbar.setThumb(visible);
		scrollbar.setPageIncrement(visible - 1);
		scrollbar.setSelection(1200 - zoomTop);

		scrollbar = getHorizontalBar();
		visible = rect.width * 100 / zoomFactor;
		scrollbar.setThumb(visible);
		scrollbar.setPageIncrement(visible - 1);
		scrollbar.setSelection(zoomLeft);
	}

	private void emitEvent(BallroomEvent be)
	{
		ListIterator iter = ballroomListenerList.listIterator();
		while (iter.hasNext())
		{
			BallroomListener listener = (BallroomListener)iter.next();
			if (be.feetCoordinatesChanged) listener.coordinatesChanged(be);
			else if (be.selectionChanged) listener.selectionChanged(be);
		}
	}

	public void addBallroomListener(BallroomListener listener)
	{
		ballroomListenerList.add(listener);
	}
	
	public void removeBallroomListener(BallroomListener listener)
	{
		ballroomListenerList.remove(listener);
	}
	
	public void animationInit(boolean singleStep, boolean backward)
	{
		animation = true;
		animationSingleStep = singleStep;
		animationBackward = backward;
		animationDrawnSteps = 0;
		animationCalcAnimFrames();
	}
	
	private void animationCalcAnimFrames()
	{
		AnimationInfo ai = pattern.getAnimationInfo(25);

		Step step = pattern.getCurrentStep();
		int framesperstep = ai.framesperbeat;
		if (step.isQuick()) framesperstep = framesperstep * pattern.getBeatsPerSlowStep() / 2;
		else if (step.isSlow()) framesperstep = framesperstep * pattern.getBeatsPerSlowStep();
		else
		{
			try
			{
				String duration = step.getDuration();
				String nominatorString = duration.substring(0,duration.indexOf('/'));
				String denominatorString = duration.substring(duration.indexOf('/')+1);
				int nominator = Integer.parseInt(nominatorString);
				int denominator = Integer.parseInt(denominatorString);

				framesperstep = ai.framesperbeat * pattern.getTimeSignatureBeats() * nominator / denominator;
			} catch(Exception e)
			{
			}
		}
		animationMaxNumber = framesperstep;
		animationNumber = 0;
	}
	
	/**
	 * Interate the animation.
	 * 
	 * @return false if animation has been completed (depending
	 *          on the parameters given at animationInit().
	 */
	public boolean animationNext()
	{
		if (animationBackward)
		{
			if (animationNumber == 0)
			{
				/* Abort animation if step has been completed and only one step should be displayed */
				animationDrawnSteps++; 
				if (animationSingleStep && animationDrawnSteps >= 2) return false;

				int newStepNum = pattern.getCurrentStepNum()-1;
				if (newStepNum < 0) return false;
				pattern.setCurrentStepNum(newStepNum);

				animationCalcAnimFrames();
				animationNumber = animationMaxNumber - 1;
			} else animationNumber--;
		} else
		{
			if (animationNumber >= animationMaxNumber)
			{
				int newStepNum = pattern.getCurrentStepNum()+1;
				if (newStepNum >= pattern.getStepLength()) return false;
				pattern.setCurrentStepNum(newStepNum);

				/* Abort animation if step has been completed and only one step should be displayed */
				animationDrawnSteps++; 
				if (animationSingleStep /*&& animationDrawnSteps >= 1*/) return false;

				animationCalcAnimFrames();
				animationNumber = 0;
			}
			animationNumber++;
		}
		redraw();
		update();
		return true;
	}
	
	public void animationStop()
	{
		animation = false;
		animationNumber = 0;
		animationMaxNumber = 0;
		redraw();
		update();
	}
	
	public void viewWholePattern()
	{
		if (pattern == null) return;
		int bounds[] = pattern.getPatternBounds();
		
		zoomLeft = bounds[0] - 10;
		zoomTop = bounds[1] + 20;
		int zoomRight = bounds[2] + 10;
		int zoomBottom = bounds[3] - 20; 

		zoomFactor = this.getClientArea().width * 100 / (zoomRight - zoomLeft + 1);  
		int newZoomFactor = this.getClientArea().height * 100 / (zoomTop - zoomBottom + 1);
		if (newZoomFactor < zoomFactor) zoomFactor = newZoomFactor;
		
		int ballroomWidth = this.getClientArea().width * 100 / zoomFactor;
		int ballroomHeight = this.getClientArea().height * 100 / zoomFactor;

		zoomLeft -= (ballroomWidth - (zoomRight - zoomLeft + 1))/2;
		zoomTop += (ballroomHeight - (zoomTop - zoomBottom + 1))/2;
		
		redraw();
		update();
		
		refreshScrollBars();
		
		BallroomEvent be = new BallroomEvent();
		be.viewChanged = true;
		emitEvent(be);
	}
}
