package de.sonumina.simpledance;
import org.eclipse.swt.widgets.Display;

/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TimerThread extends Thread
{
	private int millis;
	private Display display;
	private Runnable runnable;

	/**
	 * Constructor TimerThread.
	 * @param i
	 * @param display
	 */
	public TimerThread(Display display, int millis, Runnable runnable)
	{
		this.millis = millis; 
		this.display = display;
		this.runnable = runnable;
	}

	public void run()
	{
		try
		{
			while (!isInterrupted())
			{
				Thread.sleep(millis);
				if (display.isDisposed()) break;
				display.asyncExec(runnable);
			}
		}
		catch (InterruptedException e)
		{
		}
	}
}
