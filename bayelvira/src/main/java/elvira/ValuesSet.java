/* Crono.java */

package elvira;

import java.io.*;
import java.util.Vector;
import elvira.Node;
import elvira.FiniteStates;


/**
 * Implements a class to support a list of possible
 * values for a given variable. This class is used
 * for the treatment of constraints  
 * Operations:
 * <ul>
 * <li> Constructor
 * <li> test: See if in a configuration there is a value
 *            for this variable included in the set of allowed i
 *            values. The set of values can be defined with a
 *            negative description (e.j: X != {x2, x3})
 * @author Manuel Gomez
 * @since 7/05/2002
 */

public class ValuesSet {

/**
 * Node for which is defined the set of values 
 */
private Node node;

/**
 * Set of values 
 */
private Vector values;

/**
 * Flag, to set if the description is positive or negative 
 */
private boolean negated;

/**
 * Constructor for ValuesSet
 */

public ValuesSet(Node node, Vector values,boolean flag) {
 int i;
 FiniteStates fstates;

  this.node=node;
  this.values=(Vector)values.clone();
  this.negated=flag;

  // Once asigned the values, test them

  if (node.getTypeOfVariable() == Node.FINITE_STATES){
    fstates=(FiniteStates)node;

    for(i=0; i < values.size(); i++){
       if(fstates.getId((String)values.elementAt(i)) == -1){
          System.out.println("Trying to assign a wrong value for node");
          System.out.println(node.getName()+" in ValuesSet constructor");
          System.exit(-1);
       }
    }
  }
}

/**
 * Method to copy a values set
 * @return <code>ValuesSet</code> new values set
 */

public ValuesSet copy(){
  ValuesSet newValSet=new ValuesSet(node,values,negated);
  return newValSet;
}

/**
 * Method to access the node
 * @return <code>Node</code> the node used in this valuesSet
 */

public Node getNode(){
  return(node);
}

/**
 * Method to access the negated field
 */

public boolean getNegated(){
    return(negated);
}

/**
 * Method to acces values
 */

public Vector getValues(){
    return(values);
}

/**
 * Method to print the valueSet
 */

public void print(String whiteSpaces){
 int i;

  System.out.println(whiteSpaces+"NOMBRE: "+node.getName());
  System.out.println(whiteSpaces+"NEGATED: "+negated);
  for(i=0; i < values.size(); i++){
   System.out.println("  "+whiteSpaces+" Valor: "+values.elementAt(i));
  }
}

/**
 * Method to check if a value for this variable is in the
 * set of values
 * @param<code>String</code> the value to look at
 * @return boolean
 *
 */

public boolean checkValue(String val){
  boolean found=false;
  int i;

  for(i=0; i < values.size(); i++){
    if(val.equals((String)(values.elementAt(i)))){
       found=true;
       break;
    }
  }

  if(negated)
     found=!found;

  // Return the result

  return(found);
}

} // End of class
