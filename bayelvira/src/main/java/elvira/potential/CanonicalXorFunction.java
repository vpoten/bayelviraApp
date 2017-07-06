package elvira.potential;

import java.util.Vector;
import elvira.*;

/**
 * <p>Title: CanonicalXorFunction </p>
 * <p>Description: Canonical model for Xor </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author francisco
 * @version 1.0
 */

public class CanonicalXorFunction extends CanonicalFunction {

  /* CONSTRUCTORS */

  public CanonicalXorFunction() {
    super();
    name = "Xor";
  }

  /* METHODS */

  public Potential restrictFunctionToVariable(PotentialFunction inputPot, Configuration conf) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: restrictFunctiontoVariable not implemented in CanonicalXorFunction!!!!");
    return (Potential) null;
  }
  public Potential marginalizeFunctionPotential(Vector vars) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: marginalizeFunctionPotential not implemented in CanonicalXorFunction!!!!");
    return (Potential) null;
  }
  public Potential functionAddVariable(Vector potVar, Vector vars) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: functionAddVariable not implemented in CanonicalXorFunction!!!!");
    return (Potential) null;
  }
  public double PotValue(double arg[], Configuration conf) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: PotValue not implemented in CanonicalXorFunction!!!!");
    return 0.0;
  }

  /**
   * Return a column of the CPT; arg[] contains the set of columns corresponding
   * to the configuration involved; colSize is the number of States of the child
   * node involved
   */

  public double[] PotValues(double arg[], int colSize) {
    double[] dArray = new double[colSize];

    return dArray;
  }
}