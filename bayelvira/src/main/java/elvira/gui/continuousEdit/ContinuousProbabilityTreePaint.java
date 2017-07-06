/*
 * ContinousProbabilityTreePaint.java
 *
 * Created on 21 de octubre de 2003, 10:32
 */

package elvira.gui.continuousEdit;


import java.io.*;
import java.awt.*;
import javax.swing.tree.*;
import java.util.*;
import elvira.*;
import elvira.gui.continuousEdit.*;
import elvira.potential.*;
import elvira.parser.*;
import elvira.tools.statistics.math.Fmath;
/**
 *
 * @author  andrew
 */
public class ContinuousProbabilityTreePaint extends DefaultMutableTreeNode{
    
    public static int PRINT_LIMIT_PRECISION=2;
    
    public ContinuousProbabilityTree TreeC=null;
    
//    public DefaultMutableTreeNode TreeP;
    public NodoPaint Nodo=null;
    
    
    public int paint_x=10;
    public int paint_y=10;
    
    public int jump_x=50;
    public int jump_y=8;
    
    public int isLeaf=0;
    public int isVacio=0;
    
    
    public int isExpandido=1;
    
    //public double min;
    //public double max;
    
    public Object getTree(boolean CPT){
        

            if (CPT){
              if (isLeaf==0){
                Enumeration childs=children();
                for (int i=0; i<getChildCount();i++)
                        TreeC.setChild((ContinuousProbabilityTree)((ContinuousProbabilityTreePaint)childs.nextElement()).getTree(CPT),i);
              } 
              return TreeC;
            }else{
                ProbabilityTree T=null;
                if (isLeaf==0){
                    T=new ProbabilityTree((FiniteStates)TreeC.getVar());
                    Enumeration childs=children();
                    int fin=T.getChild().size();
                    for (int i=0; i<getChildCount(); i++)
                        T.replaceChild((ProbabilityTree)((ContinuousProbabilityTreePaint)childs.nextElement()).getTree(CPT),i);
                }else{
                    T=new ProbabilityTree(TreeC.getProb().getIndependent());
                    
                }
                return T;
            }
    }
    /** Creates a new instance of ContinuousProbabilityTreePaintPaint */
    public ContinuousProbabilityTreePaint() {
            isLeaf=1;
            MixtExpDensity f = new MixtExpDensity();
            Nodo=new NodoPaint(f);
            TreeC = new ContinuousProbabilityTree(f);        
            
    }
    
    public ContinuousProbabilityTreePaint(MixtExpDensity f){
            isLeaf=1;
            Nodo = new NodoPaint(f);
            TreeC = new ContinuousProbabilityTree(f);        
    }
    public ContinuousProbabilityTreePaint(ContinuousProbabilityTree T){
        int i;
        
        if (T.isDiscrete()){
            TreeC=new ContinuousProbabilityTree((FiniteStates)T.getVar());
            Nodo = new NodoPaint((FiniteStates)T.getVar());
        }else if (T.isContinuous()){
            TreeC=new ContinuousProbabilityTree((Continuous)T.getVar(),T.getCutPoints());
            Nodo = new NodoPaint((Continuous)T.getVar(),T.getCutPoints());
            
        }else if(T.isProbab()){
            isLeaf=1;
            Nodo = new NodoPaint(T.getProb());
            TreeC = new ContinuousProbabilityTree(T.getProb());
        }else {
            isLeaf=1;
            MixtExpDensity f = new MixtExpDensity();
            Nodo=new NodoPaint(f);
            TreeC = new ContinuousProbabilityTree(f);        
             return;
        }
        
        
        if (!T.isProbab())
        for (i=0;i<T.getNumberOfChildren();i++){
                  insert(new ContinuousProbabilityTreePaint(T.getChild(i)),i);
                  //TreeC.setChild(((ContinuousProbabilityTreePaint)getChildAt(i)).getTree(),i);
        }
        

        
        
    }
    public ContinuousProbabilityTreePaint(Continuous Var, Vector cp) {
            
            int i;
            TreeC = new ContinuousProbabilityTree(Var,cp);
            
            Nodo = new NodoPaint(Var,cp);
            
            for (i=0;i<cp.size()-1;i++){
                  insert(new ContinuousProbabilityTreePaint(),i);
            }
            
      
    }
    
    public ContinuousProbabilityTreePaint(ContinuousProbabilityTreePaint tree,Continuous Var, Vector cp) {
   
            TreeC = new ContinuousProbabilityTree(Var,cp);
        
            Nodo = new NodoPaint(Var,cp);
            
            
            
            tree.TreeC.insertChild(TreeC);
            
            tree.add(this);
        
    
    }
    
     
   public int paintTree(Graphics g){
    
       int i=0;
       int cont_paint_x=paint_x+jump_x+Nodo.width;
       int cont_paint_y=paint_y+jump_y+Nodo.height;

       //NodoPaint nodo=(NodoPaint)TreeP.getUserObject();
       ContinuousProbabilityTreePaint T;
    if (Nodo!=null){
       Nodo.setX(paint_x);
       Nodo.setY(paint_y);
       Nodo.paintNodo(g,isLeaf,isExpandido,isVacio);
       
       
          
       if (isExpandido==1){   
       Enumeration childs=children();

        
       
       for(i=0;childs.hasMoreElements();i++){
            
            T=((ContinuousProbabilityTreePaint)childs.nextElement());
                        
            T.paint_x=cont_paint_x;
            T.paint_y=cont_paint_y;

          
            //Pintamos la lìnea que va hacia el.
            int x1=paint_x+Nodo.width/2;
            int y1=paint_y+Nodo.height;
            int x2=paint_x+Nodo.width/2;
            int y2=cont_paint_y+Nodo.height/2;
            int x3=cont_paint_x;
            int y3=cont_paint_y+Nodo.height/2;
            
            g.setColor(NodoPaint.ARC_COLOR);      
            g.drawLine(x1,y1,x2,y2);
            g.drawLine(x2,y2,x3,y3);
            
            String t=new String("");
            String s;
            if (TreeC.isContinuous()){
                s="["+t.valueOf(Fmath.truncate(TreeC.getCutPoint(i),ContinuousProbabilityTreePaint.PRINT_LIMIT_PRECISION))+", "+t.valueOf(Fmath.truncate(TreeC.getCutPoint(i+1),ContinuousProbabilityTreePaint.PRINT_LIMIT_PRECISION))+")";
            }else{
                s="= "+t.valueOf(((FiniteStates)TreeC.getVar()).getState(i));
            }
                
           
            g.drawString(s,x2,y2);
            
            cont_paint_y=T.paintTree(g);
            
            
        }

       }
    }
       return cont_paint_y;
   }
    
   public ContinuousProbabilityTreePaint extractTree(int x, int y){

        
        if (Nodo.pertenecePoint(x,y)){
            return this;
        }else if (isExpandido==1){

            int i;
            ContinuousProbabilityTreePaint T;
           
            Enumeration childs=children();
        
        
       
       
            for(i=0;childs.hasMoreElements();i++){
            
                T=((ContinuousProbabilityTreePaint)childs.nextElement());
            
                T=T.extractTree(x,y);
                if (T!=null)
                        return T;
                
            }
        }

        return null;
    }
   
   public void editTree(Continuous Var, Vector cp){
        int i;
       
       TreeC = new ContinuousProbabilityTree(Var,cp);
       Nodo = new NodoPaint(Var,cp);
       if (isVacio==0){
            removeAllChildren();
       }
       for (i=0;i<cp.size()-1;i++){
                  insert(new ContinuousProbabilityTreePaint(),i);
                  //TreeC.setChild(((ContinuousProbabilityTreePaint)getChildAt(i)).getTree(),i);
       }

       isVacio=0;
       isLeaf=0;
   }


   public void editTree(FiniteStates Var){
        int i;
       
       TreeC = new ContinuousProbabilityTree(Var);
       Nodo = new NodoPaint(Var);
       if (isVacio==0){
            removeAllChildren();
       }
       for (i=0;i<Var.getNumStates();i++){
                  insert(new ContinuousProbabilityTreePaint(),i);
                  //TreeC.setChild(((ContinuousProbabilityTreePaint)getChildAt(i)).getTree(),i);
       }

       isVacio=0;
       isLeaf=0;
   }
   
   //Escribe hoja
   public void editTree(MixtExpDensity D){
    
            isVacio=0;
            isLeaf=1;
            Nodo = new NodoPaint(D);
            TreeC = new ContinuousProbabilityTree(D);
 
   }
   //Lo marca como vacio
   public void editTree(){
       if (isVacio==0){
            removeAllChildren();
       }
       TreeC=null;
       isLeaf=1;
       MixtExpDensity f = new MixtExpDensity();
       Nodo=new NodoPaint(f);
       TreeC = new ContinuousProbabilityTree(f);        
       
       isVacio=0;
       isLeaf=1;
  
   }
   public double[] Limits(Continuous var){
       
       ContinuousProbabilityTreePaint treeP1=null,treeP2=null;
       int i;
       double[] limits= new double[2];
       
       Enumeration path=pathFromAncestorEnumeration(getRoot());
       TreeNode[] path2=getPath();
       
       
       int level=-1;
       for (i=0; path.hasMoreElements(); i++){
           treeP1=(ContinuousProbabilityTreePaint)path.nextElement();
           if (treeP1.TreeC!=null){
           if (path.hasMoreElements() && treeP1.TreeC.getVar().getName().equals((String)var.getName())){
                treeP2=treeP1;
                level=i;
           }
           }
       }
       
       
       if (treeP2==null){
        limits[0]=var.getMin();
        limits[1]=var.getMax();
       }else{
        limits[0]=treeP2.TreeC.getCutPoint(path2[level].getIndex(path2[level+1]));
        //limits[1]=treeP2.TreeC.getCutPoint(treeP2.TreeC.getCutPoints().size()-1);
        limits[1]=treeP2.TreeC.getCutPoint(path2[level].getIndex(path2[level+1])+1);
       }
       
       return limits;
   }

    public boolean isExpandibleNode(Node var){
           
       ContinuousProbabilityTreePaint treeP1=null;
       int i;

       if (var.getClass()==Continuous.class){
            return true;
       }else{
       Enumeration path=pathFromAncestorEnumeration(getRoot());
       TreeNode[] path2=getPath();
       
       
       int level=-1;
       for (i=0; path.hasMoreElements(); i++){
           treeP1=(ContinuousProbabilityTreePaint)path.nextElement();
           if (treeP1.TreeC!=null){
           if (path.hasMoreElements() && treeP1.TreeC.getVar().getName().equals((String)var.getName())){
                level=i;
           }
           }
       }
       
       
       if (level==-1)
           return true;
       else
           return false;
        
      }
    }
    

}
   
    

