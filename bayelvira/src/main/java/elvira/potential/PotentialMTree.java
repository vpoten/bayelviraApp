/* PotentialMTree.java */

package elvira.potential;

import java.util.Vector;
import java.util.Hashtable;
import java.io.*;
import elvira.*;
import elvira.inference.abduction.Explanation;



/**
 * Implementation of class <code>PotentialMTree</code>. A potential whose
 * values are represented by a probability tree
 * of class <code>MultipleTree</code>.
 *
 * @since 6/3/2003
 */



public class PotentialMTree extends Potential {


/**
 * A <code>MultipleTree</code> with the values of this 
 * <code>PotentialMTree</code>.
 */
MultipleTree values;

/**
 * The number of leaves of the <code>MultipleTree values</code>.
 */
long size;

/**
 * This variable is equal to <code>true</code> if <code>values</code>
 * represents an exact potential, and <code>false</code> otherwise.
 * Every constructor sets <code>isExact</code> to <code>true</code>.
 */
protected boolean isExact;



/**
 * Contains the normalization factor of this PotentialMTree. This variables
 * is set in <code>conditionalLimitBound</code>.
 */
private double normalizationFactor;


/* CONSTRUCTORS */


/**
 * Creates a new <code>PotentialMTree</code> with no variables and
 * an empty tree.
 */

public PotentialMTree() {

  variables = new Vector();
  values = new MultipleTree();
  size = 0;
  isExact = true;
}


/**
 * Creates a <code>PotentialMTree</code> from a <code>PotentialTree</code>.
 * The result is assumed to be exact.
 * @param pot the <code>PotentialTree</code> to be converted.
 */

public PotentialMTree(PotentialTree pot) {

  variables = (Vector)pot.getVariables().clone();
  size = pot.getSize();

  values = new MultipleTree(pot.getTree());
  isExact = true;
}

/**
 * Creates a new PotentialMTree given a PotentialTable
 * The result is assumed to be exact
 * @param pot the potential to transform
 */
public PotentialMTree(PotentialTable pot) {
  variables=(Vector)pot.getVariables().clone();
  size=pot.getSize();
  values=new MultipleTree(pot.toTree().getTree());
  isExact=true;
}


/**
 * Creates a new <code>PotentialMTree</code> with an empty tree.
 * @param vars <code>Vector</code> of variables (<code>FiniteStates</code>)
 * that the potential will contain.
 */

public PotentialMTree(Vector vars) {

  variables = (Vector)vars.clone();
  values = new MultipleTree();
  size = 0;
  isExact = true;
}


/**
 * Creates a new <code>PotentialMTree</code> with an empty tree.
 * @param vars variables that the potential will contain,
 * given as a <code>NodeList</code>.
 */

public PotentialMTree(NodeList vars) {

  variables = (Vector)vars.getNodes().clone();
  values = new MultipleTree();
  size = 0;
  isExact = true;
}


/**
 * Sets the <code>PotentialMTree</code> as exact or not
 * @param exact <code>true</code> if the <code>PotentialMTree</code>
 * will be exact or <code>false</code> otherwise.
 */

public void setExact(boolean exact) {

  isExact = exact;
}


/**
 * Tells whether the potential is exact or not.
 * @return <code>true</code> if the potential is exact
 * or <code>false</code> otherwise.
 */

public boolean getExact() {

  return isExact;
}


/**
  * Returns the normalization factor of this PotentialMTree. This number
  * is only set in <code>conditionalLimitBound</code>
  */
public double getNormalizationFactor(){
  return normalizationFactor;
}


/**
 * Assigns a tree to the potential.
 * @param tree the <code>MultipleTree</code> to be assigned.
 */

public void setTree(MultipleTree tree) {

  values = tree;
  size = tree.getSize();
}


/**
 * Gets the values of the potential.
 * @return the <code>MultipleTree</code> associated with the potential.
 */

public MultipleTree getTree(){

  return values;
}


/**
 * Gets the size (number of values) of the potential.
 * @return the number of values (size) of the potential.
 */

public long getSize() {

  return size;
}


/**
 * Gets the value for a configuration.
 * @param conf a <code>Configuration</code> to evaluate.
 * @return the value corresponding to configuration <code>conf</code>.
 */

public double getValue(Configuration conf) {

  return values.getProb(conf);
}


/**
 * Gets the value for a configuration. In this case, the
 * configuration is represented by means of an array of <code>int</code>.
 * At each position, the value for certain variable is stored.
 * To know the position in the array corresponding to a given
 * variable, we use a hash table. In that hash table, the
 * position of every variable in the array is stored.
 *
 * @param positions a <code>Hashtable</code>.
 * @param conf an array of <code>int</code> with the values of the variables.
 * @return the value corresponding to configuration <code>conf</code>.
 */

public double getValue(Hashtable positions, int[] conf) {

  return values.getProb(positions,conf);
}


/**
 * Sets the value for a configuration. If the configuration is not inserted
 * in the tree, a new branch is created.
 *
 * @param conf a <code>Configuration</code>.
 * @param x a <code>double</code>: the new value for <code>conf</code>.
 */

public void setValue(Configuration conf, double x) {

  Configuration aux;
  MultipleTree tree;
  FiniteStates var;
  int i, p, val, s;
  boolean update;

 
  update = true;
  aux = conf.duplicate();
  s = conf.getVariables().size();

  tree = values;

  for (i=0 ; i<s ; i++) {

    if (tree.getLabel() != 1) {
      var = aux.getVariable(0);
      val = aux.getValue(0);
      aux.remove(0);

      if (tree.getLabel() == 2) // if the node is a probability,
	       update = false; // do not update the number of leaves.
      tree.assignVar(var);
    }
    else {
      p = aux.indexOf(tree.getVar());
      var = aux.getVariable(p);
      val = aux.getValue(p);
      aux.remove(p);
    }

    tree = (MultipleTree)tree.getChild(val);
  }

  tree.assignProb(x);

  if (update)
    size++;
}

/**
 * Sets the values for a configuration. If the configuration is not inserted
 * in the tree, a new branch is created.
 *
 * @param conf a <code>Configuration</code>.
 * @param prob a <code>double</code>: the new value for <code>conf</code>.
 * @param min a <code>double</code>: the new min value for <code>conf</code>.
 * @param max a <code>double</code>: the new max value for <code>conf</code>.
 */

public void setValue(Configuration conf, double prob, double min, double max) {
  Configuration aux;
  MultipleTree tree;
  FiniteStates var;
  int i, p, val, s;
  boolean update;

 
  update = true;
  aux = conf.duplicate();
  s = conf.getVariables().size();

  tree = values;

  for (i=0 ; i<s ; i++) {

    if (tree.getLabel() != 1) {
      var = aux.getVariable(0);
      val = aux.getValue(0);
      aux.remove(0);

      if (tree.getLabel() == 2) // if the node is a probability,
	       update = false; // do not update the number of leaves.
      tree.assignVar(var);
    }
    else {
      p = aux.indexOf(tree.getVar());
      var = aux.getVariable(p);
      val = aux.getValue(p);
      aux.remove(p);
    }

    tree = (MultipleTree)tree.getChild(val);
  }

  tree.assignProb(prob);
  tree.assignMin(min);
  tree.assignMax(max);

  if (update)
    size++;
}


/**
 * Gets the addition of the values of the potential
 * @return the addition of all the values in the potential.
 */

public double totalPotential() {

  long s;

  s = (long)FiniteStates.getSize(variables);

  return values.sum(s);
//values.conditionalSum(s);
}


/**
 * @param conf a Configuration.
 * @return the sum of all the values in the potential
 * matching with configuration <code>conf</code>. The result is the same
 * as restricting the potential to <code>conf</code> and then using
 * totalPotential()
 * @see totalPotencial()
 */

public double totalPotential(Configuration conf) {

  Configuration auxConf;
  FiniteStates temp;
  int i, nv;
  double sum;

  nv = 1;

  for (i=0 ; i<variables.size() ; i++) {
    temp = (FiniteStates)variables.elementAt(i);
    nv = nv * temp.getNumStates();
  }

  // Evaluate the tree for all the possible configurations
  // and sum the values.

  auxConf = new Configuration(variables,conf);
  sum = 0.0;

  for (i=0 ; i<nv ; i++) {
    sum += getValue(auxConf);
    auxConf.nextConfiguration(conf);
  }

  return sum;
}


/**
 * @return the entropy of the potential.
 */

public double entropyPotential() {

  Configuration auxConf;
  FiniteStates temp;
  int i, nv;
  double sum, x;


  nv = 1;

  for (i=0 ; i<variables.size() ; i++) {
    temp=(FiniteStates)variables.elementAt(i);
    nv = nv * temp.getNumStates();
  }

  // Evaluate the tree for all the configurations and
  // compute the entropy.

  auxConf = new Configuration(variables);
  sum = 0.0;

  for (i=0 ; i<nv ; i++) {
    x = getValue(auxConf);
    if (x>0.0)
      sum += x * Math.log(x);
    auxConf.nextConfiguration();
  }

  return ((-1.0)*sum);
}


/**
 * @param conf a Configuration.
 * @return the entropy of the values of the potential
   matching with configuration conf. The result is the
   same as restricting first to conf and then using
   entropyPotential().
 * @see entropyPotential()
 */

public double entropyPotential(Configuration conf) {

  Configuration auxConf;
  FiniteStates temp;
  int i, nv;
  double sum, x;


  nv = 1;
  for (i=0; i<variables.size(); i++) {
    temp=(FiniteStates)variables.elementAt(i);
    nv = nv * temp.getNumStates();
  }


  // Evaluate the tree for all the configurations and
  // compute the entropy.

  auxConf=new Configuration(variables,conf);
  sum=0.0;

  for (i=0 ; i<nv ; i++) {
    x = getValue(auxConf);
    if (x>0.0)
      sum += x * Math.log(x);
    auxConf.nextConfiguration(conf);
  }

  return ((-1.0)*sum);
}


/**
 * Restricts the potential to a given configuration.
 * @param conf restricting configuration.
 * @return Returns a new PotentialMTree where variables
 * in conf have been instantiated to their values in conf.
 */

public Potential restrictVariable(Configuration conf) {
 
  Vector aux;
  FiniteStates temp;
  PotentialMTree pot;
  MultipleTree tree;
  int i, p, s, v;

  s = variables.size();
  aux = new Vector(s); // New list of variables.
  tree = getTree(); // tree will be the new tree


  for (i=0 ; i<s ; i++) {
    temp = (FiniteStates)variables.elementAt(i);
    p = conf.indexOf(temp);

    if (p==-1) // If it is not in conf, add to the new list.
      aux.addElement(temp);
    else {     // Otherwise, restrict the tree to it.
      v = conf.getValue(p);
      tree = tree.restrict(temp,v);
    }
  }

  pot = new PotentialMTree(aux);
  pot.setTree(tree);

  return pot;
}


/**
 * Combines this protential with the argument.
 * @param p a PotentialMTree.
 * @returns a new PotentialMTree consisting of the combination
 * of p and this PotentialMTree.
 */

public Potential combine(Potential pMTree) {
 

  Vector v, v1, v2;
  FiniteStates aux;
  int i, nv;
  PotentialMTree pot,p;
  double x;
  MultipleTree tree, tree1, tree2;


  p = (PotentialMTree)pMTree;

  v1 = variables; // Variables of this potential.

  v2 = p.variables; // Variables of the argument.

  v = new Vector(); // Variables of the new potential.

  for (i=0 ; i<v1.size() ; i++) {
    aux=(FiniteStates)v1.elementAt(i);
    v.addElement(aux);
  }


  for (i=0 ; i<v2.size() ; i++) {
    aux=(FiniteStates)v2.elementAt(i);
    if (aux.indexOf(v1)==-1)
      v.addElement(aux);
  }


  // The new Potential.

  pot=new PotentialMTree(v);


  tree1 = getTree(); // Tree of this potential.

  tree2 = p.getTree(); // Tree of the argument.

  tree = MultipleTree.combine(tree1,tree2); // The new tree.

  pot.setTree(tree);

  return pot;
}


public PotentialMTree combine(PotentialMTree p) {

  return (PotentialMTree)combine((Potential)p);
}


/**
 * Combines two potentials. The argument <code>p</code> MUST be a subset of
 * the potential which receives the message, and must be a
 * <code>PotentialMTree</code>.
 *
 * IMPORTANT: this method modifies the object which receives the message.
 *
 * @param p the <code>PotentialMTree</code> to combine with this.
 */

public void combineWithSubset(Potential p) {

  MultipleTree mTree;
  
  mTree = MultipleTree.combine(this.getTree(),
                                 ((PotentialMTree)p).getTree());
  this.setTree(mTree);
}


/**
 * This method divides two potentials.
 * For the exception 0/0, the method computes the result as 0.
 * The exception ?/0: the method aborts with a message in the standar output.
 * @param p the <code>PotentialMTree</code> to divide with this.
 * @return a new <code>PotentialMTree</code> with the result of
 * dividing this potential by <code>p</code>.
 */

public Potential divide(Potential p) { 

  Vector v, v1, v2;
  FiniteStates aux;
  int i, nv;
  PotentialMTree pot;
  double x;
  MultipleTree tree, tree1, tree2;
  
  v1 = variables;   // Variables of this potential.
  v2 = p.variables; // Variables of the argument.
  v = new Vector(); // Variables of the new potential.

  for (i=0 ; i<v1.size() ; i++) {
    aux = (FiniteStates)v1.elementAt(i);
    v.addElement(aux);
  }

  for (i=0 ; i<v2.size() ; i++) {
    aux = (FiniteStates)v2.elementAt(i);
    if (aux.indexOf(v1) == -1)
      v.addElement(aux);
  }

  // The new Potential.
  pot = new PotentialMTree(v);
  
  tree1 = getTree();                          // Tree of this potential.
  tree2 = ((PotentialMTree)p).getTree();       // Tree of the argument.
  
  tree = MultipleTree.divide(tree1,tree2); // The new tree.
  
  pot.setTree(tree);

  return pot;
}



/**
 * Removes a list of variables by adding over all their states.
 * @param vars vector of FiniteStates.
 * @return A new PotentialMTree with the result of the operation.
 */

public PotentialMTree addVariable(Vector vars) {


  Vector aux;
  FiniteStates var1, var2;
  int i, j;
  boolean found;
  PotentialMTree pot;
  MultipleTree tree;


  aux=new Vector(); // New list of variables.

  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;

    for (j=0 ; j<vars.size() ; j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1==var2) {
	found = true;
	break;
      }
    }
    
    if (!found)
      aux.addElement(var1);
  }

  pot = new PotentialMTree(aux); // The new tree.

  tree = values;

  for (i=0 ; i<vars.size() ; i++) {
    var1 = (FiniteStates)vars.elementAt(i);
    tree = tree.multiAddVariable(var1);
  }

  pot.setTree(tree);
  
  return pot;
}


/**
 * Removes the argument variable summing over all its values.
 * @param var a Node variable.
 * @return a new PotentialMTree with the result of the deletion.
 */

public Potential addVariable(Node var) {

  Vector v;
  PotentialMTree pot;
  
  v = new Vector();
  v.addElement(var);
  
  pot = addVariable(v);
  
  return pot;
}


/**
 * Removes a list of variables by applying marginalization by maximum.
 * @param vars a <code>Vector</code> of <code>FiniteStates</code> variables.
 * @return a new <code>PotentialMTree</code> with the marginal.
 */

public Potential maxMarginalizePotential(Vector vars) {

  Vector aux;
  FiniteStates var1, var2;
  int i, j;
  boolean found;
  PotentialMTree pot;
  MultipleTree tree;

  
  aux = new Vector(); // New list of variables.
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;
    
    for (j=0 ; j<vars.size() ; j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    
    if (!found)
      aux.addElement(var1);
  }

  pot = new PotentialMTree(vars); // The new tree.

  tree = values;
  
  for (i=0 ; i<aux.size() ; i++) {
    var1 = (FiniteStates)aux.elementAt(i);
    tree = tree.maximizeOverVariable(var1);
  }

  pot.setTree(tree);
  
  return pot;
}


/**
 * Marginalizes a PotentialMTree to a list of variables.
 * It is equivalent to remove the other variables.
 * @param vars a vector of FiniteStates variables.
 * @return a new PotentialMTree with the marginal.
 * @see addVariable(Vector vars)
 */

public Potential marginalizePotential(Vector vars) {

  Vector v;
  int i, j;
  boolean found;
  FiniteStates var1, var2;
  PotentialMTree pot;

  v = new Vector(); // List of variables to remove
                    // (those not in vars).
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;
    for (j=0 ; j<vars.size(); j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
      
    if (!found)
      v.addElement(var1);
  }

  pot = addVariable(v);

  return pot;
}


/**
 * Updates the actual size of the potential.
 */

public void updateSize() {

  values.updateSize();
  size = values.getSize();
}


/**
 * @return a copy of this PotentialMTree.
 */

public Potential copy() {
 
  PotentialMTree pot;
  
  pot = new PotentialMTree(variables);
  pot.size = size;
  pot.values = values.copy();
  
  return pot;
}


/**
 * Normalizes this potential to sum up to one.
 */

public void normalize() {

  long totalSize;
  
  totalSize = (long)FiniteStates.getSize(variables);
  values.normalize(totalSize);
}


/**
 * Computes a new PotentialMTree from this conditional on another
 * potential.
 * @param condPot the conditioning potential.
 * @return a new PotentialMTree where the tree will be of class
 * MultipleTree, with the first value as in this and the second value
 * equal to the conditional on the argument Potential.
 */

public Potential conditional(Potential condPot) { 

  PotentialMTree pot;
  MultipleTree condTree;
  long totalSize;
  Configuration conf;

  conf = new Configuration();
  totalSize = (long)FiniteStates.getSize(condPot.getVariables());
  condTree = ((PotentialMTree)condPot).getTree();
  
  pot = new PotentialMTree(variables);
  pot.size = size;
  pot.values=((MultipleTree)values).conditional(condTree,totalSize,conf);
  
  return pot; 
}


/**
 * Sorts the variables in the tree and limits the number of leaves.
 * Sets <code>isExact</code> to false if the method carry out an
 * approximation in the <code>PotentialMTree</code>.
 * This version is for trees of class MultipleTree.
 * @param maxLeaves maximum number of leaves in the new tree.
 * @param method the method of prunning: 0 for conditional
 * prunning or 1 for max-min prunning.
 * @return a new PotentialMTree sorted and bounded.
 */

public PotentialMTree conditionalSortAndBound(int maxLeaves,
					      int method) {
 
  PotentialMTree pot;
  MultipleTree treeNew, treeSource, treeSource2, treeResult;
  NodeQueueM nodeQ;
  PriorityQueueM queue;
  FiniteStates var;
  int j, nv;
  long newSize, maxSize;
  boolean isAnApproxTree=false;
  double normalization;


  // Size of the entire tree (expanded)
  maxSize = (long)FiniteStates.getSize(variables);


   
  nodeQ = new NodeQueueM(1E20); // Infinity node.
  
  // Priority queue where the tree nodes will be stored
  // sorted according to their information value.
  queue = new PriorityQueueM(nodeQ);
  
  // The new potential (with the same variables as this).

  pot = new PotentialMTree(variables);
 

  treeNew = new MultipleTree();
  treeSource = (MultipleTree)values;


  if (method==1) {
    normalization = treeSource.sum(maxSize) * treeSource.conditionalAverage();
  }
  else if (method==2) {
    normalization = treeSource.sum(maxSize)/treeSource.conditionalAverage();
  }
  else {normalization = treeSource.sum(maxSize);}

  pot.setTree(treeNew);
  

  
  if (!values.isProbab()) {  // If the tree node is not a probab.
                             // put it in the queue.

    nodeQ = new NodeQueueM(treeNew,treeSource,maxSize,method,normalization);
    queue.insert(nodeQ);
    

    // While the size is not exceeded, add new nodes to the tree.

    while (!queue.isEmpty() &&
	   ((pot.size + queue.size()) < maxLeaves)) {

      nodeQ = queue.deleteMax();
      treeResult = (MultipleTree)nodeQ.getRes();
      treeSource = (MultipleTree)nodeQ.getSource();
      var = nodeQ.getVar();
      treeResult.assignVar(var);
      nv = var.getNumStates();
      newSize = maxSize / nv;

 
      normalization = normalization + nodeQ.getUpdateNormalization();
      
      // For each child of the selected node:

      for (j=0 ; j<nv ; j++) {
	treeSource2 = treeSource.restrict(var,j);
	treeNew = treeResult.getChild(j);
	if (treeSource2.getLabel()!=2) { // If the tree node is not
                                     // a prob. put it in the queue.

	  nodeQ = new NodeQueueM(treeNew,treeSource2,newSize,method,normalization);
	  queue.insert(nodeQ);
	}
	else {
	  treeNew.assignProb(treeSource2.getProb());
	  treeNew.assignSecondValue(treeSource2.getSecondValue());
	  treeNew.assignMax(treeSource2.getMax());
	  treeNew.assignMin(treeSource2.getMin());
	  pot.size++;
	}
      }
    }


    // Substitute the remaining nodes by the average value.

    if(!queue.isEmpty()) {
      isAnApproxTree=true;

      while (!queue.isEmpty()) {
	nodeQ = queue.deleteMax();
	treeResult = (MultipleTree)nodeQ.getRes();
	treeSource = (MultipleTree)nodeQ.getSource();
	treeResult.assignProb(treeSource.average());
	treeResult.assignSecondValue(treeSource.conditionalAverage());
	treeResult.assignMin(treeSource.minimum());
	treeResult.assignMax(treeSource.maximum());
	pot.size++;
      }
    }
  }
  else {
    treeNew.assignProb(values.getProb());
    treeNew.assignSecondValue(values.getSecondValue());
    treeNew.assignMax(values.getMax());
    treeNew.assignMin(values.getMin());
    pot.size++;
  }

  if(!this.getExact())
    pot.setExact(false);
  else if(isAnApproxTree)
    pot.setExact(false);
  else
    pot.setExact(true);


  return pot;
}


/**
 * Bounds the tree associated with the potential by removing
 * nodes which information value is lower than a given threshold.
 * The tree is modified.
 * This method is for trees of class MultipleTree only.
 * @param kindOfApproximation the kind of approximation used when 
 * substituing several leaves by a value.
 * @param limit the information limit.
 * @param lowLimit the limit used to decide if we must considered
 * the tree as not pruned or pruned (exact or not exact)
 * @param limitSum a limit used to prune when the sum of the leave is under
 * this limit with respect to the total sum of the tree. This parameter is
 * only taken into account when infoMeasure==2 and 
 * kindOfApproximation==ZERO_APPROX. 
 * @param infoMeasure: 
 * <ul>
 * <li> 1 for the method of calculating entropy published in
 * "Penniless Propagation in Join Trees (IJIS-2000)" 
 * <li> 2 for the improved method published in "Different Strategies
 * to Approximate Probability Trees in Penniless Propagation"
 * (CAEPIA-2001).
 * </ul>
 * @see MultipleTree.conditionalPrune1() and MultipleTree.conditionalPrune2()
 */

public void conditionalLimitBound(int kindOfApproximation,double limit,double lowLimit,double limitSum,int infoMeasure) {
  long maxSize;
  long [] numberDeleted;
  int bounded = 0;
  double [] globalSum;

  numberDeleted = new long[1]; // Number of deleted nodes.
  globalSum = new double[1];
  maxSize = (long)FiniteStates.getSize(variables);

  if ((infoMeasure==1) || (infoMeasure==2)){
    normalizationFactor = globalSum[0] = values.conditionalProdSum(maxSize);
 //   System.out.println("Factor Normalizacion = "+globalSum[0]);
  }
  else {
    System.out.println("Error in PotentialMTree.conditionalLimitBound: illegal value for infoMeasure="+infoMeasure);
    System.exit(1);
  }
  if (!values.isProbab()) {
    if (infoMeasure==1) {
      if(kindOfApproximation!=MultipleTree.AVERAGE_APPROX){
        System.out.println("Error in PotentialMTree.conditionalLimitBound: kindOfApproximation can be only AVERAGE with conditionalPrune1");
        System.exit(1);
      }
      else
        bounded = values.conditionalPrune1(limit,lowLimit,
                       maxSize,globalSum,numberDeleted);
    }
    else if (infoMeasure==2) {
      bounded = values.conditionalPrune2(kindOfApproximation,limit,lowLimit,
                       limitSum,maxSize,globalSum,numberDeleted);
    }
  }
  if (bounded>0){
    size -= numberDeleted[0];
    if(bounded==1)
      setExact(false);
  }
  //System.out.println("Factor Normalizacion = "+globalSum[0]);
}


/**
 * Bounds the tree associated with the potential by removing
 * nodes whose information value is lower than a given threshold.
 * THE TREE IS MODIFIED.
 * @param limit the information limit.
 * @see MultipleTree.prune()
 */

public void limitBound(double limit) {
 
  long maxSize;
  long [] numberDeleted;
  boolean bounded = false;
  double globalSum;
  
  numberDeleted = new long[1]; // Number of deleted nodes.

  maxSize = (long)FiniteStates.getSize(variables);
  
  globalSum = values.sum(maxSize);
  
  // If the tree is not just a probability value:
  if (!values.isProbab())
    bounded = values.prune(limit,maxSize,globalSum,numberDeleted);
 
  size -= numberDeleted[0];
}



/**
 * Gets the configuration of maximum probability consistent with a
 * configuration of variables.
 * @param subconf the subconfiguration to ensure consistency.
 * @return the <code>Configuration</code> of maximum probability
 * included in the <code>Potential</code>, that is consistent with the
 * subConfiguration passed as parameter (this subconfiguration can be empty).
 *
 * NOTE: if there are more than one configuration with maximum
 * probability, the first one is returned.
 */

public Configuration getMaxConfiguration(Configuration subconf) {

  Explanation best;
  Configuration bestFound;
  Vector confValues;
  int i;
  Configuration conf;

  // first we create a configuration with all the values set to -1.
  // -1 indicates that that variable can take every possible state.

  confValues = new Vector();
  for (i=0 ; i<variables.size() ; i++)
    confValues.addElement(new Integer(-1)); 
  bestFound = new Configuration(variables,confValues);

  // an explanation that will contain the best found is initialized with
  // probability equal to -1.0

  best = new Explanation(new Configuration(variables),-1.0);

  best = values.getMaxConfiguration(best,bestFound,subconf);

  // if some value in best.conf is -1, then we have found a set of
  // configurations of maximal probability. In this case we return the
  // first one by changing -1 by 0.

  conf = best.getConf(); 
  for (i=0 ; i<conf.size() ; i++)
    if (conf.getValue(i) == -1)
      conf.putValue(conf.getVariable(i),0);

  return conf;
} 


/**
 * @param subconf the subconfiguration to ensure consistency
 * @param list the list of configurations to be differents  
 * @return the configuration of maximum probability included in the
 * potential, that is consistent with the subConfiguration passed as
 * parameter (this subconfiguration can be empty), and differents to
 * all the configurations passed in the vector
 *
 * NOTE: if there are more than one configuration with maximum
 * probability, the first one is returned
 */

public Configuration getMaxConfiguration(Configuration subconf,
                                         Vector list) {

  Explanation best;
  Configuration bestFound;
  Vector confValues;
  int i;
  Configuration conf;

  // first we create a configuration with all values set to -1.
  // -1 indicates that this variable can take every possible state.

  confValues = new Vector();
  for (i=0 ; i<variables.size() ; i++)
    confValues.addElement(new Integer(-1)); 
  bestFound = new Configuration(variables,confValues);

  // an explanation that will contain the best found is initialized with
  // probability equal to -1.0.

  best = new Explanation(new Configuration( ),-1.0);
  
  best = values.getMaxConfiguration(best,bestFound,subconf,list);

  return best.getConf();
}


/**
 * This method incorporates the evidence passed as argument to the
 * potential, that is, puts to 0 all the values whose configurations
 * are not consistent with the evidence.
 *
 * The method works as follows: for each observed variable a
 * probability tree is built with 1.0 as value for the observed
 * state and 0.0 for the rest. Then the tree is combined with
 * this new tree, and the result is a new tree with the evidence
 * entered.         
 * @param ev a <code>Configuration</code> representing the evidence.
 */

public void instantiateEvidence(Configuration evid) {   
  
  MultipleTree mTree;
  ProbabilityTree tree, twig;
  Configuration conf;
  PotentialMTree pot, pot2;
  FiniteStates variable;
  int i, j, v;

  conf = new Configuration(evid,new NodeList(variables));
  
  if (conf.size() != 0) {
    pot = (PotentialMTree)copy();

    for (i=0 ; i<conf.size() ; i++) {
      variable = conf.getVariable(i);
      v = conf.getValue(i);

      // building a tree for variable
      tree = new ProbabilityTree(variable);
      for (j=0 ; j<tree.child.size() ; j++) {
        twig = (ProbabilityTree) tree.child.elementAt(j);
        twig.label = 2;
        if (j == v)
	  twig.value = 1.0;
        tree.leaves++;
      }                            
      // building the potential for the variable
      mTree = new MultipleTree(tree);
      pot2 = new PotentialMTree();
      pot2.variables.addElement(variable);
      pot2.setTree(mTree);
      // combination
      pot = pot.combine(pot2);
    }
    this.setTree(pot.getTree());
  }
}


/**
 * Saves a tree to a file.
 * @param p the PrintWriter where the tree will be written.
 */   

public void save(PrintWriter p) {

  int i;
  Node n;

  

  for (i=0 ; i<variables.size() ; i++) {

    n = (Node)variables.elementAt(i);
    p.println("node "+n.getName());
  }

  p.print("values= tree ( \n");


  values.save(p,10);

 
  p.print("\n);\n\n");
}


/**
 * Prints a Potential to the standard output.
 */

public void print() {
  
  int i;
  Node n;

 
  for (i=0 ; i<variables.size() ; i++) {

    n = (Node)variables.elementAt(i);
    System.out.println("node "+n.getName());
  }

  System.out.println("Exact Potential: " + getExact());
  System.out.print("values= tree ( \n");

  
  values.print(10);

  
  System.out.print("\n);\n\n");
}


public void showResult() {

}


public void saveResultAsTree(PrintWriter P) {

  int i;
  Node n;

  for (i=0 ; i<variables.size() ; i++) {

    n = (Node)variables.elementAt(i);
    P.println("node "+n.getName());
  }

  P.println("Exact Potential: " + getExact());
  P.print("values= tree ( \n");

  
  values.save(P,2);

  
  P.print("\n);\n\n");
}



public String getClassName() {
  return new String("PotentialMTree");
}

} // End of class
