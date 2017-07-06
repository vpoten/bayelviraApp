/* ProbabilityTree.java */

package elvira.potential;

import java.util.Vector;
import java.util.Hashtable;
import java.lang.Math;
import elvira.*;
import elvira.tools.FactorisationTools;
import elvira.inference.abduction.Explanation;
import elvira.tools.Distances;
import elvira.tools.VectorManipulator;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of class ProbabilityTree. A probability tree
 * is a compact representation of a probability distribution,
 * alternative to a probability table.
 * Each internal node represents a variable and each leaf
 * node represents a probability value. Each variable node
 * has as many children as possible values it has. The value stored
 * in a leaf corresponds to the probability of the
 * configuration that leads from the root node to that leaf.
 *
 * An object of this class is a node of the tree, that can point
 * to other nodes, forming a tree in this way.
 *
 * @author Andres Cano (acu@decsai.ugr.es)
 * @author Serafin Moral (smc@decsai.ugr.es)
 * @author Jose A. Gamez (jgamez@info-ab.uclm.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 * @author Manolo Gomez (mgomez@ujaen.es)
 * @author Irene Martinez (irene@ual.es)
 * @since 14/2/2005
 */


public class ProbabilityTree{
    
    /**
     * The variable associated with the node of the tree, if the node is
     * internal.
     */
    FiniteStates var;
    
    /**
     * The probability value, if the node is a leaf.
     */
    double value;
    
    /**
     * A label that indicates the type of the node:
     * 0: empty node.
     * 1: full node (internal node).
     * 2: probability node (a leaf).
     */
    int label;
    
    /**
     * A vector containing references to the successord of this node.
     */
    Vector child;
    
    /**
     * The number of leaves of the tree whose root is this node.
     */
    long leaves;
    
    
    static final int EMPTY_NODE=0;
    static final int FULL_NODE=1;
    static final int PROBAB_NODE=2;
    
    public static final int AVERAGE_APPROX=0;
    public static final int ZERO_APPROX=1;
    
    
    /**
     * Constructor. Creates an empty tree node.
     */
    
    public ProbabilityTree() {
        
        label = EMPTY_NODE;
        value = 0.0;
        leaves = 0;
        child = new Vector();
    }
    
    
    /**
     * Creates a tree with the argument as root node.
     * @param variable a <code>FiniteStates</code> variable.
     */
    
    public ProbabilityTree(FiniteStates variable) {
        
        int i, j;
        ProbabilityTree tree;
        
        label = FULL_NODE;
        value = 0.0;
        leaves = 0;
        var = variable;
        child = new Vector();
        
        j = variable.getNumStates();
        for (i=0 ; i<j ; i++) {
            tree = new ProbabilityTree();
            child.addElement(tree);
        }
    }
    
    
    /**
     * Creates a probability node with value p.
     * @param p a double value.
     */
    
    public ProbabilityTree(double p) {
        
        label = PROBAB_NODE;
        value = p;
        child = new Vector();
        leaves = 1;
    }
    
    
    /**
     * Creates a probability tree from a <code>ContinuousProbabilityTree</code> whith only
     * <code>FiniteStates</code> nodes
     */
    public ProbabilityTree(ContinuousProbabilityTree tree){
        
        int i;
        
        label=tree.getLabel();
        leaves=tree.getSize();
        
        if (label==PROBAB_NODE)
            value=tree.getProb().getIndependent();
        else{
            var=(FiniteStates)tree.getVar().copy();
            child=new Vector();
            for (i=0; i<tree.getNumberOfChildren(); i++){
                insertChild(new ProbabilityTree(tree.getChild(i)));
            }
        }
        
        
        
        
    }
    
    
    /**
     * Creates a <code>ProbabilityTree</code> constantly equal to 1.
     * @return a unit <code>ProbabilityTree</code>.
     */
    
    public static ProbabilityTree unitTree() {
        
        ProbabilityTree t;
        
        t = new ProbabilityTree();
        t.assignProb(1.0);
        
        return t;
    }
    
    /**
     * Determines whether the node is a leaf with value 1 or not.
     * @return <code>true</code> if the node is the probability value 1,  and
     * <code>false</code> otherwise.
     */
    
    public boolean isUnitNode() {
        
        if (label == PROBAB_NODE && value == 1.0)
            return true;
        else
            return false;
    }
    
    
    /**
     * Determines whether the node is a leaf or not.
     * @return <code>true</code> if the node is a probability and
     * <code>false</code> otherwise.
     */
    
    public boolean isProbab() {
        
        if (label == PROBAB_NODE)
            return true;
        
        return false;
    }
    
    
    /**
     * Determines whether the node is internal or not.
     * @return <code>true</code> if the node is a variable and
     * <code>false</code> otherwise.
     */
    
    public boolean isVariable() {
        
        if (label == FULL_NODE)
            return true;
        
        return false;
    }
    
    
    /**
     * Determines whether the node is empty or not.
     * @return <code>true</code> if the node is empty and
     * <code>false</code> otherwise.
     */
    
    public boolean isEmpty() {
        
        if (label == EMPTY_NODE)
            return true;
        
        return false;
    }
    
    
    /**
     * @return the label of the node.
     */
    
    public int getLabel() {
        
        return label;
    }
    
    /**
     * Sets the label.
     */
    
    public void setLabel(int l) {
        label=l;
    }
    
    /**
     * Sets the value.
     */
    
    public void setValue(double v) {
        value=v;
    }
    
    
    /**
     * Sets the value for a configuration.
     * @param conf a <code>Configuration</code>.
     * @param x a <code>double</code>, the new value for <code>conf</code>.
     */
    
    public void setValue(Configuration conf, double x) {
        
        Configuration aux;
        FiniteStates var;
        int i, p, val, s;
        ProbabilityTree tree;
        
        
        aux = conf.duplicate();
        s = conf.getVariables().size();
        tree = this;
        
        for (i=0 ; i<s ; i++) {
            
            if (!tree.isVariable()) {
                var = aux.getVariable(0);
                val = aux.getValue(0);
                aux.remove(0);
                
                if (tree.isProbab()){ // if the node is a probability,
                    tree.assignVar(var,tree.value);
                }
                else{
                    tree.assignVar(var);
                }
            }
            else {
                p = aux.indexOf(tree.getVar());
                var = aux.getVariable(p);
                val = aux.getValue(p);
                aux.remove(p);
            }
            
            tree = tree.getChild(val);
        }
        
        tree.assignProb(x);
    }
    
    
    
    /**
     * Sets the subtree beneath a configuration.
     * The argument tree is copied.
     * @param conf a <code>Configuration</code>.
     * @param x a <code>ProbabilityTree</code>, the new subtree for <code>conf</code>.
     */
    
    public void setSubTree(Configuration conf, ProbabilityTree x) {
        
        Configuration aux;
        FiniteStates var;
        int i, p, val, s;
        ProbabilityTree tree;
        
        
        aux = conf.duplicate();
        s = conf.getVariables().size();
        tree = this;
        
        for (i=0 ; i<s ; i++) {
            
            if (!tree.isVariable()) {
                var = aux.getVariable(0);
                val = aux.getValue(0);
                aux.remove(0);
                
                if (tree.isProbab()){ // if the node is a probability,
                    tree.assignVar(var,tree.value);
                }
                else{
                    tree.assignVar(var);
                }
            }
            else {
                p = aux.indexOf(tree.getVar());
                var = aux.getVariable(p);
                val = aux.getValue(p);
                aux.remove(p);
            }
            
            tree = tree.getChild(val);
        }
        
        //tree = x.copy();
        tree.assignVar(x.getVar());
        for (i=0 ; i <x.getChild().size() ; i++) {
            tree.setNewChild(x.getChild(i).copy(),i);
        }
    }
    
    
    /**
     * increases in one the number of leaves
     */
    
    public void oneMoreLeaf() {
        
        leaves++;
    }
    
    /**
     * @return the probability value attached to the node.
     */
    
    public double getProb() {
        
        return value;
    }
    
    
    /**
     * Gets the probability of a given configuration of variables.
     * @param conf a <code>Configuration</code>.
     * @return the probability value of the tree following
     * the path indicated by <code>Configuration conf</code>. The value
     * -1 is returned in case of error.
     */
    
    public double getProb(Configuration conf) {
        
        int p, val;
        ProbabilityTree tree;
        
        if (label == FULL_NODE) { // If the node is a variable
            
            val = conf.getValue(var);
            tree = (ProbabilityTree)child.elementAt(val);
            return(tree.getProb(conf));
        }
        else {
            if (label == PROBAB_NODE) // If the node is a prob.
                return value;
            else
                return(-1.0);
        }
    }
    
    
    /**
     * Gets the probability of a configuration of variables specified by
     * means of an array of <code>int</code>. The position of each variable
     * in this array is stored in a <code>Hashtable</code> given as argument.
     * @param positions a <code>Hashtable</code> with the positions of the
     * variables in conf.
     * @param conf an array of <code>int</code> with a configuration.
     * @return the value of the tree for the configuration. In case of error,
     * value -1 is returned.
     */
    
    public double getProb(Hashtable positions, int[] conf) {
        
        int p, val;
        ProbabilityTree tree;
        
        if (label == FULL_NODE) { // If the node is a variable,
            // call the same method with the
            // corresponding child.
            p = ((Integer)positions.get(var)).intValue();
            val = conf[p];
            
            tree = (ProbabilityTree)child.elementAt(val);
            return (tree.getProb(positions,conf));
        }
        else {
            if (label == PROBAB_NODE)
                return value;
            else
                return(-1.0);
        }
    }
    
    /**
     * Multiplies the content of vector  <code>  values </code> by
     * each one of the numbers in the probability tree for different cases
     * of the variable in position <code> posX </code> restricted to <code> conf </code>.
     * In this case, the
     * configuration is represented by means of an array of <code>int</code>.
     * At each position, the value for a certain variable is stored.
     * To know the position in the array corresponding to a given
     * variable, we use a hash table. In that hash table, the
     * position of every variable in the array is stored.
     *
     * @param positions a <code>Hashtable</code> with the positions of the
     * variables in the array.
     * @param posX the position of the variable for which is defined the vectors <code> values </code>
     * @param nv number of states of the variable of position <code> posX </code>
     * @param conf an array of <code>int</code> with the values of the variables.
     * @param values the vector containing the numbers that are going to be multiplied
     *
     */
    
    public void getVectors(Hashtable positions, int posX, int nv, int[] conf, double[] values){
        int p, val,i;
        ProbabilityTree tree;
        
        if (label == FULL_NODE) { // If the node is a variable,
            // call the same method with the
            // corresponding child.
            p = ((Integer)positions.get(var)).intValue();
            
            
            
            
            if (p == posX){
                for(i=0; i<nv;i++){
                    tree = (ProbabilityTree) child.elementAt(i);
                    values[i] *= tree.getProb(positions,conf);
                }
            }
            else {
                val = conf[p];
                tree = (ProbabilityTree)child.elementAt(val);
                tree.getVectors(positions,posX,nv,conf,values);
            }
        }
        else {
            if (label == PROBAB_NODE) {
                
                for(i=0; i<nv;i++){
                    values[i] *= getProb();
                }
            }
            else
                return;
        }
        
        
        
        
    }
    
    
    /**
     * It adds to <code> ActiveNodes </code> all the variables in <code> conf </code>
     * appearing  when following
     * the path associated to <code>  conf</code>.
     * In this case, the
     * configuration is represented by means of an array of <code>int</code>.
     * At each position, the value for a certain variable is stored.
     * To know the position in the array corresponding to a given
     * variable, we use a hash table. In that hash table, the
     * position of every variable in the array is stored.
     *
     *
     *
     * @param positions a <code>Hashtable</code> with the positions of the
     * variables in the array.
     * @param posX the position of the variable for which is defined the vectors <code> values </code>
     * @param conf an array of <code>int</code> with the values of the variables.
     * @param activeNodes a vector to which the found variables are added
     *
     */
    
    public void getActiveNodes(Hashtable positions,int posX ,int[] conf, Vector activeNodes){
        int nv,p, val,i;
        ProbabilityTree tree;
        
        if (label == FULL_NODE) { // If the node is a variable,
            // call the same method with the
            // corresponding child.
            p = ((Integer)positions.get(var)).intValue();
            
            
            
            if (p == posX){
                nv = var.getNumStates();
                for(i=0; i<nv;i++){
                    tree = (ProbabilityTree) child.elementAt(i);
                    tree.getActiveNodes(positions,posX,conf,activeNodes);
                }
            }
            else {
                val = conf[p];
                tree = (ProbabilityTree)child.elementAt(val);
                if (!activeNodes.contains(var)){activeNodes.addElement(var);}
                tree.getActiveNodes(positions,posX,conf,activeNodes);
            }
        }
        else {
            return;
        }
        
    }
    
    
    
    
    
    
    
    
    /**
     * Extends a tree expanding it in such a way that the assigns the value
     * <code> newVal </code> to the given configuration.
     * It has two modalities depending of the value of <code> mode </code>.
     * With <code> mode</code> equal to 1, it carries out a full expansion for
     * all the variables in the list <code> aux </code>.
     * With <code> mode</code> equal to 0, it expands only for one of the variables in the
     * list <code> aux </code> for which the potential tree was not already branched
     *
     *
     * The configuration is represented by means of an array of <code>int</code>.
     * At each position, the value for a certain variable is stored.
     * To know the position in the array corresponding to a given
     * variable, we use a hash table. In that hash table, the
     * position of every variable in the array is stored.
     *
     * @param positions a <code>Hashtable</code> with the positions of the
     * variables in the array.
     * @param conf an array of <code>int</code> with the values of the variables.
     * @param newVal the new value of the potential in the given configuration
     * @param aux the list of variables for which the tree is defined
     * @param mode an integer determining the type of branching (0: full, 1: one variable)
     *
     **/
    
    public void update(Hashtable positions,  int[] conf, double newVal, NodeList aux, int mode){
        
        ProbabilityTree tree;
        FiniteStates n;
        double oldVal;
        int i,nv,p,val;
        
        
        if (label == FULL_NODE) {
            p = ((Integer)positions.get(var)).intValue();
            val = conf[p];
            tree = (ProbabilityTree)child.elementAt(val);
            aux.removeNode(var);
            tree.update(positions,conf,newVal,aux,mode);
        }
        else if (label == PROBAB_NODE) {
            if (aux.size() == 0){
                value=newVal;
            }
            else {
                oldVal = value;
                n = (FiniteStates) aux.elementAt(aux.size()-1);
                aux.removeNode(aux.size()-1);
                assignVar(n);
                nv = n.getNumStates();
                p = ((Integer)positions.get(var)).intValue();
                val = conf[p];
                for (i=0;i<nv; i++){
                    tree = getChild(i);
                    tree.assignProb(oldVal);
                    if (val==i){
                        if (mode==0){
                            tree.update(positions,conf,newVal,aux,mode);
                        }
                        else {
                            tree.assignProb(newVal);
                        }
                    }
                }
            }
        }
        
        
        
    }
    
    
    
    
    
    
    /**
     * Assigns a value to the node.
     * Also, sets the label to <code>PROBAB_NODE</code>.
     * @param p a <code>double</code> value.
     */
    
    public void assignProb(double p) {
        
        label = PROBAB_NODE;
        value = p;
        leaves = 1;
    }
    
    
    /**
     * Assigns a variable to an empty node.
     * Initializes as many children as values of the
     * variable, to empty trees.
     * @param variable a <code>FiniteStates</code> variable that will be
     * assigned to the node.
     */
    
    public void assignVar(FiniteStates variable) {
        
        ProbabilityTree tree;
        int i,j;
        
        var = variable;
        label = FULL_NODE;
        child = new Vector();
        j = variable.getNumStates();
        
        for (i=0 ; i<j ; i++) {
            tree = new ProbabilityTree();
            child.addElement(tree);
        }
    }
    
    /**
     * Assigns a variable to an empty node.
     * Initializes as many children as values of the
     * variable, to empty trees.
     * @param variable a <code>FiniteStates</code> variable that will be
     * @param value to assign
     * assigned to the node.
     */
    
    public void assignVar(FiniteStates variable,double value) {
        
        ProbabilityTree tree;
        int i,j;
        
        var = variable;
        label = FULL_NODE;
        child = new Vector();
        j = variable.getNumStates();
        
        for (i=0 ; i<j ; i++) {
            tree = new ProbabilityTree(value);
            child.addElement(tree);
        }
    }
    
    
    /**
     * Gets the variable associatd with the node.
     * @return the <code>FiniteStates</code> variable stored in the tree node.
     */
    
    public FiniteStates getVar() {
        
        return var;
    }
    
    
    /**
     * Gets the vector of children of this node.
     * @return the <code>Vector</code> of children of the node.
     */
    
    public Vector getChild() {
        
        return child;
    }
    
    
    /**
     * Gets one of the children of this node.
     * @param i an <code>int</code> value: number of child to be returned
     * (first child is i=0).
     * @return the i-th child of the tree.
     */
    
    public ProbabilityTree getChild(int i) {
        
        return ((ProbabilityTree)(child.elementAt(i)));
    }
    
    
    /**
     * Inserts a <code>ProbabilityTree</code> as child of this node.
     * It is inserted at the end of the vector of children.
     * @param tree <code>ProbabilityTree</code> to be added as child.
     */
    
    public void insertChild(ProbabilityTree tree) {
        
        child.addElement(tree);
    }
    
    
    /**
     * Copies this tree. Only references are copied.
     * @return a <code>ProbabilityTree</code> copy of this one.
     */
    
    public ProbabilityTree copy() {
        
        ProbabilityTree tree, tree2;
        int i, nv;
        
        tree = new ProbabilityTree();
        tree.var = var;
        tree.label = FULL_NODE;
        tree.leaves = leaves;
        
        if (label != PROBAB_NODE) { // If it is not a probability,
            
            nv = child.size();
            
            for (i=0 ; i<nv ; i++) {
                tree2 = ((ProbabilityTree)child.elementAt(i)).copy();
                tree.child.addElement(tree2);
            }
        }
        else
            tree.assignProb(value);
        
        return tree;
    }
    
    
    /**
     * Restricts the tree starting from this node to a given value
     * of a variable.
     * @param variable a <code>FiniteStates</code> variable to which the tree
     * will be restricted.
     * @param v the <code>int</code> value of the variable to instantiate
     * (first value = 0).
     * @return a new <code>ProbabilityTree</code> consisting of the restriction
     * of the current tree to the value number v of
     * variable <code>variable</code>.
     */
    
    public ProbabilityTree restrict(FiniteStates variable, int v) {
        
        ProbabilityTree tree, tree2;
        int i, nv;
        
        if (label == PROBAB_NODE) {
            tree = new ProbabilityTree();
            tree.assignProb(value);
            return tree;
        }
        
        if (var == variable)
            tree = getChild(v).copy();
        else {
            tree = new ProbabilityTree();
            tree.var = var;
            tree.label = FULL_NODE;
            
            nv = child.size();
            for (i=0 ; i<nv ; i++) {
                tree2 = ((ProbabilityTree)child.elementAt(i)).restrict(variable,v);
                tree.child.addElement(tree2);
                tree.leaves += tree2.leaves;
            }
        }
        
        return tree;
    }
    
    
    /**
     * Restricts the tree starting in this node to a
     * <code>Configuration</code> of variables.
     * @param conf the <code>Configuration</code> to which the tree
     * will be restricted.
     * @return a new <code>ProbabilityTree</code> consisting of the restriction
     * of the current tree to the values of <code>Configuration conf</code>.
     */
    
    public ProbabilityTree restrict(Configuration conf) {
        
        ProbabilityTree tree, tree2;
        int i, nv, index;
        
        if (label == PROBAB_NODE) {
            tree = new ProbabilityTree();
            tree.assignProb(value);
            return tree;
        }
        
        index = conf.indexOf(var);
        if (index > -1) // if var is in conf
            tree = getChild(conf.getValue(index)).restrict(conf);
        else {
            tree = new ProbabilityTree();
            tree.var = var;
            tree.label = FULL_NODE;
            
            nv = child.size();
            for (i=0 ; i<nv ; i++) {
                tree2 = ((ProbabilityTree) child.elementAt(i)).restrict(conf);
                tree.child.addElement(tree2);
                tree.leaves += tree2.leaves;
            }
        }
        
        return tree;
    }
    
    
    /**
     * Combines two trees. This operation is analogous to the pointwise
     * product of two probability tables.
     * To be used as a static function.
     * @param tree1 a <code>ProbabilityTree</code>.
     * @param tree2 a <code>ProbabilityTree</code> to be multiplied with
     * <code>tree1</code>.
     * @return a new <code>ProbabilityTree</code> resulting from combining
     * <code>tree1</code> and <code>tree2</code>.
     */
    
    public static ProbabilityTree combine(ProbabilityTree tree1,
    ProbabilityTree tree2) {
        
        ProbabilityTree tree, tree3, tree4;
        int i, nv;
        
        if (tree1.getLabel() == PROBAB_NODE) { // Probability node.
            if (tree2.getLabel() == PROBAB_NODE) {
                tree = new ProbabilityTree(tree1.getProb() * tree2.getProb());
            }
            else {
                tree = new ProbabilityTree();
                tree.var = tree2.getVar();
                tree.label = FULL_NODE;
                
                nv = tree2.child.size();
                for (i=0 ; i<nv ; i++) {
                    tree3 = ProbabilityTree.combine(tree1,tree2.getChild(i));
                    tree.insertChild(tree3);
                    tree.leaves += tree3.leaves;
                }
            }
        }
        else {
            tree = new ProbabilityTree();
            tree.var = tree1.getVar();
            tree.label = FULL_NODE;
            
            nv = tree1.child.size();
            for (i=0 ; i<nv ; i++) {
                tree3 = tree2.restrict(tree1.getVar(),i);
                tree4 = ProbabilityTree.combine(tree1.getChild(i),tree3);
                tree.insertChild(tree4);
                tree.leaves += tree4.leaves;
            }
        }
        
        return tree;
    }
    
    /**
     * Sums two trees. This operation is analogous to the pointwise
     * sum of two probability tables.
     * To be used as a static function.
     * @param tree1 a <code>ProbabilityTree</code>.
     * @param tree2 a <code>ProbabilityTree</code> to be sum with
     * <code>tree1</code>.
     * @return a new <code>ProbabilityTree</code> resulting from suming
     * <code>tree1</code> and <code>tree2</code>.
     */
    
    public static ProbabilityTree sum(ProbabilityTree tree1,
    ProbabilityTree tree2) {
        ProbabilityTree tree, tree3, tree4;
        int i, nv;
        
        if (tree1.getLabel() == PROBAB_NODE) { // Probability node.
            if (tree2.getLabel() == PROBAB_NODE) {
                tree = new ProbabilityTree(tree1.getProb() + tree2.getProb());
            }
            else {
                tree = new ProbabilityTree();
                tree.var = tree2.getVar();
                tree.label = FULL_NODE;
                
                nv = tree2.child.size();
                for (i=0 ; i<nv ; i++) {
                    tree3 = ProbabilityTree.sum(tree1,tree2.getChild(i));
                    tree.insertChild(tree3);
                    tree.leaves += tree3.leaves;
                }
            }
        }
        else {
            tree = new ProbabilityTree();
            tree.var = tree1.getVar();
            tree.label = FULL_NODE;
            nv = tree1.child.size();
            for (i=0 ; i<nv ; i++) {
                tree3 = tree2.restrict(tree1.getVar(),i);
                tree4 = ProbabilityTree.sum(tree1.getChild(i),tree3);
                tree.insertChild(tree4);
                tree.leaves += tree4.leaves;
            }
        }
        return tree;
    }
    
    /**
     * Divides two trees.
     * To be used as a static function.
     *
     * For the exception 0/0, the method computes the result as 0.
     * The exception ?/0: the method reorts an error message to the
     * standard output.
     *
     * @param tree1 a <code>ProbabilityTree</code>.
     * @param tree2 a <code>ProbabilityTree</code>.
     * @return a new <code>ProbabilityTree</code> resulting from combining
     * <code>tree1</code> and <code>tree2</code>.
     */
    
    public static ProbabilityTree divide(ProbabilityTree tree1,
    ProbabilityTree tree2) {
        
        ProbabilityTree tree, tree3, tree4;
        int i, nv;
        double x,y,x2;
        
        if (tree1.getLabel() == PROBAB_NODE) { // Probability node.
            if (tree2.getLabel() == PROBAB_NODE) {
                x = tree1.getProb();
                y = tree2.getProb();
                x2 = x;
                
                if (y == 0.0) {
                    if (x == 0.0)
                        x = 0;
                    else {
                        // System.out.println("Division by zero: " + x2 +"/" + y);
                        x = 0;
                    }
                }
                else
                    x /= y;
                
                if (Double.isInfinite(x))
                    System.out.println(x2 + "/" + y + " equals infinite.");
                
                tree = new ProbabilityTree(x);
                tree.leaves = 1;
            }
            else {
                tree = new ProbabilityTree();
                tree.var = tree2.getVar();
                tree.label = FULL_NODE;
                tree.leaves = 1;
                
                nv = tree2.child.size();
                for (i=0 ; i<nv ; i++) {
                    tree3 = ProbabilityTree.divide(tree1,tree2.getChild(i));
                    tree.insertChild(tree3);
                    tree.leaves += tree3.leaves;
                }
            }
        }
        else {
            tree = new ProbabilityTree();
            tree.var = tree1.getVar();
            tree.label = FULL_NODE;
            
            nv = tree1.child.size();
            for (i=0 ; i<nv ; i++) {
                tree3 = tree2.restrict(tree1.getVar(),i);
                tree4 = ProbabilityTree.divide(tree1.getChild(i),tree3);
                tree.insertChild(tree4);
                tree.leaves += tree4.leaves;
            }
        }
        
        return tree;
    }
    
    
    /**
     * Adds a <code>ProbabilityTree</code> to the one starting in this node.
     * @param tree the <code>ProbabilityTree</code> to add to this one.
     * @return a new <code>ProbabilityTree</code> with the addition of
     * <code>tree</code> and the tree starting here.
     */
    
    public ProbabilityTree add(ProbabilityTree tree) {
        
        ProbabilityTree tree1, treeH;
        int i, nv;
        
        if (label == PROBAB_NODE) {
            if (tree.getLabel() == PROBAB_NODE) /* If both are probabilities */
                tree1 = new ProbabilityTree(value + tree.getProb());
            else {
                tree1 = new ProbabilityTree();
                tree1.var = tree.getVar();
                tree1.label = FULL_NODE;
                
                nv = tree.getChild().size();
                //tree2 = new ProbabilityTree(value);
                for (i=0 ; i<nv ; i++) {
                    treeH = tree.getChild(i);
                    treeH = treeH.add(this);
                    tree1.insertChild(treeH);
                    tree1.leaves += treeH.leaves;
                }
            }
        }
        else {
            tree1 = new ProbabilityTree();
            tree1.var = var;
            tree1.label = FULL_NODE;
            
            nv = child.size();
            
            for (i=0 ; i<nv ; i++) {
                treeH = getChild(i);
                treeH = treeH.add(tree.restrict(var,i));
                tree1.insertChild(treeH);
                tree1.leaves += treeH.leaves;
            }
        }
        
        return tree1;
    }
    
    
    /**
     * Removes a variable by summing over all its values.
     * @param variable a <code>FiniteStates</code> variable to remove from
     * the tree.
     * @return a new <code>ProbabilityTree</code> with the result of the
     * operation.
     */
    
    public ProbabilityTree addVariable(FiniteStates variable) {
        
        ProbabilityTree tree, treeH;
        int i, nv;
        
        
        if (label == PROBAB_NODE)
            tree = new ProbabilityTree(value * variable.getNumStates());
        else {
            if (var == variable)
                tree = addChildren();
            else {
                tree = new ProbabilityTree();
                tree.var = var;
                tree.label = FULL_NODE;
                
                nv = child.size();
                for (i=0 ; i<nv ; i++) {
                    treeH = getChild(i).addVariable(variable);
                    tree.insertChild(treeH);
                    tree.leaves += treeH.leaves;
                }
            }
        }
        
        return tree;
    }
    
    
    /**
     * Adds all the children of this node.
     * @return a new <code>ProbabilityTree</code> equal to the addition of
     * all the children of this node.
     */
    
    public ProbabilityTree addChildren() {
        
        ProbabilityTree tree;
        int i, nv;
        
        tree = getChild(0);
        nv = child.size();
        for (i=1 ; i<nv ; i++)
            tree = tree.add(getChild(i));
        
        return tree;
    }
    
    
    /**
     * Remove all the children of this node.
     */
    
    public void removeAllChildren() {
        child.removeAllElements();
    }
    
    
    /**
     * @param a a <code>double</code> value.
     * @param b a <code>double</code> value.
     * @return the maximum of <code>a</code> and <code>b</code>.
     */
    
    public double maximum(double a, double b) {
        
        if (a >= b)
            return a;
        else
            return b;
    }
    
    
    /**
     * Integrate the argument tree to this by applying maximization.
     * @param tree a <code>ProbabilityTree</code>.
     * @return a new <code>ProbabilityTree</code> with the addition of
     * <code>tree</code> and the tree starting in this node.
     */
    
    public ProbabilityTree max(ProbabilityTree tree) {
        
        ProbabilityTree tree1, tree2, treeH;
        int i, nv;
        
        if (label == PROBAB_NODE) {
            if (tree.getLabel() == PROBAB_NODE) // If both are probabilities
                tree1 = new ProbabilityTree(maximum(value,tree.getProb()));
            else {
                tree1 = new ProbabilityTree();
                tree1.var = tree.getVar();
                tree1.leaves = 0;
                tree1.label = 1;
                
                nv = tree.getChild().size();
                tree2 = new ProbabilityTree(value);
                for (i=0 ; i<nv ; i++) {
                    treeH = tree.getChild(i).restrict(tree.getVar(),i);
                    treeH = treeH.max(tree2);
                    tree1.insertChild(treeH);
                    tree1.leaves += treeH.leaves;
                }
            }
        }
        else {
            tree1 = new ProbabilityTree();
            tree1.var = var;
            tree1.leaves = 0;
            tree1.label = 1;
            
            nv = child.size();
            
            for (i=0 ; i<nv ; i++) {
                treeH = getChild(i).restrict(var,i);
                treeH = treeH.max(tree.restrict(var,i));
                tree1.insertChild(treeH);
                treeH.leaves += tree1.leaves;
            }
        }
        
        return tree1;
    }
    
    
    /**
     * @return a new <code>ProbabilityTree</code> equal to the maximization of
     * all the children of the tree starting in this node.
     */
    
    public ProbabilityTree maxChildren() {
        
        ProbabilityTree tree;
        int i, nv;
        
        tree = getChild(0);
        
        nv = child.size();
        for (i=1 ; i<nv ; i++)
            tree = tree.max(getChild(i));
        
        return tree;
    }
    
    
    /**
     * Removes a variable by maximizing over it.
     * @param variable a <code>FiniteStates</code> variable to remove.
     * @return a new <code>ProbabilityTree</code> with the result of
     * the operation.
     */
    
    public ProbabilityTree maximizeOverVariable(FiniteStates variable) {
        
        ProbabilityTree tree, treeH;
        int i, nv;
        
        
        if (label == PROBAB_NODE)
            tree = new ProbabilityTree(value); // the value to return is the same.
        else {
            if (var == variable)
                tree = maxChildren();
            else {
                tree = new ProbabilityTree();
                tree.var = var;
                tree.label = 1;
                tree.leaves = 0;
                
                nv = child.size();
                for (i=0 ; i<nv ; i++) {
                    treeH = getChild(i).restrict(var,i).maximizeOverVariable(variable);
                    tree.insertChild(treeH);
                    tree.leaves += treeH.leaves;
                }
            }
        }
        
        return tree;
    }
    
    
    /**
     * @return the configuration of maximum probability included in the
     * tree, that is consistent with the subConfiguration passed as
     * parameter (this subconfiguration can be empty).
     *
     * NOTE: if there are more than one configuration with maximum
     * probability, the first one is returned.
     *
     * @param bestExpl the best explanation found until this moment
     *  (configuration + probability)
     * @param conf the configuration built following the path from
     *        this node until the root.
     * @param subconf the subconfiguration to ensure consistency.
     */
    
    public Explanation getMaxConfiguration(Explanation bestExpl,
    Configuration conf,
    Configuration subconf) {
        
        int pos, numChildren, i;
        ProbabilityTree tree;
        Explanation exp=new Explanation();
        double prob;
        Configuration c;
        
        if (isProbab()) {
            if (value > bestExpl.getProb()) {
                bestExpl.setProb(value);
                
                // setting in conf the values of subconf
                for(i=0 ; i<subconf.size() ; i++)
                    conf.putValue(subconf.getVariable(i),subconf.getValue(i));
                
                // copying conf in bestExpl
                for (i=0 ; i<conf.size() ; i++)
                    bestExpl.getConf().putValue(conf.getVariable(i),conf.getValue(i));
            }
        }
        else if (isVariable()) {
            pos = subconf.indexOf(var);
            if (pos == -1) { // all the children are explored
                numChildren = child.size();
                
                prob = -1.0;
                for(i=0 ; i<numChildren ; i++) {
                    tree = (ProbabilityTree) child.elementAt(i);
                    conf.putValue(var,i);
                    bestExpl = tree.getMaxConfiguration(bestExpl,conf,subconf);
                    if (bestExpl.getProb() > prob){
                        prob = bestExpl.getProb();
                        c = (bestExpl.getConf()).duplicate();
                        exp = new Explanation(c,prob);
                    }
                }
                bestExpl = exp;
            }
            else { // only the child with the value in subconf is explored
                tree = (ProbabilityTree) child.elementAt(subconf.getValue(pos));
                conf.putValue(var,subconf.getValue(pos));
                bestExpl = tree.getMaxConfiguration(bestExpl,conf,subconf);
            }
            // restoring the value -1 for var in conf
            conf.putValue(var,-1);
        }
        else { // empty node
            System.out.println("Error detected at ProbabilityTree.getMaxConfiguration");
            System.out.println("An empty node was found");
            System.exit(0);
        }
        
        return bestExpl;
    }
    
    
    /**
     * @return the configuration of maximum probability included in the
     * potential, that is consistent with the subConfiguration passed as
     * parameter (this subconfiguration can be empty), and differents to
     * all the configurations passed in the vector
     *
     * NOTE: if there are more than one configuration with maximum
     * probability, the first one is returned
     *
     * @param bestExpl the best explanation found until this moment
     *  (configuration + probability)
     * @param conf the configuration built following the path from
     *        this node until the root.
     * @param subconf the subconfiguration to ensure consistency
     * @param list the list of configurations to be differents
     */
    
    public Explanation getMaxConfiguration(Explanation bestExpl,
    Configuration conf,
    Configuration subconf,
    Vector list) {
        
        int pos, numChildren, i;
        ProbabilityTree tree;
        Explanation exp=new Explanation();
        Configuration firstConf,c;
        double prob;
        
        if (isProbab()) {
            if (value > bestExpl.getProb()) {
                // setting in conf the values of subconf
                for(i=0 ; i<subconf.size() ; i++)
                    conf.putValue(subconf.getVariable(i),subconf.getValue(i));
                
                firstConf = conf.getFirstNotInList(list);
                
                if (firstConf.size() > 0) {
                    bestExpl.setProb(value);
                    // copying conf in bestExpl
                    if (bestExpl.getConf().size() == 0)
                        bestExpl.setConf(new Configuration(conf.getVariables()));
                    for (i=0 ; i<conf.size() ; i++)
                        bestExpl.getConf().putValue(firstConf.getVariable(i),
                        firstConf.getValue(i));
                }
            }
        }
        else if (isVariable()) {
            pos = subconf.indexOf(var);
            
            if (pos == -1) { // all the children are explored
                numChildren = child.size();
                prob = -1.0;
                for(i=0 ; i<numChildren ; i++) {
                    tree = (ProbabilityTree) child.elementAt(i);
                    conf.putValue(var,i);
                    bestExpl = tree.getMaxConfiguration(bestExpl,conf,subconf,list);
                    if (bestExpl.getProb() > prob){
                        c = (bestExpl.getConf()).getFirstNotInList(list);
                        if (c.size()>0){
                            prob = bestExpl.getProb();
                            c = (bestExpl.getConf()).duplicate();
                            exp = new Explanation(c,prob);
                        }
                    }
                }
                bestExpl = exp;
            }
            else { // only the child with the value in subconf is explored
                tree = (ProbabilityTree) child.elementAt(subconf.getValue(pos));
                conf.putValue(var,subconf.getValue(pos));
                bestExpl = tree.getMaxConfiguration(bestExpl,conf,subconf,list);
            }
            // restoring the value -1 for var in conf
            conf.putValue(var,-1);
        }
        else { // empty node
            System.out.println("Error detected at ProbabilityTree.getMaxConfiguration");
            System.out.println("An empty node was found");
            System.exit(0);
        }
        
        
        return bestExpl;
    }
    
    
    /**
     * Gets the list of variables in the tree starting from this node.
     * @return the list of nodes, as a <code>NodeList</code>.
     */
    
    public NodeList getVarList() {
        
        NodeList list;
        ProbabilityTree tree;
        int i;
        
        list = new NodeList();
        
        if (label != FULL_NODE)
            return list;
        
        list.insertNode(var);
        
        for (i=0 ; i<var.getNumStates() ; i++) {
            tree = getChild(i);
            list.merge(tree.getVarList());
        }
        
        return list;
    }
    
    
    /**
     * Computes the information of a variable within a tree.
     * @param variable a <code>FiniteStates</code> variable.
     * @param potentialSize maximum size of the potential containing
     * the tree (the size if it were completely expanded).
     * @return the value of information of variable.
     */
    
    public double information(FiniteStates variable,
    long potentialSize) {
        
        ProbabilityTree tree;
        int i, nv;
        long newSize;
        double entropy = 0.0, s = 0.0, totalS = 0.0, info = 0.0;
        
        nv = variable.getNumStates();
        newSize = potentialSize / nv;
        
        for (i=0 ; i<nv ; i++) {
            tree = restrict(variable,i);
            s = tree.sum(newSize);
            if (s !=0)
                entropy += -s * Math.log(s);
            totalS += s;
        }
        
        if (totalS == 0.0)
            info = 0.0;
        else
            info = Math.log(nv) - Math.log(totalS) - (entropy / totalS);
        return (totalS * info);
    }
    
    /**
     * Computes the information of a variable within a tree, but
     * the potential represents an utility function.
     * @param <code>FiniteStates</code> variable to measure
     * @param potentialSize, the maximun size of the potential
     * containing the tree (i.e. the size the tree would have if
     * were completely extended).
     * @return the value of information variable
     */
    
    public double informationUtility(FiniteStates variable,
    long potentialSize) {
        ProbabilityTree tree;
        int i, nv;
        long newSize;
        double s = 0.0, totalD = 0.0;
        Vector ceroSumms=new Vector();
        ceroSumms.addElement(new Double(0));
        
        // Get the number of states for the variable to measure
        nv = variable.getNumStates();
        
        // The rest of the tree would have this size, after
        // restrict the potential to eliminate variable from it
        newSize = potentialSize / nv;
        
        // For each one of the values for variable
        for (i=0 ; i<nv ; i++) {
            // Restrict the tree respect to the considered value
            tree = restrict(variable,i);
            
            // Sums over restricted tree.  newsize shows the size of the
            // tree if it were completely extended
            s = tree.sum(newSize)/newSize;
            
            // Now compare this value respect to the complete tree,
            // with the euclidean distance
            totalD+=tree.utilityDistance(newSize,s,ceroSumms);
        }
        
        double ceros=((Double)ceroSumms.elementAt(0)).doubleValue();
        totalD=totalD-totalD*(ceros/potentialSize);
        
        // Get the square root
        totalD=Math.sqrt(totalD);
        
        // Return the final distance
        
        return (totalD);
    }
    
    /**
     * Computes the distance between a value and the contents of
     * the tree
     * @param treeSize size of the completely expanded tree.
     * @param reference, the value to compare with the tree
     * @param ceroSumms, vector to store the number of situations
     * where the difference is 0
     * @return the estimation of distance
     */
    
    private double utilityDistance(long treeSize,double reference, Vector ceroSumms){
        double s = 0.0, dif = 0.0;
        int i, nv;
        long newSize;
        double ceros=((Double)ceroSumms.elementAt(0)).doubleValue();
        
        if (label == PROBAB_NODE){
            // May be the tree was truncated and a single node represents
            // multiple values (all of them will be the same)
            s = value-reference;

            if (s == 0)
                ceros++;
            ceroSumms.setElementAt(new Double(ceros),0);
            s = Math.pow(s,2);
        }
        else {
            nv = var.getNumStates();
            newSize = treeSize / nv;
            for (i=0 ; i<nv ; i++){
                s += getChild(i).utilityDistance(newSize,reference,ceroSumms);
            }
        }
        
        // Return the value
        
        return (s);
    }
    
    /**
     * Computes the addition of all the values in the tree starting
     * in this node.
     * @param treeSize size of the completely expanded tree.
     * @return the addition computed.
     */
    
    public double sum(long treeSize) {
        
        double s = 0.0;
        int i, nv;
        long newSize;
        
        if (label == PROBAB_NODE){
            s = (double)treeSize * value;
        }
        else {
            nv = var.getNumStates();
            newSize = treeSize / nv;
            for (i=0 ; i<nv ; i++)
                s += getChild(i).sum(newSize);
        }
        
        return s;
    }
    
    
    /**
     * Computes the average of the values in the tree.
     * @return the average.
     */
    
    public double average() {
        
        double av = 0.0;
        int i, nv;
        
        if (label == PROBAB_NODE)
            av = value;
        else {
            nv = var.getNumStates();
            for (i=0 ; i<nv ; i++)
                av += getChild(i).average();
            
            av /= (double)nv;
        }
        
        return av;
    }
    
    /**
     * Computes the maximum of the values in the tree.
     * @return the maximum.
     */
    
    public double maximumValue() {
        
        double av,max = -1;
        int i, nv;
        
        if (label == PROBAB_NODE){
            av = value;
            
            // Initialize max, if needed
            
            if (max == -1)
                max=av;
            else{
                //Actualize its value, if needed
                
                if (av > max)
                    max=av;
            }
        }
        else {
            nv = var.getNumStates();
            for (i=0 ; i<nv ; i++){
                av=getChild(i).maximumValue();
                
                // If max is not initialized, do it
                
                if (max == -1)
                    max=av;
                else{
                    // Actualize if needed
                    
                    if (av > max)
                        max=av;
                }
            }
        }
        
        // Return the maximum value
        
        return max;
    }
    
    /**
     * Computes the minimum of the values in the tree.
     * @return the minimum.
     */
    
    public double minimumValue() {
        
        double av,mim = -1;
        int i, nv;
        
        if (label == PROBAB_NODE){
            av = value;
            
            // Initialize mim, if needed
            
            if (mim == -1)
                mim=av;
            else{
                //Actualize its value, if needed
                
                if (av > mim)
                    mim=av;
            }
        }
        else {
            nv = var.getNumStates();
            for (i=0 ; i<nv ; i++){
                av=getChild(i).minimumValue();
                
                // If max is not initialized, do it
                
                if (mim == -1)
                    mim=av;
                else{
                    // Actualize if needed
                    
                    if (av < mim)
                        mim=av;
                }
            }
        }
        
        // Return the minimum value
        
        return mim;
    }
    
    /**
     * Finds the value in a tree such that are <code>limit</code>
     * leaves on it greater than that value.
     * @param limit the number of leaves to consider.
     * @return the value found.
     */
    
    public double findLimitValue(int limit) {
        
        //v = new Vector(limit);
        double[] v = new double[(int)limit+1];
        double limitFound = 0;
        int i, inserted = 0, totalValues = 0, topPosition;
        
        if ((leaves <= limit) || (isProbab())) // No need to continue
            return 0;
        
        // At the end of this for the value below which
        // leaves will be removed is stored at the first
        // position of array v.
        
        for (i=0 ; i<getChild().size() ; i++) {
            inserted = getChild(i).insertOrderedLeaves(v,totalValues,limit);
            totalValues += inserted;
        }
        if (totalValues > limit)
            topPosition = (int)limit;
        else
            topPosition = (int)totalValues;
        //
        System.out.println("UN VECTOR");
        for (i=0 ; i<topPosition ; i++)
            System.out.println(v[i]);
        System.out.println("FIN VECTOR");
        print(4);
        //
        
        
        limitFound = v[topPosition - 1];
        
        return (limitFound);
    }
    
    
    /**
     * Inserts in an array the values stored in leaves beneath
     * this node.
     * @param v the array where the values will be inserted.
     * @param totalValues the number of values contained in
     * array <code>v</code>.
     * @param limit the higher number of values to be stored in
     * array <code>v</code>.
     * @return the number of values inserted.
     */
    
    public int insertOrderedLeaves(double[] v, int totalValues,
    int limit) {
        
        int i, j, total, inserted = 0, endPos;
        double val;
        boolean found;
        
        if (isProbab()) {
            val = getProb();
            
            
            
            if (totalValues < limit)
                endPos = totalValues;
            else
                endPos = limit;
            
            inserted++;
            
            i = endPos;
            found = false;
            
            while ((!found) && (i>0)) {
                if (v[i-1] >= val) {
                    v[i] = val;
                    found = true;
                }
                else {
                    v[i] = v[i-1];
                    i--;
                }
            }
            if (!found)
                v[0] = val;
            
        }
        else { // This node is not a leaf
            total = totalValues;
            for (j=0 ; j<getChild().size() ; j++) {
                inserted = getChild(j).insertOrderedLeaves(v,total,limit);
                total += inserted;
            }
        }
        
        return (inserted);
    }
    
    
    /**
     * Bounds the tree by substituting nodes whose children are
     * leaves storing a value beneath a given threshold, by the
     * average of the values in their children.
     * @param limit the threshold for pruning.
     * @param oldSize size of this tree if it were complete.
     * @param numberDeleted an array with a single value storing
     * the number of deleted leaves.
     * @return <code>true</code> if the tree has been reduced to
     * a probability node; <code>false</code> otherwise.
     */
    
    public boolean pruneLowValues(double limit, long oldSize,
    long numberDeleted[]) {
        
        long newSize;
        int i, numberChildren;
        ProbabilityTree ch;
        double pr, sum = 0.0, entropy = 0.0, info;
        boolean bounded = true, // indicates whether the tree can be pruned
        childBounded;
        
        numberChildren = var.getNumStates();
        
        newSize = oldSize / numberChildren;
        
        for (i=0 ; i<numberChildren ; i++) {
            ch = getChild(i);
            
            if (ch.label == PROBAB_NODE) {
                if (ch.getProb() > limit) {
                    bounded = false;
                }
                
            }
            else {
                long chOldLeaves = ch.leaves;
                childBounded = ch.pruneLowValues(limit,newSize,numberDeleted);
                leaves -= (chOldLeaves - ch.leaves);
                if (!childBounded)
                    bounded = false;
            }
        }
        
        if (bounded) {
            pr = average();
            numberDeleted[0] += numberChildren-1;
            assignProb(pr);
            child = new Vector();
        }
        
        return bounded;
    }
    
    
    
    /**
     * Bounds the tree by substituting nodes whose children are
     * leaves by the average of them. This is done for nodes
     * with an information value lower than a given threshold.
     * @param limit the information threshold for pruning.
     * @param oldSize size of this tree if it were complete.
     * @param globalSum the addition of the original potential.
     * @param numberDeleted an array with a single value storing
     * the number of deleted leaves.
     * @return <code>true</code> if the tree has been reduced to a probability
     * node; <code>false</code> otherwise.
     */
    
    public boolean prune(double limit, long oldSize, double globalSum,
    long numberDeleted[]) {
        
        long newSize;
        int i, numberChildren;
        ProbabilityTree ch;
        double pr, sum = 0.0, entropy = 0.0, info;
        boolean bounded = true, // tell if the tree can be reduced to a probab. node
        childBounded;
        
        numberChildren = var.getNumStates();
        
        newSize = oldSize / numberChildren;
        
        for (i=0 ; i<numberChildren ; i++) {
            ch = getChild(i);
            
            if (ch.label == PROBAB_NODE) {
                pr = ch.value;
                sum += pr;
                entropy += (-pr*newSize * Math.log(pr*newSize));
            }
            else {
                long chOldLeaves=ch.leaves;
                childBounded = ch.prune(limit,newSize,globalSum,numberDeleted);
                leaves-=(chOldLeaves-ch.leaves);
                if (!childBounded)
                    bounded = false;
                
                if (bounded) {
                    ch = getChild(i);
                    pr = ch.value;
                    sum += pr;
                    entropy += (-pr*newSize * Math.log(pr*newSize));
                }
            }
        }

        if (bounded) {
            if (sum <= 0.0)
                info = 0.0;
            else
               info = ((newSize * sum)) *
                (Math.log(numberChildren) - Math.log(newSize*sum)) - entropy;

            info=info/globalSum;
            if (info <= limit) {
                pr = average();
                numberDeleted[0] += numberChildren-1;
                assignProb(pr);
                child = new Vector();
            }
            else
                bounded = false;
        }
        return bounded;
    }
    
    
    /**
     * Bounds the tree by substituting nodes whose children are
     * leaves by the average of them. This is done for nodes
     * with an information value lower than a given threshold.
     * @param kindOfApproximation the kind of approximation used when
     * substituing several leaves by a value.
     * @param limit the information threshold for pruning.
     * @param limitSum the limit sum for pruning.
     * @param oldSize size of this tree if it were complete.
     * @param globalSum the addition of the original potential.
     * @param numberDeleted an array with a single value storing
     * the number of deleted leaves.
     * @return <code>true</code> if the tree has been reduced to a probability
     * node; <code>false</code> otherwise.
     */
    
    public boolean prune(int kindOfApproximation,double limit, double limitSum, long oldSize,
    double globalSum[], long numberDeleted[]) {
        
        long newSize;
        int i, numberChildren;
        ProbabilityTree ch;
        double pr=0.0, sum = 0.0, entropy = 0.0, info;
        boolean bounded = true, // tell if the tree can be reduced to a probab. node
        childBounded;
        
        numberChildren = var.getNumStates();
        
        newSize = oldSize / numberChildren;
        
        for (i=0 ; i<numberChildren ; i++) {
            ch = getChild(i);
            
            if (ch.label == PROBAB_NODE) {
                pr = ch.value;
                sum += pr;
                entropy += (-pr * Math.log(pr));
            }
            else {
                long chOldLeaves=ch.leaves;
                childBounded = ch.prune(kindOfApproximation,limit,limitSum,newSize,globalSum,numberDeleted);
                leaves-=(chOldLeaves-ch.leaves);
                if (!childBounded)
                    bounded = false;
                
                if (bounded) {
                    ch = getChild(i);
                    pr = ch.value;
                    sum += pr;
                    entropy += (-pr * Math.log(pr));
                }
            }
        }
        
        if (bounded) {
            if (sum <= 0.0)
                info = 0.0;
            else
                info = ((newSize * sum) / globalSum[0]) *
                (Math.log(numberChildren) - Math.log(sum) - entropy / sum);
            //System.out.println("oldSize="+oldSize);
            //System.out.println("newSize="+newSize);
            //System.out.println("sum="+sum);
            //System.out.println("limitSum="+limitSum);
            //System.out.println("Globalsum antes de aproximar: "+globalSum[0]);
            
            //if ((info <= limit) || ((newSize * sum) <= (limitSum * globalSum[0]))) {
            if(info <= limit) {
                pr = average();
                //      print(10);
                //      System.out.println("Aproximando con "+pr);
                numberDeleted[0] += numberChildren-1;
                assignProb(pr);
                child = new Vector();
            }
            else if((newSize * sum) <= (limitSum * globalSum[0])) {
                if(kindOfApproximation==AVERAGE_APPROX){
                    pr = average();
                }
                else if(kindOfApproximation==ZERO_APPROX){
                    pr=0.0;
                    globalSum[0]-=sum;
                }
                else{
                    System.out.print("Error in ProbabilityTree.prune(int,double,double,long,double[],long[]): ilegal value for kindOfApproximation="+kindOfApproximation);
                    System.exit(1);
                }
                //      print(10);
                //      System.out.println("Aproximando con "+pr);
                numberDeleted[0] += numberChildren-1;
                assignProb(pr);
                child = new Vector();
                
                //System.out.println("PODO");
            }
            else
                bounded = false;
        }
        //System.out.println("Globalsum despues de aproximar: "+globalSum[0]);
        return bounded;
    }
        /**
     * Bounds the tree by substituting nodes whose children are
     * leaves nearest than a certain percentage on the global
     * range of the utility funcion
     * @param limit the threshold for pruning.
     * @param max the global maximum of the tree
     * @param mim the global minimum of the tree
     * @param numberDeleted an array with a single value storing
     * the number of deleted leaves.
     * @return <code>true</code> if the tree has been reduced to
     * a probability node; <code>false</code> otherwise.
     */
    
    public boolean pruneUtility_Euclidean(double limit,double minG, double maxG, long[] numberDeleted) {
        int i, numberChildren;
        ProbabilityTree ch;
        double finalLimit;
        boolean bounded = true; // indicates whether the tree can be pruned
        double d1=0, d2=0;
        bounded=true;
        
        //A leaf cannot be prunned
        if(this.isProbab())
            return false;
        
        //Computes the limit to prune
        finalLimit=limit*(maxG-minG);
        
        // Get the number of childs of this node        
        numberChildren = var.getNumStates();
        
        //Expands the tree once and computes the distance
        Vector vleaves = new Vector();
        Vector vmean = new Vector();
        for(i=0; i < numberChildren; i++){
            ch=getChild(i);
            
            Vector chLeaves = ch.getLeaves();
            vleaves.addAll(chLeaves);
            vmean.addAll(VectorManipulator.vectorMean(chLeaves));
        }

        try {
            d2 = Distances.euclidean(vmean, vleaves);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
        //Computes the distance without expanding the tree
        vleaves = getLeaves();
        vmean = VectorManipulator.vectorMean(vleaves);
        try {
            d1 = Distances.euclidean(vmean, vleaves);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
        //Computes the gain of information
        double info = d1 -d2;  
            
            
            

          if(info<=finalLimit) {   //The tree is prunned        
    
                numberDeleted[0] += numberChildren-1;
                assignProb((Double)vmean.get(0));
                child = new Vector();
                bounded = true;
            }
            else{   //The tree is not prunned
                for(i=0; i < numberChildren; i++){
                    ch=getChild(i);
                    ch.pruneUtility_Euclidean(limit, minG, maxG, numberDeleted);
                }
                bounded = false;
              
            }
        
        
         updateSize();
        // Return the value of bounded        
        return bounded;
    }
    
    
    /**
     * Bounds the tree by substituting nodes whose children are
     * leaves nearest than a certain percentage on the global
     * range of the utility funcion
     * @param limit the threshold for pruning.
     * @param max the global maximum of the tree
     * @param mim the global minimum of the tree
     * @param numberDeleted an array with a single value storing
     * the number of deleted leaves.
     * @return <code>true</code> if the tree has been reduced to
     * a probability node; <code>false</code> otherwise.
     */
    
    public boolean pruneUtility_MaxMin(double limit,double minG, double maxG,long numberDeleted[]) {
        int i, numberChildren;
        ProbabilityTree ch;
        double max=0,min=0;
        double pr,finalLimit;
        boolean bounded = true, // indicates whether the tree can be pruned
        childBounded;
        
        // The final limit will be established as a funtion
        // of maxG and minG, the global maximum and minimum of the
        // tree
        
        finalLimit=limit*(maxG-minG);
     
        // Get the number of childs of this node
        
        numberChildren = var.getNumStates();
        
        // If every child of this node is a leaf, then compute the max
        // and min values, and check if the difference between this values
        // is less than the limit
        
        bounded=true;
        for(i=0; i < numberChildren; i++){
            ch=getChild(i);
            
            if (ch.label != PROBAB_NODE){
                // Not a leaf, and must try a prune on this
                // subtree
                
                long chOldLeaves = ch.leaves;
                childBounded = ch.pruneUtility_MaxMin(limit,minG,maxG,numberDeleted);
                leaves -= (chOldLeaves - ch.leaves);
                
                if(childBounded==false){
                    bounded=false;
                }
                else{
                }
            }
            
            // If the subtree was pruned, now will be a probab node
            // and will consider it for maximum and minimum computation
            
            if (ch.label == PROBAB_NODE){
                
                // Actualize minum and maximum
                
                pr=ch.getProb();
                
                // Initializa min and max
                
                if (i==0){
                    min=pr;
                    max=pr;
                }
                
                if (pr > max){
                    max=pr;
                }
                else{
                    if(pr < min)
                        min=pr;
                }
            }
        }
        
        // After the loop over the childs, if bounded == true
        // this means all branches could be considerd for a
        // possible operation on them
        
        if(bounded == true){
      
            if ((max-min) <= finalLimit){
                // Prune on the branches
          
                pr=average();            
                numberDeleted[0] += numberChildren-1;
                assignProb(pr);
                child = new Vector();
            }
            else{
                // The prune is not applicable
                
                bounded=false;
            }
        }
        
        // Return the value of bounded
        
        return bounded;
    }
    
    /**
     * Gets the size (number of values) of the tree starting in this node.
     * @return the number of leaves beneath this tree node.
     */
    
    public long getSize() {
        
        return leaves;
    }

    /**
     * Method for getting the number of nodes of the tree
     * @return number of nodes
     */
    public long getNumberOfNodes(){
      ProbabilityTree tree;
      long sum=1;

      if (child != null){
         for(int i=0; i < child.size(); i++){
            tree=getChild(i);
            sum=sum+tree.getNumberOfNodes();
         } 
      }

      // Return sum
      return sum;
    }

    /**
     * Method for getting the number of nodes of the tree
     * @return number of nodes
     */
    public long getNumberOfLeaves(){
      ProbabilityTree tree;
      long sum=0;

      if (var != null){
         for(int i=0; i < child.size(); i++){
            tree=getChild(i);
            sum=sum+tree.getNumberOfLeaves();
         }
      }
      else{
          sum=1;
      }

      // Return sum
      return sum;
    }
    
    /**
     * Updates the counter of the number of leaves beneath this node and in
     * each of its descendants.
     */
    
    public void updateSize() {
        
        ProbabilityTree tree;
        int i, nv;
        
        if (label == PROBAB_NODE)
            leaves = 1;
        else {
            leaves = 0;
            nv = var.getNumStates();
            
            for (i=0 ; i<nv ; i++) {
                tree = getChild(i);
                tree.updateSize();
                leaves += tree.getSize();
            }
        }
    }
    
    
    /**
     * Checks if the number of leaves in this <code>ProbabilityTree</code>
     * and in each of its subtrees is consistent.
     * @return <code>true</code> if <code>leaves</code> contains the right
     * number of leaf nodes of this tree (every subtree must verify this
     * condition);  <code>false</code> otherwise.
     */
    
    public boolean checkSize() {
        
        ProbabilityTree tree;
        int i, nv;
        
        if (label == PROBAB_NODE) {
            if (leaves == 1)
                return true;
            else
                return false;
        }
        else {
            int nleaves=0;
            nv = var.getNumStates();
            
            for (i=0 ; i<nv ; i++) {
                tree = getChild(i);
                if (tree.checkSize() == false) {
                    return false;
                }
                nleaves += tree.getSize();
            }
            if (nleaves == getSize()) {
                return true;
            }
            else {
                return false;
            }
        }
    }
    
    
    /**
     * Determines whether a variable is in the tree starting in this node
     * or not.
     * @param variable a <code>FiniteStates</code> variable.
     * @return <code>true</code> if variable is in some node in the tree,
     *         and <code>false</code> otherwise.
     */
    
    public boolean isIn(FiniteStates variable) {
        
        boolean found = false;
        int i;
        
        if (label != FULL_NODE)
            found = false;
        else {
            if (var == variable)
                found = true;
            else {
                for (i=0 ; i<child.size() ; i++) {
                    if (getChild(i).isIn(variable)) {
                        found = true;
                        break;
                    }
                }
            }
        }
        
        return found;
    }
    
    
    /**
     * Saves the tree starting in this node to a file.
     * @param p the <code>PrintWriter</code> where the tree will be written.
     * @param j a tab factor (number of blank spaces before a child
     * is written).
     */
    
    public void save(PrintWriter p, int j) {
        
        int i, l, k;
        
        if (label == PROBAB_NODE)
            p.print(value+";\n");
        else {
            p.print("case "+var.getName()+" {\n");
            
            for (i=0 ; i< child.size() ; i++) {
                for (l=1 ; l<=j ; l++)
                    p.print(" ");
                
                p.print(var.getPrintableState(i) + " = ");
                getChild(i).save(p,j+10);
            }
            
            for (i=1 ; i<=j ; i++)
                p.print(" ");
            
            p.print("          } \n");
        }
    }
    
    
    /**
     * Prints a tree to the standard output.
     * @param j a tab factor (number of blank spaces before a child
     * is written).
     */
    
    public void print(int j) {
        
        int i, l, k;
        
        
        if (label == PROBAB_NODE)
            System.out.print(value+";\n");
        else {
            System.out.print("case "+var.getName()+"(leaves="+ leaves +")"+" {\n");
            
            for(i=0 ; i< child.size() ; i++) {
                for (l=1 ; l<=j ; l++)
                    System.out.print(" ");
                
                System.out.print( var.getPrintableState(i) + " = ");
                getChild(i).print(j+10);
            }
            
            for (i=1 ; i<=j ; i++)
                System.out.print(" ");
            
            System.out.print("          } \n");
        }
    }
    
    
    /**
     * Normalizes the tree starting in this node to sum up to 1.
     * The object is modified.
     * @param totalSize size of the completely expanded tree.
     */
    
    public void normalize(long totalSize) {
        
        double total;
        int i, nv;
        
        total = sum(totalSize);
        
        if (label == PROBAB_NODE){
            if(total>0.0){
                value /= total;
                if(Double.isNaN(value) || Double.isInfinite(value))
                    value=0.0;
            }
            else
                value = 0.0;
        }
        else {
            nv = var.getNumStates();
            for (i=0 ; i<nv ; i++)
                getChild(i).normalizeAux(total);
        }
    }
    
    
    /**
     * Auxiliar to the previous one. It avoids to unnecessary compute again
     * the addition of the values in the leaves. The object is modified.
     * @param total the addition of the values in the leaves of the tree
     * being normalized.
     */
    
    public void normalizeAux(double total) {
        
        int i, nv;
        
        if (label == PROBAB_NODE){
            if(total>0.0){
                value /= total;
                if(Double.isNaN(value) || Double.isInfinite(value))
                    value=0.0;
            }
            else
                value = 0.0;
        }
        else {
            nv = var.getNumStates();
            for (i=0 ; i<nv ; i++)
                getChild(i).normalizeAux(total);
        }
    }
    
    
    /**
     * Replaces a <code>ProbabilityTree</code> as child of this node.
     * It is inserted at the specified position in the vector of
     * children. The previous child at that position is discarded.
     * <b>The object is modified</b>.
     * @param tree <code>ProbabilityTree</code> to be inserted as child.
     * @param pos an <code>int</code> with the position of the new child.
     */
    
    public void setNewChild(ProbabilityTree tree, int pos) {
        
        child.setElementAt(tree,pos);
    }
    
    
    /**
     * Sets to one all the leaves in the tree except those
     * beneath <code>conf</code>.
     * WARNING: The tree is modified.
     *
     * @param conf the configuration that determines the part of the tree that
     * will remain unchanged.
     */
    
    public void setToOne(Configuration conf) {
        int i, v;
        
        if (this.getLabel() == PROBAB_NODE) {
            this.assignProb(1.0);
        }
        else {
            if (this.getLabel() == FULL_NODE) {
                v = conf.getValue(this.getVar());
                if (v == -1) {
                    return; // This is the end, because we have reached a variable not in the
                    // path.
                }
                for (i=0; i < this.getChild().size() ; i++) {
                    if (i != v) {
                        this.getChild(i).assignProb(1.0);
                    }
                    else {
                        this.setToOne(conf);
                    }
                }
            }
        }
    }
    
    
    /**
     * Sets to one all the leaves in the tree except those
     * beneath the configurations in <code>listOfConf</code>.
     * WARNING: The tree is modified.
     *
     * @param listOfConf the configurations that determine the part of the tree that
     * will remain unchanged.
     */
    
    public void setToOne(Vector listOfConf) {
        int i, j, v, acum;
        Configuration conf;
        
        if (this.getLabel() == PROBAB_NODE) {
            this.assignProb(1.0);
        }
        else {
            if (this.getLabel() == FULL_NODE) {
                acum = 0;
                for (j=0 ; j<listOfConf.size() ; j++) {
                    conf = (Configuration)listOfConf.elementAt(j);
                    v = conf.getValue(this.getVar());
                    acum += v;
                    if (v != -1) {
                        for (i=0; i < this.getChild().size() ; i++) {
                            if (i != v) {
                                this.getChild(i).assignProb(1.0);
                            }
                            else {
                                this.setToOne(listOfConf);
                            }
                        } // end for
                    } // end if
                } // end for
                if (acum < 0)
                    return;
            }
        }
    }
    
    /**
     * Replaces a <code>ProbabilityTree</code> as child of this node.
     * It is inserted at the specified position in the vector of
     * children, and returns the child previously stored at
     * that position. <b>The object is modified</b>.
     * @param tree <code>ProbabilityTree</code> to be inserted as child.
     * @param pos <code>int</code> the position of the new child.
     * @return a <code>ProbabilityTree</code> : the previous child stored.
     */
    
    public ProbabilityTree replaceChild(ProbabilityTree tree, int pos) {
        
        return ((ProbabilityTree) child.set(pos,tree));
    }
    
    
    /**
     * Seeks for the first occurrence of a given variable in
     * this probability tree and returns the path to that variable
     * in reverse order (from the variable to the root).
     *
     * @param variable the variable to locate.
     * @param confPath the configuration where the path will be stored.
     * This configuration must have been previously initialised.
     * @return <code>true</code> if the variable is found, and
     * <code>false</code> otherwise.
     */
    
    public boolean getPathToVariable(FiniteStates variable,
    Configuration confPath) {
        
        boolean found = false;
        int i;
        
        if (label != FULL_NODE)
            found = false;
        else {
            if (var == variable)
                found = true;
            else {
                for (i=0 ; i<child.size() ; i++) {
                    if (getChild(i).getPathToVariable(variable,confPath)) {
                        confPath.insert(getVar(),i);
                        found = true;
                        break;
                    }
                }
            }
        }
        return found;
    }
    
    /**
     * Divides the probability of a given configuration of variables by
     * one value. <b>The object is modified</b>.
     * @param conf a <code>Configuration</code>.
     * @param divVal a <code>double</code>.
     * @return the new probability value of the tree following
     * the path indicated by <code>Configuration conf</code>,
     * or value -1 in case of error.
     */
    
    public double divideProb(Configuration conf, double divVal ) {
        
        int p, val;
        ProbabilityTree tree;
        
        if (label == FULL_NODE) { // If the node is a variable
            val = conf.getValue(var);
            tree = (ProbabilityTree)child.elementAt(val);
            return (tree.divideProb(conf,divVal));
        }
        else {
            if (label == PROBAB_NODE){ // If the node is a prob.
                if(divVal!=0.0){
                    value = value/divVal;
                    return value;
                }
            }
            return (-1.0);
        }
    }
    
    /**
     * Divides the leaves of the tree starting in this node by a value.
     * The object is modified.
     * @param divVal:  the value, distinct from zero.
     * @return false in case of error.
     */
    
    public boolean divideLeaves(double divVal) {
        
        int i, nv;
        
        if(divVal==0.0)
            return false;
        
        if (label == PROBAB_NODE){
            value /= divVal;
            if(Double.isNaN(value) || Double.isInfinite(value))
                value=0.0;
        }
        else {
            nv = var.getNumStates();
            for (i=0 ; i<nv ; i++)
                getChild(i).divideLeaves(divVal);
            
        }
        return true;
    }
    
    
      
    /**
     * This procedure implements an special case of division
     * which is more efficient than the general one, but it
     * can be only applied when we have a tree and an special
     * variable, such that the tree is always ramified by this
     * variable just before the leaves and we are dividing by
     * a pontetial table that depends only of this variable.
     * It transforms the calling tree.
     * @param v:  the special variable.
     * @param t: the potencial table depending of u.
     * @return false in case of error.
     */
    
    public boolean divide(FiniteStates u, PotentialTable t) {
        
        int i, nv;
        double x;
            
           if (label != FULL_NODE)
           {return(false);}
           else {
            nv = var.getNumStates();
            if (u == var) {
             for (i=0 ; i<nv ; i++){
                 if (t.getValue(i) == 0.0) {return(false);}
                 x = getChild(i).getProb()/t.getValue(i);
                 getChild(i).assignProb(x);
             }
            }
           
            else {
            for (i=0 ; i<nv ; i++)
                getChild(i).divide(u,t);
            }
    
    }
        return(true);
    }
      
    /**
     * Adds x to the leaves of the tree starting in this node by a value.
     * The object is modified.
     * @param x:  the value to be added.
     *X
     */
    
    public void sum(double x) {
        
        int i, nv;
    
        
        if (label == PROBAB_NODE){
            value += x;
            
        }
        else {
            nv = var.getNumStates();
            for (i=0 ; i<nv ; i++)
                getChild(i).sum(x);
            
        }
     
    }
      
    
/**
 * Seeks for the occurrence of a given variable in
 * the probability tree and returns the path to that variable
 * (from the root to the variable);
 * At the same time the original tree (the object) is factorised.
 * The object is modified.
 * @param variable the variable to locate.
 * @param confPT :configuration with all the variables of the potential
 * @param confPath the configuration where the path is being stored.
 * This configuration must previously be initialised.
 * @param factParam factorisation parameters.
 * @param listRoots vector with the factor-trees and the original tree.
 * @return <code>true</code>  if the variable is found,
 *        <code>false</code>  otherwise
 * 
 */

public boolean findVarAndFactorise(Configuration confPT, Configuration confPath,
                                  FactorisationTools factParam,Vector lisRoots) {
  FiniteStates varFac; 
  boolean found = false;  
  int i,numFac;
  
  varFac=confPath.getVariable(0);
  
  if (label != FULL_NODE)
    found = false;
  else {
    if (var == varFac)
      found = true;
    else {
        
      factParam.incActualLevel(); //Increments the actual level of the tree
      if(factParam.isMaxLevel())  //Check if the maximun has been reached
           return(false);
        
      confPath.insert(var,0); // push the actual variable
            
      for (i=0 ; i<child.size() ; i++) {
        confPath.putValue(var,i);  
        if( getChild(i).findVarAndFactorise(confPT,confPath,factParam,lisRoots)) {
            
            numFac = getUniqueFactor(confPT,confPath,factParam,lisRoots,i);         
        }//if the variable is found
      }//for
      
      confPath.remove( confPath.size()-1); //pop the actual variable
      factParam.decActualLevel(); //Decrements the actual level of the tree
    }
  }
  return found;
}


/**
 * Seeks for the occurrence of a given variable in
 * the probability tree and returns the path to that variable
 * (from the root to the variable);
 * At the same time the splitting trees are constructed.
 * The object is not modified.
 *
 * @param confPath : the configuration where the path is being stored.
 * This configuration must previously be initialised.
 * @param confFound : vector of Configuration with the paths to the variable
 * @param onlySplit : a boolean value to select only split or split-and-factorise
 * @param tree2 : the core tree (which contains the variable)
 *
 * @return <code>ProbabilityTree</code> the  nodes of the free tree 
 * without occurrences of the variable.
 * 
 */

public ProbabilityTree findVarAndSplit(Configuration confPath,
                           Vector confFound, boolean onlySp ,
                           ProbabilityTree tree2) {
 
  FiniteStates varFac;
  ProbabilityTree auxTree,treeSp, treeH;  
  Vector auxChildFree = new Vector();
  Vector auxChildCore= new Vector();
  int nv;   
  int i,numleavCore=0, numleavFree=0;
  
  varFac= confPath.getVariable(0);
  
  if(label == PROBAB_NODE){
    treeSp= new ProbabilityTree( getProb());
    tree2.assignProb(1.0);
  }  
  
  else{ // label == FULL_NODE
      tree2.var = var;
      tree2.label = FULL_NODE;
      tree2.leaves= 0;
      
      nv= var.getNumStates();
      
      if (var == varFac){
          
        treeSp= new ProbabilityTree(1.0);
        
        for (i=0 ; i<nv ; i++) // Copy the subtrees (of the node with the variable)
            tree2.child.addElement(getChild(i).copy());
        
        tree2.leaves= leaves;
        if(!onlySp)// save the actual configuration for factorisation 
               confFound.addElement(confPath.duplicate());
        confPath.putValue(varFac,1);
      }  
      else{
          
        treeSp = new ProbabilityTree(var);  
                
        confPath.insert(var,0); // push the actual variable    
        
        for (i=0 ; i<nv ; i++) {
          confPath.putValue(var,i);    
         
          treeH = new ProbabilityTree(); 
          auxTree= getChild(i).findVarAndSplit(confPath,confFound,onlySp,treeH);
          
          if(auxTree.isUnitNode()) // A leaf node found                
                  numleavFree++;          
         
          auxChildFree.addElement(auxTree);         
          
          if(treeH.isUnitNode()) // A leaf node found                
                  numleavCore++;          
          
          auxChildCore.addElement(treeH);
          
          treeSp.leaves += auxTree.leaves; //treeSp.replaceChild(auxTree,i);
          tree2.leaves += treeH.leaves;   
   
        }//for
        
        if( numleavCore == nv ){
            tree2.assignProb(1.0); 
            tree2.child = new Vector();
        }
        else 
            tree2.child=auxChildCore;
        
        if( numleavFree == nv )
            treeSp= new ProbabilityTree(1.0); 
        else 
            treeSp.child=auxChildFree;                
        
        confPath.remove( confPath.size()-1); //pop the actual variable
                
      }//var!=variable
    }//FULL_NODE

  return treeSp; 
}


/**
 * This method looks for one variable in the probability tree, and at the same
 * time  a copy of the original tree is carried out replacing the ocurrences of
 * that variable by value 1.
 *
 * @param confPath : the configuration where the path is being stored.
 * This configuration must previously be initialised.
 * @param confFound : vector with the Configurations to the variable
 * @param saveConf : a boolean value indicating whether save the configurations
 *
 * @return <code>ProbabilityTree</code> a copy of the original tree 
 * without occurrences of the variable.
 */

public ProbabilityTree findFreeTerm(Configuration confPath,
                           Vector confFound, boolean saveConf) {
 
  FiniteStates varFac;
  ProbabilityTree auxTree,treeSp;  
  Vector auxChildFree = new Vector();
  int nv, i, numleavFree=0;;
  
  varFac= confPath.getVariable(0);
  
  if(label == PROBAB_NODE)
 
      treeSp= new ProbabilityTree( getProb());
   
  else{ // label == FULL_NODE
      
      nv= var.getNumStates();
      
      if (var == varFac){
        treeSp= new ProbabilityTree(1.0);
        
        if(saveConf) // save the actual configuration for the next factorisation 
               confFound.addElement(confPath.duplicate());
        confPath.putValue(varFac,1);
      }  
      else{
        
        treeSp = new ProbabilityTree(var);  
                
        confPath.insert(var,0); // push the actual variable    
        
        for (i=0 ; i<nv ; i++) {
            
          confPath.putValue(var,i);    
         
          auxTree= getChild(i).findFreeTerm(confPath,confFound,saveConf);
          
          if(auxTree.isUnitNode()) // A leaf node found                
                  numleavFree++;          
         
          auxChildFree.addElement(auxTree);         
          
          treeSp.leaves += auxTree.leaves; //treeSp.replaceChild(auxTree,i);
     
        }//for
  
        if( numleavFree == nv ) //if all children have been leaves
            treeSp= new ProbabilityTree(1.0); 
        else 
            treeSp.child=auxChildFree;                
        
        confPath.remove( confPath.size()-1); //pop the actual variable
        
      }//var!=variable
    }//FULL_NODE

  return treeSp; 
}


/**
 * This method looks for one variable in the probability tree, and at the same
 * time a new probability tree is created  copying  the branches containing
 * the variable and setting value 1 for the others.
 *
 * @param confPath : the configuration where the path is being stored.
 * This configuration must previously be initialised.
 *
 * @return <code>ProbabilityTree</code> a copy of the original tree 
 * restricted to the branches with the variable.
 */

public ProbabilityTree findCoreTerm(Configuration confPath) {
 
  FiniteStates varFac;
  ProbabilityTree auxTree,tree;  
  Vector auxChildCore= new Vector();
  int nv, i,numleavCore=0;
  
  varFac= confPath.getVariable(0);
  
  if(label == PROBAB_NODE)
    
      tree= new ProbabilityTree( 1.0);  
  
  else{  // label == FULL_NODE
      
      tree= new ProbabilityTree();
      tree.var = var;
      tree.label = FULL_NODE;
      tree.leaves= 0;
      
      nv= var.getNumStates();
      
      if (var == varFac){

         for (i=0 ; i<nv ; i++) // Copy the subtrees (of the node with the variable)
            tree.child.addElement(getChild(i).copy());
         tree.leaves= leaves;
      }  
      else{
            
        confPath.insert(var,0); // push the actual variable    
        
        for (i=0 ; i<nv ; i++) {
          confPath.putValue(var,i);    
         
          auxTree= getChild(i).findCoreTerm(confPath);
          
          if(auxTree.isUnitNode()) // A leaf node found                             
                  numleavCore++;          
          
          auxChildCore.addElement(auxTree);
 
          tree.leaves += auxTree.leaves;   
   
        }//for
        
        if( numleavCore == nv ){
            tree.assignProb(1.0); 
            tree.child = new Vector();
        }
        else 
            tree.child=auxChildCore;
        
        confPath.remove( confPath.size()-1); //pop the actual variable
        
      }//var!=variable
    }//FULL_NODE

  return tree; 
}

    
/**
 * Compares all the children of the variable and obtain an unique, if exists,
 * proportionality factor.
 * The tree is factorised if a number of children are proportional or almost 
 * proportional, dependig on the values selected in @param factParam.
 *
 * @param conf configuration with all the variables of the potential
 * @param confFixed the configuration containing the path to the variable.
 * @param factParam factorisation parameters.
 * @param vecR  Vector with the main tree (the latest) and the factorised trees.
 * @param indChild index of the child of the actual object, with the variable.
 * @return <code>1</code>  if factorised,
 *        <code>0</code>  otherwise
 * 
 */

public int getUniqueFactor(Configuration conf, Configuration confFixed,
            FactorisationTools factParam, Vector vecR,int indChild) {
                
  ProbabilityTree root,rootV;                           
  FiniteStates varF;
  double vFacAlpha[];  // vectors loading proportional "brothers" and 
  int vPropBros[];     // their factor of proportonality "alphai" */
  
  double vFacBF[];    // Pointers to the best of vPopBros and vFacAlpha
  int vPropBF[];  
  
  Vector vecLeaves; // vector for probability values in the approximate case
  Vector vecfacK;   // vector for partial factors  (alpha_i)
  double alpha[] = new double[2]; // save "alpha" if the trees are alpha-proportional
  long numcases,sizeTable;
  int i,l,k, nvals, minPropB;
  int valueI, valueJ; //index of the first and second child to compare
  int indfactor=-1;
  int bestFactor=-1; // index of the best of all  possible factors
  int bestCount=1;   // number of proportional brothers of the best factor
  int partialCount=1; // number of proportional brothers for each valueI
  int numFac=0;
  boolean allBrothersProp=false; // flag: if all children must be proportional
  boolean brothersP=false; 
  boolean inProportional=true; 
  boolean isLast=false;
  double probI, probJ, fprop;
 
  root = (ProbabilityTree) vecR.lastElement();
  varF = confFixed.getVariable(0); // the variable is in the first position
  nvals = varF.getNumStates();
  rootV = root;
  
  if(varF!=root.getVar()) // varF is not in root node
      rootV= this.getChild(indChild);
  
  for(i=0,l=0; i<nvals;i++)
      if( rootV.getChild(i).isProbab() ) l++;
  if(l== nvals){ //all children are leaves , then not factorise
     /*System.out.println(" Todos hojas ")*/;
     return 0;
  }
  
  sizeTable=(long)FiniteStates.getSize(conf.getVariables(),confFixed.getVariables());
  
  //Minimun number of proportional brothers
  minPropB= (int) Math.round( nvals*factParam.getProporChild());
  
  if(minPropB<=0){ 
      System.out.println("Wrong number of proportional brothers: "+minPropB); 
      return(0);
  }
  if(minPropB==nvals)
  {
      bestFactor=0; // for all brothers proportional, child 0 must be the factor
      allBrothersProp=true;
  }
  
  vFacAlpha = new double[nvals];   // Initial values = -1.0
  vPropBros = new int[nvals];     //  Initial values = -2
  
      // vPropBros[i]== -1  if child i is a factor;
      // vPropBros[i]== k  if child i is proportional with brother in position k;
      // vPropBros[i]== -2 if child i is not proportional
  
  for (i=0 ; i<nvals ; i++) {
      vFacAlpha[i]= -1.0;  vPropBros[i]= -2;
  }
  vPropBF=vPropBros;
  vFacBF=vFacAlpha;
  
  valueI = 0; // index of the first child to compare
  valueJ = 1; // index of the second child to compare
  
  /* Compare the first child (valueI) with the rest of right-children (valueJ), 
     and so on, until one of the children is proportional with the rest of their 
     brothers (ALL), or the index of valueI is such that it would be impossible 
     to reach the minimum of proportional brothers (minPropB), 
     or to reach the number of proportional brothers that have been reached by 
     a previous brother (bestCount).
     Check as many "valueI" as possible, and returns the best of them.*/
  
  while (inProportional ) {
      
    indfactor=-1;
    
    // Compares child in position "valueI" with child in position "valueJ"
              
    conf.resetConfiguration(confFixed);
    numcases=1; //sumF=0.0; // sum of all the leaves of the factor tree

    vecfacK= new Vector();  // sizeTable
    vecLeaves= new Vector();// 2*sizeTable
    isLast = false;
    
    for(k=0; k<2; alpha[k++]=-1.0); // Init with "empty values"
       
    do{  //while(brothersP)   comparing two brothers
        
      conf.putValue(varF,valueI);
      probI= root.getProb(conf); 
      
      conf.putValue(varF,valueJ);
      probJ= root.getProb(conf);

       // In exact case returns  -1 if both values are not proportional, 0 if they are.
       // In approximate case always returns 0 until all the leaves have been compared.
      indfactor=isPropor(probI,probJ,vecLeaves,vecfacK,isLast,factParam,alpha,indfactor);
        
      if( indfactor!=-1 ){ // They may be proportional
         brothersP=true; 
                
         if(numcases<sizeTable){
            conf.nextConfiguration(confFixed);// prove the next configuration
            numcases++;
            if(numcases==sizeTable) 
              isLast=true;
         }
         else{ //  all of possible configurations of the variables proved, then:
              // child "valueI" is a factor, and 
              // child "valueJ" is alpha-proportional with child "valueI" 
            
            fprop= alpha[0]; //  alpha
            vFacAlpha[valueI]= 1.0;   // flag for factor child                           
            vFacAlpha[valueJ]= fprop; // proportionality factor           
            vPropBros[valueJ]= valueI; // valueJ is alpha-proportional with valueI 
            vPropBros[valueI]= -1;     // flag for factor child in this vector
            partialCount++;            // increment counter of proportional brothers
            brothersP=false;
         }  
      }       
      else{ // (indfactor = -1) not proportional
        brothersP=false; 
        if(allBrothersProp) 
            inProportional=false;  // finish the comparisons 
      }   
    }while(brothersP);
                
   // If possible, get the next brother to compare with;
   // In other case get another "first child" valueI and the next, valueJ 
    
    if(inProportional){
       valueJ++;
       if( valueJ==nvals || (nvals-valueJ) < (minPropB-partialCount) ) {
       
            // need to change "valueI"  
        
          if(partialCount == nvals){ // ALL children have been proportional
             inProportional=false; // finish the comparations
             bestFactor=valueI;
             bestCount=nvals;
             vPropBF=vPropBros;
             vFacBF=vFacAlpha;
          }    
          else{ // save the best factor, by now, and try with the "next valueI"
             if (partialCount>=minPropB && partialCount>bestCount){ 
                bestFactor=valueI;
                bestCount=partialCount;
                vPropBF=vPropBros;   // save the actual vectors
                vFacBF=vFacAlpha;
                vFacAlpha = new double[nvals];   // Memory for two new vectors
                vPropBros = new int[nvals];
             }
          
             // next "valueI"
             valueI++;
             partialCount=1;
             valueJ= valueI+1;
             if( valueI==nvals-1 || (nvals-valueI)< minPropB 
                                 || (nvals-valueI)< bestCount ) {
                inProportional=false;
             }
             else
               for (i=0 ; i<nvals ; i++) {
                  vFacAlpha[i]= -1.0;  vPropBros[i]= -2;
               }
          }//end of else (not all proportional)
       
       }//end of checking the new value for "valueI"
    }//if        
  } // while inProportional   
  
     // if thereï¿½s at lease one factor then factorise
  if(bestFactor<0 || bestCount<minPropB) 
      return(0);
  
  //System.out.println("...arbol a factorizar, respecto a "+ varF.getName() ); //="+bestFactor);
  //this.print(10);
  //((ProbabilityTree) vecR.lastElement()).print(10);
  
  numFac = factorisePT(vFacBF,vPropBF,confFixed,vecR);
    
  if(numFac>0){ //if factorised, save the maximun of the proportionality factors
      double maxAlpha=vFacBF[0];
      for (i=1 ; i<nvals ; i++) 
             if( i!=bestFactor && vFacBF[i]>maxAlpha ) 
                 maxAlpha = vFacBF[i];
      
      factParam.vecDistApproxim.addElement(new Double(maxAlpha));
  }
    
  return (numFac);  
}

/**
 * Factorise the probability tree; creates the factor tree (without the variable)
 * and modifies the object tree with the proportionality factor values.
 * @param vfac: vector containing the proportionality factors
 * @param vPro: vector containing the indexes of the proportional brothers.
 * @param confF: configuration to the variable.
 * @param vR: vector loading the new trees. It's initialized with the object tree.
 */

int factorisePT(double vfac[], int vPro[], Configuration confF, Vector vR){
  
  ProbabilityTree root,facTree,mtree,ftree,prev;
  FiniteStates varF, var;
  int i,np, nvals, value=0, indfactor=-1,index,childvalue=0;
  int prop;
  double facProp,prob;
  boolean inRoot= false;
  
  root= (ProbabilityTree) vR.lastElement();
  mtree = root;

  facTree = new ProbabilityTree();
  ftree = facTree;
  prev= facTree;
           
  varF= confF.getVariable(0);
  
    // check the vectors and get the index of the factor-child 
  nvals= varF.getNumStates(); 
  for(i=0,np=0, prop=0; i< nvals; i++){
        if(vPro[i]==-2) np++;
        else
            if(vPro[i]==-1) indfactor=i;  // it's the factor-child
            else prop++;
  }
  if(np+prop+1 !=nvals || indfactor<0){
      System.out.println("factorisePT>error: bad vector of proportionality");
      return(0);
  }
   
  if(varF !=root.getVar() ){ // the variable is not in root node
         
      do{  // building the factor-tree
            var= mtree.getVar();
            index = confF.indexOf(var);
         
            if (index > -1){ // if var is in conf
                value = confF.getValue(index);
           
                if(var!=varF){             
                    ftree.assignVar(var);             
                    for(i=0;i<var.getNumStates(); i++)
                        if( i!= value)  (ftree.getChild(i)).assignProb(1.0);
           
                    prev=ftree;
                    ftree= ftree.getChild(value);
                    childvalue=value;
                    mtree = mtree.getChild(value);
                }
            }
            else{ 
                System.out.println("factorisePT>error: variable not found");return(0);}
        
      }while( var!=varF);  
  } 
  else // varF in root node
     inRoot=true;
    
    // Update the original tree: the factor is replaced by an unit tree, an the other children
    // are replaced by their "alpha_i" (if proportional with the factor) or by the tree
    // resulting of divide the child by the factor-tree
    
  for(i=0;i<varF.getNumStates(); i++){ 
          
      facProp= vfac[i];
        
      if(i==indfactor){   // This child is the factor, then is replaced by an unit tree
        ftree= mtree.replaceChild(new ProbabilityTree(1.0),i); 
         //ftree= mtree.replaceChild(new ProbabilityTree(sumF),i);           
        if(inRoot) 
          facTree=ftree;
        else{
           prev.setNewChild(ftree,childvalue);          
           facTree.updateSize(); // Update the number of leaves in the factor-tree
        }
       facTree.updateSize(); 
      }  
      else // the rest of the children are replaced by "facProp" or divided by the factor-tree
         if(facProp!=-1.0) // alpha can be zero
             mtree.setNewChild(new ProbabilityTree(facProp),i); 
         else{
             ProbabilityTree original=(ProbabilityTree)mtree.getChild(i);
             mtree.setNewChild( divide(original,facTree),i);   
         }
  } 
     
  i=vR.size();
  vR.insertElementAt(facTree,i-1); //the root is shifted upward 
  
   /*System.out.println(".......despues de factorizar ORIGINAL");
    ((ProbabilityTree) vR.lastElement()).print(10);  
    System.out.println(".......despues de factorizar FACTOR");
    facTree.print(10);*/
  return(1);   
}

    
/** 
 * Method for call at approximate factorisation or exact factorisation
 * dependig on the value of the input errors, and compare the leaves values.  
 * @return  <code>1</code> if theyï¿½re not proportional or one of the values is null, or
 *          <code>0</code>  if they are proportional.
 * In approximate factorisation returns 0 until all the leaves have been compared.
 */
 
private int isPropor(double prI,double prJ, Vector vProbs,Vector vfK,
                     boolean isLast,FactorisationTools factParam,
                     double alphai[],int index){
  
  double res,fprop;
  int indexFac;
 
  if( factParam.getFactorisationErrors(0)==0.0 || factParam.iscompilPhase()) // Exact case
    indexFac= isPropExact( prI,prJ,alphai,index,isLast);
  
  else // Approximate case
    indexFac= isPropApprox( prI,prJ,vProbs,vfK,isLast,factParam,alphai);  
  
  return (indexFac); // returns 0 ,1  and -1 if they're not proportional
}
 
/** 
 * Compares two leaf values and returns 0 if the possible factor K is calculated,
 * or -1 if theyï¿½re not proportional with the two previous values
 * compared.
 * If one of them is zero returns -1. 
 * If both are zeros continue with the next comparison (and returns 0).
 */

private int isPropExact(double prI,double prJ,double alphai[],int indf,boolean isLast){

  int iFac;
  double res,fprop;

  
  if( prI==0.0 && prJ==0.0 ){
      
     if(isLast && alphai[0]==-1.0){ // all leaves have been  zeros
         //System.out.println("all leaves zeros");
          return(-1);
     }
     return(0);  
  }
  else 
      //if( prI==0.0 ) //not proportional
      if( prI==0.0 || prJ==0.0) //not proportional
        return(-1); 
 
  
  res= prJ/prI;
  iFac=0;
  
  if(indf<0){ // It's the first compararison: calculate the first "alphai"
    alphai[iFac]=res;
  }
  else{ 
    fprop=alphai[iFac];
    
    if(res!=fprop) // not proportional, different values for "alphai" 
        return (-1);
  } 
  return (iFac); // returns 0, and -1 if not proportional
 
}


/** 
 * Compares all the leaves of two trees until all the alpha_i are calculated.
 * Then, the proportionality factor between the trees is obtained calling
 * one of the approximation methods, and it's acepted or descarded checking 
 * its value with one of the divergence methods.
 * (Both approximation and divergence methods are selected in the input parameters).
 * @param prI @param prJ the values of the leaves.
 * @param vProbs: vector loading the leaves of both trees.
 * @param vfacK: vector with the alpha_i
 * @param isLast: flag for check whether all the leaves have been considered. 
 * @param factParam the factorisation parameters.
 * @param alpha: the proportionality factor calculated.
 *
 * @return -1 if theyï¿½re not proportional(alpha has been descarted by the 
 *              divergence method), or if one of the leaves is zero.
 * @return 0 otherwise, and in case of the leaves are both zero.
 */

public int isPropApprox(double prI,double prJ,Vector vProbs,Vector vfacK,
                         boolean isLast, FactorisationTools factParam,
                         double alpha[]){

  int indexFac,i,num;
  double alphaI,fprop,averageK,vI,vJ, sumfK;
  Double valueK,valueI,valueJ;
  boolean isP;

  if( prI==0.0 && prJ==0.0 ){
       
    if(isLast && alpha[1]<0.) 
        return(-1); // all values have been zeros 

    vfacK.addElement(new Double (-1.0));  // skip this value 
  }
  else{
    if( prI==0.0 || prJ==0.0) return(-1); 
        //alphaI=0.0;
    else
        alphaI=prJ/prI; 
    
     alpha[1]=1.; //flag for indicate that at least one alphai have been calculated
     vfacK.addElement(new Double(alphaI));
  }
  
  vProbs.addElement(new Double (prI));
  vProbs.addElement(new Double (prJ));
       
  indexFac=0;
  
  if( isLast ){ // all the alpha-i have been calculated 

    switch (factParam.getApproxMethod()) {
        
        case 0: // Method of average
            isP= factParam.appAverage(vProbs, vfacK, alpha);
            break;
        case 1: // Weighted average method 
            isP= factParam.appWeightedAverage(vProbs, vfacK, alpha);
            break;      
        case 2: // Method of minimum Chi^2 divergence
            isP= factParam.appMinChi2Diverg(vProbs, vfacK, alpha);
            break;
        case 3: // Method of minimum mean squared error
            isP= factParam.appMinMeanSqError(vProbs, vfacK, alpha);
            break;
        case 4: // Method of weighted mean squared error
            isP= factParam.appWeightMeanSqError(vProbs, vfacK, alpha);
            break;    
        case 5: // Method of null Kullback-Leibler divergence
            isP= factParam.appNullKLDiverg(vProbs, vfacK, alpha);
            break;
        case 6: // Weight preserving method
            isP= factParam.appWeightPreserve(vProbs, vfacK, alpha);
            break;        
        case 7: // Weighted average method 
            isP= factParam.appHellinger(vProbs, vfacK, alpha);
            break;    
        default: isP=false;    
    }//switch
    
    if(!isP) indexFac=-1;
  }
   
  return (indexFac); // returns 0    (-1 if not proportional)
}

    
    /**
     * Method to compute the Kullback-Leibler distance between two
     * probability trees defined over the same set of variables
     * @param <code>ProbabilityTree</code> probability tree to compare with
     * @param <code>Vector</code> vars which take part on the whole probability tree
     * @return <code>double</code> measure of distance
     */
    public double computeKullbackLeiblerDistance(ProbabilityTree toCompare,Vector vars){
        Configuration conf=new Configuration(vars);
        long cases;
        double distance=0;
        double value, valueToCompare;
        
        int i;
        
        // Compute the number of cases related to the configuration
        
        cases=conf.possibleValues();
        
        // Go on the set of cases
        
        for(i=1; i < cases; i++){
            // Get the value of prob related to this probability tree
            
            value=getProb(conf);
            
            // Get the value for this configuration in toCompare
            
            valueToCompare=toCompare.getProb(conf);
            
            //Compute
            
            if (value == 0)
                value=0.0001;
            if (valueToCompare == 0)
                valueToCompare=0.0001;
            distance+=value*(Math.log(value/valueToCompare));
            
            // Next configuration
            
            conf.nextConfiguration();
        }
        
        // Return the distance
        
        return distance;
    }
    
    /**
     * Method to include the transparent variables contained in a tree
     * @return vector with the list of transparent variables
     */
    public Vector getListTransparents(){
        NodeList transVars=new NodeList();
        Vector result=new Vector();
        
        // Check if it is a leaf node. In this case, return
        if (label == PROBAB_NODE)
            return result;
        
        // If not, check if the root variable is transparent
        if (var.getTransparency() == FiniteStates.TRANSPARENT){
            // Add it
            transVars.insertNode(var);
        }
        
        // Anyway, go on the childs, if any
        for(int i=0; i < var.getNumStates(); i++){
            ProbabilityTree child=getChild(i);
            
            // Go on looking over the subtree
            child.getListTransparents(transVars);
        }
        
        // Finally, covert the NodeList into a Vector
        for(int i=0; i < transVars.size(); i++){
            result.addElement(transVars.elementAt(i));
        }
        return result;
    }
    
    /**
     * Method to get the transparent variables of a tree and store
     * the found variables in a NodeList passed as argument
     * @param transVars NodeList where to insert the transparent
     * variables
     */
    private void getListTransparents(NodeList transVars){
        // If it is a label nodel, return
        if (label == PROBAB_NODE)
            return;
        
        // If the root var is transparent and is not included in transVars,
        // include it
        if (var.getTransparency() == FiniteStates.TRANSPARENT){
            if (transVars.getId(var) == -1)
                transVars.insertNode(var);
        }
        
        // If the node is a complete node, go on the childs
        for(int i=0; i < var.getNumStates(); i++){
            ProbabilityTree child=getChild(i);
            
            // Go on looking over it
            child.getListTransparents(transVars);
        }
    }
    
    /**
     * Convert utility values into probability values using Cooper's
     * transformation
     * @newVar FiniteStates artificial variable used during conversion
     * @k1 first constant to use
     * @k2 second constant to use
     */
    public void convertUtilityIntoProbability(FiniteStates newVar, double k1, double k2){
        int i,j;
        
        // Consider the childs of the tree. Once a child is a value, transform it
        
        if (label == FULL_NODE){
            // Consider every child
            for(i=0; i < var.getNumStates(); i++){
                ProbabilityTree childActual=getChild(i);
                
                // Check if it is a probab node
                
                if (childActual.label == PROBAB_NODE){
                    // Convert the value
                    double finalValue=(childActual.value+k2)/k1;
                    double complement=1-finalValue;
                    
                    // This value is set for the first value of the artificial variable
                    // Create a new tree for this variable
                    ProbabilityTree newTree=new ProbabilityTree(newVar);
                    
                    // Make the childs of this tree be probab nodes
                    for(j=0; j < newVar.getNumStates(); j++){
                        ProbabilityTree finalChild=newTree.getChild(j);
                        //Set the value and the label
                        if (j == 0)
                            finalChild.setValue(finalValue);
                        else
                            finalChild.setValue(complement);
                        
                        // Set the label
                        finalChild.setLabel(PROBAB_NODE);
                    }
                    
                    // Child will now point to newTree
                    this.child.setElementAt(newTree,i);
                }
                else{
                    // Go down looking for leaf nodes to transform
                    childActual.convertUtilityIntoProbability(newVar,k1,k2);
                }
            }
        }
    }

    /**
     * Replaces each leaf by its logarithm. If a leaf
     * is equal to 0 it remains unchanged.
     * The object is modified.
     */

    public void log() {

        int i, nv;

        if (label == PROBAB_NODE) {
            if (value > 0.0) {
                value = Math.log(value);
            }
        } else {
            nv = var.getNumStates();
            for (i = 0; i < nv; i++) {
                getChild(i).log();
            }
        }
    }

    
    
    public Vector getLeaves() {
        Vector leaves = new Vector();
        this.privateGetLeaves(leaves);
        return leaves;
    }
    
    
    private void privateGetLeaves(Vector leaves) {
        
        if (label == PROBAB_NODE) {
            leaves.add(this.value);
        }else {
            
            int nv = var.getNumStates();
            for (int i = 0; i < nv; i++) {
                ((ProbabilityTree)child.get(i)).privateGetLeaves(leaves);
            }
        
        
        }
    
    }
    
    
    
} // End of class
