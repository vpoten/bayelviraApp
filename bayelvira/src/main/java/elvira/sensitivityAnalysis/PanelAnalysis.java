/*PanelAnalysis.java*/

package elvira.sensitivityAnalysis;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.event.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import elvira.IDiagram;
import elvira.RelationList;
import elvira.Relation;
import elvira.NodeList;
import elvira.Node;
import elvira.Elvira;
import java.io.IOException;
import javax.swing.table.TableColumn;
import javax.swing.ImageIcon;

/**
 * Esta clase muestra un panel con las diferentes opciones del analisis.
 * @author jruiz
 * @version 1.0
 */
public class PanelAnalysis extends JPanel {

  private SensitivityAnalysis cargaRed;
  private IDiagram diag;
  private JTabbedPane jTabbedPane1=new JTabbedPane();
  private PanelTornadoAllNodes panelTornado;
  private PanelSpiderAllNodes panelSpider;
  private PanelRelevance panelRelevancia;
  private PanelParam panelUno;
  private double utilidad = 0.0;
  private int margenX = 10;
  private int margenY = 10;
  private RangeBoxList listaRangos;
  private RangeBoxList listaRangosVisible = new RangeBoxList();
  private AnalysisTableModel tabla;
  private NumberFormat fm = new DecimalFormat("0.00");
  private Color[] colores = new Color[10];
  boolean recargar = false;
  private JSplitPane jSplitPane;
  private JScrollPane scroll;
  private JTable tablaVista;
  private JButton botonAll;
  private JButton botonNone;

  /**
   * Cosntructor por defecto.
   * @param cr Es la clase principal.
   * @throws elvira.parser.ParseException
   * @throws IOException
   */
  public PanelAnalysis(SensitivityAnalysis cr,Rectangle rec) throws elvira.parser.ParseException,
      IOException {

    int numColumnas;
    int numFilas;
    String[] columnas;
    Object[][] datos;
    RelationList listaRelaciones;
    ConfigurationList listaConfiguraciones;
    int i;
    int j;
    Relation relacion;
    NodeStateList listaNE;
    String linea = "";
    String nombreNodo;
    String estado;
    RangeBox rango;
    BoxResult resultado;
    BoxEntry entrada;
    TableColumn column;
    NodeList nodeList;
    Node nodo;

    cargaRed = cr;

    if (cargaRed != null && cargaRed.getDiag() != null) {
      diag = cr.getDiag();

      //Calculamos la utilidad del diagrama
      utilidad = cargaRed.getUtility(diag);

      setLayout(null);
      setBounds(rec);
      botonAll = new JButton(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.SelTodos"));
      botonAll.setBounds(new Rectangle(margenX,margenY - 4,100,16));
      add(botonAll);
      botonAll.addActionListener(new PanelAnalysis_botonAll_actionAdapter(this));
      botonNone = new JButton(Elvira.localize(cargaRed.getDialogBundle(),
                                              "SensitivityAnalysis.DeselTodos"));
      botonNone.setBounds(new Rectangle(margenX + 110,margenY - 4,100,16));
      add(botonNone);
      botonNone.addActionListener(new PanelAnalysis_botonNone_actionAdapter(this));
      this.addComponentListener(new PanelAnalysis_this_componentAdapter(this));
      scroll = new JScrollPane();
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.
                                          HORIZONTAL_SCROLLBAR_ALWAYS);

      //cargamos los paneles con el diagrama
      panelUno = new PanelParam(cargaRed,new Rectangle(30, 30, getWidth() - 25, getHeight() - 95),
                                listaRangos,-1,0.0);
      panelTornado = new PanelTornadoAllNodes(null,0);
      panelSpider = new PanelSpiderAllNodes(null,0);

      panelRelevancia = null;
  
      jTabbedPane1.addTab(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.DParam"),
                          new ImageIcon("elvira/gui/images/analysisOneParam.gif"),
                          panelUno);
      jTabbedPane1.addTab(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.DTornado"),
                          new ImageIcon("elvira/gui/images/tornadoDiagram.gif"),
                          panelTornado);
      jTabbedPane1.addTab(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.DSpider"),
                          new ImageIcon("elvira/gui/images/spiderDiagram.gif"),
                          panelSpider);
      jTabbedPane1.addTab(Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.DRelevancia"),
                          new ImageIcon("elvira/gui/images/relevanceTable.gif"),
                          panelRelevancia);

      jTabbedPane1.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if(jTabbedPane1.getSelectedIndex()==3 && panelRelevancia==null) {
		panelRelevancia = new PanelRelevance(cargaRed,new Rectangle(30, 30, getWidth() - 25, getHeight()
                                                 - 130));
            jTabbedPane1.setComponentAt(3,panelRelevancia);
          }
        }
      });

      //Creamos la tabla para la seleccion de parametros
      numColumnas = 8;
      numFilas = 0;
      columnas = new String[numColumnas];
      columnas[0] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Seleccionar");
      columnas[1] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Color");
      columnas[2] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Numero");
      columnas[3] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Nodo");
      columnas[4] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Parametro");
      columnas[5] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Valor");
      columnas[6] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Minimo");
      columnas[7] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Maximo");
      colores[0] = Color.blue;
      colores[1] = Color.cyan;
      colores[2] = Color.darkGray;
      colores[3] = Color.green;
      colores[4] = Color.magenta;
      colores[5] = Color.orange;
      colores[6] = Color.pink;
      colores[7] = Color.red;
      colores[8] = Color.yellow;
      colores[9] = Color.gray;

      //Llenamos la tabla con los parametros
      listaRelaciones = diag.getInitialRelations();
      nodeList = diag.getNodeList();
      nodo = cargaRed.getUtilityNode(nodeList);
      listaConfiguraciones = new ConfigurationList();

      for (i = 0; i < listaRelaciones.size(); i++) {
        relacion = listaRelaciones.elementAt(i);

        if (relacion.getValues().getClass() == GeneralizedPotentialTable.class) {
          listaConfiguraciones.addConfigurationList(cargaRed.getLNERelation(
              relacion));
        }

      }

      listaRangosVisible = cargaRed.getUtilityRange(diag,
          listaConfiguraciones,
          cargaRed.NUM_STEPS);

      i = 0;

      while (i < listaRangosVisible.size()) {
        rango = listaRangosVisible.getRangeBox(i);
        resultado = rango.getBoxResult();
        entrada = rango.getBoxEntry();
        listaNE = entrada.getNodeStateList();

        //Solo mostramos los parametros con rangos distintos de NaN
        if (new Double(listaNE.getMinValue()).isNaN() || new Double(listaNE.getMaxValue()).isNaN()) {
          listaRangosVisible.removeRangeBox(i);
          listaConfiguraciones.removeNodeStateList(i);
        } else {
          i++;
        }

      }

      if (listaRangosVisible.size() > 0 ) {
        numFilas = listaRangosVisible.size();
        datos = new Object[numFilas][numColumnas];
        tabla = new AnalysisTableModel(columnas, datos,this);
        tablaVista = new JTable(tabla);
        setUpColorRenderer(tablaVista);
        setUpColorEditor(tablaVista);

        for (i = 0; i < listaRangosVisible.size(); i++) {
          listaNE = listaConfiguraciones.getNodeStateList(i);

          if (listaNE.getName() != null &&
              !listaNE.getName().equals("\"\"") &&
              !listaNE.getName().equals("")) {

            //Eliminamos las comillas para que no aparezcan en la tabla
            linea = cargaRed.removeComillas(listaNE.getName());
          } else {

            //Escribimos U para utilidad y P para probabilidad
            if (listaNE.isUtility()) {
              linea = "U(";
            } else {
              linea = "P(";
            }

            nombreNodo = (listaNE.getNodeState(0)).getName();
            estado = (listaNE.getNodeState(0)).getState();
            linea = linea + nombreNodo + "=" + cargaRed.removeComillas(estado);

            if (!listaNE.isUtility() && listaNE.size() > 1) {
              linea = linea + " |";
            }

            for (j = 1; j < listaNE.size(); j++) {
              nombreNodo = (listaNE.getNodeState(j)).getName();
              estado = (listaNE.getNodeState(j)).getState();
              linea = linea + " " + nombreNodo + "=" + cargaRed.removeComillas(estado);
            }

            linea = linea + ")";

          }

          rango = listaRangosVisible.getRangeBox(i);
          resultado = rango.getBoxResult();
          entrada = rango.getBoxEntry();
          listaNE = entrada.getNodeStateList();
          tabla.setValueAt(new Boolean(false), i, 0);
          tabla.setValueAt(colores[i % 10], i, 1);
          rango.setColor(colores[i % 10]);
          tabla.setValueAt(new Integer(i), i, 2);

          if (listaNE.isUtility()) {
            tabla.setValueAt(listaNE.getHeadNode(),i,3);
          } else {
            tabla.setValueAt(listaNE.getNodeState(0).getName(), i, 3);
          }

          tabla.setValueAt(linea, i, 4);
          tabla.setValueAt(fm.format(listaNE.getValue()), i, 5);
          tabla.setValueAt(fm.format(listaNE.getMinValue()), i,6);
          tabla.setValueAt(fm.format(listaNE.getMaxValue()), i, 7);
        }

        //Damos a cada columna de la tabla un tamaño adecuado
        column = null;

        for (i = 0; i < 8; i++) {
          column = tablaVista.getColumnModel().getColumn(i);

          if (i < 2) {
            column.setPreferredWidth(20);
          }
          else if (i == 4) {
            column.setPreferredWidth(400);
          }
          else {
            column.setPreferredWidth(50);
          }

        }

        scroll.getViewport().add(tablaVista, null);
        jSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        jSplitPane.setTopComponent(scroll);
        jSplitPane.setBottomComponent(jTabbedPane1);
        jSplitPane.setOneTouchExpandable(true);
        jSplitPane.setBounds(new Rectangle(margenX, margenY + 15,
                                           getWidth() - (2 * margenX),getHeight() - (3 * margenY)));
        jSplitPane.setDividerLocation(130);
        jSplitPane.setDividerSize(10);
        add(jSplitPane);

        //Ahora , ya podemos poner la tabla a la espera de que se seleccione algun parametro .
        //Inicialmente , al llenar la tabla , se impedia que el oyente de la tabla actualizara
        //los datos .
        recargar = true;
      }

    }

  }

  /**
   * Permite poner colores en celdas de una tabla.
   * @param table
   */
  private void setUpColorRenderer(JTable table) {

    table.setDefaultRenderer(Color.class,
                               new ColorRenderer(true));
  }

  /**
   * Permite modificar los colores de las celdas de la tabla.
   * @param table
   */
  private void setUpColorEditor(JTable table) {

    //First, set up the button that brings up the dialog.
    final JButton button = new JButton("") {

      public void setText(String s) {
        //Button never shows text -- only color.
      }
    };

    button.setBackground(Color.white);
    button.setBorderPainted(false);
    button.setMargin(new Insets(0,0,0,0));

    //Now create an editor to encapsulate the button, and
    //set it up as the editor for all Color cells.
    final ColorEditor colorEditor = new ColorEditor(button);
    table.setDefaultEditor(Color.class, colorEditor);

    //Set up the dialog that the button brings up.
    final JColorChooser colorChooser = new JColorChooser();
    //XXX: PENDING: add the following when setPreviewPanel
    //XXX: starts working.
    //JComponent preview = new ColorRenderer(false);
    //preview.setPreferredSize(new Dimension(50, 10));
    //colorChooser.setPreviewPanel(preview);
    ActionListener okListener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        colorEditor.currentColor = colorChooser.getColor();
      }
    };

    final JDialog dialog = JColorChooser.createDialog(button,
        "Pick a Color",
        true,
        colorChooser,
        okListener,
        null); //XXXDoublecheck this is OK

    //Here's the code that brings up the dialog.
    button.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        button.setBackground(colorEditor.currentColor);
        colorChooser.setColor(colorEditor.currentColor);
        //Without the following line, the dialog comes up
        //in the middle of the screen.
        //dialog.setLocationRelativeTo(button);
        dialog.show();
      }

    });
  }

  /**
   * Se encarga de ver que configuracioens estan sñaladas en la tabla de configuraciones.
   * Despues repinta los diagramas.
   */
  public void recargar() {

    RangeBox rangeBox;
    int i;
    int s = -1;
    boolean acti = false;

    listaRangos = new RangeBoxList();

    for (i = 0; i < listaRangosVisible.size(); i++) {
      rangeBox = listaRangosVisible.getRangeBox(i);

      //Si se ha señalado un parametro en la tabla , se pone en el atributo Order su numero
      //de orden en la lista de parametros visbles en la tabla
      if ( ( (Boolean) tabla.getValueAt(i, 0)).booleanValue()) {
        rangeBox.setOrder(i);

        if (acti) {
          s = -1; 
        } else {
          s = i;
        }

        acti = true;
        rangeBox.setColor( (Color) tabla.getValueAt(i, 1));
        recargar = false;

        if ( (Color) tabla.getValueAt(i, 1) == Color.white) {
          tabla.setValueAt(colores[i % 10], i, 1);
        }

        recargar = true;
        listaRangos.addRangeBox(rangeBox);
      }

    }

    try {
      //Se actualizan los paneles con la lista de parametros seleccionados
      panelUno = new PanelParam(cargaRed,new Rectangle(30, 30, getWidth() - 25, getHeight() - 95),
                                listaRangosVisible.copy(),s,utilidad);
      jTabbedPane1.setComponentAt(0,panelUno);
      panelTornado = new PanelTornadoAllNodes(listaRangos.copy(), utilidad);
      jTabbedPane1.setComponentAt(1,panelTornado);
      panelSpider = new PanelSpiderAllNodes(listaRangos.copy(), utilidad);
      jTabbedPane1.setComponentAt(2,panelSpider);
    } catch(Exception e) {}
  }

  void this_componentResized(ComponentEvent e) {

    try {
      scroll.getViewport().add(tablaVista, null);
      jSplitPane.setBounds(new Rectangle(margenX, margenY + 15,
                                         getWidth() - (2 * margenX),getHeight() - (3 * margenY)));
      jSplitPane.setDividerLocation(jSplitPane.getDividerLocation());
      jSplitPane.setDividerSize(jSplitPane.getDividerSize());
      panelUno.setBounds(new Rectangle(30, 30, getWidth() - 25, getHeight() - 95));
      panelRelevancia.setBounds(new Rectangle(30, 30, getWidth() - 25, getHeight()
          - 130));
      jTabbedPane1.setComponentAt(3,panelRelevancia);
    } catch (Exception e1) {}
  }

  void botonAll_actionPerformed(ActionEvent e) {

    recargar = false;

    //Seleccion de todos los parametros
    for(int i=0; i < tabla.getRowCount(); i++) {
      tabla.setValueAt(new Boolean(true),i,0);
      recargar = true;
      recargar();
    }
  }

  void botonNone_actionPerformed(ActionEvent e) {

    recargar = false;

    //Deseleccion de todos los parametros
    for(int i=0; i < tabla.getRowCount(); i++) {
      tabla.setValueAt(new Boolean(false),i,0);
      recargar = true;
      recargar();
    }

  }

} //End of class

class PanelAnalysis_this_componentAdapter extends java.awt.event.ComponentAdapter {
  PanelAnalysis adaptee;

  PanelAnalysis_this_componentAdapter(PanelAnalysis adaptee) {
    this.adaptee = adaptee;
  }
  public void componentResized(ComponentEvent e) {
    adaptee.this_componentResized(e);
  }
}

class PanelAnalysis_botonAll_actionAdapter implements java.awt.event.ActionListener {

  PanelAnalysis adaptee;

  PanelAnalysis_botonAll_actionAdapter(PanelAnalysis adaptee){
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    try{
      adaptee.botonAll_actionPerformed(e);
    }
    catch(Exception e1) {}
  }
}

class PanelAnalysis_botonNone_actionAdapter implements java.awt.event.ActionListener {

  PanelAnalysis adaptee;

  PanelAnalysis_botonNone_actionAdapter(PanelAnalysis adaptee){
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    try{
      adaptee.botonNone_actionPerformed(e);
    }
    catch(Exception e1) {}
  }
}


