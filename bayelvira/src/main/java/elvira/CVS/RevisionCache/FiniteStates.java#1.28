/* FiniteStates.java */

package elvira;

import java.util.Vector;
import java.io.*;

import elvira.Node;


/**
 * Implements the class of nodes corresponding to
 * discrete random variables with a finite number of
 * possible values.
 * All the objects of this class have instance variable
 * <code>kindOfNode</code> set to CHANCE and <code>typeOfVariable</code>
 * FINITE_STATES.
 *
 * @since 11/7/2002
 */

public class FiniteStates extends Node implements Cloneable, Serializable {

static final long serialVersionUID = -4227573765951224469L;

/**
 * Names of the possible values (states) of the variables.
 * The names are of class <code>String</code>.
 */
private Vector states;

/**
 * Number of states.
 */
private int numStates;

/**
 * Indicates whether the node is transparent or not when displaying
 * a network.
 */
private int transparency;

/**
 * Values of variable <code>transparency</code>.
 */
public static int NOT_TRANSPARENT = 0;
public static int TRANSPARENT = 1;

/**
 * The value for an undefined variable
 */
public static final double UNDEFVALUE=-1.0;

/**
 * Creates a new empty <code>FiniteStates</code> object.
 */

public FiniteStates() {

  super();
  setTypeOfVariable(FINITE_STATES);
  setKindOfNode(CHANCE);
  setTransparency(NOT_TRANSPARENT);
}


/**
 * Creates a new <code>FiniteStates</code> object.
 * @param fm a font.
 */

public FiniteStates(String fm) {

  super(fm);
  setTypeOfVariable(FINITE_STATES);
  setKindOfNode(CHANCE);
  setTransparency(NOT_TRANSPARENT);
}


/**
 * Creates a <code>FiniteStates</code> object with the name and
 * the states given as parameters.
 * @param nam the name of the new node.
 * @param stat the states of the new node.
 */

public FiniteStates(String nam, Vector stat) {

  this ();
  numStates = stat.size();

  setName(nam);
  setStates (stat);
}


/**
 * Creates a <code>FiniteStates</code> object with the parameters given.
 * @param n the name of the new node.
 * @param x the x coordinate.
 * @param y the y coordinate.
 * @param defaultStates the states of the node created.
 */

public FiniteStates(String n, int x, int y, Vector defaultStates) {

  this(n, defaultStates);
  setPosX(x);
  setPosY(y);
  setTypeOfVariable(FINITE_STATES);
  setKindOfNode(CHANCE);
  setStates(defaultStates);
}


/**
 * Creates a <code>FiniteStates</code> object with the parameters given.
 * @param n the name of the new node.
 * @param x the x coordinate.
 * @param y the y coordinate.
 */

/*public FiniteStates(String n, int x, int y) {

  this(n, Elvira.getDefaultStates());
  setPosX(x);
  setPosY(y);
  setTypeOfVariable(FINITE_STATES);
  setKindOfNode(CHANCE);
}*/


/**
 * Creates a <code>FiniteStates</code> object with the parameters given.
 * @param n the name of the new node.
 * @param x the x coordinate.
 * @param y the y coordinate.
 * @param defaultStates the states of the node created.
 * @param fm a font.
 */

public FiniteStates(String n, int x, int y, Vector defaultStates,
		    String fm) {

  this(fm);
  numStates = defaultStates.size();

  setName(n);

  setPosX(x);
  setPosY(y);
  setStates(defaultStates);
  //setStates(Elvira.getDefaultStates());
}


/**
 * Creates a new <code>FiniteStates</code> object with the name given.
 * To set the default states uses an array of strings.
 * @param nam the name of the new node.
 * @param stat the states of the new node as an array of <code>String</code>.
 */

public FiniteStates(String nam, String[] stat) {

  this();

  numStates = stat.length;
  setName(nam);

  states = new Vector();
  for (int i=0 ; i<numStates ; i++)
    states.addElement(stat[i]);
}


/**
 * Creates a new <code>FiniteStates</code> object with the number of states
 * given as parameter.
 * @param n the number of states of the new node.
 * The states will be referred by numbers from 0 to n-1.
 */

public FiniteStates(int n) {

  this();

  Integer in;
  int i;

  states = new Vector();
  numStates = n;

  for (i=0 ; i<numStates ; i++) {
    in = new Integer(i);
    states.addElement(in.toString());
  }
}

/**
 * Creates a new <code>FiniteStates</code> object with the name of the
 * var and the number of states as parameters
 * @param name for the var
 * @param n the number of states of the new node.
 * The states will be referred by numbers from 0 to n-1.
 */

public FiniteStates(String name, int n) {

  this();

  Integer in;
  int i;

  setName(name);
  states = new Vector();
  numStates = n;

  for (i=0 ; i<numStates ; i++) {
    in = new Integer(i);
    states.addElement(in.toString());
  }
}



/* ****************** Access methods **************** */

/**
 * Gets the number of states of the variable.
 * @return the number of states of this node.
 */

public int getNumStates() {

  return numStates;
}


/**
 * gets the states of the variable.
 * @return the <code>Vector</code> of states of this node.
 */

public Vector getStates() {

  return states;
}


/**
 * Gets the name of a state.
 * @param i a number of state.
 * @return the name of state number <code>i</code>.
 */

public String getState(int i) {

  return ((String) states.elementAt(i));
}

/**
 * This method returns a String object that has to be 
 * printed when it is wanted to print the state of a 
 * FiniteState node. For example, when a database is saved...
 * @param i, the number of the state.
 * @return String.
 */

public String getPrintableState(int i){
    
   /*
   String name=((String) states.elementAt(i));
   try {
     int k = Integer.parseInt(name);
     return "\""+name+"\"";
   }
   catch (NumberFormatException e) {
     return name;
   }
    */

   if (i<this.numStates)
       return Integer.toString(i);
   else
       return null;

}

/************************ Modifiers *********************/

/**
 * Sets the number of states.
 * @param n the new number of states.
 */

public void setNumStates(int n) {

  numStates = n;
}


/**
 * Sets the states and the number of states of this node.
 * @param stat the <code>Vector</code> with the new states.
 */

public void setStates(Vector stat) {
  Vector theStates = new Vector();

  numStates = stat.size();
  for (int i=0; i<numStates; i++) {
     /*if (((String) stat.elementAt(i)).substring(0,1).equals("\"")) {
        theStates.addElement(((String) stat.elementAt(i)).substring(1,((String) stat.elementAt(i)).length()-1));
     }
     else {*/
        theStates.addElement(stat.elementAt(i));
     //}
  }
  states = theStates;
}


/**
 * Sets the transparency flag.
 * @param transType the type of transparency.
 */

public void setTransparency(int transType) {

  transparency = transType;
}

/**
 * Gets the transparency flag.
 */

public int getTransparency() {
  return transparency;
}


/**
 * Sets the states and the number of states of this node.
 * @param s an array of strings containing the states.
 */

public void setStates(String stat[]) {

  int i;

  numStates = stat.length;

  states = new Vector();
  for (i=0 ; i<numStates ; i++)
    states.addElement(stat[i]);
}


/**
 * For knowing the number of a state. This number is the
 * position of the string in the vector of states.
 * @param stat the name of the state.
 * @return the position of <code>stat</code> in the vector or -1 if
 *         the string is not found.
 */

public int getId(String stat) {

  int i;

  for (i=0 ; i<states.size() ; i++)
    if (stat.equals((String) states.elementAt(i)))
      return i;

  return -1;
}


/**
 * Gets the states of the node.
 * @return the states of the node as an array of <code>String</code>.
 */

public String[] getStringStates() {

  String nodeStates[];
  int i;

  nodeStates = new String[numStates];
  for (i=0 ; i<states.size() ; i++)
    nodeStates[i] = (String) states.elementAt(i);

  return nodeStates;
}


/**
 * Saves the information about a this object using the
 * given text output stream.
 * @param p a <code>PrintWriter</code> (the file).
 */

public void save(PrintWriter p) {

  String nodeStates[];
  int i;

  p.print("node "+getName()+"(finite-states)"+ " {\n");

  super.save(p);

  p.print("num-states = " + numStates + ";\n");
  p.print("states = (");
  nodeStates = getStringStates();

  for (i=0 ; i<numStates-1 ; i++)
    //p.print("\""+nodeStates[i] + "\" ");
    p.print(nodeStates[i] + " ");

  //p.print("\""+nodeStates[numStates-1] + "\");\n");
  p.print(nodeStates[numStates-1] + ");\n");

  p.print("}\n\n");
}


/**
 * This method creates a new node equal to this but the list of
 * links of its parents, children and siblings are empty.
 */

public Node copy() {

  FiniteStates n;

  n = (FiniteStates) super.copy();
  n.states = (Vector) states.clone();
  for (int i=0 ; i<states.size() ; i++) {
    String aux = new String((String) states.elementAt(i));
    n.states.setElementAt(aux,i);
  }

  return n;
}


/**
 * Gets the size of a hypothetical probability table
 * corresponding to the variables in the argument list; i.e. the product of
 * the number of cases of all the variables.
 * @param v a vector of <code>FinteStates</code> nodes.
 * @return the size of a hypothetical probability table
 * corresponding to the variables in <code>v</code>; i.e. the product of
 * the number of cases of all the variables.
 */

public static double getSize(Vector v) {

  int i;
  double s = 1.0;
  Node node;

  for (i=0 ; i<v.size() ; i++){
    node=(Node)v.elementAt(i);
    if(node instanceof FiniteStates){
      s *= ((FiniteStates)node).numStates;
    }
  }

  return s;
}


/**
 * Gets the size of a hypothetical probability table
 * corresponding to the variables in the argument list; i.e. the product of
 * the number of cases of all the variables.
 * @param list a <code>NodeList</code> of <code>FinteStates</code> nodes.
 * @return the size of a hypothetical probability table
 * corresponding to the variables in <code>list</code>; i.e. the product of
 * the number of cases of all the variables.
 */

public static double getSize(NodeList list) {

  int i;
  double s = 1.0;
  Node node;

  for (i=0 ; i<list.size() ; i++){
    node=(Node)list.elementAt(i);
    if(node instanceof FiniteStates){
       s *= ((FiniteStates)node).numStates;
    }
  }

  return s;
}


/**
 * Gets the size of a hypothetical probability table
 * corresponding to the variables in the argument list,
 * except for those variables which are also in the second argument;
 * i.e. the product of the number of cases of all the variables
 * except for those maintained fixed.
 * @param v a vector of <code>FinteStates</code> nodes.
 * @param fixed a vector of <code>FinteStates</code> nodes.
 * @return the size of a hypothetical probability table
 * corresponding to the variables in <code>v</code> minus
 * the variables in <code>fixed</code>; i.e. the product of
 * the number of cases of the variables.
 */

public static double getSize(Vector v, Vector fixed) {

  int i;
  double s = 1.0;
  FiniteStates var;

  for (i=0 ; i<v.size() ; i++) {
    var = ((FiniteStates)v.elementAt(i));
    if (!fixed.contains(var))
      s *= var.numStates;
  }
  return s;
}


/**
 * Copies this node.
 * @param byTitle indicates if the node is displayes by title
 * (<code>true</code>) or by name (<code>false</code>).
 * @return the new node.
 */

public FiniteStates copy(boolean byTitle) {

  FiniteStates n = new FiniteStates();
  n.setName(getName());
  n.setTitle(getTitle());
  n.setComment(getComment());
  n.setKindOfNode(getKindOfNode());
  n.setTypeOfVariable(getTypeOfVariable());
  n.setPosX(getPosX());
  n.setPosY(getPosY());
  n.setFont(getFont());
  n.setHigherAxis(getHigherAxis());
  n.setLowerAxis(getLowerAxis());

  n.states = (Vector) states.clone();
  for (int i=0 ; i<states.size() ; i++) {
    String aux = new String((String) states.elementAt(i));
    n.states.setElementAt(aux,i);
  }

  n.setNumStates(getNumStates());

  return n;
}



/**
 * Return index Of a element.
 * @param V indicates the Vector
 * @return the index.
 */

public int indexOf(Vector V) {

      int i;
      FiniteStates Aux;

      for (i=0; i<V.size(); i++)
         {
            Aux=(FiniteStates)V.elementAt(i);
            if (compareTo(Aux)==0)
	      return (i);
         }

      return (-1);
      }

/**
 * Returns the value of an undef value for this Node
 */

public double undefValue(){
  return UNDEFVALUE;
}

}  // End of class.

