package elvira.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.ResourceBundle;

import elvira.Elvira;
import elvira.gui.explication.*;
import elvira.*;

public class OptionsInference extends javax.swing.JDialog{
   
   NetworkFrame current;
       
   InferencePanel infpanel;
       
   CasesList caseslist;
   
   NodeList expSet;

   public OptionsInference(NetworkFrame frame){	   	   
		
      current=frame;
      infpanel=frame.getInferencePanel();
      if (infpanel!=null)
        caseslist=infpanel.getCasesList();
      else caseslist=null;
      		
      getContentPane().setLayout(null);
      setTitle(localize(dialogBundle,"OpInferenceDialog.Title"));
      setSize(439,370);
      setVisible(true);
      setModal(true);

      storedCases.setEditable(true);      
      storedCases.setBounds(252,20,72,24);		
      getContentPane().add(storedCases);
      for (int i=0; i<8; i++)
	      storedCases.addItem(String.valueOf(i));
	  if (caseslist!=null)
	      storedCases.setSelectedIndex(caseslist.getMaxNumStoredCases()-1);
	  else storedCases.setSelectedIndex(0);
	      
      storedLabel.setText(localize(dialogBundle,"OpInferenceDialog.MaxStored"));
      storedLabel.setBounds(12,16,228,31); 		
      storedLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
      getContentPane().add(storedLabel);
      		
      panel1.setLayout(null);
      panel1.setBorder(titledBorder1);		
      panel1.setBounds(12,54,120,132);
      getContentPane().add(panel1);

      autoRButton.setText(localize(dialogBundle,"OpInferenceDialog.Automatic"));
      autoRButton.setBounds(12,42,90,24);		
      bgroup1.add(autoRButton);
      panel1.add(autoRButton);

      nonautoRButton.setText(localize(dialogBundle,"OpInferenceDialog.OnDemand"));
      nonautoRButton.setBounds(12,78,90,24);
      bgroup1.add(nonautoRButton);
      panel1.add(nonautoRButton);
      	
      if (infpanel.AUTOPROPAGATION){
         autoRButton.setSelected(true);
         nonautoRButton.setSelected(false);
      }
      else {
         autoRButton.setSelected(false);
         nonautoRButton.setSelected(true);
      }
      
      panel2.setLayout(null);
      panel2.setBounds(150,54,265,132);
      panel2.setBorder(titledBorder2);
               getContentPane().add(panel2);		
              
    
      postprobRButton.setText(localize(dialogBundle,"OpInferenceDialog.Posteriori"));
     
      bgroup2.add(postprobRButton);
      panel2.add(postprobRButton);
      postprobRButton.setBounds(12,24,180,24);
      mpeRButton.setText(localize(dialogBundle,"OpInferenceDialog.MostProbable"));
      
      mpeRButton.setBounds(12,60,204,24);		
      bgroup2.add(mpeRButton);
      panel2.add(mpeRButton);
      kmpeRButton.setText(localize(dialogBundle,"OpInferenceDialog.KMostProbable"));
      kmpeRButton.setBounds(12,96,204,24);
      
      bgroup2.add(kmpeRButton);
      panel2.add(kmpeRButton);

      if (infpanel.INFERENCEAIM==InferencePanel.POSTERIORI){
          postprobRButton.setSelected(true);
          mpeRButton.setSelected(false);
          kmpeRButton.setSelected(false);
          partabdRButton.setEnabled(false);		
          totabdRButton.setEnabled(false);		
          
      }
      else {
            partabdRButton.setEnabled(true);		
            totabdRButton.setEnabled(true);		
            if (infpanel.INFERENCEAIM==InferencePanel.MOSTPROBEXPL){
               postprobRButton.setSelected(false);
               mpeRButton.setSelected(true);
               kmpeRButton.setSelected(false);
            }
            else {
                  postprobRButton.setSelected(false);
                  mpeRButton.setSelected(false);
                  kmpeRButton.setSelected(true);
            }
      }
      		
      kmpenum.setEditable(true);
      String s = new String();
      s = "" + infpanel.getNumExplanations();
      kmpenum.setText(s);
      kmpenum.setBounds(220,96,34,24);
      kmpenum.setVisible(true);
      if (infpanel.INFERENCEAIM==InferencePanel.KMOSTPROBEXPL)
        kmpenum.setEnabled(true);
        else kmpenum.setEnabled(false);
      panel2.add(kmpenum);		

      panel3.setLayout(null);
      panel3.setBorder(titledBorder3);		
      panel3.setBounds(60,194,320,90);
      getContentPane().add(panel3);

      totabdRButton.setText(localize(dialogBundle,"OpInferenceDialog.Total"));
      
      totabdRButton.setBounds(12,24,90,24);		
      bgroup3.add(totabdRButton);
      panel3.add(totabdRButton);

      partabdRButton.setText(localize(dialogBundle,"OpInferenceDialog.Partial"));
      partabdRButton.setBounds(12,50,90,24);
      
      bgroup3.add(partabdRButton);
      panel3.add(partabdRButton);

      selectButton.setText(localize(dialogBundle,"OpInferenceDialog.Variables"));
      selectButton.setEnabled(false);
      panel3.add(selectButton);
      selectButton.setBounds(110,50,180,28);
      		
      if (infpanel.TOTALABDUCTION)
          totabdRButton.setSelected(true);
          else {partabdRButton.setSelected(true);
                selectButton.setEnabled(true);
          }
      		
      okButton.setText(localize(dialogBundle,"OK.label"));
      okButton.setActionCommand("OK");
      getContentPane().add(okButton);
      okButton.setBounds(110,306,100,34);
      cancelButton.setText(localize(dialogBundle,"Cancel.label"));
      cancelButton.setActionCommand("Cancel");
      getContentPane().add(cancelButton);
      cancelButton.setBounds(240,306,100,34);
      		
      OptionsAction opsAction = new OptionsAction();		
      selectButton.addActionListener(opsAction);        
      okButton.addActionListener(opsAction);
      cancelButton.addActionListener(opsAction);
      		
      OptionsItem opsItem = new OptionsItem();
      autoRButton.addItemListener(opsItem);
      nonautoRButton.addItemListener(opsItem);
      postprobRButton.addItemListener(opsItem);
      mpeRButton.addItemListener(opsItem);
      kmpeRButton.addItemListener(opsItem);
      totabdRButton.addItemListener(opsItem);
      partabdRButton.addItemListener(opsItem);
              
      setLocationRelativeTo(Elvira.getElviraFrame());	
   }
   
   ResourceBundle dialogBundle = Elvira.getElviraFrame().getDialogBundle();
   
   javax.swing.JLabel storedLabel = new javax.swing.JLabel();
   javax.swing.JComboBox storedCases = new javax.swing.JComboBox();
   javax.swing.JLabel shownLabel = new javax.swing.JLabel();
   javax.swing.JComboBox shownCases = new javax.swing.JComboBox();
   javax.swing.border.TitledBorder titledBorder1 = 
         new javax.swing.border.TitledBorder(
               localize(dialogBundle,"OpInferenceDialog.Propagation"));
   javax.swing.border.TitledBorder titledBorder2 = 
         new javax.swing.border.TitledBorder(
               localize(dialogBundle,"OpInferenceDialog.Objetive"));
   javax.swing.border.TitledBorder titledBorder3 = 
         new javax.swing.border.TitledBorder(
               localize(dialogBundle,"OpInferenceDialog.Abduction"));
   ButtonGroup bgroup1 = new ButtonGroup();
   javax.swing.JRadioButton autoRButton = new javax.swing.JRadioButton();
   javax.swing.JRadioButton nonautoRButton = new javax.swing.JRadioButton();
   ButtonGroup bgroup2=new ButtonGroup();
   javax.swing.JRadioButton postprobRButton = new javax.swing.JRadioButton();
   javax.swing.JRadioButton mpeRButton = new javax.swing.JRadioButton();
   javax.swing.JRadioButton kmpeRButton = new javax.swing.JRadioButton();
   javax.swing.JTextArea kmpenum= new javax.swing.JTextArea();
   ButtonGroup bgroup3=new ButtonGroup();
   javax.swing.JRadioButton totabdRButton = new javax.swing.JRadioButton();
   javax.swing.JRadioButton partabdRButton = new javax.swing.JRadioButton();
   javax.swing.JButton selectButton = new javax.swing.JButton();

   javax.swing.JPanel panel1=new javax.swing.JPanel();
   javax.swing.JPanel panel2=new javax.swing.JPanel();	
   javax.swing.JPanel panel3=new javax.swing.JPanel();	
   javax.swing.JButton okButton = new javax.swing.JButton();
   javax.swing.JButton cancelButton = new javax.swing.JButton();
	
	class OptionsAction implements ActionListener{
		public void actionPerformed(java.awt.event.ActionEvent event){
			Object object = event.getSource();
			
			if (object == selectButton)
				selectButton_actionPerformed(event);
			else if (object == okButton)
				okButton_actionPerformed(event);
         else if (object == cancelButton)
				   cancelButton_actionPerformed(event);						
		}
	}

   void selectButton_actionPerformed(java.awt.event.ActionEvent event){
	   SelectionEditor caseed=new SelectionEditor((NetworkFrame) 
                    Elvira.getElviraFrame().getCurrentNetworkFrame());
	   caseed.show();
   }	    
   	
   void okButton_actionPerformed(java.awt.event.ActionEvent event){
      if (caseslist==null){
          caseslist=new CasesList();
          infpanel.setCasesList(caseslist);
      }
      caseslist.setMaxNumStoredCases(Integer.parseInt((String) storedCases.getSelectedItem()));	    
      //caseslist.setMaxNumShownCases(Integer.parseInt((String) shownCases.getSelectedItem()));     
      if (current.mode!=NetworkFrame.LEARNING_ACTIVE && autoRButton.isSelected()){
         Bnet b=current.getEditorPanel().getBayesNet(); 
         InferencePanel networkInferencePanel=current.getInferencePanel();
         if (networkInferencePanel.TOTALABDUCTION)
             networkInferencePanel.setParameters(null);
         if (mpeRButton.isSelected())
            networkInferencePanel.setNumExplanations(1);
            else if (kmpeRButton.isSelected())
                    networkInferencePanel.setNumExplanations(
                              Integer.parseInt(kmpenum.getText()));

         networkInferencePanel.setExplanationSet(expSet);
         if (!b.getIsCompiled()){
             b.compile(networkInferencePanel.getInferenceMethod(),
	                   networkInferencePanel.getParameters(),
                       networkInferencePanel.getAuxiliaryFilesNames(),
                       networkInferencePanel.getAbductiveValues());
             b.setCompiled(true);
             networkInferencePanel.setCasesList(new CasesList(b));
             Elvira.getElviraFrame().setNodeName(networkInferencePanel.getCasesList().getCurrentCase().getIdentifier());	          
   	         Elvira.getElviraFrame().setColorNodeName(networkInferencePanel.getCasesList().getCurrentCase().getColor());	          

        }
        else if (!current.getInferencePanel().propagate(current.getInferencePanel().getCasesList().getCurrentCase()))
              ShowMessages.showMessageDialog(ShowMessages.IMPOSIBLE_EVIDENCE, JOptionPane.ERROR_MESSAGE);
        networkInferencePanel.repaint();
        Elvira.getElviraFrame().enableExplanationOptions(true);
      }
      else{//In order to store data for abduction when autopropagation==false
        InferencePanel networkInferencePanel=current.getInferencePanel();
        networkInferencePanel.setExplanationSet(expSet);
        networkInferencePanel.setNumExplanations(
                             Integer.parseInt(kmpenum.getText())); 
      }
      
      dispose();
   }	    

   void cancelButton_actionPerformed(java.awt.event.ActionEvent event){
      dispose();			
   }	    

	class OptionsItem implements java.awt.event.ItemListener{
	   
	   public void itemStateChanged(java.awt.event.ItemEvent event){
		   Object object = event.getSource();
		   if (object == storedCases)
			   storedComboBox_itemStateChanged(event);
		   else if (object == shownCases)
			   shownComboBox_itemStateChanged(event);
		   else if (object == autoRButton)
			   autoRButton_itemStateChanged(event);
		   else if (object == nonautoRButton)
			   nonautoRButton_itemStateChanged(event);
		   else if (object == postprobRButton)
			   postprobRButton_itemStateChanged(event);
		   else if (object == mpeRButton)
			   mpeRButton_itemStateChanged(event);
		   else if (object == kmpeRButton)
			   kmpeRButton_itemStateChanged(event);
		   else if (object == totabdRButton)
			   totabdRButton_itemStateChanged(event);
		   else if (object == partabdRButton)
			   partabdRButton_itemStateChanged(event);
   				
	   }
	}


   void storedComboBox_itemStateChanged(java.awt.event.ItemEvent event){
   }

   void shownComboBox_itemStateChanged(java.awt.event.ItemEvent event){
   }

   void autoRButton_itemStateChanged(java.awt.event.ItemEvent event){
      infpanel.AUTOPROPAGATION=true;        
      Elvira.getElviraFrame().enablePropagation(false);
   }

   void nonautoRButton_itemStateChanged(java.awt.event.ItemEvent event){
      infpanel.AUTOPROPAGATION=false;        
      Elvira.getElviraFrame().enablePropagation(true);        
   }

   void postprobRButton_itemStateChanged(java.awt.event.ItemEvent event){
      infpanel.INFERENCEAIM=InferencePanel.POSTERIORI;
      infpanel.setInferenceMethod(0);
      kmpenum.setEnabled(false);
      partabdRButton.setEnabled(false);		
      totabdRButton.setEnabled(false);		
   }

   void mpeRButton_itemStateChanged(java.awt.event.ItemEvent event){
      infpanel.INFERENCEAIM=InferencePanel.MOSTPROBEXPL;
      infpanel.setInferenceMethod(12);
      kmpenum.setEnabled(false);
      partabdRButton.setEnabled(true);		
      totabdRButton.setEnabled(true);		
   }

   void kmpeRButton_itemStateChanged(java.awt.event.ItemEvent event){
      infpanel.INFERENCEAIM=InferencePanel.KMOSTPROBEXPL;
      infpanel.setInferenceMethod(12);
      kmpenum.setEnabled(true);
      partabdRButton.setEnabled(true);		
      totabdRButton.setEnabled(true);
   }

   void totabdRButton_itemStateChanged(java.awt.event.ItemEvent event){
      infpanel.TOTALABDUCTION=true;        
      selectButton.setEnabled(false);
   }

   void partabdRButton_itemStateChanged(java.awt.event.ItemEvent event){
      infpanel.TOTALABDUCTION=false;        
      selectButton.setEnabled(true);
   }
   
	/**
	 * Using with the bundles. Find the string given as parameter in
	 * the bundle specified
	 */
	
   public String localize (ResourceBundle bundle, String name) {
      return ElviraFrame.localize(bundle, name);
   }	
   




class SelectionEditor extends javax.swing.JDialog{
    
    private InferencePanel infpanel;

    private Bnet bnet;
    
	private boolean frameSizeAdjusted = false;

    private Object[] emptyRow = {""};	
    private String[] columnNames= {"Variable"};
    private Object[][] data;
    
    private ResourceBundle menuBundle;

	private JToolBar explanationToolbar = new JToolBar();
	
	javax.swing.JScrollPane caseScrollPane = new javax.swing.JScrollPane();
	javax.swing.JButton newFindingButton = new javax.swing.JButton();
	javax.swing.JButton OkButton= new javax.swing.JButton();	
	javax.swing.JButton CancelButton= new javax.swing.JButton();	
    javax.swing.JComboBox variablesComboBox = new javax.swing.JComboBox();        	
    

    private EditorTableModel model;
    private javax.swing.JTable caseEditorTable;
	
	public SelectionEditor(NetworkFrame frame){
	
		switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: menuBundle = ResourceBundle.getBundle ("elvira/localize/Menus");            
			                         break;
		   case Elvira.SPANISH: menuBundle = ResourceBundle.getBundle ("elvira/localize/Menus_sp");   
			                        break;		                         
		}				
		
  		setModal(true);
		setResizable(false);		

   
        //panel sobre el que se hace la inferencia y desde el que se ejecuta el CaseEditor
        infpanel=frame.getInferencePanel();
        //red sobre la que se está haciendo inferencia
        bnet=infpanel.getBayesNet();

        model= new EditorTableModel(columnNames,0);
        caseEditorTable = new javax.swing.JTable(model);

		setModal(true);
		setTitle(Elvira.localize(menuBundle, "SelectVariables"));
		getContentPane().setLayout(null);
		setSize(420,220);
	    setVisible(false);
     	caseScrollPane.setOpaque(true);
		getContentPane().add(caseScrollPane);
		caseScrollPane.setBounds(36,10,212,156);
		caseScrollPane.getViewport().add(caseEditorTable);
		caseEditorTable.setBounds(0,0,189,153);
        
		newFindingButton.setText(Elvira.localize(menuBundle, "NewVar"));
		newFindingButton.setActionCommand(Elvira.localize(menuBundle,"NewVar"));
		getContentPane().add(newFindingButton);
		newFindingButton.setBounds(264,14,140,40);
		OkButton.setText(Elvira.localize(menuBundle, "OK.label"));
		OkButton.setBounds(40, 180, 100, 30);
		getContentPane().add(OkButton);		
		CancelButton.setText(Elvira.localize(menuBundle, "Cancel.label"));
		CancelButton.setBounds(150, 180, 100, 30);
	    getContentPane().add(CancelButton);
        
        EditorAction edAction = new EditorAction();
		newFindingButton.addActionListener(edAction);
        variablesComboBox.addActionListener(edAction);
		OkButton.addActionListener (edAction);
		CancelButton.addActionListener (edAction);
       
        enableDialog (true);

		caseEditorTable.setRowSelectionAllowed(false);
		model.setMaxRows(bnet.getNodeList().size());		        

		setUpVariablesColumn(caseEditorTable.getColumnModel().getColumn(0));

        caseEditorTable.getModel().addTableModelListener(
            new TableModelListener() {

                //este método solo se ejecuta cuando se modifica alguna celda de la tabla
           	    public void tableChanged (TableModelEvent e) {
           	        int fila=e.getFirstRow();
           	        int columna=e.getColumn();
           	        if (columna==0){
           	            String nodename=(String) model.getValueAt(fila, 0);
           	            test_variables(fila, nodename);
           	        }
                }
            });
    			
	setLocationRelativeTo(Elvira.getElviraFrame());
        
	}//fin del constructor


	public SelectionEditor(String sTitle, NetworkFrame frame){
		this(frame);
		setTitle(sTitle);
	}

	public void setVisible(boolean b){
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
	}

	public void enableDialog (boolean isEditable) {
	   newFindingButton.setEnabled(isEditable);
	}

	public void addNotify(){
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();
		super.addNotify();
		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;
		// Adjust size of frame according to the insets and menu bar
		Insets insets = getInsets();
		javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
		int menuBarHeight = 0;
		if (menuBar != null)
			menuBarHeight = menuBar.getPreferredSize().height;
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
	}

	public void setUpVariablesColumn (TableColumn variableColumn) {
        //Set up the editor for the sport cells.

        variablesComboBox.addItem(" ");
        for (int posn=0; posn<bnet.getNodeList().size(); posn++){
            Node n=(Node)bnet.getNodeList().elementAt(posn);
            variablesComboBox.addItem(n.getNodeString(true));
        }
        variablesComboBox.setSelectedIndex(-1);
        variableColumn.setCellEditor(new DefaultCellEditor(variablesComboBox));
        
        //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer =new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for combo box");
        variableColumn.setCellRenderer(renderer);
        
        //Set up tool tip for the sport column header.
        TableCellRenderer headerRenderer = variableColumn.getHeaderRenderer();
        if (headerRenderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer)headerRenderer).setToolTipText(
                     "Click the variable to see a list of choices");
        } 
        
 	}

    class EditorAction implements java.awt.event.ActionListener{

		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == newFindingButton)
				newFindingButton_actionPerformed(event);
	        else if (object == OkButton)
 		             okButton_actionPerformed(event);	
	        else if (object == CancelButton)
 		             cancelButton_actionPerformed(event);	
	        else if (object == variablesComboBox)
 		             variablesComboBox_itemStateChanged(event);	
 		}
	}


	void cancelButton_actionPerformed(ActionEvent event){
	   dispose();
	}

	void okButton_actionPerformed(ActionEvent event){
        expSet=new NodeList();
        
        for(int f=0; f<caseEditorTable.getRowCount(); f++)
            //Prevenimos que haya filas en las que no se haya introducido ningún hallazgo
            if (!((String)caseEditorTable.getValueAt(f,0)).equals(" ")){
    	        Node n=bnet.getNodeList().getNodeString((String) caseEditorTable.getValueAt(f,0),true);
    	        expSet.insertNode(n);
    	    }
        InferencePanel networkInferencePanel=current.getInferencePanel();
        networkInferencePanel.setExplanationSet(expSet);
        expSet.printNames();
        dispose();
	}

	void newFindingButton_actionPerformed(java.awt.event.ActionEvent event){
         model.addRow(emptyRow);
	}

    private void test_variables(int fila, String nodeName){
        if (!nodeName.equals(" ")){
          	      int f=0; boolean found=false;
	     	      while (f<caseEditorTable.getRowCount() && !found){
                    //Miramos a ver si esa variable forma parte ya de la evidencia
                    if (f!=fila)
                       if (((String)caseEditorTable.getValueAt(f,0)).equals(nodeName)){
                            found=true;                            
                            ShowMessages.showMessageDialog(ShowMessages.DUPLICATED_FINDING, JOptionPane.ERROR_MESSAGE);
                        }
                        else f++;
		            else f++;
		        }
		}
		else ShowMessages.showMessageDialog(ShowMessages.DELETING_FINDING, JOptionPane.ERROR_MESSAGE);
	}
	
	void variablesComboBox_itemStateChanged(java.awt.event.ActionEvent event){
        String nodeName=(String) variablesComboBox.getSelectedItem();
        System.out.println(nodeName);
    }
		

public class EditorTableModel extends DefaultTableModel{
        int maxrows=0;
        
        EditorTableModel(Object[] columnNames,int numrows) {
         super (columnNames,numrows);
      }
         
         void removeValuesTable(){
            int r=0;
            while (r<getRowCount())
                removeRow(r);                        
        }
         

        int getMaxRows(){
            return maxrows;
        }
        
        void setMaxRows(int m){
            maxrows=m;
        }
        /*
         * Don't need to implement this method unless your table's
         * editable.
         */

        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 0)
                return false;
            else 
                return true;
        }
        
}//fin de la clase EditorTableModel

}//fin de la clase SelectionEditor	

}//fin de la clase OptionsInference