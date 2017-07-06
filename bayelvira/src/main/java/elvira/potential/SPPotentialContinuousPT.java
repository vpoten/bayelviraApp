package elvira.potential;

import java.util.Vector;
import java.util.ArrayList;
import java.util.Hashtable;
import java.io.*;

import elvira.*;
import elvira.tools.LinearFunction;

/**
 *
 * @author asc
 * @since 19/7/2011
 */
public class SPPotentialContinuousPT extends Potential {

    /**
     * The tree associated with the potential.
     */
    SPContinuousProbabilityTree values;
    /**
     * Size of the potential (number of leaves in the tree).
     */
    long size;
    /**
     * Types of variables.
     */
    public static int CONTINUOUS = 0;
    public static int FINITE_STATES = 1;

    /**
     * Creates an empty object.
     */
    public SPPotentialContinuousPT() {

        variables = new Vector();
        values = new SPContinuousProbabilityTree();
        size = 0;
    }

    /**
     * Creates a new <code>SPPotentialContinuousPT</code> with an empty tree.
     * @param vars variables that the potential will contain.
     */
    public SPPotentialContinuousPT(Vector vars) {

        variables = (Vector) vars.clone();
        values = new SPContinuousProbabilityTree();
        if (vars.size() > 0) {
            values.setVar((Node) vars.elementAt(0));
        }
        size = 0;
    }

    /**
     * Creates a new <code>SPPotentialContinuousPT</code> from a
     * PotentialContinuousPT.
     * @param the original potential.
     */
    public SPPotentialContinuousPT(PotentialContinuousPT pot) {

        variables = (Vector) pot.getVariables().clone();
        values = SPContinuousProbabilityTree.transform(pot.getTree());
        size = pot.size;
    }

    /**
     * Creates a new <code>SPPotentialContinuousPT</code> with an empty tree.
     * @param vars variables that the potential will contain,
     * given as a <code>NodeList</code>.
     */
    public SPPotentialContinuousPT(NodeList vars) {

        variables = (Vector) vars.getNodes().clone();
        values = new SPContinuousProbabilityTree(0);
        if (vars.size() > 0) {
            values.setVar(vars.elementAt(0));
        }
        size = 0;
    }

    /**
     * Creates a new potential with only one continuous variable,
     * and whose tree contains that variable as root node
     * and all its children being equal to a constant value given
     * as argument.
     * @param variable a <code>Continuous</code> variable.
     * @param cp a vector of cut points to determine the children
     * of the continuous variable.
     * @param x the double value to store at the leaves.
     */
    public SPPotentialContinuousPT(Continuous variable, Vector cp, double x) {

        variables = new Vector();
        variables.addElement(variable);
        size = cp.size() - 1;
        values = new SPContinuousProbabilityTree(variable, cp, x);
    }

    /**
     * Assigns a tree to the potential.
     * @param tree the <code>SPContinuousProbabilityTree</code> to be assigned.
     */
    public void setTree(SPContinuousProbabilityTree tree) {

        values = tree;
        size = tree.getSize();
    }

    /**
     * Gets the tree associated with the potential.
     * @return the <code>SPContinuousProbabilityTree</code> associated with
     * the potential.
     */
    public SPContinuousProbabilityTree getTree() {

        return values;
    }

    /**
     * Gets the size of the potential.
     * @return the number of values (<code>size</code>) of the potential.
     */
    public long getSize() {

        return size;
    }

    /**
     * Gets the value for a configuration. First, the density stored in the
     * leaf corresponding to the argument configuration is retieved, and then,
     * it is evaluated in the corresponding variables.
     *
     * @param conf a configuration. It must be of class
     * <code>ContinuousConfiguration</code>.
     * @return the value corresponding to <code>Configuration conf</code>.
     */
    public double getValue(Configuration conf) {

        double retValue = 0.0;
        ArrayList<ArrayList<MixtExpDensity>> density;

        density = values.getProb((ContinuousConfiguration) conf);

        for (int k = 0; k < density.size(); k++) {
            double itValue = 1.0;
            for (int k2 = 0; k2 < density.get(k).size(); k2++) {
                itValue = itValue * density.get(k).get(k2).getValue((ContinuousConfiguration) conf);
            }
            retValue = retValue + itValue;
        }
        return (retValue);
    }

    /**
     * Gets the value for a configuration. First, the density stored in the
     * leaf corresponding to the argument configuration is retrieved, and then,
     * it is evaluated in the corresponding variables.
     *
     * @param conf a <code>ContinuousConfiguration</code>.
     * @return the value corresponding to <code>conf</code>.
     */
    public double getValue(ContinuousConfiguration conf) {

        return getValue((Configuration) conf);
    }

    /**
     * Restricts the potential to a given configuration.
     * @param conf the restricting <code>Configuration</code>. This
     * configuration must be of class <code>ContinuousConfiguration</code>.
     * @return a new <code>SPPotentialContinuousPT</code> where variables
     * in <code>conf</code> have been instantiated to their values
     * in <code>conf</code>.
     */
    public Potential restrictVariable(Configuration conf) {

        Vector aux;
        Node temp;
        SPPotentialContinuousPT pot;
        SPContinuousProbabilityTree tree;
        ContinuousConfiguration auxConf, auxConf2;
        int type, i, p, s, intValue;
        double doubleValue;

        auxConf = (ContinuousConfiguration) conf;
        s = variables.size();
        aux = new Vector(s); // New list of variables.
        tree = getTree();    // tree will be the new tree

        for (i = 0; i < s; i++) {
            temp = (Node) variables.elementAt(i);
            type = temp.getTypeOfVariable();

            if (type == FINITE_STATES) {
                p = auxConf.indexOf((FiniteStates) temp);
            } else {
                p = auxConf.getIndex((Continuous) temp);
            }

            if (p == -1) {// If it is not in conf, add to the new list.
                aux.addElement(temp);
            } else {     // Otherwise, restrict the tree to it.
                auxConf2 = new ContinuousConfiguration();
                if (type == FINITE_STATES) {
                    intValue = auxConf.getValue(p);
                    auxConf2.insert((FiniteStates) temp, intValue);
                } else {
                    doubleValue = auxConf.getContinuousValue(p);
                    auxConf2.insert((Continuous) temp, doubleValue);
                }
                tree = tree.restrict(auxConf2);
            }
        }

        pot = new SPPotentialContinuousPT(aux);
        pot.setTree(tree);

        return pot;
    }

    /**
     * Restricts the potential to a given configuration, except a
     * variable that is given as argument, whose value is not
     * considered.
     * @param conf the restricting <code>Configuration</code>. This
     * configuration must be of class <code>ContinuousConfiguration</code>.
     * @param goalVariable the node that will not be considered for
     * restriction.
     * @return a new <code>SPPotentialContinuousPT</code> where variables
     * in <code>conf</code> have been instantiated to their values in
     * <code>conf</code>.
     */
    public Potential restrictVariable(Configuration conf, Node goalVariable) {

        Vector aux;
        Node temp;
        SPPotentialContinuousPT pot;
        SPContinuousProbabilityTree tree;
        ContinuousConfiguration auxConf, auxConf2;
        int type, i, p, s, intValue;
        double doubleValue;

        auxConf = (ContinuousConfiguration) conf;
        s = variables.size();
        aux = new Vector(s); // New list of variables.
        tree = getTree();    // tree will be the new tree

        for (i = 0; i < s; i++) {
            temp = (Node) variables.elementAt(i);
            type = temp.getTypeOfVariable();

            if (type == FINITE_STATES) {
                p = auxConf.indexOf((FiniteStates) temp);
            } else {
                p = auxConf.getIndex((Continuous) temp);
            }


            // If it is not in conf, or is the distinguished
            // variable, add to the new list.
            if ((p == -1) || (temp == goalVariable)) {
                aux.addElement(temp);
            } else {     // Otherwise, restrict the tree to it.
                auxConf2 = new ContinuousConfiguration();
                if (type == FINITE_STATES) {
                    intValue = auxConf.getValue(p);
                    auxConf2.insert((FiniteStates) temp, intValue);
                } else {
                    doubleValue = auxConf.getContinuousValue(p);
                    auxConf2.insert((Continuous) temp, doubleValue);
                }

                tree = tree.restrict(auxConf2);
            }
        }

        pot = new SPPotentialContinuousPT(aux);
        pot.setTree(tree);

        return pot;
    }

    /**
     * Combines this potential with the argument. The argument <code>p</code>
     * must be a <code>SPPotentialContinuousPT</code>.
     * @param p a <code>Potential</code>.
     * @returns a new <code>SPPotentialContinuousPT</code> consisting of the
     * combination of <code>p</code> and this <code>Potential</code>.
     */
    public Potential combine(Potential p) {

        Vector v, v1, v2;
        Node aux;
        int i, nv;
        SPPotentialContinuousPT pot;
        double x;
        SPContinuousProbabilityTree tree, tree1, tree2;

        if ((p.getClass() == PotentialTable.class)
                || (p.getClass() == PotentialTree.class)
                || (p.getClass() == SPPotentialContinuousPT.class)) {
            v1 = variables;   // Variables of this potential.
            v2 = p.variables; // Variables of the argument.
            v = new Vector(); // Variables of the new potential.

            for (i = 0; i < v1.size(); i++) {
                aux = (Node) v1.elementAt(i);
                v.addElement(aux);
            }
            for (i = 0; i < v2.size(); i++) {
                aux = (Node) v2.elementAt(i);
                if (v1.indexOf(aux) < 0) {
                    v.addElement(aux);
                }
            }

            // The new Potential.
            pot = new SPPotentialContinuousPT(v);

            tree1 = getTree(); // Tree of this potential.
            if (p.getClass() == SPPotentialContinuousPT.class) {
                tree2 = ((SPPotentialContinuousPT) p).getTree(); // Tree of the argument.
            } else if (p.getClass() == PotentialTree.class) {
                tree2 = null;
            } else if (p.getClass() == PotentialTable.class) {
                tree2 = null;
            } else {
                tree2 = null;
            }

            tree = SPContinuousProbabilityTree.combine(tree1, tree2); // The new tree
            pot.setTree(tree);
        } else {
            System.out.println("Error in Potential PotentialContinuousPT.combine(Potential p): argument p was not a PotentialTable nor a PotentialTree nor a PotentialContinuousPT");
            System.exit(1);
            pot = this;
        }
        return pot;
    }

    /**
     * Combines this potential with the <code>PotentialContinuousPT</code>
     * of the argument.
     * @param p a <code>SPPotentialcontinuousPT</code>.
     * @returns a new <code>SPPotentialContinuousPT</code> consisting of the
     * combination of <code>p (SPPotentialContinuousPT)</code> and this
     * <code>SPPotentialContinuousPT</code>.
     */
    public SPPotentialContinuousPT combine(SPPotentialContinuousPT p) {

        return (SPPotentialContinuousPT) combine((Potential) p);
    }

    /**
     * Removes a list of variables by adding over all their states, or
     * integrating if the variable is continuous.
     * @param vars <code>Vector</code> of <code>Node</code>.
     * @return a new <code>SPPotentialContinuousPT</code> with the
     * result of the operation.
     */
    public SPPotentialContinuousPT addVariable(Vector vars) {

        Vector aux;
        Node var1, var2;
        int i, j, type;
        boolean found;
        SPPotentialContinuousPT pot;
        SPContinuousProbabilityTree tree;


        aux = new Vector(); // New list of variables.
        for (i = 0; i < variables.size(); i++) {
            var1 = (Node) variables.elementAt(i);
            found = false;

            for (j = 0; j < vars.size(); j++) {
                var2 = (Node) vars.elementAt(j);
                if (var1 == var2) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                aux.addElement(var1);
            }
        }
        if (aux.size() == 0) {
            pot = new SPPotentialContinuousPT();
        } else {
            pot = new SPPotentialContinuousPT(aux); // The new potential.
        }
        tree = values;

        for (i = 0; i < vars.size(); i++) {
            var1 = (Node) vars.elementAt(i);
            if (var1.getTypeOfVariable() == FINITE_STATES) {
                tree = tree.addVariable((FiniteStates) var1);
            } else {
                tree = tree.addVariable((Continuous) var1);
            }
        }

        pot.setTree(tree);

        return pot;
    }

    /**
     * Removes the argument variable summing over all its values.
     * @param var a <code>Node</code> variable.
     * @return a new <code>SPPotentialContinuousPT</code> with the
     * result of the deletion.
     */
    public Potential addVariable(Node var) {

        Vector v;
        SPPotentialContinuousPT pot;


        v = new Vector();
        v.addElement(var);
        pot = addVariable(v);

        return pot;
    }

    /**
     * Removes the argument variable integrating over it.
     * @param var a <code>Continuous</code> variable.
     * @return a new <code>SPPotentialContinuousPT</code> with the
     * result of the deletion.
     */
    public Potential addVariable(Continuous var) {

        Vector v;
        SPPotentialContinuousPT pot;

        v = new Vector();
        v.addElement(var);

        pot = addVariable(v);

        return pot;
    }

    /**
     * Marginalizes a <code>SPPotentialContinuousPT</code> to a list of variables.
     * It is equivalent to remove the other variables.
     * @param vars a vector of variables.
     * @return a new <code>SPPotentialContinuousPT</code> with the marginal.
     * @see addVariable(Vector vars)
     */
    public Potential marginalizePotential(Vector vars) {

        Vector v;
        int i, j;
        boolean found;
        Node var1, var2;
        SPPotentialContinuousPT pot;

        v = new Vector(); // List of variables to remove
        // (those not in vars).
        for (i = 0; i < variables.size(); i++) {
            var1 = (Node) variables.elementAt(i);
            found = false;

            for (j = 0; j < vars.size(); j++) {
                var2 = (Node) vars.elementAt(j);
                if (var1.equals(var2)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                v.addElement(var1);
            }
        }

        pot = addVariable(v);

        return pot;
    }

    /**
     * Normalizes the values of this potential. By now it is restricted
     * to potentials with a single continuous variable.
     * The object is modified.
     */
    public void normalize() {

        values.normalizeLeaves((Node) variables.elementAt(0));
    }

    /**
     * Prints the potential to the standard output.
     */
    public void print() {

        int i;

        System.out.print("Variables of potential:");

        for (i = 0; i < variables.size(); i++) {
            System.out.print(" " + ((Node) (variables.elementAt(i))).getName());
        }

        System.out.println("\nValues:");

        if (values != null) {
            values.print();
        }
    }

    /**
     * Save the potential into the file represented by the
     * <code>PrintWriter p</code>.
     * @param p is the <code>PrintWriter</code>
     */
    public void saveResult(PrintWriter p) {

        int i;

        p.print("Variables of potential:");

        for (i = 0; i < variables.size(); i++) {
            p.print(" " + ((Node) (variables.elementAt(i))).getName());
        }

        p.println("\nValues:");

        if (values != null) {
            values.save(p);
        }
    }

    /**
     * Saves the potential to a file in R format. Saves just the tree.
     * This method only works for potentials with one variable.
     * @param p the <code>PrintWriter</code> where the potential will be written.
     */
    public void saveR(PrintWriter p) {

        String name = ((Node) this.variables.elementAt(0)).getName();

        p.println("f"+name+" = function("+name+") {");
        p.println("result = "+name+";");
        values.saveR(p,"");

        p.println("return(result);");
        p.print("}\n");
    }

    /**
     * Copies this potential.
     * @return a copy of this <code>SPPotentialContinuousPT</code>.
     */
    public Potential copy() {

        SPPotentialContinuousPT pot;

        pot = new SPPotentialContinuousPT(variables);
        pot.size = size;
        pot.values = values.copy();

        return pot;
    }

    /**
     * Expands the terms in the leaves of the tree.
     * The potential is modified.
     */
    public void expand() {

        values.expand();
    }
}
