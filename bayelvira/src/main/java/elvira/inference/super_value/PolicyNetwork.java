package elvira.inference.super_value;

import java.util.Vector;


import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.InvalidEditException;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;

/**
 * @author Manolo_Luque
 * It creates a policy network (see Jensen's book) for the influence diagram
 *
 */
public class PolicyNetwork extends CooperPolicyNetwork {
	
	public PolicyNetwork(){
		
	}
	
	public PolicyNetwork(IDWithSVNodes id){
		int propagMethod;
		
		propagMethod = 4;
		Vector parametersPropagation = null;
		
		//Nodes and links (except links to decision nodes)
		setStructureOfPNFrom(id);
			
		//Probability relations of chance nodes
		setRelationsOfChanceNodesFrom(id);
		
		//Links and probability relations of decision nodes
		setOptimalPoliciesAndRelationsOfDecisionNodesFrom(id, propagMethod, parametersPropagation);
		
		
	}
	
	
	public PolicyNetwork(IDWithSVNodes id,ArcReversalSV propag){
				
		//Nodes and links (except links to decision nodes)
		setStructureOfPNFrom(id);
			
		//Probability relations of chance nodes
		setRelationsOfChanceNodesFrom(id);
		
		//Links and probability relations of decision nodes
		setOptimalPoliciesAndRelationsOfDecisionNodesFrom(id,propag);
		
		
	}

	private void setOptimalPoliciesAndRelationsOfDecisionNodesFrom(
			IDWithSVNodes id, ArcReversalSV propag) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		Vector policies;
		Relation deterministicRelationInNew;
		NodeList varsDeterministicRelationInNew;
		Node decisionInNew;

		// List of optimal policies
		//policies = id.getPropagation().getResults();
		policies = propag.getResultsForPolicies();

		// Create the incoming links to the decisions and the their
		// deterministic relations
		// according to optimal policies
		for (int i = 0; i < policies.size(); i++) {
			// Compute the new deterministic relation, refered to the CPN
			deterministicRelationInNew = toDeterministicRelation((PotentialTable) (policies
					.elementAt(i)));

			// Compute the variables and the decision of the new relation,
			// refered to the CPN
			varsDeterministicRelationInNew = deterministicRelationInNew
					.getVariables();
			decisionInNew = varsDeterministicRelationInNew.elementAt(0);

			// Create the links according to the deterministic relation of the
			// decision
			for (int j = 1; j < varsDeterministicRelationInNew.size(); j++) {
				try {
					createLink(varsDeterministicRelationInNew.elementAt(j),
							decisionInNew);
				} catch (InvalidEditException iee) {
					;
				}

			}

			// Remove the relation that appears because of the creation of links
			// and substitute it by the new deterministic relation
			removeRelation(decisionInNew);
			addRelation(deterministicRelationInNew);
		}

	}

	public PolicyNetwork(ArcReversalSV2 ar) {
		// TODO Auto-generated constructor stub
	int propagMethod;
		IDWithSVNodes id;
		
		
		id = ar.getDiag();
		propagMethod = 4;
		Vector parametersPropagation = null;
		
		//Nodes and links (except links to decision nodes)
		setStructureOfPNFrom(id);
			
		//Probability relations of chance nodes
		setRelationsOfChanceNodesFrom(id);
		
		//Links and probability relations of decision nodes
		setOptimalPoliciesAndRelationsOfDecisionNodesFrom(ar);
	}

	private void setOptimalPoliciesAndRelationsOfDecisionNodesFrom(
			ArcReversalSV2 ar) {
		// TODO Auto-generated method stub
		Relation deterministicRelationInNew;
		NodeList varsDeterministicRelationInNew;
		Node decisionInNew;

	
		// List of optimal policies
		//policies = id.getPropagation().getResults();
		StochasticStrategy strategy = ar.getStochasticStrategy();
		NodeList decisions = ar.getDiag().getNodesOfKind(Node.DECISION);
		
		// Create the incoming links to the decisions and the their
		// deterministic relations
		// according to optimal policies
		for (int i = 0; i < decisions.size(); i++) {
			// Compute the new deterministic relation, refered to the CPN
			deterministicRelationInNew = translatePotentialToRelationOfPN(strategy.getPolicy((FiniteStates) decisions.elementAt(i)));

			// Compute the variables and the decision of the new relation,
			// refered to the CPN
			varsDeterministicRelationInNew = deterministicRelationInNew
					.getVariables();
			decisionInNew = varsDeterministicRelationInNew.elementAt(0);

			// Create the links according to the deterministic relation of the
			// decision
			for (int j = 1; j < varsDeterministicRelationInNew.size(); j++) {
				try {
					createLink(varsDeterministicRelationInNew.elementAt(j),
							decisionInNew);
				} catch (InvalidEditException iee) {
					;
				}

			}

			// Remove the relation that appears because of the creation of links
			// and substitute it by the new deterministic relation
			removeRelation(decisionInNew);
			addRelation(deterministicRelationInNew);
		}

	}

	private Relation translatePotentialToRelationOfPN(PotentialTable policy) {
		// TODO Auto-generated method stub
		
			Relation newRel;
			Node decisionInNew;
			Potential decisionTableInNew;
				
			
			decisionTableInNew = translatePotentialSharingValues(this,policy);
			
			decisionInNew = (Node)(decisionTableInNew.getVariables().lastElement());
			
			//New relation setting its potential to a deterministic potential
			newRel = new Relation(decisionTableInNew.getVariables());
			newRel.setValues(decisionTableInNew);
			
			return newRel;
				
		
	}
	
	
	  
	

	/*private void setStructureOfPNFrom(IDWithSVNodes id) {
		// TODO Auto-generated method stub
		Graph g;
		NodeList decisions;
		Node auxDec;
		LinkList linksToDec;
		Link auxLink;
		Node auxTail;
		Node auxHead;
	
		
		// TODO Auto-generated method stub
		g = id.duplicate();
		
		//Set the nodes
		setNodeList(id.getNodesOfKind(Node.CHANCE,Node.DECISION));
		
		//Set the links
		LinkList linksOfThis = this.getLinkList();
		LinkList auxLinks = g.getLinkList();
		for (int iLink=0;iLink<auxLinks.size();iLink++){
			auxLink = auxLinks.elementAt(iLink);
			auxTail = auxLink.getTail();
			auxHead = auxLink.getHead();
			if ((this.getNodePosition(auxTail)!=-1)&&(this.getNodePosition(auxHead)!=-1)){
				linksOfThis.insertLink(auxLink);
			}
				
							
	//Convert decision nodes into chance nodes without parents
		decisions=getNodesOfKind(Node.DECISION);
		for (int i=0;i<decisions.size();i++){
			auxDec=decisions.elementAt(i);
			linksToDec=auxDec.getParents().copy();
			//Remove the incoming links to the decision
			for (int j=0;j<linksToDec.size();j++){
				auxLink=linksToDec.elementAt(j);
				try{
					removeLink(auxLink);
				}catch(InvalidEditException iee){;}
					
			}
			//Convert the decision into a chance node
			auxDec.setKindOfNode(Node.CHANCE);
			auxDec.setTypeOfVariable(Node.FINITE_STATES);
		}
		
	}*/
}
