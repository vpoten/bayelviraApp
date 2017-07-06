package elvira.gui.explication.policytree;

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
import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.gui.ElviraFrame;
import elvira.gui.NetworkFrame;
import elvira.learning.policies.Rule;

/**
 * Intenta minimizar el nº de cambios en el ElviraFrame delegando las nuevas acciones a esta clase
 * 
 * @author Manuel Luque
 */
public class ElviraGUIMediatorPT implements ActionListener, ChangeListener {
    /* Añadido por Jorge-PFC el 28/11/2005 */
    /**
     * 
     */
    private JToolBar policyTreeToolbar= policyTreeToolbar= new JToolBar();

    /* Añadido por Jorge-PFC el 26/12/2005 */
    public JToolBar getPolicyTreeToolbar() {
    	return policyTreeToolbar;
    }
    
    /* Añadido por Jorge-PFC el 28/11/2005 */
    /**
     * 
     */
    private JSpinner spinnerNivelExpansionPT;

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
    private ResourceBundle policyTreesBundle;

	/**
	 * 
	 */
	private ElviraFrame elviraFrame;
	
	/**
	 * @param elviraFrame
	 */
	public ElviraGUIMediatorPT(ElviraFrame elviraFrame) {
		this.elviraFrame= elviraFrame;
        setupBundle();
        
        String defaultLevels=ElviraFrame.localize(policyTreesBundle,"PT.DefaultExpandedLevels.label");
        expandedLevelsByDefault= new Integer(defaultLevels).intValue();
        
        addPolicyTreeToolbar( elviraFrame.getToolbarPanel() );
	}
	
	/**
	 * @param toolbarPanel
	 */
	public void addPolicyTreeToolbar(JPanel toolbarPanel) {
        // Añadido por Jorge-PFC el 28/11/2005
        policyTreeToolbar.setAlignmentY(0.222222F);
        policyTreeToolbar.setBounds(0,0,199,29);
        policyTreeToolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        policyTreeToolbar.setFloatable(false);
        policyTreeToolbar.setVisible(false);
        
        // Se añade al panel de herramientas de Elvira
        toolbarPanel.add(policyTreeToolbar,BorderLayout.WEST);
        
        // Añadido por Jorge-PFC el 28/11/2005
        // DONE: Añadir al bundle la etiqueta
        JLabel l1= new JLabel(ElviraFrame.localize(policyTreesBundle,"PT.ExpandLevel.label"));
        l1.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
        policyTreeToolbar.add(l1);

        // Añadido por Jorge-PFC el 28/11/2005
        // DONE: Añadir al bundle la expansion inicial del arbol a mostrar
        // DONE: Nivel máximo de expansion debe cambiarse al cambiar a la vista del arbol de decision
    	SpinnerNumberModel spinnerModelNivelExpansion= new SpinnerNumberModel(0,0,999,1);
    	spinnerNivelExpansionPT= new JSpinner(spinnerModelNivelExpansion);

        // DONE: ¿Una manera menos liosa de darle un tamaño minimo al spinner?
    	spinnerNivelExpansionPT.setPreferredSize(spinnerNivelExpansionPT.getPreferredSize());
    	
    	spinnerNivelExpansionPT.setToolTipText(ElviraFrame.localize(policyTreesBundle,"PT.ExpandLevel.tip"));
    	policyTreeToolbar.add(spinnerNivelExpansionPT);
    	policyTreeToolbar.addSeparator();
    	
        // Añadido por Jorge-PFC el 01/12/2005
    	ListCellRenderer renderer = new DefaultListCellRenderer();
    	((JLabel)renderer).setHorizontalAlignment(SwingConstants.RIGHT);     	
    	comboChancePrecision.setRenderer(renderer);
    	policyTreeToolbar.addSeparator();

        JLabel l2= new JLabel(ElviraFrame.localize(policyTreesBundle,"PT.ChancePrecision.label"));
        l2.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
    	policyTreeToolbar.add(l2);
    	policyTreeToolbar.add(comboChancePrecision);
    	policyTreeToolbar.addSeparator();

        // Añadido por Jorge-PFC el 01/12/2005
    	comboUtilityPrecision.setRenderer(renderer);
        JLabel l3= new JLabel(ElviraFrame.localize(policyTreesBundle,"PT.UtilityPrecision.label"));
        l3.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
    	policyTreeToolbar.add(l3);
    	policyTreeToolbar.add(comboUtilityPrecision);
    	policyTreeToolbar.addSeparator();
    	
    	// Añadido por Jorge-PFC el 01/12/2005
    	policyTreeToolbar.addSeparator();
    	
        // Añadido por Jorge-PFC el 28/11/2005
        /* Listeners for the policyTreeToolbar items */
        spinnerNivelExpansionPT.addChangeListener(this);

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
    public void policyTreeAction(ActionEvent event,FiniteStates decision) {
    	NetworkFrame currentNetworkFrame= elviraFrame.getNetworkFrame();
    	Bnet bnet=currentNetworkFrame.getEditorPanel().getBayesNet();
    
   
    	
    	// DONE: ¿Esta comprobacion no deberia ir al principio? si no es un ID, no deberia hacer nada
    	if( bnet instanceof IDWithSVNodes) {
        	Rule policyTree;
			try {
				policyTree = PolicyTreeBuilder.getInstance().buildPT((IDWithSVNodes)bnet,decision);
			} catch (PTBuildingException e) {
        		// TODO: ¿mensaje avisando de que no se ha podido generar el arbol de decision?
				e.printStackTrace();
				return;
			}
    		
    		// DONE: añadir un numeral para evitar duplicados
    		int version=1;
    		String name= currentNetworkFrame.getTitle()+" PT ";
    		
    		if(versiones.containsKey(name)) {
    			version= versiones.get(name).intValue()+1;
    		}

    		versiones.put(name,new Integer(version));
    		name+= version;
    		
        	PolicyTreeFrame policyTreeFrame= new PolicyTreeFrame(policyTree, name, policyTreesBundle);
    		
    		elviraFrame.getSetPrecisionItem().setEnabled(true);    		
    		elviraFrame.enableMenusOpenNetworks(true, bnet);
    		
        	elviraFrame.getDesktopPane().add(policyTreeFrame);
        	policyTreeFrame.setVisible(true);
    		
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
    	PolicyTreeFrame policyTreeFrame= (PolicyTreeFrame) elviraFrame.getDesktopPane().getSelectedFrame();
    	PolicyTreeViewer tree= policyTreeFrame.getPolicyTreeViewer(); //.getTree();
    	
    	// Cuando se cambia el nivel de expansion, todo debe estar colapsado
    	for( int i=tree.getRowCount()-1; i>=0; i--) {
    		tree.collapseRow(i);
    	}
    	
    	int n= ((Integer) spinnerNivelExpansionPT.getValue()).intValue();
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
   /* public void comboChancePrecisionAction(ActionEvent event) {
    	JInternalFrame iframe= elviraFrame.getDesktopPane().getSelectedFrame();
    	if( !(iframe instanceof PolicyTreeFrame) ) {
    		return;
    	}
    	
    	String seleccion= (String) comboChancePrecision.getSelectedItem();
    	
    	PolicyTreeFrame policyTreeFrame= (PolicyTreeFrame) iframe;
		decisionTreeFrame.saveChancePrecisionIndex(comboChancePrecision.getSelectedIndex());
		
    	PolicyTreeViewer tree= policyTreeFrame.getPolicyTreeViewer(); //.getTree();
    	DecisionTreeCellRenderer renderer= (DecisionTreeCellRenderer) tree.getCellRenderer();

    	// 0.0001 -> 6 caracteres - 2 del '0.'
    	int len= seleccion.length()-2;
		if( len != renderer.getPrecisionProxy().getDefaultChancePrecision() ) {
			renderer.getPrecisionProxy().setDefaultChancePrecision(len);
			tree.getModel().fireAllNodesChanged();
		}
    }*/
   
    // Añadido por Jorge-PFC el 04/12/2005
    /**
     * @param event
     */
  /*  public void comboUtilityPrecisionAction(ActionEvent event) {
    	JInternalFrame iframe= elviraFrame.getDesktopPane().getSelectedFrame();
    	if( !(iframe instanceof PolicyTreeFrame) ) {
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
    	
    	PolicyTreeFrame policyTreeFrame= (PolicyTreeFrame) iframe;
		policyTreeFrame.saveUtilityPrecisionIndex(comboUtilityPrecision.getSelectedIndex());
		
    	// DONE: ¿Aun es necesaria esta comprobacion (treeviewer!=null)? creo q no, cada TreeFrame tiene su TreeViewer
		PolicyTreeViewer tree= policyTreeFrame.getPolicyTreeViewer(); //.getTree();
		PolicyTreeCellRenderer renderer= (PolicyTreeCellRenderer) tree.getCellRenderer();

		// 0.0001 -> 6 caracteres - 2 del '0.'
		int len= seleccion.length()-2;
		if( len != renderer.getPrecisionProxy().getDefaultUtilityPrecision() ) {
			renderer.getPrecisionProxy().setDefaultUtilityPrecision(len);
			tree.getModel().fireAllNodesChanged();
		}
    }*/
    
   /* public void setupCombos(PolicyTreeFrame.State state) {
    	comboChancePrecision.setSelectedIndex(state.precisionProbabilidades);
    	comboUtilityPrecision.setSelectedIndex(state.precisionUtilidades);
    }*/

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
    	if( !(iframe instanceof PolicyTreeFrame) ) {
    		return;
    	}
    	
		PolicyTreeViewer tree= ((PolicyTreeFrame) iframe).getPolicyTreeViewer(); //.getTree();
		
		PolicyTreeCellRenderer renderer= (PolicyTreeCellRenderer) tree.getCellRenderer();
		renderer.getDescriptionProxy().setUseTitleByDefault(byTitle);
		((PolicyTreeFrame) iframe).saveDescriptionByTitle(byTitle);
		
		tree.getModel().fireAllNodesChanged();
    }
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
        Object object = event.getSource();
        String cmd= event.getActionCommand();
		
//		if( cmd.equals("Create Policy Tree") ) {
//        	policyTreeAction (event);
//		}
     /*   else if( object == comboUtilityPrecision ) {
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
        }*/
     if( cmd.equals("By Title") ) {
            byTitleDescriptionChangedAction(true);
        }
        else if( cmd.equals("By Name") ) {
        	byTitleDescriptionChangedAction(false);;
        }
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent event) {
		Object object= event.getSource();
		
        // Añadido por Jorge-PFC el 28/11/2005
		if( object == spinnerNivelExpansionPT ) {
			nivelExpansionDTChange(event);
		}		
	}

	/**
	 * 
	 */
	public void setupBundle() {
        switch (Elvira.getLanguaje()) {
        	case Elvira.AMERICAN:
        		policyTreesBundle= ResourceBundle.getBundle("elvira/localize/PolicyTrees");
        		break;
        	case Elvira.SPANISH:
        		policyTreesBundle= ResourceBundle.getBundle("elvira/localize/PolicyTrees_sp");
        		break;
        }
	}
	
	/**
	 * @param policyTreeFrame
	 */
	public void setSpinnerModel(PolicyTreeFrame policyTreeFrame) {
		spinnerNivelExpansionPT.setModel(policyTreeFrame.getState().spinnerModelNivelExpansion);
	}
}
