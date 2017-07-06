/*ColorEditor.java*/

package elvira.sensitivityAnalysis;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JCheckBox;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
 * The editor button that brings up the dialog.
 * We extend DefaultCellEditor for convenience,
 * even though it mean we have to create a dummy
 * check box.  Another approach would be to copy
 * the implementation of TableCellEditor methods
 * from the source code for DefaultCellEditor.
 * @author jruiz
 */
class ColorEditor extends DefaultCellEditor {
  Color currentColor = null;

  public ColorEditor(JButton b) {
    super(new JCheckBox()); //Unfortunately, the constructor
    //expects a check box, combo box,
    //or text field.
    editorComponent = b;
    setClickCountToStart(1); //This is usually 1 or 2.

    //Must do this so that editing stops when appropriate.
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fireEditingStopped();
      }
    });
  }

  protected void fireEditingStopped() {
    super.fireEditingStopped();
  }

  public Object getCellEditorValue() {
    return currentColor;
  }

  public Component getTableCellEditorComponent(JTable table,
                                               Object value,
                                               boolean isSelected,
                                               int row,
                                               int column) {
    ((JButton)editorComponent).setText(value.toString());
    currentColor = (Color)value;
    return editorComponent;
  }

} //End of class