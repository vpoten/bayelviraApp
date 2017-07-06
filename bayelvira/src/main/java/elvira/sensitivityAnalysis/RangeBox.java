/*RangeBox.java*/

package elvira.sensitivityAnalysis;

import java.awt.Color;

/**
 * Clase para contener cajas de entradas y resultados al aplicar el algorimo ArcReversalSV.
 * @author jruiz
 * @version 1.0
 */
public class RangeBox {

  /**
   * Caja de entrada.
   */
  BoxEntry entry;

  /**
   * Caja de resultados.
   */
  BoxResult result;

  /**
   * Numero de orden . Se utiliza en algunos casos.
   */
  int order;

  /**
   * Color que se utilizará en los gráficos de los análisis.
   */
  Color color;

  /**
   * Constructor por defecto.
   */
  public RangeBox() {

    entry = new BoxEntry();
    result = new BoxResult();
    order = -1;
    color = Color.white;
  }

  /**
   * Constructor con una caja de entrada y otra de resultados.
   * @param e Entrada.
   * @param r Resultados.
   */
  public RangeBox(BoxEntry e, BoxResult r) {

    entry = e;
    result=r;
  }

  /**
   * Extrae la caja de entrada.
   * @return Caja de entrada.
   */
  public BoxEntry getBoxEntry() {

    return entry;
  }

  /**
   * Extrae la caja de resultados.
   * @return Caja de resultados.
   */
  public BoxResult getBoxResult() {

    return result;
  }

  /**
   * Pone una caja de entrada.
   * @param e Caja de entrada.
   */
  public void setBoxEntry(BoxEntry e) {

    entry = e;
  }

  /**
   * Pone una caja de resultados.
   * @param r Caja de resultados.
   */
  public void setBoxResult(BoxResult r) {

    result = r;
  }

  /**
   * Extrae un numero de orden.
   * @return
   */
  public int getOrder() {

    return order;
  }

  /**
   * Pone un numero de orden.
   * @param o
   */
  public void setOrder(int o) {

    order = o ;
  }
  /**
   * Extrae el color.
   * @return
   */
  public Color getColor() {

    return color;
  }

  /**
   * Pone el color.
   * @param c
   */
  public void setColor(Color c) {

    color = c ;
  }

} //End class