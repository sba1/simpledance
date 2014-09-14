package de.sonumina.simpledance;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author sebauer
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SimpleDance
{
	public static void main(String[] args)
	{
		boolean showLeaks = false;
		if (args.length >= 1 && args[0].equalsIgnoreCase("--show-leaks"))
			showLeaks = true;

		DeviceData data = new DeviceData();
		data.tracking = showLeaks;
		Display display = new Display(data);

		Dance dance = new Dance();
		Shell shell = dance.open(display);
		
		Pattern pattern = new Pattern();
		dance.setPattern(pattern);

		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}

		if (showLeaks)
		{
			Sleak sleak = new Sleak();
			sleak.open();
			while (!sleak.shell.isDisposed())
			{
				if (!display.readAndDispatch())
					display.sleep();
			}
		}

		display.dispose();
	}
}
