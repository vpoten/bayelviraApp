package elvira.tools.idiagram;

import java.util.Random;

import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.InvalidEditException;
import elvira.Link;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.potential.Function;
import elvira.potential.PotentialTable;
import elvira.potential.ProductFunction;
import elvira.potential.SumFunction;
import elvira.potential.UtilityPotential;

/**
 * @author Manolo_Luque
 * 
 */
public class GeneratorIDs {
	/**
	 * It generates IDs according to a method proposed by Marta Vomlelova. It builds a simple tree, it adds
	 * and remove links, order the decision nodes, add the utility nodes and determine their parents, it generates
	 * the structure of super-value nodes and, finally, it generates the probabilities and the utiliities.
	 * @param nNodes Number of nodes (chance and decisions)
	 * @param decRation Probability for a node being a decision
	 * @param nUtils Number of utility nodes (non-super)
	 * @param nParentsOfSV Number of parents for each super value node (every SV nodes, except probably the terminal, has exactly this number of parents)
	 * @param nParents Maximum number of parents for chance and decision nodes. It is also the exact number of parents for the utility nodes
	 * @param iterations Number of iterations for the loop that removes and andd links to the ordered simple decision tree
	 * @return Influence diagram with super-value nodes randomly generated
	 */
	public static IDWithSVNodes generateIDiagramVomlelova(int nNodes, double decRation,
			int nUtils, int nParentsOfSV, int nParents, int iterations) {

		boolean withDecisions=false;
		IDWithSVNodes id = new IDWithSVNodes();

		while (withDecisions==false){
			id = initializeSimpleOrderedTreeWithDecisionsVomlelova(nNodes, decRation);
			withDecisions = (id.getNodesOfKind(Node.DECISION).size()>0);
		}
		
			
		addAndRemoveLinksVomlelova(id, nParents, iterations);
		
		generateProbabilities(id);
		
		id.orderDecisionsGreedilyIfNotOrdered();
		
		generateUtilityNodes(id, nUtils, nParents);
		
		generateUtilities(id);

		generateSVNodes(id, nParentsOfSV);
		
		return id;

	}
	
	/**
	 * @param nNodes
	 * @return
	 * @throws InvalidEditException
	 */
	private static IDWithSVNodes initializeSimpleOrderedTreeWithDecisionsVomlelova(int nNodes,double decRation) {
		IDWithSVNodes id;
		Random r=new Random();
		int kind;
		double randomNumber;
		String prefix;
		Node auxNode;
		NodeList generatedNodes=new NodeList();
		String newName;
				
		id=new IDWithSVNodes();
		
		
		for (int i=0;i<nNodes;i++){
			//Generated a new node
			randomNumber=r.nextDouble();
			if (randomNumber<decRation){
				kind=Node.DECISION;
				prefix="D";
			}
			else{
				kind=Node.CHANCE;
				prefix="X";
			}
			newName=prefix+i;
			id.createNode(0,0,"Helvetica",newName,kind);
			auxNode=id.getNode(newName);
			//Add the node to the diagram
			try{
				id.addNode(auxNode);
			} catch (InvalidEditException iee) {
			};
			
			if (i>0){//The root node of the tree can't have any parents.
				//Add a link between from one of the previously generated nodes to the new node
				try{
					id.createLink(chooseRandom(generatedNodes,r),auxNode);
				} catch (InvalidEditException iee) {
				};
			}
			
			generatedNodes.insertNode(auxNode);
			
			
		}
		
		
		return id;
		
	}
	
	private static void addAndRemoveLinksVomlelova(IDWithSVNodes id,int nParents,int iterations){
		NodeList nodes;
		int i,j;
		Link linkIJ;
		Node iNode;
		Node jNode;
		Random r=new Random();
		int nNodes;
		

	
		
		nodes=id.getNodeList();
		nNodes=nodes.size();
		
		
		for (int k = 0;k<iterations;k++){
			i=r.nextInt(nNodes);
			j=r.nextInt(nNodes);
			if (i!=j){
				iNode=nodes.elementAt(i);
				jNode=nodes.elementAt(j);
				linkIJ=id.getLink(iNode,jNode);
				if (linkIJ != null) {//Link between i and j exists
					try {
						id.removeLink(iNode, jNode);
					} catch (InvalidEditException iee) {
						;
					}
					if (id.connectedComponents().size() > 1){//It isn't connected
															 //without the link
						//Add the link again
						try { 
							id.createLink(iNode, jNode);
						} catch (InvalidEditException iee) {
							;
						}
					}
					else{
						System.out.println("Removal arc "+iNode.getName()+"->"+jNode.getName());
					}
				}
				else{
					if (jNode.getParentNodes().size() < nParents) {//It verifies the
																   // limit of
																   // parents
						if (id.hasCycle(iNode,jNode)==false){
						try {
							id.createLink(iNode, jNode);
						} catch (InvalidEditException iee) {
							;
							
							
						}
						System.out.println("Adding arc "+iNode.getName()+"->"+jNode.getName());
						
						}
					}
				}
			}
		}
	}
	
	/**
	 * Generate random probabilities (uniform distribution) for all the potentials of probability.
	 * @param id Influence diagram
	 */
	public static void generateProbabilities(IDWithSVNodes id) {
		// TODO Auto-generated method stub
		NodeList listNodes;
		Node node;
		NodeList nodesRel;
		NodeList pa;
		Relation relation;
		PotentialTable potentialTable;
		Random generator=new Random();
		int kind;
		
		listNodes=id.getNodeList();
		
		 for (int i=0 ; i< listNodes.size() ; i++) {
		    nodesRel = new NodeList();
		    node=listNodes.elementAt(i);
		    kind=node.getKindOfNode();
		    if (kind==Node.CHANCE){//We only consider the relations whose main node is CHANCE
			    node = (FiniteStates)listNodes.elementAt(i);
			    //Remove the relation of the node
			    id.removeRelation(node);
			    //Construct the new relation
			    nodesRel.insertNode(node);
			    pa = id.parents(node);
			    nodesRel.join(pa);
			    relation = new Relation();
			    relation.setVariables(nodesRel);
			    relation.setKind(Relation.CONDITIONAL_PROB);
			    //Generate a potental of probability with uniform random numbers
			    potentialTable = new PotentialTable(generator,nodesRel,1);
			    relation.setValues(potentialTable);
			    id.getRelationList().addElement(relation);
		    }
		  }
				 
	}
	
	public static void generateUtilityNodes(IDWithSVNodes id,int nUtils, int nParents){
		
			NodeList chanceAndDecNodes;
			NodeList auxParentsU;
			Random r=new Random();
			
			chanceAndDecNodes=id.getNodesOfKind(Node.CHANCE);
			chanceAndDecNodes.join(id.getNodesOfKind(Node.DECISION));
			
				
			//Generate utility nodes and their parents
			for (int i=0;i<nUtils;i++){
				String nameU;
				
				nameU="U"+i;
				id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
				auxParentsU=chooseRandom(chanceAndDecNodes,nParents,r);
				if (i==0){//For the first utility node we link the last decision to it, so the ID is evaluable
					NodeList orderedDec;
					Node lastDecision;
					
					orderedDec=id.getDecisionList();
					lastDecision=orderedDec.lastElement();
					
					if (auxParentsU.getId(lastDecision)==-1){
						auxParentsU.removeNode(0);//Remove some parent of the utility node
						auxParentsU.insertNode(lastDecision);//Add the last decision as parent of the utility node
					}
				}
				
				Node auxU;
				auxU=id.getNode(nameU);

				//Links from auxParentsU to auxU
				for (int j=0;j<auxParentsU.size();j++){
					try{
					id.createLink(auxParentsU.elementAt(j),auxU);
					} catch (InvalidEditException iee) {
					};
				}
				
			}
		
		}
	
	/**
	 * @param id
	 */
	public static void generateUtilities(IDWithSVNodes id) {
		
		NodeList listNodes;
		Node node;
		NodeList nodesRel;
		NodeList pa;
		Relation relation;
		PotentialTable potentialTable;
		Random generator=new Random();
		int kind;
		NodeList nodesPotential;
		
		listNodes=id.getNodeList();
		
		 for (int i=0 ; i< listNodes.size() ; i++) {
		    nodesRel = new NodeList();
		    node=listNodes.elementAt(i);
		    kind=node.getKindOfNode();
		    if (kind==Node.UTILITY){//We only consider the relations whose main node is UTILITY
			    
			    //Remove the relation of the node
			    id.removeRelation(node);
			    //Construct the new relation
			    nodesRel.insertNode(node);
			    pa = id.parents(node);
			    nodesRel.join(pa); //nodesRel= X and pa(X)
			    nodesPotential = nodesRel.copy();
			    nodesPotential.removeNode(node); //nodesPotential= pa(X)
			    relation = new Relation();
			    relation.setVariables(nodesRel);
			    relation.setKind(Relation.UTILITY);
			    //Generate a potental of probability with uniform random numbers
			    potentialTable = new PotentialTable(generator,nodesPotential,100.0);
			    relation.setValues(potentialTable);
			    id.getRelationList().addElement(relation);
		    }
		  }
		
	}


	/**
	 * @param id
	 * @param nParents
	 */
	private static void generateSVNodes(IDWithSVNodes id, int nParentsOfSV) {

		NodeList candidatesToBeParentsOfSV;
		boolean finished=false;
		NodeList parents;
		int numSV=0;
		
		Random r=new Random();
	
		//Generate their parents
		candidatesToBeParentsOfSV=id.getNodesOfKind(Node.UTILITY);

		while (finished==false){
			if (candidatesToBeParentsOfSV.size()==1){//There's a terminal value node
				finished=true;
			}
			else{//We must introduce some SV node
				String nameSV;
				//
				nameSV="SV"+numSV;
				numSV++;
				id.createNode(0,0,"Helvetica",nameSV,Node.SUPER_VALUE);
				parents=chooseRandom(candidatesToBeParentsOfSV,nParentsOfSV,r);
				Node auxSV;
				auxSV=id.getNode(nameSV);

				//Links from parents to auxSV
				for (int j=0;j<parents.size();j++){
					try{
					id.createLink(parents.elementAt(j),auxSV);
					} catch (InvalidEditException iee) {
					};
				}
				
				//Choose randomly the kind of the SV node: SUM or PRODUCT
				Function functionNewNode;
				if (r.nextBoolean()){
					functionNewNode=new SumFunction();
				}
				else{
					functionNewNode=new ProductFunction();
				}
				((UtilityPotential)(id.getRelation(auxSV).getValues())).setFunction(functionNewNode);
				
				//Update the list of candidates to be selected randomly as parents of the SV node
				candidatesToBeParentsOfSV=candidatesToBeParentsOfSV.difference(parents);
				candidatesToBeParentsOfSV.insertNode(id.getNode(nameSV));
			}
		}	
	}

	
	public static NodeList chooseRandom(NodeList list, int nNodes,Random r) {
		NodeList auxList;
		
		
		int auxRandom = 0;
		boolean inserted = false;
		Node auxNode;
		int length;
		
		length=list.size();

		if (length <= nNodes) {
			auxList = list.copy();
		} else {
			auxList = new NodeList();

			for (int i = 0; i < nNodes; i++) {
				inserted = false;

				while (inserted == false){
					auxRandom = r.nextInt(length);
				auxNode = list.elementAt(auxRandom);
				if (auxList.getId(auxNode) == -1) {
					auxList.insertNode(auxNode);
					inserted = true;
				}
				}
			}
		}

		return auxList;

	}
	
	public static Node chooseRandom(NodeList list, Random r) {
		
		return chooseRandom(list,1,r).elementAt(0);
	}

	


}
