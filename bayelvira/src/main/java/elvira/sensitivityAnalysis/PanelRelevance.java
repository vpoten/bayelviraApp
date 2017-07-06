/*PanelRelevance.java*/

package elvira.sensitivityAnalysis;

import java.awt.Rectangle;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.event.ComponentEvent;
import javax.swing.table.TableColumn;
import java.util.Vector;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import elvira.IDiagram;
import elvira.RelationList;
import elvira.Relation;
import elvira.Elvira;

/**
 * Clase que muestra la relevancia de los nodos del diagrama.
 * @author jruiz
 * @version 1.0
 */
public class PanelRelevance extends JPanel {

  private NumberFormat fm=new DecimalFormat("0.00");
  private NumberFormat fmU=new DecimalFormat("0.0000");
  private JScrollPane scroll;
  private JTable tablaVista;
  private SensitivityAnalysis cargaRed;
  private IDiagram diag;
  private double utilidad = 0.0;

  /**
   * Constructor por defecto.
   * @param cr Clase principal.
   * @param rec Es el espacio que ocupa el panel.
   */
  public PanelRelevance(SensitivityAnalysis cr,Rectangle rec) {

    BoxResult resultado;
    RangeBox rango;
    BoxEntry entrada;
    RelevanceTableModel tabla;
    RelationList listaRelaciones;
    RangeBoxList listaRangos;
    ConfigurationList listaConfiguraciones;
    Relation relacion;
    NodeStateList listaNE;
    String linea = "";
    String nombreNodo;
    String estado;
    int j;
    int i = 0;
    int n = -1;
    int m = 0;
    int k;
    double relevancia;
    String[] columnas;
    Object[][] datos;
    int numColumnas;
    int numFilas;
    TableColumn column;
    String nombreUtilidad;
    boolean acti = false;

    cargaRed = cr;
    setBackground(Color.white);

    if (cargaRed != null && cargaRed.getDiag() != null) {
      diag = cr.getDiag();
      utilidad = cargaRed.getUtility(diag);
      nombreUtilidad = cargaRed.getUtilityNode(cargaRed.getDiag().getNodeList()).getName();
      setLayout(null);
      setBounds(rec);
      this.addComponentListener(new PanelRelevance_this_componentAdapter(this));
      scroll = new JScrollPane();
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      scroll.setHorizontalScrollBarPolicy(JScrollPane.
                                          HORIZONTAL_SCROLLBAR_ALWAYS);
      scroll.setBounds(new Rectangle(10, 10, getWidth() - 20,
                                     getHeight() - 20));
      listaRelaciones = diag.getInitialRelations();

      for(i=0; i < listaRelaciones.size(); i++) {
        relacion = listaRelaciones.elementAt(i);

        if (relacion.getValues().getClass() == GeneralizedPotentialTable.class) {
          listaConfiguraciones = new ConfigurationList();
          listaConfiguraciones.addConfigurationList(cargaRed.getLNERelation(
              relacion));
          acti = false;

          for (j = 0; j < listaConfiguraciones.size(); j++) {
            listaNE = listaConfiguraciones.getNodeStateList(j);
      

            if (!new Double(listaNE.getMinValue()).isNaN() &&
                !new Double(listaNE.getMaxValue()).isNaN()) {
              acti = true;
              m++;
            }
            
          }

        }

      }

      numColumnas = 7;
      columnas = new String[numColumnas];
      columnas[0] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Nodo");
      columnas[1] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Parametro");
      columnas[2] = "% " + Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.Relevancia");
      columnas[3] = Elvira.localize(cargaRed.getDialogBundle(),"SensitivityAnalysis.ValorRef");
      columnas[4] = "U min.";
      columnas[5] = "U ref.";
      columnas[6] = "U max.";
      numFilas = m;

      if (m > 0) {
        datos = new Object[numFilas][numColumnas];
        tabla = new RelevanceTableModel(columnas, datos);
        tablaVista = new JTable(tabla);
        n = -1;

        for (i = 0; i < listaRelaciones.size(); i++) {
          relacion = listaRelaciones.elementAt(i);

          if (relacion.getValues().getClass() == GeneralizedPotentialTable.class) {
            listaConfiguraciones = new ConfigurationList();
            relevancia = 0.0;
            listaConfiguraciones.addConfigurationList(cargaRed.getLNERelation(
                relacion));
            listaRangos = cargaRed.getUtilityRange(diag, listaConfiguraciones,
                cargaRed.NUM_STEPS);

            for (j = 0; j < listaRangos.size(); j++) {
              rango = listaRangos.getRangeBox(j);
              resultado = rango.getBoxResult();
              entrada = rango.getBoxEntry();
              relevancia = resultado.getMaxUtil() - resultado.getMinUtil();
              listaNE = entrada.getNodeStateList();

              if (!new Double(listaNE.getMinValue()).isNaN() &&
                  !new Double(listaNE.getMaxValue()).isNaN()) {
                n++;

                if (listaNE.getName() != null &&
                    !listaNE.getName().equals("\"\"") &&
                    !listaNE.getName().equals("")) {
                  linea = cargaRed.removeComillas(listaNE.getName());
                }
                else {

                  if (listaNE.isUtility()) {
                    linea = "U(";
                  } else {
                    linea = "P(";
                  }

                  nombreNodo = (listaNE.getNodeState(0)).getName();
                  estado = (listaNE.getNodeState(0)).getState();
                  linea = linea + nombreNodo + "=" +
                      cargaRed.removeComillas(estado);

                  if (!listaNE.isUtility() && listaNE.size() > 1) {
                    linea = linea + " |";
                  }

                  for (k = 1; k < listaNE.size(); k++) {
                    nombreNodo = (listaNE.getNodeState(k)).getName();
                    estado = (listaNE.getNodeState(k)).getState();
                    linea = linea + " " + nombreNodo + "=" +
                        cargaRed.removeComillas(estado);
                  }

                  linea = linea + ")";
                }

                if (!relacion.withUtilityNode()) {
                  tabla.setValueAt(relacion.getVariables().firstElement().
                                   getName(),
                                   n, 0);
                }
                else {
                  tabla.setValueAt(listaNE.getHeadNode(), n, 0);
                }

                if (utilidad == 0) {
                  utilidad = 1;
                }
                relevancia = relevancia * 100 / utilidad;
                tabla.setValueAt(linea, n, 1);
                tabla.setValueAt(fm.format(relevancia), n, 2);
                tabla.setValueAt(fm.format(entrada.getNodeStateList().getValue()),
                                 n, 3);
                tabla.setValueAt(fmU.format(resultado.getMinUtil()), n, 4);
                tabla.setValueAt(fm.format(utilidad), n, 5);
                tabla.setValueAt(fmU.format(resultado.getMaxUtil()), n, 6);
              }

            }

          }

        }

        for (i = 0; i < 7; i++) {
          column = tablaVista.getColumnModel().getColumn(i);

          if (i == 0) {
            column.setPreferredWidth(50);
          }
          else if (i == 1) {
            column.setPreferredWidth(400);
          }
          else {
            column.setPreferredWidth(80);
          }

        }

        scroll.getViewport().add(tablaVista, null);
        add(scroll);
      }

    }

  }

  void this_componentResized(ComponentEvent e) {

    try {
      scroll.setBounds(new Rectangle(10, 10, getWidth() - 20,
                                     getHeight() - 20));
      scroll.getViewport().add(tablaVista, null);
    } catch (Exception e1) {}
  }

} //End of class

class PanelRelevance_this_componentAdapter extends java.awt.event.ComponentAdapter {
  PanelRelevance adaptee;

  PanelRelevance_this_componentAdapter(PanelRelevance adaptee) {
    this.adaptee = adaptee;
  }
  public void componentResized(ComponentEvent e) {
    adaptee.this_componentResized(e);
  }
}
