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
}
