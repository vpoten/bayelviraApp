/* IDiagram.java */

package elvira;

import java.util.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import elvira.Node;
import elvira.parser.*;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.inference.*;
import elvira.inference.elimination.*;
import elvira.inference.elimination.ids.*;
import elvira.inference.super_value.ArcReversalSV;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.inference.super_value.PolicyNetwork;
import elvira.potential.*;

/**
 * This class implements the structure for storing and
 * manipulating the Influence Diagrams.
 *
 * @version 0.1
 * @since 21/9/2000
 * @Autor: Manuel Gomez (with copy&paste of Bnet.java)
 */

public class IDiagram extends Bnet {

Propagation prop = null;
Node globalUtility = null;
CooperPolicyNetwork cpn = null;
PolicyNetwork pn = null;

/**
 * Policies that are forced to some decisions.
 * Evaluation algorithms for IDs don't take into account this attribute to propagate
 */
Hashtable<Node,Relation> forcedPolicies;

//Array with the posterior probabilities of the chance nodes in ArcReversal's method
public ArrayList posteriorDistributions = null;
//Array with the utilities of the chance nodes in ArcReversal's method 
public ArrayList posteriorUtilities = null;


/** Constructor by default
 */
public IDiagram(){
	forcedPolicies = new Hashtable();
 }
 

/**
 * Creates an Influence Diagram parsing it from a file.
 * @param name name of the file that contains the diagram.
 */
public IDiagram(String name) throws ParseException, IOException {
     super(name);
     forcedPolicies = new Hashtable();
            
 }


/**
 * Stores the <code>IDiagram</code> in the file given as parameter.
 * @param f file where the <code>IDiagram</code> is saved.
 * @see Network#save
 */

public void saveIDiagram(FileWriter f) throws IOException {

  PrintWriter p;

  p = new PrintWriter(f);

  super.save (p);
}

/**
 * Saves the header of the file that will contain this
 * diagram
 * @param p the file.
 */

public void saveHead(PrintWriter p) throws IOException {

  p.print("// Influence Diagram\n");
  p.print("//   Elvira format \n\n");
  p.print("idiagram  \""+getName()+"\" { \n\n");
}

/**
 * Shows the results of a propagation.
 * @param p the propagation.
 */

public void showResults (Propagation p) {
	
  setCompiledPotentialList(p.results);
  if ((p.getClass()==ArcReversal.class)||(p.getClass()==ArcReversalSV.class)){
  	setPosteriorDistributions(((ArcReversal)p).posteriorDistributions);
  	setPosteriorUtilities(((ArcReversal)p).posteriorUtilities);
  }
  setPropagation(p);
 
}

private void setPropagation (Propagation p) {
  prop = p;
}

public Propagation getPropagation () {
  return prop;
}

/**
 * To set global utility, in case there is
 */

public void setGlobalUtility(Node theGlobalUtility) {
  globalUtility = theGlobalUtility;
}

/**
 * Is there a global utility node?
 */

public Node isThereAGlobalUtilityNode() {
  return globalUtility;
}

/**
 * Checks that all the links are directed.
 * @return <code>true</code> if OK, <code>false</code> in other case.
 */

public boolean directedLinks() {
  LinkList list;
  Link link;
  boolean directed;
  int numberOfLinks;
  int i;

  list = getLinkList();
  numberOfLinks = list.size();

  for (i=0 ; i < numberOfLinks ; i++) {
    link = list.elementAt(i);
    directed = link.getDirected();
    if (directed == false)
      return false;
  }

  return true;
}


/**
 * Receives a list of variables, from another IDiagram
 * and return a list with the variables in this
 * @param <code>NodeList</code> list of variables
 * @return <code>NodeList</code> list of variables in this
 */

public NodeList insertVariablesIn(NodeList variables) {
  NodeList variablesInThis=new NodeList();
  NodeList allVariables=getNodeList();
  Node node;
  int i;

  // Insert all the variables in "variables"

  for(i=0; i < variables.size(); i++) {
    node=allVariables.getNode(variables.elementAt(i).getName());

     // This node is inserted in variablesInThis

     variablesInThis.insertNode(node);
  }

  // Return variablesInThis

  return(variablesInThis);
}

/**
 * Receives a list of relations, from another IDiagram, and
 * converts them so the variables are translated to the real
 * set of variables in this IDiagram
 */

public RelationList translateRelations(RelationList foreignRelations){
 RelationList newRelations=new RelationList();
 NodeList vars;
 Relation rel, newRel;
 int i;

  // Go on the list of relations

  for(i=0; i < foreignRelations.size(); i++){
    rel=foreignRelations.elementAt(i);
    newRel=rel.copy();
    vars=insertVariablesIn(rel.getVariables());
    newRel.setVariables(vars);

    // The new relation will be inserted in newRelations

    newRelations.insertRelation(newRel);
  }

  // Return the new list of relations

  return newRelations;
}

/**
 * Checks that there is exactly one value node.
 * @return <code>true</code> if OK, <code>false</code> in other case.
 */

public boolean onlyOneValueNode() {
  NodeList list;
  Node node;
  int numberOfNodes, valueNodes = 0, i;

  list = getNodeList();
  numberOfNodes = list.size();

  for (i=numberOfNodes-1 ; i >=0 ; i--) {
    node = list.elementAt(i);
    if (node.getKindOfNode() == node.UTILITY)
      valueNodes++;
  }

  if (valueNodes != 1)
    return false;

  return true;
}


/**
 * Checks that there is a path between the decision nodes
 * and the utility node.
 * @return <code>true</code> if OK, <code>false</code> in other case.
 */

public boolean pathBetweenDecisions() {
  Node lastDecision;
  NodeList decisions=getDecisionList();
  int numberOfDec=numberOfDecisions();

  // Test if all of the decision nodes are inserted

  if(numberOfDec == decisions.size()) {

  // Check if the utility node is accessible from
  // the last decision
	  
	  if (numberOfDec>0){

  lastDecision=decisions.elementAt(numberOfDec-1);

  if (lastDecision.isUtilityParent() == true)
    return(true);
	  }
	  else{
		  return true;
	  }
  }
  

  // If we are here, something wrong

  return(false);
}


/**
 * Checks that there is a path between the decision nodes
 * @return <code>true</code> if OK, <code>false</code> in other case.
 */
public boolean areDecisionsOrdered(){ 
boolean ordered = true;
	NodeList decisions;
	decisions=getNodesOfKind(Node.DECISION);
	Node iDec;
	
	for (int i=0;(i<(decisions.size()-1))&&ordered;i++){
		iDec=decisions.elementAt(i);
		for (int j=i+1;(j<decisions.size())&&ordered;j++){
			if (((iDec.isReachable(decisions.elementAt(j)))==false)&&
			((decisions.elementAt(j).isReachable(iDec))==false)){
				ordered=false;
			}
		}
	}
	
	return ordered;
	
/*	Node lastDecision;
	NodeList decisions=getDecisionList();
	int numberOfDec=numberOfDecisions();

//	 Test if all of the decision nodes are inserted

	 return (numberOfDec == decisions.size());*/
	
}

/**
 * Checks that there is a path between the decision nodes and randomly order decisions
 * that are not ordered by adding some links
 * @return <code>true</code> if the decisions were ordered previously, <code>false</code> in other case.
 */
public boolean orderDecisionsRandomlyIfNotOrdered(){ 
boolean wereOrdered = true;
	NodeList decisions;
	decisions=getNodesOfKind(Node.DECISION);
	Node iDec;
	Node jDec;
	Node nodeA;
	Node nodeB;
	double randomNumber;
	Random r=new Random();
		
	for (int i=0;(i<(decisions.size()-1));i++){
		iDec=decisions.elementAt(i);
		for (int j=i+1;(j<decisions.size());j++){
			jDec=decisions.elementAt(j);
			if (((iDec.isReachable(jDec))==false)&&
			((jDec.isReachable(iDec))==false)){
				//The decisions aren't ordered
				wereOrdered=false;
				randomNumber=r.nextDouble();
				//Order randomly two decisions
				if (randomNumber<0.5){
					nodeA=iDec;
					nodeB=jDec;
				}
				else{
					nodeB=iDec;
					nodeA=jDec;
				}
			
					try{
						createLink(nodeA,nodeB);
					} catch (InvalidEditException iee) {
					};
					System.out.println("Force order "+nodeA.getName()+" before "+nodeB.getName());
				}
			}
		}
	
	if (areDecisionsOrdered()==false){
		System.out.println("Error in method orderDecisionsIfNotOrdered in class IDiagram." +
				" Decisions must be ordered after a call to this method");
	}
	
	
	return wereOrdered;
	
/*	Node lastDecision;
	NodeList decisions=getDecisionList();
	int numberOfDec=numberOfDecisions();

//	 Test if all of the decision nodes are inserted

	 return (numberOfDec == decisions.size());*/
	
}


/**
 * Checks that there is a path between the decision nodes and greedily order
 * decisions that are not ordered by adding some links. It's more efficient than
 * orderDecisionsRandomlyIfNotOrdered.
 * Compare with addNonForgettingArcs()
 * @return <code>true</code> if the decisions were ordered previously, <code>false</code> in other case.
 */

public void orderDecisionsGreedilyIfNotOrdered(){
	 Node decision;
	  Node nextDecision;
	  NodeList decisions=getDecisionList();

	  int i;

	  for(i=0; i < decisions.size()-1; i++) {
	    decision=decisions.elementAt(i);
	    nextDecision=decisions.elementAt(i+1);

	    // Add link
	    addLink(decision,nextDecision);
	  }
}

/**
 * Method to add non-forgetting arcs.
 */

public void addNonForgettingArcs() {
  Node decision;
  Node nextDecision;
  NodeList decisions=getDecisionList();
  NodeList parentNodes;
  int i;

  for(i=0; i < decisions.size()-1; i++) {
    decision=decisions.elementAt(i);
    nextDecision=decisions.elementAt(i+1);

    // Get the parents of decision

    parentNodes=parents(decision);

    // Add links

    addLinks(parentNodes,nextDecision);
    addLink(decision,nextDecision);
  }
}

/**
 * Method to remove informative arcs. This method is required
 * to solve the influenced diagram with a juntion tree
 */
public void removeInformativeArcs(){
  LinkList links=getLinkList();
	Iterator iter=links.iterator();
	Node head;
	Link link;
	ArrayList<Link> linksToRemove=new ArrayList<Link>();

	// Get the links one by one
	while(iter.hasNext()){
		link=(Link)iter.next();

		// Check if it has a head a decision node
		head=link.getHead();

		if (head.getKindOfNode() == Node.DECISION){
			linksToRemove.add(link);
		}
	}

	// Delete all the links
	for(Link link1:linksToRemove){
	  links.removeLink(link1);
	}
}

/**
 * Method for removing constraint relations
 */
public void removeConstraintRelations(){
    Vector relations=getRelationList();
    Vector finalRelations=new Vector();
    Relation relation;
    
    // Consider relations one by one
    for(int i=0; i < relations.size(); i++){
        relation=(Relation)relations.elementAt(i);
        
        // Check its kind
        if (relation.getKind() != Relation.CONSTRAINT){
            finalRelations.add(relation);
        }
    }
    
    // Set this relations
    setRelationList(finalRelations);
}

/**
 * To detect whether the decision is ready to be removed.
 * @param the decision node to be removed.
 * @return the result of the operation.
 */

public boolean decisionReadyToRemove(Node candidate) {
  Node util;
  NodeList candidateChildren, candidateParents, utilParents;
  int i;

  util = getValueNode();
  utilParents = parents(util);
  candidateParents = parents(candidate);

  // The decision must be parent of the utility node

  candidateChildren = children(candidate);
  if (candidateChildren.size() == 1 && candidateChildren.elementAt(0) == util) {
    // All the parents of the utility node must be parents
    // of the decision node

    for (i=0 ; i < utilParents.size() ; i++) {
      if (utilParents.elementAt(i) != candidate &&
          candidateParents.getId(utilParents.elementAt(i)) == -1) {
        return false;
      }
    }

    // Return true

    return true;
  }

  // Return false

  return false;
}

/**
 * To eliminate the redundancy of the diagram.
 */

public void eliminateRedundancy(){
  NodeList decisionsList, decParents = new NodeList(), teta, separators;
  NodeList utilList=getValueNodes();
  Node dec, util;
  Link link;
  int i, j, k;
  boolean separated,analyzed;

  decisionsList = getDecisionList();

  for (i=decisionsList.size()-1 ; i >= 0; i--) {
    dec = decisionsList.elementAt(i);

     // Obtain the parents of the node

     decParents = parents(dec);
     teta = new NodeList();

     for (j=0 ; j < decParents.size() ; j++) {
      // For each of the parents, to see if it is
      // d-separated from the value nodes, given
      // the rest of the parents

      separators = decParents.copy();
      separators.removeNode(decParents.elementAt(j));
      separators.insertNode(dec);
      separated=true;
      analyzed=false;
   
      for(k=0; k < utilList.size() && separated; k++){
        // Consider this value node
        util=utilList.elementAt(k);

        // Find out if util is relevant for dec		
        if (descendantOf(dec,util)){
		      //Find out if decParents.elementAt(j) is required for d with 
          //respect to util
		      separated=independents(decParents.elementAt(j),util,separators);
          analyzed=true;
		    }
	   }

	   // If separated == true, add this node
	   if (separated && analyzed) 
        teta.insertNode(decParents.elementAt(j));
	   }

	   // Now, eliminate the arcs between the nodes in teta and dec
	   for (j=0 ; j < teta.size(); j++) {
	     link = getLink(teta.elementAt(j),dec);
	     try{
System.out.println("Eliminado enlace entre "+teta.elementAt(j).getName()+" y "+dec.getName());
		    removeLink(link);
	     }
	     catch (InvalidEditException iee){System.out.println("Enlace nulo");}
	   }

	   // remove barren nodes
	   removeBarrenNodes();
  }
}

public void specialEliminateRedundancy(){
  NodeList decisionsList, decParents = new NodeList(), teta, separators;
  NodeList utilList=getValueNodes();
  Node dec, util;
  Link link;
  int i, j, k;
  boolean separated;

  decisionsList = getDecisionList();

  for (i=decisionsList.size()-1 ; i >= 0 ; i--) {
    dec = decisionsList.elementAt(i);

    // Obtain the parents of the node

    decParents = parents(dec);
    teta = new NodeList();

    for (j=0 ; j < decParents.size() ; j++) {
      // For each of the parents, to see if it is
      // d-separated from the value node, given
      // the rest of nodes of the parents

      separators = decParents.copy();
      separators.removeNode(decParents.elementAt(j));
      separators.insertNode(dec);

      separated=true;
      for(k=0; k < utilList.size(); k++){
        util=utilList.elementAt(k);
        if (independents(decParents.elementAt(j),util,separators) == false){
          separated=false;
          break;
        }
      }

      // If separated == true, add this node

      teta.insertNode(decParents.elementAt(j));
    }

    // Now, eliminate the arcs between the nodes in teta and dec

    for (j=0 ; j < teta.size(); j++) {
      link = getLink(teta.elementAt(j),dec);
      try{
        removeLink(link);
      }
      catch (InvalidEditException iee){;}
    }

    // remove barren nodes

    removeBarrenNodes();
  }
}




/**
 * To remove the barren nodes of the diagram
 * (chance or decision nodes without sucesors).
 */

public void removeBarrenNodes() {
  Node barren;

  while ((barren = getBarrenNode())!= (Node) null){
System.out.println("Eliminando sumidero: "+barren.getName());
    removeNodeOnly(barren);
  }
}

/**
 * Removes \"VariableDoesntMakeSense\" Constraint
 */

private void removeVariableDoesntMakeSense(Node theNode, Node theParent, double[] theValues, Relation theConstraint) {
  if (theNode.getKindOfNode() == Node.DECISION) {
    NodeList theChildren = theNode.getChildrenNodes();
    double[] theChildValues = new double[0];
    for (int i=0; i<theChildren.size(); i++) {
      if (((Node) theChildren.elementAt(i)).getName().equals(((Node) theConstraint.getVariables().elementAt(1)).getName())) {
        if (((Node) theChildren.elementAt(i)).getKindOfNode() == Node.CHANCE) {
          if (getRelation((Node) theChildren.elementAt(i)).getValues().getClass() == PotentialTable.class) {
            double[] proV = ((PotentialTable) getRelation((Node) theChildren.elementAt(i)).getValues()).getValues();
            Vector toAdd = new Vector();
            Configuration config = new Configuration(getRelation((Node) theChildren.elementAt(i)).getVariables());
            for (int j=0; j<proV.length; j++) {
              toAdd.addElement(new Double(proV[j]));
              config.nextConfiguration();
            }
            theChildValues = new double[toAdd.size()];
            for (int j=0; j<toAdd.size(); j++) {
              theChildValues[j]=((Double) toAdd.elementAt(j)).doubleValue();
            }
          }
          else if (getRelation((Node) theChildren.elementAt(i)).getValues().getClass() == CanonicalPotential.class) {
            /* TO DO */
            Vector toAdd = new Vector();
            for (int m=0; m<((CanonicalPotential) getRelation((Node) theChildren.elementAt(i)).getValues()).getArguments().size(); m++) {
              double[] proV = ((PotentialTable) ((CanonicalPotential) getRelation((Node) theChildren.elementAt(i)).getValues()).getArgumentAt(m)).getValues();
              Configuration config = new Configuration(((PotentialTable) ((CanonicalPotential) getRelation((Node) theChildren.elementAt(i)).getValues()).getArgumentAt(m)).getVariables());
              for (int j=0; j<proV.length; j++) {
                toAdd.addElement(new Double(proV[j]));
                config.nextConfiguration();
              }
            }
            theChildValues = new double[toAdd.size()];
            for (int j=0; j<toAdd.size(); j++) {
              theChildValues[j]=((Double) toAdd.elementAt(j)).doubleValue();
            }
          }
        }
        else if (((Node) theChildren.elementAt(i)).getKindOfNode() == Node.UTILITY) {
          theChildValues = ((PotentialTable) getRelation((Node)theChildren.elementAt(i)).getValues()).getValues();
        }

        if (theChildren.elementAt(i).getKindOfNode() != Node.DECISION) {
          removeVariableDoesntMakeSense(theChildren.elementAt(i),theNode,theChildValues,theConstraint);
        }
      }
    }
  }
  else if (theNode.getKindOfNode() == Node.CHANCE) {
    NodeList theParents = theNode.getParentNodes();
    boolean toRemove = true;
    for (int i=0; i<theParents.size(); i++) {
      if (true) {//(!theParents.elementAt(i).getName().equals(theParent.getName())) {
        if (theParents.elementAt(i).getKindOfNode()==Node.CHANCE) {
          Vector parentStates = ((FiniteStates) theParents.elementAt(i)).getStates();
          for (int j=0; j<parentStates.size(); j++) {
            if (((String) parentStates.elementAt(j)).equals("\"VariableDoesntMakeSense\"")) {
              toRemove = false;
            }
          }
        }
        //else if (theParents.elementAt(i).getKindOfNode()==Node.DECISION) {
        else if ((theParents.elementAt(i).getKindOfNode()==Node.DECISION) &&
                 (!theParents.elementAt(i).getName().equals(theParent.getName()))) {
          Vector relList = getRelationList();
          for (int j=0; j<relList.size(); j++) {
            if (((Relation) relList.elementAt(j)).getKind() == Relation.CONSTRAINT) {
              if (((Relation) relList.elementAt(j)).getComment().equals("Non sense constraint")) {
                NodeList nList = ((Relation) relList.elementAt(j)).getVariables();
                if (nList.elementAt(0).getName().equals(theParents.elementAt(i).getName())) {
                  if (nList.elementAt(1).getName().equals(theNode.getName())) {
                    toRemove = false;
                  }
                }
                else if (nList.elementAt(0).getName().equals(theNode.getName())) {
                  if (nList.elementAt(1).getName().equals(theParents.elementAt(i).getName())) {
                    toRemove = false;
                  }
                }
              }
            }
          }
        }
      }
    }
    /* Depending on toRemove, there is something to do with the values... */
    Vector theQuantity = new Vector();
    if (toRemove) {
      if (getRelation(theNode).getValues().getClass() == PotentialTable.class) {
        Configuration config = new Configuration(getRelation(theNode).getValues().getVariables());
        double[] oldValues = new double[theValues.length];
        for (int i=0; i<theValues.length; i++) {
          oldValues[i] = theValues[i];
        }
        Vector adding = new Vector();
        int nonSensePosition = -1;
        for (int i=0; i<((FiniteStates) theNode).getStates().size(); i++) {
          if (((FiniteStates) theNode).getState(i).equals("\"VariableDoesntMakeSense\"")) {
            nonSensePosition = i;
          }
        }
        if (nonSensePosition == -1) {
          System.out.println("Error 1!!!!!!!!!!! No \"VariableDoesntMakeSense\"!!!!!!!!!!!!!!");
          return;
        }
        for (int i=0; i<oldValues.length; i++) {
          if (config.getValue((FiniteStates) theNode) != nonSensePosition) {
            adding.addElement(new Double(oldValues[i]));
          }
          config.nextConfiguration();
        }
        theValues = new double[adding.size()];
        for (int i=0; i<adding.size(); i++) {
          theValues[i] = new Double(adding.elementAt(i).toString()).doubleValue();
        }
      }
      else if (getRelation(theNode).getValues().getClass() == CanonicalPotential.class) {
        int howmany = 0;
        Vector adding = new Vector();
        for (int j=0; j<((CanonicalPotential) getRelation(theNode).getValues()).getArguments().size(); j++) {
          Configuration config = new Configuration(((PotentialTable) ((CanonicalPotential) getRelation(theNode).getValues()).getArgumentAt(j)).getVariables());
          double[] oldValues = new double[(int) ((PotentialTable) ((CanonicalPotential) getRelation(theNode).getValues()).getArgumentAt(j)).getSize()];
          for (int i=0; i<oldValues.length; i++) {
            oldValues[i] = theValues[i+howmany];
          }
          int nonSensePosition = -1;
          int partial = 0;
          for (int i=0; i<((FiniteStates) theNode).getStates().size(); i++) {
            if (((FiniteStates) theNode).getState(i).equals("\"VariableDoesntMakeSense\"")) {
              nonSensePosition = i;
            }
          }
          if (nonSensePosition == -1) {
            System.out.println("Error 2!!!!!!!!!!! No \"VariableDoesntMakeSense\"!!!!!!!!!!!!!!");
            return;
          }
          for (int i=0; i<oldValues.length; i++) {
            if (config.getValue((FiniteStates) theNode) != nonSensePosition) {
              adding.addElement(new Double(oldValues[i]));
              partial++;
            }
            config.nextConfiguration();
            if (config.getValue(0) == 0) {
              if ((config.getVariables().size() > 1) && (config.getValue(1) == 0)) {
                break;
              }
              else if (config.getVariables().size() == 1) {
                break;
              }
            }
          }
          if ((config.getVariables().size() == 2) &&
              (((Node) config.getVariables().elementAt(1)).getName().equals(theParent.getName())) &&
              (theParent.getKindOfNode() != Node.DECISION) &&
              (toRemove)) {
            howmany = howmany + oldValues.length - ((FiniteStates) theNode).getNumStates();
          }
          else {
            howmany = howmany + oldValues.length;
          }
          theQuantity.addElement(new Integer(partial));
        }
        theValues = new double[adding.size()];
        for (int i=0; i<adding.size(); i++) {
          theValues[i] = new Double(adding.elementAt(i).toString()).doubleValue();
        }
      }
    }
    else {
      if (getRelation(theNode).getValues().getClass() == CanonicalPotential.class) {
        int tot1 = 0;
        for (int p=0; p<((CanonicalPotential) getRelation(theNode).getValues()).getArguments().size(); p++) {
          tot1 = tot1 + ((PotentialTable) ((CanonicalPotential) getRelation(theNode).getValues()).getArgumentAt(p)).getValues().length;
        }

        if (tot1 != theValues.length) {
          for (int p=0; p<((CanonicalPotential) getRelation(theNode).getValues()).getArguments().size(); p++) {
            int tot2;
            if (((PotentialTable) ((CanonicalPotential) getRelation(theNode).getValues()).getArgumentAt(p)).getVariables().size() == 1) {
              tot2 = ((FiniteStates) ((PotentialTable) ((CanonicalPotential) getRelation(theNode).getValues()).getArgumentAt(p)).getVariables().elementAt(0)).getNumStates();
            }
            else {
              tot2 = ((FiniteStates) ((PotentialTable) ((CanonicalPotential) getRelation(theNode).getValues()).getArgumentAt(p)).getVariables().elementAt(0)).getNumStates() *
                     ((FiniteStates) ((PotentialTable) ((CanonicalPotential) getRelation(theNode).getValues()).getArgumentAt(p)).getVariables().elementAt(1)).getNumStates();
            }
            theQuantity.addElement(new Integer(tot2));
          }
        }
      }
    }
    if (getRelation(theNode).getValues().getClass() == PotentialTable.class) {
      int cols = 1;
      for (int m=0; m<((PotentialTable) getRelation(theNode).getValues()).getVariables().size()-1; m++) {
        cols = cols * ((FiniteStates) ((PotentialTable) getRelation(theNode).getValues()).getVariables().elementAt(m+1)).getNumStates();
      }
      double totValue;
      for (int m=0; m<cols; m++) {
        totValue = 0.0;
        for (int n=0; n<theValues.length/cols-1; n++) {
          totValue = theValues[m+n*cols] + totValue;
        }
        double lastStateValue = Math.round((1-totValue)*10000);
        lastStateValue = lastStateValue/10000;
        theValues[m+(theValues.length/cols-1)*cols]=lastStateValue;
      }
      ((PotentialTable) getRelation(theNode).getValues()).setValues(theValues);
    }
    else if (getRelation(theNode).getValues().getClass() == CanonicalPotential.class) {
      int howmany = 0;
      //for (int m=0; m<((CanonicalPotential) getRelation(theNode).getValues()).getArguments().size(); m++) {
      for (int m=0; m<theQuantity.size(); m++) {
        double[] theVal = new double[((Integer) theQuantity.elementAt(m)).intValue()];
        for (int j=0; j<((Integer) theQuantity.elementAt(m)).intValue(); j++) {
          theVal[j] = theValues[j+howmany];
        }
        int cols = 1;
        for (int h=0; h<((PotentialTable) ((CanonicalPotential) getRelation(theNode).getValues()).getArgumentAt(m)).getVariables().size()-1; h++) {
          cols = cols * ((FiniteStates) ((PotentialTable) ((CanonicalPotential) getRelation(theNode).getValues()).getArgumentAt(m)).getVariables().elementAt(h+1)).getNumStates();
        }
        double totValue;
        for (int h=0; h<cols; h++) {
          totValue = 0.0;
          for (int n=0; n<theVal.length/cols-1; n++) {
            totValue = theVal[h+n*cols] + totValue;
          }
          double lastStateValue = Math.round((1-totValue)*10000);
          lastStateValue = lastStateValue/10000;
          theVal[h+(theVal.length/cols-1)*cols]=lastStateValue;
        }
        PotentialTable pt = new PotentialTable(((PotentialTable) ((CanonicalPotential) getRelation(theNode).getValues()).getArgumentAt(m)).getVariables());
        pt.setValues(theVal);
        ((CanonicalPotential) getRelation(theNode).getValues()).setArgumentAt(pt,m);
        Vector theRels = getRelationList();
        Relation r = new Relation();
        for (int h=0; h<theRels.size(); h++) {
          if (!((Relation) theRels.elementAt(h)).getActive()) {
            if (pt.getVariables().size() > 1) {
              if (((Relation) theRels.elementAt(h)).getValues().getVariables().size() == 2) {
                if (((Node) ((Relation) theRels.elementAt(h)).getValues().getVariables().elementAt(1)).getName().equals(((Node) pt.getVariables().elementAt(1)).getName())) {
                  r = (Relation) theRels.elementAt(h);
                  break;
                }
              }
            }
            else {
              if (((Relation) theRels.elementAt(h)).getValues().getVariables().size() == 1) {
                r = (Relation) theRels.elementAt(h);
                break;
              }
            }
          }
        }
        r.setValues(pt);
        howmany = howmany + ((Integer) theQuantity.elementAt(m)).intValue();
      }
    }
    NodeList theChildren = theNode.getChildrenNodes();
    double[] theChildValues = new double[0];
    for (int i=0; i<theChildren.size(); i++) {
      if (((Node) theChildren.elementAt(i)).getKindOfNode() == Node.CHANCE) {
        if (getRelation((Node) theChildren.elementAt(i)).getValues().getClass() == PotentialTable.class) {
          double[] proV = ((PotentialTable) getRelation((Node) theChildren.elementAt(i)).getValues()).getValues();
          Vector toAdd = new Vector();
          Configuration config = new Configuration(getRelation((Node) theChildren.elementAt(i)).getVariables());
          for (int j=0; j<proV.length; j++) {
            if (!((FiniteStates) theNode).getState(config.getValue(theNode.getName())).equals("\"VariableDoesntMakeSense\"")) {
              toAdd.addElement(new Double(proV[j]));
            }
            else {
              if (!toRemove) {
                toAdd.addElement(new Double(proV[j]));
              }
            }
            config.nextConfiguration();
          }
          theChildValues = new double[toAdd.size()];
          for (int j=0; j<toAdd.size(); j++) {
            theChildValues[j]=((Double) toAdd.elementAt(j)).doubleValue();
          }
        }
        else if (getRelation((Node) theChildren.elementAt(i)).getValues().getClass() == CanonicalPotential.class) {
          Vector toAdd = new Vector();
          for (int m=0; m<((CanonicalPotential) getRelation((Node) theChildren.elementAt(i)).getValues()).getArguments().size(); m++) {
            Configuration config = new Configuration(((PotentialTable) ((CanonicalPotential) getRelation((Node) theChildren.elementAt(i)).getValues()).getArgumentAt(m)).getVariables());
            double[] proV = ((PotentialTable) ((CanonicalPotential) getRelation((Node) theChildren.elementAt(i)).getValues()).getArgumentAt(m)).getValues();
            for (int j=0; j<proV.length; j++) {
              //if (!((FiniteStates) theNode).getState(config.getValue(theNode.getName())).equals("\"VariableDoesntMakeSense\"")) {
              FiniteStates theFSNode = new FiniteStates();
              boolean toDo = false;
              for (int l=0; l<config.getVariables().size(); l++) {
                if (config.getVariable(l).getName().equals(theNode.getName())) {
                  theFSNode = (FiniteStates) config.getVariable(l);
                  toDo = true;
                }
              }
              if (toDo) {
                if (!((FiniteStates) theFSNode).getState(config.getValue(theNode.getName())).equals("\"VariableDoesntMakeSense\"")) {
                  toAdd.addElement(new Double(proV[j]));
                }
                else {
                  if (!toRemove) {
                    toAdd.addElement(new Double(proV[j]));
                  }
                }
              }
              else {
                toAdd.addElement(new Double(proV[j]));
              }
              config.nextConfiguration();
            }
          }
          theChildValues = new double[toAdd.size()];
          for (int j=0; j<toAdd.size(); j++) {
            theChildValues[j]=((Double) toAdd.elementAt(j)).doubleValue();
          }
        }
      }
      else if (((Node) theChildren.elementAt(i)).getKindOfNode() == Node.UTILITY) {
        if (toRemove) {
          Vector toAdd = new Vector();
          Configuration config = new Configuration(((PotentialTable) getRelation((Node) theChildren.elementAt(i)).getValues()).getVariables());
          for (int j=0; j<((PotentialTable) getRelation((Node) theChildren.elementAt(i)).getValues()).getSize(); j++) {
            if (!((FiniteStates) theNode).getState(config.getValue(theNode.getName())).equals("\"VariableDoesntMakeSense\"")) {
              toAdd.addElement(new Double(((PotentialTable) getRelation((Node) theChildren.elementAt(i)).getValues()).getValue(config)));
            }
            config.nextConfiguration();
          }
          theChildValues = new double[(int) toAdd.size()];
          for (int j=0; j<toAdd.size(); j++) {
            theChildValues[j] = ((Double) toAdd.elementAt(j)).doubleValue();
          }
        }
        else {
          theChildValues = ((PotentialTable) getRelation((Node) theChildren.elementAt(i)).getValues()).getValues();
        }
      }

      if (theChildren.elementAt(i).getKindOfNode() != Node.DECISION) {
        if (toRemove) {
          Vector theStates = ((FiniteStates) theNode).getStates();
          Vector theNewStates = new Vector();
          for (int j=0; j<theStates.size(); j++) {
            if (!((String) theStates.elementAt(j)).equals("\"VariableDoesntMakeSense\"")) {
              theNewStates.addElement((String) theStates.elementAt(j));
            }
          }
          ((FiniteStates) theNode).setStates(theNewStates);
        }

        removeVariableDoesntMakeSense(theChildren.elementAt(i),theNode,theChildValues,theConstraint);

        if (toRemove) {
          Vector theStates = ((FiniteStates) theNode).getStates();
          Vector theNewStates = new Vector();
          for (int j=0; j<theStates.size(); j++) {
            theNewStates.addElement((String) theStates.elementAt(j));
          }
          theNewStates.addElement("\"VariableDoesntMakeSense\"");
          ((FiniteStates) theNode).setStates(theNewStates);
        }
      }
    }
    if (toRemove) {
      Vector theStates = ((FiniteStates) theNode).getStates();
      Vector theNewStates = new Vector();
      for (int i=0; i<theStates.size(); i++) {
        if (!((String) theStates.elementAt(i)).equals("\"VariableDoesntMakeSense\"")) {
          theNewStates.addElement((String) theStates.elementAt(i));
        }
      }
      ((FiniteStates) theNode).setStates(theNewStates);
    }
  }
  else if (theNode.getKindOfNode() == Node.UTILITY) {
    ((PotentialTable) getRelation(theNode).getValues()).setValues(theValues);
  }
}

/**
 * Removes a constraint
 */

public void removeConstraint(Relation rel) {
  for (int i=0; i<getRelationList().size(); i++) {
    if (rel == getRelationList().elementAt(i)) {
      if (((Relation) getRelationList().elementAt(i)).getComment().equals("Non sense constraint")) {
        Node theNode = (Node) rel.getVariables().elementAt(0);
        removeVariableDoesntMakeSense(theNode,null,null,rel);
      }
      getRelationList().removeElementAt(i);
      break;
    }
  }
}

/**
 * Creates a \"VariableDoesntMakeSense\"Constraint
 */

public void createVariableDoesntMakeSenseConstraint(FiniteStates theDNode, FiniteStates theCNode, String stateName) {
  Relation theCRelation = getRelation(theCNode);
  double[] theInitialValues = new double[(int) theCRelation.getValues().getSize()];
  if (theCRelation.getValues().getClass() == PotentialTable.class) {
    theInitialValues = ((PotentialTable) theCRelation.getValues()).getValues();
  }
  else if (theCRelation.getValues().getClass() == CanonicalPotential.class) {
    Vector allOfThem = new Vector();
    for (int m=0; m<((CanonicalPotential) theCRelation.getValues()).getArguments().size(); m++) {
      PotentialTable pt = (PotentialTable) ((CanonicalPotential) theCRelation.getValues()).getArgumentAt(m);
      for (int n=0; n<pt.getSize(); n++) {
        allOfThem.addElement(new Double(((PotentialTable) ((CanonicalPotential) theCRelation.getValues()).getArgumentAt(m)).getValue(n)));
      }
    }
    theInitialValues = new double[(int) allOfThem.size()];
    for (int m=0; m<allOfThem.size(); m++) {
      theInitialValues[m] = ((Double) allOfThem.elementAt(m)).doubleValue();
    }
  }

  Vector theStates = ((FiniteStates) theCNode).getStates();
  boolean toAdd = true;
  for (int k=0; k<theStates.size(); k++) {
    if (theStates.elementAt(k).equals("\"VariableDoesntMakeSense\"")) {
      toAdd=false;
    }
  }
  if (toAdd) {
    theStates.addElement("\"VariableDoesntMakeSense\"");
    ((FiniteStates) theCNode).setStates(theStates);
  }
  NodeList theNL = new NodeList();
  Vector theVars = new Vector();
  theNL.insertNode(theDNode);
  theNL.insertNode(theCNode);
  theVars.addElement(theDNode);
  theVars.add(theCNode);

  // -> Please note -> The following method should be recursive
  fillInChanceValues(theDNode, theCNode, stateName,//(String) ((FiniteStates) theDNode).getState(((FiniteStates) theDNode).getStates().size()-1),
                     theInitialValues, theCRelation, true, toAdd);

  Relation theConstraint = new Relation(theVars);
  theConstraint.setKind(Relation.CONSTRAINT);
  theConstraint.setComment("Non sense constraint");
  Vector forAVS = new Vector();
  //forAVS.addElement(((FiniteStates) theDNode).getState(((FiniteStates) theDNode).getStates().size()-1));
  forAVS.addElement(stateName);
  ValuesSet forAntecedent = new ValuesSet(theDNode,forAVS,false);
  Vector forCVS = new Vector();
  forCVS.addElement("\"VariableDoesntMakeSense\"");
  ValuesSet forConsequent = new ValuesSet(theCNode,forCVS,false);
  LogicalNode theAntecedent = new LogicalNode(forAntecedent);
  LogicalNode theConsequent = new LogicalNode(forConsequent);
  LogicalExpression theLogicExpr = new LogicalExpression(theAntecedent,theConsequent,LogicalNode.DOUBLE_IMPLICATION);
  theConstraint.setValues(theLogicExpr);
  addRelation(theConstraint);
}

/**
 * To fill the modified tables in the node related and the children ones.
 */

private void fillInChanceValues(Node father, Node child, String state,
                                double[] originalValues, Relation theRelation,
                                boolean isTheBeginning, boolean addedState)
{
  if (theRelation.getValues().getClass() == PotentialTable.class) {
    if (child.getKindOfNode() == Node.CHANCE) {
      PotentialTable poti = new PotentialTable(theRelation.getVariables());
      Configuration config = new Configuration(poti.getVariables());
      int statePosition = -1;
      for (int i=0; i<((FiniteStates) father).getStates().size(); i++) {
        if (state.equals(((FiniteStates) father).getState(i))) {
          statePosition = i;
        }
      }
      if (statePosition == -1) {
        System.out.println("No state with name "+state+"!!!!!!!!!!!!!!");
        System.exit(1);
      }
      int stateTargetPosition = ((FiniteStates) child).getStates().size()-1;
      int j=0;
      for (int i=0; i<poti.getSize(); i++) {
        if (config.getValue(father.getName()) == statePosition) {
          if (config.getValue(child.getName()) == stateTargetPosition) {
            poti.setValue(config,1.0);
            if (!addedState) {
              j++;
            }
          }
          else {
            poti.setValue(config,0.0);
            if ((isTheBeginning) || (!addedState)) {
              j++;
            }
          }
        }
        else {
          if ((config.getValue(child.getName()) == stateTargetPosition) && (addedState)) {
            poti.setValue(config,0.0);
          }
          else {
            poti.setValue(config,originalValues[j]);
            j++;
          }
        }
        config.nextConfiguration();
      }
      theRelation.setValues(poti);
      //theRelation.print();
      double[][] theInitialValues = new double[(int) child.getChildrenNodes().size()][];
      for (int k=0; k<child.getChildrenNodes().size(); k++) {
        if (((Node) child.getChildrenNodes().elementAt(k)).getKindOfNode() != Node.DECISION) {
          boolean toAdd = true;
          theInitialValues[k] = new double[(int) getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues().getSize()];
          if (getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues().getClass() == PotentialTable.class) {
            theInitialValues[k] = ((PotentialTable) getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues()).getValues();
            if (((Node) child.getChildrenNodes().elementAt(k)).getKindOfNode() == Node.CHANCE) {
              Vector theStates = ((FiniteStates) child.getChildrenNodes().elementAt(k)).getStates();
              for (int l=0; l<theStates.size(); l++) {
                if (theStates.elementAt(l).equals("\"VariableDoesntMakeSense\"")) {
                  toAdd = false;
                }
              }
              if (toAdd) {
                theStates.addElement("\"VariableDoesntMakeSense\"");
                ((FiniteStates) child.getChildrenNodes().elementAt(k)).setStates(theStates);
              }
            }
            else if (((Node) child.getChildrenNodes().elementAt(k)).getKindOfNode() == Node.UTILITY) {
              toAdd = addedState;
            }
          }
          else if (getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues().getClass() == CanonicalPotential.class) {
            // TO DO
            Vector adding = new Vector();
            for (int m=0; m<((CanonicalPotential) getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues()).getArguments().size(); m++) {
              for (int n=0; n<((PotentialTable) ((CanonicalPotential) getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues()).getArgumentAt(m)).getSize(); n++) {
                adding.addElement(new Double(((PotentialTable) ((CanonicalPotential) getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues()).getArgumentAt(m)).getValue(n)));
              }
            }
            theInitialValues[k] = new double[(int) adding.size()];
            for (int m=0; m<adding.size(); m++) {
              theInitialValues[k][m] = ((Double) adding.elementAt(m)).doubleValue();
            }
            if (((Node) child.getChildrenNodes().elementAt(k)).getKindOfNode() == Node.CHANCE) {
              Vector theStates = ((FiniteStates) child.getChildrenNodes().elementAt(k)).getStates();
              for (int l=0; l<theStates.size(); l++) {
                if (theStates.elementAt(l).equals("\"VariableDoesntMakeSense\"")) {
                  toAdd = false;
                }
              }
              if (toAdd) {
                theStates.addElement("\"VariableDoesntMakeSense\"");
                ((FiniteStates) child.getChildrenNodes().elementAt(k)).setStates(theStates);
              }
            }
          }
          fillInChanceValues(child,(Node) child.getChildrenNodes().elementAt(k),"\"VariableDoesntMakeSense\"",
                             theInitialValues[k],getRelation((Node) child.getChildrenNodes().elementAt(k)),
                             false,toAdd);
        }
      }
    }
    else if (child.getKindOfNode() == Node.UTILITY) {
      NodeList theVariables = theRelation.getVariables();
      Vector thePotVariables = new Vector();
      for (int i=0; i<theVariables.size(); i++) {
        if (theVariables.elementAt(i).getKindOfNode() != Node.UTILITY) {
          thePotVariables.addElement(theVariables.elementAt(i));
        }
      }
      PotentialTable poti = new PotentialTable(thePotVariables);
      Configuration config = new Configuration(poti.getVariables());
      int statePosition = -1;
      for (int i=0; i<((FiniteStates) father).getStates().size(); i++) {
        if (state.equals(((FiniteStates) father).getState(i))) {
          statePosition = i;
        }
      }
      if (statePosition == -1) {
        System.out.println("No state with name "+state+"!!!!!!!!!!!!!!");
        System.exit(1);
      }
      int j=0;
      for (int i=0; i<poti.getSize(); i++) {
        if (config.getValue(father.getName()) == statePosition) {
          poti.setValue(config,0.0);
          if (!addedState) {
            j++;
          }
        }
        else {
          poti.setValue(config,originalValues[j]);
          j++;
        }
        config.nextConfiguration();
      }
      theRelation.setValues(poti);
    }
  }
  else if (theRelation.getValues().getClass() == CanonicalPotential.class) {
    // TO DO
    int howmany = 0;
    for (int o=0; o<((CanonicalPotential) theRelation.getValues()).getArguments().size(); o++) {
      PotentialTable poti = new PotentialTable(((PotentialTable) ((CanonicalPotential) theRelation.getValues()).getArgumentAt(o)).getVariables());
      Configuration config = new Configuration(poti.getVariables());
      boolean columnAndRow = false;
      for (int i=0; i<poti.getVariables().size(); i++) {
        if (((Node) poti.getVariables().elementAt(i)).getName().equals(father.getName())) {
          columnAndRow = true;
        }
      }
      int statePosition = -1;
      if (columnAndRow) {
        for (int i=0; i<((FiniteStates) father).getStates().size(); i++) {
          if (state.equals(((FiniteStates) father).getState(i))) {
            statePosition = i;
          }
        }
      }
      int stateTargetPosition = ((FiniteStates) child).getStates().size()-1;
      int j=0;
      if (columnAndRow) {
        for (int i=0; i<poti.getSize(); i++) {
          if (config.getValue(father.getName()) == statePosition) {
            if (config.getValue(child.getName()) == stateTargetPosition) {
              poti.setValue(config,1.0);
              if (!addedState) {
                j++;
              }
            }
            else {
              poti.setValue(config,0.0);
              if (isTheBeginning) { //((isTheBeginning) || (!addedState)) {
                j++;
              }
            }
          }
          else {
            if ((config.getValue(child.getName()) == stateTargetPosition) && (addedState)) {
              poti.setValue(config,0.0);
            }
            else {
              poti.setValue(config,originalValues[j+howmany]);
              j++;
            }
          }
          config.nextConfiguration();
        }
      }
      else {
        if (addedState) {
          int columns = 0;
          if (poti.getVariables().size() == 1) {
            columns = 1;
          }
          else {
            columns = ((FiniteStates) poti.getVariables().elementAt(1)).getNumStates();
          }
          for (int m=0; m<poti.getSize(); m++) {
            if ((m-columns*(((FiniteStates) child).getNumStates()-1)) >= 0) {
              poti.setValue(m,0.0);
            }
            else {
              poti.setValue(m,originalValues[m+howmany]);
            }
          }
        }
        else {
          for (int m=0; m<poti.getSize(); m++) {
            poti.setValue(m,originalValues[m+howmany]);
          }
        }
      }
      if (addedState) {
        if (poti.getVariables().size() == 1) {
          howmany = howmany + ((FiniteStates) child).getNumStates()-1;
        }
        else {
          if ((columnAndRow) && (father.getKindOfNode() != Node.DECISION)) {
            howmany = howmany + (((FiniteStates) child).getNumStates()-1)*
                      (((FiniteStates) poti.getVariables().elementAt(1)).getNumStates()-1);
          }
          else {
            howmany = howmany + (((FiniteStates) child).getNumStates()-1)*
                      ((FiniteStates) poti.getVariables().elementAt(1)).getNumStates();
          }
        }
      }
      else {
        if (poti.getVariables().size() == 1) {
          howmany = howmany + ((FiniteStates) child).getNumStates();
        }
        else {
          if ((columnAndRow) && (father.getName().equals(((Node) poti.getVariables().elementAt(1)).getName())) &&
              (father.getKindOfNode() != Node.DECISION)) {
            howmany = howmany + (((FiniteStates) child).getNumStates())*
                      (((FiniteStates) poti.getVariables().elementAt(1)).getNumStates()-1);
          }
          else {
            howmany = howmany + (((FiniteStates) child).getNumStates())*
                      ((FiniteStates) poti.getVariables().elementAt(1)).getNumStates();
          }
        }
      }
      ((CanonicalPotential) theRelation.getValues()).setArgumentAt(poti,o);
      Vector rl = getRelationList();
      Relation r = new Relation();
      for (int h=0; h<rl.size(); h++) {
        if (!((Relation) rl.elementAt(h)).getActive()) {
          if (poti.getVariables().size() > 1) {
            if (((Relation) rl.elementAt(h)).getVariables().size() > 1) {
              if (((Node) ((Relation) rl.elementAt(h)).getVariables().elementAt(1)).getName().equals(((Node) poti.getVariables().elementAt(1)).getName())) {
                r = (Relation) rl.elementAt(h);
                break;
              }
            }
          }
          else {
            if (((Relation) rl.elementAt(h)).getVariables().size() == 1) {
              r = (Relation) rl.elementAt(h);
              break;
            }
          }
        }
      }
      r.setValues(poti);
    }
    //theRelation.print();
    double[][] theInitialValues = new double[(int) child.getChildrenNodes().size()][];
    for (int k=0; k<child.getChildrenNodes().size(); k++) {
      if (((Node) child.getChildrenNodes().elementAt(k)).getKindOfNode() != Node.DECISION) {
        boolean toAdd = true;
        theInitialValues[k] = new double[(int) getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues().getSize()];
        if (getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues().getClass() == PotentialTable.class) {
          theInitialValues[k] = ((PotentialTable) getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues()).getValues();
          if (((Node) child.getChildrenNodes().elementAt(k)).getKindOfNode() == Node.CHANCE) {
            Vector theStates = ((FiniteStates) child.getChildrenNodes().elementAt(k)).getStates();
            for (int l=0; l<theStates.size(); l++) {
              if (theStates.elementAt(l).equals("\"VariableDoesntMakeSense\"")) {
                toAdd = false;
              }
            }
            if (toAdd) {
              theStates.addElement("\"VariableDoesntMakeSense\"");
              ((FiniteStates) child.getChildrenNodes().elementAt(k)).setStates(theStates);
            }
          }
          else if (((Node) child.getChildrenNodes().elementAt(k)).getKindOfNode() == Node.UTILITY) {
            toAdd = addedState;
          }
        }
        else if (getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues().getClass() == CanonicalPotential.class) {
          // TO DO
          Vector adding = new Vector();
          for (int m=0; m<((CanonicalPotential) getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues()).getArguments().size(); m++) {
            for (int n=0; n<((PotentialTable) ((CanonicalPotential) getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues()).getArgumentAt(m)).getSize(); n++) {
              adding.addElement(new Double(((PotentialTable) ((CanonicalPotential) getRelation((Node) child.getChildrenNodes().elementAt(k)).getValues()).getArgumentAt(m)).getValue(n)));
            }
          }
          theInitialValues[k] = new double[(int) adding.size()];
          for (int m=0; m<adding.size(); m++) {
            theInitialValues[k][m] = ((Double) adding.elementAt(m)).doubleValue();
          }
          if (((Node) child.getChildrenNodes().elementAt(k)).getKindOfNode() == Node.CHANCE) {
            Vector theStates = ((FiniteStates) child.getChildrenNodes().elementAt(k)).getStates();
            for (int l=0; l<theStates.size(); l++) {
              if (theStates.elementAt(l).equals("\"VariableDoesntMakeSense\"")) {
                toAdd = false;
              }
            }
            if (toAdd) {
              theStates.addElement("\"VariableDoesntMakeSense\"");
              ((FiniteStates) child.getChildrenNodes().elementAt(k)).setStates(theStates);
            }
          }
        }
        fillInChanceValues(child,(Node) child.getChildrenNodes().elementAt(k),"\"VariableDoesntMakeSense\"",
                           theInitialValues[k],getRelation((Node) child.getChildrenNodes().elementAt(k)),
                           false,toAdd);
      }
    }
  }
}


/**
 * Checks the presence of cycles.
 * @return <code>true</code> if there is a cycle, or <code>false</code>
 * in other case.
 */

public boolean hasCycles() {
  Graph  g = duplicate();
  return (!(g.isADag()));
}


/**
 * To find a decision node without parents.
 * @return the decision node found.
 */

private Node firstDecision() {
  NodeList list, parents;
  Node node = null, first = null;
  int i , j;

  list = getNodeList();

  for (i=0 ; i < list.size() ; i++) {
    node = list.elementAt(i);

    if (node.getKindOfNode() == node.DECISION) {
      if (node.hasDecisionParent() == false) {
        first=node;
        break;
      }
    }
  }

  return first;
}





/**
 * To get the number of decisions in the diagram.
 * @return the number of decisions.
 */

public int numberOfDecisions() {
  int number = 0, i;
  NodeList nodes;
  Node node;

  nodes = getNodeList();
  for (i=0 ; i < nodes.size() ; i++) {
    node = nodes.elementAt(i);
    if (node.getKindOfNode() == node.DECISION)
      number++;
  }

  // Return the number of decisions

  return number;
}

/**
 * Gets the number of chance nodes present in the ID
 */
public int numberOfChanceNodes(){
  int number = 0, i;
  NodeList nodes;
  Node node;

  nodes = getNodeList();
  for (i=0 ; i < nodes.size() ; i++) {
    node = nodes.elementAt(i);
    if (node.getKindOfNode() == node.CHANCE)
      number++;
  }

  // Return the number of decisions

  return number;
} 

/**
 * Gets the number of links between the variables of the ID
 */
public int numberOfLinks(){
  LinkList links=getLinkList();
  return links.size();
}

/**
 * Gets the number of value nodes
 */
public int numberOfValueNodes(){
  NodeList valueNodes=getValueNodes();
  return valueNodes.size();
}

/**
 * Gets the min and max number of states for the nodes
 */
public Vector getMinAndMaxNumberOfStates(){
  NodeList nodes;
  Node node;
  Vector result=new Vector();
  int min=1000,max=0,numStates,i,j;

  nodes = getNodeList();
  node=nodes.elementAt(0);

  // Find the first node of chance or decision type
  for(i=0; i < nodes.size(); i++){
    if (node.getKindOfNode() != Node.UTILITY){
      min=((FiniteStates)node).getNumStates();
      max=min;
    }
  }
  
  // Once the max and min values are initialized, consider
  // the rest of them
  for (i=0 ; i < nodes.size() ; i++) {
    node = nodes.elementAt(i);
    if (node.getKindOfNode() != Node.UTILITY){
      numStates=((FiniteStates)node).getNumStates();
      if (numStates > max)
        max=numStates;
      if (numStates < min)
        min=numStates;
    }
  }

  // Return the vector with both values
  result.addElement(new Integer(min));
  result.addElement(new Integer(max));
  return result;
}


/**
 * To get the list of decision nodes of the diagram.
 * @return the list of decisions.
 */

public NodeList getDecisionList() {
  int clasified = 0;
  int i,j;
  NodeList decisionsList, nodes;
  Vector ancestors;
  Node node, parent;
  boolean insert;

  decisionsList = new NodeList();

  while (clasified < numberOfDecisions()) {

    nodes = getNodeList();

    for (i=0 ; i < nodes.size() ; i++) {

      node = nodes.elementAt(i);

      if (node.getKindOfNode() == node.DECISION) {

        if (decisionsList.getId(node) == -1) {

          // Obtain the parents of the node

          insert = true;
          ancestors = ascendants(node);
          for (j=0 ; j < ancestors.size() ; j++) {

             parent = (Node)ancestors.elementAt(j);

             if (parent.getKindOfNode() == node.DECISION) {

               // Check if this parent belongs to decisionList

               if (decisionsList.getId(parent) == -1) {

                insert = false;
                break;
               }
             }
          }

          // if insert == true, the parent is inserted in
          // decisionsList, so must be inserted too

          if (insert == true) {
            decisionsList.insertNode(node);
            clasified++;
          }
        }
      }
    }
  }

  // Return the list of decision nodes

  return decisionsList;
}

/**
 * To get the list of chance nodes without decisions as sucessors
 */

public NodeList getChanceNodesWithoutDecisionsAsSucessors(){
  NodeList nodesOfInterest=new NodeList();
  NodeList allNodes=getNodeList();
  Node considered;
  boolean has;
  int i;

  for(i=0; i < allNodes.size(); i++){
    considered=(Node)allNodes.elementAt(i);
    if (considered.getKindOfNode() == Node.CHANCE){
          has=considered.hasDirectDecisionChild();
          if (has == false){
            nodesOfInterest.insertNode(considered);
          }
    }
  }

  // Return the list of nodes

  return nodesOfInterest;
}

/**
 * Method to get the list of nodes with sucessors in a
 * given set of nodes
 */

public NodeList getChanceNodesWithSucessorsInSet(NodeList sucessors){
  NodeList nodesOfInterest=new NodeList();
  NodeList allNodes=getNodeList();
  NodeList sucessorsOfNode;
  NodeList sucessorsLocal=sucessors.copy();
  Node considered;
  Node sucessor;
  boolean toAdd,added=true;
  int i,j;

  while(added == true){
    added=false;
    for(i=0; i < allNodes.size(); i++){
      considered=(Node)allNodes.elementAt(i);
      // Consider if this node is not in  sucessors

      if (considered.getKindOfNode() == Node.CHANCE && sucessors.getId(considered) == -1){
        // Look for its own sucessors

        sucessorsOfNode=children(considered);

        // Go on this list

        toAdd=true;
        for(j=0; j < sucessorsOfNode.size(); j++){
          sucessor=(Node)sucessorsOfNode.elementAt(j);

          if (sucessor.getKindOfNode() == Node.DECISION &&
              sucessorsLocal.getId(sucessor) == -1){
            toAdd=false;
            break;
          }
        }

        // Add it if needed

        if (toAdd == true){

          // Insert the node if it is not already inserted

          if (nodesOfInterest.getId(considered) == -1){
             nodesOfInterest.insertNode(considered);
             sucessorsLocal.insertNode(considered);
             added=true;
             break;
          }
        }
      }
    }
  }

  // Return the list of nodes

  return nodesOfInterest;
}

/**
 * Method to give an instantiation order of the nodes
 */

public Vector giveInstantiationOrder(Graph graph) {
  Vector order;
  NodeList relevant=graph.getNodeList();
  Node node;
  int i;

   // Now we have to get the marginal nodes from relevant

   order=getMarginalsNames(graph);

   // Return the vector

   return(order);
}

/*
 * Auxiliar method for giveInstantiationOrder
 */

private Vector getMarginalsNames(Graph graph) {
  NodeList nodes;
  Node node;
  Vector order=new Vector();
  Vector names;
  int i;

    nodes=graph.getNodeList();

    while(nodes.size() != 0) {

      names=new Vector();

      // Look for root nodes in this graph

      for(i=0; i < nodes.size(); i++) {
          if (nodes.elementAt(i).hasParentNodes() == false) {
              // Add the name to the vector of names
              names.addElement((nodes.elementAt(i)).getName());
          }
      }

      // Now, all of these names must be stored

      order.addElement(names);

      // Remove the nodes

      for(i=0; i < names.size(); i++) {
          node=nodes.getNode((String)names.elementAt(i));

          // Remove this node

          try {
               graph.removeNode(node);
          } catch (InvalidEditException e) {
            System.out.println("Error in Method getMarginalNames, class IDiagram");
            System.out.println("Error when trying to remove nodes.....");
            System.exit(0);
          }
      }

      // Get again the list of nodes

      nodes=graph.getNodeList();
    }

    // Return order

    return(order);
}

/**
 * To check the instantiation order given by the vector
 * passed as argument
 * @param order Vector with the order of instantiation
 * @return boolean
 */

public boolean checkInstantiationOrder(Vector order) {
   int i,j;
   Node node;
   Vector fase;
   IDiagram diagCopy=qualitativeCopy();

   // For each node, check if it is marginal (no parents)
   // and eliminate it

   for(i=0; i < order.size(); i++) {
      fase=(Vector)order.elementAt(i);
      for(j=0; j < fase.size(); j++) {
         // Get the node with this name

         node=diagCopy.getNode((String)(fase.elementAt(j)));

         // Check if marginal

         if (node.hasParentNodes() == false) {
            // The node can be deleted

            diagCopy.removeNodeOnly(node);
         }
         else {
            // Set flag to false

            return(false);
         }
      }
   }

   // Return flag

   return(true);
}

/**
 * To add arcs to the diagram.
 * @param1 the set of tails (origin).
 * @param2 the head of the new arcs (destination).
 */

public void addLinks(NodeList tails, Node head) {
  int i;
  Link link;

  for (i=0 ; i < tails.size(); i++) {
    link = getLink(tails.elementAt(i),head);
    if (link == null && tails.elementAt(i) != head) {
      try{
        createLink(tails.elementAt(i),head,true);
      } catch (InvalidEditException iee){;}
    }
  }
}

/**
 * To add an arc to the diagram.
 * @param1 the tail of the arc  (origin).
 * @param2 the head of the new arc (destination).
 */

private void addLink(Node tail, Node head) {
  Link link;

    link = getLink(tail,head);
    if (link == null && tail != head) {
      try{
        createLink(tail,head,true);
      } catch (InvalidEditException iee){;}
    }
}

/**
 * To get the barren nodes.
 * @return a barren node.
 */

protected Node getBarrenNode() {
  NodeList listOfNodes, descendants, parents;
  NodeList childrens;
  NodeList considered;
  Node node;
  Node descendant;
  boolean path;
  boolean onlyDecisions;
  int i,j;

  listOfNodes = getNodeList();
  for (i=0 ; i < listOfNodes.size() ; i++) {
    node = listOfNodes.elementAt(i);

    // Look if the node has path to a value node

    if (node.getKindOfNode() != Node.UTILITY){
      considered=new NodeList();
      path=node.withPathToValueNode(considered);

      if (path == false)
        return node;
    }
    else{
      // To be a barren node, must have 0 descendants
      descendants=descendantsList(node);
      parents=parents(node);
      if (descendants.size() == 0 && parents.size() == 0)
        return node;
    }
  }
  return((Node)null);
}


/**
 * To access the value node.
 * @return a value node or <code>null</code>.
 */

public Node getValueNode() {
  NodeList listOfNodes;
  Node node;
  int i;

  listOfNodes = getNodeList();
  for (i=0 ; i < listOfNodes.size() ; i++) {
    node = listOfNodes.elementAt(i);
    if (node.getKindOfNode() == node.UTILITY)
      return node;
  }
  return((Node)null);
}


/**
 * This method assumes (in this moment) that there is a unique utility node.
 * Having several utility nodes without children will be considered in the future.
 * @return The table of utilities that the value node represents
 */
public Potential getPotentialOfGlobalUtility(){
	Relation relationValue;
	Node value;
	Potential potValue;
	
	value = getValueNode();
	relationValue=getRelation(value);
	potValue=relationValue.getValues();
	return (Potential)potValue;
}


/**
 * To access a node of the diagram given a name
 * @param <code>String</code> Name
 * @return <code>Node</code> Node or null
 */

public Node getNode(String name) {
  NodeList nodes;
  Node node;
  int id;

  nodes=getNodeList();

  // Look for the node

  id=nodes.getId(name);

  // If belong to the list, retrieve the node

  if (id != -1)
    node=nodes.elementAt(id);
  else
    node=null;

  // Return node

  return(node);
}


/**
 * To evaluate the problem size.
 * @return the number of values to store: both  probabilities
 *         and utilities.
 */

public double getProblemSize() {
  NodeList listOfNodes, parents;
  Node node;
  double totalSize = 0, size = 1;
  int kind, type, i;

  listOfNodes = getNodeList();
  for (i=0 ; i < listOfNodes.size() ; i++) {
    node = listOfNodes.elementAt(i);
    kind = node.getKindOfNode();
    parents = parents(node);
    size = 0;
    switch(kind) {
    // UTILITY
    case 2: size = parents.getSize();
      break;
    // CHANCE
    case 0: type = node.getTypeOfVariable();
      if (type == node.FINITE_STATES) {
        size = (((FiniteStates)node).getNumStates())*(parents.getSize());
      }
      else
        size = parents.getSize();
      break;
    }
    totalSize += size;
  }
  return totalSize;
}

/**
 * Method to calculate the size of the whole set of potentials
 * @return the size of the actual set of potentials associated to
 *         the relations
 */

public double calculateSizeOfPotentials(){
  RelationList relations=new RelationList();
  Vector rels;
  int i;

  rels=getRelationList();
  for(i=0; i < rels.size(); i++){
    relations.insertRelation((Relation)rels.elementAt(i));
  }

  // Once inserted, sum their sizes and return this value
  return(relations.sumSizes());
}

/**
 *  Prints the nodes, links, etc, to the standard output.
 */

public void print() {
  NodeList nodes;
  LinkList listOfLinks;
  Relation rel;
  Node node;
  Link link;
  int i, j;

  nodes = getNodeList();

  for (i=0 ; i < nodes.size() ; i++) {
    System.out.print("*********************************************\n");
    node = nodes.elementAt(i);
    node.print();
    listOfLinks = node.getParents();
    for (j=0 ; j < listOfLinks.size() ; j++) {
      link = listOfLinks.elementAt(j);
      System.out.print("PARENT(" + j + ") = "+ (link.getTail()).getName() + "\n");
    }
    listOfLinks = node.getChildren();
    for (j=0 ; j < listOfLinks.size() ; j++) {
      link = listOfLinks.elementAt(j);
      System.out.print("CHILD(" + j + ") = "+ (link.getHead()).getName() + "\n");
    }

    if (node.getKindOfNode() == node.CHANCE || node.getKindOfNode() == node.UTILITY) {
      rel=null;
      rel=getRelation(node);
      if (rel != null){
        System.out.print("--------------------------------------------\n");
        rel.print();
        System.out.print("--------------------------------------------\n");
      }
    }
  }
}


/**
 * Copies this diagram.
 * @return a copy of this diagram.
 */

public IDiagram copy () {
  IDiagram id = new IDiagram();
  Graph g = duplicate();
  Enumeration e;
  Vector rl = new Vector();
  Vector vars;
  NodeList varsInOriginal;
  Relation rNew;
  Potential pt;
  Potential ptOriginal;
  Node node;

  id.setNodeList(g.getNodeList());
  id.setLinkList(g.getLinkList());
  for (e = getRelationList().elements() ; e.hasMoreElements() ; ) {
    Relation r = (Relation) e.nextElement();

    // Get the variables of this relation, and get references
    // to these variables, but for the new list of nodes

    varsInOriginal=r.getVariables();
    vars=new Vector();

    for(int i=0; i < varsInOriginal.size(); i++){
      node=id.nodeList.getNode(((Node)varsInOriginal.elementAt(i)).getName());
      // This node is inserted in vars

      vars.addElement(node);
    }

    // Create the new relation. Use copy method to initialize
    // all data fields, but now change variables in the same
    // relation and in its potential

    rNew=r.copy();

    // Set this potential to the new relation

    rl.add(rNew);
  }
  id.setRelationList(rl);
  return id;
}

/**
 * Copies this diagram, only qualitative information
 * @return a qualitative copy of this disgram.
 */

public IDiagram qualitativeCopy () {
  IDiagram id = new IDiagram();
  Graph g = duplicate();
  Enumeration e;

  id.setNodeList(g.getNodeList());
  id.setLinkList(g.getLinkList());
  id.setTitle(this.getName());
  id.setName(this.getName());

  // Consider the set of relations, to add constraint relations

  Vector relations=getRelationList();

  for(int i=0; i < relations.size(); i++){
     Relation rel=(Relation)relations.elementAt(i);

     if (rel.getKind() == Relation.CONSTRAINT)
        id.addRelation(rel);
  }
  
  return id;
}

/**
 * Copy this diagram with relations, but not the values
 * of the relations
 */

public IDiagram qualitativeCopyWithRelations() {
  IDiagram id = new IDiagram();
  Graph g = duplicate();
  Enumeration e;
  Vector rl = new Vector();
  Vector vars;
  NodeList varsInOriginal;
  Relation rNew;
  Potential pt;
  Potential ptOriginal;
  Node node;

  id.setNodeList(g.getNodeList());
  id.setLinkList(g.getLinkList());
  for (e = getRelationList().elements() ; e.hasMoreElements() ; ) {
    Relation r = (Relation) e.nextElement();

    // Get the variables of this relation, and get references
    // to these variables, but for the new list of nodes

    varsInOriginal=r.getVariables();
    vars=new Vector();

    for(int i=0; i < varsInOriginal.size(); i++){
      node=id.nodeList.getNode(((Node)varsInOriginal.elementAt(i)).getName());
      // This node is inserted in vars

      vars.addElement(node);
    }

    // Create the new relation. Use copy method to initialize
    // all data fields, but now change variables in the same
    // relation and in its potential

    rNew=r.copy();
    rNew.setVariables(vars);
    rNew.setValues(null);
    rl.add(rNew);
  }
  id.setRelationList(rl);
  return id;
}


/**
 * Compiles this diagram (evaluating without evidences).
 */

public void compile(int index, Vector parameters) {
    
    IDiagram iDCopy;
        
    iDCopy=copy();
        
    switch (index) {

    /* Variable Elimination */

    case 0: Network bVE=iDCopy; // Need to copy: barren nodes are removed
	    Evidence eVE;
	    VariableElimination ve;

	    eVE = new Evidence();
	    ve = new VariableElimination((Bnet)bVE,eVE);

	    // There are not interest variables
	    //ve.obtainInterest();

	    // Propagate

	    ve.propagate();

	    // To present the results

	    showResults(ve);

	    break;

    /* Variable Elimination, Potential Trees */

    case 1: Network bVEPT=iDCopy; // Need to copy: barren nodes removed
	    Evidence eVEPT;
	    VEWithPotentialTree vePT;

	    eVEPT = new Evidence();
	    vePT = new VEWithPotentialTree((Bnet)bVEPT,eVEPT);

	    // There are not interest variables
	    //vePT.obtainInterest();

	    // Set the threshold for prunning

	    vePT.setThresholdForPrunning(getFloatElement(parameters, 0)); /* TO SET */
	    //vePT.setThresholdForPrunning(0.0);

	    // Propagate

	    vePT.propagate();

	    // Present the results

	    showResults(vePT);

	    break;

    /* Variable Elimination, Potential Trees and Constraints */

    case 2: Network bVEPTC=iDCopy; // Need to copy: barren nodes removed
	    Evidence eVEPTC;
	    IDVEWPTAndConstraints vePTC;

	    eVEPTC = new Evidence();
	    vePTC = new IDVEWPTAndConstraints((Bnet)bVEPTC,eVEPTC);

	    // No interest variables
	    // vePTC.obtainInterest();

	    // Store the thresholdForPrunning

	    vePTC.setThresholdForPrunning(getFloatElement(parameters, 0));/* TO SET */
	    //vePTC.setThresholdForPrunning(0.0);

	    // Propagate

	    vePTC.propagate();

	    // Present the results

	    showResults(vePTC);

	    break;

    /* Arc Reversal */

    case 3: ArcReversal eval;
	    boolean evaluable;
	    IDiagram id = iDCopy;
	    eval = new ArcReversal(id);

	    // It's necessary to copy, otherwise the real nodes are erased

	    // Initial tests about the node.

	    evaluable = eval.initialConditions();

	    // If the diagram is evaluable, do it.

	    if (evaluable == true) {
	      eval.evaluateDiagram();

	      // Make the results be accessible

	      showResults(eval);
	    }

	    break;

    /* Arc Reversal, Potential Tress */

    case 4: ARWithPotentialTree evalPT;
	    boolean evaluablePT;
	    IDiagram idPT = iDCopy;
	    evalPT = new ARWithPotentialTree(idPT);

	    // It's necessary to copy, otherwise the real nodes are erased


	    // initial chekout about the node

	    evaluablePT = evalPT.initialConditions();

	    // Set the threshold for prunning operations

	    evalPT.setThresholdForPrunning(getFloatElement(parameters,0));/* TO SET */
	    //evalPT.setThresholdForPrunning(0.0);

	    // If the diagram is suitable to be evaluated, then do it.

	    if (evaluablePT == true) {
	      evalPT.evaluateDiagram();

	      // Let results accessible

	      showResults(evalPT);
	    }

	    break;

    /* Arc Reversal, Potential Trees and Constraints */

    case 5: ARWPTAndConstraints evalPTC;
	    boolean evaluablePTC;
	    IDiagram idPTC = iDCopy;
	    evalPTC = new ARWPTAndConstraints(idPTC);

	    // It's necessary to copy, otherwise the real nodes are erased


	    // initial chekout about the node.

	    evaluablePTC = evalPTC.initialConditions();

	    // Set the threshold for prunning operations

	    evalPTC.setThresholdForPrunning(getFloatElement(parameters,0));/*TO SET*/
	    //evalPTC.setThresholdForPrunning(0.0);

	    // If the diagram is suitable to be evaluated, then do it.

	    if (evaluablePTC == true) {
	      evalPTC.evaluateDiagram();

	      // Let results be accessible

	      showResults(evalPTC);
	    }

	    break;
}
}
/**
 * Method to get the relation of a node, but taking into account the
 * possible existence of constraints on it. So will be returned a
 * relation of another kind
 * @param <code>Node</code> the node to llok for its relation
 */

public Relation getRelation(Node node){
  Relation rel=null;
  Vector relations;
  String mainVarName;
  Potential pot = new PotentialTable();
  int i;

  // Look for the relation. If the finded relation is of
  // constraint kind, keep on looking

  relations=getRelationList();

  // Go over the relations

  for(i=0; i < relations.size(); i++){
    rel=(Relation)relations.elementAt(i);

    // If it is a constraint relation, forget it and go on

    if (rel.getKind() != Relation.CONSTRAINT){
       mainVarName=((Node)(rel.getVariables().elementAt(0))).getName();
       if (mainVarName.equals(node.getName())){
          return rel;
       }
    }
  }

  // To come here means the relation was not found

  if (node.getKindOfNode() != Node.DECISION) {
    System.out.println("The relation for node: "+node.getName()+ " was not found");
    System.exit(-1);
  }
  return(rel);
}

/**
 * To check the presence of constraints related to this
 * realtion
 * @param <code>Relation</code> the relation to consider
 */
public boolean applyConstraintsOnRelation(Relation rel){
 LogicalExpression logexp;
 Relation relation;
 NodeList common;
 PotentialTree potTree;
 PotentialTree constraint;
 boolean result=false;
 boolean combined=false;
 int i;

  //Retrieve the potential tree of the relation
  potTree=(PotentialTree)(rel.getValues());

  // First at all, go over the list of relations and
  // consider the constraints

  for(i=0; i < getRelationList().size(); i++){
    relation=(Relation)getRelationList().elementAt(i);

    // Check if it is a constraint

    if (relation.getKind() == Relation.CONSTRAINT){

       // First, check if both relations share some variables
       common=relation.getVariables().intersection(rel.getVariables());

       // If common is not empty, we have to check if this set
       // of variables is enough for the constraint to be applicable

       if (common.size() != 0){
          logexp=(LogicalExpression)(relation.getValues());

          // Test if applicable

          result=logexp.check(common.toVector());

          // If is applicable, we have to combine the potentials
          // of both relations

          if (result == true){

             // Test if the logical expression is evaluated

             if (logexp.getResult() == null){
                // Evaluate the constraint
                logexp.evaluate();
             }

             // Anyway, combine the potentials

             constraint=logexp.getResult();
             constraint=(PotentialTree)constraint.maxMarginalizePotential(common.toVector());

             //potTree.limitBound(0L);
             potTree=(PotentialTree)potTree.combine(constraint);
             combined=true;
          }
       }
    }
  }

  // If it is a probability relation, will have to normalize
  // Only if it is not an unity potential and there were any
  // combination operation

  if (combined == true && potTree.getVariables().size() != 0 && rel.getKind() != Relation.UTILITY){
      normalize(rel.getVariables().elementAt(0),potTree);
  }

  // Impose this potential to the relation

  rel.setValues(potTree);

  // Return the value stored in RESULT

  return result;
}

/**
 * To check the presence of constraints related to a relation
 * The main node to be considered is passed as argument
 * @param <code>Relation</code> the relation to consider
 * @param <code>Node</code> main node to consider
 */
public boolean applyConstraintsOnRelation(Relation rel, Node node){
 LogicalExpression logexp;
 Relation relation;
 NodeList common;
 PotentialTree potTree;
 PotentialTree constraint;
 boolean result=false;
 boolean combined=false;
 int i;

  //Retrieve the potential tree of the relation

  potTree=(PotentialTree)(rel.getValues());

  // First at all, go over the list of relations and
  // consider the constraints

  for(i=0; i < getRelationList().size(); i++){
    relation=(Relation)getRelationList().elementAt(i);

    // Check if it is a constraint

    if (relation.getKind() == Relation.CONSTRAINT){
       // First, check if both relations share some variables

       common=relation.getVariables().intersection(rel.getVariables());

       // If common is not empty, we have to check if this set
       // of variables is enough for the constraint to be applicable

       if (common.size() != 0){
          logexp=(LogicalExpression)(relation.getValues());

          // Test if applicable

          result=logexp.check(common.toVector());

          // If is applicable, we have to combine the potentials
          // of both relations

          if (result == true){

             // Test if the logical expression is evaluated

             if (logexp.getResult() == null){
                // Evaluate the constraint

                logexp.evaluate();
             }

             // Anyway, combine the potentials

             constraint=logexp.getResult();
             constraint=(PotentialTree)constraint.maxMarginalizePotential(common.toVector());
             //potTree.limitBound(0L);
             potTree=(PotentialTree)potTree.combine(constraint);
             combined=true;

             //potTree=(PotentialTree)potTree.maxMarginalizePotential(rel.getVariables().getNodes());
          }
       }
    }
  }

  // If it is a probability relation, will have to normalize
  // Only apply constraints if there were any combination
  // and the potential is not an unity potential

  if (combined == true && potTree.getVariables().size() != 0 && rel.getKind() != Relation.UTILITY){
     normalize(node,potTree);
  }

  // Impose this potential to the relation

  rel.setValues(potTree);

  // Return the value stored in RESULT

  return result;
}

/**
 * To check the presence of constraints related to this
 * PotentialTree
 * @param <code>PotentialTree</code> the potential to consider
 * @param <code>boolean</code> flag to set if it is an utility potential
 */

public PotentialTree applyConstraintsOnPotential(PotentialTree pot, boolean utility){
 LogicalExpression logexp;
 Relation relation;
 NodeList common;
 NodeList varsInPot;
 PotentialTree constraint;
 PotentialTree finalTree=null;
 boolean result=false;
 boolean combined=false;
 int i;

  // Initialize final with pot
  finalTree=pot;
  
  // First at all, go over the list of relations and
  // consider the constraints

  for(i=0; i < getRelationList().size(); i++){
    relation=(Relation)getRelationList().elementAt(i);

    // Check if it is a constraint

    if (relation.getKind() == Relation.CONSTRAINT){

       // First, check if both relations share some variables

       varsInPot=new NodeList(pot.getVariables());
       common=relation.getVariables().intersection(varsInPot);

       // If common is not empty, we have to check if this set
       // of variables is enough for the constraint to be applicable

       if (common.size() != 0){
          logexp=(LogicalExpression)(relation.getValues());

          // Test if applicable

          result=logexp.check(common.toVector());

          // If is applicable, we have to combine the potentials
          // of both relations

          if (result == true){

             // Test if the logical expression is evaluated

             if (logexp.getResult() == null){
                // Evaluate the constraint
                logexp.evaluate();
             }

             // Anyway, combine the potentials

             constraint=logexp.getResult();
             constraint=(PotentialTree)constraint.maxMarginalizePotential(common.toVector());

             finalTree=(PotentialTree)finalTree.combine(constraint);
             combined=true;
          }
       }
    }
  }

  // If it is a probability relation, will have to normalize
  // Normalize when constraints have been applied, the potential is
  // related to a probability and its not an unity potential

  if (combined == true && pot.getVariables().size() != 0 && utility != true){
      normalize((Node)pot.getVariables().elementAt(0),finalTree);
  }

  return(finalTree);
}

/**
 * Checks if a given potential is a conditional probability distribution
 * for a given variable
 * @param conditioned Node acting as a conditioned variable
 * @param potential potential to test
 * @return boolean value
 */
public boolean isConditionalOrMarginalPotential(Node conditioned, Potential potential){
  Vector varsInPot=potential.getVariables();
  boolean found=false;
  int indexInPotential=0;
  
  // First at all, check if conditioned is a variable in the potential
  for(int i=0; i < varsInPot.size(); i++){
     Node var=(Node)varsInPot.elementAt(i);

     // Check if the variable in the i-th position is the conditioned variable
     if (var.getName().equals(conditioned.getName())){
        indexInPotential=i;
        found=true;
        break;
     }
  }

  // If found is false, return false
  if (!found)
   return false;

  // Anyother way, test if the rest of vars in the potential are parents of
  // conditioned. And there will not be any other variable in this situation
  for(int i=0; i < varsInPot.size(); i++){
     // Consider the rest of variables
     if (i != indexInPotential){
        Node var=(Node)varsInPot.elementAt(i);

        // Check if var is parent of conditioned
        if (var.isParentOf(conditioned) == false)
           return false;
     }
  }
    
  // Return true
  return true;
}

/**
 * Method to prepare the normalization of the potential
 * linked to a relation
 * @param  <code>String</code> main node of the relation
 * @param <code>PotentialTree</code> Potential to be normalized
 */
public void normalize(Node node, PotentialTree pot){
  NodeList conditioning;
  PotentialTree result;

    // First at all, get a list of antecessors of node

    conditioning=parents(node);

    // Use this list to call the method to repair

    pot.repair(conditioning);

    // Visualize the result

//    pot.print();
}

/**
 * Method to get the list of utility nodes of the diagram
 */

public NodeList getValueNodes(){
 NodeList utils=new NodeList();
 NodeList allNodes=getNodeList();
 Node node;
 int i;

  for(i=0; i < allNodes.size(); i++){
    node=allNodes.elementAt(i);
    if (node.getKindOfNode() == Node.UTILITY)
      utils.insertNode(node);
  }

  return utils;
}


  /**
* This function is used to know whether the link that we want to add,
* and whose head is 'child' and whose tail is 'father'
* is compatible with the structure of influence diagrams
* @param father the tail of the link that we want to add.
* @param child the head of the link that we want to add.
* @return true iff the link is compatible with the structure of influence diagram
*/
public static boolean isCompatibleLink(Node father,Node child)
{
	 int kindFather;

	 kindFather=father.getKindOfNode();
         return ((kindFather==Node.CHANCE)||(kindFather==Node.DECISION));
}

public void positionNodes(){
	
	NodeList auxChildren;
	NodeList nodesPreviousLevel;
	NodeList nodesActualLevel;
	Vector listOfLevels=new Vector();
	int level;
	Node auxNode;
	int i;
	
	
	NodeList remainingChanceAndDecNodes=getNodeList().copy();
	NodeList utils=getNodesOfKind(Node.UTILITY);
	NodeList svs=getNodesOfKind(Node.SUPER_VALUE);
	NodeList utilsAndSV=utils.copy();
	utilsAndSV.merge(svs);
	
	remainingChanceAndDecNodes=remainingChanceAndDecNodes.difference(utilsAndSV);
			
	//Compute Level 0 of Chance and Decision nodes
	listOfLevels.add(new NodeList());
	for (i=0;i<remainingChanceAndDecNodes.size();i++){
		auxNode=remainingChanceAndDecNodes.elementAt(i);
		if (auxNode.getParents().size()==0){
			((NodeList)(listOfLevels.elementAt(0))).insertNode(auxNode);
		}
		
	}
	
	//Remove the nodes of the level 0 from the list of remaining nodes
	for (i=0;i<((NodeList)(listOfLevels.elementAt(0))).size();i++){
		remainingChanceAndDecNodes.removeNode(((NodeList)(listOfLevels.elementAt(0))).elementAt(i));
	}
	
	
	//Compute the rest of levels
	while (remainingChanceAndDecNodes.size()>0){
		listOfLevels.add(new NodeList());
		level=listOfLevels.size();
		nodesPreviousLevel=(NodeList)(listOfLevels.elementAt(level-2));
		nodesActualLevel=(NodeList)(listOfLevels.elementAt(level-1));
		
		//The children of the nodes of the previous level are going to be the nodes of the actual level	
		for (i=0;i<nodesPreviousLevel.size();i++){
			auxChildren=nodesPreviousLevel.elementAt(i).getChildrenNodes();
			for (int j=0;j<auxChildren.size();j++){
				Node iChild=auxChildren.elementAt(j);
				if ((iChild.getKindOfNode()!=Node.UTILITY)&&(iChild.getKindOfNode()!=Node.SUPER_VALUE)){
				if (iChild.getParentNodes().intersection(remainingChanceAndDecNodes).size()==0){
					if (nodesActualLevel.getId(iChild)==-1){
						nodesActualLevel.insertNode(iChild);
					}
				}
				}
			}
		}
		
		//Remove the nodes of the actual level from the list of remaining nodes
		for (i=0;i<nodesActualLevel.size();i++){
			remainingChanceAndDecNodes.removeNode(nodesActualLevel.elementAt(i));
		}
	}
	
//	Compute Level of Utility nodes
	listOfLevels.add(new NodeList());
	nodesActualLevel=(NodeList)(listOfLevels.elementAt(listOfLevels.size()-1));
	for (i=0;i<remainingChanceAndDecNodes.size();i++){
		auxNode=utils.elementAt(i);
		nodesActualLevel.insertNode(auxNode);
	}
		
//	Compute the rest of levels
	while (svs.size()>0){
		listOfLevels.add(new NodeList());
		level=listOfLevels.size();
		nodesPreviousLevel=(NodeList)(listOfLevels.elementAt(level-2));
		nodesActualLevel=(NodeList)(listOfLevels.elementAt(level-1));
		
		//The children of the nodes of the previous level are going to be the nodes of the actual level	
		for (i=0;i<nodesPreviousLevel.size();i++){
			auxChildren=nodesPreviousLevel.elementAt(i).getChildrenNodes();
			for (int j=0;j<auxChildren.size();j++){
				Node iChild=auxChildren.elementAt(j);
				if (iChild.getParentNodes().intersection(remainingChanceAndDecNodes).size()==0){
					if (nodesActualLevel.getId(iChild)==-1){
						nodesActualLevel.insertNode(iChild);
					}
				}
			}
		}
		
		//Remove the nodes of the actual level from the list of remaining nodes
		for (i=0;i<nodesActualLevel.size();i++){
			remainingChanceAndDecNodes.removeNode(nodesActualLevel.elementAt(i));
		}
	}

	
	//Positionate the nodes of each level
	int width=1000;
	for (i=0;i<listOfLevels.size();i++){
		NodeList nodesLevel;
		
		nodesLevel=(NodeList)(listOfLevels.elementAt(i));
		int posY=50+i*150;
		int sizeLevel=nodesLevel.size();
		for (int j=0;j<sizeLevel;j++){
			int posX = (int) Math.round((width/sizeLevel)*(j+0.5));
			auxNode=nodesLevel.elementAt(j);
			auxNode.setPosX(posX);
			auxNode.setPosY(posY);
		}
	}
	
		
}



public static void main(String args[]) throws ParseException, IOException {
  Node from;
  Node to;
  NodeList nodes;
  IDiagram diag;
  int i,j;

  // Build the ID

  diag=(IDiagram)Network.read(args[0]);

  // Retrieve the nodes

  nodes=diag.getNodeList();

  for(i=0; i < nodes.size(); i++) {
    for(j=0; j < nodes.size(); j++) {
      if (i != j) {
        from=nodes.elementAt(i);
        to=nodes.elementAt(j);
        System.out.println("De "+from.getName()+ " to " + to.getName());
        System.out.println("   Max: "+from.maximalDistanceBetweenNodes(to));
        System.out.println("   Max: "+from.minimalDistanceBetweenNodes(to));
       }
    }
  }
}


/**
 * @return Returns the cpn.
 */
public CooperPolicyNetwork getCpn() {
	return cpn;
}
/**
 * @param cpn The cpn to set.
 */
public void setCpn(CooperPolicyNetwork cpn) {
	this.cpn = cpn;
}


/**
 * @return Returns the cpn.
 */
public PolicyNetwork getPn() {
	return pn;
}
/**
 * @param cpn The cpn to set.
 */
public void setPn(PolicyNetwork pn) {
	this.pn = pn;
}
/**
 * @param posteriorDistributions The posteriorDistributions to set.
 */
public void setPosteriorDistributions(ArrayList posteriorDistributions) {
	this.posteriorDistributions = posteriorDistributions;
}
/**
 * @return Returns the posteriorDistributions.
 */
public ArrayList getPosteriorDistributions() {
	return posteriorDistributions;
}
/**
 * @return Returns the posteriorUtilities.
 */
public ArrayList getPosteriorUtilities() {
	return posteriorUtilities;
}
/**
 * @param posteriorUtilities The posteriorUtilities to set.
 */
public void setPosteriorUtilities(ArrayList posteriorUtilities) {
	this.posteriorUtilities = posteriorUtilities;
}


public Hashtable<Node, Relation> getForcedPolicies() {
	return forcedPolicies;
}



/**
 * It returns the nodes belonging to the past of the decision
 * @param decision
 * @return
 */
public NodeList getPast(Node decision){
	NodeList decsOrdered;
	int posInDecsOrdered;
	NodeList past;
	Node auxDec;
	
	decsOrdered = this.getDecisionList();
	posInDecsOrdered = decsOrdered.getId(decision);
	
	
	past = new NodeList();
	for (int i=0;i<posInDecsOrdered;i++){
		auxDec = decsOrdered.elementAt(i);
		past.join(auxDec.getParentNodes());
		past.insertNode(auxDec);
	}
	past.join(decision.getParentNodes());
	
	return past;
	
	
}


/**
 * It returns the nodes belonging to the relevant past of the decision
 * @param decision
 * @return
 */
public NodeList getRelevantPast(Node decision){
	
	this.addNonForgettingArcs();
	this.eliminateRedundancy();
	return decision.getParentNodes();
	
}

public boolean hasForcedPolicy(Node decision){
	return (forcedPolicies.containsKey(decision));
	
}

   /**
    * Method for getting information about the network
    */
   public void getInformation(){
      int chanceNodes;
      int decisionNodes;
      int valueNodes;
      int links;
      double sizeOfPotentials;
      double constrainedConfigurations;
      
      // Get the information
      chanceNodes=numberOfChanceNodes();
      decisionNodes=numberOfDecisions();
      valueNodes=numberOfValueNodes();
      links=numberOfLinks();
      sizeOfPotentials=calculateSizeOfPotentials();
      constrainedConfigurations=computeConstrainedConfigurations();
			
      // Print the information
      System.out.println("Chance: "+chanceNodes+"  Decision: "+decisionNodes+"  Value: "+valueNodes+"  Links: "+links+"  Size: "+sizeOfPotentials+" Constrained confs: "+constrainedConfigurations);
   }

/**
 * Method for computing all the constrained configurations
 * @return constrained
 */
public double computeConstrainedConfigurations(){
    Vector relations=getRelationList();
    Relation rel;
    double constrained=0;
    
    // Consider relations one by one
    for(int i=0; i < relations.size(); i++){
        rel=(Relation)relations.elementAt(i);
        if (rel.getKind() == Relation.CONSTRAINT){
            constrained+=computeConstrainedConfigurations(rel);
        }
    }
    
    // Return constrained
    return constrained;
}

/**
 * Method for computing the number of configurations constrained
 * by a given constraint
 * @param constraint
 * @return constrained
 */
public double computeConstrainedConfigurations(Relation constraint){
   Vector relations=getRelationList();
   Potential constraintVal;
   NodeList common,difference;
   Relation rel;
   double rest,allowed,constrained=0;
   int kind;
   
   // If the relation is not a constraint return 0
   kind=constraint.getKind();
   if (kind != Relation.CONSTRAINT)
       return 0;
   
   // Check the number of values defined by the constraint
   constraintVal=constraint.getValues();
   if (constraintVal.getClassName().equals("LogicalExpression")){
     constraintVal=(LogicalExpression)constraint.getValues();
     allowed=((LogicalExpression)constraintVal).checkOnes();
   }
   else{
     allowed=constraintVal.totalPotential();
   }

   // Check if there is a relation containing the variables of the
   // constraint
   for(int i=0; i < relations.size(); i++){
      rel=(Relation)relations.elementAt(i);
      
      // Consider only non-constraint relations
      if (rel.getKind() != Relation.CONSTRAINT){
         // Get the variables in common between rel and constraint
         common=constraint.getVariables().intersection(rel.getVariables());
      
         // If common contains the variables in constraint, then the constraint
         // will be applicable on rel
         if (common.size() == constraint.getVariables().size()){
            // Get the rest of variables
            difference=rel.getVariables().difference(constraint.getVariables());
         
            // Compute the cardinal of the cartesian product for the variables
            // in difference
            rest=difference.getSize();
                  
            // Now constrained will be given as the cardinal of the cartesian
            // product for (common - allowed) * rest
            constrained+=(common.getSize()-allowed)*rest;
         }
      }
   }
   
   // Return constrained
   return constrained;
}

} // End of class
