/* Relation.java */

package elvira;

import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import elvira.*;
import elvira.potential.*;
import elvira.sensitivityAnalysis.GeneralizedPotentialTable;//Introducido por jruiz

/**
 * Class Relation. Implements relations among variables in a
 * graphical model.
 *
 * @since 1/12/2005
 */

public class Relation
    implements Cloneable, Serializable {

    static final long serialVersionUID = 9132610391389498261L;

  /**
   * A string that contains any comment about the relation.
   */
  private String comment;

  /**
   * A string with the name of the relation.
   */
  private String name;

  /**
   * Contains the potential of the relation.
   */
  private Potential values;

  /**
   * Another potential, used for propagating in Join Trees.
   */
  private Potential otherValues;

  /**
   * Indicates whether the relation is active or not
   */
  private boolean active;

  /**
   * Indicates whehter the relation is deterministic or not
   */
  private boolean deterministic;

  /**
   * Contains the kind of the relation.
   */
  private int kind;

  /**
   * Possible kinds of relations.
   */
  public static final int CONDITIONAL_PROB = 0;
  public static final int POTENTIAL = 1;
  public static final int UTILITY = 2;
  public static final int OR = 3;
  public static final int XOR = 4;
  public static final int CONSTRAINT = 5;
  public static final int CAUSAL_MAX = 6;
  public static final int GENERALIZED_MAX = 7;
  public static final int AND = 8;
  public static final int MIN = 9;
  public static final int UTILITY_COMBINATION = 10;
  public static final int RELEVATION_ARC = 11;
  public static final int CONSTRAINT_ARC = 12;
  
//public static final int SUM = 10;
//public static final int PRODUCT = 11;

  /**
   * Some used constants.
   */
  private static final String CONDITIONAL_PROB_STRING = "conditional-prob";
  private static final String POTENTIAL_STRING = "potential";
  private static final String UTILITY_STRING = "utility";
  private static final String UTILITY_COMBINATION_STRING =
      "utility-combination";
  private static final String CONSTRAINT_STRING = "constraint";

  /**
   * Contains the list of variables of the relation
   */
  private NodeList variables;

  /**
   * Creates a new empty relation. By default the relation
   * created cotains a conditional probability.
   */

  public Relation() {

    comment = new String();
    name = new String();
    active = true;
    kind = POTENTIAL;
    comment = "new";
    variables = new NodeList();
    deterministic = false;
  }

  /**
   * Creates a new relation of type conditional-prob with a
   * <code>PotentialTable</code>.
   * @param v a vector with the variables in the relation.
   */

  public Relation(Vector v) {

    comment = new String();
    name = new String();
    active = true;
    kind = POTENTIAL;
    comment = "new";
    variables = new NodeList(v);
    deterministic = false;
    Node n = (Node) v.elementAt(0);

    if (n.getKindOfNode() == Node.SUPER_VALUE) {
      v.remove(0);
      values = new UtilityPotential(v);
      kind = UTILITY_COMBINATION;

    }
    else if (n.getKindOfNode() == Node.UTILITY) {
      v.remove(0);
      values = new PotentialTable(v);
      kind = UTILITY;
    }
    else {
      if (n.getClass() == FiniteStates.class) {
        double x;
        x = 1 / ( (double) ( ( (FiniteStates) n).getNumStates()));
        values = new PotentialTable(v);
        ( (PotentialTable) values).setValue(x);
      }
      else if (n.getClass() == Continuous.class) {
        values = new PotentialContinuousPT();
      }

    }

  }

  /**
   * Creates a new relation of type conditional-prob with a
   * <code>PotentialTable</code>.
   * @param Node node. The node son of the relation
   */
  public Relation(Node node) {

    comment = new String();
    name = new String();
    active = true;
    kind = POTENTIAL;
    comment = "new";
    Vector v = new Vector();
    v.addElement(node);
    Vector par = node.getParentNodes().getNodes();
    for (int i = 0; i < par.size(); i++)
      v.addElement( (Node) par.elementAt(i));
    variables = new NodeList(v);
    deterministic = false;
    Node n = (Node) v.elementAt(0);

    if (n.getClass() == FiniteStates.class) {
      double x;
      x = 1 / ( (double) ( ( (FiniteStates) n).getNumStates()));
      values = new PotentialTable(v);
      ( (PotentialTable) values).setValue(x);
    }
    else if (n.getClass() == Continuous.class) {
      values = new PotentialContinuousPT();
    }
  }

  /**
   * Creates a new relation of type potential from a
   * given <code>Potential</code>. It will have the
   * same variables as the potential and the given potential
   * as values.
   *
   * @param pot The potential from which the relation is cretated.
   */

  public Relation(Potential pot) {

    comment = new String();
    name = new String();
    active = true;
    kind = POTENTIAL;
    comment = "new";

    variables = new NodeList(pot.getVariables());

    deterministic = false;

    values = pot;
  }

  /* *************** Access Methods ************* */

  /**
   * This method is used to get the list of variables.
   * @return the list of the nodes in the relation.
   */

  public NodeList getVariables() {

    return variables;
  }

  /**
   * This method is used to get the list of the parents of the relation.
   * @return the list of the parents in the relation.
   */

  public NodeList getParents() {

    NodeList newList = new NodeList(), list;
    Node node;
    int i;

    list = getVariables();
    for (i = 1; i < list.size(); i++) {
      node = list.elementAt(i);
      newList.insertNode(node);
    }
    return newList;
  }

  /**
   * Gets the potential of the relation.
   * @return The potential of the relation.
   */

  public Potential getValues() {

    return values;
  }

  /**
   * Gets the other potential of the relation.
   * @return The other potential of the relation
   */

  public Potential getOtherValues() {

    return otherValues;
  }

  /**
   * Gets the value of the active variable of the relation.
   * @return The value of the active variable.
   */

  public boolean getActive() {

    return active;
  }

  /**
   * Gets the kind of the relation.
   * @return the kind of the relation.
   */

  public int getKind() {

    return kind;
  }

  /**
   * Gets the name of the relation.
   * @return A string that contains the name of the relation.
   */

  public String getName() {

    return name;
  }

  /**
   * Gets the comment of the relation.
   * @return A string that contains the comment of the relation.
   */

  public String getComment() {

    return comment;
  }

  /* *************** Modifiers ************** */

  /**
   * Sets the "deterministic" property.
   */

  public void setDeterministic(boolean isDeterministic) {
    deterministic = isDeterministic;
  }

  /**
   * Sets the variables contained in parameter <code>nodes</code> according to
   * the order of this variables in parameter <code>names</code>.
   * @param names The list of names of the variables that are going to be set.
   * @param nodes The list of nodes that contain the variables that
   * are going to be set.
   */

  public void setVariablesFromNames(Vector names, NodeList nodes) {

    int i;
    Node n;

    variables = new NodeList();

    for (i = 0; i < names.size(); i++) {
      n = nodes.getNode( (String) names.elementAt(i));
      variables.insertNode(n);
    }
  }

  /**
   * Sets the variables equal to the argument list.
   * @param nl a <code>NodeList</code>.
   */

  public void setVariables(NodeList nl) {

    variables = nl;
  }

  /**
   * Sets the variables equal to a clone of the vector of nodes. Note that
   * the nodes are not cloned.
   * @param nodes a <code>Vector</code> of nodes.
   */

  public void setVariables(Vector nodes) {

    Node node;
    int i;

    variables = new NodeList();
    for (i = 0; i < nodes.size(); i++) {
      node = (Node) nodes.elementAt(i);
      variables.insertNode(node);
    }
  }

  /**
   * Sets the values equal to the argument potential.
   * @param p the new values.
   */

  public void setValues(Potential p) {

    values = p;
  }

  /**
   * Sets the other values equal to the argument potential.
   * @param p the new other values.
   */

  public void setOtherValues(Potential p) {

    otherValues = p;
  }

  /**
   * Sets the value of variable <code>comment</code>.
   * @param s a string that contains the comment.
   */

  public void setComment(String s) {

    comment = new String(s);
  }

  /**
   * Sets the value of the name.
   * @param s A string that contains the name of this relation.
   */

  public void setName(String s) {

    name = new String(s);
  }

  /**
   * Sets the kind of the relation.
   * @param s the kind of relation.
   */

  public void setKind(int s) {

    kind = s;
  }

  /**
   * Sets the kind of the relation.
   * @param s the kind of relation.
   */

  public void setKind(String s) {

    if (s.equals(CONDITIONAL_PROB_STRING))
      kind = CONDITIONAL_PROB;
    else
    if (s.equals(UTILITY_STRING))
      kind = UTILITY;
    else
    if (s.equals(CONSTRAINT_STRING))
      kind = CONSTRAINT;
    else
    if (s.equals(POTENTIAL_STRING))
      kind = POTENTIAL;
    else
    if (s.equals("Or"))
      kind = OR;
    else
    if (s.equals("CausalMax"))
      kind = CAUSAL_MAX;
    else
    if (s.equals("GeneralizedMax"))
      kind = GENERALIZED_MAX;
    else
    if (s.equals("Min"))
      kind = MIN;
    else
    if (s.equals("And"))
      kind = AND;
    else
    if (s.equals("Xor"))
      kind = XOR;
    else
    if (s.equals(UTILITY_COMBINATION_STRING))
      kind = UTILITY_COMBINATION;
      /*if (s.equals("Sum"))
          kind = SUM;
           else if (s.equals("Product"))
          kind = PRODUCT;*/
    else
      kind = POTENTIAL;

  }

  /**
   * Marks the relation as active or not.
   * @param act <code>true</code> for active.
   */

  public void setActive(boolean act) {

    active = act;
  }

  /**
   * Saves the relation.
   * @param p <code>PrintWriter</code> where the relation is saved.
   */

  public void save(PrintWriter p) {

    int i, j;

    p.print("relation ");
    i = getVariables().size();

    for (j = 0; j < i; j++)
      p.print( ( (Node) getVariables().elementAt(j)).getName() + " ");

    p.print("{ \n");

    p.print("comment = \"" + comment + "\";\n");

    if (kind != CONDITIONAL_PROB) {
      if (kind == POTENTIAL)
        p.print("kind-of-relation = " + POTENTIAL_STRING + ";\n");
      else
      if (kind == UTILITY)
        p.print("kind-of-relation = " + UTILITY_STRING + ";\n");
        //else if ((kind == SUM)||(kind==PRODUCT))
      else if (kind == UTILITY_COMBINATION)
        p.print("kind-of-relation = " + UTILITY_COMBINATION_STRING + ";\n");
      else
      if (kind == CONSTRAINT)
        p.print("kind-of-relation = " + CONSTRAINT_STRING + ";\n");

    }

    if (!active)
      p.print("active=false;\n");
    if (name.compareTo("") != 0)
      p.print("name-of-relation = " + name + ";\n");

    if (isDeterministic())
      p.print("deterministic=true;\n");
    else
      p.print("deterministic=false;\n");

      /*if (!(values==null)) {
           if (values.getClass().getName().equals("elvira.potential.PotentialTable"))
          ((PotentialTable) values).saveAsTable(p);
           else if (values.getClass().getName().equals("elvira.potential.PotentialTree"))
          ((PotentialTree) values).save(p);
        else if (values.getClass().getName().equals("elvira.potential.PotentialFunction"))
          ((PotentialFunction) values).save(p);
        else if (values.getClass().getName().equals("elvira.potential.CanonicalPotential"))
          ((CanonicalPotential) values).save(p);
        else if (values.getClass().getName().equals("elvira.potential.LogicalExpression"))
          ((LogicalExpression) values).save(p);
        else if (values.getClass().getName().equals("elvira.potential.UtilityPotential"))
          ((UtilityPotential) values).save(p);
         }*/

    if (! (values == null)) {
      values.save(p);     
    }

    p.print("}\n\n");
  }

  /**
   * Prints the relation to the standard output.
   */

  public void print() {

    int i, j;

    System.out.print("relation ");
    i = getVariables().size();

    for (j = 0; j < i; j++)
      System.out.print( ( (Node) getVariables().elementAt(j)).getName() + " ");

    System.out.print("{ \n");

    if (!comment.equals(""))
      System.out.print("comment = \"" + comment + "\";\n");

    if (kind != CONDITIONAL_PROB) {
      if (kind == POTENTIAL)
        System.out.print("kind-of-relation = " + POTENTIAL_STRING + ";\n");
      else
      if (kind == UTILITY)
        System.out.print("kind-of-relation = " + UTILITY_STRING + ";\n");
    }

    if (!active)
      System.out.print("active=false;\n");
    if (name.compareTo("") != 0)
      System.out.print("name-of-relation = " + name + ";\n");

    if (! (values == null)) {
      values.print();
    }

    System.out.print("}\n\n");
  }
  
   /**
    * Method for printing the domain of a potential
    */
   public void printDomain() {
      Node node;
      String type;
      
      switch(this.kind){
         case CONSTRAINT: type="Constraint";
                          break;
         case UTILITY: type="Utility";
                          break;
         default: type="Probability";
                          break;
      }
      
      System.out.print("pot(");
      for (int i = 0; i < variables.size(); i++) {
         node = (Node) variables.elementAt(i);
         System.out.print(" " + node.getName());
      }
      
      if (values != null){
         if (values.getClassName().equals("PotentialTree")){
            PotentialTree valTree=(PotentialTree)values;
            if(valTree.checkSize() == false){
	       valTree.updateSize();
            }
         }
         System.out.print(") --- global size: " + values.getSize());
      }
      else{
         System.out.print(") --- real size: "+variables.getSize());
      }
      System.out.println(" --- "+type);
   }  

  /**
   * Copies a <code>Relation</code>.
   * Depending on the parameter probabilities, the potential
   * is copied or not
   * @param probabilites a <code>boolean</code>
   * @return a copy of this relation.
   */

  public Relation copy(boolean probabilities) {

    Relation r;

    r = new Relation();
    r.comment = comment;
    r.name = name;
    if (probabilities) {
      if (values != null) {
        if ( (getKind() != Relation.UTILITY) &&
            (getKind() != Relation.UTILITY_COMBINATION)) {
          /*if(values.getClass() == PotentialTable.class) {
            PotentialTable pt = (PotentialTable) values.copy();
            pt.setVariables((Vector)values.getVariables().clone());
            r.setValues(pt);
                   }*/
          r.setValues(getValues().copy());
        }
        else {
          if (getKind() == Relation.CONSTRAINT) {
            r.values = ( (LogicalExpression) values).copy();
          }
          else
            r.values = values;
        }
      }
    }
    else
      r.values = null;

    r.variables = (NodeList) variables.copy();
    r.active = active;
    r.kind = kind;
    r.deterministic = deterministic;

    return r;
  }

  /**
   * Copies a <code>Relation</code>.
   * @return a copy of this relation.
   */

  public Relation copy() {
    return this.copy(true);
  }

  /**
   * This method tells us whether a node is contained in this relation.
   * @param nodeToFind the node to find in this relation.
   * @return <code>true</code> if <code>nodeToFind</code> is contained in this
   * relation, <code>false</code> in other case.
   */

  public boolean isInRelation(Node nodeToFind) {

    boolean isIn = false;
    int i = 0;
    String s = new String();

    while ( (i < this.variables.size()) && (!isIn)) {
      s = (this.variables.elementAt(i)).getName();
      if (s.equals(nodeToFind.getName()))
        isIn = true;
      else
        i++;
    }

    return isIn;
  }

  /**
   * This function tells us if a relation is contained in this relation.
   * @param r is the relation that we like to check
   * @return <code>true</code> if <code>r</code> is contained in this
   * relation, <code>false</code> in other case.
   */

  public boolean isContained(Relation r) {

    boolean value = true;
    int i = 0;

    while ( (value) && (i < r.variables.size()))
      if (!isInRelation(r.variables.elementAt(i)))
        value = false;
      else
        i++;

    return value;
  }

  /**
   * This method tell if this relation has the same
   * variables that the argument relation.
   * @param r Relation to compare with the one that receives the message.
   * @return <code>true</code> if the variables of the relations are the same.
   */

  public boolean isTheSame(Relation r) {

    int i = 0;

    if ( (r.variables.size() == this.variables.size()) && (this.isContained(r)))
      return true;
    else
      return false;
  }

  /**
   * This method obtains the union of this relation
   * and the one given as argument. The result of the union is
   * stored in this relation.
   * @param r the relation to join with this.
   */

  public void union(Relation r) {

    int j, q;

    for (j = 0; j < r.variables.size(); j++) {
      q = variables.getId(r.variables.elementAt(j));

      if (q == -1)
        variables.insertNode(r.variables.elementAt(j));
    }
  }

  /**
   * Calculates the intersection between this relation and
   * the argument one.
   * @param r the relation to intersect with this.
   * @return the intersection.
   */

  public Relation intersection(Relation r) {

    Relation intersection = new Relation();
    int i = 0, j, sizel1, sizel2;
    boolean getOut;

    sizel1 = variables.size();
    sizel2 = r.variables.size();

    // The first while controls the nodes of the first relation

    while (i < sizel1) {
      getOut = false;
      j = 0;

      /* Check that the current node of the first list is in the second
         with the next while */

      while ( (!getOut) && (j < sizel2)) {
        if (variables.elementAt(i).getName().equals(r.variables.elementAt(j).
            getName())) {
          intersection.variables.insertNode(variables.elementAt(i));
          getOut = true;
        }
        j++;
      }
      i++;
    }
    return intersection;
  }

  /**
   * Restricts the relation to the variables in the argument list.
   * NOTE: <code>otherValues</code> is set to <code>null</code>.
   * @param set a list of variables.
   */

  public void restrictToVariables(NodeList set) {

    NodeList nl;
    Potential pot;

    nl = variables.intersection(set);
    pot = (Potential)this.getValues();

    if (nl.size() == 0) {
      variables = new NodeList();
      if (pot.getClassName().equals("PotentialTable")) {
        values = new PotentialTable();
        otherValues = new PotentialTable();
      }
      else

      //Introducido por jruiz
      if (pot.getClassName().equals("GeneralizedPotentialTable")) {
        values = new GeneralizedPotentialTable();
        otherValues = new GeneralizedPotentialTable();
      }
      else//Fin introducido por jruiz

      if (pot.getClassName().equals("PotentialTree")) {
        values = new PotentialTree();
        otherValues = new PotentialTree();
      }
      else
      if (pot.getClassName().equals("PotentialMTree")) {
        values = new PotentialMTree();
        otherValues = new PotentialMTree();
      }
      else {
        System.out.println(pot.getClass().getName() +
                           " is not implemented in Relation.restricToVariables");
        System.exit(0);
      }
    }
    else {
      if (values != null)
        if (pot.getClassName().equals("PotentialTable")) {
          values = ( (PotentialTable) values).marginalizePotential(nl.toVector());
        }
        else

        //Introducido por jruiz
        if (pot.getClassName().equals("GeneralizedPotentialTable")) {
          values = ( (GeneralizedPotentialTable) values).marginalizePotential(nl.toVector());
        }
        else//Fin introducido por jruiz

        if (pot.getClassName().equals("PotentialTree")) {
          values = ( (PotentialTree) values).marginalizePotential(nl.toVector());
        }
        else
        if (pot.getClassName().equals("PotentialMTree")) {
          values = ( (PotentialMTree) values).marginalizePotential(nl.toVector());
        }
        else {
          System.out.println(pot.getClass().getName() +
              " is not implemented in Relation.restrictToVariables");
          System.exit(0);
        }

      otherValues = null;
      variables = nl;
    }
  }

  /**
   * Restricts a relation to a given configuration.
   * @param conf the <code>Configuration</code>.
   * @return the restricted relation.
   */

  public Relation restrict(Configuration conf) {

    Relation newRel;

    newRel = new Relation();

    newRel.setValues(getValues().restrictVariable(conf));

    if (getOtherValues() != null)
      newRel.setOtherValues(getOtherValues().restrictVariable(conf));

    newRel.setVariables(newRel.getValues().getVariables());

    return newRel;
  }

  /**
   * Restricts a relation to a given configuration except a goal variable.
   * @param conf the <code>Configuration</code>.
   * @param goalVar the variable that will not be restricted.
   * @return the restricted relation.
   */

  public Relation restrict(Configuration conf, Node goalVar) {

    Relation newRel;

    newRel = new Relation();

    newRel.setValues(getValues().restrictVariable(conf, goalVar));

    if (getOtherValues() != null)
      newRel.setOtherValues(getOtherValues().restrictVariable(conf, goalVar));

    newRel.setVariables(newRel.getValues().getVariables());

    return newRel;
  }

  /**
   * Tells if this relation contains a conditional potential or not.
   * @return <code>true</code> if this relation contains a conditional
   * potential and <code>false</code> otherwise.
   */

  public boolean isConditional() {

    if (getKind() == CONDITIONAL_PROB)
      return true;
    else
      return false;
  }

  /**
   * Tells whether the relation is deterministic or not
   */

  public boolean isDeterministic() {
    return deterministic;
  }

  /**
   * Tells if a utility node belongs to this relation.
   * @return <code>true</code> if the utility node takes part in this relation.
   */

  public boolean withUtilityNode() {

    int i;
    NodeList list;
    Node node;

    list = getVariables();
    for (i = 0; i < list.size(); i++) {
      node = list.elementAt(i);
      if (node.getKindOfNode() == Node.UTILITY)
        return true;
    }
    return false;
  }

  /**
   * Tells if at least one continuous variable belongs to this relation.
   * @return <code>true</code> if a continuous variable takes part in this relation.
   */

  public boolean withContinuousVariable() {

    int i;
    NodeList list;
    Node node;

    list = getVariables();
    for (i = 0; i < list.size(); i++) {
      node = list.elementAt(i);
      if (node.getTypeOfVariable() == Node.CONTINUOUS)
        return true;
    }
    return false;
  }

  /**
   * Gives the list of non-utility nodes.
   * @return the non-utility nodes.
   */

  public NodeList chanceAndDecisionNodes() {

    NodeList newList = new NodeList(), list;
    Node node;
    int i;

    
        
    list = getVariables();
    
    
    /*for (i=1 ; i < list.size() ; i++) {
      node = list.elementAt(i);
      newList.insertNode(node);
      }*/
      
    for (i=0 ; i < list.size() ; i++) {
      node = list.elementAt(i);
      if (node.getKindOfNode() != node.UTILITY) {
        newList.insertNode(node);
      }
    }
    
    

    return newList;
  }
  
  /**
   * Gives the list of non-utility nodes. Gives all the variables of the relation if the node is a super value node.
   * @return the non-utility nodes.
   */

  public NodeList chanceAndDecisionNodesOfUtilityRelation() {

    NodeList newList = new NodeList(), list;
    Node node;
    int i;

    list = getVariables();

    if (list.size() == 1) {
      //return null;
      newList = new NodeList();
    }
    else {
      /* MANUEL LUQUE: Voy a hacer que devuelva el resto de la lista tanto para relaciones de
       nodos de utilidad como de nodos supervalor */
      Node firstNode;
      firstNode = list.elementAt(0);
      if ( (firstNode.getKindOfNode() == Node.UTILITY) ||
          (firstNode.getKindOfNode() == Node.SUPER_VALUE)) {
        for (i = 1; i < list.size(); i++) {
          node = list.elementAt(i);
          newList.insertNode(node);
        }
      }

    }
    return newList;
  }

} // End of class.
