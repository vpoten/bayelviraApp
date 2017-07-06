package elvira.potential;

import java.util.Vector;
import elvira.*;

/**
 * <p>Title: CanonicalMinFunction </p>
 * <p>Description: Canonical model for Min and related models </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author francisco
 * @version 1.0
 */

public class CanonicalMinFunction extends CanonicalFunction {

  /* CONSTRUCTORS */

  public CanonicalMinFunction() {
    super();
  }
  public CanonicalMinFunction(String kind) {
    this();
    name = kind;
  }

  /* METHODS */

  public Potential restrictFunctionToVariable(PotentialFunction inputPot, Configuration conf) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: restrictFunctionToVariable not implemented in CanonicalMinFunction!!!!");
    return (Potential) null;
  }
  public Potential marginalizeFunctionPotential(Vector vars) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: marginalizeFunctionPotential not implemented in CanonicalMinFunction!!!!");
    return (Potential) null;
  }
  public Potential functionAddVariable(Vector potVar, Vector vars) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: functionAddVariable not implemented in CanonicalMinFunction!!!!");
    return (Potential) null;
  }
  public double PotValue(double arg[], Configuration conf) {
    /**@todo: implement this elvira.potential.Function abstract method*/
    System.out.println("Error: PotValue not implemented in CanonicalMinFunction!!!!");
    return 0.0;
  }

  /**
   * Return a column of the CPT; arg[] contains the set of columns corresponding
   * to the configuration involved; colSize is the number of States of the child
   * node involved
   */

  public double[] PotValues(double arg[], int colSize) {
    double[] dArray = new double[colSize];
    double[] theDArray = new double[arg.length];
    double[] theRArray = new double[colSize];

    for (int i=0; i<colSize; i++) {
      theRArray[i]=1.0;
    }

    for (int i=0; i<arg.length/colSize; i++) {
      theDArray[i*colSize]=arg[i*colSize];
      for (int j=1; j<colSize; j++) {
	theDArray[i*colSize+j]=arg[i*colSize+j]+theDArray[i*colSize+j-1];
      }
    }

    //theDArray[(arg.length/colSize-1)*colSize+colSize-1]=arg[(arg.length/colSize-1)*colSize+colSize-1];
    //for (int i=colSize-2; i>=0; i--) {
      //theDArray[(arg.length/colSize-1)*colSize+i]=arg[(arg.length/colSize-1)*colSize+i]+theDArray[(arg.length/colSize-1)*colSize+i+1];
    //}

    for (int i=0; i<arg.length/colSize; i++) {
      for (int j=0; j<colSize; j++) {
	theRArray[j]=theRArray[j]*theDArray[j+i*colSize];
      }
    }

    dArray[0]=theRArray[0];

    for (int i=1; i<colSize; i++) {
      dArray[i]=theRArray[i]-theRArray[i-1];
    }

    for (int i=0; i<dArray.length; i++) {
      double roundedValue = dArray[i];
      roundedValue = Math.round(roundedValue*100000);
      dArray[i]=roundedValue/100000;
    }

    return dArray;
  }
}