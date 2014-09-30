package de.sonumina.simpledance.core.model;

/**
 * Interface listener for observing changes in a pattern.
 *
 * @author Sebastian Bauer
 */
public interface PatternListener
{
	public void newStepActive(Pattern pattern, int newStepNum);
}
