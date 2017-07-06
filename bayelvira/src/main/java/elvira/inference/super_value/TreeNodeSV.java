/*
 * Created on 15-mar-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package elvira.inference.super_value;

import elvira.*;

import java.util.ArrayList;
import java.util.Vector;
import elvira.potential.Function;
import elvira.potential.SumFunction;
import elvira.potential.ProductFunction;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.tools.PropagationStatistics;

import java.util.Stack;

/**
 * @author Manolo
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TreeNodeSV {

	/**
	 * Constants to show if the node is an operator or an
	 * operand
	 */
	public final static int OPERATOR=1;
	public final static int OPERAND=2;


	/**
	 * Constants to express the operations
	 */	
	public final static int SUM=1;
	public final static int PRODUCT=2;
	

	/**
	 * To show the kind of node: operator or operand
	 */
	private int kind;

	/**
	 * To show the operator
	 */

	private int operator;


	/**
	* If kind is OPERAND, it's saved the relation
	*/
	
	private Relation relation;
	
	/**
	 * The operators will have branches for the operands
	 */
	private Vector child;
	
	/**
	 * Parents of the node in the graph
	 */
	private Vector parents;
	
	
	/**
	 * Only for operands.
	 * Variable that normalizes the potential
	 * It's used in the propagation with division of potentials.
	 * In this case the sum-marginalization of this potential over
	 * that variable is equal than 1 (a potential without variables)
	 */
	private Node normalizingVariable;
	
	/**
	 * Only for operands.
	 * Variable to know whether the normalizes the potential
	 * It's used in the propagation with division of potentials.
	 * In this case the sum-marginalization of this potential over
	 * that variable is equal than 1 (a potential without variables)
	 */
	private boolean unityPotential;
	
	/**
	 * Variables that appears in potentials of the tree
	 */
//	private NodeList variables;
	
	
	public TreeNodeSV(Relation rel){
		kind=OPERAND;
		relation=rel;
		child=new Vector();
		parents=new Vector();
		normalizingVariable=null;
		unityPotential=false;
	}
	
	public TreeNodeSV(Potential pot){
		kind=OPERAND;
		relation=makeRelationFromPotential(pot,Relation.POTENTIAL);
		child=new Vector();
		parents=new Vector();
		normalizingVariable=null;
		unityPotential=false;
	}

	public TreeNodeSV(int oper){
		kind=OPERATOR;
		operator=oper;
		child=new Vector();
		parents=new Vector();
		normalizingVariable=null;
		unityPotential=false;
	}
	
	public boolean isSum(){
		return ((kind==TreeNodeSV.OPERATOR)&&(operator==TreeNodeSV.SUM));
	}
	
	public boolean isProduct(){
		return ((kind==TreeNodeSV.OPERATOR)&&(operator==TreeNodeSV.PRODUCT));
	}
	
	//Function to break the links among this node and the rest of the tree
	public void delete(){
		removeLinksTo(child);
		removeLinksFrom(parents);
	}
	
	//Copy the information of 'nodeToCopy'
	private void copy(TreeNodeSV nodeToCopy){
		kind=nodeToCopy.getKindOfNodeTreeSV();
		operator=nodeToCopy.getOperator();
		relation=nodeToCopy.getRelation();
		normalizingVariable=nodeToCopy.getNormalizingVariable();
		unityPotential=nodeToCopy.isUnityPotential();
	}
	
	public Vector getChild(){
		return child;
	}
	
	public Vector getParents(){
			return parents;
		}
	
	public Node getNormalizingVariable(){
		return normalizingVariable; 
	}
	
	void createLinkTo(TreeNodeSV to){
		child.add(to);
		to.getParents().add(this);
	}
	
	void createLinkFrom(TreeNodeSV from){
				
		from.getChild().add(this);
		parents.add(from);
	}
	
	void createLinksTo(Vector toVector){
		int i;
		
		for (i=0;i<toVector.size();i++){
			createLinkTo((TreeNodeSV)(toVector.elementAt(i)));
		}
	}
	
	
	void createLinksFrom(Vector fromVector){
		int i;
		
		for (i=0;i<fromVector.size();i++){
			createLinkFrom((TreeNodeSV)(fromVector.elementAt(i)));
		}
	}
	
	
	void removeLinkTo(TreeNodeSV to){
		child.remove(to);
		to.getParents().remove(this);
	}
	
	void removeLinkFrom(TreeNodeSV from){
		parents.remove(from);
		from.getChild().remove(this);
	}
	
	void removeLinksTo(Vector toVector){
		
		int i;
		Vector auxToVector;
		
		auxToVector=new Vector(toVector);
		
		for (i=0;i<auxToVector.size();i++){
			removeLinkTo((TreeNodeSV)(auxToVector.elementAt(i)));
		}
	}
	
	void removeLinksFrom(Vector fromVector){
		int i;
		Vector auxFromVector;
		
		auxFromVector=new Vector(fromVector);
		
		for (i=0;i<auxFromVector.size();i++){
			removeLinkFrom((TreeNodeSV)(auxFromVector.elementAt(i)));
		}
	}
	
	
		
	/**
	* Get the children of this node of the tree that depend of x
	* and are leaves of the tree.
	*/
	public Vector getBranchesLeavesWithVariable(Node x){
		int i;
		TreeNodeSV aux;
		Vector leavesWithX=new Vector();
		
		for (i=0;i<child.size();i++){
			aux=(TreeNodeSV)(child.elementAt(i));
			if ((aux.kind==OPERAND)
			&&(aux.getRelation().getValues().getVariables().contains(x))){
				leavesWithX.add(aux);
			}
		}

		return leavesWithX;				
	}
	
	/**
	* Get the children of this node of the tree that are leaves.
	*/
	public Vector getBranchesLeaves(){
		int i;
		TreeNodeSV aux;
		Vector leaves=new Vector();
		
		for (i=0;i<child.size();i++){
			aux=(TreeNodeSV)(child.elementAt(i));
			if (aux.kind==OPERAND){
				leaves.add(aux);
			}
		}

		return leaves;				
		
		
	}
	
	/**
	* Get the children of this node of the tree that depend of x.
	* These children can be branches that aren't leaves.
	*/
	public Vector getBranchesWithVariable(Node x){
			int i;
			TreeNodeSV aux;
			Vector branchesWithX=new Vector();
					
			for (i=0;i<child.size();i++){
				aux=(TreeNodeSV)(child.elementAt(i));
				if (aux.isTreeWithVariable(x)){
					branchesWithX.add(aux);
				}
			}
			return branchesWithX;
	}	
	
	
	/**
	* Get the children of this node of the tree that depend of x.
	* These children can be branches that aren't leaves.
	*/
	public ArrayList<TreeNodeSV> getBranchesWithoutVariable(Node x){
			int i;
			TreeNodeSV aux;
			ArrayList<TreeNodeSV> branchesWithoutX=new ArrayList();
					
			for (i=0;i<child.size();i++){
				aux=(TreeNodeSV)(child.elementAt(i));
				if (!aux.isTreeWithVariable(x)){
					branchesWithoutX.add(aux);
				}
			}
			return branchesWithoutX;
	}	
	
	
	/**
	* Get the children of this node of the tree that depend of x and
	* that aren't leaves
	*/
	public Vector getBranchesNotLeavesWithVariable(Node x){
		int i;
		TreeNodeSV aux;
		Vector branchesWithX=new Vector();
					
				for (i=0;i<child.size();i++){
					aux=(TreeNodeSV)(child.elementAt(i));
					if (aux.isLeaf()==false){
						if (isTreeWithVariable(x)){
						branchesWithX.add(aux);
						}
					}
				}
				return branchesWithX;
	}
		
	
/**
* It returns 'true' iff the variable 'x' appears in some
* potentials of this tree or in some subtree.
*/
public boolean isTreeWithVariable(Node x){
	int i;
	boolean hasX=false;
	
	switch (getKindOfNodeTreeSV()){
		case OPERAND:
			//hasX=(getRelation().getValues().getVariables().contains(x));
			hasX=getRelation().isInRelation(x);
			break;
		case OPERATOR:
			for (i=0;(i<child.size())&&(hasX==false);i++){
				hasX=getChild(i).isTreeWithVariable(x);
			}
			break;
	}
	return hasX;
}							

		
	
/*	public void removeChildren(Vector childToRemove){
		int i;
		
		for (i=0;i<childToRemove.size();i++){
			removeOfChild((TreeNodeSV)childToRemove.elementAt(i));
		}
	}*/
			
	
	
	
	public TreeNodeSV getChild(int i){
		return (TreeNodeSV)(child.elementAt(i));
	}
	
	public int getKindOfNodeTreeSV(){
		return kind;
	}
	
	public int getOperator(){
		return operator;
	}
	
	public void addToChild(Relation rel){
		createLinkTo(new TreeNodeSV(rel));
	}
	

	
	

	
	public void addToChild(int oper){
		createLinkTo(new TreeNodeSV(oper));
	}
	
	public boolean isLeaf(){
		return (child.size()==0);
	}
	
	
	/*public NodeList getVariables(){
			return variables;
	}*/
	
	public Relation getRelation(){
		return relation;
	}
	
	
	public void setRelation(Relation r){
		relation=r;
	}
	
	
	public void setPotential(Potential pot){
		relation=makeRelationFromPotential(pot,Relation.POTENTIAL);
	}
	
	
	public void setNormalizingVariable(Node x){
		normalizingVariable=x;
	}
	
	/*public void setVariables(NodeList vars){
		variables=vars;
	}*/

	/**
	* Calculate the attribute 'variables'
	*/
/*	public void updateVariables() {
		int i;
		TreeNodeSV aux;

		switch (kind) {
			case OPERAND :
				//Set as variables the variables of the potential
				setVariables(new NodeList(getRelation().getValues().getVariables()));
				break;
			case OPERATOR :
				//Update the variables of the children and merge all
				//these sets of variables.
				variables = new NodeList();
				for (i = 0; i < child.size(); i++) {
					aux = getChild(i);
					aux.updateVariables();
					variables.merge(aux.getVariables());
				}
				break;

		}
	}*/
	
	
	/**
	* It makes there aren't consecutive operators of the same kind
	* in the tree. E.g. a SUM in level i and a SUM in level i+1
	*/
	public void compactTree(){
		int i;
		TreeNodeSV aux;
		int operOfAux;
		
		switch(kind){
			case OPERAND:
				//The operands (potential table) are always compacted
				break;
			case OPERATOR:
				//Compact the children of the operator and take up
				//the children of child which kind of operator is equal
				//than OPERATOR
				
				//child must be cloned before the loop for because child can change for the 
				//body of the loop
				Vector initialChildren=new Vector();
				initialChildren=(Vector)(child.clone());
								
				for (i = 0; i < initialChildren.size(); i++) {
					aux = (TreeNodeSV)(initialChildren.elementAt(i));
					aux.compactTree();
					if (aux.getKindOfNodeTreeSV()==OPERATOR){
						operOfAux=aux.getOperator();
						if (operOfAux==getOperator()){
						//We compact when there are two consecutive operators of the 
						//same kind
							System.out.println("Supresión de un nodo "+((operator==SUM)?"SUMA":"PRODUCTO"));
							mergeWith(aux);
						}
						else if (aux.getChild().size()==1){
						//We also compact when we have an only operand in an operator
							aux.replaceByItsOnlyChild();
							
						}
					}
					else{ //aux is an OPERAND
						if (isProduct()){
							if ((aux.isUnityPotential())&&(getChild().size()>1)){
							//Prune the child that is a unity potential in products.
							System.out.println("Poda de un nodo potencial unidad.");
							removeLinkTo(aux);
							}
						}
					}
				}
				break;
		}
	}


	/**
	* It makes all the children of some PRODUCT that are leaves and
	* depend of the variable 'x' are combined in only one leaf for each
	* PRODUCT. (It computes the product of the leaf potentials).
	*/
	public void compactProducts(Node x){
		int i;
		TreeNodeSV iChild;
		
		switch(kind){
			case OPERAND:
				//The operands (potential table) are always compacted
				break;
			case OPERATOR:
				//Compact the leaves of the children of the operator.
				//If the operator is a PRODUCT we compact the leaves
				//with X in the relation.
				for (i=0;i<child.size();i++){
					iChild=((TreeNodeSV)(child.elementAt(i)));
					iChild.compactProducts(x);
					if (iChild.getChild().size()==1){
						//Remove the unary operator, e.g. (+ 3 (x 4)) = (+ 3 4)
						iChild.replaceByItsOnlyChild();
					}
				}
	  
				if (operator==PRODUCT){
					combineLeavesWithVariable(x);
				}
		}
	}
	
	

	//This TreeNodeSV of kind OPERATOR can have only one operand after
	//the combination of children. In this case the OPERATOR is replaced
	//by its only child.
	/*public void replaceByItsOnlyChild(){
		TreeNodeSV onlyChild;
		
		if (child.size()!=1){
			 System.out.println("Error: The number of children must be 1");
			 System.out.println("replaceByItsOnlyChild(TreeNodeSV)");
			 System.exit(1);
		   } 
		   else{
		   		onlyChild=(TreeNodeSV)(child.elementAt(0));
		   		kind=onlyChild.getKindOfNodeTreeSV();
		   		operator=
		   	
		   }
	}*/
	
	
	
	private boolean isOperatorWithOnlyOneOperand(){
		return ((kind==OPERATOR)&&(child.size()==1));
	}
	/**
	* It computes the product of the leaves that are children and
	* depend of the variable 'x'
	*/
	public void combineLeavesWithVariable(Node x) {
		Vector childWithX;

		//Get the children of this treenodeSV that are leaves and
		//have got the variable X in their domains.
		childWithX = getBranchesLeavesWithVariable(x);
		
		//Aggregate them in an only leaf
		combineLeaves(childWithX);

	}
	
	/**
	* It computes the product of the leaves 'childrenWithX',
	* that are children of this and depend of the variable 'x'.
	* The potentials of 'childrenWith' are multiplied.
	*/
	public void combineLeaves(Vector childrenWithX) {
	/*	int i;
			TreeNodeSV aux;
			boolean multiply;
			RelationList relations = new RelationList();
			Vector childWithX;
			int length;
			Relation auxRel;
			Relation newRel;

			Potential acum;

	
			length = childrenWithX.size();

			if (length > 0) {
				if (length == 1)
					return;
				else {
					//Compute the aggregate potential of the leaves dependent
					//of X
					acum = ((TreeNodeSV) (childrenWithX.elementAt(0))).getRelation().getValues();
					for (i = 1; i < length; i++) {
						auxRel = ((TreeNodeSV) (childrenWithX.elementAt(i))).getRelation();
						acum = acum.combine(auxRel.getValues());
					}
					//Make the new relation that will be child of this TreeNodeSV
					newRel = makeRelationFromPotential(acum, Relation.POTENTIAL);
					//Remove the old children of this TreeNodeSV dependent of X
					removeLinksTo(childrenWithX);
					//Add to children a TreeNodeSV with the new relation dependent of X
					addToChild(newRel);
				}
			}*/
			combineLeaves(childrenWithX,TreeNodeSV.PRODUCT);
	}
	

	/**
	* It computes the product or the sum (depending of 'oper') of
	* the leaves 'children', that are children of this
	*/
	private void combineLeaves(Vector children, int oper) {
		int i;
		int length;
		Relation auxRel;
		Relation newRel;
		Function function;

		PotentialTable acum;


		length = children.size();
		
		
		if (oper==TreeNodeSV.SUM){
			function=new SumFunction();
		}
		else{//oper==TreeNodeSV.PRODUCT
			function=new ProductFunction();
		}

		if (length > 0) {
			if (length == 1)
				return;
			else {
				//Compute the aggregate potential of the leaves dependent
				//of X
				acum = (PotentialTable) ((TreeNodeSV) (children.elementAt(0))).getRelation().getValues();
				for (i = 1; i < length; i++) {
					auxRel = ((TreeNodeSV) (children.elementAt(i))).getRelation();
					acum = (PotentialTable) acum.combine((PotentialTable) auxRel.getValues(),function);
				}
				//Make the new relation that will be child of this TreeNodeSV
				newRel = makeRelationFromPotential(acum, Relation.POTENTIAL);
				//Remove the old children of this TreeNodeSV dependent of X
				removeLinksTo(children);
				//Add to children a TreeNodeSV with the new relation dependent of X
				addToChild(newRel);
			}
		}
	
	}

/**
 * Method to create a new relation with the variables of
 * the chance potential. The potential is transformed after
 * the addition
 * @param <code>Potential</code> chance potential
 * @param kind of relation to create (POTENTIAL by the moment)
 * @return <code>Relation</code> relation made from variables in potential
 */
private static Relation makeRelationFromPotential(Potential pot, 
										   int kind) {
  Relation r=null;


   // Check the kind of the relation

   if (kind != Relation.POTENTIAL) {
	 System.out.println("Error in kind of relation to create");
	 System.out.println("makeRelationFromPotential(TreeNodeSV)");
	 System.exit(1);
   } 

   // Works only for potentials not null

   if (pot != null) {
	 
	 // Creates the new relation
    
	 r=new Relation();
	 r.setKind(OPERAND);
	 r.getVariables().setNodes((Vector)pot.getVariables().clone());
	 r.setValues(pot);
	}

	return r;
}

	/**
	* Precondition: 'n' is a child of 'this'
	* It makes 'this' is replaced by 'n', so all the parents and children
	* of 'this' become in parents of 'n', and the link between 'this' and 'n'
	* is deleted
	* This function is used to compact the tree.
	*/
	public void mergeWith(TreeNodeSV n) {
		Vector childOfN;
				
		//Remove the link between 'this' and 'n'
		removeLinkTo(n);
		
		childOfN=n.getChild();

		//The children of 'n' become in children of 'this'
		createLinksTo(childOfN);
		
		//Check if it was a shared node.
		if (n.getParents().size()==0){
			//If 'n' isn't a shared node it's uncoupled of the tree
			n.delete();
		}
		
		
		
		
		
	
	}
	
	
	/**
	* It makes 'this' is replaced by its only child, so all the parents and children
	* of its only child become in parents and children respectively of 'this', and
	* the link between 'this' and its only child is deleted. Its only child become
	* in an isolated node without parents and without children.
	* This function is used to compact the tree.
	*/
	public void replaceByItsOnlyChild(){
		TreeNodeSV onlyChild;
		Vector parentsOnlyChild;
			
		onlyChild=getChild(0);
		
		parentsOnlyChild=onlyChild.getParents();
		
		removeLinkTo(onlyChild);
		
		//The parents of onlyChild are directed to 'this'
		createLinksFrom(parentsOnlyChild);
		
		//The children of onlyChild become in children of 'this'
		createLinksTo(onlyChild.getChild());
		
		//The information of 'onlyChild' is copied in 'this'
		copy(onlyChild);
		
		//'onlyChild' is uncoupled of the tree
		onlyChild.delete();
	
	}
	
	/**
	 * Preconditions: x is a chance node and the tree has been correctly reduced so
	 * the marginalization can be applied only to the leaves of the tree 
	 * It computes the marginalization of all the leaves potentials of the tree
	 */
	public void marginalizeChance(Node x){
		int i;
		
		switch(getKindOfNodeTreeSV()){
			case OPERAND:
				Potential newPot;	
				
				//To compute the sum-marginalization
				if (x!=normalizingVariable){
					newPot=getRelation().getValues().addVariable(x);
				}
				else{
					//The sum-marginalization will be 1 because we are going
					//to sum-marginalize over the marginalizing variable of the potential
					newPot=new PotentialTable();
					newPot.setValue(new Configuration(),1);
					System.out.println("Unity potential not computed, because of the variable "+x.getName());
					setUnityPotential(true);
				}
				setRelation(makeRelationFromPotential(newPot, Relation.POTENTIAL));
				break;
			case OPERATOR:
				for (i = 0; i < child.size(); i++) {
					((TreeNodeSV)(getChild(i))).marginalizeChance(x);
				}
				break;
		}
	}	
	
	
	/**
	 * It computes a lifo structure with the products that can be distributed
	 */
	public void computeStackOfProductsToDistribute(Node x, Stack products){
		int i;
		
		if (isDistributiveProduct(x)) products.push(this);
		for (i=0;i<child.size();i++){
			getChild(i).computeStackOfProductsToDistribute(x,products);
		}
	}

	/**
	 * It returns 'true' iff this TreeNodeSV is a PRODUCT with more
	 * than one branch dependent of 'x'
	 * Precondition: This TreeNodeSV has at the most one leaf that
	 * depends of 'x'.
	 */
	public boolean isDistributiveProduct(Node x){
		int numDep=0;
		TreeNodeSV aux;
		boolean isDistributive=false;
				
		if (getKindOfNodeTreeSV()==OPERAND) return false;
		else{
			if (getOperator()==SUM) return false;
			else{
				return (getBranchesWithVariable(x).size()>=2);
				/*for (int i=0;(i<child.size())&&(isDistributive==false);i++){
					aux=(TreeNodeSV)(child.elementAt(i));
					if (aux.isTreeWithVariable(x)){
						numDep++;
						isDistributive=numDep>=2;
					}
				}
				return isDistributive;*/
				
			}
		}
		
	}
	
	/**
	 * Method that returns the factor that multiplies in a distributive
	 * product respect a variable
	 * @param <code>Node</code> Variable that makes the product be distributive
	 * @return <code>TreeNodeSV</code> Factor that multiplies to a sum
	 */
	public TreeNodeSV getFactorOfDistribution(Node x){
		Vector branchesLeavesX;
		TreeNodeSV factor=null;
		Vector branchesNotLeavesX;
		
		branchesLeavesX=getBranchesLeavesWithVariable(x);
		
		//The factor can be an operand
		if (branchesLeavesX.size()==1){
			factor=(TreeNodeSV)(branchesLeavesX.elementAt(0));
		}
		else{//(branchesLeavesX.size()==0)
			//The factor must be an operator SUM
			branchesNotLeavesX=getBranchesNotLeavesWithVariable(x);
			
			if (branchesNotLeavesX.size()<2){
					System.out.println("Error: Distributive can't be applied in this TreeNodeSV");
					System.out.println("getFactorOfDistribution(TreeNodeSV)");
					System.exit(1);	
			}
			else{
				//Select as 'factor' some SUM
				factor=(TreeNodeSV)(branchesNotLeavesX.elementAt(0));
			}
		}
		
		return factor;
						
			/*if (branchesLeavesX.size()>0){//One factor is a leaf potential
				
				if (branchesLeavesX.size()>1){
					//The leaf potentials aren't combined
				
					System.out.println("Error: TreeNodeSV should have an only leaf dependent of X");
					System.out.println("getFactorAndSumOfDistribution(TreeNodeSV)");
					combineLeaves(branchesLeavesX);
					branchesLeavesX=getBranchesLeavesWithVariable(x);
					if (branchesLeavesX.size()>1){
						System.out.println("Error: TreeNodeSV should have an only leaf dependent of X after calling to 'combineLeaves'");
						System.out.println("getFactorAndSumOfDistribution(TreeNodeSV)");
						System.exit(1);
					}
				}*/
			
			
		
	}
	
	
	/**
	 * Method that returns the sum that is multiplied in a distributive
	 * product respect a variable
	 * @param <code>Node</code> Variable that makes the product be distributive
	 * @param <code>TreeNodeSV</code> Factor that multiplies to a sum
	 * @return <code>TreeNodeSV</code> Sum that is multiplied in a distributive
	 * product respect a variable. The object returned must be different to 'factor'
	 */
	public TreeNodeSV getSumOfDistribution(Node x,TreeNodeSV factor){
		
		int i;
		Vector branchesNotLeavesX;
		boolean found=false;
		TreeNodeSV aux;
		TreeNodeSV sum=null;
		int length;
		
		branchesNotLeavesX=getBranchesNotLeavesWithVariable(x);
		length=branchesNotLeavesX.size();
		
		//The distributive can't be applied
		if (((factor.getKindOfNodeTreeSV()==OPERATOR)&&(length<2))||(length<1)){
			System.out.println("Error: Distributive can't be applied in this TreeNodeSV");
			System.out.println("getFactorAndSumOfDistribution(TreeNodeSV)");
			System.exit(1);	
		}
		else{//The distributive can be applied
								
				//Find a TreeNodeSV dependent of x and different to 'factor'
				for(i=0;i<branchesNotLeavesX.size()&&(found==false);i++){
					aux=(TreeNodeSV)(branchesNotLeavesX.elementAt(i));
					if (aux!=factor){
						found=true;
						sum=aux;
					}	
				}
		}
		return sum;
	}

	/**
	 * Method that applies the distributive operation between 'factor' and 'sum
	 * The application of the operation isn't optimized, so it completely cuts up 
	 * the 'sum'.
	 */
	public void distribute(TreeNodeSV factor,TreeNodeSV sum){
		int i;
		Vector sumands=new Vector();
		TreeNodeSV newSum;
		TreeNodeSV auxProduct;
		TreeNodeSV iSumand;
		
		removeLinkTo(factor);
		removeLinkTo(sum);
		
		sumands=(Vector)(sum.getChild().clone());
		
		//The links among sum and sumands are deleted if there isn't
		//any TreeNodeSV that aims at sum 
		if (sum.getParents().size()==0){
			sum.removeLinksTo(sumands);
		}
		
		newSum=new TreeNodeSV(SUM);
		
		createLinkTo(newSum);
		
		for (i=0;i<sumands.size();i++){
			iSumand=(TreeNodeSV)(sumands.elementAt(i));
			auxProduct=new TreeNodeSV(PRODUCT);
			newSum.createLinkTo(auxProduct);
			auxProduct.createLinkTo(factor);
			auxProduct.createLinkTo(iSumand);
		}
		
		//Fuse the PRODUCT with his only child SUM if there aren't more
		//children
		if (getChild().size()==1) replaceByItsOnlyChild();
	}
	
	/**
	 * Method that applies the distributive operation between 'factor' and 'sum
	 * The application of the operation is optimized for the case the next operation
	 * to make on the tree is a marginalization on the chance variable x, so it completely cuts up 
	 * the 'sum'.
	 */
	//TODO: Arreglar porque falla con problematico*.elv
	public void distributeForMarginalizeChance(TreeNodeSV factor,TreeNodeSV sum, Node x){
		int i;
		Vector sumands=new Vector();
		TreeNodeSV newSum;
		TreeNodeSV auxProduct;
		TreeNodeSV iSumand;
		boolean canApplyUnityAxiom=false;
		int length;
		
		removeLinkTo(factor);
		removeLinkTo(sum);
		
		sumands=(Vector)(sum.getChild().clone());
		
		//The links among sum and sumands are deleted if there isn't
		//any TreeNodeSV that aims at sum 
		if (sum.getParents().size()==0){
			sum.removeLinksTo(sumands);
		}
		
		newSum=new TreeNodeSV(SUM);
		
		createLinkTo(newSum);
		
		//See whether unity axiom can be applied, so some distributions can be unnecessary
		canApplyUnityAxiom=(factor.getNormalizingVariable()==x);
		
		length = sumands.size();
		
		for (i=0;i<length;i++){
			iSumand=(TreeNodeSV)(sumands.elementAt(i));
			//If Unity axiom can be applied and 'iSumand' doesn't depend on 'x' and it don't
			//remain any sumand for distribution then we haven't to distribute
			if ((canApplyUnityAxiom==false)||(iSumand.isTreeWithVariable(x))||(i<(length-1))){
				auxProduct=new TreeNodeSV(PRODUCT);
				newSum.createLinkTo(auxProduct);
				auxProduct.createLinkTo(factor);
				auxProduct.createLinkTo(iSumand);
			}
			else{
				newSum.createLinkTo(iSumand);
				System.out.println("Don't distribute because of the unity axiom for the variable "+x.getName());
			}
		}
		
		//Fuse the PRODUCT with his only child SUM if there aren't more
		//children
		if (getChild().size()==1) replaceByItsOnlyChild();
	}

	/*	public PotentialTable marginalizeDecisionWithoutDivisions(Node x){
			Vector branchesX;
			Potential pot;
			Vector vars;
			TreeNodeSV separated;
			int sizeBranchesX;
			PotentialTable potCopy=null;
		
			switch(getKindOfNodeTreeSV()){
				case OPERAND:
					//To compute the max-marginalization
					pot=getRelation().getValues();
					//Copy the table of the decision before it's eliminated
					potCopy=(PotentialTable) ((PotentialTable)pot).copy();
					potCopy=(PotentialTable)(potCopy.sendVarToEnd(x));
					//Marginalize the table
					vars=new Vector(pot.getVariables());
					vars.removeElement(x);
					pot=pot.maxMarginalizePotential(vars);
					//Set relation with the new marginalized potential
					setRelation(makeRelationFromPotential(pot, Relation.POTENTIAL));
					break;
				case OPERATOR:
					branchesX=getBranchesWithVariable(x);
					sizeBranchesX=branchesX.size();
					if (sizeBranchesX!=0){
						if (sizeBranchesX==child.size()){
							//Compact all the tree in an only leaf
							reduce();
							//Marginalize over the reduced tree
							potCopy=marginalizeDecisionWithoutDivisions(x,statistics);
						}
						else if (branchesX.size()==1){
							potCopy=((TreeNodeSV)(branchesX.elementAt(0))).marginalizeDecisionWithoutDivisions(x, statistics);
						}
						else{
							//Some branches depend of the decision
							separated=separate(branchesX);
							separated.reduce();
							potCopy=separated.marginalizeDecisionWithoutDivisions(x, statistics);
						
						}
					}
					break;
			}
			
			return potCopy;
			
	}*/
	
	
	/**
	 * Max-marginalize the tree into the variable x.
	 * Preconditions: x is a decision node and the tree has been reduced into one leafe, so
	 * there are almost one leaf which depends on x.
	 */
	public PotentialTable marginalizeDecision(Node x){
		Vector branchesX;
		Potential pot;
		Vector vars;
		TreeNodeSV separated;
		int sizeBranchesX;
		PotentialTable potCopy=null;
	
		switch(getKindOfNodeTreeSV()){
			case OPERAND:
				if (isTreeWithVariable(x)) {
					//To compute the max-marginalization
					pot = getRelation().getValues();
					//Copy the table of the decision before it's eliminated
					potCopy = (PotentialTable) ((PotentialTable) pot).copy();
					potCopy = (PotentialTable) (potCopy.sendVarToEnd(x));
					//Marginalize the table
					vars = new Vector(pot.getVariables());
					vars.removeElement(x);
					pot = pot.maxMarginalizePotential(vars);
					//Set relation with the new marginalized potential
					setRelation(makeRelationFromPotential(pot, Relation.POTENTIAL));
				} else {
					potCopy = null;
				}
				break;
			case OPERATOR:
				for (int i = 0; (i < child.size())&&(potCopy==null); i++) {
					potCopy = ((TreeNodeSV)(getChild(i))).marginalizeDecision(x);
				}
				break;
		}
		
		return potCopy;
		
}
		
	
	
	
	/**
	 * Method that groups a set of branches in a subtree. This subtree is
	 * allocated as a child of 'this'. This method is used in order to
	 * group all the branches that depends of a variable X. The subtree
	 * that groups these branches is returned by the method.
	 */
	public TreeNodeSV separate(Vector branchesToSepare){
		TreeNodeSV separateTree;
		
		//The branches dependent of X mustn't be children of 'this'
		removeLinksTo(branchesToSepare);
		
		//Create a new TreeNodeSV of same kind of operator
		separateTree=new TreeNodeSV(getOperator());
		
		//The new node must be a child of 'this'
		createLinkTo(separateTree);
		
		//All the branches dependent of X are children of the new tree
		separateTree.createLinksTo(branchesToSepare);
		
		return separateTree;
		
	}
	
	
	/**
	 * Method that reduces a tree into a leaf. This reduction is computed
	 * through the potentials and the operators contained in the tree.
	 */
	public void reduce() {

		int i;
		Relation auxRel;
		Potential acum;
		Function f = null;
		Relation newRel;

		switch (getKindOfNodeTreeSV()) {
			case OPERAND :
				//The tree is a leaf, so it's reduced.
				break;
			case OPERATOR :
				//Determine the Function 
				switch (getOperator()) {
					case SUM :
						f = new SumFunction();
						break;
					case PRODUCT :
						f = new ProductFunction();
						break;
				}

				//Reduce all the children trees
				for (i = 0; i < child.size(); i++) {
					getChild(i).reduce();
				}

				//Compute the aggregate potential of the reduction
				//of the children. ERROR if there aren't any children
				acum =
					((TreeNodeSV) (child.elementAt(0)))
						.getRelation()
						.getValues();
				for (i = 1; i < child.size(); i++) {
					auxRel = ((TreeNodeSV) (child.elementAt(i))).getRelation();
					if (acum.getClass() == PotentialTable.class) {
						acum =
							((PotentialTable) acum).combine(
								(PotentialTable) auxRel.getValues(),
								f);
					} else {
						System.out.println(
							"Error: All the potentials in the tree must be 'PotentialTable'");
						System.out.println("reduce(TreeNodeSV)");
						System.exit(1);
					}
				}

				//children=(Vector)child.clone();

				//Make the new relation
				newRel = makeRelationFromPotential(acum, Relation.POTENTIAL);
				//Set the new attributes of the tree
				kind = OPERAND;
				relation = newRel;
				//Remove the children
				removeLinksTo(child);

		}
	}
		/**
		 * Method similar to 'reduce'.
		 * 'reduce' transforms the tree.
		 * However, 'evaluate' doesn't transform the tree. It only
		 * computes the aggregate potential equivalent to the tree.
		 */
		public PotentialTable evaluate(){
			
			int i;
			PotentialTable auxPot=null;
			PotentialTable acum=null;
			Function f = null;

			switch (getKindOfNodeTreeSV()) {
				case OPERAND :
					//The tree is a leaf, so its evaluation is its potential
					acum=(PotentialTable)(getRelation().getValues());
					break;
				case OPERATOR :
					//Determine the Function 
					switch (getOperator()) {
						case SUM :
							f = new SumFunction();
							break;
						case PRODUCT :
							f = new ProductFunction();
							break;
					}

					//Compute the aggregate potential of the evaluation
					//of the children. ERROR if there aren't any children
					acum = getChild(0).evaluate();
					for (i = 1; i < child.size(); i++) {
						auxPot = getChild(i).evaluate();
						acum = acum.combine(auxPot,f);
					}
		}
		return acum;
		
	}

		/**
		 * @param leafRelations
		 * @return
		 */
		public void getListOfRelations(RelationList leafRelations) {
			// TODO Auto-generated method stub
			switch(kind){
				case TreeNodeSV.OPERAND:
					if ((leafRelations.indexOf(getRelation()))==-1){
						leafRelations.insertRelation(getRelation());
					}
					break;
				case TreeNodeSV.OPERATOR:
					for (int i=0;i<child.size();i++){
						getChild(i).getListOfRelations(leafRelations);
					}
					break;
			}
		}

		/**
		 * To apply the subset rule once at a pair of leaves descendentes of this node
		 * of the tree
		 * @return true iff the rule is applied at a pair of leaves
		 */
		public boolean applySubsetRule() {
			int sizeLeaves;
			TreeNodeSV auxI,auxJ;
			Vector auxVector=new Vector();
			boolean isAppliedRule=false;
			TreeNodeSV auxChild;
			
			if (kind==TreeNodeSV.OPERAND){
				isAppliedRule=false;
			}
			else{//(kind==TreeNodeSV.OPERATOR)
				Vector leaves;
				
				//See if the subset rule can be applied to the direct children that are leaves
				leaves=this.getBranchesLeaves();
				sizeLeaves=leaves.size();
				
				for (int i=0;(i<sizeLeaves-1)&&(isAppliedRule==false);i++){
					auxI=(TreeNodeSV) leaves.elementAt(i);
					for (int j=i+1;(j<sizeLeaves)&&(isAppliedRule==false);j++){
						auxJ=(TreeNodeSV) leaves.elementAt(j);
						if (auxI.verifySubsetRule(auxJ)){
							auxVector.add(auxI);
							auxVector.add(auxJ);
							System.out.println("Apply subset rule");
							combineLeaves(auxVector,this.operator);
							isAppliedRule = true;
						}
					}
				}
				
				//See if the subset rule can be applied to the children that are operators
				for (int i=0;(i<child.size())&&(isAppliedRule==false);i++){
					auxChild=getChild(i);
					if (auxChild.getKindOfNodeTreeSV()==TreeNodeSV.OPERATOR){
						isAppliedRule = auxChild.applySubsetRule();
					}
				}
			}
			return isAppliedRule;
				
	
		}


		/**
		 * @param auxJ
		 * @return
		 */
		private boolean verifySubsetRule(TreeNodeSV auxJ) {
			// TODO Auto-generated method stub
			NodeList listI=new NodeList();
			NodeList listJ=new NodeList();
			
			if ((this.kind==TreeNodeSV.OPERATOR)||(auxJ.getKindOfNodeTreeSV()==TreeNodeSV.OPERATOR)){
				return false;
			}
			else{
				listI.setNodes(getRelation().getValues().getVariables());
				listJ.setNodes(auxJ.getRelation().getValues().getVariables());
				return ((listI.kindOfInclusion(listJ).equals("subset"))||
						(listJ.kindOfInclusion(listI).equals("subset")));
			}
		}

		public void print(){
			switch(kind){
				case TreeNodeSV.OPERAND:
					System.out.println("OPERAND whose relation is:");
					getRelation().print();
					break;
				case TreeNodeSV.OPERATOR:
					System.out.println("OPERATOR "+((operator==TreeNodeSV.SUM)?"SUM":"PRODUCT"));
					break;
			}
		}
	/**
	 * @return Returns the unityPotential.
	 */
	public boolean isUnityPotential() {
		return unityPotential;
	}
	/**
	 * @param unityPotential The unityPotential to set.
	 */
	public void setUnityPotential(boolean unityPotential) {
		this.unityPotential = unityPotential;
	}

	/**
	 * @param x
	 */
	public void prepareToMarginalizeDecision(Node x) {
		// TODO Auto-generated method stub
		Vector branchesX;
		Potential pot;
		Vector vars;
		TreeNodeSV separated;
		int sizeBranchesX;
		PotentialTable potCopy=null;
	
		switch(getKindOfNodeTreeSV()){
			case OPERAND:
				//Leaves are always prepared to marginalize any variable
				break;
			case OPERATOR:
				branchesX=getBranchesWithVariable(x);
				sizeBranchesX=branchesX.size();
				if (sizeBranchesX!=0){
					if (sizeBranchesX==child.size()){
						//Compact all the tree in an only leaf
						reduce();
					}
					else if (branchesX.size()==1){
						((TreeNodeSV)(branchesX.elementAt(0))).prepareToMarginalizeDecision(x);
					}
					else{
						//Some branches depend of the decision
						separated=separate(branchesX);
						separated.reduce();
					}
				}
				break;
		}
		
		return;
		
	}

	
	/**
	 * Method prepareToMarginalizeDecision (its above) has been improved for ADGs where the
	 * tree depending on D can be shared by different paths.
	 * @param x
	 /*
	public void prepareToMarginalizeDecision(Node x) {
		// TODO Auto-generated method stub
		Vector branchesX;
		Potential pot;
		Vector vars;
		TreeNodeSV separated;
		int sizeBranchesX;
		PotentialTable potCopy=null;
		ArrayList<TreeNodeSV> dephestDescDep;
		TreeNodeSV deepestNode;
	
		switch(getKindOfNodeTreeSV()){
			case OPERAND:
				//Leaves are always prepared to marginalize any variable
				break;
			case OPERATOR:
				//Compute the dephest node that join all the dependent branches and calculate
				//also those branches
				deepestNode = computeDeepestDescJoinsDependenciesOn(x);
				if (deepestNode!=null){
					
					branchesX = deepestNode.getBranchesWithVariable(x);
					sizeBranchesX=branchesX.size();
					
				if (sizeBranchesX!=0){
					if (sizeBranchesX==deepestNode.child.size()){
						//Compact all the tree in an only leaf
						deepestNode.reduce();
					}
					else{
						//Some branches depend of the decision
						separated=deepestNode.separate(branchesX);
						separated.reduce();
					}
				}
				}
				else{//deepestNode==null means the tree does not depend on the decision
					
				}
				break;
		}
		
		return;
		
	}
*/
	/**
	 * It computes the dephest node that join all the dependent branches and calculate
	 * also those branches
	 * @param x
	 * @param deepestNode
	 * @param branchesX
	 */
	private TreeNodeSV computeDeepestDescJoinsDependenciesOn(Node x) {
		// TODO Auto-generated method stub
		ArrayList<TreeNodeSV> deepestNodes;
		TreeNodeSV auxDeepestNode;
		boolean areEqualsDescs;
		TreeNodeSV auxChild,auxNode;
		TreeNodeSV deepestNode = null;
		
		switch(getKindOfNodeTreeSV()){
		case OPERAND:
			if (this.isTreeWithVariable(x)){
				deepestNode = this;
			}
			else{
				deepestNode = null;
			}
			//Leaves are always prepared to marginalize any variable
			break;
		case OPERATOR:
			//Compute for each child the dephest node that join all the dependent branches
			deepestNodes = new ArrayList();
			for (int i=0;i<child.size();i++){
				auxChild = (TreeNodeSV)(child.elementAt(i));
				auxDeepestNode = auxChild.computeDeepestDescJoinsDependenciesOn(x);
				if (auxDeepestNode!=null){
					deepestNodes.add(auxDeepestNode);
				}
			}
			//Find out if the deepest node is the same for every child
			if (deepestNodes.size()==0){
				deepestNode = null;
			}
			else if (deepestNodes.size()==1){
				deepestNode = deepestNodes.get(0);
			
			}
			else{
				auxNode = deepestNodes.get(0);
				areEqualsDescs = true;
				for (int i=1;(i<deepestNodes.size())&&areEqualsDescs;i++){
					areEqualsDescs = (auxNode == deepestNodes.get(i));
				}
				if (areEqualsDescs){
					deepestNode = auxNode;
				}
				else{
					deepestNode = this;
				}
			}
			break;
			
	}
		return deepestNode;
	}

	/**
	 * It groups the children of a sum node that don't depend on the chance variable to be eliminated.
	 * It avoids some unnecessary applications of the distributive property.
	 * @param x
	 */
	public void groupAddendsDontDependOn(Node x) {
		ArrayList<TreeNodeSV> addendsWithoutX;
		TreeNodeSV newSum;

		addendsWithoutX = getBranchesWithoutVariable(x);

		// We only create a new sum node if we have several branches don't
		// depending on X
		if (addendsWithoutX.size() > 1) {

			// Remove the links to the sumands without X
			for (TreeNodeSV auxTreeNodeSV : addendsWithoutX) {
				removeLinkTo(auxTreeNodeSV);
			}

			// Create a new sum node
			newSum = new TreeNodeSV(SUM);

			// Create links from this to the new sum
			createLinkTo(newSum);

			// Create links from the new sum to the branches without X
			for (TreeNodeSV auxTreeNodeSV : addendsWithoutX) {
				newSum.createLinkTo(auxTreeNodeSV);
			}
		}

	}

	
		// TODO Auto-generated method stub
		
	
	
	/**
	* It makes all the children of some SUM or PRODUCT that are leaves and
	* depend of the variable 'x' are combined in only one leaf for each operator
	* (It computes the corresponding operation) .
	*/
public void compactSumsAndProducts(Node x) {
		int i;
		TreeNodeSV iChild;
		
		switch(kind){
			case OPERAND:
				//The operands (potential table) are always compacted
				break;
			case OPERATOR:
				//Compact the leaves of the children of the operator.
				for (i=0;i<child.size();i++){
					iChild=((TreeNodeSV)(child.elementAt(i)));
					iChild.compactSumsAndProducts(x);
					if (iChild.getChild().size()==1){
						//Remove the unary operator, e.g. (+ 3 (x 4)) = (+ 3 4)
						iChild.replaceByItsOnlyChild();
					}
				}
				//Combine the leaves that depend on X
				combineLeavesWithVariable(x);
				
		}
	}

	public void prepareToMarginalizeDecisionByUnforking(Node x) {
	/*	// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		Vector branchesX;
		Potential pot;
		Vector vars;
		TreeNodeSV separated;
		int sizeBranchesX;
		PotentialTable potCopy=null;
	
		switch(getKindOfNodeTreeSV()){
			case OPERAND:
				//Leaves are always prepared to marginalize any variable
				break;
			case OPERATOR:
				branchesX=getBranchesWithVariable(x);
				sizeBranchesX=branchesX.size();
				if (sizeBranchesX!=0){
					for (int i=0;i<branchesX.size();i++){
						((TreeNodeSV)(branchesX.elementAt(i))).prepareToMarginalizeDecisionByUnforking(x);
					}
					if (branchesX.size()==1){
						//We don't have to do anything else
					}
					else{
						if (this.getOperator()==TreeNode.SUM){
						//Some branches depend of the decision
						separated=separate(branchesX);
						separated.reduce();
						}
						else{
							
						}
					}
				}
				break;
		}
		*/
		return;
		
	}



}//END OF CLASS
