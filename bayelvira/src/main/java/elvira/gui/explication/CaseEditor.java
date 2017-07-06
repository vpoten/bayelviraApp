/* CaseEditor.java */

package elvira.gui.explication;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import elvira.gui.ElviraFrame;
import elvira.gui.InferencePanel;
import elvira.gui.NetworkFrame;
import elvira.gui.ShowMessages;
import elvira.*;
import java.util.*;
import elvira.Elvira;
import java.util.ResourceBundle;


public class CaseEditor extends javax.swing.JDialog{
    
    private InferencePanel infpanel;

    private Case cedit;
    
    private Evidence evidence;
    
    private CasesList casesl;
    
    private Bnet bnet;
    
    private Finding f;

	private boolean frameSizeAdjusted = false;
	
   
    private Object[] emptyRow = {"", ""};	
    private String[] columnNames;
    private Object[][] data;
    
    
    private ResourceBundle menuBundle;

	private JToolBar explanationToolbar = new JToolBar();
	
	private static String imagesPath = "elvira/gui/images/";
	private javax.swing.ImageIcon firstIcon = new javax.swing.ImageIcon(imagesPath+"first.gif"),
	                              previousIcon = new javax.swing.ImageIcon (imagesPath+"previous.gif"),
	                              nextIcon = new javax.swing.ImageIcon (imagesPath+"next.gif"),
	                              lastIcon = new javax.swing.ImageIcon (imagesPath+"last.gif");
	private JButton firstButton = new JButton(),
	                previousButton = new JButton(),
	                nextButton = new JButton(),
	                lastButton = new JButton();
	                              
    private JTextField nodeName = new JTextField(15);	                             
    
	javax.swing.JLabel commentLabel = new javax.swing.JLabel();
	javax.swing.JTextField commentTextField = new javax.swing.JTextField();
	javax.swing.JScrollPane caseScrollPane = new javax.swing.JScrollPane();
	javax.swing.JButton newFindingButton = new javax.swing.JButton();
	javax.swing.JButton deleteEvidenceButton = new javax.swing.JButton();
	javax.swing.JButton propagateButton = new javax.swing.JButton();	
	javax.swing.JButton storecaseButton = new javax.swing.JButton();	
	javax.swing.JButton okButton= new javax.swing.JButton();	
	javax.swing.JButton cancelButton= new javax.swing.JButton();	
	
	private DefaultComboBoxModel combovarmodel=new DefaultComboBoxModel();
    javax.swing.JComboBox variablesComboBox = new javax.swing.JComboBox(combovarmodel);        	
    
    //para los valores de las variables    
    private DefaultComboBoxModel combomodel=new DefaultComboBoxModel();
    private javax.swing.JComboBox valuesComboBox = new javax.swing.JComboBox(combomodel);    

    private EditorTableModel model;
    private javax.swing.JTable caseEditorTable;
    
    private Node nodeCaseActual;
	
	public CaseEditor(NetworkFrame frame){
	
		switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: menuBundle = ResourceBundle.getBundle ("elvira/localize/Menus");            
			                         break;
		   case Elvira.SPANISH: menuBundle = ResourceBundle.getBundle ("elvira/localize/Menus_sp");   
			                        break;		                         
		}				
		
  		setModal(true);
		setResizable(false);		

		getContentPane().add(explanationToolbar);
		explanationToolbar.setBounds(36,50,324,30);
		explanationToolbar.setVisible(true);
		insertButton (firstButton, firstIcon, 
		              explanationToolbar, "Explanation.First");		
		insertButton (previousButton, previousIcon, 
		              explanationToolbar, "Explanation.Previous");
				
		explanationToolbar.add(nodeName);
        nodeName.setHorizontalAlignment(JTextField.CENTER);
		insertButton (nextButton, nextIcon, 
		              explanationToolbar, "Explanation.Next");		
		insertButton (lastButton, lastIcon, 
		              explanationToolbar, "Explanation.Last");		

		ElviraAction elviraAction = new ElviraAction();
		firstButton.addActionListener (elviraAction);
		nextButton.addActionListener (elviraAction);
		previousButton.addActionListener (elviraAction);
		lastButton.addActionListener (elviraAction);
		okButton.addActionListener (elviraAction);
		cancelButton.addActionListener (elviraAction);
	    
        //panel sobre el que se hace la inferencia y desde el que se ejecuta el CaseEditor
        infpanel=frame.getInferencePanel();
        //red sobre la que se está haciendo inferencia
        bnet=infpanel.getBayesNet();
        //lista de casos asociada a la red
        casesl=infpanel.getCasesList();    

        setNodeName(casesl.getCurrentCase().getIdentifier());
        setColorNodeName(casesl.getCurrentCase().getColor());
        //caso activo
        cedit=casesl.getCurrentCase();
        
        f=new Finding();
        evidence=new Evidence();
        
        if (casesl.getNumStoredCases()==0)
            commentTextField.setText(Elvira.localize(menuBundle, "NoCaseStored"));
            else commentTextField.setText(Elvira.localize(menuBundle, "ActiveCase"));
		
		columnNames=new String[2];
		columnNames[0]="Variable";
		columnNames[1]= Elvira.localize(menuBundle,"Value");
        model= new EditorTableModel(columnNames,0);
        caseEditorTable = new javax.swing.JTable(model);

		setModal(true);
		setTitle(Elvira.localize(menuBundle, "Explanation.Editor.label"));
		getContentPane().setLayout(null);
		setSize(405,300);
	    setVisible(false);
		commentLabel.setText(Elvira.localize(menuBundle, "Comment"));
		getContentPane().add(commentLabel);
        commentLabel.setBounds(20,10,80,30);
		getContentPane().add(commentTextField);
        commentTextField.setBounds(103,12,280,24);
     	caseScrollPane.setOpaque(true);
		getContentPane().add(caseScrollPane);
		caseScrollPane.setBounds(36,90,212,156);
		caseScrollPane.getViewport().add(caseEditorTable);
		caseEditorTable.setBounds(0,0,189,153);
        
		newFindingButton.setText(Elvira.localize(menuBundle, "NewFinding"));
		newFindingButton.setActionCommand(Elvira.localize(menuBundle, "NewFinding"));
		getContentPane().add(newFindingButton);
		newFindingButton.setBounds(264,94,130,40);
		deleteEvidenceButton.setText(Elvira.localize(menuBundle, "DeleteEvidence"));
		deleteEvidenceButton.setActionCommand(Elvira.localize(menuBundle, "DeleteEvidence"));
		getContentPane().add(deleteEvidenceButton);
		deleteEvidenceButton.setBounds(264,148,130,40);
		propagateButton.setText(Elvira.localize(menuBundle, "Propagate"));
		propagateButton.setActionCommand(Elvira.localize(menuBundle, "Propagate"));
		getContentPane().add(propagateButton);
		propagateButton.setBounds(264,200,130,40);
		okButton.setText(Elvira.localize(menuBundle, "OK.label"));
		okButton.setBounds(40, 260, 100, 30);
		getContentPane().add(okButton);		
		cancelButton.setText(Elvira.localize(menuBundle, "Cancel.label"));
		cancelButton.setBounds(150, 260, 100, 30);
	    getContentPane().add(cancelButton);
        
        EditorAction edAction = new EditorAction();
		newFindingButton.addActionListener(edAction);
		propagateButton.addActionListener(edAction);
		deleteEvidenceButton.addActionListener(edAction);
		
        variablesComboBox.addActionListener(edAction);
        valuesComboBox.addActionListener(edAction);		                
        
        enableDialog (true);

        f=new Finding();
        evidence=new Evidence();
        
        if (casesl.getNumActiveCase()==0)
            commentTextField.setText("There is no case stored. You must enter evidence before");
		
		caseEditorTable.setRowSelectionAllowed(false);
		model.setMaxRows(bnet.getNodeList().size());		        

		model.fillValuesTable(cedit);

		setUpVariablesColumn(caseEditorTable.getColumnModel().getColumn(0));
        setUpValuesColumn(caseEditorTable.getColumnModel().getColumn(1));

        caseEditorTable.getModel().addTableModelListener(
            new TableModelListener() {

                //este método solo se ejecuta cuando se modifica alguna celda de la tabla
           	    public void tableChanged (TableModelEvent e) {
           	        int fila=e.getFirstRow();
           	        int columna=e.getColumn();
           	        if (fila>0){
	      	            String nodename=(String) model.getValueAt(fila, 0);
    	       	        if (columna==0)//solo nos interesa controlar cuando se modifican los nombres de las variables. 
           	        			   //Sus valores se calculan en función de estos
        	   	            test_variables(fila, nodename);
            	    }
            	    
            	}	
            });
            
    			
	setLocationRelativeTo(Elvira.getElviraFrame());
        
	}//fin del constructor


	public CaseEditor(String sTitle, NetworkFrame frame){
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
	   deleteEvidenceButton.setEnabled(isEditable);
	   propagateButton.setEnabled(isEditable);
	}

   public void setNodeName(String name){
      nodeName.setFont(new Font("SansSerif",Font.BOLD,12));
      nodeName.setText(name);     
      nodeName.setEditable(false);
   }

   public void setColorNodeName(Color c){
      nodeName.setBackground(c);
   }

   public void insertButton (AbstractButton button, javax.swing.Icon icon,
                     JToolBar toolbar, String name) {
		button.setIcon(icon);
		button.setToolTipText(ElviraFrame.localize(menuBundle,name+".tip"));
		button.setMnemonic(ElviraFrame.localize(menuBundle,name+".mnemonic").charAt(0));
		toolbar.add(button);
		button.setMargin(new Insets (1,1,1,1));
		button.setAlignmentY(0.5f);
		button.setAlignmentX(0.5f);
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

	public void setUpValuesColumn (TableColumn valueColumn) {
        //Set up the editor for the sport cells.

        valueColumn.setCellEditor(new DefaultCellEditor(valuesComboBox));
        
        //Set up tool tips for the sport cells.
        DefaultTableCellRenderer renderer =new DefaultTableCellRenderer();
        renderer.setToolTipText("Click for combo box");
        valueColumn.setCellRenderer(renderer);
        //Set up tool tip for the sport column header.
        TableCellRenderer headerRenderer = valueColumn.getHeaderRenderer();
        if (headerRenderer instanceof DefaultTableCellRenderer) {
            ((DefaultTableCellRenderer)headerRenderer).setToolTipText(
                     "Click the value to see a list of choices");
        } 
 	}

    class EditorAction implements java.awt.event.ActionListener{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == newFindingButton)
				newFindingButton_actionPerformed(event);
			else if (object == propagateButton)
				    propagateButton_actionPerformed(event);
			    else if (object == deleteEvidenceButton)
				        deleteEvidenceButton_actionPerformed(event);
                     else if (object == valuesComboBox)
 		                    valuesComboBox_itemStateChanged(event);			                  
		                  else if (object == variablesComboBox)
 		                           variablesComboBox_itemStateChanged(event);	}
	}


	void newFindingButton_actionPerformed(java.awt.event.ActionEvent event){
	    
	   if (model.getRowCount()==model.getMaxRows())
           ShowMessages.showMessageDialog(ShowMessages.EVIDENCE_CASE_FULL, 
                     JOptionPane.ERROR_MESSAGE);
       else {
             if ((casesl.getNumCurrentCase()==0)&& (casesl.getNumStoredCases()>2))
                 ShowMessages.showMessageDialog(ShowMessages.PRIORI_UNCHANGED, 
                     JOptionPane.ERROR_MESSAGE);
             
             else {
                   if ((casesl.getNumCurrentCase()==0) && (casesl.getNumActiveCase()==1) && 
                       (!casesl.getActiveCase().getPropagated())){
                        ShowMessages.showMessageDialog(ShowMessages.PRIORI_UNCHANGED_NEW, 
                           JOptionPane.ERROR_MESSAGE);
                        casesl.setCurrentCase(1);
                   }
             
                   setNodeName(casesl.getCurrentCase().getIdentifier()); 
                   setColorNodeName(casesl.getCurrentCase().getColor());                   
                   nodeName.repaint();
                   model.addRow(emptyRow);
                   commentTextField.setText("");
 	        }
 	   }
	}

	void propagateButton_actionPerformed(java.awt.event.ActionEvent event){

        for (int n=0; n<bnet.getNodeList().size(); n++)
    		casesl.getCurrentCase().unsetAsFinding(bnet.getNodeList().elementAt(n));
        for(int f=0; f<caseEditorTable.getRowCount(); f++){
            //Prevenimos que haya filas en las que no se haya introducido ningún hallazgo
            if (!((String)caseEditorTable.getValueAt(f,0)).equals(" ")){
    		    Node n=bnet.getNodeList().getNodeString((String) caseEditorTable.getValueAt(f,0),true);
                if (n.getClass()==FiniteStates.class){
                    int value=((FiniteStates)n).getId((String) caseEditorTable.getValueAt(f,1));
                //al marcarlo como hallazgo, se actualiza la evidencia asociada a dicho caso
    		    casesl.getCurrentCase().setAsFinding((FiniteStates) n,value);                
	    	    casesl.addCurrentCase((FiniteStates) n,value);
/*andrew*/      }else if (n.getClass()==Continuous.class && n.getKindOfNode()==Node.CHANCE){
                    double val=0.0;
                    if (caseEditorTable.getValueAt(f,1).getClass()==String.class)
                        val=(new Double((String)caseEditorTable.getValueAt(f,1))).doubleValue();
                    if (caseEditorTable.getValueAt(f,1).getClass()==Double.class)
                        val=((Double)caseEditorTable.getValueAt(f,1)).doubleValue();

                    casesl.getCurrentCase().setAsFinding((Continuous)n,val);
                    casesl.addCurrentCase((Continuous) n,val);
                }
	    	}
        }
        if (!infpanel.propagate(casesl.getCurrentCase())){
            ShowMessages.showMessageDialog(ShowMessages.IMPOSIBLE_EVIDENCE, 
                     JOptionPane.ERROR_MESSAGE);
        for (int n=0; n<bnet.getNodeList().size(); n++)
    		casesl.getCurrentCase().unsetAsFinding(bnet.getNodeList().elementAt(n));
    		infpanel.propagate(casesl.getCurrentCase());
         }
	}

    void deleteEvidenceButton_actionPerformed(java.awt.event.ActionEvent event){
	    //al borrar la evidencia lo que hacemos es marcar todos los nodos como no observados y propagar
        for (int n=0; n<bnet.getNodeList().size(); n++)
    		casesl.getCurrentCase().unsetAsFinding(bnet.getNodeList().elementAt(n));
       model.removeValuesTable();
       if (infpanel.AUTOPROPAGATION) infpanel.propagate(casesl.getCurrentCase());
	   infpanel.repaint();
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
//                            model.removeRow(fila);
                            variablesComboBox.removeItem(nodeName);
                            if (combomodel.getSize()>0) combomodel.removeAllElements();
                            combomodel.addElement("");
                            valuesComboBox.setSelectedItem("");
                        }
                        else f++;
		            else f++;
		        }
		}
		else ShowMessages.showMessageDialog(ShowMessages.DELETING_FINDING, JOptionPane.ERROR_MESSAGE);
        
	}
	
	
	private void updateVariablesComboBox(String nodeName){
            if (combovarmodel.getSize()>0) combovarmodel.removeAllElements(); 
                    combovarmodel.addElement(nodeName);
            for (int posn=0; posn<bnet.getNodeList().size(); posn++){
                    Node n=(Node)bnet.getNodeList().elementAt(posn);
                    if (!cedit.getEvidence().isObserved(n))
                    combovarmodel.addElement(n.getNodeString(true));
            }
            combovarmodel.addElement(" ");
	}
	
	private void updateValuesComboBox(String nodeName){
        if (nodeName.equals(" ")){
            if (combomodel.getSize()>0) combomodel.removeAllElements(); 
            combomodel.addElement(" ");
        }
        else {
                    NodeList nl=bnet.getNodeList();
                    for (int p=0; p<nl.size(); p++){
                        String nodep=((Node)nl.elementAt(p)).getTitle();
                        if (nodep==null || nodep.equals(""))
                            nodep=((Node)nl.elementAt(p)).getName();
                        
                        if (nodeName.equals(nodep)){
                           nodeCaseActual=nl.elementAt(p);

/*andrew*/                 if (nl.elementAt(p).getClass()==FiniteStates.class){
                                FiniteStates fs=(FiniteStates) nl.elementAt(p);
                                Vector states=fs.getStates();
                                if (combomodel.getSize()>0) combomodel.removeAllElements();
                                for (int s=0; s<states.size(); s++)
                                    combomodel.addElement(states.elementAt(s));
                          }else if (((Node)nl.elementAt(p)).getClass()==Continuous.class && ((Node)nl.elementAt(p)).getKindOfNode()==Node.CHANCE){
                                if (combomodel.getSize()==0){ 
                                        combomodel.insertElementAt("Nuevo Valor",0);
                                        combomodel.insertElementAt("0.0",0);
                                }
                            }
                        }
                    }
            }
     }
     
	void variablesComboBox_itemStateChanged(java.awt.event.ActionEvent event){
        String nodeName=(String) variablesComboBox.getSelectedItem();
    }
		
	void valuesComboBox_itemStateChanged(java.awt.event.ActionEvent event){

            
            if (nodeCaseActual.getClass()==Continuous.class && nodeCaseActual.getKindOfNode()==Node.CHANCE){

                if (valuesComboBox.getSelectedIndex()==(combomodel.getSize()-1)){
                    IntroEvidenceDialog intro=new IntroEvidenceDialog(this,(Continuous)nodeCaseActual);
                    intro.setBounds(300,300,400,200);
                    intro.setVisible(true);

                    if (intro.isValidEvidence()){
                          Double d=new Double(intro.getEvidence());
                          combomodel.insertElementAt(d,0);
                          caseEditorTable.setValueAt(d,model.getActualRow(),1);

                    }
                }

            }

        
        }


public class EditorTableModel extends DefaultTableModel{
        int maxrows=0;
        
        private int actualRow;
        private int actualColumn;
        
        EditorTableModel(Object[] columnNames,int numrows) {
         super (columnNames,numrows);
      }
         
         void fillValuesTable(Case c){
                Object[] data=new Object[2];  
                for (int n=0; n<c.getObserved().length; n++){
                    if (c.getObserved()[n]){
                        data[0]=c.getNode(n).getNodeString(true);
                        if (c.getNode(n).getClass()==FiniteStates.class)
                            data[1]=c.getObservedStateNode(n);
                        else
                            data[1]=new Double(c.getObservedValueNode(n));                            
                    addRow(data);                        
                    }

                }
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
         * This method is called everytime some cell is going to be edited
         */

        public boolean isCellEditable(int row, int col) {
        	
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            actualRow=row; /*andrew*/
            actualColumn=col;
            if (col < 0)
                return false;
            else {if (col==0)
       				 updateVariablesComboBox(getValueAt(row,col).toString());
            	  else if (col==1)
       				 updateValuesComboBox(getValueAt(row,col-1).toString());
                 return true;
            }
        }
        /*
         * Return the actual row number what is being editable
         */
        public int getActualRow(){
            
            return actualRow;
        }

        /*
         * Return the actual column number what is being editable
        */
        public int getActualColumn(){
            
            return actualColumn;
        }

        
}//fin de la clase EditorTableModel

	class ElviraAction implements ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event){
			Object object = event.getSource();
		   if (object == okButton){
		            okButton_actionPerformed (event);
		            
		   }
		   else if (object == cancelButton){
		            cancelButton_actionPerformed (event);
		   }
		   else {
		   		if (object ==firstButton){
		      		Elvira.getElviraFrame().firstCaseAction (event);  
		   		}
           		else if (object ==nextButton){
		      		Elvira.getElviraFrame().nextCaseAction (event);  
		   		}
		   		else if (object ==previousButton){
		      		Elvira.getElviraFrame().previousCaseAction (event);  
		   		}
		   		else if (object ==lastButton){
		      		Elvira.getElviraFrame().lastCaseAction (event);  
		   		}
		        setNodeName(casesl.getCurrentCase().getIdentifier());
        		setColorNodeName(casesl.getCurrentCase().getColor());
		        model.removeValuesTable();
        		model.fillValuesTable(casesl.getCurrentCase());
		        repaint();
		}
	}

	void cancelButton_actionPerformed(ActionEvent event){
	   dispose();
	}

	void okButton_actionPerformed(ActionEvent event){
        if (infpanel.AUTOPROPAGATION) propagateButton_actionPerformed(event);
        else {
                for (int n=0; n<bnet.getNodeList().size(); n++)
    		        casesl.getCurrentCase().unsetAsFinding(bnet.getNodeList().elementAt(n));
                for(int f=0; f<caseEditorTable.getRowCount(); f++){
            //Prevenimos que haya filas en las que no se haya introducido ningún hallazgo
                    if (!((String)caseEditorTable.getValueAt(f,0)).equals(" ")){
    		            Node n=bnet.getNodeList().getNodeString((String) caseEditorTable.getValueAt(f,0),true);
                        int value=((FiniteStates)n).getId((String) caseEditorTable.getValueAt(f,1));
                //al marcarlo como hallazgo, se actualiza la evidencia asociada a dicho caso
    		            casesl.getCurrentCase().setAsFinding((FiniteStates) n,value);                
	    	            casesl.addCurrentCase((FiniteStates) n,value);
	    	        }
                }
                infpanel.notpropagate(casesl.getCurrentCase());
        }
        dispose();
	}
	
    }//fin de la clase ElviraAction
    
}//fin de la clase CaseEditor	

