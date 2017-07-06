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
 * all the variables, including the class, are considered Gaussian. 
 * The model assumes that
 * the mean of the conditional distribution of each variable given the class is
 * a linear function of the class mean.
 *
 * Feature subset selection is carried out based in a combination of mutual 
 * information ranking followed by wrapper evaluation.
 *
 *
 * @author Jose.Gamez@uclm.es
 * @author Antonio.Salmeron@ual.es
 * @since 5/04/2005
 */

public class SelectiveGaussianPredictor { 
    
    /** a nodelist with the variables included in the training set */
    NodeList m_variables = null;
    
    /** the index of the class variable */
    int m_classVariable = -1;
    
    /** the name of the predictor to be learnt */
    String m_model = null;
    
    /** the training data set */
    ContinuousCaseListMem m_cases;
    
    /** the lookahead value */
    int m_lookahead = 0; 
    
    
    /**
     * Epsilon: the threshold used to include or not variables in the
     * selected subset. Default is 1
     */

    double m_epsilon = 1.0;

    /** selected attributes -- without the class*/
    NodeList m_selected = new NodeList();
    
    /** indexes of the selected attributes -- without the class */
    Vector m_selectedIndexes = new Vector();
    
    /** the predictor learnt for the selected variables */
    GaussianPredictor selectivePredictor = null;
    
    /**
     * main program
     */
    
    public static void main(String args[]) throws ParseException, IOException {
        
        FileInputStream fileLearn, filePredict;
        DataBaseCases dbLearn, dbPredict;
        ContinuousCaseListMem casesPredict;
        SelectiveGaussianPredictor predictor;
        int classVar, i;
        Vector predictedValues;
        String model;
        double threshold;
        int lookahead;
        
        String usage = "Wrong number of arguments. Usage GaussianPredictor\n" +
                       "\t training_file.dbc -- the input file --\n" + 
                       "\t (naive|full|tan) -- the type of model to be used\n" +
                       "\t name-of-class-variable\n" +
                       "\t cases_to_predict.dbc\n" +
                       "\t threshod for selection (1.0, 1.01, 1.001, ...)\n" +
                       "\t lookahead value (0, 1, 2, ...)\n" +
                       "\t [ list-of-attributes to filter --- i.e., 1,3,5-9,11 (non-zero based)\n" +
                       "\t   inverSelection (true|fasle) -- if true the listed attributed are deleted\n" +
                       "\t ]";
        
        if((args.length != 6) && (args.length != 8)){
            System.out.println(usage);
            System.exit(0);
        }
             
        // dbc file for learning
        fileLearn = new FileInputStream(args[0]);
        dbLearn = new DataBaseCases(fileLearn);
                
        // dbc file for prediction
        filePredict = new FileInputStream(args[3]);
        dbPredict = new DataBaseCases(filePredict);
        
        // threshold and lookahead
        threshold = Double.parseDouble(args[4]);
        lookahead = Integer.parseInt(args[5]);
        
        // filtering attributes ??
        if (args.length == 8){
            Vector list = ProjectDBC.parseAttributes(args[6]); 
            boolean invert = Boolean.valueOf(args[7]).booleanValue();
            
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
        
        predictor = new SelectiveGaussianPredictor(dbLearn,classVar,model,
                                            threshold, lookahead);
        predictor.doSelection();
        predictor.learnSelectiveModel();
        predictor.predictWithMean(casesPredict);
        

        System.out.println("\nCorrelation : "+predictor.getLinearCorrelation());
        System.out.println("Rmse        : "+Math.sqrt(predictor.getMeanSquaredError())+"\n");
//        System.out.println("Estimated values:");
        //for (i=0 ; i<predictedValues.size() ; i++) {
        //    System.out.println(((Double)predictedValues.elementAt(i)).doubleValue());
        // }
        
        
    }
    
    
 /** Constructor */
    
    /**
     * Creates a Gaussian Predictor from a database.
     *
     * @param db the DataBaseCases from which the predictor will be constructed.
     * @param cv an int indicating the index (column) of the class variable in the database.
     * @param m an String indicating the type of model to learn (naive|full|tan)
     * @param eps a double value indicating the threshold for selection (1.0, 1.01, ...)
     * @param lh an int value indicating the lookahead value for the fiter-wrapper selection
     */
    
    public SelectiveGaussianPredictor(DataBaseCases db, int cv, String m, 
                                    double eps, int lh) {
        
       m_epsilon = eps;
       m_lookahead = lh;
       m_variables = db.getVariables().copy();
       m_classVariable = cv;
       m_model = m;
                    
       m_cases = (ContinuousCaseListMem)db.getCaseListMem();
        
       // randomizing the data-set, because cross validation will be used
       m_cases.randomize(new Random());
       
    }
    
    
    /**
     * This method filter the data set according to the attributes selected
     * and learn a predictor 
     */
    
    public void learnSelectiveModel(){
        // creating a nodelist with the attributes selected plus the class
        NodeList vars = new NodeList();
        vars.join(m_selected);
        vars.insertNode(m_variables.elementAt(m_classVariable));
        
        // projecting the data set
        ContinuousCaseListMem projection = m_cases.projection(vars);
        selectivePredictor = new GaussianPredictor(projection,vars.size()-1,m_model);
    }
    
    /**
     * This method first filters the given data set and them predict its output
     * with the previously stored selectivepredictor
     */ 
    
    public void predictWithMean(ContinuousCaseListMem test){
        // creating a nodelist with the attributes selected plus the class
        NodeList vars = new NodeList();
        vars.join(m_selected);
        vars.insertNode(m_variables.elementAt(m_classVariable));
        
        // projecting the data set
        ContinuousCaseListMem projection = test.projection(vars);
        selectivePredictor.predictWithMean(projection);
    }
    
    public double getLinearCorrelation(){
        return selectivePredictor.getLinearCorrelation();
    }
    
    public double getMeanSquaredError(){
        return selectivePredictor.getMeanSquaredError();
    }
    
    /**
     * This method carries out a feature subset selection based on creating
     * a mutual information-based ranking and running over the ranking in order
     * to include only those attributes which improves the prediction 
     */
    
    public void doSelection(){
        int nFolds = 5; // we used 5-cv during selection
        ContinuousCaseListMem projection;
        int i,fails,nEvals;
        double corr,best = -1.0;
        
        
        // first we create a mutual information based ranking
        int[] ranking = getMIRanking();
        
        // forward phase
        int n_att = m_variables.size()-1;
        
        for(i=0,fails=0,nEvals=0;i<n_att;i++){
          if (i==m_classVariable) continue;
          m_selected.insertNode(m_variables.elementAt(ranking[i])); //adding current attribute
          m_selected.insertNode(m_variables.elementAt(m_classVariable)); //adding class
          // doing projection
          projection = m_cases.projection( m_selected );
          // evaluating the projection with the specified predictor
          corr = doCVPrediction(projection,nFolds); 
          
          nEvals++;
          // removing class
          m_selected.removeNode(m_selected.size()-1);
          
          if (corr > (m_epsilon * best)){
              best = corr;
              m_selectedIndexes.add( new Integer(ranking[i]) );
              fails = 0;
              
          }else{ //  removing the tested attribute
              m_selected.removeNode(m_selected.size()-1);
              if (fails < m_lookahead) fails++;
              else break;
          }
          
        } // end of forward phase
        
        System.out.println("\n\nNum. of selected variables: " + m_selected.size());
        System.out.print("Indexes of the selected attributes:  ");
        for(i=0;i<m_selected.size();i++) 
          System.out.print( ((Integer)m_selectedIndexes.elementAt(i)).intValue()+ ",");
        System.out.println("\nNum. of evaluated subsets: " + nEvals);
        System.out.println("Correlation: " + best + "\n");
       
       
        
        // backward phase 
        
          // first we add the class variable at the end of the list
        m_selected.insertNode(m_variables.elementAt(m_classVariable)); //adding class
                  
        for(i=m_selected.size()-3;i>=0;i--){ // notice that the last hasn't to be studied
           // removing node in position i
           Node node = m_selected.elementAt(i); 
           m_selected.removeNode(i);
           // projecting the database
           projection = m_cases.projection( m_selected );
           // evaluating the projection with the specified predictor
           corr = doCVPrediction(projection,nFolds); 
          
           nEvals++;
           if (corr >= best){
               best = corr;
               m_selectedIndexes.remove(i);
           }else{
               Vector v = m_selected.toVector();
               v.insertElementAt(node,i);
               m_selected = new NodeList(v);
           }
        }

          // removing the class variable
        m_selected.removeNode(m_selected.size()-1);
        
        
        // reporting info
        
        System.out.println("\n\nNum. of selected variables: " + m_selected.size());
        System.out.print("Indexes of the selected attributes:  ");
        for(i=0;i<m_selected.size();i++) 
          System.out.print( ((Integer)m_selectedIndexes.elementAt(i)).intValue()+ ",");
        System.out.println("\nNum. of evaluated subsets: " + nEvals);
        System.out.println("Correlation: " + best);
       
        
    }
    
    /**
     * Gets the correlation of the predictor by using k-CV 
     */
    
    private double doCVPrediction(ContinuousCaseListMem data, int nFolds){
        ContinuousCaseListMem train,test;
        GaussianPredictor predictor;
        int i;
        double corr = 0.0;
        int cIndex = data.getVariables().size()-1;
        
        for(i=0;i<nFolds;i++){
          // training the classifier
          train = data.trainCV(i,nFolds);
          predictor = new GaussianPredictor(train,cIndex,m_model);
          // predicting with the classifier
          test =  data.testCV(i,nFolds);
          predictor.predictWithMean(test);
          // updating correlation
          corr += predictor.getLinearCorrelation() / ((double)nFolds);
        }
          
        return corr;
    }
    
    /** 
     * This method compute a mutual information based ranking.
     */
    
    public int[] getMIRanking(){
        int nVar = m_variables.size();
        double[] mean = new double[nVar];
        double[] corr = new double[nVar];
        int i,j,k;
        double[] gmi = new double[nVar];
        int[] rank = new int[nVar-1];
        
        // computing means
        for(i=0; i<nVar; i++)
          mean[i] = m_cases.mean(i);
        
        // computing correlation and gaussian mutual information
        for(i=0; i<nVar; i++){
          corr[i] = m_cases.correlation(i,m_classVariable,mean[i],mean[m_classVariable]);
          gmi[i] = -0.5 * Math.log(1 - Math.pow(corr[i],2) );
//          System.out.println("var " + i + "    mi = " + gmi[i]);
        }
        
        // creating the ranking
        int first,added=0;
        
        if (m_classVariable == 0) {first = 2; rank[0] = 1;}
        else {first=1;rank[0]=0;}
        added = 1;
        
        for(i=first;i<nVar;i++)
          if (i!=m_classVariable){
              for(j=0;j<added;j++){
                  if (gmi[i]>gmi[rank[j]]){
                      for(k=added;k>j;k--) rank[k]=rank[k-1];
                      rank[j]=i;
                      break; 
                  }
              }
              if (j==added) rank[j]=i;
              added++;
          }
        
//        System.out.println("\nRanking ..\n");
//        for(i=0;i<nVar-1;i++)
//            System.out.println(rank[i] + "   mi = " + gmi[rank[i]]);
            
        
        return rank;
    }
    
    
    
    
    
    } // End of class
