/*SensitivityAnalysis.java*/

package elvira.sensitivityAnalysis;

import elvira.Network;
import elvira.IDiagram;
import elvira.RelationList;
import elvira.Relation;
import elvira.NodeList;
import elvira.Node;
import elvira.Configuration;
import elvira.Elvira;
import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.Bnet;
import java.io.IOException;
import java.io.File;
import java.util.Vector;
import elvira.inference.super_value.ArcReversalSV;
import java.text.DateFormat;
import javax.swing.UIManager;
import java.util.Date;
import elvira.gui.ElviraFrame;
import java.util.ResourceBundle;
import elvira.potential.PotentialTable;

/**
 * Clase principal.
 *
 * @author jruiz
 * @version 1.0
 */
public class SensitivityAnalysis {

  /**
   * Atributos para la internacionalizacion
   */
  private static int language;
  public static final int SPANISH = 0;
  public static final int AMERICAN = 1;

  /**
   * Numero de tomas de valores para cada rango .
   */
  public static int NUM_STEPS = 10;

  /**
   * Frame de red.
   */
  ElviraFrame elvFrame;

  /**
   * Red.
   */
  Network net = null;

  /**
   * Diagrama.
   */
  IDiagram iDiag = null;

  /**
   * Ruta a la red.
   */
  String netPath;

  /**
   * ¿Se arranca como programa independiente?
   */
  boolean main = true;

  /**
   * Contains the menu strings for the languaje selected
   */
  ResourceBundle menuBundle;

  /**
   * Contains the dialog strings for the languaje selected
   */
  ResourceBundle dialogBundle;

  /**
   * Constructor por defecto . Si m==true , es que se ejecuta de manera independiente de Elvira .
   * @param m
   * @throws elvira.parser.ParseException
   * @throws IOException
   */
  public SensitivityAnalysis(boolean m) throws elvira.parser.ParseException,IOException {

    setDialogBundle(ResourceBundle.getBundle(
        "elvira/localize/Dialogs_sp"));
    setMenuBundle(ResourceBundle.getBundle(
        "elvira/localize/Menus_sp"));
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {}

    MainFrame miFrame;
    if (!m) {
      main = false;
      miFrame = new MainFrame(this);
    }
  }

  /**
   * Constructor con una red.
   * @param ef frame de red.
   * @throws elvira.parser.ParseException
   * @throws IOException
   */
  public SensitivityAnalysis(ElviraFrame ef) throws elvira.parser.ParseException,IOException {

    String name;
    RelationList listaRelaciones;
    Relation relacion;
    GeneralizedPotentialTable pot;

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {}

    elvFrame = ef;
    menuBundle = Elvira.getElviraFrame().getMenuBundle();
    dialogBundle = Elvira.getElviraFrame().getDialogBundle();
    net = elvFrame.getNetworkFrame().getEditorPanel().getBayesNet();
    name = elvFrame.getNetworkFrame().getTitle();
    iDiag = (IDiagram)net;
    setNetPath(name);

    listaRelaciones = iDiag.getInitialRelations();

    if(elvFrame.getNetworkFrame().isNew()){
      try {
         getElvFrame().reopenNetwork((Bnet)getDiag(),getNetPath(),true);  
         net = elvFrame.getNetworkFrame().getEditorPanel().getBayesNet();
         name = elvFrame.getNetworkFrame().getTitle();
         iDiag = (IDiagram)net;
         setNetPath(name);
         listaRelaciones = iDiag.getInitialRelations();
      } catch (Exception e1) {}
    }

    for (int i=0;i<listaRelaciones.size();i++) {
      relacion = listaRelaciones.elementAt(i);

      if (relacion.getValues().getClass() == GeneralizedPotentialTable.class) {
        pot = (GeneralizedPotentialTable) relacion.getValues();
        pot.complementValues();
      } else if (relacion.getValues().getClass() == PotentialTable.class) {
        pot = new GeneralizedPotentialTable((PotentialTable)relacion.getValues());
        relacion.setValues(pot);
        listaRelaciones.setElementAt(relacion,i);
      }
      
    }

    iDiag.setRelationList(listaRelaciones.getRelations());

    main = false;
    MainFrame miFrame = new MainFrame(this);
  }

  /**
   * Obtiene una lista de relaciones en las que uno de los nodos tenga un cierto nombre.
   * @param relationList
   * @param nodeName
   * @return
   */
  public RelationList getRelationsByNode(RelationList relationList,String nodeName) {

    int i ;
    int j;
    RelationList devRelaciones;
    Node nodo;
    NodeList nodeList;
    Relation relacion;
    boolean anadir = false;

    devRelaciones = new RelationList();

    for(i=0; i < relationList.size(); i++) {
      anadir = false;
      relacion = relationList.elementAt(i);
      nodeList = relacion.getVariables();

      for(j=0; j < nodeList.size(); j++) {
        nodo = nodeList.elementAt(j);

        if (nodo.getName().equals(nodeName)){
          anadir = true;
          break;
        }

      }

      if (anadir) {
        devRelaciones.insertRelation(relacion);
      }

    }

    return devRelaciones;
  }

  /**
   * Extrae el camino a la red.
   * @return camino.
   */
  public String getNetPath() {

    return netPath;
  }

  /**
   * Pone el camino de la red.
   * @param path Camino.
   */
  public void setNetPath(String path) {

    netPath = path;
  }

  /**
   * Obtiene la maxima probabilidad que hay en una relacion
   * @param rel Es la relacion donde realizamos los calculos
   * @return Es el valor de la maxima probabilidad en la relacion
   */
  public double maxPotential(Relation rel) {

    PotentialTable pot;
    Configuration conf;
    double retorno = 0;
    Vector variables;
    int i;
    Relation relation;

    relation = rel.copy();
    pot = (GeneralizedPotentialTable)relation.getValues();
    variables = pot.getVariables();
    conf = new Configuration(variables);

    if ((int)FiniteStates.getSize(variables) > 0 ) {
      retorno = pot.getValue(conf);
      conf.nextConfiguration();
    }

    for (i=1 ; i < (int)FiniteStates.getSize(variables) ; i++) {

      if (pot.getValue(conf) > retorno) {
        retorno = pot.getValue(conf);
      }

      conf.nextConfiguration();
    }

    return retorno;
  }

  /**
   * Obtiene la maxima probabilidad que hay en una tabla de potenciales
   * @param pot Es la tabla de potenciales donde realizamos los calculos
   * @return Es el valor de la maxima probabilidad en la tabla
   */
  public double maxPotential(PotentialTable pot) {

    Configuration conf;
    double retorno = 0;
    Vector variables;
    int i;

    variables = pot.getVariables();
    conf = new Configuration(variables);

    if ((int)FiniteStates.getSize(variables) > 0 ) {
      retorno = pot.getValue(conf);
      conf.nextConfiguration();
    }

    for (i=1 ; i < (int)FiniteStates.getSize(variables) ; i++) {

      if (pot.getValue(conf) > retorno) {
        retorno = pot.getValue(conf);
      }

      conf.nextConfiguration();
    }

    return retorno;
  }

  /**
   * Obtiene el nodo de utilidad de entre una lista de nodos.
   * @param nodeList Es la lista de nodos en la que buscamos el de utilidad
   * @return Retornamos el nodo de utilidad
   */
  public Node getUtilityNode(NodeList nodeList) {

    String nombre = "";
    int i;
    Node n;

    for (i=0; i < nodeList.size(); i++) {
      n = (Node)nodeList.elementAt(i);

      if (n.getKindOfNode() == Node.UTILITY) {
        nombre = n.getName();
        break;
      }

    }

    if (nombre.equals("")) {
      return null;
    } else {
      return nodeList.getNode(nombre);
    }

  }

  /**
   * Extrae la relacion correspondiente a una lista de nodos y estados.
   * @param diagram Diagrama.
   * @param lne Lista de nodos y estados.
   * @return La relacion.
   */
  public Relation getRelation(IDiagram diagram,NodeStateList lne) {

    int i;
    int j;
    int mas = 0;
    NodeState ne;
    RelationList listaRelaciones;
    Relation relacion = new Relation();
    Node nodo;
    NodeList listaNodos;
    boolean encontrado = true;

    if (lne.isUtility) mas = 1;
    listaRelaciones = diagram.getInitialRelations();

    for (j=0;j < listaRelaciones.size(); j++) {
      relacion = listaRelaciones.elementAt(j);
      listaNodos = relacion.getVariables();
      encontrado = true;

      if (lne.size() != (listaNodos.size() - mas)) {
        encontrado = false;
      } else {

        for (i = 0; i < lne.size(); i++) {
          ne = lne.getNodeState(i);
          nodo = listaNodos.elementAt(i + mas);

          if (!nodo.getName().equals(ne.getName())) {
            encontrado = false;
            break;
          }

        }

      }

      if (encontrado) {
        break;
      }

    }

    if (encontrado) {
      return relacion;
    } else {
      return null;
    } 

  }

  /**
   * Pone el valor de probabilidad o utilidad en una determinada configuracion de una relacion .
   * @param rel Es la relacion que vamos a modificar
   * @param conf Es la configuracion que vamos a cambiar
   * @param prob Es la probabilidad que vamos a poner
   */
  public void setProbability(Relation rel, Configuration conf, double prob) {

    GeneralizedPotentialTable pot;
    FiniteStates finiteStates;
    int pos;
    int nConf;
    int dest;
    double resto;
    int n;
    double v;
    double vDest;
    double vOrg;

    pot = (GeneralizedPotentialTable) rel.getValues();
    finiteStates = conf.getVariable(0);

    if (!rel.withUtilityNode()) {
      pos = conf.getIndexInTable();
      nConf = (int) FiniteStates.getSize(pot.getVariables());
      vOrg = pot.getValue(pos);
	pot.setValue(conf, prob);
      resto = prob - vOrg;
      n = finiteStates.getNumStates();

      for(int i=1;i<n;i++) {
        dest = (pos + (i * nConf / n)) % nConf;
        vDest = pot.getValue(dest);
        v = vDest - ((vDest + (vOrg / (n - 1))) * resto);
        pot.setValue(dest,v);
      }

    } else {
	pot.setValue(conf, prob);
    }
    
  }

  /**
   * Pone el valor de probabilidad en una determinada posicion de una relacion .
   * @param rel Es la relacion que vamos a modificar
   * @param pos Es la posicion en la lista de configuraciones que vamos a cambiar
   * @param prob Es la probabilidad que vamos a poner
   */
  public void setProbability(Relation rel, int pos, double prob) {

    GeneralizedPotentialTable pot;
    FiniteStates finiteStates;
    int nConf;
    int dest;
    double resto;
    int n;
    double v;
    double vDest;
    double vOrg;
    Configuration conf;

    pot = (GeneralizedPotentialTable) rel.getValues();

    if (!rel.withUtilityNode()) {
      conf = new Configuration(rel.getVariables());
      conf.goToConfiguration(pos);
      finiteStates = conf.getVariable(0);
      nConf = (int) FiniteStates.getSize(pot.getVariables());
      vOrg = pot.getValue(conf);
	pot.setValue(pos, prob);
      resto = prob - vOrg;
      n = finiteStates.getNumStates();

      for(int i=1;i<n;i++) {
        dest = (pos + (i * nConf / n)) % nConf;
        vDest = pot.getValue(dest);
        v = vDest - ( (vDest + (vOrg / (n - 1))) * resto);
        pot.setValue(dest, v);
      }
	
    } else {
	pot.setValue(pos, prob);
    }
    
  }

  /**
   * Devuelve la posicion de una configuracion de una relacion , la configuracion
   * viene dada por una lista de nodos con sus estados .
   * Si no exista esa configuracion , devuelve -1 .
   * @param rel Es la relacion en la que buscamos
   * @param listaNE Es la lista de estados de los nodos de entrada
   * @return Devuelve un entero [0..numero de configuraciones posibles -1] con la posicion buscada
   */
  public int getPosConfig(Relation rel, NodeStateList listaNE) {

    int retorno = -1;
    GeneralizedPotentialTable pot;
    Vector variables;
    Configuration conf;
    FiniteStates finiteStates;
    String estado;
    NodeState ne;
    boolean igual;
    int nConf;
    int i;
    int f;

    pot = (GeneralizedPotentialTable)rel.getValues();
    variables = pot.getVariables();
    conf = new Configuration(variables);
    nConf = (int)FiniteStates.getSize(variables);

    if (variables.size() == listaNE.size()) {

      for (i=0; i < nConf ; i++) {
        igual = true;

        for(f=0; f < conf.size(); f++) {
          finiteStates = (FiniteStates)variables.elementAt(f);
          estado = finiteStates.getState(conf.getValue(f));
          ne = listaNE.getNodeState(f);

          if (!estado.equals(ne.getState()) || !(ne.getName()).equals(finiteStates.getName())) {
            igual = false;
            break;
          }

        }

        if(igual) {
          retorno = i;
          break;
        } else {
          conf.nextConfiguration();
        }

      }

    }

    return retorno;
  }

  /**
   * Obtiene la utilidad de un diagrama.
   * @param r Diagrama de entrada.
   * @return Utilidad.
   */
  public double getUtility(IDiagram r) {

    IDiagram diagrama;
    ArcReversalSV eval;
    Node nodo;
    IDWithSVNodes dia;
    PotentialTable pot;

    diagrama = r.copy();
    dia = IDWithSVNodes.convertToIDWithSVNodes(diagrama);
    eval = new ArcReversalSV(dia);
    eval.initialConditions();
    eval.evaluateDiagram(true,null);
    nodo = dia.getTerminalValueNode();
    pot = dia.getTotalUtility(nodo);
    return maxPotential(pot);
  }

  /**
   * Obtiene la utilidad y probabilidad de un diagrama con un valor de probabilidad de una determinada
   * configuracion en un rango dado y con un incremento en los calculos dado.
   * @param r Es la red original , que no sera modificada en el proceso
   * @param entry Es la configuracion de entrada de datos
   * @return Devuelve un objeto con las entradas y las salidas
   */
  public RangeBox getUtilityRange(IDiagram r, BoxEntry entry) {

    NodeStateList listaNE;
    double paso;
    double minValue;
    double maxValue;
    double salto;
    double u;
    IDiagram diagrama;
    RelationList listaRelaciones;
    Relation relacion;
    ArcReversalSV eval;
    BoxResult resultado = new BoxResult();
    String nombreCabeza;
    Vector utilidadProbabilidad;
    GeneralizedPotentialTable potencial;
    Node nodo;
    IDiagram diag;
    double valor;
    int pos;
    int posRelacion;
    GeneralizedPotentialTable pot;
    IDWithSVNodes dia;

    diagrama = r.copy();
    listaRelaciones = diagrama.getInitialRelations();
    listaNE = entry.getNodeStateList();
    relacion = getRelation(diagrama,listaNE);
    nodo = relacion.getVariables().elementAt(0);
    paso = entry.getStep();

    if (paso < 1) {
      paso = 1;
    }

    minValue = listaNE.getMinValue();
    maxValue = listaNE.getMaxValue();
    salto = (maxValue - minValue) / paso;
    u = 0;
    nombreCabeza = nodo.getName();
    relacion = listaRelaciones.getRelationByNameOfNode(nombreCabeza);
    posRelacion = listaRelaciones.indexOf(relacion);
    pos = getPosConfig(relacion, listaNE);
    pot = (GeneralizedPotentialTable) relacion.getValues();
    valor = pot.getValue(pos);
    utilidadProbabilidad = new Vector();
    minValue = minValue - salto;

    do {
      minValue = minValue + salto;

      if (minValue > maxValue) {
        minValue = maxValue;
      }

      setProbability(relacion, pos, minValue);
      listaRelaciones.setElementAt(relacion, posRelacion);
      diagrama.setRelationList(listaRelaciones.getRelations());
      diag = diagrama.copy();
      dia = IDWithSVNodes.convertToIDWithSVNodes(diag);
      eval = new ArcReversalSV(dia);
      eval.initialConditions();
      eval.evaluateDiagram(true,null);
      nodo = dia.getTerminalValueNode();
      u = maxPotential(dia.getTotalUtility(nodo));
      utilidadProbabilidad.add(new ProbUtil(minValue, u));
      setProbability(relacion, pos, valor);
      listaRelaciones.setElementAt(relacion, posRelacion);
      diagrama.setRelationList(listaRelaciones.getRelations());
    }
    while (minValue < maxValue);

    resultado = new BoxResult(utilidadProbabilidad);
    return new RangeBox(entry, resultado);
  }

  /**
   * Obtiene la utilidad y probabilidad de un diagrama con un valor de probabilidad de una determinada
   * configuracion en un rango dado y con un incremento en los calculos dado. Ademas , se obtienen solo
   * los datos de una determinada configuracion en la tabla de decisiones. Se utiliza para obtener los
   * datos del analisis sobre un parametro.
   * @param r Es la red original , que no sera modificada en el proceso
   * @param entry Es la configuracion de entrada de datos
   * @param conf Orden en la tabla de decisiones.
   * @param node Nodo de decision.
   * @return Devuelve una lista de objetos con las entradas y las salidas
   */
  public RangeBoxList getUtilityRange(IDiagram r, BoxEntry entry, int conf, Node node) {

    NodeStateList listaNE;
    double paso;
    double minValue;
    double maxValue;
    double salto;
    double u;
    IDiagram diagrama;
    RelationList listaRelaciones;
    Relation relacion;
    ArcReversalSV eval;
    BoxResult resultado = new BoxResult();
    String nombreCabeza;
    Vector utilidadProbabilidad;
    GeneralizedPotentialTable potencial;
    Node nodo;
    IDiagram diag;
    double valor;
    int pos;
    int posRelacion;
    GeneralizedPotentialTable pot;
    IDWithSVNodes dia;
    RangeBoxList total = new RangeBoxList();
    DecisionTable tablaDecision = null;
    int anchoColumna;

    int y = 0;
    do {

      diagrama = r.copy();
      listaRelaciones = diagrama.getInitialRelations();
      listaNE = entry.getNodeStateList();
      relacion = getRelation(diagrama,listaNE);
      nodo = relacion.getVariables().elementAt(0);
      paso = entry.getStep();

      if (paso < 1) {
        paso = 1;
      }

      minValue = listaNE.getMinValue();
      maxValue = listaNE.getMaxValue();
      salto = (maxValue - minValue) / paso;
      u = 0;
      nombreCabeza = nodo.getName();
      relacion = listaRelaciones.getRelationByNameOfNode(nombreCabeza);
      posRelacion = listaRelaciones.indexOf(relacion);
      pos = getPosConfig(relacion, listaNE);
      pot = (GeneralizedPotentialTable) relacion.getValues();
      valor = pot.getValue(pos);
      utilidadProbabilidad = new Vector();
      minValue = minValue - salto;

      do {
        minValue = minValue + salto;

        if (minValue > maxValue) {
          minValue = maxValue;
        }
        setProbability(relacion, pos, minValue);
        listaRelaciones.setElementAt(relacion, posRelacion);
        diagrama.setRelationList(listaRelaciones.getRelations());

        diag = diagrama.copy();
        dia = IDWithSVNodes.convertToIDWithSVNodes(diag);
	  dia.compile(3,null);
	  tablaDecision = new DecisionTable(node,(Bnet)dia);
	  anchoColumna = tablaDecision.getValues().size()/tablaDecision.getStates().size();
	  Double uu = (Double)tablaDecision.getValues().elementAt(conf+(anchoColumna*y));
	  u = uu.doubleValue();

        utilidadProbabilidad.add(new ProbUtil(minValue, u));
        setProbability(relacion, pos, valor);
        listaRelaciones.setElementAt(relacion, posRelacion);
        diagrama.setRelationList(listaRelaciones.getRelations());
      }
      while (minValue < maxValue);

      resultado = new BoxResult(utilidadProbabilidad);
      total.addRangeBox(new RangeBox(entry,resultado));
      y++;
    } while(y<tablaDecision.getStates().size());
    return total;
  }

  /**
   * Obtiene la utilidad y probabilidad de un diagrama con un valor de probabilidad de todas las
   * configuraciones con un incremento en los calculos dado.
   * @param r Es la red original , que no sera modificada en el proceso
   * @param entryList Es la lista de configuraciones de entrada.
   * @param paso Es el numero de intervalos de probabilidad a tomar . Si paso<1 , se toma paso=1
   * @return Devuelve un objeto con las entradas y las salidas
   */
  public RangeBoxList getUtilityRange(IDiagram r, ConfigurationList entryList, double paso) {

    RangeBoxList listaRangos;
    int i;
    NodeStateList listaNE;
    BoxEntry entrada = new BoxEntry();
    RangeBox rangeBox;
    IDiagram diagrama;

    listaRangos = new RangeBoxList();

    for (i=0; i < entryList.size(); i++) {
      listaNE = entryList.getNodeStateList(i);
      entrada = new BoxEntry(listaNE,paso);
      diagrama = r.copy();
      rangeBox = getUtilityRange(diagrama,entrada);
      listaRangos.addRangeBox(rangeBox);
    }

    return listaRangos;
  }

  /**
   * Obtiene la utilidad y probabilidad de un diagrama con un valor de probabilidad de una
   * configuracione con un incremento en los calculos dado. Se utiliza para obtener el analisis
   * sobre un parametro , por lo que se utiliza un parametro para extraer la informacion de una
   * cierta posicion en la tabla de decisiones
   * @param r Es la red original , que no sera modificada en el proceso
   * @param entryList Es la lista de configuraciones de entrada.
   * @param paso Es el numero de intervalos de probabilidad a tomar . Si paso<1 , se toma paso=1
   * @param conf Orden dentro de la tabla de decisiones.
   * @param node Nodo de decision.
   * @return Devuelve una lista de objetos con las entradas y las salidas
   */
  public RangeBoxList getUtilityRange(IDiagram r, ConfigurationList entryList, double paso, int conf, Node node) {

    RangeBoxList listaRangos;
    int i;
    NodeStateList listaNE;
    BoxEntry entrada = new BoxEntry();
    RangeBox rangeBox;
    IDiagram diagrama;

    listaRangos = new RangeBoxList();

    listaNE = entryList.getNodeStateList(0);
    entrada = new BoxEntry(listaNE,paso);
    diagrama = r.copy();
    listaRangos = getUtilityRange(diagrama,entrada,conf,node);

    return listaRangos;
  }

  /**
   * Genera todas las configuraciones existentes en una relacion .
   * @param relation Es la relacion de entrada
   * @return Devuelve una lista de configuraciones
   */
  public ConfigurationList getLNERelation(Relation relation) {

    ConfigurationList listaC;
    GeneralizedPotentialTable potencial;
    Configuration conf;
    int i;
    int f;
    int k;
    NodeState nodo;
    NodeStateList listaNE;
    Vector variables;

    listaC = new ConfigurationList();
    potencial = (GeneralizedPotentialTable)relation.getValues();
    
    variables = potencial.getVariables();

    conf = new Configuration(variables);

    for (i=0; i < (int)FiniteStates.getSize(variables); i++) {

      listaNE = new NodeStateList();

      if (relation.withUtilityNode()) {
        listaNE.isUtility(true);
        k = 1;
      } else {
        listaNE.isUtility(false);
         k = 0;
      } 
      
      listaNE.setHeadNode((relation.getVariables().elementAt(0)).getName());

      for (f=0; f < conf.size(); f++) {
        nodo  = new NodeState((relation.getVariables().elementAt(f + k)).getName(),
                              new String((String)(
            (FiniteStates)variables.elementAt(f)).getState(conf.getValue(f))));
        listaNE.addNodeState(nodo);
      }

      if(potencial.getRange(conf)==null) {
        listaNE.setMinValue(Double.NaN);
        listaNE.setMaxValue(Double.NaN);
      } else {
        listaNE.setMinValue(potencial.getMinRange(conf));
        listaNE.setMaxValue(potencial.getMaxRange(conf));
      }

      listaNE.setValue(potencial.getValue(conf));
      listaNE.setName(potencial.getName(conf));
      listaC.addNodeStateList(listaNE);
      conf.nextConfiguration();

    }

    return listaC;
  }

  /**
   * Extrae la red.
   * @return Red.
   */
  private Network getNet() {

    return net;
  }

  /**
   * Extrae el frame de la red.
   * @return Red.
   */
  public ElviraFrame getElvFrame() {

    return elvFrame;
  }

  /**
   * Devuelve el diagrama.
   * @return
   */
  public IDiagram getDiag() {

    return iDiag;
  }

  /**
   * Pone el diagrama.
   * @param d 
   */
  public void setDiag(IDiagram d) {

    iDiag = d;
  }

  /**
   * Pone la red.
   * @throws elvira.parser.ParseException
   * @throws IOException
   */
  public void setNet() throws elvira.parser.ParseException,IOException {

    net = Network.read(getNetPath());
    iDiag = (IDiagram)net;
  }

  /**
   * Guarda la red.
   */
  public void save() {

    try {

      if (getNet() != null) {
        getNet().save(getNetPath());
        elvFrame.openFile(getNetPath());
      }

    } catch (Exception e) {}
  }

  /**
   * Elimina las comillas inicial y final de una cadena.
   * @param texto
   * @return
   */
  public String removeComillas(String texto) {

    String retorno;
    int posInicial;
    int posFinal;

    posInicial = texto.indexOf("\"");
    posFinal = texto.lastIndexOf("\"");
    if (posInicial >= 0 && posFinal < texto.length()) {
      retorno = texto.substring(posInicial + 1, posFinal);
    } else {
      retorno = texto;
    }
    return retorno;
  }

  /**
   * Extrae la clase de internacionalizacion
   * @return
   */
  public ResourceBundle getDialogBundle() {

    return dialogBundle;
  }

  /**
   * Pone la internacionalizacion
   * @param bundle
   */
  public void setDialogBundle(ResourceBundle bundle) {

    dialogBundle = bundle;
  }

  /**
   * Extrae la clase de internacionalizacion
   * @return
   */
  public ResourceBundle getMenuBundle() {

    return menuBundle;
  }

  /**
   * Pone la internacionalizacion
   * @param bundle
   */
  public void setMenuBundle(ResourceBundle bundle) {

    menuBundle = bundle;
  }

  /**
   * Metodo de arranque.
   * @param args
   * @throws elvira.parser.ParseException
   * @throws IOException
   */
  public static void main(String[] args) throws elvira.parser.ParseException,IOException {

    SensitivityAnalysis cargaRed = new SensitivityAnalysis(true);

    if (args.length > 0) {

      if (args[0].equals("es") || args[0].equals("sp")) {
        language = SPANISH;
      } else {
        language = AMERICAN;
      }

    }
    else  {
      language = SPANISH;
    }

    if (language == SPANISH) {
      cargaRed.setDialogBundle(ResourceBundle.getBundle(
          "elvira/localize/Dialogs_sp"));
      cargaRed.setMenuBundle(ResourceBundle.getBundle(
          "elvira/localize/Menus_sp"));
    }
    else {
      cargaRed.setDialogBundle(ResourceBundle.getBundle(
          "elvira/localize/Dialogs"));
      cargaRed.setMenuBundle(ResourceBundle.getBundle(
          "elvira/localize/Menus"));
    }

    MainFrame miFrame = new MainFrame(cargaRed);

  }

} // End of class