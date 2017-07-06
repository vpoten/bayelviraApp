/*
 * BranchBound.java
 *
 * Created on 13 de septiembre de 2004, 13:20
 */

package elvira.inference.misc.impreciseprob;

import elvira.inference.clustering.impreciseprob.ShenoyShaferHC;
import elvira.inference.elimination.impreciseprob.VEWithPTreeCredalSet;
import elvira.inference.elimination.impreciseprob.VEWithPTreeMinMaxCredalSet;
import elvira.potential.*;
import elvira.parser.ParseException;
import elvira.*;
import elvira.tools.DSeparation;
import java.io.*;

import java.util.Vector;

/**
 *
 * @author Andrés Cano Utrera (acu@decsai.ugr.es)
 * @author Manuel Gómez Olmedo (mgomez@decsai.ugr.es)
 */
public class BranchBound extends elvira.inference.Propagation {
  static final int FINDMAX=0;
  static final int FINDMIN=1;
  /**
   *  The best bound found so far in the recursive search
   */
  private double bestBound;
  
  /**
   * The number of visited nodes in the branch and bound search algorithm
   */
  private long visitedNodes;
  
  /**
   * This Bnet will contain a copy of the potentials in super.network transformed
   * into PTreeCredalSet. It will be the Bnet used in the propagation algorithm.
   * This Bnet contains only the variables not dseparated with the target variable
   */
  private Bnet bnet;
  
  /**
   * The paramater to use when a probability tree is pruned in inner nodes of the search tree (VEWithPTreeMinMaxCredalSet)
   */
  double limitForPruning=0.0;
  
  /**
   * The number of propagations to carry out in the Join Tree in the initialization step with ShenoyShaferHC. By default we use 3 propagations
   */
  private int nstepsShSh=3;
  
  
  
  /** Creates a new instance of BranchBound */
  public BranchBound(Bnet b, Evidence e) {
    network = b;
    observations=e;
  }
  
  /**
   * Set the field limiForPruning
   * @param sigma the new value for the field limitForPruning
   */
  public void setLimitForPruning(double sigma){
    limitForPruning=sigma;
  }
  
  /**
   * Transforms the Potential of one of the original relations. If the Potential
   * is a PotentialInterval then it is transformed into a PTreeCredalSet. If
   * the Potential is a PTreeCredalSet then it is not modified. Otherwise an
   * error is produced.
   * @ param r the <code>Relation</code> to be transformed.
   */
  public elvira.Relation transformInitialRelation(elvira.Relation r) {
    if(r.getValues().getClass()==PTreeCredalSet.class){
      return r;
    } else if(r.getValues() instanceof PotentialTable){//to convert PotentialTable and PotentialConvexSet
      PTreeCredalSet pTreeCS=new PTreeCredalSet((PotentialTable)r.getValues());
      Relation rNew=new Relation();
      rNew.setVariables(r.getVariables().copy());
      rNew.setKind(r.getKind());
      rNew.setValues(pTreeCS);
      return rNew;
    } else if(r.getValues() instanceof PotentialInterval){
      PTreeCredalSet pTreeCS=new PTreeCredalSet((PotentialInterval)r.getValues());
      Relation rNew=new Relation();
      rNew.setVariables(r.getVariables().copy());
      rNew.setKind(r.getKind());
      rNew.setValues(pTreeCS);
      return rNew;
    } else{
      System.out.print("Error in BranchBound.transformInitialRelation(Relation r): ");
      System.out.println("Potentials of"+r.getValues().getClassName()+ " class cannot be propagated with this class");
      System.exit(1);
    }
    return null;
  }
  
  private Bnet makeReducedBnet(FiniteStates var){
    RelationList rList;
    Bnet b;
    
    rList=getInitialRelations(var);
    b=new Bnet(network.getNodeList());
    b.setRelationList(rList.getRelations());
    return b;
  }
  
  /**
   * Carries out a propagation.
   * @param resultFile the name of the file where the results will be stored.
   */
  public void propagate(String resultFile)  throws IOException{
    int n=getNumberOfInterest();
    FiniteStates var;
    int ncasos;
    Vector varsResults;
    PotentialIntervalTable potInterv,initialBounds;
    ShenoyShaferHC shShHC;
    RelationList rList;
    
    // Build a Bnet with original relations transformed into PTreeCredalSets
    rList=getInitialRelations();
    bnet=new Bnet(network.getNodeList());
    bnet.setRelationList(rList.getRelations());
    
    shShHC=new ShenoyShaferHC(bnet,observations);
    shShHC.setNsteps(nstepsShSh);
    for(int i=0;i<n;i++){
      var=(FiniteStates)getVarInterest(i);
      
      bnet=makeReducedBnet(var);
      
      ncasos=var.getNumStates();
      
      // Create a PotentialIntervalTable to save the result for var
      varsResults=new Vector();
      varsResults.addElement(var);
      potInterv=new PotentialIntervalTable(varsResults);
      
      results.addElement(potInterv);
      initialBounds=shShHC.propagate(var); // propagate with ShenoyShafer for var
      System.out.println("DENTRO DE BRANCHBOUND.propagate(): INITIAL  BOUNDS...");
      initialBounds.print();
      for(int j=0;j<var.getNumStates();j++){
        System.out.println("OPTIMIZANO CASO N�MERO "+j);
        System.out.println("BUSCANDO maximo");
        // Find Max value for case j in variable var
        bestBound=initialBounds.getMaxValue(j);  // Initialize bestBound with value from ShenoyShaferHC method
        System.out.println("Valor inicial de bestBound: "+bestBound);
        branchBound(var,j,FINDMAX);
        System.out.println("\n\nNodos Visitados para el m�ximo: "+visitedNodes+"\n\n");
        potInterv.setMaxValue(j, bestBound);
        System.out.println("BUSCANDO minimo");
        // Find Min value for case j in variable var
        bestBound=initialBounds.getMinValue(j);  // Initialize bestBound with value from ShenoyShaferHC method
        branchBound(var,j,FINDMIN);
        System.out.println("\n\nNodos Visitados para el m�nimo: "+visitedNodes+"\n\n");
        potInterv.setMinValue(j, bestBound);
      }
    }
    saveResultsAsNetwork(resultFile);
  }
  
  private void branchBound(FiniteStates var, int ncase, int findMinMax){
    Vector rList;
    Vector listTransNoInst=new Vector();
    Vector listTransInst=new Vector();
    Evidence evidenceTrans=new Evidence();
    
    visitedNodes=0;
    rList = bnet.getRelationList();
    for(int i=0;i<rList.size();i++){
      Vector transVars=((Relation)rList.elementAt(i)).getValues().getListTransparents();
      listTransNoInst.addAll(transVars);
    }
    branchBoundRecur(var,ncase,findMinMax,listTransNoInst,evidenceTrans);
    System.out.println("Visitednodes="+visitedNodes);
  }
  
  private void branchBoundRecur(FiniteStates var,int ncase, int findMinMax,
          Vector listTransNoInst,Evidence evidenceTrans){
    FiniteStates trans;
    int nCasesTrans;
    PotentialIntervalTable pot;
    double value;
    boolean foundNewBestBound=false;
    
    visitedNodes++; // Add 1 to visitedNodes
    
    pot=getPotentialInNodeSearchTree(var,listTransNoInst, evidenceTrans);
    if(findMinMax==FINDMAX){
      value=pot.getMaxValue(ncase);
      if(value>bestBound){
        foundNewBestBound=true;
      }
    } else if(findMinMax==FINDMIN){
      value=pot.getMinValue(ncase);
      if(value<bestBound){
        foundNewBestBound=true;
      }
    } else{
      value=0.0;
      System.out.println("Error in" +getClass().getName()+".branchBoundRecur(FiniteStates,int, int, Vector ,Evidence): findMinMax is nor FINDMIN nor FINDMAX");
      System.exit(1);
    }
    
    if(foundNewBestBound) {
      if(listTransNoInst.size()>0){// We are in an inner node in branch and bound search
        trans=(FiniteStates)listTransNoInst.remove(0); // remove trans from listTransNoInst
        nCasesTrans=trans.getNumStates();
        for(int i=0;i<nCasesTrans;i++){
          evidenceTrans.insert(trans,i); // instantiate trans to case number i
          branchBoundRecur(var,ncase,findMinMax,listTransNoInst,evidenceTrans);
          evidenceTrans.remove(trans); // remove evidence for trans variable
        }
        listTransNoInst.insertElementAt(trans,0); // insert again trans in listTransNoInst
      } else{// We are in a leaf node in the branch and bound search
        bestBound=value; // set the best bound found so far
      }
    } else{
      if(listTransNoInst.size()>0)
        System.out.println("PODANDO para busqueda de ... besbound="+bestBound+" value="+value+"\n");
    }
    
    /*
     visitedNodes++; // Add 1 to visitedNodes
    trans=(FiniteStates)listTransNoInst.remove(0); // remove trans from listTransNoInst
    nCasesTrans=trans.getNumStates();
    for(int i=0;i<nCasesTrans;i++){
      evidenceTrans.insert(trans,i); // instantiate trans to case number i
     
      branchBoundRecur(var,ncase,findMinMax,listTransNoInst,evidenceTrans);
     
      totalEvidence=new Evidence(this.observations,evidenceTrans); //totalEvidence= normal evidencel+ evidenceTrans;
      if(listTransNoInst.size()>0){ // We are in an inner node in branch and bound search
        vePTMMCS=new VEWithPTreeMinMaxCredalSet(bnet);
        vePTMMCS.setLimitForPruning(limitForPruning);
        pot=vePTMMCS.propagate(var, totalEvidence); // Propagate with VEWithPTreeMinMaxCredalSet
        //System.out.println("Resultado VEMinMax=["+pot.getMinValue(ncase)+","+pot.getMaxValue(ncase));
      } else{ // We are in a leaf node in the branch and bound search
        vePTCS=new VEWithPTreeCredalSet(bnet);
        pot=vePTCS.propagate(var, totalEvidence); // Propagate with VEWithPTreeCredalSet
        //System.out.println("Resultado VE=["+pot.getMinValue(ncase)+","+pot.getMaxValue(ncase));
      }
     
      if(findMinMax==FINDMAX){
        value=pot.getMaxValue(ncase);
        if(value>bestBound) {
          if(listTransNoInst.size()>0){// We are in an inner node in branch and bound search
            branchBoundRecur(var,ncase,findMinMax,listTransNoInst,evidenceTrans);
          } else{// We are in a leaf node in the branch and bound search
            bestBound=value; // set the best bound found so far
          }
        } else{
          //System.out.println("PODANDO para busqueda maximo. besbound="+bestBound+" value="+value+"\n");
        }
      } else if(findMinMax==FINDMIN){
        value=pot.getMinValue(ncase);
        if(value<bestBound) {
          if(listTransNoInst.size()>0){// We are in an inner node in branch and bound search
            branchBoundRecur(var,ncase,findMinMax,listTransNoInst,evidenceTrans);
          } else{// We are in a leaf node in the branch and bound search
            bestBound=value; // set the best bound found so far
          }
        } else{
          //System.out.println("PODANDO para busqueda minimo\n");
        }
      } else{
        System.out.println("Error in elvira.inference.misc.impreciseprob.BranchBound.branchBoundRecur(FiniteStates,int, int, Vector ,Evidence): findMinMax is nor FINDMIN nor FINDMAX");
        System.exit(1);
      }
      evidenceTrans.remove(trans); // remove evidence for trans variable
    }
    listTransNoInst.insertElementAt(trans,0); // insert again trans in listTransNoInst*/
  }
  
  private PotentialIntervalTable getPotentialInNodeSearchTree(FiniteStates var,Vector listTransNoInst,Evidence evidenceTrans){
    Evidence totalEvidence;
    VEWithPTreeCredalSet vePTCS;
    VEWithPTreeMinMaxCredalSet vePTMMCS;
    PotentialIntervalTable pot;
    
    totalEvidence=new Evidence(this.observations,evidenceTrans); //totalEvidence= normal evidencel+ evidenceTrans;
    if(listTransNoInst.size()>0){ // We are in an inner node in branch and bound search
      vePTMMCS=new VEWithPTreeMinMaxCredalSet(bnet);
      vePTMMCS.setLimitForPruning(limitForPruning);
      pot=vePTMMCS.propagate(var, totalEvidence); // Propagate with VEWithPTreeMinMaxCredalSet
      //System.out.println("Resultado VEMinMax=["+pot.getMinValue(ncase)+","+pot.getMaxValue(ncase));
    } else{ // We are in a leaf node in the branch and bound search
      vePTCS=new VEWithPTreeCredalSet(bnet);
      pot=vePTCS.propagate(var, totalEvidence); // Propagate with VEWithPTreeCredalSet
      //System.out.println("Resultado VE=["+pot.getMinValue(ncase)+","+pot.getMaxValue(ncase));
    }
    return pot;
  }
  
  /**
   * Program for performing experiments from the command line.
   * The command line arguments are as follows:
   * <ol>
   * <li> bnet.elv --> the network.
   * <li> -evi <evidenceFile.evi> --> The evidence file"
   * <li> -out <resultsFile.out> --> The file with the results (if this option is not included the results will be stored in tmp.out
   * <li> -interest <varName> --> Name of the variable of interest. If no -interest option is used then all non-observed variables are included
   * <li> -sigma <limitForPruning> --> A double value used to prune probability trees in inner nodes of the search tree
   * <li> -steps <intValue> --> Number of propagations for the initialization step with ShenoyShaferHC (>=3 , default value 3)
   * </ol>
   * If the evidence file is omitted, then no evidences are
   * considered.
   */
  
  public static void main(String args[]) throws ParseException, IOException {
    Network b;
    Evidence e=new Evidence();
    double sigma=0.0;
    int nprops=3;
    BranchBound branchBound;
    String resultsFile="tmp.out";
    Vector varsInterest=new Vector();
    
    if (args.length < 1){
      System.out.println("ERROR: Too few arguments.");
      System.out.println("Use: bnet.elv [Options]");
      System.out.println("OPTIONS: ");
      System.out.println("-evi <evidenceFile.evi> --> The evidence file");
      System.out.println("-out <resultsFile.out> --> The file with the results (if this option is not included the results will be stored in tmp.out");
      System.out.println("-interest <varName> --> Name of the variable of interest. If no -interest option is used then all non-observed variables are included");
      System.out.println("-sigma <limitForPruning> --> A double value used to prune probability trees in inner nodes of the search tree (default 0.0");
      System.out.println(" -steps <intValue> --> Number of propagations for the initialization step with ShenoyShaferHC (>=3 , default value 3)");
      System.exit(0);
    }
    
    // Read the network to evaluate
    b=Network.read(args[0]);
    
    // Deal with the arguments
    for(int i=1; i < args.length; i++){
      if (args[i].equals("-evi")){
        // Create the evidence object
        e=new Evidence(args[i+1],b.getNodeList());
        i++;
      } else if (args[i].equals("-out")){
        resultsFile=args[i+1];
        i++;
      } else if (args[i].equals("-interest")){
        varsInterest.add(args[i+1]);
        i++;
      } else if (args[i].equals("-sigma")){
        sigma=Double.valueOf(args[i+1]);
        i++;
      } else if(args[i].equals("-steps")) {
        nprops=Integer.valueOf(args[i+1]);
        i++;
      }
    }
    
    // Create the objet from VEWithPTreeMinMaxCredalSet
    branchBound = new BranchBound((Bnet)b,e);
    branchBound.limitForPruning=sigma;
    branchBound.nstepsShSh=nprops;
    
    // Deal with variables of interest
    for(int i=0; i < varsInterest.size(); i++){
      branchBound.insertVarInterest(b.getNode((String)varsInterest.elementAt(i)));
    }
    branchBound.obtainInterest();
    branchBound.propagate(resultsFile);
  }
}
