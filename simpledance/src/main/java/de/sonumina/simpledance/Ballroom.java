package de.sonumina.simpledance;

import static de.sonumina.simpledance.core.I18n._;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;

import de.sonumina.simpledance.core.graphics.InputContext;
import de.sonumina.simpledance.core.graphics.InputContext.Drag;
import de.sonumina.simpledance.core.graphics.Point;
import de.sonumina.simpledance.core.graphics.Render;
import de.sonumina.simpledance.core.model.Pattern;
import de.sonumina.simpledance.core.model.Step;
import de.sonumina.simpledance.core.model.WayPoint;
import de.sonumina.simpledance.graphics.swt.SWTContext;

/**
 * @author Sebastian Bauer
 */
public class Ballroom extends Canvas
{
	private SWTContext context;
	private Render render;

	private Color ballroomColor;
	private Color countColor;
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

	private int contextFeetIndex;
	private int contextStepIndex;

	private InputContext inputContext = new InputContext();

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

		rsa.showGrid = showGrid;
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

		ballroomColor.dispose();
		countFont.dispose();
		countColor.dispose();
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
		
		ballroomColor = new Color(getDisplay(),240,240,200);
		
		countColor = new Color(getDisplay(),255,255,255);
		countFont = new Font(getDisplay(),"Thorndale",20,0);

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
				createImageBuffer();
				GC gc = bufferGC;

				render.renderScence(getRenderSceneArgs());

				if (pattern != null)
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
				Point ballroomPoint = render.transformPixToBallroom(getRenderSceneArgs(), event.x, event.y);
				if (coordinatesX != ballroomPoint.x || coordinatesY != ballroomPoint.y)
				{
					coordinatesX = ballroomPoint.x;
					coordinatesY = ballroomPoint.y;
					redraw();
					update();
				}

				if (inputContext.mousePressed && inputContext.dragOperation != Drag.NO)
				{
					render.mouseMove(getRenderSceneArgs(), inputContext, event.x, event.y);

					redraw();
					update();
					emitFeetCoordinatesChangedEvent();
				}

				if (!inputContext.mousePressed)
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

				inputContext.selectedFoot = -1;

				Render.RenderSceneArgs rsa = getRenderSceneArgs();
				Render.CoordinateInfo ci = render.getPixCoordinateInfo(rsa, ev.x, ev.y, step);

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
						ci = render.getPixCoordinateInfo(rsa, ev.x, ev.y, previousStep);
						if (ci.waypoint > 0)
						{
							inputContext.selectedWaypoint = ci.waypoint;
							inputContext.selectedStep = pattern.getCurrentStepNum() - 1;
							inputContext.selectedFoot = ci.feetIndex;
							inputContext.dragOperation = Drag.MOVE_WAYPOINT;
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

				if (ev.button != 3) inputContext.mousePressed = true;
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
						contextStepIndex = inputContext.selectedStep;

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
									pattern.addWayPoint(contextFeetIndex, inputContext.selectedWaypoint);
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
				inputContext.mousePressed = false;
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
					moveSelectedFeet(dx, dy);
					emitFeetCoordinatesChangedEvent();
				}
			}
		});
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
		Render.ZoomViewResult result = render.zoomInView(getRenderSceneArgs());

		visibleLeft = result.visibleLeft;
		visibleTop = result.visibleTop;
		zoomFactor = result.zoomFactor;
		redraw();
		refreshScrollBars();
	}

	public void zoomOut()
	{
		Render.ZoomViewResult result = render.zoomOutView(getRenderSceneArgs());

		visibleLeft = result.visibleLeft;
		visibleTop = result.visibleTop;
		zoomFactor = result.zoomFactor;
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
		Render.RotateViewResult rvr = render.rotateView(getRenderSceneArgs(), angle);
		visibleLeft = rvr.visibleLeft;
		visibleTop = rvr.visibleTop;
		rotation = rvr.rotation;

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
	 * Sets whether the next step should be displayed.
	 *
	 * @param showNextStep
	 */
	public void setShowNextStep(boolean showNextStep)
	{
		this.showNextStep = showNextStep;
		redraw();
	}

	/**
	 * Sets whether the previous step should be displayed.
	 *
	 * @param showPrevStep
	 */
	public void setShowPrevStep(boolean showPrevStep)
	{
		this.showPrevStep = showPrevStep;
		redraw();
		update();
	}

	/**
	 * Set whether an outline of the animation should be displayed.
	 *
	 * @param showAnimation
	 */
	public void setShowAnimationOutline(boolean showAnimation)
	{
		this.showAnimationOutline = showAnimation;
		redraw();
		update();
	}

	/**
	 * Set whether the gent feet should be displayed.
	 *
	 * @param showGent
	 */
	public void setShowGent(boolean showGent)
	{
		this.showGent = showGent;
		redraw();
		update();
	}

	/**
	 * Set whether the lady feet should be displayed.
	 *
	 * @param showLady
	 */
	public void setShowLady(boolean showLady)
	{
		this.showLady = showLady;
		redraw();
		update();
	}

	/**
	 * Set whether the grid should be displayed.
	 *
	 * @param showGrid
	 */
	public void setShowGrid(boolean showGrid)
	{
		this.showGrid = showGrid;
		redraw();
		update();
	}

	/**
	 * Set whether a color gradient should be used to fill the feet.
	 *
	 * @param showGradients
	 */
	public void setShowGradients(boolean showGradients)
	{
		this.showGradients = showGradients;
		redraw();
		update();
	}

	/**
	 * Move the currently selected feet by the given coordinate delta.
	 *
	 * @param dx
	 * @param dy
	 */
	public void moveSelectedFeet(int dx, int dy)
	{
		if (pattern == null) return;
		pattern.moveFeet(dx, dy, selectedArray);
		redraw();
	}

	/**
	 * Rotate selected feet by the given angle around their common center.
	 *
	 * @param rsa
	 * @param da
	 */
	public void rotateSelectedFeet(int da)
	{
		if (pattern == null) return;
		pattern.rotateFeet(da, selectedArray);
		redraw();
	}

	/**
	 * Return the sense boolean array of selected feet.
	 *
	 * @return
	 */
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
	 * Switch to the next (possibly interpolated) animation frame.
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
		Render.ViewWholePatternResult result = render.viewWholePattern(getRenderSceneArgs());

		if (result.valid)
		{
			visibleLeft = result.visibleLeft;
			visibleTop = result.visibleTop;
			zoomFactor = result.zoomFactor;

			redraw();
			update();

			refreshScrollBars();

			BallroomEvent be = new BallroomEvent();
			be.viewChanged = true;
			emitEvent(be);
		}
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
