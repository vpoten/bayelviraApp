/*
 * VEWithPTreeCredalSetSimple.java
 *
 * Created on 19 de julio de 2004, 13:30
 */

package elvira.inference.elimination.impreciseprob;

import elvira.*;
import elvira.potential.*;
import elvira.parser.ParseException;
import elvira.inference.elimination.*;
import java.util.Vector;
import java.io.*;

/**
 * This class implements a propagation algorithm for interval of probabilities using
 * the variable elimination method. The algorithm makes a loop that it is repeated for
 * each configuration of the transparent variables. Each time it restricts all the
 * Potentials to the current configuration of transparent variables, and it makes a
 * probabilistic propagation (using variable elimination). 
 * This method do not use high amounts of memory as in VEWithPTreeCredalSet
 * @author  Andrés Cano Utrera (acu@decsai.ugr.es)
 */
public class VEWithPTreeCredalSetSimple extends VEWithPTreeCredalSet {
  
  /** Creates a new instance of VEWithPTreeCredalSetSimple */
  public VEWithPTreeCredalSetSimple(Bnet b, Evidence e) {
    super(b,e);
  }
  /**
   * Gets the posterior distribution of a given variable and
   * stores it in <code>results</code>.
   * @param v a <code>FiniteStates</code> whose posterior distribution will
   * be computed.
   */
  public void getPosteriorDistributionOf(Node v) {
    NodeList notRemoved;
    //FiniteStates  y;
    Node x;
    Relation r;
    RelationList rLtemp;
    Potential pot;
    PotentialTable table;
    PairTable pt;
    int i, j, p, pos, s;
    int nconfs,k;
    double min,max,value;
    Configuration conf,confTransparentVars;
    Vector vars;
    PotentialIntervalTable pIntervalTable;
    RelationList initialCurrentRelations;
    
    initialCurrentRelations = getInitialRelations(v);
    
    /* Now restrict the valuations to the observations */
    if (observations.size() > 0)
      initialCurrentRelations.restrictToObservations(observations);
    
    /* Creates the configuration of transparent variables confTransparentVars */
    confTransparentVars=new Configuration();
    for(i=0;i<initialCurrentRelations.size();i++){
      Vector transVars=initialCurrentRelations.elementAt(i).getValues().getListTransparents();
      for(j=0;j<transVars.size();j++){
        confTransparentVars.putValue((FiniteStates)transVars.elementAt(j),0);
      }
    }
    
    nconfs=confTransparentVars.possibleValues();
    System.out.println("Numero de configuraciones: "+nconfs);
    vars=new Vector();
    vars.addElement(v);
    pIntervalTable=new PotentialIntervalTable(vars);
    // Inits min and max values in the PotentialIntervalTable
    conf=new Configuration(vars);
    for(j=0;j<((FiniteStates)v).getNumStates();j++){
      pIntervalTable.setMinValue(conf,1.0);
      pIntervalTable.setMaxValue(conf,0.0);
      conf.nextConfiguration();
    }
    
    for (k=0;k<nconfs;k++){// Repeat for each configuration of the transparent variables
      if(k%10000==0){
        System.out.println("Iteración "+k);
      }
      notRemoved = new NodeList();
      pt = new PairTable();
      s = network.getNodeList().size();
      for (i=0 ; i<s ; i++) {
        x = network.getNodeList().elementAt(i);
        if ((!observations.isObserved(x)) && (!x.equals(v))) {
          notRemoved.insertNode(x);
          pt.addElement(x);
        }
      }
      currentRelations=initialCurrentRelations.restrict(confTransparentVars);
     
      for (i=0 ; i<currentRelations.size() ; i++)
        pt.addRelation(currentRelations.elementAt(i));
      for (i=notRemoved.size() ; i>0 ; i--) {
        x = pt.nextToRemove();
        notRemoved.removeNode(x);
        pt.removeVariable(x);
        rLtemp = currentRelations.getRelationsOfAndRemove(x);
        if (rLtemp.size() > 0) {
          r = rLtemp.elementAt(0);
          pt.removeRelation(r);
          pot = r.getValues();
          for (j=1 ; j<rLtemp.size() ; j++) {
            r = rLtemp.elementAt(j);
            pt.removeRelation(r);
            pot = combine(pot,r.getValues());
          }
          pot = addVariable(pot,x);
          pot = transformAfterAdding(pot);
          r = new Relation();
          r.setKind(Relation.POTENTIAL);
          r.getVariables().setNodes((Vector)pot.getVariables().clone());
          r.setValues(pot);
          currentRelations.insertRelation(r);
          pt.addRelation(r);
        }
      }
      confTransparentVars.nextConfiguration();
      
  /* After this, currentRelations must only contain relations
     for variable v */
      rLtemp = currentRelations.getRelationsOf(v);
      r = rLtemp.elementAt(0);
      pt.removeRelation(r);
      pot = r.getValues();
      for (j=1 ; j<rLtemp.size() ; j++) {
        r = rLtemp.elementAt(j);
        pt.removeRelation(r);
        pot = combine(pot,r.getValues());
        //pot.normalize();
      }
      pot = transformAfterEliminating(pot);
      pot.normalize();
  
      conf=new Configuration(vars);
      for(j=0;j<((FiniteStates)v).getNumStates();j++){
        min=pIntervalTable.getMinValue(conf);
        value=pot.getValue(conf);
        if(value<min){
          pIntervalTable.setMinValue(conf,value);
        }
        max=pIntervalTable.getMaxValue(conf);
        if(value>max){
          pIntervalTable.setMaxValue(conf,value);
        }
        conf.nextConfiguration();
      }
    }
    results.addElement(pIntervalTable);
  }
  
  /**
   * This method is overrrided in this class to avoid normalization of results, because
   * they are yet normalized.
   */
  public void normalizeResults() {
  }
  
  /**
   * Program for performing experiments from the command line.
   * The command line arguments are as follows:
   * <ol>
   * <li> Input file: the network.
   * <li> Output file.
   * <li> Evidence file.
   * </ol>
   * If the evidence file is omitted, then no evidences are
   * considered.
   */
  
  public static void main(String args[]) throws ParseException, IOException {
    Network b;
    VariableElimination ve;
    Evidence e=new Evidence();
    String resultsFile="tmp.out";
    Vector varsInterest = new Vector();
    
    if (args.length < 1){
      System.out.println("ERROR:Too few arguments.");
      System.out.println("Use: bnet.elv [Options]");
      System.out.println("OPTIONS: ");
      System.out.println(" -evi <evidenceFile.evi> -->  The evidence file");
      System.out.println(" -out <resultsFile.out> --> The file with the results (if this option is not included then the output will be tmp.out)");
      System.out.println(" -interest <varName> --> Name of a variable of interest. If no -interest option is used then all non-observed variables are included.");
      System.exit(0);
    }
    
    b=Network.read(args[0]);
    for (int i=1; i<args.length; i++){
      if (args[i].equals("-evi")){
        e=new Evidence(args[i+1],b.getNodeList());
        i++;
      }
      else if(args[i].equals("-out")){
        resultsFile=args[i+1];
        i++;
      }
      else if(args[i].equals("-interest")){
        varsInterest.add(args[i+1]);
        i++;
      }
    }
    ve = new VEWithPTreeCredalSetSimple((Bnet)b,e);
    for(int i=0;i<varsInterest.size();i++){
      ve.insertVarInterest(b.getNode((String)varsInterest.elementAt(i)));
    }
    ve.obtainInterest();
    ve.propagate(resultsFile);
  }
  
}
