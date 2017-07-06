/* RelationList.java */

package elvira;

import java.util.Vector;
import java.util.Enumeration;
import elvira.Relation;
import elvira.potential.Potential;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialTable;
import elvira.potential.ProbabilityTree;
import elvira.potential.LogicalExpression;
import elvira.potential.PotentialFunction;
import elvira.potential.CanonicalPotential;
import elvira.potential.UtilityPotential;


/**
 * Class <code>RelationList</code>. Implements a list of relations.
 *
 * @since 22/9/2000
 */

public class RelationList {

/**
 * Contains the list of relations.
 */
private Vector relations;


/**
 * Creates an empty <code>RelationList</code>.
 */

public RelationList() {

  relations = new Vector();
}


/**
 * Gets the relations in the list.
 * @return a vector containing the relations.
 */

public Vector getRelations() {

  return relations;
}


/**
 * Sets the relations in the list from a vector.
 * @param v a vector containing relations.
 */

public void setRelations(Vector v) {

  relations = v;
}


/**
 * Inserts a relation at the end of the list.
 * @param rel the relation to insert.
 */

public void insertRelation(Relation rel) {

  relations.addElement(rel);
}

/**
 * Removes the Relation at position i from this RelationList
 * @param i the position of the Relation to be removed
 */
public void removeElementAt(int i){
  relations.removeElementAt(i);
}


/**
 * Inserts a relation at a given position.
 * @param rel the relation to insert.
 * @param pos the position where the relation will be inserted.
 */

public void setElementAt(Relation rel, int pos) {

  relations.setElementAt(rel,pos);
}


/**
 * Removes a relation from the list.
 * @param rel the relation to be removed.
 */

public void removeRelation(Relation rel) {

  relations.removeElement(rel);
}


/**
 * Removes a relation in a given position.
 * @param p the position of the relation to remove.
 */

public void removeRelationAt(int p) {

  relations.removeElementAt(p);
}


/**
 * Locates a relation in the list.
 * @param rel a relation.
 * @return the position of <code>rel</code> in the list.
 * -1 if <code>rel</code> is not in it.
 */

public int indexOf(Relation rel) {

  return relations.indexOf(rel);
}


/**
 * Gets the size of the list.
 * @return the number of relations in the list.
 */

public int size() {

  return relations.size();
}


/**
 * Retrieves a relation from the list.
 * @param p a position in the list.
 * @return the relation at position <code>p</code>.
 */

public Relation elementAt(int p) {

  return ((Relation)relations.elementAt(p));
}


/**
 * Enumerates the elements in the list.
 * @return an enumeration of the relations in the list.
 */

public Enumeration elements() {

  return ((Enumeration)relations.elements());
}


/**
 * Computes the size of the potential resulting from the
 * combination of all the relations in the list,
 * considering fully expanded potentials and
 * <code>FiniteStates</code> variables.
 * @return the size computed.
 */

public double totalSize() {

  NodeList list;
  int i;

  list = (NodeList) elementAt(0).getVariables().copy();
  for (i=1 ; i<size() ; i++)
    list.merge(elementAt(i).getVariables());

  return (FiniteStates.getSize(list));
}


/**
 * Computes the size of the potential resulting from the
 * combination of all the relations in the list
 * that contain the argument variable,
 * considering fully expanded potentials and
 * <code>FiniteStates</code> variables.
 * @param var a FiniteStates variable.
 * @return the size computed.
 */

public double totalSize(FiniteStates var) {

  RelationList list;

  list = getRelationsOf(var);

  return list.totalSize();
}

/**
 * Method to compute the total size needed to store the values
 * used to quantify the whole set of relations stored in a list
 * of Relations
 * @return <code>double</code> the sum of sizes of the potentials
 */
public double sumSizes(){
  Relation rel;
  Potential val;
  PotentialTree valTree;
  int i;
  double size=0;

  // Loop over the set of relations
  for(i=0; i < relations.size(); i++){
	  rel=(Relation)relations.elementAt(i);

	  // Check if it is a constraint relation
	  if (rel.getKind() != Relation.CONSTRAINT){
	    val=rel.getValues();
    }
    else{
      val=((LogicalExpression)rel.getValues()).getResult();
    }

    // If values is a PotentialTree, update its size
    // Anyway, compute its size
 
    if (val != null){
      if (val.getClassName().equals("PotentialTree")){
		    valTree=(PotentialTree)val;
		    if(valTree.checkSize() == false)
			    valTree.updateSize();
		    size+=valTree.getSize();
      }
	    else{
         size+=val.getSize();
	    }
    }
  }

  // Return the final size
  return(size);
}


/**
 * Gets the relations that contain a given variable.
 * @param var a <code>Node</code> variable.
 * @return a new <code>RelationList</code> containing only the relations
 * in this list that contain the argument variable.
 */

public RelationList getRelationsOf(Node var) {

  int i, p;
  Relation rel;
  RelationList list;

  list = new RelationList();

  for (i=0 ; i<size() ; i++) {
    rel = elementAt(i);
    p = rel.getVariables().getId(var);
    if (p != -1)
      list.insertRelation(rel);
  }

  return list;
}

/**
 * Method to get the relations with non empty intersection
 * given a set of variables
 * @param vars Vector of variables to look for
 * @return a new RelationList with the given set of relations
 */

public RelationList getRelationsOf(Vector vars){
  int i, j, p;
  Relation rel;
  RelationList list;
  Node var;

  // Prepare the list containing the relations defined
  // on a set of variables with non empty intersection
  // with the set of vars passed as argument
  
  list = new RelationList();

  // Consider the set of relations containes in this
  // RelationList
  
  for (i=0 ; i < size() ; i++) {

    // Consider every relation, one by one
    
    rel = elementAt(i);

    // See if in this relation there is at least one
    // of the variables passed as argument

    for(j=0; j < vars.size(); j++){

      // Consider one variable

      var=(Node)vars.elementAt(j);

      // Look if this variable is present at rel
      
      p = rel.getVariables().getId(var);
      
      if (p != -1){
        list.insertRelation(rel);

        // It is not needed to go on with this relation

        break;
      }
    }
  }

  return list;
}


/**
 * Method to get the relations whose first variable is in a given set of variables
 * @param vars Vector of variables to look for
 * @return a new RelationList with the given set of relations
 */

public RelationList getRelationsFirstVariableOf(NodeList vars){
  int i, j, p;
  Relation rel;
  RelationList list;
  Node var;
  Node firstVariable;

  // Prepare the list containing the relations whose first variable is in the set of vars passed as argument
  
  list = new RelationList();

  // Consider the set of relations contains in this
  // RelationList
  
  for (i=0 ; i < size() ; i++) {

    // Consider every relation, one by one
    
    rel = elementAt(i);

    // See if the first variable in this relation there is in the list
    // of the variables passed as argument
    
    firstVariable = rel.getVariables().elementAt(0);
    
    if (vars.getId(firstVariable)!=-1){
    	list.insertRelation(rel);
    }

  }
  
  return list;
}


/**
 * Method to get the relations with non empty intersection
 * given a configuration. For this configuration we will
 * consider its variables, and look if there is non-empty
 * intersection between the variables of the configuration
 * with a known value (not -1) and the variables of the
 * relation
 * @param conf Configuration to consider
 * @return a new RelationList with the given set of relations
 */

public RelationList getRelationsOf(Configuration conf){
  int i;
  Vector allVars;
  Vector selectedVars=new Vector();
  int value;
  String varName;
  RelationList list;

  // Consider one by one the variables in conf and select
  // them related to non -1 values

  allVars=conf.getVariables();
  for(i=0; i < allVars.size(); i++){
    varName=((Node)allVars.elementAt(i)).getName();

    // Get the value for the variable

    value=conf.getValue(varName);

    if (value != -1){
      selectedVars.addElement(allVars.elementAt(i));
    }
  }
 
  // Return the list of relations with non empty intersection
  // with the variables in selectedVars
  
  if (selectedVars.size() != 0) 
    list=getRelationsOf(selectedVars);
   else
    list=new RelationList();

   // Return the list

   return list;
}


/**
 * Obtain the relations that contain a given variable and
 * remove them from the list.
 * @param var a <code>Node</code> variable.
 * @return a new <code>RelationList</code> containing only the relations
 * in this list that contain the argument variable.
 */

public RelationList getRelationsOfAndRemove(Node var) {

  int i, p;
  Relation rel;
  RelationList list;

  list = new RelationList();

  for (i=size()-1 ; i>=0 ; i--) {
    rel = elementAt(i);
    // Check if it is a CONSTRAINT relation

    if (rel.getKind() != Relation.CONSTRAINT){
      p = rel.getVariables().getId(var);
      if (p != -1) {
        list.insertRelation(rel);
        removeRelationAt(i);
      }
    }
  }

  return list;
}

/**
 * Obtain the relations that contain a given variable and
 * remove them from the list.
 * @param var a <code>Node</code> variable
 * @flag to show the kind of relations to get: utility or another. This
 * method will not return constraint relations
 * @return a new <code>RelationList</code> containing only the relations
 * in this list that contain the argument variable.
 */

public RelationList getRelationsOfAndRemove(Node var, int kind) {

  int i, p;
  Relation rel;
  RelationList list;

  list = new RelationList();

  for (i=size()-1 ; i>=0 ; i--) {
    rel = elementAt(i);
    // Check if it is a CONSTRAINT relation

    if (rel.getKind() != Relation.CONSTRAINT){

      // If kind is UTILITY, add the relation if the relations is
      // an utility relation

      if((kind == Relation.UTILITY && rel.getKind() == kind) || 
         (kind != Relation.UTILITY && rel.getKind() != Relation.UTILITY)){
         p = rel.getVariables().getId(var);
         if (p != -1) {
           list.insertRelation(rel);
           removeRelationAt(i);
         }
      }
    }
  }

  return list;
}


/**
 * Determines whether a variable is contained in only one relation
 * in the list.
 * @param var a <code>FiniteStates<code> variable to search for.
 * @return 1 if <code>var</code> is in one relation,
 * 0 if <code>var</code> is not contained in any relation and
 * -1 if <code>var</code> is contained in more than one relation.
 */

public int isInOnlyOne(FiniteStates var) {

  int i, p = -2, nr = 0;
  Relation rel;

  for (i=0 ; i<size() ; i++) {
    rel = elementAt(i);
    p = rel.getVariables().getId(var);
    if (p != -1)
      nr++;
  }

  if (nr > 1)
    return (-1);

  return nr;
}


/**
 * Check that a variable is in the list of relations.
 * @param var a <code>FiniteStates</code> variables.
 * @return <code>true</code> if <code>var</code> is in at least one of the
 * relations.
 */

public boolean isIn(FiniteStates var) {

  int i, p = -2;
  Relation rel;

  for (i=0 ; i<size() ; i++) {
    rel = elementAt(i);
    p = rel.getVariables().getId(var);
    if (p != -1)
      return true;
  }

  return false;
}


/**
 * Determines the next node to remove according to the minimum size
 * criterion.
 * @param list a list of candidate nodes to be removed.
 * @return the position in the argument list of the
 * node producing the minimum size when being removed.
 */

public int nextToRemove(NodeList list) {

  int i, pos;
  double s, min = 90.0E20;
  FiniteStates var;

  pos = list.size()-1;

  for (i=0 ; i<list.size() ; i++) {

    var = (FiniteStates)list.elementAt(i);

    if (isInOnlyOne(var) == 1)
      return i;

    s = totalSize(var);

    if (s < min) {
      min = s;
      pos = i;
    }
  }

  return pos;
}


/**
 * Copies a <code>RelationList</code>.
 * @return a copy of this <code>RelationList</code>.
 */

public RelationList copy() {

  RelationList list;
  Relation rel;
  int i;

  list = new RelationList();

  for (i=0 ; i<relations.size() ; i++) {
    rel = (Relation)relations.elementAt(i);
    list.insertRelation(rel.copy());
  }

  return list;
}


/**
 * Used to know if the argument relation is contained in this list.
 * @param r a relation.
 * @return <code>true</code> if <code>r</code> is in the list,
 * <code>false</code> otherwise.
 */

public boolean contains (Relation r) {

  int i = 0;
  boolean find = false;

  while ((i<this.size()) && (!find)) {
    if (r.isTheSame(this.elementAt(i)))
      find = true;
    else
      i++;
  }

  return find;
}


/**
 * Prints the list to the standard output.
 */

public void print() {

  int i;

  for (i=0 ; i<size() ; i++) {
    elementAt(i).print();
  }
}

/**
 * Method to print the domains of the relations and their sizes 
 */
public void printDomainsAndSizes(){
  Relation rel;
  Potential val;
  PotentialTree valTree;
  int i;
  double size=0;

  // Loop over the set of relations
  for(i=0; i < relations.size(); i++){
     rel=(Relation)relations.elementAt(i);
     
     // Prints the domain
     rel.printDomain();

     // Check if it is a constraint relation
      if (rel.getKind() != Relation.CONSTRAINT){
         val=rel.getValues();
      }
      else{
         val=((LogicalExpression)rel.getValues()).getResult();
      }

    // If values is a PotentialTree, update its size
    // Anyway, compute its size
    if (val != null){
      if (val.getClassName().equals("PotentialTree")){
         valTree=(PotentialTree)val;
         if(valTree.checkSize() == false){
	    valTree.updateSize();
         }
         size+=valTree.getSize();
      }
      else{
         size+=val.getSize();
      }
    }
  }

  // Shows the global size
  System.out.println("Global size: "+size);
}

/**
 * Restricts the relations in the list to the given configuration.
 * @param conf the <code>Configuration</code> to restrict.
 * @return a <code>RelationList</code> with the restricted relations.
 */

public RelationList restrict(Configuration conf) {

  RelationList newList;
  Relation rel;
  int i;

  newList = new RelationList();

  for (i=0 ; i<size() ; i++) {

    rel = elementAt(i);
    newList.insertRelation(rel.restrict(conf));
  }

  return newList;
}


/**
 * Restricts the relations in the list to the given configuration, except
 * a given goal variable.
 * @param conf the <code>Configuration</code> to restrict.
 * @param goalVar the variable that will not be restricted.
 * @return a <code>RelationList</code> with the restricted relations.
 */

public RelationList restrict(Configuration conf, Node goalVar) {

  RelationList newList;
  Relation rel;
  int i;

  newList = new RelationList();

  for (i=0 ; i<size() ; i++) {

    rel = elementAt(i);
    newList.insertRelation(rel.restrict(conf,goalVar));
  }

  return newList;
}


/**
 * Restrict every relation to the set of evidences. Each observed
 * variable will be removed from the list of variables of the potential
 * and from the list of variables of the relation. The new potential will
 * contain only the values that are compatible with the evidence.
 * If one observed variable is the first in the list of variables of the
 * relation, then the new relation will be inactive
 * (<code>active</code> = false).
 * Otherwise, <code>active</code> will remain unchanged.
 * @param ev is the given evidence.
 */

public void restrictToObservations(Evidence ev) {

  Relation r;
  int i, s;
  NodeList nodesEv, newNodeList, nodesEvCont;

  if (ev.size() > 0) {
    s = size();
    nodesEv = new NodeList(ev.getVariables());
    for (i=0 ; i<s ; i++) {
      r = elementAt(i);
      if (r.getValues()!=null) {
	r.setValues (r.getValues().restrictVariable((Configuration)ev));
      }

      // Keep the kind CONSTRAINT if present

      if (r.getKind() != Relation.CONSTRAINT){
        if (ev.isObserved(r.getVariables().elementAt(0)))
	  r.setKind(Relation.POTENTIAL);
      }
      newNodeList = r.getVariables().difference(nodesEv);
      
      //There may be continuous variables in the evidence that are not retrived with getVariables, but with getContinuousVariables
      if(ev.getContinuousVariables().size() > 0){
	nodesEvCont = new NodeList(ev.getContinuousVariables());
	newNodeList = newNodeList.difference(nodesEvCont);
      }
      r.setVariables(newNodeList);
    }
  }
}


/**
 * Removes constant relations.
 * This method remove all the constant relations, i.e. relations without variables
 * and that contains a single value, by multiplying the first non-constant relation
 * by the product of all the constant-relations values found.
 */

public void removeConstantRelations(){
    Relation r;
    double f=0.0,cFactor=1.0;
    Potential pot;
    PotentialTable pTable;
    ProbabilityTree pt;
    
    for(int i=0;i<this.size(); ){
        r = this.elementAt(i);
        if (r.getVariables().size()==0){
          pot = r.getValues();
          if (pot.getClassName().equals("PotentialTree")){
              pt = ((PotentialTree)pot).getTree();
              f = pt.getProb();
          }else if (pot.getClassName().equals("PotentialTable")){
              pTable = (PotentialTable) pot;
              f = pTable.getValue(0);
          }else{
              System.out.println("*** Error ***. " + pot.getClassName() + 
                            " is not comtempled in RelationList.removeConstantRelations()");
              System.exit(0);
          }
          cFactor *= f;
          this.removeRelationAt(i);
        }
        else i++;
        
    }//end of for
    
    if (cFactor != 1.0){
        r = this.elementAt(0);
        pot = r.getValues();
        
        if (pot.getClassName().equals("PotentialTable")){
          double[] v = ((PotentialTable)pot).getValues();
          for(int j=0;j<v.length;j++)
              v[j] *= cFactor;
        }else if (pot.getClassName().equals("PotentialTree")){
          pt = ((PotentialTree)pot).getTree();  
          ProbabilityTree pt2 = new ProbabilityTree(cFactor);
          pt = ProbabilityTree.combine(pt,pt2);
          ((PotentialTree)pot).setTree(pt);
        }
        
    }
        
}

/**
 * This method is used to get the union of the variables of all the relations
 * @return the list of the nodes in the relation.
 */

public NodeList getVariables() {
	NodeList nodes;
	
	nodes = new NodeList();
  
	for(int i=0;i<relations.size();i++){
		nodes.merge(((Relation)relations.elementAt(i)).getVariables());
	}
	return nodes;
}

/**
 * Retrieves a relation from this list.
 * @param nameOfRelation the name of relation that we are looking for.
 * @return the relation in the list which name is <code>nameOfRelation</code>.
 * If such relation is not in the list, returns <code>null</code>.
 */

public Relation getRelation(String nameOfRelation) {

  int i, s;
  boolean found = false;
  Relation r = null;

  s = size();
  for (i=0 ; (i<s)&&(!found) ; i++) {
    r = elementAt(i);
    if (r.getName().equals(nameOfRelation))
      found = true;
  }
  if (!found)
    return null;
  else
    return r;
}


/**
 * Retrieves the relation of a node from this list.
 * @param nameOfNode the name of child node of the relation that we are looking for.
 * @return the relation in the list which name is <code>nameOfRelation</code>.
 * If such relation is not in the list, returns <code>null</code>.
 */

public Relation getRelationByNameOfNode(String nameOfNode){
     int i, s;
  boolean found = false;
  Relation r = null;

  s = size();
  for (i=0 ; (i<s)&&(!found) ; i++) {
    r = elementAt(i);
    if (((Node)(r.getVariables().elementAt(0))).getName().equals(nameOfNode))
      found = true;
  }
  if (!found)
    return null;
  else
    return r;

}
/**
 * Updates the set of <code>PotentialFunction</code>s in the list
 * by assigning each argument in the potential given by a string
 * to the related Potential.
 */

public void repairPotFunctions() {

  int i, s, j;
  Relation r, rAux;
  PotentialFunction f, fAux = new PotentialFunction();
  CanonicalPotential p, pAux = new CanonicalPotential();
  UtilityPotential u, uAux= new UtilityPotential();

  s = size();
  for (i=0 ; i<s ; i++) {
    r = elementAt(i);
    if (r.getValues().getClass() == PotentialFunction.class) {
      f = (PotentialFunction) r.getValues();
      for (j=0 ; j<f.getArgumentsSize() ; j++) {
	if (f.getArgumentAt(j).getClass() == String.class) {
	  rAux = getRelation((String) f.getArgumentAt(j));
	  f.setArgumentAt(rAux.getValues(),j);
	}
      }
    }
    else if (r.getValues().getClass() == CanonicalPotential.class) {
      p = (CanonicalPotential) r.getValues();
      for (j=0 ; j<p.getArgumentsSize() ; j++) {
	if (p.getArgumentAt(j).getClass() == String.class) {
	  rAux = getRelation((String) p.getArgumentAt(j));
	  p.setArgumentAt(rAux.getValues(),j);
	}
      }
    }
    else if (r.getValues().getClass() == UtilityPotential.class){
       u = (UtilityPotential) r.getValues();
       /*Vector tmp = (Vector)r.getVariables().getNodes().clone();
       tmp.remove(0);
       u.setVariables(tmp);*/
        for (j=0 ; j<u.getArgumentsSize() ; j++) {
	if (u.getArgumentAt(j).getClass() == String.class) {
	  rAux = getRelationByNameOfNode((String) u.getArgumentAt(j));
	  u.setArgumentAt(rAux.getValues(),j);
	}
      } 
  }
}
}


/**
 * Enter evidence in potentials by making 0 those
 * values which disagree with observations
 */

public void enterEvidence(Evidence ev) {

  Relation R;
  int i, s;
  Potential pot;
  SetVectorOperations svo = new SetVectorOperations();
  Vector commonVars;

  s = this.size();

  for (i=0 ; i<s ; i++) {
    R = this.elementAt(i);
    commonVars = svo.intersection(R.getVariables().getNodes(),
                                  ev.getVariables());
    if (commonVars.size() != 0) {
      pot = R.getValues();
      pot.instantiateEvidence(ev);
    }
  }
}




} // End of class.
