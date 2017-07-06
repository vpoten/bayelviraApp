/* ClassificationTree.java */

package elvira.learning.classificationtree;

import java.io.*;
import java.util.Vector;
import java.util.Arrays;

import java.lang.Math;
import elvira.*;
import elvira.database.*;
import elvira.potential.*;
import elvira.parser.ParseException;
import elvira.learning.classification.SizeComparableClassifier;




/*---------------------------------------------------------------*/ 
/**
 * Implementation of Classification Tree using Probabilities Trees. Only work with Finite States Variables.
 * Supports the ID3 and C4.5 method, and a new method based in dirichlet distribution for probabilities.
 * The undefine value is -1.
 * @author J. G. Castellano (fjgc@decsai.ugr.es)
 * @since 12/11/2002
 */


public class ClassificationTree implements SizeComparableClassifier {
/* These constants are used to distinguish the classification method */

/** Constant used for ID3 method*/
  public static final int ID3= 0;
/** Constant used for C4.5 method*/
  public static final int C45= 1;
/** Constant used for Dirichlet method*/
  public static final int DIRICHLET= 2;

/* These constants are used to distinguish the prune method */
/** Constant used for no use prune method*/
  public static final int NONE= 0; 
/** Constant used for use Reduced Error Pruning method*/
  public static final int REP = 1;
/** Constant used for use Error Based Pruning method*/
  public static final int EBP = 2;

/**
  * Default confidence factor value for EBP
  */
  double confidencefactor;

/**
  * Default building method 
  */
  int buildmethod;

/**
  * Default pruning method
  */
  int prunemethod;

/**
  * Tree where the classification tree will be stored.
  */
  ProbabilityTree ctree;

/**
  * Number of attributes for the classification tree.
  */
  int attributesnumber;

/**
  * Number of cases for attributes and the class
  */
  int casesnumber;

/**
  * attributes vector with FiniteStates variables with the attributes
  */
  Vector attributes;

/**
  * FiniteStates variable with the class
  */
  FiniteStates classvar;

/**
  *values for the cases of the attributes from the learning set
  */
   int attributescases[][];

/**
  *classcases values for the cases of the class from the learning set
  */
   int classcases[];  


/**
  *the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
  */
   float dirichletfactor;

/*---------------------------------------------------------------*/ 
/**
 * Basic Constructor. 
 */
public ClassificationTree() {
    this.attributesnumber=0;
    this.casesnumber=0;

    //building tree default method
    this.buildmethod=C45;

    //Prune default method
    this.prunemethod=EBP;

    //Prune default params
    this.confidencefactor=0.25;
}//end basic ctor.
/*---------------------------------------------------------------*/ 
/**
 * In this contructor we indicate the building methos to use, the 
 * prune method and the confidence level if we use EBP pruning 
 * method, we ignore it otherwise.
 * @param bm default method to build the tree (ID3,C45, ...)
 * @param pm defatul method to prune the tree (NONE, REP, ...)
 * @param cf confidence level used in EBP
 */
public ClassificationTree(int bm, int pm, double cl) {
    this.attributesnumber=0;
    this.casesnumber=0;
    //building tree default method
    this.buildmethod=bm;

    //Prune default method
    this.prunemethod=pm;

    //Prune default params
    this.confidencefactor=cl;
}//end ctor. with params
/*---------------------------------------------------------------*/ 
/**
 * This method return the probability tree where the classification
 * @return a ProbabilityTree
 */
public ProbabilityTree getProbabilityTree(  ) {
    if ( isEmpty() ) {
	System.err.println("ERROR: The ClassificationTree is empty.");
    }
    return this.ctree.copy();
}//end method getProbabilityTree
/*---------------------------------------------------------------*/ 
/**
 * Determines whether the classification tree is empty or not.
 * @return true if the classification tree is empty and false otherwise.
 */
public boolean isEmpty() {
    /*    if ((attributesnumber==0) || (casesnumber==0))return true;
	  else*/ if (ctree.isEmpty()) return true;
    else return false;
}

/*---------------------------------------------------------------*/ 
/**
 * Prints a tree to the standard output.
 * @param j a tab factor (number of blank spaces before a child
 * is written).
 */
public void print( int j ) {
    if ( isEmpty() ) {
	System.err.println("ERROR: The ClassificationTrees is empty.");
	return ;
    }
    printRecursive( ctree, j );
    System.out.println("");
}//end method print

/*---------------------------------------------------------------*/ 
/**
 * Internal method that prints a tree recusively to the 
 * standard output.
 * @param tree a tree that must be printed
 * @param j a tab factor (number of blank spaces before a child
 * is written).
 */
private void printRecursive( ProbabilityTree tree, int j ) {
    int i,t;

    //check it's a tree with a empty node
    if (tree.isEmpty() ) {
	for (i=0; i <j; i++)  System.out.print(" ");
	System.err.println("Empty Node (�ERROR?)");
    //check it's a tree with a probability node (leaf node)
    } else if (tree.isProbab() ) {
	System.out.print("--");
	System.out.println(tree.getProb());
    //check it's a tree with a internal root node (var node)
    } else {
	System.out.println("");
	    for (t=0; t <j; t++)  {if ((t!=0)&&(j%t==0)) System.out.print("|"); else System.out.print(" ");}
	FiniteStates node=tree.getVar();
	System.out.println(node.getName());         
        int numchilds=(tree.getChild()).size();

	//prints recursively each child of this node
	for (i=0; i < numchilds; i++) {
	    for (t=0; t <j; t++)  {if ((t!=0)&&(j%t==0)) System.out.print("|"); else System.out.print(" ");}
	    System.out.print ("|-("+node.getName()+"="+node.getPrintableState(i)+")");
	    ProbabilityTree child=tree.getChild(i);
            printRecursive(child,j+j); 
	}//end for i
    }
}//end method printRecursive
/*---------------------------------------------------------------*/ 
/**
 * Computes the number of nodes used for the tree
 * @return the number of nodes for this classificatio tree
 */
public int numNodes( ) {
    if ( isEmpty() ) {
	System.err.println("ERROR: The ClassificationTree is empty.");
	return 0;
    }
    return numNodesRecursive( this.ctree);
}//end method print

/*---------------------------------------------------------------*/ 
/**
 * Internal method that return recursively the number of noded
 * for the given tree
 * @param tree a tree with the nodes
 * @return the number of nodes for the given classification tree
 */
private int numNodesRecursive( ProbabilityTree tree ) {
    int i;
    int numnodes=1; //The root node is the first node

    //check it's a tree with a empty node
    if (tree.isEmpty() ) return 0;
    //check it's a tree with a probability node (leaf node)
    else if (tree.isProbab() ) 	return 0;
    //check it's a tree with a internal root node (var node)
    else //get recursively the number of node for each child
	for (i=0; i < (tree.getChild()).size(); i++) 
            numnodes+=numNodesRecursive(tree.getChild(i)); 
    return numnodes;

}//end method numNodesRecursive
/*---------------------------------------------------------------*/ 
/**
 * Prints the classification tree rules to the standard output.
 */
public void printRules( ) {
    if ( isEmpty() ) {
	System.err.println("ERROR: The ClassificationTrees is empty. There is no rules");
	return ;
    }
    Vector v= new Vector (attributesnumber+3);
    printRulesRecursive(ctree, v);
    System.out.println("");
}//end method print

/*---------------------------------------------------------------*/ 
/**
 * Internal method that prints the classification tree rules recusively to the 
 * standard output.
 * @param tree The tree used to get the rules
 * @param parents A vector of strings with the nodes and its states or, that's the same, the path in the tree.
 */
    private void printRulesRecursive( ProbabilityTree tree, Vector parents) {
    int i;

    //check it's a tree with a empty node
    if (tree.isEmpty() ) {
	System.err.println("Empty Node (�ERROR?)");
    //check it's a tree with a probability node (leaf node). In that case prints the rule.
    } else if (tree.isProbab() ) {
	if (tree.getProb() == 0.0 ) return ;
	else {
	    for (i=0; i < parents.size(); i+=2)
		if (i == parents.size() -2) 
		    System.out.println (" ) then "+ "("+((String)parents.elementAt(i))+"="+((String)parents.elementAt(i+1))+")"+" with probability "+tree.getProb());
	        else if (i==0) 
		    System.out.print ("if ( ("+((String)parents.elementAt(i))+" == "+((String)parents.elementAt(i+1))+")");	        
		else		    
		    System.out.print (" && ("+((String)parents.elementAt(i))+" == "+((String)parents.elementAt(i+1))+")");
	}
    //check it's a tree with a internal root node (var node)
    } else {
	FiniteStates node=tree.getVar();
        int numchilds=(tree.getChild()).size();
	//prints recursively each child of this node
	for (i=0; i < numchilds; i++) {
	    parents.add(node.getName());
	    parents.add(node.getState(i));
	    ProbabilityTree child=tree.getChild(i);
            printRulesRecursive(child, parents); 
	    parents.remove(parents.size()-1);
	    parents.remove(parents.size()-1);
	}//end for i
    }

}//end method printRulesRecursive
/*---------------------------------------------------------------*/ 
/**
 * Save the classification tree rules.
 * @param fv the FileWriter for storing the rules.
 */
public void saveRules(FileWriter f) throws IOException {

    PrintWriter fv = new PrintWriter(f);

    if ( isEmpty() ) {
	System.err.println("ERROR: The ClassificationTrees is empty. There is no rules");
	return ;
    }
    Vector v= new Vector (attributesnumber+3);
    saveRulesRecursive(fv,ctree, v);
    fv.print("\n");
    fv.close();
}//end method saveRules

/*---------------------------------------------------------------*/ 
/**
 * Internal method that prints the classification tree rules recusively to the 
 * standard output.
 * @param fv the PrintWriter for storing the rules.
 * @param parents A vector of strings with the nodes and its states or, that's the same, the path in the tree.
 * @param tree The classsification tree used to save the rules
 */
 private void saveRulesRecursive(PrintWriter fv,  ProbabilityTree tree, Vector parents) {
    int i;
    //check it's a tree with a empty node
    if (tree.isEmpty() ) {
	System.err.println("Empty Node (�ERROR?)");
    //check it's a tree with a probability node (leaf node). In that case prints the rule.
    } else if (tree.isProbab() ) {
    	if (tree.getProb() == 0.0 ) return ;
    	for (i=0; i < parents.size(); i+=2){
    		if (i == parents.size() -2){
    			fv.print (" ) then "+ "("+((String)parents.elementAt(i))+"="+((String)parents.elementAt(i+1))+")"+" with probability "+tree.getProb()+"\n");
    		} else if (i==0){ 
    			fv.print ("if ( ("+((String)parents.elementAt(i))+" == "+((String)parents.elementAt(i+1))+")");	        
    		} else {		    
    			fv.print (" && ("+((String)parents.elementAt(i))+" == "+((String)parents.elementAt(i+1))+")");
    		}
    	}
    //check it's a tree with a internal root node (var node)
    } else {
	FiniteStates node=tree.getVar();
        int numchilds=(tree.getChild()).size();
	//prints recursively each child of this node
	for (i=0; i < numchilds; i++) {
	    parents.add(node.getName());
	    parents.add(node.getState(i));
	    ProbabilityTree child=tree.getChild(i);
            saveRulesRecursive(fv,child, parents); 
	    parents.remove(parents.size()-1);
	    parents.remove(parents.size()-1);
	}//end for i
    }

}//end method saveRulesRecursive
/*---------------------------------------------------------------*/ 
/**
 *  Uses the classification tree for classify a given case.
 *  @param instance case to classify
 *  @param classnumber number of the variable to classify
 *  @return a Vector with a probability associated to each class
 */
public Vector<Double> classify(Configuration instance, int classnumber) { 
    int i,j;

    int attributescase[]= new int[instance.size()-1]; 

    //Check if there is any continuous variable
    if (instance instanceof ContinuousConfiguration) {
	System.err.println("ERROR: Continuous values not supported.");
	return new Vector<Double>();
    }

    //Build the instance using the Configuration without the class
    //    attributescase[0]=instance.getValue(classnumber);
    for (i=0;i < instance.size();i++)
	if (i!=classnumber) {
	    if (i>classnumber) j=i-1;
	    else j=i;
	    attributescase[j]=instance.getValue(i);
	}
		
    //call my own classification function
    double result[]= classifies(attributescase); 

    //resturn the classification
    Vector<Double> vresult= new Vector<Double>();
    for (i=0;i<result.length;i++)
    	vresult.add(result[i]);
    return vresult;
}//end classify method
/*---------------------------------------------------------------*/ 
/**
 *  Uses the classification tree for classify a given case.
 *  @param attributescase values for one case of the attributes to classify
 *  @return A array with the probabilities for each class type for the given case
*/
public double[] classifies( int attributescase[]) { 

    double classes[]= new double[classvar.getNumStates()];

    //check the case 
    if (attributescase.length != attributes.size() ) {
       System.err.println("ERROR: The number of attributes is different in the case ("+attributescase.length+") and the tree ("+attributes.size()+").");
	    return classes;
    }

    //return the probabilities
    return classifiesRecursive(ctree, attributescase); 
}
/*---------------------------------------------------------------*/ 
/**
 *  Uses the classification tree for classify recursively a given 
 *  case.
 *  @param attributescase values for one case of the attributes to classify
 *  @param tree The classsification tree used to clasify the case
 *  @return A array with the probabilities for each class type for the given case
*/
private double [] classifiesRecursive( ProbabilityTree tree, int attributescase[]) { 
    int i,j;
    FiniteStates node;
    int attribute=-1;
    int numchilds=-1;
    String namenode;
    double classes[]= new double[classvar.getNumStates()];

    //if the tree is empty returns a empty array
    if ( tree.isEmpty() ) {
       System.err.println("ERROR: The tree is empty.");
       return classes;
    } else if (tree.isProbab() ) {
	//if the tree is a leaf node (probability node), there is a error. Return a empty array
       System.err.println("ERROR: There is a probability node and its parent isn't a class node.");
       return classes;
    }

    //Other case, it's a internal node. It can ba a class node or a attribute node
    node=tree.getVar();
    numchilds=(tree.getChild()).size();
    attribute=-1;
    namenode=node.getName();

    //We see if it's a class node
    if ( namenode==classvar.getName() ) {
	//In the node class, we build the classes vector with the probabilities
	for (i=0; i < numchilds; i++) {
	    ProbabilityTree child=tree.getChild(i);
	    if (child.isProbab()) 
		//add the probability
		classes[i]=child.getProb();
	    else {
		//if the child of the class Vector isn't a probability node, there is a error
		System.err.println("ERROR: The child of the class node isn't a probability node.");
		return classes;
	    }
	}//end for i
	//return the porbabilities of the clases
	return classes;
    } else {
	//if it isn the class then it's a attribute. Look for the number of attribute for this node
	attribute=-1;
	for (i=0; i < attributes.size(); i++) {
	    FiniteStates nodefn = (FiniteStates)attributes.elementAt(i);
	    if (nodefn.getName() == namenode) {
		attribute=i;
		break;
	    }
	}//end for i
	
	//check if the attribute is found
	if (attribute<0) {
            System.err.println("ERROR: The attribute doesn't exists.");
	    return classes;
	} else 	if (attribute>=attributescase.length)  {
	    //if the child attribute isn't of the class Vector isn't a probability node, there is a error
            System.err.println("ERROR: The case number of attributes doesn't match with the probability tree attributes number.");
	    return classes;
	} /*else 	if (attribute > numchilds) {
            System.err.println("ERROR: There is more attributes than childs in internal node: "+namenode+" .");
	    return classes;
	    }*/

	//See it the value of the cases is undefined
	if (attributescase[attribute] < 0) {
	    //the probabilities will be the average ot he probabilties for each path
	    for (i=0; i < numchilds; i++) {
		//Get probabilities for the child i
		ProbabilityTree child=tree.getChild(i);
		double classesTemp[]= new double[classvar.getNumStates()];
		classesTemp=classifiesRecursive(child, attributescase); 
		//add the new child probabilities
		for (j=0; j < classesTemp.length; j++)
		    classes[j]+=classesTemp[j];
	    }//end for i
	    //compute the average and return it
	    for (i=0; i < classes.length; i++)
		classes[i]= classes[i] / classes.length;
	    return classes;
	} else {
	    //Get the correct child for the attribute
	    ProbabilityTree child=tree.getChild(attributescase[attribute]);
	    //do a recursive call with the child
	    return classifiesRecursive(child, attributescase); 
	}//end if it's  undefined or not
    }//end if it's a class or a attribute node
}// end method classifiesRecursive
/*---------------------------------------------------------------*/ 
/**
 *  Prune the classification tree using Quinlan's C4.5 method. This
 *  method is Error Based Pruning.
 *  @param cf confidence level
 *  @return the error after prune
*/
public double prune ( double cf) { 
    //Do the prune recursively
    Vector result=pruneRecursive(ctree, cf,attributescases, classcases, attributes); 
    return ((Double)result.elementAt(2)).doubleValue();
}//end method prune
/*---------------------------------------------------------------*/ 
/**
 *  Prune the classification tree using Quinlan's C4.5 method. This
 *  method is Error Based Pruning.
 *  @param cf confidence level
 *  @return the error after prune
*/
public double errorBasedPruning ( double cf) { 
    //Do the prune recursively
    Vector result=pruneRecursive(ctree, cf,attributescases, classcases, attributes); 
    return ((Double)result.elementAt(2)).doubleValue();
}//end method errorBasedPruning
/*---------------------------------------------------------------*/ 
/**
 *  Prune the classification tree using Quinlan's REP method. This
 *  method is RE�educed Error Pruning.
 *  @return the error after prune
 */
public double reducedErrorPruning ( ) { 
    //Do the prune recursively
    Vector result=pruneErrorRecursive(ctree,attributescases, classcases, attributes); 
    return ((Double)result.elementAt(2)).doubleValue();
}//end method reducedErrorPruning
/*---------------------------------------------------------------*/ 
/**
 *  Prune the classification tree recursively (bottom-up method), using
 *  REP schema
 *  @param tree The classsification tree used to clasify the case
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param attributes vector with Finite States variable with the attributes
 *  @return a Vector with the tree classes, the tree elements and the tree error
*/
private Vector pruneErrorRecursive( ProbabilityTree tree, int attributescases[][], int classcases[], Vector attributes) { 
    int i,j;
    FiniteStates node;
    int attribute=-1;
    int numchilds=-1;
    String namenode;
    Vector errors=new Vector();
    double error;
    double classes[]= new double[classvar.getNumStates()];
    double max;
    int e,n;

    Arrays.fill(classes,0.0);

    //if the tree is empty, return a empty Vector
    if ( tree.isEmpty() ) {
       System.err.println("ERROR: The tree is empty.");
       return errors;
    } else if (tree.isProbab() ) {
	//if the tree is a leaf node (probability node), there is a error. Return a empty Vector
       System.err.println("ERROR: There is a probability node and its parent isn't a class node.");
       return errors;
    }

    //Other case, it's a internal node. It can be a class node or a attribute node
    node=tree.getVar();
    numchilds=(tree.getChild()).size();
    attribute=-1;
    namenode=node.getName();

    //We see if it's a class node
    if ( namenode==classvar.getName() ) {
	//In the node class, get the classes probabilities
	max=-1;
	for (i=0; i < numchilds; i++) {
	    ProbabilityTree child=tree.getChild(i);
	    if (child.isProbab()) {
		//add the probability
		classes[i]=child.getProb();
	    } else {
		//if the child of the class Vector isn't a probability node, there is a error
		System.err.println("ERROR: The child of the class node isn't a probability node.");
		return errors;
	    }
	}//end for i

	//Search for the class with more probability to estimate the error
	for (max=i=0; i < classes.length; i++)max=(max<classes[i])?classes[i]:max;
	if (attributescases.length>0) {
	    n=attributescases[0].length;
	    e=(int)Math.ceil( (1-max)*n); //The error probability is 1 - the prob of the best
	}
	else n=e=0;

	if (n==0) error=0;
	errors.add(classes);
	errors.add(new Integer(n));
	errors.add(new Double(e));
	
	//Returns the node/tree classes, the node/tree elements and the node/tree error 
	return errors;
    } else {
	error=0;
	e=0;
	double childerror=0;
	//if it isn the class then it's a attribute. Look for the number of attribute for this node
	attribute=-1;
	for (i=0; i < attributes.size(); i++) {
	    FiniteStates nodefn = (FiniteStates)attributes.elementAt(i);
	    if (nodefn.getName() == namenode) {
		attribute=i;
		break;
	    }
	}//end for i

	//Get the errors from the children
	for (i=0; i < numchilds; i++) {
		ProbabilityTree child=tree.getChild(i);

		//We build new subsets of cases for attributes and the class
		//without the attribute of the classification tree
		Vector subsets=buildSubsets(i, attributescases, classcases, attribute);
		int newclasscases[]=(int [])subsets.elementAt(0);
		int newattributescases[][]=(int [][])subsets.elementAt(1);
		Vector newattributes = (Vector)attributes.clone();
		newattributes.remove(attribute);
		
		//get the child i error
		Vector childresult=pruneErrorRecursive(child, newattributescases, newclasscases, newattributes); 
		double childclasses[]=(double [])childresult.elementAt(0);
		int nchild=((Integer)childresult.elementAt(1)).intValue();
		for (j=0;j<classes.length;j++) classes[j]+=(childclasses[j]*nchild);
		childerror+=((Double)childresult.elementAt(2)).doubleValue();
	}//end for i

	//Search for the class with more probability to estimate the error
	n=attributescases[0].length;
	for (j=0;j<classes.length;j++) classes[j]/=n;	
	for (max=i=0; i < classes.length; i++)max=(max<classes[i])?classes[i]:max;
        e=(int)Math.ceil( (1-max)*n);//The error probability is 1 - the prob of the best
	error=e;

	//compare the errors of the node and of the children, if the node error is smaller, prune
	if (error < childerror ) {
	    //prune all the children
	    tree.removeAllChildren();

	    //add a new leaf node
	    ProbabilityTree leaftree=addLeafNodes(tree,classcases, classvar, dirichletfactor);
	    tree.add(leaftree);
	}
	
	errors.add(classes);
	errors.add(new Integer(n));
	errors.add(new Double(error));

	//Returns the node/tree classes, the node/tree elements and the node/tree error 
	return errors;
    }//end if it's a class or a attribute node
}// end method pruneErrorRecursive

/*---------------------------------------------------------------*/ 
/**
 *  Prune the classification tree or only estimate the 
 *  classification error, recursively starting at the leaves
 *  using Quinlan's C4.5 prunning method.
 *  @param tree The classsification tree used to clasify the case
 *  @param cf confidence level
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param attributes vector with Finite States variable with the attributes
 *  @return a Vector with the tree classes, the tree elements and the tree error
*/
private Vector pruneRecursive( ProbabilityTree tree, double cf, int attributescases[][], int classcases[], Vector attributes) { 
    int i,j;
    FiniteStates node;
    int attribute=-1;
    int numchilds=-1;
    String namenode;
    Vector errors=new Vector();
    double error;
    double classes[]= new double[classvar.getNumStates()];
    double max;
    int e,n;

    Arrays.fill(classes,0.0);

    //if the tree is empty, return a empty Vector
    if ( tree.isEmpty() ) {
       System.err.println("ERROR: The tree is empty.");
       return errors;
    } else if (tree.isProbab() ) {
	//if the tree is a leaf node (probability node), there is a error. Return a empty Vector
       System.err.println("ERROR: There is a probability node and its parent isn't a class node.");
       return errors;
    }

    //Other case, it's a internal node. It can be a class node or a attribute node
    node=tree.getVar();
    numchilds=(tree.getChild()).size();
    attribute=-1;
    namenode=node.getName();

    //We see if it's a class node
    if ( namenode==classvar.getName() ) {
	//In the node class, get the classes probabilities
	max=-1;
	for (i=0; i < numchilds; i++) {
	    ProbabilityTree child=tree.getChild(i);
	    if (child.isProbab()) {
		//add the probability
		classes[i]=child.getProb();
	    } else {
		//if the child of the class Vector isn't a probability node, there is a error
		System.err.println("ERROR: The child of the class node isn't a probability node.");
		return errors;
	    }
	}//end for i

	//Search for the class with more probability to estimate the error
	for (max=i=0; i < classes.length; i++)max=(max<classes[i])?classes[i]:max;
	if (attributescases.length>0) {
	    n=attributescases[0].length;
	    e=(int)Math.ceil( (1-max)*n); //The error probability is 1 - the prob of the best
	}
	else n=e=0;

	if (n==0) error=0;
	else error=Ucf(n,e,cf)+e;
	errors.add(classes);
	errors.add(new Integer(n));
	errors.add(new Double(error));
	
	//Returns the node/tree classes, the node/tree elements and the node/tree error 
	return errors;
    } else {


	error=0;
	e=0;
	double childerror=0;
	//if it isn the class then it's a attribute. Look for the number of attribute for this node
	attribute=-1;
	for (i=0; i < attributes.size(); i++) {
	    FiniteStates nodefn = (FiniteStates)attributes.elementAt(i);
	    if (nodefn.getName() == namenode) {
		attribute=i;
		break;
	    }
	}//end for i

	//Get the errors form the children
	for (i=0; i < numchilds; i++) {
		ProbabilityTree child=tree.getChild(i);

		//We build new subsets of cases for attributes and the class
		//without the attribute of the classification tree
		Vector subsets=buildSubsets(i, attributescases, classcases, attribute);
		int newclasscases[]=(int [])subsets.elementAt(0);
		int newattributescases[][]=(int [][])subsets.elementAt(1);
		Vector newattributes = (Vector)attributes.clone();
		newattributes.remove(attribute);
		
		//get the child i error
		Vector childresult=pruneRecursive(child, cf,newattributescases, newclasscases, newattributes); 
		double childclasses[]=(double [])childresult.elementAt(0);
		int nchild=((Integer)childresult.elementAt(1)).intValue();
		for (j=0;j<classes.length;j++) classes[j]+=(childclasses[j]*nchild);
		childerror+=((Double)childresult.elementAt(2)).doubleValue();
	}//end for i

	//Search for the class with more probability to estimate the error
	n=attributescases[0].length;
	for (j=0;j<classes.length;j++) classes[j]/=n;	
	for (max=i=0; i < classes.length; i++)max=(max<classes[i])?classes[i]:max;
        e=(int)Math.ceil( (1-max)*n);//The error probability is 1 - the prob of the best
	error=Ucf(n,e,cf)+e;

	//compare the errors of the node and of the children, if the node error is smaller, prune
	if (error < childerror ) {
	    //prune all the children
	    tree.removeAllChildren();

	    //add a new leaf node
	    ProbabilityTree leaftree=addLeafNodes(tree,classcases, classvar, dirichletfactor);
	    tree.add(leaftree);
	}
	
	errors.add(classes);
	errors.add(new Integer(n));
	errors.add(new Double(error));

	//Returns the node/tree classes, the node/tree elements and the node/tree error 
	return errors;
    }//end if it's a class or a attribute node
}// end method pruneRecursive

/*---------------------------------------------------------------*/ 
/**
 *  Compute the number of missclassified elements divided by the 
 *  number of elements
 *  @return the classification error for the tree
*/
public double classificationError ( ) { 
    //Get the error 
    return testError(this.attributescases,this.classcases);
}//end method classificationError
/*---------------------------------------------------------------*/ 
/**
 *  Compute the number of missclassified elements for the 
 *  classification tree 
 *  @param tree The classsification tree used to clasify the cases
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param attributes vector with Finite States variable with the attributes
 *  @return the number of  missclassified elements for the given tree.
*/
private double missclassifiedElements( ProbabilityTree tree, int attributescases[][], int classcases[], Vector attributes) { 
    int i;
    FiniteStates node;
    int attribute=-1;
    int numchilds=-1;
    String namenode;
    double error;
    double max;
    int n;
    double classes[]= new double[classvar.getNumStates()];
    Arrays.fill(classes,0.0);



    //if the tree is empty, return a empty error
    if ( tree.isEmpty() ) {
       System.err.println("ERROR: The tree is empty.");
       return 0;
    } else if (tree.isProbab() ) {
	//if the tree is a leaf node (probability node), there is a error. Return a empty error
       System.err.println("ERROR: There is a probability node and its parent isn't a class node.");
       return 0;
    }

    //Other case, it's a internal node. It can ba a class node or a attribute node
    node=tree.getVar();
    numchilds=(tree.getChild()).size();
    attribute=-1;
    namenode=node.getName();

    //We see if it's a class node
    if ( namenode==classvar.getName() ) {
	//In the node class, get the classes probabilities
	max=-1;
	for (i=0; i < numchilds; i++) {
	    ProbabilityTree child=tree.getChild(i);
	    if (child.isProbab()) {
		//add the probability
		classes[i]=child.getProb();
	    } else {
		//if the child of the class isn't a probability node, there is a error
		System.err.println("ERROR: The child of the class node isn't a probability node.");
		return 0;
	    }
	}//end for i

	//search for the class with more probability to estimate the error
	for (max=i=0; i < classes.length; i++)max=(max<classes[i])?classes[i]:max;
	n=attributescases[0].length;
        error=(int)Math.ceil( (1-max)*n); //The error probability is 1 - the prob of the best
	
	//Return the misclassified elements
	return error;
    } else {

	error=0;
	//if it isn the class then it's a attribute. Look for the number of attribute for this node
	attribute=-1;
	for (i=0; i < attributes.size(); i++) {
	    FiniteStates nodefn = (FiniteStates)attributes.elementAt(i);
	    if (nodefn.getName() == namenode) {
		attribute=i;
		break;
	    }
	}//end for i

	error=0;
	//Get the errors from the children
	for (i=0; i < numchilds; i++) {
		ProbabilityTree child=tree.getChild(i);

		//We build new subsets of cases for attributes and the class
		//without the attribute of the classification tree
		Vector subsets=buildSubsets(i, attributescases, classcases, attribute);
		int newclasscases[]=(int [])subsets.elementAt(0);
		int newattributescases[][]=(int [][])subsets.elementAt(1);
		Vector newattributes = (Vector)attributes.clone();
		newattributes.remove(attribute);
		error+=missclassifiedElements(child, newattributescases, newclasscases, newattributes); 
	}//end for i

	//Return the misclassified elements
	return error;
    }//end if it's a class or a attribute node
}// end method missclassifiedElements

/*---------------------------------------------------------------*/ 
/**
 *  Compute the upper limit of the binomial distribution for the
 *  given confidence factor.  The coefficient is the  square of the 
 *  number of standard deviations corresponding to the selected 
 *  confidence level.  (Taken from "Documenta Geigy Scientific Tables" 
 *  (6th Edition), pag185 )			 
 *  @param N number of cases
 *  @param e number of cases missclassificated
 *  @param cf confidence factor
 *  @return the upper limit of the binomial distribution given the confidence factor
*/
private double Ucf(double N, double e, double cf) {

    double Coeff=0;
    double Val0, Pr;
    double Val[] = {  0,  0.001, 0.005, 0.01, 0.05, 0.10, 0.20, 0.40, 1.00};
    double Dev[] = {4.0,  3.09,  2.58,  2.33, 1.65, 1.28, 0.84, 0.25, 0.00};

    if ( Coeff == 0 ) {
	// Compute and retain the coefficient value, interpolating from the values in Val and Dev  
	int i = 0;
	while ( cf > Val[i] ) i++;
	Coeff = Dev[i-1] +  (Dev[i] - Dev[i-1]) * (cf - Val[i-1]) /(Val[i] - Val[i-1]);
	Coeff = Coeff * Coeff;
    }

    if ( e < 1E-6 ) {
	return N * (1 - Math.exp(Math.log(cf) / N));
    } else if ( e < 0.9999 ) {
	Val0 = N * (1 - Math.exp(Math.log(cf) / N));
	return Val0 + e * (Ucf(N, 1.0, cf) - Val0);
    } else if ( e + 0.5 >= N ) {
	return 0.67 * (N - e);
    } else {
	Pr = (e + 0.5 + Coeff/2 + Math.sqrt(Coeff * ((e + 0.5) * (1 - (e + 0.5)/N) + Coeff/4)) ) / (N + Coeff);
	return (N * Pr - e);
    }

}//end method ucf
/*---------------------------------------------------------------*/ 	
/**
 * In this method we build a new DataBaseCases from other changing
 * the position of a given variable. We change the cases positiion too.
 *  @param dbc original data base 
 *  @param oldposition position of the variable to move
 *  @param newposition new variable position after move it.
 *  @return new DataBaseCases with the var and its cases moved
*/

public DataBaseCases changeVariablePosition(DataBaseCases dbc, int oldposition,int newposition)  throws InvalidEditException {
    int i,j;

    //get the variables and the values form the database
    NodeList variables=dbc.getVariables();
    CaseList cases=dbc.getCases(); 
    boolean hasdoubles=false;

    //create the new NodeList and look for if we has Continuous vars
    Vector newnodes= new Vector();
    for (j=0;j<variables.size();j++) {
	//check the continuous values
	if ( (variables.elementAt(j)).getTypeOfVariable() == Node.CONTINUOUS )
	    hasdoubles=true;
	//Add a new var
	if (j!=oldposition) {
	    if (j==newposition)
		newnodes.add(variables.elementAt(oldposition));
	    newnodes.add(variables.elementAt(j));
	}
    }
    NodeList newvars= new NodeList (newnodes);
    CaseListMem mcases=(CaseListMem)cases;

    //Create the new cases
    ContinuousCaseListMem dnewcases= new ContinuousCaseListMem(newvars); //There is continuous vars.
    CaseListMem inewcases= new CaseListMem(newvars); //We have only finite states var

    //build the new cases changing the cases of the variable
    int newcases=0;
    for (i=0;i<cases.getNumberOfCases();i++) {
	//copy the case i
	Vector v = new Vector();
	for (j=0; j < variables.size(); j++) {
	    if (j!=oldposition){

		//lock if we insert the i,newposition value
		if (j==newposition) {
		    Double valor=new Double(mcases.getValue(i,oldposition));
		    if ((variables.elementAt(j)).getTypeOfVariable() == Node.CONTINUOUS)
			v.add(valor);
		    else
			v.add(new Integer(valor.intValue()));
		}

		//add the i,j value
		Double valor=new Double(mcases.getValue(i,j));
		if ((variables.elementAt(j)).getTypeOfVariable() == Node.CONTINUOUS)
		    v.add(valor);
		else
		    v.add(new Integer(valor.intValue()));
	    }
	}//end for j

	//add the case i to the new case list
	if (hasdoubles)
	    dnewcases.put((Configuration)new ContinuousConfiguration(dnewcases.getVariables(), v));
	else 
	    inewcases.put(new Configuration(inewcases.getVariables(), v));

	//a new case was added
	newcases++;
    }//end for i

    //set the number of cases of the CaseList
    inewcases.setNumberOfCases(newcases);
    dnewcases.setNumberOfCases(newcases); 

    //Build the new DBC 
    DataBaseCases newdbc;
    if (hasdoubles) newdbc=new DataBaseCases(dbc.getName(),newvars,dnewcases);
    else   	    newdbc=new DataBaseCases(dbc.getName(),newvars,inewcases);

    //return the builded DataBaseCases
    return newdbc;

}//end method changeVariable
/*---------------------------------------------------------------*/ 	
/** This method is used to build the classification tree.
 *  @param training training set to build the classifier
 *  @param classnumber number of the variable to classify
 */
public void learn (DataBaseCases training, int classnumber) {
    
	//check if the class is other than the first element, in that case build a new dbc,
    //changing the class position
    
	/**
	 * following 9 lines commented out by dalgaard
	 **/
	//if (classnumber!=0) {
	//try {
	//    training=changeVariablePosition(training,classnumber,0);
	//}
	//catch (InvalidEditException invalideditE){
	 //   System.err.println("ERROR: Invalid Edit Exception.");
	  //  System.exit(0);
	//}
    //}

    //Default values
    double  threshold=0.0;
    int deeplimit=128;
    int dirichletdeep=1;
    int classes=((FiniteStates)(training.getVariables()).elementAt(classnumber)).getNumStates();
    float dirichletfactor=(float)2.0/(float)classes;
    //build the tree
    switch (this.buildmethod) {
    case ID3:    
	this.id3C45Dirichlet( training,  classnumber, threshold , deeplimit, ID3,0,0);
	break;

    case C45: 
	this.id3C45Dirichlet( training,  classnumber, threshold , deeplimit,C45,0,0);
	break;

    case DIRICHLET: 
	this.id3C45Dirichlet( training,  classnumber, threshold , deeplimit,DIRICHLET,dirichletfactor,dirichletdeep);
	break;

    default: System.err.println("ERROR: Unknown building tree method used. Using C4.5");
	this.id3C45Dirichlet( training,  classnumber, threshold , deeplimit, C45,0,0);
	break;
    }

    //prune the tree
    switch (this.prunemethod) {
    case NONE: break;
    case REP: 
	this.reducedErrorPruning();
	break;
    case EBP: 	
	this.errorBasedPruning(this.confidencefactor);
	break;
    default: System.err.println("ERROR: Unknown prune method used. Using EBP (Error Based Pruning) CFR=0.25.");
	this.errorBasedPruning(0.25);
	break;
    }
    this.ctree.updateSize();
}//end learn method

/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3 algorithm for classify the values/variables from 
 *  a DataBaseCases object; the first variable is the class. By default,
 *  entropy threshold is 0 and maximun recursive deep is 128
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaards)
*/
public void id3(DataBaseCases cases, int classnumber) { 
    double  threshold=0.0;
    int deeplimit=128;    
    id3C45Dirichlet( cases, classnumber, threshold , deeplimit, ID3,0,0);
}//end method id3 DataBaseCases
/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3 algorithm for classify the values/variables from 
 *  a DataBaseCases object; the first variable is the class. By default,
 *  entropy threshold is 0 
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param classnumber - the index of the class variable in DataBaseCases cases (added by dalgaard)
 *  @param deeplimit maximun recursive deep 
*/
public void id3(DataBaseCases cases, int classnumber, int deeplimit) { 
    double  threshold=0.0;
    id3C45Dirichlet( cases,  classnumber, threshold , deeplimit,ID3,0,0);
}//end method id3 DataBaseCases, deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3 algorithm for classify the values/variables from 
 *  a DataBaseCases object; the first variable is the class. By default,
 *  maximun recursive deep is 128
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaard)
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
*/
public void id3(DataBaseCases cases,  int classnumber, double  threshold) { 
    int deeplimit=128;    
    id3C45Dirichlet( cases,  classnumber, threshold , deeplimit,ID3,0,0);
}//end method id3 DataBaseCases,thresold
/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3 algorithm for classify the values/variables from 
 *  a DataBaseCases object; the first variable is the class. 
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
 *  @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaard)
 *  @param deeplimit maximun recursive deep 
 *  @param method for use ID3 (0) or C4.5 (1)
*/
public void id3(DataBaseCases cases, int classnumber, double  threshold , int deeplimit) { 
     id3C45Dirichlet( cases, classnumber, threshold , deeplimit,ID3,0,0);
}//end method id3 DataBaseCases,thresold, deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the C4.5 algorithm for classify the values/variables from 
 *  a DataBaseCases object; the first variable is the class. By default,
 *  entropy threshold is 0 and maximun recursive deep is 128
 *  @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaard, it is adde as the first parameter 
 *              in order to break ambiguity with c45(cases, deeplimit))
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
*/
public void c45(int classnumber, DataBaseCases cases) { 
    double  threshold=0.0;
    int deeplimit=128;    
    id3C45Dirichlet( cases, classnumber, threshold , deeplimit,C45,0,0);
}//end method c4.5 DataBaseCases
/*---------------------------------------------------------------*/ 
/**
 *  Uses the C4.5 algorithm for classify the values/variables from 
 *  a DataBaseCases object; the first variable is the class. By default,
 *  entropy threshold is 0 
 *  
 *  @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaard, it is adde as the first parameter 
 *              in order to break ambiguity with c45(cases, deeplimit))
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param deeplimit maximun recursive deep 
*/
public void c45(int classnumber, DataBaseCases cases, int deeplimit) { 
    double  threshold=0.0;
    id3C45Dirichlet( cases,  classnumber, threshold , deeplimit,C45,0,0);
}//end method c4.5 DataBaseCases,deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the C4.5 algorithm for classify the values/variables from 
 *  a DataBaseCases object; the first variable is the class. By default,
 *  maximun recursive deep is 128
 *  
 *  @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaard, it is adde as the first parameter 
 *              in order to break ambiguity with c45(cases, deeplimit))
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
*/
public void c45(int classnumber, DataBaseCases cases,  double  threshold) { 
    int deeplimit=128;    
    id3C45Dirichlet( cases,  classnumber, threshold , deeplimit,C45,0,0);
}//end method c4.5 DataBaseCases,thresold
/*---------------------------------------------------------------*/ 
/**
 *  Uses the C4.5 algorithm for classify the values/variables from 
 *  a DataBaseCases object; the first variable is the class. 
 *  
 *  @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaard, it is adde as the first parameter 
 *              in order to break ambiguity with c45(cases, deeplimit))
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
 *  @param deeplimit maximun recursive deep 
*/
public void c45(int classnumber, DataBaseCases cases,  double  threshold , int deeplimit) { 
    id3C45Dirichlet( cases, classnumber, threshold , deeplimit,C45,0,0);
}//end method c4.5 DataBaseCases,thresold, deeplimit

/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3 algorithm for classify the values/variables from 
 *  two arrays with cases; the first variable is the class. By default,
 *  entropy threshold is 0 and maximun recursive deep is 128
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
*/
public void id3( int attributescases[][], int classcases[]) { 
    double  threshold=0.0;
    int deeplimit=128;    
    id3C45Dirichlet( attributescases, classcases,  threshold , deeplimit,ID3,0,0);
}//end method id3 arrays
/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3 algorithm for classify the values/variables from 
 *  two arrays with cases; the first variable is the class. By default,
 *  entropy threshold is 0 
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param deeplimit maximun recursive deep 
*/
public void id3( int attributescases[][], int classcases[], int deeplimit) { 
    double  threshold=0.0;
    id3C45Dirichlet( attributescases, classcases,  threshold , deeplimit,ID3,0,0);
}//end method id3 arrays,deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3 algorithm for classify the values/variables from 
 *  two arrays with cases; the first variable is the class. By default,
 *  maximun recursive deep is 128
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
*/
public void id3( int attributescases[][], int classcases[],  double  threshold) { 
    int deeplimit=128;    
    id3C45Dirichlet( attributescases, classcases, threshold , deeplimit,ID3,0,0);
}//end method id3 arrays,thresold
/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3 algorithm for classify the values/variables from 
 *  two arrays with cases; the first variable is the class. 
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
 *  @param deeplimit maximun recursive deep 

*/
public void id3( int attributescases[][], int classcases[],  double  threshold , int deeplimit) { 
    id3C45Dirichlet( attributescases, classcases,  threshold , deeplimit,ID3,0,0);
}//end method id3 arrays,thresold, deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the C4.5 algorithm for classify the values/variables from 
 *  two arrays with cases; the first variable is the class. By default,
 *  entropy threshold is 0 and maximun recursive deep is 128
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
*/
public void c45( int attributescases[][], int classcases[]) { 
    double  threshold=0.0;
    int deeplimit=128;    
    id3C45Dirichlet( attributescases, classcases,  threshold , deeplimit,C45,0,0);
}//end method c45 arrays
/*---------------------------------------------------------------*/ 
/**
 *  Uses the C4.5 algorithm for classify the values/variables from 
 *  two arrays with cases; the first variable is the class. By default,
 *  entropy threshold is 0 
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param deeplimit maximun recursive deep 
*/
public void c45( int attributescases[][], int classcases[], int deeplimit) { 
    double  threshold=0.0;
    id3C45Dirichlet( attributescases, classcases,  threshold , deeplimit,C45,0,0);
}//end method c45 arrays,deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the C4.5 algorithm for classify the values/variables from 
 *  two arrays with cases; the first variable is the class. By default,
 *  maximun recursive deep is 128
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
*/
public void c45( int attributescases[][], int classcases[],  double  threshold) { 
    int deeplimit=128;    
    id3C45Dirichlet( attributescases, classcases,  threshold , deeplimit,C45,0,0);
}//end method c45 arrays,thresold
/*---------------------------------------------------------------*/ 
/**
 *  Uses the C4.5 algorithm for classify the values/variables from 
 *  two arrays with cases; the first variable is the class. 
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
 *  @param deeplimit maximun recursive deep 
*/
public void c45( int attributescases[][], int classcases[],  double  threshold , int deeplimit) { 
    id3C45Dirichlet( attributescases, classcases, threshold , deeplimit,C45,0,0);
}//end method c45 arrays,thresold, deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the dirichlet distribution based algorithm for classify the 
 *  values/variables from a DataBaseCases object; the first variable is 
 *  the class. By default, entropy threshold is 0 and maximun recursive deep is 
 *  128. Use a dirichlet distribuition for compute the probabilities. The 
 *  diritchletfactor must be normally 2 divided by the nuber of classes; if use 0 no modifications are 
 *  made to the probabilities.
 *  
 *  @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaard, it is adde as the first parameter 
 *              in order to break ambiguities)
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
*/
public void dirichlet(int classnumber, DataBaseCases cases, float dirichletfactor, int dirichletdeep ) { 
    double  threshold=0.0;
    int deeplimit=128;    
    id3C45Dirichlet( cases,  classnumber, threshold , deeplimit,0,dirichletfactor,dirichletdeep);
}//end method dirichlet DataBaseCases
/*---------------------------------------------------------------*/ 
/**
 *  Uses the dirichlet distribution based algorithm for classify the 
 *  values/variables from a DataBaseCases object; the first variable is 
 *  the class. By default,
 *  entropy threshold is 0. Use a  dirichlet distribuition for compute 
 *  the probabilities. The diritchletfactor must be normally 2 divided by the nuber of classes; if 
 *  use 0 no modifications are  made to the probabilities. 
 *  
 *   @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaard, it is adde as the first parameter 
 *              in order to break ambiguities)
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param deeplimit maximun recursive deep 
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
*/
public void dirichlet(int classnumber, DataBaseCases cases, int deeplimit, float dirichletfactor, int dirichletdeep) { 
    double  threshold=0.0;
    id3C45Dirichlet( cases, classnumber, threshold , deeplimit,0,dirichletfactor,dirichletdeep);
}//end method dirichlet DataBaseCases, deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the dirichlet distribution based algorithm for classify the 
 *  values/variables from a DataBaseCases object; the first variable is 
 *  the class. By default,
 *  maximun recursive deep is 128. Use a  dirichlet distribuition for compute 
 *  the probabilities. The  diritchletfactor must be normally 2 divided by the nuber of classes; if 
 *  use 0 no modifications are  made to the probabilities.
 *  
 *  @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaard, it is adde as the first parameter 
 *              in order to break ambiguities)
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
*/
public void dirichlet(int classnumber, DataBaseCases cases,  double  threshold, float dirichletfactor, int dirichletdeep) { 
    int deeplimit=128;    
    id3C45Dirichlet( cases, classnumber, threshold , deeplimit,0,dirichletfactor,dirichletdeep);
}//end method dirichlet DataBaseCases,thresold
/*---------------------------------------------------------------*/ 
/**
 *  Uses the dirichlet distribution based algorithm for classify the 
 *  values/variables from a DataBaseCases object; the first variable is 
 *  the class. Use a dirichlet distribuition for compute the probabilities. The 
 *  diritchletfactor must be normally 2 divided by the nuber of classes; if use 0 no modifications are 
 *  made to the probabilities.
 *  @param classnumber - the index of the class variable in DataBaseCases 
 *  			cases (added by dalgaard, it is adde as the first parameter 
 *              in order to break ambiguities)
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
 *  @param deeplimit maximun recursive deep 
 *  @param method for use ID3 (0) or C4.5 (1)
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
*/
public void dirichlet(int classnumber, DataBaseCases cases,  double  threshold , int deeplimit, float dirichletfactor, int dirichletdeep) { 
     id3C45Dirichlet( cases, classnumber, threshold , deeplimit,0,dirichletfactor,dirichletdeep);
}//end method dirichlet DataBaseCases,thresold, deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the dirichlet distribution based algorithm for classify the 
 *  values/variables from a DataBaseCases object; the first variable is 
 *  the class. By default,
 *  entropy threshold is 0 and maximun recursive deep is 128. Use a 
 *  dirichlet distribuition for compute the probabilities. The 
 *  diritchletfactor must be normally 2 divided by the nuber of classes; if use 0 no modifications are 
 *  made to the probabilities.
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
*/
public void dirichlet( int attributescases[][], int classcases[], float dirichletfactor, int dirichletdeep) { 
    double  threshold=0.0;
    int deeplimit=128;    
    id3C45Dirichlet( attributescases, classcases,  threshold , deeplimit,0,dirichletfactor,dirichletdeep);
}//end method dirichlet arrays
/*---------------------------------------------------------------*/ 
/**
 *  Uses the dirichlet distribution based algorithm for classify the 
 *  values/variables from a DataBaseCases object; the first variable is 
 *  the class.  By default,
 *  entropy threshold is 0. Use a  dirichlet distribuition for compute the 
 *  probabilities. The  diritchletfactor must be normally 2 divided by the nuber of classes; if use 0 no 
 *  modifications are  made to the probabilities. 
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param deeplimit maximun recursive deep 
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
*/
public void dirichlet( int attributescases[][], int classcases[], int deeplimit, float dirichletfactor, int dirichletdeep) { 
    double  threshold=0.0;
    id3C45Dirichlet( attributescases, classcases,  threshold , deeplimit,0,dirichletfactor,dirichletdeep);
}//end method dirichlet arrays,deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the dirichlet distribution based algorithm for classify the 
 *  values/variables from a DataBaseCases object; the first variable is 
 *  the class.  By default,
 *  maximun recursive deep is 128. Use a dirichlet distribuition for compute 
 *  the probabilities. The  diritchletfactor must be normally 2 divided by the nuber of classes; if use 0 no 
 *  modifications are made to the probabilities.
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
*/
public void dirichlet( int attributescases[][], int classcases[],  double  threshold, float dirichletfactor, int dirichletdeep) { 
    int deeplimit=128;    
    id3C45Dirichlet( attributescases, classcases, threshold , deeplimit,0,dirichletfactor,dirichletdeep);
}//end method dirichlet arrays,thresold
/*---------------------------------------------------------------*/ 
/**
 *  Uses the dirichlet distribution based algorithm for classify the 
 *  values/variables from a DataBaseCases object; the first variable is 
 *  the class.  Use a 
 *  dirichlet distribuition for compute the probabilities. The 
 *  diritchletfactor must be normally 2 divided by the nuber of classes; if use 0 no modifications are 
 *  made to the probabilities.
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
 *  @param deeplimit maximun recursive deep 
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
*/
public void dirichlet( int attributescases[][], int classcases[],  double  threshold , int deeplimit, float dirichletfactor, int dirichletdeep) { 
    id3C45Dirichlet( attributescases, classcases,  threshold , deeplimit,0,dirichletfactor,dirichletdeep);
}//end method dirichlet arrays,thresold, deeplimit
/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3, C4.5 or the Dirichlet based algorithm for classify the values/variables from 
 *  a DataBaseCases object; the first variable is the class. 
 *  @param cases DataBaseCases object with the variables and values to 
 *               be classified
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
 *  @param classnumber the index of the classvariable in DataBaseCases cases (added by dalgaard)
 *  @param deeplimit maximun recursive deep 
 *  @param method for use ID3 (0) or C4.5 (1) 
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet method. The recommended value is 2/classes.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
*/
private void id3C45Dirichlet(DataBaseCases cases,  int classnumber, double  threshold , int deeplimit, int method, float dirichletfactor, int dirichletdeep) { 
    //Iterators
    int i,j;

    //Data structures to go through values and vars
    NodeList nodes=cases.getVariables(); 
    Vector vector=cases.getRelationList(); 
    Relation relation=(Relation)vector.elementAt(0);
    CaseListMem caselistmem=(CaseListMem)relation.getValues();
    Node node;
    FiniteStates nodefn;

    //Data structures for build the classification tree
    attributescases= new int [nodes.size()-1][caselistmem.getNumberOfCases()];
    classcases= new int [caselistmem.getNumberOfCases()];

    //Create new properties
    attributes= new Vector(nodes.size()-1);//FinitStates vector with the attributes
    /**
     *  following line uncommented by dalgaard
     */
    //classvar= new FiniteStates();

    //Build the arrays with the cases (array classes cases + array attributes cases)
    for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
	for (j=0 ; j< nodes.size()  ; j++) {
	    node =(Node)(caselistmem.getVariables()).elementAt(j);
	    if (node.getTypeOfVariable()==Node.CONTINUOUS) {
		System.err.println("ERROR: There is continuous values. First, use a Discretization method.");
		System.exit(0);
	    } 
	    if (j == classnumber){ 
	    	classcases[i]=(int)caselistmem.getValue(i,j);
	    } else { 
	    	int a = (j < classnumber ? j : j-1);
	    	attributescases[a][i]=(int)caselistmem.getValue(i,j);
	    }
	}//end for j
    }//end for i

    /**
     * Following 2 lines commented out and replaced by dalgaard 
     **/
    //Build the vectors  with the classes and the attributes
    //nodefn =(FiniteStates)(caselistmem.getVariables()).elementAt(0);
    //classvar=nodefn;
    classvar=(FiniteStates)(caselistmem.getVariables()).elementAt(classnumber);
    for (j=0 ; j< nodes.size()  ; j++) {
    	if(j==classnumber) continue;
    	nodefn =(FiniteStates)(caselistmem.getVariables()).elementAt(j);
    	attributes.add(nodefn);
    }//end for j

    //add the root node
    ctree = new ProbabilityTree();
    //inititate the Classification Tree fields
    attributesnumber=attributescases.length;
    casesnumber=classcases.length;
    this.dirichletfactor=dirichletfactor;


    //build the classification tree recursively
    //build the classification tree recursively
    if (dirichletfactor <= 0)
	id3c45Recursive(ctree, attributescases, classcases, attributes, classvar, threshold, 0 , deeplimit,method);
    else {
	int deepcut=dirichletRecursive(ctree, attributescases, classcases, attributes, classvar, threshold, 0 , deeplimit,dirichletfactor,0,dirichletdeep); 
	if (deepcut >=0) {
	    System.err.println("WARNING: The no pruned tree have one node");
	    ProbabilityTree leaftree=addLeafNodes(ctree,classcases, classvar, dirichletfactor);
	    ctree.add(leaftree);
	}
    }


}//end method id3C45Dirichlet for DataBaseCASES
/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3, C4.5 or the Dirichlet based algorithm for classify the values/variables from 
 *  two arrays, one for the attributes cases (2-dimensional) and the other for
 *  the class cases (1.dimensional).
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param threshold A thresold of the minimun entropy for the leaf nodes 
 *               of the classification tree
 *  @param deeplimit maximun recursive deep 
 *  @param method for use ID3 (0) or C4.5 (1)
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet method. The recommended value is 2/classes.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.

*/
private void id3C45Dirichlet( int attributescases[][], int classcases[],  double  threshold , int deeplimit, int method, float dirichletfactor, int dirichletdeep) { 
    //Iterators
    int i,j,t;

    //Data structures to go through values and vars
    int classesnumber=1;
    FiniteStates nodefn ;

    //Data structures for build the classification tree
    int attributesmaxvalues[]= new int [attributescases.length];
    int classmaxvalue=0;

    //Create new properties
    attributes= new Vector(attributescases.length);// FiniteStates Vector with the atiributes
    classvar= new FiniteStates();


    //Init the vars max values vectors
    for (j=0 ; j< attributescases.length  ; j++)  attributesmaxvalues[j]=0;

    //Build the arrays with the max cases
    for (i=0 ; i< attributescases.length; i++) 
	for (j=0 ; j< attributescases[0].length  ; j++) 
      		if (attributescases[i][j] > attributesmaxvalues[i] )  attributesmaxvalues[i]=attributescases[i][j];

    for (i=0 ; i< classcases.length; i++) 
	    	if (classcases[i] > classmaxvalue )  classmaxvalue=classcases[i];

    //Build the vector with the classes 
    classvar= new FiniteStates();
    classvar.setName("ClassificationVar");
    classvar.setNumStates(classmaxvalue+1);
    String states[]=new String[classvar.getNumStates()];
    for (j=0 ; j< classvar.getNumStates()  ; j++) states[j]=(""+j+"");
    classvar.setStates(states);

    //Build the vector with the attributes
    for (j=0 ; j< attributescases.length  ; j++) {
	//new FiniteStates Attribute 
	nodefn = new FiniteStates();
	nodefn.setName(("Attribute"+j));
	//set states of the attribute
	nodefn.setNumStates(attributesmaxvalues[j]+1);
	String states2[]=new String[nodefn.getNumStates()];
	for (t=0 ; t< nodefn.getNumStates() ; t++) states2[t]=(""+t+"");
	nodefn.setStates(states2);
	//add the attribute
        attributes.add(nodefn);
    }//end for j

    //add the root node
    ctree = new ProbabilityTree();
    //inititate the Classification Tree fields
    attributesnumber=attributescases.length;
    casesnumber=classcases.length;
    this.dirichletfactor=dirichletfactor;
    this.attributescases= new int [attributescases.length][attributescases[0].length];
    this.classcases= new int [classcases.length];
    for (i=0; i < attributescases.length; i++)
	for (j=0; j < attributescases[0].length; j++)
	    this.attributescases[i][j]=attributescases[i][j];
    for (i=0; i < classcases.length; i++)
	this.classcases[i]=classcases[i];

    //build the classification tree recursively
    if (dirichletfactor <= 0)
	id3c45Recursive(ctree, attributescases, classcases, attributes, classvar, threshold, 0 , deeplimit,method); 
    else {
	int deepcut=dirichletRecursive(ctree, attributescases, classcases, attributes, classvar, threshold, 0 , deeplimit,dirichletfactor,0,dirichletdeep);
	if (deepcut >=0) {
	    System.err.println("WARNING: The no pruned tree have one node");
	    ProbabilityTree leaftree=addLeafNodes(ctree,classcases, classvar, dirichletfactor);
	    ctree.add(leaftree);
	}
    }
	 
}//end method id3C45Dirichlet for arrays
/*---------------------------------------------------------------*/ 
/**
 *  Uses a new database to test the classification tree
 *  @param cases DataBaseCases object with the variables and values to 
 *               be tested
 *  @return the test error for the given DataBaseCases object
*/
public double testError(DataBaseCases cases) {
    //Iterators
    int i,j;

    //Data structures to go through values and vars
    NodeList nodes=cases.getVariables(); 
    Vector vector=cases.getRelationList(); 
    Relation relation=(Relation)vector.elementAt(0);
    CaseListMem caselistmem=(CaseListMem)relation.getValues();
    Node node;

    //Data structures for test the classification tree
    int attributescases[][]= new int [nodes.size()-1][caselistmem.getNumberOfCases()];
    int classcases[]= new int [caselistmem.getNumberOfCases()];


    //Build the arrays with the cases (array classes cases + array attributes cases)
    for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
	for (j=0 ; j< nodes.size()  ; j++) {
	    node =(Node)(caselistmem.getVariables()).elementAt(j);
	    if (node.getTypeOfVariable()==Node.CONTINUOUS) {
		System.err.println("ERROR: There is continuous values. First, use a Discretization method.");
		System.exit(0);
            //The first var is the class
	    } else if (j < 1 ) {
		classcases[i]=(int)caselistmem.getValue(i,j);
            //The rest vars are attributes
	    } else {
		attributescases[j-1][i]=(int)caselistmem.getValue(i,j);
	    }
	}//end for j
    }//end for i

    return testError(attributescases,classcases);

}//end method testError for DataBaseCases
/*---------------------------------------------------------------*/ 
/**
 *  Uses a new database to test the classification tree
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @return the test error for the given arrays
*/
public double testError( int attributescases[][], int classcases[]) {
    //Iterators
    int i,j,max;
    double errors=0;    

    //do the classfication for the cases
    for (i=0;i<classcases.length;i++) {
	//classifies a case
	int attributescase[]= new int[attributescases.length];
	for (j=0;j<attributescases.length; j++)
	    attributescase[j]=attributescases[j][i];
	double classes[]=classifies(attributescase);

	//get max class
	for (max=j=0; j < classes.length; j++) max=(classes[max]<classes[j])?j:max;
	//check if the predicted class is the correct
	if (max!=classcases[i]) errors++;
    }
    return (errors/classcases.length);
}//end method testError for arrays
/*---------------------------------------------------------------*/ 
/**  Add the class node and a child por each class value with 
 * the probability
 *  @param tree root tree for the new nodes
 *  @param classescases values for the cases of the class 
 *  @param clases Finite States variable with the class
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @return the tree with the leaf nodes
 */
private ProbabilityTree addLeafNodes (ProbabilityTree tree, int classcases[], FiniteStates classvar, float dirichletfactor) {

   int j;
   Vector classesprob=probabilitiesOne(classcases, dirichletfactor);

   //add the i class child 
   tree.assignVar(classvar); 

   //build the child i subtree
   int numstates=classvar.getNumStates();

   //Build a probability node for each value of the class
   for (j=0; j < numstates; j++) {
       String statename=classvar.getState(j);
       int state=classvar.getId(statename);

       //Look for lost states of the class
       if ( ((double [])classesprob.elementAt(0)).length <= state ) {
              (tree.getChild(j)).assignProb( 0.00 );
       }else {
              (tree.getChild(j)).assignProb( ((double [])classesprob.elementAt(0))[state] );
       }
   }

   return tree;
}//end method addLeafNodes
/*---------------------------------------------------------------*/ 
/** Check if all the values of the class are the same, in this case
 *  return true. If any different values, returns false.
 *  @param classcases values for the cases of the class 
 *  @return this method return if all the cases for the class are the same
 */
private boolean haveEqualElements (int classcases[]) {
    int i;
    for (i=1; i < classcases.length; i++) 
	    if (classcases[i-1]!=classcases[i]) return false;
    return true;
}
/*---------------------------------------------------------------*/ 
/**
 *  Stop condition for the ID3, c45 and dirichlet algorithms, the stop conditions are:
 *  -Maximun recursive deep is reached
 *  -All values for the classes are the same
 *  -Entropy below a thresold
 *  -Indecidible case, have only one attribute
 *  @param tree root node for the actual tree
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param attributes vector with Finite States variable with the attributes
 *  @param classvar finite States variable with the class
 *  @param entropy entropy for the actual root node
 *  @param threshold a thresold of the minimun entropy for the leaf nodes of the classification tree
 *  @param deep actual recursive deep 
 *  @param deeplimit maximun recursive deep 
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @return this method return if stop building the tree
*/
private boolean stopId3C45Dirichlet (ProbabilityTree tree, 
			 int attributescases[][], int classcases[],
			 Vector attributes, FiniteStates  classvar, 
			 double entropy,  double threshold, int deep , int deeplimit,float dirichletfactor) {

    //First stop condition: Maximun recursive deep is reached
    if ( deep > deeplimit ) { 
	ProbabilityTree leaftree=addLeafNodes(tree,classcases, classvar, dirichletfactor);
	tree.add(leaftree);
	return true; }

    //Second stop condition: All values for the classes are the same
    if ( haveEqualElements(classcases)) { 
	ProbabilityTree leaftree=addLeafNodes(tree,classcases, classvar, dirichletfactor);
	tree.add(leaftree);
	return true; }

    //Fourthd stop condition: Entropy below a thresold.
    if ( entropy < threshold ) { 
	ProbabilityTree leaftree=addLeafNodes(tree,classcases, classvar, dirichletfactor);
	tree.add(leaftree);
	return true; }

    //Third stop condition: Indecidible case:  There is no attributes
    if ( attributes.size()==0) { 
	ProbabilityTree leaftree=addLeafNodes(tree,classcases, classvar, dirichletfactor);
	tree.add(leaftree);
	return true; }
    
    //We continue building the tree
     return false;
}//end method stopID3C45Dirichlet
/*---------------------------------------------------------------*/ 
/**
 *  Stop condition only for dirichlet distribution base method. 
 *  The stop condition is when there isn't a information gain 
 *  at the best child of the actual tree
 *  @param tree root node for the actual tree
 *  @param classcases values for the cases of the class 
 *  @param classvar finite States variable with the class
 *  @param entropy entropy for the actual root nod
 *  @param minchildentropy minimun entropy for a child
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @param actualdirichletdeep actual depth of worse child entropies.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
 *  @return this method return the deep of worse entropies child. return -1 if stop building the tree
*/
private int stopDirichlet (ProbabilityTree tree, int classcases[], FiniteStates  classvar, 
			   double entropy, double minchildentropy, float dirichletfactor, int actualdirichletdeep, 
			   int dirichletdeep) {
    if ( entropy < minchildentropy) { 
        //Dirichlet stop condition: There is no information gain after "dirichletdeep" level of worse child
	if (actualdirichletdeep==dirichletdeep) {
	    // ProbabilityTree leaftree=addLeafNodes(tree,classcases, classvar, dirichletfactor);
	    //tree.add(leaftree);
	    return -1; 
	} else {
	    //new level of worse solutions
	    actualdirichletdeep++;
	    return actualdirichletdeep;
	}
    }
    else
	//We continue building the tree, we still have better child's entropies
	return 0;
}//end method stopDirichlet
/*---------------------------------------------------------------*/ 
/**
 *  Computes the probability of each value for a set of cases
 *  @param attributes vector with Finite States variable with the attributes
 *  @param varscases values for the cases of the variables
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @return a Vector with  probabilities arrays for each value and each variable. The first is the probability for undef values.
*/
private Vector probabilities (Vector attributes, int varscases[][], float dirichletfactor) {
    int i,j;
    Vector prob = new Vector (attributes.size());
    double undefprob[]=new double [attributes.size()];

    for (i=0; i < varscases.length; i++) {
	//The freq array will be a hash table where the var value is the key and the value is the probability
	double freq[]=new double [((FiniteStates)attributes.elementAt(i)).getNumStates()];

	//Initiate freq to dirichlet factor (in only ID3 o C4.5 is zero)
	for (j=0;j<((FiniteStates)attributes.elementAt(i)).getNumStates();j++) freq[j]=dirichletfactor; 
	undefprob[i]=0; 

	//Determine the frequencies for eacch var value
        for (j=0;j<(varscases[i]).length;j++) 
	    if (varscases[i][j] >= 0) //ignoring the undefined values (in Finite States the undef values are negative)
		freq[varscases[i][j]]++;
	    else
		undefprob[i]++;

	//Computes the poobabilities using the frequencies and the dirichlet factor
	if (dirichletfactor == 0)
            for (j=0;j<((FiniteStates)attributes.elementAt(i)).getNumStates();j++) 
		freq[j]=freq[j]/( (varscases[0]).length );
	else
	    for (j=0;j<((FiniteStates)attributes.elementAt(i)).getNumStates();j++) 
		freq[j]=freq[j]/( (varscases[0]).length + (((FiniteStates)attributes.elementAt(i)).getNumStates() * dirichletfactor) );
	undefprob[i]=undefprob[i]/( (varscases[0]).length );

	//add the new probabilities array for var i
	prob.add(freq);
    }//end for i

    //add frequencies for undefined values
    prob.add(undefprob);

    //return the probabilties Vector
    return prob;
}//end method probabilities
/*---------------------------------------------------------------*/ 
/**
 *  Computes the probability of each value for a one var
 *  @param varcases values for the cases of the variable
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @return a Vector with a  probabilities array for each value of the variable
*/

private Vector probabilitiesOne (int varcases[], float dirichletfactor) {
    int j;
    Vector prob = new Vector (1);

    //The freq array will be a hash table where the var value is the key and the value is the probability
    double freq[]=new double [classvar.getNumStates()];
    //Initiate freq to dirichlet factor (in only ID3 o C4.5 is zero)
    for (j=0;j<classvar.getNumStates();j++) freq[j]=dirichletfactor; 

    //Determine the frequencies for each var value
    for (j=0;j<varcases.length;j++) 
	if (varcases[j] >= 0) //ignoring the undefined values (in Finite States the undef values are negative)
	    freq[varcases[j]]++;


    //Computes the probabilities using the frequencies and the dirichlet factor
    if (dirichletfactor == 0) 
	for (j=0;j<classvar.getNumStates();j++) {
	      freq[j]=freq[j]/( varcases.length );
	      if (varcases.length==0) freq[j]=0;
	}    
    else
	for (j=0;j<classvar.getNumStates();j++) freq[j]=freq[j]/( varcases.length + (classvar.getNumStates() * dirichletfactor) );

    //add the new probabilities array for var i
    prob.add(freq);

    //return the probabilties Vector
    return prob;
}//end method probabilitiesOne

/*---------------------------------------------------------------*/ 
/**
 *  Calculates the entropy of a set of probabilities
 *  @param classesprob a vector with a probabilities array for the class
 *  @return the entropy computed
*/

private double thisLevelEntropy (Vector classesprob) {

    int i,j;
    double entropy=0;

    for (i=0; i < classesprob.size(); i++) {
	double prob[]= (double [])classesprob.elementAt(i);
	for (j=0; j < prob.length; j++)
	    if (prob[j] != 0)
		entropy+=prob[j]*(Math.log(1/prob[j]))/(Math.log(2)); //p*log2 (1/p)=p*( (log 1/p) / log 2 )
    }
    return entropy;
}//end method thisLeveLEntropy

/*---------------------------------------------------------------*/ 
/**
 *  Computes the entropy for the next level, when build a subtree with 
 *  a attribute. Computes the conditional entropy.
 *  @param attribute attribute that will be the root node for the next level
 *  @param attributes finite States variable with the attributes
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @return the conditional entropy for the next level using a attribute as root node
*/

private double [] nextLevelEntropy (int attribute, Vector attributes, int attributescases[][], 
				    int classcases[],  float dirichletfactor) {
    int i,j,u=-1,size=-1;
    double entropy=0;
    double entropies[]= new double [((FiniteStates)attributes.elementAt(attribute)).getNumStates()];

    //Initiate the entropies vector for the nex level
    for (i=0; i < entropies.length; i++) entropies[i]=0;

    //for each value of the attribute, build the subset without that attribute anda compute its entropy
    for (i=0; i < ((FiniteStates)attributes.elementAt(attribute)).getNumStates(); i++) {
	    //Determine the size of the new subset of the values without the attribute cases
	    for (j=0,size=0;j < attributescases[0].length; j++) 
		if (attributescases[attribute][j] == i) size++;

	    //Build the subset of the values without the attribute cases
	    int newclasscases[]= new int [size];
	    for (u=0,j=0;j < attributescases[0].length; j++) {
		if (attributescases[attribute][j] == i) {
			newclasscases[u]=classcases[j];
		    u++;
		}
	    }//end for j

	    //calculate the entropy for the subset
	    Vector classesprob=probabilitiesOne(newclasscases, dirichletfactor);
	    entropy=thisLevelEntropy(classesprob);
	entropies[i]=entropy;
    }//end for i
    return entropies;
}//end method nextLeveLEntropy
/*---------------------------------------------------------------*/ 
/**
 *  Computes the split information for the given attribute
 *  @param attributes Finite States variable with the attribute
 *  @param prob contional probabilities for compute the split info
 *  @param undefprob conditional probabilities fo undefined values
 *  @return the split info for the given probabilities
*/
private double splitInfo (FiniteStates attribute, double prob[], double undefprob) {
    int i;
    double splitinfo=0;
    //Add the split info 
    for (i=0; i < attribute.getNumStates(); i++)
	if (prob[i]!=0)
	    splitinfo+=prob[i] * ( Math.log(1/prob[i]) / Math.log(2) ); //p*log2 (1/p)= p * ( log(1/p) / log(2) )

    //Add the split info for undef values
    if (undefprob!=0)
	splitinfo+=undefprob * ( Math.log(1/undefprob) / Math.log(2) ); 

    //when the splitinfo is equal to zero, all the prob are zero, so are impossible. Gain alwaus >= 0, in this case
    //split info will be -1, then the gain ratio will be lesser.
    if (splitinfo==0) return -1;

    return splitinfo;
}//end method splitInfo

/*---------------------------------------------------------------*/ 
/**
 * We build new subsets of cases values for attributes and the class
 * without the attribute with max gain, but with the cases where appear the value "value"
 * of that attribute 
 *  @param value value of the attribute with max gain
 *  @param attributescases values for the cases of the attributes
 *  @param classescases values for the cases of the class 
 *  @param maxgain attribute with maximum porfit
 *  @return a Vector with the new subsets ( newclascases, newattributescases)
*/
private Vector buildSubsets(int value, int attributescases[][], int classcases[], int maxgain) {
    int j,t,u,v,w,size;

    Vector subsets=new Vector();

    //We build new subsets of cases for vattributes and the class
    //without for the attribute with max gain
    int newclasscases[]=new int [0];
    int newattributescases[][]=new int [0][0];


    //Computes the size of the new cases subset for attributes
    for (j=0,size=0;j < attributescases[0].length; j++) 
	if (attributescases[maxgain][j] == value) size++; 

    //Build the new cases subset for attributes and the class
    newclasscases= new int [size];
    newattributescases= new int [attributescases.length-1][size];
    for (v=0,u=0,j=0;j < attributescases[0].length; j++) {
	if (attributescases[maxgain][j] == value) {
	    //Build the new cases subset for the class
	    newclasscases[u]=classcases[j];
	    u++;

	    //Build the new cases subset for the attributes 
	    for (t=0;t<attributescases.length; t++) {
		if (t==maxgain) continue;
		else if (t>maxgain) w=t-1;
		else w=t;
		newattributescases[w][v]=attributescases[t][j];
	    }//end for t
	    v++;
	}//end if
    }//end for j;

    //Add the new subsets to the output vector
    subsets.add(newclasscases);
    subsets.add(newattributescases);

    //return the Vector With the subsets
    return subsets;

}//end method buildSubsetes
/*---------------------------------------------------------------*/ 
/**
 *  This method computes the gain ratio for a set of variables
 *  @param dbc DataBaseCases with the variables and the values
 *  @param classnumber number of the variable to classify
 *  @return a array with the gain ratio for each variable
*/
public double [] computeGainRatio (DataBaseCases dbc, int classnumber) {
    int attributescases[][];
    int classcases[];
    Vector attributes;
    FiniteStates classvar;

    int i,j;
    double entropy=0;

    //Data structures for compute the gain ratio
    attributescases= new int [dbc.getVariables().size()-1][dbc.getNumberOfCases()];
    classcases= new int [dbc.getNumberOfCases()];

    //Create new properties
    attributes= new Vector(dbc.getVariables().size()-1);//FinitStates vector with the attributes
    classvar= new FiniteStates();

    //Build the arrays with the cases (array classes cases + array attributes cases)
    for (i=0 ; i< dbc.getNumberOfCases() ; i++) {
	for (j=0 ; j< dbc.getVariables().size()  ; j++) {
	    Node node =(Node)(dbc.getVariables()).elementAt(j);
	    if (node.getTypeOfVariable()==Node.CONTINUOUS) {
		System.err.println("ERROR: There is continuous values. First, use a Discretization method.");
		System.exit(0);
            //The first var is the class
	    } else if (j == classnumber ) 
		classcases[i]=(int)((CaseListMem)dbc.getCases()).getValue(i,j);
            //The rest vars are attributes
	    else { 
		if (j < classnumber)
		    attributescases[j][i]=(int)((CaseListMem)dbc.getCases()).getValue(i,j);
		else
		    attributescases[j-1][i]=(int)((CaseListMem)dbc.getCases()).getValue(i,j);
	    }
	}//end for j
    }//end for i

    //Build the vectors  with the classes and the attributes
    FiniteStates nodefn =(FiniteStates)(dbc.getVariables()).elementAt(classnumber);
    classvar=nodefn;
    this.classvar=classvar;
    for (j=0 ; j< dbc.getVariables().size()  ; j++) 
	if (j!=classnumber) {
	    nodefn =(FiniteStates)(dbc.getVariables()).elementAt(j);
	    attributes.add(nodefn);
	}
    
    //Compute the proabilities for the classes and the attributes
    Vector classesprob=probabilitiesOne(classcases, 0);
    Vector attributesprob=probabilities(attributes, attributescases, 0);
    //get the probabilities for undef values
    double undefprob[]=(double [])attributesprob.remove(attributesprob.size()-1);
    
    //determine the actual node entropy
    entropy=thisLevelEntropy(classesprob);

    //For each attribute, compute the contional entropy
    double nextlevelentropyaverage [] = new double [attributes.size()];
    for (i=0; i < attributes.size(); i++) {
	double prob[]= (double [])attributesprob.elementAt(i);
	double nextlevelentropy[]=nextLevelEntropy(i,attributes,attributescases, classcases, 0);
	for (j=0; j < prob.length; j++)
	    if (prob[j] != 0) 
		nextlevelentropyaverage[i]+=prob[j]*nextlevelentropy[j];
    }//end for i

    //Compute the gain ratio (c4.5)  
    double gain [] = new double [attributes.size()];
    double gainratio [] = new double [attributes.size()];
    double splitinfo [] = new double [attributes.size()];
    for (i=0; i < nextlevelentropyaverage.length; i++) {
	gain[i]=(1-undefprob[i])*(entropy-nextlevelentropyaverage[i]);
	splitinfo[i]=splitInfo( (FiniteStates)attributes.elementAt(i), (double [])attributesprob.elementAt(i), undefprob[i] );
	gainratio[i]=gain[i]/splitinfo[i];
	
    }//end for i
    
    return gainratio;
}//end gain ratio method
/*---------------------------------------------------------------*/ 
/**
 *  This method computes the gain for a set of variables
 *  @param dbc DataBaseCases with the variables and the values
 *  @param classnumber number of the variable to classify
 *  @return a array with the gain for each variable
*/
public double [] computeGain (DataBaseCases dbc, int classnumber) {
    int attributescases[][];
    int classcases[];
    Vector attributes;
    FiniteStates classvar;

    int i,j;
    double entropy=0;

    //Data structures for compute the gain ratio
    attributescases= new int [dbc.getVariables().size()-1][dbc.getNumberOfCases()];
    classcases= new int [dbc.getNumberOfCases()];

    //Create new properties
    attributes= new Vector(dbc.getVariables().size()-1);//FinitStates vector with the attributes
    classvar= new FiniteStates();

    //Build the arrays with the cases (array classes cases + array attributes cases)
    for (i=0 ; i< dbc.getNumberOfCases() ; i++) {
	for (j=0 ; j< dbc.getVariables().size()  ; j++) {
	    Node node =(Node)(dbc.getVariables()).elementAt(j);
	    if (node.getTypeOfVariable()==Node.CONTINUOUS) {
		System.err.println("ERROR: There is continuous values. First, use a Discretization method.");
		System.exit(0);
            //The first var is the class
	    } else if (j == classnumber ) 
		classcases[i]=(int)((CaseListMem)dbc.getCases()).getValue(i,j);
            //The rest vars are attributes
	    else { 
		if (j < classnumber)
		    attributescases[j][i]=(int)((CaseListMem)dbc.getCases()).getValue(i,j);
		else
		    attributescases[j-1][i]=(int)((CaseListMem)dbc.getCases()).getValue(i,j);
	    }
	}//end for j
    }//end for i

    //Build the vectors  with the classes and the attributes
    FiniteStates nodefn =(FiniteStates)(dbc.getVariables()).elementAt(classnumber);
    classvar=nodefn;
    this.classvar=classvar;
    for (j=0 ; j< dbc.getVariables().size()  ; j++) 
	if (j!=classnumber) {
	    nodefn =(FiniteStates)(dbc.getVariables()).elementAt(j);
	    attributes.add(nodefn);
	}
    
    //Compute the proabilities for the classes and the attributes
    Vector classesprob=probabilitiesOne(classcases, 0);
    Vector attributesprob=probabilities(attributes, attributescases, 0);
    //get the probabilities for undef values
    double undefprob[]=(double [])attributesprob.remove(attributesprob.size()-1);
    
    //determine the actual node entropy
    entropy=thisLevelEntropy(classesprob);

    //For each attribute, compute the contional entropy
    double nextlevelentropyaverage [] = new double [attributes.size()];
    for (i=0; i < attributes.size(); i++) {
	double prob[]= (double [])attributesprob.elementAt(i);
	double nextlevelentropy[]=nextLevelEntropy(i,attributes,attributescases, classcases, 0);
	for (j=0; j < prob.length; j++)
	    if (prob[j] != 0) 
		nextlevelentropyaverage[i]+=prob[j]*nextlevelentropy[j];
    }//end for i

    //Compute the gain
    double gain [] = new double [attributes.size()];
    for (i=0; i < nextlevelentropyaverage.length; i++) 
	gain[i]=(1-undefprob[i])*(entropy-nextlevelentropyaverage[i]);
    
    return gain;
}//end gain method
/*---------------------------------------------------------------*/ 
/**
 *  This method computes the gain for a set of variables where the
 *  probabilities are soomthed using a Dirichlet distribution
 *  @param dbc DataBaseCases with the variables and the values
 *  @param classnumber number of the variable to classify
 *  @param s parameter s for the Dirichlet distribution, usally s=1 or s=2
 *  @return a array with the gain for each variable
*/
public double [] computeGainDirichlet (DataBaseCases dbc, int classnumber, int s) {
    int attributescases[][];
    int classcases[];
    Vector attributes;
    FiniteStates classvar;

    int i,j;
    double entropy=0;

    //Data structures for compute the gain ratio
    attributescases= new int [dbc.getVariables().size()-1][dbc.getNumberOfCases()];
    classcases= new int [dbc.getNumberOfCases()];

    //Create new properties
    attributes= new Vector(dbc.getVariables().size()-1);//FinitStates vector with the attributes
    classvar= new FiniteStates();

    //Build the arrays with the cases (array classes cases + array attributes cases)
    for (i=0 ; i< dbc.getNumberOfCases() ; i++) {
	for (j=0 ; j< dbc.getVariables().size()  ; j++) {
	    Node node =(Node)(dbc.getVariables()).elementAt(j);
	    if (node.getTypeOfVariable()==Node.CONTINUOUS) {
		System.err.println("ERROR: There is continuous values. First, use a Discretization method.");
		System.exit(0);
            //The first var is the class
	    } else if (j == classnumber ) 
		classcases[i]=(int)((CaseListMem)dbc.getCases()).getValue(i,j);
            //The rest vars are attributes
	    else { 
		if (j < classnumber)
		    attributescases[j][i]=(int)((CaseListMem)dbc.getCases()).getValue(i,j);
		else
		    attributescases[j-1][i]=(int)((CaseListMem)dbc.getCases()).getValue(i,j);
	    }
	}//end for j
    }//end for i

    //Build the vectors  with the classes and the attributes
    FiniteStates nodefn =(FiniteStates)(dbc.getVariables()).elementAt(classnumber);
    classvar=nodefn;
    this.classvar=classvar;
    for (j=0 ; j< dbc.getVariables().size()  ; j++) 
	if (j!=classnumber) {
	    nodefn =(FiniteStates)(dbc.getVariables()).elementAt(j);
	    attributes.add(nodefn);
	}
    
    //Compute the proabilities for the classes and the attributes
    Vector classesprob=probabilitiesOne(classcases, 0);
    Vector attributesprob=probabilities(attributes, attributescases, 0);
    //get the probabilities for undef values
    double undefprob[]=(double [])attributesprob.remove(attributesprob.size()-1);
    
    //determine the actual node entropy
    entropy=thisLevelEntropy(classesprob);

    //For each attribute, compute the contional entropy
    double nextlevelentropyaverage [] = new double [attributes.size()];
    for (i=0; i < attributes.size(); i++) {
	double prob[]= (double [])attributesprob.elementAt(i);
	double nextlevelentropy[]=nextLevelEntropy(i,attributes,attributescases, classcases, ((float)s)/((float)this.classvar.getNumStates()));
	for (j=0; j < prob.length; j++)
	    if (prob[j] != 0) 
		nextlevelentropyaverage[i]+=prob[j]*nextlevelentropy[j];
    }//end for i

    //Compute the gain
    double gain [] = new double [attributes.size()];
    for (i=0; i < nextlevelentropyaverage.length; i++) 
	gain[i]=(1-undefprob[i])*(entropy-nextlevelentropyaverage[i]);

    return gain;
}//end computeGainDirichlet method
/*---------------------------------------------------------------*/ 
/**
 *  Uses the ID3 or C4.5 algorithm for classify a set of values and their classes and
 *  build the classfication tree.
 *  @param tree root node for the actual tree
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param attributes vector with Finite States variable with the attributes
 *  @param classvar finite States variable with the class
 *  @param threshold a thresold of the minimun entropy for the leaf nodes of the classification tree
 *  @param deep actual recursive deep 
 *  @param deeplimit maximun recursive deep 
 *  @param method for use ID3 (0) or C4.5 (1)
*/

private void id3c45Recursive (ProbabilityTree tree,  int attributescases[][], int classcases[],  
			 Vector attributes, FiniteStates classvar,
			 double threshold, int deep , int deeplimit,int method ) {
    int i,j;
    double entropy=0;

    //we bring up to date the recursive deep 
    deep++;
    //Compute the proabilities for the classes and the attributes
    Vector classesprob=probabilitiesOne(classcases, 0);
    Vector attributesprob=probabilities(attributes, attributescases, 0);
    //get the probabilities for undef values
    double undefprob[]=(double [])attributesprob.remove(attributesprob.size()-1);
    
    //determine the actual node entropy
    entropy=thisLevelEntropy(classesprob);

    //check stop condition
    if ( stopId3C45Dirichlet(tree,  attributescases, classcases, attributes, classvar, entropy, threshold, deep, deeplimit, 0) ) return;    

    //For each attribute, compute the contional entropy
    double nextlevelentropyaverage [] = new double [attributes.size()];
    for (i=0; i < attributes.size(); i++) {
	double prob[]= (double [])attributesprob.elementAt(i);
	double nextlevelentropy[]=nextLevelEntropy(i,attributes,attributescases, classcases, 0);
	for (j=0; j < prob.length; j++)
	    if (prob[j] != 0) 
		nextlevelentropyaverage[i]+=prob[j]*nextlevelentropy[j];
    }//end for i

    //Compute the gain (id3) or gain ratio (c4.5)  and look for the max gain
    double gain [] = new double [attributes.size()];
    int maxgain=0;
    if (method == 1) {
	//C4.5 method. Compute the gain ,split info anda gain ratio. Look for the max gain ratio also.
	double gainratio [] = new double [attributes.size()];
	double splitinfo [] = new double [attributes.size()];
	for (i=0; i < nextlevelentropyaverage.length; i++) {
	    gain[i]=(1-undefprob[i])*(entropy-nextlevelentropyaverage[i]);
	    splitinfo[i]=splitInfo( (FiniteStates)attributes.elementAt(i), (double [])attributesprob.elementAt(i), undefprob[i] );
	    gainratio[i]=gain[i]/splitinfo[i];
	    maxgain=(gainratio[i]>gainratio[maxgain])?i:maxgain;
	}
    } else {
	//Id3 method. Compute the gain, look for the max gain ratio also.
    	for (i=0; i < nextlevelentropyaverage.length; i++) {
	    gain[i]=(1-undefprob[i])*(entropy-nextlevelentropyaverage[i]);
	    maxgain=(gain[i]>gain[maxgain])?i:maxgain;
	}
    }

    //the attribute with max gain will be the root node
    tree.assignVar((FiniteStates)attributes.elementAt(maxgain));

    //Build a subtree for each value of the attribute with max gain.
    for (i=0; i < ((FiniteStates)attributes.elementAt(maxgain)).getNumStates(); i++) {
	//We build new subsets of cases  for attributes and the class
	//without for the attribute with max gain
	Vector subsets=buildSubsets(i, attributescases, classcases, maxgain);
        int newclasscases[]=(int [])subsets.elementAt(0);
        int newattributescases[][]=(int [][])subsets.elementAt(1);
    
	//add the i child 
	ProbabilityTree childnode = new ProbabilityTree();
	tree.add(childnode);

	//build the subtree for the child i
	childnode=tree.getChild(i);
	Vector newattributes = (Vector)attributes.clone();
	newattributes.remove(maxgain);

	//recursive call for build the subtree
	id3c45Recursive(childnode, newattributescases, newclasscases, newattributes,classvar, threshold, deep , deeplimit,method); 
      
    }//end for i

}//end method id3c45Recursive






/*---------------------------------------------------------------*/ 
/*---------------------------------------------------------------*/ 
/**
 *  Uses the Dirichlet distribution bases algorithm for classify a
 *  set of values and their classes and
 *  build the classification tree.
 *  @param tree root node for the actual tree
 *  @param attributescases values for the cases of the attributes
 *  @param classcases values for the cases of the class 
 *  @param attributes vector with Finite States variable with the attributes
 *  @param classvar finite States variable with the class
 *  @param threshold a thresold of the minimun entropy for the leaf nodes of the classification tree
 *  @param deep actual recursive deep 
 *  @param deeplimit maximun recursive deep 
 *  @param dirichletfactor the factor to add to the probabilities, for use a dirichlet distribution. The recommended value is 2/classes.
 *  @param actualdirichletdeep actual depth of worse child entropies.
 *  @param dirichletdeep acceptance and exploring  depth for worse child entropies.
 *  @return count to the level of deep we have to stop building the tree. if it's -1 don't have to stop.
*/

private int dirichletRecursive (ProbabilityTree tree,  int attributescases[][], int classcases[],  
				 Vector attributes, FiniteStates classvar,
				 double threshold, int deep , int deeplimit, 
				 float dirichletfactor, int actualdirichletdeep, int dirichletdeep) {
    int i,j;
    double entropy=0;
    int min=0;

    //we bring up to date the recursive deep 
    deep++;
    //Compute the proabilities for the classes and the attributes
    Vector classesprob=probabilitiesOne(classcases, dirichletfactor);
    Vector attributesprob=probabilities(attributes, attributescases, dirichletfactor);
    //get the probabilities for undef values
    double undefprob[]=(double [])attributesprob.remove(attributesprob.size()-1);
    
    //determine the actual node entropy
    entropy=thisLevelEntropy(classesprob);

    //check stop condition
    if ( stopId3C45Dirichlet(tree,  attributescases, classcases, attributes, classvar, entropy, threshold, deep, deeplimit, dirichletfactor) ) return -1;    

    //For each attribute, compute the contional entropy
    double nextlevelentropyaverage [] = new double [attributes.size()];
    for (i=0; i < attributes.size(); i++) {
	double prob[]= (double [])attributesprob.elementAt(i);
	double nextlevelentropy[]=nextLevelEntropy(i,attributes,attributescases, classcases, dirichletfactor);
	for (j=0; j < prob.length; j++)
	    if (prob[j] != 0) 
		nextlevelentropyaverage[i]+=prob[j]*nextlevelentropy[j];
    }//end for i

    //Compute the child with min entropy 
    for (i=0; i < nextlevelentropyaverage.length; i++) 
	    min=(nextlevelentropyaverage[i]<nextlevelentropyaverage[min])?i:min;

    //Dirichlet stop condition
    int aux=stopDirichlet(tree, classcases, classvar, entropy, nextlevelentropyaverage[min], dirichletfactor,actualdirichletdeep, dirichletdeep);
    if (aux==-1) return actualdirichletdeep-1;
    else actualdirichletdeep=aux;

    //the attribute with min entropy will be the root node
    tree.assignVar((FiniteStates)attributes.elementAt(min));

    //Build a subtree for each value of the attribute with min entropy.
    for (i=0; i < ((FiniteStates)attributes.elementAt(min)).getNumStates(); i++) {
	//We build new subsets of cases  for attributes and the class
	//without for the attribute with min entropy
	Vector subsets=buildSubsets(i, attributescases, classcases, min);
        int newclasscases[]=(int [])subsets.elementAt(0);
        int newattributescases[][]=(int [][])subsets.elementAt(1);
    
	//add the i child 
	ProbabilityTree childnode = new ProbabilityTree();
	tree.add(childnode);

	//build the subtree for the child i
	childnode=tree.getChild(i);
	Vector newattributes = (Vector)attributes.clone();
	newattributes.remove(min);

	//recursive call for build the subtree
	int deepcut=dirichletRecursive(childnode, newattributescases, newclasscases, newattributes,classvar, threshold, deep , deeplimit, dirichletfactor,actualdirichletdeep, dirichletdeep); 

	//deep back
	//we have to stop building at this level
	if (deepcut ==0) {
	    ProbabilityTree leaftree=addLeafNodes(tree,classcases, classvar, dirichletfactor);
	    tree.add(leaftree);
	    return -1;
	}
	//we have to stop building the tree at higher level
	else if (deepcut>0){
	    deepcut--;
	    return deepcut;
	}
    }//end for i

    //return no higher stop building 
    return -1;

}//end method dirichletRecursive

/*---------------------------------------------------------------*/ 
/**
 * Saves the classfication tree using the probability tree save method.
 * @param f the <code>FileWriter</code> where the tree will be written.
 * @param j a tab factor (number of blank spaces before a child
 * is written).
 */
public void save(FileWriter f, int j) {
    
  PrintWriter p;
  p = new PrintWriter(f);
  ctree.save(p,j);
}

/*---------------------------------------------------------------*/ 
/*---------------------------------------------------------------*/ 
/**
 * For performing tests 
 */
public static void main(String args[]) throws ParseException, IOException, InvalidEditException
{
      if(args.length < 6){
	  System.out.println("ERROR:Too few arguments.");
          System.out.println("Use: file.dbc test.dbc (for test the tree) tree.out (for saving the tree) rules.out (for saving the rules)  method (0=Id3 , 1=C4.5 , 2=Dirichlet) prune_method (0=Reduced Error Prunning -ERP-, 1=Error Based Pruning -EBP-) [confidence_level_for_EBP]");
	  System.exit(0);
      }

      //Open the database
      int method = (Integer.valueOf(args[4])).intValue();
      FileInputStream f = new FileInputStream(args[0]);
      DataBaseCases cases = new DataBaseCases(f);
      f.close();

      f = new FileInputStream(args[1]);
      DataBaseCases cases2 = new DataBaseCases(f);
      f.close();

      //Prune params
      int prunemethod = (Integer.valueOf(args[5])).intValue();
      double confidencefactor=0.25;
      if (args.length == 7) {
	  confidencefactor = 0.1; //(Double.valueOf(args[6])).doubleValue();
      } 

      //Look for classification method
      ClassificationTree classificationtree= new ClassificationTree();      

      switch ( method ) {
              case ID3:	  System.out.println("Classification algorithm: ID3");
	                  classificationtree.id3(cases, 0);
	                  break;
              case C45:	  System.out.println("Classification algorithm: C4.5 "); 
                          classificationtree.c45(0, cases);
	                  break;
              case DIRICHLET: 
		          //get the number of different values for the class  
		          int classnumber=((FiniteStates)(cases.getVariables()).elementAt(0)).getNumStates();
                          float dirichletfactor=(float)2.0/(float)classnumber;
                          System.out.println("Classification algorithm: Dirichlet. Dirichlet Factor="+dirichletfactor+", Dirichlet Deep=1"); 
                          classificationtree.dirichlet(0, cases,dirichletfactor,1);
			  break;
	      default:	  System.out.println("ERROR: Unknown classification tree method.");
		          System.out.println("Use: file.dbc test.dbc (for test the tree) tree.out (for saving the tree) rules.out (for saving the rules)  method (0=Id3 , 1=C4.5 , 2=Dirichlet) prune_method (0=Reduced Error Prunning -ERP-, 1=Error Based Pruning -EBP-) [confidence_level_for_EBP]");
 	                  System.exit(0);
	}//end switch 

      //show the classification tree

      System.out.println("\n The classification tree is:");
      classificationtree.print(2);

      System.out.print("\n The classification error of the tree is: ");
      System.out.println(classificationtree.classificationError());
      System.out.print(" The test error of the tree is: ");
      System.out.println(classificationtree.testError(cases2));


      //show the classfication tree rules
      System.out.println("\n\n The rules from the classification tree are:");
      classificationtree.printRules();

      //Save the classification tree
      FileWriter  f2 = new FileWriter(args[2]);
      classificationtree.save(f2,2);
      f2.close();

      //Save the classification tree rules
      FileWriter  f3 = new FileWriter(args[3]);
      classificationtree.saveRules(f3);
      f3.close();


      switch ( prunemethod ) {
              case 0:	  System.out.println("Prune Method: ERP");
			  classificationtree.reducedErrorPruning();
	                  break;      
              case 1:	  System.out.println("Prune Method: EBP");
			  classificationtree.errorBasedPruning(confidencefactor);
	                  break;      
	      default:	  System.out.println("ERROR: Unknown pruning method.");
		          System.out.println("Use: file.dbc test.dbc (for test the tree) tree.out (for saving the tree) rules.out (for saving the rules)  method (0=Id3 , 1=C4.5 , 2=Dirichlet) prune_method (0=Reduced Error Prunning -ERP-, 1=Error Based Pruning -EBP-) [confidence_level_for_EBP]");
 	                  System.exit(0);
	}//end switch 



      //show the classification tree pruned

      System.out.println("\n The pruned classification tree is:");
      classificationtree.print(2);

      System.out.print("\n The classification error of the pruned tree is: ");
      System.out.println(classificationtree.classificationError());
      System.out.print(" The test error of the pruned tree is: ");
      System.out.println(classificationtree.testError(cases2));


      //show the classfication tree rules
      System.out.println("\n\n The rules from the pruned classification tree are:");
      classificationtree.printRules();
 
      //Save the classification tree
      f2 = new FileWriter("pruned_"+args[2]);
      classificationtree.save(f2,2);
      f2.close();

      //Save the pruned classification tree rules
      f3 = new FileWriter("pruned_"+args[3]);
      classificationtree.saveRules(f3);
      f3.close();

}//End main 

public long size(){
	this.ctree.updateSize();
	return this.ctree.getSize();
}

public void saveModelToFile(String ap) throws IOException {
	String name = "ClassificationTree";
	switch(this.buildmethod){
	case ClassificationTree.ID3 :
		name+="-id3";
		break;
	case ClassificationTree.DIRICHLET :
		name+="-dir";
		break;
	case ClassificationTree.C45 :
		name+="-c45";
		break;
	}
	switch(this.prunemethod){
	case ClassificationTree.EBP :
		name+="-ebp";
		break;
	case ClassificationTree.REP :
		name+="-rep";
		break;
	case ClassificationTree.NONE :
		name+="-none";
		break;
	}
	FileWriter fw = new FileWriter(ap+name+".rules");
	this.save(fw,1);
	fw.close();
}

} // End of class ClassificationTree
