package de.sonumina.simpledance;
/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface BallroomListener 
{
	public void coordinatesChanged(BallroomEvent be);
	public void selectionChanged(BallroomEvent be);
	public void viewChanged(BallroomEvent be);
}
