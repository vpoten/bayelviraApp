/*
 * GenerateDBCPanel.java
 *
 * Created on 19 de mayo de 2002, 19:07
 */

package elvira.gui;
import javax.swing.*;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.*;

import elvira.*;
import elvira.Elvira;

/**
 *
 * @author  Administrador
 * @version 
 */
public class GenerateDBCPanel extends ElviraPanel {

    
    private int selectionMemory = 0;
    private int number_cases=0;
    private String name="";
    private Vector parameters;
    
    /**
    * Default constructor for LearningPanel object.
    */
  
   public GenerateDBCPanel() {            
      super();
      
      selectionMemory = 0;
      number_cases=0;
      name="";
      parameters = new Vector();
   }

    
    /** Creates new GenerateDBCPanel */
  
    /**
    * Return the index of the current learning method
    *
    * @return an int with the index of the learing method
    */
   
   public int getSelectionMemory() {
       
      return selectionMemory;
      
   }
   
   
   public int getNumberCases() {
       
      return number_cases;
      
   }
   
   public String getfileName() {
       
      return name;
      
   }
   
   public void setSelectionMemory (int i) {
      selectionMemory = i;
   }
   
   public void setNumberCases (int i) {
      number_cases = i;
   }
   
   public void setfileName (String f) {
      name = f;
   }
   
   
    /**
    * Return a vector that contains the parameters of the 
    * current learning method
    */
   
   public Vector getParameters() {
      return parameters;
   }
    
    
   public void setParameters (Vector v){
      parameters = v;
   }
}
