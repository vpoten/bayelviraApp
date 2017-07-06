/*
		A basic implementation of the JDialog class.
*/

package elvira.gui.explication;

import java.awt.*;
import javax.swing.*;
import elvira.*;
import elvira.potential.*;
import elvira.gui.*;
import elvira.inference.clustering.*;
import java.util.ResourceBundle;
import java.text.*;

public class ExplainLinkDialog extends javax.swing.JDialog
{
	public ExplainLinkDialog(/*Frame parent,*/ Link l)
	{
		//super(parent);

		int languaje=Elvira.getLanguaje();
	   	
	   	switch (languaje) {
		   case 0: explanationBundle = ResourceBundle.getBundle ("elvira/ElviraFrame.localize/Explanation_sp");
		                         break;
		   case 1: explanationBundle = ResourceBundle.getBundle ("elvira/ElviraFrame.localize/Explanation");
		                        break;		                         
		}				
	
		setResizable(false);
		getContentPane().setLayout(null);
		setSize(550,218+(((FiniteStates)l.getHead()).getNumStates()*80));
		setVisible(true);
		setModal(true);
		relationLabel.setText(ElviraFrame.localize(explanationBundle,"RelationKind.label"));
		getContentPane().add(relationLabel);
		relationLabel.setBounds(10,60,150,24);
		destinoTextField.setEditable(false);
		getContentPane().add(destinoTextField);
		relationTextField.setBounds(160,60,168,24);
		relationTextField.setText(l.getKindofRelation());
		razonLabel.setText(ElviraFrame.localize(explanationBundle,"LikelihoodRatio.label"));
		getContentPane().add(razonLabel);
		razonLabel.setBounds(224,90,180,24);
		originLabel.setText(ElviraFrame.localize(explanationBundle,"Source.label"));
		getContentPane().add(originLabel);
		originLabel.setBounds(10,24,48,24);
		originTextField.setEditable(false);
		getContentPane().add(originTextField);
		originTextField.setBounds(72,24,168,24);
		destinoLabel.setText(ElviraFrame.localize(explanationBundle,"Destination.label"));
		getContentPane().add(destinoLabel);
		destinoLabel.setBounds(300,24,48,24);
		relationTextField.setEditable(false);
		getContentPane().add(relationTextField);
		destinoTextField.setBounds(372,24,168,24);
		propiedadesButton.setText(ElviraFrame.localize(explanationBundle,"ViewLinkProperties.label"));
		propiedadesButton.setActionCommand("Ver Propiedades");
		propiedadesButton.setMnemonic((int)'V');
		getContentPane().add(propiedadesButton);
		System.out.println("Altura "+getContentPane().getBounds().height);
		propiedadesButton.setBounds(100,getContentPane().getBounds().height-50,188,40);
		closeButton.setText(ElviraFrame.localize(explanationBundle,"Close.label"));
		closeButton.setActionCommand("Close");
		closeButton.setMnemonic((int)'C');
		getContentPane().add(closeButton);
    	closeButton.setBounds(324,getContentPane().getBounds().height-50,108,38);
		estadosLabel=new javax.swing.JLabel[((FiniteStates)l.getHead()).getNumStates()];
		razonScrollPane=new javax.swing.JScrollPane[((FiniteStates)l.getHead()).getNumStates()];
		razonTextArea=new javax.swing.JTextArea[((FiniteStates)l.getHead()).getNumStates()];
		int dist=120;
		for (int st=0; st<estadosLabel.length; st++){
		    razonScrollPane[st]=new JScrollPane();
		    razonScrollPane[st].setOpaque(true);
		    getContentPane().add(razonScrollPane[st]);
		    razonScrollPane[st].setBounds(110,dist,380,80);
		    razonTextArea[st]=new JTextArea();
		    razonTextArea[st].setEditable(false);
		    razonScrollPane[st].getViewport().add(razonTextArea[st]);
		    razonTextArea[st].setBounds(0,0,377,80);
		    estadosLabel[st]=new javax.swing.JLabel();
		    estadosLabel[st].setText(((FiniteStates)l.getHead()).getState(st));
		    estadosLabel[st].setBounds(20,dist,80,30);		    
		    getContentPane().add(estadosLabel[st]);
		    dist=dist+85;
		}
		setTitle("Explicación del enlace ");
		current=l;
		bayesnet=((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getEditorPanel().getBayesNet();		
		setHeadLink();
		setTailLink();
        setlikelihood();
    	
		//}}

		setLocationRelativeTo(Elvira.getElviraFrame());
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		closeButton.addActionListener(lSymAction);
		propiedadesButton.addActionListener(lSymAction);
		
		//}}
	}

	public void setVisible(boolean b)
	{
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
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
	javax.swing.JLabel relationLabel = new javax.swing.JLabel();
	javax.swing.JTextField destinoTextField = new javax.swing.JTextField();
	javax.swing.JLabel razonLabel = new javax.swing.JLabel();
	javax.swing.JLabel originLabel = new javax.swing.JLabel();
	javax.swing.JTextField relationTextField = new javax.swing.JTextField();
	javax.swing.JLabel destinoLabel = new javax.swing.JLabel();
	javax.swing.JTextField originTextField = new javax.swing.JTextField();
	javax.swing.JScrollPane[] razonScrollPane;// = new javax.swing.JScrollPane();
	javax.swing.JTextArea[] razonTextArea;// = new javax.swing.JTextArea();
	javax.swing.JButton propiedadesButton = new javax.swing.JButton();
	javax.swing.JButton closeButton = new javax.swing.JButton();
	javax.swing.JLabel[] estadosLabel;
	//}}
	ResourceBundle explanationBundle;
   
   Link current;
   Bnet bayesnet;
   double[] priori;

	public void setHeadLink(){
		StringBuffer head=new StringBuffer("");
        FiniteStates pfs=(FiniteStates)current.getHead();
        if (pfs.getTitle().equals(""))
	        head.append(pfs.getName()+" ");
	        else head.append(pfs.getTitle()+" ");
        destinoTextField.setText(head.toString());
    }

	public void setTailLink(){
		StringBuffer tail=new StringBuffer("");
        FiniteStates pfs=(FiniteStates)current.getTail();
        if (pfs.getTitle().equals(""))
	        tail.append(pfs.getName()+" ");
	        else tail.append(pfs.getTitle()+" ");
        originTextField.setText(tail.toString());
    }
    
    public void setlikelihood(){
        StringBuffer salida=new StringBuffer("");
        FiniteStates tail=(FiniteStates) current.getTail();
        FiniteStates head=(FiniteStates) current.getHead();

        if (((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getMode()==NetworkFrame.EDITOR_ACTIVE){
            salida.append("En modo edición no calculo la R.V.");        
            System.out.println("En modo edición no calculo la R.V.");
        }
            else if (((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getMode()==NetworkFrame.INFERENCE_ACTIVE){
                    //si solo tiene un padre, simplemente se hacen cálculos sobre las tablas
                    if (head.getParents().size()==1){
                        Relation rhead=bayesnet.getRelation(head);
                        PotentialTable pots=(PotentialTable) rhead.getValues();
                        int numval=0;
                        double[] prob;
                        for (int nums=0; nums<head.getNumStates(); nums++){
                            prob=new double[tail.getNumStates()];
//                            salida.append("Para el estado "+head.getState(nums)+"\n");
                            for (int p=0; p<tail.getNumStates(); p++){
                                prob[p]=pots.getValue(numval);
                                numval++;
                            }
                            salida.append(exp_razonProb(prob, tail));
                            razonTextArea[nums].setText(salida.toString());
                            salida=new StringBuffer("");
                        }
                    }
                    else {//si tiene más de uno, se instancia la "cola" del enlace para calcular probs.
                        double[][] prob=new double[head.getNumStates()][tail.getNumStates()];

                        for (int nums=0; nums<head.getNumStates(); nums++){
                            Evidence evid =((NetworkFrame) Elvira.getElviraFrame().getCurrentNetworkFrame()).getInferencePanel().getCasesList().getCurrentCase().getEvidence();
                            HuginPropagation prop=new HuginPropagation(bayesnet);
                            int statetail=-1;
                            if (evid.indexOf(tail.getName())!=-1)
                                statetail=evid.getValue(tail.getName());
                            for (int p=0; p<tail.getNumStates(); p++){
                                evid.putValue(tail,p);
                                prop = new HuginPropagation(bayesnet,evid,"tables");
                                prop.obtainInterest();                                                                                                                                                                                                                                                                                              
                                prop.propagate(prop.getJoinTree().elementAt(0),"si");
                                boolean found = false;
    	                        int  ip= 0;
	                            while (!found && ip<prop.results.size()) {
	      	                        Potential pots = (Potential) prop.results.elementAt(ip);
                                    if (head.equals(((FiniteStates)pots.getVariables().elementAt(0)))){
                                        found=true;
                                        for (int r=0; r<head.getNumStates(); r++)
                                            prob[r][p]= ((PotentialTable) pots).getValues()[r];
                                    }
                                    else ip++;
	                            }
                                System.out.println();
                            }
                            if (statetail!=-1)
                                evid.putValue(tail,statetail);
                                else evid.remove(evid.indexOf(tail.getName()));
                            prop = new HuginPropagation(bayesnet,evid,"tables");
                            prop.obtainInterest();
                            prop.propagate(prop.getJoinTree().elementAt(0),"si");
                        }
                        for (int nums=0; nums<head.getNumStates(); nums++){
//                             salida.append("Para el estado "+head.getState(nums)+" de "+head.getTitle()+" ");
                             salida.append(exp_razonProb(prob[nums], tail));
                             razonTextArea[nums].setText(salida.toString());
                             salida=new StringBuffer("");
                        }
                    }
            }
            //razonTextArea.setText(salida.toString());
    }

    public String exp_razonProb(double[] p, FiniteStates tail){
        StringBuffer salida=new StringBuffer("");
        salida.append(razonprob(p, tail));
        return salida.toString();
    }

    public String razonprob(double[] pr, FiniteStates current){
    /** Calcula la razón de probabilidad a priori de un nodo y genera una cadena que explica
     * dicho valor. Si es binario, solo hay una razón; si tiene n valores, habrá n sobre 2
     */
        NumberFormat nf=new DecimalFormat("0.00");
        StringBuffer salida=new StringBuffer ();
        double razon;
        if (pr.length==2){
            razon=pr[0]/pr[1];
  //          System.out.println("Razon:"+razon);
            salida.append("La R. V. de los estados de "+current.getTitle()+" es: \n");
            if (razon>1){
                salida.append("MAYOR QUE 1, por lo que "+"\n");
                salida.append("el estado "+String.valueOf(current.getStates().elementAt(0))+ " ");
                salida.append("lo explica mejor que el estado "+String.valueOf(current.getStates().elementAt(1))+".\n");
                salida.append("En concreto, " +nf.format(razon)+" veces mejor");
            }
            else if (razon<1){
            	razon=pr[1]/pr[0];
                salida.append("MENOR QUE 1, lo que implica que "+"\n");
                salida.append("el estado "+String.valueOf(current.getStates().elementAt(1))+" ");
                salida.append("lo explica mejor que el estado "+String.valueOf(current.getStates().elementAt(0))+".\n");
                salida.append("En concreto, " +nf.format(1/razon)+" veces mejor");
            }
            else {
                salida.append("IGUAL QUE 1, es decir, \nambos valores son igual de probables");
            }
        }
        else {//(node.getNumstates()>2)
              /** En este caso, ordenamos los valores de mayor a menor y calculamos las razones
               * teniendo en cuenta dicha ordenación
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
              for (int ne=0; ne<current.getNumStates()-1; ne++){
                salida.append("el estado :"+String.valueOf(current.getStates().elementAt(posis[ne]))+"\n");
                for (int e=ne+1; e<current.getNumStates(); e++){
                    razon=ord[ne]/ord[e];
                    salida.append("lo explica "+nf.format(razon)+" veces mejor que :"+String.valueOf(current.getStates().elementAt(posis[e])));
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
	
	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == closeButton)
				closeButton_actionPerformed(event);
			else if (object == propiedadesButton)
				propiedadesButton_actionPerformed(event);
		}
	}

	void closeButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		dispose();			 
	}

	void propiedadesButton_actionPerformed(java.awt.event.ActionEvent event)
	{
	   LinkPropertiesDialog d = new LinkPropertiesDialog(current,false);
	   d.show();
	}
}
