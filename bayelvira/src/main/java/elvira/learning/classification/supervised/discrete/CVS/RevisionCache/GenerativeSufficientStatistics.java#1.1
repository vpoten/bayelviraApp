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


public class GenerativeSufficientStatistics extends SufficientStatistics 
{
    private boolean   isValid;//Indicates if the current set of sufficient statistics is or not valid 
    double    nCases; //Sufficient Stateitics that counts the number of cases   
    ArrayList Sxi;    //Sufficient Statistics related only to Xi
    ArrayList Sxixji; //Sufficient Statistics related to Xi and Xj(i) (parent of Xi)
    
    /* NOTE: due to precision error, the solution of the systems of equations may give
    * negative values close to zero for some sufficient statistics. In order to solve this 
    * problem, we will consider as 0 any value between PRECISIONLIMIT and 0
    */ 
    private static final double PRECISIONLIMIT = -1000; 
    
               
    public GenerativeSufficientStatistics(NodeList nl)
    {      
      int i;
      Iterator nodesIt;      
      nodeList = nl;   
      nVar      = nl.size() - 1; //class is not included
      varStates = new int[nVar];
      parStates = new int[nVar];
      
      isValid                = true;
      FiniteStates classNode = (FiniteStates)nl.elementAt(nVar);
      cStates                = classNode.getNumStates();
      Sc                     = new double [cStates];
      Scxi                     = new ArrayList();
      Scxixji                     = new ArrayList();
      Sxi                     = new ArrayList();
      Sxixji                     = new ArrayList();
      
      // initialize the sufficien statistics (the default value is 0)
      nodesIt = nl.getNodes().iterator();
      for(i=0;i<nVar;i++)
      {        
        FiniteStates node   = (FiniteStates)nodesIt.next();
        varStates[i] = node.getNumStates();
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
            parStates[i] = parent.getNumStates(); 
          }//end if          
        }//end while       
        if(parentsL.size() == 0)
        {// Xi has no parents
          Scxi.add(null);
          Scxixji.add(null);      
          Sxi.add(new double [varStates[i]]);
          Sxixji.add(null);     
        }//(parentsL.size() == 0)
        else if(parentsL.size() == 1)
        {// Xi has only one parent
          if(parStates[i] == 0)
          {// Xi's parent is C
            Scxi.add(new double [cStates * varStates[i]]);
            Scxixji.add(null);      
            Sxi.add(new double [varStates[i]]);
            Sxixji.add(null);          
          }//if(parStates[i] == 0)
          else
          {// Xi's parent is another predictive variable, Xji
            Scxi.add(null);
            Scxixji.add(null);
            Sxi.add(new double [varStates[i]]);
            Sxixji.add(new double [varStates[i] * parStates[i]]);
          }//else -> !(parStates[i] == 0)
          
        }//elseif(parentsL.size() == 1)
        else if(parentsL.size() == 2)
        {// Xi has two parents: C and Xji
          Scxi.add(new double [cStates * varStates[i]]);
          Scxixji.add(new double [cStates * varStates[i] * parStates[i]]);      
          Sxi.add(new double [varStates[i]]);
          Sxixji.add(new double [varStates[i] * parStates[i]]);
        }//elseif(parentsL.size() == 2)
      }//end for_i  
    }//end SufficientStatistics(NodeList nl)
    
    /**
     * Adds the value in val to the value in nCases. nCases = nCases + val
     * @param val values to add to nCases
     */
    public void addToNCases(double val)
    {
      nCases += val;      
    }//end addToNCases(val)
    
    /**
     * Adds the value in val to the values in Sxi. Sxi and val must have the same
     * size. Sxi = Sxi + val
     * @param val values to add to Sxi
     */
    public void addToSxi(int Xi, double[] val)
    {
      int i;
      if(((double[])Sxi.get(Xi)).length != val.length)
      {
        System.out.println("ERROR in addToSxi from SufficientStatistics Sxi and val must have the same size");
        System.exit(-1);
      }//end if
      for(i=0;i<val.length;i++)
      {
        ((double[])Sxi.get(Xi))[i] += val[i];
      }//for_i
    }//addToSxi(int Xi, double[] val)
    
     /**
     * Adds the value in val to the corresponding value in Sxi.
     * @param val values to add to Sxi
     */
    public void addToSxi(int Xi, int xiIndex, double val)
    {      
      double[] d = (double[])Sxi.get(Xi);      
      if(xiIndex > d.length)
      {
        System.out.println("ERROR in addToSxi from SufficientStatistics index must be < Sxi[i].length");
        System.exit(-1);
      }//end if      
      d[xiIndex] += val;
    }//end addToSxi(int Xi, int xiIndex, double val)
    
    /**
     * Adds the value in val to the values in Sxi. Sxi and val must have the same
     * size. Sxi = Sxi + val
     * @param val values to add to Sxi
     */
    public void addToSxi(int Xi,Matrix val)
    {
      addToSxi(Xi,val.getColumnPackedCopy());      
    }//addToSxi(int Xi,Matrix val)
    
    /**
     * Adds the value in val to the values in Sxixji. Sxixji and val must have the same
     * size. Sxixji = Sxixji + val
     * @param val values to add to Sxixji
     */
    public void addToSxixji(int Xi, double[] val)
    {
      int i;
      if(((double[])Sxixji.get(Xi)).length != val.length)
      {
        System.out.println("ERROR in addToSxixji from SufficientStatistics Sxixji and val must have the same size");
        System.exit(-1);
      }//end if
      for(i=0;i<val.length;i++)
      {
        ((double[])Sxixji.get(Xi))[i] += val[i];
      }//for_i
    }//addToSxixji(int Xi, double[] val)
    
    /**
     * Adds the value in val to the corresponding value in Sxixji.
     * @param val values to add to Sxixji
     */
    public void addToSxixji(int Xi, int xiIndex, int xjiIndex, double val)
    {      
      double[] d = (double[])Sxixji.get(Xi);
      int index = (xjiIndex * varStates[Xi]) + xiIndex;
      if(index > d.length)
      {
        System.out.println("ERROR in addToSxixji from SufficientStatistics index must be < Sxixji[i].length");
        System.exit(-1);
      }//end if      
      d[index] += val;
    }//end addToSxixji(int Xi, int xiIndex, int xjiIndex, double val)
     
    /**
     * Adds the value in val to the values in Sxixji. Sxixji and val must have the same
     * size. Sxixji = Sxixji + val
     * @param val values to add to Sxixji
     */
    public void addToSxixji(int Xi,Matrix val)
    {
      addToSxi(Xi,val.getColumnPackedCopy());      
    }//addToSxixji(int Xi,Matrix val)
    
    /**
   * Calculate the value of the ML generative sufficient statistics from the Data cases
   * @param dbc DataBaseCases to calculate the statistics.
   */
    public void calculateStatistics(DataBaseCases dbc)
    {      
      int i;
      int [] dataSample;
      Iterator dataIt = dbc.getCaseListMem().getCases().iterator();
      Vector   vars  = this.nodeList.getNodes();
      while(dataIt.hasNext())
      {
        dataSample = (int[]) dataIt.next();
        Vector vals = new Vector();
        for(i=0;i<dataSample.length;i++)
        {
          vals.addElement(new Integer(dataSample[i]));
        }// for_i
        Configuration conf = new Configuration(vars,vals);
        this.countCase(conf);
      }// while     
    }//end calculateStatistics(DataBaseCases dbc)   
   
    /**
   * Adds 1 to the correspondig sufficient statiscs which are given by configuration
   * The order of the variables for the sufficient statistics and for the configuration
   * are suposed to be the same (and the same size).
   * @param conf codify a data case
   */
    public void countCase(Configuration conf)
    {
      int i;
      int [] values = new int [conf.getValues().size()];
      int    cVal;
      Iterator it;
         
      this.nCases ++; 
      
      it = conf.getValues().iterator();
      for(i=0;it.hasNext();i++)
      {
        values[i] = ((Integer)it.next()).intValue();
      }//for_i
      cVal = values[values.length - 1];
      
      it = conf.getVariables().iterator();
      for(i=0;i<this.nVar;i++)
      {
        FiniteStates Xi  = (FiniteStates) it.next();
        LinkList parentsLn = Xi.getParents();
        if(parentsLn.size() == 2)
        {
          // first parent is Xj(i) and the second one is the ClassNode
          FiniteStates Xji = ((FiniteStates)parentsLn.elementAt(0).getTail());
          int j = (new NodeList(conf.getVariables())).getId(Xji);
          addToScxixji(i,cVal,values[i],values[j],1);
          addToSxixji(i,values[i],values[j],1);
          addToScxi(i,cVal,values[i],1);
          addToSxi(i,values[i],1);
        }//end if
        else if(parentsLn.size() == 1)
        {
          FiniteStates Xji = ((FiniteStates)parentsLn.elementAt(0).getTail());
          if(Xji.getComment().compareTo("ClassNode") == 0) // the parent is the class node
          {
            addToScxi(i,cVal,values[i],1);
            addToSxi(i,values[i],1);
          }//end if
          else // the parent is another variable
          {
            int j = (new NodeList(conf.getVariables())).getId(Xji);
            addToSxixji(i,values[i],values[j],1);
            addToSxi(i,values[i],1);
          }//end else         
        }// end else if
        else if(parentsLn.size() == 0)
        {
          addToSxi(i,values[i],1);
        }//end else if
      }// end for_i
      // add the 1 to the statististic corresponding only to the class variable
      addToSc(cVal,1);
    }//end countCase
    
    /**
   * Makes a copy of the current object
   * @return a GenerativeSufficientStatistics object which is a copy of the current one
   
    public GenerativeSufficientStatistics copy()
    {     
      GenerativeSufficientStatistics N = new GenerativeSufficientStatistics(this.nodeList.copy());
      N.copyFrom(this);
    }
   */ 
    /**
   * copy the values of the Suficient Statistics in s into the current object
   * @param s Sufficient Statistic object
   */
    public void copyFrom(GenerativeSufficientStatistics s)
    {
      int i,j;
      
      this.nCases = s.nCases;
      if (Sc.length != s.Sc.length           || Scxi.size() != s.Scxi.size() 
       || Scxixji.size() != s.Scxixji.size() || Sxi.size() != s.Sxi.size() 
       || Sxixji.size() != s.Sxixji.size())
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
        if ((Scxi.get(i) != null && ((double[])Scxi.get(i)).length  != ((double[])s.Scxi.get(i)).length) 
        || (Scxixji.get(i) != null && ((double[])Scxixji.get(i)).length != ((double[])s.Scxixji.get(i)).length)
        || ((double[])Sxi.get(i)).length     != ((double[])s.Sxi.get(i)).length
        || (Sxixji.get(i) != null && ((double[])Sxixji.get(i)).length  != ((double[])s.Sxixji.get(i)).length))
        {
          System.out.println("ERROR in SufficientStatistics copyFrom");
          System.exit(-1);
        }//end if

        // Scxixji(i), Scxi(i), Sxixji(i) can be null because c is not a parent of xi or there is not
        // predictive variable, xji, which is parent of xi. Therefore we choose the maximum length of
        // Scxixji(i), Scxi(i), Sxixji(i) as the iterations we will make
        int max = ((double [])Sxi.get(i)).length;
        if(Scxixji.get(i) != null)
        {
          int length = ((double [])Scxixji.get(i)).length;
          if(length > max)
          {
            max = length;
          }//if(length > max)
        }//if(Scxixji.get(i) != null)
        else 
        {
          if(Scxi.get(i) != null)
          {
            int length = ((double [])Scxi.get(i)).length;
            if(length > max)
            {
              max = length;
            }//if(length > max)
          }//if(Scxi.get(i) != null)
          
          if(Sxixji.get(i) != null)
          {
            int length = ((double [])Sxixji.get(i)).length;
            if(length > max)
            {
              max = length;
            }//if(length > max)
          }//if(Sxixji.get(i) != null)
        }//else
        //COPY THE VALUES
        for(j=0;j<max;j++)
        {
          if ((Scxi.get(i) != null) && (j < ((double[])Scxi.get(i)).length))
          {
            ((double [])Scxi.get(i))[j] = ((double [])s.Scxi.get(i))[j];            
          }//if ((Scxi.get(i)) != null) && (j < ((double[])Scxi.get(i)).length))
          if((Sxi.get(i) != null) && (j < ((double[])Sxi.get(i)).length))
          {
            ((double [])Sxi.get(i))[j] = ((double [])s.Sxi.get(i))[j];
          }//if((Sxi.get(i)) != null) && (j < ((double[])Sxi.get(i)).length))
          if((Sxixji.get(i) != null) && (j < ((double[])Sxixji.get(i)).length))
          {
            ((double [])Sxixji.get(i))[j] = ((double [])s.Sxixji.get(i))[j];
          }//if((Sxixji.get(i)) != null) && (j < ((double[])Sxixji.get(i)).length))
          if(Scxixji.get(i) != null)
          {
            ((double [])Scxixji.get(i))[j] = ((double [])s.Scxixji.get(i))[j];
          }//if(Scxixji.get(i)) != null)
        }// for_j
      }// for_i      
    }// copyFrom(GenerativeSufficientStatistics s)
        
    /**
     * Returns the value of nCases
     * @return nCases
     */
    public double getNCases()
    {
      return nCases;
    }//getNCases()
 
    /**
     * Returns the value of the element in statistics Sxi corresponding to the
     * values Xi = xiIndex
     * @param index position of the statistic we want to get
     * @return value of the statistic
     */
    public double getElementFromSxi(int Xi, int xiIndex)
    {
      double [] d = (double[])Sxi.get(Xi);
      return d[xiIndex];
    }//getElementFromSxi(int Xi, int xiIndex)
    
    /**
   * Returns a Matrix with the Statistics Sc. This matrix contains all the Generative
   * Sufficient Statistics except N(C=0). This one can be calculated with the others 
   * Sufficient Statistics. The resultant matrix can be used in the equation system 
   * LU X = B, where LU is the LU decompossition of the coeffient matrix, B is a matrix
   * with the discriminative sufficient statistics and X is the matrix returnes by this 
   * method
   * @return a matrix with the generative sufficient statistics 
   */
    public Matrix getMatrixScForEquationSystem()
    {
      int i;
      double [] newSc = new double [cStates - 1];
      for(i=0;i<cStates-1;i++)
      {
        newSc[i] = this.Sc[i+1];
      }//for(i=0;i<cStates-1;i++)
      Matrix matrix = new Matrix(newSc,newSc.length);
      return matrix;
    }//getMatrixScForEquationSystem()
    
    /**
   * Returns a Matrix with the Statistics Scxi. This matrix contains all the Generative
   * Sufficient Statistics except N(C=0,Xi=0), N(C=c,Xi=0) and N(C=0,Xi=xi). This one can 
   * be calculated with the others Sufficient Statistics. The resultant matrix can be 
   * used in the equation system LU X = B, where LU is the LU decompossition of the 
   * coeffient matrix, B is a matrix with the discriminative sufficient statistics 
   * and X is the matrix returnes by this method
   * @param Xi variable index to obtain its generative sufficient statistics
   * @return a matrix with the generative sufficient statistics 
   */
    public Matrix getMatrixScxiForEquationSystem(int Xi)
    {
      int i,j;
      double [] scxi    = (double []) this.Scxi.get(Xi);
      double [] newScxi = new double[(cStates - 1) * (varStates[Xi] - 1)];
      
      if(scxi == null)
      {
        return null;
      }
      else
      {      
        for(i=0;i<varStates[Xi]-1;i++)
        {
          for(j=0;j<cStates-1;j++)
          {
            newScxi[i*(cStates-1) + j] = scxi[(i+1)*cStates + (j+1)];
          }//for(j=0;j<cStates-1;j++)
        }//for(i=0;i<varStates[Xi]-1;i++)
        Matrix matrix = new Matrix(newScxi,newScxi.length);
        return matrix;
      }
    }//getMatrixScxiForEquationSystem()
    
     /**
   * Returns a Matrix with the Statistics Scxixji. This matrix contains all the Generative
   * Sufficient Statistics except N(C=0,Xi=0,Xji=0), N(C=c,Xi=0,Xji=0), ...  This one can 
   * be calculated with the others Sufficient Statistics. The resultant matrix can be 
   * used in the equation system LU X = B, where LU is the LU decompossition of the 
   * coeffient matrix, B is a matrix with the discriminative sufficient statistics 
   * and X is the matrix returnes by this method
   * @param Xi variable index to obtain its generative sufficient statistics
   * @return a matrix with the generative sufficient statistics 
   */
    public Matrix getMatrixScxixjiForEquationSystem(int Xi)
    {
      int i,j,k;
      double [] scxixji    = (double []) this.Scxixji.get(Xi);
      double [] newScxixji = new double[(cStates - 1) * (varStates[Xi] - 1) * (parStates[Xi] - 1)];
      
      if(scxixji == null)
      {
        return null;
      }
      else
      {      
        for(i=0;i<parStates[Xi]-1;i++)
        {
          for(j=0;j<varStates[Xi]-1;j++)
          {
            for(k=0;k<cStates-1;k++)
            {          
              newScxixji[i*(varStates[Xi]-1)*(cStates-1) + j*(cStates-1) + k] = scxixji[(i+1)*varStates[Xi]*cStates + (j+1)*cStates + (k+1)];
            }//for(k=0;k<cStates-1;k++)
          }//for(j=0;j<cStates-1;j++)
        }//for(i=0;i<varStates[Xi]-1;i++)
        Matrix matrix = new Matrix(newScxixji,newScxixji.length);
        return matrix;
      }
    }//getMatrixScxixjiForEquationSystem()
  
    
    /**
     * Returns the value of the  statistics Sxi
     * @param Xi index of the variable that we want to Sxi for.
     * @return Sxi
     */    
    public double [] getSxi(int Xi)
    {
      return (double[])Sxi.get(Xi);
    }//getSxi(int Xi)
    
    /**
     * Returns the value of the  statistics Sxi in a matrix
     * @param Xi index of the variable that we want to Sxi for.
     * @return Matrix with Sxi values
     */
    public Matrix getSxiMatrix(int Xi)
    {
      double[] d = (double[])Sxi.get(Xi); 
      Matrix m = new Matrix(d,d.length);
      return m;
    }//getSxi(int Xi)
    
    /**
     * Returns the value of the element in statistics Sxixji corresponding to the
     * values Xi = xiIndex and Xji=xjiIndex
     * @param index position of the statistic we want to get
     * @return value of the statistic
     */
    public double getElementFromSxixji(int Xi, int xiIndex, int xjiIndex)
    {
      double [] d = (double[])Sxixji.get(Xi);
      return d[(xjiIndex * varStates[Xi]) + xiIndex];
    }//getElementFromSxixji(int Xi, int xiIndex, int xjiIndex)
    
    /**
     * Returns the value of the  statistics Sxixji
     * @param Xi index of the variable that we want to Sxixji for.
     * @return Sxixji
     */    
    public double [] getSxixji(int Xi)
    {
      return (double[])Sxixji.get(Xi);
    }//getSxixji(int Xi)
    
    /**
     * Returns the value of the  statistics Sxixji in a matrix
     * @param Xi index of the variable that we want to Sxixji for.
     * @return Matrix with Sxixji values
     */
    public Matrix getSxixjiMatrix(int Xi)
    {
      double[] d = (double[])Sxixji.get(Xi); 
      Matrix m = new Matrix(d,d.length);
      return m;
    }//getSxixji(int Xi)
 
   /**
   * initialize the values of all the Suficient Statistics to val
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
          if(j < ((double[])Sxi.get(i)).length)
          {
            ((double [])Sxi.get(i))[j] = val;
          }// if(j < ((double[])Sxi.get(i)).length)
          if(j < ((double[])Sxixji.get(i)).length)
          {
            ((double [])Sxixji.get(i))[j] = val;
          }// if(j < ((double[])Sxixji.get(i)).length)
            
          ((double [])Scxixji.get(i))[j] = val;
        }// for_j
      }// for_i      
    }// initialize(double val)
 
    /**
   * Return true if the current values for the generative sufficient statistics are valid and false otherwise
   * @return the value for the isValid flag
   */
  public boolean isValid()
  {
    return this.isValid;
  }//isValid()
    
    /**
   * Set the values for the Generative Sufficient Statistics Sc from a matrix which
   * is calculated from the Discriminative Sufficient Statistics via an equation system. 
   * This matrix contains all the Generative Sufficient Statistics except N(C=0). This 
   * have to be calculated from the others Sufficient Statistics. 
   * The resultant matrix can be used in the equation system LU X = B, 
   * where LU is the LU decompossition of the coeffient matrix, B is a matrix
   * with the discriminative sufficient statistics and X is the matrix that is given
   * as a parameter for this method
   * @param a matrix with the generative sufficient statistics 
   */
    private void setScFromEquationSystemSolutionMatrix(Matrix m) throws VerifyError
    {
      int i;
      if(m.getRowDimension() != this.Sc.length - 1)
      {
        System.out.println("ERROR: matrix shoud be mx1 and the Sc statistics (m+1)x1");
        System.exit(-1);
      }
      /* NOTE: due to precision error, the solution of the systems of equations may give
       * negative values close to zero for some sufficient statistics. In order to solve this 
       * problem, we will consider as 0 any value between PRECISIONLIMIT and 0
       */ 
      double sum      = 0;
      double [] newSc = m.getColumnPackedCopy();      
      for(i=newSc.length-1;i>=0;i--)
      {
        if(newSc[i] < PRECISIONLIMIT)
        {//illegal value -> Sufficient statistics are not valid
          throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
        }//if(newSc[i] < PRECISIONLIMIT)
        else if((PRECISIONLIMIT < newSc[i]) && (newSc[i] < 0))
        {//Illegal value due to precision error -> set value to 0
          this.Sc[i+1] = 0;
        }//else if((PRECISIONLIMIT < newSc[i]) && (newSc[i] < 0))
        else
        {// Nijk > 0 -> legal value
          this.Sc[i+1] = newSc[i];
          sum         += newSc[i]; 
        }//else       
      }//for(i=newSc.length-1;i=0;i--)
      double value = this.nCases - sum;      
      if(value < PRECISIONLIMIT)
      {//illegal value -> Sufficient statistics are not valid
        throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
      }//if(newSc[i] < PRECISIONLIMIT)
      else if((PRECISIONLIMIT < value) && (value < 0))
      {//Illegal value due to precision error -> set value to 0
        this.Sc[0] = 0;
        if(newSc.length-1 != 0)
        {
          this.Sc[1] += ((-1) * value);
        }          
      }//else if((PRECISIONLIMIT < value) && (value < 0))
      else
      {// Nijk > 0 -> legal value
        this.Sc[0] = value;          
      }//else                     
    }//setScFromEquationSystemSolutionMatrix(Matrix m)
    
    /**
   * Set the values for the Generative Sufficient Statistics Scxi from a matrix which
   * is calculated from the Discriminative Sufficient Statistics via an equation system. 
   * This matrix contains all the Generative Sufficient Statistics except N(C=0,Xi=0), 
   * N(C=c,Xi=0) and N(C=0,Xi=xi). This have to be calculated from the others 
   * Sufficient Statistics. The resultant matrix can be used in the equation system 
   * LU X = B, where LU is the LU decompossition of the coeffient matrix, B is a matrix
   * with the discriminative sufficient statistics and X is the matrix that is given
   * as a parameter for this method
   * @param Xi index of the variable whose values are going to be set
   * @param a matrix with the generative sufficient statistics 
   */
    private void setScxiFromEquationSystemSolutionMatrix(int Xi, Matrix m)
    {
      int i,j;
      double [] newScxi = m.getColumnPackedCopy();
      double [] Scxi    = ((double[])this.Scxi.get(Xi));
      int dimension = Scxi.length - cStates - (varStates[Xi] - 1);
      if(newScxi.length != dimension)
      {
        System.out.println("ERROR: matrix and Scxi dimension are not agree in setScxiFromEquationSystemSolutionMatrix");
        System.exit(-1);
      }
      /* NOTE: due to precision error, the solution of the systems of equations may give
       * negative values close to zero for some sufficient statistics. In order to solve this 
       * problem, we will consider as 0 any value between PRECISIONLIMIT and 0
       */ 
      
      //sumcXi[c] = SUM_(xi=1..varStates[Xi]-1) N(C=c,Xi=i)
      double [] sumCxi = new double[cStates];
      for(i=varStates[Xi]-2;i>=0;i--)
      {        
        double     sum = 0;
        for(j=cStates-2;j>=0;j--)
        {          
          double currentValue       = newScxi[i*(cStates-1) + j];
          if(currentValue < PRECISIONLIMIT)
          {//illegal value -> Sufficient statistics are not valid
            throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
          }//if(newSc[i] < PRECISIONLIMIT)
          else if((PRECISIONLIMIT < currentValue) && (currentValue < 0))
          {//Illegal value due to precision error -> set value to 0
            Scxi[(i+1)*cStates + j+1] = 0;
          }//else if((PRECISIONLIMIT < newSc[i]) && (newSc[i] < 0))
          else
          {// Nijk > 0 -> legal value
            Scxi[(i+1)*cStates + j+1] = currentValue;
            sum                      += currentValue;
            sumCxi[j+1]              += currentValue;
          }//else                 
        }//for(j=cStates-2;j>0;j--)
        
        // when C=0 => N(C=0,Xi=i) =  N(Xi=i) - sum, 
        // where sum = N(C=1,Xi=i),...,N(C=cStates-1,Xi=i).
        double value        = this.getElementFromSxi(Xi,i+1) - sum;
        if(value < PRECISIONLIMIT)
        {//illegal value -> Sufficient statistics are not valid
          throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
        }//if(newSc[i] < PRECISIONLIMIT)
        else if((PRECISIONLIMIT < value) && (value < 0))
        {//Illegal value due to precision error -> set value to 0
          Scxi[(i+1)*cStates] = 0;
          /*if(cStates-2 != 0)
          {
           Scxi[(i+1)*cStates +1] += ((-1) * value);
          }*/          
        }//else if((PRECISIONLIMIT < value) && (value < 0))
        else
        {// Nijk > 0 -> legal value
          Scxi[(i+1)*cStates] = value;
          sumCxi[0]          += value;   
        }//else        
      }//for(i=varStates[Xi]-2;i>0;i--)
      
      // when Xi=0, we compute the values N(C=c,Xi=0)
      double sum = 0;
      for(j=cStates-2;j>=0;j--)
      {        
        // N(C=c,Xi=0) = N(C=c) - sumCXi[c],
        // where sumCXi[c] = SUM_(i=1..varStates[Xi]-1) N(C=c,Xi=i)
         double currentValue = this.getElementFromSc(j+1) - sumCxi[j+1];
         if(currentValue < PRECISIONLIMIT)
          {//illegal value -> Sufficient statistics are not valid
            throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
          }//if(newSc[i] < PRECISIONLIMIT)
          else if((PRECISIONLIMIT < currentValue) && (currentValue < 0))
          {//Illegal value due to precision error -> set value to 0
            Scxi[j+1] = 0;
          }//else if((PRECISIONLIMIT < newSc[i]) && (newSc[i] < 0))
          else
          {// Nijk > 0 -> legal value
            Scxi[j+1] = currentValue;
            sum      += currentValue;            
          }//else                         
      }//for(j=cStates-2;j>0;j--)
      //when C=0 and Xi=0, N(C=0,Xi=0)
      double value  = this.getElementFromSxi(Xi,0) - sum;      
      if(value < PRECISIONLIMIT)
      {//illegal value -> Sufficient statistics are not valid
        throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
      }//if(newSc[i] < PRECISIONLIMIT)
      else if((PRECISIONLIMIT < value) && (value < 0))
      {//Illegal value due to precision error -> set value to 0
        Scxi[0] = 0;
        /*if(cStates-2 != 0)
        {
          Scxi[1] += ((-1) * value);
        }*/          
      }//else if((PRECISIONLIMIT < value) && (value < 0))
      else
      {// Nijk > 0 -> legal value
        Scxi[0] = value;        
      }//else              
    }//setScxiFromEquationSystemSolutionMatrix(int Xi, Matrix m)
    
    /**
   * Set the values for the Generative Sufficient Statistics Scxixji from a matrix which
   * is calculated from the Discriminative Sufficient Statistics via an equation system. 
   * This matrix contains all the Generative Sufficient Statistics except N(C=0,Xi=0,Xji=0), 
   * N(C=c,Xi=0,Xji=0), ..., N(C=0,Xi=0,Xji=xji). This have to be calculated from the others 
   * Sufficient Statistics. The resultant matrix can be used in the equation system 
   * LU X = B, where LU is the LU decompossition of the coeffient matrix, B is a matrix
   * with the discriminative sufficient statistics and X is the matrix that is given
   * as a parameter for this method
   * @param Xi index of the variable whose values are going to be set
   * @param a matrix with the generative sufficient statistics 
   */
    private void setScxixjiFromEquationSystemSolutionMatrix(int Xi, Matrix m)
    {
      int i,j,k;
      
      /* NOTE: due to precision error, the solution of the systems of equations may give
       * negative values close to zero for some sufficient statistics. In order to solve this 
       * problem, we will consider as 0 any value between PRECISIONLIMIT and 0
       */ 
      
      FiniteStates node   = (FiniteStates)nodeList.elementAt(Xi);
      //the first parent is the predictive variable Xji, and the second is Class
      FiniteStates parent = (FiniteStates)node.getParents().elementAt(0).getTail();
      //position of the parent of Xi (Xji) in the nodeList
      int parXi           = nodeList.getId(parent);
      
      double [] newScxi = m.getColumnPackedCopy();
      double [] Scxixji   = ((double[])this.Scxixji.get(Xi));
      //sumcxiXji[c*][xi*] = SUM_(xji=1..parStates[Xi]-1) N(C=c*,Xi=xi*,Xji=xji)
      double [][] sumcxiXji = new double[cStates][varStates[Xi]];
      //sumcXiXji[c*] = SUM(xi=1..varStates[Xi]-1;xji=1..parStates[Xi]-1) N(C=c*,Xi=xi,Xji=xji)
      double []   sumcXiXji = new double[cStates];
      
      for(i=parStates[Xi]-2;i>=0;i--)
      {        
        //sumcXixji[c*] = SUM_(xi=1..varStates[Xi]-1) N(C=*,Xi=xi,Xji=i+1)
        double[] sumcXixji = new double[cStates];        
        for(j=varStates[Xi]-2;j>=0;j--)
        {
          // sum = SUM_(c=1..cStates-1) N(C=c,Xi=j+1,Xji=i+1)
          double sum = 0;
          for(k=cStates-2;k>=0;k--)
          {
            double currentValue       = newScxi[i*(varStates[Xi]-1)*(cStates-1) + j*(cStates-1) + k];
            if(currentValue < PRECISIONLIMIT)
            {//illegal value -> Sufficient statistics are not valid
              throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
            }//if(newSc[i] < PRECISIONLIMIT)
            else if((PRECISIONLIMIT < currentValue) && (currentValue < 0))
            {//Illegal value due to precision error -> set value to 0
              Scxixji[(i+1)*cStates*varStates[Xi] + (j+1)*cStates + (k+1)] = 0;
            }//else if((PRECISIONLIMIT < newSc[i]) && (newSc[i] < 0))
            else
            {// Nijk > 0 -> legal value
              Scxixji[(i+1)*cStates*varStates[Xi] + (j+1)*cStates + (k+1)] = currentValue;
              sum                                                         += currentValue;
              sumcXixji[k+1]                                              += currentValue;
              sumcxiXji[k+1][j+1]                                         += currentValue;
              sumcXiXji[k+1]                                              += currentValue;
            }//else                          
          }//for(k=cStates-2;k>=0;k--)
          // when C=0 => N(C=0,Xi=j,Xji=i) =  N(Xi=j,Xji=i) - sum, 
          // where sum = N(C=1,Xi=j,Xji=i),...,N(C=cStates-1,Xi=j,Xji=i).
          double value        = this.getElementFromSxixji(Xi,j+1,i+1) - sum;
          if(value < PRECISIONLIMIT)
          {//illegal value -> Sufficient statistics are not valid
            throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
          }//if(newSc[i] < PRECISIONLIMIT)
          else if((PRECISIONLIMIT < value) && (value < 0))
          {//Illegal value due to precision error -> set value to 0
            Scxixji[(i+1)*cStates*varStates[Xi] + (j+1)*cStates] = 0;
            /*if(cStates-2 != 0)
            {
              double positiveVal = ((-1) * value);
              //acumulate the value in the previous statistic: Ncxixji(c+1,xi,xji)
              Scxixji[((i+1)*cStates*varStates[Xi] + (j+1)*cStates) + 1] += positiveVal;
              sumcxiXji[1][j+1] += positiveVal;
              sumcXixji[1]      += positiveVal;
              sumcXiXji[1]      += positiveVal;
            }*/          
          }//else if((PRECISIONLIMIT < value) && (value < 0))
          else
          {// Nijk > 0 -> legal value
            Scxixji[(i+1)*cStates*varStates[Xi] + (j+1)*cStates] = value;
            sumcxiXji[0][j+1] += value;
            sumcXixji[0]      += value;
            sumcXiXji[0]      += value;
          }//else          
        }//for(j=varStates[Xi]-2;j>=0;j--)
        //When Xi=0
        for(k=cStates-2;k>=0;k--)
        {
          double currentValue = this.getElementFromScxi(parXi,k+1,i+1) - sumcXixji[k+1]; 
          if(currentValue < PRECISIONLIMIT)
          {//illegal value -> Sufficient statistics are not valid
            throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
          }//if(newSc[i] < PRECISIONLIMIT)
          else if((PRECISIONLIMIT < currentValue) && (currentValue < 0))
          {//Illegal value due to precision error -> set value to 0
            Scxixji[(i+1)*cStates*varStates[Xi] + (k+1)] = 0;
          }//else if((PRECISIONLIMIT < newSc[i]) && (newSc[i] < 0))
          else
          {// Nijk > 0 -> legal value
            Scxixji[(i+1)*cStates*varStates[Xi] + (k+1)] = currentValue;
            sumcxiXji[k+1][0]                           += currentValue;
            sumcXiXji[k+1]                              += currentValue;
          }//else          
        }//for(k=cStates-2;k>=0;k--)
        //when C=0 and Xi=0
        double value = this.getElementFromScxi(parXi,0,i+1) - sumcXixji[0];
        if(value < PRECISIONLIMIT)
        {//illegal value -> Sufficient statistics are not valid
          throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
        }//if(newSc[i] < PRECISIONLIMIT)
        else if((PRECISIONLIMIT < value) && (value < 0))
        {//Illegal value due to precision error -> set value to 0
          Scxixji[(i+1)*cStates*varStates[Xi]] = 0;
          /*if(cStates-2 != 0)
          {
            double positiveVal = ((-1) * value);
            //acumulate the value in the previous statistic: Ncxixji(c+1,xi,xji)
            Scxixji[((i+1)*cStates*varStates[Xi]) + 1] = positiveVal;
            sumcxiXji[1][0]                           += positiveVal;          
            sumcXiXji[1]                              += positiveVal;
          }*/          
        }//else if((PRECISIONLIMIT < value) && (value < 0))
        else
        {// Nijk > 0 -> legal value
          Scxixji[(i+1)*cStates*varStates[Xi]] = value;
          sumcxiXji[0][0]                     += value;          
          sumcXiXji[0]                        += value;
        }//else                  
      }//for(i=parStates[Xi]-2;i>=0;i--)
      
      // when Xji=0, we compute the values N(C=c,Xi=xi,Xji=0)      
      for(j=varStates[Xi]-2;j>=0;j--)
      {
        double sum = 0;
        for(k=cStates-2;k>=0;k--)
        {
          double currentValue = this.getElementFromScxi(Xi,k+1,j+1) - sumcxiXji[k+1][j+1];
          if(currentValue < PRECISIONLIMIT)
          {//illegal value -> Sufficient statistics are not valid
            throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
          }//if(newSc[i] < PRECISIONLIMIT)
          else if((PRECISIONLIMIT < currentValue) && (currentValue < 0))
          {//Illegal value due to precision error -> set value to 0
            Scxixji[(j+1)*cStates + (k+1)] = 0;
          }//else if((PRECISIONLIMIT < newSc[i]) && (newSc[i] < 0))
          else
          {// Nijk > 0 -> legal value
            Scxixji[(j+1)*cStates + (k+1)] = currentValue;
            sum                           += currentValue;          
            sumcXiXji[k+1]                += currentValue;
          }//else           
        }//for(k=cStates-2;k>=0;k--)
        // when C=0,Xji=0 => N(C=0,Xi=j+1,Xji=0) =  N(Xi=j+1,Xji=0) - sum, 
        // where sum = N(C=1,Xi=j+1,Xji=0),...,N(C=cStates-1,Xi=j+1,Xji=0).
        double value        = this.getElementFromSxixji(Xi,j+1,0) - sum;
        if(value < PRECISIONLIMIT)
        {//illegal value -> Sufficient statistics are not valid
          throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
        }//if(newSc[i] < PRECISIONLIMIT)
        else if((PRECISIONLIMIT < value) && (value < 0))
        {//Illegal value due to precision error -> set value to 0
          Scxixji[(j+1)*cStates] = 0;
          /*if(cStates-2 != 0)
          {
            double positiveVal = ((-1) * value);
            //acumulate the value in the previous statistic: Ncxixji(c+1,xi,xji)
            Scxixji[((j+1)*cStates) + 1] = positiveVal;
            sumcxiXji[1][j+1]           += value;          
            sumcXiXji[1]                += value; 
          }*/          
        }//else if((PRECISIONLIMIT < value) && (value < 0))
        else
        {// Nijk > 0 -> legal value
          Scxixji[(j+1)*cStates] = value;
          sumcxiXji[0][j+1]     += value;          
          sumcXiXji[0]          += value; 
        }//else              
      }//for(j=varStates[Xi]-2;j>=0;j--)
      //when Xi=0, Xji=0  
      for(k=cStates-2;k>=0;k--)
      {
        double currentValue = this.getElementFromSc(k+1) - sumcXiXji[k+1]; 
        if(currentValue < PRECISIONLIMIT)
        {//illegal value -> Sufficient statistics are not valid
          throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
        }//if(newSc[i] < PRECISIONLIMIT)
        else if((PRECISIONLIMIT < currentValue) && (currentValue < 0))
        {//Illegal value due to precision error -> set value to 0
          Scxixji[(k+1)] = 0;
        }//else if((PRECISIONLIMIT < newSc[i]) && (newSc[i] < 0))
        else
        {// Nijk > 0 -> legal value
          Scxixji[(k+1)]      = currentValue;    
          sumcXiXji[k+1]      = currentValue;
        }//else         
      }//for(k=cStates-2;k>=0;k--)
      //when C=0, Xi=0, Xji=0
      double value = this.getElementFromSc(0) - sumcXiXji[0];
      if(value < PRECISIONLIMIT)
      {//illegal value -> Sufficient statistics are not valid
        throw new VerifyError("Illegal value found in the set of generative sufficient statistis");
      }//if(newSc[i] < PRECISIONLIMIT)
      else if((PRECISIONLIMIT < value) && (value < 0))
      {//Illegal value due to precision error -> set value to 0
        Scxixji[0] = 0;
        /*if(cStates-2 != 0)
        {
          double positiveVal = ((-1) * value);
          //acumulate the value in the previous statistic: Ncxixji(c+1,xi,xji)
          Scxixji[1] = positiveVal;
        }*/          
      }//else if((PRECISIONLIMIT < value) && (value < 0))
      else
      {// Nijk > 0 -> legal value
        Scxixji[0] = value;        
      }//else                     
    }//setScxixjiFromEquationSystemSolutionMatrix(int Xi, Matrix m)
    
    /**
     * Sets the value of the  statistics Sxi
     * @param Xi index of the variable that we want to Sxi for.
     * @param val The new value for the parameters 
     */
    public void setSxi(int Xi, double [] val)
    {
      if(val.length != cStates)
      {
        System.out.println("ERROR in setSxi from DiscreteClassifierDiscriminativeLearning: " + 
         "the length of val must be equal to the number of states for the class variable");
         System.exit(-1);
      }
      Sxi.set(Xi,val);
    }//end setSxi(Xi,val)

    /**
     * Sets the value of the  statistics Sxi
     * @param Xi index of the variable that we want to Sxi for.
     * @param val The new value for the parameters in a matrix objet 
     */
    public void setSxi(int Xi,Matrix val)
    {    
      if(val.getRowDimension() != cStates || val.getColumnDimension() != 1)
      {
         System.out.println("ERROR in setSxi(Matrix val) from DiscreteClassifierDiscriminativeLearning: " + 
          "val must be rc X 1 where rc is the number of states for the class variable");
         System.exit(-1);
      }
      double [] d = val.getColumnPackedCopy();
      setSxi(Xi,d);
    }//end setSxi(Xi,val)
    
    /**
     * Sets the value of the  statistics  Sxixji
     * @param Xi index of the variable that we want to Sxixji for.
     * @param val The new value for the parameters 
     */
    public void setSxixji(int Xi, double [] val)
    {
      if(val.length != cStates)
      {
        System.out.println("ERROR in setSxixji from DiscreteClassifierDiscriminativeLearning: " + 
         "the length of val must be equal to the number of states for the class variable");
         System.exit(-1);
      }
      Sxixji.set(Xi,val);
    }//end setScxi(Xi,val)

    /**
     * Sets the value of the statistics Sxixji
     * @param Xi index of the variable that we want to Sxixji for.
     * @param val The new value for the parameters in a matrix objet 
     */
    public void setSxixji(int Xi,Matrix val)
    {    
      if(val.getRowDimension() != cStates || val.getColumnDimension() != 1)
      {
         System.out.println("ERROR in setSxixji(Matrix val) from DiscreteClassifierDiscriminativeLearning: " + 
          "val must be rc X 1 where rc is the number of states for the class variable");
         System.exit(-1);
      }
      double [] d = val.getColumnPackedCopy();
      setSxixji(Xi,d);
    }//end setSxixji(Xi,val)
    
    /**
   * Calculate and set the value of the new generative sufficient statistics. These new values 
   * are obtained from the discriminative sufficient statistics and the coefficients via
   * linear systems of equations and they replace the old ones. 
   * Only the values of Sc, Scxi and Scxixji chage.
   * @param coeffs
   * @param M
   */
    public void updateStatistics(Coefficients coeffs, DiscriminativeSufficientStatistics M)
    {
      int i;
      Iterator nodeIt;
      /*
       * The current set of generative sufficient statistics is valid 'a priori'
       * if we find any sufficient statistics < 0, we will set the flag is valid to false
       * and stop the updating
       * NOTE: due to precision error, the solution of the systems of equations may give
       * negative values close to zero for some sufficient statistics. In order to solve this 
       * problem, we will consider as 0 any value between PRECISIONLIMIT and 0
       */ 
      this.isValid = true;
      
      //update the generative suficient statistics related to C (Sc)
      Matrix ScSolution = coeffs.solveEquationSystemSc(M.getScMatrix());
      try
      {
        this.setScFromEquationSystemSolutionMatrix(ScSolution);  
      }
      catch(VerifyError e)
      {
        this.isValid = false;
        return;
      }
      
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
            Matrix ScxiSolution = coeffs.solveEquationSystemScxi(i,M.getScxiMatrix(i));
            try
            {
              this.setScxiFromEquationSystemSolutionMatrix(i,ScxiSolution);  
            }
            catch(VerifyError e)
            {
              this.isValid = false;
              return;
            }
          }//if(parent.getComment().compareToIgnoreCase("ClassNode") == 0)
        }//if(links.size() == 1)
        else if(links.size() == 2)
        {//The current variable has two parents: a predictive variable (Xji) and C
         // => we have to update both Scxi and Scxixji
          //Scxi
          Matrix ScxiSolution = coeffs.solveEquationSystemScxi(i,M.getScxiMatrix(i));
          try
            {
              this.setScxiFromEquationSystemSolutionMatrix(i,ScxiSolution);
            }
            catch(VerifyError e)
            {
              this.isValid = false;
              return;
            }          
          //Scxixji
          Matrix ScxixjiSolution = coeffs.solveEquationSystemScxixji(i,M.getScxixjiMatrix(i));
          try
            {
              this.setScxixjiFromEquationSystemSolutionMatrix(i,ScxixjiSolution);
            }
            catch(VerifyError e)
            {
              this.isValid = false;
              return;
            }                 
        }//else if(links.size() == 2)        
      }//for(i=0;i<this.nVar;i++)
    }//updateStatistics(Coefficients coeffs, DiscriminativeSufficientStatistics M)
}//GenerativeSufficientStatistics