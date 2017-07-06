/* PotentialTree.java */

package elvira.potential;

import java.util.Vector;
import java.util.Hashtable;
import java.io.*;
import java.lang.reflect.*;

import elvira.tools.FactorisationTools;
import elvira.inference.abduction.Explanation;
import elvira.*;

/**
 * Implementation of class <code>PotentialTree</code>. A potential
 * whose values are represented by a probability tree.
 *
 * @author Andres Cano (acu@decsai.ugr.es)
 * @author Serafin Moral (smc@decsai.ugr.es)
 * @author Jose A. Gamez (jgamez@info-ab.uclm.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 * @author Irene Martinez (irene@ual.es)
 * @since 15/2/2005
 */

public class PotentialTree extends Potential {
    
    /**
     * A <code>ProbabilityTree</code> with the values of this
     * <code>PotentialTree</code>.
     */
    ProbabilityTree values;
    
    /**
     * The number of leaves of the <code>ProbabilityTree values</code>.
     */
    long size;
    
    
    /**
     * main to check factorisation.
     */
    
  public static void main(String args[]) {
        
  PotentialTree t,tp,pp;
  ProbabilityTree t2,tx,ty,tz,aux;
  FiniteStates x,y,z,w,s,unk;
  Configuration c;
  Vector v, v2;
  int i;
  FactorisationTools factParam;
  
  unk= new FiniteStates(2); unk.setName("Uk");
  s= new FiniteStates(3);   s.setName("SS");
  w= new FiniteStates(3);   w.setName("W");
  x = new FiniteStates(2);  x.setName("X");
  z = new FiniteStates(2);  z.setName("Z");
  y = new FiniteStates(2);  y.setName("Y");
  
  v = new Vector();
  
  v.addElement(s);   v.addElement(w);
  v.addElement(z);   v.addElement(x);
  v.addElement(y);
  
  c= new Configuration(v);
  t=new PotentialTree(v);
  
  t.setValue(c,0.2);   c.nextConfiguration();
  t.setValue(c,0.4);   c.nextConfiguration();
  t.setValue(c,0.1);   c.nextConfiguration();
  t.setValue(c,0.2);   c.nextConfiguration();
  /******************************/
  t.setValue(c,0.8);   c.nextConfiguration();
  t.setValue(c,1.6);   c.nextConfiguration(); 
  t.setValue(c,0.4);   c.nextConfiguration();
  t.setValue(c,0.8);
  /********************************/
  
  tx=new ProbabilityTree(x);
  tx.getChild(0).assignProb(0.3);
  tx.getChild(1).assignProb(0.6);
  aux=t.getTree();
  aux.getChild(0).setNewChild(tx,1);
  /*******************************/
  
  ty=new ProbabilityTree(y);
  ty.getChild(0).assignProb(0.9);
  ty.getChild(1).assignProb(0.7);
  aux.getChild(0).setNewChild(ty,2);
  /*******************************/
  
  tz=new ProbabilityTree(z);
  tz.getChild(0).assignProb(0.8);
  //tz.getChild(1).assignProb(1.6);
  ProbabilityTree tmas,tbig;
  tmas=tx.copy();
  
  tz.setNewChild(tmas,1);
  aux.setNewChild(tz,1);
  /*******************************/
  tbig= aux.getChild(0).copy();
  //tbig=new ProbabilityTree(0.44455);
  
  ProbabilityTree tprueba=new ProbabilityTree(unk);
  tprueba.getChild(0).assignProb(0.9999);
  tprueba.getChild(1).assignProb(0.0001);
  tbig.setNewChild(tprueba,2);
  aux.setNewChild(tbig,2);
   
  t.print();
  
    //factParam= new FactorisationTools(double fNod, double fTr, int mFact, int appMethod, 
    //                int divMethod, double porcentProp, int level,int fase);
  
  factParam= new FactorisationTools(0.0, 0.1, 0, 1, 
                                    1, 1.0, 100,1);
  //v2 = t.splitAndFactorisePT(z,factParam);  // BUSCA la z !!
 
  v2 = t.factoriseOnlyPT(z,factParam);
 // v2 = t.splitOnlyPT(z,factParam);  // BUSCA la z !!
  
  System.out.println(",..............");

  PotentialTree res = new PotentialTree(new ProbabilityTree(1));
  for (i=0 ; i<v2.size() ; i++) {
    System.out.println("************************subTree number ="+i);  
    tp = (PotentialTree)v2.elementAt(i);
    tp.print();
    //res = res.combine(tp);
  }

  //System.out.println("RESULTADO COMBINADO");  res.print();
  
  
  Vector statSizeVec= factParam.getClassStatistic(1);
       if(statSizeVec!=null){

           System.out.println(".....These are the statistics about the max of the approximations: ......");
           System.out.println("Number of approximations :" +factParam.vecDistApproxim.size());
           factParam.printStatistics(statSizeVec);}
    }
    
    
    
    
    
    /* CONSTRUCTORS */
    
    
    /**
     * Creates a new <code>PotentialTree</code> with no variables and
     * a single value equal to 0.
     */
    
    public PotentialTree() {
        
        variables = new Vector();
        values = new ProbabilityTree(0);
        size = 1;
    }
    
    
    /**
     * Constructs a <code>PotentialTree</code> from a
     * <code>ProbabilityTree</code>. The tree is not
     * copied.
     *
     * @param t a probability tree.
     */
    
    public PotentialTree(ProbabilityTree t) {
        
        variables = (Vector)(t.getVarList().getNodes().clone());
        values = t;
        size = t.getSize();
    }
    
    /**
     * Construct a <code>PotentialTree</code> from a
     * <code>ProbabilityTree</code> and for a given list of variables.
     * The tree is not copied.
     *
     * @param t a probability tree
     * @param vars a <code>Vector</code> of variables (<code>FiniteStates</code>)
     * that the potential will contain.
     */
    
    public PotentialTree( ProbabilityTree t, Vector vars){
        variables = (Vector)vars.clone();
        values = t;
        size = t.getSize();
    }
    
    
    /**
     * Creates a new <code>PotentialTree</code> for a given list of variables
     * and a tree with a single value equal to 0.
     * @param vars a <code>Vector</code> of variables (<code>FiniteStates</code>)
     * that the potential will contain.
     */
    
    public PotentialTree(Vector vars) {
        
        variables = (Vector)vars.clone();
        values = new ProbabilityTree(0);
        size = 1;
    }
    
    
    /**
     * Creates a new <code>PotentialTree</code> for a given list of variables
     * and a tree with a single value equal to 0.
     * @param vars a <code>NodeList</code> with the variables
     * (<code>FiniteStates</code>) that the potential will contain.
     */
    
    public PotentialTree(NodeList vars) {
        
        variables = (Vector)vars.getNodes().clone();
        values = new ProbabilityTree(0);
        size = 1;
    }
    
    
    /**
     * Constructs a <code>PotentialTree</code> from a <code>NodeList</code>
     * and a <code>Relation</code> defined over a subset of variables of the
     * <code>NodeList</code> passed as parameter.
     * If the potential attached to the relation passed as parameter is not
     * defined over a subset of the variables in the list, the method builds
     * a unitary potential.
     *
     * @param vars the <code>NodeList</code> of variables over for the new
     * potential.
     * @param rel the <code>Relation</code> defined over a subset of vars.
     */
    
    public PotentialTree(NodeList vars, Relation rel) {
        
        int i, nv, pos;
        Configuration conf, subConf;
        PotentialTree pot;
        
        variables = (Vector)vars.toVector().clone();
        
        // determining if pot.variables is a subset of vars
        
        if ( (rel.getVariables().kindOfInclusion(vars)).equals("subset") ) {
            pot = (PotentialTree)rel.getValues();
            values = pot.getTree().copy();
            size = pot.getSize();
        }
        else{ // unitary potential
            values = new ProbabilityTree(1.0);
            size = 1;
        }
    }
    
    
    /**
     * Constructs a <code>PotentialTree</code> from another
     * <code>Potential</code>.
     *
     * @param <code>pot</code> the <code>Potential</code> to be transformed to
     * <code>PotentialTree</code>.
     */
    
    public PotentialTree(Potential pot) {
        
        this(pot.getVariables());
        
        Configuration conf;
        Vector vars;
        
        vars = (Vector) pot.getVariables().clone();
        conf = new Configuration();
        
        setTreeFromTable(pot,values,conf,vars);
        updateSize();
    }
    
    /** Constructs a <code>PotentialTree</code> from a <code>PotentialContinuousPT</code>
     *  @param <code>PotentialContinuousPT</code> whith only discrete nodes.
     */
    
    public PotentialTree(PotentialContinuousPT pot){
        
        this(pot.getVariables());
        size=pot.getSize();
        values = new ProbabilityTree(pot.getTree());
        
    }
    /* METHODS */
    
    
    /**
     * Makes a new <code>PotentialTree</code>.
     * @return an empty <code>PotentialTree</code>.
     */
    
    public Potential makePotential() {
        
        return new PotentialTree();
    }

    /**
     * Makes a new instance of a PotentialTree
     * @param vars list of variables of the new PotentialTree
     * @return a new instance of a PotentialTree
     */
    public PotentialTree getInstance(Vector vars){
        return new PotentialTree(vars);
    }
    
    
    /**
     * Recursive procedure that constructs a <code>ProbabilityTree</code>
     * from a probability table.
     * @param tree the <code>ProbabilityTree</code> we are constructing.
     * This tree is modified.
     * @param conf the <code>Configuration</code> that leads to the subtree
     * we are operating in this recursion step. This configuration is
     * modified.
     * @param vars <code>Vector</code> of variables (<code>FiniteStates</code>)
     * not already explored. This vector is modified.
     */
    
    private void setTreeFromTable(Potential pot, ProbabilityTree tree,
    Configuration conf, Vector vars) {
        
        FiniteStates var;
        int i;
        Vector aux;
        
        if (vars.size() == 0)
            tree.assignProb(pot.getValue(conf));
        else {
            var = (FiniteStates) vars.elementAt(0);
            vars.removeElementAt(0);
            tree.assignVar(var);
            
            for(i=0 ; i<var.getNumStates() ; i++) {
                aux = (Vector)vars.clone();
                conf.insert(var,i);
                setTreeFromTable(pot,tree.getChild(i),conf,aux);
                conf.remove(conf.getVariables().size()-1);
            }
        }
    }
    
    
    /**
     * Assigns a tree to the potential.
     * @param tree the <code>ProbabilityTree</code> to be assigned
     * as values of the potential.
     */
    
    public void setTree(ProbabilityTree tree) {
        
        values = tree;
        size = tree.getSize();
    }
    
    
    /**
     * Gets the values of the potential.
     * @return the <code>ProbabilityTree</code> containing the values
     * of the potential.
     */
    
    public ProbabilityTree getTree(){
        
        return values;
    }
    
    
    /**
     * Gets the size (number of values) of the potential.
     * @return the number of values (size) of the potential.
     */
    
    public long getSize() {

        return size;
    }

    /**
     * Method for getting the number of nodes of the potential
     * @return number of nodes of the probability tree
     */
    public long getNumberOfNodes(){
       return values.getNumberOfNodes();
    }

    /**
     * Method for getting the number of leaves of the potential
     * @return number of nodes of the probability tree
     */
    public long getNumberOfLeaves(){
       return values.getNumberOfLeaves();
    }
    
    /**
     * Gets the size of the potential, taken directly from the
     * <code>ProbabilityTree values</code>.
     * @return the number of values.
     */
    
    public long getNumberOfValues() {
        
        return values.getSize();
    }
    
    
    /**
     * Saves the potential to a file. Saves just the tree.
     * @param p the <code>PrintWriter</code> where the potential will be written.
     */
    
    public void save(PrintWriter p) {
        
        p.print("values= tree ( \n");
        
        values.save(p,10);
        
        p.print("\n);\n\n");
    }
    
    
    /**
     * Prints a <code>PotentialTree</code> to the standard output.
     */
    
    public void print() {
        
        super.print();
        
        System.out.println("Size: "+size);
        
        if (values.isVariable()) {
            System.out.println("node "+values.getVar().getName());
            System.out.print("values= tree ( \n");
            
            values.print(10);
            
            System.out.print("\n);\n\n");
        }
        else if (values.isProbab()) {
            System.out.print("values= tree  \n       "+values.getProb()+";\n");
        }
        else System.out.println("The Probability Tree is not build");
    }
    
    
    /**
     * Compares this potential with the one given as argument.
     * @param pot the PotentialTree to compare with this.
     * @return 0 if both trees represent the same potential or 1 otherwise.
     */
    
    public int compareTo(PotentialTree pot) {
        
        double i, v1, v2,  numberOfConfigurations=FiniteStates.getSize(variables);
        Configuration conf = new Configuration(variables);
        
        for (i=0 ; i<numberOfConfigurations ; i++) {
            v1 = getValue(conf);
            v2 = pot.getValue(conf);
            if (v1 != v2)
                return 1;
            conf.nextConfiguration();
        }
        
        return (0);
    }
    
    
    /**
     * Compares this potential with the one given as argument
     * and prints the unequal mismatching configurations by the standard output.
     * @param pot the PotentialTree to compare with this.
     * @return 0 if both trees represent the same potential or 1 otherwise.
     */
    
    public int compareAndPrint(PotentialTree pot) {
        
        double i, v1, v2,  numberOfConfigurations=FiniteStates.getSize(variables);
        Configuration conf = new Configuration(variables);
        int comparison = 0;
        
        for (i=0 ; i<numberOfConfigurations ; i++) {
            v1 = getValue(conf);
            v2 = pot.getValue(conf);
            if (v1 != v2) {
                comparison = 1;
                System.out.println("Configuration : \n");
                conf.print();
                System.out.println("Values: "+v1+" "+v2+"\n");
            }
            conf.nextConfiguration();
        }
        
        return (comparison);
    }
    
    
    /**
     * Gets the value for a configuration.
     * @param conf a <code>Configuration</code> of variables.
     * @return the value corresponding to <code>Configuration conf</code>.
     */
    
    public double getValue(Configuration conf) {
        
        return values.getProb(conf);
    }
    
    
    /**
     * Gets the value for a configuration. In this case, the
     * configuration is represented by means of an array of <code>int</code>.
     * At each position, the value for a certain variable is stored.
     * To know the position in the array corresponding to a given
     * variable, we use a hash table. In that hash table, the
     * position of every variable in the array is stored.
     *
     * @param positions a <code>Hashtable</code> with the positions of the
     * variables in the array.
     * @param conf an array of <code>int</code> with the values of the variables.
     * @return the value corresponding to configuration <code>conf</code>.
     */
    
    public double getValue(Hashtable positions, int[] conf) {
        
        return values.getProb(positions,conf);
    }
    
    
    /**
     * Multiplies the content of vector  <code>  values </code> by
     * each one of the numbers in the potential for different cases
     * of the variable of position <code> posX </code> restricted to <code> conf </code>.
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
     * @param nv number of states of the variable of position <code> posX </code>
     * @param conf an array of <code>int</code> with the values of the variables.
     * @param vals the vector containing the numbers that are going to be multiplied
     *
     */
    
    public void getVectors(Hashtable positions, int posX, int nv, int[] conf, double[] vals) {
        
        values.getVectors(positions,posX,nv,conf,vals);
    }
    
    /**
     * It adds to <code> ActiveNodes </code> all the variables in <code> conf </code>
     * appearing in the part of the tree associated to the potential, when following
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
     * @param nv number of states of the variable of position <code> posX </code>
     * @param conf an array of <code>int</code> with the values of the variables.
     * @param vals the vector containing the numbers that are going to be multiplied
     *
     */
    
    public void getActiveNodes(Hashtable positions,int posX ,int[] conf, Vector activeNodes) {
        values.getActiveNodes(positions,posX,conf,activeNodes);
        
    }
    
    
    
    
    
    
    /**
     * Extends a potential expanding it in such a way that the assigns the value
     * <code> newVal </code> to the given configuration.
     * It has two modalities depending of the value of <code> mode </code>.
     * With <code> mode</code> equal to 1, it carries out a full expansion for
     * all the variables in the configuration.
     * With <code> mode</code> equal to 0, it expands only for one of the variables in the
     * configuration for which the potential tree was not already branched
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
     * @param mode an integer determining the type of branching (0: full, 1: one variable)
     *
     **/
    
    public void update(Hashtable positions,  int[] conf, Vector activeNodes, double newVal, int mode){
        
        NodeList aux;
        
        aux = new NodeList(activeNodes);
        values.update(positions,  conf, newVal, aux, mode);
        
        
    }
    
    
    
    /**
     * Sets the value for a configuration.
     * @param conf a <code>Configuration</code>.
     * @param x a <code>double</code>, the new value for <code>conf</code>.
     */
    
    public void setValue(Configuration conf, double x) {
        
        Configuration aux;
        ProbabilityTree tree;
        FiniteStates var;
        int i, p, val, s;
        boolean update;
        
        
        update = true;
        //aux = conf.copy();
        aux = conf.duplicate();
        s = conf.getVariables().size();
        tree = values;
        
        for (i=0 ; i<s ; i++) {
            
            if (!tree.isVariable()) {
                var = aux.getVariable(0);
                val = aux.getValue(0);
                aux.remove(0);
                
                if (tree.isProbab()){ // if the node is a probability,
                    update = false;      // do not update the number of leaves.
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
        if (update)
            size++;
    }
    
    
    /**
     * Gets the sum of the values of the potential.
     * @return the sum of all the values in the potential.
     */
    
    public double totalPotential() {
        
        long s;
        
        s = (long)FiniteStates.getSize(variables);
        
        return values.sum(s);
    }
    
    
    /**
     * Gets the sum of the values of the potential consistent with a given
     * configuration of variables.
     * @param conf a <code>Configuration</code>.
     * @return the sum of all the values in the potential
   matching with <code>Configuration conf</code>. The result is the same
   as restricting the potential to <code>conf</code> and then using
   <code>totalPotential()</code>.
     * @see totalPotencial
     */
    
    public double totalPotential(Configuration conf) {
        
        Configuration auxConf;
        FiniteStates temp;
        int i, nv;
        double sum;
        
        nv = 1;
        for (i=0 ; i<variables.size() ; i++) {
            temp = (FiniteStates)variables.elementAt(i);
            nv = nv * temp.getNumStates();
        }
        
        // Evaluate the tree for all the possible configurations
        // and sum the values.
        
        auxConf = new Configuration(variables,conf);
        sum = 0.0;
        for (i=0 ; i<nv ; i++) {
            sum += getValue(auxConf);
            auxConf.nextConfiguration(conf);
        }
        
        return sum;
    }
    
    /**
     * Gets the sum of the values consistent with a given configuration
     * of variables: consider only that values matching the configuration
     * Equivalence: remove adding variables not in conf
     */
    
    
    public double sumConsistent(Configuration conf){
        Configuration auxConf,total;
        int i,nv;
        double sum;
        
        // Create a new configuration with the variables not in conf
        
        auxConf=new Configuration(conf,variables,0);
        
        // Make a configuration for the whole set of variables
        
        total=new Configuration(variables);
        
        // Initialize sum
        
        sum=0.0;
        
        // Get the number of possible values for a configuration
        
        nv=auxConf.possibleValues();
        
        for(i=0; i < nv; i++){
            
            // Give total the values in conf and auxConf
            
            total.setValues(conf,auxConf);
            
            // Get the value and add it
            
            sum+=getValue(total);
            auxConf.nextConfiguration();
        }
        
        // return sum
        
        return sum;
    }
    
    /**
     * Method to get the values (non cero) consistent with a
     * given configuration
     */
    
    public int nonCeroValues(Configuration conf){
        Configuration auxConf,total;
        int i,nv;
        int count=0;
        
        // Create a new configuration with the variables not in conf
        
        auxConf=new Configuration(conf,variables,0);
        
        // Make a configuration for the whole set of variables
        
        total=new Configuration(variables);
        
        // Get the number of possible values for a configuration
        
        nv=auxConf.possibleValues();
        
        // Go on possible values and consider those different to 0
        //
        for(i=0; i < nv; i++){
            
            // Give total the values in conf and auxConf
            
            total.setValues(conf,auxConf);
            
            // Get the value and add it
            
            if (getValue(total) != 0.0)
                count++;
            
            auxConf.nextConfiguration();
        }
        
        // return count
        
        return count;
    }
    
    /**
     * Method to add a value to non cero values consistent with the
     * given configuration
     */
    
    public void addValue(Configuration conf,double toAdd){
        Configuration auxConf,total;
        int i,nv;
        int count=0;
        double value;
        
        // Create a new configuration with the variables not in conf
        
        auxConf=new Configuration(conf,variables,0);
        
        // Make a configuration for the whole set of variables
        
        total=new Configuration(variables);
        
        // Get the number of possible values for a configuration
        
        nv=auxConf.possibleValues();
        
        // Go on possible values and consider those different to 0
        
        for(i=0; i < nv; i++){
            
            // Give total the values in conf and auxConf
            
            total.setValues(conf,auxConf);
            
            // Get the value and add it
            
            if ((value=getValue(total)) != 0.0){
                value+=toAdd;
                setValue(total,value);
            }
            
            auxConf.nextConfiguration();
        }
        
    }
    
    /**
     * Gets the entropy of the potential.
     * @return the entropy of the potential.
     */
    
    public double entropyPotential() {
        
        Configuration auxConf;
        FiniteStates temp;
        int i, nv;
        double sum, x;
        
        nv = 1;
        for (i=0 ; i<variables.size() ; i++) {
            temp = (FiniteStates)variables.elementAt(i);
            nv = nv * temp.getNumStates();
        }
        
        
        // Evaluate the tree for all the configurations and
        // compute the entropy.
        
        auxConf = new Configuration(variables);
        sum = 0.0;
        
        for (i=0 ; i<nv ; i++) {
            x = getValue(auxConf);
            if (x > 0.0)
                sum += x * Math.log(x);
            auxConf.nextConfiguration();
        }
        
        return ((-1.0) * sum);
    }
    
    
    /**
     * Gets the entropy of the potential restricted to a given configuration.
     * @param conf a <code>Configuration</code>.
     * @return the entropy of the values of the potential
   matching with <code>Configuration conf</code>. The result is the
   same as restricting first to <code>conf</code> and then using
   <code>entropyPotential()</code>.
     * @see entropyPotential
     */
    
    public double entropyPotential(Configuration conf) {
        
        Configuration auxConf;
        FiniteStates temp;
        int i, nv;
        double sum, x;
        
        nv = 1;
        for (i=0 ; i<variables.size() ; i++) {
            temp = (FiniteStates)variables.elementAt(i);
            nv = nv * temp.getNumStates();
        }
        
        // Evaluate the tree for all the configurations and
        // compute the entropy.
        
        auxConf = new Configuration(variables,conf);
        sum = 0.0;
        
        for (i=0 ; i<nv ; i++) {
            x = getValue(auxConf);
            if (x > 0.0)
                sum += x * Math.log(x);
            auxConf.nextConfiguration(conf);
        }
        
        return ((-1.0) * sum);
    }
    
    
    /**
     * Restricts the potential to a given configuration.
     * @param conf the restricting <code>Configuration</code>.
     * @return a new <code>PotentialTree</code> where variables
     * in <code>conf</code> have been instantiated to their values
     * in <code>conf</code>.
     */
    
    public Potential restrictVariable(Configuration conf) {
        
        Vector aux;
        FiniteStates temp;
        //PotentialTree pot;
        Object pot;
        ProbabilityTree tree;
        int i, p, s, v;
        
        s = variables.size();
        aux = new Vector(s); // New list of variables.
        tree = getTree();    // tree will be the new tree
        
        for (i=0 ; i<s ; i++) {
            temp = (FiniteStates)variables.elementAt(i);
            p = conf.indexOf(temp);
            
            if (p == -1) // If it is not in conf, add to the new list.
                aux.addElement(temp);
            else {       // Otherwise, restrict the tree to it.
                v = conf.getValue(p);
                tree = tree.restrict(temp,v);
            }
        }
        
        Class currentClass=getClass();
        Class[] arrayTypes=new Class[1];
        arrayTypes[0]=Vector.class;
        Object[] arrayArgs=new Object[1];
        arrayArgs[0]=aux;
        pot=null;
        try{
            Constructor constructor=currentClass.getConstructor(arrayTypes);
            pot=constructor.newInstance(arrayArgs);
        }
        catch(Exception e){
            System.out.println(e);
            System.exit(1);
        }
        //pot = new PotentialTree(aux);
        ((PotentialTree)pot).setTree(tree);
        
        return (PotentialTree)pot;
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
        pot = new PotentialTree(v);
        
        tree1 = getTree();                          // Tree of this potential.
        tree2 = ((PotentialTree)p).getTree();       // Tree of the argument.
        
        tree = ProbabilityTree.divide(tree1,tree2); // The new tree.
        
        pot.setTree(tree);
        
        return pot;
    }
    
    
    /**
     * Combines this potential with the argument. The argument <code>p</code>
     * can be a <code>PotentialTable</code> or a <code>PotentialTree</code>.
     * @param p a <code>Potential</code>.
     * @returns a new <code>PotentialTree</code> consisting of the combination
     * of <code>p</code> and this <code>Potential</code>.
     */
    
    public Potential combine(Potential p) {
        
        Vector v, v1, v2;
        FiniteStates aux;
        int i, nv;
        PotentialTree pot;
        double x;
        ProbabilityTree tree, tree1, tree2;
        
        if (p.getClass().getName().equals("elvira.potential.PotentialTree")) {
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
            pot = new PotentialTree(v);
            
            tree1 = getTree();                           // Tree of this potential.
            tree2 = ((PotentialTree)p).getTree();        // Tree of the argument.
            
            tree = ProbabilityTree.combine(tree1,tree2); // The new tree.
            
            pot.setTree(tree);
        }
        else if (p.getClass().getName().equals("elvira.potential.PotentialTable") ||
        p.getClass().getName().equals("elvira.potential.PotentialConvexSet")) {
            return combine((PotentialTable)p);
        }
        else if (p.getClass()==PotentialContinuousPT.class){
            return p.combine(this);
        }
        else {
            System.out.println("Error in Potential PotentialTree.combine(Potential p): argument p was not a PotentialTree nor a PotentialTable nor a PotentialConvexSet");
            try{
                throw new Exception();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            System.exit(1);
            pot = this;
        }
        
        return pot;
    }
    
    
    /**
     * Combines in this <code>PotentialTree</code> the <code>PotentialTree</code>s
     * <code>pot1</code> and <code>pot2</code>, but only the fields indicated by
     * <code>field</code> with the following meaning:
     * <UL>
     * <LI> field=ONLY_VARS then combine only field <code>variable</code>.
     * <LI> field=ONLY_VALUES then combine only field <code>values</code>.
     * </UL>
     * @param pot1 the first <code>Potential</code> to be combined.
     * @param pot2 the second <code>Potential</code> to be combined.
     */
    
    public void combine(Potential pot1,Potential pot2,int field) {
        
        if (!(pot1 instanceof PotentialTree) || !(pot2 instanceof PotentialTree)) {
            System.out.println("Error in "+
            "PotentialTree.combine(Potential pot1,Potential pot2,Vector vars):"+
            "pot1 or pot2 was not a PotentialTree");
            System.exit(1);
        }
        
        if (field == ONLY_VARS) { // Combine only field variable of pot1 and pot2
            super.combine(pot1,pot2,ONLY_VARS);
        }
        else if (field == ONLY_VALUES) { //Combine only values of pot1, pot2
            ProbabilityTree tree1,tree2,tree;
            tree1 = ((PotentialTree)pot1).getTree();
            tree2 = ((PotentialTree)pot2).getTree();
            tree = ProbabilityTree.combine(tree1,tree2);
            setTree(tree);
        }
    }
    
    
    /**
     * Combines this potential with the <code>PotentialTable</code>
     * of the argument.
     * @param p a <code>PotentialTable</code>.
     * @returns a new <code>PotentialTree</code> consisting of the combination
     * of <code>p (PotentialTable)</code> and this <code>PotentialTree</code>.
     */
    
    public PotentialTree combine(PotentialTable p) {
        PotentialTree pt;
        pt = p.toTree();
        return (PotentialTree)combine((Potential)pt);
    }
    
    
    /**
     * Combines this potential with the <code>PotentialTree</code>
     * of the argument.
     * @param p a <code>PotentialTree</code>.
     * @returns a new <code>PotentialTree</code> consisting of the combination
     * of <code>p (PotentialTree)</code> and this <code>PotentialTree</code>.
     */
    
/*public PotentialTree combine(PotentialTree p) {
  return (PotentialTree)combine((Potential)p);
}*/
    
    /**
     * Sums this potential with <code>Potential</code> pot
     * @param p the <code>Potential</code> to sum with this
     * <code>Potential</code>. p must be a PotentialTable, PotentialConvexSet or
     * PotentialTree.
     * @return a new <code>PotentialTree</code> consisting of the sum
     * of <code>p</code> and this <code>Potential</code>.
     */
    
    public Potential addition(Potential p) {
        Vector v, v1, v2;
        FiniteStates aux;
        int i, nv;
        PotentialTree pot;
        double x;
        ProbabilityTree tree, tree1, tree2;
        
        if (p.getClass().getName().equals("elvira.potential.PotentialTree")) {
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
            pot = new PotentialTree(v);
            tree1 = getTree();                           // Tree of this potential.
            tree2 = ((PotentialTree)p).getTree();        // Tree of the argument.
            tree = ProbabilityTree.sum(tree1,tree2); // The new tree.
            pot.setTree(tree);
        }
        else if (p.getClass().getName().equals("elvira.potential.PotentialTable") ||
        p.getClass().getName().equals("elvira.potential.PotentialConvexSet")) {
            return sum((PotentialTable)p);
        }
        else {
            System.out.println("Error in Potential PotentialTree.combine(Potential p): argument p was not a PotentialTree nor a PotentialTable nor a PotentialConvexSet");
            System.exit(1);
            pot = this;
        }
        
        return pot;
    }
    
    /**
     * Sums this potential with the <code>PotentialTable</code>
     * of the argument.
     * @param p a <code>PotentialTable</code>.
     * @returns a new <code>PotentialTree</code> consisting of the combination
     * of <code>p (PotentialTable)</code> and this <code>PotentialTree</code>.
     */
    
    public PotentialTree sum(PotentialTable p) {
        PotentialTree pt;
        pt = p.toTree();
        return (PotentialTree)addition((Potential)pt);
    }
    
    /**
     * Combines two potentials. The argument <code>p</code> MUST be a subset of
     * the potential which receives the message, and must be a
     * <code>PotentialTree</code>.
     *
     * IMPORTANT: this method modifies the object which receives the message.
     *
     * @param p the <code>PotentialTree</code> to combine with this.
     */
    
    public void combineWithSubset(Potential p) {
        ProbabilityTree tree;
        tree = ProbabilityTree.combine(this.getTree(),
        ((PotentialTree)p).getTree());
        this.setTree(tree);
    }
    
    
    /**
     * Removes a list of variables by adding over all their states.
     * @param vars <code>Vector</code> of <code>FiniteStates</code> variables
     * to be removed from the potential.
     * @return A new <code>PotentialTree</code> with the result of the operation.
     */
    
    public PotentialTree addVariable(Vector vars) {
        
        Vector aux;
        FiniteStates var1, var2;
        int i, j;
        boolean found;
        PotentialTree pot;
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
        
        pot = new PotentialTree(aux); // The new tree.
        tree = values;
        
        for (i=0 ; i<vars.size() ; i++) {
            var1 = (FiniteStates)vars.elementAt(i);
            tree = tree.addVariable(var1);
        }
        
        pot.setTree(tree);
        
        return pot;
    }
    
    
    /**
     * Removes the argument variable summing over all its values.
     * @param var a <code>Node</code> variable.
     * @return a new <code>PotentialTree</code> with the result of the deletion.
     */
    
    public Potential addVariable(Node var) {
        
        Vector v;
        PotentialTree pot;
        
        v = new Vector();
        v.addElement(var);
        
        pot = addVariable(v);
        
        return pot;
    }
    
    
    /**
     * Removes in <code>Potential pot</code> the argument variable
     * summing over all its values, but only the fields indicated by
     * <code>field</code> with the following meaning:
     * <UL>
     * <LI> field=ONLY_VARS then sum out only field <code>variable</code>.
     * </UL>
     * The result is put in this  <code>Potential</code>.
     * @param pot a <code>Potential</code> over which variable <code>var</code>
     * will be summed out.
     * @param var a <code>FiniteStates</code> variable.
     * @param field a <code>int</code> to determine to which field
     * marginalization affects.
     */
    
    public void addVariable(Potential pot, FiniteStates var, int field) {
        
        FiniteStates var1;
        int i;
        
        if (field == ONLY_VARS) { // sum out only in field variable
            super.addVariable(pot,var,field);
        }
        else if (field == ONLY_VALUES) { // sum out only in values
            ProbabilityTree tree;
            
            tree = ((PotentialTree)pot).values;
            tree = tree.addVariable(var);
            setTree(tree);
        }
    }
    
    
    /**
     * Marginalizes a <code>PotentialTree</code> to a list of variables.
     * It is equivalent to remove (add) the other variables.
     * @param vars a <code>Vector</code> of <code>FiniteStates</code> variables.
     * @return a new <code>PotentialTree</code> with the marginal.
     * @see addVariable(Vector vars)
     */
    
    public Potential marginalizePotential(Vector vars) {
        
        Vector v;
        int i, j;
        boolean found;
        FiniteStates var1, var2;
        PotentialTree pot;
        
        v = new Vector(); // List of variables to remove
        // (those not in vars).
        for (i=0 ; i<variables.size() ; i++) {
            var1 = (FiniteStates)variables.elementAt(i);
            found = false;
            
            for (j=0 ; j<vars.size(); j++) {
                var2 = (FiniteStates)vars.elementAt(j);
                if (var1 == var2) {
                    found = true;
                    break;
                }
            }
            
            if (!found)
            {v.addElement(var1);}
        }
        
        pot = addVariable(v);
        
        return pot;
    }
    
    
    /**
     * Removes a list of variables by applying marginalization by maximum.
     * @param vars a <code>Vector</code> of <code>FiniteStates</code> variables.
     * @return a new <code>PotentialTree</code> with the marginal.
     */
    
    public Potential maxMarginalizePotential(Vector vars) {
        
        Vector aux;
        FiniteStates var1, var2;
        int i, j;
        boolean found;
        PotentialTree pot;
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
        
        pot = new PotentialTree(vars); // The new tree.
        
        tree = values;
        
        for (i=0 ; i<aux.size() ; i++) {
            var1 = (FiniteStates)aux.elementAt(i);
            tree = tree.maximizeOverVariable(var1);
        }
        
        pot.setTree(tree);
        
        return pot;
    }
    
    
    /**
     * Gets the configuration of maximum probability consistent with a
     * configuration of variables.
     * @param subconf the subconfiguration to ensure consistency.
     * @return the <code>Configuration</code> of maximum probability
     * included in the <code>Potential</code>, that is consistent with the
     * subConfiguration passed as parameter (this subconfiguration can be empty).
     *
     * NOTE: if there are more than one configuration with maximum
     * probability, the first one is returned.
     */
    
    public Configuration getMaxConfiguration(Configuration subconf) {
        
        Explanation best;
        Configuration bestFound;
        Vector confValues;
        int i;
        Configuration conf;
        
        // first we create a configuration with all the values set to -1.
        // -1 indicates that that variable can take every possible state.
        
        confValues = new Vector();
        for (i=0 ; i<variables.size() ; i++)
            confValues.addElement(new Integer(-1));
        bestFound = new Configuration(variables,confValues);
        
        // an explanation that will contain the best found is initialized with
        // probability equal to -1.0
        
        best = new Explanation(new Configuration(variables),-1.0);
        
        best = values.getMaxConfiguration(best,bestFound,subconf);
        
        // if some value in best.conf is -1, then we have found a set of
        // configurations of maximal probability. In this case we return the
        // first one by changing -1 by 0.
        
        conf = best.getConf();
        for (i=0 ; i<conf.size() ; i++)
            if (conf.getValue(i) == -1)
                conf.putValue(conf.getVariable(i),0);
        
        return conf;
    }
    
    
    /**
     * @param subconf the subconfiguration to ensure consistency
     * @param list the list of configurations to be differents
     * @return the configuration of maximum probability included in the
     * potential, that is consistent with the subConfiguration passed as
     * parameter (this subconfiguration can be empty), and differents to
     * all the configurations passed in the vector
     *
     * NOTE: if there are more than one configuration with maximum
     * probability, the first one is returned
     */
    
    public Configuration getMaxConfiguration(Configuration subconf,
    Vector list) {
        
        Explanation best;
        Configuration bestFound;
        Vector confValues;
        int i;
        Configuration conf;
        
        // first we create a configuration with all values set to -1.
        // -1 indicates that this variable can take every possible state.
        
        confValues = new Vector();
        for (i=0 ; i<variables.size() ; i++)
            confValues.addElement(new Integer(-1));
        bestFound = new Configuration(variables,confValues);
        
        // an explanation that will contain the best found is initialized with
        // probability equal to -1.0.
        
        best = new Explanation(new Configuration( ),-1.0);
        
        best = values.getMaxConfiguration(best,bestFound,subconf,list);
        
        return best.getConf();
    }
    
    
    /**
     * Sorts the variables in the tree putting first the most informative
     * ones, and limits the number of leaves.
     * @param maxLeaves maximum number of leaves in the new tree.
     * @return a new <code>PotentialTree</code> sorted and bounded.
     */
    
    public Potential sortAndBound(int maxLeaves) {
        
        PotentialTree pot;
        ProbabilityTree treeNew, treeSource, treeSource2, treeResult;
        NodeQueue nodeQ;
        PriorityQueue queue;
        FiniteStates var;
        int j, nv;
        long newSize, maxSize;
        
        // Size of the entire tree (expanded).
        maxSize = (long)FiniteStates.getSize(variables);
        
        nodeQ = new NodeQueue(1E20); // Infinity node.
        
        // Priority queue where the tree nodes will be stored
        // sorted according to their information value.
        queue = new PriorityQueue(nodeQ);
        
        // The new potential (with the same variables as this).
        pot = new PotentialTree(variables);
        
        treeNew = new ProbabilityTree();
        treeSource = values;
        pot.setTree(treeNew);
        
        if (!values.isProbab()) { // If the tree node is not a probab.
            // put it in the queue.
            nodeQ = new NodeQueue(treeNew,treeSource,maxSize);
            queue.insert(nodeQ);
            
            // While the size is not exceeded, add new nodes to the tree.
            while (!queue.isEmpty() &&
            ((pot.size + queue.size()) < maxLeaves)) {
                nodeQ = queue.deleteMax();
                treeResult = (ProbabilityTree) nodeQ.getRes();
                treeSource = (ProbabilityTree) nodeQ.getSource();
                var = nodeQ.getVar();
                treeResult.assignVar(var);
                nv = var.getNumStates();
                newSize = maxSize / nv;
                
                // For each child of the selected node:
                for (j=0 ; j<nv ; j++) {
                    treeSource2 = treeSource.restrict(var,j);
                    treeNew = treeResult.getChild(j);
                    if (!treeSource2.isProbab()) { // If the tree node is not
                        // a prob. put it in the queue.
                        nodeQ = new NodeQueue(treeNew,treeSource2,newSize);
                        queue.insert(nodeQ);
                    }
                    else {
                        treeNew.assignProb(treeSource2.getProb());
                        pot.size++;
                    }
                }
            }
            
            // Substitute the remaining nodes by the average value.
            while (!queue.isEmpty()) {
                nodeQ = queue.deleteMax();
                treeResult = (ProbabilityTree) nodeQ.getRes();
                treeSource = (ProbabilityTree) nodeQ.getSource();
                treeResult.assignProb(treeSource.average());
                pot.size++;
            }
        }
        else {
            treeNew.assignProb(values.getProb());
            pot.size++;
        }
        
        return pot;
    }
    
    /**
     * Sorts the variables in the tree putting first the most informative
     * ones, and prune similar leaves
     * @param threshold threshold of proximity
     * @return a new <code>PotentialTree</code> sorted and bounded.
     */
    
    public PotentialTree sortAndBound(double threshold) {
        PotentialTree pot;
        
        // Sort it. Only if the potential has more than 1 variable
        if (variables.size() > 1){
            pot=sort();
        }
        else{
            pot=this;
        }
        
        // Prune it
        pot.limitBound(threshold);
        
        // Return the final portential
        return pot;
    }
    
    
    /**
     * Sorts the variables in the tree, according to an information
     * criterion. The same as <code>sortAndBond</code> method, but without
     * the restriction of a maximum number of nodes.
     * @return a new <code>PotentialTree</code> with the variables sorted.
     */
    
    public PotentialTree sort() {
        
        PotentialTree pot;
        ProbabilityTree treeNew, treeSource, treeSource2, treeResult;
        NodeQueue nodeQ;
        PriorityQueue queue;
        FiniteStates var;
        int j, nv;
        long newSize, maxSize;
        
        
        maxSize = (long)FiniteStates.getSize(variables);
        nodeQ = new NodeQueue(1E20);
        queue = new PriorityQueue(nodeQ);
        
        //pot = new PotentialTree(variables);
        pot = getInstance(variables);
        treeNew = new ProbabilityTree();
        treeSource = values;
        pot.setTree(treeNew);
        
        
        if (!values.isProbab()) { // If the tree node is not a probab.
            // put it in the queue.
            nodeQ = new NodeQueue(treeNew,treeSource,maxSize);
            queue.insert(nodeQ);
            
            while (!queue.isEmpty()) {
                nodeQ = queue.deleteMax();
                treeResult = (ProbabilityTree) nodeQ.getRes();
                treeSource = (ProbabilityTree) nodeQ.getSource();
                var = nodeQ.getVar();
                treeResult.assignVar(var);
                nv = var.getNumStates();
                newSize = maxSize / nv;
                
                for (j=0 ; j<nv ; j++) {
                    treeSource2 = treeSource.restrict(var,j);
                    treeNew = treeResult.getChild(j);
                    if (treeSource2.getLabel()!=2) {
                        nodeQ = new NodeQueue(treeNew,treeSource2,newSize);
                        queue.insert(nodeQ);
                    }
                    else {
                        treeNew.assignProb(treeSource2.getProb());
                        pot.size++;
                    }
                }
            }
        }
        else {
            treeNew.assignProb(values.getProb());
            pot.size++;
        }
        
        return pot;
    }
    
    /**
     * Sorts the variables in the tree, according to an information
     * criterion referred to utility.
     * @return a new <code>PotentialTree</code> with the variables sorted.
     */
    
    public PotentialTree sortUtility() {
        
        PotentialTree pot;
        ProbabilityTree treeNew, treeSource, treeSource2, treeResult;
        NodeQueue nodeQ;
        PriorityQueue queue;
        FiniteStates var;
        int j, nv;
        long newSize, maxSize;
        
        // Get the cardinal of cartesian product of all the values
        // for the variables
        
        maxSize = (long)FiniteStates.getSize(variables);
        
        // Build a PriorityQueue
        
        nodeQ = new NodeQueue(1E20);
        queue = new PriorityQueue(nodeQ);
        
        // Build the potential to return
        
        pot = new PotentialTree(variables);
        
        // Build a probability tree; it will be used as VALUES
        // for pot
        
        treeNew = new ProbabilityTree();
        pot.setTree(treeNew);
        
        // Retrieve the original values and store it in treeSource
        // (a probability tree)
        
        treeSource = values;
        
        // If the node of the tree is not a leaf; if not it is a
        // tree and we have to measure the importance they have
        // respecto to the utility values
        
        if (!values.isProbab()) {
            //Build the NodeQueue
            nodeQ = new NodeQueue(treeNew,treeSource,maxSize,true);
            
           
            
            // The construction implies to get the measure of its importance
            // in utility function
            
            queue.insert(nodeQ);
            
            // Loop over the priority queue
            
            while (!queue.isEmpty()) {
                // Get the NodeQueue with maximum priority (i.e. the
                // most important variable respect to utility values)
                
                nodeQ = queue.deleteMax();
                
                // Store in treeResult the probabilityTree associated to
                // the NodeQueue
                
                treeResult = (ProbabilityTree) nodeQ.getRes();
                
                // Retrieve the original potential
                
                treeSource = (ProbabilityTree) nodeQ.getSource();
                
                // Assign to treeResult the variable used in NodeQueue
                
                var = nodeQ.getVar();
                treeResult.assignVar(var);
                nv = var.getNumStates();
                newSize = maxSize / nv;
                
                // Now, for each one of the values for the variable
                // go on with the construction of the tree
                
                for (j=0 ; j<nv ; j++) {
                    
                    // Store in treeSource2 the original values, but restricted
                    // to the values of this branch
                    
                    treeSource2 = treeSource.restrict(var,j);
                    
                    // The new tree will be built from the branch related
                    // to the considered value
                    
                    treeNew = treeResult.getChild(j);
                    
                    // If the original tree, restricted to the values for
                    // this branch is not a leaf (i.e. there are more
                    // variable to deal with), insert new NodeQueues for
                    // them
                    
                    if (treeSource2.getLabel()!=2) {
                        
                        // Create new NodeQueue objects to measure the
                        // influence of the remaining variables
                        
                        nodeQ = new NodeQueue(treeNew,treeSource2,newSize,true);
                        queue.insert(nodeQ);
                    }
                    else {
                        // Reached the leafs of the tree. Assign the value
                        
                        treeNew.assignProb(treeSource2.getProb());
                        pot.size++;
                    }
                }
            }
        }
        else {
            // Reached the leafs of the tree. Assign the value
            
            treeNew.assignProb(values.getProb());
            pot.size++;
        }
        
        
        
        return pot;
    }
    
    /**
     * Gives a vector with the relative importance of the variables
     * This method will be applied to utility potentials
     * @param <code>String</code> the variable name which owns the potential
     *                            to analize
     * @return<code>Vector</code> with the measure of relevance for the
     *                            variables
     */
    
    public Vector measureRelevance(String varName){
        Vector relevance=new Vector();
        ProbabilityTree probTree;
        FiniteStates y;
        NodeList list;
        String varRelevance=null;
        long maxSize;
        double measure,globalRelevance,actual,maxRelevance=-1;
        int i;
        
        // Get the cardinal of cartesian product of all the values
        // for the variables
        
        maxSize = (long)FiniteStates.getSize(variables);
        
        // Retrieve the original values and store it in probTree
        // that will be used to measure the information of every
        // variable
        
        probTree = values;
        
        // Now go over the list of variables and calculates its
        // relevance
        
        list=probTree.getVarList();
        
        // Set the name of the main variable
        
        relevance.addElement((Object)("Analizing variable "+varName));
        
        // Measure on every variable
        
        for(i=0; i < list.size(); i++){
            y=(FiniteStates)list.elementAt(i);
            
            // Measure this variable if it is not which owns the potential
            
            if (!varName.equals(y.getName())){
                measure=probTree.informationUtility(y,maxSize);
                
                if(maxRelevance == -1){
                    maxRelevance=measure;
                }
                else{
                    if(measure > maxRelevance){
                        maxRelevance=measure;
                    }
                }
                relevance.addElement(new Double(measure));
            }
        }
        
        // Once obtained the vector relevance, normaliza its results
        // Begin with index 1; firs position with the name of the
        // main variable
        
        for(i=1,globalRelevance=0; i < relevance.size(); i++){
            actual=((Double)relevance.elementAt(i)).doubleValue();
            actual=maxRelevance-actual;
            relevance.setElementAt(new Double(actual),i);
            
            // Increment the global size
            
            globalRelevance+=((Double)(relevance.elementAt(i))).doubleValue();
        }
        
        // Now compute the percentage
        
        for(i=1; i < relevance.size(); i++){
            measure=(((Double)(relevance.elementAt(i))).doubleValue()/globalRelevance)*100;
            relevance.setElementAt(new Double(measure),i);
        }
        
        // Now, store the values
        
        for(i=0; i < list.size(); i++){
            
            if(i==8)
                System.out.println("");
            
            y=(FiniteStates)list.elementAt(i);
            if (!varName.equals(y.getName())){
                varRelevance="Relevance for: "+y.getName()+" ";
                varRelevance=varRelevance+((Double)(relevance.elementAt(i+1))).toString();
                relevance.setElementAt((Object)varRelevance,i+1);
            }
        }
        
        // Return the vector with the measures of relevance
        
        return(relevance);
    }
    
    /**
     * Sorts the variables in the tree, according to an information
     * criterion referred to utility and prune the values with lower
     * utility (less than a value passed as argument)
     * @param <code>double</code> threshold to prune
     * @param <code>double</code> minimum value for utility function
     * @param <code>double</code> maximum value for utility function
     * @return a new <code>PotentialTree</code> with the variables sorted.
     */
    
    public PotentialTree sortUtilityAndPrune(double minimum, double maximum,
    double limit) {
        PotentialTree potTree;
        
        // First at all, sort the variables. This will be done on potentials
        // defined on more than one variable
        if(variables.size() > 1){
            potTree=sortUtility();
        }
        else{
            potTree=this;
        }
        
        // Now, prune it
        potTree.limitBoundUtility(minimum,maximum,limit);
                
        // Return potTree
        return(potTree);
    }
    
    /**
     * Sorts the variables in the tree, according to an information
     * criterion referred to utility and prune the values with lower
     * utility. It not necessary to indicate the maximum and the minimum of the tree
     * @param <code>double</code> threshold to prune
     * @return a new <code>PotentialTree</code> with the variables sorted.
     */
    
    public PotentialTree sortUtilityAndPrune(double limit) {
        PotentialTree potTree;
        
        double minimun = this.getTree().minimumValue();
        double maximun = this.getTree().maximumValue();

        potTree = this.sortUtilityAndPrune(minimun, maximun, limit);
        
        // Return potTree
        return(potTree);
    }
    
    
    
    
    
    /**
     * Method to prune the lower values of a potential, given a
     * threshold
     * @param <code>double</code> theshold to prune
     */
    
    public void pruneLowValues(double limit){
        long maxSize;
        long [] numberDeleted;
        double threshold;
        
        // Space to store the number of leafs to delete
        
        numberDeleted = new long[1];
        
        // Maximun size of the tree
        
        maxSize = (long)FiniteStates.getSize(variables);
        
        // Prune if the tree is not just a probability value
        
        if (!values.isProbab()) {
            values.pruneLowValues(limit,maxSize,numberDeleted);
        }
        
        size -= numberDeleted[0];
    }
    
    /**
     * Bounds the tree associated with the potential by removing
     * nodes whose information value is lower than a given threshold.
     * THE TREE IS MODIFIED.
     * @param limit the information limit.
     * @see ProbabilityTree.prune()
     */
    
    public void limitBound(double limit) {
        
        long maxSize;
        long [] numberDeleted;
        boolean bounded = false;
        double globalSum;
        
        numberDeleted = new long[1]; // Number of deleted nodes.
        
        maxSize = (long)FiniteStates.getSize(variables);
        
        globalSum = values.sum(maxSize);
        
        // If the tree is not just a probability value:
        if (!values.isProbab())
            bounded = values.prune(limit,maxSize,globalSum,numberDeleted);
        
        size -= numberDeleted[0];
    }
    
    
    /**
     * Bounds the tree associated with the potential by removing
     * nodes whose information value is lower than a given threshold
     * or whose addition is lower than a given value.
     * THE TREE IS MODIFIED.
     * @param limit the information limit.
     * @param limitSum the limit sum for pruning.
     * @see ProbabilityTree.prune()
     */
    
    public void limitBound(double limit, double limitSum) {
        limitBound(ProbabilityTree.AVERAGE_APPROX,limit,limitSum);
    }
    
    /**
     * Bounds the tree associated with the potential by removing
     * nodes whose information value is lower than a given threshold
     * or whose addition is lower than a given value.
     * THE TREE IS MODIFIED.
     * @param kindOfAppr the method used to approximate several leaves
     * with a double value (AVERAGE_APPROX, ZERO_APPROX, ...)
     * @param limit the information limit.
     * @param limitSum the limit sum for pruning.
     * @see ProbabilityTree.prune()
     */
    
    public void limitBound(int kindOfAppr,double limit, double limitSum) {
        
        long maxSize;
        long [] numberDeleted;
        boolean bounded = false;
        double [] globalSum;
        
        numberDeleted = new long[1]; // Number of deleted nodes.
        globalSum = new double[1];
        
        maxSize = (long)FiniteStates.getSize(variables);
        
        globalSum[0] = values.sum(maxSize);
        
        //System.out.println("VOY A APROXIMAR:");
        //print();
        // If the tree is not just a probability value:
        if (!values.isProbab())
            bounded = values.prune(kindOfAppr,limit,limitSum,maxSize,globalSum,numberDeleted);
        
        size -= numberDeleted[0];
    }
    
    
    /**
     * Bounds the tree associated with the potential removing leaves
     * until only <code>limit</code> leaves remain. The leaves kept are
     * those with higher probability.
     * THE TREE IS MODIFIED.
     * @param limit the number of leaves to keep.
     */
    
    public void limitBound(int limit) {
        
        long maxSize;
        long [] numberDeleted;
        boolean bounded = false;
        double threshold;
        
        numberDeleted = new long[1]; // Number of deleted nodes.
        
        maxSize = (long)FiniteStates.getSize(variables);
        
        // If the tree is not just a probability value:
        if (!values.isProbab()) {
            threshold = values.findLimitValue(limit);
            System.out.println("LIMITE: "+threshold);
            bounded = values.pruneLowValues(threshold,maxSize,numberDeleted);
        }
        
        size -= numberDeleted[0];
    }
    
    /**
     * Bounds the tree associated with an utility potential
     * Criteria: the leafs must be closed, with a measure given
     * by limit
     * THE TREE IS MODIFIED.
     * @param limit the threshold for prunning
     * @param minimum the minimum value for utility function
     * @param maximum the maximum value for utility function
     * @see ProbabilityTree.prune()
     */
    
    public void limitBoundUtility(double minimum, double maximum, double limit) {
        
        long maxSize;
        long [] numberDeleted;
        boolean bounded = false;
        double globalSum,globalMax,globalMin;
        
        numberDeleted = new long[1]; // Number of deleted nodes.
        
        // Compute max and min values of the whole tree
        
        globalMax=values.maximumValue();
        globalMin=values.minimumValue();
        
        // If the tree is not just a probability value:
        if (!values.isProbab()) {
            bounded = values.pruneUtility_Euclidean(limit,minimum,maximum,numberDeleted);
            updateSize();
        }
        //size -= numberDeleted[0];
    }
    
    /**
     * Updates the size of the potential.
     */
    
    public void updateSize() {
        
        values.updateSize();
        size = values.getSize();
    }
    
    
    /**
     * Checks whether the <code>size</code> of this <code>PotentialTree</code>
     * is equal to the number of <code>leaves</code> of the
     * <code>ProbabilityTree</code> contained in <code>values</code>.
     * @return <code>true> if the <code>size</code> is consistent;
     * <code>fase</code> otherwise.
     */
    
    public boolean checkSize() {
        
        if (!values.checkSize())
            return false;
        
        if (size != values.getSize())
            return false;
        
        return true;
    }
    
    
    /**
     * Copies this potential.
     * @return a copy of this <code>PotentialTree</code>.
     */
    
    public Potential copy() {
        
        PotentialTree pot;
        
        pot = new PotentialTree(variables);
        pot.size = size;
        pot.values = values.copy();
        
        return pot;
    }
    
    
    /**
     * Normalizes this potential to sum up to one.
     */
    
    public void normalize() {
        long totalSize;
        
        totalSize = (long)FiniteStates.getSize(variables);
        values.normalize(totalSize);
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
     * @param ev a <code>Configuration</code> representing the evidence.
     */
    
    public void instantiateEvidence(Configuration evid) {
        
        ProbabilityTree tree, twig;
        Configuration conf;
        PotentialTree pot, pot2;
        FiniteStates variable;
        int i, j, v;
        
        conf = new Configuration(evid,new NodeList(variables));
        
        if (conf.size() != 0) {
            pot = (PotentialTree)copy();
            
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
                pot2 = new PotentialTree();
                pot2.variables.addElement(variable);
                pot2.setTree(tree);
                // combination
                pot = (PotentialTree)pot.combine(pot2);
            }
            this.setTree(pot.getTree());
        }
    }
    
    
    /**
     * Gets the name of the class.
     * @return a String with the name of the class.
     */
    
    public String getClassName() {
        
        return new String("PotentialTree");
    }
    
    
    
    /**
     * Computes the cross entropy of this potential.
     * We assume that the last n-2 variables of
     * the potential are the condicional subset.
     * @return the entropy.
     */
    
    public double crossEntropyPotential() {
        
        Configuration auxConf,confyz,confxz,confz;
        PotentialTree pyz,pz,pxz;
        Vector aux,varsyz,varsxz,varsz;
        FiniteStates temp;
        int i, nv;
        double sum, valxyz,valyz,valxz,valz,valxZ,valyZ,valxyZ;
        
        // Size of the restricted potential.
        nv = (int)FiniteStates.getSize(variables);
        
        
        // Configuration preserving the values in variables.
        auxConf = new Configuration(variables);
        
        // Compute subsets of variables
        varsyz = (Vector)variables.clone();
        varsyz.removeElementAt(0); // remove variable x
        varsxz = (Vector)variables.clone();
        varsxz.removeElementAt(1); // remove variable y
        varsz = (Vector)varsxz.clone();
        varsz.removeElementAt(0); // remove variables x and y
        
        // Compute the marginals Potentials Trees
        
        
        pyz = (PotentialTree)addVariable((FiniteStates)variables.elementAt(0));
        pxz = (PotentialTree)addVariable((FiniteStates)variables.elementAt(1));
        
        
        if(varsz.size() > 0){
            
            pz = (PotentialTree)addVariable((FiniteStates)variables.elementAt(0));
            pz = (PotentialTree)pz.addVariable((FiniteStates)variables.elementAt(1));
        }else{
            pz = new PotentialTree();
        }
        
        sum = 0.0;
        for(i=0 ; i < nv ; i++){
            valxyz = getValue(auxConf);
            if (valxyz > 0.0){
                confyz = new Configuration(varsyz,auxConf);
                confxz = new Configuration(varsxz,auxConf);
                if(varsz.size() > 0){
                    confz = new Configuration(varsz,auxConf);
                }else{
                    confz = new Configuration();
                }
                valyz = pyz.getValue(confyz);
                valxz = pxz.getValue(confxz);
                if(varsz.size() > 0){
                    valz = pz.getValue(confz);
                }else{
                    valz=1.0;
                }
                
                sum = sum +(valxyz * Math.log((valxyz*valz)/(valxz*valyz)));
            }
            auxConf.nextConfiguration();
        }
        
        return(sum);
        
    }
    
    
 /**
 * Factorises a potential tree by a given variable when all
 * the subtrees of that variable are proportional or almost
 * proportional (given an error threshold).
 *
 * @param varFac the variable from which the factorisation
 * will be considered.
 * @param factParam values for factorisation parameters.
 * @return a vector of PotentialTrees with all the factor-trees (without the variable),
 *     and the last element is the original tree factorised (with the variable).
 */

public Vector factoriseOnlyPT(FiniteStates varFac, FactorisationTools factParam) {

  ProbabilityTree pTree;
  Configuration conf;
  Configuration confFixedVbles;
  FiniteStates varR;
  Vector listofRoots=new Vector();
  Vector listofPT= new Vector();
  int i,j, numFac = 0;
  
  pTree = getTree().copy();  // tree of this potential
   
  if(pTree.isProbab()) 
      return listofPT;
  
  listofRoots.addElement(pTree); // the main tree in the first position
  
  varR =  pTree.getVar();
  
  confFixedVbles = new Configuration();
  confFixedVbles.insert(varFac,0); // the variable is in the first position
  
  // conf = facVle + confFixedVbles + restofVblesofthePotential 
  conf = new Configuration(variables,confFixedVbles);
 
  factParam.setActualLevel(-1); //Reset the value of the actual level
  factParam.setMaxLevelD(getVariables().size()); //set the max level that can be reached
  
  //System.out.print(" nivel max= " +factParam.getMaxLevelD()+" ");
  if (varR == varFac) // the variable is in root node
   
      numFac = pTree.getUniqueFactor(conf,confFixedVbles,factParam,listofRoots,0);    
  
  else  // for every branch, look for the variable and factorise 
      
      pTree.findVarAndFactorise(conf,confFixedVbles,factParam,listofRoots);  
 
  
  if(listofRoots.size()>1) // the main tree has been factorised, so
      pTree.updateSize();  // update the number of leaves
  else
      return(listofPT);
  
  
  // Return a vector of potential trees:
   
  PotentialTree pt; // Insert the potential trees without the variable
  for(j=0; j<listofRoots.size()-1 ; j++){ 
     pt= new PotentialTree( (ProbabilityTree)listofRoots.elementAt(j), getVariables() ); 
     i= pt.getVariables().indexOf(varFac);
     (pt.getVariables()).removeElementAt(i); //remove the variable from the list
     listofPT.addElement(pt); 
  }
    // Insert the potential tree containing the variable (the last one)
  pt=new PotentialTree((ProbabilityTree)listofRoots.lastElement(),getVariables());
  listofPT.addElement(pt); 
  
  return listofPT;
} 
    
 
/**
 * This method splits the potential tree that receives the call into
 * two potential trees, one of which contain variable <code>varSplit</code>
 * and not the other. The two new potentials are returned.
 *
 * @param varSplit the variable with respect to which the potential will be
 * split.
 * @return a vector with the potential trees into which the potential is split.
 */
    
public Vector split(FiniteStates varSplit) {
        
  ProbabilityTree thisTree, freeTerm, coreTerm;
  Configuration path, aux;
  boolean found = false, appears = true;
  Vector factors = new Vector();
  Vector listofPT =new Vector();
  Vector listOfPaths = new Vector();
  int last, i;
        
  thisTree = this.getTree();
        //System.out.println("Entro en split"); thisTree.print(10);
        
  path = new Configuration();
  found = thisTree.getPathToVariable(varSplit,path);
  path = path.reverse();
  //path.pPrint(); System.out.println("Variable "+varSplit.getName());
        
  listOfPaths.addElement(path);
     
  last = path.size();
  if (found && (last > 1)) {
     // The variable has been found and is not located at the root.
                //System.out.println("Encuentro la variable");
     freeTerm = thisTree.copy();
            
     // freeTerm will not contain the variable.
     freeTerm.setValue(path,1.0);//System.out.println("FreeT"); freeTerm.print(10);
            
     // Now search for more occurrences of the variable
     while (appears) {
           aux = new Configuration();
           appears = freeTerm.getPathToVariable(varSplit, aux);
           aux.pPrint();
           aux = aux.reverse();
           aux.pPrint();
                
           if (appears && (aux.size() > 0)) {
              listOfPaths.addElement(aux);
              freeTerm.setValue(aux,1.0);                    
           }
     }
            
     coreTerm = new ProbabilityTree(1.0);
            
     for (i=0 ; i<listOfPaths.size() ; i++) {
         aux = (Configuration)listOfPaths.elementAt(i);
                  /*System.out.println("\nConfiguracion i="+i);
                    aux.pPrint();
                    System.out.println("A ver este");
                    thisTree.restrict(aux).print(10);*/
         coreTerm.setSubTree(aux, thisTree.restrict(aux));
      }
                //System.out.println("\nCore"); coreTerm.print(10);
                //System.out.println("\nFree"); freeTerm.print(10);
                
      factors.addElement(freeTerm);    
      factors.addElement(coreTerm);
      
      // *** Converts to Potential Trees ****//
      
       // Insert the potential tree without the variable  
      PotentialTree pt= new PotentialTree( freeTerm, getVariables() ); 
      i= pt.getVariables().indexOf(varSplit);
      (pt.getVariables()).removeElementAt(i);
      listofPT.addElement(pt); 
  
      // Insert the potential tree containing the variable 
      listofPT.addElement(new PotentialTree(coreTerm,getVariables()));
            
  }//found 
        
        
   return listofPT; // factors; // -> ( ProbabilityTrees)
}
    
    
 /**
 * Splits a potential tree with respect to a given variable.
 * Returns two potential trees: 
 * the first one (free tree) without the variable, and the second (core tree) 
 * containing it;
 *
 * @param varFac the variable from which the split will be considered.
 * @param factParam values of  factorisation parameters.
 * @return a vector with 2 PotentialTrees: the free tree (in the first position), 
 *         and the core tree. 
 *@return an empty vector if the variable�s not found, or it's in the root node.
 *              
 */

public Vector splitOnlyPT(FiniteStates varFac, FactorisationTools factParam) {

  ProbabilityTree pTree,freeTree,coreTree;
  PotentialTree pt;
  Configuration confFixedVbles;
  FiniteStates varR;
  Vector listofPT,listConf;  
  int i;
  boolean allUnit = true;
  boolean onlySpl= factParam.isOnlySplit();
  
  pTree = getTree().copy();  // tree of this potential
   
  listofPT = new Vector();    
  varR =  pTree.getVar();
  
  if(pTree.isProbab() || (varR == varFac))  
     return listofPT;
   
  confFixedVbles = new Configuration();
  confFixedVbles.insert(varFac,0); // the variable is in the first position
   
  listConf= new Vector();
  
  coreTree= new ProbabilityTree(); // tree containing the variable 

  //factParam.setActualLevel(-1); factParam.setMaxLevelD(getVariables().size());
  
  freeTree = pTree.findVarAndSplit(confFixedVbles,listConf,onlySpl,coreTree);
 
  if(confFixedVbles.getValue(0)==0) //Variable not found    
     return(listofPT);
           
    // Exit if the free tree has all the children equal 1 .  
  if(!freeTree.isUnitNode())
    for (i=0; allUnit && i< freeTree.getVar().getNumStates(); i++)
          if ( !freeTree.getChild(i).isUnitNode() ) 
              allUnit=false;
                          
  if(allUnit)
      return(listofPT);
  
    
  factParam.incNumSplit(); // Increment the counter with number of splits
  
  // *** Converts to Potential Trees ****//
  
    // Insert the potential tree without the variable  
  pt= new PotentialTree( freeTree, getVariables() ); 
  i= pt.getVariables().indexOf(varFac);
  (pt.getVariables()).removeElementAt(i);
  listofPT.addElement(pt); 
  
    // Insert the potential tree containing the variable 
  listofPT.addElement(new PotentialTree(coreTree,getVariables()));    
     
  return listofPT;
  
} 

/**
 * Splits and factorises a potential tree with respect to a given variable.
 * First splits the tree and returns two trees: 
 * the first one without the variable, and the second containing it;
 * If the variable is not found, returns an empty vector.
 * If the variable is in root node, only factorise.
 *
 * @param varFac the variable from which the split and the factorisation
 * will be considered.
 * @param factParam values of  factorisation parameters.
 * @return a vector of PotentialTrees with: the free tree (in the first position), 
 *         all the factor-trees found, and a copy of the original tree (core) 
 *                      split and factorised (the last element). 
 *@return an empty vector if the variable�s not found, or any factorisation have
 *                      been made.
 *              
 */


public Vector splitAndFactorisePT(FiniteStates varFac, FactorisationTools factParam){
     
  
  ProbabilityTree pTree,freeTree,tree,coreTree;
  Configuration conf;
  Configuration confFixedVbles;
  FiniteStates varR;
  Vector listofRoots,listConf,listofPT;
  int i,j, ind, numOcu, numFac = 0;
  boolean allUnit = true;
  boolean onlySplit= factParam.isOnlySplit();
  
  pTree = getTree().copy();  // tree of this potential

  freeTree= new ProbabilityTree();
  
  listofRoots = new Vector();
  listofPT = new Vector(); 
    
  if(pTree.isProbab()){
     return listofPT;
  }
  
  varR =  pTree.getVar();
  
  confFixedVbles = new Configuration();
  confFixedVbles.insert(varFac,0); // the variable is in the first position
  
  // conf = facVle + confFixedVbles + restofVblesofthePotential 
  conf = new Configuration(variables,confFixedVbles);
  
  if (varR == varFac) {  // the variable is in root node: only factorise
    
      listofRoots.addElement(pTree); // the main tree in the first position         
      numFac = pTree.getUniqueFactor(conf,confFixedVbles,factParam,listofRoots,0);        
      if(numFac>0) 
          pTree.updateSize(); //update the number of leaves
      else 
          return(listofPT); //returns an empty vector if it hasn't been factorised
  }
  
  else { // Search for the variable split and factorise (if it found)
      
    // *** SPLIT *** //
  
    listConf= new Vector();
    coreTree= new ProbabilityTree();
  
    freeTree=pTree.findVarAndSplit(confFixedVbles,listConf,onlySplit,coreTree);

    if(confFixedVbles.getValue(0)==0){ //Variable not found
        return(listofPT);
    }
               
    // discard the free tree if all the children are 1 . 
    if(!freeTree.isUnitNode())
    for (i=0; allUnit && i< freeTree.getVar().getNumStates(); i++)
          if ( !freeTree.getChild(i).isUnitNode() ) 
          {
              allUnit=false;
              listofRoots.addElement(freeTree);
          }   
    
    if(allUnit)
      return(listofPT);
    
    listofRoots.addElement(coreTree);  // Insert the tree containing the variable  
    
    factParam.incNumSplit(); // Increment the counter with number of splits
    
      // *** FACTORISE *** //

    Configuration cfx;
    numOcu= listConf.size();
    //factParam.setActualLevel(-1); //Reset the value of the actual level
    factParam.setMaxLevelD(getVariables().size()); //max level that can be reached
          
    for(i=0; i<numOcu; i++){
        cfx= (Configuration)listConf.elementAt(i); //cfx.pPrint();  
        
        if(cfx.size()<= factParam.getMaxLevelD()){ 
            //check the max level allowed, and factorise if the variable is in
            // a level less than the maximun
           tree=coreTree; //get the "subtree" with the variable
           for(j=1; j<cfx.size()-1; j++){ 
               ind= cfx.getValue(j);
               tree= tree.getChild(ind);
           }
           ind= cfx.getValue(cfx.size()-1); //tree.print(10);
         
           numFac += tree.getUniqueFactor(conf,cfx,factParam,listofRoots,ind);
        }     
    }//for
    
    if(numFac>0) coreTree.updateSize(); //update the number of leaves
   
  } 
  
  
  // *** Converts to Potential Trees ****//
  
  PotentialTree pt;
  for (j=0;j<listofRoots.size()-1;j++) {
      pt= new PotentialTree( (ProbabilityTree)listofRoots.elementAt(j),getVariables() ); 
      i= pt.getVariables().indexOf(varFac);
      (pt.getVariables()).removeElementAt(i);
      listofPT.addElement(pt); 
  }   
  
    // Insert the last potential tree containing the variable 
  pt= new PotentialTree((ProbabilityTree)listofRoots.lastElement(),getVariables());
  listofPT.addElement(pt);  
   
  return listofPT;
  
} 

    
    /**
     * Method to repair this distribution to be a probability distribution
     * @param <code>NodeList</code> nodes conditioning the distribution
     * @return <code>PotentialTree</code> Potential already normalized
     */
    
    public void repair(NodeList nodes){
        Configuration conf;
        double size=nodes.getSize();
        int valuesToConsider;
        double sum;
        double lost;
        double add;
        double i;
        
        // First at all, do a configuration with all variables
        // in nodes
        
        conf=new Configuration(nodes);
        
        // Calculate the number of possible values for this configuration
        
        for(i=0; i < size; i++){
            // Now, go on the possible values for this configuration
            
            sum=sumConsistent(conf);
            
            // We need to change the values for this values to be a distribution
            // Get the numbers different to 0
            
            valuesToConsider=nonCeroValues(conf);
            
            // Compute the lost probability (due to 0s)
            
            lost=1.0-sum;
            
            if (lost > 0.0) {
                // This probability will be assigned to the rest of values
                
                add=lost/valuesToConsider;
                
                // Finally this value must be added to non cero values consistent
                // with the given configuration
                
                addValue(conf,add);
            }
            
            // Go on with the next configuration
            
            conf.nextConfiguration();
        }
    }
    
    /**
     * Converts a <code>Potential</code> to a <code>PotentialTree</code>.
     * @param pot the <code>Potential</code> to convert.
     * @returns a new <code>PotentialTree</code> resulting from converting
     * <code>Potential pot</code>
     */
    
    public static PotentialTree convertToPotentialTree(Potential pot) {
        
        PotentialTree newPot;
        
        if (pot.getClass().getName().equals("elvira.potential.PotentialTable")) {
            newPot = ((PotentialTable)pot).toTree();
        }
        else if (pot.getClass().getName().equals("elvira.potential.PotentialTree")) {
            newPot = (PotentialTree)(pot.copy());
        }
        else if (pot.getClassName().equals("CanonicalPotential")) {
            newPot = ((CanonicalPotential) pot).getCPT().toTree();
        }
        else
            newPot = null;
        
        return newPot;
    }
    
    /**
     * Converts the utility potential into a probability potential. Once
     * the potential is converted it can be used the methods for sorting
     * and approximating probability potentials
     */
    public void convertUtilityIntoProbability(){
        // Add a new variable to the variables in the potential. This new
        // variable will have two states
        FiniteStates newVar=new FiniteStates(2);
        newVar.setName("artificial");
        
        // Get the maximum and minimum values in the potential of utility
        double maximum=values.maximumValue();
        double minimum=values.minimumValue();
        
        // Now determine the constants required for the transformation
        double k1=maximum-minimum;
        double k2=-minimum;
        
        // Call the method to transform the values of utility into
        // probabilities
        values.convertUtilityIntoProbability(newVar,k1,k2);
        
        // La nueva variable se incluye a la lista de variables
        variables.addElement(newVar);
        System.out.println("Tras hacer modificacion en values: ");
        values.print(2);
    }


    /**
     * Factorises a tree as a list of two factors, one containing only the
     * variable given as argument, and the other with the rest.
     *
     * @param x the variable with respect to which the factorisation will be
     * carried out.
     * @return a <code>Vector</code> with the two factors.
     */

    public Vector<PotentialTree> factoriseRT(FiniteStates x) {

        Vector<PotentialTree> factors = new Vector<PotentialTree>();
        Vector vars;
        Configuration conf, confX;
        PotentialTree factorX, factorY;
        double[] betas = new double[x.getNumStates()];
        double alpha0, alpha;
        int i;


        vars = this.getVariables();

        conf = new Configuration(vars);

        conf.remove(x);

        factorX = (PotentialTree)this.restrictVariable(conf);

        // factorX must be a tree containing only variable X.

        confX = new Configuration();
        confX.putValue(x, 0);
        alpha0 = factorX.getValue(confX);
        betas[0] = 1.0;

        for (i=1 ; i<x.getNumStates() ; i++) {
            confX.putValue(x, i);
            alpha = factorX.getValue(confX);
            betas[i] = alpha / alpha0;
        }

        for (i=0 ; i<x.getNumStates() ; i++) {
            confX.putValue(x, i);
            factorX.setValue(confX, betas[i]);
        }

        confX.putValue(x,0);
        factorY = (PotentialTree)this.restrictVariable(confX);

        factors.addElement(factorX);
        factors.addElement(factorY);

        return (factors);

    }


    /**
     * Computes the factorisation degree of a tree for a given variable.
     *
     * @param x the variable with respect to which the degree will be
     * computed.
     * @return the factorisation degree.
     */

    public double factorisationDegree(FiniteStates x) {

        double d = 0.0;
        PotentialTree t1, t2;
        Vector<PotentialTree> factors;

        factors = this.factoriseRT(x);

        t1 = factors.elementAt(0);
        t2 = factors.elementAt(1);

        //t1.normalize();
        t1.log();
        t2.log();

        d = t1.totalPotential() + t2.totalPotential();

        return d;
    }


    /**
     * Computes an upper bound of the factorisation degree of a tree for a
     * given variable, using Jensen's inequality.
     *
     * @param x the variable with respect to which the degree will be
     * computed.
     * @return the factorisation degree.
     */

    public double factorisationDegree2(FiniteStates x) {

        double d = 0.0;
        PotentialTree t1, t2;
        Vector<PotentialTree> factors;

        factors = this.factoriseRT(x);

        t1 = factors.elementAt(0);
        t2 = factors.elementAt(1);

        //t1.normalize();
        //t1.log();
        //t2.log();

        d = Math.log(t1.totalPotential()) + Math.log(t2.totalPotential());

        return d;
    }


    /**
     * Replaces this potential by its logarithm.
     */
    public void log() {

        values.log();
    }



    /**
     * Generates a random potential tree, with leaves uniformly distributed
     * between 0 and 1.
     *
     * @param v the list of variables of the tree.
     * @return a random tree for variables <code>v</code>
     */

    public static PotentialTree randomTree(NodeList v) {

        PotentialTree rndt;
        int s, i;
        Configuration conf;
        double va;

        s = (int)v.getSize();

        rndt = new PotentialTree(v);

        conf = new Configuration(v);

        for (i=0 ; i<s ; i++) {
            va = Math.random();
            if (va==0.0) va = 0.00001;
            rndt.setValue(conf, va);
            conf.nextConfiguration();
        }

        return rndt;
    }



    /**
     * Computes the Kullback-Leibler divergence between two trees.
     * Both trees must have to be defined over the same set of variables.
     *
     * @return the kl-divergence.
     */

    public double KLDivergence(PotentialTree t, PotentialTree t2) {

        double kl = 0;
        int s, i;
        Configuration conf;
        double va, vb, vc;
        NodeList v;

        //this.print();
        v = new NodeList(this.getVariables());

        s = (int)v.getSize();

        conf = new Configuration(v);
        //v.print();
        //conf.print();

        for (i=0 ; i<s ; i++) {
            va = this.getValue(conf);
            vb = t.getValue(conf);
            vc = t2.getValue(conf);
            if ((va>0.0) && (vb>0.0) && (vc>0)) {
                kl += (va * Math.log(va/(vb*vc)));
            }
            conf.nextConfiguration();
        }

        return kl;
    }


    /**
     * Simulates a sample of size n. The tree must be normalised.
     *
     * @return a vector of configurations.
     */

    public Vector<Configuration> simulateSample(int n) {

        Vector<Configuration> sample = new Vector<Configuration>();
        int i, j, s;
        NodeList v;
        Configuration conf;
        double acum = 0.0, r;

        v = new NodeList(this.getVariables());

        s = (int)v.getSize();

        for (i=0 ; i<n ; i++) {
            conf = new Configuration(v);
            acum = 0.0;
            r = Math.random();
            for (j=0 ; j<s ; j++) {
                acum += this.getValue(conf);
                if (acum >= r)
                    break;
                else conf.nextConfiguration();
            }
            sample.addElement(conf);
        }


        return sample;
    }


    /**
     * Evaluates a set of configurations according to this probability tree,
     * by computing the log-likelihood.
     *
     * @param v a set of configurations given as a vector.
     * @return the computed log-likelihood, i.e., the sum of the evaluations
     * of all the configurations.
     */

    public double evaluateSetOfConfigurations(Vector<Configuration> v) {

        double likelihood = 0;
        int i;
        Configuration conf;

        for (i=0 ; i<v.size() ; i++) {
            conf = v.elementAt(i);
            likelihood += Math.log(this.getValue(conf));
        }

        return likelihood;
    }


    /**
     * Returns the variable with highest factorisation degree.
     */

    FiniteStates bestToFactorise() {

        FiniteStates best = new FiniteStates(), current;
        int i, bestPos = 0;
        double bestDeg, deg;

        best = (FiniteStates)this.getVariables().elementAt(0);
        bestDeg = this.factorisationDegree2(best);

        for (i=1 ; i<this.getVariables().size() ; i++) {
            current = (FiniteStates)this.getVariables().elementAt(i);
            deg = this.factorisationDegree2(current);
            if (deg > bestDeg) {
                bestDeg = deg;
                best = current;
            }
        }

        return best;
    }


//  public static void main(String args[]) {
//
//        PotentialTree t, factor1, factor2, temp;
//        FiniteStates x, x1, x2, x3, x4, x5, x6, x7, x8, x9, x10;
//        NodeList nl;
//        double fd, fd2, kl, llExact, llApprox;
//        int i, j;
//        Vector<PotentialTree> vpt = new Vector<PotentialTree>();
//        Vector<Configuration> sample = new Vector<Configuration>();

        // PRIMER EXPERIMENTO: Generar Ã¡rboles binarios al azar, y factorizar por
        // cada una de las variables, midiendo las medidas de factorizaciÃ³n
        // y la log-verosimilitud.


//        x1 = new FiniteStates(2);
//        x1.setName("X1");
//        x2 = new FiniteStates(2);
//        x2.setName("X2");
//        x3 = new FiniteStates(2);
//        x3.setName("X3");
//        x4 = new FiniteStates(2);
//        x4.setName("X4");
//        x5 = new FiniteStates(2);
//        x5.setName("X5");
//        x6 = new FiniteStates(2);
//        x6.setName("X6");
//        x7 = new FiniteStates(2);
//        x7.setName("X7");
//        x8 = new FiniteStates(2);
//        x8.setName("X8");
//        x9 = new FiniteStates(2);
//        x9.setName("X9");
//        x10 = new FiniteStates(2);
//        x10.setName("X10");
//
//        nl = new NodeList();
//
//        nl.insertNode(x1);
//        nl.insertNode(x2);
//        nl.insertNode(x3);
//        nl.insertNode(x4);
//        nl.insertNode(x5);
//        nl.insertNode(x6);
//        nl.insertNode(x7);
//        nl.insertNode(x8);
//        nl.insertNode(x9);
//        nl.insertNode(x10);

//        System.out.println("FD;FD2;KL;LLExact;LLApprox");
//
//        int nruns = 10, sampleSize = 100;
//
//        for (i = 0; i < nruns; i++) {
//            t = PotentialTree.randomTree(nl);
//            t.normalize();
//
//            sample = t.simulateSample(sampleSize);
//
//            // Factorizamos por cada una de las variables.
//
//            for (j = 0; j < nl.size(); j++) {
//                x = (FiniteStates) nl.elementAt(j);
//                fd = t.factorisationDegree(x);
//                fd2 = t.factorisationDegree2(x);
//                vpt = t.factoriseRT(x);
//                factor1 = vpt.elementAt(0);
//                factor2 = vpt.elementAt(1);
//                kl = t.KLDivergence(factor1, factor2);
//
//                llExact = t.evaluateSetOfConfigurations(sample);
//                llApprox = factor1.evaluateSetOfConfigurations(sample);
//                llApprox += factor2.evaluateSetOfConfigurations(sample);
//                System.out.println(fd + ";" + fd2 + ";" + kl + ";" + llExact + ";" + llApprox);
//            }
//
//        }




        // SEGUNDO EXPERIMENTO: Generar Ã¡rboles ternarios al azar, y factorizar por
        // cada una de las variables, midiendo las medidas de factorizaciÃ³n
        // y la log-verosimilitud.

//        x1 = new FiniteStates(3);
//        x1.setName("X1");
//        x2 = new FiniteStates(3);
//        x2.setName("X2");
//        x3 = new FiniteStates(3);
//        x3.setName("X3");
//        x4 = new FiniteStates(3);
//        x4.setName("X4");
//        x5 = new FiniteStates(3);
//        x5.setName("X5");
//        x6 = new FiniteStates(3);
//        x6.setName("X6");
//        x7 = new FiniteStates(3);
//        x7.setName("X7");
//        x8 = new FiniteStates(3);
//        x8.setName("X8");
//        x9 = new FiniteStates(3);
//        x9.setName("X9");
//        x10 = new FiniteStates(3);
//        x10.setName("X10");
//
//        nl = new NodeList();
//
//        nl.insertNode(x1);
//        nl.insertNode(x2);
//        nl.insertNode(x3);
//        nl.insertNode(x4);
//        nl.insertNode(x5);
//        nl.insertNode(x6);
//        nl.insertNode(x7);
//        nl.insertNode(x8);
//        nl.insertNode(x9);
//        nl.insertNode(x10);
//
//
//        System.out.println("FD;FD2;KL;LLExact;LLApprox");
//
//        int nruns = 10, sampleSize = 100;
//
//        for (i = 0; i < nruns; i++) {
//            t = PotentialTree.randomTree(nl);
//            t.normalize();
//
//            sample = t.simulateSample(sampleSize);
//
//            // Factorizamos por cada una de las variables.
//
//            for (j = 0; j < nl.size(); j++) {
//                x = (FiniteStates) nl.elementAt(j);
//                fd = t.factorisationDegree(x);
//                fd2 = t.factorisationDegree2(x);
//                vpt = t.factoriseRT(x);
//                factor1 = vpt.elementAt(0);
//                factor2 = vpt.elementAt(1);
//                kl = t.KLDivergence(factor1, factor2);
//
//                llExact = t.evaluateSetOfConfigurations(sample);
//                llApprox = factor1.evaluateSetOfConfigurations(sample);
//                llApprox += factor2.evaluateSetOfConfigurations(sample);
//                System.out.println(fd + ";" + fd2 + ";" + kl + ";" + llExact + ";" + llApprox);
//            }
//
//        }




        // TERCER EXPERIMENTO: Generar Ã¡rboles ternarios al azar, y factorizar por
        // la mejor variable. Luego, los factores descomponerlos tambiÃ©n por
        // la mejor variable, y asÃ­ sucesivamente, midiendo verosimilitud
        // en cada paso.
//        x1 = new FiniteStates(2);
//        x1.setName("X1");
//        x2 = new FiniteStates(2);
//        x2.setName("X2");
//        x3 = new FiniteStates(2);
//        x3.setName("X3");
//        x4 = new FiniteStates(2);
//        x4.setName("X4");
//        x5 = new FiniteStates(2);
//        x5.setName("X5");
//        x6 = new FiniteStates(2);
//        x6.setName("X6");
//        x7 = new FiniteStates(2);
//        x7.setName("X7");
//        x8 = new FiniteStates(2);
//        x8.setName("X8");
//        x9 = new FiniteStates(2);
//        x9.setName("X9");
//        x10 = new FiniteStates(2);
//        x10.setName("X10");

//        x1 = new FiniteStates(3);
//        x1.setName("X1");
//        x2 = new FiniteStates(3);
//        x2.setName("X2");
//        x3 = new FiniteStates(3);
//        x3.setName("X3");
//        x4 = new FiniteStates(3);
//        x4.setName("X4");
//        x5 = new FiniteStates(3);
//        x5.setName("X5");
//        x6 = new FiniteStates(3);
//        x6.setName("X6");
//        x7 = new FiniteStates(3);
//        x7.setName("X7");
//        x8 = new FiniteStates(3);
//        x8.setName("X8");
//        x9 = new FiniteStates(3);
//        x9.setName("X9");
//        x10 = new FiniteStates(3);
//        x10.setName("X10");

//        nl = new NodeList();
//
//        nl.insertNode(x1);
//        nl.insertNode(x2);
//        nl.insertNode(x3);
//        nl.insertNode(x4);
//        nl.insertNode(x5);
//        nl.insertNode(x6);
//        nl.insertNode(x7);
//        nl.insertNode(x8);
//        nl.insertNode(x9);
//        nl.insertNode(x10);
//
//
//        System.out.println("NVARS;LLExact;LLApprox");
//
//        int nruns = 1, sampleSize = 100;
//
//
//        for (i = 0; i < nruns; i++) {
//            t = PotentialTree.randomTree(nl);
//            t.normalize();
//
//            sample = t.simulateSample(sampleSize);
//
//            Vector<PotentialTree> list = new Vector<PotentialTree>();
//            list.add(t);
//
//
//            for (j = 0; j < (nl.size()-1) ; j++) {
//                for (int k=(list.size()-1) ; k>=0 ; k--) {
//                    temp = list.elementAt(k);
//                    if (temp.getVariables().size() > 1) {
//                        FiniteStates b = temp.bestToFactorise();
//                        vpt = temp.factoriseRT(b);
//                        list.removeElementAt(k);
//                        list.add(vpt.elementAt(0));
//                        list.add(vpt.elementAt(1));
//                    }
//                }
//
//                llExact = t.evaluateSetOfConfigurations(sample);
//                llApprox = 0;
//
//                // Now we normalise the list
//
//                double tot=0;
//                for (int k=0 ; k<list.size() ; k++) {
//                    temp = list.elementAt(k);
//                    tot += temp.totalPotential();
//                }
//                PotentialTree normTree = new PotentialTree(new ProbabilityTree(1/tot));
//                list.add(normTree);
//
//                for (int k=0 ; k<list.size() ; k++) {
//                    temp = list.elementAt(k);
//                    llApprox += temp.evaluateSetOfConfigurations(sample);
//                }
//                System.out.println(nl.size()-j-1+ ";" + llExact + ";" + llApprox);
//            }
//
//        }
//
//    }
    

} // end of class
