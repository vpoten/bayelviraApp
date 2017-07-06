/*ProbUtil.java*/

package elvira.sensitivityAnalysis;

/**
 * Clase para contener parejas de probabilidades y utilidades.
 * @author jruiz
 * @version 1.0
 */
public class ProbUtil {

  /**
   * Probabilidad.
   */
  double probability;

  /**
   * Utilidad.
   */
  double utility;

  /**
   * Constructor inicial.
   */
  public ProbUtil() {

    probability = 0.0;
    utility = 0.0;
  }

  /**
   * Crea un objeto y pone probabilidad y utilidad.
   * @param p Probabilidad
   * @param u Utilidad.
   */
  public ProbUtil(double p, double u) {

    probability = p;
    utility = u;
  }

  /**
   * Pone probabilidad.
   * @param p Probabilidad.
   */
  public void setProbability(double p) {

    probability = p;
  }

  /**
   * Pone utilidad.
   * @param u Utilidad.
   */
  public void setUtility(double u) {

    utility = u;
  }

  /**
   * Extrae probabilidad.
   * @return Probabilidad.
   */
  public double getProbability() {

    return probability;
  }

  /**
   * Extrae utilidad.
   * @return Utilidad.
   */
  public double getUtility() {

    return utility;
  }

} //End of class