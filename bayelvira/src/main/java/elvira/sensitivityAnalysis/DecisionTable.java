/*DecisionTable.java*/

package elvira.sensitivityAnalysis;

import java.util.*;
import java.io.*;

import elvira.*;
import elvira.potential.*;
import elvira.parser.ParseException;
import java.io.IOException;

/**
 * Esta clase se encarga de obtener los valores de la tabla de una decision
 */
public class DecisionTable {

	Vector potentVector;
  	Node theNode;
  	Potential thePot;
  	private Vector configuraciones;
	private Vector statesOfDecision;
	private Vector valores;

	/*
	 * Constructor con el nodo de decision y el diagrama
	 * @param node Nodo de decision
	 * @param bnet Red
	 */
	public DecisionTable(Node node, Bnet bnet) {

		Vector potVector;

    		potVector = bnet.getCompiledPotentialList();

		statesOfDecision = new Vector();
		configuraciones = new Vector();
		valores = new Vector();

    		if ((node==null) || (potVector==null)) {
     			System.out.println("null parameters in DecisionTable!!!!!!!");
      		System.exit(1);
   	 	}
    
    		theNode = node;
    		potentVector = potVector;

    		Potential pot = null;
	
		for (int i = 0; i < potVector.size(); i++) {
			if (potVector.elementAt(i) != null) {
				if (((Node) ((Potential) potVector.elementAt(i))
					.getVariables()
					.elementAt(
						((Potential) potVector.elementAt(i))
							.getVariables()
							.size()
							- 1))
					.getName()
					.equals(node.getName())) {
					pot = (Potential) potVector.elementAt(i);
					break;
				}
			}
		}
    
    		thePot = pot;
    
		generateDecisionTableUtilities(theNode,thePot);

	}

	private void generateDecisionTableUtilities(Node node, Potential pot) {
	
    		Vector leftColumn = new Vector();//Cadenas con los nombres de la configuracion
    		Vector states;//Estados posibles de la decisi¢n
    		NodeList headerNodes;//Nombres de los nodos de la configuracion
    		int rowLines, rows;

    		states = new Vector();
    		rowLines = ((FiniteStates) node).getStates().size();
    		for (int i=0; i<rowLines; i++) {
      		states.addElement(withoutQm(((FiniteStates) node).getState(i)));
    		}

		statesOfDecision = states;

    		Vector nodes = new Vector();

    		for (int i=0; i<pot.getVariables().size(); i++) {
      		if (!((Node) pot.getVariables().elementAt(i)).getName().equals(node.getName())) {
			nodes.addElement((FiniteStates) pot.getVariables().elementAt(i));
      		}
    		}

    		Vector arrangedNodes = new Vector();
    
    		for (int i=0; i<nodes.size(); i++) {
        		arrangedNodes.add(nodes.elementAt(i));
    		}

    		boolean moreThanTheParents = false;
    
    		for (int i=0; i<nodes.size(); i++) {
        		if ((node.getParents().getID((FiniteStates) nodes.elementAt(i)) == -1) ||
            		(node.getParents().getID((FiniteStates) nodes.elementAt(i)) > nodes.size()-1)) {
           			moreThanTheParents = true;
           			break;
        		}
        		else {
           			arrangedNodes.setElementAt(nodes.elementAt(i),
				node.getParents().getID((FiniteStates) nodes.elementAt(i)));
        		}
    		}
    
    		if (moreThanTheParents) {
        		arrangedNodes = nodes;
    		}
    
    		headerNodes = new NodeList(arrangedNodes);

    		for (int i=0; i<headerNodes.size(); i++) {
       		FiniteStates father=(FiniteStates) headerNodes.elementAt(i);
			leftColumn.addElement(father.getName());
    		}

    		int positions[]= new int[headerNodes.size()];

        	rows = positions.length+states.size();

    		// Set the columns with its headers
    		Configuration config = new Configuration(headerNodes);
    		int ncolumns = (int) pot.getSize()/states.size();

    		for (int i=0; i<ncolumns; i++) {
			String cadena = "";
			Vector st = setColumn(config.getVariables(),config.getValues());//Vector con la configuracion de estados
			for(int j=0;j<st.size();j++) {
				cadena = cadena + (String)leftColumn.elementAt(j) + "="+st.elementAt(j)+" ";
			}
      		config.nextConfiguration();
			configuraciones.addElement(cadena);
    		}

    		//fillValues(headerNodes, node, pot, ncolumns);
    		fillValuesUtilities(headerNodes,node,pot,ncolumns);
        	
 	}

  	private String withoutQm(String s) {
            if (s.substring(0,1).equals("\""))
            {
                return (s.substring(1,s.length()-1));
            }
            else {
                return s;
            }
    	}
  
  	private void fillValuesUtilities(NodeList headerNodes, Node node, Potential pot, int columns) {

    		int tableColumns, tableRows, headerRows, valuesColumns;
    		int numStates;

    		int maxFinal = 0;
    		double maxVal = 0.0;
    		Configuration confMax = new Configuration(((Potential) 
			potentVector.elementAt(potentVector.size()-1)).getVariables());

    		for (int i=0; i<((Potential) potentVector.elementAt(potentVector.size()-1)).getSize(); i++) {
      		if (((Potential) potentVector.elementAt(potentVector.size()-1)).getValue(confMax) > maxVal) {
         			maxVal = ((Potential) potentVector.elementAt(potentVector.size()-1)).getValue(confMax);
         			maxFinal = i;
      		}

      		confMax.nextConfiguration();
    		}

    		numStates = ((FiniteStates) node).getNumStates();
   	 	tableColumns = columns;
    		valuesColumns = columns;
    		tableRows = numStates;
    		headerRows = headerNodes.size();

    		Configuration configHD = new Configuration(headerNodes);
    		Configuration configT;
    		double theMaxRow[] = new double[tableColumns];
    		int theMaxIndx[] = new int[tableColumns];
    		for (int j=0; j<tableColumns; j++) {
      		theMaxRow[j]=Float.NEGATIVE_INFINITY;
      		theMaxIndx[j]=-1;
    		}

        	for (int i=0; i<tableRows; i++) {
            	configT = new Configuration(pot.getVariables(),configHD,false);    
            	configT.putValue(((FiniteStates) node).getName(),i);
            	for (int j=0; j<tableColumns; j++) {
                		if (theMaxRow[j] < pot.getValue(configT)) {
                    		theMaxRow[j]=pot.getValue(configT);
                		}
				valores.addElement(pot.getValue(configT));
                		configHD.nextConfiguration();
                		for (int k=0; k<configHD.getVariables().size(); k++) {
                    		configT.putValue(((FiniteStates) configHD.getVariables().elementAt(k)).getName(),
						configHD.getValue(((FiniteStates) configHD.getVariables().elementAt(k)).getName()));
                		}
            	}

            	configHD = new Configuration(headerNodes);
        	}
      
    	}

  	private Vector setColumn (Vector headerNodes, Vector numstates) {

     		Vector column = new Vector();

     		for (int i=0; i<headerNodes.size(); i++) {
			column.addElement(withoutQm(((FiniteStates)headerNodes.elementAt(i)).getState(((Integer)numstates.elementAt(i)).intValue())));
     		}

     		return column;
  	}

	/**
	 * Extrae la lista de configuraciones
	 */
	public Vector getConfigurations() {
		return configuraciones;
	}

	/**
	 * Extrae la lista de estados de la decision
	 */
	public Vector getStates() {
		return statesOfDecision;
	}

	/**
	 * Extrae los valores
	 */
	public Vector getValues() {
		return valores;
	}

	public static void main(String[] args) throws ParseException,IOException {
		Network net = Network.read("super.elv");
		IDiagram diag = (IDiagram)net;
		diag.compile(3,null);
		NodeList listaNodos = diag.getNodeList();
		Node nodo = listaNodos.getNode("D");
		DecisionTable prueba = new DecisionTable(nodo,(Bnet)diag);
		Vector c = prueba.getConfigurations();
		Vector s = prueba.getStates();
		Vector v = prueba.getValues();
		int contador = 0;
		int paso = v.size() / s.size();
		for(int i=0;i<c.size();i++) {
			for(int j=0;j<s.size();j++) {
				System.out.println("D="+s.elementAt(j)+" "+c.elementAt(i)+" : "+v.elementAt(contador+(j*paso)));
			}
			contador++;
		}
	}
  
}//End of class
