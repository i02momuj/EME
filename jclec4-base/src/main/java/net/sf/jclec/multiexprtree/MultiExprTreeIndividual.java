package net.sf.jclec.multiexprtree;

import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.base.AbstractIndividual;
import net.sf.jclec.exprtree.ExprTree;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Individual that contains several Expression trees as genotype.
 * 
 * @author Sebastian Ventura
 */

@SuppressWarnings("deprecation")
public class MultiExprTreeIndividual extends AbstractIndividual<ExprTree[]> 
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////

	/** Generated by Eclipse */
	
	private static final long serialVersionUID = 5269312474198753496L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/** 
	 * Empty constructor.
	 */
	
	public MultiExprTreeIndividual() 
	{
		super();
	}

	/**
	 * Constructor that sets individual genotype.
	 * 
	 * @param genotype Individual genotype
	 */
	
	public MultiExprTreeIndividual(ExprTree[] genotype)
	{
		super(genotype);
	}

	/**
	 * Constructor that sets individual genotype and fitness.
	 * 
	 * @param genotype Individual genotype
	 * @param fitness  Individual fitness
	 */
	
	public MultiExprTreeIndividual(ExprTree[] genotype, IFitness fitness) 
	{
		super(genotype, fitness);
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	// IIndividual interface

	/**
	 * {@inheritDoc}
	 */
	
	public double distance(IIndividual other) 
	{
		// TODO Implement an expression tree distance 
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	
	public IIndividual copy() 
	{
		int gl = genotype.length;
		ExprTree [] genotypeCopy = new ExprTree[gl];
		for (int i=0; i<gl; i++) {
			genotypeCopy[i] = genotype[i].copy();
		}
		return new MultiExprTreeIndividual(genotypeCopy);
	}
	
	// java.lang.Object methods
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public boolean equals(Object other) 
	{
		if (other instanceof MultiExprTreeIndividual) {
			MultiExprTreeIndividual cother = (MultiExprTreeIndividual) other;
			EqualsBuilder eb = new EqualsBuilder();
			eb.append(genotype, cother.genotype);
			return eb.isEquals();
		}
		else {
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	@Override
	public String toString()
	{
		ToStringBuilder tsb = new ToStringBuilder(this);
		tsb.append("genotype", genotype);
		tsb.append("fitness", fitness);
		return tsb.toString();
	}
}
