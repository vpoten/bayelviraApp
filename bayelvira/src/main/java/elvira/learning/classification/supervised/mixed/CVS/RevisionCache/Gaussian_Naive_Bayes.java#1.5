/*
 * Gaussian_Naive_Bayes.java
 *
 * Created on 1 de marzo de 2004, 10:17
 */

package elvira.learning.classification.supervised.mixed;


import elvira.Bnet;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.ContinuousConfiguration;
import elvira.FiniteStates;
import elvira.Continuous;
import elvira.Evidence;
import elvira.Link;
import elvira.LinkList;
import elvira.Node;
import elvira.potential.Potential;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.potential.PotentialTable;
import elvira.potential.ContinuousProbabilityTree;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.MixtExpDensity;
import elvira.inference.elimination.VariableElimination;
import elvira.learning.classification.AuxiliarPotentialTable;
import elvira.learning.classification.*;
import elvira.learning.classification.supervised.discrete.*;
import java.io.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.Enumeration;
import elvira.learning.classification.supervised.validation.AvancedConfusionMatrix;

/**
 * Gaussian_Naive_Bayes.java
 *
 * This is a public class to learn a naive-Bayes classification model with continuous and
 * discrete variables. 
 * The structural learning is carried out by Mixed_Naive_Bayes.
 * This class implements the parametric learning. It's assumed that the continuous
 * variables are Gaussian known the class of the class variable.
 * @author  andrew
 */
public class Gaussian_Naive_Bayes extends Mixed_Naive_Bayes {

  static final long serialVersionUID = -7556674477503837844L;    
  
  /** Creates a new instance of Gaussian_Naive_Bayes */
  public Gaussian_Naive_Bayes() {
  }
  /**
   * Constructor
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean correction. To apply the laplace correction
   */
  public Gaussian_Naive_Bayes(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
    super(data, lap);
  }

  public Gaussian_Naive_Bayes(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException{
    super(data, lap, classIndex);
    //this.type=0;
  }


  /**
   * This method makes the factorization of the classifier
   * When naive-Bayes the factorization is made by means of
   * the relative frecuencies given the class
   */
  public void parametricLearning(){
  
    double [][][] predistributions;
    double[] estprob;
    double sum,mean,deviation;
    int i,l,k,n;
    MixtExpDensity f;
    ContinuousProbabilityTree treepot, child;
    PotentialContinuousPT potential;
    NodeList list;
    Relation rel;

    Node node;


     predistributions = this.cases.getPreDistributionsNB(classVar);


     Enumeration enumerator=this.classifier.getNodeList().getNodes().elements();
     i=0;
     while(enumerator.hasMoreElements()){
         node=(Node)enumerator.nextElement();
	  if(!classVar.equals(node)){
	      treepot  = new ContinuousProbabilityTree(classVar) ;
               if(node instanceof FiniteStates){
                        n = ((FiniteStates) node).getNumStates();
			estprob = new double[n];
	                 for(l=0; l<this.classNumber;l++)
	                   { sum=0.0;
			    for(k=0;k<n;k++)
			       {estprob[k] = predistributions[i][l][k];
                                sum += estprob[k];}
		            if (this.laplace)
			       {    for(k=0;k<n;k++)
			                  {estprob[k] +=1 ;
                                           sum += n;}
					   }
			    for(k=0;k<n;k++)
			       {estprob[k] = estprob[k]/sum;}
                            child = new ContinuousProbabilityTree((FiniteStates) node, estprob);
                           treepot.setNewChild(child,l);
                          }
                }else if(node instanceof Continuous){
                        for(l=0; l<this.classNumber;l++){ 
                                     mean = predistributions[i][l][0];
                                     deviation = predistributions[i][l][1];
                                     sum = predistributions[i][l][2];
                                    //deviation = (deviation - (mean*mean)/sum)/(sum-1.0);
                                    mean = mean/sum;
                                    //deviation = deviation/sum - (mean*mean);
                                    deviation = (deviation/sum - (mean*mean))*sum/(sum-1);
                                    deviation=Math.sqrt(deviation);
                                    
                                    f = new MixtExpDensity( (Continuous) node,mean,deviation);
                                   child = new ContinuousProbabilityTree(f);
                                  treepot.setNewChild(child,l);
                       }
	       }else{
                System.out.println("Error in Mixed_Naive_Bayes parametricLearning():"+
                                   " Nodes must be FiniteStates or Continuous");
                System.exit(1);
               }
              list = new NodeList();
              list.insertNode(node);
              list.insertNode(classVar);
              potential = new PotentialContinuousPT(list,treepot);
              rel = new Relation();
              rel.setVariables(list);
              //potential.normalize();
              rel.setValues(potential);
              rel.setKind(Relation.CONDITIONAL_PROB);
              this.classifier.addRelation(rel);
              i++;
           }else{ //el nodo acual es classVar

              Vector      vector      = this.cases.getRelationList();
              Relation    relation    = (Relation)vector.elementAt(0);
              CaseListMem caselistmem = (CaseListMem)relation.getValues();

              AuxiliarPotentialTable auxPotentialTable = new AuxiliarPotentialTable((FiniteStates)classVar);
              //The table are initialized with random values of probability
              auxPotentialTable.initialize(0);
              
              for(l= 0; l< this.nCases; l++) {
                 auxPotentialTable.addCase((int)caselistmem.getValue(l, classifier.getNodeList().getId(classVar)), 0, 1);
              }

              if (this.laplace) 
                auxPotentialTable.applyLaplaceCorrection();
              
              PotentialTable potentialTable=new PotentialTable(classVar);
              potentialTable.setValues(auxPotentialTable.getPotentialTableCases());
               
               
              treepot  = new ContinuousProbabilityTree(classVar) ;
              
              list = new NodeList();
              list.insertNode(classVar);
              potential = new PotentialContinuousPT(potentialTable);
              rel = new Relation();
              rel.setVariables(list);
              //potential.normalize();
              rel.setValues(potential);
              rel.setKind(Relation.CONDITIONAL_PROB);
              this.classifier.addRelation(rel);
           }
   }
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
   * Main to use the class from the command line
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
          System.out.println(" -tt <FileTest.dbc> -> Train with file.dbc and test with FileTest.dbc");          
          System.out.println("                       If this option isn't showed, this test is not carried out.");          
          System.out.println(" -fs <saveELV> --> 0: The generate .elv file isn't saved (By default)");             
          System.out.println("                   1: The generate .elv file is saved.");             
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
              }else{
                  validation=2;
		  k=(Integer.valueOf(args[i+1])).intValue();
              }
          }else if (args[i].equals("-tt")){
              isTested=true;
              fileTest=args[i+1];

          }
      }//end for i
      

      

      Classifier nb = new Gaussian_Naive_Bayes(db, false,classnumber);	
      
      
      if (validation==0) {
	  
          Gaussian_Naive_Bayes clasificador = (Gaussian_Naive_Bayes)nb;
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
	ConfusionMatrix cm=validator.kFoldCrossValidation(k);

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
	  
          Gaussian_Naive_Bayes clasificador = (Gaussian_Naive_Bayes)nb;
	  clasificador.train();
	  System.out.println("Classifier learned");

	  FileInputStream ft = new FileInputStream(args[0]);
	  DataBaseCases   dt = new DataBaseCases(ft);
	  ft.close();

          double accuracy = clasificador.test(dt,classnumber);
	  System.out.println("Classifier tested. Train accuracy: " + accuracy);
	  if (UMBRAL==-1)
            ((AvancedConfusionMatrix)clasificador.getConfusionMatrix()).print();
          else
            ((AvancedConfusionMatrix)clasificador.getConfusionMatrix()).print(UMBRAL);
      
	  ft = new FileInputStream(fileTest);
	  dt = new DataBaseCases(ft);
	  ft.close();

          accuracy = clasificador.test(dt,classnumber);
	  System.out.println("Classifier tested with file: "+fileTest+"\n Test accuracy: " + accuracy);
	  if (UMBRAL==-1)
            ((AvancedConfusionMatrix)clasificador.getConfusionMatrix()).print();
          else
            ((AvancedConfusionMatrix)clasificador.getConfusionMatrix()).print(UMBRAL);
    }

    if (save==1) {
        
        DiscreteClassifier classifier=((DiscreteClassifier)nb);
	classifier.train();


	Bnet net=classifier.getClassifier();
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
        //FileWriter fo = new FileWriter("/home/andrew/classifier.elv");
        FileWriter fo = new FileWriter("c:\\tmp\\classifier.elv");
	net.saveBnet(fo);
	fo.close();
    }//end save

    
    
    
}
  
}
