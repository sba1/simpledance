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
	private Dance dance;

	/**
	 * Constructor TimerThread.
	 * @param i
	 * @param display
	 */
	public TimerThread(int millis, Dance dance)
	{
		this.millis = millis; 
		this.display = dance.getDisplay();
		this.dance = dance;
		
		
	}

	public void run()
	{
		try
		{
			while (!isInterrupted())
			{
				Thread.sleep(millis);
				if (display.isDisposed()) break;
				display.asyncExec(dance);
			}
		}
		catch (InterruptedException e)
		{
		}
	}
}
