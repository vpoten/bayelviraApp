package elvira.gui.explication;

import javax.swing.*;
import java.awt.*;
import elvira.Elvira;
import elvira.*;
import elvira.gui.*;
import elvira.inference.clustering.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.table.*;


public class ExplainCase extends javax.swing.JDialog{
    
    private Evidence evi;
    
    private Bnet bnet;
    
    private Case casexp;
    
    private CasesList casesl;

	private String[] columnNamesI = {"State", "PriorAcum", "PosteriorAcum", "AcumRatio"};
    private Object[][] data;
        
    private Object[] emptyRow = {"", ""};	
    private String[] columnNamesEviI= {"Variable", "Value"};
    private Object[][] dataevi;

    private Vector bnetlinks=new Vector();    
    private int oldcase;

	
	private CaseTableModel caseTableModel;
	private javax.swing.JTable caseTable;

	private ResourceBundle dialogBundle;
	
		

	public ExplainCase(CasesList CL, Case c, int old){
		// This line prevents the "Swing: checked access to system event queue" message seen in some browsers.
		getRootPane().putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);


		switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs");
		                         break;
		   case Elvira.SPANISH: dialogBundle = ResourceBundle.getBundle ("elvira/localize/Dialogs_sp");
		                        break;
		}
		evi=c.getEvidence();
		bnet=c.getBnet();
		casexp=c;
		casesl=CL;
		oldcase=old;
		
		getContentPane().setLayout(null);
		setModal(true);
		setSize(393,500);
		setTitle(Elvira.localize(dialogBundle, "ExplainingCase"));
		name.setText(Elvira.localize(dialogBundle, "Identifier"));
		name.setBounds(24,12,84,33);
		getContentPane().add(name);		

        nametext.setBounds(108,12,264,34);
        nametext.setEditable(false);
        nametext.setText(c.getIdentifier());
        
		getContentPane().add(nametext);
		
		evidence.setText(Elvira.localize(dialogBundle, "Evidence"));
		evidence.setBounds(24,60,85,84);
		getContentPane().add(evidence);
		
		evidenceScroll.setOpaque(true);
		evidenceScroll.setBounds(108,60,264,96);
		getContentPane().add(evidenceScroll);
		
		String[] columnNamesEvi=new String[columnNamesEviI.length];
		for (int ce=0; ce<columnNamesEvi.length; ce++)
			columnNamesEvi[ce]=Elvira.localize(dialogBundle, columnNamesEviI[ce]);

        model= new EvidenceTableModel(columnNamesEvi,0);
        caseEditorTable = new javax.swing.JTable(model);
		caseEditorTable.setRowSelectionAllowed(false);
		model.setMaxRows(evi.size());		        
		model.fillValuesTable(casexp);

		caseEditorTable.setBounds(0,0,156,96);
		evidenceScroll.getViewport().add(caseEditorTable);		

		evidenceprob.setText(Elvira.localize(dialogBundle, "ProbabilityEvidence"));
		evidenceprob.setBounds(48,168,181,39);
		getContentPane().add(evidenceprob);

		evidenceprobval.setBounds(228,168,108,36);
		getContentPane().add(evidenceprobval);
		HuginPropagation hp = new HuginPropagation(bnet,c.getEvidence(),"tables");
        if (c.getEvidence().size() > 0) 
            evidenceprobval.setText(""+hp.obtainEvidenceProbability("yes"));
        else evidenceprobval.setText("1.0");


		hipothesis.setBorder(titledBorder1);
		hipothesis.setLayout(null);
        hipothesis.setBounds(12,204,363,215);

 		titledBorder1 = new javax.swing.border.TitledBorder(Elvira.localize(dialogBundle, "Hypothesis"));
 		titledBorder1.setTitleFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
		titledBorder1.setTitleJustification(javax.swing.border.TitledBorder.CENTER);
		titledBorder1.setTitleColor(new java.awt.Color(128,128,255));
		getContentPane().add(hipothesis);

		hname.setText(Elvira.localize(dialogBundle, "Name"));
		hname.setBounds(24,24,84,36);
		hipothesis.add(hname);
		
		hiponame.setBackground(java.awt.Color.white);
		hiponame.setBounds(120,24,206,25);
		setValues();
		hiponame.setSelectedIndex(-1);
		hipothesis.add(hiponame);
		
 		String[] columnNames = new String[columnNamesI.length];
 		for (int cl=0; cl<columnNames.length; cl++)	
 			columnNames[cl]=Elvira.localize(dialogBundle, columnNamesI[cl]);
 		
        caseTableModel= new CaseTableModel(columnNames,0);
        caseTable = new javax.swing.JTable(caseTableModel);
        hipothesis.add(caseTable);
        
        caseTable.setBounds(0,0,324,100);   
        caseTable.getRowSelectionAllowed();
        caseTableModel.fillTable("");

        TableColumn colorColumn = caseTable.getColumn(Elvira.localize(dialogBundle, "State"));        
        
        // Set a pink background and tooltip for the Color column renderer.
        DefaultTableCellRenderer colorColumnRenderer = new DefaultTableCellRenderer();
        colorColumnRenderer.setBackground(Color.pink);
        colorColumn.setCellRenderer(colorColumnRenderer); 
        
        TableColumn column = caseTable.getColumn(caseTable.getColumnName(0));
        column.setMinWidth (70);
        column.setMaxWidth (70);
        column = caseTable.getColumn(caseTable.getColumnName(1));
        column.setMinWidth (80);
        column.setMaxWidth (80);
        column = caseTable.getColumn(caseTable.getColumnName(2));
        column.setMinWidth (90);
        column.setMaxWidth (90);
        column = caseTable.getColumn(caseTable.getColumnName(3));
        column.setMinWidth (80);
        column.setMaxWidth (80);

        tableScrollPane.setOpaque(true);
		hipothesis.add(tableScrollPane);
		tableScrollPane.setBounds(12,72,336,84);
		tableScrollPane.getViewport().add(caseTable);
		
		whyButton.setText(Elvira.localize(dialogBundle, "Why?"));
		whyButton.setForeground(java.awt.Color.blue);
		whyButton.setBounds(84,168,96,36);
		hipothesis.add(whyButton);
		
		howButton.setText(Elvira.localize(dialogBundle, "How?"));
		howButton.setForeground(java.awt.Color.blue);
		howButton.setBounds(192,168,96,36);
		hipothesis.add(howButton);
		
		closeButton.setText(Elvira.localize(dialogBundle, "Close"));
		closeButton.setBounds(132,432,123,36);
		getContentPane().add(closeButton);

        ExplainingAction explainingaction = new ExplainingAction();
	    hiponame.addActionListener(explainingaction);		
	    whyButton.addActionListener(explainingaction);		
	    howButton.addActionListener(explainingaction);		
	    closeButton.addActionListener(explainingaction);		
}
	
	javax.swing.JScrollPane tableScrollPane = new javax.swing.JScrollPane();
	javax.swing.JLabel name = new javax.swing.JLabel();
	javax.swing.JTextField nametext = new javax.swing.JTextField();
	javax.swing.JTextField evidenceprobval = new javax.swing.JTextField();
	javax.swing.JLabel evidence = new javax.swing.JLabel();
	javax.swing.JLabel evidenceprob = new javax.swing.JLabel();
	javax.swing.JScrollPane evidenceScroll= new javax.swing.JScrollPane();
	javax.swing.JPanel hipothesis = new javax.swing.JPanel();
    javax.swing.DefaultComboBoxModel modelcombo=new DefaultComboBoxModel();	
	javax.swing.JComboBox hiponame = new javax.swing.JComboBox(modelcombo);
	javax.swing.JLabel hname = new javax.swing.JLabel();
	javax.swing.JButton whyButton = new javax.swing.JButton();
	javax.swing.JButton howButton = new javax.swing.JButton();
	javax.swing.border.TitledBorder titledBorder1;
	javax.swing.JButton closeButton = new javax.swing.JButton();
	
    private EvidenceTableModel model;
    private javax.swing.JTable caseEditorTable;
	
	private void setValues(){
		if (modelcombo.getSize()>0) modelcombo.removeAllElements();
		
		for (int v=0; v<bnet.getNodeList().size(); v++){
	        FiniteStates fs = (FiniteStates) bnet.getNodeList().elementAt(v);
	        if (evi.indexOf(fs.getName())==-1)
        	    hiponame.addItem(fs.getNodeString(true));     
    	}
    }
    
    class ExplainingAction implements ActionListener{
		public void actionPerformed(java.awt.event.ActionEvent event){
			Object object = event.getSource();
			if (object == hiponame)
			    hiponame_actionPerformed(event);
			else if (object == whyButton)
		            whyButton_actionPerformed (event);
			else if (object == howButton)
		            howButton_actionPerformed (event);
			else if (object == closeButton)
		            closeButton_actionPerformed (event);
		}

        void hiponame_actionPerformed(ActionEvent event){
	         String nodeName = (String) hiponame.getSelectedItem();	
	         System.out.println(nodeName);
	         caseTableModel.removeTable();
             caseTableModel.fillTable(nodeName);
             repaint();
        }

        void whyButton_actionPerformed(ActionEvent event){
        	if (hiponame.getSelectedItem()!=null){
            	Node h=bnet.getNodeList().getNodeString((String) hiponame.getSelectedItem(), true);
            	new EvidenceAnalysis(bnet, casexp, evi, h).show();
            }
            else ShowMessages.showMessageDialog(ShowMessages.NO_VAR_SELECTED, JOptionPane.ERROR_MESSAGE); 
        }
        
        void howButton_actionPerformed(ActionEvent event){
            Node h=bnet.getNodeList().getNodeString((String) hiponame.getSelectedItem(), true);
            for (int i=0 ; i<bnet.getNodeList().size() ; i++){
                Node node=((Node) bnet.getNodeList().elementAt(i));
                node.setMarked(false);
            }   
			NodeList allmarked=macroExplanation.pathExplanation(casesl, bnet, evi, h);
            for (int n=0; n<allmarked.size(); n++)
               	((Node) allmarked.elementAt(n)).setMarked(true);

            InferencePanel infpanel=((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getInferencePanel();
            Elvira.getElviraFrame().setNodeName(casexp.getIdentifier());
            Elvira.getElviraFrame().setColorNodeName(casexp.getColor());
            infpanel.MACROEXPLANATION=true;
            infpanel.PATHS=true;
            Elvira.getElviraFrame().enablePathsButton();
            infpanel.repaint();



        }
        
        void closeButton_actionPerformed(ActionEvent event){
            InferencePanel infpanel=((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getInferencePanel();
            if (!infpanel.PATHS){
            	for (int n=0; n<bnet.getNodeList().size(); n++)
                	((Node)bnet.getNodeList().elementAt(n)).setVisited(false);
            	for (int l=0; l<bnet.getLinkList().size(); l++)
                	((Link)bnet.getLinkList().elementAt(l)).setUPDOWN(true);
            	casesl.setCurrentCase(oldcase);
            	Elvira.getElviraFrame().setNodeName(casesl.getCurrentCase().getIdentifier());
            	Elvira.getElviraFrame().setColorNodeName(casesl.getCurrentCase().getColor());
            	infpanel.MACROEXPLANATION=false;
        	}
            infpanel.repaint();
			dispose();        
		}

    }//end ExplainingAction	
   
   
   public class CaseTableModel extends DefaultTableModel{
    
    int maxrows;

        CaseTableModel(Object[] columnNames,int numrows) {
         super (columnNames,numrows);
        }
         
        public void fillTable(String nodeName){
            if (!nodeName.equals("")){
                FiniteStates fs = (FiniteStates) bnet.getNodeList().getNodeString(nodeName, true);
                Vector states = fs.getStates();
                
                Case prior=casesl.getCaseNum(0);
                double[] probp=prior.getProbOfNode(fs);

                Case current=casexp;
                double[] post=casexp.getProbOfNode(fs);
                
                Object[] data=new Object[4];
                double sum1=0,sum2=0;
                for (int st=0; st<states.size(); st++){
                     data[0]=(String) states.elementAt(st);
                     sum1=sum1+probp[st];
                     data[1]=new Double(sum1);
                     sum2=sum2+post[st];
                     data[2]=new Double(sum2);
                     
                	 if (!data[2].equals(new Double(0)))
                        data[3]=new Double(Math.log(((Double) data[2]).doubleValue()/((Double) data[1]).doubleValue()));
                        else data[3]=new String("Infinity");
                     addRow(data);                        
                }
            }
        }
        
        public boolean isCellEditable(int row, int col) {         
            return false;
        }

        public void removeTable(){
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
   }
}

class EvidenceTableModel extends DefaultTableModel{
        int maxrows=0;
        
        EvidenceTableModel(Object[] columnNames,int numrows) {
         super (columnNames,numrows);
      }
         
         void fillValuesTable(Case c){
                Object[] data=new Object[2];  
                for (int n=0; n<c.getObserved().length; n++){
                    if (c.getObserved()[n]){
                        data[0]=c.getNode(n).getNodeString(true);
                        data[1]=c.getObservedStateNode(n);
                    addRow(data);                        
                    }

                }
        }
         
        public boolean isCellEditable(int row, int col) {         
            return false;
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
        
}//fin de la clase EditorTableModel


