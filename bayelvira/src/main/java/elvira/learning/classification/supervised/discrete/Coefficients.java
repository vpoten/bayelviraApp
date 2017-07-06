package elvira.learning.classification.supervised.discrete;
import elvira.Link;
import elvira.LinkList;
import elvira.tools.Jama.LUDecomposition;
import elvira.FiniteStates;
import elvira.NodeList;
import elvira.tools.Jama.Matrix;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

public class Coefficients 
{
  
  Matrix          Coeffc;
  ArrayList       Coeffcxi;
  ArrayList       Coeffcxixji;
  //ArrayList       Coefxi;
  //ArrayList       Coefxixji;

  LUDecomposition LUc;
  ArrayList       LUcxi;
  ArrayList       LUcxixji;
  //ArrayList       LUxi;
  //ArrayList       LUxixji;
  
  private NodeList nodeList;
  /**
   * The maximun error allowed when solving the equation systems.
   */
  private double MAX_ERROR = 0.1;
  
  int             nVar;
  int[]           varStates;
  int[]           parStates;
  int             cStates;
  
  // path, sets the directory where the files which contain the coefficient for
  // the equation systems are stored.
  String path;
  
  public Coefficients(NodeList nl)
  {
      int i;
      Iterator nodesIt;
      String fileNameLU;
      String fileNameCoeff;
      File   fLU;
      File   fCoeff;
         
      //set the path into the current working directory      
      //path = "/home/guzman/BDdicotomicas/coeffs";
      path = "." + File.separator +"coeffs";
      fLU    = new File(path);
      if(!fLU.exists())
      {
        fLU.mkdir();
      }//if(!fLU.exists())
      
      nodeList  = nl;
      nVar      = nl.size() - 1; //class is not included
      varStates = new int[nVar];
      parStates = new int[nVar];
      
      
      FiniteStates classNode = (FiniteStates)nl.elementAt(nVar);
      cStates                = classNode.getNumStates();
      
      /** coefficients for the statistics Sc { */
      fileNameLU    = (path + File.separator + "LU_varPar0_varStates_" + cStates + "_parCStates0_parXjiStates0.coef");
      fileNameCoeff = (path + File.separator + "Coeff_varPar0_varStates_" + cStates + "_parCStates0_parXjiStates0.coef");
      fLU        = new File(fileNameLU);
      fCoeff     = new File(fileNameCoeff);
      if(fLU.exists() && fCoeff.exists())
      {        
        try
        {
          LUc    = LUDecomposition.read(new FileReader(fLU));  
          Coeffc = Matrix.read(new FileReader(fCoeff));  
        }
        catch(IOException e)
        {
          System.out.println("ERROR: Wrong file format for the LUDecomposition stored object");
          System.exit(-1);
        }        
      }//if(f.exists())
      else
      {// we create the Coeffc object        
        ArrayList aux = this.generateCoefSc(cStates);
        Coeffc        = (Matrix) aux.get(0);
        LUc           = (LUDecomposition) aux.get(1);
        //save the calculations for a future use
        try
        {
          Coeffc.print(fCoeff);
          LUc.saveToFile(fLU);          
        }
        catch(Exception e)//FileNotFoundException or IOException
        {
          System.out.println("ERROR: can not create the file to save the LU object");
          System.exit(-1);
        }
        
        
      }//else -> !(f.exists())
      /** } coefficients for the statistics Sc */
      
      Coeffcxi    = new ArrayList();
      Coeffcxixji = new ArrayList();
      LUcxi       = new ArrayList();
      LUcxixji    = new ArrayList();
      //Coeffxi                     = new ArrayList();
      //Coeffxixji                     = new ArrayList();
      
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
          if(parent.getComment().compareToIgnoreCase("ClassNode") != 0)
          {           
            parStates[i] = parent.getNumStates(); 
          }//end if          
        }//end while       
        if(parentsL.size() == 0)
        {// Xi has no parents
          Coeffcxi.add(null);
          LUcxi.add(null);
          Coeffcxixji.add(null);
          LUcxixji.add(null);
          //Coeffxixji.add(null);          
          
          /** coefficients for the statistics Sxi { **
          fileName = (path + File.separator + "LU_varPar0_varStates_" + varStates[i] + "_parCStates0_parXjiStates0.coef");
          f        = new File(fileName);
          if(f.exists())
          {        
            try
            {            
              Coefxi.add(LUDecomposition.read(new FileReader(f)));              
            }
            catch(IOException e)
            {
              System.out.println("ERROR: Wrong file format for the LUDecomposition stored object");
              System.exit(-1);
            }        
          }//if(f.exists())
          else
          {// we create the Coeffxi object
            LUDecomposition lu = this.generateCoefSxi(varStates[i]);
            Coefxi.add(lu);
            //save the calculations for a future use
            try
            {
              lu.saveToFile(f);
            }
            catch(Exception e)//FileNotFoundException or IOException
            {
              System.out.println("ERROR: can not create the file to save the LU object");
            }
          }//else -> !(f.exists())          
          /** } coefficients for the statistics Sxi */
        }//(parentsL.size() == 0)
        else if(parentsL.size() == 1)
        {// Xi has only one parent
          if(parStates[i] == 0)
          {// Xi's parent is C
            Coeffcxixji.add(null);
            LUcxixji.add(null);
            //Coeffxixji.add(null);    
            
            /** coefficients for the statistics Sxi { *
            fileName = (path + File.separator + "LU_varPar0_varStates_" + varStates[i] + "_parCStates0_parXjiStates0.coef");
            f        = new File(fileName);
            if(f.exists())
            {        
              try
              {                
                Coefxi.add(LUDecomposition.read(new FileReader(f)));
              }
              catch(IOException e)
              {
                System.out.println("ERROR: Wrong file format for the LUDecomposition stored object");
                System.exit(-1);
              }        
            }//if(f.exists())
            else
            {// we create the Coeffxi object
              LUDecomposition lu = this.generateCoefSxi(varStates[i]);
              Coefxi.add(lu);
              //save the calculations for a future use
              try
              {
                lu.saveToFile(f);
              }
              catch(Exception e)//FileNotFoundException or IOException
              {
                System.out.println("ERROR: can not create the file to save the LU object");
                System.exit(-1);
              }
            }//else -> !(f.exists())
            /** } coefficients for the statistics Sxi */
            
            /** coefficients for the statistics Scxi { */
            fileNameCoeff = (path + File.separator + "Coeff_varPar1_varStates_" + varStates[i] + "_parCStates" + 
             cStates + "_parXjiStates0.coef");
            fileNameLU    = (path + File.separator + "LU_varPar1_varStates_" + varStates[i] + "_parCStates" + 
             cStates + "_parXjiStates0.coef");
            fCoeff     = new File(fileNameCoeff);
            fLU        = new File(fileNameLU);
            if(fCoeff.exists() && fLU.exists())
            {        
              try
              {
                Coeffcxi.add(Matrix.read(new FileReader(fCoeff)));                
                LUcxi.add(LUDecomposition.read(new FileReader(fLU)));
              }
              catch(IOException e)
              {
                System.out.println("ERROR: Wrong file format for the LUDecomposition stored object");
                System.exit(-1);
              }        
            }//if(f.exists())
            else
            {// we create the Coeffc object
              ArrayList aux = this.generateCoefScxi(cStates,varStates[i]);
              Coeffcxi.add((Matrix)aux.get(0));
              LUcxi.add((LUDecomposition)aux.get(1));
              //save the calculations for a future use
              try
              {
                ((Matrix)aux.get(0)).print(fCoeff);
                ((LUDecomposition)aux.get(1)).saveToFile(fLU);                
              }
              catch(Exception e)//FileNotFoundException or IOException
              {
                System.out.println("ERROR: can not create the file to save the LU object");
                System.exit(-1);
              }
            }//else -> !(f.exists())
             /** } coefficients for the statistics Scxi */
          }//if(parStates[i] == 0)
          else
          {// Xi's parent is another predictive variable, Xji            
            Coeffcxi.add(null); 
            LUcxi.add(null);
            Coeffcxixji.add(null);            
            LUcxixji.add(null);
            
            /** coefficients for the statistics Sxi { **
            fileName = (path + File.separator + "LU_varPar0_varStates_" + varStates[i] + "_parCStates0_parXjiStates0.coef");
            f        = new File(fileName);
            if(f.exists())
            {        
              try
              {                
                Coefxi.add(LUDecomposition.read(new FileReader(f)));
              }
              catch(IOException e)
              {
                System.out.println("ERROR: Wrong file format for the LUDecomposition stored object");
                System.exit(-1);
              }        
            }//if(f.exists())
            else
            {// we create the Coeffc object
              LUDecomposition lu = this.generateCoefSxi(varStates[i]);
              Coefxi.add(lu);
              //save the calculations for a future use
              try
              {
                lu.saveToFile(f);
              }
              catch(Exception e)//FileNotFoundException or IOException
              {
                System.out.println("ERROR: can not create the file to save the LU object");
                System.exit(-1);
              }        
            }//else -> !(f.exists())
            /** } coefficients for the statistics Sxi */
          
            /** coefficients for the statistics Sxixji { **
            fileName = (path + File.separator + "LU_varPar1_varStates_" + varStates[i] + "_parCStates0" + 
             "_parXjiStates"+ parStates[i] + ".coef");
            f       = new File(fileName);
            if(f.exists())
            {        
              try
              {
                Coefxixji.add(LUDecomposition.read(new FileReader(f)));                
              }
              catch(IOException e)
              {
                System.out.println("ERROR: Wrong file format for the LUDecomposition stored object");
                System.exit(-1);
              }        
            }//if(f.exists())
            else
            {// we create the Coeffc object
              LUDecomposition lu = this.generateCoefSxixji(varStates[i],parStates[i]);
              Coefxixji.add(lu);
              //save the calculations for a future use
              try
              {
                lu.saveToFile(f);
              }
              catch(Exception e)//FileNotFoundException or IOException
              {
                System.out.println("ERROR: can not create the file to save the LU object");
                System.exit(-1);
              }
            }//else -> !(f.exists())         
            /** } coefficients for the statistics Sxixji */
          }//else -> !(parStates[i] == 0)          
        }//elseif(parentsL.size() == 1)
        else if(parentsL.size() == 2)
        {// Xi has two parents: C and Xji    
        
          /** coefficients for the statistics Scxi { */
            fileNameCoeff = (path + File.separator + "Coeff_varPar1_varStates_" + varStates[i] + "_parCStates" + 
             cStates + "_parXjiStates0.coef");
            fileNameLU    = (path + File.separator + "LU_varPar1_varStates_" + varStates[i] + "_parCStates" + 
             cStates + "_parXjiStates0.coef");
            fCoeff     = new File(fileNameCoeff);
            fLU        = new File(fileNameLU);
            if(fCoeff.exists() && fLU.exists())
            {        
              try
              {
                Coeffcxi.add(Matrix.read(new FileReader(fCoeff)));                
                LUcxi.add(LUDecomposition.read(new FileReader(fLU)));
              }
              catch(IOException e)
              {
                System.out.println("ERROR: Wrong file format for the LUDecomposition stored object");
                System.exit(-1);
              }        
            }//if(f.exists())
            else
            {// we create the Coeffc object
              ArrayList aux = this.generateCoefScxi(cStates,varStates[i]);
              Coeffcxi.add((Matrix)aux.get(0));           
              LUcxi.add((LUDecomposition)aux.get(1));
              //save the calculations for a future use
              try
              {
                ((Matrix)aux.get(0)).print(fCoeff);
                ((LUDecomposition)aux.get(1)).saveToFile(fLU);                
              }
              catch(Exception e)//FileNotFoundException or IOException
              {
                System.out.println("ERROR: can not create the file to save the LU object");
                System.exit(-1);
              }
            }//else -> !(f.exists())
             /** } coefficients for the statistics Scxi */
          
          /** coefficients for the statistics Scxixji { */
          fileNameCoeff = (path + File.separator + "Coeff_varPar2_varStates_" + varStates[i] + "_parCStates" + 
            cStates + "_parXjiStates" + parStates[i] + ".coef");
          fileNameLU    = (path + File.separator + "LU_varPar2_varStates_" + varStates[i] + "_parCStates" + 
            cStates + "_parXjiStates" + parStates[i] + ".coef");
          fCoeff        = new File(fileNameCoeff);
          fLU           = new File(fileNameLU);
          if(fCoeff.exists() && fLU.exists())
          {        
            try
            {
              Coeffcxixji.add(Matrix.read(new FileReader(fCoeff)));                
              LUcxixji.add(LUDecomposition.read(new FileReader(fLU)));                
            }
            catch(IOException e)
            {
              System.out.println("ERROR: Wrong file format for the LUDecomposition stored object");
              System.exit(-1);
            }        
          }//if(f.exists())
          else
          {// we create the Coefcxixji object
            ArrayList      aux = this.generateCoefScxixji(cStates,varStates[i],parStates[i]);
            Coeffcxixji.add((Matrix)aux.get(0));
            LUcxixji.add((LUDecomposition)aux.get(1));            
            //save the calculations for a future use
            try
            {
              ((Matrix)aux.get(0)).print(fCoeff);
              ((LUDecomposition)aux.get(1)).saveToFile(fLU);
            }
            catch(Exception e)//FileNotFoundException or IOException
            {
              System.out.println("ERROR: can not create the file to save the LU object");
              System.exit(-1);
            }
          }//else -> !(f.exists())
          /** } coefficients for the statistics Scxixji */
             
          /** coefficients for the statistics Sxi { **
          fileName = (path + File.separator + "LU_varPar0_varStates_" + varStates[i] + "_parCStates0_parXjiStates0.coef");
          f        = new File(fileName);
          if(f.exists())
          {        
            try
            {                
              Coefxi.add(LUDecomposition.read(new FileReader(f)));
            }
            catch(IOException e)
            {
              System.out.println("ERROR: Wrong file format for the LUDecomposition stored object");
              System.exit(-1);
            }        
          }//if(f.exists())
          else
          {// we create the Coeffc object
            LUDecomposition lu = this.generateCoefSxi(varStates[i]);
            Coefxi.add(lu);
            //save the calculations for a future use
            try
            {
              lu.saveToFile(f);
            }
            catch(Exception e)//FileNotFoundException or IOException
            {
              System.out.println("ERROR: can not create the file to save the LU object");
              System.exit(-1);
            }        
          }//else -> !(f.exists())
          /** } coefficients for the statistics Sxi */
          
          /** coefficients for the statistics Sxixji{ **
          fileName = (path + File.separator + "LU_varPar1_varStates_" + varStates[i] + "_parCStates0" + 
            "_parXjiStates"+ parStates[i] + ".coef");
          f       = new File(fileName);
          if(f.exists())
          {        
            try
            {
              Coefxixji.add(LUDecomposition.read(new FileReader(f)));                
            }
            catch(IOException e)
            {
              System.out.println("ERROR: Wrong file format for the LUDecomposition stored object");
              System.exit(-1);
            }        
          }//if(f.exists())
          else
          {// we create the Coeffc object            
            LUDecomposition lu = this.generateCoefSxixji(varStates[i],parStates[i]);
            Coefxixji.add(lu);
            //save the calculations for a future use
            try
            {
              lu.saveToFile(f);
            }
            catch(Exception e)//FileNotFoundException or IOException
            {
              System.out.println("ERROR: can not create the file to save the LU object");
              System.exit(-1);
            }
          }//else -> !(f.exists())       
          /** } coefficients for the statistics Sxixji */
          
        }//elseif(parentsL.size() == 2)
      }//end for_i         
  }//Coefficients(NodeList nl)
  
  /**
   * Calculate the values of the discriminative sufficient statistics using the LU coefficient matrix and the 
   * generative sufficient statistics. Generative sufficient statistics are realted discriminative ones via a
   * system of equations coeff * N = M, where coeff is the cofficient matrix whose LU transformation is given 
   * in the current object (Coefficients), N are the generative sufficient statistics and M are the discriminative
   * sufficient statistics
   * @param N values of the current generative sufficient statistics
   * @return the values of the discriminative sufficient statistics
   */
  public DiscriminativeSufficientStatistics calculateDiscriminativeSufficientStatistics(GenerativeSufficientStatistics N)
  {
    int i;
    Iterator nodeIt;
    
    DiscriminativeSufficientStatistics M = new DiscriminativeSufficientStatistics(nodeList);
    
    //Matrix objects used to store partial solutions    
    Matrix A;
    Matrix A_N;
    
    //set the discriminative suficient statistics related to C (Sc)
    // A is the original matrix which the LU transformation have been calculated from.
    // In order to calculate the LU transformation eficiently, A is permutated and the LU transf. for the 
    // permutated matrix is calculated, therefore to retrieve A we have to multiply L*U and then permutate the result
    A    = this.Coeffc;
    //.getMatrix(this.Coefc.getPivot(),0,this.Coefc.getUColumnDimension()-1);
    A_N  = A.times(N.getMatrixScForEquationSystem()); // A_N = A * N    
    M.setSc(A_N.getColumnPackedCopy()); // M = A * N
      
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
          A    = (Matrix)this.Coeffcxi.get(i);
          //.getMatrix(lu.getPivot(),0,lu.getUColumnDimension()-1);
          A_N  = A.times(N.getMatrixScxiForEquationSystem(i)); // A_N = A * N              
          M.setScxi(i,A_N); // M = A * N
          }//if(parent.getComment().compareToIgnoreCase("ClassNode") == 0)
        }//if(links.size() == 1)
        else if(links.size() == 2)
        {//The current variable has two parents: a predictive variable (Xji) and C
         // => we have to update both Scxi and Scxixji
          //Scxi
          A    = (Matrix)this.Coeffcxi.get(i);         
          A_N  = A.times(N.getMatrixScxiForEquationSystem(i)); // A_N = A * N                        
      
          M.setScxi(i,A_N); // M = A * N          
          //Scxixji
          A    = (Matrix)this.Coeffcxixji.get(i);          
          A_N  = A.times(N.getMatrixScxixjiForEquationSystem(i)); // A_N = A * N              
          
          M.setScxixji(i,A_N); // M = A * N
        }//else if(links.size() == 2)        
      }//for(i=0;i<this.nVar;i++)
      return M;
  }//DiscriminativeSufficientStatistics calculateDiscriminativeSufficientStatistics(GenerativeSufficientStatistics N)
  
  /**
   * Generate a coefficient Matrix and its LUDecomposition object. The coeficients are those ones 
   * for the equation system needed to obtain the generative sufficient statistics from
   * the discriminative sufficient statistics.
   * @param cStates number of states for the class value
   * @return ArrayList with two elements 1st  = the coefficients for the equation system
   * and 2nd = the LU transformation of the coefficient matrix.
   */
  private ArrayList generateCoefSc(int cStates)
  {
    int i,j;
    //The equation system does not include informtation for the sufficient 
    // statistic N(C=0) because it can be calculated from the others suffient
    // statistics.
    double [][] values = new double[cStates-1][cStates-1];
    for(i=0;i<cStates-1;i++)
    {
      if(i==0)
      {//the terms in the first row from values don't need to be reaise to any exponent
        for(j=0;j<cStates-1;j++)
        {
          values[i][j] = (double)j+1;
        }//for(j=0;j<cStates-1;j++)        
      }//if(i==0)
      else
      {
        values[i][0] = 1;
        for(j=1;j<cStates-1;j++)
        {
          values[i][j] = Math.pow(j+1,i+1);
        }//for(j=0;j<cStates-1;j++)
      }//else -> !(i==0)
    }//for(i=0;i<cStates-1;i++)
    ArrayList coeffs_LU = new ArrayList();  
  
    Matrix m = new Matrix(values);
    coeffs_LU.add(m);
    /********************************************************************************************************
    String fname = new String("coeff_varPar0_varStates_" + cStates + "_parCStates0_parXjiStates0.dat");
    try
    {
      FileOutputStream fo = new FileOutputStream("/home/guzman/coeffs/" + fname);
      PrintWriter p = new PrintWriter(fo);
      double [][] mat = m.getArray();
      for(i=0;i<mat.length;i++)
      {
        for(j=0;j<mat[i].length;j++)
        {
          p.print(mat[i][j] + ";");
        }
        p.println();
      }
      p.close();
      fo.close();
    }
    catch(Exception e){}
    /********************************************************************************************************/
    LUDecomposition lu = new LUDecomposition(m);
    coeffs_LU.add(lu);
    return coeffs_LU;  
  }//generateCoefSc(int cStates)
  
  /**
   * Generate only the coefficient Matrix  
   * for the equation system needed to obtain the generative sufficient statistics Sxi from
   * the discriminative sufficient statistics.
   * @param xiStates number of states for the class value
   * @return Matrix the coefficients matrix
   *
  private Matrix generateCoefSxi(int xiStates)
  {
    int i,j;
    //The equation system does not include informtation for the sufficient 
    // statistic N(Xi=0) because it can be calculated from the others suffient
    // statistics.
    double [][] values = new double[xiStates-1][xiStates-1];
    for(i=0;i<xiStates-1;i++)
    {
      if(i==0)
      {//the terms in the first row from values don't need to be reaise to any exponent
        for(j=0;j<xiStates-1;j++)
        {
          values[i][j] = (double)j+1;
        }//for(j=0;j<cStates-1;j++)        
      }//if(i==0)
      else
      {
        values[i][0] = 1;
        for(j=1;j<xiStates-1;j++)
        {
          values[i][j] = Math.pow(j+1,i+1);
        }//for(j=0;j<cStates-1;j++)
      }//else -> !(i==0)
    }//for(i=0;i<cStates-1;i++)

    Matrix m = new Matrix(values);    
    return m;  
  }//generateCoefSxi(int xiStates)
  
  /**
   * Generate a coefficient Matrix and its LUDecomposition object. The coeficients are those ones 
   * for the equation system needed to obtain the generative sufficient statistics Scxi from
   * the discriminative sufficient statistics.
   * @param cStates number of states for the class value
   * @param xiStates number of states for the class value
   * @return ArrayList with two elements 1st  = the coefficients for the equation system
   * and 2nd = the LU transformation of the coefficient matrix.
   */
  private ArrayList generateCoefScxi(int cStates, int xiStates)
  {
    int coefC,coefXi,expC,expXi;
    //The equation system does not include informtation for the sufficient 
    // statistic N(Xi=0) because it can be calculated from the others suffient
    // statistics.
    double [][] values = new double[(cStates-1) * (xiStates-1)][(cStates-1) * (xiStates-1)];
    for(expXi=0;expXi<xiStates-1;expXi++)
    {
      for(expC=0;expC<cStates-1;expC++)      
      {
        for(coefXi=0;coefXi<xiStates-1;coefXi++)
        {
          for(coefC=0;coefC<cStates-1;coefC++)          
          {
            int row    = expC + (expXi * (cStates - 1));
            int column = coefC + (coefXi * (cStates -1));
            if(expC == 0 && expXi == 0)
            {
              values[row][column] =  (coefC + 1) * (coefXi + 1); 
            }//if(expC == 0 && expXi == 0)
            else if(expC == 0 && expXi != 0)
            {
              values[row][column] = (coefC + 1) * Math.pow(coefXi + 1,expXi + 1); 
            }//else if(expC == 0 && expXi != 0)
            else if(expC != 0 && expXi == 0)
            {
              values[row][column] =  (Math.pow(coefC + 1,expC + 1)) * (coefXi + 1); 
            }//else if(expC != 0 && expXi == 0)
            else
            {
              values[row][column] =  (Math.pow(coefC + 1,expC + 1)) * (Math.pow(coefXi + 1, expXi + 1)); 
            }//else -> (expC != 0 && expXi != 0)
          }//for(coefXi=0;coefXi<xiStates-1;coefXi++)
        }//for(coefC=0;coefC<cStates-1;coefC++)        
      }//for(expXi=0;expXi<xiStates-1;expXi++)
    }//for(expC=0;expC<cStates-1;expC++)
    ArrayList coeffs_LU = new ArrayList();
    Matrix m = new Matrix(values);
    coeffs_LU.add(m);
    /********************************************************************************************************
    String fname = new String("coeff_varPar1_varStates_" + xiStates + "_parCStates" + cStates + "_parXjiStates0.dat");
    try
    {
      FileOutputStream fo = new FileOutputStream("/home/guzman/coeffs/" + fname);
      PrintWriter p = new PrintWriter(fo);
      //m.print(p,20,12);
      double [][] mat = m.getArray();
      for(int i=0;i<mat.length;i++)
      {
        for(int j=0;j<mat[i].length;j++)
        {
          p.print(mat[i][j] + ";");
        }
        p.println();
      }
      p.close();
      fo.close();
    }
    catch(Exception e){}
    /********************************************************************************************************/
    LUDecomposition lu = new LUDecomposition(m);  
    coeffs_LU.add(lu);
    return coeffs_LU;  
  }//generateCoefScxi(int cStates, int xiStates)
  
  /**
   * Generate only the coefficient Matrix  
   * for the equation system needed to obtain the generative sufficient statistics Sxixji from
   * the discriminative sufficient statistics.
   * @param xjiStates number of states for the predictive variable which is parent of Xi, i.e. Xji
   * @return Matrix the coefficient matrix
   *
  private Matrix generateCoefSxixji(int xiStates, int xjiStates)
  {
    int coefXi,coefXji,expXi,expXji;
    //The equation system does not include informtation for the sufficient 
    // statistic N(Xi=0,Xji=xji), N(Xi=xi,Xji=0), N(Xi=0,Xji=0) because it can be calculated from the others suffient
    // statistics.
    double [][] values = new double[(xiStates-1) * (xjiStates-1)][(xiStates-1) * (xjiStates-1)];
    for(expXi=0;expXi<xiStates-1;expXi++)
    {
      for(expXji=0;expXji<xjiStates-1;expXji++)
      {
        for(coefXi=0;coefXi<xiStates-1;coefXi++)
        {
          for(coefXji=0;coefXji<xjiStates-1;coefXji++)
          {
            int row    = expXi + (expXji * (xiStates - 1));
            int column = coefXi + (coefXji * (xiStates -1));
            if(expXi == 0 && expXji == 0)
            {
              values[row][column] =  (coefXi + 1) * (coefXji + 1); 
            }//if(expXi == 0 && expXji == 0)
            else if(expXi == 0 && expXji != 0)
            {
              values[row][column] =  (coefXi + 1) * Math.pow(coefXji + 1,expXji + 1); 
            }//else if(expXi == 0 && expXji != 0)
            else if(expXi != 0 && expXji == 0)
            {
              values[row][column] =  (Math.pow(coefXi + 1,expXi + 1)) * (coefXji + 1); 
            }//else if(expXi != 0 && expXji == 0)
            else
            {
              values[row][column] =  (Math.pow(coefXi + 1,expXi + 1)) * (Math.pow(coefXji + 1, expXji + 1)); 
            }//else -> (expXi != 0 && expXji != 0)
          }//for(coefXji=0;coefXji<xjiStates-1;coefXji++)
        }//for(coefXi=0;coefXi<xiStates-1;coefXi++)        
      }//for(expXji=0;expXji<xjiStates-1;expXji++)
    }//for(expXi=0;expXi<xiStates-1;expXi++)
    Matrix m = new Matrix(values);
    return m;  
  }//generateCoefSxixji(int xiStates, int xjiStates)
  
  /**
   * Generate a coefficient Matrix and its LUDecomposition object. The coeficients are those ones 
   * for the equation system needed to obtain the generative sufficient statistics Scxixji from
   * the discriminative sufficient statistics.
   * @param cStates number of states for the class variable
   * @param xiStates number of states for the Xi variable
   * @param xjiStates number of states for the Xji variable
   * @return ArrayList with two elements 1st  = the coefficients for the equation system
   * and 2nd = the LU transformation of the coefficient matrix.

   */
  private ArrayList generateCoefScxixji(int States, int xiStates, int xjiStates)
  {
    int coefC,coefXi,coefXji,expC,expXi,expXji;
    //The equation system does not include informtation for the sufficient 
    // statistic N(Xi=0) because it can be calculated from the others suffient
    // statistics.
    double [][] values = new double[(cStates-1) * (xiStates-1) * (xjiStates-1)][(cStates-1) * (xiStates-1) * (xjiStates-1)];
    for(expXji=0;expXji<xjiStates-1;expXji++)
    {
      for(expXi=0;expXi<xiStates-1;expXi++)
      {        
        for(expC=0;expC<cStates-1;expC++)
        {     
          for(coefXji=0;coefXji<xjiStates-1;coefXji++)
          {
            for(coefXi=0;coefXi<xiStates-1;coefXi++)
            {
              for(coefC=0;coefC<cStates-1;coefC++)              
              {
                int row    = expC + (expXi * (cStates - 1)) + (expXji * (cStates - 1) * (xiStates - 1));
                int column = coefC + (coefXi * (cStates -1))+ (coefXji * (cStates - 1) * (xiStates - 1));
              
                values[row][column] = Math.pow(coefC + 1,expC + 1) * Math.pow(coefXi + 1, expXi + 1) *
                                      Math.pow(coefXji + 1,expXji + 1);
              }//for(coefXji=0;coefXji<xjiStates-1;coefXji++)
            }//for(coefXi=0;coefXi<xiStates-1;coefXi++)
          }//for(coefC=0;coefC<cStates-1;coefC++)        
        }//for(expXji=0;expXji<xjiStates-1;expXji++)
      }//for(expXi=0;expXi<xiStates-1;expXi++)
    }//for(expC=0;expC<cStates-1;expC++)
    ArrayList coeffs_LU = new ArrayList();
    Matrix m = new Matrix(values);
    coeffs_LU.add(m);
    /********************************************************************************************************
    String fname = new String("coeff_varPar2_varStates_" + xiStates + "_parCStates" + cStates + "_parXjiStates" + xjiStates + ".dat");
    try
    {
      FileOutputStream fo = new FileOutputStream("/home/guzman/coeffs/" + fname);
      PrintWriter p = new PrintWriter(fo);
      double [][] mat = m.getArray();
      for(int i=0;i<mat.length;i++)
      {
        for(int j=0;j<mat[i].length;j++)
        {
          p.print(mat[i][j] + ";");
        }
        p.println();
      }
      p.close();
      fo.close();
    }
    catch(Exception e){}
    /********************************************************************************************************/
    LUDecomposition lu = new LUDecomposition(m);
    coeffs_LU.add(lu);
    return coeffs_LU;  
  }//generateCoefScxixji(int States, int xiStates, int xjiStates)
  
  /**
   * Solves the Equation system A X = B, where A is the LU transformation of the
   * coefficient matrix (Coefc), B is the Matrix that contains the discriminative
   * sufficient Statistics (Discriminative-Sc) and X is the solution which we want
   * to obtain. X represents the generative sufficient statistics (generative-Sc).
   * @param M matrix containing the discriminative sufficient statistics
   * @return a Matrix containing the generative sufficient statistics
   */
  public Matrix solveEquationSystemSc(Matrix M)
  {
    Matrix          A = Coeffc;
    LUDecomposition lu = LUc;
    
    boolean bigError = true;
    double  error;
    Matrix  x        = lu.solve(M);

    error = Matrix.getError(A.times(x),M);
    if(error < MAX_ERROR)
    {
        bigError = false;
    }//if(error < MAX_ERROR)
    while(bigError)
    {
        Matrix aux1 = A.times(x).minusEquals(M);
        Matrix  dxi = lu.solve(aux1);

        Matrix aux2 = A.times(dxi).minusEquals(aux1);
        Matrix ddxi = lu.solve(aux2);

        Matrix aux3  = A.times(ddxi).minus(aux2);
        Matrix dddxi = lu.solve(aux3);

        ddxi.minusEquals(dddxi);
        dxi.minusEquals(ddxi);

        Matrix currentX = x.minus(dxi);
        double currentError = Matrix.getError(A.times(currentX),M);
        if(currentError < MAX_ERROR)
        {
          x = currentX;
          bigError = false;
        }//if(error < MAX_ERROR)
        else if(currentError > error)
        {
          bigError = false;
        }
        else
        {
          error = currentError;
          x     = currentX;
        }
    }//while(bigError)    
    return x;
  }//solveEquationSystemSc(Matrix M)
  
  /**
   * Solves the Equation system A X = B, where A is the LU transformation of the
   * coefficient matrix (Coefcxi), B is the Matrix that contains the discriminative
   * sufficient Statistics (Discriminative-Scxi) and X is the solution which we want
   * to obtain. X represents the generative sufficient statistics (generative-Scxi).
   * @param Xi index of the variable whose generative sufficient statistics we want to obtain
   * @parem M matrix containing the discriminative sufficient statistics
   * @return a Matrix containing the generative sufficient statistics
   */
  public Matrix solveEquationSystemScxi(int Xi, Matrix M)
  {
    LUDecomposition lu = (LUDecomposition)LUcxi.get(Xi); 
    Matrix           A = (Matrix)Coeffcxi.get(Xi); 
    
    boolean bigError = true;
    double  error;
    Matrix  x        = lu.solve(M);

    error = Matrix.getError(A.times(x),M);
    if(error < MAX_ERROR)
    {
        bigError = false;
    }//if(error < MAX_ERROR)
    while(bigError)
    {
        Matrix aux1 = A.times(x).minusEquals(M);
        Matrix  dxi = lu.solve(aux1);

        Matrix aux2 = A.times(dxi).minusEquals(aux1);
        Matrix ddxi = lu.solve(aux2);

        Matrix aux3  = A.times(ddxi).minus(aux2);
        Matrix dddxi = lu.solve(aux3);

        ddxi.minusEquals(dddxi);
        dxi.minusEquals(ddxi);

        Matrix currentX = x.minus(dxi);
        double currentError = Matrix.getError(A.times(currentX),M);
        if(currentError < MAX_ERROR)
        {
          x = currentX;
          bigError = false;
        }//if(error < MAX_ERROR)
        else if(currentError > error)
        {
          bigError = false;
        }
        else
        {
          error = currentError;
          x     = currentX;
        }
    }//while(bigError)    
    return x;
  }//solveEquationSystemScxi(int Xi, Matrix M)
  
  /**
   * Solves the Equation system A X = B, where A is the LU transformation of the
   * coefficient matrix (Coefcxisji), B is the Matrix that contains the discriminative
   * sufficient Statistics (Discriminative-Scxixji) and X is the solution which we want
   * to obtain. X represents the generative sufficient statistics (generative-Scxixji).
   * @param Xi index of the variable whose generative sufficient statistics we want to obtain
   * @parem M matrix containing the discriminative sufficient statistics
   * @return a Matrix containing the generative sufficient statistics
   */
  public Matrix solveEquationSystemScxixji(int Xi, Matrix M)
  {
    LUDecomposition lu = (LUDecomposition)LUcxixji.get(Xi);
    Matrix           A = (Matrix)Coeffcxixji.get(Xi);
    
    boolean bigError = true;
    double  error;
    Matrix  x        = lu.solve(M);

    error = Matrix.getError(A.times(x),M);
    if(error < MAX_ERROR)
    {
        bigError = false;
    }//if(error < MAX_ERROR)
    while(bigError)
    {
        Matrix aux1 = A.times(x).minusEquals(M);
        Matrix  dxi = lu.solve(aux1);

        Matrix aux2 = A.times(dxi).minusEquals(aux1);
        Matrix ddxi = lu.solve(aux2);

        Matrix aux3  = A.times(ddxi).minus(aux2);
        Matrix dddxi = lu.solve(aux3);

        ddxi.minusEquals(dddxi);
        dxi.minusEquals(ddxi);

        Matrix currentX = x.minus(dxi);
        double currentError = Matrix.getError(A.times(currentX),M);
        if(currentError < MAX_ERROR)
        {
          x = currentX;
          bigError = false;
        }//if(error < MAX_ERROR)
        else if(currentError > error)
        {
          bigError = false;
        }
        else
        {
          error = currentError;
          x     = currentX;
        }
    }//while(bigError)    
    return x;
  }//solveEquationSystemScxixji(int Xi, Matrix M)

}