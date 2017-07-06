package elvira.learning.classification.supervised.continuous;

import elvira.Elvira;
import elvira.decisionTrees.SuperValueAddFunction;
import java.util.*;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.potential.*;

import elvira.*;
import java.util.Enumeration;
import java.util.Vector;
import elvira.database.*;
import elvira.learning.preprocessing.ProjectDBC;
import elvira.learning.MTELearning;

/**
 * Implements a predictor (classificator with continuous class variable) where
 * all the variables, including the class, are of class MTE.
 * The model assumes that the joint distribution is MTE.
 *
 * Feature subset selection is carried out based on a filter approach
 * using mutual information.
 *
 * @author Antonio Fernandez Alvarez (afalvarez@ual.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 *
 * @since 14/06/2007
 */

public class SelectiveNaiveMTEPredictor extends NaiveMTEPredictor {
       
    
    
    double rmse_mean_M, rmse_median_M, lcc_mean_M,lcc_median_M;
    /**
     * Creates an empty instance of this class.
     */
             
    public SelectiveNaiveMTEPredictor()  throws java.lang.Throwable{
        
        variables = new NodeList();
        classVariable = -1;
        net = new Bnet();  
    }
    
    
    /**
     * Creates a SelectiveNaiveMTEPredictor from a database. Feature subset 
     * selection is carried out based on a filter approach using mutual 
     * information.
     *
     * @param dbTrain the training DataBaseCases.
     * @param dbTest the test DataBaseCases.
     * @param cv an int indicating the index (column) of the class variable in the database. The first column
     * is labeled as 0.
     * @param intervals the number of intervals into which the domain of the continuous
     * variables will be split.
     */
    
    public SelectiveNaiveMTEPredictor(DataBaseCases dbTrain, DataBaseCases dbTest, 
            int cv, int intervals) throws ParseException,IOException{
              
        super(dbTrain,cv,intervals);
    
        NodeList netNodeList;
        Node classVar, childVar, child_i;
        DataBaseCases dbTrainAux, dbTestAux;
        NaiveMTEPredictor M;
        double bias;//,rmse_mean_M, rmse_median_M, lcc_mean_M,lcc_median_M;
        double rmse_mean_M1, rmse_median_M1, lcc_mean_M1, lcc_median_M1;
        
        System.out.print("VARIABLES: ");variables.printNames();
        
        netNodeList=new NodeList();      
        classVar=variables.elementAt(cv).copy();
      
        //Compute the mutual informations �(Xi,Y) and order them by decreasing way
        //---------------------------------------------------------------------      
        Vector v_MI=new Vector(this.getListOfMututlinformation());
        System.out.println("MUTUAL INFORMATION VECTOR: " + v_MI.toString());
        Vector v_OrderedMI= new Vector(this.OrderedIndexByMutualInformation(v_MI));
        System.out.println("ORDER: " + v_OrderedMI.toString());
        
        //Proyect in the dbc the actual variables: X(0) and Y        
        //-------------------------------------------------------------------------           
        System.out.println("Inserting the class variable: " + classVar.getName());
        netNodeList.insertNode(classVar);
        
        childVar=variables.elementAt(((Integer)v_OrderedMI.elementAt(0)).intValue()).copy();
        System.out.println("Inserting X(0): " + childVar.getName());
        netNodeList.insertNode(childVar);

        dbTrainAux=dbTrain.copy();
        dbTestAux=dbTest.copy();
        dbTrainAux.projection(netNodeList);
        dbTestAux.projection(netNodeList);
        
        //Create the initial NaiveBayes Model
        M=new NaiveMTEPredictor(dbTrainAux, 0, intervals);
     
        //Compute rmse and lcc
        //---------------------------------------------------------------------
        Vector results=new Vector();
        Vector vError=new Vector();
        
        results=M.predictWithMean(dbTrainAux);
        bias=M.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));
              
        results=M.predictWithMean(dbTestAux,bias); 

        vError =M.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));
        
        rmse_mean_M=((Double)(vError.elementAt(0))).doubleValue();
        lcc_mean_M=((Double)(vError.elementAt(1))).doubleValue();
        
        vError = M.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));
        
        rmse_median_M=((Double)(vError.elementAt(0))).doubleValue();
        lcc_median_M=((Double)(vError.elementAt(1))).doubleValue();
        
        System.out.println("--------------INITIAL MODEL WITH Y AND X(1)----------------------------");
        System.out.println("MEAN ----->  rmse_M: " + rmse_mean_M + "    lcc_M: "+ lcc_mean_M);       
        System.out.println("MEDIAN --->  rmse_M: " + rmse_median_M + "    lcc_M: "+ lcc_median_M);
        System.out.println("-----------------------------------------------------------------------");   
      
        //For each X(i), i>=1
        for (int i=1;i<v_OrderedMI.size()-1;i++){
            
            //Construct a naive Bayes model M1 for the variables Y and X(i)
            //-----------------------------------------------------------------       
            child_i=variables.elementAt(((Integer)v_OrderedMI.elementAt(i)).intValue()).copy();
            System.out.println("Inserting variable X("+i+"): " + child_i.getName());          
            netNodeList.insertNode(child_i);
           
            //Proyect in the dbc the actual variables of the model using 
            //auxiliar variables to don�t lose the original dbc
            //----------------------------------------------------------------       
            dbTrainAux=dbTrain.copy();
            dbTestAux=dbTest.copy();
            dbTrainAux.projection(netNodeList);
            dbTestAux.projection(netNodeList);            
            
            //Construct the new model
            M=new NaiveMTEPredictor(dbTrainAux, 0, intervals);
            
            //Compute rmse and lcc of the new model
            //---------------------------------------------------------------------          
            results=M.predictWithMean(dbTrainAux);
            bias=M.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));            
            results=M.predictWithMean(dbTestAux,bias); 
            
            vError = M.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));

            rmse_mean_M1=((Double)(vError.elementAt(0))).doubleValue();
            lcc_mean_M1=((Double)(vError.elementAt(1))).doubleValue();

            vError = M.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));

            rmse_median_M1=((Double)(vError.elementAt(0))).doubleValue();
            lcc_median_M1=((Double)(vError.elementAt(1))).doubleValue();
        
            System.out.println("------------------ NEW MODEL ---------------------------------------");
            System.out.println("MEAN --->  rmse_M1: " + rmse_mean_M1 + "   lcc_M1: "+ lcc_mean_M1);       
            System.out.println("MEDIAN ->  rmse_M1: " + rmse_median_M1 + "   lcc_M1: "+ lcc_median_M1);
            System.out.println("--------------------------------------------------------------------");

            //If the rmse is lower I keep the last variable in the model. In
            //other case, I removed the last variable information from the model
            //---------------------------------------------------------------------
            //Improve
            if ((rmse_mean_M1<rmse_mean_M)&&(rmse_mean_M1<rmse_median_M)||
                (rmse_median_M1<rmse_mean_M)&&(rmse_median_M1<rmse_median_M)){              
                System.out.println("This model improves to the last one. Updating ...");
                rmse_mean_M=rmse_mean_M1;
                rmse_median_M=rmse_median_M1;  
                lcc_mean_M=lcc_mean_M1;
                lcc_median_M=lcc_median_M1;
            }
            //Don't improve
            else {
                try{
                    M.net.removeLink(classVar, child_i);
                } catch(Exception e){}
                M.net.removeRelation(child_i);                         
                netNodeList.removeNode(child_i);
            }
          //  netNodeList.printNames();
            
        }
        System.out.println("\n\n\n===================== FINAL MODEL =======================");
        System.out.println("MEAN --->  rmse_M: " + rmse_mean_M + "   lcc_M: "+ lcc_mean_M);       
        System.out.println("MEDIAN ->  rmse_M: " + rmse_median_M + "   lcc_M: "+ lcc_median_M);
        System.out.println("====================================================================");
        System.out.println("FINAL MODEL VARIABLES: "+netNodeList.size());
        netNodeList.printNames();
        System.out.println("====================================================================\n\n");
        
        //Update the actual object with the data obtained
        this.classVariable=M.classVariable;       
      
        this.net.setNodeList(M.net.getNodeList());
       
        this.variables=M.variables;
   
        this.net.setRelationList(M.net.getRelationList());
    
        this.net.setLinkList(M.net.getLinkList());
      
        
    }
    
    
   
/**
 * Obtains the descending order of the numbers of the input vector
 * @param W <code>Vector</code> contains the mutual informations between
 * each feature variable and the class variable in the same order that 
 * the nodes in NodeList.
 * @return a <code>Vector</code> with the indexes position ordered by 
 * descending.
 *
 */ 
public static Vector OrderedIndexByMutualInformation (Vector W){
   
     double max; 
     int index;
     
     Vector ordered_indexes=new Vector();
     Vector weight_aux=(Vector)W.clone();
     
     for (int i=0;i<weight_aux.size();i++){
        max=-1000.0;
        index=0;
        for (int j=0;j<weight_aux.size();j++){
            if (((Double)weight_aux.elementAt(j)).doubleValue()>max){
                max=((Double)weight_aux.elementAt(j)).doubleValue();
                index=j;              
            }
        }
        ordered_indexes.addElement(new Integer(index));
        weight_aux.setElementAt(new Double(-1000.0),index);
     }
     return ordered_indexes;
}     
    
    
   /**
     * Main for constructing an MTE selective naive predictor from a data base.
     *
     * Arguments:
     * 1. the dbc train file.
     * 2. the dbc test file. "CV" for cross-validation.
     * 3. the index of the class variables (starting from 0).
     * 4. the number of intervals into which the domain of the continuous variables
     *    will be split.
     * 5. In case of cross validation, the number of folds.
     */
    
    public static void main(String args[]) throws ParseException,IOException {
      
        
        if (args[1].compareTo("CV") == 0) { // Cross validation
            FileInputStream f = new FileInputStream(args[0]);
            int k = Integer.valueOf(args[4]).intValue(), i;
            int classv = Integer.valueOf(args[2]).intValue();
            int interv = Integer.valueOf(args[3]).intValue();
            Vector results, errors;
            double bias;
            DataBaseCases dbTrain, dbTest;
            DataBaseCases c = new DataBaseCases(f);
            double lcc_mean = 0.0, lcc_median = 0.0, rmse_mean = 0.0, rmse_median = 0.0;
        
            
            for (i=0 ; i<k ; i++) {
               
                System.out.println("ITERATION "+i);
               
                dbTrain = c.getTrainCV(i,k);
                dbTest = c.getTestCV(i,k);
                
                SelectiveNaiveMTEPredictor pred = new SelectiveNaiveMTEPredictor(dbTrain, dbTest,classv,interv);              
                       
                rmse_mean += pred.rmse_mean_M; 
                lcc_mean +=  pred.lcc_mean_M;
                
                rmse_median += pred.rmse_median_M;
                lcc_median += pred.lcc_median_M;
            }
        
            rmse_mean /= k;
            lcc_mean /= k;
            rmse_median /= k;
            lcc_median /= k;
            
            System.out.println("\nFinal results:");
            System.out.println("rmse_mean,lcc_mean,rmse_median,lcc_median");
            System.out.println(rmse_mean + "," + lcc_mean + "," + rmse_median + "," + lcc_median);
            System.out.println("\n");
        } else {
        
            Vector results, errors;

            FileInputStream fTrain = new FileInputStream(args[0]);
            FileInputStream fTest = new FileInputStream(args[1]);
            int classv = Integer.valueOf(args[2]).intValue();
            int interv = Integer.valueOf(args[3]).intValue();

            DataBaseCases dbTrain = new DataBaseCases(fTrain);
            DataBaseCases dbTest = new DataBaseCases(fTest);

            SelectiveNaiveMTEPredictor pred = 
               new SelectiveNaiveMTEPredictor(dbTrain, dbTest,classv,interv);
             
            }
  
        //Divide the database cases: dbTrain y dbTest
        //---------------------------------------------------------------------      
     /* DataBaseCases dbTrain=new DataBaseCases();
        DataBaseCases dbTest=new DataBaseCases();
        
        db.divideIntoTrainAndTest(dbTrain,dbTest,0.7);         
        try {
            dbTrain.saveDataBase(new FileWriter(db.getName()+ "_train.dbc"));
            dbTest.saveDataBase(new FileWriter(db.getName() + "_test.dbc"));
        }catch(Exception e){}; */
         
        
        
    }
}
