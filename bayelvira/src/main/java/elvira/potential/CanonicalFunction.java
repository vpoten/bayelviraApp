package elvira.potential;

/**
 * <p>Title: CanonicalFunction </p>
 * <p>Description: Common ancestor for Canonical models </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author francisco
 * @version 1.0
 */

public abstract class CanonicalFunction extends Function {

  public CanonicalFunction() {
  }

  abstract double[] PotValues(double arg[], int colSize);
}