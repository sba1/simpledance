/*
 * Created on 01.04.2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.sonumina.simpledance.graphics.swt;

import org.eclipse.swt.widgets.Display;

import de.sonumina.simpledance.graphics.Color;


/**
 * @author sba
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SWTColor extends Color
{
	org.eclipse.swt.graphics.Color col;

	/**
	 * Construct a color.
	 * 
	 * @param display defines the display where the color is allocated
	 * @param r defines the red part of the color
	 * @param g defines the green part of the color
	 * @param b defines the blue part of the color
	 */
	public SWTColor(Display display, int r, int g, int b) {
		super(r, g, b);
		col = new org.eclipse.swt.graphics.Color(display,r,g,b);
	}
	
	public void dispose()
	{
		col.dispose();
	}
}
