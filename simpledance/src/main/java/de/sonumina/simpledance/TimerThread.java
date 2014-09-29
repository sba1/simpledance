package de.sonumina.simpledance;
import org.eclipse.swt.widgets.Display;

/**
 * A special thread that invokes the given runnable on the context
 * of the main user interface thread in a periodic fashion.
 *s
 * @author Sebastian Bauer
 */
public class TimerThread extends Thread
{
	private int millis;
	private Display display;
	private Runnable runnable;

	/**
	 * Constructor TimerThread.
	 *
	 * @param display the display that is attached to the user interface thread.
	 * @param millis the period at which the runnable is invoked.
	 * @param runnable defines the runnable that is invoked on the context of the GUI
	 *  thread.
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
