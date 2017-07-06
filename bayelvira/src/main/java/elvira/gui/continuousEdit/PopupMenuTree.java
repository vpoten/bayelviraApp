/*
 * PopupMenuTree.java
 *
 * Created on 21 de octubre de 2003, 14:36
 */

package elvira.gui.continuousEdit;

import elvira.parser.*;
import elvira.potential.*;
import elvira.*;
import elvira.gui.*;
import elvira.gui.continuousEdit.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.regex.*;
/**
 *
 * @author  andrew
 */
public class PopupMenuTree implements ActionListener, ItemListener {
    
    JPopupMenu popup;
    CustomDialog customDialog;
    //Application F;
    PanelEditTree F;
    //JPanel jpanel;
    //public ContinuousProbabilityTreePaint Tc=null;

    public TreePaint tP=null;
    PopupListener popupListener;
     
    public Object getTree(boolean CPT){
            return tP.getTree(CPT);
    }
    /** Creates a new instance of PopupMenuTree */
    public PopupMenuTree(PanelEditTree F1) {
  
      F=F1;
      //jpanel=F1.jPanel1;
     //Crear popupMenu           
        popup=new JPopupMenu();
        
        
        tP=new TreePaint(F1);

     JMenuItem menuItem;
    
     
     menuItem=new JMenuItem("Asignar Variable");
     menuItem.addActionListener(this);
     popup.add(menuItem);

     menuItem=new JMenuItem("Asignar Hoja");
     menuItem.addActionListener(this);
     popup.add(menuItem);

     
     menuItem=new JMenuItem("Editar");
     menuItem.addActionListener(this);
     popup.add(menuItem);

     menuItem=new JMenuItem("Expandir");
     menuItem.addActionListener(this);
     popup.add(menuItem);
     
     menuItem=new JMenuItem("Contraer");
     menuItem.addActionListener(this);
     popup.add(menuItem);
     
     menuItem=new JMenuItem("Podar Nodo");
     menuItem.addActionListener(this);
     popup.add(menuItem);
     
     
     popupListener= new PopupListener();
     F.addMouseListener(popupListener);
    // menuBar.addMouseListener(popupListener);

//     customDialog = new CustomDialog(F1.parent);
//     customDialog.pack();

     
    }
    private Vector toCutPoints(String salida){

        String[] t;
        salida=salida.trim();
        salida=salida.substring(1,salida.length()-1);
        t=salida.split(",");
        Vector cutPoints=new Vector();
        for (int i=0; i<t.length; i++){
            t[i]=t[i].trim();
            cutPoints.addElement(new Double(t[i]));
        }
        return cutPoints;
        
    }
    public void actionPerformed(ActionEvent e) {
   
            JMenuItem source = (JMenuItem)(e.getSource());
            String s;
            
            s=source.getText();
            
            ContinuousProbabilityTreePaint T= tP.tC.extractTree(popupListener.pos_x,popupListener.pos_y);
            
            if (T!=null){
                
                         
            if (s.compareTo("Asignar Variable")==0){
                
                NodeList nodes=F.getRelation().getVariables();
                Vector nodelist= nodes.getNodes();
                String[] varNames=new String[nodelist.size()];
                
                for (int i=0; i<varNames.length; i++)
                    varNames[i]=new String(((Node)nodelist.elementAt(i)).getName());
                
                customDialog = new CustomDialog(F.parent,nodes,T);
                customDialog.pack();
                customDialog.setLocationRelativeTo(F);
                customDialog.setVisible(true);

                
                String varName= customDialog.getvarName();
                String salida = customDialog.getValidatedText();
                
               if (salida!=null){
                   
                        Vector cp= toCutPoints(salida);
                        T.editTree((Continuous)nodes.getNode(varName),cp);
                        tP.repaintTree();
               }else if (varName!=null){
                   T.editTree((FiniteStates)nodes.getNode(varName));                   
                   tP.repaintTree();
               }

               
            }else if (s.compareTo("Asignar Hoja")==0 && T.TreeC.isProbab()){
                        
                        //T=new ContinuousProbabilityTreePaint(new MixtExpDensity(0.0));
                        T.TreeC=new ContinuousProbabilityTree(new MixtExpDensity(0.0));
                        T.isLeaf=1;
                        customDialog = new CustomDialog(F.parent,T);
                        customDialog.pack();
                        customDialog.setLocationRelativeTo(F);
                        customDialog.setVisible(true);
  
                       
                        String salida;
                        salida = customDialog.getValidatedText();
                        if (salida!=null){

                                StringBufferInputStream f;
                                MixtExpDensity D=new MixtExpDensity(0.0);
                                f = new StringBufferInputStream(salida);
                                BayesNetParse parser = new BayesNetParse(f);
                                parser.initialize();
                                    
                                parser.Nodes=F.getRelation().getVariables();
                                
                                try{
                                    parser.DensityDeclaration(D);
                                    T.editTree(D);
                                    
                                }catch (ParseException e1) {
                                    System.out.println("Parse error: " + e1 + "\n");
                                    JOptionPane.showMessageDialog(null, e1, "Mixture Exponential Density Incorrecta", JOptionPane.ERROR_MESSAGE);
                                    //ShowMessages.showMessageDialog(ShowMessages.POT_TREE_INCOMP,JOptionPane.ERROR_MESSAGE);

                                }
                                
                                tP.repaintTree();

                        
                        }    
            
            }else if (s.compareTo("Expandir")==0){
             
                        if (tP!=null)tP.expandir(popupListener.pos_x,popupListener.pos_y);
                
            }else if (s.compareTo("Contraer")==0){
             
                        if (tP!=null)tP.contraer(popupListener.pos_x,popupListener.pos_y);
                
            }else if (s.compareTo("Editar")==0 && T.TreeC.isProbab()){
                        
                    
                        customDialog = new CustomDialog(F.parent,T);
                        customDialog.pack();
                        customDialog.setLocationRelativeTo(F);
                        customDialog.setVisible(true);
  
                       
                        String salida=null;
                        salida = customDialog.getValidatedText();
                        if (salida!=null){
                     
                            if (T.TreeC.isContinuous()){

                                T.editTree((Continuous)T.TreeC.getVar(),toCutPoints(salida));
                                tP.repaintTree();
                            }else if (T.TreeC.isProbab()){

                                StringBufferInputStream f;
                                MixtExpDensity D=new MixtExpDensity(0.0);
                                f = new StringBufferInputStream(salida);
                                BayesNetParse parser = new BayesNetParse(f);
                                parser.initialize();
                                    
                                parser.Nodes=F.getRelation().getVariables();
                                
                                try{
                                    parser.DensityDeclaration(D);
                                    T.editTree(D);
                                    
                                }catch (ParseException e1) {
                                    JOptionPane.showMessageDialog(null, e1, "Mixture Exponential Density Incorrecta", JOptionPane.ERROR_MESSAGE);
                                }
                                
                                tP.repaintTree();
                            
                            }
                                                       
                        }
                        
            }else if (s.compareTo("Podar Nodo")==0){
                    T.editTree();
                    tP.repaintTree();
            
            }
         }//del if T!=null
    }
    
        // Returns just the class name -- no package info.
    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");
        return classString.substring(dotIndex+1);
    }

    
    public void itemStateChanged(ItemEvent e) {
    }
    
    class PopupListener extends MouseAdapter {
        public int pos_x;
        public int pos_y;
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                pos_x=e.getX();
                pos_y=e.getY();
                
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }

}
