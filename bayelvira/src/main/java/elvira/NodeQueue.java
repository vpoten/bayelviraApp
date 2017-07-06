/* NodeQueue.java */

package elvira;

import elvira.potential.ProbabilityTree;

/**
 * Implements nodes of the queue used while sorting
 * and bounding probability trees.
 *
 * @since 22/9/2000
 */

public class NodeQueue {

/**
 * Max and min information values.
 */
private static final double MIN_INF = -1E20;
private static final double MAX_INF = 1E20;

  
/**
 * Result tree.
 */
ProbabilityTree res;           

/**
 * Source tree.
 */
ProbabilityTree source;        

/**
 * Variable to be put in <code>res</code>.
 */
FiniteStates var;   

/**
 * Value of information of selecting <code>var</code>.
 */
double information; 


/**
 * Constructs an empty <code>NodeQueue</code>.
 */

public NodeQueue() {
  
  information = 0.0;
}


/**
 * Constructs a <code>NodeQueue</code> with information as in the argument.
 * @param inf the information value.
 */

public NodeQueue(double inf) {
  
  information = inf;
}


/**
 * Creates a new <code>NodeQueue</code> with the given trees.
 * Also, computes the information value and
 * obtains the node producing it.t
 * @param r the tree to be put as <code>res</code>.
 * @param s the source tree.
 * @param potentialSize size of the potential which correspondig tree is
 * <code>s</code> if it were fully expanded.
 */

public NodeQueue(ProbabilityTree r, ProbabilityTree s, long potentialSize) {
  
  NodeList list;
  FiniteStates y, yMax;
  double max, inf;
  int i, nv;
  
  res = r;
  list = s.getVarList();
  
  source = s;
  
  nv = list.size();
  max = MIN_INF;
  
  yMax = (FiniteStates)list.elementAt(0);
  
  for (i=0 ; i<nv ; i++) {
    y = (FiniteStates)list.elementAt(i);
    inf = s.information(y,potentialSize);
    if (inf > max) {
      max = inf;
      yMax = y;
    }
  }
  
  var = yMax;
  information = max;
}

/**
 * Creates a new <code>NodeQueue</code> with the given trees.
 * Also, computes the information value and
 * obtains the node producing it.
 * If the last flag is true means we work with utilities instead of
 * probabilities
 * @param r the tree to be put as <code>res</code>.
 * @param s the source tree.
 * @param potentialSize size of the potential which correspondig tree is
 * <code>s</code> if it were fully expanded.
 * @param <boolean>flag</code> to say if the potential is a utility or not
 * True = utility, false = any other value
 * if flag == false this method is equivalent to the previous
 * constructor
 */

public NodeQueue(ProbabilityTree r, ProbabilityTree s, 
                 long potentialSize, boolean flag) {
  
  NodeList list;
  FiniteStates y, yMin;
  double min, inf;
  int i, nv;
  
  res = r;
  list = s.getVarList();
  
  source = s;
  
  nv = list.size();
  min = MAX_INF;
  
  yMin = (FiniteStates)list.elementAt(0);
  
  for (i=0 ; i<nv ; i++) {
    y = (FiniteStates)list.elementAt(i);
    if (flag == false)
       inf = s.information(y,potentialSize);
     else
       inf = s.informationUtility(y,potentialSize);

    if (inf < min) {
      min = inf;
      yMin = y;
    }
  }
  
  var = yMin;
  information = min;
}


/**
 * Gets the result tree.
 * @return the tree in <code>res</code>.
 */

public ProbabilityTree getRes() {
  
  return res;
}


/**
 * Gets the source tree.
 * @return the tree in <code>source</code>.
 */

public ProbabilityTree getSource() {
  
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
 * Compares this Node with the argument, according to the
 * information.
 * @param node a <code>NodeQueue</code>.
 * @return 0 if both are equal, -1 if the argument is
 * greater and 1 if the argumen is smaller.
 */

public int compares(NodeQueue node) {
  
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
 * @param node a <code>NodeQueue</code>.
 * @return <code>true</code> if this node is greater than the argument,
 * and <code>false</code> otherwise.
 */

public boolean greaterThan(NodeQueue node) {
  
  if (compares(node) > 0)
    return true;
  return false;
}

} // End of class
