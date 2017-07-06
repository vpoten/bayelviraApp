package elvira.learning.classification;

import elvira.tools.RandomGenerator;
import java.io.*;
import java.util.*;

import elvira.*;
import elvira.database.*;

  
  
/**
 * <p>The AuxiliarPotentiaTable is a class designed to make easier the calculation of relative 
 * frecuencies or conditional probabilities when they are estimated from a database. This 
 * class allow us to perform these caltulations in a time O(N), where N is the size of the 
 * database.</p>  
 * <p>The AuxiliarPotentialTable deal with two structures: numerator (a bidimensional array of size:
 * <code>number of states of the variable</code> x <code>number of states of its parets</code>)
 * and denominator (a unidemensional array of size: <code>number of states of its parets</code>).
 * The numerator(i,j) stores the number of cases where the variable takes its i-st value and its
 * parents take their j-st configuration. The denominator(j) stores the number of cases where 
 * the parents of the variable take their j-st configuration .</p> 
 * <p>In the case where the variable has no parents, the numerator will be an array
 * of size <code>number of states of the variable</code> x <code>1</code> and the denominator 
 * will be a <code>1</code> x <code>1</code> array. Therefore, when we have an 
 * AuxiliarPotentialTable object for a variable with no parents and we want to use a method of 
 * the class which has the parameter <code>stateOfParents</code> we must set always this parameter
 * to 0.</p>
 *
 * @author Guzmán Santafé
 * @version 0.3
 * @since 25/03/2003
 */
  public class AuxiliarPotentialTable
  {

    /**
     * Set the accurancy for the probabilities in the probability tables. 
     * by default <code>PRECISION</code> is set to an accuracy of -1 (use maximun
     * double precision) but it can be modified once a object of the class is
     * created. This is useful to show the Bnet in the GUI, therefore numbers like
     * 4.1019394399993299293929*10^-4 can be showed as, for instance, 0.00041 by 
     * setting the PRECISION to 5. Thus, the GUI could be more friendly.
     */
    private int PRECISION; 
  
    /**
     * A bidimensional array of size: <code>number of states of the variable</code> 
     * x <code>number of states of its parets</code>. Numerator(i,j) stores the number 
     * of cases where the variable takes its i-st value and its parents take their j-st 
     * configuration
     */
    private double [][] numerator;
    /**
     * A unidemensional array of size: <code>number of states of its parets</code>. 
     * Denominator(j) stores the number of cases where the parents of the variable 
     * take their j-st configuration 
     */
    private double []   denominator;
    /**
     * Number of possible values of the variable
     */
    private int         nStatesOfVariable;
    /**
     * Number of possible configurations of the parents of the variable
     */
    private int         nStatesOfParents;

     /**
     * Random numbers generator
     */
     private static Random randomGenerator;
    

    /**
     * Basic constructor. Create an AuxiliarPotentialTable for the variable node.
     * The number of states of the varialbe as well as the number of possible configurations
     * of its parents are taken from the node
     * 
     * @param node FiniteStates variable which we want to create the AuxiliarPotentialTable for
     */
    public AuxiliarPotentialTable(FiniteStates node)
    {
      int i;
      int j;
      
      this.PRECISION = -1;
      this.nStatesOfVariable  = node.getNumStates();
      this.nStatesOfParents    = 1;
      NodeList parents = node.getParentNodes();
      for(i=0;i<parents.size();i++)
      {
        this.nStatesOfParents *= ((FiniteStates)parents.elementAt(i)).getNumStates();
      }

      numerator   = new double [nStatesOfVariable][nStatesOfParents];
      denominator = new double [nStatesOfParents];

      // Initialize the random seed and random number generator
      //randomSeed = (new GregorianCalendar()).getTimeInMillis();
      randomGenerator = RandomGenerator.getRandomGenerator();      
    }//end AuxiliarPotentialTable(node)

    /**
     * Basic constructor. Create an AuxiliarPotentialTable for the variable.
     * The number of states of the varialbe as well as the number of possible configurations
     * of its parents are given as parameters
     * 
     * @param nStatesOfVariable number of possible states that the variable can take
     * @param nStatesOfParents  number of possible configuration that the parents of the
     * variable can take
     */
    public AuxiliarPotentialTable(int nStatesOfVariable, int nStatesOfParents)
    {
      int i;
      int j;
      
      this.PRECISION = -1;
      this.nStatesOfVariable  = nStatesOfVariable;
      this.nStatesOfParents   = nStatesOfParents;
      numerator   = new double [nStatesOfVariable][nStatesOfParents];
      denominator = new double [nStatesOfParents];

       // Initialize the random seed and random number generator
      //randomSeed = (new GregorianCalendar()).getTimeInMillis();
      randomGenerator = RandomGenerator.getRandomGenerator();
      
    }//end AuxiliarPotentialTable(int nStatesOfVariable, int nStatesOfParents)

    /**
     * Basic constructor. Create an AuxiliarPotentialTable for the variable.
     * The number of states of the varialbe as well as the number of possible configurations
     * of its parents are given as parameters
     * 
     * @param nStatesOfVariable number of possible states that the variable can take
     * @param configurationOfParents  number of possible configuration that the parents of the
     * variable can take
     */
    public AuxiliarPotentialTable(int nStatesOfVariable, Configuration configurationOfParents)
    {
      int i;
      int j;
      
      this.PRECISION = -1;
      this.nStatesOfVariable  = nStatesOfVariable;
      this.nStatesOfParents   = configurationOfParents.possibleValues();
      numerator   = new double [nStatesOfVariable][nStatesOfParents];
      denominator = new double [nStatesOfParents];

       // Initialize the random seed and random number generator
      //randomSeed = (new GregorianCalendar()).getTimeInMillis();
      randomGenerator = RandomGenerator.getRandomGenerator();
      
    }//end AuxiliarPotentialTable(int nStatesOfVariable, int nStatesOfParents)

    /**
     * Add the ocurrence of a case in the data base where the variable take 
     * <code>stateOfVariable</code>-st value and their parents their 
     * <code>stateOfParents</code>-st configuration.
     * The AuxiliarPotentialTable is designed to be able to work with booth
     * relative frecuencies and probabilities. This way, if we are stimating
     * relative frecuencies we will use <code>addCase</code> with <code>quantity = 1</code>
     * but if we have the probability of a case in the database we will use
     * <code>addCase</code> with <code>quantity = probability</code>
     * 
     * @param stateOfVariable state of the variable
     * @param stateOfParents configuration of variable's parents
     * @param quantity quantity to be added
     * 
     */
    public void addCase(int stateOfVariable, int stateOfParents, double quantity)
    {
      numerator[stateOfVariable][stateOfParents] += quantity;
      denominator[stateOfParents] += quantity;
    }//end addProbabilityOfCase(stateOfVariable, stateOfParents, probability)

    /**
     * Add the ocurrence of a case in the data base where the variable take 
     * <code>stateOfVariable</code>-st value and their parents their 
     * <code>stateOfParents</code>-st configuration.
     * The AuxiliarPotentialTable is designed to be able to work with booth
     * relative frecuencies and probabilities. This way, if we are stimating
     * relative frecuencies we will use <code>addCase</code> with <code>quantity = 1</code>
     * but if we have the probability of a case in the database we will use
     * <code>addCase</code> with <code>quantity = probability</code>
     * 
     * @param stateOfVariable state of the variable
     * @param configurationOfParents configuration of variable's parents
     * @param quantity quantity to be added
     * 
     */
    public void addCase(int stateOfVariable, Configuration configurationOfParents, double quantity)
    {
      numerator[stateOfVariable][configurationOfParents.getIndexInTable()] += quantity;
      denominator[configurationOfParents.getIndexInTable()] += quantity;
    }//end addProbabilityOfCase(stateOfVariable, stateOfParents, probability)

    /**
     * Add the <code>quantity</code> value to the denominator structure where the
     * parents of the variable take their <code>stateOfParents</code> configuration
     * 
     * @param stateOfParents configuration of variable's parents
     * @param quantity quantity to be added to the denominator
     */
    public void addToDenominator(int stateOfParents, double quantity)
    {
      denominator[stateOfParents] += quantity;
    }//end addToDenominator(stateOfParents, double quantity)

    /**
     * Add the <code>quantity</code> value to the numerator structure where the
     * variable take its <code>stateOfVariable</code> value and the parents of 
     * the variable take their <code>stateOfParents</code> configuration
     *
     * @param stateOfVariable value of the variable
     * @param stateOfParents configuration of variable's parents
     * @param quantity quantity to be added to the denominator
     */
    public void addToNumerator(int stateOfVariable, int stateOfParents, double quantity)
    {
      numerator[stateOfVariable][stateOfParents] += quantity;
    }//end addToNumerator(stateOfVariable, stateOfParents, quantity)

    /**
     * Apply the Laplace correction to the AuxiliarPotentialTable. This method sum one
     * to every element in the numerator structure and the number of possible values
     * that the variable can take to every element in the denominator structure
     */
    public void applyLaplaceCorrection()
    {
      int i;
      int j;

      for(j=0;j<nStatesOfParents;j++)
      {
        for(i=0;i<nStatesOfVariable;i++)
        {
          numerator[i][j] += 1;          
        }
        denominator[j] += nStatesOfVariable;
      }
    } //end applyLaplaceCorrection()

    /**
     * Returns a copy of the AuxiliarPotentialTable
     * 
     * @return A new object which is a exact copy of the current one.
     */
    public AuxiliarPotentialTable copy()
    {
      int i;
      int j;
      
      AuxiliarPotentialTable copy = new AuxiliarPotentialTable(this.nStatesOfVariable, this.nStatesOfParents);
      for(j=0;j<this.nStatesOfParents;j++)
      {
        for(i=0;i<this.nStatesOfVariable;i++)  
        {
          copy.numerator[i][j] = this.numerator[i][j];
        }
        copy.denominator[j] = this.denominator[j];
      }
      return copy;
    }

    /**
     * Copies the values of <code>a</code> into the current object. The
     * values of nStatesOfVariable and nStatesOfParents must be the same in both
     * object (current AuxiliarPotentialTable and <code>a</code>)
     * 
     * @parameter a  AuxiliarPotentialTable to be copied into the current AuxiliarPotentialTable.
     */

    public void copyFromObject(AuxiliarPotentialTable a)
    {
      int i;
      int j;

      if(this.nStatesOfParents != a.nStatesOfParents ||
          this.nStatesOfVariable != a.nStatesOfVariable)
          {
            System.out.println("ERROR: copyFromObject method. The values of nStatesOfParents " + 
              "and nStatesOfVar must be the same in both objects");
              System.exit(-1);
          }
      
      for(j=0;j<this.nStatesOfParents;j++)
      {
        for(i=0;i<this.nStatesOfVariable;i++)  
        {
          this.numerator[i][j] = a.numerator[i][j];
        }
        this.denominator[j] = a.denominator[j];
      }
    }


    /**
     * Retuns the value of the <code>stateOfParents</code>-st element in the denominator
     * structure
     * 
     * @param stateOfParents configuration of the parents of the variable.
     * @return stateOfParents-st element in the denominator
     */
    public double getDenominator(int stateOfParents)
    {
      return denominator[stateOfParents];
    }//end getDenominator(stateOfParents)

    /**
     * Retuns the number of possible configurations of the parents of the variable
     * 
     * @return number of states of the parents
     */
    public int getNStatesOfParents()
    {
      return nStatesOfParents;
    }

        /**
     * Retuns the number of values that the variable can take
     * 
     * @return number of states of the variable
     */
    public int getNStatesOfVariable()
    {
      return nStatesOfVariable;
    }

    /**
     * Retuns the value of the (<code>stateOfVariable,stateOfParents</code>) element in 
     * the numerator structure
     * 
     * @param stateOfVariable value of the variable
     * @param stateOfParents configuration of variable's parents
     * @return (stateOfVariable,stateOfParents) element in the numerator
     */
    public double getNumerator(int stateOfVariable, int stateOfParents)
    {
      return numerator[stateOfVariable][stateOfParents];
    }//end getNumerator(stateOfVariable,stateOfParents)

    /**
     * Retuns the value of the probabiltity of the variable of taking
     * its <code>stateOfVariable</code>-st value given that its parents take their
     * <code>stateOfParents</code>-st configuration.
     * The returned value is calculated as 
     * numerator(statesOfVariable,stateOfParents)/denominator(stateOfParents)
     * 
     * @param stateOfVariable value of the variable
     * @param stateOfParents configuration ofthe parents of the variable
     * @return conditional probability P(X=stateOfVariable|Pa=stateOfParents)
     */
    public double getPotential(int stateOfVariable, int stateOfParents)
    {		
      return  numerator[stateOfVariable][stateOfParents]/denominator[stateOfParents];
    }//end getPotential(stateOfVariable, stateOfParents)

		/**
     * Retuns the value of the probabiltity of the variable of taking its
     * <code>stateOfVariable</code>-st value and their parents their
     * <code>stateOfParents</code>-st configuration.
     *
     * @param stateOfVariable state of the variable
     * @param configurationOfParents configuration of variable's parents
     * @param quantity quantity to be added
     *
     */
     public double getPotential(int stateOfVariable, Configuration configurationOfParents)
    {
			int stateOfParents = configurationOfParents.getIndexInTable();
			return this.getPotential(stateOfVariable,stateOfParents);
    }//end getPotential(stateOfVariable, stateOfParents)

	
    /**
     * Returns the conditional probability table of the variable represented in the
     * same format as in the PotentialTable class (unidimensional array)
     * 
     * @return double [] conditional probability table of the variable
     */
    public double[] getPotentialTableCases()
    {
      double [] potentialTableCases; 
      potentialTableCases = new double [nStatesOfVariable * nStatesOfParents];
      
      // Variables used to round the values in the potential table to an accuracy of
      // 'PRECISION' decimal places
      double   sum;
      double   aux;
      double mult = Math.pow(10,PRECISION);

      for(int j=0;j<nStatesOfParents;j++)            
      {        
        sum = 0;
        for(int i=0;i<nStatesOfVariable;i++)
        {
          potentialTableCases[(nStatesOfParents*i)+j] = numerator[i][j]/denominator[j];            

          // Set an accuracy of 'PRECISION' decimal places for probabilities in the conditional
          // probability tables and correct them in order to sum up to one for the same 
          // parent configuration
          if(i != nStatesOfVariable - 1)
          {
            if(this.PRECISION != -1)
            {
              aux = Math.floor(potentialTableCases[(nStatesOfParents * i) + j] * mult);
              potentialTableCases[(nStatesOfParents * i) + j] = ((double) aux) / mult;    
              sum += aux; 
            }//if(this.PRECISION != -1)            
            else
            {
              sum += potentialTableCases[(nStatesOfParents * i) + j];
            }
                       
          }
          else
          {
            if(this.PRECISION != -1)
            {
              potentialTableCases[(nStatesOfParents * i) + j] = ((double) (mult - sum)) / ((double) mult);    
            }//if(this.PRECISION == -1)
            else
            {
              potentialTableCases[(nStatesOfParents * i) + j] = 1 - sum;
            }
          }
        }
      }
      return potentialTableCases;
    }//end getPotentialTableCases()
 
    /**
     * Every element in the numerator structure is initialized to <code>quantity</code>
     * value. The denominator values are initialized to <code>nStatesOfVariable*quantity</code>
     * 
     * @param quantity the value to initialize the numerator structure
     */
    public void initialize(double quantity)
    {
      int i;
      int j;

     for(j=0;j<nStatesOfParents;j++)
      {
        for(i=0;i<nStatesOfVariable;i++)        
        {
          numerator[i][j] = quantity;
        }
        denominator[j] = nStatesOfVariable*quantity;
      }
      
    }//end initialize()

  /**
   * Prints in the standar output the values of the current AuxiliarPotentialTable
   */
  public void printValues()
  {
    int i,j;
    
    //Print the numerator
    for(i=0;i<numerator.length;i++)
    {
      for(j=0;j<numerator[i].length;j++)
      {
        System.out.print(numerator[i][j]);
        System.out.print('\t');
      }//for(int j=0;j<numerator[i].length;j++)
      System.out.println();      
    }//for(int i=0;i<numerator.length;i++)
    
    //Print a line separation between numerator and denominator
    for(i=0;i<denominator.length;i++)
    {
      System.out.print("  ---  ");
      System.out.print('\t');
    }//for(i=0;i<denominator.length;i++)
    System.out.println();
    
    //Print the denominator
    for(i=0;i<denominator.length;i++)
    {
      System.out.print(denominator[i]);
      System.out.print('\t');
    }//for(i=0;i<denominator.length;i++)
  }//printValues()
  
  
  /**
   * Assign the same probability to every conditional probability 
   * in the conditional probability table
   */
   public void setEqualProbabilityTable()
    {
      // Iterators
      int i;
      int j;

      for (i=0;i<nStatesOfParents;i++)      
      {
        for (j=0;j<nStatesOfVariable;j++)  
        {
          numerator[j][i] = 1;          
        }
        denominator[i] = nStatesOfVariable;
      }
    }//end setEqualProbabilityTable()

    /**
     * Set the <code>stateOfParents</code>-st element in the denominator structure
     * to the value <code>quantity</code>
     * 
     * @param stateOfParents configuration of variables's parents
     * @param quantity value to be assigned to the denominator
     */
    public void setDenominator(int stateOfParents, double quantity)
    {
      denominator[stateOfParents] = quantity;
    }//end setDenominator(stateOfClass,quantity)
    
    /**
     * Set the (<code>stateOfVariable,stateOfParents</code>) element in the denominator 
     * structure to the value <code>quantity</code>
     * 
     * @param stateOfVariable value of the variable
     * @param stateOfParents configuration of variables's parents
     * @param quantity value to be assigned to the denominator
     */
    public void setNumerator(int stateOfVariable, int stateOfParents, double quantity)
    {
      numerator[stateOfVariable][stateOfParents] = quantity;
    }//end setNumerator(stateOfVariable,stateOfClass,quantity)
    
    /**
     * Set the precision to an accuray of <code>precision</code> decimal places
     * 
     * @param precision number of decimal places taken into account
     */
    public void setPrecision(int precision)
    {
      this.PRECISION = precision;
    }//end setPrecision(precision)
    
    /**
     * Change the value of the random seed
     * 
     * @param seed the new value of the random seed
     */
    public void setRandomSeed(long seed)
     {
       RandomGenerator.setRandomSeed(seed);
     }

    /**
   * Assign a random probability to every conditional probability 
   * in the conditional probability table
   */
    public void setRandomTable()
    {
      // Iterators
      int i;
      int j;

      double sum;
      double rand;
           
      
      for (i=0;i<nStatesOfParents;i++)
      {
        sum = 0;
        for (j=0;j<nStatesOfVariable;j++)
        {
          rand = randomGenerator.nextDouble();
          //System.out.println("Número aleatorio " + j +": " + rand); 
          numerator[j][i] = rand;
          sum += rand;
        }

        denominator [i] = sum;
      }
    }//end setRandomTable()
  }//end AuxiliarPotentialTable class
