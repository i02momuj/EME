package net.sf.jclec.problem.classification.multilabel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mulan.data.MultiLabelInstances;
import mulan.classifier.MultiLabelLearner;
import mulan.classifier.MultiLabelOutput;
import mulan.classifier.transformation.LabelPowerset;
import mulan.evaluation.Evaluator;
import mulan.evaluation.Evaluation;
import mulan.evaluation.measure.AveragePrecision;
import mulan.evaluation.measure.Coverage;
import mulan.evaluation.measure.ErrorSetSize;
import mulan.evaluation.measure.ExampleBasedAccuracy;
import mulan.evaluation.measure.ExampleBasedFMeasure;
import mulan.evaluation.measure.ExampleBasedPrecision;
import mulan.evaluation.measure.ExampleBasedRecall;
import mulan.evaluation.measure.ExampleBasedSpecificity;
import mulan.evaluation.measure.GeometricMeanAverageInterpolatedPrecision;
import mulan.evaluation.measure.GeometricMeanAveragePrecision;
import mulan.evaluation.measure.HammingLoss;
import mulan.evaluation.measure.HierarchicalLoss;
import mulan.evaluation.measure.IsError;
import mulan.evaluation.measure.LogLoss;
import mulan.evaluation.measure.MacroAUC;
import mulan.evaluation.measure.MacroPrecision;
import mulan.evaluation.measure.MacroRecall;
import mulan.evaluation.measure.MacroSpecificity;
import mulan.evaluation.measure.MeanAverageInterpolatedPrecision;
import mulan.evaluation.measure.MeanAveragePrecision;
import mulan.evaluation.measure.Measure;
import mulan.evaluation.measure.MacroFMeasure;
import mulan.evaluation.measure.MicroAUC;
import mulan.evaluation.measure.MicroFMeasure;
import mulan.evaluation.measure.MicroPrecision;
import mulan.evaluation.measure.MicroRecall;
import mulan.evaluation.measure.MicroSpecificity;
import mulan.evaluation.measure.OneError;
import mulan.evaluation.measure.RankingLoss;
import mulan.evaluation.measure.SubsetAccuracy;
import weka.classifiers.trees.J48;
import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.base.AbstractParallelEvaluator;
import net.sf.jclec.binarray.BinArrayIndividual;
import net.sf.jclec.fitness.SimpleValueFitness;
import net.sf.jclec.fitness.ValueFitnessComparator;


public class EnsembleMLCEvaluator extends AbstractParallelEvaluator
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////
	
	/** Generated by Eclipse */
	
	protected static final long serialVersionUID = -2635335580011827514L;
	
	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////
	
	protected MultiLabelInstances datasetTrain;
	
	protected int numberLabelsClassifier;
	
	protected int numberClassifiers;
	
	protected double predictionThreshold;
	
	protected boolean variable;
	
	protected boolean maximize = true;
	
	protected Comparator<IFitness> COMPARATOR = new ValueFitnessComparator(!maximize);
	
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
	
	public MultiLabelInstances getDataset()
	{
		return datasetTrain;
	}
	
	public void setDataset(MultiLabelInstances datasetTrain) {
		this.datasetTrain = datasetTrain;
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
	
	public Comparator<IFitness> getComparator() {
		return COMPARATOR;
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
		EnsembleClassifier classifier = new EnsembleClassifier(numberLabelsClassifier, numberClassifiers, predictionThreshold, variable, new LabelPowerset(new J48()), genotype);
		
		Evaluator eval = new Evaluator();          
        
        try {   
        	    // Build classifier using train data   		
        	    classifier.build(datasetTrain);   		
        	    
        	      List<Measure> measures = new ArrayList<Measure>();  	       
    	       	  measures = prepareMeasures(classifier, datasetTrain);
    	       	  Evaluation results = eval.evaluate(classifier, datasetTrain, measures);    
        	    
			       for (Measure m : results.getMeasures())
			       {	   
			    	   System.out.println(m.getName()+"  "+m.getClass()+"  "+m.getValue());
			    	   //The fitness is the MacroFMeasure
			    	   if (m.getClass() == HammingLoss.class)
			    	   {
					       ind.setFitness(new SimpleValueFitness(m.getValue()));
					   }
			       }   
								
				
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}	
	}
	
	protected List<Measure> prepareMeasures(MultiLabelLearner learner,
            MultiLabelInstances mlTestData) {
        List<Measure> measures = new ArrayList<Measure>();

        MultiLabelOutput prediction;
        try {
            prediction = learner.makePrediction(mlTestData.getDataSet().instance(0));
            int numOfLabels = mlTestData.getNumLabels();
            
            // add bipartition-based measures if applicable
            if (prediction.hasBipartition()) {
                // add example-based measures
                measures.add(new HammingLoss());
                measures.add(new SubsetAccuracy());
                measures.add(new ExampleBasedPrecision());
                measures.add(new ExampleBasedRecall());
                measures.add(new ExampleBasedFMeasure());
                measures.add(new ExampleBasedAccuracy());
                measures.add(new ExampleBasedSpecificity());
                // add label-based measures
                measures.add(new MicroPrecision(numOfLabels));
                measures.add(new MicroRecall(numOfLabels));
                measures.add(new MicroFMeasure(numOfLabels));
                measures.add(new MicroSpecificity(numOfLabels));
                measures.add(new MacroPrecision(numOfLabels));
                measures.add(new MacroRecall(numOfLabels));
                measures.add(new MacroFMeasure(numOfLabels));
                measures.add(new MacroSpecificity(numOfLabels));
            }
            // add ranking-based measures if applicable
            if (prediction.hasRanking()) {
                // add ranking based measures
                measures.add(new AveragePrecision());
                measures.add(new Coverage());
                measures.add(new OneError());
                measures.add(new IsError());
                measures.add(new ErrorSetSize());
                measures.add(new RankingLoss());
            }
            // add confidence measures if applicable
            if (prediction.hasConfidences()) {
                measures.add(new MeanAveragePrecision(numOfLabels));
                measures.add(new GeometricMeanAveragePrecision(numOfLabels));
                measures.add(new MeanAverageInterpolatedPrecision(numOfLabels, 10));
                measures.add(new GeometricMeanAverageInterpolatedPrecision(numOfLabels, 10));
                measures.add(new MicroAUC(numOfLabels));
                measures.add(new MacroAUC(numOfLabels));
                measures.add(new LogLoss());
            }
            // add hierarchical measures if applicable
            if (mlTestData.getLabelsMetaData().isHierarchy()) {
                measures.add(new HierarchicalLoss(mlTestData));
            }
        } catch (Exception ex) {
            Logger.getLogger(Evaluator.class.getName()).log(Level.SEVERE, null, ex);
        }

        return measures;
    }


}