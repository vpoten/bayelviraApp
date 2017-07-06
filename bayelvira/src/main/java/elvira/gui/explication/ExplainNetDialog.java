package elvira.gui.explication;

import elvira.*;
import java.util.*;


public class ExplainNetDialog extends javax.swing.JDialog{
    
	private Bnet bnet;
	
	private ResourceBundle explanationBundle;
	
	public ExplainNetDialog(Bnet b){
		// This line prevents the "Swing: checked access to system event queue" message seen in some browsers.
		
   	switch (Elvira.getLanguaje()) {
		   case Elvira.AMERICAN: explanationBundle = ResourceBundle.getBundle ("elvira/localize/Explanation");            		                         
		                         break;
		   case Elvira.SPANISH: explanationBundle = ResourceBundle.getBundle ("elvira/localize/Explanation_sp");   		                        
		                        break;		                         
		}				
		bnet=b;
//		System.out.println("Entra en el constructor");
		getContentPane().setLayout(null);
		setSize(600,500);
		setTitle(Elvira.localize(explanationBundle, "ExplainNet"));
		getContentPane().add(JScrollPane1);
		JScrollPane1.setBounds(12,12,550,400);
		JScrollPane1.getViewport().add(JEditorPane1);
//		JScrollPane1.setResizable(true);
		JEditorPane1.setBounds(0,0,450,400);
		fillEditor();
	}
	
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JEditorPane JEditorPane1 = new javax.swing.JEditorPane();
	
	String causesdis;
	String effectsdis;
	String riskfactors;
	String symptoms;
	String signs;
	String tests;
	String auxiliary;
	String others;

	void preparedata(){
	    causesdis=new String("");
	    effectsdis=new String("");
	    riskfactors=new String("");
	    symptoms=new String("");
	    signs=new String("");
	    tests=new String("");
	    auxiliary=new String("");
	    others=new String("");
	}

	void classifynodes(Vector asc, boolean up){
        for (int n=0; n<asc.size(); n++){
	        String nodoasc=(((Node) asc.elementAt(n)).getPurpose());
	        if (nodoasc.equals("Disease")){
	        	if (up)
	            causesdis=causesdis+(((Node) asc.elementAt(n)).getTitle()+", ");
	            else effectsdis=effectsdis+(((Node) asc.elementAt(n)).getTitle()+", ");
	        }
            else if (nodoasc.equals("Symptom"))
	                 symptoms=symptoms+(((Node) asc.elementAt(n)).getTitle()+", ");
            else if (nodoasc.equals("Sign"))
            		 signs=signs+(((Node) asc.elementAt(n)).getTitle()+", ");	                    
            else if (nodoasc.equals("Riskfactor"))
            		 riskfactors=riskfactors+(((Node) asc.elementAt(n)).getTitle()+", ");	                    
            else if (nodoasc.equals("Test"))
	                 tests=tests+(((Node) asc.elementAt(n)).getTitle()+", ");	                    
            else if (nodoasc.equals("Aux"))
            		 auxiliary=auxiliary+(((Node) asc.elementAt(n)).getTitle()+", ");	                    
            else if (nodoasc.equals("Other"))
	                 others=others+(((Node) asc.elementAt(n)).getTitle()+", ");	                    
	    }
	}
	
	String filltext(String nodename){
		
	     String enf;
	     if (!(causesdis.toString()).equals("") || !(riskfactors.toString()).equals("")){
	     	enf=new String("\n"+Elvira.localize(explanationBundle, "disease")+" "+nodename+" "+Elvira.localize(explanationBundle, "has")+"\n");
         	if (!(causesdis.toString()).equals(""))
         		enf=enf+(Elvira.localize(explanationBundle, "diseases")+":\n"+causesdis+"\n");
         	if (!(riskfactors.toString()).equals("")) 
         		enf=enf+(Elvira.localize(explanationBundle, "riskfactors")+":\n"+riskfactors+"\n");
         }
         else enf=new String("\n"+Elvira.localize(explanationBundle, "disease")+nodename+Elvira.localize(explanationBundle, "hasno")+" "+"\n");
         if (!(causesdis.toString()).equals("") || (!(symptoms.toString()).equals("") || !(signs.toString()).equals(""))){
         		enf=enf+(Elvira.localize(explanationBundle, "hasym"));
         	if (!(effectsdis.toString()).equals(""))
         		enf=enf+(Elvira.localize(explanationBundle, "diseases")+":\n"+effectsdis+"\n");
         	if (!(symptoms.toString()).equals("")) 
         		enf=enf+(Elvira.localize(explanationBundle, "symptoms")+":\n"+symptoms+"\n");         
         	if (!(signs.toString()).equals("")) 
         		enf=enf+(Elvira.localize(explanationBundle, "signs")+":\n"+signs+"\n");         
         }
         else enf=enf+(Elvira.localize(explanationBundle, "hasnosym")+":\n");
         if (!(tests.toString()).equals("")){
            enf=enf+(Elvira.localize(explanationBundle,"tests")+":\n");
            enf=enf+(tests+"\n");
         }
         else enf=enf+("notest"+"\n");
         if (!(others.toString()).equals("")){
            enf=enf+("other"+"\n");
            enf=enf+(others+"\n");
         }
         if (!(auxiliary.toString()).equals("")){
            enf=enf+(Elvira.localize(explanationBundle, "aux")+":\n");
            enf=enf+(auxiliary+"\n\n");
         }
         
         return enf;
         
	}
	    
	void fillEditor(){
	    
	    String texto=new String("");
	    
	    texto=texto+(Elvira.localize(explanationBundle, "Net")+" "+ bnet.getName()+" "+Elvira.localize(explanationBundle, "repinf")+":\n");
	    
	    Enumeration nodos=bnet.getNodeList().elements();
	    
	    while (nodos.hasMoreElements()){
	    
	        Node nodo=(Node) nodos.nextElement();
	        preparedata();
	        if (nodo.getPurpose().equals("Disease")){
	            classifynodes(bnet.ascendants(nodo), true);
	            classifynodes(bnet.descendants(nodo), false);	            
	            texto=texto+(filltext(nodo.getTitle()));
	        }
	     }
	    JEditorPane1.setFont(new java.awt.Font("Arial", 0, 14));    
	    JEditorPane1.setText(texto.toString());
     }
     
}//end of class ExplainNetDialog	
	