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
 * Class <code>NaiveMTEClassifier</code> 
 *  
 * @author Antonio Fernandez Alvarez (afalvarez@ual.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 * 
 * @since 15/01/2008
 */

public class NaiveMTEClassifier extends MixedClassifier {
    
    //number of intervals into which the domain of the continuous variables will be split
    int intervals;
    
    //The other variables are inherited from MixedClassifier
    
    /**
     * Constructor empty of a NaiveMTEClassifier class.
     */
    public NaiveMTEClassifier(){
        
    }
    
     /**
     * Constructor of a NaiveMTEClassifier class.
     *
     * @param dbctrain the DataBaseCases from which the predictor will be constructed.
     * @param classIndex an integer value indicating the index (column) of the class variable in the database. 
     * The first column is labeled as 0.
     * @param intervals the number of intervals into which the domain of the continuous
     * variables will be split.
     */
    public NaiveMTEClassifier(DataBaseCases dbtrain, int classindex, int intervals) 
    throws elvira.InvalidEditException{
    
        super(dbtrain, true, classindex);
        this.intervals=intervals; 
    }
    
    /**
     * 
     * Creates a directed link between the class variable and each feature variable.
     * The "net" variable is updated
     *
     */
    private void CreateDirectedLinkClassFeatures(){
                
       for (int i=0;i<nVariables;i++)
         if (i!=this.classIndex)
             try {
                 classifier.createLink(classifier.getNodeList().elementAt(classIndex), classifier.getNodeList().elementAt(i), true);
             }
             catch (Exception e) { System.out.println("Problems to create the link");}          
    }

    /**
     * Compute the structural learning of a NB structure stored in variable 
     * "classifier".
     */    
    public void structuralLearning() {
           
        Vector<Node> vNodes = cases.getNewVectorOfNodes();       
        classifier = new Bnet();
        NodeList nl = new NodeList();
        nl.setNodes(vNodes);
        classifier.setNodeList(nl);    
        CreateDirectedLinkClassFeatures();
    }
 
    /**
     * Compute the parametric learning of a Naive structure stored in variable 
     * "classifier"
     */ 
    public void parametricLearning() {
        
        MTELearning learningObject;
        ContinuousProbabilityTree t;
        PotentialContinuousPT pot;
        Relation rel;
        Vector netRL;
        NodeList parent, relationVariables;        
        Node  childVar;
       
        learningObject= new MTELearning(cases);  
        
        netRL=new Vector();
       
        System.out.println("\n\n====> Learning NB <=======");
       
        for (int i=0;i<nVariables;i++){
         if (i!=classIndex){
            
            childVar=classifier.getNodeList().elementAt(i);
            parent=new NodeList();
            parent.insertNode(classVar);
          
            System.out.println("   Learning " + childVar.getName()+" ...");
            t=learningObject.learnConditional(childVar, parent, cases, intervals, 4);
            
            relationVariables=new NodeList();
            relationVariables.insertNode(childVar);
            relationVariables.insertNode(classVar);
          
            pot=new PotentialContinuousPT(relationVariables,t);
            rel=new Relation();
            rel.setVariables(relationVariables);
            rel.setValues(pot); 
            netRL.addElement(rel);
        }
       }
       
       System.out.println("   Learning " + classVar.getName()+" ... (CLASS VARIABLE)");
       t=learningObject.learnConditional(classVar, new NodeList(), cases, intervals, 4);
       
       relationVariables=new NodeList();
       relationVariables.insertNode(classVar); 
       pot=new PotentialContinuousPT(relationVariables,t);
       rel=new Relation();
       rel.setVariables(relationVariables);    
       rel.setValues(pot); 
       netRL.addElement(rel);
       classifier.setRelationList(netRL);
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
        
        FileInputStream fTrain = new FileInputStream(args[0]);
        int classIndex = Integer.valueOf(args[2]).intValue();
        int interv = Integer.valueOf(args[3]).intValue();
            
        DataBaseCases dbcTrain = new DataBaseCases(fTrain);
        
        NaiveMTEClassifier classif = new NaiveMTEClassifier(dbcTrain,classIndex,interv);
        
        classif.train();

        classif.saveModelToFile("NBfinal");
        
        if (args[1].compareTo("CV") == 0) { //k-folds cross validation
            
            int k = Integer.valueOf(args[4]).intValue();
            ClassifierValidator validator=new ClassifierValidator(classif, dbcTrain, classIndex);
            ConfusionMatrix cm=validator.kFoldCrossValidation(k);
            System.out.println(k +"-folds Cross-Validation. Accuracy="+(1.0-cm.getError())+"\n\n");
        }
        else //Specific test set
        {
            FileInputStream fTest = new FileInputStream(args[1]);
            DataBaseCases dbcTest=new DataBaseCases(fTest);
            double accuracy = classif.test(dbcTest,classIndex);
            System.out.println("Classifier tested. Train accuracy: " + accuracy);   
        }
    }     
}    










