/*
 * LazyShenoyShaferHC.java
 
 *
 * Created on 12 de abril de 2005, 13:54
 */

package elvira.inference.clustering.impreciseprob;

import java.util.Hashtable;
import java.util.Vector;
import java.io.*;
import elvira.*;
import elvira.inference.*;
import elvira.inference.clustering.*;
import elvira.inference.clustering.multiplemessaging.*;
import elvira.potential.*;
import elvira.parser.ParseException;

/**
 * This class extends ShenoyShaferHC. It is a modification of that propagation algorithm for imprecise probabilities.
 * Now, the algorithm uses lazy evaluation by making use of ListPotential.
 * @author Andrés Cano Utrera (acu@decsai.ugr.es)
 * @author Manuel Gómez Olmedo (mgomez@decsai.ugr.es)
 */
public class LazyShenoyShaferHC extends ShenoyShaferHC {
  
  /**
   * Creates a new instance of  LazyShenoyShaferHC
   * @param b the Bnet (Credal network) to be propagated
   * @param e the set of observed variables (Evidence set)
   */
  public LazyShenoyShaferHC(Bnet b, Evidence e) {
    super(b,e);
  }
  
  /**
   * Initializes the potentials in the join tree as objects of
   * class <code>PTreeCredalSet</code>. The potentials are taken from
   * a given RelationList.
   * @param ir the <code>RelationList</code> which contains the initial relations.
   */
  public void initCliques(RelationList ir) {
    Relation r;
    ListPotential pot;
    NodeJoinTree node;
    
    marginalCliques = joinTree.Leaves(ir);
    joinTree.binTree();
    for (int i=0 ; i<joinTree.getJoinTreeNodes().size() ; i++) {
      node = joinTree.elementAt(i);
      r = node.getNodeRelation();
      if(r.getValues()==null){
        pot = (ListPotential)makeUnitPotential(r.getVariables());  
        r.setValues(pot);
      }
    }
  }
  
  /**
   * Transforms the Potential of one of the original relations. If the Potential
   * is a PotentialInterval then it is transformed into a PTreeCredalSet. If
   * the Potential is a PTreeCredalSet then it is not modified. Otherwise an
   * error is produced. Finally the potential is introduced into a ListPotential
   * @param r the Relation to be transformed
   * @return The transformed Relation
   */
  public elvira.Relation transformInitialRelation(elvira.Relation r) {
    Relation rel;
    Potential pot;
    
    rel=super.transformInitialRelation(r);
    pot=new ListPotential(r.getValues());
    rel.setValues(pot);
    return rel;
  }
  
  /**
   * Creates a new ListPotential initialized with no Potential.
   * It contains an empty list of variables
   * @return the new ListPotential
   */
  public Potential makeUnitPotential(){
    ListPotential pot;
    pot = new ListPotential();
    return pot;
  }
  
  /**
   * Creates a new ListPotential initialized with no Potential.
   * The list of variables will be a copy of the NodeList parameter.
   * @param nlist The NodeList to use as list of variables of the new PTreeCredalSet
   * @return the new ListPotential
   */
  public elvira.potential.Potential makeUnitPotential(NodeList nlist){
    ListPotential pot;
    pot = new ListPotential(nlist);
    return pot;
  }
  
  public static void main(String args[]) throws ParseException, IOException {
    Network b;
    ShenoyShaferHC propagation;
    Evidence e=new Evidence();
    String resultsFile="tmp.out";
    Vector<String> varsInterest = new Vector<String>();
    int nprops=3;
    
    if (args.length < 1){
      System.out.println("ERROR:Too few arguments.");
      System.out.println("Use: bnet.elv [Options]");
      System.out.println("OPTIONS: ");
      System.out.println(" -evi <evidenceFile.evi> -->  The evidence file");
      System.out.println(" -out <resultsFile.out> --> The file with the results (if this option is not included then the output will be tmp.out)");
      System.out.println(" -interest <varName> --> Name of a variable of interest. If no -interest option is used then all non-observed variables are included.");
      System.out.println(" -steps <intValue> --> Number of propagations (>=3 , default value 3)");
      System.exit(0);
    }
    b=Network.read(args[0]);
    for (int i=1; i<args.length; i++){
      if (args[i].equals("-evi")){
        e=new Evidence(args[i+1],b.getNodeList());
        i++;
      } else if(args[i].equals("-out")){
        resultsFile=args[i+1];
        i++;
      } else if(args[i].equals("-interest")){
        varsInterest.add(args[i+1]);
        i++;
      }
       else if(args[i].equals("-steps")) {
        nprops=Integer.valueOf(args[i+1]);
        i++;
      }
    }
    propagation=new LazyShenoyShaferHC((Bnet)b,e);
    propagation.setNsteps(nprops);
    
    for(int i=0;i<varsInterest.size();i++){
      propagation.insertVarInterest(b.getNode((String)varsInterest.elementAt(i)));
    }
    propagation.obtainInterest(); // If there are not any interest variable put all the non-observed ones
    propagation.propagate(resultsFile);
  }
}
