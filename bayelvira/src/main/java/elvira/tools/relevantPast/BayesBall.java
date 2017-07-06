
package elvira.tools.relevantPast;

import elvira.Network;
import elvira.IDiagram;
import elvira.Evidence;
import elvira.NodeList;
import elvira.Relation;
import elvira.Node;
import elvira.parser.ParseException;

import java.io.*;
import java.util.Vector;
import java.util.HashMap;
import java.util.Collection;

public class BayesBall{
  /**
   * Data member to store the network analyzed
   */
  private Network network;

  /**
   * Data member to store the set of nodes belonging to K
   */
  private NodeList inK;

  /**
   * Data member to store the set of nodes belonging to J
   */
  private NodeList inJ;

  /**
   * Data member to store the set of observed nodes
   */
  private NodeList inE;

  /**
   * Data member to store the pairs name-nodeInfo in a
   * Map
   */
  private HashMap<String,NodeInfoBayesBall> map;

  /**
   * Constructor
   */
  public BayesBall(Network network, NodeList inJ, NodeList inK){
    this.network=network;
    this.inJ=inJ;
    this.inK=inK;
    this.inE=null;

    // Create the map
    map=new HashMap<String,NodeInfoBayesBall>();

    // Mark observed nodes
    NodeList nodes=network.getNodeList();

    // For every node create and object of NodeInfoBayesBall
    Node node;
    NodeInfoBayesBall nodeInfo;
    for(int i=0; i < nodes.size(); i++){
      node=nodes.elementAt(i);

      // All the nodes will have a reference to map in order
      // to send balls to another nodes
      // Before that it is needed to check if the node belongs
      // to a deterministic relation. So we need to get the
      // relations for the node
      boolean deterministic=hasDeterministicRelation(node);
      nodeInfo=new NodeInfoBayesBall(node,deterministic,map);

      // If the node is in K, mark it
      if (inK.getId(node.getName()) != -1){
        nodeInfo.setInK(true);
      }
      else{
        // If it is in J, mark it
        if (inJ.getId(node.getName()) != -1){
          nodeInfo.setInJ(true);
        }
      }

      // Insert it into map
      map.put(node.getName(),nodeInfo);
    }
  }

  /**
   * Constructor
   */
  public BayesBall(Network network, NodeList inE){
    this.network=network;
    this.inJ=inJ;
    this.inK=inK;
    this.inE=inE;

    // Create the map
    map=new HashMap<String,NodeInfoBayesBall>();

    // Mark observed nodes
    NodeList nodes=network.getNodeList();

    // For every node create and object of NodeInfoBayesBall
    Node node;
    NodeInfoBayesBall nodeInfo;
    for(int i=0; i < nodes.size(); i++){
      node=nodes.elementAt(i);

      // All the nodes will have a reference to map in order
      // to send balls to another nodes
      // Before that it is needed to check if the node belongs
      // to a deterministic relation. So we need to get the
      // relations for the node
      boolean deterministic=hasDeterministicRelation(node);
      nodeInfo=new NodeInfoBayesBall(node,deterministic,map);

      // Insert it into map
      map.put(node.getName(),nodeInfo);
    }
  }

  /**
   * Method for dealing with the balls movements
   */
  public void ballsMoving(){
    // Consider the nodes belonging to J and send them balls
    // from an artificial child
    NodeList nodes=network.getNodeList();
    NodeInfoBayesBall nodeInfo;

    for(int i=0; i < nodes.size(); i++){
      nodeInfo=map.get(nodes.elementAt(i).getName());

      // If it is in J, program balls sending from child
      if (nodeInfo.isInJ()){
        nodeInfo.receiveBallFromChild();
      }
    } 
  }

  /**
   * Algorithm for running Bayes-Ball algorithm on influence
   * diagrams
   */
  public void ballsMovingOnId(){
    IDiagram submodel;
    Node decision, node;
    NodeList utilNodes;
    NodeList informativeParents;
    NodeInfoBayesBall nodeInfo;
    IDiagram diagCopy=((IDiagram)network).qualitativeCopy();
    NodeList relevantObservations=null;
    NodeList relevantProbs=null;
    
    // Remove informative arcs on diagCopy
    //diagCopy.removeNonForgettingArcs();

    // Get the list of decisions
    NodeList decisions=diagCopy.getDecisionList();

    // Consider every decision one by one
    for(int i=decisions.size()-1; i >= 0; i--){
      decision=decisions.elementAt(i);
System.out.println("Decision: "+decision.getName());
      
      // Get the relevant value nodes for this decision. This set will
      // be J
      utilNodes=getChildrenValueNodes(decision);

      // Ii relevantObservations is not null, add them to utilNodes
      if (relevantObservations != null){
         utilNodes.join(relevantObservations);
      }

      // Get a new NodeList containing to decision and all its informative
      // parents
      informativeParents=decision.getParentNodes();
      // Add decision to this list. This final set will be K
      informativeParents.insertNode(decision);
System.out.println("Padres informativos de : "+decision.getName());
for(int k=0; k < informativeParents.size(); k++){
  Node nodeParent=informativeParents.elementAt(k);
  System.out.println("    Padre: "+nodeParent.getName());
}
System.out.println("------------------------------------------");

      // Mark nodes inJ
      setJ(utilNodes);

      // The same for inK
      setK(informativeParents);

      // Method for starting the balls sending
      startBallsSending();

      // Present the info about this stage
System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::");
System.out.println("Ciclo: "+i);
      relevantObservations=presentInfo(decision,informativeParents);
System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::");
System.out.println("State of nodes.......................................");
print();
System.out.println("-----------------------------------------------------");

      // At the end resets the marks of inJ and inK to prepare the
      // new stage
      resetMarksInJInK();
    }

    // At the end, the final stage consider relevantObservations as J and
    // the set of observed nodes as K
    setK(inE);
    setJ(relevantObservations);

    // Make the balls to begin the movements
    startBallsSending();

    // Present the info about the stage
    relevantObservations=new NodeList();
    relevantProbs=new NodeList();
System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::");
System.out.println("Final...............................................:");
    presentInfo(relevantObservations,relevantProbs);
System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::::");
  }

  /**
   * Method for presenting the info about the application of the algorithm
   * @param decision node just analyzed
   * @param informativeParents nodes acting as K
   * @return relevantObservations
   */
  public NodeList presentInfo(Node decision, NodeList informativeParents){
    NodeInfoBayesBall nodeInfo;
    NodeList relevantObservations=new NodeList();
    NodeList relevantProbs=new NodeList();
    Node node;

    // Get the nodeInfo about the decision
    nodeInfo=map.get(decision.getName());

    // Show if it is relevant or not
    System.out.println(decision.getName()+ ": "+nodeInfo.getVisited());

    // Get the nodes being relevant observations: those nodes
    // belonging to K and marked as visited
    for(int i=0; i < informativeParents.size(); i++){
      node=informativeParents.elementAt(i);
System.out.println("Considerando antecedente informativo............");
      // Consider it if it is not decision
      if (node != decision){
        // Check if visited
        nodeInfo=map.get(node.getName());
        if (nodeInfo.getVisited() && relevantObservations.getId(node.getName()) == -1){
System.out.println("      relevant observation: "+node.getName());
          relevantObservations.insertNode(node);
        }
      }
    }

    // Get the relevantProbs: those nodes marked at the top
    Collection<NodeInfoBayesBall> info=map.values();
    for(NodeInfoBayesBall nodeInf : info){
      // Check if it is marked at the top
      if (nodeInf.getTopMarked() && relevantProbs.getId(nodeInf.getNode().getName()) == -1){
        relevantProbs.insertNode(nodeInf.getNode());
System.out.println("      relevant probs: "+nodeInf.getNode().getName());
      }
    }

    // Return relevantObservations
    return relevantObservations;
  }

  /**
   * Method for presenting the info about the application of the algorithm
   * @param relevantObservations
   * @param relevantProbs
   */
  public void presentInfo(NodeList relevantObservations, NodeList relevantProbs){
    NodeInfoBayesBall nodeInfo;
    Node node;

    // Get the nodes being relevant observations: those nodes
    // belonging to E and marked as visited
    for(int i=0; i < inE.size(); i++){
      node=inE.elementAt(i);
      // Check if visited
      nodeInfo=map.get(node.getName());
      if (nodeInfo.getVisited() && relevantObservations.getId(node.getName()) == -1){
System.out.println("      relevant observation: "+node.getName());
         relevantObservations.insertNode(node);
      }
    }

    // Get the relevantProbs: those nodes marked at the top
    Collection<NodeInfoBayesBall> info=map.values();
    for(NodeInfoBayesBall nodeInf : info){
      // Check if it is marked at the top
      if (nodeInf.getTopMarked() && relevantProbs.getId(nodeInf.getNode().getName()) == -1){
        relevantProbs.insertNode(nodeInf.getNode());
System.out.println("      relevant probs: "+nodeInf.getNode().getName());
      }
    }
  }

  /**
   * Method for resetting the marks inJ and inK
   */
  public void resetMarksInJInK(){
    Collection<NodeInfoBayesBall> info=map.values();
    for(NodeInfoBayesBall nodeInfo : info){
      nodeInfo.setInK(false);
      nodeInfo.setInJ(false);
    }
  }

  /**
   * Method for printing
   */
  public void print(){
    Collection<NodeInfoBayesBall> values=map.values();
    for(NodeInfoBayesBall nodeInfo : values){
      nodeInfo.print();
    }
  }

  /**
   * Method for setting a set of nodes as J
   * @param nodes
   */
  public void setJ(NodeList nodes){
    NodeInfoBayesBall nodeInfo;
    Node node;

    for(int i=0; i < nodes.size(); i++){
      node=nodes.elementAt(i);

      // Get the nodeInfo
      nodeInfo=map.get(node.getName());
      nodeInfo.setInJ(true);
    }  
  }

  /**
   * Method for setting a set of nodes as K
   * @param nodes
   */
  public void setK(NodeList nodes){
    NodeInfoBayesBall nodeInfo;
    Node node;

    for(int i=0; i < nodes.size(); i++){
      node=nodes.elementAt(i);

      // Get the nodeInfo
      nodeInfo=map.get(node.getName());
      nodeInfo.setInK(true);
    }  
  }

  /**
   * Method for starting the balls movements
   */
  public void startBallsSending(){
    Collection<NodeInfoBayesBall> info=map.values();

    // Now send balls to the nodes in J
    for(NodeInfoBayesBall nodeInfo : info){
      // If the node is in J, begin sending a ball as if it
      // were sent by one child
      if (nodeInfo.isInJ()){
        nodeInfo.receiveBallFromChild();
      }
    }  
  }

  /**
   * Main method
   */
  public static void main(String args[]) throws ParseException, IOException{
    BayesBall object;
    FileInputStream file;
    Network network=null;
    NodeList inJ=new NodeList();
    NodeList inK=new NodeList();
    NodeList inE=new NodeList();
    NodeList nodes=null;
    Node node=null;
    int i,j;
    int kind=0;
    
    // Check there are three arguments: network, evidence 
    // for J and evidence for K and observed nodes. Only the
    // network is needed
    if (args.length < 1){
      printUsage();
    }
    else{
      i=0;
      while(i < args.length){
        if (args[i].equals("-net")){
          network=Network.read(args[1]); 
System.out.println("Red leida.............................");
network.save("kk");
System.out.println("--------------------------------------");
          nodes=network.getNodeList();
          i=i+2;
        }
        else{
          if (args[i].equals("-j")){
            // Add one to i
            i++;

            // Read the nodes
            while(!args[i].equals("-k")){
              // Read the nodes in j
              node=nodes.getNode(args[i]);

              //Check if null
              if (node == null){
                System.out.println("Node "+args[i]+" not in network....");
                System.exit(0);
              }
              else{
                inJ.insertNode(node);

                // Add one to i
                i++;
              }
            }
          }
          else{
            if (args[i].equals("-k")){
              // From here to the end, read the nodes in k
              j=i+1;
              while(j < args.length){
                node=nodes.getNode(args[j]);

                // Check if null
                if (node == null){
                  System.out.println("Node "+args[i]+" not in network....");
                  System.exit(0);
                }
                else{
                  inK.insertNode(node);
                  j++;
                }
              }

              // Make i equals to j again
              i=j;
            }
            else{
              if (args[i].equals("-e")){
                // Read the nodes in E (observed nodes)
                kind=1; //Application for influence diagrams
                j=i+1;
                while(j < args.length){
                  node=nodes.getNode(args[j]);

                  // Check if null
                  if (node == null){
                    System.out.println("Node "+args[i]+" not in network....");
                    System.exit(0);
                  }
                  else{
                    inE.insertNode(node);
                    j++;
                  }
                }

                // Make i equals to j again
                i=j;
              }
              else{
                printUsage();
              }
            }
          }
        }
      }
    }
   
    // Create an objet
    if (kind == 0)
       object=new BayesBall(network,inJ,inK);
    else
      object=new BayesBall(network,inE);

    if (network instanceof IDiagram)
      object.ballsMovingOnId();
    else
      object.ballsMoving();

    // Print the results
    object.print();
  }  

  /**
   * Method for printing the way to use this class
   */
  public static void printUsage(){
    System.out.println("Use: ");
    System.out.println("java elvira.relevantPast.BayesBall -net <net.elv>");
    System.out.println("[-j <var1 ...... varn>] [-k <var1 ...... varn>]");
    System.out.println("[-e <var1 ...... varn>]");
    System.exit(0);
  }

  /**
   * Private method for checking if a node belong to a deterministic
   * relation
   * @param node
   * @return boolean
   */
   private boolean hasDeterministicRelation(Node node){
      Vector relations=network.getRelationList();
      Relation rel;
      Node firstNode;
      boolean deterministic=false;

      for(int i=0; i < relations.size() && !deterministic; i++){
        rel=(Relation)relations.elementAt(i);

        // Check if node is the first node involved in the relation
        firstNode=rel.getVariables().elementAt(0);
        if (firstNode.getName().equals(node.getName())){
          if (rel.isDeterministic()){
            deterministic=true;
          }
        }
      }

      // Return deterministic
      return deterministic;
   }

   /**
    * Method for getting the list of value nodes related to a given
    * node
    * @param node
    * @return nodeList
    */
   private NodeList getChildrenValueNodes(Node node){
     NodeList children=node.getChildrenNodes();
     NodeList result=new NodeList();
     Node nodeRef;

     for(int i=0; i < children.size(); i++){
       nodeRef=children.elementAt(i);
       if (nodeRef.getKindOfNode() == Node.UTILITY){
         result.insertNode(nodeRef);
       }
     }

     // Return result
     return result;
   }
}
