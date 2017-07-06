package elvira.learning.classification.supervised.discrete;

import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Link;
import elvira.LinkList;
import elvira.NodeList;
import elvira.database.DataBaseCases;
import elvira.tools.Jama.Matrix;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;


public class DiscriminativeSufficientStatistics extends SufficientStatistics 
{
  public DiscriminativeSufficientStatistics(NodeList nl)
  {
      int i;
      Iterator nodesIt;
         
      nodeList  = nl;
      nVar      = nl.size() - 1; //class is not included
      varStates = new int[nVar];
      parStates = new int[nVar];
      
      FiniteStates classNode = (FiniteStates)nl.elementAt(nVar);
      // in the discriminative sufficient statistics, the discriminative sufficient statistics 
      // related to C are: M1,...,Mr0, i.e. the number of number of states for C are r0-1
      cStates                = classNode.getNumStates() - 1;
      Sc                     = new double [cStates];
      Scxi                   = new ArrayList();
      Scxixji                = new ArrayList();
      // initialize the sufficien statistics (the default value is 0)
      nodesIt = nl.getNodes().iterator();
      for(i=0;i<nVar;i++)
      {        
        FiniteStates node   = (FiniteStates)nodesIt.next();
        // in the discriminative sufficient statistics, the discriminative sufficient statistics 
        // related to C and Xi are: M11,...,Mr01,...,Mri1,...,Mrir0, i.e. the number of number of states 
        // for C are r0-1 and for Xi are ri-1
        varStates[i] = node.getNumStates()-1;
        // the DiscreteClassifierdiscriminativeLearning includes models up to TAN,
        // so each variable can only has up to two parents: a predictive variable
        // and C        
        parStates[i] = 0;//if Xi has only C as a parent, parStates[i] = 0
        LinkList parentsL = node.getParents();
        Iterator linksIt = parentsL.getLinks().iterator();
        while(linksIt.hasNext())
        {
          FiniteStates parent = (FiniteStates)((Link)linksIt.next()).getTail();
          if(parent.getComment().compareTo("ClassNode") != 0)
          {
            // the same that have been said for CStates and varStates is valid for parStates
            parStates[i] = parent.getNumStates()-1; 
          }//end if          
        }//end while       
        if(parentsL.size() == 0)
        {// Xi has no parents
          Scxi.add(null);
          Scxixji.add(null);       
        }//(parentsL.size() == 0)
        else if(parentsL.size() == 1)
        {// Xi has only one parent
          if(parStates[i] == 0)
          {// Xi's parent is C
            Scxi.add(new double [cStates * varStates[i]]);
            Scxixji.add(null);      
          }//if(parStates[i] == 0)
          else
          {// Xi's parent is another predictive variable, Xji
            Scxi.add(null);
            Scxixji.add(null);            
          }//else -> !(parStates[i] == 0)
          
        }//elseif(parentsL.size() == 1)
        else if(parentsL.size() == 2)
        {// Xi has two parents: C and Xji
          Scxi.add(new double [cStates * varStates[i]]);
          Scxixji.add(new double [cStates * varStates[i] * parStates[i]]);      
        }//elseif(parentsL.size() == 2)
      }//end for_i    
  }//DiscriminativeSufficientStatistics
  
    /**
   * calculate the values of the discriminative sufficient statistics in a new object 
   * according to the way that the TM algorithm does --> M = Mcurrent + M_0 - E.
   * Mcurrent, M_0 and E must codify discriminative sufficient statistics and expected
   * suffcient statistics for the same structure (same dependencies in the nodelist)
   * @param M_curent current values for the discriminative sufficient statistics
   * @param M0 initial values for the discriminative sufficient statistics
   * @param E expected values for the discriminative sufficient statistics   
   */
    public static DiscriminativeSufficientStatistics calculateStatistics(DiscriminativeSufficientStatistics M_current, 
                                                                          DiscriminativeSufficientStatistics M_0, 
                                                                          ExpectedSufficientStatistics E)
    {
      return calculateStatistics(M_current,M_0,E,1);
    }//DiscriminativeSufficientStatistics calculateStatistics(DiscriminativeSufficientStatistics M_current, ...)
   
   /**
   * calculate the values of the discriminative sufficient statistics in a new object 
   * according to the way that the TM algorithm does --> M = Mcurrent + lambda*(M_0 - E).
   * Both Mcurrent and M_0 must codigy discriminative sufficient statistics for the same
   * structure (same dependencies in the nodelist)
   * @param M_curent current values for the discriminative sufficient statistics
   * @param M0 initial values for the discriminative sufficient statistics
   * @param E expected values for the discriminative sufficient statistics
   * @param lambda multiplier value
   */
    public static DiscriminativeSufficientStatistics calculateStatistics(DiscriminativeSufficientStatistics M_current, 
                                                                          DiscriminativeSufficientStatistics M_0, 
                                                                          ExpectedSufficientStatistics E, double lambda)
    {
      int i;
      Iterator nodeIt;   
      
      //Matrix objects used to store partial solutions
      Matrix aux;
    
      DiscriminativeSufficientStatistics M = new DiscriminativeSufficientStatistics(M_0.getNodeList());   
      if(lambda == 0)
      { //the discriminative sufficient statistics does not change.     
        M.copyFrom(M_current);
      }
      else
      {        
        //set the discriminative suficient statistics related to C (Sc)
        aux   = M_0.getScMatrix().minus(E.getScMatrix());
        if(lambda != 1)
        {
          aux.timesEquals(lambda);  
        }        
        aux.plusEquals(M_current.getScMatrix());
        M.setSc(aux);
      
        //update the generative suficient statistics related to Xi (only Scxi and Scxixji when they exist)
        nodeIt = M.getNodeList().getNodes().iterator();
        for(i=0;i<M.nVar;i++)
        {
          FiniteStates node = (FiniteStates)nodeIt.next();
          LinkList    links = node.getParents();
          if(links.size() == 1)
          {
            FiniteStates parent = (FiniteStates)links.elementAt(0).getTail();
            if(parent.getComment().compareToIgnoreCase("ClassNode") == 0)
            {// the unique parent is the class variable (C) => we only have to update Scxi and not Scxixji 
              aux   = M_0.getScxiMatrix(i).minus(E.getScxiMatrix(i));
              if(lambda != 1)
              {
                aux.timesEquals(lambda);  
              }              
              aux.plusEquals(M_current.getScxiMatrix(i));
              M.setScxi(i,aux);
            }//if(parent.getComment().compareToIgnoreCase("ClassNode") == 0)
          }//if(links.size() == 1)
          else if(links.size() == 2)
          {//The current variable has two parents: a predictive variable (Xji) and C
           // => we have to update both Scxi and Scxixji
           //Scxi
            aux   = M_0.getScxiMatrix(i).minus(E.getScxiMatrix(i));
            if(lambda != 1)
            {
              aux.timesEquals(lambda); 
            }            
            aux.plusEquals(M_current.getScxiMatrix(i));
            M.setScxi(i,aux);
            //Scxixji
            aux   = M_0.getScxixjiMatrix(i).minus(E.getScxixjiMatrix(i));
            if(lambda != 0)
            {
              aux.timesEquals(lambda); 
            }           
            aux.plusEquals(M_current.getScxixjiMatrix(i));
            M.setScxixji(i,aux);
          }//else if(links.size() == 2)        
        }//for(i=0;i<this.nVar;i++)*/
      }//else -> (lambda != 0)
      return M;
    }//updateStatistics(M_current, M_0, E, lambda)
    
  /**
   * copy the values of the Suficient Statistics in s into the current object
   * @param s Sufficient Statistic object
   */
    public void copyFrom(DiscriminativeSufficientStatistics s)
    {
      int i,j;
      
      if (Sc.length != s.Sc.length           || Scxi.size() != s.Scxi.size() 
       || Scxixji.size() != s.Scxixji.size())
      {
        System.out.println("ERROR in SufficientStatistics copyFrom");
        System.exit(-1);
      }//end if
      
      for(i=0;i<Sc.length;i++)
      {
        Sc[i] = s.Sc[i];
      }//end for_i
      
      for(i=0;i<nVar;i++)
      {
        if(Scxixji.get(i) != null)
        {       
          if (((double[])Scxi.get(i)).length  != ((double[])s.Scxi.get(i)).length 
          || ((double[])Scxixji.get(i)).length != ((double[])s.Scxixji.get(i)).length)
          {
            System.out.println("ERROR in SufficientStatistics copyFrom");
            System.exit(-1);
          }//end if

          for(j=0;j<((double [])Scxixji.get(i)).length;j++)
          {
            if (j < ((double[])Scxi.get(i)).length)
            {
              ((double [])Scxi.get(i))[j] = ((double [])s.Scxi.get(i))[j];            
            }// if (j < ((double[])Scxi.get(i)).length)                       
            ((double [])Scxixji.get(i))[j] = ((double [])s.Scxixji.get(i))[j];
          }// for_j
        }//if(Scxixji.get(i) != null)
        else if(Scxi.get(i) != null)
        {
          if (((double[])Scxi.get(i)).length  != ((double[])s.Scxi.get(i)).length)
          {
            System.out.println("ERROR in SufficientStatistics copyFrom");
            System.exit(-1);
          }//end if

          for(j=0;j<((double [])Scxi.get(i)).length;j++)
          {            
              ((double [])Scxi.get(i))[j] = ((double [])s.Scxi.get(i))[j];                       
          }// for_j
        }//else if(Scxi.get(i) != null)
      }// for_i      
    }// copyFrom(DiscriminativeSufficientStatistics s)
    
    /**
   * returns the nodeList that codify the dependencies between all the variables 
   * @return nodeList
   */
    public NodeList getNodeList()
    {
      return this.nodeList;
    }//public NodeList getNodeList()
    /**
   * initialize the values of all the Discriminative Suficient Statistics to val
   * @param val value to initialize the current SufficientStatistic
   */
    public void initialize(double val)
    {
      int i,j;
            
      for(i=0;i<Sc.length;i++)
      {
        Sc[i] = val;
      }//end for_i
      
      for(i=0;i<nVar;i++)
      {
        for(j=0;j<((double [])Scxixji.get(i)).length;j++)
        {
          if (j < ((double[])Scxi.get(i)).length)
          {
            ((double [])Scxi.get(i))[j] = val;            
          }// if (j < ((double[])Scxi.get(i)).length)             
          ((double [])Scxixji.get(i))[j] = val;
        }// for_j
      }// for_i      
    }// initialize(double val)
    
    /**
   * update the values of the current discriminative sufficient statistics accordint to the way that the TM
   * algorithm does --> M = M + M_0 - E.
   * @param M0 initial values for the discriminative sufficient statistics
   * @param E expected values for the discriminative sufficient statistics   
   */
    public void updateStatistics(DiscriminativeSufficientStatistics M_0, ExpectedSufficientStatistics E)
    {
      this.updateStatistics(M_0,E,1);
    }//updateStatistics(DiscriminativeSufficientStatistics M_0, ExpectedSufficientStatistics E)
    
    /**
   * update the values of the current discriminative sufficient statistics accordint to the way that the TM
   * algorithm does --> M = M + lambda*(M_0 - E).
   * @param M0 initial values for the discriminative sufficient statistics
   * @param E expected values for the discriminative sufficient statistics
   * @param lambda multiplier value
   */
    public void updateStatistics(DiscriminativeSufficientStatistics M_0, ExpectedSufficientStatistics E, double lambda)
    {
      int i;
      Iterator nodeIt;   
      
      //Matrix objects used to store partial solutions
      Matrix aux;
    
      if(lambda != 0)
      { //the discriminative sufficient statistics does change (if lambda ==0 it does not change).     
        //set the discriminative suficient statistics related to C (Sc)
        aux   = M_0.getScMatrix().minus(E.getScMatrix());
        if(lambda != 1)
        {
          aux.timesEquals(lambda);  
        }        
        this.getScMatrix().plusEquals(aux);
      
        //update the generative suficient statistics related to Xi (only Scxi and Scxixji when they exist)
        nodeIt = nodeList.getNodes().iterator();
        for(i=0;i<this.nVar;i++)
        {
          FiniteStates node = (FiniteStates)nodeIt.next();
          LinkList    links = node.getParents();
          if(links.size() == 1)
          {
            FiniteStates parent = (FiniteStates)links.elementAt(0).getTail();
            if(parent.getComment().compareToIgnoreCase("ClassNode") == 0)
            {// the unique parent is the class variable (C) => we only have to update Scxi and not Scxixji 
              aux   = M_0.getScxiMatrix(i).minus(E.getScxiMatrix(i));
              if(lambda != 1)
              {
                aux.timesEquals(lambda);  
              }              
              this.getScxiMatrix(i).plusEquals(aux);
            }//if(parent.getComment().compareToIgnoreCase("ClassNode") == 0)
          }//if(links.size() == 1)
          else if(links.size() == 2)
          {//The current variable has two parents: a predictive variable (Xji) and C
           // => we have to update both Scxi and Scxixji
           //Scxi
            aux   = M_0.getScxiMatrix(i).minus(E.getScxiMatrix(i));
            if(lambda != 1)
            {
              aux.timesEquals(lambda); 
            }            
            this.getScxiMatrix(i).plusEquals(aux);
            //Scxixji
            aux   = M_0.getScxixjiMatrix(i).minus(E.getScxixjiMatrix(i));
            if(lambda != 0)
            {
              aux.timesEquals(lambda); 
            }           
            this.getScxixjiMatrix(i).plusEquals(aux);          
          }//else if(links.size() == 2)        
        }//for(i=0;i<this.nVar;i++)*/
      }//if(lambda != 0)
    }//updateStatistics(DiscriminativeSufficientStatistics M_0, ExpectedSufficientStatistics E, double lambda)
 
}