/*
 * PotentialInterval.java
 *
 * Created on 20 de abril de 2004, 18:47
 */

package elvira.potential;


import elvira.FiniteStates;
import java.util.Vector;

/**
 * This is the abtract superclass for Potentials that represents intervals of probability
 * @author  Andrés Cano (acu@decsai.ugr.es)
 */
public abstract class PotentialInterval extends Potential {
  static private final double EPSILON=0.000001;
    
  abstract double[] getArrayCopyMinValues();
  abstract double[] getArrayCopyMaxValues();
  

  /**
   * Gets the extrems points from the set of intervals in this intervalSet. The potential
   * intervalSet must contain only one FiniteState variable
   * @return a Vector with the extrem points. Each component in the Vector is an array of 
   * double values (one double for each state of the FiniteState variable).
   */
  public Vector getListExtrems()
  //get_list_extrems in de Campos algorithm
  {
    int i,current;
    double landa; // the amount of probability that has not yet been assigned
    FiniteStates var=(FiniteStates)(getVariables().elementAt(0));
    int num_cases=var.getNumStates();
    boolean expl[]=new boolean[num_cases];
    double p[]=new double[num_cases];
    double low[]=getArrayCopyMinValues();
    double upper[]=getArrayCopyMaxValues();
    Vector extremPoints=new Vector();
    
    if(!avoidsSecureLoss(low, upper)){
      System.out.println("Error in PotentialInterval.getListExtrems(): Intervals do not avoid secure loss");
      System.exit(1);
    }
    for (i = 0; i < num_cases; i++)
      expl[i] = false;
    current = 0;
    for (i = 0; i < num_cases; i++)
      p[i] = low[i];
    landa = 0.0;
    for (i = 0; i < num_cases; i++)
      landa += low[i];
    landa = 1 - landa;
    if (landa < EPSILON) {
      /*for (i = 0; i < num_cases; i++) {
        printf("%6.4f", p[i]);
        putchar(' ');
      }
      printf("\n");*/
      double p_aux[]=new double[num_cases];
      System.arraycopy(p,0, p_aux,0,num_cases);
      extremPoints.addElement(p_aux);
    } 
    else {
      getProbListExtrems(p, landa, expl, current,num_cases,low,upper,extremPoints);
    }
    return extremPoints;
  }
 
  /**
   * Gets the extreme points from the set of intervals in the arrays of probability, upper and low. It is
   * an auxliary method used by <code>Vector getListExtrems()</code>. This is a recursive
   * method calculates and appends the extreme points to extremPoints.
   * @param p the extreme point that is being calculated
   * @param landa the amount of probability that has not yet been assigned.
   * @param expl array to determine the already explored cases.
   * @param current the index chat is being currently explored
   * @param num_cases number of cases of the variable
   * @param low the low values in the intervals
   * @param upper the upper values in the intervals
   * @param extremPoins a Vector where the extrem points are inserted (list of extreme points found so far).
   * Each component in the Vector is an array of
   * double values (one double for each state of the FiniteState variable).
   */
  private void getProbListExtrems(double p[],double landa,boolean expl[],int current,
  int num_cases,double low[],double upper[],Vector extremPoints)
  //getprob_list_extrems in de Campos algorithm
  {
    int i,j;
    double p_aux[]=new double[num_cases];
    
    System.arraycopy(p,0, p_aux, 0,num_cases);
    for (i = 0; i < num_cases; i++) {
      if (!belongListExtrems(i + 1, expl)) {
        if (landa < upper[i] - low[i] - EPSILON) {
          double auxp[]=new double[num_cases];
          System.arraycopy(p_aux,0, auxp,0,num_cases);
          auxp[i] += landa;
          extremPoints.addElement(auxp);
        }
        else if (i + 1 > current) {
          if (landa - upper[i] + low[i] > EPSILON && upper[i] - low[i] > EPSILON) {
            boolean auxexpl[]=new boolean[num_cases];
            double auxp[]=new double[num_cases];
            System.arraycopy(p_aux,0, auxp, 0,num_cases);
            auxp[i] = upper[i];
            System.arraycopy(expl,0,auxexpl,0,num_cases);
            auxexpl[i] = true;
            getProbListExtrems(auxp, landa - upper[i] + low[i], auxexpl, i + 1,
            num_cases,low,upper,extremPoints);
          }
          else if (upper[i] - low[i] > EPSILON) {
            double auxp[]=new double[num_cases];
            System.arraycopy(p,0, auxp, 0,num_cases);
            auxp[i] += landa;
            extremPoints.addElement(auxp);
          }
        }
      }
    }
  }

  /**
   * Tells if a node (case) has been yet already explored
   * @return true if the node has been already explored
   */
  private boolean belongListExtrems(int i,boolean expl[])
  // belong_list_extrems in de Campos algorithm
  {
    return (expl[i - 1]);
  }
  
  /** 
   * Tells if a set of intervals avoids secure loss (propios), (that is, if contains at least
   * one extreme points). If the intervals avoids secure loss, but are not coherent 
   * (alcanzables), then they are transformed into coherent intervals (that is, the natural
   * extension is obtained).
   * @param low array of low limits of the intervals
   * @param upper array of upper limits of the intervals
   * @return true if the intervals avoids secure loss. else otherwise.
   */
  public boolean avoidsSecureLoss(double low[],double upper[])
  // compruebainterv_list_extrems in de Campos algorithm
  {
    int j;
    double inf, sup;
    boolean alcanza;
    int num_cases=low.length;
    double laux[]=new double[num_cases];
    double uaux[]=new double[num_cases];
    
    inf = 0.0;
    sup = 0.0;
    for (j = 0; j < num_cases; j++) {
      inf += low[j];
      sup += upper[j];
    }
    if (inf > 1 + EPSILON || sup < 1 - EPSILON) {
      System.out.println("Error in PotentialInterval.avoidsSecureLoss(double[],double[]): Intervals do not avoid secure loss (no son propios)");
      return false;
    }
    j = 0;
    alcanza = true;
    while (alcanza && j < num_cases) {
      j++;
      if (inf - low[j - 1] + upper[j - 1] > 1 + EPSILON ||
      sup - upper[j - 1] + low[j - 1] < 1 - EPSILON) {
        System.out.print("Warning in PotentialInterval.avoidsSecureLoss(double[],double[]): Intervals are not coherent (no son alcanzables)");
        System.out.println(" They will be transfomed into coherent intervals (natural extension is obtained)");
        alcanza = false;
      }
    }
    if (!alcanza) {
      for (j = 0; j < num_cases; j++) {
        laux[j] = low[j];
        uaux[j] = upper[j];
      }
      for (j = 0; j < num_cases; j++) {
        if (sup - upper[j] + low[j] < 1 - EPSILON)
          laux[j] = 1 - sup + upper[j];
        if (inf - low[j] + upper[j] > 1 + EPSILON)
          uaux[j] = 1 - inf + low[j];
      }
      for (j = 0; j < num_cases; j++) {
        low[j] = laux[j];
        upper[j] = uaux[j];
      }
    }
    return true;
  }
}
