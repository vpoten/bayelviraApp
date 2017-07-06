package elvira;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;


	
	
	
	import java.util.*; 
	import java.awt.*; 
	import java.io.*; 
	import java.net.URL; 

import elvira.inference.Propagation;
import elvira.inference.clustering.HuginPropagation;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.inference.uids.AODinamicImprovedFirstBranchUID;
import elvira.inference.uids.AODinamicImprovedRandomizedUID;
import elvira.inference.uids.AODinamicImprovedUID;
import elvira.inference.uids.AODinamicTailUID;
import elvira.inference.uids.AOUID;
import elvira.inference.uids.AOUIDCoalescenceAndConservative;
import elvira.inference.uids.AOUIDLOfH;
import elvira.inference.uids.DynamicUID;
import elvira.inference.uids.GSDAG;
import elvira.inference.uids.MNode;
import elvira.inference.uids.Anytime.AOUID_Anytime;
import elvira.inference.uids.AnytimeUpdKAdmissBreadthSearch.AOUID_Any_Upd_K_Adm_Breadth;
import elvira.inference.uids.AnytimeUpdatingK.AOUID_Any_Upd_K;
import elvira.inference.uids.AnytimeUpdatingKAdmiss.AOUID_Any_Upd_K_Adm;
	import elvira.parser.*; 
import elvira.potential.*; 

	 

	/** 
	 * This class implements the structure for storing and  
	 * manipulating the Unconstrained Influence Diagrams.
	 * 
	 * @version 0.1 
	 * @since 21/2/2002 
	 * @Autor: Marta Vomlelova, originally Manuel Gomez (with copy&paste of Bnet.java) 
	 */ 
	public class UID extends IDWithSVNodes { 
		
		public enum AlgorithmsForUID { AOUID, DYNAMICUID, AODINAMICIMPROVEDUID, AODINAMICIMPROVEDRANDOMIZEDUID, AOUIDLOFH, AODINAMICIMPROVEDFIRSTBRANCHUID, AOUIDCONSERVATIVE, AOUIDANYTIME, AOUIDANYTIMEUPDATING, AOUIDANYTIMEUPDATINGADMISS, AOUIDANYTIMEUPDATINGADMISSBREADTH}
//		public enum AlgorithmsForUID { DINAMICUID}
		
	//	Propagation propagation = null;

	    final boolean compile=true; // switch for testing the structural algorithm only
	    final double epsilon=0.000001;
	    final boolean fullname=false; // switch for more detail labeles on the nodes
	    final boolean joinUtils=true; // join utility potentials when eliminating a chance node?
	    final boolean prune=false; // prunes nodes not in StepFunctions
	    final boolean structuralPruning=false;
	    final boolean tablePruning=false;
		final int xStep=75;
		final int yStep=50;
		final int xMod=12;
		final int yMod=12;

	    Bnet theGraph=null;    // the G-graph - general solution graph for a PID

	    public Bnet getGraph(){
		return theGraph;
	    }
	
	    
	 /** 
	 * Non-observable nodes are marked by "h" in comment.
	 * This function returns info whether a node is observable.
	 * I think h means 'hidden'
	 */

	    public boolean isObservable (Node node){
		return(!"h".equals(node.getComment()));
	    }
	    
	    
	   /* 
		 * Non Observables are the variables without decision in the set of descendants.
		 * Observables are the rest. A variable can be observed when its ancestors decisions have been taken
		 * This function returns info whether a node is observable.
		 */
	    
	    /**
	     * I consider observable the variables if there is a decision that is ancestor the node
	     * @return
	     */
	/*    private boolean isObservable (Node node){
	    	NodeList decisions;
	    	NodeList ascendNodes;
	    	   	
	    	    
	    	Vector ascend = ascendants(node);
	    	ascendNodes = new NodeList(ascend);
	    	decisions = getNodesOfKind(Node.DECISION);
	    	
	    	return (decisions.intersectionNames(ascendNodes).size()>0);
	    	
	    				
		}*/
	 
	 /** 
	 * Should be used only on diverging nodes: 
	 * links used in Step-functions are marked red
	 */

	    private boolean isUsedLink (Link link){
		return((link.getColorLink()==Color.red));
	    }


	 /** 
	 * Visits the 
	 * @param node
	 * and all its used descendants
	 */

	    private void markVisited (Node node){
			node.setVisited(true);
			LinkList children=node.getChildren();
			switch(children.size()){
			case 0:
			    break;
			case 1:
			    MNode  child1=(MNode)children.elementAt(0).getHead();
			    if(!child1.getVisited())
			    	 markVisited(child1);
			    break;
			default:
				for(Enumeration enumeration=children.elements(); enumeration.hasMoreElements();){
					Link link=(Link)enumeration.nextElement();
					if(isUsedLink(link)){
					    MNode  child=(MNode)link.getHead();
					    if(!child.getVisited())
					    	 markVisited(child);
					}
				}
			}
		return;
	    }


	 /** 
	 * Removes all unvisited links
	 */

	    private void removeUnvisited (){
		NodeList list=theGraph.getNodeList();
		for(int j=list.size()-1; j>=0;j--){
			Node node=list.elementAt(j);
			if(!node.getVisited()){
				theGraph.removeNode(node);
			}
		}
		}

	 /** 
	 * Marks all nodes as unvisited
	 */

	    private void markAllUnvisit(){
		NodeList list=theGraph.getNodeList();
		for(Enumeration enumeration=list.elements(); enumeration.hasMoreElements();){
			Node node=(Node) enumeration.nextElement();
			node.setVisited(false);
		}
		}

	 /** 
	 * This function returns the list of names of non observable nodes.
	 */

	    private TreeSet getNonObservables(){
		Enumeration e;
		
		TreeSet allNonObservable=new TreeSet(); 
		for (e = getNodeList().elements(); e.hasMoreElements(); ) {
		    Node node=(Node)e.nextElement();
		    if((node.getKindOfNode()==Node.CHANCE) && !isObservable(node))
			allNonObservable.add(node.getName());
		}
		return allNonObservable;
	    }
	    
	    /** 
		 * This function returns the list of names of non observable node, but in an ArrayList
		 */
	    public ArrayList<String> getNonObservablesArrayList(){
	    	Enumeration e;
			
			ArrayList<String> allNonObservable=new ArrayList(); 
			for (e = getNodeList().elements(); e.hasMoreElements(); ) {
			    Node node=(Node)e.nextElement();
			    if((node.getKindOfNode()==Node.CHANCE) && !isObservable(node))
				allNonObservable.add(node.getName());
			}
			return allNonObservable;
		    }
	    
	    		  
	    /** 
		 * This function returns the number of non observable variables
		 */
	    public int getNumNonObservable(){
	    	
	    	return (this.getNonObservablesArrayList().size());
	    	
	    }
	    
	    
	    /** 
		 * This function returns the list of names of observable variables
		 */
	    public int getNumObservable(){
	    	
	    	return (this.getNumNodesOfKind(Node.CHANCE)-this.getNumNonObservable());
	    	
	    }
	    
	    
	 /** 
	 * Takes a 
	 * @param map and tests all its sets; selects those that include another set, i.e. are not minimal w.r.t. inclusion
	 * returns a <code>TreeSet</code> of selected nodes
	 */

	   private TreeSet toRemove(TreeMap map){
		TreeSet toRemove=new TreeSet();
		for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
		    String key1=(String) it.next();
		    for (Iterator it2 = map.keySet().iterator(); it2.hasNext(); ) {
			String key2=(String) it2.next();
			if(!key1.equals(key2)){

			    if(!toRemove.contains(key1) && ((TreeSet)map.get(key2)).containsAll(((TreeSet)map.get(key1))))
				{
				    toRemove.add(key2);
				}
			}
		    }
		}  
		return toRemove;
	    }

	 /** 
	 * Takes a 
	 * @param NodeList and returns a TreeSet of names of <b>observables</b> contained in the list
	 */

	    private TreeSet nodeListToObsSet(NodeList list){
		TreeSet set=new TreeSet();
		Enumeration e;   
		for(e=list.elements();e.hasMoreElements();){
		    Node node1=(Node)e.nextElement();
		    if((node1.getKindOfNode()==Node.CHANCE) && isObservable(node1))
			set.add(node1.getName());
		}
		return set;
	    }
	 /** 
	 * Takes a 
	 * @param NodeList and returns a TreeSet of names of nodes contained in the list
	 */

	    private TreeSet nodeListToSet(NodeList list){
		TreeSet set=new TreeSet();
		Enumeration e;   
		for(e=list.elements();e.hasMoreElements();){
		    Node node1=(Node)e.nextElement();
			set.add(node1.getName());
		}
		return set;
	    }

	    private TreeSet nodeVectorToSet(Vector list){
		TreeSet set=new TreeSet();
		Enumeration e;   
		for(e=list.elements();e.hasMoreElements();){
		    Node node1=(Node)e.nextElement();
			set.add(node1.getName());
		}
		return set;
	    }
	 /** 
	 * Takes a 
	 * @param NodeList and returns a HashMap of names of descendant observables for each node in the list, the key is the NAME of the node
	 */

	    private HashMap descMap(NodeList list){
		HashMap desc=new HashMap();
		Enumeration e;   
		for(e=list.elements();e.hasMoreElements();){
		    Node node1=(Node)e.nextElement();
			desc.put(node1.getName(),nodeListToObsSet(descendantsList(node1)));
		}
		return desc;
	    }

	 /** 
	 *  Returns an existing decision <code>MNode</code> labeled <code>
	 * @param newList.toString()+
	 * @param obsList.toString()+
	 * @param oldList.toString()  </code> from 
	 *@param theGraph
	 *or creates a new one 
	 */

	    private MNode getDNode(TreeSet newList,TreeSet obsList,TreeSet oldList)
	    {
		MNode node=null;
		try{
			    try{
				System.out.print(newList.toString()+obsList.toString()+oldList.toString()+"\n");
				node=(MNode)theGraph.getNode(newList.toString()+obsList.toString()+oldList.toString());
			    }
			    catch(ArrayIndexOutOfBoundsException ex) {
				    node=new MNode(newList.toString()+obsList.toString()+oldList.toString(),newList,this);
				    node.setToEliminate((TreeSet)newList.clone());
				    node.setObsEliminate((TreeSet)obsList.clone());
			    }
		    node.setKindOfNode(Node.DECISION);
			    if(node.getChildren().size()==0){
				if(!fullname){
				    node.setTitle(newList.toString()+obsList.toString());
				}
				//		node.setStates(new Vector(newList));
				newList.addAll(oldList);
				newList.addAll(obsList);
				theGraph.addNode( node);  
			    } 
		}
		catch(InvalidEditException ex){
		    System.err.print(ex); 
		}
		return node;
	    }
	 /** 
	 *  Returns an existing decision <code>MNode</code> labeled <code>
	 * @param newObservations.toString()+
	 * @param oldObservations.toString()  </code> from 
	 *@param theGraph
	 *or creates a new one at 
	 *@param whereX and
	 *@param whereY
	 */

	    private MNode getONode(TreeSet newObservations, TreeSet oldObservations)
	    {
		MNode  oNode=null;
		try{
		    // creates list of all (future) observations and list of new (future) observations added in this step
		    //these two sets identify a node
		    TreeSet observ=new TreeSet();
		    observ.addAll(oldObservations);
		    observ.addAll(newObservations);
		    try{
			oNode=(MNode)theGraph.getNode(oldObservations.toString());
		    }
		    catch(ArrayIndexOutOfBoundsException ex) { // new node with observations
			oNode=new MNode(oldObservations.toString(),observ,this);
			oNode.setToEliminate((TreeSet)newObservations.clone());
			oNode.setObservations(observ);
			if(!fullname){
			    oNode.setTitle(".");
			}
			//	oNode.setStates(new Vector(newObservations));
			theGraph.addNode( oNode);  
		    }
		}
		catch(InvalidEditException ex){
		    System.err.print(ex); 
		}
		return oNode;
	    }

	 /** 
	 *  Returns an existing observation <code>MNode</code>, generates the label 
	 *@param theGraph
	 *or creates a new one at Math.min(30*
	 *@param whereX , 400) and procNode.getPosX()-50
	 * and updates the list of observations in
	 * @param newNode
	 */

	    private MNode getONode( MNode procNode, MNode newNode, TreeSet newList)
	    {
		MNode  oNode=null;
		    // creates list of all (future) observations and list of new (future) observations added in this step
		    //these two sets identify a node
		    TreeSet observ=new TreeSet();
		    observ.addAll(procNode.getObservations());
		    TreeSet oldObservations=(TreeSet)observ.clone();
		    observ.addAll(newList);
		    newNode.setObservations(observ);
		    TreeSet newObservations=(TreeSet)observ.clone();
		    newObservations.removeAll(oldObservations);
		    newNode.setObsEliminate(newObservations);
		    if(newObservations.size()>0)
			oNode=getONode( newObservations, oldObservations );
		return oNode;
	    }



	    private void printNodeMap( HashMap map){
		for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
		    String nodeString=(String) it.next();
		    System.out.print(nodeString+": "); 
		    for (Iterator nodeIt = ((Set)map.get(nodeString)).iterator(); nodeIt.hasNext(); ) {
			Node node=(Node) nodeIt.next();
			System.out.print(node.getName()+" "); 
		    }

		    System.out.print("\n"); 
		}
		System.out.print("\n\n"); 

	    }

	    private TreeSet toStrings (AbstractCollection set){
		TreeSet tree=new TreeSet();
	        for (Iterator setIt = set.iterator(); setIt.hasNext(); ) {
		    Node node=(Node)setIt.next();
		    tree.add(node.getName());
		}
		return tree;
	    }

/*	 *//** 
	 *  Takes
	 * @param MNode
	 * @param potentials
	 * @param utilities, eliminates nodes described in label of MNode and
	 * stores the results in MNode.setRelations().
	 *//*
	public void initEval(MNode firstNode){
	       Vector rels=getRelationList();
	    Vector potentials=new Vector();
	    Vector utilities=new Vector();
	    for(Iterator it=rels.iterator(); it.hasNext();){
		Relation rel=(Relation)it.next();
		if(rel.getKind()==Relation.POTENTIAL){
			MPotential mpot=new MPotential(rel);
		    potentials.add(mpot);
		}
		else if(rel.getKind()==Relation.UTILITY){
			NodeList va=rel.getVariables().copy();
			Node head=rel.getVariables().elementAt(0);
			va.removeNode(head);
			rel.restrictToVariables(va);
			MPotential mpot=new MPotential(head, va, rel);
		    utilities.add(mpot);
		}
		else
		    System.out.print("Strange type of relation.\n");
	    }
//	    nameRelations(utilities);
	    evaluate(firstNode,potentials, utilities);
	    }
*/
/*	public void eval(MNode node){
		try{
		MNode child0=(MNode)node.getChildren().elementAt(0).getHead();
		if(!child0.getVisited()){
			child0.setVisited(true);
			LinkList children=child0.getChildren();	
			switch(children.size()){
			case 0:
			    break;
			case 1:
			    MNode  child=(MNode)children.elementAt(0).getHead();
			    evaluate(child0, (Vector)child.getPotentials().clone(), (Vector)child.getUtilities().clone());
			    break;
			default:
			    combEval(child0);
			}
		}
		node.setVisited(true);	
		evaluate(node,(Vector)child0.getPotentials().clone(), (Vector)child0.getUtilities().clone());
		}
		catch(NullPointerException e){
			System.out.print("NullPointerException in eval.\n");
		}

	}*/

/*	    public void evaluate(MNode procNode, Vector potentials, Vector utilities){
		//******************  propagation block
		if((potentials==null)||(utilities==null) ){
		    System.out.print("Evaluate: null relation list\n"); 
		    return;
		}
		
		if(procNode.getKindOfNode()==Node.CHANCE){
		    procNode.setRelations(potentials, utilities);
		    return;
		}
		//eliminate observations first
		for (Iterator e = procNode.getObsEliminate().iterator(); e.hasNext(); ) {
		    String str=(String)e.next();
		    FiniteStates elimNode=(FiniteStates)getNode(str);
		    if(elimNode==null){
			System.out.print("Cannot find node " + str+"\n"); 
			continue;
		    }
	  	    pidStep(elimNode,potentials, utilities);
//	  	    System.out.print("After"+str+"\n");
//	  	    potentials.print();
		}

		if(procNode.getKindOfNode()==Node.UTILITY){
		    procNode.setRelations(potentials, utilities);
		    return;
		}

		Vector results=new Vector();
		//then eliminate decisions
		for (Iterator e = procNode.getToEliminate().iterator(); e.hasNext(); ) {
		    String str=(String)e.next();
		    FiniteStates elimNode=(FiniteStates)getNode(str);
		    if(elimNode==null){
			System.out.print("Cannot find node " + str+"\n"); 
			continue;
		    }
		    //pidStep modifies potentials and utilities !!
		    results.addAll( pidStep(elimNode,(Vector) potentials.clone(), utilities));
		}
	    procNode.setRelations(potentials, utilities, results);
	    //  	if((procNode.getKindOfNode()==Node.DECISION) && (procNode.getChildrenNodes().getSize()>0)){
	    //  	    MNode chance=((MNode)procNode.getChildrenNodes().elementAt(0));
	    //  	    if(chance.getKindOfNode()==Node.CHANCE)
	    //  		chance.setPairTable(null);
	    //  	}
	}*/

	  
/*	  private boolean dominated (Potential first, Potential second, Vector variables, int potSize){
		Configuration conf, conf1, conf2;
	    conf = new Configuration(variables);
	    for (int i=0 ; i<potSize ; i++) {
	      	conf1 = new Configuration(first.getVariables(),conf);
		    conf2 = new Configuration(second.getVariables(),conf);
		    if(first.getValue(conf1)>second.getValue(conf2)+epsilon){
			    	return false;
			}
			conf.nextConfiguration();
	    }
		return true;  
	  }*/
	  	
	/*  *//** 
	   * Evaluates "more tails" node, looks for the maximal utility in every cell over all outgoing arcs.
	 * and stores the results in MNode.setRelations().
	 *//*

	private void insertResult(MNode node, String states[], double values[] ){
		    node.setStates(states);
	    PotentialTable pot2=new PotentialTable(node);
	    pot2.setValues(values);

	    Vector vec1=getCompiledPotentialList();
	    vec1.add(pot2);
	}
*/
/*	private MPotential getCombinedPotential(
	    MNode node, Vector pt, Vector vars[], Vector variabless, int oneChampion){
			int firstChampion=-1;
			int secondChampion=-1;
		    String states[];
		    double values[];
			Configuration conf, conf1;
			LinkList childrenL=node.getChildren();
			Node utilNode=((MPotential)pt.elementAt(0)).getUtilNode();
		  
			// Creates the new potential.
			MPotential pot = new MPotential(variabless,utilNode);
		  
		      // Now explore all the configurations in the new potential,
		    // evaluate the two operands according to this configuration,
		    // and sum the two values.
		  
		    conf = new Configuration(variabless);
			states=new String[(int)pot.getSize()];
			values=new double[(int)pot.getSize()];
		
		    for (int i=0 ; (secondChampion!=-2) && (i<pot.getValues().length) ; i++) {
				int maxJ=0;
			      conf1 = new Configuration(vars[0],conf);
				double maxValue=((Potential)pt.get(0)).getValue(conf1);
				//	    System.out.print(maxValue+", ");
				for(int j=1; j<pt.size();j++){
				    conf1 = new Configuration(vars[j],conf);
				    double value=((Potential)pt.get(j)).getValue(conf1);
				    //    System.out.print(value+", ");
				    if(value>maxValue+epsilon){
					maxValue=value;
					maxJ=j;
				    }
				}
				//	    System.out.print("\n MaxValue"+maxValue+"\n");
				pot.setValue(conf,maxValue);	
				if(firstChampion==-1)
				    firstChampion=maxJ;
				else if (firstChampion!=maxJ)
					if(secondChampion==-1)
						secondChampion=maxJ;
					else	
					    secondChampion=-2;
				states[i]=conf.toString()+"..."+((MNode)childrenL.elementAt(maxJ).getHead()).getName(); 	
				values[i]=maxValue;
				childrenL.elementAt(maxJ).setColorLink(Color.red); 
				conf.nextConfiguration();
		    }
		   	if((secondChampion>-1) && dominated((Potential)pt.get(firstChampion), 
		   						(Potential)pt.get(secondChampion), 
		   						variabless, pot.getValues().length)){
		   		firstChampion=secondChampion;
		   		secondChampion=-1;
		   	} 
		   	if(secondChampion==-1){    		
			    states=new String[1];
			    states[0]=((MNode)childrenL.elementAt(firstChampion).getHead()).getName();
			    values=new double[1];
			    values[0]=-1;
			    oneChampion=firstChampion;
		   	}
		   	else
		   		oneChampion=-1;
			insertResult(node, states, values);
		return pot;
	}*/
	   
	/*    public void combEval(MNode node) {

		LinkList childrenL=node.getChildren();
		Vector vars[]=new Vector[childrenL.size()];
		Vector rell[]=new Vector[childrenL.size()];

		Vector variabless=new Vector();
		for(int i=0; i<childrenL.size();i++){
			rell[i]=(Vector)((MNode)childrenL.elementAt(i).getHead()).getUtilities().clone();
		    }
		Vector utilities=new Vector();
		for(int j=rell[0].size()-1; j>=0;j--){
			Potential r0=(Potential)rell[0].elementAt(j);
			boolean inAll=true;
			for(int i=1; inAll && i<childrenL.size();i++){
				if( ! rell[i].contains(r0))
					inAll=false;
			}
			if(inAll){
				utilities.add(r0);
				for(int i=0;  i<childrenL.size();i++){
						rell[i].removeElement(r0);
				}
			}			
		}
	  try{
		if(rell[0].size()==0){
		    String states[];
		    double values[];
		    states=new String[1];
		    values=new double[1];
			childrenL.elementAt(0).setColorLink(Color.red); 		
			insertResult(node, states, values);
		}
		else{	    
			Vector pt=new Vector(childrenL.size());
			Node utilNode=((MPotential)rell[0].elementAt(0)).getUtilNode();
			for(int i=0; i<childrenL.size();i++){
		//		System.out.print("Child "+i+": utilities size= "+utilities.size()+"\n relations:"); 	
		
				MPotential potential=utilList2table(rell[i]);
				// adds all variables into the list of variables ... if they are not there already
				if(potential==null)
					System.out.print("Empty utility list during combEval.\n");

				vars[i]=potential.getVariables();
				for (Enumeration en = vars[i].elements() ; en.hasMoreElements() ;) {
				    Object obj=en.nextElement();
				    if(! variabless.contains(obj))
					variabless.add(obj);
				}
		
				pt.add(potential);
			    }
			    int oneChampion=-1;
			MPotential pot=getCombinedPotential(node, pt, vars, variabless, oneChampion);	
			if(oneChampion>-1){    		
				utilities=(Vector)((MNode)childrenL.elementAt(oneChampion).getHead()).getUtilities().clone();
		    }
			else{    		
		       	utilities.add(pot);
		    }
			
		}
	    Vector potentials=(Vector)((MNode)childrenL.elementAt(0).getHead()).getPotentials().clone();

	    node.setRelations(potentials, utilities );
		if(node.getKindOfNode()==Node.UTILITY)
		    evaluate(node, (Vector)potentials.clone(), (Vector)utilities.clone());
	  }
	  catch(NullPointerException e){
	  	System.out.print("NullPointerException in combEval.\n");
	  }    
	    return;
	}
*/


	  /** 
	   * Generates an GS-graph - a general solution graph for a PID
	   * stores the graph in theGraph
	 */
	    private void gAddLink(MNode tail, MNode head){
		try{
		    Link   link = theGraph.getLink(tail,head);  
		    if (link == null){
			theGraph.createLink(tail,head, true);
			link = theGraph.getLink(tail,head);  
			link.setColorLink(Color.black); 
		    }
		}
		catch(InvalidEditException ex) {// if the link already exists
		    System.out.print(tail.getName()+ "-->"+ head.getName()+"  ");
		}
	    
	    }

	    //Simplified version of the Marta's method, in which we don't detect structural
	    //redundancies to pospone decisions when there's observations that are irrelevant
	    //for them
	private void findParentsSimplified(Vector decV, Vector obsV, MNode procNode, TreeSet decisionList,
									HashMap descendants){
		TreeSet eliminated=(TreeSet)procNode.getEliminated().clone();
	    TreeMap map=new TreeMap();// {nodeString, TreeSet of descendants}
	    TreeSet nonElDec=((TreeSet)decisionList.clone());
	    nonElDec.removeAll(eliminated);

	    // only decisions in the current set are considered
	    for (Iterator itProc = nonElDec.iterator(); itProc.hasNext(); ) {
			String node1=(String)itProc.next();
			TreeSet descNonEl=(TreeSet)((TreeSet)descendants.get(node1)).clone();
			descNonEl.removeAll(eliminated);
			map.put(node1,descNonEl);
	    }
	    // erasing sets of observation descendants that are not minimal w.r.t. inclusion
	    TreeSet toRemove=toRemove(map);
	    // creating a observation and decision node for each remaining (i.e. minimal) set
	    for (Iterator it0 = map.keySet().iterator(); it0.hasNext(); ) {
			String node1=(String)it0.next();
			if(toRemove.contains(node1))continue;
			TreeSet states1=new TreeSet();
			states1.add(node1);
			// adding all equivalent decisions with the processed one (i.e. they have the same set of descendant unobserved observations)
			for (Iterator it2 = map.keySet().iterator(); it2.hasNext(); ) {
			    String key2=(String) it2.next();
			    if(toRemove.contains(key2) && ((TreeSet)map.get(node1)).containsAll(((TreeSet)map.get(key2))))
				states1.add(key2);
			}
			decV.add(states1);
			obsV.add(map.get(node1));
		}	

	return;	
	}
	
	/*private void findParents(Vector decV, Vector obsV, MNode procNode, TreeSet decisionList,
			HashMap descendants){
TreeSet eliminated=(TreeSet)procNode.getEliminated().clone();
TreeMap map=new TreeMap();// {nodeString, TreeSet of descendants}
TreeSet nonElDec=((TreeSet)decisionList.clone());
nonElDec.removeAll(eliminated);

// only decisions in the current set are considered
for (Iterator itProc = nonElDec.iterator(); itProc.hasNext(); ) {
String node1=(String)itProc.next();
TreeSet descNonEl=(TreeSet)((TreeSet)descendants.get(node1)).clone();
descNonEl.removeAll(eliminated);
map.put(node1,descNonEl);
}
// erasing sets of observation descendants that are not minimal w.r.t. inclusion
TreeSet toRemove=toRemove(map);
// creating a observation and decision node for each remaining (i.e. minimal) set
for (Iterator it0 = map.keySet().iterator(); it0.hasNext(); ) {
String node1=(String)it0.next();
if(toRemove.contains(node1))continue;
TreeSet states1=new TreeSet();
states1.add(node1);
// adding all equivalent decisions with the processed one (i.e. they have the same set of descendant unobserved observations)
for (Iterator it2 = map.keySet().iterator(); it2.hasNext(); ) {
String key2=(String) it2.next();
if(toRemove.contains(key2) && ((TreeSet)map.get(node1)).containsAll(((TreeSet)map.get(key2))))
states1.add(key2);
}
decV.add(states1);
obsV.add(map.get(node1));
}	
//eliminating decisions that may be posponed since other set of obsV is
// irrelevant for them.
if(structuralPruning){
Vector utilities=procNode.getUtilities();
Vector potentials=procNode.getPotentials();
Vector varV=new Vector();
for(int i=0; i<obsV.size(); i++){
TreeSet variables=new TreeSet();
for(Iterator it=((TreeSet)obsV.elementAt(i)).iterator();it.hasNext();)
{
String str=(String)it.next();
FiniteStates elimNode=(FiniteStates)getNode(str);
if(elimNode==null){
System.out.print("Cannot find node " + str+"\n"); 
continue;
}
Vector utils=getPotentialsOf(utilities,elimNode); 
for(Enumeration enumeration=utils.elements();enumeration.hasMoreElements();){
variables.addAll(nodeVectorToSet(
			((Potential)enumeration.nextElement()).getVariables()));
}
Vector pots=getPotentialsOf(potentials,elimNode); 
for(Enumeration enumeration=pots.elements();enumeration.hasMoreElements();){
variables.addAll(nodeVectorToSet(
			((Potential)enumeration.nextElement()).getVariables()));
}
}
variables.removeAll(decisionList);
varV.add(variables);	
}
boolean toRem[]=new boolean[decV.size()];
Arrays.fill(toRem, false); 
//boolean used[]=new boolean[decV.size()];
//Arrays.fill(used, false); 
for(int i=0; i<obsV.size();i++){
if(!toRem[i])	    	
for(int j=i+1; j<varV.size();j++){
//if(used[j] || (i==j))
//	continue;
TreeSet varClone=(TreeSet)((TreeSet)varV.elementAt(j)).clone();
	if(!varClone.removeAll((TreeSet)obsV.elementAt(i))){
		toRem[j]=true;
//		used[i]=true;
		break;
	}
}		
}
for(int i=obsV.size()-1; i>=0;i--){
if(toRem[i]){
decV.removeElementAt(i);
obsV.removeElementAt(i);
}
}
}
return;	
}
*/
	    public MNode createGSDAG(){
		theGraph=new Bnet();
		TreeSet decisionList=getDecisions();
		int numDecisions=decisionList.size();

		HashMap descendants=descMap(getDecisionList()); // remembers decsendants .. hopefully faster than search in the graph each time
		Enumeration e;      
		//list of nodes to process
		Vector process=new Vector(); 
		//first - initial node
		TreeSet nonObservables=getNonObservables();
		MNode newNode=getONode(new TreeSet(),nonObservables);
		newNode.setName("."+newNode.getName());
		newNode.setTitle(nonObservables.toString());
		newNode.setObsEliminate(nonObservables);
		newNode.setKindOfNode(Node.UTILITY);
		MNode firstNode=newNode;
//		initEval(firstNode);
		process.add(newNode);	    
		HashSet firstDecisions=new HashSet();
		// main cycle
		while(!process.isEmpty()){
		    MNode procNode=(MNode)process.firstElement();
		    process.removeElement(procNode);
//		    if(procNode != firstNode)
//		    	eval(procNode);
			Vector decV=new Vector();
			Vector obsV=new Vector();
			findParentsSimplified(decV,obsV, procNode, decisionList, descendants);
		    for (int i=0; i<decV.size();i++ ) {
			TreeSet decs=(TreeSet)decV.elementAt(i);
			TreeSet obss=(TreeSet)obsV.elementAt(i);
			//new decision node
			newNode= getDNode( decs,  obss,  procNode.getEliminated());
			if(newNode.getChildren().size()==0){
			    if(newNode.getNumDecisions()<numDecisions){
				process.add(newNode);
			    }
			    else {
				firstDecisions.add(newNode);
			    } 
			}
			// creates list of all (future) observations and list of new (future) observations added in this step
			//these two sets identify a node
			MNode oNode=getONode(procNode, newNode, obss);
			if(oNode!=null){
			    gAddLink(oNode, procNode);
			    gAddLink(newNode,oNode);  
			}
			else {
				gAddLink(newNode,firstNode);  
			}
		    } // end for(it0 ...)
		    procNode.freeDO();
		} // end while process not empty
		// observations that are not blocked at all
//		if(newNode!=null){
		    TreeSet newObservations=new TreeSet();
		    for(Enumeration enumeration=enumerateNodes();enumeration.hasMoreElements();)
			{
			    Node tmpNode=(Node)enumeration.nextElement();
			    if(tmpNode.getKindOfNode()==Node.CHANCE)
				newObservations.add(tmpNode.getName());
			}
		    newObservations.removeAll(newNode.getObservations());
		    MNode oNode=getONode( newObservations,newNode.getObservations());
			oNode.setObsEliminate(newObservations);
			oNode.setKindOfNode(Node.UTILITY);
		    if(!fullname)
		    	oNode.setTitle(newObservations.toString());
		    for (Iterator itM = firstDecisions.iterator(); itM.hasNext(); ) {
			MNode dNode=(MNode)itM.next();
//			eval(dNode);
			gAddLink(oNode,dNode);  
		    }
	/*	    if(oNode.getChildren().size()>1)
		    	combEval(oNode);
		    else{
		    	MNode child=(MNode)oNode.getChildren().elementAt(0).getHead();
		    	evaluate(oNode,(Vector)child.getPotentials().clone(), 
		    		(Vector)child.getUtilities().clone());
		    	}
		   	if(prune){
				markAllUnvisit();
				markVisited(oNode);
				removeUnvisited();
			}
		    markAllUnvisit();
	*/   		levelCounts=new Vector();
			layoutNode(oNode,0);
			return oNode;
//		} // end if newNode!=null
	    }

	       
	    /**  
		     * Creates a new empty <code>IDiagram</code> object.
		     */

		    public UID() {
	  
			super();
		    }




		    /** 
		     * Creates a Network parsing it from a file.
		     * @param f file that contains the <code>IDiagram</code>. 
		     */

		    public UID(FileInputStream f) throws ParseException ,IOException {    

			BayesNetParse parser = new BayesNetParse(f); 
			parser.initialize(); 
	  
			parser.CompilationUnit(); 
			translate(parser);   
		    } 


		    /** 
		     * Creates a new <code>IDiagram</code> using the file given in a URL.
		     * @param url location of the file.
		     * @see BayesNetParse#initialize.
		     * @see BayesNetParse#CompilationUnit.
		     */

		    public UID(URL url) throws IOException, ParseException { 
	       
			InputStream istream = url.openStream(); 
			BayesNetParse parser = new BayesNetParse(istream); 
			parser.initialize(); 
	  
			parser.CompilationUnit(); 
			translate(parser);       
		    } 


		    /** 
		     * Stores the <code>IDiagram</code> in the file given as parameter.
		     * @param f file where the <code>IDiagram</code> is saved.
		     * @see Network#save
		     */

		    public void saveIDiagram(FileWriter f) throws IOException { 
	 
			PrintWriter p; 
	  
			p = new PrintWriter(f); 
	  
			super.save (p); 
		    } 


		    /**
		     * Saves the header of the file that will contain this
		     * diagram
		     * @param p the file.
		     */

		    public void saveHead(PrintWriter p) throws IOException {
	  
			p.print("// Influence Diagram\n"); 
			p.print("//   Elvira format \n\n"); 
			p.print("uid  \""+getName()+"\" { \n\n");
		    }


		    /** 
		     * Checks that all the links are directed.
		     * @return <code>true</code> if OK, <code>false</code> in other case.
		     */

		    public boolean directedLinks() {
	  
			LinkList list;
			Link link;
			boolean directed;
			int numberOfLinks;
			int i;
	  
			list = getLinkList(); 
			numberOfLinks = list.size(); 
	  
			for (i=0 ; i < numberOfLinks ; i++) {
			    link = list.elementAt(i);
			    directed = link.getDirected();
			    if (directed == false)
				return false; 
			} 
	  
			return true;
		    }


		    /** 
		     * To get the number of decisions in the diagram.
		     * @return TreeSet of names of decisions.
		     */

		    private TreeSet getDecisions() { 
	  
			Node node;
			TreeSet decisions=new TreeSet();
	  
			for (Enumeration enumeration=getNodeList().elements() ; enumeration.hasMoreElements() ; ) { 
			    node = (Node)enumeration.nextElement(); 
			    if (node.getKindOfNode() == node.DECISION) 
				decisions.add(node.getName()); 
			} 
	  
			return decisions; 
		    } 

	

		    /** 
		     * Checks the presence of cycles.
		     * @return <code>true</code> if there is a cycle, or <code>false</code>
		     * in other case.
		     */

		    public boolean hasCycles() { 
	  
			Graph  g = duplicate(); 
			return (!(g.isADag())); 
		    } 



		


		    /** 
		     * To evaluate the problem size.
		     * @return the number of values to store: both  probabilities 
		     *         and utilities.
		     */

		    public double getProblemSize() {
	  
			NodeList listOfNodes, parents; 
			Node node;
			double totalSize = 0, size = 1; 
			int kind, type, i; 
	  
			listOfNodes = getNodeList(); 
			for (i=0 ; i < listOfNodes.size() ; i++) { 
			    node = listOfNodes.elementAt(i);             
			    kind = node.getKindOfNode(); 
			    parents = parents(node); 
			    size = 0; 
			    switch(kind) { 
				// UTILITY 
			    case 2: size = parents.getSize(); 
				break; 
				// CHANCE 
			    case 0: type = node.getTypeOfVariable(); 
				if (type == node.FINITE_STATES) { 
				    size = (((FiniteStates)node).getNumStates())*(parents.getSize()); 
				} 
				else 
				    size = parents.getSize(); 
				break; 
			    } 
			    totalSize += size; 
			} 
			return totalSize; 
		    }


		    /**
		     * To store the decision tables. Now it is no used: the tables are
		     * added to results
		     */

		    private void storeDecisionTable(Node util, Node toRemove, RelationList tables) { 
	  
			NodeList nodes; 
			PotentialTable orderedPotential, actualPotential; 
			Configuration conf; 
			Relation finalRel; 
			double utility; 
			int total, i; 
	  
			// The dimensions of the ordered potential will be the same 
			// as the actual, but ordered: the last will be the decision 
			// to remove 
	  
			nodes = parents(util); 
			nodes.removeNode(toRemove); 
			nodes.insertNode(toRemove); 
	  
			// We create a potential to store the actual utility function 
	  
			orderedPotential = new PotentialTable(nodes); 
			total = (int)FiniteStates.getSize(orderedPotential.getVariables()); 
	  
			// We get the actual potential 
	  
			actualPotential = (PotentialTable)getRelation(util).getValues(); 
			conf = new Configuration(nodes); 
	  
			// To order the values. We copy the actual utility function 
			// in the ordered potential 
	  
			for (i=0 ; i < total ; i++) { 
			    utility = actualPotential.getValue(conf); 
			    orderedPotential.setValue(conf,utility); 
			    conf.nextConfiguration(); 
			} 
	  
			// This potential (ordered) is stored as final result of the 
			// evaluation. For that we create a new Relation 
	  
			finalRel = new Relation(); 
			finalRel.setName(util.getName()); 
			finalRel.setKind(finalRel.UTILITY); 
			finalRel.setVariables(nodes); 
			finalRel.setValues(orderedPotential); 
	  
			// This relation is inserted in the decisionTables 
	  
			tables.insertRelation(finalRel); 
		    } 


		    /** 
		     *  Prints the nodes, links, etc, to the standard output.
		     */

		    public void print() { 
	  
			NodeList nodes; 
			LinkList listOfLinks; 
			Node node; 
			Link link; 
			int i, j; 
	  
			nodes = getNodeList(); 
	  
			for (i=0 ; i < nodes.size() ; i++) { 
			    System.out.print("*********************************************\n"); 
			    node = nodes.elementAt(i); 
			    node.print(); 
			    listOfLinks = node.getParents(); 
			    for (j=0 ; j < listOfLinks.size() ; j++) { 
				link = listOfLinks.elementAt(j); 
				System.out.print("PARENT(" + j + ") = "+ (link.getTail()).getName() + "\n"); 
			    } 
			    listOfLinks = node.getChildren(); 
			    for (j=0 ; j < listOfLinks.size() ; j++) { 
				link = listOfLinks.elementAt(j); 
				System.out.print("CHILD(" + j + ") = "+ (link.getHead()).getName() + "\n"); 
			    } 
	        
			    if (node.getKindOfNode() == node.CHANCE || node.getKindOfNode() == 
				node.UTILITY) { 
				System.out.print("--------------------------------------------\n"); 
				(getRelation(node).getValues()).print(); 
				System.out.print("--------------------------------------------\n"); 
			    } 
			} 
		    } 


		    /**
		     * Copies this diagram.
		     * @return a copy of this disgram.
		     */

		    public UID copy () {
	  
			UID id = new UID();
			Graph g = duplicate();
			Enumeration e;
			Vector rl = new Vector();
	  
			id.setNodeList(g.getNodeList());
			id.setLinkList(g.getLinkList());
			for (e = getRelationList().elements() ; e.hasMoreElements() ; ) {
			    Relation r = (Relation) e.nextElement();
			    rl.add(r.copy());
			}
			id.setRelationList(rl);
			return id;
		    }


		    /**
		     * Compiles this diagram (evaluating without evidences).
		     * This method was implemented by Marta Vomlelova
		     */

	    public void compile() {
		setCompiledPotentialList(new Vector());
		MNode root=createGSDAG();
	    }


	/**
	  * Gets the expected utility tables for the decision nodes in
	  * an influence diagram
	  */
//	**********************mmm******************************************
	private void nameRelations(RelationList rels){
		for(Enumeration enumeration=rels.elements();enumeration.hasMoreElements();){
			Relation r=(Relation)enumeration.nextElement();
			r.setName(nodeListToSet(r.getVariables()).toString());
		}

	}

	    private Relation pot2rel(Potential pot, boolean potentialRel){
	        Relation r = new Relation();
	        if(potentialRel)
		    r.setKind(Relation.POTENTIAL);
		else
		    r.setKind(Relation.UTILITY);
	        r.getVariables().setNodes((Vector)pot.getVariables().clone());
	        r.setValues(pot);
	        r.setName(nodeListToSet(r.getVariables()).toString());
//		r.print();
		return r;
	    }
	    public PotentialTable pot2table(Potential p) {

		Vector v=p.getVariables();
		PotentialTable pot = new PotentialTable(v);
		Configuration conf = new Configuration(v);

		for (int i=0 ; i<pot.getSize() ; i++) {
		    pot.setValue(conf,p.getValue(conf));
		    conf.nextConfiguration();
		}

		return pot;
	    }

	/*	private MPotential utilList2table(Vector utils) {
		if(utils.size()<1) {
			return new MPotential(true);
		}
		int i=0;
		MPotential pot;
//		Configuration emptyConf=new Configuration();
		double startNum=0.0;
		while((i<utils.size()) && ((MPotential)utils.elementAt(i)).getSize()==1){
			Potential pot2=(MPotential)utils.elementAt(i);
			startNum+=((MPotential)pot2).getValue(0);
			i++;
		}
		if(i<utils.size()){
			pot = (MPotential)(((MPotential)utils.elementAt(i)).clone());
			for(i++;i<utils.size();i++){
				if(((MPotential)utils.elementAt(i)).getSize()>1)
					pot=pot.gAddition((MPotential)utils.elementAt(i));
				else{
					Potential pot2=((Potential)utils.elementAt(i));
					startNum+=((PotentialTable)pot2).getValue(0);
				}
			}
			if(startNum>0.0){
				double [] values=pot.getValues();
				for(int j=0; j<values.length;j++)
					values[j]+=startNum;
				pot.setValues(values);
			}			
		}
		else{
			pot=new MPotential(true);
			pot.utilNode=((MPotential)utils.elementAt(0)).getUtilNode();
			pot.setValue(startNum);
		}
//		pot.print();
		return pot;
		}*/

	    private Vector list2vector(RelationList list){
		Vector vector=new Vector();
		for (Enumeration enumeration=list.elements() ; enumeration.hasMoreElements() ; vector.add(((Relation)enumeration.nextElement()).getValues())); 
	        return vector;
	    }

/*	private Potential util2delta(Potential pot, FiniteStates node){
		Vector variables=(Vector)pot.getVariables().clone();
		int nodeStates=node.getNumStates();
		variables.remove(node);
		Potential newPot=new PotentialTable(variables);
		Configuration confJ=new Configuration(variables);
		variables.add(node);
		
		for(int j=0; j< confJ.size();j++){
			Configuration confI=new Configuration(variables, confJ);
			double max=pot.getValue(confI);
			int maxI=0;
			for(int i=1; i<nodeStates;i++){
				confI.putValue(node, i);
				double val= pot.getValue(confI);
				if(val>max){
					max=val;
					maxI=i;
				}
			}
			newPot.setValue(confJ, maxI);
			confJ.nextConfiguration();
		}
		if(tablePruning){
			variables.remove(node);
			Vector vars=(Vector)variables.clone();

			Vector indepVector=new Vector();
			for(Iterator it=vars.iterator();it.hasNext();){
				FiniteStates node1=(FiniteStates)it.next();
				variables.remove(node1);
				Configuration confIt=new Configuration(variables);
				variables.add(node1);
				boolean indep=true;
				for(int j=0; ((j< confIt.size()) && indep) || (j==0);j++){
					Configuration confI=new Configuration(variables, confIt);
					double value=newPot.getValue(confI);
					for(int i=1; i<node1.getNumStates();i++){
						confI.putValue(node1, i);
						double val= newPot.getValue(confI);
						if(val>value+epsilon || val<value-epsilon){
							indep=false;
							break;
						}
					}
					confIt.nextConfiguration();
				}
				if(indep)
					indepVector.add(node1);
			}
			if(indepVector.size()>0){
				variables.removeAll(indepVector);
				newPot=newPot.maxMarginalizePotential(variables); 
			}
		}			
	return newPot;
	}*/

/*		public Vector getPotentialsOfAndRemove(Vector potentials, FiniteStates var) {
				  
		  int i, p;
		  Potential pot;
		  Vector list;
		  
		  list = new Vector();
		  
		  for (i=potentials.size()-1 ; i>=0 ; i--) {
		    pot = (Potential)potentials.elementAt(i);
		    if(pot.getVariables().contains(var)) {
		      list.add(0,pot);
		      potentials.removeElementAt(i);
		    }
		  }
		  
		  return list;
		}*/
		

/*		public Vector getPotentialsOf(Vector potentials, FiniteStates var) {
				  
		  int i, p;
		  Potential pot;
		  Vector list;
		  
		  list = new Vector();
		  
		  for (i=potentials.size()-1 ; i>=0 ; i--) {
		    pot = (Potential)potentials.elementAt(i);
		    if(pot.getVariables().contains(var)) {
		      list.add(0,pot);
		    }
		  }
		  
		  return list;
		}*/


/*	  public Vector pidStep(FiniteStates x,Vector pots, Vector utils ) {
		Vector results=new Vector();
		Vector potentials=getPotentialsOfAndRemove(pots,x); 
		Vector utilities=getPotentialsOfAndRemove(utils,x); 

		if(x.getKindOfNode()==Node.CHANCE){
			MPotential  potChance=new MPotential(false);
			for(int i=0;i<potentials.size();i++){
				potChance=potChance.gCombine((MPotential)potentials.elementAt(i));
			}
			for(int i=0;i<utilities.size();i++){
				MPotential potUtil=((MPotential)utilities.elementAt(i)).gCombine(potChance);
				potUtil=potUtil.gAddVariable((FiniteStates)x, null);
				if(potUtil==null)
					System.out.print("Null utility potential.\n");
				utilities.setElementAt(potUtil,i);
			}
			potChance=potChance.gAddVariable((FiniteStates)x, pots);// i.e. MARGINALIZE OUT!!!
		    boolean neutral=(potChance==null);
			for(int i=0;i<utilities.size();i++){
				MPotential potUtil=(MPotential)utilities.elementAt(i);
				if(potUtil !=null){
					if(!neutral)
						potUtil=potUtil.gDivide(potChance);
			  		utils.add(potUtil);
				}
			}
//			if(!neutral)
//				pots.add(potChance);
	    }
		else if(x.getKindOfNode()==Node.DECISION){
		  if(utilities.size()>0){
			MPotential potUtility=utilList2table(utilities);
//			potUtility.addRequisite(x);
			if(potentials.size()>0){
				for(Iterator it=potentials.iterator();it.hasNext();){
					MPotential pot=(MPotential)it.next();
					Vector vars=(Vector)pot.getVariables().clone();
					if(vars.size()>1){				
						pot.gMaxMarginalizePotential(vars);
						if(!pot.isNeutral())
							pots.add(pot);
					}
				}
				System.out.print("Decision "+x.getName()+" in a chance potential when it is eliminated\n");
		    }
		    if(potUtility!=null){
//				results.addElement(util2delta(potUtility,x));
				Vector vars=null;
				vars=new Vector(potUtility.getVariables());
				vars.removeElement(x);	
				System.out.print("Requisite for "+x.getName()+" ");			
				potUtility=potUtility.gMaxMarginalizePotential(vars);
			    utils.add(potUtility);
			}
		  }
		  else
		  	System.out.print("Decision "+x.getName()+" irrelevant for all utilities.\n");
		}
		return results;
	    } // end method
*/	    
	private	Vector levelCounts;
	public void layoutNode(Node node, int level){
		node.setVisited(true);
		node.setAxis(30,20+node.getTitle().length()*7);
		while(levelCounts.size()<=level)
			levelCounts.add(new Integer(0));
		int order=((Integer)levelCounts.elementAt(level)).intValue();
		levelCounts.setElementAt(new Integer(order+1),level);
		node.setPosX((15+(level % xMod)*xStep)+ (level / xMod)*10);
		node.setPosY((15+(order % yMod)*yStep)+ order / yMod);
		LinkList children=node.getChildren();
		if(children==null)
			return;
		for(Enumeration enumeration=children.elements();enumeration.hasMoreElements();){
			Node child=((Link)enumeration.nextElement()).getHead();
			if(!child.getVisited())
				layoutNode(child, level+1);
		}
		return;
	}
/*	public Propagation getPropagation() {
		return propagation;
	}
	public void setPropagation(Propagation propagation) {
		this.propagation = propagation;
	}*/
	public void compile(int codeForCompile, Vector paramsForCompile) {
		// TODO Auto-generated method stub
		Propagation prop=null;
		AlgorithmsForUID alg;
		
		alg = AlgorithmsForUID.values()[codeForCompile];
		
		switch (alg){
		case AOUID:
			prop = new AOUID(this);
		    ((AOUID) prop).propagate(paramsForCompile);
		    break;
		case DYNAMICUID:
		    prop = new DynamicUID(this);
		    ((DynamicUID) prop).propagate(paramsForCompile);
		    break;
		case AODINAMICIMPROVEDUID:
			prop=new AODinamicImprovedUID(this);
			((AODinamicImprovedUID) prop).propagate(paramsForCompile);	
			break;
		case AODINAMICIMPROVEDRANDOMIZEDUID:
			prop=new AODinamicImprovedRandomizedUID(this);
			((AODinamicImprovedRandomizedUID) prop).propagate(paramsForCompile);
			break;
		case AOUIDLOFH:
			prop = new AOUIDLOfH(this);
		    ((AOUIDLOfH) prop).propagate(paramsForCompile);
		    break;
		case AODINAMICIMPROVEDFIRSTBRANCHUID:
			prop=new AODinamicImprovedFirstBranchUID(this);
			((AODinamicImprovedFirstBranchUID) prop).propagate(paramsForCompile);
			break;
		case AOUIDCONSERVATIVE:
			prop=new AOUIDCoalescenceAndConservative(this);
			((AOUIDCoalescenceAndConservative) prop).propagate(paramsForCompile);
			break;
		case AOUIDANYTIME:
			prop=new AOUID_Anytime(this);
			((AOUID_Anytime) prop).propagate(paramsForCompile);
			break;
		case AOUIDANYTIMEUPDATING:
			prop=new AOUID_Any_Upd_K(this);
			((AOUID_Any_Upd_K) prop).propagate(paramsForCompile);
			break;
		case AOUIDANYTIMEUPDATINGADMISS:
			prop=new AOUID_Any_Upd_K_Adm(this);
			((AOUID_Any_Upd_K_Adm) prop).propagate(paramsForCompile);
			break;
		case AOUIDANYTIMEUPDATINGADMISSBREADTH:
			prop=new AOUID_Any_Upd_K_Adm_Breadth(this);
			((AOUID_Any_Upd_K_Adm) prop).propagate(paramsForCompile);
			break;
			
			
			    
		
	}
		showResults(prop);
	
	}
	
	public boolean hasLinearGSDAG(){
		GSDAG gsdag=null;
		
	this.createGSDAG();
	
	try {
		gsdag = new GSDAG(this);
	} catch (InvalidEditException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return gsdag.isATraditionalID(); 
	}


	
	public boolean hasNonLinearGSDAGAndBranchAtBeginning(int minNumChildrenFirstBranch){
		
		GSDAG gsdag=null;
		
		this.createGSDAG();
		
		try {
			gsdag = new GSDAG(this);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ((gsdag.isATraditionalID()==false)&&(gsdag.hasBranchAtBeginning(minNumChildrenFirstBranch))); 
		
	}


	public int getNumberOfPaths() {
		// TODO Auto-generated method stub
		GSDAG gsdag=null;
		
		this.createGSDAG();
		
		try {
			gsdag = new GSDAG(this);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("A priori information: "+gsdag.getAPrioriInformation().toString2());
		
		if (gsdag!=null) return gsdag.getNumberOfPaths();
		else return 0;
	}
	
	
	public int getNumberOfDecisionsFirstBranch() {
		// TODO Auto-generated method stub
		GSDAG gsdag=null;
		
		this.createGSDAG();
		
		try {
			gsdag = new GSDAG(this);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (gsdag!=null) return gsdag.getNumDecisionsFirstBranch();
		else return 0;
	}
	
	/**
	 * @return The list of chance variables preceding to the first decision
	 */
	public NodeList getAPrioriInformation() {
		// TODO Auto-generated method stub
		GSDAG gsdag=null;
		NodeList aPriori;
		
		this.createGSDAG();
		
		try {
			gsdag = new GSDAG(this);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (gsdag!=null){
			aPriori = gsdag.getAPrioriInformation();
		}
		else{
			aPriori = null;
		}
		return aPriori;
	}

	
	/**
	 * All the parameters are output
	 * @param paths 
	 * @param numNodesAPriori
	 * @param sizeNodesAPriori
	 */
	public void getNumberOf(int paths,int numNodesAPriori,double sizeNodesAPriori) {
		// TODO Auto-generated method stub
		GSDAG gsdag=null;
		NodeList apriori;
		
		this.createGSDAG();
		
		try {
			gsdag = new GSDAG(this);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Number of paths
		paths = ((gsdag!=null)?gsdag.getNumberOfPaths():0);
		
		//Number of nodes before the first branch or decision
		apriori = gsdag.getAPrioriInformation();
		System.out.println("A priori information: "+apriori.toString2());
		numNodesAPriori = apriori.size();
		
		//Size of the nodes a priori
		sizeNodesAPriori = apriori.getSize();
		
		
		
	}
	
	
	/**
	 * @param conf Configuration over a subset of variables preceding to the first decision
	 * @return
	 */
	public double obtainProbability(Configuration conf){
		Bnet b;
		Evidence e;
		double prob;
		
	    b = this.constructABayesianNetworkFromUID();
	    e = new Evidence(conf);
	    
	    HuginPropagation hp = new HuginPropagation(b,e,"tables");
	    
	    prob = hp.obtainEvidenceProbability("yes");
        	
		return prob;
		
	}
	
	/**
	 * @return A bayesian network built from the UID by replacing decision nodes by chance nodes
	 * with even distribution
	 */
	private Bnet constructABayesianNetworkFromUID() {
		// TODO Auto-generated method stub
		CooperPolicyNetwork cpn;
		
		cpn= new CooperPolicyNetwork();
		
		cpn.setStructureOfPNFrom(this);
		
		cpn.setRelationsOfChanceNodesFrom(this);
		
		cpn.setRandomPoliciesAndRelationsOfDecisionNodesFrom(this);
		
		return cpn;
	}


	public boolean hasNonLinearGSDAGAndBranchAtBeginningChildrenOneDec(
			int minNumChildrenFirstBranch) {
		// TODO Auto-generated method stub
	GSDAG gsdag=null;
		
		this.createGSDAG();
		
		try {
			gsdag = new GSDAG(this);
		} catch (InvalidEditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ((gsdag.isATraditionalID()==false)&&(gsdag.hasBranchAtBeginning(minNumChildrenFirstBranch))&&(gsdag.chilrenOfRootHaveOnlyOneDecision())); 
	}


	/**
	 * To get the barren nodes.
	 * @return a barren node.
	 */

	public Node getBarrenUnobservableNode() {
		
		boolean barrenFounded=false;
		Node barrenNode=null;
		
		NodeList listOfNodes = getNodeList();
		
		for (int i=0;((i<listOfNodes.size())&&(barrenFounded==false));i++){
			Node auxNode;
			int kind;
			auxNode=listOfNodes.elementAt(i);
			kind=auxNode.getKindOfNode();
			if ((kind==Node.CHANCE)&&(this.isObservable(auxNode)==false)){
				if (auxNode.getChildren().size()==0){
					barrenFounded=true;
					barrenNode=auxNode;
					
				}
			}
		}
		return barrenNode;
	}


	public void compileByDefault() {
		// TODO Auto-generated method stub
		Propagation prop;
		AlgorithmsForUID alg;
		//Check how the observable/unobservable nodes are marked in the UID:
			//Way a) With "h" we mark the unobservable nodes (defined by Marta Vomlelova
			//Way b) With purpose = OBSERVABLE we mark the observable nodes
		if (areObservableNodesMarkedWithObservablePurpose()){
			markUnobservableChanceNodesWithHComment();
		}
		
		prop = new DynamicUID(this);
		((DynamicUID) prop).propagate();
		showResults(prop);
		
	}


	private void markUnobservableChanceNodesWithHComment() {
		// TODO Auto-generated method stub
	
		
		NodeList nodes = this.getNodeList();
		for (int i=0;(i<nodes.size());i++){
			Node iNode = nodes.elementAt(i);
			if (!(iNode.getPurpose().equalsIgnoreCase("Observable"))&&(iNode.getKindOfNode()==Node.CHANCE)){
				iNode.setComment("h");
			}
				
			
		}
		
		
	}


	private boolean areObservableNodesMarkedWithObservablePurpose() {
		// TODO Auto-generated method stub
		boolean marked = false;
		
		NodeList nodes = this.getNodeList();
		for (int i=0;(i<nodes.size())&&!marked;i++){
			Node iNode = nodes.elementAt(i);
			marked = (iNode.getPurpose().equalsIgnoreCase("Observable"));
				
			
		}
		return marked;
	}
	
	
	    } // End of class









