/* MTE_Naive_Bayes.java */

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

import elvira.learning.classification.Classifier;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.ConfusionMatrix;

import java.io.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.Enumeration;

/**
 * MTE_Naive_Bayes.java
 *
 * This is a public class to learn a naive-Bayes classification model with continuous and
 * discrete variables. The continuous variables are modelized with mixture of exponentials.
 *
 * @author  J.G. Castellano fjgc@desai.ugr.es Univ. Granada.
 *
 * @version 0.1
 * @since 19/02/2004
 */

public class MTE_Naive_Bayes extends Mixed_Naive_Bayes {

    /** The proportion of the cases that will be used like number of intervals 
      * of the empiric density	when we are estimating a mixture of exponencials
      */
   double empiricIntFactor;

    /** The proportion of the cases that will be used linke number of intervals 
      * as maximum when splitting the domain. 	when we are estimating a 
      * mixture of exponencials
      */
   double intervalsFactor;

    /** Number of points used for each line in the derivative method 
      * when we are estimating a mixture of exponencials
      */
   int numpoints;

  /*---------------------------------------------------------------*/
  /**
   * Basic Constructor
   */
   public MTE_Naive_Bayes() {
    empiricIntFactor=0.05;
    intervalsFactor=0.5;
    numpoints=3;
   }

  /*---------------------------------------------------------------*/
  /**
   * Constructor
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean correction. To apply the laplace correction
   */
  public MTE_Naive_Bayes(DataBaseCases data, boolean lap) throws elvira.InvalidEditException{
    super(data, lap);
    empiricIntFactor=0.005;
    intervalsFactor=0.5;
    numpoints=3;
  }

  /*---------------------------------------------------------------*/
  /**
   * Constructor
   * @param DataBaseCases. cases. The input to learn a classifier
   * @param boolean correction. To apply the laplace correction
   * @param classIndex number of the variable to classify
   */
  public MTE_Naive_Bayes(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException{
    super(data, lap, classIndex);
    empiricIntFactor=0.05;
    intervalsFactor=0.5;
    numpoints=3;
  }
  /*---------------------------------------------------------------*/
  /**
   * This methos set the params used by the algorithm in the 
   * estimating of the mixtures of exponencials.
   * @param ef The proportion of the cases that will be used like number of intervals 
   *            of the empiric density when we are estimating a mixture of exponencials
   * @param inf The proportion of the cases that will be used linke number of intervals 
   *            as maximum when splitting the domain when we are estimating a 
   *            mixture of exponencials
   * @param np Number of points used for each line in the derivative method 
   *           when we are estimating a mixture of exponencials
   */
  
  public void setParams(double ef, double inf,int np){
    this.empiricIntFactor=ef;
    this.intervalsFactor=inf;
    this.numpoints=np;
  }
  /*---------------------------------------------------------------*/
  /**
   * This method sort the cases for every variable using the differents
   * values of the class.
   * @return a bidmensional array of Vectors where the first index correspond 
   * with the different cases of the parameter varClass (FiniteStates), and the 
   * second index correspond to each one of the variables of this 
   * DataBaseCases (except the variable varClass); the Vectors has the cases for 
   * this  variable that have the same value of the varClass.
   */
  public Vector[][] sortByTheClass(){
      int i,j;
      CaseListMem caselist=(CaseListMem)cases.getCases();

      //TODO:This only work if the last variable is the class (and it's FiniteStates, of course)

      //Build the bidimensional array of Vectors
      Vector casessort[][]=new Vector[this.classNumber][this.nVariables-1];
      for (i=0;i<this.classNumber;i++)
	  for (j=0;j<this.nVariables-1;j++)
	  casessort[i][j]=new Vector();

      
      //look all the cases
      int classIndex=this.cases.getNodeList().getId(this.classVar);
      for(i=0;i<this.nCases;i++){  
	  //get the class for this case
	  int classValue=(int)caselist.getValue(i,classIndex);
	  for (j=0;j<this.nVariables;j++) if (j!=classIndex) {
	      //Add the value to the appropiate vector, in function of the class ante var
	      Double valor=new Double(caselist.getValue(i,j));
	      if (j>classIndex)
		  casessort[classValue][j-1].add(valor);
	      else
		  casessort[classValue][j].add(valor);
	  }//end for j
      }//end for i
      
      return casessort;

  }//end sortByTheClass method
  /*---------------------------------------------------------------*/
  /**
   * This method makes the factorization of the classifier
   * When naive-Bayes the factorization is made by means of
   * the relative frecuencies given the class
   */

  public void parametricLearning(){
     Vector [][] casessort;
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
     casessort=sortByTheClass();

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
			  }
	       else if(node instanceof Continuous){
		   for(l=0; l<this.classNumber;l++) { 
		       mean = predistributions[i][l][0];
		       deviation = predistributions[i][l][1];
		       sum = predistributions[i][l][2];
		       mean = mean/sum;
		       deviation = (deviation/sum - (mean*mean))*sum/(sum-1);
		       deviation=Math.sqrt(deviation);

		       /*//DEBUG vemos la muestra
		       System.out.println("\nMuestra de "+casessort[l][i].size());
		       for (int ii=0;ii<casessort[l][i].size();ii++) {
			   System.out.print((casessort[l][i]).elementAt(ii)+" ");
			   }
		       //DEBUG vemos datos de la variable
		       System.out.println("Variable ->"+node.getName()+" media="+mean+" desviación="+deviation+
					      " sum="+sum+" min="+((Continuous)node).getMin()+" max="+
					      ((Continuous)node).getMax());*/

		       if (casessort[l][i].size()>=3) {
			   //The empiric intervals will be a % of the sample    
			   int empiricInt=(int)Math.round(casessort[l][i].size()*this.empiricIntFactor);
			   //The max intervals will be a % of the sample   
			   int intervals=(int)Math.round(casessort[l][i].size()*this.intervalsFactor);

			   //empiricInt and intervals must be greater than 2
			   if (empiricInt < 3) empiricInt=3;
			   if (intervals <3) intervals=3;
			   
			   //Estimate the MTE
			   boolean reduce;
			   do {
			       reduce=false;
			       /*//DEBUG vemos  parametros			   
				 System.out.println("\nempiricInt="+empiricInt+" intervals="+intervals+" numpoints="+numpoints);*/
			       
			       child = ContinuousProbabilityTree.learnUnivariate((Continuous)node,
						   casessort[l][i],intervals,numpoints); 

			       //Check if all teh values of the interval are the same 
			       for (int ii=0;ii < child.getNumberOfChildren() && (!reduce) ;ii++) {
				   Double independent=new Double(child.getChild(ii).getProb().getIndependent());
				   if ( independent.isNaN())  {
				       reduce=true;
				       //decrementing the intervals
				       empiricInt--;
				   }
			       }//end for i
			       
			       if (empiricInt<3) {
				   reduce=false;
				   System.out.println("Warning:Repetition of the same value. Using Gaussian instead MTE");
				   f = new MixtExpDensity( (Continuous) node,mean,deviation);
				   child = new ContinuousProbabilityTree(f);
			       }
			   } while (reduce);
			   
			   //add the estimated MTE
			   treepot.setNewChild(child,l);

		       } else {
			   System.out.println("Warning: Few values. Using Gaussian instead MTE");
			   f = new MixtExpDensity( (Continuous) node,mean,deviation);
			   child = new ContinuousProbabilityTree(f);
		       } 
		   }//end for l
	       }else{
                System.out.println("Error in MTE_Naive_Bayes parametricLearning():"+
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
           }else{ //the actual node is classVar

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
     }//end while

  }//end parameterLearning method
  /*---------------------------------------------------------------*/
  /**
   * Main to use the class from the command line
   */
  public static void main(String[] args) throws FileNotFoundException, IOException, elvira.InvalidEditException, elvira.parser.ParseException, Exception{
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
              }else {
                  validation=2;
		  k=(Integer.valueOf(args[i+1])).intValue();
	      }
          }
      }//end for i
      

      
      MTE_Naive_Bayes clasificador;
      if (validation==0) {
	  clasificador = new MTE_Naive_Bayes(db, false,classnumber);	
	  
	  clasificador.train();
	  System.out.println("Classifier learned");

	  FileInputStream ft = new FileInputStream(args[0]);
	  DataBaseCases   dt = new DataBaseCases(ft);
	  ft.close();

	  double accuracy = clasificador.test(dt,classnumber);
	  System.out.println("Classifier tested. Train accuracy: " + accuracy);
	  clasificador.getConfusionMatrix().print();
      } else if (validation==1) {
	  Classifier nb;
	  nb=(Classifier)new MTE_Naive_Bayes(db,false,classnumber);
	
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
    } else {
	Classifier nb;
	nb=(Classifier)new MTE_Naive_Bayes(db,false,classnumber);
 
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
    }

    if (save==1) {
	clasificador = new MTE_Naive_Bayes(db, false,classnumber);
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

  }//end main

}//End of MTE_Naive_Bayes class

