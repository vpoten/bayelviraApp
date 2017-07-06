package elvira.learning.classification.supervised.continuous;

import elvira.Elvira;
import java.util.*;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.potential.PotentialTable;
import elvira.potential.*;

import elvira.*;
import java.util.Enumeration;
import elvira.database.*;

/**
 * Implements a predictor (classificator with continuous class variable) where
 * all the variables, including the class, are Gaussian. The model assumes that
 * the mean of the conditional distribution of each variable given the class is
 * a lenear function of the class mean.
 *
 * @author Jose.Gamez@uclm.es
 * @author Antonio.Salmeron@ual.es
 * @since 5/04/2005
 */

public class NaiveGaussianPredictor {
    
    /**
     * Variables for which the predictor is defined.
     */
    NodeList variables;
    
    /**
     * The index of the class variable in list <code>variables</code>.
     */
    int classVariable;
    
    /**
     * The vector of means of the variables in the database.
     */
    Vector meanVector;
    
    /**
     * The vector of variances of the variables in the database.
     */
    Vector varianceVector;
    
    
    /**
     * The matrix of covariances of the variables in the model.
     */
    
    double[][] covarianceMatrix;
    
    
    /**
     * The accuracy of the classifier, measured as the linear correlation
     * between the predicted values and the actual ones.
     */
    
    double linearCorrelation;
    
    
    public static void main(String args[]) throws ParseException, IOException {
        
        FileInputStream fileLearn, filePredict;
        DataBaseCases dbLearn, dbPredict;
        ContinuousCaseListMem casesPredict;
        NaiveGaussianPredictor predictor;
        int classVar, i;
        Vector predictedValues;
        
        if((args.length != 3)){
            System.out.println("wrong number of arguments: Usage: training_file.dbc index_of_class_variable cases_to_predict.dbc");
            System.exit(0);
        }
        
        
        // dbc file for learning
        fileLearn = new FileInputStream(args[0]);
        dbLearn = new DataBaseCases(fileLearn);
        
        
        // dbc file for predicting
        filePredict = new FileInputStream(args[2]);
        dbPredict = new DataBaseCases(filePredict);
        
        // cases for predicting.
        
        casesPredict = (ContinuousCaseListMem)dbPredict.getCaseListMem();
        
        classVar = Integer.parseInt(args[1]);
        
        predictor = new NaiveGaussianPredictor(dbLearn,classVar);
        
        predictedValues = predictor.predictWithMean(casesPredict);
        
        System.out.println("Correlation : "+predictor.getLinearCorrelation()+"\n");
        System.out.println("Estimated values:");
        //for (i=0 ; i<predictedValues.size() ; i++) {
        //    System.out.println(((Double)predictedValues.elementAt(i)).doubleValue());
        // }
        
        
    }
    
    
    /**
     * Creates an empty instance of this class.
     */
    
    public NaiveGaussianPredictor(){
        
        variables = new NodeList();
        classVariable = -1;
        meanVector = new Vector();
        varianceVector = new Vector();
        covarianceMatrix = new double[1][1];
    }
    
    
    /**
     * Creates a Naive Gaussian Predictor from a database.
     *
     * @param db the DataBaseCases from which the predictor will be constructed.
     * @param cv an int indicating the index (column) of the class variable in the database.
     */
    
    public NaiveGaussianPredictor(DataBaseCases db, int cv) {
        
        ContinuousCaseListMem cases;
        int i, j, nVar;
        double v;
        
        variables = db.getVariables().copy();
        classVariable = cv;
        
        nVar = variables.size();
        
        meanVector = new Vector();
        varianceVector = new Vector();
        covarianceMatrix = new double[nVar][nVar];
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        for (i=0 ; i<nVar ; i++) {
            v = cases.variance(i);
            varianceVector.addElement(new Double(v));
            meanVector.addElement(new Double(cases.mean(i)));
            // This is the general normal case
            /*for (j=0 ; j<nVar ; j++) {
                if (i == j) // The diagonal contains the variances
                    covarianceMatrix[i][j] = v;
                else
                    covarianceMatrix[i][j] = cases.covariance(i,j);
            }*/
            
            // This is the Naive Gaussian case
            
            for (j=0 ; j<nVar ; j++) {
                if (i == j) // The diagonal contains the variances
                    covarianceMatrix[i][j] = v;
                else {
                    if ((i==cv) || (j==cv))
                        covarianceMatrix[i][j] = cases.covariance(i,j);
                    else
                        covarianceMatrix[i][j] = 0.0;
                }
            }
            covarianceMatrix[i][i] = v;
            covarianceMatrix[i][cv] = cases.covariance(i,cv);
            covarianceMatrix[cv][i] = covarianceMatrix[i][cv];
        }
    }
    
    
    /**
     * Predicts the value of the class variable for a given configuration.
     * The prediction is equal to the mean of the posterior Gaussian
     * distribution of the class variable.
     *
     * The class variable is assumed to be in the given cases, and in the
     * same position stored in <code>classVariable</code>.
     *
     * The propagation is  done using Castillo's method.
     *
     * @param conf the <code>ContinuousConfiguration</code> for which the
     * value of the class will be predicted.
     * @return the predicted value.
     */
    
    public double predictWithMean(ContinuousConfiguration conf) {
        
        double m = 0.0, s = 0.0, v, mi, mj, si, sj, cov, updateFactor;
        int i, j, k, index;
        double[] auxMean;
        double[][] auxVar;
        
        
        // We start with the vector of means of all the variables.
        auxMean = new double[this.meanVector.size()];
        auxVar = new double[this.meanVector.size()][this.meanVector.size()];
        
        for (i=0 ; i<meanVector.size() ; i++) {
            v = ((Double)meanVector.elementAt(i)).doubleValue();
            auxMean[i] = v;
            for (j=0 ; j<meanVector.size() ; j++) {
                auxVar[i][j] = this.covarianceMatrix[i][j];
            }
        }
        
        for (i=conf.size()-1 ; i>=0 ; i--) {
            
            if (i!=this.classVariable) {
                v = conf.getContinuousValue(i);
                mi = auxMean[i];
                si = auxVar[i][i];
                
                updateFactor = 0.0;
                for (j=0 ; j<i ; j++) {
                    cov = auxVar[j][i];
                    updateFactor += Math.pow(cov, 2);
                }
                
                
                
                // Now, take care of updating the data about the class variable
                // if the index is below it
                if (i < this.classVariable) {
                    // Update the mean
                    cov = auxVar[this.classVariable][i];
                    updateFactor += Math.pow(cov, 2);
                    updateFactor /= si;
                    
                    mj = auxMean[this.classVariable];
                    mj = mj + (cov * (v-mi) / si);
                    auxMean[this.classVariable] = mj;
                }
                else {
                    updateFactor /= si;
                }
                
                
                for (j=0 ; j<i ; j++) {
                    // Update the means
                    mj = auxMean[j];
                    cov = auxVar[j][i];
                    mj = mj + (cov * (v-mi) / si);
                    auxMean[j] = mj;
                    
                    // Update the varainces
                    
                    for (k=0 ; k<i ; k++) {
                        auxVar[j][k] = auxVar[j][k] - updateFactor;
                    }
                    if (i < this.classVariable) {
                        auxVar[j][this.classVariable] = auxVar[j][this.classVariable] - updateFactor;
                        auxVar[this.classVariable][j] = auxVar[this.classVariable][j] - updateFactor;
                    }
                }
                
                if (i < this.classVariable) {
                    auxVar[this.classVariable][this.classVariable] = auxVar[this.classVariable][this.classVariable] - updateFactor;
                }
                
            }
        }
        
        // At this point, in the first position of auxMean the mean of the class
        // variable must be stored.
        m = auxMean[this.classVariable];
        s = auxVar[this.classVariable][this.classVariable];
        System.out.println("Var : "+s+" Mean : "+m);
        return m;
    }
    
    
    /**
     * Predicts the value of the class variable for a given set of configurations.
     * The prediction is equal to the mean of the posterior Gaussian
     * distribution of the class variable.
     *
     * The class variable is assumed to be in the given cases, and in the
     * same position stored in <code>classVariable</code>.
     *
     * The propagation is  done using Castillo's method.
     *
     * @param cases the <code>ContinuousCaseListMem</code> for which configurations the
     * value of the class will be predicted.
     * @return a vector with the predicted value.
     */
    
    public Vector predictWithMean(ContinuousCaseListMem cases) {
        
        int i;
        double predictedValue, predictedMean = 0.0, predictedSquare = 0.0,
        predictedSigma, classSigma, temp = 0.0, classMean;
        Vector predicted = new Vector();
        ContinuousConfiguration currentCase;
        
        for (i=0 ; i<cases.getNumberOfCases() ; i++) {
            currentCase = (ContinuousConfiguration)cases.get(i);
            predictedValue = this.predictWithMean(currentCase);
            predicted.addElement(new Double(predictedValue));
            predictedMean += predictedValue;
            predictedSquare += (predictedValue * predictedValue);
            temp += (predictedValue * currentCase.getContinuousValue(this.classVariable));
            System.out.println("Valor estimado: "+predictedValue+" real: "+currentCase.getContinuousValue(this.classVariable));
        }
        
        // Here it is assumed that there are no missing values. Otherwise, the
        // correct n should be taken into account.
        
        predictedMean /= (double)cases.getNumberOfCases();
        predictedSigma = predictedSquare / (double)cases.getNumberOfCases() - Math.pow(predictedMean, 2);
        predictedSigma = Math.sqrt(predictedSigma);
        classSigma = ((Double)this.varianceVector.elementAt(classVariable)).doubleValue();
        classSigma = Math.sqrt(classSigma);
        classMean = ((Double)this.meanVector.elementAt(classVariable)).doubleValue();
        temp = (temp /(double)cases.getNumberOfCases()) - (predictedMean * classMean);
        
        System.out.println("Class mean "+classMean+" ; Predicted mean: "+predictedMean);
        System.out.println("Class sigma "+classSigma+" ; Predicted sigma: "+predictedSigma);
        linearCorrelation = temp / (predictedSigma * classSigma);
        return predicted;
    }
    
    
    /**
     * Access method to the linear correlation.
     *
     * @return the value of <code>linearCorrelation</code> instance variable.
     */
    
    public double getLinearCorrelation() {
        
        return this.linearCorrelation;
    }
    
} // End of class
