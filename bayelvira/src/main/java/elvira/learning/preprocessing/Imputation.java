/* Imputation.java */
package elvira.learning.preprocessing;

import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.inference.abduction.*;
import elvira.potential.*;
import elvira.learning.preprocessing.*;
import elvira.learning.classificationtree.ClassificationTree;

import java.io.*;
import java.lang.Runtime;

import java.util.Random;
import java.util.Vector;

import elvira.parser.ParseException;

/**
 * Implements several imputation methods for missing values.
 * For example:
 *<pre>
 *       Imputation imputation = new Imputation(Imputation.AVERAGEIMPUTATION);
 *       imputation.apply(dataBaseCases);
 *
 *</pre>
 * Another example:
 *<pre>
 *       Imputation imputation2 = new Imputation(Imputation.CLASSIFICATIONTREEIMPUTATION, ClassificationTree.C45));
 *       imputation2.apply();
 *
 *</pre>
 * And other:
 *<pre>
 *       Imputation imputation3 = new Imputation(Imputation.);
 *       imputation3.averageImputation(dataBaseCases3);
 *
 *</pre>
 *
 * Note: For very large databases and the classification tree imputation method will produce a OutOfMemory Exception,
 *       to solve this issue, you can use the java interpreter with more memory. For example:
 *<pre>
 *       java -Xms512m -Xmx512m elvira/learning/preprocessing/Imputation
 *</pre>
 *
 * @author fjgc@decsai.ugr.es
 * @author avrofe@ual.es
 * @version 0.1
 * @since 13/4/2004
 */

public class Imputation {
    
    /* These constants are used to set the imputation method */
    public static final int ZEROIMPUTATION= 0;
    public static final int AVERAGEIMPUTATION = 1;
    public static final int CLASSIFICATIONTREEIMPUTATION = 2;
    public static final int CLASSCONDITIONEDMEANIMPUTATION = 3;
    public static final int ITER_MPEIMPUTATION = 4;
    public static final int INCR_MPEIMPUTATION = 5;
    public static final int REMOVEMISSING = 6;
    
    
    /**
     * Imputation method to use.
     */
    int imputationMethod;
    
    /**
     * Imputation submethod to use
     */
    int imputationSubMethod=0;
    
    //int =0;
    
/*---------------------------------------------------------------*/
/**
 *  Basic Constructor
 */
    
    public Imputation() {
	imputationMethod=AVERAGEIMPUTATION;
	imputationSubMethod=-1;
	//numIteraciones = -1;
    }
    
    
    /*---------------------------------------------------------------*/
    /**
     *  Constructor
     *
     *  @param method constant with the imputation method to use
     */
    
    public Imputation(int method) {
	imputationMethod=method;
	imputationSubMethod=-1;
	//numIteraciones = -1;
    }
    
    
    /*---------------------------------------------------------------*/
    /**
     *  Constructor with imputation method and submethod
     *
     *  @param method constant with the imputation method to use
     *  @param submethod constant submethod to imputation use. In the
     *                classConditionesMeanImputation methos it's the class
     *                number, that is, the number of the variable to classify
     * 		    In MPE Imputation, is the number of iterations.
     */
    public Imputation(int method, int submethod) {
	imputationMethod=method;
	imputationSubMethod=submethod;
    }
    
    
    
    /*---------------------------------------------------------------*/
/**
 *  Apply a imputation method to a DataBaseCases object. Replacing
 *  the missing values for new computed values.
 *
 *  @param Cases DataBaseCases object with the variables and values
 */
    
    public void apply(DataBaseCases cases) throws IOException{
	
	switch (imputationMethod) {
	case ZEROIMPUTATION:
	    this.zeroImputation(cases);
	    break;
	case AVERAGEIMPUTATION:
	    this.averageImputation(cases);
	    break;
	case CLASSCONDITIONEDMEANIMPUTATION:
	    this.classConditionedMeanImputation(cases,imputationSubMethod);
	    break;
	case CLASSIFICATIONTREEIMPUTATION:
	    this.classificationTreeImputation(cases,imputationSubMethod);
	    break;
	case ITER_MPEIMPUTATION:
	    this.ITER_MPEImputation(cases,imputationSubMethod);
	    break;
	case INCR_MPEIMPUTATION:
	    this.INCR_MPEImputation(cases);
	    break;
	case REMOVEMISSING:
	    this.removeMissing(cases);
	    break;
	}
    }//End apply method
    
    
    /*---------------------------------------------------------------*/
    /**
     *  Apply the class-contioned mean imputation method to a DataBaseCases
     *  object. Replacing the missing values using class-conditioned means.
     *
     *  @param Cases DataBaseCases object with the variables and values
     *  @param classnumber nuber of variable that it's the class
     */
    public void classConditionedMeanImputation(DataBaseCases cases, int classnumber) {
	//Iterators
	int i,j;

	//Data strucutures to go through values and vars
  NodeList nodes=cases.getVariables();
  Vector vector=cases.getRelationList();
  Relation relation=(Relation)vector.elementAt(0);
  CaseListMem caselistmem=(CaseListMem)relation.getValues();
  Configuration conf = (Configuration)caselistmem.get(0);
  
  //Check it the classnumber is a valid variable
  if ( (classnumber<0) || (classnumber>=nodes.size()) ) {	
      System.err.print("WARNING: Using a incorrect class variable ("+classnumber);
      classnumber=nodes.size()-1;
      System.err.println("). Using "+classnumber+".");
  }
  
  //check if the class variables it's FiniteStates
  if ((nodes.elementAt(classnumber)).getTypeOfVariable()==Node.CONTINUOUS) {
      throw new SecurityException ("The variable to classify ("+(classnumber+1)+") can't be Continuous");
  }
  
  //Get the number of classes
  int classes=((FiniteStates)nodes.elementAt(classnumber)).getNumStates();

  //Data structures to store average values and the number of valid values (no undef)
  double    averageValues[][] = new double [nodes.size()][classes];
  long  numberValidValues[][] = new long   [nodes.size()][classes];
  
  //Initiate the vectors (the valid number of cases and with the averages) with 0
  for (j=0 ; j< nodes.size()  ; j++)
      for (i=0 ; i< classes  ; i++) 
	  averageValues[j][i]=numberValidValues[j][i]=0;
  
  //Impute the class, using a priori probability. So impute the most frequent.
  int max=0;
  Node c =(Node)(caselistmem.getVariables()).elementAt(classnumber);
  for (j=0 ; j< caselistmem.getNumberOfCases()  ; j++) 
      if ( c.undefValue()!=caselistmem.getValue(j,classnumber) ) {
    averageValues[classnumber][(int)caselistmem.getValue(j,classnumber)]++;
    if ( averageValues[classnumber][(int)caselistmem.getValue(j,classnumber)] > averageValues[classnumber][max] )
	max=(int)caselistmem.getValue(j,classnumber);
      }
  
  //Store the class to impute to the variable to classify
  for (j=0 ; j< classes ; j++) 
      averageValues[classnumber][j]=max;
  //imput the class to the variable to classify
  for (i=0 ; i< caselistmem.getNumberOfCases()  ; i++) {
      Node n =(Node)(caselistmem.getVariables()).elementAt(classnumber);
      //Impute the average only in undef/missing values
      if ( n.undefValue()==caselistmem.getValue(i,classnumber) ) {
	  caselistmem.setValue(i,classnumber, max);
      }
  }//end for i
  
  //Add the valid cases for each variable and for each class
  for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
      conf = (Configuration)caselistmem.get(i);
      for (j=0 ; j< nodes.size()  ; j++) 
	  if (j!=classnumber) {
	      Node n =(Node)(caselistmem.getVariables()).elementAt(j);
	      //Use only no undef values
      if ( n.undefValue()!=caselistmem.getValue(i,j) ) {
	  averageValues[j][(int)caselistmem.getValue(i,classnumber)]+=(double)caselistmem.getValue(i,j);
	  numberValidValues[j][(int)caselistmem.getValue(i,classnumber)]++;
      }//end if
	  }//end if 
  }//end for i
  
  //Compute the average for each variable and for each class
  for (i=0 ; i< nodes.size()  ; i++) 
      for (j=0 ; j< classes  ; j++) 
	  averageValues[i][j]/=(double)numberValidValues[i][j];
  
  //We travel through the values for each case
  for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
      conf = (Configuration)caselistmem.get(i);
      for (j=0 ; j< nodes.size()  ; j++) {
	  Node n =(Node)(caselistmem.getVariables()).elementAt(j);
	  //Impute the average only in undef/missing values, looking the class
	  if ( n.undefValue()==caselistmem.getValue(i,j) ) {
	      if (n.getTypeOfVariable()==Node.CONTINUOUS)
		  caselistmem.setValue(i,j, averageValues[j][(int)caselistmem.getValue(i,classnumber)] );
	      else
		  caselistmem.setValue(i,j, (int)Math.round(averageValues[j][(int)caselistmem.getValue(i,classnumber)]) );
	  }
      }//end for j
  }//end for i
  

  //If after the imputation there is a value that it's stil undefined, 
  //we change the UndefValue
  for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
      for (j=0 ; j< nodes.size()  ; j++) {
	  Node n =(Node)(caselistmem.getVariables()).elementAt(j);
	  if ( (n.getTypeOfVariable()==Node.CONTINUOUS) && (n.undefValue()==caselistmem.getValue(i,j)) ) {
	      Continuous n2 =(Continuous)(caselistmem.getVariables()).elementAt(j);
	      //n2.setUndefVal(caselistmem.getValue(i,j)-1);
              n2.setUndefVal(Double.NaN);
	  }
      }//end for j
  }//end for i

}//End classConditionedMeanImputation method
    
    /*---------------------------------------------------------------*/ 
    /**
     *  Apply the average imputation method to a DataBaseCases object. Replacing
     *  the missing values for new computed values. Replaces missing values
     *  with the average of all the values for that variable.
     *
     *  @param Cases DataBaseCases object with the variables and values
     */
    public void averageImputation(DataBaseCases cases) { 
	//Iterators
	int i,j;
	
	//Data strucutures to go through values and vars
	NodeList nodes=cases.getVariables(); 
	Vector vector=cases.getRelationList();
	Relation relation=(Relation)vector.elementAt(0);
	CaseListMem caselistmem=(CaseListMem)relation.getValues();
	Configuration conf = (Configuration)caselistmem.get(0);
  
	//Data structures to store average values and the number of valid values (no undef)
	double    averageValues[] = new double [nodes.size()];
	long  numberValidValues[] = new long   [nodes.size()];
  
	//Initiate the vectors (the valid number of cases and with the averages) with 0
	for (j=0 ; j< nodes.size()  ; j++) 
	    averageValues[j]=numberValidValues[j]=0;
	
	//Add the valid cases for each variable
	for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
	    conf = (Configuration)caselistmem.get(i);
	    for (j=0 ; j< nodes.size()  ; j++) {
		Node n =(Node)(caselistmem.getVariables()).elementAt(j);
		//Use only  no undef values
		if ( n.undefValue()!=caselistmem.getValue(i,j) ) {
		    averageValues[j]+=(double)caselistmem.getValue(i,j);
		    numberValidValues[j]++;
		}
	    }
	}
	
	//Compute the average
	for (j=0 ; j< nodes.size()  ; j++) 
	    averageValues[j]/=(double)numberValidValues[j];
	
	//We travel through the values for each case
	for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
	    conf = (Configuration)caselistmem.get(i);
	    for (j=0 ; j< nodes.size()  ; j++) {
		Node n =(Node)(caselistmem.getVariables()).elementAt(j);
		//Impute the average only in undef/missing values
		if ( n.undefValue()==caselistmem.getValue(i,j) ) {
		    if (n.getTypeOfVariable()==Node.CONTINUOUS)
			caselistmem.setValue(i,j, averageValues[j] );
		    else
			caselistmem.setValue(i,j, (int)Math.round(averageValues[j]) );
		}
	    }//end for j
	}//end for i
	
	
	//If after the imputation there is a value that it's stil undefined, 
	//we change the UndefValue
	for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
	    for (j=0 ; j< nodes.size()  ; j++) {
		Node n =(Node)(caselistmem.getVariables()).elementAt(j);
		if ( (n.getTypeOfVariable()==Node.CONTINUOUS) && (n.undefValue()==caselistmem.getValue(i,j)) ) {
		    Continuous n2 =(Continuous)(caselistmem.getVariables()).elementAt(j);
                    //n2.setUndefVal(caselistmem.getValue(i,j)-1);
                    n2.setUndefVal(Double.NaN);
		}
	    }//end for j
	}//end for i
	
    }//End averageImputation method
    /*---------------------------------------------------------------*/ 
    /**
     *  Apply the zero imputation method to a DataBaseCases object. Replacing
     *  the missing values for new computed values. Replaces missing values
     *  with zeros.
     *
     *  @param Cases DataBaseCases object with the variables and values
     */
    
    public void zeroImputation(DataBaseCases cases) { 
	//Iterators
	int i,j;
	//Data structures to go through values and vars
	NodeList nodes=cases.getVariables(); 
	Vector vector=cases.getRelationList(); 
	Relation relation=(Relation)vector.elementAt(0);
	CaseListMem caselistmem=(CaseListMem)relation.getValues();
	Configuration conf;

	//We travel through the values for each case
	for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
	    conf = (Configuration)caselistmem.get(i);
	    for (j=0 ; j< nodes.size()  ; j++) {
		Node n =(Node)(caselistmem.getVariables()).elementAt(j);
		//Impute 0 only in undef/missing values
		if ( n.undefValue()==caselistmem.getValue(i,j) )
		    caselistmem.setValue(i,j,0);
	    }//end for j
	}//end fot i

	//If after the imputation there is a value that it's stil undefined, 
	//we change the UndefValue
	for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
	    for (j=0 ; j< nodes.size()  ; j++) {
		Node n =(Node)(caselistmem.getVariables()).elementAt(j);
		if ( (n.getTypeOfVariable()==Node.CONTINUOUS) && (n.undefValue()==caselistmem.getValue(i,j)) ) {
		    Continuous n2 =(Continuous)(caselistmem.getVariables()).elementAt(j);
                    //n2.setUndefVal(caselistmem.getValue(i,j)-1);
                    n2.setUndefVal(Double.NaN);
		}
	    }//end for j
	}//end for i
	

}//End zeroImputation method


    /*---------------------------------------------------------------*/
    /**
     *  Apply the classification tree method to a DataBaseCases object. Replacing
     *  the missing values for new computed values. The new values are computes
     *  using a ClasificationTree, where the vars with undef values are the class.
     *
     *  @param cases DataBaseCases object with the variables and values
     *  @param method method for build the classification tree
     */

    public void classificationTreeImputation(DataBaseCases cases, int method) {
	//Iterators
	int i,j,u;
	//Data structures to go through values and vars
	NodeList nodes=cases.getVariables();
	Vector vector=cases.getRelationList();
	Relation relation=(Relation)vector.elementAt(0);
	CaseListMem caselistmem=(CaseListMem)relation.getValues();
	Node node;
	boolean undef;

	//We build a classification tree for each variable with undef values
	for (u=0; u < nodes.size(); u++) {
	    undef=false;

	    //Data structures for build the classification tree
	    int attributescases[][]= new int [nodes.size()-1][caselistmem.getNumberOfCases()];
	    int classcases[]= new int [caselistmem.getNumberOfCases()];
	    ClassificationTree ctree=new ClassificationTree();

	    //Build a attributes and class arrays for use the classification tree, where the attribute 'u' is the class
	    for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
		for (j=0 ; j< nodes.size()  ; j++) {
		    node =(Node)(caselistmem.getVariables()).elementAt(j);
		    //Error with continuous variables
		    if (node.getTypeOfVariable()==Node.CONTINUOUS) {
			throw new SecurityException ("There is continuous values. First, use a Discretization method.");
			//The 'u' var is the class
		    } else if (j == u ) {
			classcases[i]=(int)caselistmem.getValue(i,j);
			if (classcases[i]<0) undef=true; //-1 is the undefvalue for FiniteStates variables
			//The rest vars are attributes
		    } else  if (j<u)    attributescases[j][i]=(int)caselistmem.getValue(i,j);
		    else /*if (j>u)*/ attributescases[j-1][i]=(int)caselistmem.getValue(i,j);
      }//end for j
	    }//end for i
	    
	    //If there isn't any undef/missing value, there is nothing to impute.
	    if (!undef) continue;
	    
	    //Create the Classification Tree using  the build arrays
	    switch (method) {
	    case (ClassificationTree.ID3): ctree.id3(attributescases, classcases,10);
		//Prune the classification tree with EBP, using 0.25 confidence level
		ctree.errorBasedPruning(0.25);
		break;
	    case (ClassificationTree.C45): ctree.c45(attributescases, classcases,10);		
		//Prune the classification tree with ERP
		ctree.reducedErrorPruning();
		break;
	    case (ClassificationTree.DIRICHLET): 
		int classnumber=((FiniteStates)(cases.getVariables()).elementAt(u)).getNumStates();
		float dirichletfactor=(float)2.0/(float)classnumber;
		ctree.dirichlet(attributescases, classcases,10,dirichletfactor,1);
		//Prune the classification tree with ERP
		ctree.reducedErrorPruning();
		break;
		
	    default: ctree.c45(attributescases, classcases,10);		
		//Prune the classification tree with EBP, using 0.25 confidence level
		ctree.prune(0.25);
		break;
	    }
	    

	    //Impute the undef/missing values using the ClassficationTree
	    for (i=0; i <  caselistmem.getNumberOfCases(); i++) 
		//classifies only where we have undef values for the class (attribute u)
		if (classcases[i] < 0 ) { //-1 is the undefvalue for FiniteStates variables

		    //buil one case to classify
		    int onecase[]=new int [nodes.size()-1];
		    for (j=0; j < nodes.size()-1; j++) 
			onecase[j]=attributescases[j][i];
		    
		    //classify the case
		    double result[]=ctree.classifies(onecase);
		    
		    //get the class value with max probability
		    int max=0;
		    for (j=0; j < result.length; j++)
			if (result[j] != 0 ) {
			    //			    System.out.println("Attribute "+u+", case "+i+" is the class "+j+" with a probability="+result[j]);
			    if (result[j]>result[max]) max=j;
			}//end if result !=0

		    //impute he class value with max probability
		    caselistmem.setValue(i,u,max);
		    //		    System.out.println("\t Impute to the attribute "+u+", case "+i+" the class "+max);
		}//end if classcases < 0

	}//end for u

	//If after the imputation there is a value that it's stil undefined,
	//we change the UndefValue
	for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
	    for (j=0 ; j< nodes.size()  ; j++) {
		Node n =(Node)(caselistmem.getVariables()).elementAt(j);
		if ( (n.getTypeOfVariable()==Node.CONTINUOUS) && (n.undefValue()==caselistmem.getValue(i,j)) ) {
		    Continuous n2 =(Continuous)(caselistmem.getVariables()).elementAt(j);
                    //n2.setUndefVal(caselistmem.getValue(i,j)-1);
                    n2.setUndefVal(Double.NaN);
		}
	    }//end for j
	}//end for i

    }//End classificationTreeImputation method

    /*--------------------------------------------------------------*/
    /**
     *  This method replaces, in each case with missing, all missing values with one
     *  of the 0 more probable explanation, according to its probability, using
     *  abductive inference. The net is learnt, in the first iteration, using the complete
     *  cases, and in the next iteration, the imputed values are used as well.
     *
     *  @param cases <code>DataBaseCases</code> object with the variables and
     *  the values. Is modified.
     *  @param numIterations Number of iterations.
     */

    public void ITER_MPEImputation(DataBaseCases cases, int numIterations) throws IOException, SecurityException{

	int i, j, k, m, n, i1, j1, t, posi, posj;
	int numExplanations, numMaxExp, numExp, numMissingCases, numValues;
	int maxProbValue, numValuesEvid, numMissingValues;
	int numParent = 5;
	double sumaProb, probability, acumulatedFrec, acumulatedFrecNext;
	double value, undefValue, value1;
	double[] probExp;
	boolean noPutError, firstIteration;
	boolean[] intervals;

	FiniteStates variable = new FiniteStates();
	Configuration currentCase, currentCase1, withoutMissingCase, maxProbConf;
	DataBaseCases dbComplete;
	CaseListMem allCases;
	CaseListMem completeCases = new CaseListMem() ;
	CaseListMem missingCases = new CaseListMem();
	CaseListMem imputedCases;
	CaseListMem withCompleteCases = new CaseListMem();

	Vector positions = new Vector();
	Vector pos, explanations;
	Vector evidenceVector = new Vector();

	PCLearning l;
	Evidence evidence;
	AbductiveInferenceNilsson propagation;
	Explanation maxProbExp , exp;
	Bnet net;
	NodeList nodesSorted, delSeq, expSet;
	Metrics met;
	DELearning outputNet3;
	K2Learning outputNet1;

	numValues = cases.getVariables().size();
	undefValue = variable.undefValue();


	//compruebo que no hay continuas
	boolean hasContVar = false;
	for (int q = 0; (q < numValues)&&(!hasContVar) ; q++){
	if (cases.getVariables().elementAt(q).getTypeOfVariable()==Node.CONTINUOUS)
		hasContVar = true;
	}

	if (hasContVar){
	throw new SecurityException("There is continuous values. First, use a Discretization method.");
	}
	else{


	//Get the list of cases of the DataBaseCases
	allCases = (CaseListMem) cases.getCases();
	//Separate complete cases from missing cases
	allCases.separateMissingValues(missingCases, completeCases);
	numMissingCases = missingCases.getNumberOfCases();


	//Check completeCases is not empty
	if (completeCases.getNumberOfCases() == 0 || missingCases.getNumberOfCases()==0){

	    if (completeCases.getNumberOfCases() == 0)
	    	throw new SecurityException("MPE Imputation method can not be used for this data base.\n Please, try with other imputation method.");
	    else
	    	throw new SecurityException("There isn't any missing value. It isn't necessary an imputation method.");
	}

	else {

	    //if completeCases is not empty and there are some missing values, we perform the imputation method

	    firstIteration = true;

	//Performs several iterations
	    for(int p = 0; p < numIterations; p++) {

	    	System.out.println("\nIteration's number: "+p);

		//Initialise the variables of the CaseList
		imputedCases = new CaseListMem();
		imputedCases.setVariables(completeCases.getVariables());
		imputedCases.setCases(new Vector());

		//Learn with K2 :
		//dbComplete consist of complete cases, in the first iteration,
		//and complete cases plus imputed cases from the last iteration, in the others
		dbComplete = new DataBaseCases("dbComplete", completeCases);
		dbComplete.setNumberOfCases(completeCases.getNumberOfCases());
		met = (Metrics) new BICMetrics(dbComplete);
		nodesSorted = dbComplete.getNodeList();
		outputNet1 = new K2Learning(dbComplete,nodesSorted,numParent,met);
		outputNet1.learning();
		outputNet3 = new DELearning(dbComplete,outputNet1.getOutput());
		outputNet3.learning();
		net = (Bnet)outputNet1.getOutput();

		//Store missing values'positions respect to the set of missing cases,
		//if numIterat > 1, only in the first iteration
		n = 0;
		for (i1 = 0; (i1 < numMissingCases) && (numIterations >1) && (firstIteration==true); i1++){
		    currentCase1 = missingCases.get(i1);
		    if (currentCase1 != null){
			for ( j1 = 0; j1 < numValues; j1++){
			    value1 = currentCase1.getValue(j1);
			    if (value1 == undefValue){
				pos = new Vector();
				pos.insertElementAt(new Integer(i1), 0);
				pos.insertElementAt(new Integer(j1), 1);
				positions.insertElementAt(pos, n);
				n++;
			    }

			}
		    }
		    numMissingValues = positions.size();

		}


		m = 0;

		for (i = 0; i < numMissingCases; i++){
		    currentCase = missingCases.get(i).copy();

		    if (currentCase != null){
			withoutMissingCase = new Configuration();
			//Explanation set
			expSet = new NodeList();
			//Get an evidence from the no-missing values of the missing case
			for (j = 0; j < numValues; j++){
			    variable = currentCase.getVariable(j);
			    if (variable.getTypeOfVariable()==Node.CONTINUOUS)
				throw new SecurityException ("There is continuous values. First, use a Discretization method.");
			    else{
				value = currentCase.getValue(j);

				//Store the evidence only in the first iteration, because is always the same
				if(firstIteration){
				    if (value != undefValue)
					withoutMissingCase.putValue(variable, (int) value);
				    else{
					expSet.insertNode(variable);
				    }
				}
				else {  //no first iteration
				    numMissingValues = positions.size();
				    if(m < numMissingValues){
					pos = (Vector) positions.elementAt(m);
					posi = ((Integer) pos.elementAt(0)).intValue();
					posj = ((Integer) pos.elementAt(1)).intValue();
					//in the position (posi, posj), there is a missing value
					//recover this position
					if ((i==posi)&&(j==posj))  {
					    expSet.insertNode(variable);
					    pos = (Vector) positions.elementAt(m);
					    m++;
					}
				    }
				}

			    }//End for else
			}//End for j


			if (firstIteration){
			    if(withoutMissingCase.size()==0)
				System.out.println("There isn't any values for get the evidence.");
			    evidence = new Evidence (withoutMissingCase);
			    evidenceVector.add(evidence);
			}
			evidence = (Evidence) evidenceVector.elementAt(i);

			//Implements Nilsson's algorithm for finding the 10 most
			//probable explanation with this evidence
			propagation = new AbductiveInferenceNilsson (net, evidence, "trees");
			propagation.setNExplanations(10);
			delSeq = new NodeList();
			//propagation.setDeletionSequence(delSeq);
			propagation.setExplanationSet(expSet);
			propagation.propagate("out");

			//Get the 10 first explanations, if their probabilities are different from 0
			explanations = new Vector();
			explanations = propagation.getKBest();
			t = 0;
			do {
			    exp = new Explanation();
			    exp = (Explanation) explanations.elementAt(t);
			    t++;
			} while ((exp.getProb() != 0) && (t < 10));
			numExplanations = t-1;
			numMaxExp = Math.min(numExplanations, 10);

			//Normalizes the probabilities of the explanations
			sumaProb = 0;
			probExp = new double[numMaxExp];
			for (int q = 0 ; q < numMaxExp; q++){
			    probExp[q] = ((Explanation) explanations.elementAt(q)).getProb();
			    sumaProb = sumaProb + probExp[q];
			}
			for (int q = 0 ; q < numMaxExp; q++){
			    probExp[q] = (((Explanation) explanations.elementAt(q)).getProb())/sumaProb;
			}

			//According to the probability, choose one or other explanations
			probability = Math.random()*1.0;
			intervals = new boolean[numMaxExp];
			intervals[0] = (probability >= 0) && (probability < probExp[0]);
			acumulatedFrec = probExp[0];
			numExp = 0;
			for (int q = 1 ; q < numMaxExp ; q++){
			    acumulatedFrecNext = acumulatedFrec + probExp[q];
			    intervals[q] = (probability >= acumulatedFrec) && (probability < acumulatedFrecNext);
			    acumulatedFrec = acumulatedFrecNext;
			    if (intervals[q]) numExp = q;
			}
			maxProbExp = (Explanation) explanations.elementAt(numExp);
			maxProbConf = maxProbExp.getConf();

			//Replace the missing values using this explanation
			numValuesEvid = maxProbConf.size();
			for(k = 0; k < numValuesEvid; k++){
			    variable = maxProbConf.getVariable(k);
			    if (variable.getTypeOfVariable()==Node.CONTINUOUS)
				throw new SecurityException ("There is continuous values. First, use a Discretization method.");
			    else{
				maxProbValue = maxProbConf.getValue(k);
				currentCase.putValue(variable, maxProbValue);
			    }
			  }//End for k

			noPutError = imputedCases.put(currentCase);
			if (!noPutError) System.out.println("Error adding a case.");

		    }//End if


		    else
			System.out.println("Error getting a case.");

		}//End for i

		//Complete the DataBaseCases with the completed cases and store
		//the previous completeCases for next iterations

		if (firstIteration){
		    withCompleteCases.setVariables(completeCases.getVariables());
		    withCompleteCases.setCases (completeCases.getCases());
		}
		else{
		    completeCases.removeCases();
		    completeCases.setCases(withCompleteCases.getCases());
		}

		completeCases.merge(imputedCases);
		cases.replaceCases(completeCases) ;

		firstIteration = false;
	    }

	}//end else

	}//end else

    }//End ITER_MPEImputation method





    /*--------------------------------------------------------------*/
    /**
     *  This method replaces in each case with missing, all missing values with one
     *  of the 10 more probable explanation, according to its probability, using
     *  abductive inference. Not all cases with missing are replaced at the same time,
     *  but first, cases with one missing value, second, with two, ... At first, the net
     *  is learnt with complete cases, and in each step, imputed cases are used as well.
     *
     *  @param cases <code>DataBaseCases</code> object with the variables and
     *  the values. Is modified.
     */

    public void INCR_MPEImputation(DataBaseCases cases) throws IOException, SecurityException{

	int i, j, k, m, n, i1, j1, t, posi, posj;
	int numExplanations, numMaxExp, numExp, numMissing, numCurrentMissingCases;
	int numMissingCases, numValues, maxProbValue, numValuesEvid, numMissingValues;
	int numParent = 5;
	double sumaProb, probability, acumulatedFrec, acumulatedFrecNext;
	double value, undefValue, value1;
	double[] probExp;
	boolean noPutError;
	boolean modified = true;
	boolean[] intervals;

	Vector positions = new Vector();
	Vector pos, explanations;
	Vector evidenceVector = new Vector();

	FiniteStates variable = new FiniteStates();
	PCLearning l;
	Evidence evidence;
	AbductiveInferenceNilsson propagation;
	NodeList delSeq, nodesSorted, expSet;
	Explanation maxProbExp , exp;
	DELearning outputNet3;
	K2Learning outputNet1;
	Bnet net = new Bnet();
	Metrics met;

	Configuration currentCase, currentCase1, withoutMissingCase, currentMissingCase, maxProbConf;
	CaseListMem allCases, imputedCases, currentMissingCases;
	CaseListMem completeCases = new CaseListMem() ;
	CaseListMem missingCases = new CaseListMem();
	CaseListMem withCompleteCases = new CaseListMem();
	DataBaseCases dbComplete;

	numValues = cases.getVariables().size();
	undefValue = variable.undefValue();

	//compruebo que no hay continuas
	boolean hasContVar = false;
	for (int q = 0; (q < numValues)&&(!hasContVar) ; q++){
	if (cases.getVariables().elementAt(q).getTypeOfVariable()==Node.CONTINUOUS)
		hasContVar = true;
	}

	if (hasContVar){
	throw new SecurityException("There is continuous values. First, use a Discretization method.");
	}
	else{

	//Get the list of cases of the DataBaseCases
	allCases = (CaseListMem) cases.getCases();
	//Separate complete cases from missing cases
	allCases.separateMissingValues(missingCases, completeCases);

	numMissingCases = missingCases.getNumberOfCases();
	int[] numMissingPosition = new int[numMissingCases];

	//Compute and store the number of missing values in each missing case
	for (int z = 0; z < numMissingCases; z++){ //for each case with missing
	    numMissing = 0;
	    currentMissingCase = missingCases.get(z);
	    for (int w = 0; w < numValues; w++) {
		if (currentMissingCase.getValue(w)==undefValue)
		    numMissing++;
	    }
	    numMissingPosition[z] = numMissing;
	}


	//Check completeCases are not empty and there are any missing values
	if (completeCases.getNumberOfCases() == 0 || missingCases.getNumberOfCases()==0){

	    if (completeCases.getNumberOfCases() == 0)
	    	throw new SecurityException("MPE Imputation method can not be used for this data base.\n Please, try with other imputation method.");
	    else
	    	throw new SecurityException("There isn't any missing value. It isn't necessary an imputation method.");
	}

	else {

	    //if completeCases is not empty and there are some missing values, we perform the imputation method

	    //We consider first: cases with one missing value, second: cases with two, and thus incrementally
	    for(int x = 0; x < numValues; x++) {

		//Initialise the variables of the CaseList
		imputedCases = new CaseListMem();
		imputedCases.setVariables(completeCases.getVariables());
		imputedCases.setCases(new Vector());

		currentMissingCases = new CaseListMem();
		currentMissingCases.setVariables(completeCases.getVariables());
		currentMissingCases.setCases(new Vector());

		numCurrentMissingCases = 0;

		// we only consider those cases that have numMissing missing values
		//for example: first, we only consider those that have one missing value
		for (int z = 0; z < numMissingCases; z++) {
		    if (numMissingPosition[z]==x+1)
			currentMissingCases.put(missingCases.get(z));
		}

		numCurrentMissingCases = currentMissingCases.getNumberOfCases();

		//Only learn a net if completeCases have been modified, that is,
		if (modified==true){
		    // Learn with K2:
		    //dbComplete consist of complete cases and those imputed cases
		    //that had less than numMissing+1 missing values
		    dbComplete = new DataBaseCases("dbComplete", completeCases);
		    dbComplete.setNumberOfCases(completeCases.getNumberOfCases());
		    met = (Metrics) new BICMetrics(dbComplete);
		    nodesSorted = dbComplete.getNodeList();
		    outputNet1 = new K2Learning(dbComplete,nodesSorted,numParent,met);
		    outputNet1.learning();
		    outputNet3 = new DELearning(dbComplete,outputNet1.getOutput());
		    outputNet3.learning();
		    net = (Bnet)outputNet1.getOutput();
		}

		m = 0;

		for (i = 0; i < numCurrentMissingCases; i++){
		    currentCase = currentMissingCases.get(i).copy();

		    if (currentCase != null){
			withoutMissingCase = new Configuration();
			//Explanation set
			expSet = new NodeList();

			//Get an evidence from the no-missing values of the missing case
			for (j = 0; j < numValues; j++){
			    variable = currentCase.getVariable(j);
			    if (variable.getTypeOfVariable()==Node.CONTINUOUS)
				throw new SecurityException ("There is continuous values. First, use a Discretization method.");
			    else{
				value = currentCase.getValue(j);
				if (value != undefValue)
				    withoutMissingCase.putValue(variable, (int) value);
				else{
				    expSet.insertNode(variable);
				}
			    }//End for else
			}//End for j
			if(withoutMissingCase.size()==0)
			    System.out.println("There isn't any values for get the evidence.");
			evidence = new Evidence (withoutMissingCase);
			evidenceVector.add(evidence);
			evidence = (Evidence) evidenceVector.elementAt(i);

			//Implements Nilsson's algorithm for finding the 10 most
			//probable explanation with this evidence
			propagation = new AbductiveInferenceNilsson (net, evidence, "trees");
			propagation.setNExplanations(10);
			delSeq = new NodeList();
			//propagation.setDeletionSequence(delSeq);
			propagation.setExplanationSet(expSet);
			propagation.propagate("out");

			//Get the 10 first explanations, if their probabilities are different from 0
			explanations = new Vector();
			explanations = propagation.getKBest();
			t = 0;
			do {
			    exp = new Explanation();
			    exp = (Explanation) explanations.elementAt(t);
			    t++;
			} while ((exp.getProb() != 0) && (t < 10));
			numExplanations = t-1;
			numMaxExp = Math.min(numExplanations, 10);

			//Normalizes the probabilities of the explanations
			sumaProb = 0;
			probExp = new double[numMaxExp];
			for (int q = 0 ; q < numMaxExp; q++){
			    probExp[q] = ((Explanation) explanations.elementAt(q)).getProb();
			    sumaProb = sumaProb + probExp[q];
			}
			for (int q = 0 ; q < numMaxExp; q++){
			    probExp[q] = (((Explanation) explanations.elementAt(q)).getProb())/sumaProb;
			}

			//According to the probabilities of the explanation, choose one or other
			probability = Math.random()*1.0;
			intervals = new boolean[numMaxExp];
			intervals[0] = (probability >= 0) && (probability < probExp[0]);
			acumulatedFrec = probExp[0];
			numExp = 0;
			for (int q = 1 ; q < numMaxExp ; q++){
			    acumulatedFrecNext = acumulatedFrec + probExp[q];
			    intervals[q] = (probability >= acumulatedFrec) && (probability < acumulatedFrecNext);
			    acumulatedFrec = acumulatedFrecNext;
			    if (intervals[q]) numExp = q;
			}
			maxProbExp = (Explanation) explanations.elementAt(numExp);
			maxProbConf = maxProbExp.getConf();

			//Replace the missing values using this explanation
			numValuesEvid = maxProbConf.size();
			for(k = 0; k < numValuesEvid; k++){
			    variable = maxProbConf.getVariable(k);
			    if (variable.getTypeOfVariable()==Node.CONTINUOUS)
				throw new SecurityException ("There is continuous values. First, use a Discretization method.");
			    else{
				maxProbValue = maxProbConf.getValue(k);
				currentCase.putValue(variable, maxProbValue);
			    }
			}//End for k

			noPutError = imputedCases.put(currentCase);
			if (!noPutError) System.out.println("Error adding a case.");

		    }//End if


		    else
			System.out.println("Error getting a case.");

		}//End for i

		//Adds those imputed cases that had numMissing missing values
		//to completeCases. CompleteCases increase incrementally.

		//if there is any case with this number of missing values
		if (imputedCases.getNumberOfCases()!=0){
		    completeCases.merge(imputedCases);
		    cases.replaceCases(completeCases) ;
		    modified = true;
		}
		else
		    modified = false;

	    }


	    }//end else

	}//end else



    }//End INCR_MPEImputation method


    /*---------------------------------------------------------------*/
    /*---------------------------------------------------------------*/
    /**
     *  In this method remove the rows with missing values.
     *
     *  @param cases DataBaseCases object with the variables and values. In this
     *               object will be stored the new DataBaseCases without rows
     *               with missing values.
     */

    public void removeMissing(DataBaseCases cases) {
	//get the old cases
	Vector vector=cases.getRelationList();
	Relation relation=(Relation)vector.elementAt(0);
	CaseListMem caselistmem=(CaseListMem)relation.getValues();

	//build the new cases without missing values
	CaseListMem newcases = (CaseListMem) new ContinuousCaseListMem(cases.getNodeList());
	int added=0;
	for (int i=0;i<cases.getNumberOfCases();i++) {
	    //Look if this row have missing values
	    boolean missing=false;
	    for(int j=0;j<cases.getNodeList().size();j++)
		if ( (cases.getNodeList().elementAt(j)).undefValue() == caselistmem.getValue(i,j) )
		    missing=true;
	    //if this row haven't missing values, it must be stored
	    if (!missing)  { newcases.put(caselistmem.get(i)); added++;}
	}//end for i


	//Store the cases without missing values in the dbc
	relation = new Relation();
	relation.setVariables(cases.getNodeList());
	relation.setValues(newcases);
	Vector relations = new Vector();
	relations.addElement(relation);
	cases.setRelationList(relations);
	cases.setNumberOfCases(added);

    }//end removeMissing method

    /*---------------------------------------------------------------*/
    /*---------------------------------------------------------------*/
    /**
     * For performing tests
     */

    public static void main(String args[]) throws ParseException, IOException
    {
	if(args.length < 3){
	    System.out.println("Too few arguments: Usage: file.dbc out.dbc (for saving the result) method (0=Zeros, 1=Average, 2=Classification tree, 3=Class-conditioned mean imputation, 4=Iterative-MPE imputation, 5=Incremental-MPE imputation, 6=Remove Missing) [submethod] (0=ID3, 1=C4.5, 2=Dirichlet, classnumber, or numIterat)");
	    System.exit(0);
	}

	//Open the database
	int method = (Integer.valueOf(args[2])).intValue();
	int submethod = ClassificationTree.C45;
	if(args.length > 3) submethod = (Integer.valueOf(args[3])).intValue();

	int numIteraciones = (Integer.valueOf(args[2])).intValue();

	FileInputStream f = new FileInputStream(args[0]);
	DataBaseCases cases = new DataBaseCases(f);
	f.close();
	Imputation imputation= new Imputation();

	//Do the imputation for missing values
	switch ( method ) {
	case 0: System.out.println("Imputation Method: Zeros");
	  imputation.zeroImputation(cases);
	  break;     
	case 1: System.out.println("Imputation Method: Average");
	    imputation.averageImputation(cases);
	    break;     
	case 2: System.out.print  ("Imputation Method: Classification Tree ");
	    switch (submethod) {
	    case (ClassificationTree.ID3): System.out.println("(ID3)");break;
	    case (ClassificationTree.C45): System.out.println("(C45)");break;
	    case (ClassificationTree.DIRICHLET): System.out.println("(Dirichlet)");break;
	    default: System.out.println("( C45 )");break;
	    }
	    imputation.classificationTreeImputation(cases,submethod);
	  break;
	case 3: System.out.println ("Imputation Method: Class-conditioned Mean Imputation (class variable="+submethod+")");
	    imputation.classConditionedMeanImputation(cases,submethod);
	    break;
	case 4: System.out.println("Imputation Method: Iterative MPE Imputation (number of iterations="+submethod+")");
	    imputation.ITER_MPEImputation(cases,submethod);
	    break;
	case 5: System.out.println("Imputation Method: Incremental MPE Imputation");
	    imputation.INCR_MPEImputation(cases);
	    break;

	case 6: System.out.println("Imputation Method: Remove Missing");
	    imputation.removeMissing(cases);
	    break;

	default: System.out.println("Too few arguments: Usage: file.dbc out.dbc (for saving the result) method (0=Zeros, 1=Average, 2=Classification tree, 3=Class-conditioned mean imputation, 4=Iterative-MPE imputation, 5=Incremental-MPE imputation, 6=Remove Missing) [submethod] (0=ID3, 1=C4.5, 2=Dirichlet, classnumber, or numIterat)");
	    System.exit(0);
	}//end switch

	//Save the new DataBaseCase
	FileWriter  f2 = new FileWriter(args[1]);
	cases.saveDataBase(f2);
	f2.close();
    }//End main
}//End Imputation class
