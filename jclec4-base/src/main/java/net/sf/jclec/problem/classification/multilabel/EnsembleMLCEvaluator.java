package net.sf.jclec.problem.classification.multilabel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import mulan.data.MultiLabelInstances;
import mulan.classifier.MultiLabelLearner;
import mulan.classifier.transformation.LabelPowerset;
import mulan.evaluation.Evaluation;
import mulan.evaluation.measure.HammingLoss;
import mulan.evaluation.measure.Measure;
import weka.classifiers.trees.J48;
import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.base.AbstractParallelEvaluator;
import net.sf.jclec.binarray.BinArrayIndividual;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.fitness.ValueFitnessComparator;
import net.sf.jclec.util.random.IRandGenFactory;


public class EnsembleMLCEvaluator extends AbstractParallelEvaluator
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////

	protected static final long serialVersionUID = -2635335580011827514L;
	
	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////
	
	/* Dataset to build the ensemble */
	protected MultiLabelInstances datasetTrain;
	
	/* Dataset to evaluate the individuals */
	protected MultiLabelInstances datasetValidation;
	
	/* Number of active labels in each base classifier */
	protected int numberLabelsClassifier;
	
	/* Number of base classifiers of the ensemble */
	protected int numberClassifiers;
	
	/* Threshold for voting process prediction*/
	protected double predictionThreshold;
	
	/* Indicates if the number of active labels is variable for each base classifier */
	protected boolean variable;
	
	/* Indicates if the fitness is a value to maximize */
	protected boolean maximize = true;
	
	/* Base learner for the classifiers of the ensemble */
	public MultiLabelLearner baseLearner;
	
	/* Fitness values comparator */
	protected Comparator<IFitness> COMPARATOR = new ValueFitnessComparator(!maximize);
	
	/* Table that stores all base classifiers built */
	public Hashtable<String, MultiLabelLearner> tableClassifiers;
	
	/* Table that stores the fitness of all evaluated individuals */
	public Hashtable<String, Double> tableFitness;
	
	/* Matrix with phi correlations between labels */
	double [][] phiMatrix;
	
	/* Indicates if the individual diversity is contemplated in fitness */
	private boolean fitnessWithIndividualDiversity = false;
	
	/* Random numbers generator */
	protected IRandGenFactory randGenFactory;
	
	/* Indicates if the individual fitness contemplates the phi correlation between labels */
	private boolean phiInFitness;
	
	/* Indicates if the entropy is used in fitness */
	private boolean useEntropy;
	
	/* Indicates if the measure of difficulty is used in fitness */
	private boolean useMeasureOfDifficulty;
	
	/* Indicates if the coverage is used in fitness */
	private boolean useCoverage;
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////
	
	/**
	* Empty constructor.
	*/
	public EnsembleMLCEvaluator()
	{
		super();
	}
	
	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////
	
	public int getNumberClassifiers()
	{
		return numberClassifiers;
	}
	
	public MultiLabelInstances getDatasetTrain()
	{
		return datasetTrain;
	}
	
	public MultiLabelInstances getDatasetValidation()
	{
		return datasetValidation;
	}
	
	public void setDatasetTrain(MultiLabelInstances datasetTrain) {
		this.datasetTrain = datasetTrain;
	}
	
	public void setDatasetValidation(MultiLabelInstances datasetValidation) {
		this.datasetValidation = datasetValidation;
	}

	public void setNumberClassifiers(int numberClassifiers) {
		this.numberClassifiers = numberClassifiers;
	}

	public void setNumberLabelsClassifier(int numberLabelsClassifier) {
		this.numberLabelsClassifier = numberLabelsClassifier;
	}
	
	public void setPredictionThreshold(double predictionThreshold) {
		this.predictionThreshold = predictionThreshold;
	}
	
	public void setVariable(boolean variable) {
		this.variable = variable;
	}
	
	public void setBaseLearner(MultiLabelLearner baseLearner)
	{
		this.baseLearner = baseLearner;
	}
	
	public Comparator<IFitness> getComparator() {
		return COMPARATOR;
	}
	
	public boolean getFitnessWithIndividualDiversity()
	{
		return fitnessWithIndividualDiversity;
	}
	
	public void setFitnessWithIndividualDiversity(boolean b)
	{
		fitnessWithIndividualDiversity = b;
	}
	
	public void setTable(Hashtable<String, MultiLabelLearner> tableClassifiers) {
		this.tableClassifiers = tableClassifiers;
	}
	
	public void setTableMeasures(Hashtable<String, Double> tableFitness) {
		this.tableFitness = tableFitness;
	}
	
	public void setPhiMatrix(double [][] matrix)
	{
		phiMatrix = matrix;
	}
	
	 public void setRandGenFactory(IRandGenFactory randGenFactory)
	 {
		 this.randGenFactory = randGenFactory;
	 }
	 
	 public void setPhiInFitness(boolean phiInFitness)
	 {
		 this.phiInFitness = phiInFitness;
	 }
	 
	 public void setUseEntropy(boolean useEntropy)
	 {
		 this.useEntropy = useEntropy;
	 }
	 
	 public void setUseMeasureOfDifficulty(boolean useMeasureOfDifficulty) {
		this.useMeasureOfDifficulty = useMeasureOfDifficulty;
	}
	 
	 public void setUseCoverage(boolean useCoverage) {
		this.useCoverage = useCoverage;
	}
	
	/////////////////////////////////////////////////////////////////
	// ------------------------ Overwriting AbstractEvaluator methods
	/////////////////////////////////////////////////////////////////
	
	@Override
	protected void evaluate(IIndividual ind) 
	{
		// Individual genotype
		byte[] genotype = ((BinArrayIndividual) ind).getGenotype();
		
		// Create classifier
		EnsembleClassifier classifier = new EnsembleClassifier(numberLabelsClassifier, numberClassifiers, predictionThreshold, variable, new LabelPowerset(new J48()), genotype, tableClassifiers, randGenFactory.createRandGen());
		
//		EntropyEvaluator eval = new EntropyEvaluator();          
		MeasureOfDifficultyEvaluator eval = new MeasureOfDifficultyEvaluator();

        try {
        	    // Build classifier using train data
        	    classifier.build(datasetTrain);
        	    
        	    List<Measure> measures = new ArrayList<Measure>();  
        	    //Add only the measure to use
  	       	  	measures.add(new HammingLoss());
  	       	  	Evaluation results;
  	       	  	
  	       	  	// Obtain ensembleMatrix
  	       	  	byte [][] ensembleMatrix = classifier.getEnsembleMatrix();
  	       	  	String s = classifier.getOrderedStringFromEnsembleMatrix();
  	       	  	
  	       	  	double fitness = -1;
  	       	  	//Try to get the individual fitness from the table
  	       	  	if(tableFitness.containsKey(s))
  	       	  	{
  	       	  		fitness = tableFitness.get(s).doubleValue();
  	       	  	}
  	       	  	else
  	       	  	{
  	       	  		//Calculate base fitness (1-HLoss) with validation set
  	       	  		results = eval.evaluate(classifier, datasetValidation, measures);
  	       	  		fitness = 1 - results.getMeasures().get(0).getValue();
  	       	  		
     	  			if(phiInFitness)
     	  			{
     	  				/*
	       	  			 * Introduces Phi correlation in fitness
	       	  			 * 	Maximize [(1-HLoss) + PhiSum]
	       	  			 */
	       	  			   	  	       	  			
	       	  			double phiTotal = 0;
	       	  			
	       	  			//Calculate sumPhi for all base classifiers
	       	  			for(int c=0; c<getNumberClassifiers(); c++)
	       	  			{
	       	  				double sumPhi = 0;
	       	  				//calculate sum of phi label correlations for a base classifier
	       	  				for(int i=0; i<getDatasetTrain().getNumLabels()-1; i++)
	       	  				{
	       	  					for(int j=i+1; j<getDatasetTrain().getNumLabels(); j++)
	       	  					{
	       	  						if((ensembleMatrix[c][i] == 1) && (ensembleMatrix[c][j] == 1))
	       	  							sumPhi += Math.abs(phiMatrix[i][j]);
	       	  					}
	       	  				}
	       	  				
	       	  				phiTotal += sumPhi/numberLabelsClassifier;
	       	  			}
	       	  		
	       	  			phiTotal = phiTotal/getNumberClassifiers();
	       	  			//System.out.println("phiTotal: " + phiTotal);
	       	  			//Maximize [(1-HLoss) + PhiSum]
	       	  			fitness = fitness + phiTotal;
     	  			}
     	  			
     	  			if(useEntropy)
     	  			{
//     	  				System.out.println("classifier entropy: " + classifier.getEntropy());
     	  				fitness = fitness + classifier.getEntropy();
     	  			}
     	  			
     	  			if(useMeasureOfDifficulty)
     	  			{
//     	  				System.out.println("classifier measure of difficulty: " + classifier.getMeasureOfDifficulty());
     	  				fitness = fitness - classifier.getMeasureOfDifficulty();
     	  			}
     	  			
     	  			if(useCoverage)
     	  			{
     	  				int [] v = classifier.getVotesPerLabel();
     	  				int expectedVotes = (numberClassifiers*numberLabelsClassifier)/classifier.getNumLabels();
     	  				//System.out.println("expectedVotes: " + expectedVotes);
     	  				double distance = 0;
     					for(int i=0; i<getDatasetTrain().getNumLabels(); i++)
     					{
     						distance += (double)Math.pow(expectedVotes - v[i], 2);
     					}
     					
     					distance = Math.sqrt(distance) / datasetTrain.getNumLabels();
     					fitness = fitness - distance;
     					//System.out.println("distance: " + distance + " -> fitness: " + fitness);
     	  			}
//  	       	  		
  	       	  		if(fitnessWithIndividualDiversity)
  	       	  		{
  	       	  			int max = 0;
	  				
  	       	  			/*
  	       	  			 * Calculate the individual diversity:
  	       	  			 * 		- Calculate the maximum number of repetitions of a label
  	       	  			 * 		- Divide max by possible number of appearances
  	       	  			 * 		- The diversity is 1 -  (try to have a balanced appearance of labels)
  	       	  			 */
		  				for(int i=0; i<getDatasetTrain().getNumLabels(); i++)
		  				{
		  					int sum = 0;
		  					
		  					for(int j=0; j<getNumberClassifiers(); j++)
		  					{
		  						sum = sum + genotype[i+j*getDatasetTrain().getNumLabels()];
		  					}
		  					
		  					if (sum > max)
		  						max = sum;
		  				}		  				
		  				//The diversity is the opposite of the max number of label repeat
		  				double div = 1 - (double)max/getNumberClassifiers();
		  				
		  				if(maximize)
		  				{
		  					fitness = fitness*0.6 + div*0.4;
		  				}
		  				else
		  				{
		  					//The diversity is to maximize, so if fitness is to minimize, we have to minimize (1-diversity)
		  					fitness = fitness*0.6 + (1-div)*0.4;
		  				}
  	       	  		}
	  				
  	       	  		tableFitness.put(s, fitness);
  	       	  	}
  	       	  	
//  	       	  	System.out.println("Fitness: " + fitness);
  	       	  	ind.setFitness(new SimpleValueFitness(fitness));

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}	
	}
	
}