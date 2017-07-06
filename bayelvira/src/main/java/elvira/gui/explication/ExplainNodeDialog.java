/*
		A basic implementation of the JDialog class.
*/

package elvira.gui.explication;

import java.awt.*;
import java.util.*;
import java.text.*;
import elvira.*;
import elvira.potential.*;
import elvira.gui.*;
import java.util.ResourceBundle;


public class ExplainNodeDialog extends javax.swing.JDialog
{
	public ExplainNodeDialog(Frame parent)
	{
		super(parent);
	    
	    int languaje=Elvira.getLanguaje();
	   	
	   	switch (languaje) {
		   case Elvira.SPANISH: explanationBundle = ResourceBundle.getBundle ("elvira/localize/Explanation_sp");
		                         break;
		   case Elvira.AMERICAN: explanationBundle = ResourceBundle.getBundle ("elvira/localize/Explanation");
		                        break;		                         
		}				

		
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setModal(true);
		setResizable(false);
		getContentPane().setLayout(null);
		setSize(600,400);
		setVisible(false);
		nameLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
		nameLabel.setText(ElviraFrame.localize(explanationBundle,"Name.label"));
		getContentPane().add(nameLabel);
		nameLabel.setBounds(12,12,48,24);
		nameTextField.setEditable(false);
		getContentPane().add(nameTextField);
		nameTextField.setBounds(72,12,120,24);

		statesTextField.setEditable(false);
		getContentPane().add(statesTextField);
		statesTextField.setBounds(262,12,254,24);
		statesLabel.setText(ElviraFrame.localize(explanationBundle,"States.label"));
		getContentPane().add(statesLabel);
		statesLabel.setBounds(212,12,48,24);
		
		causeLabel.setToolTipText(ElviraFrame.localize(explanationBundle,"Causes.tip"));
		causeLabel.setText(ElviraFrame.localize(explanationBundle,"Causes.label"));
		causeLabel.setBounds(288,52,48,24);
		getContentPane().add(causeLabel);
		//causeList.setModel(stringListModel1);
		causeList.setEditable(false);
		causeList.setBounds(336,52,100,20);
        getContentPane().add(causeList);
        
        gotocauseButton.setBounds(450,52,120,20);
        gotocauseButton.setText(ElviraFrame.localize(explanationBundle, "gocause"));
        getContentPane().add(gotocauseButton);

        ExplainAction expAction = new ExplainAction();
		causeList.addActionListener(expAction);
		gotocauseButton.addActionListener(expAction);
		gotosymptomButton.addActionListener(expAction);
		
		symptomLabel.setToolTipText(ElviraFrame.localize(explanationBundle,"Effects.tip"));
		symptomLabel.setText(ElviraFrame.localize(explanationBundle,"Effects.label"));
		getContentPane().add(symptomLabel);
		symptomLabel.setBounds(288,114,48,24);
		symptomList.setEditable(false);
		symptomList.setBounds(336,114,100,20);
        getContentPane().add(symptomList);

        gotosymptomButton.setBounds(450,114,120,20);
        gotosymptomButton.setText(ElviraFrame.localize(explanationBundle, "goeffect"));
        getContentPane().add(gotosymptomButton);


    	orPreLabel.setToolTipText(ElviraFrame.localize(explanationBundle,"Pre_test.tip"));
		orPreLabel.setText(ElviraFrame.localize(explanationBundle,"Pre_test.label"));
		getContentPane().add(orPreLabel);
		orPreLabel.setBounds(12,178,88,44);
		orPostLabel.setToolTipText(ElviraFrame.localize(explanationBundle,"Pos_test.tip"));
		orPostLabel.setText(ElviraFrame.localize(explanationBundle,"Pos_test.label"));
		getContentPane().add(orPostLabel);
		orPostLabel.setBounds(12,248,100,44);
		propertiesButton.setText(ElviraFrame.localize(explanationBundle,"ViewNodeProperties.label"));
		propertiesButton.setActionCommand("View Node Properties");
		propertiesButton.setMnemonic((int)'V');
		getContentPane().add(propertiesButton);
		propertiesButton.setBounds(108,350,180,40);
		closeButton.setText(ElviraFrame.localize(explanationBundle,"Close.label"));
		closeButton.setMnemonic((int)'C');
		getContentPane().add(closeButton);
		closeButton.setBounds(336,350,96,40);
		definitionLabel.setText(ElviraFrame.localize(explanationBundle,"Definition.label"));
		getContentPane().add(definitionLabel);
		definitionLabel.setBounds(12,52,60,24);
		getContentPane().add(definitionScrollPane);
		definitionScrollPane.setBounds(72,52,210,110);
		definitionTextArea.setEditable(false);
		definitionTextArea.setWrapStyleWord(true);
		definitionTextArea.setText("");
		definitionTextArea.setEditable(true);
		definitionTextArea.setLineWrap(true);
		definitionScrollPane.getViewport().add(definitionTextArea);
		definitionTextArea.setBounds(0,0,440,110);
		getContentPane().add(orPreScrollPane);
		orPreScrollPane.setBounds(130,175,444,75);
		orPreTextArea.setEditable(false);
		orPreTextArea.setWrapStyleWord(true);
		orPreTextArea.setLineWrap(true);
		orPreScrollPane.getViewport().add(orPreTextArea);
		orPreTextArea.setBounds(0,0,441,70);
		getContentPane().add(orPostScrollPane);
		orPostScrollPane.setBounds(130,265,444,75);
		orPostTextArea.setEditable(false);
		orPostTextArea.setWrapStyleWord(true);
		orPostScrollPane.getViewport().add(orPostTextArea);
		orPostTextArea.setBounds(0,0,439,70);
		//}}

		setLocationRelativeTo(Elvira.getElviraFrame());
	
		//{{REGISTER_LISTENERS
		propertiesButton.addActionListener(expAction);
		closeButton.addActionListener(expAction);
		//}}
				
	}

	public ExplainNodeDialog()
	{
		this((Frame)null);
	}

	public ExplainNodeDialog(String sTitle)
	{
		this();
		setTitle(sTitle);
	}		

	public ExplainNodeDialog (FiniteStates n, String sTitle)
	{
		this();
		current = n;
		setTitle(ElviraFrame.localize(explanationBundle,"ExplainNode")+" "+sTitle);
		bayesnet=((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().getBayesNet();	   
		setNameNode();
		setStatesNode();
		setDefinitionNode();
		if (!current.getTitle().equals(""))
		    sTitle=current.getTitle();
		setCausesNode(sTitle);
		setSymptomsNode(sTitle);
		setOrPreNode();		
		setOrPostNode();		
	}

	public void setVisible(boolean b)
	{
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
	}

	static public void main(String args[])
	{
		(new ExplainNodeDialog()).setVisible(true);
	}

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JLabel nameLabel = new javax.swing.JLabel();
	javax.swing.JTextField nameTextField = new javax.swing.JTextField();
	javax.swing.JLabel causeLabel = new javax.swing.JLabel();
	javax.swing.DefaultComboBoxModel causecombomodel=new javax.swing.DefaultComboBoxModel();
	javax.swing.JComboBox causeList= new javax.swing.JComboBox(causecombomodel);
	javax.swing.DefaultComboBoxModel symptomcombomodel=new javax.swing.DefaultComboBoxModel();
	javax.swing.JComboBox symptomList = new javax.swing.JComboBox(symptomcombomodel);
	javax.swing.JLabel symptomLabel = new javax.swing.JLabel();
	javax.swing.JTextField statesTextField = new javax.swing.JTextField();
	javax.swing.JLabel statesLabel = new javax.swing.JLabel();
	javax.swing.JLabel definitionLabel = new javax.swing.JLabel();
	javax.swing.JLabel orPreLabel = new javax.swing.JLabel();
	javax.swing.JLabel orPostLabel = new javax.swing.JLabel();
	javax.swing.JButton propertiesButton = new javax.swing.JButton();
	javax.swing.JButton gotocauseButton = new javax.swing.JButton();
	javax.swing.JButton gotosymptomButton = new javax.swing.JButton();
	javax.swing.JButton closeButton = new javax.swing.JButton();
	javax.swing.JScrollPane definitionScrollPane = new javax.swing.JScrollPane();
	javax.swing.JTextArea definitionTextArea = new javax.swing.JTextArea();
	javax.swing.JScrollPane orPreScrollPane = new javax.swing.JScrollPane();
	javax.swing.JTextArea orPreTextArea = new javax.swing.JTextArea();
	javax.swing.JScrollPane orPostScrollPane = new javax.swing.JScrollPane();
	javax.swing.JTextArea orPostTextArea = new javax.swing.JTextArea();
	
	ResourceBundle explanationBundle;
	//}}
	
   private FiniteStates current;
   private Bnet bayesnet;
   private double[] priori,posteriori;
	
	public void setNameNode(){
		if (current.getTitle().equals(""))
		    nameTextField.setText(current.getName());
		    else nameTextField.setText(current.getTitle());
	}
	
	public void setStatesNode(){
		FiniteStates fs=(FiniteStates)current;
		statesTextField.setText(fs.getStates().toString());
    }
	
	public void setDefinitionNode(){
		FiniteStates fs=(FiniteStates)current;
		definitionTextArea.setText(fs.getComment());
	}

	public void setCausesNode(String nodeTitle){
//	    System.out.println("titulodelnodo:"+nodeTitle);
        NodeList nl=bayesnet.getNodeList();
        for (int p=0; p<nl.size(); p++){
             String nodep=((Node)nl.elementAt(p)).getTitle();
//             System.out.println("t�tulo: "+nodep);
             if (nodep==null || nodep.equals(""))
                 nodep=((Node)nl.elementAt(p)).getName();
             if (nodeTitle.equals(nodep)){
      //                f.setNode((FiniteStatesNode)nl.elementAt(p));
                 if (causecombomodel.getSize()>0) causecombomodel.removeAllElements();
                 FiniteStates fs=(FiniteStates) nl.elementAt(p);
           		 Enumeration parentsfs=fs.getParents().elements();
		         while (parentsfs.hasMoreElements()){
//		                System.out.println("Otro padre");
		                Link l=(Link) parentsfs.nextElement();
		                FiniteStates pfs=(FiniteStates)l.getTail();
		                if (pfs.getTitle().equals(""))
		                    causecombomodel.addElement(pfs.getName());
		                else causecombomodel.addElement(pfs.getTitle());
                 }
             }
      }
    }

    
    public void setSymptomsNode(String nodeName){
       if (nodeName.equals("")){
           if (symptomcombomodel.getSize()>0) symptomcombomodel.removeAllElements(); 
               symptomcombomodel.addElement(" ");
       }
       else{
                    NodeList nl=bayesnet.getNodeList();
                    for (int p=0; p<nl.size(); p++){
                        String nodep=((Node)nl.elementAt(p)).getTitle();
                        if (nodep==null || nodep.equals(""))
                            nodep=((Node)nl.elementAt(p)).getName();
                        if (nodeName.equals(nodep)){
      //                f.setNode((FiniteStatesNode)nl.elementAt(p));
                            if (symptomcombomodel.getSize()>0) symptomcombomodel.removeAllElements();
                            FiniteStates fs=(FiniteStates) nl.elementAt(p);
                    		Enumeration childrenfs=fs.getChildren().elements();
		                    while (childrenfs.hasMoreElements()){
		                        Link l=(Link) childrenfs.nextElement();
		                        FiniteStates pfs=(FiniteStates)l.getHead();
		                        if (pfs.getTitle().equals(""))
		                            symptomcombomodel.addElement(pfs.getName());
		                        else symptomcombomodel.addElement(pfs.getTitle());
		                    }
		                }
		            }
	   }
	}
                            
    
    
    public void setOrPreNode(){
        String texto="";
        if (bayesnet.getIsCompiled()){
		    Vector pot=bayesnet.getCompiledPotentialList();
		    boolean found=false;
		    int posp=0;
		    while (!found && posp<pot.size()){
    		    PotentialTable potn=(PotentialTable) pot.elementAt(posp);
    		    if (current.equals((FiniteStates)potn.getVariables().elementAt(0))){
    		        priori=potn.getValues();
    		        found=true;
    		    }
    		    else posp++;
    	    }
		    texto=exp_razonProb(priori);
            
        }
        else texto=ElviraFrame.localize(explanationBundle, "noCompiled");
        orPreTextArea.setText(texto);
    }
 

    public void setOrPostNode(){
        if (((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getMode()==NetworkFrame.EDITOR_ACTIVE ||
            ((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getInferencePanel().getCasesList().getNumStoredCases()==0)
            orPostTextArea.setText(ElviraFrame.localize(explanationBundle, "noInference"));    
            else {
                  Case active=((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getInferencePanel().getCasesList().getActiveCase();
                  if (!active.getPropagated())
                     orPostTextArea.setText(ElviraFrame.localize(explanationBundle, "noInference"));    
                  else {
		                posteriori=active.getProbOfNode(current);
		                String texto=exp_razonProb(posteriori);
                        orPostTextArea.setText(texto);
                  }
            }
        
    }
 
/*
     public String explicar(double[] prob){
        //Rehacerlo con los intervalos propuestos por Elsaesser
        StringBuffer salida=new StringBuffer("\n");
        String estado, texto;
        int[] states=current.getStates();
        for (int i=0; i<current.getNumStates(); i++){
            estado=new String(" El valor "+String.valueOf(states[i]));
            if ((int)prob[i]<=20)
                texto=new String(" es muy poco probable"+"\n");
                else if (((int) prob[i]>20) && ((int) prob[i]<=40))
                         texto=new String(" es poco probable"+"\n");
                else if (((int) prob[i]>40) && ((int) prob[i]<=50))
                         texto=new String(" es probable"+"\n");
                else if (((int) prob[i]>50) && ((int) prob[i]<=70))
                         texto=new String(" es algo probable"+"\n");
                else if (((int) prob[i]>70) && ((int) prob[i]<=90))
                         texto=new String(" es muy probable"+"\n");
                else if (((int) prob[i]>90))
                         texto=new String(" es casi seguro"+"\n");
                     else texto=new String("");
        salida.append(estado);
        salida.append(texto);
        }
        salida.append("\n");
        return salida.toString();
    }
  */  

    public String exp_razonProb(double[] p){
        StringBuffer salida=new StringBuffer("");
        salida.append(razonprob(p));
        return salida.toString();
    }

    public String razonprob(double[] pr){
    /** Calcula la raz�n de probabilidad a priori de un nodo y genera una cadena que explica
     * dicho valor. Si es binario, solo hay una raz�n; si tiene n valores, habr� n sobre 2
     */
        NumberFormat nf=new DecimalFormat("0.00");
        StringBuffer salida=new StringBuffer (ElviraFrame.localize(explanationBundle, "ProbRatio")+": \n");
        double razon;
        if (current.getNumStates()==2){
            razon=pr[0]/pr[1];
//            System.out.println("Razon:"+razon);
            if (razon>1){
                salida.append(ElviraFrame.localize(explanationBundle, "greater"));
                salida.append(" "+ElviraFrame.localize(explanationBundle, "state")+" "+String.valueOf(current.getStates().elementAt(0))+"\n");
                salida.append(ElviraFrame.localize(explanationBundle, "morep")+" "+String.valueOf(current.getStates().elementAt(1))+".\n");
                salida.append(ElviraFrame.localize(explanationBundle, "part") +" "+nf.format(razon)+" "+ElviraFrame.localize(explanationBundle, "times"));
            }
            else if (razon<1){
            	razon=pr[1]/pr[0];
                salida.append(ElviraFrame.localize(explanationBundle, "less"));
                salida.append(" "+ElviraFrame.localize(explanationBundle, "state")+" "+String.valueOf(current.getStates().elementAt(1))+"\n");
                salida.append(ElviraFrame.localize(explanationBundle, "morep")+" "+String.valueOf(current.getStates().elementAt(0))+".\n");
                salida.append(ElviraFrame.localize(explanationBundle, "part") +" "+nf.format(razon)+" "+ElviraFrame.localize(explanationBundle, "times"));
            }
            else {
           		salida.append(ElviraFrame.localize(explanationBundle, "equals"));
            }
        }
        else {//(node.getNumstates()>2)
              /** En este caso, ordenamos los valores de mayor a menor y calculamos las razones
               * teniendo en cuenta dicha ordenaci�n
               */
              double[] ord=new double[current.getNumStates()];
              int[] posis=new int[current.getNumStates()];
              for (int ne=0; ne<current.getNumStates(); ne++){
                ord[ne]=pr[ne];
                posis[ne]=ne;
              }
              for (int ne=1; ne<current.getNumStates(); ne++){
                for (int e=0; e<current.getNumStates()-1; e++){
                    if (ord[e]<ord[e+1]){
                        double aux=ord[e];
                        ord[e]=ord[e+1];
                        ord[e+1]=aux;
                        int aux2=posis[e];
                        posis[e]=posis[e+1];
                        posis[e+1]=aux2;
                    }
                }
              }

              double maxrazon=0;
              int posne,pose;
              salida.append(ElviraFrame.localize(explanationBundle, "order")+"\n");
              for (int ne=0; ne<current.getNumStates(); ne++){
                salida.append(ElviraFrame.localize(explanationBundle, "Position")+" "+String.valueOf(ne)+"--> "+ElviraFrame.localize(explanationBundle, "State")+": "+String.valueOf(current.getStates().elementAt(posis[ne]))+"\n");
                for (int e=ne+1; e<current.getNumStates(); e++){
                    razon=ord[ne]/ord[e];
                    salida.append(ElviraFrame.localize(explanationBundle, "is")+" "+nf.format(razon)+" "+ElviraFrame.localize(explanationBundle, "timesm")+" "+String.valueOf(current.getStates().elementAt(posis[e]))+"\n");
                    if (razon>maxrazon){
                        maxrazon=razon;
                        posne=ne;
                        pose=e;
                    }
                }
              }
        }

        return salida.toString();
    }
	

       class ExplainAction implements java.awt.event.ActionListener{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == propertiesButton)
				propertiesButton_actionPerformed(event);
			else if (object == closeButton)
				closeButton_actionPerformed();
				else if (object == gotocauseButton)
    			         gotocauseButton_actionPerformed(event);  
                     else if (object == gotosymptomButton)
				          gotosymptomButton_actionPerformed(event);      			         
		}
	}

   
	void propertiesButton_actionPerformed(java.awt.event.ActionEvent event)
	{
	   EditVariableDialog edit=new EditVariableDialog(current, 
	        true, "Node Properties", false);
	   edit.show();			 
	}

	void closeButton_actionPerformed()
	{
		current.setComment(definitionTextArea.getText());
		dispose();			 
	}
	
	
	void gotocauseButton_actionPerformed(java.awt.event.ActionEvent event){
	    String nodeName=(String) causeList.getSelectedItem();
	    if (nodeName!=null){
	        FiniteStates fs=(FiniteStates) bayesnet.getNodeList().getNodeString(nodeName, true);
	        dispose();
	        new ExplainNodeDialog(fs,nodeName).show();
	    }
	}

		void gotosymptomButton_actionPerformed(java.awt.event.ActionEvent event){
	    String nodeName=(String) symptomList.getSelectedItem();
	    if (nodeName!=null){
	        FiniteStates fs=(FiniteStates) bayesnet.getNodeList().getNodeString(nodeName, true);
	        dispose();
	        new ExplainNodeDialog(fs,nodeName).show();
	    }
	}

}