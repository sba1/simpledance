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

	private int lastSelectedStepIndex;
	private int lastSelectedFootIndex;
	private int lastSelectedWaypoint;
	
	private boolean [] selectedArray = new boolean[4];
	
	/* attributes used when painting */
	private boolean animation;
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

		leftFeetColor = new Color(getDisplay(),250,250,250);
		leftFeetSelectedColor = new Color(getDisplay(),250,250,170);
		leftFeetBorderColor = new Color(getDisplay(),5,5,5);

		rightFeetColor = new Color(getDisplay(),5,5,5);
		rightFeetSelectedColor = new Color(getDisplay(),80,80,0);
		rightFeetBorderColor = new Color(getDisplay(),250,250,250);

		darkGreyColor = new Color(getDisplay(),70,70,70); 
		shineGreyColor = new Color(getDisplay(),180,180,180);
		ballroomColor = new Color(getDisplay(),96,108,175);
		
		lineColor = new Color(getDisplay(),255,255,0);
		
		yellowColor = new Color(getDisplay(),240,240,0);
		redColor = new Color(getDisplay(),240,0,0);

		animationColor = new Color(getDisplay(),230,230,230);
		animationSelectedColor = new Color(getDisplay(),255,255,255);
		gridColor = new Color(getDisplay(),0,0,0);
		
		countColor = new Color(getDisplay(),255,255,255);
		countFont = new Font(getDisplay(),"Thorndale",20,0);

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
						}
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
						contextStepIndex = pattern.getCurrentStepNum();

						menuItem = new MenuItem(contextMenu, 0);
						menuItem.setText(_("Insert way point"));
						menuItem.addSelectionListener(new SelectionAdapter()
						{
							public void widgetSelected(SelectionEvent e) {
								contextMenu.setVisible(false);
								pattern.addWayPoint(contextFeetIndex);
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

	private void myFillPolygon(GC gc, WayPoint feetCoord, boolean mirror, int [] data, int pixSize, int ballroomSize)
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
		gc.fillPolygon(newData);
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

/*	private void fillRectangleGradient(GC gc, Rectangle bounds, int a, RGB startColor, RGB endColor)
	{
		
		for (int x = 0;x<bounds.width;x++)
		{
			for (int y=0;y<bounds.height;y++)
			{
				gc.fillRectangle(bounds.x + x, bounds.y + y,1,1);
			}
		}
	}*/
	
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

		if (type == Foot.BALL_STEP_STAY || type == Foot.BALL_STAY)
		{
			fillBale = false;
			fillHeel = false;
		}

		gc.setBackground(backgroundColor);

		if (fillBale) myFillPolygon(gc,feetCoord,step.isFeetLeft(footNum),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize);
		myDrawPolygon(gc,feetCoord,step.isFeetLeft(footNum),graphicsData.baleData,graphicsData.feetDataYSize,graphicsData.realYSize,true);

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
						WayPoint feetCoord = previousStep.getFeet(i).getInterpolatedWayPoint(step.getStartingWayPoint(i),j,6);
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
				feetCoord = step.getFeet(i).getInterpolatedWayPoint(nextStep.getStartingWayPoint(i),animationNumber,animationMaxNumber);
			else
				feetCoord = step.getStartingWayPoint(i);

			drawFoot(gc,step,feetCoord,i,isSelected);
			
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

	public void animationInit()
	{
		animation = true;
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

		animationNumber = 0;
		animationMaxNumber = framesperstep;
	}
	
	public boolean animationNext()
	{
		if (animationNumber >= animationMaxNumber)
		{
			int newStepNum = pattern.getCurrentStepNum()+1;
			if (newStepNum >= pattern.getStepLength())
			{
				return false;
			}
			pattern.setCurrentStepNum(newStepNum);
			animationInit();
		}
		animationNumber++;
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
