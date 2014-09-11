import gnu.gettext.GettextResource;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DetailedOverviewCanvas extends Canvas
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

	private Color whiteColor;
	private Color greyColor;
	private Color lightBlueColor;
	private Color blueColor;
	private Color blackColor;

	private GC fakeGC;

	/* listener Stuff */
	private LinkedList<DetailedOverviewListener> detailedOverviewListener;

	/* the table's contents */
	private int columns;
	private int rows;
	private Cell cells[][];
	Fraction maxFrac;
	private Fraction tempoFrac[];

	/* Layout stuff */
	private int cellpadding = 2;
	private int gridthinkness = 1;
	private int [] columnWidths;
	private int [] rowHeights;
	private int tempoNominator;
	private int tempoDenominator;
	private int currentStepNum;
	
	private int tableHeight = 100;
	private int tableWidth = 100;
	private int width = 200;
	private int height = 200;
	
	private String [] leftLabel = new String[]
	{
		_("Step"),
		_("Gent"),
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		_("Lady"),
		"",
		"",
		"",
		"",
		"",
		"",
		"",
		_("Count"),
	};

	private class Cell
	{
		public String text;
		public int x;
		public int y;

		public int width;
		public int height;
		
		public Cell(String text){ this.text = text;};
		public void layout(GC gc)
		{
			Point p = gc.textExtent(text);
			width = p.x;
			height = p.y;
		}
		
		public void drawCentered(GC gc, int areaWidth, int areaHeight)
		{
			gc.drawText(text,x + (areaWidth - width)/2, y+(areaHeight - height)/2);
		}
	};

	public DetailedOverviewCanvas(Composite comp, int style)
	{
		super(comp,style|SWT.NO_BACKGROUND);

		detailedOverviewListener = new LinkedList<>();
		
		whiteColor = new Color(comp.getDisplay(),255,255,255);
		greyColor = new Color(comp.getDisplay(),210,210,210);
		blackColor = new Color(comp.getDisplay(),0,0,0);
		lightBlueColor = new Color(comp.getDisplay(),230,230,255);
		blueColor = new Color(comp.getDisplay(),200,200,230);

		addPaintListener(new PaintListener()
		{
			public void paintControl(PaintEvent event)
			{
				render(event.gc);
			}
		});
		
		addMouseListener(new MouseAdapter()
		{
			public void mouseDown(MouseEvent me)
			{
				for (int j=0;j<columns;j++)
				{
					for (int i=0;i<rows;i++)
					{
						Cell cell = cells[j][i];
						if (me.x >= cell.x && me.x <= cell.x + columnWidths[j] - 1 &&
							me.y >= cell.y && me.y <= cell.y + columnWidths[j] - 1)
						{
							if ((j != 0) && ((j - 1) != currentStepNum))
							{
								DetailedOverviewEvent ev = new DetailedOverviewEvent();
								currentStepNum = ev.stepNo = j - 1;
								redraw();
								emitDetailedOverviewEvent(ev,0);
							}
							break;
						}
					}
				}
			}
		});
		fakeGC = new GC(new Image(comp.getDisplay(),10,10));
	}

	public void dispose()
	{
		fakeGC.dispose();
		whiteColor.dispose();
		greyColor.dispose();
		blackColor.dispose();
		lightBlueColor.dispose();
		blueColor.dispose();
		super.dispose();
	}
	
	private int calculateColumnWidth(GC gc, String [] texts)
	{
		int width = 0;
		for (int i=0;i<texts.length;i++)
		{
			Point p = gc.textExtent(texts[i]);
			if (p.x > width) width = p.x;
		}
		return width;
	}

	private void drawBracket(GC gc, int x1, int y1, int width, int height)
	{
		int middleX = x1 + (width / 2) - 1;
		int middleY = y1 + (height / 2) - 1;
		int x2 = x1 + width - 1;
		int y2 = y1 + height - 1;
		
		int lx,ly;
		
		gc.drawLine(x1,y1,lx = x1+2, ly = middleY);
		gc.drawLine(lx,ly,lx = middleX - 2, ly);
		gc.drawLine(lx,ly,lx = middleX, ly = y2);
		gc.drawLine(lx,ly,lx = middleX + 2, ly = middleY);
		gc.drawLine(lx,ly,lx = x2 - 2, ly);
		gc.drawLine(lx,ly,lx = x2, ly = y1);
	} 

	private Point fractionExtent(GC gc,int nominator, int denominator)
	{
		Point nomPoint = gc.textExtent(nominator+"");
		Point denomPoint = gc.textExtent(denominator+"");
		Point p = new Point(nomPoint.x,nomPoint.y);
		if (denomPoint.x > p.x) p.x = denomPoint.x;
		p.y += denomPoint.y + 1;
		return p;
	}  

	private void drawFraction(GC gc, int x, int y, int nominator, int denominator)
	{
		Point nomPoint = gc.textExtent(nominator+"");
		Point denomPoint = gc.textExtent(denominator+"");
		Point p = new Point(nomPoint.x,nomPoint.y);
		if (denomPoint.x > p.x) p.x = denomPoint.x; 
		gc.drawText(nominator+"",x+(p.x-nomPoint.x)/2,y);
		y += nomPoint.y;
		gc.drawLine(x,y,x+p.x-1,y);
		y++;
		gc.drawText(denominator+"",x+(p.x-denomPoint.x)/2,y);
	}

	private void render(GC gc)
	{
		int x = 0;

		StringList stringList = new StringList();
		Fraction currentFrac = new Fraction(0,1);

		Rectangle bounds = getClientArea();
		gc.setBackground(whiteColor);
		gc.fillRectangle(bounds);

		/* draw Contents */
		for (int j=0;j<columns;j++)
		{
			Color col1;
			Color col2;

			if (j == currentStepNum+1)
			{
				col1 = greyColor;
				col2 = blueColor;
//				Cell cell = cells[j][0];
//				gc.setForeground(blackColor);
//				gc.drawRectangle(cell.x,cell.y,columnWidths[j],cell.y + tableHeight);
			} else
			{
				col1 = whiteColor;
				col2 = lightBlueColor;
			} 
			
			for (int i=0;i<rows;i++)
			{
				Cell cell = cells[j][i];
				Color background;
				if (i%2==1) background = col1;
				else background = col2;
				gc.setBackground(background);
				gc.fillRectangle(cell.x,cell.y,columnWidths[j],rowHeights[i]);
				cell.drawCentered(gc,columnWidths[j],rowHeights[i]);
			}
			
		}

		/* draw the bottom tempo information */
		int startX = 0;
		boolean setNewStartX = true;

		for (int i=1;i<tempoFrac.length;i++)
		{
			boolean drawBracket = false;
			boolean isLast = false;
			if (setNewStartX)
			{
				startX = cells[i+1][0].x;
				setNewStartX = false;
			}

			Fraction stepFrac = tempoFrac[i];//pattern.getStepTempo(i);
			currentFrac.add(stepFrac);
			
			if (currentFrac.compare(maxFrac)>=0)
			{
				int countdown = 10;
				while (currentFrac.compare(maxFrac)>=0 && countdown-- != 0)
					currentFrac.sub(maxFrac);
				drawBracket = true;
			} else if (i==tempoFrac.length-1)
			{
				drawBracket = true;
				isLast = true;
			}
			
			if (drawBracket == true)
			{
				int endX = cells[i+1][0].x + columnWidths[i+1];
				if (isLast) endX += 40;
				int w = endX-startX+1;
				if (currentFrac.nominator != 0  && !isLast)
				{
					w -= columnWidths[i+1] * currentFrac.nominator / currentFrac.denominator;
				} else
				{
					setNewStartX = true;
				}
				drawBracket(gc,startX,tableHeight+2,w,6);

				Point p = fractionExtent(gc,tempoNominator,tempoDenominator);
				drawFraction(gc,startX + (w - p.x)/2,tableHeight+8,tempoNominator,tempoDenominator);
				
				if (!setNewStartX)
				{
					startX += w + 2; 
				}
			}
		}
	}

	public void setPattern(Pattern pattern)
	{
		columns = 1 + pattern.getStepLength();
		rows = 2 + pattern.getStep(0).getNumberOfFeets() * 4;

		cells = new Cell[columns][rows];
		columnWidths = new int[columns];
		rowHeights = new int[rows];
		cellpadding = 2;
		gridthinkness = 1;

		for (int i=0;i<rows;i++)
		{
			for (int j=0;j<columns;j++)
				cells[j][i] = new Cell("");
		}

		for (int i = 0; i<rows; i++)
		{
			if (i < leftLabel.length)
				cells[0][i] = new Cell(leftLabel[i]);
		}

		for (int j = 0; j < pattern.getStepLength();j++)
		{
			int i=0;
			cells[j+1][i] = new Cell(j + "");
			
			Step step = pattern.getStep(j);
			i++;
			for (int k=0;k<step.getNumberOfFeets();k++)
			{
				WayPoint wp = step.getFeet(k).getStartingWayPoint();
				cells[j+1][i++] = new Cell(wp.x + ""); 
				cells[j+1][i++] = new Cell(wp.y + "");
				cells[j+1][i++] = new Cell(wp.a + "");
				cells[j+1][i++] = new Cell(_(Step.stepTypes[step.getFeet(k).getType()]));
			}
			cells[j+1][i++] = new Cell(step.getCount());
		}

		tempoFrac = new Fraction[pattern.getStepLength()];

		for (int i=0;i<pattern.getStepLength();i++)
		{
			tempoFrac[i] = pattern.getStepTempo(i);
		}

		tempoNominator = pattern.getTimeSignatureBars();
		tempoDenominator = pattern.getTimeSignatureBeats();
		maxFrac = new Fraction(pattern.getTimeSignatureBars(),pattern.getTimeSignatureBeats());
		currentStepNum = pattern.getCurrentStepNum();

		redraw();
	}
	
	/**
	 * Layout the table and it's cells
	 */
	private void layoutMe()
	{
		tableWidth = 0;
		tableHeight = 0;
		/* layout the cells, we also find out about every row's height here */
		for (int i=0;i<rows;i++)
		{
			int rowHeight = 0;
			for (int j=0;j<columns;j++)
			{
				cells[j][i].layout(fakeGC);
				if (cells[j][i].height > rowHeight) rowHeight = cells[j][i].height; 
			}
			rowHeight += 2*cellpadding;
			rowHeights[i] = rowHeight;
			tableHeight += rowHeight;
		}
		tableHeight += (rows-1)*gridthinkness;
		
		/* determine the column width */
		for (int j=0;j<columns;j++)
		{
			int columnWidth = 0;
			for (int i=0;i<rows;i++)
			{
				cells[j][i].layout(fakeGC);
				if (cells[j][i].width > columnWidth) columnWidth = cells[j][i].width; 
			}
			columnWidth += 2*cellpadding;
			columnWidths[j] = columnWidth;
			tableWidth += columnWidth;
		}
		tableWidth += (columns-1)*gridthinkness;
		
		/* derermine the position, 3rd pass */
		int lx = 0;
		for (int j=0;j<columns;j++)
		{
			int ly = 0;
			for (int i=0;i<rows;i++)
			{
				cells[j][i].x = lx;
				cells[j][i].y = ly;
				
				ly += rowHeights[i] + gridthinkness;
			}
			lx += columnWidths[j] + gridthinkness;
		}
		
		Point p = fractionExtent(fakeGC,tempoNominator,tempoDenominator);
		height = tableHeight + p.y + 7;
		width = tableWidth;
	}
	
	/**
	 * @see org.eclipse.swt.widgets.Control#computeSize(int, int)
	 */
	public Point computeSize(int arg0, int arg1)
	{
		layoutMe();
		return new Point(width,height);
	}

	/* Event managment */
	
	/**
	 * Forwards the given event to all added listeners
	 * 
	 * @param ev the intance of the forwarded event
	 * @param type defines the method which should be called. Nothing defined yet.
	 */
	protected void emitDetailedOverviewEvent(DetailedOverviewEvent ev, int type)
	{
		for (DetailedOverviewListener dol : detailedOverviewListener)
			dol.stepClicked(ev);
	}

	/**
	 * Adds the specified DetailedOverview Listener.
	 * 
	 * @param dol defines the listener to add.
	 */
	public void addDetailedOverviewListener(DetailedOverviewListener dol)
	{
		detailedOverviewListener.add(dol);
	}
	
	/**
	 * Removes the specified DetailedOverview Listener.
	 * 
	 * @param dol defines the listener to remove (which must have been added before)
	 */
	public void removeDetailedOverviewListener(DetailedOverviewListener dol)
	{
		detailedOverviewListener.remove(dol);
	}
}
