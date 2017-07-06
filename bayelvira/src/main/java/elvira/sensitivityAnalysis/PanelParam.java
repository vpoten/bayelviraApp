/*PanelParam.java*/

package elvira.sensitivityAnalysis;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ComponentEvent;
import java.util.Vector;
import elvira.RelationList;
import elvira.NodeList;
import elvira.IDiagram;
import elvira.Node;
import elvira.Elvira;
import elvira.Relation;
import java.io.IOException;
import elvira.Bnet;
import elvira.FiniteStates;

/**
 * Muestra el panel del analisis one-way.
 * @author jruiz
 * @version 1.0
 */
public class PanelParam extends JPanel {

  private PanelOneParam panelUna;
  private RelationList listaRelaciones;
  private RangeBoxList listaRangos;
  private RangeBoxList listaRangosVisible;
  private RangeBoxList nuevaListaRangos;
  private NodeList listaNodos;
  private int posicion;
  private double utilidad = 0.0;
  private JComboBox jComboBox1 = new JComboBox();
  private JComboBox jComboBox2 = new JComboBox();
  private JLabel jLabel1;
  private JLabel jLabel2;
  private SensitivityAnalysis cargaRed;
  private IDiagram diag;
  private boolean iniciar = false;
  private Vector listaConfig = new Vector();
  private NodeStateList listaSenalada;
  private Vector listaEstadosDecision;

  /**
   * Cosntructor por defecto.
   * @param cr Es la clase principal.
   * @throws elvira.parser.ParseException
   * @throws IOException
   */
  public PanelParam(SensitivityAnalysis cr,Rectangle rec,RangeBoxList lr,int pos,double u)
      throws elvira.parser.ParseException,IOException {

    int i;
    Node n;
    RangeBox rango;
    BoxResult resultado;
    BoxEntry entrada;
    NodeStateList listaNE;
    String nombreNodo;
    NodeList listaDecisiones;

    cargaRed = cr;
    posicion = pos;
    utilidad = u;
    listaEstadosDecision = new Vector();
    jLabel1 = new JLabel(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Decision"));
    jLabel2 = new JLabel(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Configuration"));

    if (cargaRed != null && cargaRed.getDiag() != null) {
      diag = cr.getDiag();
      listaRangos = lr;
	listaDecisiones = diag.getDecisionList();
      listaRelaciones = cargaRed.getDiag().getInitialRelations();
      setLayout(null);
      setBounds(new Rectangle(10, 10, getWidth() - 20, getHeight() - 20));
      this.addComponentListener(new PanelParam_this_componentAdapter(this));
      jComboBox1.setBounds(new Rectangle(80, 8, 100, 22));
      jComboBox1.addItemListener(new PanelParam_jComboBox1_itemAdapter(this));
      jComboBox2.setBounds(new Rectangle(290, 8, 340, 22));
      jComboBox2.addItemListener(new PanelParam_jComboBox2_itemAdapter(this));
      jLabel1.setBounds(new Rectangle(25, 8, 50, 22));
      jLabel2.setBounds(new Rectangle(190, 8, 100, 22));
      add(jComboBox1, null);
      add(jComboBox2, null);
      add(jLabel1, null);
      add(jLabel2, null);
      listaRangosVisible = new RangeBoxList();
      nuevaListaRangos = new RangeBoxList();

      if (pos > -1) {
        listaNodos = diag.getNodeList();
        i = 0;

        while (i < listaRangos.size()) {
          rango = listaRangos.getRangeBox(i);
          resultado = rango.getBoxResult();
          entrada = rango.getBoxEntry();
          listaNE = entrada.getNodeStateList();

          if (new Double(listaNE.getMinValue()).isNaN() ||
              new Double(listaNE.getMaxValue()).isNaN()) {
            listaRangos.removeRangeBox(i);
          }
          else {
            i++;
          }

        }

        rango = listaRangos.getRangeBox(posicion);
        entrada = rango.getBoxEntry();
        listaSenalada = entrada.getNodeStateList();

        for (i=0; i < listaDecisiones.size(); i++) {
          n = listaDecisiones.elementAt(i);
          nombreNodo = n.getName();

          if (n.getKindOfNode() == Node.DECISION){
            jComboBox1.addItem(nombreNodo);
          }

        }

        if (jComboBox1.getItemCount() > 0) {

          iniciar = true;
          jComboBox1.setSelectedIndex(0);
          cargar();
        }

      } else {
        iniciar = true;
        cargar();
      }

    }

  }

  /**
   * Pone los datos de la lista de configuraciones.
   * @param lista
   */
  public void setLista(RangeBoxList lista) {

    if(iniciar) {

      if (panelUna != null) {
        remove(panelUna);
      }

      panelUna = new PanelOneParam(cargaRed, lista,
                                    new Rectangle(10, 40, getWidth() - 20,
          getHeight() - 50),listaEstadosDecision,(String)jComboBox1.getSelectedItem());
      add(panelUna, null);
      panelUna.setBounds(new Rectangle(10,40,getWidth() - 20,getHeight() - 50));
      panelUna.repaint();
    }

  }

  /**
   * Carga los datos.
   */
  void cargar() {

    int k;

    if (iniciar) {
      iniciar = false;
      jComboBox2.removeAllItems();

      if (jComboBox1.getItemCount() > 0) {
	  diag.compile(3,null);
	  Node nodoL = diag.getNode((String)jComboBox1.getSelectedItem());
	  DecisionTable tablaDecisiones = new DecisionTable(nodoL,(Bnet)diag);
	  Vector configuraciones = tablaDecisiones.getConfigurations();
	  listaEstadosDecision = tablaDecisiones.getStates();
	  for(k=0;k<configuraciones.size();k++) {
	    jComboBox2.addItem(configuraciones.elementAt(k));
	  }
	  if(jComboBox2.getItemCount()>0) {
	    jComboBox2.setSelectedIndex(0);
	  }
	  iniciar = true;
	  if(configuraciones.size()<2) {
		nuevaListaRangos = cargaRed.getUtilityRange(diag,new ConfigurationList(listaSenalada),cargaRed.NUM_STEPS,0,nodoL);
		setLista(nuevaListaRangos);
	  } else {
	    cargar2();
	  }
      }

    }

    iniciar = true;
  }

  /**
   * Carga las configuraciones.
   */
  void cargar2() {

    int pos;

    if (iniciar) {
      iniciar = false;

      if (jComboBox2.getItemCount() > 0) {
        pos = jComboBox2.getSelectedIndex();
	  Node nodoL = diag.getNode((String)jComboBox1.getSelectedItem());
	  nuevaListaRangos = cargaRed.getUtilityRange(diag,new ConfigurationList(listaSenalada),cargaRed.NUM_STEPS,pos,nodoL);
      }

      iniciar = true;
      setLista(nuevaListaRangos);
    }

  }

  void jComboBox1_itemStateChanged(ItemEvent e) {

    cargar();
  }

  void jComboBox2_itemStateChanged(ItemEvent e) {

    cargar2();
  }

  void this_componentResized(ComponentEvent e) {

    try {
        panelUna.setBounds(new Rectangle(10,40,getWidth() - 20,getHeight() - 50));
        panelUna.repaint();
    } catch (Exception e1) {}
  }

} //End of class

class PanelParam_jComboBox1_itemAdapter implements java.awt.event.ItemListener {
  PanelParam adaptee;

  PanelParam_jComboBox1_itemAdapter(PanelParam adaptee) {
    this.adaptee = adaptee;
  }
  public void itemStateChanged(ItemEvent e) {
    adaptee.jComboBox1_itemStateChanged(e);
  }
}

class PanelParam_jComboBox2_itemAdapter implements java.awt.event.ItemListener {
  PanelParam adaptee;

  PanelParam_jComboBox2_itemAdapter(PanelParam adaptee) {
    this.adaptee = adaptee;
  }
  public void itemStateChanged(ItemEvent e) {
    adaptee.jComboBox2_itemStateChanged(e);
  }
}

class PanelParam_this_componentAdapter extends java.awt.event.ComponentAdapter {
  PanelParam adaptee;

  PanelParam_this_componentAdapter(PanelParam adaptee) {
    this.adaptee = adaptee;
  }
  public void componentResized(ComponentEvent e) {
    adaptee.this_componentResized(e);
  }
}
