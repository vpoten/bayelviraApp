package elvira.inference.uids;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Graph;
import elvira.InvalidEditException;
import elvira.Link;
import elvira.Node;
import elvira.NodeList;
import elvira.UID;
import elvira.inference.clustering.LazyPenniless;
import elvira.inference.clustering.ShenoyShaferPropagation;
import elvira.inference.uids.NodeAOUID.TypeOfNodeAOUID;
import elvira.inference.uids.NodeGSDAG.TypeOfNodeGSDAG;
import elvira.tools.statistics.roots.RealRoot;

public class GraphAOUID extends Graph{
	//ArrayList<NodeAOUID> open;
	//ArrayList<NodeAOUID> closed;
	protected GSDAG gsdag;
	
	//Root of the graph of search (the solution graph is included in this through pointers)
	protected NodeAOUID root;
	
	public int selectedHeuristic[]={0,0};
	public boolean applyDynamicWeighting;
	//Number of nodes in the tree
	protected int numberOfNodes;
	
	//ShenoyShaferPropagation to compute the conditional probabilities
	ShenoyShaferPropagation	ssp;
	
	 /**
     * used for depuration
     */
     public static boolean debug;

	
	
	
	public GraphAOUID(){
		
	}
		
	public GraphAOUID(UID uid,GSDAG gsdag2,boolean applyDinamicWeighting2)  {
		NodeAOUID initialState;
		// TODO Auto-generated constructor stub
		/*open = new ArrayList();
		closed = new ArrayList();*/
		
	    //Compile the UID
	    uid.setCompiledPotentialList(new Vector());
				
		gsdag = gsdag2;
		
		applyDynamicWeighting = applyDinamicWeighting2;
		
		System.out.println("First state of the tree of search");
		initialState = new NodeAOUID(uid,gsdag,this);
		
		addNode(initialState);
		
		root = initialState;
		
		//open.add(initialState);
		initialState.setOpen(true);
		
		numberOfNodes = 1;
				
		
	}
	
	/**
	 * @return The list of candidates to expand in the global tree
	 */
	public ArrayList<NodeAOUID> obtainCandidatesToExpand(){
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		
		nodesOfPartialSolution = obtainNodesOfPartialSolution();
		candidates = new ArrayList();
		
		//The set of candidates is the intersection between the sets open and nodesPartialSolution
		for (NodeAOUID auxNodeSolution: nodesOfPartialSolution){
			if (isOpen(auxNodeSolution)){
				candidates.add(auxNodeSolution);
			}
		}
		
		return candidates;
		
		
		
	}
	
	
	/**
	 * @return The list of candidates to expand in the global tree
	 */
	public ArrayList<NodeAOUID> obtainOpenNodes(){
		ArrayList<NodeAOUID> openNodes;
		NodeList nodes;
		NodeAOUID auxNode;
		
		
		nodes = this.getNodeList();
		openNodes = new ArrayList();
		
		//The set of candidates is the intersection between the sets open and nodesPartialSolution
		for (int i=0;i<nodes.size();i++){
			auxNode = (NodeAOUID) nodes.elementAt(i);
			if (isOpen(auxNode)){
				openNodes.add(auxNode);
			}
		}
		
		return openNodes;
		
		
		
	}
	
	/**
	 * @return The list of candidates to expand in the tree rooted by 'nodeAOUID'
	 */
	public ArrayList<NodeAOUID> obtainCandidatesToExpand(NodeAOUID nodeAOUID) {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		
		nodesOfPartialSolution = obtainNodesOfPartialSolution(nodeAOUID);
		candidates = new ArrayList();
		
		//The set of candidates is the intersection between the sets open and nodesPartialSolution
		for (NodeAOUID auxNodeSolution: nodesOfPartialSolution){
			if (isOpen(auxNodeSolution)){
				candidates.add(auxNodeSolution);
			}
		}
		
		return candidates;
		
		
	}
	
/*	public ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpand() {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		
		nodesOfPartialSolution = obtainNodesOfPartialSolution();
		candidates = new ArrayList();
		
		//The set of candidates is the intersection between the sets open and nodesPartialSolution
		for (NodeAOUID auxNodeSolution: nodesOfPartialSolution){
			if (isOpen(auxNodeSolution)){
				candidates.add(auxNodeSolution);
				return candidates;
			}
		}
		return candidates;
	
	}*/
	
	public ArrayList<NodeAOUID> improvedObtainAnOnlyCandidateToExpand() {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		
		nodesOfPartialSolution = obtainNodesOfPartialSolution();
		candidates = new ArrayList();
		
		//The set of candidates is the intersection between the sets open and nodesPartialSolution
		for (NodeAOUID auxNodeSolution: nodesOfPartialSolution){
			if (isOpen(auxNodeSolution)){
				candidates.add(auxNodeSolution);
				return candidates;
			}
		}
		return candidates;
	
	}
	
	/**
	 * @return The list of candidates to expand in the tree rooted by 'nodeAOUID'
	 */
	public ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpand(NodeAOUID nodeAOUID) {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		
		nodesOfPartialSolution = obtainNodesOfPartialSolution(nodeAOUID);
		candidates = new ArrayList();
		
		//The set of candidates is the intersection between the sets open and nodesPartialSolution
		for (NodeAOUID auxNodeSolution: nodesOfPartialSolution){
			if (isOpen(auxNodeSolution)){
				candidates.add(auxNodeSolution);
				return candidates;
			}
		}
		
		return candidates;
		
		
	}
	

	
	public boolean isOpen(NodeAOUID n){
		/*for (NodeAOUID auxOpenNode:open){
			if (auxOpenNode == n) return true;
		}
		return false;
*/	
		return (n.isOpen());
	}

	public ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpand() {
		return obtainAnOnlyCandidateToExpandHigherProb();
		//return obtainAnOnlyCandidateToExpandLessDeep();
		//return obtainAnOnlyCandidateToExpandHighestWeigth();
	}
	
	protected ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpandHighestWeigth() {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		NodeAOUID foundNode;
		
	candidates = new ArrayList();
		
		//foundNode = obtainAnOpenNodeOfPartialSolution();
		//foundNode = obtainAnOpenNodeOfPartialSolutionInBreadth();
		//foundNode=obtainTheOpenNodeWithHighestProbability();
		//foundNode = obtainTheOpenNodeLessDeep();
		foundNode=obtainTheOpenNodeWithHighestWeigthInEU();
		
		if (foundNode!=null){
			candidates.add(foundNode);
		}
		
		return candidates;
	}
	
	
	private ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpandLessDeep() {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		NodeAOUID foundNode;
		
	candidates = new ArrayList();
		
		foundNode = obtainTheOpenNodeLessDeep();
			
		if (foundNode!=null){
			candidates.add(foundNode);
		}
		
		return candidates;
	}

	public ArrayList<NodeAOUID> obtainAnOnlyCandidateToExpandHigherProb() {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> nodesOfPartialSolution;
		ArrayList<NodeAOUID> candidates;
		NodeAOUID foundNode;
		
		candidates = new ArrayList();
			
		foundNode=obtainAnOpenNodeWithHighestProbability();
		
		if (foundNode!=null){
			candidates.add(foundNode);
		}
		
		return candidates;
	}
	
	//It returns a list with all the nodes of the current partial optimal solution. 
	ArrayList<NodeAOUID> obtainNodesOfPartialSolution() {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> auxNodes;
		
		auxNodes = new ArrayList();
		root.auxObtainNodesOfPartialSolution(auxNodes);
			
		return auxNodes;
	}
	
	
	//It returns a list with all the nodes of the current partial optimal solution. 
	/*NodeAOUID obtainAnOpenNodeOfPartialSolution() {
		// TODO Auto-generated method stub
		
		return root.obtainAnOpenNodeOfPartialSolution();
			
		
	}*/
	
	/*protected NodeAOUID obtainAnOpenNodeOfPartialSolutionInBreadth() {
		// TODO Auto-generated method stub
		LinkedBlockingQueue<NodeAOUID> queueOfNodes;
		NodeAOUID auxNode;
		NodeList children;
		NodeAOUID foundNode=null;
		boolean found;
		
		queueOfNodes = new LinkedBlockingQueue();
		
		queueOfNodes.add(root);
		
		while (queueOfNodes.size()>0){
			auxNode = queueOfNodes.remove();
			if (auxNode.isOpen()){
				foundNode=auxNode;
				found=true;
			}
			else{
				if (auxNode.getChildren().size()>0){
					switch(auxNode.type){
					case BRANCH:
					case DECISION:
						queueOfNodes.add(auxNode.bestChildInPartialSolution);
						break;
					case CHANCE:
						children = auxNode.getChildrenNodes();
						for(int i=0;(i<children.size());i++){
							queueOfNodes.add((NodeAOUID)children.elementAt(i));
						}
						break;
					}	
				}
			}
		}
		return foundNode;
		
	}

	*/

/*protected NodeAOUID obtainTheOpenNodeWithHighestProbability() {
	// TODO Auto-generated method stub
	ArrayList<NodeAOUID> open;
	NodeAOUID auxNode;
	ArrayList<Double> probsOfOpen;
	double max;
	double auxProb;
	int posMax;
	double probAuxNode;
		
	
	open = new ArrayList();
	probsOfOpen = new ArrayList();
	auxNode = root;
	
	System.out.println("Let us choose a node to be expanded");
	
	//Compute open and their probabilities
	for (int i=0;i<this.getNodeList().size();i++){
		auxNode = (NodeAOUID) getNodeList().elementAt(i);
		if (auxNode.isOpen()){
			probAuxNode = auxNode.getProbability();
			
			if (probAuxNode>0.0){
				probsOfOpen.add(probAuxNode);
				open.add(auxNode);
				System.out.println("The probability of the node "+auxNode.getInstantiations().toString()+": "+probAuxNode);
				//System.out.println("and its heuristic: "+auxNode.f);
			}
			
		}
	}
	
	if (open.size()>0){
	//Look for the maximum of probabilities
	max = Double.MIN_VALUE;
	posMax = 0;
	for (int i=0;i<probsOfOpen.size();i++){
		auxProb = probsOfOpen.get(i);
		if (auxProb>max){
			max = auxProb;
			posMax = i;
		}
		
	}
	return open.get(posMax);
	}
	else{
		return null;
	}
		
}*/

/**
 * @return an open node with highest probability. If there are several nodes with the same
 * value then the tie is broken randomly
 */
protected NodeAOUID obtainAnOpenNodeWithHighestProbability() {
	// TODO Auto-generated method stub
	ArrayList<NodeAOUID> open;
	NodeAOUID auxNode;
	ArrayList<Double> probsOfOpen;
	double max;
	double auxProb;
	double probAuxNode;
	NodeAOUID auxOpen;
	ArrayList<NodeAOUID> listOfMax = null;
	NodeAOUID chosenNode;
		
	
	open = new ArrayList();
	probsOfOpen = new ArrayList();
	auxNode = root;
	
	if (isDebug()){
		System.out.println("Let us choose a node to be expanded");
		System.out.println("Looking for the node to expand with the highest probability");
	}
	
	//Compute open and their probabilities
	for (int i=0;i<this.getNodeList().size();i++){
		auxNode = (NodeAOUID) getNodeList().elementAt(i);
		if (auxNode.isOpen()){
			probAuxNode = auxNode.getProbability();
			
			if (probAuxNode>0.0){
				probsOfOpen.add(probAuxNode);
				open.add(auxNode);
				if (isDebug()){
					System.out.println("The probability of the node "+auxNode.getInstantiations().toString()+": "+probAuxNode);
				//System.out.println("and its heuristic: "+auxNode.f);
				}
			}
			
		}
	}
	
	if (open.size()>0){
	//Look for the maximum of probabilities
	max = Double.MIN_VALUE;
	
	for (int i=0;i<probsOfOpen.size();i++){
		auxProb = probsOfOpen.get(i);
		auxOpen = open.get(i);
		if (auxProb>max){
			listOfMax = new ArrayList();
			listOfMax.add(auxOpen);
			max = auxProb;
		}
		else if (auxProb==max){
			listOfMax.add(auxOpen);
		}
		
	}
	
	
	chosenNode = (((listOfMax!=null)&&(listOfMax.size()>0))?((NodeAOUID) NodeAOUID.selectChildRandomlyWhenTie(listOfMax)):null);
	
	}
	else{
		chosenNode = null;
	}
	return chosenNode;
		
}


/**
 * @return An open node randomly selected with uniform distribution, giving identical probability of being selected
 * to every open node. Open nodes which can be pruned because of their bounds are obviously discarded.
 */
protected NodeAOUID obtainAnOpenNodeRandomly() {
	// TODO Auto-generated method stub
	// TODO Auto-generated method stub
	ArrayList<NodeAOUID> open;
	NodeAOUID auxNode;
	ArrayList<Double> probsOfOpen;
	double max;
	double auxProb;
	double probAuxNode;
	NodeAOUID auxOpen;
	
	NodeAOUID chosenNode;
		
	
	open = new ArrayList();
	probsOfOpen = new ArrayList();
	auxNode = root;
	
	System.out.println("Let us choose a node to be expanded");
	System.out.println("Looking for the node to expand with the highest probability");
	
	//Compute open and their probabilities
	for (int i=0;i<this.getNodeList().size();i++){
		auxNode = (NodeAOUID) getNodeList().elementAt(i);
		if (auxNode.isOpen()){
			probAuxNode = auxNode.getProbability();
			
			if (probAuxNode>0.0){
				probsOfOpen.add(probAuxNode);
				open.add(auxNode);
				System.out.println("The probability of the node "+auxNode.getInstantiations().toString()+": "+probAuxNode);
				//System.out.println("and its heuristic: "+auxNode.f);
			}
			
		}
	}
	
	if (open.size()>0){
		chosenNode = (((open!=null)&&(open.size()>0))?((NodeAOUID) NodeAOUID.selectChildRandomlyWhenTie(open)):null);
	}
	else{
		chosenNode = null;
	}
	return chosenNode;

	
}


protected NodeAOUID obtainTheOpenNodeLessDeep() {
	// TODO Auto-generated method stub
	ArrayList<NodeAOUID> open;
	NodeAOUID auxNode;
	ArrayList<Integer> depths;
	double min;
	int auxDepth;
	int posMin;
	
		
	
	open = new ArrayList();
	depths = new ArrayList();
	auxNode = root;
	
	//Compute open and their probabilities
	for (int i=0;i<this.getNodeList().size();i++){
		auxNode = (NodeAOUID) getNodeList().elementAt(i);
		if (auxNode.isOpen()){
			auxDepth = auxNode.getInstantiations().size();
			
			depths.add(auxDepth);
				open.add(auxNode);
				
			}
			
		}
	
	
	if (open.size()>0){
	//Look for the minimum depth
	min = Integer.MAX_VALUE;
	posMin = 0;
	for (int i=0;i<depths.size();i++){
		auxDepth = depths.get(i);
		if (auxDepth<min){
			min = auxDepth;
			posMin = i;
		}
	}
	return open.get(posMin);
	}
	else{
		return null;
	}
		
}

protected NodeAOUID obtainTheOpenNodeWithHighestWeigthInEU() {
	// TODO Auto-generated method stub
	ArrayList<NodeAOUID> open;
	NodeAOUID auxNode;
	ArrayList<Double> weigthsOfOpen;
	double max;
	double auxProb;
	int posMax;
	double probAuxNode;
	double weigth;
	double auxF;

		
	
	open = new ArrayList();
	weigthsOfOpen = new ArrayList();
	auxNode = root;
	
	//Compute open and their probabilities
	for (int i=0;i<this.getNodeList().size();i++){
		auxNode = (NodeAOUID) getNodeList().elementAt(i);
		if (auxNode.isOpen()){
			probAuxNode = auxNode.getProbability();
			
			if (probAuxNode>0.0){
				auxF = auxNode.f;
				weigth = probAuxNode*auxF;
				weigthsOfOpen.add(new Double(weigth));
				open.add(auxNode);
				System.out.println("The prob of the node "+auxNode.getInstantiations().toString()+": "+probAuxNode+", and its heuristic: "+auxF+". Its weigth: "+weigth);
			}
			
		}
	}
	
	if (open.size()>0){
	//Look for the maximum of probabilities
	max = Double.MIN_VALUE;
	posMax = 0;
	for (int i=0;i<weigthsOfOpen.size();i++){
		auxProb = weigthsOfOpen.get(i);
		if (auxProb>max){
			max = auxProb;
			posMax = i;
		}
	}
	return open.get(posMax);
	}
	else{
		return null;
	}
		
}
	
	
	  
	/**
	 * @param node
	 * @return list with all the nodes of the partial optimal solution rooted by 'node'
	 */
	protected ArrayList<NodeAOUID> obtainNodesOfPartialSolution(NodeAOUID node) {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> auxNodes;
		
		auxNodes = new ArrayList();
		node.auxObtainNodesOfPartialSolution(auxNodes);
			
		return auxNodes;
	}
	
	
	
	//Expand a node in the graph of search, updating all the data structures
	public void expand(NodeAOUID nodeToExpand) {
		ArrayList<NodeAOUID> sucessors;
		boolean modifiedF;
		// TODO Auto-generated method stub
		//Remove nodeToExpand from open
		/*open.remove(nodeToExpand);
		closed.add(nodeToExpand);*/
		nodeToExpand.setOpen(false);
		sucessors = nodeToExpand.generateSucessors();

		
		if (sucessors.size()>0){
		
		for (NodeAOUID auxSuc:sucessors){
			//			 Link from the node of decisions to the node of chance
			// variables
			try {
				createLink(nodeToExpand,auxSuc);
			} catch (InvalidEditException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		nodeToExpand.setSolved(nodeToExpand.hasAllChildrenSolved());
		nodeToExpand.updateHeuristicInNode();
		}
		
		
	}
	
	/**
	 * Inserts a node in the graph if it doesn't exist in the graph.
	 * @param n the node to insert.
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

	//It looks for a nodeAOUID with the same instantations that the configuration joined
	//with the parameters
/*	public NodeAOUID getNodeAOUID(Configuration instantiations, FiniteStates nodeUID, int val) {
		// TODO Auto-generated method stub
		Configuration auxConf,fullConf;
		NodeAOUID auxNodeAOUID;
		boolean found = false;
		NodeAOUID foundNodeAOUID = null;
		fullConf = instantiations.duplicate();
		fullConf.insert(nodeUID,val);
		
		//It looks for in the list of nodeAOUIDs
		for (int i=0;(i<this.nodeList.size())&&(found == false);i++){
			auxNodeAOUID = (NodeAOUID) nodeList.elementAt(i);
			auxConf = auxNodeAOUID.getInstantiations();
			if (fullConf.equals(auxConf)){
				foundNodeAOUID = auxNodeAOUID;
				found = true;
			}
		}
		return foundNodeAOUID;
	
	}*/
	
	public NodeAOUID improvedGetNodeAOUID(Configuration instantiations, FiniteStates nodeUID, int val){
		// TODO Auto-generated method stub
		Configuration auxConf,fullConf;
		NodeAOUID auxNodeAOUID;
		boolean found = false;
		NodeAOUID foundNodeAOUID = null;
		fullConf = instantiations.duplicate();
		fullConf.insert(nodeUID,val);


		return root.improvedGetNodeAOUID(fullConf);
		
	}
	
	/*public NodeAOUID getNodeAOUID(Configuration instantiations, FiniteStates nodeUID, int val) {
		return null;
	}
*/
	public GSDAG getGsdag() {
		return gsdag;
	}

	public void setGsdag(GSDAG gsdag) {
		this.gsdag = gsdag;
	}

	
	public void print(){
		NodeAOUID aux;
		NodeList children;
		
	
		for (int i=0;i<this.getNodeList().size();i++){
			aux = (NodeAOUID) getNodeList().elementAt(i);
			if (aux.type==TypeOfNodeAOUID.BRANCH){
				System.out.println("** Node BRANCH"+aux.getInstantiations().toString()+" F: "+aux.getF());
			}
			else if (aux.type==TypeOfNodeAOUID.DECISION){
				System.out.println("** Node "+aux.getInstantiations().toString()+" F: "+aux.getF());
			}
			else{
				System.out.println("** Node "+aux.getInstantiations().toString()+" F: "+aux.getF()+". Probabilities: "+aux.conditionalProbs.toString());
			}
			System.out.println("*Children:");
			children = aux.getChildrenNodes();
			for (int j=0;j<children.size();j++){
				System.out.println(((NodeAOUID)children.elementAt(j)).getInstantiations().toString());
			}
			System.out.println("");
		}
	}

	
	public int countNumberOfNodesInTheTree(){
		//return (open.size()+closed.size());
		return root.getNumNodesInTheTree();
	}
	
	public int getDepth(){
		return root.getDepth();
	}
	
	public double getEffectiveBranchingFactor(){
		return getEffectiveBranchingFactor(false);
	}
	
	public double getEffectiveBranchingFactor(boolean exact){
		BranchingFactor bf;
		int depth;
		int numNodes;

		
		depth = getDepth();
		numNodes = getNumberOfNodes();
		
		if (depth == numNodes)
			return 1;
		else {
			if (exact){
			bf = new BranchingFactor(depth, numNodes);
			// We look for the zero of the function in the interval [1,10]
			return RealRoot.bisect(bf, 1.0, 10.0);
			}
			else{
				return Math.pow(numNodes,1.0/depth);
			}
		}
		
		//return Math.pow(getNumNodesInTheTree(),1.0/getDepth());
		
	}

	public int getFirstDecisionMadeInTheTree() {
		if (root.getTypeOfNodeAOUID()==NodeAOUID.TypeOfNodeAOUID.BRANCH){
			return root.getIndexOfBestChild();
		}
		else{//It is DECISION
			return 0;
		}
	}
	
	
	
	public void printValueOfFOfChildrenOfRoot() {
		// TODO Auto-generated method stub
		NodeList children;
		NodeAOUID auxNode;
		
		System.out.println("Value of F of the children of the root:");
		children = root.getChildrenNodes();
		for (int i=0;i<children.size();i++){
			auxNode = (NodeAOUID)(children.elementAt(i));
			System.out.println(auxNode.nameOfVariable+":"+auxNode.f);
		}
	}
	//public int getBranchinEffectiveFactor

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}

	public ShenoyShaferPropagation getSSP() {
		return ssp;
	}

	public int getFirstOptionChosenInTheTree(ArrayList<Integer> accumNumStates) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
//		 TODO Auto-generated method stub
		int first=-1;
		int state;
		int firstOption;
		
		Random r = new Random();

		//Decision selected
		first = getFirstDecisionMadeInTheTree();
		if (first==-1){
			//If the first branch is not selecting anything we select randomly a decision
			first = r.nextInt(gsdag.getNumDecisionsFirstBranch());
		}
		
		//Option selected
		if (root.getTypeOfNodeAOUID()==NodeAOUID.TypeOfNodeAOUID.BRANCH){
		state = ((NodeAOUID)root.getChildrenNodes().elementAt(first)).getIndexOfBestChild();
		}
		else{//It is a TypeOfNodeAOUID.DECISION
			state = ((NodeAOUID)root).getIndexOfBestChild();
		}
		if (state==-1){
//			If the first decision is not selecting anything we select randomly an option
			state = r.nextInt(gsdag.getNumStatesDecisionFirstBranch(first));
		}
		
		firstOption = getOptionToChooseOfState(first,state,accumNumStates);
		
		return firstOption;
		
	
	}

	
	/**
	 * @param decision
	 * @param state
	 * @param numStates 
	 * @return
	 */
	public int getOptionToChooseOfState(Integer decision,Integer state, ArrayList<Integer> accumNumStates){
		int sum = 0;
		int option;
		
		sum = accumNumStates.get(decision);
		option = sum+state;
		return option;
	}

	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean debug) {
		GraphAOUID.debug = debug;
	}

	public NodeAOUID getRoot() {
		// TODO Auto-generated method stub
		return root;
	}

	public int getNumberOfLevelsCompleted() {
		// TODO Auto-generated method stub
		return root.getNumberOfLevelsCompleted();
	}

	
	
}
