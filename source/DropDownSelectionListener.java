import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
abstract public class DropDownSelectionListener extends SelectionAdapter
{
	private Menu menu = null;
	
	abstract protected Menu createDropDownMenu();
	abstract protected void normalSelected(SelectionEvent event);
	
	public void widgetSelected(SelectionEvent event)
	{
		/**
		 * A selection event will be fired when a drop down tool
		 * item is selected in the main area and in the drop
		 * down arrow.  Examine the event detail to determine
		 * where the widget was selected.
		 */
		if (event.detail == SWT.ARROW)
		{
			/* The drop down arrow was selected. */
			if (menu != null)
			{
				/* Hide the menu to give the Arrow the appearance of being a toggle button.*/
				menu.setVisible(false);
				menu.dispose();
				menu = null;
			}

			menu = createDropDownMenu();

			/* Position the menu below and vertically aligned with the the drop down tool button. */
			final ToolItem toolItem = (ToolItem) event.widget;
			final ToolBar  toolBar = toolItem.getParent();
					
			Rectangle toolItemBounds = toolItem.getBounds();
			Point point = toolBar.toDisplay(new Point(toolItemBounds.x, toolItemBounds.y));
			menu.setLocation(point.x, point.y + toolItemBounds.height);
			menu.setVisible(true);
		} else
		{
			normalSelected(event);
		}
	}
}
