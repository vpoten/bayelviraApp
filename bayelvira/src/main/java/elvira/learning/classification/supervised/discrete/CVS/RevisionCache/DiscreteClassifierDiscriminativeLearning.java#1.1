package elvira.learning.classification.supervised.discrete;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Link;
import elvira.LinkList;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.database.DataBaseCases;
import elvira.potential.PotentialTable;
import elvira.tools.Jama.LUDecomposition;
import elvira.tools.Jama.Matrix;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public abstract class DiscreteClassifierDiscriminativeLearning extends DiscreteClassifier 
{ 
   
   /**
    * if verbose == true -> print information in the screen
    */
   protected boolean verbose;
   
   /**
   * Constructor
   * @param DataBaseCases cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public DiscreteClassifierDiscriminativeLearning()
  {
    super();
    verbose = false;
  }//DiscreteClassifierDiscriminativeLearning() throws elvira.InvalidEditException

  /**
   * Constructor
   * @param DataBaseCases cases. The input to learn a classifier
   * @param boolean lap. To apply the laplace correction
   */
  public DiscreteClassifierDiscriminativeLearning(DataBaseCases data, boolean lap) throws elvira.InvalidEditException
  {
    super(data,lap);
    verbose = false;
  }//DiscreteClassifierDiscriminativeLearning(DataBaseCases data, boolean lap) throws elvira.InvalidEditException
  
  public double logConditionalL(ProbabilityList p)
  {
    int i;
    int [] dataSample;
    Iterator varsIt;
    
    double logConditionalLValue = 0;
            
    int classValue;
    Iterator dataIt = this.cases.getCaseListMem().getCases().iterator();
    Vector   vars   = (Vector)p.getNodeList().getNodes().clone();
    // get and remove the class node from the nodeList. The class node must be
    // the first element in the nodelist in order to construct a configuration
    // object and obtain the conditional probability of the case codified by 
    // the given configuration.
    FiniteStates classNode = (FiniteStates)vars.lastElement();
    /**vars.removeElementAt(vars.size()-1);*/
    while(dataIt.hasNext())
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
      // include the ClassNode and the value for the ClassNode in the last
      // position.
      vals.addElement(new Integer(dataSample[dataSample.length-1]));
      /**vars.insertElementAt(classNode,0);
      //vals.insertElementAt(new Integer(dataSample[dataSample.length-1]),0);
      */
        
      Configuration conf = new Configuration(vars,vals);
      try
      {
        //double aux = Math.log(p.getProbability(conf));
        double aux = p.getLogConditionalProbability(conf);
        //System.out.println(aux);
        logConditionalLValue += aux;
      }
      catch(VerifyError e)
      {
        System.out.println("ERROR when calculation the conditional log-likelihood: the current values for the sufficient statistics are illegal");      
        System.exit(-1);
      }//catch(VerifyError e)    
      //System.out.println(logConditionalLValue);
      
    }// while     
    return logConditionalLValue;
	}//end logConditionalL
  
  /**
   * Set the verbose flag on, print information in the standar output
   */
  public void setVerboseOn()
  {
    this.verbose = true;
  }//setVerboseOn()
  
  /**
   * Set the verbose flag off, do not print information in the standar output
   */
  public void setVerboseOff()
  {
    this.verbose = false;
  }//setVerboseOf()  
  
  /**
   * Performs a discriminative learning for the parameters of the classifier (the learning maximizes the
   * conditional log-likelihood). The method returns the final result for the conditional log-likelihood. 
   * The learned parameters are saved into the conditional probability tables (PotentialTables) only 
   * if it is required.
   * @return the value of the Conditional Log-Likelihood
   */
  public double TM()
	{    
    // constant to determine the stopping criterion, if the diference of the 
    // conditional log-likelihood in two consecutive steps is less than epsilon 
    // the algorithm stops
    final double epsilon = 0.001;
    
    double logCondL1, logCondL2;    
    GenerativeSufficientStatistics N;
    DiscriminativeSufficientStatistics M_0,M,Maux;
    ExpectedSufficientStatistics E,Eaux;
    Coefficients coeffs;
    NodeList nl;
    
    nl     = this.getClassifier().getNodeList();
    N      = new GenerativeSufficientStatistics(nl);    
    M      = new DiscriminativeSufficientStatistics(nl);    
    coeffs = new Coefficients(nl);    
        
    N.calculateStatistics(this.cases);    
    //
    //GenerativeSufficientStatistics prueba = new GenerativeSufficientStatistics(nl);
    //prueba.copyFrom(N);
    //
    M_0       =  coeffs.calculateDiscriminativeSufficientStatistics(N);    
    //prueba.updateStatistics(coeffs,M_0);
    M.copyFrom(M_0);
    E         = ExpectedSufficientStatistics.calculateExpectedSufficientStatistics(nl,N,this.cases,this.laplace);
    logCondL1 = this.logConditionalL(E.getProbabilityList());    
    logCondL2 = logCondL1; 
    if(this.verbose)
    {
      System.out.println(logCondL2);   
    }//if(this.verbose)
    boolean localSearch = false;
    boolean stop        = false;
    while(!stop)
    {     
      Maux = DiscriminativeSufficientStatistics.calculateStatistics(M,M_0,E);   
      N.updateStatistics(coeffs,Maux);
      Eaux = ExpectedSufficientStatistics.calculateExpectedSufficientStatistics(nl,N,this.cases,this.laplace);
      if(Eaux.isValid() && N.isValid())
      {
        logCondL2 = logConditionalL(Eaux.getProbabilityList());
        if(this.verbose)
        {
          System.out.println(logCondL2);
        }//if(this.verbose)
        if(logCondL2 > logCondL1)
        {          
          M         = Maux;
          E         = Eaux;          
          if(logCondL2 - logCondL1 < epsilon)
          {
            stop = true;
          }//if(logCondL2 - logCondL1 < epsilon)          
          logCondL1 = logCondL2;
        }//if(logCondL2 > logCondL1)
        else if(logCondL2 == logCondL1)
        {
          stop = true;
        }//else if(logCondL2 == logCondL1)
        else//(logCondL2< logCondL1)
        {
          localSearch = true;
        }//else -> logCondL2< logCondL1        
      }//if(Eaux.isValid())
      
      //LOCAL SEARCH
      double bestLambda = 0;
      if(!Eaux.isValid() || localSearch)
      {
        double bestLogCondL = logCondL1;
        ExpectedSufficientStatistics bestE = E;
        DiscriminativeSufficientStatistics bestM = M;
        
        int i                     = 10;
        int increment             = 10;
        boolean finishLocalSearch = false;
        //for(int i=1;i<100;i += increment)        
        while(i<100 && !finishLocalSearch)
        {          
          double lambda = (double)i / 100;              
          Maux = DiscriminativeSufficientStatistics.calculateStatistics(M,M_0,E,lambda);   
          N.updateStatistics(coeffs,Maux);
          Eaux = ExpectedSufficientStatistics.calculateExpectedSufficientStatistics(nl,N,this.cases,this.laplace);
          if(Eaux.isValid() && N.isValid())
          {
            logCondL2 = logConditionalL(Eaux.getProbabilityList());
            if(logCondL2 > bestLogCondL)
            {
              bestLambda = lambda;
              bestM        = Maux;
              bestLogCondL = logCondL2;
              bestE        = Eaux;
            }//if(logCondL2 > bestLogCondL)            
          }//if(Eaux.isValid())
          else if((!Eaux.isValid() || !N.isValid()) && increment == 10)
          {
            // we reset the value of i to its value in the previous iteration and refine the local 
            // search using increment = 1
            i -= 10;
            increment = 1;
          } 
          else if((!Eaux.isValid() || !N.isValid()) && increment == 1)
          {
            // we finish the local search
            finishLocalSearch = true;
          }
          i += increment;
        }//while(i<100 || !finishLocalSearch)
        
        if(bestLogCondL > logCondL1)
        {        
          M = bestM;
          E = bestE;
          if(bestLogCondL - logCondL1 < epsilon)
          {
            stop = true;
          }//if(bestLogCondL - logCondL1 < epsilon)
          logCondL1 = bestLogCondL;
          if(this.verbose)
          {
            System.out.println(bestLogCondL);
          }//if(this.verbose)
        }//(bestLogCondL > logCondL1)
        else 
        {//(bestLogCondL == logCondL1) because bestLogCondL starts with the value logCondL
          stop = true;
        }//else -> (bestLogCondL == logCondL1)      
      }//if(!Eaux.isValid() || localSearch)
    }//while(!stop)    
    
    /** 
     * Set the calculated discriminative parameters into the classifier
     */      
    double [] cpt; //conditional potential table where we store p(Xi=xi|Pai=pai)    
    NodeList nodeList = this.classifier.getNodeList();      
    Iterator varsIt = nodeList.getNodes().iterator();
    Iterator relsIt = this.classifier.getRelationList().iterator();
    for(int i=0;i<this.nVariables-1;i++)
    {
      FiniteStates varXi = (FiniteStates)varsIt.next();      
      if(varXi.getParents().size() == 0)
      {// varXi has no parents
        // calculate the conditional potential table for the current variable
        int index          = 0; // index indicate the current position of cpt that it is being calculated. 
        cpt                = new double [varXi.getNumStates()];
        Vector variables   = new Vector();
        variables.addElement(varXi);
        Configuration conf = new Configuration(variables);
        for(int valXi=0;valXi<varXi.getNumStates();valXi++)
        {
          conf.getValues().setElementAt(new Integer(valXi),0);
          cpt[index] = E.getProbabilityList().getSimpleProbability(conf);
          index ++;
        }//for(int valXi=0;valXi<varXi.getNumStates();valXi++)        
      }//if(varXi.getParents().size() == 0)
      else if(varXi.getParents().size() == 1)
      {// the unique parent of varXi is the class variable or another predictive variable Xij
        FiniteStates parXi = (FiniteStates)varXi.getParents().elementAt(0).getTail();               
        // calculate the conditional potential table for the current variable
        int index          = 0; // index indicate the current position of cpt that it is being calculated. 
        cpt                = new double [varXi.getNumStates() * parXi.getNumStates()];
        Vector variables   = new Vector();
        variables.addElement(varXi);
        variables.addElement(parXi);
        Configuration conf = new Configuration(variables);
        for(int valXi=0;valXi<varXi.getNumStates();valXi++)
        {
          conf.getValues().setElementAt(new Integer(valXi),0);
          for(int valParXi=0;valParXi<parXi.getNumStates();valParXi++)
          {
            conf.getValues().setElementAt(new Integer(valParXi),1);
            cpt[index] = E.getProbabilityList().getSimpleProbability(conf);
            index ++;
          }//for(int valParXi=0;valParXi<parXi.getNumStates();valParXi++)          
        }//for(int valXi=0;valXi<varXi.getNumStates();valXi++)                       
      }//else if(varXi.getParents().size() == 1)
      else 
      {// varXi.getParents().size() == 2 -> the parents of varXi are both the class variable and Xij
        Iterator linksIt       = varXi.getParents().getLinks().iterator();
        FiniteStates parXi     = (FiniteStates)((Link)linksIt.next()).getTail();               
        FiniteStates classNode = (FiniteStates)((Link)linksIt.next()).getTail();               
        // calculate the conditional potential table for the current variable
        int index          = 0; // index indicate the current position of cpt that it is being calculated. 
        cpt                = new double [varXi.getNumStates() * parXi.getNumStates() * classNode.getNumStates()];
        Vector variables   = new Vector();
        variables.addElement(varXi);
        variables.addElement(parXi);
        variables.addElement(classNode);
        Configuration conf = new Configuration(variables);
        for(int valXi=0;valXi<varXi.getNumStates();valXi++)
        {
          conf.getValues().setElementAt(new Integer(valXi),0);
          for(int valParXi=0;valParXi<parXi.getNumStates();valParXi++)
          {
            conf.getValues().setElementAt(new Integer(valParXi),1);
            for(int valC=0;valC<classNode.getNumStates();valC++)
            {
              conf.getValues().setElementAt(new Integer(valC),2);  
              int h = conf.hashCode();
              cpt[index] = E.getProbabilityList().getSimpleProbability(conf);
              index ++;
            }//for(int valC=0;valC<classNode.getNumStates();valC++)            
          }//for(int valParXi=0;valParXi<parXi.getNumStates();valParXi++)          
        }//for(int valXi=0;valXi<varXi.getNumStates();valXi++)                       
      }//else -> the parents of varXi are both the class variable and Xij
      // store the potentials into the potential table of the classifier
      Relation relation = (Relation)relsIt.next();
      PotentialTable potential = (PotentialTable)relation.getValues();
      potential.setValues(cpt);      
    }//for(int i=0;i<this.nVariables;i++)
    //Finally we set the calculated discriminative parameters for the class node
    FiniteStates classNode = (FiniteStates)varsIt.next();      
    // calculate the conditional potential table for the current variable
    int index          = 0; // index indicate the current position of cpt that it is being calculated. 
    cpt                = new double [classNode.getNumStates()];
    Vector variables   = new Vector();
    variables.addElement(classNode);
    Configuration conf = new Configuration(variables);
    for(int valC=0;valC<classNode.getNumStates();valC++)
    {
      conf.getValues().setElementAt(new Integer(valC),0);
      cpt[index] = E.getProbabilityList().getSimpleProbability(conf);
      index ++;
    }//for(int valC=0;valC<classNode.getNumStates();valC++)
    // store the potentials into the potential table of the classifier
    Relation relation = (Relation)relsIt.next();
    PotentialTable potential = (PotentialTable)relation.getValues();
    potential.setValues(cpt);      
    
    return logCondL1;
  }//TM()
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception
  {
    /*Matrix m = Matrix.identity(4,4);
    LUDecomposition lu = m.lu();
    m.timesEquals(2);
    
    Matrix m2 = Matrix.random(4,4);
    m.plusEquals(m2);
    
    System.out.println();
    
    FileOutputStream out = new FileOutputStream("/home/guzman/prueba.conf");
    PrintWriter p = new PrintWriter(out);
    lu.print(p,10);
    p.close();
    out.close();*/
    
    //lu = LUDecomposition.read(new FileReader("/home/guzman/prueba.conf"));
   // DiscreteClassifierDiscriminativeLearning discreteClassifierDiscriminativeLearning = new DiscreteClassifierDiscriminativeLearning();
  }
}