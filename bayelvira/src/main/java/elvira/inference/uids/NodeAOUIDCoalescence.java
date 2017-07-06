package elvira.inference.uids;

import java.util.ArrayList;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.UID;
import elvira.inference.uids.NodeAOUID.TypeOfNodeAOUID;
import elvira.potential.Potential;

public class NodeAOUIDCoalescence extends NodeAOUID {

	
	public NodeAOUIDCoalescence(UID uid, GSDAG gsdag, GraphAOUIDCoalescence graph) {
		// TODO Auto-generated constructor stub
		super(uid,gsdag,graph);
	}



	public NodeAOUIDCoalescence() {
		// TODO Auto-generated constructor stub
	}



	
	/**
	 * @param newNameOfVariable 
	 * @return The minimum set of variables whose coincidence between scenarios is required for coalescence
	 */
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
		System.out.println("* Coalescence requires coincidence in variables:");
		for (int i=0;i<varsOfRelations.size();i++){
			System.out.println(varsOfRelations.elementAt(i).getName());
		}
		
		return varsOfRelations;
	}
	
	
	
	private NodeList obtainVarsOfRelationsWhereAppear(RelationList rels, ArrayList<String> varsOfFuture) {
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



	/**
	 * @param fullConf
	 * @return A nodeAOUID whose configuration is equivalent to fullConf
	 */
	public NodeAOUIDCoalescence improvedGetNodeAOUID(Configuration fullConf,NodeList varsRequiringCoincidence) {
		// TODO Auto-generated method stub
		NodeAOUIDCoalescence auxNode;
		boolean found;
		NodeAOUIDCoalescence foundNode=null;
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
					auxNode = ((NodeAOUIDCoalescence)(children.elementAt(i).getHead())).improvedGetNodeAOUID(fullConf,varsRequiringCoincidence);
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
							foundNode = ((NodeAOUIDCoalescence) (children
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
								auxNode = ((NodeAOUIDCoalescence) (children.elementAt(i)
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
					
					newNodeAOUID.calculateValueOfHeuristic(this);
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
	
	public NodeAOUID copy(){
		NodeAOUIDCoalescence auxNode;
		
		auxNode = new NodeAOUIDCoalescence();
		auxNode.uid = uid;
		auxNode.graphUID = graphUID;
		auxNode.instantiations = instantiations.duplicate();
		auxNode.f = f;
		auxNode.type = type;
		auxNode.nameOfVariable = nameOfVariable;
		auxNode.nodeGSDAG = nodeGSDAG;

		return auxNode;
	}
	
}
