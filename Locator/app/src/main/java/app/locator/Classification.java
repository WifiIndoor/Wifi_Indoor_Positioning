package app.locator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import jsat.classifiers.CategoricalResults;
import jsat.classifiers.ClassificationDataSet;
import jsat.classifiers.Classifier;
import jsat.classifiers.DataPoint;
import jsat.classifiers.boosting.Bagging;
import jsat.classifiers.trees.RandomForest;
import jsat.io.CSV;

/**
 * A simple example where we load up a data set for classification purposes.
 *
 * @author Edward Raff
 */
public class Classification
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        int test_lines_to_skip = 0;
        Set<Integer> test_categoricalFeatures = Collections.EMPTY_SET;
        //ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        //File file = new File(classloader.getResource("iris.arff").getFile());
        //DataSet dataSet = ARFFLoader.loadArffFile(file);
        File CSVFile = new File("./test.csv");
        ClassificationDataSet dataSet = null;
        try {
            dataSet = CSV.readC(0, new FileReader(CSVFile), test_lines_to_skip, test_categoricalFeatures);
        }catch (IOException e){
            System.out.println(e);
        }

        //We specify '0' as the class we would like to make the target class.
        //ClassificationDataSet cDataSet = new ClassificationDataSet(dataSet, 0);


        int lines_to_skip = 0;
        Set<Integer> categoricalFeatures = Collections.EMPTY_SET;
        File irisCSVFile = new File("./train.csv");
        //ClassificationDataSet irisData2 = CSV.readC(0, irisCSVFile.toPath(), lines_to_skip, categoricalFeatures);
        ClassificationDataSet cDataSet = null;
        try {
            cDataSet = CSV.readC(0, new FileReader(irisCSVFile), lines_to_skip, categoricalFeatures);
        }catch (IOException e){
            System.out.println(e);
        }

        //System.out.println("test");
        int errors = 0;
        Classifier baseclassifier = new RandomForest(1000);
        Classifier classifier = new Bagging(baseclassifier);
        classifier.trainC(cDataSet);

        for(int i = 0; i < dataSet.getSampleSize(); i++)
        {
            DataPoint dataPoint = dataSet.getDataPoint(i);//It is important not to mix these up, the class has been removed from data points in 'cDataSet'
            int truth = dataSet.getDataPointCategory(i);//We can grab the true category from the data set

            //Categorical Results contains the probability estimates for each possible target class value.
            //Classifiers that do not support probability estimates will mark its prediction with total confidence.
            CategoricalResults predictionResults = classifier.classify(dataPoint);
            int predicted = predictionResults.mostLikely();
            if(predicted != truth)
                errors++;
            System.out.println( i + "| True Class: " + truth + ", Predicted: " + predicted + ", Confidence: " + predictionResults.getProb(predicted) );
        }

        System.out.println(errors + " errors were made, " + 100.0*errors/dataSet.getSampleSize() + "% error rate" );
    }
}