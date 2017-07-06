/*      ImportDialog       */

package elvira.gui;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.util.*;
import java.awt.Image;

import elvira.*;
import elvira.gui.*;
import elvira.parser.ParseException;
import elvira.database.DataBaseCases;
import elvira.Elvira;


/**
 * Class ImportDialog
 *
 * This class imports .csv files to .dbc files. It allows you to
 * edit variables, and force numeric finite-states variables to be
 * continuous.
 *
 * @author avrofe@ual.es
 * @version 1.4
 * @since 21/02/2007
 */

public class ImportDialog extends JDialog {
    
    private static String imgPath = "elvira/gui/images/";
    
    String variablesName[];
    ImportVariable variablesSet[];
    JLabel jLabel1 = new JLabel();
    JTextField tDBName = new JTextField();
    JLabel jLabel2 = new JLabel();
    int regNumber; //The number of registers
    int variables; //The number of variables
    
    //number of variables which you can force to be continuous
    int numForceVariables;
    
    
    //itemlistener para states
    ImportDialog_editStatesComboBox_actionAdapter l ;
    
    String[] editStatesNames;
    
    //If this value is 1, the window is visible and 0 otherwise.
    int disappear = 1;
    
    //To know if current variable's changes have been saved
    boolean saveChanges = true;
    
    //para evitar que se importe sin analizar el fixhero
    boolean analizado = false;
    
    //show if a numeric variable is now no numeric, because you have
    //introduce a no numeric new state
    boolean[] noNum;
    
    
    boolean primeravez = true;
    
    //position of the variable that you want to edit
    int posCurrentEditVar = 0;
    //position of the state that you want to edit
    int posCurrentEditStates = 0;
    //position of the variable that you want to force,
    //with regard to the set of variables that can be forced
    int posCurrentForceVar = 0;
    //position of the variable that you want to force
    //with regard to the set of variables
    int[] posCurrentForceVarVector;
    
    //aqui almaceno: oldValue, newValue
    //tiene tantos elementos como estados a modificar
    Vector editStatesVector = new Vector();
    
    boolean[] forceCont;
    
    //to allow variables to be edit
    String[] title;
    String[] name;
    String[] comment;
    Vector statesVector = new Vector();
    String[] states;
    
    
    //Character that separes a value in a csv file
    String separator = new String(";");
    
    //Panel to show error and success messages
    JOptionPane jOPanel = new JOptionPane();
    
    //The paths of properties files
    private ResourceBundle menusBundle;
    private ResourceBundle messagesBundle;
    private ResourceBundle dialogBundle;
    
    //File Chooser to choose dbc and csv files
    private ElviraFileChooser dbcFileDialog = new ElviraFileChooser(System.getProperty("user.dir"));
    private JFileChooser csvFileDialog = new JFileChooser(System.getProperty("user.dir"));
    
    
    // Visual Components
    
    //Main panels
    JPanel csvFilePanel = new JPanel();
    JPanel westPanel = new JPanel();
    JPanel eastPanel = new JPanel();
    JPanel importCancelPanel = new JPanel();
    JTabbedPane centralPanel = new JTabbedPane();
    JPanel optionPanel = new JPanel();
    JPanel forcePanel = new JPanel();
    JLabel csvFileLabel = new JLabel();
    
    //Components of options panel
    JTextField csvFileText = new JTextField();
    JButton csvFileButton = new JButton();
    JButton importButton = new JButton();
    JButton cancelButton = new JButton();
    JLabel dbcFileLabel = new JLabel();
    JTextField dbcFileText = new JTextField();
    JButton dbcFileButton = new JButton();
    JLabel optionLabel1 = new JLabel();
    
    //Components of Force panel
    JLabel forceLabel1 = new JLabel();
    JRadioButton forceMassiveButton = new JRadioButton();
    JRadioButton forceNothingButton = new JRadioButton();
    JRadioButton forceNormalButton = new JRadioButton();
    JLabel forceLabel2 = new JLabel();
    JLabel forceLabel1b = new JLabel();
    JLabel forceLabel2b = new JLabel();
    JPanel forceSubPanel = new JPanel();
    JLabel subForceNameLabel = new JLabel();
    JLabel subForceNameText = new JLabel();
    TitledBorder titledBorder1 = new TitledBorder("");
    JLabel subForceTitleText = new JLabel();
    JLabel subForceTitleLabel = new JLabel();
    JLabel subForceNoStatesText = new JLabel();
    JLabel subForceNoStatesLabel = new JLabel();
    JLabel subForceMaxLabel = new JLabel();
    JLabel subForceMinText = new JLabel();
    JLabel subForceMaxText = new JLabel();
    JLabel subForceMinLabel = new JLabel();
    JLabel forceLabel3 = new JLabel();
    JCheckBox forceCheckBox = new JCheckBox();
    JLabel subForceNoCasesLabel = new JLabel();
    JLabel subForceNoCasesText = new JLabel();
    
    JPanel importPanel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel editPanel = new JPanel();
    
    JComboBox forceComboBox = new JComboBox();
    JComboBox forceComboBox2 = new JComboBox();
    
    ButtonGroup buttonGroupOption1 = new ButtonGroup();
    ButtonGroup buttonGroupOption2 = new ButtonGroup();
    ButtonGroup buttonGroupForce = new ButtonGroup();
    
    //Components of edit panel
    JLabel editLabel1 = new JLabel();
    JComboBox editVariablesComboBox = new JComboBox();
    //this combobox only can be seen when there is not a .dbc file selected
    JComboBox editVariablesComboBox2 = new JComboBox();
    JLabel editTitleLabel = new JLabel();
    JLabel editNameLabel = new JLabel();
    JTextField editNameText = new JTextField();
    JTextField editTitleText = new JTextField();
    JLabel editCommentLabel = new JLabel();
    JTextField editCommentText = new JTextField();
    JLabel editOptionStatesLabel = new JLabel();
    JComboBox[] editStatesComboBox;
    JComboBox editStatesComboBox2 = new JComboBox();
    JTextField editOptionStatesNameText = new JTextField();
    JLabel editOptionStatesNameLabel = new JLabel();
    JLabel jLabel3 = new JLabel();
    Border border1;
    TitledBorder titledBorder2;
    TitledBorder titledBorder3;
    TitledBorder titledBorder4;
    JLabel jLabel5 = new JLabel();
    JLabel jLabel6 = new JLabel();
    JTextField optionDBNameText = new JTextField();
    ButtonGroup buttonGroup1 = new ButtonGroup();
    JRadioButton optionRadioButton1a = new JRadioButton();
    JRadioButton optionRadioButton1b = new JRadioButton();
    JButton forceProcessButton = new JButton();
    JButton editProcessButton = new JButton();
    JPanel editSubPanel = new JPanel();
    TitledBorder titledBorder5;
    JLabel optionLabel4 = new JLabel();
    JButton optionAnalyseButton = new JButton();
    JLabel editExplanationLabel = new JLabel();
    // JLabel analyseButtonLabel = new JLabel();
    
    // Constructor
    
    public ImportDialog(Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        
        //In this switch is selected the path of the properties files
        
        switch (Elvira.getLanguaje()) {
            case Elvira.AMERICAN:
                menusBundle = ResourceBundle.getBundle("elvira/localize/Menus");
                dialogBundle =ResourceBundle.getBundle("elvira/localize/Dialogs");
                messagesBundle =ResourceBundle.getBundle("elvira/localize/Messages");
                break;
            case Elvira.SPANISH:
                menusBundle =ResourceBundle.getBundle("elvira/localize/Menus_sp");
                dialogBundle =ResourceBundle.getBundle("elvira/localize/Dialogs_sp");
                messagesBundle =ResourceBundle.getBundle("elvira/localize/Messages_sp");
                break;
        } // End of switch
        
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        
        try {
            jbInit();
            pack();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public ImportDialog() {
        this(null, "", false);
    }
    
    private void jbInit() throws Exception {
        
        
        //Inicialise main panels
        titledBorder1 = new TitledBorder("");
        border1 = BorderFactory.createEmptyBorder();
        titledBorder2 = new TitledBorder("");
        titledBorder3 = new TitledBorder("");
        titledBorder4 = new TitledBorder("");
        titledBorder5 = new TitledBorder("");
        
        this.setTitle(Elvira.localize(dialogBundle,"Import.title"));
        this.setSize(new Dimension(501, 356));
        importPanel.setPreferredSize(new Dimension(501, 354));
        csvFilePanel.setBorder(null);
        csvFilePanel.setPreferredSize(new Dimension(10, 60));
        csvFilePanel.setLayout(null);
        importCancelPanel.setBorder(null);
        importCancelPanel.setPreferredSize(new Dimension(5, 50));
        importCancelPanel.setLayout(null);
        eastPanel.setMinimumSize(new Dimension(20, 10));
        eastPanel.setPreferredSize(new Dimension(15, 10));
        centralPanel.setEnabled(true);
        centralPanel.setFont(new java.awt.Font("MS Sans Serif", 0, 11));
        centralPanel.setForeground(Color.black);
        centralPanel.setPreferredSize(new Dimension(15, 5));
        westPanel.setPreferredSize(new Dimension(30, 10));
        westPanel.setPreferredSize(new Dimension(15, 10));
        
        //Inicialise components of option panel
        
        csvFileLabel.setText(Elvira.localize(dialogBundle,"Import.CsvFile.label"));
        csvFileLabel.setBounds(new Rectangle(35, 3, 292, 20));
        csvFileText.setText("[none]");
        csvFileText.setEnabled(false);
        csvFileText.setBounds(new Rectangle(24, 27, 307, 24));
        csvFileButton.setBounds(new Rectangle(359, 27, 104, 23));
        
        csvFileButton.setToolTipText(Elvira.localize(dialogBundle,"Import.CsvFileChooser.label"));
        csvFileButton.setMnemonic('S');
        
        csvFileButton.setText(Elvira.localize(dialogBundle,"Import.Browse.button"));
        csvFileButton.addActionListener(new
        ImportDialog_csvFileButton_actionAdapter(this));
        
        
        importButton.setText(Elvira.localize(dialogBundle,"Import.Import.label"));
        importButton.addActionListener(new
        ImportDialog_importButton_actionAdapter(this));
        importButton.setBounds(new Rectangle(107, 12, 105, 25));
        importButton.setMnemonic('I');
        //en principio deshabilitado hasta que no se ha analizado el fichero .csv
        //importButton.setEnabled(false);
        
        cancelButton.setText(Elvira.localize(dialogBundle,"Cancel.label"));
        cancelButton.addActionListener(new
        ImportDialog_cancelButton_actionAdapter(this));
        cancelButton.setBounds(new Rectangle(296, 12, 105, 25));
        cancelButton.setMnemonic('C');
        optionPanel.setLayout(null);
        dbcFileLabel.setBounds(new Rectangle(20, 155, 292, 20));
        
        dbcFileLabel.setText(Elvira.localize(dialogBundle,"Import.DbcFile.label"));
        dbcFileText.setBounds(new Rectangle(23, 177, 286, 24));
        dbcFileText.setText("[none]");
        dbcFileText.setEnabled(false);
        
        dbcFileButton.setText(Elvira.localize(dialogBundle,"Import.Browse.button"));
        dbcFileButton.addActionListener(new
        ImportDialog_dbcFileButton_actionAdapter(this));
        dbcFileButton.setBounds(new Rectangle(320, 174, 108, 28));
        
        dbcFileButton.setToolTipText(Elvira.localize(dialogBundle,"Import.DbcFileChooser.label"));
        
        optionLabel1.setText(Elvira.localize(dialogBundle,"Import.Separator.label"));
        optionLabel1.setBounds(new Rectangle(20, 65, 158, 15));
        
        //Initialise components of Force panel
        
        forceLabel1.setText(Elvira.localize(dialogBundle,"Import.ForceAdvise.label"));
        forceLabel1.setBounds(new Rectangle(18, 2, 327, 29));
        forcePanel.setLayout(null);
        
        forceLabel1b.setText(Elvira.localize(dialogBundle,"Import.ForceAdvise.label3"));
        forceLabel2b.setText(Elvira.localize(dialogBundle,"Import.ForceAdvise.label4"));
        forceLabel1b.setBounds(new Rectangle(31, 48, 385, 60));
        forceLabel2b.setBounds(new Rectangle(31, 96, 385, 26));
        forceLabel1b.setVisible(false);
        forceLabel2b.setVisible(false);
        String forceMassiveString =
        Elvira.localize(dialogBundle,"Import.ForceMassive.label");
        forceMassiveButton.setText(forceMassiveString);
        forceMassiveButton.setBounds(new Rectangle(19, 44, 86, 24));
        forceMassiveButton.addActionListener(new
        ImportDialog_forceMassiveButton_actionAdapter(this));
        forceMassiveButton.setActionCommand(forceMassiveString);
        forceMassiveButton.setEnabled(false);
        String forceNothingString =
        Elvira.localize(dialogBundle,"Import.ForceNothing.label");
        forceNothingButton.setText(forceNothingString);
        forceNothingButton.setBounds(new Rectangle(105, 45, 102, 23));
        forceNothingButton.addActionListener(new
        ImportDialog_forceNothingButton_actionAdapter(this));
        forceNothingButton.setActionCommand(forceNothingString);
        forceNothingButton.setEnabled(false);
        //esta es la de por defecto
        forceNothingButton.setSelected(true);
        String forceNormalString =
        Elvira.localize(dialogBundle,"Import.ForceNormal.label");
        forceNormalButton.setText(forceNormalString);
        forceNormalButton.setBounds(new Rectangle(205, 45, 146, 23));
        forceNormalButton.addActionListener(new
        ImportDialog_forceNormalButton_actionAdapter(this));
        forceNormalButton.setEnabled(false);
        forceNormalButton.setActionCommand(forceNormalString);
        
        forceLabel2.setText(Elvira.localize(dialogBundle,"Import.ForceAdvise.label2"));
        forceLabel2.setBounds(new Rectangle(17, 25, 308, 20));
        forceSubPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        forceSubPanel.setBounds(new Rectangle(16, 96, 434, 116));
        
        importPanel.setLayout(borderLayout1);
        forceComboBox2.setBounds(new Rectangle(218, 70, 115, 21));
        forceComboBox2.setEnabled(false);
        
        //Inicialise components of edit panel. En principio deshabilitados
        
        editLabel1.setText(Elvira.localize(dialogBundle,"Import.ChooseEditVariable.label"));
        editLabel1.setBounds(new Rectangle(27, 6, 176, 22));
        editPanel.setLayout(null);
        editVariablesComboBox2.setBounds(new Rectangle(278, 9, 128, 22));
        editVariablesComboBox2.setEnabled(false);
        
        editTitleLabel.setText(Elvira.localize(dialogBundle,"Import.Title.label"));
        editTitleLabel.setBounds(new Rectangle(226, 17, 62, 17));
        editTitleLabel.setEnabled(false);
        editNameLabel.setBounds(new Rectangle(30, 17, 62, 17));
        
        editNameLabel.setText(Elvira.localize(dialogBundle,"Import.Name.label"));
        editNameLabel.setEnabled(false);
        editNameText.setText("");
        editNameText.setHorizontalAlignment(SwingConstants.RIGHT);
        editNameText.setBounds(new Rectangle(111, 18, 98, 18));
        editNameText.setEnabled(false);
        editNameText.addActionListener(new
        ImportDialog_editNameText_actionAdapter(this));
        editTitleText.setText("");
        editTitleText.setHorizontalAlignment(SwingConstants.RIGHT);
        editTitleText.setBounds(new Rectangle(306, 18, 99, 18));
        editTitleText.setEnabled(false);
        editTitleText.addActionListener(new
        ImportDialog_editTitleText_actionAdapter(this));
        
        editCommentLabel.setText(Elvira.localize(dialogBundle,"Import.Comment.label"));
        editCommentLabel.setBounds(new Rectangle(30, 42, 107, 20));
        editCommentLabel.setEnabled(false);
        editCommentText.setText("");
        editCommentText.setHorizontalAlignment(SwingConstants.RIGHT);
        editCommentText.setBounds(new Rectangle(143, 41, 263, 21));
        editCommentText.setEnabled(false);
        editCommentText.addActionListener(new
        ImportDialog_editCommentText_actionAdapter(this));
        editOptionStatesLabel.setOpaque(false);
        
        editOptionStatesLabel.setText(Elvira.localize(dialogBundle,"Import.States.label"));
        editOptionStatesLabel.setBounds(new Rectangle(30, 66, 183, 21));
        editOptionStatesLabel.setEnabled(false);
        editStatesComboBox2.setBounds(new Rectangle(231, 67, 174, 22));
        editStatesComboBox2.setEnabled(false);
        editOptionStatesNameText.setText("");
        editOptionStatesNameText.setBounds(new Rectangle(238, 94, 152, 21));
        editOptionStatesNameText.setEnabled(false);
        editOptionStatesNameText.addActionListener(new
        ImportDialog_editOptionStatesNameText_actionAdapter(this));
        editOptionStatesNameLabel.setBounds(new Rectangle(125, 93, 116, 19));
        editOptionStatesNameLabel.setForeground(Color.black);
        
        editOptionStatesNameLabel.setText(Elvira.localize(dialogBundle,"Import.StatesNewName.label"));
        editOptionStatesNameLabel.setEnabled(false);
        
        //more componentes de option panel
        jLabel3.setBorder(titledBorder4);
        jLabel3.setText(Elvira.localize(dialogBundle,"Import.OptionCsv.label"));
        jLabel3.setBounds(new Rectangle(17, 13, 211, 21));
        jLabel5.setBounds(new Rectangle(21, 101, 211, 21));
        jLabel5.setText(Elvira.localize(dialogBundle,"Import.OptionDbc.label"));
        jLabel5.setBorder(titledBorder4);
        jLabel6.setText(Elvira.localize(dialogBundle,"Import.DBName.label"));
        jLabel6.setBounds(new Rectangle(20, 131, 204, 17));
        optionDBNameText.setBounds(new Rectangle(243, 132, 185, 20));
        optionDBNameText.setText("Untitled");
        optionDBNameText.setHorizontalAlignment(SwingConstants.LEADING);
        optionDBNameText.addActionListener(new
        ImportDialog_optionDBNameText_actionAdapter(this));
        
        optionRadioButton1a.setText("  ; ");
        optionRadioButton1a.setBounds(new Rectangle(200, 61, 49, 23));
        optionRadioButton1a.setFont(new java.awt.Font("MS Sans Serif", 0, 11));
        optionRadioButton1a.setSelected(true);
        
        optionRadioButton1b.setText("  ,");
        optionRadioButton1b.setBounds(new Rectangle(247, 62, 54, 23));
        
        
        forceProcessButton.setBounds(new Rectangle(362, 12, 88, 76));
        
        forceProcessButton.setText(Elvira.localize(dialogBundle,"Import.Process.button"));
        forceProcessButton.setIcon(new ImageIcon(imgPath + "gear.gif"));
        forceProcessButton.setMnemonic('P');
        forceProcessButton.setHorizontalTextPosition(SwingConstants.CENTER);
        forceProcessButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        forceProcessButton.addActionListener(new
        ImportDialog_forceProcessButton_actionAdapter(this));
        forceProcessButton.setEnabled(false);
        subForceNameText.setHorizontalAlignment(SwingConstants.RIGHT);
        subForceNameText.setHorizontalTextPosition(SwingConstants.TRAILING);
        subForceTitleText.setHorizontalAlignment(SwingConstants.RIGHT);
        subForceNoCasesText.setHorizontalAlignment(SwingConstants.RIGHT);
        subForceMaxText.setHorizontalAlignment(SwingConstants.RIGHT);
        subForceMinText.setHorizontalAlignment(SwingConstants.RIGHT);
        subForceMinText.setHorizontalTextPosition(SwingConstants.TRAILING);
        subForceNoStatesText.setHorizontalAlignment(SwingConstants.RIGHT);
        
        editProcessButton.setEnabled(false);
        
        editProcessButton.setToolTipText(Elvira.localize(dialogBundle,"Import.EditToolTip.label"));
        editProcessButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        editProcessButton.addActionListener(new
        ImportDialog_editProcessButton_actionAdapter(this));
        editProcessButton.setHorizontalTextPosition(SwingConstants.CENTER);
        editProcessButton.setMnemonic('E');
        
        editProcessButton.setText(Elvira.localize(dialogBundle,"Import.SaveChanges.label"));
        editProcessButton.setBounds(new Rectangle(139, 127, 152, 25));
        editSubPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        editSubPanel.setBounds(new Rectangle(11, 47, 442, 164));
        editSubPanel.setLayout(null);
        forceLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        forceLabel3.setHorizontalTextPosition(SwingConstants.TRAILING);
        optionLabel4.setText(Elvira.localize(dialogBundle,"Import.OptionLabel4.label"));
        optionLabel4.setBounds(new Rectangle(20, 38, 328, 20));
        
        optionAnalyseButton.setIcon(new ImageIcon(imgPath + "gear.gif"));
        optionAnalyseButton.setMnemonic('A');
        optionAnalyseButton.setHorizontalTextPosition(SwingConstants.CENTER);
        optionAnalyseButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        
        
        optionAnalyseButton.setBounds(new Rectangle(340, 23, 88,76));
        optionAnalyseButton.setText(Elvira.localize(dialogBundle,"Import.AnalyseButton.button"));
        optionAnalyseButton.addActionListener(new ImportDialog_optionAnalyseButton_actionAdapter(this));
        optionAnalyseButton.setEnabled(false);
        editExplanationLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        editExplanationLabel.setText(Elvira.localize(dialogBundle,"Import.EditExplanationLabel.label"));
        editExplanationLabel.setBounds(new Rectangle(27, 27, 210, 18));
        buttonGroupOption1.add(optionRadioButton1a);
        buttonGroupOption1.add(optionRadioButton1b);
        
        forceSubPanel.setLayout(null);
        
        subForceNameLabel.setText(Elvira.localize(dialogBundle,"Import.Name.label"));
        subForceNameLabel.setBounds(new Rectangle(21, 13, 62, 17));
        subForceNameLabel.setEnabled(false);
        subForceNameText.setBorder(BorderFactory.createEtchedBorder());
        subForceNameText.setText("");
        subForceNameText.setBounds(new Rectangle(89, 12, 100, 19));
        subForceNameText.setEnabled(false);
        subForceTitleText.setBounds(new Rectangle(308, 12, 102, 18));
        subForceTitleText.setText("");
        subForceTitleText.setBorder(BorderFactory.createEtchedBorder());
        subForceTitleText.setEnabled(false);
        subForceTitleLabel.setBounds(new Rectangle(225, 13, 62, 17));
        
        subForceTitleLabel.setText(Elvira.localize(dialogBundle,"Import.Title.label"));
        subForceTitleLabel.setEnabled(false);
        subForceNoStatesText.setBounds(new Rectangle(138, 36, 51, 18));
        subForceNoStatesText.setText("");
        subForceNoStatesText.setBorder(BorderFactory.createEtchedBorder());
        subForceNoStatesText.setEnabled(false);
        subForceNoStatesLabel.setBounds(new Rectangle(21, 36, 105, 18));
        
        subForceNoStatesLabel.setText(Elvira.localize(dialogBundle,"Import.NoStates.label"));
        subForceNoStatesLabel.setEnabled(false);
        
        subForceMaxLabel.setText(Elvira.localize(dialogBundle,"Import.MaxValue.label"));
        subForceMaxLabel.setBounds(new Rectangle(225, 60, 109, 17));
        subForceMaxLabel.setEnabled(false);
        subForceMinText.setBounds(new Rectangle(138, 61, 50, 17));
        subForceMinText.setText("");
        subForceMinText.setBorder(BorderFactory.createEtchedBorder());
        subForceMinText.setEnabled(false);
        subForceMaxText.setBorder(BorderFactory.createEtchedBorder());
        subForceMaxText.setText("");
        subForceMaxText.setBounds(new Rectangle(336, 60, 74, 19));
        subForceMaxText.setEnabled(false);
        subForceMinLabel.setBounds(new Rectangle(21, 58, 117, 21));
        
        subForceMinLabel.setText(Elvira.localize(dialogBundle,"Import.MinValue.label"));
        subForceMinLabel.setEnabled(false);
        
        forceLabel3.setText(Elvira.localize(dialogBundle,"Import.ForceQuestion.label"));
        forceLabel3.setBounds(new Rectangle(101, 89, 188, 21));
        forceLabel3.setEnabled(false);
        forceCheckBox.setEnabled(true);
        forceCheckBox.setSelected(false);
        forceCheckBox.setText("");
        forceCheckBox.setBounds(new Rectangle(291, 88, 83, 23));
        forceCheckBox.addItemListener(new
        ImportDialog_forceCheckBox_itemAdapter(this));
        forceCheckBox.setEnabled(false);
        forceCheckBox.addActionListener(new
        ImportDialog_forceCheckBox_actionAdapter(this));
        
        subForceNoCasesLabel.setText(Elvira.localize(dialogBundle,"Import.NoCases.label"));
        subForceNoCasesLabel.setBounds(new Rectangle(225, 36, 94, 18));
        subForceNoCasesLabel.setEnabled(false);
        subForceNoCasesText.setBorder(BorderFactory.createEtchedBorder());
        subForceNoCasesText.setText("");
        subForceNoCasesText.setBounds(new Rectangle(336, 37, 74, 17));
        subForceNoCasesText.setEnabled(false);
        
        forceSubPanel.add(subForceNameLabel, null);
        forceSubPanel.add(subForceMaxText, null);
        forceSubPanel.add(subForceMinText, null);
        forceSubPanel.add(subForceNoStatesText, null);
        forceSubPanel.add(subForceTitleText, null);
        forceSubPanel.add(subForceNoStatesLabel, null);
        forceSubPanel.add(subForceNoCasesText, null);
        forceSubPanel.add(subForceNameText, null);
        forceSubPanel.add(subForceMinLabel, null);
        forceSubPanel.add(forceCheckBox, null);
        forceSubPanel.add(forceLabel3, null);
        forceSubPanel.add(subForceNoCasesLabel, null);
        forceSubPanel.add(subForceTitleLabel, null);
        forceSubPanel.add(subForceMaxLabel, null);
        forcePanel.add(forceLabel1, null);
        forcePanel.add(forceLabel2, null);
        forcePanel.add(forceNothingButton, null);
        forcePanel.add(forceNormalButton, null);
        forcePanel.add(forceComboBox2, null);
        forcePanel.add(forceMassiveButton, null);
        forcePanel.add(forceProcessButton, null);
        forcePanel.add(forceLabel1b, null);
        forcePanel.add(forceLabel2b, null);
        forcePanel.add(forceSubPanel, null);
        
        optionPanel.add(jLabel3, null);
        optionPanel.add(dbcFileText, null);
        optionPanel.add(jLabel5, null);
        optionPanel.add(optionRadioButton1a, null);
        optionPanel.add(optionRadioButton1b, null);
        optionPanel.add(jLabel6, null);
        optionPanel.add(dbcFileLabel, null);
        optionPanel.add(optionLabel4, null);
        optionPanel.add(optionLabel1, null);
        optionPanel.add(dbcFileButton, null);
        optionPanel.add(optionDBNameText, null);
        optionPanel.add(optionAnalyseButton, null);
        
        buttonGroupForce.add(forceMassiveButton);
        buttonGroupForce.add(forceNothingButton);
        buttonGroupForce.add(forceNormalButton);
        
        importPanel.add(csvFilePanel, BorderLayout.NORTH);
        this.getContentPane().add(importPanel, BorderLayout.CENTER);
        importPanel.add(westPanel, BorderLayout.WEST);
        importPanel.add(eastPanel, BorderLayout.EAST);
        importPanel.add(importCancelPanel, BorderLayout.SOUTH);
        importPanel.add(centralPanel, BorderLayout.CENTER);
        csvFilePanel.add(csvFileLabel, null);
        csvFilePanel.add(csvFileText, null);
        csvFilePanel.add(csvFileButton, null);
        importCancelPanel.add(cancelButton, null);
        importCancelPanel.add(importButton, null);
        
        editSubPanel.add(editNameLabel, null);
        editSubPanel.add(editNameText, null);
        editSubPanel.add(editTitleLabel, null);
        editSubPanel.add(editCommentText, null);
        editSubPanel.add(editStatesComboBox2, null);
        editSubPanel.add(editOptionStatesNameLabel, null);
        editSubPanel.add(editTitleText, null);
        editSubPanel.add(editCommentLabel, null);
        editSubPanel.add(editOptionStatesLabel, null);
        editSubPanel.add(editProcessButton, null);
        editSubPanel.add(editOptionStatesNameText, null);
        editPanel.add(editLabel1, null);
        editPanel.add(editVariablesComboBox2, null);
        editPanel.add(editExplanationLabel, null);
        editPanel.add(editSubPanel, null);
        
        
        centralPanel.add(optionPanel,   Elvira.localize(dialogBundle, "Import.OptionPanel.label"), 0);
        centralPanel.add(editPanel,    Elvira.localize(dialogBundle, "Import.EditPanel.label"), 1);
        centralPanel.add(forcePanel,    Elvira.localize(dialogBundle, "Import.ForcePanel.label"), 2);
        
    }
    
    
    /**
     * Returns the active dialog, use this method in order to invoke any specific method of
     * the dialog
     *
     * @return  the active dialog
     */
    
    public JDialog getActiveDialog() {
        return this;
    } //end getActiveDialog()
    
    
    
    /**
     *
     * Prints, in the .dbc file,information about variables and registers
     *
     */
    
    
    public void printDBC() {
        
        File dbcFile = new File(dbcFileText.getText());
        
        try {
            FileWriter fw = new FileWriter(dbcFile);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter exit = new PrintWriter(bw);
            
            // Checking
            Name dataBaseName = new Name();
            double precision;
            int intPrecision;
            
            dataBaseName.chain = optionDBNameText.getText();
            dataBaseName.chain = dataBaseName.checkSymbols();
            dataBaseName.chain = dataBaseName.checkNumbers();
            
            exit.println("// Data Base \""+(csvFileDialog.getSelectedFile().getName())+ ".\" Elvira Format");
            
            exit.println("\ndata-base " + dataBaseName.chain + " {");
            exit.println("\nnumber-of-cases = " + (regNumber - 1) + ";");
            exit.println("");
            
            for (int i = 0; i < variables; i++) {
                if ( (variablesSet[i].getNumeric() == false) ||
                (variablesSet[i].getContinuous() == false)) {
                    //non-numeric variables
                    
                    variablesSet[i].variableName =ImportVariable.checkNumbers(variablesSet[i].variableName);
                    variablesSet[i].variableName =ImportVariable.checkSymbols(variablesSet[i].variableName);
                    
                    exit.println("");
                    exit.println("node " + variablesSet[i].variableName +" (finite-states) {");
                    
                    if (variablesSet[i].getEdited() == true) {
                        exit.println("title = \""+title[i]+"\";");
                    }
                    else
                        exit.println("title = \"" + "Variable " + i + "\";");
                    
                    exit.println("kind-of-node = chance;");
                    exit.println("type-of-variable = finite-states;");
                    
                    exit.println("pos_x = 100;");
                    exit.println("pos_y = 100;");
                    
                    //The comment
                    if (variablesSet[i].getEdited() == true) {
                        exit.println("purpose = \""+comment[i]+"\";");
                        
                    }
                    else {
                        exit.println("purpose = \"" + "\";");
                    }
                    
                    
                    exit.println("num-states = "+variablesSet[i].casesNumber+";");
                    
                    exit.print("states = (");
                    for (int j = 0; j < variablesSet[i].casesNumber; j++) {
                        exit.print(variablesSet[i].cases[j] + " ");
                    }
                    exit.println(");");
                    
                    
                    // End of the Variable
                    exit.println("}");
                    
                } //end if if (variablesSet[i].getNumeric()==false) {
                else {
                    
                    variablesSet[i].variableName =ImportVariable.checkNumbers(variablesSet[i].variableName);
                    variablesSet[i].variableName =ImportVariable.checkSymbols(variablesSet[i].variableName);
                    
                    if (variablesSet[i].getContinuous() == false) {
                        exit.println("");
                        exit.println("node " + variablesSet[i].variableName +" (finite-states) {");
                    }
                    else {
                        exit.println("");
                        exit.println("node " + variablesSet[i].variableName +" (continuous) {");
                    }
                    
                    /* Title */
                    if (variablesSet[i].getEdited() == true) {
                        exit.println("title = \""+title[i]+"\";");
                        exit.println("kind-of-node = chance;");
                        if (variablesSet[i].getContinuous() == false) {
                            exit.println("type-of-variable = finite-states;");
                        }
                        else {
                            exit.println("type-of-variable = continuous;");
                        }
                        
                        exit.println("pos_x = 100;");
                        exit.println("pos_y = 100;");
                        
                        // Setting the comment
                        if (variablesSet[i].getContinuous() == false)
                            exit.println("purpose = \""+comment[i]+"\";");
                        else {
                            if (variablesSet[i].getContinuous() == true) {
                                if (variablesSet[i].isNumeric == true) {
                                    exit.print("purpose = \"The rank of values is: [" +
                                    variablesSet[i].minValue.toString() + "," +
                                    variablesSet[i].maxValue.toString() + "]");
                                    
                                    if ((comment[i].compareTo("")!=0)&&(comment[i].compareTo(" ")!=0))
                                        exit.print(", " +comment[i]);
                                    
                                    exit.println("\";");
                                    
                                    exit.println("min = " +variablesSet[i].minValue.toString() +";");
                                    exit.println("max = " +variablesSet[i].maxValue.toString() +";");
                                    
                                    //if ((variablesSet[i].minValue.intValue()) == (variablesSet[i].minValue.doubleValue()))
                                    //    exit.println("min = " +(variablesSet[i].minValue).intValue() +" ;");
                                    //else
                                    //    exit.println("min = " +variablesSet[i].minValue.toString() +" ;");
                                    
                                    //if ((variablesSet[i].maxValue.intValue()) == (variablesSet[i].maxValue.doubleValue()))
                                    //    exit.println("max = " +(variablesSet[i].maxValue).intValue() +" ;");
                                    //else
                                    //    exit.println("max = " +variablesSet[i].maxValue.toString() +" ;");
                                    
                                    
                                }
                                else {
                                    exit.println("purpose = \"The rank of values is: [" +
                                    variablesSet[i].minValue.toString() + "," +
                                    variablesSet[i].maxValue.toString() + "]");
                                    if ((comment[i].compareTo("")!=0)&&(comment[i].compareTo(" ")!=0))
                                        exit.print(", " +comment[i]);
                                    
                                    exit.println("\";");
                                    
                                    exit.println("min = " +variablesSet[i].minValue.toString() +";");
                                    exit.println("max = " +variablesSet[i].maxValue.toString() +";");
                                    
                                    //if ((variablesSet[i].minValue.intValue()) == (variablesSet[i].minValue.doubleValue()))
                                    //    exit.println("min = " +(variablesSet[i].minValue).intValue() +" ;");
                                    //else
                                    //    exit.println("min = " +variablesSet[i].minValue.toString() +" ;");
                                    
                                    //if ((variablesSet[i].maxValue.intValue()) == (variablesSet[i].maxValue.doubleValue()))
                                    //    exit.println("max = " +(variablesSet[i].maxValue).intValue() +" ;");
                                    //else
                                    //    exit.println("max = " +variablesSet[i].maxValue.toString() +" ;");
                                    
                                }
                            }
                            
                        }
                        
                        // End of the Variable
                        exit.println("}");
                    }
                    else {
                        exit.println("title = \"" + "Variable " + i + "\";");
                        exit.println("kind-of-node = chance;");
                        
                        if (variablesSet[i].getContinuous() == false) {
                            exit.println("type-of-variable = finite-states;");
                        }
                        else {
                            exit.println("type-of-variable = continuous;");
                        }
                        
                        exit.println("pos_x = 100;");
                        exit.println("pos_y = 100;");
                        
                        if (variablesSet[i].getContinuous() == false) {
                            exit.println("purpose = \""+ "\";");
                        }
                        else
                            if (variablesSet[i].getContinuous() == true) {
                                exit.print("purpose = \"The rank of values is: [" +
                                variablesSet[i].minValue.toString() + "," +
                                variablesSet[i].maxValue.toString() + "]");
                                
                                if ((comment[i].compareTo("")!=0)&&(comment[i].compareTo(" ")!=0))
                                    exit.print(", " +comment[i]);
                                
                                exit.println("\";");
                                
                                exit.println("min = " +variablesSet[i].minValue.toString() +";");
                                exit.println("max = " +variablesSet[i].maxValue.toString() +";");
                                
                                //if ((variablesSet[i].minValue.intValue()) == (variablesSet[i].minValue.doubleValue()))
                                //    exit.println("min = " +(variablesSet[i].minValue).intValue() +" ;");
                                //else
                                //    exit.println("min = " +variablesSet[i].minValue.toString() +" ;");
                                
                                //if ((variablesSet[i].maxValue.intValue()) == (variablesSet[i].maxValue.doubleValue()))
                                //    exit.println("max = " +(variablesSet[i].maxValue).intValue() +" ;");
                                //else
                                //    exit.println("max = " +variablesSet[i].maxValue.toString() +" ;");
                                
                            }
                        exit.println("}");
                    }
                } //end else
            } //end for
            
            //Inserting in dbc file
            exit.println("");
            exit.println("");
            exit.println("relation {");
            exit.println("");
            exit.println("memory = true;");
            exit.println("");
            exit.println("cases = (");
            for (int i = 0; i < regNumber; i++) {
                exit.print("[");
                for (int j = 0; j < variables - 1; j++) {
                    exit.print(variablesSet[j].registers[i] + ",");
                }
                exit.println(variablesSet[variables - 1].registers[i] + "]");
            }
            exit.println(");");
            exit.println("");
            exit.println("}");
            exit.println("}");
            
            exit.close();
            jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,
            "Import.SuccessfulImport.label"),Elvira.localize(messagesBundle,
            "Import.Elvira.label"),jOPanel.PLAIN_MESSAGE);
            setVisible(false);
        }
        catch (IOException salidaexception) {
            jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,
            "Import.DbcFileError"),Elvira.localize(messagesBundle,
            "Import.Elvira.label"),jOPanel.ERROR_MESSAGE);
        }
        
    } //end print
    
    
    /**
     *
     * Analyses the .csv file and sets properties's variables
     *
     * @return error return true if any problem has happened, false, otherwise
     */
    
    
    
    public boolean analyseCsvFile() {
        
        //Selecting the entry file's name and path
        
        boolean error = false;
        
        try {
            
            File incoming = new File(csvFileText.getText());
            
            //Assign a place in memory
            RandomAccessFile inFile = new RandomAccessFile(incoming, "r");
            
            String line1; // The file's first line
            line1 = inFile.readLine(); //Read the first line
            StringTokenizer tokens = new StringTokenizer(line1, separator);
            //Set the ';' char to delimit the values
            
            if (tokens.countTokens() < 2) {
                //Show an error message
                jOPanel.showMessageDialog(this,
                Elvira.localize(messagesBundle,"Import.CsvFileError"),
                Elvira.localize(messagesBundle,"Import.Elvira.label"),
                jOPanel.ERROR_MESSAGE);
                error = true;
                
            }
            else {
                variables = tokens.countTokens();
                inFile.seek(0); //We are in the file's first line (The variable's name is here)
                String lineFirst = inFile.readLine(); //Read the first line
                String line;
                //inFile.seek(1); // We are in the second line. No hace falta porque ya estamos en la segunda linea. Carlos
                regNumber = 0;
                
                // Count the number of registers
                while ( (line = inFile.readLine()) != null) {
                    regNumber++;
                }
                
                // A new object
                variablesSet = new ImportVariable[variables];
                
                // Inizialize the variables' array
                for (int i = 0; i < variables; i++) {
                    variablesSet[i] = new ImportVariable();
                    variablesSet[i].initialise(regNumber); //Inicializar el numero de registros.
                }
                
                // Analyse the first line.
                StringTokenizer firstAnalysis = new StringTokenizer(lineFirst,
                separator);
                String temporal;
                boolean repeatedVar = false;
                for (int i = 0; i < variables; i++) {
                    temporal = firstAnalysis.nextToken();
                    //checks variables's names
                    temporal = ImportVariable.checkNumbers(temporal);
                    temporal = ImportVariable.checkSymbols(temporal);
                    
                    //compruebo que no he metido ya una variables con ese nombre
                    //check if this variables exits
                    
                    for (int k = 0; (k < i) && (!repeatedVar); k++){
                        if (variablesSet[k].getName().compareTo(temporal)==0) {
                            repeatedVar = true;
                        }
                        
                    }
                    
                    if (repeatedVar){
                        //Repeated name for the variable
                        //shows an error message and doesn't saves the changes
                        //le a�ado "1" al final
                        temporal = temporal+"1";
                        //jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Import.RepeatedNameVariable"),Elvira.localize(dialogBundle,"Import.Elvira.label"),jOPanel.ERROR_MESSAGE);
                    }
                    
                    variablesSet[i].setName(temporal);
                    
                }
                
                // To read the next lines.
                String line2 = new String();
                inFile.seek(0);
                line2 = inFile.readLine();
                String registerToken = new String();
                
                for (int i = 0; i <= regNumber - 1; i++) {
                    line2 = inFile.readLine();
                    
                    //preproceso: cambio todos los ;; por ;?;
                    
                    int numeroVariables=0;
                    String newString = "?";
                    int index1=line2.indexOf(separator.charAt(0));
					int index2=-1;
					// Compruebo la primera letra de la linea 
                	if (line2.charAt(0)==separator.charAt(0))
                	{
                		line2=newString+line2;
                		index1=line2.indexOf(separator.charAt(0));
                	}
                	// Compruebo la ultima letra de la linea
                	if (line2.charAt(line2.length()-1)==separator.charAt(0))
                	{
                		line2=line2+newString;
                	}
					while (numeroVariables<variables-2)
                    {
                   		index2 = line2.indexOf(separator.charAt(0), index1+1);
                   		// Compruebo si hay ;; para sustituirlo por ;?;
                   		if (index2-index1==1)
                   		{
                   			String subString=line2.substring(0, index1+1);
                   			String superString=line2.substring(index2, line2.length());
                   			line2=subString+newString+superString;
                   			index2++;
                   			numeroVariables++;
                   		}
                   		else
                   		{
              				numeroVariables++;
                   		}
                   		index1=index2;
                    }
                    
                    StringTokenizer lineToken = new StringTokenizer(line2,separator);
                    
                    if (lineToken.countTokens() > variables) {
                        jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle, "Import.CsvFileError"),
                        Elvira.localize(dialogBundle,"Import.Elvira.label"),jOPanel.ERROR_MESSAGE);
                        //poner aqui un error de errores en el fichero de entrada
                        error = true;
                    }
                    else{
                        boolean enBlanco = false;
                        for (int j = 0; j < variables; j++) {
                                registerToken = lineToken.nextToken();
                                variablesSet[j].addRegister(registerToken);
                        }//end for j                    
                    }//del else
                }//end for i
                
                
                try{                  
                    // Conversion process
                    for (int i = 0; i < variables; i++) {
                        variablesSet[i].makeConversion();
                    }
                    
                }//end try
                catch(NullPointerException e5){
                    throw new NullPointerException("�Hay l�neas en blanco al final del fichero .csv?\n");
                    
                    //dispose();
                    //this.setVisible(false);
                    //jOPanel.setVisible(false);
                }//end catch
                
                variablesName = new String[variables];
                String nameVar;
                
                for (int i = 0; i < variables; i++) {
                    nameVar = tokens.nextToken();
                    nameVar = ImportVariable.checkNumbers(nameVar);
                    nameVar = ImportVariable.checkSymbols(nameVar);
                    variablesName[i] = nameVar;
                }
                
            } //end else
            
        }
        catch (java.io.FileNotFoundException e2) {
            error = true;
        }
        catch (java.io.IOException e3) {
            error = true;
        }
        
        return error;
        
    }
    
    
    /**
     * Force variable i to be continuous
     *
     * @param i the position of the variable
     */
    
    public void forceCont( int i){
        
        String aux;
        
        //remove inverted commas and the "s" in registers and cases:
        if (variablesSet[i].isContinuous == false){
            
            for (int j=0 ; j<variablesSet[i].regNumber ; j++) {
                if (variablesSet[i].registers[j].compareTo("?")!=0){
                    aux = new String((variablesSet[i].registers[j]).substring(2,(variablesSet[i].registers[j].length()-1)));
                    variablesSet[i].registers[j] = aux;
                }
            }
            for (int j=0 ; j<variablesSet[i].casesNumber ; j++) {
                aux = new String((variablesSet[i].cases[j]).substring(2,(variablesSet[i].cases[j].length()-1)));
                variablesSet[i].cases[j] = aux;
            }
            
        }
        variablesSet[i].isContinuous = true;
    }
    
    
    
    /**
     * Update panels
     *
     */
    
    
    private void updatePanels(){
        
        //update combobox of editPanel
        //q si cambio el nombre de un estado en el combobox de edit aparezca el cambio
        
        //if it is finite-state, enabled statesComboBox
        for (int i = 0; i < variables; i++){
            if (variablesSet[i].getContinuous() == false) {
                //editStatesCombox2 is empty, we replace with editStatesComboBox[i]
                editStatesComboBox2.setVisible(false);
                editStatesNames = new String[variablesSet[i].casesNumber];
                for (int j = 0; j < variablesSet[i].casesNumber; j++) {
                    //show the state without " "
                    //show the state without "s "
                    String state = variablesSet[i].cases[j];
                    
                    if (variablesSet[i].isContinuous == false){
                        state = state.substring(1, state.length() - 1);
                        if (variablesSet[i].isNumeric == true)
                            state = state.substring(1);
                    }
                    editStatesNames[j] = state;
                }
                
                editSubPanel.remove(editStatesComboBox[i]);
                editStatesComboBox[i] = new JComboBox(editStatesNames);
                editStatesComboBox[i].setBounds(new Rectangle(231, 67, 174, 22));
                
                editStatesComboBox[i].addActionListener(new
                ImportDialog_editStatesComboBox_actionAdapter(this));
                //enabled the text
                editOptionStatesLabel.setEnabled(true);
                editOptionStatesNameLabel.setEnabled(true);
                editOptionStatesNameText.setEnabled(true);
                editSubPanel.add(editStatesComboBox[i], null);
                editPanel.add(editSubPanel, null);
                centralPanel.add(editPanel,
                Elvira.localize(dialogBundle,"Import.EditPanel.label"), 1);
                importPanel.add(centralPanel, BorderLayout.CENTER);
                repaint();
            }//end if es finite-states
            else{
                //q se vea el combobox2, pq si la has forzado a cont se verian los valores
                for (int t = 0; (t < variables) ; t++) {
                    editStatesComboBox[t].setVisible(false);
                }
                editStatesComboBox2.setVisible(true);
                editStatesComboBox2.setEnabled(false);
                editOptionStatesLabel.setEnabled(false);
                editOptionStatesNameLabel.setEnabled(false);
                editOptionStatesNameText.setEnabled(false);
                editSubPanel.add(editStatesComboBox2, null);
                editPanel.add(editSubPanel, null);
                centralPanel.add(editPanel,
                Elvira.localize(dialogBundle,"Import.EditPanel.label"), 1);
                importPanel.add(centralPanel, BorderLayout.CENTER);
                repaint();
            }
        }
        
        //update combobox of forcePanel
        //(a numeric variable can become no numeric if you change states)
        
        forceComboBox2.setVisible(false);
        forceComboBox.setEnabled(true);
        
        int j = 0;
        
        for (int i = 0; i < variables; i++) {
            if ( (variablesSet[i].isNumeric == true) &&
            (variablesSet[i].getContinuous() == false)) {
                j++;
            }
        }
        
        numForceVariables = j;
        
        if (numForceVariables == 0) {
            
            forceComboBox.setVisible(false);
            forceSubPanel.setVisible(false);
            forceNormalButton.setVisible(false);
            forceMassiveButton.setVisible(false);
            forceNothingButton.setVisible(false);
            forceProcessButton.setVisible(false);
            forceLabel1.setVisible(false);
            forceLabel2.setVisible(false);
            forceLabel1b.setVisible(true);
            forceLabel2b.setVisible(true);
        }//end if numForce = 0
        else {//else if numForce = 0
            
            forcePanel.remove(forceComboBox);
            
            forceLabel1b.setVisible(false);
            forceLabel2b.setVisible(false);
            
            posCurrentForceVarVector = new int[numForceVariables];
            
            String[] forceVariablesNames = new String[numForceVariables];
            
            j = 0;
            
            for (int i = 0; i < variables; i++) {
                if ( (variablesSet[i].isNumeric == true) &&
                (variablesSet[i].getContinuous() == false)) {
                    //if it is numeric but not continuous, it can be forced
                    
                    posCurrentForceVarVector[j] = i;
                    forceVariablesNames[j] = variablesSet[i].variableName;
                    j++;
                }
            }
            
            forceComboBox = new JComboBox(forceVariablesNames);
            
            //enable it in radiobutttonforceNormal_actionperformed
            forceComboBox.setEnabled(false);
            forceComboBox.setBounds(new Rectangle(218, 70, 115, 21));
            
            forceCheckBox.setSelected(false);
            for (int i = 0; i < variables; i++)
                variablesSet[i].isforced = false;
            
            
            forceComboBox.addActionListener(new
            ImportDialog_forceComboBox_actionAdapter(this));
            
            forcePanel.add(forceComboBox, null);
            centralPanel.add(forcePanel,
            Elvira.localize(dialogBundle, "Import.ForcePanel.label"),2);
            
        }//end else if numForce = 0
        
        this.repaint();
        
    }//end update Panels
    
    
    /**
     * Overridden so we can exit when window is closed
     *
     * @param e
     */
    
    protected void processWindowEvent(WindowEvent e) {
        
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            setVisible(false);
        }
    }
    
    
    
    
    /*****    COMPONENTS'S ACTION PERFORMED  *****/
    
    
    /**
     * Checks which button of a groupbutton has been selected
     *
     * @param e
     */
    
    public void actionPerformed(ActionEvent e) {
        
        //you can chose "Allways", "Never" or "Study variables"
        
        //At first, NothingButton is selected
        if (e.getActionCommand() == Elvira.localize(dialogBundle,"Import.ForceNothing.label")) {
            forceComboBox.setEnabled(false);
        }
        
        //Force in all variables that can do it
        if (e.getActionCommand() == Elvira.localize(dialogBundle,"Import.ForceMassive.label")) {
            //NO FUNCIONAN
            forceComboBox.setEnabled(false);
        }
        
        if (e.getActionCommand() == Elvira.localize(dialogBundle,"Import.ForceNormal.label")) {
        }
        
        repaint();
        
    }
    
    
    /**
     * Selects the .csv file
     *
     * @param e
     */
    private File lastVisitedDirectory;
    
    void csvFileButton_actionPerformed(ActionEvent e) {
        
        //Set a file filter for .csv files
        csvFileDialog.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File file) {
                String filename = file.getName();
                return (filename.endsWith(".csv") || file.isDirectory());
            }
            //Shows a description in the file dialog
            public String getDescription() {
                return ".csv Files (*.csv)";
            }
        });
        
        if (lastVisitedDirectory==null)
        {
        	csvFileDialog.rescanCurrentDirectory();
        }
        else
        {
        	csvFileDialog.setCurrentDirectory(lastVisitedDirectory);
        }
        //Opens an save dialog to choose the .csv file
        csvFileDialog.setDialogType(csvFileDialog.SAVE_DIALOG);
        csvFileDialog.setDialogTitle(Elvira.localize(dialogBundle,"Import.CsvFileChooser.title"));
        csvFileDialog.setSelectedFile(new File(""));
        
        //If cancel option has been selected, show an error message
        int state = csvFileDialog.showDialog(this.getActiveDialog(),
        Elvira.localize(dialogBundle,"Export.Browse.button"));
        if (state == csvFileDialog.CANCEL_OPTION) {
            csvFileDialog.setSelectedFile(null);
            jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle, "Import.NoCsvFileSelected"),
            Elvira.localize(dialogBundle,"Import.Elvira.label"),jOPanel.ERROR_MESSAGE);
            csvFileText.setText("[none]");
        }
        else
            lastVisitedDirectory=csvFileDialog.getCurrentDirectory();
            if (csvFileDialog.getSelectedFile().getPath().endsWith(".csv")){
                //Inserts .csv file name in .csv file text
                csvFileText.setText(csvFileDialog.getSelectedFile().getPath());
                
                //Enable analyse button
                optionAnalyseButton.setEnabled(true);
                
            }//del if
            else {
                jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Import.NoCsvFileError"),
                Elvira.localize(dialogBundle,"Import.Elvira.label"),jOPanel.ERROR_MESSAGE);
                csvFileText.setText("[none]");
            }
        
    }//end csvFileButton_actionPerformed
    
    
    /**
     * Print in the .dbc file
     *
     * @param e
     */
    
    void importButton_actionPerformed(ActionEvent e) {
        
        if (analizado){
            if ( (dbcFileText.getText().compareTo("[none]") != 0) &&
            (csvFileText.getText().compareTo("[none]") != 0))
                this.printDBC();
            else if (csvFileText.getText().compareTo("[none]") == 0)
                jOPanel.showMessageDialog(this,
                Elvira.localize(messagesBundle, "Import.NoCsvFileSelected"),
                Elvira.localize(dialogBundle,
                "Import.Elvira.label"),
                jOPanel.ERROR_MESSAGE);
            else if (dbcFileText.getText().compareTo("[none]") == 0)
                jOPanel.showMessageDialog(this,
                Elvira.localize(messagesBundle, "Import.NoDbcFileSelected"),
                Elvira.localize(dialogBundle,
                "Import.Elvira.label"),
                jOPanel.ERROR_MESSAGE);
            
        }
        else{
            
            //show a message: file has not been analysed
            jOPanel.showMessageDialog(this,
            Elvira.localize(messagesBundle, "Import.NoDbcFileSelected"),
            Elvira.localize(dialogBundle,
            "Import.Elvira.label"),
            jOPanel.ERROR_MESSAGE);
            
        }
        
    }
    
    
    /**
     * Exit of the import dialog, without performs the importation
     *
     * @param e
     */
    
    void cancelButton_actionPerformed(ActionEvent e) {
        this.dispose();
    }
    
    
    /**
     * Selects the cases file, that is, the .dbc file.
     *
     */
    
    void dbcFileButton_actionPerformed(ActionEvent e) {
        
        //Opens an open dialog to choose the .dbc file, using the elvira files filter
        dbcFileDialog.setDataBaseFilter();
        
        if (lastVisitedDirectory==null)
        {
        	dbcFileDialog.rescanCurrentDirectory();
        }
        else
        {
        	dbcFileDialog.setCurrentDirectory(lastVisitedDirectory);
        }

        dbcFileDialog.setDialogType(dbcFileDialog.OPEN_DIALOG);
        dbcFileDialog.setDialogTitle(Elvira.localize(dialogBundle,
        "Import.DbcFileChooser.title"));
        dbcFileDialog.setSelectedFile(new File("newfile.dbc"));
        
        //If cancel option has been selected, show an error message
        int state = dbcFileDialog.showDialog(this.getActiveDialog(),
        Elvira.localize(dialogBundle,"Import.Browse.button"));
        if (state == dbcFileDialog.CANCEL_OPTION) {
            dbcFileDialog.setSelectedFile(null);
            jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Import.NoDbcFileSelected"),
            Elvira.localize(dialogBundle,"Import.Elvira.label"),jOPanel.ERROR_MESSAGE);
            dbcFileText.setText("[none]");
        }
        else {
        	
            lastVisitedDirectory=dbcFileDialog.getCurrentDirectory();
            if (dbcFileDialog.getSelectedFile().getPath().endsWith(".dbc")) {
                
                //Inserts .dbc file name in .dbc file text
                dbcFileText.setText(dbcFileDialog.getSelectedFile().getPath());
                
                String dbName = dbcFileDialog.getSelectedFile().getName();
                dbName = dbName.substring(0, dbName.lastIndexOf('.'));
                dbName = ImportVariable.checkSymbols(dbName);
                dbName = ImportVariable.checkNumbers(dbName);
                
                if (dbName.compareTo("newfile")==0)
                    dbName = "Untitled";
                
                optionDBNameText.setText(dbName);
            }
            else{
                jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Import.NoDbcFileError"),
                Elvira.localize(dialogBundle,"Import.Elvira.label"),jOPanel.ERROR_MESSAGE);
                dbcFileText.setText("[none]");
            }
            
        }
        
    }//end dbcFileButton_actionPerformed
    
    
    /**
     * Sets the new data base's name
     *
     * @param e
     */
    
    void optionDBNameText_actionPerformed(ActionEvent e) {
        
        String name = optionDBNameText.getText();
        
        //Checks if the new name is a correct name
        if ((name.compareTo("")==0)||name.compareTo(" ")==0)
            name = "Untitled";
        name = ImportVariable.checkNumbers(name);
        name = ImportVariable.checkSymbols(name);
        
    }
    
    
    /**
     * Selects the variable to edit
     *
     * @param e
     */
    
    void editVariablesComboBox_actionPerformed(ActionEvent e) {
        
        // updatePanels();
        
        //coges la variable a editar
        int i = editVariablesComboBox.getSelectedIndex();
        //estamos en la variable i
        
        variablesSet[i].isEdited=true;
        
        posCurrentEditVar = i;
        
        //enables components to edit the variable
        editNameLabel.setEnabled(true);
        editNameText.setEnabled(true);
        editTitleLabel.setEnabled(true);
        editTitleText.setEnabled(true);
        editCommentLabel.setEnabled(true);
        editCommentText.setEnabled(true);
        editProcessButton.setEnabled(true);
        editStatesComboBox[i].setEnabled(true);
        
        //to store new and old values of the edit states
        editStatesVector = new Vector();
        
        for (int t = 0 ; t < variablesSet[i].casesNumber; t++){
            editStatesVector.insertElementAt(null, t);
        }
        
        //if it is continuous, show values's interval and a comment
        
        if (variablesSet[i].getContinuous()==true){
            
            editCommentText.setText("comment = \"The rank of values is: ["+variablesSet[i].minValue.toString() + "," +variablesSet[i].maxValue.toString()
            + "], " + "\";");
        }
        else{
        }
        
        editOptionStatesLabel.setEnabled(true);
        
        //old Value for the name
        editNameText.setText(name[i]);
        //at first , there is not any title
        editTitleText.setText(title[i]);
        //at first , there is not any comment
        editCommentText.setText(comment[i]);
        
        editOptionStatesNameText.setText("");
        
        //if it is finite-state, enabled statesComboBox
        
        if (variablesSet[i].getContinuous() == false) {
            //editStatesCombox2 is empty, we replace with editStatesComboBox[i]
            editStatesComboBox2.setVisible(false);
            for (int t = 0; (t < variables) ; t++)
            {if (t!=i)
                 editStatesComboBox[t].setVisible(false);
            }
            editStatesComboBox[i].setVisible(true);
            editStatesComboBox[i].setEnabled(true);
            //create editStateComboBox[i]
            editStatesComboBox[i].addActionListener(new
            ImportDialog_editStatesComboBox_actionAdapter(this));
            //enabled the text
            editOptionStatesLabel.setEnabled(true);
            editOptionStatesNameLabel.setEnabled(true);
            editOptionStatesNameText.setEnabled(true);
            editSubPanel.add(editStatesComboBox[i], null);
            editPanel.add(editSubPanel, null);
            centralPanel.add(editPanel,
            Elvira.localize(dialogBundle,"Import.EditPanel.label"), 1);
            importPanel.add(centralPanel, BorderLayout.CENTER);
            repaint();
        }//end if es finite-states
        else{
            //q se vea el combobox2, pq si la has forzado a cont se verian los valores
            for (int t = 0; (t < variables) ; t++) {
                editStatesComboBox[t].setVisible(false);
            }
            editStatesComboBox2.setVisible(true);
            editStatesComboBox2.setEnabled(false);
            editOptionStatesLabel.setEnabled(false);
            editOptionStatesNameLabel.setEnabled(false);
            editOptionStatesNameText.setEnabled(false);
            editSubPanel.add(editStatesComboBox2, null);
            editPanel.add(editSubPanel, null);
            centralPanel.add(editPanel,
            Elvira.localize(dialogBundle,"Import.EditPanel.label"), 1);
            importPanel.add(centralPanel, BorderLayout.CENTER);
            repaint();
        }
        
        saveChanges = false;
        
        this.repaint();
        
    }//de editComboBox
    
    
    /**
     * Sets the new name of the variable
     *
     * @param e
     */
    
    void editNameText_actionPerformed(ActionEvent e) {
        
        variablesSet[posCurrentEditVar].setName(editNameText.getText());
        String nameVar = editNameText.getText();
        name[posCurrentEditVar] = nameVar;
        
    }
    
    
    /**
     * Sets the new title of the variable
     *
     * @param e
     */
    
    void editTitleText_actionPerformed(ActionEvent e) {
        title[posCurrentEditVar] = editTitleText.getText();
    }
    
    
    /**
     * Sets the new comment of the variable
     *
     * @param e
     */
    
    void editCommentText_actionPerformed(ActionEvent e) {
        comment[posCurrentEditVar] = editCommentText.getText();
    }
    
    
    /**
     * Selects the state to edit
     *
     * @param e
     */
    
    void editStatesComboBox_actionPerformed(ActionEvent e) {
        
        //updatePanels();
        
        String state;
        //Select the state to edit
        int j = editStatesComboBox[posCurrentEditVar].getSelectedIndex();
        posCurrentEditStates = j;
        
        if (j!=-1){
            
            //enable components to edit the state
            editOptionStatesNameLabel.setEnabled(true);
            editOptionStatesNameText.setEnabled(true);
            
            //sets the old value
            
            state = variablesSet[posCurrentEditVar].cases[posCurrentEditStates];
            
            if (variablesSet[posCurrentEditVar].isContinuous==false){
                state = state.substring(1, state.length()-1);
                if (variablesSet[posCurrentEditVar].isNumeric==true)
                    state = state.substring(1);
            }
           
            editOptionStatesNameText.setText(state);
            
        }
        else {
            
        }
    }
    
    
    private Object makeObj(final String item)  {
        return new Object() { public String toString() { return item; } };
    }
    
    
    /**
     * Sets the new name of the state
     *
     * @param e
     */
    
    void editOptionStatesNameText_actionPerformed(ActionEvent e) {
        
        String values[] = new String[2];
        String oldValue, newValue;
        String state;
        
        state = editOptionStatesNameText.getText();
        
        //check if state is a number
        try {
            Double doubleValue = new Double(state);
        }
        catch( NumberFormatException e1){
            //it is not a number
            //if variable was numeric, now it is not
            if (variablesSet[posCurrentEditVar].isNumeric == true){
                noNum[posCurrentEditVar] = true;
            }
        }
        
        state = ImportVariable.checkSymbols(state);
        
        //add " " and "s" if it is numeric and no continuous
        if (variablesSet[posCurrentEditVar].isContinuous == false){
            try {
                //if it is a number, I have to write "s"
                Double doubleValue = new Double(state);
                state = "s"+state;
            }
            catch( NumberFormatException e2){
            }
            state = "\""+state+"\"";
        }
        
        oldValue = variablesSet[posCurrentEditVar].cases[posCurrentEditStates];
        newValue = state;
        values[0] = oldValue;
        values[1] = newValue;
        editStatesVector.insertElementAt(values , posCurrentEditStates);
    }
    
    
    /**
     * Checks if the variable is selected to force
     * to be continuous
     *
     * @param e
     */
    
    void forceCheckBox_actionPerformed(ActionEvent e) {
        if (forceCheckBox.isSelected()==true){
            
            //esto estaba mal???????????????
            int j = posCurrentForceVar;
            int i = posCurrentForceVarVector[j];
            
            variablesSet[i].isforced = true;
        }
    }
    
    
    /**
     * Changes the state of the forceCheckBox
     *
     * @param e
     */
    
    void forceCheckBox_itemStateChanged(ItemEvent e) {
        
        int j = posCurrentForceVar;
        int i = posCurrentForceVarVector[j];
        
        if (forceCheckBox.isSelected())
            variablesSet[i].isforced = true;
        else
            variablesSet[i].isforced = false;
        
    }
    
    
    /**
     * Selects the variable that you want to force to be continuous
     *
     * @param e
     */
    
    void forceComboBox_actionPerformed(ActionEvent e) {
        
        //hay q tener en cuenta si se ha forzado ya ,
        //por ejemplo mirando aqui si en efecto es discreta
        
        posCurrentForceVar = forceComboBox.getSelectedIndex();
        int j = posCurrentForceVar;
        int i = posCurrentForceVarVector[j];
        
        // enables neccessary components to edit a variable
        
        //????????????
        forceCheckBox.setSelected(variablesSet[i].isforced);
        
        subForceMaxLabel.setEnabled(true);
        subForceMinLabel.setEnabled(true);
        subForceMaxText.setEnabled(true);
        subForceMinText.setEnabled(true);
        subForceNameLabel.setEnabled(true);
        subForceNameText.setEnabled(true);
        subForceNoCasesLabel.setEnabled(true);
        subForceNoCasesText.setEnabled(true);
        subForceNoStatesLabel.setEnabled(true);
        subForceNoStatesText.setEnabled(true);
        subForceTitleLabel.setEnabled(true);
        subForceTitleText.setEnabled(true);
        forceLabel3.setEnabled(true);
        forceCheckBox.setEnabled(true);
        
        //Sets the properties for these component
        subForceNameText.setText(variablesSet[i].getName());
        subForceTitleText.setText("Variable "+i);
        subForceNoCasesText.setText((new
        Integer(variablesSet[i].regNumber)).toString());
        subForceNoStatesText.setText((new
        Integer(variablesSet[i].casesNumber)).toString());
        
        
        subForceMaxText.setText((variablesSet[i].maxValue).toString());
        subForceMinText.setText((variablesSet[i].minValue).toString());
        
    }//end forceComboBox_actionPerformed
    
    
    /**
     * Puts into action the conversions of the variable
     * in force panel
     *
     * @param e
     */
    
    void forceProcessButton_actionPerformed(ActionEvent e) {
        
        //Checks if : forceMassiveButton.isSelected(),
        //forceNothingButton.isSelected() or  forceNormalButton.isSelected()
        int i;
        String information = "";
        
        if ( forceMassiveButton.isSelected()){
            //force to be continuous all variables than can do
            for (int k = 0 ; k < numForceVariables; k++){
                //force variablesSet[i] to be continuous
                i = posCurrentForceVarVector[k];
                forceCont(i);
                information = Elvira.localize(messagesBundle,
                "Import.MassiveForceInformation");
            }
        }
        if ( forceNormalButton.isSelected()){
            //checks all the checkboxs
            information = Elvira.localize(messagesBundle,
            "Import.NormalForceInformation");
            for (int k = 0 ; k < numForceVariables; k++){
                //force variablesSet[i] to be continuous
                i = posCurrentForceVarVector[k];
                if (variablesSet[i].isforced==true) {
                    information = information+"\n    "+variablesSet[i].getName();
                    forceCont(i);
                }
            }
        }
        
        if (forceNothingButton.isSelected())
            information = Elvira.localize(messagesBundle,
            "Import.NothingForceInformation");
        
        
        //show a message: analysis has been correct
        jOPanel.showMessageDialog(this,
        Elvira.localize(messagesBundle,
        "Import.SuccessfulForce.label")+information,
        Elvira.localize(messagesBundle,
        "Import.Elvira.label"),
        jOPanel.PLAIN_MESSAGE);
        
        
        
    }
    
    
    /**
     * Force all variables to be continuous
     *
     * @param e
     */
    
    void forceMassiveButton_actionPerformed(ActionEvent e) {
        
        if (forceMassiveButton.isSelected()) {
            String aux;
            int i;
            for (int k = 0 ; k < numForceVariables; k++){
                i = posCurrentForceVarVector[k];
                variablesSet[i].isforced = true;
                //add "s "because it is not continuous yet
                if (variablesSet[i].isContinuous == true){
                    
                    for (int m = 0; m < variablesSet[i].casesNumber; m++){
                        aux = "\"s"+variablesSet[i].cases[m]+"\"";
                        variablesSet[i].cases[m] = aux;
                    }
                    for (int j=0 ; j<variablesSet[i].regNumber ; j++) {
                        if (variablesSet[i].registers[j].compareTo("?")!=0){
                            aux = "\"s"+variablesSet[i].registers[j]+"\"";
                            variablesSet[i].registers[j] = aux;
                        }
                    }
                }
                
                
                //at first, until forceCont(i), it is not continuous
                variablesSet[i].isContinuous = false;
            }
            
            
            //Enable components
            
            forceComboBox.setEnabled(false);
            forceSubPanel.setEnabled(false);
            subForceNameText.setText("");
            subForceTitleText.setText("");
            subForceMaxText.setText("");
            subForceMinText.setText("");
            subForceNoCasesText.setText("");
            subForceNoStatesText.setText("");
            
            forceCheckBox.setSelected(false);
            subForceNameText.setEnabled(false);
            subForceNameLabel.setEnabled(false);
            subForceTitleText.setEnabled(false);
            subForceTitleLabel.setEnabled(false);
            subForceMaxText.setEnabled(false);
            subForceMaxLabel.setEnabled(false);
            subForceMinText.setEnabled(false);
            subForceMinLabel.setEnabled(false);
            subForceNoCasesText.setEnabled(false);
            subForceNoCasesLabel.setEnabled(false);
            subForceNoStatesText.setEnabled(false);
            subForceNoStatesLabel.setEnabled(false);
            forceCheckBox.setEnabled(false);
            forceLabel3.setEnabled(false);
            
        }
    }
    
    
    /**
     * No force any variable to be continuous
     *
     * @param e
     */
    
    void forceNothingButton_actionPerformed(ActionEvent e) {
        
        String aux;
        if (forceNothingButton.isSelected()) {
            
            int i;
            for (int k = 0 ; k < numForceVariables; k++){
                i = posCurrentForceVarVector[k];
                variablesSet[i].isforced = false;
                
                
                //add "s "because it is not continuous yet
                if (variablesSet[i].isContinuous == true){
                    for (int m = 0; m < variablesSet[i].casesNumber; m++){
                        aux = "\"s"+variablesSet[i].cases[m]+"\"";
                        variablesSet[i].cases[m] = aux;
                    }
                    for (int j=0 ; j<variablesSet[i].regNumber ; j++) {
                        if (variablesSet[i].registers[j].compareTo("?")!=0){
                            aux = "\"s"+variablesSet[i].registers[j]+"\"";
                            variablesSet[i].registers[j] = aux;
                        }
                    }
                    
                }
                
                //if you have pressed other option before
                variablesSet[i].isContinuous = false;
                
            }
            
            forceComboBox.setEnabled(false);
            forceSubPanel.setEnabled(false);
            subForceNameText.setText("");
            subForceTitleText.setText("");
            subForceMaxText.setText("");
            subForceMinText.setText("");
            subForceNoCasesText.setText("");
            subForceNoStatesText.setText("");
            
            forceCheckBox.setSelected(false);
            subForceNameText.setEnabled(false);
            subForceNameLabel.setEnabled(false);
            subForceTitleText.setEnabled(false);
            subForceTitleLabel.setEnabled(false);
            subForceMaxText.setEnabled(false);
            subForceMaxLabel.setEnabled(false);
            subForceMinText.setEnabled(false);
            subForceMinLabel.setEnabled(false);
            subForceNoCasesText.setEnabled(false);
            subForceNoCasesLabel.setEnabled(false);
            subForceNoStatesText.setEnabled(false);
            subForceNoStatesLabel.setEnabled(false);
            forceCheckBox.setEnabled(false);
            forceLabel3.setEnabled(false);
            
        }
    }
    
    
    /**
     * Studies variables to decide if force or not.
     * Enables forceComboBox.
     *
     * @param e
     */
    
    void forceNormalButton_actionPerformed(ActionEvent e) {
        
        if (forceNormalButton.isSelected()) {
            String aux;
            int i;
            
            for (int j = 0; j < variables; j++)
                variablesSet[j].isforced = false;
            
            //at first, nothing variable is forced
            for (int k = 0 ; k < numForceVariables; k++){
                i = posCurrentForceVarVector[k];
                variablesSet[i].isforced = false;
                forceCheckBox.setSelected(false);
                
                //add "s "because it is not continuous yet
                if (variablesSet[i].isContinuous == true){
                    for (int m = 0; m < variablesSet[i].casesNumber; m++){
                        aux = "\"s"+variablesSet[i].cases[m]+"\"";
                        variablesSet[i].cases[m] = aux;
                    }
                    for (int j=0 ; j<variablesSet[i].regNumber ; j++) {
                        if (variablesSet[i].registers[j].compareTo("?")!=0){
                            aux = "\"s"+variablesSet[i].registers[j]+"\"";
                            variablesSet[i].registers[j] = aux;
                        }
                    }
                }
                
                //if you press MassiveForceButton before
                variablesSet[i].isContinuous = false;
                
            }
            
            forceCheckBox.setSelected(false);
            for (int k = 0; k < variables; k++)
                variablesSet[k].isforced = false;
            
            
            forceComboBox.setEnabled(true);
            
        }
        
    }
    
    
    /**
     * Puts into action the changes in the edited variables
     *
     * @param e ActionEvent
     */
    
    void editProcessButton_actionPerformed(ActionEvent e) {
        
        String nameVar = editNameText.getText();
        
        boolean repeated = false;
        
        if ((nameVar.compareTo("")==0)||(nameVar.compareTo(" ")==0)){
            //Invalid name for the variable
            //shows an error message and doesn't save the changes
            jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Import.InvalidNameVariable"),Elvira.localize(dialogBundle,"Import.Elvira.label"),
            jOPanel.ERROR_MESSAGE);
        }
        else{
            
            nameVar = ImportVariable.checkNumbers(nameVar);
            nameVar = ImportVariable.checkSymbols(nameVar);
            
            //check if this variables exits
            for (int i = 0; (i < variables) && (!repeated); i++){
                if ((variablesSet[i].getName().compareTo(nameVar)==0)&& (i != posCurrentEditVar) ) {
                    repeated = true;
                }
                
            }
            
            if (repeated){
                //Repeated name for the variable
                //shows an error message and doesn't saves the changes
                jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Import.RepeatedNameVariable"),Elvira.localize(dialogBundle,"Import.Elvira.label"),jOPanel.ERROR_MESSAGE);
            }
            else{
                
                String titleVar;
                String commentVar;
                String newValue, oldValue;
                String[] values;
                boolean noProblem = true;
                
                titleVar = editTitleText.getText();
                commentVar = editCommentText.getText();
                
                if ((titleVar.compareTo("")==0)||(titleVar.compareTo(" ")==0)){
                    //shows an error message and doesn't saves the changes
                    jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Import.InvalidTitleVariable"),Elvira.localize(dialogBundle,"Import.Elvira.label"),
                    jOPanel.ERROR_MESSAGE);
                }
                else{
                    saveChanges  = true;
                    
                    //look for in the state vector:
                    for (int t = 0; (t < variablesSet[posCurrentEditVar].casesNumber) && noProblem; t++){
                        //current state = t
                        //if it is null, this state has not been edited
                        if (editStatesVector.elementAt(t)==null){
                        }else{
                            values = (String[]) editStatesVector.elementAt(t);
                            newValue = values[1];
                            
                            repeated = false;
                            //check if this state exits
                            for (int j = 0; (j < variablesSet[posCurrentEditVar].casesNumber) && (!repeated); j++){
                                
                                if ((variablesSet[posCurrentEditVar].cases[j].compareTo(newValue)==0)&& (j != t) ) {
                                    repeated = true;
                                }
                            }
                            
                            if (repeated){
                                //repeated name for the state
                                //shows an error message and doesn't saves the changes
                                jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Import.RepeatedNameState"),Elvira.localize(dialogBundle,"Import.Elvira.label"),jOPanel.ERROR_MESSAGE);
                                noProblem = false;
                            }
                            else{
                                
                                //check if the values is not empty
                                if ((newValue.compareTo("")!=0) && (newValue.compareTo(" ")!=0)){
                                    
                                    //si no hay problema almaceno el nuevo nombre del estado
                                    //y cambio en los registros el valor antiguo
                                    
                                }//end if ese valor de estado es vacio
                                else{
                                    //Invalid name for the state
                                    //shows an error message and doesn't saves the changes
                                    jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle,"Import.InvalidNameState"),Elvira.localize(dialogBundle,"Import.Elvira.label"),
                                    jOPanel.ERROR_MESSAGE);
                                    noProblem = false;
                                }
                                
                            }//end else if repeated
                            
                        }//end if this state has not edited
                        
                    }//end for t
                    
                    
                    if (noProblem){
                        //if there was not any problem, save changes
                        
                        name[posCurrentEditVar] = nameVar;
                        title[posCurrentEditVar] = titleVar;
                        comment[posCurrentEditVar] = commentVar;
                        
                        variablesSet[posCurrentEditVar].setName(nameVar);
                        variablesSet[posCurrentEditVar].comment = commentVar;
                        variablesSet[posCurrentEditVar].setName(name[posCurrentEditVar]);
                        
                        if (noNum[posCurrentEditVar] == true){
                            variablesSet[posCurrentEditVar].isInteger = false;
                        }
                        
                        //for states
                        
                        for (int t = 0; (t < variablesSet[posCurrentEditVar].casesNumber) ; t++){
                            //current state = t
                            //if it is null, this state has not been edited
                            if (editStatesVector.elementAt(t) != null) {
                                
                                values = (String[]) editStatesVector.elementAt(t);
                                oldValue = values[0];
                                newValue = values[1];
                                
                                //check if it was numeric before and now not, because you
                                //have written a not integer string
                                
                                if (variablesSet[posCurrentEditVar].isNumeric == true){
                                    try {
                                        new Double(newValue);
                                    }
                                    catch (NumberFormatException e6){
                                        //los estados eran numericos y ahora hay un string                                        
                                        variablesSet[posCurrentEditVar].isNumeric = false;
                                        variablesSet[posCurrentEditVar].isContinuous = false;                                        
                                    }
                                }
                                    
                                
                                //como puede llevar s y el usuario borrarla la vuelvo a poner
                                
                                try {
                                    new Integer(newValue);
                                    newValue = "s"+newValue;
                                }
                                catch (NumberFormatException e4){
                                    
                                }
                                
                                variablesSet[posCurrentEditVar].cases[t] = newValue;
                                
                                //change the name of the state in all register
                                for (int i = 0; i < variablesSet[posCurrentEditVar].regNumber; i++) {
                                    //replace with new value
                                    if (variablesSet[posCurrentEditVar].registers[i].compareTo(oldValue) == 0)
                                        variablesSet[posCurrentEditVar].registers[i] = newValue;
                                } //end for i
                            } //end if ese estado se ha editado
                        }//end for t
                        
                        updatePanels();
                        
                        for (int i = 0; i < variables; i++){
                            if (i != posCurrentEditVar)
                                editStatesComboBox[i].setVisible(false);
                            else
                                editStatesComboBox[i].setVisible(true);
                        }
                        
                        
                        String information;
                        information = "\n\n     Name:    "+variablesSet[posCurrentEditVar].getName()+
                        "\n     Title:    "+titleVar+
                        "\n     Comment:    "+variablesSet[posCurrentEditVar].comment;
                        
                        if (variablesSet[posCurrentEditVar].isContinuous == false){
                            information = information+"\n     States :";
                            for (int p = 0; p < variablesSet[posCurrentEditVar].casesNumber; p++) {
                                if (p == 0)
                                    information = information+"\n        "+variablesSet[posCurrentEditVar].cases[p]+", ";
                                else{
                                    if (p % 5 == 0)
                                        information = information+"\n        ";
                                    information = information+variablesSet[posCurrentEditVar].cases[p];
                                    if (p != variablesSet[posCurrentEditVar].casesNumber -1)
                                        information = information+", ";
                                }
                            }
                        }
                        
                        information = information+"\n\n";
                        
                        jOPanel.showMessageDialog(this,
                        Elvira.localize(messagesBundle,"Import.SuccessfulEdit.label")+information,Elvira.localize(messagesBundle,
                        "Import.Elvira.label"),jOPanel.PLAIN_MESSAGE);
                        
                    }//end if noProblem
                    
                }//end of else
            }//end of else
        }//end of else
        
    }//end of editProcessButton_action performed
    
    
    /**
     * Analyses the .csv file, taking into account the separator
     * you have choosen
     *
     * @param e
     */
    
    void optionAnalyseButton_actionPerformed(ActionEvent e) {
        
        if (optionRadioButton1a.isSelected())
            separator=";";
        else
            if (optionRadioButton1b.isSelected())
                separator = ",";
        
        //ojooooooooo esto puede fallar pq checksimbols quita las ,
        //no, pq  en un token separado por , nunca habra una , dentro
        //y si las hay es problema del q lo mete
        
        if (csvFileText.getText().compareTo("[none]")!=0){
            //Leo el csv file y almaceno la informacion sobre las variables
            
            boolean error;
            error = this.analyseCsvFile();
            
            //enabled importButton
            
            if (!error){
                
                analizado = true;
                
                //create editVariableCombobox
                
                noNum = new boolean[variables];
                
                for (int i = 0; i < variables; i++)
                    noNum[i] = false;
                
                editVariablesComboBox2.setVisible(false);
                editVariablesComboBox.setEnabled(true);
                
                String[] editVariablesNames = new String[variables];
                for (int i = 0; i < variables; i++) {
                    editVariablesNames[i] = variablesSet[i].variableName;
                }
                
                //create a combobox to store states for each variable
                String[] editStatesNames;
                String state;
                editStatesComboBox = new JComboBox[variables];
                for (int i = 0; i < variables; i++) {
                    
                    //if it is finite-state, it have a set of states
                    if (variablesSet[i].isContinuous == false){
                        
                        editStatesNames = new String[variablesSet[i].casesNumber];
                        for (int j = 0; j < variablesSet[i].casesNumber; j++) {
                            //show the state without " "
                            //show the state without "s "
                            state = variablesSet[i].cases[j];
                            state = state.substring(1, state.length() - 1);
                           	if (variablesSet[i].isNumeric == true)
                           		state = state.substring(1);
                            editStatesNames[j] = state;
                        }
                        
                        editStatesComboBox[i] = new JComboBox(editStatesNames);
                        editStatesComboBox[i].setBounds(new Rectangle(231, 67, 174, 22));
                        editStatesComboBox[i].setVisible(false);
                        
                    }
                    else{
                        editStatesComboBox[i] = new JComboBox();
                        editStatesComboBox[i].setBounds(new Rectangle(231, 67, 174, 22));
                        editStatesComboBox[i].setVisible(false);
                        
                    }
                    
                }
                
                editVariablesComboBox = new JComboBox(editVariablesNames);
                editVariablesComboBox.setBounds(new Rectangle(278, 9, 128, 22));
                editVariablesComboBox.setEnabled(true);
                editVariablesComboBox.addActionListener(new ImportDialog_editVariablesComboBox_actionAdapter(this));
                
                editPanel.add(editVariablesComboBox, null);
                centralPanel.add(editPanel,Elvira.localize(dialogBundle,"Import.EditPanel.label"), 1);
                importPanel.add(centralPanel, BorderLayout.CENTER);
                
                forceCont = new boolean[variables];
                for (int i = 0; i < variables; i++){
                    forceCont[i]=false;
                    variablesSet[i].isforced = false;
                }
                
                title = new String[variables];
                for (int i = 0; i < variables; i++)
                    title[i] = "Variable "+i;
                
                name = new String[variables];
                for (int i = 0; i < variables; i++)
                    name[i] = variablesSet[i].getName() ;
                
                comment = new String[variables];
                for (int i = 0; i < variables; i++)
                    comment[i] = "";
                
                for (int i = 0 ; i < variables; i++){
                    
                    if (variablesSet[i].isContinuous==false){
                        int numberOfStates = variablesSet[i].casesNumber;
                        states = new String[numberOfStates];
                        for (int k = 0; k < numberOfStates; k++){
                            states[k] = variablesSet[i].cases[k];
                        }
                        statesVector.insertElementAt(states, i);
                    }
                    else{
                        statesVector.insertElementAt("null", i);
                    }
                }
                
                //enabled components
                
                forceMassiveButton.setEnabled(true);
                forceNothingButton.setEnabled(true);
                forceNormalButton.setEnabled(true);
                forceProcessButton.setEnabled(true);
                
                //create forcePanel and editPanel, that were empty
                
                forceComboBox2.setVisible(false);
                forceComboBox.setEnabled(true);
                
                int j = 0;
                
                //variables that can be forced
                for (int i = 0; i < variables; i++) {
                    if ( (variablesSet[i].isNumeric == true) &&
                    (variablesSet[i].getContinuous() == false)) {
                        j++;
                    }
                }
                
                numForceVariables = j;
                
                if (numForceVariables == 0){
                    forceComboBox.setVisible(false);
                    forceSubPanel.setVisible(false);
                    forceNormalButton.setVisible(false);
                    forceMassiveButton.setVisible(false);
                    forceNothingButton.setVisible(false);
                    forceProcessButton.setVisible(false);
                    forceLabel1.setVisible(false);
                    forceLabel2.setVisible(false);
                    forceLabel1b.setVisible(true);
                    forceLabel2b.setVisible(true);
                }
                else{
                    forceLabel1b.setVisible(false);
                    forceLabel2b.setVisible(false);
                    posCurrentForceVarVector = new int[numForceVariables];
                    String[] forceVariablesNames = new String[numForceVariables];
                    j = 0;
                    for (int i = 0; i < variables; i++) {
                        if ( (variablesSet[i].isNumeric == true) &&
                        (variablesSet[i].getContinuous() == false)) {
                            //if it is numeric but not continuous, you can force continuity
                            posCurrentForceVarVector[j] = i;
                            forceVariablesNames[j] = variablesSet[i].variableName;
                            j++;
                        }
                    }
                    
                    forceComboBox = new JComboBox(forceVariablesNames);
                    
                    //enabled when you press radio button
                    forceComboBox.setEnabled(false);
                    forceComboBox.setBounds(new Rectangle(218, 70, 115, 21));
                    
                    forceCheckBox.setSelected(false);
                    for (int i = 0; i < variables; i++)
                        variablesSet[i].isforced = false;
                    
                    
                    forceComboBox.addActionListener(new ImportDialog_forceComboBox_actionAdapter(this));
                    forcePanel.add(forceComboBox, null);
                    centralPanel.add(forcePanel,Elvira.localize(dialogBundle,"Import.ForcePanel.label"), 2);
                }
                
                this.repaint();
                
                //show a message: process has been correct
                jOPanel.showMessageDialog(this,
                Elvira.localize(messagesBundle,
                "Import.SuccessfulAnalyse.label"),
                Elvira.localize(messagesBundle,
                "Import.Elvira.label"),
                jOPanel.PLAIN_MESSAGE);
            }
        }
        else
            jOPanel.showMessageDialog(this,Elvira.localize(messagesBundle, "Import.NoCsvFileSelected"),
            Elvira.localize(dialogBundle,"Import.Elvira.label"),jOPanel.ERROR_MESSAGE);
    }//end optionAnalyseButton_actionPerformed
    
    
    
    
    
    
    
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    //////////////        Codigo generado automaticamente. No tocar.   /////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    
    
    class ImportDialog_csvFileButton_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_csvFileButton_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.csvFileButton_actionPerformed(e);
        }
    }
    
    class ImportDialog_importButton_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_importButton_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.importButton_actionPerformed(e);
        }
    }
    
    class ImportDialog_cancelButton_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_cancelButton_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.cancelButton_actionPerformed(e);
        }
    }
    
    class ImportDialog_dbcFileButton_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_dbcFileButton_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.dbcFileButton_actionPerformed(e);
        }
    }
    
    class ImportDialog_optionDBNameText_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_optionDBNameText_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.optionDBNameText_actionPerformed(e);
        }
    }
    
    class ImportDialog_editVariablesComboBox_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_editVariablesComboBox_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.editVariablesComboBox_actionPerformed(e);
        }
    }
    
    class ImportDialog_editNameText_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_editNameText_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.editNameText_actionPerformed(e);
        }
    }
    
    class ImportDialog_editTitleText_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_editTitleText_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.editTitleText_actionPerformed(e);
        }
    }
    
    class ImportDialog_editCommentText_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_editCommentText_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.editCommentText_actionPerformed(e);
        }
    }
    
    class ImportDialog_editStatesComboBox_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_editStatesComboBox_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.editStatesComboBox_actionPerformed(e);
        }
    }
    
    class ImportDialog_editOptionStatesNameText_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_editOptionStatesNameText_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.editOptionStatesNameText_actionPerformed(e);
        }
    }
    
    class ImportDialog_forceCheckBox_actionAdapter
    implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_forceCheckBox_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void actionPerformed(ActionEvent e) {
            adaptee.forceCheckBox_actionPerformed(e);
        }
    }
    
    class ImportDialog_forceCheckBox_itemAdapter
    implements java.awt.event.ItemListener {
        ImportDialog adaptee;
        
        ImportDialog_forceCheckBox_itemAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        
        public void itemStateChanged(ItemEvent e) {
            adaptee.forceCheckBox_itemStateChanged(e);
        }
    }
    
    class ImportDialog_forceComboBox_actionAdapter implements
    java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_forceComboBox_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        public void actionPerformed(ActionEvent e) {
            adaptee.forceComboBox_actionPerformed(e);
        }
    }
    
    class ImportDialog_forceProcessButton_actionAdapter implements
    java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_forceProcessButton_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        public void actionPerformed(ActionEvent e) {
            adaptee.forceProcessButton_actionPerformed(e);
        }
    }
    
    class ImportDialog_forceMassiveButton_actionAdapter implements
    java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_forceMassiveButton_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        public void actionPerformed(ActionEvent e) {
            adaptee.forceMassiveButton_actionPerformed(e);
        }
    }
    
    class ImportDialog_forceNothingButton_actionAdapter implements
    java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_forceNothingButton_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        public void actionPerformed(ActionEvent e) {
            adaptee.forceNothingButton_actionPerformed(e);
        }
    }
    
    class ImportDialog_forceNormalButton_actionAdapter implements
    java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_forceNormalButton_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        public void actionPerformed(ActionEvent e) {
            adaptee.forceNormalButton_actionPerformed(e);
        }
    }
    
    class ImportDialog_editProcessButton_actionAdapter implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_editProcessButton_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        public void actionPerformed(ActionEvent e) {
            adaptee.editProcessButton_actionPerformed(e);
        }
    }
    
    class ImportDialog_optionAnalyseButton_actionAdapter implements java.awt.event.ActionListener {
        ImportDialog adaptee;
        
        ImportDialog_optionAnalyseButton_actionAdapter(ImportDialog adaptee) {
            this.adaptee = adaptee;
        }
        public void actionPerformed(ActionEvent e) {
            adaptee.optionAnalyseButton_actionPerformed(e);
        }
    }
    
    
    
}//end DialogImport class
