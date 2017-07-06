package elvira.probabilisticDecisionGraph;

import elvira.probabilisticDecisionGraph.tools.*;
import elvira.*;
import java.util.*;

/**
 * 
 * This class implements the Variable Node in PDG model.
 * All variables 
 * 
 * @author dalgaard
 * @since 07/02/07
 */
public class PDGVariableNode{

	private final Vector<PDGParameterNode> parameterNodes = new Vector<PDGParameterNode>();
	
	private FiniteStates var;
	final private Vector<PDGVariableNode> successors = new Vector<PDGVariableNode>();
	private PDGVariableNode predecessor = null;
	double[] evidence;
	
	public PDGVariableNode(FiniteStates v){
		var = v;
		evidence = VectorOps.getNewDoubleArray(v.getNumStates(), 1.0);
	}

	public PDGVariableNode(FiniteStates v, Vector<PDGVariableNode> ch){
		var = v;
		successors.addAll(ch);
	}

	public PDGVariableNode(FiniteStates v, 
			Vector<PDGVariableNode> ch, 
			PDGVariableNode par){
		var = v;
		successors.addAll(ch);
		predecessor = par;
	}
	
	public String getName(){
		return var.getName();
	}
	
	void addParameterNode(PDGParameterNode p){
		parameterNodes.add(p);
	}
	
	PDGParameterNode getParameterRootNode(){
		PDGParameterNode retval = null;
		if(predecessor == null && parameterNodes.size() == 1){
			retval = parameterNodes.elementAt(0);
		}
		return retval;
	}
	
	public int getIndexOf(PDGParameterNode p){
		return parameterNodes.indexOf(p);
	}
	
	public Vector<PDGVariableNode> getSuccessors(){
		return successors;
	}
	
	void addSuccessors(Vector<PDGVariableNode> succs){
		successors.addAll(succs);
		for(PDGVariableNode c : succs) 
			c.predecessor = this;
	}
	
	PDGVariableNode findNodeByName(String str){
		PDGVariableNode retval = null;
		if(this.var.getName().compareToIgnoreCase(str) == 0){
			retval = this;
		} else {
			for(PDGVariableNode p : successors){
				if((retval = p.findNodeByName(str)) != null) break;
			}
		}
		return retval;
	}
	
	void insertEvidence(int state) throws PDGIncompatibleEvidenceException{
		if(state < 0 || evidence.length < state )
			throw new PDGIncompatibleEvidenceException("state number '"+state+"' out of range for variable '"+var.getName()+"'");
		for(int i=0;i<evidence.length; i++)
			evidence[i] = (i==state ? 1.0 : 0.0);
	}
	
	int getSuccessorIndex(PDGVariableNode succ){
		return successors.indexOf(succ);
	}
	
	void removeEvidence(){
		this.evidence = VectorOps.getNewDoubleArray(evidence.length, 1.0);
		for(PDGVariableNode s : successors) s.removeEvidence();
	}
	
	void compileNodeList(NodeList nl){
		nl.insertNode(this.var);
		for(PDGVariableNode p : successors){
			p.compileNodeList(nl);
		}
	}
	
	FiniteStates getVar(){
		return var;
	}
	
	PDGVariableNode copy(PDGVariableNode predecessorOfCopy){
		PDGVariableNode cp = new PDGVariableNode(var);
		cp.predecessor = predecessorOfCopy;
		
		// First copy the variable tree structure ..
		for(PDGVariableNode y : successors)
			cp.successors.add(y.copy(cp));
		
		// .. then copy the graph-structure over PDGParameterNodes.
		// This will happen bottom up and we can safely assume that 
		// PDGParameterNodes of successor PDGVariableNodes are initialised
		// correctly.
		for(PDGParameterNode p : this.parameterNodes){
			// copy the node PDGParameterNode ...
			PDGParameterNode newParameterNode = p.copyNodeOnly(cp);
			newParameterNode.updateReach(p.getReach());
			int idx;
			// .. and set the connections
			for(int i=0;i<this.successors.size();i++){
				for(int h=0; h<this.var.getNumStates();h++){
					PDGVariableNode thisVarSucc = this.successors.elementAt(i);
					PDGVariableNode cpVarSucc = cp.successors.elementAt(i);
					PDGParameterNode thisParSucc = p.succ(thisVarSucc, h);
					idx = thisVarSucc.parameterNodes.indexOf(thisParSucc);
					PDGParameterNode newParSucc = cpVarSucc.parameterNodes.elementAt(idx);
					newParameterNode.setSuccessor(newParSucc, cpVarSucc, h);
				}
			}
		}
		return cp;
	}
	
	int countVariables(){
		int c = 1;
		for(PDGVariableNode y : successors)
			c += y.countVariables();
		return c;
	}
	
	public boolean checkReach(int max){
		boolean res = true;
		for(PDGParameterNode p : parameterNodes){
			if(p.getReach().getNumberOfCases() > max){
				System.out.println("too many cases in reach for PDGParameterNode "+p.id+" in PDGVariableNode "+this.getName()+" - we have "+p.getReach().getNumberOfCases()+" while max is "+max);
				res = false;
			}
		}
		for(PDGVariableNode p : successors){
			res &= p.checkReach(max);
		}
		return res;
	}
	
	int countNodes(){
		int c = parameterNodes.size();
		for(PDGVariableNode y : successors)
			c += y.countNodes();
		return c;
	}
	
	long countIndependentParameters(){
		long c = parameterNodes.size() * (var.getNumStates() - 1);
		for(PDGVariableNode y : successors)
			c += y.countIndependentParameters();
		return c;
	}
	
	void getNodes(Vector<Node> v){
		v.add(var);
		for(PDGVariableNode s : successors){
			s.getNodes(v);
		}
	}
	
	boolean insertMultipleEvidence(Configuration conf) throws PDGIncompatibleEvidenceException {
		int state = conf.getValue(var);
		if(state == -1 ) state = conf.getValue(var.getName());
		boolean retval = (state != -1);
		if(state != -1){
			insertEvidence(state);
		}
		for(PDGVariableNode p : successors){
			retval &= p.insertMultipleEvidence(conf);
		}
		return retval;
	}
	
	/**
	 * 
	 * @return the number of states for the variable represented by this PDGVariableNode
	 */
	public int getNumStates(){
		return var.getNumStates();
	}

	public PDGVariableNode predecessor(){
		return predecessor;
	}

	PDGParameterNode getPNodeById(int i){
		PDGParameterNode p = null;
		for(PDGParameterNode node : parameterNodes){
			if(node.id == i){
				p=node;
				break;
			}
		}
		return p;
	}
	
	public FiniteStates getFiniteStates(){
		return var;
	}
	
	PDGVariableNode findPDGVariableNodeByElviraNode(final Node n){
		PDGVariableNode retval = null;
		if(n.equals(var)){
			retval = this;
		} else {
			for(PDGVariableNode s : successors){
				retval = s.findPDGVariableNodeByElviraNode(n);
				if(retval != null)
					break;
			}
		}
		return retval;
	}
	
	public void printNames(){
		System.out.println(var.getName());
		for(PDGVariableNode p : successors){
			p.printNames();
		}
	}
	
	void unsafeRemoveParameterNode(PDGParameterNode p){
		parameterNodes.remove(p);
	}
	
	/**
	 * This method adds the newSucc PDGVariableNode as a new successor
	 * of this PDGVariableNode. 
	 * 
	 * @param newSucc
	 * @return
	 */
	public int addSuccessor(PDGVariableNode newSucc){
		/**
		 * TODO : this method should probably construct
		 * some default connections between PDGParameterNodes, 
		 * as it is a public method! Then we should make children array
		 * private and unavailable to any other class. 
		 * */
		
		int idx = successors.indexOf(newSucc);
		if(idx == -1){
			successors.add(newSucc);
			idx = successors.indexOf(newSucc);
			for(PDGParameterNode pn : parameterNodes){
				pn.variableSuccessorAdded(idx);
			}
		}
		newSucc.predecessor = this;
		return idx;
	}
	
	/**
	 * 
	 * This method should be used to add a fully expanded PDGVariableNode
	 * as a new successor of this PDGVariableNode. A fully expanded PDGVariableNode
	 * contains a unique PDGParameterNode for each PDGParameterNode and each value pair
	 * of this PDGVariableNode.
	 * 
	 * The newSucc PDGVariableNode should not contain any PDGParameterNodes, if it
	 * does they are removed. New PDGParameterNodes are created and connected 
	 * appropriately.
	 * 
	 * Cases are added to reach, and parameters will be exstimated for the new PDGParameterNodes.
	 * 
	 * @param newSucc - the new PDGVariableNode to add
	 * @return the index assigned to the newSucc PDGVariableNode.
	 */
	public int addFullyExpandedSuccessor(PDGVariableNode newSucc){
		int idx = successors.indexOf(newSucc);
		if(idx == -1){
			successors.add(newSucc);
			newSucc.predecessor = this;
			idx = successors.indexOf(newSucc);
			newSucc.parameterNodes.clear();
			for(PDGParameterNode p : parameterNodes){
				p.variableSuccessorAdded(idx);
				for(int h = 0;h<this.var.getNumStates();h++){
					PDGParameterNode succp = new PDGParameterNode(newSucc);
					p.setSuccessor(succp, idx, h);
					succp.initializeReach(p.getReach().getVariables());
					succp.updateReach(CasesOps.selectFromWhere(p.getReach(), this.var, h));
					succp.updateValuesFromReach(false);
				}
			}
		}
		return idx;
	}
	
	void clearReachAndCounts(){
		for(PDGParameterNode p : parameterNodes)
			p.clearReachAndCounts();
		for(PDGVariableNode pv : successors)
			pv.clearReachAndCounts();
	}

	void clearReach(Vector<FiniteStates> vars) throws PDGException {
		for(PDGParameterNode p : parameterNodes)
			p.clearReach(vars);
		for(PDGVariableNode pv : successors)
			pv.clearReach(vars);
	}

	
	void reComputeReach(CaseListMem data) throws PDGException{
		if(this.predecessor != null) throw new PDGException("updateReach invoked on non-root PDGVariableNode");
		clearReach(data.getVariables());
		parameterNodes.elementAt(0).updateReach(data);
		this.propagateReach();
	}

	void reComputeReachCountsAndParameters(CaseListMem data, boolean smooth) throws PDGException{
		if(this.predecessor != null) throw new PDGException("updateReach invoked on non-root PDGVariableNode");
		clearReachAndCounts();
		parameterNodes.elementAt(0).updateReachCountsAndParameters(data, smooth);
		this.propagateReach();
	}

	
	void propagateReach(){
		for(PDGParameterNode p : parameterNodes)
			p.propagateReachOneLevel();
		for(PDGVariableNode pv : successors)
			pv.propagateReach();
	}
	
	protected void getDepthFirstStack(Stack<PDGVariableNode> stack){
		stack.push(this);
		for(PDGVariableNode p : successors)
			p.getDepthFirstStack(stack);
	}
	
	public PDGParameterNode getMaxReachNode(){
		PDGParameterNode retval = parameterNodes.elementAt(0);
		for(PDGParameterNode p : parameterNodes){
			if(p.getReach().getNumberOfCases() > retval.getReach().getNumberOfCases())
				retval = p;
		}
		return retval;
	}
	
	public int getNumberOfParameterNodes(){
		return parameterNodes.size();
	}
	
	public Vector<PDGParameterNode> getParameterNodesCopy(){
		return new Vector<PDGParameterNode>(parameterNodes);
	}

	public PDGParameterNode getParameterNodeByVectorIndex(int i){
		return parameterNodes.elementAt(i);
	}
	
	int getVectorIndexOf(PDGParameterNode p){
		return parameterNodes.indexOf(p);
	}
	
	//void setParameterNodes(Vector<PDGParameterNode> nodes){
	//	parameterNodes = nodes;
	//}
	
	void propagateIfl(double productOutFlows) throws PDGException {
		if(this.predecessor == null){
			//this is a root
			parameterNodes.elementAt(0).inFlow = productOutFlows;
			for(PDGVariableNode y : successors)	y.propagateIfl();
		} else {
			throw new PDGException("propagateIfl(double) called on non-root PDGVariableNode!!");
		}
	}
	
	void propagateIfl(){
		for(PDGParameterNode p : parameterNodes){
			p.computeIfl();
		}
		for(PDGVariableNode y : successors){
			y.propagateIfl();
		}
	}
	
	/**
	 * Computes the outFlow of all PDGParamteterNodes associated whith this 
	 * PDGVariabelNode and all PDGParameterNodes in the subtree rooted at this 
	 * PDGVariableNode. It only makes sense to call this on the root of a variabele
	 * tree.
	 * 
	 * @throws PDGException if this PDGVariableNode is not a root
	 */
	void computeOutFlow() throws PDGException{
		if(this.predecessor != null){ throw new PDGException("Out-flow computation initiated on the variable level at internal node!!"); }
		resetOflIsComputed();
		for(PDGParameterNode p : parameterNodes){
			p.computeOutFlow();
		}
	}
	
	int effectiveSize(){
		int es = 0;
		for(PDGVariableNode y : successors) es += y.effectiveSize();
		es += (successors.size() == 0 ? parameterNodes.size() : parameterNodes.size()*successors.size());
		return es;
	}
	
	void clearCounts(){
		for(PDGParameterNode p : parameterNodes)
			p.clearCounts();
		for(PDGVariableNode y : successors)
			y.clearCounts();
	}
	
	double[] getMarginal(){
		double[] marginal = new double[var.getNumStates()];
		for(int h=0;h<var.getNumStates();h++){
			marginal[h] = 0.0;
			for(PDGParameterNode p : parameterNodes){
				double succOflProd = 1.0;
				for(PDGVariableNode y : successors){
					succOflProd *= p.succ(y, h).outFlow;
				}
				marginal[h] += p.inFlow * p.getValues()[h] * evidence[h] * succOflProd;
			}
		}
		VectorOps.normalise(marginal);
		return marginal;
	}
	
	private void resetOflIsComputed(){
		for(PDGParameterNode p : parameterNodes) p.oflIsComputed = false;
		for(PDGVariableNode y : successors) y.resetOflIsComputed();
	}
	
	void toString(StringBuilder vars, StringBuilder struct){
		vars.append("\tnode "+var.getName()+"("+var.getType()+"){\n");
		vars.append("\t\ttitle=\""+var.getTitle()+"\";\n");
		vars.append("\t\tstates=(");
		for(String str : (Vector<String>)var.getStates()){
			if(!str.startsWith("\""))
				vars.append("\""+str+"\" ");
			else
				vars.append(""+str+" ");
		}
		vars.append(");\n");
		for(PDGParameterNode pn : parameterNodes){
			vars.append("\t\tpnode{\n");
			vars.append("\t\t\tid="+pn.id+";\n");
			vars.append("\t\t\tvalues=table("+VectorOps.doubleArrayToSimpleString(pn.getValues())+");\n");
			vars.append("\t\t}\n");
		}
		vars.append("\t}\n\n");
		
		if(successors.size() != 0){
			struct.append("\t\t"+var.getName()+" -> (");
			for(PDGVariableNode s : successors) 
				struct.append(s.getName()+" ");
			struct.append("){\n");
			for(PDGParameterNode pdgn : parameterNodes){
				struct.append("\t\t\t"+pdgn.id+" -> ");
				for(PDGVariableNode y : successors){
					struct.append("(");
					for(int h=0;h<var.getNumStates();h++){
						struct.append(pdgn.succ(y, h).id+" ");
					}
					struct.append(") ");
				}
				struct.append(";\n");
			}
			struct.append("\t\t}\n");
			for(PDGVariableNode s : successors){
				s.toString(vars, struct);
			}
		}
	}

	void synchronizeVariables(Vector<Node> v){
		for(Node n : v){
			if(n.getName().compareTo(var.getName()) == 0){
				var = (FiniteStates)n;
				break;
			}
		}
		for(PDGVariableNode s : successors)
			s.synchronizeVariables(v);
	}
	
	void updateValues(boolean smooth){
		updateValues(smooth, false);
	}
	
	void updateValues(boolean smooth, boolean recomputeCounts){
		for(PDGParameterNode p : parameterNodes){
			if(recomputeCounts) p.recomputeCounts();
			p.updateValues(smooth);
		}
		for(PDGVariableNode s : successors)
			s.updateValues(smooth, recomputeCounts);
	}
	
	void getVarNodesAboveDepth(Vector<PDGVariableNode> varNodes, int depth){
		varNodes.add(this);
		if(depth > 0){
			for(PDGVariableNode y : successors){
				y.getVarNodesAboveDepth(varNodes, depth - 1);
			}
		}
	}
	
	public Vector<CaseList> getPartitions(){
		Vector<CaseList> partitions = new Vector<CaseList>();
		for(PDGParameterNode p : parameterNodes)
			partitions.add(p.getReach());
		return partitions;
	}
	
	int depth(){
		int childdepth, maxcd = 0, thisdepth = 0;
		if(successors.size() != 0) thisdepth = 1; 
		for(PDGVariableNode p : successors){
			childdepth = p.depth();
			maxcd = (childdepth > maxcd ? childdepth : maxcd);
		}
		return thisdepth + maxcd;
	}
	
	int branching(){
		return successors.size();
	}
	
	int maxbranching(){
		int maxbr = this.branching(), chbr;
		for(PDGVariableNode p : successors){
			chbr = p.maxbranching();
			maxbr = (chbr > maxbr ? chbr : maxbr);
		}
		return maxbr;
	}
	public String successorsToString(){
		String result = "[";
		for(PDGVariableNode n : successors){
			result += n.getName()+" ";
		}
		return result+"]";
	}

	public int getDepthFromRoot(){
		return (predecessor == null ? 0 : 1 + predecessor.getDepthFromRoot());
	}
	
	//void addPseudoCount(double pc){
	//	for(PDGParameterNode p : parameterNodes)
	//		p.addPseudoCount(pc);
	//}

	void initializeReach(NodeList nl){
		for(PDGParameterNode p : parameterNodes){
			p.initializeReach(nl);
		}
		for(PDGVariableNode succ : successors){
			succ.initializeReach(nl);
		}
	}

	public boolean containsSuccessor(PDGVariableNode X){
		return successors.contains(X);
	}
	
	public boolean testPredecessor(){
		return (predecessor != null ? predecessor.getSuccessors().contains(this) : true);
	}
	
	public boolean testSuccessors(){
		boolean result = true;
		if(successors.size() != 0){
			result = !VectorOps.containsDuplicates(successors);
			if(result){
				for(PDGVariableNode succ : successors) if(!(result = succ.predecessor() == this)) break;
			}
		}
		return result;
	}
	
	public boolean testStructure(){
		if(!(testPredecessor() && testSuccessors())) return false;
		for(PDGParameterNode node : parameterNodes){
			if(!node.testStructure()) return false;
		}
		for(PDGVariableNode variableSuccessor : successors){
			if(!variableSuccessor.testStructure()) return false;
		}
		return true;
	}
}