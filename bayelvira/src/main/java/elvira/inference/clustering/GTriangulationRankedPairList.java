package elvira.inference.clustering;


import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import elvira.Node;
import elvira.FiniteStates;
import elvira.Graph;
import elvira.NodeList;
import java.util.Random;
import java.lang.Math;

/**
 *
 * Class <code>GTriangulationRankedPairList.java</code>
 * Implements several operations to carry out in order to 
 * obtain a deletion sequence. In each time, the list mantain
 * a rank among the nodes not yet deleted, according to its
 * value (given by a selected heuristic)
 * 
 * @author Julia Flores
 * @author Jose A. Gámez
 * @since 22/6/2003
 */

public class GTriangulationRankedPairList {
   
/**
 * The ranked list
 */

private ArrayList list;


//
// -------------------- constructors ----------------
// 


/**
 * Creates an empty object
 */

public GTriangulationRankedPairList( ){
  list = new ArrayList();
}

/**

 */

public GTriangulationRankedPairList(Graph g,String criterion){
  list = new ArrayList(g.getNodeList().size());
  double val=0.0;
  int i,index;
  GTriangulationPair p;
  FiniteStates node;
  NodeList listOfNodes;
  String name;
  GTriangulation gt = new GTriangulation();
  
  listOfNodes = g.getNodeList();
  
  for(i=0;i<listOfNodes.size();i++){
    node = (FiniteStates)listOfNodes.elementAt(i);
    if (criterion.equals("minFill")){
      val = (double)gt.linksToAdd(node);
    } else if (criterion.equals("minSize")){
      val = (double)gt.numberOfVars(node); 
    } else if (criterion.equals("minWeight")){
      val = (double)gt.sizeOfClique(node); 
    } else if (criterion.equals("CanoMoral")){
        val = (double)(gt.sizeOfClique(node)/
         (double)((FiniteStates)node).getNumStates());
    } else if (criterion.equals("CanoMoral2")){
        val = (double)(gt.sizeOfClique(node)/gt.sizeOfSumCliques(node));
    } else {
        System.out.println("GTriangulationPairList: " + criterion + 
			" is not allowed as triangulation method\n");
        System.exit(0);
    }
    p = new GTriangulationPair(node,val);
    insertElement(p);
  }
  
}


/**
 * Creates an object and initialises it with all the nodes 
 * of <code>Graph</code> that are included in set (passed as argument. 
 * The rank is built by
 * using the value assigned to each node by the heuristic represented
 * by the parameter Criterion
 * @param set a <code>Vector</code> containing the set of distiguished variables
 * @param g a <code>Graph</code>
 * @param criterion a <code>String</code> identifying the criterion/heuristic 
 * to be used during triangulation
 *
 */

public GTriangulationRankedPairList(Vector set, Graph g,String criterion){
  list = new ArrayList(g.getNodeList().size());
  double val=0.0;
  int i,index;
  GTriangulation gt = new GTriangulation();
  GTriangulationPair p;
  FiniteStates node;
  NodeList listOfNodes;
  String name;
  
  listOfNodes = g.getNodeList();

  for(i=0;i<set.size();i++){
    node = (FiniteStates) set.elementAt(i);
    if (criterion.equals("minFill")){
      val = (double)gt.linksToAdd(node);
    } else if (criterion.equals("minSize")){
      val = (double)gt.numberOfVars(node);
    } else if (criterion.equals("minWeight")){
      val = (double)gt.sizeOfClique(node); 
    } else if (criterion.equals("CanoMoral")){
        val = (double)(gt.sizeOfClique(node)/
              (double)((FiniteStates)node).getNumStates());
    } else if (criterion.equals("CanoMoral2")){
        val = (double)(gt.sizeOfClique(node))/gt.sizeOfSumCliques(node);
    } else {
        System.out.println("GTriangulationPairList: " + criterion + 
			" is not allowed as triangulation method\n");
        System.exit(0);
    }
    p = new GTriangulationPair(node,val);
    insertElement(p);
  }
  
}

//
// ----------- access methods ------------------
//

/**
 * @param i an <code>int</code> indicating a position
 * @return the pair in position i
 */

public GTriangulationPair elementAt(int i){
  return (GTriangulationPair) list.get(i);
}

/**
 * add a new element at the end of the list
 * @param p the <code>GTriangulationPair<code> to be added
 */

public void addElement(GTriangulationPair p){
  list.add(p);
}

/**
 * @return the number of pairs in the list
 */
public int size( ){
  return list.size();
}

/** 
 * getIndexOfPair. 
 * @param i an <code>int</code> indicating a position in list
 * @return the pair in the required position
 */

public String getNameOfPair(int i){
  return elementAt(i).getNode().getName();
}

public Node getNodeOfPair(int i){
  return elementAt(i).getNode();
}

/** 
 * getValueOfPair. 
 * @param i an <code>int</code> indicating a position in list
 * @return the value of the pair stored in the passed position
 */

public double getValueOfPair(int i){
  return elementAt(i).getValue();
}

/**
 * getIndexOfNameInList
 * @param n a <code>String</code> indicating the name of a variable
 * @return the position with the pair whose name is the one passed as 
 * parameter. Return -1 if such a pair is not in the list
 */

public  int getIndexOfNodeInList(Node n){
  int j,pos;
  
  pos=-1;
  for(j=0;j<list.size();j++)
    if (n.getName().equals(getNameOfPair(j))) {pos=j;break;}
  return pos;
}

/**
 * removeElementAt: delete the pair in position p
 * @param p an <code>int</code> indicating a position in list
 */
public void removeElementAt(int p){
  list.remove(p);
}

/**
 * getNodeToEliminate: As we are using a ranked list, the next
 * node to be eliminated is the firs one in the list
 * 
 * @param arbitrary no if we want to break ties randomly. If yes
 * the pair in the first position is returned (arbitrary decision)
 * @param generator a <code>Random</code> generator to break ties
 * @return the <code>Node</code> to be eliminated next
 */

public Node getNodeToEliminate(String arbitrary,Random generator){
  int i;
  String minName;
  double minValue;
  int howMany;

  if (arbitrary.equals("no")){
    howMany = howManyEquals();
    return getNodeOfPair(generator.nextInt(howMany));
  }
  return getNodeOfPair(0);
}

/**
 * UpdateAllExceptIndex. 
 * Update the value of all the nodes in list but the one
 * passed as parameter
 *
 * @param vars a <code>NodeList</code> with the nodes to be updated
 * @param node a <code>Node</code> standing for the variable which
 * is not updated
 * @param g the <code>Graph</code> being triangulated
 * @param criterion a <code>String</code> with the criterion being used as heuristic
 */

public void updateAllExceptIndex(NodeList vars,Node node,
                         Graph g,String criterion)
 {

  int i,id,p;
  double val=0.0;
  GTriangulationPair pair;
  NodeList listOfNodes;
  GTriangulation gt = new GTriangulation();
  String currentName;
  Node currentNode;
  String name;
  
  name = node.getName();
  listOfNodes = g.getNodeList();
  for(i=0;i<vars.size();i++){
    currentNode = vars.elementAt(i);
    if (!name.equals(currentNode.getName())){
      p = getIndexOfNodeInList(currentNode);        
      if (criterion.equals("minFill")){
          val = gt.linksToAdd(currentNode);
      } else if (criterion.equals("minSize")){
          val = gt.numberOfVars(currentNode);
      } else if (criterion.equals("minWeight")){
          val = (double)gt.sizeOfClique(currentNode); 
      } else if (criterion.equals("CanoMoral")){
          val = (double) gt.sizeOfClique(currentNode)/
               (double)((FiniteStates)currentNode).getNumStates();
      } else if (criterion.equals("CanoMoral2")){
          val = (double)(gt.sizeOfClique(currentNode)/
                         gt.sizeOfSumCliques(currentNode));
      } else {
        System.out.println("\nPairList: " + criterion +
                " is not allowed as triangulation method\n");
        System.exit(0);
      }
      pair = elementAt(p);
      removeElementAt(p);
      pair.setValue(val);
      insertElement(pair);
    }
  }

}

/**
 * Insert a new element to the list, mantaining the rank.
 * @param e the <code>GTriangulationPair</code> to be inserted
 */

public void insertElement(GTriangulationPair e) {

  int min,max,middle;
  int s;
  double eValue,middleValue;


  eValue = e.getValue( );
  s = list.size();
  if (s == 0){
    middle = 0;
  }
  else if ( ((GTriangulationPair)list.get(s-1)).getValue() < eValue){
      middle = s;
  }
  else{
    for(min = 0, max = s-1, middle = (int) (min+max)/2; 
        min < max; 
        middle = (int) (min+max)/2){
      middleValue = ((GTriangulationPair)list.get(middle)).getValue();
      if (middleValue > eValue){
        max = middle;
      }
      else if (middleValue < eValue) {
        min = middle + 1;
      }
      else break; // middle is the possition 
    }
  }
  // Insert the element in middle
  list.add(middle,e);
}

/** 
 * howManyEquals.
 * @return how many pairs have the same value (all of them are optima)
 */

public int howManyEquals(){
  double first,val;
  int j,howMany=1;

  first = (double) getValueOfPair(0);
  for(j=1;j<list.size();j++){
    val = (double) getValueOfPair(j);
    if (first==val) howMany++;
    else break;
  }
  return howMany;
}


/**
 * Print the GTriangulationRankedPairList
 */

public void print(){
  int i;
  GTriangulationPair p;

  System.out.println("\nGTriangulationRankedPairList:");
  for(i=0;i<list.size();i++){
    p = elementAt(i);
    System.out.print("("+p.getNode().getName() + "," + p.getValue() + ")");
  }
}

} // end of class
