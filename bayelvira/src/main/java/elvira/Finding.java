package elvira;


/**
 * Class <code>Finding</code>. Implements a finding, consisting of a
 * <code>FiniteStates</code> node and a value for that node.
 * @since 18/9/2000
 */

public class Finding {

/**
 * The node to which the finding is concerned.
 */
FiniteStates node=null;

/**
 * The value in the finding for node <code>node</code>.
 */
int state;

/**
 * The node to which the finding is concerned.
 */
Continuous nodeC=null;

/**
 * The value in the finding for nodeC <code>node</code>.
 */
double value;


/*
 * This show if Finding is for a continuous or discrete node
 */
boolean  isContinuous=false;

/**
 * Construct an empty finding.
 */

public Finding() {
  
  node = null;
  state = -1;
}


/**
 * Constructs a finding given a node and a value.
 * @param n the node in the finding.
 * @param s the value of <code>n</code>.
 */

public Finding(FiniteStates n, int s) {
  
  node = n;
  state = s;
  isContinuous=false;
}

/**
 * Constructs a finding given a node and a value.
 * @param n the continuous node in the finding.
 * @param s the value of <code>n</code>.
 */

public Finding(Continuous n, double s) {
  
  nodeC = n;
  value = s;
  isContinuous=true;
}


/**
 * Sets the node in the finding.
 * @param n the node.
 */

public void setNode(FiniteStates n) {
  
  node = n;

  isContinuous=false;
}

/**
 * Sets the node in the finding.
 * @param n the node.
 */

public void setNode(Continuous n) {
  
  nodeC = n;
  isContinuous=true;
}

/**
 * Sets the value of the node in the finding.
 * @param s the value.
 */

public void setStateNode(int s) {
  
  state = s;
}

/**
 * Sets the value of the node in the finding.
 * @param s the value.
 */

public void setValueNode(double s) {
  
  value = s;
}

/**
 * Gets the node in the finding.
 * @return the node.
 */

public Node getNode() {
  
  if (isContinuous)
      return nodeC;
  else
      return node;
}


/**
 * Gets the value of the node in the finding.
 * @return the value of the node.
 */

public int getStateNode() {
  
  if (isContinuous)
      return -1;
  else
      return state;
}

/**
 * Gets the value of the node in the finding.
 * @return the value of the node.
 */

public double getValueNode() {
  
  if (isContinuous)
      return value;
  else
      return 0;
}

} // End of class
