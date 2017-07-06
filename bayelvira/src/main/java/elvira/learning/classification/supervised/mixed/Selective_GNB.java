/*
 * Selective_GNB.java
 *
 * Created on 10 de mayo de 2004, 13:34
 */

package elvira.learning.classification.supervised.mixed;

import elvira.*;
import elvira.database.DataBaseCases;
import elvira.potential.*;
import elvira.inference.elimination.VariableElimination;
import elvira.learning.classification.*;
import elvira.learning.classification.supervised.mixed.*;
import elvira.learning.classification.supervised.discrete.*;
import java.io.*;
import java.util.*;
import java.text.*;
import elvira.learning.classification.supervised.validation.AvancedConfusionMatrix;


/**
 * This class implements a gaussian naive bayes classifier with feature selection. So this class:
 *      -Implements the Selective_Classifer interface
 *      -Overwrites the following methods:
 *            - structuralLearning, now this method invokes to selectiveStructuralLearning method.
 *            - evaluationKFK, evaluationLOO and getNewClassifer, see the method's comment for details.
 * @author  andrew
 */
public class Selective_GNB extends Selective_MixedNB implements Selective_Classifier {
    
    static final long serialVersionUID = -1613707090090313962L;    


    /** Creates a new instance of Selective_GNB */
    public Selective_GNB() {
    }

    public Selective_GNB(DataBaseCases data, boolean lap, int classIndex2, Vector order2) throws elvira.InvalidEditException{
        super(data, lap, classIndex2);
        order=(Vector)order2.clone(); 
    }
    
    public Selective_GNB(DataBaseCases data, boolean lap, int classIndex2) throws elvira.InvalidEditException{
        super(data, lap, classIndex2);
        
    }

    public MixedClassifier getNewClassifier(DataBaseCases cases){
        try{    
        Gaussian_Naive_Bayes gn =new Gaussian_Naive_Bayes(cases, this.laplace,cases.getVariables().getId((Node)this.classVar));
        gn.train();
        return gn;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
     
      double UMBRAL=-1;
        
        //Check the args
      if (args.length < 1){
	  System.out.println("ERROR:Too few arguments.");
          System.out.println("Use: file.dbc [Options]");
          System.out.println("OPTIONS: ");
          System.out.println(" -cn <classnumber> -->  The number of the variable to classify. If it's the first use 0. By default: the last variable is set");
          System.out.println(" -va <validation> --> Options:");   
          System.out.println("                                   0 -> No validation. Show the train error (by default).");
          System.out.println("                                   t -> Train And Test.");
          System.out.println("                                   k (number)-> k-fold croos-validation.");          
          System.out.println("                                   l -> leave-one-out validation.");
          System.out.println(" -ft <fileTest.dbc> --> If validation option is '0', 'k' or 'l' the training is carried out by the previous methods and the testing with FileTest.dbc");          
          System.out.println("                        Note: The maximum probability configuration is calculated before the test of the classifier");
          System.out.println("                        If this option isn't showed, this test is not carried out.");          
          System.out.println(" -fs <saveELV> --> 0: The generate .elv file isn't saved (By default)");             
          System.out.println("                   1: The generate .elv file is saved.");             
          System.out.println(" -fo <OrderFile> --> file with the variable selection order. The line N of this file contain an integer with the index of the N-th variable");             
          System.out.println("                              If this option isn't showed, the variable selection order isn't considered");                    
          System.exit(0);
      }

      //read the .dbc
      FileInputStream fi = new FileInputStream(args[0]);
      DataBaseCases   db = new DataBaseCases(fi);
      fi.close();

      

      //Get the classnumber and the savesets option
      int classnumber=db.getVariables().size()-1;
      int save=0;
      int validation=0;
      int k=10;
      boolean isTested=false;
      String fileTest=new String();
      boolean isOrdered=false;
      String fileOrder=new String();      
      for (int i=1; i<args.length; i++){
          if (args[i].equals("-cn")){
              classnumber=(Integer.valueOf(args[i+1])).intValue();
          }else if (args[i].equals("-fs")){
              save=(Integer.valueOf(args[i+1])).intValue();
          }else if (args[i].equals("-va")){
              if (args[i+1].equals("0")){
                  validation=0;
              }else if (args[i+1].equals("t")){
                  validation=1;
              }else if (args[i+1].equals("l")){
                  validation=3;
              }else {
                  validation=2;
		  k=(Integer.valueOf(args[i+1])).intValue();
              }
          }else if (args[i].equals("-ft")){
                  isTested=true;
                  fileTest=args[i+1];
          }else if (args[i].equals("-fo")){
                  isOrdered=true;
                  fileOrder=args[i+1];
          }

      }//end for i
      
      Classifier nb=null;
      if (!isOrdered){
          nb = new Selective_GNB(db, false,classnumber);	
      }else{
            Vector order=new Vector();
            try {
                FileReader fr = new FileReader(args[2]);
                BufferedReader entrada = new BufferedReader(fr);
                String s;
                while((s = entrada.readLine()) != null)
                    order.addElement(new Integer(s));
                entrada.close();
            }
            catch(java.io.FileNotFoundException fnfex) {
                System.out.println("Archivo no encontrado: " + fnfex);
            }
            nb = new Selective_GNB(db, false,classnumber,order);	          
      }
      
      
      if (validation==0) {
	  
          Selective_GNB clasificador = (Selective_GNB)nb;
	  clasificador.train();
	  System.out.println("Classifier learned");

	  FileInputStream ft = new FileInputStream(args[0]);
	  DataBaseCases   dt = new DataBaseCases(ft);
	  ft.close();

	  dt.projection(clasificador.getDataBaseCases().getVariables());
          double accuracy = clasificador.test(dt,classnumber);
	  System.out.println("Classifier tested. Train accuracy: " + accuracy);
	  if (UMBRAL==-1)
            ((AvancedConfusionMatrix)clasificador.getConfusionMatrix()).print();
          else
            ((AvancedConfusionMatrix)clasificador.getConfusionMatrix()).print(UMBRAL);
          
      } else if (validation==1) {
	  //Run the validator
	  ClassifierValidator validator=new ClassifierValidator(nb, db, classnumber);
	  Vector resultv=validator.trainAndTest();
	  ConfusionMatrix cm=(ConfusionMatrix)resultv.elementAt(0);
      
	  //Prints the confusion matrix and error
	  System.out.println("-----------------------------------------------------------");
	  System.out.println("Training confusion matrix");
	  cm.print();
	  System.out.println("Trainig accuracy="+((1.0-cm.getError())*100.0)+" error:"+cm.getError()+" Variance:"+cm.getVariance());
	  cm=(ConfusionMatrix)resultv.elementAt(1);
	  System.out.println("\nTest confusion matrix");
	  cm.print();
	  System.out.println("Test accuracy="+((1.0-cm.getError())*100.0)+" error:"+cm.getError()+" Variance:"+cm.getVariance());
	  System.out.println("-----------------------------------------------------------");
    } else if (validation==2) {
 
	//Run the validator
	ClassifierValidator validator=new ClassifierValidator(nb, db, classnumber);
	ConfusionMatrix cm=null;
        for (int i=0; i<1; i++)
            cm=validator.kFoldCrossValidation(k);

	//Prints the confusion matrix and error
	System.out.println("K-folds Cross-Validation (k="+k+") confusion matrix");
	cm.print();
	System.out.println("K-folds Cross-Validation (k="+k+") variance confusion matrix");
	cm.printVariance();
	System.out.println("K-folds Cross-Validation (k="+k+") accuracy="+((1.0-cm.getError())*100.0)+" error:"+cm.getError()+" Variance:"+cm.getVariance()+"\n\n");
	System.out.println("-----------------------------------------------------------");
    } else if (validation==3) {
    
	//Run the validator
	ClassifierValidator validator=new ClassifierValidator(nb, db, classnumber);
	ConfusionMatrix cm=validator.leaveOneOut();

        
        //Prints the confusion matrix and error
        System.out.println("Leave One Out confusion matrix");
        cm.print();
        System.out.println("Leave One Out variance confusion matrix");
        cm.printVariance();
        System.out.println("Leave One Out accuracy="+((1.0-cm.getError())*100.0)+" error:"+cm.getError()+" Variance:"+cm.getVariance()+"\n\n");
    
    }
    
    if (isTested) {
          Selective_GNB clasificador = (Selective_GNB)nb;	  
          clasificador.setMaximumProbabilityConfiguration(db);

          FileInputStream ft = new FileInputStream(args[0]);
	  DataBaseCases   dt = new DataBaseCases(ft);
	  ft.close();

	  dt.projection(clasificador.getDataBaseCases().getVariables());
          
          double accuracy = clasificador.test(dt,dt.getVariables().getId(clasificador.getClassVar()));
	  System.out.println("Classifier tested. Train accuracy: " + accuracy);
	  if (UMBRAL==-1)
            ((AvancedConfusionMatrix)clasificador.getConfusionMatrix()).print();
          else
            ((AvancedConfusionMatrix)clasificador.getConfusionMatrix()).print(UMBRAL);
      
	  ft = new FileInputStream(fileTest);
	  dt = new DataBaseCases(ft);
	  ft.close();

	  dt.projection(clasificador.getDataBaseCases().getVariables());
          accuracy = clasificador.test(dt,dt.getVariables().getId(clasificador.getClassVar()));
	  System.out.println("Classifier tested with file: "+fileTest+"\n Test accuracy: " + accuracy);
	  if (UMBRAL==-1)
            ((AvancedConfusionMatrix)clasificador.getConfusionMatrix()).print();
          else
            ((AvancedConfusionMatrix)clasificador.getConfusionMatrix()).print(UMBRAL);
    }

    if (save==1) {
        Selective_GNB clasificador = (Selective_GNB)nb;            
	clasificador.train();


	Bnet net=clasificador.getClassifier();
	int width = 800;
	int height = 600;
	//Positioning the class node
	Node nodeClass=net.getNodeList().elementAt(classnumber);
	net.getNodeList().elementAt(classnumber).setPosX(width/2);
	net.getNodeList().elementAt(classnumber).setPosY(height/8);
	
	//Positioning the rest nodes
	int nodesRow = width/128;
	int numRows = (net.getNodeList().size()-1)/nodesRow;
	if (((net.getNodeList().size()-1) % nodesRow) != 0) numRows++;
	
	for (int i=0; i < numRows-1; i++) {
	    for (int j=0; j < nodesRow; j++) {
		if (! net.getNodeList().elementAt((i*nodesRow)+j).equals(nodeClass)) {
		    net.getNodeList().elementAt((i*nodesRow)+j).setPosY((height/2)+i*(height/(2*numRows)));
		    net.getNodeList().elementAt((i*nodesRow)+j).setPosX((128*j)+100);
		}
	    }
	}
	//Nodes that cannot have been positioned until now
	int j=0;
	for (k=(numRows-1)*nodesRow; k < net.getNodeList().size(); k++) {
	    if (! net.getNodeList().elementAt(k).equals(nodeClass)) {
		net.getNodeList().elementAt(k).setPosY((height/2)+(numRows-1)*(height/(2*numRows)));
		net.getNodeList().elementAt(k).setPosX((128*j)+100);
		j++;
	    }
	}
	System.out.println("Saving \"classifier.elv\"");
	FileWriter fo = new FileWriter("classifier.elv");
	net.saveBnet(fo);
	fo.close();
    }//end save

    
}

}
