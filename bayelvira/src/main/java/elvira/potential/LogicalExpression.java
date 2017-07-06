/* Crono.java */

package elvira.potential;


import java.io.*;
import java.util.Vector;
import elvira.parser.*;
import elvira.Network;
import elvira.IDiagram;
import elvira.LogicalNode;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Node;
import elvira.RelationList;
import elvira.Relation;
import elvira.potential.PotentialTable;

/**
 * Implements a logical expression antecedent -> consecuent
 * Operations:
 * <ul>
 * <li> Constructor
 * @author Manuel Gomez
 * @since 7/05/2002
 */

public class LogicalExpression extends Potential{

/**
 * To store the consecuent and antecedent
 */
private LogicalNode consecuent;
private LogicalNode antecedent;

/**
 * To store the relation that exists between
 * antecedent and consecuent
 */
private int operator;

/**
 * To index the location of variables in expression
 */
private Vector index;

/**
 * Flag to remember the previous indexations
 */
private boolean indexed;

/**
 * To store the result of the evaluation, for the whole
 * set of values for the variables
 */
private PotentialTree result;

/**
 * Constructor
 */

public LogicalExpression(){
  indexed=false;
}

/**
 * Constructor of the class
 * @param <code>LogicalNode</code> antecedent
 * @param <code>LogicalNode</code> consecuent
 * @param kind of relation between antecedent and consecuent
 */

public LogicalExpression(LogicalNode antecedent, LogicalNode consecuent,
                         int operator) {
  this.antecedent=antecedent;
  this.consecuent=consecuent;
  this.operator=operator;

  // To remember the variables are not indexed

  indexed=false;
}

/**
 * Method to copy a logical expression
 * @return <code>LogicalExpression</code> new LogicalExpression
 */

public Potential copy(){
  LogicalExpression newLE=new LogicalExpression();

  // Copy the consecuent
  newLE.consecuent=consecuent.copy();

  // Copy the antecedent
  newLE.antecedent=antecedent.copy();

  // Copy the operator
  newLE.operator=operator;

  // Copy the result
  if (result != null) {
    newLE.result=(PotentialTree)result.copy();
  }

  // Return newLE
  return newLE;
}

/**
 * Method to set the antecedent
 * @param <code>LogicalNode</code> the logical node for antecedent
 *
 */

public void setAntecedent(LogicalNode antecedent){
  this.antecedent=antecedent;
}

/**
 * Method to set the consecuent
 * @param <code>int</code> the operator for this expression
 *
 */

public void setOperator(int operator){
  this.operator=operator;
}

/**
 * Method to set the consecuent
 * @param <code>LogicalNode</code> the logical node for consecuent
 *
 */

public void setConsecuent(LogicalNode consecuent){
  this.consecuent=consecuent;
}

/**
 * Method to set the result data member
 * @param <code>PotentialTree</code> result of the evaluation
 */

public void setResult(PotentialTree result){
  this.result=result;
}

/**
 * Method to check is applicable for a given set of variables
 * @param <code>Vector</code> vars to consider
 * @result true-false
 */

public boolean check(Vector vars){
 Configuration conf;
 boolean resAnt, resCon, resTot;

  // Build the indexes for the constraint
  if (indexed == false){
      buildIndex();
  }

  // Create a configuration for the variables in VARS
  conf=new Configuration(vars);

  // Test on antecedent
  resAnt=antecedent.check(conf);

  // Test the consecuent
  resCon=consecuent.check(conf);

  // Consider now the relation between them

  switch(operator){
    case LogicalNode.AND: resTot = resAnt && resCon;
              break;
    case LogicalNode.OR: resTot = resAnt || resCon;
             break;
    case LogicalNode.IMPLICATION: resTot = resAnt && resCon;
             break;
    case LogicalNode.DOUBLE_IMPLICATION: resTot = resAnt && resCon;
             break;
    default: resTot=false;
             System.out.println("Class LogicalExpression, check method");
             System.out.println("Error in logical expression analysis");
             break;
  }

  // Return the result
  return resTot;
}

/**
 * Method to access the result data member of this class
 */

public PotentialTree getResult(){
  return result;
}

/*
 * Set of methods impossed by inheritance from abstract class
 */

/**
 * Combines this <code>Potential</code> with <code>Potential</code> pot
 * @param pot the <code>Potential</code> to combine with this
 * <code>Potential</code>.
 * @return The combination of the two potentials.
 */

public Potential combine (Potential pot){
  System.out.println("combine method: class LogicalExpression");
  return null;
}

/**
 * Sum this <code>Potential</code> with <code>Potential</code> pot
 * @param pot the <code>Potential</code> to sum with this
 * <code>Potential</code>.
 * @return The addition of the two potentials.
 */

public Potential addition(Potential pot){
  System.out.println("addition method: class LogicalExpression");
  return null;
}

/**
 * Removes the argument variable summing over all its values.
 * @param var a Node variable.
 * @return a new Potential with the result of the deletion.
 */

public Potential addVariable(Node var){
  System.out.println("addVariable method: class LogicalExpression");
  return null;
}

/**
 * Returns a string with the expression
 */

public String returnExpression() {
  String theStr = new String("");

  if (antecedent != null) {
    theStr=theStr+antecedent.returnLogicalNode();
  }

  switch (operator) {
    case LogicalNode.IMPLICATION: theStr=theStr+" -> ";
            break;
    case LogicalNode.DOUBLE_IMPLICATION: theStr=theStr+" <-> ";
            break;
  }

  if (consecuent != null) {
    theStr=theStr+consecuent.returnLogicalNode();
  }

  return theStr;
}

/**
 * Saves the Values field to file: method still to be completed.
 */

public void save(PrintWriter p) {
   p.print("values=logical-expression(");
   if (antecedent != null) {
     antecedent.save(p);
   }
   switch (operator) {
       case LogicalNode.IMPLICATION: p.print(" -> ");
               break;
       case LogicalNode.DOUBLE_IMPLICATION: p.print(" <-> ");
               break;
   }
   if (consecuent != null) {
     consecuent.save(p);
   }
   p.print(");\n");
}

/**
 * Saves the potential to the file represented by the
 * <code>PrintWriter p</code>.
 * @param p the <code>PrintWriter</code>.
 */

public void saveResult(PrintWriter p){
  System.out.println("saveResult method: class LogicalExpression");
}


/**
 * Normalizes the values of this potential. The object is modified.
 */

public void normalize(){
  System.out.println("normalize method: class LogicalExpression");
}


/**
 * Gets the value of a potential for a given configuration of
 * variables.
 * @param conf a <code>Configuration</code> of variables.
 * @return the value of the potetial for <code>conf</code>.
 */

public double getValue(Configuration conf){
  System.out.println("getValue method: class LogicalExpression");
  System.exit(1);
  return 0;
}

/**
 * Sets the value of a <code>Potential</code> for a given
 * <code>Configuration</code>.
 * @param conf a <code>Configuration</code> of variables.
 * @param val the value for conf.
 */

public void setValue(Configuration conf, double val){
  System.out.println("setValue method: class LogicalExpression");
  System.exit(1);
}


/**
 * Gets the size of the potential. The size of a potential is the number
 * of real numbers used to represent it.
 * @return the size of the array in table mode;
 * the number of nodes in tree mode.
 */

public long getSize(){
  long size=0;
  if (result != null)
     size=result.getSize();
  return (size);
}


/**
 * Gets the addition of all the values of the potential.
 * @return the addition of all the values of the potential.
 */

public double totalPotential(){
  System.out.println("totalPotential method: class LogicalExpression");
  System.exit(1);
  return 0;
}


/**
 * Gets the addition of the values of a potential that are consistent
 * with a given configuration of variables.
 * @param conf a <code>Configuration</code> of variables.
 * @return the sum of the values of this potential
 * restricted to <code>Configuration conf</code>.
 */

public double totalPotential(Configuration conf){
  System.out.println("totalPotential method: class LogicalExpression");
  System.exit(1);
  return 0;
}


/**
 * Compute;
  return 0;
}


/**
 * Compute @return the sum of the values x * Log x stored in the potential.
 */

public double entropyPotential(){
  System.out.println("entropyPotential method: class LogicalExpression");
  System.exit(1);
  return 0;
}

/**
 * Computes the entropy of a potential restricted to
 * a given configuration.
 * @param conf the <code>Configuration</code>.
 * @return the sum of the values x Log x fixing
 * <code>Configuration conf</code>.
 */

public double entropyPotential(Configuration conf){
  System.out.println("entropyPotential(Configuration) method: class LogicalExpression");
  System.exit(1);
  return 0;
}


/**
 * Restricts the potential to a configuration of variables.
 *
 * @param conf the <code>Configuration</code>.
 * @return the restricted <code>Potential</code>.
 */

public Potential restrictVariable(Configuration conf){
  return this;
}


/**
 * Marginalizes over a set of variables. It is equivalent
 * to <code>addVariable</code> over the other variables.
 * @param vars a <code>Vector</code> of variables (<code>Node</code>).
 * @return a <code>Potential</code> with the marginalization
 * over <code>vars</code>.
 */

public Potential marginalizePotential(Vector vars){
  System.out.println("marginalizePotential method: class LogicalExpression");
  System.exit(1);
  return null;
}

/*
 * Overwrite some methods developed for Potential,with
 * different meaning for LogicalExpressions
 */

/**
  * Combines in this <code>Potential</code> the <code>Potential</code> pot1
  * and pot2, but only the fields indicated by <code>field</code> with
  * the following meaning:
  * <UL>
  * <LI> field=ONLY_VARS then combine only field <code>variable</code>
  * </UL>
  * @param pot1 the first <code>Potential</code> to be combined.
  * @param pot2 the second <code>Potential</code> to be combined.
  */

public void combine(Potential p1, Potential p2, int field) {
   System.out.println("combine(Potential,Potential,int) method: ");
   System.out.println("     class LogicalExpression");
   System.exit(1);
}

/**
 * Removes in <code>Potential pot</code> the argument variable
 * summing over all its values, but only the fields indicated by
 * <code>field</code> with the following
 * meaning:
 * <UL>
 * <LI> field=ONLY_VARS then sum out only field <code>variable</code>
 * </UL>
 * The result is put in this  <code>Potential</code>
 * @param pot a <code>Potential</code> to be sum out over <code>var</code>.
 * @param var a FiniteStates variable.
 * @param field a int to determine to which field the
 * marginalization affects.
 */

public void addVariable(Potential pot, FiniteStates var, int field) {
   System.out.println("addVariable(Potential,FiniteStates,int) method: ");
   System.out.println("     class LogicalExpression");
   System.exit(1);
}

/**
 * Normalizes a given potential to sum up to one.
 * @param pot a <code>Potential</code>.
 * @return a new normalized <code>Potential</code>.
 */

public Potential normalize(Potential pot) {
   System.out.println("normalize(Potential) method: class LogicalExpression");
   System.exit(1);
   return null;
}

/**
 * Tells whether the <code>Potential</code> is exact or not.
 * @return <code>true</code> if the <code>Potential</code> is exact,
 * <code>false</code> otherwise. By default, returns <code>true</code>.
 */

public boolean getExact() {
   System.out.println("getExact method: class LogicalExpression");
   System.exit(1);
   return(false);
}

/**
 * Gets the name of the class.
 * @return a <code>String</code> with the name of the class.
 */

public String getClassName() {
  return new String("LogicalExpression");
}

/**
 * Prunes the potential according to an information value.
 * the object is modified.
 * @param limitForPrunning the information value (<code>double</code>)
 * under which the potential will be prunned.
 */

public void limitBound(double limitForPrunning) {
  System.out.println("limitBound(double) method: class LogicalExpression");
  System.exit(1);
}

/**
 * Bounds the tree associated with the potential by removing
 * nodes whose information value is lower than a given threshold
 * or whose addition is lower than a given value.
 * THE TREE IS MODIFIED.
 * @param limit the information limit.
 * @param limitSum the limit sum for pruning.
 */

public void limitBound(double limit, double limitSum) {
  System.out.println("limitBound(double,double) method:");
  System.out.println("           class LogicalExpression:");
  System.exit(1);
}

/**
 * Bounds the tree associated with the potential by removing
 * nodes whose information value is lower than a given threshold
 * or whose addition is lower than a given value.
 * THE TREE IS MODIFIED.
 * @param kindOfApprPruning the method used to approximate several leaves
 * with a double value (AVERAGE_APPROX, ZERO_APPROX, AVERAGEPRODCOND_APPROX ...)
 * @param limit the information limit.
 * @param limitSum the limit sum for pruning.
 */

public void limitBound(int kindOfApprPruning,double limit, double limitSum) {
  System.out.println("limitBound(int,double,double) method:");
  System.out.println("           class LogicalExpression:");
  System.exit(1);
}

/**
 * Prunes the potential keeping the branches with
 * higher probability.
 * The object is modified.
 * @param n the number of leaves to keep (<code>int</code>)
 * under which the potential will be prunned.
 */

public void limitBound(int n) {
  System.out.println("limitBound(int) method:");
  System.out.println("           class LogicalExpression:");
  System.exit(1);
}


/**
 * Sorts the variables in the potential placing first the most
 * informative ones, and pruned the potential not to exceed a maximum
 * number of values.
 * @param maxLeaves the maximum number of values allowed.
 * @return the resulting <code>Potential</code>.
 */

public Potential sortAndBound(int maxLeaves) {
  System.out.println("sortAndBound(int) method:");
  System.out.println("           class LogicalExpression:");
  System.exit(1);
  return null;
}


/**
 * Conditions this potential to another one. To condition is to store
 * in one potential the values of the other one.
 * @param pot the conditioning <code>Potential</code>.
 * @return the conditioned <code>Potential</code>.
 */

public Potential conditional(Potential pot) {
  System.out.println("conditional(Potential) method:");
  System.out.println("           class LogicalExpression:");
  System.exit(1);
  return null;
}


/**
 * Prunes the potential according to an information value.
 * the object is modified.
 * @param lp the information value (<code>double</code>)
 * under which the potential will be prunned (depending on the method).
 * @param llp the lower limit for prunning (used depending on the method).
 * @param method the method of prunning (see subclass definitions).
 */

public void conditionalLimitBound(double lp, double llp, int method) {
  System.out.println("conditionallimitBound(double, double, int) method:");
  System.out.println("           class LogicalExpression:");
  System.exit(1);
}

/*
 * LOGICALEXPRESSION Methods
 */

/**
 * Method to evaluate the whole expression
 */

public void evaluate(){
  PotentialTable resultTable;
  Configuration conf;
  int i,total,valInt;
  boolean valBool;

  // First at all, build the indexes. So I know the list
  // of variables involved, and the positions they have
  // in antecedent and consecuent. This helps the propagation
  // of values

  if (indexed == false)
      buildIndex();

  // Build the potential table to store the evaluation

  resultTable=new PotentialTable(variables);

  // Now I have to build a configuration for this set of
  // variables

  conf=new Configuration(variables);

  // Loop to go over the whole set of possible values

  total=(int)FiniteStates.getSize(variables);

  for(i=0; i < total; i++){
    valBool=evaluate(conf);
    valInt=(valBool==true) ? 1: 0;
    resultTable.setValue(conf,valInt);
    conf.nextConfiguration();
  }

  // Now transform it to a probabilityTree

  result=resultTable.toTree();

  // Now prune it to store the constraint as compact as possible

  result=result.sortAndBound(0L);
}

/**
 * Method to evaluate the expression for a given configuration
 * (changed private access to public since 08/11/2005)
 */

public boolean evaluate(Configuration conf){
 boolean antVal;
 boolean conVal;
 boolean finalResult;

  // Evaluate the antecedent

  antVal=antecedent.evaluateConfiguration(conf);

  // Evaluate the consecuent

  conVal=consecuent.evaluateConfiguration(conf);

  // Compose the results

     // Compose them

     switch(operator){
       case LogicalNode.IMPLICATION: finalResult=(!antVal || conVal);
                 break;
       case LogicalNode.DOUBLE_IMPLICATION: finalResult=(!antVal || conVal)
                                          &&(antVal || !conVal);
                 break;
       default: finalResult=false;
                break;
     }

  return(finalResult);
}

/**
 * Method to print the logical expression
 */

public void print(){

  // Print the antecedent

  System.out.println("ANTECEDENT.....................");
  antecedent.print(1);
  System.out.println("------------------------------");

  // Print the relation between antecedent and consecuent

  switch(operator){
     case LogicalNode.IMPLICATION:
          System.out.println("OPERATOR: IMPLICATION");
          break;
     case LogicalNode.DOUBLE_IMPLICATION:
          System.out.println("OPERATOR: DOUBLE IMPLICATION");
          break;
     default:
          System.out.println("Unknown relation between antecedent and");
          System.out.println("consecuent");
          break;
  }

  System.out.println("------------------------------");

  // Print the consecuent

  System.out.println("CONSECUENT.....................");
  consecuent.print(1);

  // Print the values

  System.out.println("MATRIX.....................");
  if (result != null)
      result.print();
   else
      System.out.println("Non evaluated constraint");
  System.out.println("-----------------------------------");
}

/**
 * Method to index the variables in the expression. This
 * allows a quick evaluation over the configurations
 */

private void buildIndex(){

  // Go over antecedent and consecuent. Every
  // variable will be added to variables vector,
  // indexing repect to the nodes where it appears
  antecedent.indexVariables();
  consecuent.indexVariables();

  // Merge both sides
  mergeVectorsOfVariablesAndIndexes();

  // Set a flag saying the variables are indexed
  indexed=true;
}

/**
 * Method to merge the vectors of variables and indexes
 * for antecedent and consecuent.
 */

private void mergeVectorsOfVariablesAndIndexes(){
  int leftSize,rightSize;
  Vector newVariables,newIndex,indexForVar,indexPrev;
  LogicalNode base,other;
  Node node;
  int i,j,pos;

   leftSize=antecedent.size();
   rightSize=consecuent.size();

   if(leftSize > rightSize){
      base=antecedent;
      other=consecuent;
   }
    else{
      base=consecuent;
      other=antecedent;
   }

    // Copy the vectors from base
    newVariables=(Vector)(base.getVariables().clone());
    newIndex=(Vector)(base.getIndex().clone());

    // Now, loop over the other LogicalNode, adding
    // variables and indexes
    for(i=0; i < other.getVariables().size(); i++){
      node=(Node)(other.getVariables().elementAt(i));

      // Check if present
      pos=newVariables.indexOf((Object)node);

      // If present, pos != -1. Otherwise pos = 1
      if (pos == -1){
         // Add it

         newVariables.addElement((Object)node);
         pos=newVariables.size();

         // Copy the indexes for this variable
         indexForVar=(Vector)((Vector)(other.getIndex().elementAt(i))).clone();

         // Insert in vector newIndex
         newIndex.addElement((Object)indexForVar);
      }
      else{
         // Add the new indexes for this variable
         indexForVar=((Vector)(other.getIndex().elementAt(i)));
         indexPrev=((Vector)(newIndex.elementAt(pos)));

         for(j=0; j < indexForVar.size(); j++){
            indexPrev.addElement(indexForVar.elementAt(j));
         }
      }
    }

    // Once done, change data members

    variables=newVariables;
    index=newIndex;
}

/**
 * Public method checking the number of ones in the tree
 * @return ones
 */
public double checkOnes(){
   double ones=0;
   
   if (indexed == false)
      evaluate();
   
   // Now compute the number of ones
   ones=result.totalPotential();
   
   // Return ones
   return ones;
}


/**
 * MAIN, to make tests
 */

public static void main(String args[]) throws IOException, ParseException{
  IDiagram diag;
  Vector relations;
  Relation relation;
  LogicalExpression logicalExpression;
  int i;

  diag=(IDiagram)Network.read(args[0]);
  relations=diag.getRelationList();

  // Go over the set of relations

  for(i=0; i < relations.size(); i++){
     relation=(Relation)relations.elementAt(i);

     // Look the kind of relation

     if (relation.getKind() == Relation.CONSTRAINT){
        // Retrieve the logical expression

        logicalExpression=(LogicalExpression)relation.getValues();

        // Evaluate

        logicalExpression.evaluate();

        // Show the result

        logicalExpression.print();
     }
  }
}

public LogicalNode getAntecedent()
{
    return antecedent;
}

public LogicalNode getConsecuent()
{
    return consecuent;
}

} // End of class
