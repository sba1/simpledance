package de.sonumina.simpledance;

/**
 * Listener interface for intercepting of the Ballroom component.
 *
 * @author Sebastian Bauer
 */
public interface BallroomListener 
{
	public void coordinatesChanged(BallroomEvent be);
	public void selectionChanged(BallroomEvent be);
	public void viewChanged(BallroomEvent be);
}
