/* PairTable.java */

package elvira.tools.idiagram.pairtable;

import elvira.IDiagram;
import elvira.PairTable;
import elvira.NodePairTable;
import elvira.Node;
import elvira.FiniteStates;
import elvira.Evidence;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.inference.elimination.ids.IDQualitativeVariableElimination;

import java.util.ArrayList;
import java.util.Vector;
import java.io.*;

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

public class IDPairTable extends PairTable {

/**
 * Constants for the criteria used for selection
 */
public final static int MINSIZE=1;
public final static int CANOMORAL=2;
public final static int MINFILL=3;
public final static int CLIQUESIZE=4;
public final static int CLIQUEWEIGHT=5;
public final static int WEIGHTFILL=6;
public final static int WEIGHTFILL_STATES=7;
public final static int MINFILL_STATES=8;
public final static int MINFILL_MINSIZE=9;
public final static int CANOMORAL_MINFILL=10;
public final static int RELATIONSWEIGHT=11;
public final static int OTRO=12;

/**
 * Data member for storing a qualitative copy of the influence
 * diagram for performing operations on it
 */
private IDiagram diagCopy;

/**
 * Data member used to store the criteria used for selecting a new
 * variable for influence diagrams
 */
private int iDCriteria;

/**
 * Data member for setting a given elimination order
 */
private ArrayList<String> numeration;
private static int stage=-1;

/**
 * Default constructor
 */
public IDPairTable(){
   super();
}

/**
  * Makes a PairTable for an influence diagram. The variables are placed by
  * reverse temporal order. Observed variables are not placed in the PairTable
  * because they do not need to be eliminated.
  * @param diag the influence diagram
  * @param evidence the available evidence. 
  */
public IDPairTable(IDiagram diag,Evidence evidence){
  super();
  RelationList relationsList,relationsOfNode;
  Relation relation;
  IDiagram diagCopy;
  IDQualitativeVariableElimination eval;
  Vector relations,order;
  Node node;
  boolean evaluable;
  int i,j;

  // Solve this ID with qualitative evaluation
  eval=new IDQualitativeVariableElimination(diag,false);

  // We assume the ID is evaluable (in any other case
  // we should check the initial conditions)
  evaluable=true;
  if (evaluable == false){
    System.out.println("The influence diagram is not evaluable.....");
    System.exit(-1);
  }

  // Gets the partial order
  eval.getPartialOrder();

  // From the evaluation, get the order used to remove the nodes 
  order=eval.getOrderOfElimination();

  // Copy the relations on relationsList (but only relations
  // not for constraints)
  relations=diag.getRelationList(); // Get the relations list
  relationsList=new RelationList(); // Create the RelationList  
  for(i=0; i < relations.size(); i++){ //Use a RelationsList instead of a Vector
    relation=(Relation)relations.elementAt(i); 
    if (relation.getKind() != Relation.CONSTRAINT)
        relationsList.insertRelation(relation);
  }

  // Look for the nodes which names are stored in order
  for(i=order.size()-1; i >= 0; i--) {
    node=diag.getNode((String)order.elementAt(i));

    // For every node, insert it (if no observed)
    if (node.getKindOfNode() == node.CHANCE) {
       // Test if observed
       if (!evidence.isObserved((FiniteStates)node)) {

           // Insert the variable
	        addElement((FiniteStates)node);
       }
    }
    else {
       // Insert the decision
       addElement((FiniteStates)node);
    }
  
    // Get the relations of this variable
    relationsOfNode=relationsList.getRelationsOf((FiniteStates)node);

    // Insert these relations in the pairtable
    for(j=0; j < relationsOfNode.size(); j++) {
	      addRelation((FiniteStates)node,relationsOfNode.elementAt(j));
    }
  }
}

/**
 * Returns the next node to remove according to
 * the criterium of minimum size for influence diagrams.
 * @return the variable to remove.
 */
public Node nextToRemoveID() {
  IDNodePairTable node;
  int i=info.size()-1;
  double s, min = 90.0E20;
  Node var=null;
  
  node=(IDNodePairTable)info.elementAt(i);
  if(node.getVariable().getKindOfNode()==Node.DECISION){
    var = node.getVariable();
  }
  else{
    do {  
      // If the node is in just one relation, remove it.
      if (node.getRelationsSize() == 1)
        return node.getVariable();   
      s = node.totalSize(); 
      if (s < min) {
        min = s;
        var = node.getVariable();	
      }
      if (i > 0)
         node = (IDNodePairTable)info.elementAt(--i);    
    } while((node.getVariable().getKindOfNode()!=Node.DECISION) && (i>0));  
  }
  return var;
}


/**
 * Inserts an element at the end of the list.
 * The element is a <code>Node</code> variable with no relations.
 * It is assumed that the argument is not in the list.
 * @param var a <code>FiniteStates</code> variable.
 */
public void addElement(Node var) {
  IDNodePairTable node;
  node = new IDNodePairTable(var);
  info.addElement(node);
}

/**
 * Method for setting the iDCriteria data member
 * @param value
 */
public void setIDCriteria(int value){
  if (value > 0 && value < 13)
    iDCriteria=value;
}

/**
 * Returns the next node to remove according to
 * the criterium selected for influence diagrams.
 * @return the variable to remove 
 */
public Node nextToRemoveIDWithCriteria() {
  IDNodePairTable node;
  int i=info.size()-1;
  double s, min = 90.0E20;
  Node var=null,varSelecc=null;
  
  node=(IDNodePairTable)info.elementAt(i);
  var=node.getVariable();
  varSelecc=var;

  // If var is a decision, nothing more to do. In any other
  // case, consider the variable
  if(var.getKindOfNode() != Node.DECISION){
    do {  
      // If the node is in just one relation, remove it.
      if (node.getRelationsSize() == 1){
        //return node.getVariable();   
      }
      s = node.computeCost(iDCriteria); 
      if (s < min) {
        min = s;
        varSelecc = node.getVariable();	
      }

      // Consider the next node
      if (i > 0){
        node = (IDNodePairTable)info.elementAt(--i);    
        var=node.getVariable();
      }
    } while((var.getKindOfNode()!=Node.DECISION) && (i>0));  
  }

  return varSelecc;
}

/**
 * Returns the next node to remove according to
 * the criterium selected for influence diagrams
 * and removing the variables as they are deleted
 * @return the variable to remove 
 */
public Node nextToRemoveIDWithCriteriaRemoving() {
  IDNodePairTable node,nodeSelecc;
  int i=info.size()-1;
  double s, min = 90.0E20;
  Node var=null,varSelecc=null;

  node=(IDNodePairTable)info.elementAt(i);
  var=node.getVariable();
  varSelecc=var;
  nodeSelecc=node;

  // If var is a decision, nothing more to do. In any other
  // case, consider the variable
  if(var.getKindOfNode() != Node.DECISION){
    do {  
      // If the node is in just one relation, remove it.
      if (node.getRelationsSize() == 1){
        //varSelecc=node.getVariable();   
        //nodeSelecc=node;
        // Break the loop
        //break;
      }

      // If there are several relations, compute the costs
      s = node.computeCost(iDCriteria); 

      if (s < min) {
        min = s;
        varSelecc = node.getVariable();	
        nodeSelecc=node;
      }

      // Consider the next node
      if (i > 0){
        node = (IDNodePairTable)info.elementAt(--i);    
        var=node.getVariable();
      }
    } while((var.getKindOfNode()!=Node.DECISION) && (i>0));  
  }

  // Remove the variable from the pairTable
  removeVariable(varSelecc);

  // To let the influence diagram prepared for the next operation,
  // remove the relations of the varSelecc, create a new relation with
  // all the variables in it 
  NodeList varsInRelations=new NodeList();
  Relation rel;
  for(i=0; i < nodeSelecc.getRelationsSize(); i++){
    rel=(Relation)nodeSelecc.getRelationAt(i);
    varsInRelations.join(rel.getVariables());

    // Remove rel
    removeRelation(rel);
  }

  // Create a new relation for the whole set of variables except
  // for the one belonging to node
  varsInRelations.removeNode(varSelecc);
  varsInRelations.removeNodes(Node.UTILITY);

  if (varsInRelations.size() > 0){
    rel=new Relation(varsInRelations.toVector());
    rel.setValues(null);

    // Add the new relation
    addRelation(rel);
  }
 
  // Return varSelecc 
  return varSelecc;
}

  /**
   * Public method for setting a given elimination order
   * @param fileName with the order of elimination
   */
  public void setEliminationOrder(String fileName){
    numeration=new ArrayList<String>();
    FileReader file=null;
    try{
      file=new FileReader(fileName);
    }catch(FileNotFoundException e){
      System.out.println("Elimination order description file "+fileName+" nor found");
      System.exit(0);
    }
    BufferedReader reader=new BufferedReader(file);

    // Read the names of the variables
    String varName;
    try{
      while((varName=reader.readLine()) != null){
        numeration.add(varName);
      }
    }catch(IOException e){};

    // Close the descriptors
    try{
      reader.close();
      file.close();
    }catch(IOException e){};
  }

  /**
   * Public method for removing a variable following the elimination
   * order stored at numeration
   */
  public Node nextToRemoveIDFixed(){
    if (stage == -1)
      stage=numeration.size()-1;

    String varName=numeration.get(stage--);
    Node node;

    // Look for this var into info vector
    for(int i=0; i < info.size(); i++){
      node=elementAt(i).getVariable();

      if (node.getName().equals(varName)){
        return node;
      }
    }

    // If this point is reached, something is wrong
    System.out.println("Problem with nextToRemoveIDFixed");
    return null;
  }
}  // End of class.
