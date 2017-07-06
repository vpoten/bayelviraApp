package elvira.learning.classification.supervised.continuous;

import elvira.Elvira;
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
 *
 * @author Antonio Fernandez Alvarez (afalvarez@ual.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 *
 * @since 27/04/2007
 */

public class SelectiveTANMTEPredictor extends TANMTEPredictor{
     
    double rmse_mean_M, rmse_median_M, lcc_mean_M,lcc_median_M;
    
    public SelectiveTANMTEPredictor() {
        
        variables = new NodeList();
        classVariable = -1;
        net = new Bnet();       
    }
   /**
     * Creates a SelectiveTANMTEPredictor from a database. Feature subset 
     * selection is carried out based on a filter approach using conditional
     * mutual information.
     *
     * @param db the DataBaseCases from which the predictor will be constructed.
     * @param cv an int indicating the index (column) of the class variable in 
     * the database. The first column is labeled as 0.
     * @param intervals the number of intervals into which the domain of the continuous
     * variables will be split.
     */
    public SelectiveTANMTEPredictor(DataBaseCases dbTrain, DataBaseCases dbTest, int cv, int intervals) throws ParseException,IOException{

             
        TANMTEPredictor M, M1;
        MTELearning learningObject;
        ContinuousProbabilityTree t;
        Node classVar, childVar, child_i;
        Vector netRL;
        NodeList netNodeList, relationVariables;
        PotentialContinuousPT pot;
        Relation rel;
        DataBaseCases dbTrainAux, dbTestAux;
        double bias;//,rmse_mean_M, rmse_median_M, lcc_mean_M,lcc_median_M;
        double rmse_mean_M1, rmse_median_M1, lcc_mean_M1, lcc_median_M1;
        
        variables=dbTrain.getNodeList().copy();
        System.out.print("VARIABLES: ");
        variables.printNames(); 
        
       
        netNodeList=new NodeList();
        netRL=new Vector();
                 
        classVar=variables.elementAt(cv).copy();
    
        //Compute the mutual informations �(Xi,Y) and order them by decreasing 
        //way. It's necesary to create a NaiveMTEPredictor
        NaiveMTEPredictor predNB=new NaiveMTEPredictor(dbTrain, cv, intervals);
        Vector v_MI=new Vector(predNB.getListOfMututlinformation());
        System.out.println("VECTOR INF. MUTUA: " + v_MI.toString());
        Vector v_OrderedMI= new Vector(this.OrderedIndexByMutualInformation(v_MI));
        System.out.println("ORDENADOS: " + v_OrderedMI.toString());
       
       /* Vector v_OrderedMI;
        v_OrderedMI=new Vector();
        v_OrderedMI.addElement(new Integer(4));
        v_OrderedMI.addElement(new Integer(3));
        v_OrderedMI.addElement(new Integer(5));
        v_OrderedMI.addElement(new Integer(1));
        v_OrderedMI.addElement(new Integer(0));
        v_OrderedMI.addElement(new Integer(2));
         System.out.println("ORDENADOS: " + v_OrderedMI.toString());
*/
        //Proyect in the dbc the actual variables: X(1) and Y        
        //-------------------------------------------------------------------------     
        System.out.println("Inserting the class variable: " + classVar.getName());
        netNodeList.insertNode(classVar);
        
        childVar=variables.elementAt(((Integer)v_OrderedMI.elementAt(0)).intValue()).copy();
        System.out.println("Inserting X(0): " + childVar.getName());
        netNodeList.insertNode(childVar);
                   
        dbTrainAux=dbTrain.copy();
        dbTestAux=dbTest.copy();
        
        for (int j=0;j<netNodeList.size();j++){
            ((Node)netNodeList.getNodes().elementAt(j)).setParents(new LinkList());
            ((Node)netNodeList.getNodes().elementAt(j)).setChildren(new LinkList());
            ((Node)netNodeList.getNodes().elementAt(j)).setSiblings(new LinkList());
        }       
        
        dbTrainAux.projection(netNodeList);
        dbTestAux.projection(netNodeList);
        
        //Create the initial NaiveBayes Model
        M=new TANMTEPredictor(dbTrainAux, 0, intervals, this.getRandomIndex());
      
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

            TANMTEPredictor Maux=new TANMTEPredictor();
           // TANMTEPredictor Maux=new TANMTEPredictor(dbTrainAux,0,intervals);
            Maux=copy_model(M);
            
            //Construct a naive Bayes model M1 for the variables Y and X(i)
            //-----------------------------------------------------------------       
            child_i=variables.elementAt(((Integer)v_OrderedMI.elementAt(i)).intValue()).copy();
            System.out.println("Insertando la variable X("+i+"): " + child_i.getName());          
            netNodeList.insertNode(child_i);
           
            //Proyect in the dbc the actual variables of the model using 
            //auxiliar variables to don�t lose the original dbc
            //----------------------------------------------------------------       
            
            System.out.println("Proyectando la dbc con:");
            netNodeList.printNames();
         
            dbTrainAux=dbTrain.copy();
            dbTestAux=dbTest.copy();
         
            for (int j=0;j<netNodeList.size();j++){
                ((Node)netNodeList.getNodes().elementAt(j)).setParents(new LinkList());
                ((Node)netNodeList.getNodes().elementAt(j)).setChildren(new LinkList());
                ((Node)netNodeList.getNodes().elementAt(j)).setSiblings(new LinkList());
            }   
                   
            dbTrainAux.projection(netNodeList);
            dbTestAux.projection(netNodeList);            
            
           
            //Construct the new model
            
            //Antes de llamar con dbTrain debemos eliminar los padres de cada nodo para
            //que se construya un nuevo TAN sin problemas
            M=new TANMTEPredictor(dbTrainAux, 0, intervals, this.getRandomIndex());
            
            
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

            System.out.println("\n------------------ OLD MODEL ---------------------------------------");
            System.out.println("MEAN --->  rmse_M: " + rmse_mean_M + "   lcc_M: "+ lcc_mean_M);       
            System.out.println("MEDIAN ->  rmse_M: " + rmse_median_M + "   lcc_M: "+ lcc_median_M);
            System.out.println("--------------------------------------------------------------------");

            
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
            //AQUI ESTA EL PROBLEMA
            //Don't improve
            else {
                System.out.println("Borrando el nodo "+ child_i.getName());
                
                /*System.out.println("----------------- ANTES -----------------------------");
                M.printParents();
                System.out.println("\n");*/
                
                netNodeList.removeNode(child_i);
                               
                dbTrainAux=dbTrain.copy();
                dbTestAux=dbTest.copy();
                
                for (int j=0;j<netNodeList.size();j++){
                    ((Node)netNodeList.getNodes().elementAt(j)).setParents(new LinkList());
                    ((Node)netNodeList.getNodes().elementAt(j)).setChildren(new LinkList());
                    ((Node)netNodeList.getNodes().elementAt(j)).setSiblings(new LinkList());
                }
                
                dbTrainAux.projection(netNodeList);
                dbTestAux.projection(netNodeList); 
                
                M=Maux;
                /*System.out.println("\n----------------- DESP -----------------------------");
                M.printParents();   
                System.out.println("\n");*/
            }      
            
        }
        System.out.println("\n\n\n===================== DATOS DEL MODELO FINAL =======================");
        System.out.println("MEAN --->  rmse_M: " + rmse_mean_M + "   lcc_M: "+ lcc_mean_M);       
        System.out.println("MEDIAN ->  rmse_M: " + rmse_median_M + "   lcc_M: "+ lcc_median_M);
        System.out.println("====================================================================");
        System.out.println("VARIABLES DEL MODELO FINAL: "+netNodeList.size());
        netNodeList.printNames();
        System.out.println("====================================================================\n\n");
               
        //Update the actual object with the data obtained
        this.classVariable=M.classVariable;       
        this.net.setNodeList(M.net.getNodeList());
        this.variables=M.variables;
        this.net.setRelationList(M.net.getRelationList());
        this.net.setLinkList(M.net.getLinkList());
    }
     public void printParents(){
        System.out.print("\nPADRES DE CADA NODO\n");
       for (int i=0;i<net.getNodeList().size();i++){
           System.out.print("\nNODO: " + net.getNodeList().elementAt(i).getName()+" <--");
           for (int j=0;j<net.getNodeList().elementAt(i).getParentNodes().size();j++){
                System.out.print(" " + net.getNodeList().elementAt(i).getParentNodes().elementAt(j).getName());
           }
       }
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
private Vector OrderedIndexByMutualInformation (Vector W){
   
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
     * Main for constructing an MTE selective TAN predictor from a data base.
     *
     * Arguments:
     * 1. the dbc train file.
     * 2. the dbc test file. "CV" for cross-validation.
     * 3. the index of the class variables (starting from 0).
     * 4. the number of intervals into which the domain of the continuous 
     *    variables will be split.
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
                
                SelectiveTANMTEPredictor pred = new SelectiveTANMTEPredictor(dbTrain, dbTest,classv,interv);              
                       
                rmse_mean += pred.rmse_mean_M; 
                lcc_mean +=  pred.lcc_mean_M;
                
                rmse_median += pred.rmse_median_M;
                lcc_median += pred.lcc_median_M;
            }
        
            rmse_mean /= k;
            lcc_mean /= k;
            rmse_median /= k;
            lcc_median /= k;
            
            System.out.println(k+"-fold cross validation.");
            System.out.println("\nFinal results:");
            System.out.println("rmse_mean,lcc_mean,rmse_median,lcc_median");
            System.out.println(rmse_mean + "," + lcc_mean + "," + rmse_median + "," + lcc_median);
            System.out.println("\n");

        } else {
        
            FileInputStream fTrain = new FileInputStream(args[0]);
            FileInputStream fTest = new FileInputStream(args[1]);
            int classv = Integer.valueOf(args[2]).intValue();
            int interv = Integer.valueOf(args[3]).intValue();

            Vector results, errors;

            DataBaseCases cTrain = new DataBaseCases(fTrain);
            DataBaseCases cTest = new DataBaseCases(fTest);      

            SelectiveTANMTEPredictor pred = new SelectiveTANMTEPredictor(cTrain, cTest, classv,interv);
            pred.saveNetwork("SelectiveTAN.elv");
        }
        
        
       
        





    

    



        //Divide the database cases: dbTrain y dbTest
        //---------------------------------------------------------------------      
     /* DataBaseCases dbTrain=new DataBaseCases();
        DataBaseCases dbTest=new DataBaseCases();
        
        db.divideIntoTrainAndTest(dbTrain,dbTest,0.7);         
        try {
            dbTrain.saveDataBase(new FileWriter("c:\\bd\\" + db.getName()+ "_train.dbc"));
            dbTest.saveDataBase(new FileWriter("c:\\bd\\" + db.getName() + "_test.dbc"));
        }catch(Exception e){}; */
         
        
        
    }
}
