package elvira.gui;
import java.awt.Frame;
import java.awt.Dimension;
import javax.swing.JDialog;
import javax.swing.JButton;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import javax.swing.JFormattedTextField;
import java.text.NumberFormat;

import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.awt.CardLayout;
import java.util.Vector;
import java.util.ResourceBundle;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.FontMetrics;

import elvira.database.DataBaseCases;
import elvira.Elvira;
import elvira.gui.NetworkFrame;
import elvira.gui.explication.*;
import elvira.Bnet;
import elvira.Relation;
import elvira.CaseListMem;
import elvira.Node;
import elvira.FiniteStates;
import elvira.learning.preprocessing.Discretization;
import elvira.learning.classification.ConfusionMatrix;

import java.io.File;
import javax.swing.JProgressBar;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import javax.swing.text.*;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.Scrollable;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import java.util.*;
import javax.swing.JEditorPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.text.*;
/**
 * Dialog which concentrates the posibilities Elvira brings for databases files.
 * It is a modal dialog that monitorizes different tasks with a worker {@link DataBaseMonitorWorker}
 *
 * @since 01/06/04
 * @version 1.18
 * @author Armañanzas, R.
 * @author ISG Group
 *
 */
public final class DataBaseMonitor extends JDialog
{
  private JButton selecButton = new JButton();
  private JLabel jLabel1 = new JLabel();
  private JTextField jTextField1 = new JTextField();
  private JTabbedPane jTabbedPane1 = new JTabbedPane();
  private JPanel panelMLearning = new JPanel();
  private JPanel panelPreprocessing = new JPanel();

  private static String imgPath = "elvira/gui/images/";
  private static String mathPath = "elvira/gui/images/math/";

/**
  * Contains the dialog strings for the languaje selected
  */
  private ResourceBundle dialogStrings;

/**
 * Disposes this dialog if no task is enqueued
 */
  private JButton cancelButton = new JButton();

/**
 * Constant for the measurement of a second, expressed in miliseconds
 */
  private final static int ONE_SECOND = 1000;
/**
 * Monitor of the time elapsed from the start of a given task
 */
  private int timeElapsed = 0;

/**
 * This timer is used for controlling the state of the tasks have been doing
 */
  private Timer timer;

/**
 * Internal object that will execute the tasks, see {@link DataBaseMonitorWorker}
 */
  private DataBaseMonitorWorker worker;
  private JProgressBar jProgressBar1 = new JProgressBar();

/**
 * File chooser dialog implemented in the class {@link elvira.gui.ElviraFileChooser}
 */
  private ElviraFileChooser fileDialog = new ElviraFileChooser(System.getProperty("user.dir"));

/**
 * Index of the programmed task that must be done
 * <ul>
 * <li><code>0</code> - idle
 * <li><code>1</code> - load a dbc file in memory
 * <li><code>2</code> - load a dbc file in Elvira and launch the learning dialog
 * <li><code>3</code> - used in order to invoke a filter measure task
 * <li><code>4</code> - ...
 * </ul>
 */
  private int taskToDo = 0;

/**
 * Used for accesing the dialog
 */
  private JDialog currentMonitorDialog;

/**
 * Imputation algorithms availables
 */
  private final static String[] imputationAlg={"Zeros", "Average", "Class conditioned mean",
                                                   "Classification tree", "Iterative MPE", "Incremental MPE"};
/**
 * Imputation algorithms availables for tree construct/prunning
 */
  private final static String[] imputationTree={"ID3", "C4.5", "Dirichlet"};

/**
 * Filter Measures availables
 */
  private final static String[] filterOptions={ "Mutual information", "Euclidean distance",
                                                "Matusita distance", "Kullback-Leibler 1",
                                                "Kullback-Leibler 2", "Shannon entropy",
                                                "Bhattacharyya metric", "CFS"};
/**
 * Discretization algorithms availables
 */
  private final static String[] discretizationAlg={"Equal Frecuency", "Equal Width", "Sum Square Differences",
                                                   "Unsupervised Monothetic Contrast", "K-Means"};

/**
 * Main factorization learning methods
 */
 private final static String[] factorizationMethods={"K2 Learning", "K2SN Learning", "PC Learning", "DVNSST Learning", "Structural MTE Learning"};

/**
 * Factorization metrics
 */
 private final static String[] factorizationMetrics={"BICMetrics", "K2Metrics", "BDeMetrics"};

/**
 * Vector of AuxiliarDiscreteNodes used in the individual discretization process
 */
  private Vector auxiliarVectorNodes;

/**
 * Index of the last node selected in the discretization spin control
 */
  private Integer tabDiscretizationLastIndexedNode = new Integer(0);

/**
 * Instance of the Discretization class used when a normal discretization has to be made
 */
  private Discretization normalDiscretizeInstance = null;

/**
 * Supervised structures availables
 */
  private final static String[] supervisedStructs={"Naïve-Bayes", "TAN", "KDB"};

/**
 * Supervised substructures availables
 */
 private final static String[] supervisedSubstructs={"Todas", "Selective", "Semi"};

/**
 * Unsupervised structures availables
 */
  private final static String[] unsupervisedStructs={"Naïve-Bayes"};

/**
 * Unsupervised learning algorithms
 */
  private final static String[] unsupervisedAlgs={"EM", "EM-MultiStart"};

/**
 * NetworkFrame used to preload a dbc and configure the restrictions
 */
 private NetworkFrame tabFactorizationKnowledgeFrame = null;

/**
 * Vector of JInternalFrames with classifiers
 */
  private Vector classifiers = new Vector();

  private JTabbedPane jTabbedPane2 = new JTabbedPane();
  private JPanel tabSupervised = new JPanel();
  private JPanel tabUnsupervised = new JPanel();
  private JPanel panelPostLearning = new JPanel();
  private JPanel tabFactorization = new JPanel();
  private JTabbedPane jTabbedPane3 = new JTabbedPane();
  private JPanel tabImputation = new JPanel();
  private JPanel tabDiscretization = new JPanel();
  private JPanel tabFilter = new JPanel();
  private JTabbedPane jTabbedPane4 = new JTabbedPane();
  private JPanel tabTest = new JPanel();
  private JLabel tabFilterMainLabel = new JLabel();
  private JLabel tabImputationMainLabel = new JLabel();
  private JComboBox tabImputationComboBox = new JComboBox(imputationAlg);
  private JComboBox tabFilterComboBox = new JComboBox(filterOptions);
  private JCheckBox tabFilterCheckBox = new JCheckBox();
  private JLabel tabFilterVarsLabel = new JLabel();
  private JNumberTextField tabFilterVarsTextField = new JNumberTextField();
  private JTextField tabFilterPathTextField = new JTextField();
  private JButton tabFilterOutFileBrowseButton = new JButton();
  private JLabel tabFilterPathLabel = new JLabel();
  private JScrollPane tabFilterScrollPanel = new JScrollPane();
  private JButton tabFilterProcessButton = new JButton();
  private JPanel jPanel12 = new JPanel();
  private JSpinner tabDiscretizationSpin = new JSpinner();
  private JSpinner.NumberEditor tabDiscretizationNumberSpinEditor = new NumberEditor(tabDiscretizationSpin);
  private SpinnerNumberModel tabDiscretizationNumberModel = new SpinnerNumberModel(1,1,1,1);
  private JButton tabDiscretizationOutFileBrowseButton = new JButton();
  private JButton tabDiscretizationProcessButton = new JButton();
  private JTextField tabDiscretizationFileText = new JTextField();
  private JLabel jLabel2 = new JLabel();
  private JLabel tabImputationLabel1 = new JLabel();
  private JComboBox tabDiscretizationComboBox = new JComboBox(discretizationAlg);
  private JCheckBox tabDiscretizationCheckBox = new JCheckBox();
  private JLabel jLabel3 = new JLabel();
  private JLabel jLabel4 = new JLabel();
  private JLabel jLabel5 = new JLabel();
  private JNumberTextField tabDiscretizationFileText1 = new JNumberTextField();
  private JTextField tabDiscretizationFileText2 = new JTextField();
  private ButtonGroup tabDiscretizationButtonGroup = new ButtonGroup();
  private JRadioButton tabDiscretizationRadioButtonMasive = new JRadioButton();
  private JRadioButton tabDiscretizationRadioButtonNormal = new JRadioButton();
  private JLabel jLabel6 = new JLabel();
  private JLabel jLabel7 = new JLabel();
  private JLabel jLabel8 = new JLabel();
  private JTextField tabDiscretizationFileText3 = new JTextField();
  private JTextField tabDiscretizationFileText4 = new JTextField();
  private JLabel jLabel9 = new JLabel();
  private JTextField tabImputationFileText = new JTextField();
  private JButton tabImputationOutFileBrowseButton = new JButton();
  private JButton tabImputationProcessButton = new JButton();
  private JLabel tabImputationLabel2 = new JLabel();
  private JNumberTextField tabImputationTextField = new JNumberTextField();
  private JLabel tabImputationLabel4 = new JLabel();
  private JNumberTextField tabImputationTextField1 = new JNumberTextField();
  private JLabel tabImputationLabel3 = new JLabel();
  private JComboBox tabImputationComboBox1 = new JComboBox(imputationTree);
  private ScrollableTextPane tabImputationTextPane = new ScrollableTextPane("");
  private JComboBox tabUnsupervisedComboBox = new JComboBox(unsupervisedStructs);
  private JComboBox tabUnsupervisedComboBox1 = new JComboBox(unsupervisedAlgs);
  private JLabel tabUnsupervisedMainLabel = new JLabel();
  private JButton tabUnsupervisedProcessButton = new JButton();
  private JLabel tabUnsupervisedNoClusters = new JLabel();
  private JLabel tabUnsupervisedLearningMethod = new JLabel();
  private JCheckBox tabUnsupervisedCheckBox = new JCheckBox();
  private JNumberTextField tabUnsupervisedTextField = new JNumberTextField();
  private ScrollableTextPane tabUnsupervisedTextPane = new ScrollableTextPane("");
  private JLabel tabUnsupervisedNoModelers = new JLabel();
  private JNumberTextField tabUnsupervisedTextField1 = new JNumberTextField();
  private JButton tabSupervisedProcessButton = new JButton();
  private JLabel tabSupervisedMainLabel = new JLabel();
  private JComboBox tabSupervisedComboBox = new JComboBox(supervisedStructs);
  private JComboBox tabSupervisedComboBox1 = new JComboBox(supervisedSubstructs);
  private JLabel tabSupervisedSubstruct = new JLabel();
  private ScrollableTextPane tabSupervisedTextPane = new ScrollableTextPane("");
  private JCheckBox tabSupervisedCheckBox = new JCheckBox();
  private ButtonGroup tabSupervisedButtonGroup = new ButtonGroup();
  private JRadioButton tabSupervisedRadioButtonFilter = new JRadioButton();
  private JRadioButton tabSupervisedRadioButtonWrapper = new JRadioButton();
  private ButtonGroup tabSupervisedButtonGroup1 = new ButtonGroup();
  private JRadioButton tabSupervisedRadioButtonGreedy = new JRadioButton();
  private JRadioButton tabSupervisedRadioButtonUmda = new JRadioButton();
  private ButtonGroup tabSupervisedButtonGroup2 = new ButtonGroup();
  private JRadioButton tabSupervisedRadioButton95 = new JRadioButton();
  private JRadioButton tabSupervisedRadioButton99 = new JRadioButton();
  private JLabel tabSupervisedParameterK = new JLabel();
  private JNumberTextField tabSupervisedTextField = new JNumberTextField();
  private ButtonGroup tabFactorizationButtonGroup = new ButtonGroup();
  private JRadioButton tabFactorizationRadioButtonDE = new JRadioButton();
  private JRadioButton tabFactorizationRadioButtonLP = new JRadioButton();
  private JComboBox tabFactorizationLearningComboBox = new JComboBox(factorizationMethods);
  private JComboBox tabFactorizationMetricsComboBox = new JComboBox(factorizationMetrics);
  private JButton tabFactorizationProcessButton = new JButton();
  private JLabel tabFactorizationLabel1 = new JLabel();
  private JLabel tabFactorizationLabel2 = new JLabel();
  private JLabel tabFactorizationLabel3 = new JLabel();
  private JLabel tabFactorizationMaxParentsLabel = new JLabel();
  private JNumberTextField tabFactorizationMaxParents = new JNumberTextField();
  private JLabel tabFactorizationNoCasesLabel = new JLabel();
  private JNumberTextField tabFactorizationNoCases = new JNumberTextField();
  private JLabel tabFactorizationConfidenceLabel = new JLabel();
  private JNumberTextField tabFactorizationConfidence = new JNumberTextField();
  private JButton tabFactorizationKnowledgeButton = new JButton();
  private JLabel tabFactorizationNoProcLabel = new JLabel();
  private JNumberTextField tabFactorizationNoProc = new JNumberTextField();
  private JLabel tabFactorizationTamPobLabel = new JLabel();
  private JNumberTextField tabFactorizationTamPob = new JNumberTextField();
  private JLabel tabFactorizationNoIterLabel = new JLabel();
  private JNumberTextField tabFactorizationNoIter = new JNumberTextField();
  private JLabel tabFactorizationMaxNoNeighboursLabel = new JLabel();
  private JNumberTextField tabFactorizationMaxNoNeighbours = new JNumberTextField();
  private JLabel tabTestClassifierLabel = new JLabel();
  private JComboBox tabTestClassifierComboBox = new JComboBox();
  private ButtonGroup tabTestButtonGroup = new ButtonGroup();
  private JRadioButton tabTestRadioButtonValidation = new JRadioButton();
  private JRadioButton tabTestRadioButtonCategorize = new JRadioButton();
  private JRadioButton tabTestRadioButtonTest = new JRadioButton();
  private JButton tabTestProcessButton = new JButton();
  private JTextField tabTestFileText = new JTextField();
  private JButton tabTestOutFileBrowseButton = new JButton();
  private JNumberTextField tabTestTextField = new JNumberTextField();
  private JLabel tabTestParameterK = new JLabel();
  private JCheckBox tabFilterCheckBox1 = new JCheckBox();
/**
 * Constructor for the main dialog, it will be fixed as modal
 * 
 * @param parent the Frame from the dialog is invoked
 * @param title the title of the dialog
 *        
 */
  public DataBaseMonitor(Frame parent, String title)
  {
    super(parent, title, true);
    try
    {
      jbInit();
    }
    catch(Exception e)
    {
      Elvira.println("Fatal error creating the database dialog");
      e.printStackTrace();
    }
  }//end DataBaseMonitor(Frame, String)

/**
 * Initialices all the dialog components
 */
  private void jbInit() throws Exception
  {
    dialogStrings = Elvira.getElviraFrame().getDialogBundle();
    
    /////////////////////////////////////////////////////-1. Initialize the main properties the dialog have
    this.setSize(new Dimension(600, 400));
    this.getContentPane().setLayout(null);
    this.setResizable(false);
    this.setDefaultCloseOperation(0); //Disables the close control
    jLabel1.setText(localize(dialogStrings,"DataBaseMonitor.CaseLabel.label"));
    jLabel1.setBounds(new Rectangle(10, 10, 125, 20));
    jTextField1.setBounds(new Rectangle(135, 8, 310, 25));
    jTextField1.setEnabled(false);
    jTextField1.setText("[none]");
    jTabbedPane1.setBounds(new Rectangle(10, 45, 570, 255));
    jTabbedPane1.setFont(new Font("Dialog", 1, 14));
    cancelButton.setText(localize(dialogStrings,"DataBaseMonitor.Cancel.label"));
    cancelButton.setMnemonic((int)(localize(dialogStrings,"DataBaseMonitor.Cancel.label")).charAt(0));
    cancelButton.setBounds(new Rectangle(220, 335, 150, 25));
    selecButton.setText(localize(dialogStrings,"DataBaseMonitor.Browse.label"));
    selecButton.setMnemonic((int)(localize(dialogStrings,"DataBaseMonitor.Browse.label")).charAt(0));
    selecButton.setBounds(new Rectangle(460, 8, 120, 25));
    jTabbedPane1.addTab(localize(dialogStrings,"DatabaseMonitor.Preprocess.tab"), panelPreprocessing);
    jTabbedPane1.addTab(localize(dialogStrings,"DataBaseMonitor.mLearning.tab"), panelMLearning);
    jTabbedPane1.addTab(localize(dialogStrings,"DataBaseMonitor.PostLearning.tab"), panelPostLearning);   
    jTabbedPane1.setSelectedIndex(0);
    jProgressBar1.setBounds(new Rectangle(10, 305, 570, 20));
    jProgressBar1.setVisible(false);
    this.getContentPane().add(jProgressBar1, null);    this.getContentPane().add(cancelButton, null);
    this.getContentPane().add(jTextField1, null);      this.getContentPane().add(jLabel1, null);
    this.getContentPane().add(selecButton, null);      this.getContentPane().add(jTabbedPane1, null);

    ///////- 1.a. Action Listeners
    selecButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { selecFile_actionPerformed(e);}
      });
    cancelButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { cancel_actionPerformed(e); }
      });
    ///////- end 1.a
    /////////////////////////////////////////////////////- 1. end initialize dialog

    /////////////////////////////////////////////////////- 2. Initialize jTabbedPane1
    panelPreprocessing.setLayout(null);      panelPreprocessing.add(jTabbedPane3, null);
    panelMLearning.setLayout(null);          panelMLearning.add(jTabbedPane2, null);
    panelPostLearning.setLayout(null);       panelPostLearning.add(jTabbedPane4, null);
    /////////////////////////////////////////////////////- 2. End jTabbedPane1

    /////////////////////////////////////////////////////- 3. Initialize panelPreprocessing
    jTabbedPane3.setBounds(new Rectangle(5, 5, 555, 215));
    jTabbedPane3.setTabPlacement(JTabbedPane.BOTTOM);
    jTabbedPane3.addTab(localize(dialogStrings, "DataBaseMonitor.Imputation.label"), tabImputation);
    jTabbedPane3.addTab(localize(dialogStrings, "DataBaseMonitor.Discretization.label"), tabDiscretization);
    jTabbedPane3.addTab(localize(dialogStrings, "DataBaseMonitor.Filter.tab"), tabFilter);
    jTabbedPane3.setSelectedIndex(0);
    /////////////////////////////////////////////////////- 3. End panelPreprocessing

    /////////////////////////////////////////////////////- 3.1 Initialize tabImputation
    tabImputation.setLayout(null);
    tabImputationProcessButton.setText(localize(dialogStrings, "DatabaseMonitor.Process"));
    tabImputationProcessButton.setBounds(new Rectangle(455, 95, 90, 80));
    tabImputationProcessButton.setIcon(new ImageIcon(imgPath + "gear.gif"));
    tabImputationProcessButton.setHorizontalTextPosition(SwingConstants.CENTER);
    tabImputationProcessButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    tabImputationOutFileBrowseButton.setBounds(new Rectangle(325, 150, 105, 25));
    tabImputationOutFileBrowseButton.setText(localize(dialogStrings, "DataBaseMonitor.Browse.label"));
    tabImputationFileText.setText("[none]");
    tabImputationFileText.setBounds(new Rectangle(10, 150, 310, 25));
    tabImputationFileText.setEnabled(false);
    tabImputationLabel1.setBounds(new Rectangle(10, 125, 145, 25));
    tabImputationLabel1.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.outFile"));
    tabImputationMainLabel.setBounds(new Rectangle(15, 5, 180, 25));
    tabImputationMainLabel.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.mainlabel"));
    tabImputationComboBox1.setBounds(new Rectangle(175, 95, 97, 25));
    tabImputationLabel4.setBounds(new Rectangle(290, 95, 100, 25));
    tabImputationLabel4.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.noIterations"));
    tabImputationTextField1.setBounds(new Rectangle(400, 97, 25, 20));
    tabImputationTextField1.setErrorMessage(localize(dialogStrings, "DataBaseMonitor.Imputation.errorNoIterations"));
    tabImputation.add(tabImputationLabel4, null);
    tabImputation.add(tabImputationTextField1, null);
    tabImputationComboBox.setBounds(new Rectangle(15, 30, 180, 25));
    tabImputationTextField.setBounds(new Rectangle(110, 65, 25, 20));
    tabImputationTextField.setErrorMessage(localize(dialogStrings, "DataBaseMonitor.Imputation.errorClassVar"));
    tabImputationLabel2.setBounds(new Rectangle(15, 65, 95, 20));
    tabImputationLabel2.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.classVar"));
    tabImputationLabel3.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.treeBuild"));
    tabImputationLabel3.setBounds(new Rectangle(15, 95, 155, 20));
    tabImputationTextPane.setBounds(new Rectangle(225, 10, 315, 70));
    tabImputationTextPane.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.ZerosExplanation"));
    tabImputationTextPane.setEnabled(false);

    tabImputation.add(tabImputationTextPane, null);
    tabImputation.add(tabImputationComboBox1, null);
    tabImputation.add(tabImputationLabel3, null);
    tabImputation.add(tabImputationTextField, null);
    tabImputation.add(tabImputationLabel2, null);
    tabImputation.add(tabImputationFileText, null);
    tabImputation.add(tabImputationOutFileBrowseButton, null);
    tabImputation.add(tabImputationProcessButton, null);
    tabImputation.add(tabImputationLabel1, null);
    tabImputation.add(tabImputationMainLabel, null);
    tabImputation.add(tabImputationComboBox, null);
    ///////- 3.1.a. Action Listeners
    tabImputationOutFileBrowseButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e){
          tabImputationOutFileBrowseButton_actionPerformed(e); }
      });
    tabImputationProcessButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e){
          tabImputationProcessButton_actionPerformed(e); }
      });
    tabImputationComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabImputationComboBox_actionPerformed(e); }
      });
    ///////- end 3.1.a
    /////////////////////////////////////////////////////- 3.1 End tabImputation
    /////////////////////////////////////////////////////- 3.2 Initialize tabDiscretization
    tabDiscretization.setLayout(null);
    auxiliarVectorNodes = null;
    tabDiscretizationSpin.setModel(tabDiscretizationNumberModel);
    tabDiscretizationSpin.setBounds(new Rectangle(405, 65, 50, 25));
    tabDiscretizationOutFileBrowseButton.setBounds(new Rectangle(325, 150, 105, 25));
    tabDiscretizationOutFileBrowseButton.setText(localize(dialogStrings, "DataBaseMonitor.Browse.label"));
    tabDiscretizationProcessButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    tabDiscretizationProcessButton.setHorizontalTextPosition(SwingConstants.CENTER);
    tabDiscretizationProcessButton.setBounds(new Rectangle(455, 95, 90, 80));
    tabDiscretizationProcessButton.setIcon(new ImageIcon(imgPath+"gear.gif"));
    tabDiscretizationProcessButton.setText(localize(dialogStrings, "DatabaseMonitor.Process"));
    tabDiscretizationFileText.setBounds(new Rectangle(10, 150, 310, 25));
    tabDiscretizationFileText.setEnabled(false);
    tabDiscretizationFileText.setText("[none]");
    jLabel2.setBounds(new Rectangle(10, 125, 145, 25));
    jLabel2.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.outFile"));

    tabDiscretizationComboBox.setBounds(new Rectangle(15, 60, 230, 25));
    tabDiscretizationCheckBox.setBounds(new Rectangle(455, 65, 95, 25));
    tabDiscretizationCheckBox.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.discretize"));
    tabDiscretizationCheckBox.setHorizontalTextPosition(SwingConstants.RIGHT);
    
    tabDiscretizationRadioButtonMasive.setBounds(new Rectangle(195, 5, 80, 25));
    tabDiscretizationRadioButtonNormal.setBounds(new Rectangle(275, 5, 85, 25));
    tabDiscretizationButtonGroup.add(tabDiscretizationRadioButtonMasive);
    tabDiscretizationButtonGroup.add(tabDiscretizationRadioButtonNormal);
    tabDiscretizationRadioButtonMasive.setSelected(true);
    this.controlNormalDiscretizationGUI(false);

    tabDiscretizationFileText2.setEnabled(false); jLabel5.setEnabled(false);
        
    jLabel9.setBounds(new Rectangle(15, 35, 75, 25));
    jLabel9.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.alg"));
    tabDiscretizationFileText4.setBounds(new Rectangle(450, 35, 95, 25));
    tabDiscretizationFileText4.setEditable(false);
    tabDiscretizationFileText3.setEditable(false);
    tabDiscretizationFileText3.setBounds(new Rectangle(450, 5, 95, 25));
    jLabel8.setBounds(new Rectangle(390, 35, 40, 25));
    jLabel8.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.varType"));
    jLabel7.setText("Variable");
    jLabel7.setBounds(new Rectangle(390, 5, 55, 25));
    jLabel6.setBounds(new Rectangle(330, 65, 80, 25));
    jLabel6.setText("Nº Variable");
    tabDiscretizationFileText2.setText("0.05");
    tabDiscretizationRadioButtonMasive.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.masive"));
    tabDiscretizationRadioButtonNormal.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.normal"));
    tabDiscretizationFileText2.setBounds(new Rectangle(210, 95, 35, 20));
    tabDiscretizationFileText1.setBounds(new Rectangle(105, 95, 25, 20));
    tabDiscretizationFileText1.setErrorMessage(localize(dialogStrings, "DataBaseMonitor.Discretization.errorIntervals"));
    jLabel5.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.options"));
    jLabel5.setBounds(new Rectangle(145, 93, 70, 25));
    jLabel4.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.intervals"));
    jLabel4.setBounds(new Rectangle(15, 93, 90, 25));
    jLabel3.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.label2"));
    jLabel3.setBounds(new Rectangle(15, 5, 180, 25));

    tabDiscretization.add(jLabel9, null);
    tabDiscretization.add(tabDiscretizationFileText4, null);
    tabDiscretization.add(tabDiscretizationFileText3, null);
    tabDiscretization.add(jLabel8, null);
    tabDiscretization.add(jLabel7, null);
    tabDiscretization.add(jLabel6, null);
    tabDiscretization.add(tabDiscretizationFileText2, null);
    tabDiscretization.add(tabDiscretizationFileText1, null);
    tabDiscretization.add(jLabel5, null);
    tabDiscretization.add(jLabel4, null);
    tabDiscretization.add(jLabel3, null); tabDiscretization.add(tabDiscretizationFileText, null);
    tabDiscretization.add(jLabel2, null); tabDiscretization.add(tabDiscretizationSpin, null);
    tabDiscretization.add(tabDiscretizationOutFileBrowseButton, null);
    tabDiscretization.add(tabDiscretizationProcessButton, null);  
    tabDiscretization.add(tabDiscretizationComboBox, null);
    tabDiscretization.add(tabDiscretizationCheckBox, null);
    tabDiscretization.add(tabDiscretizationRadioButtonMasive, null);
    tabDiscretization.add(tabDiscretizationRadioButtonNormal, null);

    ///////- 3.2.a. Action Listeners
    tabDiscretizationOutFileBrowseButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabDiscretizationOutFileBrowseButton_actionPerformed(e); }
      });
    tabDiscretizationProcessButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabDiscretizationProcessButton_actionPerformed(e); }
      });
    tabDiscretizationRadioButtonMasive.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabDiscretizationRadioButton_actionPerformed(e); }
      });
    tabDiscretizationRadioButtonNormal.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabDiscretizationRadioButton_actionPerformed(e); }
      });      
    tabDiscretizationComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabDiscretizationComboBox_actionPerformed(e); }
      });      
    tabDiscretizationCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabDiscretizationCheckBox_actionPerformed(e);  }
      });
    tabDiscretizationSpin.addChangeListener(new ChangeListener()
      {
        public void stateChanged(ChangeEvent e)
        {
          tabDiscretizationSpin_stateChanged(e);
        }
      });
    ///////- end 3.2.a
    /////////////////////////////////////////////////////- 3.2 End tabDiscretization
    /////////////////////////////////////////////////////- 3.3 Initialize tabFilter
    tabFilter.setLayout(null);
    tabFilterComboBox.setBounds(new Rectangle(15, 30, 165, 25));
    tabFilterScrollPanel.setBounds(new Rectangle(15, 95, 430, 80));
    tabFilterScrollPanel.setAutoscrolls(true);
    tabFilterMainLabel.setBounds(new Rectangle(15, 5, 105, 25));
    tabFilterMainLabel.setText(localize(dialogStrings, "DataBaseMonitor.Filter.label"));    
    tabFilterCheckBox.setBounds(new Rectangle(10, 65, 155, 20));
    tabFilterCheckBox.setText(localize(dialogStrings, "DataBaseMonitor.Filter.savecheckbox"));
    tabFilterVarsLabel.setBounds(new Rectangle(200, 40, 105, 25));
    tabFilterVarsLabel.setText(localize(dialogStrings,"DataBaseMonitor.Filter.vars"));
    tabFilterPathLabel.setBounds(new Rectangle(200, 8, 110, 25));
    tabFilterPathLabel.setText(localize(dialogStrings, "DataBaseMonitor.Discretization.outFile"));    
    tabFilterVarsTextField.setBounds(new Rectangle(305, 42, 25, 23));
    tabFilterVarsTextField.setErrorMessage("Error");
    tabFilterPathTextField.setBounds(new Rectangle(315, 10, 229, 23));
    tabFilterPathTextField.setText("[none]");
    tabFilterPathTextField.setEnabled(false);
    tabFilterOutFileBrowseButton.setBounds(new Rectangle(440, 40, 105, 28));
    tabFilterOutFileBrowseButton.setText(localize(dialogStrings, "DataBaseMonitor.Browse.label"));
    tabFilterProcessButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    tabFilterProcessButton.setHorizontalTextPosition(SwingConstants.CENTER);    
    tabFilterProcessButton.setBounds(new Rectangle(455, 95, 90, 80));
    tabFilterProcessButton.setIcon(new ImageIcon(imgPath+"gear.gif"));
    tabFilterProcessButton.setText(localize(dialogStrings, "DatabaseMonitor.Process"));
    
    tabFilterCheckBox1.setText(localize(dialogStrings, "DataBaseMonitor.Filter.auto.label"));
    tabFilterCheckBox1.setBounds(new Rectangle(334, 41, 100, 25));
    tabFilterCheckBox1.setHorizontalTextPosition(SwingConstants.RIGHT);
    tabFilterCheckBox1.setVerticalAlignment(SwingConstants.CENTER);
    tabFilterCheckBox1.setToolTipText(localize(dialogStrings, "DataBaseMonitor.Filter.auto.tip"));
    
    tabFilter.add(tabFilterProcessButton, null);
    tabFilter.add(tabFilterCheckBox1, null);     tabFilter.add(tabFilterScrollPanel, null);
    tabFilter.add(tabFilterPathLabel, null);     tabFilter.add(tabFilterOutFileBrowseButton, null);
    tabFilter.add(tabFilterVarsLabel, null);     tabFilter.add(tabFilterCheckBox, null);
    tabFilter.add(tabFilterComboBox, null);      tabFilter.add(tabFilterMainLabel, null);
    tabFilter.add(tabFilterPathTextField, null);
    tabFilter.add(tabFilterVarsTextField, null);
    ///////- 3.3.a. Action Listeners
    tabFilterComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabFilterComboBox_actionPerformed(e); }
      });    
    tabFilterCheckBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabFilterCheckBox_actionPerformed(e); }
      });
    tabFilterOutFileBrowseButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabFilterOutFileBrowseButton_actionPerformed(e); }
      });
    tabFilterProcessButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabFilterProcessButton_actionPerformed(e); }
      }); 
    tabFilterCheckBox1.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabFilterCheckBox_actionPerformed(e); }
      });
    ///////- end 3.3.a    
    /////////////////////////////////////////////////////- 3.3 End tabFilter
    
    /////////////////////////////////////////////////////- 4. Initialize panelMLearning
    jTabbedPane2.setBounds(new Rectangle(5, 5, 555, 215));
    jTabbedPane2.setTabPlacement(JTabbedPane.BOTTOM);
    jTabbedPane2.addTab(localize(dialogStrings,"DataBaseMonitor.Supervised.tab"), tabSupervised);
    jTabbedPane2.addTab(localize(dialogStrings,"DataBaseMonitor.Unsupervised.tab"), tabUnsupervised);
    jTabbedPane2.addTab(localize(dialogStrings,"DataBaseMonitor.Factorization.tab"), tabFactorization);
    //jTabbedPane2.setMnemonicAt(0,(int)(localize(dialogStrings,"DataBaseMonitor.Supervised.tab")).charAt(0));
    //jTabbedPane2.setMnemonicAt(1,(int)(localize(dialogStrings,"DataBaseMonitor.Unsupervised.tab")).charAt(1));    
    /////////////////////////////////////////////////////- 4.. End panelMLearning
    /////////////////////////////////////////////////////- 4.1 Initialize tabSupervised
    tabSupervised.setLayout(null);
    tabSupervisedComboBox.setBounds(new Rectangle(15, 30, 180, 25));
    tabSupervisedComboBox1.setBounds(new Rectangle(260, 30, 120, 25));    
    tabSupervisedMainLabel.setBounds(new Rectangle(15, 5, 215, 25));
    tabSupervisedMainLabel.setText(localize(dialogStrings, "DataBaseMonitor.Mlearning.mainLabel"));
    tabSupervisedProcessButton.setText(localize(dialogStrings, "DatabaseMonitor.Process"));
    tabSupervisedProcessButton.setBounds(new Rectangle(455, 95, 90, 80));
    tabSupervisedProcessButton.setIcon(new ImageIcon(imgPath + "gear.gif"));
    tabSupervisedProcessButton.setHorizontalTextPosition(SwingConstants.CENTER);
    tabSupervisedProcessButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    tabSupervisedSubstruct.setText(localize(dialogStrings, "DataBaseMonitor.Supervised.subLabel"));
    tabSupervisedSubstruct.setBounds(new Rectangle(260, 5, 100, 25));    
    tabSupervisedTextPane.setBounds(new Rectangle(15, 95, 425, 80));
    tabSupervisedTextPane.setEnabled(false);
    tabSupervisedTextPane.setText(localize(dialogStrings, "DataBaseMonitor.Supervised.NBAllExplanation"));
    tabSupervisedCheckBox.setBounds(new Rectangle(15, 60, 165, 25));
    tabSupervisedCheckBox.setText(localize(dialogStrings, "DataBaseMonitor.Mlearning.Laplace"));    
    tabSupervisedRadioButtonFilter.setBounds(new Rectangle(230, 60, 75, 25));
    tabSupervisedRadioButtonFilter.setText(localize(dialogStrings, "DataBaseMonitor.Supervised.filterLabel"));
    tabSupervisedRadioButtonWrapper.setBounds(new Rectangle(310, 60, 85, 25));
    tabSupervisedRadioButtonWrapper.setText(localize(dialogStrings, "DataBaseMonitor.Supervised.wrapperLabel"));
    tabSupervisedButtonGroup.add(tabSupervisedRadioButtonFilter);
    tabSupervisedButtonGroup.add(tabSupervisedRadioButtonWrapper);
    tabSupervisedRadioButtonFilter.setSelected(true);
    tabSupervisedRadioButtonGreedy.setBounds(new Rectangle(415, 34, 70, 25));
    tabSupervisedRadioButtonGreedy.setText("Greedy");
    tabSupervisedRadioButtonUmda.setBounds(new Rectangle(481, 34, 65, 25));
    tabSupervisedRadioButtonUmda.setText("Umda");
    tabSupervisedButtonGroup1.add(tabSupervisedRadioButtonGreedy);
    tabSupervisedButtonGroup1.add(tabSupervisedRadioButtonUmda);
    tabSupervisedRadioButtonGreedy.setSelected(true);  
    tabSupervisedRadioButton95.setBounds(new Rectangle(415, 8, 55, 25));
    tabSupervisedRadioButton95.setText("95 %");
    tabSupervisedRadioButton99.setBounds(new Rectangle(481, 8, 55, 25));
    tabSupervisedRadioButton99.setText("99 %");
    tabSupervisedButtonGroup2.add(tabSupervisedRadioButton95);
    tabSupervisedButtonGroup2.add(tabSupervisedRadioButton99);
    tabSupervisedRadioButton95.setSelected(true);
    tabSupervisedParameterK.setBounds(new Rectangle(422, 60, 80, 25));
    tabSupervisedParameterK.setText(localize(dialogStrings,"DataBaseMonitor.Test.kParameterLabel"));
    tabSupervisedTextField.setBounds(new Rectangle(507, 62, 25, 20));
    tabSupervisedTextField.setErrorMessage(localize(dialogStrings, "DataBaseMonitor.Supervised.ParamKError"));
    tabSupervisedTextField.setValue(new Integer(2));

    tabSupervised.add(tabSupervisedComboBox, null);
    tabSupervised.add(tabSupervisedComboBox1, null);
    tabSupervised.add(tabSupervisedMainLabel, null);
    tabSupervised.add(tabSupervisedProcessButton, null);
    tabSupervised.add(tabSupervisedSubstruct, null);
    tabSupervised.add(tabSupervisedTextPane, null);
    tabSupervised.add(tabSupervisedCheckBox, null);
    tabSupervised.add(tabSupervisedRadioButtonFilter, null);
    tabSupervised.add(tabSupervisedRadioButtonWrapper, null);
    tabSupervised.add(tabSupervisedRadioButton95, null);
    tabSupervised.add(tabSupervisedRadioButton99, null);
    tabSupervised.add(tabSupervisedRadioButtonGreedy, null);
    tabSupervised.add(tabSupervisedRadioButtonUmda, null);
    tabSupervised.add(tabSupervisedParameterK, null);
    tabSupervised.add(tabSupervisedTextField, null);
    
    ///////- 4.2.a. Action Listeners
    tabSupervisedComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabSupervisedComboBox_actionPerformed(e); }
      });     
    tabSupervisedRadioButtonFilter.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabSupervisedComboBox_actionPerformed(e); }
      });
    tabSupervisedRadioButtonWrapper.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabSupervisedComboBox_actionPerformed(e); }
      });
    tabSupervisedComboBox1.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabSupervisedComboBox_actionPerformed(e);}
      });     
    tabSupervisedProcessButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabSupervisedProcessButton_actionPerformed(e); }
      });
    tabSupervisedComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabSupervisedComboBox_actionPerformed(e); }
      });      
    tabSupervisedRadioButton95.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabSupervisedControlNBExplanation(); }
      });      
    tabSupervisedRadioButton99.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabSupervisedControlNBExplanation(); }
      });      
    tabSupervisedRadioButtonGreedy.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabSupervisedControlNBExplanation(); }
      });      
    tabSupervisedRadioButtonUmda.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabSupervisedControlNBExplanation(); }
      });      
    ///////- end 4.2.a       
    /////////////////////////////////////////////////////- 4.1 End tabSupervised
    /////////////////////////////////////////////////////- 4.2 Initialize tabUnsupervised
    tabUnsupervised.setLayout(null);    
    tabUnsupervisedComboBox.setBounds(new Rectangle(15, 30, 180, 25));
    tabUnsupervisedComboBox1.setBounds(new Rectangle(260, 30, 120, 25));    
    tabUnsupervisedMainLabel.setBounds(new Rectangle(15, 5, 215, 25));
    tabUnsupervisedMainLabel.setText(localize(dialogStrings, "DataBaseMonitor.Mlearning.mainLabel"));
    tabUnsupervisedProcessButton.setText(localize(dialogStrings, "DatabaseMonitor.Process"));
    tabUnsupervisedProcessButton.setBounds(new Rectangle(455, 95, 90, 80));
    tabUnsupervisedProcessButton.setIcon(new ImageIcon(imgPath + "gear.gif"));
    tabUnsupervisedProcessButton.setHorizontalTextPosition(SwingConstants.CENTER);
    tabUnsupervisedProcessButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    tabUnsupervisedNoClusters.setText(localize(dialogStrings, "DataBaseMonitor.Unsupervised.NoClusters"));
    tabUnsupervisedNoClusters.setBounds(new Rectangle(15, 60, 110, 25));
    tabUnsupervisedTextField.setBounds(new Rectangle(120, 62, 25, 20));
    tabUnsupervisedTextField.setErrorMessage(localize(dialogStrings, "DataBaseMonitor.Unsupervised.errorClusters"));
    tabUnsupervisedLearningMethod.setText(localize(dialogStrings, "DataBaseMonitor.Unsupervised.Learning"));
    tabUnsupervisedLearningMethod.setBounds(new Rectangle(260, 5, 160, 25));
    tabUnsupervisedCheckBox.setBounds(new Rectangle(260, 60, 165, 25));
    tabUnsupervisedCheckBox.setText(localize(dialogStrings, "DataBaseMonitor.Mlearning.Laplace"));
    tabUnsupervisedTextPane.setBounds(new Rectangle(15, 95, 425, 80));
    tabUnsupervisedTextPane.setEnabled(false);
    tabUnsupervisedTextPane.setText(localize(dialogStrings, "DataBaseMonitor.Unsupervised.EMExplanation"));
    tabUnsupervisedTextField1.setBounds(new Rectangle(515, 30, 25, 20));
    tabUnsupervisedTextField1.setErrorMessage(localize(dialogStrings, "DataBaseMonitor.Unsupervised.errorExecs"));
    tabUnsupervisedNoModelers.setBounds(new Rectangle(425, 5, 120, 25));
    tabUnsupervisedNoModelers.setText(localize(dialogStrings, "DataBaseMonitor.Unsupervised.NoExecs"));
    
    tabUnsupervised.add(tabUnsupervisedTextField1, null);
    tabUnsupervised.add(tabUnsupervisedNoModelers, null);
    tabUnsupervised.add(tabUnsupervisedTextPane, null);
    tabUnsupervised.add(tabUnsupervisedTextField, null);
    tabUnsupervised.add(tabUnsupervisedCheckBox, null);
    tabUnsupervised.add(tabUnsupervisedLearningMethod, null);
    tabUnsupervised.add(tabUnsupervisedNoClusters, null);
    tabUnsupervised.add(tabUnsupervisedComboBox1, null);
    tabUnsupervised.add(tabUnsupervisedComboBox, null);    
    tabUnsupervised.add(tabUnsupervisedMainLabel, null);
    tabUnsupervised.add(tabUnsupervisedProcessButton, null);
    ///////- 4.2.a. Action Listeners
    tabUnsupervisedProcessButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabUnsupervisedProcessButton_actionPerformed(e); }
      }); 
    tabUnsupervisedComboBox1.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabUnsupervisedComboBox1_actionPerformed(e); }
      }); 
    ///////- end 4.2.a       
    /////////////////////////////////////////////////////- 4.2 End tabUnsupervised
    /////////////////////////////////////////////////////- 4.3 Initialize tabFactorization
    tabFactorization.setLayout(null);
    tabFactorizationLearningComboBox.setBounds(new Rectangle(15, 30, 185, 25));
    tabFactorizationMetricsComboBox.setBounds(new Rectangle(240, 30, 120, 25));
    tabFactorizationProcessButton.setText(localize(dialogStrings, "DatabaseMonitor.Process"));
    tabFactorizationProcessButton.setBounds(new Rectangle(455, 95, 90, 80));
    tabFactorizationProcessButton.setIcon(new ImageIcon(imgPath + "gear.gif"));
    tabFactorizationProcessButton.setHorizontalTextPosition(SwingConstants.CENTER);
    tabFactorizationProcessButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    tabFactorizationButtonGroup.add(tabFactorizationRadioButtonLP);
    tabFactorizationButtonGroup.add(tabFactorizationRadioButtonDE);
    tabFactorizationRadioButtonLP.setBounds(new Rectangle(400, 30, 120, 25));
    tabFactorizationRadioButtonLP.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.LPLabel"));
    tabFactorizationRadioButtonLP.setSelected(true);
    tabFactorizationRadioButtonDE.setBounds(new Rectangle(400, 55, 140, 25));
    tabFactorizationRadioButtonDE.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.DELabel"));
    tabFactorizationLabel1.setBounds(new Rectangle(15, 5, 215, 25));
    tabFactorizationLabel1.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.MethodLabel"));
    tabFactorizationLabel2.setBounds(new Rectangle(240, 5, 215, 25));
    tabFactorizationLabel2.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.MetricLabel"));
    tabFactorizationLabel3.setBounds(new Rectangle(400, 5, 215, 25));
    tabFactorizationLabel3.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.EstimationLabel"));    
    tabFactorizationMaxParentsLabel.setBounds(new Rectangle(15, 63, 150, 25));
    tabFactorizationMaxParentsLabel.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.MaxParentsLabel"));
    tabFactorizationMaxParents.setBounds(new Rectangle(160, 65, 25, 20));
    tabFactorizationMaxParents.setErrorMessage(localize(dialogStrings,"DataBaseMonitor.Factorization.MaxParentsError"));
    tabFactorizationNoCasesLabel.setBounds(new Rectangle(15, 88, 150, 25));
    tabFactorizationNoCasesLabel.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.CasesLabel"));
    tabFactorizationNoCases.setBounds(new Rectangle(160, 90, 25, 20));
    tabFactorizationNoCases.setErrorMessage(localize(dialogStrings,"DataBaseMonitor.Factorization.CasesError"));
    tabFactorizationConfidenceLabel.setBounds(new Rectangle(15, 113, 150, 25));
    tabFactorizationConfidenceLabel.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.ConfLabel"));
    tabFactorizationConfidence.setErrorMessage(localize(dialogStrings,"DataBaseMonitor.Factorization.ConfError"));
    tabFactorizationConfidence.setBounds(new Rectangle(160, 115, 25, 20));
    tabFactorizationKnowledgeButton.setBounds(new Rectangle(15, 145, 170, 25));
    tabFactorizationKnowledgeButton.setIcon(new ImageIcon(imgPath + "constraints2.gif"));
    tabFactorizationKnowledgeButton.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.KnowledgeLabel"));
    tabFactorizationNoProcLabel.setBounds(new Rectangle(215, 63, 150, 25));
    tabFactorizationNoProcLabel.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.ProcLabel"));
    tabFactorizationNoProc.setBounds(new Rectangle(365, 65, 25, 20));
    tabFactorizationNoProc.setErrorMessage(localize(dialogStrings,"DataBaseMonitor.Factorization.ProcError"));
    tabFactorizationTamPobLabel.setBounds(new Rectangle(215, 88, 150, 25));
    tabFactorizationTamPobLabel.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.TamLabel"));
    tabFactorizationTamPob.setBounds(new Rectangle(365, 90, 25, 20));
    tabFactorizationTamPob.setErrorMessage(localize(dialogStrings,"DataBaseMonitor.Factorization.TamError"));
    tabFactorizationNoIterLabel.setBounds(new Rectangle(215, 113, 150, 25));
    tabFactorizationNoIterLabel.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.NoIterLabel"));
    tabFactorizationNoIter.setBounds(new Rectangle(365, 115, 25, 20));
    tabFactorizationNoIter.setErrorMessage(localize(dialogStrings,"DataBaseMonitor.Factorization.NoIterError"));
    tabFactorizationMaxNoNeighboursLabel.setBounds(new Rectangle(215, 138, 150, 25));
    tabFactorizationMaxNoNeighboursLabel.setText(localize(dialogStrings,"DataBaseMonitor.Factorization.MaxNeigLabel"));
    tabFactorizationMaxNoNeighbours.setBounds(new Rectangle(365, 140, 25, 20));
    tabFactorizationMaxNoNeighbours.setErrorMessage(localize(dialogStrings,"DataBaseMonitor.Factorization.MaxNeigError"));

    //Set some initial values
    tabFactorizationMaxParents.setValue(new Integer(5));
    tabFactorizationConfidence.setValue(new Integer(95));
    tabFactorizationNoProc.setValue(new Integer(1));
    tabFactorizationTamPob.setValue(new Integer(1));
    tabFactorizationNoIter.setValue(new Integer(0));
    tabFactorizationMaxNoNeighbours.setValue(new Integer(0));

    tabFactorization.add(tabFactorizationNoProcLabel, null);
    tabFactorization.add(tabFactorizationNoProc, null);
    tabFactorization.add(tabFactorizationTamPobLabel, null);
    tabFactorization.add(tabFactorizationTamPob, null);
    tabFactorization.add(tabFactorizationNoIterLabel, null);
    tabFactorization.add(tabFactorizationNoIter, null);
    tabFactorization.add(tabFactorizationMaxNoNeighboursLabel, null);
    tabFactorization.add(tabFactorizationMaxNoNeighbours, null);
    tabFactorization.add(tabFactorizationKnowledgeButton, null);
    tabFactorization.add(tabFactorizationConfidenceLabel, null);
    tabFactorization.add(tabFactorizationConfidence, null);
    tabFactorization.add(tabFactorizationNoCasesLabel, null);
    tabFactorization.add(tabFactorizationNoCases, null);
    tabFactorization.add(tabFactorizationMaxParentsLabel, null);
    tabFactorization.add(tabFactorizationMaxParents, null);
    tabFactorization.add(tabFactorizationLabel1, null);
    tabFactorization.add(tabFactorizationLabel2, null);
    tabFactorization.add(tabFactorizationLabel3, null);
    tabFactorization.add(tabFactorizationLearningComboBox, null);
    tabFactorization.add(tabFactorizationMetricsComboBox, null);
    tabFactorization.add(tabFactorizationProcessButton, null);
    tabFactorization.add(tabFactorizationRadioButtonDE, null);
    tabFactorization.add(tabFactorizationRadioButtonLP, null);
    ///////- 4.3.a. Action Listeners
    tabFactorizationKnowledgeButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabFactorizationKnowledgeButton_actionPerformed(e); }
      });

    tabFactorizationProcessButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabFactorizationProcessButton_actionPerformed(e); }
      });

    tabFactorizationLearningComboBox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabFactorizationLearningComboBox_actionPerformed(e); }
      });
    ///////- end 4.3.a
    /////////////////////////////////////////////////////- 4.3 End tabFactorization
    /////////////////////////////////////////////////////- 5. Initialize panelPostLearning
    jTabbedPane4.setBounds(new Rectangle(5, 5, 550, 215));
    jTabbedPane4.setTabPlacement(JTabbedPane.BOTTOM);
    jTabbedPane4.addTab(localize(dialogStrings,"DataBaseMonitor.PostLearning.test.tab"), tabTest);
    /////////////////////////////////////////////////////- 5.. End panelPostLearning
    /////////////////////////////////////////////////////- 5.1 Initialize tabTest
    tabTest.setLayout(null);
    tabTestClassifierLabel.setBounds(new Rectangle(30, 5, 170, 25));
    tabTestClassifierLabel.setText(localize(dialogStrings,"DataBaseMonitor.Test.SelecClassifierLabel"));
    tabTestClassifierComboBox.setBounds(new Rectangle(210, 5, 320, 25));
    tabTestRadioButtonValidation.setBounds(new Rectangle(10, 40, 155, 25));
    tabTestRadioButtonValidation.setText(localize(dialogStrings,"DataBaseMonitor.Test.CvLabel"));
    tabTestRadioButtonCategorize.setBounds(new Rectangle(10, 120, 195, 25));
    tabTestRadioButtonCategorize.setText(localize(dialogStrings,"DataBaseMonitor.Test.CategorizeLabel"));
    tabTestRadioButtonTest.setBounds(new Rectangle(210, 120, 205, 25));
    tabTestRadioButtonTest.setText(localize(dialogStrings, "DataBaseMonitor.Test.TestLabel"));
    tabTestButtonGroup.add(tabTestRadioButtonValidation);
    tabTestButtonGroup.add(tabTestRadioButtonCategorize);
    tabTestButtonGroup.add(tabTestRadioButtonTest);
    tabTestRadioButtonValidation.setSelected(true);
    tabTestProcessButton.setText(localize(dialogStrings, "DatabaseMonitor.Process"));
    tabTestProcessButton.setBounds(new Rectangle(455, 95, 90, 80));
    tabTestProcessButton.setIcon(new ImageIcon(imgPath + "gear.gif"));
    tabTestProcessButton.setHorizontalTextPosition(SwingConstants.CENTER);
    tabTestProcessButton.setVerticalTextPosition(SwingConstants.BOTTOM);
    tabTestOutFileBrowseButton.setBounds(new Rectangle(325, 150, 105, 25));
    tabTestOutFileBrowseButton.setText(localize(dialogStrings, "DataBaseMonitor.Browse.label"));
    tabTestFileText.setText("[none]");
    tabTestFileText.setBounds(new Rectangle(10, 150, 310, 25));
    tabTestFileText.setEnabled(false);
    tabTestParameterK.setText(localize(dialogStrings,"DataBaseMonitor.Test.kParameterLabel"));
    tabTestParameterK.setBounds(new Rectangle(35, 68, 95, 25));
    tabTestTextField.setBounds(new Rectangle(120, 70, 25, 20));
    tabTestTextField.setErrorMessage(localize(dialogStrings, "DataBaseMonitor.Test.errorK"));
    
    tabTest.add(tabTestClassifierLabel, null);
    tabTest.add(tabTestClassifierComboBox, null);
    tabTest.add(tabTestRadioButtonValidation, null);
    tabTest.add(tabTestRadioButtonCategorize, null);
    tabTest.add(tabTestRadioButtonTest, null);
    tabTest.add(tabTestProcessButton, null);
    tabTest.add(tabTestFileText, null);
    tabTest.add(tabTestOutFileBrowseButton, null);
    tabTest.add(tabTestTextField, null);
    tabTest.add(tabTestParameterK, null);
    
    ///////- 5.1.a. Action Listeners
    tabTestOutFileBrowseButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e){
          tabTestOutFileBrowseButton_actionPerformed(e); }
      });
    tabTestRadioButtonValidation.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { controlTestGUI(true); }
      });     
    tabTestRadioButtonCategorize.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { controlTestGUI(true); }
      });        
    tabTestRadioButtonTest.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { controlTestGUI(true); }
      });
    tabTestProcessButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        { tabTestProcessButton_actionPerformed(e); }
      });
    ///////- end 5.1.a     
    /////////////////////////////////////////////////////- 5.1 End tabTest

    //Timer definition
    timer = new Timer(ONE_SECOND, new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          timeElapsed++;
          enableProgressBar(true);
          jProgressBar1.setString("Wait please ...  " + timeElapsed +" sec. elapsed"+
                                                    " - Actual task: "+worker.getActualWork());
          switch(taskToDo){
            case 1:
            {
              if (worker.done()) {
                timer.stop();
                guiComponentStatus(true);
                enableProgressBar(false);
                DataBaseCases cases = (DataBaseCases)worker.getResult();
                JOptionPane.showMessageDialog(getActiveDialog() ,"File "+
                                            fileDialog.getSelectedFile().getName() +
                                              " loaded in "+timeElapsed+" seconds", "Done",1);
                timeElapsed=0;
                taskToDo=0;
              }
              break;
            }//end case1
            case 2:
            {
              if (worker.done()) {
                timer.stop();
                timeElapsed=0;
                enableProgressBar(false);
                taskToDo=0;
              }
              break;
            }//end case2
            case 3:  //Filter metrics
            {
              if (worker.done()) {
                timer.stop();
                timeElapsed=0;
                enableProgressBar(false);
                guiComponentStatus(true);
                taskToDo=0;
              }
              break;
            }//end case3
            case 4:  //Masive Discretization
            {
              if (worker.done()) {
                timer.stop();
                timeElapsed=0;
                enableProgressBar(false);
                guiComponentStatus(true);
                taskToDo=0;
              }
              break;
            }//end case4
            case 5:  //Load variables and create the Discretization instance
            {
              if (worker.done()) {
                timer.stop();      
                timeElapsed=0;
                enableProgressBar(false);
                //guiComponentStatus(true);
                taskToDo=0;
              }
              break;
            }//end case5
            case 6:  //Normal Discretization
            {
              if (worker.done()) {
                timer.stop();
                timeElapsed=0;
                enableProgressBar(false);
                guiComponentStatus(true);
                taskToDo=0;
              }
              break;
            }//end case6
            case 7:  //Imputation
            {
              if (worker.done()) {
                timer.stop();
                timeElapsed=0;
                enableProgressBar(false);
                guiComponentStatus(true);
                taskToDo=0;
              }
              break;
            }//end case7
            case 8:  //Clustering
            {
              if (worker.done()) {
                timer.stop();
                timeElapsed=0;
                enableProgressBar(false);
                guiComponentStatus(true);
                taskToDo=0;
                //Update the classifier ComboBox
                tabTestInitializeComboBox();
              }
              break;
            }//end case8   
            case 9:  // Supervised learning
            {
              if (worker.done()) {
                timer.stop();      
                timeElapsed=0;
                enableProgressBar(false);                
                guiComponentStatus(true);
                taskToDo=0;
                //Update the classifier ComboBox
                tabTestInitializeComboBox();
              }
              break;
            }//end case9   
            case 10:  // CrossValidation
            {
              if (worker.done()) {
                timer.stop();      
                timeElapsed=0;
                enableProgressBar(false);                
                guiComponentStatus(true);
                taskToDo=0;
              }
              break;
            }//end case10   
            case 11:  // Categorization
            {
              if (worker.done()) {
                timer.stop();      
                timeElapsed=0;
                enableProgressBar(false);                
                guiComponentStatus(true);
                taskToDo=0;
              }
              break;
            }//end case11
            case 12:  // Test supervised classifier
            {
              if (worker.done()) {
                timer.stop();      
                timeElapsed=0;
                enableProgressBar(false);                
                guiComponentStatus(true);
                taskToDo=0;
              }
              break;
            }//end case12
            case 13:  // Initialize tabFactorizationKnowledgeFrame
            {
              if (worker.done()) {
                timer.stop();      
                timeElapsed=0;
                enableProgressBar(false);                
                guiComponentStatus(true);
                taskToDo=0;
              }
              break;
            }//end case13
            case 14:  // Factorization of joint probabilities
            {
              if (worker.done()) {
                timer.stop();
                timeElapsed=0;
                enableProgressBar(false);
                guiComponentStatus(true);
                taskToDo=0;
              }
              break;
            }//end case14

          }//end switch
        }
    });//end timer definition

    //--- Pre-initializations of the components
    currentMonitorDialog = (JDialog)this;

    this.updateFilterExplanationPanel(mathPath+"mutualinf.jpg");
    this.enableFilterSaving(false);
    this.controlImputationGUI(true);
    this.controlUnsupervisedGUI(true);
    this.controlSupervisedGUI(true);
    this.tabTestInitializeComboBox();
    this.controlTestGUI(true);
    this.controlFactorizationGUI(true);
    //----

  }//end jbInit()

  private File lastVisitedDirectory;
  
  /**
 * Shows a FileChooser dialog with the dbc file filter activated
 */
  private void selecFile_actionPerformed(ActionEvent e)
  {
 	  fileDialog.setDataBaseFilter();
 	  
 	  	if (lastVisitedDirectory==null)
 	  	{
 	  		fileDialog.rescanCurrentDirectory();
 	  	}
 	  	else
 	  	{
 	  		fileDialog.setCurrentDirectory(lastVisitedDirectory);
 	  	}
 	  	
		fileDialog.setDialogType(fileDialog.OPEN_DIALOG);
		fileDialog.setDialogTitle(localize(dialogStrings,"DataBaseMonitor.FileChooser.title"));
  	fileDialog.setSelectedFile(new File(""));
		int state = fileDialog.showDialog(this.getActiveDialog(),
                        localize(dialogStrings,"DataBaseMonitor.Browse.label"));
    if (state == fileDialog.CANCEL_OPTION)
    { fileDialog.setSelectedFile(null); jTextField1.setText("[none]");}  
    else 
      jTextField1.setText(fileDialog.getSelectedFile().getPath());
      lastVisitedDirectory=fileDialog.getCurrentDirectory();
    //Initialize the KnowledgeFrame 
    this.tabFactorizationKnowledgeFrame = null;
    
  }//end selecFile_actionPerformed(ActionEvent e)

/**
 * Testing method of a dbc loading into memory
 */
  private void loadDBC_actionPerformed(ActionEvent e)
  {
    if ((fileDialog.getSelectedFile() != null) && (fileDialog.getSelectedFile().getName().endsWith(".dbc")))
    {
      guiComponentStatus(false);
      try{
        taskToDo = 1; //Only dbc loading
        Vector p = new Vector(1);
        p.addElement(fileDialog.getSelectedFile().getPath());
        worker = new DataBaseMonitorWorker(this, taskToDo, p);
        worker.go();
        timer.start();        
      }catch(Exception exception){System.out.println("Error: "+e.toString());}
    }
    else
      messageNoDBCSelected();    
  }//end tabFilterOutFileBrowseButton_actionPerformed(ActionEvent)

/**
 * Closes the active dialog if any task is pendient of being done
 */
  private void cancel_actionPerformed(ActionEvent e)
  {
    if (taskToDo == 0)
      this.dispose();
  }//end cancel_actionPerformed(ActionEvent)

/**
 * Activates the visual characteristics of the progress bar that monitorizes the
 * process of a launched task
 */
  private void enableProgressBar(boolean state)
  {
    jProgressBar1.setIndeterminate(state);
    jProgressBar1.setVisible(state);
    jProgressBar1.setStringPainted(state);
  }//end enableProgressBar(boolean)

/**
 * Shows an error message
 */
  public void messageNoSupervisedClassifierSelected()
  {
    JOptionPane.showMessageDialog(getActiveDialog() , localize(dialogStrings,"DataBaseMonitor.Test.noClassError"), "Error",0);
  }//end messageNoSupervisedClassifierSelected()

/**
 * Shows an error message
 */
  private void messageNoClassifierSelected()
  {
    JOptionPane.showMessageDialog(getActiveDialog(),
                        localize(dialogStrings,"DataBaseMonitor.Test.noLearnt"), "[No classifier]",0);
  }//end messageNoClassifierSelected()

/**
 * Shows an error message
 */
  private void messageNoDBCSelected()
  {
    JOptionPane.showMessageDialog(getActiveDialog() , 
                        localize(dialogStrings,"DataBaseMonitor.NoDBC.text"), "[No file]",0);
  }//end messageNoDBCSelected()

/**
 * Shows an error message
 */
  private void messageNoOutDBCSelected()
  {
    JOptionPane.showMessageDialog(getActiveDialog() , 
                        localize(dialogStrings,"DataBaseMonitor.NoOutDBC.text"), "[No file]",0);
  }//end messageNoDBCSelected()

/**
 * Shows an error message
 */
  private void messageNoCategorizeDBCSelected()
  {
    JOptionPane.showMessageDialog(getActiveDialog() ,
                        "Seleccione un fichero de casos a categorizar", "[No file]",0);
  }//end messageNoCategorizeDBCSelected()

/**
 * Shows an error message
 */
  private void messageNoTestDBCSelected()
  {
    JOptionPane.showMessageDialog(getActiveDialog() ,
                        "Seleccione un fichero de casos de entrenamiento", "[No file]",0);
  }//end messageNoTestDBCSelected()

/**
 * Enables / disables the graphical components
 *
 * @param b <code>true</code> - enables the components; <code>false</code> - disables the components
 */
  private void guiComponentStatus(boolean b)
  {
      selecButton.setEnabled(b);
      cancelButton.setEnabled(b);
      jLabel1.setEnabled(b);
      jTabbedPane1.setEnabled(b);
      jTabbedPane3.setEnabled(b);
      jTabbedPane2.setEnabled(b);
      tabFilterProcessButton.setEnabled(b);
      tabFilterComboBox.setEnabled(b);
      tabFilterCheckBox.setEnabled(b);
      tabFilterMainLabel.setEnabled(b);
      if (tabFilterCheckBox.isSelected())
        { tabFilterVarsLabel.setEnabled(b);
          tabFilterPathLabel.setEnabled(b); tabFilterOutFileBrowseButton.setEnabled(b);}
      tabFilterVarsTextField.setEnabled(b); 
      tabFilterCheckBox1.setEnabled(b);
      if (b) this.enableFilterSaving(tabFilterCheckBox.isSelected());
      panelMLearning.setEnabled(b);
      panelPreprocessing.setEnabled(b);
      panelPostLearning.setEnabled(b);

      for (int i=0; i<tabDiscretization.getComponents().length; i++)
        tabDiscretization.getComponents()[i].setEnabled(b);
      tabDiscretizationFileText.setEnabled(false);

      if ( tabDiscretizationComboBox.getSelectedIndex() != 2 && b) tabDiscretizationFileText2.setEnabled(!b);
      if ( tabDiscretizationComboBox.getSelectedIndex() == 3 ) tabDiscretizationFileText1.setEnabled(false);
      this.controlMassiveDiscretizationGUI(tabDiscretizationRadioButtonMasive.isSelected());
      this.controlNormalDiscretizationGUI(tabDiscretizationRadioButtonNormal.isSelected());
      //this.controlNormalDiscretizationGUI(b);

      tabImputationMainLabel.setEnabled(b);
      this.controlImputationGUI(b);
      this.controlUnsupervisedGUI(b);
      this.controlSupervisedGUI(b);
      this.controlTestGUI(b);
      this.controlFactorizationGUI(b);
  }//end guiComponentStatus(boolean)

/**
 * Finds a string in a ResourceBudle object of the Elvira gui
 *
 * @param bundle the bundle object for seeking into
 * @param name the name of the desired label
 * @return the complete string in the actual loaded language
 */
  private String localize (ResourceBundle bundle, String name)
  {
    return Elvira.getElviraFrame().localize(bundle, name);
  }//end localize (ResourceBundle, String)

/**
 * Returns the active dialog, use this method in order to invoke any specific method of
 * the dialog
 *
 * @return  the active dialog
 */
  public JDialog getActiveDialog()
  {
    return this;
  }//end getActiveDialog()

/**
 *
 *
 */
 public void displayLearntBnet(NetworkFrame netFrame, String title)
 {
    Bnet net = netFrame.getEditorPanel().getBayesNet();
   //Positionate the nodes
    int width = Elvira.getElviraFrame().getWidth();
    int height = Elvira.getElviraFrame().getDesktopPane().getHeight();
    int nodesRow = width/128;
    int numRows = net.getNodeList().size()/nodesRow;
    if ((net.getNodeList().size() % nodesRow) != 0) numRows++;

    for (int i=0; i < numRows-1; i++)
    {
      for (int j=0; j < nodesRow; j++)
      {
        net.getNodeList().elementAt((i*nodesRow)+j).setPosY((i*50)+50);
        net.getNodeList().elementAt((i*nodesRow)+j).setPosX((128*j)+50);
      }
    }
    //Nodes that cannot have been positioned until now
    int j=0;
    for (int k=(numRows-1)*nodesRow; k < net.getNodeList().size(); k++)
    {
      net.getNodeList().elementAt(k).setPosY((numRows*50));
      net.getNodeList().elementAt(k).setPosX((128*j)+50);
      j++;
    }

    //Update the elvira UI
    netFrame.getEditorPanel().setModifiedNetwork(true);
    Elvira.getElviraFrame().createNewFrame(title, false);
    Elvira.getElviraFrame().setCurrentNetworkFrame(netFrame);

    for (int i=0; i<net.getNodeList().size(); i++) {
      Node n = net.getNodeList().elementAt(i);
      n.setFont("Helvetica");
      FontMetrics fm=Elvira.getElviraFrame().getFontMetrics(ElviraPanel.getFont(n.getFont()));
      VisualNode.setAxis(n, n.getNodeString(true),fm);
    }
    ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().setBayesNet(net);
    ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().refreshElviraPanel(1.0);
    ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().load(net);
    Elvira.getElviraFrame().setTitle("Elvira - "+ title);

 }//end displayLearntBnet(Bnet, String)

/**
 * Redisplays a dbc file in the current active frame in an ordered form
 *
 * @param net a dbc structure casted into a Bnet variable
 * @param nameOfFile the name from which the data base has been loaded
 */
  public void displayDBC(Bnet net, String nameOfFile)
  {
    //Information Panel
    StyleContext sc = new StyleContext();
    Style heading = sc.addStyle("Heading", null);
    heading.addAttribute(StyleConstants.Foreground, Color.black);
    heading.addAttribute(StyleConstants.FontSize, new Integer(16));
    heading.addAttribute(StyleConstants.FontFamily, "Helvetica");
    heading.addAttribute(StyleConstants.Bold, new Boolean(true));

    DefaultStyledDocument info = new DefaultStyledDocument(sc);
    JTextPane pane = new JTextPane(info);
    try{
      info.insertString(0,"File name: "+nameOfFile+"\nNumber of cases: "+
      ((CaseListMem)((Relation)net.getRelationList().elementAt(0)).getValues()).getCases().size()+
      "\nNumber of variables: "+net.getNodeList().size(),null);
      info.setParagraphAttributes(0,1,heading, false);
    }
    catch (Exception e){}
    pane.setBackground(new Color(204,204,204));
    pane.setEditable(false);
    Elvira.getElviraFrame().getCurrentNetworkFrame().getContentPane().add(pane, BorderLayout.NORTH);
    Elvira.getElviraFrame().getCurrentNetworkFrame().updateUI();
    //----

    int width = Elvira.getElviraFrame().getWidth();
    int height = Elvira.getElviraFrame().getDesktopPane().getHeight();
    int nodesRow = width/128;
    int numRows = net.getNodeList().size()/nodesRow;
    if ((net.getNodeList().size() % nodesRow) != 0) numRows++;

    for (int i=0; i < numRows-1; i++)
    {
      for (int j=0; j < nodesRow; j++)
      {
        net.getNodeList().elementAt((i*nodesRow)+j).setPosY((i*50)+50);
        net.getNodeList().elementAt((i*nodesRow)+j).setPosX((128*j)+50);
      }
    }
    //Nodes that cannot have been positioned until now
    int j=0;
    for (int k=(numRows-1)*nodesRow; k < net.getNodeList().size(); k++)
    {
      net.getNodeList().elementAt(k).setPosY((numRows*50));
      net.getNodeList().elementAt(k).setPosX((128*j)+50);
      j++;
    }
    ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().refreshElviraPanel(1.0);
    ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().repaint();
  }//end displayDBC(Bnet, String)

/**
 * Preprocess -> Filter Measures -> Save DBC proyection activation checkbox
 */
  private void tabFilterCheckBox_actionPerformed(ActionEvent e)
  {
    this.enableFilterSaving(tabFilterCheckBox.isSelected());
  }//end tabFilterCheckBox_actionPerformed(ActionEvent)

  private void enableFilterSaving(boolean b)
  {
      tabFilterVarsLabel.setEnabled(b);
      tabFilterPathLabel.setEnabled(b);
      tabFilterOutFileBrowseButton.setEnabled(b);
      tabFilterVarsTextField.setEnabled(b);
      tabFilterCheckBox1.setEnabled(b);
      if (b && (tabFilterComboBox.getSelectedIndex() == 7))
        {tabFilterVarsTextField.setEnabled(false);
         tabFilterVarsLabel.setEnabled(false);
         tabFilterCheckBox1.setEnabled(false);}
      if (tabFilterCheckBox1.isSelected()){
        tabFilterVarsLabel.setEnabled(false);
        tabFilterVarsTextField.setEnabled(false);
      }         
  }//end enableFilterSaving(boolean)

/**
 * Preprocess -> Filter Measures -> Select Path
 */
  private void tabFilterOutFileBrowseButton_actionPerformed(ActionEvent e)
  {
    ElviraFileChooser fileCh = new ElviraFileChooser(System.getProperty("user.dir"));

 	  fileCh.setDataBaseFilter();
		
	  if (lastVisitedDirectory==null)
 	  {
 	  	fileCh.rescanCurrentDirectory();
 	  }
 	  else
 	  {
 	  	fileCh.setCurrentDirectory(lastVisitedDirectory);
 	  }

 	  fileCh.setDialogType(fileDialog.OPEN_DIALOG);
  	fileCh.setSelectedFile(new File("newfile.dbc"));
		int state = fileCh.showDialog(this.getActiveDialog(),
                        localize(dialogStrings,"DataBaseMonitor.Browse.label"));
    if (state == fileDialog.CANCEL_OPTION)
      fileCh.setSelectedFile(null);
    else
      tabFilterPathTextField.setText(fileCh.getSelectedFile().getPath());
      lastVisitedDirectory=fileCh.getCurrentDirectory();
  }//end tabFilterOutFileBrowseButton_actionPerformed(ActionEvent)

/**
 * Preprocess -> Filter Measures -> Process
 */
  private void tabFilterProcessButton_actionPerformed(ActionEvent e)
  {
    // Preparing the invocation
    Vector parameters = new Vector();
    boolean flag = true;
    //Verify that is a dbc file selected
    if ((fileDialog.getSelectedFile() != null) && (fileDialog.getSelectedFile().getName().endsWith(".dbc")))
    {
      parameters.add(fileDialog.getSelectedFile());
      parameters.add(new Integer(tabFilterComboBox.getSelectedIndex()));
      if (tabFilterCheckBox.isSelected())
      {
        if (!tabFilterCheckBox1.isSelected() && tabFilterComboBox.getSelectedIndex() != 7){
          if ( tabFilterVarsTextField.getField().equals("")) flag = false;
          parameters.add(tabFilterVarsTextField.getText()); //# of variables      
        }
        else
          parameters.add("0"); //Automatic determination of the vblas              
        String name = tabFilterPathTextField.getText();
        if ( name.equals("") || !name.endsWith(".dbc") ){ messageNoOutDBCSelected(); flag = false;}
        else
          parameters.add(name);
      }
    }
    else { messageNoDBCSelected(); flag = false; }
      
    if (flag)  // Task invocation
    {  
      guiComponentStatus(false);
      try{
        taskToDo = 3; //Filter task
        worker = new DataBaseMonitorWorker(this, taskToDo, parameters);
        worker.go();
        timer.start();
      }catch(Exception exception){System.out.println("Error: "+e.toString());}
    }
  }//end tabFilterProcessButton_actionPerformed(ActionEvent)
  
/**
 * Preprocess -> Filter Measures -> Changing filter measure
 */
  private void tabFilterComboBox_actionPerformed(ActionEvent e)
  {
    enableFilterSaving(tabFilterCheckBox.isSelected());
    switch (tabFilterComboBox.getSelectedIndex())
    {
      case 0:{ this.updateFilterExplanationPanel(mathPath+"mutualinf.jpg");
        break; }
      case 1:{ this.updateFilterExplanationPanel(mathPath+"euclidean.jpg");
        break; }
      case 2:{ this.updateFilterExplanationPanel(mathPath+"matusita.jpg");
        break; }
      case 3:{ this.updateFilterExplanationPanel(mathPath+"kl1.jpg");
        break; }
      case 4:{ this.updateFilterExplanationPanel(mathPath+"kl2.jpg");
        break; }
      case 5:{ this.updateFilterExplanationPanel(mathPath+"shannon.jpg");
        break; }
      case 6:{ this.updateFilterExplanationPanel(mathPath+"bhatta.jpg");
        break; }      
      case 7:{ 
        if ( Elvira.getLanguaje() == Elvira.AMERICAN )
          this.updateFilterExplanationPanel(mathPath+"cfsEN.jpg");
        else
          this.updateFilterExplanationPanel(mathPath+"cfsES.jpg");        
        break; }      
    }//end switch
    
  }//end tabFilterComboBox_actionPerformed

/**
 * 
 */
 private void updateFilterExplanationPanel(String name)
 {
   ImageIcon icon = new ImageIcon(name);
   ScrollablePicture pict = new ScrollablePicture(icon, 1);
   tabFilterScrollPanel.getViewport().add(pict);
 }//end updateFilterWxplanationPanel

/**
 * Format of the Vector of results :
 * pos 0 -> name of the source dbc
 * pos 1 -> name of the filter measure applied
 * pos 2 -> number of variables in the source dbc
 * pos 3 -> number of cases in the source dbc
 * pos 4 -> name of the output dbc file
 * pos 5 -> number of variables of the proyection
 * 
 * pos 6 -> name of the first variable
 * pos 7 -> filter result of the first variable (pos 3)
 * 
 * and so on ...
 * 
 */
 public void displayFilterResults(Vector res)
 {
    //Information Panel
    StyleContext sc = new StyleContext();
    Style heading = sc.addStyle("Heading", null);
    heading.addAttribute(StyleConstants.Foreground, Color.black);
    heading.addAttribute(StyleConstants.FontSize, new Integer(14));
    heading.addAttribute(StyleConstants.FontFamily, "Helvetica");
    heading.addAttribute(StyleConstants.Bold, new Boolean(true));

    DefaultStyledDocument info = new DefaultStyledDocument(sc);
    JTextPane pane = new JTextPane(info);
    pane.setBackground(new Color(204,204,204));
    pane.setEditable(false);
    try{
      info.insertString(0," "+localize(dialogStrings,"DataBaseMonitor.Filter.results.file")+" "+(String)res.get(0)
                                   +"\n "+localize(dialogStrings,"DataBaseMonitor.Filter.results.label")+" "+(String)res.get(1)
                                   +"\n "+localize(dialogStrings,"DataBaseMonitor.Filter.results.cases")+" "+(String)res.get(3)
                                   +"\n "+localize(dialogStrings,"DataBaseMonitor.Filter.results.vars")+" "+(String)res.get(2)
                                   +"\n\n "+localize(dialogStrings,"DataBaseMonitor.Filter.results.outfile")+" "+ (String)res.get(4)
                                   +"\n "+localize(dialogStrings,"DataBaseMonitor.Filter.results.includevars")+" " +(String)res.get(5)
                                   +"\n\n "+localize(dialogStrings,"DataBaseMonitor.Filter.results.var")+"                             "
                                   +localize(dialogStrings,"DataBaseMonitor.Filter.results.metric"), null);
      info.setParagraphAttributes(0,1,heading, false);      
    }catch (Exception e){}

    JSeparator line = new JSeparator(SwingConstants.HORIZONTAL);

    JTextArea pane2 = new JTextArea(0,0);
    pane2.setFont(new Font("Courier", Font.PLAIN, 13));
    
    //Display the results in the textarea
    char[] lineOut = new char[45];
    for (int i=6; i<res.size(); i=i+2)
    {
      for (int aux=0; aux< lineOut.length; aux++) lineOut[aux]= ' ';
      String node = (String)res.get(i);
      node.getChars(0, node.length(), lineOut,0);      
      String value = ((Double)res.get(i+1)).toString();
      value.getChars(0,value.length(),lineOut,20);

      pane2.append(" "+new String(lineOut)+"\n");
    }
    
    pane2.setEditable(false);
    JScrollPane scroll = new JScrollPane(pane2);
    scroll.setAutoscrolls(true);

    JButton close = new JButton(localize(dialogStrings,"OK.label"));
    JPanel paneAux = new JPanel();
    paneAux.add(close);

    final JDialog dialog = new JDialog(getActiveDialog(), localize(dialogStrings,"DataBaseMonitor.Filter.results.title"), true);
    //dialog.setResizable(false);
    dialog.setResizable(true);
    dialog.setBounds(300, 200, 400, 350);
    dialog.getContentPane().add(pane, BorderLayout.NORTH);
    dialog.getContentPane().add(scroll, BorderLayout.CENTER);
    dialog.getContentPane().add(paneAux, BorderLayout.SOUTH);
    close.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          dialog.dispose();
        }
      });

    dialog.show();

 }//end displayFilterResults(Vector)

/**
 * 
 */
 public String[] getFilterMeasuresOptions ()
 {
   return this.filterOptions;
 }//end getFilterMeasuresOptions()

/**
 * Imputation -> Browse out file
 */
  private void tabImputationOutFileBrowseButton_actionPerformed(ActionEvent e)
  {
    ElviraFileChooser fileCh = new ElviraFileChooser(System.getProperty("user.dir"));

 	  fileCh.setDataBaseFilter(); 
 	  
	  	if (lastVisitedDirectory==null)
 	  	{
 	  		fileCh.rescanCurrentDirectory();
 	  	}
 	  	else
 	  	{
 	  		fileCh.setCurrentDirectory(lastVisitedDirectory);
 	  	}

		fileCh.setDialogType(fileDialog.OPEN_DIALOG);
  	fileCh.setSelectedFile(new File("newfile.dbc"));
		int state = fileCh.showDialog(this.getActiveDialog(), localize(dialogStrings,"DataBaseMonitor.Browse.label"));
    if (state == fileDialog.CANCEL_OPTION)
      fileCh.setSelectedFile(null);
    else{
      tabImputationFileText.setText(fileCh.getSelectedFile().getPath());
      lastVisitedDirectory=fileCh.getCurrentDirectory();
    }
  }//end tabImputationOutFileBrowseButton_actionPerformed(ActionEvent)

/**
 * Imputation -> ProcessButton
 */
  private void tabImputationProcessButton_actionPerformed(ActionEvent e)
  {
    // Preparing the invocation
    Vector parameters = new Vector();
    boolean flag = true;
    //Verify that is a dbc file selected
    if ((fileDialog.getSelectedFile() != null) && (fileDialog.getSelectedFile().getName().endsWith(".dbc")))
    {
      parameters.add(fileDialog.getSelectedFile());

      //2.- Verifiy there is an output file selected
       String name = tabImputationFileText.getText();
      if ( name.equals("") || !name.endsWith(".dbc") ){ messageNoOutDBCSelected(); flag = false;}
      else parameters.add(name);

      int alg = tabImputationComboBox.getSelectedIndex();
      parameters.add(new Integer(alg));
      //Extra parameters
      if (alg == 2)
        {
          String classNode = tabImputationTextField.getField();
          if ( classNode =="" ) flag=false;
          else
          {
            Integer noNode = new Integer(classNode);
            if ( noNode.intValue() < 1 )
            {
             JOptionPane.showMessageDialog(getActiveDialog(),
                       localize(dialogStrings, "DataBaseMonitor.Imputation.errorClassVar"), "Error",0);
             flag=false;
            }
            parameters.add(noNode);
          }
        }
      else if (alg == 3)
        parameters.add(new Integer(tabImputationComboBox1.getSelectedIndex()));
      else if (alg == 4)
  		{
        String noIterString = tabImputationTextField1.getField();
        if ( noIterString =="" ) flag=false;
        else
        {
          Integer noIter = new Integer(noIterString);
          if ( noIter.intValue() < 1 )
          {
            JOptionPane.showMessageDialog(getActiveDialog(),
                localize(dialogStrings, "DataBaseMonitor.Imputation.errorNoIterations"), "Error",0);
            flag=false;
          }
          parameters.add(noIter);
        }

	    }
      else if (alg == 5)
        parameters.add(new Integer(tabImputationComboBox1.getSelectedIndex()));
    }
    else { messageNoDBCSelected(); flag = false; }

    if (flag)  // Task invocation
    {
      this.guiComponentStatus(false);
      try{
        taskToDo = 7; //Imputation task
        worker = new DataBaseMonitorWorker(this, taskToDo, parameters);
        worker.go();
        timer.start();
      }catch(Exception exception){System.out.println("Error: "+e.toString());}
    }
  }//end tabImputationProcessButton_actionPerformed(ActionEvent)

/**
 * Imputation -> Main ComboBox action performed
 */
  private void tabImputationComboBox_actionPerformed(ActionEvent e)
  {
    this.controlImputationGUI(true);
    //Change the text component
    switch (tabImputationComboBox.getSelectedIndex())
    {
      case 0: {
        tabImputationTextPane.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.ZerosExplanation"));
        break; }
      case 1: {
        tabImputationTextPane.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.AverageExplanation"));
        break; }
      case 2: {
        tabImputationTextPane.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.ClassNodeExplanation"));
        break; }
      case 3: {
        tabImputationTextPane.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.TreeExplanation"));
        break; }
      case 4: {
        tabImputationTextPane.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.ITER_MPEExplanation"));
        break; }
      case 5: {
        tabImputationTextPane.setText(localize(dialogStrings, "DataBaseMonitor.Imputation.INCR_MPEExplanation"));
        break; }
    }
  }//end tabImputationComboBox_actionPerformed(ActionEvent)

/**
 * Imputation GUI components logical control
 */
  private void controlImputationGUI(boolean b)
  {
    if (b)
    {
      tabImputationLabel2.setEnabled(false); tabImputationTextField.setEnabled(false);
      tabImputationLabel3.setEnabled(false); tabImputationTextField.setEnabled(false);
      tabImputationLabel4.setEnabled(false); tabImputationTextField1.setEnabled(false);
      tabImputationComboBox1.setEnabled(false);
      if (tabImputationComboBox.getSelectedIndex() == 2){
          tabImputationLabel2.setEnabled(true); tabImputationTextField.setEnabled(true);}
      else if (tabImputationComboBox.getSelectedIndex() == 3){
          tabImputationLabel3.setEnabled(true); tabImputationComboBox1.setEnabled(true);}
 	    else if (tabImputationComboBox.getSelectedIndex() == 4){
         	tabImputationLabel4.setEnabled(true); tabImputationTextField1.setEnabled(true);}
    }
    else //Desactivate components
    {
      tabImputationLabel2.setEnabled(false); tabImputationTextField.setEnabled(false);
      tabImputationLabel3.setEnabled(false); tabImputationTextField.setEnabled(false);
      tabImputationLabel4.setEnabled(false); tabImputationTextField1.setEnabled(false);
      tabImputationComboBox1.setEnabled(false);
    }
    tabImputationOutFileBrowseButton.setEnabled(b);
    tabImputationProcessButton.setEnabled(b);
    tabImputationComboBox.setEnabled(b);
    tabImputationTextPane.setEnabled(b);
    tabImputationLabel1.setEnabled(b);
  }//end controlImputationGUI(boolean)

/**
 * Changes the value of the class number textfield
 */
  public void tabImputationClassNumberControl(int no)
  {
    tabImputationTextField.setText(new Integer(no).toString());
  }//end tabIMputationClassNumberControl(int)

/**
 * Changes the value of the iterations number textfield
 */
  public void tabImputationIterationsNumberControl(int no)
  {
    tabImputationTextField1.setText(new Integer(no).toString());
  }//end tabIMputationIterationsNumberControl(int)

/**
 * Shows a validation message for the imputation
 */
  public void messageImputationDone()
  {
    JOptionPane.showMessageDialog(getActiveDialog(),
                      localize(dialogStrings,"DataBaseMonitor.Imputation.ok"), "OK",1);
  }//end messageImputationDone()

/**
 * Discretization -> Process
 */
 private void tabDiscretizationProcessButton_actionPerformed(ActionEvent e)
 {
    Vector parameters = new Vector();
    boolean flag = true;
    boolean normal =false;
    
   //1.- Verify there is a input file selected or loaded
    if ((fileDialog.getSelectedFile() != null) && (fileDialog.getSelectedFile().getName().endsWith(".dbc")))
    {
       parameters.add(fileDialog.getSelectedFile());
       String name = tabDiscretizationFileText.getText();
       //2.- Verifiy there is an output file selected
       if ( name.equals("") || !name.endsWith(".dbc") ){ messageNoOutDBCSelected(); flag = false;}
       else parameters.add(name);
    }
    else { messageNoDBCSelected(); flag = false; }
   //3.- Verifiy that all the parameters are correct
   if (tabDiscretizationRadioButtonMasive.isSelected() && flag) //Massive Discretization 
   {
     parameters.add(new Integer(0));
     String aux = tabDiscretizationFileText1.getField(); int intervals = 0; int alg=tabDiscretizationComboBox.getSelectedIndex();
     if ( ! aux.equals("")) intervals = new Integer(tabDiscretizationFileText1.getText()).intValue();
     if (intervals < 2) flag = false;
     else { parameters.add(new Integer(alg)); parameters.add(new Integer(intervals)); }

     parameters.add(new Double(tabDiscretizationFileText2.getText()));
   }
   else if (tabDiscretizationRadioButtonNormal.isSelected() && flag) //Normal Discretization
   {
    //Storing the values of the last viewed variable, if needed
      int aux = tabDiscretizationLastIndexedNode.intValue() - 1;
      if ((!tabDiscretizationLastIndexedNode.equals(new Integer(0))) && tabDiscretizationCheckBox.isSelected())
      {
        ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).setDiscretizeMark(true);
        ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).setAlgorithm(tabDiscretizationComboBox.getSelectedIndex());
        int aux2 = new Integer(tabDiscretizationFileText1.getText()).intValue();
        ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).setIntervals(aux2);
        double aux3 = new Double(tabDiscretizationFileText2.getText()).doubleValue();
        ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).setOptions(aux3);
      }
      if ((!tabDiscretizationCheckBox.isSelected()) && 
          ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).isMarkDiscretize())
      {
        ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).setDiscretizeMark(false);
      }
      //-
      parameters.add(new Integer(1));
      parameters.add(normalDiscretizeInstance);
      // Parsing the parameters
      // param[4]=numVar, param[5]=alg, param[6]=intervals, param[7]=options
      for (int i=0; i<auxiliarVectorNodes.size(); i++)
      {
        if ( ((AuxiliarDiscretizeNode)auxiliarVectorNodes.elementAt(i)).isMarkDiscretize() )
        {
          parameters.add(new Integer(i));
          parameters.add(new Integer(((AuxiliarDiscretizeNode)auxiliarVectorNodes.elementAt(i)).getAlgorithm()));
          parameters.add(new Integer(((AuxiliarDiscretizeNode)auxiliarVectorNodes.elementAt(i)).getIntervals()));
          Vector opt = new Vector();
          opt.add(new Double( ((AuxiliarDiscretizeNode)auxiliarVectorNodes.elementAt(i)).getOptions() ));
          parameters.add(opt);      
        }
      }
      normal = true;
   }
   //Let's invoke the task
    if (flag && !normal)
    {  
      guiComponentStatus(false);
      try{
        taskToDo = 4; //Massive Discretization
        worker = new DataBaseMonitorWorker(this, taskToDo, parameters);
        worker.go();
        timer.start();        
      }catch(Exception exception){System.out.println("Error: "+e.toString());}
    }   
    if (flag && normal)
    {  
      guiComponentStatus(false);
      try{
        taskToDo = 6; //Massive Discretization
        worker = new DataBaseMonitorWorker(this, taskToDo, parameters);
        worker.go();
        timer.start();        
      }catch(Exception exception){System.out.println("Error: "+e.toString());}
    }   
    
 }//end tabDiscretizationProcessButton_actionPerformed(ActionEvent)

/**
 * Discretization -> Radio Button Massive | Radio Button Normal
 */
 private void tabDiscretizationRadioButton_actionPerformed(ActionEvent e)
 {
   //Enable-Disable the respective elements
   if ( tabDiscretizationRadioButtonMasive.isSelected() ) 
   { controlNormalDiscretizationGUI(false); controlMassiveDiscretizationGUI(true); }
   else 
   {
      if ( (fileDialog.getSelectedFile() != null) && (fileDialog.getSelectedFile().getName().endsWith(".dbc")) )
      {
          controlNormalDiscretizationGUI(true);
          //Update the fields of the variables
          tabDiscretizationLastIndexedNode = new Integer(0);
          tabDiscretizationNumberModel.setValue(new Integer(1));
          normalDiscretizationLogicalControl(fileDialog.getSelectedFile());
      }
      else {messageNoDBCSelected(); tabDiscretizationRadioButtonMasive.setSelected(true);}
   }
 }//end tabDiscretizationRadioButton_actionPerformed(ActionEvent)

/**
 * Update the GUI fields with the correspondient values
 */
 private void updateDiscretizeNormalFields(AuxiliarDiscretizeNode node)
 {
    tabDiscretizationFileText3.setText( node.getName() );
    tabDiscretizationFileText4.setText( node.getType() );

    int alg = node.getAlgorithm();
    tabDiscretizationComboBox.setSelectedIndex( alg );

    String intervals = new Integer(node.getIntervals()).toString();
    if ( !intervals.equals("0")) tabDiscretizationFileText1.setText(intervals);

    boolean marked = node.isMarkDiscretize();
    tabDiscretizationCheckBox.setSelected( marked);
    if ( ! marked ) controlMassiveDiscretizationGUI(false);
    else controlMassiveDiscretizationGUI(true);

 }//end updateDiscretizeNormalFields(AuxiliarDiscretizeNode)

/**
 * 
 */
 private void controlNormalDiscretizationGUI(boolean b)
 {
     tabDiscretizationCheckBox.setEnabled(b); 
     tabDiscretizationSpin.setEnabled(b);
     jLabel6.setEnabled(b); jLabel7.setEnabled(b); jLabel8.setEnabled(b);
     tabDiscretizationFileText3.setEnabled(b); tabDiscretizationFileText4.setEnabled(b);
     
 }//end controlNormalDiscretizationGUI(boolean)

/**
 * 
 */
  private void controlMassiveDiscretizationGUI(boolean b)
  {
    jLabel9.setEnabled(b); jLabel4.setEnabled(b); jLabel5.setEnabled(b);
    tabDiscretizationComboBox.setEnabled(b); tabDiscretizationFileText1.setEnabled(b);
    tabDiscretizationFileText1.setEnabled(b); jLabel4.setEnabled(b);
    tabDiscretizationFileText2.setEnabled(b); jLabel5.setEnabled(b);
    
    switch (tabDiscretizationComboBox.getSelectedIndex()){
      case 2: { tabDiscretizationFileText2.setEnabled(true); jLabel5.setEnabled(true); break;}
      case 3: { tabDiscretizationFileText1.setText("2"); tabDiscretizationFileText1.setEnabled(false);
                    tabDiscretizationFileText2.setEnabled(false); jLabel4.setEnabled(false);
                    jLabel5.setEnabled(false); break; }
      default:
      {
        tabDiscretizationFileText2.setEnabled(false); jLabel5.setEnabled(false);
        break;
      }
    }//ends switch
  }//end controlMassiveDiscretizationGUI(boolean)
  
/**
 *
 */
  private void normalDiscretizationLogicalControl(File dbcFile)
  {
      ///Creates the Discretization object and read the file and gets the values
      try{
        guiComponentStatus(false);
        taskToDo = 5; //Load discretize
        Vector params = new Vector();
        params.add(dbcFile);
        worker = new DataBaseMonitorWorker(this, taskToDo, params);
        worker.go();
        timer.start();        
      }catch(Exception exception){System.out.println("Error: "+exception.toString());}
  }//end normalDiscretizationLogicalControl(File)

/**
 * It's supposed that the GUI controls have been disabled
 */
 public void tabDiscretizationInitializeNormalDiscretization(Discretization discrete)
 {
      normalDiscretizeInstance = discrete;
      // Parsing the values
      Vector  DBCInformation = new Vector();
      DBCInformation = normalDiscretizeInstance.getDBCInformation();
      Integer n_vars = (Integer)DBCInformation.elementAt(1);

      auxiliarVectorNodes = new Vector(n_vars.intValue());
      for (int i=0; i<n_vars.intValue(); i++)
      {
        AuxiliarDiscretizeNode node = new AuxiliarDiscretizeNode();
        node.setName((String)DBCInformation.elementAt(2*i +3));
        switch ( ((Integer)DBCInformation.elementAt(2*i+4)).intValue() )
        {
          case Node.CONTINUOUS: { node.setType("Continuous"); break; }
          case Node.FINITE_STATES: { node.setType("Finite-States"); break; }
          default: node.setType(DBCInformation.elementAt(2*i+4).toString());
        }
        auxiliarVectorNodes.add(node);                                                                                               
      }

      guiComponentStatus(true);
      //This method fires a changeState event, so the normal discrete GUI is controlled implicitly
      tabDiscretizationNumberModel.setMaximum(n_vars);
      //Activate the GUI controls
      //guiComponentStatus(true);
      //controlNormalDiscretizationGUI(true);


  }//end normalDiscretizationLogicalControl()

/**
 * DiscretizationCheckBox changed
 */
  private void tabDiscretizationComboBox_actionPerformed(ActionEvent e)
  {
    this.controlMassiveDiscretizationGUI(true);
  }//end tabDiscretizationComboBox_actionPerformed(ActionEvent)

/**
 * Discretization -> Browse out file
 */
  private void tabDiscretizationOutFileBrowseButton_actionPerformed(ActionEvent e)
  {
    ElviraFileChooser fileCh = new ElviraFileChooser(System.getProperty("user.dir"));

 	  fileCh.setDataBaseFilter(); 
 	  
	  if (lastVisitedDirectory==null)
 	  {
 	  	fileCh.rescanCurrentDirectory();
 	  }
 	  else
 	  {
 	  	fileCh.setCurrentDirectory(lastVisitedDirectory);
 	  }

 	  fileCh.setDialogType(fileDialog.OPEN_DIALOG);
  	fileCh.setSelectedFile(new File("newfile.dbc"));
		int state = fileCh.showDialog(this.getActiveDialog(), localize(dialogStrings,"DataBaseMonitor.Browse.label"));
    if (state == fileDialog.CANCEL_OPTION)
      fileCh.setSelectedFile(null);
    else{
      tabDiscretizationFileText.setText(fileCh.getSelectedFile().getPath());
      lastVisitedDirectory=fileCh.getCurrentDirectory();
    }
  }//end tabDiscretizationOutFileBrowseButton_actionPerformed(ActionEvent)

/**
 * 
 */
  private void tabDiscretizationCheckBox_actionPerformed(ActionEvent e)
  {
    //If this method is invoked we must suppose that a normal discretization is selected
    if (tabDiscretizationCheckBox.isSelected()) //Before it doesn't
      controlMassiveDiscretizationGUI(true);
    else //Before it does
      controlMassiveDiscretizationGUI(false);
  }//end tabDiscretizationCheckBox_actionPerformed(ActionEvent)

/**
 * 
 */
  private void  tabDiscretizationSpin_stateChanged(ChangeEvent e)
  {
      Integer var = (Integer)tabDiscretizationSpin.getValue();
      //Save the params of the last indexed node if the discretization checkbox is selected
      if ((tabDiscretizationLastIndexedNode != var) && (!tabDiscretizationLastIndexedNode.equals(new Integer(0))) && 
           tabDiscretizationCheckBox.isSelected())
      {
        int aux = tabDiscretizationLastIndexedNode.intValue() - 1;
        ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).setDiscretizeMark(true);
        ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).setAlgorithm(tabDiscretizationComboBox.getSelectedIndex());
        int aux2 = new Integer(tabDiscretizationFileText1.getText()).intValue();
        ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).setIntervals(aux2);
        double aux3 = new Double(tabDiscretizationFileText2.getText()).doubleValue();
        ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).setOptions(aux3);
      }

      //If the checkbox is deselected, the variable must be marked as non discretize
      else if ((tabDiscretizationLastIndexedNode != var) && (!tabDiscretizationLastIndexedNode.equals(new Integer(0))) && 
           ! tabDiscretizationCheckBox.isSelected()) 
      {
        int aux = tabDiscretizationLastIndexedNode.intValue() - 1;
        ((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(aux)).setDiscretizeMark(false);
      }
      //Initialize the next node fields
      updateDiscretizeNormalFields((AuxiliarDiscretizeNode)auxiliarVectorNodes.get(var.intValue()-1));
      //Update the global variable
      tabDiscretizationLastIndexedNode = var;      
  }
  
/**
 * Shows a validation message
 */
  public void messageDiscretizationDone()
  {
    JOptionPane.showMessageDialog(getActiveDialog(),
                      localize(dialogStrings,"DataBaseMonitor.Discretization.ok"), "OK",1);
  }//end messageDiscretizationDone()

/**
 * Supervied GUI components logical control
 */
  private void controlSupervisedGUI(boolean b)
  {
    for (int i=0; i<tabSupervised.getComponentCount(); i++)
      tabSupervised.getComponents()[i].setEnabled(b);

    if (0 == tabSupervisedComboBox.getSelectedIndex() && b) //Control NB logic
    {
      if ( 1 == tabSupervisedComboBox1.getItemCount()){
        tabSupervisedComboBox1.insertItemAt(new String("Selective"), 1);
        tabSupervisedComboBox1.insertItemAt(new String("Semi"), 2);}
        
      if (0 == tabSupervisedComboBox1.getSelectedIndex()) //All the variables
      {
        tabSupervisedRadioButtonFilter.setEnabled(false);
        tabSupervisedRadioButtonWrapper.setEnabled(false);
        tabSupervisedRadioButtonUmda.setEnabled(false);
        tabSupervisedRadioButtonGreedy.setEnabled(false);        
        tabSupervisedRadioButton95.setEnabled(false);
        tabSupervisedRadioButton99.setEnabled(false);        
      }
      else //Semi or Selective
      {
        if ( tabSupervisedRadioButtonFilter.isSelected() )
        {
          tabSupervisedRadioButtonUmda.setEnabled(false);
          tabSupervisedRadioButtonGreedy.setEnabled(false);          
        }
        else //Wrapper treatment
        {
          tabSupervisedRadioButton95.setEnabled(false);
          tabSupervisedRadioButton99.setEnabled(false);                    
        }
      }
      tabSupervisedTextField.setEnabled(false);
      tabSupervisedParameterK.setEnabled(false);
    }
    else if ((1 == tabSupervisedComboBox.getSelectedIndex() && b) ||
             (2 == tabSupervisedComboBox.getSelectedIndex() && b) )
    {
      if (3 == tabSupervisedComboBox1.getItemCount())
      {
        tabSupervisedComboBox1.setSelectedIndex(0);
        tabSupervisedComboBox1.removeItem(new String("Selective"));
        tabSupervisedComboBox1.removeItem(new String("Semi"));
      }
      tabSupervisedTextField.setEnabled(false);
      tabSupervisedParameterK.setEnabled(false);
      tabSupervisedRadioButtonFilter.setEnabled(false);
      tabSupervisedRadioButtonWrapper.setEnabled(false);
      tabSupervisedRadioButton95.setEnabled(false);
      tabSupervisedRadioButton99.setEnabled(false);
      tabSupervisedRadioButtonUmda.setEnabled(false);
      tabSupervisedRadioButtonGreedy.setEnabled(false);
      if (2 == tabSupervisedComboBox.getSelectedIndex() && b) //KDB
      {
        tabSupervisedTextField.setEnabled(true);
        tabSupervisedParameterK.setEnabled(true);        
      }
    }
  }//end controlSupervisedGUI(boolean)

/**
 * TabSupervised -> Substructure Combo Box
 */
  private void tabSupervisedControlNBExplanation()
  {
       switch(tabSupervisedComboBox1.getSelectedIndex()){
          case 0: //NB-All variables
          { tabSupervisedTextPane.setText(
               localize(dialogStrings, "DataBaseMonitor.Supervised.NBAllExplanation")); break;}
          case 1: //NB­Selective
          {
            if (tabSupervisedRadioButtonFilter.isSelected()) //Filter
            {
              if (tabSupervisedRadioButton95.isSelected()) //95%
                tabSupervisedTextPane.setText(
                 localize(dialogStrings, "DataBaseMonitor.Supervised.NotAvailabe")); 
              else //99%
                tabSupervisedTextPane.setText(
                 localize(dialogStrings, "DataBaseMonitor.Supervised.NotAvailabe")); 
            }
            else //Wrapper
            {
              if (tabSupervisedRadioButtonGreedy.isSelected()) //Greedy
                tabSupervisedTextPane.setText(
                            localize(dialogStrings, "DataBaseMonitor.Supervised.WrapperSelectiveNBExplanation"));
              else //UMDA
                tabSupervisedTextPane.setText(
                 localize(dialogStrings, "DataBaseMonitor.Supervised.NotAvailabe")); 
            }
          break;}
          case 2: //NB-Semi
          {
            if (tabSupervisedRadioButtonFilter.isSelected()) //Filter
            {
              if (tabSupervisedRadioButton95.isSelected()) //95%
                tabSupervisedTextPane.setText(
                 localize(dialogStrings, "DataBaseMonitor.Supervised.NotAvailabe")); 
              else //99%
                tabSupervisedTextPane.setText(
                 localize(dialogStrings, "DataBaseMonitor.Supervised.NotAvailabe")); 
            }
            else //Wrapper
            {
              if (tabSupervisedRadioButtonGreedy.isSelected()) //Greedy
                tabSupervisedTextPane.setText(
                            localize(dialogStrings, "DataBaseMonitor.Supervised.WrapperSemiNBExplanation"));
              else //UMDA
                tabSupervisedTextPane.setText(
                 localize(dialogStrings, "DataBaseMonitor.Supervised.NotAvailabe")); 
            }
          break;}
       }
    
  }//end tabSupervisedControlNBExplanation()

/**
 * TabSupervised -> Main Combo Box
 */
  private void tabSupervisedComboBox_actionPerformed(ActionEvent e)
  {
    this.controlSupervisedGUI(true);
    switch(tabSupervisedComboBox.getSelectedIndex())
    {
      case 0:{ tabSupervisedControlNBExplanation(); break;}
      case 1:{ tabSupervisedTextPane.setText(
               localize(dialogStrings, "DataBaseMonitor.Supervised.TANAllExplanation")); break;}
      case 2:{ tabSupervisedTextPane.setText(
               localize(dialogStrings, "DataBaseMonitor.Supervised.KDBAllExplanation")); break;}
    }
  }//end tabSupervisedComboBox_actionPerformed(ActionEvent)

/**
 * TabSupervised -> Process Button
 */
  private void tabSupervisedProcessButton_actionPerformed(ActionEvent e)
  {
    Vector parameters = new Vector();
    boolean flag = true;
    
    //1.- Verify there is a input file selected or loaded
    if ((fileDialog.getSelectedFile() != null) && (fileDialog.getSelectedFile().getName().endsWith(".dbc")))
    {
      parameters.add(fileDialog.getSelectedFile());

      parameters.add(new Integer(tabSupervisedComboBox.getSelectedIndex()));
      parameters.add(new Integer(tabSupervisedComboBox1.getSelectedIndex()));
      parameters.add(new Boolean(tabSupervisedCheckBox.isSelected()));
      if ((!(0 == tabSupervisedComboBox1.getSelectedIndex())) && (tabSupervisedRadioButtonFilter.isSelected() ||
          tabSupervisedRadioButtonUmda.isSelected()) )
      {
        JOptionPane.showMessageDialog(getActiveDialog(),
        localize(dialogStrings, "DataBaseMonitor.Supervised.NotAvailabe"), "Error",0);
        tabSupervisedComboBox1.setSelectedIndex(0);
        flag = false;          
      }      
      parameters.add(new Boolean(tabSupervisedRadioButtonFilter.isSelected()));
      parameters.add(new Boolean(tabSupervisedRadioButton95.isSelected()));
      parameters.add(new Boolean(tabSupervisedRadioButtonUmda.isSelected()));
      parameters.add(new Integer(tabSupervisedTextField.getField()));
    }  
    else { messageNoDBCSelected(); flag = false; }
    
    if (flag) //Invoke the task
    {
      this.guiComponentStatus(false);
      try{
        taskToDo = 9; //Supervised task
        worker = new DataBaseMonitorWorker(this, taskToDo, parameters);
        worker.go();
        timer.start();        
      }catch(Exception exception){System.out.println("Error: "+e.toString());}      
    }
  }//end tabSupervisedProcessButton_actionPerformed(ActionEvent)
  
/**
 * Unsupervied GUI components logical control
 */
  private void controlUnsupervisedGUI(boolean b)
  {
    tabUnsupervisedMainLabel.setEnabled(b); tabUnsupervisedNoClusters.setEnabled(b);
    tabUnsupervisedTextField.setEnabled(b); tabUnsupervisedComboBox.setEnabled(b);
    tabUnsupervisedTextField1.setEnabled(b); tabUnsupervisedLearningMethod.setEnabled(b);
    tabUnsupervisedComboBox1.setEnabled(b); tabUnsupervisedCheckBox.setEnabled(b);
    tabUnsupervisedNoModelers.setEnabled(b); tabUnsupervisedTextField1.setEnabled(b);
    tabUnsupervisedProcessButton.setEnabled(b);
    if (b)
    {
      if (tabUnsupervisedComboBox1.getSelectedIndex() == 0){ 
          tabUnsupervisedNoModelers.setEnabled(false); tabUnsupervisedTextField1.setEnabled(false);}
    }
  }//end controlUnsupervisedGUI(boolean)

/**
 * TabUnsupervised -> Learning method Combo Box
 */
  private void tabUnsupervisedComboBox1_actionPerformed(ActionEvent e)
  {
    this.controlUnsupervisedGUI(true);
    switch(tabUnsupervisedComboBox1.getSelectedIndex())
    {
      case 0:{ tabUnsupervisedTextPane.setText(
               localize(dialogStrings, "DataBaseMonitor.Unsupervised.EMExplanation")); break;}
      case 1:{ tabUnsupervisedTextPane.setText(
               localize(dialogStrings, "DataBaseMonitor.Unsupervised.MultiStartExplanation")); break;}
    }
  }

/**
 * TabUnsupervised -> Process Button
 */
  private void tabUnsupervisedProcessButton_actionPerformed(ActionEvent e)
  {
    Vector parameters = new Vector();
    boolean flag = true;
    
    //1.- Verify there is a input file selected or loaded
    if ((fileDialog.getSelectedFile() != null) && (fileDialog.getSelectedFile().getName().endsWith(".dbc")))
    {
       parameters.add(fileDialog.getSelectedFile());
      //2.- Verify the number of clusters > 1
      String clusters = tabUnsupervisedTextField.getField();
      if (clusters == "") flag = false;
      else
      {
        Integer noClusters = new Integer(clusters);
        if ( noClusters.intValue() > 1 ) parameters.add(noClusters);
        else
        { JOptionPane.showMessageDialog(getActiveDialog(),
                     localize(dialogStrings, "DataBaseMonitor.Unsupervised.errorClusters"), "Error",0);
          flag = false; 
        }
        //3.- Learning method & optional parameters
        int alg = tabUnsupervisedComboBox1.getSelectedIndex();
        parameters.add(new Integer(alg));
        if (alg == 1)
        {
          String models = tabUnsupervisedTextField1.getField();
          if ( models == "" ) flag = false;
          else parameters.add(new Integer(models));
        }
        else parameters.add(new Integer(1));
      }
    }  
    else { messageNoDBCSelected(); flag = false; }
    
    if (flag) //Invoke the task
    {
      Boolean correction = new Boolean(tabUnsupervisedCheckBox.isSelected());
      parameters.add(correction);
      this.guiComponentStatus(false);
      try{
        taskToDo = 8; //Clustering task
        worker = new DataBaseMonitorWorker(this, taskToDo, parameters);
        worker.go();
        timer.start();        
      }catch(Exception exception){System.out.println("Error: "+e.toString());}      
    }
  }//end tabUnsupervisedProcessButton
  
/**
 * tabTest 
 */
  private void tabTestInitializeComboBox()
  {
    classifiers.removeAllElements();
    tabTestClassifierComboBox.removeAllItems();
    JInternalFrame[] networkFrameList = Elvira.getElviraFrame().getDesktopPane().getAllFrames();
    for (int i=0; i<networkFrameList.length; i++)
    {
      if ( networkFrameList[i].getContentPane().getComponentCount() == 2 )
        {
          String visibleName = networkFrameList[i].getTitle();
          visibleName = visibleName.substring(visibleName.lastIndexOf(System.getProperty("file.separator"))+1, visibleName.lastIndexOf("."));
          tabTestClassifierComboBox.insertItemAt(visibleName, tabTestClassifierComboBox.getItemCount());
          classifiers.add(tabTestClassifierComboBox.getItemCount()-1, networkFrameList[i]);
        }
    }
    if (tabTestClassifierComboBox.getItemCount()>0) tabTestClassifierComboBox.setSelectedIndex(0);
  }//end tabTestInitializeComboBox(JComboBox)

/**
 * FileOut in categorization
 */
  private void tabTestOutFileBrowseButton_actionPerformed(ActionEvent e)
  {
    ElviraFileChooser fileCh = new ElviraFileChooser(System.getProperty("user.dir"));

 	  fileCh.setDataBaseFilter(); 
 	   	  
	  	if (lastVisitedDirectory==null)
 	  	{
 	  		fileCh.rescanCurrentDirectory();
 	  	}
 	  	else
 	  	{
 	  		fileCh.setCurrentDirectory(lastVisitedDirectory);
 	  	}

 	  	fileCh.setDialogType(fileDialog.OPEN_DIALOG);
  	//fileCh.setSelectedFile(new File("newfile.dbc"));
		int state = fileCh.showDialog(this.getActiveDialog(), localize(dialogStrings,"DataBaseMonitor.Browse.label"));
    if (state == fileDialog.CANCEL_OPTION)
      fileCh.setSelectedFile(null);
    else{
      tabTestFileText.setText(fileCh.getSelectedFile().getPath());
      lastVisitedDirectory=fileCh.getCurrentDirectory();
    }        
  }//end tabTestOutFileBrowseButton

/**
 * CrossValidation & Categorization GUI components logical control
 */
  private void controlTestGUI(boolean b)
  {
    tabTestClassifierLabel.setEnabled(b); tabTestClassifierComboBox.setEnabled(b);
    tabTestProcessButton.setEnabled(b); tabTestRadioButtonCategorize.setEnabled(b);
    tabTestRadioButtonValidation.setEnabled(b); tabTestParameterK.setEnabled(b);
    tabTestTextField.setEnabled(b); tabTestOutFileBrowseButton.setEnabled(b);
    tabTestRadioButtonTest.setEnabled(b); jTabbedPane4.setEnabled(b);

    if ( b ) 
    {
      if (tabTestRadioButtonValidation.isSelected())
        tabTestOutFileBrowseButton.setEnabled(false); 
      else
      { tabTestParameterK.setEnabled(false); tabTestTextField.setEnabled(false); }
    }

  }//end controlTestGUI(boolean)

/**
 * TabTest -> Process Button
 */
  private void tabTestProcessButton_actionPerformed(ActionEvent e)
  {
    Vector parameters = new Vector();
    boolean flag = true;
    
    //1.- Verify there is a input file selected or loaded
    if ((fileDialog.getSelectedFile() != null) && (fileDialog.getSelectedFile().getName().endsWith(".dbc")))
    {
       parameters.add(fileDialog.getSelectedFile());
      //2.- Check if there is a classificator selected
      if (tabTestClassifierComboBox.getItemCount() != 0)
      {
        JInternalFrame netFrame = (JInternalFrame)classifiers.elementAt(tabTestClassifierComboBox.getSelectedIndex());
        parameters.add(netFrame);
        //3.- Swtich between the different options
        if (tabTestRadioButtonValidation.isSelected())
        {
          //Verify the files
          InformationPane info = (InformationPane)netFrame.getContentPane().getComponent(1);
          if (! (new File(info.getDataFile())).equals(fileDialog.getSelectedFile()))
          {
            JOptionPane.showMessageDialog(getActiveDialog(), 
                    "El fichero original y el fichero indicado no coinciden", "Error", 0);
            flag = false;
          }
          String kValue = tabTestTextField.getField();
          if ( kValue == "") flag = false;
          else parameters.add( new Integer(kValue) );
        }
        else if (tabTestRadioButtonCategorize.isSelected())
        {
          // Verify there is an output file selected
          String name = tabTestFileText.getText();
          if ( name.equals("") || !name.endsWith(".dbc") ){ messageNoCategorizeDBCSelected(); flag = false;}
          else parameters.add(name);
        }
        else if (tabTestRadioButtonTest.isSelected())
        {
          // Verify there is an output file selected
          String name = tabTestFileText.getText();
          if ( name.equals("") || !name.endsWith(".dbc") ){ messageNoTestDBCSelected(); flag = false;}
          else parameters.add(name);        
        }
      }
      else { messageNoClassifierSelected(); flag = false; }
    }
    else { messageNoDBCSelected(); flag = false; }
    
    if (flag) //Invoke the task
    {
      this.guiComponentStatus(false);
      try{
        //Switch between tasks
        if ( tabTestRadioButtonValidation.isSelected() ) { taskToDo = 10; }//CrossValidate
        else if ( tabTestRadioButtonCategorize.isSelected() ) { taskToDo = 11; }//Categorize 
        else if ( tabTestRadioButtonTest.isSelected() ) { taskToDo = 12; }//Classifier Testing 
        
        worker = new DataBaseMonitorWorker(this, taskToDo, parameters);
        worker.go();
        timer.start();
      }catch(Exception exception){System.out.println("Error: "+e.toString());}      
    }      
  }//end tabTestProcessButton_actionPerformed(ActionEvent)
 
/**
 * Shows a validation message for the categorization process
 */
  public void messageCategorizationDone()
  {
    JOptionPane.showMessageDialog(getActiveDialog(),
                      "Fichero categorizado correctamente", "OK",1);
  }//end messageImputationDone()

/**
 * Shows a validation message with the accuracy of the supervised classifier
 */
  public void messageTestSupervisedDone(double acc)
  {
    JOptionPane.showMessageDialog(getActiveDialog(),
                      "Precisión del clasificador: " + acc +" %", "OK",1);
  }//end messageImputationDone()

/**
 * Factorization GUI components logical control
 */
  private void controlFactorizationGUI(boolean b)
  {
    for (int i=0; i<tabFactorization.getComponentCount(); i++)
      tabFactorization.getComponents()[i].setEnabled(b);

    if (b) //Options visibility logical control
    {
      switch (tabFactorizationLearningComboBox.getSelectedIndex())
      {
        case 0: //K2
        {
          tabFactorizationNoProcLabel.setEnabled(false);
          tabFactorizationNoProc.setEnabled(false);
          tabFactorizationTamPobLabel.setEnabled(false);
          tabFactorizationTamPob.setEnabled(false);
          tabFactorizationNoIterLabel.setEnabled(false);
          tabFactorizationNoIter.setEnabled(false);
          tabFactorizationMaxNoNeighboursLabel.setEnabled(false);
          tabFactorizationMaxNoNeighbours.setEnabled(false);
          tabFactorizationNoCasesLabel.setEnabled(false);
          tabFactorizationNoCases.setEnabled(false);
          tabFactorizationConfidenceLabel.setEnabled(false);
          tabFactorizationConfidence.setEnabled(false);
          tabFactorizationLabel2.setEnabled(false);
          tabFactorizationMetricsComboBox.setEnabled(false);
          break;
        }
        case 1: //K2SN
        {
          tabFactorizationNoProcLabel.setEnabled(false);
          tabFactorizationNoProc.setEnabled(false);
          tabFactorizationTamPobLabel.setEnabled(false);
          tabFactorizationTamPob.setEnabled(false);
          tabFactorizationNoIterLabel.setEnabled(false);
          tabFactorizationNoIter.setEnabled(false);
          tabFactorizationMaxNoNeighboursLabel.setEnabled(false);
          tabFactorizationMaxNoNeighbours.setEnabled(false);
          tabFactorizationNoCasesLabel.setEnabled(false);
          tabFactorizationNoCases.setEnabled(false);
          tabFactorizationConfidenceLabel.setEnabled(false);
          tabFactorizationConfidence.setEnabled(false);
          break;
        }
        case 2: //PC
        {
          tabFactorizationMaxParentsLabel.setEnabled(false);
          tabFactorizationMaxParents.setEnabled(false);
          tabFactorizationNoProcLabel.setEnabled(false);
          tabFactorizationNoProc.setEnabled(false);
          tabFactorizationTamPobLabel.setEnabled(false);
          tabFactorizationTamPob.setEnabled(false);
          tabFactorizationNoIterLabel.setEnabled(false);
          tabFactorizationNoIter.setEnabled(false);
          tabFactorizationMaxNoNeighboursLabel.setEnabled(false);
          tabFactorizationMaxNoNeighbours.setEnabled(false);
          tabFactorizationLabel2.setEnabled(false);
          tabFactorizationMetricsComboBox.setEnabled(false);
          break;
        }
        case 3: //DVNSST
        {
          tabFactorizationNoCasesLabel.setEnabled(false);
          tabFactorizationNoCases.setEnabled(false);
          tabFactorizationConfidenceLabel.setEnabled(false);
          tabFactorizationConfidence.setEnabled(false);
          tabFactorizationMaxParentsLabel.setEnabled(false);
          tabFactorizationMaxParents.setEnabled(false);
          break;
        }
	case 4: //Structural MTE
	{
          tabFactorizationMaxParentsLabel.setEnabled(false);
          tabFactorizationMaxParents.setEnabled(false);
   	  tabFactorizationNoProcLabel.setEnabled(false);
          tabFactorizationNoProc.setEnabled(false);
          tabFactorizationTamPobLabel.setEnabled(false);
          tabFactorizationTamPob.setEnabled(false);
          tabFactorizationNoIterLabel.setEnabled(false);
          tabFactorizationNoIter.setEnabled(false);
          tabFactorizationMaxNoNeighboursLabel.setEnabled(false);
          tabFactorizationMaxNoNeighbours.setEnabled(false);
          tabFactorizationNoCasesLabel.setEnabled(false);
          tabFactorizationNoCases.setEnabled(false);
          tabFactorizationConfidenceLabel.setEnabled(false);
          tabFactorizationConfidence.setEnabled(false);
          tabFactorizationLabel2.setEnabled(false);
          tabFactorizationMetricsComboBox.setEnabled(false);
          break;
	}
      }
    }//end if

  }//end controlFactorizationGUI(boolean)

/**
 * Factorization -> Process
 */
  private void tabFactorizationProcessButton_actionPerformed(ActionEvent e)
  {
    boolean flag = true;
    Vector parameters = new Vector();
    Vector internalParam = new Vector();
    //1.-Check if there's a dbc file selected
    if ((fileDialog.getSelectedFile() != null) && (fileDialog.getSelectedFile().getName().endsWith(".dbc")))
    {
      //Invocamos a DataBaseMonitorWorker y si le paso un null en el KnowledgeFrame que lo
      // cargue él.
      //Preparar el vector de parámetros tal y como lo quiere el método de aprendizaje
      //Formato parámetros: 1-File dbc seleccionado
      //                    2-NetworkFrame con el Knowledge precargado o no
      //                    3-Integer LearningMethod
      //                    4-Integer ParameterMethod
      //                    5-Integer Metric
      //                    6-Vector con los parámetros particulares de cada uno
      parameters.add(fileDialog.getSelectedFile());
      parameters.add(this.tabFactorizationKnowledgeFrame);
      switch(tabFactorizationLearningComboBox.getSelectedIndex())
      {
        case 0:
          parameters.add(new Integer(0));
          String maxParents = tabFactorizationMaxParents.getField();
          if (maxParents == "") flag = false;
          else internalParam.add(new Integer(maxParents));
          break;
        case 1:
          parameters.add(new Integer(3));
          maxParents = tabFactorizationMaxParents.getField();
          if (maxParents == "") flag = false;
          else internalParam.add(new Integer(maxParents));
          break;
        case 2:
          parameters.add(new Integer(1));
          String noCases = tabFactorizationNoCases.getField();
          String conf = tabFactorizationConfidence.getField();
          if (noCases == "" || conf =="") flag = false;
          else
          {
          internalParam.add(new Integer(noCases));
          internalParam.add(new Integer(conf));
          }
          break;
        case 3:
          parameters.add(new Integer(2));
          String noProc = tabFactorizationNoProc.getField();
          String tamPob = tabFactorizationTamPob.getField();
          String noIter = tabFactorizationNoIter.getField();
          String MaxNeighs = tabFactorizationMaxNoNeighbours.getField();
          if ( noProc=="" || tamPob=="" || noIter=="" || MaxNeighs=="") flag=false;
          else
          {
          internalParam.add(new Integer(noProc));
          internalParam.add(new Integer(tamPob));
          internalParam.add(new Integer(noIter));
          internalParam.add(new Integer(MaxNeighs));
          }
          break;
	case 4:
	  parameters.add(new Integer(4));
	  break;
      }
      //In order to continue with old semantic of this vector
      internalParam.add(new String(""));

      if (tabFactorizationRadioButtonLP.isSelected()) parameters.add(new Integer(1));
      else parameters.add(new Integer(0));
      parameters.add(new Integer(tabFactorizationMetricsComboBox.getSelectedIndex()));
      parameters.add(internalParam);
    }
    else
      { messageNoDBCSelected(); flag = false; }

    if (flag) //Invoke the task
    {
      this.guiComponentStatus(false);
      try{
        guiComponentStatus(false);
        taskToDo = 14; //Factorization of joint probabilities
        worker = new DataBaseMonitorWorker(this, taskToDo, parameters);
        worker.go();
        timer.start();
      }catch(Exception exception){System.out.println("Error: "+e.toString());}
    }

  }//end tabFactorizationProcessButton_actionPerformed(ActionEvent)

/**
 * Factorization -> Learning comboBox selection changed
 */
  private void tabFactorizationLearningComboBox_actionPerformed(ActionEvent e)
  {
    this.controlFactorizationGUI(true);
  }//end tabFactorizationLearningComboBox_actionPerformed(ActionEvent)

/**
 * Factorization -> Restrictions button
 */
  private void tabFactorizationKnowledgeButton_actionPerformed(ActionEvent e)
  {
    boolean flag = true;
    //1.-Check if there's a dbc file selected
    if ((fileDialog.getSelectedFile() != null) && (fileDialog.getSelectedFile().getName().endsWith(".dbc")))
    {
      //2.-Check if there has been initilized the Knowledge Frame
      if (tabFactorizationKnowledgeFrame == null)
      {
        ///Initialize tabFactorizationKnowledgeFrame variable and its internal dbc
        try{
          guiComponentStatus(false);
          taskToDo = 13; //Initilize restrictions
          Vector params = new Vector();
          params.add(fileDialog.getSelectedFile());
          worker = new DataBaseMonitorWorker(this, taskToDo, params);
          worker.go();
          timer.start();
        }catch(Exception exception){System.out.println("Error: "+exception.toString());}
      }
      else
        tabFactorizationLaunchKnowledgeDialog(this.tabFactorizationKnowledgeFrame);
    }
    else
      messageNoDBCSelected();
  }//end tabFactorizationKnowledgeButton_actionPerformed(ActionEvent)

/**
 * Assigns the KnowledgeFrame and displays the ConstrainKnowledge dialog
 */
 public void tabFactorizationLaunchKnowledgeDialog(NetworkFrame net)
 {
   tabFactorizationKnowledgeFrame = net;
   ConstraintKnowledgeDialog d = new ConstraintKnowledgeDialog(this, tabFactorizationKnowledgeFrame);
   d.show();

 }//end tabFactorizationLaunchKnowledgeDialog(NetworkFrame)

////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Auxiliar functions
 */
  public void visualizeBnetGUI(Bnet net, String name)
  {
    Elvira.getElviraFrame().createNewFrame(name, false);
    for (int i=0; i<net.getNodeList().size(); i++) {
      Node n = net.getNodeList().elementAt(i);
      n.setFont("Helvetica");
      FontMetrics fm=Elvira.getElviraFrame().getFontMetrics(ElviraPanel.getFont(n.getFont()));
      VisualNode.setAxis(n, n.getNodeString(true),fm);
    }
    ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().setBayesNet(net);
    Elvira.getElviraFrame().setTitle("Elvira - "+name);
    updateElviraPanel();
  }//end visualizeBnetGUI(Bnet)

  private void displayClassifier(Bnet net, Node nodeClass)
  {
    int width = Elvira.getElviraFrame().getWidth();
    int height = Elvira.getElviraFrame().getDesktopPane().getHeight();
    //Positioning the class node
    nodeClass.setPosX(width/2);
    nodeClass.setPosY(height/6);

    //Positioning the rest nodes
    int nodesRow = width/128;
    int numRows = (net.getNodeList().size()-1)/nodesRow;
    if (((net.getNodeList().size()-1) % nodesRow) != 0) numRows++;

    for (int i=0; i < numRows-1; i++)
    {
      for (int j=0; j < nodesRow; j++)
      {
        if (! net.getNodeList().elementAt((i*nodesRow)+j).equals(nodeClass))
        {
          net.getNodeList().elementAt((i*nodesRow)+j).setPosY((height/2)+i*(height/(2*numRows)));
          net.getNodeList().elementAt((i*nodesRow)+j).setPosX((128*j)+100);
        }
      }
    }
    //Nodes that cannot have been positioned until now
    int j=0;
    for (int k=(numRows-1)*nodesRow; k < net.getNodeList().size(); k++)
    {
      if (! net.getNodeList().elementAt(k).equals(nodeClass))
      {
        net.getNodeList().elementAt(k).setPosY((height/2)+(numRows-1)*(height/(2*numRows)));
        net.getNodeList().elementAt(k).setPosX((128*j)+100);
        j++;
      }
    }
    updateElviraPanel();
  }//end displayClassifier(Bnet, Node)

  public void displayNaiveClassifier()
  {
    Bnet net = ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().getBayesNet();
    this.displayClassifier(net, (Node)net.getNodeList().getNodes().lastElement());
  }//end displayNaiveClassifier()

  public void displayTANClassifier()
  {
    Bnet net = ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().getBayesNet();
    int width = Elvira.getElviraFrame().getWidth();
    int height = Elvira.getElviraFrame().getDesktopPane().getHeight();
    //Positioning the class node, the last of the net
    net.getNodeList().lastElement().setPosX(100);
    net.getNodeList().lastElement().setPosY(30);
    //Positioning the main parent nodes
    Node n = null;
    Vector mainParents = new Vector();
    for (int i=0; i<net.getNodeList().size()-1; i++)
    {
      n=net.getNodeList().elementAt(i);
      if (n.getParentNodes().size()==1)
        mainParents.add(n);
    }
    
    int level = height/8 + 75;
    positionateTANodes(level, width, mainParents);
    mainParents = this.getNodesLevel(mainParents);
    while (mainParents.size()>0)
    {
      level = level + 75;
      this.positionateTANodes(level, width, mainParents);
      mainParents = this.getNodesLevel(mainParents);      
    }

    updateElviraPanel();    
  }//end displayTANClassifier()

  public void displayKDBClassifier()
  {
    Bnet net = ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().getBayesNet();
    int width = Elvira.getElviraFrame().getWidth();
    int height = Elvira.getElviraFrame().getDesktopPane().getHeight();
    //Obtaining an auxiliar vector that will indicate the level of the nodes
    // pos 0  -> Class Node
    // pos  1 -> Nodes that only has one parent, the class node
    // pos  2 -> Nodes that are direct children of the pos 1 set nodes
    //
    // pos  N -> Nodes that are direct children of the n-1 set nodes
    // pos -1 -> Nodes that do not have children
    int[] KDBNodes = new int[net.getNodeList().size()];
    boolean[] KDBPosition = new boolean[KDBNodes.length];
    KDBNodes[net.getNodeList().size() - 1] = 0; //The class node is always the last
    KDBPosition[net.getNodeList().size() - 1] = true;
    //Determining the pos 1 nodes and the pos -1 nodes
    for (int i=0; i<net.getNodeList().size()-2; i++)
    {
      if ( ((Node)net.getNodeList().elementAt(i)).getParentNodes().size() == 1 )
        { KDBNodes[i] = 1; KDBPosition[i] = true; }
      if ( ((Node)net.getNodeList().elementAt(i)).getChildrenNodes().size() == 0 )
        { KDBNodes[i] = -1; KDBPosition[i] = true; }
    }
    //Determining the pos 2 nodes
    for (int i=0; i<net.getNodeList().size()-2; i++)
    {
      if (KDBNodes[i] == 1)
      {
        Node parent = net.getNodeList().elementAt(i);
        for (int j=0; j<parent.getChildrenNodes().size(); j++)
        {
          KDBNodes[net.getNodeList().getId(parent.getChildrenNodes().elementAt(j))] = 2;
          KDBPosition[net.getNodeList().getId(parent.getChildrenNodes().elementAt(j))] = true;
        }
      }
    }
    //Treatment of the rest
    boolean next_level = false;
    for (int i=0; i<KDBPosition.length; i++)
      if (!KDBPosition[i]){ next_level = true; break; }
    int level = 2;
    while (next_level)
    {
      for (int i = 0; i<KDBNodes.length; i++)
      {
        if (KDBNodes[i] == level)
        {
          //Mark the children of node i as the next level nodes
          Node parent = net.getNodeList().elementAt(i);
          for (int j = 0; j<parent.getChildrenNodes().size(); j++)
            if (!KDBPosition[net.getNodeList().getId(parent.getChildrenNodes().elementAt(j))])
              {
                KDBNodes[net.getNodeList().getId(parent.getChildrenNodes().elementAt(j))] = level+1;
                KDBPosition[net.getNodeList().getId(parent.getChildrenNodes().elementAt(j))] = true;                
              }
        }
      }
      next_level = false;
      for (int i=0; i<KDBPosition.length; i++)
        if (!KDBPosition[i]){ next_level = true; break; }
      level++;
    }//end while
    
    //Using a quick access main structure
    Vector[] positionNodes = new Vector[level+1];
    //Initialization
    for (int i = 0; i<positionNodes.length; i++)
      positionNodes[i] = new Vector();
    //Location of each node
    for (int i = 0; i<KDBNodes.length; i++)
    {
      if (KDBNodes[i] == -1)
        positionNodes[level].add(net.getNodeList().elementAt(i));
      else if (KDBNodes[i] != 0)
        positionNodes[KDBNodes[i]-1].add(net.getNodeList().elementAt(i));
    }

    //Positioning the class node, the last of the net
    net.getNodeList().lastElement().setPosX(100);
    net.getNodeList().lastElement().setPosY(30);
    //Positioning the rest of them
    for (int i = 0; i<positionNodes.length; i++)
      positionateKDBNodes(50+(i*150), width, positionNodes[i]);

    updateElviraPanel();
  }//end displayKDBClassifier()

  private Vector getNodesLevel(Vector parents)
  {
    Vector sortChildren = new Vector();
    for (int i=0; i<parents.size(); i++)
      for (int j=0; j<((Node)parents.elementAt(i)).getChildrenNodes().size(); j++)
        sortChildren.add(((Node)parents.elementAt(i)).getChildrenNodes().elementAt(j));
    return sortChildren;
  }//end getNodesLevel(Vector)

  private void positionateKDBNodes(int height, int width, Vector nodes)
  {
    if (nodes.size() < 6)
      positionateTANodes(height, width, nodes);
    else
    {
      Vector nodes_1 = new Vector();
      Vector nodes_2 = new Vector();
      for (int i=0; i<1+nodes.size()/2; i++)
        nodes_1.add(nodes.elementAt(i));
      for (int i=1+nodes.size()/2; i<nodes.size(); i++)
        nodes_2.add(nodes.elementAt(i));
      positionateTANodes(height-25, width, nodes_1);
      positionateTANodes(height+25, width, nodes_2);
    }
  }//end positionateKDBNodes(int, int, Vector)
  
  private void positionateTANodes(int height, int width, Vector nodes)
  {
    for (int i=0; i<nodes.size(); i++)
    {
      ((Node)nodes.elementAt(i)).setPosY(height); 
      ((Node)nodes.elementAt(i)).setPosX((width/(nodes.size()+1))*(i+1));      
    }    
  }//end positionateTANnodes(int, int, Vector)

  private void updateElviraPanel()
  {
    ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().refreshElviraPanel(1.0);
    ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().setStrings();
    ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().repaint();
    ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().setModifiedNetwork(true);        
  }//end updateElviraPanel()
  
  public void displayClassifierPanel(Vector data)
  {
    //Information Panel
    StyleContext sc = new StyleContext();
    DefaultStyledDocument doc = new DefaultStyledDocument(sc);    
    Style myStyle = doc.addStyle("MiEstilo", null);
    InformationPane pane = new InformationPane(doc, data);
    myStyle.addAttribute(StyleConstants.FontSize, new Integer(16));
    myStyle.addAttribute(StyleConstants.FontFamily, "Helvetica");
    myStyle.addAttribute(StyleConstants.Bold, new Boolean(true));
    
    String text="";
    
    switch(pane.getModelType())
    {
      case 0: //Unsupervised NB
      {
        text = localize(dialogStrings,"DataBaseMonitor.ClassifierPanel.file")+" "+pane.getDataFile()+
               "\n"+localize(dialogStrings,"DataBaseMonitor.ClassifierPanel.log")+" "+pane.getLikelihood()+
               "\n"+localize(dialogStrings,"DataBaseMonitor.ClassifierPanel.ltime")+" "+pane.getLearningTime()+" ms"+
               "\n"+localize(dialogStrings,"DataBaseMonitor.ClassifierPanel.model")+" "+pane.getComments()+
               "\n"+localize(dialogStrings,"DataBaseMonitor.ClassifierPanel.lp")+" "+pane.isCorrected();
        break;
      }
      default: //Supervised NB && Supervised TAN && Supervised KDB
      {
        text = localize(dialogStrings,"DataBaseMonitor.ClassifierPanel.file")+" "+pane.getDataFile()+
               "\n"+localize(dialogStrings,"DataBaseMonitor.ClassifierPanel.ltime")+" "+pane.getLearningTime()+" ms"+
               "\n"+localize(dialogStrings,"DataBaseMonitor.ClassifierPanel.model")+" "+pane.getComments()+
               "\n"+localize(dialogStrings,"DataBaseMonitor.ClassifierPanel.lp")+" "+pane.isCorrected();
        break;
      }
    }//end switch

   try{
     doc.insertString(0, text, null);
     doc.setParagraphAttributes(0, 1, myStyle, false);
   }catch (Exception e){System.out.println("Error al aplicar el texto al estilo");}      

    pane.setBackground(new Color(204,204,204));
    pane.setEditable(false);
    Elvira.getElviraFrame().getCurrentNetworkFrame().getContentPane().add(pane, BorderLayout.NORTH);
    Elvira.getElviraFrame().getCurrentNetworkFrame().updateUI();

  }//end displayClassifierPanel(String, Vector)

  public void displayCVResults(InformationPane data, ConfusionMatrix matrix, Node classNode)
  {
    //Information Panel
    StyleContext sc = new StyleContext();
    Style heading = sc.addStyle("Heading", null);
    heading.addAttribute(StyleConstants.Foreground, Color.black);
    heading.addAttribute(StyleConstants.FontSize, new Integer(14));
    heading.addAttribute(StyleConstants.FontFamily, "Helvetica");
    heading.addAttribute(StyleConstants.Bold, new Boolean(true));

    DefaultStyledDocument info = new DefaultStyledDocument(sc);
    JTextPane pane = new JTextPane(info);
    pane.setBackground(new Color(204,204,204));
    pane.setEditable(false);
    try{
      info.insertString(0," "+localize(dialogStrings,"DataBaseMonitor.Filter.results.file")+ " "+data.getDataFile()+"\n "
                             +localize(dialogStrings,"DataBaseMonitor.Test.results.classif")
                             +data.getName().substring(data.getName().lastIndexOf(System.getProperty("file.separator"))+1)+"\n "
                             +localize(dialogStrings,"DataBaseMonitor.Test.results.acc")+" "+(1.0 - matrix.getError())+"\n "
                             +"\n "+localize(dialogStrings,"DataBaseMonitor.Test.results.matrix"), null);
      info.setParagraphAttributes(0,1,heading, false);      
    }catch (Exception e){}

    JSeparator line = new JSeparator(SwingConstants.HORIZONTAL);

    JTextArea pane2 = new JTextArea(0,0);
    pane2.setFont(new Font("Courier", Font.PLAIN, 13));

    //Display the results in the textarea
    char[][] m = this.visualizeConfusionMatrix(matrix, classNode);
    String auxLine;
    for (int i=0; i< m.length; i++)
    {
      auxLine = new String(m[i]);
      pane2.append(auxLine+"\n");
    }
    
    pane2.setEditable(false);
    JScrollPane scroll = new JScrollPane(pane2);
    scroll.setAutoscrolls(true);

    JButton close = new JButton(localize(dialogStrings,"OK.label"));
    JPanel paneAux = new JPanel();
    paneAux.add(close);

    final JDialog dialog = new JDialog(getActiveDialog(), 
                                localize(dialogStrings,"DataBaseMonitor.Test.results.title"), true);
    //dialog.setResizable(false);
    dialog.setResizable(true);
    dialog.setBounds(300, 200, 400, 350);
    dialog.getContentPane().add(pane, BorderLayout.NORTH);
    dialog.getContentPane().add(scroll, BorderLayout.CENTER);
    dialog.getContentPane().add(paneAux, BorderLayout.SOUTH);
    close.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          dialog.dispose();
        }
      });

    dialog.show();

  }//end displayCVResults(ConfusionMatrix)

  private char[][] visualizeConfusionMatrix(ConfusionMatrix mat, Node node)
  {
    Vector states = ((FiniteStates)node).getStates();
    int noStates = states.size();
    char[][] display = new char[noStates+3][(noStates+1)*15];
    //Blanking the matrix
    for (int i=0; i<display.length; i++)
      for (int j=0; j<display[i].length; j++)
        display[i][j]=' ';

    display[0][9]='R'; display[0][10]='E'; display[0][11]='A'; display[0][12]='L'; display[0][14]='|';
    for (int j=0; j<display[1].length; j++) display[1][j]='_';
    display[2][0]='A'; display[2][1]='S'; display[2][2]='S'; display[2][3]='I';
    display[2][4]='G'; display[2][5]='N'; display[2][6]='E'; display[2][7]='D';
    for (int i=2; i<display.length; i++) display[i][14]='|';

    //Display node's posible values
    String aux;
    for (int label=0; label<noStates; label++)
    {
      aux = (String)states.elementAt(label);
      char[] name = new char[aux.length()];
      name = aux.toCharArray();
      //Positionate the names on the columns and the first row
      for (int j=0; (j<name.length && j<15); j++)
      {
        display[3+label][14-name.length+j]=name[j];
        display[0][((label+1)*15)+j]=name[j];
      }
      for (int j=0; j<noStates; j++)
      {
        aux = new Double(mat.getValue(label,j)).toString();
        char[] value = new char[aux.length()];
        value = aux.toCharArray();
        for (int pos=0; pos<value.length-2; pos++)
          display[3+j][((label+1)*15)+pos]=value[pos];
      }      
    }

    return display;
  }//end visualizeConfusionMatrix
  
/*
  private void tabFilterOutFileBrowseButton_actionPerformed(ActionEvent e)
  {
    try{
      Bnet red = ((NetworkFrame)Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().getBayesNet();
      this.displayClassifier(red,red.getNodeList().elementAt(0)); //Nodo clase el 0
    }catch (Exception exception){
      System.out.print("Error al posicionar: "+exception.toString());
      Elvira.println("Eror al posicionar: "+exception.toString());
    }finally { this.dispose();}

  }//end tabFilterOutFileBrowseButton_actionPerformed(ActionEvent)

  private void tabFilterProcessButton_actionPerformed(ActionEvent e)
  {
    try{
      displayNaiveClassifier();
    }catch (Exception exception){
      System.out.print("Error al posicionar: "+exception.toString());
      Elvira.println("Eror al posicionar: "+exception.toString());
    }finally { this.dispose();}

  }
*/

//////////////////////////////////////////////////////////////////////////////
/////////////////////////  AUXILIAR CLASSES /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/**
 * 
 */
  public class JNumberTextField extends JFormattedTextField
  {
    private String errorMessage;

    public JNumberTextField()
    { 
      super(NumberFormat.getNumberInstance());
      errorMessage = "";
    }

    public JNumberTextField(Integer value)
    { 
      super(NumberFormat.getNumberInstance());
      this.setValue(value);
      errorMessage = "";
    }

/*    public JNumberTextField(String m)
    {
      super(NumberFormat.getNumberInstance());
      errorMessage = m;
    }
*/
    public String getErrorMessage()
    { return errorMessage; }

    public void setErrorMessage(String m)
    { errorMessage = m; }

    public String getField()
    { 
      if ( super.getValue() == null ) 
      {
        JOptionPane.showMessageDialog(getActiveDialog() , errorMessage, "Error",0);
        return "";
      }
      else
        return super.getValue().toString();
    }
  }//end JNumberTextField

/**
 * 
 */
  public class InformationPane extends JTextPane
  {
    private Vector data;

    public InformationPane(DefaultStyledDocument style, Vector d)
    {
      super(style);
      data=d;
    }
    public String getDataFile(){ return (String)data.elementAt(0); }
    public int getModelType(){ return ((Integer)data.elementAt(1)).intValue(); }
    public int getStructure(){ return ((Integer)data.elementAt(2)).intValue(); }
    public long getLearningTime(){ return ((Long)data.elementAt(3)).longValue(); }
    public boolean isCorrected(){ return ((Boolean)data.elementAt(4)).booleanValue(); }
    public double getLikelihood() { return ((Double)data.elementAt(5)).doubleValue(); }
    public int getMaxNoParents() {return ((Integer)data.elementAt(6)).intValue(); }
    public String getComments() { return (String)data.elementAt(9); }
    public String getName() {return (String)data.elementAt(10); }
  }//end InformationPane

/**
 * 
 */

  private class AuxiliarDiscretizeNode
  {
    private String name;
    private String type;
    private boolean discretize;
    private int algorithm;
    private int intervals;
    private double options;

    public AuxiliarDiscretizeNode()
    {
      name=""; type=""; discretize=false; algorithm=0; intervals=2; options=0.0;
    }
    public AuxiliarDiscretizeNode(String n, String t)
    {
      name=n; type=t; discretize=false; algorithm=0; intervals=2; options=0.0;
    }
    public void setName(String n){ name =n;}
    public void setType(String t){ type = t;}
    public void setAlgorithm(int alg) { 
      algorithm = alg;
      if (algorithm ==3) intervals = 2;}
    public void setIntervals(int inter) {intervals = inter;}
    public void setOptions(double d){ options =d; }
    public void setDiscretizeMark(boolean b){ discretize = b;}
    public String getName(){ return name;}
    public String getType(){ return type;}
    public boolean isMarkDiscretize(){ return discretize;}
    public int getAlgorithm(){ return algorithm;}
    public int getIntervals(){ return intervals;}
    public double getOptions(){ return options;}

  }//end AuxiliarDiscretizeNode


/**
 * Auxiliar class for showing images with scrollbar
 */
  private class ScrollablePicture extends JLabel implements Scrollable {

    private int maxUnitIncrement = 1;

    public ScrollablePicture(ImageIcon i, int m) {
        super(i);
        maxUnitIncrement = m;
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
        //Get the current position.
        int currentPosition = 0;
        if (orientation == SwingConstants.HORIZONTAL)
            currentPosition = visibleRect.x;
        else
            currentPosition = visibleRect.y;

        //Return the number of pixels between currentPosition
        //and the nearest tick mark in the indicated direction.
        if (direction < 0) {
            int newPosition = currentPosition - 
                             (currentPosition / maxUnitIncrement) *
                              maxUnitIncrement;
            return (newPosition == 0) ? maxUnitIncrement : newPosition;
        } else {
            return ((currentPosition / maxUnitIncrement) + 1) *
                   maxUnitIncrement - currentPosition;
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction) {
        if (orientation == SwingConstants.HORIZONTAL)
            return visibleRect.width - maxUnitIncrement;
        else
            return visibleRect.height - maxUnitIncrement;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public void setMaxUnitIncrement(int pixels) {
        maxUnitIncrement = pixels;
    }
  }//end ScrollablePicture



  public class ScrollableTextPane extends JScrollPane
  {
    private JTextPane _textPane;

    public ScrollableTextPane(String text)
    {
      super();
      _textPane= new JTextPane();
      _textPane.setText(text);
      _textPane.setEditable(false);
      this.setViewportView(_textPane);
    }

    public void setText(String text)
    { _textPane.setText(text); }

    public String getText() { return _textPane.getText(); }
  }

/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////


}//end class DataBaseMonitor
