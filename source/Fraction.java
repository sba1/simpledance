/**
 * This class represents a fraction and defines some operation
 * like addition and subtraction on it. 
 *
 * @author Sebastian Bauer
 */
public class Fraction
{
	/** The fractions nominator */
	public int nominator;

	/** The fractions denominator */
	public int denominator;

	/**
	 * Calculates the greatest common divisor of the given numbers.
	 * 
	 * @param x
	 * @param y
	 * @return the GCD looked for
	 */ 
	private int ggT(int x, int y)
	{
	  if (x>y)
	  {
		return ggT(x-y,y);
	  } else if (y>x)
	  {
		return ggT(y-x,x);
	  } else return x;
	}
	
	
	/**
	 * Cancels the Fraction down.
	 */
	private void cancel()
	{
		if (nominator == 0)
		{
			denominator = 1;
			return;
		}
		int ggt = ggT(Math.abs(nominator),denominator);
		nominator /= ggt;
		denominator /= ggt;
	}

	/**
	 * Construct a new fraction with given nominator and
	 * denominator. The fraction will automatically canceled
	 * down.
	 * 
	 * @param nominator
	 * @param denominator
	 */
	public Fraction(int nominator, int denominator)
	{
		this.nominator = nominator;
		this.denominator = denominator;
		
		cancel();
	}

	/**
	 * Adds the fraction represented by toAddNominator and
	 * toAddDenominatior to this fraction. The resulting 
	 * fraction is always canceled down.
	 * 
	 * @param toAddNominator
	 * @param toAddDenominatior
	 */
	public void add(int toAddNominator, int toAddDenominatior)
	{
		if (toAddDenominatior == denominator)
		{
			nominator += toAddNominator;
		} else
		{
			nominator = nominator * toAddDenominatior + toAddNominator * denominator;
			denominator *= toAddDenominatior;
			cancel();			
		}
	}
	
	/**
	 * Adds the fraction represented by toAdd to this fraction.
	 * The resulting fraction is always canceled down.
	 * 
	 * @param toAdd the fraction to add
	 */
	public void add(Fraction toAdd)
	{
		add(toAdd.nominator,toAdd.denominator);
	}

	/**
	 * Subtracts the fraction represented by toAddNominator and
	 * toAddDenominatior to this fraction. The resulting 
	 * fraction is always canceled down.
	 * 
	 * @param toSubNominator
	 * @param toSubDenominatior
	 */
	public void sub(int toSubNominator, int toSubDenominatior)
	{
		add(-toSubNominator,toSubDenominatior);
	}
	

	/**
	 * Subtracts the fraction represented by toSub from this fraction.
	 * The resulting fraction is always canceled down.
	 * 
	 * @param toSub the fraction to subtract
	 */
	public void sub(Fraction toSub)
	{
		sub(toSub.nominator,toSub.denominator);
	}
	
	/**
	 * Converts the fraction to a string.
	 */
	public String toString()
	{
		return nominator + "/" + denominator;
	}
	
	/**
	 * Compares the given fraction tocmp with this fraction.
	 * 
	 * @param tocmp the fraction to compare with
	 * @return <ul>
	 * 			<li>lower than 0 if tocmp is lower</li>
	 * 			<li>greater than 0 if tocmp is greater</li>
	 * 			<li>0 if tocmp equals this fractin</li>
	 * 			</ul>
	 */
	public int compare(Fraction tocmp)
	{
		return nominator * tocmp.denominator - tocmp.nominator * denominator;
	}
}
