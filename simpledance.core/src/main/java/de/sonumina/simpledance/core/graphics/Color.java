package de.sonumina.simpledance.core.graphics;

/**
 * A class for representing a color.
 *
 * @author Sebastian Bauer
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
