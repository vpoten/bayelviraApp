/*
 * Created on 19-ene-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package elvira.inference.super_value;

import java.io.IOException;
import java.util.Vector;

import elvira.Bnet;
import elvira.Continuous;
import elvira.Evidence;
import elvira.Graph;
import elvira.IDWithSVNodes;
import elvira.IDiagram;
import elvira.InvalidEditException;
import elvira.Link;
import elvira.LinkList;
import elvira.Network;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.parser.ParseException;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.FiniteStates;

/**
 * @author Manolo
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/**
 * @author Manolo
 *
 */
public class CooperPolicyNetwork extends Bnet {
	
	private class CooperNode{
		Node node;
		double maxValue;
		double minValue;
		public CooperNode(Node n,double min,double max){
			super();
			node = n;
			maxValue = max;
			minValue = min;
		}
	
		
			/**
		 * @return Returns the maxValue.
		 */
		public double getMaxValue() {
			return maxValue;
		}
		/**
		 * @param maxValue The maxValue to set.
		 */
		public void setMaxValue(double maxValue) {
			this.maxValue = maxValue;
		}
		/**
		 * @return Returns the minValue.
		 */
		public double getMinValue() {
			return minValue;
		}
		/**
		 * @param minValue The minValue to set.
		 */
		public void setMinValue(double minValue) {
			this.minValue = minValue;
		}
		/**
		 * @return Returns the node.
		 */
		public Node getNode() {
			return node;
		}
		/**
		 * @param node The node to set.
		 */
		public void setNode(Node node) {
			this.node = node;
		}
}
	
	private class CooperTable{
		Vector table;
		
		
		/**
		 * Constructor
		 */
		public CooperTable() {
			super();
			table = new Vector();
		}
		public CooperNode getCooperNode(Node value){
			CooperNode aux;
			CooperNode auxFounded=null;
			for (int i=0;i<table.size();i++){
				aux = (CooperNode)(table.elementAt(i));
				if (aux.getNode().getName().equals(value.getName())){
					auxFounded = aux;
				}
			}
			return auxFounded;
		}
		
		public CooperNode elementAt(int i){
			return (CooperNode)(table.elementAt(i));
		}
		
		public void insertCooperNode(CooperNode valueNodeCPN){
			table.add(valueNodeCPN);
		}
		
		public NodeList getValueNodes(){
			NodeList nl;
						
			nl = new NodeList();
			for (int i=0;i<table.size();i++){
				nl.insertNode(elementAt(i).getNode());
			}
			return nl;
		}
	}
	
	CooperTable vectorValueNodesCPN;
	
	//Vector of ValueNodeCPN
	//For each value node of the original ID we store in the CPN:
	//Value node, the maximum and the minimum of the its utilities
	//This information is necessary for the inverse transformation of Cooper.
	CooperTable cooperTable;
	
//	Vector optimalPolicies;
	
	public CooperPolicyNetwork() {
		super();
		cooperTable = new CooperTable();
	}
	
	/**
	 * It constructs a policy network (Jensen, Nilsson) for the influence diagram 'id'.
	 * It adds the utility nodes as chance nodes according to Cooper's transformation 
	 * @param propagationMethod
	 * @param parametersPropagation
	 * @throws InvalidEditException
	 */
	public static CooperPolicyNetwork constructCPNFrom(IDWithSVNodes id, int propagationMethod, Vector parametersPropagation) {
		CooperPolicyNetwork cpn;
		
		cpn= new CooperPolicyNetwork();
		
		//ReductionAndEvalID.
		
		//Nodes and links (except links to decision nodes)
		cpn.setStructureOfCPNFrom(id);
		
		//Probability relations of chance nodes
		cpn.setRelationsOfChanceNodesFrom(id);
		
		//Links and probability relations of decision nodes
		cpn.setOptimalPoliciesAndRelationsOfDecisionNodesFrom(id, propagationMethod, parametersPropagation);
		
		//Relation of utility node through Cooper's transformation
		cpn.setRelationsOfValueNodesFrom(id);
		
		return cpn;
	}

	
	/**
	 * @return Returns the cooperTable.
	 */
	public CooperTable getCooperTable() {
		return cooperTable;
	}
	/**
	 * @param cooperTable The cooperTable to set.
	 */
	public void setCooperTable(CooperTable cooperTable) {
		this.cooperTable = cooperTable;
	}
	
	/**
	 * Given a Cooper Policy Network and a node of it, this method return the node of the influence
	 * diagram 'id' whose name is the same.
	 * @param nodeOfCPN Node of a Cooper Policy Network
	 * @param id Influence diagram with super value nodes
	 * @return Node of 'id' with the same name that 'nodeOfCPN'
	 *//*
	public Node getNodeOfID(Node nodeOfCPN,IDWithSVNodes id){
		
		
		
		
	}*/
	/**
	 * Given a node of an influence diagram with sv nodest, this method return the node of
	 * this Cooper Policy Network whose name is the same.
	 * @param nodeOfID Node of an influence diagram with sv nodes
	 * @return Node of the CPN with the same name that 'nodeOfID'
	 */
	public Node getNodeOfCPN(Node nodeOfID){
		return getNode(nodeOfID.getName());
		
	}
	/**
	 * @param id Original influence diagram, whose evaluation will give us the deterministic relations of decisions
	 */
	protected void setOptimalPoliciesAndRelationsOfDecisionNodesFrom(IDWithSVNodes id,int methodPropagation,Vector parametersPropagation) {
		// TODO Auto-generated method stub
		Vector policies;
		Relation deterministicRelationInNew;
		NodeList varsDeterministicRelationInNew;
		Node decisionInNew;

		// Compile the influence diagram
		id.compile(methodPropagation, parametersPropagation);

		// List of optimal policies
		//policies = id.getPropagation().getResults();
		policies = ((ArcReversalSV)id.getPropagation()).getResultsForPolicies();

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
		

	/**
	 * It sets a uniform distribution for decision nodes
	 * @param id Original influence diagram, whose evaluation will give us the deterministic relations of decisions
	 */
	public void setRandomPoliciesAndRelationsOfDecisionNodesFrom(IDWithSVNodes id) {
		// TODO Auto-generated method stub
		//Copy the relations of chance nodes
		NodeList decisions;
		String nameDec;
		Node auxNode;
	
		decisions = id.getNodesOfKind(Node.DECISION);
		
		//Create a relation for each decision node
		for (int i=0;i<decisions.size();i++){
			nameDec = decisions.elementAt(i).getName();
			auxNode = this.getNode(nameDec);
			this.addRelation(auxNode);
		}
		
	}		

	/**
	 * @param id
	 */
	public void setRelationsOfChanceNodesFrom(IDWithSVNodes id) {
		// TODO Auto-generated method stub
		//Copy the relations of chance nodes
		Vector rlOfID;
		Vector rlOfCPN;
		Relation auxRel;
		Relation newRel;
			
		rlOfID = id.getRelationList();
		rlOfCPN = new Vector();
			  
		for (int i=0;i<rlOfID.size();i++){
			//We only copy the relation of the chance nodes
			auxRel = (Relation)(rlOfID.elementAt(i));
		  	if (auxRel.getVariables().elementAt(0).getKindOfNode()==Node.CHANCE){		  		 
		  		newRel=translateRelation(auxRel);
		  		newRel.setKind(Relation.CONDITIONAL_PROB);
		  		rlOfCPN.add(newRel);
		  		
		  	}  	
		  }
		  setRelationList(rlOfCPN);
		
		}
		  
		
	/**
	 * This method returns a relation with the probabilities of Cooper and stores a ValueNodeCPN that
	 * joins the information about the value node of the relation and the maximum and minimum of the
	 * utilities. These extremes are necessary to recover the utilities from Cooper's probabilities
	 * @param id
	 */
	private Relation obtainCooperRelationAndStoreValueNodeCPN(Node utilityInOriginal,IDWithSVNodes id) {
		// TODO Auto-generated method stub
		//Copy the relations of chance nodes
		Node utilityInNew;
		Relation aggregateRelation;
		Relation translatedRelation;
		Relation cooperRelation;
		PotentialTable translatedPotential;
		PotentialTable cooperPotential;
		PotentialTable aggregatePotential;
		Vector varsAggregateRelation;
		double maxValue;
		double minValue;
		
		//Obtain the utility refered to the new CPN
		utilityInNew = getNode(utilityInOriginal.getName());
		
		//Obtain the relation of the table of utilities of the original ID
		aggregatePotential = id.getTotalUtility(utilityInOriginal);
		varsAggregateRelation = (Vector)(aggregatePotential.getVariables().clone());
		varsAggregateRelation.add(0,utilityInOriginal);
		aggregateRelation = new Relation(varsAggregateRelation);
		aggregateRelation.setValues(aggregatePotential);
		
		//Translate the aggregate table of utilities of the original ID to the CPN
		translatedRelation = translateRelation(aggregateRelation);
		translatedPotential = (PotentialTable)(translatedRelation.getValues());
		
		//Transform the translated relation with Cooper's transformation
		cooperRelation = new Relation();
		cooperRelation.setVariables(translatedRelation.getVariables());
		maxValue = translatedPotential.maximumValue();
		minValue = translatedPotential.minimumValue();
		cooperTable.insertCooperNode(new CooperNode(utilityInNew,minValue,maxValue));
		cooperPotential = translatedPotential.convertUtilityIntoProbability((FiniteStates)utilityInNew,minValue,maxValue);
		cooperRelation.setValues(cooperPotential);
		
		return cooperRelation;

}
	
	/**
	 * Method to convert the probabilistic (or utility) PotentialTable to a deterministic relation
	 * using maximum. I assume the node of the new deterministic relation is the last
	 * (decision variable in decision tables).
	 * @param decisionTableInOriginal Potential Table refered to the original influence diagram
	 * @return a relation with a deterministic potential, computed through maximization, and refered
	 * to the variables of the new Cooper Policy Network
	 */
	protected Relation toDeterministicRelation(PotentialTable decisionTableInOriginal){
		Relation newRel;
		Node decisionInNew;
		Potential decisionTableInNew;
		Potential deterministicPotentialInNew;
		
			
	
		
		decisionTableInNew = translatePotential(this,decisionTableInOriginal);
		
		decisionInNew = (Node)(decisionTableInNew.getVariables().lastElement());
		
		//New relation setting its potential to a deterministic potential
		deterministicPotentialInNew = ((PotentialTable) decisionTableInNew).toDeterministic(decisionInNew);
		newRel = new Relation(deterministicPotentialInNew.getVariables());
		newRel.setValues(deterministicPotentialInNew);
		
		return newRel;
			
	}
	
	/**
	 * It translates a relation expressed with nodes of other bn in a potential 
	 * expressed with nodes of 'b'
	 * @param relInOriginal
	 * @return
	 */
	public static Relation translateRelation(Bnet b,Relation rel){
		Potential potInB;
		Relation relInB;
		
		potInB = translatePotential(b,rel.getValues());
		relInB = new Relation(potInB.getVariables());
		relInB.setValues(potInB);
		
		return relInB;
	}
	
	
	/**
	 * It translates a potential expressed with nodes of other bn in a potential 
	 * expressed with nodes of 'b'
	 * @param decisionTableInOriginal
	 * @return
	 */
	private static Potential translatePotential(Bnet b,Potential decisionTableInOriginal) {
		// TODO Auto-generated method stub
	
		Vector varsInOriginal;
		Node decisionInOriginal;
		NodeList varsInNew;
	
		Potential decisionTableInNew;
	
		
		//Variables and decision refered to the original ID
		varsInOriginal = decisionTableInOriginal.getVariables();
		decisionInOriginal = (Node)(varsInOriginal.lastElement());
		
			
		//Variables of the potential, refered to the new CPN
		varsInNew = new NodeList();
		//varsInNew.insertNode(decisionInNew);
		for(int i=0;i<varsInOriginal.size();i++)
		{
			varsInNew.insertNode(b.getNode(((Node)(varsInOriginal.elementAt(i))).getName()));
		}
		
		//Decision table refered to the new CPN
		decisionTableInNew = decisionTableInOriginal.copy();
		decisionTableInNew.setVariables(varsInNew.getNodes());
		 
		
		return decisionTableInNew;
	}
	
	
	/**
	 * It translates a potential expressed with nodes of other bn in a potential 
	 * expressed with nodes of 'b'
	 * @param decisionTableInOriginal
	 * @return
	 */
	protected static PotentialTable translatePotentialSharingValues(Bnet b,PotentialTable decisionTableInOriginal) {
		// TODO Auto-generated method stub
	
		Vector varsInOriginal;
		Node decisionInOriginal;
		NodeList varsInNew;
	
		PotentialTable decisionTableInNew;
	
		
		//Variables and decision refered to the original ID
		varsInOriginal = decisionTableInOriginal.getVariables();
		decisionInOriginal = (Node)(varsInOriginal.lastElement());
		
			
		//Variables of the potential, refered to the new CPN
		varsInNew = new NodeList();
		//varsInNew.insertNode(decisionInNew);
		for(int i=0;i<varsInOriginal.size();i++)
		{
			varsInNew.insertNode(b.getNode(((Node)(varsInOriginal.elementAt(i))).getName()));
		}
		
		//Decision table refered to the new CPN
		decisionTableInNew = new PotentialTable();
		decisionTableInNew.setValues(decisionTableInOriginal.getValues());
		decisionTableInNew.setVariables(varsInNew.getNodes());
		 
		
		return decisionTableInNew;
	}

	/**
	 * It constructs the structure corresponding to chance and decision nodes
	 * and also add utility nodes to the list of nodes of the network in spite
	 * of the fact that their structure is not added here.	 
	 * @param id
	 */
	public void setStructureOfCPNFrom(IDWithSVNodes id) {
		Graph g;
		NodeList decisions;
		Node auxDec;
		LinkList linksToDec;
		Link auxLink;
	
		
		// TODO Auto-generated method stub
		g = id.duplicate();
		
		setNodeList(g.getNodeList());
		setLinkList(g.getLinkList());
		  
		
							
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
		
		
		
	
		
		
	}
	
	
	/**
	 * It converts the decision 'dec' of 'id' in a chance node without parents
	 * @param id
	 * @param dec
	 */
	public static void convertDecisionIntoChanceWithoutParents(IDWithSVNodes id,Node dec){
		
		LinkList linksToDec;
		Link auxLink;
		
		
		linksToDec=dec.getParents().copy();
		//Remove the incoming links to the decision
		for (int j=0;j<linksToDec.size();j++){
			auxLink=linksToDec.elementAt(j);
			try{
				id.removeLink(auxLink);
			}catch(InvalidEditException iee){;}
				
		}
		//Convert the decision into a chance node
		dec.setKindOfNode(Node.CHANCE);
		dec.setTypeOfVariable(Node.FINITE_STATES);
		
	}
	
	
	/**
	 * It constructs the structure corresponding to chance and decision nodes
	 * Only chance and decision nodes are allowed to be in the PN
	 * @param id
	 */
	public void setStructureOfPNFrom(IDWithSVNodes id) {
		// TODO Auto-generated method stub
		
		Graph g;
		NodeList decisions;
		Node auxDec;
		LinkList linksToDec;
		Link auxLink;
		g = id.duplicate();
		NodeList utilNodes;
		NodeList svNodes;
		
		
		
		setNodeList(g.getNodeList());
		setLinkList(g.getLinkList());
		  
		
							
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
		
		
		//Remove utility nodes
		utilNodes = this.getNodesOfKind(Node.UTILITY);
		
		for (int i=0;i<utilNodes.size();i++){
			this.removeNode(utilNodes.elementAt(i));
		}
	
		
//		Remove sv nodes
		svNodes = this.getNodesOfKind(Node.SUPER_VALUE);
		
		for (int i=0;i<svNodes.size();i++){
			this.removeNode(svNodes.elementAt(i));
		}
		
		
	}

	/*public static PotentialTable generateDeterministicPotentialFromUtilitiesOfDecision(PotentialTable policy){
		PotentialTable deterministicTable;
		
		deterministicTable = new PotentialTable()
		
		
		return null;
		
	}*/
	
	/**
	 * Add chance nodes corresponding to the utility nodes and set their relations
	 * @param id
	 */
	private void setRelationsOfValueNodesFrom(IDWithSVNodes id) {
		NodeList valueNodes;
		NodeList parentsOfValueUInOriginal;
		Node valueUInOriginal;
		NodeList parentsOfValueUInNew;
		Node valueUInNew;
		Node chanceU;
		Relation cooperRelation;
		
		//List of value nodes
		valueNodes = id.getValueNodes();
		
		//
		for (int i=0;i<valueNodes.size();i++){
			//Obtain a value node
			valueUInOriginal = valueNodes.elementAt(i);
			valueUInNew = getNode(valueUInOriginal.getName());
			
			//Obtain the functional predecessors of the value node
			parentsOfValueUInOriginal = id.getChanceAndDecisionPredecessors(valueUInOriginal);
			parentsOfValueUInNew = translateNodeList(parentsOfValueUInOriginal);
			
			//Remove the old value node
			removeNode(valueUInNew);
			
			//Create the new value node (kind chance)
			chanceU = new FiniteStates(valueUInNew.getName(),valueUInNew.getPosX(),valueUInNew.getPosY(),getFSDefaultStates(Node.CHANCE));
			chanceU.setTitle(valueUInNew.getTitle());
			
			try{
				addNode(chanceU);
			}catch(InvalidEditException iee){;}
			
//			Create links from the functional predecessors to the value node (of kind chance)
			for (int j=0;j<parentsOfValueUInNew.size();j++){
				try{
				createLink(parentsOfValueUInNew.elementAt(j),chanceU);
			}catch(InvalidEditException iee){;}
			}
			
			cooperRelation = obtainCooperRelationAndStoreValueNodeCPN(valueUInOriginal,id);
			
			// Remove the relation that appears because of the creation of links
			// and substitute it by the new utility relation
			removeRelation(valueUInOriginal);
			
			addRelation(cooperRelation);

			
			
		}
	}
	
	/**
	 * Transform a list of nodes of other ID to a list of nodes of this CPN
	 * @param nodes List of nodes refered an ID
	 * @return List of nodes refered to this CPN
	 */
	private NodeList translateNodeList(NodeList nodes){
		NodeList newList;
		
		newList = new NodeList();
		
		for (int i=0;i<nodes.size();i++){
			newList.insertNode(getNode(nodes.elementAt(i).getName()));
		}
		return newList;
		
	}
	
	   
	
	/**
	 * Method to obtain the reverse transformation of Cooper, i.e. to obtain the true utilities
	 * from a table of Cooper's probabilities.
	 * @param nameOfValueNode Name of the value node of the CPN whose utilities are calculated in the method
	 * @return Table of utilities of that value node
	 */
	public PotentialTable obtainUtility(String nameOfValueNode){
		Node value;
		PotentialTable pot;
		CooperNode auxCooperNode;
		
		value = this.getNode(nameOfValueNode);
		pot = (PotentialTable)(getRelation(value).getValues());
		auxCooperNode = cooperTable.getCooperNode(value);
		return pot.convertProbabilityIntoUtility(auxCooperNode.getMinValue(),auxCooperNode.getMaxValue());

 
	
	}
	
	public double getMaximumUtility(Node n){
		return cooperTable.getCooperNode(n).getMaxValue();
	}
	
	public double getMinimumUtility(Node n){
		return cooperTable.getCooperNode(n).getMinValue();
	}
	
	public double getRangeOfUtility(Node n){
		CooperNode cn;
		
		cn = cooperTable.getCooperNode(n);
		return (cn.getMaxValue()-cn.getMinValue());
	}
	
	/**
	 * @param utility Value of the utility for a value node
	 * @param min Minimum value of the utility for the node in question
	 * @param max Maximum value of the utility for the node in question
	 * @return The probability of Yes for the value node (direct Cooper's transformation)
	 */
	public static double directCooperTransformation(double utility,double min,double max){
		double probabilityOfYes;
		double k1=max-min;
		double k2=-min;
		
		if (k1!=0.0){
			probabilityOfYes =  (utility+k2)/k1;
		}
		else{//All utilities are equal, so this value is irrelevant, but we have to solve the 
			//indetermination 0/0
			probabilityOfYes = 1.0;
		}
		
		
		return probabilityOfYes;
	}
	
	public static void main(String args[]) throws ParseException, IOException {
		
	    if (args.length < 1){
	        System.out.println("Use: ");
	        System.out.println("java CooperPolicyNetwork inputFile (without extension)");
	        System.exit(0);
	      }
	    else{
	    	
	   
		  Node from;
		  Node to;
		  NodeList nodes;
		  IDWithSVNodes diag;
		  int i,j;
		  CooperPolicyNetwork cpn;
		  String nameOfFile;
		  String extension;
		  NodeList valueNodes;
		  
		  
		  
		  //nameOfFile = "C:\\bayelvira2\\ejemplo4_3";
		  //nameOfFile = "C:\\bayelvira2\\prueba_Cooper_1";
		  //nameOfFile = "C:\\bayelvira2\\Redes para probar ArcReversalSV\\chances3";
		  //nameOfFile = "C:\\bayelvira2\\Redes de Carlos Disdier\\Nuevo Formato\\mediastino-basico-3";
		  nameOfFile = args[0];
		  extension = ".elv";
		  

		  // Build the ID
	   diag=(IDWithSVNodes)Network.read(nameOfFile+extension);
		  
		  // Build the Cooper policy network
		cpn = CooperPolicyNetwork.constructCPNFrom(diag,0,null);
		
		cpn.save(nameOfFile+"Cooper"+extension);

		  // Retrieve the nodes

		  nodes=cpn.getNodeList();

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
		  
		  valueNodes = cpn.getCooperTable().getValueNodes();
		  
		  System.out.println("Tras la aplicación directa de Cooper y después la inversa tenemos las siguientes tablas de utilidades:");
		  for (i=0;i<valueNodes.size();i++){
		  	cpn.obtainUtility(valueNodes.elementAt(i).getName()).print();
		  }
		  
	    }
	
}

}


