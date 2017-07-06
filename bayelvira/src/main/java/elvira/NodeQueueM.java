/* NodeQueueM.java */

package elvira;

import elvira.potential.MultipleTree;

/**
 * Implements nodes of the queue used while sorting
 * and bounding probability trees of class <code>MultipleTree</code>.
 *
 * @since 22/9/2000
 */

public class NodeQueueM {
 
/**
 * Max and min information values.
 */
private static final double MIN_INF = -1E20;
private static final double MAX_INF = 1E20;

  
/**
 * Result tree.
 */
MultipleTree res;           
  
/**
 * Source tree.
 */
MultipleTree source;        
  
/**
 * Variable to be put in <code>res</code>.
 */
FiniteStates var;   
  
/**
 * Value of information of selecting <code>var</code>.
 */
double information; 

/**
 * Factor to update the normalization when this node is expanded.
 */
double updateNormalization; 
  
  
/**
 * Constructs an empty <code>NodeQueueM</code>.
 */

public NodeQueueM() {
  
  information = 0.0;
}
  
  
/**
 * Constructs a <code>NodeQueueM</code> with information as in the argument.
 * @param inf the information value.
 */

public NodeQueueM(double inf) {
  
  information = inf;
}


/**
 * Creates a new <code>NodeQueue</code> with the given trees.
 * Also, computes the information value and
 * obtains the node producing it.
 * @param r the tree to be put as <code>res</code>.
 * @param s the source tree.
 * @param potentialSize size of the potential which correspondig tree is
 * <code>s</code> if it were fully expanded.
 * @param method the method of conditioning.
 * @normalization a normalization factor.
 */

public NodeQueueM(MultipleTree r, MultipleTree s,
		  long potentialSize, int method, double normalization) {
  
  NodeList list;
  FiniteStates y, yMax;
  double max, inf;
  int i, nv;
  double [] updateFactor;
  
  updateFactor = new double[1];
  
  res = r;
  list = s.getVarList();
  
  source = s;
  
  nv = list.size();
  max = MIN_INF;
  
  yMax = (FiniteStates)list.elementAt(0);
  
  for (i=0 ; i<nv ; i++) {
    y = (FiniteStates)list.elementAt(i);
    if (method == 1) {
      inf = s.conditionalInformation(y,potentialSize,normalization,updateFactor);
    }
    else if (method == 2) {
           inf = s.conditionalInformationSimple(y,potentialSize,normalization,updateFactor);}
         else {
           inf = 0.0;
         }
    if (inf > max) {
      max = inf;
      yMax = y;
      updateNormalization = updateFactor[0];
    }
  }
  
  var = yMax;
  information = max;
}


/**
 * Gets the result tree.
 * @return the tree in <code>res</code>.
 */

public MultipleTree getRes() {
  
  return res;
}


/**
 * Gets the source tree.
 * @return the tree in <code>source</code>.
 */

public MultipleTree getSource() {
  
  return source;
}


/**
 * Gets the variable.
 * @return the variable in <code>var</code>.
 */

public FiniteStates getVar() {
  
  return var;
}


/**
 * Gets the factor to update the normalization when this node is expanded.
 * @return the factor.
 */

public double getUpdateNormalization() {
  
  return updateNormalization;
}


/**
 * Compares this Node with the argument, according to the
 * information.
 * @param node a <code>NodeQueueM</code>.
 * @return 0 if both are equal, -1 if the argument is
 * greater and 1 if the argumen is smaller.
 */

public int compares(NodeQueueM node) {
  
  if (information < node.information)
    return -1;
  else
    if (information > node.information)
      return 1;
  return 0;
}



/**
 * Compares this Node with the argument, according to the
 * information.
 * @param node a <code>NodeQueueM</code>.
 * @return <code>true</code> if this node is greater than the argument,
 * and <code>false</code> otherwise.
 */

public boolean greaterThan(NodeQueueM node) {
  
  if (compares(node) > 0)
    return true;
  return false;
}

} // End of class.