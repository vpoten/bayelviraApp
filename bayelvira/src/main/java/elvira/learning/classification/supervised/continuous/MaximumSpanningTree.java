/* MaximumSpanningTree.java */

package elvira.learning.classification.supervised.continuous;

import elvira.*;
import java.util.*;

/**
 * Class <code>MaximumSpanningTree</code> obtains the maximum spanning tree 
 * (MST) of a undirected graph using the kruskal's algorithm.
 *
 * @author Antonio Fern�ndez �lvarez (afalvarez@ual.es)
 * @since 16/02/2007
 */

public class MaximumSpanningTree {

   /**
    * Maximum Spanning Tree of the graph
    */
   private Graph MST;
  int mejor_raiz;
   
/**
 * Computes the MST variable
 * @param G <code>Graph</code>
 * @param W <code>Vector</code> contains the link weights (mutual information
 * of each pair of variables) in the same order that the links in <code>LinkList
 * </code>
 *
 */
public MaximumSpanningTree(Graph bn, Vector W) {
              
      
      System.out.println("\n\nCreating Maximum Spanning Tree ...");
    
      MST=new Graph();
      
      Vector vIndex=new Vector();
      Vector vWeights=new Vector();
      
      vIndex=ComputeWeightsOrder(W);
             
      System.out.println("\nOrdered indexes by �(Xi,Xj|C):    " +vIndex.toString());

      //kind of graph is UNDIRECTED
      MST.setKindOfGraph(1);
      
      MST.setNodeList(bn.getNodeList());
      int num_nodes=bn.getNodeList().size();
           
      int index;
      int i=0;
      int comp1, comp2;
      
      comp2=MST.connectedComponents().size()-1;
      
      while (MST.getLinkList().size()!=num_nodes-1){
              
          index=((Integer)(vIndex.elementAt(i))).intValue(); 
          comp1=comp2;
          
          try {      
              MST.createLink(((Link)(bn.getLinkList().getLinks().elementAt(index))).getTail(),
                             ((Link)(bn.getLinkList().getLinks().elementAt(index))).getHead(),false);            
          } catch (Exception e){System.out.println("The link cannot be created");};
          
          comp2=MST.connectedComponents().size()-1;
                 
          //if the last link inserted causes a cyclic graph, we remove it
          if (comp2==comp1){
            try {
              MST.removeLink(((Link)(bn.getLinkList().getLinks().elementAt(index))).getTail(),
                             ((Link)(bn.getLinkList().getLinks().elementAt(index))).getHead());
            } catch (Exception e){
                System.out.println("The link has been removed because it causes a cycle");
            };
          }
          i++;
      }         
   }

/**
 * Obtains the ascending order of the links basing on their weigths 
 * @param W <code>Vector</code> contains the link weights (mutual information
 * of each pair of variables) in the same order that the nodes in NodeList of G
 * @return a <code>Vector</code> with the indexes position of the Links in the 
 * LinkList of G in descending order. 
 *
 */ 
private Vector ComputeWeightsOrder (Vector W){
   
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
 * Returns the MST variable
 * 
 * @return a <code>Graph</code> with the Maximum Spanning Tree of the Graph G 
 *
 */ 
public Graph getMST(){
       
       return MST;
}

public void printLinks(){
      
      System.out.println("\nLink List: ");
      for (int i=0;i<MST.getLinkList().size();i++){
          System.out.print("("+((Link)MST.getLinkList().getLinks().elementAt(i)).getHead().getName()+
                             ","+((Link)MST.getLinkList().getLinks().elementAt(i)).getTail().getName()+")\n");      
      }
}

public void printWeights(Vector v){
  
      System.out.println("\n\n Links Weights: \n\n");
      for (int i=0;i<v.size();i++)
          System.out.println(((Double)v.elementAt(i)).doubleValue());
  }     
 
}

  
  
  
  
  