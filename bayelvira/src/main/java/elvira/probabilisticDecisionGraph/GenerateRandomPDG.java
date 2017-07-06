/**
 * Created by dalgaard on May 31, 2007
 */
package elvira.probabilisticDecisionGraph;

import java.io.IOException;
import java.util.*;

import elvira.FiniteStates;
import elvira.probabilisticDecisionGraph.PDGParameterNode.StateNumberException;
import elvira.probabilisticDecisionGraph.tools.*;

/**
 * This class implements procedures for generating 
 * PDG models.
 * 
 * @author dalgaard
 *
 */
public class GenerateRandomPDG {

	private static Random rnd = new Random(System.currentTimeMillis());

	public static void setRandomSeed(long seed){
		rnd.setSeed(seed);
	}

	private static Vector<FiniteStates> generateVariables(int numVars, int nextId, int minCard, int maxCard){
		Vector<FiniteStates> vars = new Vector<FiniteStates>();
		for(int i=0;i<numVars;i++){
			int idx = nextId + i;
			String name = "X"+idx;
			int card = minCard + rnd.nextInt(maxCard - minCard);
			Vector<String> fv = new Vector<String>(card);
			for(int j=0;j<card;j++) fv.addElement("X"+i+"_"+j);
			FiniteStates v = new FiniteStates(name, fv);
			v.setTitle(name);
			vars.add(v);
		}
		return vars;
	}

	
	private static PDGVariableNode generateRandomVarialbeTree(Vector<FiniteStates> vars, double treeBranchingFactor){
		Iterator<FiniteStates> itr = vars.iterator();
		PDGVariableNode root, currentRootNode, nextVarNode;
		Vector<PDGVariableNode> pending = new Vector<PDGVariableNode>();
		root = new PDGVariableNode(itr.next(), new Vector<PDGVariableNode>(), null);
		currentRootNode = root;
		while(itr.hasNext()){
			nextVarNode = new PDGVariableNode(itr.next(), new Vector<PDGVariableNode>(), currentRootNode);
			currentRootNode.addSuccessor(nextVarNode);
			pending.add(nextVarNode);
			if(rnd.nextDouble() < treeBranchingFactor){
				currentRootNode = pending.remove(rnd.nextInt(pending.size()));
			}
		}
		return root;
	}

	private static void createRandomParameterNodesRecursively(PDGVariableNode v){
		int numNodes = 1;		
		if(v.predecessor() != null){
			int maxNodes = v.predecessor().getNumberOfParameterNodes() * v.predecessor().getNumStates();
			numNodes = (maxNodes/2+1) + rnd.nextInt(maxNodes/2);
		}
		for(int i = 0;i<numNodes;i++){
			PDGParameterNode pnode = new PDGParameterNode(v);
			double[] vals = VectorOps.randomDiscreteDistribution(v.getNumStates(), rnd);
			try {
				pnode.setValues(vals);
			} catch (StateNumberException e) {
				e.printStackTrace();
				System.exit(112);
			}
		}
		for(PDGVariableNode s : v.getSuccessors())
			createRandomParameterNodesRecursively(s);
	}
	
	private static void initialiseGraphStructure(PDGVariableNode var){
		Vector<PDGParameterNode> currentParameterNodes = var.getParameterNodesCopy();
		int numVals = var.getNumStates();
		int numSuccs = var.getSuccessors().size();
		for(PDGVariableNode succ : var.getSuccessors()){
			Vector<PDGParameterNode> nodesStillNotConnected = succ.getParameterNodesCopy();
			Vector<PDGParameterNode> childVector = new Vector<PDGParameterNode>(nodesStillNotConnected);
			for(PDGParameterNode parent : currentParameterNodes){
				for(int state=0;state<numVals; state++){
					PDGParameterNode child = childVector.elementAt(rnd.nextInt(childVector.size()));
					parent.setSuccessor(child, succ, state);
					nodesStillNotConnected.remove(child);
				}
			}
			//remove nodes in the successor PDGVariableNode that has been left disconnected.
			for(PDGParameterNode p : nodesStillNotConnected){
				succ.unsafeRemoveParameterNode(p);
			}
		}
	}
	
	private static void initialiseTree(PDGVariableNode root){
		createRandomParameterNodesRecursively(root);
		Stack<PDGVariableNode> pending = new Stack<PDGVariableNode>();
		pending.addAll(root.getSuccessors());
		pending.push(root);
		while(!pending.isEmpty()){
			//TODO initialise the graph structure over the parameterNodes			
			PDGVariableNode current = pending.pop();
			initialiseGraphStructure(current);
			pending.addAll(current.getSuccessors());
		}
	}
	
	public static PDG generateRandomPDG(int numVars, int numTrees, int minCard, int maxCard, double treeBranchingFactor){
		PDG p = new PDG();
		int id = 0;
		int varsPerTree = numVars / numTrees;
		for(int i = 0; i < numTrees; i++){
			Vector<FiniteStates> vars = generateVariables(varsPerTree, id, minCard, maxCard);
			PDGVariableNode t = generateRandomVarialbeTree(vars, treeBranchingFactor);
			initialiseTree(t);
			p.addTree(t);
			id = id + varsPerTree;
		}
		return p;
	}
	
	public static void main(String[] args) throws IOException {
		if(args.length < 5){
			System.out.println("arguments are : <number of variables>\n" +
					           "                <number of trees>\n" +
					           "                <minimum cardinality>\n" +
					           "                <maximum cardinality>\n" +
					           "                <tree branching factor: [0.0,1.0]>\n" +
					           "                <name> (optional)");
		}
		int numVars = Integer.parseInt(args[0]);
		int numTrees = Integer.parseInt(args[1]);
		int minCard = Integer.parseInt(args[2]);
		int maxCard = Integer.parseInt(args[3]);
		double treeBranchingFactor = Double.parseDouble(args[4]);
		//boolean curb = Boolean.parseBoolean(args[5]);
		String name = "default";
		if(args.length >= 5){
			name = args[5];
		}
		PDG p = generateRandomPDG(numVars, numTrees, minCard, maxCard, treeBranchingFactor);
		p.setName(name);
		PDGio.save(p,p.name+".pdg");
	}
}
