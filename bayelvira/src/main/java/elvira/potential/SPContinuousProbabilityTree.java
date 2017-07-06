/* SPContinuousProbabilityTree.java */
package elvira.potential;

import java.util.Vector;
import java.util.ArrayList;
import java.util.Hashtable;
import java.lang.Math;
import elvira.*;
import elvira.inference.abduction.Explanation;
import elvira.potential.MixtExpDensity;
import java.io.*;
import elvira.learning.*;
import elvira.tools.ContinuousFunction;
import elvira.tools.LinearFunction;
import elvira.tools.QuadraticFunction;
import java.io.BufferedReader;
import elvira.tools.SampleGenerator;

/**
 * This class implements a sum-prod continuous probability tree. It is the same
 * as a continuous probability tree, but the leaves contain a list of
 * lists of MIxtExpDensity objects. The first list level refers to sums
 * and the second level refers to products.
 * 
 * @author asc
 * @since 19/7/2011
 */
public class SPContinuousProbabilityTree {

    /**
     * Variable in this node of the tree.
     */
    Node var;
    /**
     * Value of the node (in case of being a leaf).
     */
    ArrayList<ArrayList<MixtExpDensity>> value;
    /**
     * Label of the node. Possible labels are defined below as constants.
     */
    int label;
    /**
     * Vector of children of this node.
     */
    Vector<SPContinuousProbabilityTree> child;
    /**
     * Vector of cut-points. They define the intervals in which the domain
     * of the variable is partitioned. There will be one sub-interval for each
     * child of this node.
     */
    Vector cutPoints;
    /**
     * Number of leaves below this node.
     */
    long leaves;
    /**
     * Possible labels of a node.
     */
    static final int EMPTY_NODE = 0;
    static final int DISCRETE_NODE = 1;
    static final int PROBAB_NODE = 2;
    static final int CONTINUOUS_NODE = 3;

    /**
     * Constructor. Creates an empty tree node.
     */
    public SPContinuousProbabilityTree() {

        label = EMPTY_NODE;
        leaves = 0;
        child = new Vector<SPContinuousProbabilityTree>();
    }

    /**
     * Creates a tree with the argument (a discrete variable) as root node.
     * @param variable a <code>FiniteStates</code> variable.
     */
    public SPContinuousProbabilityTree(FiniteStates variable) {

        int i, j;
        SPContinuousProbabilityTree tree;

        label = DISCRETE_NODE;
        leaves = 0;
        var = variable;
        child = new Vector<SPContinuousProbabilityTree>();

        j = variable.getNumStates();
        for (i = 0; i < j; i++) {
            tree = new SPContinuousProbabilityTree();
            tree.setVar((Node) variable);
            child.addElement(tree);
        }
    }

    /**
     * Creates a tree with the argument (a discrete variable) as root node
     * and assigning the values given as parameter
     * @param variable a <code>FiniteStates</code> variable.
     * @param values the values to store in the leaves
     */
    public SPContinuousProbabilityTree(FiniteStates variable, double[] values) {

        int i, j;
        SPContinuousProbabilityTree tree;


        label = DISCRETE_NODE;
        leaves = 0;
        var = variable;
        child = new Vector<SPContinuousProbabilityTree>();

        j = variable.getNumStates();
        for (i = 0; i < j; i++) {
            tree = new SPContinuousProbabilityTree(values[i]);
            tree.setVar((Node) variable);
            child.addElement(tree);
        }
    }

    /**
     * Creates a tree with the argument (a continuous variable) as root node.
     * @param variable a <code>Continuous</code> variable.
     * @param cp a vector of cut points to determine the children of the
     * continuous variable.
     */
    public SPContinuousProbabilityTree(Continuous variable, Vector cp) {

        int i, j;
        SPContinuousProbabilityTree tree;

        label = CONTINUOUS_NODE;
        leaves = 0;
        var = variable;
        child = new Vector<SPContinuousProbabilityTree>();
        cutPoints = (Vector) cp.clone();

        j = cp.size() - 1;
        for (i = 0; i < j; i++) {
            tree = new SPContinuousProbabilityTree();
            tree.setVar((Node) variable);
            child.addElement(tree);
        }
    }

    /**
     * Creates a tree with the argument (a continuous variable) as root node
     * and all its children being equal to a constant value given
     * as argument.
     * @param variable a <code>Continuous</code> variable.
     * @param cp a vector of cut points to determine the children of
     * the continuous variable.
     * @param x the double value to store at the leaves.
     */
    public SPContinuousProbabilityTree(Continuous variable, Vector cp, double x) {

        int i;
        SPContinuousProbabilityTree tree;

        label = CONTINUOUS_NODE;
        leaves = cp.size() - 1;
        var = variable;
        child = new Vector<SPContinuousProbabilityTree>();
        cutPoints = (Vector) cp.clone();

        for (i = 0; i < leaves; i++) {
            tree = new SPContinuousProbabilityTree(x);
            tree.setVar((Node) variable);
            child.addElement(tree);
        }
    }

    /**
     * Creates a probability node with a density that is constant and equal to
     * a given value.
     * @param p a double value.
     */
    public SPContinuousProbabilityTree(double p) {

        label = PROBAB_NODE;
        MixtExpDensity mte = new MixtExpDensity(p);
        leaves = 1;

        value = new ArrayList<ArrayList<MixtExpDensity>>();
        ArrayList<MixtExpDensity> auxVect = new ArrayList<MixtExpDensity>();

        auxVect.add(mte);
        value.add(auxVect);
    }

    /**
     * Creates a probability node with a given density.
     * @param f a density function.
     */
    public SPContinuousProbabilityTree(MixtExpDensity f) {

        label = PROBAB_NODE;
        MixtExpDensity mte = f.duplicate();
        leaves = 1;

        value = new ArrayList<ArrayList<MixtExpDensity>>();
        ArrayList<MixtExpDensity> auxVect = new ArrayList<MixtExpDensity>();

        auxVect.add(mte);
        value.add(auxVect);
    }

    /**
     * Assigns a continuous variable with given cut-points to a continuous tree.
     * @param variable a <code>Continuous</code> variable.
     * @param cp a vector of cut points to determine the children of
     * the continuous variable.
     */
    public void setVar(Node variable) {

        var = variable;

    }

    /**
     * Gets the label of the node.
     * @return the label of the node.
     */
    public int getLabel() {

        return label;
    }

    /**
     * Determines whether a node is a continuous variable.
     * @return <code>true</code> if the node is a continuous variable and
     * <code>false</code> otherwise.
     */
    public boolean isContinuous() {

        if (label == CONTINUOUS_NODE) {
            return true;
        }

        return false;
    }

    /**
     * Gets the number of children of this node.
     * @return the number of children of this node.
     */
    public int getNumberOfChildren() {

        return child.size();
    }

    /**
     * Gets the variable stored in this node.
     * @return the variable attached to a node.
     */
    public Node getVar() {

        return var;
    }

    /**
     * Determines whether a node is a discrete variable.
     * @return <code>true</code> if the node is a discrete variable and
     * <code>false</code> otherwise.
     */
    public boolean isDiscrete() {

        if (label == DISCRETE_NODE) {
            return true;
        }

        return false;
    }

    /**
     * Gets the cutpoint at the given position.
     * @param pos the position in the list of cutpoints.
     * @return the value of the cutpoint.
     */
    public double getCutPoint(int pos) {

        if ((pos < 0) || (pos >= cutPoints.size())) {
            System.out.println("ERROR: Not so many cutpoints!");
            System.exit(1);
        }

        return ((Double) cutPoints.elementAt(pos)).doubleValue();
    }

    /**
     * Assigns a discrete variable to a tree. It creates
     * a vector of children of size equal to the number
     * of cases of the discrete variable.
     * @param variable the discrete variable.
     */
    public void assignVar(FiniteStates variable) {

        int i, j;
        SPContinuousProbabilityTree tree;

        label = DISCRETE_NODE;
        leaves = 0;
        var = variable;
        child = new Vector<SPContinuousProbabilityTree>();
        cutPoints = null;
        j = variable.getNumStates();
        for (i = 0; i < j; i++) {
            tree = new SPContinuousProbabilityTree();
            child.addElement(tree);
        }
    }

    /**
     * Assigns a continuous variable with given cutpoints to a continuous tree.
     * @param variable a <code>Continuous</code> variable.
     * @param cp a vector of cut points to determine the children of
     * the continuous variable.
     */
    public void assignVar(Continuous variable, Vector cp) {

        int i, j;
        SPContinuousProbabilityTree tree;

        label = CONTINUOUS_NODE;
        leaves = 0;
        var = variable;
        child = new Vector<SPContinuousProbabilityTree>();
        cutPoints = (Vector) cp.clone();

        j = cp.size() - 1;
        for (i = 0; i < j; i++) {
            tree = new SPContinuousProbabilityTree();
            child.addElement(tree);
        }
    }

    /**
     * Determines whether a node is a density or not.
     * @return <code>true</code> if the node is a density and
     * <code>false</code> otherwise.
     */
    public boolean isProbab() {

        if (label == PROBAB_NODE) {
            return true;
        }

        return false;
    }

    /**
     * Transforms a ContinuousProbabilityTree into a
     * SPContinuousProbabilityTree.
     *
     * @param the CPT to transform
     * @return the new SPCPT.
     */
    public static SPContinuousProbabilityTree transform(ContinuousProbabilityTree c) {

        SPContinuousProbabilityTree res = new SPContinuousProbabilityTree();
        int i;
        Vector cp = new Vector();

        res.label = c.getLabel();

        if (c.isContinuous()) {//If it is continuous, it has cutPoints
            for (i = 0; i < (c.getNumberOfChildren() + 1); i++) {
                cp.addElement(new Double(c.getCutPoint(i)));
            }

            res.assignVar((Continuous) c.getVar(), cp);
        } else //Dircrete or prob.
        if (c.isDiscrete()) {
            res.assignVar((FiniteStates) c.getVar());
        }


        if (!c.isProbab()) {//If it is not a prob., then it h as children
            for (i = 0; i < c.getNumberOfChildren(); i++) {
                res.child.setElementAt(transform(c.getChild(i)), i);
            }
        } else {//It is a prob., so we copy its density
            MixtExpDensity mte = c.getProb();

            res.value = new ArrayList<ArrayList<MixtExpDensity>>();
            ArrayList<MixtExpDensity> auxVect = new ArrayList<MixtExpDensity>();

            auxVect.add(mte);
            res.value.add(auxVect);
            res.leaves = 1;
        }

        return res;

    }

    /**
     * Combines a SP factorised potential in a leaf of a tree,
     * with a tree, that can be a leaf or not.
     * It multiplies the densities in a lazy way.
     * To be used as a static function.
     * @param f a <code>ArrayList<ArrayList<MixtExpDensity>></code>.
     * @param tree a <code>SPContinuousProbabilityTree</code> to be multiplied
     * with <code>f</code>.
     * @return a new <code>SPContinuousProbabilityTree</code> resulting of
     * combining <code>f</code> and <code>tree</code>.
     */
    public static SPContinuousProbabilityTree combine(ArrayList<ArrayList<MixtExpDensity>> f, SPContinuousProbabilityTree tree) {

        SPContinuousProbabilityTree treeResult;
        int s;

        treeResult = new SPContinuousProbabilityTree();
        treeResult.label = tree.label;
        treeResult.var = tree.var;
        treeResult.leaves = tree.leaves;

        if (tree.isContinuous()) {
            treeResult.cutPoints = (Vector) tree.cutPoints.clone();
        }

        if (tree.isProbab()) { // Lazy multiplication
            treeResult.value = new ArrayList<ArrayList<MixtExpDensity>>();

            int l = f.size(); // Number of summands in f
            int m = tree.value.size(); // Number of summands in tree

            for (int i = 0; i < l; i++) {
                ArrayList<MixtExpDensity> v1 = f.get(i);
                for (int j = 0; j < m; j++) {
                    ArrayList<ArrayList<MixtExpDensity>> aux = new ArrayList<ArrayList<MixtExpDensity>>();
                    ArrayList<MixtExpDensity> v2 = tree.value.get(j);
                    ArrayList<MixtExpDensity> vRes = new ArrayList<MixtExpDensity>();
                    ArrayList<MixtExpDensity> vResConstants = new ArrayList<MixtExpDensity>();
                    for (int k = 0; k < v1.size(); k++) {
                        MixtExpDensity td = v1.get(k);
                        if (td.factors.size() == 0) {
                            vResConstants.add(td);
                        } else {
                            vRes.add(td);
                        }
                    }
                    for (int k = 0; k < v2.size(); k++) {
                        MixtExpDensity td = v2.get(k);
                        if (td.factors.size() == 0) {
                            vResConstants.add(td);
                        } else {
                            vRes.add(td);
                        }
                    }
                    if (vResConstants.size() > 0) {
                        MixtExpDensity t = vResConstants.get(0);
                        for (int k = 1; k < vResConstants.size(); k++) {
                            t = t.multiplyDensities(vResConstants.get(k), l);
                        }
                        if (vRes.size() > 0) {
                            MixtExpDensity temp = vRes.get(0);
                            t = t.multiplyDensities(temp, l);
                            vRes.remove(0);
                        }
                        vRes.add(t);
                    }

                    treeResult.value.add(vRes);
                }
            }
        } else {
            s = tree.child.size();
            for (int i = 0; i < s; i++) {
                treeResult.child.addElement(combine(f, tree.child.elementAt(i)));
            }
        }

        return treeResult;
    }

    /**
     * Combines two  trees.
     * It multiplies the associated densities in a lazy way.
     * To be used as a static function.
     * @param tree1 a <code>SPContinuousProbabilityTree</code>.
     * @param tree2 a <code>SPContinuousProbabilityTree</code> to be multiplied
     * with <code>tree1</code>.
     * @return a new <code>SPContinuousProbabilityTree</code> resulting of
     * combining <code>tree1</code> and <code>tree2</code>.
     */
    public static SPContinuousProbabilityTree combine(SPContinuousProbabilityTree tree1,
            SPContinuousProbabilityTree tree2) {

        SPContinuousProbabilityTree tree;
        Configuration c1;
        ContinuousIntervalConfiguration c2;
        int i, nv;

        if (tree1.isProbab()) { // Probability node.
            tree = combine(tree1.value, tree2);
        } else {
            tree = new SPContinuousProbabilityTree();
            tree.label = tree1.label;
            tree.var = tree1.var;
            tree.leaves = tree1.leaves;
            tree.child = new Vector();

            nv = tree1.child.size();

            if (tree1.isContinuous()) {
                tree.cutPoints = (Vector) tree1.cutPoints.clone();
            }

            for (i = 0; i < nv; i++) {
                c1 = new Configuration();
                c2 = new ContinuousIntervalConfiguration();
                if (tree1.isContinuous()) {
                    c2.putValue((Continuous) tree.var, tree.getCutPoint(i), tree.getCutPoint(i + 1));
                } else {
                    if (tree1.isDiscrete()) {
                        c1.putValue((FiniteStates) tree.var, i);
                    }
                }

                tree.child.addElement(combine(c1, c2, tree1.getChild(i), tree2));
            }
        }

        return tree;
    }

    /**
     * Combines two trees.
     * The second tree is to be restricted to the configuration <code>c1</code>
     * and to the configuration of intervals <code>c2</code>.
     * It is an auxiliary function to
     * <code>combine(SPContinuousProbabilityTree tree1,SPContinuousProbabilityTree tree2)</code>.
     * It multiplies the associated densities in a lazy way.
     * To be used as a static function.
     * @param c1 a <code> Configuration </code> containing the path followed
     * in <code>tree1</code> for discrete variables and to be used as
     * restriction for <code>tree2</code>.
     * @param c2 a <code>ContinuousIntervalConfiguration</code> containing the
     * path followed in <code>tree1</code> for continuous variables and to be
     * used as restriction for <code>tree2</code>.
     * @param tree1 a <code>ContinuousProbabilityTree</code>.
     * @param tree2 a <code>ContinuousProbabilityTree</code> to be multiplied
     * with <code>tree1</code>.
     * @return a new <code>SPContinuousProbabilityTree</code> resulting of
     * combining <code>tree1</code> and <code>tree2</code>.
     */
    public static SPContinuousProbabilityTree combine(Configuration c1,
            ContinuousIntervalConfiguration c2,
            SPContinuousProbabilityTree tree1,
            SPContinuousProbabilityTree tree2) {

        SPContinuousProbabilityTree tree;
        ContinuousIntervalConfiguration cx2;
        int i, nv;


        if (tree1.isProbab()) // Probability node.
        {
            tree = combine(tree1.value, tree2.restrict(c1, c2));
        } else {
            tree = new SPContinuousProbabilityTree();
            tree.label = tree1.label;
            tree.var = tree1.var;
            tree.leaves = tree1.leaves;
            nv = tree1.child.size();

            if (tree1.isContinuous()) {
                tree.cutPoints = (Vector) tree1.cutPoints.clone();
                for (i = 0; i < nv; i++) {
                    cx2 = c2.duplicate();
                    cx2.putValue((Continuous) tree.var, ((Double) tree.cutPoints.elementAt(i)).doubleValue(),
                            ((Double) tree.cutPoints.elementAt(i + 1)).doubleValue());
                    if (tree.child == null) {
                    }
                    tree.child.addElement(combine(c1, cx2, tree1.child.elementAt(i), tree2));
                }
            }
            if (tree1.isDiscrete()) {
                for (i = 0; i < nv; i++) {
                    c1.putValue((FiniteStates) tree.var, i);
                    tree.child.addElement(combine(c1, c2, tree1.child.elementAt(i), tree2));
                }
                c1.remove(c1.size() - 1);
            }
        }

        return tree;
    }

    /**
     * It restricts a tree to a discrete configuration and to a configuration
     * of intervals for continuous variables.
     *
     * @param c1 the discrete configuration.
     * @param c2 the configuration of continuous intervals.
     * @return the restricted tree.
     */
    public SPContinuousProbabilityTree restrict(Configuration c1,
            ContinuousIntervalConfiguration c2) {

        SPContinuousProbabilityTree aux;
        int pos, i, s, element, initial, fin;
        double x, y, x1, y1;

        aux = new SPContinuousProbabilityTree();
        if (isProbab()) {
            aux.value = new ArrayList<ArrayList<MixtExpDensity>>();
            for (int k = 0; k < value.size(); k++) {
                ArrayList<MixtExpDensity> vRes = new ArrayList<MixtExpDensity>();

                for (int k2 = 0; k2 < value.get(k).size(); k2++) {
                    MixtExpDensity td = value.get(k).get(k2);
                    vRes.add(td);
                }
                aux.value.add(vRes);
            }
            aux.label = PROBAB_NODE;
            aux.leaves = 1;
        }
        if (isDiscrete()) {
            pos = c1.indexOf(var);
            if (pos == -1) {
                aux.label = DISCRETE_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.child = new Vector();
                s = child.size();
                for (i = 0; i < s; i++) {
                    aux.child.addElement((child.elementAt(i)).restrict(c1, c2));
                }
            } else {
                element = c1.getValue(pos);
                aux = (child.elementAt(element)).restrict(c1, c2);
            }
        }
        if (isContinuous()) {
            pos = c2.indexOf(var);
            if (pos == -1) {
                aux.label = CONTINUOUS_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.cutPoints = (Vector) cutPoints.clone();
                aux.child = new Vector();
                s = child.size();
                for (i = 0; i < s; i++) {
                    aux.child.addElement((child.elementAt(i)).restrict(c1, c2));
                }
            } else {
                x = c2.getLowerValue(pos);
                y = c2.getUpperValue(pos);
                s = child.size();
                initial = -1;
                fin = -1;
                for (i = 0; i < s; i++) {
                    x1 = ((Double) cutPoints.elementAt(i)).doubleValue();
                    y1 = ((Double) cutPoints.elementAt(i + 1)).doubleValue();

                    if ((y1 > x) && (initial == -1)) { 
                        initial = i;
                    }
                    if (y <= y1) {
                        if (x1 == y) {
                            fin = i - 1;
                        } else {
                            fin = i;
                        }
                        break;
                    }
                }

                x1 = ((Double) cutPoints.elementAt(0)).doubleValue();
                y1 = ((Double) cutPoints.elementAt(s)).doubleValue();

                if (fin == -1 && initial != -1 && y > y1) {
                    fin = s - 1;
                }

                if (initial == 0 && fin == 0 && y <= x1) {
                    initial = -1;
                    fin = -1;
                }



                if (fin == -1 && initial == 0) {
                    //In this case, the restriction process return a empty tree.
                    aux.value = new ArrayList<ArrayList<MixtExpDensity>>();
                    aux.value.add(new ArrayList<MixtExpDensity>());
                    aux.value.get(0).add(new MixtExpDensity(0.0));
                    aux.label = PROBAB_NODE;
                    aux.leaves = 1;

                } else if (initial == -1 && fin == -1) {
                    //In this case, the restriction process return a empty tree.
                    aux.value = new ArrayList<ArrayList<MixtExpDensity>>();
                    aux.value.add(new ArrayList<MixtExpDensity>());
                    aux.value.get(0).add(new MixtExpDensity(0.0));
                    aux.label = PROBAB_NODE;
                    aux.leaves = 1;

                } else if (fin == initial) {
                    x1 = ((Double) cutPoints.elementAt(initial)).doubleValue();
                    y1 = ((Double) cutPoints.elementAt(cutPoints.size() - 1)).doubleValue();
                    if (x < x1 && initial == 0) {
                        aux.label = CONTINUOUS_NODE;
                        aux.var = var;
                        aux.leaves = leaves;
                        aux.cutPoints = new Vector();
                        aux.child = new Vector();
                        aux.cutPoints.addElement(new Double(x));
                        aux.child.addElement(new SPContinuousProbabilityTree(0.0));


                        aux.cutPoints.addElement(new Double(x1));
                        aux.child.addElement((child.elementAt(initial)).restrict(c1, c2));

                        y1 = ((Double) cutPoints.elementAt(initial + 1)).doubleValue();
                        if (y < y1) {
                            y1 = y;
                        }
                        aux.cutPoints.addElement(new Double(y1));



                        if (y > y1) {
                            aux.cutPoints.addElement(new Double(y));
                            aux.child.addElement(new SPContinuousProbabilityTree(0.0));
                        }
                    } else if (y > y1 && fin == s - 1) {

                        aux.label = CONTINUOUS_NODE;
                        aux.var = var;
                        aux.leaves = leaves;
                        aux.cutPoints = new Vector();
                        aux.child = new Vector();

                        x1 = ((Double) cutPoints.elementAt(initial)).doubleValue();
                        if (x1 < x) {
                            x1 = x;
                        }

                        aux.cutPoints.addElement(new Double(x1));
                        aux.child.addElement((child.elementAt(initial)).restrict(c1, c2));
                        y1 = ((Double) cutPoints.elementAt(cutPoints.size() - 1)).doubleValue();
                        aux.cutPoints.addElement(new Double(y1));

                        aux.child.addElement(new SPContinuousProbabilityTree(0.0));
                        aux.cutPoints.addElement(new Double(y));
                    } else {
                        aux = (child.elementAt(initial)).restrict(c1, c2);
                    }

                } else {
                    aux.label = CONTINUOUS_NODE;
                    aux.var = var;
                    aux.leaves = leaves;
                    aux.cutPoints = new Vector();
                    aux.child = new Vector();

                    x1 = ((Double) cutPoints.elementAt(0)).doubleValue();
                    if (x < x1 && initial == 0) {
                        aux.cutPoints.addElement(new Double(x));
                        aux.child.addElement(new SPContinuousProbabilityTree(0.0));
                    }

                    for (i = initial; i <= fin; i++) {
                        x1 = ((Double) cutPoints.elementAt(i)).doubleValue();
                        if (x1 < x) {
                            x1 = x;
                        }
                        y1 = ((Double) cutPoints.elementAt(i + 1)).doubleValue();
                        if (y < y1) {
                            y1 = y;
                        }

                        ContinuousIntervalConfiguration newInterval = c2.duplicate();
                        newInterval.putValue((Continuous) var, x1, y1);
                        aux.child.addElement((child.elementAt(i)).restrict(c1, newInterval));
                        aux.cutPoints.addElement(new Double(x1));
                    }
                    y1 = ((Double) cutPoints.elementAt(i)).doubleValue();
                    if (y < y1) {
                        y1 = y;
                    }
                    aux.cutPoints.addElement(new Double(y1));

                    y1 = ((Double) cutPoints.elementAt(s)).doubleValue();
                    if (y > y1 && fin == s - 1) {
                        aux.cutPoints.addElement(new Double(y));
                        aux.child.addElement(new SPContinuousProbabilityTree(0.0));
                    }

                }
            }
        }

        return aux;
    }

    /**
     * Gets a child of this node.
     * @param i an int value. Number of child to be returned.
     * (first value is <code>i=0</code>).
     * @return the <code>i</code>-th child of this node.
     */
    public SPContinuousProbabilityTree getChild(int i) {

        return (child.elementAt(i));
    }

    /**
     * It removes a discrete variable from a continuous probability tree
     * by summing over it.
     *
     * @param variable the variable to be removed.
     * @return the continuous probability tree result of the deletion.
     */
    public SPContinuousProbabilityTree addVariable(FiniteStates variable) {

        SPContinuousProbabilityTree aux;
        int i, s;

        aux = new SPContinuousProbabilityTree();

        if (isProbab()) {
            // It is enough to multilpy the first density by the cases of the variable
            aux.value = new ArrayList<ArrayList<MixtExpDensity>>();
            ArrayList<MixtExpDensity> auxVect = new ArrayList<MixtExpDensity>();
            // Now we copy the rest of summands
            for (int k = 0; k < value.size(); k++) {
                auxVect = new ArrayList<MixtExpDensity>();
                auxVect.add(value.get(k).get(0).multiplyDensities(variable.getNumStates()));
                for (int k2 = 1; k2 < value.get(k).size(); k2++) {
                    MixtExpDensity mte = value.get(k).get(k2);
                    auxVect.add(mte);
                }
                aux.value.add(auxVect);
            }
            aux.label = PROBAB_NODE;
            aux.leaves = 1;
        } else {
            if (isDiscrete()) {
                if (var == variable) {
                    aux = addChildren();
                } else {
                    aux.label = DISCRETE_NODE;
                    aux.var = var;
                    aux.leaves = leaves;
                    aux.child = new Vector();
                    s = child.size();
                    for (i = 0; i < s; i++) {
                        aux.child.addElement((child.elementAt(i)).addVariable(variable));
                    }
                }
            } else {
                if (isContinuous()) {
                    aux.label = CONTINUOUS_NODE;
                    aux.var = var;
                    aux.leaves = leaves;
                    aux.cutPoints = (Vector) cutPoints.clone();
                    aux.child = new Vector();
                    s = child.size();
                    for (i = 0; i < s; i++) {
                        aux.child.addElement((child.elementAt(i)).addVariable(variable));
                    }
                }
            }
        }
        return aux;
    }

    /**
     * Adds the children of this node.
     * @return a new continuous tree equal to the addition of all the children
     *         of the current tree.
     */
    public SPContinuousProbabilityTree addChildren() {

        SPContinuousProbabilityTree tree;
        int i, nv;

        tree = getChild(0);
        nv = child.size();
        for (i = 1; i < nv; i++) {
            tree = add(getChild(i), tree);
        }

        return tree;
    }

    /**
     * Sums two continuous trees.
     * It sums the associated densities.
     * To be used as a static function.
     * @param tree1 a <code>SPContinuousProbabilityTree</code>.
     * @param tree2 a <code>SPContinuousProbabilityTree</code> to be added with
     * <code>tree1</code>.
     * @return a new <code>SPContinuousProbabilityTree</code> resulting from adding
     * <code>tree1</code> and <code>tree2</code>.
     */
    public static SPContinuousProbabilityTree add(SPContinuousProbabilityTree tree1,
            SPContinuousProbabilityTree tree2) {

        SPContinuousProbabilityTree tree;
        Configuration c1;
        ContinuousIntervalConfiguration c2;
        int i, nv;
        if (tree1.isProbab()) // Probability node.
        {
            tree = add(tree1.value, tree2);
        } else {
            tree = new SPContinuousProbabilityTree();
            tree.label = tree1.label;
            tree.var = tree1.var;
            tree.leaves = tree1.leaves;


            if (tree1.isContinuous()) {
                tree.cutPoints = (Vector) tree1.cutPoints.clone();
            }
            nv = tree1.child.size();
            for (i = 0; i < nv; i++) {
                c1 = new Configuration();
                c2 = new ContinuousIntervalConfiguration();
                if (tree1.isContinuous()) {
                    c2.putValue((Continuous) tree.var, ((Double) tree.cutPoints.elementAt(i)).doubleValue(),
                            ((Double) tree.cutPoints.elementAt(i + 1)).doubleValue());
                }
                if (tree1.isDiscrete()) {
                    c1.putValue((FiniteStates) tree.var, i);
                }

                tree.child.addElement(add(c1, c2, tree1.child.elementAt(i), tree2));
            }
        }

        return tree;
    }

    /**
     * Sums a density to a continuous tree.
     *
     * To be used as a static function.
     * @param f a <code>ArrayList<ArrayList<MixtExpDensity>></code>.
     * @param tree a <code>SPContinuousProbabilityTree</code> to be added with
     * <code>f</code>.
     * @return a new <code>SPContinuousProbabilityTree</code> resulting from adding
     * <code>f</code> and <code>tree</code>.
     */
    public static SPContinuousProbabilityTree add(ArrayList<ArrayList<MixtExpDensity>> f,
            SPContinuousProbabilityTree tree) {

        SPContinuousProbabilityTree treeResult;
        ArrayList<MixtExpDensity> auxVect;
        int i, s;

        treeResult = new SPContinuousProbabilityTree();
        treeResult.label = tree.label;
        treeResult.var = tree.var;
        treeResult.leaves = tree.leaves;

        if (tree.isContinuous()) {
            treeResult.cutPoints = (Vector) tree.cutPoints.clone();
        }
        if (tree.isProbab()) {
            treeResult.value = new ArrayList<ArrayList<MixtExpDensity>>();
            treeResult.value.addAll(f);
            treeResult.value.addAll(tree.value);
        } else {
            s = tree.child.size();
            for (i = 0; i < s; i++) {
                treeResult.child.addElement(add(f, tree.child.elementAt(i)));
            }
        }

        return treeResult;
    }

    /**
     * Sums two continuous trees.
     * The second tree is to be restricted to the configuration <code>c1</code>
     * and to the configuration of intervals <code>c2</code>.
     * It is an auxiliary function to <code> add(SPContinuousProbabilityTree tree1,
     *				      SPContinuousProbabilityTree tree2) </code>
     * It sums the associated densities in a lazy way.
     * To be used as a static function.
     * @param c1 a <code> Configuration </code> containing the path followed in
     * <code>tree1</code> for discrete variables and to be used as restriction
     * for <code>tree2</code>.
     * @param c2 a <code> ContinuousIntervalConfiguration </code> containing
     * the path followed in <code>tree1</code> for continuous variables and
     * to be used as restriction for <code>tree2</code>.
     * @param tree1 a <code>SPContinuousProbabilityTree</code>.
     * @param tree2 a <code>SPContinuousProbabilityTree</code> to be added with
     * <code>tree1</code>.
     * @return a new <code>SPContinuousProbabilityTree</code> resulting from adding
     * <code>tree1</code> and <code>tree2</code>.
     */
    public static SPContinuousProbabilityTree add(Configuration c1,
            ContinuousIntervalConfiguration c2,
            SPContinuousProbabilityTree tree1,
            SPContinuousProbabilityTree tree2) {

        SPContinuousProbabilityTree tree;
        ContinuousIntervalConfiguration cx2;
        int i, nv;

        if (tree1.isProbab()) // Probability node.
        {
            tree = add(tree1.value, tree2.restrict(c1, c2));
        } else {
            tree = new SPContinuousProbabilityTree();
            tree.label = tree1.label;
            tree.var = tree1.var;
            tree.leaves = tree1.leaves;
            nv = tree1.child.size();

            if (tree1.isContinuous()) {
                tree.cutPoints = (Vector) tree1.cutPoints.clone();
                for (i = 0; i < nv; i++) {
                    cx2 = c2.duplicate();
                    cx2.putValue((Continuous) tree.var, ((Double) tree.cutPoints.elementAt(i)).doubleValue(),
                            ((Double) tree.cutPoints.elementAt(i + 1)).doubleValue());
                    tree.child.addElement(add(c1, cx2, tree1.child.elementAt(i), tree2));
                }
            }
            if (tree1.isDiscrete()) {
                for (i = 0; i < nv; i++) {
                    c1.putValue((FiniteStates) tree.var, i);
                    tree.child.addElement(add(c1, c2, tree1.child.elementAt(i), tree2));
                }
                c1.remove(c1.size() - 1);
            }
        }

        return tree;
    }

    /**
     * It removes a continuous variable from a continuous probability tree
     * by integrating with respect to it.
     *
     * @param variable the variable to be removed.
     * @return the continuous probability tree result of the deletion.
     */
    public SPContinuousProbabilityTree addVariable(Continuous variable) {

        return integrateOut(variable, variable.getMin(), variable.getMax());
    }

    /**
     * It makes the integral of a continuous tree with respect to a continuous
     * variable between two values. It is necessary that lower <= upper.
     *
     * @param variable the continuous variable with respect to which the integral
     * is carried out.
     * @param lower a double: the low limit of the integral.
     * @param upper a double: the up limit of the integral.
     * @return a continuous tree with the result of the integral.
     *
     */
    public SPContinuousProbabilityTree integrateOut(Continuous variable, double lower,
            double upper) {

        SPContinuousProbabilityTree aux;
        int i, s;
        double x, y;
        ArrayList<MixtExpDensity> auxVect;

        aux = new SPContinuousProbabilityTree(0.0);

        if (lower == upper) {
            return aux;
        }


        if (isProbab()) {

            for (int k = 0; k < value.size(); k++) {
                auxVect = new ArrayList<MixtExpDensity>();
                boolean done = false;
                for (int k2 = 0; k2 < value.get(k).size(); k2++) {
                    MixtExpDensity d = value.get(k).get(k2);
                    d.simplify();
                    Vector terms = d.getTerms();
                    boolean found = false;
                    int j = 0;
                    while ((!found) && (j < terms.size())) {
                        LinearFunction lf = (LinearFunction) (terms.elementAt(j));
                        if (lf.indexOf(variable) != -1) {
                            found = true;
                        }
                        j++;
                    }
                    if (found) {
                        auxVect.add(d.integral(variable, lower, upper, 1));
                        done = true;
                    } else {
                        auxVect.add(d);
                    }
                }
                if (!done) {
                    // We have to integrate a unity function
                    auxVect.add(new MixtExpDensity(upper - lower));
                }
                aux.value.add(auxVect);
            }
            aux.label = PROBAB_NODE;
            aux.leaves = 1;
            if (aux.value.size() > 1) {
                aux.value.remove(0);
            }
        } else {
            if (isDiscrete()) {
                aux.label = DISCRETE_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.child = new Vector();
                s = child.size();
                for (i = 0; i < s; i++) {
                    aux.child.addElement((child.elementAt(i)).integrateOut(variable, lower, upper));
                }
            } else {
                if (isContinuous()) {
                    if (variable.equals(var)) {
                        s = child.size();
                        x = ((Double) cutPoints.elementAt(0)).doubleValue();
                        for (i = 0; i < s; i++) {
                            y = ((Double) cutPoints.elementAt(i + 1)).doubleValue();
                            if (lower > x) {
                                x = lower;
                            }
                            if (y > upper) {
                                y = upper;
                            }
                            if (y > x) {
                                aux = add(aux, getChild(i).integrateOut(variable, x, y));
                            }
                            x = y;
                        }
                    } else {
                        aux.label = CONTINUOUS_NODE;
                        aux.var = var;
                        aux.leaves = leaves;
                        aux.cutPoints = (Vector) cutPoints.clone();
                        aux.child = new Vector();
                        s = child.size();
                        for (i = 0; i < s; i++) {
                            aux.child.addElement((child.elementAt(i)).integrateOut(variable, lower, upper));
                        }
                    }
                }
            }
        }
        return aux;
    }

    /**
     * Gets the number of leaves below this node.
     * @return the size of the tree (i.e. the value of <code>leaves</code>).
     */
    public long getSize() {

        return leaves;
    }

    /**
     * Retrieves the density stored in a leaf.
     * @param conf a <code>ContinuousConfiguration</code>.
     * @return the density of the tree following the path indicated by
     * configuration <code>conf</code> and restricted to the values of
     * that configuration.
     */
    public ArrayList<ArrayList<MixtExpDensity>> getProb(ContinuousConfiguration conf) {

        int index, i, s, val;
        SPContinuousProbabilityTree tree;
        double xval;
        double inf, sup;
        ArrayList<ArrayList<MixtExpDensity>> retValue = new ArrayList<ArrayList<MixtExpDensity>>();

        if (isDiscrete()) { // If the node is a discrete variable
            val = conf.getValue((FiniteStates) var);
            return ((child.elementAt(val)).getProb(conf));
        } else {
            if (isContinuous()) {
                xval = conf.getValue((Continuous) var);
                index = -1;
                s = cutPoints.size();
                inf = ((Double) cutPoints.elementAt(0)).doubleValue();
                if (xval >= inf) {
                    for (i = 1; i < s; i++) {
                        sup = ((Double) cutPoints.elementAt(i)).doubleValue();
                        if (xval <= sup) {
                            index = i - 1;
                            break;
                        }
                    }
                } else {
                    retValue.add(new ArrayList<MixtExpDensity>());
                    retValue.get(0).add(new MixtExpDensity(0.0));
                    return (retValue);
                }
                if (index > -1) {
                    return ((child.elementAt(index)).getProb(conf));
                } else {
                    retValue.add(new ArrayList<MixtExpDensity>());
                    retValue.get(0).add(new MixtExpDensity(0.0));
                    return (retValue);
                }
            } else {
                if (isProbab()) // If the node is a prob.
                {
                    return value;
                } else {
                    retValue.add(new ArrayList<MixtExpDensity>());
                    retValue.get(0).add(new MixtExpDensity(-1.0));
                    return (retValue);
                }
            }
        }
    }

    /**
     * It restricts a tree to a continuous configuration containing values
     * for discrete and continuous variables.
     *
     * @param c the continuous configuration.
     * @return the restricted tree.
     */
    public SPContinuousProbabilityTree restrict(ContinuousConfiguration c) {

        SPContinuousProbabilityTree aux;
        double x, x1, y1;
        int i, s, pos, element;

        aux = new SPContinuousProbabilityTree();

        if (isProbab()) {
            for (int k = 0; k < value.size(); k++) {
                ArrayList<MixtExpDensity> vRes = new ArrayList<MixtExpDensity>();

                for (int k2 = 0; k2 < value.get(k).size(); k2++) {
                    MixtExpDensity td = value.get(k).get(k2).restrict(c);
                    vRes.add(td);
                }
                aux.value.add(vRes);
            }
            aux.label = PROBAB_NODE;
            aux.leaves = 1;
        }
        if (isDiscrete()) {
            pos = c.indexOf(var);
            if (pos == -1) {
                aux.label = DISCRETE_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.child = new Vector();
                s = child.size();
                for (i = 0; i < s; i++) {
                    aux.child.addElement(child.elementAt(i).restrict(c));
                }
            } else {
                element = c.getValue(pos);
                if (element == -1) {
                    System.out.println();
                }
                aux = child.elementAt(element).restrict(c);
            }
        }

        if (isContinuous()) {
            pos = c.getIndex((Continuous) var);
            if (pos == -1) {
                aux.label = CONTINUOUS_NODE;
                aux.var = var;
                aux.leaves = leaves;
                aux.cutPoints = (Vector) cutPoints.clone();
                aux.child = new Vector();
                s = child.size();
                for (i = 0; i < s; i++) {
                    aux.child.addElement(child.elementAt(i).restrict(c));
                }
            } else {
                x = c.getContinuousValue(pos);
                s = child.size();

                for (i = 0; i < s; i++) {
                    x1 = ((Double) cutPoints.elementAt(i)).doubleValue();
                    y1 = ((Double) cutPoints.elementAt(i + 1)).doubleValue();
                    if ((x >= x1) && (x <= y1)) {
                        aux = child.elementAt(i).restrict(c);
                        return aux;
                    }
                }
                return new SPContinuousProbabilityTree(0.0);
            }
        }

        return aux;
    }

    /**
     * This method operates over a continuous (or a discrete variable) whose
     * children are probability nodes.
     * The result is that the leaves are normalized to get
     * that the integral (sum) over the whole domain of the
     * continuous variable is equal to 1.
     * The object is modified.
     */
    public void normalizeLeaves(Node variable) {

        double acum = 0.0;
        int i;
        ArrayList<ArrayList<MixtExpDensity>> density, resDensity;
        SPContinuousProbabilityTree auxTree;


        if (isDiscrete()) {

            for (i = 0; i < getNumberOfChildren(); i++) {
                auxTree = getChild(i);
                if (!auxTree.isProbab()) {
                    System.out.println("Error in normalizeLeaves: no leaf to normalize.");
                    System.exit(1);
                }
                density = auxTree.getProb();

                for (int j = 0; j < density.size(); j++) {
                    ArrayList<MixtExpDensity> prod = density.get(j);
                    double acumj = 1.0;
                    for (int k = 0; k < prod.size(); k++) {
                        MixtExpDensity d = prod.get(k);
                        if (d.getNumberOfExp() == 0) {
                            acumj *= d.getIndependent();
                        } else {
                            acumj *= d.integral((Continuous) variable, ((Continuous) variable).getMin(), ((Continuous) variable).getMax()).getIndependent();
                        }
                    }
                    acum += acumj;
                }

            }

            for (i = 0; i < getNumberOfChildren(); i++) {
                auxTree = getChild(i);
                density = auxTree.getProb();

                MixtExpDensity d;

                if (acum != 0.0) {
                    for (int kk = 0; kk < density.size(); kk++) {
                        d = density.get(kk).get(0).duplicate();
                        d = d.multiplyDensities(1 / acum);
                        density.get(kk).remove(0);
                        density.get(kk).add(d);
                    }
                } else {
                    System.out.println("Problema.......................");
                    for (int kk = 0; kk < density.size(); kk++) {
                        d = new MixtExpDensity(1 / (double) getNumberOfChildren());
                        density.get(kk).remove(0);
                        density.get(kk).add(d);
                    }
                }


                getChild(i).assignProb(density);
            }

        } else if (isContinuous()) {
            auxTree = integrateOut((Continuous) getVar(), getCutPoint(0), getCutPoint(getCutPoints().size() - 1));
            density = auxTree.getProb();

            acum = 0.0;
            for (int j = 0; j < density.size(); j++) {
                ArrayList<MixtExpDensity> prod = density.get(j);
                double acumj = 1.0;
                for (int k = 0; k < prod.size(); k++) {
                    MixtExpDensity d = prod.get(k);
                    acumj *= d.getIndependent();
                }
                acum += acumj;
            }

            // Here, acum is the normalization factor.
            // Now we divide each leaf by its integral
            MixtExpDensity exp = new MixtExpDensity(1 / acum);
            ArrayList<ArrayList<MixtExpDensity>> exp2 = new ArrayList<ArrayList<MixtExpDensity>>();
            exp2.add(new ArrayList<MixtExpDensity>());
            exp2.get(0).add(exp);

            for (i = 0; i < getNumberOfChildren(); i++) {
                auxTree = combine(exp2, getChild(i));
                setChild(auxTree, i);
            }


        } else {
            density = getProb();

            if (variable.getClass() == Continuous.class && variable.getTypeOfVariable() == Node.CHANCE) {
                acum = 0.0;
                for (int j = 0; j < density.size(); j++) {
                    ArrayList<MixtExpDensity> prod = density.get(j);
                    double acumj = 1.0;
                    for (int k = 0; k < prod.size(); k++) {
                        MixtExpDensity d = prod.get(k);
                        acumj *= d.integral((Continuous) variable, ((Continuous) variable).getMin(), ((Continuous) variable).getMax()).getIndependent();
                    }
                    acum += acumj;
                }
            } else if (variable.getClass() == FiniteStates.class) {
                acum = 0.0;
                for (int j = 0; j < density.size(); j++) {
                    ArrayList<MixtExpDensity> prod = density.get(j);
                    double acumj = 1.0;
                    for (int k = 0; k < prod.size(); k++) {
                        MixtExpDensity d = prod.get(k);
                        acumj *= d.integral((Continuous) variable, ((Continuous) variable).getMin(), ((Continuous) variable).getMax()).getIndependent();
                    }
                    acum += acumj;
                }
                acum *= ((FiniteStates) variable).getNumStates();
            }


            density = getProb();
            MixtExpDensity d;
            if (acum != 0.0) {
                for (int kk = 0; kk < density.size(); kk++) {
                    d = density.get(kk).get(0);
                    d = d.multiplyDensities(1 / acum);
                    density.get(kk).remove(0);
                    density.get(kk).add(d);
                }
            } else {
                System.out.println("Problema.......................");
                for (int kk = 0; kk < density.size(); kk++) {
                    d = new MixtExpDensity(1 / (double) getNumberOfChildren());
                    density.get(kk).remove(0);
                    density.get(kk).add(d);
                }

            }


            assignProb(density);

        }
    }

    /**
     * Gets the density function associated with this node.
     * @return the density attached to the node.
     */
    public ArrayList<ArrayList<MixtExpDensity>> getProb() {

        return value;
    }

    /**
     * Assigns a density to a node.
     * The density is not duplicated.
     * Also, sets the label to PROBAB_NODE.
     * @param f a density to be assigned.
     */
    public void assignProb(ArrayList<ArrayList<MixtExpDensity>> f) {

        label = PROBAB_NODE;
        value = f;
        leaves = 1;
    }

    /**
     *  Return CutPoints of tree.
     */
    public Vector getCutPoints() {

        return cutPoints;
    }

    /**
     * Puts <code>copt</code> as a child of the ContinuousProbabilityTree
     * in the position <code>i</code> of the vector child.
     *
     * @param cpt The child to include
     * @param i the position to put it
     */
    public void setChild(SPContinuousProbabilityTree cpt, int i) {

        if (getNumberOfChildren() >= i) {
            child.setElementAt(cpt, i);
        } else {
            System.out.println("Error: The position " + i + " does not exist in the vector of children");
        }

    }

    /**
     * Prints the tree to the standard output.
     */
    public void print() {
        print(0);
    }

    /**
     * Prints the tree to the standard output with n tabs.
     */
    public void print(int n) {

        int i;

        if (isProbab()) {
            for (int j = 0; j < value.size(); j++) {
                if (j > 0) {
                    System.out.println("+");
                }
                for (int k = 0; k < value.get(j).size(); k++) {
                    if (k > 0) {
                        System.out.println("*");
                    }
                    value.get(j).get(k).print(n);
                }
            }
            System.out.println();
        } else {
            var.print(n);

            for (i = 0; i < child.size(); i++) {
                if (cutPoints != null) {
                    if (cutPoints.size() > 0) {
                        for (int z = 0; z < n; z++) {
                            System.out.print("\t");
                        }
                        System.out.print("Interval : (" + getCutPoint(i) + "," + getCutPoint(i + 1) + ")\n");
                    }
                }
                getChild(i).print(n + 1);
            }
        }
    }

    /**
     * Saves the tree to a file.
     *
     * @param p the <code>PrintWriter</code> where the tree will be written.
     */
    public void save(PrintWriter p) {

        int i;

        if (isProbab()) {
            for (int j = 0; j < value.size(); j++) {
                if (j > 0) {
                    p.println("+");
                }
                for (int k = 0; k < value.get(j).size(); k++) {
                    if (k > 0) {
                        p.println("*");
                    }
                    value.get(j).get(k).save(p);
                }
            }
            p.println();
        } else {
            if (var != null) {
                var.save(p);
            }

            for (i = 0; i < child.size(); i++) {
                if (cutPoints != null) {
                    if (cutPoints.size() > 0) {
                        p.print("Interval : (" + getCutPoint(i) + "," + getCutPoint(i + 1) + ")\n");
                    }
                }
                getChild(i).save(p);
            }
        }
    }

    /**
     * Saves the tree to a file in R format.
     *
     * @param p the <code>PrintWriter</code> where the tree will be written.
     */
    public void saveR(PrintWriter p, String cond) {

        int i;

        if (isProbab()) {
            for (int j = 0; j < value.size(); j++) {
                if (j > 0) {
                    p.print(" + ");
                }
                for (int k = 0; k < value.get(j).size(); k++) {
                    if (k > 0) {
                        p.println(" * ");
                    }
                    p.print("(");
                    value.get(j).get(k).saveR(p,cond);
                    p.print(")");
                }
            }
            p.println();
        } else {
            for (i = 0; i < child.size(); i++) {
                String condition = "";
                if (cutPoints != null) {
                    if (cutPoints.size() > 0) {
                        condition = "[("+var.getName() + ">=" + getCutPoint(i) + ")&(" + var.getName() + "<=" + getCutPoint(i + 1) + ")]";
                        p.print("result"+condition+" = ");
                    }
                }
                getChild(i).saveR(p,condition);
            }
        }
    }

    /**
     * Creates a copy of the SPContinuousProbabilityTree
     *
     */
    public SPContinuousProbabilityTree copy() {

        SPContinuousProbabilityTree res = new SPContinuousProbabilityTree();
        int i;
        Vector cp = new Vector();

        res.label = getLabel();

        if (isContinuous()) {//Si es continua tiene cutPoints
            for (i = 0; i < (getNumberOfChildren() + 1); i++) {
                cp.addElement(new Double(getCutPoint(i)));
            }

            res.assignVar((Continuous) getVar(), cp);
        } else //Es discreta o prob
        if (isDiscrete())//Es discreta
        {
            res.assignVar((FiniteStates) getVar());
        }


        if (!isProbab())//Si no es probabilidad es pq tiene hijos
        {
            for (i = 0; i < getNumberOfChildren(); i++) {
                res.child.setElementAt(getChild(i), i);
            }
        } else // Es una probabilidad, luego tenemos que copiar su MTE
        {
            res.value = getProb();
        }

        return res;

    }//End of mehtod

    /**
     * Expands the leaves of the tree, by carrying out
     * the operations indicated in the expression.
     * The tree is modified.
     */
    public void expand() {
        if (isProbab()) {
            MixtExpDensity expanded;
            expanded = value.get(0).get(0);
            for (int i = 1; i < value.get(0).size(); i++) {
                expanded = expanded.multiplyDensities(value.get(0).get(i), 1);
            }
            for (int k = 1; k < value.size(); k++) {
                MixtExpDensity expandedK = value.get(k).get(0);
                for (int i = 1; i < value.get(k).size(); i++) {
                    expandedK = expandedK.multiplyDensities(value.get(k).get(i), 0);
                }
                expanded = expanded.sumDensities(expandedK);
            }
            value = new ArrayList<ArrayList<MixtExpDensity>>();
            value.add(new ArrayList<MixtExpDensity>());
            value.get(0).add(expanded);
        } else {
            for (int i = 0; i < child.size(); i++) {
                child.elementAt(i).expand();
            }
        }
    }
}
