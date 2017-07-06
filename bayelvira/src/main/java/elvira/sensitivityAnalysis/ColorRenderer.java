/*ColorRenderer.java*/

package elvira.sensitivityAnalysis;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.BorderFactory;
import javax.swing.table.TableCellRenderer;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Color;

  /**
   * Clase para poder introducir colores en celdas de una tabla.
   * @author jruiz
   * @version 1.0
   */
public class ColorRenderer extends JLabel implements TableCellRenderer {

  Border unselectedBorder = null;
  Border selectedBorder = null;
  boolean isBordered = true;

  public ColorRenderer(boolean isBordered) {

    super();
    this.isBordered = isBordered;
    setOpaque(true); //MUST do this for background to show up.
  }

  public Component getTableCellRendererComponent(
      JTable table, Object color,
      boolean isSelected, boolean hasFocus,
      int row, int column) {

    setBackground((Color)color);

    if (isBordered) {

      if (isSelected) {

        if (selectedBorder == null) {
          selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
              table.getSelectionBackground());
        }

        setBorder(selectedBorder);
      } else {

        if (unselectedBorder == null) {
          unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
              table.getBackground());
        }

        setBorder(unselectedBorder);
      }

    }

    return this;
  }

} //End of class