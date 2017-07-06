package elvira.inference.uids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.RelationList;
import elvira.inference.uids.NodeAOUID.TypeOfNodeAOUID;
import elvira.potential.PotentialTable;

//It encapsulates the same information of the NodeGSDAG but with the utility and probability
//potentials during the evaluation of the GSDAG, and also the name of the next variable
//to be eliminated from the potentials in the NodeGSDAG

public class NodeGSDAG extends Node {
	
	public class RelationsNodeGSDAG {
		public RelationsNodeGSDAG() {
			probabilityRelations = new RelationList();
			utilityRelations = new RelationList();
			// TODO Auto-generated constructor stub
		}
		private RelationList probabilityRelations;
		private RelationList utilityRelations;
		public RelationList getProbabilityRelations() {
			return probabilityRelations;
		}
		public void setProbabilityRelations(RelationList probabilityRelations) {
			this.probabilityRelations = probabilityRelations;
		}
		public RelationList getUtilityRelations() {
			return utilityRelations;
		}
		public void setUtilityRelations(RelationList utilityRelations) {
			this.utilityRelations = utilityRelations;
		}
	}
	
	public class PotentialsForDecisionTable {
		PotentialTable utilitiesDecisionTable;
		PotentialTable policyDecisionTable;
		public PotentialTable getUtilitiesDecisionTable() {
			return utilitiesDecisionTable;
		}
		public void setUtilitiesDecisionTable(PotentialTable utilitiesDecisionTable) {
			this.utilitiesDecisionTable = utilitiesDecisionTable;
		}
		public PotentialTable getPolicyDecisionTable() {
			return policyDecisionTable;
		}
		public void setPolicyDecisionTable(PotentialTable policyDecisionTable) {
			this.policyDecisionTable = policyDecisionTable;
		}
		
	}
	
	

	
	
	
	
	
	public enum TypeOfNodeGSDAG {DECISION, CHANCE, BRANCH}

//	Names of the variables of the node of the GSDAG
	ArrayList<String> variables;
	
	/**
	 * Minimum set of variables for coalescence
	 */
	ArrayList<NodeList> minVarsCoal;
	
	/**
	 * Complement of the minimum set of variables for coalescence, i.e.:
	 * Var(Past)-minVarsCoal
	 */
	ArrayList<NodeList> complementMinVarsCoal;
	
	public TypeOfNodeGSDAG type;
	
	//RelationList probabilityRelations;
	
	//RelationList utilityRelations;
	
	/**
	 * It contains the list of relations after eliminating each variable
	 */
	LinkedList<RelationsNodeGSDAG> listRelations;
	
	
	// Policies and the utilities (for decisions and branches) for each branch or decision
	
	HashMap<String,PotentialsForDecisionTable> potentialsForDecisionTable;
	

	
	//Indicates the next variable of the NodeGSDAG that must be eliminated from the 
	//probability and utility relations. In branch nodes this value is "". This value
	//"" is also used when there aren't more variables to eliminate in the NodeGSDAG,
	//but we leave the obtained potentials in the same NodeGSDAG
	String lastEliminatedVariable;
	
	//It indicates if all the variables of the potentials of the node have been eliminated,
	//so the parents of the node can get the obtained potentials
	boolean completelyEvaluated;
	
	
	
	
	
	
	public NodeGSDAG(TypeOfNodeGSDAG typeNode) {
		// TODO Auto-generated constructor stub
		type = typeNode;
		variables = new ArrayList();
		//probabilityRelations=new RelationList();
		//utilityRelations=new RelationList();
		listRelations = new LinkedList<RelationsNodeGSDAG>();
		
		lastEliminatedVariable = "";
		completelyEvaluated = false;
		
		potentialsForDecisionTable = new HashMap<String,PotentialsForDecisionTable> ();
		
		
	}




	@Override
	public double undefValue() {
		// TODO Auto-generated method stub
		return 0;
	}




	public ArrayList<String> getVariables() {
		return variables;
	}

	
	



	public void setVariables(ArrayList<String> variables) {
		this.variables = variables;
	}

	public void print(){
		System.out.println(type.toString());
		for(String aux:variables){
			System.out.println(aux);
		}
	}




	public TypeOfNodeGSDAG getTypeOfNodeGSDAG() {
		// TODO Auto-generated method stub
		return type;
	}









	/*public void setProbabilityRelations(RelationList probabilityRelations) {
		this.probabilityRelations = probabilityRelations;
	}




	public RelationList getUtilityRelations() {
		return utilityRelations;
	}




	public void setUtilityRelations(RelationList utilityRelations) {
		this.utilityRelations = utilityRelations;
	}*/
	
	public void copyRelationsFrom(NodeGSDAG node){
		RelationList probRels;
		RelationList utilRels;
		RelationList newProbRels;
		RelationList newUtilRels;
		
		probRels = node.getCurrentProbabilityRelations();
		utilRels = node.getCurrentUtilityRelations();
	//Copy of the pointers to the probability relations
		newProbRels = new RelationList();
	for (int i=0;i<probRels.size();i++){
		newProbRels.insertRelation(probRels.elementAt(i));
	}
	
//	Copy of the pointers to the utility relations
	newUtilRels = new RelationList();
	for (int i=0;i<utilRels.size();i++){
		newUtilRels.insertRelation(utilRels.elementAt(i));
	}
	this.setCurrentRelations(newProbRels, newUtilRels);
	}




	public boolean areAllChildrenEvaluated() {
		// TODO Auto-generated method stub
		boolean allEvaluated = true;
		NodeList childrenNodes;
		
		childrenNodes = this.getChildrenNodes();
		
		for (int i=0;(i<childrenNodes.size())&&allEvaluated;i++){
			if (((NodeGSDAG) childrenNodes.elementAt(i)).isCompletelyEvaluated()==false){
				allEvaluated = false;
			}
			
		}
		
		return allEvaluated;
	}






	public boolean isCompletelyEvaluated() {
		return completelyEvaluated;
	}




	public void setCompletelyEvaluated(boolean evaluated) {
		this.completelyEvaluated = evaluated;
	}




	public String getLastEliminatedVariable() {
		return lastEliminatedVariable;
	}




	public void setLastEliminatedVariable(String lastEliminatedVariable) {
		this.lastEliminatedVariable = lastEliminatedVariable;
	}




	/**
	 * @return list of NodeGSDAG that are descendants of this nodeGSDAG and have calculated their potentials.
	 * If all the parents of a node are included in this list then it's not included.
	 * This method is used to calculate the heuristic of the nodeGSDAGs
	 */
	public ArrayList<NodeGSDAG> obtainNearestDescendantsWithSomeVariablesEliminated() {
		// TODO Auto-generated method stub
		ArrayList<NodeGSDAG> list;
		
		
		list = new ArrayList();
		
		auxObtainNearestDescendantsWithSomeVariablesEliminated(list);
		
		return list;
		
	}
	
	
	/**
	 * @return list of NodeGSDAG that are descendants of this nodeGSDAG and have calculated their potentials.
	 * If all the parents of a node are included in this list then it's not included.
	 * This method is used to calculate the heuristic of the nodeGSDAGs
	 */
	public ArrayList<NodeGSDAG> obtainMinimalSetOfNearestDescendantsWithSomeVariablesEliminated() {
		// TODO Auto-generated method stub
		ArrayList<NodeGSDAG> cloneList;
		ArrayList<NodeGSDAG> list;
		ArrayList<NodeGSDAG> finalList= new ArrayList();
		boolean includeAuxNode1;
		
		
		list = new ArrayList();
		
		auxObtainNearestDescendantsWithSomeVariablesEliminated(list);
		
		for (NodeGSDAG auxNode1:list){
			//We see if auxNode1 has descendants in the list
			includeAuxNode1 = true;
			for (NodeGSDAG auxNode2:list){
				if (auxNode1!=auxNode2){
					if (auxNode2.isDescendantOf(auxNode1)){
						includeAuxNode1 = false;
					}
				}
			}
			//If it does not have descendants then it's included in the finalList
			if (includeAuxNode1){
				finalList.add(auxNode1);
			}
		
		}
		
		
		return finalList;
		
	}
	
	/**
	 * Auxiliar method of 'obtainNearestDescendantsEvaluated
	 * @param list
	 */
	private void auxObtainNearestDescendantsWithSomeVariablesEliminated(ArrayList<NodeGSDAG> list){
		NodeList children;
		
		
		//We check if some potentials have been calculated
		//if (this.getCurrentUtilityRelations().size()==0){
		if (this.hasNoRelations()){
			//We have to look for in the children
			children = this.getChildrenNodes();
			if (children!=null){
				for (int i=0;i<children.size();i++){
					((NodeGSDAG)children.elementAt(i)).auxObtainNearestDescendantsWithSomeVariablesEliminated(list);
				}
			}
		}
		else{//Some potentials are calculated in the node
			//We include this NodeGSDAG in the list of the descendants and we don't look for in the children
			if (list.contains(this)==false){
				list.add(this);
			}
		}
	}




	private boolean hasAnyRelations() {
		// TODO Auto-generated method stub
		return (this.listRelations.size()>0);
	}


	
	boolean hasNoRelations() {
		// TODO Auto-generated method stub
		return (this.listRelations.size()==0);
	}


	public String nextVariable(String nameOfVariable) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub		// TODO Auto-generated method stub
		int indexOfVar;
		ArrayList<String> vars;
		int newIndex;
		String nextVar;
		
		if (type == TypeOfNodeGSDAG.BRANCH){//The method shouldn't be invoked for branches
				nextVar = null;
				System.out.println("Error. The method nextNodeGSDAG shouldn't be invoked for branches");
		}
		else{
			vars = getVariables();
			indexOfVar = vars.indexOf(nameOfVariable);
			newIndex = indexOfVar+1;
			
			if (newIndex < vars.size()){//There's more variables to process in the same NodeGSDAG
				nextVar = vars.get(newIndex);
			}
			else{//All variables in the NodeGSDAG have been processed, so we have to process the sucessor
				vars = ((NodeGSDAG)getChildrenNodes().elementAt(0)).getVariables();
				if ((vars == null)||(vars.size()==0)){
					nextVar = "";//The next node is a branch

				}
				else{
					nextVar = vars.get(0); 
				}
				
			}
		}
		return nextVar;
	}




	@Override
	public boolean equals(Object n) {
		// TODO Auto-generated method stub
		return (this==n);
	}




	/**
	 * @param auxNodeGSDAG
	 * @return The probability of that auxNodeGSDAG is selected in a strategy that
	 * select randomly with uniform distribution any of the next branches.
	 */
	public double obtainProbabilityOfSelect(NodeGSDAG auxNodeGSDAG) {
		NodeList children;
		double probChild;
		double numChildren;
		double totalProb = 0;
		
		
		// TODO Auto-generated method stub
		//We check if some potentials have been calculated
		if (this.hasAnyRelations()==false){
			//We have to look for in the children
			children = this.getChildrenNodes();
			if (children!=null){
				numChildren = children.size();
				totalProb = 0.0;
				for (int i=0;i<numChildren;i++){
					probChild=((NodeGSDAG)children.elementAt(i)).obtainProbabilityOfSelect(auxNodeGSDAG);
					totalProb = totalProb + probChild/numChildren;
					
				}
			}
		}
		else{//The probability is 1.0 iff we have reached auxNodeGSDAG
			if (this==auxNodeGSDAG){
				totalProb = 1.0;
			}
			else{
				totalProb = 0.0;
			}
		}
		
		return totalProb;
	}
	
	/**
	 * @param auxNodeGSDAG
	 * @return The probability of that auxNodeGSDAG is selected in a strategy that
	 * select randomly with uniform distribution any of the next branches.
	 * This method is adapted for the GSDAG of reference
	 */
	public double obtainProbabilityOfSelectGSDAGReference(NodeGSDAG auxNodeGSDAG) {
		NodeList children;
		double probChild;
		double numChildren;
		double totalProb = 0;
		
		
		if (this==auxNodeGSDAG){
			totalProb = 1.0;
		}
		else{
			//We have to look for in the children
			children = this.getChildrenNodes();
			if (children!=null){
				numChildren = children.size();
				totalProb = 0.0;
				for (int i=0;i<numChildren;i++){
					probChild=((NodeGSDAG)children.elementAt(i)).obtainProbabilityOfSelectGSDAGReference(auxNodeGSDAG);
					totalProb = totalProb + probChild/numChildren;
					
				}
			}
		}
		return totalProb;
	}

	
	/**
	 * @param node
	 * @return true if 'this' is descendant of node
	 */
	public boolean isDescendantOf(NodeGSDAG node){
		if (this.isChildrenOf(node)){
			return true;
		}
		else{
			NodeList children;
			children = node.getChildrenNodes();
			for (int i=0;i<children.size();i++){
				if (this.isDescendantOf((NodeGSDAG) children.elementAt(i))) return true;
			}
		}
		return false;
	
	}
	
	public boolean isChildrenOf(NodeGSDAG node){
		NodeList children;
		
		children = node.getChildrenNodes();
		
		for (int i=0;i<children.size();i++){
			if (children.elementAt(i)==this) return true;
		}
		return false;
	}


	public int distanceToLastNode() {
		// TODO Auto-generated method stub
		NodeList children;
		int dist=0;
		
		children = getChildrenNodes();
		if ((children==null)||(children.size()==0)){//It's the last node
			dist=0;
		}
		else{
			dist = 1+((NodeGSDAG) children.elementAt(0)).distanceToLastNode();
		}
		return dist;
	}
	
	public int distanceToRootNode() {
		// TODO Auto-generated method stub
		NodeList parents;
		int dist=0;
		
		parents = getParentNodes();
		if ((parents==null)||(parents.size()==0)){//It's the root node
			dist=0;
		}
		else{
			dist = 1+((NodeGSDAG) parents.elementAt(0)).distanceToRootNode();
		}
		return dist;
	}




	/**
	 * @return the number of paths from this node to the sink node of the gsdag
	 */
	public int getNumberOfPaths() {
		NodeList children;
		int numPaths;
		// TODO Auto-generated method stub
		
		numPaths = 0;
		children = this.getChildrenNodes();
		if ((children != null)&&(children.size()>0)){
		for (int i=0;i<children.size();i++){
			numPaths = numPaths + ((NodeGSDAG)(children.elementAt(i))).getNumberOfPaths();
			
		}
		//numPaths = children.size()*((NodeGSDAG)(children.elementAt(0))).getNumberOfPaths();
			
		
		}
		else{
			numPaths = 1;
		}
		
		return numPaths;
	}




	public ArrayList<NodeList> getMinVarsCoal() {
		return minVarsCoal;
	}




	public void setMinVarsCoal(ArrayList<NodeList> minVarsCoal) {
		this.minVarsCoal = minVarsCoal;
	}




	public ArrayList<NodeList> getComplementMinVarsCoal() {
		return complementMinVarsCoal;
	}




	public void setComplementMinVarsCoal(ArrayList<NodeList> complementMinVarsCoal) {
		this.complementMinVarsCoal = complementMinVarsCoal;
	}




	public void printMinVarsCoal() {
		// TODO Auto-generated method stub
		for (int i=0;i<variables.size();i++){
			System.out.println("Variable "+variables.get(i)+" "+minVarsCoal.get(i).toString());
			
		}
	}


	public boolean hasBranchAtBeginning(int minNumChildrenFirstBranch) {
		// TODO Auto-generated method stub
		return ((this.type==NodeGSDAG.TypeOfNodeGSDAG.BRANCH)&&(this.getChildren().size()>=minNumChildrenFirstBranch));
	}




	/**
	 * This method is used when the nodeGSDAG contains several variables to be eliminated and some of them can be eliminated.
	 * In that case
	 * @return
	 */
	public RelationList getCurrentUtilityRelations() {
		// TODO Auto-generated method stub
		return getCurrentRelations().getUtilityRelations();
		
	}
	
	public RelationList getCurrentProbabilityRelations() {
		return getCurrentRelations().getProbabilityRelations();
	}

	
	public RelationsNodeGSDAG getCurrentRelations() {
		return this.listRelations.getFirst();
	}
	
	/**
	 * @param variable
	 * @return the relations obtained after a variable has been eliminated
	 */
	public RelationsNodeGSDAG getRelationsAfterEliminating(String variable){
		int indexOfRels;
		
		indexOfRels = this.getIndexOfRelationsAfterEliminating(variable);
		return listRelations.get(indexOfRels);
	}
	
	
	/**
	 * @param variable
	 * @return the index in 'listRelations' where the relations of the variable named 'variable' are placed
	 */
	public int getIndexOfRelationsAfterEliminating(String variable){
		int index;
		int indexOfRels;
		
		index = variables.indexOf(variable);
		
		if (this.type==TypeOfNodeGSDAG.BRANCH){
			indexOfRels = 0;
		}
		else{
		if (index==-1){
			indexOfRels = listRelations.size()-1;
		}
		else{
			indexOfRels = index;
		}
		}
		
		return indexOfRels;
	}
	
	
	/**
	 * @param variable
	 * @return the index in 'listRelations' where the relations of the variable named 'variable' are placed
	 */
	public int getIndexOfRelationsBeforeEliminating(String variable){
		int index;
		int indexOfRels;
		
		index = variables.indexOf(variable);
		
		if (index==-1){
			indexOfRels = listRelations.size()-1;
		}
		else{
			indexOfRels = index+1;
		}
		
		return indexOfRels;
	}

	
	public RelationList getUtilityRelationsAfterEliminating(String variable) {
		// TODO Auto-generated method stub
		
		return this.getRelationsAfterEliminating(variable).getUtilityRelations();
	}


	public RelationList getProbabilityRelationsAfterEliminating(String variable) {
		// TODO Auto-generated method stub
		
		return this.getRelationsAfterEliminating(variable).getProbabilityRelations();
	}




	public void setCurrentRelations(RelationList newProbRels,
			RelationList newUtilRels) {
		// TODO Auto-generated method stub
		RelationsNodeGSDAG rels;
		
		rels = new RelationsNodeGSDAG();
		rels.setProbabilityRelations(newProbRels);
		rels.setUtilityRelations(newUtilRels);
		this.listRelations.addFirst(rels);
		
		
	}




	public LinkedList<RelationsNodeGSDAG> getListRelations() {
		return listRelations;
	}




	public void setListRelations(LinkedList<RelationsNodeGSDAG> listRelations) {
		this.listRelations = listRelations;
	}




	public ArrayList<NodeGSDAGAndRelations> obtainNearestDescendantsAfterDeciding(
			int numDecisionsPlotMEU) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		ArrayList<NodeGSDAGAndRelations> list;
		
		
		list = new ArrayList();
		
		auxObtainNearestDescendantsAfterDeciding(numDecisionsPlotMEU,list);
		
		return list;
	}




	private void auxObtainNearestDescendantsAfterDeciding(
			int numDecisionsPlotMEU, ArrayList<NodeGSDAGAndRelations> list) {
		// TODO Auto-generated method stub
	NodeList children;
	int numDecsOrBranches;
	NodeGSDAGAndRelations aux;
	int newNumDecisionsPlotMEU;
		
		
	
		if (numDecisionsPlotMEU==0){
			//We take the first relations
			aux = new NodeGSDAGAndRelations();
			aux.setNodeGSDAG(this);
			aux.setRelations(this.getCurrentRelations());
			list.add(aux);
		}
		else{
			numDecsOrBranches = getNumberOfDecisionsOrBranches(); 
			if (numDecsOrBranches>numDecisionsPlotMEU){
				//We look for the relations in this node
				aux = new NodeGSDAGAndRelations();
				aux.setNodeGSDAG(this);
				aux.setRelations(listRelations.get(numDecisionsPlotMEU));
				list.add(aux);
			}
			else{
				//We have to look for in the children
				newNumDecisionsPlotMEU = numDecisionsPlotMEU-numDecsOrBranches;
				children = this.getChildrenNodes();
				if (children!=null){
					for (int i=0;i<children.size();i++){
						((NodeGSDAG)children.elementAt(i)).auxObtainNearestDescendantsAfterDeciding(newNumDecisionsPlotMEU,list);
					}
				}	
			}
		}
	}




	int getNumberOfDecisionsOrBranches() {
		int num=0;
		// TODO Auto-generated method stub
		switch(type){
		case BRANCH:
			num = 1;
			break;
		case DECISION:
			num = variables.size();
			break;
		case CHANCE:
			num = 0;
			break;
		}
		return num;
		
	}




	public int getNumberOfDecisionsOrBranchesEliminated() {
		// TODO Auto-generated method stub
		int lengthRels;
		int numElim = 0;
		
		lengthRels = listRelations.size();
		switch (type){
		case BRANCH:
			numElim = lengthRels;
			break;
		case DECISION:
			numElim = lengthRels-1;
			break;
		case CHANCE:
			numElim = 0;
			break;
		}
		return numElim;
	}




	public void setNameToPaintIt() {
		// TODO Auto-generated method stub
		TypeOfNodeGSDAG typeOfNodeGSDAG = this.getTypeOfNodeGSDAG();
		switch (typeOfNodeGSDAG){
		case CHANCE:
		case DECISION:
			this.setName(variables.toString());
			break;
		case BRANCH:
			this.setName("Branch");
			break;
		}
	}




	public void storeUtilitiesAndPolicyForDecisionTable(PotentialTable utilPot, PotentialTable policyPot,String nameVar) {
		// TODO Auto-generated method stub
		PotentialsForDecisionTable pot;
		
		pot = new PotentialsForDecisionTable();
		
		pot.setPolicyDecisionTable(policyPot);
		pot.setUtilitiesDecisionTable(utilPot);
				
		this.potentialsForDecisionTable.put(nameVar,pot);
	}




	public HashMap<String, PotentialsForDecisionTable> getPotentialsForDecisionTable() {
		return potentialsForDecisionTable;
	}




	public void setPotentialsForDecisionTable(
			HashMap<String, PotentialsForDecisionTable> potentialsForDecisionTable) {
		this.potentialsForDecisionTable = potentialsForDecisionTable;
	}
	
	
	public ArrayList<String> getAnAdmissibleOrderOfThePast() {

		NodeList parents = this.getParentNodes();
		ArrayList<String> order;
		order = new ArrayList<String>();

		if (this.type != TypeOfNodeGSDAG.BRANCH) {
			order.addAll(this.getVariables());
		}
		if (parents != null) {
			if (parents.size() > 0) {
				order.addAll(((NodeGSDAG) parents.elementAt(0))
						.getAnAdmissibleOrderOfThePast());
			}
		}
		return order;
	}
	
}//end of class


