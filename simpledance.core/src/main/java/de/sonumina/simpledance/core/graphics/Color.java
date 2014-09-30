package de.sonumina.simpledance.core.graphics;

/**
 * @author sba
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
abstract public class Color
{
	protected RGB rgb;
	
	public Color(int r, int g, int b){rgb = new RGB(r,g,b);};
	
	public int getRed(){return rgb.r;};
	public int getGreen(){return rgb.g;};
	public int getBlue(){return rgb.b;}
	public RGB getRGB(){ return rgb;}
}
