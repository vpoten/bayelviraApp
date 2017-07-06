
package elvira.inference.clustering.lazyid;

import elvira.Node;
import elvira.FiniteStates;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.tools.idiagram.pairtable.IDPairTable;
import elvira.potential.Potential;
import elvira.potential.PotentialTree;

import java.util.ArrayList;
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class JunctionTreeNode{
  /**
   * Data member to store the list of variables defined on
   * this clique
   */
  NodeList variables;
   
  /**
   * Data member to store the upward neighbour of the node
   */
  JunctionTreeSeparator up;

  /**
   * Set of separators to store the downward neighbours of the node
   */
  ArrayList<JunctionTreeSeparator> down;

  /**
   * Set of probability potentials related to the clique represented 
   * with this node
   */
  HashMap<Node,ArrayList<Potential>> probPotentials;

  /**
   * Set of utility potentials related to the clique represented
   * with this node
   */
  HashMap<Node,ArrayList<Potential>> utilPotentials;

  /**
   * Set of constraint potentials related to the clique represented 
   * with this node
   */
  HashMap<Node,ArrayList<Potential>> constraintPotentials;

  /**
   * Data member to store the index of the clique
   */
  int index;

  /**
   * Data member to store a reference to the StrongJunctionTree where
   * the node is inserted
   */
  protected StrongJunctionTree tree;

  /**
   * Constructor
   * @param tree 
   * @param variables
   * @param index
   */
  public JunctionTreeNode(StrongJunctionTree tree,NodeList variables,int index){
    this.tree=tree;
    this.variables=variables;

    // Assign the index
    this.index=index;

    //Create the arraylist for downward neighbours
    down=new ArrayList<JunctionTreeSeparator>();

    // Create the map to store the probability potentials
    probPotentials=new HashMap<Node,ArrayList<Potential>>();

    // The same for utility potentials
    utilPotentials=new HashMap<Node,ArrayList<Potential>>();
  }

  /**
   * Method to print the data of the junction tree node
   */
  public void print(){
    JunctionTreeSeparator separator;
    JunctionTreeNode neighbour;
    Node node;
    
    System.out.println("-------- CLIQUE WITH INDEX: "+index+" ------------");    
    // Print the variables of the clique
    System.out.println("Index: "+index);
    System.out.println("\n\n ----------------- Variables -------------------");
    for(int i=0; i < variables.size(); i++){
      node=variables.elementAt(i);
      System.out.println(node.getName());
    }

    // Print the potentials related to the clique, variable to
    // variable
    System.out.println(" ........ PROBABILITY POTENTIALS ..........");
    ArrayList<Potential> pots;
    Potential pot;
    for(int i=0; i < variables.size(); i++){
      node=variables.elementAt(i);
      System.out.println("... Para variable: "+node.getName());
      pots=probPotentials.get(node);
      if (pots != null){
        for(int j=0; j < pots.size(); j++){
          System.out.println("-----------------"+j+"------------------");
          pot=pots.get(j);
          //pot.print();
          System.out.println("-----------------------------------------");
        }
      }
    }
    System.out.println(" ........ UTILITY POTENTIALS ..........");
    for(int i=0; i < variables.size(); i++){
      node=variables.elementAt(i);
      System.out.println("... Para variable: "+node.getName());
      pots=utilPotentials.get(node);
      if (pots != null){
        for(int j=0; j < pots.size(); j++){
          System.out.println("-----------------"+j+"------------------");
          pot=pots.get(j);
          pot.print();
          System.out.println("-----------------------------------------");
        }
      }
    }

    for(int i=0; i < down.size(); i++){
      separator=down.get(i);

      // Call the print method on it
      System.out.println("    Down separator number: "+ i);
      separator.print();
    }

    // Print now the inferior neighbours
    for(int i=0; i < down.size(); i++){
      separator=down.get(i);
      neighbour=separator.getInferiorNeighbour();

      // Call the method to print the clique
      System.out.println("        Inferior neighbour: "+i);
      neighbour.print();
    }
  }

  /**
   * Method to print the data of the junction tree node
   * without traversing the tree: only the node it is printed
   */
  public void printAlone(){
    JunctionTreeSeparator separator;
    JunctionTreeNode neighbour;
    Node node;
    
    // Print the variables of the clique
    System.out.println("Indice: "+index);
    System.out.println("\n\n ----------------- Variables -------------------");
    for(int i=0; i < variables.size(); i++){
      node=variables.elementAt(i);
      System.out.println(node.getName());
    }

    // Print the potentials related to the clique, variable to
    // variable
    System.out.println(" ........ PROBABILITY POTENTIALS ..........");
    ArrayList<Potential> pots;
    Potential pot;
    for(int i=0; i < variables.size(); i++){
      node=variables.elementAt(i);
      System.out.println("... Para variable: "+node.getName());
      pots=probPotentials.get(node);
      if (pots != null){
        for(int j=0; j < pots.size(); j++){
          System.out.println("-----------------"+j+"------------------");
          pot=pots.get(j);
          pot.print();
          System.out.println("-----------------------------------------");
        }
      }
    }
    System.out.println(" ........ UTILITY POTENTIALS ..........");
    for(int i=0; i < variables.size(); i++){
      node=variables.elementAt(i);
      System.out.println("... Para variable: "+node.getName());
      pots=utilPotentials.get(node);
      if (pots != null){
        for(int j=0; j < pots.size(); j++){
          System.out.println("-----------------"+j+"------------------");
          pot=pots.get(j);
          pot.print();
          System.out.println("-----------------------------------------");
        }
      }
    }

    for(int i=0; i < down.size(); i++){
      separator=down.get(i);

      // Call the print method on it
      System.out.println("    Down separator number: "+ i);
      separator.print();
    }
  }

  /**
   * Public method for printing the structure of the tree
   * @param white spaces to print
   */
  public void printStructure(int spaces){
    int newSpaces;

    JunctionTreeNode inferiorNeigbour;
    
    // Print the index
    System.out.println(index);
    // Print the white spaces
    for(int i=0; i < spaces; i++)
      System.out.print(" ");
    System.out.println(" |");

    // Consider the inferior separators and print them
    for(JunctionTreeSeparator separator : down){
      // Print the structure of the separator and follow with the
      // node related to it
      newSpaces=spaces+2;
      separator.printStructure(newSpaces);
    }  
  }

  /**
   * Public method to accomodate a clique into a node
   * @param clique to connect
   * @param intersection separator of the clique
   * @param index of the clique
   * @return boolean condition
   */
  public boolean accomodate(NodeList clique, NodeList separator,int index){
    JunctionTreeSeparator separatorNode;
    int trials=0;
    boolean accomodated=false;

    // Check if the variables in separator are contained in
    // the set of variables of this clique
    if (separator.size() != 0 && separator.isIncluded(variables)){
      // If the separator is contained in the variables of
      // this node, add it as down neighbour. First at all,
      // create a new node
      JunctionTreeNode node=buildTreeNode(tree,clique,index);

      // Add it as down neighbour
      addDownwardNeighbour(node,separator);

      // Return true
      accomodated=true;
    }
    else{
      // Recursively call to this method on the downward
      // neighbours untill we receive a true as answer
      while(!accomodated && trials < down.size()){
        // Select a down neighbour
        separatorNode=down.get(trials);

        // Call accomodate on node
        accomodated=separatorNode.getInferiorNeighbour().accomodate(clique,separator,index);

        // Add one to trials
        trials++;
      }
    }

    // Return the value of accomodated
    return accomodated;
  }

  /**
   * Method for building a new tree node
   * @param tree
   * @param clique nodelist to include in the node
   * @param index of the node
   */
  protected JunctionTreeNode buildTreeNode(StrongJunctionTree tree, 
                                         NodeList clique, int index){
    JunctionTreeNode node=new JunctionTreeNode(tree,clique,index);
    return node;
  }

  /**
  * Method for applying constraints
  * @param boolean flag for showing if inferior neigbourghs must be
  * considered
  */
  protected void applyConstraints(boolean goDown){
  }

  /**
   * Method for collectEvidence. This method will be allways called
   * from the superior neighbour
   */
  public void collectEvidence(){
    JunctionTreeNode inferiorNeighbour;
    
    // The method check if the inferior neighbour is a leaf node. In
    // such case, there is no more invocations to collect: only to
    // absorb
    for(int i=0; i < down.size(); i++){
      inferiorNeighbour=down.get(i).getInferiorNeighbour();

      // Check if it is a leaf node
      if (inferiorNeighbour.isLeafNode()){
        // Call to absorbEvidence on it. This will make the marginalizations
        // required at it will charge the potentials in the separator
        inferiorNeighbour.absorbEvidence();
      }
      else{
        // It is needed a par of calls: collect and after that absorb
        inferiorNeighbour.collectEvidence();
        inferiorNeighbour.absorbEvidence();
      }
    }
  }

  /**
   * Method for absorbing the evidence from inferior neighbours
   */
  public void absorbEvidence(){
    ArrayList<Node> varsToRemove=new ArrayList<Node>();
    Node node;
    int variableEliminationCriteria=tree.getVariableEliminationCriteria();
    
    // Determine the variables to remove
    for(int i=0; i < variables.size(); i++){
      node=variables.elementAt(i);

      // Check if the node it is in the separator list of variables
      if (!up.isVariablePresent(node)){
        // This variable must be deleted
        varsToRemove.add(node);
      }
    }

    // If the array list contains more than two variables,  then
    // they must be ordered respect to the criteria used:
    // (a) elimination order used for triangulation
    // (b) on-line triangulation with the same criteria employed
    // for triangulation of the whole graph
    if (varsToRemove.size() > 1){
      switch(variableEliminationCriteria){
        case StrongJunctionTree.OFFLINE_TRIANGULATION: varsToRemove=tree.orderVariablesWithOfflineTriangulation(varsToRemove);
                                    break;
        case StrongJunctionTree.ONLINE_TRIANGULATION: varsToRemove=orderVariablesWithOnlineTriangulation(varsToRemove);
                                    break;
        default: System.out.println("Invalid criteria for ordering variables to remove");
                 System.out.println("Method: absorbEvidence     Class: JunctionTreeNode");
                 System.exit(0);
      }
    }

    // Finally, call the method for removing the variables
    removeVariables(varsToRemove);
  }

  /**
   * Method for ordering the variables to remove with an on-line triangulation. It will be
   * used the same criteria employed for the triangulation of the whole graph
   * @param varsToRemove array with the variables to order
   * @param orderedVars
   */
  private ArrayList<Node> orderVariablesWithOnlineTriangulation(ArrayList<Node> varsToRemove){
      IDPairTable pairTable=new IDPairTable();
      RelationList relationsForVar;
      Relation relation;
      FiniteStates variable;
      ArrayList<RelationList> relationsForVars;
      ArrayList<Node> orderedVars=new ArrayList<Node>();
      Node varToRemove;

    // Fix the criteria for the triangulation
    pairTable.setIDCriteria(tree.getTriangulationCriteria());

    // It is needed to get the relations containing the variables of the potentials for
    // every variable to remove
    for(int i=0; i < varsToRemove.size(); i++){
      variable=(FiniteStates)varsToRemove.get(i);

      // Make relations from the potentials where var appears
      relationsForVar=makeRelationsFromVarPotentials(variable); 

      // Add the variable and the relations to the pairTable
      pairTable.addElement(variable);

      // Add the relations
      for(int j=0; j < relationsForVar.size(); j++){
        relation=relationsForVar.elementAt(j);
        pairTable.addRelation(variable,relation);
      }
    }

    // Now the variables must be ordered according to the selected criteria

    for(int i=0; i < varsToRemove.size(); i++){
      varToRemove=pairTable.nextToRemoveIDWithCriteriaRemoving();

      // Print debug information if needed
      if (tree.getDebugFlag()){
         System.out.println("Variable "+i+" a eliminar : "+varToRemove.getName());
      }
      orderedVars.add(varToRemove);
    }

    // Return orderedVars
    return orderedVars;
  }

  /**
   * Method for getting a RelationList containing relations linking the
   * variables of the potentials where the variable passed as argument
   * appears
   * @param varToRemove
   * @return relations
   */
  private RelationList makeRelationsFromVarPotentials(Node varToRemove){
    RelationList relations=new RelationList();
    Potential pot;
    Relation rel;

    // Get the potentials where varToRemove appears
    ArrayList<Potential> probPotentialsVar=getProbabilityPotentials(varToRemove,false);
    ArrayList<Potential> utilPotentialsVar=getUtilityPotentials(varToRemove,false);
    ArrayList<Potential> sepProbPotentials=getInferiorSeparatorsProbPotentials(varToRemove,false);
    ArrayList<Potential> sepUtilPotentials=getInferiorSeparatorsUtilPotentials(varToRemove,false);

    // Now make a new "artificial" relation for every potential
    if (probPotentialsVar != null){
      for(int i=0; i < probPotentialsVar.size(); i++){
        pot=probPotentialsVar.get(i);
        rel=new Relation(pot);

        // Add the relation to relations
        relations.insertRelation(rel);
      }
    }

    if (utilPotentialsVar != null){
      for(int i=0; i < utilPotentialsVar.size(); i++){
        pot=utilPotentialsVar.get(i);
        rel=new Relation(pot);

        // Add it to relations
        relations.insertRelation(rel);
      }
    }

    if (sepProbPotentials != null){
      for(int i=0; i < sepProbPotentials.size(); i++){
        pot=sepProbPotentials.get(i);
        rel=new Relation(pot);

        // Add it to relations
        relations.insertRelation(rel);
      }
    }

    if (sepUtilPotentials != null){
      for(int i=0; i < sepUtilPotentials.size(); i++){
        pot=sepUtilPotentials.get(i);
        rel=new Relation(pot);

        // Add it to relations
        relations.insertRelation(rel);
      }
    }

    // Finally, return the relationList
    return relations;
  }

  /**
   * Method to compute the decision table for a clique containing one
   * of the decisions
   * @param decision to compute its table
   */
  public void computeDecisionTable(Node decision){
    // Get the relevant past for decision
    NodeList past=tree.diag.getRelevantPast(decision);
    ArrayList<Node> toRemoveArray=new ArrayList<Node>();
    ArrayList<Node> toRemoveArrayOrdered=null;
    Node node;

    if (tree.getDebugFlag()){
      System.out.println("----------------- computeDecisionTable ---------------");
      System.out.println("      Para decision: "+decision.getName());
    }

    // Add information for statistics in order to identify operations
    // required for computing decision tables
    if (tree.getStatisticsFlag()){
      tree.statistics.addOperation("------");
      tree.statistics.addSize(0);
      tree.statistics.addTime(tree.crono.getTime());
    }

    
    // The rest of variables must be deleted
    NodeList toRemove=variables.differenceNames(past);

    // Pass the variables to an arrayList
    for(int i=0; i < toRemove.size(); i++){
      toRemoveArray.add(toRemove.elementAt(i));
    }

    // Now, remove the variables; before that impose an order between
    // them, according to the criteria used for removing. The removal must
    // be done with the same order as the variables belonging to the past
    toRemoveArrayOrdered=tree.orderVariablesWithOfflineTriangulation(toRemoveArray);

    // Now, remove these variables
    removeVariables(toRemoveArrayOrdered);
  }

  /**
   * Private method for removing the variables of the clique
   * @param varsToRemove arraylist with the variables to be
   *        removed
   */
  private void removeVariables(ArrayList<Node> varsToRemove){
    int last=varsToRemove.size();
    boolean lastFlag=false;
    int i=0;

    // Iterate on the arraylist to remove the variables stored
    // on it
    for(Node node : varsToRemove){
      if (tree.getDebugFlag()){
         System.out.println("\n\n\n Eliminado: "+node.getName()+"\n\n");
      }

      // Add one to i
      i++;

      // If it is the last, pass true as second argument
      if (i == last)
        lastFlag=true;

      switch(node.getKindOfNode()){
        case Node.CHANCE: removeChanceVariable(node,lastFlag);
                          break;
        case Node.DECISION: removeDecisionVariable(node,lastFlag);
                            break;
        default: System.out.println("Invalid node kind: "+node.getKindOfNode());
                 System.exit(0);
      }
System.out.println("Eliminado: "+node.getName());
System.out.println(".....................................");
    }

    // Set the potentials to the up separator
    if (tree.getDebugFlag()){
      System.out.println();
      System.out.println("Asigna potenciales al separador superior........");
      System.out.println("Ultima operacion sobre el clique: "+index);
      System.out.println();
    }

    if (up != null){
      setPotentialsToUpSeparator();
    }

    // Print statistics information
    if (tree.getStatisticsFlag()){
      if (lastFlag){
        tree.statistics.addSize(tree.getSize());
        tree.statistics.addTime(tree.crono.getTime());
      }
    }
  }

  /**
   * Public method for removing a chance node
   * @param node to remove
   * @param lastFlag to show if potentials must be sent to the upper separator
   */
  public void removeChanceVariable(Node node, boolean lastFlag){
    // Look for the potentials related to this variable. Part of them
    // will be on the same clique

    ArrayList<Potential> cliqueNodeProbPotentials=getProbabilityPotentials(node,true);
    ArrayList<Potential> infSeparatorsNodeProbPotentials=getInferiorSeparatorsProbPotentials(node,true);
    ArrayList<Potential> cliqueNodeUtilPotentials=getUtilityPotentials(node,true);
    ArrayList<Potential> infSeparatorsNodeUtilPotentials=getInferiorSeparatorsUtilPotentials(node,true);

    if (tree.getDebugFlag()){
      System.out.println("----------------- removeChanceVariable ---------------");
      System.out.println("Clique: "+index);
      System.out.println("--------------- Nodo : "+node.getName());
      if (cliqueNodeProbPotentials != null)
         System.out.println("P. de prob. en clique: "+
                            cliqueNodeProbPotentials.size());
      if (infSeparatorsNodeProbPotentials != null)
         System.out.println("P. de prob. en sep. : "+
                            infSeparatorsNodeProbPotentials.size());
      if (cliqueNodeUtilPotentials != null)
         System.out.println("P. de util en clique: "+
                            cliqueNodeUtilPotentials.size());
      if (infSeparatorsNodeUtilPotentials != null)
         System.out.println("P. de util en sep. :"+
                            infSeparatorsNodeUtilPotentials.size());
    }

    if (tree.getStatisticsFlag()){
      tree.statistics.addOperation(node.getName()); 
    }
    
    // Combine the probability potentials
    Potential probPots=combineProbabilityPotentials(cliqueNodeProbPotentials,
                                                    infSeparatorsNodeProbPotentials);

    // Combine the utility potentials
    Potential utilPots=combineUtilityPotentials(cliqueNodeUtilPotentials,
                                                infSeparatorsNodeUtilPotentials);

    // Combine probability and utility potentials
    if (probPots != null && utilPots != null){
      utilPots=utilPots.combine(probPots);

      if (tree.getDebugFlag()){
         System.out.println("Obtenida combinacion de probs y utils......");
         utilPots.print();
         System.out.println("-------------------------------------------");
      }

      if (tree.getDebugFlag()){
         System.out.println("Tam. inicial del arbol de utilidad antes del tratamiento del arbol: "+utilPots.getSize());
      }
      
      // Postprocess utility tree
      //utilPots=postProcessUtility(utilPots);

      if (tree.getDebugFlag()){
         System.out.println("Tam. final del arbol de utilidad despues del tratamiento del arbol: "+utilPots.getSize());
      }
    }

    // Check if the probability potential can lead to a unity potential
    if (probPots != null){
      if (tree.diag.isConditionalOrMarginalPotential(node,probPots) == false){
        // This is not an unity potential and is required to remove the variable
        probPots=probPots.addVariable(node);

        // Show the potential
        if (tree.getDebugFlag()){
          System.out.println("Potencial prob. resultante de la marginalizacion: ");
          probPots.print();
          System.out.println("--------------------------------------------------------");
        }

        if (tree.getDebugFlag()){
           System.out.println("Tam. inicial del arbol de prob. antes de tratamiento del arbol: "+probPots.getSize());
        }

        // Postprocess probability tree
        //probPots=postProcessProbability(probPots);

        if (tree.getDebugFlag()){
           System.out.println("Tam. final del arbol de prob. despues del tratamiento del arbol: "+probPots.getSize());
        }
      }
      else{
        if (tree.getDebugFlag()){
           System.out.println("Al marginalizar seria un potencial unidad.......");
           probPots.print();
           System.out.println("...................................");
        }
        probPots=null;
      }
    }

    // Remove the variable from the utility potentials
    if (utilPots != null){
      utilPots=utilPots.addVariable(node);
      if (tree.getDebugFlag()){
         System.out.println("Combinacion de probs y utils marginalizada en ......"+node.getName());
         utilPots.print();
         System.out.println("-------------------------------------------");
      }

      if (tree.getDebugFlag()){
         System.out.println("Tam. inicial antes del arbol de util. del tratamiento del arbol: "+utilPots.getSize());
      }

      // Postprocessing utility tree
      //utilPots=postProcessUtility(utilPots);
      
      if (tree.getDebugFlag()){
         System.out.println("Tam. final del arbol de util. despues del tratamiento del arbol: "+utilPots.getSize());
      }
    }

    // If it is needed, make the division between utilPots and probPots
    if (probPots != null && utilPots != null){
      if (tree.getDebugFlag()){
        System.out.println("Se procede a realizar la division");
      }

      // Perform the division
      utilPots=utilPots.divide(probPots);

      if (tree.getDebugFlag()){
        System.out.println("Arbol de utilidad normalizado tras la division");
        System.out.println("-------------------------------------------");
        utilPots.print();
        System.out.println("-------------------------------------------");
      }
    }

    /**
     * Rearrange probability and utility potential, as results, to
     * minimize space memory usage
     */
    if (probPots != null){
      if (tree.getDebugFlag()){
         System.out.println("Tam. del potential de prob. antes del tratamiento del arbol: "+probPots.getSize());
      }
      
      // Postprocess probability tree
      probPots=postProcessProbability(probPots);
      
      if (tree.getDebugFlag()){
         System.out.println("Tam. del potential de prob. despues del tratamiento del arbol: "+probPots.getSize());
      }
    }
    
    if (utilPots != null){
      if (tree.getDebugFlag()){
         System.out.println("Tam. del potential de util. antes del tratamiento del arbol: "+utilPots.getSize());
      } 
      
      utilPots=postProcessUtility(utilPots);
      
      if (tree.getDebugFlag()){
         System.out.println("Tam. del potential de util. despues del tratamiento del arbol: "+utilPots.getSize());
      }
    }

    // Add the probability potential, if needed
    if (probPots != null){
       addProbabilityPotential(probPots);
    }

    // The same for the utility
    if (utilPots != null){
       addUtilityPotential(utilPots);
    }

    // May be constraints can be applied now
    applyConstraints(false);

    // Get the size of the potentials and print it
    if (tree.getDebugFlag()){
       System.out.println("TOTAL SIZE: "+tree.getSize());
    }

    if (tree.getStatisticsFlag()){
      if (lastFlag == false){
        tree.statistics.addSize(tree.getSize());
        tree.statistics.addTime(tree.crono.getTime());
      }
    }
  }

  /**
   * Public method for removing a decision variable
   * @param node to remove
   * @param lastFlag to show if the variable to be removed is the last
   * operation over the clique
   */
  public void removeDecisionVariable(Node node, boolean lastFlag){

    // Look for the potentials related to this variable. Part of them
    // will be on the same clique
    ArrayList<Potential> cliqueNodeProbPotentials=getProbabilityPotentials(node,true);
    ArrayList<Potential> infSeparatorsNodeProbPotentials=getInferiorSeparatorsProbPotentials(node,true);
    ArrayList<Potential> cliqueNodeUtilPotentials=getUtilityPotentials(node,true);
    ArrayList<Potential> infSeparatorsNodeUtilPotentials=getInferiorSeparatorsUtilPotentials(node,true);

    if (tree.getDebugFlag()){
      System.out.println("----------------- removeDecisionVariable ---------------");
      System.out.println("Clique: "+index);
      System.out.println("--------------- Nodo : "+node.getName());
      if (cliqueNodeProbPotentials != null)
         System.out.println("P. de prob. en clique: "+
                            cliqueNodeProbPotentials.size());
      if (infSeparatorsNodeProbPotentials != null)
         System.out.println("P. de prob. en sep. : "+
                            infSeparatorsNodeProbPotentials.size());
      if (cliqueNodeUtilPotentials != null)
         System.out.println("P. de util en clique: "+
                            cliqueNodeUtilPotentials.size());
      if (infSeparatorsNodeUtilPotentials != null)
         System.out.println("P. de util en sep. :"+
                            infSeparatorsNodeUtilPotentials.size());
    }

    if (tree.getStatisticsFlag()){
      tree.statistics.addOperation(node.getName()); 
    }

    // Combine the probability potentials
    Potential probPots=combineProbabilityPotentials(cliqueNodeProbPotentials,
                                                    infSeparatorsNodeProbPotentials);

    // Combine the utility potentials
    Potential utilPots=combineUtilityPotentials(cliqueNodeUtilPotentials,
                                                infSeparatorsNodeUtilPotentials);

    // Combine probability and utility potentials
    if (probPots != null && utilPots != null){
      utilPots=utilPots.combine(probPots);

      if (tree.getDebugFlag()){
         System.out.println("Tam. inicial de arbol de utilidad antes de tratarlo: "+utilPots.getSize());
      }
      
      // Reorder the tree
      //utilPots=postProcessUtility(utilPots);
      
      if (tree.getDebugFlag()){
         System.out.println("Tam. final de arbol de utilidad antes de tratarlo: "+utilPots.getSize());
      }
    }

    // Check if the probability potential can lead to a unity potential
    if (probPots != null){
      Vector vars=new Vector(probPots.getVariables());
      vars.removeElement(node);
      
      // This is not an unity potential and is required to remove the variable
      probPots=probPots.maxMarginalizePotential(vars);

      // Show the potential
      if (tree.getDebugFlag()){
          System.out.println("Potencial prob. resultante de la marginalizacion: ");
          probPots.print();
          System.out.println("--------------------------------------------------------");
      }

      if (tree.getDebugFlag()){
         System.out.println("Tam. del arbol de prob. antes de tratarlo: "+ probPots.getSize());
      }
      // Reorder the tree
      //probPots=postProcessProbability(probPots);
      
      if (tree.getDebugFlag()){
         System.out.println("Tam. del arbol de prob. despues de tratarlo: "+ probPots.getSize());
      }
    }

    // If it is needed, make the division between utilPots and probPots
    if (probPots != null && utilPots != null){
      utilPots=utilPots.divide(probPots);

      if (tree.getDebugFlag()){
         System.out.println("Arbol de utilidad tras normalizar.....");
         System.out.println("-----------------------------------------------");
         utilPots.print();
         System.out.println("-----------------------------------------------");
         System.out.println("Tam. de arbol de util. antes de tratarlo: "+utilPots.getSize());
      }
      
      // Reorder the tree
      //utilPots=postProcessUtility(utilPots);
      
      if (tree.getDebugFlag()){
         System.out.println("Tam. de arbol de util. despues de tratarlo: "+utilPots.getSize());
      }
    }

    // Remove the variable from the utility potentials
    if (utilPots != null){
      // Before removing the variable, store the decision table as result
      tree.results.put(node,utilPots.copy());
      Vector vars=new Vector(utilPots.getVariables());
      vars.removeElement(node);
      utilPots=utilPots.maxMarginalizePotential(vars);

      // Show the marginalization
      if (tree.getDebugFlag()){
        System.out.println("Potencial de util. tras marginalizar:....................");
        utilPots.print();
        System.out.println("..........................................................");
        System.out.println("Tam. del arbol de util. antes de tratarlo: "+utilPots.getSize());
      }

      // Reorder the tree
      utilPots=postProcessUtility(utilPots);
      
      if (tree.getDebugFlag()){
         System.out.println("Tam. del arbol de util. antes de tratarlo: "+utilPots.getSize());
      }
    }

    if (probPots != null){
      if (tree.getDebugFlag()){
         System.out.println("Tam. del arbol de prob. antes de tratarlo: "+probPots.getSize());
      }
      probPots=postProcessProbability(probPots);
      if (tree.getDebugFlag()){
         System.out.println("Tam. del arbol de prob. despues de tratarlo: "+probPots.getSize());
      }
    }

    // Add the probability potential, is needed
    if (probPots != null){
       addProbabilityPotential(probPots);
    }

    // The same for the utility
    if (utilPots != null){
       addUtilityPotential(utilPots);
    }

    // May be constraints can be applied now
    applyConstraints(false);

    // Get the size of the potentials and print it
    if (tree.getDebugFlag()){
      System.out.println("TOTAL SIZE: "+tree.getSize());
    }

    if (tree.getStatisticsFlag()){
      if (lastFlag == false){
        tree.statistics.addSize(tree.getSize());
        tree.statistics.addTime(tree.crono.getTime());
      }
    }
  }

  /**
   * Private method for post processing the utility potentials. For
   * PotentialTable this method does nothing, but with probability
   * potentials the variables will be sorted and a prunning will
   * be done if required
   * @param potential
   */
  protected Potential postProcessUtility(Potential potential){
    return potential;
  }

  /**
   * Private method for post processing the probability potentials. For
   * PotentialTable this method does nothing, but with probability
   * potentials the variables will be sorted and a prunning will
   * be done if required
   * @param potential
   */
  protected Potential postProcessProbability(Potential potential){
    return potential;
  }


  /**
   * Public method for setting the potentials of the clique to
   * the upper separator
   */
  public void setPotentialsToUpSeparator(){
    HashSet<Potential> probPots=new HashSet<Potential>();
    HashSet<Potential> utilPots=new HashSet<Potential>();
    ArrayList<Potential> potentials;
    ArrayList<Potential> inferiorSepProbPotentials;
    ArrayList<Potential> inferiorSepUtilPotentials;

    // Get an iterator for the probaility potentials
    Iterator<ArrayList<Potential>> iterator=probPotentials.values().iterator();

    // Consider them: every arraylist will be related to a var
    while(iterator.hasNext()){
      potentials=iterator.next();

      // Consider the potentials one by one
      for(Potential potential : potentials){
        // If it is not already in probPotentials, send it to the
        // separator
        if(probPots.add(potential)){
          if (tree.getDebugFlag()){
            System.out.println("Asignando potencial de probabilidad a separador superior de: "+index+"  separador: "+up.getIndex());
            potential.print();
            System.out.println("...................................................................");
          }
          up.setProbMessage(potential);
        }
      }
    }

    // Add prob potentials in the separators
    for(JunctionTreeSeparator infSeparator : down){
      inferiorSepProbPotentials=infSeparator.removeProbMessages();
      if (inferiorSepProbPotentials != null){
         for(Potential potential : inferiorSepProbPotentials){
            up.setProbMessage(potential);
         }
      }
    }

    // The same for utility potentials
    iterator=utilPotentials.values().iterator();

    // Consider them: every arraylist will be related to a var
    while(iterator.hasNext()){
      potentials=iterator.next();

      // Consider the potentials one by one
      for(Potential potential : potentials){
        // If it is not already in probPotentials, send it to the
        // separator
        if(utilPots.add(potential)){
          if (tree.getDebugFlag()){
            System.out.println("Asignando potencial de utilidad a separador superior de: "+index);
            potential.print();
            System.out.println("...................................................................");
          }
          up.setUtilMessage(potential);
        }
      }
    }

    // Add util potentials in the separators
    for(JunctionTreeSeparator infSeparator : down){
      inferiorSepUtilPotentials=infSeparator.removeUtilMessages();
      if (inferiorSepUtilPotentials != null){
         for(Potential potential : inferiorSepUtilPotentials){
            up.setUtilMessage(potential);
         }
      }
    }

    // The data members for probPotentials and utilPotentials are
    // cleaned
    probPotentials=new HashMap<Node,ArrayList<Potential>>();
    utilPotentials=new HashMap<Node,ArrayList<Potential>>();
  }

  /**
   * Private mthod for combining the probability potentials for a given
   * variable
   * @param cliquePots clique potentials related to node
   * @param sepPots separator potentials related to node
   * @return potential
   */
  private Potential combineProbabilityPotentials(ArrayList<Potential> cliquePots,
                                                 ArrayList<Potential> sepPots){
    Potential finalPotential=null;

    if (tree.getDebugFlag()){
      System.out.println("(Inicio)----------------- combineProbabilityPotentials ---------------");
    }
    
    // Consider the clique potentials, if it is not null
    if (cliquePots != null) {
      for(Potential pot : cliquePots){
        if (finalPotential == null){
          finalPotential=pot;

          if (tree.getDebugFlag()){
            System.out.println("Comienzo con potencial: ................");
            pot.print();
            System.out.println("----------------------------------------");
          }
        }
        else{
          // Print debug information
          if (tree.getDebugFlag()){
            System.out.println("Continuo con potencial: ................");
            pot.print();
            System.out.println("----------------------------------------");
          }

          finalPotential=finalPotential.combine(pot);

          // Print debug information
          if (tree.getDebugFlag()){
            System.out.println("Resultado parcial: ................");
            finalPotential.print();
            System.out.println("----------------------------------------");
          }
        }
      }
    }

    // Add debug information
    if (tree.getDebugFlag()){
      System.out.println();
      System.out.println("Se procede ahora a combinar con potenciales de separadores");
      System.out.println();
    }

    // The same for separator potentials
    if (sepPots != null){
      for(Potential pot : sepPots){
        if (finalPotential == null){
          finalPotential=pot;

          // Add debug information
          if (tree.getDebugFlag()){
            System.out.println("Comienzo con potencial: ................");
            pot.print();
            System.out.println("----------------------------------------");
          }
        }
        else{
          // Add debug information
          if (tree.getDebugFlag()){
            System.out.println("Continuo con potencial: ................");
            pot.print();
            System.out.println("----------------------------------------");
          }

          finalPotential=finalPotential.combine(pot);

          // Add debug information
          if (tree.getDebugFlag()){
            System.out.println("Resultado parcial: ................");
            pot.print();
            System.out.println("----------------------------------------");
          }

          // Sort and bound this potential if it is a PotentialTree
          if (tree.getDebugFlag()){
             System.out.println("Tam. inicial: "+finalPotential.getSize());
          }
        }
      }
    }

    // Show the exit from the method
    if (tree.getDebugFlag()){
        System.out.println("(Fin)----------------- combineProbabilityPotentials ---------------");
        System.out.println(" Potencial calculado...............................................");
        if (finalPotential != null)
           finalPotential.print();
        else
           System.out.println("Potencial nulo");
        System.out.println("-------------------------------------------------------------------");
        System.out.println();
    }

    // Return the combination
    return finalPotential;
  }

  /**
   * Private method for combining the utility potentials for a given
   * variable
   * @param cliquePots clique potentials related to node
   * @param sepPots separator potentials related to node
   * @return potential
   */
  private Potential combineUtilityPotentials(ArrayList<Potential> cliquePots,
                                                 ArrayList<Potential> sepPots){
    Potential finalPotential=null;

    if (tree.getDebugFlag()){
      System.out.println("(Inicio)----------------- combineUtilityPotentials ---------------");
    }

    // Consider the clique potentials, if it is not null
    if (cliquePots != null) {
      for(Potential pot : cliquePots){
        if (finalPotential == null){
          finalPotential=pot;

          if (tree.getDebugFlag()){
            System.out.println("Comienzo con potencial: ................");
            pot.print();
            System.out.println("----------------------------------------");
          }
        }
        else{
          // Print debug information
          if (tree.getDebugFlag()){
            System.out.println("Continuo con potencial: ................");
            pot.print();
            System.out.println("----------------------------------------");
          }

          finalPotential=finalPotential.addition(pot);

          // Print debug information
          if (tree.getDebugFlag()){
            System.out.println("Resultado parcial: ................");
            finalPotential.print();
            System.out.println("----------------------------------------");
          }

          // Sort and bound the potential
          if (tree.getDebugFlag()){
             System.out.println("Tam. inicial: "+finalPotential.getSize());
          }
        }
      }
    }

    // Add debug information
    if (tree.getDebugFlag()){
      System.out.println();
      System.out.println("Se procede ahora a combinar con potenciales de separadores");
      System.out.println();
    }

    // The same for separator potentials
    if (sepPots != null){
      for(Potential pot : sepPots){
        if (finalPotential == null){
          finalPotential=pot;

          // Add debug information
          if (tree.getDebugFlag()){
            System.out.println("Comienzo con potencial: ................");
            pot.print();
            System.out.println("----------------------------------------");
          }
        }
        else{
          // Add debug information
          if (tree.getDebugFlag()){
            System.out.println("Continuo con potencial: ................");
            pot.print();
            System.out.println("----------------------------------------");
          }

          finalPotential=finalPotential.addition(pot);

          // Add debug information
          if (tree.getDebugFlag()){
            System.out.println("Resultado parcial: ................");
            pot.print();
            System.out.println("----------------------------------------");
          }

          // Sort and bound the potential
          if (tree.getDebugFlag()){
             System.out.println("Tam. inicial: "+finalPotential.getSize());
          }
        }
      }
    }

    // Show the exit from the method
    if (tree.getDebugFlag()){
        System.out.println("(Fin)----------------- combineUtilityPotentials ---------------");
        System.out.println(" Potencial calculado...............................................");
        if (finalPotential != null)
          finalPotential.print();
        else
          System.out.println("Potencial nulo");
        System.out.println("-------------------------------------------------------------------");
        System.out.println();
    }

    // Return the combination
    return finalPotential;
  }

  /**
   * Private method for getting the set of prob potentials related to
   * a given variable in the inferior separators of a given clique
   * @param node to consider
   * @param liberate to show if the references to the potentials must
   *        be liberated
   * @return potentials related to this node
   *         return null if there is no potentials related to node
   */
  private ArrayList<Potential> getInferiorSeparatorsProbPotentials(Node node,boolean liberate){
    ArrayList<Potential> separatorNodePotentials=null;
    ArrayList<Potential> separatorPotentials;

    // Get the messages related to this node in the down separator
    if (down.size() != 0){
      separatorNodePotentials=new ArrayList<Potential>();

      // Loop over them to get the potentials. When the potential
      // returned is not null, store it
      for(JunctionTreeSeparator separator : down){
        separatorPotentials=separator.getProbMessages(node,liberate);
        // Add the potentials to separatorNodePotentials
        if (separatorPotentials != null){
          separatorNodePotentials.addAll(separatorPotentials);
        }
      }
    }

    // return the arraylist
    return separatorNodePotentials;
  }

  /**
   * Private method for getting the set of util potentials related to
   * a given variable in the inferior separators of a given clique
   * @param node to consider
   * @param liberate to show if the references to the potentials must
   *        be liberated
   * @return potentials related to this node
   *         return null if there is no potentials related to node
   */
  private ArrayList<Potential> getInferiorSeparatorsUtilPotentials(Node node,boolean liberate){
    ArrayList<Potential> separatorNodePotentials=null;
    ArrayList<Potential> separatorPotentials;

    // Get the messages related to this node in the down separator
    if (down.size() != 0){
      separatorNodePotentials=new ArrayList<Potential>();

      // Loop over them to get the potentials. When the potential
      // returned is not null, store it
      for(JunctionTreeSeparator separator : down){
        // Print debug information
        if (tree.getDebugFlag()){
           System.out.println("\n\n");
           System.out.println("Buscando potenciales de utilidad en separador: "+separator.getIndex());
        }

        separatorPotentials=separator.getUtilMessages(node,liberate);

        // Add the potentials to separatorNodePotentials
        if (separatorPotentials != null){
          separatorNodePotentials.addAll(separatorPotentials);
          if (tree.getDebugFlag()){
            System.out.println("Hay potenciales de utilidad: "+separatorPotentials.size());
            System.out.println("Acumulados: "+separatorNodePotentials.size());
          }
        }
      }
    }

    if (tree.getDebugFlag()){
      System.out.println("Saliendo de busqueda de potenciales de utilidad en separadores");
      System.out.println();
    }

    // return the arraylist
    return separatorNodePotentials;
  } 

  /**
   * Private method to check if a given clique is leaf node or not
   * @return boolean value
   */
  private boolean isLeafNode(){
    if (down.size() == 0)
      return true;
    else
      return false;
  }

  /**
   * Method to add a downward neighbour to this node
   * @param node junction tree node to add as neighbour
   * @param separator 
   */
  private void addDownwardNeighbour(JunctionTreeNode node, NodeList separator){
    // Create a new JunctionTreeSeparator
    JunctionTreeSeparator nodeSeparator=new JunctionTreeSeparator(this,node,separator);

    // Add the nodeSeparator to down
    down.add(nodeSeparator);

    // But now, the node passed as argument will have this separator
    // as upward neighbour
    node.setUpwardNeighbour(nodeSeparator);
  }

  /**
   * Private method to set the upward neighbour to this node
   * @param node juntion tree node to add as neighbour
   * @param separator
   */
  private void setUpwardNeighbour(JunctionTreeSeparator separator){
    // Set the nodeSeparator to up
    up=separator;
  }

  /**
   * Public method to assign a potential to the clique. This method will
   * be called on the root node
   * @param potential
   * @param kind of potential (utility, probability, constraint)
   */
  public void assignPotential(Potential potential, int kind){
    JunctionTreeSeparator separator;
    JunctionTreeNode jTreeNode;
    HashMap<Integer,Double> candidates;
    double size;

    // Two rounds are needed: the first one computes the cost of assigning
    // this potential, and the second one to make the assignment
    candidates=new HashMap<Integer,Double>();

    // Consider if it can be assigned to the root
    if (containsPotentialVars(potential)){
      size=totalSize()+potential.getSize();
      candidates.put(index,size);
    }

    // Anyway, consider the rest of cliques
    for(int i=0; i < down.size(); i++){
      separator=down.get(i);
      jTreeNode=separator.getInferiorNeighbour();

      // Test it
      jTreeNode.assignPotential(potential,candidates);
    }

    // When this is finished, loop over candidates to determine
    // the better clique for the assignment
    double min=Double.MAX_VALUE;
    double value;
    int candidateIndex=0, finalIndex=0;

    Iterator<Integer> indexes=candidates.keySet().iterator();
    while(indexes.hasNext()){
      candidateIndex=indexes.next();
      value=candidates.get(candidateIndex);
      if (value< min){
        finalIndex=candidateIndex;
        min=value;
      }
    }

    // Assign the potential to the selected clique. If it is a 
    // constraint will be assignet to each candidate clique
    if (kind != Relation.CONSTRAINT){ 
       assignPotential(potential,finalIndex,kind);
    }
    else{
      int j=1;
      //Assign the potential to every candidate
      indexes=candidates.keySet().iterator();
      while(indexes.hasNext()){
        candidateIndex=indexes.next();
        assignPotential(potential,candidateIndex,kind);
      }
    }
  }

  /**
   * Public method for getting the size of the probability trees
   * related to a clique and the inferior neighbours or if
   * @return size
   */
  public double getProbSize(){
    HashSet<Potential> probPots=new HashSet();
    ArrayList<Potential> potentials;
    JunctionTreeNode infNeighbour;
    double size=0;
    double partialSize=0;

    // Compute the size for the probability potentials related to the
    // clique
    Iterator<ArrayList<Potential>> iterator=probPotentials.values().iterator();

    // Every position will return an array list 
    while(iterator.hasNext()){
      potentials=iterator.next();
      for(Potential potential : potentials){
         if (probPots.add(potential)){
           partialSize+=potential.getSize();
         }
      }
    }

    if (tree.getDebugFlag()){
       System.out.println("Prob size ("+index+") = "+partialSize);
    }

    // Now consider the inferior separators
    for(JunctionTreeSeparator downSep : down){
      potentials=downSep.getProbMessages();
      if (potentials != null){
        // Add the potentials to probPots

        for(Potential potential : potentials){
          if (probPots.add(potential)){
             if (tree.getDebugFlag()){
                System.out.println("Prob. separador ("+downSep.getIndex()+") = "+potential.getSize());
             }
          }
        }
      }

      // Anyyway, consider the related inferior neighbour
      infNeighbour=downSep.getInferiorNeighbour();

      // Compute the size of potentials from it
      infNeighbour.getProbSize(probPots);
    }

    size=computeSizes(probPots);

    // Return size
    return size;
  }

  /**
   * Private auxiliar method for getting the size of the probability trees
   * related to a clique 
   */
  public void getProbSize(HashSet<Potential> probPots){
    ArrayList<Potential> potentials;
    JunctionTreeNode infNeighbour;
    double partialSize=0;

    // Compute the size for the probability potentials related to the
    // clique
    Iterator<ArrayList<Potential>> iterator=probPotentials.values().iterator();

    // Every position will return an array list 
    while(iterator.hasNext()){
      potentials=iterator.next();
      for(Potential potential : potentials){
         if(probPots.add(potential)){
           partialSize+=potential.getSize();
         }
      }
    }

    if (tree.getDebugFlag()){
       System.out.println("Prob size ("+index+") = "+partialSize);
    }

    // Now consider the inferior separators
    for(JunctionTreeSeparator downSep : down){
      potentials=downSep.getProbMessages();
      if (potentials!= null){
        // Add the potentials to probPots
        for(Potential potential : potentials){
          if (probPots.add(potential)){
             if (tree.getDebugFlag()){
                System.out.println("Prob. separador ("+downSep.getIndex()+") = "+potential.getSize());
             }
          }
        }
      }

      // Anyyway, consider the related inferior neighbour
      infNeighbour=downSep.getInferiorNeighbour();

      // Compute the size of potentials from it
      infNeighbour.getProbSize(probPots);
    }
  }

  /**
   * Public method for getting the size of the utility trees
   * related to a clique and the inferior neighbours
   * @return size
   */
  public double getUtilSize(){
    HashSet<Potential> utilPots=new HashSet();
    ArrayList<Potential> potentials;
    JunctionTreeNode infNeighbour;
    double size=0;
    double partialSize=0;

    // Compute the size for the probability potentials related to the
    // clique
    Iterator<ArrayList<Potential>> iterator=utilPotentials.values().iterator();

    // Every position will return an array list 
    while(iterator.hasNext()){
      potentials=iterator.next();
      for(Potential potential : potentials){
         // Add the potential
         if (utilPots.add(potential)){
            partialSize+=potential.getSize();
         }
      }
    }

    if (tree.getDebugFlag()){
       System.out.println("Util size ("+index+") = "+partialSize);
    }

    // Now consider the inferior separators
    for(JunctionTreeSeparator downSep : down){
      potentials=downSep.getUtilMessages();
      if (potentials != null){
        for(Potential potential : potentials){
          if (utilPots.add(potential)){
            if (tree.getDebugFlag()){
               System.out.println("Util. separador ("+downSep.getIndex()+") = "+potential.getSize());
            }
          }
        }
      }

      // Anyyway, consider the related inferior neighbour
      infNeighbour=downSep.getInferiorNeighbour();

      // Compute the size of potentials from it
      infNeighbour.getUtilSize(utilPots);
    }

    // When this is done, compute the sizes for the potentials in
    // utilPots
    size=computeSizes(utilPots);

    // Return size
    return size;
  }

  /**
   * Private auxiliar method for computing the sizes of the
   * utility potentials
   * @param set of utility potentials already considered
   */
  private void getUtilSize(HashSet<Potential> utilPots){
    ArrayList<Potential> potentials;
    JunctionTreeNode infNeighbour;
    double partialSize=0;

    // Get the utility potentials for the clique
    Iterator<ArrayList<Potential>> iterator=utilPotentials.values().iterator();

    // Every position will return an array list 
    while(iterator.hasNext()){
      potentials=iterator.next();
      for(Potential potential : potentials){
         // Add the potential
         if(utilPots.add(potential)){
           partialSize+=potential.getSize();
         }
      }
    }

    if (tree.getDebugFlag()){
       System.out.println("Util size ("+index+") = "+partialSize);
    }

    // Now consider the inferior separators
    for(JunctionTreeSeparator downSep : down){
      potentials=downSep.getUtilMessages();
      if (potentials != null){
        for(Potential potential : potentials){
          if (utilPots.add(potential)){
            if (tree.getDebugFlag()){
               System.out.println("Util. separador ("+downSep.getIndex()+") = "+potential.getSize());
            }
          }
        }
      }

      // Anyyway, consider the related inferior neighbour
      infNeighbour=downSep.getInferiorNeighbour();

      // Compute the size of potentials from it
      infNeighbour.getUtilSize(utilPots);
    }
  }

  /**
   * Public method for getting the size of the constraint trees
   * related to a clique and the inferior neighbours
   * @return size
   */
  public double getConstraintSize(){
    HashSet<Potential> constraintPots=new HashSet();
    ArrayList<Potential> potentials;
    JunctionTreeNode infNeighbour;
    double size=0;
    double partialSize=0;

    // If there are not constraints, return 0
    if (constraintPotentials == null)
       return 0;

    // Compute the size for the constraint potentials related to the
    // clique
    Iterator<ArrayList<Potential>> iterator=constraintPotentials.values().iterator();

    // Every position will return an array list 
    while(iterator.hasNext()){
      potentials=iterator.next();
      for(Potential potential : potentials){
         // Add the potential
         if (constraintPots.add(potential)){
            partialSize+=potential.getSize();
         }
      }
    }

    if (tree.getDebugFlag()){
       System.out.println("Constraints size ("+index+") = "+partialSize);
    }

    for(JunctionTreeSeparator downSep : down){
      // Consider the related inferior neighbour
      infNeighbour=downSep.getInferiorNeighbour();

      // Compute the size of potentials from it
      infNeighbour.getConstraintSize(constraintPots);
    }

    // When this is done, compute the sizes for the potentials in
    // constraintPots
    size=computeSizes(constraintPots);

    // Return size
    return size;
  }

  /**
   * Private auxiliar method for computing the sizes of the
   * constraint potentials
   * @param set of constraint potentials already considered
   */
  private void getConstraintSize(HashSet<Potential> constraintPots){
    ArrayList<Potential> potentials;
    JunctionTreeNode infNeighbour;
    double partialSize=0;

    // Get the constraint potentials for the clique
    Iterator<ArrayList<Potential>> iterator=constraintPotentials.values().iterator();

    // Every position will return an array list 
    while(iterator.hasNext()){
      potentials=iterator.next();
      for(Potential potential : potentials){
         // Add the potential
         if(constraintPots.add(potential)){
           partialSize+=potential.getSize();
         }
      }
    }

    if (tree.getDebugFlag()){
       System.out.println("Constraints size ("+index+") = "+partialSize);
    }

    // Now consider the inferior separators
    for(JunctionTreeSeparator downSep : down){
      // Anyyway, consider the related inferior neighbour
      infNeighbour=downSep.getInferiorNeighbour();

      // Compute the size of potentials from it
      infNeighbour.getConstraintSize(constraintPots);
    }
  }

  /**
   * Method for computing the sizes of a set of potentials
   * @param set of potentials
   */
  private double computeSizes(HashSet<Potential> pots){
    Potential potential;
    double size=0;
    
    Iterator<Potential> iterator=pots.iterator();
    while(iterator.hasNext()){
      potential=iterator.next();
      if (potential.getClassName().equals("PotentialTree")){
        PotentialTree pot=(PotentialTree)potential;
        if (pot.checkSize() == false)
          pot.updateSize();
        size+=pot.getSize();
      }
      else{
        size+=potential.getSize();
      }
    }

    // return size
    return size;
  }

  /**
   * Method for looking for a node with a given index
   * @param index to search for
   * @return node with this index
   */
  public JunctionTreeNode lookForNode(int cliqueIndex){
    JunctionTreeNode treeNode;
    JunctionTreeNode found;
    
    if (this.index == cliqueIndex)
      return this;

    // En caso contrario, llama al metodo de busqueda sobre los
    // vecinos inferiores
    for(JunctionTreeSeparator separator : down){
       treeNode=separator.getInferiorNeighbour();
       found=treeNode.lookForNode(cliqueIndex);

       // Look if it is found
       if (found != null){
         return found;
       }
    }

    // If this point is reached, return null
    return null;
  }

  /**
   * Public method to assign a potential to a clique. This is an auxiliar
   * method that will never be used directly with the root clique
   * @param potential
   * @param candidates hashmap with the costs of assignments
   */
  private void assignPotential(Potential potential,HashMap candidates){
    JunctionTreeSeparator separator;
    JunctionTreeNode jTreeNode;
    double size;

    // Consider if it can be assigned to this clique
    if (containsPotentialVars(potential)){
      size=totalSize()+potential.getSize();
      candidates.put(index,size);
    }

    // Anyway, consider the rest of the cliques
    for(int i=0; i < down.size(); i++){
      separator=down.get(i);
      jTreeNode=separator.getInferiorNeighbour();

      // Assign it
      jTreeNode.assignPotential(potential,candidates);
    }
  }

  /**
   * Private method to assign the potential to a node with a given
   * index
   * @param potential to assign
   * @param index of the junction tree node
   * @param kind of potential
   */
  private void assignPotential(Potential potential, int index, int kind){
    // First at all, treverse the tree until reaching the junction
    // tree node with the given index
    JunctionTreeNode jTreeNode=null;
    JunctionTreeSeparator separator;

    if (tree.getDebugFlag()){
      System.out.println("------------------- assignPotential ------------------");
    }
    
    if (this.index == index){
      addPotential(potential,kind);
      if (tree.getDebugFlag()){
         System.out.println("Indice: "+index);
         System.out.println("...................................................");
      }
    }
    else{
      // Consider the rest of nodes
      for(int i=0; i < down.size(); i++){
        separator=down.get(i);

        // Consider the juntion tree node
        jTreeNode=separator.getInferiorNeighbour();
        if (jTreeNode.index == index){
           if (tree.getDebugFlag()){
              System.out.println("Indice: "+index);
              System.out.println("...................................................");
           }
           jTreeNode.addPotential(potential,kind);
        }
        else{
          // Look from this clique down
          jTreeNode.assignPotential(potential,index,kind);
        }
      }
    }
  }

  /**
   * Method for adding a potential to a junction tree node
   * @param potential to include
   * @param kind of potential to assign
   */
  private void addPotential(Potential potential, int kind){
    switch(kind){
      case Relation.UTILITY: addUtilityPotential(potential);
                             break;
      case Relation.CONSTRAINT: addConstraintPotential(potential);
                             break;
      default: addProbabilityPotential(potential);
                             break;
    }
  }

  /**
   * Method to add a probability potential to this junction tree node
   * @param potential
   */
  protected void addProbabilityPotential(Potential potential){
    Vector variables=potential.getVariables();
    Node node;

    // Show debug information
    if (tree.getDebugFlag()){
      System.out.println("Agregar potencial de probabilidad a nodo: "+index);
      potential.print();
      System.out.println("-------------------------------------------------");
    }

    // To get the variables related to the potentials where they
    // appear, consider the variables one by one
    for(int i=0; i < variables.size(); i++){
      node=(Node)variables.elementAt(i);

      // See if it is included in probPotentials
      if (probPotentials.get(node) == null){
        // Create a new ArrayList to include the potential
        ArrayList<Potential> potentials=new ArrayList<Potential>();
        potentials.add(potential);

        // Include the pair into probPotentials
        probPotentials.put(node,potentials);
      }
      else{
        // Get the array list
        ArrayList<Potential> potentials=probPotentials.get(node);

        // Include the potential
        potentials.add(potential);
      }
    }
  }

  /**
   * Method to add an utility potential to this junction tree node
   * @param potential
   */
  protected void addUtilityPotential(Potential potential){
    Vector variables=potential.getVariables();
    Node node;

    // Show debug information
    if (tree.getDebugFlag()){
      System.out.println("Agregar potencial de utilidad a nodo: "+index);
      potential.print();
      System.out.println("-------------------------------------------------");
    }

    // To get the variables related to the potentials where they
    // appear, consider the variables one by one
    for(int i=0; i < variables.size(); i++){
      node=(Node)variables.elementAt(i);

      // See if it is included in utilPotentials
      if (utilPotentials.get(node) == null){
        // Create a nuew ArrayList to include the potential
        ArrayList<Potential> potentials=new ArrayList<Potential>();
        potentials.add(potential);

        // Include the pair into probPotentials
        utilPotentials.put(node,potentials);
      }
      else{
        // Get the array list
        ArrayList<Potential> potentials=utilPotentials.get(node);

        // Include the potential
        potentials.add(potential);
      }
    }
  }

  /**
   * Method to add a constraint potential to this junction tree node
   * @param potential
   */
  protected void addConstraintPotential(Potential potential){
  }

  /**
   * Private method to determine the total size of the potentials
   * assigned to the clique
   * @return size
   */
  private double totalSize(){
    ArrayList potentials;
    Potential potential;
    double size=0;

    // Begin with prob potentials
    Iterator<ArrayList<Potential>> iterator=probPotentials.values().iterator();

    // Every position will return an array list 
    while(iterator.hasNext()){
      potentials=iterator.next();
      for(int i=0; i < potentials.size(); i++){
         potential=(Potential)potentials.get(i);
         size+=potential.getSize();
      }
    }

    // The same for util potentials
    iterator=utilPotentials.values().iterator();
    while(iterator.hasNext()){
      potentials=iterator.next();
      for(int i=0; i < potentials.size(); i++){
        potential=(Potential)potentials.get(i);
        size+=potential.getSize();
      }
    }

    // The same for constraint potentials
    if (constraintPotentials != null){
      iterator=constraintPotentials.values().iterator();
      while(iterator.hasNext()){
        potentials=iterator.next();
        for(int i=0; i < potentials.size(); i++){
          potential=(Potential)potentials.get(i);
          size+=potential.getSize();
        }
      }
    }

    // Return the size
    return size;
  }

  /**
   * Private method to check if the variables of a potential are
   * included in the list of variables of the junction tree node
   * @param potential
   * @return boolean value
   */
  private boolean containsPotentialVars(Potential potential){
    // Get the variables of the potential
    Vector varsPotential=potential.getVariables();
    Node node;
    
    // Consider them one by one
    for(int i=0; i < varsPotential.size(); i++){
      node=(Node)varsPotential.elementAt(i);

      // Look if it is included into variables (the variables of
      // this juntion tree node)
      if (variables.getId(node.getName()) == -1){
        // There is one variable not include in the list of variables
        // of the junction tree node, and return false
        return false;
      }
    }

    // If this point is reached, return true
    return true;
  }

  /**
   * Method to get and optionally liberate the references for the potentials 
   * related to a node
   * @param node
   * @param liberate 
   * @return list of probability potentials related to node
   */
  private ArrayList<Potential> getProbabilityPotentials(Node node, boolean liberate){
    ArrayList<Potential> cliqueNodePotentials;
    ArrayList<Potential> varPotentials;
    Node nodeInClique;

    // Get the potentials related with a node. If liberate is true, remove the
    // references to this potential
    if (liberate){
      cliqueNodePotentials=probPotentials.remove(node);
    }
    else{
      cliqueNodePotentials=probPotentials.get(node);
    }

    // If there are not potentials related to node, return null
    if (cliqueNodePotentials != null){
      // Consider the rest of variables of the clique and liberate the references
      // to the potentials contained in cliqueNodePotentials
      for(int i=0; i < variables.size(); i++){
        nodeInClique=variables.elementAt(i);

        // Get the potentials for this variable 
        varPotentials=probPotentials.get(nodeInClique);

        // Consider the potentials for this node, and remove the ones contained
        // in cliqueNodeProbPotentials
        if (liberate && varPotentials != null)
           removeRepetitions(varPotentials,cliqueNodePotentials);
      }
    }

    // return cliquenodePotentials
    return cliqueNodePotentials;
  }

  /**
   * Method to get and optionally liberate the references for the potentials 
   * related to a node
   * @param node
   * @param liberate
   * @return list of utility potentials related to node
   */
  private ArrayList<Potential> getUtilityPotentials(Node node, boolean liberate){
    ArrayList<Potential> cliqueNodePotentials;
    ArrayList<Potential> varPotentials;
    Node nodeInClique;

    // Get the potentials related with a node. If liberate is true, remove the
    // references to this potential
    if (liberate){
      cliqueNodePotentials=utilPotentials.remove(node);
    }
    else{
      cliqueNodePotentials=utilPotentials.get(node);
    }

    // If there are no cliques related to node, return null
    if (cliqueNodePotentials != null){
      // Consider the rest of variables of the clique and liberate the references
      // to the potentials contained in cliqueNodePotentials
      for(int i=0; i < variables.size(); i++){
        nodeInClique=variables.elementAt(i);

        // Get the potentials for this variable 
        varPotentials=utilPotentials.get(nodeInClique);

        // Consider the potentials for this node, and remove the ones contained
        // in cliqueNodePotentials
        if (liberate && varPotentials != null)
           removeRepetitions(varPotentials,cliqueNodePotentials);
      }
    }

    // return cliquenodePotentials
    return cliqueNodePotentials;
  }

  /**
   * Private method for removing the references to potentials already used
   * @param first list where the references must be cleaned
   * @param second list containing the references to remove
   */
  protected void removeRepetitions(ArrayList<Potential> first, ArrayList<Potential> second){
    // Consider the potentials in second
    for(Potential potential : second){

      // Look if it is referenced in first
      if (first.contains(potential)){
        first.remove(potential);
      }
    }
  }

  /**
   * Method for removing a  potential from the list of 
   * constraint potentials
   * @param potential
   * @param list of potentials to consider
   */
  protected void removeConstraintPotential(Potential potential){
    ArrayList<Potential> varPotentials;
    Node nodeInClique;

    // Consider the variables of the clique one by one
    for(int i=0; i < variables.size(); i++){
       nodeInClique=variables.elementAt(i);

       // Get the potentials for this variable 
       varPotentials=constraintPotentials.get(nodeInClique);

       // Consider the potentials for this node, and remove the ones contained
       // in cliqueNodePotentials
       if (varPotentials != null && varPotentials.contains(potential)){
          varPotentials.remove(potential);
       }
    }
  }
}
