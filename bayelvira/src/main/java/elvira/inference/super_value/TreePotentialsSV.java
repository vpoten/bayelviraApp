/*
 * Created on 10-mar-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package elvira.inference.super_value;

import elvira.*;

import java.util.Vector;
import elvira.potential.UtilityPotential;
import elvira.potential.Function;
import elvira.potential.SumFunction;
import elvira.potential.PotentialTable;
import elvira.potential.Potential;
import elvira.tools.PropagationStatistics;

import java.util.Stack;
import java.util.ArrayList;

/**
 * @author Manolo
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TreePotentialsSV {
	
	/**
	 * Root of the tree
	 */
	private TreeNodeSV root;
	

	
	private Bnet bnet;
	
		
	public TreePotentialsSV(TreeNodeSV r){
		root=r;
	}
	
	
	
	
	public TreePotentialsSV(int kind){
		
		root=new TreeNodeSV(TreeNodeSV.PRODUCT);
	}
	/**
	 * It constructs a tree with all the relations
	 */
	public TreePotentialsSV(Bnet bn){
		int i;
		Vector relations;
		Relation rel;
		Node terminalSVNode;
		int kind;
		TreeNodeSV aux;
		
		//Set attribute network
		bnet=bn;
		
		//The root of the tree is always a product of potentials
		root=new TreeNodeSV(TreeNodeSV.PRODUCT);
				
		relations=bnet.getRelationList();
		
		//To place the relations of probability on the tree
		for (i=0;i<relations.size();i++){
			rel=(Relation)(relations.elementAt(i));
			kind=rel.getKind();
			if ((kind!=Relation.UTILITY_COMBINATION)&&(kind!=Relation.UTILITY)){
				aux=new TreeNodeSV(rel);
				root.createLinkTo(aux);
				//Set the normalizing variable
				if (rel.getValues().getClass()==PotentialTable.class){
					aux.setNormalizingVariable(rel.getVariables().elementAt(0));
				}
			}
		}
		
		
		
		//To place the relations of the utility and super-value nodes
		
		
		
		
		
		if (((IDWithSVNodes)bnet).hasSVNodes()){ //There are super value nodes
			if (((IDWithSVNodes)bnet).hasOnlyOneTerminalSVNode()){
					terminalSVNode=((IDWithSVNodes)bnet).getTerminalValueNode();
					//root.createLinkTo(obtainTreeSV(terminalSVNode));
					root.createLinkTo(obtainGraphSV(terminalSVNode));
			}
			else{
				System.out.println("Influence diagrams with super-value nodes must have only a super value node");
			}
		}
		else{ //There aren´t super value nodes --> There must be only one utility node
			if (((IDWithSVNodes)bnet).hasOnlyOneValueNode()){
				Node util;
				//Obtain the utility node
				util=bnet.getNodesOfKind(Node.UTILITY).elementAt(0);
				//Create a link to the leaf of the potential of the utility node
				//root.createLinkTo(obtainTreeSV(util));
				root.createLinkTo(obtainGraphSV(util));
			}
			else{
				System.out.println("Influence diagrams without super-value nodes must have only a utility node");
			}
		}
		
		//It makes the attribute 'variables' of 'root' is consistent
		//root.updateVariables();

	}
	
	
	public void createLink(TreeNodeSV from,TreeNodeSV to){
			from.getChild().add(to);
			to.getParents().add(from);
		}
		
	public TreeNodeSV getRoot(){
			return root;
	}
	

	
	/**
	* It calculates the tree of the super-value and utility nodes
    */
	public TreeNodeSV obtainTreeSV(Node termValueNode){
			Relation rel;
			UtilityPotential pot;
			Vector variables;
			int kindOfOperator;
			TreeNodeSV nodeTree=null;
			
		
			rel=bnet.getRelation(termValueNode);
		
			if (termValueNode.getKindOfNode()==Node.UTILITY){
				nodeTree=new TreeNodeSV(rel);			
			}
			else{//kind==Node.SUPER_VALUE
			
				//Potential of the super-value node
				pot=(UtilityPotential)(rel.getValues());
			
				//Set the kind of the operator of the super value node
				if (pot.getFunction().getClass()==SumFunction.class){
					kindOfOperator=TreeNodeSV.SUM;
				}
				else{
					kindOfOperator=TreeNodeSV.PRODUCT;
				}
				nodeTree=new TreeNodeSV(kindOfOperator);
			
				//Constructs the tree of the arguments of the utility potential
				//and sets them as children
				variables=pot.getVariables();
				for (int i=0;i<variables.size();i++){
					nodeTree.createLinkTo(obtainTreeSV((Node)variables.elementAt(i)));
				}
			}
			
			return nodeTree;
	}


	/**
	* It calculates the graph of the super-value and utility nodes
	*/
	public TreeNodeSV obtainGraphSV(Node termValueNode){
			NodeList valueNodes;
			Vector graphs;
			int i;
			
			//Union of the UTILITY and SUPER_VALUE nodes
			valueNodes=bnet.getNodesOfKind(Node.SUPER_VALUE);
			valueNodes.join(bnet.getNodesOfKind(Node.UTILITY));
			
			//List with the trees of the value nodes
			//graphs=new Vector(valueNodes.size());
			graphs=new Vector();
			//Initialize to null the list of the trees of the value nodes
			for (i=0;i<valueNodes.size();i++){
				graphs.addElement(null);
			}
			
			return obtainGraphSVAux(termValueNode,valueNodes,graphs);
			
	}


	public TreeNodeSV obtainGraphSVAux(Node node,NodeList valueNodes,Vector graphs){
		int indexOfNode;
		TreeNodeSV graphOfNode;
		Relation rel;
		UtilityPotential pot;
		int kindOfOperator;
		Vector variables;
		
		//Calculate the index of the graph of the 'node' in the arraylist
		//'graphs' and get the graph.
		indexOfNode=valueNodes.getId(node);
		graphOfNode=(TreeNodeSV)(graphs.get(indexOfNode));
		if (graphOfNode!=null){
			//The graph of the node has been calculated previously
			return graphOfNode;
		}
		else{
			//The graph of the node hasn't been calculated previously,
			//so it must be calculated now
			
			rel=bnet.getRelation(node);
		
			if (node.getKindOfNode()==Node.UTILITY){
				graphOfNode=new TreeNodeSV(rel);
						
			}
			else{//kind==Node.SUPER_VALUE
			
				//Potential of the super-value node
				pot=(UtilityPotential)(rel.getValues());
			
				//Set the kind of the operator of the super value node
				if (pot.getFunction().getClass()==SumFunction.class){
					kindOfOperator=TreeNodeSV.SUM;
				}
				else{
					kindOfOperator=TreeNodeSV.PRODUCT;
				}
				graphOfNode=new TreeNodeSV(kindOfOperator);
			
				//Constructs the tree of the arguments of the utility potential
				//and sets them as children
				variables=pot.getVariables();
				for (int i=0;i<variables.size();i++){
					graphOfNode.createLinkTo(obtainGraphSVAux((Node)variables.elementAt(i),valueNodes,graphs));
				}
			}
			graphs.set(indexOfNode,graphOfNode);
			return graphOfNode;
		}
	}

	public void compactLeavesInProducts(Node x){
		getRoot().compactProducts(x);
	}
	
	public void compactTree(){
			getRoot().compactTree();
	}
	
/**  
* Eliminate the variable 'x' in the tree of potentials 'tree'
 * @param statistics
 * @param eliminateDecByUnforking 
*/
public PotentialTable eliminateWithoutDivisions(Node x, PropagationStatistics statistics,boolean groupNoDependents){
	PotentialTable policyTable=null;
	
	switch(x.getKindOfNode()){
		case Node.CHANCE:
			eliminateWithoutDivisionsChance(x,statistics,groupNoDependents);
			break;
		case Node.DECISION:
			policyTable = eliminateWithoutDivisionsDecision(x,statistics);
			break;
		
	}
	//compactTree();
//	Statistics about the size of the problem
	double size = this.getSize();
	statistics.addSize(size);
	System.out.println("The size of the potentials after marginalizing "+x.getName()+" is: "+size);

	
	return policyTable;
}
		


//private PotentialTable eliminateWithoutDivisionsDecision(Node x, PropagationStatistics statistics,boolean eliminateDecByUnforking) {
//	PotentialTable policy;

	/*if (eliminateDecByUnforking){
		policy = eliminateWithoutDivisionsDecisionByUnforking(x,statistics);
	}
	else{*/
	//	policy = eliminateWithoutDivisionsDecisionSimple(x,statistics);
	//}
	//return policy;
//}
/*private PotentialTable eliminateWithoutDivisionsDecisionByUnforking(Node x, PropagationStatistics statistics) {
	// TODO Auto-generated method stub
	// TODO Auto-generated method stub
	PotentialTable policyTable;
//	Statistics about the size of the problem
	prepareToMarginalizeDecisionByUnforking(x);
	policyTable=marginalizeDecision(x);
	return policyTable;
	
	return null;
	return null;
}
*/



/*private void prepareToMarginalizeDecisionByUnforking(Node x) {
	// TODO Auto-generated method stub
	getRoot().prepareToMarginalizeDecisionByUnforking(x);
}*/




private PotentialTable eliminateWithoutDivisionsDecision(Node x, PropagationStatistics statistics) {
	// TODO Auto-generated method stub
	
	// TODO Auto-generated method stub
	PotentialTable policyTable;
//	Statistics about the size of the problem
	prepareToMarginalizeDecision(x);
	policyTable=marginalizeDecision(x);
	return policyTable;
}




private void eliminateWithoutDivisionsChance(Node x, PropagationStatistics statistics, boolean groupNoDependents) {
	// TODO Auto-generated method stub
	compactLeavesInProducts(x);
	distributeProducts(x,groupNoDependents);
	marginalizeChance(x);
	
}




/**
 * @param x
 * @return
 */
private PotentialTable marginalizeDecision(Node x) {
	return getRoot().marginalizeDecision(x);
}






/**
 * Prepare the tree to max-marginalize it in the decision x.
 * After this method, the tree must have almost one leaf which depends on x.
 * This preparation is performed not unforking the tree for decisions.
 * @param x
 */
private void prepareToMarginalizeDecision(Node x) {
	// TODO Auto-generated method stub
	getRoot().prepareToMarginalizeDecision(x);
	
}


/**  
* Eliminate the variable 'x' in the tree of potentials 'tree'
 * @param statistics
*/
private void eliminateWithDivisionsChance(TreePotentialsSV probTree, Node x, PropagationStatistics statistics, boolean groupNoDependents) {
	// TODO Auto-generated method stub
	probTree.compactTree();
	probTree.compactLeavesInProducts(x);
	introduceDivision(probTree,x);
	compactLeavesInProducts(x);
	distributeProducts(x,groupNoDependents);
	marginalizeChance(x);
	compactTree();
//	Statistics about the size of the problem
	double size = probTree.getSize()+this.getSize();
	statistics.addSize(size);
	System.out.println("The size of the potentials before marginalize "+x.getName()+" is: "+size);
}


public PotentialTable eliminateWithDivisions(TreePotentialsSV probTree, Node x, PropagationStatistics statistics,boolean groupNoDependents) {
	PotentialTable policy = null;
	
	switch(x.getKindOfNode()){
	case Node.CHANCE:
		eliminateWithDivisionsChance(probTree,x,statistics,groupNoDependents);
	
		break;
	case Node.DECISION:
		policy = eliminateWithDivisionsDecision(probTree,x,statistics);
		break;
	}
	return policy;
	

}


/*private PotentialTable eliminateWithDivisionsDecision(TreePotentialsSV probTree, Node x, PropagationStatistics statistics,boolean eliminateDecByUnforking) {
	PotentialTable policy;

	if (eliminateDecByUnforking){
		policy = eliminateWithDivisionsDecisionByUnforking(probTree,x,statistics);
	}
	else{
		policy = eliminateWithDivisionsDecisionSimple(probTree,x,statistics);
	}
	return policy;
}
*/


/*private PotentialTable eliminateWithDivisionsDecisionByUnforking(TreePotentialsSV probTree, Node x, PropagationStatistics statistics) {
	// TODO Auto-generated method stub
	return null;
}*/




private PotentialTable eliminateWithDivisionsDecision(TreePotentialsSV probTree, Node x, PropagationStatistics statistics) {
	PotentialTable policyTable=null;
	double size;
	
	// TODO Auto-generated method stub
	probTree.compactTree();
	probTree.compactLeavesInProducts(x);
//	introduceDivision(probTree,x);
	projectProbabilityPotentialOfDecision(probTree,x);
	prepareToMarginalizeDecision(x);
	policyTable=marginalizeDecision(x);
	compactTree();
//	Statistics about the size of the problem
	size = probTree.getSize()+this.getSize();
	statistics.addSize(size);
	System.out.println("The size of the potentials before marginalize "+x.getName()+" is: "+size);
	
	return policyTable;
}




/**
 * @param probTree
 * @param x
 * This methods is used when eliminating a decision variable in variable elimination with divisions. When the decision node appears in some probability potential,
 * we have to multiply them (that product is computed before this method) and project them by removing the decision, because that variable doesn't affect the potential.
 */
private void projectProbabilityPotentialOfDecision(TreePotentialsSV probTree,Node x) {
	Vector branchesX;
	Potential probX;
	TreeNodeSV treeNodeProbX=null;
	Potential marginalizedProbX=null;
	Potential normalizedProbX;
	//TreeNodeSV nodeWithDivisionX;
	TreeNodeSV auxUtilTree;
	boolean normalized = true;
	Vector vars;
	
	//Calculate the leaves in the probTree that depends on X
	branchesX=probTree.getRoot().getBranchesLeavesWithVariable(x);
	
	//We suppose the leaves with X were compacted previously
	if (branchesX.size()==1){
		
		System.out.println("Decision "+x.getName()+" has appeared in a probability potential, which has been projected");
		
		//TreeNode that group the probability potentials dependent of X
		treeNodeProbX=(TreeNodeSV)(branchesX.elementAt(0));
		
		//Probability potential dependent of X
		probX=treeNodeProbX.getRelation().getValues();
		
		//Marginalize the potential dependent of X over X
		vars=new Vector(probX.getVariables());
		vars.removeElement(x);
		marginalizedProbX=probX.maxMarginalizePotential(vars);
		
		//Set the marginalized potential
		treeNodeProbX.setPotential(marginalizedProbX);
		
	
			
}
}




/**  
* It introduces a potential, after division, in the treepotential of utilities
*/
public void introduceDivision(TreePotentialsSV probTree,Node x){
	Vector branchesX;
	Potential probX;
	TreeNodeSV treeNodeProbX=null;
	Potential marginalizedProbX=null;
	Potential normalizedProbX;
	TreeNodeSV nodeWithDivisionX;
	TreeNodeSV auxUtilTree;
	boolean normalized = true;
	
	//Calculate the leaves in the probTree that depends on X
	branchesX=probTree.getRoot().getBranchesLeavesWithVariable(x);
	
	//We suppose the leaves with X were compacted previously
	if (branchesX.size()==1){
		//TreeNode that group the probability potentials dependent of X
		treeNodeProbX=(TreeNodeSV)(branchesX.elementAt(0));
		
		//Probability potential dependent of X
		probX=treeNodeProbX.getRelation().getValues();
		
		//Marginalize the potential dependent of X over X
		switch(x.getKindOfNode()){
			case Node.CHANCE:
				normalized = (x==treeNodeProbX.getNormalizingVariable());
				if (normalized == false){
					marginalizedProbX=probX.addVariable(x);
				}
				else{
					System.out.println("Sum-marginalization in probability potentials for variable "+x.getName()+" because it would be a unity potential");
				}
				break;
			case Node.DECISION:
				Vector vars=new Vector(probX.getVariables());
				vars.removeElement(x);
				marginalizedProbX=probX.maxMarginalizePotential(vars);
				break;
		}
		
		
		if (normalized == false){
			
			//Set the marginalized potential
			treeNodeProbX.setPotential(marginalizedProbX);
			
			//Normalize probX
			normalizedProbX=probX.divide(marginalizedProbX);
		}
		else{//It isn't necessary to divide because the marginalization is 1
			//Removal of unity potential
			probTree.getRoot().removeLinkTo(treeNodeProbX);
			
			//We don't divide
			normalizedProbX = probX;
		}
		
		//Create the tree node with the new potential
		nodeWithDivisionX=new TreeNodeSV(normalizedProbX);
		nodeWithDivisionX.setNormalizingVariable(x);
		
		
		//Introduce the factor with the marginalized potential into
		//the tree of utilities. If the tree of utilities is 
		//a PRODUCT it only adds a branch to it
		
		if ((root.getKindOfNodeTreeSV()==TreeNodeSV.OPERAND)||
			(root.getOperator()==TreeNodeSV.SUM)){
			auxUtilTree=getRoot();
			root=new TreeNodeSV(TreeNodeSV.PRODUCT);
			root.createLinkTo(nodeWithDivisionX);
			root.createLinkTo(auxUtilTree);
		}
		else{ //(root.getOperator()==TreeNodeSV.PRODUCT)
			root.createLinkTo(nodeWithDivisionX);
		}
			
}
		
		
		
	
}



public PotentialTable getUtilitiesEliminationWithD(Node x){
	return (PotentialTable)(getRoot().evaluate().sendVarToEnd(x));
}





public void marginalizeChance(Node x){
	
	getRoot().marginalizeChance(x);
}



public void distributeProducts(Node x, boolean groupNoDependents){
	
	TreeNodeSV product;
	Stack stackDistribute=new Stack();
	TreeNodeSV factor;
	TreeNodeSV sum;
	
	//Compute a list with the products that must be distributed
	getRoot().computeStackOfProductsToDistribute(x,stackDistribute);
	while(stackDistribute.isEmpty()==false){
		product=(TreeNodeSV)(stackDistribute.pop());
		factor=product.getFactorOfDistribution(x);
		sum=product.getSumOfDistribution(x,factor);
		
		
		if (groupNoDependents){
			//Group the summands that don't depend on X on an only tree to keep grouped the tree.
			sum.groupAddendsDontDependOn(x);
		}
		
		//Proving distribute with optimization of unity axiom
		//product.distribute(factor,sum);
		//TODO Se utiliza distribute porque distributeForMarginalizeChance no está bien
		//por el momento.
		//product.distributeForMarginalizeChance(factor,sum,x);
		product.distribute(factor,sum);
		
		//There can be consecutive products that must be compacted
		product.compactTree();
		
		//There can be leaves that must be compacted, but only in the
		//scope of the tree 'product'
		product.compactProducts(x);
		
		//We must search more products to distribute in the subtree
		//of the distributed node 'product', that would be the first ones
		//in order to be distributed (push on the top of the stack)
		product.computeStackOfProductsToDistribute(x,stackDistribute);
		
	}
}
	
	/**  
	* Method to obtain the subtree of utilities.
	* The method makes 'this' contains a tree with the probability potentials.
	* It returns a tree with the utility potentials.
	*/
	public TreePotentialsSV quitUtilitiesTree(){
		TreeNodeSV util;
		Vector children;
		int length;
		
		children=getRoot().getChild();
		length=children.size();
		
		if (length>0){
			util=(TreeNodeSV)(children.elementAt(length-1));
			getRoot().removeLinkTo(util);
			return new TreePotentialsSV(util);
		}
		else{
			return null;
		}
	}




	/**
 * Method to compute the total size needed to store the values
 * used to quantify the whole set of relations stored in the leaves
 * of the tree of potentials
 * @return <code>double</code> the sum of sizes of the potentials
 */

	public double getSize() {
		RelationList leafRelations=new RelationList();
		
		getRoot().getListOfRelations(leafRelations);
		return leafRelations.sumSizes();
	}




	/**
	 * @return
	 */
	public boolean applySubsetRule() {
		// TODO Auto-generated method stub
		return getRoot().applySubsetRule();
	}


/**  
 * Precondition: There's only a leave direct children of this TreeNodeSV
 * that depends of 'x'
 * It returns 'true' in 'mustDistribute' iff the root of the tree
 * is a PRODUCT and has two branches that are dependents of 'x'.
 * In this case it returns in 'factor' the leave that distributes to
 * the SUM tree that is returned in 'sumTree'
*/
//public boolean mustBeDistributed(
/*Input*/ //Node x,
/*Output*/// boolean mustDistribute, TreeNodeSV factor, TreeNodeSV sumTree){
	
/*	Vector dependents;
	
	dependents=getRoot().getLeavesWithVariable(x);*/
	



	
	
}//end of class


