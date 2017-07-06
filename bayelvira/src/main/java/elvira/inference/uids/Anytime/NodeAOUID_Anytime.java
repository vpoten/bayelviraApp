package elvira.inference.uids.Anytime;

import java.util.ArrayList;

import elvira.Configuration;
import elvira.Evidence;
import elvira.Node;
import elvira.NodeList;
import elvira.RelationList;
import elvira.UID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.GraphAOUID;
import elvira.inference.uids.NodeAOUID;
import elvira.inference.uids.NodeGSDAG;
import elvira.inference.uids.Anytime.AOUID_Anytime.HeuristicForSearching;
import elvira.inference.uids.NodeAOUID.TypeOfNodeAOUID;
import elvira.potential.Potential;

public class NodeAOUID_Anytime extends NodeAOUID {
	
	//It stores the value of the best node explored until now in the children
	//If the admissible heuristic for a child is not better than this bound then
	//the search in that child can be discarded
	enum Heuristic{UPPERBOUND,SEARCH,LOWERBOUND}
	
	
//DYNAMIC_W --> We use dynamic weighting to modify an admissible heuristic
	//MIXED_HEUR --> We combine an admiissible heuristic with a non-admissible heuristic
	
	
	
	//Heuristic to prune (it must be an admissible heuristic)
	//It is an upper bound
	//double fUpperBound;
	
	//Heuristic to select the node to expand (it can be non-admissible)
//	double fSearch;
	
	//Lower bound of the node
	//double fLowerBound;
	

	
	//boolean pruned;

	
	
	

	@Override
	public NodeAOUID_Anytime copy() {
		
		
		NodeAOUID_Anytime auxNode = new NodeAOUID_Anytime();
		auxNode.setUid(this.getUid());
		auxNode.graphUID = graphUID;
		auxNode.setInstantiations(this.getInstantiations().duplicate());
		auxNode.setF(this.getF());
		auxNode.setType(this.getTypeOfNodeAOUID());
		auxNode.setNameOfVariable(this.getNameOfVariable());
		auxNode.setNodeGSDAG(this.getNodeGSDAG());
		//auxNode.setFLowerBound(this.getFLowerBound());
		//auxNode.setFUpperBound(this.getFUpperBound());
		
		return auxNode;
	}

/*	@Override
	public void updateHeuristicInNode() {
		// TODO Auto-generated method stub
		//updateBoundInNode(Heuristic.UPPERBOUND);

		//updateBoundInNode(Heuristic.LOWERBOUND);
		//We calculate the heuristic of search after the bounds because it is possible that we want to calculate
		//it as a function of them
		updateHeuristicFInNode();

	}*/
	
/*	public void updateHeuristicInNode(Heuristic heur) {
		switch(heur){
		case UPPERBOUND:
			updateBoundInNode(Heuristic.UPPERBOUND);
			break;
		case LOWERBOUND:
			updateBoundInNode(Heuristic.LOWERBOUND);
			break;
		case SEARCH:
			super.updateHeuristicInNode();
			break;
		}
		
	}
*/
	
/*	private void updateBoundInNode(Heuristic heur) {
		// TODO Auto-generated method stub
				// TODO Auto-generated method stub
		
		double newH;
		NodeList childrenNodes;
		NodeList parentNodes;
		NodeAOUID_Anytime auxChild;
		double auxHChild;
		ArrayList<NodeAOUID> newBestChild = null; 
		double h;
		
		h = getHeuristic(heur);
		newH = h;
		childrenNodes = this.getChildrenNodes();
		switch (this.getTypeOfNodeAOUID()){
		case BRANCH:
		case DECISION:
			//Maximize over the children
			newH = Double.NEGATIVE_INFINITY;
			
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID_Anytime)childrenNodes.elementAt(i);
				auxHChild = auxChild.getFSearch();
				
				if (auxHChild>newH){
					newH = auxHChild;
				}
				break;
			}
					
			//bestChildInPartialSolution = (((newBestChild!=null)&&(newBestChild.size()>0))?(newBestChild.get(0)):null);
		
			break;
		case CHANCE:
			//Weighted sum over the children
			newH = 0;
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID_Anytime)childrenNodes.elementAt(i);
				newH = newH + auxChild.getHeuristic(heur)*getConditionalProbs().getValue(auxChild.getInstantiations());
			}
		
			break;
		}
		
		//If f is updated we have to notify it to the parents
		if (newH!=h){
			setHeuristic(heur,newH);
			if (heur == Heuristic.LOWERBOUND){
				pruneOpenChildrenUsingBounds();
			}
			//System.out.println("The value of F in the node "+this.getInstantiations().toString()+ " has changed to:"+this.f);
			updateHeuristicInParents(heur);
		}
		//We can update the heuristic in parents always to select other paths when we have tie
		//updateHeuristicInParents();
		
		
	}
*/
/*	private void updateBoundInNode(Heuristic heur) {
		// TODO Auto-generated method stub
				// TODO Auto-generated method stub
		
		double newH;
		NodeList childrenNodes;
		NodeList parentNodes;
		NodeAOUID_Anytime auxChild;
		double auxHChild;
		ArrayList<NodeAOUID> newBestChild = null; 
		double h;
		
		h = getHeuristic(heur);
		newH = h;
		childrenNodes = this.getChildrenNodes();
		switch (this.getTypeOfNodeAOUID()){
		case BRANCH:
		case DECISION:
			//Maximize over the children
			newH = Double.NEGATIVE_INFINITY;
			
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID_Anytime)childrenNodes.elementAt(i);
				auxHChild = auxChild.getBound(heur);
				
				if (auxHChild>newH){
					newH = auxHChild;
				}
				
			}
					
			//bestChildInPartialSolution = (((newBestChild!=null)&&(newBestChild.size()>0))?(newBestChild.get(0)):null);
		
			break;
		case CHANCE:
			//Weighted sum over the children
			newH = 0;
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID_Anytime)childrenNodes.elementAt(i);
				newH = newH + auxChild.getHeuristic(heur)*getConditionalProbs().getValue(auxChild.getInstantiations());
			}
		
			break;
		}
		
		//If f is updated we have to notify it to the parents
		if (newH!=h){
			setHeuristic(heur,newH);
			if (heur == Heuristic.LOWERBOUND){
				pruneChildrenUsingBounds();
			}
			//System.out.println("The value of F in the node "+this.getInstantiations().toString()+ " has changed to:"+this.f);
			updateHeuristicInParents(heur);
		}
		//We can update the heuristic in parents always to select other paths when we have tie
		//updateHeuristicInParents();
		
		
	}*/

/*	private double getBound(Heuristic heur) {
	// TODO Auto-generated method stub
		double bound=0.0;
		switch(heur){
		case LOWERBOUND:
			bound = fLowerBound;
			break;
		case UPPERBOUND:
			bound = fUpperBound;
			break;
		default:
			System.out.println("Error in method getBound. Only parameters LOWERBOUND and UPPERBOUND are allowed");
			System.exit(-1);
			
		}
	return bound;
}*/

/*	private void pruneChildrenUsingBounds() {
		// TODO Auto-generated method stub
		NodeList children;
		NodeAOUID_Anytime iChild;
		
		
		
		if ((this.type==TypeOfNodeAOUID.BRANCH)||(this.type==TypeOfNodeAOUID.DECISION)){
		
		children = this.getChildrenNodes();
		
		for (int i=0;i<children.size();i++){
			iChild = (NodeAOUID_Anytime) children.elementAt(i);
			
			if (iChild.getFUpperBound()<this.getFLowerBound()){
				iChild.setPruned(false);
				System.out.println("Pruning node:"+iChild.getInstantiations().toString()+ "with U:"+iChild.getFUpperBound());
				System.out.println("becaus its parent has L:"+getFLowerBound());
			}
			
		}
		}
	}*/


/*	private void updateHeuristicSearchInNode() {
		// TODO Auto-generated method stub
		
		//Dynamic weighting --> Multiply the upper bound (admissible heuristic) by the corresponding weigth
		
		//Mixed heuristic discussed with Finn and Thomas --> Combine the upper and the lower bounds.
		
		
		double newH;
		NodeList childrenNodes;
		NodeList parentNodes;
		NodeAOUID_Anytime auxChild;
		double auxHChild;
		ArrayList<NodeAOUID> newBestChild = null; 
		double h;
		
		h = getFSearch();
		newH = h;
		childrenNodes = this.getChildrenNodes();
		switch (this.getTypeOfNodeAOUID()){
		case BRANCH:
		case DECISION:
			//Maximize over the children
			newH = Double.NEGATIVE_INFINITY;
			
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID_Anytime)childrenNodes.elementAt(i);
				auxHChild = auxChild.getFSearch();
				
				//We update the non-admissible heuristic and keep the pointer to the best
				
					if (auxHChild>newH){//We improve newF
						newBestChild = new ArrayList();
						newBestChild.add(auxChild);
						newH = auxHChild;
					}
					else if (auxHChild==newH){//We have a tie in newF
						newBestChild.add(auxChild);
					}
//					Update of bestChildInPartialSlution
					bestChildInPartialSolution = (((newBestChild!=null)&&(newBestChild.size()>0))?((NodeAOUID) selectChildRandomlyWhenTie(newBestChild)):null);
					break;
						
			}

		
			
			//bestChildInPartialSolution = (((newBestChild!=null)&&(newBestChild.size()>0))?(newBestChild.get(0)):null);
		
			break;
		case CHANCE:
			//Weighted sum over the children
			newH = 0;
			for (int i=0;i<childrenNodes.size();i++){
				auxChild = (NodeAOUID_Anytime)childrenNodes.elementAt(i);
				newH = newH + auxChild.getFSearch()*getConditionalProbs().getValue(auxChild.getInstantiations());
			}
		
			break;
		}
		
		//If f is updated we have to notify it to the parents
		if (newH!=h){
			setFSearch(newH);
			//System.out.println("The value of F in the node "+this.getInstantiations().toString()+ " has changed to:"+this.f);
			updateHeuristicInParents(Heuristic.SEARCH);
		}
		//We can update the heuristic in parents always to select other paths when we have tie
		//updateHeuristicInParents();
		
		
		
	}*/
	
/*	private void setHeuristic(Heuristic heur, double newH) {
		// TODO Auto-generated method stub
		switch (heur){
		case LOWERBOUND:
			setFLowerBound(newH);
			break;
		case UPPERBOUND:
			setFUpperBound(newH);
			break;
		case SEARCH:
			System.out.println("Error in method getBound. Only parameters LOWERBOUND and UPPERBOUND are allowed");
			System.exit(-1);
			break;

		}
		
	}*/


/*	private double getHeuristic(Heuristic heur) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		double h = 0;
		switch (heur){
		case SEARCH:
			h = getF();
			break;
		case LOWERBOUND:
			h = getFLowerBound();
			break;
		case UPPERBOUND:
			h = getFUpperBound();
			break;
			
		}
		return h;
	}*/


	//It sends the message updateHeuristicInNode to the parents in order
	//they update the value of the heuristic
/*	void updateHeuristicInParents(Heuristic heur) {
		// TODO Auto-generated method stub
		NodeList parentNodes;
		NodeAOUID_Anytime auxParent;
		
		parentNodes = this.getParentNodes();
		for (int i=0;i<parentNodes.size();i++){
			auxParent = (NodeAOUID_Anytime)parentNodes.elementAt(i);
			auxParent.updateHeuristicInNode(heur);
		}
		
		
		
	}*/




/*	public double getFUpperBound() {
		return fUpperBound;
	}

	public void setFUpperBound(double upperBound) {
		fUpperBound = upperBound;
	}

	public double getFLowerBound() {
		return fLowerBound;
	}

	public void setFLowerBound(double lowerBound) {
		fLowerBound = lowerBound;
	}
	*/
	
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
			switch (((GraphAOUID_Anytime) graphUID).getHeurForSearching()){
				case DYNAMIC_W:
//					The upperbound is always the same: The maximum of the utilities
					instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
					u = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
					//setFUpperBound(u);
					setF(this.modifyHeuristicWithDynamicWeighting(u));
					//setFLowerBound(0.0);
					break;
				case MIXED_HEUR:
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
					
					break;
			}
		}
	
				
			
	}
	


	protected double mixHeuristics(double wl, double l, double wu, double u) {
		// TODO Auto-generated method stub
		return (wl*l+wu*u);
	}

/*	*//**
	 * @param u The admissible heuristic (maximum)
	 * @param l The lower bound (decisions with even distribution)
	 * @return It combines them by weighting
	 *//*
	private double mixHeuristics(double l, double u) {
		// TODO Auto-generated method stub
		//Compute the variables of the future
		ArrayList<String> varsOfFuture;
		int numDecs;
		int numVars;
		double wu;
		double wl;
		double h;
		//Constant that the higher value the higher importance for non-admissible heuristic
		//Value k=3 worked perfectly with the diagrams:
		//externName=path+"uid1Simplified2.elv";
		//externName=path+"UID2.elv";
		//double k=3;
		
		double k=1;
		double sumW;
			
		varsOfFuture=getGraphUID().getGsdag().getDescendantVariables(nameOfVariable,nodeGSDAG);
		
		numVars = varsOfFuture.size();
		
		//We count the number of decisions until the end of the gsdag
		numDecs = 0;
		for (String auxVar:varsOfFuture){
			if (this.uid.getNode(auxVar).getKindOfNode()==Node.DECISION){
				numDecs++;
			}
		}
		
		//Calculate the weights
		wu = (double)numDecs/numVars;
		wl = 1.0-(double)numDecs/numVars;
		wl = (1+k)*wl;
		sumW = wu+wl;
		wu = wu/sumW;
		wl = wl/sumW;
		
		
		
		//Mix the heuristics by weighting them
		h = l*wl+u*wu;
		
		return h;
	}
*/
	

	/**d
	 * @return
	 */
	protected double computeLowerWeightForMixingHeuristics() {
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
		k = getK_chance_For_Estimate();
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
	
	

	protected double getK_chance_For_Estimate() {
	// TODO Auto-generated method stub
		return ((GraphAOUID_Anytime) getGraphUID()).getK_chance();
}

	/**d
	 * @return
	 */
	private boolean areAllTheFutureVariablesAreChance(){
		return areAllTheFutureVariablesAreOfKind(NodeAOUID.CHANCE);
	}
	
	private boolean areAllTheFutureVariablesAreOfKind(int type) {
		// TODO Auto-generated method stub
		ArrayList<String> varsOfFuture;
		int numVarKind;
		int numVars;
		
		varsOfFuture=getGraphUID().getGsdag().getDescendantVariables(nameOfVariable,nodeGSDAG);
		
		numVars = varsOfFuture.size();
		
		//We count the number of decisions until the end of the gsdag
		numVarKind = 0;
		for (String auxVar:varsOfFuture){
			if (this.uid.getNode(auxVar).getKindOfNode()==type){
				numVarKind++;
			}
		}
		return (numVars==numVarKind);
	}

	private boolean areAllTheFutureVariablesAreDecisions(){
		return areAllTheFutureVariablesAreOfKind(NodeAOUID.DECISION);
	}
	
	
	
	
	public NodeAOUID_Anytime(UID uid2, GSDAG gsdag, GraphAOUID graphUID2) {
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
		
		calculateValueOfHeuristic(null);
		
		//pruned = false;
		
		
	}
	
	public NodeAOUID_Anytime(UID uid2, GSDAG gsdag, GraphAOUID graphUID2,Configuration initialInstant) {
		// TODO Auto-generated constructor stub
		
		//Keep a pointer to the UID
		uid = uid2;
		
		//Keep a pointer to the graphUID
		graphUID =graphUID2; 
		
		//nodeGSDAG = gsdag.getRoot();
		nodeGSDAG = gsdag.getNextNode(initialInstant);
		
		if (nodeGSDAG.type!=NodeGSDAG.TypeOfNodeGSDAG.BRANCH){
			nameOfVariable = nodeGSDAG.getVariables().get(0);
			//nameOfVariable = nodeGSDAG.getVariables()
		}
		type = getTypeFromGSDAG(nodeGSDAG.type);
		
		instantiations = initialInstant.duplicate();
		
		solved = false;
		
		calculateValueOfHeuristic(null);
		
		//pruned = false;
		
		
	}

	public NodeAOUID_Anytime() {
		// TODO Auto-generated constructor stub
	}


/*	public boolean isPruned() {
		return pruned;
	}*/

	/*public void setPruned(boolean pruned) {
		this.pruned = pruned;
	}
*/
	@Override
	public boolean isSolved() {
		double k;
		
		k = ((GraphAOUID_Anytime) getGraphUID()).getK_chance();
		// TODO Auto-generated method stub
		return (super.isSolved()||
				(this.areAllTheFutureVariablesAreChance()&&(k>0.0))||
				(this.areAllTheFutureVariablesAreDecisions()&&(k<Double.POSITIVE_INFINITY)));
	}
	
	
	
}
