package de.sonumina.simpledance;

import static de.sonumina.simpledance.I18n._;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;

import de.sonumina.simpledance.graphics.Point;
import de.sonumina.simpledance.graphics.swt.SWTContext;

/**
 * @author Sebastian Bauer
 */
public class Ballroom extends Canvas
{
	static final int DRAG_NO = 0;
	static final int DRAG_ROTATE_BALE = 1;
	static final int DRAG_ROTATE_HEEL = 2;
	static final int DRAG_MOVE_WAYPOINT = 3;

	private SWTContext context;
	private Render render;
	
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

	private int horizontalScrollbarSelection;
	private int verticalScrollbarSelection;

	private int bufferWidth = -1;
	private int bufferHeight = -1;
	private Image bufferImage;
	private GC bufferGC;

	private Pattern pattern;
	private int zoomFactor = 300;
	private int rotation = 0;
	private int visibleLeft = 570;
	private int visibleTop = 650;
	private int coordinatesX = -1;
	private int coordinatesY = -1;
	private boolean showAnimationOutline = false;
	private boolean showNextStep = false;
	private boolean showPrevStep = false;
	private boolean showGent = true;
	private boolean showLady = true;
	private boolean showCoordinates = true;
	private boolean showGrid = true;
	private boolean showGradients = true;

	private boolean mousePressed = false;
	private de.sonumina.simpledance.graphics.Point rotationCenterBallroomPoint;
	private int distance;
	private int dragOperation;
	private int contextFeetIndex;
	private int contextStepIndex;

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

	private LinkedList<BallroomListener> ballroomListenerList = new LinkedList<>();

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

	public int calculatePixelAngle(int mx, int my, int x, int y)
	{
		int angle = 180 - calculateBallroomAngle(mx,my,x,y);
		if (angle >= 360) angle -= 360;
		else if (angle < 0) angle += 360 * ((-angle + 359)/360);
		return angle;
	}
	
	/**
	 * Transform current pixel coordinates to ballroom coordinates
	 * 	 * @param x	 * @param y	 * @return Point	 */
	private Point transformPixToBallroom(int x, int y)
	{
		Point r = new Point(x * 100, y * 100).rotate(-rotation);
		x = r.x / zoomFactor + visibleLeft;
		y = - r.y / zoomFactor + visibleTop;
		return new Point(x,y);
	}
	
	
	/**
	 * Transforms ballroom Coordinates to pixel coordinates
	 * 	 * @param x	 * @param y	 * @return Point	 */
	private Point transformBallroomToPix(int x, int y)
	{
		x = (x - visibleLeft) * zoomFactor / 100;
		y = (visibleTop - y) * zoomFactor / 100;
		return new Point(x,y);
	}

	/**
	 * Returns the current scene parameters relevant for rendering.
	 *
	 * @return the scene parameters.
	 */
	private Render.RenderSceneArgs getRenderSceneArgs()
	{
		Render.RenderSceneArgs rsa = new Render.RenderSceneArgs();
		rsa.pattern = pattern;
		rsa.stepNumber = pattern.getCurrentStepNum();
		int visibleWidth = getClientArea().width * 100 / zoomFactor;
		int visibleHeight = getClientArea().height * 100 / zoomFactor;
		rsa.visibleLeftTop = new Point(visibleLeft, visibleTop);
		rsa.visibleRightTop = rsa.visibleLeftTop.add(new Point(visibleWidth, 0).rotate(rotation));
		rsa.visibleLeftBottom = rsa.visibleLeftTop.add(new Point(0, -visibleHeight).rotate(rotation));
		rsa.visibleRightBottom = rsa.visibleLeftTop.add(new Point(visibleWidth, -visibleHeight).rotate(rotation));
		rsa.visibleRotation = rotation;
		rsa.pixelWidth = getClientArea().width;
		rsa.pixelHeight = getClientArea().height;

		rsa.showPrevStep = showPrevStep;
		rsa.showGradients = showGradients;
		rsa.showPrevStep = showPrevStep;
		rsa.insideAnimation = animation;
		rsa.animationNumber = animationNumber;
		rsa.animationMaxNumber = animationMaxNumber;
		rsa.showLady = showLady;
		rsa.showGent = showGent;
		rsa.showAnimationOutline = showAnimationOutline;
		for (int i=0;i<4;i++) rsa.selectedArray[i] = selectedArray[i];
		return rsa;
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
			context.setGC(bufferGC);
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
		render.dispose();
		context.dispose();
	}
	
	public Ballroom(Composite comp, int style)
	{
		/* Note since we use a border our client area is proably different */
		super(comp,style|SWT.NO_BACKGROUND|SWT.V_SCROLL|SWT.H_SCROLL|SWT.BORDER);

		context = new SWTContext(getDisplay());
		render = new Render(context);
		
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

		animationColor = new Color(getDisplay(),200,200,54);
		animationSelectedColor = new Color(getDisplay(),200,200,54);
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
				Point diff;
				if ((scrollbar.getStyle() & SWT.V_SCROLL) != 0)
				{
					diff = new Point(0, verticalScrollbarSelection - scrollbar.getSelection()).rotate(rotation);
					verticalScrollbarSelection = scrollbar.getSelection();
				} else
				{
					diff = new Point(scrollbar.getSelection() - horizontalScrollbarSelection, 0).rotate(rotation);
					horizontalScrollbarSelection = scrollbar.getSelection();
				}
				visibleLeft += diff.x;
				visibleTop += diff.y;

				redraw();
				update();
			}
			public void widgetDefaultSelected(SelectionEvent event) {}
		};
		
		
		ScrollBar scrollbar = getVerticalBar();
		scrollbar.setMaximum(1199); 
		scrollbar.setMinimum(0);
		scrollbar.setIncrement(1);
		scrollbar.setSelection(1200 - visibleTop);
		scrollbar.addSelectionListener(selectionListener);
		verticalScrollbarSelection = scrollbar.getSelection();
		
		scrollbar = getHorizontalBar();
		scrollbar.setMaximum(1199); 
		scrollbar.setMinimum(0);
		scrollbar.setIncrement(1);
		scrollbar.setSelection(visibleLeft);
		scrollbar.addSelectionListener(selectionListener);
		horizontalScrollbarSelection = scrollbar.getSelection();
		
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

				if (pattern != null)
				{
					render.renderScence(getRenderSceneArgs());

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

					if (animation && pattern.getCurrentStep() != null)
					{
						String count = pattern.getCurrentStep().getCount();
						if (count != null)
						{
							gc.setBackground(ballroomColor);
							gc.setFont(countFont);
							org.eclipse.swt.graphics.Point p = gc.textExtent(count);
							gc.setForeground(countColor);
							gc.drawText(count,getClientArea().x + getClientArea().width - 1 - p.x - 20,2);
						}
					}
				}
				

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

							WayPoint feetCoord = pattern.getStep(lastSelectedStepIndex).getFoot(lastSelectedFootIndex).getStartingWayPoint();
							feetCoord.x = rotationCenterBallroomPoint.x - (int)((distance * sin(toRadians(winkel))));
							feetCoord.y = rotationCenterBallroomPoint.y + (int)((distance * cos(toRadians(winkel))));
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

							WayPoint feetCoord = pattern.getStep(lastSelectedStepIndex).getFoot(lastSelectedFootIndex).getStartingWayPoint();
							feetCoord.x = rotationCenterBallroomPoint.x + (int)((distance * sin(toRadians(winkel))));
							feetCoord.y = rotationCenterBallroomPoint.y - (int)((distance * cos(toRadians(winkel))));
							feetCoord.a = winkel;
						} else
						if (dragOperation == DRAG_MOVE_WAYPOINT)
						{
							Point p = transformPixToBallroom(event.x,event.y);
							WayPoint feetCoord = pattern.getStep(lastSelectedStepIndex).getFoot(lastSelectedFootIndex).getWayPoint(lastSelectedWaypoint);
							feetCoord.x = p.x;
							feetCoord.y = p.y;
						}
					}

					redraw();
					update();
					emitFeetCoordinatesChangedEvent();
				}

				if (!mousePressed)
				{
					boolean disposeCursor = true;

					if (pattern == null) return;
					Step step = pattern.getCurrentStep();
					if (step == null) return;
					Step previousStep = pattern.getPreviousStep();

					Render.RenderSceneArgs rsa = getRenderSceneArgs();
					Render.CoordinateInfo ci = render.getPixCoordinateInfo(rsa, event.x, event.y, step);
					if (ci.feetIndex != -1)
					{
						if (ci.feetPart != Render.FootPart.NO || ci.waypoint == 0)
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
						ci = render.getPixCoordinateInfo(rsa, event.x, event.y, previousStep);
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

				Render.RenderSceneArgs rsa = getRenderSceneArgs();
				Render.CoordinateInfo ci = render.getPixCoordinateInfo(rsa, ev.x, ev.y, step);

				if (ci.feetIndex != -1)
				{
					if (ci.feetPart != Render.FootPart.NO || ci.waypoint == 0)
					{
						lastSelectedWaypoint = ci.waypoint;
						lastSelectedStepIndex = pattern.getCurrentStepNum();
						lastSelectedFootIndex = ci.feetIndex;
						dragOperation = ci.feetPart == Render.FootPart.BALE?DRAG_ROTATE_BALE:DRAG_ROTATE_HEEL;
						if (ci.waypoint != -1)	dragOperation = DRAG_MOVE_WAYPOINT;
						distance = ci.distance;
						rotationCenterBallroomPoint = ci.rotationCenterBallroomPoint;
					}						
				} else
				{
					if (previousStep != null)
					{
						ci = render.getPixCoordinateInfo(rsa, ev.x, ev.y, previousStep);
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

						menuItem = new MenuItem(contextMenu, SWT.CHECK);
						menuItem.setText(_("Rotate around long angle"));
						menuItem.setSelection(pattern.getStep(contextStepIndex).getFoot(contextFeetIndex).isLongRotation());
						menuItem.addSelectionListener(new SelectionAdapter()
						{
							public void widgetSelected(SelectionEvent event)
							{
								pattern.getStep(contextStepIndex).getFoot(contextFeetIndex).setLongRotation(((MenuItem)event.widget).getSelection());
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

		addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				int dx = 0, dy = 0;
				switch (e.keyCode)
				{
					case	SWT.ARROW_LEFT: dx = -1; break;
					case	SWT.ARROW_RIGHT: dx = 1; break;
					case	SWT.ARROW_UP: dy = 1; break;
					case	SWT.ARROW_DOWN: dy = -1; break;
				}
				if (dx != 0 || dy != 0)
				{
					moveSelectedFeets(dx, dy);
					emitFeetCoordinatesChangedEvent();
				}
			}
		});
	}

	private void drawGrid(GC gc)
	{
		if (showGrid)
		{
			/* Draw the grid, this is very unoptimized, it should be best done by Render */
			context.pushCurrentTransform();
			int lineStyle = gc.getLineStyle();
			gc.setForeground(gridColor);
			gc.setLineAttributes(new LineAttributes(1, SWT.CAP_FLAT, SWT.JOIN_MITER, SWT.LINE_DOT, null, 0, 10));
			context.applyRotateTransformation(rotation);
			for (int y = 1200; y > 0; y -= 50)
			{
				Point p = transformBallroomToPix(0,y);
				gc.drawLine(-1200,p.y,getClientArea().width+getClientArea().height-1,p.y);
			}

			for (int x = 0; x < 1200; x += 50)
			{
				Point p = transformBallroomToPix(x,0);
				gc.drawLine(p.x,-1200, p.x,getClientArea().width+getClientArea().height-1);
			}
			gc.setLineStyle(lineStyle);

			/* Workaround for Eclipse Bug 214841 (FIXME: this is not the right place where to account for it) */
			context.drawPolygon(new int[]{});
			context.popCurrentTransform();
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
		int oldBallroomWidth = this.getClientArea().width * 100 / zoomFactor;
		int oldBallroomHeight = this.getClientArea().height * 100 / zoomFactor;

		zoomFactor = zoomFactor * 3 / 2;
		
		int ballroomWidth = this.getClientArea().width * 100 / zoomFactor;
		int ballroomHeight = this.getClientArea().height * 100 / zoomFactor;
		
		visibleLeft += (oldBallroomWidth - ballroomWidth)/2;
		visibleTop -= (oldBallroomHeight - ballroomHeight)/2;
		
		redraw();
		refreshScrollBars();
	}

	public void zoomOut()
	{
		int oldBallroomWidth = this.getClientArea().width * 100 / zoomFactor;
		int oldBallroomHeight = this.getClientArea().height * 100 / zoomFactor;

		if (zoomFactor == 25) return;
		zoomFactor = zoomFactor * 2 / 3;
		if (zoomFactor < 25) zoomFactor = 25;

		int ballroomWidth = this.getClientArea().width * 100 / zoomFactor;
		int ballroomHeight = this.getClientArea().height * 100 / zoomFactor;
		
		visibleLeft += (oldBallroomWidth - ballroomWidth)/2;
		visibleTop -= (oldBallroomHeight - ballroomHeight)/2;

		redraw();
		refreshScrollBars();
	}

	/**
	 * Rotate the view using the given angle.
	 *
	 * @param angle
	 */
	public void rotate(int angle)
	{
		int visibleLeft = this.visibleLeft;
		int visibleTop = this.visibleTop;

		int visibleWidth = getClientArea().width * 100 / zoomFactor;
		int visibleHeight = getClientArea().height * 100 / zoomFactor;

		Point extend = new Point(visibleWidth, -visibleHeight).rotate(rotation);
		Point visibleLeftTop = new Point(visibleLeft, visibleTop);
		Point visibleRightBottom = new Point(visibleLeft + extend.x, visibleTop + extend.y);
		Point center = visibleLeftTop.center(visibleRightBottom);

		Point newVisibleLeftTop = visibleLeftTop.rotate(angle, center);
		this.visibleLeft = newVisibleLeftTop.x;
		this.visibleTop = newVisibleLeftTop.y;
		rotation += angle;

		redraw();
		refreshScrollBars();
	}

	/**
	 * Returns the showAnimation.
	 * @return boolean
	 */
	public boolean isShowAnimationOutline()
	{
		return showAnimationOutline;
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
	public void setShowAnimationOutline(boolean showAnimation)
	{
		this.showAnimationOutline = showAnimation;
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
	
	public void setShowGradients(boolean showGradients)
	{
		this.showGradients = showGradients;
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

					double cosa = cos(toRadians(-da));
					double sina = sin(toRadians(-da));
    		
					double newx = (px * cosa + py * sina);
					double newy = (-px * sina + py * cosa);
					
					feetCoord.x = (int)round(newx) + rotationCenterX;
					feetCoord.y = (int)round(newy) + rotationCenterY;
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
		scrollbar.setSelection(1200 - visibleTop);
		verticalScrollbarSelection = scrollbar.getSelection();

		scrollbar = getHorizontalBar();
		visible = rect.width * 100 / zoomFactor;
		scrollbar.setThumb(visible);
		scrollbar.setPageIncrement(visible - 1);
		scrollbar.setSelection(visibleLeft);
		horizontalScrollbarSelection = scrollbar.getSelection();
	}

	private void emitEvent(BallroomEvent be)
	{
		for (BallroomListener listener : ballroomListenerList)
		{
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
		Pattern.AnimationInfo ai = pattern.getAnimationInfo(25);

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
		
		visibleLeft = bounds[0] - 10;
		visibleTop = bounds[1] + 20;
		int visibleRight = bounds[2] + 10;
		int visibleBottom = bounds[3] - 20;

		zoomFactor = this.getClientArea().width * 100 / (visibleRight - visibleLeft + 1);
		int newZoomFactor = this.getClientArea().height * 100 / (visibleTop - visibleBottom + 1);
		if (newZoomFactor < zoomFactor) zoomFactor = newZoomFactor;
		
		if (zoomFactor == 0) zoomFactor = 1;

		int ballroomWidth = this.getClientArea().width * 100 / zoomFactor;
		int ballroomHeight = this.getClientArea().height * 100 / zoomFactor;

		visibleLeft -= (ballroomWidth - (visibleRight - visibleLeft + 1))/2;
		visibleTop += (ballroomHeight - (visibleTop - visibleBottom + 1))/2;
		
		redraw();
		update();
		
		refreshScrollBars();
		
		BallroomEvent be = new BallroomEvent();
		be.viewChanged = true;
		emitEvent(be);
	}

	/**
	 * Emit the event for feet coordinates having changed.
	 */
	private void emitFeetCoordinatesChangedEvent()
	{
		BallroomEvent be = new BallroomEvent();
		be.feetCoordinatesChanged = true;
		emitEvent(be);
	}
}
