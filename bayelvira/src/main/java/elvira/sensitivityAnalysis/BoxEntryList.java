/*BoxEntryList.java*/

package elvira.sensitivityAnalysis;

import java.util.Vector;

/**
 * Clase que contiene una lista de datos de entrada.
 * @author jruiz
 * @version 1.0
 */
public class BoxEntryList {

  /**
   * Lista de cajas de entrada.
   */
  private Vector list;

  /**
   * Crea una lista nueva.
   */
  public BoxEntryList() {

    list = new Vector();
  }

  /**
   * Crea una lista nueva con una caja de entrada inicial.
   * @param entrada Caja inicial.
   */
  public BoxEntryList(BoxEntry entrada) {

    this();
    list.add(entrada);
  }

  /**
   * Pone en una posicion concreta una caja de entrada.
   * @param entrada Caja de entrada.
   * @param pos Posicion.
   */
  public void setBoxEntry(BoxEntry entrada, int pos) {

    list.setElementAt(entrada,pos);
  }

  /**
   * Extrae la caja de entrada en una posicion concreta.
   * @param pos Posicion.
   * @return Devuelve la caja de entrada.
   */
  public BoxEntry getBoxEntry(int pos) {

    return (BoxEntry)list.elementAt(pos);
  }

  /**
   * Anade una nueva caja de entrada.
   * @param entrada Caja de entrada.
   */
  public void addBoxEntry(BoxEntry entrada) {

    list.add(entrada);
  }

  /**
   * Devuelve el tamano de la lista.
   * @return Tamano.
   */
  public int size() {

    return list.size();
  }

} //End of class