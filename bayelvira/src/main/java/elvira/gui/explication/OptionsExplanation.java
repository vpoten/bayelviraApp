package elvira.gui.explication;

import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import elvira.gui.InferencePanel;
import elvira.gui.NetworkFrame;
import elvira.*;

public class OptionsExplanation extends javax.swing.JDialog{
   
   NetworkFrame current;
       
   InferencePanel infpanel;
       
   CasesList caseslist;
   
   NodeList expSet;

	private ResourceBundle dialogBundle;
	
	public OptionsExplanation(NetworkFrame frame){	   	   

		switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs");
		                         break;
		   case Elvira.SPANISH: dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs_sp");
		                        break;
		}
		
      current=frame;
      infpanel=frame.getInferencePanel();
      if (infpanel!=null)
        caseslist=infpanel.getCasesList();
      else caseslist=null;
      		
		getContentPane().setLayout(null);
		setSize(426,380);
		
		expBorder= new javax.swing.border.TitledBorder(Elvira.localize(dialogBundle,"Explanation"));
		comBorder = new javax.swing.border.TitledBorder(Elvira.localize(dialogBundle,"Comparation"));
		varBorder = new javax.swing.border.TitledBorder(Elvira.localize(dialogBundle,"Variation"));
		
		setTitle(Elvira.localize(dialogBundle, "OptionsExpl"));
		
		explanPanel.setBorder(expBorder);
		explanPanel.setLayout(null);
		getContentPane().add(explanPanel);
		explanPanel.setBounds(12,12,396,60);
		autoButton.setText(Elvira.localize(dialogBundle, "Automatic"));
		expbgroup1.add(autoButton);
		explanPanel.add(autoButton);
		autoButton.setBounds(48,17,120,26);
		handButton.setText(Elvira.localize(dialogBundle,"Nonauto"));
	
		if (infpanel.AUTOEXPLANATION){ 
		    handButton.setSelected(false);		
		    autoButton.setSelected(true);
		}
		else {
		      handButton.setSelected(true);		
		      autoButton.setSelected(false);		
		}
		    
        expbgroup1.add(handButton);		
		explanPanel.add(handButton);
		handButton.setBounds(264,17,120,26);

		comparPanel.setBorder(comBorder);
		comparPanel.setLayout(null);
		getContentPane().add(comparPanel);
		comparPanel.setBounds(12,96,190,155);
		priorButton.setText(Elvira.localize(dialogBundle,"CasePrior"));
		if (infpanel.COMPARINGCASE==InferencePanel.CASOP)
		    priorButton.setSelected(true);
		    else priorButton.setSelected(false);
		combgroup2.add(priorButton);
		comparPanel.add(priorButton);
		priorButton.setBounds(24,24,132,26);
		befButton.setText(Elvira.localize(dialogBundle,"CaseBefore"));
        if (infpanel.COMPARINGCASE==InferencePanel.CASOANT)
		    befButton.setSelected(true);
		    else befButton.setSelected(false);
		combgroup2.add(befButton);
		comparPanel.add(befButton);
		befButton.setBounds(24,60,115,26);
		caseButton.setText(Elvira.localize(dialogBundle,"CaseNum"));
		if (current.mode==NetworkFrame.EDITOR_ACTIVE || current.mode==NetworkFrame.LEARNING_ACTIVE){
		    caseButton.setSelected(false);
		    caseText.setEnabled(false);
		    priorButton.setSelected(true);
		}
		else if (infpanel.COMPARINGCASE==InferencePanel.CASOK){
		    caseButton.setSelected(true);
		    caseText.setEnabled(true);
		}
		else {caseButton.setSelected(false);
		      caseText.setEnabled(false);
		}
		combgroup2.add(caseButton);
		comparPanel.add(caseButton);
		caseButton.setBounds(24,96,115,26);
		comparPanel.add(caseText);
		caseText.setBounds(140,96,39,28);
		if (caseButton.isSelected())
		    caseText.setText(String.valueOf(infpanel.getCasetoCompare()));
		errorText.setBounds(24,260,256,25);
		errorText.setEnabled(false);
		getContentPane().add(errorText);
		thresPanel.setBorder(varBorder);
		thresPanel.setLayout(null);
		getContentPane().add(thresPanel);
		thresPanel.setBounds(240,96,170,155);
		absButton.setText(Elvira.localize(dialogBundle,"Absolute"));
		absButton.setSelected(true);
		varbgroup3.add(absButton);
		thresPanel.add(absButton);
		absButton.setBounds(37,27,77,25);
		relButton.setText(Elvira.localize(dialogBundle,"Relative"));
		varbgroup3.add(relButton);
		thresPanel.add(relButton);
		relButton.setBounds(37,57,96,25);
		relButton.setEnabled(false);
		thresLabel.setText(Elvira.localize(dialogBundle,"Threshold"));
		thresPanel.add(thresLabel);
		thresLabel.setBounds(24,96,60,29);
		thresvalue.setText(String.valueOf(infpanel.getTheta()));
		thresvalue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		thresPanel.add(thresvalue);
		thresvalue.setBounds(96,96,56,29);
		expBorder.setTitleFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
	    expBorder.setTitleJustification(javax.swing.border.TitledBorder.CENTER);
		expBorder.setTitleColor(java.awt.Color.blue);
		comBorder.setTitleFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		comBorder.setTitleColor(java.awt.Color.blue);
		varBorder.setTitleFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
		varBorder.setTitleColor(java.awt.Color.blue);
		okButton.setText(Elvira.localize(dialogBundle,"OK.Label"));
		getContentPane().add(okButton);
		okButton.setBounds(96,300,100,36);
		cancelButton.setText(Elvira.localize(dialogBundle,"Cancel.Label"));
		getContentPane().add(cancelButton);
		cancelButton.setBounds(228,300,100,36);
		
      OptionsAction opsAction = new OptionsAction();		
      okButton.addActionListener(opsAction);
      cancelButton.addActionListener(opsAction);
      caseText.addActionListener(opsAction);
      thresvalue.addActionListener(opsAction);
      
      OptionsItem opsItem = new OptionsItem();
      autoButton.addItemListener(opsItem);
      handButton.addItemListener(opsItem);
      priorButton.addItemListener(opsItem);
      befButton.addItemListener(opsItem);
      caseButton.addItemListener(opsItem);
      absButton.addItemListener(opsItem);
      relButton.addItemListener(opsItem);
              
      setLocationRelativeTo(Elvira.getElviraFrame());	
		
	}
   
//   ResourceBundle dialogBundle = Elvira.getElviraFrame().getDialogBundle();
   
    ButtonGroup expbgroup1 = new ButtonGroup();
    ButtonGroup combgroup2 = new ButtonGroup();
    ButtonGroup varbgroup3 = new ButtonGroup();
	javax.swing.JPanel explanPanel = new javax.swing.JPanel();
	javax.swing.JRadioButton autoButton = new javax.swing.JRadioButton();
	javax.swing.JRadioButton handButton = new javax.swing.JRadioButton();
	javax.swing.JPanel comparPanel = new javax.swing.JPanel();
	javax.swing.JRadioButton priorButton = new javax.swing.JRadioButton();
	javax.swing.JRadioButton befButton = new javax.swing.JRadioButton();
	javax.swing.JRadioButton caseButton = new javax.swing.JRadioButton();
	javax.swing.JTextField caseText = new javax.swing.JTextField();
	javax.swing.JTextField errorText = new javax.swing.JTextField();
	javax.swing.JPanel thresPanel = new javax.swing.JPanel();
	javax.swing.JRadioButton absButton = new javax.swing.JRadioButton();
	javax.swing.JRadioButton relButton = new javax.swing.JRadioButton();
	javax.swing.JLabel thresLabel = new javax.swing.JLabel();
	javax.swing.JTextField thresvalue = new javax.swing.JTextField();
	javax.swing.border.TitledBorder expBorder;
	javax.swing.border.TitledBorder comBorder;
	javax.swing.border.TitledBorder varBorder;
	javax.swing.JButton okButton = new javax.swing.JButton();
	javax.swing.JButton cancelButton = new javax.swing.JButton();
	
	class OptionsAction implements ActionListener{
		public void actionPerformed(java.awt.event.ActionEvent event){
			Object object = event.getSource();
			
			if (object == okButton)
				okButton_actionPerformed(event);
            else if (object == cancelButton)
			     cancelButton_actionPerformed(event);						
 		    else if (object == caseText)
			     caseText_actionPerformed(event);
			else if (object == thresvalue)
				 thresvalue_actionPerformed(event);
		}
	}

  	
   void okButton_actionPerformed(java.awt.event.ActionEvent event){
   	  infpanel.repaint();
   	  dispose();
   }	    

   void cancelButton_actionPerformed(java.awt.event.ActionEvent event){
      dispose();			
   }	    

   void caseText_actionPerformed(java.awt.event.ActionEvent event){
        int casonum=Integer.parseInt(caseText.getText());
        if (caseslist==null){
            errorText.setText(Elvira.localize(dialogBundle, "NoOption"));
        }
        else if (caseslist.getNumStoredCases()-1<casonum){
            errorText.setText(Elvira.localize(dialogBundle, "UnExCase"));
        }
        else if (caseslist.getNumStoredCases()-1>=casonum){
                errorText.setText(Elvira.localize(dialogBundle, "ValidCase"));
                infpanel.setCasetoCompare(casonum);
        }
   }
   
   void thresvalue_actionPerformed(java.awt.event.ActionEvent event){
        infpanel.setTheta(Double.parseDouble(thresvalue.getText()));
   }	    

	class OptionsItem implements java.awt.event.ItemListener{
	   
	   public void itemStateChanged(java.awt.event.ItemEvent event){
		   Object object = event.getSource();
		   if (object == autoButton)
			   autoButton_itemStateChanged(event);
		   else if (object == handButton)
			   handButton_itemStateChanged(event);
		   else if (object == priorButton)
			   priorButton_itemStateChanged(event);
		   else if (object == befButton)
			   befButton_itemStateChanged(event);
		   else if (object == caseButton)
			   caseButton_itemStateChanged(event);
		   else if (object == absButton)
			   absButton_itemStateChanged(event);
		   else if (object == relButton)
			   relButton_itemStateChanged(event);
   				
	   }
	}


   void autoButton_itemStateChanged(java.awt.event.ItemEvent event){
      infpanel.AUTOEXPLANATION=true;     
      infpanel.repaint();
   }

   void handButton_itemStateChanged(java.awt.event.ItemEvent event){
      infpanel.AUTOEXPLANATION=false;        
   }

   void priorButton_itemStateChanged(java.awt.event.ItemEvent event){
    infpanel.COMPARINGCASE=InferencePanel.CASOP;
    caseText.setText("");
    errorText.setText("");
   }

   void befButton_itemStateChanged(java.awt.event.ItemEvent event){
    infpanel.COMPARINGCASE=InferencePanel.CASOANT;
    caseText.setText("");
    errorText.setText("");
   }

   void caseButton_itemStateChanged(java.awt.event.ItemEvent event){
      if (current.mode == NetworkFrame.EDITOR_ACTIVE || current.mode == NetworkFrame.LEARNING_ACTIVE )
        errorText.setText("Error. Esta opción no se puede seleccionar");
        else {//if (current.mode == INFERENCE_ACTIVE)
              infpanel.COMPARINGCASE=InferencePanel.CASOK;
              caseText.setEnabled(true);
        }
   }
   
   void absButton_itemStateChanged(java.awt.event.ItemEvent event){
   }

   void relButton_itemStateChanged(java.awt.event.ItemEvent event){
   }
   
	/**
	 * Using with the bundles. Find the string given as parameter in
	 * the bundle specified
	 */
	
/*   public String localize (ResourceBundle bundle, String name) {
      return Elvira.getElviraFrame().localize(bundle, name);
   }	

*/}//fin de la clase OptionsExplanation