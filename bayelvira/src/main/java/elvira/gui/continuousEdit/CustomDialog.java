
package elvira.gui.continuousEdit;

import java.util.*;
import javax.swing.*;
import java.beans.*; //Property change stuff
import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

import elvira.potential.*;
import elvira.gui.continuousEdit.*;
import elvira.*;
import elvira.tools.statistics.math.Fmath;

class CustomDialog extends JDialog {

    private String typedText = null;
    private JOptionPane optionPane=null;
    public ContinuousProbabilityTreePaint TreeP=null;
    public String varName=null;
    final NodeList nL;
    public double[] limits;
    JComboBox varList;
    final JTextField textField = new JTextField(10);
    
    
    private static String ST1CONTINUOUS=new String("Introduce los nuevos Intervalos de la Variable Continua");
    private static String ST2CONTINUOUS=new String("(Ejemplo: (0,0.5,1) --> Indica los intervalos [0,0.5) y [0.5,1]");
    private static String ST1DISCRETE=new String("Introduce el nuevo valor de probabilidad Nodo Hoja");
    private static String ST2DISCRETE=new String("Ejemplo: 0.5, 0.3, 0.2, 1.");
    private static String ST1PROBAB=new String("Introduce el nuevo valor del Nodo Hoja");
    private static String ST2PROBAB=new String("(Ejemplos: 0.9+1.0*exp(1.0*X2) , 0.5");
    
    private int isContinuous=0;
    private String limites;
    public String getValidatedText() {
        return typedText;
    }
    public String getvarName(){
        
        return varName;
    }
    
    
//*************************************************************
    //Customdialog to add a new tree's node
    public CustomDialog(Frame parent, NodeList nL1, ContinuousProbabilityTreePaint T){
        super(parent,true);
        this.nL=nL1;
        TreeP=T;
        
        String[] varNames1=new String[nL.size()];
        int cont=0;
        for (int i=0; i<varNames1.length; i++){
         
            if (T.isExpandibleNode((Node)nL.elementAt(i))){
                   varNames1[cont]=new String(((Node)nL.elementAt(i)).getName());
                   cont=cont+1;
            }else{
                System.out.println("No expandible.");
            }
        }
        String[] varNames=new String[cont];
        for (int i=0; i<cont; i++){
                varNames[i]=varNames1[i];
        }
        
        varList = new JComboBox(varNames);
        varList.setSelectedIndex(0);
        //setContentPane(optionPane);

        varList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                varName = (String)cb.getSelectedItem();
            if (nL.getNode(varName).getClass()==Continuous.class){
                isContinuous=1;
                limits=TreeP.Limits((Continuous)nL.getNode(varName));
                String limites=new String("["+Fmath.truncate(limits[0],ContinuousProbabilityTreePaint.PRINT_LIMIT_PRECISION)+", "+Fmath.truncate(limits[1],ContinuousProbabilityTreePaint.PRINT_LIMIT_PRECISION)+"]");

                final String msgString1 = "Seleccione el Nombre de la Variable a Expandir";
                final String msgString2 = "Introduce los puntos de corte separados por comas del siguiente intervalo: ";
                final String msgString3 = limites;
                Object [] array = {msgString1, varList, msgString2, msgString3, textField};

                optionPane.setMessage(array);
                //newOptionPane();

                //remove(optionPane);
                //setContentPane(optionPane);
                //this.pack();
            }else{
                isContinuous=0;
                limites=new String("");

                final String msgString1 = "Seleccione el Nombre de la Variable a Expandir";
                final String msgString2 = "No ha de aparecer en el camino hacia la raiz del arbol                     ";
                final String msgString3 = "--------------------------------";
                final String msgString4 = "--------------------------------";
                Object[] array = {msgString1,msgString2,  varList, msgString3, msgString4};
                optionPane.setMessage(array);
            
            }
            }
        });

        varName = (String)varList.getSelectedItem();

        newOptionPane();
        
        



        
    }
    
    private void newOptionPane(){
     
                           
        final String btnString1 = "Enter";
        final String btnString2 = "Cancel";
        Object[] options = {btnString1, btnString2};
        //textField = new JTextField(10);
        
        //if (optionPane!=null)remove(optionPane);
        if (nL.getNode(varName).getClass()==Continuous.class){
            limits=TreeP.Limits((Continuous)nL.getNode(varName));
            String limites=new String("["+Fmath.truncate(limits[0],ContinuousProbabilityTreePaint.PRINT_LIMIT_PRECISION)+", "+Fmath.truncate(limits[1],ContinuousProbabilityTreePaint.PRINT_LIMIT_PRECISION)+"]");
            isContinuous=1;
            final String msgString1 = "Seleccione el Nombre de la Variable a Expandir";
            final String msgString2 = "Introduce los puntos de corte separados por comas del siguiente intervalo: ";
            final String msgString3 = limites;
            Object [] array = {msgString1, varList, msgString2, msgString3, textField};


            optionPane = new JOptionPane(array, 
                                        JOptionPane.QUESTION_MESSAGE,
                                        JOptionPane.YES_NO_OPTION,
                                        null,
                                        options,
                                        options[0]);
            setContentPane(optionPane);

            
        }else{
            isContinuous=0;
            limites=new String("");

            final String msgString1 = "Seleccione el Nombre de la Variable a Expandir";
            final String msgString2 = "No ha de aparecer en el camino hacia la raiz del arbol                     ";
            final String msgString3 = "--------------------------------";
            final String msgString4 = "--------------------------------";
            Object[] array = {msgString1,msgString2,  varList, msgString3, msgString4};

            optionPane = new JOptionPane(array, 
                                        JOptionPane.QUESTION_MESSAGE,
                                        JOptionPane.YES_NO_OPTION,
                                        null,
                                        options,
                                        options[0]);
            setContentPane(optionPane);
        }
        
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
                            
                          if (isContinuous==1){
                            boolean error=false;
                            typedText = textField.getText();
                            String salida = new String(typedText);
                            salida=salida.trim();
                                 if (isVectorCutPoints(salida)){
                                        String[] t=salida.split(",");
                                        Vector cutPoints=new Vector();
                                        for (int i=0; i<t.length; i++){
                                            t[i]=t[i].trim();
                                            cutPoints.addElement(new Double(t[i]));
                                            if(i==0 && ((Double)cutPoints.elementAt(i)).doubleValue()<limits[0]){
                                                error=true;
                                            }else if(i==t.length-1 && ((Double)cutPoints.elementAt(i)).doubleValue()>limits[1]){
                                                error=true;
                                            }else if(i!=0 && ((Double)cutPoints.elementAt(i-1)).doubleValue()>=((Double)cutPoints.elementAt(i)).doubleValue()){
                                                error=true;
                                            }

                                        }

                                        if (error){
                                            JOptionPane.showMessageDialog(null, "Verifica que los puntos de corte estan:\n Ordenados y dentro del rango:"+salida.valueOf(limits[0])+","+salida.valueOf(limits[1]), "Vector Cut Points Error", JOptionPane.ERROR_MESSAGE);
                                            typedText=null;
                                            varName=null;
                                            setVisible(true);
                                        }else{
                                            typedText="["+salida.valueOf(limits[0])+", "+typedText+", "+salida.valueOf(limits[1])+")";
                                            setVisible(false);
                                        }
                                 }else{
                                     
                                    JOptionPane.showMessageDialog(null, "Verifica la sintaxis: ({FLOAT},)*{FLOAT}", "Vector Cut Points Error", JOptionPane.ERROR_MESSAGE);
                                    typedText=null;
                                    varName=null;
                                    setVisible(true);
                                 }
                        
                          
                          }else{
                              
                               typedText=null;
                               setVisible(false);
                          }
                          
                          
                        
                            
                    } else { // user closed dialog or clicked cancel
                        typedText = null;
                        varName=null;
                        setVisible(false);
                    }
                }
            }
        });

      
        
    }
    
//*************************************************************    
    //Customdialog to edit a tree's node
    public CustomDialog(Frame parent, ContinuousProbabilityTreePaint TreeP) {
                super(parent,true);
                nL=null;
                this.TreeP=TreeP;
                if (TreeP.TreeC.isContinuous()){
                    CustomDialog1(parent,ST1CONTINUOUS,ST2CONTINUOUS);
                }
                if (TreeP.TreeC.isProbab()){
                    CustomDialog1(parent,ST1PROBAB,ST2PROBAB);
                    if (TreeP.isVacio==0)
                        textField.setText(TreeP.Nodo.name);
                }
                
                if (TreeP.TreeC.isDiscrete()){
                    CustomDialog1(parent,ST1DISCRETE,ST2DISCRETE);
                }
             
    }
    
    
    public  void CustomDialog1(Frame parent, String ST1, String ST2) {
    

         setTitle("Quiz");
         
         
        
        final String msgString1 = new String(ST1);
        final String msgString2 = new String(ST2);
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
                            String s=typedText;
                            
                            setVisible(false);
                        
                            
                    } else { // user closed dialog or clicked cancel
                        typedText = null;
                        setVisible(false);
                    }
                }
            }
        });
        
    }
    
    private boolean isVectorCutPoints(String s){
        String FLOAT = new String("(\\p{Digit}+)|(\\p{Digit}+\\.\\p{Digit}+)");
        String FLOAT_COMA= new String("(("+FLOAT+")\\,)*"+"("+FLOAT+")");
        String REGEX=new String("("+FLOAT_COMA+")");
        
        return Pattern.matches(REGEX,s);
    
    }


}
