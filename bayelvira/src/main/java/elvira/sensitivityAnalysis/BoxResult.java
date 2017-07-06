/*BoxResult.java*/

package elvira.sensitivityAnalysis;

import java.util.Vector;

/**
 * Clase para guardar los resultados del analisis de sensibilidad.
 * @author jruiz
 * @version 1.0
 */
public class BoxResult {

  /**
   * Vector que va a contener elementos de la clase ProbUtil.
   */
  Vector data;

  /**
   * Contiene la probabilidad del elemento con maxima utilidad.
   */
  double probMax;

  /**
   * Contiene la probabilidad del elemento con minima utilidad.
   */
  double probMin;

  /**
   * Contiene la maxima utilidad de los datos.
   */
  double maxUtility;

  /**
   * Conteine la minima utilidad de los datos.
   */
  double minUtility;

  /**
   * Crea una nueva caja de resultados.
   */
  public BoxResult() {

    data = new Vector();
    probMax = 0.0;
    probMin = 0.0;
    maxUtility = 0.0;
    minUtility = 0.0;
  }

  /**
   * Crea una nueva caja de resultados a partir de un vector de datos inicial.
   * @param d vector de datos inicial.
   */
  public BoxResult(Vector d) {

    this();
    data = d;
    maxUtility = getMaximoU();
    minUtility = getMinimoU();
    probMax = getProbMaxUtil();
    probMin = getProbMinUtil();
  }

  /**
   * Devuelve el vector de datos.
   * @return Vector de datos.
   */
  public Vector getData() {

    return data;
  }

  /**
   * Devuelve la probabilidad del elemento de maxima utilidad.
   * @return Probabilidad de la maxima utilidad.
   */
  public double getMaxProb() {

    return probMax;
  }

  /**
   * Devuelve la probabilidad del elemento de minima utilidad.
   * @return Probabilidad de la minima utilidad.
   */
  public double getMinProb() {

    return probMin;
  }

  /**
   * Devuelve la maxima utilidad.
   * @return Maxima utilidad.
   */
  public double getMaxUtil() {

    return maxUtility;
  }

  /**
   * Devuelve la minima utilidad.
   * @return Minima utilidad.
   */
  public double getMinUtil() {

    return minUtility;
  }

  /**
   * Calcula el maximo de todos los elementos.
   * @return Maximo.
   */
  private double getMaximoU() {

    double max = Double.NEGATIVE_INFINITY;
    ProbUtil pu;
    int i;

    for (i=0; i < data.size(); i++) {
      pu = (ProbUtil)data.elementAt(i);

      if (pu.getUtility() > max) {
        max = pu.getUtility();
      }

    }

    return max;
  }

  /**
   * Calcula el minimo de todos los elementos.
   * @return Minimo.
   */
  private double getMinimoU() {

    double min = Double.POSITIVE_INFINITY;
    ProbUtil pu;
    int i;

    for (i=0; i < data.size(); i++) {
      pu = (ProbUtil)data.elementAt(i);

      if (pu.getUtility() < min) {
        min = pu.getUtility();
      }

    }

    return min;
  }

  /**
   * Calcula la probabilidad del maximo.
   * @return Probabilidad del maximo.
   */
  private double getProbMaxUtil() {

    int i = 0;
    ProbUtil pu;
    double max;

    pu = new ProbUtil();
    max = getMaximoU();

    for (i=0; i < data.size(); i++) {
      pu = (ProbUtil)data.elementAt(i);

      if (max == pu.getUtility()) {
        break;
      }

    }

    return pu.getProbability();
  }

  /**
   * Calcula la probabilidad del minimo.
   * @return Probabilidad del minimo.
   */
  private double getProbMinUtil() {

    int i = 0;
    ProbUtil pu;
    double min;

    pu = new ProbUtil();
    min = getMinimoU();

    for (i=0; i < data.size(); i++) {
      pu = (ProbUtil)data.elementAt(i);

      if (min == pu.getUtility()) {
        break;
      }

    }

    return pu.getProbability();
  }

} //End of class