/*
 * EditNonSenseConstraints.java
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

public class EditNonSenseConstraints extends JDialog {

    ResourceBundle dialogBundle = Elvira.getElviraFrame().getDialogBundle();
    javax.swing.JPanel buttonsPanel = new javax.swing.JPanel();
    javax.swing.JPanel editPanel = new javax.swing.JPanel();
    javax.swing.JButton okButton = new javax.swing.JButton();
    javax.swing.JButton cancelButton = new javax.swing.JButton();
    javax.swing.JButton applyButton = new javax.swing.JButton();
    javax.swing.JScrollPane decisionScrollPane = new javax.swing.JScrollPane();
    javax.swing.JScrollPane chanceScrollPane = new javax.swing.JScrollPane();
    javax.swing.JTable decisionTable = new javax.swing.JTable();
    javax.swing.JTable chanceTable = new javax.swing.JTable();
    javax.swing.JButton setButton = new javax.swing.JButton();
    public javax.swing.ImageIcon setIcon = new javax.swing.ImageIcon("elvira/gui/images/question.gif");
    javax.swing.JDialog statesDialog = new javax.swing.JDialog();
    javax.swing.JPanel statesDialogPanel = new javax.swing.JPanel();
    javax.swing.JButton okStatesButton = new javax.swing.JButton();
    javax.swing.JButton cancelStatesButton = new javax.swing.JButton();
    javax.swing.JPanel buttonsStatesDialogPanel = new javax.swing.JPanel();
    javax.swing.JScrollPane statesDialogScrollPane = new javax.swing.JScrollPane();
    javax.swing.JTable statesDialogTable = new javax.swing.JTable();
    Object[][] statesData;
    String[] statesName = {localize(Elvira.getElviraFrame().getDialogBundle(),"EditVariable.State.label")};
    StatesTableModel statesDialogModel = new StatesTableModel(statesData,statesName);
    ButtonGroup statesGroup = new ButtonGroup();
    EditTableModel decisionModel, chanceModel;
    String[] columnDecisionName = {localize(dialogBundle,"ENSCDecision.label")};
    String[] columnChanceName = {localize(dialogBundle,"ENSCChance.label")};
    Object[][] data;
    Object[] emptyRow = {""};
    Vector theDecisionNodes = new Vector();
    Vector theChanceNodes = new Vector();
    IDiagram theDiag;
    String lastSelectedDecisionNode = "";
    String stateName = "";
    boolean cancelled = false;
    boolean isAccepted = false;

    /* CONSTRUCTORS */

    /** Creates a new instance of EditIDiagramConstraints */
    public EditNonSenseConstraints() {
        setModal(true);
        getContentPane().setLayout(new BorderLayout(0,0));
	setSize(545,250);
	setVisible(false);
        setLocationRelativeTo(Elvira.getElviraFrame());

        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
	getContentPane().add(BorderLayout.SOUTH,buttonsPanel);
	buttonsPanel.setBounds(0,215,545,35);
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
        decisionScrollPane.setOpaque(true);
        editPanel.add(decisionScrollPane);
        decisionScrollPane.setBounds(34,16,204,150);
	decisionScrollPane.getViewport().add(decisionTable);
        chanceScrollPane.setOpaque(true);
        editPanel.add(chanceScrollPane);
        chanceScrollPane.setBounds(300,16,204,150);
        chanceScrollPane.getViewport().add(chanceTable);
        setButton.setActionCommand("Set");
	editPanel.add(setButton);
	setButton.setBounds(249,76,38,38);
	setButton.setIcon(setIcon);

        columnDecisionName[0] = localize(dialogBundle, "ENSCDecision.label");
        columnChanceName[0] = localize(dialogBundle, "ENSCChance.label");
        decisionModel = new EditTableModel(data, columnDecisionName);
        chanceModel = new EditTableModel(data, columnChanceName);
	decisionTable.setModel(decisionModel);
        chanceTable.setModel(chanceModel);

        decisionTable.getModel().addTableModelListener(
            new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    int theRow = e.getLastRow();
		    int theColumn = e.getColumn();

		    if ((theRow < 0) || (theColumn < 0)) {
                        return;
		    }

		    String datas = (String) decisionModel.getValueAt(theRow, theColumn);

		    if (datas==null) {
                        TableCellEditor rowcolCellEditor =
			  (TableCellEditor) decisionTable.getCellEditor(theRow,theColumn);
			rowcolCellEditor.stopCellEditing(); /* This was the action lacking */
		        datas = (String) rowcolCellEditor.getCellEditorValue();

		    }
		 }
	    });

            chanceTable.getModel().addTableModelListener(
                new TableModelListener() {
                    public void tableChanged(TableModelEvent e) {
                        int theRow = e.getLastRow();
                        int theColumn = e.getColumn();

                        if ((theRow < 0) || (theColumn < 0)) {
                            return;
                        }

                        String datas = (String) chanceModel.getValueAt(theRow, theColumn);

                        if (datas==null) {
                            TableCellEditor rowcolCellEditor =
                              (TableCellEditor) chanceTable.getCellEditor(theRow,theColumn);
                            rowcolCellEditor.stopCellEditing(); /* This was the action lacking */
                            datas = (String) rowcolCellEditor.getCellEditorValue();

                        }
                     }
	    });

        /* Dialog to choose parent state... */

        statesDialog.setModal(true);
        statesDialog.getContentPane().setLayout(new BorderLayout(0,0));
        statesDialog.setVisible(false);
        statesDialog.setResizable(false);
        statesDialog.setTitle(localize(dialogBundle, "EditConstraints.statesDialogTitle.label"));
        statesDialog.setBounds(0,0,180,230);
        statesDialog.setLocationRelativeTo(this);
        statesDialogPanel.setVisible(true);
        statesDialogPanel.setLayout(null);
        statesDialogPanel.setBounds(0,0,174,160);
        statesDialogScrollPane.setOpaque(true);
        statesDialogPanel.add(statesDialogScrollPane);
        statesDialogScrollPane.setBounds(7,7,160,150);
        statesDialogScrollPane.getViewport().add(statesDialogTable);
        statesDialog.getContentPane().add(statesDialogPanel);
        buttonsStatesDialogPanel.setLayout(null);
        statesDialog.getContentPane().add(buttonsStatesDialogPanel);
        buttonsStatesDialogPanel.setBounds(0,164,164,40);
        okStatesButton.setText(localize(dialogBundle,"OK.label"));
        okStatesButton.setActionCommand("OK");
        buttonsStatesDialogPanel.add(okStatesButton);
        okStatesButton.setBounds(3,170,80,30);
        cancelStatesButton.setText(localize(dialogBundle,"Cancel.label"));
        cancelStatesButton.setActionCommand("Cancel");
        buttonsStatesDialogPanel.add(cancelStatesButton);
        cancelStatesButton.setBounds(87,170,84,30);

        /* Listeners */

        SymAction lSymAction = new SymAction();
        editPanel.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        buttonsPanel.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        decisionScrollPane.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        chanceScrollPane.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        statesDialogScrollPane.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        statesDialogPanel.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        buttonsStatesDialogPanel.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        decisionTable.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        chanceTable.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        statesDialogTable.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0,false),JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	okButton.addActionListener(lSymAction);
        okButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
	cancelButton.addActionListener(lSymAction);
        cancelButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        applyButton.addActionListener(lSymAction);
        applyButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        setButton.addActionListener(lSymAction);
        setButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        okStatesButton.addActionListener(lSymAction);
        okStatesButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
        cancelStatesButton.addActionListener(lSymAction);
        cancelStatesButton.registerKeyboardAction(lSymAction, javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0,false),JComponent.WHEN_FOCUSED);
    }

    public EditNonSenseConstraints(IDiagram theDiagram, FiniteStates theDecNode, FiniteStates theChanceNode) {
        this();
        this.setTitle(localize(dialogBundle,"ENSCTitle.label"));
        if (theDecNode == null) {
          setDecisionTable(theDiagram);
        }
        else {
          if (theChanceNode == null) {
            setDecisionTable(theDecNode);
            setChanceTable(theDecNode);
          }
          else {
            setDecisionTable(theDecNode);
            setChanceTable(theDecNode);
            for (int i=0; i<chanceTable.getRowCount(); i++) {
              if (((String) chanceTable.getValueAt(i,0)).equals(theChanceNode.getName())) {
                chanceTable.changeSelection(i,0,true,false);
              }
            }
            stateName = ((FiniteStates) theDecNode).getState(((FiniteStates) theDecNode).getNumStates()-1);
            //isAccepted();
          }
        }
        theDiag = theDiagram;
    }

    /* METHODS */

    /**
     * Actions to perform when apply or accept buttons are pressed
     */

    public void isAccepted() {
      if (lastSelectedDecisionNode.equals("")) {
        return;
      }

      /**
       * To add: a dialog to select the state in decision node to link to the
       * Non sense state; for the moment, it will be the last state...
       */

      if (stateName.equals("")) {
        statesDialog.show();
      }

      if (cancelled) {
        cancelled = false;
        return;
      }

      /* END to add */

      Node theDNode = theDiag.getNodeList().getNode(lastSelectedDecisionNode);

      int[] theRows = chanceTable.getSelectedRows();

      boolean valid = true;

      for (int i=0; i<theRows.length; i++) {
        if (theRows[i]<0) {
          valid = false;
        }
      }

      if (valid) {
        for (int i=0; i<theRows.length; i++) {
          boolean toDo = true;
          Vector theRList = theDiag.getRelationList();
          for (int j=0; j<theRList.size(); j++) {
            if (((Relation) theRList.elementAt(j)).getKind() == Relation.CONSTRAINT) {
              NodeList theVars = ((Relation) theRList.elementAt(j)).getVariables();
              if (theVars.size() == 2) {
                if (((Node) theVars.elementAt(0)).getName().equals(lastSelectedDecisionNode)) {
                  if (((Node) theVars.elementAt(1)).getName().equals((String) chanceTable.getValueAt(theRows[i],0))) {
                    if (((Relation) theRList.elementAt(j)).getComment().equals("Non sense constraint")) {
                      toDo = false;
                      break;
                    }
                  }
                }
                if (((Node) theVars.elementAt(1)).getName().equals(lastSelectedDecisionNode)) {
                  if (((Node) theVars.elementAt(0)).getName().equals((String) chanceTable.getValueAt(theRows[i],0))) {
                    if (((Relation) theRList.elementAt(j)).getComment().equals("Non sense constraint")) {
                      toDo = false;
                      break;
                    }
                  }
                }
              }
            }
          }

          if (toDo) {
            ((IDiagram) theDiag).createVariableDoesntMakeSenseConstraint((FiniteStates) theDiag.getNodeList().getNode(lastSelectedDecisionNode),(FiniteStates) theDiag.getNodeList().getNode((String) chanceTable.getValueAt(theRows[i],0)),stateName);
          }

          /*if (toDo) {
            Node theCNode = theDiag.getNodeList().getNode((String) chanceTable.getValueAt(theRows[i],0));
            Relation theCRelation = theDiag.getRelation(theCNode);
            double[] theInitialValues = new double[(int) theCRelation.getValues().getSize()];
            if (theCRelation.getValues().getClass() == PotentialTable.class) {
              theInitialValues = ((PotentialTable) theCRelation.getValues()).getValues();
            }
            else if (theCRelation.getValues().getClass() == CanonicalPotential.class) {
              Vector allOfThem = new Vector();
              for (int m=0; m<((CanonicalPotential) theCRelation.getValues()).getArguments().size(); m++) {
                PotentialTable pt = (PotentialTable) ((CanonicalPotential) theCRelation.getValues()).getArgumentAt(m);
                for (int n=0; n<pt.getSize(); n++) {
                  allOfThem.addElement(new Double(((PotentialTable) ((CanonicalPotential) theCRelation.getValues()).getArgumentAt(m)).getValue(n)));
                }
              }
              theInitialValues = new double[(int) allOfThem.size()];
              for (int m=0; m<allOfThem.size(); m++) {
                theInitialValues[m] = ((Double) allOfThem.elementAt(m)).doubleValue();
              }
            }

            Vector theStates = ((FiniteStates) theCNode).getStates();
            boolean toAdd = true;
            for (int k=0; k<theStates.size(); k++) {
              if (theStates.elementAt(k).equals("\"VariableDoesntMakeSense\"")) {
                toAdd=false;
              }
            }
            if (toAdd) {
              theStates.addElement("\"VariableDoesntMakeSense\"");
              ((FiniteStates) theCNode).setStates(theStates);
            }
            NodeList theNL = new NodeList();
            Vector theVars = new Vector();
            theNL.insertNode(theDNode);
            theNL.insertNode(theCNode);
            theVars.addElement(theDNode);
            theVars.add(theCNode);

            // -> Please note -> The following method should be recursive
            fillInChanceValues(theDNode, theCNode, stateName,//(String) ((FiniteStates) theDNode).getState(((FiniteStates) theDNode).getStates().size()-1),
                               theInitialValues, theCRelation, true, toAdd);

            Relation theConstraint = new Relation(theVars);
            theConstraint.setKind(Relation.CONSTRAINT);
            theConstraint.setComment("Non sense constraint");
            Vector forAVS = new Vector();
            //forAVS.addElement(((FiniteStates) theDNode).getState(((FiniteStates) theDNode).getStates().size()-1));
            forAVS.addElement(stateName);
            ValuesSet forAntecedent = new ValuesSet(theDNode,forAVS,false);
            Vector forCVS = new Vector();
            forCVS.addElement("\"VariableDoesntMakeSense\"");
            ValuesSet forConsequent = new ValuesSet(theCNode,forCVS,false);
            LogicalNode theAntecedent = new LogicalNode(forAntecedent);
            LogicalNode theConsequent = new LogicalNode(forConsequent);
            LogicalExpression theLogicExpr = new LogicalExpression(theAntecedent,theConsequent,LogicalNode.DOUBLE_IMPLICATION);
            theConstraint.setValues(theLogicExpr);
            theDiag.addRelation(theConstraint);
          }*/
        }
      }
      else {
        return;
      }

    }

    /**
     * Used by the bundles. Find the string given as parameter in
     * the bundle specified
     */

    private String localize (ResourceBundle bundle, String name) {
        return Elvira.getElviraFrame().localize(bundle, name);
    }

    /**
     * Initializes the chance table with the possible chance nodes
     */

    private void setChanceTable(FiniteStates theDecNode) {
      LinkList theChildren = theDecNode.getChildren();
      Vector theData = new Vector();

      for (int i=0; i<theChildren.size(); i++) {
        if (((Link) theChildren.elementAt(i)).getHead().getKindOfNode() == Node.CHANCE) {
          theData.addElement((String) ((Link) theChildren.elementAt(i)).getHead().getName());
        }
      }

      data = new Object[theData.size()][1];
      for (int i=0; i<theData.size(); i++) {
        data[i][0]=new String((String) (theData.elementAt(i)));
      }

      chanceModel = new EditTableModel(data, columnChanceName);
      chanceTable.setModel(chanceModel);

      chanceTable.getModel().addTableModelListener(
          new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
              int theRow = e.getLastRow();
              int theColumn = e.getColumn();

              if ((theRow < 0) || (theColumn < 0)) {
                return;
              }

              String datas = (String) chanceModel.getValueAt(theRow, theColumn);

              if (datas==null) {
                TableCellEditor rowcolCellEditor =
                  (TableCellEditor) chanceTable.getCellEditor(theRow,theColumn);
                rowcolCellEditor.stopCellEditing(); /* This was the action lacking */
                datas = (String) rowcolCellEditor.getCellEditorValue();

              }
            }
         });

       statesDialogModel = new StatesTableModel(statesData,statesName);
       statesDialogTable.setModel(statesDialogModel);

       for (int i=0; i<theDecNode.getNumStates(); i++) {
         statesDialogModel.addRow(emptyRow);
         statesDialogTable.setValueAt(withoutQm((String)theDecNode.getState(i)),i,0);
       }
    }

    /**
     * Initializes the decision table with the only decision node
     */

    private void setDecisionTable(FiniteStates theDecNode) {

      lastSelectedDecisionNode = theDecNode.getName();
      data = new Object[1][1];
      data[0][0]=new String(theDecNode.getName());

      decisionModel = new EditTableModel(data, columnDecisionName);
      decisionTable.setModel(decisionModel);

      decisionTable.getModel().addTableModelListener(
          new TableModelListener() {
              public void tableChanged(TableModelEvent e) {
                  int theRow = e.getLastRow();
                  int theColumn = e.getColumn();

                  if ((theRow < 0) || (theColumn < 0)) {
                      return;
                  }

                  String datas = (String) decisionModel.getValueAt(theRow, theColumn);

                  if (datas==null) {
                      TableCellEditor rowcolCellEditor =
                        (TableCellEditor) decisionTable.getCellEditor(theRow,theColumn);
                      rowcolCellEditor.stopCellEditing(); /* This was the action lacking */
                      datas = (String) rowcolCellEditor.getCellEditorValue();
                  }
               }
          });

          decisionTable.clearSelection();
          decisionTable.changeSelection(0,0,true,false);
          //decisionTable.repaint();
    }

    /**
     * Initializes the decision table with the possible decision nodes
     */

    private void setDecisionTable(IDiagram theDiagram) {
        NodeList theNodes = theDiagram.getNodeList();
        Vector theData = new Vector();

        for (int i=0; i<theNodes.size(); i++) {
            if (((Node) theNodes.elementAt(i)).getKindOfNode() == Node.DECISION) {
               theData.addElement((String) ((FiniteStates) theNodes.elementAt(i)).getName());
            }
        }

        data = new Object[theData.size()][1];
        for (int i=0; i<theData.size(); i++) {
            data[i][0]=new String((String) (theData.elementAt(i)));
        }

        decisionModel = new EditTableModel(data, columnDecisionName);
        decisionTable.setModel(decisionModel);

        decisionTable.getModel().addTableModelListener(
            new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    int theRow = e.getLastRow();
		    int theColumn = e.getColumn();

		    if ((theRow < 0) || (theColumn < 0)) {
                        return;
		    }

		    String datas = (String) decisionModel.getValueAt(theRow, theColumn);

		    if (datas==null) {
                        TableCellEditor rowcolCellEditor =
			  (TableCellEditor) decisionTable.getCellEditor(theRow,theColumn);
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
            else if (object == setButton)
                setButton_actionPerformed(event);
            else if (object == okStatesButton)
                okStatesButton_actionPerformed(event);
            else if (object == cancelStatesButton)
                cancelStatesButton_actionPerformed(event);
            else if (object == editPanel)
                cancelButton_actionPerformed(event);
            else if (object == buttonsPanel)
                cancelButton_actionPerformed(event);
            else if (object == decisionScrollPane)
                cancelButton_actionPerformed(event);
            else if (object == chanceScrollPane)
                cancelButton_actionPerformed(event);
            else if (object == statesDialogPanel)
                cancelStatesButton_actionPerformed(event);
            else if (object == buttonsStatesDialogPanel)
                cancelStatesButton_actionPerformed(event);
            else if (object == decisionTable)
                cancelButton_actionPerformed(event);
            else if (object == chanceTable)
                cancelButton_actionPerformed(event);
            else if (object == statesDialogTable)
                cancelStatesButton_actionPerformed(event);
            else if (object == statesDialogScrollPane)
                cancelStatesButton_actionPerformed(event);
        }
    }

    private void okButton_actionPerformed(java.awt.event.ActionEvent event)
    {
      if (!isAccepted) {
        cancelled = false;
        isAccepted = true;
        isAccepted();
      }
      dispose();
    }

    private void cancelButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        dispose();
    }

    private void applyButton_actionPerformed(java.awt.event.ActionEvent event)
    {
      if (!isAccepted) {
        cancelled = false;
        isAccepted = true;
        isAccepted();
      }
    }

    private void setButton_actionPerformed(java.awt.event.ActionEvent event)
    {
      int[] theRows = decisionTable.getSelectedRows();

      if (theRows.length > 1) {
        return;
      }

      if (theRows.length == 0) {
        return;
      }

      if (theRows[0] < 0) {
        return;
      }

      lastSelectedDecisionNode = (String) decisionTable.getValueAt(theRows[0],0);
      setChanceTable((FiniteStates) theDiag.getNodeList().getNode((String) decisionTable.getValueAt(theRows[0],0)));

    }

    private void okStatesButton_actionPerformed(java.awt.event.ActionEvent event)
    {
      if ((statesDialogTable.getSelectedRows().length > 1) || (statesDialogTable.getSelectedRows().length == 0)) {
        cancelled = true;
        isAccepted = false;
      }
      else {
        stateName = addQm((String)statesDialogTable.getValueAt(statesDialogTable.getSelectedRows()[0],0));
      }

      statesDialog.dispose();
    }

    private void cancelStatesButton_actionPerformed(java.awt.event.ActionEvent event)
    {
      statesDialog.dispose();
      cancelled = true;
      isAccepted = false;
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
	  return false;
	}
    }

    public class StatesTableModel extends DefaultTableModel {

      public StatesTableModel (Object[][] data, Object[] columnNames) {
        super(data, columnNames);
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
        /*if (isEditable)
        return true;
        else
        return false;
        */
        return false;
      }
    }
    
    private String withoutQm(String s)
    {
       if (s.substring(0,1).equals("\""))
       {
          return (s.substring(1,s.length()-1));
       }
       else {
          return s;
       }
    }
    
       
    private String addQm(String s)
    {
        if (s.charAt(0)=='\"') return s;
        else return "\""+s+"\"";
    }
}
