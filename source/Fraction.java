/**
 * @author Standard
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

public class Fraction
{
	public int nominator;
	public int denominatior;

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
	
	private void kuerzen()
	{
		if (nominator == 0)
		{
			denominatior = 1;
			return;
		}
		int ggt = ggT(Math.abs(nominator),denominatior);
		nominator /= ggt;
		denominatior /= ggt;
	}

	public Fraction(int nominator, int denominatior)
	{
		this.nominator = nominator;
		this.denominatior = denominatior;
		
		kuerzen();
	}

	public void add(int toAddNominator, int toAddDenominatior)
	{
		if (toAddDenominatior == denominatior)
		{
			nominator += toAddNominator;
		} else
		{
			nominator = nominator * toAddDenominatior + toAddNominator * denominatior;
			denominatior *= toAddDenominatior;
			kuerzen();			
		}
	}
	
	public void add(Fraction toAdd)
	{
		add(toAdd.nominator,toAdd.denominatior);
	}

	public void sub(int toSubNominator, int toSubDenominatior)
	{
		add(-toSubNominator,toSubDenominatior);
	}
	
	public void sub(Fraction toSub)
	{
		sub(toSub.nominator,toSub.denominatior);
	}
	
	public String toString()
	{
		return nominator + "/" + denominatior;
	}
	
	public int compare(Fraction tocmp)
	{
		return nominator * tocmp.denominatior - tocmp.nominator * denominatior;
	}
}
