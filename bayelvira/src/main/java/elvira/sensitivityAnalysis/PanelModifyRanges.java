/*PanelModifyRanges.java*/

package elvira.sensitivityAnalysis;

import java.awt.Rectangle;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.util.Vector;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import javax.swing.table.TableColumn;
import javax.swing.event.*;
import elvira.IDiagram;
import elvira.Relation;
import elvira.RelationList;
import elvira.NodeList;
import elvira.Node;
import elvira.Elvira;
import elvira.Configuration;
import elvira.FiniteStates;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.Date;
import javax.swing.ImageIcon;

/**
 * Clase que contiene una panel con una tabla en la que modificar los rangos de
 * las configuraciones de los nodos.
 * @author jruiz
 * @version 1.0
 */
public class PanelModifyRanges extends JPanel implements TableModelListener{

  private SensitivityAnalysis cargaRed;
  private PanelAnalysis panelAnalisis;
  private JTabbedPane jTabbedPane;
  private IDiagram diag;
  private JComboBox jComboBox1 = new JComboBox();
  private String nombre;
  private boolean iniciar = false;
  private Relation relacion;
  private JLabel etiqueta;
  private GeneralizedPotentialTable potencial;
  private RangeTableModel tabla;
  private JTable tablaVista;
  private JScrollPane scroll;
  private RelationList listaRelaciones;
  private JButton botonAplicarCambios;
  private int margenX = 10;
  private int margenY = 40;
  protected boolean modify = false;

  /**
   * Constructor con un elemento de la clase principal.
   * @param cr Elemento de la clase principal.
   * @param jTabbedPane1 Todos los paneles para ser modificados al guardar los cambios.
   * @param rec Es el espacio que ocupa.
   */
  public PanelModifyRanges(SensitivityAnalysis cr,JTabbedPane jtp,Rectangle rec) {

    Relation relacion;
    int i;
    NodeList nl;
    Node n;

    cargaRed = cr;
    NumberFormat fm = new DecimalFormat("0.00");
    JLabel jLabel1 = new JLabel(Elvira.localize(cargaRed.getDialogBundle(),
                                                "SensitivityAnalysis.Parametro"));
    setBackground(Color.white);
    setLayout(null);
    setBounds(rec);
    jComboBox1.setBounds(new Rectangle(70,10,100,22));
    jComboBox1.addItemListener(new PanelModifyRanges_jComboBox1_itemAdapter(this));
    this.addComponentListener(new PanelModifyRanges_this_componentAdapter(this));
    add(jComboBox1,null);
    etiqueta = new JLabel();
    etiqueta.setText("");
    jLabel1.setBounds(new Rectangle(10,10,50,22));
    add(jLabel1,null);
    etiqueta.setBounds(new Rectangle(180, 10, 200, 15));
    add(etiqueta);
    botonAplicarCambios = new JButton(Elvira.localize(cargaRed.getDialogBundle(),
        "SensitivityAnalysis.ApliCambios"));
    botonAplicarCambios.setBounds(new Rectangle((getWidth() / 2) - 70,getHeight() - 30,140,20));
    add(botonAplicarCambios);
    botonAplicarCambios.addActionListener(new PanelModifyRanges_botonAplicarCambios_actionAdapter(this));
    scroll = new JScrollPane();
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scroll.setBounds(new Rectangle(margenX,margenY,getWidth()
                                   -(2 * margenX),getHeight() - margenX - margenY - 30));
    scroll.getViewport().add(tablaVista, null);
    add(scroll);
    jTabbedPane = jtp;

    if (cargaRed.getDiag() != null) {
      diag = cr.getDiag();
      listaRelaciones = diag.getInitialRelations();

      //Se llena el combo con los nodos destino de las relaciones con tablas de potenciales
      for (i = 0; i < listaRelaciones.size(); i++) {
        relacion = listaRelaciones.elementAt(i);
        nl = relacion.getVariables();
        n = (Node) nl.elementAt(0);

        if (relacion.getValues().getClass() == GeneralizedPotentialTable.class) {
          jComboBox1.addItem(n.getName());
        }

      }

      nombre = (String) jComboBox1.getSelectedItem();
      cargar(nombre);
      iniciar = true;
    }

  }

  /**
   * Carga un nuevo diagrama con un nombre.
   * @param nom Nombre del diagrama.
   */
  public void cargar(String nom) {

    int i;
    int j;
    int f;
    int numColumnas = 0;
    int numFilas = 0;
    String[] columnas;
    String texto;
    Configuration conf;
    Vector variables;
    int nConf;
    Object[][] datos;
    int numV;
    TableColumn column;

    nombre = nom;
    relacion = listaRelaciones.getRelationByNameOfNode(nombre);

    //Se llena la tabla correspondiente al nodo seleccionado
    if (relacion.withUtilityNode()) {
      numV = relacion.getVariables().size() - 1;
      columnas = new String[numV + 5];
      columnas[0] = "S";
      texto = "U( ";

      for (i = 1; i < relacion.getVariables().size(); i++) {
        columnas[i] = (relacion.getVariables().elementAt(i)).getName();
        texto = texto + (relacion.getVariables().elementAt(i)).getName() + " ";
      }

    } else {
      numV = relacion.getVariables().size();
      columnas = new String[numV + 5];
      columnas[0] = "S";
      texto = "P( " + (relacion.getVariables().elementAt(0)).getName();

      if (relacion.getVariables().size() > 1) {
        texto = texto + " | ";
      }

      columnas[1] = (relacion.getVariables().elementAt(0)).getName();

      for (i = 1; i < relacion.getVariables().size(); i++) {
        columnas[i + 1] = (relacion.getVariables().elementAt(i)).getName();
        texto = texto + (relacion.getVariables().elementAt(i)).getName() + " ";
      }

    }

    texto = texto + ")";
    etiqueta.setText(texto);
    columnas[numV + 1] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Valor");
    columnas[numV + 2] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Minimo");
    columnas[numV + 3] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Maximo");
    columnas[numV + 4] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Nombre");
    numColumnas = columnas.length;
    potencial = (GeneralizedPotentialTable)relacion.getValues();
    variables = potencial.getVariables();
    conf = new Configuration(variables);
    nConf = (int)FiniteStates.getSize(variables);
    numFilas = nConf;
    datos = new Object[numFilas][numColumnas];

    for (j=0 ; j < nConf; j++) {

      for (f=0; f < conf.size(); f++) {
        datos[j][f + 1] = cargaRed.removeComillas(new String( (String)
                                     ( (FiniteStates) variables.elementAt(f)).
                                     getState(conf.getValue(f))));
      }
      datos[j][f + 1] = Double.toString(potencial.getValue(conf));
	if(potencial.getRange(conf)==null) {
        datos[j][f + 2] = new String("");
        datos[j][f + 3] = new String("");
	} else {
        datos[j][f + 2] = Double.toString(potencial.getMinRange(conf));
        datos[j][f + 3] = Double.toString(potencial.getMaxRange(conf));
      }

      //Si el parametro tiene rangos NaN no se selecciona
      if (((String)datos[j][f+2]).equals("NaN") || ((String)datos[j][f+2]).equals("")){
        datos[j][0] = new Boolean(false);
        datos[j][f + 2] = new String("");
        datos[j][f + 3] = new String("");
      }
      else {
        datos[j][0] = new Boolean(true);
      }

      if (potencial.getName(conf) == null || potencial.getName(conf).equals("")) {
        datos[j][f + 4] = "";
      }
      else {
        datos[j][f + 4] = withoutQm(potencial.getName(conf));
        datos[j][0] = new Boolean(true);
      }

      conf.nextConfiguration();

    }

    tabla = new RangeTableModel(columnas,datos);
    tablaVista = new JTable(tabla);
    tabla.addTableModelListener(this);

    //Para relaciones con destino nodo de azar , se complementa el valor de la probabilidad
    if (!relacion.withUtilityNode()) {
      tabla.numStates = ((FiniteStates)relacion.getVariables().elementAt(0)).getNumStates();
      tabla.complementar = true;
    }

    tabla.validar = true;
    column = null;

    //Se seleccionan los tamaños de las columnas de la tabla
    for (i = 0; i < tabla.getColumnCount(); i++) {
      column = tablaVista.getColumnModel().getColumn(i);

      if (i == 0) {
        column.setPreferredWidth(20);
      }
      else if (i == tabla.getColumnCount() - 4) {
        column.setPreferredWidth(50);
      }
      else if (i == tabla.getColumnCount() - 3) {
        column.setPreferredWidth(50);
      }
      else if (i == tabla.getColumnCount() - 2) {
        column.setPreferredWidth(50);
      }
      else if (i == tabla.getColumnCount() - 1) {
        column.setPreferredWidth(400);
      }
      else {
        column.setPreferredWidth(80);
      }

    }

    scroll.getViewport().add(tablaVista, null);
  }

  /**
   * Se acepta aplicar los cambios.
   * @param e
   * @throws elvira.parser.ParseException
   * @throws IOException
   */
  void botonAplicarCambios_actionPerformed(ActionEvent e) throws elvira.parser.ParseException,
      IOException {

    int columna = tabla.getColumnCount() - 3;
    Double min;
    Double max;
    int fila;
    String name;


    //Se llena la tabla de potenciales con los valores de la tabla vista
    for (fila = 0; fila < tablaVista.getRowCount(); fila++) {
      if(((Boolean)tablaVista.getValueAt(fila,0)).booleanValue() == false) {
        potencial.setRange(fila,null);
	  potencial.setName(fila,null);
      } else {
	  if(((String)tablaVista.getValueAt(fila,columna)).equals("")) {
	    min = Double.NaN;
        } else {
          min = new Double((String)tablaVista.getValueAt(fila, columna));
        }
	  if(((String)tablaVista.getValueAt(fila,columna + 1)).equals("")) {
          max = Double.NaN;
        } else {
          max = new Double((String)tablaVista.getValueAt(fila, columna + 1));
        }
        if(!min.isNaN() && !max.isNaN()) {
          potencial.setRange(fila, min.doubleValue(), max.doubleValue());
          if (!((String)tablaVista.getValueAt(fila,columna + 2)).equals("")) {
            name = (String)("\"" + tabla.getValueAt(fila, columna + 2) + "\"");               
          } else {
	      name = "";
          }
          potencial.setName(fila,name);
	  }
      }
    }
    if(!relacion.withUtilityNode()) {
	potencial.uncomplementChance();
    }

    //Se actualiza el diagrama con los nuevos valores de probabilidades
    /*listaRelaciones.removeRelation(listaRelaciones.getRelationByNameOfNode(
        nombre));
    listaRelaciones.insertRelation(relacion);*/
    listaRelaciones.setElementAt(relacion,listaRelaciones.indexOf(relacion));
    diag.setRelationList(listaRelaciones.getRelations());
    cargaRed.setDiag(diag);
    //cargaRed.getElvFrame().getNetworkFrame().getEditorPanel().setBayesNet(diag);

    //Se pone la bandera de que el diagrama ha sido modificado
    if (cargaRed.getElvFrame() != null) {
        modify = true;
    }

    //Se actuaizan los paneles con los nuevos datos
    jComboBox1.setSelectedIndex(0);
    jTabbedPane.remove(1);
    panelAnalisis = new PanelAnalysis(cargaRed,new Rectangle(30, 80,
        this.getParent().getWidth(),
        this.getParent().getHeight() - 80));
    jTabbedPane.addTab(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Analisis"),
                       new ImageIcon("elvira/gui/images/analysis.gif"),
                       panelAnalisis);

    //Aviso de que los cambios se ha realizado con exito
    JOptionPane.showMessageDialog(this, Elvira.localize(cargaRed.getDialogBundle(),
        "SensitivityAnalysis.Modificado"));
  }

  /**
   * Caja para seleccionar un determinado nodo.
   * @param e
   */
  void jComboBox1_itemStateChanged(ItemEvent e) {

    if (iniciar) {
      nombre = (String) jComboBox1.getSelectedItem();

      if (e.getStateChange() == e.SELECTED) {
        cargar(nombre);
      }

    }

  }

  public void tableChanged(TableModelEvent e) { 
  } 

  void this_componentResized(ComponentEvent e) {

    try {
      botonAplicarCambios.setBounds(new Rectangle((getWidth() / 2) - 70,getHeight() - 30,140,20));
      scroll.setBounds(new Rectangle(margenX,margenY,getWidth()-(2 * margenX),
                                     getHeight() - margenX - margenY - 30));
      scroll.getViewport().add(tablaVista, null);
    } catch (Exception e1) {}
  }

  	private String withoutQm(String s) {
            if (s.substring(0,1).equals("\""))
            {
                return (s.substring(1,s.length()-1));
            }
            else {
                return s;
            }
    	}

} //End of class

class PanelModifyRanges_botonAplicarCambios_actionAdapter implements java.awt.event.ActionListener {

  PanelModifyRanges adaptee;

  PanelModifyRanges_botonAplicarCambios_actionAdapter(PanelModifyRanges adaptee){
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
	try{
    adaptee.botonAplicarCambios_actionPerformed(e);
	}
	catch(Exception e1) {}
  }
}

class PanelModifyRanges_jComboBox1_itemAdapter implements java.awt.event.ItemListener {

  PanelModifyRanges adaptee;

  PanelModifyRanges_jComboBox1_itemAdapter(PanelModifyRanges adaptee) {
    this.adaptee = adaptee;
  }

  public void itemStateChanged(ItemEvent e) {
    adaptee.jComboBox1_itemStateChanged(e);
  }
}

class PanelModifyRanges_this_componentAdapter extends java.awt.event.ComponentAdapter {
  PanelModifyRanges adaptee;

  PanelModifyRanges_this_componentAdapter(PanelModifyRanges adaptee) {
    this.adaptee = adaptee;
  }
  public void componentResized(ComponentEvent e) {
    adaptee.this_componentResized(e);
  }
}

