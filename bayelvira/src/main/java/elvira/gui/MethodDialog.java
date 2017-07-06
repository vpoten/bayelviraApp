/* MethodDialog.java */

package elvira.gui;

import javax.swing.*;
import java.awt.Frame;
import java.util.Vector;

/**
 * This class contains all the common variables and methods
 * of the dialogs that allow select the propagation and learning
 * method
 *
 * @author Roberto Atienza
 */

public abstract class MethodDialog extends javax.swing.JDialog {
    
   public MethodDialog (Frame parent) {
      super(parent);
   }
   
   /**
    * Get the value contained in the TextField <CODE>p</CODE> and
    * stores it in the Vector <CODE>p</CODE> as an integer
    *
    * @param tf TextField where the integer value is
    * @param p Vector where the value is stored
    */
   
   public void getIntegerValue(JTextField tf, Vector p) {           
      if (tf.isVisible()) {
         try {
            Integer i = Integer.valueOf(tf.getText());
            if (i.intValue()>=0)
               p.add(i);
            else
               ShowMessages.showMessageDialog("The value must be an integer >= 0",
                  JOptionPane.ERROR_MESSAGE);               
         }
         catch (NumberFormatException e) {
            ShowMessages.showMessageDialog("The value must be an integer >= 0",
                  JOptionPane.ERROR_MESSAGE);
         }
      }      
   }
   
   
   /**
    * Get the value contained in the TextField <CODE>tf</CODE> and
    * stores it in the Vector <CODE>p</CODE> as a double
    *
    * @param tf TextField where the double value is
    * @param p Vector where the value is stored
    */
    
   public void getDoubleValue(JTextField tf, Vector p) {            
      if (tf.isVisible()) {
         try {
            Double d = Double.valueOf(tf.getText());
            if (d.doubleValue()>=0) 
               p.add(d);
            else
               ShowMessages.showMessageDialog("The value must be a double >= 0",
                  JOptionPane.ERROR_MESSAGE);
         }
         catch (NumberFormatException e) {
            ShowMessages.showMessageDialog("The value must be a double >= 0",
                  JOptionPane.ERROR_MESSAGE);
         }
      }            
   }
   
   
   /**
    * Get the value contained in the TextField <CODE>tf</CODE> and
    * stores it in the Vector <CODE>p</CODE> as a string
    *
    * @param tf TextField where the string is.
    * @param p Vector where the value is stored
    * @param errorMessage String with the error that will be printed
    * if there is any problem getting the value
    */

   public void getStringValue (JTextField tf, Vector p, String errorMessage) {
      if (tf.isVisible())
         if (tf.getText()==null) 
            ShowMessages.showMessageDialog(errorMessage, 
               JOptionPane.ERROR_MESSAGE);
         else
            p.add(tf.getText());
      
   }
   
   
   public abstract Vector getParameters();
      
   public abstract void fillTextFields (Vector parameters, Vector filesNames);
    
}