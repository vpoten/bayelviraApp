/*
 * HierarchyBnet.java
 *
 * Created on 12 de diciembre de 2006, 12:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package elvira;


import java.io.*;
import java.util.Vector;


/**
 *
 * @author smc
 */
public class HierarchyBnet extends Hierarchy {
    
public FiniteStates auxvar;

public Bnet net;
    
    
    
    /** Creates a new instance of HierarchyBnet */
    public HierarchyBnet() {
    }
   
     
    /** Creates a new instance of NodeHierarchy */
    public HierarchyBnet(FiniteStates x) {
     
        
      super(x);
      
      auxvar=null;
      net=null;
        
        
    }
     
    
       
    /** Creates a new instance of NodeHierarchy */
    public HierarchyBnet(Hierarchy h) {
     
        HierarchyBnet hc;
        int i,n;
        
        reference = h.reference;
        type = h.type;
        nchild = h.nchild;
        name="Valuesof"+h.name;
         children = new Vector();
         
         if (nchild>0) {auxvar=new FiniteStates(nchild);
         auxvar.setName(name);}
         else {auxvar=null;}
         
         
         
         for (i=0; i<nchild; i++){
          hc = new HierarchyBnet( h.getChildat(i));
          children.addElement(hc);
         
         }
         
        members = new boolean[reference.getNumStates()];
        
        n=0;
        
        for(i=0;i<reference.getNumStates(); i++){
            
            members[i] = h.members[i];
            if (members[i]) {n++;}
        }  
     
      if (type == LEAF){
            
       auxvar=new FiniteStates(n);
       auxvar.setName(name);  
            
      }
     
     
        
        
    }
     
        
/***********************
 *
 * Add a child to a node with the given defnition
 *
 **********************/    
    
 public void addChild(boolean definition[]){
     
      HierarchyBnet y;
      
      
     
        type = INNER;
         nchild ++;
        
         y = new HierarchyBnet(reference);
         y.members = definition;
         children.addElement (y);
        
        
     
     
 }    
    
 
 public FiniteStates getAuxVar(){
     
     return (auxvar);
 }
    
    
}
