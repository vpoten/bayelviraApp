package elvira.inference.uids;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.Vector;

import elvira.Bnet;
import elvira.Configuration;
import elvira.Evidence;
import elvira.FiniteStates;
import elvira.Graph;
import elvira.InvalidEditException;
import elvira.Link;
import elvira.LinkList;
import elvira.Network;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.UID;
import elvira.inference.super_value.TreeNodeSV;
import elvira.inference.uids.NodeGSDAG.TypeOfNodeGSDAG;

/**
 * @author Manolo
 * 
 */
//public class GSDAG extends Graph {
public class GSDAG extends Bnet {

	NodeGSDAG root;
	UID uid;

	public GSDAG(Network network) throws InvalidEditException {
		MNode rootgsdagMarta;
		NodeList apriori;

		Graph gsdagMarta = ((UID) network).getGraph();
		
		uid = (UID)network;

		rootgsdagMarta = getRootOfGSDAGMarta(gsdagMarta);
		root = constructGSDAG(rootgsdagMarta, (UID) network);
		
		System.out.println("GSDAG with "+root.getChildren().size()+ "children of the root");
		
		apriori = this.getAPrioriInformation();
		System.out.println("Information a priori:"+apriori);
		System.out.println("The size of states space of the a priori information is:"+apriori.getSize());
		
		print();
	}

	/**
	 * Inserts a node in the graph if it doesn't exist in the graph.
	 * 
	 * @param n
	 *            the node to insert.
	 */

	public void addNode(Node n) {

		this.nodeList.insertNode(n);
	}

	@Override
	public void createLink(Node tail, Node head) throws InvalidEditException {
		// TODO Auto-generated method stub

		Link l = new Link(tail, head, true);

		linkList.getLinks().addElement(l);

		tail.getSiblings().getLinks().addElement(l);
		head.getSiblings().getLinks().addElement(l);

		tail.getChildren().getLinks().addElement(l);
		head.getParents().getLinks().addElement(l);

	}

	/**
	 * Method that constructs a GSDAG like in the paper of UIDs from a GSDAG in
	 * Marta's way
	 * 
	 * @param rootgsdagMarta
	 * @return
	 * @throws InvalidEditException
	 */
	protected NodeGSDAG constructGSDAG(MNode rootgsdagMarta, UID uid)
			throws InvalidEditException {
		// TODO Auto-generated method stub
		NodeList childrenOfRoot;
		NodeGSDAG rootGSDAG;
		NodeGSDAG last;
		Link linkLast;
		NodeGSDAG nonObsNodeGSDAG;
		NodeGSDAG fatherOfLast;
		MNode initialMNode = null;

		if (hasMNodeToElim(rootgsdagMarta)){//There are variables to eliminate at the beginning
			rootGSDAG = auxConstructGSDAG(rootgsdagMarta);
		}
		else {
			initialMNode = rootgsdagMarta;
			childrenOfRoot = rootgsdagMarta.getChildrenNodes();
			if (childrenOfRoot == null) {
					rootGSDAG = null;
			} 
			else if (childrenOfRoot.size() == 0) {// An empty graph
				rootGSDAG = null;
			} else if (childrenOfRoot.size() == 1) {
				rootGSDAG = auxConstructGSDAG((MNode) (childrenOfRoot.elementAt(0)));
			} else {
				rootGSDAG = new NodeGSDAG(NodeGSDAG.TypeOfNodeGSDAG.BRANCH);
				this.addNode(rootGSDAG);
				// We add as children all the graphs of the sucessors
				for (int i = 0; i < childrenOfRoot.size(); i++) {
					NodeGSDAG auxNodeGSDAG = auxConstructGSDAG((MNode) (childrenOfRoot
							.elementAt(i)));
					try {
						createLink(rootGSDAG, auxNodeGSDAG);
					} catch (InvalidEditException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		//We add the variables known from the beginning as the root of the GSDAG
		if ((initialMNode!=null)&&(hasMNodeChanceToElim(initialMNode))){
			 	NodeGSDAG chanceNodeGSDAG = new NodeGSDAG(NodeGSDAG.TypeOfNodeGSDAG.CHANCE);
				chanceNodeGSDAG.setVariables(getNamesOfVariables(initialMNode.getObsEliminate()));

				addNode(chanceNodeGSDAG);
				// Link from the node of decisions to the node of chance
				// variables
				try {
					createLink(chanceNodeGSDAG,rootGSDAG);
					} catch (InvalidEditException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
				rootGSDAG = chanceNodeGSDAG;
		}
		
		
		
		
		
		
		// We add the non observable variables in a CHANCE node at the end of
		// the GS-DAG. If there isnt' non observable variables we do nothing else
		nonObsNodeGSDAG = computeNonObsNodeGSDAG(uid);
		if (nonObsNodeGSDAG != null) {
			addNode(nonObsNodeGSDAG);
			last = getLastNodeGSDAG(rootGSDAG);
			// The last node (branch) always have an only father
			// We redirect the links to insert the new node
			Vector auxLinksParentsLast;
			
			auxLinksParentsLast = last.getParents().getLinks();
			
			Vector auxToVector=new Vector(auxLinksParentsLast);
			
			for (int i=0;i<auxToVector.size();i++){
				Link auxLinkToLast =(Link) auxToVector.elementAt(i);
				fatherOfLast = (NodeGSDAG) auxLinkToLast.getTail();
				removeLink(auxLinkToLast);
				createLink(fatherOfLast, nonObsNodeGSDAG);
			}
			
			createLink(nonObsNodeGSDAG, last);
		}

		return rootGSDAG;

	}

	//Create a nodeGSDAG with the names of the non observable variables
	private NodeGSDAG computeNonObsNodeGSDAG(UID uid) {
		// TODO Auto-generated method stub
		ArrayList nonObservables;
		NodeGSDAG chanceNodeGSDAG;
		
		nonObservables = uid.getNonObservablesArrayList();
		
		if (nonObservables.size() > 0) {

			// Create the NodeGSDAG for the decisions
			chanceNodeGSDAG = new NodeGSDAG(NodeGSDAG.TypeOfNodeGSDAG.CHANCE);
			chanceNodeGSDAG.setVariables(nonObservables);
		} else {
			chanceNodeGSDAG = null;
		}
			
		
		return chanceNodeGSDAG;
		
	}

	
	/**
	 * Dettach a link the graph without destroying it.
	 * @param l link to remove.
	 */

	public void removeLink (Link l) throws InvalidEditException {
	  
	  Node h, t;
	  int pos, posl = linkList.indexOf(l);
	  
	  
	  if (posl != -1) {
	    h = l.getHead();
	    t = l.getTail();
	    removeLinkOfLinkList(t.getChildren(),l);
	    removeLinkOfLinkList(h.getParents(),l);
	    
	    removeLinkOfLinkList(linkList,l);
	  }
	  else throw new InvalidEditException(4);
	}

	/**
	 * Deletes the link given as argument. If the link doesn't exists,
	 * des not do anything.
	 * @param l the link to remove.
	 */

	public static void removeLinkOfLinkList(LinkList list,Link l) {
	  Link l2;
	  Vector links = list.getLinks();
	  int position = links.indexOf(l);
	  
	//  System.out.println(".. y posicion es " + position);
	  if (position != -1) links.removeElementAt(position);

	}

	
	
	/**
	 * @param node
	 * @return
	 * @throws InvalidEditException
	 */
	private NodeGSDAG auxConstructGSDAG(MNode node) throws InvalidEditException {
		// TODO Auto-generated method stub
		NodeGSDAG decNodeGSDAG, chanceNodeGSDAG;
		MNode sucessor;
		NodeGSDAG rootGSDAG;

		TreeSet obsEliminate = node.getObsEliminate();
		TreeSet toEliminate = node.getToEliminate();

		if (hasMNodeToElim(node)) {// A decision node of Marta's graph
			rootGSDAG = auxConstructGSDAGEliminateVariablesMarta(node);
		} else if (isMNodeSink(node)) {// sink node of Marta
			rootGSDAG = null;
		} else {// Chance node in Marta's graph
			rootGSDAG = auxConstructGSDAGChanceMarta(node);
		}
		return rootGSDAG;
	}

	/**
	 * @param mNode
	 * @return
	 */
	private NodeGSDAG auxConstructGSDAGEliminateVariablesMarta(MNode mNode)
			throws InvalidEditException {
		// TODO Auto-generated method stub
		NodeGSDAG decNodeGSDAG=null;
		NodeGSDAG chanceNodeGSDAG;
		NodeGSDAG sucessorOfCurrentLastNodeGSDAG;
		NodeGSDAG existingNodeGSDAG;
		MNode sucessorOfMNode;
		NodeGSDAG root = null;
		NodeGSDAG finalNodeGSDAG;
		NodeGSDAG currentLastNodeGSDAG;
		boolean hasDec;
		
		hasDec = hasMNodeDecisions(mNode);

		//We see if there are decisions and we create a node for them
		if (hasDec){
			hasDec = true;
//			 Create the NodeGSDAG for the decisions
			decNodeGSDAG = new NodeGSDAG(NodeGSDAG.TypeOfNodeGSDAG.DECISION);
			decNodeGSDAG.setVariables(getNamesOfVariables(mNode.getToEliminate()));
			addNode(decNodeGSDAG);
			//We set the return value
			root = decNodeGSDAG;
		}
		
		//We see if there are chance nodes and we create a node for them
		if (hasMNodeChanceObs(mNode)){
//			 Create the NodeGSDAG for the chance variables
			chanceNodeGSDAG = new NodeGSDAG(NodeGSDAG.TypeOfNodeGSDAG.CHANCE);
			chanceNodeGSDAG.setVariables(getNamesOfVariables(mNode
					.getObsEliminate()));

			addNode(chanceNodeGSDAG);
			
			if (hasDec) {

				// Link from the node of decisions to the node of chance
				// variables
				try {
					createLink(decNodeGSDAG, chanceNodeGSDAG);
				} catch (InvalidEditException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{//The chance node is the first in the tree
				root = chanceNodeGSDAG;
			}
			currentLastNodeGSDAG = chanceNodeGSDAG;
		}
		else{
			currentLastNodeGSDAG = decNodeGSDAG;
		}
		
		NodeList childrenOfMNode;
		
		childrenOfMNode =  (mNode.getChildrenNodes());
		if (childrenOfMNode.size()>1){
			NodeGSDAG newBranch;
			
			newBranch = new NodeGSDAG(NodeGSDAG.TypeOfNodeGSDAG.BRANCH);
			this.addNode(newBranch);
//			 Create a link
			try {
				createLink(currentLastNodeGSDAG, newBranch);
			} catch (InvalidEditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			currentLastNodeGSDAG = newBranch;
		}

		
		for (int i=0;i<childrenOfMNode.size();i++){

		// We connect the currentLastNodeGSDAG with the future
		sucessorOfMNode = (MNode) (childrenOfMNode.elementAt(i));

		if (sucessorOfMNode.getParents().size() == 1) {// It's the only parent
			// of sucessor
			// We continue advancing to the right with sucessor
			sucessorOfCurrentLastNodeGSDAG = auxConstructGSDAG(sucessorOfMNode);
		} else {// The sucessor has several parents. The first must create a
				// BRANCH so the rest add link to this
			// The nodeGSDAG for the sucessor can exist so we have
			// to find out it
			existingNodeGSDAG = existingNodeGSDAG(sucessorOfMNode);
			if (existingNodeGSDAG == null) {// Several parents and this is the
											// first so it has to link to a
											// BRANCH
				sucessorOfCurrentLastNodeGSDAG = auxConstructGSDAG(sucessorOfMNode);
				// The first must create, if necessary, a BRANCH
				if (sucessorOfCurrentLastNodeGSDAG.getTypeOfNodeGSDAG() != TypeOfNodeGSDAG.BRANCH) {
					NodeGSDAG aux = sucessorOfCurrentLastNodeGSDAG;
					sucessorOfCurrentLastNodeGSDAG = new NodeGSDAG(
							NodeGSDAG.TypeOfNodeGSDAG.BRANCH);
					this.addNode(sucessorOfCurrentLastNodeGSDAG);
					// Create a link
					try {
						createLink(sucessorOfCurrentLastNodeGSDAG, aux);
					} catch (InvalidEditException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			} else {
				sucessorOfCurrentLastNodeGSDAG = existingNodeGSDAG;
			}
			
		}

		// Link from the node of chance variables to the future
		if (sucessorOfCurrentLastNodeGSDAG == null) {
			// I finish the graph with a branch node in any case
			sucessorOfCurrentLastNodeGSDAG = new NodeGSDAG(
					NodeGSDAG.TypeOfNodeGSDAG.BRANCH);
			this.addNode(sucessorOfCurrentLastNodeGSDAG);
		}

		// Create a link from the currentLastNodeGSDAG to the sucessor
		try {
			createLink(currentLastNodeGSDAG, sucessorOfCurrentLastNodeGSDAG);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}

		return root;

	}

	/**
	 * @param variable
	 * @return
	 */
	private NodeGSDAG auxConstructGSDAGChanceMarta(MNode mNode)
			throws InvalidEditException {
		// TODO Auto-generated method stub
		NodeList childrenOfMNode;
		NodeList parentsOfMNode;
		int numChildrenOfMNode;
		int numParentsOfMNode;
		NodeGSDAG rootGSDAG;
		NodeGSDAG auxNodeGSDAG;

		childrenOfMNode = mNode.getChildrenNodes();
		parentsOfMNode = mNode.getParentNodes();

		numChildrenOfMNode = childrenOfMNode.size();
		numParentsOfMNode = parentsOfMNode.size();

		// We put branch points in three cases:
		// 1. At the end of my gsdag
		// 2. When the node have several children
		// 3. When the node have several parents
		if ((numChildrenOfMNode == 1) && (numParentsOfMNode == 1)) {// We don't
																	// have a
																	// branch
																	// point at
																	// the
																	// beginning
			rootGSDAG = auxConstructGSDAG((MNode) (childrenOfMNode.elementAt(0)));
		} else {// We have a branch point in the root
			// We create a branch node
			rootGSDAG = new NodeGSDAG(NodeGSDAG.TypeOfNodeGSDAG.BRANCH);
			this.addNode(rootGSDAG);
			// We add as children all the graphs of the sucessors
			for (int i = 0; i < numChildrenOfMNode; i++) {
				auxNodeGSDAG = auxConstructGSDAG((MNode) (childrenOfMNode
						.elementAt(i)));
				if (auxNodeGSDAG != null) {
					try {
						createLink(rootGSDAG, auxNodeGSDAG);
					} catch (InvalidEditException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return rootGSDAG;
	}

	public ArrayList<String> getNamesOfVariables(TreeSet treeNames) {
		ArrayList<String> names;

		names = new ArrayList();

		for (Object auxName : treeNames) {
			names.add((String) auxName);
		}
		return names;
	}

	public ArrayList<String> getNamesOfDecisions(MNode node) {
		return getNamesOfVariables(node.getToEliminate());
	}

	/**
	 * Method to obtain the root of the GSDAG constructed by Marta
	 * 
	 * @param gsdagMarta
	 * @return
	 */
	private MNode getRootOfGSDAGMarta(Graph gsdagMarta) {
		NodeList mNodes;
		boolean found = false;
		MNode auxMNode = null;
		MNode root = null;

		mNodes = gsdagMarta.getNodeList();
		// TODO Auto-generated method stub
		for (int i = 0; (i < mNodes.size()) && (found == false); i++) {
			auxMNode = (MNode) (mNodes.elementAt(i));
			if (auxMNode.getParents().size() == 0) {
				found = true;
				root = auxMNode;
			}
		}
		return root;
	}

	public NodeGSDAG existingNodeGSDAG(MNode branchMNode) {
		NodeGSDAG auxNodeGSDAG;
		NodeGSDAG existingNode = null;
		boolean found = false;

		for (int i = 0; (i < this.nodeList.size()) && (found == false); i++) {
			auxNodeGSDAG = (NodeGSDAG) nodeList.elementAt(i);
			if ((auxNodeGSDAG.type == NodeGSDAG.TypeOfNodeGSDAG.BRANCH)
					&& (auxNodeGSDAG.getParentNodes().size() > 0)
					&& (haveSameFuture(auxNodeGSDAG, branchMNode))) {
				// We admit that auxNodeGSDAG is not a BRANCH, but the method
				// that receives the result of this method
				// has to create a branch
				// if
				// ((auxNodeGSDAG.getParentNodes().size()>0)&&(haveSameFuture(auxNodeGSDAG,branchMNode))){
				found = true;
				existingNode = auxNodeGSDAG;
			}
		}
		return existingNode;

	}

	// Method to know if an existing branch node in MLuque's graph is equivalent
	// to other
	// branch in Marta's graph
	private boolean haveSameFuture(NodeGSDAG auxNodeGSDAG, MNode branchMNode) {
		// TODO Auto-generated method stub
		ArrayList<String> futureDecsBranchMNode;
		ArrayList<String> futureDecsBranchNodeGSDAG;
		MNode auxMNode;

		futureDecsBranchMNode = new ArrayList();
		futureDecsBranchNodeGSDAG = new ArrayList();

		// Compute the future decisions of branchMNode
		auxMNode = branchMNode;
		while (auxMNode != null) {
			if (hasMNodeDecisions(auxMNode)) {
				futureDecsBranchMNode.addAll(getNamesOfDecisions(auxMNode));
				auxMNode = (MNode) (auxMNode.getChildrenNodes().elementAt(0));
			} else if (isMNodeSink(auxMNode)) {
				auxMNode = null;
			} else if (auxMNode.getChildrenNodes().size()>0){// Circle in Marta's graph
				auxMNode = (MNode) (auxMNode.getChildrenNodes().elementAt(0));

			}
			else{
				auxMNode = null;
			}
		}

		// Compute the future decisions of auxNodeGSDAG
		while (auxNodeGSDAG != null) {
			if (auxNodeGSDAG.type == NodeGSDAG.TypeOfNodeGSDAG.DECISION) {
				futureDecsBranchNodeGSDAG.addAll(auxNodeGSDAG.getVariables());
			}
			if ((auxNodeGSDAG.getChildrenNodes() != null)
					&& (auxNodeGSDAG.getChildrenNodes().size() > 0)) {
				auxNodeGSDAG = (NodeGSDAG) (auxNodeGSDAG.getChildrenNodes()
						.elementAt(0));
			} else {
				auxNodeGSDAG = null;
			}

		}

		return (futureDecsBranchMNode.containsAll(futureDecsBranchNodeGSDAG))
				&& (futureDecsBranchNodeGSDAG
						.containsAll(futureDecsBranchMNode));

	}

	// It computes the descendant variables of a variable of a NodeGSDAG
	public ArrayList<String> getDescendantVariables(String variable,NodeGSDAG nodeGSDAG) {
		ArrayList<String> variables, descendants;
		int indexOfVariable;
		NodeList auxChildren;
		NodeGSDAG auxNode;

		variables = nodeGSDAG.getVariables();
		
		
		descendants = new ArrayList();
		
		if ((variable!=null)||(variable=="")){

		indexOfVariable = variables.indexOf(variable);

		if (indexOfVariable!=-1){
		// Add the rest of variables of node
		descendants
				.addAll(variables.subList(indexOfVariable, variables.size()));
		}
		}
		else{
			descendants.addAll(variables);
		}

		// Add the variables of the descendant
		auxChildren = nodeGSDAG.getChildrenNodes();

		// Compute the names of the future variables
		while ((auxChildren != null) && (auxChildren.size() > 0)) {
			auxNode = (NodeGSDAG) auxChildren.elementAt(0);
			descendants.addAll(auxNode.getVariables());
			auxChildren = auxNode.getChildrenNodes();
		}

		return descendants;
	}

	//It returns true if the mnode contains variables that have to be eliminated
	boolean hasMNodeToElim(MNode mnode){
		
		TreeSet obsElim;
		boolean has = false;
		TreeSet toElim = mnode.getToEliminate();
		obsElim = mnode.getObsEliminate();
		has = (hasVariablesOfKind(toElim,Node.DECISION)||
		(hasVariablesOfKind(toElim,Node.CHANCE)&&(hasVariablesOfKind(obsElim,Node.CHANCE))));
		
		return has;
		
		
	}
	
//	It returns true if the mnode has decision nodes in the attribute toElim
	boolean hasMNodeDecisions(MNode mnode){
		return hasVariablesOfKind(mnode.getToEliminate(),Node.DECISION);
	}
	
//	It returns true if the mnode has chance nodes in the attribute toElim
	boolean hasMNodeChanceToElim(MNode mnode){
		return hasVariablesOfKind(mnode.getToEliminate(),Node.CHANCE);
	}
	
	//It returns true if the mnode has chance nodes in the attribute obsEliminate
	boolean hasMNodeChanceObs(MNode mnode){
		return hasVariablesOfKind(mnode.getObsEliminate(),Node.CHANCE);
	}
	
	//It returns true if the treeset of variables contains the same of a variable
	//of kind 'kindOfNode'
	boolean hasVariablesOfKind(TreeSet variables,int kindOfNode) {
		boolean has;
		/*return ((mnode.getObsEliminate() != null) && (mnode.getObsEliminate()
				.size() > 0)*/
		
		if (variables!=null){
			if (variables.size()>0){
				if (uid.getNode((String)(variables.first())).getKindOfNode()==kindOfNode){
					has = true;
				}
				else{
					has=false;
				}
			}
			else{
				has = false;
			}
		}
		else{
			has = false;
		}
		return has;
	}

	boolean isMNodeSink(MNode mnode) {
		return ((mnode.getObsEliminate() != null) && (mnode.getObsEliminate()
				.size() == 0));

	}
	
	public NodeGSDAG getLastNodeGSDAG(){
		return getLastNodeGSDAG(root);
	}

	// It returns the last NodeGSDAG (sink node) of the GSDAG
	public NodeGSDAG getLastNodeGSDAG(NodeGSDAG rootGSDAG) {
		NodeGSDAG last = null;
		NodeGSDAG aux;
		boolean found;

		if (rootGSDAG != null) {
			found = false;
			aux = rootGSDAG;
			while (!found) {
				NodeList auxChildren;
				auxChildren = aux.getChildrenNodes();
				if ((auxChildren == null) || (auxChildren.size() == 0)) {
					found = true;
					last = aux;
				} else {
					aux = (NodeGSDAG) auxChildren.elementAt(0);
				}

			}

		} else {
			last = null;
		}
		return last;
	}

	public void print() {
		NodeGSDAG aux;
		NodeList children;

		for (int i = 0; i < this.getNodeList().size(); i++) {
			aux = (NodeGSDAG) getNodeList().elementAt(i);
			if (aux.getTypeOfNodeGSDAG() == TypeOfNodeGSDAG.BRANCH) {
				System.out.println("** Node BRANCH");
			} else {
				System.out.println("** Node " + aux.getVariables().toString());
			}
			System.out.println("*Children:");
			children = aux.getChildrenNodes();
			for (int j = 0; j < children.size(); j++) {
				System.out.println(((NodeGSDAG) children.elementAt(j))
						.getVariables().toString());
			}
			
			if (aux.getMinVarsCoal()!=null){ 
				System.out.println("** Minimum set of variables for coalescence: ");
				aux.printMinVarsCoal();
			}
			
			
			if (aux.getComplementMinVarsCoal()!=null){ 
				System.out.println("** Complement of minimum set of variables for coalescence: "+aux.getComplementMinVarsCoal().toString());
			}
			System.out.println("");
		}
	}

	
	/*public void print2() {
		NodeGSDAG aux;
		NodeList children;

		for (int i = 0; i < this.getNodeList().size(); i++) {
			aux = (NodeGSDAG) getNodeList().elementAt(i);
			if (aux.getTypeOfNodeGSDAG() == TypeOfNodeGSDAG.BRANCH) {
				System.out.println("** Node BRANCH");
			} else {
				System.out.println("** Node " + aux.getVariables().toString());
			}
			System.out.println("*Children:");
			children = aux.getChildrenNodes();
			for (int j = 0; j < children.size(); j++) {
				System.out.println(((NodeGSDAG) children.elementAt(j))
						.getVariables().toString());
			}
			
			System.out.println("** Minimum set of variables for coalescence: "+aux.getMinVarsCoal().toString());
			System.out.println("** Complement of minimum set of variables for coalescence: "+aux.getComplementMinVarsCoal().toString());
			System.out.println("");
		}
	}*/
	public NodeGSDAG getRoot() {
		return root;
	}

	// It intializes the last node of the GSDAG to start the evaluation
	public void initializePotentials(Vector relationList) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		NodeGSDAG lastNodeGSDAG;
		Relation rel;
		int kind;

		lastNodeGSDAG = getLastNodeGSDAG();

		// Set separately the relations of the node
		RelationList probabilityRelations = new RelationList();
		RelationList utilityRelations = new RelationList();

		for (int i = 0; i < relationList.size(); i++) {
			rel = (Relation) relationList.elementAt(i);
			kind = rel.getKind();
			if (kind == Relation.UTILITY) {
				utilityRelations.insertRelation(rel);
			} else {// probability relation
				probabilityRelations.insertRelation(rel);
			}
		}

		// Prepare the last node to start the evaluation
		lastNodeGSDAG.setCurrentRelations(probabilityRelations,utilityRelations);

	}
	
	
	/**
	 * @return the set of variables that precedes to the first branch or decision
	 */
	public NodeList getAPrioriInformation(){
		
		ArrayList<String> vars;
		NodeList nodes;
		
		nodes = new NodeList();
		if (root.type != TypeOfNodeGSDAG.BRANCH){

			vars = root.getVariables();
			
			if (uid.getNode(vars.get(0)).getKindOfNode()==Node.CHANCE){
				for (int i=0;i<vars.size();i++){
					nodes.insertNode(uid.getNode(vars.get(i)));
				}
			}
		}
		return nodes;
	}			
	
	/**
	 * @return true iff the GSDAG represents sequence of decisions and observations where
	 * there is no branches to choose the next decisions. I.e., the evaluation can be performed
	 * by an algorithhm for traditional IDs. The GSDAG is a linear sequence of NodeGSDAGs.
	 */
	public boolean isATraditionalID(){
		NodeGSDAG auxNode;
		auxNode = root;
		boolean finished = false;
		boolean isAnID=true;
		
		
		while (finished==false){
			NodeList children = auxNode.getChildrenNodes();
			
			if ((children==null)||(children.size()==0)){
				finished = true;
				isAnID = true;
			}
			else if (children.size()==1){
				auxNode = (NodeGSDAG) children.elementAt(0);
			}
			else{
				finished = true;
				isAnID = false;
			}
		}
		return isAnID;
		
		
	}

	
	public boolean hasBranchAtBeginning(int minNumChildrenFirstBranch){
		return root.hasBranchAtBeginning(minNumChildrenFirstBranch);
	}
	
	
	
	
	public int getNumberOfPaths(){
		
		return root.getNumberOfPaths();
			
	}

	/**
	 * @param firstToDecide 
	 * @return The number of states of each child of the root the gsdag
	 */
	private ArrayList<Integer> getNumStatesEachDecisionFirstBranch(NodeGSDAG firstToDecide) {
		// TODO Auto-generated method stub
		NodeList children;
		ArrayList<Integer> numStates = null;
		String decName;
		FiniteStates dec;

			children = firstToDecide.getChildrenNodes();
			numStates = new ArrayList();
			for (int i=0;i<children.size();i++){
				decName = ((NodeGSDAG) children.elementAt(i)).getVariables().get(0);
				dec = (FiniteStates) uid.getNode(decName);
				numStates.add(dec.getNumStates());
			}

		return numStates;
	}
	
	
	
	
	
	
	
	public ArrayList<Integer> getNumStatesToChooseFirstInGSDAG(NodeGSDAG firstToDecide) {
		ArrayList<Integer> numStates = null;
		NodeList children;
		String decName;
		FiniteStates dec;
		
		if (firstToDecide.hasBranchAtBeginning(1)){//The root is a branch
			numStates = getNumStatesEachDecisionFirstBranch(firstToDecide);
		}
		else if (firstToDecide.type == NodeGSDAG.TypeOfNodeGSDAG.DECISION){//We suppose the root is a decision
			numStates = new ArrayList();
			decName = firstToDecide.getVariables().get(0);
			dec = (FiniteStates) uid.getNode(decName);
			numStates.add(dec.getNumStates());
			
		}
		else {//CHANCE
			
		}
		return numStates;
	}
	
	public ArrayList<Integer> getNumStatesToChooseFirstInGSDAG() {
		ArrayList<Integer> numStates;
		
		if (root.type != NodeGSDAG.TypeOfNodeGSDAG.CHANCE){
			numStates = getNumStatesToChooseFirstInGSDAG(root);  
		}
		else{
			numStates = getNumStatesToChooseFirstInGSDAG((NodeGSDAG) root.getChildrenNodes().elementAt(0));
		}
		return numStates;
	}
	
	/**
	 * @param index
	 * @return The number of states of the child 'index' of the root the gsdag
	 */
	public int getNumStatesDecisionFirstBranch(int index){
		NodeList children;
		String decName;
		FiniteStates dec;
		int numStates;
		
		children = root.getChildrenNodes();
		if (root.getTypeOfNodeGSDAG()==NodeGSDAG.TypeOfNodeGSDAG.BRANCH){
		decName = ((NodeGSDAG) children.elementAt(index)).getVariables().get(0);
		}
		else{//The root is a decision
			decName = root.getVariables().get(0);
		}
		dec = (FiniteStates) uid.getNode(decName);
		numStates = dec.getNumStates();
		
		return numStates;
	}
	
	
	/**
	 * @param index
	 * @return The decision node of the child 'index' of the root the gsdag
	 */
	public Node getNodeDecisionFirstBranch(NodeGSDAG nodeGSDAG,int index){
		NodeList children;
		String decName;
		FiniteStates dec;
			
		children = nodeGSDAG.getChildrenNodes();
		if (nodeGSDAG.getTypeOfNodeGSDAG()==NodeGSDAG.TypeOfNodeGSDAG.BRANCH){
		decName = ((NodeGSDAG) children.elementAt(index)).getVariables().get(0);
		}
		else{//The root is a decision
			decName = nodeGSDAG.getVariables().get(0);
		}
		dec = (FiniteStates) uid.getNode(decName);
		
		return dec;
	}
	
	/**
	 * @param index
	 * @return The decision node of the child 'index' of the root the gsdag
	 */
	public Node getNodeDecisionFirstBranch(int index){
		NodeList children;
		String decName;
		FiniteStates dec;
		
		NodeGSDAG nodeGSDAG = root;
		children = nodeGSDAG.getChildrenNodes();
		if (nodeGSDAG.getTypeOfNodeGSDAG()==NodeGSDAG.TypeOfNodeGSDAG.BRANCH){
		decName = ((NodeGSDAG) children.elementAt(index)).getVariables().get(0);
		}
		else{//The root is a decision
			decName = nodeGSDAG.getVariables().get(0);
		}
		dec = (FiniteStates) uid.getNode(decName);
		
		
		return dec;
	}
	
	
	/**
	 * @param firstToDecide
	 * @param indexOption Index of the option to be chosen, as a global index over the sum of the options of the different decision
	 * nodes children of the branch
	 * @return the index of the state in the first decision to be made in the GSDAG
	 */
	public int getStateToChooseFirstInGSDAG(NodeGSDAG firstToDecide,int globalIndexOpt) {
		
		NodeList children;
		String decName;
		FiniteStates dec;
		int localIndex=0;
		int auxNumStates;
		boolean found;
		
		
		
		if (firstToDecide.hasBranchAtBeginning(1)){//The root is a branch
			
			int sum = 0;
			
			children = firstToDecide.getChildrenNodes();
			
			found = false;
			for (int i=0;(i<children.size())&&(found==false);i++){
				decName = ((NodeGSDAG) children.elementAt(i)).getVariables().get(0);
				dec = (FiniteStates) uid.getNode(decName);
				auxNumStates= dec.getNumStates();
				if (globalIndexOpt>=sum+auxNumStates){
					sum = sum+auxNumStates;
				}
				else{
					found = true;
					localIndex = globalIndexOpt-sum;
				}
				
			}
		
			
		}
		else if (firstToDecide.type == NodeGSDAG.TypeOfNodeGSDAG.DECISION){//We suppose the root is a decision
			localIndex = globalIndexOpt;
			
		}
		else {//CHANCE
			
		}
		return localIndex;
	}
	
	
	
	/**
	 * @param firstToDecide
	 * @param indexOption Index of the option to be chosen, as a global index over the sum of the options of the different decision
	 * nodes children of the branch
	 * @return
	 */
	public String getDecisionToChooseFirstInGSDAG(NodeGSDAG firstToDecide,int globalIndexOpt) {
		
		NodeList children;
		String decName;
		FiniteStates dec;
		
		int auxNumStates;
		boolean found;
		String nameFoundNode = null;
		
		
		
		if (firstToDecide.hasBranchAtBeginning(1)){//The root is a branch
			
			int sum = 0;
			
			children = firstToDecide.getChildrenNodes();
			
			found = false;
			for (int i=0;(i<children.size())&&(found==false);i++){
				decName = ((NodeGSDAG) children.elementAt(i)).getVariables().get(0);
				dec = (FiniteStates) uid.getNode(decName);
				auxNumStates= dec.getNumStates();
				if (globalIndexOpt>=sum+auxNumStates){
					sum = sum+auxNumStates;
				}
				else{
					found = true;
					nameFoundNode = decName;
				}
				
			}
		
			
		}
		else if (firstToDecide.type == NodeGSDAG.TypeOfNodeGSDAG.DECISION){//We suppose the root is a decision
			nameFoundNode = firstToDecide.getVariables().get(0);
			
		}
		else {//CHANCE
			
		}
		return nameFoundNode;
	}
	

	
	public int getNumDecisionsFirstBranch(){
		int numDecs;
		if (root.type==NodeGSDAG.TypeOfNodeGSDAG.BRANCH){
			numDecs = root.getChildrenNodes().size();
		}
		else{
			numDecs = 1;
		}
		return numDecs;
	}

	/**
	 * @param initialInstant
	 * @return The node of the GSDAG that corresponds after performing
	 * the instantiations 'initialInstant'
	 */
	public NodeGSDAG getNextNode(Configuration initialInstant) {
		// TODO Auto-generated method stub
		NodeGSDAG node = null;
		ArrayList<String> vars;
		boolean isValidInstant=true;
		
		
		if ((initialInstant==null)||(initialInstant.size()==0)){
			node = this.root;
		}
		else{
			vars = root.getVariables();
			if (vars.size()==initialInstant.size()){
				for(String auxVar:vars){
					if (initialInstant.indexOf(auxVar)==-1){
						isValidInstant=false;
					}
				}
			}
			else{
				isValidInstant = false;
				
			}
			if (isValidInstant==false){
				System.out.println("Instantiations must be performed over the full past of the first decision");
				System.exit(-1);
			}
			else{
				node = (NodeGSDAG) this.root.getChildrenNodes().elementAt(0);
			}
			
		}
		return node;
	}

	public void initializePotentials(Vector relationList,
			Configuration configuration) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		NodeGSDAG lastNodeGSDAG;
		Relation rel;
		int kind;
		Relation relRestricted;

		lastNodeGSDAG = getLastNodeGSDAG();

		// Set separately the relations of the node
		RelationList probabilityRelations = new RelationList();
		RelationList utilityRelations = new RelationList();

		for (int i = 0; i < relationList.size(); i++) {
			rel = (Relation) relationList.elementAt(i);
			kind = rel.getKind();
			relRestricted = rel.restrict(configuration);
			if (kind == Relation.UTILITY) {
				utilityRelations.insertRelation(relRestricted);
			} else {// probability relation
				probabilityRelations.insertRelation(relRestricted);
			}
		}

		// Prepare the last node to start the evaluation
		lastNodeGSDAG.setCurrentRelations(probabilityRelations, utilityRelations);

		
	}

	/**
	 * It computes the minimum set of variables for coalescence for each node of the gsdag and for each variable
	 */
	public void obtainMinSetOfVarsCoal() {
		// TODO Auto-generated method stub
		NodeGSDAG last;
		NodeGSDAG nodeGSDAG;
		ArrayList<String> vars;
		NodeList minNodes;
	
		last = this.getLastNodeGSDAG();
		for (int i=0;i<this.nodeList.size();i++){
			nodeGSDAG = (NodeGSDAG) nodeList.getNodes().elementAt(i);
			nodeGSDAG.setMinVarsCoal(new ArrayList<NodeList>());
			nodeGSDAG.setComplementMinVarsCoal(new ArrayList<NodeList>());
			vars = nodeGSDAG.variables;
			for (int j=0;j<vars.size();j++){
				//WE insert the minimum set of vars for coalescence
				minNodes = auxObtainMinSetOfVarsCoal(nodeGSDAG,vars.get(j),last);
				nodeGSDAG.getMinVarsCoal().add(minNodes);
				
			}
			
		}
		
		
	}
	
	
	/**
	 * @param newNameOfVariable 
	 * @return The minimum set of variables whose coincidence between scenarios is required for coalescence
	 */
	private NodeList auxObtainMinSetOfVarsCoal(NodeGSDAG nodeGSDAG,String nameOfVariable,NodeGSDAG last){
		ArrayList<String> varsOfFuture;
	
		NodeList varsOfRelations;
		RelationList rels=null;
		RelationList relsP,relsU;
		
	
		
		//Compute the variables of the future
		varsOfFuture=getDescendantVariables(nameOfVariable,nodeGSDAG);
		//The variable of this node is not considered part of the future
		varsOfFuture.remove(nameOfVariable);
		
		//Join all the relations
		rels = new RelationList();
		relsP= last.getCurrentProbabilityRelations();
		relsU= last.getCurrentUtilityRelations();
		for (int i=0;i<relsP.size();i++){
			rels.insertRelation(relsP.elementAt(i));
		}
		for (int i=0;i<relsU.size();i++){
			rels.insertRelation(relsU.elementAt(i));
		}

//		Obtain the relations where the vars in "varsOfFuture" appear
		varsOfRelations=obtainVarsOfRelationsWhereAppear(rels,varsOfFuture);
		//Remove the vars of the future from varsOfRelations
		for (String nameVarFuture:varsOfFuture){
			varsOfRelations.removeNode(varsOfRelations.getId(nameVarFuture));
		}
		//System.out.println("* Coalescence requires coincidence in variables:");
		for (int i=0;i<varsOfRelations.size();i++){
			System.out.println(varsOfRelations.elementAt(i).getName());
		}
		
		return varsOfRelations;
	}

	
	private static NodeList obtainVarsOfRelationsWhereAppear(RelationList rels, ArrayList<String> varsOfFuture) {
		NodeList varsAppear;
		Relation rel;
		String auxName;
		boolean appears;
		NodeList varsRel;
		Node auxNode;
	
		varsAppear = new NodeList();
		
		//For each relation
		for (int i=0;i<rels.size();i++){
			rel=rels.elementAt(i);
			varsRel = rel.getVariables();
			appears = false;
			//We look if a variable of the future is included in it
			for (int j=0;((j<varsOfFuture.size())&&(appears==false));j++){
				auxName = varsOfFuture.get(j);
				if (varsRel.getId(auxName)!=-1){
					//We insert in varsAppear all the variables of the relation
					appears = true;
					for (int k=0;k<varsRel.size();k++){
						auxNode = varsRel.elementAt(k);
						if (auxNode.getKindOfNode()!=Node.UTILITY){
						if (varsAppear.getId(auxNode)==-1){
						varsAppear.insertNode(varsRel.elementAt(k));
						}
						}
					}
				}
			}
		}
		return varsAppear;
	}

	public boolean chilrenOfRootHaveOnlyOneDecision() {
		// TODO Auto-generated method stub
		NodeList children;
		boolean onlyOneDec = true;
		children = root.getChildrenNodes();
		NodeGSDAG auxChild;
		ArrayList<String> auxNames;
		
		
		for (int i=0;(i<children.size())&&onlyOneDec;i++){
			auxChild = (NodeGSDAG)children.elementAt(i);
			auxNames = auxChild.getVariables();
			if (auxNames.size()==1){
				onlyOneDec = this.uid.getNode(auxNames.get(0)).getKindOfNode()==Node.DECISION;
			}
			else{
				onlyOneDec = false;
			}
		}
		return onlyOneDec;
		
	}

	public void prepareGsdagToPaintIt() {
		// TODO Auto-generated method stub
		
		NodeList nodes = getNodeList();
		
		for (int i=0;i<nodes.size();i++){
			NodeGSDAG iNode = (NodeGSDAG) nodes.elementAt(i);
			iNode.setNameToPaintIt();
			iNode.setFont("Helvetica");
			iNode.setPosX(500);
			iNode.setPosY(500);
			iNode.setAxis(30,20+iNode.getName().length()*8);
			//Change the type of node for drawing it
			switch (iNode.getTypeOfNodeGSDAG()){
			case CHANCE:
				iNode.setKindOfNode(Node.CHANCE);
				break;
			case DECISION:
				iNode.setKindOfNode(Node.DECISION);
				break;
			case BRANCH:
				iNode.setKindOfNode(Node.DECISION);
				break;
			}
			
			
		}
		
		removeBranchNodesHavingLessThanTwoChildren();
		positionNodesWithLeftToRightLayout();
		
		
	}
	
	
	/**
	 * This methods removes the branch nodes that don't have at least two children. This simplification of the GS-DAG is used in the 
	 * inference panel of Elvira
	 */
	private void removeBranchNodesHavingLessThanTwoChildren() {
		// TODO Auto-generated method stub
		removeBranchNodesWithoutChildren();
		removeBranchNodesWithAnOnlyChild();
	}
	
	private void removeBranchNodesWithAnOnlyChild() {
		// TODO Auto-generated method stub
		
		ArrayList<NodeGSDAG> nodes;
		NodeGSDAG iNode;
		
		
		nodes = getNodesWithNumberOfChildrenAndType(1,TypeOfNodeGSDAG.BRANCH);
		for (NodeGSDAG auxNode:nodes)
		{
			NodeGSDAG auxChild = (NodeGSDAG) auxNode.getChildrenNodes().elementAt(0);
			createLinksFrom(new Vector(auxNode.getParentNodes().getNodes()),auxChild);
			this.removeNode(auxNode);
		}
		
		
	}

	private void createLinksFrom(Vector parents, NodeGSDAG auxChild) {
		// TODO Auto-generated method stub
		for (int i=0;i<parents.size();i++){
			try {
				createLink((NodeGSDAG) parents.elementAt(i),auxChild);
			} catch (InvalidEditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * This methods removes the branch nodes wihtout children. This simplification of the GS-DAG is used in the 
	 * inference panel of Elvira
	 */
	private void removeBranchNodesWithoutChildren() {
		// TODO Auto-generated method stub
		ArrayList<NodeGSDAG> nodes;
		NodeGSDAG iNode;
		
		
		nodes = getNodesWithNumberOfChildrenAndType(0,TypeOfNodeGSDAG.BRANCH);
		for (NodeGSDAG auxNode:nodes){
			
			this.removeNode(auxNode);
		}
		

	}
	
	public void removeNode (NodeGSDAG node) {
		
	
		
	
	//Remove its links
	//Remove the incoming links
		
	Vector auxToVector=new Vector(node.getParents().getLinks());
	
	for (int i=0;i<auxToVector.size();i++){
		Link auxLinkTo =(Link) auxToVector.elementAt(i);
		try {
			removeLink(auxLinkTo);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Remove the outgoing links
	Vector auxFromVector = new Vector(node.getChildren().getLinks());
	for (int i=0;i<auxFromVector.size();i++){
		Link auxLinkFrom =(Link) auxFromVector.elementAt(i);
		try {
			removeLink(auxLinkFrom);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Remove the node from the list of nodes
	boolean found = false;
	for (int pos=0;(pos<nodeList.size())&&!found;pos++){
	 if (nodeList.elementAt(pos)==node){
		 found = true;
		 nodeList.getNodes().removeElementAt(pos);
		 }
	 }
	
	}

	private ArrayList<NodeGSDAG> getNodesWithNumberOfChildrenAndType(int numChildren, TypeOfNodeGSDAG typeOfNodeGSDAG) {
		// TODO Auto-generated method stub
		NodeList nodes;
		NodeGSDAG iNode;
		ArrayList<NodeGSDAG> nodesSelected;
		
		nodesSelected = new ArrayList<NodeGSDAG>();
		
		nodes = this.getNodeList(); 
		
		for (int i=0;i<nodes.size();i++){
			iNode = (NodeGSDAG) nodes.elementAt(i);
			if ((iNode.getTypeOfNodeGSDAG()==typeOfNodeGSDAG)&&(iNode.getChildren().size()==numChildren)){
				nodesSelected.add(iNode);
			}
		}
		
		return nodesSelected;
	}

	private void positionNodesWithLeftToRightLayout() {
		// TODO Auto-generated method stub
			ArrayList<ArrayList<Node>> levels;
			NodeList nodes;
			ArrayList<Node> auxLevel;
			int minY = Integer.MAX_VALUE;
			int y;
			int verticalSeparation=100;
			int verticalStart=50;
			int horizontalSeparation;
			int maxWidthOfNodes=Integer.MIN_VALUE;
			int auxWidth;
			
			
			
			//First, we position the nodes by levels
			
			//Obtain the levels
			nodes = this.getNodeList();
			//levels = new ArrayList<ArrayList<Node>>();
			for (int i=0;i<nodes.size();i++){
				auxWidth = this.getNodeList().elementAt(i).getHigherAxis();
				if (auxWidth>maxWidthOfNodes){
					maxWidthOfNodes = auxWidth;
				}
			}
			
			
			
			for (int i=0;i<nodes.size();i++){
				auxLevel = this.nodesAtDepth(i);
				double yStartLevel = ((double)(auxLevel.size()-1))/2.0;
				for (int j=0;j<auxLevel.size();j++){
					Node auxNode = auxLevel.get(j);
					auxNode.setPosX(50+i*(50+maxWidthOfNodes));
					y = (int)((yStartLevel-j)*verticalSeparation);
					auxNode.setPosY(y);
					
					if (y<minY){
						minY = y;
					}
				
				}
			}
			
			
			//We move the nodes according to the upper node in the inference panel (its Y is minY)
			
			int verticalOffset;
			verticalOffset = verticalStart - minY;
			for (int i=0;i<nodes.size();i++){
				Node auxNode = nodes.elementAt(i);			
				auxNode.setPosY(auxNode.getPosY()+verticalOffset);
			}
			
		}




	
	
}
