package elvira.learning.classification.supervised.discrete;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Link;
import elvira.LinkList;
import elvira.NodeList;
import elvira.database.DataBaseCases;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class ExpectedSufficientStatistics extends DiscriminativeSufficientStatistics 
{

  /**
   * Objet to calculate the probability of a case.
   */
  private ProbabilityList p; 
  
  /**
   * flag that indicate if the calculated values are valid of not. That is, if
   * we try to calculate the Expected sufficient statistics from a illegal set
   * of generative sufficient statistics, at the time that the a illegal value
   * of the generative sufficient statistics (the probability values calculated
   * from the generative sufficient statistics are <0, >1, or do not sum up to 1)
   * is detected, the process of calculating the expected sufficien statistics is
   * stopped and the situation is reported through the flag 'isValid'
   */
  private boolean isValid; 
  
   
  /**
   * Basic constructor
   * @param nl Node List which gives the relationship between variables
   * @param laplace use or not laplace correction
   */
  public ExpectedSufficientStatistics(NodeList nl, GenerativeSufficientStatistics S, boolean laplace)
  {
      super(nl);      
      this.p = new ProbabilityList(S,laplace);
      isValid = true;
  }//ExpectedSufficientStatistics(NodeList nl, SufficientStatistics S, boolean laplace)
  
  /**
   * Basic constructor
   * @param nl Node List which gives the relationship between variables   
   */
  public ExpectedSufficientStatistics(NodeList nl, GenerativeSufficientStatistics S)
  {
    this(nl,S,false);    
  }
  
  /**
   * Calculate the value of the Expected discriminative sufficient statistics from the Data cases
   * in a new ExpectedSufficientStatistics object
   * @param nl NodeList which codify the dependencies between variables
   * @param S GenerativeSufficientStatistics where the number of ocurrences of the variables are counted
   * @param dbc DataBaseCases to calculate the statistics.
   * @returns the values for the expected sufficient statistics
   */
    public static ExpectedSufficientStatistics calculateExpectedSufficientStatistics(NodeList nl, 
                                                                                      GenerativeSufficientStatistics S,
                                                                                      DataBaseCases dbc)
    {   
      return calculateExpectedSufficientStatistics(nl,S,dbc,false);
    }//ExpectedSufficientStatistics calculateExpectedSufficientStatistics(NodeList nl, ...)
  
  
  /**
   * Calculate the value of the Expected discriminative sufficient statistics from the Data cases
   * in a new ExpectedSufficientStatistics object
   * @param nl NodeList which codify the dependencies between variables
   * @param S GenerativeSufficientStatistics where the number of ocurrences of the variables are counted
   * @param dbc DataBaseCases to calculate the statistics.
   * @param laplace use or not the laplace correction when calculating the conditional probabilities
   * @returns the values for the expected sufficient statistics
   */
    public static ExpectedSufficientStatistics calculateExpectedSufficientStatistics(NodeList nl, 
                                                                                      GenerativeSufficientStatistics S,
                                                                                      DataBaseCases dbc,
                                                                                      boolean laplace)
    {      
      int i;
      int [] dataSample;
      Iterator varsIt;
      ExpectedSufficientStatistics E; 
      GenerativeSufficientStatistics newS = new GenerativeSufficientStatistics(S.nodeList);
      newS.copyFrom(S);
      E = new ExpectedSufficientStatistics(nl,newS,laplace);
      // If the current set of Generative Sufficient Statistics is not valid, 
      // the method countCase throws a VerifyError exception and we should
      // finish the calculations and set the isValid flag to false
      // Moreover,it may have been detected that the current Generative Sufficient Statistics
      // is invalid (some sufficient statistic < 0), therefore it is not need to do any calculation
      // for this method because the result will be an invalid ExpectedSufficientStatistics
      // 
      boolean finish;
      if(S.isValid())
      {
        finish = false;        
      }//if(S.isValid())
      else
      {
        finish    = true; 
        E.isValid = false;
      }//else    
      
      int classValue;
      Iterator dataIt = dbc.getCaseListMem().getCases().iterator();
      Vector   vars   = (Vector)nl.getNodes().clone();      
      // get and remove the class node from the nodeList. The class node must be
      // the first element in the nodelist in order to construct a configuration
      // object and obtain the conditional probability of the case codified by 
      // the given configuration.
      FiniteStates classNode = (FiniteStates)vars.lastElement();
      /**vars.removeElementAt(vars.size()-1);
      // include the ClassNode and the value for the ClassNode in the first 
      // position.        
      vars.insertElementAt(classNode,0);
      */
      while(dataIt.hasNext() && !finish)
      {
        dataSample = (int[]) dataIt.next();
        Vector vals = new Vector();
        varsIt = vars.iterator();
        // the last element in dataSample is the value for the ClassNode
        for(i=0;i<dataSample.length-1;i++)
        {
          FiniteStates var = (FiniteStates)varsIt.next();        
          vals.addElement(new Integer(dataSample[i]));           
        }// for_i
        //vals.insertElementAt(new Integer(dataSample[dataSample.length-1]),0);
        vals.addElement(new Integer(dataSample[dataSample.length-1]));
        
        Configuration conf = new Configuration(vars,vals);
        try
        {
          E.countCase(conf);  
        }
        catch(VerifyError e)
        {
          E.isValid = false;
          finish    = true;
        }//catch(VerifyError e)        
      }// while     
      return E;
    }//end calculateExpectedSufficientStatistics(DataBaseCases dbc)
  
   /**
   * Adds the correspondig value to the sufficient statiscs which are given by configuration.
   * Note that, following the TM algorithm, only the expected sufficien statistics related to c
   * are used and updated.
   * The order of the variables for the sufficient statistics and for the configuration
   * are suposed to be the same (and the same size).
   * @param conf codify a data case (the class is in the last element)
   */
    public void countCase(Configuration conf) throws VerifyError
    {
      int i,c,w,t,z;
      FiniteStates C            = (FiniteStates)conf.getVariable(0);
      int          cStates      = C.getNumStates();
      int []       confValues   = new int[conf.size()]; // store the values of each variable given in conf for a better access
      Iterator varsIt,valsIt;
      
      
      boolean finish = false;
            
      valsIt = conf.getValues().iterator();
      for(i=0;valsIt.hasNext();i++)
      {
        confValues[i] = ((Integer)valsIt.next()).intValue();
      }//for_i
           
      // in the calculation of the Expected sufficient statistics we multiply by
      // the value of C, c, we don't need to calculate the values when c=0 because
      // the result will be 0 anyway
      for(c=1;c<cStates;c++)
      {
        // set the value of C in the configuration to c (C is the last element)
        Configuration newConf = new Configuration((Vector)conf.getVariables().clone(),(Vector)conf.getValues().clone());
        newConf.getValues().setElementAt(new Integer(c),newConf.size()-1);
        
        // if the value of prob is illegal, the method getProbability trows a
        // VerifyError exception
        double prob = p.getConditionalProbability(newConf);   // prob = p(c|x0,...,xn)
        double valueSc = prob;    
        for(w=0;w<this.cStates;w++)
        {
          // valueSc = p(c|x0,...,xn) * pow(valC,w+1)
          valueSc *= c;
          addToSc(w,valueSc);
          varsIt = newConf.getVariables().iterator();      
          valsIt = newConf.getValues().iterator();      
          for(i=0;i<nVar;i++)
          {
            FiniteStates Xi       = (FiniteStates)varsIt.next();
            int          XiVal    = ((Integer)valsIt.next()).intValue();
            //int          XiStates = Xi.getNumStates();
            if(Xi.getParents().size() == 1)
            {//the variable has a unique parent (C or a predictive variable)              
              FiniteStates parXi       = (FiniteStates)Xi.getParents().elementAt(0).getTail();
              //int          parXiStates = parXi.getNumStates();
              if(parXi.getComment().equalsIgnoreCase("ClassNode"))
              {
                double valueScxi = valueSc;
                for(t=0;t<this.varStates[i];t++)
                {
                  // valueScxi = p(c|x0,...,xn) * pow(c,w+1) * pow(XiVal,t+1)
                  valueScxi *= confValues[i]; 
                  addToScxi(i,w,t,valueScxi);
                }//for(t=0;t<XiStates;t++)
              }//if(XiPar.getComment().equalsIgnoreCase("ClassNode"))
            }//if(Xi.getParents().size() == 1)
            else if (Xi.getParents().size() == 2)
            {// variable Xi has two parents c and a predictive variable (Xji). The first one in the 
             // parents list of Xi is Xji
              FiniteStates parXi       = (FiniteStates)Xi.getParents().elementAt(0).getTail();
              //int          parXiStates = parXi.getNumStates();
              // position of parXi in the variable list from conf
              int          parXiPos    = (new NodeList(conf.getVariables())).getId(parXi);
              int          parXiVal    = confValues[parXiPos];
              double valueScxi = valueSc;
              for(t=0;t<this.varStates[i];t++)
              {
                // valueScxi = p(c|x0,...,xn) * pow(valC,w+1) * pow(valXi,t+1)
                valueScxi *= confValues[i]; 
                addToScxi(i,w,t,valueScxi);
                double valueScxixji = valueScxi;
                for(z=0;z<this.parStates[i];z++)
                {
                  // valueScxixji = p(c|x0,...,xn) * pow(c,w+1) * pow(XiVal,t+1) * pow(parXiVal,z+1
                  valueScxixji *= parXiVal;
                  addToScxixji(i,w,t,z,valueScxixji);
                }//for(z=0;z<parXiStates;z++)
                }//for(t=0;t<XiStates;t++)
            }//elseif (Xi.getParents().size() == 2)
          }//for_Xi
        }//for_w        
      }//for_c      
    }//end countCase
        
  /**
   * Returns the ProbabilityList object that contains information about the conditional
   * probability values.
   * @return ProbabilityList
   */
  public ProbabilityList getProbabilityList()
  {
    return p;
  }
  
  /**
   * Return true if the current values for the Expected sufficient statistics are valid and false otherwise
   * @return the value for the isValid flag
   */
  public boolean isValid()
  {
    return this.isValid;
  }//isValid()
  /**
   * Set the current value of the Suffcient Statistics to the new value given as parameter. This makes that the probability
   * of a case change and new calculation of the values for the Expected Sufficient Statistics may be done (using the method 
   * calculateStatistics)
   * @param S new value for the Sufficient Statistics
   */
    public void resetSufficientStatistics(GenerativeSufficientStatistics S)
    {
      boolean laplace = p.usingLaplace();  
      p               = new ProbabilityList(S,laplace);
      this.isValid    = true;
    }
}