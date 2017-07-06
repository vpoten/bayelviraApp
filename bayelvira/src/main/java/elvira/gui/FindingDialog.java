package elvira.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import elvira.*;
import java.util.*;
import java.util.ResourceBundle;

public class FindingDialog extends javax.swing.JDialog{

private Bnet bnet;
private Node node;
private NetworkFrame frame;
private ResourceBundle dialogBundle;

public FindingDialog(NetworkFrame parent, Bnet b, Node n){
 
    	switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs");            		                         
		                         break;
		   case Elvira.SPANISH: dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs_sp");   		                        
		                        break;		                         
		}				
        bnet=b;
        node=n;
        frame=parent;
        setModal(true);
        setTitle(Elvira.localize(dialogBundle,"SetEvidence"));
       	setSize(380,215);
        getContentPane().setLayout(null);   		
	    getContentPane().add(NameOfNode);
	    NameOfNode.setBounds(118,24,204,30);
	    setupNameOfNodeComboBox();
    	NameOfNode.setSelectedItem(node.getNodeString(true));

	values.setToolTipText(Elvira.localize(dialogBundle,"valuesTip"));
	getContentPane().add(values);
	values.setBounds(118,72,204,30);
	setupValuesComboBox(n.getNodeString(true));	
	
	okButton.setText(Elvira.localize(dialogBundle,"OK.label"));
	okButton.setMnemonic((int)'O');
	getContentPane().add(okButton);
	okButton.setBounds(34,136,133,30);
	cancelButton.setText(Elvira.localize(dialogBundle,"Cancel.label"));
	cancelButton.setMnemonic((int)'C');
	getContentPane().add(cancelButton);
	cancelButton.setBounds(190,136,133,30);
	nodeName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
	nodeName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
	nodeName.setText(Elvira.localize(dialogBundle,"EditVariable.Name.label"));
	getContentPane().add(nodeName);
	nodeName.setFont(new Font("Dialog", Font.BOLD, 14));
	nodeName.setBounds(14,24,65,30);
	stateName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
	stateName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
	stateName.setText(Elvira.localize(dialogBundle,"EditVariable.State.label"));
	getContentPane().add(stateName);
	stateName.setFont(new Font("Dialog", Font.BOLD, 14));
	stateName.setBounds(14,72,65,30);
	
	FindingAction findingaction = new FindingAction();
	NameOfNode.addActionListener(findingaction);
	okButton.addActionListener(findingaction);
	cancelButton.addActionListener(findingaction);
}

	//{{DECLARE_CONTROLS
	javax.swing.DefaultComboBoxModel modelcombo=new DefaultComboBoxModel();
	javax.swing.JComboBox values = new javax.swing.JComboBox(modelcombo);
	javax.swing.JComboBox NameOfNode = new javax.swing.JComboBox();
	javax.swing.JButton okButton = new javax.swing.JButton();
	javax.swing.JButton cancelButton = new javax.swing.JButton();
	javax.swing.JLabel nodeName = new javax.swing.JLabel();
	javax.swing.JLabel stateName = new javax.swing.JLabel();
	//}}
	
	void setupNameOfNodeComboBox(){
	    for (int posn=0; posn<bnet.getNodeList().size(); posn++){
        	 Node n=(Node)bnet.getNodeList().elementAt(posn);
	         NameOfNode.addItem(n.getNodeString(true));
            }

    }

	void setupValuesComboBox(String nodename){
		if (modelcombo.getSize()>0) modelcombo.removeAllElements();
	        FiniteStates fs = (FiniteStates) bnet.getNodeList().getNodeString(nodename,true);
        	Vector states=fs.getStates();
	        for (int s=0; s<states.size(); s++)
        	     values.addItem(states.elementAt(s));
        	values.addItem("");
        	Evidence current=frame.getInferencePanel().getCasesList().getCurrentCase().getEvidence();
        	int state=current.getValue(fs);
        	if (state!=-1)
        		values.setSelectedIndex(state);
        		else values.setSelectedItem("");
    }
	
	
	class FindingAction implements ActionListener{
		public void actionPerformed(java.awt.event.ActionEvent event){
			Object object = event.getSource();
			if (object == NameOfNode)
			    nameofnode_actionPerformed(event);
		   	else if (object == cancelButton)
		            cancelButton_actionPerformed (event);
			else if (object == okButton)
		            okButton_actionPerformed (event);
		}
		
        void nameofnode_actionPerformed(ActionEvent event){
	     String nodeName = (String) NameOfNode.getSelectedItem();	

             setupValuesComboBox(nodeName);
        }

	void cancelButton_actionPerformed(ActionEvent event){
	   dispose();
	}

	void okButton_actionPerformed(ActionEvent event){
		Node n=bnet.getNodeList().getNodeString((String) NameOfNode.getSelectedItem(),true);
        if ((frame.getInferencePanel().getCasesList().getNumCurrentCase()==0) && 
           (frame.getInferencePanel().getCasesList().getNumActiveCase()==1) && 
           (!frame.getInferencePanel().getCasesList().getActiveCase().getPropagated()))
		    if (values.getSelectedItem().equals("")){
		        System.out.println("El nodo no está observado");
		    }
		    else {
		          frame.getInferencePanel().getCasesList().setCurrentCase(1);
		          frame.getInferencePanel().propagate((FiniteStates)n, values.getSelectedIndex());
		    }
		else if (values.getSelectedItem().equals("")){         
		        if (frame.getInferencePanel().getCasesList().getCurrentCase().getIsObserved(n)){		    
		            frame.getInferencePanel().getCasesList().getCurrentCase().unsetAsFinding(n);
                    frame.getInferencePanel().propagate ((FiniteStates)n, -1);
                    }
                else {System.out.println("El nodo no está observado");}
             }
             else frame.getInferencePanel().propagate((FiniteStates)n, values.getSelectedIndex());
        dispose();
	}
		
	}//fin de la clase FindingAction
}
