/* PCLearningCK.java */
package elvira.learning.constraints;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

import elvira.*;
import elvira.potential.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.learning.*;
import java.util.Random; //For select links in remove cycles

/**
 * This class implements the PC algorithm(Causation, Prediction and Search
 * 1993, Lectures Notes in Statistical 81 SV. Spirtes,Glymour,Sheines), using 
 * 3 types of contraints knowledge (existence, absence and partial order) in 
 * learning process. 
 *
 * Created: 9/10/2003
 *
 * @author fjgc@decsai.ugr.es
 * @version 1.0
 */

public class PCLearningCK extends Learning  {

   /**
     *  Constraint Knowledge class where store the constraints
     */
    private ConstraintKnowledge ck=null; 

    ConditionalIndependence input; // Input of the Learning Process.
    double delay;                    // Delay of the Learning Process.
    int numberOfTest;              // Number of test in the learning process.
    double setSizeCondMean;        // size mean of conditionating set.
    double levelOfConfidence;         // level of conf. for the C.I. tests
    Graph GUGo; //Graph G U Go

    /*---------------------------------------------------------------*/
    /**
     *  Basic constructor
     */
    public PCLearningCK(){
      setInput(null);
      setOutput(null);
      ck=null;
    }//end basic ctor.

    /*---------------------------------------------------------------*/
    /** 
      * Initializes a full unidirected graph (except with the no directed 
      * arcs form absence constraints) with the variables contained into
      * the Data Base of Cases cases. Also initializes the input of algorithm
      * PC as a Data Base of Cases and the PC output as a Bnet with the above
      * graph. 
      * @param constraints Used contraints int he learn process of the bnet 
      * @param cases The data base of discrete cases, used to learn the bnet
      */    
    public PCLearningCK(ConstraintKnowledge constraints, DataBaseCases cases) {
	Bnet b;
	Graph dag;
	NodeList nodes;
	LinkList links;
	Link link;
	Node nodet,nodeh;
	boolean directed = false;
	int i,j;
	//store the contraints
	this.ck=constraints;
	nodes = cases.getVariables();//.copy();
	//links = new LinkList();
	b = new Bnet();
	b.setKindOfGraph(2);
	dag = (Graph) b;

	for(i=0 ; i < (nodes.size()) ;i++)
	    try {
		dag.addNode(nodes.elementAt(i).copy());
	    } catch (InvalidEditException iee) {};

	nodes=dag.getNodeList();
	for(i=0 ; i < (nodes.size()-1) ;i++)
	    for(j=i+1 ; j<nodes.size() ;j++){
		nodet=(Node)nodes.elementAt(i);
		nodeh=(Node)nodes.elementAt(j);
		try {
		    dag.createLink(nodet, nodeh, directed);
		}catch (InvalidEditException iee) {};
	    }

	this.input = cases;
	setLevelOfConfidence(0.99);
	setOutput((Bnet)dag);
	
    }//end DataBaseCases ctor.

    /*---------------------------------------------------------------*/
    /** 
     * Initializes a full unidirected graph (except with the no 
     * directed arcs form absence constraints) with the variables contained into
     * the List of Nodes nodes. Also initializes the input of algorithm
     * PC as a Data Base of Cases and the PC output as a Bnet with the above
     * graph. It's 
     * very important that the variables contained in nodes are
     * a subset of the variables contained in the Data Bases of Cases.
     * @see DataBaseCases - method getVariables();
     * @param constraints Used contraints int he learn process of the bnet      
     * @param DataBaseCases cases. The data bases of discrete cases.
     * @param NodeList nodes. Must be a subset of variables of the cases.
     */    
    public PCLearningCK(ConstraintKnowledge constraints, DataBaseCases cases, NodeList nodes) {

    Bnet b;
    Graph dag;
    LinkList links;
    Link link;
    Node nodet,nodeh;
    boolean directed = false;
    int i,j;

    b = new Bnet();
    b.setKindOfGraph(2);
    dag = (Graph) b;
    for(i=0 ; i < (nodes.size()) ;i++)
       try {
          dag.addNode(nodes.elementAt(i));
       }catch (InvalidEditException iee) {};
    
    for(i=0 ; i < (nodes.size()-1) ;i++)
	for(j=i+1 ; j<nodes.size() ;j++){
	    nodet=(Node)nodes.elementAt(i);
	    nodeh=(Node)nodes.elementAt(j);
	    try {
	       dag.createLink(nodet, nodeh, directed);
	    }catch (InvalidEditException iee) {};
	}
    
    this.input = cases;
    setLevelOfConfidence(0.99);
    this.ck=constraints;//store the contraints
    setOutput((Bnet)dag);
    
    }//end DataBaseCases and NodeList ctor.

    /*---------------------------------------------------------------*/
    /** 
     * Initializes a full unidirected graph (except with the no directed arcs 
     * form absence constraints)with the variables contained into
     * the List of Nodes of the parameter input. Also initializes the input of
     * algorithm PC as a Graph and the PC output as a Bnet with the above
     * graph. 
     * @param constraints Used contraints int he learn process of the bnet  
     * @param Graph input. The input graph. (d-separation criterion).
     */    
    public PCLearningCK(ConstraintKnowledge constraints, Graph input) {

    Bnet b;
    Graph dag;
    NodeList nodes;
    LinkList links;
    Link link;
    Node nodet,nodeh;
    boolean directed = false;
    int i,j;
    
    nodes = input.getNodeList().duplicate();
    b = new Bnet();
    b.setKindOfGraph(2);
    dag = (Graph) b;
    for(i=0 ; i < (nodes.size()) ;i++)
       try {
	       dag.addNode(nodes.elementAt(i));
	    }catch (InvalidEditException iee) {};
    
    for(i=0 ; i < (nodes.size()-1) ;i++)
	for(j=i+1 ; j<nodes.size() ;j++){
	    nodet=(Node)nodes.elementAt(i);
	    nodeh=(Node)nodes.elementAt(j);
	    try {
	       dag.createLink(nodet, nodeh, directed);
	    }catch (InvalidEditException iee) {};
	    
	}
   
    this.input=input;
    setLevelOfConfidence(0.99);
    this.ck=constraints;//store the contraints
    setOutput((Bnet)dag);

    }//end Graph ctor.


    /*---------------------------------------------------------------*/
    /**
     * see  PCLearningCK(ConstraintKnowledge constraints,Graph input) and 
     * also PCLearningCK(ConstraintKnowledge constraints,DataBaseCases cases, NodeList nodes)
     * @param constraints Used contraints int he learn process of the bnet   
     * @param input It can be a Graph or a DataBaseCases (both implements ConditionalIndependece interface)
     * @param nodes Must be a subset of variables of the input
     */
    public PCLearningCK(ConstraintKnowledge constraints, ConditionalIndependence input, NodeList nodes) {
    Bnet b;
    Graph dag;
    LinkList links;
    Link link;
    Node nodet,nodeh;
    boolean directed = false;
    int i,j;
    NodeList nodesInput = input.getNodeList().duplicate();
    
    nodesInput = nodesInput.intersectionNames(nodes);   
    b = new Bnet();
    b.setKindOfGraph(2);
    dag = (Graph) b;
    
    for(i=0 ; i < (nodesInput.size()) ;i++)
      try {
	      dag.addNode(nodesInput.elementAt(i));
	   } catch (InvalidEditException iee) {};

    for(i=0 ; i < (nodesInput.size()-1) ;i++)
	for(j=i+1 ; j<nodesInput.size() ;j++){
	    nodet=(Node)nodesInput.elementAt(i);
	    nodeh=(Node)nodesInput.elementAt(j);
	    try {
	       dag.createLink(nodet, nodeh, directed);
	    } catch (InvalidEditException iee) {};
	    
	}
    
    this.input = input;
    setLevelOfConfidence(0.99);
    this.ck=constraints;//store the contraints
    setOutput((Bnet)dag);

    }//end ConditionalIndependence and NodeList ctor.

    /*---------------------------------------------------------------*/
    /**
     * This method implements the PC algorithm(Causation, Prediction and Search
     * 1993. Lectures Notes in Statistical 81 SV. Spirtes,Glymour,Sheines), using 
     * 3 types of contraints knowledge (existence, absence and partial order) in 
     * learning process.
     * Only the structure of the net is discovered.
     * levelOfConfidence indicates the level of
     * confidence for testing the conditional independences. 0.0 will be the
     * minor confidence.
     */
    public void learning(){
	Graph dag;
	int n,i,j,pos,jj;
	FiniteStates nodeX,nodeY;
	Hashtable sepSet;
	NodeList adyacenciesX,adyacenciesY,adyacenciesXY,adyacenciesYX,vars,subSet;
	LinkList linkList;
	Link link;
	Vector subSetsOfnElements,index;
	Enumeration en;
	boolean ok,encontrado=false,directed=false;
	Date D = new Date();
	delay = (double) D.getTime();

	///System.out.println("Reviento 3");
	index = new Vector();
	for(i=0 ; i< getOutput().getNodeList().size() ;i++){
	    sepSet = new Hashtable();
	    index.addElement(sepSet);
	}
	dag = (Graph)getOutput();


	Graph Ga=ck.getAbsenceConstraints();
	Graph unionGreGo=new Graph();
	try {
	    unionGreGo=this.unionGraph( this.ck.getPartialOrderConstraints(), this.ck.getRealExistenceConstraints(ck.getExistenceConstraints()) );
	}catch(InvalidEditException iee){
	    System.out.println("WARNING: The union of Gre and Go fails. ");
	};


	for (n=0 ; n <= dag.maxOfAdyacencies() ; n++)
	    for (i=0 ; i<(dag.getNodeList()).size();i++)
		for (j=0 ; j< (dag.getNodeList()).size() ;j++)
		    if(i!=j){
			if((j<i)&&(n==0)) continue;
			encontrado=false;
			nodeX=(FiniteStates)(dag.getNodeList()).elementAt(i);
			nodeY=(FiniteStates)(dag.getNodeList()).elementAt(j);

			adyacenciesX=dag.neighbours(nodeX);
			link=getOutput().getLinkList().getLinks(nodeX.getName(),nodeY.getName());
			if(link==null)
			    link=getOutput().getLinkList().getLinks(nodeY.getName(),nodeX.getName());
			
			if(adyacenciesX.getId(nodeY) !=-1){ // X and Y  are adyacent.
			    adyacenciesX.removeNode(nodeY);
			    
			    Node nodexunion=unionGreGo.getNodeList().getNode(nodeX.getName());
			    Node nodexabs=Ga.getNodeList().getNode(nodeX.getName());
			    NodeList parentsGa = Ga.parents(nodexabs);
			    NodeList siblingsGa = Ga.siblings(nodexabs);
			    
			    for (jj=0 ; jj< adyacenciesX.size() ;jj++){
				Node nodeZ = (Node) adyacenciesX.elementAt(jj);
				Node nodezunion=unionGreGo.getNodeList().getNode(nodeZ.getName());
				Node nodezabs=Ga.getNodeList().getNode(nodeZ.getName());
				unionGreGo.setVisitedAll(false);
				if ((unionGreGo.isThereDirectedPath(nodexunion,nodezunion)) || (parentsGa.getId(nodezabs) != -1) || (siblingsGa.getId(nodezabs) != -1)) {
				    adyacenciesX.removeNode(nodeZ);
				    jj--;
				}
			    }//enf for jj

			    if(adyacenciesX.size() >= n){
				subSetsOfnElements=adyacenciesX.subSetsOfSize(n);
				en = subSetsOfnElements.elements();
				while((!encontrado)&&(en.hasMoreElements()||n==0)){
				    if(n==0) subSet = new NodeList();
				    else subSet = (NodeList)en.nextElement();
				    for (int ii=0; ii<subSet.size(); ii++) {
					Node nodedbc=getInput().getNodeList().getNode(subSet.elementAt(ii).getName());
					subSet.setElementAt(nodedbc,ii);
				    }
				    Node nodex=getInput().getNodeList().getNode(nodeX.getName());
				      Node nodey=getInput().getNodeList().getNode(nodeY.getName());
				    
				    ok=getInput().independents(nodex,nodey,subSet,getLevelOfConfidence());            
				    
				    if(ok){
					
					try {
					    dag.removeLink(link);
					} catch (InvalidEditException iee) { 
					    System.out.println("Exception when remove "+link+" in the independence test phase");
					};
					
					pos = dag.getNodeList().getId(nodeX);
					sepSet = (Hashtable)index.elementAt(pos);
					sepSet.put(nodeY,subSet);
					pos = dag.getNodeList().getId(nodeY);
					sepSet = (Hashtable)index.elementAt(pos);
					sepSet.put(nodeX,subSet);
					encontrado = true;
				    }  
				    if(n==0) encontrado=true;
				}//end while    
			    }// end if adyancenciesX.size() >= n
			}// end if adyacenciesX.getId(nodeY) !=-1
			//}//end if search in existence constraints
		    }//end if(i!=j)


	//remove the edges form absence constraints
	LinkList absenceLinks=ck.getAbsenceConstraints().getLinkList();
	for (i=0; i < absenceLinks.size(); i++) {
	    Link l=absenceLinks.elementAt(i);
	    if (!l.getDirected())
		try {
		    l=new Link(dag.getNodeList().getNode(l.getTail().getName()), 
			       dag.getNodeList().getNode(l.getHead().getName()), l.getDirected());

		    pos=dag.getLinkList().getID(l.getTail().getName(),l.getHead().getName());
		    if (pos!=-1) 
			dag.removeLink(pos);
		    else {
			pos=dag.getLinkList().getID(l.getHead().getName(),l.getTail().getName());
			if (pos!=-1) dag.removeLink(pos);
		    }

		} catch (InvalidEditException iee) {
		    System.out.println("Exception when deletting "+l+" in the remove edges from Ga phase");
		}
	}//end for


	//Force the existence links
	LinkList existenceLinks=ck.getRealExistenceConstraints(ck.getExistenceConstraints()).getLinkList();
	for (i=0; i < existenceLinks.size(); i++) {
	    Link l=existenceLinks.elementAt(i);
	    //if ( l.getDirected() )
		try {
		    pos=dag.getLinkList().getID(l.getTail().getName(),l.getHead().getName());
		    if (pos!=-1) {
			dag.removeLink(pos);
			dag.createLink(dag.getNodeList().getNode(l.getTail().getName()), 
				       dag.getNodeList().getNode(l.getHead().getName()), l.getDirected());
		    } else {
			dag.createLink(dag.getNodeList().getNode(l.getTail().getName()), 
				       dag.getNodeList().getNode(l.getHead().getName()), l.getDirected());
		    }
		} catch (InvalidEditException iee) {
		    System.out.println("Exception when create "+l+" in the add Ge phase");
		}
	}//end for


  	//forcedConstraintsDirections
	forcedConstraintsDirections((Graph)dag);

	//compute the v-structures
	headToHeadLink((Graph)dag,index);

	//carry out the direction of the undirected links
	remainingLink((Graph)dag,index);
	extendOutput();

	//Store the expensed time
	D = new Date();
	delay = (((double)D.getTime()) - delay) / 1000;

    }//end learning method
    /*---------------------------------------------------------------*/
    /**
     * this method is used by the learning method, this method carry out the 
     * directions forced by the constraints knowledge.
     * @param Graph dag. An undirected Graph 
     * @return The dag with the forced directions
     */
    private Graph forcedConstraintsDirections(Graph dag){
	int i;
	Link link;
	Node nodeX,nodeY;

	//merge graph with the real existence constraints 
	Graph Ga=ck.getAbsenceConstraints();
	Graph Go=ck.getPartialOrderConstraints();

	//Forced Links by Ordering Constraints
        for(i=0;i< dag.getLinkList().size();i++){
	    link = (Link)dag.getLinkList().elementAt(i);
	    nodeX=dag.getNodeList().getNode(link.getTail().getName());
	    nodeY=dag.getNodeList().getNode(link.getHead().getName());
	    Link xy=new Link(link.getTail(), link.getHead(), true); //x->y
	    Link yx=new Link(link.getHead(), link.getTail(), true); //x<-y


	    if (!link.getDirected()) {
		//If  x--y in G AND x->..->y in Go => x--y goes to x->y
		Go.setVisitedAll(false);
    		if ( this.ck.getPath(Go.getNodeList().getNode(nodeY.getName()),Go.getNodeList().getNode(nodeX.getName()), Go, new LinkList())) {
		      try {
			  dag.removeLink(link);
			  if ( Ga.getLinkList().indexOf(xy)==-1 ) 
			      dag.createLink(nodeX,nodeY,true);
			  //start again from the beginning
			  i=-1;
		      } catch (InvalidEditException iee) {
			  System.out.println("Error in forcedConstraintsDirections: I can't replace x--y by x->y (Go))");
			  System.out.println("IEE Error in forcedConstraintsDirections: I can't replace "+
					     nodeX.getName()+"--"+nodeY.getName()+" by "+
					     nodeX.getName()+"->"+nodeY.getName()+" (Go)");
			  iee.printStackTrace();
		      };


		//If  x--y in G AND y->..->x in Go => x--y goes to y->x
		} else { 
		  Go.setVisitedAll(false);
		  if ( this.ck.getPath(Go.getNodeList().getNode(nodeX.getName()), Go.getNodeList().getNode(nodeY.getName()), Go, new LinkList())) {
		      try {
			  dag.removeLink(link);
			  if ( Ga.getLinkList().indexOf(yx)==-1 ) 
			      dag.createLink(nodeY,nodeX,true);
			  //start again from the beginning
			  i=-1;
		      } catch (InvalidEditException iee) {
			  System.out.println("Error in forcedConstraintsDirections: I can't replace x--y by x->y (Go))");
			  System.out.println("IEE Error in forcedConstraintsDirections: I can't replace "+
					     nodeX.getName()+"--"+nodeY.getName()+" by "+
					     nodeY.getName()+"->"+nodeY.getName()+" (Go)");
			  iee.printStackTrace();
		      };
                  }
		}
	    }//end ig link directed
	}//end for i

	//compute the G U Go U Gre union
	try{
	    GUGo=unionGraph(this.ck.getPartialOrderConstraints(),dag);
	}catch(InvalidEditException iee){
	    System.out.println("WARNING: The union of G and G_o fails. ");
	};
	
	//Forced Links by Absence Constraints
        for(i=0;i< dag.getLinkList().size();i++){
	    link = (Link)dag.getLinkList().elementAt(i);
	    nodeX=dag.getNodeList().getNode(link.getTail().getName());
	    nodeY=dag.getNodeList().getNode(link.getHead().getName());
	    Link xy=new Link(link.getTail(), link.getHead(), true); //x->y
	    Link yx=new Link(link.getHead(), link.getTail(), true); //x<-y

	    if (!link.getDirected()) {
		
		//If x--y in G AND x->y in Ga => x--y goes to y->x if NOT exists x->..->y in GUGo 	    
		if ( Ga.getLinkList().indexOf(xy)!=-1 ) {
		    orientLink(dag,link,nodeY,nodeX,false);
		    //start again from the beginning
		    i=-1;
		}

		//If x--y in G AND y->x in Ga => x--y goes to x->y if NOT exists y->..->x in GUGo 	    
		else if ( Ga.getLinkList().indexOf(yx)!=-1 ) {
		    orientLink(dag,link,nodeX,nodeY,false);
		    //start again from the beginning
		    i=-1;
		} 
	    }//end if directed
	}//end for i


	return dag;


    }//end forcedConstraintsDirections method

    /*---------------------------------------------------------------*/
    /**
     * this method is used by the learning method, this method carry out the 
     * directions forced by the existence constraints knowledge.
     * @param Graph dag. An undirected Graph 
     */
    private void forcedExistenceConstraintsDirections(Graph dag){
	NodeList nodes;
	LinkList links;
	Link link;
	int i;
	Node nodeX,nodeY;
	
	nodes = dag.getNodeList();
	links = dag.getLinkList();

        for(i=0;i< links.size();i++){
	    link = (Link) links.elementAt(i);
	    nodeX=nodes.getNode(link.getTail().getName());
	    nodeY=nodes.getNode(link.getHead().getName());

	    Link xy=new Link(link.getTail(), link.getHead(), true); //x->y
	    Link yx=new Link(link.getHead(), link.getTail(), true); //x<-y

	    if (!link.getDirected()) {

		Graph Ge=this.ck.getRealExistenceConstraints(this.ck.getExistenceConstraints());

		//If x->y in Ge => x--y goes to x->y
		if ( Ge.getLinkList().indexOf(xy)!=-1 ) {
		    try {
			dag.removeLink(link);
			dag.createLink(nodeX,nodeY,true);
			i=-1;
		    } catch (InvalidEditException iee) {
			System.out.println("Error in forcedExistenceConstraintsDirections: I can't replace x--y by x->y (Ge')");
			iee.printStackTrace();
		    };

		} 


		//If y->x in Ge => x--y goes to y->x
		else if ( Ge.getLinkList().indexOf(yx)!=-1 ) {
		    try {
			dag.removeLink(link);
			dag.createLink(nodeY,nodeX,true);
			i=-1;
		    } catch (InvalidEditException iee) {
			System.out.println("Error in forcedExistenceConstraintsDirections: I can't replace x--y by y->x (Ge')");
			iee.printStackTrace();
		    };
		}

	    }//end if getDirected
	}//end for i
    }//end forcedExistenceConstraintsDirections method
    /*---------------------------------------------------------------*/
    /**
     * this method is used by the learning method. This method carry out the 
     * v-structures (-->x<--).
     * @param Graph dag. An undirected Graph 
     * @param Vector index. Vector that stores the true conditional 
     * independence tests found in the learning process.
     */
    protected void headToHeadLink(Graph dag, Vector index){

	NodeList nodes,nbX,nbY,subDsep;
	Hashtable sepSet;
	LinkList links;
	Link delLink,newLink;
	int i,j,z,pos;
	Node nodeX,nodeY,nodeZ;
	boolean directedXY,directedYZ;

	LinkList absenceLinks=ck.getAbsenceConstraints().getLinkList();

	nodes = dag.getNodeList();
	links = dag.getLinkList();

	for(i=0;i< nodes.size();i++){
	    nodeX = (Node) nodes.elementAt(i);
	    nbX = dag.neighbours(nodeX);

	    for(j=0 ; j< nbX.size() ; j++){
		nodeY = (Node) nbX.elementAt(j);
		nbY = dag.neighbours(nodeY);
		nbY.removeNode(nodeX);

		for(z=0 ; z < nbY.size() ;z++){
		    nodeZ = (Node) nbY.elementAt(z);
		    if( nbX.getId(nodeZ) == -1 ){  // No adyacentes X y Z
			pos = nodes.getId(nodeX);
			sepSet = (Hashtable)index.elementAt(pos);
			subDsep = (NodeList)sepSet.get(nodeZ);
			///{ System.out.println("Testing 3 nodes " +subDsep.size() );}
			    if((subDsep!=null)&&(subDsep.getId(nodeY) == -1)){
				///System.out.println("Directing ");
				delLink = links.getLinks(nodeX.getName(),nodeY.getName());
				if(delLink == null){
				    delLink = links.getLinks(nodeY.getName(),nodeX.getName());
				}
				directedXY = delLink.getDirected();
				
				if (!directedXY) {
				    ///System.out.println("Intento crear (si no hay ciclos ni Go) "+nodeX.getName()+"->"+nodeY.getName());
				    orientLink(dag,delLink,nodeX,nodeY,true);

				}
				
				
				delLink = links.getLinks(nodeY.getName(),nodeZ.getName());
				if(delLink == null){ 
				    delLink = links.getLinks(nodeZ.getName(),nodeY.getName());
				}
				directedYZ = delLink.getDirected();
				if (!directedYZ) {
				    ///System.out.println("Intento crear (si no hay ciclos ni Go) "+nodeY.getName()+"->"+nodeZ.getName());
				    orientLink(dag,delLink,nodeZ,nodeY,true);
				}
			    }
			    
		    }
		}//end for z
	    }//end for j
	}//end for i
    }//end headToHeadLink method
    
    /*---------------------------------------------------------------*/
    /**
     * This method carry out the direction of the remaining link that can
     * be computed.
     * @param Graph @see above method.
     * @param Vector @see above method.
     */
    private void remainingLink(Graph dag,Vector index){

	boolean change,change2,oriented,skip;
	int i,j,k;
	Link linkAB,linkBC,linkCB,linkBW;
	Node nodeA,nodeB,nodeC,nodeW;
	NodeList nbTail,nbHead,nbC,children;
	Vector acc;
	LinkList links;
	LinkList absenceLinks=ck.getAbsenceConstraints().getLinkList();
	
	do{ 
	    change2=false;
	    do{
		change = false;
		for(i=0 ;i<dag.getLinkList().size();i++){
		    linkAB = (Link) dag.getLinkList().elementAt(i);
		    nodeA = (Node) linkAB.getTail();
		    nodeB = (Node) linkAB.getHead();

		    if(linkAB.getDirected()){   // A-->B
			//      System.out.println("Direced arc ");
			nbHead = dag.siblings(nodeB);
			//   nbHead.removeNode(nodeA);
			//   nbTail = dag.neighbours(nodeA);
			//   nbTail.removeNode(nodeB);
			for(j=0 ; j< nbHead.size() ; j++){
			    nodeC = (Node) nbHead.elementAt(j);
			    //        System.out.println("Vecino de cabeza "+ nodeC.getName());
			    nbC = dag.neighbours(nodeC);
			    if(nbC.getId(nodeA) == -1){    
				linkCB = dag.getLinkList().getLinks(nodeC.getName(),nodeB.getName());
				if(linkCB== null)  
				     linkCB =dag.getLinkList().getLinks(nodeB.getName(),nodeC.getName());
				
				if  (!linkCB.getDirected()) {
				    orientLink(dag,linkCB,nodeB,nodeC,true);//dag.orientLinkDag(linkCB,nodeB,nodeC);
				    change = true;
				}
				
			    }
			}//end for j
		    } else { // A -- B Non-oriented link
			oriented = false;
			
			if (dag.isThereDirectedPath(nodeA,nodeB)) {
			    ///System.out.println("Orienting from "+ nodeA.getName() + " to "+ nodeB.getName() + "directed path");
			    orientLink(dag,linkAB,nodeA,nodeB,true);
			    change = true;
			    oriented = true;
			}
			if((dag.isThereDirectedPath(nodeB,nodeA)) && (!oriented)){
			    ///System.out.println("Orienting from "+ nodeB.getName() + " to "+ nodeA.getName() + "directed path");
			    orientLink(dag,linkAB,nodeB,nodeA,true);
			    change = true;
			    oriented = true;
			    
			}

			if (!oriented){
			    nbHead =  dag.siblings(nodeB);
			    nbHead.removeNode(nodeA);
			    for(j=0 ; j< nbHead.size() ; j++){
				nodeC = (Node) nbHead.elementAt(j);
				// System.out.println("Vecino de B "+ nodeC.getName());
				nbC = dag.neighbours(nodeC);
				if(nbC.getId(nodeA) == -1){   
				    for(k=0 ; k< nbHead.size() ; k++){
					if ((k!=j)){
					    skip = false;
					    children = dag.children(nodeA);
					    nodeW = (Node) nbHead.elementAt(k);
                            
					    if(children.getId(nodeW) == -1){skip=true;}
					    
					    linkBC = dag.getLinkList().getLinks(nodeB.getName(),nodeC.getName());
					    if(linkBC == null) {linkBC = dag.getLinkList().getLinks(nodeC.getName(),nodeB.getName());}
					    if (linkBC.getDirected()) {skip=true;}
					    
					    if (!skip){
						children = dag.children(nodeC);
						if(children.getId(nodeW) == -1){
						    linkBW = dag.getLinkList().getLinks(nodeB.getName(),nodeW.getName());
						    
						    if(linkBW == null) 
							linkBW = dag.getLinkList().getLinks(nodeW.getName(),nodeB.getName());
						    if (!linkBW.getDirected()) {
							orientLink(dag,linkBW,nodeB,nodeW,true);
							change = true;
							///System.out.println("Orienting from "+ nodeB.getName() + " to "+ nodeW.getName() + " rule 3");
						    }
						    skip=true;  
						}
					    }

					    if (!skip){
						children = dag.children(nodeW);
						if (children.getId(nodeC) == -1) {
						    orientLink(dag,linkBC,nodeB,nodeC,true);
						    ///System.out.println("Orienting from "+ nodeB.getName() + " to "+ nodeC.getName() + " rule 4");
						    change = true;
						    
						}
					    }  
					}
				    }//end for k
				}
			    }//end for j
			}
		    }
		}//end for i
	    }while (change);
    

	    for(i=0 ;i<dag.getLinkList().size();i++){
		linkAB = (Link) dag.getLinkList().elementAt(i);
		nodeA = (Node) linkAB.getTail();
		nodeB = (Node) linkAB.getHead();
		//    System.out.println("Testing nodes " + nodeA.getName() + nodeB.getName());
		if(!linkAB.getDirected()){
		    orientLink(dag,linkAB,nodeA,nodeB,true);
		    change2=true;
		    break;
		}
	    }//end for i
	}while (change2);     
 
	
    }//end reaminingLinks method

    /*---------------------------------------------------------------*/
    /**
     * This method construct a consistent extension of a partially oriented
     * graph (usually, the output of the PC algorithm). This extension is 
     * achieved having into account the index nodes of the param nodes.
     * @param NodeList nodes. A set of sorted Nodes.
     */
    public void extendOutput(NodeList nodes){

	LinkList links,linksOutput;
	int i,posTail,posHead;
	Link link,newLink;
	Graph dag;
	
	dag = (Graph) getOutput();
	links = linkUnOriented();

	for(i=0 ; i< links.size() ; i++){
	    link = links.elementAt(i);
	    posTail = nodes.getId(link.getTail());
	    posHead = nodes.getId(link.getHead());
	    if(posTail < posHead)
		link.setDirected(true);
	    else 
		try {
		    dag.removeLink(link);
		    dag.createLink(link.getHead(),link.getTail());
		}
		catch (InvalidEditException iee) {};
	}
    }//end extendOutput with a NodeList method


    /*---------------------------------------------------------------*
    /**
     * Sorts the links of a directed acyclic graph using an ancestral
     * order of its nodes (Chickering's sort algorithm).
     * @param withoutSort links without sort
     * @param nodesOrdered an ancestrally ordered <code>NodeList</code>.
     * @return an ordered <code>Vector</code> with the graph links.
     */
    public Vector sortLinks (LinkList withoutSort, NodeList nodesOrdered) {
  
	int i, j, k, topVal, maxTopVal;
	Node y;
	Vector sorted = new Vector();
	LinkList selected;

	i = 0;
	while (withoutSort.size() != 0) {
	    y = nodesOrdered.elementAt(i);
	    selected = new LinkList();
	    for (j=0 ; j<withoutSort.size() ; j++)
		if ((withoutSort.elementAt(j).getHead().compareTo(y) == 0))
		    selected.insertLink(withoutSort.elementAt(j));
	    withoutSort = withoutSort.difference(selected);
	    
	    while (selected.size() != 0) {
		maxTopVal = 0;
		k = 0;
		for (j=0 ; j<selected.size() ; j++) {
		    topVal = nodesOrdered.getId(selected.elementAt(j).getTail().getName());
		    if (topVal > maxTopVal) {
			maxTopVal = topVal;
			k = j;
		    }
		}
		sorted.addElement(selected.elementAt(k));
		selected.removeLink(k);
	    }
	    i++;
	}
	
	return sorted;	
    }

    /*---------------------------------------------------------------*
    /**
     * This method construct a consistent extension of a partially oriented
     * graph (usually, the output of the PC algorithm).
     */
    public void extendOutput(){
	//LinkList linksUnsorted;
	Vector links;
	Link link;
	Graph dag;
	NodeList cola;
	Node node,hermano;

	cola = new NodeList();
	dag = (Graph) getOutput();

	//linksUnsorted = linkUnOriented();
	links=sortLinks(linkUnOriented(),this.ck.getPartialOrderConstraints().topologicalOrder());//Vector());
	//links=dag.sortLinks(this.ck.getPartialOrderConstraints().topologicalOrder());

	while(links.size()>0){
	    link = (Link)links.elementAt(0);
	    node = link.getTail();
	    for( ; dag.siblings(node).size()>0 ;){
		hermano = dag.siblings(node).elementAt(0);
		cola.insertNode(hermano);
		link = dag.getLink(node,hermano);
		if(link==null) link=dag.getLink(hermano,node);
		
		orientLink(dag,link,node,hermano,true);
	    }
	    
	    while(cola.size()>0){
		node = cola.elementAt(0);
		for(; dag.siblings(node).size()>0;){
		    hermano = dag.siblings(node).elementAt(0);
		    cola.insertNode(hermano);
		    link = dag.getLink(node,hermano);
		    if(link == null) link=dag.getLink(hermano,node);
		    orientLink(dag,link,hermano,node,true);
		} 
		cola.removeNode(node);
	    }
	    // links = linkUnOriented();
	    links=sortLinks(linkUnOriented(),this.ck.getPartialOrderConstraints().topologicalOrder());//Vector());
	    //links=dag.sortLinks(this.ck.getPartialOrderConstraints().topologicalOrder());
	}
	
    }//end extendOutput without params method

    /*---------------------------------------------------------------*/
    /**
     * This method carry out the link unoriented of the PC output as a Link
     * List.
     * @return LinkList. A Link list of unoriented links.
     */
    public LinkList linkUnOriented(){
	LinkList links, linksUO;
	int i;
	Link link;

	linksUO = new LinkList();
	links = getOutput().getLinkList();
	
	for(i=0 ; i< links.size() ; i++){
	    link = links.elementAt(i);
	    if(!link.getDirected())
		linksUO.insertLink(link);
	}
	return linksUO;
    }//end LinkUnOriented method
    /*---------------------------------------------------------------*/
    /**
     * This method computes the union Graph of two graphs where we have 
     * to comply that (x->y) U (x--y) = (x->y)
     * @param Graph Go. First graph 
     * @param Graph G. Graph that we are learning
     * @return The Union of the two graphs, that is, G U Go
     */
    private Graph unionGraph(Graph Go, Graph g) throws InvalidEditException {

	//look if there is a directed path from y to x in G U Go
 	Graph ordergraph=new Graph(Go); //Go
	Graph uniongraph = ordergraph.union( new Graph(g.getNodeList().duplicate(),
						       g.getLinkList().duplicate(),Graph.MIXED)); // G U Go

	//We have to comply that (x->y) U (x--y) = (x->y)
	LinkList unionlinks=(uniongraph.getLinkList()).copy();
	LinkList orderlinks=(ordergraph.getLinkList()).copy();
	for (int i=0;i<unionlinks.size();i++) {
	    Link nodirected=unionlinks.elementAt(i);//x--y ?
	    //if the link it's x--y, we search x->y or x<-y in 
	    if ( !nodirected.getDirected() ) {
		Link one=new Link(nodirected.getTail(), nodirected.getHead(), true); //x->y
		Link two=new Link(nodirected.getHead(), nodirected.getTail(), true); //x<-y
		for (int j=0;j<orderlinks.size();j++)
		    //if exists, x->y (or y->x), we delete x--y and add x->y (or y->x)
		    if ( one.equals(orderlinks.elementAt(j)) ) {
			uniongraph.removeLink(nodirected);
			uniongraph.createLink(nodirected.getTail(), nodirected.getHead(), true);			
			break;
		    } else if (two.equals(orderlinks.elementAt(j)) ) {
			uniongraph.removeLink(nodirected);
			uniongraph.createLink(nodirected.getHead(), nodirected.getTail(), true);
			break;
		    }
	    }
	}//end for i

	return uniongraph;
    }
    /*---------------------------------------------------------------*/
    /**
     * This method create a link in the graph and in the GUGo structure.
     * @param Graph G. Graph that we are learning
     * @param Link l. The link to create.
     */
    private void createLink(Graph g, Link l) throws InvalidEditException {
	Node tail;
	Node head;

	//Get the nodes of the Link
	tail=GUGo.getNodeList().getNode(l.getTail().getName());
	head=GUGo.getNodeList().getNode(l.getHead().getName());

	//create the link in GUGo (if it's necessary)
	int pos=this.GUGo.getLinkList().indexOf(new Link(tail,head,l.getDirected()));
	if (pos<0) 
	    this.GUGo.createLink(tail,head,l.getDirected());

	//create the link in G
	tail=g.getNodeList().getNode(l.getTail().getName());
	head=g.getNodeList().getNode(l.getHead().getName());
	g.createLink(tail,head,l.getDirected());
    }
    /*---------------------------------------------------------------*/
    /**
     * This method orient a unoriented link with a proposed orientation
     * if it isn't consistent with the partial ordering constraints, we 
     * use the other orientation.
     * @param Graph G. Graph that we are learning
     * @param Link unorientedLink. The Link of to orient.
     * @param Node tail. The node proposed as tail in the orientation of the link.
     * @param Node head. The node proposed as head in the orientation of the link.
     * @para boolean invertibles. This var indicates if the link can ne inverte 
     *          when the proposed directions isn't allowed
     */
    private void orientLink(Graph g, Link unorientedLink, Node tail, Node head, boolean invertible) {
        boolean insert=true;

	//Check if the proposed direction is consistent with the ordering constraints.
	GUGo.setVisitedAll(false);
	if ( GUGo.isThereDirectedPath(GUGo.getNodeList().getNode(head.getName()),GUGo.getNodeList().getNode(tail.getName())) ) {

	    //It isn't consitent and it's invertible, we try the other direction
	    if (invertible) {
  	      Node aux=tail;
  	      tail=head;
  	      head=aux;
	    

  	      if ( GUGo.isThereDirectedPath(GUGo.getNodeList().getNode(head.getName()),GUGo.getNodeList().getNode(tail.getName())) ) {
		System.out.println("DIRECCION INVERSA A LA ORIGINAL TAMPCO ESTA NO PERMITIDA en enlace "+tail.getName()+"->"+head.getName());
		//System.out.println("Grafo G");
		//print(g);
		//System.out.println("Grafo GUGo");
		//print(GUGo);
		System.exit(-2);
		}
            } else {
              insert=false;
            }//end if-else invertible
	}



	//remove the link in GUGo 
	try {
	Link aux=GUGo.getLinkList().getLinks(tail.getName(),head.getName());
        if (aux!=null)GUGo.removeLink(aux);
        
        //remove the link in G
        g.removeLink(unorientedLink);

        //create the link in GUGo y G
	  if (insert) {
  	    tail=GUGo.getNodeList().getNode(tail.getName());
  	    head=GUGo.getNodeList().getNode(head.getName());
  	    GUGo.createLink(tail,head,true);

  	    tail=g.getNodeList().getNode(tail.getName());
  	    head=g.getNodeList().getNode(head.getName());
  	    g.createLink(tail,head,true);
  	    }
	} catch (InvalidEditException iee) {
	    System.out.println("InvalidEditExcepction en orientLink");
	    iee.printStackTrace();
	    System.out.println("Intento orientar "+unorientedLink+" como "+tail.getName()+"->"+head.getName());
	}
    }

    /*---------------------------------------------------------------*/
    /** Access methods ***/

    /*---------------------------------------------------------------*/
    public double getLevelOfConfidence(){
	return levelOfConfidence;
    }
    /*---------------------------------------------------------------*/
    public void setLevelOfConfidence(double  level){
	levelOfConfidence = level;
    }

    /*---------------------------------------------------------------*/
    public void setInput(ConditionalIndependence input){
	this.input = input;
    }

    /*---------------------------------------------------------------*/
    public ConditionalIndependence getInput(){
	return input;
    }

    /*---------------------------------------------------------------*/
    /*---------------------------------------------------------------*/
    /*---------------------------------------------------------------*/
    /**
     * For performing tests
     */
    public static void main(String args[]) throws ParseException, IOException { 
	ConstraintKnowledge ck=null;
	K2Metrics metric;
	Bnet net, baprend;
	FileWriter f2;
	net = null;
	
	//Check the arguments
	if(args.length < 6){
	    System.out.println("too few arguments: Usage: file.dbc numberCases out.elv (for saving the result)  existenceConstrains.elv absenceContrints.elv partialOrdercontraints.elv [file2.elv (true net to be compared)]");
	    System.exit(0);
	}
	
	//Get the arguments
	//Read the dbc
	System.out.println("Reading DataBaseCases "+args[0]);
	FileInputStream f = new FileInputStream(args[0]);
	DataBaseCases cases = new DataBaseCases(f);
	cases.setNumberOfCases(Integer.valueOf(args[1]).intValue());
	//Read the constraints
	System.out.println("Reading Constraints "+args[3]+", "+args[4]+" and "+args[5]);
	try {  
	    ck= new ConstraintKnowledge(args[3],args[4],args[5]);
	} catch (InvalidEditException e){};
	//Read the true net (if exists)
	if(args.length > 6){ 
	    System.out.println("Reading true net to compare: "+args[6]);      
	    FileInputStream fnet = new FileInputStream(args[6]);
	    net = new Bnet(fnet); 
	} 
	
	//Build a new PCLearningCK class and learn a bnet
	PCLearningCK outputNet2 = new PCLearningCK(ck,cases);
	outputNet2.setLevelOfConfidence(0.99);
	outputNet2.learning();
	
	//Build a new DELearning class and learn a bnet using the bnet learned with PCLearningCK
	DELearning outputNet3 = new DELearning(cases,outputNet2.getOutput());
	outputNet3.learning();
	
	//Metric to use for evaluete the output bnet
	metric = new K2Metrics(cases);
	
	//Show results
	double d = cases.getDivergenceKL(outputNet3.getOutput());
	System.out.println("KL Divergence = "+d);
	System.out.println("Bayes score for output net: "+metric.score(outputNet2.getOutput()));
	System.out.println("time: "+outputNet2.delay);
	
	//Save the bnet learned with DELearning
	f2 = new FileWriter(args[2]);
	baprend = (Bnet)outputNet3.getOutput();
	baprend.saveBnet(f2);
	f2.close();
	
	
	//compare learned bnet with true bnet
	if(args.length > 6){
	    FileInputStream fnet = new FileInputStream(args[6]);
	    net = new Bnet(fnet);
	    double d2 = cases.getDivergenceKL(net);
	    
	    System.out.println("True net divergence: "+d2);
	    System.out.println("Real divergence: "+(d2-d));
	    LinkList addel[] = new LinkList[3];
	    addel = outputNet2.compareOutput(net);
	    System.out.print("\nNumber of added arcs: "+addel[0].size());
	    System.out.print(addel[0].toString());
	    System.out.print("\nNumber of removed arcs: "+addel[1].size());
	    System.out.print(addel[1].toString());
	    System.out.println("\nNumber of bad oriented arcs: "+addel[2].size());
	    System.out.print(addel[2].toString());
	    System.out.print("\nNot oriented arcs: ");
	    System.out.print(outputNet2.linkUnOriented().toString());
	}  
    }//end main method
    
} // End PCLearningCK class

















