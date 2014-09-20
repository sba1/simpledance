package de.sonumina.simpledance.graphics;

import org.junit.Assert;
import org.junit.Test;

public class PointTest
{
	@Test
	public void testRotate()
	{
		Point result = new Point(2,2).rotate(45, new Point(2,2));
		Assert.assertEquals(2, result.x);
		Assert.assertEquals(2, result.y);

		result = new Point(2,0).rotate(90);
		Assert.assertEquals(0, result.x);
		Assert.assertEquals(2, result.y);

		result = new Point(2,0).rotate(180);
		Assert.assertEquals(-2, result.x);
		Assert.assertEquals(0, result.y);
}
}
