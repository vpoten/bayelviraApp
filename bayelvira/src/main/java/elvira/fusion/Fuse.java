/* Fuse.java */

package elvira.fusion;

import java.io.*;
import java.util.*;
import elvira.*;
import elvira.fusion.Fusion;
import elvira.parser.*;
import elvira.potential.*;


/**
 * Class for fusing networks from the command line.
 */

public class Fuse {


/**
 * For performing experiments from the command line.
 */
  
public static void main(String[] args) throws ParseException, IOException, InvalidEditException {

  if ((args.length == 4) || (args.length == 5)) {
    
    System.out.println("Reading input files...");
    FileWriter f;
    FileInputStream fis = new FileInputStream(args[2]);
    Bnet iBn1 = new Bnet(fis);
    fis.close();
    
    fis = new FileInputStream(args[3]);
    Bnet iBn2 = new Bnet(fis);
    fis.close();
    
    Integer qualitative_type = new Integer (args[0]);
    Integer quantitative_type = new Integer (args[1]);
    
    if ((qualitative_type.intValue()<0) || (qualitative_type.intValue()>3)) {
      System.out.println("Fuse error: Invalid quantitative_type. It must be an integer from [0..3].");
      System.exit(1);
    }
    
    if ((quantitative_type.intValue()<0) || (quantitative_type.intValue()>3)) {
      System.out.println("Fuse error: Invalid quantitative_type. It must be an integer from [0..3].");
      System.exit(1);
    }
    
    System.out.println("Fusing networks...");
    
    Fusion fusBn = new Fusion (qualitative_type.intValue(),quantitative_type.intValue(),iBn1,iBn2);
    
    System.out.println("Fused Bayesian network created");
    if (args.length == 5) {
      f = new FileWriter(args[4]);
      fusBn.save(f);
      f.close();
      System.out.println("    (1) New file: "+args[4]);
    }
    else {
      f = new FileWriter("fuse.elv");
      fusBn.save(f);
      f.close();
      System.out.println("    (1) New file: fuse.elv");
    }
  }
  else {
    System.out.println("\nFuse Error: Invalid number of arguments");
    System.out.println("Fuse Usage: Fuse qualitative_type quantitative_type input_file_1 input_file_2 [output_file]");
    System.exit(1);
  }
}

} // End of class.