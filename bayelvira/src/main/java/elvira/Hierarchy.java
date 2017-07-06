/*
 * NodeHierarchy.java
 *
 * Created on 17 de noviembre de 2006, 11:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package elvira;

/**
 *
 * @author smc
 */

import java.io.*;
import java.util.Vector;


public class Hierarchy {
    
    
/*
 * The type of node of the hierarchy: leaf or inner
 *
 **/
    
    

protected int type;
public static final int LEAF = 1;
public static final int INNER = 0;
    

   
 
/*
 * The FiniteStates node to which is makes reference
 *
 **/   
    

protected FiniteStates reference;





/*
 * The number of children (0 if it is a leaf)
 *
 **/
protected int nchild;



/*
 * The name of the node. It is not the same than the variable
 * name: it will correspond to a subset of values of the variable 
 * (all the cases of its descendants)
 *
 **/
    

protected String name;


/*
 * An array of boolean values saying the cases of the variable corresponding to this node
 *
 */

protected boolean members[];



/*
 ** A vector with its chidren (all of them of type NodeHierarchy)
 **
 */


protected Vector children;





    /** Creates a new instance of NodeHierarchy */
    public Hierarchy() {
    }
    
    
    /** Creates a new instance of NodeHierarchy */
    public Hierarchy(FiniteStates x) {
        
        int i;
        
        reference = x;
        type = LEAF;
        nchild = 0;
        name="Valuesof"+x.getName();
        children = new Vector();
        members = new boolean[x.getNumStates()];
        
        for(i=0;i<x.getNumStates(); i++){
            
            members[i] = true;
            
        }
        
        
    }
    
/***********************
 *
 * Add a child to a node with the given defnition
 *
 **********************/    
    
 public void addChild(boolean definition[]){
     
      Hierarchy y;
      
      
     
       type = INNER;
        nchild ++;
        
        y = new Hierarchy(reference);
        y.members = definition;
        children.addElement (y);
        
        
     
     
 }    
     
      
 public void addChild(Hierarchy y){
     
    
      
      
     
       type = INNER;
        nchild ++;
        
      
       
        children.addElement (y);
        
        
     
     
 }    
 
 public int getNumChildren(){
     
     return nchild;
     
 }
 
 
 public boolean isLeaf(){
     
     if (type==LEAF) {return true;}
     else {return false;}
     
     
 }
 
 
 public Hierarchy getChildat(int i){
     
     return (Hierarchy) children.elementAt(i);
     
     
 }
 
 
 
 public boolean[] getMembers(){
     
     return members;
     
     
 }
 
 
 public int getNumberElements(){
     
     int i,total;
     
     total=0;
     
     for (i=0; i<reference.getNumStates(); i++){
         if (members[i]) {total++;}
     }
     
     return(total);
     
     
 }
 
 
 public FiniteStates getVar(){
     
     return(reference);
     
 }
 
 public void setName(String s){
    

    if (s.equals("")){
	name = "noname";
    } else if (s.substring(0,1).equals("\"")) {
	name = new String(s.substring(1,s.length()-1));
    } else {
	name = new String(s);
    }
}
 }
 
    

