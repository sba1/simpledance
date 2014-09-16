package de.sonumina.simpledance;

import static de.sonumina.simpledance.I18n._;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * This class implements the main view and controller.
 *
 * @author Sebastian Bauer
 */
public class Dance implements Runnable
{
	private Shell shell;

	static final int TOOLBAR_NEW = 1;
	static final int TOOLBAR_OPEN = 2;
	static final int TOOLBAR_SAVE = 3;
	static final int TOOLBAR_SAVEAS = 4;
	static final int TOOLBAR_ZOOM_IN = 5;
	static final int TOOLBAR_ZOOM_OUT = 6;
	static final int TOOLBAR_SHOW_PREV = 7;
	static final int TOOLBAR_SHOW_NEXT = 8;
	static final int TOOLBAR_SHOW_ANIM = 9;
	static final int TOOLBAR_SHOW_GENT = 10;
	static final int TOOLBAR_SHOW_LADY = 11;
	static final int TOOLBAR_SHOW_GRID = 12;
	static final int TOOLBAR_PLAY = 13;
	static final int TOOLBAR_PLAYSTOP = 14;
	static final int TOOLBAR_PLAYBACKWARD = 15;
	static final int TOOLBAR_PLAYFORWARD = 16;

	protected Pattern pattern;
	
	/** The step which has been copied (inside the "clipboard") */
	private Step copiedStep;

	private int lastStepSelected; 
	
	private Ballroom ballroom;
	private Table stepOverviewTable;
	private Text gentLeftXText;
	private Text gentLeftYText;
	private Text gentLeftAngleText;
	private Combo gentLeftTypeCombo;
	private Button gentLeftSelectedButton;
	private Text gentRightXText;
	private Text gentRightYText;
	private Text gentRightAngleText;
	private Combo gentRightTypeCombo;
	private Button gentRightSelectedButton;
	private Text ladyLeftXText;
	private Text ladyLeftYText;
	private Text ladyLeftAngleText;
	private Combo ladyLeftTypeCombo;
	private Button ladyLeftSelectedButton;
	private Text ladyRightXText;
	private Text ladyRightYText;
	private Text ladyRightAngleText;
	private Combo ladyRightTypeCombo;
	private Button ladyRightSelectedButton;
	private StyledText stepDescriptionStyledText;
	private Text stepCountText;
	private Combo durationCombo;
	
	/* all created images, which are disposed in dispose() */
	private LinkedList<Image> imageList = new LinkedList<Image>();
	
	private PatternProp patternPropShell;
	private DetailedOverviewShell detailedOverviewShell;
	private TimerThread timerThread;

	private boolean coordinatesAreRelative = true;
	private boolean useGradients = true;

	private Text createInteger(Composite parent, ModifyListener modifyListener, Object data)
	{
		Text text = new Text(parent,SWT.BORDER);
		text.setTextLimit(5);
		text.setData(data);
		if (modifyListener != null) text.addModifyListener(modifyListener);
		text.addVerifyListener(new IntegerVerifyListener());
		GridData gridData = new GridData();
		gridData.widthHint = 28;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		text.setLayoutData(gridData);
		return text;
	}
	
	private Image createImage(String name)
	{
		ImageData source;
		try
		{
			source = new ImageData(name);
		}
		catch(Exception e)
		{
			source = new ImageData(getClass().getResourceAsStream("/" + name));

		}
		ImageData mask = source.getTransparencyMask();
		Image image = new Image(shell.getDisplay(), source, mask);

		if (image != null)
		{
			imageList.add(image);
		} 
		return image;
	}
	
	private void setInteger(Text text, int value)
	{
		String str = text.getText();
		if (!str.equals(value + ""))
			text.setText(value + "");
	}

	/**
	 * Create the coolbar.
	 * 
	 * @param parent defines the coolbar's parent
	 * @return the coolbar object
	 */
	private CoolBar createCoolBar(Composite parent)
	{
		CoolBar coolbar = new CoolBar(parent,0);
		ToolItem toolitem;
		CoolItem coolitem;
	
		/* Toolbar */
		SelectionListener selectionListener = new SelectionListener()
		{
			public void widgetSelected(SelectionEvent event)
			{
				int type = ((Integer)(event.widget.getData())).intValue();
				switch (type)
				{
					case TOOLBAR_NEW: newPattern(); break;
					case TOOLBAR_OPEN: loadPattern(); break;
					case TOOLBAR_SAVE: savePattern(); break;
					case TOOLBAR_SAVEAS: savePatternAs(); break;
					case TOOLBAR_ZOOM_IN: ballroom.zoomIn(); break;
					case TOOLBAR_ZOOM_OUT: ballroom.zoomOut(); break;
					case TOOLBAR_SHOW_ANIM: ballroom.setShowAnimationOutline(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_SHOW_PREV: ballroom.setShowPrevStep(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_SHOW_NEXT: ballroom.setShowNextStep(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_SHOW_GENT: ballroom.setShowGent(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_SHOW_LADY: ballroom.setShowLady(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_SHOW_GRID: ballroom.setShowGrid(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_PLAY: play(); break;
					case TOOLBAR_PLAYBACKWARD: playBackward(); break;
					case TOOLBAR_PLAYFORWARD: playForward(); break;
					case TOOLBAR_PLAYSTOP: playStop(); break;
				}
			}
			public void widgetDefaultSelected(SelectionEvent event)
			{
			}
		};
		
		ToolBar toolbar = new ToolBar(coolbar,SWT.FLAT);
		toolitem = new ToolItem(toolbar,0);
		Image image = createImage("images/new.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Creates a new dance pattern"));
		toolitem.setData(new Integer(TOOLBAR_NEW));
		toolitem.addSelectionListener(selectionListener);
		toolitem = new ToolItem(toolbar,SWT.DROP_DOWN);
		image = createImage("images/open.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Opens a dance pattern"));
		toolitem.setData(new Integer(TOOLBAR_OPEN));
		toolitem.addSelectionListener(new OpenDropDownSelectionListener(shell));
		toolitem = new ToolItem(toolbar,0);
		image = createImage("images/save.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Stores current dance pattern"));
		toolitem.setData(new Integer(TOOLBAR_SAVE));
		toolitem.addSelectionListener(selectionListener);
		toolitem = new ToolItem(toolbar,0);
		image = createImage("images/saveas.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Stores the current dance pattern under a given filename"));
		toolitem.setData(new Integer(TOOLBAR_SAVEAS));
		toolitem.addSelectionListener(selectionListener);
	
		coolitem = new CoolItem(coolbar,0);
		coolitem.setControl(toolbar);
		Point pushSize = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		pushSize = coolitem.computeSize(pushSize.x, pushSize.y);
		coolitem.setSize(pushSize);
		coolitem.setMinimumSize(toolitem.getWidth(), pushSize.y);
	
		toolbar = new ToolBar(coolbar,SWT.FLAT);
		toolitem = new ToolItem(toolbar,0);
		image = createImage("images/play.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Plays the whole sequence"));
		toolitem.setData(new Integer(TOOLBAR_PLAY));
		toolitem.addSelectionListener(selectionListener);
	
		toolitem = new ToolItem(toolbar,0);
		image = createImage("images/playstop.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Stops the playing"));
		toolitem.setData(new Integer(TOOLBAR_PLAYSTOP));
		toolitem.addSelectionListener(selectionListener);
	
		toolitem = new ToolItem(toolbar,0);
		image = createImage("images/playprev.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Plays backward one frame"));
		toolitem.setData(new Integer(TOOLBAR_PLAYBACKWARD));
		toolitem.addSelectionListener(selectionListener);
	
		toolitem = new ToolItem(toolbar,0);
		image = createImage("images/playnext.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Plays forward one frame"));
		toolitem.setData(new Integer(TOOLBAR_PLAYFORWARD));
		toolitem.addSelectionListener(selectionListener);
	
		coolitem = new CoolItem(coolbar,0);
		coolitem.setControl(toolbar);
		pushSize = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		pushSize = coolitem.computeSize(pushSize.x, pushSize.y);
		coolitem.setSize(pushSize);
		coolitem.setMinimumSize(toolitem.getWidth(), pushSize.y);
	
		toolbar = new ToolBar(coolbar,SWT.FLAT);
		toolitem = new ToolItem(toolbar,0);
		image = createImage("images/zoomin.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Zoom in"));
		toolitem.setData(new Integer(TOOLBAR_ZOOM_IN));
		toolitem.addSelectionListener(selectionListener);
		toolitem = new ToolItem(toolbar,0);
		image = createImage("images/zoomout.gif"); 
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Zoom out"));
		toolitem.setData(new Integer(TOOLBAR_ZOOM_OUT));
		toolitem.addSelectionListener(selectionListener);
		toolitem = new ToolItem(toolbar,SWT.CHECK);
		image = createImage("images/showgrid.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Show the dancefloor's grid"));
		toolitem.setData(new Integer(TOOLBAR_SHOW_GRID));
		toolitem.addSelectionListener(selectionListener);
		toolitem.setSelection(true);
		toolitem = new ToolItem(toolbar,SWT.CHECK);
		image = createImage("images/showprev.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Show previous step"));
		toolitem.setData(new Integer(TOOLBAR_SHOW_PREV));
		toolitem.addSelectionListener(selectionListener);
		toolitem = new ToolItem(toolbar,SWT.CHECK);
		image = createImage("images/shownext.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Show next step"));
		toolitem.setData(new Integer(TOOLBAR_SHOW_NEXT));
		toolitem.addSelectionListener(selectionListener);
		toolitem = new ToolItem(toolbar,SWT.CHECK);
		image = createImage("images/showgent.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Show feet of gent"));
		toolitem.setData(new Integer(TOOLBAR_SHOW_GENT));
		toolitem.addSelectionListener(selectionListener);
		toolitem.setSelection(true);
		toolitem = new ToolItem(toolbar,SWT.CHECK);
		image = createImage("images/showlady.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Show feet of lady"));
		toolitem.setData(new Integer(TOOLBAR_SHOW_LADY));
		toolitem.addSelectionListener(selectionListener);
		toolitem.setSelection(true);
		toolitem = new ToolItem(toolbar,SWT.CHECK);
		image = createImage("images/showanim.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Show animation"));
		toolitem.setData(new Integer(TOOLBAR_SHOW_ANIM));
		toolitem.addSelectionListener(selectionListener);
		toolitem = new ToolItem(toolbar,SWT.DROP_DOWN);
		image = createImage("images/showanimno.gif");
		toolitem.setImage(image);
		toolitem.setToolTipText(_("Select the number of Animationframes"));
		toolitem.addSelectionListener(new FramesDropDownSelectionListener(shell));
	
		coolitem = new CoolItem(coolbar,0);
		coolitem.setControl(toolbar);
		pushSize = toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		pushSize = coolitem.computeSize(pushSize.x, pushSize.y);
		coolitem.setSize(pushSize);
		coolitem.setMinimumSize(toolitem.getWidth(), pushSize.y);

		return coolbar;
	}
	
	/**
	 * Creates the navigation composite (for movement and rotation)
	 * 
	 * @param parent is the parent composite where this composite is added to
	 * @return the navigation composite
	 */
	private Composite createNavigationComposite(Composite parent)
	{
		Composite navigationComposite = new Composite(parent, 0);

		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		navigationComposite.setLayout(gridLayout);

		Button bt;
		bt = new Button(navigationComposite, 0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navul.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(-1, 1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});

		bt = new Button(navigationComposite, 0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navu.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(0, 1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		bt = new Button(navigationComposite, 0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navur.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(1, 1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});

		bt = new Button(navigationComposite, 0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navl.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(-1, 0);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		new Label(navigationComposite, 0);
		bt = new Button(navigationComposite, 0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navr.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(1, 0);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});

		bt = new Button(navigationComposite, 0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navdl.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(-1, -1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		bt = new Button(navigationComposite, 0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navd.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(0, -1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		bt = new Button(navigationComposite, 0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navdr.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(1, -1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});

		/* rotation */
		Composite rotationComposite = new Composite(navigationComposite, 0);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.CENTER;
		rotationComposite.setLayoutData(gridData);
		gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		rotationComposite.setLayout(gridLayout);

		bt = new Button(rotationComposite, 0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/rotl.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.rotateSelectedFeets(10);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		bt = new Button(rotationComposite, 0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/rotr.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.rotateSelectedFeets(-10);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		return navigationComposite;
	}

	/**
	 * Creates the step coordinates composite (for editing and selecting)
	 * 
	 * @param parent is the parent composite where this composite is added to
	 * @return the step coordinates composite
	 */
	private Composite createStepCoordinatesComposite(Composite parent)
	{
		Composite stepCoordinatesComposite = new Composite(parent, 0);
		GridLayout gridLayout = new GridLayout(7, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		stepCoordinatesComposite.setLayout(gridLayout);
		ModifyListener modifyListener = new ModifyListener()
		{
			public void modifyText(ModifyEvent event)
			{
				/* called when the user pressed enter */
				if (pattern == null) return;
				Step step = pattern.getCurrentStep();
				if (step == null) return;

				try
				{
					int newInt =
						Integer.parseInt(((Text) event.widget).getText());
					int which = ((Integer) event.widget.getData()).intValue();
					WayPoint feetCoord = step.getStartingWayPoint(which / 3);
					Step fakeStep = getFakeStep();
					WayPoint fakeWayPoint =
						fakeStep.getStartingWayPoint(which / 3);

					switch (which % 3)
					{
						case 0 :
							feetCoord.x = fakeWayPoint.x + newInt;
							break;
						case 1 :
							feetCoord.y = fakeWayPoint.y + newInt;
							break;
						case 2 :
							feetCoord.a = fakeWayPoint.a + newInt;
							break;
					}
					ballroom.redraw();
					detailedOverviewShell.refresh();
				}
				catch (NumberFormatException e)
				{
				}
			}
		};

		SelectionListener checkboxListener = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				boolean[] selectedArray = new boolean[4];
				selectedArray[0] = gentLeftSelectedButton.getSelection();
				selectedArray[1] = gentRightSelectedButton.getSelection();
				selectedArray[2] = ladyLeftSelectedButton.getSelection();
				selectedArray[3] = ladyRightSelectedButton.getSelection();
				ballroom.setSelectionArray(selectedArray);
			}
		};

		SelectionListener comboListener = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				if (pattern == null) return;
				Step step = pattern.getCurrentStep();
				if (step == null) return;

				int feet = ((Integer) (event.widget.getData())).intValue();
				step.getFoot(feet).setType(
					((Combo) event.widget).getSelectionIndex());
				ballroom.redraw();
			}
		};

		Label label;
		label = new Label(stepCoordinatesComposite, 0);
		label.setText(_("Gent"));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		label.setLayoutData(gridData);

		label = new Label(stepCoordinatesComposite, 0);
		label.setText(_("L"));
		gentLeftXText = createInteger(stepCoordinatesComposite,modifyListener,new Integer(0));
		gentLeftYText = createInteger(stepCoordinatesComposite,modifyListener,new Integer(1));
		gentLeftAngleText = createInteger(stepCoordinatesComposite, modifyListener,	new Integer(2));
		gentLeftTypeCombo = new Combo(stepCoordinatesComposite, SWT.READ_ONLY);
		for (int j = 0; j < Step.stepTypes.length; j++)
			gentLeftTypeCombo.add(Step.stepTypes[j]);
		gentLeftTypeCombo.setData(new Integer(0));
		gentLeftTypeCombo.select(0);
		gentLeftTypeCombo.addSelectionListener(comboListener);
		gentLeftSelectedButton = new Button(stepCoordinatesComposite, SWT.CHECK);
		gentLeftSelectedButton.addSelectionListener(checkboxListener);

		label = new Label(stepCoordinatesComposite, 0);
		label = new Label(stepCoordinatesComposite, 0);
		label.setText(_("R"));
		gentRightXText = createInteger(stepCoordinatesComposite,modifyListener,new Integer(3));
		gentRightYText = createInteger(stepCoordinatesComposite,modifyListener,new Integer(4));
		gentRightAngleText = createInteger(stepCoordinatesComposite,modifyListener,new Integer(5));
		gentRightTypeCombo = new Combo(stepCoordinatesComposite, SWT.READ_ONLY);
		for (int j = 0; j < Step.stepTypes.length; j++)
			gentRightTypeCombo.add(Step.stepTypes[j]);
		gentRightTypeCombo.select(0);
		gentRightTypeCombo.addSelectionListener(comboListener);
		gentRightTypeCombo.setData(new Integer(1));
		gentRightSelectedButton =
			new Button(stepCoordinatesComposite, SWT.CHECK);
		gentRightSelectedButton.addSelectionListener(checkboxListener);

		label = new Label(stepCoordinatesComposite, 0);
		label.setText(_("Lady"));
		gridData = new GridData();
		label.setLayoutData(gridData);

		label = new Label(stepCoordinatesComposite, 0);
		label.setText(_("L"));
		ladyLeftXText = createInteger(stepCoordinatesComposite,modifyListener,new Integer(6));
		ladyLeftYText =	createInteger(stepCoordinatesComposite,modifyListener,new Integer(7));
		ladyLeftAngleText =	createInteger(stepCoordinatesComposite,modifyListener,new Integer(8));
		ladyLeftTypeCombo = new Combo(stepCoordinatesComposite, SWT.READ_ONLY);
		for (int j = 0; j < Step.stepTypes.length; j++)
			ladyLeftTypeCombo.add(Step.stepTypes[j]);
		ladyLeftTypeCombo.select(0);
		ladyLeftTypeCombo.setData(new Integer(2));
		ladyLeftTypeCombo.addSelectionListener(comboListener);
		ladyLeftSelectedButton =
			new Button(stepCoordinatesComposite, SWT.CHECK);
		ladyLeftSelectedButton.addSelectionListener(checkboxListener);

		label = new Label(stepCoordinatesComposite, 0);
		label = new Label(stepCoordinatesComposite, 0);
		label.setText(_("R"));
		ladyRightXText = createInteger(stepCoordinatesComposite,modifyListener,new Integer(9));
		ladyRightYText = createInteger(stepCoordinatesComposite,modifyListener,new Integer(10));
		ladyRightAngleText = createInteger(stepCoordinatesComposite,modifyListener,	new Integer(11));
		ladyRightTypeCombo = new Combo(stepCoordinatesComposite, SWT.READ_ONLY);
		for (int j = 0; j < Step.stepTypes.length; j++)
			ladyRightTypeCombo.add(Step.stepTypes[j]);
		ladyRightTypeCombo.select(0);
		ladyRightTypeCombo.setData(new Integer(3));
		ladyRightTypeCombo.addSelectionListener(comboListener);
		ladyRightSelectedButton =
			new Button(stepCoordinatesComposite, SWT.CHECK);
		ladyRightSelectedButton.addSelectionListener(checkboxListener);
		
		return stepCoordinatesComposite;
	}
	
	public Shell open(Display display)
	{
		GridData gridData;

		shell = new Shell(display);
		shell.setText("SimpleDance");
		shell.addShellListener(new ShellAdapter()
		{
			public void shellClosed(ShellEvent arg0)
			{
				if (timerThread != null)
				{
					timerThread.interrupt();
					timerThread = null;
				}
			}
		});
		
		shell.addDisposeListener(new DisposeListener()
		{
			public void widgetDisposed(DisposeEvent event)
			{
				for (Image img : imageList)
					img.dispose();
				ballroom.dispose();
			}
		});

		shell.setLayout(new FillLayout());
		Composite mainComposite = new Composite(shell,0);
		mainComposite.setLayout(new GridLayout(1,false));
	
		CoolBar coolbar = createCoolBar(mainComposite);
		coolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		/* the sash form which links the left area with the right area */
		SashForm sashForm = new SashForm(mainComposite,SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH|GridData.GRAB_HORIZONTAL));

		/* the left area */
		Composite leftComposite = new Composite(sashForm,0);
		leftComposite.setLayout(new GridLayout(1,false));
		
		/* The ballroom */
		ballroom = new Ballroom(leftComposite,0);
		gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		ballroom.setLayoutData(gridData);
		ballroom.addBallroomListener(new BallroomListener()
		{
			public void coordinatesChanged(BallroomEvent event)
			{
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}

			public void selectionChanged(BallroomEvent be)
			{
				boolean [] selectionArray = ballroom.getSelectionArray();
				gentLeftSelectedButton.setSelection(selectionArray[0]);
				gentRightSelectedButton.setSelection(selectionArray[1]);
				ladyLeftSelectedButton.setSelection(selectionArray[2]);
				ladyRightSelectedButton.setSelection(selectionArray[3]);
			}
			
			public void viewChanged(BallroomEvent be)
			{
			}
		});
		
		/* The right area */
		Group rightComposite = new Group(sashForm,0);
		rightComposite.setLayout(new GridLayout(1,false));		
		rightComposite.setText(_("Overview"));

		stepOverviewTable = new Table(rightComposite,SWT.BORDER|SWT.FULL_SELECTION);
		stepOverviewTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		stepOverviewTable.setHeaderVisible(true);
		stepOverviewTable.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				storeDescription();
				pattern.setCurrentStepNum(stepOverviewTable.getSelectionIndex());

				ballroom.redraw();
				refreshStepCoordiantes();
				stepDescriptionStyledText.setText(pattern.getCurrentStep().getDescription());
				stepCountText.setText(pattern.getCurrentStep().getCount());
				if (pattern.getCurrentStep().isSlow()) durationCombo.select(0);
				else if (pattern.getCurrentStep().isQuick()) durationCombo.select(1);
				else durationCombo.setText(pattern.getCurrentStep().getDuration());
				lastStepSelected = stepOverviewTable.getSelectionIndex();
			}
		});
		
		TableColumn column = new TableColumn(stepOverviewTable,0);
		column.setText(_("Step"));
		column.setAlignment(SWT.CENTER);
		column.pack();

		column = new TableColumn(stepOverviewTable, 0);
		column.setText(_("Count"));
		column.pack();

		column = new TableColumn(stepOverviewTable, 0);
		column.setText(_("Tempo"));
		column.pack();
		
		Composite buttonComposite = new Composite(rightComposite,0);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonComposite.setLayout(new GridLayout(2,true));

		Button button = new Button(buttonComposite,0);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.setText(_("Add step"));
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				addNewStep();
			}
		});

		button = new Button(buttonComposite,0);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button.setText(_("Remove step"));
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				removeCurrentStep();
			}
		});

		/* step detail */
		Group stepDetailGroup = new Group(leftComposite,0);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;

		stepDetailGroup.setLayoutData(gridData);
		stepDetailGroup.setLayout(new GridLayout(3,false));
		
		/* stepCoordinates */
		Composite stepCoordinatesComposite = createStepCoordinatesComposite(stepDetailGroup);
		gridData = new GridData();
		gridData.verticalAlignment = GridData.CENTER;
		stepCoordinatesComposite.setLayoutData(gridData);
		
		/* movement */
		Composite movementComposite = createNavigationComposite(stepDetailGroup);
		gridData = new GridData();
		gridData.verticalAlignment = GridData.CENTER;
		movementComposite.setLayoutData(gridData);

		/* stuff */
		Composite stuffComposite = new Composite(stepDetailGroup,0);
		GridLayout gridLayout = new GridLayout(4,false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		stuffComposite.setLayout(gridLayout);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		stuffComposite.setLayoutData(gridData);

		Label countLabel = new Label(stuffComposite,0);
		countLabel.setText(_("Count"));

		stepCountText = new Text(stuffComposite,SWT.BORDER);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.widthHint = 30;
		stepCountText.setLayoutData(gridData);
		stepCountText.addModifyListener(new ModifyListener()
		{
			public void modifyText(ModifyEvent event)
			{
				Step step = pattern.getStep(stepOverviewTable.getSelectionIndex());
				if (step == null) return;
				
				step.setCount(((Text)event.widget).getText());
				TableItem ti = stepOverviewTable.getItem(stepOverviewTable.getSelectionIndex());
				if (ti != null) ti.setText(1,step.getCount()); 
			}
			
		});

		Label durationLabel = new Label(stuffComposite,0);
		durationLabel.setText(_("Tempo"));
		
		durationCombo = new Combo(stuffComposite,SWT.READ_ONLY);
		durationCombo.add(_("slow"));
		durationCombo.add(_("quick"));
		durationCombo.add("1/2");
		durationCombo.add("1/4");
		durationCombo.add("3/16");
		durationCombo.add("1/8");		durationCombo.add("1/16");
		durationCombo.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				if (pattern == null) return;
				Step step = pattern.getCurrentStep();
				if (step == null) return;

				Combo combo = (Combo)event.widget;
				int selectionIdx = combo.getSelectionIndex();
				if (selectionIdx == 0) step.setDuration("slow");
				else if (selectionIdx == 1) step.setDuration("quick");
				else step.setDuration(combo.getText());
				
				TableItem ti = stepOverviewTable.getItem(stepOverviewTable.getSelectionIndex());
				if (ti != null)
				{
					ti.setText(2,getDurationString(step));
				}
			}
		});

		stepDescriptionStyledText = new StyledText(stuffComposite,SWT.MULTI|SWT.BORDER|SWT.WRAP);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		stepDescriptionStyledText.setLayoutData(gridData);

		createMenuBar();

		sashForm.setWeights(new int[]{100,25});

		patternPropShell = new PatternProp(shell);
		detailedOverviewShell = new DetailedOverviewShell(shell);

		shell.open();
		return shell;
	}

	private Menu createMenuBar()
	{
		Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);
		
		createFileMenu(menuBar);
		createEditMenu(menuBar);
		createViewMenu(menuBar);
	
		return menuBar;
	}

	/**
	 * Set the given item index and accelerator using the default qualifier.
	 *
	 * @param item
	 * @param itemText
	 * @param key
	 */
	private void setMenuItemText(MenuItem item, String itemText, char key)
	{
		if (key != 0)
		{
			itemText = itemText + "\tCtrl+" + key;
			item.setAccelerator(SWT.CTRL | key);
		}
		item.setText(itemText);
	}

	@SuppressWarnings("unused")
	private void createFileMenu(Menu menuBar)
	{
		Menu menu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText(_("File"));
		item.setMenu(menu);

		// File -> New Contact
		MenuItem subItem = new MenuItem(menu, SWT.NULL);
		setMenuItemText(subItem, _("New pattern..."),'N');
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				newPattern();
			}
		});
		
		subItem = new MenuItem(menu, SWT.SEPARATOR);
		
		subItem = new MenuItem(menu, SWT.NULL);
		setMenuItemText(subItem, _("Open pattern..."), 'O');
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				loadPattern();
			}
		});

		subItem = new MenuItem(menu, SWT.NULL);
		setMenuItemText(subItem, _("Save pattern"), 'S');
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				savePattern();
			}
		});


		subItem = new MenuItem(menu, SWT.NULL);
		subItem.setText(_("Save pattern as..."));
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				savePatternAs();
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);
		
		/* disabled as this is not working */
		if (false)
		{
			subItem = new MenuItem(menu, SWT.NULL);
			subItem.setText(_("Export as PDF..."));
			subItem.addSelectionListener(new SelectionAdapter()
			{
				public void widgetSelected(SelectionEvent e)
				{
					exportAsPDF();
				}
			});
		}
		
		new MenuItem(menu, SWT.SEPARATOR);

		subItem = new MenuItem(menu, SWT.NULL);
		subItem.setText(_("Properties..."));
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				patternPropShell.open();
			}
		});

		subItem = new MenuItem(menu, SWT.NULL);
		subItem.setText(_("Detailed step overview..."));
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				detailedOverviewShell.open();
			}
		});
		
		
		new MenuItem(menu, SWT.SEPARATOR);

		subItem = new MenuItem(menu, SWT.NULL);
		setMenuItemText(subItem, _("Quit"), 'Q');
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				shell.dispose();
			}
		});
	}

	private void createEditMenu(Menu menuBar)
	{
		Menu menu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText(_("Edit"));
		item.setMenu(menu);

		// Edit -> Cut
		MenuItem subItem = new MenuItem(menu, SWT.NULL);
		setMenuItemText(subItem, _("Cut"), 'X');
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				if (pattern == null) return;
				Step step = pattern.getCurrentStep();
				if (step == null) return;
				copiedStep = pattern.getCurrentStep().duplicate(false);

				removeCurrentStep();
			}
		});
		
		subItem = new MenuItem(menu, SWT.NULL);
		setMenuItemText(subItem, _("Copy"), 'C');
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				if (pattern == null) return;
				Step step = pattern.getCurrentStep();
				if (step == null) return;
				copiedStep = pattern.getCurrentStep().duplicate(false);
			}
		});

		subItem = new MenuItem(menu, SWT.NULL);
		setMenuItemText(subItem, _("Paste"), 'V');
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				storeDescription();
				if (copiedStep == null) return;
				pattern.insertStep(copiedStep.duplicate(false));
				refreshAll();
				lastStepSelected = stepOverviewTable.getSelectionIndex();
			}
		});
		
		subItem = new MenuItem(menu, SWT.SEPARATOR);
		
		subItem = new MenuItem(menu, SWT.CHECK);
		subItem.setText(_("Enter relative coordinates"));
		subItem.setSelection(coordinatesAreRelative);
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				MenuItem mi = (MenuItem)event.widget;
				coordinatesAreRelative = mi.getSelection();
				refreshStepCoordiantes();
			}
		});
	}
	
	private void createViewMenu(Menu menuBar)
	{
		Menu menu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText(_("View"));
		item.setMenu(menu);

		MenuItem subItem = new MenuItem(menu, SWT.NULL);
		subItem.setText(_("Zoom in") + "\t+");
		subItem.setAccelerator('+');
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				ballroom.zoomIn();
			}
		});
		
		subItem = new MenuItem(menu, SWT.NULL);
		subItem.setText(_("Zoom out") + "\t-");
		subItem.setAccelerator('-');
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				ballroom.zoomOut();
			}
		});
		
		subItem = new MenuItem(menu, SWT.SEPARATOR);
		
		subItem = new MenuItem(menu, SWT.CHECK);
		subItem.setText(_("Show gradients"));
		subItem.setSelection(useGradients);
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent event)
			{
				MenuItem mi = (MenuItem)event.widget;
				useGradients = mi.getSelection();
				ballroom.setShowGradients(useGradients);
			}
		});
	}

	/**
	 * Method setPattern.
	 * @param pattern
	 */
	public void setPattern(Pattern newPattern)
	{
		lastStepSelected = -1;
		this.pattern = newPattern;
		ballroom.setPattern(newPattern);
		patternPropShell.setPattern(newPattern);
		detailedOverviewShell.setPattern(newPattern); /* includes a refresh */
		refreshStepOverviewTable();
		lastStepSelected = stepOverviewTable.getSelectionIndex();
		refreshStepCoordiantes();

		stepDescriptionStyledText.setText(newPattern.getCurrentStep().getDescription());
		stepCountText.setText(newPattern.getCurrentStep().getCount());
		if (newPattern.getCurrentStep().isSlow()) durationCombo.select(0);
		else if (newPattern.getCurrentStep().isQuick()) durationCombo.select(1);
		else durationCombo.setText(newPattern.getCurrentStep().getDuration());
		
		newPattern.addPatternListener(new PatternListener()
		{
			public void newStepActive(Pattern thisPattern, int newStepNum)
			{
				if (pattern == thisPattern)
				{
					stepOverviewTable.select(newStepNum);
					refreshStepCoordiantes();
				}
			}

		});
	}
	
	private void storeDescription()
	{
		Step step = pattern.getStep(lastStepSelected);
		if (step == null) return;
		step.setDescription(stepDescriptionStyledText.getText());
	}
	
	private String getDurationString(Step step)
	{
		if (step.isQuick()) return _("quick");
		else if (step.isSlow()) return _("slow");
		return step.getDuration();
	}
	
	public void refreshStepOverviewTable()
	{
		stepOverviewTable.removeAll();
		for (int i=0;i<pattern.getStepLength();i++)
		{
			String stepStr;

			TableItem item = new TableItem(stepOverviewTable,0);
		
			if (i==0) stepStr = _("Initial");
			else stepStr = i + "";
		
			item.setText(0,stepStr);
			item.setText(1,pattern.getStep(i).getCount());
			item.setText(2,getDurationString(pattern.getStep(i)));
		}
		stepOverviewTable.getColumn(0).pack();
		stepOverviewTable.setSelection(pattern.getCurrentStepNum());
	}
	
	public Display getDisplay()
	{
		return shell.getDisplay();
	}
	
	private Step getFakeStep()
	{
		Step fakeStep;
		if (coordinatesAreRelative)
		{
			fakeStep = pattern.getPreviousStep();
			if (fakeStep == null) fakeStep = new Step(true);
		} else fakeStep = new Step(true);
		return fakeStep;
	}
	
	public void refreshStepCoordiantes()
	{
		if (pattern == null) return;
		Step step = pattern.getCurrentStep();
		if (step == null) return;
		
		Step fakeStep = getFakeStep();

		WayPoint feetCoord = step.getStartingWayPoint(0);
		WayPoint fakeCoord = fakeStep.getStartingWayPoint(0);
		setInteger(gentLeftXText,feetCoord.x - fakeCoord.x);
		setInteger(gentLeftYText,feetCoord.y - fakeCoord.y);
		setInteger(gentLeftAngleText,feetCoord.a - fakeCoord.a);
		gentLeftTypeCombo.select(step.getFoot(0).getType());
		
		feetCoord = step.getStartingWayPoint(1);
		fakeCoord = fakeStep.getStartingWayPoint(1);
		setInteger(gentRightXText,feetCoord.x - fakeCoord.x);
		setInteger(gentRightYText,feetCoord.y - fakeCoord.y);
		setInteger(gentRightAngleText,feetCoord.a - fakeCoord.a);
		gentRightTypeCombo.select(step.getFoot(1).getType());
		
		feetCoord = step.getStartingWayPoint(2);
		fakeCoord = fakeStep.getStartingWayPoint(2);
		setInteger(ladyLeftXText,feetCoord.x - fakeCoord.x);
		setInteger(ladyLeftYText,feetCoord.y - fakeCoord.y);
		setInteger(ladyLeftAngleText,feetCoord.a - fakeCoord.a);
		ladyLeftTypeCombo.select(step.getFoot(2).getType());
		
		feetCoord = step.getStartingWayPoint(3);
		fakeCoord = fakeStep.getStartingWayPoint(3);
		setInteger(ladyRightXText,feetCoord.x - fakeCoord.x);
		setInteger(ladyRightYText,feetCoord.y - fakeCoord.y);
		setInteger(ladyRightAngleText,feetCoord.a - fakeCoord.a);
		ladyRightTypeCombo.select(step.getFoot(3).getType());
	}
	
	class OpenDropDownSelectionListener extends DropDownSelectionListener
	{
		private Shell shell;
		
		OpenDropDownSelectionListener(Shell shell)
		{
			this.shell = shell;
		}

		protected Menu createDropDownMenu()
		{
			Menu menu = new Menu(shell);

			File directory = new File("./patterns/");
			if (directory.exists())
			{
				String [] list = directory.list(new FilenameFilter()
				{
					public boolean accept(File file, String name)
					{
						return name.endsWith(".sdn");
					}
				});

				LinkedList [] allPatternsArray = new LinkedList[Pattern.DANCE_MAX];
				for (int i=0;i<allPatternsArray.length;i++)
					allPatternsArray[i] = new LinkedList();

				for (int i=0;i<list.length;i++)
				{
					File file = new File(directory,list[i]);

					FileInputStream fis = null;
					try
					{
						fis = new FileInputStream(file);
						byte [] input = new byte[fis.available()];
						fis.read(input);
						String str = new String(input);
						Pattern.PatternInfo pi = Pattern.getPatternInfo(str);
						pi.data = file;
						allPatternsArray[pi.type].add(pi);
					}
					catch (FileNotFoundException e)
					{
					}
					catch (IOException e)
					{
					}
					finally
					{
						try
						{
							if (fis != null)
							{
								fis.close();
							}
						}
						catch (IOException e)
						{
						}
					}
				}

				/* All patterns types get moved into the list */
				LinkedList<Integer> allPatternsList = new LinkedList<Integer>();
				for (int i=0;i<allPatternsArray.length;i++)
				{
					if (allPatternsArray[i].size()>0)
						allPatternsList.add(new Integer(i));
				}
				
				Collections.sort(allPatternsList,new Comparator()
				{
					public int compare(Object arg0, Object arg1)
					{
						String s0 = _(Pattern.getTypeName(((Integer)arg0).intValue()));
						String s1 = _(Pattern.getTypeName(((Integer)arg1).intValue()));
						
						return s0.compareTo(s1);
					}
				});

				ListIterator<Integer> iter = allPatternsList.listIterator();
				
				while (iter.hasNext())
				{
					int i = (iter.next()).intValue();
					
					if (allPatternsArray[i].size()>0)
					{
						MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
						menuItem.setText(_(Pattern.getTypeName(i)));
						Menu subMenu = new Menu(menuItem);
						menuItem.setMenu(subMenu);

						for (int j=0;j<allPatternsArray[i].size();j++)
						{
							Pattern.PatternInfo pi = (Pattern.PatternInfo) allPatternsArray[i].get(j);
							MenuItem subItem = new MenuItem(subMenu,0);
							subItem.setText(pi.name);
							subItem.setData(pi.data);
							subItem.addSelectionListener(new SelectionAdapter()
							{
								public void widgetSelected(SelectionEvent event)
								{
									MenuItem selectedItem = (MenuItem)event.widget;
									File file = (File)selectedItem.getData();
									loadPattern(file.getPath());
								}
							});
						}							
					}
				}
			}
			return menu;
		}

		protected void normalSelected(SelectionEvent event)
		{
			loadPattern();
		}
	};
	
	class FramesDropDownSelectionListener extends DropDownSelectionListener
	{
		private Shell shell;
		
		FramesDropDownSelectionListener(Shell shell)
		{
			this.shell = shell;
		}

		protected Menu createDropDownMenu()
		{
			Menu menu = new Menu(shell);
			for (int i = 2; i < 10; i++)
			{
				String text = i + " Frames";
				if (text.length() != 0) {
					MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
					menuItem.setText(text);

					if (i==6)
					{
						menuItem.setSelection(true);
					}
				}
			}
			return menu;
		}

		protected void normalSelected(SelectionEvent event)
		{
		}
	}
	
	public void refreshAll()
	{
		refreshStepOverviewTable();
		detailedOverviewShell.refresh();
		ballroom.redraw();
		refreshStepCoordiantes();
		stepDescriptionStyledText.setText(pattern.getCurrentStep().getDescription());
		stepCountText.setText(pattern.getCurrentStep().getCount());
		if (pattern.getCurrentStep().isSlow()) durationCombo.select(0);
		else if (pattern.getCurrentStep().isQuick()) durationCombo.select(1);
		else durationCombo.setText(pattern.getCurrentStep().getDuration());
	}
	
	public void removeCurrentStep()
	{
		if (pattern.removeCurrentStep())
		{
			refreshAll();
			lastStepSelected = stepOverviewTable.getSelectionIndex();
		}
	}
	
	public void addNewStep()
	{
		storeDescription();
		pattern.addStep();

		refreshAll();
		lastStepSelected = stepOverviewTable.getSelectionIndex();
	}
	
	public void loadPattern(String fileName)
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(fileName);
			byte [] input = new byte[fis.available()];
			fis.read(input);
			String str = new String(input);
			
			Pattern newPattern = Pattern.fromString(str); 
			newPattern.setFilename(fileName);
				
			setPattern(newPattern);
		} catch (FileNotFoundException e)
		{
			MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
			mb.setMessage(e.getLocalizedMessage());
			mb.open();
		} catch (IOException ioex)
		{
			MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
			mb.setMessage(ioex.getLocalizedMessage());
			mb.open();
		} finally
		{
			try
			{
				if (fis != null)
				{
					fis.close();
				}
			} catch (IOException e)
			{
			}
		}

	}
	
	public void newPattern()
	{
		MessageBox mb = new MessageBox(shell,SWT.ICON_QUESTION|SWT.NO|SWT.YES);
		mb.setMessage("This will erase the current pattern. Are you sure?");
		if ((mb.open() & SWT.YES) != 0)
		{
			setPattern(new Pattern());
		}
	}

	public void loadPattern()
	{
		FileDialog fileDialog = new FileDialog(shell,SWT.OPEN);
		fileDialog.setFilterExtensions(new String[]{"*.sdn","*.*"});
		fileDialog.setFilterNames(new String[]{"SimpleDance Pattern File","All files"});

		String fileName = fileDialog.open();
		if (fileName != null)
		{
			loadPattern(fileName);
		}
	}
	
	public void savePattern()
	{
		String fileName = pattern.getFilename();
		if (fileName == null)
		{
			savePatternAs();
			return;
		}

		if (!fileName.endsWith(".sdn")) fileName += ".sdn";
		File file = new File(fileName);
		boolean cont = true;

		if (file.exists())
		{
			MessageBox mbox = new MessageBox(shell,SWT.ICON_WARNING|SWT.YES|SWT.NO);
			mbox.setMessage(_("This file already exists. Are you sure to overwrite it?"));
			int rc = mbox.open();
			cont = (rc & SWT.YES) != 0;
		}
		if (cont)
		{
			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream(file);
				PrintStream pw = new PrintStream(fos);
				pw.println(pattern.toString());
				pw.close();
			} catch (FileNotFoundException e) {
				MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
				mb.setMessage(e.getLocalizedMessage());
				mb.open();
			} finally
			{
				try
				{
					if (fos != null)
					{
						fos.close();
					}
				} catch (IOException e)
				{
				}
			}
		}
		file = null;
		System.runFinalization();
		
	}
	
	public void savePatternAs()
	{
		storeDescription();

		FileDialog fileDialog = new FileDialog(shell,SWT.SAVE);
		fileDialog.setFilterExtensions(new String[]{"*.sdn","*.*"});
		fileDialog.setFilterNames(new String[]{"SimpleDance Pattern File","All files"});
		fileDialog.setFileName(Pattern.getTypeName(pattern.getType()) + " - " + pattern.getName() + ".sdn");

		String fileName = fileDialog.open();
		if (fileName != null)
		{
			pattern.setFilename(fileName);
			savePattern();
		}
	}
	

	/**
	 * Exports the current step as PDF
	 */
	protected void exportAsPDF()
	{
		storeDescription();

		FileDialog fileDialog = new FileDialog(shell,SWT.SAVE);
		fileDialog.setFilterExtensions(new String[]{"*.pdf"});
		fileDialog.setFilterNames(new String[]{"PDF Document"});
		fileDialog.setFileName(Pattern.getTypeName(pattern.getType()) + " - " + pattern.getName() + ".pdf");

		String fileName = fileDialog.open();
		if (fileName != null)
		{
			PDFOutput.write(fileName,pattern,getDisplay());
		}
		
	}


	/**
	 * Starts playing the animation sequence..
	 * 
	 * @param singleStep defines if only one step should be animated
	 * @param backward defines if the anomation should be played packward
	 */
	private void play(boolean singleStep, boolean backward)
	{
		if (timerThread != null) return;
		Pattern.AnimationInfo ai = pattern.getAnimationInfo(25);
		timerThread = new TimerThread(1000*1000/ai.fp1000s,this);
		ballroom.animationInit(singleStep,backward);
		ballroom.redraw();
		timerThread.start();
	}
	
	public void play()
	{
		pattern.setCurrentStepNum(0);
		play(false,false);
	}
	
	public void playBackward()
	{
		play(true,true);
	}
	
	public void playForward()
	{
		play(true,false);
	}

	public void playStop()
	{
		if (timerThread == null) return;
		timerThread.interrupt();
		timerThread = null;
		ballroom.animationStop();
	}
	
	//***BEGIN Runnable
	public void run()
	{
		if (ballroom.isDisposed())
			return;

		if (!ballroom.animationNext())
		{
			if (timerThread != null)
			{
				timerThread.interrupt();
				timerThread = null;
			}
			ballroom.animationStop();
		}
	}
	//***END Runnable
}