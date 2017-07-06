/*
 * GenerateDBC.java
 *
 * Created on 19 de mayo de 2002, 17:32
 */

package elvira.gui;
import java.util.Vector;
import java.awt.*;
import javax.swing.*;
import elvira.database.*;

import elvira.Elvira;
/**
 *
 * @author  Administrador
 * @version 
 */
public class GenerateDBC extends MethodDialog {

    /** Creates new GenerateDBC */
    public GenerateDBC(Frame parent) {
        super(parent);

    
        setResizable(false);
        setModal(true);
	setTitle("Generate DBC");
	getContentPane().setLayout(null);
	setSize(300,280);
	setVisible(false);
        this.setLocationRelativeTo(Elvira.getElviraFrame());
        
        
        
        okButton.setText("OK");
        okButton.setActionCommand("OK");
	okButton.setMnemonic((int)'O');
	getContentPane().add(okButton);
	okButton.setBounds(40,210,80,33);
	cancelButton.setText("Cancel");
	cancelButton.setActionCommand("OK");
	cancelButton.setMnemonic((int)'C');
	getContentPane().add(cancelButton);
	cancelButton.setBounds(168,210,80,33);
        getContentPane().add(parTextField1);
	parTextField1.setBounds(170,40,48,28);
        parTextField1.setText("10");
	JLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
	JLabel1.setText("Número de Casos");
	getContentPane().add(JLabel1);
	
        JLabel1.setBounds(0,40,156,28);
        
        JLabel2.setBounds(45,130,156,28);
        JLabel2.setText("Nombre Fichero DBC");
        getContentPane().add(JLabel2);
        
        getContentPane().add(parTextField2);
	parTextField2.setBounds(170,130,65,28);
        parTextField2.setFont(new java.awt.Font("Times New Roman", 1, 14));                
        
        
        getContentPane().add(typeComboBox);
	typeComboBox.setBounds(170,85,48,28);
        typeComboBox.addItem("Sí");
	typeComboBox.addItem("No");
        typeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
	typeLabel.setText("Activar Memoria");
	typeLabel.setLabelFor(typeComboBox);
	getContentPane().add(typeLabel);
	typeLabel.setBounds(20,85,132,28);
	
	
        
        
        //{{REGISTER_LISTENERS
        SymAction lSymAction = new SymAction();
	okButton.addActionListener(lSymAction);
	cancelButton.addActionListener(lSymAction);
	//}}
    }
    
   public GenerateDBC()
	{
		this((Frame)null);
	}
   	
	
        
	public GenerateDBC(String sTitle)
	{
		this();
		setTitle(sTitle);
        }
    
    public GenerateDBC (GenerateDBCPanel gd) {
	   this();
	    generateDBCpanel = gd;
	   
           int mem = generateDBCpanel.getSelectionMemory();
           typeComboBox.setSelectedIndex(mem);
           
           
	   fillTextFields(generateDBCpanel.getParameters(),null);
          
	}	
    
    
    
    public void fillTextFields (Vector parameters, Vector filesNames) {
      Integer number;
      String fich;
      
      if (parameters == null)
         return;
      
      int size = parameters.size();
      
      
      
      
      if (size>0) {         
         fich = (String) parameters.elementAt(0);
         parTextField2.setText(fich.toString());
      }
                  
   }
   
   
   public Vector getParameters() {
    
      Vector parameters = new Vector(); 
      String error="";
     
     
      getStringValue(parTextField2,parameters,error); 
      return parameters;      
     
   }

    /**
    * @param args the command line arguments
    */
       
    public static void main (String args[]) {
        (new GenerateDBC()).setVisible(true);
    }
    
    
    public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height);
	}

        
        class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == okButton)
				okButton_actionPerformed(event);
			else if (object == cancelButton)
				cancelButton_actionPerformed(event);
			
                                               
		}
	}
        
        
        void okButton_actionPerformed(java.awt.event.ActionEvent event)
	{
            
         int index = typeComboBox.getSelectedIndex();	
         generateDBCpanel.setSelectionMemory(index); 
         String s=parTextField1.getText();
         
         String fileName=parTextField2.getText();
         Double aux=new Double(s);
         int num_casos=aux.intValue();
   
         generateDBCpanel.setNumberCases(num_casos);
         generateDBCpanel.setfileName(fileName);
	 
         generateDBCpanel.setParameters(getParameters());
         
         
         NetworkFrame n = (NetworkFrame)Elvira.getElviraFrame().
                       getCurrentNetworkFrame();
         n.activeGenerateDBCPanel();
         
       
         dispose();			 
	}
    
	void cancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		dispose();			 
	}
        
        
        
        // Used by addNotify
	boolean frameSizeAdjusted = false;
        
        javax.swing.JComboBox typeComboBox = new javax.swing.JComboBox();
        javax.swing.JButton okButton = new javax.swing.JButton();
	javax.swing.JButton cancelButton = new javax.swing.JButton();
        javax.swing.JTextField parTextField1 = new javax.swing.JTextField();
        javax.swing.JTextField parTextField2 = new javax.swing.JTextField();
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
        
        javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
        
        javax.swing.JLabel typeLabel = new javax.swing.JLabel();
        
        GenerateDBCPanel generateDBCpanel;


}