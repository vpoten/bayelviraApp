/* Continuous.java */

package elvira;

import java.util.Vector;
import java.util.Locale;
import elvira.Node;

import java.io.*;
import java.text.DecimalFormat;


/**
 * Implements the class of nodes corresponding to
 * continuous variables (for random and utility nodes).
 * The objects of this type can be variables and value nodes.
 * The type of variable will be CONTINUOUS.
 *
 * @since 19/9/2000
 */

public class Continuous extends Node implements Cloneable, Serializable {

/**
 * Lower limit of the interval where the variable takes values.
 */
private double min;

/**
 * Upper limit of the interval where the variable takes values.
 */
private double max;

/**
 * Number of decimal digits used to represent the values.
 */
private int precision;

/**
 * Unit of the variable (for instance, seconds, km/h, ...)
 */
private String unit;


/**
  * The value for an undefined variable
  */

private double undefVal=Double.NaN;

/**
 * Creates a new empty <code>Continuous</code> object.
 */

public Continuous() {

  super();
  setTypeOfVariable(CONTINUOUS);
  setPrecision(2);
  setMin(0.0);
  setMax(1.0);
  setUnit("");
  setUndefVal(Double.NaN);
}


/**
 * Creates a new <code>Continuous</code> object.
 * @param fm a font.
 */

public Continuous(String fm) {

  super(fm);
  setTypeOfVariable(CONTINUOUS);
  setPrecision(2);
  setMin(0.0);
  setMax(1.0);
  setUnit("");
  setUndefVal(Double.NaN);
}


/**
 * Creates a new <code>Continuous</code> object with name and position.
 * @param n the name of the variable.
 * @param x coordinate in the x axis.
 * @param y coordinate in the y axis.
 * @param fm a font.
 */

public Continuous(String n, int x, int y, String fm) {

  this (fm);

  setName(n);

  setPosX(x);
  setPosY(y);
  setPrecision(2);
  setMin(0.0);
  setMax(1.0);
  setUnit("");
}


/**
 * Creates a new <code>Continuous</code> object with the given parameters.
 * @param n the name of the variable.
 * @param x coordinate in the x axis.
 * @param y coordinate in the y axis.
 * @param fm a font.
 * @param p the precision.
 * @param u the lower limit of the interval.
 * @param v the upper limit of the interval.
 */

public Continuous(String n, int x, int y, String fm, int p, double u,
		  double v) {

  this (n,x,y,fm);
  setPrecision(p);
  setMin(u);
  setMax(v);
}


/* ****************** Access methods **************** */

/**
 * Gets the minimum possible value of the variable.
 * @return the minimum value for the variable.
 */

public double getMin() {

  return min;
}


/**
 * Gets the maximum possible value of the variable.
 * @return the maximun value for the variable.
 */

public double getMax() {

  return max;
}


/**
 * Gets the precision.
 * @return the precision of the variable.
 */

public int getPrecision() {

  return precision;
}


/**
 * Gets the unit.
 * @return the unir of measure.
 */

public String getUnit() {

  return unit;
}



/************************ Modifiers *********************/

/**
 * To set the minimum value for the variable.
 * @param minimun the minimum.
 */

public void setMin(double minimum) {

  min = minimum;
  //undefVal = min-1.0;
}


/**
 * To set the maximum value for the variable.
 * @param maximun the maximum.
 */

public void setMax(double maximum) {

  max = maximum;
}


/**
 * To set the undefined value for this node.
 * @param undefVal the new undefined value.
 */

public void setUndefVal(double undefVal) {
  this.undefVal = undefVal;
}

/**
 * Sets the precision of the variable.
 * @param i the new precision.
 */

public void setPrecision(int i) {

  precision = i;
}


/**
 * Sets the unit of measure of the variable.
 * @param s the new unit.
 */

public void setUnit(String s) {
  if (s.equals("")) {
    unit = new String(s);
    return;
  }
  if (s.substring(0,1).equals("\"")) {
    unit = new String(s.substring(1,s.length()-1));
  }
  else {
    unit = new String(s);
  }
}


/**
 * Saves the information about this object using the
 * given text output stream.
 * @param p a <code>PrintWriter</code> (the file).
 */

public void save(PrintWriter p) {

  p.print("node "+getName()+"(continuous)"+ " {\n");

  super.save(p);
  DecimalFormat format=(DecimalFormat)DecimalFormat.getInstance(Locale.ENGLISH);
  format.setGroupingUsed(false);
    

  p.print("min = " + format.format(min) + ";\n");
  p.print("max = " + format.format(max) + ";\n");
    
  p.print("precision = " + precision + ";\n");


  if ((getUnit()!=null) && (!getUnit().equals("")))
    p.print("unit = \""+ getUnit()+"\";\n");
  p.print("}\n\n");
}


/**
 * This method creates a new node equal to this but the list of
 * links of its parents, children and siblings are empty.
 */

public Node copy() {

  Continuous n;

  n = (Continuous) super.copy();
  n.setTypeOfVariable(CONTINUOUS);
  n.setMax(max);
  n.setMin(min);
  n.setPrecision(precision);
  n.setUnit(unit);
  return n;
}

public double undefValue(){
  return undefVal;
}

public static boolean isUndefined(double value){
   return Double.isNaN(value);
}


}  // End of class.
