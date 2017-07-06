/*NodeState.java*/

package elvira.sensitivityAnalysis;

/**
 * Clase para contener el nombre de un nodo y un estado.
 * @author jruiz
 * @version 1.0
 */
public class NodeState{

  /**
   * Nombre del nodo.
   */
  String name;

  /**
   * Estado .
   */
  String state;

  /**
   * Constructor por defecto.
   */
  public NodeState() {

    name = "";
    state = "";
  }

  /**
   * Constructor con un nombre de nodo y su estado.
   * @param n Nombre del nodo.
   * @param e Estado.
   */
  public NodeState(String n, String e) {

    name = n;
    state = e;
  }

  /**
   * Extrae el nombre del nodo.
   * @return Nombre del nodo.
   */
  public String getName() {

    return name;
  }

  /**
   * Extrae el estado del nodo.
   * @return Estado del nodo.
   */
  public String getState() {

    return state;
  }

  /**
   * Pone el nombre del nodo.
   * @param n Nombre del nodo.
   */
  public void setName(String n) {

    name = n;
  }

  /**
   * Pone el estado del nodo.
   * @param e Estado del nodo.
   */
  public void setState(String e) {

    state = e;
    }

} //End of class