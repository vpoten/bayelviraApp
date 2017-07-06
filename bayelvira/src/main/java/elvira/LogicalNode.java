/* Crono.java */

package elvira;


import java.io.*;
import java.util.Vector;
import elvira.Node;
import elvira.FiniteStates;


/**
 * Implements a node to use in the construction of logical
 * expressions
 * Operations:
 * <ul>
 * <li> Constructor
 * @author Manuel Gomez
 * @since 7/05/2002
 */

public class LogicalNode {

/**
 * Constants to show if the node is an operator or an
 * operand
 */

public final static int OPERATOR=1;
public final static int OPERAND=2;

/**
 * Constants to express the operations
 */
public final static int AND=1;
public final static int OR=2;
public final static int NOT=2;
public final static int IMPLICATION=3;
public final static int DOUBLE_IMPLICATION=4;

/**
 * To show the kind of node: operator or operand
 */
private int kind;

/**
 * To show the operator
 */

private int operator;

/**
 * To show if the node is affected by a negation
 */
private boolean negated;

/**
 * The operators will have two branches for the operands
 */
private LogicalNode leftOperand;
private LogicalNode rightOperand;

/**
 * If acts as a data node, a set of values for a variable
 */
private ValuesSet valuesSet;

/**
 * To store the list of variables used
 */

private Vector variables;

/**
 * To store the indexes
 */

private Vector index;

/**
 * To store the result of evaluation
 */

private boolean result;

/**
 * To store a value considered as observed for a variable
 */

private int observedValue;

/**
 * To store if the node is covered by a given set of variables
 */

private boolean covered;

/**
 * Constructor of the class
 * @param operator integer to show is the node is going to
 *        represent an AND or OR operator
 */

public LogicalNode(int operator) {
  this.operator=operator;
  this.kind=LogicalNode.OPERATOR;

  // Set negated flag to false

  negated=false;

  // Build the vector for variables

  variables=new Vector();

  // Vector for index

  index=new Vector();

  // Set covered flag to false

  covered=false;

  // At creation time, observed value is -1

  observedValue=-1;
}

/**
 * Constructor for data nodes
 * @param valueSet the set of constrained values for a variable
 */

public LogicalNode(ValuesSet values) {
  this.valuesSet=values;
  this.kind=LogicalNode.OPERAND;

  // Set negated flag to false

  negated=false;

  // Build the vector for variables

  variables=new Vector();

  // Vector for index

  index=new Vector();

  // Set covered flag to false

  covered=false;

  // At creation time, observed value is -1

  observedValue=-1;
}

/**
 * Default constructor
 */

public LogicalNode(){
  variables=new Vector();
  index=new Vector();
  covered=false;
  negated=false;
  observedValue=-1;
  valuesSet=null;
}

/**
 * Method to copy a LogicalNode
 * @return </code>LogicalNode</code> new LogicalNode
 */

public LogicalNode copy(){
  LogicalNode newLogNode=new LogicalNode();

  // Copy kind data member

  newLogNode.kind=kind;

  // Copy the operator

  newLogNode.operator=operator;

  // Copy the negated flag

  newLogNode.negated=negated;

  // Copy left anf right operands

  if (leftOperand != null)
    newLogNode.leftOperand=leftOperand.copy();

  if(rightOperand != null)
    newLogNode.rightOperand=rightOperand.copy();

  // Copy the values set

  if(valuesSet != null)
    newLogNode.valuesSet=valuesSet.copy();

  // Copy result

  newLogNode.result=result;

  // Copy observed value

  newLogNode.observedValue=observedValue;

  // Copy covered

  newLogNode.covered=covered;

  // Return the new logical node

  return newLogNode;
}

/**
 * To assign the left operand
 * @param <code>LogicalNode</code> left operand
 */

public void setLeftOperand(LogicalNode left){
  leftOperand=left;

  // Test if the node is really an operator node

  if (kind != LogicalNode.OPERATOR){
     System.out.println("Trying to assign an operand to a non operator");
     System.out.println("node (setLeftOperand; LogicalNode)");
     System.exit(-1);
  }
}

/**
 * To assign the right operand
 * @param <code>LogicalNode</code> right operand
 */

public void setRightOperand(LogicalNode right){
  rightOperand=right;

  // Test if the node is really an operator node

  if (kind != LogicalNode.OPERATOR){
     System.out.println("Trying to assign an operand to a non operator");
     System.out.println("node (setRightOperand; LogicalNode)");
     System.exit(-1);
  }
}

/**
 * Method to assign the negation flag
 * @param flag the value for negated data member
 */

public void setNegated(boolean flag){
  negated=flag;
}

/**
 * To access the vector of variables
 */

public Vector getVariables(){
  return(variables);
}

/**
 * To access the vector of indexes
 */

public Vector getIndex(){
  return(index);
}

/**
 * Method to print the data about the nodes
 */

public void print(int level){
 String whiteSpaces="  ";
 int i;

  // Print the kind of node

  for(i=0; i < level; i++)
    whiteSpaces=whiteSpaces.concat("     ");

  System.out.print(whiteSpaces+"kind : ");

  if (kind == LogicalNode.OPERATOR) {
      System.out.print(" OPERADOR ");
      switch(operator){
       case AND: System.out.println("AND");
                 break;
       case OR: System.out.println("OR");
                 break;
       case IMPLICATION: System.out.println("IMPLICATION");
                 break;
       case DOUBLE_IMPLICATION: System.out.println("DOUBLE IMPLICATION");
                 break;
      }

      // Show the operands, from left to right

      level++;
      leftOperand.print(level);
      rightOperand.print(level);
  }
  else{
    System.out.println("OPERAND ");

    // show the valuesSet

    System.out.println(whiteSpaces+"Values set: ......................");
    valuesSet.print(whiteSpaces);

    // Decrement level

    level--;
  }

  System.out.println(whiteSpaces+"Negated : "+negated);
  System.out.println(whiteSpaces+"Covered : "+covered);
}

/**
 * Method to index the variables of the node, respect to the
 * position where they appear
 */

public void indexVariables(){
  Node node;
  Vector indexes;
  int position;

  // Go over the logical node, adding all the variables

  if (kind != LogicalNode.OPERATOR){

    node=valuesSet.getNode();

    // Add the variable if its not already stored

    if(variables.contains((Object)node) == false){
       variables.addElement((Object)node);

       // Build its vector of indexes

       indexes=new Vector();

       // Store the location

       indexes.addElement(this);

       // This vector belongs to index

       index.addElement((Object)indexes);
    }
    else {
      // The variable is already stored in the vector of
      // variables, but it must be indexed

      position=variables.indexOf((Object)node);

      indexes=(Vector)index.elementAt(position);

      // Store the new reference

      indexes.addElement(this);
    }
  }
  else{
    // If the logical node is an operator, translate
    // the operation to leftOperand and rightOperand

    leftOperand.indexVariables();
    rightOperand.indexVariables();

    // Now is needed to merge the vectors of variables and
    // indexes from both of them

    mergeVectorsOfVariablesAndIndexes();
  }
}

/**
 * Method to evaluate the logical node given a configuration
 * @param <code>Configuration</code> the configuration for which
 *                               the logical node will be evaluated
 * @return the result of the evaluation
 */

public boolean evaluateConfiguration(Configuration conf){

  // Propagate the values for variables

  propagateValues(conf);

  // Get tthe result

  result=evaluate();
  return(result);
}

/**
 * Method to propagate the values of the configuration,
 * using the vectors of variables and indexes
 * @param <code>Configuration</code> configuration to use
 *
 */

private void propagateValues(Configuration conf){
  int i,j;
  Node node;
  LogicalNode ocurrence;
  Vector locations;
  int posInVariables;
  int newValue;
  String value;

  // For each variable in conf, propagate its value

  for(i=0; i < conf.size(); i++){
    node=conf.getVariable(i);

    // Now, get the position of this node in the vector
    // of variables

    posInVariables=variables.indexOf((Object)node);

    // Propagate only if the variable was found

    if(posInVariables != -1){
      // This position is used to locate the nodes where
      // this variable appears

      locations=(Vector)index.elementAt(posInVariables);

      // Loop over this vector to locate all the nodes
      // related to this variable

      for(j=0; j < locations.size(); j++){
        ocurrence=(LogicalNode)locations.elementAt(j);

        // Check the value already stored for it

        newValue=conf.getValue((FiniteStates)node);
        if(ocurrence.observedValue != newValue){
          // Needed to change the asignation and the comparison
          // with the values

          ocurrence.observedValue=newValue;
          value=((FiniteStates)node).getState(ocurrence.observedValue);

          // Check the value for this variable

          ocurrence.result=ocurrence.valuesSet.checkValue(value);
        }
      }
    }
  }
}

/**
 * Method to propagate the effect of a set of variables,
 * to test if a set of variables is able to activate
 * the constraint
 * @param <code>Configuration</code> configuration with the variables
 *
 */

private void propagateEffect(Configuration conf){
  int i,j;
  Node node;
  LogicalNode ocurrence;
  Vector locations;
  int posInVariables;
  int newValue;
  String value;

  // For each variable in conf, propagate its value

  for(i=0; i < conf.size(); i++){
    node=conf.getVariable(i);

    // Now, get the position of this node in the vector
    // of variables

    posInVariables=variables.indexOf((Object)node);

    // Propagate only if the variable was found

    if(posInVariables != -1){
      // This position is used to locate the nodes where
      // this variable appears

      locations=(Vector)index.elementAt(posInVariables);

      // Loop over this vector to locate all the nodes
      // related to this variable

      for(j=0; j < locations.size(); j++){
        ocurrence=(LogicalNode)locations.elementAt(j);

        // Activate the "covered" flag

        ocurrence.covered=true;
      }
    }
  }
}

/**
 * Method to evaluate, once values are propagated
 * @return result
 */

private boolean evaluate(){
  boolean finalResult,leftResult,rightResult;

  // Get results and compose them

  if(kind == LogicalNode.OPERATOR){

     // Get the results

     leftResult=leftOperand.evaluate();
     rightResult=rightOperand.evaluate();

     // Compose them

     switch(operator){
       case AND: finalResult=(leftResult && rightResult);
                 break;
       case OR: finalResult=(leftResult || rightResult);
                 break;
       case IMPLICATION: finalResult=(!leftResult || rightResult);
                 break;
       case DOUBLE_IMPLICATION: finalResult=(!leftResult || rightResult)
                                          &&(leftResult || !rightResult);
                 break;
       default: finalResult=false;
                break;
     }
  }
  else{

     // If the node is an OPERAND; return the value produced
     // as result after the evaluation respect to the values
     // propagated

     finalResult=result;
  }

  if (negated)
     finalResult=!finalResult;

  return(finalResult);
}

/**
 * Method to check if the covered nodes are capable to
 * activate this logical relation
 * @return result
 */

private boolean evaluateEffect(){
  boolean finalResult,leftResult,rightResult;

  // Get results and compose them

  if(kind == LogicalNode.OPERATOR){

     // Get the results

     leftResult=leftOperand.evaluateEffect();
     rightResult=rightOperand.evaluateEffect();

     // Compose them

     switch(operator){
       case AND: finalResult=(leftResult && rightResult);
                 break;
       case OR: finalResult=(leftResult || rightResult);
                 break;
       // For implication and double implication we impose the
       // needed for both operands be covered
       case IMPLICATION: finalResult=(leftResult && rightResult);
                 break;
       case DOUBLE_IMPLICATION: finalResult=(leftResult && rightResult);
                 break;
       default: finalResult=false;
                break;
     }
  }
  else{

     // If the node is an OPERAND; return the value of the flag
     // "covered"

     finalResult=covered;
  }

  return(finalResult);
}

/**
 * Method to calculate the size of the LogicalNode, i.e, the
 * number of operands and operators included in it
 */

public int size(){
  int size=1;

  // If it is an operand, return one. Otherwise go over
  // left and right operands

  if(kind == LogicalNode.OPERATOR){
     size=size+leftOperand.size();
     size=size+rightOperand.size();
  }

  // Return size

  return(size);
}

/**
 * Method to test if the variables passed in the configuration
 * are able to make applicable this logical relation between
 * variables
 * @param <code>Configuration</code>
 * @result true-false
 */

public boolean check(Configuration conf){
 boolean result;

  // Initially, set covered to false
  covered=false;

  // Propagate the effect of these variables
  propagateEffect(conf);

  // Now evaluate the effect
  result=evaluateEffect();

  // Return this value
  return(result);
}

/**
 * Method to merge the vectors of variables and indexes
 * for left and right operand nodes. This method is only
 * for LogicalNodes used as operators
 */

private void mergeVectorsOfVariablesAndIndexes(){
  int leftSize,rightSize;
  Vector newVariables,newIndex,indexForVar,indexPrev;
  LogicalNode base,other;
  Node node;
  int i,j,pos;

  if(kind == LogicalNode.OPERATOR)
  {
    leftSize=leftOperand.size();
    rightSize=rightOperand.size();

    if(leftSize > rightSize){
      base=leftOperand;
      other=rightOperand;
    }
    else{
      base=rightOperand;
      other=leftOperand;
    }

    // Copy the vectors from base

    newVariables=(Vector)(base.getVariables().clone());
    newIndex=(Vector)(base.getIndex().clone());

    // Now, loop over the other LogicalNode, adding
    // variables and indexes

    for(i=0; i < other.variables.size(); i++){
      node=(Node)other.variables.elementAt(i);

      // Check if present

      pos=newVariables.indexOf((Object)node);

      // If present, pos != -1. Otherwise pos = 1

      if (pos == -1){
         // Add it

         newVariables.addElement((Object)node);
         pos=newVariables.size();

         // Copy the indexes for this variable

         indexForVar=(Vector)((Vector)(other.index.elementAt(i))).clone();

         // Insert in vector newIndex

         newIndex.addElement((Object)indexForVar);
      }
      else{
         // Add the new indexes for this variable

         indexForVar=((Vector)(other.index.elementAt(i)));
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
}

/**
 * Returns a string with the expression
 */

public String returnLogicalNode() {
  String theStr = new String("");

  if (leftOperand != null) {
    theStr=theStr+leftOperand.returnLogicalNode();
  }
  switch (kind) {
    case OPERATOR:
        switch (operator) {
            case AND: theStr=theStr+" & ";
            case OR: theStr=theStr+" | ";
            case IMPLICATION: theStr=theStr+" -> ";
            case DOUBLE_IMPLICATION: theStr=theStr+" <-> ";
        }
        break;
    case OPERAND:
        if (negated) {
          theStr=theStr+"!(";
        }
        if (valuesSet != null) {
            theStr=theStr+valuesSet.getNode().getName();
            if (valuesSet.getNegated()) {
                theStr=theStr+" !in ";
            }
            else {
                theStr=theStr+" in ";
            }
            theStr=theStr+"{";
            for (int i=0; i<valuesSet.getValues().size(); i++) {
                theStr=theStr+valuesSet.getValues().elementAt(i).toString();
                if ((valuesSet.getValues().size()>1) && (i<valuesSet.getValues().size()-1)){
                    theStr=theStr+",";
                }
            }
            theStr=theStr+"}";
            if (negated) {
                theStr=theStr+")";
            }
        }
        break;
  }
  if (rightOperand != null) {
    theStr=theStr+rightOperand.returnLogicalNode();
  }

  return theStr;
}

/**
 * Method to save LogicalNode to file.
 */

public void save(PrintWriter p) {
    if (leftOperand != null) {
        leftOperand.save(p);
    }
    switch (kind) {
        case OPERATOR:
            switch (operator) {
                case AND: p.print(" & ");
                case OR: p.print(" | ");
                case IMPLICATION: p.print(" -> ");
                case DOUBLE_IMPLICATION: p.print(" <-> ");
            }
            break;
        case OPERAND:
            if (negated) {
              p.print("!(");
            }
            if (valuesSet != null) {
                p.print(valuesSet.getNode().getName());
                if (valuesSet.getNegated()) {
                    p.print(" !in ");
                }
                else {
                    p.print(" in ");
                }
                p.print("{");
                for (int i=0; i<valuesSet.getValues().size(); i++) {
                    p.print(valuesSet.getValues().elementAt(i));
                    if ((valuesSet.getValues().size()>1) && (i<valuesSet.getValues().size()-1)){
                        p.print(",");
                    }
                }
                p.print("}");
                if (negated) {
                    p.print(")");
                }
            }
            break;
    }
    if (rightOperand != null) {
        rightOperand.save(p);
    }
}

public ValuesSet getValuesSet()
{
    return valuesSet;
}

/**
 * Method to test if there are variables in common betwwen this
 * logical node and a configuration passed as argument
 * @param <code>Configuration</code> configuration to test with
 * @return <code>boolean</code> result of the operation
 */
public boolean testVarsInCommon(Configuration conf){
  Vector varsInConf=conf.getVariables();
  Node node,nodeInConf;
  int i,j;

  for(i=0; i < variables.size(); i++){
    node=(Node)variables.elementAt(i);

    // Look for this node in the variables of the configuration

    for(j=0; j < varsInConf.size(); j++){
      nodeInConf=(Node)varsInConf.elementAt(j);

      if ((nodeInConf.getName()).equals(node.getName())){
        return true;
      }
    } 
  }

  // return false

  return false;
}

} // End of class
