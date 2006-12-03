/*
 * Created on 01.04.2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package graphics;

/**
 * @author sba
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
abstract public class Context
{
	protected Color foreground;
	protected Color background;
	protected int lineWidth;
	protected Font font;
	
	/* Pixel operation */
	abstract public void putPixel(int x, int y);
	abstract public void drawPolyline(int [] data);
	abstract public void drawPolygon(int [] data);
	abstract public void fillPolygon(int [] data);
	abstract public void drawLine(int x0, int y0, int x1, int y1);

	abstract public void drawOval(int x, int y, int mx, int my);
	abstract public void drawText(String string, int x, int y, boolean transparent);
	abstract public Point stringExtent(String string);
	abstract public void gradientPolygon(int [] newData, RGB startRGB, RGB endRGB, int angle);
	
	/* Color operations */
	abstract public Color allocateColor(int r, int g, int b);
	abstract public void deallocateColor(Color color);

	/* Transform operations */
	abstract public void applyRotateTransformation(float angle);
	abstract public void applyTranslationTransformation(float x, float y);
	abstract public void applyScaleTransformation(float scale);
	abstract public void applyScaleXTransformation(float f);

	/**
	 * @return Returns the current background color.
	 */
	public Color getBackground()
	{
		return background;
	}
	
	/**
	 * @param background The background color to set.
	 */
	public void setBackground(Color background)
	{
		this.background = background;
	}
	
	/**
	 * @return Returns the foreground.
	 */
	public Color getForeground()
	{
		return foreground;
	}
	
	/**
	 * @param foreground The foreground to set.
	 */
	public void setForeground(Color foreground)
	{
		this.foreground = foreground;
	}

	/**
	 * @return Returns the lineWidth.
	 */
	public int getLineWidth()
	{
		return lineWidth;
	}

	/**
	 * @param lineWidth The lineWidth to set.
	 */
	public void setLineWidth(int lineWidth)
	{
		this.lineWidth = lineWidth;
	}

	/**
	 * @param object
	 */
	public void setFont(Font font)
	{
		this.font = font;
	}

	/**
	 * @return Returns the font.
	 */
	public Font getFont()
	{
		return font;
	}

}
