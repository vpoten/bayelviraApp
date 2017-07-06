/* PotentialVar.java */

package elvira.potential;
import elvira.*;

/**
 * Class <code>PotentialVar</code>. Implements a holder to a pair 
 * <code>Potential</code>-<code>Node</code>. This class can be used to
 * build a hash table where the key is a <code>PotentialVar</code>
 * (<code>Potential</code>-<code>Node</code>) representing a 
 * <code>Potential</code> that is going to be sum out on
 * the <code>Node</code>.
 *
 * @since 11/10/2000
 */

public class PotentialVar {
  
/**
 * The potential.
 */
private Potential potential;

/**
 * The variable.
 */
private Node var;


/**
 * Constructs an empty pair potential-node.
 */

public PotentialVar() {
  
  potential = null;
  var = null;
}


/**
 * Creates a <code>PotentialVar</code>.
 * @param pot the potential.
 * @param v the node.
 */

public PotentialVar(Potential pot, Node v) {
  
  potential = pot;
  var = v;
}


/**
 * Compares two pairs potential-node.
 * @param potVar a <code>PotentialVar</code>.
 * @return true if <code>potVar</code> is equal to this object,
 * or false otherwise.
 */

public boolean equals(Object potVar) {
  
  if ((potVar!=null) && (potVar instanceof PotentialVar)) {
    if ((potential==((PotentialVar)potVar).potential) && 
        (var==((PotentialVar)potVar).var)) {
      return true;
    }
    else {
      return false;
    }
  }
  else {
    return false;
  }
}


/**
 * Gives the hash code of this object.
 * @return the hash code.
 */

public int hashCode() {
  
  return (potential.hashCode()+var.hashCode());
}

} // End of class