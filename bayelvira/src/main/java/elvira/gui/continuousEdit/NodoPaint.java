/*
 * NodoPaint.java
 *
 * Created on 20 de octubre de 2003, 12:05
 */

package elvira.gui.continuousEdit;

import java.util.Vector;
import elvira.*;
import elvira.potential.*;
import java.awt.*;

/**
 *
 * @author  andrew
 */
public class NodoPaint extends java.lang.Object {
    
    
    public static final Color NODE_COLOR = new Color(255,255,200);

    public static final Color ARC_COLOR=Color.darkGray;    
    
    public static final Color NODE_NAME_COLOR = Color.black;
    
    public static final Color EXPANDIDO_NODE_COLOR = new Color(255,102,102);
    public static final Color VACIO_NODE_COLOR = new Color(102,153,255);
    public static Color LEAF_NODE_COLOR = new Color(204,102,255);
   
    private int x = 0;
    
    private int y = 0;
    
    public static int width = 20;
    
    public static int height = 20;
    
    public String name="VACIO";
    
    private static int isContinuous=0;
    
    private static int isFiniteStates=0;
    
    //private static int isLeaf=0;
    
    private static Vector cp;
    
    /** Creates a new instance of NodoPaint */
    public NodoPaint(){
        
    }
  /*  public NodoPaint(int x, int y) {
        
        setX(x);
        setY(y);
    }
  */  
    
    public NodoPaint(Continuous Var, Vector cp_u){
     
           isContinuous=1;
           name=Var.getName();
           cp=(Vector)cp_u.clone();
        
    }
    
    public NodoPaint(FiniteStates Var){
           isFiniteStates=1;
           name=Var.getName();
    }

    public NodoPaint(MixtExpDensity f){
        
         name=f.ToString();
              
    }
    public void paintNodo(java.awt.Graphics g){
        
        g.setColor(NODE_COLOR);   
        g.fillRect(x,y,width,height);
        g.setColor(NODE_NAME_COLOR);   
        g.drawString(name,x+width/2-10,y+height/2-10);
       
   }
   public void paintNodo(java.awt.Graphics g, int isLeaf, int isExpandido, int isVacio){
        
        if (isLeaf==1){
                g.setColor(LEAF_NODE_COLOR);      
        }else if (isExpandido==0){
                g.setColor(EXPANDIDO_NODE_COLOR);      
        }else if (isVacio==1){
                g.setColor(VACIO_NODE_COLOR);      
        }else{
                g.setColor(NODE_COLOR);   
        }
        
        if (isLeaf==0){
        g.fillRect(x,y,width,height);
        g.setColor(NODE_NAME_COLOR);   
        g.drawString(name,x+3,y+height/2+3);
        }else
            g.drawString(name,x+width/2,y+height/2);
    }

    public int getX(){
        
     return x;   
    }
    
    public int getY(){
     
        return y;
    }
    
    public void setX(int x_){
     x=x_;
     }
    public void setY(int y_){
     y=y_;   
        
    }
    public boolean pertenecePoint(int x1,int y1){
        if (x1<x+width)
            if (y1<y+height)
                return true;
        
        return false;
        
    }
}
