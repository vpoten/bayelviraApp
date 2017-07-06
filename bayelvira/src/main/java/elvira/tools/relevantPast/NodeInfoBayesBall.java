
package elvira.tools.relevantPast;

import elvira.Node;
import elvira.NodeList;

import java.util.HashMap;

public class NodeInfoBayesBall{
  /**
   * Data member to store the node related to this object
   */
  private Node node;

  /**
   * Data member to show if the node is visited
   */
  private boolean visited;

  /**
   * Data member to store if the node is marked on its top
   */
  private boolean topMarked;

  /**
   * Data member to store if the node is marked at the bottom
   */
  private boolean bottomMarked;

  /**
   * Data member to store if the node belongs to K (the set of
   * conditioning variables)
   */
  private boolean inK;

  /**
   * Data node to show if the node is in J set
   */
  private boolean inJ;

  /**
   * Data member to store if the node is deterministic
   */
  private boolean deterministic;

  /**
   * Data member to store a reference to the map containing info
   * of all the nodes
   */
  private HashMap<String,NodeInfoBayesBall> map;

  /**
   * Constructor
   * @param node
   */
  public NodeInfoBayesBall(Node node,boolean deterministic,HashMap<String,NodeInfoBayesBall> map){
    // Set node
    this.node=node;

    // Set deterministic
    this.deterministic=deterministic;

    // Set map
    this.map=map;

    // Set visited equals to false
    visited=false;

    // Set topMarked equals to false
    topMarked=false;

    // Set bottomMarked set to false
    bottomMarked=false;

    // Set inK to false
    inK=false;

    // Set inJ to false
    inJ=false;
  }

  /**
   * Method for getting node value
   */
  public Node getNode(){
    return node;
  }

  /**
   * Method for getting the value of visited
   */
  public boolean getVisited(){
    return visited;
  }

  /**
   * Method for setting the top mark
   */
  public void markTop(){
    topMarked=true;
  }

  /**
   * Method for returning the value of topMarked
   */
  public boolean getTopMarked(){
    return topMarked;
  }

  /**
   * Method for setting the bottom mark
   */
  public void markBottom(){
    bottomMarked=true;
  }

  /**
   * Method for setting visited data member
   */
  public void setVisited(){
    visited=true;
  }

  /**
   * Method for checking of the node is deterministic
   */
  public boolean getDeterministic(){
    return deterministic;
  }

  /**
   * Method for setting the inK data member
   */
  public void setInK(boolean state){
    inK=state;
  }

  /**
   * Method for setting inJ data member
   */
  public void setInJ(boolean state){
    inJ=state;
  }

  /**
   * Method for getting tha inJ value
   */
  public boolean isInJ(){
    return inJ;
  }

  /**
   * Method for attending incoming balls from child
   */
  public void receiveBallFromChild(){
    NodeList parents=node.getParentNodes();
    NodeList children=node.getChildrenNodes();
    NodeInfoBayesBall nodeInfo;

    // Mark the node as visited
    visited=true;

    // The things to do will depend on the kind of node
    // If the node does not belong to K, program visits
    // to parents if needed
    if (!inK){
      // If the top part is not marked, program calls
      // to children
      if (topMarked == false){
         // Mark it
         topMarked=true;

         // Visit the parents
         for(int i=0; i < parents.size(); i++){
           nodeInfo=map.get(parents.elementAt(i).getName());
           nodeInfo.receiveBallFromChild();
         }
      }

      // If the node is not deterministic, program visits to
      // children if needed
      if (!deterministic){
        // Visit children only if the bottom is not marked
        if (bottomMarked == false){
          // Mark it
          bottomMarked=true;

          // Visit the childs
          for(int i=0; i < children.size(); i++){
             nodeInfo=map.get(children.elementAt(i).getName());
             nodeInfo.receiveBallFromParent();
          }
        }
      }
    }
  }

  /**
   * Method for attending incoming balls from parent
   */
  public void receiveBallFromParent(){
    NodeList parents=node.getParentNodes();
    NodeList children=node.getChildrenNodes();
    NodeInfoBayesBall nodeInfo;

    // Mark the node as visited
    visited=true;

    // The things to do will depend on the kind of node
    // If the node belong to K, program visits
    // to parents if needed
    if (inK){
      // If the top part is not marked, program calls
      // to children
      if (topMarked == false){
         // Mark it
         topMarked=true;

         // Visit the parents
         for(int i=0; i < parents.size(); i++){
           nodeInfo=map.get(parents.elementAt(i).getName());
           nodeInfo.receiveBallFromChild();
         }
      }
    }
    else{
      // Visit children only if the bottom is not marked
      if (bottomMarked == false){
        // Mark it
        bottomMarked=true;

        // Visit the childs
        for(int i=0; i < children.size(); i++){
           nodeInfo=map.get(children.elementAt(i).getName());
           nodeInfo.receiveBallFromParent();
        }
      }
    }
  }

  /**
   * Method for printing the data members values
   */
  public void print(){
    System.out.println("--------------------------------------");
    System.out.println("Node: "+node.getName());
    System.out.println("Visited: "+visited);
    System.out.println("Top marked: "+topMarked);
    System.out.println("Bottom marked: "+bottomMarked);
    System.out.println("Is in J: "+inJ);
    System.out.println("Is in K: "+inK);
    System.out.println("Deterministic: "+deterministic);
  }

}


