package elvira.learning.classification.supervised.continuous;

import elvira.tools.statistics.analysis.Stat;
import elvira.tools.statistics.math.Fmath;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.potential.*;
import elvira.*;
import elvira.learning.MTELearning;
import java.util.Vector;
import java.util.Random;
import elvira.inference.elimination.VariableElimination;
import elvira.inference.clustering.MTESimplePenniless;
import javax.swing.JOptionPane;

/**
 * Implements a naive Bayes predictor (classifier with 
 * continuous class variable) where all the variables, 
 * including the class, are of class MTE. The model 
 * assumes that the joint distribution is MTE. The input
 * database has missing values.
 *  
 * @author Antonio Fernandez Alvarez (afalvarez@ual.es)
 * @author Jens Dalgaard Nielsen (dalgaard@ual.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 * 
 * @since 28/03/2008
 */

public class NaiveMTEPredictorMissingData {
    
   /**
     * Variables for which the predictor is defined.
     */
    NodeList variables;
    
    /**
     * The index of the class variable in list <code>variables</code>.
     */
    int classVariable;
    
    /**
     * The network that defined the predictor.
     */
    Bnet net;
       
    /**
     * The database from which the network.
     */
    DataBaseCases dbCases;
    
    
    /**
     * Creates an empty instance of this class.
     */
      
    /**
     * Creates a NaiveMTEPredictorMissingData from a database with missing 
     * values
     *
     * @param dbfull the  full <code>DataBaseCases</code> from which the 
     * predictor will be constructed.
     * @param classIndex an int indicating the index (column) of the class 
     * variable in the database. The first column is labeled as 0.
     * @param intervals the number of intervals into which the domain of the 
     * continuous variables will be split.
     * @param percent is a double value between 0 and 1 thats represents the 
     * percent of missing values in the database
     */
    
 public NaiveMTEPredictorMissingData(DataBaseCases dbtrain,  int cv, int intervals){
        

        this.dbCases=dbtrain; 
        this.classVariable=cv;
                
        DataBaseCases dbfilled = FillValuesLearnUnivariate();
        /* try{
                   dbfilled.saveDataBase(new FileWriter("borrar1.dbc"));
                }catch(Exception e){}*/
        NaiveMTEPredictor modeltemp = new NaiveMTEPredictor(dbfilled, classVariable, intervals);            

        this.net=modeltemp.net;
            
        double rmse=1000000000.0;

        //Algoritmo iterativo testeado con la bd train
        double rmsetemp=getrmse(modeltemp,dbtrain);
         
        while (rmsetemp<rmse){        
            
                System.out.println("     Improving rmse: "+ rmse + " --> " + rmsetemp);
                rmse = rmsetemp;
                
                
                dbfilled=FillSimulatedPredictedValues(modeltemp);
              /*  try{
                   dbfilled.saveDataBase(new FileWriter("borrar2.dbc"));
                }catch(Exception e){}*/
                modeltemp = new NaiveMTEPredictor(dbfilled, classVariable, intervals);   
                
                this.net=modeltemp.net;
                
                rmsetemp=getrmse(modeltemp,dbtrain);
               
               // System.out.println("rmsetemp:"+rmsetemp);
         }     
    }
 
 /**
     * Predicts the value of the class variable for a given configuration.
     * The prediction is equal to the mean and the median of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is given as argument.
     *
     * The propagation is done using the Penniless method.
     *
     * @param conf the <code>ContinuousConfiguration</code> for which the
     * value of the class will be predicted.
     * @param classVar the class variable.
     * @return a vector with the predicted value in the first position (mean of the
     * posterior distribution), the variance in the second position, the predicted value
     * using the median of the posterior distribution in the third position and the exact value
     * for the class variable in the fourth position.
     */
   
    public Vector predictWithMeanMissingValues(ContinuousConfiguration conf, Node classVar) {
        
        double exactValue, mean, mode, median, variance;
        Evidence evidence;
        MTESimplePenniless propagation;
        Vector results, returnValues;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree tree;
        
        exactValue = conf.getValue((Continuous)classVar);
        
        // Remove the class variable from the configuration
        conf.remove(classVar);
        
        evidence =  new Evidence(conf);
        
        propagation = new MTESimplePenniless(net,evidence,0,0,0,0,0);
        
        // Since the only unobserved variable is the class, after the propagation,
        // in vector results the only potential will be the corresponding
        // to the posterior distribution of the class.
        propagation.propagate(evidence);
        
        results = propagation.getResults();
        
        pot = (PotentialContinuousPT)results.elementAt(0);
        
        //pot.print();
        
        tree = pot.getTree();
        
        mean = tree.firstOrderMoment();
        
        variance = tree.Variance();
        
        //mode = tree.plainMonteCarloMode(5000);
        //mode = tree.gradientMonteCarloMode(50);
        median = tree.median();
        
        returnValues = new Vector();
        
        returnValues.addElement(new Double(mean));
        returnValues.addElement(new Double(variance));
        //returnValues.addElement(new Double(mode));
        returnValues.addElement(new Double(median));
        returnValues.addElement(new Double(exactValue));
        
       // System.out.println(mean+" ; "+variance+" ; "+median+" ; "+exactValue);
        
        return (returnValues);
    } 
 

     /**
     * Predicts the value of the class variable for all the
     * registers in a given database.
     * The prediction is equal to the mean of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is supposed to be stored in column
     * <code>classVariable</code>.
     *
     * The propagation is  done using the Penniless method.
     *
     * @param db the database with the registers to predict.
     * @param bias the bias of the prediction.
     * @return a vector with a vector in each position containing the predicted value
     * in the first position (mean of the
     * posterior distribution), the variance in the second position, the median in the third and the exact value
     * for the class variable in the fourth position.
     */
    
    public Vector predictWithMeanMissingValues(DataBaseCases db, double bias) {
        
        ContinuousCaseListMem cases;
        ContinuousConfiguration conf;
        NodeList vars;
        Node classVar;
        Vector resultValues, registerValues, vectorMeans,  vectorMedians, vectorVariances;
        Vector vectorExact;
        
        int i, nc;
        
        vars = db.getVariables();
        classVar = vars.elementAt(classVariable);
        
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        resultValues = new Vector();
        vectorMeans = new Vector();
        vectorMedians = new Vector();
        vectorVariances = new Vector();
        vectorExact = new Vector();
        
        nc = cases.getNumberOfCases();
        for (i=1 ; i<nc ; i++) {
            //System.out.println("i:" + i);
            conf = (ContinuousConfiguration)cases.get(i);
            conf.removeUndefinedValues();
            //conf.print();
            registerValues = predictWithMeanMissingValues(conf,classVar,bias);
            vectorMeans.addElement((Double)registerValues.elementAt(0));
            vectorVariances.addElement((Double)registerValues.elementAt(1));
            vectorMedians.addElement((Double)registerValues.elementAt(2));
            vectorExact.addElement((Double)registerValues.elementAt(3));
        }
        
        resultValues.addElement(vectorMeans);
        resultValues.addElement(vectorVariances);
        resultValues.addElement(vectorMedians);
        resultValues.addElement(vectorExact);
        
        
        return (resultValues);
    }
    
     /**
     * Predicts the value of the class variable for a given configuration.
     * The prediction is equal to the mean and the median of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is given as argument.
     *
     * The propagation is done using the Penniless method.
     *
     * @param conf the <code>ContinuousConfiguration</code> for which the
     * value of the class will be predicted.
     * @param classVar the class variable.
     * @param bias the bias of the prediction.
     * @return @return a vector with the predicted value in the first position (mean of the
     * posterior distribution), the variance in the second position, the predicted value
     * using the median of the posterior distribution in the third position and the exact value
     * for the class variable in the fourth position.
     */

    
    public Vector predictWithMeanMissingValues(ContinuousConfiguration conf, Node classVar, double bias) {
        
        double exactValue, mean, mode, median, variance;
        Evidence evidence;
        MTESimplePenniless propagation;
        Vector results, returnValues;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree tree;
        
        exactValue = conf.getValue((Continuous)classVar);
        
        // Remove the class variable from the configuration
        conf.remove(classVar);
        
        evidence =  new Evidence(conf);
        
        propagation = new MTESimplePenniless(net,evidence,0,0,0,0,0);
        
        // Since the only unobserved variable is the class, after the propagation,
        // in vector results the only potential will be the corresponding
        // to the posterior distribution of the class.
        propagation.propagate(evidence);
        
        results = propagation.getResults();
        
        pot = (PotentialContinuousPT)results.elementAt(0);
        
        //pot.print();
        
        tree = pot.getTree();
        
        mean = tree.firstOrderMoment() - bias;
        
        variance = tree.Variance();
        
        //mode = tree.plainMonteCarloMode(5000);
        //mode = tree.gradientMonteCarloMode(50);
        median = tree.median() - bias;
        
        returnValues = new Vector();
        
        returnValues.addElement(new Double(mean));
        returnValues.addElement(new Double(variance));
        //returnValues.addElement(new Double(mode));
        returnValues.addElement(new Double(median));
        returnValues.addElement(new Double(exactValue));
        
      //  System.out.println(mean+" ; "+variance+" ; "+median+" ; "+exactValue);
        
        return (returnValues);
    }
    
    
   public Vector predictWithMeanMissingValues(DataBaseCases dbtest) {
        
        ContinuousCaseListMem cases;
        ContinuousConfiguration conf;
        NodeList vars;
        Node classVar;
        Vector resultValues, registerValues, vectorMeans,  vectorMedians, vectorVariances;
        Vector vectorExact;
        
        int i, nc;
        
        vars = dbtest.getVariables();
        classVar = vars.elementAt(classVariable);
        
        cases = (ContinuousCaseListMem)dbtest.getCaseListMem();
        
        resultValues = new Vector();
        vectorMeans = new Vector();
        vectorMedians = new Vector();
        vectorVariances = new Vector();
        vectorExact = new Vector();
        
        nc = cases.getNumberOfCases();
        for (i=1 ; i<nc ; i++) {
          // System.out.println("i:"+i); 
           conf = (ContinuousConfiguration)cases.get(i);
            //Elimino los valores de la configuracion con missing
                               
            conf.removeUndefinedValues();
           
            registerValues = predictWithMeanMissingValues(conf,classVar);
            vectorMeans.addElement((Double)registerValues.elementAt(0));
            vectorVariances.addElement((Double)registerValues.elementAt(1));
            vectorMedians.addElement((Double)registerValues.elementAt(2));
            vectorExact.addElement((Double)registerValues.elementAt(3));
        }
        
        resultValues.addElement(vectorMeans);
        resultValues.addElement(vectorVariances);
        resultValues.addElement(vectorMedians);
        resultValues.addElement(vectorExact);
        
        
        return (resultValues);
    }
 
  /**
     * Computes the rooted mean squared error (RMSE) of the model given as 
     * parameter using a test database.
     * @param model a NaiveMTEPredictor object.
     * @param dbtest a complete database.
     * @return rmse.
     */
    
    private double getrmse(NaiveMTEPredictor model, DataBaseCases dbtest){
        
        double rmse_mean, rmse_median, lcc_mean, lcc_median;
        
        Vector results, errors;
        
        double bias; 
        
        dbtest.removeCasesMissingValue(classVariable);
        
        results = this.predictWithMeanMissingValues(dbtest);
        
        bias = NaiveMTEPredictor.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));
                
        results = predictWithMeanMissingValues(dbtest,bias);
        
        errors = NaiveMTEPredictor.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));
        
        rmse_mean = ((Double)(errors.elementAt(0))).doubleValue();
        lcc_mean = ((Double)(errors.elementAt(1))).doubleValue();
        
        errors = model.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));
        
        rmse_median = ((Double)(errors.elementAt(0))).doubleValue();
        
        lcc_median = ((Double)(errors.elementAt(1))).doubleValue();
            
       /* System.out.println("rmse_mean: " + rmse_mean);
        System.out.println("lcc_mean: " + lcc_mean);
        System.out.println("rmse_median: " + rmse_median);
        System.out.println("lcc_median: " + lcc_median);
        System.out.println("\n");
*/
        
        return rmse_mean;
        
    }
 
  /**
     * Fill the missing values in the database simulating from the univariate 
     * distribution learnt from the present values for each variable. 
     */
     
    private DataBaseCases FillValuesLearnUnivariate (){
            
        Node n;
        DataBaseCases dbaux= dbCases.copy();
        DataBaseCases dbfilled = dbCases.copy();

        NodeList nl;
        int pos=0;
        ContinuousProbabilityTree cpt;
        MTELearning learningObject = new MTELearning();
        
        DataBaseCases cases = new DataBaseCases();

        
        for (int i=0;i<dbfilled.getVariables().size();i++){
            
            dbaux= dbfilled.copy();
            n = dbCases.getVariables().elementAt(i);
            //System.out.println("Procesing variable: " + n.getName()+ ". . .");
            nl = new NodeList();
            nl.insertNode(n);
            dbaux.projection(nl); 
            pos = 0;
            for (int j=0;j<dbfilled.getNumberOfCases();j++){
               //System.out.println(dbfilled.getCaseListMem().getValue(j,i));
               if (Continuous.isUndefined(dbfilled.getCaseListMem().getValue(j,i))){
                                              
                    dbaux.getCaseListMem().getCases().removeElementAt(pos);
                    dbaux.getCaseListMem().setNumberOfCases(dbaux.getCaseListMem().getNumberOfCases()-1);
                    dbaux.setNumberOfCases(dbaux.getNumberOfCases()-1);
                }
                else 
                    pos++;
            }
            //dbaux.saveDataBase(new FileWriter("temp.dbc"));
            //Ya tengo la base de datos de la variable limpia de valores missing. COMPROBAR!!!!
            //---------------------------------------------------------------------------------
            //Aprendo un CPT para simular valores y rellenar los valores missing
            cpt = new ContinuousProbabilityTree();
            cpt = learningObject.learnConditional(n,new NodeList(),dbaux, 4, 4);
           
            //Recorro la base de datos en busca de posiciones missing e inserto los valores simulados
            for (int k=0;k<dbCases.getNumberOfCases();k++){
              if (Continuous.isUndefined(dbfilled.getCaseListMem().getValue(k,i))){
                    dbfilled.getCaseListMem().setValue(k,i,cpt.simulateValue());      
                    //System.out.println("Valor simulado: "+ cpt.simulateValue());
                 }
            }
        }
        return dbfilled;
    }
     
      /*
     * Create a new database filling the missing values as follows:
     * For the features variables: simulating a value from the model given as
     * parameter.
     * For the class variable: Predicting with the mean or median of the 
     * posterior distribution of the variable.
     * @param model is a NaiveMTEPredictor used to obtain the missing
     * @return a complete database
     */   
    private DataBaseCases FillSimulatedPredictedValues(NaiveMTEPredictor model) {

         ContinuousConfiguration conf, confprop;
         Node n;
         ContinuousProbabilityTree cpt;
         Evidence evidence;
         VariableElimination prop;
         Vector results;
         double bias;
         PotentialContinuousPT pot;
         double doubval;
         int intval;
              
         //System.out.println("CLASSVARIABLE:" + dbtemp.getVariables().elementAt(model.classVariable).getName());
         
         DataBaseCases dbaux=dbCases.copy();
         
         for(int i=0;i<dbCases.getNumberOfCases();i++){
           
         
            
            conf = (ContinuousConfiguration)dbCases.getCaseListMem().get(i).copy();
            
            
                //conf = (ContinuousConfiguration)dbtemp.getCaseListMem().get(i).copy();
                
                for (int j=0;j<dbCases.getVariables().size();j++){
                    
                    n = dbCases.getVariables().elementAt(j);         
                    
                    if (Continuous.isUndefined(dbaux.getCaseListMem().getValue(i,j))){//missing
  
                       confprop=(ContinuousConfiguration)conf.copy();
                        
                       if (j == classVariable){ //predict
                           confprop.print();
                            results = predictWithMeanMissingValues(confprop,n);
                            doubval = ((Double)results.elementAt(0)).doubleValue();
                            //System.out.println(doubval);
                            //System.out.println("VARIABLE:" + n.getName()+ "["+((Continuous)n).getMin()+","+((Continuous)n).getMax()+"] --> "+ doubval); 
                            conf.putValue((Continuous)n,doubval);
                            //System.out.println("PREDICT:" + doubval);
                       }      
                       else {
                            confprop.remove(n);
                            evidence = new Evidence(confprop);
                            prop= new VariableElimination(model.net,evidence);
                            NodeList temp = new NodeList();
                            temp.insertNode(n);
                            prop.setInterest(temp);
                            prop.propagate();
                            results = prop.getResults();
                            pot = (PotentialContinuousPT)results.elementAt(0);
                            cpt = pot.getTree();

                            //System.out.println("SIMULATE:" + cpt.simulateValue()); 

                            if (n instanceof FiniteStates){
                                intval=Math.round((float)cpt.simulateValue());
                                conf.putValue((FiniteStates)n,intval);
                                //System.out.println(intval);
                            }
                            else {//Continuous variable
                                doubval = cpt.simulateValue();                        
                           // System.out.println("VARIABLE:" + n.getName()+ "["+((Continuous)n).getMin()+","+((Continuous)n).getMax()+"] --> "+ doubval);
                                conf.putValue((Continuous)n,doubval);
                            }
                       }
                   }   
                }
                //Modify current case
                ((ContinuousCaseListMem)dbaux.getCaseListMem()).replaceCase(conf,i);   
         }
         return dbaux;
    }
    
       
       
       
    /**
     * }
     * Main for constructing an MTE naive predictor from a data base.
     *
     * Arguments:
     * 1. the dbc  train file.
     * 2. the dbc test file. "CV" for cross-validation.
     * 3. the index of the class variables (starting from 0).
     * 4. the number of intervals into which the domain of the continuous variables
     *    will be split.
     * 5. In case of cross validation, the number of folds.
     */      
    public static void main(String args[])throws ParseException,IOException{
        
        /*  FileInputStream f = new FileInputStream(args[0]);
          DataBaseCases dbfull = new DataBaseCases(f);
          dbfull.setMissingValues(0.5);
          dbfull.saveDataBase(new FileWriter("mte50_missing0.5.dbc"));
      */
        double lcc_mean = 0.0, lcc_median = 0.0, rmse_mean = 0.0, rmse_median = 0.0;
        
        if (args[1].compareTo("CV") == 0) { // Cross validation
            FileInputStream f = new FileInputStream(args[0]);
            int k = Integer.valueOf(args[4]).intValue(), i;
            int classv = Integer.valueOf(args[2]).intValue();
            int interv = Integer.valueOf(args[3]).intValue();
            Vector results, errors;
            double bias;
            DataBaseCases dbTrain, dbTest;
            DataBaseCases c = new DataBaseCases(f);
           
            
            for (i=0 ; i<k ; i++) {
                System.out.println("ITERATION "+i);
                
                dbTrain = c.getTrainCV(i,k);
                dbTest = c.getTestCV(i,k);
                
                
                
                dbTest.removeCasesMissingValue(classv);
               // dbTrain.removeCasesMissingValue(classv);
                
                NaiveMTEPredictorMissingData pred = new NaiveMTEPredictorMissingData(dbTrain, classv,interv);
                                
                results = pred.predictWithMeanMissingValues(dbTrain);
                bias = NaiveMTEPredictor.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));
                
                
                
                results = pred.predictWithMeanMissingValues(dbTest,bias);
                errors = NaiveMTEPredictor.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));
                rmse_mean += ((Double)(errors.elementAt(0))).doubleValue();
                lcc_mean += ((Double)(errors.elementAt(1))).doubleValue();
                
                errors = NaiveMTEPredictor.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));
                rmse_median += ((Double)(errors.elementAt(0))).doubleValue();
                lcc_median += ((Double)(errors.elementAt(1))).doubleValue();
            }
        
            rmse_mean /= k;
            lcc_mean /= k;
            rmse_median /= k;
            lcc_median /= k;
            System.out.println("-------------------------------------------------");
            System.out.println(k+"-fold cross validation.");
            System.out.println("\nFinal results:");
            System.out.println("rmse_mean,lcc_mean,rmse_median,lcc_median");
            System.out.println(rmse_mean + "," + lcc_mean + "," + rmse_median + "," + lcc_median);
            System.out.println("\n");

        } else {
            FileInputStream f = new FileInputStream(args[0]);
            FileInputStream f2 = new FileInputStream(args[1]);
            int classv = Integer.valueOf(args[2]).intValue();
            int interv = Integer.valueOf(args[3]).intValue();
            Vector results, errors;
            double bias;
            
            DataBaseCases c = new DataBaseCases(f);
          
            NaiveMTEPredictor pred = new NaiveMTEPredictor(c,classv,interv);
            
            pred.saveNetwork("NB.elv");
            
            results = pred.predictWithMean(c);
            
            bias = pred.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));
            
            DataBaseCases d = new DataBaseCases(f2);
           
            results = pred.predictWithMean(d,bias);
            
            System.out.println("Hold-out validation");
            errors = pred.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));
           // System.out.println("With the mean");
           // System.out.println("rmse = "+((Double)(errors.elementAt(0))).doubleValue()+" ; lcc = "+((Double)(errors.elementAt(1))).doubleValue());
            
            errors = pred.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));
            //System.out.println("With the median");
            //System.out.println("rmse = "+((Double)(errors.elementAt(0))).doubleValue()+" ; lcc = "+((Double)(errors.elementAt(1))).doubleValue());
            

        }
    }
}

