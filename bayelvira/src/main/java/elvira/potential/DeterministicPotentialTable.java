package elvira.potential;

import java.util.Vector;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;

/**
 * @author Manolo_Luque
 * This class is similar to a Potential Table, but instead of assigning a double value to each configuration of the variables of the potentials we assign
 * an integer indicating the state selected of a variable. This can be useful, for example, for decision tables, where we indicate the option chosen
 * for each configuration of the variables when making the decision
 * It is important to notice that the attribute 'variables', inherited from 'Potential' contains the variables whose configurations are mapped, while the variable
 * whose states are selected for each configuration are stored in a separated attribute named 'variable'
 */
public class DeterministicPotentialTable extends Potential{
	/**
	 * Variable whose states are chosen
	 */
	FiniteStates variable;
	
	
	/**
	 * 
	 */
	int values[];
	
	
	public int[] getValues() {
		return values;
	}

	public void setValues(int[] values) {
		this.values = values;
	}

	/**
	 * Method to convert the probabilistic (or utility) potential to deterministic potential
	 * using maximum
	 * @param node node of the deterministic table
	 * @return a new and transformed potential table, whose first variable is the deterministic node
	 */

	public DeterministicPotentialTable(PotentialTable pot,Node node){
		NodeList nl;
		PotentialTable newPot;
		Configuration auxConf;
		Configuration maxConf;
		int numberOfValues;
		

		//Initalize the new potential to 0
		variable = (FiniteStates) node;
		this.variables = (Vector)(pot.variables.clone());
		variables.remove(node);
		numberOfValues = (int)FiniteStates.getSize(variables);
		values = new int[numberOfValues];
		
		//Rest of variables of the potential
		nl = new NodeList(variables);
				
		//Compute the maximum configuration for each combination of the variables
		auxConf = new Configuration(nl);
		for (int i=0;i<nl.getSize();i++){
			//Compute the maximum configuration
			maxConf = pot.getMaxConfiguration(auxConf);
			
			//Set the state of the maximum configuration
			setValue(auxConf,maxConf.getValue(variable));
			
			//Next configuration
			auxConf.nextConfiguration();
		}
		
		
	}
	
	/**
	 * @return true if all the values assigned to the potential are identical
	 */
	public boolean isConstantPotential(){
		int valueFound;
		boolean isConstant;
		int auxValue;
		
		valueFound = -1;
		isConstant = true;
		for (int i=0;(i<values.length)&&isConstant;i++){
			auxValue = values[i];
			if (valueFound==-1){
				valueFound = auxValue;
			}
			else{
				isConstant = (auxValue == valueFound);
			}
		}
		return isConstant;
		
		
	}
	
	
	
	/**
	 * @return true if all the values assigned to the potential are identical
	 */
	public boolean isConstantPotential(Configuration instantiations){
		int valueFound;
		boolean isConstant;
		int auxValue;
		
		  Configuration auxConf;
		  Vector<FiniteStates> aux;
		  FiniteStates temp;
		  int i, nv;
		  

		  aux = new Vector<FiniteStates>();
		  for (i=0 ; i<variables.size() ; i++) {
		    temp = (FiniteStates)variables.elementAt(i);
		    if (instantiations.indexOf(temp) == -1)
		      aux.addElement(temp);
		  }

		  // Size of the restricted potential.
		  nv = (int)FiniteStates.getSize(aux);

		  // Configuration preserving the values in instantiations
		  auxConf = new Configuration(variables,instantiations);

		  valueFound = -1;
		  isConstant = true;
		  for (i=0 ; (i<nv) && isConstant; i++) {
		    auxValue = getValueOfConf(auxConf);
		    if (valueFound==-1){
				valueFound = auxValue;
			}
			else{
				isConstant = (auxValue == valueFound);
			}
		    auxConf.nextConfiguration();
		  }

		return isConstant;
			
	}
	
	
	/**
	 * Gets the value for a configuration of variables.
	 * @param conf a <code>Configuration</code>.
	 * @return the value of the potential for <code>Configuration conf</code>.
	 */

	public int getValueOfConf(Configuration conf) {

	  int pos;
	  Configuration aux;

	  // Take a configuration from conf just for variables
	  // in the potential.
	  aux = new Configuration(variables,conf);
	  pos = aux.getIndexInTable();

	  return values[pos];
	}
	
	

	
	/**
	 * Method to convert the probabilistic (or utility) potential to deterministic potential
	 * using maximum. It assumes that the first variable of the potential is the corresponding node.
	 * @return a new and transformed potential table, whose first variable is the deterministic node
	 */

	public DeterministicPotentialTable(PotentialTable pot){
		this(pot,(FiniteStates) (pot.variables.elementAt(pot.variables.size()-1)));
		}
	public DeterministicPotentialTable() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sets the value for a configuration of variables.
	 * @param conf a <code>Configuration</code> of variables.
	 * @param value the new value for <code>Configuration conf</code>.
	 */

	public void setValue(Configuration conf, int value) {
	  int index;
	  Configuration aux;

	  aux = new Configuration(variables,conf);
	  index = aux.getIndexInTable();
	  values[index] = value;
	}


	@Override
	public Potential addVariable(Node var) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Potential combine(Potential pot) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Potential restrictVariable(Configuration conf) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getValueInConstantPotential() {
		// TODO Auto-generated method stub
		return values[0];
	}

	
	public Integer getValueInConstantPotential(Configuration instantiations) {
		// TODO Auto-generated method stub
		int valueFound;
		boolean isConstant;
		int auxValue;
		
		  Configuration auxConf;
		  Vector<FiniteStates> aux;
		  FiniteStates temp;
		  int i, nv;
		  

		  aux = new Vector<FiniteStates>();
		  for (i=0 ; i<variables.size() ; i++) {
		    temp = (FiniteStates)variables.elementAt(i);
		    if (instantiations.indexOf(temp) == -1)
		      aux.addElement(temp);
		  }

		  // Size of the restricted potential.
		  nv = (int)FiniteStates.getSize(aux);

		  // Configuration preserving the values in instantiations
		  auxConf = new Configuration(variables,instantiations);
		  
		  return getValueOfConf(auxConf);
	}
	public FiniteStates getVariable() {
		return variable;
	}

	public void setVariable(FiniteStates variable) {
		this.variable = variable;
	}
}
