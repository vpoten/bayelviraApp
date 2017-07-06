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


public abstract class SufficientStatistics
  {   
    double [] Sc;     //Sufficient Statistics related only to C
    ArrayList Scxi;   //Sufficient Statistics related to C and Xi
    ArrayList Scxixji;//Sufficient Statistics related to C, Xi and Xj(i) (parent of Xi)

    int    nVar;      // no. of variables (Class is not included)
    int [] varStates; // no. of states for the variable Xi
    int [] parStates; // no. of states for the parent of Xi (only if it is different from C
    int    cStates;   // no. of states for the class variable
    
    NodeList nodeList;      
    /**
   * Basic constructor for the class
   * @param nl NodeList which codify the dependencies between variables
   
    public abstract SufficientStatistics(NodeList nl);    
   */
   
     /**
     * Adds the value in val to the values in Sc. Sc and val must have the same
     * size. Sc = Sc + val
     * @param val values to add to Sc
     */
    public void addToSc(double[] val)
    {
      int i;
      if(Sc.length != val.length)
      {
        System.out.println("ERROR in addToSc from SufficientStatistics Sc and val must have the same size");
        System.exit(-1);
      }//end if
      for(i=0;i<Sc.length;i++)
      {
        Sc[i] += val[i];
      }//end for_i    
    }//end addToSc(val)
    
    /**
     * Adds the value in val to the corresponding value in Sc.
     * @param val values to add to Sc
     */
    public void addToSc(int cIndex, double val)
    {      
      if(cIndex > Sc.length)
      {
        System.out.println("ERROR in addToSc from SufficientStatistics cIndex must be < Sc.length");
        System.exit(-1);
      }//end if
      Sc[cIndex] += val;
    }//end addToSc(val)
    
    /**
     * Adds the value in val to the values in Sc. Sc and val must have the same
     * size. Sc = Sc + val
     * @param val values to add to Sc
     */
    public void addToSc(Matrix val)
    {
      addToSc(val.getColumnPackedCopy());      
    }//addToSc(Matrix val)
    
    /**
     * Adds the value in val to the values in Scxi. Scxi and val must have the same
     * size. Scxi = Scxi + val
     * @param val values to add to Scxi
     */
    public void addToScxi(int Xi, double[] val)
    {
      int i;
      if(((double[])Scxi.get(Xi)).length != val.length)
      {
        System.out.println("ERROR in addToScxi from SufficientStatistics Scxi and val must have the same size");
        System.exit(-1);
      }//if
      for(i=0;i<val.length;i++)
      {
        ((double[])Scxi.get(Xi))[i] += val[i];
      }//for_i
    }//addToScxi(int Xi, double[] val)
    
    /**
     * Adds the value in val to the corresponding value in Scxi.
     * @param val values to add to Scxi
     */
    public void addToScxi(int Xi, int cIndex, int xiIndex, double val)
    {      
      double[] d = (double[])Scxi.get(Xi);
      int index = (xiIndex * cStates) + cIndex;
      if(index > d.length)
      {
        System.out.println("ERROR in addToScxi from SufficientStatistics index must be < Scxi[i].length");
        System.exit(-1);
      }//end if      
      d[index] += val;
    }//end addToScxi(int Xi, int cIndex, int xiIndex, double val)
    
    /**
     * Adds the value in val to the values in Scxi. Scxi and val must have the same
     * size. Scxi = Scxi + val
     * @param val values to add to Scxi
     */
    public void addToScxi(int Xi,Matrix val)
    {
      addToScxi(Xi,val.getColumnPackedCopy());      
    }//addToScxi(int Xi,Matrix val)
    
    /**
     * Adds the value in val to the values in Scxixji. Scxixji and val must have the same
     * size. Scxixji = Scxixji + val
     * @param val values to add to Scxixji
     */
    public void addToScxixji(int Xi, double[] val)
    {
      int i;
      if(((double[])Scxixji.get(Xi)).length != val.length)
      {
        System.out.println("ERROR in addToScxixji from SufficientStatistics Scxixji and val must have the same size");
        System.exit(-1);
      }//end if
      for(i=0;i<val.length;i++)
      {
        ((double[])Scxixji.get(Xi))[i] += val[i];
      }//for_i
    }//addToScxixji(int Xi, double[] val)
    
     /**
     * Adds the value in val to the corresponding value in Scxixji.
     * @param val values to add to Scxixji
     */
    public void addToScxixji(int Xi, int cIndex, int xiIndex, int xjiIndex, double val)
    {      
      double[] d = (double[])Scxixji.get(Xi);
      int index = (xjiIndex * varStates[Xi] * cStates) + (xiIndex * cStates) + cIndex;
      if(index > d.length)
      {
        System.out.println("ERROR in addToScxixji from SufficientStatistics index must be < Scxixji[i].length");
        System.exit(-1);
      }//end if      
      d[index] += val;
    }//end addToScxixji(int Xi, int cIndex, int xiIndex, int xjiIndex, double val)
    
    /**
     * Adds the value in val to the values in Scxixji. Scxixji and val must have the same
     * size. Scxixji = Scxixji + val
     * @param val values to add to Scxixji
     */
    public void addToScxixji(int Xi,Matrix val)
    {
      addToScxixji(Xi,val.getColumnPackedCopy());      
    }//addToScxixji(int Xi,Matrix val)
    
    /**
   * copy the values of the Suficient Statistics in s into the current object
   * @param s Sufficient Statistic object
   */
  //public abstract void copyFrom(SufficientStatistics s);
    
    /**
     * Returns the value of the element in statistics Sc which is in the position
     * 'index'
     * @param index position of the statistic we want to get
     * @return value of the statistic
     */
    public double getElementFromSc(int index)
    {
      return Sc[index];
    }//getElementFromSc(int index)
       
    /**
     * Returns the value of the  statistics Sc
     * @return Sc
     */
    public double [] getSc()
    {
      return Sc;
    }//getSc(int Xi)
    
    /**
     * Returns the value of the  statistics Sc in a matrix
     * @return Matrix with Sc values
     */
    public Matrix getScMatrix()
    {
      Matrix m = new Matrix(Sc,Sc.length);
      return m;
    }//getSc(int Xi)
    
    /**
     * Returns the value of the element in statistics Scxi which is in the position
     * 'index'
     * @param index position of the statistic we want to get
     * @return value of the statistic
     */
    public double getElementFromScxi(int Xi, int cIndex, int xiIndex)
    {
      double [] d = (double[])Scxi.get(Xi);
      return d[(xiIndex * cStates) + cIndex];
    }//getElementFromScxi(int Xi, int cIndex, int xiIndex)
    
    /**
     * Returns the value of the  statistics Scxi
     * @param Xi index of the variable that we want to Scxi for.
     * @return Scxi
     */    
    public double [] getScxi(int Xi)
    {
      return (double[])Scxi.get(Xi);
    }//getScxi(int Xi)
    
    /**
     * Returns the value of the  statistics Sc in a matrix
     * @param Xi index of the variable that we want to Scxi for.
     * @return Matrix with Scxi values
     */
    public Matrix getScxiMatrix(int Xi)
    {
      double[] d = (double[])Scxi.get(Xi); 
      Matrix m = new Matrix(d,d.length);
      return m;
    }//getScxi(int Xi)
    
    /**
     * Returns the value of the element in statistics Scxixji corresponding to the
     * values C=cIndex, Xi = xiIndex and Xji=xjiIndex
     * @param index position of the statistic we want to get
     * @return value of the statistic
     */
    public double getElementFromScxixji(int Xi, int cIndex, int xiIndex, int xjiIndex)
    {
      double [] d = (double[])Scxixji.get(Xi);
      return d[(xjiIndex * varStates[Xi] * cStates) + (xiIndex * cStates) + cIndex];      
    }//getElementFromScxi(int Xi, int cIndex, int xiIndex, int xjiIndex)
    
    /**
     * Returns the value of the  statistics Scxixji
     * @param Xi index of the variable that we want to Scxixji for.
     * @return Scxixji
     */    
    public double [] getScxixji(int Xi)
    {
      return (double[])Scxixji.get(Xi);
    }//getScxixji(int Xi)
    
    /**
     * Returns the value of the  statistics Scxixji in a matrix
     * @param Xi index of the variable that we want to Scxixji for.
     * @return Matrix with Scxixji values
     */
    public Matrix getScxixjiMatrix(int Xi)
    {
      double[] d = (double[])Scxixji.get(Xi); 
      Matrix m = new Matrix(d,d.length);
      return m;
    }//getScxixji()

     /**
   * initialize the values of all the Suficient Statistics to val
   * @param val value to initialize the current SufficientStatistic
   */
    public abstract void initialize(double val);
   
    /**
     * Sets the value of the  statistics Sc
     * @param val The new value for the parameters 
     */
    public void setSc(double [] val)
    {
      if(val.length != Sc.length)
      {
        System.out.println("ERROR in setSc from DiscreteClassifierDiscriminativeLearning: " + 
         "the length of val must be equal to the number of states for the class variable");
         System.exit(-1);
      }
      Sc = val;
    }//end setSc(val)

    /**
     * Sets the value of the  statistics Sc
     * @param val The new value for the parameters in a matrix objet 
     */
    public void setSc(Matrix val)
    {    
      if(val.getRowDimension() != Sc.length || val.getColumnDimension() != 1)
      {
         System.out.println("ERROR in setSc(Matrix val) from DiscreteClassifierDiscriminativeLearning: " + 
          "val must be rc X 1 where rc is the number of states for the class variable");
         System.exit(-1);
      }
      double [] d = val.getColumnPackedCopy();
      setSc(d);
    }//end setSc(val)
    
     /**
     * Sets the value of the  statistics Scxi
     * @param Xi index of the variable that we want to Scxi for.
     * @param val The new value for the parameters 
     */
    public void setScxi(int Xi, double [] val)
    {
      /*if(val.length != ((double [])Scxi.get(Xi)).length)
      {
        System.out.println("ERROR in setScxi from DiscreteClassifierDiscriminativeLearning: " + 
         "the length of val must be equal to the number of states for the class variable");
         System.exit(-1);
      }*/
      Scxi.set(Xi,val);
    }//end setScxi(Xi,val)

    /**
     * Sets the value of the  statistics Scxi
     * @param Xi index of the variable that we want to Scxi for.
     * @param val The new value for the parameters in a matrix objet 
     */
    public void setScxi(int Xi,Matrix val)
    {    
      /*if(val.getRowDimension() != cStates || val.getColumnDimension() != 1)
      {
         System.out.println("ERROR in setScxi(Matrix val) from DiscreteClassifierDiscriminativeLearning: " + 
          "val must be rc X 1 where rc is the number of states for the class variable");
         System.exit(-1);
      }*/
      double [] d = val.getColumnPackedCopy();
      setScxi(Xi,d);
    }//end setScxi(Xi,val)
     
     /**
     * Sets the value of the  statistics Scxixji
     * @param Xi index of the variable that we want to Scxixji for.
     * @param val The new value for the parameters 
     */
    public void setScxixji(int Xi, double [] val)
    {
      /*if(val.length != cStates)
      {
        System.out.println("ERROR in setScxixji from DiscreteClassifierDiscriminativeLearning: " + 
         "the length of val must be equal to the number of states for the class variable");
         System.exit(-1);
      }*/
      Scxixji.set(Xi,val);
    }//end setScxixji(Xi,val)

    /**
     * Sets the value of the  statistics Scxixji
     * @param Xi index of the variable that we want to Scxixji for.
     * @param val The new value for the parameters in a matrix objet 
     */
    public void setScxixji(int Xi,Matrix val)
    {    
      /*if(val.getRowDimension() != cStates || val.getColumnDimension() != 1)
      {
         System.out.println("ERROR in setScxixji(Matrix val) from DiscreteClassifierDiscriminativeLearning: " + 
          "val must be rc X 1 where rc is the number of states for the class variable");
         System.exit(-1);
      }*/
      double [] d = val.getColumnPackedCopy();
      setScxixji(Xi,d);
    }//end setScxixji(Xi,val)   

  }//end class SufficientStatistics