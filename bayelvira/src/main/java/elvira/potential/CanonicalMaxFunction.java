package elvira.potential;

import java.util.Vector;
import elvira.*;

/**
 * <p>Title: CanonicalMaxFunction </p>
 * <p>Description: Canonical model for Max and related models </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author francisco
 * @version 1.0
 */

public class CanonicalMaxFunction extends CanonicalFunction {

  /* CONSTRUCTORS */

  public CanonicalMaxFunction() {
    super();
  }
  public CanonicalMaxFunction(String kind) {
    this();
    name = kind;
  }

  /* METHODS */

  public Potential restrictFunctionToVariable(PotentialFunction inputPot, Configuration conf) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: restrictFunctionToVariable not implemented in CanonicalMaxFunction!!!!");
    return (Potential) null;
  }
  public Potential marginalizeFunctionPotential(Vector vars) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: marginalizeFunctionPotential not implemented in CanonicalMaxFunction!!!!");
    return (Potential) null;
  }
  public Potential functionAddVariable(Vector potVar, Vector vars) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: functionAddVariable not implemented in CanonicalMaxFunction!!!!");
    return (Potential) null;
  }
  public double PotValue(double arg[], Configuration conf) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: PotValue not implemented in CanonicalMaxFunction!!!!");
    return 0.0;
  }

  /**
   * Return a column of the CPT; arg[] contains the set of columns corresponding
   * to the configuration involved; colSize is the number of States of the child
   * node involved
   */

  public double[] PotValues(double arg[], int colSize) {
    double[] dArray = new double[colSize];
    double[] theCArray = new double[arg.length];
    double[] theQArray = new double[colSize];

    for (int i=0; i<colSize; i++) {
      theQArray[i]=1.0;
    }

    for (int i=0; i<arg.length/colSize; i++) {
      theCArray[i*colSize+colSize-1]=arg[i*colSize+colSize-1];
      for (int j=colSize-2; j>=0; j--) {
	theCArray[i*colSize+j]=arg[i*colSize+j]+theCArray[i*colSize+j+1];
      }
    }

    for (int i=0; i<arg.length/colSize; i++) {
      for (int j=0; j<colSize; j++) {
	theQArray[j]=theQArray[j]*theCArray[j+i*colSize];
      }
    }

    dArray[colSize-1]=theQArray[colSize-1];

    for (int i=colSize-2; i>=0; i--) {
      dArray[i]=theQArray[i]-theQArray[i+1];
    }

    for (int i=0; i<dArray.length; i++) {
      double roundedValue = dArray[i];
      roundedValue = Math.round(roundedValue*100000);
      dArray[i]=roundedValue/100000;
    }

    return dArray;
  }

  public void transform2Henrion(Vector arguments, int states) {

    double leakage[] = new double[states];

    leakage[states-1]=((PotentialTable) arguments.elementAt(arguments.size()-1)).getValue(states-1);
    for (int k=states-2; k>=0; k--) {
      leakage[k] = leakage[k+1] + ((PotentialTable) arguments.elementAt(arguments.size()-1)).getValue(k);
    }

    for (int k=0; k<arguments.size()-1; k++) {
      int orColumns = ((PotentialTable) arguments.elementAt(k)).getValues().length/states, pos, cpos, i, j;
      double  c[] = new double[((PotentialTable) arguments.elementAt(k)).getValues().length],
	      v[] = new double [((PotentialTable) arguments.elementAt(k)).getValues().length],
	      total = 0, lastOne, tmp;

      for (i=0; i<orColumns; i++) {
	c[i+orColumns*(states-1)]=((PotentialTable) arguments.elementAt(k)).getValue(i+orColumns*(states-1));
	for (j=states-2; j>=0; j--) {
	   c[i+j*orColumns] = c[i+(j+1)*orColumns] + ((PotentialTable) arguments.elementAt(k)).getValue(i+j*orColumns);
	}
      }

      for (i=0; i<orColumns; i++) {
        lastOne = 1;
	double theTotal = 0;
        for (j=0; j<states-1; j++) {
          pos = (j * orColumns) + i;
          total = c[((j+1)*orColumns)+i] * leakage[j+1];
          tmp = Math.round((lastOne-total)*10000);
          tmp = tmp / 10000;
          v[pos] = tmp;
	  theTotal = v[pos] + theTotal;
          lastOne = total;
        }
	tmp = Math.round((1-theTotal)*10000);
	v[(states-1)*orColumns+i] = tmp/10000;
      }

      ((PotentialTable) arguments.elementAt(k)).setValues(v);
    }
  }

  public void transform2Diez(Vector arguments, int states) {
    double leakage[] = new double[states];

    leakage[states-1]=((PotentialTable) arguments.elementAt(arguments.size()-1)).getValue(states-1);
    for (int k=states-2; k>=0; k--) {
      leakage[k] = leakage[k+1] + ((PotentialTable) arguments.elementAt(arguments.size()-1)).getValue(k);
    }

    for (int k=0; k<arguments.size()-1; k++) {
      int orColumns = ((PotentialTable) arguments.elementAt(k)).getValues().length/states, pos, cpos, i, j;
      double  c[] = new double[((PotentialTable) arguments.elementAt(k)).getValues().length],
	      v[] = new double [((PotentialTable) arguments.elementAt(k)).getValues().length],
	      total = 0, lastOne, tmp;

      for (i=0; i<orColumns; i++) {
	c[i+orColumns*(states-1)]=((PotentialTable) arguments.elementAt(k)).getValue(i+orColumns*(states-1));
	for (j=states-2; j>=0; j--) {
	   c[i+j*orColumns] = c[i+(j+1)*orColumns] + ((PotentialTable) arguments.elementAt(k)).getValue(i+j*orColumns);
	}
      }

      for (i=0; i<orColumns; i++) {
        lastOne = 1;
	double theTotal = 0;
        for (j=0; j<states-1; j++) {
          pos = (j * orColumns) + i;
          total = c[((j+1)*orColumns)+i] / leakage[j+1];
          tmp = Math.round((lastOne-total)*10000);
          tmp = tmp / 10000;
          v[pos] = tmp;
	  theTotal = v[pos] + theTotal;
          lastOne = total;
        }
	tmp = Math.round((1-theTotal)*10000);
	v[(states-1)*orColumns+i] = tmp/10000;
      }

      ((PotentialTable) arguments.elementAt(k)).setValues(v);
    }
  }
}