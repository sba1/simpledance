import gnu.gettext.GettextResource;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ResourceBundle;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfWriter;

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

	static void write(String fileName, Pattern pattern)
	{
		Document document = new Document();
		try
		{
			int i;

			PdfWriter.getInstance(document, new FileOutputStream(fileName));
			document.open();

			for (i=0;i<pattern.getStepLength();i++)
			{
				Table table = new Table(2,2);
				table.setPadding(2);
				table.addCell(new Paragraph("ww"));
				table.addCell("0.1");
				table.addCell("1.0");
				table.addCell("1.1"); 

				document.add(table);
									
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
