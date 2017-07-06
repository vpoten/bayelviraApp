/*RangeTableModel.java*/

package elvira.sensitivityAnalysis;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

/**
 * Modelo de tabla para contener configuraciones , valores y rangos de nodos.
 * @author jruiz
 * @version 1.0
 */
class RangeTableModel extends AbstractTableModel {

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
  final boolean DEBUG = false;

  /**
  * ¿Complementar?
  */
  boolean complementar = false;

  /**
  * ¿validar rangos?
  */
  boolean validar = false;

  /**
   * Numero de estados del nodo.
   */
  int numStates;

  /**
   * Crea un nuevo modelo de tabla a partir de una serie de nombres de columnas y unos datos.
   * @param cn Nombres de columnas.
   * @param d Datos de entrada.
   */
  public RangeTableModel(String[] cn, Object[][] d) {

    int i;
    int j;
    int k;

    columnNames = new String[cn.length];
    data = new Object[d.length][d[0].length];

    for (i=0; i < cn.length; i++) {
      columnNames[i]=cn[i];
    }

    for (j=0; j < d.length; j++) {

      for (k=0; k < d[0].length;k++) {
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
    if(c==0) return (new Boolean(true)).getClass();
    else return "".getClass();
  }

  /**
   * El sistema comprueba si una fila y columna es editable.
   * @param row Fila.
   * @param col Columna.
   * @return True o false.
   */
  public boolean isCellEditable(int row, int col) {

    if (col < getColumnCount() - 3 && col > 0) {
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

   if (col == getColumnCount() - 1) {
     String valor;

     if (value != null) {
       valor = (String)value;

       if (valor.length() > 0 && valor.indexOf("\"") == 0) {
         valor = valor.substring(1);
       }

       if (valor.length() > 0 && valor.indexOf("\"") == valor.length() - 1) {
         valor = valor.substring(0, valor.length() - 1);
       }

       data[row][col] = valor;
     }

   } else if (col==0) {
     data[row][col] = new Boolean(((Boolean) value).booleanValue());
     if(((Boolean)value).booleanValue()) {
       if (complementar) complementarChance(row);
       else complementarUtility(row);
     } else {
       data[row][getColumnCount() - 2] = new String("");
       data[row][getColumnCount() - 3] = new String("");
       data[row][getColumnCount() - 1] = new String("");    
     }

   } else {
     String valor = (String)value;
     try {
       double doble = Double.parseDouble(valor);
       double vFijo = Double.parseDouble((String)data[row][getColumnCount()-4]);
       if(col==getColumnCount()-3 && doble>vFijo) {
         valor = (String)data[row][getColumnCount()-4];
       } else {
         if(col==getColumnCount()-2 && doble<vFijo) {
           valor = (String)data[row][getColumnCount()-4];
         }
       }
     } catch(NumberFormatException e) {
       valor = "";
     }
     data[row][col] = valor;
   }
   fireTableCellUpdated(row, col); 
   if (DEBUG) {
     System.out.println("New value of data:");
     printDebugData();
   }

 }

  /**
   * Obtiene los complementarios de los rangos y cambia los no señalados .
   * Solo se activa para nodos con dos estados.
   * @param fila Fila.
   */
  private void complementarChance(int fila) {

    double v = 0.0;
    double valor = 0.0;
    boolean margi = complementar;
    int f = 0;
    int c = 0;
    int columna;
    int fFija = fila;

    if (validar) {

      if (numStates == 2) {
        validar = false;
        complementar = false;

        for (columna = getColumnCount() - 3; columna < getColumnCount() - 1;
             columna++) {
          fila = fFija;

          if (fila < getRowCount() / 2) {
            f = fila + (getRowCount() / 2);

            if (columna == getColumnCount() - 3) {
              c = columna + 1;
            }
            else {
              c = columna - 1;
            }

          }
          else {
            f = fila - (getRowCount() / 2);

            if (columna == getColumnCount() - 3) {
              c = columna + 1;
            }
            else {
              c = columna - 1;
            }

          }

	    if(((String)getValueAt(f,c)).equals("")) v=Double.NaN;
          else v = (new Double((String)getValueAt(f, c))).doubleValue();
	    if(((String)getValueAt(fila,columna)).equals("")) valor=Double.NaN;
          else valor = (new Double((String)getValueAt(fila, columna))).doubleValue();

          if((Double.toString(1-v)).equals("NaN")) setValueAt(new String(""),fila,columna);
	    else setValueAt(Double.toString(1 - v), fila, columna);
          data[fila][0] = new Boolean(true);
          setValueAt(new String(""), f, c);
          data[f][0] = new Boolean(false);
        }

        validar = true;
        complementar = margi;
      } else {
        complementarUtility(fila);
      }

    }

    fireTableDataChanged();
  }

  /**
   * Obtiene los complementarios de los rangos de utilidad .
   * @param fila Fila.
   */
  private void complementarUtility(int fila) {

    double v = 0.0;
    double valor = 0.0;
    boolean margi = complementar;
    int f = 0;
    int c = 0;
    int columna;
    int fFija = fila;

    if (validar) {
      validar = false;
      complementar = false;
      columna = getColumnCount() - 3;

      v = (new Double((String)getValueAt(fila, getColumnCount()-4))).doubleValue();

      if (((String)getValueAt(fila,columna)).equals("")) {
        setValueAt(Double.toString(v),fila,columna);
        setValueAt(Double.toString(v),fila,columna + 1);
        setValueAt(new Boolean(true),fila,0);
      } else {
        setValueAt(new String(""),fila,columna);
        setValueAt(new String(""),fila,columna + 1);
        setValueAt(new Boolean(false),fila,0);
      }

      validar = true;
      complementar = margi;
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
