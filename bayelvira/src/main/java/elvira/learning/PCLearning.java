
/* PCLearning.java */

package elvira.learning;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

import elvira.*;
import elvira.potential.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;

/**
 * PCLearning.java
 *
 *
 * Created: Tue May 11 11:54:08 1999
 *
 * @author Proyecto Elvira
 * @version 1.0
 */

public class PCLearning extends Learning  {

    ConditionalIndependence input; // Input of the Learning Process.
    int indmethod= 0; // 0 test 1 scores 
    int refining = 0;// 0 no 1 yes
    int triangles = 0;// 0 no 1 yes
    double delay;                    // Delay of the Learning Process.
    int numberOfTest;              // Number of test in the learning process.
    double setSizeCondMean;        // size mean of conditionating set.
    double levelOfConfidence;         // level of conf. for the C.I. tests

   public static void main(String args[]) throws ParseException, IOException { 

      BDeMetrics metric;
      Bnet net, baprend;
      FileWriter f2;
      int method;
      net = null;
      if(args.length < 6){
      System.out.println("too few arguments: Usage: file.dbc numberCases file.elv (for saving the results) method (0|1) refining (0|1) triangles (0|1)   [file.elv (true net to be compared)]");
      System.exit(0);
      }


      FileInputStream f = new FileInputStream(args[0]);
      DataBaseCases cases = new DataBaseCases(f);
      cases.setNumberOfCases(Integer.valueOf(args[1]).intValue());
      method = (Integer.valueOf(args[3]).intValue());
      
      metric = new BDeMetrics(cases);
      if(args.length > 6){ 
          FileInputStream fnet = new FileInputStream(args[6]);
          net = new Bnet(fnet); 
      } 
      PCLearning outputNet2 = new PCLearning(cases,method);
      outputNet2.setRefining( (Integer.valueOf(args[4]).intValue())); 
      outputNet2.setTriangles( (Integer.valueOf(args[5]).intValue())); 
      outputNet2.setLevelOfConfidence(0.99);
      outputNet2.learning();
      
      DELearning outputNet3 = new DELearning(cases,outputNet2.getOutput());
      outputNet3.learning(2.0);
      
     
      
      double d = cases.getDivergenceKL(outputNet3.getOutput());
      System.out.println("Divergencia de KL = "+d);
      f2 = new FileWriter(args[2]);
      baprend = (Bnet)outputNet3.getOutput();
      baprend.saveBnet(f2);
      f2.close();
      System.out.println("Medida Bayes. de la red resultado: "+metric.score(outputNet2.getOutput()));
       System.out.println("tiempo consumido"+outputNet2.delay);
      if(args.length > 6){
      FileInputStream fnet = new FileInputStream(args[6]);
      net = new Bnet(fnet);
          double d2 = cases.getDivergenceKL(net);
          System.out.println("Divergencia de la red real: "+d2);
          System.out.println("Divergencia real: "+(d2-d));
      LinkList addel[] = new LinkList[3];
      addel = outputNet2.compareOutput(net);
      System.out.print("\nNumero de arcos a�adidos: "+addel[0].size());
      System.out.print(addel[0].toString());
      System.out.print("\nNumero de arcos borrados: "+addel[1].size());
      System.out.print(addel[1].toString());
      System.out.println("\nNumero de arcos mal orientados: "+addel[2].size());
      System.out.print(addel[2].toString());
      System.out.print("\nArcos no orientados: ");
      System.out.print(outputNet2.linkUnOriented().toString());
      }  

      
   }  



public PCLearning(){
    
    setInput(null);
    setOutput(null);
}

    /** 
     * Initializes a full unidirected graph with the variables contained into
     * the Data Base of Cases cases. Also initializes the input of algorithm
     * PC as a Data Base of Cases and the PC output as a Bnet with the above
     * graph. 
     * @param DataBaseCases cases. The data bases of discrete cases.
     */    


public PCLearning(DataBaseCases cases) {

    Bnet b;
    Graph dag;
    NodeList nodes;
    LinkList links;
    Link link;
    Node nodet,nodeh;
    boolean directed = false;
    int i,j;

  //  System.out.println("Paso por Crear base de datos");
    nodes = cases.getVariables();
    //links = new LinkList();
    b = new Bnet();
    b.setKindOfGraph(2);
    dag = (Graph) b;
    for(i=0 ; i < (nodes.size()) ;i++)
       try {
           dag.addNode(nodes.elementAt(i).copy());
        }
        catch (InvalidEditException iee) {};

    nodes=dag.getNodeList();
    for(i=0 ; i < (nodes.size()-1) ;i++)
    for(j=i+1 ; j<nodes.size() ;j++){
        nodet=(Node)nodes.elementAt(i);
        nodeh=(Node)nodes.elementAt(j);
        try {
           dag.createLink(nodet, nodeh, directed);
        }
        catch (InvalidEditException iee) {};
    }
    
    this.input = cases;
    setOutput(b);
    setLevelOfConfidence(0.99);
    
} 


    /** 
     * Initializes a full unidirected graph with the variables contained into
     * the List of Nodes nodes. Also initializes the input of algorithm
     * PC as a Data Base of Cases and the PC output as a Bnet with the above
     * graph. It's very important that the variables contained in nodes are
     * a subset of the variables contained in the Data Bases of Cases.
     * @see DataBaseCases - method getVariables();
     * @param DataBaseCases cases. The data bases of discrete cases.
     * @param NodeList nodes. Must be a subset of variables of the cases.
     */    


public PCLearning(DataBaseCases cases, NodeList nodes) {

    Bnet b;
    Graph dag;
    LinkList links;
    Link link;
    Node nodet,nodeh;
    boolean directed = false;
    int i,j;

    //links = new LinkList();
    b = new Bnet();
    b.setKindOfGraph(2);
    dag = (Graph) b;
    for(i=0 ; i < (nodes.size()) ;i++)
       try {
	   dag.addNode(nodes.elementAt(i).copy());
       }
       catch (InvalidEditException iee) {};
    
    nodes=dag.getNodeList();
    for(i=0 ; i < (nodes.size()-1) ;i++)
    for(j=i+1 ; j<nodes.size() ;j++){
        nodet=(Node)nodes.elementAt(i);
        nodeh=(Node)nodes.elementAt(j);
        try {
           dag.createLink(nodet, nodeh, directed);
        }
        catch (InvalidEditException iee) {};
    }
    
    this.input = cases;
    setOutput(b);
    setLevelOfConfidence(0.99);
    
}



    /** 
     * This method allows the initialization of several differents
     * PC learning algorithms.
     * The default is method 0, which simply calls to the PC with classical
     * statistical tests procedures.
     * With method 1, it will consider PC algorithm, but with tests done
     * by comparing Bayesian scores.
     * @param DataBaseCases cases. The data bases of discrete cases.
     * @param NodeList nodes. The nodes on which the learning will be carried out.
     * @param int method The method for the conditional independence tests.
     */    



public PCLearning(Graph input) {

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
           dag.addNode(nodes.elementAt(i).copy());
        }
        catch (InvalidEditException iee) {};
    //links = new LinkList();
    
    nodes=dag.getNodeList();
    for(i=0 ; i < (nodes.size()-1) ;i++)
    for(j=i+1 ; j<nodes.size() ;j++){
        nodet=(Node)nodes.elementAt(i);
        nodeh=(Node)nodes.elementAt(j);
        try {
           dag.createLink(nodet, nodeh, directed);
        }
        catch (InvalidEditException iee) {};
        
    }
   
    this.input=input;
    setOutput(b);
    setLevelOfConfidence(0.99);
    
}

    /**
     * see  PCLearning(Graph input) and 
     * also PCLearning(DataBaseCases cases, NodeList nodes)
     * @param BDeMetrics input Mtrics used for the conditional independence tests
     * @param NodeList nodes.
     */

public PCLearning(BDeMetrics input, NodeList nodes) {

    Bnet b;
    Graph dag;
    LinkList links;
    Link link;
    Node nodet,nodeh;
    boolean directed = false;
    int i,j;
    NodeList nodesInput = input.getNodeList().duplicate();
    
    //links = new LinkList();
 
    nodesInput = nodesInput.intersectionNames(nodes);   
    b = new Bnet();
    b.setKindOfGraph(2);
    dag = (Graph) b;
    
    for(i=0 ; i < (nodesInput.size()) ;i++)
      try {
          dag.addNode(nodesInput.elementAt(i).copy());
       }
       catch (InvalidEditException iee) {};

    nodes=dag.getNodeList();
    for(i=0 ; i < (nodesInput.size()-1) ;i++)
    for(j=i+1 ; j<nodesInput.size() ;j++){
        nodet=(Node)nodesInput.elementAt(i);
        nodeh=(Node)nodesInput.elementAt(j);
        try {
           dag.createLink(nodet, nodeh, directed);
        }
        catch (InvalidEditException iee) {};
        
    }
    
    this.input = input;
    setOutput(b);
    setLevelOfConfidence(0.99);
}



    /**
     * see  PCLearning(Graph input) and 
     * also PCLearning(DataBaseCases cases, NodeList nodes)
     * @param Graph input
     * @param NodeList nodes.
     */

public PCLearning(ConditionalIndependence input, NodeList nodes) {

    Bnet b;
    Graph dag;
    LinkList links;
    Link link;
    Node nodet,nodeh;
    boolean directed = false;
    int i,j;
    NodeList nodesInput = input.getNodeList().duplicate();
    
    //links = new LinkList();
 
    nodesInput = nodesInput.intersectionNames(nodes);   
    b = new Bnet();
    b.setKindOfGraph(2);
    dag = (Graph) b;
    
    for(i=0 ; i < (nodesInput.size()) ;i++)
      try {
          dag.addNode(nodesInput.elementAt(i).copy());
       }
       catch (InvalidEditException iee) {};

    nodes=dag.getNodeList();
    for(i=0 ; i < (nodesInput.size()-1) ;i++)
    for(j=i+1 ; j<nodesInput.size() ;j++){
        nodet=(Node)nodesInput.elementAt(i);
        nodeh=(Node)nodesInput.elementAt(j);
        try {
           dag.createLink(nodet, nodeh, directed);
        }
        catch (InvalidEditException iee) {};
        
    }
    
    this.input = input;
    setOutput(b);
    setLevelOfConfidence(0.99);
}



public PCLearning(DataBaseCases cases, NodeList nodes, int method) {

     this(cases,nodes);
     
     indmethod = method;
     
     ConditionalIndependence input;
    
    switch (method){
        case 0: {}
        case 1: {this.input = new BDeMetrics(cases);}
    }

    
    
}


public PCLearning(DataBaseCases cases, int method) {
    this(cases);
     
     ConditionalIndependence input;
   
     indmethod = method;
     
    switch (method){
        case 0: {System.out.println("M�todo tradicional");break;}
      case 1: {this.input = new BDeMetrics(cases);}
    }

    
    
}


public void setRefining(int i){
    refining = i;
}



public void setTriangles(int i){
    triangles = i;
}



public void order0indep(Vector index) {
  Graph dag;
  int i,j,pos;
   Hashtable sepSet;
   FiniteStates nodeX,nodeY;
  NodeList    subSet;
  boolean ok;
   Link link;
   
 
  dag = (Graph)getOutput();
  
    
 for (i=0 ; i<(dag.getNodeList()).size();i++){
      nodeX=(FiniteStates)(dag.getNodeList()).elementAt(i);
  for (j=i+1 ; j< (dag.getNodeList()).size() ;j++){
      nodeY=(FiniteStates)(dag.getNodeList()).elementAt(j);
   //   System.out.println("Nodes: "+nodeX.getName()+", "
//                +nodeY.getName()+" Step: 0");
       subSet = new NodeList();
        ok=input.independents(nodeX,nodeY,subSet,levelOfConfidence);
        System.out.print("\n I( "+nodeX.getName()+" , "+nodeY.getName()+" | "+subSet.toString2()+") : "+ok+"\n");
        if (ok) {
             link=getOutput().getLinkList().getLinks(nodeX.getName(),
                             nodeY.getName());
         if(link==null)
             link=getOutput().getLinkList().getLinks(nodeY.getName()
                                 ,nodeX.getName());
                   try {
           dag.removeLink(link);
        }
        catch (InvalidEditException iee) {};
                     
                 pos = dag.getNodeList().getId(nodeX);
                 sepSet = (Hashtable)index.elementAt(pos);
                 sepSet.put(nodeY,subSet);
                 pos = dag.getNodeList().getId(nodeY);
                 sepSet = (Hashtable)index.elementAt(pos);
                 sepSet.put(nodeX,subSet);  
        }
      
  }
 }
    
    
    
}

public void solvetrian(Vector index) {
  Graph dag;
  int i,j,k,pos;
   Hashtable sepSet;
   FiniteStates nodeX,nodeY,nodeZ,nodeA,nodeB;
  NodeList    subSet,adyacenciesX,adyacenciesY;
  double max= -1;
  int best = -1;
  double sxy, syz, sxz;
  
  boolean ok;
   Link link;
   BDeMetrics metric;
   
   
   
 
   if (indmethod == 0) {return;}
   
  metric = (BDeMetrics) input;
  dag = (Graph)getOutput();

  
  
  System.out.println("Start solving triangles"); 
  
 for (i=0 ; i<(dag.getNodeList()).size();i++){
       nodeX=(FiniteStates)(dag.getNodeList()).elementAt(i);
        
       
  for (j=i+1 ; j< (dag.getNodeList()).size() ;j++){
      nodeY=(FiniteStates)(dag.getNodeList()).elementAt(j);
         adyacenciesX=dag.neighbours(nodeX);
      if (adyacenciesX.getId(nodeY) !=-1) {
           adyacenciesY=dag.neighbours(nodeY);
  
            for (k=j+1 ; k< (dag.getNodeList()).size() ;k++){
                 nodeZ=(FiniteStates)(dag.getNodeList()).elementAt(k);
             if ((adyacenciesX.getId(nodeZ) !=-1)&&(adyacenciesY.getId(nodeZ) !=-1)) {
                  System.out.println("Solving a triangle");
                     subSet = new NodeList();
                      subSet.insertNode(nodeZ);
                     max = -1.0;
                     best = -1;
                      sxy = metric.scoreDep (nodeX, nodeY, subSet );    
           //              System.out.println("Nodes: "+nodeX.getName()+", "
           //       +nodeY.getName()+" Score : " + sxy);
                      subSet = new NodeList();
                      subSet.insertNode(nodeX);
                      syz = metric.scoreDep (nodeY, nodeZ, subSet );   
                                          subSet = new NodeList();
          //                                 System.out.println("Nodes: "+nodeY.getName()+", "
         //         +nodeZ.getName()+" Score : " + syz);
                      subSet.insertNode(nodeY);
                      sxz = metric.scoreDep (nodeX, nodeZ, subSet );  
                  //     System.out.println("Nodes: "+nodeX.getName()+", "
                 // +nodeZ.getName()+" Score : " + sxz); 
                     if (sxy > max) {max = sxy; best = 1;}
                      if (syz > max) {max = syz;best = 2;}
                       if (sxz > max) {max = sxz;best = 3;}
                     if (max > 0) {
             //             System.out.println("removing link " + best);
                              switch (best){  
                         case 1  :  nodeA = nodeX;
                                    nodeB = nodeY;
                                     subSet = new NodeList();
                                     subSet.insertNode(nodeZ);
                                     k= dag.getNodeList().size();
                                    break;
                         case 2 :  nodeA = nodeZ;
                                   nodeB = nodeY;
                                       subSet = new NodeList();
                                     subSet.insertNode(nodeX);
                                    break;
                            case 3 :nodeA = nodeX;
                                    nodeB = nodeZ;          
                                      subSet = new NodeList();
                                     subSet.insertNode(nodeY);
                                     break;
                                    
                                  default : nodeA = null;
                                            nodeB = null;
                              }
                              
               //               System.out.println("Eliminando enlace entre " +  nodeA.getName() + " y nodo " +  nodeB.getName());
                   link=getOutput().getLinkList().getLinks(nodeA.getName(),
                             nodeB.getName());
         if(link==null)
             link=getOutput().getLinkList().getLinks(nodeB.getName()
                                 ,nodeA.getName());
                   try {
           dag.removeLink(link);
        }
        catch (InvalidEditException iee) {};
                     
                
                 pos = dag.getNodeList().getId(nodeA);
              //   System.out.println("posicion " + pos);
                 sepSet = (Hashtable)index.elementAt(pos);
                 sepSet.put(nodeB,subSet);
                 pos = dag.getNodeList().getId(nodeB);
                    
                 sepSet = (Hashtable)index.elementAt(pos);
                 sepSet.put(nodeA,subSet);               
                              
                              
                     }
                      
                      
             
             }      
                 
                 
          
            }
          
      }
      
      
   
      
  }
 }
    
    
    
}

    /** 
     * Initializes a full unidirected graph with the variables contained into
     * the List of Nodes of the parameter input. Also initializes the input of
     * algorithm PC as a Graph and the PC output as a Bnet with the above
     * graph. 
     * @param Graph input. The input graph. (d-separation criterion).
     */    

    /**
     * This method implements the PC algorithm(Causation, Prediction and Search
     * 1993. Lectures Notes in Statistical 81 SV. Spirtes,Glymour,Sheines).
     * Only the structure of the net is discovered.
     * levelOfConfidence indicates the level of
     * confidence for testing the conditional independences. 0.0 will be the
     * minor confidence.
     */

public void learning(){

     Metrics metric; 
    
 Graph dag;
 int n,i,j,pos;
 FiniteStates nodeX,nodeY;
 Hashtable sepSet;
 NodeList adyacenciesX,adyacenciesY,adyacenciesXY,
     adyacenciesYX,vars,subSet;
 LinkList linkList;
 Link link;
 Vector subSetsOfnElements,index;
 Enumeration en;
 boolean ok,encontrado=false,directed=false;
 Date D = new Date();
 delay = (double) D.getTime();

 //vars = getOutput().getNodeList();
 index = new Vector();
 for(i=0 ; i< getOutput().getNodeList().size() ;i++){
     sepSet = new Hashtable();
     index.addElement(sepSet);
 }
 //linkList = getOutput().getLinkList();
 dag = (Graph)getOutput();

 
 order0indep(index); 
 
if (triangles ==1) { solvetrian(index);}
 
 
 for (n=1 ; n <= dag.maxOfAdyacencies() ; n++)
     for (i=0 ; i<(dag.getNodeList()).size();i++){
     for (j=0 ; j< (dag.getNodeList()).size() ;j++){
         if(i!=j){
         encontrado=false;
         nodeX=(FiniteStates)(dag.getNodeList()).elementAt(i);
         adyacenciesX=dag.neighbours(nodeX);
         nodeY=(FiniteStates)(dag.getNodeList()).elementAt(j);
         System.out.println("Nodes: "+nodeX.getName()+", "
                  +nodeY.getName()+" Step: "+n);
         link=getOutput().getLinkList().getLinks(nodeX.getName(),
                             nodeY.getName());
         if(link==null)
             link=getOutput().getLinkList().getLinks(nodeY.getName()
                                 ,nodeX.getName());
         
         if(adyacenciesX.getId(nodeY) !=-1){ // they are adyacent.
             adyacenciesX.removeNode(nodeY);
             if(adyacenciesX.size() >= n){
             subSetsOfnElements=adyacenciesX.subSetsOfSize(n);
             en = subSetsOfnElements.elements();
             while((!encontrado)&&(en.hasMoreElements()||n==0)){
                 if(n==0) subSet = new NodeList();
                 else subSet = (NodeList)en.nextElement();
                 ok=input.independents(nodeX,nodeY,subSet,levelOfConfidence);
           //      System.out.print("\n I( "+nodeX.getName()+" , "+nodeY.getName()+" | "+subSet.toString2()+") : "+ok+"\n");
                 if(ok){
                 
                 try {
                     dag.removeLink(link);
                 } catch (InvalidEditException iee) { };                 
                 pos = dag.getNodeList().getId(nodeX);
                 sepSet = (Hashtable)index.elementAt(pos);
                 sepSet.put(nodeY,subSet);
                 pos = dag.getNodeList().getId(nodeY);
                 sepSet = (Hashtable)index.elementAt(pos);
                 sepSet.put(nodeX,subSet);
                 encontrado = true;
                 
                 }
                 if(n==0) encontrado=true;
             }
             }
         }
         }
     }
     }

 
// for(i=0 ; i< index.size(); i++){
//     nodeX=(FiniteStates)dag.getNodeList().elementAt(i);
//     sepSet = (Hashtable) index.elementAt(i);
//     en=sepSet.keys();
//     while(en.hasMoreElements()){
//   nodeY = (FiniteStates)en.nextElement();
//   subSet = (NodeList)sepSet.get(nodeY);
//   System.out.println(nodeX.getName()+"  "+nodeY.getName());
//   System.out.println("----------------------------------");
//   subSet.print();
    //   try{
//           System.in.read();
//       }catch (IOException e){
//       }
//     }
// }

 headToHeadLink((Graph)dag,index);
 remainingLink((Graph)dag,index);
 extendOutput();
 if (refining==1) {
     if (indmethod == 0) {
         metric = new BDeMetrics((DataBaseCases) input);
     }
     else { metric = (BDeMetrics) input;                        }
     refine(metric); 
 }
 D = new Date();
 delay = (((double)D.getTime()) - delay) / 1000;
 
}


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
                 { System.out.println("Testing 3 nodes " +subDsep.size() );}
         if((subDsep!=null)&&(subDsep.getId(nodeY) == -1)){
                    System.out.println("Directing ");
             delLink = links.getLinks(nodeX.getName(),nodeY.getName());
             if(delLink == null){
             delLink = links.getLinks(nodeY.getName(),nodeX.getName());
             }
             directedXY = delLink.getDirected();
             if(!directedXY){ 
                         dag.orientLinkDag(delLink,nodeX,nodeY);
                        }
                     
                     
             delLink = links.getLinks(nodeY.getName(),nodeZ.getName());
             if(delLink == null){ 
             delLink = links.getLinks(nodeZ.getName(),nodeY.getName());
             }
             directedYZ = delLink.getDirected();
             if(!directedYZ){
              dag.orientLinkDag(delLink,nodeZ,nodeY);
                     
                     }
         }
             
         }
     }
     }
 }
}
    

    /**
     * This method carry out the direction of the remaining link that can
     * be computed.
     * @param Graph @see above method.
     * @param Vector @see above method.
     */

protected void remainingLink(Graph dag,Vector index){

boolean change,change2,oriented,skip;
int i,j,k;
Link linkAB,linkBC,linkCB,linkBW;
Node nodeA,nodeB,nodeC,nodeW;
NodeList nbTail,nbHead,nbC,children;
Vector acc;
LinkList links;

do{ 
    change2=false;
 do{
     change = false;
     for(i=0 ;i<dag.getLinkList().size();i++){
	//dag.setVisitedAll(false);
     linkAB = (Link) dag.getLinkList().elementAt(i);
     nodeA = (Node) linkAB.getTail();
     nodeB = (Node) linkAB.getHead();
        System.out.println("Testing nodes " + nodeA.getName() + " " + nodeB.getName());
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
             linkCB = dag.getLinkList().getLinks(nodeC.getName(),
                                 nodeB.getName());
                       if(linkCB== null)  
                       { linkCB =dag.getLinkList().getLinks(nodeB.getName(),nodeC.getName());}
                     
             
             if(!linkCB.getDirected()){
                             dag.orientLinkDag(linkCB,nodeB,nodeC);
                              change = true;
             }
             
         }
         }
     }
     else{ // A -- B Non-oriented link
          oriented = false;
          if(dag.isThereDirectedPath(nodeA,nodeB))
          {  System.out.println("Orienting from "+ nodeA.getName() + " to "+ nodeB.getName() + "directed path");
              dag.orientLinkDag(linkAB,nodeA,nodeB);
              change = true;
              oriented = true;
          }
           if((dag.isThereDirectedPath(nodeB,nodeA)) && (!oriented))
          {   System.out.println("Orienting from "+ nodeB.getName() + " to "+ nodeA.getName() + "directed path");
              dag.orientLinkDag(linkAB,nodeB,nodeA);
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
                 
                                if(linkBW == null) {linkBW = dag.getLinkList().getLinks(nodeW.getName(),nodeB.getName());}
                                          if (!linkBW.getDirected()) {dag.orientLinkDag(linkBW,nodeB,nodeW);
                                                                     change = true;
                                                                     System.out.println("Orienting from "+ nodeB.getName() + " to "+ nodeW.getName() + " rule 3");
                                                                       }
                                   
                                 skip=true;  
                               }}
                               if (!skip){
                                    children = dag.children(nodeW);
                                    if(children.getId(nodeC) == -1){
                                          dag.orientLinkDag(linkBC,nodeB,nodeC);
                                          System.out.println("Orienting from "+ nodeB.getName() + " to "+ nodeC.getName() + " rule 4");
                                                                      
                                          change = true;
                                    
                                    }
                                   
                                   
                               }  
                               
                           }
                           
                           
                           
                       }
                     
                   
                     
                 }
            }
          }
         
     }
         
     }
     
     
     
     
     }while (change);
    

 for(i=0 ;i<dag.getLinkList().size();i++){
     linkAB = (Link) dag.getLinkList().elementAt(i);
     nodeA = (Node) linkAB.getTail();
     nodeB = (Node) linkAB.getHead();
     //    System.out.println("Testing nodes " + nodeA.getName() + nodeB.getName());
     if(!linkAB.getDirected()){
              try {
                   dag.removeLink(linkAB);
                     if (!dag.isThereDirectedPath(nodeB,nodeA)){
                        System.out.println("Orienting from "+ nodeA.getName() + " to "+ nodeB.getName() + " arbitrary");
                        dag.createLink(nodeA,nodeB); 
                     }
                     else 
                     { System.out.println("Orienting from "+ nodeB.getName() + " to "+ nodeA.getName() + " arbitrary");
                                           
                         dag.createLink(nodeB,nodeA);} 
              }
                  catch (InvalidEditException iee) {};
                          
         change2=true;
         break;
         }
              
             
         }
 


 }while (change2);     
 
 
}

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
//linksOutput = getOutput().getLinkList();

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

}


    /**
     * This method construct a consistent extension of a partially oriented
     * graph (usually, the output of the PC algorithm).
     */


public void extendOutput(){

LinkList links;
Link link;
Graph dag;
NodeList cola;
Node node,hermano;

cola = new NodeList();
dag = (Graph) getOutput();
links = linkUnOriented();

 System.out.println("Second extension");
  while(links.size()>0){
   link = links.elementAt(0);
   node = link.getTail();
   for( ; dag.siblings(node).size()>0 ;){
      hermano = dag.siblings(node).elementAt(0);
      cola.insertNode(hermano);
      link = dag.getLink(node,hermano);
      if(link==null) link=dag.getLink(hermano,node);
      try{
        dag.removeLink(link);
        if (!dag.isThereDirectedPath(hermano,node))
           {dag.createLink(node,hermano);}
        else
           {dag.createLink(hermano,node);}
      }catch(InvalidEditException iee){};   
   }
   while(cola.size()>0){
      node = cola.elementAt(0);
      for(; dag.siblings(node).size()>0;){
         hermano = dag.siblings(node).elementAt(0);
         cola.insertNode(hermano);
         link = dag.getLink(node,hermano);
         if(link == null) link=dag.getLink(hermano,node);
         try{
           dag.removeLink(link);
            if (!dag.isThereDirectedPath(hermano,node))
           {dag.createLink(node,hermano);}
        else
           {dag.createLink(hermano,node);}
         }catch(InvalidEditException iee){};
      } 
      cola.removeNode(node);
   }
   links = linkUnOriented();
  }
}

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

}

    /** Access methods ***/

    public double getLevelOfConfidence(){
    return levelOfConfidence;
    }

    public void setLevelOfConfidence(double  level){
    levelOfConfidence = level;
    }
    
    public void setInput(ConditionalIndependence input){
    this.input = input;
    }
    public ConditionalIndependence getInput(){
    return input;
    }

 
public void refine(Metrics metric){
    
    Graph dag;
    boolean improve,add;
    NodeList nodes,parents,vars;
    double fitness,fitnessNew;
    Node NodeX,NodeY;
    int i,j,pos,best;
    boolean improv;
    
    add=true;
    dag= (Graph) getOutput();
     if (!dag.isADag()){ System.out.println("no es dag");  }
    System.out.println(" Graph " + dag.toString());
     System.out.println("antes de topological order ");
    nodes = dag.topologicalOrder(); 
     System.out.println("despues de topological order ");
    for(i=0; i< nodes.size(); i++){
      NodeX = (FiniteStates) nodes.elementAt(i);
      System.out.println("Refinando nodo " + NodeX.getName());
      parents = dag.parents(NodeX); 
      vars = new NodeList();
      vars.insertNode(NodeX);
      vars.join(parents);
      fitness = metric.score(vars);
      improv=true;
      best=0;
      while(improv){
       improv=false;
       for(j=0;j<i;j++){
          NodeY  = (FiniteStates) nodes.elementAt(j);
           //     System.out.println("Comprobando nodo " + NodeY.getName());
          pos = vars.getId(NodeY);
          if(pos==-1) { 
                //   System.out.println("Nodo no padre");
              vars.insertNode(NodeY);
              //     System.out.println("Computing score " + vars.size());
              fitnessNew = metric.score(vars);
               //    System.out.println("Score finished");
              if (fitnessNew > fitness){
                //   System.out.println("Score mejorado");
                  fitness = fitnessNew;
                  improv=true;
                  
                   add=true;
                  best=j;
              }
              
                vars.removeNode(NodeY); 
           
          }
          else {
             //    System.out.println("Nodo padre");
            vars.removeNode(NodeY);
             //      System.out.println("Computing score " + vars.size());
             fitnessNew = metric.score(vars);
              //     System.out.println("Score finished");
              if (fitnessNew > fitness){
              //         System.out.println("Score mejorado" + NodeY.getName());
                  fitness = fitnessNew;
                  improv=true;
                  add=false;
                   best = j;
              }
              vars.insertNode(NodeY);
              
          }
       }
       if (improv){
          // System.out.println("BEst thing " + best);
           if (add){
               NodeY = (FiniteStates) nodes.elementAt(best);
               vars.insertNode(NodeY);
             //   System.out.println(NodeY.getName());
                try {
         dag.createLink(NodeY,NodeX);
        }
        catch (InvalidEditException iee) {};
           }
           else {
              NodeY = (FiniteStates) nodes.elementAt(best);
              vars.removeNode(NodeY); 
             // System.out.println(NodeY.getName()+NodeX.getName());
              try {
               //   System.out.println(dag.getLinkList().toString());
         dag.removeLink(NodeY,NodeX);
        }
        catch (InvalidEditException iee) {};
           }
           
       }
       
      }
      }
      
    }
} // PCLearning
