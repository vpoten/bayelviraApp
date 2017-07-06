/*RangeBoxList.java*/

package elvira.sensitivityAnalysis;

import java.util.Vector;

/**
 * Clase para contener una lista de cajas con entradas y resultados.
 * @author jruiz
 * @version 1.0
 */
public class RangeBoxList {

  /**
   * Lista.
   */
  private Vector list;

  /**
   * Constructor por defecto.
   */

  public RangeBoxList() {

    list = new Vector();
  }

  /**
   * Crea una nueva lista con una caja de rangos.
   * @param rangeBox Caja de entrada y resultados.
   */
  public RangeBoxList(RangeBox rangeBox) {

    this();
    list.add(rangeBox);
  }

  /**
   * Crea una nueva lista con una caja de entrada y otra de resultados.
   * @param entrada Entrada.
   * @param resultado resultados.
   */
  public RangeBoxList(BoxEntry entrada,BoxResult resultado) {

    this();
    RangeBox rangeBox = new RangeBox(entrada,resultado);
    list.add(rangeBox);
  }

  /**
   * Extrae una posicion de la lista.
   * @param pos Posicion.
   * @return Caja de rangos.
   */
  public RangeBox getRangeBox(int pos) {

    return (RangeBox)list.elementAt(pos);
  }

  /**
   * Pone una caja en una posicion.
   * @param r caja de rangos.
   * @param pos Posicion.
   */
  public void setRangeBox(RangeBox r,int pos) {

    list.setElementAt(r,pos);
  }

  /**
   * Anade un nuevo elemento a la lista.
   * @param r caja de rangos.
   */
  public void addRangeBox(RangeBox r) {

    list.add(r);
  }

  /**
   * Borra un elemento de la lista senalado.
   * @param pos Posicion a eliminar.
   */
  public void removeRangeBox(int pos) {

    list.remove(pos);
  }

  /**
   * Ordena la caja de rangos de mayor a menor amplitud en el rango de utilidades.
   * @return Nueva caja de rangos ordenada.
   */
  public RangeBoxList ordenarResultados() {

    RangeBoxList salida;
    BoxResult resultado;
    int i = 0;
    int j;
    RangeBox rango = new RangeBox();
    double distancia;
    double d;
    int orden;

    salida = new RangeBoxList();

    while (size()>0) {
      distancia = 0.0;
      orden = 0;

      for(j=0 ; j < size(); j++) {
        rango = getRangeBox(j);
        resultado = rango.getBoxResult();
        d = resultado.getMaxUtil() - resultado.getMinUtil();

        if (d > distancia) {
          distancia = d;
          orden = j;
        }

      }

      salida.addRangeBox(getRangeBox(orden));
      removeRangeBox(orden);
    }

    return salida;
  }

  /**
   * Copia una lista de RangeBox.
   * @return
   */
  public RangeBoxList copy() {

    RangeBoxList rbl = new RangeBoxList();
    RangeBox rb;
    int i;

    for(i=0; i < size(); i++) {
      rb = getRangeBox(i);
      rbl.addRangeBox(rb);
    }

    return rbl;
  }

  /**
   * Devuelve el tamano de la lista.
   * @return Tamano.
   */
  public int size() {

    return list.size();
  }

} //End of class