/*  ConstraintKnowledgeDialog.java */
package elvira.gui;

//Graphical interfaz classes
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.ListModel;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import java.awt.Rectangle;
import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;


//Java util & IO classes
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.File;

import java.util.Vector;
import java.util.ResourceBundle;

//Elvira classes
import elvira.Elvira;
import elvira.Graph;
import elvira.Bnet;
import elvira.NodeList;
import elvira.Node;
import elvira.Link;
import elvira.InvalidEditException;
import elvira.parser.ParseException;
import elvira.learning.constraints.ConstraintKnowledge;


/*
 * Graphical Interface to manage constraints knowledge.The constraints types supported
 * are: existence of  arc and/or edges constraints, absence of arc and/or edges
 * constraints and partial order constraints.
 * @author fjgc@decsai.ugr.es
 * @since 07/01/2003
 */
public class ConstraintKnowledgeDialog extends javax.swing.JDialog {

  /**
    * Bnet where we store the net that we use
    */
  private Bnet bnet;

  /**
  * ConstraintKnoledge where we store the contraints
  */
  private ConstraintKnowledge ck=null;

  /**
  * Network fram for this dialalog
  */
  private NetworkFrame networkframe;

  /**
  * Main JPanel of the dialalog
  */
  private JPanel mainpanel = new JPanel();

  /**
   * Main ConstraintKnowledgePanel of the dialalog
   */
  private ConstraintKnowledgePanel constraintKnowledgePanel;


  /**
   * TabbedPanel of the dialalog with 3 tabs
   */
  private JTabbedPane jTabbedPane = new JTabbedPane();

  /** Tab for the existence constraints  */
  private  JPanel tabExistence = new JPanel();
  /** Tab for the absence constraints  */
  private  JPanel tabAbsence = new JPanel();
  /** Tab for the partial order constraints  */
  private  JPanel tabPartialOrder = new JPanel();


  //Existence constraints part
  private JLabel labelTitleE = new JLabel();
  private JComboBox comboBoxTailE = new JComboBox();
  private JComboBox comboBoxSourceE = new JComboBox();
  private JLabel labelTailE = new JLabel();
  private JLabel labelHeadE = new JLabel();
  private JList listSelectedE = new JList();
  private JLabel labelSelectedE = new JLabel();
  private JRadioButton radioButtonDirectedE = new JRadioButton();
  private JTextField textFieldInputE = new JTextField();
  private JLabel labelInputE = new JLabel();
  private JTextField textFieldOutputE = new JTextField();
  private JLabel labelOutputE = new JLabel();
  private JButton buttonAddE = new JButton();
  private JButton buttonQuitE = new JButton();
  private JComboBox comboBoxHeadE = new JComboBox();
  private JPanel PanelFilesE = new JPanel();
  private TitledBorder titleBorderE;
  private JButton buttonSelecInputE = new JButton();
  private JButton buttonSelecOutputE = new JButton();


  //Absence constraints part
  private JLabel labelTitleA = new JLabel();
  private JComboBox comboBoxTailA = new JComboBox();
  private JComboBox comboBoxSourceA = new JComboBox();
  private JLabel labelTailA = new JLabel();
  private JLabel labelHeadA = new JLabel();
  private JList listSelectedA = new JList();
  private JLabel labelSelectedA = new JLabel();
  private JRadioButton radioButtonDirectedA = new JRadioButton();
  private JTextField textFieldInputA = new JTextField();
  private JLabel labelInputA = new JLabel();
  private JTextField textFieldOutputA = new JTextField();
  private JLabel labelOutputA = new JLabel();
  private JButton buttonAddA = new JButton();
  private JButton buttonQuitA = new JButton();
  private JComboBox comboBoxHeadA = new JComboBox();
  private JPanel PanelFilesA = new JPanel();
  private TitledBorder titleBorderA;
  private JButton buttonSelecInputA = new JButton();
  private JButton buttonSelecOutputA = new JButton();


  //Partial Order constraints part
  private JLabel labelTitleO = new JLabel();
  private JComboBox comboBoxTailO = new JComboBox();
  private JComboBox comboBoxSourceO = new JComboBox();
  private JLabel labelTailO = new JLabel();
  private JLabel labelHeadO = new JLabel();
  private JList listSelectedO = new JList();
  private JLabel labelSelectedO = new JLabel();
  private JTextField textFieldInputO = new JTextField();
  private JLabel labelInputO = new JLabel();
  private JTextField textFieldOutputO = new JTextField();
  private JLabel labelOutputO = new JLabel();
  private JButton buttonAddO = new JButton();
  private JButton buttonQuitO = new JButton();
  private JComboBox comboBoxHeadO = new JComboBox();
  private JPanel PanelFilesO = new JPanel();
  private TitledBorder titleBorderO;
  private JButton buttonSelecInputO = new JButton();
  private JButton buttonSelecOutputO = new JButton();


  //dialog buttons
  private JButton botonAccept = new JButton();
  private JButton botonCancel = new JButton();


  /**
   * Contains the dialog strings for the languaje selected
   */
  private ResourceBundle dialogStrings;

  /*------------------------------------------------------------------*/
  /**
   * Constructor for the main knowledge contraints dialog
   * @param parent the JDialog from this dialog is invoked
   * @param net the NetworkFrame with the problem where the constraints will be applied
   * @param ckpanel ConstraintKnowledgePanel where the constraints will be available
  */
  public ConstraintKnowledgeDialog( JDialog parent, NetworkFrame net) {
    //We build the windows where we have our dialog
    super(parent);
    pack();
    //put the title of the window
    setTitle("Constraint Knowledge");
    //resize the window
    setSize(657, 400);
    setResizable(false);
    setVisible(false);
    setModal(true);
    setLocationRelativeTo(parent);

    //We build the main panel and add to the jdialog
    mainpanel.setDoubleBuffered(true);
    mainpanel.setLayout(null);
    mainpanel.setEnabled(true);
    setContentPane(mainpanel);

    //Store the ConstraintKnowledgePanelobject
    this.constraintKnowledgePanel=net.getConstraintKnowledgePanel();


    //Store the NetworkFrame object
    this.networkframe = net;


    //Store the bnet object
    //this.bnet = bnet;
    this.bnet = net.getEditorPanel().getBayesNet();

    //We see if the contraints were edited before
    this.ck= net.getConstraintKnowledge();

    //If the constraints doesn't were edited before, we build a new ContraintKnowledge object
    if (this.ck == null)
      this.ck = new ConstraintKnowledge(bnet);

    try {
      //Build the GUI
      jbInit();

      //Init the comboxes
      comboBoxInitE(this.bnet.getNodeList());
      comboBoxInitA(this.bnet.getNodeList());
      comboBoxInitO(this.bnet.getNodeList());

      //Init the list with the existence constraints
      listInitE();
      //Init the list with the absence constraints
      listInitA();
     //Init the list with the partial order constraints
      listInitO();

    }
    catch (Exception e) {
      e.printStackTrace();
    }
  } //End ctor

  /*------------------------------------------------------------------*/
  /**
   * Init the existence List with the contraints
   */
  private void listInitE() throws Exception {
    Vector vector =this.ck.getExistenceConstraints().getLinkList().getLinks();
    this.listSelectedE.setListData(vector);
  }//end listInitE method

  /*------------------------------------------------------------------*/
  /**
   * Init the existence head and tail nodes ComboBoxes
   * @param nodes NodeList with the nodes to use in the ComboBoxes
   */
  private void comboBoxInitE(NodeList nodes) throws Exception {
    String nodeNames[] = new String[nodes.size()];
    for (int i = 0; i < nodes.size(); i++) {
      this.comboBoxTailE.addItem(nodes.elementAt(i).getName());
      this.comboBoxHeadE.addItem(nodes.elementAt(i).getName());
    }
  } //end comboBoxInitE method

  /*------------------------------------------------------------------*/
  /**
   * Init the absence List with the contraints
   */
  private void listInitA() throws Exception {
    Vector vector =this.ck.getAbsenceConstraints().getLinkList().getLinks();
    this.listSelectedA.setListData(vector);
  }//end listInitE method

  /*------------------------------------------------------------------*/
  /**
   * Init the absence head and tail nodes ComboBoxes
   * @param nodes NodeList with the nodes to use in the ComboBoxes
   */
  private void comboBoxInitA(NodeList nodes) throws Exception {
    String nodeNames[] = new String[nodes.size()];
    for (int i = 0; i < nodes.size(); i++) {
      this.comboBoxTailA.addItem(nodes.elementAt(i).getName());
      this.comboBoxHeadA.addItem(nodes.elementAt(i).getName());
    }
  } //end comboBoxInitA method

  /*------------------------------------------------------------------*/
  /**
   * Init the partial order List with the contraints
   */
  private void listInitO() throws Exception {
    Vector vector =this.ck.getPartialOrderConstraints().getLinkList().getLinks();
    this.listSelectedO.setListData(vector);
  }//end listInitO method

  /*------------------------------------------------------------------*/
  /**
   * Init the partial order head and tail nodes ComboBoxes
   * @param nodes NodeList with the nodes to use in the ComboBoxes
   */
  private void comboBoxInitO(NodeList nodes) throws Exception {
    String nodeNames[] = new String[nodes.size()];
    for (int i = 0; i < nodes.size(); i++) {
      this.comboBoxTailO.addItem(nodes.elementAt(i).getName());
      this.comboBoxHeadO.addItem(nodes.elementAt(i).getName());
    }
  } //end comboBoxInitO method

  /*------------------------------------------------------------------*/
  /**
   * Initialices all the dialog components
   */
  private void jbInit() throws Exception {

    //Get the localized Strings for the dialog
    dialogStrings = Elvira.getElviraFrame().getDialogBundle();

    //Build the TabbedPane where we put the three tabs
    jTabbedPane.setDoubleBuffered(true);
    jTabbedPane.setBounds(new Rectangle(5, 0, 639, 332));

    //Configure the tab's layout
    tabExistence.setLayout(null);
    tabAbsence.setLayout(null);
    tabPartialOrder.setLayout(null);

    //Buil the dialog buttons
    botonAccept.setBounds(new Rectangle(211, 341, 109, 29));
    botonAccept.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelAccept"));
    botonAccept.addActionListener(new ConstraintKnowledgeDialog_botonAccept_actionAdapter(this));
    botonCancel.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelCancel"));
    botonCancel.addActionListener(new ConstraintKnowledgeDialog_botonCancel_actionAdapter(this));
    botonCancel.setBounds(new Rectangle(331, 341, 109, 29));

    //Build The components for the existence tab
    labelTitleE.setEnabled(true);
    labelTitleE.setDebugGraphicsOptions(0);
    labelTitleE.setDoubleBuffered(true);
    labelTitleE.setDisplayedMnemonic('0');
    labelTitleE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.Existence.labelTitle"));
    labelTitleE.setVerticalAlignment(SwingConstants.CENTER);
    labelTitleE.setVerticalTextPosition(SwingConstants.CENTER);
    labelTitleE.setBounds(new Rectangle(216, 4, 200, 21));
    comboBoxTailE.setAutoscrolls(true);
    comboBoxTailE.setDoubleBuffered(true);
    comboBoxTailE.setActionCommand("comboBoxChanged");
    comboBoxTailE.setBounds(new Rectangle(6, 56, 117, 24));

    labelTailE.setDoubleBuffered(true);
    labelTailE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelTail"));
    labelTailE.setBounds(new Rectangle(5, 37, 84, 15));
    labelHeadE.setBounds(new Rectangle(128, 39, 102, 15));
    labelHeadE.setDoubleBuffered(true);
    labelHeadE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelHead"));
    listSelectedE.setBounds(new Rectangle(396, 43, 233, 246));
    labelSelectedE.setDoubleBuffered(true);
    labelSelectedE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelSelected"));
    labelSelectedE.setBounds(new Rectangle(396, 20, 205, 25));
    radioButtonDirectedE.setDoubleBuffered(true);
    radioButtonDirectedE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelDirected"));
    radioButtonDirectedE.setBounds(new Rectangle(87, 95, 81, 23));
    textFieldInputE.setBackground(Color.white);
    textFieldInputE.setForeground(Color.gray);
    textFieldInputE.setDoubleBuffered(true);
    textFieldInputE.setDisabledTextColor(Color.black);
    textFieldInputE.setEditable(false);
    textFieldInputE.setText("[none]");
    textFieldInputE.setBounds(new Rectangle(21, 40, 235, 29));
    labelInputE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelInput"));
    labelInputE.setBounds(new Rectangle(22, 18, 122, 24));
    textFieldOutputE.setBackground(Color.white);
    textFieldOutputE.setForeground(Color.gray);
    textFieldOutputE.setDoubleBuffered(true);
    textFieldOutputE.setEditable(false);
    textFieldOutputE.setText("[none]");
    textFieldOutputE.setBounds(new Rectangle(20, 102, 235, 29));
    labelOutputE.setBounds(new Rectangle(19, 79, 122, 24));
    labelOutputE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelOutput"));
    buttonAddE.setBounds(new Rectangle(271, 54, 119, 28));
    buttonAddE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelAdd"));
    buttonAddE.addActionListener(new ConstraintKnowledgeDialog_buttonAddE_actionAdapter(this));
    buttonQuitE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelQuit"));
    buttonQuitE.addActionListener(new ConstraintKnowledgeDialog_buttonQuitE_actionAdapter(this));
    buttonQuitE.setBounds(new Rectangle(272, 90, 117, 28));
    comboBoxHeadE.setDoubleBuffered(true);
    comboBoxHeadE.setEditable(false);
    comboBoxHeadE.setBounds(new Rectangle(130, 56, 117, 24));

    buttonSelecOutputE.addActionListener(new ConstraintKnowledgeDialog_buttonSelecOutputE_actionAdapter(this));
    buttonSelecInputE.addActionListener(new ConstraintKnowledgeDialog_buttonSelecInputE_actionAdapter(this));

    titleBorderE = new TitledBorder(BorderFactory.createLineBorder(new Color(153, 153, 153), 2), localize(dialogStrings,"ConstraintKnowledgeDialog.titleBorder"));
    PanelFilesE.setBorder(titleBorderE);
    PanelFilesE.setDebugGraphicsOptions(0);
    PanelFilesE.setBounds(new Rectangle(9, 152, 381, 138));
    PanelFilesE.setLayout(null);
    buttonSelecInputE.setBounds(new Rectangle(262, 43, 110, 26));
    buttonSelecInputE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.buttonSelecInput"));
    buttonSelecInputE.setDoubleBuffered(true);
    buttonSelecOutputE.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.buttonSelecOutput"));
    buttonSelecOutputE.setBounds(new Rectangle(263, 100, 110, 26));
    buttonSelecOutputE.setDoubleBuffered(true);

    PanelFilesE.add(textFieldInputE, null);
    PanelFilesE.add(textFieldOutputE, null);
    PanelFilesE.add(labelOutputE, null);
    PanelFilesE.add(labelInputE, null);
    PanelFilesE.add(buttonSelecInputE, null);
    PanelFilesE.add(buttonSelecOutputE, null);


    //Build The components for the absence tab
    labelTitleA.setEnabled(true);
    labelTitleA.setDebugGraphicsOptions(0);
    labelTitleA.setDoubleBuffered(true);
    labelTitleA.setDisplayedMnemonic('0');
    labelTitleA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.Absence.labelTitle"));
    labelTitleA.setVerticalAlignment(SwingConstants.CENTER);
    labelTitleA.setVerticalTextPosition(SwingConstants.CENTER);
    labelTitleA.setBounds(new Rectangle(216, 4, 200, 21));
    comboBoxTailA.setAutoscrolls(true);
    comboBoxTailA.setDoubleBuffered(true);
    comboBoxTailA.setActionCommand("comboBoxChanged");
    comboBoxTailA.setBounds(new Rectangle(6, 56, 117, 24));

    labelTailA.setDoubleBuffered(true);
    labelTailA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelTail"));
    labelTailA.setBounds(new Rectangle(5, 37, 84, 15));
    labelHeadA.setBounds(new Rectangle(128, 39, 102, 15));
    labelHeadA.setDoubleBuffered(true);
    labelHeadA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelHead"));
    listSelectedA.setBounds(new Rectangle(396, 43, 233, 246));
    labelSelectedA.setDoubleBuffered(true);
    labelSelectedA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelSelected"));
    labelSelectedA.setBounds(new Rectangle(396, 20, 205, 25));
    radioButtonDirectedA.setDoubleBuffered(true);
    radioButtonDirectedA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelDirected"));
    radioButtonDirectedA.setBounds(new Rectangle(87, 95, 81, 23));
    textFieldInputA.setBackground(Color.white);
    textFieldInputA.setForeground(Color.gray);
    textFieldInputA.setDoubleBuffered(true);
    textFieldInputA.setDisabledTextColor(Color.black);
    textFieldInputA.setEditable(false);
    textFieldInputA.setText("[none]");
    textFieldInputA.setBounds(new Rectangle(21, 40, 235, 29));
    labelInputA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelInput"));
    labelInputA.setBounds(new Rectangle(22, 18, 122, 24));
    textFieldOutputA.setBackground(Color.white);
    textFieldOutputA.setForeground(Color.gray);
    textFieldOutputA.setDoubleBuffered(true);
    textFieldOutputA.setEditable(false);
    textFieldOutputA.setText("[none]");
    textFieldOutputA.setBounds(new Rectangle(20, 102, 235, 29));
    labelOutputA.setBounds(new Rectangle(19, 79, 122, 24));
    labelOutputA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelOutput"));
    buttonAddA.setBounds(new Rectangle(271, 54, 119, 28));
    buttonAddA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelAdd"));
    buttonAddA.addActionListener(new ConstraintKnowledgeDialog_buttonAddA_actionAdapter(this));
    buttonQuitA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelQuit"));
    buttonQuitA.addActionListener(new ConstraintKnowledgeDialog_buttonQuitA_actionAdapter(this));
    buttonQuitA.setBounds(new Rectangle(272, 90, 117, 28));
    comboBoxHeadA.setDoubleBuffered(true);
    comboBoxHeadA.setEditable(false);
    comboBoxHeadA.setBounds(new Rectangle(130, 56, 117, 24));

    buttonSelecOutputA.addActionListener(new ConstraintKnowledgeDialog_buttonSelecOutputA_actionAdapter(this));
    buttonSelecInputA.addActionListener(new ConstraintKnowledgeDialog_buttonSelecInputA_actionAdapter(this));

    titleBorderA = new TitledBorder(BorderFactory.createLineBorder(new Color(153, 153, 153), 2), localize(dialogStrings,"ConstraintKnowledgeDialog.titleBorder"));
    PanelFilesA.setBorder(titleBorderA);
    PanelFilesA.setDebugGraphicsOptions(0);
    PanelFilesA.setBounds(new Rectangle(9, 152, 381, 138));
    PanelFilesA.setLayout(null);
    buttonSelecInputA.setBounds(new Rectangle(262, 43, 110, 26));
    buttonSelecInputA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.buttonSelecInput"));
    buttonSelecInputA.setDoubleBuffered(true);
    buttonSelecOutputA.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.buttonSelecOutput"));
    buttonSelecOutputA.setBounds(new Rectangle(263, 100, 110, 26));
    buttonSelecOutputA.setDoubleBuffered(true);

    PanelFilesA.add(textFieldInputA, null);
    PanelFilesA.add(textFieldOutputA, null);
    PanelFilesA.add(labelOutputA, null);
    PanelFilesA.add(labelInputA, null);
    PanelFilesA.add(buttonSelecInputA, null);
    PanelFilesA.add(buttonSelecOutputA, null);



    //Build The components for the partial order tab
    labelTitleO.setEnabled(true);
    labelTitleO.setDebugGraphicsOptions(0);
    labelTitleO.setDoubleBuffered(true);
    labelTitleO.setDisplayedMnemonic('0');
    labelTitleO.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.PartialOrder.labelTitle"));
    labelTitleO.setVerticalAlignment(SwingConstants.CENTER);
    labelTitleO.setVerticalTextPosition(SwingConstants.CENTER);
    labelTitleO.setBounds(new Rectangle(216, 4, 300, 21));
    comboBoxTailO.setAutoscrolls(true);
    comboBoxTailO.setDoubleBuffered(true);
    comboBoxTailO.setActionCommand("comboBoxChanged");
    comboBoxTailO.setBounds(new Rectangle(6, 56, 117, 24));

    labelTailO.setDoubleBuffered(true);
    labelTailO.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelTail"));
    labelTailO.setBounds(new Rectangle(5, 37, 84, 15));
    labelHeadO.setBounds(new Rectangle(128, 39, 102, 15));
    labelHeadO.setDoubleBuffered(true);
    labelHeadO.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelHead"));
    listSelectedO.setBounds(new Rectangle(396, 43, 233, 246));
    labelSelectedO.setDoubleBuffered(true);
    labelSelectedO.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.PartialOrder.labelSelected"));
    labelSelectedO.setBounds(new Rectangle(396, 20, 205, 25));
    textFieldInputO.setBackground(Color.white);
    textFieldInputO.setForeground(Color.gray);
    textFieldInputO.setDoubleBuffered(true);
    textFieldInputO.setDisabledTextColor(Color.black);
    textFieldInputO.setEditable(false);
    textFieldInputO.setText("[none]");
    textFieldInputO.setBounds(new Rectangle(21, 40, 235, 29));
    labelInputO.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelInput"));
    labelInputO.setBounds(new Rectangle(22, 18, 122, 24));
    textFieldOutputO.setBackground(Color.white);
    textFieldOutputO.setForeground(Color.gray);
    textFieldOutputO.setDoubleBuffered(true);
    textFieldOutputO.setEditable(false);
    textFieldOutputO.setText("[none]");
    textFieldOutputO.setBounds(new Rectangle(20, 102, 235, 29));
    labelOutputO.setBounds(new Rectangle(19, 79, 122, 24));
    labelOutputO.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelOutput"));
    buttonAddO.setBounds(new Rectangle(271, 54, 119, 28));
    buttonAddO.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelAdd"));
    buttonAddO.addActionListener(new ConstraintKnowledgeDialog_buttonAddO_actionAdapter(this));
    buttonQuitO.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.labelQuit"));
    buttonQuitO.addActionListener(new ConstraintKnowledgeDialog_buttonQuitO_actionAdapter(this));
    buttonQuitO.setBounds(new Rectangle(272, 90, 117, 28));
    comboBoxHeadO.setDoubleBuffered(true);
    comboBoxHeadO.setEditable(false);
    comboBoxHeadO.setBounds(new Rectangle(130, 56, 117, 24));

    buttonSelecOutputO.addActionListener(new ConstraintKnowledgeDialog_buttonSelecOutputO_actionAdapter(this));
    buttonSelecInputO.addActionListener(new ConstraintKnowledgeDialog_buttonSelecInputO_actionAdapter(this));

    titleBorderO = new TitledBorder(BorderFactory.createLineBorder(new Color(153, 153, 153), 2), localize(dialogStrings,"ConstraintKnowledgeDialog.titleBorder"));
    PanelFilesO.setBorder(titleBorderO);
    PanelFilesO.setDebugGraphicsOptions(0);
    PanelFilesO.setBounds(new Rectangle(9, 152, 381, 138));
    PanelFilesO.setLayout(null);
    buttonSelecInputO.setBounds(new Rectangle(262, 43, 110, 26));
    buttonSelecInputO.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.buttonSelecInput"));
    buttonSelecInputO.setDoubleBuffered(true);
    buttonSelecOutputO.setText(localize(dialogStrings,"ConstraintKnowledgeDialog.buttonSelecOutput"));
    buttonSelecOutputO.setBounds(new Rectangle(263, 100, 110, 26));
    buttonSelecOutputO.setDoubleBuffered(true);

    PanelFilesO.add(textFieldInputO, null);
    PanelFilesO.add(textFieldOutputO, null);
    PanelFilesO.add(labelOutputO, null);
    PanelFilesO.add(labelInputO, null);
    PanelFilesO.add(buttonSelecInputO, null);
    PanelFilesO.add(buttonSelecOutputO, null);



    //Add the components to the existence tab
    tabExistence.add(labelTitleE, null);
    tabExistence.add(labelSelectedE, null);
    tabExistence.add(listSelectedE, null);
    tabExistence.add(buttonAddE, null);
    tabExistence.add(comboBoxTailE, null);
    tabExistence.add(labelTailE, null);
    tabExistence.add(labelHeadE, null);
    tabExistence.add(comboBoxHeadE, null);
    tabExistence.add(radioButtonDirectedE, null);
    tabExistence.add(buttonQuitE, null);
    tabExistence.add(PanelFilesE, null);


    //Add the components to the absence tab
    tabAbsence.add(labelTitleA, null);
    tabAbsence.add(labelSelectedA, null);
    tabAbsence.add(listSelectedA, null);
    tabAbsence.add(buttonAddA, null);
    tabAbsence.add(comboBoxTailA, null);
    tabAbsence.add(labelTailA, null);
    tabAbsence.add(labelHeadA, null);
    tabAbsence.add(comboBoxHeadA, null);
    tabAbsence.add(radioButtonDirectedA, null);
    tabAbsence.add(buttonQuitA, null);
    tabAbsence.add(PanelFilesA, null);


    //Add the components to the partial order tab
    tabPartialOrder.add(labelTitleO, null);
    tabPartialOrder.add(labelSelectedO, null);
    tabPartialOrder.add(listSelectedO, null);
    tabPartialOrder.add(buttonAddO, null);
    tabPartialOrder.add(comboBoxTailO, null);
    tabPartialOrder.add(labelTailO, null);
    tabPartialOrder.add(labelHeadO, null);
    tabPartialOrder.add(comboBoxHeadO, null);
    tabPartialOrder.add(buttonQuitO, null);
    tabPartialOrder.add(PanelFilesO, null);




    //Add to the main panel the three tabs
    jTabbedPane.add(tabExistence, localize(dialogStrings,"ConstraintKnowledgeDialog.Existence.tab"));
    jTabbedPane.add(tabAbsence, localize(dialogStrings,"ConstraintKnowledgeDialog.Absence.tab"));
    jTabbedPane.add(tabPartialOrder, localize(dialogStrings,"ConstraintKnowledgeDialog.PartialOrder.tab"));
//    this.setContentPane(jTabbedPane1);


    //Add the OK and Cancel Button to the main panel
    this.getContentPane().add(jTabbedPane, null);
    this.getContentPane().add(botonCancel, null);
    this.getContentPane().add(botonAccept, null);
  }

  /*###############################################################
                Methods to manage events
    ##############################################################*/
  /*------------------------------------------------------------------*/
  /**
   * This method is called when the Cancel button is pressed. Exit
   * with no contraints
   * @param e the action event
   */
  void botonCancel_actionPerformed(ActionEvent e) {
    dispose();
  }

  /*------------------------------------------------------------------*/
  /**
  * This method is called when the OK button is pressed. Store
  * the contraints in the ConstraintKnowledgePanel and, then, in the NetworkFrame
  * @param e the action event
  */
  void botonAccept_actionPerformed(ActionEvent e) {
    //Store the contraints in the ConstraintKnowledgePanel
    constraintKnowledgePanel.setConstraints(this.ck);

    //Store the contraints in the NetworkFrame
    networkframe.activeConstraintKnowledgePanel();

    //exit
    dispose();
  }

  /*------------------------------------------------------------------*/
  /**
   * This method is called in Existence tab when the Add button is pressed. Store
   * the existence contraint.
   * @param e the action event
   */
  void buttonAddE_actionPerformed(ActionEvent e) {
    //Get the tail node
    String tail = (String)this.comboBoxTailE.getSelectedItem();

    //Get the head node
    String head = (String)this.comboBoxHeadE.getSelectedItem();

    //Los if the contraint is directed
    boolean directed = this.radioButtonDirectedE.isSelected();

    //Add (if we can) to the existence contraints
    //-Build the new Link
    Node tailnode = bnet.getNodeList().getNode(tail);
    Node headnode = bnet.getNodeList().getNode(head);
    Link newlink = new Link(tailnode, headnode, directed);

    //-Try to add to existence constraints
    boolean fail = false;
    try {
      if (this.ck.addConstraint(ck.EXISTENCE, newlink)) {
        //It was added, we copy it to the GUI existence List
        //Cet the elements form the existence List
        ListModel listmodel = this.listSelectedE.getModel();
        Vector vector = new Vector();
        for (int i = 0; i < listmodel.getSize(); i++) {
          vector.add(listmodel.getElementAt(i));
        }
        //add the new element
        vector.add(newlink);
        //update the List
        this.listSelectedE.setListData(vector);
      }
      else {
        fail = true;
      }
    }
    catch (InvalidEditException ev) {
      fail = true;
    }

    //if we can't add to the contraints, we show this error.
    if (fail)
      JOptionPane.showMessageDialog(this, localize(dialogStrings,"ConstraintKnowledgeDialog.ErrorAdd.msg")+ newlink.toString() +localize(dialogStrings,"ConstraintKnowledgeDialog.ErrorAdd.consistent"), "Error", JOptionPane.ERROR_MESSAGE);

  } //End buttonAddE_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * This method is called in Existence tab when the Quit button is pressed. Remove
   * the selected contraint/s.
   * @param e the action event
   */
  void buttonQuitE_actionPerformed(ActionEvent e) {
    int i, j;

    //Get the selected contraints
    int selected[] = this.listSelectedE.getSelectedIndices();
    if (selected.length == 0) {
      return;
    }

    //Get all the List contraints, in two vectors: selected and noselected
    ListModel listmodel = this.listSelectedE.getModel();
    Vector selectedvector = new Vector();
    Vector noselectedvector = new Vector();
    Vector allvector = new Vector();
    for (i = 0, j = 0; i < listmodel.getSize(); i++) {
      Link link = (Link) listmodel.getElementAt(i);
      while ( (j < selected.length - 1) && (i > selected[j])) {
        j++;
      }
      if (i == selected[j]) {
        selectedvector.add(link);
      }
      else {
        noselectedvector.add(link);
      }
      allvector.add(link);
    }

    //Ask to remove or not
    String question = localize(dialogStrings,"ConstraintKnowledgeDialog.quitAction1");

    for (i = 0; i < selectedvector.size(); i++) {
      question += " " + selectedvector.elementAt(i).toString();
    }
    question += localize(dialogStrings,"ConstraintKnowledgeDialog.Existence.quitAction2");

    int deleteselected = JOptionPane.showConfirmDialog(this, question, "Error",
        JOptionPane.YES_NO_OPTION);

    //If delete is selected, remove the contraints
    if (deleteselected == JOptionPane.YES_OPTION) {
      //first, remove them from the ContraintKnowledge object
      for (i = 0; i < selectedvector.size(); i++) {
        boolean fail = false;
        try {
          if (!this.ck.removeConstraint(ck.EXISTENCE, (Link) selectedvector.elementAt(i))) {
            fail = true;
          }
        }
        catch (InvalidEditException ev) {
          fail = true;
        }
        if (fail) {
          JOptionPane.showMessageDialog(this,
              localize(dialogStrings,"ConstraintKnowledgeDialog.Existence.ErrorQuit.msg") +
              selectedvector.elementAt(i).toString(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
      //Second, remove them from gui List
      this.listSelectedE.setListData(noselectedvector);
    }
    else {
      this.listSelectedE.setListData(allvector);
    }
  } //end buttonQuitE_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * In this method, we use a FileChooser to save the existence contraints
   * from a file
   * @param e the action event
   */
  void buttonSelecOutputE_actionPerformed(ActionEvent e) {
    //We use a ElviraFileChooser to choose the file to save the existence contraints
    ElviraFileChooser filechooser = new ElviraFileChooser(System.getProperty("user.dir"));
    filechooser.rescanCurrentDirectory();
    filechooser.setDialogType(filechooser.SAVE_DIALOG);
    filechooser.setElviraFilter();

    int returnval = filechooser.showSaveDialog(this);

    //if a file is selected, we get it
    if (returnval == JFileChooser.APPROVE_OPTION) {
      File file = filechooser.getSelectedFile();
      //Update the gui
      this.textFieldOutputE.setText(file.getPath());

      //Write the file
      try {
        FileWriter fe = new FileWriter(file.getPath());
        this.ck.saveExistenceConstraints(fe);
        fe.close();
      } catch(java.io.IOException ex) {
        JOptionPane.showMessageDialog(this, localize(dialogStrings,"ConstraintKnowledgeDialog.Existence.ErrorSave.msg"),
                                  "Error", JOptionPane.ERROR_MESSAGE);
      }
    }else this.textFieldOutputE.setText("[none]");

  } // end buttonSelecOutputE_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * In this method, we use a FileChooser to read the existence contraints
   * from a file
   * @param e the action event
   */
  void buttonSelecInputE_actionPerformed(ActionEvent e) {

    //We use a ElviraFileChooser to choose the file
    ElviraFileChooser filechooser = new ElviraFileChooser(System.getProperty("user.dir"));
    filechooser.rescanCurrentDirectory();
    filechooser.setDialogType(filechooser.OPEN_DIALOG);
    filechooser.setElviraFilter();
    int returnval = filechooser.showOpenDialog(this);

    //if a file is selected, we read it
    if (returnval == JFileChooser.APPROVE_OPTION) {
      File file = filechooser.getSelectedFile();
      //update the GUI
      this.textFieldInputE.setText(file.getPath());

      //Store the existence contraints int the selected file
      int fail=0;
      try {
        fail = this.ck.loadExistenceConstraints(file.getPath());
      } catch (ParseException ev ) {fail=-1;}
      catch (InvalidEditException eve ) {fail=-1;}
      catch (IOException even ) {fail=-2;}

      //If there is a error, we show it
      if (fail<-1) JOptionPane.showMessageDialog(this,localize(dialogStrings,"ConstraintKnowledgeDialog.Existence.ErrorRead.msg1"), "Error",JOptionPane.ERROR_MESSAGE);
      else if (fail<0) JOptionPane.showMessageDialog(this,localize(dialogStrings,"ConstraintKnowledgeDialog.Existence.ErrorRead.msg2"), "Error",JOptionPane.ERROR_MESSAGE);
      else if (fail==0) JOptionPane.showMessageDialog(this,localize(dialogStrings,"ConstraintKnowledgeDialog.ErrorRead.msg3"), "Info",JOptionPane.INFORMATION_MESSAGE);
      else
        //If there is no error, we add the constraint tihe gui List
        this.listSelectedE.setListData(this.ck.getExistenceConstraints().getLinkList().getLinks());

      if (fail<=0)
        this.textFieldInputE.setText("[none]");

    } else this.textFieldInputE.setText("[none]");
  } //end buttonSelecInputE_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * This method is called in Absence tab when the Add button is pressed. Store
   * the absence contraint.
   * @param e the action event
   */
  void buttonAddA_actionPerformed(ActionEvent e) {
    //Get the tail node
    String tail = (String)this.comboBoxTailA.getSelectedItem();

    //Get the head node
    String head = (String)this.comboBoxHeadA.getSelectedItem();

    //Los if the contraint is directedt
    boolean directed = this.radioButtonDirectedA.isSelected();

    //Add (if we can) to the absence contraints
    //-Build the new Link
    Node tailnode = bnet.getNodeList().getNode(tail);
    Node headnode = bnet.getNodeList().getNode(head);
    Link newlink = new Link(tailnode, headnode, directed);

    //-Try to add to absence constraints
    boolean fail = false;
    try {
      if (this.ck.addConstraint(ck.ABSENCE, newlink)) {
        //It was added, we copy it to the GUI absence List
        //Cet the elements form the absence List
        ListModel listmodel = this.listSelectedA.getModel();
        Vector vector = new Vector();
        for (int i = 0; i < listmodel.getSize(); i++) {
          vector.add(listmodel.getElementAt(i));
        }
        //add the new element
        vector.add(newlink);
        //update the List
        this.listSelectedA.setListData(vector);
      }
      else {
        fail = true;
      }
    }
    catch (InvalidEditException ev) {
      fail = true;
    }

    //if we can't add to the contraints, we show this error.
    if (fail)
      JOptionPane.showMessageDialog(this, localize(dialogStrings,"ConstraintKnowledgeDialog.ErrorAdd.msg")+ newlink.toString() +localize(dialogStrings,"ConstraintKnowledgeDialog.ErrorAdd.consistent"), "Error", JOptionPane.ERROR_MESSAGE);

  } //End buttonAddA_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * This method is called in Absence tab when the Quit button is pressed. Remove
   * the selected contraint/s.
   * @param e the action event
   */
  void buttonQuitA_actionPerformed(ActionEvent e) {
    int i, j;

    //Get the selected contraints
    int selected[] = this.listSelectedA.getSelectedIndices();
    if (selected.length == 0) {
      return;
    }

    //Get all the List contraints, in two vectors: selected and noselected
    ListModel listmodel = this.listSelectedA.getModel();
    Vector selectedvector = new Vector();
    Vector noselectedvector = new Vector();
    Vector allvector = new Vector();
    for (i = 0, j = 0; i < listmodel.getSize(); i++) {
      Link link = (Link) listmodel.getElementAt(i);
      while ( (j < selected.length - 1) && (i > selected[j])) {
        j++;
      }
      if (i == selected[j]) {
        selectedvector.add(link);
      }
      else {
        noselectedvector.add(link);
      }
      allvector.add(link);
    }

    //Ask to remove or not
    String question = localize(dialogStrings,"ConstraintKnowledgeDialog.quitAction1");

    for (i = 0; i < selectedvector.size(); i++) {
      question += " " + selectedvector.elementAt(i).toString();
    }
    question += localize(dialogStrings,"ConstraintKnowledgeDialog.Absence.quitAction2");

    int deleteselected = JOptionPane.showConfirmDialog(this, question, "Error",
        JOptionPane.YES_NO_OPTION);

    //If delete is selected, remove the contraints
    if (deleteselected == JOptionPane.YES_OPTION) {
      //first, remove them from the ContraintKnowledge object
      for (i = 0; i < selectedvector.size(); i++) {
        boolean fail = false;
        try {
          if (!this.ck.removeConstraint(ck.ABSENCE, (Link) selectedvector.elementAt(i))) {
            fail = true;
          }
        }
        catch (InvalidEditException ev) {
          fail = true;
        }
        if (fail) {
          JOptionPane.showMessageDialog(this,
              localize(dialogStrings,"ConstraintKnowledgeDialog.Absence.ErrorQuit.msg") +
              selectedvector.elementAt(i).toString(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
      //Second, remove them from gui List
      this.listSelectedA.setListData(noselectedvector);
    }
    else {
      this.listSelectedA.setListData(allvector);
    }
  } //end buttonQuitA_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * In this method, we use a FileChooser to save the absence contraints
   * from a file
   * @param e the action event
   */
  void buttonSelecOutputA_actionPerformed(ActionEvent e) {
    //We use a ElviraFileChooser to choose the file to save the absence contraints
    ElviraFileChooser filechooser = new ElviraFileChooser(System.getProperty("user.dir"));
    filechooser.rescanCurrentDirectory();
    filechooser.setDialogType(filechooser.SAVE_DIALOG);
    filechooser.setElviraFilter();

    int returnval = filechooser.showSaveDialog(this);

    //if a file is selected, we get it
    if (returnval == JFileChooser.APPROVE_OPTION) {
      File file = filechooser.getSelectedFile();
      //Update the gui
      this.textFieldOutputA.setText(file.getPath());

      //Write the file
      try {
        FileWriter fe = new FileWriter(file.getPath());
        this.ck.saveAbsenceConstraints(fe);
        fe.close();
      } catch(java.io.IOException ex) {
        JOptionPane.showMessageDialog(this, localize(dialogStrings,"ConstraintKnowledgeDialog.Absence.ErrorSave.msg"),
                                  "Error", JOptionPane.ERROR_MESSAGE);
      }
    }else this.textFieldOutputA.setText("[none]");

  } // end buttonSelecOutputA_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * In this method, we use a FileChooser to read the absence contraints
   * from a file
   * @param e the action event
   */
  void buttonSelecInputA_actionPerformed(ActionEvent e) {

    //We use a ElviraFileChooser to choose the file
    ElviraFileChooser filechooser = new ElviraFileChooser(System.getProperty("user.dir"));
    filechooser.rescanCurrentDirectory();
    filechooser.setDialogType(filechooser.OPEN_DIALOG);
    filechooser.setElviraFilter();
    int returnval = filechooser.showOpenDialog(this);

    //if a file is selected, we read it
    if (returnval == JFileChooser.APPROVE_OPTION) {
      File file = filechooser.getSelectedFile();
      //update the GUI
      this.textFieldInputA.setText(file.getPath());

      //Store the absence contraints int the slected file
      int fail=0;
      try {
        fail = this.ck.loadAbsenceConstraints(file.getPath());
      } catch (ParseException ev ) {fail=-1;}
      catch (InvalidEditException eve ) {fail=-1;}
      catch (IOException even ) {fail=-2;}

      //If there is a error, we show it
      if (fail<-1) JOptionPane.showMessageDialog(this,localize(dialogStrings,"ConstraintKnowledgeDialog.Absence.ErrorRead.msg1"), "Error",JOptionPane.ERROR_MESSAGE);
      else if (fail<0) JOptionPane.showMessageDialog(this,localize(dialogStrings,"ConstraintKnowledgeDialog.Absence.ErrorRead.msg2"), "Error",JOptionPane.ERROR_MESSAGE);
      else if (fail==0) JOptionPane.showMessageDialog(this,localize(dialogStrings,"ConstraintKnowledgeDialog.ErrorRead.msg3"), "Info",JOptionPane.INFORMATION_MESSAGE);
      else
        //If there is no error, we add the constraint tihe gui List
        this.listSelectedA.setListData(this.ck.getAbsenceConstraints().getLinkList().getLinks());

      if (fail<=0)
        this.textFieldInputA.setText("[none]");

    } else this.textFieldInputA.setText("[none]");
  } //end buttonSelecInputA_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * This method is called in PartialOrder tab when the Add button is pressed. Store
   * the order contraint.
   * @param e the action event
   */
  void buttonAddO_actionPerformed(ActionEvent e) {
    //Get the tail node
    String tail = (String)this.comboBoxTailO.getSelectedItem();

    //Get the head node
    String head = (String)this.comboBoxHeadO.getSelectedItem();

    //Add (if we can) to the order contraints
    //-Build the new Link
    Node tailnode = bnet.getNodeList().getNode(tail);
    Node headnode = bnet.getNodeList().getNode(head);
    Link newlink = new Link(tailnode, headnode, true);

    //-Try to add to order constraints
    boolean fail = false;
    try {
      if (this.ck.addConstraint(ck.PARTIALORDER, newlink)) {
        //It was added, we copy it to the GUI order List
        //Cet the elements form the order List
        ListModel listmodel = this.listSelectedO.getModel();
        Vector vector = new Vector();
        for (int i = 0; i < listmodel.getSize(); i++) {
          vector.add(listmodel.getElementAt(i));
        }
        //add the new element
        vector.add(newlink);
        //update the List
        this.listSelectedO.setListData(vector);
      }
      else {
        fail = true;
      }
    }
    catch (InvalidEditException ev) {
      fail = true;
    }

    //if we can't add to the contraints, we show this error.
    if (fail)
      JOptionPane.showMessageDialog(this, localize(dialogStrings,"ConstraintKnowledgeDialog.ErrorAdd.msg")+ newlink.toString() +localize(dialogStrings,"ConstraintKnowledgeDialog.ErrorAdd.consistent"), "Error", JOptionPane.ERROR_MESSAGE);

  } //End buttonAddO_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * This method is called in PartialOrder tab when the Quit button is pressed. Remove
   * the selected contraint/s.
   * @param e the action event
   */
  void buttonQuitO_actionPerformed(ActionEvent e) {
    int i, j;

    //Get the selected contraints
    int selected[] = this.listSelectedO.getSelectedIndices();
    if (selected.length == 0) {
      return;
    }

    //Get all the List contraints, in two vectors: selected and noselected
    ListModel listmodel = this.listSelectedO.getModel();
    Vector selectedvector = new Vector();
    Vector noselectedvector = new Vector();
    Vector allvector = new Vector();
    for (i = 0, j = 0; i < listmodel.getSize(); i++) {
      Link link = (Link) listmodel.getElementAt(i);
      while ( (j < selected.length - 1) && (i > selected[j])) {
        j++;
      }
      if (i == selected[j]) {
        selectedvector.add(link);
      }
      else {
        noselectedvector.add(link);
      }
      allvector.add(link);
    }

    //Ask to remove or not
    String question = localize(dialogStrings,"ConstraintKnowledgeDialog.quitAction1");

    for (i = 0; i < selectedvector.size(); i++) {
      question += " " + selectedvector.elementAt(i).toString();
    }
    question += localize(dialogStrings,"ConstraintKnowledgeDialog.PartialOrder.quitAction2");

    int deleteselected = JOptionPane.showConfirmDialog(this, question, "Error",
        JOptionPane.YES_NO_OPTION);

    //If delete is selected, remove the contraints
    if (deleteselected == JOptionPane.YES_OPTION) {
      //first, remove them from the ContraintKnowledge object
      for (i = 0; i < selectedvector.size(); i++) {
        boolean fail = false;
        try {
          if (!this.ck.removeConstraint(ck.PARTIALORDER, (Link) selectedvector.elementAt(i))) {
            fail = true;
          }
        }
        catch (InvalidEditException ev) {
          fail = true;
        }
        if (fail) {
          JOptionPane.showMessageDialog(this,
              localize(dialogStrings,"ConstraintKnowledgeDialog.PartialOrder.ErrorQuit.msg") +
              selectedvector.elementAt(i).toString(),
              "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
      //Second, remove them from gui List
      this.listSelectedO.setListData(noselectedvector);
    }
    else {
      this.listSelectedO.setListData(allvector);
    }
  } //end buttonQuitO_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * In this method, we use a FileChooser to save the order contraints
   * from a file
   * @param e the action event
   */
  void buttonSelecOutputO_actionPerformed(ActionEvent e) {
    //We use a ElviraFileChooser to choose the file to save the order contraints
    ElviraFileChooser filechooser = new ElviraFileChooser(System.getProperty("user.dir"));
    filechooser.rescanCurrentDirectory();
    filechooser.setDialogType(filechooser.SAVE_DIALOG);
    filechooser.setElviraFilter();

    int returnval = filechooser.showSaveDialog(this);

    //if a file is selected, we get it
    if (returnval == JFileChooser.APPROVE_OPTION) {
      File file = filechooser.getSelectedFile();
      //Update the gui
      this.textFieldOutputO.setText(file.getPath());

      //Write the file
      try {
        FileWriter fe = new FileWriter(file.getPath());
        this.ck.savePartialOrderConstraints(fe);
        fe.close();
      } catch(java.io.IOException ex) {
        JOptionPane.showMessageDialog(this, localize(dialogStrings,"ConstraintKnowledgeDialog.PartialOrder.ErrorSave.msg"),
                                  "Error", JOptionPane.ERROR_MESSAGE);
      }
    }else this.textFieldOutputO.setText("[none]");

  } // end buttonSelecOutputO_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * In this method, we use a FileChooser to read the order contraints
   * from a file
   * @param e the action event
   */
  void buttonSelecInputO_actionPerformed(ActionEvent e) {

    //We use a ElviraFileChooser to choose the file
    ElviraFileChooser filechooser = new ElviraFileChooser(System.getProperty("user.dir"));
    filechooser.rescanCurrentDirectory();
    filechooser.setDialogType(filechooser.OPEN_DIALOG);
    filechooser.setElviraFilter();
    int returnval = filechooser.showOpenDialog(this);

    //if a file is selected, we read it
    if (returnval == JFileChooser.APPROVE_OPTION) {
      File file = filechooser.getSelectedFile();
      //update the GUI
      this.textFieldInputO.setText(file.getPath());

      //Store the order contraints int the slected file
      int fail=0;
      try {
        fail = this.ck.loadPartialOrderConstraints(file.getPath());
      } catch (ParseException ev ) {fail=-1;}
      catch (InvalidEditException eve ) {fail=-1;}
      catch (IOException even ) {fail=-2;}

      //If there is a error, we show it
      if (fail<-1) JOptionPane.showMessageDialog(this,localize(dialogStrings,"ConstraintKnowledgeDialog.PartialOrder.ErrorRead.msg1"), "Error",JOptionPane.ERROR_MESSAGE);
      else if (fail<0) JOptionPane.showMessageDialog(this,localize(dialogStrings,"ConstraintKnowledgeDialog.PartialOrder.ErrorRead.msg2"), "Error",JOptionPane.ERROR_MESSAGE);
      else if (fail==0) JOptionPane.showMessageDialog(this,localize(dialogStrings,"ConstraintKnowledgeDialog.ErrorRead.msg3"), "Info",JOptionPane.INFORMATION_MESSAGE);
      else
        //If there is no error, we add the constraint tihe gui List
        this.listSelectedO.setListData(this.ck.getPartialOrderConstraints().getLinkList().getLinks());

      if (fail<=0)
        this.textFieldInputO.setText("[none]");

    } else this.textFieldInputO.setText("[none]");
  } //end buttonSelecInputO_actionPerformed method

  /*------------------------------------------------------------------*/
  /**
   * Finds a string in a ResourceBudle object of the Elvira gui
   * @param bundle the bundle object for seeking into
   * @param name the name of the desired label
   * @return the complete string in the actual loaded language
   */
  private String localize (ResourceBundle bundle, String name)
   {
     return Elvira.getElviraFrame().localize(bundle, name);
   }//end localize method





/*------------------------------------------------------------------*/
 /*###############################################################
               Events Listener Classes
   ##############################################################*/
/*------------------------------------------------------------------*/


 /*------------------------------------------------------------------*/
 /*
  * Auxiliar class to listen Cancel Button events 
  */
public class ConstraintKnowledgeDialog_botonCancel_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_botonCancel_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.botonCancel_actionPerformed(e);
  }
}

/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen OK Button events 
 */

public class ConstraintKnowledgeDialog_botonAccept_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_botonAccept_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.botonAccept_actionPerformed(e);
  }
}

/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Add Button events in existence tab
 */

public class ConstraintKnowledgeDialog_buttonAddE_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonAddE_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonAddE_actionPerformed(e);
  }
}
/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Quit Button events in existence tab
 */
public class ConstraintKnowledgeDialog_buttonQuitE_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonQuitE_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonQuitE_actionPerformed(e);
  }
}
/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Browse output Button events in existence tab
 */
public class ConstraintKnowledgeDialog_buttonSelecOutputE_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonSelecOutputE_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonSelecOutputE_actionPerformed(e);
  }
}
/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Browse input Button events in existence tab
 */
public class ConstraintKnowledgeDialog_buttonSelecInputE_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonSelecInputE_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonSelecInputE_actionPerformed(e);
  }
}

/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Add Button events in absence tab
 */

public class ConstraintKnowledgeDialog_buttonAddA_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonAddA_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonAddA_actionPerformed(e);
  }
}
/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Quit Button events in absence tab
 */
public class ConstraintKnowledgeDialog_buttonQuitA_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonQuitA_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonQuitA_actionPerformed(e);
  }
}
/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Browse output Button events in absence tab
 */
public class ConstraintKnowledgeDialog_buttonSelecOutputA_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonSelecOutputA_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonSelecOutputA_actionPerformed(e);
  }
}
/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Browse input Button events in absence tab
 */
public class ConstraintKnowledgeDialog_buttonSelecInputA_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonSelecInputA_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonSelecInputA_actionPerformed(e);
  }
}

/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Add Button events in order tab
 */

public class ConstraintKnowledgeDialog_buttonAddO_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonAddO_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonAddO_actionPerformed(e);
  }
}
/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Quit Button events in order tab
 */
public class ConstraintKnowledgeDialog_buttonQuitO_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonQuitO_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonQuitO_actionPerformed(e);
  }
}
/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Browse output Button events in order tab
 */
public class ConstraintKnowledgeDialog_buttonSelecOutputO_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonSelecOutputO_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonSelecOutputO_actionPerformed(e);
  }
}
/*------------------------------------------------------------------*/
/*
 * Auxiliar class to listen Browse input Button events in order tab
 */
private class ConstraintKnowledgeDialog_buttonSelecInputO_actionAdapter
    implements java.awt.event.ActionListener {
  ConstraintKnowledgeDialog adaptee;

  ConstraintKnowledgeDialog_buttonSelecInputO_actionAdapter(ConstraintKnowledgeDialog adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.buttonSelecInputO_actionPerformed(e);
  }
}
/*------------------------------------------------------------------*/


 } //end ConstraintKnowledgeDialog class
