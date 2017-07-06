/* ListPotential.java */

package elvira.potential;

import java.util.Date;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.io.*;

import elvira.*;
import elvira.tools.FactorisationTools;
/**
 * Implementation of class ListPotential. An object of this class
 * represents a potential specified by a list of other potentials.
 * It is NOT a list of potentials, but a potential itself, with
 * its own operations.
 *
 * @author Andres Cano (acu@decsai.ugr.es)
 * @author Serafin Moral (smc@decsai.ugr.es)
 * @author Irene Martinez (irene@ual.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 * @since 5/12/2003
 */

public class ListPotential extends Potential {

/**
 * Contains a list of potentials.
 */
  
private Vector list;

/**
 * Indicates whether the potential is exact or approximate.
 */

private boolean isExact;

//private static final Object null=new Object();


public static final int FIRST_TWO=0;
public static final int MIN_SIZE_VARS=1;
public static final int MIN_SIZE_VARS_AND_PROD=2;
public static final int MIN_SIZE_PROD=3;
public static final int MIN_DIFF_SIZE_VARS=4;
public static final int MIN_DIFF_SIZE_PROD=5;

/**
 * Creates a new ListPotential with no variables and
 * no list.
 */
    
public ListPotential() {

  variables = new Vector();
  list = new Vector();
  isExact = true;
}


/**
 * Creates a new ListPotential from a list of variables.
 * @param nl a <code>NodeList</code>.
 */

public ListPotential(NodeList nl) {
 
  variables = (Vector)nl.getNodes().clone();
  list = new Vector();
  isExact = true;
}

/**
 * Creates a new ListPotential from a <code>Vector</code> of
 * potentials.
 *
 * @param potentials a <code>Vector</code> of potentials. This  
 * <code>Vector</code> will be
 * cloned in variable <code>values</code>.
 */

public ListPotential(Vector potentials) {
  
  Vector v;
  Potential p;
  Node n;
  int i;
  SetVectorOperations op;

  op = new SetVectorOperations();
  list = (Vector)(potentials.clone());
  variables = new Vector();
  
  for (i=0 ; i<potentials.size() ; i++) {
    p = (Potential)potentials.elementAt(i);
    
    v = p.getVariables();
    variables = op.union(variables,v);
  }
  
  isExact = true;
}


public ListPotential(Potential pmt) {
  this();

  variables = pmt.getVariables();
  list.addElement(pmt);
}


public ListPotential(Potential pt1, Potential pt2) {
  this();

  SetVectorOperations op;  
  Vector v;

  op = new SetVectorOperations();
  variables = pt1.getVariables();
  v = pt2.getVariables();
  variables = op.union(variables,v);
  list.addElement(pt1);
  list.addElement(pt2);
}
 

/**
 * Gives the number of potentials contained in this ListPotential.
 *
 * @return the size of <code>list</code>.
 */

public int getListSize() {
 
  return list.size();
}


/**
 * @param i a position.
 * @return the potential at position <code>i</code> in
 * <code>list</code>.
 */

public Potential getPotentialAt(int i) {
  
  if ((i > (list.size()-1)) || (i<0)) {
    System.out.println("Error in ListPotential.getPotentialAt(int i): Position "+ i + " out of range");
  }
  
  return (Potential)list.elementAt(i);   
}


/**
 * @return true if all the potentials are exact. false otherwise.
 */

public boolean getExact() {

  return isExact;
}


/**
 * Sets the isExact flag to the given value.
 * @param exact a boolean value.
 */

public void setExact(boolean exact) {
 
  isExact = exact;
}


/**
 * @return the list of potentials.
 */

public Vector getList() {
  
  return list;
}


/**
 * Sets the list of potentials from a vector.
 * @param potentials a vector of potentials.
 */

public void setList(Vector potentials) {
  
  list = potentials;
}

/**
   * This method incorporates the evidence passed as argument to the
   * potential, that is, puts to 0 all the values whose configurations
   * are not consistent with the evidence.
   * @param evid The Evidence to be incorporated in this ListPotential
   */
  public void instantiateEvidence(Configuration evid) {
    int n=list.size();
    for(int i=0;i<n;i++){
      getPotentialAt(i).instantiateEvidence(evid);
    }
  }

/**
 * Tells if two <code>ListPotential</code>s contains the same
 * <code>Potential</code>s. This method is used to allow constructing a
 * <code>HashMap</code> with <code>ListPotential</code>s as keys.
 * @return <code>true</code> if this <code>ListPotential</code> contains
 * the same list of <code>Potential</code>s as <code>pot</code>;
 *  <code>else</code> otherwise.
 */
public boolean equals(Object pot) {

  int i;

  if ((pot!=null) && (pot instanceof ListPotential)) {
    if (getListSize() != ((ListPotential)pot).getListSize()) {
      return false;
    }
    
    for (i=0 ; i<getListSize() ; i++) {
      if (((ListPotential)pot).list.indexOf(getPotentialAt(i)) < 0) {
        return false;
      }
    }
    return true;
  }
  else {
    return false;
  }
}


/**
 * Gets a hash code for this <code>ListPotential</code> to allow constructing a
 * <code>HashMap</code> with <code>ListPotential</code>s as keys. The hash
 * code is calculated as the sum of the hash codes of all the 
 * <code>Potential</code>s in this <code>ListPotential</code>
 * @return the hash code associated to this <code>ListPotential</code>
 */

public int hashCode() {
  int i, code = 0;
  
  for (i=0 ; i<getListSize() ; i++) {
    code += getPotentialAt(i).hashCode();
  }
  return code;
}


/**
 * Combines two ListPotentials. The combination is just the union
 * of both lists of potentials.
 * To keep compatibility with the abstract definition of this method,
 * the value returned is of class <code>Potential</code>.
 *
 * @param lp the ListPotential to combine with this one.
 * @return a new <code>Potential</code> with the result of
 * the combination.
 */

public Potential combine(Potential lp) {

  ListPotential newPot, pot;
  SetVectorOperations op = new SetVectorOperations();
  Vector newList;
  
  pot = (ListPotential)lp;
  
  newList = op.union(list,pot.getList());
  
  newPot = new ListPotential(newList);
  
  return newPot;
}


/**
 * Calls the method combine(Potential), and performs the
 * corresponding casting to return a value
 * of class <code>ListPotential</code>.
 *
 * @param lp the ListPotential to combine with this one.
 * @return a new <code>ListPotential</code> with the result of
 * the combination.
 */

public ListPotential combine(ListPotential lp) {
  
  return (ListPotential)combine((Potential)lp);
}

/**
 *  Divides this ListPotential by the ListPotential passed to this method. This is made by dividing
 * the combination of all the Potentials of the list of this ListPotential with the combination
 * of all the Potentials of the parameter pot.
 * @param pot the ListPotential used as divisor 
 * @return a new Potential resulting from dividing this ListPotential by the ListPotential pot
 */
public Potential divide(Potential pot){
   Potential pot1,pot2;
   pot1 = createPotential();
   pot2 = ((ListPotential)pot).createPotential();
   return pot1.divide(pot2);
}


/**
 * Looks for the potentials in the list that contain a given
 * <code>Node</code> variable.
 *
 * @param x a <code>Node</code> variable.
 * @return a <code>Vector</code> containing the <code>Potential</code>s
 * in <code>list</code> defined for the argument variable.
 */

public Vector getPotentialsContaining(Node x) {

  int i;
  Vector v;
  Potential pot;
  
  v = new Vector();
  
  for (i=0 ; i<list.size() ; i++) {
    pot = (Potential)list.elementAt(i);
    if (pot.getVariables().indexOf(x) >= 0)
      v.addElement(pot);
  }
  
  return v;
}
 

/**
 * Removes the argument variable summing over all its values.
 * To remove the variable, all the potentials containing it are
 * combined and then the variable is removed from the potential
 * resulting from the combination.
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials combined from the <code>list</code> are replaced
 * y the potential obtained after combining them and summing over the
 * deleted variable.
 *
 * @param var a <code>Node</code> variable.
 * @return a new <code>Potential</code> with the result of the deletion.
 */

public Potential addVariable(Node var) {

  Vector newList, potsToRemove;
  int i;
  Potential pot;
  ListPotential newPot;

  newList = new Vector();
  potsToRemove = new Vector();
  
  for (i=list.size()-1 ; i>=0 ; i--) {
    pot = (Potential)list.elementAt(i);
    if (pot.getVariables().indexOf(var) >= 0)
      potsToRemove.addElement(pot);
    else
      newList.addElement(pot);
  }
  
  if (potsToRemove.size() > 0) {
    ListPotential listPotsToRemove = new ListPotential(potsToRemove);
    pot = listPotsToRemove.createPotential(); 
    pot = pot.addVariable(var);
    newList.addElement(pot);
  }
  newPot = new ListPotential(newList);
  
  return newPot;
}


/**
 * Removes the argument variable summing over all its values.
 * The potentials containing it are NOT combined.
 *
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials containing the variable to remove are replaced
 * by the potentials obtained from them after summing out the
 * omitted variable.
 *
 * @param var a <code>FiniteStates</code> variable.
 * @param limitForPrunning the limit for prunning after combining and summing
 * on the <code>FiniteStates</code> variable var.
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(FiniteStates var,
			     double limitForPrunning) {
  
  Vector newList, potsToRemove;
  int i;
  Potential pot;
  ListPotential newPot;
  
  newList = new Vector();
  potsToRemove = new Vector(); 
  for (i=list.size()-1 ; i>=0 ; i--) {
    pot = (Potential)list.elementAt(i);
    if (pot.getVariables().indexOf(var) >= 0)
      potsToRemove.addElement(pot);
    else
      newList.addElement(pot);
  }
  
  for (i=0 ; i<potsToRemove.size() ; i++) {
    pot = (Potential)potsToRemove.elementAt(i);
    pot = pot.addVariable(var);
    pot.limitBound(limitForPrunning);
    newList.addElement(pot);
  }
    
  newPot = new ListPotential(newList);
  return newPot;
}


/**
 * Removes the argument variable summing over all its values.
 * The potentials containing it are NOT combined.
 *
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials containing the variable to remove are replaced
 * by the potentials obtained from them after summing out the
 * omitted variable.
 *
 * @param var a <code>FiniteStates</code> variable.
 * @param limitForPrunning the limit for prunning after combining and summing
 * on the <code>FiniteStates</code> variable var.
 * @param limitSum limit sum for pruning.
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(FiniteStates var,
			     double limitForPrunning,
			     double limitSum) {
  
  Vector newList, potsToRemove;
  int i;
  Potential pot;
  ListPotential newPot;
  
  newList = new Vector();
  potsToRemove = new Vector(); 
  for (i=list.size()-1 ; i>=0 ; i--) {
    pot = (Potential)list.elementAt(i);
    if (pot.getVariables().indexOf(var) >= 0)
      potsToRemove.addElement(pot);
    else
      newList.addElement(pot);
  }
  
  for (i=0 ; i<potsToRemove.size() ; i++) {
    pot = (Potential)potsToRemove.elementAt(i);
    pot = pot.addVariable(var);
    pot.limitBound(limitForPrunning,limitSum);
    newList.addElement(pot);
  }
    
  newPot = new ListPotential(newList);
  return newPot;
}


/**
 * Removes the argument variable summing over all its values.
 * To remove the variable, all the potentials containing it are
 * combined and then the variable is removed from the potential
 * resulting from the combination.
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials combined from the <code>list</code> are replaced
 * y the potential obtained after combining them and summing over the
 * deleted variable.
 *
 * @param var a <code>FiniteStates</code> variable.
 * @param limitForPrunning the limit for prunning after combining and summing
 * on the <code>FiniteStates</code> variable var.
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(FiniteStates var,
			     double limitForPrunning,
                             int heuristicToJoin) {
  
  Vector newList, potsToRemove;
  int i;
  Potential pot;
  ListPotential newPot;
  
  newList = new Vector();
  potsToRemove = new Vector(); 
  
  for (i=list.size()-1 ; i>=0 ; i--) {
    pot = (Potential)list.elementAt(i);
    if (pot.getVariables().indexOf(var) >= 0)
      potsToRemove.addElement(pot);
    else
      newList.addElement(pot);
  }
  
  if (potsToRemove.size() > 0) {
    ListPotential listPotsToRemove = new ListPotential(potsToRemove);
    pot = listPotsToRemove.createPotential(limitForPrunning,heuristicToJoin);
    pot = pot.addVariable(var);
    pot.limitBound(limitForPrunning);
    newList.addElement(pot);
  }
  newPot = new ListPotential(newList);
  return newPot;
}


/**
 * Removes the argument variable summing over all its values.
 * To remove the variable, all the potentials containing it are
 * combined and then the variable is removed from the potential
 * resulting from the combination.
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials combined from the <code>list</code> are replaced
 * y the potential obtained after combining them and summing over the
 * deleted variable.
 *
 * @param var a <code>FiniteStates</code> variable.
 * @param limitForPrunning the limit for prunning after combining and summing
 * on the <code>FiniteStates</code> variable var.
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @param limitSum limit sum for pruning.
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(FiniteStates var,
			     double limitForPrunning,
                             int heuristicToJoin,
			     double limitSum) {
  
  Vector newList, potsToRemove;
  int i;
  Potential pot;
  ListPotential newPot;
  
  newList = new Vector();
  potsToRemove = new Vector(); 
  
  for (i=list.size()-1 ; i>=0 ; i--) {
    pot = (Potential)list.elementAt(i);
    if (pot.getVariables().indexOf(var) >= 0)
      potsToRemove.addElement(pot);
    else
      newList.addElement(pot);
  }
  
  if (potsToRemove.size() > 0) {
    ListPotential listPotsToRemove = new ListPotential(potsToRemove);
    pot = listPotsToRemove.createPotential(limitForPrunning,limitSum,heuristicToJoin);
    pot = pot.addVariable(var);
    pot.limitBound(limitForPrunning,limitSum);
    newList.addElement(pot);
  }
  newPot = new ListPotential(newList);
  return newPot;
}


// SIZE
/**
 * Removes the argument variable summing over all its values.
 * To remove the variable, all the potentials containing it are
 * combined and then the variable is removed from the potential
 * resulting from the combination.
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials combined from the <code>list</code> are replaced
 * y the potential obtained after combining them and summing over the
 * deleted variable.
 *
 * @param var a <code>FiniteStates</code> variable.
 * @param limitForPrunning the limit for prunning after combining and summing
 * on the <code>FiniteStates</code> variable var.
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @param limitSum limit sum for pruning.
 * @param vecSiz vector loading the size of the potential just before delete
 *               the variable from it.
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(FiniteStates var,
			     double limitForPrunning,
                             int heuristicToJoin,
			     double limitSum, Vector vecSiz) {
  
  Vector newList, potsToRemove;
  int i;
  Potential pot;
  ListPotential newPot;
  
  newList = new Vector();
  potsToRemove = new Vector(); 
  
  for (i=list.size()-1 ; i>=0 ; i--) {
    pot = (Potential)list.elementAt(i);
    if (pot.getVariables().indexOf(var) >= 0)
      potsToRemove.addElement(pot);
    else
      newList.addElement(pot);
  }
  
  if (potsToRemove.size() > 0) {
    ListPotential listPotsToRemove = new ListPotential(potsToRemove);
    pot = listPotsToRemove.createPotential(limitForPrunning,limitSum,heuristicToJoin);
    // SIZE: get the size of the potential
      if(vecSiz!=null){
         Double tama=new Double((double)pot.getSize());
         vecSiz.addElement(tama);
      }//end
    pot = pot.addVariable(var);
    pot.limitBound(limitForPrunning,limitSum);
    newList.addElement(pot);
  }
  newPot = new ListPotential(newList);
  return newPot;
}




/**
 * Removes the argument variable summing over all its values.
 * To remove the variable, all the potentials containing it are
 * combined and then the variable is removed from the potential
 * resulting from the combination. However, before the combination,
 * the potentials are factorised with respect to the given
 * variable.
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials combined from the <code>list</code> are replaced
 * by the potential obtained after combining them and summing over the
 * deleted variable.
 *
 * @param var a <code>FiniteStates</code> variable.
 * @param limitForPrunning the limit for prunning after combining and summing
 * on the <code>FiniteStates</code> variable var.
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @param limitSum limit sum for pruning.
 * @param factParam the parameters for approximate or exact factorisation.
 * @return a new Potential with the result of the deletion.
 */

public Potential factorAddVariable(FiniteStates var,
				   double limitForPrunning,
				   int heuristicToJoin,
				   double limitSum,
                                   FactorisationTools factParam) {
  
  Vector newList, potsToRemove, tempPots;
  int i, j, indexVar, numTrees;
  Potential pot;
  PotentialTree tmpPot,copiaPT;
  ListPotential newPot, listPotsToRemove;
 
  newList = new Vector();
  potsToRemove = new Vector(); 
  
  for (i=list.size()-1 ; i>=0 ; i--)
  {   
      pot = (Potential)list.elementAt(i);
      indexVar= pot.getVariables().indexOf(var);
      copiaPT= (PotentialTree)pot;
      
      if ( indexVar >= 0 ) {   
          
         switch(factParam.getFactMethod()) {
           case 0: {  // only Split
             tempPots = copiaPT.splitOnlyPT(var,factParam);
             /* if(tempPots.size()==2){
                PotentialTree auxpp,p0,p1;
                p0= (PotentialTree)tempPots.elementAt(0);
                p1= (PotentialTree)tempPots.elementAt(1);
                auxpp= p0.combine(p1);
                auxpp.compareAndPrint(copiaPT);
             }*/
             break;
           }       
           case 1:{ // only Factorise     
             tempPots = copiaPT.factoriseOnlyPT(var,factParam); 
             break;
           }    
           case 2:{ // split&factorise
               tempPots = copiaPT.splitAndFactorisePT(var,factParam);
               break;
           }  
           default: {tempPots=new Vector(); break;}  // Initialize the vector
         }//switch
 
         numTrees= tempPots.size();
        
         if(numTrees==0) // nor split, nor factorisation have been made
            potsToRemove.addElement(copiaPT); //(pot);
         else {
            for (j=0 ; j< numTrees; j++) {
               tmpPot = (PotentialTree)tempPots.elementAt(j);
               if ( j== numTrees-1) // only the last tree contains the variable
                  potsToRemove.addElement(tmpPot);
               else 
                 if (tmpPot.getVariables().size() > 0) // Ignore potentials without variables
                     newList.addElement(tmpPot);                                          
            }//for                
         }
      }// if variable in the potential
      
      else // the variable is NOT in the potential
        newList.addElement(copiaPT); //(pot)
      
  }//for (all the potentials in the list)
  
  if (potsToRemove.size() > 0)
  {
      listPotsToRemove = new ListPotential(potsToRemove);
      pot = listPotsToRemove.createPotential(limitForPrunning,limitSum,heuristicToJoin);
    
      if(factParam.sizesPot){
         Double tama=new Double((double)pot.getSize());
         factParam.vecPotSizes.addElement(tama);
      }
      
      pot = pot.addVariable(var);
      pot.limitBound(limitForPrunning,limitSum);
      newList.addElement(pot);
  }
  newPot = new ListPotential(newList);
  
  return newPot;
}


/**
 * Factorise all the potentials of the list, each one with respect to  
 * its own variables.
 * 
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials from the <code>list</code> are replaced
 * by the potential obtained after factorise them respect to its variables.
 * 
 * @param factParam the values for approximate or exact factorisation.
 * @return a new Potential.
 */

public Potential factorisePotentialAllVbles(FactorisationTools facParam){
    
   int i,v, j,p, numPrTrees,indexVar;
   int contFac=0;
   Potential newPot,pot;
   PotentialTree copiaPot,tmpPot;
   Vector nextList,vecPots,outList,newListPot;
   FiniteStates varFac;
   
   
   newListPot = new Vector();
   
   for (i=list.size()-1 ; i>=0 ; i--){
      
      pot = (Potential)list.elementAt(i);
      
      nextList=new Vector(); // Pot. trees to be factorised
      nextList.addElement((PotentialTree)pot);
     
      for(v=0; v< pot.getVariables().size() ; v++) {
          
          varFac= (FiniteStates)(pot.getVariables()).elementAt(v);
          
          outList = new Vector(); // loads  Pot.Trees after the factorisation
                
          for(p=0;p<nextList.size();p++){
              
             copiaPot= (PotentialTree)nextList.elementAt(p);
              
             switch(facParam.getFactMethod()) {
                case 0: {  // only Split
                    vecPots = copiaPot.splitOnlyPT(varFac,facParam); 
                    break;
                }       
                case 1:{ // only Factorise     
                    vecPots = copiaPot.factoriseOnlyPT(varFac,facParam); 
                    break;
                }    
                case 2:{ // split&factorise
                    vecPots = copiaPot.splitAndFactorisePT(varFac,facParam);
                    break;
                }  
                default: {vecPots=new Vector(); break;} 
             }//switch
 
             numPrTrees= vecPots.size(); 
        
             if(numPrTrees>0){ // if ==0 means nor split, nor factorisation have been made
            
               contFac++;
               for (j=0 ; j< numPrTrees ; j++){
                  tmpPot= (PotentialTree)vecPots.elementAt(j);
                  outList.addElement(tmpPot);
               }              
            }
            else // add only the initial Potential (not factorised)
                outList.addElement(copiaPot);     
          }// for each Potential Tree in the vector "nextList"
          
          nextList=outList; // now, factorise these potential trees respect to another variable
          
      }//for each variable of the potential  
      
      // Save the factorised potential trees from the actual potential
      for(j=0;j<nextList.size();j++) 
          newListPot.addElement( (PotentialTree) nextList.elementAt(j));
      
   }// for each potential in the list
   //System.out.println( "...Pot Final= " +newListPot.size());
   
   if (contFac > 0) // at least one pot. tree has been factorised
      newPot = new ListPotential(newListPot);
   else
      newPot=null;
   
   return(newPot);
}


/**
 * Removes the argument variable summing over all its values.
 * To remove the variable, all the potentials containing it are
 * combined and then the variable is removed from the potential
 * resulting from the combination.
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials combined from the <code>list</code> are replaced
 * y the potential obtained after combining them and summing over the
 * deleted variable.
 *
 * @param var a <code>FiniteStates</code> variable.
 * @param limitForPrunning the limit for prunning after combining and summing
 * on the <code>FiniteStates</code> variable var.
 * @param cache1 a <code>HashMap</code> where the key is a 
 * <code>ListPotential</code> with two <code>Potential</code>s and the
 * value is a <code>Potential</code> corresponding to the combination of
 * the two previous <code>Potential</code>s. This cache is used to save 
 * combinations when they have been previously done, but combinations are done
 * only with variables.
 * @param cache2 a <code>HashMap</code> similar to cache1 but now combinations
 * are completly done.
 * @param cache1M a <code>HashMap</code> used to save marginalizations in
 * first navigation
 * @param cache2M a <code>HashMap</code> used to save marginalizations in
 * second navigation
 * @param firstTour a <code>boolean</code> that indicates if we are in the
 * first navigation (<code>true</code>) or in the second navigation 
 * (<code>false</code>).
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(FiniteStates var,
			     double limitForPrunning,
			     HashMap cache1,
			     HashMap cache2,
			     HashMap cache1M,
			     HashMap cache2M,
			     boolean firstTour,
			     int heuristicToJoin) {
  
  Vector newList, potsToRemove;
  int i;
  Potential pot, potR;
  ListPotential newPot;
  PotentialVar pVar;
  
  newList = new Vector();
  potsToRemove = new Vector();
  
  for (i=list.size()-1 ; i>=0 ; i--) {
    pot = (Potential)list.elementAt(i);
    if (pot.getVariables().indexOf(var) >= 0)
      potsToRemove.addElement(pot);
    else
      newList.addElement(pot);
  }
  
  if (potsToRemove.size() > 0) {
    ListPotential listPotsToRemove = new ListPotential(potsToRemove);
    pot = listPotsToRemove.createPotential(limitForPrunning,cache1,cache2,
					   firstTour,heuristicToJoin);
    if (firstTour) {
      pVar = new PotentialVar(pot,var);
      if (!cache1M.containsKey(pVar)) { // when this marginalization has not 
                                        // been previously done
        potR = pot.makePotential();
        potR.addVariable(pot,var,ONLY_VARS);
        cache1M.put(pVar,potR);
      }
      else { // when this marginalization has been previously done
        potR = (Potential)cache1M.get(pVar);
        cache2M.put(pVar,null);
      }
    }
    else {
      pVar = new PotentialVar(pot,var);
      if (cache2M.containsKey(pVar)) {
        potR = (Potential)cache2M.get(pVar);
        if (potR == null) {
          potR = (Potential)cache1M.get(pVar);
          potR.addVariable(pot,var,ONLY_VALUES);
          potR.limitBound(limitForPrunning);
          cache2M.put(pVar,potR);
        }
      }
      else {
        potR = (Potential)cache1M.get(pVar); 
        potR.addVariable(pot,var,ONLY_VALUES);
        potR.limitBound(limitForPrunning);      
        cache1M.remove(pVar);
      }
    }
    newList.addElement(potR);
  }
  newPot = new ListPotential(newList);
  
  return newPot;
}


/**
 * Removes the argument variable summing over all its values.
 * To remove the variable, all the potentials containing it are
 * combined and then the variable is removed from the potential
 * resulting from the combination.
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials combined from the <code>list</code> are replaced
 * y the potential obtained after combining them and summing over the
 * deleted variable.
 *
 * @param var a <code>FiniteStates</code> variable.
 * @param limitForPrunning the limit for prunning after combining and summing
 * on the <code>FiniteStates</code> variable var.
 * @param limitSum limit sum for pruning.
 * @param cache1 a <code>HashMap</code> where the key is a 
 * <code>ListPotential</code> with two <code>Potential</code>s and the
 * value is a <code>Potential</code> corresponding to the combination of
 * the two previous <code>Potential</code>s. This cache is used to save 
 * combinations when they have been previously done, but combinations are done
 * only with variables.
 * @param cache2 a <code>HashMap</code> similar to cache1 but now combinations
 * are completly done.
 * @param cache1M a <code>HashMap</code> used to save marginalizations in
 * first navigation
 * @param cache2M a <code>HashMap</code> used to save marginalizations in
 * second navigation
 * @param firstTour a <code>boolean</code> that indicates if we are in the
 * first navigation (<code>true</code>) or in the second navigation 
 * (<code>false</code>).
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(FiniteStates var,
			     double limitForPrunning,
			     double limitSum,
			     HashMap cache1,
			     HashMap cache2,
			     HashMap cache1M,
			     HashMap cache2M,
			     boolean firstTour,
			     int heuristicToJoin) {
  
  Vector newList, potsToRemove;
  int i;
  Potential pot, potR;
  ListPotential newPot;
  PotentialVar pVar;
  
  newList = new Vector();
  potsToRemove = new Vector();
  
  for (i=list.size()-1 ; i>=0 ; i--) {
    pot = (Potential)list.elementAt(i);
    if (pot.getVariables().indexOf(var) >= 0)
      potsToRemove.addElement(pot);
    else
      newList.addElement(pot);
  }
  
  if (potsToRemove.size() > 0) {
    ListPotential listPotsToRemove = new ListPotential(potsToRemove);
    pot = listPotsToRemove.createPotential(limitForPrunning,limitSum,
					   cache1,cache2,
					   firstTour,heuristicToJoin);
    if (firstTour) {
      pVar = new PotentialVar(pot,var);
      if (!cache1M.containsKey(pVar)) { // when this marginalization has not 
                                        // been previously done
        potR = pot.makePotential();
        potR.addVariable(pot,var,ONLY_VARS);
        cache1M.put(pVar,potR);
/*System.out.println("METO EN CACHE1M:");
pot.print();
var.print();
potR.print();*/
      }
      else { // when this marginalization has been previously done
        potR = (Potential)cache1M.get(pVar);
        cache2M.put(pVar,null);
      }
    }
    else {
      pVar = new PotentialVar(pot,var);
      if (cache2M.containsKey(pVar)) {
        potR = (Potential)cache2M.get(pVar);
        if (potR == null) {
          potR = (Potential)cache1M.get(pVar);
          potR.addVariable(pot,var,ONLY_VALUES);
          potR.limitBound(limitForPrunning,limitSum);
          cache2M.put(pVar,potR);
        }
      }
      else {
        potR = (Potential)cache1M.get(pVar); 
        potR.addVariable(pot,var,ONLY_VALUES);
        potR.limitBound(limitForPrunning,limitSum);      
        cache1M.remove(pVar);
      }
    }
    newList.addElement(potR);
  }
  newPot = new ListPotential(newList);
  
  return newPot;
}


/**
 * Removes the argument variable summing over all its values.
 * To remove the variable, all the potentials containing it are
 * combined and then the variable is removed from the potential
 * resulting from the combination.
 * The result will be a new object of class <code>Potential</code>
 * that can be casted to class <code>ListPotential</code> and where
 * the potentials combined from the <code>list</code> are replaced
 * y the potential obtained after combining them and summing over the
 * deleted variable.
 *
 * @param var a FiniteStates variable.
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(FiniteStates var,
			     double limitForPrunning,
			     double lowLimitForPrunning,
			     int method) {
  
  Vector newList, potsToRemove;
  int i;
  Potential pot;
  ListPotential newPot;
  
  newList = new Vector();
  potsToRemove = new Vector();
  
  for (i=list.size()-1 ; i>=0 ; i--) {
    pot = (Potential)list.elementAt(i);
    if (pot.getVariables().indexOf(var) >= 0)
      potsToRemove.addElement(pot);
    else
      newList.addElement(pot);
  }
  
  if (potsToRemove.size() > 0) {
    ListPotential listPotsToRemove = new ListPotential(potsToRemove);
    pot = listPotsToRemove.createPotential();
    
    pot = pot.addVariable(var);
    
    if (pot.getClassName().equals("PotentialMTree")) {
      PotentialMTree unit;
      unit = new PotentialMTree();
      unit.setTree(MultipleTree.unitTree());
      pot = ((PotentialMTree)pot).conditional((Potential)unit);
      ((PotentialMTree)pot).conditionalLimitBound(limitForPrunning,lowLimitForPrunning,method);
    }
    else {
      System.out.println("Error in ListPotential.addVariable(" + 
	"FiniteStates var,double limitForPrunning," + 
	"double lowLimitForPrunning,int method) :" +
	"this method must be only used when this list contains only" +
	"potentials of class PotentialMTree");
      System.exit(1);
    }
      
    newList.addElement(pot);
  }

  newPot = new ListPotential(newList);
  
  return newPot;
}


/**
 * Marginalizes a ListPotential to a list of variables.
 * It is equivalent to remove (addVariable) the other variables.
 *
 * @param vars a vector of <code>FiniteStates</code> variables.
 * @return a new <code>ListPotential</code> with the marginal.
 * @see addVariable(Vector vars)
 */

public Potential marginalizePotential(Vector vars) {

  int i, j;
  boolean found;
  FiniteStates var1, var2;
  ListPotential newPot;

  newPot = new ListPotential(list);
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;
    for (j=0 ; j<vars.size(); j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    if (!found)
      newPot = (ListPotential)newPot.addVariable(var1);
  }
  return newPot;
}


/**
 * Marginalizes a <code>ListPotential</code> approximating the result to a
 * list of variables. It is equivalent to remove (addVariable) 
 * the other variables.
 * Each variable is removed without combining the potentials
 * that contain it.
 *
 * @param vars a <code>Vector</code> of the <code>FiniteStates</code>
 * variables we want to sum out in this <code>Potential</code>
 * @param limitForPrunning the limit for prunning.
 * @return a new <code>ListPotential</code> with the marginal.
 * @see addVariable(FiniteStates var,
	double limitForPrunning)
 */

public Potential marginalizePotential(Vector vars,
				      double limitForPrunning) {
  
  int i, j;
  boolean found;
  FiniteStates var1, var2;
  ListPotential newPot;
  
  newPot = new ListPotential(list);
  
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;

    for (j=0 ; j<vars.size() ; j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    if (!found)
      newPot = (ListPotential)newPot.addVariable(var1,limitForPrunning);
  }
  return newPot;
}


/**
 * Marginalizes a <code>ListPotential</code> approximating the result to a
 * list of variables. It is equivalent to remove (addVariable) 
 * the other variables.
 * Each variable is removed without combining the potentials
 * that contain it.
 *
 * @param vars a <code>Vector</code> of the <code>FiniteStates</code>
 * variables we want to sum out in this <code>Potential</code>
 * @param limitForPrunning the limit for prunning.
 * @param limitSum limit sum for pruning.
 * @return a new <code>ListPotential</code> with the marginal.
 * @see addVariable(FiniteStates var,
	double limitForPrunning)
 */

public Potential marginalizePotential(Vector vars,
				      double limitForPrunning,
				      double limitSum) {
  
  int i, j;
  boolean found;
  FiniteStates var1, var2;
  ListPotential newPot;
  
  newPot = new ListPotential(list);
  
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;

    for (j=0 ; j<vars.size() ; j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    if (!found)
      newPot = (ListPotential)newPot.addVariable(var1,
						 limitForPrunning,
						 limitSum);
  }
  return newPot;
}


/**
 * Marginalizes a <code>ListPotential</code> approximating the result to a
 * list of variables.It is equivalent to remove (addVariable) 
 * the other variables.
 *
 * @param vars a <code>Vector</code> of the <code>FiniteStates</code>
 * variables we want to sum out in this <code>Potential</code>
 * @param limitForPrunning the limit for prunning.
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @return a new <code>ListPotential</code> with the marginal.
 * @see addVariable(FiniteStates var,
	double limitForPrunning)
 */

public Potential marginalizePotential(Vector vars,
				      double limitForPrunning,
                                      int heuristicToJoin) {
  
  int i, j;
  boolean found;
  FiniteStates var1, var2;
  ListPotential newPot;

  newPot = new ListPotential(list);
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;

    for (j=0 ; j<vars.size(); j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    if (!found)
      newPot = (ListPotential)newPot.addVariable(var1,
						 limitForPrunning,
                                                 heuristicToJoin);
  }
  return newPot;
}


/**
 * Marginalizes a <code>ListPotential</code> approximating the result to a
 * list of variables.It is equivalent to remove (addVariable) 
 * the other variables.
 *
 * @param vars a <code>Vector</code> of the <code>FiniteStates</code>
 * variables we want to sum out in this <code>Potential</code>
 * @param limitForPrunning the limit for prunning.
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @param limitSum limit sum for pruning.
 * @return a new <code>ListPotential</code> with the marginal.
 * @see addVariable(FiniteStates var,
	double limitForPrunning)
 */

public Potential marginalizePotential(Vector vars,
				      double limitForPrunning,
                                      int heuristicToJoin,
				      double limitSum) {
  
  int i, j;
  boolean found;
  FiniteStates var1, var2;
  ListPotential newPot;

  newPot = new ListPotential(list);
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;

    for (j=0 ; j<vars.size(); j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    if (!found)
      newPot = (ListPotential)newPot.addVariable(var1,
						 limitForPrunning,
                                                 heuristicToJoin,
						 limitSum);
  }
  return newPot;
}


//SIZES
/**
 * Marginalizes a <code>ListPotential</code> approximating the result to a
 * list of variables.It is equivalent to remove (addVariable) 
 * the other variables.
 *
 * @param vars a <code>Vector</code> of the <code>FiniteStates</code>
 * variables we want to sum out in this <code>Potential</code>
 * @param limitForPrunning the limit for prunning.
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @param limitSum limit sum for pruning.
 * @param vecSiz vector loading the size of the potential just before delete
 *               the variable from it.
 * @return a new <code>ListPotential</code> with the marginal.
 * @see addVariable(FiniteStates var,
	double limitForPrunning)
 */

public Potential marginalizePotential(Vector vars,
				      double limitForPrunning,
                                      int heuristicToJoin,
				      double limitSum, Vector vecsiz) {
  
  int i, j;
  boolean found;
  FiniteStates var1, var2;
  ListPotential newPot;

  newPot = new ListPotential(list);
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;

    for (j=0 ; j<vars.size(); j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    if (!found)
      newPot = (ListPotential)newPot.addVariable(var1,
						 limitForPrunning,
                                                 heuristicToJoin,
						 limitSum, vecsiz);
  }
  return newPot;
}


/**
 * Marginalizes a <code>ListPotential</code> approximating the result to a
 * list of variables.It is equivalent to remove (addVariable) 
 * the other variables. The potentials containing the
 * variable to remove in each step are factorised.
 *
 * @param vars a <code>Vector</code> of the <code>FiniteStates</code>
 * variables we want to sum out in this <code>Potential</code>
 * @param limitForPrunning the limit for prunning.
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @param limitSum limit sum for pruning.
 * @param factParam the parameters for approximate or exact factorisation.
 * @return a new <code>ListPotential</code> with the marginal.
 * @see addVariable(FiniteStates var,
	double limitForPrunning)
 */

public Potential factorMarginalizePotential(Vector vars,
					    double limitForPrunning,
					    int heuristicToJoin,
					    double limitSum,
					    FactorisationTools factParam){
  
  int i, j;
  boolean found;
  FiniteStates var1, var2;
  ListPotential newPot;

  newPot = new ListPotential(list);
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;

    for (j=0 ; j<vars.size(); j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    if (!found)
      newPot = (ListPotential)newPot.factorAddVariable(var1,
						       limitForPrunning,
						       heuristicToJoin,
						       limitSum,
						       factParam);
  }
  return newPot;
}



/**
 * Marginalizes a ListPotential approximating the result to a
 * list of variables.It is equivalent to remove (addVariable) 
 * the other variables.
 *
 * @param vars a <code>Vector</code> of the <code>FiniteStates</code>
 * variables we want to sum out in this <code>Potential</code>
 * @param limitForPrunning the limit for prunning.
 * @param cache1 a <code>HashMap</code> where the key is a 
 * <code>ListPotential</code> with two <code>Potential</code>s and the
 * value is a <code>Potential</code> corresponding to the combination of
 * the two previous <code>Potential</code>s. This cache is used to save 
 * combinations when they have been previously done, but combinations are done
 * only with variables.
 * @param cache2 a <code>HashMap</code> similar to cache1 but now combinations
 * are completly done.
 * @param cache1M a <code>HashMap</code> used to save marginalizations
 * @param cache2M a <code>HashMap</code> used to save marginalizations in
 * second navigation
 * @param firstTour a <code>boolean</code> that indicates if we are in the
 * first navigation (<code>true</code>) or in the second navigation 
 * (<code>false</code>).
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @return a new <code>ListPotential</code> with the marginal.
 * @see addVariable(FiniteStates var,
 *	double limitForPrunning,HashMap cache1,HashMap cache2,
 *      HashMap cache1M,HashMap cache2M,boolean firstTour,int heuristicToJoin)
 */

public Potential marginalizePotential(Vector vars,
				      double limitForPrunning,
				      HashMap cache1,
				      HashMap cache2,
				      HashMap cache1M,
				      HashMap cache2M,
				      boolean firstTour,
				      int heuristicToJoin) {
  
  int i, j;
  boolean found;
  FiniteStates var1, var2;
  ListPotential newPot;

  newPot = new ListPotential(list);
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;
    for (j=0 ; j<vars.size(); j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    if (!found)
      newPot = (ListPotential)newPot.addVariable(var1,
                 limitForPrunning,cache1,cache2,cache1M,cache2M,
                 firstTour,heuristicToJoin);
  }
  return newPot;
}


/**
 * Marginalizes a ListPotential approximating the result to a
 * list of variables.It is equivalent to remove (addVariable) 
 * the other variables.
 *
 * @param vars a <code>Vector</code> of the <code>FiniteStates</code>
 * variables we want to sum out in this <code>Potential</code>
 * @param limitForPrunning the limit for prunning.
 * @param limitSum limit sum for pruning.
 * @param cache1 a <code>HashMap</code> where the key is a 
 * <code>ListPotential</code> with two <code>Potential</code>s and the
 * value is a <code>Potential</code> corresponding to the combination of
 * the two previous <code>Potential</code>s. This cache is used to save 
 * combinations when they have been previously done, but combinations are done
 * only with variables.
 * @param cache2 a <code>HashMap</code> similar to cache1 but now combinations
 * are completly done.
 * @param cache1M a <code>HashMap</code> used to save marginalizations
 * @param cache2M a <code>HashMap</code> used to save marginalizations in
 * second navigation
 * @param firstTour a <code>boolean</code> that indicates if we are in the
 * first navigation (<code>true</code>) or in the second navigation 
 * (<code>false</code>).
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @return a new <code>ListPotential</code> with the marginal.
 * @see addVariable(FiniteStates var,
 *	double limitForPrunning,HashMap cache1,HashMap cache2,
 *      HashMap cache1M,HashMap cache2M,boolean firstTour,int heuristicToJoin)
 */

public Potential marginalizePotential(Vector vars,
				      double limitForPrunning,
				      double limitSum,
				      HashMap cache1,
				      HashMap cache2,
				      HashMap cache1M,
				      HashMap cache2M,
				      boolean firstTour,
				      int heuristicToJoin) {
  
  int i, j;
  boolean found;
  FiniteStates var1, var2;
  ListPotential newPot;

  newPot = new ListPotential(list);
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;
    for (j=0 ; j<vars.size(); j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    if (!found)
      newPot = (ListPotential)newPot.addVariable(var1,
                 limitForPrunning,limitSum,cache1,cache2,cache1M,
		 cache2M,firstTour,heuristicToJoin);
  }
  return newPot;
}


/**
 * Marginalizes a ListPotential approximating the result to a
 * list of variables.It is equivalent to remove (addVariable) 
 * the other variables.
 * This method must be only used when list contains only potentials
 * of class <code>PotentialMTree</code>
 *
 * @param vars a <code>Vector</code> of the <code>FiniteStates</code>
 * variables we want to sum out in this <code>Potential</code>
 * @param limitForPrunning the limit for prunning.
 * @param lowLimitForPrunning the low limit for prunning.
 * @param method the method for prunning.
 * @return a new <code>ListPotential</code> with the marginal.
 * @see addVariable(FiniteStates var,
	double limitForPrunning,double lowLimitForPrunning,
	int method)
 */

public Potential marginalizePotential(Vector vars,
				      double limitForPrunning,
				      double lowLimitForPrunning,
				      int method) {
  
  int i, j;
  boolean found;
  FiniteStates var1, var2;
  ListPotential newPot;

  newPot = new ListPotential(list);
  for (i=0 ; i<variables.size() ; i++) {
    var1 = (FiniteStates)variables.elementAt(i);
    found = false;
    for (j=0 ; j<vars.size(); j++) {
      var2 = (FiniteStates)vars.elementAt(j);
      if (var1 == var2) {
	found = true;
	break;
      }
    }
    if (!found)
      newPot = (ListPotential)newPot.addVariable(var1,
						 limitForPrunning,
						 lowLimitForPrunning,
						 method);
  }
  return newPot;
}

/**
 * Gets the size of this potential.
 * @return the sum of the sizes of the potentials that constitute this
 * one.
 */

public long getSize() {
  
  long s = 0;
  int i;
  
  for (i=0 ; i<list.size() ; i++)
    s += ((Potential)list.elementAt(i)).getSize();
  
  return s;
}


/**
 * Inserts a pontential in the vector of values.
 * 
 * @param pot the potential to insert.
 */

public void insertPotential(Potential pot) {
  
  if (list.indexOf(pot) < 0)
    list.addElement(pot);
}


/**
 * Removes the <code>Potential pot</code> from <code>list</code>
 * @param pot the <code>Potential pot</code> to be removed from 
 * <code>list</code>
 */

public void removePotential(Potential pot) {
  
  if (!(list.removeElement(pot))) {
    System.out.println("Error in ListPotential.removeElement(Potential pot): pot was not in list");
  }
}


/**
 * Saves this pontential.
 * @param p a <code>PrintWriter</code> where the
 * potential will be saved.
 */

public void saveResult(PrintWriter p) {
  
  int i;
  Potential pot;
  
  for (i=0 ; i<list.size() ; i++) {
     pot = (Potential)list.elementAt(i);
     pot.saveResult(p);
  }
}


/**
 * Restricts the potential to a given configuration.
 * @param conf restricting configuration.
 * @return Returns a new ListPotential where variables
 * in <code>conf</code> have been instantiated to their
 * values in <code>conf</code>.
 */

public Potential restrictVariable(Configuration conf) {
 
  ListPotential pot;
  Potential p;
  Vector temp;
  int i;
  
  temp = new Vector();
  
  // Computes the restriction of all the potentials in values.
  
  for (i=0 ; i<list.size() ; i++) {
    p = (Potential)list.elementAt(i);
    temp.addElement(p.restrictVariable(conf));
  }
  
  
  // Construct the restricted ListPotential.
  
  pot = new ListPotential(temp);
  
  return pot;
}


/**
 * Returns the value of the potential for the given configuration.
 * @param conf a Configuration.
 * @return the value for <code>conf</code>.
 */

public double getValue(Configuration conf) {
  
  int i;
  Potential pot;
  double x = 1.0;
  
  for (i=0 ; i<list.size() ; i++) {
    pot = (Potential)list.elementAt(i);
    x *= pot.getValue(conf);
  }
  return x;
}


/**
 * Gets the value for a configuration. In this case, the
 * configuration is represented by means of an array of int.
 * At each position, the value for certain variable is stored.
 * To know the position in the array corresponding to a given
 * variable, we use a hash table. In that hash table, the
 * position of every variable in the array is stored.
 *
 * @param positions a Hashtable.
 * @param conf an array of int.
 * @return the value corresponding to configuration conf.
 */

public double getValue(Hashtable positions, int[] conf) {
  
  int i;
  Potential pot;
  double x = 1.0;
  
  for (i=0 ; i<list.size() ; i++) {
    pot = (Potential)list.elementAt(i);
    x *= pot.getValue(positions,conf);
  }
  return x;
}


/**
 * Prunes the trees associated with the potentials in <code>list</code>.
 * The potentials are assumed to be of class <code>PotentialTree</code>.
 * This method MODIFIES the potentials in <code>list</code>.
 *
 * @param lp the limit information value for pruning.
 */

public void limitBound(double lp) {
 
  int i;
  PotentialTree pot;
  
  for (i=0 ; i<list.size() ; i++) {
    pot = (PotentialTree)list.elementAt(i);
    pot.limitBound(lp);
  }
}


/**
 * Prunes the trees associated with the potentials in <code>list</code>.
 * The potentials are assumed to be of class <code>PotentialMTree</code>.
 * The flag isExact is set to <code>false</code> if some of the potentials
 * come to be approximate after pruning.
 *
 * This method MODIFIES the potentials in <code>list</code>.
 *
 * @param lp the limit information value for pruning.
 * @param llp the limit used to decide if we must considered
 * the tree as not pruned or pruned (exact or approximate).
 * @param method 1 for the exact method of calculating entropy and
 * 2 for an approximate method.
 */

public void conditionalLimitBound(double lp,
				  double llp,
				  int method) {
 
  int i;
  PotentialMTree pot;
  boolean exact = true;
  
  for (i=0 ; i<list.size() ; i++) {
    pot = (PotentialMTree)list.elementAt(i);
    pot.conditionalLimitBound(lp,llp,method);
    if (!pot.getExact())
      exact = false;
  }
  
  setExact(exact);
}


/**
 * Sorts the variables in all the potentials in <code>list</code>
 * and limits the number of leaves.
 * Sets <code>isExact</code> to false if the method carries out an
 * approximation in any of the <code>PotentialMTree</code>.
 * This version is for trees of class MultipleTree.
 *
 * @param maxLeaves maximum number of leaves in the new tree.
 * @param method the method of prunning: 0 for conditional
 * prunning or 1 for max-min prunning.
 * @return a new ListPotential with all the potentials sorted and bounded.
 */

public ListPotential conditionalSortAndBound(int maxLeaves, int method) {
 
  int i;
  PotentialMTree pot;
  ListPotential newPot;
  boolean exact = true;
  Vector v;
  
  v = new Vector();
  
  for (i=0 ; i<list.size() ; i++) {
    pot = (PotentialMTree)list.elementAt(i);
    pot = pot.conditionalSortAndBound(maxLeaves,method);
    if (!pot.getExact())
      exact = false;
    
    v.addElement(pot);
  }
  
  newPot = new ListPotential(v);
  
  newPot.setExact(exact);

  return newPot;
}


/**
 * This method is intended to condition a <code>ListPotential</code> on
 * another one. However, right now, it conditions every potential in
 * <code>list</code> to the unit <code>Potential</code>.
 *
 * @param listPot de conditioning <code>Potential</code>.
 * @return the conditional potential.
 */

public Potential conditional(Potential listPot) {

  PotentialMTree pmt, unit;
  ListPotential newPot;
  Vector v;
  int i;
  
  unit = new PotentialMTree();
  unit.setTree(MultipleTree.unitTree());
  
  v = new Vector();
  
  for (i=0 ; i<list.size() ; i++) {
    pmt = (PotentialMTree)list.elementAt(i);
    pmt = (PotentialMTree)pmt.conditional(unit);
    v.addElement(pmt);
  }
  
  newPot = new ListPotential(v);

  return newPot;
}


/**
 * Copies this potential.
 * @return a copy of this potential.
 */

public Potential copy() {
  
  int i;
  Vector v;
  ListPotential newPot;
  Potential pmt;
  
  v = new Vector();
  
  for (i=0 ; i<list.size() ; i++) {
    pmt = (Potential)list.elementAt(i);
    v.addElement(pmt.copy());
  }
  
  newPot = new ListPotential(v);
  
  return newPot;
}


/**
 * Gets the name of this class.
 * @return the name of this class as a String.
 */

public String getClassName() {
  
  return new String("ListPotential");
}


/**
 * Creates a <code>Potential</code> combining all the potentials in
 * <code>list</code>.
 * @return the result of the combination. If this list do not contain any
 * <code>Potential</code> then return <code>null</code>
 */

public Potential createPotential() {
  
  int i;
  Potential pot;
  
  if (list.size() > 0) {
    pot = getPotentialAt(0);
    for (i=1 ; i<list.size() ; i++)
      pot = pot.combine((Potential)list.elementAt(i));
  }
  else {
    pot = null;
  }
  return pot;
}


/**
 * Creates a <code>Potential</code> combining all the potentials in
 * <code>list</code>.
 * @param limitForPrunning the umbral used to approximate with
 * <code>limitBound(double umbral)</code> after combining two potentials
 * @return the result of the combination. If this list do not contain any
 * <code>Potential</code> then return <code>null</code>
 */

public Potential createPotential(double limitForPrunning) {
  
  int i;
  Potential pot;
  
  if (list.size() > 0) {
    pot = getPotentialAt(0);  
    for (i=1 ; i<list.size() ; i++){
      pot = pot.combine((Potential)list.elementAt(i));
      pot.limitBound(limitForPrunning);
    } 
  }
  else {
    pot = null;
  }
  return pot;
}


/**
 * Creates a <code>Potential</code> combining all the potentials in
 * <code>list</code>.
 * @param limitForPrunning the umbral used to approximate with
 * <code>limitBound(double umbral)</code> after combining two potentials
 * @param limitSum limit sum for pruning.
 * @return the result of the combination. If this list do not contain any
 * <code>Potential</code> then return <code>null</code>
 */

public Potential createPotential(double limitForPrunning,
				 double limitSum) {
  
  int i;
  Potential pot;
  
  if (list.size() > 0) {
    pot = getPotentialAt(0);  
    for (i=1 ; i<list.size() ; i++){
      pot = pot.combine((Potential)list.elementAt(i));
      pot.limitBound(limitForPrunning,limitSum);
    } 
  }
  else {
    pot = null;
  }
  return pot;
}


/**
 * Creates a <code>Potential</code> combining all the potentials in
 * <code>list</code>.
 * @param limitForPrunning the umbral used to approximate with
 * <code>limitBound(double umbral)</code> after combining two potentials
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @return the result of the combination. If this list do not contain any
 * <code>Potential</code> then return <code>null</code>
 */

public Potential createPotential(double limitForPrunning,
                                 int heuristicToJoin) {
  
  Potential pot = null;
  
  if (heuristicToJoin == FIRST_TWO){
    pot = createPotential(limitForPrunning);
  }
  else
    if((heuristicToJoin==MIN_SIZE_VARS) ||
       (heuristicToJoin==MIN_SIZE_VARS_AND_PROD) ||
       (heuristicToJoin==MIN_SIZE_PROD)||
       (heuristicToJoin==MIN_DIFF_SIZE_VARS)||
       (heuristicToJoin==MIN_DIFF_SIZE_PROD)) {
      int i;
      Potential pot1, pot2;
      int[] potToCombine = new int[2];
      Vector listAux;
      
      if (list.size() > 0) {
	if (list.size() > 1) {
	  listAux = (Vector)list.clone();
	  while (listAux.size() > 1) {
	    findTwoPotsToCombine(listAux,potToCombine,heuristicToJoin);
	    pot1 = (Potential)listAux.elementAt(potToCombine[0]);
	    pot2 = (Potential)listAux.elementAt(potToCombine[1]);
	    pot = pot1.combine(pot2);
	    listAux.removeElement(pot1);
	    listAux.removeElement(pot2);
	    listAux.addElement(pot);
	  }
	}
	else
	  pot = getPotentialAt(0);
      }
      else {
	pot = null;
      }
    }
    else {
      System.out.println("Error in ListPotential.createPotential("+
			 "double limitForPrunning): "+
			 "Bad value for ListPotentialheuristicToJoin");
      System.exit(1);
    }

  return pot;
}


/**
 * Creates a <code>Potential</code> combining all the potentials in
 * <code>list</code>.
 * @param limitForPrunning the umbral used to approximate with
 * <code>limitBound(double umbral)</code> after combining two potentials
 * @param limitSum limit sum for pruning.
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @return the result of the combination. If this list do not contain any
 * <code>Potential</code> then return <code>null</code>
 */

public Potential createPotential(double limitForPrunning,
				 double limitSum,
                                 int heuristicToJoin) {
  
  Potential pot = null;
  
  if (heuristicToJoin == FIRST_TWO){
    pot = createPotential(limitForPrunning,limitSum);
  }
  else
    if((heuristicToJoin==MIN_SIZE_VARS) ||
       (heuristicToJoin==MIN_SIZE_VARS_AND_PROD) ||
       (heuristicToJoin==MIN_SIZE_PROD)||
       (heuristicToJoin==MIN_DIFF_SIZE_VARS)||
       (heuristicToJoin==MIN_DIFF_SIZE_PROD)) {
      int i;
      Potential pot1, pot2;
      int[] potToCombine = new int[2];
      Vector listAux;
      
      if (list.size() > 0) {
	if (list.size() > 1) {
	  listAux = (Vector)list.clone();
	  while (listAux.size() > 1) {
	    findTwoPotsToCombine(listAux,potToCombine,heuristicToJoin);
	    pot1 = (Potential)listAux.elementAt(potToCombine[0]);
	    pot2 = (Potential)listAux.elementAt(potToCombine[1]);
	    pot = pot1.combine(pot2);
	    listAux.removeElement(pot1);
	    listAux.removeElement(pot2);
	    listAux.addElement(pot);
	  }
	}
	else
	  pot = getPotentialAt(0);
      }
      else {
	pot = null;
      }
    }
    else {
      System.out.println("Error in ListPotential.createPotential("+
			 "double limitForPrunning): "+
			 "Bad value for ListPotentialheuristicToJoin");
      System.exit(1);
    }

  return pot;
}


/**
 * Looks for two potentials to combine according to
 * a given heuristics.
 * @param listAux a Vector of potentials.
 * @param potToCombine an array that will contain
 * the two potentials found that will be combined,
 * among those in <code>listAux</code>.
 * @param heuristicToJoin the heuristics.
 */

static private void findTwoPotsToCombine(Vector listAux,
					 int[] potToCombine,
					 int heuristicToJoin) {
  
  int i, j, nPot, minElement;
  double minTotal = Double.MAX_VALUE,min = 0;
  int minPot1, minPot2;
  Potential pot1, pot2;
  Vector varTotal;
  SetVectorOperations setV;

  setV = new SetVectorOperations();
  nPot = listAux.size();
  
  for (i=0 ; i<nPot-1 ; i++) {
    for(j=i+1 ; j<nPot ; j++) {
      pot1 = (Potential)listAux.elementAt(i);
      pot2 = (Potential)listAux.elementAt(j);
      if (pot1.getSize() < 0) {
	System.out.println("Error en ListPotentials.findTwoPotentials"+
			   "(): "+
			   "pot1.getSize(Vector listAux,int[] potToCombine)<0");
	pot1.print();
      }
      if (pot2.getSize() < 0) {
	System.out.println("Error en ListPotentials.findTwoPotentials"+
			   "(Vector listAux,int[] potToCombine): "+
			   "pot2.getSize()<0");
	pot2.print();
      }       
      if (heuristicToJoin == MIN_SIZE_VARS) {
        varTotal = setV.union(pot1.getVariables(),pot2.getVariables());
        min = FiniteStates.getSize(varTotal);
      } 
      else if(heuristicToJoin == MIN_SIZE_VARS_AND_PROD) {
        varTotal = setV.union(pot1.getVariables(),pot2.getVariables());
        min = Math.min((double)(pot1.getSize()*pot2.getSize()),
		       FiniteStates.getSize(varTotal));
      }
      else if (heuristicToJoin == MIN_SIZE_PROD) {
        min = (double)(pot1.getSize() * pot2.getSize());
      }
      else if (heuristicToJoin == MIN_DIFF_SIZE_VARS) {
        varTotal = setV.union(pot1.getVariables(),pot2.getVariables());
        min = FiniteStates.getSize(varTotal)-
	      FiniteStates.getSize(pot1.getVariables())-      
	      FiniteStates.getSize(pot2.getVariables());  
      }
      else if (heuristicToJoin == MIN_DIFF_SIZE_PROD) {
        min = (double)(pot1.getSize() * pot2.getSize())-
	      pot1.getSize() - pot2.getSize();
      }
      else {
        System.out.println("Error in ListPotential.findTwoPotsToCombine("+
                           "Vector listAux,int[] potToCombine,"+
                           "int heuristicToJoin): "+
                           "Bad value for heuristicToJoin");
        System.exit(1);
      }
      if (min < minTotal) {
        potToCombine[0] = i;
        potToCombine[1] = j;
        minTotal = min;
      }
    } // end for(j)
  } // end for(i)
}


/**
 * Creates a <code>Potential</code> combining all the potentials in
 * <code>list</code>.
 * @param limitForPrunning the umbral used to approximate with
 * <code>limitBound(double umbral)</code> after combining two potentials
 * @param cache1 a <code>HashMap</code> where the key is a 
 * <code>ListPotential</code> with two <code>Potential</code>s and the
 * value is a <code>Potential</code> corresponding to the combination of
 * the two previous <code>Potential</code>s. This cache is used to save 
 * combinations when they have been previously done,  but combinations are done
 * only with variables.
 * @param cache2 a <code>HashMap</code> similar to cache1 but now combinations
 * are completly done.
 * @param firstTour a <code>boolean</code> that indicates if we are in the
 * first navigation (<code>true</code>) or in the second navigation 
 * (<code>false</code>).
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @return the result of the combination. If this list do not contain any
 * <code>Potential</code> then return <code>null</code>
 */

public Potential createPotential(double limitForPrunning,
				 HashMap cache1,
				 HashMap cache2,
				 boolean firstTour,
				 int heuristicToJoin) {
  
  int i;
  Potential pot, pot1, pot2, potR;
  Vector newList;
  ListPotential pList;
  
  if (heuristicToJoin == FIRST_TWO) {
    pot = createPotential(limitForPrunning,cache1,cache2,firstTour);
  }
  else if ((heuristicToJoin == MIN_SIZE_VARS)||
	   (heuristicToJoin == MIN_DIFF_SIZE_VARS) ) {
    if (getListSize() > 0) {
      if (getListSize() > 1) {
	int[] potToCombine = new int[2];
	
	newList = (Vector)list.clone();
	while (joinTwoPotentials(limitForPrunning,cache1,cache2,
				 newList,firstTour));
	pot = (Potential)newList.elementAt(0);
	while (newList.size() > 1) {
	  findTwoPotsToCombine(newList,potToCombine,heuristicToJoin);
	  pot1 = (Potential)newList.elementAt(potToCombine[0]);
	  pot2 = (Potential)newList.elementAt(potToCombine[1]);
	  
	  pList = new ListPotential(pot1,pot2);
	  if (firstTour == true) { // First tour
	    potR = (Potential)cache1.get(pList);
	    if (potR == null) {
	      potR = pot.makePotential();
	      potR.combine(pot1,pot2,ONLY_VARS); // Combine only variables
	      
	      cache1.put(pList,potR);
	      newList.removeElement(pot1);
	      newList.removeElement(pot2);
	      newList.insertElementAt(potR,0);
	    }
	    else { // This will neve happen
	      System.out.println("It happened!");
	      System.exit(1);
	      cache2.put(pList,null);
	    }
	    pot = potR;
	  }
	  else { // Second tour
	    if (cache2.containsKey(pList)) { 
	      potR = (Potential)cache1.get(pList);
	      if (cache2.get(pList) == null) { // Not combined yet
		potR.combine(pot1,pot2,ONLY_VALUES); // Combine only values
		potR.limitBound(limitForPrunning);
		cache2.put(pList,potR);
		newList.removeElement(pot1);
		newList.removeElement(pot2);
		newList.insertElementAt(potR,0);
	      }
	      pot = potR;
	    }
	    else {
	      potR = (Potential)cache1.get(pList);
	      potR.combine(pot1,pot2,ONLY_VALUES); // Combine only values
	      potR.limitBound(limitForPrunning);
	      pot = potR;
	      cache1.remove(pList);
	      newList.removeElement(pot1);
	      newList.removeElement(pot2);
	      newList.insertElementAt(potR,0);
	    }
	  }
	}
      } // end if(getListSize()>1)
      else {
	pot = getPotentialAt(0);      
      }
    } // end if(getListSize()>0)
    else {
      System.out.println("En createPotential con cache pot==null");
      pot = null;
    }
  } // end if(heuristic == ...
  else {
    System.out.println("Error in ListPotential.createPotential("+
		       "double limitForPrunning,HashMap cache1,"+
		       "HashMap cache2,boolean firstTour,int heuristicToJoin): "+
		       "Invalid heuristicToJoin="+heuristicToJoin);
    System.exit(1);
    pot = null;
  }
  return pot;
}


/**
 * Creates a <code>Potential</code> combining all the potentials in
 * <code>list</code>.
 * @param limitForPrunning the umbral used to approximate with
 * <code>limitBound(double umbral)</code> after combining two potentials
 * @param limitSum limit sum for pruning.
 * @param cache1 a <code>HashMap</code> where the key is a 
 * <code>ListPotential</code> with two <code>Potential</code>s and the
 * value is a <code>Potential</code> corresponding to the combination of
 * the two previous <code>Potential</code>s. This cache is used to save 
 * combinations when they have been previously done,  but combinations are done
 * only with variables.
 * @param cache2 a <code>HashMap</code> similar to cache1 but now combinations
 * are completly done.
 * @param firstTour a <code>boolean</code> that indicates if we are in the
 * first navigation (<code>true</code>) or in the second navigation 
 * (<code>false</code>).
 * @param heuristicToJoin the heuristic to choose two <code>Potential</code>s
 * to be combined
 * @return the result of the combination. If this list do not contain any
 * <code>Potential</code> then return <code>null</code>
 */

public Potential createPotential(double limitForPrunning,
				 double limitSum,
				 HashMap cache1,
				 HashMap cache2,
				 boolean firstTour,
				 int heuristicToJoin) {
  
  int i;
  Potential pot, pot1, pot2, potR;
  Vector newList;
  ListPotential pList;
  
  if (heuristicToJoin == FIRST_TWO) {
    pot = createPotential(limitForPrunning,limitSum,cache1,cache2,
			  firstTour);
  }
  else if ((heuristicToJoin == MIN_SIZE_VARS)||
	   (heuristicToJoin == MIN_DIFF_SIZE_VARS) ) {
    if (getListSize() > 0) {
      if (getListSize() > 1) {
	int[] potToCombine = new int[2];
	
	newList = (Vector)list.clone();
	while (joinTwoPotentials(limitForPrunning,limitSum,
				 cache1,cache2,
				 newList,firstTour));
	pot = (Potential)newList.elementAt(0);
	while (newList.size() > 1) {
	  findTwoPotsToCombine(newList,potToCombine,heuristicToJoin);
	  pot1 = (Potential)newList.elementAt(potToCombine[0]);
	  pot2 = (Potential)newList.elementAt(potToCombine[1]);
	  
	  pList = new ListPotential(pot1,pot2);
	  if (firstTour == true) { // First tour
	    potR = (Potential)cache1.get(pList);
	    if (potR == null) {
	      potR = pot.makePotential();
	      potR.combine(pot1,pot2,ONLY_VARS); // Combine only variables
	      
	      cache1.put(pList,potR);
	      newList.removeElement(pot1);
	      newList.removeElement(pot2);
	      newList.insertElementAt(potR,0);
	    }
	    else { // This will neve happen
	      System.out.println("It happened!");
	      System.exit(1);
	      cache2.put(pList,null);
	    }
	    pot = potR;
	  }
	  else { // Second tour
	    if (cache2.containsKey(pList)) { 
	      potR = (Potential)cache1.get(pList);
	      if (cache2.get(pList) == null) { // Not combined yet
		potR.combine(pot1,pot2,ONLY_VALUES); // Combine only values
		potR.limitBound(limitForPrunning,limitSum);
		cache2.put(pList,potR);
		newList.removeElement(pot1);
		newList.removeElement(pot2);
		newList.insertElementAt(potR,0);
	      }
	      pot = potR;
	    }
	    else {
	      potR = (Potential)cache1.get(pList);
	      potR.combine(pot1,pot2,ONLY_VALUES); // Combine only values
	      potR.limitBound(limitForPrunning,limitSum);
	      pot = potR;
	      cache1.remove(pList);
	      newList.removeElement(pot1);
	      newList.removeElement(pot2);
	      newList.insertElementAt(potR,0);
	    }
	  }
	}
      } // end if(getListSize()>1)
      else {
	pot = getPotentialAt(0);      
      }
    } // end if(getListSize()>0)
    else {
      System.out.println("En createPotential con cache pot==null");
      pot = null;
    }
  } // end if(heuristic == ...
  else {
    System.out.println("Error in ListPotential.createPotential("+
		       "double limitForPrunning,HashMap cache1,"+
		       "HashMap cache2,boolean firstTour,int heuristicToJoin): "+
		       "Invalid heuristicToJoin="+heuristicToJoin);
    System.exit(1);
    pot = null;
  }
  return pot;
}


/**
 * Creates a <code>Potential</code> combining all the potentials in
 * <code>list</code>.
 * @param limitForPrunning the umbral used to approximate with
 * <code>limitBound(double umbral)</code> after combining two potentials
 * @param cache1 a <code>HashMap</code> where the key is a 
 * <code>ListPotential</code> with two <code>Potential</code>s and the
 * value is a <code>Potential</code> corresponding to the combination of
 * the two previous <code>Potential</code>s. This cache is used to save 
 * combinations when they have been previously done,  but combinations are done
 * only with variables.
 * @param cache2 a <code>HashMap</code> similar to cache1 but now combinations
 * are completly done.
 * @param firstTour a <code>boolean</code> that indicates if we are in the
 * first navigation (<code>true</code>) or in the second navigation 
 * (<code>false</code>).

 * @return the result of the combination. If this list do not contain any
 * <code>Potential</code> then return <code>null</code>
 */

public Potential createPotential(double limitForPrunning,
				 HashMap cache1,
				 HashMap cache2,
				 boolean firstTour) {
  
  int i;
  Potential pot, pot2, potR;
  Vector newList;
  ListPotential pList;
  
  if (getListSize() > 0) {
    newList = (Vector)list.clone();
    while (joinTwoPotentials(limitForPrunning,cache1,cache2,newList,firstTour));
    
    pot = (Potential)newList.elementAt(0); 
    
    for (i=1 ; i<newList.size(); i++) {
      pot2 = (Potential)newList.elementAt(i); 
      pList = new ListPotential(pot,pot2);
      if (firstTour == true) { // First tour
	potR = (Potential)cache1.get(pList);
	if (potR == null) {
	  potR = pot.makePotential();
	  potR.combine(pot,pot2,ONLY_VARS); // Combine only variables
	  pot = potR;
	  cache1.put(pList,pot);
	}
	else { // This will never happen
	  System.out.println("It happened!!");
	  cache2.put(pList,null);
	}
	pot = potR;
      }
      else { // Second tour
        if (cache2.containsKey(pList)) { 
          potR = (Potential)cache1.get(pList);
          if (cache2.get(pList) == null) { // Not combined yet
            potR.combine(pot,pot2,ONLY_VALUES); // Combine only values
            potR.limitBound(limitForPrunning);
            cache2.put(pList,potR);
          }
          pot = potR;
        }
        else {
          potR = (Potential)cache1.get(pList);
          potR.combine(pot,pot2,ONLY_VALUES); // Combine only values
          potR.limitBound(limitForPrunning);
          pot = potR;
          cache1.remove(pList);
        }
      }
    }
  }
  else {
    System.out.println("En createPotential con cache pot==null");
    pot = null;
  }
  return pot;
}


/**
 * Creates a <code>Potential</code> combining all the potentials in
 * <code>list</code>.
 * @param limitForPrunning the umbral used to approximate with
 * <code>limitBound(double umbral)</code> after combining two potentials
 * @param limitSum limit sum for pruning.
 * @param cache1 a <code>HashMap</code> where the key is a 
 * <code>ListPotential</code> with two <code>Potential</code>s and the
 * value is a <code>Potential</code> corresponding to the combination of
 * the two previous <code>Potential</code>s. This cache is used to save 
 * combinations when they have been previously done,  but combinations are done
 * only with variables.
 * @param cache2 a <code>HashMap</code> similar to cache1 but now combinations
 * are completly done.
 * @param firstTour a <code>boolean</code> that indicates if we are in the
 * first navigation (<code>true</code>) or in the second navigation 
 * (<code>false</code>).

 * @return the result of the combination. If this list do not contain any
 * <code>Potential</code> then return <code>null</code>
 */

public Potential createPotential(double limitForPrunning,
				 double limitSum,
				 HashMap cache1,
				 HashMap cache2,
				 boolean firstTour) {
  
  int i;
  Potential pot, pot2, potR;
  Vector newList;
  ListPotential pList;
  
  if (getListSize() > 0) {
    newList = (Vector)list.clone();
    while (joinTwoPotentials(limitForPrunning,limitSum,
			     cache1,cache2,newList,firstTour));
    
    pot = (Potential)newList.elementAt(0); 
    
    for (i=1 ; i<newList.size(); i++) {
      pot2 = (Potential)newList.elementAt(i); 
      pList = new ListPotential(pot,pot2);
      if (firstTour == true) { // First tour
	potR = (Potential)cache1.get(pList);
	if (potR == null) {
	  potR = pot.makePotential();
	  potR.combine(pot,pot2,ONLY_VARS); // Combine only variables
	  pot = potR;
	  cache1.put(pList,pot);
/*System.out.println("METO EN CACHE1:");
pList.print();
pot.print();*/
	}
	else { // This will never happen
	  System.out.println("It happened!!");
	  cache2.put(pList,null);
	}
	pot = potR;
      }
      else { // Second tour
        if (cache2.containsKey(pList)) { 
          potR = (Potential)cache1.get(pList);
          if (cache2.get(pList) == null) { // Not combined yet
            potR.combine(pot,pot2,ONLY_VALUES); // Combine only values
            potR.limitBound(limitForPrunning,limitSum);
            cache2.put(pList,potR);
          }
          pot = potR;
        }
        else {
          potR = (Potential)cache1.get(pList);
          potR.combine(pot,pot2,ONLY_VALUES); // Combine only values
          potR.limitBound(limitForPrunning,limitSum);
          pot = potR;
          cache1.remove(pList); // To allow Garbage collector 
                                // recover memory used by pList
        }
      }
    }
  }
  else {
    System.out.println("En createPotential con cache pot==null");
    pot = null;
  }
  return pot;
}


/**
 * Combines all potentials in <code>list</code> containing <code>var</code>
 * Therefore, this <code>ListPotential</code> is modified
 * @param var a <code>Node</code> (variable) of some of the
 * potentials in this <code>ListPotential</code>
 */

public void combinePotentialsOf(Node var) {
  
  Vector potVector;
  ListPotential potList;
  Potential pot;
  int i;
  
  potVector = getPotentialsContaining(var);
  
  if (potVector.size() > 1) {
    potList = new ListPotential(potVector);
    pot = potList.createPotential();
    for (i=0 ; i<potVector.size() ; i++) {
      removePotential(potList.getPotentialAt(i));
    }
    insertPotential(pot);
  }
}


/**
 * Combines all potentials in <code>list</code> containing 
 * the <code>Vector</code> of <code>var</code> variables.
 * Therefore, this <code>ListPotential</code> is modified
 * This method must be only used if this <code>ListPotential</code>
 * contains only potentials of class <code>PotentialMTree</code>
 * @param var a <code>Node</code> (variable) of some of the
 * potentials in this <code>ListPotential</code>
 */

public void combinePotentialsOf(Node var,
				Potential conditioned_pot,
				double limitForPrunning,
				double lowLimitForPrunning,
				int method) {
  
  Vector potVector;
  ListPotential potList;
  Potential pot;
  int i;
  
  potVector = getPotentialsContaining(var);
  
  if (potVector.size() > 1) {
    potList = new ListPotential(potVector);
    pot = potList.createPotential();   
      
    for (i=0 ; i<potVector.size() ; i++) {
      removePotential(potList.getPotentialAt(i));
    }
    if (pot.getClassName().equals("PotentialMTree")) {
      pot = pot.conditional(conditioned_pot);
      pot.conditionalLimitBound(limitForPrunning,lowLimitForPrunning,method);
    }
    else {
      System.out.println("Error in ListPotential.combinePotentialsOf("+
			 "Node var,Potential conditioned_pot,double limitForPrunning," +
			 "double lowLimitForPrunning,int method): " + "this method must"+
			 "be only called with PotentialMTree");
      System.exit(1);
    }
    
    insertPotential(pot);
  }
}


/**
 * Combines all potentials in <code>list</code> containing <code>var</code>
 * Therefore, this <code>ListPotential</code> is modified
 * @param var a <code>Node</code> (variable) of some of the
 * potentials in this <code>ListPotential</code>
 * @param limitForPrunning the limit for prunning after each combination
 */
 
public void combinePotentialsOf(Node var,double limitForPrunning) {
  
  Vector potVector;
  ListPotential potList;
  Potential pot;
  int i;
  
  potVector = getPotentialsContaining(var);
  
  if (potVector.size() > 1) {
    potList = new ListPotential(potVector);
    pot = potList.createPotential(limitForPrunning);
      
    for (i=0 ; i<potVector.size() ; i++) {
      removePotential(potList.getPotentialAt(i));
    }

    insertPotential(pot);
  }
}


/**
 * Combines all potentials in <code>list</code> containing <code>var</code>
 * Therefore, this <code>ListPotential</code> is modified
 * @param var a <code>Node</code> (variable) of some of the
 * potentials in this <code>ListPotential</code>
 * @param limitForPrunning the limit for prunning after each combination
 * @param limitSum limit sum for pruning.
 */
 
public void combinePotentialsOf(Node var,double limitForPrunning,
				double limitSum) {
  
  Vector potVector;
  ListPotential potList;
  Potential pot;
  int i;
  
  potVector = getPotentialsContaining(var);
  
  if (potVector.size() > 1) {
    potList = new ListPotential(potVector);
    pot = potList.createPotential(limitForPrunning,limitSum);
      
    for (i=0 ; i<potVector.size() ; i++) {
      removePotential(potList.getPotentialAt(i));
    }

    insertPotential(pot);
  }
}

public void combinePotentialsOf(Node var,double limitForPrunning,
				double limitSum,
                                HashMap cache1,
                                HashMap cache2,
			        boolean firstTour,
			        int heuristicToJoin) {
  
  Vector potVector;
  ListPotential potList;
  Potential pot;
  int i;
  
  potVector = getPotentialsContaining(var);
  
  if (potVector.size() > 1) {
    potList = new ListPotential(potVector);
    pot = potList.createPotential(limitForPrunning,limitSum,
                          cache1,cache2,firstTour,heuristicToJoin);      
    for (i=0 ; i<potVector.size() ; i++) {
      removePotential(potList.getPotentialAt(i));
    }

    insertPotential(pot);
  }


}



/**
 * Combines every two potentials pot1 and pot2 in this
 * <code>ListPotential</code> verifying
 * pot1.getSize()+pot2.getSize() >= 0.5*FiniteStates.getSize(varTotal);
 * After each combination it applies a <code>limitBound(limitForPrunning)</code>
 * to the output of combination.
 * This method modifies this <code>ListPotential</code>
 * @param limitForPrunning the limit to apply 
 * <code>limitBound(limitForPrunning)</code>
 */

public void joinPotentials(double limitForPrunning) {
  
  while (joinTwoPotentials(limitForPrunning) == true);
}


/**
 * Combines every two potentials pot1 and pot2 in this
 * <code>ListPotential</code> verifying
 * pot1.getSize()+pot2.getSize() >= 0.5*FiniteStates.getSize(varTotal);
 * After each combination it applies a <code>limitBound(limitForPrunning)</code>
 * to the output of combination.
 * This method modifies this <code>ListPotential</code>
 * @param limitForPrunning the limit to apply 
 * <code>limitBound(limitForPrunning)</code>
 * @param limitSum limit sum for pruning.
 */

public void joinPotentials(double limitForPrunning,
			   double limitSum) {
  
  while (joinTwoPotentials(limitForPrunning,limitSum) == true);
}


/** 
 * Combines two potentials pot1 and pot2 in this
 * <code>ListPotential</code> verifying
 * pot1.getSize()+pot2.getSize() >= 0.5*FiniteStates.getSize(varTotal);
 * inserting the output into this <code>ListPotential</code> and removing
 * pot1 and pot2 from this <code>ListPotential</code>.
 * After each combination it applies a <code>limitBound(limitForPrunning)</code>
 * to the output of combination.
 * This method modifies this <code>ListPotential</code>
 * @param limitForPrunning the limit to apply 
 * <code>limitBound(limitForPrunning)</code>
 * @return <code>true</code> if two potentials have been combined; 
 * <code>false</code> if no potentials have been combined.
 */

private boolean joinTwoPotentials(double limitForPrunning) {
  
  int i, j, nPot;
  Potential pot1, pot2, pot;
  Vector varTotal;
  SetVectorOperations setV;
  
  setV = new SetVectorOperations();
  nPot = getListSize();
  for (i=0 ; i<nPot-1 ; i++) {
    for(j=i+1 ; j<nPot ; j++) {
      pot1 = getPotentialAt(i);
      pot2 = getPotentialAt(j);
      varTotal = setV.union(pot1.getVariables(),pot2.getVariables());
      if (pot1.getSize() < 0) {
	System.out.println("Error en ListPotentials.joinTwoPotentials"+
			   "(double limitForPrunning): "+"pot1.getSize()<0");
	print();
      }
      if (pot2.getSize() < 0) {
	System.out.println("Error en ListPotentials.joinTwoPotentials"+
			   "(double limitForPrunning): "+"pot2.getSize()<0");
	print();
      }
      if (pot1.getSize()+pot2.getSize() >= 0.5*FiniteStates.getSize(varTotal)) {
        pot = pot1.combine(pot2);
        pot.limitBound(limitForPrunning);
        removePotential(pot1);
        removePotential(pot2);   
        insertPotential(pot);
        return true;
      }
    }
  }
  return false;
}


/** 
 * Combines two potentials pot1 and pot2 in this
 * <code>ListPotential</code> verifying
 * pot1.getSize()+pot2.getSize() >= 0.5*FiniteStates.getSize(varTotal);
 * inserting the output into this <code>ListPotential</code> and removing
 * pot1 and pot2 from this <code>ListPotential</code>.
 * After each combination it applies a <code>limitBound(limitForPrunning)</code>
 * to the output of combination.
 * This method modifies this <code>ListPotential</code>
 * @param limitForPrunning the limit to apply 
 * <code>limitBound(limitForPrunning)</code>
 * @param limitSum limit sum for pruning.
 * @return <code>true</code> if two potentials have been combined; 
 * <code>false</code> if no potentials have been combined.
 */

private boolean joinTwoPotentials(double limitForPrunning,
				  double limitSum) {
  
  int i, j, nPot;
  Potential pot1, pot2, pot;
  Vector varTotal;
  SetVectorOperations setV;
  
  setV = new SetVectorOperations();
  nPot = getListSize();
  for (i=0 ; i<nPot-1 ; i++) {
    for(j=i+1 ; j<nPot ; j++) {
      pot1 = getPotentialAt(i);
      pot2 = getPotentialAt(j);
      varTotal = setV.union(pot1.getVariables(),pot2.getVariables());
      if (pot1.getSize() < 0) {
	System.out.println("Error en ListPotentials.joinTwoPotentials"+
			   "(double limitForPrunning): "+"pot1.getSize()<0");
	print();
      }
      if (pot2.getSize() < 0) {
	System.out.println("Error en ListPotentials.joinTwoPotentials"+
			   "(double limitForPrunning): "+"pot2.getSize()<0");
	print();
      }
      if (pot1.getSize()+pot2.getSize() >= 0.5*FiniteStates.getSize(varTotal)) {
        pot = pot1.combine(pot2);
        pot.limitBound(limitForPrunning,limitSum);
        removePotential(pot1);
        removePotential(pot2);   
        insertPotential(pot);
        return true;
      }
    }
  }
  return false;
}


/** 
 * Searches the two first potentials pot1 and pot2 in the <code>Vector</code> of
 * <code>Potential</code>s <code>newList</code> that are also into the 
 * <code>HashMap cache</code>. This <code>HashMap cache</code> must contain
 * a set of pairs <code>ListPotential</code> (with two <code>Potential</code>s)
 * and a <code>Potential</code> (result of combination of the two
 * <code>Potential</code>s).
 * Two <code>Potential</code>s are in 
 * <code>cache</code> when they have been previously combined.
 * When two <code>Potential</code> are found then they are remove from 
 * <code>newList</code> and the result of their combination (got from the
 * <code>HashMap cache</code>) is added to <code>newList</code>.
 * This method modifies <code>newList</code>
 * @param newList a <code>Vector</code> of <code>Potentials</code>
 * @param cache a <code>HashMap</code> where the key is a 
 * <code>ListPotential</code> with two <code>Potential</code>s and the
 * value is a <code>Potential</code> corresponding to the combination of
 * the two previous <code>Potential</code>s. This cache is used to save 
 * combinations when they have been previously done.
 * @return <code>true</code> if two potentials have been found; 
 * <code>false</code> if no potentials have been found.
 */

private boolean joinTwoPotentials(double limitForPrunning,
				  HashMap cache1,
				  HashMap cache2,
				  Vector newList,
				  boolean firstTour) {
  
  int i, j, nPot;
  Potential pot1, pot2, potR;
  ListPotential pList;
  
  nPot = newList.size();
  
  for (i=0 ; i<nPot-1 ; i++) {
    for (j=i+1 ; j<nPot ; j++) {
      pot1 = (Potential)newList.elementAt(i);
      pot2 = (Potential)newList.elementAt(j);
      
      pList = new ListPotential(pot1,pot2); 
      if (cache1.containsKey(pList)) { //if pList is in cache1
        if (firstTour == true) {
          potR = (Potential)cache1.get(pList);
          if (!cache2.containsKey(pList)) {//if this combination wasn't in cache2
            cache2.put(pList,null);
          }
          newList.removeElement(pot1);
          newList.removeElement(pot2); 
          newList.insertElementAt(potR,0);
          return true;
        }
        else {
          potR = (Potential)cache2.get(pList);
          if (potR != null) {
            newList.removeElement(pot1);
            newList.removeElement(pot2); 
            newList.insertElementAt(potR,0);
            return true;
          }
        }
      }  // end if(cache1.containsKey(pList))  
    }  // end for(j)
  } // end for(i)
  return false;
}


/** 
 * Searches the two first potentials pot1 and pot2 in the <code>Vector</code> of
 * <code>Potential</code>s <code>newList</code> that are also into the 
 * <code>HashMap cache</code>. This <code>HashMap cache</code> must contain
 * a set of pairs <code>ListPotential</code> (with two <code>Potential</code>s)
 * and a <code>Potential</code> (result of combination of the two
 * <code>Potential</code>s).
 * Two <code>Potential</code>s are in 
 * <code>cache</code> when they have been previously combined.
 * When two <code>Potential</code> are found then they are remove from 
 * <code>newList</code> and the result of their combination (got from the
 * <code>HashMap cache</code>) is added to <code>newList</code>.
 * This method modifies <code>newList</code>
 * @param limitSum limit sum for pruning.
 * @param newList a <code>Vector</code> of <code>Potentials</code>
 * @param cache a <code>HashMap</code> where the key is a 
 * <code>ListPotential</code> with two <code>Potential</code>s and the
 * value is a <code>Potential</code> corresponding to the combination of
 * the two previous <code>Potential</code>s. This cache is used to save 
 * combinations when they have been previously done.
 * @return <code>true</code> if two potentials have been found; 
 * <code>false</code> if no potentials have been found.
 */

private boolean joinTwoPotentials(double limitForPrunning,
				  double limitSum,
				  HashMap cache1,
				  HashMap cache2,
				  Vector newList,
				  boolean firstTour) {
  
  int i, j, nPot;
  Potential pot1, pot2, potR;
  ListPotential pList;
  
  nPot = newList.size();
  
  for (i=0 ; i<nPot-1 ; i++) {
    for (j=i+1 ; j<nPot ; j++) {
      pot1 = (Potential)newList.elementAt(i);
      pot2 = (Potential)newList.elementAt(j);
      
      pList = new ListPotential(pot1,pot2); 
      if (cache1.containsKey(pList)) { //if pList is in cache1
        if (firstTour == true) {
          potR = (Potential)cache1.get(pList);
          if (!cache2.containsKey(pList)) {//if this combination wasn't in cache2
            cache2.put(pList,null);
          }
          newList.removeElement(pot1);
          newList.removeElement(pot2); 
          newList.insertElementAt(potR,0);
          return true;
        }
        else {
          potR = (Potential)cache2.get(pList);
          if (potR != null) {
            newList.removeElement(pot1);
            newList.removeElement(pot2); 
            newList.insertElementAt(potR,0);
            return true;
          }
        }
      }  // end if(cache1.containsKey(pList))  
    }  // end for(j)
  } // end for(i)
  return false;
}


/**
 * Normalizes this potential to sum up to one. It combines all potentials
 * in this <code>ListPotential</code> and then calls to <code>normalize()</code>
 * of the corresponding <code>Potential</code>.
 * @param pot a <code>ListPotential</code>
 * @return a new normalized <code>Potential</code>
 */

public Potential normalize(Potential pot) {
  Potential pt;

  pt=((ListPotential)pot).createPotential();
  pt.normalize();
  return pt;
}


/* NOT IMPLEMENTED METHODS (just reported here to keep compatibility
   with abstract class Potential) */


/**
 * NOT IMPLEMENTED.
 */

public double totalPotential() {
  
  System.out.println("NOT IMPLEMENTED");
  return -1.0;
}


/**
 * NOT IMPLEMENTED.
 */

public double totalPotential(Configuration conf) {
  
  System.out.println("NOT IMPLEMENTED");
  return -1.0;
}


/**
 * NOT IMPLEMENTED.
 */

public double entropyPotential() {
  
  System.out.println("NOT IMPLEMENTED");
  return -1.0;
}


/**
 * NOT IMPLEMENTED.
 */

public double entropyPotential(Configuration conf) {
  
  System.out.println("NOT IMPLEMENTED");
  return -1.0;
}


/**
 * NOT IMPLEMENTED.
 */

public void normalize() {
  System.out.println("normalize() NOT IMPLEMENTED in ListPotential.java");

}


/**
 * NOT IMPLEMENTED.
 */

public void setValue(Configuration conf, double val) {
  
  System.out.println("NOT IMPLEMENTED");
}

/**
 * Prints this Potential to the standard output.
 */

public void print() {
  super.print();
  int i;
  
  for(i=0;i<getListSize();i++) {
    ((Potential)list.elementAt(i)).print();
  }
}

} // End of class 
