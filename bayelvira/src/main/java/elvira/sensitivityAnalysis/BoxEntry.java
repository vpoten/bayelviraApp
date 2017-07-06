/*BoxEntry.java*/

package elvira.sensitivityAnalysis;

/**
 * Esta clase contiene los datos necesarios para pasar al algoritmo de evaluacion
 * de diagramas de influencia ArcReversalSV
 * para una configuracion de nodos y estados concreta.
 * @author jruiz
 * @version 1.0
 */
public class BoxEntry {

  /**
   * Lista de nodos con sus estados.
   */
  NodeStateList list;

  /**
   * Es el minimo valor del rango.
   */
  double minValue;

  /**
   * Es el maximo valor del rango.
   */
  double maxValue;

  /**
   * Es el numero de tomas de datos entre los valores minimo y maximo del rango.
   */
  double step;

  /**
   * ¿Es de utilidad?.
   */
  boolean isUtility;

  /**
   * Nombre del nodo de cabeza.
   */
  String headNode;

  /**
   * Crea una nueva entrada.
   */
  public BoxEntry() {

    list = new NodeStateList();
    minValue = 0;
    maxValue = 0;
    step = 0;
    isUtility = false;
    headNode = "";
  }

  /**
   * Crea una entrada a partir de una lista de nodos y estados y un paso.
   * @param l Lista de ndos y estados.
   * @param p Paso.
   */
  public BoxEntry(NodeStateList l, double p) {

    this();
    list = l;
    minValue = l.getMinValue();
    maxValue = l.getMaxValue();
    step = p;
    isUtility = l.isUtility();
    headNode = l.getHeadNode();
  }

  /**
   * Devuelve la lista de nodos y estados.
   * @return Lista de nodos y estados.
   */
  public NodeStateList getNodeStateList() {

    return list;
  }

  /**
   * Devuelve el valor minimo del rango de entrada.
   * @return Valor minimo del rango.
   */
  public double getMinValue() {

    return minValue;
  }

  /**
   * Devuelve el valor maximo del rango de entrada.
   * @return Valor maximo del rango.
   */
  public double getMaxValue() {

    return maxValue;
  }

  /**
   * Pone el nombre del nodo de cabeza.
   * @param hNode
   */
  public void setHeadNode(String hNode) {

    headNode = hNode;
  }

  /**
   * Extrae el nombre del nodo de cabeza.
   * @return 
   */
  public String getHeadNode() {

    return headNode;
  }

  /**
   * Devuelve el paso.
   * @return Paso.
   */
  public double getStep() {

    return step;
  }

  /**
   * Es de utilidad.
   * @param u
   */
  public void isUtility(boolean u) {

    isUtility = u;
  }

  /**
   * ¿Es de utilidad?.
   * @return
   */
  public boolean isUtility() {

    return isUtility;
  }

  /**
   * Imprime la lista de nodos y estados.
   */
  public void print() {

    list.print();
  }

} //End of class
