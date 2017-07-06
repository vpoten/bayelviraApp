/*
 * PTreeMinMaxCredalSet.java
 *
 * Created on 27 de octubre de 2004, 13:27
 * Author: mgomez@decsai.ugr.es
 */

package elvira.potential;

import elvira.FiniteStates;
import elvira.Node;
import elvira.Configuration;
import java.util.*;


/**
 * This class represents a Credal Set using a ProbabilityTree
 * @author  Manuel Gómez (mgomez@decsai.ugr.es)
 */
public class PTreeMinMaxCredalSet extends elvira.potential.PotentialMTree implements CredalSet {
  
  /**
   * Constructor of the class. It will receive a PTreeCredal (a PotentialTree) as an
   * argument and creates a new object
   * @param pTreeCredalSet 
   */
   public PTreeMinMaxCredalSet(PotentialTree pTreeCredalSet) {
     super(pTreeCredalSet);
   }
   
  /**
   * Constructor receiving a PotentialTable as argument
   * @param pot to convert into a PTreeMinMaxCredalSet
   */
   public PTreeMinMaxCredalSet(PotentialTable pot) {
     super(pot);
   }

  /**
   * Constructor that creates a new PTreeMinMaxCredalSet with an empty
   * tree
   * @param vars Vector of variables
   */
   public PTreeMinMaxCredalSet(Vector vars){
     variables=(Vector)vars.clone();
     values=new MultipleTree();
     size=0;
     isExact=true;
   }
   

  /**
   * Combination method. This method is overriden to get PTreeMinMaxCredalSet
   * as a result of this operation
   * @param p a Potential
   * @returns a new PTreeMinMaxCredalSet consisting of the combination
   *          of pMTree and this PTreeMinMaxCredalSet 
   */
   public Potential combine(Potential pMTree) {
     Vector v, v1, v2;
     FiniteStates aux;
     int i, nv;
     PTreeMinMaxCredalSet pot,p;
     double x;
     MultipleTree tree, tree1, tree2;

     // Get a reference to the object pased as argument, but as a
     // PTreeMinMaxCredalSet
     p = (PTreeMinMaxCredalSet)pMTree;

     v1 = variables; // Variables of this potential.
     v2 = p.variables; // Variables of the argument.
     v = new Vector(); // Variables of the new potential.

     // Insert in v the variables comming from this potential
     for (i=0 ; i<v1.size() ; i++) {
       aux=(FiniteStates)v1.elementAt(i);
       v.addElement(aux);
     }

     // Insert in v the variables comming from the argument (only
     // if are not included yet)
     for (i=0 ; i<v2.size() ; i++) {
       aux=(FiniteStates)v2.elementAt(i);
       if (aux.indexOf(v1)==-1)
         v.addElement(aux);
     }


     // Make the new PTreeMinMaxCredalSet
     pot=new PTreeMinMaxCredalSet(v);

     // Get the trees with the values both for this potential and
     // for the argument
     tree1 = getTree(); // Tree of this potential.
     tree2 = p.getTree(); // Tree of the argument.

     // Combine both trees
     tree = MultipleTree.combine(tree1,tree2); // The new tree.

     // Set this new tree as the tree for the new PTreeMinMaxCredalSet
     pot.setTree(tree);

     // Return the new PTreeMinMaxCredalSet
     return pot;
   }

   /**
    * Combine method to used when the argument is an PotentialMTree
    * @param pTreeMinMax potential to combine with this potential
    * @return result of the operation
    */
   public PTreeMinMaxCredalSet combine(PTreeMinMaxCredalSet p) {
     return (PTreeMinMaxCredalSet)combine((Potential)p);
   }

   /**
    * Removes a list of variables by adding over all their states.
    * @param vars vector of FiniteStates.
    * @return A new PTreeMinMaxCredalSet with the result of the operation.
    */
   public PotentialMTree addVariable(Vector vars) {
     Vector aux;
     FiniteStates var1, var2;
     int i, j;
     boolean found;
     PTreeMinMaxCredalSet pot;
     MultipleTree tree;

     // Make a new vector with the variables to keep, checking
     // the variables in the potential and the list of variables
     // to remove
     aux=new Vector(); // New list of variables.

     for (i=0 ; i<variables.size() ; i++) {
       // Consider one by one the variables in the potential
       var1 = (FiniteStates)variables.elementAt(i);
       found = false;

       // Look if this variable is present in the list of variables
       // to remove
       for (j=0 ; j<vars.size() ; j++) {
         var2 = (FiniteStates)vars.elementAt(j);
         if (var1==var2) {
	          found = true;
	          break;
         }
       }
    
       // If the variable was not found in the list of variables to
       // remove, it must be in the final potential
       if (!found)
         aux.addElement(var1);
       }

       // Create a new PTreeMinMaxCredalSet
       pot = new PTreeMinMaxCredalSet(aux);

       // Get the values of this potential 
       tree = values;

       // Removes the variables in vars one by one
       for (i=0 ; i<vars.size() ; i++) {
         var1 = (FiniteStates)vars.elementAt(i);
         tree = tree.multiAddVariable(var1);
       }

       // Set the tree to the potential
       pot.setTree(tree);
  
       // Return the final potential
       return pot;
   }

   /**
    * Removes the argument variable summing over all its values.
    * @param var a Node variable.
    * @return a new PTreeMinMaxCredalSet with the result of the deletion.
    */
   public Potential addVariable(Node var) {
     Vector v;
     PTreeMinMaxCredalSet pot;
     
     v = new Vector();
     v.addElement(var);
     pot = (PTreeMinMaxCredalSet)addVariable(v);
  
     return pot;
   }

 /**
  * Restricts the potential to a given configuration.
  * @param conf restricting configuration.
  * @return Returns a new PotentialMTree where variables
  * in conf have been instantiated to their values in conf.
  */

  public Potential restrictVariable(Configuration conf) {
    Vector aux;
    FiniteStates temp;
    PTreeMinMaxCredalSet pot;
    MultipleTree tree;
    int i, p, s, v;

    s = variables.size();
    aux = new Vector(s); // New list of variables.
    tree = getTree(); // tree will be the new tree

    for (i=0 ; i<s ; i++) {
      temp = (FiniteStates)variables.elementAt(i);
      p = conf.indexOf(temp);

      if (p==-1) // If it is not in conf, add to the new list.
        aux.addElement(temp);
      else {     // Otherwise, restrict the tree to it.
        v = conf.getValue(p);
        tree = tree.restrict(temp,v);
      }
    }

    pot = new PTreeMinMaxCredalSet(aux);
    pot.setTree(tree);

    return pot;
  }

  
 /**
  * Method to normalize the potential. The method will use max and min
  * values to get intervals for the interest variables
  * @return a PotentialIntervalTable with the results of the normalization
  */
  public PotentialInterval normalizeWithMinMax() {
    // Make a configuration for the transparent variables
    Vector transparentVars=getListTransparents();
    Configuration transparents=new Configuration(transparentVars);
    
    // Make a configuration for the rest of variables (non transparent)
    Vector nonTransparentVars=getListNonTransparents();
    Configuration nonTransparents=new Configuration(nonTransparentVars);
    
    // The final result of the method will be a PotentialIntervalTable with
    // the intervals derived from the actual PTreeMinMaxCredalSet
    PotentialIntervalTable finalResults=new PotentialIntervalTable(nonTransparentVars);
    finalResults.setMinValue(1);
    finalResults.setMaxValue(0);
    
    // Consider the whole set of values for the transparent variables. For
    // every configuration for the transparent variables will be created
    // a PotentialConvexSet to contain the extreme points derived from the
    // intervals in the PTreeMinMaxCredalSet
    for(long i=0; i < FiniteStates.getSize(transparentVars); i++) {

      // Make a PotentialConvexSet for the nonTransparent variables and
      // the number of states equals to the number of possible values
      // for all of them multiplied by two (every configuration will be
      // used to generate two extreme points)
      double numStates=FiniteStates.getSize(nonTransparentVars)*2;
      
      // For this configuration for the set of transparent variables in the
      // PTreeMinMaxCredalSet, get the values for the interest variables.
      // For that purpose, restrict the PTreeMinMaxCredalSet to the value
      // for the transparent variables in this configuration
      MultipleTree restricted=this.values.restrict(transparents);
      
      // On this restricted MultipleTree, consider one by one the values
      // for the variables in the tree. This means to go on the configuration
      // for the non-transparent variables. For every configuration it must
      // be used another loop for the configuration itself, just because
      // the extreme points are formed with min and max values for the 
      // whole set of values for a given configuration for the transparent
      // variables. 
            
      // This index will be used to count the number of extreme points
      // generated
      int index=0;
      
      // Consider the values for the configuration formed with the non transparent
      // variables
      for(long j=0; j < FiniteStates.getSize(nonTransparentVars); j++) {
        PotentialConvexSet pConvexSet=new PotentialConvexSet(nonTransparentVars,(int)numStates);
        // Make a configuration for the variables in pConvexSet
        Configuration confForPConvexSet=new Configuration(pConvexSet.getVariables());
        FiniteStates transInPConvexSet=(FiniteStates)pConvexSet.getListTransparents().elementAt(0);
        
        // For this configuration we have to generate two extreme points:
        // the first using the min for the given configuration and max
        // for the rest; and a second with the max for the given configuration
        // and the min for the rest
        // Get the values for this configuration (only min and max) 
        double min=restricted.getMin(nonTransparents);
        double max=restricted.getMax(nonTransparents);
        
        // These values are related to the values index and index+1 for
        // the transparent variable in the potential convex set
        confForPConvexSet.resetConfiguration(nonTransparents);
        confForPConvexSet.putValue(transInPConvexSet.getName(),index);
        pConvexSet.setValue(confForPConvexSet,min);
        confForPConvexSet.putValue(transInPConvexSet.getName(),index+1);
        pConvexSet.setValue(confForPConvexSet,max);
        
        // We use an additional aux Configuration over the non transparent
        // variables
        Configuration auxConf=new Configuration(nonTransparentVars);
        
        // Get the max and min values for the rest of values for the
        // configuration of non transparent variables
        for(long k=0; k < FiniteStates.getSize(nonTransparentVars); k++) {
          // Consider the rest of values for the configuration
          if (auxConf.equals(nonTransparents) == false) {
            // Get the max and the min
            double minRest=restricted.getMin(auxConf);
            double maxRest=restricted.getMax(auxConf);
            // The min and max values will be assigned to the configurations
            // related to index and index+1 values for the transparent variable
            // in the convext set
            confForPConvexSet.resetConfiguration(auxConf);
            confForPConvexSet.putValue(transInPConvexSet.getName(),index);
            pConvexSet.setValue(confForPConvexSet,maxRest);
            confForPConvexSet.putValue(transInPConvexSet.getName(),index+1);
            pConvexSet.setValue(confForPConvexSet,minRest);            
          }
          // Jump to the next
          auxConf.nextConfiguration();
        }
        
        // Consider the next value for the configuration without transparents
        nonTransparents.nextConfiguration();

        // Here we have to normalize the PotentialConvexSet
        pConvexSet.normalize();
       
        // And now, the values in the PotentialConvexSet must be stored into
        // the PotentialIntervalTable
        finalResults.actualizeValues(pConvexSet);
        
        // Add one to index
        index++;
      }
      
      // Go to the next configuration
      transparents.nextConfiguration();
    }
       
    // Get sure the intervals obtained are coherent
    double minValues[]=finalResults.getArrayCopyMinValues();
    double maxValues[]=finalResults.getArrayCopyMaxValues();
    finalResults.avoidsSecureLoss(minValues,maxValues);
    finalResults.setMaxValues(maxValues);
    finalResults.setMinValues(minValues);
    
    // return the final result
    return finalResults;
  }

 /**
  * Get the list of transparent variables included in one of
  * the nodes of its associated probability tree.
  * @return a Vector with the list of transparent variables included in one
  * of the nodes of the associated probability tree
  */
  public Vector getListTransparents(){
    Vector transVars=values.getListTransparents();
    return transVars;
  }
}
