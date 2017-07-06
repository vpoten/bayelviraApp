package elvira.inference.uids.AnytimeUpdatingK;

import java.util.ArrayList;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.RelationList;
import elvira.UID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.NodeAOUID;
import elvira.inference.uids.NodeGSDAG;
import elvira.inference.uids.Anytime.GraphAOUID_Anytime;
import elvira.inference.uids.Anytime.NodeAOUID_Anytime;
import elvira.inference.uids.NodeAOUID.TypeOfNodeAOUID;
import elvira.potential.Potential;

public class NodeAOUID_Any_Upd_K extends NodeAOUID_Anytime {
	
	double k_chance;
	
	
	/**
	 * Lower bound of the utilities
	 */
	double l;
	/**
	 * Upper bound of the utilities
	 */
	double u;
	/**
	 * Number of chance nodes / Number of variables
	 */
	protected double c;
	/**
	 * Number of decision nodes / Number of variables (c+d=1)
	 */
	protected double d;

	/**
	 * Number of chance nodes
	 */
	private int numChance;
	
	/**
	 * Number of decision nodes
	 */
	private int numDecisions;

	

	public NodeAOUID_Any_Upd_K(UID uid2, GSDAG gsdag,
			GraphAOUID_Any_Upd_K graphUID2,
			double k_chance2) {
		// TODO Auto-generated constructor stub
	// TODO Auto-generated constructor stub
		
		//Keep a pointer to the UID
		uid = uid2;
		
		//Keep a pointer to the graphUID
		graphUID =graphUID2; 
		
		nodeGSDAG = gsdag.getRoot();
		if (nodeGSDAG.type!=NodeGSDAG.TypeOfNodeGSDAG.BRANCH){
			nameOfVariable = gsdag.getRoot().getVariables().get(0);
		}
		type = getTypeFromGSDAG(nodeGSDAG.type);
		
		instantiations = new Configuration();
		
		solved = false;
		
		k_chance = k_chance2;
		
		calculateValueOfHeuristic(null);
		
		//pruned = false;
	}

	public NodeAOUID_Any_Upd_K() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected double getK_chance_For_Estimate() {
		// TODO Auto-generated method stub
		//return this.k_chance;
		return getK_Chance_Of_Grandparent();
	}

	private double getK_Chance_Of_Grandparent() {
		NodeList parents;
		NodeList grandparents;
		NodeAOUID_Any_Upd_K father;
		double k;
		// TODO Auto-generated method stub
		//By the moment we return the value of K of a grandparent if it exists.
		//Else if the father exists we return the value of K of a father
		//Else we return the current value of K
		parents = this.getParentNodes();
		if (parents.size()>0){
			father = (NodeAOUID_Any_Upd_K) parents.elementAt(0);
			grandparents = father.getParentNodes();
			if (grandparents.size()>0){
				k = ((NodeAOUID_Any_Upd_K) grandparents.elementAt(0)).getK_chance();
			}
			else{//Children of the root of the tree
				k = father.getK_chance();
			}
		}
		else{//Root of the tree
			k = this.getK_chance();
		}
		
		return k;
	}

	@Override
	public NodeAOUID_Any_Upd_K copy() {
		// TODO Auto-generated method stub
		
		NodeAOUID_Any_Upd_K auxNode = new NodeAOUID_Any_Upd_K();
		auxNode.setUid(this.getUid());
		auxNode.graphUID = graphUID;
		auxNode.setInstantiations(this.getInstantiations().duplicate());
		auxNode.setF(this.getF());
		auxNode.setType(this.getTypeOfNodeAOUID());
		auxNode.setNameOfVariable(this.getNameOfVariable());
		auxNode.setNodeGSDAG(this.getNodeGSDAG());
		auxNode.setK_chance(this.getK_chance());
		auxNode.setC(this.getC());
		auxNode.setD(this.getD());
		auxNode.setU(this.getU());
		auxNode.setL(this.getL());
		auxNode.setNumChance(this.getNumChance());
		auxNode.setNumDecisions(this.getNumDecisions());
		//auxNode.setFLowerBound(this.getFLowerBound());
		//auxNode.setFUpperBound(this.getFUpperBound());
		
		return auxNode;
	}

	public void setK_chance(double k_chance) {
		this.k_chance = k_chance;
	}

	
	/**
	 * @param father It's the father of the node receiving the message, in the AO graph
	 * It can be used to save computation of the heuristic (for example, when it's implemented
	 * like a list of heuristics.
	 */
	protected void calculateValueOfHeuristic(NodeAOUID father) {
		NodeGSDAG last;
		RelationList instantUtilRels;
		RelationList instantProbRels;
		double f1;
		double f2;
		double u=Double.POSITIVE_INFINITY;
		double l=0.0;
		double wl;
		double wu;
		
		
		last = getGraphUID().getGsdag().getLastNodeGSDAG();

		
		
		
		
		if (this.isSolvedNodeAOUID()){
//			The upperbound is always the same: The maximum of the utilities
			instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
			u = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
			setF(u);
		}
		else{
					wl = computeLowerWeightForMixingHeuristics();
					wu = 1-wl;
					if (wu>0.0){
						instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
//						The upperbound is always the same: The maximum of the utilities
						u = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
					}
					if (wl>0.0){
					instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
					instantProbRels = instantiateRelations(last.getCurrentProbabilityRelations());
					l = heuristicEvenDistributionForDecisions(instantProbRels, instantUtilRels);
					}
					if (wu==0.0){
						setF(l);
					}
					else if (wl==0.0){
						setF(u);
					}
					else{
						setF(mixHeuristics(wl,l,wu,u));
					}
					
			
		}
		
		setCDLU(l,u);
	
				
			
	}

	
	/**
	 * @param father It's the father of the node receiving the message, in the AO graph
	 * It can be used to save computation of the heuristic (for example, when it's implemented
	 * like a list of heuristics.
	 */
	protected void calculateValueOfHeuristic(NodeAOUID father,Potential lowerBounds) {
		NodeGSDAG last;
		RelationList instantUtilRels;
		RelationList instantProbRels;
		double f1;
		double f2;
		double u=Double.POSITIVE_INFINITY;
		double l=0.0;
		double wl;
		double wu;
		
		
		last = getGraphUID().getGsdag().getLastNodeGSDAG();

		
		
		
		
		if (this.isSolvedNodeAOUID()){
//			The upperbound is always the same: The maximum of the utilities
			instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
			u = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
			setF(u);
		}
		else{
					wl = computeLowerWeightForMixingHeuristics();
					wu = 1-wl;
					if (wu>0.0){
						instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
//						The upperbound is always the same: The maximum of the utilities
						u = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
					}
					if (wl>0.0){
					
					l = lowerBounds.getValue(this.getInstantiations());
					}
					if (wu==0.0){
						setF(l);
					}
					else if (wl==0.0){
						setF(u);
					}
					else{
						setF(mixHeuristics(wl,l,wu,u));
					}
					
			
		}
		
		setCDLU(l,u);
	
				
			
	}
	protected void setCDLU(double l2, double u2) {
		// TODO Auto-generated method stub
		double sumW;
		
		GraphAOUID_Anytime graph = (GraphAOUID_Anytime) getGraphUID();
		ArrayList<String> varsOfFuture = graph.getGsdag().getDescendantVariables(nameOfVariable,nodeGSDAG);
		
		int numVars = varsOfFuture.size();
		
		//We count the number of decisions until the end of the gsdag
		int numDecs = 0;
		for (String auxVar:varsOfFuture){
			if (this.uid.getNode(auxVar).getKindOfNode()==Node.DECISION){
				numDecs++;
			}
		}
		
		//Save the parameters necessary to compute the heuristic
		if (numVars!=0){
		d = (double) numDecs/numVars;
		c = 1.0-d;
		numDecisions = numDecs;
		numChance = numVars-numDecs;
		}
		else{
			d=0.5;
			c=0.5;
			numDecisions=0;
			numChance=0;
		}
		l = l2;
		u = u2;
	}
	
	/**
	 * Method that update the necessary heuristics in node and its parents
	 * if it is necessary.
	 * The node can use an only heuristic for select the node and prune.
	 * Or it can use different heuristics.
	 * The class NodeAOUID uses an heuristic f.
	 * See how the class NodeAOUID_Anytime uses two.
	 * IMPORTANT: The value of k_chance is updated according to how Thomas and I discussed
	 */
	/*public void updateHeuristicInNode(){
		updateHeuristicFInNode();
		updateK_chanceInNode();
	}*/
	
	//It updates the value of F of this node and of the ancestors if it's necessary
	//It is invoked when we use an only heuristic (admissible) for both tasks:
	// (a) Select the node to expand
	// (b) Prune the branches that do not lead to an optimal solution
	protected void updateHeuristicFInNode() {
		// TODO Auto-generated method stub
		
		double newF;
		NodeList childrenNodes;
		NodeList parentNodes;
		NodeAOUID auxChild;
		double auxFChild;
		ArrayList<NodeAOUID> newBestChild = null; 
		
		
		childrenNodes = this.getChildrenNodes();
		switch (type){
		case BRANCH:
		case DECISION:
			//Maximize over the children
			newF = Double.NEGATIVE_INFINITY;
			
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID)childrenNodes.elementAt(i);
				auxFChild = auxChild.getF();
				if (auxFChild>newF){//We improve newF
					newBestChild = new ArrayList();
					newBestChild.add(auxChild);
					newF = auxFChild;
				}
				else if (auxFChild==newF){//We have a tie in newF
					newBestChild.add(auxChild);
				}
			}
			//Update of bestChildInPartialSolution
			//No sé por qué falla la randomización
			
			bestChildInPartialSolution = (((newBestChild!=null)&&(newBestChild.size()>0))?((NodeAOUID) selectChildRandomlyWhenTie(newBestChild)):null);
			//bestChildInPartialSolution = (((newBestChild!=null)&&(newBestChild.size()>0))?(newBestChild.get(0)):null);
			//If f is updated we have to notify it to the parents
			if (newF!=f){
				updateFAndK(newF);
				
				//updateHeuristicInParents();
			}
			//We can update the heuristic in parents always to select other paths when we have tie
			updateHeuristicInParents();
			break;
		case CHANCE:
			//Weighted sum over the children
			newF = 0;
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID)childrenNodes.elementAt(i);
				newF = newF + auxChild.getF()*conditionalProbs.getValue(auxChild.getInstantiations());
			}
			//Update of F
			if (newF!=f){
				updateFAndK(newF);
				
				//updateHeuristicInParents();
			}
//			We can update the heuristic in parents always to select other paths when we have tie
			updateHeuristicInParents();
			break;
		}
		
		
	}


	private void updateFAndK(double newF) {
		// TODO Auto-generated method stub
		
		if (this.getGraphUID().isDebug()) System.out.println("The value of F in the node "+this.getInstantiations().toString()+ " has changed from:"+this.f+" to:"+newF);
		f = newF;
		if (((GraphAOUID_Anytime) this.getGraphUID()).updateK()){
			updateK_chanceInNode();
		}
	}

	public void updateK_chanceInNode() {
		// TODO Auto-generated method stub
		//setK_chanceFromCDLUAndF();
		double k;
		double f;
		
		f = this.getF();
		
		if (canKBeDetermined()){
			k =calculateNewKLearningRate();
		
		}
		else{
			k = 1.0;
		
		}
			if (isDebug()){
				System.out.println("Updating k: The value of K in the node "+this.getInstantiations().toString());
				System.out.println("f= "+f+", d= "+d+", c= "+c+", u= "+u+", l= "+l+". K has changed from k= "+this.getK_chance()+" to k= "+k);
			}
			
		
			setK_chance(k);
		
		
	}
	
	private double calculateNewKLearningRate() {
		double f;
		double newK;
		double newKRated;
		double learningRate = 0.5;
		
		f=this.getF();
		// TODO Auto-generated method stub
		newK = (d*(u-f))/(c*(f-l));
		//newKRated = this.getK_chance()*(1-learningRate)+newK*learningRate;
		newKRated = newK;
		return newKRated;
	}

	private boolean canKBeDetermined() {
		// TODO Auto-generated method stub
		return ((d!=0.0)&&(d!=1.0)&&(c!=0.0)&&(c!=0)&&(u!=f)&&(f!=l));
	}


	public double getK_chance() {
		return k_chance;
	}
	
	/*public ArrayList<NodeAOUID> generateSucessorsOfBranch() {
			
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> sucessors;
		NodeAOUID_Anytime_Updating_K suc;
		
		sucessors = super.generateSucessorsOfBranch();
		
		for (int i=0;i<sucessors.size();i++){
			suc = (NodeAOUID_Anytime_Updating_K) sucessors.get(i);
			suc = suc.copyCDLUFromFather();
		}
		
		return sucessors;
	}

	private NodeAOUID_Anytime_Updating_K copyCDLUFromFather() {
		// TODO Auto-generated method stub
		NodeAOUID_Anytime_Updating_K father;
		
		father = (NodeAOUID_Anytime_Updating_K) this.getParentNodes().elementAt(0);
		
		c = father.c;
		d = father.d;
		l = father.l;
		u = father.u;
		
	}*/

	public double getL() {
		return l;
	}

	public void setL(double l) {
		this.l = l;
	}

	public double getU() {
		return u;
	}

	public void setU(double u) {
		this.u = u;
	}

	public double getC() {
		return c;
	}

	public void setC(double c) {
		this.c = c;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}
	
	/**d
	 * @return
	 *//*
	protected double computeLowerWeightForMixingHeuristicsAndSaveCDLU() {
		// TODO Auto-generated method stub
		//Compute the variables of the future
		ArrayList<String> varsOfFuture;
		int numDecs;
		int numVars;
		double wu;
		double wl;
		double h;
		GraphAOUID_Anytime graph;
		double k;
		//Constant that the higher value the higher importance for non-admissible heuristic
		//Value k=3 worked perfectly with the diagrams:
		//externName=path+"uid1Simplified2.elv";
		//externName=path+"UID2.elv";
		//double k=4;
		//Constant that the higher value the higher importance for non-admissible heuristic
		//double k_chance=3;
		//Weight to do lower the importance of the presence of chance nodes because the higher
		//the number of branches the higher the estimation should be closed to the maximum heuristic
		//double k_paths;
		double sumW;
			
		graph = (GraphAOUID_Anytime) getGraphUID();
		varsOfFuture=graph.getGsdag().getDescendantVariables(nameOfVariable,nodeGSDAG);
		
		numVars = varsOfFuture.size();
		
		//We count the number of decisions until the end of the gsdag
		numDecs = 0;
		for (String auxVar:varsOfFuture){
			if (this.uid.getNode(auxVar).getKindOfNode()==Node.DECISION){
				numDecs++;
			}
		}
		
		// Calculate the weights
		//k = graph.getK_chance();
		k = getK_chance();
		if (k == 0.0) {//Only upper bound (admissible search)
			wl = 0.0;
		} else if (k == Double.POSITIVE_INFINITY) {//Greedy search
			wl = 1.0;
		} else {//Normal search
			wu = (double) numDecs / numVars;
			wl = 1.0 - (double) numDecs / numVars;
			wl = k * wl;
			sumW = wu + wl;
			wu = wu / sumW;
			wl = wl / sumW;
		}
		
		return wl;
		//return wl = 1.0;
	}
	
	
*/

	
	public void printEstimates() {
		// TODO Auto-generated method stub
		super.printEstimates();
		System.out.println("Static bounds: l="+this.l+" and u="+this.u);
	}
	
	//It generates the sucessors of the node to expand
	//Add the new nodes to the aouid and to the list open
	public ArrayList<NodeAOUID> generateSucessors() {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> sucessors;
		NodeAOUID newNodeAOUID;
		NodeGSDAG newNodeGSDAG;
		NodeList childrenGSDAG;
		Configuration auxConf;
		FiniteStates nodeUID;
		String newNameOfVariable;
		TypeOfNodeAOUID newType;
		Potential pot=null;
		NodeGSDAG auxNodeGSDAG;
		NodeAOUID existingNodeAOUID;
		//Lower bounds of the children generated
		Potential lowerBounds=null;
		Potential condProbsAndHeurEven[];
		
		sucessors = new ArrayList();
		//if (isSolvedNodeAOUID()){
		if (isSolved()){
			
			if (isDebug()) System.out.println("Removing from open the node with instantiations "+instantiations.toString());
			return sucessors;
		}
		else if (isChildOfChanceNodeAOUID()&&hasZeroProbability()){
		
			if (isDebug()) System.out.println("We remove from open the node with instantiatons "+instantiations.toString()+" because it has probability 0");
			return sucessors;
		}
		else{
			
		if (isDebug()){
			System.out.print("* Expanding the node of kind "+type.toString()+", name "+nameOfVariable+" and instantiations ");
			instantiations.print();
			System.out.println("");
		}
		
		
		
		
		switch (type){
		case DECISION:
		case CHANCE:
				//Set the new name of variable and the nodeGSDAG
				newNameOfVariable = nodeGSDAG.nextVariable(nameOfVariable);
				newNodeGSDAG = nextNodeGSDAG();
				newType = getTypeFromGSDAG(newNodeGSDAG.getTypeOfNodeGSDAG());
				nodeUID = (FiniteStates) uid.getNode(nameOfVariable);
				
				
				if (type == TypeOfNodeAOUID.CHANCE){
					condProbsAndHeurEven = calculateConditionedProbabilitiesAndHeurEvenDistrib(true);
					conditionalProbs = condProbsAndHeurEven[0];
					lowerBounds = condProbsAndHeurEven[1];
				//conditionalProbs = calculateConditionedProbabilitiesSSP();
				}
				else{
					lowerBounds = calculateLowerBounds();
				}
				//System.out.println("Children of the expanded node:");
				//Copy the parent in each child and set the new values for it
				for (int i=0;i<nodeUID.getNumStates();i++){
					existingNodeAOUID = graphUID.improvedGetNodeAOUID(instantiations,nodeUID,i);
					
					if (existingNodeAOUID == null){
//					Copy of the current NodeAOUID in the child
					newNodeAOUID = copy();
					newNodeAOUID.setNameOfVariable(newNameOfVariable);
					newNodeAOUID.setNodeGSDAG(newNodeGSDAG);
					newNodeAOUID.setType(newType);
					//Modify the instantiations adding the new variable with the corresponding value
					newNodeAOUID.instantiations.insert(nodeUID,i);
					
					((NodeAOUID_Any_Upd_K)newNodeAOUID).calculateValueOfHeuristic(this,lowerBounds);
					if (isDebug()) System.out.println("Node "+newNodeAOUID.getInstantiations().toString()+ " F:"+newNodeAOUID.getF());
					
					if (type == TypeOfNodeAOUID.CHANCE){
						if (isDebug()) System.out.println("Probability: "+conditionalProbs.getValue(newNodeAOUID.getInstantiations()));
					}
					graphUID.addNode(newNodeAOUID);
					
					newNodeAOUID.setOpen(true);
					newNodeAOUID.setSolved(isSolvedNodeAOUID());
					sucessors.add(newNodeAOUID);
					graphUID.setNumberOfNodes(graphUID.getNumberOfNodes()+1);
					}
					else {
						if (isDebug()) System.out.println("State not added to the graph of search because it exists");
						sucessors.add(existingNodeAOUID);
					}
					
					
				}
				break;
		case BRANCH:
			sucessors = generateSucessorsOfBranch();
		
			break;
				
				
		}		
		return sucessors;
		}
	}

	private Potential calculateLowerBounds() {
		// TODO Auto-generated method stub
		
		return calculateConditionedProbabilitiesAndHeurEvenDistrib(false)[1];
		
	}

	public int getNumChance() {
		return numChance;
	}

	public void setNumChance(int numChance) {
		this.numChance = numChance;
	}

	public int getNumDecisions() {
		return numDecisions;
	}

	public void setNumDecisions(int numDecisions) {
		this.numDecisions = numDecisions;
	}

	

	
}
