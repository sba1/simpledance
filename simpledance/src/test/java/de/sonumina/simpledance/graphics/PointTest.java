package de.sonumina.simpledance.graphics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PointTest
{
	@Test
	public void testRotate()
	{
		Point result = new Point(2,2).rotate(45, new Point(2,2));
		assertEquals(2, result.x);
		assertEquals(2, result.y);

		result = new Point(2,0).rotate(90);
		assertEquals(0, result.x);
		assertEquals(2, result.y);

		result = new Point(2,0).rotate(180);
		assertEquals(-2, result.x);
		assertEquals(0, result.y);
	}
}
