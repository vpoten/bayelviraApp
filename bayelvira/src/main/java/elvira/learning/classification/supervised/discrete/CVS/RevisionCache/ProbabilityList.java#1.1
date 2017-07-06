package elvira.learning.classification.supervised.discrete;
import elvira.Configuration;
import elvira.learning.classification.supervised.discrete.SufficientStatistics;
import elvira.FiniteStates;
import elvira.NodeList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.lang.VerifyError;

public class ProbabilityList 
{
  
  /**
   * hashtables to save the probability values already calculated
   */
  private Hashtable simpleProbs; //stores the factoriced probabilities: p(Xi=xi|Pai=pai)
  private Hashtable jointProbs; //stotes the joint probability of a data sample: p(x0,...,xn,c)
  private Hashtable conditionalProbs; //stores the conditional probability of a data sample: p(c|x0,...,xn)
  
  GenerativeSufficientStatistics S;
  
  boolean laplace;
  
  boolean isValid;
 
  /**
   * Basic constructor for the ProbabilityList class
   */
  public ProbabilityList(GenerativeSufficientStatistics S, boolean laplace)
  {
   simpleProbs      = new Hashtable();
   jointProbs       = new Hashtable();
   conditionalProbs = new Hashtable();
   this.S           = S;
   this.laplace     = laplace;
   this.isValid     = true;
  }
  
  /**
   * Calculate the conditional probability p(c|x0,...,xn), where the values of c,x0,...,xn are given by conf, being
   * c the first element in conf
   * @param conf which gives the values of the variables
   * @return the value of p(c|x0,...,xn)
   * @throws java.lang.VerifyError
   */
  public double calculateConditionalProbability(Configuration conf) throws VerifyError
  {
    FiniteStates cVar = conf.getVariable(conf.size()-1);
    int          cVal = conf.getValue(conf.size()-1);
    double prob = 0;
    double sum  = 0;
    for(int c=0;c<cVar.getNumStates();c++)
    {
      Configuration newConf = new Configuration((Vector)conf.getVariables().clone(),(Vector)conf.getValues().clone());
      newConf.getValues().set(newConf.size()-1,new Integer(c));
      double currentVal = this.getJointProbability(newConf);
      sum += currentVal;
      if(c == cVal)
      {
        prob = currentVal;
      }//if(c == cVal)
    }//for(int c=0;c<cVar.getNumStates();c++)   
    double result = prob / sum;
    this.conditionalProbs.put(conf,new Double(result));    
    return result;
  }//calculateConditionalProbability(Configuration conf) throws VerifyError
  
   /**
   * Calculate the log conditional probability logp(c|x0,...,xn), where the values of c,x0,...,xn are given by conf, being
   * c the first element in conf
   * @param conf which gives the values of the variables
   * @return the value of logp(c|x0,...,xn)
   * @throws java.lang.VerifyError
   */
  public double calculateLogConditionalProbability(Configuration conf) throws VerifyError
  {
    FiniteStates cVar = conf.getVariable(conf.size()-1);
    int          cVal = conf.getValue(conf.size()-1);
    double prob = 0;
    double sum  = 0;
    for(int c=0;c<cVar.getNumStates();c++)
    {
      conf.getValues().set(conf.size()-1,new Integer(c));
      double currentVal = this.getJointProbability(conf);
      sum += currentVal;
      //System.out.print(currentVal + " ; ");
      if(c == cVal)
      {
        prob = currentVal;
      }//if(c == cVal)
    }//for(int c=0;c<cVar.getNumStates();c++)    
    return Math.log(prob) - Math.log(sum);
  }//calculateLogConditionalProbability(Configuration conf) throws VerifyError
  
  /**
   * Calculate the value of a simple configuration, p(Xi=xi|Pai=pai), givev by a 
   * configuration
   * @param conf Configuration which represetn a value for Xi and its parents
   * @return the value of the siple probability p(Xi=xi|Pai=pai)
   */
  public double calculateSimpleProbability(Configuration newConf)
  {    
    double result = 0;
    if(newConf.getValue(0) == 0)
    {// the value of p(Xi=0|Pai=pai) = 1 - p(Xi=k|Pai=pai), with k=1,...,ri      
      result = 1;
      FiniteStates varXi = newConf.getVariable(0);
      for(int i=1;i<varXi.getNumStates();i++)
      {
        // set the new value for varXi in the configuration
        Configuration conf = new Configuration((Vector)newConf.getVariables().clone(),(Vector)newConf.getValues().clone());
        conf.getValues().setElementAt(new Integer(i),0);
        result -= this.getSimpleProbability(conf);
      }//for(int i=1;i<varXi.getNumStates();i++)           
    }//if(newConf.getValue(0) == 0)
    else
    {      
    //Calculate the probability p(Xi=xi|Pai=pai)
        Iterator varsIt_newConf,valsIt_newConf;        
        varsIt_newConf = newConf.getVariables().iterator();
        valsIt_newConf = newConf.getValues().iterator();
         
        FiniteStates varXi = (FiniteStates)varsIt_newConf.next();
        int          valXi = ((Integer)valsIt_newConf.next()).intValue();
        if(varXi.getComment().equalsIgnoreCase("ClassNode"))
        {
          if(this.laplace)
          {
            result = (S.getElementFromSc(valXi) + 1) / (S.getNCases() + varXi.getNumStates());  
          }//if(this.laplace)
          else
          {
            result = S.getElementFromSc(valXi) / S.getNCases();  
          }//else --> !(this.laplace)          
        }//end if(varXi.getComment().equalsIgnoreCase("ClassNode"))
        else
        {
          int Xi = S.nodeList.getId(varXi);
          if(newConf.size() == 1)
          {
            // p(Xi=valXi)
            if(this.laplace)
            {
              result = (S.getElementFromSxi(Xi,valXi) + 1) / (S.getNCases() + varXi.getNumStates());  
            }//if(this.laplace)
            else
            {
              result = S.getElementFromSxi(Xi,valXi) / S.getNCases();  
            }//else --> !(this.laplace)            
          }//end if(newConf.size() == 1)
          else if(newConf.size() == 2)
          {
            FiniteStates par    = (FiniteStates)varsIt_newConf.next();
            int          parVal = ((Integer)valsIt_newConf.next()).intValue();
            int          parPos = this.S.nodeList.getId(par);
            if(par.getComment().equalsIgnoreCase("ClassNode"))
            {// the unique parent is the class node              
              if(this.laplace)
              {
                result = (S.getElementFromScxi(Xi,parVal,valXi) + 1) / (S.getElementFromSc(parVal) + varXi.getNumStates());
              }//if(this.laplace)
              else
              {
                result = S.getElementFromScxi(Xi,parVal,valXi) / S.getElementFromSc(parVal);
              }//else --> !(this.laplace)          
            }//if(par.getComment().equalsIgnoreCase("ClassNode"))
            else
            {//the unique parent is a predictive variable              
              if(this.laplace)
              {
                result = (S.getElementFromSxixji(Xi,valXi,parVal) + 1) / (S.getElementFromSxi(parPos,parVal) + varXi.getNumStates());
              }//if(this.laplace)
              else
              {
                result = S.getElementFromSxixji(Xi,valXi,parVal) / S.getElementFromSxi(parPos,parVal);
              }//else --> !(this.laplace)          
            } //end else            
          }//end elseif(newConf.size() == 2)
          else if(newConf.size() == 3)
          {//the variable Xi has two parents: C and a predictive variable
            FiniteStates par    = (FiniteStates)varsIt_newConf.next();
            int          parVal = ((Integer)valsIt_newConf.next()).intValue();
            int          parPos = S.nodeList.getId(par);
            FiniteStates C      = (FiniteStates)varsIt_newConf.next();
            int          cVal   = ((Integer)valsIt_newConf.next()).intValue();
           
            if(this.laplace)
            {
              result = (S.getElementFromScxixji(Xi,cVal,valXi,parVal) + 1) / (S.getElementFromScxi(parPos,cVal,parVal) + varXi.getNumStates());
            }//if(this.laplace)
            else
            {
              result = S.getElementFromScxixji(Xi,cVal,valXi,parVal) / S.getElementFromScxi(parPos,cVal,parVal);
            }//else --> !(this.laplace)          
          }//end elseif(newConf.size() == 3)
        }//end else !(varXi..getComment().equalsIgnoreCase("ClassNode"))       
    }
    /**
         * If the value of the probability is ilegal, report the situation via an exception
         * and set the flag isValid to false
         */

    if(result < 0 || result > 1)
    {
      this.isValid = false;
      throw new VerifyError("Illegal provability value found. The set of sufficient statistis is invalid");
    }    
    // save the calculated probability into the hashtable for a future use
    this.simpleProbs.put(newConf,new Double(result));       
    return result;
  }//calculateSimpleProbability(Configuration newConf)
  
  /**
   * Calculate the joint probability p(c,x0,...,xn), where the values of x0,...,xn,c are given by conf, being
   * c the last element in conf
   * @param conf which gives the values of the variables
   * @return the value of p(c,x0,...,xn)
   * @throws java.lang.VerifyError
   */
  public double calculateJointProbability(Configuration conf) throws VerifyError
  {
    int i;
    double probabilityValue = 1;
    Vector vars             = conf.getVariables();
    Vector vals             = conf.getValues();
    Iterator nodesIt = vars.iterator();
    Iterator valsIt  = vals.iterator();   
    for(i=0;i<vars.size();i++)
    {
      FiniteStates node  = (FiniteStates) nodesIt.next();
      int          val   = ((Integer)valsIt.next()).intValue();
      Vector currentVars = node.getParentNodes().getNodes();
      Vector currentVals = new Vector();
      Iterator parIt = currentVars.iterator();
      while(parIt.hasNext())
      {
        FiniteStates parentNode = (FiniteStates)parIt.next();
        int pos                 = (new NodeList(vars)).getId(parentNode);
        currentVals.addElement(new Integer(conf.getValue(pos)));
      }//while
      
      currentVars.insertElementAt(node,0);
      currentVals.insertElementAt(new Integer(val),0);
      Configuration newConf = new Configuration(currentVars,currentVals);
      
      probabilityValue *= this.getSimpleProbability(newConf);      
    }//end for_i  
    //save teh probability value for the data case into the hashtable for a future use
    this.jointProbs.put(conf,new Double(probabilityValue));
    
    return probabilityValue;
  }//calculateJointProbability(Configuration conf)
  
  /**
   * Returns the nodeList that codify the relation between variables
   * @return nodelist
   */
  public NodeList getNodeList()
  {
    return this.S.nodeList;
  }
  
  public double getSimpleProbability(Configuration conf)
  {
    if(simpleProbs.containsKey(conf))
    {
      return ((Double)simpleProbs.get(conf)).doubleValue();
    }//if(simpleProbs.containsKey(conf))
    else
    {
      return this.calculateSimpleProbability(conf);
    }
  }//getSimpleProbability(Configuration conf)
  
  /**
   * Returns the value for the conditional probability value p(c|x0,...,xn)
   * @param conf configuration which codify a data sample
   * @return value of the conditional probability
   * @throws java.lang.VerifyError
   */
  public double getConditionalProbability(Configuration conf) throws VerifyError
  {
    if(isCalculatedConditional(conf))
    {
      return ((Double)conditionalProbs.get(conf)).doubleValue();
    }//end if(isCalculated(conf))
    else
    { 
      return calculateConditionalProbability(conf);
    }
  }
  
  public double getLogConditionalProbability(Configuration conf) throws VerifyError
  {
    /*if(isCalculated(conf))
    {
      return ((Double)probs.get(conf)).doubleValue();
    }//end if(isCalculated(conf))
    else
    { */
    return this.calculateLogConditionalProbability(conf);
    //}
  }
  
  public double getJointProbability(Configuration conf) throws VerifyError
  {
    if(isCalculatedJoint(conf))
    {
      return ((Double)jointProbs.get(conf)).doubleValue();
    }//end if(isCalculated(conf))
    else
    { 
      return calculateJointProbability(conf);
    }
  }//getJoinProbability(Configuration conf)
  
  /**
   * Returns true if the probability value for conf is already calculated and saved
   * into the hashtable simpleProbs
   * @param conf Configuration which represent a probability
   */
  public boolean isCalculatedSimple(Configuration conf)
  {    
    return simpleProbs.containsKey(conf);
  }
  
  /**
   * Returns true if the probability value for conf is already calculated and saved
   * into the hashtable jointProbs
   * @param conf Configuration which represent a probability
   */
  public boolean isCalculatedJoint(Configuration conf)
  {    
    return jointProbs.containsKey(conf);
  }
  
  /**
   * Initialize all the hash tables in the current ProbabilityList
   */
  public void initialize()
  {
    simpleProbs      = new Hashtable();
    jointProbs       = new Hashtable();
    conditionalProbs = new Hashtable();
  }//initialize()
  
  /**
   * Returns true if the probability value for conf is already calculated and saved
   * into the hashtable conditionalProbs
   * @param conf Configuration which represent a probability
   */
  public boolean isCalculatedConditional(Configuration conf)
  {    
    return this.conditionalProbs.containsKey(conf);
  }
  
  /**
   * returns true if laplace correction is used when calculating the probabilities and not otherwise
   * @return the value of 'laplace' variable
   */
  public boolean usingLaplace()
  {
    return this.laplace;
  }
}