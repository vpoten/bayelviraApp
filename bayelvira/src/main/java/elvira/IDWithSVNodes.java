/* IDWithSVNodes.java */

package elvira;


import elvira.gui.ElviraPanel;
import elvira.gui.explication.VisualNode;
import elvira.inference.super_value.*;
import elvira.inference.uids.NodeGSDAG;

import java.util.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import elvira.parser.*;
import elvira.potential.Function;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.potential.ProductFunction;
import elvira.potential.SumFunction;
import elvira.potential.UtilityPotential;
import elvira.inference.*;
import elvira.inference.elimination.*;
import elvira.potential.*;

/**
 * This class implements the structure for storing and
 * manipulating the Influence Diagrams with Super Value Nodes.
 *
 * @version 0.1
 * @since 27/1/2004
 * @author  Manuel Luque
 */
public class IDWithSVNodes extends IDiagram{
    
    /** Creates a new instance of IDWithSVNodes */
    public IDWithSVNodes() {
    }
    
    /**
 * Creates an Influence Diagram with SVNodes parsing it from a file.
 * @param name name of the file that contains the diagram.
 */
public IDWithSVNodes(String name) throws ParseException, IOException {
     super(name);
            
 }
 
 /**
* This function is used to know whether the link that we want to add,
* and whose head is 'child' and whose tail is 'father',
* is compatible with super value node structures.
* This function suppose the structure of value nodes is  a tree
* @param father the tail of the link that we want to add.
* @param child the head of the link that we want to add.
* @return true iff the link is compatible with super value node structures
*/
public static boolean isCompatibleLinkMustBeTree(Node father,Node child)
{
    
	 LinkList listParents;
	 int kindFather;
     boolean compatible=false;
	
	 kindFather=father.getKindOfNode();
	 switch (child.getKindOfNode())
	 {
		 case Node.CHANCE:
			 compatible= ((kindFather==Node.CHANCE)||(kindFather==Node.DECISION));
			 break;
		 case Node.DECISION:
			 compatible= ((kindFather==Node.CHANCE)||(kindFather==Node.DECISION));
			 break;
		case Node.UTILITY:
                case Node.SUPER_VALUE:
                    //The father must have some father. There must be in order in the construction
                    //of the super value structure.
                    //Proving what happens if we add a link between an utility or super value node without
                    //parents and a super value (or utility) node
                    /*if (((kindFather==Node.UTILITY)||(kindFather==Node.SUPER_VALUE))
                    &&(father.getParents().size()==0)) compatible=false;
                    else {*/
                        listParents=child.getParents();
                        if (listParents.size()==0) { //In this case child UTILITY or SUPER_VALUE is equivalent
                            if ((kindFather==Node.CHANCE)||(kindFather==Node.DECISION)) {
                                compatible=true;
                            }
                            else if ((kindFather==Node.UTILITY)||(kindFather==Node.SUPER_VALUE)) {
                                compatible=!father.hasChildrenNodes();
                            }
                        }
                        else {
                            //When an utility or super_value node has parents his kind is correct.
                            //This is used in this block.
                            if (child.getKindOfNode()==Node.UTILITY){
                                compatible= ((kindFather==Node.CHANCE)||(kindFather==Node.DECISION));
                            }
                            else{//Child Node.SUPER_VALUE
                                compatible=(((kindFather==Node.UTILITY)||(kindFather==Node.SUPER_VALUE))
                                &&(!father.hasChildrenNodes()));
                            }
                         }
                    //}//end of else of if (((kindFather==...
                    break;
         }
         return compatible;
}           


 /**
* This function is used to know whether the link that we want to add,
* and whose head is 'child' and whose tail is 'father',
* is compatible with super value node structures.
* The structure of value nodes hasn't to be a tree, it can be a graph.
* @param father the tail of the link that we want to add.
* @param child the head of the link that we want to add.
* @return true iff the link is compatible with super value node structures
*/
public static boolean isCompatibleLink(Node father,Node child)
{
    
	 LinkList listParents;
	 int kindFather;
	 boolean compatible=false;
	
	 kindFather=father.getKindOfNode();
	 switch (child.getKindOfNode())
	 {
		 case Node.CHANCE:
			 compatible= ((kindFather==Node.CHANCE)||(kindFather==Node.DECISION));
			 break;
		 case Node.DECISION:
			 compatible= ((kindFather==Node.CHANCE)||(kindFather==Node.DECISION));
			 break;
		case Node.UTILITY:
				case Node.SUPER_VALUE:
					//The father must have some father. There must be in order in the construction
					//of the super value structure.
					//Proving what happens if we add a link between an utility or super value node without
					//parents and a super value (or utility) node
					/*if (((kindFather==Node.UTILITY)||(kindFather==Node.SUPER_VALUE))
					&&(father.getParents().size()==0)) compatible=false;
					else {*/
						listParents=child.getParents();
						if (listParents.size()==0) { //In this case child UTILITY or SUPER_VALUE is equivalent
							if ((kindFather==Node.CHANCE)||(kindFather==Node.DECISION)) {
								compatible=true;
							}
							else if ((kindFather==Node.UTILITY)||(kindFather==Node.SUPER_VALUE)) {
								//Father can have other children, so the structure
								//of value nodes hasn't to be a tree
								//compatible=!father.hasChildrenNodes();
								compatible=true;
							}
						}
						else {
							//When an utility or super_value node has parents his kind is correct.
							//This is used in this block.
							if (child.getKindOfNode()==Node.UTILITY){
								compatible= ((kindFather==Node.CHANCE)||(kindFather==Node.DECISION));
							}
							else{//Child Node.SUPER_VALUE
								
								/*compatible=(((kindFather==Node.UTILITY)||(kindFather==Node.SUPER_VALUE))
								&&(!father.hasChildrenNodes()));*/
								//Father can have other children, so the structure
								//of value nodes hasn't to be a tree
								compatible=((kindFather==Node.UTILITY)||(kindFather==Node.SUPER_VALUE));
																
							}
						 }
					//}//end of else of if (((kindFather==...
					break;
		 }
		 return compatible;
}           


/**
* This function is used to know whether the kind of the node 'child' must
* be changed before adding a link. They are only changed utility nodes and
* super value nodes. The precondition of this function is that the link
* ('father','child') must be compatible with super value node structures.
* @param father the tail of the link that we want to add.
* @param child the head of the link that we want to add.
* @return true iff the kind of the utility node or super value node must be changed
*/
public static boolean changeKindOfChildBeforeAddLink(Node father,Node child) {
    int kindFather;
    boolean changeKindOfChild=false;
    
    kindFather=father.getKindOfNode();
    switch (child.getKindOfNode()) {
        case Node.CHANCE:
            changeKindOfChild=false;
            break;
        case Node.DECISION:
            changeKindOfChild=false;
            break;
        case Node.UTILITY:
            changeKindOfChild= ((kindFather==Node.SUPER_VALUE)||(kindFather==Node.UTILITY));
            // newKindOfChild=Node.SUPER_VALUE;
            break;
        case Node.SUPER_VALUE:
            changeKindOfChild=((kindFather==Node.CHANCE)||(kindFather==Node.DECISION));
            //newKindOfChild=Node.UTILITY;
            break;
    }
    return changeKindOfChild;
}


public boolean hasSVNodes(){
	
	return (getNodesOfKind(Node.SUPER_VALUE).size()>0);
}




/**
 * Stores the <code>IDiagram With SVNodes</code> in the file given as parameter.
 * @param f file where the <code>IDiagram With SVNodes</code> is saved.
 * @see Network#save
 */

public void saveIDWithSVNodes(FileWriter f) throws IOException {

  PrintWriter p;

  p = new PrintWriter(f);

  super.save (p);
}

/**
 * To get the barren nodes.
 * @return a barren node.
 */



/**
 * Saves the header of the file that will contain this
 * diagram
 * @param p the file.
 */

public void saveHead(PrintWriter p) throws IOException {

  if (hasSVNodes()==false){
  	super.saveHead(p);
  }
  else{
	  p.print("// Influence Diagram With Super Value Nodes\n");
	  p.print("//   Elvira format \n\n");
	  p.print("id-with-svnodes  \""+getName()+"\" { \n\n");
  }
}


/**
 * Copies this diagram.
 * @return a copy of this diagram.
 */

public IDWithSVNodes copyIDWSV() {
    IDWithSVNodes idwsv;
    Graph g;
    Enumeration e;
    Vector rl = new Vector();
    Vector vars;
    NodeList varsInOriginal;
    Relation rNew;
    Potential pt;
    Potential ptOriginal;
    Node node;
    RelationList rList;
    
    g=duplicate();
    idwsv=new IDWithSVNodes();
    idwsv.setNodeList(g.getNodeList());
    idwsv.setLinkList(g.getLinkList());
    for (e = getRelationList().elements() ; e.hasMoreElements() ; ) {
        Relation r = (Relation) e.nextElement();
        
        // Get the variables of this relation, and get references
        // to these variables, but for the new list of nodes
        
        varsInOriginal=r.getVariables();
        vars=new Vector();
        
        for(int i=0; i < varsInOriginal.size(); i++){
            node=idwsv.nodeList.getNode(((Node)varsInOriginal.elementAt(i)).getName());
            // This node is inserted in vars
            
            vars.addElement(node);
        }
        
        // Create the new relation. Use copy method to initialize
        // all data fields, but now change variables in the same
        // relation and in its potential
        
        rNew=r.copy();
        rNew.setVariables(vars);
        
        // Now, copy its values, if present
        
        ptOriginal=r.getValues();
        if (ptOriginal != null){
            pt=ptOriginal.copy();
            // Now set the list of original variables. If the relation
            // is a UTILITY relation, the initial variable (for the
            // utility node should not appear in the potential)
            
            if((r.getKind() == Relation.UTILITY)||(r.getKind()==Relation.UTILITY_COMBINATION)){
                vars.removeElementAt(0);
            }
            
            pt.setVariables(vars);
            
            // Set this potential to the new relation
            
            rNew.setValues(pt);
        }
        
        rl.add(rNew);
    }
    //We must repair the potentials of the idwithsvnodes
    rList=new RelationList();
    rList.setRelations(rl);
    rList.repairPotFunctions();
    idwsv.setRelationList(rList.getRelations());
    return idwsv;
}


/**
 * Compiles this diagram (evaluating without evidences).
 */
public void compile(int index, Vector parameters) {

	IDWithSVNodes iDWSVCopy;
	Propagation eval;
	boolean evaluable;
	int inferenceMethod;
	int specificAlg;
	
	//Number of specific algorithms for IDWithSVNodes. It's used to compute
	//the index of algorithm for IDiagram
	specificAlg=5;

	if ((hasSVNodes() == false) && ((index >= specificAlg) && (index < (specificAlg+6)))) {

		inferenceMethod = index - specificAlg;
		//This is according to class IDPropagationDialog
		super.compile(inferenceMethod, parameters);

	} else {
		//The influence has super-value nodes or the inference
		//method is variable elimination for ID with SV nodes, although
		//the ID hasn't any SV nodes.

		//Copy the diagram
		iDWSVCopy = copyIDWSV();

		if ((index >= specificAlg) && (index < (specificAlg+6))) {
			/* Reduction and Variable Elimination */
			/* Reduction and Variable Elimination, Potential Trees */
			/* Reduction and Variable Elimination, Potential Trees and Constraints */
			/* Reduction and Arc Reversal */
			/* Reduction and Arc Reversal, Potential Tress */
			/* Reduction and Arc Reversal, Potential Trees and Constraints */

			//This is according to class IDPropagationDialog
			inferenceMethod = index - specificAlg;
			

			eval =
				new ReductionAndEvalID(iDWSVCopy, inferenceMethod, parameters);

			// Initial tests about the node.
			evaluable = ((ReductionAndEvalID) eval).initialConditions();

			if (evaluable) {

				((ReductionAndEvalID) eval).propagate();

				//Make the results accesible
				showResults(eval);
			}
		} else if (index == 0) { //With divisions, policies and utilities
			eval = new VariableEliminationSV(iDWSVCopy);
			((VariableEliminationSV) eval).propagate(0,parameters);
			showResults(eval);
		} else if (index == 1) { //With divisions, only policies
			eval = new VariableEliminationSV(iDWSVCopy);
			((VariableEliminationSV) eval).propagate(1,parameters);
			showResults(eval);
		} else if (index == 2) { //Without divisions => Only policies
			eval = new VariableEliminationSV(iDWSVCopy);
			((VariableEliminationSV) eval).propagate(2,parameters);
			showResults(eval);
		} else if (index == 3) { //Tatman and Shachter's algorithm, policies and utilities
			eval = new ArcReversalSV(iDWSVCopy);
			// Initial tests about the node.
			evaluable = ((ArcReversalSV) eval).initialConditions();
			if (evaluable) {
				((ArcReversalSV) eval).evaluateDiagram(true,parameters);
				//Make the results accesible
				showResults(eval);
			}
		} else if (index == 4) { //Tatman and Shachter's algorithm, only policies
			eval = new ArcReversalSV(iDWSVCopy);
			// Initial tests about the node.
			evaluable = ((ArcReversalSV) eval).initialConditions();
			if (evaluable) {
				((ArcReversalSV) eval).evaluateDiagram(false,parameters);
				//Make the results accesible
				showResults(eval);
			}
		}

	}
}//end compile(...)

/**
 * Compiles this diagram with an algorithm for influence diagrams.
 * Precondition of the method: Diagram can't have any super-value nodes.
 */
public void compileID(int index, Vector parameters){
    super.compile(index,parameters);
}

/**
 * Copies the influence diagram 'id' into an object of IDWithSVNodes
 * @return a copy of 'id' that is an object of IDWithSVNodes.
 */
public static IDWithSVNodes convertToIDWithSVNodes(IDiagram id){
	   IDWithSVNodes idwsv;
	   Graph g;
	   Enumeration e;
	   Vector rl = new Vector();
	   Vector vars;
	   NodeList varsInOriginal;
	   Relation rNew;
	   Potential pt;
	   Potential ptOriginal;
	   Node node;
	   RelationList rList;
    
	   g=id.duplicate();
	   idwsv=new IDWithSVNodes();
	   idwsv.setNodeList(g.getNodeList());
	   idwsv.setLinkList(g.getLinkList());
	   for (e = id.getRelationList().elements() ; e.hasMoreElements() ; ) {
		   Relation r = (Relation) e.nextElement();
        
		   // Get the variables of this relation, and get references
		   // to these variables, but for the new list of nodes
        
		   varsInOriginal=r.getVariables();
		   vars=new Vector();
        
		   for(int i=0; i < varsInOriginal.size(); i++){
			   node=idwsv.nodeList.getNode(((Node)varsInOriginal.elementAt(i)).getName());
			   // This node is inserted in vars
            
			   vars.addElement(node);
		   }
        
		   // Create the new relation. Use copy method to initialize
		   // all data fields, but now change variables in the same
		   // relation and in its potential
        
		   rNew=r.copy();
		   rNew.setVariables(vars);
        
		   // Now, copy its values, if present
        
		   ptOriginal=r.getValues();
		   if (ptOriginal != null){
			   pt=ptOriginal.copy(); //Por aquí voy (MLUQUE)
			   // Now set the list of original variables. If the relation
			   // is a UTILITY relation, the initial variable (for the
			   // utility node should not appear in the potential)
            
			   if((r.getKind() == Relation.UTILITY)||(r.getKind()==Relation.UTILITY_COMBINATION)){
				   vars.removeElementAt(0);
			   }
            
			   pt.setVariables(vars);
            
			   // Set this potential to the new relation
            
			   rNew.setValues(pt);
		   }
        
		   rl.add(rNew);
	   }
	   //We must repair the potentials of the idwithsvnodes
	   rList=new RelationList();
	   rList.setRelations(rl);
	   rList.repairPotFunctions();
	   idwsv.setRelationList(rList.getRelations());
	   return idwsv;	
}
	
	
	

	
/**  
 * It returns true iff the influence diagram with super value nodes has
 * only a super-value node that hasn't children.
 */
public boolean hasOnlyOneTerminalSVNode(){
	NodeList nodesSV;
	int nodesSVWithoutChild=0;
	int i;
	Node node;
	
	nodesSV=getNodesOfKind(Node.SUPER_VALUE);
	
	for(i=0;i<nodesSV.size();i++){
		node=nodesSV.elementAt(i);
		if (node.getChildren().size()==0){
			nodesSVWithoutChild++;
		}
	}
	
	return (nodesSVWithoutChild==1);
}


/**  
 * It returns true iff the influence diagram with super value nodes has
 * only a utility node that hasn't children. It's not the same that in method
 * hasOnlyOneTerminalSVNode because here we count all utility nodes: super and non-super
 */
public boolean hasOnlyOneTerminalValueNode(){
	int nodesUWithoutChild=0;
	int i;
	Node node;
	int kind;
	NodeList nodes;
	
	nodes = getNodeList();
		
	for(i=0;i<nodes.size();i++){
		node=nodes.elementAt(i);
		kind = node.getKindOfNode();
		if ((kind==Node.UTILITY)||(kind==Node.SUPER_VALUE)){
		if (node.getChildren().size()==0){
			nodesUWithoutChild++;
		}
		}
	}
	
	return (nodesUWithoutChild==1);
}

/**  
 * It returns true iff the influence diagram with super value nodes hasn't got
 * any super-value nodes and it has only one utility node
 */
public boolean hasOnlyOneValueNode(){
	NodeList nodesSV;
	NodeList nodesU;
		
	nodesSV=getNodesOfKind(Node.SUPER_VALUE);
	
	if (nodesSV.size()!=0) return false;
	else{
		nodesU=getNodesOfKind(Node.UTILITY);
		return (nodesU.size()==1);
	}
}

/**
 * Method to get the list of utility (non-super) and super-value nodes of the diagram
 */

public NodeList getValueNodes(){
 NodeList utils=new NodeList();
 NodeList allNodes=getNodeList();
 Node node;
 int i;
 int kind;

  for(i=0; i < allNodes.size(); i++){
    node=allNodes.elementAt(i);
    kind = node.getKindOfNode();
    if ((kind == Node.UTILITY)||(kind == Node.SUPER_VALUE)){
      utils.insertNode(node);
    }
  }

  return utils;
}
/**  
 * It returns the super-value node that hasn't children in an influence
 * diagram with super-value nodes.
 * Precondition: There's only a super-value node with these conditions.
 */



public Node getTerminalValueNode(){

	NodeList nodesSV;
	int nodesSVWithoutChild=0;
	int i;
	Node node;
	Node terminalNode=null;
	boolean found;
	
	nodesSV=getNodesOfKind(Node.SUPER_VALUE);
	found=false;
	
	if (hasSVNodes()){
		for(i=0;(i<nodesSV.size())&&(found==false);i++){
				node=nodesSV.elementAt(i);
				if (node.getChildren().size()==0){
					terminalNode=node;
					found=true;
				}
		}
	}
	else{
		NodeList utils;
		utils = getNodesOfKind(Node.UTILITY);
		if (utils.size()==1){
			terminalNode=utils.elementAt(0);
		}
		else{
			System.out.println("Error in Method getTerminalValueNode, class IDWithSVNodes");
			System.out.println("Influence diagram hasn't got one terminal value node");
			System.exit(0);
		}
			
	}
	
	
	return terminalNode;
}


/**  
 * It returns a list with the value nodes (super or non-super) that haven't children in an influence
 * diagram with super-value nodes.
 */
public NodeList getListOfTerminalValueNodes(){

	int i;
	Node node;
	NodeList terminals;
	NodeList nodes;
	int kind;

	terminals = new NodeList();
	nodes = this.getNodeList();
	for(i=0;i<nodes.size();i++){
		node = nodes.elementAt(i);
		kind = node.getKindOfNode();
		if ((kind==Node.UTILITY)||(kind==Node.SUPER_VALUE)){
			if (node.getChildren().size()==0){
			terminals.insertNode(node);
			}
		}
	}
	return terminals;
	}

/**
 * To obtain the functional predecessors of the utility or super value
 * node 'n' which are chance or decision nodes.
 */         
public NodeList getChanceAndDecisionPredecessors(Node n){
	int type;
	NodeList predecessors=null;
	NodeList parentsOfSV;
	NodeList auxPred;
		
	type=n.getKindOfNode();
	switch (type){
		case Node.CHANCE: 
			predecessors=null;
			break;
		case Node.DECISION:
			predecessors=null;
			break;
		case Node.UTILITY:
			predecessors=parents(n);
			break;
		case Node.SUPER_VALUE:
			parentsOfSV=parents(n);
			predecessors=new NodeList();
			for (int i=0;i<parentsOfSV.size();i++){
				auxPred=getChanceAndDecisionPredecessors(parentsOfSV.elementAt(i));
				predecessors.merge(auxPred);				
			}
	}
	return predecessors;
}

/**  
 * It checks if the structure of value nodes is a tree, i.e. each value
 * node has one sucessor, except the terminal super value node.
 */

public boolean isTreeStructureSV(){
	NodeList nodes;
	int type;
	Node node;
	Node svTerminal;
	boolean isTree;
	
	nodes=getNodeList();
	if (hasOnlyOneTerminalSVNode()){ //There's one terminal super value node
		svTerminal=getTerminalValueNode();
		isTree=true;
		for (int i=0;i<nodes.size();i++){
			node=nodes.elementAt(i);
			type=node.getKindOfNode();
			if ((type==Node.UTILITY)||(type==Node.SUPER_VALUE)){
				//Value nodes must have on sucessor, except the terminal sv node
				if ((node.children.size()!=1)&&(node!=svTerminal)){
					isTree=false;
				}
			}
		}
	}
	else{
		// influence diagram may have only one value node (non sv) 
		isTree= hasOnlyOneValueNode();
	}
	return isTree;
}
	
	

/**
 * Copy this diagram with relations, but not the values
 * of the relations
 */

public IDWithSVNodes qualitativeCopyWithRelationsSV() {
  IDWithSVNodes id = new IDWithSVNodes();
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



public static void main(String args[]) throws ParseException, IOException {
  Node from;
  Node to;
  NodeList nodes;
  IDWithSVNodes diag;
  int i,j;
  //PartialOrder p;

  // Build the ID with SV Nodes
  diag=new IDWithSVNodes(args[0]);
  
 
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
  
 
  diag.compile(0,null);
  i=1;
}

	/* (non-Javadoc)
	 * @see elvira.IDiagram#getBarrenNode()
	 * 
	 */
	/**
	 * To get the barren nodes.
	 * @return a barren node.
	 */

	public Node getBarrenNode() {
		
		boolean barrenFounded=false;
		Node barrenNode=null;
		
		NodeList listOfNodes = getNodeList();
		
		for (int i=0;((i<listOfNodes.size())&&(barrenFounded==false));i++){
			Node auxNode;
			int kind;
			auxNode=listOfNodes.elementAt(i);
			kind=auxNode.getKindOfNode();
			if ((kind==Node.CHANCE)||(kind==Node.DECISION)){
				if (auxNode.getChildren().size()==0){
					barrenFounded=true;
					barrenNode=auxNode;
					System.out.println("Removal of barren node: "+barrenNode.getName());
				}
			}
		}
		return barrenNode;
	}


	/**
	 * It computes the aggregate table of utilities of a value node. This node can be a super-value
	 * node or a utility (non-super) node. If it's a super-value node, the combination of several tables
	 * would be necessary.
	 * @param value Value node
	 * @return The table of utilities that the value node represents
	 */
	public PotentialTable getTotalUtility(Node value) {
		PotentialTable aggregate;
		Relation relationValue;
		Potential potValue;
		NodeList nodesPotValue=new NodeList();
		
		//Compute the aggregate potential of the parents of the super value node.
		
		relationValue=getRelation(value);
		potValue=relationValue.getValues();
		
		if (value.getKindOfNode()==Node.UTILITY){
			return (PotentialTable)potValue;
		}
		else{
			//SUPER_VALUE
			nodesPotValue.setNodes(potValue.getVariables());
			aggregate=getTotalUtility(nodesPotValue.elementAt(0));
			for (int i=1;i<nodesPotValue.size();i++){
			   aggregate=aggregate.combine(getTotalUtility(nodesPotValue.elementAt(i)),((UtilityPotential)potValue).getFunction());
			}
			return aggregate;
		}
		
	}
	
	/**
	 * It computes the aggregate table of utilities of the terminal value node.
	 * @return The table of utilities that the terminal value node represents
	 */
	public Potential getPotentialOfGlobalUtility(){
		//// TODO The potential returned is a potential table. I have to genarlize the method
		// to return simply a potential
		return (getTotalUtility(getTerminalValueNode()));
	}

	/* (non-Javadoc)
	 * @see elvira.Graph#removeLink(elvira.Node, elvira.Node)
	 */
	public void removeLink(Node tail, Node head) throws InvalidEditException {

		int p;

		if (head.getKindOfNode() != Node.SUPER_VALUE) {
			 			  
			  // Remove the link
			  p = getLinkList().getID(tail.getName(),head.getName());
			  removeLink(p);

			  // now take the old relation from head
			  // first remove the old one
			  removeRelation(head);
			  
			  //// TODO Talk with Manolo Gomez about the next if...
			  if (head.getKindOfNode()!=Node.DECISION){	
				  // now add the new relation
				  addRelation(head);
			  }
		} else {
			Function functionSV;

			//Remove the link
			p = getLinkList().getID(tail.getName(), head.getName());
			removeLink(p);

			//Store the function (sum or product) of the relation of the sv node
			//before the removing of its relation
			functionSV =
				((UtilityPotential) (getRelation(head).getValues()))
					.getFunction();

			// now take the old relation from head
			// first remove the old one
			removeRelation(head);

			// now add the new relation
			addRelation(head);

			//Set the function (sum or product) of the relation of the sv node
			//after the adding of its relation
			((UtilityPotential) (getRelation(head).getValues())).setFunction(
				functionSV);
		}

	}
	
	/**
	 * Creates a new link between head and tail in the network.
	 * @param tail first node in the new link.
	 * @param head second node in the new link.
	 */

	public void createLink(Node tail, Node head) throws InvalidEditException {

	  // Create a new link
	  super.createLink(tail,head);
	  
	  if (changeKindOfChildBeforeAddLink(tail,head)){
			head.setKindOfNode(((head.getKindOfNode()==Node.UTILITY)?Node.SUPER_VALUE:Node.UTILITY));
	  }
	  	  
	  if(head.getKindOfNode()!=Node.DECISION){
		if (head.getKindOfNode() != Node.SUPER_VALUE) {
			removeRelation(head);
		  // Now create the new relation and add it
			addRelation(head);

		} else {
			Potential pot;
			
			pot= getRelation(head).getValues();
			
			if (pot.getClass()==UtilityPotential.class){
			
			Function functionSV;

			//Store the function (sum or product) of the relation of the sv node
			//before the removing of its relation
			functionSV = ((UtilityPotential) pot).getFunction();

			// now take the old relation from head
			// first remove the old one
			removeRelation(head);

			// now add the new relation
			addRelation(head);

			//Set the function (sum or product) of the relation of the sv node
			//after the adding of its relation
			((UtilityPotential) (getRelation(head).getValues())).setFunction(
				functionSV);
			}
			else{
				removeRelation(head);
						  // Now create the new relation and add it
				addRelation(head);
			}
		}

	  }
	}


	
	/**
	 * Method that adds a terminal super value node to the influence diagram
	 * It assumes that there is at least a utility node in the ID
	 */
	public Node addATerminalSuperValueNode(){
		NodeList terminals;
		int sumX;
		int maxY;
		int numTerminals;
		Node auxNode;
		int newX;
		int newY;
		int offset;
		String newName;
		Node newNodeSV;
		
		terminals = getListOfTerminalValueNodes();
		
		numTerminals = terminals.size();
		sumX = 0;
		maxY = 0;
		//Calculate the new positions of the sv node
		for (int i=0;i<numTerminals;i++){
			auxNode = terminals.elementAt(i);
			sumX = auxNode.getPosX() + sumX;
			maxY = Math.max(auxNode.getPosY(),maxY);
		}
		
		newX = Math.round((float)sumX/numTerminals);
		offset = 100;
		newY = maxY + offset;
		
		//Create the new sv node
		newName = "U_0";
		createNode (newX, newY, "Helvetica",newName, Node.SUPER_VALUE);
		
		newNodeSV=getNode(newName);
		
	     

		//Links from old terminals to new sv node
		for (int j=0;j<numTerminals;j++){
			try{
			createLink(terminals.elementAt(j),newNodeSV);
			} catch (InvalidEditException iee) {
			};
		}
		
		//Set the new sum relation for the sv node
		Function functionNewNode;
		functionNewNode=new SumFunction();
		
		((UtilityPotential)(getRelation(newNodeSV).getValues())).setFunction(functionNewNode);
		
	
		return newNodeSV;
	}
	
	/**
	 * @return a new ID where decisions appearing in forcedPolicies are replaced by chance nodes
	 * with probability distribution given by the forced policy
	 */
	public IDWithSVNodes constructAnIDUsingForcedPolicies(){
		IDWithSVNodes newID;
		Node decInOriginal;
		Node decInNew;
		NodeList decsInOriginal;
		Relation relInOriginal;
		Relation relInNew;
		NodeList varsRelInNew;
		
		
		decsInOriginal = this.getNodesOfKind(Node.DECISION);
		
		//Copy the full ID
		newID = copyIDWSV();
		
		
		for (int i=0;i<decsInOriginal.size();i++){
			decInOriginal = decsInOriginal.elementAt(i);
			
			if (forcedPolicies.containsKey(decInOriginal)){
				decInNew = newID.getNode(decInOriginal.getName());
				//Convert decision node into chance node
				CooperPolicyNetwork.convertDecisionIntoChanceWithoutParents(newID,decInNew);
				//Translate the forced policy to the new ID
				relInOriginal = forcedPolicies.get(decInOriginal);
				relInNew = CooperPolicyNetwork.translateRelation(newID,relInOriginal);
				
//				 Create the links according to the new relation
				varsRelInNew = relInNew.getVariables();
				for (int j = 1; j < varsRelInNew.size(); j++) {
					try {
						newID.createLink(varsRelInNew.elementAt(j),decInNew);
					} catch (InvalidEditException iee) {
						;
					}
				}

				// Remove the relation that appears because of the creation of links
				// and substitute it by the new relation
				newID.removeRelation(decInNew);
				newID.addRelation(relInNew);
			}
		}
		
		return newID;
			
	}
	
	/**
	 * To remove the barren nodes of the diagram
	 * (chance or decision nodes without sucesors).
	 * 
	 */

	public ArrayList<String> removeBarrenNodesAndReturnThem() {
	  Node barren;
	  ArrayList<String> barrens;
	  
	  barrens = new ArrayList();
	  
	  while ((barren = getBarrenNode())!= (Node) null){
	    removeNodeOnly(barren);
	    barrens.add(barren.getName());
	  }
	  return barrens;
	}

	/**
	 * It obtains a CPN with the optimal strategy without forced policies. The CPN is set to the attribute 'cpn'
	 */
	public void obtainAndSetCPN() {
		// TODO Auto-generated method stub
		CooperPolicyNetwork cooperPN = CooperPolicyNetwork.constructCPNFrom(this,3,null);
     	//The cooper policy network must also be compiled.
     	cooperPN.compile(0,null,null,null);
     	
     	setCpn(cooperPN);
	}
	
	
	/**
	 * It obtains a CPN with the optimal strategy without forced policies. The CPN is set to the attribute 'cpn'
	 */
	public void obtainAndSetPN() {
		// TODO Auto-generated method stub
				
		Propagation propag = this.getPropagation();
		
		if ((propag!=null)&&(propag.getClass()==ArcReversalSV.class)){
			pn = new PolicyNetwork(this,(ArcReversalSV) propag);
		}
		else{
			pn = new PolicyNetwork(this);
		}
		
		
		
     	
     	setPn(pn);
	}
	
	
	/**
	 * @param configuration
	 * @return The list of probability relations, but restricted to 'configuration'
	 */
	public RelationList getProbabilityRelations(Configuration configuration) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		NodeGSDAG lastNodeGSDAG;
		Relation rel;
		int kind;
		Relation relRestricted;
		
		Vector relations = this.getRelationList();
		// Set separately the relations of the node
		RelationList probabilityRelations = new RelationList();
		
		for (int i = 0; i < relations.size(); i++) {
			rel = (Relation) relations.elementAt(i);
			kind = rel.getKind();
			relRestricted = rel.restrict(configuration);
			if (kind != Relation.UTILITY) {
				// probability relation
				probabilityRelations.insertRelation(relRestricted);
			}
		}
		return probabilityRelations;

		
	}
	
	
	public RelationList getProbabilityRelations() {
		return getProbabilityRelations(new Configuration());
	}
	
	
	public IDWithSVNodes obtainACopyWithAnOnlyValueNode(){
		return obtainACopyWithoutSVNodes(false);
	}
	
	/**  
	 * Reduce the Influence Diagram With SV Nodes to an IDWithSVNodes but it only have utility nodes
	 * The resulting ID can have one or more utility nodes.
	 * If allowSeveralUtilities is true the resulting ID can have several utilities
	 * If allowSeveralUtilities is false the resulting ID must have one utility node
	 */

	public IDWithSVNodes obtainACopyWithoutSVNodes(boolean allowSeveralUtilities){
		
		ReductionAndEvalID red;
		
		IDWithSVNodes newDiag;
		
		//Copy the diagram
		newDiag = this.copyIDWSV();
		
		//Reduce it to a diagram without SV nodes
		red = new ReductionAndEvalID(newDiag,0,null);{
			
		red.reducediag(allowSeveralUtilities);
		
		return newDiag;
	}
		
		
		
		
		
		
	}

} // End of class
