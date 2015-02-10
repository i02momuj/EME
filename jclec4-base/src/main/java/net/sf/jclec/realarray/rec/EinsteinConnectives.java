package net.sf.jclec.realarray.rec;


/**
 * Einstein Connectives.
 * 
 * @author Alberto Lamarca-Rosales 
 * @author Sebastian Ventura
 */

public class EinsteinConnectives extends AbstractFuzzyConnectives
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////
	
	/** Generated by eclipse */

	private static final long serialVersionUID = -2502656913454059636L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Empty constructor
	 */
	public EinsteinConnectives() 
	{
		super();
	}
	
	/////////////////////////////////////////////////////////////////
	// ---------------------------------- AbstractConnectives methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Einstein Function m
	 */
	
	public double m(double x, double y) 
	{
		if (x == 0 || y == 0 )
			return 0;
		else
			return  2/(1+(Math.pow((2-x)/x,(1-alpha))*Math.pow((2-y)/y,alpha)));
	}

	/**
	 * Einstein Function s
	 */
	public double s(double x, double y) 
	{
		return (x+y)/(1+(x*y));
	}

	/**
	 * Einstein Function f
	 */
	public double f(double x, double y) 
	{
		if (x == 1 || y == 1)
			return 0.0;
		else
			return (x*y)/(1+(1-x)*(1-y));
	}

	/**
	 * Einstein Function l
	 */
	public double l(double x, double y) 
	{
		return m(f(x,y),s(x,y));
	}
}
