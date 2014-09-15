package de.sonumina.simpledance;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class StringList
{
	private LinkedList list = new LinkedList();
	
	public String [] getListContent()
	{
		String [] array = new String[list.size()];
		ListIterator iter = list.listIterator();
		int i = 0;

		while (iter.hasNext())
			array[i++] = (String)iter.next();

		return array;
	}

	public void add(String str)
	{
		list.add(str);
	}
	
	public void clear()
	{
		list.clear();
	}
}
