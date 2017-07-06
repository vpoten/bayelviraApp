package elvira.probabilisticDecisionGraph;

//import elvira.Node;
import java.util.*;

import elvira.*;
import elvira.probabilisticDecisionGraph.tools.*;

/**
 * 
 * This class implements a parameternode in a PDG model.
 * 
 * @author dalgaard
 * @since 07/02/07
 */

public class PDGParameterNode{

	/**
	 * 
	 */
	private final PDGVariableNode pdgVarNode;
	private static long nextId = 0;
	
	private static synchronized long nextId(){
		return nextId++;
	}
	
	/**
	 * A double array of parameters of this parameternode.
	 */
	private final double[] values;
	
	/**
	 * {@code pi} is a double array that holds for each index the product of 
	 * out-flows of all successor parameter-nodes that are reached by an edge 
	 * annotated whith the index. 
	 * 
	 * {@code pi} then holds intermediate results computed in out-flow computation
	 * {@link computeOfl} for efficient subsequent computation of in-flow in
	 * {@link computeIfl}. 
	 * 
	 */
	private final double[] pi;
	
	private final double[] count;
	private CaseListMem reach;
	
	
	boolean oflIsComputed = false;
	
	long id;
	/**
	 * 
	 */
	double outFlow = 1.0;
	/**
	 * 
	 */
	double inFlow = 1.0;
	
	/**  
     * {@code children} is an array of successor nodes.
     * Rows correspond to the different
     * successors of 'pdgVarNode' in the
     * underlying tree structure, and
     * columns correspond to the different 
     * values of 'mytn.varname', that is
     * children[k][l]=succ(this,Y,x_l)
     * if Y is the k'th variable in pdgVarNode.successors(),
     * and x_l is the l'th possible value of 
     * pdgVarNode.var 
	 */
	private PDGParameterNode[][] children = null;
	/**
	 * 
	 */
	private final Vector<PDGParameterNode> parents = new Vector<PDGParameterNode>();
	
	/**
	 * @param pdgvn
	 */
	public PDGParameterNode(PDGVariableNode pdgvn){
		pdgVarNode = pdgvn;
		pdgVarNode.addParameterNode(this);
		values = VectorOps.getNormalisedUniformDoubleArray(pdgVarNode.getNumStates());
		pi=new double[values.length];
		children = new PDGParameterNode[pdgVarNode.getSuccessors().size()][pdgVarNode.getNumStates()];
		count = new double[values.length];
		id = nextId();
	}

	/**
	 * @param pdgvn
	 * @param ch
	 */
	public PDGParameterNode(PDGVariableNode pdgvn, 
							PDGParameterNode[][] ch){
		pdgVarNode = pdgvn;
		pdgVarNode.addParameterNode(this);
		children = ch;
		values = VectorOps.getNormalisedUniformDoubleArray(pdgvn.getNumStates());
		pi=new double[values.length];
		count = new double[values.length];
		id = nextId();
	}
	
	public PDGParameterNode succ(PDGVariableNode y, int value){
		PDGParameterNode retval = null;
		if(children != null){
			try{
				retval = children[pdgVarNode.getSuccessors().indexOf(y)][value];
			} catch(Exception e){
				System.out.println("joder");
			}
		}
		return retval;
	}
	
	public void setCount(int state, int c){
		count[state] = c;
	}
	
	public void updateValuesFromReach(boolean smooth){
		double correction = (smooth ? 1.0 : 0.0);
		resetCounts();
		for(int i=0;i<reach.getNumberOfCases();i++){
			count[reach.get(i).getValue(pdgVarNode.getVar())]++;
		}
		for(int i=0;i<count.length;i++)
			values[i] = count[i] + correction;
		VectorOps.normalise(values);
	}
	
	public int getVectorIndex(){
		return pdgVarNode.getVectorIndexOf(this);
	}
	
	public void updateValuesFromCount(boolean smooth){
		double correction = smooth ? 1.0 : 0.0;
		for(int i=0;i<count.length;i++){ values[i] = (count[i] + correction);}
		VectorOps.normalise(values);
	}
	
	void clearCounts(){
		for(int i=0;i<count.length;i++) count[i] = 0;
	}
	
	public double getInflow(){
		return inFlow;
	}
	
	public double getOutflow(){
		return outFlow;
	}
	

	/**
	 * This method checks if this PDGParameterNode is the parent of the argument PDGParameterNode p.
	 * 
	 * @param p
	 * @return
	 */
	public boolean isParentOf(PDGParameterNode p){
		boolean retval = false;
		for(int i = 0;i<values.length;i++){
			if(this.succ(p.getPDGVariableNode(), i) == p){
				retval = true;
				break;
			}
		}
		return retval;
	}
	
	public Vector<PDGParameterNode> getParentsCopy(){
		return new Vector<PDGParameterNode>(parents);
	}
	
	/**
	 * This method creates and returns a (shallow) copy of this PDGParameterNode. 
	 * The copy will be contained in the same PDGVariableNode, will have the same values,
	 * and will have the same children for the corresponding values. <b>IMPORTANT:</b> 
	 * The copy is born an orphan, that is, the copy will initially be without parents. This means that
	 * the PDG in which this PDGParameterNode has been copied will be temporarily syntactically inconsistent. 
	 * This has been decided
	 * because there is no obvious way to initialise parents that yields in a syntactically consistent structure. 
	 * Hence, it is important that atleast one parent is added to the copy by the caller of this method. This
	 * should be done by calling the {@code setSuccesspor} method on the parent PDGParameterNode with the copy
	 * as argument.
	 * 
	 * 
	 * @return PDGParameterNode - a copy of this node
	 */
	public PDGParameterNode shallowCopy(){
		PDGParameterNode clone = new PDGParameterNode(this.pdgVarNode);
		try {
			clone.setValues(this.values);
		} catch (StateNumberException e) {
			System.out.println("This should not happen! while making a copy a parameternode provided a wrong number of states for the copy of itself!!");
			e.printStackTrace();
			System.exit(112);
		}
		for(PDGVariableNode succVar : pdgVarNode.getSuccessors()){
			int succId = pdgVarNode.getSuccessorIndex(succVar);
			for(int state=0;state<values.length;state++){
				clone.setSuccessor(children[succId][state], succId, state);
			}
		}
		return clone;
	}
	
	/**
	 * This method signals to this PDGParameterNode that it may need to update
	 * its parents vector as one parent (parameter PDGParameterNode parent) has 
	 * removed an incoming link. It is a safe method, which means that it will always 
	 * leave the PDG structure as a consistent PDG structure (given that it was consistent
	 * prior to invoking safeRemoveParent). 
	 * 
	 *  Please note: parent.isParentOf(this, this.pdgVarNode) is used to check whether to 
	 *  modify the parents Vector of this PDGParameterNode, and hence it is not necessary
	 *  to check this in the caller.
	 * 
	 * @param parent - the parent that made a change in its children involving 
	 * removing an incomming edge from this PDGParameterNode
	 */
	public void safeRemoveParent(PDGParameterNode parent){
		if(parent.isParentOf(this)) return;
		unsafeRemoveParent(parent);
	}
	
	private void unsafeRemoveParent(PDGParameterNode parent){
		parents.remove(parent);
		if(parents.size() == 0){
			/**
			 * this node has become unreachable and we therefore remove it
			 */
			pdgVarNode.unsafeRemoveParameterNode(this);
			for(int i=0;i<children.length; i++){
				for(int j=0;j<children[i].length;j++){
					children[i][j].unsafeRemoveParent(this);
					//children[i][j] = null;
				}
			}
		}
	}
	
	/**
	 * 
	 * This method selects from the cases reaching this PDGParameterNode, those
	 * cases in which the variable that this PDGParameterNode reprents assumes
	 * state value. 
	 * 
	 * @param value
	 * @return CaseList - the result of the query.
	 */
	public CaseListMem selectFromReach(int value){
		return CasesOps.selectFromWhere(reach, this.pdgVarNode.getVar(), value);
	}
	
	public void updateReach(CaseListMem newReach){
		if(this.reach == null){
			this.reach = new CaseListMem(newReach.getVariables());
		}
		this.reach.appendCases(newReach);
	}
	
	public PDGVariableNode getPDGVariableNode(){
		return pdgVarNode;
	}
	
	public void updateReachCountsAndParameters(CaseListMem newReach, boolean smooth){
		if(this.reach == null) this.reach = new CaseListMem(newReach.getVariables());
		double[] newCount = CasesOps.getMarginalCounts(newReach, this.pdgVarNode.getVar());
		for(int i=0;i<this.count.length; i++){
			count[i]+=newCount[i];
		}
		this.updateValuesFromCount(smooth);
		this.reach.appendCases(newReach);
	}
	
	void clearReach(Vector<FiniteStates> vars){
		this.reach = new CaseListMem(vars);
		clearCounts();
	}

	void clearReachAndCounts(){
		this.reach = new CaseListMem(this.reach.getVariables());
		clearCounts();
	}

	void propagateReachOneLevel(){
		for(int i=0;i<children.length; i++){
			for(int j=0;j<values.length;j++){
				try{
					children[i][j].updateReach(CasesOps.selectFromWhere(this.reach, this.pdgVarNode.getFiniteStates(), j));
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public void safeMerge(PDGParameterNode p, boolean smooth) throws PDGException, VectorOpsException{
		safeMerge(p, false, smooth);
	}
	
	public void safeMerge(PDGParameterNode p, boolean updateReach, boolean smooth) throws PDGException {
		if(this.equals(p)) throw new PDGException("can not merge PDGParameterNode with itself");
		this.updateReachCountsAndParameters(p.reach, smooth);
		// Note :
		// the following copy of the parents vector of p is necessary
		// becourse redirectAllChildConnections will modify the original
		// parents vector and it can therefor not be used in the loop definition
		Vector<PDGParameterNode> tmpParents = new Vector<PDGParameterNode>(p.parents);
		for(PDGParameterNode parent : tmpParents)
			parent.redirectAllChildConnections(p.pdgVarNode, p, this);		
		// remove p from the collection of PDGParameterNodes representing this PDGVariable
		this.pdgVarNode.unsafeRemoveParameterNode(p);
		
		// update reach - this means clear reach for all nodes that was
		// reached from the old PDGParameterNode and update from parents
		// Note : 
		// this is potentially very expensive so we turn it off by default
		if(updateReach){
			Vector<PDGParameterNode> processed = new Vector<PDGParameterNode>();
			for(int i=0;i<values.length;i++){
				for(PDGVariableNode y : this.pdgVarNode.getSuccessors()){
					PDGParameterNode pchild = p.succ(y, i);
					if(processed.contains(pchild)) continue;
					pchild.updateEverythingFromParents(smooth);
					processed.add(pchild);
				}
			}
		}
		
	}
	
	private void updateEverythingFromParents(boolean smooth){
		this.clearReach(this.reach.getVariables());
		for(PDGParameterNode p : parents){
			CaseListMem cases = p.getCasesReachingChild(this);
			this.reach.appendCases(cases);
		}
		this.updateValuesFromReach(smooth);
	}
	
	private CaseListMem getCasesReachingChild(PDGParameterNode ch){
		CaseListMem cases = new CaseListMem(this.reach.getVariables());
		int varSuccIdx = this.pdgVarNode.getSuccessorIndex(ch.pdgVarNode);
		for(int i=0;i<this.values.length;i++){
			if(children[varSuccIdx][i].equals(ch)){
				cases.appendCases(this.selectFromReach(i));
			}
		}
		return cases;
	}
	
	private void redirectAllChildConnections(PDGVariableNode varSucc, PDGParameterNode oldChild, PDGParameterNode newChild){
		for(int i = 0; i<values.length; i++){
			if(this.succ(varSucc, i).equals(oldChild)){
				this.setSuccessor(newChild, varSucc, i);
			}
		}
		oldChild.unsafeRemoveParent(this);
	}
	
	/**
	 * Returns a copy of this PDGParameterNode. Children and values are copied, but
	 * parents can not be copied if we wish the PDG including the copy of this PDGParameterNode to be 
	 * a correct PDG structure. Therefore, parents are created and will contain the PDGParameterNodes 
	 * in parents parameter.
	 * 
	 * @param cpVarNode - the PDGVariableNode that should contain the copy of this PDGParameterNode
	 * @param newParents - the PDGParameterNodes that should be the parents of the copy of this node
	 * @return
	 */
	PDGParameterNode copyNodeKeepChildren(PDGVariableNode cpVarNode, final Vector<PDGParameterNode> newParents){
		PDGParameterNode cpParNode = new PDGParameterNode(cpVarNode);
		cpParNode.parents.addAll(newParents);
		int numSuccs = cpVarNode.getSuccessors().size();
		for(int h = 0; h<cpParNode.values.length;h++){
			cpParNode.values[h] = values[h];
			for(int l=0;l<numSuccs;l++){
				cpParNode.children[l][h] = children[l][h];
			}
		}
		cpParNode.parents.addAll(newParents);
		return cpParNode;
	}
	
	/**
	 * 
	 * Makes a copy of this PDGParameterNode without keeping children, that is all children
	 * of the returned PDGParameterNode will be null and must be set elswhere to obtain a 
	 * valid PDG structure. Also parents will be empty. The values array is copied to the
	 * new PDGParameterNode.
	 * 
	 * @param var - the PDGVariableNode for the new PDGParameterNode.
	 * @return
	 */
	PDGParameterNode copyNodeOnly(PDGVariableNode var){
		PDGParameterNode newNode = new PDGParameterNode(var);
		for(int h=0;h<newNode.values.length;h++) newNode.values[h] = this.values[h];
		return newNode;
	}
	
	/**
	 * 
	 */
	void computeOutFlow(){
		outFlow = 0.0;
		PDGParameterNode succParameterNode;
		if(children.length > 0){
			for(int h=0;h<values.length;h++){
				this.pi[h] = 1.0;
				Vector<PDGVariableNode> varSucc = pdgVarNode.getSuccessors();
				for(PDGVariableNode y : varSucc){
					succParameterNode = succ(y,h);
					if(!succParameterNode.oflIsComputed) succParameterNode.computeOutFlow();
					pi[h] *= succParameterNode.outFlow;
				}
				outFlow += pdgVarNode.evidence[h]*values[h]*pi[h];
			}
		} else {
			for(int h=0;h<values.length;h++){
				outFlow += pdgVarNode.evidence[h]*values[h];
			}
		}
		//System.out.println("outflow("+id+")\t = "+outFlow);
		this.oflIsComputed = true;
	}
	
	void computeIfl(){
		this.inFlow = 0.0;
		PDGVariableNode predr = pdgVarNode.predecessor();
		for(int h=0;h<predr.getNumStates();h++){
			//if(predr.evidence[h] == 0.0) continue;
			for(PDGParameterNode par : parents){
				if(par.succ(this.pdgVarNode, h).equals(this)){
					if(outFlow != 0.0){
						inFlow += par.inFlow * predr.evidence[h] * par.values[h] * (par.pi[h] / outFlow);
					} else {
						inFlow += par.inFlow * predr.evidence[h] * par.values[h] * (par.calcProdOflExpl(pdgVarNode, h));
					}
				}
			}
		}
		//System.out.println("inflow("+id+")\t = "+inFlow);
	}

	private double calcProdOflExpl(PDGVariableNode toExclude, int value){
		double prod = 1.0;
		int idx = pdgVarNode.getSuccessors().indexOf(toExclude);
		for(int i=0; i<values.length;i++){
			if(i==value) continue;
			prod *= children[idx][i].outFlow;
		}
		return prod;
	}

	/**
	 * This method returns a reference to the double array holding the values of
	 * this PDGParameterNode, and <b><i>not</i></b> just a reference to a copy of the array. So 
	 * you can directly modify the values as you wish.
	 * 
	 * @return double[] - the values (or parameters) defined by this PDGParameterNode.
	 * 						
	 */
	public double[] getValues() {
		return values;
	}

	/**
	 * This method sets the values of this PDGParameterNode to the ones provided in the
	 * argument. That is, the values are copied and the argument array v is left untouched.
	 * 
	 * @param v  - the new values for this PDGParameterNode
	 * @throws StateNumberException - when the provided array of values has a wrong number of entries.
	 */
	public void setValues(double[] v) throws StateNumberException {
		if(v.length != values.length){
			throw new StateNumberException("Trying to set wrong number of states"+
					" - was givin '"+v.length+"' but needs '"+values.length+"'");
		}
		for(int i=0;i<v.length;i++)
			this.values[i] = v[i];
	}

	PDGVariableNode getVarNode(){
		return pdgVarNode;
	}
	
	void unsafeSetAllChildren(PDGParameterNode ch[][]){
		this.children = new PDGParameterNode[ch.length][];
		for(int succ = 0; succ < ch.length;succ++){
			this.children[succ] = new PDGParameterNode[ch[succ].length];
			for(int state = 0; state < ch[succ].length;state++){
				this.children[succ][state] = ch[succ][state];
				if(!children[succ][state].parents.contains(this)) children[succ][state].parents.add(this);	
			}
		}
	}
	
	public void setAllChildren(PDGParameterNode[][] ch) throws PDGException {
		if(ch.length != children.length) 
			throw new PDGException("PDGParameteNode.setAllChildren called with argument of lenght: "+ch.length+"\n" +
					               "where this.children.length                   == "+children.length+"\n" +
					               "and this.pdgVarNode.getSuccessors().size()   == "+this.pdgVarNode.getSuccessors().size());
		for(int i=0;i<ch.length;i++){
			if(ch[i].length != children[i].length) throw new PDGException("Wrong number ("+ch[i].length+") of successor pdgnodes (need "+children[i].length+")");
			for(int j=0;j<ch[i].length;j++){
				children[i][j] = ch[i][j];
				if(!children[i][j].parents.contains(this)) children[i][j].parents.add(this);
			}		
		}
	}

	void resetCounts(){
		for(int i=0;i<count.length;i++)
			count[i]=0;
	}

	/**
	 * 
	 * This method sets the child PDGParameterNode for this PDGParameterNode for variable successor
	 * varSucc and state h to succ. In addition, the parents array of succ is updated to include
	 * this PDGParameterNode.
	 * 
	 * @param succ
	 * @param varSucc
	 * @param state
	 */
	public void setSuccessor(PDGParameterNode succ, PDGVariableNode varSucc, int state){
		int succId = pdgVarNode.getSuccessorIndex(varSucc);
		if(succId == -1){
			System.out.println("Could not set successor parameter-node as variable "+this.pdgVarNode.getName()+" does not include "+varSucc.getName()+" successor!!!");
			System.exit(112);
		}
		setSuccessor(succ, succId, state);
	}

	public void setSuccessor(PDGParameterNode succ, int state){
		setSuccessor(succ, succ.getPDGVariableNode(), state);
	}
	
	/**
	 * 
	 * This method sets the child PDGParameterNode for this PDGParameterNode for variable successor
	 * varSucc and state h to succ. In addition, the parents array of succ is updated to include
	 * this PDGParameterNode.
	 * 
	 * @param succ - the new successor PDGParameterNode
	 * @param succIdx - the index of the PDGVariableNode of the succ PDGParameterNode
	 * @param state - the state for which the edge has endpoint in the succ PDGParameterNode
	 */
	void setSuccessor(PDGParameterNode succ, int succIdx, int state){
		if(children.length == 0){}
		children[succIdx][state] = succ;
		if(!succ.parents.contains(this)) succ.parents.add(this);
	}
	
	void variableSuccessorAdded(int newIdx){
		PDGParameterNode[][] newChildren = new PDGParameterNode[children.length+1][pdgVarNode.getNumStates()];
		for(int i=0;i<newIdx;i++){
			newChildren[i] = children[i];
		}
		newChildren[newIdx] = new PDGParameterNode[pdgVarNode.getNumStates()];
		for(int i=newIdx; i< children.length;i++){
			newChildren[i+1] = children[i];
		}
		children = newChildren;
	}
	
	void countConfiguration(Configuration conf){
		int state = conf.getValue(pdgVarNode.getVar());
		count[state]++;
		for(int i=0;i<children.length; i++){
			children[i][state].countConfiguration(conf);
		}
	}
	
	void generateSample(Configuration conf, Random rnd){
		double coin = rnd.nextDouble();
		double accumulated = 0.0;
		int h;
		for(h=0;h<values.length;h++){
			accumulated += values[h];
			if(coin < accumulated){
				break;
			}
		}
		conf.insert(pdgVarNode.getVar(), h);
		for(int i=0;i<children.length;i++)
			children[i][h].generateSample(conf, rnd);
	}
	
	void recomputeCounts(){
		for(int i=0;i<count.length;i++)
			count[i] = 0.0;
		for(int i=0;i<reach.getNumberOfCases();i++)
			count[reach.get(i).getValue(this.pdgVarNode.getFiniteStates())]++;
	}
	
	void updateValues(boolean smooth){
		double correction = 0.0;
		if(smooth) correction = 1.0;
		for(int i=0;i<count.length;i++)
			values[i] = count[i] + correction;
		VectorOps.normalise(values);
	}
	
	public void initializeReach(NodeList variables){
		reach = new CaseListMem(variables);
	}
	
	void initializeReach(Vector<Node> variables){
		reach = new CaseListMem(variables);
	}
	
	public CaseListMem getReach(){
		return reach;
	}
	
	void smoothValues(double smoothingValue){
		for(int i=0;i<values.length;i++){
			values[i] += smoothingValue;
		}
		VectorOps.normalise(values);
	}
	
	public class StateNumberException extends PDGException{

		public StateNumberException(String msg) {
			super(msg);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -8093143025498178752L;
		
	}
	
	public boolean testParentConnections(){
		if(parents.size() == 0) return (pdgVarNode.predecessor() == null);
		if(VectorOps.containsDuplicates(parents)) return false;
		for(PDGParameterNode parent : parents){
			if(!(parent.isParentOf(this))) return false;
			if(!(parent.getPDGVariableNode() == pdgVarNode.predecessor())) return false;
		}
		return true;
	}
	
	public boolean testChildrenConnections(){
		boolean result = (children.length == pdgVarNode.getSuccessors().size());
		if(result){
			for(PDGVariableNode variableSuccessor : pdgVarNode.getSuccessors()){
				for(int state = 0; state < values.length; state++){
					if(!(this.succ(variableSuccessor, state).getPDGVariableNode() == variableSuccessor))
						return false;
				}
			}
		}
		return result;
	}
	
	public boolean testStructure(){
		return testParentConnections() && testChildrenConnections();
	}
}
