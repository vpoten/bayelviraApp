/*ConfigurationList.java*/

package elvira.sensitivityAnalysis;

import java.util.Vector;

/**
 * Contiene una lista de configuraciones de nodos y estados.
 * @author jruiz
 * @version 1.0
 */
public class ConfigurationList {

  /**
   * Lista de configuraciones.
   */
  private Vector list;

  /**
   * Crea una nueva lista de configuraciones.
   */
  public ConfigurationList() {

    list = new Vector();
  }

  /**
   * Crea una nueva lista de configuraciones a partir de una lista de nodos y estados.
   * @param nsl Lista de nodos y estados.
   */
  public ConfigurationList(NodeStateList nsl) {

    this();
    list.add(nsl);
  }

  /**
   * Pone una configuracion en una posicion de la lista.
   * @param nsl onfiguracion a poner.
   * @param pos Posicion.
   */
  public void setNodeStateList(NodeStateList nsl, int pos) {

    list.setElementAt(nsl,pos);
  }

  /**
   * Anade una nueva configuracion a la lista.
   * @param nsl Configuracion a anadir.
   */
  public void addNodeStateList(NodeStateList nsl) {

    list.add(nsl);
  }

  /**
   * Extrae una configuracion de la lista.
   * @param pos Posicion.
   * @return Configuracion.
   */
  public NodeStateList getNodeStateList(int pos) {

    return (NodeStateList)list.elementAt(pos);
  }

  /**
   * Elimina una configuracion de la lista.
   * @param pos Posicion.
   */
  public void removeNodeStateList(int pos) {

    list.remove(pos);
  }

  /**
   * Comprueba si existe una configuracion en la lista.
   * @param listaNE Lista de nodos y estados.
   * @return True o false.
   */
  public boolean existNodeStateList(NodeStateList listaNE) {

    boolean retorno=false;
    int i;

    for (i=0; i < size(); i++) {

      if (getNodeStateList(i).equal(listaNE)) {
        retorno = true;
        break;
      }

    }

    return retorno;
  }

  /**
   * Tamano de la lista.
   * @return tamano.
   */
  public int size() {

    return list.size();
  }

  /**
   * Copia una lista en otra nueva.
   * @return Copia de la lista.
   */
  public ConfigurationList copy() {

    ConfigurationList listaC;
    int i;
    int j;
    NodeStateList listaNE;
    NodeStateList listaN;
    NodeState NE;
    NodeState n;

    listaC = new ConfigurationList();

    for( i=0; i < size(); i++) {
      listaNE = getNodeStateList(i);
      listaN = new NodeStateList();

      for (j=0; j < listaNE.size(); j++) {
        NE = listaNE.getNodeState(j);
        n = new NodeState(NE.getName(),NE.getState());
        listaN.addNodeState(n);
      }

      listaC.addNodeStateList(listaN);
    }

    return listaC;
  }

  /**
   * Anade una lista de configuraciones .
   * @param listaC Lista a anadir
   */
  public void addConfigurationList(ConfigurationList listaC) {

    int i;

    for(i=0; i < listaC.size(); i++) {
      addNodeStateList(listaC.getNodeStateList(i));
    }

  }

  /**
   * Imprime la lista de configuraciones.
   */
  public void print() {

    NodeStateList listaNE;
    int i;

    for (i=0; i < list.size(); i++) {
      listaNE = getNodeStateList(i);
      listaNE.print();
    }

  }

} //End of class