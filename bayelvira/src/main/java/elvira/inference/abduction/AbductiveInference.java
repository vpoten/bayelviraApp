/* AbductiveInference.java */

package elvira.inference.abduction;

import java.io.*;
import java.util.Date;
import java.util.Vector;
import elvira.*;
import elvira.inference.Propagation;
import elvira.inference.clustering.HuginPropagation;
import elvira.potential.PotentialTable;

/**
 * Class <code>AbductiveInference</code>.
 * Implements a general setting for performing abductive inference.
 *
 * @since 14/9/2000
 */

public class AbductiveInference extends Propagation {

/**
 * The number of explanations to be calculated.
 */

protected int nExplanations;		


/**
 * <code>true</code> if an explanation set is defined.
 * <code>false</code> in other case.
 */

protected boolean partial;		

/**
 * Variables in the explanation set.
 */ 

protected NodeList explanationSet;	

/**
 * A vector containing the K best explanations found during the propagation.
 * Each explanation will be represented by an instance of
 * <code>Explanation</code>.
 */
protected Vector kBest;



/**
 * Gets the number of explanations to be calculated.
 * @return that number.
 */

public int getNExplanations() {
  
  return nExplanations;
}


/**
 * @return the kind of abductive inference to be performed.
 */

public boolean getPartial() {
 
  return partial;
}


/**
 * @return the explanation set.
 */

public NodeList getExplanationSet() {
  
  return explanationSet;
}


/**
 * @return the k best explanations found until this moment.
 */

public Vector getKBest() {
  
  return kBest;
}


/**
 * Sets the number of explanations to compute.
 * @param i the number of explanations.
 */

public void setNExplanations(int i) {
  
  nExplanations = i;
}


/**
 * Sets the explanation set.
 * @param nl the node list to be fixed as the explanation set.
 */

public void setExplanationSet(NodeList nl) {
  
  explanationSet = nl;
}


/** 
 * Sets the value of <code>partial</code>.
 * @param b the value to set.
 */

public void setPartial(boolean b) {
  
  partial = b;
}


/**
 * Obtains the K MPEs using exhaustive computation.
 */

public void exhaustive() {
  
  PotentialTable pot, pot2;
  int i, pos;
  double prob, time;
  Relation r2;
  RelationList rl;
  Configuration conf, aux;
  Explanation exp;
  double pEvidence;
  HuginPropagation hp;
  Date D;


  System.out.println("Computing best explanation (exhaustive)...");
  D = new Date();
  time = (double)D.getTime();    

  // first, we calculate the probability of the evidence
  // using a hugin propagation

  hp = new HuginPropagation(network,observations);
  if (observations.size() > 0) {
    pEvidence = hp.obtainEvidenceProbability("yes");
  }
  else
    pEvidence = 1.0;
  System.out.println("Probabilidad de la evidencia: " + pEvidence);          


  // if there is not explanation set, put it as the non observed variables
  if (explanationSet.size() == 0) {
    obtainInterest();
    explanationSet = interest;
  }
  
  rl = network.getInitialRelations();
  pot = (PotentialTable)((Relation)rl.elementAt(0)).getValues();
  
  if (observations.size() > 0)
    pot.instantiateEvidence(observations); 
  for (i=1 ; i<rl.size() ; i++) {
    r2 = (Relation) rl.elementAt(i);
    pot2 = (PotentialTable)r2.getValues();
    if (observations.size() > 0)
      pot2.instantiateEvidence(observations);
    pot = pot.combine(pot2);
  }
  pot = (PotentialTable) pot.marginalizePotential(explanationSet.toVector()); 
  
  // now select the kBest

  kBest = new Vector();
  conf = new Configuration(pot.getVariables());
  for (i=0 ; i<pot.getValues().length ; i++) {
    prob = pot.getValue(i);
    pos = posToInsert(prob);
    if (pos != -1) {
      aux = new Configuration(explanationSet.toVector(),conf);
      exp = new Explanation(aux,prob);
      insertExplanation(exp,pos);
    }
    conf.nextConfiguration();
  }

  // now we divide by pEvidence

  if (observations.size() != 0) {
    for(i=0 ; i < nExplanations ; i++){
      exp = (Explanation) kBest.elementAt(i);
      exp.setProb(exp.getProb() / pEvidence);
    }
  }

  // showing messages

  D = new Date();
  time = ((double)D.getTime() - time) / 1000;
  System.out.println("Best explanation computed");
  System.out.println("Time (secs): " + time);
}


/**
 * Gets the position in <code>kBest</code> where a given value must be
 * inserted.
 * @param val the value to insert.
 * @return the position of <code>kBest</code> in which value <code>val</code>
 * must be inserted, or -1 if the value mustn't be inserted.
 */

public int posToInsert(double val) {

  int min, max, middle;
  int s;
  double prob, pmiddle;

  s = kBest.size();
  if (s == 0) {
    middle = 0;
  }
  else if ( ( ((Explanation)kBest.elementAt(s-1)).getProb() > val)) {
    if (s < nExplanations)
      middle = s;
    else
      middle = -1;
  }
  else {
    for (min = 0, max = s-1, middle = (int) (min+max)/2; 
	 min < max; 
	 middle = (int) (min+max)/2) {
      pmiddle = ((Explanation)kBest.elementAt(middle)).getProb();
      if (pmiddle < val) {
        max = middle;
      }
      else if (pmiddle > val) {
        min = middle + 1;
      }
      else break; // middle is the possition 
    }  
  }

  return middle;
}


/**
 * returns true if two doubles are almost equals, given a threshold
 */

public boolean almostEqual(double d1, double d2, double threshold){
    if (Math.abs(d1-d2) < threshold) return true;
    else return false;
}

/**
 * Inserts a given explanation in the given position and reduce the size
 * of kBest if needed
 * @param exp a <code>Explanation</code>
 * @param pos an <code>int</code> with the position in which exp will be inserted
 */

public void insertExplanation(Explanation exp, int pos){
  Explanation aux;
  int i,fails=0;
  boolean included;
  
  
  //the method is more complex because precision errors with double
  
  if (kBest.size()==0) kBest.insertElementAt(exp,pos);
  else{
    // first we look if the explanation is already in kBest
      // comparing positions <= pos
    included = false;
    if (pos == kBest.size()) i = pos-1;
    else i = pos;
    
    do{
      aux = (Explanation)kBest.elementAt(i);
      if (almostEqual(aux.getProb(),exp.getProb(),0.000001)){
          if (exp.getConf().equals(aux.getConf())) {
              included = true; 
              break;
          } else i--;
      } else{
          if (fails==0) {i--;fails++;}
          else {fails=0;break;}
      }
    }while ( (!included) && (i>=0));
    
    if (!included){  // comparing positions > pos
      for(i=pos+1; i<kBest.size(); i++){
        aux = (Explanation)kBest.elementAt(i);
        if (almostEqual(aux.getProb(),exp.getProb(),10e-6)){
          if (exp.getConf().equals(aux.getConf())) {
              included = true; 
              break;
          } 
        } else{
            if (fails==0) fails++;
            else break;
        }
      }        
    }
    
    if (!included) kBest.insertElementAt(exp,pos);
  } 
  
  // testing if we have to reduce kBest size
  if (kBest.size() > nExplanations) 
    kBest.removeElementAt(nExplanations); 
}

/**
 * Saves the result of a propagation to a file.
 * @param s a String containing the file name.
 */

public void saveResults(String s) throws IOException {

  FileWriter f;
  PrintWriter p;
  Explanation exp;
  int i;

  f = new FileWriter(s);
  
  p = new PrintWriter(f);
  
  for (i=1 ; i<=kBest.size() ; i++) {
    exp = (Explanation)kBest.elementAt(i-1);
    p.print("\nExplanation " + i + " { \n");
    exp.save(p);
  }
  
  f.close();
}


}  // end of class