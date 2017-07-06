/* ClassifierValidator.java */

package elvira.learning.classification;

import elvira.Relation;
import elvira.learning.classification.supervised.discrete.DiscreteClassifierDiscriminativeLearning;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Vector;
import java.util.Arrays;
import java.util.Random;

import java.lang.Math;

import elvira.NodeList;
import elvira.CaseList;
import elvira.CaseListMem;
import elvira.ContinuousCaseListMem;
import elvira.Configuration;
import elvira.ContinuousConfiguration;
import elvira.InvalidEditException;
import elvira.Node;
import elvira.FiniteStates;
import elvira.Continuous;

import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.learning.classification.Classifier;
import elvira.learning.classification.ConfusionMatrix;

import elvira.learning.classification.supervised.discrete.Naive_Bayes;//Used only for test purposes
import elvira.learning.classification.supervised.mixed.*;
import elvira.learning.classification.supervised.validation.AvancedConfusionMatrix;
import elvira.learning.classification.supervised.validation.ClassifierDBC;
import elvira.learning.classification.supervised.discrete.DiscreteClassifier;
/*---------------------------------------------------------------*/ 
/**
 * This class is for using to validate a classifier (implements 
 * Classifier interface). Supports TrainingAndTest validation and
 *  k-fold cross-validation. To use this class the classier must comply:<ul>
 *<li>All the classifier parameters are introduced in the constructor because 
 *  this class have to be completly independent from classifier parameters.</li>
 *<li>Must have a learn method with a DataBaseCase parameter that build the 
 *  classifier and a second integer parameter that indicates what variable is the 
 *  class.</li> 
 *<li>Must have a classify method with a Configuration parameter that it's a 
 *  instance and a second integer parameter that indicates what variable is the 
 *  class. This method must return the probability for each class for the given 
 *  instance.</li>
 *</ul>
 *
 * @author J. G. Castellano (fjgc@decsai.ugr.es)
 * @since 10/04/2002
 */


public class ClassifierValidator {
    /* These constants are used to distinguish the validation method*/
    /** No one validation method is selected*/
    public static final int NONE = -1;    
    /** For use Train and Test validation */
    public static final int TRAINANDTEST = 0;    
    /** For use K-fold cross validation */
    public static final int KFOLD = 1;    
    /** For use Leave One Out cross validation */
    public static final int LEAVEONEOUT = 2;    

/**
  * The DataBaseCases where all the cases will be stored
  */
  DataBaseCases dbc;

/**
  * Vector where the subsets will be stored
  */
  Vector subsets;

/**
  * Classifier to evaluate. 
  */
  Classifier classifier;

/**
 * Discriminative model to be validated
 */
  DiscreteClassifierDiscriminativeLearning discriminativeClassifier;

/**
  * The number of the var that is the class
  */
  int classvar;
  
  /**
   * The target or class variable.
   */
  FiniteStates classVariable;

/**
  * Random object to generate random numbers
  */
  Random rand;

/**
  * To show warning messages or not, by default it's true.
  */
  boolean warnings;

/**
  * The last validation method used
  */
  int method;


/**
  * The k used in k-fold cross validation
  */
  int k;

/*---------------------------------------------------------------*/ 
/**
 * Constructor for the classifier validator.
 * @param classifier classifier to validate
 * @param dbc set to build and test the classifier
 * @param classvar number of the variable to classify
 */
public ClassifierValidator(Classifier classifier, DataBaseCases dbc, int classvar) {
    this.classifier=classifier;
    this.dbc=dbc;
    this.classvar=classvar;
    this.classVariable=(FiniteStates)dbc.getVariables().elementAt(classvar);
    
    this.subsets=new Vector();
    this.rand= new Random(0);
    this.warnings=true;
    this.k=-1;
    this.method=NONE;
    
    
   
    //check the classvar (number and FiniteStates)
    if ( (classvar<0) || (classvar>=(dbc.getVariables()).size()) ) {	
	this.classvar=(dbc.getVariables()).size()-1;
	System.err.println("WARNING: Using a incorrect class variable. Using "+this.classvar+".");		
	classvar=this.classvar;
    }

    if ((dbc.getVariables()).elementAt(classvar).getTypeOfVariable() != Node.FINITE_STATES ) {
	System.err.println("WARNING: Using a incorrect class variable, it's continuous. This will fail.");		
    }

}//end ctor for one dbc
/*---------------------------------------------------------------*/ 
/**
 * Constructor for the classifier validator.
 * @param classifier classifier to validate
 * @param dbcs vector with the DataBaseCases to use for build and test the classifier. If use 
 *             Train And Test, the first dbc will be the train set and the second the test set.
 * @param classvar number of the variable to classify
 * @param method the method to use for make the validation (TRAINANDTEST, KFOLD, etc.)
 */
public ClassifierValidator(Classifier classifier, Vector dbcs, int classvar, int method) throws InvalidEditException {
    int i;

    this.classifier=classifier;
    this.classvar=classvar;
    this.classVariable=(FiniteStates)((DataBaseCases)dbcs.elementAt(0)).getVariables().elementAt(classvar);
    this.rand= new Random(); 
    this.warnings=true;

    //check if there is any DataBaseCase
    if ( dbcs.size()<=0 ) {
	System.err.println("WARNING: No DataBaseCases sets are found.");		
	System.exit(0);
    }

    //check the k or T&T
    if(method==KFOLD) {
	this.k=dbcs.size();
	this.method=KFOLD;
    } else if (method==TRAINANDTEST) {
	this.k=-1; 
	this.method=TRAINANDTEST;
    } else if (method==LEAVEONEOUT) {
	this.k=dbcs.size();
	this.method=LEAVEONEOUT;
    } else {
	this.k=-1;
	this.method=NONE;
    }

    //check the variables
    for (i=0;i<dbcs.size()-1;i++) {
	if (   (((DataBaseCases)dbcs.elementAt(i)).getVariables()).size() != (((DataBaseCases)dbcs.elementAt(i+1)).getVariables()).size() ) {
	    System.err.println("ERROR: Using different DataBaseCases sets.");		
	    System.exit(0);
	}

	if ( (method==LEAVEONEOUT) && (((DataBaseCases)dbcs.elementAt(i)).getCases()).getNumberOfCases() 
 > 1  ) {
	    System.err.println("ERROR: Using bad DataBaseCases for Leave One Out.");		
	    System.exit(0);
	}
    }
    //build the subsets vector
    this.subsets=new Vector(); 
    for (i=0;i<dbcs.size();i++) 
	this.subsets.add(dbcs.elementAt(i));

    //Build the dbc with all the parts
    this.dbc=this.mergeCases();

    //check the classvar (number and FiniteStates)
    if ( (classvar<0) || (classvar>=(dbc.getVariables()).size()) ) {	
	this.classvar=(dbc.getVariables()).size()-1;
	System.err.println("WARNING: Using a incorrect class variable. Using "+this.classvar+".");		
	classvar=this.classvar;
    }
    if ((dbc.getVariables()).elementAt(classvar).getTypeOfVariable() != Node.FINITE_STATES ) {
	System.err.println("WARNING: Using a incorrect class variable, it's continuous. This will fail.");		
    }

}//end ctor from more than one dbc



/**
 * Constructor for the classifier validator.
 * @param classifier classifier to validate
 * @param dbcTotal the whole data base where is validated the classifier.
 * @param dbcs vector with the DataBaseCases to use for build and test the classifier. If use 
 *             Train And Test, the first dbc will be the train set and the second the test set.
 * @param classvar number of the variable to classify
 * @param method the method to use for make the validation (TRAINANDTEST, KFOLD, etc.)
 */

public ClassifierValidator(Classifier classifier, DataBaseCases dbcTotal, Vector dbcs, int classvar, int method) throws InvalidEditException {
    int i;

    this.classifier=classifier;
    this.classvar=classvar;
    this.classVariable=(FiniteStates)((DataBaseCases)dbcs.elementAt(0)).getVariables().elementAt(classvar);
    this.rand= new Random(); 
    this.warnings=true;

    //check if there is any DataBaseCase
    if ( dbcs.size()<=0 ) {
	System.err.println("WARNING: No DataBaseCases sets are found.");		
	System.exit(0);
    }

    //check the k or T&T
    if(method==KFOLD) {
	this.k=dbcs.size();
	this.method=KFOLD;
    } else if (method==TRAINANDTEST) {
	this.k=-1; 
	this.method=TRAINANDTEST;
    } else if (method==LEAVEONEOUT) {
	this.k=dbcs.size();
	this.method=LEAVEONEOUT;
    } else {
	this.k=-1;
	this.method=NONE;
    }

    //check the variables
    for (i=0;i<dbcs.size()-1;i++) {
	if (   (((DataBaseCases)dbcs.elementAt(i)).getVariables()).size() != (((DataBaseCases)dbcs.elementAt(i+1)).getVariables()).size() ) {
	    System.err.println("ERROR: Using different DataBaseCases sets.");		
	    System.exit(0);
	}

	if ( (method==LEAVEONEOUT) && (((DataBaseCases)dbcs.elementAt(i)).getCases()).getNumberOfCases() 
 > 1  ) {
	    System.err.println("ERROR: Using bad DataBaseCases for Leave One Out.");		
	    System.exit(0);
	}
    }
    //build the subsets vector
    this.subsets=new Vector(); 
    for (i=0;i<dbcs.size();i++) 
	this.subsets.add(dbcs.elementAt(i));

    //Build the dbc with all the parts
    //this.dbc=this.mergeCases();
    this.dbc=dbcTotal;

    //check the classvar (number and FiniteStates)
    if ( (classvar<0) || (classvar>=(dbc.getVariables()).size()) ) {	
	this.classvar=(dbc.getVariables()).size()-1;
	System.err.println("WARNING: Using a incorrect class variable. Using "+this.classvar+".");		
	classvar=this.classvar;
    }
    if ((dbc.getVariables()).elementAt(classvar).getTypeOfVariable() != Node.FINITE_STATES ) {
	System.err.println("WARNING: Using a incorrect class variable, it's continuous. This will fail.");		
    }

}//end ctor from more than one dbc


/*---------------------------------------------------------------*/ 
/**
 * Constructor for the classifier validator.
 * @param classifier classifier to validate
 * @param dbc set to build and test the classifier
 * @param classvar number of the variable to classify
 */
public ClassifierValidator(Classifier classifier, DataBaseCases dbc, int classvar, boolean warnings) {
    this.classifier=classifier;
    this.dbc=dbc;
    this.classvar=classvar;
    this.classVariable=(FiniteStates)dbc.getVariables().elementAt(classvar);
    this.subsets=new Vector();
    this.rand= new Random();
    this.warnings=warnings;
    this.k=-1;
    this.method=NONE;
    //check the classvar (number and FiniteStates)
    if ( (classvar<0) || (classvar>=(dbc.getVariables()).size()) ) {	
	this.classvar=(dbc.getVariables()).size()-1;
	if (warnings) System.err.println("WARNING: Using a incorrect class variable. Using "+this.classvar+".");		
	classvar=this.classvar;
    }

    if ((dbc.getVariables()).elementAt(classvar).getTypeOfVariable() != Node.FINITE_STATES ) {
	if (warnings) System.err.println("WARNING: Using a incorrect class variable, it's continuous. This will fail.");		
    }
}//en ctor
/*---------------------------------------------------------------*/ 
/**
 * Constructor for the classifier validator.
 * @param DiscreteClassifierDiscriminativeLearning to validate
 * @param dbc set to build and test the classifier
 * @param classvar number of the variable to classify
 */
public ClassifierValidator(DiscreteClassifierDiscriminativeLearning classifier, DataBaseCases dbc, int classvar) {
    this.discriminativeClassifier = classifier;
    this.classifier               = classifier;
    this.dbc=dbc;
    this.classvar=classvar;
    this.classVariable=(FiniteStates)dbc.getVariables().elementAt(classvar);
    this.subsets=new Vector();
    this.rand= new Random();
    this.warnings=true;
    this.k=-1;
    this.method=NONE;
    
    //check the classvar (number and FiniteStates)
    if ( (classvar<0) || (classvar>=(dbc.getVariables()).size()) ) {	
      this.classvar=(dbc.getVariables()).size()-1;
      System.err.println("WARNING: Using a incorrect class variable. Using "+this.classvar+".");		
      classvar=this.classvar;
    }

    if ((dbc.getVariables()).elementAt(classvar).getTypeOfVariable() != Node.FINITE_STATES ) {
      System.err.println("WARNING: Using a incorrect class variable, it's continuous. This will fail.");		
    }

}//end ctor for one dbc
/*---------------------------------------------------------------*/ 
/**
 * This method compute a random permutation from 0 to n
 * @param m permutation size
 * @return int array with the random permutaition
 */
private int [] randPermutationInt(int m) {
    Vector list1= new Vector();//vector for positions
    Vector list2= new Vector();//vector for contents
    int sol[]= new int[m];   //solution

    
    //Initializate t he two vectors
    for (int i=0;i<m;i++) { 
	list1.add(new Integer(i)); 
	list2.add(new Integer(i));
    }

    //Generate a random permitation
    while (list1.size()>0) {
	//get 2 random positions
	int pos1=this.rand.nextInt(list1.size());
	int pos2=this.rand.nextInt(list2.size());

	//Get the position and the content
	int pos=((Integer)list1.elementAt(pos1)).intValue();
	int con=((Integer)list2.elementAt(pos2)).intValue();
	
	//remove both elements frmm the vectors
	list1.removeElementAt(pos1);
	list2.removeElementAt(pos2);

	//add the element to the solution
	sol[pos]=con;
    }
    return  sol;
}//end randPermutationInt method

/*---------------------------------------------------------------*/ 
/**
 * This method compute a random permutation from 0 to n
 * @param m permutation size
 * @return int array with the random permutaition
 */
private int [] randEqualDistributedPermutationInt(int K) {
    ClassifierDBC cdbc;
//System.out.println(classVariable.getName());
    if (this.dbc.getClass()!=ClassifierDBC.class){
        
        cdbc=new ClassifierDBC(this.dbc,this.dbc.getVariables().getId(this.classVariable));
    }
    else
        cdbc=(ClassifierDBC)this.dbc;
    

    int[][] casesK=new int[K][cdbc.getNumberOfStates()];
    int[] numcasesClass=cdbc.getCasesClass();
    
    for (int i=0; i<K; i++){
        for (int j=0; j<casesK[i].length; j++){
            casesK[i][j]=(int)Math.round(numcasesClass[j]/(double)(K-i));
            numcasesClass[j]-=casesK[i][j];
        }
    }
            
    int[][] numberofCases=cdbc.getNumberCasesByClass();
    numcasesClass=cdbc.getCasesClass();
    
    Vector listPositions= new Vector();//vector for positions
    int sol[]= new int[this.dbc.getNumberOfCases()];   //solution

    for (int i=0; i<cdbc.getNumberOfStates(); i++){
        Vector listNumbers=new Vector();
        for (int j=0; j<numcasesClass[i]; j++)
            listNumbers.addElement(new Integer(numberofCases[i][j]));
        listPositions.addElement(listNumbers);
    }
    int cont=0;
    for (int i=0; i<K; i++){
        for (int j=0; j<cdbc.getNumberOfStates(); j++){
            Vector list=(Vector)listPositions.elementAt(j);
            for (int k=0; k<casesK[i][j]; k++){
                int pos=this.rand.nextInt(list.size());
                sol[cont]=((Integer)list.elementAt(pos)).intValue();
                list.removeElementAt(pos);
                cont++;
            }
        }
    }
            
    return  sol;
}//end randPermutationInt method
/*---------------------------------------------------------------*/ 
/**
 * Sets the seed of the random number generator using a single long 
 * seed. The random number generatos is used to make random 
 * partitions fopr the training and test sets.
 * @param seed the initial seed
 */
public void setSeed(long seed) { 
    this.rand.setSeed(seed);
}//end setSeed method
/*---------------------------------------------------------------*/ 



/**
 * This method split the data set in k equal (more or less) data 
 * sets. This sets are randomly generated.
 * @param k number of subsets 
 */
public void splitCases(int k) throws InvalidEditException {
    int n=this.dbc.getNumberOfCases();

    //check if k is greater than the data cases or less than 0
    if ((k<1) || (k>n)) {
	System.err.print("ERROR: Using a incorrect k="+k+" por partition for "+n+" cases.");
	System.exit(-1);
    }

    //get the vars and cases from the DataBaseCases
    int i,j,t;
    NodeList vars=dbc.getVariables();
    CaseListMem cases=(CaseListMem)dbc.getCases();

    ContinuousCaseListMem tmp= new ContinuousCaseListMem(vars);
    int[] indexPutConf=tmp.getIndexPutQuickly((Configuration)new ContinuousConfiguration(tmp.getVariables()));

    //Get a ramdom permutation for a random selection of the sets
    //int randorder[]=this.randPermutationInt(n);
    int randorder[]=this.randEqualDistributedPermutationInt(k);

    //build the k subsets
    int limit=0;
    float flimit=0;
    for (i=0,t=1; t <= k; t++) {
        
	//create the k partition cases
	ContinuousCaseListMem dkpartcases= new ContinuousCaseListMem(vars); //There is continuous vars.
	CaseListMem ikpartcases= new CaseListMem(vars); //We have only finite states vars

	//look for if we has Continuous vars
	boolean hasdoubles;
	if (cases.getClass()==ContinuousCaseListMem.class)  hasdoubles=true;
	else hasdoubles=false;


	//set the limit number for each k partition
	if (t==k) flimit=n;
	else flimit+=(float)n/(float)k;
	limit=Math.round(flimit);

	//build the k partition
	int start=i;
	for (;i<limit;i++) {

	    //copy the random case 	    
	    Vector v = new Vector(vars.size());
	    for (j=0; j < vars.size(); j++) {
		Double valor=new Double(cases.getValue(randorder[i],j));
		/*Double valor;
		if (hasdoubles) valor=new Double(drandomcase[j]);
		else valor=new Double(irandomcase[j]);*/

		if ((vars.elementAt(j)).getTypeOfVariable() == Node.CONTINUOUS)
		    v.add(valor);
		else
		    v.add(new Integer(valor.intValue()));
	    }//end for j

	    //add the case i to the training set
	    if (hasdoubles){
                dkpartcases.putQuickly((Configuration)new ContinuousConfiguration(dkpartcases.getVariables(), v),indexPutConf);
            }else 
		ikpartcases.put(new Configuration(ikpartcases.getVariables(), v));

	}//end for i
	
	if (hasdoubles) {
	    //Add the number of cases for the k part
	    dkpartcases.setNumberOfCases(limit-start);
	    //Build the new DBC using the cases
	    DataBaseCases dkpartdbc = new DataBaseCases (this.dbc.getName()+"_"+t+"part",vars,dkpartcases);
	    //Add the part
	    this.subsets.add(dkpartdbc);
	} else {
	    //Add the number of cases for the k part
	    ikpartcases.setNumberOfCases(limit-start);
	    //Build the new DBC using the cases
	    DataBaseCases ikpartdbc = new DataBaseCases (this.dbc.getName()+"_"+t+"part",vars,ikpartcases);
	    //Add the part
	    this.subsets.add(ikpartdbc);
	}
    }//end for t

}//end method splitCases
/*---------------------------------------------------------------*/ 
/**
 * This method split the data set in k equal data sets, this sets
 * aren't randomly generated, are sequential generated.
 * @param k number of subsets 
 */
private void splitCasesNoRandomly(int k) throws InvalidEditException {
    int n=this.dbc.getNumberOfCases();

    //check if k is greater than the data cases or less than 0
    if ((k<1) || (k>n)) {
	if (warnings) System.err.println("WARNING: Using a incorrect k por partition. Using "+n+".");
	k=n;
    }

    //get the vars and cases from the DataBaseCases
    int i,j,t;
    NodeList vars=dbc.getVariables();
    CaseListMem cases=(CaseListMem)dbc.getCases();
    
    //build the k subsets
    int limit=0;
    for (i=0,t=1; t <= k; t++) {
	//create the k partirtion cases
	ContinuousCaseListMem dkpartcases= new ContinuousCaseListMem(vars);
	CaseListMem ikpartcases= new CaseListMem(vars);

	//look for if we has Continuous vars
	boolean hasdoubles;
	if (cases instanceof ContinuousCaseListMem)  hasdoubles=true;
	else hasdoubles=false;

	//set the limit number for each k partition
	if (t==k) limit=n;
	else limit+=Math.round((float)n/(float)k);

	//build the k partition
	int start=i;
	for (;i<limit;i++) {
	    Vector v = new Vector();//vars.size());
	    
	    //copy the case i
	    for (j=0; j < vars.size(); j++) {
		Double valor=new Double(cases.getValue(i,j));
		if ((vars.elementAt(j)).getTypeOfVariable() == Node.CONTINUOUS)
		    v.add(valor);
		else
		    v.add(new Integer(valor.intValue()));
	    }//end for j

	    //add the case i to the training set
	    if (hasdoubles) 
		dkpartcases.put((Configuration)new ContinuousConfiguration(dkpartcases.getVariables(), v));
	    else
		ikpartcases.put(new Configuration(ikpartcases.getVariables(), v));

	}//end for i
	if (hasdoubles) {
	    //Add the number of cases for the k part
	    dkpartcases.setNumberOfCases(limit-start);
	    //Build the new DBC using the cases
	    DataBaseCases dkpartdbc = new DataBaseCases (this.dbc.getName()+"_"+t+"part",vars,dkpartcases);
	    //Add the part
	    this.subsets.add(dkpartdbc);
	} else {
	    //Add the number of cases for the k part
	    ikpartcases.setNumberOfCases(limit-start);
	    //Build the new DBC using the cases
	    DataBaseCases ikpartdbc = new DataBaseCases (this.dbc.getName()+"_"+t+"part",vars,ikpartcases);
	    //Add the part
	    this.subsets.add(ikpartdbc);
	}
    }//end for t

}//end method splitCasesNoRamdomly

/*---------------------------------------------------------------*/ 



/**
 * This method merge all the subsets except that one passed as argument.
 * @param e subset excluded, not used to merge
 * @return the DataBaseCases with the merged subsets
 */
public DataBaseCases mergeCases(int e) throws InvalidEditException {
    //get the vars from the DataBaseCases
    int i,j,t,u;
    NodeList vars=dbc.getVariables();

    ContinuousCaseListMem tmp= new ContinuousCaseListMem(vars);
    int[] indexPutConf=tmp.getIndexPutQuickly((Configuration)new ContinuousConfiguration(tmp.getVariables()));
    
    //check if i is greater than the data cases or less than 0
    if ((e<0) || (e>this.subsets.size())) {
	System.err.println("ERROR: The test set number "+e+" doesn't exist.");
	System.exit(-1);
    }

    //create the merged kpartition cases
    ContinuousCaseListMem mergecasesC= new ContinuousCaseListMem(vars); //There is continuous vars.
    CaseListMem mergecasesFS= new CaseListMem(vars); //We have only finite states vars

    //we must look for if we has Continuous vars
    boolean hasdoubles=false;

    //merge the k subsets
    u=this.subsets.size();
    for (t=0; t <u ; t++) {
        //System.out.println("Merging:"+t+" of "+u);
	if (t!=e) {
	    DataBaseCases kpartdbc=(DataBaseCases)subsets.elementAt(t);
	    CaseListMem kpartcases =(CaseListMem)kpartdbc.getCases();//(descomposeDBC(kpartdbc)).elementAt(1);
	    if (kpartcases instanceof ContinuousCaseListMem)  hasdoubles=true;//We look if we have continuous vars
	    
	    int n=kpartcases.getNumberOfCases();

	    //merge the k partition
	    for (i=0;i<n;i++) {
		Vector v = new Vector(vars.size());
		
		//copy the case i of the k partition
		for (j=0; j < vars.size(); j++) {
		    Double valor=new Double(kpartcases.getValue(i,j));
		    if ((vars.elementAt(j)).getTypeOfVariable() == Node.CONTINUOUS)
			v.add(valor);
		    else
			v.add(new Integer(valor.intValue()));
		}//end for j

		//add the case i of the k partition to the merged set
		if (hasdoubles)
		    mergecasesC.putQuickly((Configuration)new ContinuousConfiguration(mergecasesC.getVariables(), v),indexPutConf);
		else 
		    mergecasesFS.put(new Configuration(mergecasesFS.getVariables(), v));
	    }//end for i
	}//end if 
    }//end for t

    //Build and return the new DBC using the cases (FinitesStates cases o Continuous cases)
    if (hasdoubles)
	return new DataBaseCases(this.dbc.getName()+"_"+e+"part",vars,mergecasesC);
    else
	return new DataBaseCases(this.dbc.getName()+"_"+e+"part",vars,mergecasesFS);
}//end method mergeCases execpt one

/*---------------------------------------------------------------*/ 
/**
 * This method merge all the subsets
 * @return the DataBaseCases with the merged subsets
 */
private DataBaseCases mergeCases() throws InvalidEditException {
    //get the vars from the DataBaseCases
    int i,j,t,u;
    NodeList vars;
    if (dbc!=null)
        vars=dbc.getVariables();
    else
        vars=((DataBaseCases)this.subsets.elementAt(0)).getVariables();

    ContinuousCaseListMem tmp= new ContinuousCaseListMem(vars);
    int[] indexPutConf=tmp.getIndexPutQuickly((Configuration)new ContinuousConfiguration(tmp.getVariables()));
    
    //create the merged kpartition cases
    ContinuousCaseListMem mergecases= new ContinuousCaseListMem(vars);

    //merge the k subsets
    u=this.subsets.size();
    for (t=0; t <u ; t++) {
	DataBaseCases kpartdbc=(DataBaseCases)subsets.elementAt(t);
	CaseListMem kpartcases =(CaseListMem)kpartdbc.getCases();//(descomposeDBC(kpartdbc)).elementAt(1);
	
	int n=kpartcases.getNumberOfCases();

	//merge the k partition
	for (i=0;i<n;i++) {
	    Vector v = new Vector(vars.size());
	    
	    //copy the case i of the k partition
	    for (j=0; j < vars.size(); j++) {
		Double valor=new Double(kpartcases.getValue(i,j));
		if ((vars.elementAt(j)).getTypeOfVariable() == Node.CONTINUOUS)
		    v.add(valor);
		else
		    v.add(new Integer(valor.intValue()));
	    }//end for j
	    
	    //add the case i of the k partition to the merged set
	    ContinuousConfiguration newconf = new ContinuousConfiguration(mergecases.getVariables(), v);
	    mergecases.putQuickly(newconf,indexPutConf);
	}//end for i
    }//end for t
    
    //Build and return the new DBC using the cases
    if (dbc!=null)
       return new DataBaseCases(this.dbc.getName()+"_all_parts",vars,mergecases);
    else
       return new DataBaseCases(((DataBaseCases)this.subsets.elementAt(0)).getName()+"_all_parts",vars,mergecases);
}//end method mergeCases for all
/*---------------------------------------------------------------*/ 
/**
 * This method merge all the subsets in the vector passed as argument
 * @param dbcs a vector with the DataBasesCases subsets to merge
 * @return the DataBaseCases with the merged subsets
 */
public DataBaseCases mergeCases(Vector dbcs) throws InvalidEditException {
    //get the vars from the DataBaseCases
    int i,j,t,u;
    NodeList vars=((DataBaseCases)dbcs.elementAt(0)).getVariables();

    ContinuousCaseListMem tmp= new ContinuousCaseListMem(vars);
    int[] indexPutConf=tmp.getIndexPutQuickly((Configuration)new ContinuousConfiguration(tmp.getVariables()));

    
    //create the merged kpartition cases
    ContinuousCaseListMem mergecases= new ContinuousCaseListMem(vars);

    //merge the k subsets
    u=dbcs.size();
    for (t=0; t <u ; t++) {
	DataBaseCases kpartdbc=(DataBaseCases)dbcs.elementAt(t);
	CaseListMem kpartcases =(CaseListMem)kpartdbc.getCases();//(descomposeDBC(kpartdbc)).elementAt(1);
	
	int n=kpartcases.getNumberOfCases();

	//merge the k partition
	for (i=0;i<n;i++) {
	    Vector v = new Vector(vars.size());
	    
	    //copy the case i of the k partition
	    for (j=0; j < vars.size(); j++) {
		Double valor=new Double(kpartcases.getValue(i,j));
		if ((vars.elementAt(j)).getTypeOfVariable() == Node.CONTINUOUS)
		    v.add(valor);
		else
		    v.add(new Integer(valor.intValue()));
	    }//end for j
	    
	    //add the case i of the k partition to the merged set
	    ContinuousConfiguration newconf = new ContinuousConfiguration(mergecases.getVariables(), v);
	    mergecases.putQuickly(newconf,indexPutConf);
	}//end for i
    }//end for t
    
    //Build and return the new DBC using the cases
    return new DataBaseCases("MergedDBC",vars,mergecases);
}//end method mergeCases for a Vector of DataBaseCases

/*---------------------------------------------------------------*/ 
/**
 * This method save all the used subsets in dbc files
 * @param basename base name used for the subsets
 */
public void saveSubsets(String basename) throws IOException, InvalidEditException, ParseException {

    for (int i=0;i< this.subsets.size();i++) {
	//Save the i DataBaseCase
        FileWriter  f = new FileWriter(basename+i+".dbc");
	DataBaseCases cases=(DataBaseCases)this.subsets.elementAt(i);
        cases.saveDataBase(f);
        f.close();
    }
}//en methos saveSubsets
/*---------------------------------------------------------------*/ 
/**
 * This method split the data set in two subsets (one for training 
 * other for test)
 * @param p training set proportion  (normally 2/3). The test set
 *          will be 1-p (normally 1 - 2/3 = 1/3)
 */
public void split2Cases(double p) throws InvalidEditException {

    
    //get the vars and cases from the DataBaseCases
    int i,j;
    int n=this.dbc.getNumberOfCases();
    NodeList vars=dbc.getVariables();
    CaseListMem cases=(CaseListMem)dbc.getCases();

    //create the training and test cases
    ContinuousCaseListMem dtrainingcases= new ContinuousCaseListMem(vars); //There is continuous vars.
    CaseListMem itrainingcases= new CaseListMem(vars); //We have only finite states var
    ContinuousCaseListMem dtestcases= new ContinuousCaseListMem(vars); //There is continuous vars.
    CaseListMem itestcases= new CaseListMem(vars); //We have only finite states vars

    //check if p greater than 0 and less than 1
    if ((p<=0) || (p>1.0)) {
	if (warnings) System.err.println("WARNING: Using a incorrect proportion for training. Using 2/3");
	p=(2.0/3.0);
    }

    //look for if we has Continuous vars
    boolean hasdoubles;
    if (cases instanceof ContinuousCaseListMem)  hasdoubles=true;
    else hasdoubles=false;

    //Get a ramdom permutation for a random selection of the sets
    int randorder[]=this.randPermutationInt(n);

    //build the training cases
    for (i=0;i<Math.round((double)n*p);i++) {

        
	//copy the random case i
	Vector v = new Vector(vars.size());
	for (j=0; j < vars.size(); j++) {
	    Double valor=new Double(cases.getValue(randorder[i],j));
	    
	    if ((vars.elementAt(j)).getTypeOfVariable() == Node.CONTINUOUS)
		v.add(valor);
	    else
		v.add(new Integer(valor.intValue()));
	}//end for j

	//add the case i to the training set
	if (hasdoubles)
	    dtrainingcases.put((Configuration)new ContinuousConfiguration(dtrainingcases.getVariables(), v));
	else 
	    itrainingcases.put(new Configuration(itrainingcases.getVariables(), v));

    }//end for i
    //set the number of cases of the CaseList
    itrainingcases.setNumberOfCases((int)Math.round((double)n*p));
    dtrainingcases.setNumberOfCases((int)Math.round((double)n*p));

    
    //build the test cases
    for (;i<n;i++) {
        
	//copy the random case i
	Vector v = new Vector(vars.size());
	for (j=0; j < vars.size(); j++) {
	    Double valor=new Double(cases.getValue(randorder[i],j));//drandomcase[j]);
	    if ((vars.elementAt(j)).getTypeOfVariable() == Node.CONTINUOUS)
		v.add(valor);
	    else
		v.add(new Integer(valor.intValue()));
	}//end for j

	//add the case i to the training set
	if (hasdoubles) 
	    dtestcases.put((Configuration)new ContinuousConfiguration(dtestcases.getVariables(), v));
	else 
	    itestcases.put(new Configuration(itestcases.getVariables(), v));

    }//end for i

    //set teh number of cases of the CaseList
    itestcases.setNumberOfCases(n-(int)Math.round((double)n*p));  
    dtestcases.setNumberOfCases(n-(int)Math.round((double)n*p));  


    //Build the new DBC using trainig and test cases
    DataBaseCases trainingdbc;
    DataBaseCases testdbc;
    if (hasdoubles) {
	trainingdbc=new DataBaseCases(this.dbc.getName()+"_training",vars,dtrainingcases);
	testdbc    =new DataBaseCases(this.dbc.getName()+"_test",vars,dtestcases);
    } else {
	trainingdbc=new DataBaseCases(this.dbc.getName()+"_training",vars,itrainingcases);
	testdbc    =new DataBaseCases(this.dbc.getName()+"_test",vars,itestcases);
    }

    //Add the training set
    this.subsets.add(trainingdbc);

    //Add the test set
    this.subsets.add(testdbc);

}//end method split2cases
/*---------------------------------------------------------------*/ 
/**
 * This method implements Trainig and Test validation method
 * @return Vector with the training confusion matrix and test confusion matrix
 */
public Vector trainAndTest() throws InvalidEditException {
    Vector result= new Vector();

    //Obtain the trainig and test sets if it's necessary
    if (this.method!=TRAINANDTEST) {
	this.clean();
	this.split2Cases(2.0/3.0);
    }
    
    DataBaseCases trainDB=((DataBaseCases)subsets.elementAt(0)).copy();
    
    DataBaseCases testDB=((DataBaseCases)subsets.elementAt(1)).copy();
    
    //Learn the classifier using the training set
    this.classifier.learn(trainDB,this.classvar);
    
    for (int ind=0; ind<this.classifier.getClass().getClasses().length; ind++)
        if (this.classifier.getClass().getClasses()[ind]==MixedClassifier.class){
               trainDB=((MixedClassifier)this.classifier).testDBCPreprocessing(trainDB);    
               testDB=((MixedClassifier)this.classifier).testDBCPreprocessing(testDB);
        }
    
    //Training error
    ConfusionMatrix trainerror=this.confusionMatrix(this.classifier,trainDB,trainDB.getVariables().getId(this.classVariable));

    //Test error
    ConfusionMatrix testerror=this.confusionMatrix(this.classifier,testDB,testDB.getVariables().getId(this.classVariable));

    //add the train and test confusion matrixes
    result.add(trainerror);
    result.add(testerror);

    //Indicate that TRain And Test method was used
    this.method=TRAINANDTEST;

    //return the confusion matrixes
    return result;
}//Train And Test Method
/*---------------------------------------------------------------*/ 
/**
 * This method implements k-fold cross-validation
 * @param k number of subsets that will be used in training and test
 * @return the average confusion matrix 
 */
public ConfusionMatrix kFoldCrossValidation (int k) throws InvalidEditException {
    int i;
    //    Vector matrixes= new Vector();

    //check if k is greater than the data cases
    int n=this.dbc.getNumberOfCases();
    if (k>n) {
	if (warnings) System.err.print("WARNING: Using a incorrect k="+k+" partitions for "+n+" cases.");
	k=5; 
	if (n<k) k=n;
	if (warnings) System.err.println(" Using k="+k);
    }

    //check if k is less than 0
    if (k<1) {
	if (warnings) System.err.println("WARNING: Using a incorrect k="+k+" partitions for "+n+" cases.");
	ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
	return result;
    }
    
    //Compute the confusion matrix for each partition
    Vector matrixes=kFoldCrossValidation_Vector (k);

    //compute the average confusion matrix
    AvancedConfusionMatrix result= new AvancedConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
    result.average(matrixes);

    //return the average confusion matrix 
    return result;

}//end kFoldCrossValidation method
/*---------------------------------------------------------------*/ 
/**
 * This method implements k-fold cross-validation
 * @param k number of subsets that will be used in training and test
 * @return the average confusion matrix 
 */
public ConfusionMatrix kFoldCrossValidationDiscriminative (int k) throws InvalidEditException {
    int i;
    //    Vector matrixes= new Vector();

    //check if k is greater than the data cases
    int n=this.dbc.getNumberOfCases();
    if (k>n) {
      if (warnings) System.err.print("WARNING: Using a incorrect k="+k+" partitions for "+n+" cases.");
        k=5; 
        if (n<k) k=n;
        if (warnings) System.err.println(" Using k="+k);
    }

    //check if k is less than 0
    if (k<1) {
      if (warnings) System.err.println("WARNING: Using a incorrect k="+k+" partitions for "+n+" cases.");
        ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));	
        return result;
    }
    
    //Compute the confusion matrix for each partition
    Vector matrixes=kFoldCrossValidationDiscriminative_Vector (k);

    //compute the average confusion matrix
    ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
    result.average(matrixes);

    //return the average confusion matrix 
    return result;

}//end kFoldCrossValidation method
/*---------------------------------------------------------------*/ 
/**
 * This method implements k-fold cross-validation. Returns the matrix sum of each fold confusion matrix.
 * @param k number of subsets that will be used in training and test
 * @return the matrix sum of the confusion matrix of each fold 
 */
public ConfusionMatrix kFoldSumCrossValidation (int k) throws InvalidEditException {
    //check if k is greater than the data cases
    int n=this.dbc.getNumberOfCases();
    if (k>n) {
      if (warnings) System.err.print("WARNING: Using a incorrect k="+k+" partitions for "+n+" cases.");
      k=5; 
      if (n<k) k=n;
      if (warnings) System.err.println(" Using k="+k);
    }
    //check if k is less than 0
    if (k<1) {
      if (warnings) System.err.println("WARNING: Using a incorrect k="+k+" partitions for "+n+" cases.");
      ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
      return result;
    }
    
    //Compute the confusion matrix for each partition
    Vector matrixes=kFoldCrossValidation_Vector (k);

    ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
    ConfusionMatrix aux;
    for (int i=0; i<matrixes.size(); i++)
    {
      aux = (ConfusionMatrix)matrixes.elementAt(i);
      for (int row=0; row<aux.getDimension(); row++)
        for (int column=0; column<aux.getDimension(); column++)
          for (int value=0; value < (int)aux.getValue(row, column); value++)
          result.actualize(row, column);
    }
    //return the sum confusion matrix 
    return result;
}//end kFoldSumCrossValidation method
/*---------------------------------------------------------------*/ 
/**
 * This method implements k-fold cross-validation. Return a vector
 * with the confusion matrix at each k partition.
 * @param k number of subsets that will be used in training and test
 * @return a Vector with a confusion matrix for each k partition
 */
public Vector kFoldCrossValidation_Vector (int k) throws InvalidEditException {
    int i;
    Vector matrixes= new Vector();

    //check if k is greater than the data cases
    int n=this.dbc.getNumberOfCases();
    if (k>n) {
	if (warnings) System.err.print("WARNING: Using a incorrect k="+k+" partitions for "+n+" cases.");
	k=5; 
	if (n<k) k=n;
	if (warnings) System.err.println(" Using k="+k);
    }

    //check if k is less than 0
    if (k<1) {
	if (warnings) System.err.println("WARNING: Using a incorrect k="+k+" partitions for "+n+" cases.");
	ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
	matrixes.add(result);
	return matrixes;
    }
    
    //create the k subsets for the method if it's necessary
    if ((this.method!=KFOLD) || (this.k!=k)) {
	this.clean();
        splitCases(k);
    }

    for (i=0; i < k; i++) {
	//use all the subsets excpet i for training
	DataBaseCases training=mergeCases(i).copy();

	//use the i subset por test
	DataBaseCases test=((DataBaseCases)this.subsets.elementAt(i)).copy();

	//Learn the classifier using the training set
	this.classifier.learn(training,this.classvar);

        for (int ind=0; ind<this.classifier.getClass().getClasses().length; ind++)
            if (this.classifier.getClass().getClasses()[ind]==MixedClassifier.class){
                   test=((MixedClassifier)this.classifier).testDBCPreprocessing(test);
            }

	//Test error for the k part
	ConfusionMatrix testerror=this.confusionMatrix(this.classifier,test,this.classvar);
	
	//Store the k errors
	matrixes.add(testerror);
    }

    //Indicate that K-Fold cross-validation method was used
    this.method=KFOLD; this.k=k;

    //return the Vector of confusion error matrixes
    return matrixes;

}//end kFoldCrossValidation_Vector method
/*---------------------------------------------------------------*/ 
/**
 * This method implements k-fold cross-validation. Return a vector
 * with the confusion matrix at each k partition.
 * @param k number of subsets that will be used in training and test
 * @return a Vector with a confusion matrix for each k partition
 */
public Vector kFoldCrossValidationDiscriminative_Vector (int k) throws InvalidEditException {
		
    int i;
    Vector matrixes= new Vector();

    //check if k is greater than the data cases
    int n=this.dbc.getNumberOfCases();
    if (k>n) {
      if (warnings) System.err.print("WARNING: Using a incorrect k="+k+" partitions for "+n+" cases.");
      k=5; 
      if (n<k) k=n;
      if (warnings) System.err.println(" Using k="+k);
    }

    //check if k is less than 0
    if (k<1) {
      if (warnings) System.err.println("WARNING: Using a incorrect k="+k+" partitions for "+n+" cases.");
      ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));	
      matrixes.add(result);
      return matrixes;
    }
    
    //create the k subsets for the method if it's necessary
    if ((this.method!=KFOLD) || (this.k!=k)) {
      this.clean();
      splitCases(k);
    }

    for (i=0; i < k; i++) {
      /*if(i%100 == 0)
      {
        System.out.println("Aprendizaje TM, ejecucion " + i);
      }*/
    //use all the subsets excpet i for training
    DataBaseCases training=mergeCases(i);
    //remplace the cases in the dataBaseCases which is in discriminativeClassifier by the test cases
    Vector vector           = training.getRelationList();
    Relation relation       = (Relation)vector.elementAt(0);
    CaseListMem caselistmem = (CaseListMem)relation.getValues();
    this.discriminativeClassifier.getDataBaseCases().replaceCases(caselistmem);
    this.discriminativeClassifier.getDataBaseCases().setNumberOfCases(training.getNumberOfCases());

    //use the i subset por test
    DataBaseCases test=(DataBaseCases)this.subsets.elementAt(i);
    
    
    

    //Learn the classifier with the TM algorithm using the training set    
    this.discriminativeClassifier.TM();
    
    //Test error for the k part
    ConfusionMatrix testerror=this.confusionMatrix(discriminativeClassifier,test,this.classvar);
	
    //Store the k errors
    matrixes.add(testerror);
  }

    //Indicate that K-Fold cross-validation method was used
    this.method=KFOLD; this.k=k;

    //return the Vector of confusion error matrixes
    return matrixes;

}//end kFoldCrossValidation_Vector method
/*---------------------------------------------------------------*/ 
/**
 * This method implements leave-one-out cross-validation method
 * @return the average confusion matrix
 */
public ConfusionMatrix leaveOneOut() throws InvalidEditException {
    int i;
    int k=this.dbc.getNumberOfCases();

    //check if k is less than 0
    if (k<1) {
	if (warnings) System.err.println("WARNING: Using "+k+" cases.");
	ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
	return result;
    }
    
    //Compute the confusion matrix for each partition
    Vector matrixes= leaveOneOut_Vector();

    //compute the average confusion matrix
    AvancedConfusionMatrix result= new AvancedConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
    result.average(matrixes);
    
    //return the average confusion matrix 
    return result;
}//end leaveoneout method
/*---------------------------------------------------------------*/ 
/**
 * This method implements leave-one-out cross-validation method. Returns the matrix sum of each instance confusion matrix.
 * @return the matrix sum of the confusion matrix for each instance 
 */
public ConfusionMatrix leaveOneOutSum() throws InvalidEditException {
    int k=this.dbc.getNumberOfCases();

    //check if k is less than 0
    if (k<1) {
      if (warnings) System.err.println("WARNING: Using "+k+" cases.");
    	ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
      return result;
    }
    
    //Compute the confusion matrix for each partition
    Vector matrixes= leaveOneOut_Vector();

    ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
    ConfusionMatrix aux;
    for (int i=0; i<matrixes.size(); i++)
    {
      aux = (ConfusionMatrix)matrixes.elementAt(i);
      for (int row=0; row<aux.getDimension(); row++)
        for (int column=0; column<aux.getDimension(); column++)
          for (int value=0; value < (int)aux.getValue(row, column); value++)
          result.actualize(row, column);
    }
    //return the sum confusion matrix 
    return result;
}//end leaveOneOutSum method
/*---------------------------------------------------------------*/ 
/**
 * This method implements leave-one-out cross-validation. Return a vector
 * with the confusion matrix at each partition.
 * @return a Vector with a confusion matrix for each partition
 */
public Vector leaveOneOut_Vector() throws InvalidEditException {
    int i;
    Vector matrixes= new Vector();
    int k=this.dbc.getNumberOfCases();

    //check if k is less than 0
    if (k<1) {
	if (warnings) System.err.println("WARNING: Using "+k+" cases.");
	ConfusionMatrix result= new ConfusionMatrix ((FiniteStates)dbc.getVariables().elementAt(this.classvar));
	matrixes.add(result);
	return matrixes;
    }

    
    //create the k subsets for the method if it's necessary
    if (this.method!=LEAVEONEOUT) {
	this.clean();
	splitCasesNoRandomly(k);
    }
    for (i=0; i < k; i++) {
        //System.out.println("LOO: "+i);
	//use all the subsetes excpet i for training
	DataBaseCases training=mergeCases(i).copy();

	//use the i subset por test
	DataBaseCases test=((DataBaseCases)this.subsets.elementAt(i)).copy();
	
	//Learn the classifier using the training set
	this.classifier.learn(training,this.classvar);

        for (int ind=0; ind<this.classifier.getClass().getClasses().length; ind++)
            if (this.classifier.getClass().getClasses()[ind]==MixedClassifier.class){
                   test=((MixedClassifier)this.classifier).testDBCPreprocessing(test);
            }

        //Test error for the k part
	ConfusionMatrix testerror=this.confusionMatrix(this.classifier,test,this.classvar);
	
	//Store the k errors
	matrixes.add(testerror);
    }

    //Indicate that Leave One Out Classification method was used
    this.method=LEAVEONEOUT;

    //return the Vector of confusion error matrixes
    return matrixes;
}//end leaveOneOut_Vector method

/*---------------------------------------------------------------*/ 
/**
 * This method compute the error rate for a classifier, categorizing 
 * the cases from a DataBaseCases object passed as argument
 * @param classifier the classifier to test
 * @param dbc the DataBaseCases to test
 * @param classnumber indicate the number of the variable to classify
 * @return the error rate
 */
public double error (Classifier classifier,  DataBaseCases dbc, int classnumber) {
    ConfusionMatrix cm=this.confusionMatrix (classifier,dbc,classnumber);
    return cm.getError();
}
/*---------------------------------------------------------------*/ 
/**
 * This method apply the Wilcoxon Paired Signed Rank Test, to 
 * two Vector of confusion matrixes (it uses the error stored in each
 * confusion matrix). It's useful when we compare two classification
 * methods. Whe can use only when the size of the samples (the size of 
 * the Vectors) it's between 6 and 25, both included.
 *
 * Source: Statistical Tables, 2�Ed., Rohlf FJ & Sokal, RR, W.: H. Freeman & Co. 1981
 *
 * @param list1 a Vector of confusion matrixes
 * @param list2 another Vector of confusion matrixes
 * @return the level of significance (it can be 0.05, 0.02 or 0.01) if the two methods are 
 *         significatively differents. I we can't say that if they are differents or not, 
 *         return -1.0;
 */
public double wilcoxonPairedSignedRankTest (Vector list1, Vector list2) {
    
    //check the preconditions
    if (list1.size() != list2.size()) {
	System.err.println ("ERROR: The sizes of the samples in Wilcoxon Paired Signed Rank Test are differents");
	return -1.0;
    }
    if ( (list1.size() < 6) || (list1.size() > 25) ) {
	System.err.println ("ERROR: The sizes ("+list1.size()+")of the samples in Wilcoxon Paired Signed Rank Test are out of range [6,25].");
	return -1.0;
    }
	
    // Significancy-->0.05 0.02 0.01    N
    int    table[][]= { {0 , 0 , 0 },// 6
			{2 , 0 , 0 },// 7
			{4 , 2 , 0 },// 8
			{6 , 3 , 2 },// 9
			{8 , 5 , 3 },// 10
			{11, 7 , 5 },// 11
			{14, 10, 7 },// 12
			{17, 13, 10},// 13
			{21, 16, 13},// 14
			{25, 20, 16},// 15
			{30, 24, 20},// 16
			{35, 28, 23},// 17
			{40, 33, 28},// 18
			{46, 38, 32},// 19
			{52, 43, 38},// 20
			{59, 49, 43},// 21
			{66, 56, 49},// 22
			{73, 62, 55},// 23
			{81, 69, 61},// 24
			{89, 77, 68} // 25
			};

    int i,j; //iterators
    
    Vector differences=new Vector(); //where we store the differences
    //int ranks[]=new int[list1.size()];  
    int Tplus;  //T+
    int Tminus; //T-
    int T;      // min(/T+/,/T-/)

    //Compute the differences
    int relevants=0;
    for (i=0;i < list1.size(); i++) {
	double diff=((ConfusionMatrix)list1.elementAt(i)).getError()-((ConfusionMatrix)list2.elementAt(i)).getError();
	if (diff!=0) relevants++;
	differences.add(new Double(diff));
    }

    //the two lists are the same or are out of range
    if (relevants<6) return -1;

    //sort the differences without the sign
    boolean swapped;
    do {
	swapped=false;
	for (j=0;j<differences.size()-1;j++)
	    if (Math.abs(((Double)differences.elementAt(j)).doubleValue()) > Math.abs(((Double)differences.elementAt(j+1)).doubleValue())) {
		swapped=true;
		Double aux= (Double)differences.elementAt(j);
		differences.setElementAt((Double)differences.elementAt(j+1),j);
		differences.setElementAt(aux,j+1);
	    }	
    }while (swapped);
    
    //compute the ranks
    double previous=Double.NaN;
    int start=0;
    double ranks[]=new double[differences.size()]; //where we store the ranks
    for (i=0;i<differences.size();i++) {
	if (Math.abs(((Double)differences.elementAt(i)).doubleValue()) == previous) {
	    double meanrank=((double)(start+i+2))/2.0;
	    for (j=start;j<=i;j++) 
		ranks[j]=(((Double)differences.elementAt(j)).doubleValue()>0)?meanrank:-meanrank;
	} else {
	    ranks[i]=(((Double)differences.elementAt(i)).doubleValue()>0)?(i+1):-(i+1);
	    previous = Math.abs(((Double)differences.elementAt(i)).doubleValue());
	    start=i;
	}
    }//end for i

    //Compute T+, T- and T
    Tplus=Tminus=T=0;
    for (i=0;i <ranks.length; i++)
	if (ranks[i]<0) Tminus+=Math.abs(ranks[i]);
        else Tplus+=ranks[i];
    T=(Tminus<Tplus)?Tminus:Tplus;

    //Look for T in the table, and set result 
    if (T<table[list1.size()-6][0]){
	if (T<table[list1.size()-6][1]) {
	    if (T<table[list1.size()-6][2]) {
		return 0.01;
	    } else return 0.02;
	} else return 0.05;
    } else return -1.0;

}//end wilcoxonPairedSignedRankTest method
/*---------------------------------------------------------------*/ 
/**
 * This method compute the confusion matrix for a classifier, categorizing 
 * the cases from a DataBaseCases object passed as argument
 * @param classifier the classifier to test
 * @param dbc the DataBaseCases to test
 * @param classnumber indicate the number of the variable to classify
 * @return a confusion matrix
 */
public ConfusionMatrix confusionMatrix (Classifier classifier,  DataBaseCases dbc, int classnumber) {
    int i,j;//iterators
    //AConfusionMatrix cm=new AConfusionMatrix((FiniteStates)dbc.getVariables().elementAt(classnumber));
    AvancedConfusionMatrix cm=new AvancedConfusionMatrix(this.classVariable);
    CaseListMem cases=(CaseListMem)dbc.getCases();

    //compute the confusion matrix for each case of the test set
    for (i=0;i<dbc.getNumberOfCases();i++) {
	//classify the i case
	Configuration instance=cases.get(i);

        Vector result=classifier.classify (instance, classnumber);
        cm.actualize(instance.getValue(this.classVariable),result);
    }
    
    //return the computed confusion matrix
   
    return cm;
}//end confusionMatrix method
/*---------------------------------------------------------------*/ 
/**
 * This method change the classifier used for validation. This it's util
 * when you can compare different clasifiers results with the same 
 * DataBaseCases sets.
 * @param c new Classifier tu use in validator
 */
public void  setClassifier(Classifier c) {
    this.classifier= c;
}//end classifier method

/*---------------------------------------------------------------*/ 
/**
 * This method clean the used subsets. It can be used when first use 
 * a validation method and next we want use another. Before used second
 * we must clean. 
 * @return Vector with the classification confusion matrix and test confusion matrix
 */
private void  clean() {
    this.subsets= new Vector();
}//end clean method

/**
 * This method return the 'subsets' field.
 */
public Vector getSubSets(){
    return this.subsets;
}

/*---------------------------------------------------------------*/ 
/**
 * For performing tests 
 */

public static void main(String args[]) throws IOException, InvalidEditException, ParseException,Exception{
      if (args.length < 1){
	  System.out.println("ERROR:Too few arguments.");
//          System.out.println("Use: file.dbc classnumber (the number of the variable to classify if it's the first use 0) [save_dbcs] (by default =0, the generated dbc files aren't saved)");
          System.out.println("Use: file.dbc [Options]");
          System.out.println("OPTIONS: ");
          System.out.println(" -cn <classnumber> -->  The number of the variable to classify. If it's the first use 0. By default: the last variable is set");
          System.out.println(" -cf <classifier> --> Two options:");   
          System.out.println("                                   nv -> Naive_Bayes classifier(Default).");
          System.out.println("                                   gnb -> Gaussian_Naive_Bayes classifier.");          
          System.out.println("                                   sgnb -> Selective_GaussianNaiveBayes classifier.");          
          System.out.println("                                   tan -> Gaussian TAN classifier.");          
          System.out.println("                                   stan -> Selective Gaussian TAN classifier.");                    
          System.out.println("                                   mte -> MTE Naive Bayes classifier.");                              
          System.out.println(" -fs <saveDBC> --> 0: The generate dbc files aren't saved (By default)");             
          System.out.println("                   1: The generate dbc files are saved.");             
          System.exit(0);
      }

      //Open the dbc
      FileInputStream f = new FileInputStream(args[0]);
      DataBaseCases cases = new DataBaseCases(f);
      f.close();

      //Inicializamos valores por defecto
      int classnumber=cases.getVariables().size()-1;
      int save=0;
      int classifier=0;
      
      for (int i=1; i<args.length; i++){
        
          if (args[i].equals("-cn")){
              classnumber=(Integer.valueOf(args[i+1])).intValue();
          }else if (args[i].equals("-fs")){
              save=(Integer.valueOf(args[i+1])).intValue();
          }else if (args[i].equals("-cf")){
              if (args[i+1].equals("nb")){
                  classifier=0;
              }else if (args[i+1].equals("gnb")){
                  classifier=1;
              }else if (args[i+1].equals("sgnb")){
                  classifier=2;
              }else if (args[i+1].equals("tan")){
                  classifier=3;
              }else if (args[i+1].equals("stan")){
                  classifier=4;
              }else if (args[i+1].equals("mte")){
                  classifier=5;
              }
          }
          
       }
      
      //Get the classnumber and the savesets option

      
      Classifier nb=null;
      //Use a classifier
      if (classifier==0){
        nb=(Classifier)new Naive_Bayes();
      }else if (classifier==1){
        nb=(Classifier)new Gaussian_Naive_Bayes(cases,false,classnumber);
      }else if (classifier==2){
        nb=(Classifier)new Selective_GNB(cases,false,classnumber);          
      }/*else if (classifier==3){
        nb=(Classifier)new MixedCMutInfTAN(cases,false,classnumber);          
      }else if (classifier==4){
        nb=(Classifier)new Selective_GTAN(cases,false,classnumber);          
      }else if (classifier==5){
        nb=(Classifier)new MTE_Naive_Bayes(cases,false,classnumber);          
      }*/
      
      //Use a validator
      ClassifierValidator validator=new ClassifierValidator(nb, cases, classnumber);

 
      //Run the validator
      Vector resultv=validator.trainAndTest();
      ConfusionMatrix cm=(ConfusionMatrix)resultv.elementAt(0);
      
      //Prints the confusion matrix and error
      //ConfusionMatrix cm=validator.kFoldCrossValidation(10);
      System.out.println("-----------------------------------------------------------");
      System.out.println("Training confusion matrix");
      cm.print();
      System.out.println("Trainig accuracy="+((1.0-cm.getError())*100.0)+" error:"+cm.getError()+" Variance:"+cm.getVariance());
      System.out.println("-loglikelikelihood. Media: "+ ((AvancedConfusionMatrix)cm).getLoglikelihood());      
      cm=(ConfusionMatrix)resultv.elementAt(1);
      System.out.println("\nTest confusion matrix");
      cm.print();
      System.out.println("Test accuracy="+((1.0-cm.getError())*100.0)+" error:"+cm.getError()+" Variance:"+cm.getVariance());
      System.out.println("-loglikelikelihood. Media: "+ ((AvancedConfusionMatrix)cm).getLoglikelihood());      
      if (save!=0) {
	  System.out.println("-----------------------------------------------------------");
	  System.out.println("Save the Test and Trainin .dbc sets with test_TandT prefix");
	  //save the subsets used for validation
	  validator.saveSubsets("test_TandT");
	  System.out.println("-----------------------------------------------------------");
      }
      System.out.println("-----------------------------------------------------------");
      System.out.println("\n\n\n\n-----------------------------------------------------------");


      //Run the validator again 
      int k=10;
      cm=validator.kFoldCrossValidation(k);
      //Prints the confusion matrix and error
      System.out.println("K-folds Cross-Validation (k="+k+") confusion matrix");
      cm.print();
      System.out.println("K-folds Cross-Validation (k="+k+") variance confusion matrix");
      cm.printVariance();
      System.out.println("K-folds Cross-Validation (k="+k+") accuracy="+((1.0-cm.getError())*100.0)+" error:"+cm.getError()+" Variance:"+cm.getVariance());
      System.out.println("-loglikelikelihood. Media: "+ ((AvancedConfusionMatrix)cm).getLoglikelihood()+" Variance: "+((AvancedConfusionMatrix)cm).getLoglikelihoodVariance()+"\n\n");
      if (save!=0) {
	  System.out.println("-----------------------------------------------------------");
	  System.out.println("Save the  .dbc sets with test_kf prefix");
	  //save the subsets used for validation
	  validator.saveSubsets("test_kf");
	  System.out.println("-----------------------------------------------------------");
      }
      System.out.println("-----------------------------------------------------------");
      System.out.println("\n\n\n\n-----------------------------------------------------------");


      //Run the validator again
      cm=validator.leaveOneOut();
      //Prints the confusion matrix and error
      System.out.println("Leave One Out confusion matrix");
      cm.print();
      System.out.println("Leave One Out variance confusion matrix");
      cm.printVariance();
      System.out.println("Leave One Out accuracy="+((1.0-cm.getError())*100.0)+" error:"+cm.getError()+" Variance:"+cm.getVariance());
      System.out.println("-loglikelikelihood. Media: "+ ((AvancedConfusionMatrix)cm).getLoglikelihood()+" Variance: "+((AvancedConfusionMatrix)cm).getLoglikelihoodVariance()+"\n\n");            
      if (save!=0) {
	  System.out.println("-----------------------------------------------------------");
	  System.out.println("Save the  .dbc sets with test_lvo prefix");
	  //save the subsets used for validation
	  validator.saveSubsets("test_lvo");
	  System.out.println("-----------------------------------------------------------");
      }
      System.out.println("-----------------------------------------------------------");

}//End main 

} // End of class ClassifierValidator
