/*
 * EditIDiagramConstraints.java
 *
 * Created on 27 de diciembre de 2002, 10:23
 */

package elvira.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import java.io.*;
import java.util.*;
import java.awt.event.*;

import elvira.*;
import elvira.gui.*;
import elvira.Elvira;
import elvira.potential.*;

/**
 * Edits and Shows Constraints in IDiagrams.
 *
 * @author  Administrador
 */

public class EditIDiagramConstraints extends JDialog {

    ResourceBundle dialogBundle = Elvira.getElviraFrame().getDialogBundle();
    javax.swing.JPanel buttonsPanel = new javax.swing.JPanel();
    javax.swing.JPanel editPanel = new javax.swing.JPanel();
    javax.swing.JButton okButton = new javax.swing.JButton();
    javax.swing.JButton cancelButton = new javax.swing.JButton();
    javax.swing.JButton applyButton = new javax.swing.JButton();
    javax.swing.JButton newButton = new javax.swing.JButton();
    javax.swing.JButton editButton = new javax.swing.JButton();
    javax.swing.JButton deleteButton = new javax.swing.JButton();
    javax.swing.JScrollPane constraintsScrollPane = new javax.swing.JScrollPane();
    javax.swing.JTable constraintsTable = new javax.swing.JTable();
    javax.swing.JDialog constraintsTypeDialog = new javax.swing.JDialog();
    javax.swing.JPanel panelConstraints = new javax.swing.JPanel();
    javax.swing.JPanel buttonsConstraintsTypeDialogPanel = new javax.swing.JPanel();
    javax.swing.JButton okConstraintsTypeDialogButton = new javax.swing.JButton();
    javax.swing.JButton cancelConstraintsTypeDialogButton = new javax.swing.JButton();
    javax.swing.JButton upButton = new javax.swing.JButton();
    javax.swing.JButton downButton = new javax.swing.JButton();
    javax.swing.JRadioButton noSenseStateRadioButton = new javax.swing.JRadioButton();
    javax.swing.JRadioButton generalConstraintsRadioButton = new javax.swing.JRadioButton();
    ButtonGroup consGroup = new ButtonGroup();
    public javax.swing.ImageIcon arrowUpIcon = new javax.swing.ImageIcon("elvira/gui/images/arrowup.gif"),
	                         arrowDownIcon = new javax.swing.ImageIcon("elvira/gui/images/arrowdown.gif");
    EditTableModel model;
    String[] columnName = {localize(dialogBundle,"Constraints.label")};
    Object[][] data;
    Vector theConstraints = new Vector();
    Vector deletedConstraints = new Vector();
    Vector addedConstraints = new Vector();
    Vector oldConstraints = new Vector();
    IDiagram theDiag;

    /* CONSTRUCTORS */

    /** Creates a new instance of EditIDiagramConstraints */
    public EditIDiagramConstraints() {
        setModal(true);
        getContentPane().setLayout(new BorderLayout(0,0));
	setSize(578,376);
	setVisible(false);
        setLocationRelativeTo(Elvira.getElviraFrame());

        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
	getContentPane().add(BorderLayout.SOUTH,buttonsPanel);
	buttonsPanel.setBounds(0,341,578,35);
	okButton.setText(localize(dialogBundle,"OK.label"));
	okButton.setActionCommand("OK");
	buttonsPanel.add(okButton);
	okButton.setBounds(189,5,51,25);
	cancelButton.setText(localize(dialogBundle,"Cancel.label"));
	cancelButton.setActionCommand("Cancel");
	buttonsPanel.add(cancelButton);
	cancelButton.setBounds(245,5,73,25);
	applyButton.setText(localize(dialogBundle,"Apply.label"));
	applyButton.setActionCommand("Apply");
	buttonsPanel.add(applyButton);
	applyButton.setBounds(323,5,65,25);

        editPanel.setLayout(null);//FlowLayout.CENTER,5,5));
        getContentPane().add(BorderLayout.CENTER,editPanel);
	editPanel.setBounds(2,27,573,311);
        editPanel.setVisible(true);
        newButton.setText(localize(dialogBundle,"EditConstraints.New.label"));
        newButton.setActionCommand("New");
        editPanel.add(newButton);
        newButton.setBounds(467,93,70,33);
        editButton.setText(localize(dialogBundle,"EditConstraints.Edit.label"));
        editButton.setActionCommand("Edit");
        editPanel.add(editButton);
        editButton.setBounds(467,141,70,33);
        deleteButton.setText(localize(dialogBundle,"EditConstraints.Delete.label"));
        deleteButton.setActionCommand("Delete");
        editPanel.add(deleteButton);
        deleteButton.setBounds(467,189,70,33);
        constraintsScrollPane.setOpaque(true);
        editPanel.add(constraintsScrollPane);
        constraintsScrollPane.setBounds(34,30,380,270);
	constraintsScrollPane.getViewport().add(constraintsTable);
        upButton.setActionCommand("Up");
	editPanel.add(upButton);
	upButton.setBounds(420,125,32,30);
	downButton.setActionCommand("Down");
	editPanel.add(downButton);
	downButton.setBounds(420,155,32,30);
        upButton.setIcon(arrowUpIcon);
	downButton.setIcon(arrowDownIcon);
        enableEdition(true);

        columnName[0] = localize(dialogBundle, "Constraints.label");
        model = new EditTableModel(data, columnName);
	constraintsTable.setModel(model);

        constraintsTable.getModel().addTableModelListener(
            new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    int theRow = e.getLastRow();
		    int theColumn = e.getColumn();

		    if ((theRow < 0) || (theColumn < 0)) {
                        return;
		    }

		    String datas = (String) model.getValueAt(theRow, theColumn);

		    if (datas==null) {
                        TableCellEditor rowcolCellEditor =
			  (TableCellEditor) constraintsTable.getCellEditor(theRow,theColumn);
			rowcolCellEditor.stopCellEditing(); /* This was the action lacking */
		        datas = (String) rowcolCellEditor.getCellEditorValue();

		    }
		 }
	    });

        /* The dialog to choose what kind of constraint to edit / create */

        constraintsTypeDialog.setModal(true);
        constraintsTypeDialog.getContentPane().setLayout(new BorderLayout(0,0));
        constraintsTypeDialog.setVisible(false);
        constraintsTypeDialog.setResizable(false);
        constraintsTypeDialog.setTitle(localize(dialogBundle, "EditConstraints.constraintsTypeDialogTitle.label"));
        constraintsTypeDialog.setBounds(0,0,180,120);
        constraintsTypeDialog.setLocationRelativeTo(this);
        panelConstraints.setLayout(null);
        panelConstraints.setBounds(0,0,174,60);
        noSenseStateRadioButton.setText(
            localize(dialogBundle,"EditConstraints.NoSenseState.label"));
        noSenseStateRadioButton.setBounds(15,10,145,20);
        panelConstraints.add(noSenseStateRadioButton);
        generalConstraintsRadioButton.setText(
            localize(dialogBundle, "EditConstraints.General.label"));
        generalConstraintsRadioButton.setBounds(15,30,145,20);
        panelConstraints.add(generalConstraintsRadioButton);
        /*orRadioButton.setText(
            localize(dialogBundle,"EditVariable.constraintsTypeDialogOr.label"));
        orRadioButton.setBounds(15,30,145,20);
        panelConstraints.add(orRadioButton);
        causalMaxRadioButton.setText(
            localize(dialogBundle,"EditVariable.constraintsTypeDialogCausalMax.label"));
        causalMaxRadioButton.setBounds(15,50,145,20);
        panelConstraints.add(causalMaxRadioButton);
        generalizedMaxRadioButton.setText(
            localize(dialogBundle,"EditVariable.constraintsTypeDialogGeneralizedMax.label"));
        generalizedMaxRadioButton.setBounds(15,70,145,20);
        panelConstraints.add(generalizedMaxRadioButton);
        andRadioButton.setText(
            localize(dialogBundle,"EditVariable.constraintsTypeDialogAnd.label"));
        andRadioButton.setBounds(15,90,145,20);
        panelConstraints.add(andRadioButton);
        minRadioButton.setText(
            localize(dialogBundle,"EditVariable.constraintsTypeDialogMin.label"));
        minRadioButton.setBounds(15,110,145,20);
        panelConstraints.add(minRadioButton);
        xorRadioButton.setText(
            localize(dialogBundle,"EditVariable.constraintsTypeDialogXor.label"));
        xorRadioButton.setBounds(15,130,145,20);
        panelConstraints.add(xorRadioButton);*/
        panelConstraints.setBorder(new EtchedBorder());
        constraintsTypeDialog.getContentPane().add(panelConstraints);
        consGroup.add(noSenseStateRadioButton);
        consGroup.add(generalConstraintsRadioButton);
        /*relGroup.add(orRadioButton);
        relGroup.add(causalMaxRadioButton);
        relGroup.add(generalizedMaxRadioButton);
        relGroup.add(andRadioButton);
        relGroup.add(minRadioButton);
        relGroup.add(xorRadioButton);*/
        noSenseStateRadioButton.setSelected(true);
        generalConstraintsRadioButton.setSelected(false);
        /*orRadioButton.setEnabled(true);
        causalMaxRadioButton.setEnabled(true);
        generalizedMaxRadioButton.setEnabled(true);
        andRadioButton.setEnabled(true);
        minRadioButton.setEnabled(true);
        xorRadioButton.setEnabled(false);*/
        buttonsConstraintsTypeDialogPanel.setLayout(null);
        constraintsTypeDialog.getContentPane().add(buttonsConstraintsTypeDialogPanel);
        buttonsConstraintsTypeDialogPanel.setBounds(0,60,164,40);
        okConstraintsTypeDialogButton.setText(localize(dialogBundle,"OK.label"));
        okConstraintsTypeDialogButton.setActionCommand("OK");
        buttonsConstraintsTypeDialogPanel.add(okConstraintsTypeDialogButton);
        okConstraintsTypeDialogButton.setBounds(3,65,80,30);
        cancelConstraintsTypeDialogButton.setText(localize(dialogBundle,"Cancel.label"));
        cancelConstraintsTypeDialogButton.setActionCommand("Cancel");
        buttonsConstraintsTypeDialogPanel.add(cancelConstraintsTypeDialogButton);
        cancelConstraintsTypeDialogButton.setBounds(87,65,84,30);

        // Set the dialog position
        setLocationRelativeTo(Elvira.getElviraFrame());

        /* Listeners */

        SymAction lSymAction = new SymAction();
        editPanel.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        buttonsPanel.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        buttonsConstraintsTypeDialogPanel.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        panelConstraints.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        constraintsTable.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	okButton.addActionListener(lSymAction);
        okButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
	cancelButton.addActionListener(lSymAction);
        cancelButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        applyButton.addActionListener(lSymAction);
        applyButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        newButton.addActionListener(lSymAction);
        newButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        editButton.addActionListener(lSymAction);
        editButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        deleteButton.addActionListener(lSymAction);
        deleteButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        upButton.addActionListener(lSymAction);
        upButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        downButton.addActionListener(lSymAction);
        downButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        okConstraintsTypeDialogButton.addActionListener(lSymAction);
        okConstraintsTypeDialogButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        cancelConstraintsTypeDialogButton.addActionListener(lSymAction);
        cancelConstraintsTypeDialogButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
    }

    public EditIDiagramConstraints(IDiagram theDiagram) {
        this();
        this.setTitle(localize(dialogBundle,"EditConstraints.label")+" "+theDiagram.getName());
        setConstraintsTable(theDiagram);
        theDiag = theDiagram;
    }

    /* METHODS */

    /**
     * Actions to perform when apply or accept buttons are pressed
     */

    private void isAccepted() {
        for (int i=0; i<deletedConstraints.size(); i++) {
            ((IDiagram) theDiag).removeConstraint((Relation) deletedConstraints.elementAt(i));
        }

        for (int i=0; i<addedConstraints.size(); i++) {
            theDiag.addRelation((Relation) addedConstraints.elementAt(i));
        }

        /* And the changes made to the existing constraints... */
    }

    /**
     * Used by the bundles. Find the string given as parameter in
     * the bundle specified
     */

    private String localize (ResourceBundle bundle, String name) {
        return Elvira.getElviraFrame().localize(bundle, name);
    }

    /**
     * Just to enable/disable the edition possibilities according to
     * the selection made in the scroll pane
     */

    private void enableEdition(boolean according) {
        newButton.setEnabled(according);
	//newButton.setEnabled(false); /* For the moment */
        //editButton.setEnabled(according);
        editButton.setEnabled(false); /* For the moment */
        deleteButton.setEnabled(according);
	//deleteButton.setEnabled(false); /* For the moment */
    }

    /**
     * Initializes the constraints table
     */

    private void setConstraintsTable(IDiagram theDiagram) {
        Vector rl = theDiagram.getRelationList();
        Vector theData = new Vector();

        for (int i=0; i<rl.size(); i++) {
            if (((Relation) rl.elementAt(i)).getKind() == Relation.CONSTRAINT) {
               theData.addElement((String) ((Relation) rl.elementAt(i)).getName());
               theConstraints.addElement((Relation) rl.elementAt(i));
               oldConstraints.addElement((Relation) rl.elementAt(i));
            }
        }

        data = new Object[theData.size()][1];
        for (int i=0; i<theData.size(); i++) {
            /*if (((String) theData.elementAt(i)).equals("")) {
                data[i][0]=new String("Constraint "+i);
            }
            else {
                data[i][0]=(String) theData.elementAt(i);
            }*/
            data[i][0]=new String(((LogicalExpression) ((Relation) theConstraints.elementAt(i)).getValues()).returnExpression());
        }

        if (theData.size() > 0) {
            enableEdition(true);
        }

        model = new EditTableModel(data, columnName);
        constraintsTable.setModel(model);

        constraintsTable.getModel().addTableModelListener(
            new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    int theRow = e.getLastRow();
		    int theColumn = e.getColumn();

		    if ((theRow < 0) || (theColumn < 0)) {
                        return;
		    }

		    String datas = (String) model.getValueAt(theRow, theColumn);

		    if (datas==null) {
                        TableCellEditor rowcolCellEditor =
			  (TableCellEditor) constraintsTable.getCellEditor(theRow,theColumn);
			rowcolCellEditor.stopCellEditing(); /* This was the action lacking */
		        datas = (String) rowcolCellEditor.getCellEditorValue();

		    }
		 }
	    });

    }

    /* LISTENERS */

    class SymAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (object == okButton)
                okButton_actionPerformed(event);
            else if (object == cancelButton)
		cancelButton_actionPerformed(event);
            else if (object == applyButton)
		applyButton_actionPerformed(event);
            else if (object == newButton)
                newButton_actionPerformed(event);
            else if (object == editButton)
                editButton_actionPerformed(event);
            else if (object == deleteButton)
                deleteButton_actionPerformed(event);
            else if (object == upButton)
                upButton_actionPerformed(event);
            else if (object == downButton)
                downButton_actionPerformed(event);
            else if (object == okConstraintsTypeDialogButton)
                okConstraintsTypeDialogButton_actionPerformed(event);
            else if (object == cancelConstraintsTypeDialogButton)
                cancelConstraintsTypeDialogButton_actionPerformed(event);
            else if (object == editPanel)
                cancelButton_actionPerformed(event);
            else if (object == buttonsPanel)
                cancelButton_actionPerformed(event);
            else if (object == constraintsTable)
                cancelButton_actionPerformed(event);
            else if (object == panelConstraints)
                cancelConstraintsTypeDialogButton_actionPerformed(event);
            else if (object == buttonsConstraintsTypeDialogPanel)
                cancelConstraintsTypeDialogButton_actionPerformed(event);
        }
    }

    private void okButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        isAccepted();
        dispose();
    }

    private void cancelButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        dispose();
    }

    private void applyButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        isAccepted();
    }

    private void newButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        /*Object[] newData = {"NewConstraint "+addedConstraints.size()};
        String newString = new String("NewConstraint "+addedConstraints.size());
        model.addRow(newData);

        Relation newRel = new Relation();
        newRel.setKind(Relation.CONSTRAINT);
        newRel.setComment("");
        newRel.setName(newString);

        theConstraints.addElement(newRel);
        addedConstraints.addElement(newRel);*/

        constraintsTypeDialog.show();
    }

    private void editButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        int row = constraintsTable.getSelectedRow();

        if (row < 0) {
            row = 0;
        }

        /* To go on... */
    }

    private void deleteButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        int[] rows = constraintsTable.getSelectedRows();

        if (rows.length == 0) {
          return;
        }

        if (rows[0] < 0) {
            return;
        }

        for (int i=rows.length-1; i>=0; i--) {
          model.removeRow(rows[i]);
          deletedConstraints.addElement(theConstraints.elementAt(rows[i]));
        }

        for (int j=rows.length-1; j>=0; j--) {
          int toSee = -1;
          for (int i=0; i<addedConstraints.size(); i++) {
            if (addedConstraints.elementAt(i) == theConstraints.elementAt(rows[j])) {
               toSee = i;
               break;
            }
          }
          if (toSee != -1) {
            addedConstraints.removeElementAt(toSee);
          }

          theConstraints.removeElementAt(rows[j]);
        }

        if (constraintsTable.getRowCount() < 1) {
            enableEdition(false);
        }
    }

    private void upButton_actionPerformed(java.awt.event.ActionEvent event)
    {
      int[] theRows = constraintsTable.getSelectedRows();

      boolean isValid = true;
      for (int i=0; i<theRows.length; i++) {
        if (theRows[i] == 0) {
          isValid = false;
        }
      }

      if (isValid) {
        for (int i=0; i<theRows.length; i++) {
          constraintsTable.changeSelection(theRows[i]-1,0,true,false);
          constraintsTable.changeSelection(theRows[i],0,true,false);
        }
      }
    }

    private void downButton_actionPerformed(java.awt.event.ActionEvent event)
    {
      int[] theRows = constraintsTable.getSelectedRows();

      boolean isValid = true;
      for (int i=0; i<theRows.length; i++) {
        if (theRows[i] == constraintsTable.getRowCount()-1) {
          isValid = false;
        }
      }

      if (isValid) {
        for (int i=0; i<theRows.length; i++) {
          constraintsTable.changeSelection(theRows[i],0,true,false);
          constraintsTable.changeSelection(theRows[i]+1,0,true,false);
        }
      }
    }

    private void okConstraintsTypeDialogButton_actionPerformed(java.awt.event.ActionEvent event)
    {
      this.dispose();
      if (noSenseStateRadioButton.isSelected()) {
        constraintsTypeDialog.dispose();
        EditNonSenseConstraints eNSC = new EditNonSenseConstraints(theDiag,(FiniteStates) null,(FiniteStates) null);
        eNSC.show();
      }
      else if (generalConstraintsRadioButton.isSelected()) {
        constraintsTypeDialog.dispose();
      }
      else {
        constraintsTypeDialog.dispose();
      }
    }

    private void cancelConstraintsTypeDialogButton_actionPerformed(java.awt.event.ActionEvent event)
    {
      constraintsTypeDialog.dispose();
    }

    /* DEFINITIONS */

    public class EditTableModel extends DefaultTableModel {

        public EditTableModel (Object[][] data, Object[] columnName) {
	    super(data, columnName);
	}

	/**
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.  If we didn't implement this method,
	 * then the last column would contain text ("true"/"false"),
	 * rather than a check box.
	 */

	public Class getColumnClass(int c) {
	  if(getValueAt(0,c)!=null)
	    return getValueAt(0, c).getClass();
	  else
	    return super.getColumnClass(c);
	}

	/**
	 * Don't need to implement this method unless your table's
	 * editable.
	 */

	public boolean isCellEditable(int row, int col) {
	  //Note that the data/cell address is constant,
	  //no matter where the cell appears onscreen.
	  /*if (false)
	    return true;
	  else
	    return false;
*/
	  return false;
	}
    }
}
