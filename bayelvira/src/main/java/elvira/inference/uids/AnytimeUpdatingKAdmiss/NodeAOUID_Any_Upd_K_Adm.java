package elvira.inference.uids.AnytimeUpdatingKAdmiss;

import java.util.ArrayList;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Link;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.UID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.GraphAOUIDCoalescence;
import elvira.inference.uids.NodeAOUID;
import elvira.inference.uids.NodeAOUIDCoalescence;
import elvira.inference.uids.NodeGSDAG;
import elvira.inference.uids.AnytimeUpdatingK.NodeAOUID_Any_Upd_K;
import elvira.inference.uids.NodeAOUID.TypeOfNodeAOUID;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.tools.statistics.analysis.Stat;

public class NodeAOUID_Any_Upd_K_Adm extends NodeAOUID_Any_Upd_K{
	
	
	/**
	 * Estimate using admissible search
	 */
	double fUpper;
	
	
	/**
	 * Estimate using the most pessimiistic heuristic
	 */
	double fLower;
	
	
	/**
	 * List of NodeAOUID which are children of this and are pruned because of the bounds
	 */
	ArrayList<NodeAOUID_Any_Upd_K_Adm> pruned;


	private double maxError = 0.000001;;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	//boolean pruned;


	
	public NodeAOUID_Any_Upd_K_Adm(
			UID uid,
			GSDAG gsdag,
			GraphAOUID_Any_Upd_K_Adm graphAOUID,
			double k_chance2) {
		// TODO Auto-generated constructor stub
		super(uid,gsdag,graphAOUID,k_chance2);
		//this.setPruned(false);
		pruned = new ArrayList<NodeAOUID_Any_Upd_K_Adm>(); 
	}




	public NodeAOUID_Any_Upd_K_Adm() {
		// TODO Auto-generated constructor stub
		pruned = new ArrayList<NodeAOUID_Any_Upd_K_Adm>();
	}




	/**
	 * @param father It's the father of the node receiving the message, in the AO graph
	 * It can be used to save computation of the heuristic (for example, when it's implemented
	 * like a list of heuristics.
	 *//*
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
			instantUtilRels = instantiateRelations(last.getUtilityRelations());
			u = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
			l = u;
			setF(u);
		}
		else{
					wl = computeLowerWeightForMixingHeuristics();
					wu = 1-wl;
					//Calculate the upper bound (we need it despite wu = 0)
					if (wu>0.0){
						instantUtilRels = instantiateRelations(last.getUtilityRelations());
//						The upperbound is always the same: The maximum of the utilities
						u = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
					}
					if (wl>0.0){
					instantUtilRels = instantiateRelations(last.getUtilityRelations());
					instantProbRels = instantiateRelations(last.getProbabilityRelations());
					l = heuristicEvenDistributionForDecisions(instantProbRels, instantUtilRels);
					
					
					}
					if (wu==0.0){
						setF(l);
						u = l;
					}
					else if (wl==0.0){
						setF(u);
						l = u;
					}
					else{
						setF(mixHeuristics(wl,l,wu,u));
					}
					
			
		}
		
		setCDLU(l,u);
		//fAdmiss is equal to the upper bound
		this.setFUpper(u);
	
		//fPessim is equal to the lower bound
		this.setFLower(l);

		
				
			
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
			l = u;
			setF(u);
		}
		else{
					wl = computeLowerWeightForMixingHeuristics();
					wu = 1-wl;
					//Calculate the upper bound (we need it despite wu = 0)
					if (wu>0.0){
						instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
//						The upperbound is always the same: The maximum of the utilities
						u = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
					}
					if (wl>0.0){
						//if (wu==0){
					instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
					instantProbRels = instantiateRelations(last.getCurrentProbabilityRelations());
					l = heuristicEvenDistributionForDecisions(instantProbRels, instantUtilRels);
						//}
						//else{//We obtain a better estimate of the lower bound
							//l = heuristicUnconditionalDistributionForDecisions(last.getProbabilityRelations(),last.getUtilityRelations());
						//}
					
					
					
					}
					if (wu==0.0){
						setF(l);
						u = l;
					}
					else if (wl==0.0){
						setF(u);
						l = u;
					}
					else{
						setF(mixHeuristics(wl,l,wu,u));
					}
					
			
		}
		
		setCDLU(l,u);
		//fAdmiss is equal to the upper bound
		this.setFUpper(u);
	
		//fPessim is equal to the lower bound
		this.setFLower(l);

		
				
			
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
			l = u;
			setF(u);
		}
		else{
					wl = computeLowerWeightForMixingHeuristics();
					wu = 1-wl;
					//Calculate the upper bound (we need it despite wu = 0)
					if (wu>0.0){
						instantUtilRels = instantiateRelations(last.getCurrentUtilityRelations());
//						The upperbound is always the same: The maximum of the utilities
						u = heuristicMaximumGlobalUtilityByDP(instantUtilRels);
					}
					if (wl>0.0){
						//if (wu==0){
					l = lowerBounds.getValue(this.getInstantiations());
						//}
						//else{//We obtain a better estimate of the lower bound
							//l = heuristicUnconditionalDistributionForDecisions(last.getProbabilityRelations(),last.getUtilityRelations());
						//}
					
					
					
					}
					if (wu==0.0){
						setF(l);
						u = l;
					}
					else if (wl==0.0){
						setF(u);
						l = u;
					}
					else{
						setF(mixHeuristics(wl,l,wu,u));
					}
					
			
		}
		
		setCDLU(l,u);
		//fAdmiss is equal to the upper bound
		this.setFUpper(u);
	
		//fPessim is equal to the lower bound
		this.setFLower(l);

		
				
			
	}



	private double heuristicUnconditionalDistributionForDecisions(
			RelationList probabilityRelations, RelationList utilityRelations) {
		double[] lower;
		RelationList instantUtilRels;
		RelationList instantProbRels;
		int stateDec;
		Configuration conf;
		NodeList decisionsToBeSet;
		int numStatesDecs;
		int sizeLower;

		decisionsToBeSet = obtainDecisionsInRelations(probabilityRelations,utilityRelations,instantiations);
		
		if (decisionsToBeSet.size()>0){
			numStatesDecs = ((FiniteStates)decisionsToBeSet.elementAt(0)).getNumStates();
		}
		else{
			numStatesDecs = 0;
		}
		sizeLower = numStatesDecs+1;
		
		lower = new double[sizeLower];
		
		// We calculate the lower bound with the uniform distribution
		instantProbRels = instantiateRelations(probabilityRelations);
		instantUtilRels = instantiateRelations(utilityRelations);
		lower[0] = heuristicEvenDistributionForDecisions(instantProbRels, instantUtilRels);
		
		//Unconditional policies

		if (decisionsToBeSet.size()>0){
		
		for (int i=0;i<numStatesDecs;i++){
			stateDec = i;
			conf = duplicateConfigurationAndSetDecisions(instantiations,decisionsToBeSet,stateDec);
			instantUtilRels = instantiateRelations(utilityRelations,conf);
			instantProbRels = instantiateRelations(probabilityRelations,conf);
			lower[i+1]=heuristicEvenDistributionForDecisions(instantProbRels, instantUtilRels);
		}
		}
		
		
		return Stat.max(lower);
	}

	
	private static Configuration duplicateConfigurationAndSetDecisions(
			Configuration instantiations, NodeList decisionsToBeSet,
			int stateDec) {
		// TODO Auto-generated method stub
		Configuration conf;
		
		conf = instantiations.duplicate();
		
		for (int i=0;i<decisionsToBeSet.size();i++){
			conf.putValue((FiniteStates) decisionsToBeSet.elementAt(i), stateDec);
		}
			
		return conf;
		
	}




	private NodeList obtainDecisionsInRelations(RelationList instantProbRels,
			RelationList instantUtilRels,Configuration conf) {
		// TODO Auto-generated method stub
		NodeList decs;
		NodeList vars;
		int length;
		double finalValues[];
		FiniteStates var;
		Relation newDecRel;
		PotentialTable newDecPot;
		NodeList auxNodes;
		
		vars = instantProbRels.getVariables();
		vars.join(instantUtilRels.getVariables());
		
		decs = new NodeList();
		
		//We eliminate all variables through sum using probability and utility potentials
		for(int i=0;i<vars.size();i++){
			if (vars.elementAt(i).getClass()==FiniteStates.class){
			var = (FiniteStates)vars.elementAt(i);
			//We add a uniform distribution for the decision node
			if ((var.getKindOfNode()==Node.DECISION)&&(conf.indexOf(var)==-1)){
				decs.insertNode(var);
			}
			}
		}
		return decs;
	}




	private Configuration duplicateAndSetDecisions(
			Configuration instantiations, int stateDec) {
		// TODO Auto-generated method stub
		return null;
	}




	public double getFUpper() {
		return fUpper;
	}




	public void setFUpper(double admiss) {
		fUpper = admiss;
	}

	
	/**
	 * Method that update the necessary heuristics in node and its parents
	 * if it is necessary.
	 * The node can use an only heuristic for select the node and prune.
	 * Or it can use different heuristics.
	 * The class NodeAOUID uses an heuristic f.
	 * See how the class NodeAOUID_Anytime uses two.
	 */
	public void updateHeuristicInNode(){
		updateHeuristicFUpperInNode();
		updateHeuristicFLowerInNode();
		updateHeuristicFInNode();
		
	}

	/**
	 * It uses dynamic bounds (fLower and fUpper) instead of l and u
	 */
	/*	public void updateK_chanceInNode() {
		// TODO Auto-generated method stub
		//setK_chanceFromCDLUAndF();
		double k;
		double f;
		
		
		f = this.getF();
		
		if (canKBeDetermined()){
		k = (d*(fUpper-f))/(c*(f-fLower));
		}
		else{
			k = 1.0;
		
		}
			System.out.println("Updating k: The value of K in the node "+this.getInstantiations().toString());
			System.out.println("f= "+f+", d= "+d+", c= "+c+", fUpper= "+fUpper+", fLower= "+fLower+". K has changed from k= "+this.getK_chance()+" to k= "+k);
		
			setK_chance(k);
	}*/










	private void updateHeuristicFUpperInNode() {
		// TODO Auto-generated method stub
	
			
			double newFUpper = 0.0;
			NodeList childrenNodes;
			NodeAOUID_Any_Upd_K_Adm auxChild;
			double auxFUpperChild;
		 
			
			
			childrenNodes = this.getChildrenNodes();
			switch (type){
			case BRANCH:
			case DECISION:
				//Maximize over the children
				newFUpper = Double.NEGATIVE_INFINITY;
				
				for (int i=0;i<childrenNodes.size();i++){
					auxChild = (NodeAOUID_Any_Upd_K_Adm)childrenNodes.elementAt(i);
					auxFUpperChild = auxChild.getFUpper();
					if (auxFUpperChild>newFUpper){//We improve newF
						newFUpper = auxFUpperChild;
					}
				}
			
				break;
			case CHANCE:
				//Weighted sum over the children
				newFUpper = 0.0;
				for (int i=0;i<childrenNodes.size();i++){
					auxChild = (NodeAOUID_Any_Upd_K_Adm)childrenNodes.elementAt(i);
					newFUpper = newFUpper + auxChild.getFUpper()*conditionalProbs.getValue(auxChild.getInstantiations());
				}

				break;
			}
			
			if (newFUpper!=fUpper){
				if (newFUpper<fUpper){
				fUpper = newFUpper;
				if (isDebug()) System.out.println("The value of FUpper in the node "+this.getInstantiations().toString()+ " has changed to:"+this.fUpper);
				}
				else{
					if ((newFUpper-maxError)>fUpper){
				System.out.println("Exception: fUpper has increased its value during the execution");
				System.exit(-1);
					}
				}
			}
			//We can update the heuristic in parents always to select other paths when we have tie
			updateHeuristicFUpperInParents();
			
			
		}




	private void updateHeuristicFUpperInParents() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		NodeList parentNodes;
		NodeAOUID_Any_Upd_K_Adm auxParent;
		
		parentNodes = this.getParentNodes();
		for (int i=0;i<parentNodes.size();i++){
			auxParent = (NodeAOUID_Any_Upd_K_Adm)parentNodes.elementAt(i);
			auxParent.updateHeuristicFUpperInNode();
		}
	}

	
	private void updateHeuristicFLowerInNode() {
		
		// TODO Auto-generated method stub
	
			
			double newFLower = 0.0;
			NodeList childrenNodes;
			NodeAOUID_Any_Upd_K_Adm auxChild;
			double auxFLowerChild;
		 
			
			
			childrenNodes = this.getChildrenNodes();
			switch (type){
			case BRANCH:
			case DECISION:
				//Maximize over the children
				newFLower = Double.NEGATIVE_INFINITY;
				
				for (int i=0;i<childrenNodes.size();i++){
					auxChild = (NodeAOUID_Any_Upd_K_Adm)childrenNodes.elementAt(i);
					auxFLowerChild = auxChild.getFLower();
					if (auxFLowerChild>newFLower){//We improve newF
						newFLower = auxFLowerChild;
					}
				}
			
				break;
			case CHANCE:
				//Weighted sum over the children
				newFLower = 0.0;
				for (int i=0;i<childrenNodes.size();i++){
					auxChild = (NodeAOUID_Any_Upd_K_Adm)childrenNodes.elementAt(i);
					newFLower = newFLower + auxChild.getFLower()*conditionalProbs.getValue(auxChild.getInstantiations());
				}

				break;
			}
			
			if (newFLower!=fLower){
				
				if (newFLower>fLower){
					fLower = newFLower;
					updatePruned();
					if (isDebug()) System.out.println("The value of FLower in the node "+this.getInstantiations().toString()+ " has changed to:"+this.fUpper);
				}
				else{
					if ((newFLower+maxError)<fLower){
					//System.out.println("Exception: fLower has decreased its value during the execution. fLower:"+fLower+" and newFLower:"+newFLower);
					//System.exit(-1);
					}
				}
				
			}
			
			updatePruned();
			//We can update the heuristic in parents always to select other paths when we have tie
			updateHeuristicFLowerInParents();
			
			
		}

	
	private void updatePruned() {
		// TODO Auto-generated method stub
		
		NodeAOUID_Any_Upd_K_Adm auxChild;
		NodeList children;
				
		
		
		if ((type==TypeOfNodeAOUID.BRANCH)||(type==TypeOfNodeAOUID.DECISION)){
		
		children = this.getChildrenNodes();
		for (int i=0;i<children.size();i++){
			
			auxChild = (NodeAOUID_Any_Upd_K_Adm) children.elementAt(i);
			
			if ((pruned.contains(auxChild)==false)&&(auxChild.canBePruned(this))){
				if (isDebug()) System.out.println("Pruning the node "+auxChild.getInstantiations()+" as child of "+this.getInstantiations());
				pruned.add(auxChild);
			}
		
		}
		}
		
		
	}




	

	private void updateHeuristicFLowerInParents() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		NodeList parentNodes;
		NodeAOUID_Any_Upd_K_Adm auxParent;
		
		parentNodes = this.getParentNodes();
		for (int i=0;i<parentNodes.size();i++){
			auxParent = (NodeAOUID_Any_Upd_K_Adm)parentNodes.elementAt(i);
			auxParent.updateHeuristicFLowerInNode();
		}
	}

	
	/**
	 * @return The probability of a configuration in the current hypergraph solution, but giving a small probability to the branches
	 * that have probability zero in the current strategy. Branches which can be pruned because of the bounds are assigned to probability 0.0
	 * Configurations not in the solution has probability 0
	 */
	public Double getProbabilityUniformNotPruned() {
		// TODO Auto-generated method stub
		NodeList parents;
		NodeAOUID_Any_Upd_K_Adm auxParent;
		double prob;
		double auxProb=0.0;
		
		parents = this.getParentNodes();
		if ((parents==null)||(parents.size()==0)){
			prob= 1.0;
		}
		else{
			//CORREGIR ESTO: PUEDE QUE POR UN CAMINO LA PROBABILIDAD
			//SEA CERO PERO POR OTRO NO. HAY QUE SUMAR LA PROBABILIDAD
			//QUE SE OBTIENE POR TODOS LOS CAMINOS PARENTALES
			prob = 0.0;
			//Sum the probability through the different parents
			for (int i = 0; i < parents.size(); i++) {

				auxParent = (NodeAOUID_Any_Upd_K_Adm) parents.elementAt(i);
				switch (auxParent.getTypeOfNodeAOUID()) {
				case BRANCH:
				case DECISION:
					if (auxParent.getBestChildInPartialSolution() == this) {
						auxProb = modifyProbForTheBestChildUniform(auxParent)*auxParent.getProbability();
					} else if (auxParent.isPruned(this)==false){
						auxProb = giveSmallProbForTheNonBestChildUniform(auxParent)*auxParent.getProbability();
					}
					else{
						auxProb = 0.0;
					/*	System.out.println("Child pruned because its bounds are: ");
						printDynamicBounds();
						System.out.println("While the bounds for its father are: ");
						auxParent.printDynamicBounds();*/
					}
					break;
				case CHANCE:
					auxProb = auxParent.getConditionalProbs().getValue(getInstantiations())	* auxParent.getProbability();
					break;
				}
				prob = prob + auxProb;
			}
			
		}
		return prob;
	}

	/**
	 * @return The probability of a configuration in the current hypergraph solution, but giving a non-zero probability to the branches
	 * that have probability zero in the current strategy. Branches which can be pruned because of the bounds are assigned to probability 0.0
	 * Configurations not in the solution has probability 0. Not pruned branches have a probability proportional to the sum of the utilities of the non-pruned
	 * branches.
	 */
	public Double getProbabilityProportionalUtilitiesNotPruned() {
		// TODO Auto-generated method stub
		NodeList parents;
		NodeAOUID_Any_Upd_K_Adm auxParent;
		double prob;
		double auxProb=0.0;
		
		parents = this.getParentNodes();
		if ((parents==null)||(parents.size()==0)){
			prob= 1.0;
		}
		else{
			//CORREGIR ESTO: PUEDE QUE POR UN CAMINO LA PROBABILIDAD
			//SEA CERO PERO POR OTRO NO. HAY QUE SUMAR LA PROBABILIDAD
			//QUE SE OBTIENE POR TODOS LOS CAMINOS PARENTALES
			prob = 0.0;
			//Sum the probability through the different parents
			for (int i = 0; i < parents.size(); i++) {

				auxParent = (NodeAOUID_Any_Upd_K_Adm) parents.elementAt(i);
				switch (auxParent.getTypeOfNodeAOUID()) {
				case BRANCH:
				case DECISION:
					if (auxParent.getBestChildInPartialSolution() == this) {
						auxProb = giveProbabilityProportionalUtilities(auxParent)*auxParent.getProbability();
					} else if (auxParent.isPruned(this)==false){
						auxProb = giveProbabilityProportionalUtilities(auxParent)*auxParent.getProbability();
					}
					else{
						auxProb = 0.0;
					/*	System.out.println("Child pruned because its bounds are: ");
						printDynamicBounds();
						System.out.println("While the bounds for its father are: ");
						auxParent.printDynamicBounds();*/
					}
					break;
				case CHANCE:
					auxProb = auxParent.getConditionalProbs().getValue(getInstantiations())	* auxParent.getProbability();
					break;
				}
				prob = prob + auxProb;
			}
			
		}
		return prob;
	}

	/**
	 * @param auxParent
	 * @return A probability for the node proportional to the sum of the probabilities of the non-pruned
	 * nodes
	 */
	private Double giveProbabilityProportionalUtilities(
			NodeAOUID_Any_Upd_K_Adm auxParent) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		NodeList auxChildren;
		double sum;
		NodeAOUID_Any_Upd_K_Adm auxChild;
			
		
		auxChildren = auxParent.getChildrenNodes();
		sum = 0;
		for (int i=0;i<auxChildren.size();i++){
			auxChild= (NodeAOUID_Any_Upd_K_Adm) auxChildren.elementAt(i);
			if (auxParent.isPruned(auxChild)==false){
				sum = sum + auxChild.getF();
			}
		}
		return (this.getF()/sum);
	}







	private boolean isPruned(
			NodeAOUID_Any_Upd_K_Adm child) {
		// TODO Auto-generated method stub
		return pruned.contains(child);
	}


	
	/**
	 * @return true iff the node is pruned because it is impossible to find a path from the root
	 * where all the nodes of those path are not pruned
	 */
	public boolean isPruned(){
		NodeList parents;
		NodeAOUID_Any_Upd_K_Adm auxParent;
		boolean isPruned;
		
		parents = this.getParentNodes();
		if ((parents==null)||(parents.size()==0)){
			isPruned = false;
		}
		else{
			isPruned = true;
			
			for (int i = 0; (i < parents.size())&&isPruned; i++) {

				auxParent = (NodeAOUID_Any_Upd_K_Adm) parents.elementAt(i);
				switch (auxParent.getTypeOfNodeAOUID()) {
				case BRANCH:
				case DECISION:
					isPruned = ((auxParent.isPruned())||(auxParent.isPruned(this)));
					break;
				case CHANCE:
					isPruned = auxParent.isPruned();
					break;
				}
			}
		}
		return isPruned;
		
		
		
	}


	private void printDynamicBounds() {
		// TODO Auto-generated method stub
		System.out.println("FLower= "+fLower+" and fUpper= "+fUpper);
	}




	private double giveSmallProbForTheNonBestChildUniform(
			NodeAOUID_Any_Upd_K_Adm parent) {
		// TODO Auto-generated method stub
		return giveProbEvenDistrib(parent);
		//return 0.1;
	}




	

	/**
	 * The probability is assigned using a uniform distribution for the non-pruned children
	 * @param auxParent
	 * @return the probability that we assigns to the child with highest estimate
	 */
	private Double modifyProbForTheBestChildUniform(
			NodeAOUID_Any_Upd_K_Adm parent) {
		return giveProbEvenDistrib(parent);
		//return 1.0;
	}

	/**
	 * @param auxParent
     * @return the probability that we assigns to the child with highest estimate
	 */
	private Double giveProbEvenDistrib(NodeAOUID_Any_Upd_K_Adm parent) {
		// TODO Auto-generated method stub
		double numPruned;
		double numNotPruned;
		double numChildren;
		double prob;
		
		numPruned = parent.pruned.size();
		
		numChildren = parent.getChildren().size();
		
		numNotPruned = numChildren - numPruned;
		
		prob = 1.0/numNotPruned;
		
		return prob;
		
	}




	private double getNumberOfChildrenCanBePruned() {
		// TODO Auto-generated method stub
		
		double numCanBePruned;
		NodeAOUID_Any_Upd_K_Adm auxChild;
		NodeList children;
				
		
		
		numCanBePruned = 0; 
		
		children = this.getChildrenNodes();
		
		for (int i=0;i<children.size();i++){
			auxChild = (NodeAOUID_Any_Upd_K_Adm) children.elementAt(i);
		
			if (auxChild.canBePruned(this)){
				numCanBePruned = numCanBePruned+1;
			}
		}
		
		return numCanBePruned;
	}




	/**
	 * @param parent
	 * @return true iff the 'parent' node can prune this child because of the bounds
	 */
	private boolean canBePruned(NodeAOUID_Any_Upd_K_Adm parent) {
		// TODO Auto-generated method stub
		return (parent.getFLower()>(this.getFUpper()+0.000001));
	}



	/**
	 * @return The probability of a configuration in the current hypergraph solution
	 * Configurations not in the solution has probability 0
	 */
	@Override
	public Double getProbability() {
		// TODO Auto-generated method stub
		return getProbabilityUniformNotPruned();
		//return this.getProbabilityProportionalUtilitiesNotPruned();
	}



	public double getFLower() {
		return fLower;
	}




	public void setFLower(double lower) {
		fLower = lower;
	}
	
	public void printEstimates() {
		// TODO Auto-generated method stub
		super.printEstimates();
		System.out.println("Dynamic bounds: fLower="+this.fLower+" and fUpper="+this.fUpper);
	}

	
	@Override
	public NodeAOUID_Any_Upd_K_Adm copy() {
		// TODO Auto-generated method stub
		
		NodeAOUID_Any_Upd_K_Adm auxNode = new NodeAOUID_Any_Upd_K_Adm();
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
		auxNode.setFLower(this.getFLower());
		auxNode.setFUpper(this.getFUpper());
		//auxNode.setPruned(this.isPruned());
		
		return auxNode;
	}




	
	@Override
	protected double getEUOfCurrentStrategyForLeaves() {
		// TODO Auto-generated method stub
		if (this.fLower>0) return fLower;
		else return super.getEUOfCurrentStrategyForLeaves();
	}




	public double range() {
		// TODO Auto-generated method stub
		return (fUpper-fLower);
	}

	
	
	/**
	 * @param newNameOfVariable 
	 * @return The minimum set of variables whose coincidence between scenarios is required for coalescence
	 *//*
	private NodeList obtainMinimumSetOfVariablesForCoalescence(){
		ArrayList<String> varsOfFuture;
		NodeGSDAG last;
		NodeList varsOfRelations;
		RelationList rels=null;
		RelationList relsP,relsU;
		
		last = getGraphUID().getGsdag().getLastNodeGSDAG();
		
		//Compute the variables of the future
		varsOfFuture=getGraphUID().getGsdag().getDescendantVariables(nameOfVariable,nodeGSDAG);
		//The variable of this node is not considered part of the future
		varsOfFuture.remove(this.nameOfVariable);
		
		//Join all the relations
		rels = new RelationList();
		relsP= last.getProbabilityRelations();
		relsU= last.getUtilityRelations();
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
		System.out.println("* Coalescence requires coincidence in variables:");
		for (int i=0;i<varsOfRelations.size();i++){
			System.out.println(varsOfRelations.elementAt(i).getName());
		}
		
		return varsOfRelations;
	}
	
	
	*/
	

/*	*//**
	 * @param fullConf
	 * @return A nodeAOUID whose configuration is equivalent to fullConf
	 *//*
	public NodeAOUID_Any_Upd_K_Adm improvedGetNodeAOUID(Configuration fullConf,NodeList varsRequiringCoincidence) {
		// TODO Auto-generated method stub
		NodeAOUID_Any_Upd_K_Adm auxNode;
		boolean found;
		NodeAOUID_Any_Upd_K_Adm foundNode=null;
		int valueOfVariable;
		
		
		if (instantiations.size()==fullConf.size()){
//			If we reach this point is because 'instantiations' and 'fullConf' are equivalent
			foundNode = this;
		}
		else{
			//The size of instantiations is lower than the size of fullConf.
			
			switch (type){
			case BRANCH:
				found = false;
				for (int i=0;(i<children.size())&&(found==false);i++){
					//We look for in all the children of the branch until we found
					//something different of null
					auxNode = ((NodeAOUID_Any_Upd_K_Adm)(children.elementAt(i).getHead())).improvedGetNodeAOUID(fullConf,varsRequiringCoincidence);
					if (auxNode!=null){
						foundNode = auxNode;
						found = true;
					}
				}
				break;
			case CHANCE:
			case DECISION:
				if (children.size() > 0) {
					valueOfVariable = fullConf.getValue(nameOfVariable);
					
					if (valueOfVariable!=-1){
						// The configuration contains the variable
						if (varsRequiringCoincidence.getId(nameOfVariable) != -1){
//							 We continue the search through the corresponding
							// child
							foundNode = ((NodeAOUID_Any_Upd_K_Adm) (children
									.elementAt(valueOfVariable).getHead()))
									.improvedGetNodeAOUID(fullConf,varsRequiringCoincidence);
						}
						else{
							found = false;
							// We don't need coincidence in the variable, so we
							// admit every path
							for (int i = 0; (i < children.size())
									&& (found == false); i++) {
								// We look for in all the children of the node until
								// we found
								// something different of null
								auxNode = ((NodeAOUID_Any_Upd_K_Adm) (children.elementAt(i)
										.getHead())).improvedGetNodeAOUID(fullConf,varsRequiringCoincidence);
								if (auxNode != null) {
									foundNode = auxNode;
									found = true;
								}
							}
							
						}
						
					}
					else{//The configuration doesn't contain the variable
						foundNode = null;
					}
					
				
				} else {
					// If the node is a leaf then we haven't found the node that
					// we were
					// looking for
					foundNode = null;
				}
				break;
		}
		
	}
		return foundNode;
	}
*/
	
/*	//It generates the sucessors of the node to expand
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
		
		sucessors = new ArrayList();
		
		if (isSolved()){
			
			//System.out.println("Removing from open the node with instantiations "+instantiations.toString());
			return sucessors;
		}
		else if (isChildOfChanceNodeAOUID()&&hasZeroProbability()){
		
			//System.out.println("We remove from open the node with instantiatons "+instantiations.toString()+" because it has probability 0");
			return sucessors;
		}
		else{
			
		System.out.println("* Expanding the node of kind "+type.toString()+", name "+nameOfVariable+" and instantiations:");
		System.out.println(instantiations.toString());
		//instantiations.print();
		//System.out.println("");
		
		
		
		switch (type){
		case DECISION:
		case CHANCE:
				//Set the new name of variable and the nodeGSDAG
				newNameOfVariable = nodeGSDAG.nextVariable(nameOfVariable);
				newNodeGSDAG = nextNodeGSDAG();
				newType = getTypeFromGSDAG(newNodeGSDAG.getTypeOfNodeGSDAG());
				nodeUID = (FiniteStates) uid.getNode(nameOfVariable);
				if (type == TypeOfNodeAOUID.CHANCE){
				conditionalProbs = calculateConditionedProbabilities();
				//conditionalProbs = calculateConditionedProbabilitiesSSP();
				
				}
				//System.out.println("Children of the expanded node:");
				//Copy the parent in each child and set the new values for it
				NodeList varsReq = obtainMinimumSetOfVariablesForCoalescence();
				
				for (int i=0;i<nodeUID.getNumStates();i++){
					existingNodeAOUID = ((GraphAOUIDCoalescence)graphUID).improvedGetNodeAOUID(instantiations,nodeUID,i,varsReq);
					
					if (existingNodeAOUID == null){
//					Copy of the current NodeAOUID in the child
					newNodeAOUID = copy();
					newNodeAOUID.setNameOfVariable(newNameOfVariable);
					newNodeAOUID.setNodeGSDAG(newNodeGSDAG);
					newNodeAOUID.setType(newType);
					
					newNodeAOUID.instantiations.insert(nodeUID,i);
					
					((NodeAOUID_Any_Upd_K_Adm)newNodeAOUID).calculateValueOfHeuristic(this);
					//System.out.println("Node "+newNodeAOUID.getInstantiations().toString()+ " F:"+newNodeAOUID.getF());
					
					if (type == TypeOfNodeAOUID.CHANCE){
						//System.out.println("Probability: "+conditionalProbs.getValue(newNodeAOUID.getInstantiations()));
					}
					graphUID.addNode(newNodeAOUID);
					
					newNodeAOUID.setOpen(true);
					newNodeAOUID.setSolved(isSolvedNodeAOUID());
					sucessors.add(newNodeAOUID);
					graphUID.setNumberOfNodes(graphUID.getNumberOfNodes()+1);
					}
					else {
						System.out.println("State not added to the graph of search because it exists");
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
	*/


	/*public boolean isPruned() {
		return pruned;
	}*/




	/*public void setPruned(boolean pruned) {
		this.pruned = pruned;
	}*/
}