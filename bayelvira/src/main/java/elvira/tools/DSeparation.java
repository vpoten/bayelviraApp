/* DSeparation.java */

package elvira.tools;

import java.util.*;
import elvira.*;

/**
 * This class can be used to obtain the variables d-connected to a given
 * variable. Also it can be used to obtain a list of all variables whose
 * distributions can affect the marginal posterior of a given variable.
 * @see DSeparation#allConnected
 * @see DSeparation#allAffecting
 * @since 11/10/2000
 */

public class DSeparation {

/**
 * Contains the <code>Bnet</code> used for checking the d-separation.
 */
Bnet bn;

/**
 * Contains the evidence of <code>Bnet bn</code>.
 */
Evidence evidence;


boolean above[], below[];

private final static int CONNECTED_VARIABLES = 0;
private final static int AFFECTING_VARIABLES = 1;


/**
 * Constructor for <code>DSeparation</code> object.
 * @param bnet the network.
 * @param evid the evidence.
 */

public DSeparation(Bnet bnet, Evidence evid) {

  bn = bnet;
  evidence = evid;
}


/**
 * Return a list of all variables that are d-connected to
 * a given variable.
 * @param x the index of a variable.
 * @return the list of variables d-connected to <code>x</code>.
 */

public Vector allConnected(int x) {

  return (separation(x, CONNECTED_VARIABLES));
}


/**
 * Returns a list of all variables whose distributions can
 * affect the marginal posterior of a given variable.
 * @param n a variable.
 * @return a list of all variables whose distributions can
 * affect the marginal posterior of variable <code>n</code>.
 */

public Vector allAffecting(Node n) {

  int x;

  x = bn.getNodeList().getId(n);
  return (separation(x, AFFECTING_VARIABLES));
}


/**
 * Returns a list of all variables whose distributions can
 * affect the marginal posterior of a given variable.
 * @param x is the index of a variable.
 * @return a list of all variables whose distributions can
 * affect the marginal posterior of variable with index <code>x</code>.
 */

public Vector allAffecting(int x) {

  return (separation(x, AFFECTING_VARIABLES));
}


/**
 * Finds all d-separation relations.
 */

private void separationRelations(int x, int flag) {

  int nvertices = bn.getNodeList().size();
  if (flag == AFFECTING_VARIABLES)
    nvertices += nvertices;

  boolean ans = false;

  above = new boolean[nvertices];
  below = new boolean[nvertices];

  int current[] = new int[2];

  int i, j, v, subscript;

  for (i=0 ; i < nvertices ; i++) {
    above[i] = false;
    below[i] = false;
  }

  Stack stack = new Stack( );

  int Xabove[] = { x, 1 };
  int Xbelow[] = { x, -1 };

  stack.push(Xabove);
  stack.push(Xbelow);

  below[x] = true;
  above[x] = true;

  while ( !stack.empty( ) ) {
    current = (int[]) stack.pop( );
    v = current[0];
    subscript = current[1];

    if (subscript < 0) {

      for (i=0 ; i < nvertices ; i++) {//parents
	if ( adj(i, v, flag) )
	  if ( (!below[i]) && ( !isSeparator(i, flag) )) {
	    below[i] = true;
	    int Vbelow[] = { i, -1 };
	    stack.push(Vbelow);
	  }
      }

      for ( j = 0; j < nvertices; j++) //children
	if ( adj(v, j, flag) )
	  if (!above[j]) {
	    above[j] = true;
	    int Tabove[] = { j, 1 };
	    stack.push(Tabove);
          }

        above[v] = true;
    }  // subscript < 0

    else {
      if ( isSeparator(v, flag) ) {  // v known

	for ( i = 0; i < nvertices; i++ ) //parents
	  if ( adj(i, v, flag) )
	    if ( ( !isSeparator(i, flag) ) && !below[i] ) {
	      below[i] = true;
	      int Tbelow[] = { i, -1 };
	      stack.push(Tbelow);
	    }
      }
      else                      // v not known
	for ( j = 0; j < nvertices; j++ ) //children
	  if ( adj(v, j, flag) )
	    if(!above[j]) {
	      above[j] = true;
	      int Sabove[] = { j, 1 };
	      stack.push(Sabove);
            }
    } // subscript >= 0
  }  // while

}


/**
 * Runs the separation algorithm and processes its results
 *
 * @param x Index of the variable to separate.
 * @param flag
 * @return
 */

private Vector separation(int x, int flag) {

  int i;
  int nVertices = bn.getNodeList().size();
  Vector dSeparatedVariables = new Vector();

  // Run algorithm
  separationRelations(x, flag);

  // Process results
  if (flag == CONNECTED_VARIABLES) {
    for (i=0 ; i<nVertices ; i++) {
      if (below[i] || above[i])
	dSeparatedVariables.addElement(bn.getNodeList().elementAt(i));
    }
  }
  else {
    for (i=nVertices ; i<(nVertices+nVertices) ; i++) {
      //if (below[i] || above[i])  
      if (above[i])
	dSeparatedVariables.addElement(bn.getNodeList().elementAt(i - nVertices));
    }

  }

  return (dSeparatedVariables);
}


/**
 * Check whether the variable given by the index is in the
 * list of separators (i.e., it is observed)
 *
 * @param i Index of the variable to check
 * @param flag
 * see Evidence#isObserved
 */

private boolean isSeparator(int i, int flag) {

  if ((flag == CONNECTED_VARIABLES) ||
      ((flag == AFFECTING_VARIABLES) && (i < bn.getNodeList().size())))
    return (evidence.isObserved(bn.getNodeList().elementAt(i)));
  else
    return(false);
}


/**
 * Check whether there is a link from variable indexFrom to
 * variable indexTo.
 *
 * @param indexFrom Index of the first variable
 * @param indexTo Index of the second variable
 * @param flag
*/

private boolean adj(int indexFrom, int indexTo, int flag) {

  Relation pf = null;

  if ((flag == CONNECTED_VARIABLES) ||
      ((flag == AFFECTING_VARIABLES) && (indexTo < bn.getNodeList().size()) &&
       (indexFrom < bn.getNodeList().size()))) {

    return (bn.getLinkList().parent(bn.getNodeList().elementAt(indexFrom),
				    bn.getNodeList().elementAt(indexTo)));
  }
  else {
    if ( ( indexFrom - indexTo ) == bn.getNodeList().size() )
      return (true);
    else
      return(false);
  }
}

}  // End of class