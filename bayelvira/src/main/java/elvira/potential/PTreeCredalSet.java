/*
 * PotentialTreeCredalSet.java
 *
 * Created on 19 de abril de 2004, 13:27
 */

package elvira.potential;

import java.util.Vector;
import elvira.*;

import elvira.parser.*;
import java.io.*;

/**
 * This class represents a Credal Set using a ProbabilityTree
 * @author  Andr�s Cano (acu@decsai.ugr.es)
 */
public class PTreeCredalSet extends elvira.potential.PotentialTree implements CredalSet{
  
  /** Creates a new instance of PotentialTreeCredalSet */
  public PTreeCredalSet() {
  }
  
  /**
   * Creates a new instance of PotentialTreeCredalSet from a PotentialInterval
   * @param intervalSet a PotentialInterval
   */
  public PTreeCredalSet(PotentialInterval intervalSet){
    super(intervalSet.getVariables());
    ProbabilityTree pTree;
    pTree=intervalsToPTreeCredalSet(intervalSet);
    setTree(pTree);
  }
  
   /**
   * Creates a new instance of PotentialTreeCredalSet from a PotentialConvexSet. It 
   * append a new transparent variable in the root of this tree with as many child as
   * the number of extreme points in the PotentialConvexSet pot.
   * @param pot the PotentialConvexSet to be converted to PTreeCredalSet
   */
    public PTreeCredalSet(PotentialTable pot) {
        //super(pot.getVariables());
        super(pot.getListNonTransparents());
        Vector vTrans;
        Configuration confTrans;
        int nextremes;
        PotentialTable potrestricted;
        PotentialTree newpot;
        ProbabilityTree probTree=null;

        vTrans = pot.getListTransparents();
        confTrans = new Configuration(vTrans);
        nextremes = confTrans.possibleValues();

        if (nextremes > 1) {
            FiniteStates newTrans;
 
            probTree = new ProbabilityTree();
            newTrans = appendTransparentVariable(nextremes);
            probTree.assignVar(newTrans);
            for (int i = 0; i < nextremes; i++) {
                potrestricted = (PotentialTable) pot.restrictVariable(confTrans);
                newpot = potrestricted.toTree();
                probTree.replaceChild(newpot.getTree(), i);
                confTrans.nextConfiguration();
            }
            addVariable(newTrans);
            
        }
        else if(nextremes==1){
            potrestricted = (PotentialTable) pot.restrictVariable(confTrans);
            newpot = potrestricted.toTree();
            probTree=newpot.getTree();
        }
        setTree(probTree);

    }
  
  /**
   * Creates a new <code>PTreeCredalSet</code> for a given list of variables.
   * The probabilities will be initialized to 0.
   * @param vars a <code>Vector</code> of variables (<code>FiniteStates</code>)
   * that this potential will contain.
   */
  public PTreeCredalSet(Vector vars) {
    variables = (Vector)vars.clone();
    values = new ProbabilityTree(0);
    size = 1;
  }

  /**
    * Makes a new instance of a PTreeCredalSet
    * @param vars list of variables of the new PTreeCredalSet
    * @return a new instance of a PTreeCredalSet
    */
   public PTreeCredalSet getInstance(Vector vars){
       return new PTreeCredalSet(vars);
   }
  
  /**
   * Convert a PotentialInterval intervalSet P(Y|X_I) into a Credal Set using a PTreeCredalSet.
   * Variables in X_I are put in the upper levels of the resulting ProbabilityTree
   * Then, new transparent variables are added, and finally variable Y is put in
   * the botton level of the resulting ProbabilityTree.
   * It is supposed that variable Y is the first variable in the list of variables of
   * intervalSet.
   * @see void intervalsToPTreeCredalSet(PotentialInterval intervalSet,
   * ProbabilityTree probTree, Configuration conf,int nvar)
   * @param intervalSet the PotentialInterval to be converted
   * @return a ProbabilyTree resulting from the conversion of this PotentialInterval into
   * the extrem points
   */
  private ProbabilityTree intervalsToPTreeCredalSet(PotentialInterval intervalSet){
    ProbabilityTree probTree=new ProbabilityTree();
    Configuration conf=new Configuration();
    intervalsToPTreeCredalSet(intervalSet,probTree,conf,1);
    return probTree;
  }
  
  /**
   * Convert a PotentialInterval intervalSet P(Y|X_I) into a Credal Set using a PTreeCredalSet.
   * The result of conversion is saved in probTree. Variables in X_I are put in the upper levels
   * of probTree. Then, new transparent variables are put, and finally variable Y is put in
   * the botton level of probTree.
   * This is a recursive function used by
   * <code>ProbabilityTree intervalsToPTCredalSet(PotentialInterval intervalSet)</code>
   * It is supposed that variable Y is the first variable in the list of variables of
   * intervalSet.
   * @see ProbabilityTree intervalsToPTCredalSet(PotentialInterval intervalSet)
   * @param intervalSet the PotentialInterval to be converted
   * @param probTree the ProbabilityTree from the conversion
   * @param conf a configuration of variables in intervalSet, that have been yet inserted in probTree.
   * It is modified in the recursive calls. Users must call this method using a new Configuration
   * @param nvar number of variable in intervalSet to be inserted in probTree.
   * It is modified in the recursive calls. Users must call this method using 1
   */
  private void intervalsToPTreeCredalSet(PotentialInterval intervalSet,ProbabilityTree probTree,
  Configuration conf,int nvar){
    Vector vars=intervalSet.getVariables();
    
    if(nvar<vars.size()){ // make the top part of the tree with variables in X_I
      FiniteStates currentVar=(FiniteStates)vars.elementAt(nvar);
      int ncases=currentVar.getNumStates();
      probTree.assignVar(currentVar);
      conf.insert(currentVar, 0);
      for(int i=0;i<ncases;i++){
        conf.putValue(currentVar,i);
        intervalsToPTreeCredalSet(intervalSet,probTree.getChild(i),conf,nvar+1);
      }
    } // end if
    else{
      FiniteStates conditionalVar=(FiniteStates)(intervalSet.getVariables().elementAt(0));
      PotentialInterval intervalSetRestricted = (PotentialInterval)intervalSet.restrictVariable(conf);
      Vector extremePoints=intervalSetRestricted.getListExtrems();
      int nExtrems=extremePoints.size();
      FiniteStates transp=appendTransparentVariable(nExtrems);
      int ncases=conditionalVar.getNumStates();
      
      probTree.assignVar(transp);
      for(int i=0;i<nExtrems;i++){
        double array[]=(double [])(extremePoints.elementAt(i));
        probTree.getChild(i).assignVar(conditionalVar);
        for(int j=0;j<ncases;j++){
          probTree.getChild(i).getChild(j).assignProb(array[j]);
        }
      }
    } // end else
  }
  
  /**
   * Copies this potential.
   * @return a copy of this <code>PTreeCredalSet</code>.
   */
  public Potential copy() {
    PTreeCredalSet pot;
    
    pot = new PTreeCredalSet(variables);
    pot.size = size;
    pot.values = values.copy();
    
    return pot;
  }
  
  /**
   * This method incorporates the evidence passed as argument to the
   * potential, that is, puts to 0 all the values whose configurations
   * are not consistent with the evidence.
   * 
   * The method works as follows: for each observed variable a
   * probability tree is built with 1.0 as value for the observed
   * state and 0.0 for the rest. Then the tree is combined with
   * this new tree, and the result is a new tree with the evidence
   * entered.
   * @param evid The Evidence to be incorporated in this PTreeCredalSet
   */
  public void instantiateEvidence(Configuration evid) {
    ProbabilityTree tree, twig;
    Configuration conf;
    PTreeCredalSet pot, pot2;
    FiniteStates variable;
    int i, j, v;
    
    conf = new Configuration(evid,new NodeList(variables));
    if (conf.size() != 0) {
      pot = (PTreeCredalSet)copy();
      for (i=0 ; i<conf.size() ; i++) {
        variable = conf.getVariable(i);
        v = conf.getValue(i);
        
        // building a tree for variable
        tree = new ProbabilityTree(variable);
        for (j=0 ; j<tree.child.size() ; j++) {
          twig = (ProbabilityTree) tree.child.elementAt(j);
          twig.label = 2;
          if (j == v)
            twig.value = 1.0;
          tree.leaves++;
        }
        // building the potential for the variable
        pot2 = new PTreeCredalSet();
        pot2.variables.addElement(variable);
        pot2.setTree(tree);
        // combination
        
        pot = (PTreeCredalSet)pot.combine(pot2);
      }
      this.setTree(pot.getTree());
    }
  }
  
  public static void main(String args[]){
    Network b=null;
    
    if (args.length < 1)
      System.out.println("Too few arguments. Arguments are: ElviraFile [OutputElviraFile]");
    else {
      try{
        b = Network.read(args[0]);
      }
      catch(ParseException e){
        System.out.println(e);
        System.exit(1);
      }
      catch(IOException e){
        System.out.println(e);
        System.exit(1);
      }
    } // end else
    Vector rList=b.getRelationList();
    Potential pot;
    Potential newPot;
    Relation rel;
    for(int i=0;i<rList.size();i++){
      rel=(Relation)rList.elementAt(i);
      pot=(Potential)(rel.getValues());
      if(pot instanceof PotentialIntervalTable){
        newPot=new PTreeCredalSet((PotentialIntervalTable)pot);
      }
      else{
        newPot=pot;
      }
      rel.setValues(newPot);
    }
    try{
      if (args.length == 2) {
        b.save(args[1]);
      }
    }
    catch(IOException e){
      System.out.println(e);
      System.exit(1);
    }
  }
  
  /**
   * Saves this potential to a file. The values are written as a convex-set-tree 
   * @param p the <code>PrintWriter</code> where the potential will be written.
   */
  
  public void save(PrintWriter p) {
    Vector listTrans;
    Vector varsSinTrans=new Vector();
    Vector varsConTrans=new Vector();
    FiniteStates mainVar=(FiniteStates)variables.elementAt(0);
    FiniteStates var;
    FiniteStates transVar;
    Configuration indexingVars, confComplete;
    int whites;
    int i,j,k,l;
    
    p.print("values= credal-set-tree (\n");   
    listTrans=getListTransparents();

    // Create a configuration removing transparent variables and mainVar
    for(k=1; k < variables.size(); k++){
      var=(FiniteStates)variables.elementAt(k);

      // If this variable is in listTrans, do not consider
      if (listTrans.contains(var) == false){
        varsSinTrans.addElement(var);
      }
    }
    
    // Create the configuration 
    indexingVars=new Configuration(varsSinTrans);
    confComplete=new Configuration(variables);

    // Call the recursive method responsible for printing the branches
    // of the tree
    printBranch(indexingVars, confComplete, listTrans, mainVar,2,p);
    p.print(");\n");
  }

  /**
   * Auxiliar method to print the tree-like branch relative to a given
   * configuration
   * @param indexingVars Configuration with the variables of the branch
   * @param confComplete Configuration with the whole set of variables
   * @param listTrans Vector with all the transparent variables
   * @param mainVar FiniteStates owning the convex set tree to print
   * @param tabs number of tabs spaces to print to separate the
   *        different nodes of the tree
   * @param p printWriter where to print
   */

  private void printBranch(Configuration indexingVars, Configuration confComplete, 
                           Vector listTrans, FiniteStates mainVar, int tabs, 
                           PrintWriter p){
    Configuration restOfVariables=null;
    Configuration parents=null;
    Vector parentVars=new Vector();
    int i,j,k;

    // First at all, test if confSinTrans is null. In this case, it is time
    // to print the values. The values will be contained between "{" and "}"
    if (indexingVars.getVariables().size() == 0){
      // In this case, get the transparent related to the configuration
      // for non transparent variables. From confComplete form a new
      // configuration with all the non transparent variables. Remember
      // that confSinTrans does not contain all the non transparent
      // variables, but those that are not considered yet

      for(i=0; i < confComplete.getVariables().size(); i++){
        FiniteStates var=(FiniteStates)confComplete.getVariables().elementAt(i);

        if (listTrans.contains(var) == false && var != mainVar){
          parentVars.addElement(var);
        }
      }
      parents=new Configuration(parentVars);

      // Reset the configuration with the values in confComplete
      parents.resetConfiguration(confComplete);
      
      // Get the index of the concrete values for confSinTrans respect the
      // whole set of possible values
      int index=parents.getIndexInTable();

      // Get the transparent variable related to this configuration
      FiniteStates transVar=(FiniteStates)listTrans.elementAt(index);

      // We have to write as much tables as the number of values for
      // the transparent variable. Print the "{". Before printing "{"
      // print the right number of tabs
      for(k=0; k < tabs+3; k++)
        p.print("  ");
      p.println("{");

      // Print the extreme points for the values of the transparent
      // variable 
      for(i=0; i < transVar.getNumStates(); i++){
        // Print the required number of spaces
        for(k=0; k < tabs+3; k++)
            p.print("  ");

        // Complete the configuration to get the desired values
        confComplete.putValue(transVar,i);

        // Print the word table
        p.print(" table (");

        // Consider the values for the mainVar
        for(j=0; j < mainVar.getNumStates(); j++){
          confComplete.putValue(mainVar.getName(),j);

          // Get the value and print it
          double value=getValue(confComplete);
          p.print(value);
          if (j != mainVar.getNumStates()-1)
            p.print(", ");
        }

        // This table is finished
        p.println(")");
      }

      // One all the tables are printed, print "}"
      for(k=0; k < tabs+3; k++)
        p.print("  ");
      p.println("}");
    }
    else{
      // Consider a new variable. We must go over its states
      FiniteStates varConsidered=(FiniteStates)indexingVars.getVariables().elementAt(0);

      // Add a new tab separation
      tabs++;

      // We must consider all the values for this variable
      for(i=0; i < varConsidered.getNumStates(); i++){

        // Make a new configuration removing this variable
        restOfVariables=new Configuration(indexingVars.getVariables(),varConsidered.getName());

        // Print the spaces
        for(j=0; j < tabs; j++)
          p.print("  ");

        // For the first value, print "case nameOfVar {"
        if (i == 0){
          p.println("case "+varConsidered.getName()+"{");
          // Add a tab separation
          tabs++;

          // Add new tabs for the first value
          for(j=0; j < tabs; j++)
            p.print("  ");
        }

        // Print the name of the state
        p.println(varConsidered.getPrintableState(i)+ " = ");

        // Set this value for confComplete
        confComplete.putValue(varConsidered.getName(),i);

        // New call to the method
        printBranch(restOfVariables,confComplete,listTrans,mainVar,tabs,p);
        
        // If it is the last value, then close the "}"
        if (i == varConsidered.getNumStates()-1){
          // Decrease the number of whites
          tabs-=1;
          // Add a tab separation
          for(j=0; j < tabs; j++)
            p.print("  ");
          p.println("}");

        }
      }
    }
  }
  
  /**
   * This method divides two potentials.
   * For the exception 0/0, the method computes the result as 0.
   * The exception ?/0: the method aborts with a message in the standar output.
   * @param p the <code>PotentialTree</code> to divide with this.
   * @return a new <code>PotentialTree</code> with the result of
   * dividing this potential by <code>p</code>.
   */
  public Potential divide(Potential p) {
    Vector v, v1, v2;
    FiniteStates aux;
    int i, nv;
    PotentialTree pot;
    double x;
    ProbabilityTree tree, tree1, tree2;
    
    v1 = variables;   // Variables of this potential.
    v2 = p.variables; // Variables of the argument.
    v = new Vector(); // Variables of the new potential.
    
    for (i=0 ; i<v1.size() ; i++) {
      aux = (FiniteStates)v1.elementAt(i);
      v.addElement(aux);
    }
    
    for (i=0 ; i<v2.size() ; i++) {
      aux = (FiniteStates)v2.elementAt(i);
      if (aux.indexOf(v1) == -1)
        v.addElement(aux);
    }
    
    // The new Potential.
    pot = new PTreeCredalSet(v);
    
    tree1 = getTree();                          // Tree of this potential.
    tree2 = ((PotentialTree)p).getTree();       // Tree of the argument.
    
    tree = ProbabilityTree.divide(tree1,tree2); // The new tree.
    
    pot.setTree(tree);
    
    return pot;
  }
  
  /**
   * Combines this potential with the Potential p. The argument <code>p</code>
   * must be a <code>PTreeCredalSet</code>.
   * @returns a new <code>PTreeCredalSet</code> consisting of the combination
   * of <code>p</code> and this <code>Potential</code>.
   * @param p a PTreeCredalSet
   * @return A new PTreeCredalSet with the combination of this PTreeCredalSet and the
   * argument p
   */
  public Potential combine(Potential p) {
    Vector v, v1, v2;
    FiniteStates aux;
    int i, nv;
    PotentialTree pot;
    double x;
    ProbabilityTree tree, tree1, tree2;
    
    if (p instanceof elvira.potential.PTreeCredalSet) {
      v1 = variables;   // Variables of this potential.
      v2 = p.variables; // Variables of the argument.
      v = new Vector(); // Variables of the new potential.
      
      for (i=0 ; i<v1.size() ; i++) {
        aux = (FiniteStates)v1.elementAt(i);
        v.addElement(aux);
      }
      for (i=0 ; i<v2.size() ; i++) {
        aux = (FiniteStates)v2.elementAt(i);
        if (aux.indexOf(v1) == -1)
          v.addElement(aux);
      }
      // The new Potential.
      pot = new PTreeCredalSet(v);
      tree1 = getTree();                           // Tree of this potential.
      tree2 = ((PTreeCredalSet)p).getTree();        // Tree of the argument.
      tree = ProbabilityTree.combine(tree1,tree2); // The new tree.
      pot.setTree(tree);
    }
    else {
      System.out.println("Error in PTreeCredalSet.combine(Potential p): argument p was not a PTreeCredalSet");
      System.exit(1);
      pot = this;
    }
    return pot;
  }
  
  /**
   * Removes the argument variable summing over all its values.
   * @param var a <code>FiniteStates</code> variable.
   * @return a new <code>PTreeCredalSet</code> with the result of the deletion.
   */
  
  public Potential addVariable(Node var) {
    Vector v;
    PTreeCredalSet pot;
    
    v = new Vector();
    v.addElement(var);
    pot = (PTreeCredalSet)addVariable(v);
    return pot;
  }
  
  /**
   * Removes a list of variables by adding over all their states.
   * @param vars <code>Vector</code> of <code>FiniteStates</code> variables
   * to be removed from this potential.
   * @return A new <code>PTreeCredalSet</code> with the result of the operation.
   */
  
  public PotentialTree addVariable(Vector vars) {
    Vector aux;
    FiniteStates var1, var2;
    int i, j;
    boolean found;
    PTreeCredalSet pot;
    ProbabilityTree tree;
    
    aux = new Vector(); // New list of variables.
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
        aux.addElement(var1);
    }
    pot = new PTreeCredalSet(aux); // The new tree.
    tree = values;
    for (i=0 ; i<vars.size() ; i++) {
      var1 = (FiniteStates)vars.elementAt(i);
      tree = tree.addVariable(var1);
    }
    pot.setTree(tree);
    return pot;
  }

  /**
   * Method to add new values to the PTreeCredalSet. Every time this method is
   * called a new transparent variable is added to the PTreeCredalSet
   * @param potentials Vector containing the potentials storing the values
   * @param pTree ProbabilityTree place where the values will be stored
   */
  public void addValues(Vector potentials, ProbabilityTree pTree){
    FiniteStates transVar;
    ProbabilityTree pNewTree,finalTree;
    FiniteStates mainVar=(FiniteStates)getVariables().elementAt(0); 
    Vector vars;
    Configuration conf;
    Potential pot;
    int i,j;
    
    // First at all, add a new transparent variable to the object. The number
    // of values for this transparent variable will be the same as the size
    // of potentials vector. Every potential will be used for a value of the
    // transparent variable
    transVar=appendTransparentVariable(potentials.size());

    // This variable must be assignet to the probabilityTree
    pTree.assignVar(transVar);

    // Consider one by one the branches for this new variable
    for(i=0; i < potentials.size(); i++){

      // Get the tree related to this value of the transparent variable
      pNewTree=pTree.getChild(i);

      // In this branch we must begin adding the variable owning the relation
      // which values are under consideration
      pNewTree.assignVar(mainVar);

      // Get the potential for this value of the transparent var
      pot=(Potential)potentials.elementAt(i);

      // Make a configuration to get the values of the potential
      vars=new Vector();
      vars.addElement(mainVar);
      conf=new Configuration(vars);

      // Assign the extreme points
      for(j=0; j < mainVar.getNumStates(); j++){
        // Get the tree where the value must be assigned
        finalTree=pNewTree.getChild(j);
        finalTree.assignProb(pot.getValue(conf));
        conf.nextConfiguration();
      }
    }
  }

 /**
  * Method to remove from the potentials the variables which does not
  * take part into the ProbabilityTree used to store the values
  */
  public void removeTransNotInTree(){
    // Get the list of transparents which take part into the tree
    Vector transparents=values.getListTransparents();
    Vector<FiniteStates> varsToRemove = new Vector<FiniteStates>();

    // Now remove the transparent in the PotentialTree if they do not
    // appear in the tree
    // Go on the set of variables, one by one
    for(int i=0; i < variables.size(); i++){
      FiniteStates var=(FiniteStates)variables.elementAt(i);

      // If transparent, consider if it must be removed
      if (var.getTransparency() == FiniteStates.TRANSPARENT){
        // Check if it is present in transparents
        for(int j=0; j < transparents.size(); j++){
          if (transparents.contains(var) == false)
              varsToRemove.addElement(var);
            
        }
      }
    }
    for(int i=0;i<varsToRemove.size();i++){
        variables.remove(varsToRemove.elementAt(i)); 
    }
  }
}
