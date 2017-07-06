/*GeneralizedValues.java*/

package elvira.sensitivityAnalysis;

import java.util.Vector;

/**
 * Clase para contener y manipular los valores de los rangos y los nombres.
 * @author jruiz
 * @version 1.0
 */
public class GeneralizedValues {

  /**
   * Lista de rangos.
   */
  private Vector listRanges;

  /**
   * Lista de nombres.
   */
  private Vector listNames;

  /**
   * Constructor por defecto para inicializar las dos listas.
   */
  public GeneralizedValues() {

    listRanges = new Vector();
    listNames = new Vector();
  }

  /**
   * Constructor con un número determinado de valores.
   * @param numRanges
   */
  public GeneralizedValues(int numRanges) {

    int i;

    listRanges = new Vector();
    listNames = new Vector();
    for (i=0; i < numRanges; i++) {
      addRange(null);
      addName(null);
    }
  }

  /**
   * Constructor con un número determinado de valores.
   * @param numRanges
   */
  public GeneralizedValues(long numRanges) {

    int i;

    listRanges = new Vector();
    listNames = new Vector();
    for (i=0; i < numRanges; i++) {
      addRange(null);
      addName(null);
    }
  }

  /**
   * Añade un rango mediante un valor minimo y uno maximo.
   * @param min
   * @param max
   */
  public void addRange(double min,double max) {

    Range range = new Range(min,max);
    listRanges.add(range);
  }

  /**
   * Añade un rango mediante un objeto del tipo Range.
   * @param range
   */
  public void addRange(Range range) {

    listRanges.add(range);
  }

  /**
   * Añade un nombre.
   * @param name
   */
  public void addName(String name) {

    listNames.add(name);
  }

  /**
   * Extrae un rango.
   * @param pos
   * @return
   */
  public Range getRange(int pos) {

    return (Range)listRanges.elementAt(pos);
  }

  /**
   * Extrae un nombre.
   * @param pos
   * @return
   */
  public String getName(int pos) {

   return (String)listNames.elementAt(pos);
  }

  /**
   * Pone un rango mediante los valores maximo y minimo.
   * @param pos
   * @param min
   * @param max
   */
  public void setRange(int pos,double min,double max) {

    Range range = new Range(min,max);
    listRanges.setElementAt(range,pos);
  }

  /**
   * Pone un rango mediante un objeto del tipo Range.
   * @param range
   * @param pos
   */
  public void setRange(Range range,int pos) {

    listRanges.setElementAt(range,pos);
  }

  /**
   * Pone un nombre.
   * @param pos
   * @param name
   */
  public void setName(int pos, String name) {

    listNames.setElementAt(name,pos);
  }

  /**
   * Pone un nombre.
   * @param name
   * @param pos
   */
  public void setName(String name, int pos) {

    listNames.setElementAt(name,pos);
  }

  /**
   * Elimina un rango de la lista.
   * @param pos
   */
  public void removeRange(int pos) {

    listRanges.remove(pos);
  }

  /**
   * Elimina un nombre de la lista.
   * @param pos
   */
  public void removeName(int pos) {

    listNames.remove(pos);
  }

  /**
   * Extrae el minimo del rango de una posicion.
   * @param pos
   * @return
   */
  public double getMin(int pos) {

    Range range = (Range)listRanges.elementAt(pos);
    return range.getMin();
  }

  /**
   * Extrae el maximo del rango de una posicion.
   * @param pos
   * @return
   */
  public double getMax(int pos) {

    Range range = (Range)listRanges.elementAt(pos);
    return range.getMax();
  }

  /**
   * Tamaño de la lista de rangos.
   * @return
   */
  public int size() {

    return listRanges.size();
  }

  /**
   * Copia la clase.
   * @return
   */
  public GeneralizedValues copy() {

    Range m;
    String n;
    int i;
    GeneralizedValues gvs = new GeneralizedValues();

    for (i=0; i < size(); i++) {
      m = getRange(i);
      n = getName(i);
      gvs.addRange(m);
      gvs.addName(n);
    }
    return gvs;
  }

}//End of class