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
import java.util.Vector;
import elvira.database.*;
import elvira.learning.preprocessing.ProjectDBC;

/**
 * Implements a predictor (classificator with continuous class variable) where
 * all the variables, including the class, are Gaussian. The model assumes that
 * the mean of the conditional distribution of each variable given the class is
 * a linear function of the class mean.
 *
 * @author Jose.Gamez@uclm.es
 * @author Antonio.Salmeron@ual.es
 * @since 5/04/2005
 */

public class GaussianPredictor { 
    
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
    
    /** 
     * The mean squared error between the predicted values and the actual ones
     */
    
    double meanSquaredError = 0.0;
    
    /**
     * Indicates the type of model to be used for prediction
     * naive --> naive structure
     * full ---> the full covariance matrix is used
     * tan  ---> a tree augmented naive bayes structure is used
     */
    
    String model = "naive"; // default value
    
    
    /**
     * main program
     */
    
    public static void main(String args[]) throws ParseException, IOException {
        
        FileInputStream fileLearn, filePredict;
        DataBaseCases dbLearn, dbPredict;
        ContinuousCaseListMem casesPredict;
        GaussianPredictor predictor;
        int classVar, i;
        Vector predictedValues;
        String model;
        
        String usage = "Wrong number of arguments. Usage GaussianPredictor\n" +
                       "\t training_file.dbc -- the input file --\n" + 
                       "\t (naive|full|tan) -- the type of model to be used\n" +
                       "\t name-of-class-variable\n" +
                       "\t cases_to_predict.dbc\n" +
                       "\t [ list-of-attributes to filter --- i.e., 1,3,5-9,11 (non-zero based)\n" +
                       "\t   inverSelection (true|fasle) -- if true the listed attributed are deleted\n" +
                       "\t ]";
        
        if((args.length != 4) && (args.length != 6)){
            System.out.println(usage);
            System.exit(0);
        }
             
        // dbc file for learning
        fileLearn = new FileInputStream(args[0]);
        dbLearn = new DataBaseCases(fileLearn);
                
        // dbc file for prediction
        filePredict = new FileInputStream(args[3]);
        dbPredict = new DataBaseCases(filePredict);
        
        // filtering attributes ??
        if (args.length == 6){
            Vector list = ProjectDBC.parseAttributes(args[4]); 
            boolean invert = Boolean.valueOf(args[5]).booleanValue();
            
            ProjectDBC projection = new ProjectDBC(dbLearn,list,invert);
            DataBaseCases output = projection.doProjection();
            dbLearn = output;
            
            projection = new ProjectDBC(dbPredict,list,invert);
            output = projection.doProjection();
            dbPredict = output;
        }
        
        // cases for predicting.       
        casesPredict = (ContinuousCaseListMem)dbPredict.getCaseListMem();
     
        // setting the class Var index
        classVar = dbLearn.getVariables().getId(args[2]);
        if (classVar == -1){
            System.out.println("** Error **, variable " + args[2] + " is not " +
                                    "included in the (projected) data base");
            System.exit(0);
        }
        
        model = args[1];   
        if (!model.equals("naive") &&
            !model.equals("full") &&
            !model.equals("tan")){
          System.out.println("*** Error ***. Model " + model + 
                                " is not allowed. Exiting .... ");
          System.exit(0);
        }
        
        predictor = new GaussianPredictor(dbLearn,classVar,model);
        
        predictedValues = predictor.predictWithMean(casesPredict);
        
        System.out.println("Correlation : "+predictor.getLinearCorrelation()+"\n");
        System.out.println("Rmse        : "+Math.sqrt(predictor.getMeanSquaredError())+"\n");
        System.out.println("Estimated values:");
        //for (i=0 ; i<predictedValues.size() ; i++) {
        //    System.out.println(((Double)predictedValues.elementAt(i)).doubleValue());
        // }
        
        
    }
    
    
    /**
     * Creates an empty instance of this class.
     */
    
    public GaussianPredictor(){
        
        variables = new NodeList();
        classVariable = -1;
        meanVector = new Vector();
        varianceVector = new Vector();
        covarianceMatrix = new double[1][1];
    }
    
    
    /**
     * Creates a Gaussian Predictor from a database.
     *
     * @param db the DataBaseCases from which the predictor will be constructed.
     * @param cv an int indicating the index (column) of the class variable in the database.
     * @param m an String indicating the type of model to learn (naive|full|tan)
     */
    
    public GaussianPredictor(DataBaseCases db, int cv, String m) {
        
        ContinuousCaseListMem cases;       
       
        variables = db.getVariables().copy();
        classVariable = cv;
        model = m;
              
        meanVector = new Vector();
        varianceVector = new Vector();
        
        int nVar = variables.size();
        covarianceMatrix = new double[nVar][nVar];
        
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        if (model.equals("full")) learnFullModel(cases);
        else if (model.equals("naive")) learnNaiveModel(cases);
        else if (model.equals("tan")) learnTanModel(cases);
    
    }
    
    
     /**
     * Creates a Gaussian Predictor from a continuouscaselistmem.
     *
     * @param cases the ContinuousCaseListMem from which the predictor will be constructed.
     * @param cv an int indicating the index (column) of the class variable in the database.
     * @param m an String indicating the type of model to learn (naive|full|tan)
     */
    
    public GaussianPredictor(ContinuousCaseListMem cases, int cv, String m) {
        variables = new NodeList(cases.getVariables());
        classVariable = cv;
        model = m;
              
        meanVector = new Vector();
        varianceVector = new Vector();
        
        int nVar = variables.size();
        covarianceMatrix = new double[nVar][nVar];
                
        if (model.equals("full")) learnFullModel(cases);
        else if (model.equals("naive")) learnNaiveModel(cases);
        else if (model.equals("tan")) learnTanModel(cases);
    
    }
    
    /** 
     * This method learns the full model, i.e., the variance and
     * mean vector, and also the full covariance matrix
     *
     * @param cases a <code>ContinuousCaseListMem</code> containing the cases
     *                  to learn from
     */
    
    public void learnFullModel(ContinuousCaseListMem cases) {
        
        int i, j, nVar;
        double v;
              
        nVar = variables.size();
       
        for (i=0 ; i<nVar ; i++) {
            v = cases.variance(i);
            varianceVector.addElement(new Double(v));
            meanVector.addElement(new Double(cases.mean(i)));
            
            covarianceMatrix[i][i] = v;
            for(j=0 ; j<i ; j++){
              covarianceMatrix[i][j] = cases.covariance(i,j);
              covarianceMatrix[j][i] = covarianceMatrix[i][j];
            }
        }
        
    }
    
    
    /** 
     * This method learns a naive structure model, i.e., the variance and
     * mean vector, but all the positions in the covariance matrix are 0.0
     * except the diagonal and those pairs containing the class variable 
     *
     * @param cases a <code>ContinuousCaseListMem</code> containing the cases
     *                  to learn from
     */
    
    public void learnNaiveModel(ContinuousCaseListMem cases){
           
        int i, j, nVar;
        double v;
              
        nVar = variables.size();
       
        for (i=0 ; i<nVar ; i++)
            for(j=0 ; j<nVar ; j++)
                covarianceMatrix[i][j] = 0.0;
        
        for (i=0 ; i<nVar ; i++) {
            v = cases.variance(i); 
            varianceVector.addElement(new Double(v));
            meanVector.addElement(new Double(cases.mean(i)));
            
            covarianceMatrix[i][i] = v;
            
            if (i != classVariable){
                covarianceMatrix[i][classVariable] = 
                            cases.covariance(i,classVariable);
                covarianceMatrix[classVariable][i] = 
                        covarianceMatrix[i][classVariable];
            }
        } 
     
    }
    
    
     /** 
     * This method learns a TAN (Tree Augmented Naive structure model, i.e., 
     * a tree is learnt for the predictive attributes by using Chow-Liu 
     * algorithm but using conditional mutual information given the class.
     * Then, the tree is extended by using hte Naive edges. Only, the edges
     * included in the TAN structure will be updated in the covariance matrix.
     *
     * @param cases a <code>ContinuousCaseListMem</code> containing the cases
     *                  to learn from
     */
    
    public void learnTanModel(ContinuousCaseListMem cases){
        int nVar = variables.size();
        ArrayList cgmi;
        int i,j;
        Tuple tuple;
        double v;
        
        // computing conditional gaussian information
        cgmi = getConditionalGMI(cases);
        
        // initialising the graph structure
        int[][] tree = new int[nVar][nVar];
        for(i=0; i<nVar; i++)
          for(j=0; j<nVar; j++) 
            tree[i][j] = 0;
        
        // running chow-liu algorithm
        
        for(i=0; i<nVar-2; ){
          tuple = (Tuple) cgmi.remove(0);
          if (!provokesCycle(tuple.p1,tuple.p2,tree)){
            tree[tuple.p1][tuple.p2] = tree[tuple.p2][tuple.p1] = 1;
            i++;
          }
        }
        
        // adding like-naive edges
        for(i=0; i<nVar; i++)
          tree[i][classVariable]=tree[classVariable][i]=1;
        
        // learning model
        for (i=0 ; i<nVar ; i++) {
            v = cases.variance(i);
            varianceVector.addElement(new Double(v));
            //meanVector.addElement(new Double(cases.mean(i)));
            
            covarianceMatrix[i][i] = v;
            for(j=0 ; j<i ; j++)
              if (tree[i][j]==1){
                covarianceMatrix[i][j] = cases.covariance(i,j);
                covarianceMatrix[j][i] = covarianceMatrix[i][j];
              }
        }
        
    }
    
    /**
     * tests whether the addition of a link between x and y provokes
     * a cycle in the graph passed as parameter (adjacency matrix representation)
     *
     * @param x the position of the first node
     * @param y the position of the second node
     * @param graph an adjacency matrix to test if (x,y) provokes a cycle
     *
     * @returns true if a cycle is introduced by (x,y) 
     */
    
    private boolean provokesCycle(int x, int y, int[][] g){
      int i;
      int nVar = variables.size();
      boolean b;
      
      for(i=0;i<nVar;i++)
        if (g[x][i] == 1){
          b = isAccessible(i,y,g,x);
          if (b) return true;
        }
          
      return false;
    }
    
    /**
     * returns true if y is accesible from x in g without using p
     */
    
    public boolean isAccessible(int x, int y, int[][] g, int p) {
      int nVar = variables.size();
      int i;
      boolean b;
      
      for(i=0; i<nVar; i++){
        if (g[x][i] == 1){
          if (i==p) continue;
          else if (i==y) return true;
          else return isAccessible(i,y,g,x);
        }
      }
      
      return false;
    }
    
    /**
     * this method computes conditional gaussian mutual information
     * for each pairs of variables given the class, and returns the 
     * values in a sorted arraylist
     *
     * @param cases the data from which the mutual information is computed
     * @returns the sorted ArrayList
     */
    
    public ArrayList getConditionalGMI(ContinuousCaseListMem cases){
        int c = classVariable;
        int nVar = variables.size();
        double[][] corr = new double[nVar][nVar];
        ArrayList list = new ArrayList(nVar*(nVar-2)); // an estimation
        Tuple tuple;
        double v;
        double[] mean = new double[nVar];
        double partialCorr;
        int i,j;
        
        
        // computing means
        for(i=0; i<nVar; i++){
          mean[i] = cases.mean(i);
          meanVector.addElement(new Double(mean[i]));
        }
         
        // computing correlations
        
        for(i=0; i<nVar; i++){
          corr[i][i] = 1.0;
          for(j=0; j<i; j++)
            corr[i][j] = corr[j][i] = cases.correlation(i,j,mean[i],mean[j]);
        }
          
        // computing conditional gaussian information of (i,j) given the class
        
        for(i=0; i<nVar; i++)
          for(j=0; j<i; j++)
            if ((i != c) && (j != c)){
              partialCorr = (corr[i][j] - (corr[i][c] * corr[j][c])) /
                    ( Math.sqrt((1-Math.pow(corr[i][c],2))*
                                        (1-Math.pow(corr[j][c],2)) ) );
              v = -0.5 * Math.log( 1 - Math.pow(partialCorr,2) );
              
              tuple = new Tuple(i,j,v);
              list.add(tuple);
            }
       
       // ordering the list according to value (from high to low values) 
       Comparator compare = new TupleComparator();
       Collections.sort(list, compare);  
     
        
       return list;
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
//        System.out.println("Var : "+s+" Mean : "+m);
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
//            System.out.println("Valor estimado: "+predictedValue+" real: "+currentCase.getContinuousValue(this.classVariable));
            meanSquaredError += Math.pow(predictedValue-currentCase.getContinuousValue(this.classVariable),2);
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
        meanSquaredError = meanSquaredError / (double)cases.getNumberOfCases();
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
    
    /**
     * Access method to the meanSquaredError.
     *
     * @return the value of <code>meanSquaredError</code> instance variable.
     */
    
    public double getMeanSquaredError() {
        
        return this.meanSquaredError;
    }
    
    
    
    
/**
 * private class triplet. Stores a pair of int (position for variables)
 * and a double (i.e. correlation, mutual information, ...
 */
    
    private class Tuple {
        public int p1;
        public int p2;
        public double value;
        
        public Tuple(int x, int y, double d){
            p1=x;
            p2=y;
            value=d;
        }
    } // end of private class
    
  /**
     * TupleComparator
     * 
     * Private class to order an array list of tuples according to value
     * It implements the interfaz Comparator
     */

private class TupleComparator implements Comparator {
  /**
   * The implementation of the method compare of the interfaz Comparator
   */

  public int compare(Object o1, Object o2){
    Tuple t1 = (Tuple)o1;
    Tuple t2 = (Tuple)o2;

    if (t1.value < t2.value) return (1);
    if (t1.value > t2.value) return (-1);
    return (0);
  }
  
} // End private class TupleComparator  
    
    
} // End of class
