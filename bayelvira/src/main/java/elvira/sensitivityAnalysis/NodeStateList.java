/*NodeStateList*/

package elvira.sensitivityAnalysis;

import java.util.Vector;

/**
 * Clase para contener una lista de nombres de nodos y estados.
 * @author jruiz
 * @version 1.0
 */
public class NodeStateList{

  /**
   * Lista.
   */
  private Vector list;

  /**
   * Valor minimo del rango de la configuracion.
   */
  double minValue;

  /**
   * Valor maximo del rango de la configuracion.
   */
  double maxValue;

  /**
   * Valor de la configuracion.
   */
  double value;

  /**
   * Nombre de la configuracion.
   */
  String name;

  /**
   * ¿Es de una relacion de utilidad?.
   */
  boolean isUtility;

  /**
   * Nombre del nodo de cabeza.
   */
  String headNode;

  /**
   * Constructor por defecto.
   */
  public NodeStateList() {

    list = new Vector();
    minValue = 0.0;
    maxValue = 0.0;
    value = 0.0;
    name = null;
    isUtility = false;
    headNode = "";
  }

  /**
   * Crea una lista de nodos y estados a partir de un nodo y estado inicial.
   * @param ne Nodo y estado.
   */
  public NodeStateList(NodeState ne) {

    this();
    list.add(ne);
  }

  /**
   * Obtiene un nodo y estado en una posicion en la lista.
   * @param pos Posicion.
   * @return Nodo y estado.
   */
  public NodeState getNodeState(int pos) {

    return (NodeState)list.elementAt(pos);
  }

  /**
   * Pone un nodo y estado en una posicion de la lista.
   * @param ne Nodo y estado.
   * @param pos Posicion.
   */
  public void setNodeState(NodeState ne,int pos) {

    list.setElementAt(ne,pos);
  }

  /**
   * Anade un nodo y estado a la lista.
   * @param ne Nodo y estado.
   */
  public void addNodeState(NodeState ne) {

    list.add(ne);
  }

  /**
   * Elimina un nodo y estado de una posicion de la lista.
   * @param pos Posicion.
   */
  public void removeNodeState(int pos) {

    list.remove(pos);
  }

  /**
   * Devuelve el tamano de la lista.
   * @return tamano.
   */
  public int size() {

    return list.size();
  }

  /**
   * Copia la lista en otra lista.
   * @return Lista copiada.
   */
  public NodeStateList copy() {

    NodeStateList listaNE;
    int i;
    NodeState NE;
    NodeState NET;

    listaNE=new NodeStateList();

    for (i=0; i  <size(); i++) {
      NE = getNodeState(i);
      NET = new NodeState(NE.getName(), NE.getState());
      listaNE.addNodeState(NET);
    }

    listaNE.setMinValue(minValue);
    listaNE.setMaxValue(maxValue);
    listaNE.setValue(value);
    listaNE.isUtility(isUtility());
    listaNE.setHeadNode(headNode);
    return listaNE;
  }

  /**
   * Comprueba si dos listas son iguales.
   * @param listaNE Lista a comprobar.
   * @return True o false.
   */
  public boolean equal(NodeStateList listaNE) {

    boolean retorno;
    int i;
    NodeState NE1;
    NodeState NE2;

    retorno=true;

    if (listaNE.size() != size()) {
      retorno = false;
    } else {

      for(i=0; i < size(); i++) {
        NE1 = getNodeState(i);
        NE2 = listaNE.getNodeState(i);

        if (!NE1.getName().equals(NE2.getName()) || !NE1.getState().equals(NE2.getState())) {
          retorno = false;
          break;
        }

      }

    }

    return retorno;
  }

  /**
   * Pone el valor minimo del rango de la configuracion.
   * @param mv Valor minimo.
   */
  public void setMinValue(double mv) {

    minValue = mv;
  }

  /**
   * Extrae el valor minimo de la configuracion.
   * @return valor minimo.
   */
  public double getMinValue() {

    return minValue;
  }

  /**
   * Pone el valor maximo del rango de la configuracion.
   * @param mv Valor maximo.
   */
  public void setMaxValue(double mv) {

    maxValue = mv;
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
   * Extrae el valor maximo de la configuracion.
   * @return valor maximo.
   */
  public double getMaxValue() {

    return maxValue;
  }

  /**
   * Pone el valor de la configuracion.
   * @param v valor.
   */
  public void setValue(double v) {

    value = v;
  }

  /**
   * Extrae el valor de la configuracion.
   * @return Valor.
   */
  public double getValue() {

    return value;
  }

  /**
   * POne el nombre.
   * @param n
   */
  public void setName(String n) {

    name = n;
  }

  /**
   * Extrae el nombre.
   * @return
   */
  public String getName() {

    return name;
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
   * Comprueba si existe el nombre de un nodo en la lista de nodos y estados.
   * @param n Nombre a buscar.
   * @return True o false.
   */
  public boolean nodeExists(String n) {

    int i;
    NodeState nodoEstado;
    boolean retorno = false;

    for (i=0; i < size();i++) {
      nodoEstado = getNodeState(i);

      if (nodoEstado.getName().equals(n)) {
        retorno = true;
        break;
      }

    }

    return retorno;
  }

  /**
   * Comprueba si dos listas de nodos y estados tinen los mismos nodos.
   * @param nsl
   * @return
   */
  public boolean compatible(NodeStateList nsl) {

    boolean retorno = true;
    int i;

    if (size() != nsl.size()) retorno = false;
    else {

      for (i=0; i < size(); i++) {

        if (!getNodeState(i).getName().equals(nsl.getNodeState(i).getName())){
          retorno = false;
          break;
        }

      }

    }

    return retorno;
  }

  /**
   * Devuelve la posición de un nodo en la lista de nodos y estados.
   * @param n
   * @return
   */
  public int getPos(String n) {

    int i;
    int retorno = -1;

    for (i=0; i < size(); i++) {

      if (getNodeState(i).getName().equals(n)) {
        retorno = i;
        break;
      }

    }

    return retorno;
  }

  /**
   * Imprime la configuracion con sus valores.
   */
  public void print() {

    NodeState ne;
    int i;
    int j;

    if (getName() != null) System.out.println(getName() + " ");

    System.out.print("[ ");

    for(i=0; i < list.size() - 1; i++) {
      ne = getNodeState(i);
      System.out.print(ne.getName()+"="+ne.getState()+" , ");
    }

    for(j=list.size() - 1; j < list.size(); j++) {
      ne = getNodeState(j);
      System.out.print(ne.getName()+"="+ne.getState()+" ]");
      System.out.println(" "+getValue() + " " + getMinValue()+" "+getMaxValue());
    }

  }

} //End class