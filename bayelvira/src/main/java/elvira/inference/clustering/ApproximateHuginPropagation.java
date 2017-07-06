/* ApproximateHuinPropagation.java */

package elvira.inference.clustering;

import java.util.Vector;
import java.io.*;
import java.util.Date;
import elvira.*;
import elvira.inference.Propagation;
import elvira.parser.ParseException;
import elvira.potential.PotentialTree;
import elvira.potential.PotentialTable;
import elvira.potential.Potential;
import elvira.inference.clustering.HuginPropagation;

/**
 * Class ApproximateHuginPropagation. Implements the Hugin probabilistic propagation,
 * using probabilityTrees and allow approximate the potentials. 
 *
 * @since 5/03/03
 */

public class ApproximateHuginPropagation extends HuginPropagation {

/**
 * The value used to prune the probabilityTree.
 * For exact computation the ideal value will be 0.0 but the method 
 * compares with < and not with <= so a very small value has to be used
 */

private double limitForPrunning = 10e-30;

/**
 * The max number of leaves in the probabilityTree.
 * For exact computation the value is taken as the maximum available.
 */

private int maximumSize; 

/**
 * A boolean variable to indicate if sortAndBound is applied
 * Default is true.
 */

private boolean ApplySortAndBound=true;



/**
 * Program for performing experiments.
 * The arguments are as follows.
 * 1. Input file: the network.
 * 2. Output file.
 * 3. The value used as limit
 * 4. The value used as maximum number of leaves in the probabilityTree.
 *    If this value is 0, then no sortAndBound is applied
 * 5. Evidence file.
 * If the evidence file is omitted, then no evidences are
 * considered
 */

public static void main(String args[]) throws ParseException, IOException {

  Bnet b;
  Evidence e;
  FileInputStream networkFile, evidenceFile;
  ApproximateHuginPropagation hp;
  int i;
  double limit;
  int maxLeaves;
  boolean sab;
 
  
  if (args.length<5){
    System.out.println("Too few arguments. The arguments are:");
    System.out.println("\tNetwork output-file limit-for-prunning max-pot-size sort-and-bound(true|false) [evidence-file]\n");
  }
  else {
    networkFile = new FileInputStream(args[0]);
    b = new Bnet(networkFile);

    limit = (Double.valueOf(args[2])).doubleValue();
    maxLeaves = (Integer.valueOf(args[3])).intValue();
    

    if (args.length==6) {
      evidenceFile= new FileInputStream(args[5]);
      e = new Evidence(evidenceFile,b.getNodeList());
    }
    else
      e = new Evidence();
  
    if (args[4].equals("true")) sab=true;
    else sab=false;
   
    hp = new ApproximateHuginPropagation(b,e,limit,maxLeaves,sab);

    //if (maxLeaves == 0) hp.setApplySortAndBound(false);


    hp.propagate(hp.getJoinTree().elementAt(0),"no");
    hp.saveResults(args[1]);
  }
}



/**
 * Set the value for ApplySortAndBond
 */
   
public void setApplySortAndBound(boolean sab){
  ApplySortAndBound = sab;
}


/**
 * Set the value for limitForPrunning
 * @param l the limit for pruning. 
 */
   
public void setLimitForPruning(double l) {

  limitForPrunning = l;
}


/**
 * Get the value for limitForPrunning
 */
   
public double getLimitForPruning() {

  return (limitForPrunning);
}


/**
 * Constructor.
 * @param b the Bnet used for the compilation of the joinTree
 * @param e the evidence
 * assumes tables for potentials
 */

public ApproximateHuginPropagation(Bnet b,Evidence e){

  super(b,e,"trees");
  limitForPrunning = 10e-30;
  maximumSize = 10000;        
}


/**
 * Constructor.
 * @param b the Bnet used for the compilation of the joinTree
 * @param e the evidence
 * @param pot the tyoe of potentials to be used
 */

public ApproximateHuginPropagation(Bnet b, Evidence e, String pot) {

  super(b,e,pot);
  limitForPrunning = 10e-30;
  maximumSize = 10000;        
}


/**
 * Constructor.
 * @param b the Bnet used for the compilation of the joinTree
 * @param e the evidence
 * @param pot indicates the type of potential to be used
 */

public ApproximateHuginPropagation(Bnet b,Evidence e,double limit,
                                   int maxSize,boolean SaB){

  super(b,e,"trees");
  limitForPrunning = limit;
  maximumSize = maxSize;    
  ApplySortAndBound=SaB;

  jt.setLimitForPotentialPruning(limitForPrunning);
  jt.setMaximumSizeForPotentialPrunning(maximumSize);
  jt.setApplySortAndBound(ApplySortAndBound); 

}

/**
 * Constructor.
 * @param b the Bnet used for the compilation of the joinTree
 * @param e the evidence
 * @param sigma the deletion sequence to be used 
 */

public ApproximateHuginPropagation(Bnet b,Evidence e,NodeList sigma,double limit,
                                   int maxSize,boolean SaB){

  super(b,e,"trees",sigma);
  limitForPrunning = limit;
  maximumSize = maxSize;    
  ApplySortAndBound=SaB;

  jt.setLimitForPotentialPruning(limitForPrunning);
  jt.setMaximumSizeForPotentialPrunning(maximumSize);
  jt.setApplySortAndBound(ApplySortAndBound); 

}


/**
 * Constructor.
 * @param b the Bnet used for the compilation of the joinTree
 * @param e the evidence
 * @param pot indicates the type of potential to be used
 */

public ApproximateHuginPropagation(Bnet b,Evidence e,double limit,
                                   int maxSize){

  super(b,e,"trees");
  limitForPrunning = limit;
  maximumSize = maxSize;    
  ApplySortAndBound=true;

  jt.setLimitForPotentialPruning(limitForPrunning);
  jt.setMaximumSizeForPotentialPrunning(maximumSize);
  jt.setApplySortAndBound(ApplySortAndBound); 

}


/**
 * Constructor. Do not build the join tree
 * @param b the Bnet used for the compilation of the joinTree
 * @param e the evidence
 * @param pot the type of potential to be used
 */

public ApproximateHuginPropagation(Evidence e, Bnet b, double limit, 
                                   int maxSize){

  super(e,b,"trees");
  limitForPrunning = limit;
  maximumSize = maxSize;

}


/**
 * Transforms the initial relations of the joinTree if they
 * are potentialTrees. The only thing to do is pruning the 
 * nodes whose children are equals, so we use a very small value
 * for limit.
 * This method can be overcharged for special requirements.
 * @ param r the relation to be transformed
 */

public void transformRelationsInJoinTree( ){
  int i,s;
  Relation r;

    s = jt.size();
    for(i=0;i<s;i++){
      r = ((NodeJoinTree)jt.elementAt(i)).getNodeRelation();
      r = transformRelation(r);
    }

}                      


/**
 * Transforms a relation if it is a PotentialTree.
 * The only thing to do is pruning the 
 * nodes which children are equals, so we use a very small value
 * for limit.
 * This method can be overcharged for special requirements.
 * @ param r the relation to be transformed
 */

public Relation transformRelation(Relation r){
  PotentialTree pot;

    pot = (PotentialTree)r.getValues();
    //pot.limitBound(limitForPrunning);
    if ( pot.getTree().getLabel() == 1) 
      if (ApplySortAndBound) pot = (PotentialTree) pot.sortAndBound(maximumSize);
    pot.limitBound(limitForPrunning);
    r.setValues(pot);

  return r;
}                      


/**
 * Transforms a potentialTree.
 * The only thing to do is to prune the 
 * nodes whose children are equals, so we use a very small value
 * for limit.
 * This method can be overcharged for special requirements.
 * IMPORTANT: the potential passed as parameter is modified
 * @ param pot the relation to be transformed
 */

public Potential transformPotential(Potential pot){

    if ( ((PotentialTree)pot).getTree().getLabel() == 1) 
      if (ApplySortAndBound)
        pot = ((PotentialTree)pot).sortAndBound(maximumSize);
    ((PotentialTree)pot).limitBound(limitForPrunning);

  return pot;
}                      



} // end of class
