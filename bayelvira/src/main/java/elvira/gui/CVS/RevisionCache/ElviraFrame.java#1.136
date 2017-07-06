/* ElviraFrame.java */

package elvira.gui;
import java.awt.*;
import java.io.*;
import java.beans.*;
import java.awt.event.*;

import javax.swing.event.*;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.*;

import elvira.*;
import elvira.gui.explication.*;
import elvira.gui.explication.policytree.ElviraGUIMediatorPT;
import elvira.gui.explication.policytree.PolicyTreeFrame;
import elvira.inference.super_value.CooperPolicyNetwork;
import elvira.parser.*;
import elvira.Elvira;
import elvira.decisionTrees.*;
import elvira.sensitivityAnalysis.*;


/**
 * This is the main class of the Elvira interface. It contains all the menuItems,
 * toolbar buttons and listeners for all these items and buttons. This class also
 * contains the necessary intermediate structures (DesktopPane, currentNetworkFrame)
 * and variables (frameCount) to manage all the internal frames of Elvira
 *
 * @author ..., fjdiez, ratienza, ...
 * @version 1.118
 * @since 20/04/04
 */

public class ElviraFrame extends JFrame implements ClipboardOwner {
    // Used by addNotify
    private boolean frameSizeAdjusted = false;
    
    private ScrollPane elviraScrollPane = new ScrollPane(
    ScrollPane.SCROLLBARS_AS_NEEDED);
    
    /**
     * Manage all the Internal NetworkFrames
     */
    private ElviraDesktopPane desktopPane = new ElviraDesktopPane();
    
    /**
     * For accesing to the frame where the messages are displayed
     */
    private MessageFrame messageWindow;
    
    /**
     * Contains the number of empty NetworkFrames which name began with
     * "Untitled" that have been displayed during the execution of Elvira
     */
    private int untitledCounter = 1;
    
    /**
     * Contains the actual networkFrame
     */
    private NetworkFrame currentNetworkFrame;
    
    
    private boolean maximizeWindows = true;
    
    /**
     * Used to agrupate all CheckboxMenuItems of the windowMenu
     */
    private ButtonGroup windowGroup = new ButtonGroup();
    private ButtonGroup modifyGroup = new ButtonGroup();
    private ButtonGroup viewGroup = new ButtonGroup();
    
    /**
     * Contains the menu strings for the languaje selected
     */
    private ResourceBundle menuBundle;
    
    /**
     * Contains the dialog strings for the languaje selected
     */
    private ResourceBundle dialogBundle;
    
    private ElviraFileChooser fileChooser;
    private JPanel toolbarPanel = new JPanel();
    private JToolBar standardToolbar, editionToolbar,
    explanationToolbar, returnToolbar;
    
    /**
     * Contains the elvira Clipboard
     */
    private Clipboard elviraClipboard = new Clipboard("Elvira");
    
    
    /* Images that will be placed into the toolbar buttons */
    
    private static String imagesPath = "gui/images/";
    
    public ImageIcon newIcon, openIcon, openDBCIcon, saveIcon, cutIcon, copyIcon,
    pasteIcon, undoIcon, redoIcon, zoomInIcon,
    zoomOutIcon, linkIcon, influenceLinkIcon, layoutIcon, chanceNodeIcon, continuousNodeIcon,
    decisionNodeIcon, utilityNodeIcon, constraintsIcon, dectreeIcon, pointerIcon,
    lineIcon, aboutIcon, saveCaseIcon, storeCaseIcon, expandIcon,
    optionsIcon, explanationIcon, firstIcon, previousIcon, nextIcon,
    lastIcon, deleteIcon, emptyCasesIcon, editorIcon, caseIcon, propagateIcon, explainCaseIcon, pathsIcon;
    
    //Introducido por jruiz
    public ImageIcon sensitivityAnalysisIcon;
    //Fin introducido por jruiz

    //Introducido por Alberto Ruiz
    public ImageIcon sensitivityOneAnalysisIcon;
    
    /* buttons of the standard toolbar */
    
    private JButton newButton = new JButton(),
    openButton = new JButton(),
    openDBCButton = new JButton(),
    saveButton = new JButton(),
    cutButton = new JButton(),
    copyButton = new JButton(),
    pasteButton = new JButton(),
    undoButton = new JButton(),
    redoButton = new JButton(),
    zoomInButton = new JButton(),
    zoomOutButton = new JButton(),
    aboutButton = new JButton(),
    functionButton= new JButton(),
    saveCaseButton = new JButton(),
    storeCaseButton = new JButton(),
    expandButton = new JButton(),
    optionsButton = new JButton(),
    firstButton = new JButton(),
    previousButton = new JButton(),
    nextButton = new JButton(),
    lastButton = new JButton(),
    deleteButton = new JButton(),
    emptyCasesButton = new JButton(),
    editorButton = new JButton(),
    caseButton = new JButton(),
    propagateButton = new JButton(),
    explainCaseButton=new JButton(),
    pathsButton=new JButton(),
    influenceLinkButton=new JButton(),
    layoutButton = new JButton(),
    influencesLinkButton=new JButton(),
    dectreeButton=new JButton(),
    constraintsButton=new JButton(),
    returnButton=new JButton();
    
    //Introducido por jruiz
    private JButton sensitivityAnalysisButton = new JButton();
    //Fin introducido por jruiz

    //Introducido por Alberto Ruiz
    private JButton sensitivityOneAnalysisButton= new JButton();
    //Fin introducido por Alberto Ruiz

    private JToggleButton selectButton = new JToggleButton(),
    chanceNodeButton = new JToggleButton(),
    observedNodeButton = new JToggleButton(),
    continuousNodeButton = new JToggleButton(),
    decisionNodeButton = new JToggleButton(),
    utilityNodeButton = new JToggleButton(),
    directedLinkButton = new JToggleButton(),
    undirectedLinkButton = new JToggleButton();
    
    /**
     * Agrupate all the edition buttons and only let one active
     */
    private ButtonGroup editionButtonGroup = new ButtonGroup();
    
    /**
     * Have the working mode of the active JInternalFrame
     */
    private JComboBox workingMode;
    private JComboBox zoomComboBox = new JComboBox();
    private JTextField nodeName = new JTextField(17);
    private JComboBox thresholdComboBox = new JComboBox();
    private JLabel thresholdLabel = new JLabel();
    
    /* Main structures for the menuBar */
    private JPanel menuBarPanel = new JPanel();
    private JMenuBar menuBar;
    
    private JMenu fileMenu, editMenu, inferenceMenu,
    viewMenu, tasksMenu, optionsMenu, windowMenu, helpMenu;
    
    /* Items of the file Menu */
  private JMenuItem newItem, openItem, openDBCItem, importItem, exportItem,
      openURLItem,
    saveItem, saveAsItem, saveAndReopenItem, saveAllItem,
    loadEvidenceItem, saveEvidenceItem,  importBnetXbifItem, exportBnetXbifItem,
    closeItem, exitItem;
    
    /* Items of the edit Menu */
    private JMenuItem undoItem, redoItem, cutItem,
    copyItem, pasteItem, deleteItem,
    selectAllItem, constraintsItem;
    
    private JCheckBoxMenuItem selectItem, chanceItem,continuousItem,
    decisionItem, utilityItem, linkItem;
    
    /* Items of the inference Menu */
    private JMenuItem saveCaseItem, storeCaseItem, expandItem, explainItem,
    optionsItem, firstCaseItem,
    previousCaseItem, nextCaseItem,
    lastCaseItem, caseEditorItem, caseMonitorItem, propagateItem, dectreeItem,
        //Introducido por Alberto Ruiz
    analysisOneParameterItem;
  	//Fin introducido por Alberto Ruiz

    /* Items of the view Menu */
    private JCheckBoxMenuItem byNameItem, byTitleItem;
    private JCheckBoxMenuItem influenceItem;
    private JMenu setPrecisionItem;
  private JCheckBoxMenuItem Prec1Item, Prec2Item, Prec3Item, Prec4Item,
      Prec5Item,
    Prec6Item, Prec7Item, Prec8Item, LastPrecItem;
    
    private JMenuItem fusionItem;
    
    //Introducido por jruiz
    private JMenuItem sensitivityAnalysisItem;
    //Fin introducido por jruiz

    /* Items of the options Menu */
  private JMenuItem propagationMethodItem, generateDBCItem,
      inferenceOptionsItem, explanationOptionsItem;
    
    /* Items of the window Menu */
    private JMenuItem cascadeItem, minimizeAllItem,
    restoreAllItem, previousItem,
    nextItem, showMessageWindowItem;
    
    /* Items of help Menu */
    private JMenuItem aboutItem;
    
    public JMenu emptyMenu = new JMenu();
    
    /* Status Area */
    static private JPanel statusArea = new JPanel(new GridLayout(1, 1));
    static private JLabel status = new JLabel("");
    
    private Font ROMAN = new Font("TimesRoman", Font.BOLD, 12);
    
    /* Used by returnToolbar */
    
    EditVariableDialog evd;
    
    /**
     * This class will modify Elvira's UI: toolbars and actions for operating decision trees
     * 
     * Added by Jorge-PFC el 04/12/2005
     */
    private ElviraGUIMediator elviraGUIMediator;

    
    /**
     * This class will modify Elvira's UI: toolbars and actions for operating with policy trees
     * 
     * Added by Manuel LuqueJorge-PFC el 04/12/2005
     */
    private ElviraGUIMediatorPT elviraGUIMediatorPT;
    
    
    
    /* CONSTRUCTORS */
    
    
    /**
     * Main Constructor. Creates a new frame, inicializing all the menus and
     * toolbar items. Furthermore, it creates the desktopPane that allow manage
     * all the Internal NetworkFrames.
     */
    
    public ElviraFrame() {
        switch (Elvira.getLanguaje()) {
      case Elvira.AMERICAN:
        menuBundle = ResourceBundle.getBundle("elvira/localize/Menus");
            dialogBundle = ResourceBundle.getBundle("elvira/localize/Dialogs");
            break;
      case Elvira.SPANISH:
        menuBundle = ResourceBundle.getBundle("elvira/localize/Menus_sp");
            dialogBundle = ResourceBundle.getBundle("elvira/localize/Dialogs_sp");
            break;
        }
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Frame"));
        File fpath=new File(".");
        fileChooser = new ElviraFileChooser(fpath.getAbsolutePath());
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Frame"));
        explanationToolbar = new JToolBar();
        editionToolbar = new JToolBar();
        standardToolbar = new JToolBar();
        returnToolbar = new JToolBar();
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Images"));
        
        newIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"new.gif"));
        openIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"open.gif"));
        openDBCIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"open2.gif"));
        saveIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"save.gif"));
        cutIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"cut.gif"));
        copyIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"copy.gif"));
        pasteIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"paste.gif"));
        undoIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"undo.gif"));
        redoIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"redo.gif"));
        zoomInIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"zoomin.gif"));
        zoomOutIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"zoomout.gif"));
        linkIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"arrow.gif"));
        chanceNodeIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"oval.jpg"));
        continuousNodeIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"ovalC.jpg"));
        
        decisionNodeIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"square2.gif"));
        influenceLinkIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"influence.gif"));
        layoutIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"layout.gif"));
        dectreeIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"dectree.gif"));
        constraintsIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"constraints.gif"));
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Images"));
        
        utilityNodeIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"hexagon2.gif"));
        pointerIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"pointer.gif"));
        lineIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"line.gif"));
        aboutIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"about.gif"));
        storeCaseIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"saveCase.gif"));
        saveCaseIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"store.gif"));
        expandIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"expand.gif"));
        optionsIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"options.gif"));
        explanationIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"explain(2).gif"));
        firstIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"first.gif"));
        previousIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"previous.gif"));
        nextIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"next.gif"));
        lastIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"last.gif"));
        deleteIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"deleteCurrentEv.gif"));
        emptyCasesIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"emptyCasesList.gif"));
        editorIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"edit.gif"));
        caseIcon = new ImageIcon(Elvira.class.getResource(imagesPath+"case.gif"));
        propagateIcon=new ImageIcon(Elvira.class.getResource(imagesPath+"propagate.gif"));
        explainCaseIcon=new ImageIcon(Elvira.class.getResource(imagesPath+"explain.gif"));
        pathsIcon=new ImageIcon(Elvira.class.getResource(imagesPath+"caminos.gif"));
        
        //Introducido por jruiz
        sensitivityAnalysisIcon = new ImageIcon(Elvira.class.getResource(imagesPath +
          "sensitivityAnalysisIco.gif"));
        //Fin introducido por jruiz

        //Introducido por Alberto Ruiz
        sensitivityOneAnalysisIcon = new ImageIcon(Elvira.class.getResource(imagesPath +"sensitivity.gif"));
        //Fin introducido por Alberto Ruiz

        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Panes"));
        
        JPanel contentPane = new JPanel(new BorderLayout());
        //elviraScrollPane.add(desktopPane);
        contentPane.add(desktopPane, BorderLayout.CENTER);
        contentPane.add(statusArea, BorderLayout.SOUTH);
        
        setContentPane(contentPane);
        desktopPane.setAutoscrolls(true);
        
                /*Container contentPane = getContentPane();
                contentPane.add(desktopPane, BorderLayout.CENTER);
                contentPane.add(statusArea, BorderLayout.SOUTH);*/
        
        
        /* Set general characteristics of the Elvira frame */
        setTitle("Elvira");
        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screen = tk.getScreenSize();
        setSize((screen.width*3)/4,(screen.height*3)/4);
        
        setVisible(false);
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        /* Setting the status bar */
        statusArea.setBorder(BorderFactory.createEtchedBorder());
        statusArea.add(status);
        status.setHorizontalAlignment(JLabel.LEFT);
        
        showStatus("For Help, press F1");
        
        
                /* ****************************
                   SETTING THE STANDARD TOOLBAR
                 **************************** */
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Toolbars"));
        
        toolbarPanel.setLayout(new BorderLayout(0,0));
        getContentPane().add("North", toolbarPanel);
        standardToolbar.setAlignmentY(0.222222F);
        toolbarPanel.add(standardToolbar,BorderLayout.NORTH);
        standardToolbar.setBounds(0,0,199,29);
        standardToolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        standardToolbar.setFloatable(false);
        
        /* Setting the Buttons into the standard toolbar */
        insertButton(newButton, newIcon, standardToolbar, "File.New");
        insertButton(openButton, openIcon, standardToolbar, "File.Open");
        insertButton(openDBCButton, openDBCIcon, standardToolbar, "File.OpenDBC");
        insertButton(saveButton, saveIcon, standardToolbar, "File.Save");
        
        standardToolbar.addSeparator();
        insertButton(zoomInButton, zoomInIcon, standardToolbar, "Edit.ZoomIn");
        
        String zoomChoices[] = {"500%", "300%", "200%", "170%",
        "100%", "75%", "50%", "25%", "10%" };
        zoomComboBox = new JComboBox(zoomChoices);
        zoomComboBox.setEditable(true);
        standardToolbar.add(zoomComboBox);
        zoomComboBox.setPreferredSize(new Dimension(70,20));
        zoomComboBox.setMaximumSize(zoomComboBox.getPreferredSize());
        zoomComboBox.setSelectedIndex(4);
        
        insertButton(zoomOutButton, zoomOutIcon, standardToolbar, "Edit.ZoomOut");
        standardToolbar.addSeparator();
        insertButton(aboutButton, aboutIcon, standardToolbar, "Help.About");
        standardToolbar.addSeparator();
        
        workingMode = new JComboBox();
        workingMode.addItem(localize(menuBundle,"WorkingMode.Edit.label"));
        workingMode.addItem(localize(menuBundle,"WorkingMode.Inference.label"));
        standardToolbar.add(workingMode);
        workingMode.setMaximumSize(workingMode.getPreferredSize());
        
        
                /* *******************************
                   SETTING THE EDITION TOOLBAR
                 ******************************* */
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Toolbars"));
        
        editionToolbar.setAlignmentY(0.222222F);
        toolbarPanel.add(editionToolbar,BorderLayout.CENTER);
        editionToolbar.setBounds(0,0,250,29);
        editionToolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        editionToolbar.setFloatable(false);
        
        /* Setting the Buttons into the edition toolbar */
        
        insertButton(cutButton, cutIcon, editionToolbar, "Edit.Cut");
        insertButton(copyButton, copyIcon, editionToolbar, "Edit.Copy");
        insertButton(pasteButton, pasteIcon, editionToolbar, "Edit.Paste");
        
        editionToolbar.addSeparator();
        
        insertButton(undoButton, undoIcon, editionToolbar, "Edit.Undo");
        insertButton(redoButton, redoIcon, editionToolbar, "Edit.Redo");
        
        editionToolbar.addSeparator();
        
        insertButton(selectButton, pointerIcon, editionToolbar, "Edit.Select");
        editionButtonGroup.add(selectButton);
        selectButton.setMargin(new Insets(1,1,1,1));
        
        insertButton(chanceNodeButton, chanceNodeIcon, editionToolbar, "Edit.Chance");
        editionButtonGroup.add(chanceNodeButton);
        chanceNodeButton.setMargin(new Insets(1,1,1,1));
        
          
        insertButton(continuousNodeButton, continuousNodeIcon, editionToolbar, "Edit.Continuous");
        editionButtonGroup.add(continuousNodeButton);
        continuousNodeButton.setMargin(new Insets(1,1,1,1));
        
        insertButton(decisionNodeButton, decisionNodeIcon, editionToolbar, "Edit.Decision");
        editionButtonGroup.add(decisionNodeButton);
        decisionNodeButton.setMargin(new Insets(1,1,1,1));
        
        insertButton(utilityNodeButton, utilityNodeIcon, editionToolbar, "Edit.Utility");
        editionButtonGroup.add(utilityNodeButton);
        utilityNodeButton.setMargin(new Insets(1,1,1,1));
        
        insertButton(directedLinkButton, linkIcon, editionToolbar, "Edit.Link");
        editionButtonGroup.add(directedLinkButton);
        directedLinkButton.setMargin(new Insets(1,1,1,1));
        
                /*insertButton (undirectedLinkButton, lineIcon, editionToolbar, "Edit.unLink");
                editionButtonGroup.add(undirectedLinkButton);
                undirectedLinkButton.setMargin(new Insets(1,1,1,1));*/
        
        insertButton(constraintsButton, constraintsIcon, editionToolbar, "Edit.Constraints");
        editionButtonGroup.add(constraintsButton);
        constraintsButton.setMargin(new Insets(1,1,1,1));
        
        insertButton(influenceLinkButton, influenceLinkIcon, editionToolbar, "Edit.influenceLink");
        editionButtonGroup.add(influenceLinkButton);
        influenceLinkButton.setMargin(new Insets(1,1,1,1));
        
        insertButton(layoutButton, layoutIcon, editionToolbar, "Edit.layout");
        editionButtonGroup.add(layoutButton);
        layoutButton.setMargin(new Insets(1,1,1,1));
        
        editionToolbar.addSeparator();
        
        
                /* *******************************
                   SETTING THE EXPLANATION TOOLBAR
                 ******************************* */
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Toolbars"));
        
        explanationToolbar.setAlignmentY(0.222222F);
        explanationToolbar.setBounds(0,0,199,29);
        explanationToolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        explanationToolbar.setFloatable(false);
        explanationToolbar.setVisible(false);
        toolbarPanel.add(explanationToolbar,BorderLayout.SOUTH);
        
        // Set buttons into de explanation Toolbar
        thresholdLabel.setText(localize(menuBundle,"Explanation.Threshold.label"));
        functionButton.setText(localize(menuBundle,"Explanation.Purpose.label"));
        explanationToolbar.add(thresholdLabel);
        thresholdComboBox = new JComboBox();
        
        explanationToolbar.add(thresholdComboBox);
        for (int i=10; i>=0; i--) {
            thresholdComboBox.addItem(String.valueOf(i)+".00");
        }
        thresholdComboBox.setSelectedItem("7.00");
        thresholdComboBox.setMaximumSize(new Dimension(70,20));//thresholdComboBox.getPreferredSize());
        thresholdComboBox.setEditable(false);
        explanationToolbar.addSeparator();
        
        functionButton.setMaximumSize(functionButton.getPreferredSize());
        explanationToolbar.add(functionButton);
        explanationToolbar.addSeparator();
        
        insertButton(saveCaseButton, saveCaseIcon,
        explanationToolbar, "Explanation.Save");
        
        insertButton(storeCaseButton, storeCaseIcon,
        explanationToolbar, "Explanation.Store");
        insertButton(expandButton, expandIcon,
        explanationToolbar, "Explanation.Expand");
        insertButton(optionsButton, optionsIcon,
        explanationToolbar, "Explanation.Options");
        
        explanationToolbar.addSeparator();
        
        insertButton(firstButton, firstIcon,
        explanationToolbar, "Explanation.First");
        insertButton(previousButton, previousIcon,
        explanationToolbar, "Explanation.Previous");
        
        explanationToolbar.add(nodeName);
        nodeName.setMaximumSize(nodeName.getPreferredSize());
        nodeName.setHorizontalAlignment(JTextField.CENTER);
        
        setNodeName("Prior probabilities");
        setColorNodeName(VisualExplanationFStates.green);
        
        insertButton(nextButton, nextIcon,
        explanationToolbar, "Explanation.Next");
        insertButton(lastButton, lastIcon,
        explanationToolbar, "Explanation.Last");
        
        explanationToolbar.addSeparator();
        
        insertButton(deleteButton, deleteIcon,
        explanationToolbar, "Explanation.Delete");
        
        insertButton(emptyCasesButton, emptyCasesIcon,
        explanationToolbar, "Explanation.EmptyCases");
        
        insertButton(caseButton, caseIcon,
        explanationToolbar, "Explanation.Case");
        
        
        insertButton(editorButton, editorIcon,
        explanationToolbar, "Explanation.Editor");
        insertButton(caseButton, caseIcon,
        explanationToolbar, "Explanation.Case");
        
        explanationToolbar.addSeparator();
        
        //Introducido por Alberto Ruiz
        insertButton(sensitivityOneAnalysisButton, sensitivityOneAnalysisIcon,
        		explanationToolbar, "Analysis.SensitivityOneWay");
        sensitivityOneAnalysisButton.setMargin(new Insets(1,1,1,1));
        //Introducido por Alberto Ruiz

        explanationToolbar.addSeparator();
        
        
        insertButton(propagateButton, propagateIcon,
        explanationToolbar, "Explanation.Propagate");
        
        explanationToolbar.addSeparator();
        
        insertButton(influencesLinkButton, influenceLinkIcon, explanationToolbar, "Edit.influenceLink");
        editionButtonGroup.add(influencesLinkButton);
        //		influenceLinkButton.setMargin(new Insets(1,1,1,1));
        
        explanationToolbar.addSeparator();
    /*insertButton(dectreeButton, dectreeIcon, explanationToolbar, "Explanation.Dectree");
     */
        
        insertButton(explainCaseButton, explainCaseIcon,
        explanationToolbar, "Explanation.Explain");
        
        insertButton(pathsButton, pathsIcon,
        explanationToolbar, "Explanation.Paths");
        
        /* Set the general characteristics for the menuBar */
        menuBarPanel.setLayout(null);
        getContentPane().add("South", menuBarPanel);
        menuBarPanel.setBounds(0,29,488,280);
        
        
        /* Setting the File Menu with all its options */
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Menus"));
        
        fileMenu = insertMenu("File");
        
        newItem = insertMenuItem(newIcon, fileMenu, "File.New", "New");
        newItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, Event.CTRL_MASK));
        
        openItem = insertMenuItem(openIcon, fileMenu, "File.Open", "Open...");
        openItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, Event.CTRL_MASK));
        
        openDBCItem = insertMenuItem(openDBCIcon, fileMenu, "File.OpenDBC", "Open a dbc file");
        openDBCItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK));
        
        importItem = insertMenuItem(null, fileMenu, "File.Import", "Import");
        importItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, Event.CTRL_MASK));
        
        exportItem = insertMenuItem(null, fileMenu, "File.Export", "Export");
        exportItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, Event.CTRL_MASK));
        
        fileMenu.add(new JSeparator());
        
        importBnetXbifItem = insertMenuItem(null, fileMenu, "File.ImportBnetXbif", "Import network from XBIF");
        
        exportBnetXbifItem = insertMenuItem(null, fileMenu, "File.ExportBnetXbif", "Export network to XBIF");
       
        fileMenu.add(new JSeparator());
        
        
        openURLItem = insertMenuItem(null, fileMenu, "File.OpenURL", "Open URL...");
        
        fileMenu.add(new JSeparator());
        
        saveItem = insertMenuItem(saveIcon, fileMenu, "File.Save", "Save");
        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, Event.CTRL_MASK));
        
        saveAsItem = insertMenuItem(null, fileMenu, "File.SaveAs", "Save As...");
        saveAllItem = insertMenuItem(null, fileMenu, "File.SaveAll", "Save all");
        saveAndReopenItem = insertMenuItem(null, fileMenu, "File.SaveAndReopen", "Save and Reopen");
        
        fileMenu.add(new JSeparator());
        
        loadEvidenceItem = insertMenuItem(null, fileMenu, "File.LoadEvidence", "Load evidence");
        saveEvidenceItem = insertMenuItem(null, fileMenu, "File.SaveEvidence", "Save evidence");
        
        fileMenu.add(new JSeparator());
        
        
        closeItem = insertMenuItem(null, fileMenu, "File.Close", "Close");
        closeItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, Event.CTRL_MASK));
        
        exitItem = insertMenuItem(null, fileMenu, "File.Exit", "Exit");
        
        
        /* Setting the Edit Menu with all its options */
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Menus"));
        
        editMenu = insertMenu("Edit");
        
        undoItem = insertMenuItem(undoIcon, editMenu, "Edit.Undo", "Undo");
        undoItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, Event.CTRL_MASK));
        
        redoItem = insertMenuItem(redoIcon, editMenu, "Edit.Redo", "Redo");
        redoItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, Event.CTRL_MASK));
        
        editMenu.add(new JSeparator());
        
        cutItem = insertMenuItem(cutIcon, editMenu, "Edit.Cut", "Cut");
        cutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
        java.awt.event.KeyEvent.VK_X, Event.CTRL_MASK));
        
        copyItem = insertMenuItem(copyIcon, editMenu, "Edit.Copy", "Copy");
        copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
        java.awt.event.KeyEvent.VK_C, Event.CTRL_MASK));
        
        pasteItem = insertMenuItem(pasteIcon, editMenu, "Edit.Paste", "Paste");
        pasteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
        java.awt.event.KeyEvent.VK_V, Event.CTRL_MASK));
        
        
        deleteItem = insertMenuItem(null, editMenu, "Edit.Delete", "Delete");
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE,0));
        
        editMenu.add(new JSeparator());
        
        selectAllItem = insertMenuItem(null, editMenu, "Edit.SelectAll", "Select All");
        selectAllItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, Event.CTRL_MASK));
        
        editMenu.add(new JSeparator());
        
        selectItem = insertCheckBoxMenuItem(pointerIcon, editMenu,
        "Edit.Select", "Selection Tool");
        modifyGroup.add(selectItem);
        
        chanceItem = insertCheckBoxMenuItem(chanceNodeIcon, editMenu,
        "Edit.Select", "Add Chance Node");
        chanceItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, Event.SHIFT_MASK));
        modifyGroup.add(chanceItem);
        
        
        continuousItem = insertCheckBoxMenuItem(chanceNodeIcon, editMenu,
        "Edit.Select", "Add Continous Node");
        continuousItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, Event.SHIFT_MASK));
        modifyGroup.add(continuousItem);
        
        
        decisionItem = insertCheckBoxMenuItem(decisionNodeIcon, editMenu,
        "Edit.Decision", "Add Decision Node");
        decisionItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, Event.SHIFT_MASK));
        modifyGroup.add(decisionItem);
        
        utilityItem = insertCheckBoxMenuItem(utilityNodeIcon, editMenu,
        "Edit.Utility", "Add Utility Node");
        utilityItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, Event.SHIFT_MASK));
        modifyGroup.add(utilityItem);
        
        linkItem = insertCheckBoxMenuItem(linkIcon, editMenu,
        "Edit.Link", "Add Link");
        linkItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, Event.SHIFT_MASK));
        modifyGroup.add(linkItem);
        
        editMenu.add(new JSeparator());
        
        constraintsItem = insertMenuItem(constraintsIcon, editMenu, "Edit.Constraints", "Constraints");
        
     /* ************************************
      * SETTING THE RETURN TOOLBAR
      ************************************ */
        
        returnToolbar.setAlignmentY(0.222222F);
        returnToolbar.setBounds(0,0,199,29);
        returnToolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        returnToolbar.setFloatable(false);
        returnToolbar.setVisible(false);
        
        toolbarPanel.add(returnToolbar,BorderLayout.WEST);
        
        /* Setting the Buttons into the return toolbar */
        //insertButton (newButton, newIcon, standardToolbar, "File.New");
        returnButton.setText(localize(dialogBundle,"EditVariable.returnButton.label"));
        returnToolbar.add(returnButton);
        //returnButton.setMargin(new Insets (1,1,1,1));
        //returnButton.setAlignmentY(0.5f);
        //returnButton.setAlignmentX(0.5f);
        returnButton.setLocation(300,0);
        
        /* Setting the Inference Menu with all its options */
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Menus"));
        
        inferenceMenu = new JMenu();
        inferenceMenu.setText(localize(menuBundle,"Explanation.label"));
        inferenceMenu.setActionCommand("Inference");
        inferenceMenu.setMnemonic((int) localize(menuBundle,"Explanation.mnemonic").charAt(0));
        
        saveCaseItem = insertMenuItem(saveCaseIcon, inferenceMenu,
        "Explanation.Save", "Save");
        storeCaseItem = insertMenuItem(storeCaseIcon, inferenceMenu,
        "Explanation.Store", "Store");
        expandItem = insertMenuItem(expandIcon, inferenceMenu,
        "Explanation.Expand", "Expand");
        explainItem = insertMenuItem(null, inferenceMenu,
        "Explanation.Explain", "Explain");
        
        inferenceMenu.add(new JSeparator());
        
        optionsItem = insertMenuItem(optionsIcon, inferenceMenu,
        "Explanation.Options", "Options");
        
        inferenceMenu.add(new JSeparator());
        
        firstCaseItem = insertMenuItem(firstIcon, inferenceMenu,
        "Explanation.First", "First");
        previousCaseItem = insertMenuItem(previousIcon, inferenceMenu,
        "Explanation.Previous", "Previous");
        nextCaseItem = insertMenuItem(nextIcon, inferenceMenu,
        "Explanation.Next", "Next");
        lastCaseItem = insertMenuItem(lastIcon, inferenceMenu,
        "Explanation.Last", "Last");
        
        inferenceMenu.add(new JSeparator());
        
        caseEditorItem = insertMenuItem(editorIcon, inferenceMenu,
        "Explanation.Editor", "Case Editor");
        caseMonitorItem = insertMenuItem(caseIcon, inferenceMenu,
        "Explanation.Case", "Case Monitor");
        
        inferenceMenu.add(new JSeparator());
        
        propagateItem = insertMenuItem(propagateIcon, inferenceMenu,
        "Explanation.Propagate", "Propagate");
        
    /*inferenceMenu.add(new JSeparator());
     
    dectreeItem = insertMenuItem (dectreeIcon, inferenceMenu,
                    "Explanation.Dectree", "Dectree");
     */
        
        /* Setting the View Menu with all its options */
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Menus"));
        
        viewMenu = insertMenu("View");
        
        byNameItem = insertCheckBoxMenuItem(null, viewMenu,
        "View.ByName", "By Name");
        viewGroup.add(byNameItem);
        
        byTitleItem = insertCheckBoxMenuItem(null, viewMenu,
        "View.ByTitle", "By Title");
        byTitleItem.setSelected(true);
        viewGroup.add(byTitleItem);
        
        viewMenu.add(new JSeparator());
        
        influenceItem = insertCheckBoxMenuItem(influenceLinkIcon, viewMenu,
        "Edit.influenceLink", "Mostrar influencias");
        
        influenceItem.setSelected(false);
        
        viewMenu.add(new JSeparator());
        
        setPrecisionItem = insertMenu(null, viewMenu,
        "View.setPrecision", "Set Precision");
        Prec1Item = insertCheckBoxMenuItem(null, setPrecisionItem,
        "View.Prec1Item", "Precision to 1");
        Prec2Item = insertCheckBoxMenuItem(null, setPrecisionItem,
        "View.Prec2Item", "Precision to 2");
        Prec3Item = insertCheckBoxMenuItem(null, setPrecisionItem,
        "View.Prec3Item", "Precision to 3");
        Prec4Item = insertCheckBoxMenuItem(null, setPrecisionItem,
        "View.Prec4Item", "Precision to 4");
        Prec5Item = insertCheckBoxMenuItem(null, setPrecisionItem,
        "View.Prec5Item", "Precision to 5");
        Prec6Item = insertCheckBoxMenuItem(null, setPrecisionItem,
        "View.Prec6Item", "Precision to 6");
        Prec7Item = insertCheckBoxMenuItem(null, setPrecisionItem,
        "View.Prec7Item", "Precision to 7");
        Prec8Item = insertCheckBoxMenuItem(null, setPrecisionItem,
        "View.Prec8Item", "Precision to 8");
        
        /* Only enabled in inference mode */
        setPrecisionItem.setEnabled(false);
        Prec2Item.setSelected(true);
        LastPrecItem = Prec2Item;
        
        /* Setting the Tasks Menu */
        
        tasksMenu = insertMenu("Tasks");
        
        fusionItem = insertMenuItem(null, tasksMenu,
        "Tasks.Fusion", "Fusion");
        
        //Introducido por jruiz
        sensitivityAnalysisItem = insertMenuItem(null, tasksMenu,
                                             "Analysis.SensitivityAnalysis",
                                             "Sensitivity Analysis");
        //Fin introducido por jruiz

                 tasksMenu.add(new JSeparator());
         
        analysisOneParameterItem = insertMenuItem(sensitivityOneAnalysisIcon, tasksMenu,
                                   "Analysis.SensitivityOneWay",
                                   "Sensitivity Analysis One Way");
        //Only enabled in inference mode
        analysisOneParameterItem.setEnabled(false);
        //Fin introducido por Alberto Ruiz

        /* Setting the Options Menu */
        
        optionsMenu = insertMenu("Options");
        
        propagationMethodItem = insertMenuItem(null, optionsMenu,
        "Options.PropagationMethod", "Propagation Method");
        
        generateDBCItem = insertMenuItem(null, optionsMenu,
        "Options.GenerateDBC", "Generate DBC");
        
        inferenceOptionsItem = insertMenuItem(optionsIcon, optionsMenu,
        "Options.InferenceOptions", "Inference Options");
        
        explanationOptionsItem = insertMenuItem(explanationIcon, optionsMenu,
        "Options.ExplanationOptions", "Explanation Options");
        
        
        /* Setting the Window Menu with all its options */
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Menus"));
        
        windowMenu = insertMenu("Window");
        
        cascadeItem = insertMenuItem(null, windowMenu, "Window.Cascade", "Cascade");
        minimizeAllItem = insertMenuItem(null, windowMenu,
        "Window.MinimizeAll", "Minimize All");
        restoreAllItem = insertMenuItem(null, windowMenu,
        "Window.RestoreAll", "Restore All");
        
        windowMenu.add(new JSeparator());
        
        previousItem = insertMenuItem(null, windowMenu, "Window.Previous", "Previous");
        previousItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, Event.CTRL_MASK));
        
        nextItem = insertMenuItem(null, windowMenu, "Window.Next", "Next");
        nextItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, Event.CTRL_MASK));
        
        showMessageWindowItem = insertMenuItem(null, windowMenu,
        "Window.Show", "Show Message Window");
        
        
        /* Setting the Help Menu with all its actions */
        
        helpMenu = insertMenu("Help");
        
        aboutItem = insertMenuItem(aboutIcon, helpMenu, "Help", "About...");
        
        emptyMenu.setVisible(false);
        menuBar.add(emptyMenu);
        
                /* The message window it is the first internal Frame created into the
                   ElviraFrame object, so it must be added to the desktopPane */
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.MessageWindow"));
        
        desktopPane.add(messageWindow = new MessageFrame());
        messageWindow.setVisible(true);
        
        /* After this, it must be set as the active window */
        try {
            messageWindow.setSelected(true);
        } catch (PropertyVetoException e) { System.out.println("You can't select that frame"); }
        
                /* Actions to add the message window to the list (that in this moment
                   is empty) in the Window Menu that let us access quickly to a
                   Internal NetworkFrame */
        if (desktopPane.getAllFrames().length==1)
            windowMenu.add(new JSeparator());
        
        JCheckBoxMenuItem newMenuItem = new JCheckBoxMenuItem(localize(menuBundle,"MessageWindow.label"),true);
        newMenuItem.addItemListener(new WindowMenuItemListener());
        windowMenu.add(newMenuItem);
        windowGroup.add(newMenuItem);
        
        /* Set in the File Menu the name of the last files referenced */
        loadLastReferences(this);
        
                /* Enable only the options that can be executed with an ElviraFrame
                   without NetworkFrames opened (except the Message Window) */
        enableMenusOpenNetworks(false, null);
        enableUndo(false);
        enableRedo(false);
        enablePaste(false);
        enableCutCopy(false);
        
        SwingUtilities.updateComponentTreeUI(getContentPane());
        
        /* LISTENERS DEFINITION */
        /* Listener for the ElviraFrame closing action */
        ElviraWindowAdapter elviraWindowAdapter = new ElviraWindowAdapter();
        this.addWindowListener(elviraWindowAdapter);
        
        Elvira.incrementProgressBar(localize(dialogBundle,"ProgressDialog.Listeners"));
        
        /* Listener for the ElviraFrame menu actions */
        ElviraAction elviraAction = new ElviraAction();
        openItem.addActionListener(elviraAction);
        openDBCItem.addActionListener(elviraAction);
        importItem.addActionListener(elviraAction);
        exportItem.addActionListener(elviraAction);
        saveItem.addActionListener(elviraAction);
        loadEvidenceItem.addActionListener(elviraAction);
        saveEvidenceItem.addActionListener(elviraAction);
        importBnetXbifItem.addActionListener(elviraAction);
        exportBnetXbifItem.addActionListener(elviraAction);
        exitItem.addActionListener(elviraAction);
        cascadeItem.addActionListener(elviraAction);
        newItem.addActionListener(elviraAction);
        minimizeAllItem.addActionListener(elviraAction);
        restoreAllItem.addActionListener(elviraAction);
        openURLItem.addActionListener(elviraAction);
        previousItem.addActionListener(elviraAction);
        nextItem.addActionListener(elviraAction);
        showMessageWindowItem.addActionListener(elviraAction);
        saveAsItem.addActionListener(elviraAction);
        saveAndReopenItem.addActionListener(elviraAction);
        closeItem.addActionListener(elviraAction);
        
        undoItem.addActionListener(elviraAction);
        redoItem.addActionListener(elviraAction);
        cutItem.addActionListener(elviraAction);
        pasteItem.addActionListener(elviraAction);
        copyItem.addActionListener(elviraAction);
        deleteItem.addActionListener(elviraAction);
        selectAllItem.addActionListener(elviraAction);
        
        expandItem.addActionListener(elviraAction);
        explainItem.addActionListener(elviraAction);
        saveCaseItem.addActionListener(elviraAction);
        storeCaseItem.addActionListener(elviraAction);
        firstCaseItem.addActionListener(elviraAction);
        nextCaseItem.addActionListener(elviraAction);
        previousCaseItem.addActionListener(elviraAction);
        lastCaseItem.addActionListener(elviraAction);
        optionsItem.addActionListener(elviraAction);
        
        
        caseEditorItem.addActionListener(elviraAction);
        caseMonitorItem.addActionListener(elviraAction);
        propagateItem.addActionListener(elviraAction);
        /*dectreeItem.addActionListener (elviraAction);*/
        
        byNameItem.addActionListener(elviraAction);
        byTitleItem.addActionListener(elviraAction);
        influenceItem.addActionListener(elviraAction);
        setPrecisionItem.addActionListener(elviraAction);
        Prec1Item.addActionListener(elviraAction);
        Prec2Item.addActionListener(elviraAction);
        Prec3Item.addActionListener(elviraAction);
        Prec4Item.addActionListener(elviraAction);
        Prec5Item.addActionListener(elviraAction);
        Prec6Item.addActionListener(elviraAction);
        Prec7Item.addActionListener(elviraAction);
        Prec8Item.addActionListener(elviraAction);
        
        fusionItem.addActionListener(elviraAction);
        propagationMethodItem.addActionListener(elviraAction);
        
        //Introducido por jruiz
        sensitivityAnalysisItem.addActionListener(elviraAction);
        //Fin introducido por jruiz

        //Introducido por Alberto Ruiz
        analysisOneParameterItem.addActionListener(elviraAction);
        //Fin Introducido por Alberto Ruiz
        
        inferenceOptionsItem.addActionListener(elviraAction);
        generateDBCItem.addActionListener(elviraAction);
        
        explanationOptionsItem.addActionListener(elviraAction);
        
        selectItem.addActionListener(elviraAction);
        chanceItem.addActionListener(elviraAction);
        continuousItem.addActionListener(elviraAction);
        decisionItem.addActionListener(elviraAction);
        utilityItem.addActionListener(elviraAction);
        constraintsItem.addActionListener(elviraAction);
        linkItem.addActionListener(elviraAction);
        
        aboutItem.addActionListener(elviraAction);
        
        /* Listener for the ElviraFrame standard Toolbar Items */
        openButton.addActionListener(elviraAction);
        openDBCButton.addActionListener(elviraAction);
        saveButton.addActionListener(elviraAction);
        newButton.addActionListener(elviraAction);
        zoomInButton.addActionListener(elviraAction);
        zoomOutButton.addActionListener(elviraAction);
        
        aboutButton.addActionListener(elviraAction);
        
        WokingModeItem workingModeAction = new WokingModeItem();
        workingMode.addActionListener(workingModeAction);
        
        ZoomComboBoxItem zoomAction = new ZoomComboBoxItem();
        zoomComboBox.addActionListener(zoomAction);
        
        /* Listeners for the edition Toolbar items */
        
        undoButton.addActionListener(elviraAction);
        redoButton.addActionListener(elviraAction);
        cutButton.addActionListener(elviraAction);
        pasteButton.addActionListener(elviraAction);
        copyButton.addActionListener(elviraAction);
        
        selectButton.addActionListener(elviraAction);
        chanceNodeButton.addActionListener(elviraAction);
        observedNodeButton.addActionListener(elviraAction);
        continuousNodeButton.addActionListener(elviraAction);
        decisionNodeButton.addActionListener(elviraAction);
        utilityNodeButton.addActionListener(elviraAction);
        directedLinkButton.addActionListener(elviraAction);
        constraintsButton.addActionListener(elviraAction);
        influenceLinkButton.addActionListener(elviraAction);
        layoutButton.addActionListener(elviraAction);
        influencesLinkButton.addActionListener(elviraAction);
        
        /* Listeners for the explication Toolbar items */
        
        functionButton.addActionListener(elviraAction);
        expandButton.addActionListener(elviraAction);
        editorButton.addActionListener(elviraAction);
        storeCaseButton.addActionListener(elviraAction);
        saveCaseButton.addActionListener(elviraAction);
        firstButton.addActionListener(elviraAction);
        nextButton.addActionListener(elviraAction);
        previousButton.addActionListener(elviraAction);
        lastButton.addActionListener(elviraAction);
        deleteButton.addActionListener(elviraAction);
        emptyCasesButton.addActionListener(elviraAction);
        optionsButton.addActionListener(elviraAction);
        propagateButton.addActionListener(elviraAction);
        /*dectreeButton.addActionListener (elviraAction);*/
        explainCaseButton.addActionListener(elviraAction);
        pathsButton.addActionListener(elviraAction);
        
        caseButton.addActionListener(elviraAction);
        thresholdComboBox.addActionListener(elviraAction);
        
        /* Listeners for the returnToolbar items */
        returnButton.addActionListener(elviraAction);

        // Jorge-PFC el 05/12/2005: crea el mediador para esta instancia del UI de elvira
        elviraGUIMediator= new ElviraGUIMediator(this);
        
        elviraGUIMediatorPT = new ElviraGUIMediatorPT(this);
        
        // --> Jorge-PFC 26/12/2005
        insertButton(dectreeButton, dectreeIcon, editionToolbar, "Explanation.Dectree");
        dectreeButton.addActionListener(elviraGUIMediator);
        dectreeButton.setActionCommand("Create Decision Tree");        
        // <-- Jorge-PFC 26/12/2005

        //Introducido por Alberto Ruiz
        sensitivityOneAnalysisButton.addActionListener(elviraAction);
        //Fin Introducido por Alberto Ruiz

       //Introducido por jruiz
       insertButton(sensitivityAnalysisButton, sensitivityAnalysisIcon,
                 editionToolbar, "Analysis.SensitivityAnalysis");
       sensitivityAnalysisButton.addActionListener(elviraAction);
       //Fin introducido por jruiz

    }//end CONSTRUCTOR
    
    
    private void insertButton(AbstractButton button, javax.swing.Icon icon,
    JToolBar toolbar, String name) {
        button.setIcon(icon);
        button.setToolTipText(localize(menuBundle,name+".tip"));
        button.setMnemonic(localize(menuBundle,name+".mnemonic").charAt(0));
        toolbar.add(button);
        button.setMargin(new Insets(1,1,1,1));
        button.setAlignmentY(0.5f);
        button.setAlignmentX(0.5f);
    }
    
    public JMenu insertMenu(javax.swing.Icon icon, JMenu menu,
    String name, String actionCommand) {
        JMenu item = new JMenu();
        defineMenu(item, icon, menu, name, actionCommand);
        return item;
    }
    
    public JMenuItem insertMenuItem(javax.swing.Icon icon, JMenu menu,
    String name, String actionCommand) {
        JMenuItem item = new JMenuItem();
        defineMenuItem(item, icon, menu, name, actionCommand);
        return item;
    }
    
    public JCheckBoxMenuItem insertCheckBoxMenuItem(javax.swing.Icon icon,
    JMenu menu, String name, String actionCommand) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem();
        defineMenuItem(item, icon, menu, name, actionCommand);
        return item;
    }
    
    public void defineMenu(JMenu item, javax.swing.Icon icon,
    JMenu menu, String name, String actionCommand) {
        item.setIcon(icon);
        item.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        item.setText(localize(menuBundle,name+".label"));
        item.setActionCommand(actionCommand);
        item.setMnemonic((int) localize(menuBundle,name+".mnemonic").charAt(0));
        menu.add(item);
    }
    
    public void defineMenuItem(JMenuItem item, javax.swing.Icon icon,
    JMenu menu, String name, String actionCommand) {
        item.setIcon(icon);
        item.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        item.setText(localize(menuBundle,name+".label"));
        item.setActionCommand(actionCommand);
        item.setMnemonic((int) localize(menuBundle,name+".mnemonic").charAt(0));
        menu.add(item);
    }
    
    public JMenu insertMenu(String name) {
        JMenu menu = new JMenu();
        menu.setText(localize(menuBundle,name+".label"));
        menu.setActionCommand(name);
        menu.setMnemonic((int) localize(menuBundle,name+".mnemonic").charAt(0));
        menuBar.add(menu);
        return menu;
    }
    
    public void resizeDesktopPane(int x, int y) {
        Dimension actualSize = desktopPane.getPreferredSize();
        int width = (int) actualSize.getWidth(),
        height = (int) actualSize.getHeight();
        
        if (x>width)
            width = x;
        if (y>height)
            height = y;
        desktopPane.setPreferredSize(new Dimension(width, height));
        desktopPane.revalidate();
    }
    
    
    /**
     * Creates a new instance of ElviraFrame with the given title.
     * @param sTitle the title for the new frame.
     * @see #ElviraFrame()
     */
    
    public ElviraFrame(String sTitle) {
        this();
        setTitle(sTitle);
    }
    
    
    /**
     * Notifies this component that it has been added to a container
     * This method should be called by <code>Container.add</code>, and
     * not by user code directly.
     * Overridden here to adjust the size of the frame if needed.
     * @see java.awt.Container#removeNotify
     */
    
    public void addNotify() {
        // Record the size of the window prior to calling parents addNotify.
        Dimension size = getSize();
        
        super.addNotify();
        
        if (frameSizeAdjusted)
            return;
        frameSizeAdjusted = true;
        
        // Adjust size of frame according to the insets and menu bar
        JMenuBar menuBar = getRootPane().getJMenuBar();
        int menuBarHeight = 0;
        if (menuBar != null)
            menuBarHeight = menuBar.getPreferredSize().height;
        Insets insets = getInsets();
        setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
    }
    
    
    /**
     * Exits from Elvira. If there is any InternalFrame open, check if there
     * are unsaved changes display the corresponding dialogs.
     */
    
    void exitApplication() {
        try {
            // Beep
            Toolkit.getDefaultToolkit().beep();
            
            //WindowMenuListener listener = new WindowMenuListener ();
            JInternalFrame[] frames = desktopPane.getAllFrames();
            
            // Checking if every frame opened has unsaved changes
            for (int i=0; i<frames.length; i++) {
                
        	// Jorge-PFC 27/12/2005
        	if (frames[i] instanceof NetworkFrame) {
                    
        		/* If we are going to close a normal NetworkFrame, the relation between
                the active NetworkFrame and the list of opened frames must be
                      actualized. After this, the frame is closed */
                    
                    currentNetworkFrame = (NetworkFrame) frames[i];
                    //listener.menuSelected(new MenuEvent(this));
                    currentNetworkFrame.setClosed(true);
                }
            }
            this.setVisible(false);    // hide the Frame
            this.dispose();            // free the system resources
            System.exit(0);            // close the application
        } catch (Exception e) {
        }
    }
    
    
    /**
     * Set the text given as parameter into the status area
     */
    
    static public void showStatus(String s) {
        status.setText(s);
    }
    
    
    /**
     * Get the text of the undo menu item. Used with undo actions
     */
    
    public JMenuItem getUndoItem(){
        return undoItem;
    }
    
    public JMenuItem getRedoItem(){
        return redoItem;
    }
    
    public ResourceBundle getMenuBundle() {
        return menuBundle;
    }
    
    public ResourceBundle getDialogBundle() {
        return dialogBundle;
    }
    
    public ElviraDesktopPane getDesktopPane() {
        return desktopPane;
    }
    
    
    /**
     * Gets the visual precision of the Network currently on screen
     */
    
    public String getVisualPrecision() {
    	// Jorge-PFC 07/01/2006 ï¿½Se estï¿½ en proceso de repintado de un frame?
    	NetworkFrame frame= getNetworkToPaint();
    	if(frame!=null) {
    		return frame.getEditorPanel().getBayesNet().getVisualPrecision();
    	}
    	
    	// --> Jorge-PFC 07/01/2006
    	// DONE: utilizar el estado interno del DecisionTreeFrame y cambiar por controlar el 'selectedFrame'
    	JInternalFrame iframe= getDesktopPane().getSelectedFrame();
    	if (iframe instanceof NetworkFrame) {
    		return ((NetworkFrame)iframe).getEditorPanel().getBayesNet().getVisualPrecision();
    	}
    	
    	// Jorge-PFC 27/12/2005: probablemente no tenga sentido utilizar este metodo si el tipo
    	// de frame seleccionado no es un NetworkFrame, de todas maneras se devuelve el valor de
    	// la precision que tenga el menu de elvira
    	return LastPrecItem.getText().replace('1','0');
    }
    
    /**
     * Get the editor panel of the current networkFrame
     */
    
    public EditorPanel getCurrentEditorPanel() {
        return currentNetworkFrame.getEditorPanel();
    }
    
    public JMenuBar getBarMenu() {
        return menuBar;
    }
    
    public NetworkFrame getNetworkFrame() {
        return currentNetworkFrame;
    }
    
    public void setMaximizeWindows(boolean b) {
        maximizeWindows = b;
        if (!b)
            desktopPane.cascade();
    }
    
    public boolean isMaximizeWindows() {
        return maximizeWindows;
    }
    
    /**
     * Notifies this object that it is no longer the owner of
     * the contents of the clipboard
     *
     * @param clipboard The clipboard that is no longer owned
     * @param contents The contents which this owner had placed
     * on the clipboard
     */
    
    public void lostOwnership(Clipboard eClipboard, Transferable contents) {
        
    }
    
    
    /* ****************************************************** */
    /* ******************** MENU ACTIONS ******************** */
    /* ****************************************************** */
    
    /**
     * Shows a dialog to select the file to open, load into the frame
     * and enables the menu options for the new active frame
     * @see openFile
     */
    
    void openItem_actionPerformed(java.awt.event.ActionEvent event) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        
        try {
            // openFileDialog Show the FileDialog
            fileChooser.rescanCurrentDirectory();
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setDialogTitle("Open File");
            fileChooser.setElviraFilter();
            if (fileChooser.showOpenDialog(null) == JFileChooser.CANCEL_OPTION)
                return;
        } catch (Exception e) {}
        
        if (fileChooser.getSelectedFile() == null)
            return;
        
        try {
            openFile(fileChooser.getSelectedFile().getPath());
        } catch (Exception e) {
            System.out.println("Exception: "+e.toString()+ " opening file:"+
            fileChooser.getSelectedFile().getPath());
            return;
        };
        
        if (windowMenu.getItemCount()>8) {
            enableMenusOpenNetworks(true,
            currentNetworkFrame.getEditorPanel().getBayesNet());
            enableMenusOpenFrames(true);
            setInitialPrecision(currentNetworkFrame);
        }
    }
    
    void openDBCItem_actionPerformed(java.awt.event.ActionEvent event) {
        DataBaseMonitor dbcDialog = new DataBaseMonitor(this,
        localize(dialogBundle,"DataBaseMonitor.Title"));
        dbcDialog.setLocationRelativeTo(this);
        dbcDialog.show();
    }//end openDBCItem_actionPerformed(java.awt.event.ActionEvent)
    
    void saveItem_actionPerformed(java.awt.event.ActionEvent event) {
        saveAction();
    }
    
    
    void importItem_actionPerformed(java.awt.event.ActionEvent event) {
        importAction();
    }
    
    void exportItem_actionPerformed(java.awt.event.ActionEvent event) {
        exportAction();
    }
    
    
    void saveAsItem_actionPerformed(java.awt.event.ActionEvent event) {
        saveAsAction();
    }
    
    void saveAndReopenItem_actionPerformed(java.awt.event.ActionEvent event) {
        saveAction();
        JInternalFrame[] frames = desktopPane.getAllFrames();
        
        int frIndex = -1;
        
        for (int i=0; i<frames.length; i++) {
            if (frames[i].getTitle() == currentNetworkFrame.getTitle()) {
                frIndex = i;
                break;
            }
        }
        
        String toOpen = currentNetworkFrame.getTitle();
        currentNetworkFrame.setTitle("");
        
        try {
            openFile(toOpen);
        } catch (Exception e){}
        
        if (windowMenu.getItemCount()>8) {
            enableMenusOpenNetworks(true,
            currentNetworkFrame.getEditorPanel().getBayesNet());
            enableMenusOpenFrames(true);
            setInitialPrecision(currentNetworkFrame);
        }
        
        try{
            frames[frIndex].setClosed(true);
            frames[frIndex].dispose();
        } catch (Exception e){}
    }
    
    void exitItem_actionPerformed(java.awt.event.ActionEvent event) {
        try {
            saveLastReferences();
            this.exitApplication();
        } catch (Exception e) {
        }
        
    }
    
    void aboutItem_actionPerformed(java.awt.event.ActionEvent event) {
        try {
            // AboutDialog Create with owner and show as modal
            {
                ProgressDialog aboutDialog = new ProgressDialog();
                aboutDialog.setModal(true);
                aboutDialog.setAboutMode();
                aboutDialog.show();
            }
        } catch (Exception e) {
        }
    }
    
    void cascadeItem_actionPerformed(java.awt.event.ActionEvent event) {
        desktopPane.cascade();
    }
    
    void newItem_actionPerformed(java.awt.event.ActionEvent event) {
        Bnet bayesNet = new Bnet();
        bayesNet.setName("Untitled" + untitledCounter);
        NetworkPropertiesDialog d = new NetworkPropertiesDialog(this,true,bayesNet,true);
        bayesNet.setFSDefaultStates(Elvira.getDefaultStates());
        
        d.show();
        
        if (d.isCancel()) {
            d.dispose();
            return;
        }
        
        addNewFrame(d.bayesNet);
        
        d.dispose();
    }
    
    
    public void addNewFrame(Bnet b) {
        createNewFrame(b.getName(),true);
        untitledCounter++;
        if (windowMenu.getItemCount()>8) {
            enableMenusOpenNetworks(true,b);
            enableMenusOpenFrames(true);
        }
        
                /* Set the new bayes network obtained from the dialog displayed at
                   the begining of the method */
        
        currentNetworkFrame.getEditorPanel().setBayesNet(b);
        
    }
    
    
    void openURLItem_actionPerformed(java.awt.event.ActionEvent event) {
        OpenURLDialog openURL = new OpenURLDialog();
        openURL.show();
    }
    
    void previousItem_actionPerformed(java.awt.event.ActionEvent event) {
        desktopPane.previous();
    }
    
    void nextItem_actionPerformed(java.awt.event.ActionEvent event) {
        desktopPane.next();
    }
    
    void addChanceNodeAction(ActionEvent event) {
        currentNetworkFrame.getEditorPanel().setMode(EditorPanel.CREATE_NODE);
        currentNetworkFrame.getEditorPanel().setNodeType(Node.CHANCE);
        chanceNodeButton.setSelected(true);
        chanceItem.setSelected(true);
        Network bayelvira = currentNetworkFrame.getEditorPanel().getBayesNet();
        bayelvira.setDefaultChanceNode(Node.FINITE_STATES);
        
    }
    void addContinuousNodeAction(ActionEvent event) {
        currentNetworkFrame.getEditorPanel().setMode(EditorPanel.CREATE_NODE);
        currentNetworkFrame.getEditorPanel().setNodeType(Node.CHANCE);
        continuousNodeButton.setSelected(true);
        continuousItem.setSelected(true);
        Network bayelvira = currentNetworkFrame.getEditorPanel().getBayesNet();
        bayelvira.setDefaultChanceNode(Node.CONTINUOUS);
        
    }
    
    void closeItem_actionPerformed(java.awt.event.ActionEvent event) {
        JInternalFrame[] frames = desktopPane.getAllFrames();
        try {
            if (currentNetworkFrame!=null) {
                currentNetworkFrame.setClosed(true);
                currentNetworkFrame.dispose();
                if (frames.length==0)
                    enableMenusOpenFrames(false);
            }
            else
                if (messageWindow!=null)
                    messageWindow.setClosed(true);
        } catch (Exception e) {}
    }
    
    void minimizeAllItem_actionPerformed(java.awt.event.ActionEvent event) {
        desktopPane.closeAll();
    }
    
    void restoreAllItem_actionPerformed(java.awt.event.ActionEvent event) {
        desktopPane.openAll();
    }
    
    /**
     * Select the messageWindow as the active one. If this window is
     * not visible, create a new one
     */
    
    void showMessageWindowItem_actionPerformed(ActionEvent event) {
            /* if the message window is not visible, a new one
             is created */
        
        if (messageWindow.isClosed()) {
            
            messageWindow = new MessageFrame();
            desktopPane.add(messageWindow);
            
            if (windowMenu.getItemCount()<8)
                this.enableMenusOpenFrames(true);
            
            if (desktopPane.getAllFrames().length==1)
                windowMenu.add(new JSeparator());
            
            JCheckBoxMenuItem newMenuItem = new JCheckBoxMenuItem(localize(menuBundle,"MessageWindow.label"),true);
            newMenuItem.addItemListener(new WindowMenuItemListener());
            windowMenu.insert(newMenuItem, 8);
            windowGroup.add(newMenuItem);
            
        }
        else if (messageWindow.isIcon()) {
            try {
                messageWindow.setIcon(false);
            }
            catch (PropertyVetoException e) {}
        }
        
        
        /* After this, it must be set as the active window */
        try {
            messageWindow.setSelected(true);
            closeItem.setText(localize(menuBundle, "File.Close.label")+localize(menuBundle, "MessageWindow.label"));
        }
        catch (PropertyVetoException e) {
            System.out.println("You can't select that frame"); }
        
        if (currentNetworkFrame!=null) {
            try {
                currentNetworkFrame.setSelected(false);
            }
            catch(PropertyVetoException e) {}
            
            currentNetworkFrame = null;
        }
    }
    
    void undoAction(ActionEvent event) {
        currentNetworkFrame.getEditorPanel().undo();
        currentNetworkFrame.getEditorPanel().repaint();
        redoItem.setEnabled(true);
    }
    
    void redoAction(ActionEvent event) {
        currentNetworkFrame.getEditorPanel().redo();
        currentNetworkFrame.getEditorPanel().repaint();
    }
    
    void zoomOutAction(ActionEvent event) {
    	// --> Jorge-PFC 27/12/2005
    	JInternalFrame iframe= desktopPane.getSelectedFrame();
    	if (iframe instanceof DecisionTreeFrame) {
    		// DONE 29/12/2005: Por ahora no se refleja en el combo del zoom
    		// Llamar a un metodo del decisiontreeframe para que realice la actualizacion
    		// y tb copie el valor a su estado interno
    		((DecisionTreeFrame)iframe).modifyZoomFactor(-0.2);
    		return;
    	}
    	// <-- Jorge-PFC 27/12/2005
    	
        double z = currentNetworkFrame.getEditorPanel().getZoom();
        if (z>0.2) {
            z=z-0.2;
            currentNetworkFrame.setZoom(z);
            currentNetworkFrame.repaintPanel(z);
            zoomComboBox.setSelectedItem(String.valueOf((int) (z*100))+"%");
        }
    }
    
    void zoomInAction(ActionEvent event) {
    	// --> Jorge-PFC 27/12/2005
    	JInternalFrame iframe= desktopPane.getSelectedFrame();
    	if (iframe instanceof DecisionTreeFrame) {
    		// DONE 29/12/2005: Por ahora no se refleja en el combo del zoom
    		// Llamar a un metodo del decisiontreeframe para que realice la actualizacion
    		// y tb copie el valor a su estado interno
    		((DecisionTreeFrame)iframe).modifyZoomFactor(+0.2);
    		return;
    	}
    	// <-- Jorge-PFC 27/12/2005
    	
        double z = currentNetworkFrame.getEditorPanel().getZoom();
        if (z<=5) {
            z=z+0.2;
            currentNetworkFrame.setZoom(z);
            currentNetworkFrame.repaintPanel(z);
            zoomComboBox.setSelectedItem(String.valueOf((int) (z*100))+"%");
        }
    }
    
    void byTitleAction(ActionEvent event) {
    	// --> Jorge-PFC 27/12/2005
    	JInternalFrame iframe= desktopPane.getSelectedFrame();
    	if (iframe instanceof DecisionTreeFrame) {
    		((DecisionTreeFrame) iframe).saveDescriptionByTitle(true);
    		return;
    	}
    	// <-- Jorge-PFC 27/12/2005
    	
        currentNetworkFrame.getEditorPanel().setByTitle(true);
        currentNetworkFrame.getInferencePanel().setByTitle(true);
        currentNetworkFrame.repaintPanel();
    }
    
    void byNameAction(ActionEvent event) {
    	// --> Jorge-PFC 27/12/2005
    	JInternalFrame iframe= desktopPane.getSelectedFrame();
    	if (iframe instanceof DecisionTreeFrame) {
    		((DecisionTreeFrame) iframe).saveDescriptionByTitle(false);
    		return;
    	}
    	// <-- Jorge-PFC 27/12/2005
    	
        currentNetworkFrame.getEditorPanel().setByTitle(false);
        currentNetworkFrame.getInferencePanel().setByTitle(false);
        currentNetworkFrame.repaintPanel();
    }
    
    void setPrecision(ActionEvent event) {
        //System.out.println("Set Precision Selected");
        return;
    }
    
    void PrecItem(ActionEvent event, Object object) {
        //System.out.println("Set Precision Element Selected");
        //System.out.println("Was selected: "+object.toString());
        LastPrecItem.setSelected(false);
        
        // --> Jorge-PFC 27/12/2005
        LastPrecItem= ((JCheckBoxMenuItem) object);
        LastPrecItem.setSelected(true);

        // DONE: si es un DecisionTreeFrame, almacenar la precision en su Estado interno (hecho por dicha clase)
        if( currentNetworkFrame!=null ) {
    		// Una precision de 3 decimales se indica en el menu de elvira como '0.001' y como '0.000' en el
        	// atributo 'visualPrecision' de la red, de ahi que para almacenar la precision que se ha seleccionado
        	// baste con tomar el texto del menu y reemplazar el ultimo '1' con un '0' antes de fijar la precision
        	String precision= LastPrecItem.getText().replace('1','0');
        	currentNetworkFrame.getEditorPanel().getBayesNet().setVisualPrecision(precision);
        	currentNetworkFrame.getInferencePanel().expandNodes();
        }
        // <-- Jorge-PFC 27/12/2005
    }
    
    void addDirectedLinkAction(ActionEvent event) {
        currentNetworkFrame.getEditorPanel().setMode(EditorPanel.CREATE_LINK);
        directedLinkButton.setSelected(true);
        linkItem.setSelected(true);
    }
    
    void constraints_actionPerformed(ActionEvent event) {
        /* To write */
        EditIDiagramConstraints eidc = new EditIDiagramConstraints((IDiagram) currentNetworkFrame.getEditorPanel().getBayesNet());
        eidc.show();
    }
    
    void addDecisionNodeAction(ActionEvent event) {
        currentNetworkFrame.getEditorPanel().
        setMode(EditorPanel.CREATE_NODE);
        currentNetworkFrame.getEditorPanel().setNodeType(Node.DECISION);
        decisionNodeButton.setSelected(true);
        decisionItem.setSelected(true);
    }
    
    void addUtilityNodeAction(ActionEvent event) {
        currentNetworkFrame.getEditorPanel().
        setMode(EditorPanel.CREATE_NODE);
        currentNetworkFrame.getEditorPanel().setNodeType(Node.UTILITY);
        utilityNodeButton.setSelected(true);
        utilityItem.setSelected(true);
    }
    
   
    
    void showInfluencesEdition(){
    	Network net;
    	boolean showInfl;
    	boolean addTerminal;
    	IDWithSVNodes auxID;
    	boolean hasCPN;
    	 net = currentNetworkFrame.getEditorPanel().getBayesNet();
    	
        if (influenceLinkButton.isSelected()){
        	showInfl = false;
        }
        else {
        	if (IDiagram.class.isInstance(net)){
        		//We need a terminal SV node if it is necessary
        		auxID = (IDWithSVNodes) net;
        			if (doWeNeedToAddTerminalSVNode(auxID)){
        				
        				addTerminal = askWhetherAddATerminalValueNodeInfluences(auxID);
        				if (addTerminal){
        					addTerminalSuperValueNode(auxID);
        					this.getCurrentEditorPanel().setModifiedNetwork(true);
        					
        				}
        				showInfl = addTerminal;
        			}
        			else{
        				showInfl = true;
        			}
        			//We calculate the CPN if it is necessary
        			if (showInfl){
        				hasCPN = (auxID.getCpn()!=null);
        				if (hasCPN==false){
        					auxID.obtainAndSetCPN();
        				}
        			}	
				}
        	else{
            	showInfl = true;
            }
        }
        
               
        influenceItem.setSelected(showInfl);
        influenceLinkButton.setSelected(showInfl);
        currentNetworkFrame.getEditorPanel().setInfluences(showInfl);
        
        currentNetworkFrame.getEditorPanel().repaint();
    }
    
  
    	 private void addTerminalSuperValueNode(IDWithSVNodes auxID) {
		// TODO Auto-generated method stub
    		 Node newNode = auxID.addATerminalSuperValueNode();
     		setVisualPropertiesNewSVNode(newNode);
    	 
		
	}


		private boolean askWhetherAddATerminalValueNodeInfluences(IDWithSVNodes auxID) {
    			// TODO Auto-generated method stub
    				boolean propagate;
    		    	String msg;
    		    	
    				
    		    	msg = ShowMessages.ADD_TERMINAL_VALUE_NODE_INFLUENCES;
    		    	
    		    	int reply;
    		    	
    		    	Object[] options = {localize(dialogBundle,"Yes.label"),localize(dialogBundle,"No.label")};
    		    	
    		    	reply = ShowMessages.showOptionDialog(msg,JOptionPane.QUESTION_MESSAGE,options,0);
    		    	
    		    	propagate = (reply==0);
    		    	
    		return propagate;

	}


	private boolean doWeNeedToAddTerminalSVNode(IDWithSVNodes net) {
		// TODO Auto-generated method stub
    	boolean add;
    	
    	IDWithSVNodes auxID = (IDWithSVNodes)net;
    	
    	add = (IDWithSVNodes.class.isInstance(net))&&(auxID.hasOnlyOneTerminalValueNode()==false);
    	
    	return add;
	}


	void showInfluencesInference(){
        if (influencesLinkButton.isSelected()){
            influenceItem.setSelected(false);
            influencesLinkButton.setSelected(false);
            currentNetworkFrame.getInferencePanel().setInfluences(false);
        }
        else {
            influenceItem.setSelected(true);
            influencesLinkButton.setSelected(true);
            currentNetworkFrame.getInferencePanel().setInfluences(true);
        }
        currentNetworkFrame.getInferencePanel().repaint();
    }
    
    void networkPropertiesItem_actionPerformed(ActionEvent event) {
        NetworkPropertiesDialog d = new NetworkPropertiesDialog();
        d.show();
    }
    
    
    public void activeSelect() {
        selectButton.setSelected(true);
        selectItem.setSelected(true);
    }
    
    /**
     * Set the editor mode to Move. This will be done
     * when the select tool is selected.
     */
    
    void selectAction(ActionEvent event) {
        currentNetworkFrame.getEditorPanel().setMode(EditorPanel.MOVE);
        activeSelect();
    }
    
    
    /**
     * Select all the nodes and links of the current
     * network
     */
    
    void selectAllAction(ActionEvent event) {
        currentNetworkFrame.getEditorPanel().selectAll();
    }
    
    
    /**
     * Removes the nodes and links selected in the current
     * NetworkFrame
     */
    
    void deleteAction(ActionEvent event) {
        EditorPanel e = ((NetworkFrame) getCurrentNetworkFrame()).getEditorPanel();
        e.delete();
        e.repaint();
    }
    
    
    /**
     * Method that is executed when the cut option is enter.
     * Stores the nodes and links selected in the current NetworkFrame
     * in the clipboard and removes from it.
     */
    
    public void cutAction(ActionEvent event) {
        EditorPanel e = ((NetworkFrame) getCurrentNetworkFrame()).getEditorPanel();
        e.deleteSelection();
        currentNetworkFrame.getEditorPanel().undoCutAction();
        e.repaint();
        elviraClipboard.setContents((Selection) e.getSelection(), this);
        enablePaste(true);
    }
    
    
    /**
     * Method that is executed when the paste option is enter.
     * Get the selection store in the clipboard and add to the
     * current NetworkFrame.
     */
    
    public void pasteAction(ActionEvent event) {
        EditorPanel e = ((NetworkFrame) getCurrentNetworkFrame()).getEditorPanel();
        Selection s = (Selection) elviraClipboard.getContents(this);
        Selection s_clone =  s.copySelectionToPaste();
        s_clone.changeAllNames(e.getBayesNet());
        e.addSelection(s_clone);
        currentNetworkFrame.getEditorPanel().undoPasteAction(s_clone);
        e.repaint();
    }
    
    
    /**
     * Method that is executed when the copy option is enter.
     * Stores the nodes and links selected in the current NetworkFrame
     * in the clipboard.
     */
    
    public void copyAction(ActionEvent event) {
        EditorPanel e = ((NetworkFrame) getCurrentNetworkFrame()).getEditorPanel();
        elviraClipboard.setContents(e.getSelection().copySelectionToPaste(), this);
        enablePaste(true);
    }
    
    void loadEvidenceAction(ActionEvent event) {
        Bnet bn = currentNetworkFrame.getEditorPanel().getBayesNet();
        int state = 0;
        
        try {
            // openFileDialog Show the FileDialog
            fileChooser.rescanCurrentDirectory();
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setDialogTitle("Load Evidence");
            fileChooser.setEvidenceFilter();
            state = fileChooser.showOpenDialog(null);
        } catch (Exception e) {
        }
        
        if (fileChooser.getSelectedFile() == null || state == JFileChooser.CANCEL_OPTION)
            return;
        
        String filename = fileChooser.getSelectedFile().getPath();
        
        if (loadEvidenceFromFile(filename,bn)) {
            Elvira.println("\tEvidence file loaded.\n\n");
            
            // check if the evidence is coherent with the actual belief network
            
                  /* if (!currentNetworkFrame.getInferencePanel().getEvidence().coherentEvidence(bn)){
                  currentNetworkFrame.getInferencePanel().setEvidence (new Evidence());
                  Elvira.println("\tThe loaded evidence is not coherent with the belief network\n\n");
                  }*/
        }
        else
            Elvira.println("\tEvidence file not loaded correctly.\n\n");
        
    }
    
    
    /**
     * Load the evidence from a file
     *
     * @param filename The name of the file to open
     * @return True if there is no problem loading the file
     * False in other case
     */
    
    private boolean loadEvidenceFromFile(String filename, Bnet bn){
        Evidence ev;
        boolean problems=false;
        try {
            Elvira.println("\nLoading " + filename + "\n");
            FileInputStream f = new FileInputStream(filename);
            ev=new Evidence(f,bn.getNodeList());
            //currentNetworkFrame.getInferencePanel().setEvidence (ev);
            f.close();
            if(!currentNetworkFrame.getInferencePanel().
            getEvidence().coherentEvidence(bn)){
                //currentNetworkFrame.getInferencePanel().setEvidence (new Evidence());
                Elvira.println("\tThe loaded evidence is not coherent with the belief network\n\n");
                problems=true;
            }
            
            else {currentNetworkFrame.getInferencePanel().getCasesList().storeCase(ev);
            Elvira.getElviraFrame().setNodeName(
            currentNetworkFrame.getInferencePanel().
            getCasesList().getCurrentCase().getIdentifier());
            Elvira.getElviraFrame().setColorNodeName(
            currentNetworkFrame.getInferencePanel().
            getCasesList().getCurrentCase().getColor());
            currentNetworkFrame.getInferencePanel().repaint();
            if(currentNetworkFrame.getInferencePanel().AUTOPROPAGATION){
                if(!currentNetworkFrame.getInferencePanel().propagate(
                currentNetworkFrame.getInferencePanel().getCasesList().
                getCurrentCase())){
                    Bnet bnet=currentNetworkFrame.getEditorPanel().getBayesNet();
                    ShowMessages.showMessageDialog(ShowMessages.IMPOSIBLE_EVIDENCE,
                    JOptionPane.ERROR_MESSAGE);
                    for (int n=0; n<bnet.getNodeList().size(); n++)
                        currentNetworkFrame.getInferencePanel().getCasesList().
                        getCurrentCase().unsetAsFinding(bnet.getNodeList().elementAt(n));
                    currentNetworkFrame.getInferencePanel().propagate(
                    currentNetworkFrame.getInferencePanel().getCasesList().
                    getCurrentCase());
                }
            }
            }//else
        }
        catch (ParseException e) {
            Elvira.println("Parse error: " + e + "\n");
            problems=true;
            //return(false);
        }
        catch (IOException e) {
            Elvira.println("Exception: " + e +"\n");
            problems=true;
            //return(false);
        }
        
        return(!problems);
        
    }
    
    
    void saveEvidenceAction(ActionEvent event) {
        Bnet bn = currentNetworkFrame.getEditorPanel().getBayesNet();
        int state = 0;
        
        try {
            // saveFileDialog Show the FileDialog
            fileChooser.rescanCurrentDirectory();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setDialogTitle("Save Evidence");
            fileChooser.setEvidenceFilter();
            state = fileChooser.showSaveDialog(null);
        } catch (Exception e) {}
        
        if (fileChooser.getSelectedFile() == null || state == JFileChooser.CANCEL_OPTION)
            return;
        
        String filename = fileChooser.getSelectedFile().getPath();
        
        if (saveEvidenceToFile(filename,bn)) {
            Elvira.println("\tEvidence file saved.\n\n");
        }
        else Elvira.println("\tEvidence object not saved to file correctly.\n\n");
        
    }
    
    /**
     * Save the evidence in a file
     *
     * @param filename The file where the evidence be saved
     * @return True if there is no problem saving the file
     * False in other case.
     */
    
    public boolean saveEvidenceToFile(String filename, Bnet bn){
        
        Evidence evidenceToSave; // = new Evidence();
        evidenceToSave = currentNetworkFrame.getInferencePanel().getEvidence();
        
        if (evidenceToSave == null) {
            Elvira.println("\t No Evidence object to be saved.\n\n");
            return(false);
        }
        else if ( ((Vector) evidenceToSave.getVariables()).size() == 0){
            Elvira.println("\t No Evidence object to be saved.\n\n");
            return(false);
        }
        
        try {
            
            if ( ((String) evidenceToSave.getName()).equals("") )
                evidenceToSave.setName("noName");
            
            Elvira.println("\nSaving " + filename + "\n");
            FileWriter f = new FileWriter(filename);
            evidenceToSave.save(f);
            f.close();
        }
        catch (IOException e) {
            Elvira.println("Exception: " + e +"\n");
            return(false);
        }
        
        return(true);
        
    }
    
    
    void importBnetXbifAction(ActionEvent event){
       
        int state = 0;
        
        try {
            // openFileDialog Show the FileDialog
            fileChooser.rescanCurrentDirectory();
            fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooser.setDialogTitle("Import network from XBIF");
            fileChooser.setXBIFFilter();
            state = fileChooser.showOpenDialog(null);
        } catch (Exception e) {
        }
        
        if (fileChooser.getSelectedFile() == null || state == JFileChooser.CANCEL_OPTION)
            return;
        
        String filename = fileChooser.getSelectedFile().getPath();
        
        if (importBnetXbifFromFile(filename)) {
            Elvira.println("\tNetwork file loaded.\n\n");
        }
        else
            Elvira.println("\tNetwork file not loaded correctly.\n\n");
    }
    
    /**
     * Load a Bnet from a Xbif format file
     *
     * @param filename The name of the file to open
     * @return True if there is no problem loading the file
     * False in other case
     */
    
    public boolean importBnetXbifFromFile(String filename){
       
      try {
         openXbifFile(filename);
         return true;
      }catch(Exception e){
        Elvira.println("\tProblems importing xbif file.\n\n");
         return false;}
    }
       
  
    
    void exportBnetXbifAction(ActionEvent event){
       
       
        int state = 0;
        
        try {
            // openFileDialog Show the FileDialog
            fileChooser.rescanCurrentDirectory();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setDialogTitle("Save network in Xbif");
            fileChooser.setXBIFFilter();
            state = fileChooser.showOpenDialog(null);
        } catch (Exception e) {
        }
        
        if (fileChooser.getSelectedFile() == null || state == JFileChooser.CANCEL_OPTION)
            return;
        
        String filename = fileChooser.getSelectedFile().getPath();
        
        
        if (exportBnet2XbifFile(filename)) {
            Elvira.println("\tNetwork file saved.\n\n");
        }
        else
            Elvira.println("\tNetwork file not save correctly.\n\n");
    }
    
    /**
     * Save a network in a Xbif format file
     *
     * @param filename The name of the file to save
     * @return True if there is no problem loading the file
     * False in other case
     */
    
    public boolean exportBnet2XbifFile(String filename){
       
      Evidence ev = new Evidence();
      
      if (currentNetworkFrame.getMode() == currentNetworkFrame.INFERENCE_ACTIVE)
         ev=currentNetworkFrame.getInferencePanel().getEvidence();
      
      try {
         FileWriter f = new FileWriter(filename);
         Bnet bnet=currentNetworkFrame.getEditorPanel().getBayesNet();
         bnet.saveBnet2XBIF(f, ev);
         f.close();
         JOptionPane.showMessageDialog(null,"File XBIF saved", "", 1);
         return true;
      }catch(Exception e){return false;}
    }
    
    
    public void expandAction(ActionEvent event) {  
        currentNetworkFrame.getInferencePanel().
        expandMenuItem_actionPerformed(event);
    }
    
    public void saveCaseAction(ActionEvent event) {
        int state=0;
        try {
            fileChooser.rescanCurrentDirectory();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setDialogTitle("Save Case");
            fileChooser.setSelectedFile(new File(""));
            fileChooser.setCaseFilter();
            state = fileChooser.showSaveDialog(null);
            
        } catch (Exception e) {
        }
        
        if (fileChooser.getSelectedFile() == null || state == JFileChooser.CANCEL_OPTION)
            return;
        
        String filename = fileChooser.getSelectedFile().getPath();
        
        if (saveCaseToFile(filename)) {
            Elvira.println("\tEvidence file saved.\n\n");
        }
        else Elvira.println("\tEvidence object not saved to file correctly.\n\n");
        
    }
    
    /**
     * Saves the case in a file
     *
     * @param filename The file where the case will be saved
     * @return True if there is no problem when saving the file
     * False in other case.
     */
    
    public boolean saveCaseToFile(String filename){
        
        Case caseToSave = currentNetworkFrame.getInferencePanel().getCasesList().getCurrentCase();
        
        if (caseToSave == null) {
            Elvira.println("\t No Case object to be saved.\n\n");
            return(false);
        }
        
        try {
            
            if ( ((String) caseToSave.getIdentifier()).equals("") )
                caseToSave.setIdentifier("noName");
            
            Elvira.println("\nSaving " + filename + "\n");
            FileWriter f = new FileWriter(filename);
            caseToSave.save(f);
            f.close();
        }
        catch (IOException e) {
            Elvira.println("Exception: " + e +"\n");
            return(false);
        }
        
        return(true);
        
    }
    
    public void storeCaseAction(ActionEvent event) {
        currentNetworkFrame.getInferencePanel().storeCase_actionPerformed(event);
    }
    
    public void explainAction(ActionEvent event) {
        currentNetworkFrame.getInferencePanel().
        explainMenuItem_actionPerformed(event);
    }
    
    public void firstCaseAction(ActionEvent event) {
        currentNetworkFrame.getInferencePanel().
        firstCaseItem_actionPerformed(event);
    }
    
    public void nextCaseAction(ActionEvent event) {
        currentNetworkFrame.getInferencePanel().
        nextCaseItem_actionPerformed(event);
    }
    
    public void previousCaseAction(ActionEvent event) {
        currentNetworkFrame.getInferencePanel().
        previousCaseItem_actionPerformed(event);
    }
    
    public void lastCaseAction(ActionEvent event) {
        currentNetworkFrame.getInferencePanel().
        lastCaseItem_actionPerformed(event);
    }
    
    public void caseMonitorAction(ActionEvent event) {
        CaseMonitor d = new CaseMonitor(currentNetworkFrame);
        d.show();
    }
    
    public void caseEditorAction(ActionEvent event) {
        CaseEditor d = new CaseEditor(currentNetworkFrame);
        d.show();
    }
    
    public void optionsAction(ActionEvent event) {
        OptionsInference d = new OptionsInference(currentNetworkFrame);
        d.show();
    }
    
    public void deleteEviAction(ActionEvent event){
        Bnet bnet=currentNetworkFrame.getInferencePanel().getBayesNet();
        InferencePanel infpanel=currentNetworkFrame.getInferencePanel();
        CasesList casesl=currentNetworkFrame.getInferencePanel().getCasesList();
        for (int n=0; n<bnet.getNodeList().size(); n++)
            casesl.getCurrentCase().unsetAsFinding(bnet.getNodeList().elementAt(n));
        if (infpanel.AUTOPROPAGATION) infpanel.propagate(casesl.getCurrentCase());
        infpanel.repaint();
    }
    
    public void emptyCasesAction(ActionEvent event){
        Bnet bnet=currentNetworkFrame.getInferencePanel().getBayesNet();
        InferencePanel infpanel=currentNetworkFrame.getInferencePanel();
        CasesList casesl=currentNetworkFrame.getInferencePanel().getCasesList();
        int nc=casesl.getNumStoredCases();
        for (int c=0; c<nc-1; c++){
            casesl.removeCase(casesl.getCaseNum(1));
        }
        casesl=new CasesList(bnet);
        Elvira.getElviraFrame().setNodeName(casesl.getCurrentCase().getIdentifier());
        Elvira.getElviraFrame().setColorNodeName(casesl.getCurrentCase().getColor());
        infpanel.setCasesList(casesl);
        infpanel.repaint();
        
    }
    
    public void returnButtonAction(ActionEvent event) {
        evd.returnButton_actionPerformed(event);
    }
    
    public void disableReturnToolbar() {
        menuBar.setVisible(true);
        returnToolbar.setVisible(false);
        standardToolbar.setVisible(true);
        editionToolbar.setVisible(true);
        evd = null;
    }
    
    public void enableReturnToolbar(EditVariableDialog theEvd) {
        menuBar.setVisible(false);
        standardToolbar.setVisible(false);
        editionToolbar.setVisible(false);
        returnToolbar.setVisible(true);
        evd = theEvd;
    }
        //  PFC 2007    //Introducido por Alberto Ruiz
    public void sensitivityOneAnalysisMethod(ActionEvent event) {

      try {
        if (currentNetworkFrame != null) {
          AnalisysMainFrame ana = new AnalisysMainFrame (currentNetworkFrame);
      
        }
        else {
        //	Analisis1 d = new Analisis1(null);
        }
      }
      catch (Exception e) {}
    }//Fin introducido por ARuiz

    public void propagateAction(ActionEvent event) {
        Bnet bnet=currentNetworkFrame.getEditorPanel().getBayesNet();
        InferencePanel networkInferencePanel=currentNetworkFrame.getInferencePanel();
        if (!bnet.getIsCompiled()){
            bnet.compile(networkInferencePanel.getInferenceMethod(),
            networkInferencePanel.getParameters(),
            networkInferencePanel.getAuxiliaryFilesNames(),
            networkInferencePanel.getAbductiveValues());
            bnet.setCompiled(true);
            enableExplanationOptions(true);
            currentNetworkFrame.activeInferencePanel();
        }
        else if (!currentNetworkFrame.getInferencePanel().propagate(currentNetworkFrame.getInferencePanel().getCasesList().getCurrentCase()))
            ShowMessages.showMessageDialog(ShowMessages.IMPOSIBLE_EVIDENCE, JOptionPane.ERROR_MESSAGE);
    }
    
    public void dectreeAction(ActionEvent event) {
        /* To define */
        ShowDecisionTree sdt = new ShowDecisionTree((IDiagram) currentNetworkFrame.getInferencePanel().getBayesNet());
        sdt.show();
    }
    
    public void explainCaseAction(ActionEvent event) {
        ExplainCase ec=new ExplainCase(currentNetworkFrame.getInferencePanel().getCasesList(),
        currentNetworkFrame.getInferencePanel().getCasesList().getCurrentCase(),
        currentNetworkFrame.getInferencePanel().getCasesList().getNumCurrentCase());
        ec.show();
    }
    
    public void pathsAction(ActionEvent event) {
        Bnet bnet=currentNetworkFrame.getEditorPanel().getBayesNet();
        for (int n=0; n<bnet.getNodeList().size(); n++)
            ((Node)bnet.getNodeList().elementAt(n)).setVisited(false);
        for (int l=0; l<bnet.getLinkList().size(); l++)
            ((Link)bnet.getLinkList().elementAt(l)).setUPDOWN(true);
        currentNetworkFrame.getInferencePanel().PATHS=false;
        currentNetworkFrame.getInferencePanel().MACROEXPLANATION=false;
        enablePathsButton();
        currentNetworkFrame.getInferencePanel().repaint();
    }
    
    public void selectNodestoExpand(ActionEvent event){
        PurposeDialog pd=new PurposeDialog(localize(dialogBundle,"PurposeDialog.Title"));
        pd.show();
        currentNetworkFrame.getInferencePanel().expandNodes();
    }
    
    public void expandNodes(ActionEvent event) {
        try {
            Double thresholdValue = Double.valueOf((String) thresholdComboBox.getSelectedItem());
            
            currentNetworkFrame.getInferencePanel().setExpansionThreshold(
            thresholdValue.doubleValue());
            
            currentNetworkFrame.getInferencePanel().expandNodes();
            
        }
        catch (NumberFormatException e) {
            ShowMessages.showMessageDialog(
            ShowMessages.WRONG_EXPANSION_THRESHOLD,
            JOptionPane.ERROR_MESSAGE);
            thresholdComboBox.setSelectedItem("5.00");
        }
        
    }
    
    public void fusionMethod(ActionEvent event) {
        FusionDialog d = new FusionDialog();
        if (d.canShow())
            d.show();
    }
    
    //Introducido por jruiz
    public void sensitivityAnalysisMethod(ActionEvent event) {

      try {
        if (currentNetworkFrame != null) {
          SensitivityAnalysis d = new SensitivityAnalysis(this);
        }
        else {
          SensitivityAnalysis d = new SensitivityAnalysis(false);
        }
      }
      catch (Exception e) {}
    }//Fin introducido por jruiz

    public void propagationMethod(ActionEvent event) {
        //if (currentNetworkFrame.getEditorPanel().getBayesNet().getClass() == IDiagram.class) {
        if ((currentNetworkFrame.getEditorPanel().getBayesNet().getClass() == IDiagram.class)||
        (currentNetworkFrame.getEditorPanel().getBayesNet().getClass() == IDWithSVNodes.class)) {
            Vector reList = currentNetworkFrame.getEditorPanel().getBayesNet().getRelationList();
            boolean withConstraints = false;
            for (int i=0; i<reList.size(); i++) {
                if (((Relation) reList.elementAt(i)).getKind() == Relation.CONSTRAINT) {
                    withConstraints = true;
                }
            }
            
      /*
       * No special treatment in IDPropagationDialog with constraints
       */
            
            withConstraints = false;
            
            IDPropagationDialog d;
            if (!withConstraints) {
            	/*Bnet bnet=currentNetworkFrame.getEditorPanel().getBayesNet();
            	if (bnet.getClass()==IDWithSVNodes.class){
            		if (((IDWithSVNodes)bnet).hasSVNodes()){
						//Set Tatman and Shachter's algorithm as inference method.
						currentNetworkFrame.getInferencePanel().setInferenceMethod(8);
            		}
            	}*/
            	
                d = new IDPropagationDialog(
                currentNetworkFrame.getInferencePanel());
            }
            else {
                int index = currentNetworkFrame.getInferencePanel().getInferenceMethod();
                if ((index != 2) && (index != 5)) {
                    currentNetworkFrame.getInferencePanel().setInferenceMethod(2);
                    Vector v = new Vector();
                    Double db = new Double(0.0);
                    v.addElement(db);
                    currentNetworkFrame.getInferencePanel().setParameters(v);
                }
                
                d = new IDPropagationDialog(
                currentNetworkFrame.getInferencePanel());
            }
            d.show();
        }
        else {
            PropagationDialog d = new PropagationDialog(
            currentNetworkFrame.getInferencePanel());
            d.show();
        }
    }
    
    public void generateDBCMethod(ActionEvent event) {
        GenerateDBC d = new GenerateDBC(
        currentNetworkFrame.getGenerateDBCPanel());
        d.show();
    }
    
    public void inferenceOptions(ActionEvent event) {
        OptionsInference d = new OptionsInference(currentNetworkFrame);
        d.show();
    }
    
    
    public void explanationOptions(ActionEvent event) {
        OptionsExplanation d = new OptionsExplanation(currentNetworkFrame);
        d.show();
    }
    
    public void setThresholdComboValue(double d) {
        String value = String.valueOf(d);
        thresholdComboBox.setSelectedItem(value);
    }
    
    //   public void setFunctionComboValue (String s) {
    //      functionComboBox.setSelectedItem(s);
    //   }
    
    public double getThresholdComboValue() {
        String value = (String) thresholdComboBox.getSelectedItem();
        Double d = Double.valueOf(value);
        return d.doubleValue();
    }
    
    //   public String getFunctionComboValue () {
    //      return (String) functionComboBox.getSelectedItem();
    //   }
    
    public void setNodeName(String name){
        nodeName.setFont(new Font("SansSerif",Font.BOLD,12));
        nodeName.setText(name);
        nodeName.setEditable(false);
        nodeName.repaint();
    }
    
    public void setColorNodeName(Color c){
        nodeName.setBackground(c);
    }
    
    
    /* *************** AUXILIARY FUNCTIONS *************** */
    
   /* The next functions are mainly used by the methods implement
      above for menus and buttons */
    
    
    public void activeMode(int mode) {
        if (mode == NetworkFrame.INFERENCE_ACTIVE) {
            workingMode.setSelectedIndex(1);
            setPrecisionItem.setEnabled(true);
        }
        else {
            workingMode.setSelectedIndex(0);
            setPrecisionItem.setEnabled(false);
        }
    }
    
    /**
     * Este metodo se utiliza para el analisis de sensibilidad.
     * @author jruiz
     */
    public boolean byTitleItemIsSelected() {
    
      return byTitleItem.isSelected();
    }

    /**
     * This method do all the necessary operations to insert a new
     * internalFrame into the ElviraFrame, setting it as the active frame.
     *
     * @param name Title of the NetworkFrame created
     * @param isNew True if the new frame created will be empty
     * and false if the new frame is going to contain a
     * network loaded from a file
     */
    
    public void createNewFrame(String name, boolean isNew) {
        JCheckBoxMenuItem newMenuItem;
        
        currentNetworkFrame = new NetworkFrame(
        name,     // title
        true,  // resizable
        true,  // closable
        true,  // maximizable
        true,  // iconifiable
        isNew);
        
        //setBounds((numFrames-1)*25, (numFrames-1)*25, 400, 400);
        desktopPane.putClientProperty(
        "JDesktopPane.dragMode",
        "outline");
        
        desktopPane.add(currentNetworkFrame);
        
        try {
            currentNetworkFrame.setSelected(true);
            currentNetworkFrame.setMaximum(true);
            currentNetworkFrame.setVisible(true);
            closeItem.setText(localize(menuBundle,"File.Close.label")+currentNetworkFrame.getTitle());
        }
        catch (PropertyVetoException e) {
            System.out.println("You can't select that frame"); }
        
        if (desktopPane.getAllFrames().length==1)
            windowMenu.add(new JSeparator());
        
        newMenuItem = new JCheckBoxMenuItem(name,false);
        newMenuItem.addItemListener(new WindowMenuItemListener());
        windowMenu.add(newMenuItem);
        windowGroup.add(newMenuItem);
        newMenuItem.setSelected(true);
        
    }
    
    
    /**
     * Do all the operations related with the opening of a file: create a new
     * pane for the file, load the network in the editorPanel, and sets the
     * name of the file in all the menuItems where it is necessary
     */
    
    public void openFile(String nameOfFile) throws
    ParseException, IOException{
        //Bnet netToLoad;
        Network netToLoad;
                       
         try {
            netToLoad=Network.read(nameOfFile);
            //     FileInputStream f;
            Elvira.println("\nLoading " + nameOfFile + "\n");
            //      f = new FileInputStream(nameOfFile);
            
            /*     BayesNetParse parser = new BayesNetParse(f);
            parser.initialize();
             
            parser.CompilationUnit();
            if (parser.Type.equals("iDiagram"))
            netToLoad = new IDiagram();
            else
            netToLoad = new Bnet();
            netToLoad.translate(parser);
             
            f.close();*/
            
            // El siguiente codigo es temporal hasta que se puedan
            // almacenar y leer de un fichero los campos referentes
            // a las caracterï¿½sticas visuales de los nodos
            for (int i=0; i<netToLoad.getNodeList().size(); i++) {
                Node n = netToLoad.getNodeList().elementAt(i);
                n.setFont("Helvetica");
                FontMetrics fm=getFontMetrics(ElviraPanel.getFont(n.getFont()));
                VisualNode.setAxis(n,n.getNodeString(byTitleItem.isSelected()),fm);
            }
        }
        catch (IOException e) {
            Elvira.println("Exception: " + e +"\n");
            Elvira.println("\tFile not loaded correctly.\n\n");
            throw new IOException();
        }
        catch (ParseException e) {
            try {
                FileInputStream f = new FileInputStream(nameOfFile);
                //DataBaseCases dbcases = new DataBaseCases(f);
                //netToLoad = (Bnet) dbcases;
                netToLoad = new Bnet(f);
            }
            catch (ParseException ex) {
                Elvira.println("Parse error: " + ex + "\n");
                Elvira.println("\tFile not loaded correctly.\n\n");
                throw new ParseException();
            }
        }
        createNewFrame(nameOfFile, false);
        if (netToLoad.getClass()==IDiagram.class){
            netToLoad=IDWithSVNodes.convertToIDWithSVNodes((IDiagram)netToLoad);
        }
        currentNetworkFrame.getEditorPanel().setBayesNet((Bnet)netToLoad);
        currentNetworkFrame.getEditorPanel().refreshElviraPanel(1.0);
        if (!netToLoad.getName().equals("")) {
            //currentNetworkFrame.setTitle(netToLoad.getName());
            currentNetworkFrame.setTitle(nameOfFile);
        }
        
        // Put the network into the graphical interface
        currentNetworkFrame.getEditorPanel().load((Bnet)netToLoad);
        //setTitle ("Elvira - " + nameOfFile);
          /*if (!netToLoad.getName().equals("")) {
            setTitle("Elvira - " + netToLoad.getName());
          }
          else {
            setTitle("Elvira - " + nameOfFile);
          }*/
    }
    
     /**
     * Do all the operations related with the opening of a file: create a new
     * pane for the file, load the network in the editorPanel, and sets the
     * name of the file in all the menuItems where it is necessary
     */
    
    public void openXbifFile(String nameOfFile) throws
    elvira.translator.xbif2elv.ParseException, ParseException, IOException{

        Network netToLoad;

        try {
            netToLoad=Network.readXbif(nameOfFile);
        
            Elvira.println("\nLoading " + nameOfFile + "\n");
        
            // El siguiente codigo es temporal hasta que se puedan
            // almacenar y leer de un fichero los campos referentes
            // a las caracteristicas visuales de los nodos
            for (int i=0; i<netToLoad.getNodeList().size(); i++) {
                Node n = netToLoad.getNodeList().elementAt(i);
                n.setFont("Helvetica");
                FontMetrics fm=getFontMetrics(ElviraPanel.getFont(n.getFont()));
                VisualNode.setAxis(n,n.getNodeString(byTitleItem.isSelected()),fm);
            }
        }
        catch (IOException e) {
            Elvira.println("Exception: " + e +"\n");
            Elvira.println("\tFile not loaded correctly.\n\n");
            throw new IOException();
        }
        
        createNewFrame(nameOfFile, false);
        
        currentNetworkFrame.getEditorPanel().setBayesNet((Bnet)netToLoad);
        currentNetworkFrame.getEditorPanel().refreshElviraPanel(1.0);
        if (!netToLoad.getName().equals("")) {
            currentNetworkFrame.setTitle(nameOfFile);
        }   
        // Put the network into the graphical interface
        currentNetworkFrame.getEditorPanel().load((Bnet)netToLoad);
 
        JOptionPane.showMessageDialog(null,"Evidence saved in "+nameOfFile+".evi", "", 1);    
    }
    
    /**
     * Open the files whose names are in the Vector given as parameter
     * at the same time. Every network open will be placed in a different
     * NetworkFrame. </P>
     * <P> Warning: When one of the file names given in the Vector does
     * not correspond to a existing file, the method stops. </P>
     */
    
    public void openVariousFiles(Vector names) {
        for (int i=0; i<names.size(); i++)
            try {
                openFile( (String) names.elementAt(i));
            } catch (Exception e) {
                System.out.println("Wrong name file");
            }
        
        JInternalFrame[] frames = desktopPane.getAllFrames();
        
        setCurrentNetworkFrame((NetworkFrame) frames[0]);
        
        if (windowMenu.getItemCount()>8) {
            enableMenusOpenNetworks(true,
            currentNetworkFrame.getEditorPanel().getBayesNet());
            enableMenusOpenFrames(true);
        }
    }
    
    
    
    public void importAction(){
        ElviraConversor conversor=new ElviraConversor(0);
    }
    
    public void exportAction(){
        ElviraConversor conversor=new ElviraConversor(1);
    }
    
    
    /**
     * This method saves the network recorded in the actual NetworkPane.
     * As name of file uses the string contained in CurrentFile. If there
     * is no string on it calls at saveAsAction. If the network is successful
     * saved, set the value of modified to False.
     *
     * @see ElviraFrame#saveAsAction
     * @return True if the file has been saved correctly
     */
    
    private Network reopenToBak(String nameOfFile) throws
    ParseException, elvira.translator.xbif2elv.ParseException,IOException{
        Network netToLoad;
        
        try {
            netToLoad=Network.read(nameOfFile);
            //     FileInputStream f;
            Elvira.println("\nLoading " + nameOfFile + "\n");
            //      f = new FileInputStream(nameOfFile);
            
                /*     BayesNetParse parser = new BayesNetParse(f);
                parser.initialize();
                 
                parser.CompilationUnit();
                if (parser.Type.equals("iDiagram"))
                netToLoad = new IDiagram();
                else
                netToLoad = new Bnet();
                netToLoad.translate(parser);
                 
                f.close();*/
            
            // El siguiente codigo es temporal hasta que se puedan
            // almacenar y leer de un fichero los campos referentes
            // a las caracterï¿½sticas visuales de los nodos
            for (int i=0; i<netToLoad.getNodeList().size(); i++) {
                Node n = netToLoad.getNodeList().elementAt(i);
                n.setFont("Helvetica");
                FontMetrics fm=getFontMetrics(ElviraPanel.getFont(n.getFont()));
                VisualNode.setAxis(n,n.getNodeString(byTitleItem.isSelected()),fm);
            }
        }
        catch (IOException e) {
            Elvira.println("Exception: " + e +"\n");
            Elvira.println("\tFile not loaded correctly.\n\n");
            throw new IOException();
        }
        catch (ParseException e) {
            try {
                FileInputStream f = new FileInputStream(nameOfFile);
                //DataBaseCases dbcases = new DataBaseCases(f);
                //netToLoad = (Bnet) dbcases;
                netToLoad = new Bnet(f);
            }
            catch (ParseException ex) {
                Elvira.println("Parse error: " + ex + "\n");
                Elvira.println("\tFile not loaded correctly.\n\n");
                throw new ParseException();
            }
        }
        
        return netToLoad;
    }
    
    public boolean saveAction() {
        
        String nameOfFile = currentNetworkFrame.getTitle();
        String nam = new String(nameOfFile);
        String nameOfBakFile = new String();
        if (!(nam.substring(nam.length()-4)).equals(".bak")) {
            nameOfBakFile = new String(nameOfFile.substring(0,nameOfFile.length()-4)+".bak");
        }
        else {
            nameOfBakFile = new String(nameOfFile+".bak");
        }
        boolean saved = true;
        
        if ((nameOfFile == null) || (nameOfFile.equals("")) ||
        (currentNetworkFrame.isNew()))
            saved = saveAsAction();
        else {
            if (currentNetworkFrame.getEditorPanel().getBayesNet() == null) {
                Elvira.println("\n No Bayesian network to be saved.\n\n");
                return true;
            }
            
            Network netToLoad = new Network();
            
            try {
                netToLoad = reopenToBak(nameOfFile);
            } catch (Exception e) {}
            
            try {
                //FileWriter fileout = new FileWriter(nameOfBakFile);
                netToLoad.save(nameOfBakFile);
                //fileout.close();
            }
            catch (IOException e) {
                Elvira.println("Exception: " + e + "\n");
            }
            
            try {
                FileWriter fileout = new FileWriter(nameOfFile);
                currentNetworkFrame.getEditorPanel().getBayesNet().saveBnet(fileout);
                fileout.close();
            }
            catch (IOException e) {
                Elvira.println("Exception: " + e + "\n");
            }
            
            Elvira.println("\tFile saved.\n\n");
            currentNetworkFrame.getEditorPanel().setModifiedNetwork(false);
        }
        
        return saved;
        
    }
    
    
    /**
     * Used when the saveFileDialog must be open for introduce
     * the name of the file to save
     *
     * @return True if the file has been saved correctly
     */
    
    public boolean saveAsAction() {
        
        String newName = null,
        oldName = currentNetworkFrame.getTitle();
        int state = 0;
        
        try {
            // saveFileDialog Show the FileDialog
            fileChooser.rescanCurrentDirectory();
            fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooser.setDialogTitle("Save File");
            fileChooser.setSelectedFile(new File(oldName));
            //fileChooser.setElviraFilter();
            state = fileChooser.showSaveDialog(null);
            newName = fileChooser.getSelectedFile().getPath();
        } catch (Exception e) {}
        
        if (fileChooser.getSelectedFile() == null || state == JFileChooser.CANCEL_OPTION)
            return false;
        
        try {
            FileWriter fileout = new FileWriter(newName);
            currentNetworkFrame.getEditorPanel().getBayesNet().saveBnet(fileout);
            fileout.close();
        }
        catch (IOException e) {
            Elvira.println("Exception: " + e + "\n");
            Elvira.println("\tFile not saved correctly.\n\n");
        }
        
        currentNetworkFrame.getEditorPanel().setModifiedNetwork(false);
        currentNetworkFrame.setTitle(newName);
        currentNetworkFrame.repaint();
        
        JMenuItem closeItem = fileMenu.getItem(fileMenu.getItemCount()-2);
        closeItem.setText(localize(menuBundle, "File.Close.label")+" "+newName);
        
        Enumeration wMenu = windowGroup.getElements();
        boolean exit = false;
        while (wMenu.hasMoreElements() && !exit) {
            JMenuItem windowItem = (JMenuItem) wMenu.nextElement();
            if (windowItem.getText().equals(oldName)) {
                windowItem.setText(newName);
                exit = true;
            }
        }
        
        //setTitle("Elvira - " + newName);
        return true;
    }
    
    
    /**
     * Close the Network editor showing a dialog to accept or cancel this
     * action. If the network in the editor has been modified, shows other
     * dialog for stores this modifications.
     *
     * @see closeNetworkPane
     */
    
    public void closeAction(PropertyChangeEvent e) throws PropertyVetoException {
        int reply;
        // Jorge-PFC 07/01/2006 si la ventana no tiene el foco (se pulsa directamente sobre el boton de cerrar
        // de la ventana, se estarï¿½ comprobando si ha cambiado la red que no es!
        // NetworkFrame frameClose = currentNetworkFrame;
        NetworkFrame frameClose = (NetworkFrame) e.getSource();
        
        //WindowMenuListener m = new WindowMenuListener();
        MenuEvent event = new MenuEvent(windowMenu);
        
        //m.menuSelected(event);
        // Beep
        Toolkit.getDefaultToolkit().beep();
        
        // Show a confirmation dialog
        
        Object[] options = { localize(dialogBundle,"Yes.label"), localize(dialogBundle,"No.label"),
        localize(dialogBundle,"Cancel.label")};
        
        // Jorge-PFC 07/01/2006 si la ventana no tiene el foco (se pulsa directamente sobre el boton de cerrar
        // de la ventana, se estarï¿½ comprobando si ha cambiado la red que no es!
        // if (currentNetworkFrame.getEditorPanel().isModifiedNetwork()) {
        //
        if(frameClose.getEditorPanel().isModifiedNetwork()) {
            Object[] names = {frameClose.getTitle()};
            reply = ShowMessages.showOptionDialogPlus(
            ShowMessages.UNSAVED_NETWORK,
            JOptionPane.QUESTION_MESSAGE,
            options, 0, names);
        }
        else
            reply = 1;
        
        switch (reply) {
            case 0: boolean saved = saveAction();
            if (saved)
                reestructWindowMenu(frameClose.getTitle());
            else
                throw new PropertyVetoException("close cancelled", e);
            break;
            case 1: reestructWindowMenu(frameClose.getTitle());
            break;
            case 2: throw new PropertyVetoException("close cancelled", e);
        }
        
        if (!(frameClose.getTitle().startsWith("Untitled")) &&
        !(frameClose.getTitle().equals(localize(menuBundle, "MessageWindow.label"))))
            insertLastReference(frameClose.getTitle(),false);
    }
    
    /**
     * Reabre una red.
     * @param b Bnet
     * @param nameOfFile String Nombre del archivo
     * @param messages boolean ï¿½Mostrar mensajes?
     * @author jruiz
     */
    public void reopenNetwork(Bnet b,String nameOfFile,boolean messages) {

      int reply = 0;
      Object[] options = { localize(dialogBundle,"Yes.label"), localize(dialogBundle,"No.label"),
                           localize(dialogBundle,"Cancel.label")};
        
      Object[] names = {nameOfFile};

        reply = ShowMessages.showOptionDialogPlus(
          ShowMessages.UNSAVED_NETWORK,
          JOptionPane.QUESTION_MESSAGE,
          options, 0, names);

       if(reply==0) {
         try {
	     currentNetworkFrame.getEditorPanel().setModifiedNetwork(messages);
           if(messages) {
             saveAction();
             nameOfFile = currentNetworkFrame.getTitle();
           } else {
             b.save(nameOfFile);
           }
         } catch (Exception e) {}

         JInternalFrame[] frames = desktopPane.getAllFrames();
        
         int frIndex = -1;
        
         for (int i=0; i<frames.length; i++) {
           if (frames[i].getTitle() == nameOfFile) {
             frIndex = i;
             try{
               frames[frIndex].setClosed(true);
               frames[frIndex].dispose();
             } catch (Exception e){}
             break;
           }
         }
      
         try {
           openFile(nameOfFile);
         } catch (Exception e){}
                
         if ((b.getClass()==IDiagram.class)||(b.getClass()==IDWithSVNodes.class)||(b.getClass()==UID.class)) {
           showIDiagramOptions();
         }
         else {
           hideIDiagramOptions();
         }
      }

    }
    
    /**
     * Take the necessary actions when a FrameNetwork is closed. The
     * mainly action is reestruct the window menu, because is here
     * where appears the name of the window closed
     */
    
    public void reestructWindowMenu(String windowToClose) {
        
        String closeString = localize(menuBundle, "File.Close.label");
        
           /* First: looking for the windowMenuItem corresponding to
              the window closed for remove it */
        
        boolean removed = false;
        int i = 8;  // 7 is the first position of a JCheckBoxMenuItem in the menu
        Enumeration buttons = windowGroup.getElements();
        
        while (!removed && i<windowMenu.getItemCount()) {
            if (windowMenu.getItem(i).getText().equals(windowToClose)) {
                windowGroup.remove(windowMenu.getItem(i));
                windowMenu.remove(i);
                removed=true;
            }
            else
                i++;
        }
        
           /* if we have removed the last JCheckBoxMenuItem we removed
              the separator too */
        
        if (windowMenu.getItemCount()==8)
            windowMenu.remove(7);
        
        /* Now we look for the new object that must be appear selected */
        
        if (windowMenu.getItemCount()>8) {
            int lastItemIndex = windowMenu.getItemCount()-1;
            JMenuItem lastItem = windowMenu.getItem(lastItemIndex);
            JInternalFrame[] frames = desktopPane.getAllFrames();
            lastItem.setSelected(true);
            boolean selected = false;
            i=frames.length-1;
            
            while (!selected && i>=0) {
                if (lastItem.getText().equals(frames[i].getTitle())) {
                	// --> Jorge-PFC 27/12/2005
                	if( frames[i] instanceof NetworkFrame ) {
                        currentNetworkFrame = (NetworkFrame) frames[i];
                	}
                    else {
                        currentNetworkFrame = null;
                    }
                	// <-- Jorge-PFC 27/12/2005
                	
                    selected = true;
                }
                i--;
            }
            
            if (selected==false)
                closeItem.setText(closeString);
            else
                closeItem.setText(closeString+" "+lastItem.getText());
        }
        else
            closeItem.setText(closeString);
        
    }
    
    
    public void appendText(String text) {
        messageWindow.messageArea.append(text+"\n");
    }
    
    /**
     * Returns the active frame into the ElviraFrame
     * @return the network if the active frame is a NetworkFrame, null otherwise
     * 
     * DONE: Jorge-PFC 27/12/2005,  Provocaba una excepcion si esta es la ultima ventana q se cerraba
     * Jorge-PFC 27/12/2005 only returns the active frame if it's a NetworkFrame
     */
    public JInternalFrame getCurrentNetworkFrame() {
    	JInternalFrame frame= desktopPane.getSelectedFrame();
        
    	return frame instanceof NetworkFrame ? frame : null;
    }
    
    public void setCurrentNetworkFrame(NetworkFrame frame) {
        currentNetworkFrame = frame;
        
        String Preci = currentNetworkFrame.getEditorPanel().getBayesNet().getVisualPrecision();
        LastPrecItem.setSelected(false);
        
        // --> Jorge-PFC 27/12/2005
    	for( int i=0; i< setPrecisionItem.getItemCount(); i++ ) {
    		JCheckBoxMenuItem item= (JCheckBoxMenuItem) setPrecisionItem.getItem(i);
    		String txt= item.getText().replace('1','0');
    		
    		if(txt.equals(Preci)) {
    			LastPrecItem= item;
    			break;
    		}
    	}
        
    	// DONE: almacenar en el estado interno del DecisionTreeFrame (si procede)
        LastPrecItem.setSelected(true);
        // <-- Jorge-PFC 27/12/2005
        
        if (currentNetworkFrame.getMode() == NetworkFrame.EDITOR_ACTIVE) {
            if (workingMode.getSelectedIndex()==1)
                activeMode(NetworkFrame.EDITOR_ACTIVE);
        }
        else
            if (workingMode.getSelectedIndex()==0)
                activeMode(NetworkFrame.INFERENCE_ACTIVE);
        
        activeSelect();
        activeViewOption();
    }
    
    
    public void enableUndo(boolean value) {
        undoItem.setEnabled(value);
        undoButton.setEnabled(value);
    }
    
    public void enableRedo(boolean value) {
        redoItem.setEnabled(value);
        redoButton.setEnabled(value);
    }
    
    public void enableCutCopy(boolean value) {
        cutItem.setEnabled(value);
        cutButton.setEnabled(value);
        copyItem.setEnabled(value);
        copyButton.setEnabled(value);
        deleteItem.setEnabled(value);
    }
    
    public void enablePaste(boolean value) {
        pasteItem.setEnabled(value);
        pasteButton.setEnabled(value);
        if (currentNetworkFrame!=null)
            currentNetworkFrame.getEditorPanel().
            pasteMenuItem.setEnabled(value);
    }
    
    public void enableExpand(boolean value) {
        expandItem.setEnabled(value);
        expandButton.setEnabled(value);
    }
    
    
    public void enableStoreCase(boolean value) {
        storeCaseItem.setEnabled(value);
        storeCaseButton.setEnabled(value);
    }
    
    public void enableInference(boolean value) {
        enableExpand(value);
        explainItem.setEnabled(value);
    }
    
    public void enablePropagation(boolean value) {
        propagateButton.setEnabled(value);
        propagateItem.setEnabled(value);
    }
    
    public void enableConstraints(boolean value) {
        constraintsButton.setEnabled(value);
        constraintsItem.setEnabled(value);
    }
    
    public void enableExpandDecTree(boolean value) {
    	// Jorge-PFC 26/12/2005
        dectreeButton.setEnabled(value);
        /*dectreeItem.setEnabled(value);*/
    }
    
    public void enableToolbars(boolean value) {
        toolbarPanel.setVisible(value);
        standardToolbar.setVisible(value);
        editionToolbar.setVisible(value);
    }
    
    public void enableMenus(boolean value) {
        menuBar.setVisible(value);
    }
    
    public void enablePathsButton(){
        System.out.println("Paths"+currentNetworkFrame.getInferencePanel().PATHS);
        if (currentNetworkFrame.getInferencePanel().PATHS)
            pathsButton.setEnabled(true);
        else pathsButton.setEnabled(false);
    }
    
    public void enableExplanationOptions(boolean value) {
        functionButton.setEnabled(value);
        saveCaseItem.setEnabled(value);
        saveCaseButton.setEnabled(value);
        storeCaseButton.setEnabled(value);
        storeCaseItem.setEnabled(value);
        firstButton.setEnabled(value);
        firstCaseItem.setEnabled(value);
        previousButton.setEnabled(value);
        previousCaseItem.setEnabled(value);
        nextButton.setEnabled(value);
        nextCaseItem.setEnabled(value);
        lastButton.setEnabled(value);
        explainCaseButton.setEnabled(value);
        lastCaseItem.setEnabled(value);
        deleteButton.setEnabled(value);
        emptyCasesButton.setEnabled(value);
        editorButton.setEnabled(value);
        caseEditorItem.setEnabled(value);
        caseButton.setEnabled(value);
        caseMonitorItem.setEnabled(value);
        explainItem.setEnabled(value);
        optionsButton.setEnabled(value);
        optionsItem.setEnabled(value);
        //Introducido por Alberto Ruiz        
        sensitivityOneAnalysisButton.setEnabled(value);
        analysisOneParameterItem.setEnabled(value);
        //Fin introducido por Alberto Ruiz
        
        if (currentNetworkFrame.getInferencePanel().AUTOPROPAGATION)
            enablePropagation(false);
        else enablePropagation(true);
        enablePathsButton();
    }
    
    
    /**
     * Remove and hide the buttons and menu items that are not
     * necessary when the active network is not a influence
     * diagram
     */
    
    public void hideIDiagramOptions() {
        utilityNodeButton.setEnabled(false);
        decisionNodeButton.setEnabled(false);
        utilityItem.setEnabled(false);
        decisionItem.setEnabled(false);
        influenceItem.setEnabled(true);
        influencesLinkButton.setEnabled(true);
        enableExpandDecTree(false);
        enableConstraints(false);
        generateDBCItem.setEnabled(true);
        inferenceOptionsItem.setEnabled(true);
        explanationOptionsItem.setEnabled(true);

        //Introducido por jruiz
        sensitivityAnalysisItem.setEnabled(false);
        sensitivityAnalysisButton.setEnabled(false);
        //Fin introducido por jruiz
    }
    
    
    /**
     * Add and show the buttons and menu items that only can
     * be used when it is working with influence diagrams or with
     * influence diagrams with super value nodes
     */
    
    public void showIDiagramOptions() {
        utilityNodeButton.setEnabled(true);
        decisionNodeButton.setEnabled(true);
        utilityItem.setEnabled(true);
        decisionItem.setEnabled(true);
        influenceItem.setEnabled(true);
        influencesLinkButton.setEnabled(true);
        continuousItem.setEnabled(false);
        continuousNodeButton.setEnabled(false);

        // Jorge-PFC 26/12/2005
        enableExpandDecTree(true);

        enableConstraints(true);
        generateDBCItem.setEnabled(true);
        inferenceOptionsItem.setEnabled(true);
        explanationOptionsItem.setEnabled(true);
        //Introducido por Alberto Ruiz
	       sensitivityOneAnalysisButton.setEnabled(false);
	       analysisOneParameterItem.setEnabled(false);
        
       //Introducido por jruiz
       sensitivityAnalysisItem.setEnabled(true);
       sensitivityAnalysisButton.setEnabled(true);
       //Fin introducido por jruiz
    }
    
    
 
    boolean askWhetherAddATerminalValueNode(IDWithSVNodes id){
    	boolean propagate;
    	String msg;
    	
		
    	msg = ShowMessages.ADD_TERMINAL_VALUE_NODE;
    	
    	int reply;
    	
    	Object[] options = {localize(dialogBundle,"Yes.label"),localize(dialogBundle,"No.label")};
    	
    	reply = ShowMessages.showOptionDialog(msg,JOptionPane.QUESTION_MESSAGE,options,0);
    	
    	if (reply==0){ //The answer is YES
    		Node newNode;
    		propagate = true;
    		newNode = id.addATerminalSuperValueNode();
    		setVisualPropertiesNewSVNode(newNode);
    		this.getCurrentEditorPanel().setModifiedNetwork(true);
    		
    	}
    	else{//The answer is NO
    		propagate = false;
    	}
    	
    	return propagate;
    }
    
    
    private void setVisualPropertiesNewSVNode(Node newNode) {
		// TODO Auto-generated method stub
    
    	FontMetrics fm=getFontMetrics(ElviraPanel.getFont(newNode.getFont()));
	      VisualNode.setAxis(newNode,newNode.getNodeString(getCurrentEditorPanel().byTitle),fm);
	}


	public void setExpandName(int expandMode) {
        if (expandMode == InferencePanel.EXPANDED) {
            expandItem.setText(localize(menuBundle,"Explanation.Expand.label"));
            expandItem.setMnemonic(
            localize(menuBundle,"Explanation.Expand.mnemonic").charAt(0));
            expandButton.setToolTipText(
            localize(menuBundle,"Explanation.Expand.tip"));
        }
        else {
            expandItem.setText(localize(menuBundle,"Explanation.Contract.label"));
            expandItem.setMnemonic(
            localize(menuBundle,"Explanation.Contract.mnemonic").charAt(0));
            expandButton.setToolTipText(
            localize(menuBundle,"Explanation.Contract.tip"));
        }
    }
    
    public void setInitialPrecision(NetworkFrame NF) {
        if (NF == null)
            return;
        
        String PR = currentNetworkFrame.getEditorPanel().getBayesNet().getVisualPrecision();
        LastPrecItem.setSelected(false);
        
        // --> Jorge-PFC 27/12/2005
    	for( int i=0; i< setPrecisionItem.getItemCount(); i++ ) {
    		JCheckBoxMenuItem item= (JCheckBoxMenuItem) setPrecisionItem.getItem(i);
    		String txt= item.getText().replace('1','0');
    		
    		if(txt.equals(PR)) {
    			LastPrecItem= item;
    			break;
    		}
    	}
        
        LastPrecItem.setSelected(true);
        // <-- Jorge-PFC 27/12/2005
    }
    
    public void setExplainName(int explainMode) {
        if (explainMode == InferencePanel.EXPLAIN_NODE_MODE) {
            explainItem.setText(localize(menuBundle,"Explanation.ExpNode.label"));
            explainItem.setActionCommand("Explain Node");
            explainItem.setEnabled(true);
        }
        else if (explainMode == InferencePanel.EXPLAIN_LINK_MODE) {
            explainItem.setText(localize(menuBundle,"Explanation.ExpLink.label"));
            explainItem.setActionCommand("Explain Link");
            explainItem.setEnabled(true);
        }
        else {
            explainItem.setText(localize(menuBundle,"Explanation.Explain.label"));
            explainItem.setEnabled(false);
        }
        
    }
    
    
    public void setZoom(double zoom) {
        zoomComboBox.setSelectedItem(
        (Object) String.valueOf(Math.round(zoom*100))+"%");
    }
    
    
    /**
     * Set the correct option of the view menu for the
     * NetworkFrame that is active. This method will be used
     * when the currentNetworkFrame is changed
     */
    
    public void activeViewOption(){
        if (currentNetworkFrame.getEditorPanel().getByTitle())
            byTitleItem.setSelected(true);
        else
            byNameItem.setSelected(true);
    }
    
    
    /**
     * This method disables/enables all the menu items and toolbar buttons that
     * can/cannot be used when there is/isn't networks opened.
     */
    
    public void enableMenusOpenNetworks(boolean value, Bnet bnet) {
        saveItem.setEnabled(value);
        saveAsItem.setEnabled(value);
        saveAllItem.setEnabled(value);
        saveAndReopenItem.setEnabled(value);
        
        loadEvidenceItem.setEnabled(value);
        saveEvidenceItem.setEnabled(value);
  
        viewMenu.setEnabled(value);
        optionsMenu.setEnabled(value);
        if (currentNetworkFrame!=null) {
        	// --> Jorge-PFC 26/12/2005
        	if (currentNetworkFrame.mode!=NetworkFrame.INFERENCE_ACTIVE) {
        		editionToolbar.setVisible(true);
        		explanationToolbar.setVisible(false);
        	}
        	else {
        		editionToolbar.setVisible(false);
        		explanationToolbar.setVisible(true);        		
        	}
        	// <-- Jorge-PFC 26/12/2005
        	
            inferenceOptionsItem.setEnabled(true);
            explanationOptionsItem.setEnabled(true);
        }
        
        tasksMenu.setEnabled(value);
        
        //Introducido por jruiz
        sensitivityAnalysisItem.setEnabled(value);
        //Fin introducido por jruiz

        selectAllItem.setEnabled(value);
        selectItem.setEnabled(value);
        chanceItem.setEnabled(value);
        continuousItem.setEnabled(value);
        linkItem.setEnabled(value);
        if (currentNetworkFrame!=null) {
            if (currentNetworkFrame.getEditorPanel().getUndoManager().canRedo())
                enableRedo(value);
            if (currentNetworkFrame.getEditorPanel().getUndoManager().canUndo())
                enableUndo(value);
        }
        else {
            enableRedo(false);
            enableUndo(false);
        }
        
        saveButton.setEnabled(value);
        selectButton.setEnabled(value);
        chanceNodeButton.setEnabled(value);
        observedNodeButton.setEnabled(value);
        continuousNodeButton.setEnabled(value);
        directedLinkButton.setEnabled(value);
        zoomInButton.setEnabled(value);
        zoomOutButton.setEnabled(value);
        
        workingMode.setEnabled(value);
        zoomComboBox.setEnabled(value);
        
        optionsButton.setEnabled(value);
        influenceLinkButton.setEnabled(value);
        layoutButton.setEnabled(value);
        if (bnet == null) {
            hideIDiagramOptions();
            hideDanOptions();
            influenceLinkButton.setEnabled(value);
        }
        else{
        	influenceLinkButton.setEnabled(true);
        	
            Class<? extends Bnet> bnetClass = bnet.getClass();
			if ((bnetClass==IDiagram.class)||(bnet.getClass()==IDWithSVNodes.class)) {
                showIDiagramOptions();
            }
            else if (bnetClass==UID.class){
            	showIDiagramOptions();
            }
            else {
                hideIDiagramOptions();
                if (bnet.getClass()==Dan.class){
                	showDanOptions();
                }
                else{
                	hideDanOptions();
                }
            }
        }
    }
    
    
    private void hideDanOptions() {
		// TODO Auto-generated method stub
    	   utilityNodeButton.setEnabled(false);
           decisionNodeButton.setEnabled(false);
           observedNodeButton.setEnabled(false);
           utilityItem.setEnabled(false);
           decisionItem.setEnabled(false);
           influenceItem.setEnabled(true);
           influencesLinkButton.setEnabled(true);
           
           
           enableExpandDecTree(false);
           enableConstraints(false);
           generateDBCItem.setEnabled(true);
           inferenceOptionsItem.setEnabled(true);
           explanationOptionsItem.setEnabled(true);

           //Introducido por jruiz
           sensitivityAnalysisItem.setEnabled(false);
           sensitivityAnalysisButton.setEnabled(false);
           //Fin introducido por jruiz
	}


	public ButtonGroup getWindowGroup() {
        return windowGroup;
    }
    
    public MessageFrame getMessageWindow() {
        return messageWindow;
    }
    
    
    /**
     * This method disables/enables all the menu items and toolbar buttons that
     * can/cannot be used when there is/isn't frames opened.
     */
    
    public void enableMenusOpenFrames(boolean value) {
        
        closeItem.setEnabled(value);
        
        cascadeItem.setEnabled(value);
        minimizeAllItem.setEnabled(value);
        restoreAllItem.setEnabled(value);
        
        previousItem.setEnabled(value);
        nextItem.setEnabled(value);
        
    }
    
    
    
    /* ** Methods to work with the last references files ** */
    
    /**
     * Inserts in the file menu the name of the last file closed
     *
     * @param fileName The name of the file to insert in the menu
     */
    
    public void insertLastReference(String fileName, boolean isNew) {
        int elements, position;
        String menuItemName;
        
        // The file had a name, so we add to the list of last files
        // used
        if (fileMenu.getItemCount()==17) {
            
            // There isn't any name in the list of last reference files, so
            // add two separators for the list
            fileMenu.insertSeparator(11);
        }
        else {
            
            // The list has 4 elements, so we have to remove the first
            if (fileMenu.getItemCount()==19)
                fileMenu.remove(17);
        }
        
        if (!isNew) {
            elements = fileMenu.getItemCount()-17;
            menuItemName = "1. "+fileName;
            position = 12;
        }
        else {
            elements = fileMenu.getItemCount()-17;
            menuItemName = Integer.toString(elements)+". "+fileName;
            position = elements+11;
        }
        
        JMenuItem m = new JMenuItem(menuItemName);
        fileMenu.insert(m, position);
        m.addActionListener(new LastReferenceListener(this));
        
        // Reorganize the lasts reference elements
        if (!isNew) {
            if (elements==4)
                elements--;
            for (int i=1; i<=elements; i++) {
                JMenuItem item = fileMenu.getItem(i+11);
                String s = item.getText(), s1 = Integer.toString(i+1);
                s = s.substring(1);
                s = s1.concat(s);
                item.setText(s);
            }
        }
    }
    
    
    
    
    /**
     * Saves in a file called "elvira.lst" the list of the last references.
     * This method is called when the option exit is clicked
     */
    
    public void saveLastReferences() {
        
        FileWriter f;
        PrintWriter p;
        int numberFiles,i;
        
        try {
            f = new FileWriter("elvira.lst");
            p = new PrintWriter(f);
            
            // 17 is the number of elements in the file menu that are not
            // name of files to save
            
            numberFiles=fileMenu.getItemCount()-17;
            
            p.print(Integer.toString(numberFiles)+"\n");
            
            for (i=0; i<numberFiles; i++) {
                p.print(fileMenu.getItem(i+12).getText().substring(3)+"\n");
            }
            
            f.close();
        }
        catch (IOException e) {  // There is a problem the list of last reference
            // files is not created
        }
    }
    
    
    
    /**
     * Load the last references to a files from "elvira.lst". This method is
     * called when the menu constructor is called
     */
    
    public void loadLastReferences(ElviraFrame frame) {
        FileReader f;
        LineNumberReader ln;
        String line;
        char car;
        int numberFiles,i;
        
        try {
            f = new FileReader("elvira.lst");
            ln = new LineNumberReader(f);
            
         /* 17 is the number of elements of the file menu that are
          * always shown */
            
            line = ln.readLine();
            car = line.charAt(0);
            numberFiles = Character.digit(car, 10);
            
            for (i=0; i<numberFiles; i++) {
                insertLastReference(ln.readLine(),true);
            }
            
            f.close();
        }
        catch (IOException e) {  // There is a problem the list of last reference
            // files is not created
        }
    }
    
    
    /**
     * Method for get the string from the resource bundle
     *
     * @param bundle Where the string is
     * @param name Variable in the bundle that contains the string
     *             that this method returns
     */
    
    public static String localize(ResourceBundle bundle, String name) {
        return Elvira.localize(bundle, name);
    }
    
    
    public boolean unselectAllComponents() {
        
        if (menuBar.isSelected()) {
            emptyMenu.doClick();
            menuBar.setSelected(null);
            currentNetworkFrame.getEditorPanel().unSelectAll();
            return true;
        }
        
        if (workingMode.isPopupVisible()) {
            workingMode.processKeyEvent(new KeyEvent(workingMode,0,
            0,0,KeyEvent.VK_TAB));
            //mluque: To avoid a node is moved after it's selected the working combo box.
            currentNetworkFrame.getEditorPanel().unSelectAll();
            currentNetworkFrame.getInferencePanel().unSelectAll();
            return true;
        }
        
        if (zoomComboBox.isPopupVisible()) {
            zoomComboBox.processKeyEvent(new KeyEvent(workingMode,0,
            0,0,KeyEvent.VK_TAB));
            return true;
        }
        
        return false;
        
    }
    
    
    
    /* *********************************************************** */
    /* ***************** AUXILIARY CLASSES *********************** */
    /* *********************************************************** */
    
    /**
     * This class manage the event produce in the close icon of
     * the main window of Elvira
     * @see ElviraFrame#exitApplication
     *
     * @author ..., fjdiez, ratienza, ...
     * @version 0.1
     * @since 18/10/99
     */
    
    class ElviraWindowAdapter extends WindowAdapter {
      public void windowClosing(java.awt.event.WindowEvent event) {
            Object object = event.getSource();
            if (object == ElviraFrame.this)
                try {
                    exitApplication();
                }
                catch (Exception e) {}
            
        }
    }
    
    
    /* ******************************************************************* */
    
    
    /**
     * The next class is implemented to set all the operations
     * that will be executed when a Elvira's menu or Elvira's toolbar button
     * is clicked
     *
     * @author ..., fjdiez, ratienza, ...
     * @version 0.1
     * @since 18/10/99
     */
    
    public class ElviraAction implements ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (object == openItem || object == openButton)
                openItem_actionPerformed(event);
            else if(object == openDBCItem || object == openDBCButton)
                openDBCItem_actionPerformed(event);
            else if(object == importItem)
                importItem_actionPerformed(event);
            else if(object == exportItem)
                exportItem_actionPerformed(event);
            else if (object == saveItem || object == saveButton)
                saveItem_actionPerformed(event);
            else if (object == exitItem)
                exitItem_actionPerformed(event);
            else if (object == aboutItem || object == aboutButton)
                aboutItem_actionPerformed(event);
            else if (object == cascadeItem)
                cascadeItem_actionPerformed(event);
            else if (object == newItem || object == newButton)
                newItem_actionPerformed(event);
            else if (object == minimizeAllItem)
                minimizeAllItem_actionPerformed(event);
            else if (object == restoreAllItem)
                restoreAllItem_actionPerformed(event);
            else if (object == openURLItem)
                openURLItem_actionPerformed(event);
            else if (object == previousItem)
                previousItem_actionPerformed(event);
            else if (object == nextItem)
                nextItem_actionPerformed(event);
            else if (object == chanceItem || object == chanceNodeButton)
                addChanceNodeAction(event);
            else if (object == continuousItem || object == continuousNodeButton)
                addContinuousNodeAction(event);
            else if (object == saveAsItem)
                saveAsItem_actionPerformed(event);
            else if (object == saveAndReopenItem)
                saveAndReopenItem_actionPerformed(event);
            else if (object == closeItem)
                closeItem_actionPerformed(event);
            else if (object == linkItem || object == directedLinkButton)
                addDirectedLinkAction(event);
            else if (object == constraintsItem || object == constraintsButton)
                constraints_actionPerformed(event);
            else if (object == influenceLinkButton || object == influenceItem)
                showInfluencesEdition();
            else if (object == influencesLinkButton)
                showInfluencesInference();
            else if (object == byTitleItem)
                byTitleAction(event);
            else if (object == byNameItem)
                byNameAction(event);
            else if (object == setPrecisionItem)
                setPrecision(event);
            else if (object == Prec1Item)
                PrecItem(event, object);
            else if (object == Prec2Item)
                PrecItem(event, object);
            else if (object == Prec3Item)
                PrecItem(event, object);
            else if (object == Prec4Item)
                PrecItem(event, object);
            else if (object == Prec5Item)
                PrecItem(event, object);
            else if (object == Prec6Item)
                PrecItem(event, object);
            else if (object == Prec7Item)
                PrecItem(event, object);
            else if (object == Prec8Item)
                PrecItem(event, object);
            else if (object == showMessageWindowItem)
                showMessageWindowItem_actionPerformed(event);
            else if (object == selectButton || object == selectItem)
                selectAction(event);
            else if (object == undoItem || object == undoButton)
                undoAction(event);
            else if (object == redoItem || object == redoButton)
                redoAction(event);
            else if (object == zoomInButton)
                zoomInAction(event);
            else if (object == zoomOutButton)
                zoomOutAction(event);
            else if (object == decisionItem || object == decisionNodeButton)
                addDecisionNodeAction(event);
            else if (object == utilityItem || object == utilityNodeButton)
                addUtilityNodeAction(event);
            else if (object == deleteItem)
                deleteAction(event);
            else if (object == loadEvidenceItem)
                loadEvidenceAction(event);
            else if (object == saveEvidenceItem)
                saveEvidenceAction(event);
            else if (object == importBnetXbifItem)
                importBnetXbifAction(event);
            else if (object == exportBnetXbifItem)
                exportBnetXbifAction(event);
            else if (object == cutItem || object == cutButton)
                cutAction(event);
            else if (object == pasteItem || object == pasteButton)
                pasteAction(event);
            else if (object == copyItem || object == copyButton)
                copyAction(event);
            else if (object == expandItem || object == expandButton)
                expandAction(event);
            else if (object == explainItem)
                explainAction(event);
            else if (object == storeCaseItem || object == storeCaseButton)
                storeCaseAction(event);
            else if (object == saveCaseItem ||object == saveCaseButton)
                saveCaseAction(event);
            else if (object == caseMonitorItem || object == caseButton)
                caseMonitorAction(event);
            else if (object == caseEditorItem || object == editorButton)
                caseEditorAction(event);
            else if (object == propagateItem || object == propagateButton)
                propagateAction(event);
                   /*else if (object == dectreeItem || object == dectreeButton)
                      dectreeAction (event);*/
            else if (object == explainCaseButton)
                explainCaseAction(event);
            else if (object == pathsButton)
                pathsAction(event);
            else if (object == selectAllItem)
                selectAllAction(event);
            else if (object == thresholdComboBox)
                expandNodes(event);
            else if (object == functionButton)
                selectNodestoExpand(event);
            else if (object == fusionItem)
                fusionMethod(event);

           //Introducido por jruiz
           else if (object == sensitivityAnalysisItem ||
                    object == sensitivityAnalysisButton)
                sensitivityAnalysisMethod(event);
           //Fin introducido por jruiz
            //Introducido por Alberto Ruiz
           else if (object == analysisOneParameterItem ||
                   object == sensitivityOneAnalysisButton)
               sensitivityOneAnalysisMethod(event);
            //Fin introducido por Alberto Ruiz

            else if (object == propagationMethodItem)
                propagationMethod(event);
            else if (object == generateDBCItem)
                generateDBCMethod(event);
            else if (object == inferenceOptionsItem)
                inferenceOptions(event);
            else if (object == explanationOptionsItem)
                explanationOptions(event);
            else if (object == firstCaseItem || object ==firstButton)
                firstCaseAction(event);
            else if (object == nextCaseItem || object ==nextButton)
                nextCaseAction(event);
            else if (object == previousCaseItem || object ==previousButton)
                previousCaseAction(event);
            else if (object == lastCaseItem || object ==lastButton)
                lastCaseAction(event);
            else if (object == optionsItem || object ==optionsButton)
                optionsAction(event);
            else if (object == deleteButton)
                deleteEviAction(event);
            else if (object == emptyCasesButton)
                emptyCasesAction(event);
            else if (object == returnButton)
                returnButtonAction(event);
            else if (object == layoutButton)
            	layoutNetwork(event);
        }

		
    }
    
    
    /**
     * This class controls all the events produced in the network menu.
     *
     * @author ..., fjdiez, ratienza, ...
     * @version 0.1
     * @since 18/10/99
     */
    
    public class WindowMenuItemListener implements ItemListener {
        
        /**
         * <P>Select and unselect the InternalFrames of Elvira according to the
         * item change its state. </P>
         * <P>This method looks first if the window that
         * must appear selected is the message window, because it has a special
         * treatment. If this not happen, the method look for the networkWindow
         * that must appear selected.</P>
         *
         */
        
        public void itemStateChanged(ItemEvent event) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getItemSelectable();
            JInternalFrame[] networkFrameList = desktopPane.getAllFrames();
            int i=0, networkSelectedIndex=0;
            String closeString = localize(menuBundle, "File.Close.label");
            String messageWindowString = localize(menuBundle, "MessageWindow.label");
            
            if (event.getStateChange() == event.DESELECTED)
                return;
            
            if (item.getText().equals(messageWindowString)) {
                currentNetworkFrame = null;
                if (!messageWindow.isSelected()) {
                    try {
                        messageWindow.setSelected(true);
                        closeItem.setText(closeString+messageWindowString);
                    }
                    catch(PropertyVetoException e) {}
                }
            }
            else {
                
                while (i<networkFrameList.length) {
                    if (networkFrameList[i].getTitle().equals(item.getText()))
                        networkSelectedIndex = i;
                    else
                        try {
                            if (networkFrameList[i].isSelected())
                                networkFrameList[i].setSelected(false);
                        }
                        catch (PropertyVetoException e) {System.out.println("You can't select that frame");}
                    i++;
                }

                // --> Jorge-PFC 27/12/2005
                if( networkFrameList[networkSelectedIndex] instanceof DecisionTreeFrame ) {
                    currentNetworkFrame = null;
                    
                    if (!networkFrameList[networkSelectedIndex].isSelected()) {
                        try {
                        	networkFrameList[networkSelectedIndex].setSelected(true);
                            closeItem.setText(closeString+networkFrameList[networkSelectedIndex].getTitle());
                        }
                        catch(PropertyVetoException e) {}
                    }
                	
                    return;
                }
                // <-- Jorge-PFC 27/12/2005
                
                
                if( networkFrameList[networkSelectedIndex] instanceof PolicyTreeFrame ) {
                    currentNetworkFrame = null;
                    
                    if (!networkFrameList[networkSelectedIndex].isSelected()) {
                        try {
                        	networkFrameList[networkSelectedIndex].setSelected(true);
                            closeItem.setText(closeString+networkFrameList[networkSelectedIndex].getTitle());
                        }
                        catch(PropertyVetoException e) {}
                    }
                	
                    return;
                }
                // 
                
                try {
                    currentNetworkFrame = (NetworkFrame) networkFrameList[networkSelectedIndex];
                    currentNetworkFrame.setSelected(true);
                    currentNetworkFrame.setMaximum(true);
                    //setTitle("Elvira - "+currentNetworkFrame.getEditorPanel().getBayesNet().getName());
                    closeItem.setText(closeString+" "+currentNetworkFrame.getTitle());
                }
                catch (PropertyVetoException e) {System.out.println("You can't select that frame");}
                
            }
        }
        
    } // end of WindowMenuItemListener class
    
    
    
    /**
     * This class' objects are created when a new item is added to
     * the File Menu as a last reference item (remember that the last
     * reference list contains the last files that had been opened in
     * Elvira).
     *
     * @author ..., fjdiez, ratienza, ...
     * @version 0.1
     * @since 18/10/99
     */
    
    public class LastReferenceListener implements ActionListener {
        ElviraFrame frame;
        
        public LastReferenceListener(ElviraFrame f){
            frame=f;
        }
        
        public void actionPerformed(ActionEvent e) {
            JMenuItem option= (JMenuItem) e.getSource();
            String name = option.getText();
            
            // Every item in this list has the estructure      <Number>. <Name of file>
            // The first thing to do is remove the <Number>.
            name = name.substring(3);
            
            try {
                frame.openFile(name);
            }
            catch (Exception evt) {};
            
            if (windowMenu.getItemCount()>8) {
                enableMenusOpenNetworks(true,
                currentNetworkFrame.getEditorPanel().getBayesNet());
                enableMenusOpenFrames(true);
            }
            
            
            // if the list only have this element we have to remove the Menu's separators
            if (frame.fileMenu.getItemCount()==17) {
                frame.fileMenu.remove(10);
                frame.fileMenu.remove(11);
            }
            frame.fileMenu.remove(option);
        }
        
    } // end of LastReferenceListener class
    
    
    /**
     * This class manage the events produced in the workingModeComboBox.
     * The main action for this ComboBox it is when a new value
     * is selected and the currentNetworkFrame must change its work
     * mode.
     */
    
    class WokingModeItem implements java.awt.event.ActionListener {
        
        /**
         * Manage the change of state in the valuesComboBox
         */
        
        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (object == workingMode)
                workingComboBox_actionPerformed(event);
        }
        
        
    }
    
    
    /**
     * Set the states in the valuesTable
     *
     * @see loadNewRows
     */
    
    void workingComboBox_actionPerformed(java.awt.event.ActionEvent event) {
        Bnet bnet;

        bnet=currentNetworkFrame.getEditorPanel().getBayesNet();

        
        
        
        switch (workingMode.getSelectedIndex()) {
            
            case 0:
                editionToolbar.setVisible(true);
                explanationToolbar.setVisible(false);
                currentNetworkFrame.activeEditorPanel();
                bnet=currentNetworkFrame.getEditorPanel().getBayesNet();
                inferenceOptionsItem.setEnabled(true);
                explanationOptionsItem.setEnabled(true);
                //Introducido por Alberto Ruiz
                analysisOneParameterItem.setEnabled(false);
                //Introducido por Alberto Ruiz
                
                setPrecisionItem.setEnabled(false);
                if (menuBar.getMenuCount()>5)
                    menuBar.remove(1);
                menuBar.add(editMenu,1);
                break;
            case 1: /*if (currentNetworkFrame.getEditorPanel().getBayesNet().getClass() == IDiagram.class) {
                              JOptionPane.showMessageDialog(null, "Por el momento no se pueden compilar los diagramas de influencia",
                                                            "", JOptionPane.INFORMATION_MESSAGE);
                              workingMode.setSelectedIndex(0);
                           }
                           else {*/
            	//Ask
            	boolean propagate;
            	//We check if the ID has a terminal value node and we ask to the user.
            	if (currentNetworkFrame.getEditorPanel().getBayesNet().getClass() == IDWithSVNodes.class) {
            		IDWithSVNodes id;
            		id = (IDWithSVNodes) currentNetworkFrame.getEditorPanel().getBayesNet();
            		if (id.hasOnlyOneTerminalValueNode()==false){
            			propagate = askWhetherAddATerminalValueNode(id);
            		}
            		else{
            			propagate = true;
            		}
            	}
            	else{
            		propagate = true;
            	}
            	
            	if (propagate){
            	            	
                editionToolbar.setVisible(false);
                
                
               
                explanationToolbar.setVisible(true);
                inferenceOptionsItem.setEnabled(true);
                explanationOptionsItem.setEnabled(true);
                currentNetworkFrame.activeInferencePanel();
                setPrecisionItem.setEnabled(true);
                //Explanation Options are alson enabled for IDiagram and IDWithSVNodes
                 if (!currentNetworkFrame.getInferencePanel().getBayesNet().getIsCompiled())
                    enableExplanationOptions(false);
                else enableExplanationOptions(true);
                if (menuBar.getMenuCount()>5)
                    menuBar.remove(1);
                menuBar.add(inferenceMenu,1);
                enableInference(false);
                //}
            	}
            	else{
            		workingMode.setSelectedIndex(0);
            	}
                break;
                
        }
        toolbarPanel.repaint();
        
    }
    
    



	public void layoutNetwork(ActionEvent event) {
		// TODO Auto-generated method stub
		//this.currentNetworkFrame.getEditorPanel().getBayesNet().positionNodes();
		positionNodes(this.currentNetworkFrame.getEditorPanel().getBayesNet());
		this.currentNetworkFrame.getEditorPanel().refreshElviraPanel(1.0);
		  // Put the network into the graphical interface
        currentNetworkFrame.getEditorPanel().load(this.currentNetworkFrame.getEditorPanel().getBayesNet());
	}



	private void positionNodes(Bnet bayesNet) {
		// TODO Auto-generated method stub
			ArrayList<ArrayList<Node>> levels;
			NodeList nodes;
			ArrayList<Node> auxLevel;
			
			//Obtain the levels
			nodes = bayesNet.getNodeList();
			//levels = new ArrayList<ArrayList<Node>>();
			for (int i=0;i<nodes.size();i++){
				auxLevel = bayesNet.nodesAtDepth(i);
				
				for (int j=0;j<auxLevel.size();j++){
					Node auxNode = auxLevel.get(j);
					auxNode.setPosX(50+j*100);
					auxNode.setPosY(50+i*100);
				}
			}
		}

	



	/**
     * This class manage the events produced in the workingModeComboBox.
     * The main action for this ComboBox it is when a new value
     * is selected and the currentNetworkFrame must change its work
     * mode.
     */
    
    class ZoomComboBoxItem implements java.awt.event.ActionListener {
        
        /**
         * Manage the change of state in the valuesComboBox
         */
        
        public void actionPerformed(java.awt.event.ActionEvent event) {
            Object object = event.getSource();
            if (object == zoomComboBox)
                zoomComboBoxItem_actionPerformed(event);
        }
    }
    
    
    /**
     * Set the states in the valuesTable
     *
     * @see loadNewRows
     */
    
    void zoomComboBoxItem_actionPerformed(java.awt.event.ActionEvent event) {
        JComboBox cb = (JComboBox)event.getSource();
        String newSelection = (String)cb.getSelectedItem();
        int position;
        
        if (newSelection.charAt(newSelection.length()-1)=='%')
            position = newSelection.length()-1;
        else
            position = newSelection.length();
        
        try {
            Integer zoom = new Integer(newSelection.substring(0, position));
            if (zoom.intValue()<10.0 || zoom.intValue()>500.0)
                ShowMessages.showMessageDialog(
                ShowMessages.ZOOM_NOT_IN_INTERVAL,
                JOptionPane.ERROR_MESSAGE);
            else {
            	// --> Jorge-PFC 28/12/2005
            	JInternalFrame iframe= desktopPane.getSelectedFrame();
            	if (iframe instanceof DecisionTreeFrame) {
            		// DONE: Por ahora no se refleja en el combo del zoom
            		// Actualizar en el estado interno... llamar a funcion del DecisionTreeFrame
            		double zr= zoom.intValue()*0.01;
            		((DecisionTreeFrame)iframe).setZoomFactor(zr);
            	}
            	// <-- Jorge-PFC 28/12/2005
            	
                if (currentNetworkFrame!=null) {
                    currentNetworkFrame.setZoom(zoom.intValue()*0.01);
                    currentNetworkFrame.repaintPanel(zoom.intValue()*0.01);
                }
            }
            
        }
        catch (NumberFormatException e) {
            ShowMessages.showMessageDialog(
            ShowMessages.WRONG_ZOOM,
            JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Jorge-PFC  05/01/2006, para evitar el fallo en VisualNode
     * Solo tiene sentido durante el proceso de repintado del NetworkFrame
     */
    NetworkFrame networkToPaint;
    
    /**
     * @param frame
     * 
     * Jorge-PFC 05/01/2006
     * Solo tiene sentido durante el proceso de repintado del NetworkFrame
     */
    public void setNetworkToPaint(NetworkFrame frame) {
    	networkToPaint= frame;
    }
    
    /**
     * Jorge-PFC 05/01/2006
     * Solo tiene sentido durante el proceso de repintado del NetworkFrame
     * @return network being painted
     */
    public NetworkFrame getNetworkToPaint() {
    	// Jorge-PFC 07/01/2006 Al cargar el primer diagrama, fallaria la llamada a este metodo
    	if(networkToPaint==null) {
    		return currentNetworkFrame;
    	}
    	
    	return networkToPaint;
    }
    
    /**
     * Getter sobre el atributo 'LastPrecItem', necesario para que el
     * mediator creado pueda acceder a este atributo
     * 
     * Aï¿½adido por Jorge-PFC el 05/12/2005
     * 
     * @return referencia al item del menu de la ultima precision seleccionada
     */
    public JCheckBoxMenuItem getLastPrecItem() {
    	return LastPrecItem;
    }

    /**
    * Setter sobre el atributo 'LastPrecItem', necesario para que el
    * mediator creado pueda modificar este atributo (ya que el nuevo
    * combobox de precisiones debe actualizarlo) 
    * 
    * @param item nuevo item a seleccionar
    * 
    * Aï¿½adido por Jorge-PFC el 05/12/2005
     */
    public void setLastPrecItem(JCheckBoxMenuItem item) {
    	LastPrecItem= item;
    }
    
    /**
     * Getter sobre el atributo 'setPrecItem', necesario para que el
     * mediator creado pueda acceder a este atributo
     * 
     * Aï¿½adido por Jorge-PFC el 06/12/2005
     * 
     * @return referencia al submenu de precisiones
     */
    public JMenu getSetPrecisionItem() {
    	return setPrecisionItem;
    }

    /**
     * Getter sobre la toolbar de edicion, es necesario para que el
     * mediator creado pueda acceder a esta toolbar y aï¿½ada el
     * boton/botones necesarios
     * 
     * Aï¿½adido por Jorge-PFC el 06/12/2005
     *  
     * @return la toolbar de edicion
     */
    public JToolBar getEditionToolbar() {
    	return editionToolbar;
    }
    
    /**
     * Getter sobre el atributo 'editionButtonGroup', necesario para que el
     * mediator creado pueda acceder a este atributo
     * 
     * Aï¿½adido por Jorge-PFC el 06/12/2005
     *      
     * @return el grupo de botones de edicion
     */
    public ButtonGroup getEditionButtonGroup() {
    	return editionButtonGroup;
    }

    /**
     * Getter sobre el atributo 'viewGroup', necesario para que el
     * mediator creado pueda acceder a este atributo
     * 
     * Aï¿½adido por Jorge-PFC el 08/12/2005
     *      
     * @return el grupo de botones asociado
     */
    public ButtonGroup getViewGroup() {
    	return viewGroup;
    }

    /** Getter sobre el atributo 'windowMenu', necesario para que el
     * mediator creado pueda acceder a este atributo
     * 
     * Aï¿½adido por Jorge-PFC el 27/12/2005
     * 
     * @return el menu de ventanas de elvira
     */
    public JMenu getWindowMenu() {
    	return windowMenu;
    }
    
    /**
     * Getter sobre el panel con las toolbars es necesario para que el
     * mediator creado pueda acceder al panel para poner/quitar toolbars
     * 
     * Aï¿½adido por Jorge-PFC el 06/12/2005
     *  
     * @return el panel de toolbars
     */    
    public JPanel getToolbarPanel() {
    	return toolbarPanel;
    }
    
    /**
     * Habilita las toolbars generales: utilizado cuando
     * no se quiere mostrar el arbol de decision
     * 
     * Aï¿½adido por Jorge PFC el 06/12/2005
     */
    public void disableDecisionTreeToolbars() {
    	elviraGUIMediator.getDecisionTreeToolbar().setVisible(false);
    	
    	/*
    	editionToolbar.setVisible(true);
    	explanationToolbar.setVisible(true);
    	editMenu.setEnabled(true);
    	*/
    }
    
    /**
     * Habilita las toolbars generales: utilizado cuando
     * no se quiere mostrar el arbol de política de la decision
     * 
     * Añadido por Manuel Luque 25/2/2009
     */
    public void disablePolicyTreeToolbars() {
    	elviraGUIMediatorPT.getPolicyTreeToolbar().setVisible(false);
    	
    	/*
    	editionToolbar.setVisible(true);
    	explanationToolbar.setVisible(true);
    	editMenu.setEnabled(true);
    	*/
    }
    
    /**
     * Deshabilita las toolbars generales y las opciones de
     * menu/botones que no se quieran utilizar cuando estï¿½
     * mostrandose el arbol de decision
     * 
     * Aï¿½adido por Jorge PFC el 06/12/2005
     */
    public void enableDecisionTreeToolbars(DecisionTreeFrame decisionTreeFrame) {

    	elviraGUIMediator.getDecisionTreeToolbar().setVisible(true);

    	editionToolbar.setVisible(false);
    	editMenu.setEnabled(false);
    	
    	explanationToolbar.setVisible(false);
    	
    	inferenceMenu.setEnabled(false);
    	tasksMenu.setEnabled(false);

      //Introducido por jruiz
      sensitivityAnalysisItem.setEnabled(false);
      //Fin introducido por jruiz

      //Introducido por Alberto Ruiz
      analysisOneParameterItem.setEnabled(false);
      //Introducido por Alberto Ruiz
    	optionsMenu.setEnabled(false);
    	viewMenu.setEnabled(true);
    	
    	setPrecisionItem.setEnabled(true);
    	influenceItem.setEnabled(false);
        saveItem.setEnabled(false);
        saveAsItem.setEnabled(false);
        saveAllItem.setEnabled(false);
        saveAndReopenItem.setEnabled(false);
        loadEvidenceItem.setEnabled(false);
        saveEvidenceItem.setEnabled(false);
        importBnetXbifItem.setEnabled(true);
        exportBnetXbifItem.setEnabled(true);
        // Jorge-PFC 26/12/2005
        workingMode.setEnabled(false);
        saveButton.setEnabled(false);
        
        // Jorge-PFC 27/12/2005
		currentNetworkFrame= null;
		
        // Jorge-PFC 27/12/2005
		elviraGUIMediator.setSpinnerModel(decisionTreeFrame);
		
		// Jorge-PFC 29/12/2005
		elviraGUIMediator.setupCombos(decisionTreeFrame.getState());
		
		// Jorge-PFC 07/01/2006
    	if (decisionTreeFrame.getState().descriptionByTitle) {
    		byTitleItem.setSelected(true);
    	}
    	else {
    		byNameItem.setSelected(true);
    	}
	}

    
    /**
     * Deshabilita las toolbars generales y las opciones de
     * menu/botones que no se quieran utilizar cuando esté
     * mostrandose el arbol de decision
     * 
     * Añadido por Manuel Luque el 25/2/2009
     */
    public void enablePolicyTreeToolbars(PolicyTreeFrame policyTreeFrame) {

    	elviraGUIMediatorPT.getPolicyTreeToolbar().setVisible(true);

    	editionToolbar.setVisible(false);
    	editMenu.setEnabled(false);
    	
    	explanationToolbar.setVisible(false);
    	
    	inferenceMenu.setEnabled(false);
    	tasksMenu.setEnabled(false);

      //Introducido por jruiz
      sensitivityAnalysisItem.setEnabled(false);
      //Fin introducido por jruiz

    	optionsMenu.setEnabled(false);
    	viewMenu.setEnabled(true);
    	
    	setPrecisionItem.setEnabled(true);
    	influenceItem.setEnabled(false);
        saveItem.setEnabled(false);
        saveAsItem.setEnabled(false);
        saveAllItem.setEnabled(false);
        saveAndReopenItem.setEnabled(false);
        loadEvidenceItem.setEnabled(false);
        saveEvidenceItem.setEnabled(false);
        
        // Jorge-PFC 26/12/2005
        workingMode.setEnabled(false);
        saveButton.setEnabled(false);
        
        // Jorge-PFC 27/12/2005
		currentNetworkFrame= null;
		
        // Jorge-PFC 27/12/2005
		elviraGUIMediatorPT.setSpinnerModel(policyTreeFrame);
		
		// Jorge-PFC 29/12/2005
		//elviraGUIMediatorPT.setupCombos(policyTreeFrame.getState());
		
		// Jorge-PFC 07/01/2006
    	if (policyTreeFrame.getState().descriptionByTitle) {
    		byTitleItem.setSelected(true);
    	}
    	else {
    		byNameItem.setSelected(true);
    	}
	}



	public void showDanOptions() {
		// TODO Auto-generated method stub
		 utilityNodeButton.setEnabled(true);
	        decisionNodeButton.setEnabled(true);
	        observedNodeButton.setEnabled(true);
	        utilityItem.setEnabled(true);
	        decisionItem.setEnabled(true);
	        continuousItem.setEnabled(false);
	        continuousNodeButton.setEnabled(false);
	        
	        
	        influenceItem.setEnabled(false);
	        influencesLinkButton.setEnabled(false);

	        // Jorge-PFC 26/12/2005
	        enableExpandDecTree(false);

	        enableConstraints(false);
	        generateDBCItem.setEnabled(false);
	        inferenceOptionsItem.setEnabled(false);
	        explanationOptionsItem.setEnabled(false);
	        
	       //Introducido por jruiz
	       sensitivityAnalysisItem.setEnabled(false);
	       sensitivityAnalysisButton.setEnabled(false);
	}


	public void showUIDOptions() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		 utilityNodeButton.setEnabled(true);
	        decisionNodeButton.setEnabled(true);
	        observedNodeButton.setEnabled(true);
	        utilityItem.setEnabled(true);
	        decisionItem.setEnabled(true);
	        continuousItem.setEnabled(false);
	        continuousNodeButton.setEnabled(false);
	        
	        
	        influenceItem.setEnabled(false);
	        influencesLinkButton.setEnabled(false);

	        // Jorge-PFC 26/12/2005
	        enableExpandDecTree(false);

	        enableConstraints(false);
	        generateDBCItem.setEnabled(false);
	        inferenceOptionsItem.setEnabled(false);
	        explanationOptionsItem.setEnabled(false);
	        
	       //Introducido por jruiz
	       sensitivityAnalysisItem.setEnabled(false);
	       sensitivityAnalysisButton.setEnabled(false);
		
	}


	public ElviraGUIMediatorPT getElviraGUIMediatorPT() {
		return elviraGUIMediatorPT;
	}


	public void setElviraGUIMediatorPT(ElviraGUIMediatorPT elviraGUIMediatorPT) {
		this.elviraGUIMediatorPT = elviraGUIMediatorPT;
	}        
}

