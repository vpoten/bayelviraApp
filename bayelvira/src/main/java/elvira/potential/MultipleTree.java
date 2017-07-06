/* MultipleTree.java */

package elvira.potential;

import java.util.Vector;
import java.util.Hashtable;
import java.io.*;
import elvira.FiniteStates;
import elvira.Configuration;
import elvira.NodeList;
import elvira.inference.abduction.Explanation;

/**
 * Class MultipleTree. Implements a probability tree with
 * two possible values at each leaf. A probability tree
 * is a compact representation of a probability distribution,
 * alternative to a probability table.
 * Each internal node represents a variable and each leaf
 * node represents a probability value; each leaf also contains
 * a second value that can be used to compute the information of
 * a tree conditional on that value. Furthermore, it is possible to store
 * max and min values at each leaf, indicating an interval where the
 * exact probability of the configuration leading to that leaf lies.
 * Each variable node
 * has as many children as possible values it has. The value stored
 * in a leaf corresponds to the probability of the
 * configuration that leads from the root node to that leaf.
 *
 * An object of this class is a node of the tree, that can point
 * to other nodes, forming a tree in this way.
 *
 * @since  6/3/2003
 */

public class MultipleTree {
 
/**
 * The variable associated with the node of the tree, if the node is
 * internal.
 */
FiniteStates var;

/**
 * A label that indicates the type of the node:
 * 0: empty node.
 * 1: full node (internal node).
 * 2: probability node (a leaf).
 */
int label; // 0: empty node. 1: full node. 2: probability node

static final int EMPTY_NODE=0;
static final int FULL_NODE=1;
static final int PROBAB_NODE=2;

public static final int AVERAGE_APPROX=0;
public static final int ZERO_APPROX=1;
public static final int AVERAGEPRODCOND_APPROX=2;

/**
 * <code>Vector</code> of children of the root node of this 
 * <code>MultipleTree</code>.
 */
Vector child;

/**
 * Number of leaf nodes this <code>MultipleTree</code> has.
 */
private long leaves;

/**
 * The probability value, if the node is a leaf.
 */
private double value;

/**
 * A second probability value, if the node is a leaf.
 */
private double secondValue;

/**
 * Indicates the maximum probability value of the configuration
 * corresponding to this leaf node.
 */
private double max;

/**
 * Indicates the miniimum probability value of the configuration
 * corresponding to this leaf node.
 */
private double min;
  

/**
 * Constructor. Creates an empty tree node.
 */

public MultipleTree() {

  label = EMPTY_NODE;
  value = 0.0;
  max = 0.0;
  min = 0.0;
  secondValue = 0.0;
  leaves = 0;
  child = new Vector();
}


/**
 * Constructor. Creates a <code>MultipleTree</code> from a
 * <code>ProbabilityTree</code>, with the same information as this one.
 * @param pt a <code>ProbabilityTree</code>.
 */

public MultipleTree(ProbabilityTree pt) {
 
  int i;
  MultipleTree temp;
  
  child = new Vector();
  label = pt.getLabel();
  if (label == PROBAB_NODE) {
    value = pt.getProb();
    max = value;
    min = value;
    leaves = 1;
  }
  else {
    var = pt.getVar();
    for (i=0 ; i<pt.getChild().size() ; i++) {
      temp = new MultipleTree(pt.getChild(i));
      child.addElement(temp);
    }
  }
}


/**
 * Creates a probability node with a given value.
 * @param p a <code>double</code> value to store in the node.
 */

public MultipleTree(double p) {

  label = PROBAB_NODE;
  value = p;
  max = p;
  min = p;
  child = new Vector();
  leaves = 1;
}


/**
 * Gets the size (number of values) of the tree starting from this node.
 * @return the number of leaves beneath this tree node.
 */

public long getSize() {
 
  return leaves;
}


/**
 * Updates the number of leaves in this node and in each of
 * its descendants.
 */

public void updateSize() {
 
  MultipleTree tree;
  int i, nv;
  
  if (label == PROBAB_NODE)
    leaves = 1;
  else {
    leaves = 0;
    nv = var.getNumStates();
    
    for (i=0 ; i<nv ; i++) {
      tree = getChild(i);
      tree.updateSize();
      leaves += tree.getSize();
    }
  }
}


/**
 * Gets the label of this node.
 * @return the label of the node.
 */

public int getLabel() {

  return label;
}


/**
 * Gets the probability value of this node.
 * @return the probability value attached to the node.
 */

public double getProb() {

  return value;
}


/**
 * Gets the probability of a given configuration of variables.
 * @param conf a <code>Configuration</code>.
 * @return the probability value of the tree following
 * the path indicated by <code>Configuration conf</code>. The value
 * -1 is returned in case of error.
 */

public double getProb(Configuration conf) {

  int p, val;
  MultipleTree tree;

  if (label == FULL_NODE) { // If the node is a variable   
    val = conf.getValue(var);
    tree = (MultipleTree)child.elementAt(val);
    return(tree.getProb(conf));
  }
  else {
    if (label == PROBAB_NODE) // If the node is a prob. 
      return value;
    else 
      return(-1.0);
  }
}


/**
 * Gets the probability of a configuration of variables specified by
 * means of an array of <code>int</code>. The position of each variable
 * in this array is stored in a <code>Hashtable</code> given as argument.
 * @param positions a <code>Hashtable</code> with the positions of the
 * variables in conf.
 * @param conf an array of <code>int</code> with a configuration.
 * @return the value of the tree for the configuration. In case of error,
 * value -1 is returned.
 */

public double getProb(Hashtable positions, int[] conf) {
 
  int p, val;
  MultipleTree tree;

  if (label == FULL_NODE) { // If the node is a variable,
                            // call the same method with the
                            // corresponding child.
    p = ((Integer)positions.get(var)).intValue();
    val = conf[p];

    tree = (MultipleTree)child.elementAt(val);
    return (tree.getProb(positions,conf));
  }
  else {
    if (label == PROBAB_NODE) 
      return value;
    else 
      return(-1.0);
  }
}

/**
 * Gets the min value of a given configuration of variables.
 * @param conf a <code>Configuration</code>.
 * @return the min value of the tree following
 * the path indicated by <code>Configuration conf</code>. The value
 * -1 is returned in case of error.
 */

public double getMin(Configuration conf) {

  int p, val;
  MultipleTree tree;

  if (label == FULL_NODE) { // If the node is a variable   
    val = conf.getValue(var);
    tree = (MultipleTree)child.elementAt(val);
    return(tree.getMin(conf));
  }
  else {
    if (label == PROBAB_NODE) // If the node is a prob. 
      return min;
    else 
      return(-1.0);
  }
}

/**
 * Gets the max value of a given configuration of variables.
 * @param conf a <code>Configuration</code>.
 * @return the min value of the tree following
 * the path indicated by <code>Configuration conf</code>. The value
 * -1 is returned in case of error.
 */

public double getMax(Configuration conf) {

  int p, val;
  MultipleTree tree;

  if (label == FULL_NODE) { // If the node is a variable   
    val = conf.getValue(var);
    tree = (MultipleTree)child.elementAt(val);
    return(tree.getMax(conf));
  }
  else {
    if (label == PROBAB_NODE) // If the node is a prob. 
      return max;
    else 
      return(-1.0);
  }
}
/**
 * Assigns a value to the node.
 * Also, sets the label to <code>PROBAB_NODE</code>.
 * @param p a <code>double</code> value.
 */

public void assignProb(double p) {

  label = PROBAB_NODE;
  value = p;
  leaves = 1;
}


/**
 * Assigns a variable to an empty node.
 * Initializes as many children as values of the
 * variable, to empty trees.
 * @param variable a <code>FiniteStates</code> variable.
 */

public void assignVar(FiniteStates variable) {
 
  MultipleTree tree;
  int i,j;
    
  var = variable;
  label = FULL_NODE;
  child = new Vector();
  j = variable.getNumStates();
  
  for (i=0 ; i<j ; i++) {
    tree = new MultipleTree();
    child.addElement(tree);
  }
}


/**
 * Gets the variable associatd with the node.
 * @return the <code>FiniteStates</code> variable stored in the tree node.
 */

public FiniteStates getVar() {

  return var;
}


/**
 * Assigns the second value to this node.
 * @param x a <code>double</code> value.
 */

public void assignSecondValue(double x) {
 
  secondValue = x;
}


/**
 * Assigns the max value.
 * @param x the value that will be the max.
 */

public void assignMax(double x) {
  
  max = x;
}


/**
 * Assigns the min value.
 * @param x the value that will be the min.
 */

public void assignMin(double x) {
  
  min = x;
}


/**
 * Gets the second value.
 * @return the secon value.
 */

public double getSecondValue() {
  
  return secondValue;
}


/**
 * Gets the max value.
 * @return the max value.
 */

public double getMax() {
  
  return max;
}


/**
 * Gets the min value.
 * @return the min value.
 */

public double getMin() {
  
  return min;
}


/**
 * Gets the vector of children of this node.
 * @return the <code>Vector</code> of children of the node.
 */

public Vector getChild() {
  
  return child; 
}


/**
 * Gets one of the children of this node.
 * @param i an <code>int</code> value: number of child to be returned
 * (first child is i=0).
 * @return the i-th child of the tree.
 */

public MultipleTree getChild(int i) {
  
  return ((MultipleTree)(child.elementAt(i))); 
}


/**
 * Inserts a <code>MultipleTree</code> as child of this node.
 * It is inserted at the end of the vector of children.
 * @param tree <code>MultipleTree</code> to be added as child.
 */

public void insertChild(MultipleTree tree) {
 
  child.addElement(tree);
}
 

/**
 * Creates a <code>MultipleTree</code> constantly equal to 1.
 * @return a unit <code>MultipleTree</code>.
 */

public static MultipleTree unitTree() {
 
  MultipleTree t;
  
  t = new MultipleTree();
  t.assignProb(1.0);
  t.assignMax(1.0);
  t.assignMin(1.0);
  
  return t;
}


/**
 * Computes the minimum of the min values in the subtree starting from this
 * node.
 * @return the minimum of the min values in the subtree.
 */

public double minimum() {
  
  double m = 1e20, m2;
  int i, nv;
  
  if (label == PROBAB_NODE)
    m = min;
  else {
    nv = var.getNumStates();
    for (i=0 ; i<nv ; i++) {
      m2 = ((MultipleTree)child.elementAt(i)).minimum();
      if (m2 < m)
	m = m2;
    }
  }
  
  return m;
}


/**
 * Computes the maximum of the max values in the subtree starting from this
 * node.
 * @return the maximum of the max values in the subtree.
 */

public double maximum() {
  
  double m = 0.0, m2;
  int i, nv;
  if (label == PROBAB_NODE){
    m = max;
  }
  else {
    nv = var.getNumStates();
    for (i=0 ; i<nv ; i++) {
      m2 = ((MultipleTree)child.elementAt(i)).maximum();
      if (m2 > m){
	      m = m2;
      }
    }
  }
  return m;
}


/**
 * Constructs a <code>MultipleTree</code> from the tree starting
 * in this node, conditional on another <code>MultipleTree</code>.
 * The first value <code>value<code> at each leaf will
 * be equal to the one in the first tree, and the second
 * (<code>secondValue<code>) to the conditional value.
 * The conditional value in a leaf is computed as the averade of the
 * values of the conditioning tree consistent with the configuration
 * corresponding to that leaf.
 * @param condTree the conditioning tree.
 * @param currentConf the <code>Configuration</code> leading to this
 * tree node.
 * @return the new <code>MultipleTree</code>.
 */

public MultipleTree conditional(MultipleTree condTree,long totalSize,
				Configuration currentConf) {

  int i, nv;
  MultipleTree newTree, auxTree, otherTree;
  
  newTree = new MultipleTree();

  newTree.var = var;
  newTree.label = FULL_NODE;
  newTree.leaves = leaves;
  newTree.assignMax(max);
  newTree.assignMin(min);

  if (label != PROBAB_NODE) { // If it is not a probability,
    nv = child.size();
      
    for (i=0 ; i<nv ; i++) {
      currentConf.insert(var,i);
      auxTree = ((MultipleTree)child.elementAt(i)).conditional(condTree,
						   totalSize,
						   currentConf);
      newTree.child.addElement(auxTree);
      currentConf.remove(currentConf.size()-1);
    }
  }
  else {
    otherTree = condTree.restrict(currentConf);
    newTree.assignProb(value);
    newTree.assignSecondValue(otherTree.average());
  }
  
  return newTree;
}
 
 
/**
 * Computes the information of a variable within a tree. 
 * This method is called by NodeQueueM(MultipleTree r, MultipleTreee,
 * long potentialSize,int method,double normalization), which is called
 * by PotentialMTree.conditionalSortAndBound(int maxLeaves,int method)
 * 
 * @param variable a <code>FiniteStates</code> variable.
 * @param potentialSize maximum size of the potential containing
 * the tree (product of number of states of its variables)
 * @param normalization is the normalization factor used to compute 
 * the information of <code>variable</code>. This number is calculated
 * in PotentialMTree.conditionalSortAndBound(int maxLeaves,int method), and
 * depend on the selected method for doing the sortAndBound. In the simplest
 * method (method = 0), this number is the sum of the values of the 
 * source tree.
 * @param updateFactor an output value used to save a double. This double
 * is used to update the normalization factor when variable 
 * <code>variable</code> will be expanded in the <code>MultipleTree</code>
 * in PotentialMTree.conditionalSortAndBound(int maxLeaves,int method).
 * 
 * @return the value of information of variable <code>variable</code>.
 */

public double conditionalInformation(FiniteStates variable,
				     long potentialSize,
				     double normalization,
				     double[] updateFactor ) {
 
  MultipleTree tree;
  int i, nv;
  long newSize;
  double entropy = 0.0, s = 0.0, s2 = 0.0,
         totalA = 0.0, totalS = 0.0, total3=0.0, info = 0.0;
  
  nv = variable.getNumStates();
  newSize = potentialSize / nv;
  
  for (i=0 ; i<nv ; i++) {
    tree = restrict(variable,i);
    s = tree.average();
    s2 = tree.conditionalAverage();
    if (s > 0.0) {
      entropy += s * s2 * Math.log(s);
    }
    totalS += s2;
    totalA += s;
    total3 += s * s2;
  }
  
  updateFactor[0] = newSize * (total3 - (totalS * totalA) / nv);
  
  normalization = normalization + updateFactor[0];
  
  
  if (totalA == 0.0)
    info = 0.0;
  else {
    if (normalization == 0.0) {
      info = 1E10;
    }
    else {
      info = (1.0/normalization) *
	(newSize*entropy - newSize*total3 *Math.log(totalA/nv) + 
	 normalization*Math.log( (normalization - updateFactor[0])/normalization));
      if (info > 1E10) {
	info=1E10;
	System.out.println("Very high information.");
      }
    }
  }
  
  return (info);
}

 
/**
 * Computes the information of a variable within a tree.
 * It is a simplified and approximate version of
 * <code>conditionalInformation</code>.
 * This method is called by NodeQueueM(MultipleTree r, MultipleTreee,
 * long potentialSize,int method,double normalization), which is called
 * by PotentialMTree.conditionalSortAndBound(int maxLeaves,int method)
 * @param variable a <code>FiniteStates</code> variable.
 * @param potentialSize maximum size of the potential containing
 * the tree.
 * @param normalization is the normalization factor used to compute 
 * the information of <code>variable</code>. This number is calculated
 * in PotentialMTree.conditionalSortAndBound(int maxLeaves,int method), and
 * depend on the selected method for doing the sortAndBound. In the simplest
 * method (method = 0), this number is the sum of the values of the 
 * source tree.
 * @param updateFactor an output value used to save a double. This double
 * is used to update the normalization factor when variable 
 * <code>variable</code> will be expanded in the <code>MultipleTree</code>
 * in PotentialMTree.conditionalSortAndBound(int maxLeaves,int method).
 * @return the value of information of variable <code>variable</code>.
 */

public double conditionalInformationSimple(FiniteStates variable,
					   long potentialSize,
					   double normalization,
					   double[] updateFactor ) {
 
  MultipleTree tree;
  int i, nv;
  long newSize;
  double entropy = 0.0, s = 0.0, s2 = 0.0,
         totalA = 0.0, totalS = 0.0,  info = 0.0;
  
  nv = variable.getNumStates();
  newSize = potentialSize / nv;
  
  for (i=0 ; i<nv ; i++) {
    tree = restrict(variable,i);
    s = tree.average();
    s2 = tree.conditionalAverage();
    if (s > 0.0) {
      entropy += s * Math.log(s);
    }
    totalS += s2;
    totalA += s;
  }
  
  updateFactor[0] = 0.0;  
  
  if (totalA == 0.0)
    info = 0.0;
  else {
    if (normalization == 0.0) {
      info= 1E10;
    }
    else {
      info = (1.0/normalization)* (newSize*entropy - newSize*totalA *Math.log(totalA/nv))*(totalS/nv);
      if (info > 1E10) {
	info=1E10;
	System.out.println("Very high information.");
      }
    }
  }

 return (info);
}



/**
 * Computes the information of a variable within a tree,
 * according to the max-min criterion.
 * @param variable a <code>FiniteStates</code> variable.
 * @param potentialSize maximum size of the potential containing
 * the tree.
 * @return the value of information of variable <code>variable</code>.
 */

public double maxMinInformation(FiniteStates variable,
				long potentialSize) {
 
  MultipleTree tree;
  int i, nv;
  long newSize;
  double mini = 1e20, info = 0.0;
  double[] s;
  double[] m;
  
  
  nv = variable.getNumStates();
  s = new double[nv];
  m = new double[nv];
  newSize = potentialSize / nv;
  
  for (i=0 ; i<nv ; i++) {
    tree = restrict(variable,i);
    s[i] = tree.conditionalSum(newSize);
    m[i] = tree.minimum();
    if (m[i]<mini)
      mini = m[i];
  }
  
  for (i=0 ; i<nv ; i++) {
    info += ( (m[i]-mini) * s[i] );
  }
  
 return info;
}


/**
 * Restricts a tree to a variable.
 * @param variable a <code>FiniteStates</code> variable to which the tree
 * will be restricted.
 * @param v the value of <code>variable</code> to instantiate
 * (first value = 0).
 * @return a new tree consisting of the restriction of the
 * current tree to the value number v of variable <code>variable</code>.
 */

public MultipleTree restrict(FiniteStates variable, int v) {
 
  MultipleTree tree, tree2;
  int i, nv;
  
  if (label == PROBAB_NODE) {
    tree = new MultipleTree();
    tree.assignProb(value);
    tree.assignSecondValue(secondValue);
    tree.assignMax(max);
    tree.assignMin(min);
    return tree;
  }
  
  if (var == variable)
    tree = getChild(v).copy();
  else { 
    tree = new MultipleTree();
    tree.var = var;
    tree.label = FULL_NODE;
      
    nv = child.size();
    for (i=0 ; i<nv ; i++) {
      tree2 = ((MultipleTree)
	       child.elementAt(i)).restrict(variable,v);
      tree.child.addElement(tree2);
      tree.leaves += tree2.leaves;
    }
  }
    
  return tree;
}

/**
 * Restricts a tree to a <code>Configuration</code> of variables.
 * @param conf the <code>Configuration</code> to which the tree
 * will be restricted.
 * @return a new <code>MultipleTree</code> consisting of the restriction
 * of the current tree to the values of <code>Configuration conf</code>.
 */

public MultipleTree restrict(Configuration conf) { 
  
  MultipleTree tree, tree2;
  int i, nv,index;
  
  if (label == PROBAB_NODE) {
    tree = new MultipleTree();
    tree.assignProb(value);
    tree.assignSecondValue(secondValue);
    tree.assignMax(max);
    tree.assignMin(min);
    return tree;
  }
  
  index = conf.indexOf(var);
  if (index > -1) // if var is in conf
    tree = getChild(conf.getValue(index)).restrict(conf);
  else { 
    tree = new MultipleTree();
    tree.var = var;
    tree.label = FULL_NODE;
      
    nv = child.size();
    for (i=0 ; i<nv ; i++) {
      tree2 = ((MultipleTree)
	       child.elementAt(i)).restrict(conf);
      tree.child.addElement(tree2);
      tree.leaves += tree2.leaves;
    }
  }
    
  return tree;
}


/**
 * Computes the addition of all the values in the tree,
 * considering the second values.
 * @param treeSize size of the completely expanded tree.
 * @return the addition computed.
 */

public double conditionalSum(long treeSize) {
 
  double s = 0.0;
  int i, nv;
  long newSize;
  
  if (label == PROBAB_NODE)
    s = (double)treeSize * secondValue;
  else {
    nv = var.getNumStates();
    newSize = treeSize / nv;
    for (i=0 ; i<nv ; i++)
      s += ((MultipleTree)child.elementAt(i)).conditionalSum(newSize);
  }
  
  return s;
}


/**
 * Computes the addition of all the products of
 * first and second values in the tree
 * @param treeSize size of the completely expanded tree.
 * @return the addition computed.
 */

public double conditionalProdSum(long treeSize) {
 
  double s = 0.0;
  int i, nv;
  long newSize;
  
  if (label == PROBAB_NODE)
    s = (double)treeSize * value * secondValue;
  else {
    nv = var.getNumStates();
    newSize = treeSize / nv;
    for (i=0 ; i<nv ; i++)
      s += ((MultipleTree)child.elementAt(i)).conditionalProdSum(newSize);
  }
  
  return s;
}


/**
 * Computes the mean of the second values in the tree.
 * @return the mean.
 */

public double conditionalAverage() {

  double av = 0.0;
  int i, nv;
  
  if (label == PROBAB_NODE)
    av = secondValue;
  else {
    nv = var.getNumStates();
    for (i=0 ; i<nv ; i++)
      av += ((MultipleTree)child.elementAt(i)).conditionalAverage();
    av /= (double)nv;
  }
  
  return av;
}


/**
 * Bounds the tree by substituing nodes whose children are
 * leaves by the average of them. This is done for nodes
 * with an information value lower than a given threshold 
 * <code>limit</code>.
 * @param limit the information threshold for pruning.
 * @param lowLimit the limit used to decide whether we must consider
 * the tree as not pruned (the method returns 0 or 1) or pruned 
 * (the method returns 2).
 * @param oldSize size of this tree if it were a complete tree.
 * @param globalSum the addition of the values of the original potential.
 * @param numberDeleted an array with a single value storing
 * the number of deleted leaves.
 * @return 
 * <ol>
 * <li> 0 if the resulting tree has not been pruned. Information
 * of its children is greater than <code>limit</code> .
 * <li> 1 if the resulting tree has been pruned but in every case
 * the information values of the pruned nodes are between
 * <code>limit</code> and <code>lowLimit</code>.
 * <li> 2 if the resulting tree has been pruned
 * and the information values of the pruned nodes are smaller than
 * <code>lowLimit</code>.
 * </ol>
 */

public int conditionalPrune1(double limit, 
     double lowLimit, long oldSize,
     double globalSum[], long numberDeleted[]) {
  long newSize;
  int i, numberChildren;
  MultipleTree ch;
  double pr, pr2, // maxvalue, minvalue, 
         sum2=0.0, sum = 0.0, sum3=0.0,
	 entropy = 0.0, info, normalization;
  Double aux;
  // These variables can contain 0, 1 or 2 and it indicates whether
  // the tree can be pruned (value > 0)
  int bounded = 2, childBounded; 
  
  numberChildren = var.getNumStates();
  newSize = oldSize / numberChildren;
  
  for (i=0 ; i<numberChildren ; i++) {
    ch = (MultipleTree)child.elementAt(i);    
    if (ch.getLabel() == PROBAB_NODE) {
       pr = ch.getProb();
       pr2 = ch.getSecondValue();
       sum2 += pr2;
       sum3 += pr*pr2;
       sum += pr;
       if (pr > 0.0) {
	 entropy += (pr*pr2 * Math.log(pr));
       }
    }
    else {
      childBounded = ch.conditionalPrune1(limit,lowLimit,newSize,
					 globalSum,numberDeleted);
      bounded = Math.min(bounded,childBounded);
      if (bounded > 0) {
	ch = (MultipleTree)child.elementAt(i);
	pr = ch.getProb();
	pr2 = ch.getSecondValue();
	sum2 += pr2;
	sum += pr;
	sum3 += pr * pr2;
	if (pr > 0.0) {
	  entropy += (pr*pr2 * Math.log(pr));
	}
      }
    }
  } // end for
      
  if (bounded > 0) {
    normalization = globalSum[0];
    if (sum <= 0.0)
      info = 0.0;
    else {
      if (normalization > 0.0) {
	 info = ( 1.0 / normalization) * ( newSize*entropy - newSize*sum3*Math.log(sum/numberChildren)+
		normalization * Math.log( (normalization - sum3*newSize + sum*sum2*newSize/numberChildren)/normalization));
      }
      else { 
	if(sum2 == 0.0) {
	  info = 0.0;
	} 
	else {
	  info = 1E10;
	}
      }
    }
      
    if (info <= limit) {
      if (info > lowLimit)
	bounded = 1;
      approximate(AVERAGE_APPROX);
      numberDeleted[0] += numberChildren-1;

      /*pr = average();
      pr2 = conditionalAverage(); 
      maxvalue = maximum();
      minvalue = minimum();
      numberDeleted[0] += numberChildren-1;
      assignProb(pr);
      assignSecondValue(pr2);
      assignMax(maxvalue);
      assignMin(minvalue);
      child = new Vector();*/
      
      globalSum[0] = normalization - sum3*newSize + newSize*sum*sum2/numberChildren;
    }
    else
      bounded = 0;
  }

  return bounded;
}

/**
 * Bounds the tree by substituing nodes whose children are
 * leaves by the average of them. This is done for nodes
 * with an information value lower than a given threshold 
 * <code>limit</code>.
 * @param kindOfApproximation the kind of approximation used when 
 * substituing several leaves by a value.
 * @param limit the information threshold for pruning.
 * @param lowLimit the limit used to decide whether we must consider
 * the tree as not pruned (the method returns 0 or 1) or pruned 
 * (the method returns 2).
 * @param oldSize size of this tree if it were a complete tree.
 * @param globalSum the addition of the values of the original potential.
 * @param numberDeleted an array with a single value storing
 * the number of deleted leaves.
 * @return 
 * <ol>
 * <li> 0 if the resulting tree has not been pruned. Information
 * of its children is greater than <code>limit</code> .
 * <li> 1 if the resulting tree has been pruned but in every case
 * the information values of the pruned nodes are between
 * <code>limit</code> and <code>lowLimit</code>.
 * <li> 2 if the resulting tree has been pruned
 * and the information values of the pruned nodes are smaller than
 * <code>lowLimit</code>.
 * </ol>
 */

public int conditionalPrune2(int kindOfApproximation,
  double limit, double lowLimit, double limitSum,long oldSize,
     double globalSum[], long numberDeleted[]) {
  long newSize;
  int i, numberChildren;
  MultipleTree ch;
  double pr, pr2, // maxvalue, minvalue, 
         sum2=0.0, sum = 0.0, 
         sum3=0.0,
	 entropy = 0.0, info, normalization;
  //Double aux;
  // These variables can contain 0, 1 or 2 and it indicates whether
  // the tree can be pruned (value > 0)
  int bounded = 2, childBounded; 
  
  numberChildren = var.getNumStates();
  newSize = oldSize / numberChildren;
  for (i=0 ; i<numberChildren ; i++) {
    ch = (MultipleTree)child.elementAt(i);    
    if (ch.getLabel() == PROBAB_NODE) {
       pr = ch.getProb();
       pr2 = ch.getSecondValue();
       sum2 += pr2;
       sum3 += pr*pr2;
       sum += pr;
       if (pr > 0.0) {
	 entropy += (pr*pr2 * Math.log(pr));
       }
    }
    else {
      childBounded = ch.conditionalPrune2(kindOfApproximation,
        limit,lowLimit,limitSum,newSize,globalSum,numberDeleted);
      bounded = Math.min(bounded,childBounded);
      if (bounded > 0) {
	ch = (MultipleTree)child.elementAt(i);
	pr = ch.getProb();
	pr2 = ch.getSecondValue();
	sum2 += pr2;
	sum += pr;
	sum3 += pr * pr2;
	if (pr > 0.0) {
	  entropy += (pr*pr2 * Math.log(pr));
	}
      }
    }
  } // end for
  if (bounded > 0) {
    normalization = globalSum[0];
    if (sum <= 0.0)
      info = 0.0;
    else {
      if (normalization > 0.0) {
        info=newSize*(entropy + sum3 * Math.log(sum2/sum3))/normalization;
      }
      else { 
	if(sum2 == 0.0) {
	  info = 0.0;
	} 
	else {
	  info = 1E10;
	}
      }
    }
    if (info <= limit) {
      if (info > lowLimit)
	bounded = 1;
      //approximate(AVERAGEPRODCOND_APPROX);
      if (kindOfApproximation == AVERAGE_APPROX){
        approximate(AVERAGE_APPROX);
      }
      else
	approximate(AVERAGEPRODCOND_APPROX);
      numberDeleted[0] += numberChildren-1;
    }
    else if((newSize * sum3) <= (limitSum * globalSum[0])) {
      bounded=2;
      if (kindOfApproximation == AVERAGEPRODCOND_APPROX){
        approximate(AVERAGEPRODCOND_APPROX);
      }
      else if (kindOfApproximation == ZERO_APPROX){
        approximate(ZERO_APPROX);
      }
      else if (kindOfApproximation == AVERAGE_APPROX){
        approximate(AVERAGE_APPROX);
      }
      else {
        System.out.println("Error in MultipleTree.conditionalPrune2(): illegal value for kindOfApproximation="+kindOfApproximation);
        System.exit(1);
      }   
      numberDeleted[0] += numberChildren-1;   
    }
    else
      bounded = 0;
  }
  return bounded;
}


/**
 * Replace a MultipleTree by an approximate value. This approximate value
 * is calculated depending on the parameter kindOfApproximation
 * @param kindOfApproximation the kind of approximation for the 
 * new approximate value.
 * @see the final static values in MultipleTree for possible values for
 * kindOfApproximation
 */ 
void approximate(int kindOfApproximation){
  double pr=0.0,pr2=0.0,maxvalue,minvalue;

  if(kindOfApproximation==AVERAGE_APPROX){
    pr = average();
    pr2 = conditionalAverage(); 
  }
  else if(kindOfApproximation==AVERAGEPRODCOND_APPROX){
    pr = averageProdCond(var.getNumStates());
    pr2 = conditionalAverage();
  }
  else if(kindOfApproximation==ZERO_APPROX){
    pr = 0.0;
    pr2 = conditionalAverage();
  }
  else{
    System.out.print("Error in MultipleTree.approximate(int): ilegal value for kindOfApproximation="+kindOfApproximation);
    System.exit(1);
  }
  maxvalue = maximum();
  minvalue = minimum();
  //numberDeleted[0] += numberChildren-1;
  assignProb(pr);
  assignSecondValue(pr2);
  assignMax(maxvalue);
  assignMin(minvalue);
  child = new Vector();
}

/**
 * Bounds the tree by substituting nodes whose children are
 * leaves by the average of them. This is done for nodes
 * with an information value lower than a given threshold.
 * The difference with <code>conditionalPrune</code> is he measure of
 * conditional information, which is simpler.
 * @param limit the information threshold for prunning.
 * @param lowLimit the lowLimit threshold fo prunning.
 * @param oldSize size of this tree if it were complete.
 * @param globalSum the addition of the values of the original potential.
 * @param numberDeleted an array with a single value storing
 * the number of deleted leaves.
 */

/*public int conditionalPruneSimple(double limit, double lowLimit,
				  long oldSize, double globalSum[],
				  long numberDeleted[]) {
  
  long newSize;
  int i, numberChildren;
  MultipleTree ch;
  double pr, pr2, maxvalue, minvalue,  sum = 0.0, sum2=0.0,
	 entropy = 0.0, info, normalization;
  //Double aux;
  int bounded = 2, childBounded; 
  
  numberChildren = var.getNumStates();  
  newSize = oldSize / numberChildren;

  for (i=0 ; i<numberChildren ; i++) {
    ch = (MultipleTree)child.elementAt(i);
    
    if (ch.getLabel() == PROBAB_NODE) {
       pr = ch.getProb();
       pr2 = ch.getSecondValue();
       sum2 += pr2;
       sum += pr;
       if (pr > 0.0) {
	 entropy += (pr * Math.log(pr));
       }
    }
    else {
      childBounded = ch.conditionalPruneSimple(limit,lowLimit,
			newSize,globalSum,numberDeleted);
      bounded = Math.min(bounded,childBounded);

      if (bounded > 0) {
	ch = (MultipleTree)child.elementAt(i);
	pr = ch.getProb();
	pr2 = ch.getSecondValue();
	sum2 += pr2;
	sum += pr;
	if (pr > 0.0) {
	  entropy += (pr * Math.log(pr));
	}
      }
    }
  }
  
  if (bounded > 0) {
     normalization = globalSum[0];
    if (sum <= 0.0)
      info = 0.0;
    else {
      if (normalization > 0.0) {
	info = ( 1.0 / normalization) * ( newSize*entropy - newSize*sum*Math.log(sum/numberChildren))*(sum2/numberChildren);
      }
      else {
	if (sum2 == 0.0) {
	  info = 0.0;
	}
	else {
	  info = 1E10;
	}
      }
    }
    
    if (info <= limit) {
      if (info > lowLimit)
	bounded = 1;
      approximate(AVERAGE_APPROX);
      numberDeleted[0] += numberChildren - 1;
    }
    else
      bounded = 0;
  }

  return bounded;
}
*/

/**
 * Combines two trees. The combination is the product of the trees.
 * To be used as a static function.
 * @param tree1 a <code>MultipleTree</code>.
 * @param tree2 a <code>MultipleTree</code>.
 * @return a new <code>MultipleTree</code> resulting from combining
 * <code>tree1</code> and <code>tree2</code>.
 */

public static MultipleTree combine(MultipleTree tree1, MultipleTree tree2) {

  MultipleTree tree, tree3, tree4;
  int i, nv;
  double pr;

  if (tree1.getLabel() == PROBAB_NODE) { // Probability node.
    if (tree2.getLabel() == PROBAB_NODE) {
      pr = tree1.getProb() * tree2.getProb();
      tree = new MultipleTree();
      tree.assignProb(pr);
      tree.assignMax(tree1.getMax() * tree2.getMax());
      tree.assignMin(tree1.getMin() * tree2.getMin());
    }
    else {
      tree = new MultipleTree();
      tree.var = tree2.getVar();
      tree.label = FULL_NODE;
      
      nv=tree2.getChild().size();
      for (i=0 ; i<nv ; i++) {
	tree3 = MultipleTree.combine(tree1,tree2.getChild(i));
	tree.insertChild(tree3);
	tree.leaves += tree3.leaves;
      }
    }
  }
  else {
    tree = new MultipleTree();
    tree.var = tree1.getVar();
    tree.label = FULL_NODE;

    nv = tree1.getChild().size();
    for (i=0 ; i<nv ; i++) {
      tree3 = tree2.restrict(tree1.getVar(),i);
      tree4 = MultipleTree.combine(tree1.getChild(i),tree3);
      tree.insertChild(tree4);
      tree.leaves += tree4.leaves;
    }
  }

  return tree;
}


/**
 * Divides two trees.
 * To be used as a static function.
 *
 * For the exception 0/0, the method computes the result as 0.
 * The exception ?/0: the method reorts an error message to the
 * standard output.  
 *
 * @param tree1 a <code>MultipleTree</code>.
 * @param tree2 a <code>MultipleTree</code>.
 * @return a new <code>MultipleTree</code> resulting from combining
 * <code>tree1</code> and <code>tree2</code>.
 */

public static MultipleTree divide(MultipleTree tree1, MultipleTree tree2) {

  MultipleTree tree, tree3, tree4;
  int i, nv;
  double x, y, x2;

  if (tree1.getLabel() == PROBAB_NODE) { // Probability node.
    if (tree2.getLabel() == PROBAB_NODE) {
      x = tree1.getProb();
      y = tree2.getProb();
      x2 = x; 
      
      if (y == 0.0) {
        if (x == 0.0)
	  x = 0;
        else {
          // System.out.println("Division by zero: " + x2 +"/" + y);
          x = 0;
        }
      }
      else
	x /= y;
      
      if (Double.isInfinite(x))
	System.out.println(x2 + "/" + y + " equals infinite.");
      
      tree = new MultipleTree(x);
      tree.leaves = 1;
    }
    else {
      tree = new MultipleTree();
      tree.var = tree2.getVar();
      tree.label = FULL_NODE;
      tree.leaves = 1;
      
      nv = tree2.child.size();
      for (i=0 ; i<nv ; i++) {
	tree3 = MultipleTree.divide(tree1,tree2.getChild(i));
	tree.insertChild(tree3);
	tree.leaves += tree3.leaves;
      }
    }
  }
  else {
    tree = new MultipleTree();
    tree.var = tree1.getVar();
    tree.label = FULL_NODE;

    nv = tree1.child.size();
    for (i=0 ; i<nv ; i++) {
      tree3 = tree2.restrict(tree1.getVar(),i);
      tree4 = MultipleTree.divide(tree1.getChild(i),tree3);
      tree.insertChild(tree4);
      tree.leaves += tree4.leaves;
    }
  }

  return tree;
}


/**
 * Removes a variable by maximizing over it.
 * @param variable a <code>FiniteStates</code> variable to remove.
 * @return a new <code>MultipleTree</code> with the result of
 * the operation.
 */

public MultipleTree maximizeOverVariable(FiniteStates variable) {

  MultipleTree tree, treeH;
  int i, nv;
  
  
  if (label == PROBAB_NODE)
    tree = new MultipleTree(value); // the value to return is the same.
  else {
    if (var == variable)
      tree = maxChildren();
    else {
      tree = new MultipleTree();
      tree.var = var;
      tree.label = 1;
      tree.leaves = 0;
      
      nv = child.size();
      for (i=0 ; i<nv ; i++) {
	treeH = getChild(i).restrict(var,i).maximizeOverVariable(variable);
	tree.insertChild(treeH);
	tree.leaves += treeH.leaves;
      }
    }
  }
  
  return tree;
}


/**
 * @return a new <code>MultipleTree</code> equal to the maximization of
 * all the children of the tree starting in this node.
 */

public MultipleTree maxChildren() {
 
  MultipleTree tree;
  int i, nv;
  
  tree = getChild(0);
  
  nv = child.size();
  for (i=1 ; i<nv ; i++)
    tree = tree.max(getChild(i));

  return tree;
}


/**
 * @param a a <code>double</code> value.
 * @param b a <code>double</code> value.
 * @return the maximum of <code>a</code> and <code>b</code>.
 */

public double maximum(double a, double b) {
  
  if (a >= b)
    return a;
  else
    return b;
}


/**
 * Integrate the argument tree to this by applying maximization.
 * @param tree a <code>MultipleTree</code>.
 * @return a new <code>MultipleTree</code> with the addition of
 * <code>tree</code> and the tree starting in this node.
 */

public MultipleTree max(MultipleTree tree) {
 
  MultipleTree tree1, tree2, treeH;
  int i, nv;
  
  if (label == PROBAB_NODE) {
    if (tree.getLabel() == PROBAB_NODE) // If both are probabilities
      tree1 = new MultipleTree(maximum(value,tree.getProb()));
    else {
      tree1 = new MultipleTree();
      tree1.var = tree.getVar();
      tree1.leaves = 0;
      tree1.label = 1;

      nv = tree.getChild().size();
      tree2 = new MultipleTree(value);
      for (i=0 ; i<nv ; i++) {
	treeH = tree.getChild(i).restrict(tree.getVar(),i);
	treeH = treeH.max(tree2);
	tree1.insertChild(treeH);
	tree1.leaves += treeH.leaves;
      }
    }      
  }
  else {
    tree1 = new MultipleTree();
    tree1.var = var;
    tree1.leaves = 0;
    tree1.label = 1;
    
    nv = child.size();
    
    for (i=0 ; i<nv ; i++) {
      treeH = getChild(i).restrict(var,i);
      treeH = treeH.max(tree.restrict(var,i));
      tree1.insertChild(treeH);
      treeH.leaves += tree1.leaves;
    }
  }
    
  return tree1;
}


/**
 * Removes a variable by summing up over all its values.
 * @param variable a <code>FiniteStates</code> variable.
 * @return a new <code>MultipleTree</code> with the result of the operation.
 */

public MultipleTree multiAddVariable(FiniteStates variable) {

  MultipleTree tree, treeH;
  int i, nv, ns;
   
  // If it is a probability node this means that the variable passed
  // as argument is not present in the tree. In this case the variable
  // passed as argument is collapsed and it is assigned the same value
  // for all of its states
  if (label == PROBAB_NODE) {
    ns = variable.getNumStates();
    tree = new MultipleTree();
    tree.assignProb(value * ns);
    tree.assignMax(max * ns);
    tree.assignMin(min * ns);
  }
  else {
    // The var passed as argument is just the variable owning the
    // MultipleTree
    if (var == variable){
      // Go an add over the tree
      tree = multiAddChildren();
    }
    else {
      // The node is not a PROBAB_NODE. Create a new tree that will be
      // the result of the summation
      tree = new MultipleTree();
      tree.var = var;
      tree.label = FULL_NODE;
      
      // The summ will be done for every child of the tree
      nv = getChild().size();
      for (i=0 ; i<nv ; i++) {
        // Every child is restricted for the given value for the root variable
        // and the operation will be done for this subtree
	      //treeH = ((MultipleTree)child.elementAt(i)).restrict(var,i).multiAddVariable(variable);
	      treeH = ((MultipleTree)child.elementAt(i)).multiAddVariable(variable);
        // The computed tree will substitute the old tree
	      tree.insertChild(treeH);
        // Update the number of leaves
	      tree.leaves += treeH.leaves;
      }
    }
  }
  return tree;
}


/**
 * Adds the children of this node.
 * @return a new <code>MultipleTree</code> equal to the addition of all the
 * children of this node.
 */

public MultipleTree multiAddChildren() {
  MultipleTree tree;
  int i, nv;

  // The tree for the first value will be used to store the results 
  tree = (MultipleTree)child.elementAt(0);
  
  // Consider the rest of values
  nv = child.size();
  for (i=1 ; i<nv ; i++){
    // The final result is obtained adding the branch for the ith value
    // with the branch for the 0th value
    tree = tree.multiAdd((MultipleTree)child.elementAt(i));
  }
  return tree;
}


/**
 * Adds a <code>MultipleTree</code> to this.
 * @param tree a <code>MultipleTree</code>.
 * @return a new <code>MultipleTree</code> with the addition of
 * <code>tree</code> and the current <code>MultipleTree</code>.
*/

public MultipleTree multiAdd(MultipleTree tree) { 
  MultipleTree tree1, tree2, treeH;
  int i, nv;
 
  if (label == PROBAB_NODE) {
    if (tree.getLabel() == PROBAB_NODE) { 

      // If both are probabilities, get both values and add them
      tree1 = new MultipleTree();
      tree1.assignProb(value + tree.getProb());
      tree1.assignMax(max + tree.getMax());
      tree1.assignMin(min + tree.getMin());
    }
    else {
      // The tree passed as argument is not a probab node. In this
      // case, make a new MultipleTree (tree1), for the same var
      // as the tree passed as argument
      tree1 = new MultipleTree();
      tree1.var = tree.getVar();
      tree1.label = FULL_NODE;

      // Consider the number of branches for the tree passed as
      // argument. the method getChild return a vector with all
      // of the childs for a tree
      nv = tree.getChild().size();

      // Create a new MultipleTree (tree2), as a probab node and
      // assign to it the values, max and min than "this" tree
      tree2 = new MultipleTree();
      tree2.assignProb(value);
      tree2.assignMax(max);
      tree2.assignMin(min);
      
      // Consider all the values for tree passed as argument
      for (i=0 ; i<nv ; i++) {
         // Get the subtree for the ith value
	       //treeH = ((MultipleTree)tree.child.elementAt(i)).restrict(tree.getVar(),i);
	       treeH = ((MultipleTree)tree.child.elementAt(i));

         // Add it to tree2, containing the probab_node for "this" tree
	       treeH = treeH.multiAdd(tree2);

         // The result is inserted into tree1, the final result
	       tree1.insertChild(treeH);
	       tree1.leaves += treeH.leaves;
      }
    }
  }
  else {
    // None of the nodes are PROBAB_NODES. The new MultipleTree (tree1)
    // will be the result of the operation
    tree1 = new MultipleTree();
    tree1.var = var;
    tree1.label = FULL_NODE;
    
    // Get the number of childrens for "this" tree
    nv = getChild().size();
    // Consider every child
    for (i=0 ; i<nv ; i++) {
      // Restrict the child
      //treeH = ((MultipleTree)child.elementAt(i)).restrict(var,i);
      treeH = (MultipleTree)child.elementAt(i);

      // Add it to the subtree comming from the tree passed as argument
      treeH = treeH.multiAdd(tree.restrict(var,i));
      tree1.insertChild(treeH);
      treeH.leaves += tree1.leaves;
    }
  }
  return tree1;
}


/**
 * Gets the list of variables in the tree.
 * @return the list of nodes, as a <code>NodeList</code>.
 */

public NodeList getVarList() {
 
  NodeList list;
  MultipleTree tree;
  int i;
  
  list = new NodeList();
  
  if (label != FULL_NODE)
    return list;
  
  list.insertNode(var);
  
  for (i=0 ; i<var.getNumStates() ; i++) {
    tree = getChild(i);
    list.merge(tree.getVarList());
  }
  
  return list;
}


/**
 * Copies this tree.
 * @return a copy of the tree.
 */

public MultipleTree copy() {

  MultipleTree tree, tree2;
  int i, nv;
  
  tree = new MultipleTree();
  tree.var = var;
  tree.label = FULL_NODE;
  tree.leaves = leaves;

  if (label != PROBAB_NODE) { // If it is not a probability,

    nv = child.size();
      
    for (i=0 ; i<nv ; i++) {
      tree2 = getChild(i).copy();
      tree.child.addElement(tree2);
    }
  }
  else {
    tree.assignProb(value);
    tree.assignSecondValue(secondValue);
    tree.assignMax(max);
    tree.assignMin(min);
  }
  return tree;
}


/**
 * Determines whether a variable is in this tree or not.
 * @param variable a <code>FiniteStates</code> variable.
 * @return <code>true</code> if <code>variable</code> is in some node
 * in the tree, and <code>false</code> otherwise.
 */

public boolean isIn(FiniteStates variable) {
 
  boolean found = false;
  int i;
  
  if (label != FULL_NODE)
    found = false;
  else {
    if (var == variable)
      found = true;
    else {
      for (i=0 ; i<child.size() ; i++) {
	if (getChild(i).isIn(variable)) {
	  found = true;
	  break;
	}
      }
    }
  }
  
  return found;
}


/**
 * Saves the tree to a file.
 * @param p the <code>PrintWriter</code> where the tree will be written.
 * @param j a tab factor (number of blank spaces befor a child
 * is written).
 */

public void save(PrintWriter p,int j) {
  
  int i, l, k;
  
  if (label == PROBAB_NODE)
    p.print(value+"; ["+min+" , "+max+"]\n");
  else {
    p.print("case "+var.getName()+" {\n");
    
    for (i=0 ; i< child.size() ; i++) {
      for (l=1 ; l<=j ; l++)
	p.print(" ");
      
      p.print(var.getPrintableState(i) + " = ");
      getChild(i).save(p,j+10);
    }
    
    for (i=1 ; i<=j ; i++)
      p.print(" ");
    
    p.print("          } \n");
  }        
}


/**
 * Prints a tree to the standard output.
 */

public void print() {
  
  print(10);
}


/**
 * Prints a tree to the standard output.
 * @param j a tab factor (number of blank spaces befor a child
 * is written).
 */

public void print(int j) {
  
  int i, l, k;
  
  if (label == PROBAB_NODE)
    System.out.print(value+" ("+secondValue+")" + "; ["+min+" , "+max+"]\n");
  else {
    System.out.print("case "+var.getName()+" {\n");
    
    for(i=0; i< child.size(); i++) {
      for (l=1 ; l<=j ; l++)
	System.out.print(" ");
      
      System.out.print( var.getPrintableState(i) + " = ");
      getChild(i).print(j+10);
    }
    
    for (i=1 ; i<=j ; i++)
      System.out.print(" ");
    
    System.out.print("          } \n");
  }        
}


/**
 * Normalizes this tree to sum up to 1. The object is modified.
 * @param totalSize size of the completely expanded tree.
 */

public void normalize(long totalSize) {
 
  double total;
  int i, nv;
  
  total = sum(totalSize);
  
  if (label == PROBAB_NODE){
    if(total>0.0){
      value /= total;
      max /= total;
      min /= total;
      if(Double.isNaN(value) || Double.isInfinite(value))
        value=0.0;
    }
    else{
      value = 0.0;
      max = 0.0;
      min = 0.0;
    }
  }
  else {
    nv = var.getNumStates();
    for (i=0 ; i<nv ; i++)
      getChild(i).normalizeAux(total);
  }
}

/*public void normalize(long totalSize) {
 
  double total;
  int i, nv;
  
  total = sum(totalSize);
  
  if (label == PROBAB_NODE) {
    value /= total;
    max /= total;
    min /= total;
  }
  else {
    nv = var.getNumStates();
    for (i=0 ; i<nv ; i++)
      ((MultipleTree)child.elementAt(i)).normalizeAux(total);
  }
}
*/

/**
 * Auxiliar to the previous one. It avoids to unnecessary compute again
 * the addition of the values in the leaves. The object is modified.
 * @param total the addition of the values in the leaves of the tree
 * being normalized.
 */
public void normalizeAux(double total) {
 
  int i, nv;
  
  if (label == PROBAB_NODE){
    if(total>0.0){
      value /= total;
      max /= total;
      min /= total;
      if(Double.isNaN(value) || Double.isInfinite(value))
        value=0.0;
    }
    else{
      value = 0.0;
      max = 0.0;
      min = 0.0;
    }
  }
  else {
    nv = var.getNumStates();
    for (i=0 ; i<nv ; i++)
      getChild(i).normalizeAux(total);
  }  
}



/*public void normalizeAux(double total) {
 
  int i, nv;
  
  
  if (label == PROBAB_NODE) {
    value /= total;
    max /= total;
    min /= total;
  }
  else {
    nv = var.getNumStates();
    for (i=0 ; i<nv ; i++)
      ((MultipleTree)child.elementAt(i)).normalizeAux(total);
  }  
}
*/

/**
 * Computes the addition of all the values in the tree.
 * @param treeSize size of the completely expanded tree.
 * @return the addition computed.
 */

public double sum(long treeSize) {
 
  double s = 0.0;
  int i, nv;
  long newSize;
  
  if (label == PROBAB_NODE)
    s = (double)treeSize * value;
  else {
    nv = var.getNumStates();
    newSize = treeSize / nv;
    for (i=0 ; i<nv ; i++)
      s += getChild(i).sum(newSize);
  }
  
  return s;
}


/**
 * Computes the mean of the values in the tree.
 * @return the mean.
 */

public double average() {

  double av = 0.0;
  int i, nv;
  
  if (label == PROBAB_NODE)
    av = value;
  else {
    nv = var.getNumStates();
    for (i=0 ; i<nv ; i++)
      av += getChild(i).average();
    av /= (double)nv;
  }
  
  return av;
}

/**
 * Computes the mean of the product of value and secondValue in this tree
 * conditioned to secondValues
 * @return the mean.
 */

public double averageProdCond(int treeSize) {
  double prodsum,condSum;
  
  prodsum=conditionalProdSum(treeSize); 
  condSum=conditionalSum(treeSize);
  if(condSum>0.0)
    return (prodsum/condSum);
  else
    return(0.0);
}



/**
 * Computes the information of a variable within a tree.
 * @param variable a <code>FiniteStates</code> variable.
 * @param potentialSize maximum size of the potential containing
 * the tree (the number of values if it were completely expanded).
 * @return the value of information of <code>variable</code>.
 */

public double information(FiniteStates variable, long potentialSize) {
 
  MultipleTree tree;
  int i, nv;
  long newSize;
  double entropy = 0.0, s = 0.0, totalS = 0.0, info = 0.0;
  
  nv = variable.getNumStates();
  newSize = potentialSize / nv;
  
  for (i=0 ; i<nv ; i++) {
    tree = restrict(variable,i);
    s = tree.sum(newSize);
    entropy += -s * Math.log(s);
    totalS += s;
  }
  
  if (totalS == 0.0)
    info = 0.0;
  else
    info = Math.log(nv) - Math.log(totalS) - (entropy / totalS);

 return (totalS * info);
}



/**
 * @return the configuration of maximum probability included in the
 * tree, that is consistent with the subConfiguration passed as
 * parameter (this subconfiguration can be empty).
 *
 * NOTE: if there are more than one configuration with maximum 
 * probability, the first one is returned.
 *
 * @param bestExpl the best explanation found until this moment
 *  (configuration + probability)
 * @param conf the configuration built following the path from
 *        this node until the root.
 * @param subconf the subconfiguration to ensure consistency.
 */

public Explanation getMaxConfiguration(Explanation bestExpl,
                                         Configuration conf,
                                         Configuration subconf) {
  
  int pos, numChildren, i;
  MultipleTree tree;
  Explanation exp=new Explanation();
  double prob;
  Configuration c;

  if (isProbab()) {
    if (value > bestExpl.getProb()) {
      bestExpl.setProb(value);
      
      // setting in conf the values of subconf
      for(i=0 ; i<subconf.size() ; i++)
        conf.putValue(subconf.getVariable(i),subconf.getValue(i));      

      // copying conf in bestExpl
      for (i=0 ; i<conf.size() ; i++)
        bestExpl.getConf().putValue(conf.getVariable(i),conf.getValue(i));
    }
  }
  else if (isVariable()) {
    pos = subconf.indexOf(var);
    if (pos == -1) { // all the children are explored
      numChildren = child.size();

      prob = -1.0;
      for(i=0 ; i<numChildren ; i++) {
        tree = (MultipleTree) child.elementAt(i);
        conf.putValue(var,i);
        bestExpl = tree.getMaxConfiguration(bestExpl,conf,subconf);
        if (bestExpl.getProb() > prob){
          prob = bestExpl.getProb();
          c = (bestExpl.getConf()).duplicate(); 
          exp = new Explanation(c,prob);
        }
      }
      bestExpl = exp;
    }
    else { // only the child with the value in subconf is explored
      tree = (MultipleTree) child.elementAt(subconf.getValue(pos));
      conf.putValue(var,subconf.getValue(pos));
      bestExpl = tree.getMaxConfiguration(bestExpl,conf,subconf);
    } 
    // restoring the value -1 for var in conf
    conf.putValue(var,-1);
  }
  else { // empty node
    System.out.println("Error detected at MultipleTree.getMaxConfiguration");
    System.out.println("An empty node was found");
    System.exit(0);
  }

  return bestExpl;
}


/**
 * @return the configuration of maximum probability included in the
 * potential, that is consistent with the subConfiguration passed as
 * parameter (this subconfiguration can be empty), and differents to
 * all the configurations passed in the vector        
 *
 * NOTE: if there are more than one configuration with maximum 
 * probability, the first one is returned
 *
 * @param bestExpl the best explanation found until this moment
 *  (configuration + probability)
 * @param conf the configuration built following the path from
 *        this node until the root.
 * @param subconf the subconfiguration to ensure consistency
 * @param list the list of configurations to be differents     
 */

public Explanation getMaxConfiguration(Explanation bestExpl,
				       Configuration conf,
				       Configuration subconf,
				       Vector list) {

  int pos, numChildren, i;
  MultipleTree tree;
  Explanation exp=new Explanation();
  Configuration firstConf,c; 
  double prob;

  if (isProbab()) {
    if (value > bestExpl.getProb()) {
      // setting in conf the values of subconf
      for(i=0 ; i<subconf.size() ; i++)
        conf.putValue(subconf.getVariable(i),subconf.getValue(i));      
      
      firstConf = conf.getFirstNotInList(list);

      if (firstConf.size() > 0) {
        bestExpl.setProb(value);
        // copying conf in bestExpl
        if (bestExpl.getConf().size() == 0) 
          bestExpl.setConf(new Configuration(conf.getVariables()));
        for (i=0 ; i<conf.size() ; i++)
          bestExpl.getConf().putValue(firstConf.getVariable(i),
                                      firstConf.getValue(i));
      }
    }
  }
  else if (isVariable()) {
    pos = subconf.indexOf(var);

    if (pos == -1) { // all the children are explored
      numChildren = child.size();
      prob = -1.0;
      for(i=0 ; i<numChildren ; i++) {
        tree = (MultipleTree) child.elementAt(i);
        conf.putValue(var,i);
        bestExpl = tree.getMaxConfiguration(bestExpl,conf,subconf,list);
        if (bestExpl.getProb() > prob){
          c = (bestExpl.getConf()).getFirstNotInList(list);
          if (c.size()>0){
            prob = bestExpl.getProb();   
            c = (bestExpl.getConf()).duplicate();
            exp = new Explanation(c,prob);
          }
        }
      }
      bestExpl = exp;
    }
    else { // only the child with the value in subconf is explored
      tree = (MultipleTree) child.elementAt(subconf.getValue(pos));
      conf.putValue(var,subconf.getValue(pos));
      bestExpl = tree.getMaxConfiguration(bestExpl,conf,subconf,list);
    } 
    // restoring the value -1 for var in conf
    conf.putValue(var,-1);
  }
  else { // empty node
    System.out.println("Error detected at MultipleTree.getMaxConfiguration");
    System.out.println("An empty node was found");
    System.exit(0);
  }


  return bestExpl;
}




/**
 * Determines whether a node is a leaf (a probability value).
 * @return <code>true</code> if the node is a probability and
 * <code>false</code> otherwise.
 */

public boolean isProbab() {
  
  if (label == PROBAB_NODE)
    return true;
  
  return false;
}


/**
 * Determines whether a node is internal (a variable).
 * @return <code>true</code> if the node is a variable and
 * <code>false</code> otherwise.
 */

public boolean isVariable() {
  
  if (label == FULL_NODE)
    return true;
  
  return false;
}


/**
 * Determines whether a node is empty.
 * @return <code>true</code> if the node is empty and
 * <code>false</code> otherwise.
 */

public boolean isEmpty() {
  
  if (label == EMPTY_NODE)
    return true;
  
  return false;
}


/**
 * Bounds the tree by substituting nodes whose children are
 * leaves by the average of them. This is done for nodes
 * with an information value lower than a given threshold.
 * @param limit the information threshold for pruning.
 * @param oldSize size of this tree if it were complete.
 * @param globalSum the addition of the original potential.
 * @param numberDeleted an array with a single value storing
 * the number of deleted leaves.
 * @return <code>true</code> if the tree has been reduced to a probability 
 * node; <code>false</code> otherwise.
 */

public boolean prune(double limit, long oldSize, double globalSum,
		     long numberDeleted[]) {

  long newSize;
  int i, numberChildren;
  MultipleTree ch;
  double pr, sum = 0.0, entropy = 0.0, info, maxValue=0, minValue=1e20;
  boolean bounded = true, // tell if the tree can be reduced to a probab. node
          childBounded; 

  numberChildren = var.getNumStates();
  newSize = oldSize / numberChildren;
  
  for (i=0 ; i<numberChildren ; i++) {
    ch = getChild(i);
    
    if (ch.label == PROBAB_NODE) {
       pr = ch.value;
       sum += pr;
       entropy += (-pr * Math.log(pr));
       if (ch.max > maxValue)
         maxValue=ch.max;
       if (ch.min < minValue)
         minValue=ch.min;
    }
    else {
      long chOldLeaves=ch.leaves;
      childBounded = ch.prune(limit,newSize,globalSum,numberDeleted);
      leaves-=(chOldLeaves-ch.leaves);

      if (!childBounded){
	      bounded = false;
      }

      if (bounded) {
	      ch = getChild(i);
	      pr = ch.value;
        if (ch.max > maxValue)
          maxValue=ch.max;
        if (ch.min < minValue)
          minValue=ch.min;
	      sum += pr;
	      entropy += (-pr * Math.log(pr));
      }
    }
  }
  
  if (bounded) {
    if (sum <= 0.0)
      info = 0.0;
    else
      info = ((newSize * sum) / globalSum) *
	(Math.log(numberChildren) - Math.log(sum) - entropy / sum);
   
    if (info <= limit) {
      pr = average();
      numberDeleted[0] += numberChildren-1; 
      assignProb(pr);
      assignMin(minValue);
      assignMax(maxValue);
      child = new Vector();
    }
    else
      bounded = false;
  }
  return bounded;
}

/**
 * Gets the transparent variables contained in a tree
 * @return a Vector with the list of transparent variables
 */
public Vector getListTransparents(){
  NodeList transVars=new NodeList();
  Vector result=new Vector();

  // Check if it is a leaf node. In this case, return
  if (label == PROBAB_NODE)
    return result;

  // If not, check if the root variable is transparent
  if (var.getTransparency() == FiniteStates.TRANSPARENT){
    // Add it
    transVars.insertNode(var);
  }

  // Anyway, go on the childs, if any
  for(int i=0; i < var.getNumStates(); i++){
    MultipleTree child=getChild(i);

    // Go on looking over the subtree
    child.getListTransparents(transVars);
  }

  // Finally, covert the NodeList into a Vector
  for(int i=0; i < transVars.size(); i++){
    result.addElement(transVars.elementAt(i));
  }
  return result;
}

/**
 * Gets the transparent variables of this tree and store
 * the found variables in a NodeList passed as argument
 * @param transVars NodeList where the transparent
 * variables are inserted
 */
private void getListTransparents(NodeList transVars){
  // If it is a label nodel, return
  if (label == PROBAB_NODE)
    return;

  // If the root var is transparent and is not included in transVars,
  // include it
  if (var.getTransparency() == FiniteStates.TRANSPARENT){
    if (transVars.getId(var) == -1)
      transVars.insertNode(var);
  }

  // If the node is a complete node, go on the childs
  for(int i=0; i < var.getNumStates(); i++){
    MultipleTree child=getChild(i);

    // Go on looking over it
    child.getListTransparents(transVars);
  }
}


} // End of class
