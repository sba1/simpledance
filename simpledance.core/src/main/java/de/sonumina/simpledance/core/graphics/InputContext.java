package de.sonumina.simpledance.core.graphics;

public class InputContext
{
	public enum Drag
	{
		NO,
		ROTATE_BALE,
		ROTATE_HEEL,
		MOVE_WAYPOINT
	}

	public int selectedStep = -1;
	public int selectedFoot = -1;
	public int selectedWaypoint = -1;
	public boolean mousePressed = false;
	public Drag dragOperation;
	public Point rotationCenterBallroomPoint;
	public int distance;

	/** An array to hold which foot is currently selected */
	public boolean [] selectedArray = new boolean[4];

	/**
	 * Return whether any foot is selected (according to the selectedArray)
	 *
	 * @return
	 */
	public boolean anySelected()
	{
		for (int i=0;i<selectedArray.length;i++)
		{
			if (selectedArray[i])
				return true;
		}
		return false;
	}

	/**
	 * Clear the foot selection.
	 */
	public void clearSelection()
	{
		for (int i=0;i<selectedArray.length;i++)
		{
			selectedArray[i] = false;
		}
	}

	/**
	 * Set the selection according to the given array.
	 *
	 * @param array
	 */
	public void setSelection(boolean [] array)
	{
		for (int i=0;i<selectedArray.length;i++)
			selectedArray[i] = array[i];

	}
}
