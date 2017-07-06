/* Compare.java */

package elvira.fusion;

import java.io.*;
import java.util.*;
import elvira.*;
import elvira.fusion.Fusion;
import elvira.parser.*;
import elvira.potential.*;


/**
 * Compares two Bayesian networks with the same variables. The results of the 
 * comparison are an structural study (coincident, inverted, added links) and
 * the Kullback-Leibler divergence between the two probability distributions.
 *
 * @see class Fusion 
 * @since 25/9/2000
 */

public class Compare {


/**
 * For performing experiments from the command line.
 */

public static void main(String[] args) throws ParseException, IOException, InvalidEditException {

  if (args.length == 2) {
    
    System.out.println("Reading input files...");
    FileWriter f;
    FileInputStream fis = new FileInputStream(args[0]);
    Bnet iBn1 = new Bnet(fis);
    fis.close();
    
    fis = new FileInputStream(args[1]);
    Bnet iBn2 = new Bnet(fis);
    fis.close();
    
    System.out.println("Comparing networks...");
    
    Fusion fusBn = new Fusion (iBn1);
    fusBn.compare(iBn2);
    
  }
  else {
    System.out.println("\nCompare Error: Invalid number of arguments");
    System.out.println("Compare Usage: Compare input_file_1 input_file_2");
    System.exit(1);
  }
}

} // End of class