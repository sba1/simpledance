package de.sonumina.simpledance;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.sonumina.simpledance.core.model.Pattern;

/**
 * The class that provides the main entry point of SimpleDance.
 *
 * @author Sebastian Bauer
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
