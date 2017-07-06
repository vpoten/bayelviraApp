/* NodePairTable.java */

package elvira.tools.idiagram.pairtable;

import elvira.NodePairTable;
import elvira.Relation;
import elvira.Node;
import elvira.FiniteStates;
import elvira.NodeList;

/**
 * This class implements a pair of values
 * (<code>FiniteStates,Vector</code>), where the <code>Vector</code> contains
 * <code>Relations</code> where the <code>FiniteStates</code> variable
 * appears. It can be used in propagation algorithms to control
 * the relations in which a variable takes part, for instance, when the
 * variable is going to be deleted.
 *
 * @since 22/9/2000
 */

public class IDNodePairTable extends NodePairTable {

/**
 * Class constructor
 */
public IDNodePairTable(Node var){
   super(var);
}

/**
 * Gets the number of relations
 */
public int getRelationsSize(){
  return relations.size();
}

/**
 * Gets the relation in a given position
 */
public Relation getRelationAt(int position){
   return (Relation)relations.elementAt(position);
}

/**
 * Computes the weight of the links to add during the creation
 * of the potential resulting from the
 * combination of all the relations in the list,
 * considering fully expanded potentials and
 * <code>FiniteStates</code> variables.
 * @return the number of links calculated.
 */
public double weightOfLinksToAdd() {
  Relation r;
  int i, j, k, s, na, nb;
  double weight = 0;
  boolean found;
  Node a, b;
  NodeList v, nl = new NodeList(); 
  
  for (i=0 ; i<relations.size() ; i++) {
    r = (Relation)relations.elementAt(i);
    nl.join(r.getVariables());
  }
  
  // as variable is contained in all the relations, 
  // no link is added containing variable, so we can remove it
  nl.removeNode(variable);
  
  // now we are going to build 
  s = nl.size(); 
 
  // Consider the variables in nl 
  for (i=0 ; i<(s-1) ; i++) {
    a = nl.elementAt(i);
    for (j=i+1 ; j<s ; j++) {
      b = nl.elementAt(j);
      // check that the link between the nodes in positions i and j exists
      found = false;
      for (k=0 ; k<relations.size() ; k++) {
	      r = (Relation)relations.elementAt(k);
	      v = r.getVariables();
	      if ( (v.getId(a)!=-1) && (v.getId(b)!=-1) ){
	        found = true;
	        break;
	      }      
      }
      if (!found){
        na=1;
        nb=1;
        if (a instanceof FiniteStates)
          na=((FiniteStates)a).getNumStates();
        if (b instanceof FiniteStates)
          nb=((FiniteStates)b).getNumStates();
	      weight+=na*nb;
      }
    }
  }    
  return weight;
}

/**
 * Computes the clique size resulting from removing the
 * variable of this node 
 * @return the number of variables calculated
 */
public double cliqueSize() {
  Relation r;
  int i, na;
  Node a;
  NodeList v, nl = new NodeList(); 
  
  for (i=0 ; i<relations.size() ; i++) {
    r = (Relation)relations.elementAt(i);
    nl.join(r.getVariables());
  }
  
  // Return the number of variables in nl 
  return nl.size();
}

/**
 * Computes the clique weight resulting from removing the
 * variable of this node 
 * @return the number of variables calculated
 */
public double cliqueWeight() {
  Relation r;
  int i, na;
  double weight = 1;
  Node a;
  NodeList v, nl = new NodeList(); 
  
  for (i=0 ; i<relations.size() ; i++) {
    r = (Relation)relations.elementAt(i);
    nl.join(r.getVariables());
  }
  
  // Consider the variables in nl 
  for (i=0 ; i < nl.size() ; i++) {
    a = nl.elementAt(i);
    na=1;
    if (a instanceof FiniteStates)
      na=((FiniteStates)a).getNumStates();
	  weight*=na;
  }    
  return weight;
}

/**
 * Method for computing the weight of the relations related
 * to a variable
 */
public double computeRelationsWeight(){
  Relation r;
  double weight=0;
  int i;

  for (i=0 ; i<relations.size() ; i++) {
    r = (Relation)relations.elementAt(i);
    weight+=computeSize(r);
  }

  // Return the computed value
  return weight;
}

/**
 * Method for computing the cost of removing the variable related
 * to this node. Tha criteria to use is passed as argument
 * @param criteria
 * @return cost
 */
public double computeCost(int criteria){
  double cost=0;
  
  switch(criteria){
    case IDPairTable.MINSIZE: 
       cost=totalSize();
       break;
    case IDPairTable.CANOMORAL: 
       if (variable instanceof FiniteStates)
          cost=(double)totalSize()/((FiniteStates)variable).getNumStates();
       else
          cost=totalSize();
       break;
    case IDPairTable.CLIQUESIZE : 
       cost=cliqueSize();
       break;
    case IDPairTable.CLIQUEWEIGHT : 
       cost=cliqueWeight();
       break;
    case IDPairTable.MINFILL: 
       cost=numberOfLinksToAdd();
       break;
    case IDPairTable.WEIGHTFILL: 
       cost=weightOfLinksToAdd();
       break;
    case IDPairTable.WEIGHTFILL_STATES: 
       cost=weightOfLinksToAdd()/((FiniteStates)variable).getNumStates();
       break;
    case IDPairTable.MINFILL_STATES: 
       cost=(double)numberOfLinksToAdd()/((FiniteStates)variable).getNumStates();
       break;
    case IDPairTable.MINFILL_MINSIZE: 
       cost=totalSize()+numberOfLinksToAdd();
       break;
    case IDPairTable.CANOMORAL_MINFILL:
       cost=(double)totalSize()/((FiniteStates)variable).getNumStates()+numberOfLinksToAdd();
       break;
    case IDPairTable.RELATIONSWEIGHT: cost=computeRelationsWeight();
       break;
    case IDPairTable.OTRO: cost=(double)((FiniteStates)variable).getNumStates() / totalSize();
       break;
    default: System.out.println("Wrong criteria: "+criteria);
                            break;
  }

  // Finally, return the cost
  return cost;
}

/**
 * Method for removing a relation passed as argument. This method
 * tests if the variables of the relation passed as argument are
 * the same than the variables of the relation for this node. If
 * this is the case, the relation is removed
 * @param rel relation to remove
 */
public void removeRelation(Relation rel){
  // Get the variables of the relation
  NodeList varsInRel=rel.getVariables();

  // Now consider the relations of the node
  Relation nodeRelation;
  NodeList varsInNodeRel;
  for(int i=0; i < relations.size(); i++){
    varsInNodeRel=((Relation)relations.elementAt(i)).getVariables();

    // Check if both sets of nodes are the same, comparing
    // their names
    if (varsInNodeRel.equalsByName(varsInRel)){
      relations.removeElementAt(i);
    }
  }
}

  /**
   * Method for computing the size of the potential required
   * to store the values of the relation
   */
  public double computeSize(Relation relation){
    Node node;
    NodeList variablesInRelation=new NodeList();
    double size=1;
    int numStates;

    // Considers all the variables
    for(int i=0; i < relation.getVariables().size(); i++){
      node=relation.getVariables().elementAt(i);
      variablesInRelation.insertNode(node);
    }    

    // Return the size
    return variablesInRelation.getSize();
  }

} // End of class.
