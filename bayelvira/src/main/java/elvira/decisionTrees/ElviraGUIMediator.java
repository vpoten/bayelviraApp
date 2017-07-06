package elvira.decisionTrees;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;
import elvira.Bnet;
import elvira.Elvira;
import elvira.IDWithSVNodes;
import elvira.gui.ElviraFrame;
import elvira.gui.NetworkFrame;

/**
 * Intenta minimizar el nº de cambios en el ElviraFrame delegando las nuevas acciones a esta clase
 * 
 * @author Jorge Fern&aacute;ndez Su&aacute;rez
 * 
 * DONE: Realmente es un mediator, cambiarle el nombre
 */
public class ElviraGUIMediator implements ActionListener, ChangeListener {
    /* Añadido por Jorge-PFC el 28/11/2005 */
    /**
     * 
     */
    private JToolBar decisionTreeToolbar= decisionTreeToolbar= new JToolBar();

    /* Añadido por Jorge-PFC el 26/12/2005 */
    public JToolBar getDecisionTreeToolbar() {
    	return decisionTreeToolbar;
    }
    
    /* Añadido por Jorge-PFC el 28/11/2005 */
    /**
     * 
     */
    private JSpinner spinnerNivelExpansionDT;

    /* Añadido por Jorge-PFC el 01/12/2005 */
    /**
     * 
     */
    private JComboBox comboChancePrecision= new JComboBox();
    
    /* Añadido por Jorge-PFC el 01/12/2005 */
    /**
     * 
     */
    private JComboBox comboUtilityPrecision= new JComboBox();

    /**
     * 
     */
    static public int expandedLevelsByDefault=0;
    
    /**
     * 
     */
    private ResourceBundle decisionTreesBundle;

	/**
	 * 
	 */
	private ElviraFrame elviraFrame;
	
	/**
	 * @param elviraFrame
	 */
	public ElviraGUIMediator(ElviraFrame elviraFrame) {
		this.elviraFrame= elviraFrame;
        setupBundle();
        
        String defaultLevels=ElviraFrame.localize(decisionTreesBundle,"PFC_DT.DefaultExpandedLevels.label");
        expandedLevelsByDefault= new Integer(defaultLevels).intValue();
        
        addDecisionTreeToolbar( elviraFrame.getToolbarPanel() );
	}
	
	/**
	 * @param toolbarPanel
	 */
	public void addDecisionTreeToolbar(JPanel toolbarPanel) {
        // Añadido por Jorge-PFC el 28/11/2005
        decisionTreeToolbar.setAlignmentY(0.222222F);
        decisionTreeToolbar.setBounds(0,0,199,29);
        decisionTreeToolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        decisionTreeToolbar.setFloatable(false);
        decisionTreeToolbar.setVisible(false);
        
        // Se añade al panel de herramientas de Elvira
        toolbarPanel.add(decisionTreeToolbar,BorderLayout.WEST);
        
        // Añadido por Jorge-PFC el 28/11/2005
        // DONE: Añadir al bundle la etiqueta
        JLabel l1= new JLabel(ElviraFrame.localize(decisionTreesBundle,"PFC_DT.ExpandLevel.label"));
        l1.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        decisionTreeToolbar.add(l1);

        // Añadido por Jorge-PFC el 28/11/2005
        // DONE: Añadir al bundle la expansion inicial del arbol a mostrar
        // DONE: Nivel máximo de expansion debe cambiarse al cambiar a la vista del arbol de decision
    	SpinnerNumberModel spinnerModelNivelExpansion= new SpinnerNumberModel(0,0,999,1);
    	spinnerNivelExpansionDT= new JSpinner(spinnerModelNivelExpansion);

        // DONE: ¿Una manera menos liosa de darle un tamaño minimo al spinner?
    	spinnerNivelExpansionDT.setPreferredSize(spinnerNivelExpansionDT.getPreferredSize());
    	
    	spinnerNivelExpansionDT.setToolTipText(ElviraFrame.localize(decisionTreesBundle,"PFC_DT.ExpandLevel.tip"));
    	decisionTreeToolbar.add(spinnerNivelExpansionDT);
    	decisionTreeToolbar.addSeparator();
    	
        // Añadido por Jorge-PFC el 01/12/2005
    	ListCellRenderer renderer = new DefaultListCellRenderer();
    	((JLabel)renderer).setHorizontalAlignment(SwingConstants.RIGHT);     	
    	comboChancePrecision.setRenderer(renderer);
    	decisionTreeToolbar.addSeparator();

        JLabel l2= new JLabel(ElviraFrame.localize(decisionTreesBundle,"PFC_DT.ChancePrecision.label"));
        l2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
    	decisionTreeToolbar.add(l2);
    	decisionTreeToolbar.add(comboChancePrecision);
    	decisionTreeToolbar.addSeparator();

        // Añadido por Jorge-PFC el 01/12/2005
    	comboUtilityPrecision.setRenderer(renderer);
        JLabel l3= new JLabel(ElviraFrame.localize(decisionTreesBundle,"PFC_DT.UtilityPrecision.label"));
        l3.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
    	decisionTreeToolbar.add(l3);
    	decisionTreeToolbar.add(comboUtilityPrecision);
    	decisionTreeToolbar.addSeparator();
    	
    	// Añadido por Jorge-PFC el 01/12/2005
    	decisionTreeToolbar.addSeparator();
    	
        // Añadido por Jorge-PFC el 28/11/2005
        /* Listeners for the decisionTreeToolbar items */
        spinnerNivelExpansionDT.addChangeListener(this);

    	// Añadido por Jorge-PFC el 01/12/2005
    	for( int i=0; i<elviraFrame.getSetPrecisionItem().getItemCount(); i++ ) {
    		JMenuItem item= elviraFrame.getSetPrecisionItem().getItem(i);
    		comboChancePrecision.addItem(item.getText());
    		comboUtilityPrecision.addItem(item.getText());
    		
    		item.addActionListener(this);
    	}

    	Enumeration<AbstractButton> e= elviraFrame.getViewGroup().getElements();
    	while( e.hasMoreElements() ) {
    		JCheckBoxMenuItem item= (JCheckBoxMenuItem) e.nextElement();
    		
    		item.addActionListener(this);
    	}
    	
    	// Añadido por Jorge-PFC el 01/12/2005
    	comboChancePrecision.setMaximumSize( comboChancePrecision.getPreferredSize() );
    	
    	// Añadido por Jorge-PFC el 01/12/2005
    	comboUtilityPrecision.setMaximumSize( comboUtilityPrecision.getPreferredSize() );
    	
    	// Añadido por Jorge-PFC el 01/12/2005
    	comboChancePrecision.addActionListener(this);
    	
    	// Añadido por Jorge-PFC el 01/12/2005
    	comboUtilityPrecision.addActionListener(this);
	}
	
	
	/**
	 * Control de nº de arbol de decision creado para un mismo DI
	 */
	HashMap<String,Integer> versiones= new HashMap<String,Integer>();
	
	// Modificado por Jorge-PFC el 24/11/2005
    /**
     * @param event
     * 
     * DONE: ¿porque al pulsar este boton a partir de la 2ª para un mismo DI, no sale el AD expandido
     * hasta el nivel predeterminado? Se debía a que se duplicaba el nombre de la nueva ventana
     */
    public void dectreeAction(ActionEvent event) {
    	NetworkFrame currentNetworkFrame= elviraFrame.getNetworkFrame();
    	Bnet bnet=currentNetworkFrame.getEditorPanel().getBayesNet();
    	
    	// DONE: ¿Esta comprobacion no deberia ir al principio? si no es un ID, no deberia hacer nada
    	if( bnet instanceof IDWithSVNodes) {
        	AbstractCompositeNode decisionTree;
			try {
				decisionTree = DecisionTreeBuilder.getInstance().buildDT((IDWithSVNodes)bnet);
			} catch (DTBuildingException e) {
        		// TODO: ¿mensaje avisando de que no se ha podido generar el arbol de decision?
				e.printStackTrace();
				return;
			}
    		
    		// DONE: añadir un numeral para evitar duplicados
    		int version=1;
    		String name= currentNetworkFrame.getTitle()+" DT ";
    		
    		if(versiones.containsKey(name)) {
    			version= versiones.get(name).intValue()+1;
    		}

    		versiones.put(name,new Integer(version));
    		name+= version;
    		
        	DecisionTreeFrame decisionTreeFrame= new DecisionTreeFrame(decisionTree, name, decisionTreesBundle);
    		
    		elviraFrame.getSetPrecisionItem().setEnabled(true);    		
    		elviraFrame.enableMenusOpenNetworks(true, bnet);
    		
        	elviraFrame.getDesktopPane().add(decisionTreeFrame);
        	decisionTreeFrame.setVisible(true);
    		
            JCheckBoxMenuItem newMenuItem = new JCheckBoxMenuItem(name,true);
            newMenuItem.addItemListener(elviraFrame.new WindowMenuItemListener());
            elviraFrame.getWindowMenu().add(newMenuItem);
            elviraFrame.getWindowGroup().add(newMenuItem);
            newMenuItem.setSelected(true);

            // Necesario para la expansion inicial del arbol de decision
            nivelExpansionDTChange(null);
    	}
    }
    
    /**
     * @param event
     * 
     * Modificado por Jorge-PFC el 28/11/2005
     */
    public void nivelExpansionDTChange(ChangeEvent event) {
    	DecisionTreeFrame decisionTreeFrame= (DecisionTreeFrame) elviraFrame.getDesktopPane().getSelectedFrame();
    	DecisionTreeViewer tree= decisionTreeFrame.getDecisionTreeViewer(); //.getTree();
    	
    	// Cuando se cambia el nivel de expansion, todo debe estar colapsado
    	for( int i=tree.getRowCount()-1; i>=0; i--) {
    		tree.collapseRow(i);
    	}
    	
    	int n= ((Integer) spinnerNivelExpansionDT.getValue()).intValue();
    	if( n==0 ) {
    		return;
    	}
    	
    	// Se expanden todos los nodos al nivel indicado en el spinner
    	Vector<TreePath> paths= tree.getModel().getLevelProxy().getLevel( n-1 );
    	
    	for (TreePath path : paths) {
    		tree.expandPath(path);
    	}
    }

    /**
     * @param event
     * 
     * Añadido por Jorge-PFC el 04/12/2005 
     */
    public void comboChancePrecisionAction(ActionEvent event) {
    	JInternalFrame iframe= elviraFrame.getDesktopPane().getSelectedFrame();
    	if( !(iframe instanceof DecisionTreeFrame) ) {
    		return;
    	}
    	
    	String seleccion= (String) comboChancePrecision.getSelectedItem();
    	
    	DecisionTreeFrame decisionTreeFrame= (DecisionTreeFrame) iframe;
		decisionTreeFrame.saveChancePrecisionIndex(comboChancePrecision.getSelectedIndex());
		
    	DecisionTreeViewer tree= decisionTreeFrame.getDecisionTreeViewer(); //.getTree();
    	DecisionTreeCellRenderer renderer= (DecisionTreeCellRenderer) tree.getCellRenderer();

    	// 0.0001 -> 6 caracteres - 2 del '0.'
    	int len= seleccion.length()-2;
		if( len != renderer.getPrecisionProxy().getDefaultChancePrecision() ) {
			renderer.getPrecisionProxy().setDefaultChancePrecision(len);
			tree.getModel().fireAllNodesChanged();
		}
    }
   
    // Añadido por Jorge-PFC el 04/12/2005
    /**
     * @param event
     */
    public void comboUtilityPrecisionAction(ActionEvent event) {
    	JInternalFrame iframe= elviraFrame.getDesktopPane().getSelectedFrame();
    	if( !(iframe instanceof DecisionTreeFrame) ) {
    		return;
    	}
    	
    	String seleccion= (String) comboUtilityPrecision.getSelectedItem();

    	elviraFrame.getLastPrecItem().setSelected(false);

    	// Añadido por Jorge-PFC el 01/12/2005
    	for( int i=0; i<elviraFrame.getSetPrecisionItem().getItemCount(); i++ ) {
    		JCheckBoxMenuItem item= (JCheckBoxMenuItem) elviraFrame.getSetPrecisionItem().getItem(i);
    		
    		if(seleccion.equals(item.getText())) {
    			item.setSelected(true);
    			elviraFrame.setLastPrecItem(item);
    			break;
    		}
    	}
    	
    	DecisionTreeFrame decisionTreeFrame= (DecisionTreeFrame) iframe;
		decisionTreeFrame.saveUtilityPrecisionIndex(comboUtilityPrecision.getSelectedIndex());
		
    	// DONE: ¿Aun es necesaria esta comprobacion (treeviewer!=null)? creo q no, cada TreeFrame tiene su TreeViewer
		DecisionTreeViewer tree= decisionTreeFrame.getDecisionTreeViewer(); //.getTree();
		DecisionTreeCellRenderer renderer= (DecisionTreeCellRenderer) tree.getCellRenderer();

		// 0.0001 -> 6 caracteres - 2 del '0.'
		int len= seleccion.length()-2;
		if( len != renderer.getPrecisionProxy().getDefaultUtilityPrecision() ) {
			renderer.getPrecisionProxy().setDefaultUtilityPrecision(len);
			tree.getModel().fireAllNodesChanged();
		}
    }
    
    public void setupCombos(DecisionTreeFrame.State state) {
    	comboChancePrecision.setSelectedIndex(state.precisionProbabilidades);
    	comboUtilityPrecision.setSelectedIndex(state.precisionUtilidades);
    }

    // Añadido por Jorge-PFC el 05/12/2005
    /**
     * @param item
     */
    public void utilityPrecisionItemChangedAction(JCheckBoxMenuItem item) {
    	String seleccion= item.getText();
    	
    	for( int i=0; i<comboUtilityPrecision.getItemCount(); i++ ) {
    		String txt= (String) comboUtilityPrecision.getItemAt(i);
    		
    		if(seleccion.equals(txt)) {
    			comboUtilityPrecision.setSelectedIndex(i);
    			break;
    		}
    	}    	
    }

    /**
     * @param byTitle
     */
    public void byTitleDescriptionChangedAction(boolean byTitle) {
    	JInternalFrame iframe= elviraFrame.getDesktopPane().getSelectedFrame();
    	if( !(iframe instanceof DecisionTreeFrame) ) {
    		return;
    	}
    	
		DecisionTreeViewer tree= ((DecisionTreeFrame) iframe).getDecisionTreeViewer(); //.getTree();
		
		DecisionTreeCellRenderer renderer= (DecisionTreeCellRenderer) tree.getCellRenderer();
		renderer.getDescriptionProxy().setUseTitleByDefault(byTitle);
		((DecisionTreeFrame) iframe).saveDescriptionByTitle(byTitle);
		
		tree.getModel().fireAllNodesChanged();
    }
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
        Object object = event.getSource();
        String cmd= event.getActionCommand();
		
		if( cmd.equals("Create Decision Tree") ) {
        	// Reactivado por Jorge-PFC el 24/11/2005
        	dectreeAction (event);
		}
        else if( object == comboUtilityPrecision ) {
        	// Añadido por Jorge-PFC 04/12/2005
        	comboUtilityPrecisionAction(event);
        }
        else if( object == comboChancePrecision ) {
        	// Añadido por Jorge-PFC 04/12/2005
        	comboChancePrecisionAction(event);
        }
        else if( cmd.startsWith("Precision to ") ) {
        	// Añadido por Jorge-PFC 05/12/2005 (aprox)
            utilityPrecisionItemChangedAction((JCheckBoxMenuItem)object);
        }
        else if( cmd.equals("By Title") ) {
        	// Añadido por Jorge-PFC 08/12/2005
            byTitleDescriptionChangedAction(true);
        }
        else if( cmd.equals("By Name") ) {
        	// Añadido por Jorge-PFC 08/12/2005
        	byTitleDescriptionChangedAction(false);;
        }
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent event) {
		Object object= event.getSource();
		
        // Añadido por Jorge-PFC el 28/11/2005
		if( object == spinnerNivelExpansionDT ) {
			nivelExpansionDTChange(event);
		}		
	}

	/**
	 * 
	 */
	public void setupBundle() {
        switch (Elvira.getLanguaje()) {
        	case Elvira.AMERICAN:
        		decisionTreesBundle= ResourceBundle.getBundle("elvira/localize/DecisionTrees");
        		break;
        	case Elvira.SPANISH:
        		decisionTreesBundle= ResourceBundle.getBundle("elvira/localize/DecisionTrees_sp");
        		break;
        }
	}
	
	/**
	 * @param decisionTreeFrame
	 */
	public void setSpinnerModel(DecisionTreeFrame decisionTreeFrame) {
		spinnerNivelExpansionDT.setModel(decisionTreeFrame.getState().spinnerModelNivelExpansion);
	}
}
