/*
 * PanelEditTree.java
 *
 * Created on 28 de octubre de 2003, 9:53
 */

/**
 *
 * @author  andrew
 */

package elvira.gui.continuousEdit;


import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import elvira.gui.continuousEdit.*;
import elvira.*;
import elvira.potential.*;


/**
 * This class allow to show a probablity tree, discrete or continuous. 
 * Only it need a frame as a container and a ContinuousProbabilityTree or ProbabilityTree.
 * @autor andrew
 */
public class PanelEditTree extends JPanel{

    /** Creates a new instance of PanelEditTree */
    public PanelEditTree(Frame parent1) {
            
            parent=parent1;
            ini_components();
            g1 = getGraphics();
            popupMenuTree= new PopupMenuTree(this);
            repaint();
    
    }
    public void setRelation(Relation R){
        
            this.R=R;
        
    }
    public Relation getRelation(){
        
            return R;
        
    }
    public boolean isCPT(){
        Enumeration E = R.getVariables().elements();
        boolean CPT=false;
        for (;E.hasMoreElements();){
            if (E.nextElement().getClass()==Continuous.class)
                CPT=true;
        }
        return CPT;

        
    }
    public Object getTree(){
        //Vemos si hay que devolver devolver un ContinuousProbabilityTree o un ProbabilityTree    
        
        return popupMenuTree.getTree(isCPT());
        
    }
    public void setTree(ContinuousProbabilityTree T){
        
        popupMenuTree.tP=new TreePaint(this,T);
//        popupMenuTree.tP.
  //      popupMenuTree.tP.tC.TreeC=new ContinuousProbabilityTree(T);
    
    }

    public void setTree(ProbabilityTree T){
        
        popupMenuTree.tP=new TreePaint(this,T);
    
    }

    void ini_components(){
         //    jPanel1 = new javax.swing.JPanel();

      //  getContentPane().setLayout(new java.awt.BorderLayout(10, 10));

/*        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });*/

       // setLayout(new java.awt.GridLayout(1, 20));

       setBorder(new javax.swing.border.MatteBorder(null));
       // setMinimumSize(new java.awt.Dimension(20, 20));
        setPreferredSize(new java.awt.Dimension(1000, 1000));
       // setAutoscrolls(true);

        //getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);
        //getAccessibleContext().setAccessibleParent(this);

   
   
    }
  
    /**
     * Get the ContinuousProbabilityTreePaint wich is painted in this panel. 
     */
    public ContinuousProbabilityTreePaint getCPTreePaint(){
        return popupMenuTree.tP.tC;
    }
    
    public void paint(Graphics g){
        super.paint(g);
        if (popupMenuTree.tP!=null)
            if (popupMenuTree.tP.tC!=null)
                popupMenuTree.tP.tC.paintTree(g);
    }
  
    public Relation R;
    public static Graphics g1;
    public Frame parent;
    
    JPopupMenu popup;
    JTextArea output;
    JScrollPane scrollPane;
    
    PopupMenuTree popupMenuTree;
    
    
}
