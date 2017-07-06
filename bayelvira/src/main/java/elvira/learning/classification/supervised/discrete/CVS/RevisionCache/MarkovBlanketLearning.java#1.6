/* MarkovBlanketLearning.java */

package elvira.learning.classification.supervised.discrete;

import elvira.*;
import elvira.learning.Learning;
import elvira.learning.ParameterLearning;
import elvira.learning.DELearning;
import elvira.learning.LPLearning;
import elvira.learning.classification.*;

import elvira.potential.PotentialTable;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;


import java.io.*;
import java.util.Vector;

/*---------------------------------------------------------------*/
/**
 * MarkovBlanketLearning.java
 *
 * The MarkovBlanketLearning is an abstract class. It was designed to be the
 * parent of all the classifers based on the Markov Blanket in a Bayesian Network. This
 * class extends Learning class and implements the Classifier interface.
 *
 * @author J.G. Castellano (fjgc@decsai.ugr.es)
 * @since 12/02/2004
 * @version 0.1
 */

public abstract class MarkovBlanketLearning extends Learning implements SizeComparableClassifier {

    /** 
     * Data Base of Cases used in the learning process
     */    
    DataBaseCases input;

    /**
     * The number of the var that is the class
     */
    int classvar;

    /**
     * The laplace correction in the parameter learning process
     */
    boolean laplace;


    /*---------------------------------------------------------------*/
    /** Basic ctor.
     */
    public MarkovBlanketLearning(){
	setInput(null);
	setOutput(null);
	setIfAplyLaplaceCorrection(true);
	setVarToClassify(0);
    }//end basic ctor.


    /*---------------------------------------------------------------*/
    /** Constructor
     * @param DataBaseCases cases. The data bases of discrete cases.
     * @param classvar. number of the variable to classify
     * @param boolean lap. To apply the laplace correction in the pa
     *                rameter learning process.
     */
    public MarkovBlanketLearning(DataBaseCases cases, int classvar, boolean lap) {
	//Store the cases, the classvar and laplace correction
	setVarToClassify(classvar);
	setInput(cases);
	setIfAplyLaplaceCorrection(lap);
    } //end ctor with databasecases

    /*---------------------------------------------------------------*/

    /**
     * This method carries out the learning proccess.
     */

    abstract public void learning();

    /*---------------------------------------------------------------*/

    /* Access methods */

    /*---------------------------------------------------------------*/
    /** 
     * Initializes/store the Data Base of Cases used in the learning process
     * @param DataBaseCases dbc. The data bases of discrete cases.
     */    
    public void setInput(DataBaseCases dbc){
	this.input = dbc;
    }
    /*---------------------------------------------------------------*/
    /** 
     * Return the stored Data Base of Cases used in the learning process
     * @return the data bases of discrete cases.
     */    
    public DataBaseCases getInput(){
	return this.input;
    }
    /*---------------------------------------------------------------*/
    /** 
     * Initializes/store the number of the variable to classify
     * @param classnumbee the var to classify 
     */    
    public void setVarToClassify(int classnumber){
	this.classvar = classnumber;
    }
    /*---------------------------------------------------------------*/
    /** 
     * Return the number of the variable to classify
     * @return the var to classify
     */    
    public int getVarToClassify(){
	return this.classvar;
    }
    /*---------------------------------------------------------------*/
    /** 
     * Set if aply the laplace correction in the parameter learning
     * @param lap if aply the laplace correction 
     */    
    public void setIfAplyLaplaceCorrection(boolean lap){
	this.laplace = lap;
    }
    /*---------------------------------------------------------------*/
    /** 
     * Return if aply the laplace correction in the parameter learning
     * @return if aply the laplace correction
     */    
    public boolean getIfAplyLaplaceCorrection(){
	return this.laplace;
    }

    /*---------------------------------------------------------------*/


    /*---------------------------------------------------------------*/

    /* Classifier interface methods */

    /*---------------------------------------------------------------*/

    /** This method is used to build the Markov Blanket classifier 
     *	@param training training set to build the classifier
     *	@param classnumber number of the variable to classify
     */
    public void learn (DataBaseCases training, int classnumber) {

	//update the properties
	setInput(training);
	setVarToClassify(classnumber);

	//learn the structure of the bnet
	learning();

	//Learn the parameters of the bnet
	ParameterLearning outputNet2;
	if (this.laplace) {
	    outputNet2 = new LPLearning(training,getOutput());
	    outputNet2.learning();
	} else {
	    outputNet2 = new DELearning(training,getOutput());
	    outputNet2.learning();
	}

	//we store the bnet learned (structure+params)
	setOutput(outputNet2.getOutput());

    }//end learn method
    /*---------------------------------------------------------------*/
    /** This method is used to classify a instance
     *  @param instance case to classify
     *	@param classnumber number of the variable to classify
     *	@return a Vector with a probability associated to each class
    */

  public Vector classify (Configuration instance, int classnumber) {
    int nVariables=getInput().getNodeList().size();
    int[] caseTest = new int[nVariables];
    Vector values  = instance.getValues();
    for(int i= 0; i< nVariables; i++)
      caseTest[i] = ((Integer)values.elementAt(i)).intValue();

    Vector probabilities = new Vector();

    Node classNode                     = getOutput().getNodeList().elementAt(classnumber);

    Relation classRelation             = getOutput().getRelation(classNode);
    PotentialTable classPotentialTable = (PotentialTable)classRelation.getValues();
    int numClasses=((FiniteStates)classNode).getNumStates();
    NodeList parentsClass = classNode.getParentNodes();


    for(int c= 0; c< numClasses; c++) {
      Vector vParentsClass  = new Vector();
      Vector vParentsClassValues = new Vector();

      vParentsClass.addElement(classNode);
      vParentsClassValues.addElement(new Integer(c));
      for(int p= 0; p< parentsClass.size();p++) {
        vParentsClass.addElement(parentsClass.elementAt(p));
        vParentsClassValues.addElement(new Integer((int)(caseTest[getOutput().getNodeList().getId((Node)parentsClass.elementAt(p))])));
       }
      Configuration parentClassConfiguration = new Configuration(vParentsClass, vParentsClassValues);

      double currentProb     = classPotentialTable.getValue(parentClassConfiguration);

      NodeList childrenNodes = classNode.getChildrenNodes();
      for(int i= 0; i< childrenNodes.size() ; i++) {
        FiniteStates currentNode      = (FiniteStates)childrenNodes.elementAt(i);
        Relation relation             = getOutput().getRelation(currentNode);
        PotentialTable potentialTable = (PotentialTable)relation.getValues();

        NodeList parentsNodes = currentNode.getParentNodes();
        Vector vParentsNodes  = new Vector();
        Vector vParentsValues = new Vector();

        vParentsNodes.addElement(currentNode);
        vParentsValues.addElement(new Integer((int)(caseTest[getInput().getNodeList().getId(currentNode)])));
        for(int p= 0; p< parentsNodes.size();p++)
          if(! parentsNodes.elementAt(p).equals(classNode)) {
            vParentsNodes.addElement(parentsNodes.elementAt(p));
            vParentsValues.addElement(new Integer((int)(caseTest[getOutput().getNodeList().getId((Node)parentsNodes.elementAt(p))])));
          }
        vParentsNodes.addElement(classNode);
        vParentsValues.addElement(new Integer(c));

        Configuration parentConfiguration = new Configuration(vParentsNodes, vParentsValues);
        currentProb = currentProb * potentialTable.getValue(parentConfiguration);
      }
      probabilities.addElement(new Double(currentProb));
    }
    return(probabilities);
  }
    /*---------------------------------------------------------------*/
    /**
    * For test and compare bnets
    */
   public static void main(String args[]) throws ParseException, IOException { 

      //Look the arguments for the test
      if(args.length < 3){
	  System.out.println("too few arguments: Usage: input.elv input.dbc class [file.elv]");
	  System.out.println("\tinput.elv : Bayesian network to test and compare");
	  System.out.println("\tinput.dbc : DataBaseCases file for test the bnet");
          System.out.println("\tclass : The number of the variable to classify if it's the first use 0.");
          System.out.println("\tfile.elv: Optional. True net to be compared.");
	  System.exit(0);
      }


      //read the Bnet
      FileInputStream f = new FileInputStream(args[0]);
      Bnet bnet = new Bnet(f);
      f.close();

      //read the cases
      f = new FileInputStream(args[1]);
      DataBaseCases cases = new DataBaseCases(f);
      f.close();

      //get the class
      int classnumber = (new Integer(args[2])).intValue();

      //Buil the markov blanket class to validate the bnet
      MarkovBlanketLearning  bnetclassifier = new MarkovBlanketLearning () {
	      public void learning () {}
	      public void saveModelToFile(java.io.File of){}
	  };
      
      //Set the bnet classifier
      bnetclassifier.setInput(cases);
      bnetclassifier.setOutput(bnet);
      bnetclassifier.setVarToClassify(classnumber);

      //Test The classifier
      ClassifierValidator validator= new ClassifierValidator((Classifier)bnetclassifier,cases,classnumber);
      ConfusionMatrix testerror=validator.confusionMatrix(bnetclassifier,cases,classnumber);

      //Print the test error
      System.out.println("");
      testerror.print();
      System.out.println("Accuracy="+ testerror.getAccuracy()+"% ");//+('\u00B1')+" "+testerror.getAccuracyStandardDeviation());
      System.out.println(""); 
      

      //compare the learned bnet with the optimal bnet
      if(args.length > 3) {
	  //read the optimal net
	  FileInputStream fnet = new FileInputStream(args[3]);
	  Bnet net = new Bnet(fnet);
	  fnet.close();

	  //compare and show divergences
          double d  = cases.getDivergenceKL(bnet);
          double d2 = cases.getDivergenceKL(net);
          System.out.println("kL Divergence for input net: "+d2);
          System.out.println("kL Divergence for optimal net: "+d2);
          System.out.println("Divergence between optimal and learned nets: "+(d2-d));

	  //show the links differences
	  LinkList addel[] = new LinkList[3];
	  addel = bnetclassifier.compareOutput(net);
	  System.out.println("\nAdded links: "+addel[0].size());
	  System.out.print(addel[0].toString());
	  System.out.println("\nRemoved links: "+addel[1].size());
	  System.out.print(addel[1].toString());
	  System.out.println("\nInverted links: "+addel[2].size());
	  System.out.print(addel[2].toString());
	  //System.out.print("\nUnoriented links: ");
	  //System.out.print(outputNet2.linkUnOriented().toString());
      }//end if

   } //end main method
   
   public long size(){
	   return this.getOutput().getNumberOfFreeParameters();
   }
   
   public void saveModelToFile(String ap) throws java.io.IOException {
	   java.io.FileWriter fw = new java.io.FileWriter(ap+"markovBlanketLearning.elv");
	   this.getOutput().saveBnet(fw);
	   fw.close();   
   }
   
} // end Markov Blanket class
