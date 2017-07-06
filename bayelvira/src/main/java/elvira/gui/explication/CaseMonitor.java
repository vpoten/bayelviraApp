/* CaseMonitor.java */

package elvira.gui.explication;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import elvira.gui.NetworkFrame;
import elvira.gui.ShowMessages;
import elvira.Elvira;

public class CaseMonitor extends javax.swing.JDialog{

    private CasesList CL;

    private NetworkFrame nframe;
    
    private int maxNumStoredCases;
    
    private int maxNumShownCases;
    
    private int ActiveCase;
    
    private int oldcase;

    String[] columnNames;
        
    Object[][] data;

	
	CaseTableModel caseTableModel;
	javax.swing.JTable caseTable;


	public CaseMonitor(NetworkFrame frame)
	{
	int languaje = Elvira.getLanguaje();
		
   	switch (languaje) {
		   case Elvira.AMERICAN: explanationBundle = ResourceBundle.getBundle ("elvira/localize/Explanation");            		                         
		                         break;
		   case Elvira.SPANISH: explanationBundle = ResourceBundle.getBundle ("elvira/localize/Explanation_sp");   		                        
		                        break;		                         
		}				
		setModal(true);
		setResizable(false);		
		getContentPane().setLayout(null);
		setSize(330,300);
        setVisible(false);
		setTitle(Elvira.localize(explanationBundle, "MonitorTitle"));
		getContentPane().add(addCaseButton);
		addCaseButton.setBounds(24,12,45,45);
		getContentPane().add(deleteCaseButton);
		deleteCaseButton.setBounds(84,12,45,45);
		getContentPane().add(editCaseButton);
		editCaseButton.setBounds(144,12,45,45);
		getContentPane().add(optionsButton);
		optionsButton.setBounds(204,12,45,45);
		getContentPane().add(helpButton);
		helpButton.setBounds(264,12,45,45);
		caseScrollPane.setOpaque(true);
		getContentPane().add(caseScrollPane);
		caseScrollPane.setBounds(24,72,288,168);
		caseScrollPane.getViewport().add(caseTable);
		
		explainButton.setText(Elvira.localize(explanationBundle,"Explain"));
		explainButton.setActionCommand(Elvira.localize(explanationBundle,"Explain"));
		explainButton.setMnemonic('E');
		getContentPane().add(explainButton);
		explainButton.setBounds(24,252,84,40);
		okButton.setText(Elvira.localize(explanationBundle,"OK.label"));
		okButton.setActionCommand(Elvira.localize(explanationBundle,"OK.label"));
		getContentPane().add(okButton);
		okButton.setBounds(126,252,84,40);
		cancelButton.setText(Elvira.localize(explanationBundle,"Cancel"));
		cancelButton.setActionCommand(Elvira.localize(explanationBundle,"Cancel"));
		getContentPane().add(cancelButton);
		cancelButton.setBounds(228,252,84,40);
		nframe=frame;
		CL=frame.getInferencePanel().getCasesList();
		columnNames=new String[3];
		columnNames[0]= Elvira.localize (explanationBundle, "Inuse");
		columnNames[1]= Elvira.localize (explanationBundle, "Name");
		columnNames[2]= Elvira.localize (explanationBundle, "Color");
        caseTableModel= new CaseTableModel(columnNames,0);
        caseTable = new javax.swing.JTable(caseTableModel);
        caseScrollPane.getViewport().add(caseTable);

		setLocationRelativeTo(Elvira.getElviraFrame());
		

		addCaseButton.setIcon(addIcon);
		addCaseButton.setToolTipText(localize(explanationBundle,"CaseMonitor.add.tip"));
		addCaseButton.setMnemonic(localize(explanationBundle,"CaseMonitor.add.mnemonic").charAt(0));		

		deleteCaseButton.setIcon(deleteIcon);
		deleteCaseButton.setToolTipText(localize(explanationBundle,"CaseMonitor.delete.tip"));
		deleteCaseButton.setMnemonic(localize(explanationBundle,"CaseMonitor.delete.mnemonic").charAt(0));		

		editCaseButton.setIcon(editIcon);
		editCaseButton.setToolTipText(localize(explanationBundle,"CaseMonitor.edit.tip"));
		editCaseButton.setMnemonic(localize(explanationBundle,"CaseMonitor.edit.mnemonic").charAt(0));		
		
		optionsButton.setIcon(optionsIcon);
		optionsButton.setToolTipText(localize(explanationBundle,"CaseMonitor.options.tip"));
		optionsButton.setMnemonic(localize(explanationBundle,"CaseMonitor.options.mnemonic").charAt(0));		
		
		helpButton.setIcon(helpIcon);
		helpButton.setToolTipText(localize(explanationBundle,"CaseMonitor.help.tip"));
		helpButton.setMnemonic(localize(explanationBundle,"CaseMonitor.help.mnemonic").charAt(0));		
		
		//Set up renderer and editor for the Favorite Color column.
        caseTable.setBounds(0,0,285,165);   
        caseTable.getModel().addTableModelListener(
            new TableModelListener() {
           	    public void tableChanged (TableModelEvent e) {
	            }
	            });
    			
      caseTable.getRowSelectionAllowed();
      caseTableModel.fillTable(CL);
      
		
      setUpColorRenderer(caseTable);
      setUpColorEditor(caseTable);
      
      TableColumn column = caseTable.getColumn(caseTable.getColumnName(0));
      column.setMinWidth (50);
      column.setMaxWidth (50);
      
      column = caseTable.getColumn(caseTable.getColumnName(1));
      column.setMinWidth (150);
      column.setMaxWidth (150);
 
      column = caseTable.getColumn(caseTable.getColumnName(2));
      column.setMinWidth (82);
      column.setMaxWidth (82);
 
       CaseMonitorAction caseMonitorAction = new CaseMonitorAction();
	   editCaseButton.addActionListener(caseMonitorAction);
	   okButton.addActionListener(caseMonitorAction);
	   cancelButton.addActionListener(caseMonitorAction);
	   addCaseButton.addActionListener(caseMonitorAction);
	   deleteCaseButton.addActionListener(caseMonitorAction);
	   explainButton.addActionListener(caseMonitorAction);
	}

	public CaseMonitor(NetworkFrame c, String sTitle)
	{
		this(c);
		setTitle(sTitle);
	}

	public void setVisible(boolean b)
	{
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
	}

    public CasesList getCasesList(){
        return CL;
    }
    
    public NetworkFrame getNetworkFrame(){
        return nframe;
    }

    public CaseTableModel getCaseTableModel(){
        return caseTableModel;
    }
    
	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets and menu bar
		Insets insets = getInsets();
		int menuBarHeight = 0;
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	JButton addCaseButton = new JButton();
	JButton deleteCaseButton = new JButton();
	JButton editCaseButton = new JButton();
	JButton optionsButton = new JButton();
	JButton helpButton = new JButton();
	JScrollPane caseScrollPane = new JScrollPane();
	JButton explainButton = new JButton();
	JButton okButton = new JButton();
	JButton cancelButton = new JButton();
	//}}
	ResourceBundle explanationBundle;
	
	private static String imagesPath = "elvira/gui/images/";	
 	
 	private javax.swing.ImageIcon addIcon = new javax.swing.ImageIcon(imagesPath+"yes1a.gif"),
                                 deleteIcon = new javax.swing.ImageIcon(imagesPath+"no2c.gif"),
                                 editIcon = new javax.swing.ImageIcon(imagesPath+"editcase.gif"),
                                 optionsIcon = new javax.swing.ImageIcon(imagesPath+"tools1d.gif"),
                                 helpIcon = new javax.swing.ImageIcon(imagesPath+"question.gif");

	//{{DECLARE_MENUS
	//}}

   public static String localize (ResourceBundle bundle, String name) {
      
      try {
         return bundle.getString(name);
      }
      catch (MissingResourceException e) {
         int first = name.indexOf(".");
         int last = name.lastIndexOf(".");
         if (first == last) 
            first = 0;
         return name.substring(first, last);         
      }
   }
   
    class ColorRenderer extends JLabel
                        implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {

            super();
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(
                                JTable table, Object color, 
                                boolean isSelected, boolean hasFocus,
                                int row, int column) {
            setBackground((Color)color);
            if (isBordered) {
                if (isSelected) {

                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                                  table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
                                                  table.getBackground());

                    }
                    setBorder(unselectedBorder);
                }
            }
            return this;
        }
    }

    private void setUpColorRenderer(JTable table) {
        table.setDefaultRenderer(Color.class,
                                 new ColorRenderer(true));
    }

    //Set up the editor for the Color cells.
    private void setUpColorEditor(JTable table) {
        //First, set up the button that brings up the dialog.

        final JButton button = new JButton("") {
            public void setText(String s) {
                //Button never shows text -- only color.
            }
        };
        button.setBackground(Color.white);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0,0,0,0));

        //Now create an editor to encapsulate the button, and
        //set it up as the editor for all Color cells.
        final ColorEditor colorEditor = new ColorEditor(button);

        table.setDefaultEditor(Color.class, colorEditor);

        //Set up the dialog that the button brings up.
        final JColorChooser colorChooser = new JColorChooser();
        ActionListener okListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colorEditor.currentColor = colorChooser.getColor();
            }
        };
        final JDialog dialog = JColorChooser.createDialog(button,
                                        "Pick a Color",
                                        true,
                                        colorChooser,
                                        okListener,
                                        null); //XXXDoublecheck this is OK

        //Here's the code that brings up the dialog.
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button.setBackground(colorEditor.currentColor);
                colorChooser.setColor(colorEditor.currentColor);

                //Without the following line, the dialog comes up
                //in the middle of the screen.
                //dialog.setLocationRelativeTo(button);
                dialog.show();
            }
        });
    }

    /*
     * The editor button that brings up the dialog.
     * We extend DefaultCellEditor for convenience,
     * even though it mean we have to create a dummy
     * check box.  Another approach would be to copy
     * the implementation of TableCellEditor methods

     * from the source code for DefaultCellEditor.
     */
    class ColorEditor extends DefaultCellEditor {
        Color currentColor = null;

        public ColorEditor(JButton b) {
                super(new JCheckBox()); //Unfortunately, the constructor
                                        //expects a check box, combo box,
                                        //or text field.
            editorComponent = b;
            setClickCountToStart(1); //This is usually 1 or 2.

            //Must do this so that editing stops when appropriate.
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }

        public Object getCellEditorValue() {
            return currentColor;
        }

        public Component getTableCellEditorComponent(JTable table, 
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            ((JButton)editorComponent).setText(value.toString());
            currentColor = (Color)value;
            return editorComponent;
        }
    }

   
   
   public class CaseTableModel extends DefaultTableModel{
    
    int maxrows;

        CaseTableModel(Object[] columnNames,int numrows) {
         super (columnNames,numrows);
        }
         
        public void fillTable(CasesList c){
                Object[] data=new Object[3];  
                int n;
                for (n=0; n<c.getNumStoredCases()-1; n++){
                     if (c.getCaseNum(n).getIsShown())
                        data[0]=new Boolean(true);
                     else data[0]=new Boolean(false);
                     data[1]=c.getCaseNum(n).getIdentifier();
                     data[2]=c.getCaseNum(n).getColor();
                     addRow(data);                        
                }

                if (c.getCaseNum(c.getNumStoredCases()-1).getPropagated() && c.getActiveCase().getIsShown()){
                    data[0]=new Boolean(true);
                    data[1]=c.getCaseNum(n).getIdentifier();
                    data[2]=c.getCaseNum(c.getNumActiveCase()).getColor();                    
                    addRow(data);
                }
                else if (c.getCaseNum(c.getNumStoredCases()-1).getPropagated() && !c.getActiveCase().getIsShown()){
                    data[0]=new Boolean(false);
                    data[1]=c.getCaseNum(n).getIdentifier();
                    data[2]=c.getCaseNum(c.getNumActiveCase()).getColor();                    
                    addRow(data);
                }
                
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
                                      

        /*
         * JTable uses this method to determine the default renderer/

         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

   }

	class CaseMonitorAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == editCaseButton)
				editCaseButton_actionPerformed(event);
		   else if (object == cancelButton)
		      cancelButton_actionPerformed(event);
		   else if (object == okButton)
		      okButton_actionPerformed(event);
		   else if (object == addCaseButton)
		      addCaseButton_actionPerformed(event);
		   else if (object == deleteCaseButton)
		      deleteCaseButton_actionPerformed(event);
		   else if (object == explainButton)
		      explainButton_actionPerformed(event);
		      
		}
	}

	void editCaseButton_actionPerformed(java.awt.event.ActionEvent event)
	{
         int numfilas=caseTable.getSelectedRowCount();
         if (numfilas>1)
            ShowMessages.showMessageDialog(ShowMessages.EDIT_SEVERAL_CASES, JOptionPane.ERROR_MESSAGE);
         else if (numfilas==0)
                ShowMessages.showMessageDialog(ShowMessages.NO_ROW_SELECTED, JOptionPane.ERROR_MESSAGE);
              else {   
                    CL.setCurrentCase(caseTable.getSelectedRow());
                    CaseEditor d = new CaseEditor (nframe);
                    d.show();
	      }
        }
	
	void cancelButton_actionPerformed(ActionEvent event)
	{
	   dispose();
	}

	void okButton_actionPerformed(ActionEvent event)
	{
	   Case c;	   
	   boolean encf=false;
	   boolean encl=false;
	   for (int i=0; i<caseTable.getRowCount(); i++) {
	      c = CL.getCaseNum(i);
	      boolean b = ((Boolean) caseTable.getValueAt(i,0)).booleanValue();	      

	      //Si el caso activo no se ha propagado, no se puede mostrar
	      if ((i==(caseTable.getRowCount()-1) && b) &&
	         (i==CL.getNumActiveCase() && !CL.getActiveCase().getPropagated())){
              ShowMessages.showMessageDialog(ShowMessages.ACTIVE_CASE_NO_PROPAGATED, JOptionPane.ERROR_MESSAGE);
              b=!b;              
              caseTable.setValueAt(new Boolean(b),i,0);
          }

          //el caso actual no se puede marcar para no ser mostrado
          if (i==CL.getNumCurrentCase() && !b){
              ShowMessages.showMessageDialog(ShowMessages.CURRENT_CASE_SHOWN, JOptionPane.ERROR_MESSAGE);
              b=!b;              
              caseTable.setValueAt(new Boolean(b),i,0);
          }

	      if (b && !encf){
	          CL.setFirstShown(i);
	          encf=true;
	      }
	      if (b && !encl){
	          CL.setLastShown(i);
	      }
	      c.setIsShown (b);	      
	      if (((String) caseTable.getValueAt(i,1)).equals("Next Case"))
	         c.setIdentifier(localize(explanationBundle, "CaseNumber ")+String.valueOf(i));
	         else c.setIdentifier ((String) caseTable.getValueAt(i,1));
	      c.setColor((Color) caseTable.getValueAt(i,2));
	   }
//	   System.out.println("Caso activo "+CL.getNumActiveCase()+" Caso actual"+CL.getNumCurrentCase());
	   nframe.getInferencePanel().repaint();
	   Elvira.getElviraFrame().setNodeName(CL.getCurrentCase().getIdentifier());
	   Elvira.getElviraFrame().setColorNodeName(CL.getCurrentCase().getColor());
	   dispose();
	}
	
	void addCaseButton_actionPerformed(ActionEvent event)
	{
	    //si el caso activo=anterior a él, quiere decir que todavía no se ha añadido evidencia
	    //a dicho caso, lo que es equivalente a que está "vacío". Por tanto, en esta situación,
	    //al pulsar a añadir caso, saldrá un mensaje y no se añadirá uno nuevo.
	
          if (CL.getActiveCase().equals(CL.getCaseNum(CL.getNumActiveCase()-1)))
           ShowMessages.showMessageDialog(ShowMessages.EMPTY_ACTIVE_CASE, 
                     JOptionPane.ERROR_MESSAGE);

	     else {
                if (!CL.getActiveCase().getPropagated()){
                    CL.setCurrentCase(CL.getNumActiveCase());
                    nframe.getInferencePanel().propagate(CL.getActiveCase());                
                    nframe.getInferencePanel().repaint();

                }else {
                      CL.storeCase(CL.getActiveCase());
                      CL.getActiveCase().setIsShown(false);
                }
                
                Elvira.getElviraFrame().setNodeName(CL.getCurrentCase().getIdentifier());
                Elvira.getElviraFrame().setColorNodeName(CL.getCurrentCase().getColor());
                caseTableModel.removeTable();
                caseTableModel.fillTable(CL);
                repaint();
         }
	    
	}
	
    void deleteCaseButton_actionPerformed(ActionEvent event){
            int[] numfilas=caseTable.getSelectedRows();
            if (numfilas.length==0)
                ShowMessages.showMessageDialog(ShowMessages.NO_ROW_SELECTED, JOptionPane.ERROR_MESSAGE);
            else {
                  int casenum=numfilas[0];
                  for (int f=0; f<numfilas.length; f++){
                      if (casenum<1){
                          ShowMessages.showMessageDialog(ShowMessages.DELETE_PRIORICASE, JOptionPane.ERROR_MESSAGE);
                          casenum++;
                      }
                      else if (casenum==CL.getNumActiveCase() && !CL.getActiveCase().getPropagated())
                              ShowMessages.showMessageDialog(ShowMessages.DELETE_ACTIVE_CASE_NO_PROPAGATED, JOptionPane.ERROR_MESSAGE);
                           else {
                                 nframe.getInferencePanel().getResultsList().removeElementAt(CL.getNumActiveCase()-1);
                                 CL.removeCase(CL.getCaseNum(casenum));
                                 getCaseTableModel().removeTable();
                                 getCaseTableModel().fillTable(getCasesList());
                                 repaint();
                                 nframe.getInferencePanel().repaint();


                      }
                  }
             }
	}
	
    public void explainButton_actionPerformed(ActionEvent event){
         int[] numfilas=caseTable.getSelectedRows();
         if (numfilas.length==0)
             ShowMessages.showMessageDialog(ShowMessages.NO_ROW_SELECTED, JOptionPane.ERROR_MESSAGE);
         else if (numfilas.length==1){
               int casenum=numfilas[0];
               if (!CL.getCaseNum(casenum).getPropagated())
                   ShowMessages.showMessageDialog(ShowMessages.CURRENT_CASE_NO_PROPAGATED, JOptionPane.ERROR_MESSAGE);
               else {
                     dispose();    
                     oldcase=CL.getNumCurrentCase();
                     CL.setCurrentCase(casenum);
                     ExplainCase ec=new ExplainCase(CL, CL.getCaseNum(casenum), oldcase);
                     ec.show();
               }
         }
         else System.out.println("Solo se debe seleccionar una fila");
    }


}

