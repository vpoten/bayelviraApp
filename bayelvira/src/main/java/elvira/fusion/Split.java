/* Split.java */

package elvira.fusion;

import java.io.*;
import java.util.*;
import elvira.*;
import elvira.fusion.Fusion;
import elvira.parser.*;
import elvira.potential.*;

/**
 * This class can be used to split in two a given Bayesian network
 * from the command line.
 *
 * @see class Fusion
 * @since 25/9/2000
 */

public class Split {

/**
 * To split a network from the command line.
 */
  
public static void main(String[] args) throws ParseException, IOException, InvalidEditException {

  if ((args.length==4) || (args.length==6)) {
    FileWriter f;
    FileInputStream fis = new FileInputStream(args[3]);
    Fusion iBn = new Fusion (new Bnet(fis));
    fis.close();
    
    Vector nets = new Vector();
    
    if (args[0].compareTo("t") == 0) {
      // Topological split
      nets = iBn.split(Integer.valueOf(args[1]).intValue(),Integer.valueOf(args[2]).intValue());
      System.out.println("Bayesian networks created");
      if (args.length == 6) {
	f = new FileWriter(args[4]);
	((Bnet)nets.elementAt(0)).saveBnet(f);
	f.close();
	f = new FileWriter(args[5]);
	((Bnet)nets.elementAt(1)).saveBnet(f);
	f.close();
	System.out.println("    (2) New files: "+args[4]+" "+args[5]);
      }
      else {
	f = new FileWriter("split1.elv");
	((Bnet)nets.elementAt(0)).saveBnet(f);
	f.close();
	System.out.println("Pasa");
	f = new FileWriter("split2.elv");
	((Bnet)nets.elementAt(1)).saveBnet(f);
	f.close();
	System.out.println("Pas2");
	System.out.println("    (2) New files: split1.elv split2.elv");
      }
    }
    else if (args[0].compareTo("a") == 0) {
      // Random split
      nets = iBn.randomSplit(Integer.valueOf(args[1]).intValue(),Integer.valueOf(args[2]).intValue());
      System.out.println("Bayesian networks created");
      if (args.length == 6) {
	f = new FileWriter(args[4]);
	((Bnet)nets.elementAt(0)).saveBnet(f);
	f.close();
	f = new FileWriter(args[5]);
	((Bnet)nets.elementAt(1)).saveBnet(f);
	f.close();
	System.out.println("    (2) New files: "+args[4]+" "+args[5]);
      }
      else {
	f = new FileWriter("rsplit1.elv");
	((Bnet)nets.elementAt(0)).saveBnet(f);
	f.close();
	f = new FileWriter("rsplit2.elv");
	((Bnet)nets.elementAt(1)).saveBnet(f);
	f.close();
	System.out.println("    (2) New files: rsplit1.elv rsplit2.elv");
      }
    }
    else {
      System.out.println("Split Usage: Split type %nodes %common input_file [output_file_1 output_file_2]");
      System.out.println("Split Usage: type must be one of the following characters: a(random), t(topological)");
      System.exit(1);
    }
  }
  else {
    System.out.println("\nSplit Error: Invalid number of arguments");
    System.out.println("Split Usage: Split type %nodes %common input_file [output_file_1 output_file_2]");
    System.exit(1);
  }
}

} // End of class.