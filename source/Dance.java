import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ResourceBundle;

import gnu.gettext.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import Pattern.PatternInfo;

/**
 * @author sebauer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Dance implements Runnable
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

	private Pattern pattern;
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
	private LinkedList imageList = new LinkedList();
	
	private PatternProp patternPropShell;
	private DetailedOverviewShell detailedOverviewShell;
	private TimerThread timerThread;

	private boolean coordinatesAreRelative = true;

	private Text createInteger(Composite parent, ModifyListener modifyListener, Object data)
	{
		Text text = new Text(parent,SWT.BORDER);
		text.setTextLimit(5);
		Point p = text.computeSize(SWT.DEFAULT,SWT.DEFAULT);
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
			source = new ImageData(getClass().getResourceAsStream(name));

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
	
	public Shell open(Display display)
	{
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
				ListIterator iter = imageList.listIterator();
				while (iter.hasNext())
				{
					Image img = (Image)iter.next();
					img.dispose();
				}
				ballroom.dispose();
			}
		});

		shell.setLayout(new FillLayout());
		Composite mainComposite = new Composite(shell,0);
		mainComposite.setLayout(new GridLayout(2,false));
	
		GridData gridData;		
		
		/* Coolbar */
		CoolBar coolbar = new CoolBar(mainComposite,0);
		gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		coolbar.setLayoutData(gridData);

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
					case TOOLBAR_SHOW_ANIM: ballroom.setShowAnimation(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_SHOW_PREV: ballroom.setShowPrevStep(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_SHOW_NEXT: ballroom.setShowNextStep(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_SHOW_GENT: ballroom.setShowGent(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_SHOW_LADY: ballroom.setShowLady(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_SHOW_GRID: ballroom.setShowGrid(((ToolItem)event.widget).getSelection()); break;
					case TOOLBAR_PLAY: play(); break;
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

		Composite leftComposite = new Composite(mainComposite,0);
		leftComposite.setLayout(new GridLayout(1,false));
		gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		leftComposite.setLayoutData(gridData);
		
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
		Composite composite = new Composite(mainComposite,0);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.verticalSpan = 1;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(1,false));		

		/* step overview area */
		Group stepOverviewGroup = new Group(composite,0);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		gridData.grabExcessVerticalSpace = true;
		stepOverviewGroup.setLayoutData(gridData);
		stepOverviewGroup.setText(_("Overview"));
		stepOverviewGroup.setLayout(new GridLayout(2,false));

		stepOverviewTable = new Table(stepOverviewGroup,SWT.BORDER|SWT.FULL_SELECTION);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 2;
		stepOverviewTable.setLayoutData(gridData);
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

		Button button = new Button(stepOverviewGroup,0);
		button.setText(_("Add step"));
		button.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				addNewStep();
			}
		});

		button = new Button(stepOverviewGroup,0);
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
		stepDetailGroup.setLayout(new GridLayout(9,false));
		
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
					int newInt = Integer.parseInt(((Text)event.widget).getText());
					int which = ((Integer)event.widget.getData()).intValue();
					WayPoint feetCoord = step.getStartingWayPoint(which/3);
					Step fakeStep = getFakeStep();
					WayPoint fakeWayPoint = fakeStep.getStartingWayPoint(which/3);

					switch (which % 3)
					{
						case	0: feetCoord.x = fakeWayPoint.x + newInt;break;  
						case	1: feetCoord.y = fakeWayPoint.y + newInt;break;
						case	2: feetCoord.a = fakeWayPoint.a + newInt;break;
					}
					ballroom.redraw();
					detailedOverviewShell.refresh();
				} catch(NumberFormatException e)
				{
				}
			}
		};
		
		SelectionListener checkboxListener = new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				boolean [] selectedArray = new boolean[4];
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
				
				int feet = ((Integer)(event.widget.getData())).intValue();
				step.getFeet(feet).setType(((Combo)event.widget).getSelectionIndex());
				ballroom.redraw();
			}
		};

		Label label;
		label = new Label(stepDetailGroup,0);
		label.setText(_("Gent"));
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.END;
		label.setLayoutData(gridData);

		label = new Label(stepDetailGroup,0);
		label.setText(_("L"));
		gentLeftXText = createInteger(stepDetailGroup,modifyListener,new Integer(0));
		gentLeftYText = createInteger(stepDetailGroup,modifyListener,new Integer(1));
		gentLeftAngleText = createInteger(stepDetailGroup,modifyListener,new Integer(2));
		gentLeftTypeCombo = new Combo(stepDetailGroup,SWT.READ_ONLY);
		for (int j=0;j<Step.stepTypes.length;j++)
			gentLeftTypeCombo.add(Step.stepTypes[j]);
		gentLeftTypeCombo.setData(new Integer(0));
		gentLeftTypeCombo.select(0);
		gentLeftTypeCombo.addSelectionListener(comboListener);
		gentLeftSelectedButton = new Button(stepDetailGroup,SWT.CHECK);
		gentLeftSelectedButton.addSelectionListener(checkboxListener);
		
		/* movement */
		Composite movementComposite = new Composite(stepDetailGroup,0);
		gridData = new GridData();
		gridData.verticalSpan = 4;
		movementComposite.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout(3,false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		movementComposite.setLayout(gridLayout);

		Button bt;
		bt = new Button(movementComposite,0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navul.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(-1,1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});

		bt = new Button(movementComposite,0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navu.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(0,1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		bt = new Button(movementComposite,0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navur.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(1,1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});

		bt = new Button(movementComposite,0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navl.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(-1,0);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		new Label(movementComposite,0);
		bt = new Button(movementComposite,0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navr.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(1,0);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});

		bt = new Button(movementComposite,0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navdl.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(-1,-1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		bt = new Button(movementComposite,0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navd.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(0,-1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		bt = new Button(movementComposite,0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/navdr.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.moveSelectedFeets(1,-1);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});

		/* rotation */
		Composite rotationComposite = new Composite(movementComposite,0);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.CENTER;
		rotationComposite.setLayoutData(gridData);
		gridLayout = new GridLayout(2,false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		rotationComposite.setLayout(gridLayout);

		bt = new Button(rotationComposite,0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/rotl.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.rotateSelectedFeets(-10);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});
		bt = new Button(rotationComposite,0);
		bt.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		bt.setImage(createImage("images/rotr.gif"));
		bt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent ev)
			{
				ballroom.rotateSelectedFeets(10);
				refreshStepCoordiantes();
				detailedOverviewShell.refresh();
			}
		});

		Composite stuffComposite = new Composite(stepDetailGroup,0);
		gridLayout = new GridLayout(4,false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		stuffComposite.setLayout(gridLayout);
		gridData = new GridData();
		gridData.verticalSpan = 4;
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

		label = new Label(stepDetailGroup,0);
		label = new Label(stepDetailGroup,0);
		label.setText(_("R"));
		gentRightXText = createInteger(stepDetailGroup,modifyListener,new Integer(3));
		gentRightYText = createInteger(stepDetailGroup,modifyListener,new Integer(4));
		gentRightAngleText = createInteger(stepDetailGroup,modifyListener,new Integer(5));
		gentRightTypeCombo = new Combo(stepDetailGroup,SWT.READ_ONLY);
		for (int j=0;j<Step.stepTypes.length;j++)
			gentRightTypeCombo.add(Step.stepTypes[j]);
		gentRightTypeCombo.select(0);
		gentRightTypeCombo.addSelectionListener(comboListener);
		gentRightTypeCombo.setData(new Integer(1));
		gentRightSelectedButton = new Button(stepDetailGroup,SWT.CHECK);
		gentRightSelectedButton.addSelectionListener(checkboxListener);

		label = new Label(stepDetailGroup,0);
		label.setText(_("Lady"));
		gridData = new GridData();
		label.setLayoutData(gridData);

		label = new Label(stepDetailGroup,0);
		label.setText(_("L"));
		ladyLeftXText = createInteger(stepDetailGroup,modifyListener,new Integer(6));
		ladyLeftYText = createInteger(stepDetailGroup,modifyListener,new Integer(7));
		ladyLeftAngleText = createInteger(stepDetailGroup,modifyListener,new Integer(8));
		ladyLeftTypeCombo = new Combo(stepDetailGroup,SWT.READ_ONLY);
		for (int j=0;j<Step.stepTypes.length;j++)
			ladyLeftTypeCombo.add(Step.stepTypes[j]);
		ladyLeftTypeCombo.select(0);
		ladyLeftTypeCombo.setData(new Integer(2));
		ladyLeftTypeCombo.addSelectionListener(comboListener);
		ladyLeftSelectedButton = new Button(stepDetailGroup,SWT.CHECK);
		ladyLeftSelectedButton.addSelectionListener(checkboxListener);

		label = new Label(stepDetailGroup,0);
		label = new Label(stepDetailGroup,0);
		label.setText(_("R"));
		ladyRightXText = createInteger(stepDetailGroup,modifyListener,new Integer(9));
		ladyRightYText = createInteger(stepDetailGroup,modifyListener,new Integer(10));
		ladyRightAngleText = createInteger(stepDetailGroup,modifyListener,new Integer(11));
		ladyRightTypeCombo = new Combo(stepDetailGroup,SWT.READ_ONLY);
		for (int j=0;j<Step.stepTypes.length;j++)
			ladyRightTypeCombo.add(Step.stepTypes[j]);
		ladyRightTypeCombo.select(0);
		ladyRightTypeCombo.setData(new Integer(3));
		ladyRightTypeCombo.addSelectionListener(comboListener);
		ladyRightSelectedButton = new Button(stepDetailGroup,SWT.CHECK);
		ladyRightSelectedButton.addSelectionListener(checkboxListener);

		createMenuBar();

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
	
		return menuBar;
	}

	private void createFileMenu(Menu menuBar)
	{
		Menu menu = new Menu(shell, SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText(_("File"));
		item.setMenu(menu);

		// File -> New Contact
		MenuItem subItem = new MenuItem(menu, SWT.NULL);
		subItem.setText(_("New pattern"));
		subItem.setAccelerator(SWT.CTRL + 'N');
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				newPattern();
			}
		});
		
		subItem = new MenuItem(menu, SWT.NULL);
		subItem.setText(_("Open pattern..."));
		subItem.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0)
			{
				loadPattern();
			}
		});

		subItem = new MenuItem(menu, SWT.NULL);
		subItem.setText(_("Save pattern"));
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
		subItem.setText(_("Quit..."));
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

		// File -> New Contact
		MenuItem subItem = new MenuItem(menu, SWT.NULL);
		subItem.setText(_("Cut"));
		subItem.setAccelerator(SWT.CTRL + 'X');
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
		subItem.setText(_("Copy"));
		subItem.setAccelerator(SWT.CTRL + 'C');
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
		subItem.setText(_("Paste"));
		subItem.setAccelerator(SWT.CTRL + 'V');
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

	/**
	 * Method setPattern.
	 * @param pattern
	 */
	public void setPattern(Pattern pattern)
	{
		this.pattern = pattern;
		ballroom.setPattern(pattern);
		patternPropShell.setPattern(pattern);
		detailedOverviewShell.setPattern(pattern); /* includes a refresh */
		refreshStepOverviewTable();
		refreshStepCoordiantes();

		stepDescriptionStyledText.setText(pattern.getCurrentStep().getDescription());
		stepCountText.setText(pattern.getCurrentStep().getCount());
		if (pattern.getCurrentStep().isSlow()) durationCombo.select(0);
		else if (pattern.getCurrentStep().isQuick()) durationCombo.select(1);
		else durationCombo.setText(pattern.getCurrentStep().getDuration());
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
		gentLeftTypeCombo.select(step.getFeet(0).getType());
		
		feetCoord = step.getStartingWayPoint(1);
		fakeCoord = fakeStep.getStartingWayPoint(1);
		setInteger(gentRightXText,feetCoord.x - fakeCoord.x);
		setInteger(gentRightYText,feetCoord.y - fakeCoord.y);
		setInteger(gentRightAngleText,feetCoord.a - fakeCoord.a);
		gentRightTypeCombo.select(step.getFeet(1).getType());
		
		feetCoord = step.getStartingWayPoint(2);
		fakeCoord = fakeStep.getStartingWayPoint(2);
		setInteger(ladyLeftXText,feetCoord.x - fakeCoord.x);
		setInteger(ladyLeftYText,feetCoord.y - fakeCoord.y);
		setInteger(ladyLeftAngleText,feetCoord.a - fakeCoord.a);
		ladyLeftTypeCombo.select(step.getFeet(2).getType());
		
		feetCoord = step.getStartingWayPoint(3);
		fakeCoord = fakeStep.getStartingWayPoint(3);
		setInteger(ladyRightXText,feetCoord.x - fakeCoord.x);
		setInteger(ladyRightYText,feetCoord.y - fakeCoord.y);
		setInteger(ladyRightAngleText,feetCoord.a - fakeCoord.a);
		ladyRightTypeCombo.select(step.getFeet(3).getType());
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

					try
					{
						FileInputStream fis;
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
				}

				/* All patterns types get moved into the list */
				LinkedList allPatternsList = new LinkedList();
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

				ListIterator iter = allPatternsList.listIterator();
				
				while (iter.hasNext())
				{
					int i = ((Integer)(iter.next())).intValue();
					
					if (allPatternsArray[i].size()>0)
					{
						MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
						menuItem.setText(_(Pattern.getTypeName(i)));
						Menu subMenu = new Menu(menuItem);
						menuItem.setMenu(subMenu);

						for (int j=0;j<allPatternsArray[i].size();j++)
						{
							PatternInfo pi = (PatternInfo) allPatternsArray[i].get(j);
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
		try
		{
			FileInputStream fis = new FileInputStream(fileName);
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
		System.out.println(fileName); 
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
			try
			{
				FileOutputStream fos = new FileOutputStream(file); 
				PrintStream pw = new PrintStream(fos);
				pw.println(pattern.toString());
					
				pw = null;
				fos = null;
			} catch (FileNotFoundException e) {
				MessageBox mb = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
				mb.setMessage(e.getLocalizedMessage());
				mb.open();
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
	
	public void play()
	{
		if (timerThread != null) return;
		Pattern.AnimationInfo ai = pattern.getAnimationInfo(25);
		timerThread = new TimerThread(1000*1000/ai.fp1000s,this);
		
		pattern.setCurrentStepNum(0);
		ballroom.animationInit();
		ballroom.redraw();
		timerThread.start();
	}

	//***BEGIN Runnable
	public void run()
	{
		if (ballroom.isDisposed())
			return;

		if (!ballroom.animationNext())
		{
			timerThread.interrupt();
			timerThread = null;
			ballroom.animationStop();
		}
	}
	//***END Runnable
}