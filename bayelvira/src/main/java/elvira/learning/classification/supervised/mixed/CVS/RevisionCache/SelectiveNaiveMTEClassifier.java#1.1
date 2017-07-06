package elvira.learning.classification.supervised.mixed;


import elvira.Elvira;
import java.util.*;
import elvira.tools.VectorManipulator;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.potential.*;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.ConfusionMatrix;
import elvira.*;
import java.util.Vector;
import elvira.learning.MTELearning;
import elvira.learning.classification.supervised.continuous.*;

/**
 *
 * @author afa
 */
public class SelectiveNaiveMTEClassifier extends NaiveMTEClassifier{
    
    public double accuracy;
    

    /** Creates a new instance of SelectiveNaiveMTEClassifier */
    public SelectiveNaiveMTEClassifier(DataBaseCases dbtrain, DataBaseCases dbtest, int classindex, int intervals) 
                                        throws elvira.InvalidEditException {
        
        //Compute the mutual informations I(Xi,Y) and order them by decreasing way
        //------------------------------------------------------------------------      
        NaiveMTEPredictor auxNB=new NaiveMTEPredictor(dbtrain, classindex, intervals);
        Vector v_MI=new Vector(auxNB.getListOfMututlinformation());
        System.out.println("MUTUAL INFORMATION VECTOR: " + v_MI.toString());
        Vector v_OrderedMI= new Vector(SelectiveNaiveMTEPredictor.OrderedIndexByMutualInformation(v_MI));
        System.out.println("ORDER: " + v_OrderedMI.toString());
        //------------------------------------------------------------------------ 
        
        
        
        NodeList netNodeList=new NodeList();     
        Node classVar, childVar, child_i;
        DataBaseCases dbTrainAux, dbTestAux;
        NaiveMTEClassifier M;
        double acc,acc1;
        
        
        Vector<Node> vNodes = dbtrain.getNewVectorOfNodes(); 
        classVar=vNodes.elementAt(classindex).copy();
               
        
        //Proyect in the dbc the actual variables: X(0) and Y        
        //-------------------------------------------------------------------------           
        System.out.println("Inserting the class variable: " + classVar.getName());
        netNodeList.insertNode(classVar);
        
        childVar=vNodes.elementAt(((Integer)v_OrderedMI.elementAt(0)).intValue()).copy();
        System.out.println("Inserting X(0): " + childVar.getName());
        netNodeList.insertNode(childVar);

        dbTrainAux=dbtrain.copy();
        dbTestAux=dbtest.copy();
        dbTrainAux.projection(netNodeList);
        dbTestAux.projection(netNodeList);
        
        
        M=new NaiveMTEClassifier(dbTrainAux,0,intervals);
        M.structuralLearning();
        M.parametricLearning();
        acc=M.test(dbTestAux,0);
       
        System.out.println("--------------INITIAL MODEL WITH Y AND X(1)----------------------------");
        System.out.println("ACCURACY ----->  " + acc);       
        System.out.println("-----------------------------------------------------------------------");  

              
        //For each X(i), i>=1
        for (int i=1;i<v_OrderedMI.size()-1;i++){
            
            //Construct a naive Bayes model M1 for the variables Y and X(i)
            //-----------------------------------------------------------------       
            child_i=vNodes.elementAt(((Integer)v_OrderedMI.elementAt(i)).intValue()).copy();
            System.out.println("Inserting variable X("+i+"): " + child_i.getName());          
            netNodeList.insertNode(child_i);
           
            //Proyect in the dbc the actual variables of the model using 
            //auxiliar variables to don�t lose the original dbc
            //----------------------------------------------------------------       
            dbTrainAux=dbtrain.copy();
            dbTestAux=dbtest.copy();
            dbTrainAux.projection(netNodeList);
            dbTestAux.projection(netNodeList);            
            
            //Construct the new model
            M=new NaiveMTEClassifier(dbTrainAux, 0, intervals);
            M.structuralLearning();
            M.parametricLearning();
            acc1=M.test(dbTestAux,0);
                
            System.out.println("NEW MODEL ACCURACY --->  " + acc1);       

            //If the rmse is lower I keep the last variable in the model. In
            //other case, I removed the last variable information from the model
            //---------------------------------------------------------------------
            //Improve
            if (acc1>acc){
                System.out.println("This model improves to the last one. Updating ...");
                acc=acc1;
            }
            //Doesn't improve
            else {
                try{
                    M.getClassifier().removeLink(classVar, child_i);
                } catch(Exception e){}
                M.getClassifier().removeRelation(child_i);                         
                netNodeList.removeNode(child_i);
            }
        }
        System.out.println("\n\n\n===================== FINAL MODEL =======================");
        System.out.println("ACCURACY --->  " + acc);
        System.out.println("====================================================================");
        System.out.println("FINAL MODEL VARIABLES: "+ netNodeList.size()); netNodeList.printNames();       
        System.out.println("====================================================================\n\n");
      
        this.accuracy=acc;
        this.classifier=new Bnet();
        this.classifier.setNodeList(netNodeList);
        this.classifier.setLinkList(M.getClassifier().getLinkList());    
        this.classifier.setRelationList(M.getClassifier().getRelationList());
    
        
    }
    /**
     * Main for constructing an MTE NB classifier from a data base.
     *
     * Arguments:
     * 1. the dbc train file.
     * 2. the dbc test file. "CV" for cross-validation.
     * 3. the index of the class variables (starting from 0).
     * 4. the number of intervals into which the domain of the continuous 
     *    variables will be split.
     * 5. In case of cross validation, the number of folds.
     */
    public static void main(String args[]) throws Exception {
               
        FileInputStream f = new FileInputStream(args[0]);
        int classIndex = Integer.valueOf(args[2]).intValue();
        int interv = Integer.valueOf(args[3]).intValue();
            
        DataBaseCases c = new DataBaseCases(f);
        
        if (args[1].compareTo("CV") == 0) { // Cross validation
             int k = Integer.valueOf(args[4]).intValue();
             double sum_accuracy=0.0;
             DataBaseCases dbTrain, dbTest;
            
             for (int i=0 ; i<k ; i++) { 
                System.out.println("ITERATION "+i);
               
                dbTrain = c.getTrainCV(i,k);
                dbTest = c.getTestCV(i,k);
                
                SelectiveNaiveMTEClassifier modelo= new SelectiveNaiveMTEClassifier(dbTrain, dbTest,classIndex,interv);              
                
                sum_accuracy+=modelo.accuracy;
            }  
            sum_accuracy /= k;
            System.out.println("Final accuracy");
            System.out.println(sum_accuracy);
            System.out.println("\n");
        }
        else
        {
            FileInputStream fTest = new FileInputStream(args[1]);
            DataBaseCases dbcTest = new DataBaseCases(fTest);
            SelectiveNaiveMTEClassifier classif = new SelectiveNaiveMTEClassifier(c,dbcTest, classIndex,interv);
            System.out.println("ACCURACY: " + classif.accuracy);
            classif.saveModelToFile("FINALSNB");
       }    
    }
}
