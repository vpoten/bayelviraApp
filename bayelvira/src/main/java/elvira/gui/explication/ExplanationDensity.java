/*
 * ExplanationFunction.java
 *
 * Created on 21 de noviembre de 2003, 12:10
 */

package elvira.gui.explication;

import java.util.ResourceBundle;
import javax.swing.BorderFactory; 
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import elvira.gui.continuousEdit.*;
import elvira.potential.*;
import elvira.Elvira;

/**
 * This class is used to show in detail, in an other frame, a density function result of a propagation algorithm.
 * This gui allows integration calculus above this density, too.
 * It's activated by popup menu of inference panel.
 * 27/10/03
 * @author  andrew
 */
public class ExplanationDensity extends JFrame implements ActionListener {
    
    static int WIDTH = 600;
    VisualExplanationContinuous visual;
    ExplanationContinuous exp;
    
    JTextField eResultado;
    JTextField elimitInf;
    JTextField elimitSup;
    
    PanelEditTree function;
    
    JPanel iResult;
    
    String integrationResult=String.valueOf(0.0);

    ContinuousProbabilityTree treePropag;
    ContinuousProbabilityTree treeIntegrate;
    
    double minLimit=0.0;
    double maxLimit=0.0;
    
    JPanel functionGraph;

    /* Items of the inference Menu */

    /**
     * Contains the actual networkFrame
     */


    private static String imagesPath = "elvira/gui/images/";    
    private JMenuItem saveCaseItem, storeCaseItem, expandItem, explainItem,
	                  optionsItem, firstCaseItem,
	                  previousCaseItem, nextCaseItem,
	                  lastCaseItem, caseEditorItem, caseMonitorItem, propagateItem, dectreeItem;
    
    /**
     * Contains the menu strings for the languaje selected
     */
  
    private ResourceBundle menuBundle;
    
    private JComboBox workingMode;
    
    private JTextField nodeName = new JTextField(25);    
    
    private JComboBox thresholdComboBox = new JComboBox();
    private JLabel thresholdLabel = new JLabel();    

    JButton firstButton = new JButton(),
            previousButton = new JButton(),
            nextButton = new JButton(),
            lastButton = new JButton(),
    	    functionButton= new JButton();
    
    public ImageIcon firstIcon, previousIcon, nextIcon, lastIcon;
    
    
    private JPanel toolbarPanel = new JPanel();
    private JToolBar explanationToolbar;
    
    ElviraAction elviraAction = new ElviraAction();
    
    /** Creates a new instance of ExplanationFunction 
     *  @param exp, a explanationContinuous of a continuous node.
     */
    public ExplanationDensity(ExplanationContinuous exp) {
        
        
         super("Explain Function");
        
        this.exp=exp;
         visual=new VisualExplanationContinuous(exp);
        JButton salirButton = new JButton("EXIT");
        
        salirButton.setActionCommand("EXIT");
        salirButton.addActionListener(this);
        
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        TitledBorder titled = BorderFactory.createTitledBorder(loweredbevel, "Integration");
        
        
        functionGraph=new JPanel();
        functionGraph.setLayout(null);
        functionGraph.setPreferredSize(new Dimension(WIDTH,300));
        functionGraph.setMinimumSize(new Dimension(WIDTH,300));
        functionGraph.setAlignmentX(LEFT_ALIGNMENT);
        TitledBorder t = new javax.swing.border.TitledBorder("Graph Function");
        t.setTitleJustification(TitledBorder.CENTER);
        functionGraph.setBorder(t);
        
        visual.graph.setBounds(0,10,WIDTH-50,275);
        
        functionGraph.add(visual.graph);
        
       
        JPanel pane=new JPanel();
        

        pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));

        
        pane.add(Box.createRigidArea(new Dimension(0,5)));
        pane.add(functionGraph);
        
        
        function = new PanelEditTree(this);

        if (exp!=null){
        if (exp.isEvidenceIntroduced())
            treePropag=exp.getPCPTEvidence().getTree();
        else
            treePropag=exp.getPCPT().getTree();
        
        function.setTree(treePropag);
        
        
        function.getCPTreePaint().paint_x=5;   
        function.getCPTreePaint().paint_y=5;   
        
        NodoPaint.LEAF_NODE_COLOR=exp.getCasesList().getCurrentCase().getColor();
        
        
        }
        
        /*function.setBorder(new javax.swing.border.TitledBorder("Ecuation"));
        function.setPreferredSize(new Dimension(WIDTH,100));
        function.setMinimumSize(new Dimension(WIDTH,100));
        function.setAlignmentX(LEFT_ALIGNMENT);
        pane.add(function);
         */
        JScrollPane treeScrollPane = new JScrollPane(function);
        treeScrollPane.setBorder(new javax.swing.border.TitledBorder("Ecuation"));
        treeScrollPane.setPreferredSize(new Dimension(WIDTH,100));
        treeScrollPane.setMinimumSize(new Dimension(WIDTH,100));
        treeScrollPane.setPreferredSize(new Dimension(HEIGHT,150));
        treeScrollPane.setMinimumSize(new Dimension(HEIGHT,150));
        
        treeScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        pane.add(treeScrollPane);

      
        
                //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        
        
        JPanel integration = new JPanel();
        
        integration.setLayout(new BoxLayout(integration,BoxLayout.X_AXIS));
        //integration.setBorder(new javax.swing.border.TitledBorder("Integration"));
        
        
        titled = BorderFactory.createTitledBorder(loweredbevel, "Integration");
        integration.setBorder(titled);
        
        
        integration.setPreferredSize(new Dimension(WIDTH,200));
        integration.setMinimumSize(new Dimension(WIDTH,200));
        integration.setPreferredSize(new Dimension(HEIGHT,150));
        integration.setMinimumSize(new Dimension(HEIGHT,150));
        
        integration.setAlignmentX(LEFT_ALIGNMENT);
        
        
        JPanel limits = new JPanel();
        limits.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        limits.setPreferredSize(new Dimension(200,150));
        limits.setMinimumSize(new Dimension(200,150));
        limits.setAlignmentX(LEFT_ALIGNMENT);
        
        GridBagLayout gridbag= new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        limits.setLayout(gridbag);
        
        integration.add(limits);
        
        
        
        c.ipadx=0;
        c.ipady=0;
        c.fill=GridBagConstraints.HORIZONTAL;
 //        c.weightx=0.5;
//        c.weighty=0.0;
        c.anchor=GridBagConstraints.WEST;
        c.insets=new Insets(5,10,5,10);
        c.gridwidth=1;
        c.gridheight=1;
        
        
        
        JLabel limitInf=new JLabel("Low Limit Integration:");
        c.gridx=0;
        c.gridy=0;
        gridbag.setConstraints(limitInf, c);
        limits.add(limitInf);
        
        
        JLabel limitSup=new JLabel("Upper Limit Integration:");
        c.gridx=0;
        c.gridy=1;
        gridbag.setConstraints(limitSup, c);
        limits.add(limitSup);
        

        c.gridwidth=1;
        elimitInf=new JTextField(4);
        elimitInf.setHorizontalAlignment(JTextField.RIGHT);
    
        c.gridx=1;
        c.gridy=0;
        gridbag.setConstraints(elimitInf, c);
        limits.add(elimitInf);
        
        
        elimitSup=new JTextField(4);
        elimitSup.setHorizontalAlignment(JTextField.RIGHT);
        c.gridx=1;
        c.gridy=1;
        gridbag.setConstraints(elimitSup, c);
        limits.add(elimitSup);

        JButton integrationButton = new JButton("INTEGRATION");
        integrationButton.setActionCommand("Integrate");
        integrationButton.addActionListener(this);
        c.weightx=0.0;
        c.weighty=0.75;
        c.gridx=0;
        c.gridy=3;
        gridbag.setConstraints(integrationButton, c);
        limits.add(integrationButton);

        JButton clearButton = new JButton("CLEAR AREA");
        clearButton.setActionCommand("Clear Area");
        clearButton.addActionListener(this);
        c.weightx=0.0;
        c.weighty=0.75;
        c.gridx=1;
        c.gridy=3;
        gridbag.setConstraints(clearButton, c);
        limits.add(clearButton);

        iResult= new JPanel();
        iResult.setLayout(null);
        TitledBorder b = new javax.swing.border.TitledBorder("IntegrationResult");
        b.setTitleJustification(TitledBorder.CENTER);
        iResult.setBorder(b);
        iResult.setPreferredSize(new Dimension(300,150));
        iResult.setMinimumSize(new Dimension(200,150));
        iResult.setAlignmentX(LEFT_ALIGNMENT);

        integration.add(iResult);
        
               
/*        JLabel resultado=new JLabel("Integration Result:");
        c.gridx=2;
        c.gridy=1;
        gridbag.setConstraints(resultado, c);
 
         limits.add(resultado);
        
        
        JTextField eresultado=new JTextField(4);
        c.gridx=2;
        c.gridy=2;
        gridbag.setConstraints(eresultado, c);
    
         limits.add(eresultado);
        */
        
        
        
        pane.add(integration);

        
        salirButton.setActionCommand("EXIT");
        salirButton.addActionListener(this);
        buttonPane.add(salirButton);

        //Put everything together, using the content pane's BorderLayout.

        toolbarPanel.setLayout(null);
        toolbarPanel.setPreferredSize(new Dimension(WIDTH,50));
        toolbarPanel.setMinimumSize(new Dimension(WIDTH,50));
        toolbarPanel.setAlignmentX(LEFT_ALIGNMENT);
        t = new javax.swing.border.TitledBorder("CHANGE CURRENT CASE");
        t.setTitleJustification(TitledBorder.CENTER);
        toolbarPanel.setBorder(t);

        Container contentPane = getContentPane();
        
        contentPane.add(toolbarPanel,BorderLayout.NORTH);

        contentPane.add(pane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        
        //***********************************************************************
        //Explanation tool bar
        //***********************************************************************
        switch (Elvira.getLanguaje()) {
           case Elvira.AMERICAN: menuBundle = ResourceBundle.getBundle ("elvira/localize/Menus");
                                 break;
           case Elvira.SPANISH: menuBundle = ResourceBundle.getBundle ("elvira/localize/Menus_sp");
                                break;
        }
        
      
           firstIcon = new ImageIcon(imagesPath+"first.gif");
           previousIcon = new ImageIcon (imagesPath+"previous.gif");
	   nextIcon = new ImageIcon (imagesPath+"next.gif");
	   lastIcon = new ImageIcon (imagesPath+"last.gif");

        
        explanationToolbar = new JToolBar();


		explanationToolbar.setAlignmentY(0.222222F);
		explanationToolbar.setBounds(152,15,280,29);
		explanationToolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		explanationToolbar.setFloatable(true);
		explanationToolbar.setVisible(true);
		toolbarPanel.add(explanationToolbar,BorderLayout.NORTH);
                
                               
                nodeName.setMaximumSize(nodeName.getPreferredSize());
                nodeName.setHorizontalAlignment(JTextField.CENTER);

                setNodeName("Prior probabilities");
                setColorNodeName(VisualExplanationFStates.green);



		insertButton (firstButton, firstIcon,
		              explanationToolbar, "Explanation.First");
		insertButton (previousButton, previousIcon,
		              explanationToolbar, "Explanation.Previous");

		explanationToolbar.add(nodeName);
		nodeName.setMaximumSize(nodeName.getPreferredSize());
           	nodeName.setHorizontalAlignment(JTextField.CENTER);

		setNodeName("Prior probabilities");
		setColorNodeName(VisualExplanationFStates.green);

		insertButton (nextButton, nextIcon,
		              explanationToolbar, "Explanation.Next");
		insertButton (lastButton, lastIcon,
		              explanationToolbar, "Explanation.Last");

        
                firstButton.addActionListener (elviraAction);
		nextButton.addActionListener (elviraAction);
		previousButton.addActionListener (elviraAction);
		lastButton.addActionListener (elviraAction);

               toolbarPanel.setLayout(new BorderLayout(0,0));
               pane.add(toolbarPanel);
               if (exp!=null){
                setNodeName(exp.getCasesList().getCurrentCase().getIdentifier());
                setColorNodeName(exp.getCasesList().getCurrentCase().getColor());
               }

        //***********************************************************************
        
        
        pack();

        
        //setBounds(250,250,700,500);
        setVisible(true);

        
        //visual.paintFunction((Graphics2D)getGraphics(),0,10,WIDTH-50,275);
        
        if (exp!=null){
            elimitInf.setText(new Double(exp.getNode().getMin()).toString());
            elimitSup.setText(new Double(exp.getNode().getMax()).toString());
        }
    }
     public void paint (Graphics g){
        super.paint(g);
        Graphics g2 = iResult.getGraphics();
        g2.setColor(new Color(255,255,200));
        g2.setFont(new Font(g2.getFont().getFontName(),g2.getFont().getStyle(),45));
        g2.drawString(integrationResult,130-10*integrationResult.length(),75);
   
        if (exp!=null){
        visual.graph.datarect.x+=3;
        visual.graph.datarect.y+=41;
        visual.graph.datarect.height-=2;
        visual.data.fillArea(g, visual.graph.datarect, minLimit,maxLimit);
        }
        toolbarPanel.repaint();        
     }
    
    public void actionPerformed(ActionEvent e){
        
        if (e.getActionCommand().equals("EXIT")){
            setVisible(false);
            
        }else if (e.getActionCommand().equals("Integrate")){
            minLimit=Double.valueOf(elimitInf.getText()).doubleValue();
            maxLimit=Double.valueOf(elimitSup.getText()).doubleValue();
            
            if (minLimit>maxLimit){
                minLimit=exp.getNode().getMin();
                elimitInf.setText(Double.toString(minLimit));
                maxLimit=minLimit;
                elimitSup.setText(Double.toString(maxLimit));
                
            }
            
            if (minLimit<exp.getNode().getMin()){
                    minLimit=exp.getNode().getMin();
                    elimitInf.setText(Double.toString(minLimit));
                    
            }
            
            if (maxLimit>exp.getNode().getMax()){
                    maxLimit=exp.getNode().getMax();
                    elimitSup.setText(Double.toString(maxLimit));
            }

            
            
            
            treeIntegrate=treePropag.integral(exp.getNode(),minLimit,maxLimit);
            
            //function.setTree(treeIntegrate);
            treeIntegrate.print();

            integrationResult=treeIntegrate.getProb().ToString();
            if (integrationResult.length()>=10)
                integrationResult=integrationResult.substring(0,9);
           
            repaint();
            
        }else if (e.getActionCommand().equals("Clear Area")){
            
            minLimit=exp.getNode().getMin();
            maxLimit=minLimit;
            repaint();
            
        }
        
    }

  private void insertButton (AbstractButton button, javax.swing.Icon icon,
                                    JToolBar toolbar, String name) {
		button.setIcon(icon);
		button.setToolTipText(localize(menuBundle,name+".tip"));
		button.setMnemonic(localize(menuBundle,name+".mnemonic").charAt(0));
		toolbar.add(button);
		button.setMargin(new Insets (1,1,1,1));
		button.setAlignmentY(0.5f);
		button.setAlignmentX(0.5f);
	}

   public void setNodeName(String name){
      nodeName.setFont(new Font("SansSerif",Font.BOLD,12));
      nodeName.setText(name);
      nodeName.setEditable(false);
      nodeName.repaint();
   }

    public void setColorNodeName(Color c){
      nodeName.setBackground(c);
   }

   public static String localize (ResourceBundle bundle, String name) {
      return Elvira.localize(bundle, name);
   }

   public void enableToolbars (boolean value) {
     toolbarPanel.setVisible(value);
   }

    class WokingModeItem implements java.awt.event.ActionListener
    {

       /**
        * Manage the change of state in the valuesComboBox
        */

            public void actionPerformed(java.awt.event.ActionEvent event)
            {
                    Object object = event.getSource();
                    if (object == workingMode)
                            workingComboBox_actionPerformed (event);
            }
    }

   
   void workingComboBox_actionPerformed(java.awt.event.ActionEvent event)
	{
                explanationToolbar.setVisible(true);
		toolbarPanel.repaint();

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

	public class ElviraAction implements ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
		   Object object = event.getSource();
                   if (object == firstCaseItem || object ==firstButton)
		      firstCaseAction (event);
                   else if (object == nextCaseItem || object ==nextButton)
		      nextCaseAction (event);
		   else if (object == previousCaseItem || object ==previousButton)
		      previousCaseAction (event);
		   else if (object == lastCaseItem || object ==lastButton)
		      lastCaseAction (event);
		}
	}

  public void firstCaseAction(ActionEvent event) {

    Elvira.getElviraFrame().firstCaseAction (event);      
    reload();
  }

  public void nextCaseAction (ActionEvent event) {

    
    
    Elvira.getElviraFrame().nextCaseAction (event);      
    reload();
    
  }

    public void previousCaseAction (ActionEvent event) {
      Elvira.getElviraFrame().previousCaseAction (event);      
      reload();
    }

  public void lastCaseAction (ActionEvent event) {

      Elvira.getElviraFrame().lastCaseAction (event);      
      reload();
  }
  
  void reload(){
      
    setNodeName(exp.getCasesList().getCurrentCase().getIdentifier());
    setColorNodeName(exp.getCasesList().getCurrentCase().getColor());

    functionGraph.remove(visual.graph);
    visual=new VisualExplanationContinuous(exp);
    visual.graph.setBounds(0,10,WIDTH-50,275);
        
    functionGraph.add(visual.graph);

    if (exp.isEvidenceIntroduced())
            treePropag=exp.getPCPTEvidence().getTree();
    else
            treePropag=exp.getPCPT().getTree();
        
    function.setTree(treePropag);

    function.getCPTreePaint().paint_x=5;   
    function.getCPTreePaint().paint_y=5;   

    NodoPaint.LEAF_NODE_COLOR=exp.getCasesList().getCurrentCase().getColor();
    repaint();

  
  }
  public static void main(String[] args) {
        ExplanationDensity a=new ExplanationDensity(null);
        a.setVisible(true);
        a.pack();
        
   }
}
