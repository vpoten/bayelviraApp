package elvira.learning.classification.supervised.discrete;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

import elvira.Bnet;
import elvira.CaseList;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Graph;
import elvira.InvalidEditException;
import elvira.Link;
import elvira.LinkList;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.database.DataBaseCases;
import elvira.learning.classification.*;
import elvira.learning.classification.supervised.continuous.MaximumSpanningTree;
import elvira.potential.PotentialTable;
import elvira.probabilisticDecisionGraph.tools.CasesOps;
import elvira.probabilisticDecisionGraph.tools.Measures;

public class TreeAugmentedNaiveBayes implements SizeComparableClassifier {
	
	private Bnet classifier;
	private FiniteStates classVar = null;
	
	public long size() {
		return classifier.getNumberOfFreeParameters();
	}

	public Vector<Double> classify(Configuration instance, int classnumber) {
		if(classifier.getNodeList().size() != instance.getVariables().size()){
			System.out.println("WARNING : method classify recieved a configuration that is not full - this is probably an error!!!");
		}
		//FiniteStates classVar = instance.getVariable(classnumber);
		int numClassLabels = classVar.getNumStates();
		Vector<Double> values = new Vector<Double>(numClassLabels);
		for(int label=0;label<numClassLabels;label++){
			instance.setValue(classnumber, label);
			double p = this.classifier.evaluateFullConfiguration(instance);
			values.add(p);
		}
		return values;
	}
	
	private void completeUndirectedGraph(Graph udg){
		NodeList nl = udg.getNodeList();
		for(int i=0;i<nl.size();i++){
			Node X_i = nl.elementAt(i);
			for(int j=i+1;j<nl.size();j++){
				Node X_j = nl.elementAt(j);
				try {
					udg.createLink(X_i, X_j, false);
				} catch (InvalidEditException e) {
					e.printStackTrace();
					System.exit(112);
				}
			}
		}
	}
	
	private void repairNodesFromLinkList(){
		if(classifier != null){
			NodeList nl = classifier.getNodeList();
			LinkList ll = classifier.getLinkList();
			for(int i=0;i<nl.size();i++){
				Node n = nl.elementAt(i);
				n.setParents(new LinkList());
				n.setChildren(new LinkList());
				n.setSiblings(new LinkList());
			}
			for(int i=0;i<ll.size();i++){
				Link l = ll.elementAt(i);
				if(l.getDirected()){
					l.getTail().addChild(l);
					l.getHead().addParent(l);
				} else {
					l.getHead().addNeighbour(l.getTail());
					l.getTail().addNeighbour(l.getHead());
				}
			}
		}
	}
	
	private void directTree(Graph tree, Node root) {
		Stack<Node> pending = new Stack<Node>();
		pending.push(root);
		Vector<Link> undirectedLinks = new Vector<Link>(tree.getLinkList().getLinks());
		
		//LinkList directedLinks = new LinkList();
		Node tail;
		
		// prepare the tree to conatin directed and undirected links
		tree.setKindOfGraph(Graph.MIXED);
		// direct the links
		while(!pending.isEmpty()){
			tail = pending.pop();
			Vector<Link> directedLinks = new Vector<Link>();
			for(Link nextLink : undirectedLinks){
				Node head = null;
				if(nextLink.getHead() == tail){ head = nextLink.getTail(); }
				else if(nextLink.getTail() == tail){ head = nextLink.getHead();}
				if(head != null){ 
					pending.push(head);
					try{
						tree.removeLink(nextLink);
					} catch(InvalidEditException iee){
						iee.printStackTrace();
						System.out.println("Could not remove link!!!");
					}
					try{
						tree.createLink(tail, head, true);
					} catch(InvalidEditException iee){
						iee.printStackTrace();
						System.out.println("It seems that the tree givin to directTree method was not really a tree - that is, it contained undirected cycles!!!");
					}
					directedLinks.add(nextLink);
				}
			}
			undirectedLinks.removeAll(directedLinks);
		}	
	}
	
	public void learn(DataBaseCases training, int classnumber) {
		Vector<Node> features = training.getNewVectorOfNodes();
		FiniteStates FNClassVar = (FiniteStates)features.elementAt(classnumber);
		this.classVar = FNClassVar;
		features.remove(classnumber);
		
		// construct a fully conected graph over the features
		Graph myTree = new Graph();
		myTree.setKindOfGraph(Graph.UNDIRECTED);
		myTree.setNodeList(new NodeList(features));
		completeUndirectedGraph(myTree);

		// generate the partitions of the database, according to the class var
		Vector<CaseList> partitions = new Vector<CaseList>(FNClassVar.getNumStates());
		for(int i=0;i<FNClassVar.getNumStates();i++) partitions.add(CasesOps.selectFromWhere(training.getCases(), FNClassVar, i));

		// compute mutual information for each pair of features
		Vector<Double> weights = new Vector<Double>();
		LinkList llpairs = myTree.getLinkList();
		for(int i=0;i<llpairs.size();i++){
			Link l = llpairs.elementAt(i);
			FiniteStates X = (FiniteStates)l.getHead();
			FiniteStates Y = (FiniteStates)l.getTail();
			double cmut = Measures.normalisedCMI(X, Y, partitions);
			weights.add(cmut);
		}
		// compute the conditional mutual information for each pair of features, 
		// and create the connection in the graph
		
		
		MaximumSpanningTree mst = new MaximumSpanningTree(myTree, weights);
		Graph bestTree = mst.getMST();

		// use the maximally connected node as root
		// and direct the arcs accordingly
		directTree(bestTree, bestTree.getMaximallyConnectedNode());
		
		//		 now construct the classifier
		classifier = new Bnet();
		classifier.setNodeList(training.getVariables());
		classifier.setLinkList(bestTree.getLinkList());
		// add the links from class to all features
		for(Node feature : features){
			try {
				classifier.createLink(this.classVar, feature, true);
			} catch (InvalidEditException e) {
				e.printStackTrace();
				System.out.println("Unable to create link from Class to feature "+feature.getName()+"!!!");
				System.exit(112);
			}
		}
	
		// create list of relations from the structure and learn the
		// parameters from data.
		RelationList rl = new RelationList();
		NodeList nl = classifier.getNodeList();
		for(int i=0;i<nl.size(); i++){
			FiniteStates n = (FiniteStates)nl.elementAt(i);
			NodeList vars = new NodeList();
			vars.insertNode(n);
			NodeList parents = new NodeList(classifier.getLinkList().getParentsInList(n));
			vars.join(parents);
			//PotentialTable pot = new PotentialTable(vars);
			PotentialTable pot = training.getPotentialTable(vars);
			pot.sum(1.0);
			PotentialTable q = (PotentialTable) pot.addVariable(n);
			PotentialTable conditional = pot.divide(q);
			Relation r = new Relation(conditional);
			rl.insertRelation(r);
		}
		classifier.setRelationList(rl.getRelations());
		this.repairNodesFromLinkList();
	}

	public void saveModelToFile(String absolutPath) throws IOException {
		// TODO Auto-generated method stub
		FileWriter fw = new FileWriter(absolutPath+"/tan.elv");
		classifier.saveBnet(fw);
		fw.close();
	}

	public Bnet getClassifier(){
		return classifier;
	}
	
	public FiniteStates getClassVar(){
		return classVar;
	}
	
	//private void print
	
	public static void main(String[] argv) throws Exception {
		DataBaseCases train = new DataBaseCases(argv[0]);
		TreeAugmentedNaiveBayes tan = new TreeAugmentedNaiveBayes();
		tan.learn(train, train.getClassId());
	}
}
