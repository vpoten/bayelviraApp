/*
 * TreePaint.java
 *
 * Created on 23 de octubre de 2003, 10:30
 */

package elvira.gui.continuousEdit;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import elvira.*;
import elvira.gui.continuousEdit.*;
import elvira.potential.*;
/**
 *
 * @author  andrew
 */
public class TreePaint {

    public ContinuousProbabilityTreePaint tC=null;
    PanelEditTree F;
    public Object getTree(boolean CPT){
        return tC.getTree(CPT);
    }
    
    /** Creates a new instance of TreePaint */
    public TreePaint(PanelEditTree F1 ) {
        F=F1;
        tC=new ContinuousProbabilityTreePaint();
    }
    public TreePaint(PanelEditTree F1, ContinuousProbabilityTree T){
        F=F1;
        tC=new ContinuousProbabilityTreePaint(T);
                    
    }
    
    public TreePaint(PanelEditTree F1, ProbabilityTree T){
        F=F1;
        tC=new ContinuousProbabilityTreePaint(new ContinuousProbabilityTree(T));
                    
    }

    
    public void insertaNodo(int x, int y, Continuous var,Vector cp){
    
       if (tC==null){
           tC=new ContinuousProbabilityTreePaint(var,cp);
           repaintTree();
       }else{
           
           ContinuousProbabilityTreePaint tC2=tC.extractTree(x,y);
           if (tC2!=null){
               tC2.editTree(var,cp);
                   
           }
           repaintTree();
           
       }
       
       
   }
   public void expandir(int x, int y){
           
           ContinuousProbabilityTreePaint tC2=tC.extractTree(x,y);
           if (tC2!=null){
               tC2.isExpandido=1;
               repaintTree();
           }

       
   }
   public void contraer(int x, int y){
           
           ContinuousProbabilityTreePaint tC2=tC.extractTree(x,y);
           if (tC2!=null){
               tC2.isExpandido=0;
               repaintTree();
           }

       
   }
   public void insertarHoja(int x, int y, String s){
           ContinuousProbabilityTreePaint tC2=tC.extractTree(x,y);
            if (tC2!=null){
                tC2.Nodo.name=new String(s);
                tC2.isLeaf=1;
               repaintTree();
           }
       
   }
   public void repaintTree(){
        
        //jpanel.setVisible(false);
        //jpanel.setVisible(true);
       //jpanel.updateUI(); 
       //jpanel.setBackground(new Color(100,100,100));
      // jpanel.update(g);
       F.repaint();
       //tC.paintTree(g);
   }
    
   public void new_CPTP(){
   }
    
}
