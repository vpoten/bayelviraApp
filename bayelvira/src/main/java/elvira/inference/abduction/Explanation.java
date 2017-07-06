/* Explanation.java */

package elvira.inference.abduction;

import java.io.*;
import elvira.*;
import java.util.Vector;
import elvira.potential.PotentialTable;

/**
 * Class <code>Explanation</code>.
 * An object of this class is a pair (configuration,double). 
 * 
 * @since 14/9/2000
 */

public class Explanation {

/** 
 * The configuration of variables and values
 */

private Configuration conf;

/**
 * Probability asociated to the <code>Configuration conf</code>.
 */

private double prob;


/**
 * Creates an empty explanation with probability equal to 0.0.
 */

public Explanation() {

  conf = new Configuration();
  prob = 0.0;
}


/**
 * Creates an explanation for a given configuration and a given probability.
 * @param c the configuration.
 * @param d the probability of <code>c</code>.
 */

public Explanation(Configuration c, double d) {

  conf = c;
  prob = d;
}


/**
 * Gets the configuration in the explanation.
 * @return <code>Configuration conf</code>.
 */

public Configuration getConf() {
  
  return conf;
}


/**
 * Gets the probability of the explanation.
 * @return the probability stored in <code>prob</code>.
 */

public double getProb() {

  return prob;
}


/**
 * Method to modify the configuration in the explanation.
 * @param c the <code>Configuration</code> to store in <code>conf</code>.
 */

public void setConf(Configuration c) {
  
  conf = c;
}


/**
 * Method to modify the probability of the explanation.
 * @param p the probability value to store in <code>prob</code>.
 */

public void setProb(double p) {
  
  prob = p;
}


/**
 * Prints this explanation to the standard output.
 */

public void print() {
  
  System.out.print("(");
  conf.print();
  System.out.print(" , "+ prob + ")"); 
}


/**
 * Saves this explanation in a file.
 * @param p the <code>PrintWriter</code> where the explanation
 * will be written.
 */

public void save(PrintWriter p) throws IOException {

  int i, s;
  String n;
  
  s = conf.size();
  for (i=0 ; i<s ; i++) {
    p.print( ((Node)conf.getVariables().elementAt(i)).getName() + " = ");
    n = new String((String)
                   ((FiniteStates)conf.getVariables().elementAt(i)).
                   getPrintableState(((Integer)
                            conf.getValues().elementAt(i)).intValue()));     

    p.print(n + "\n");
  }

  p.print("} with probability " + prob + "\n");
}


/**
 * Transform the explanation into a string
 */

public String toString( ) {

  int i, s;
  String n;
  String sal = "";
  
  s = conf.size();
  for (i=0 ; i<s ; i++) {
    n = ((Node)conf.getVariables().elementAt(i)).getTitle() + " = ";
    n = n + ((FiniteStates)conf.getVariables().elementAt(i)).
             getState(((Integer) conf.getValues().elementAt(i)).intValue());     
    n = n + "\n";
    sal = sal + n;
  }

  //p.print("} with probability " + prob + "\n");
  return sal;
}



/**
 * Transform an explanation into a posteriorProbability Vector
 * of PotentialTable.
 * For all the variables not included in the explanation or in
 * the evidence, all the states will have probability equal to 0
 *
 * @param nl the <code>NodeList</code> with the variables in the network
 * @param e the observed <code>Evidence</code> 
 */

public Vector toPosteriorProbability(NodeList nl,Evidence e) {
    
    Vector postProb = new Vector();
    int i,index,value;
    FiniteStates fs;
    PotentialTable pot;
  
    
    for(i=0;i<nl.size();i++){
      fs = (FiniteStates)nl.elementAt(i);

      if (!e.isObserved(fs)){ // fs has to be introduced in postProb
        
        pot = new PotentialTable(fs); // prob = 0.0 for all states

        index = this.getConf().indexOf(fs.getName());
        if (index != -1) { // set probability of state included in this 
          value = this.getConf().getValue(index);
          pot.setValue(value,this.getProb()); 
        }

        postProb.addElement(pot);
      }
    
    } // end of main for

    return postProb; 
}



} // end of class