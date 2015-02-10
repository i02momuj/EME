package net.sf.jclec.problem.classification.listener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import net.sf.jclec.AlgorithmEvent;
import net.sf.jclec.problem.classification.IClassifier;
import net.sf.jclec.problem.classification.base.ClassificationAlgorithm;
import net.sf.jclec.problem.classification.base.ClassificationReporter;
import net.sf.jclec.problem.classification.base.Rule;
import net.sf.jclec.problem.classification.base.RuleBase;
import net.sf.jclec.problem.util.dataset.FileDataset;
import net.sf.jclec.problem.util.dataset.attribute.CategoricalAttribute;
import net.sf.jclec.problem.util.dataset.metadata.IMetadata;

/**
 * Classification reporter for rule-based classification algorithms.<p/>
 * 
 * Extends the ClassificationReporter with the implementation of the doClassificationReport() method.
 * It defines the reports of the train/test datasets after the classifier has been learned.
 * It shows the complete rule-base as the classifier and the performance statistics of the classification of the datasets.
 * Namely, accuracy, Cohen's Kappa rate, AUC, geometric mean, number of rules, number of conditions, average number of conditions, number of evaluations, execution time.
 * 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Alberto Cano 
 * @author Juan Luis Olmo
 */

public class RuleBaseReporter extends ClassificationReporter
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////

	/** Generated by Eclipse */
	
	private static final long serialVersionUID = -8548482239030974796L;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Constructor
	 */
	
	public RuleBaseReporter() 
	{
		super();
	}

	/////////////////////////////////////////////////////////////////
	// -------------------------------------------- Protected methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Make a classifier report in train and test
	 * 
	 * @param algorithm Algorithm
	 */
    protected void doClassificationReport(ClassificationAlgorithm algorithm)
	{
		// Test report name
		String testReportFilename = "TestClassificationReport.txt";
		// Train report name
		String trainReportFilename = "TrainClassificationReport.txt";
		// Test report file
		File testReportFile = new File(reportDirectory, testReportFilename);
		// Train report file
		File trainReportFile = new File(reportDirectory, trainReportFilename);
		// Test file writer
		FileWriter testFile = null;
		// Train file writer
		FileWriter trainFile = null;
		// Number of conditions
		int conditions = 0;
		// Classifier
		IClassifier classifier = algorithm.getClassifier();
		
		int[][] confusionMatrixTrain = classifier.getConfusionMatrix(algorithm.getTrainSet());
		int[][] confusionMatrixTest = classifier.getConfusionMatrix(algorithm.getTestSet());
		
		int[] numberInstancesTrain = new int[confusionMatrixTrain.length];
		int[] numberInstancesTest = new int[confusionMatrixTest.length];
		int correctedClassifiedTrain = 0, correctedClassifiedTest = 0;
		
		for(int i = 0; i < confusionMatrixTrain.length; i++)
		{
			correctedClassifiedTrain += confusionMatrixTrain[i][i];
			correctedClassifiedTest += confusionMatrixTest[i][i];
			
			for(int j = 0; j < confusionMatrixTrain.length; j++)
			{
				numberInstancesTrain[i] += confusionMatrixTrain[i][j];
				numberInstancesTest[i] += confusionMatrixTest[i][j];
			}
		}
		
		double kappaRateTrain = Kappa(confusionMatrixTrain);
		double kappaRateTest = Kappa(confusionMatrixTest);
		
		double aucTrain = AUC(confusionMatrixTrain);
		double aucTest = AUC(confusionMatrixTest);
		
		double mediaGeoTrain = GeoMean(confusionMatrixTrain);
		double mediaGeoTest = GeoMean(confusionMatrixTest);
		
		DecimalFormat df = new DecimalFormat("0.00");
		DecimalFormat df4 = new DecimalFormat("0.0000");
		
		try {
			testReportFile.createNewFile();
			trainReportFile.createNewFile();
			testFile = new FileWriter (testReportFile);
			trainFile = new FileWriter (trainReportFile);
			
			// Dataset metadata
			IMetadata metadata = algorithm.getTrainSet().getMetadata();
			
			// Get the classifier
			List<Rule> classificationRules = ((RuleBase) classifier).getClassificationRules();
			
			// Obtain the number of conditions
			conditions = ((RuleBase) classifier).getConditions();
			
			// Obtain the number of classes
			CategoricalAttribute catAttribute = (CategoricalAttribute) metadata.getAttribute(metadata.getClassIndex()); 
			int numClasses = catAttribute.getCategories().size();
			
			// Train data
			trainFile.write("File name: " + ((FileDataset) algorithm.getTrainSet()).getFileName());
			trainFile.write(("\nRuntime (s): " + (((double)(endTime-initTime)) / 1000.0)));
			trainFile.write("\nNumber of different attributes: " + (metadata.numberOfAttributes()-1));
			trainFile.write("\nNumber of rules: " + (classificationRules.size()+1));
			trainFile.write("\nNumber of conditions: "+ conditions);
			trainFile.write("\nAverage number of conditions per rule: " + (double)conditions/((double)classificationRules.size()+1.0));
			trainFile.write("\nAccuracy: " + df4.format((correctedClassifiedTrain /  (double) algorithm.getTrainSet().getInstances().size())));
			
			// Write the geometric mean
			trainFile.write("\nGeometric mean: " + df4.format(mediaGeoTrain));
			trainFile.write("\nCohen's Kappa rate: " + df4.format(kappaRateTrain));
			trainFile.write("\nAUC: " + df4.format(aucTrain));
			
			trainFile.write("\n\n#Percentage of correct predictions per class");			
			
			// Test data
			testFile.write("File name: " + ((FileDataset) algorithm.getTestSet()).getFileName());
			testFile.write(("\nRuntime (s): " + (((double)(endTime-initTime)) / 1000.0)));
			testFile.write("\nNumber of different attributes: " + (metadata.numberOfAttributes()-1));		
			testFile.write("\nNumber of rules: " + (classificationRules.size()+1));
			testFile.write("\nNumber of conditions: "+ conditions);
			testFile.write("\nAverage number of conditions per rule: " + (double)conditions/((double)classificationRules.size()+1.0));
			testFile.write("\nAccuracy: " + df4.format((correctedClassifiedTest /  (double) algorithm.getTestSet().getInstances().size())));
			
			// Write the geometric mean
			testFile.write("\nGeometric mean: " + df4.format(mediaGeoTest));
			testFile.write("\nCohen's Kappa rate: " +  df4.format(kappaRateTest));
			testFile.write("\nAUC: " +  df4.format(aucTest));
			
			testFile.write("\n\n#Percentage of correct predictions per class");

			// Check if the report directory name is in a file
			String aux = "";
			if(getReportDirName().split("/").length>1)
				aux = getReportDirName().split("/")[0]+"/";
			else
				aux = "./";

			// Global report for train
			String nameFileTrain = aux +getGlobalReportName() + "-train.txt";
			File fileTrain = new File(nameFileTrain);
			BufferedWriter bwTrain;
			
			// Global report for test
			String nameFileTest = aux +getGlobalReportName() + "-test.txt";
			File fileTest = new File(nameFileTest);
			BufferedWriter bwTest; 
			
			// If the global report for train exist
			if(fileTrain.exists())
			{
				bwTrain = new BufferedWriter (new FileWriter(nameFileTrain,true));
				bwTrain.write(System.getProperty("line.separator"));
			}
			else
			{
				bwTrain = new BufferedWriter (new FileWriter(nameFileTrain));
				bwTrain.write("Dataset, Accuracy, Cohen's Kappa rate, AUC, geometric mean, number of rules, number of conditions, average number of conditions, number of evaluations, execution time\n");
			}
			
			// If the global report for test exist
			if(fileTest.exists())
			{
				bwTest = new BufferedWriter (new FileWriter(nameFileTest,true));
				bwTest.write(System.getProperty("line.separator"));
			}
			else
			{
				bwTest = new BufferedWriter (new FileWriter(nameFileTest));
				
				bwTest.write("Dataset, Accuracy, Cohen's Kappa rate, AUC, geometric mean, number of rules, number of conditions, average number of conditions, number of evaluations, execution time\n");
			}
			
			//Write the train dataset name
			bwTrain.write(((FileDataset) algorithm.getTrainSet()).getFileName() + ",");
			//Write the test dataset name
			bwTest.write(((FileDataset) algorithm.getTestSet()).getFileName() + ",");
			//Write the percentage of correct predictions
			bwTrain.write(((correctedClassifiedTrain /  (double) algorithm.getTrainSet().getInstances().size())) + ",");
			
			bwTrain.write(kappaRateTrain + ",");
			bwTrain.write(aucTrain + ",");
			
			for(int i=0; i<numClasses; i++)
			{
				String result = new String();
				
				result = "\n Class " + metadata.getAttribute(metadata.getClassIndex()).show(i) + ":";
				if(numberInstancesTrain[i] == 0)
				{
					result += " 100.00";
				}
				else
				{
					result += " " + df.format((confusionMatrixTrain[i][i] / (double) numberInstancesTrain[i]) * 100) + "%";
				}
		
				trainFile.write(result);
			}
			
			trainFile.write("\n#End percentage of correct predictions per class");

			bwTrain.write(mediaGeoTrain + ",");
			bwTrain.write((classificationRules.size()+1) + ",");
			bwTrain.write(conditions + ",");
			bwTrain.write((double)conditions/((double)classificationRules.size()+1.0)+",");
			bwTrain.write(algorithm.getEvaluator().getNumberOfEvaluations()+",");
			bwTrain.write((((double)(endTime-initTime)) / 1000.0) + "");
		
			trainFile.write("\n\n#Classifier\n");
			
			trainFile.write(classifier.toString(metadata));
			
			// Write the Percentage of correct predictions
			bwTest.write(((correctedClassifiedTest /  (double) algorithm.getTestSet().getInstances().size())) + ",");
			bwTest.write(kappaRateTest + ",");
			bwTest.write(aucTest + ",");
					
			for(int i=0; i<numClasses; i++)
			{
				String result = new String();
				
				result = "\n Class " + metadata.getAttribute(metadata.getClassIndex()).show(i) +":";
				if(numberInstancesTest[i] == 0)
				{
					result += " 100.00";
				}
				else
				{
					result += " " + df.format((confusionMatrixTest[i][i] / (double) numberInstancesTest[i]) *100) + "%";
				}
				testFile.write(result);
			}
			
			testFile.write("\n#End percentage of correct predictions per class");

			bwTest.write(mediaGeoTest + ",");		
			bwTest.write((classificationRules.size()+1) + ",");
			bwTest.write(conditions + ",");
			bwTest.write((double)conditions/((double)classificationRules.size()+1.0) + ",");
			bwTest.write(algorithm.getEvaluator().getNumberOfEvaluations()+",");
			bwTest.write((((double)(endTime-initTime)) / 1000.0) + "");
		
			testFile.write("\n\n#Classifier\n");
			
			testFile.write(classifier.toString(metadata));
			
			testFile.write("\n#Test Classification Confusion Matrix\n");

			testFile.write("\t\t\tPredicted\n\t\t\t");
			
			for(int i = 0; i < metadata.numberOfClasses(); i++)
				testFile.write("C"+ i + "\t");
			
			testFile.write("|\nActual");
				
			for(int i = 0; i < metadata.numberOfClasses(); i++)
			{
				if(i != 0)
					testFile.write("\t");

				testFile.write("\tC" + i + "\t");
				
				for(int j = 0; j < metadata.numberOfClasses(); j++)
					testFile.write(confusionMatrixTest[i][j] + "\t");
					
				testFile.write("|\tC" + i + " = " + metadata.getAttribute(metadata.getClassIndex()).show(i) + "\n");
			}
			
			trainFile.write("\n#Train Classification Confusion Matrix\n");

			trainFile.write("\t\t\tPredicted\n\t\t\t");
			
			for(int i = 0; i < metadata.numberOfClasses(); i++)
				trainFile.write("C"+ i + "\t");
			
			trainFile.write("|\nActual");
				
			for(int i = 0; i < metadata.numberOfClasses(); i++)
			{
				if(i != 0)
					trainFile.write("\t");

				trainFile.write("\tC" + i + "\t");
				
				for(int j = 0; j < metadata.numberOfClasses(); j++)
					trainFile.write(confusionMatrixTrain[i][j] + "\t");
					
				trainFile.write("|\tC" + i + " = " + metadata.getAttribute(metadata.getClassIndex()).show(i) + "\n");
			}
			
			// Close the files
			bwTest.close();
			bwTrain.close();
			testFile.close();
			trainFile.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
    
    /////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////
    
    @Override
	public void algorithmTerminated(AlgorithmEvent event) {
	}
}