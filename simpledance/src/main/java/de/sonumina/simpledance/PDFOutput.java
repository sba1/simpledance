package de.sonumina.simpledance;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;

import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

import de.sonumina.simpledance.graphics.Point;
import de.sonumina.simpledance.graphics.swt.SWTContext;

/*
 * Created on 02.12.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

/**
 * @author sba
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PDFOutput
{
	static void write(String fileName, Pattern pattern, Display display)
	{
		Document document = new Document(PageSize.A4);
		try
		{
			int i;

			PdfWriter.getInstance(document, new FileOutputStream(fileName));
			
			document.open();
			
			 Table table = new Table(3);
			 table.setBorderWidth(1);
			 table.setBorderColor(new Color(0, 0, 255));
			 table.setPadding(5);
			 table.setSpacing(5);
			 Cell cell = new Cell("header");
			 cell.setHeader(true);
			 cell.setColspan(3);
			 table.addCell(cell);
			 table.endHeaders();
			 cell = new Cell("example cell with colspan 1 and rowspan 2");
			 cell.setRowspan(2);
			 cell.setBorderColor(new Color(255, 0, 0));
			 table.addCell(cell);
			 table.addCell("1.1");
			 table.addCell("2.1");
			 table.addCell("1.2");
			 table.addCell("2.2");
			 table.addCell("cell test1");
			 cell = new Cell("big cell");
			 cell.setRowspan(2);
			 cell.setColspan(2);
			 table.addCell(cell);
			 table.addCell("cell test2");
			 document.add(table);

			 table = new Table(3);
			 table.setBorderWidth(1);
			 table.setBorderColor(new Color(0, 0, 255));
			 table.setPadding(5);
			 table.setSpacing(5);
			 cell = new Cell("header");
			 cell.setHeader(true);
			 cell.setColspan(3);
			 table.addCell(cell);
			 table.endHeaders();
			 cell = new Cell("example cell with colspan 1 and rowspan 2");
			 cell.setRowspan(2);
			 cell.setBorderColor(new Color(255, 0, 0));
			 table.addCell(cell);
			 table.addCell("1.1");
			 table.addCell("2.1");
			 table.addCell("1.2");
			 table.addCell("2.2");
			 table.addCell("cell test1");
			 cell = new Cell("big cell");
			 cell.setRowspan(2);
			 cell.setColspan(2);
			 table.addCell(cell);
			 table.addCell("cell test2");
			 document.add(table);

			if (pattern.getStepLength() != 0)
			{
				int [] bounds = new int[4];
				int [] prevBounds = pattern.getStep(0).getStepBounds();
				int [] currBounds;

				for (i=0;i<pattern.getStepLength();i++)
				{
			 table = new Table(3);
			 table.setBorderWidth(1);
			 table.setBorderColor(new Color(0, 0, 255));
			 table.setPadding(5);
			 table.setSpacing(5);
			 cell = new Cell("header");
			 cell.setHeader(true);
			 cell.setColspan(3);
			 table.addCell(cell);
			 table.endHeaders();
			 cell = new Cell("example cell with colspan 1 and rowspan 2");
			 cell.setRowspan(2);
			 cell.setBorderColor(new Color(255, 0, 0));
			 table.addCell(cell);
			 table.addCell("1.1");
			 table.addCell("2.1");
			 table.addCell("1.2");
			 table.addCell("2.2");
			 table.addCell("cell test1");
			 cell = new Cell("big cell");
			 cell.setRowspan(2);
			 cell.setColspan(2);
			 table.addCell(cell);
			 table.addCell("cell test2");
			 
			 document.add(table);
			 
			 
					Step step = pattern.getStep(i);

					currBounds = step.getStepBounds();
					for (int j=0;j<4;j++) bounds[j] = currBounds[j];
					
					if (prevBounds[0] < bounds[0]) bounds[0] = prevBounds[0];
					if (prevBounds[1] > bounds[1]) bounds[1] = prevBounds[1];
					if (prevBounds[2] > bounds[2]) bounds[2] = prevBounds[2];
					if (prevBounds[3] < bounds[3]) bounds[3] = prevBounds[3];
					prevBounds = currBounds;
			
					int width = 500;
					int height = 500;

					Image bufferImage = new Image(display,width,height);
					GC bufferGC = new GC(bufferImage);
					SWTContext context = new SWTContext(display);
					context.setGC(bufferGC);
					Render render = new Render(context);
					
					Render.RenderSceneArgs rsa = new Render.RenderSceneArgs();
					rsa.pattern = pattern;
					rsa.stepNumber = i;
					rsa.showGradients = false;
					rsa.showPrevStep = true;
					rsa.showGent = true;
					rsa.showLady = true;

					int visibleLeft = bounds[0] - 25;
					int visibleTop = bounds[1] + 25;
					int visibleWidth = Math.abs(bounds[2] - bounds[0]) + 51;
					int visibleHeight = Math.abs(bounds[3] - bounds[1]) + 51;
					
					if (visibleWidth > visibleHeight)
					{
						visibleTop += (visibleWidth - visibleHeight)/2;
						visibleHeight = visibleWidth;
					} else if (visibleHeight > visibleWidth)
					{
						visibleLeft -= (visibleHeight - visibleWidth)/2;
						visibleWidth = visibleHeight;
					}
					rsa.visibleLeftTop = new Point(visibleLeft, visibleTop);
					rsa.visibleRightTop = new Point(visibleLeft + visibleWidth - 1, visibleTop);
					rsa.visibleLeftBottom = new Point(visibleLeft, visibleTop + visibleHeight - 1);
					rsa.visibleRightBottom = new Point(visibleLeft + visibleWidth - 1, visibleTop + visibleHeight - 1);

					rsa.pixelWidth = width;
					rsa.pixelHeight = height;
					render.renderScence(rsa);
					
					ImageData data = new ImageData(width,height,24,new PaletteData(0xff0000,0xff00,0xff));
					Image tempImage = new Image(display,data);
					bufferGC.copyArea(tempImage,0,0);
					data = tempImage.getImageData();
			
					com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(width,height,3,8,data.data);
					img.scalePercent(30);

					table = new Table(2);
//					table.setPadding(2f);
					
					cell = new Cell(img);
					table.addCell(cell);
					table.addCell("Test");

//					document.add(table);
					
					tempImage.dispose();
					bufferGC.dispose();
					bufferImage.dispose();
					
			//				document.add(table);									
				}
			}
				
			document.close();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (DocumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}
