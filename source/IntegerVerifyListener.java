import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class IntegerVerifyListener implements VerifyListener
{
	public void verifyText(VerifyEvent ve)
	{
		int start;
		if (ve.text.startsWith("-") && ve.start == 0) start = 1;
		else start = 0;

		for (int j=start;j<ve.text.length();j++)
		{
			boolean isChar = true;
			for (int i=0;i<10;i++)
			{
				String str = i + "";
				if (ve.text.charAt(j) == str.charAt(0))
				{
					isChar = false;
					break;
				}
			}
			if (isChar)
			{
				ve.doit = false;
				break;
			}
		}
	}
}
