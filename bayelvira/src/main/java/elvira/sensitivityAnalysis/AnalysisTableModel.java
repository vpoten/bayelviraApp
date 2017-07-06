/*AnalysisTableModel*/

package elvira.sensitivityAnalysis;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;
import java.awt.Color;

/**
 * Modelo de tabla para contener configuraciones , valores y rangos de nodos.
 * @author jruiz
 * @version 1.0
 */
class AnalysisTableModel extends AbstractTableModel {

  /**
  * Nombres de las columnas.
  */
  final String[] columnNames;

  /**
  * Datos.
  */
  final Object[][] data;

  /**
  * Para realizar un debug.
  */
  final boolean DEBUG=false;

  /**
   * Es el manel que contiene la tabla.
   */
  private PanelAnalysis panelAnalysis;

  /**
   * Crea un nuevo modelo de tabla a partir de una serie de niombres de columnas y unos datos.
   * @param cn Nombres de columnas.
   * @param d Datos de entrada.
   * @param pa Es el panel a repintar.
   */
  public AnalysisTableModel(String[] cn, Object[][] d,PanelAnalysis pa) {

    int i;
    int j;
    int k;

    panelAnalysis = pa;
    columnNames = new String[cn.length];
    data = new Object[d.length][d[0].length];

    for (i=0; i < cn.length; i++) {
      columnNames[i] = cn[i];
    }

    for (j=0; j < d.length; j++) {

      for (k=0; k < d[0].length; k++) {
        data[j][k] = d[j][k];
      }

    }

  }

  /**
   * Devuelve el numero de columnas.
   * @return Numero de columnas.
   */
  public int getColumnCount() {

    return columnNames.length;
  }

  /**
   * Devuelve el numero de filas.
   * @return Numero de filas.
   */
  public int getRowCount() {

    return data.length;
  }

  /**
   * Devuelve el nombre de la columna de una determinada posicion.
   * @param col Posicion de la columnas.
   * @return Nombre.
   */
  public String getColumnName(int col) {

    return columnNames[col];
  }

  /**
   * Devuelve el valor de una posicion en la tabla.
   * @param row Fila.
   * @param col Columna.
   * @return valor.
   */
  public Object getValueAt(int row, int col) {

    return data[row][col];
  }

  /**
   * Devuelve el nombre de la clase del objeto a devolver en una columna dada.
   * @param c Posicion de la columna.
   * @return Nombre de clase del objeto.
   */
  public Class getColumnClass(int c) {

    return getValueAt(0, c).getClass();
  }

  /**
   * El sistema comprueba si una fila y columna es editable.
   * @param row Fila.
   * @param col Columna.
   * @return True o false.
   */
  public boolean isCellEditable(int row, int col) {

    if (col > 1) {
      return false;
    } else {
      return true;
    }

  }

  /**
   * Pone un valor en una fila y columna.
   * @param value Valor.
   * @param row Fila.
   * @param col Columna.
   */
  public void setValueAt(Object value, int row, int col) {

    if (DEBUG) {
      System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
    }

    if (data[0][col] instanceof Boolean) {
      data[row][col] = new Boolean(((Boolean)value).booleanValue());

      if (panelAnalysis.recargar) {
        panelAnalysis.recargar();
      }

    } else if (data[0][col] instanceof Color) {
      data[row][col] = (Color)value;

      if (panelAnalysis.recargar) {
        panelAnalysis.recargar();
      }

    } else {
      data[row][col] = value;
    }

    if (DEBUG) {
      System.out.println("New value of data:");
      printDebugData();
    }

    fireTableDataChanged();
  }

  /**
   * Imprime los datos contenidos en la tabla.
   */
  private void printDebugData() {

    int numRows = getRowCount();
    int numCols = getColumnCount();
    int i;
    int j;

    for (i=0; i < numRows; i++) {
      System.out.print("    row " + i + ":");

      for (j=0; j < numCols; j++) {
        System.out.print("  " + data[i][j]);
      }

      System.out.println();
    }

    System.out.println("--------------------------");
  }

} //End of class
