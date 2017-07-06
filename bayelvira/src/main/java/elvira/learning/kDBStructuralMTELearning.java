package elvira.learning;

import elvira.potential.ContinuousProbabilityTree;
import java.util.*;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.*;
import elvira.potential.MixtExpDensity;
import elvira.parser.ParseException;
import elvira.potential.PotentialContinuousPT;

/*
 * Implements an algorithm to create a network using some ideas of a kDB in 
 * order to learn its structure. There are continuous and discrete variables. 
 * It assumes that the joint distribution is MTE. The main steps are:
 * 1. Create a full connected graph among the variables.
 * 2. Compute de I(Xi,Xj) for each pair of variables
 * 3. Order the links decreasingly using I(Xi,Xj)
 * 4. Use this order to apply the Sahami's ideas (1996) to create a kDB among 
 *    the variables. Obviously we have no class variable in this problem.
 * 5. The final structure will be learned using MTEs.
 *  
 * @author Antonio Fernandez Alvarez (afalvarez@ual.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 * 
 * @since 16/10/2007
 */

public class kDBStructuralMTELearning{
    
    /**
     * Variables for which the network is defined.
     */
    NodeList variables;
       
    /**
     * The network
     */
    Bnet net;
      
    /**
     * Creates a kDBStructuralLearning object.
     *
     * @param db the DataBaseCases from which the network will be constructed. 
     * @param intervals the number of intervals into which the domain of the continuous
     * variables will be split.
     * @param k is the maximum number of parents in the networks.
     
     */
    
    public kDBStructuralMTELearning(DataBaseCases db, int intervals, int k) {
        
        MTELearning learningObject;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree t;
        Relation rel;
        Vector netRL;
        NodeList parents, relationVariables; 
        Node var; 
        
        net=new Bnet();
        variables = db.getNodeList();  
        net.setNodeList(variables);
      

        System.out.println("------------------------------------------------------------------------");
        System.out.println("Name of DataBaseCase: " + db.getName());
        System.out.println("Number of variables: " + variables.size());
        System.out.print("Names of variables: "); variables.printNames(); 
        System.out.print(k + "-DB\n");             
        //--------------------------------------------------------------------------------------------
        
        System.out.println("\nMaking the complete graph with the variables ... ");
        LinkList completeLinkList= this.CompleteLinkList(variables);
        
        Vector vMI= this.getListOfMututlInformation(db, completeLinkList,intervals);
        
        Vector vOrderedMI = this.OrderedIndexByMutualInformation(vMI);
        
        System.out.println(vOrderedMI.toString());
        
        
        NodeList vOrderedNodesByMI=this.getOrderedNodesByMI(completeLinkList,vOrderedMI);
        
        vOrderedNodesByMI.printNames();
        
        
        //Creo la lista de nodos S y le inserto el mejor nodo
        NodeList S=new NodeList();
        Node aux=vOrderedNodesByMI.elementAt(0);
        S.insertNode(aux); 
        
        //Para el resto de nodos
        for (int i=1;i<vOrderedNodesByMI.size();i++){

            Node Xmax=vOrderedNodesByMI.elementAt(i);
                                            
            //Number of links added
            int m=Math.min(i,k);        
            
            NodeList NodosOrigen=this.getNodesMaxMI(m, S,completeLinkList, vOrderedMI, Xmax);
            S.insertNode(Xmax);
            
            //Creo los links
            for (int j=0;j<NodosOrigen.size();j++){
                System.out.println("Inserting link "+ NodosOrigen.elementAt(j).getName()+"-->" + Xmax.getName());
                try {      
                    net.createLink(NodosOrigen.elementAt(j), Xmax, true);   
                }  catch (Exception e) { System.out.println("Problems to create the link");};                  
            }
        }
        
       //TRAIN 
       netRL=new Vector();
       learningObject= new MTELearning(db);
       System.out.println("\n\n====> Learning network <=======");
       
       for (int i=0;i<variables.size();i++){
          
           var=variables.elementAt(i);
           parents=new NodeList();
           parents=var.getParentNodes();
          
           System.out.println("   Learning " + var.getName()+" ...");
           t=learningObject.learnConditional(var, parents, db, intervals, 4);
            
           relationVariables=new NodeList();
           relationVariables.insertNode(var);
           for (int j=0;j<parents.size();j++){
                relationVariables.insertNode(parents.elementAt(j));
           }
           pot=new PotentialContinuousPT(relationVariables,t);
           rel=new Relation();
           rel.setVariables(relationVariables);
           rel.setValues(pot); 
           netRL.addElement(rel);
       }
      net.setRelationList(netRL);
 
    } 
      
    /**
     * Get a list of nodes with the best order using the mutual information
     * @param ll <code>LinkList</code> of the full connected graph
     * @param vOrd <code>Vector</code> contains the ordered indexes of ll by MI.
     * @return <code>NodeList</code> list of ordered nodes.
     *
     */
    private NodeList getOrderedNodesByMI(LinkList ll, Vector vOrd){
        
        NodeList nl = new NodeList();
        Node nod;
        int index=((Integer)vOrd.elementAt(0)).intValue();    
        
        //Alternativa: cambiar las 4 instrucciones siguientes de orden
        nod=ll.elementAt(index).getTail();
        nl.insertNode(nod);
        
        nod=ll.elementAt(index).getHead();
        nl.insertNode(nod);
                              
        for (int i=0;i<variables.size()-2;i++){
            
            int j=1;
            boolean flag=false;
            while (!flag){
                
                int indexj=((Integer)vOrd.elementAt(j)).intValue();
                Node tail=ll.elementAt(indexj).getTail();
                Node head=ll.elementAt(indexj).getHead();
                    
                if ((nod.getName().compareTo(tail.getName())==0)&&(nl.getId(head)==-1)){
                     nod=head;
                     nl.insertNode(nod);
                     flag=true;
                } 
                else
                    if ((nod.getName().compareTo(head.getName())==0)&&(nl.getId(tail)==-1)){
                         nod=tail;
                         nl.insertNode(nod);
                         flag=true;
                    }    
                j++; 
            }                
        } 
        return nl;
    }
    
     /**
     * Obtain a list of the mutual information between each pair of variables.
     * @param db the DataBaseCases of the problem.
     * @param ll LinkList of the full connected graph constructed with the 
     * variables
     * @param intervals the number of intervals into which the domain of the continuous
     * variables will be split.
     * @return Vector of Double values (mutual informations) in the same order
     * as ll.
     */    
    
    public Vector getListOfMututlInformation(DataBaseCases db, LinkList ll, int intervals) {
        
        ContinuousProbabilityTree featureTree_i, featureTree_j;
        MTELearning learningObject;
        double valueMI;
        Vector vResult=new Vector();
  
        learningObject = new MTELearning(db);
        
        System.out.println("Estimating the mutual information ...");
        
        for (int i=0;i<ll.size();i++){
            
            Node Xi=ll.elementAt(i).getTail();
            Node Xj=ll.elementAt(i).getHead();

            NodeList parents=new NodeList();
            
            featureTree_i = learningObject.learnConditional(Xi,parents,db,intervals,4);    
            
            parents.insertNode(Xi);
            
            featureTree_j = learningObject.learnConditional(Xj,parents,db,intervals,4);    
           
            
            valueMI=ContinuousProbabilityTree.estimateMutualInformation(featureTree_i,featureTree_j,5000);
            
            vResult.addElement(new Double(valueMI));
            
            System.out.println(i+" - I("+ll.elementAt(i).getTail().getName()+","+ll.elementAt(i).getHead().getName()+") = "+valueMI);
            
        }
        
        return vResult;
    }
    
   /**
     * Return a vector of m Nodes Xmax (features) included in NodeList S and with 
     * the highest I(Xmax,Xj|C) or I(Xj,Xmax|C) with respect to Xmax given as 
     * parameter.
     * @param m number of Nodes included in returned Vector.
     * @param S is a NodeList that included the computed nodes
     * @param ll is a LinkList of the complete graph among the features
     * @param vOrderedCMI is a vector of ordered indexes about CMI
     * @param Xmax is the destination node.
     * @return NodeList with the m best nodes.
    */
    
    private NodeList getNodesMaxMI(int m, NodeList S, LinkList ll, Vector vOrderedCMI, Node Xmax){
    
        NodeList vNodes=new NodeList();
               
        int i=0;
        while (vNodes.size()<m){
                     
            int index=((Integer)vOrderedCMI.elementAt(i)).intValue();
            Node head=ll.elementAt(index).getHead();
            Node tail=ll.elementAt(index).getTail();
            
            //if Xmax is in the actual link
            if ((Xmax.equals(head)) || (Xmax.equals(tail)))
            {   
               //If head is included in S
               if (S.getId(head.getName())!=-1) {
                   vNodes.insertNode(variables.getNode(head.getName()));
               }                
               //If tail is included in S
               if (S.getId(tail.getName())!=-1) {
                   vNodes.insertNode(variables.getNode(tail.getName()));
               }      
            }     
            i++;
        }
        return vNodes;
    }
    
    /**
 * Obtains the descending order of the numbers of the input vector
 * @param W <code>Vector</code> contains the mutual informations between
 * each feature variable and the class variable in the same order that 
 * the nodes in NodeList.
 * @return a <code>Vector</code> with the indexes position ordered by 
 * descending.
 *
 */ 
private Vector OrderedIndexByMutualInformation (Vector W){
   
     double max; 
     int index;
     
     Vector ordered_indexes=new Vector();
     Vector weight_aux=(Vector)W.clone();
     
     for (int i=0;i<weight_aux.size();i++){
        max=-1000.0;
        index=0;
        for (int j=0;j<weight_aux.size();j++){
            if (((Double)weight_aux.elementAt(j)).doubleValue()>max){
                max=((Double)weight_aux.elementAt(j)).doubleValue();
                index=j;              
            }
        }
        ordered_indexes.addElement(new Integer(index));
        weight_aux.setElementAt(new Double(-1000.0),index);
     }
     return ordered_indexes;
} 
      
    /**
     * Creates all the posible links between the feature variables
     *  
     * @param vars is a <code>NodeList</code> of the graph
     *
     * @return a <code>LinkList</code> 
     */     
    private LinkList CompleteLinkList(NodeList vars){
        
        LinkList list=new LinkList();
        Link l;
        
        for (int i=0;i<vars.size();i++) {
            for (int j=i+1;j<vars.size();j++) {        
                  //System.out.print("("+i+","+j+")");  
                   l = new Link(vars.elementAt(i),vars.elementAt(j),false);                
                   list.insertLink(l);
            }
        }
        return list;
      }
    
    
    /**
     * Saves the network in the given file.
     *
     * @param Name the name of the file where the net will be written.
     */  
    public void saveNetwork(String name) throws IOException {
        
        FileWriter f = new FileWriter(name);    
        net.saveBnet(f);
        f.close();
    }
     
  
       
    /**
     * Main for constructing an MTE kDB predictor from a database.
     *
     * Arguments:
     * 0. the dbc train file.
     * 1. the number of intervals into which the domain of the continuous 
     *    variables will be split.
     * 2. k = the maximum number of predictor variables as parents (k-DB)
     */
    public static void main(String args[]) throws ParseException,IOException {
       
       //Load input parameters
       FileInputStream fTrain = new FileInputStream(args[0]);   
       int interv= Integer.valueOf(args[1]).intValue();
       int k=Integer.valueOf(args[2]).intValue();

       DataBaseCases cTrain = new DataBaseCases(fTrain);

       kDBStructuralMTELearning kdbmodel = new kDBStructuralMTELearning(cTrain,interv,k); 
         
       kdbmodel.saveNetwork("kDBStructural.elv");
       
    }    
}

















