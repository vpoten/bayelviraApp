/*
 * IntroEvidenceDialog.java
 *
 * Created on 28 de noviembre de 2003, 11:25
 */

package elvira.gui.explication;


import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JTextField;
import java.beans.*; //Property change stuff
import java.awt.event.*;

import elvira.*;
/**
 *
 * @author  andrew
 */
public class IntroEvidenceDialog extends JDialog{
    
    private String typedText = null;

    private Continuous node;
    
    private JOptionPane optionPane;
    
    private double evidence=0.0;

    public boolean isValidEvidence() {
        
        return typedText!=null;
    }
    
    public double getEvidence(){
        return evidence;
    }

    /** Creates a new instance of IntroEvidenceDialog */
    public IntroEvidenceDialog(java.awt.Dialog dialog, Continuous n) {
       
        super(dialog, true);
        
        this.node=n;
        setTitle("Intro Evidence");

        final String msgString1 = "Introduce un valor como evidencia de la variable continua.";
        final String msgString2 = "Rango: ["+Double.toString(node.getMin())+", "+Double.toString(node.getMax())+"].";
        final JTextField textField = new JTextField(10);
        Object[] array = {msgString1, msgString2, textField};

        final String btnString1 = "Enter";
        final String btnString2 = "Cancel";
        Object[] options = {btnString1, btnString2};

        optionPane = new JOptionPane(array, 
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);
        setContentPane(optionPane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
                    optionPane.setValue(new Integer(
                                        JOptionPane.CLOSED_OPTION));
            }
        });

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                optionPane.setValue(btnString1);
            }
        });

        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (isVisible() 
                 && (e.getSource() == optionPane)
                 && (prop.equals(JOptionPane.VALUE_PROPERTY) ||
                     prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
                    Object value = optionPane.getValue();

                    if (value == JOptionPane.UNINITIALIZED_VALUE) {
                        //ignore reset
                        return;
                    }

                    // Reset the JOptionPane's value.
                    // If you don't do this, then if the user
                    // presses the same button next time, no
                    // property change event will be fired.
                    optionPane.setValue(
                            JOptionPane.UNINITIALIZED_VALUE);

                    if (value.equals(btnString1)) {
                            typedText = textField.getText();
                        
                        
                        evidence = Double.valueOf(typedText).doubleValue();
                        
                        if (evidence < node.getMin() || evidence > node.getMax()){
                            textField.selectAll();
                            JOptionPane.showMessageDialog(
                                            IntroEvidenceDialog.this,
                                            "Sorry, \"" + typedText + "\" "
                                            + "isn't into the especified range.\n"
                                            + "Please enter new value.", "Try again",
                                            JOptionPane.ERROR_MESSAGE);
                            typedText = null;
                        }else{
                            //setVisible(false);
                            hide();
                        }
                    } else { // user closed dialog or clicked cancel
                        typedText = null;
                        //setVisible(false);
                        hide();
                    }
                }
            }
        });
      
        
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
/*        Dialog a = new Dialog();
        a.setVisible(true);
        a.pack();
        IntroEvidenceDialog intro=new IntroEvidenceDialog(a,new Continuous("a"));
        intro.setVisible(true);*/
    }
    
}
