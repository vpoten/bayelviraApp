
package elvira.inference.clustering.lazyid;

import elvira.Node;
import elvira.NodeList;
import elvira.potential.Potential;
import elvira.potential.PotentialTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

public class JunctionTreeNodeWithPTAC extends JunctionTreeNodeWithPT{

  /**
   * Constructor
   * @param tree 
   * @param variables
   * @param index
   */
  public JunctionTreeNodeWithPTAC(StrongJunctionTree tree,
                                NodeList variables,int index){
    super(tree,variables,index);
    constraintPotentials=new HashMap<Node,ArrayList<Potential>>();
  }

  /**
   * Method for building a new tree node
   * @param tree
   * @param clique nodelist to include in the node
   * @param index of the node
   */
  protected JunctionTreeNode buildTreeNode(StrongJunctionTree tree, 
                                         NodeList clique, int index){
    JunctionTreeNodeWithPTAC node=new JunctionTreeNodeWithPTAC(tree,clique,index);
    return node;
  }

  /**
   * Private method for post processing the utility potentials. For
   * PotentialTable this method does nothing, but with probability
   * potentials the variables will be sorted and a prunning will
   * be done if required
   * @param potential
   */
  protected Potential postProcessUtility(Potential potential){
    PotentialTree finalPot=(PotentialTree)super.postProcessUtility(potential);
    // Tratamiento de las restricciones
    return finalPot;
  }

  /**
   * Private method for post processing the probability potentials. For
   * PotentialTable this method does nothing, but with probability
   * potentials the variables will be sorted and a prunning will
   * be done if required
   * @param potential
   */
  protected Potential postProcessProbability(Potential potential){
    PotentialTree finalPot=(PotentialTree)super.postProcessProbability(potential);
    return finalPot;
  }

  /**
   * Method to add a constraint potential to this junction tree node
   * @param potential
   */
  public void addConstraintPotential(Potential potential){
    Vector variables=potential.getVariables();
    Node node;

    // Show debug information
    if (tree.getDebugFlag()){
      System.out.println("Agregar potencial de restriccion a nodo: "+index);
      potential.print();
      System.out.println("-------------------------------------------------");
    }

    // To get the variables related to the potentials where they
    // appear, consider the variables one by one
    for(int i=0; i < variables.size(); i++){
      node=(Node)variables.elementAt(i);

      // See if it is included in constraintPotentials
      if (constraintPotentials.get(node) == null){
        // Create a new ArrayList to include the potential
        ArrayList<Potential> potentials=new ArrayList<Potential>();
        potentials.add(potential);

        // Include the pair into probPotentials
        constraintPotentials.put(node,potentials);
      }
      else{
        // Get the array list
        ArrayList<Potential> potentials=constraintPotentials.get(node);

        // Include the potential
        potentials.add(potential);
      }
    }
  }

  /**
   * Method for combining constraints
   * @param boolean flag: propagate on neighbourhs
   */
  public void applyConstraints(boolean goDown){
    HashSet<Potential> constraints=new HashSet<Potential>();
    ArrayList<Potential> probConstrained;
    ArrayList<Potential> utilConstrained;
    Iterator<ArrayList<Potential>> iteratorConsPotentials=constraintPotentials.values().iterator();
    ArrayList<Potential> potentials;
    Potential potential,constraint;
    boolean applied;

    // Get constraint potentials
    while(iteratorConsPotentials.hasNext()){
      potentials=iteratorConsPotentials.next();
      for(int i=0; i < potentials.size();i++){
         // As constraints is a set, there will ne no repetitions
         constraints.add(potentials.get(i));
      }
    }
  
    // Consider every constraint
    Iterator<Potential> iteratorConstraints=constraints.iterator();
    while(iteratorConstraints.hasNext()){
       constraint=iteratorConstraints.next(); 
       applied=false;
       // Get the probability potentials constrained by this constraint
       probConstrained=getProbabilityPotentialsConstrained(constraint.getVariables());

       // Combine the potentials
       if (probConstrained != null && probConstrained.size() != 0){
          combineWithProbabilityPotentials(constraint,probConstrained);
          applied=true;
       }

       // Get the utility potentials constrained by this constraint
       utilConstrained=getUtilityPotentialsConstrained(constraint.getVariables());

       // Combine the potentials
       if (utilConstrained != null && utilConstrained.size() != 0){
         combineWithUtilityPotentials(constraint,utilConstrained);
          applied=true;
       }

       // APPLIED CONSTRAINTS SHOULD BE REMOVED: are already USED
       if (applied == true){
         removeConstraintPotential(constraint);
       }
    }

    // Make the same with the inferior neigbourghs
    if (goDown){
      JunctionTreeNode inferiorNeighbour;
      for(int i=0; i < down.size(); i++){
        inferiorNeighbour=down.get(i).getInferiorNeighbour();
        inferiorNeighbour.applyConstraints(goDown);
      }
    }
  }

  /**
   * Method for getting the probability potentials with the variables
   * contained in the first argument. The potentials returned are removed
   * from the list of probability potentials
   * @return ArrayList<Potential>
   */
  private ArrayList<Potential> getProbabilityPotentialsConstrained(Vector varsInConst){
    ArrayList<Potential> constrained=new ArrayList<Potential>();
    Iterator<ArrayList<Potential>> iterator=probPotentials.values().iterator();
    ArrayList<Potential> potentials=null;
    NodeList listVarsInConst=new NodeList(varsInConst);
    NodeList listVarsInPotential;
    Node varInClique;
    Potential potential;

    // Consider the potentials one by one
    while(iterator.hasNext()){
      potentials=iterator.next();
      for(int i=0; i < potentials.size();i++){
        potential=potentials.get(i);
        listVarsInPotential=new NodeList(potential.getVariables());

        // listVarsInPotential should be included in listVarsInConf
        if (listVarsInConst.isIncluded(listVarsInPotential)){
           if (!constrained.contains(potential)){
              // Include the potential in constrained
              constrained.add(potential);
           }
        }
      }
    }

    // Now the potentials must be removed from the lists of potentials
    // for every variable
    if (constrained.size() != 0){
       // Consider the variables of the clique one by one
       for(int i=0; i < variables.size(); i++){
          varInClique=variables.elementAt(i);

          // Get the potentials for this variable
          potentials=probPotentials.get(varInClique);

          // Remove them
          if(potentials != null){
            removeRepetitions(potentials,constrained);
          }
       }
    }

    // Return constrained
    return constrained;
  }

  /**
   * Method for getting the utility potentials with the variables
   * contained in the first argument. The potentials returned are removed
   * from the list of utility potentials
   * @return ArrayList<Potential>
   */
  private ArrayList<Potential> getUtilityPotentialsConstrained(Vector varsInConst){
    ArrayList<Potential> constrained=new ArrayList<Potential>();
    Iterator<ArrayList<Potential>> iterator=utilPotentials.values().iterator();
    ArrayList<Potential> potentials=null;
    NodeList listVarsInConst=new NodeList(varsInConst);
    NodeList listVarsInPotential;
    Node varInClique;
    Potential potential;

    // Consider the potentials one by one
    while(iterator.hasNext()){
      potentials=iterator.next();
      for(int i=0; i < potentials.size();i++){
        potential=potentials.get(i);
        listVarsInPotential=new NodeList(potential.getVariables());

        // listVarsInPotential should be included in listVarsInConf
        if (listVarsInConst.isIncluded(listVarsInPotential)){
           if (!constrained.contains(potential)){
              // Include the potential in constrained
              constrained.add(potential);
           }
        }
      }
    }

    // Now the potentials must be removed from the lists of potentials
    // for every variable
    if (constrained.size() != 0){
       // Consider the variables of the clique one by one
       for(int i=0; i < variables.size(); i++){
          varInClique=variables.elementAt(i);

          // Get the potentials for this variable
          potentials=utilPotentials.get(varInClique);

          // Remove them
          if(potentials != null){
            removeRepetitions(potentials,constrained);
          }
       }
    }

    // Return constrained
    return constrained;
  }

 /**
   * Private mthod for combining the probability potentials for a given
   * variable
   * @param constraint potential with the constraint
   * @param probPots probability potentials constrained by the constraint
   */
  private void combineWithProbabilityPotentials(Potential constraint,
                                                 ArrayList<Potential> probPots){
    Potential pot=null;
    if (tree.getDebugFlag()){
      System.out.println("(Inicio)----------------- combineWithProbabilityPotentials ---------------");
    }

    // Consider the probability potentials, if it is not null
    if (probPots != null) {
      for(int i=0; i < probPots.size(); i++){
        pot=probPots.get(i);

        // Combine
        pot=pot.combine(constraint);

        // Reorder the potential
        pot=postProcessProbability(pot);

        // Add the new potential to the set of probability potentials
        addProbabilityPotential(pot);
      }
    }
    
  }

 /**
   * Private mthod for combining the utility potentials for a given
   * variable
   * @param constraint potential with the constraint
   * @param probPots utility potentials constrained by the constraint
   */
  private void combineWithUtilityPotentials(Potential constraint,
                                                 ArrayList<Potential> utilPots){
    Potential pot=null;

    if (tree.getDebugFlag()){
      System.out.println("(Inicio)----------------- combineWithUtilityPotentials ---------------");
    }
   
    // Consider the utility potentials, if it is not null
    if (utilPots != null) {
      for(int i=0; i < utilPots.size(); i++){
        pot=utilPots.get(i);

        // Combine
        pot=pot.combine(constraint);

        // Reorder the potential
        pot=postProcessUtility(pot);
      }
    }
    
    // Add the new potential to the set of utility potentials
    if (pot != null)
      addUtilityPotential(pot);
  }

}
