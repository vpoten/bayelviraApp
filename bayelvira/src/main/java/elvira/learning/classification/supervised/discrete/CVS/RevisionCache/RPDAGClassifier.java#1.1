/* RPDAGClassifier */

package elvira.learning.classification.supervised.discrete;

import elvira.learning.classification.Classifier;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.ConfusionMatrix;

import elvira.*;
import elvira.learning.*;

import elvira.parser.*;
import elvira.database.*;

import java.util.*;
import java.io.*;

/**
 * Class RPDAG. Allows to travel in the rpdag space (graph typed as MIXED)
 * The basic structure of a rpdag contains directed and undirected links,
 * these represent any orientation posible, whenever it does not contain
 * additional head to head (h2h) nodes.
 * Referred to paper: Searching for Bayesian network in the space of restricted
 * acyclic partially directed graphs. in JAIR
 * The operation to travel in the space is add and remove conexions. The
 * inversion of an arc is not considereded.
 *
 * Para compilar: javac -O elvira/learning/classification/supervised/discrete/RPDAGClassifier.java
 * Para ejecutar: java -Xms128m -Xmx512m elvira/learning/classification/supervised/discrete/RPDAGClassifier $CASTLEHOME/asia2.dbc $CASTLEHOME/asiaS2R.elv 0 BDe
 *
/ *
/ * @author Luis Miguel de Campos  (lci@decsai.ugr.es)
 * @author Silvia Acid  (acid@decsai.ugr.es)
 * @since 12/07/2004
 */

public class RPDAGClassifier extends MarkovBlanketLearning {

    Bnet   currentBnet;

    /**
     * Metric to score bnets
     */
    Metrics metric;

 public    long numIndEval = 0;
 public    long numIndTot = 0;
 public    long numDetectedCycle = 0;
 public    long numTestCycle = 0;
 public    long numInd2Explore = 0;


    /*---------------------------------------------------------------*/

    /* Access methods */

    /*---------------------------------------------------------------*/
    /** 
     * Initializes/store the metric for scoring bnets.
     * @param Metric metric The metric for scoring bnets.
     */    
    public void setMetrics(Metrics metric){
	this.metric = metric;
    }
    /*---------------------------------------------------------------*/
    /** 
     * Return the used metric for scoring bnets.
     * @return the metric for scoring bnets.
     */
    public Metrics getMetrics(){
	return this.metric;
    }

   /*---------------------------------------------------------------*/
    /** Basic ctor.  Creates an empty bnet. 
     */

    public  RPDAGClassifier() {
	setInput(null);
	metric= null;
	setOutput(null);
	currentBnet=new Bnet();
        currentBnet.setKindOfGraph(Graph.MIXED);
    }

    /**
     * Constructor.
     * Initializes the input of RPDAG algorithm as a Data Base of
     * Cases and the RPDAG output as a Bnet with a empty graph.
     * @param DataBaseCases cases. The data bases of discrete cases.
     * @param int classvar number of the variable to classify
     * @param boolean lap. To apply the laplace correction in the 
     *                parameter learning process.
     * @param  metric parameter <code>Metrics</code> given in the command line. Default BDeMetrics.
     */

    public  RPDAGClassifier(DataBaseCases cases, int classvar, boolean lap, Metrics metric){
	super(cases,classvar,lap);
	this.metric = metric;

	NodeList vars = cases.getNodeList();
	currentBnet=new Bnet();

	for(int i=0; i< vars.size(); i++){
	    try{
		currentBnet.addNode(vars.elementAt(i));
	    }catch(InvalidEditException iee){};
	}
        currentBnet.setKindOfGraph(Graph.MIXED);
    } 

   /**
    * This method is used to know if the introduction of an 
    * undirected link between a and b, creates an undirected cycle
    * @return True if there is a cycle (output in brokens, the 
    * closer two nodes to the extrems in the path)
    * False in other case
    * @param a an extrem, @param u the other,  @param b repeat second argument 
    * adaptation of isThereDirectedPath incorrect?
    */

  private boolean hasUndirectedCycle(Graph g,Node a, Node u, Node b, Vector brokens){
    
        if (a.equals(u)) {
	    brokens.addElement(b);
            return true;
        } 
        else {
                  Enumeration e=g.siblings(u).elements();
                  Node ady;
                  boolean found=false;

                  while ((!found) && (e.hasMoreElements())){
                     ady = (Node) e.nextElement();
		     if (!ady.equals(b)) // node where it comes from
			 if (!ady.getVisited()) {
			     ady.setVisited(true);
			     found=hasUndirectedCycle(g,a,ady,u,brokens);
			 }
		     if ((found) && (u.equals(b))) 
			 brokens.addElement(ady);
		  }
                  return found;
            }
    }

   /**
    * This method is used to know if the introduction of an arc
    * between a and b, may creates a directed cycle (after completion of
    * the dag) through  undirected links
    * @return True if there is a cycle (output in brokens, the 
    * closer node to the extrem in the path)
    * False in other case
    * @param a tail, @param u head of the candidate arc
    * @param b repeat second argument 
    */

private  boolean hasMixedCycle(Graph g, Node a, Node u, Node b, Vector brokens) {

        if (a.equals(u)) 
            return true;
	else {
	    Enumeration e1=g.siblings(u).elements();
	    Enumeration e2=g.children(u).elements();
	    Node ady1,ady2;
	    boolean found=false;
            
	    while ((!found) && (e1.hasMoreElements())){
                     ady1 = (Node) e1.nextElement();
		     if (!ady1.equals(b)) // node where it comes from
			 if (!ady1.getVisited()) {
			     ady1.setVisited(true);
			     found=hasMixedCycle(g,a,ady1,u,brokens);
			 }
		     if ((found) && (u.equals(b))) {
			 brokens.addElement(ady1); 
		     }
	    }

	    while ((!found) && (e2.hasMoreElements())){
                     ady2 = (Node) e2.nextElement();
		     if (!ady2.equals(b)) 
			 if (!ady2.getVisited()) {
			     ady2.setVisited(true);
			     found=hasMixedCycle(g,a,ady2,u,brokens);
			 }
		     if ((found) && (u.equals(b))) {
			 brokens.addElement(ady2); 
		     }
	    }
	    return found;
	}
} /* hasMixedCycle */

    /* --------------------------------------------- */
    /* Implementation of the RPDAG algorithm traveling
    /* in the space of equivalent graph structures 
    /* --------------------------------------------- */
    public  void learning(){
	
	double fitness,fitnessNew,fitnessBnet;
	boolean OkToProceed, stop;
	int op, typeLink,typeTest;
	Node nodeX,nodeY;

	
System.out.println("With the bnet: "+currentBnet.getLinkList().toString());
    fitnessBnet = metric.score(currentBnet);
    fitness = fitnessBnet;
System.out.println("initial fitness :"+ fitnessBnet);

    OkToProceed = true;
    while(OkToProceed){
	int [] vvals=new int [3];
	Vector vvars=new Vector();
	fitnessNew =  maxScore(vvals,vvars,currentBnet);

	op=vvals[0];
	nodeX = (Node)vvars.elementAt(0);
	nodeY = (Node)vvars.elementAt(1);
	fitness +=  fitnessNew;
	if(fitness > fitnessBnet) { // OkToProceed
	    fitnessBnet = fitness;
	    
	    if (op==0){ // remove link

		Link link = currentBnet.getLink(nodeX,nodeY);
		if (link != null) {
		    currentBnet.removeLinkrepairRPDAG(link);
		    //System.out.println("*** remove link: "+nodeX.getName()+"--"+nodeY.getName());

		}
		else{
		    System.out.println("link to delete not found!!!!");
		    OkToProceed=false;
		}
	    }
	    else { // include new link
		
		typeLink = vvals[1];
		typeTest = vvals[2];
		switch (typeLink){
		case 0: try{
		            currentBnet.createLink(nodeX,nodeY,false);  /*X-Y*/
		        }catch(InvalidEditException iee){};
		    //System.out.println("*** Put  link: "+nodeX.getName()+"--"+nodeY.getName());
		    break;
		case 1: try{
			    currentBnet.createLink(nodeX,nodeY,true);  /*X->Y*/
			}catch(InvalidEditException iee){};
		    //System.out.println("*** Put  link: "+nodeX.getName()+"->"+nodeY.getName());
		    break;

		case 2: try{
			    currentBnet.createLink(nodeX,nodeY,true);  /*X->Y + child*/
			}catch(InvalidEditException iee){};
		        currentBnet.orientInCascade(nodeY);
			//System.out.println("*** Put  link: "+nodeX.getName()+"->"+nodeY.getName()+"e children");
		    break;
		default:
			Link clink = (Link)currentBnet.getLink((Node) vvars.elementAt(2),nodeY);
			try{
			    currentBnet.createLink(nodeX,nodeY,true);  /*X->Y + h2h*/
			    currentBnet.removeLink(clink);
			    currentBnet.createLink((Node) vvars.elementAt(2),nodeY,true);  /*h2h->Y*/
			}catch(InvalidEditException iee){};
			if (typeTest==1)
			    currentBnet.orientInCascade(nodeY);

			//System.out.println("*** Put  link: "+nodeX.getName()+"->"+nodeY.getName()+"<-"+((Node)vvars.elementAt(2)).getName());
		} /* swith */	
	    }
	    // There is improvement
	}
	else OkToProceed=false;

    } /* while to proceed */
    System.out.println("***FitnessBnet Final: "+fitnessBnet);
    System.out.println("Number of arcs "+currentBnet.getLinkList().size());

    currentBnet.extendRPDAG();
    setOutput(currentBnet);  

    } /* end of learning */




    /**
     * This methods is private. It is used for searching the link operation
     * that maximize the score metric. I always finds a maximum.
     * @param vvalR a vector with op (0-delete, 1-add), the type and the tests for the link
     * @param vvarsR the list of nodes involved (2 o 3 nodes)
     * @param currentBnet. The current dag
     * @return double higher fitness found.
     */

    private double maxScore( int [] vvalR, Vector vvarsR,Bnet currentBnet){
	
	int l,op;
	boolean putlink, fixed;
	Link link;
	NodeList vars,paNY,varsR=null;
	Node nodeX, nodeY,nodX,nodY, h2hAux;
	int [] theConfs = new int [7]; 
	double val,max = (-1.0/0.0);
	int sz=currentBnet.getNodeList().size();
		
	for(int i=0 ; i< sz ; i++){

	 nodX=(Node) currentBnet.getNodeList().elementAt(i);
	 for(int j=i+1 ; j< sz ; j++) {

	      nodY=(Node) currentBnet.getNodeList().elementAt(j);

	      if ((link = (Link) currentBnet.getLink(nodX,nodY)) == null)
		if ((link = (Link) currentBnet.getLink(nodY,nodX)) == null)
		    op=1; // to include
		else op=0;// to remove
	      else op=0;  // to remove
	     
	      if (op==1){   // to include link

	       rpdagNeighb(currentBnet,nodX,nodY,theConfs);
	       fixed = false;
	       for (l=1;l<theConfs[0];) {
		    numIndTot++;
		   if (theConfs[l]<0){ //config negative changes head & tail
		       nodeX= nodY; 
		       nodeY= nodX;
		   }
		   else{
		       nodeX= nodX;
		       nodeY=nodY;
		   } 
		   putlink=true; // apriori unless some detected troubles
		   h2hAux = null;
		   double valOld=0.0;
		   vars = new NodeList();

		switch (Math.abs(theConfs[l])){
		case 0:{
		    Vector brokens = new Vector();
		    currentBnet.setVisitedAll(false);
		    if (theConfs[l+1] == 2)
			numTestCycle++;

		    if ((theConfs[l+1] == 2)  && hasUndirectedCycle(currentBnet,nodeX,nodeY,nodeY, brokens)){   // only state C
			putlink=false;
			numDetectedCycle++;
			int e1=currentBnet.siblings(nodeX).getId((Node)brokens.elementAt(0));
			int e2=currentBnet.siblings(nodeY).getId((Node)brokens.elementAt(1));
			// record the respective position in the list to avoid the
			// cycle by introducing a h2h structure

			theConfs[l+2]= 3+e2;
			theConfs[l+3]= (nodeY.getSiblings().size() > 1) ? 1 : 0;
			theConfs[l+4]=-3-e1;
			theConfs[l+5]= (nodeX.getSiblings().size() > 1) ? 1 : 0;
			fixed=true;		
		    }
		    if (putlink){ // states A,C,D
			//
			//System.out.println(".... Try  link: "+nodeX.getName()+"--"+nodeY.getName());
                        // parents set  empty 
			vars.insertNode(nodeY);
			valOld = metric.score(vars);
		    }
		}
		    break;
		case 1: 
		    numTestCycle++;
		    if (theConfs[l+1] == 2) { // only state B
			Vector acc = new Vector();
	                acc = currentBnet.directedDescendants(nodeY);
			if(acc.indexOf(nodeX) != -1){
			    //when  currentBnet.hasCycle(nodeX,nodeY)
			    putlink = false;
			    numDetectedCycle++;
			}
		    }
		    if (putlink){ // states B,F,G
			//
			//System.out.println("... Try arc: "+nodeX.getName()+"->"+nodeY.getName());
			paNY = currentBnet.parents(nodeY);
			vars.insertNode(nodeY);
			vars.join(paNY);
			valOld = metric.score(vars);
		    }
		    break;
		case 2: {
		    Vector brokens = new Vector();
		    //
		    //System.out.println("... Try arc: "+nodeX.getName()+"->"+nodeY.getName()+" todo children");
		    numTestCycle++;

		    currentBnet.setVisitedAll(false);
		    if (hasMixedCycle(currentBnet,nodeX,nodeY,nodeY, brokens)) {
			numDetectedCycle++;
			putlink = false;
			Node broken1=(Node)brokens.elementAt(0);

			if (nodeY.getChildrenNodes().getId(broken1) != -1) // cycle through a child
			    theConfs[0]=1; // cancel the rest of configurations
			else{ //1 cycle through a sibling (may be several)
			    // in order to look for another possible cycle 

			    Link clink = (Link)currentBnet.getLink(nodeY.getName(), broken1.getName());
			    try{
				currentBnet.removeLink(clink);
			    }catch(InvalidEditException iee){};
			    
			    brokens= new Vector();
			    boolean meetcycle;
			    currentBnet.setVisitedAll(false);
			    meetcycle=hasMixedCycle(currentBnet,nodeX,nodeY,nodeY, brokens);
			    try{ // to put back the previously stablished clink
				    currentBnet.createLink(clink.getTail(),clink.getHead(),false);
			    }catch(InvalidEditException iee){};
			    if (meetcycle)    // cycle through another sibling
				theConfs[0]=1;  // cancel the rest of configurations
			    else{ // we cut the cycle
				
				int idOfh2h=currentBnet.siblings(nodeY).getId(broken1);
				if (theConfs[l+2]>0)
				    theConfs[l+2]= 3+idOfh2h;
				else theConfs[l+2]=-3-idOfh2h;
				fixed=true;
			    }
			}
		    } /* cycle */

		    if (putlink) { // states F,G
			//
			//System.out.println("....Try  arc+children  : "+nodeX.getName()+"->"+nodeY.getName());
                                            //paNY parents set empty
			vars.insertNode(nodeY);
			valOld = metric.score(vars);
			//System.out.println("Antes "+vars.toString2());

		    }
		}
		break;
		default: // states C,D,E,F,G
		    //
		    //System.out.println("....Try  h2h: "+nodeX.getName()+"->"+nodeY.getName());
      			paNY = new NodeList();
			h2hAux=(Node) currentBnet.siblings(nodeY).elementAt(Math.abs(theConfs[l])-3);
			//
			//System.out.println("....       con: "+nodeY.getName()+"<-"+h2hAux.getName());
			paNY.insertNode(h2hAux);
			vars.insertNode(nodeY);
			vars.join(paNY);
			valOld = metric.score(vars);
		
		} /* switch */
		vars.insertNode(nodeX);

		double valNew = metric.score(vars);
		val =  valNew-valOld;

		if (putlink && (val > max)) {

		                       // the best to  insert
		    vvarsR.clear();
		    vvarsR.addElement(nodeX); // nodes implied in the link
		    vvarsR.addElement(nodeY);

		    if (h2hAux!= null)
			vvarsR.addElement(h2hAux);  // to record the h2h's father node
		    vvalR[0]=op;
		    vvalR[1]=(Math.abs(theConfs[l]));  // to record the type of link
		    vvalR[2]=(theConfs[l+1]);  // to record the type of tests
		    max = val;

		}
		
		if (Math.abs(theConfs[l]) <4) l+=2; /* link type 0,1,2 & 3 used in one step, then skip to next configuration */
		   else {
		       if (fixed) l+=2; /* confs fixed that avoid cycle */
		       else if (theConfs[l] < 0) (theConfs[l])++; /*next. configurat.*/
			    else (theConfs[l])--;
		   }
	       } /* for l */
	      }
	      else {   // to remove link

	       numIndTot++;
	       nodeX=link.getTail();
	       nodeY=link.getHead();

	       //
	       //System.out.print("... trying to delete   :"+ nodeX.getName()+ nodeY.getName()); 
	       if (link.getDirected())
		   paNY = currentBnet.parents(nodeY);
	       else {
		   paNY=new NodeList();
		   paNY.insertNode(nodeX);
	       }
	       vars = new NodeList();
	       vars.insertNode(nodeY);
	       vars.join(paNY);
	       double valOld = metric.score(vars);
	       paNY.removeNode(nodeX);
	       vars = new NodeList();
	       vars.insertNode(nodeY);
	       vars.join(paNY);
	       double valNew = metric.score(vars);
	       val = valNew - valOld;
	       //	       System.out.println("Valor al Borrar: "+val+" n:"+ valNew+" de: "+vars.toString2()+" o"+valOld);
	       if(val> max){
		   // max to delete
		   max = val;
		   vvarsR.clear();
		   vvarsR.addElement(nodeX); // nodes implied in the link
		   vvarsR.addElement(nodeY);
		   vvalR[0]=op;
	       }
	      }
	    } /* j */
	} /* i */
	return max;
    }


    /**
     * Codifies in the array The, the number and the type of neighboring
     * configurations when trying to set a new connection 
     */ 

    public void rpdagNeighb(Bnet dag, Node nodex,Node nodey,int The[]){

	int px,py,bx,by; //number of parents and siblings of x and y respectively
 	int i=1;

	bx=dag.siblings(nodex).size();
	by=dag.siblings(nodey).size();
	px=dag.parents(nodex).size();
	py=dag.parents(nodey).size();
	if ((bx==0) && (by==0)){
	    if ((px==0) && (py==0)){ // state A
               //
		//System.out.println("State A");
               The[i++]=0; // x--y 
               The[i]=0; // no test, no complete
	    }
	    else {          // state B 
               //
		//System.out.println("State B");
    		The[i++]=1; // x->y  
		The[i]= (px !=0) && (dag.children(nodey).size() != 0) ? 2 : 0;
		                                   //  test & no complete or notest & no complete
		i++;
   		The[i++]=-1;// x<-y  
		The[i]= (py !=0) && (dag.children(nodex).size() != 0) ? 2 : 0;
	    }
	}
	else if  ((px==0)&&(py==0)){
	    if (bx !=0 && by != 0){ // state C
		//
		//System.out.println("State C");
		The[i++]=0; // x--y
    		The[i]=2;   // test, no complete
		i++;
		The[i++]=2+by;	// x->y, a parent of y, remainder ones as children of y
		The[i]= (by > 1) ? 1 : 0;	
		                       //  complete or not
		i++;
		The[i++]=-2-bx;	// x<-y,  a parent of x, remainder ones as children of x*/
	        The[i]= (bx > 1) ? 1 : 0;
	    }
	    else if (bx==0){ //state D
		//
		//System.out.println("State D");
		The[i++]=0; // x--y
    		The[i]=0;   // no test, no complete
		i++;	
		The[i++]=2+by;	// x->y, a parent of y, remainder ones as children of y
		The[i]= (by > 1) ? 1 : 0;
	    }
	    else { // state E
		//
		// System.out.println("State E");
		The[i++]=0; // x--y
    		The[i]=0;   // no test, no complete
		i++;	
		The[i++]=-2-bx;	// x<-y,  a parent of x, remainder ones as children of x*/
	        The[i]= (bx > 1) ? 1 : 0;
	    }
	}
	else if (bx==0){ //state F
	    //
	    // System.out.println("State F");
	    The[i++]=-1;	 //  link x<-y */
	    The[i]=0;		 //no test, no complete
	    i++;
	    The[i++]=2;		 // x->y all siblings become children of y 
	    The[i]=1;	         //   complete
	    i++;
	    The[i++]=2+by;	 // x->y, a parent of y, remainder ones as children of y
	    The[i]= (by > 1) ? 1 : 0; 	//  complete or not
	}
	else { // state G
	    //
	    //System.out.println("State G");
	    The[i++]=1;	         //  link x->y */
	    The[i]=0;		 //no test, no complete
	    i++;
	    The[i++]=-2;	 // link x<-y  all siblings become children of x
	    The[i]=1;	         //  complete
	    i++;
	    The[i++]=-2-bx;	 // x<-y, a parent of x, remainder ones as children of x
	    The[i]= (bx > 1) ? 1 : 0; 	// complete or not  
	}
	The[0]=++i;

    } /* rpdagNeighb */


 public static void main(String args[]) throws ParseException, IOException { 

	Bnet baprend;
	FileWriter f2;
	Metrics metric;
	double time;

	if(args.length < 4){
	    System.out.println("too few arguments: Usage: file.dbc file.elv classvar  Metric(K2,BIC,BDe) [file.dbc]");
	    System.exit(0);
	}

	FileInputStream f = new FileInputStream(args[0]);	
	DataBaseCases cases = new DataBaseCases(f);

	   //get the variable to classify 
        int classnumber = (new Integer(args[2])).intValue();

	if(args[3].equals("K2")) metric = (Metrics)new K2Metrics(cases);
	else if (args[3].equals("BIC")) metric = (Metrics)new BICMetrics(cases);
	//	 if(args[3].equals("BDe"))
	else metric = (Metrics)new BDeMetrics(cases);
	
	FileInputStream fnet=null;

      Date date = new Date();
      time = (double) date.getTime();
      RPDAGClassifier methdRPDAG =  new RPDAGClassifier(cases,classnumber,true,metric);

                     //Build a bnet with the RPDAG Learning method      
      methdRPDAG.learning(); 

     
      date = new Date();
      time = (((double) date.getTime())-time)/1000;
      System.out.println("Tiempo consumido: "+time);
      System.out.println("Numero de Individuos Evaluados: "+ methdRPDAG.numIndTot);
      System.out.println("Numero de pasos: "+ methdRPDAG.numIndEval);
      System.out.println("Numero de tests para detectar ciclos: "+methdRPDAG.numTestCycle);
      System.out.println("Numero de ciclos positivos: "+methdRPDAG.numDetectedCycle);
      System.out.println("Estadisticos evaluados: "+metric.getTotalStEval());
      System.out.println("Total de estadisticos: "+metric.getTotalSt()); 
      System.out.println("Numero medio de var en St: "+metric.getAverageNVars()); 
    
                     //Learn the parameters of the bnet
      ParameterLearning outputNet2;
      if (methdRPDAG.laplace) {
	  outputNet2= new LPLearning(cases,methdRPDAG.getOutput());
	  outputNet2.learning();
      } else {
	    outputNet2 = new DELearning(cases,methdRPDAG.getOutput());
	    outputNet2.learning();
      }

                    //save the learned bnet
      f2 = new FileWriter(args[1]);
      String st=args[1];
      st=st.substring(st.lastIndexOf('/')+1);
      baprend = (Bnet)outputNet2.getOutput();
      baprend.setName(st); 
      baprend.setComment("learned with RPDAG from "+args[0]);
      baprend.saveBnet(f2);
      f2.close();

      time = (((double) date.getTime())-time)/1000;
      System.out.println("Tempos consumido: "+time); 
      
             // to perform the classification

      if (args.length > 4) {  // with a diferent dataset
      System.out.println(args[4]);

     	  f = new FileInputStream(args[4]);
	  cases = new DataBaseCases(f);
      }
      System.out.println("we have "+cases.getNumberOfCases()+" samples");
      System.out.println("the classvar is  "+cases.getVariables().elementAt(classnumber).getName());

      
      ClassifierValidator validator= new ClassifierValidator((Classifier)outputNet2, cases, classnumber);
      Vector matricesRPDAG;
try {
      //c?digo donde salta la excepci?n
      matricesRPDAG =validator.trainAndTest();
      ConfusionMatrix cm=(ConfusionMatrix)matricesRPDAG.elementAt(0);

      //Prints the confusion matrix and error

      System.out.println("-----------------------------------------------------------");
      System.out.println("Training confusion matrix");
      cm.print();
      System.out.println("Training accuracy="+((1.0-cm.getError())*100.0)+" error:"+cm.getError()+" Variance:"+cm.getVariance());
      cm=(ConfusionMatrix)matricesRPDAG.elementAt(1);
      System.out.println("\nTest confusion matrix");
      cm.print();
      System.out.println("Test accuracy="+((1.0-cm.getError())*100.0)+" error:"+cm.getError()+" Variance:"+cm.getVariance());


} catch (InvalidEditException iee) {
   System.out.println("Exception algo va mal?");
   };


      /*

      CaseListMem clm=(CaseListMem)cases.getCases();

	  for (int k=0;k<cases.getNumberOfCases();k++){
	      Configuration onecase=clm.get(k);
	      Vector salida=methdRPDAG.classify(onecase,classnumber);

	      int max=0;
	      for (int kk=0;kk<salida.size();kk++){
		  if (((Double)salida.elementAt(kk)).doubleValue() > ((Double)salida.elementAt(max)).doubleValue())
		      max=kk;
		  System.out.print(salida.elementAt(kk)+" , ");
		  }
	      Vector values  = onecase.getValues();
	      int real=((Integer)values.elementAt(classnumber)).intValue();
	      if (max!=real) {
		  System.out.print("Caso "+k+" ->");
		  for (int kk=0;kk<values.size();kk++)
		      System.out.print(values.elementAt(kk)+" ");
		  System.out.print("(clase predicha="+ max);//((Double)salida.elementAt(max)).doubleValue());
		  System.out.println(" y clase real -> "+real+")");
		  for (int kk=0;kk<salida.size();kk++){
       		  	System.out.print(salida.elementAt(kk)+" ");
		  }
		  System.out.println("");
	      }
	      } */
      } /* main */
} /* RPDAGClassifier class */


