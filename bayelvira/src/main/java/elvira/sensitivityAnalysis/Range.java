package elvira.sensitivityAnalysis;

/**
 * Clase que contiene los rangos.
 * @author jruiz
 * @version 1.0
 */
public class Range {

  /**
   * Valor minimo.
   */
  private double min;

  /**
   * Valor maximo.
   */
  private double max;

  /**
   * Constructor por defecto.
   */
  public Range() {
    min=0.0;
    max=1.0;
  }

  /**
   * Constructor con los valores minimo y maximo.
   * @param m Minimo.
   * @param n Maximo.
   */
  public Range(double m,double n) {
    min=m;
    max=n;
  }

  /**
   * Pone el minimo.
   * @param m
   */
  public void setMin(double m) {
    min=m;
  }

  /**
   * Pone el maximo.
   * @param m
   */
  public void setMax(double m) {
    max=m;
  }

  /**
   * Extrae el minimo.
   * @return
   */
  public double getMin() {
    return min;
  }

  /**
   * Extrae el maximo.
   * @return
   */
  public double getMax() {
    return max;
  }

}//End of class
