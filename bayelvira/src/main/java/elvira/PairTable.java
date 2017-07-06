/* PairTable.java */

package elvira;

import java.util.Random;
import java.util.Vector;
import elvira.IDiagram;
import elvira.inference.elimination.ids.IDQualitativeVariableElimination;
import elvira.*;
import elvira.potential.PotentialContinuousPT;


/**
 * This class implements a list where each element is a pair
 * (<code>FiniteStates,Vector</code>), where the <code>Vector</code> contains
 * <code>Relations</code> where the <code>FiniteStates</code> variable
 * appears. It can be used in propagation algorithms to control
 * the relations in which a variable takes part, for instance, when the
 * variable is going to be deleted.
 *
 * @since 22/9/2000
 */

public class PairTable {
  
/**
 * The pairs are stored here.
 */
protected Vector info;

/**
 * Creates an empty table.
 */

public PairTable() {
  
  info = new Vector();
}

/**
 * It returns the list of relationships associated to a node 
 * @param var the variable for which we want to know the list of variables
 * @return the list of relationships containing this variables
 */

public Vector getRelations(Node var) {
  NodePairTable node;
  int i, s;
 
  
  s = info.size();
  
  for (i=s-1 ; i>=0 ; i--) {
    node = (NodePairTable)(info.elementAt(i));
    
    if (node.variable == var) {
      return node.relations;
      
    }
 
  }   return null;
}
  

/**
 * Inserts an element at the end of the list.
 * @param node the element to insert.
 */

public void addElement(NodePairTable node) {
  
  info.addElement(node);
}


/**
 * Inserts an element at the end of the list.
 * The element is a <code>Node</code> variable with no relations.
 * It is assumed that the argument is not in the list.
 * @param var a <code>FiniteStates</code> variable.
 */

public void addElement(Node var) {
  
  NodePairTable node;
  
  node = new NodePairTable(var);
  info.addElement(node);
}


/**
 * Gets the size of the list.
 * @return the size of this <code>PairTable</code>.
 */

public int size() {
  
  return info.size();
}


/**
 * Retrieves an element from the list.
 * @param i an index.
 * @return the element at position <code>i</code>.
 */

public NodePairTable elementAt(int i) {
  
  return (NodePairTable)info.elementAt(i);
}


/**
 * Inserts a relation in the position corresponding to
 * the argument variable var, which is assumed to be in the list.
 * @param var the key variable.
 * @param rel the relation to insert.
 */

public void addRelation(FiniteStates var, Relation rel) {
  
  NodePairTable node;
  boolean done = false;
  int i = 0;
  
  while ((!done) && (i<info.size())) {
    node = (NodePairTable)(info.elementAt(i));
    if (node.variable == var) {
      done = true;
      node.relations.addElement(rel);
    }
    i++;
  }
}


/**
 * Inserts a relation at all the positions corresponding
 * to variables in the domain of the relation.
 * @param rel a <code>Relation</code> to insert.
 */

public void addRelation(Relation rel) {
  
  NodePairTable node;
  int i = 0;
  
  for (i=0 ; i<info.size() ; i++) {
    node = (NodePairTable)(info.elementAt(i));
    
    if (rel.getVariables().getId(node.variable) > -1)
      node.relations.addElement(rel);
  }
}


/**
 * Removes a relation from all the positions where it
 * appears.
 * @param rel the relation to remove.
 */

public void removeRelation(Relation rel) {
  
  int i, s;
  NodePairTable node;
  
  s = info.size();
  
  for (i=s-1 ; i>=0 ; i--) {
    node = (NodePairTable)(info.elementAt(i));
    node.relations.removeElement(rel);
  }
}


/**
 * Removes the element which key is the variable given as argument.
 * @param var a <code>Node</code> variable.
 */

public void removeVariable(Node var) {
  
  int i, s;
  NodePairTable node;
  
  s = info.size();
  
  for (i=s-1 ; i>=0 ; i--) {
    node = (NodePairTable)(info.elementAt(i));
    
    if (node.variable == var) {
      info.removeElementAt(i);
      break;
    }
  }
}


/**
 * Returns the next node to remove according to
 * the criterium of minimum size.
 * @return the variable to remove.
 */

public Node nextToRemoveCont() {
  
  NodePairTable node;
  int i;
  double s, min = 90.0E20;
  
  Node var;

  var = ((NodePairTable)info.elementAt(0)).variable;
  
  for (i=0 ; i<info.size() ; i++) {   
    
      node = (NodePairTable)info.elementAt(i);
    
    // If the node is in just one relation, remove it.
    if (node.relations.size() == 1)
      return node.variable;
    
    //System.out.println("Esta es la variable para la que miramos el size ");
    //(node.getVariable()).print();
    s = node.continuousTotalSize();
    
    if (s < min) {
      min = s;
      var = (Node)node.variable;	
    }
  }
  
  return var;
}


/**
 * Returns the next node to remove according to
 * the criterium of minimum size.
 * @return the variable to remove.
 */

public Node nextToRemove() {
  
  NodePairTable node;
  int i;
  double s, min = 90.0E20;
  Node var;
  
  var = ((NodePairTable)info.elementAt(0)).variable;
  
  for (i=0 ; i<info.size() ; i++) {   
    
    node = (NodePairTable)info.elementAt(i);
    
    // If the node is in just one relation, remove it.
    if (node.relations.size() == 1)
      return node.variable;
    
    s = node.totalSize();
    
    if (s < min) {
      min = s;
      var = node.variable;	
    }
  }
  
  return var;
}

/**
 * Returns the next node to remove according to
 * the criterium of minimum size, and taking into account that
 * the deletion sequece obtained is a constrained deletion sequence,
 * where the nodes of the argument list have to be the nodes placed at
 * the end of the sequence.
 * @param set the list of nodes that must be removed only after
 * the rest of nodes have been removed.
 * @return the variable to remove.
 */

public Node nextToRemove(NodeList set) {
  
  NodePairTable node;
  int i;
  double s, min = 90.0E20;
  Node var;
  
  var = ((NodePairTable)info.elementAt(0)).variable;
  
  for (i=0 ; i<info.size() ; i++) {   
    
    node = (NodePairTable)info.elementAt(i);
    
    s = node.totalSize();
    
    if (set.getId(node.getVariable()) == -1){
      // in this way the value s for a node not in
      // set is always lower than for a node in
      // set. The minus sign is to mantain the
      // order among the nodes not in set.
      s = (double) -1/s;
    }
    
    
    if (s < min) {
      min = s;
      var = node.getVariable();	
    }
  }
  
  return var;
}


/**
 * Returns the next node to remove according to
 * the criterium of minimum size. The node must be
 * different of the argument.
 * @param notRemovable a <code>FiniteStates</code> variable that must not be
 * deleted.
 * @return the variable to remove.
 */

public Node nextToRemove(Node notRemovable) {
  
  NodePairTable node;
  int i;
  double s, min = 90.0E20;
  Node var;
  
  var = ((NodePairTable)info.elementAt(0)).variable;
  
  if (var == notRemovable)
    var = new FiniteStates();
  
  for (i=0 ; i<info.size() ; i++) {   
    
    node = (NodePairTable)info.elementAt(i);
    
    if ((node.relations.size()==1) && (node.variable!=notRemovable))
      return node.variable;
    
    s = node.totalSize();
    
    if ((s<min) && (node.variable!=notRemovable)) {
      min = s;
      var = node.variable;	
    }
  }
  
  return var;
}


/**
 * Returns the next node to remove according to
 * the criterium of minimum size, and taking into account that
 * the deletion sequece obtained is a constrained deletion sequence,
 * where the nodes of the argument list have to be the nodes placed
 * at the end of the sequence.
 * @param set the list of nodes that must be removed only after
 * the rest of nodes have been removed.
 * @param criterion indicates the heuristic to be used.
 * @return the next node to remove.
 */

public FiniteStates nextToRemove(NodeList set, String criterion) {
  
  NodeList candidates = new NodeList();   
  
  if ((criterion.equals("minSize")) || 
      (criterion.equals("CanoMoral")) ||
      (criterion.equals("minFill")) ||
      (criterion.equals("minFillExpRest")) ||
      (criterion.equals("minFillNotExp")) ) {
    candidates = getCandidates(set,criterion);
  }
  else
    if (criterion.equals("minSize+minFillExpRest")) {
      candidates = getCandidates(set,"minSize");
      if (candidates.size()>1)
	candidates = getCandidatesOnlyFor(candidates,"minFillExpRest");
    }
    else
      if (criterion.equals("minSize+minFillNotExp")) {
        candidates = getCandidates(set,"minSize");
	if (candidates.size()>1)
	  candidates =  getCandidatesOnlyFor(candidates,"minFillNotExp");
      }
      else
	if (criterion.equals("CanoMoral+minFillExpRest")) {
	  candidates = getCandidates(set,"CanoMoral");
	  if (candidates.size() > 1)
	    candidates =  getCandidatesOnlyFor(candidates,"minFillExpRest");
        }
        else
	  if (criterion.equals("CanoMoral+minFillNotExp")) {
	    candidates = getCandidates(set,"CanoMoral");
	    if (candidates.size() > 1)
	      candidates = getCandidatesOnlyFor(candidates,"minFillNotExp");
	  }
	  else
	    if (criterion.equals("minFill+minFillExpRest")) {
	      candidates = getCandidates(set,"minFill");
	      if (candidates.size() > 1)
		candidates = getCandidatesOnlyFor(candidates,"minFillExpRest");
	    }
	    else
	      if (criterion.equals("minFill+minFillNotExp")) {
	        candidates = getCandidates(set,"minFill");
		if (candidates.size() > 1)
		  candidates = getCandidatesOnlyFor(candidates,"minFillNotExp");
	      }
	      else
		if (criterion.equals("minFillExpRest+minSize")) {
		  candidates = getCandidates(set,"minFillExpRest");
		  if (candidates.size() > 1)
		    candidates = getCandidatesOnlyFor(candidates,"minSize");
	        }
	        else
		  if (criterion.equals("minFillExpRest+CanoMoral")) {
		    candidates = getCandidates(set,"minFillExpRest");
		    if (candidates.size() > 1)
		      candidates = getCandidatesOnlyFor(candidates,"CanoMoral");
		  }
		  else
		    if (criterion.equals("minFillExpRest+minFill")) {
		      candidates = getCandidates(set,"minFillExpRest");
		      if (candidates.size() > 1)
			candidates = getCandidatesOnlyFor(candidates,"minFill");
		    }
		    else
		      if (criterion.equals("minFillExpRest+minFillNotExp")) {
		        candidates = getCandidates(set,"minFillExpRest");
			if (candidates.size() > 1)
			  candidates = getCandidatesOnlyFor(candidates,"minFillNotExp");
		      }
		      else
			if (criterion.equals("minFillNotExp+minSize")) {
			  candidates = getCandidates(set,"minFillNotExp");
			  if (candidates.size() > 1)
			    candidates = getCandidatesOnlyFor(candidates,"minSize");
		        }
		        else
			  if (criterion.equals("minFillNotExp+CanoMoral")) {
			    candidates = getCandidates(set,"minFillNotExp");
			    if (candidates.size() > 1)
			      candidates = getCandidatesOnlyFor(candidates,"CanoMoral");
			  }
			  else
			    if (criterion.equals("minFillNotExp+minFill")) {
			      candidates = getCandidates(set,"minFillNotExp");
			      if (candidates.size() > 1)
				candidates = getCandidatesOnlyFor(candidates,"minFill");
			    }
			    else
			      if (criterion.equals("minFillNotExp+minFillExpRest")) {
			        candidates = getCandidates(set,"minFillNotExp");
				if (candidates.size() > 1)
				  candidates = getCandidatesOnlyFor(candidates,"minFillExpRest");
			      }
			      else {
				System.out.println("The " + criterion + " triangulation heuristic is not implemented in Elvira");
				System.exit(0); 
			      }
			      
  if (candidates.size() == 1) {
    return ((FiniteStates)candidates.elementAt(0));
  }
  else { 
    return (FiniteStates)candidates.elementAt(0); // arbitrary decision
  }
}


/**
 * Chooses (randomly) a node from the argument list.
 * @param candidates the list from which the node is selected.
 * @return the randomly selected node.
 */

public FiniteStates breakTie(NodeList candidates) {
  
  Random generator = new Random(); 
  int pos;
  
  pos = java.lang.Math.abs(generator.nextInt()) % candidates.size();    
  return (FiniteStates)candidates.elementAt(pos);
}


/**
 * Returns the next node to remove according to some criterion.
 * @param criterion indicates the heuristic to be used.
 * @return the next node to remove.
 */

public FiniteStates nextToRemove(String criterion) {
  
  return nextToRemove(new NodeList(),criterion);
}


/**
 * Returns a list of nodes containing the nodes which mimimize a given
 * criterion, and taking into account that
 * the deletion sequece obtained is a constrained deletion sequence,
 * where the nodes in the argument list have to be the nodes placed at the
 * end of the sequence.
 * @param set the list of nodes that must be removed only after
 * the rest of nodes have been removed.
 * @param criterion the deletion criterion.
 * @return the next node to remove
 */

public NodeList getCandidates(NodeList set, String criterion) {
  
  NodeList nl = new NodeList();
  NodePairTable node;
  int i;
  double s, min = 90.0E20;
  
  for (i=0 ; i<info.size() ; i++) {   
    
    node = (NodePairTable)info.elementAt(i);
    
    if (criterion.equals("minSize")) {
      s = node.totalSize();
    }
    else
      if (criterion.equals("CanoMoral")) {
        Node var;
	var=node.getVariable();
	if(var instanceof FiniteStates){
	  s = (double) node.totalSize()/((FiniteStates)var).getNumStates();
	}
	else{
	  s = (double) node.totalSize();
	}
      }
      else
	if (criterion.equals("minFill")) {
          s = node.numberOfLinksToAdd();
        }
        else
	  if (criterion.equals("minFillExpRest")) {
	    s = node.numberOfLinksToAddBetweenExplanationAndRest(set);
	  }
	  else
	    if (criterion.equals("minFillNotExp")) {
	      s = node.numberOfLinksToAddExceptAmongExplanationNodes(set);
	    }
	    else {
	      System.out.println("PairTable: the heuristic " + criterion + 
				 " is not implemented for triangulation");
	      s = 1; // this assignation does not have any effect
	      System.exit(0);
	    }
	   
    if (set.getId(node.getVariable()) == -1) { 
      // in this way the value s for a node not in
      // set is always lower that for a node in
      // set. The minus sign is to mantain the
      // order among the nodes not in set.
      s = (double) -1/s;
    }				
    
    if (s == min) {
      nl.insertNode(node.getVariable());
    }
    else
      if (s < min) {
        min = s;
        nl = new NodeList();
	nl.insertNode(node.getVariable());	
      }
  }
  
  return nl;
}


/**
 * Returns a node list containing the nodes which mimimizes criterion. 
 * @param criterion the criterion.
 * @return the list of nodes obtained.
 */

public NodeList getCandidates(String criterion) {
  
  return getCandidates(new NodeList(),criterion);
}


/**
 * Returns a node list containing the nodes of set which mimimizes criterion.
 * @param set the list of nodes that must be removed only after
 * the rest of nodes have been removed
 * @param criterion the criterion.
 * @return the list of nodes obtained.
 */

public NodeList getCandidatesOnlyFor(NodeList set, String criterion) {
  
  NodeList nl = new NodeList();
  NodePairTable node;
  int i;
  double s, min = 90.0E20;
  
  for (i=0 ; i<info.size() ; i++) {   
    
    node = (NodePairTable)info.elementAt(i);
    
    if (set.getId(node.getVariable()) != -1) { 
      
      if (criterion.equals("minSize")) {
	s = node.totalSize();
      }
      else
	if (criterion.equals("CanoMoral")) {
	  Node var;
	  var=node.getVariable();
	  if(var instanceof FiniteStates){
	    s = (double) node.totalSize()/((FiniteStates)var).getNumStates();
	  }
	  else{
	    s = (double) node.totalSize();
	  }
        }
        else
	  if (criterion.equals("minFill")) {
	    s = node.numberOfLinksToAdd();
          }
	  else
	    if (criterion.equals("minFillExpRest")) {
              s = node.numberOfLinksToAddBetweenExplanationAndRest(set);
	    }
	    else
	      if (criterion.equals("minFillNotExp")) {
	        s = node.numberOfLinksToAddExceptAmongExplanationNodes(set);
	      }
	      else {
		System.out.println("PairTable: the heuristic " + criterion + 
				   " is not implemented for triangulation");
		s = 1; // this assignation does not have any effect
		System.exit(0);
	      }

        if (s == min) {
          nl.insertNode(node.getVariable());
        }
        else
	  if (s < min) {
	    min = s;
	    nl = new NodeList();
	    nl.insertNode(node.getVariable());	
          }
    }
    
  }
  
  return nl;
}

/**
 * To test if a variable is already in the PairTable 
 * @param var to look for <code>FiniteStates</code> 
 * @return true or false 
 */

public boolean varAlreadyIn(FiniteStates var) {

  int i;

  for(i=0; i < size(); i++)
  {
    if (elementAt(i).getVariable().getName() == var.getName())
       return(true);
  }
  
  // If here, the var is not in the PairTable

  return(false); 
}

}  // End of class.
