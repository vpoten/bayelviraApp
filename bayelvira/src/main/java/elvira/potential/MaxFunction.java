/*
 * MaxFunction.java
 *
 * Created on 16/11/2002, 17:25
 */

package elvira.potential;

import java.util.Vector;
import elvira.*;


/**
 * MaxFunction
 * This function computes the family of canonical max models. 
 * 
 * @author J. del Sagrado (jsagrado@ual.es)
 * @author A. Salmerón (asalmero@ual.es)
 * Last modified : 12/12/2002 
 */
public class MaxFunction extends Function {
    
    private boolean FACTORIZED;
    private Vector tF; // tree factorisation
    private boolean COMPUTED;
    private PotentialTree poT;
    
    /** Creates a new instance of MaxFunction */
    public MaxFunction() {
        FACTORIZED = false;
        tF = new Vector();
        COMPUTED = false;
        poT = new PotentialTree();
        name = new String("MaxFamily");
        tp = 10;      
    }
    
    public MaxFunction(Vector arg){
        tF = new Vector();
        COMPUTED = false;
        poT = new PotentialTree();
        name = new String("MaxFamily");
        tp = 10;
        MaxPotential(arg, false);
    }
    
    public MaxFunction(Vector arg, boolean full){
        tF = new Vector();
        COMPUTED = false;
        poT = new PotentialTree();
        name = new String("MaxFamily");
        tp = 10;
        MaxPotential(arg, full);
    }
      
    /** 
     * Obtains a <code>PotentialTree</code> representation for the canonical
     * deterministic max function using the set of arguments (arg).
     * @param arg a <code>Vector</code> with the arguments of the function.
     * @return the representation of a deterministic max function as a 
     * <code>ListPotential</code>.
     */  
    private Potential deterministicMax (Vector arg){
        int i,j,val,max;
        long size;
        double tcval;     // tree configuration value
        Potential aux;
        PotentialTree ft; // tree for a deterministic max function
        Vector v;         // variables for the potential tree
        Vector cval;      // configuration values
        Configuration c;

                
        v = new Vector();
        for (i=0; i<arg.size(); i++){
            aux = (Potential)arg.elementAt(i);
            v.addElement((Potential)aux.getVariables().elementAt(0));
        }

        size=(long)FiniteStates.getSize(v);
        ft = new PotentialTree(v);
        c = new Configuration(v);
        for (i=0; i<size ; i++){
           cval = c.getValues();
           max = 0;
           for (j=1; j<cval.size(); j++) {
               val =((Integer)cval.elementAt(j)).intValue();
               if (max < val)
                   max = val;
           }
           if (max==0) { 
              tcval = ((Potential)arg.elementAt(0)).getValue(c);
           } else {
               if (max==((Integer)cval.elementAt(0)).intValue())
                   tcval = 1.0;
               else tcval = 0.0;
           }
           ft.setValue(c, tcval);
           c.nextConfiguration();
        }
        
        return ft;
    }
    
    /**
     * Search for the maximum value in a given configuration.
     * Returns the maximum value and the first position where it was found.
     * It is assumed that the values of the variables in the configuration
     * are disposed in a descending order, i.e.: first the most significants.
     */  
    private int[] max(Configuration conf){
        int i,k;
        int pos;
        int valPos[] = new int[2];     // max value and position
        Vector aux = conf.getValues();
        
        pos = 0;
        valPos[0] = ((Integer)aux.elementAt(pos)).intValue();
        valPos[1] = pos;
        for (i=1; i<aux.size(); i++){
            k = ((Integer)aux.elementAt(i)).intValue();
            if (valPos[0]>k){
                valPos[0] = k;
                valPos[1] = i;
            }       
        }
        
        return valPos;
    }
    
    /**
     *  Builds a <code>Potential Tree</code> representing a deterministic max
     *  model. It is assumed that the variables' values are ordered from greater
     *  to lesser significance, ie.: present, ausent. 
     */
     private Potential deterministicMax (Potential leakyPot, NodeList hiddenVars) {
        int j,k;
        long i;
        long size;         // tamaño
        long nroConf;      // number of configurations
        int mvp[];         // max value of the configuration and position
        double val;
        PotentialTree pt;
        Configuration confHidden;
        Configuration confAux;
        Configuration conf;
        NodeList confVars;
        NodeList presentVars;
        FiniteStates efect;
        boolean prune = false;
        
        // deterministic Max as potential tree
        efect = (FiniteStates)leakyPot.getVariables().elementAt(0);
        confVars = hiddenVars.copy();
        confVars.insertNode(efect);
        pt = new PotentialTree(confVars);
        
        conf = new Configuration(confVars);
        confHidden = new Configuration(conf, hiddenVars);
        size = (long)FiniteStates.getSize(hiddenVars.toVector());
               
        for(i=0; i<size; ){
           // Creates and stores a configuration in the tree
           mvp = max(confHidden);
           if ((mvp[0] == 0) && (mvp[1] != hiddenVars.size()-1)) { // Prune
               prune = true;
               presentVars = new NodeList();
               for (j=0; j<mvp[1]+1; j++)
                   presentVars.insertNode((FiniteStates)hiddenVars.elementAt(j));
               confAux = new Configuration(confHidden, presentVars);
           } else {
               confAux = new Configuration(confHidden, hiddenVars);
           }
           confAux.putValue(efect,0);
        
           // Stores a configuration in the tree
           for (j=0; j<efect.getNumStates(); j++) {
               if (mvp[0] == confAux.getValue(efect))
                     pt.setValue(confAux, 1.0);
               else pt.setValue(confAux, 0.0);
               confAux.nextConfiguration();
           }
           
           // Search for next configuration
           if (prune) {
               prune = false;
               // Jump some comfigurations
               nroConf = 1;
               for (j=mvp[1]+1; j<hiddenVars.size(); j++)
                   nroConf *= ((FiniteStates)hiddenVars.elementAt(j)).getNumStates();
               for (j=0; j<nroConf; j++, i++)
                   confHidden.nextConfiguration();
           } else {
               // Go to next configuration
               i++;
               confHidden.nextConfiguration();
           }
        }
                
        // tree update in order to incorporate leaky probabilities
        confHidden = new Configuration (conf, hiddenVars);
        for (j=0; j<confHidden.getVariables().size(); j++)
            confHidden.putValue (confHidden.getVariable(j), confHidden.getVariable(j).getNumStates()-1);
        confHidden.putValue(efect,0);
        confAux = new Configuration (conf, hiddenVars.toVector());
        
        for (j=0; j<efect.getNumStates(); j++) {
            pt.setValue(confHidden, leakyPot.getValue(confAux));
            confHidden.nextConfiguration();
            confAux.nextConfiguration();
        }
                      
        return pt;
    }
     
    /**
     *  Builds a <bf>full</bf> <code>Potential Tree</code> representing a deterministic max
     *  model. It is assumed that the variables' values are ordered from greater
     *  to lesser significance, ie.: present, ausent. 
     */
     private Potential fullDeterministicMax (Potential leakyPot, NodeList hiddenVars) {
        int j,k;
        long i;
        long size;         
        long nroConf;      // number of configurations
        int mvp[];         // value of the maximum of the configuration and position
        double val;
        PotentialTree pt;
        Configuration confHidden;
        Configuration confAux;
        Configuration conf;
        NodeList confVars;
        NodeList presentVars;
        FiniteStates efect;
        boolean prune = false;
        
        // deterministic Max as potential tree
        efect = (FiniteStates)leakyPot.getVariables().elementAt(0);
        confVars = hiddenVars.copy();
        confVars.insertNode(efect);
        pt = new PotentialTree(confVars);
        
        conf = new Configuration(confVars);
        confHidden = new Configuration(conf, hiddenVars);
        size = (long)FiniteStates.getSize(hiddenVars.toVector());
               
        for(i=0; i<size; i++){
           // Create a configuration to store in the tree 
           mvp = max(confHidden);
        
           // Stores a configuration in the tree
           for (j=0; j<efect.getNumStates(); j++) {
               if (mvp[0] == conf.getValue(efect))
                     pt.setValue(conf, 1.0);
               else pt.setValue(conf, 0.0);
               conf.nextConfiguration();
           }
           
           confHidden = new Configuration(conf, hiddenVars);
        }
                
        // update the tree in order to incorporate the leaky probabilities
        conf = new Configuration(confVars);
        for (j=0; j<conf.size()-1; j++)
            conf.putValue (conf.getVariable(j), conf.getVariable(j).getNumStates()-1);
        confAux = new Configuration (conf, hiddenVars.toVector());
        
        for (j=0; j<efect.getNumStates(); j++) {
            pt.setValue(conf, leakyPot.getValue(confAux));
            conf.nextConfiguration();
            confAux.nextConfiguration();
        }
        
        return pt;
    }    

    /** 
     * Computes a <code>ListPotential</code> representation for the max family
     * of functions using the set of arguments (arg).
     * @param arg a <code>Vector</code> with the arguments (potentials) of the function.
     * @return the representation of this function as a <code>ListPotential</code>.
     */
    public void MaxPotential(Vector arg, boolean full) {
        int i,j;
        
        String hname;     // name of the auxiliary node
        Vector hvalues;   // states of the auxiliary node
        FiniteStates hn;  // auxiliary node
        NodeList hnl;     // list of auxiliary nodes 
        Vector nl;        // auxiliar node list for the potentials of the parameters 
        
        
        Potential auxPot; // auxiliar potential 
        PotentialTree ptree;
        Vector pv = new Vector();   // potentials of the factorization
        
        // Creates auxiiary variables and obtain the probability trees
        // that represeentthe factorization
        hnl = new NodeList();
        //System.out.println("Nro. argumentos = "+ arg.size());
        
        for (i=0,j=1; i<arg.size()-1;i++,j++){
           // Obtain potential
           auxPot = (Potential)arg.elementAt(i);
           // Create a new value
           hname = new String("Z"+j);
           hvalues = ((FiniteStates)(auxPot.getVariables()).elementAt(0)).getStates();
           hn = new FiniteStates(hname, hvalues);
           hnl.insertNode(hn);
           // Substitute a conditional variable by the new variable
           nl = new Vector();
           nl.addElement(hn);
           nl.addElement((FiniteStates)auxPot.getVariables().elementAt(1));        
           auxPot.setVariables(nl);
           // Create a tree with the potential
           ptree = new PotentialTree(auxPot);
           pv.addElement(ptree);
           
           // Dep
           //System.out.println("Potencial de entrada "+i);
           //auxPot.print();
           //System.out.println("Transformado a árbol");
           //ptree.print();
           // Fin Dep */
        }
        
        // Obtains a probability tree representing the deterministic function of the model
        if (full)
             pv.addElement((PotentialTree)fullDeterministicMax((Potential)arg.elementAt(i), hnl));
        else pv.addElement((PotentialTree)deterministicMax((Potential)arg.elementAt(i), hnl));
        FACTORIZED = true;
        
        // Dep
        // System.out.println("\nFactorización obtenida");
        // Fin Dep
        for (i=0; i<pv.size(); i++) {
            //Dep
            //((PotentialTree)pv.elementAt(i)).print();
            // Fin Dep
            tF.addElement((PotentialTree)pv.elementAt(i));
        }
        
    }
    
   /**
    * Gets a  <code>PotentialTree</code> representation of the function
    * over a given list of variables.
    * @param vars a <code>Vector</code> of <code>FiniteStates</code> variables.
    * @return a new <code>PotentialTree</code> with the marginal.
    */
    Potential getPotentialTree(Vector vars){
        
        if (!FACTORIZED)
            MaxPotential(vars, false);
        
        return getPotentialTree();
    }
    
    Potential getPotentialTree() {
        
        if (!FACTORIZED) {
            System.out.println("Error in MaxFunction.getPotentialTree(): MaxFunction.MaxPotential(Vector, boolean) have to be used before invoking this method.");
            return null;
        }
        
        int i;
        Vector varsToAdd;
        PotentialTree pt = new PotentialTree((PotentialTree)tF.elementAt(0));
        //PotentialTree p;
        
        varsToAdd = new Vector();
        for (i=0; i<tF.size()-1; i++)
            varsToAdd.addElement((FiniteStates)((PotentialTree)tF.elementAt(i)).getVariables().elementAt(0));
        
        // Combine
        pt = new PotentialTree((PotentialTree)tF.elementAt(0));
        //pt.print();
        for (i=1; i<tF.size(); i++) {
            pt = (PotentialTree)pt.combine((PotentialTree)tF.elementAt(i));
            //pt.print();
        }      
        
        // Marginalize
        //p = (PotentialTree)pt.marginalizePotential(vars); 
        for (i=0; i<varsToAdd.size(); i++)
            pt = (PotentialTree)pt.addVariable((FiniteStates)varsToAdd.elementAt(i));
        //pt.print();
        
        poT = pt;
        COMPUTED = true;
        
        return pt;
    }
    
    /** 
     * Evaluates the function for a given configuration. In order to evaluate
     * this function we will use the set of arguments (arg).
     * @param conf a given configuration to evaluate the function.
     * @param arg a <code>Vector</code> with the arguments of the function.
     * @return the value of this function for <code>conf</code>.
     */
    double PotValue(double[] arg, Configuration conf) {
        if (!FACTORIZED) {
            System.out.println("Error in MaxFunction.PotValue(double[], Configuration): MaxFunction.MaxPotential(Vector, boolean) have to be used before invoking this method.");
            return 0;
        }
        return PotValue(conf);
    }
    
    double PotValue(Vector arg, Configuration conf) {
        // Factorise
        if (!FACTORIZED)
            MaxPotential(arg, false);
        
        return PotValue(conf);
    }
    
    double PotValue (Configuration conf){
        Vector confVars;          // Variables in conf
        Vector hiddenVars;        // Hidden variables
        Configuration globalConf; // Configuration including hidden variables
        Configuration potConf;    // Configuration restricted to potential variables
        Potential pot;            // Auxiliar potential
        long nroConf;             // Number of confs that must be explored
        double val = 0.0;         // value computed for the given configuration
        double prod = 1.0;        // value needed to compute the potential value for the given configuration
        long j;
        int i;
        
        if (COMPUTED)
            return poT.getValue(conf);
        
        // Get the variables
        //confVars = conf.getVariables();
        
        confVars = new Vector();
        for (i=0; i<conf.getVariables().size(); i++)
           confVars.addElement((FiniteStates)conf.getVariables().elementAt(i));
        
        hiddenVars = new Vector();
        for (i=0; i<tF.size()-1; i++) {
            confVars.addElement((FiniteStates)((Potential)tF.elementAt(i)).getVariables().elementAt(0));
            hiddenVars.addElement((FiniteStates)((Potential)tF.elementAt(i)).getVariables().elementAt(0));
        }
        nroConf = (long)FiniteStates.getSize(hiddenVars);              

        globalConf = new Configuration(confVars, conf, false);
        /*/ Dep
        System.out.print("\n\nConf");
        conf.print();
        System.out.print("GlobalConf");
        globalConf.print();
        // Fin DEP */
        
        // Get the values and compute potential
        val = 0.0;
        for (j=0; j<nroConf; j++) {
            /*/ Dep
            System.out.print("\nCONF. ENTRADA");
            conf.print();
            System.out.println("\nCONF. GLOBAL");
            globalConf.print();
            // Fin Dep */
           for (i=0; i<tF.size(); i++) {
              pot = (Potential)tF.elementAt(i);
              potConf = new Configuration(pot.getVariables(), globalConf, false);
              
              /*/ inicio Dep
              System.out.println("\nPotencial para buscar");
              pot.print();
              System.out.println("\nConfiguración a buscar");
              potConf.print();
              // fin Dep */
              
              System.out.println();
              
              if (i==0) {
                   prod = pot.getValue(potConf);
              } else {
                  prod *= pot.getValue(potConf);
              }
              /*/ Dep
              System.out.println("\nPOTENCIAL");
              pot.print();
              System.out.println("\nConfiguración");
              potConf.print();
              System.out.println(" = "+pot.getValue(potConf));
              System.out.println("prod = "+prod);
              // Fin Dep */
           }
           val += prod;
           /*/ Dep
           System.out.println("\n Valor obtenido ="+val);
           // Fin Dep */
           globalConf.nextConfiguration();
        }
        
        return val;     
    }
    
    /**
     * Removes the argument variable suming over all its values.
     * @param potVar the set of variables in the original Potential.
     * @param var a <code>FiniteStates</code> variable.
     * @return a new Potential with the result of the deletion.
     */
    Potential functionAddVariable(Vector potVar, Vector var) {
        PotentialTree paux;
        
        if (!FACTORIZED) {
            System.out.println("Error in MaxFunction.functionAddVariable(Vector, Vector): MaxFunction.MaxPotential(Vector, full) have to be used before invoking this method.");
            return null;
        }
        
        if (!COMPUTED)
            paux = (PotentialTree)this.getPotentialTree();
        else paux = poT;
        
        return paux.addVariable((FiniteStates)var.elementAt(0));
    }
    
    /** 
     * Marginalizes over a set of variables.
     * @param vars  a <code>Vector</code> of variables.
     * @return a Potential with the marginalization over <code>vars</code>.
     *
     */
    Potential marginalizeFunctionPotential(Vector vars) {
        PotentialTree paux;
        
        if (!FACTORIZED) {
            System.out.println("Error in MaxFunction.marginalizeFunctionPotential(Vector, full): MaxFunction.MaxPotential(Vector) have to be used before invoking this method.");
            return null;
        }
        
        if (!COMPUTED)
            paux = (PotentialTree)this.getPotentialTree();
        else paux = poT;
        
        return paux.marginalizePotential(vars);
    }
    
    /** 
     * Restricts a potential specified by a function to a given configuration
     * of variables.
     * @param inputPot the potential to restrict (It is assumed that 
     * corresponds to a PotentialFunction whose function is a MaxFamiliy function).
     * @param conf the configuration to which <code>inputPot</code> will be
     * restricted.
     * @return the restricted <code>Potential</code>.
     */
    Potential restrictFunctionToVariable(PotentialFunction inputPot, Configuration conf) {
        
        if (!inputPot.getFunction().getName().equalsIgnoreCase("MaxFamily")) {
            System.out.println("Error in MaxFunction.restrictFunctionToVariable(PotentialFunction, Configuration): PotentialFunction has to have a MaxFunction member.");
            return null;
        }
        
        if (!FACTORIZED) {
            System.out.println("Error in MaxFunction.restrictFunctionToVariable(PotentialFunction, Configuration): MaxFunction.MaxPotential(Vector, boolean) have to be used before invoking this method.");
            return null;
        }
        
        int i,j;
        Vector aux;
        Configuration auxConf;
        PotentialTable paux;
        FiniteStates temp;

        aux = new Vector();
        
        for (i=0 ; i<inputPot.getVariables().size() ; i++) {
            temp = (FiniteStates)inputPot.getVariables().elementAt(i);
            if (conf.indexOf(temp) == -1)
                aux.addElement(temp);
        }
  
        paux = new PotentialTable(aux);
        auxConf = new Configuration(inputPot.getVariables(),conf);
  
        for (i=0 ; i<paux.getValues().length ; i++) {
            paux.setValue (i, ((MaxFunction)inputPot.getFunction()).PotValue(auxConf));
            auxConf.nextConfiguration(conf);
        }

        return paux;
    }
    
    /** Evaluates the function for a given configuration. In order to evaluate
     * this function we will use the set of arguments (arg).
     * @param conf the configuration to evaluate the function.
     * @param arg[] the arguments of the function.
     * @return the value of this function for <code>conf</code>.
     */
    /*
    double getPotValue() {
    }
    */
}
