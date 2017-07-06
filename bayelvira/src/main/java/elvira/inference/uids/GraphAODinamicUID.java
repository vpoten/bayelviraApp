package elvira.inference.uids;

import java.util.ArrayList;
import java.util.Vector;

import elvira.InvalidEditException;
import elvira.NodeList;
import elvira.RelationList;
import elvira.UID;

public class GraphAODinamicUID extends GraphAOUID {
	
	public GraphAODinamicUID(){
		
	}
	
	public GraphAODinamicUID(UID uid, GSDAG gsdag2, Boolean applyDinamicWeighting2) {
		//super(uid);
		// TODO Auto-generated constructor stub
		
		NodeAODinamicUID initialState;
		// TODO Auto-generated constructor stub
		//open = new ArrayList();
		//closed = new ArrayList();
		
		applyDynamicWeighting = applyDinamicWeighting2;
		gsdag = gsdag2;
		
		initialState = new NodeAODinamicUID(uid,gsdag,this);
		
		addNode(initialState);
		
		root = initialState;
		
		//open.add(initialState);
		initialState.setOpen(true);
		
		numberOfNodes = 1;
	}

	
	public void expand(NodeAOUID nodeToExpand) {
		// TODO Auto-generated method stub
		//super.expand(nodeToExpand);
		
		ArrayList<NodeAOUID> sucessors;
		boolean modifiedF;
		// TODO Auto-generated method stub
		//Remove nodeToExpand from open
		//open.remove(nodeToExpand);
		//closed.add(nodeToExpand);
		nodeToExpand.setOpen(false);
		
		
		if (((NodeAODinamicUID) nodeToExpand).isCoveredByDP()) {// Frontier with
																// the DP
		/*if ((((NodeAODinamicUID) nodeToExpand).isCoveredByDP())||
				(((NodeAODinamicUID) nodeToExpand).hasObsoleteHeuristic()))*/
				//{// Frontier with
			// For the nodes covered by
			((NodeAODinamicUID)nodeToExpand).calculateValueOfHeuristic(null);
			//((NodeAODinamicUID)nodeToExpand).setHasObsoleteF(false);
			System.out.println("* Recalculating the heuristic for the node of kind "+nodeToExpand.type.toString()+", name "+nodeToExpand.nameOfVariable+" and instantiations "+nodeToExpand.instantiations.toString());
			nodeToExpand.updateHeuristicInParents();

		} else {// Not in the frontier with the DP
			sucessors = nodeToExpand.generateSucessors();

			if (sucessors.size() > 0) {

				for (NodeAOUID auxSuc : sucessors) {
					// Link from the node of decisions to the node of chance
					// variables
					try {
						createLink(nodeToExpand, auxSuc);
					} catch (InvalidEditException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				nodeToExpand.updateHeuristicInNode();
			}
		}
		
	}



	public void reCalculateOpen() {
		// TODO Auto-generated method stub
		
		
	}

	public void pruneTheTreeAfterDPAndMarkFInOpenNodesAsObsolete() {
		// TODO Auto-generated method stub
		ArrayList<NodeAOUID> newOpen;
		
		
		newOpen = new ArrayList();
		((NodeAODinamicUID)root).pruneAfterDP(newOpen);
		//(Reimplementation becuase of the changes in the way of storing open nodes.)
		//We mark all the nodes of newOpen like open and F like obsolote
		for (NodeAOUID auxNodeAOUID:newOpen){
			auxNodeAOUID.setOpen(true);
			((NodeAODinamicUID)auxNodeAOUID).setHasObsoleteF(true);
		}
		
		this.setNumberOfNodes(this.countNumberOfNodesInTheTree());
		
	}



	


	/*public void markFInOpenNodesAsObsolete() {
		// TODO Auto-generated method stub
		for (NodeAOUID auxNode:open){
			((NodeAODinamicUID)auxNode).setHasObsoleteF(true);
			
		}
	}*/


	


	
	
}
