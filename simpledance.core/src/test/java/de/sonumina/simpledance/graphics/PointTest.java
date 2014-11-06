package de.sonumina.simpledance.graphics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sonumina.simpledance.core.graphics.Point;

public class PointTest
{
	@Test
	public void testAdd()
	{
		Point result = new Point(2,2).add(new Point(3,1));
		assertEquals(5, result.x);
		assertEquals(3, result.y);
	}

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

	@Test
	public void testIsContainedIn()
	{
		int data [] = new int[]
		{
				0,0,
				0,10,
				10,10
		};
		assertTrue(new Point(1,5).isContainedIn(data));
		assertFalse(new Point(100,5).isContainedIn(data));
		assertFalse(new Point(100,-5).isContainedIn(data));

		data = new int[]
		{
				0,0,
				0,10,
				0,20,
				0,30,
				10,40,
				20,50,
				30,40,
				50,30,
				50,0
		};

		for (int i=0; i < 50; i++)
			assertFalse(new Point(i,60).isContainedIn(data));
	}
}
