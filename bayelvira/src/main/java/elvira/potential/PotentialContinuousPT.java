/* PotentialContinuousPT.java */
package elvira.potential;

import java.util.Vector;
import java.util.Hashtable;
import java.io.*;

import elvira.*;
import elvira.tools.LinearFunction;

/**
 * Implementation of class <code>PotentialContinuousPT</code>. A potential
 * represented by a continuous probability tree, where both discrete and
 * continuous variables can be simultaneously found.
 *
 * @since 19/7/2011
 */
public class PotentialContinuousPT extends Potential {

    static final long serialVersionUID = 758343096085815964L;
    /**
     * Tre tree associated with the potential.
     */
    ContinuousProbabilityTree values;
    /**
     * Size of the potential (number of leaves in the tree).
     */
    long size;

    /*
     * Number of exponential terms
     */
    int numTerms;
    /*
     * Number of splits
     */
    int numSplits;
    /**
     * A string that contains any comment about the relation.
     */
    String comment;
    /**
     * Types of variables.
     */
    public static int CONTINUOUS = 0;
    public static int FINITE_STATES = 1;

    /**
     * Creates an empty object.
     */
    public PotentialContinuousPT() {

        variables = new Vector();
        values = new ContinuousProbabilityTree();
        size = 0;
    }

    /**
     * Creates a new <code>PotentialContinuousPT</code> with an empty tree.
     * @param vars variables that the potential will contain.
     */
    public PotentialContinuousPT(Vector vars) {

        variables = (Vector) vars.clone();
        values = new ContinuousProbabilityTree();
        if (vars.size() > 0) {
            values.setVar((Node) vars.elementAt(0));
        }
        size = 0;
    }

    /**
     * Creates a new <code>PotentialContinuousPT</code> with an empty tree.
     * @param vars variables that the potential will contain,
     * given as a <code>NodeList</code>.
     */
    public PotentialContinuousPT(NodeList vars) {

        variables = (Vector) vars.getNodes().clone();
        values = new ContinuousProbabilityTree(0);
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
    public PotentialContinuousPT(Continuous variable, Vector cp, double x) {

        variables = new Vector();
        variables.addElement(variable);
        size = cp.size() - 1;
        values = new ContinuousProbabilityTree(variable, cp, x);
    }

    /**
     * Constructs a <code>PotentialContinuousPT</code> from a
     * <code>PotentialTree</code>.
     *
     * @param <code>pot</code> the <code>PotentialTree</code> to be transformed
     * into a <code>PotentialContinuousPT</code>.
     */
    public PotentialContinuousPT(PotentialTree pot) {

        this(pot.getVariables());

        ContinuousProbabilityTree cpt;

        cpt = new ContinuousProbabilityTree(pot.getTree());

        setTree(cpt);
    }

    /**
     * Constructs a <code>PotentialContinuousPT</code> from a
     * <code>PotentialTable</code>.
     *
     * @param <code>pot</code> the <code>PotentialTable</code> to be transformed
     * into a <code>PotentialContinuousPT</code>.
     */
    public PotentialContinuousPT(PotentialTable pot) {

        this(new PotentialTree(pot));
    }

    /**
     * Creates a new <code>PotentialContinuousPT</code> with a list of variables
     * and a continuous probability tree. This probability tree is not cloned, but
     * directly assigned.
     * @param vars variables that the potential will contain,
     * @ tree the continuous probability tree
     * given as a <code>NodeList</code>.
     */
    public PotentialContinuousPT(NodeList vars, ContinuousProbabilityTree tree) {

        variables = (Vector) vars.getNodes().clone();
        values = tree;
        size = tree.getSize();
    }

    /**
     * Assigns a tree to the potential.
     * @param tree the <code>ContinuousProbabilityTree</code> to be assigned.
     */
    public void setTree(ContinuousProbabilityTree tree) {

        values = tree;
        size = tree.getSize();
    }

    /**
     * Gets the tree associated with the potential.
     * @return the <code>ContinuousProbabilityTree</code> associated with
     * the potential.
     */
    public ContinuousProbabilityTree getTree() {

        return values;
    }

    public String getComment() {

        return comment;
    }

    public void setComment(String a) {

        comment = a;
    }

    public boolean isUnity() {

        return values.isUnity();
    }

    /**
     * Sets the number of exponentials terms
     */
    public void setNumTerms(int n) {

        numTerms = n;

    }//End of method

    /**
     * Sets the number of splits of the domain of continuous variables
     */
    public void setNumSplits(int n) {

        numSplits = n;

    }//End of method

    /**
     * Gets the number of terms of the ContinuousProbabilityTree
     *
     */
    public int getNumTerms() {


        return numTerms;
    }

    /**
     * Gets the number of splits of the ContinuousProbabilityTree
     *
     */
    public int getNumSplits() {

        return numSplits;
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

        MixtExpDensity density;
        density = values.getProb((ContinuousConfiguration) conf);

        return (density.getValue((ContinuousConfiguration) conf));
    }

    /**
     * Gets the value for a configuration. First, the density stored in the
     * leaf corresponding to the argument configuration is retieved, and then,
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
     * @return a new <code>PotentialContinuousPT</code> where variables
     * in <code>conf</code> have been instantiated to their values
     * in <code>conf</code>.
     */
    public Potential restrictVariable(Configuration conf) {

        Vector aux;
        Node temp;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree tree;
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

            if (p == -1) // If it is not in conf, add to the new list.
            {
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

        pot = new PotentialContinuousPT(aux);
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
     * @return a new <code>PotentialContinuousPT</code> where variables
     * in <code>conf</code> have been instantiated to their values in
     * <code>conf</code>.
     */
    public Potential restrictVariable(Configuration conf, Node goalVariable) {

        Vector aux;
        Node temp;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree tree;
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

        pot = new PotentialContinuousPT(aux);
        pot.setTree(tree);

        return pot;
    }

    /**
     * Combines this potential with the argument. The argument <code>p</code>
     * must be a <code>PotentialContinuousPT</code>.
     * @param p a <code>Potential</code>.
     * @returns a new <code>PotentialContinuousPT</code> consisting of the
     * combination of <code>p</code> and this <code>Potential</code>.
     */
    public Potential combine(Potential p) {

        Vector v, v1, v2;
        Node aux;
        int i, nv;
        PotentialContinuousPT pot;
        double x;
        ContinuousProbabilityTree tree, tree1, tree2;

        if ((p.getClass() == PotentialTable.class)
                || (p.getClass() == PotentialTree.class)
                || (p.getClass() == PotentialContinuousPT.class)) {
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
            pot = new PotentialContinuousPT(v);

            tree1 = getTree(); // Tree of this potential.
            if (p.getClass() == PotentialContinuousPT.class) {
                tree2 = ((PotentialContinuousPT) p).getTree(); // Tree of the argument.
            } else if (p.getClass() == PotentialTree.class) {
                tree2 = new ContinuousProbabilityTree(((PotentialTree) p).getTree());
            } else if (p.getClass() == PotentialTable.class) {
                tree2 = new ContinuousProbabilityTree((new PotentialTree(p)).getTree());
            } else {
                tree2 = null;
            }
            tree = ContinuousProbabilityTree.combine(tree1, tree2, 1); // The new tree (simplifiying the MixtExpdensities in the leaves).
            pot.setTree(tree);
        } else {
            System.out.println("Error in Potential PotentialContinuousPT.combine(Potential p): argument p was not a PotentialTable nor a PotentialTree nor a PotentialContinuousPT");
            System.exit(1);
            pot = this;
        }
        return pot;
    }

    /**
     * Combines this potential with the argument. The argument <code>p</code>
     * must be a <code>PotentialContinuousPT</code>.
     * @param p a <code>Potential</code>.
     * @param flag an integer indicating if the MixtExpDensities in the leaves must be simplified
     * @returns a new <code>PotentialContinuousPT</code> consisting of the
     * combination of <code>p</code> and this <code>Potential</code>.
     */
    public Potential combine(Potential p, int flag) {

        Vector v, v1, v2;
        Node aux;
        int i, nv;
        PotentialContinuousPT pot;
        double x;
        ContinuousProbabilityTree tree, tree1, tree2;

        if (flag == 0) {
            return combine(p);
        } else {
            if ((p.getClass() == PotentialTable.class)
                    || (p.getClass() == PotentialTree.class)
                    || (p.getClass() == PotentialContinuousPT.class)) {
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
                pot = new PotentialContinuousPT(v);

                tree1 = getTree(); // Tree of this potential.
                if (p.getClass() == PotentialContinuousPT.class) {
                    tree2 = ((PotentialContinuousPT) p).getTree(); // Tree of the argument.
                } else if (p.getClass() == PotentialTree.class) {
                    tree2 = new ContinuousProbabilityTree(((PotentialTree) p).getTree());
                } else if (p.getClass() == PotentialTable.class) {
                    tree2 = new ContinuousProbabilityTree((new PotentialTree(p)).getTree());
                } else {
                    tree2 = null;
                }
                tree = ContinuousProbabilityTree.combine(tree1, tree2, flag); // The new tree.
                pot.setTree(tree);
            } else {
                System.out.println("Error in Potential PotentialContinuousPT.combine(Potential p): argument p was not a PotentialTable nor a PotentialTree nor a PotentialContinuousPT");
                System.exit(1);
                pot = this;
            }

            return pot;
        }
    }

    /**
     * Combines this potential with the <code>PotentialContinuousPT</code>
     * of the argument.
     * @param p a <code>PotentialcontinuousPT</code>.
     * @returns a new <code>PotentialContinuousPT</code> consisting of the
     * combination of <code>p (PotentialContinuousPT)</code> and this
     * <code>PotentialContinuousPT</code>.
     */
    public PotentialContinuousPT combine(PotentialContinuousPT p) {

        return (PotentialContinuousPT) combine((Potential) p);
    }

    /**
     * Combines this potential with the <code>PotentialContinuousPT</code>
     * of the argument.
     * @param p a <code>PotentialcontinuousPT</code>.
     * @param flag an integer indicating if the MixtExpDensity in the leaves must be simplified
     * @returns a new <code>PotentialContinuousPT</code> consisting of the
     * combination of <code>p (PotentialContinuousPT)</code> and this
     * <code>PotentialContinuousPT</code>.
     */
    public PotentialContinuousPT combine(PotentialContinuousPT p, int flag) {

        if (flag == 0) {
            return (PotentialContinuousPT) combine((Potential) p);
        } else {
            return (PotentialContinuousPT) combine((Potential) p, flag);
        }
    }

    /**
     * Removes a list of variables by adding over all their states, or
     * integrating if the variable is continuous.
     * @param vars <code>Vector</code> of <code>Node</code>.
     * @return a new <code>PotentialContinuousPT</code> with the
     * result of the operation.
     */
    public PotentialContinuousPT addVariable(Vector vars) {

        Vector aux;
        Node var1, var2;
        int i, j, type;
        boolean found;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree tree;


        aux = new Vector(); // New list of variables.
        for (i = 0; i < variables.size(); i++) {
            var1 = (Node) variables.elementAt(i);
            found = false;

            for (j = 0; j < vars.size(); j++) {
                var2 = (Node) vars.elementAt(j);
                if (var1 == var2) {
                    //System.out.println("Hemos encontrado la variable");
                    found = true;
                    break;
                }
            }

            if (!found) {
                aux.addElement(var1);
            }
        }
        //System.out.println("Antes de hacer el constructor, aux tiene tam: "+aux.size());
        if (aux.size() == 0) {
            pot = new PotentialContinuousPT();
        } else {
            pot = new PotentialContinuousPT(aux); // The new potential.
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
     * @return a new <code>PotentialContinuousPT</code> with the
     * result of the deletion.
     */
    public Potential addVariable(Node var) {

        Vector v;
        PotentialContinuousPT pot;

        if (var.getClass() == FiniteStates.class) {
            values.expandZeros();
        }

        v = new Vector();
        v.addElement(var);
        pot = addVariable(v);

        return pot;
    }

    /**
     * Removes the argument variable integrating over it.
     * @param var a <code>Continuous</code> variable.
     * @return a new <code>PotentialContinuousPT</code> with the
     * result of the deletion.
     */
    public Potential addVariable(Continuous var) {

        Vector v;
        PotentialContinuousPT pot;

        v = new Vector();
        v.addElement(var);

        pot = addVariable(v);

        return pot;
    }

    /**
     * Marginalizes a <code>PotentialContinuousPT</code> to a list of variables.
     * It is equivalent to remove the other variables.
     * @param vars a vector of variables.
     * @return a new <code>PotentialContinuousPT</code> with the marginal.
     * @see addVariable(Vector vars)
     */
    public Potential marginalizePotential(Vector vars) {

        Vector v;
        int i, j;
        boolean found;
        Node var1, var2;
        PotentialContinuousPT pot;

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
     * Simulates a value for the variable for which the potential
     * is defined. If the potential es defined for more then one
     * variable, an error message is reported ant the program
     * aborts.
     *
     * @return the simulated value.
     */
    public double simulateValue() {

        if (variables.size() > 1) {
            System.out.println("ERROR: More than one variable in the potential to simulate");
            System.exit(1);
        }

        return values.simulateValue();
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
     * Normalizes the values of this potential. By now it is restricted
     * to potentials with a single continuous variable.
     * The object is modified.
     */
    public void normalize() {

        values.normalizeLeaves((Node) variables.elementAt(0), 1);
    }

    /**
     * Normalizes the values of this potential. By now it is restricted
     * to potentials with a single continuous variable.
     * The object is modified.
     * @param flag an integer indicating if the MixtExpDensities in the leaves must be simplified
     */
    public void normalize(int flag) {

        values.normalizeLeaves((Node) variables.elementAt(0), flag);
    }

    /**
     * Multiplies the potential by a given constant.
     * The object is modified.
     *
     * @param c the constant (a double)
     */
    public void multiplyByConstant(double c) {

        values.multiplyByConstant(c);
    }

    /**
     * Sets the value for a configuration of <code>FiniteStates</code>
     * variables.
     * WARNING: The method works only for discrete configurations.
     * @param conf a <code>Configuration</code> of discrete variables.
     * @param x a <code>double</code>, the new value for <code>conf</code>.
     */
    public void setValue(Configuration conf, double x) {

        Configuration aux;
        ContinuousProbabilityTree tree;
        FiniteStates var;
        int i, p, val, s;
        boolean update;


        update = true;

        aux = conf.duplicate();
        s = conf.getVariables().size();
        tree = values;

        for (i = 0; i < s; i++) {

            if (!tree.isVariable()) {
                var = aux.getVariable(0);
                val = aux.getValue(0);
                aux.remove(0);

                if (tree.isProbab()) // if the node is a probability,
                {
                    update = false;    // do not update the number of leaves.
                }
                tree.assignVar(var);
            } else {
                p = aux.indexOf(tree.getVar());
                var = aux.getVariable(p);
                val = aux.getValue(p);
                aux.remove(p);
            }

            tree = tree.getChild(val);
        }

        tree.assignProb(x);
        if (update) {
            size++;
        }
    }

    /* NOT IMPLEMENTED METHODS */
    /**
     * @return the sum of all the values of a potential.
     */
    public double totalPotential() {

        System.out.println("ERROR: NOT IMPLEMENTED");
        System.exit(0);

        return 0;
    }

    /**
     * @param conf a configuration of variables.
     * @return the sum of the values of a potential
     * restricted to configuration conf.
     */
    public double totalPotential(Configuration conf) {

        System.out.println("ERROR: NOT IMPLEMENTED");
        System.exit(0);

        return 0;
    }

    /**
     * Computes the entropy of a potential.
     * @return the sum of the values x Log x stored in the potential.
     */
    public double entropyPotential() {

        System.out.println("ERROR: NOT IMPLEMENTED");
        System.exit(0);

        return 0;
    }

    /**
     * Computes the entropy of a potential restricted to
     * a given configuration.
     * @param conf the configuration.
     * @return the sum of the values x Log x fixing configuration conf.
     */
    public double entropyPotential(Configuration conf) {

        System.out.println("ERROR: NOT IMPLEMENTED");
        System.exit(0);

        return 0;
    }

    /**
     * Saves the potential to a file. Saves just the tree.
     * @param p the <code>PrintWriter</code> where the potential will be written.
     */
    public void save(PrintWriter p) {

        p.print("values= continuous-tree ( \n");

        values.save(p, 10);

        p.print("\n);\n\n");
    }

    /**
     * Saves the potential to a file in R format. Saves just the tree.
     * This method only works for potentials defined over a single variable.
     * @param p the <code>PrintWriter</code> where the potential will be written.
     */
    public void saveR(PrintWriter p) {

        String name = ((Node) this.variables.elementAt(0)).getName();

        p.println("f" + name + " = function(" + name + ") {");
        p.println("result = " + name + ";");
        values.saveR(p, "");

        p.println("return(result);");
        p.print("}\n");
    }

    /**
     * This method prunes the the ContinuousProbabilityTree of this Potential
     * The potential is modified
     */
    public void prune(double delta, double epsilon, double epsilonJoin, double epsilonDisc) {

        ContinuousProbabilityTree cpt;

        cpt = getTree();

        cpt.prune(cpt, delta, epsilon, epsilonJoin, epsilonDisc);

        setTree(cpt);

    }// End of method prune

    /**
     * Obtains the number of splits on each range of continuous variable, assuming it is constant in every variable
     *
     * @return (int) The number of splits
     */
    public int obtainNumSplits() {

        ContinuousProbabilityTree cpt;

        cpt = getTree();

        return cpt.obtainNumSplits();

    }

    /**
     * Obtains the number of terms on each leaf, assuming it is constant in every leaf
     *
     * @return (int) The number of terms (including independent term)
     */
    public int obtainNumTerms() {

        ContinuousProbabilityTree cpt;

        cpt = getTree();

        return cpt.obtainNumTerms();

    }

    /**
     * Obtains the actual size of the potential, that is, the number of exponential terms (including contants)
     * this Potential has.
     *
     * @return (int) The actual size.
     */
    public int actualSize() {

        ContinuousProbabilityTree cpt;

        cpt = getTree();

        return cpt.actualSize();

    }

    /**
     * Copies this potential.
     * @return a copy of this <code>PotentialContinuousPT</code>.
     */
    public Potential copy() {

        PotentialContinuousPT pot;

        pot = new PotentialContinuousPT(variables);
        pot.size = size;
        pot.setNumTerms(numTerms);
        pot.setNumSplits(numSplits);
        pot.values = values.copy();

        return pot;
    }

    /**
     *
     * Prunes in such a way that there will be just two exponentials left
     *
     **/
    public void prune2() {

        ContinuousIntervalConfiguration conf = new ContinuousIntervalConfiguration();
        ContinuousProbabilityTree cpt;

        cpt = getTree();
        cpt.prune2(conf);

    }

    /**
     * Replaces a variable by a linear function.
     * It requires that the exponents are linear functions.
     * The object is modified.
     * 
     * @param v the variable (Continuous) to replace.
     * @param lf the LinearFunction that will replace variable <code>v</code>.
     * @return a new density, where <code>v</code> is replaced by <code>lf</code>.
     */
    public void replaceVariableByLF(Continuous v, LinearFunction lf) {

        this.values.replaceVariableByLF(v, lf);
    }

    /**
     * Inserts a deterministic variable in the leaves of a tree. Used for convolution
     * operations. The object is modified.
     *
     * @param v the variable to insert.
     * @param f a linear function that determines the new variable.
     */
    public void insertDeterministicVariable(Continuous v, LinearFunction f) {

        ContinuousIntervalConfiguration conf = new ContinuousIntervalConfiguration();

        this.variables.addElement(v);

        Vector va = f.getVariables();

        for (int i = 0; i < va.size(); i++) {
            Continuous c = (Continuous) va.elementAt(i);
            conf.putValue(c, c.getMin(), c.getMax());
        }
        this.values.insertDeterministicVariable(v, f, conf);
    }
} // End of class

