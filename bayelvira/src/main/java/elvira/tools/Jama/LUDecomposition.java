package elvira.tools.Jama;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;


   /** LU Decomposition.
   <P>
   For an m-by-n matrix A with m >= n, the LU decomposition is an m-by-n
   unit lower triangular matrix L, an n-by-n upper triangular matrix U,
   and a permutation vector piv of length m so that A(piv,:) = L*U.
   If m < n, then L is m-by-m and U is m-by-n.
   <P>
   The LU decompostion with pivoting always exists, even if the matrix is
   singular, so the constructor will never fail.  The primary use of the
   LU decomposition is in the solution of square systems of simultaneous
   linear equations.  This will fail if isNonsingular() returns false.
   */

public class LUDecomposition implements java.io.Serializable {

/* ------------------------
   Class variables
 * ------------------------ */

   /** Array for internal storage of decomposition.
   @serial internal array storage.
   */
   private double[][] LU;

   /** Row and column dimensions, and pivot sign.
   @serial column dimension.
   @serial row dimension.
   @serial pivot sign.
   */
   private int m, n, pivsign; 

   /** Internal storage of pivot vector.
   @serial pivot vector.
   */
   private int[] piv;

/* ------------------------
   Constructor
 * ------------------------ */

   /** LU Decomposition
    * private constructor to be used by the static read method to construtc tan
    * empty LUDecomposition Objet
    */
   private LUDecomposition()
   {
     
   }
   /** LU Decomposition
   @param  A   Rectangular matrix
   @return     Structure to access L, U and piv.
   */

   public LUDecomposition (Matrix A) {

   // Use a "left-looking", dot-product, Crout/Doolittle algorithm.

      LU = A.getArrayCopy();
      m = A.getRowDimension();
      n = A.getColumnDimension();
      piv = new int[m];
      for (int i = 0; i < m; i++) {
         piv[i] = i;
      }
      pivsign = 1;
      double[] LUrowi;
      double[] LUcolj = new double[m];

      // Outer loop.

      for (int j = 0; j < n; j++) {

         // Make a copy of the j-th column to localize references.

         for (int i = 0; i < m; i++) {
            LUcolj[i] = LU[i][j];
         }

         // Apply previous transformations.

         for (int i = 0; i < m; i++) {
            LUrowi = LU[i];

            // Most of the time is spent in the following dot product.

            int kmax = Math.min(i,j);
            double s = 0.0;
            for (int k = 0; k < kmax; k++) {
               s += LUrowi[k]*LUcolj[k];
            }

            LUrowi[j] = LUcolj[i] -= s;
         }
   
         // Find pivot and exchange if necessary.

         int p = j;
         for (int i = j+1; i < m; i++) {
            if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
               p = i;
            }
         }
         if (p != j) {
            for (int k = 0; k < n; k++) {
               double t = LU[p][k]; LU[p][k] = LU[j][k]; LU[j][k] = t;
            }
            int k = piv[p]; piv[p] = piv[j]; piv[j] = k;
            pivsign = -pivsign;
         }

         // Compute multipliers.
         
         if (j < m & LU[j][j] != 0.0) {
            for (int i = j+1; i < m; i++) {
               LU[i][j] /= LU[j][j];
            }
         }
      }
   }

/* ------------------------
   Temporary, experimental code.
   ------------------------ *\

   \** LU Decomposition, computed by Gaussian elimination.
   <P>
   This constructor computes L and U with the "daxpy"-based elimination
   algorithm used in LINPACK and MATLAB.  In Java, we suspect the dot-product,
   Crout algorithm will be faster.  We have temporarily included this
   constructor until timing experiments confirm this suspicion.
   <P>
   @param  A             Rectangular matrix
   @param  linpackflag   Use Gaussian elimination.  Actual value ignored.
   @return               Structure to access L, U and piv.
   *\

   public LUDecomposition (Matrix A, int linpackflag) {
      // Initialize.
      LU = A.getArrayCopy();
      m = A.getRowDimension();
      n = A.getColumnDimension();
      piv = new int[m];
      for (int i = 0; i < m; i++) {
         piv[i] = i;
      }
      pivsign = 1;
      // Main loop.
      for (int k = 0; k < n; k++) {
         // Find pivot.
         int p = k;
         for (int i = k+1; i < m; i++) {
            if (Math.abs(LU[i][k]) > Math.abs(LU[p][k])) {
               p = i;
            }
         }
         // Exchange if necessary.
         if (p != k) {
            for (int j = 0; j < n; j++) {
               double t = LU[p][j]; LU[p][j] = LU[k][j]; LU[k][j] = t;
            }
            int t = piv[p]; piv[p] = piv[k]; piv[k] = t;
            pivsign = -pivsign;
         }
         // Compute multipliers and eliminate k-th column.
         if (LU[k][k] != 0.0) {
            for (int i = k+1; i < m; i++) {
               LU[i][k] /= LU[k][k];
               for (int j = k+1; j < n; j++) {
                  LU[i][j] -= LU[i][k]*LU[k][j];
               }
            }
         }
      }
   }

\* ------------------------
   End of temporary code.
 * ------------------------ */

/* ------------------------
   Public Methods
 * ------------------------ */

   /** Is the matrix nonsingular?
   @return     true if U, and hence A, is nonsingular.
   */

   public boolean isNonsingular () {
      for (int j = 0; j < n; j++) {
         if (LU[j][j] == 0)
            return false;
      }
      return true;
   }
   
   /** Print the matrix to the output stream.   Elements are separated by
    * semi-colon (;).
   @param output Output stream.  
   @param d      Number of digits after the decimal.
   */

   public void print (PrintWriter output, int d) {
      DecimalFormat format = new DecimalFormat();
      format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
      format.setMinimumIntegerDigits(1);
      format.setMaximumFractionDigits(d);
      format.setMinimumFractionDigits(d);
      format.setGroupingUsed(false);
      print(output,format);
   }
   
    /** Print the matrix to the output stream.  Line the elements up in columns.
     * The elemets are separated by semicoloms (;)
     * Note that is the matrix is to be read back in, you probably will want
     * to use a NumberFormat that is set to US Locale.
   @param output the output stream.
   @param format A formatting object to format the matrix elements 
   
   @see java.text.DecimalFormat#setDecimalFormatSymbols
   */

   public void print (PrintWriter output, NumberFormat format) {
      int i,j;      
      output.println("// value of m");
      output.println(this.m);
      output.println("// value of n");
      output.println(this.n);
      output.println("// pivot = ");
      for(i=0;i<m;i++)
      {        
        output.print(piv[i] + " ");
      }
      output.println();
      output.println("//pivsign = ");
      output.println(this.pivsign);
      
      output.println("// LU matrix values = ");
      for (i = 0; i < m; i++) {
         for (j = 0; j < n; j++) {
            String s = format.format(LU[i][j]) + " "; // format the number            
            output.print(s);
         }
         output.println();
      }
      output.println();   // end with blank line.
   }


   /** Return lower triangular factor
   @return     L
   */

   public Matrix getL () {
      Matrix X = new Matrix(m,n);
      double[][] L = X.getArray();
      for (int i = 0; i < m; i++) {
         for (int j = 0; j < n; j++) {
            if (i > j) {
               L[i][j] = LU[i][j];
            } else if (i == j) {
               L[i][j] = 1.0;
            } else {
               L[i][j] = 0.0;
            }
         }
      }
      return X;
   }   
   
   /**
   * Returns the number of columns for the L matrix
   * @return column dimension for L matrix
   */
   public int getLColumnDimension()
   {
     return m;
   }//getLColumnDimension()
   
   /**
   * Returns the number of rows for the L matrix
   * @return row dimension for L matrix
   */
   public int getLRowDimension()
   {
     return m;
   }//getLRowDimension()

   /** Return upper triangular factor
   @return     U
   */

   public Matrix getU () {
      Matrix X = new Matrix(n,n);
      double[][] U = X.getArray();
      for (int i = 0; i < n; i++) {
         for (int j = 0; j < n; j++) {
            if (i <= j) {
               U[i][j] = LU[i][j];
            } else {
               U[i][j] = 0.0;
            }
         }
      }
      return X;
   }

  /**
   * Returns the number of columns for the U matrix
   * @return column dimension for U matrix
   */
   public int getUColumnDimension()
   {
     return n;
   }//getUColumnDimension()
   
   /**
   * Returns the number of rows for the U matrix
   * @return row dimension for U matrix
   */
   public int getURowDimension()
   {
     return m;
   }//getURowDimension()

   /** Return pivot permutation vector
   @return     piv
   */

   public int[] getPivot () {
      int[] p = new int[m];
      for (int i = 0; i < m; i++) {
         p[i] = piv[i];
      }
      return p;
   }

   /** Return pivot permutation vector as a one-dimensional double array
   @return     (double) piv
   */

   public double[] getDoublePivot () {
      double[] vals = new double[m];
      for (int i = 0; i < m; i++) {
         vals[i] = (double) piv[i];
      }
      return vals;
   }

   /** Determinant
   @return     det(A)
   @exception  IllegalArgumentException  Matrix must be square
   */

   public double det () {
      if (m != n) {
         throw new IllegalArgumentException("Matrix must be square.");
      }
      double d = (double) pivsign;
      for (int j = 0; j < n; j++) {
         d *= LU[j][j];
      }
      return d;
   }

    /** Read a matrix from a FileInputStream.  The format is the same the print method,
     * so printed matrices can be read back in (provided they were printed using
     * US Locale).  Elements are separated by
     * semi-coloms. The elements which appear in the file are in the following order:
     * m,n,piv,pivsign and LU, all the elements for each row from the LU matrix appear on a single line,
     * the last row is followed by a blank line.
   @param input the input stream.
   */

   public static LUDecomposition read (FileReader input) throws java.io.IOException {      
      int i,j;
      
      LUDecomposition lu = new LUDecomposition();
      StreamTokenizer tokenizer= new StreamTokenizer(new BufferedReader(input));
      tokenizer.slashSlashComments(true); //ignore the coments introduced by //

      //read the value of m
      tokenizer.nextToken();      
      lu.m = (int)tokenizer.nval;
      //read the value of n
      tokenizer.nextToken();      
      lu.n = (int)tokenizer.nval;
      
      //read the pivot
      lu.piv = new int[lu.m];
      for(i=0;i<lu.m;i++)
      {
        tokenizer.nextToken();  
        lu.piv[i] = (int)tokenizer.nval;
      }
      
      //read the pivsign
      tokenizer.nextToken();  
      lu.pivsign = (int)tokenizer.nval;
      
      //read the values for the LU matrix
      lu.LU = new double[lu.m][lu.n];
      for (i = 0; i < lu.m; i++) 
      {
         for (j = 0; j < lu.n; j++) 
         {
          tokenizer.nextToken();
          lu.LU[i][j] = tokenizer.nval;          
        }
      }
      return lu;
   }

   /**
   *  Writes the current object into a file. The format of the file is specific of this method and
   *  the objet can be retrieved by the read method
   * @param file the file where we want to save the current object
   */
   public void saveToFile(File file) throws FileNotFoundException, IOException
   {
      FileOutputStream out = new FileOutputStream(file);
      PrintWriter p = new PrintWriter(out);
      this.print(p,20);
      p.close();
      out.close();  
   }
   
   /** Solve A*X = B
   @param  B   A Matrix with as many rows as A and any number of columns.
   @return     X so that L*U*X = B(piv,:)
   @exception  IllegalArgumentException Matrix row dimensions must agree.
   @exception  RuntimeException  Matrix is singular.
   */

   public Matrix solve (Matrix B) {
      if (B.getRowDimension() != m) {
         throw new IllegalArgumentException("Matrix row dimensions must agree.");
      }
      if (!this.isNonsingular()) {
         throw new RuntimeException("Matrix is singular.");
      }

      // Copy right hand side with pivoting
      int nx = B.getColumnDimension();
      Matrix Xmat = B.getMatrix(piv,0,nx-1);
      double[][] X = Xmat.getArray();

      // Solve L*Y = B(piv,:)
      for (int k = 0; k < n; k++) {
         for (int i = k+1; i < n; i++) {
            for (int j = 0; j < nx; j++) {
               X[i][j] -= X[k][j]*LU[i][k];
            }
         }
      }
      // Solve U*X = Y;
      for (int k = n-1; k >= 0; k--) {
         for (int j = 0; j < nx; j++) {
            X[k][j] /= LU[k][k];
         }
         for (int i = 0; i < k; i++) {
            for (int j = 0; j < nx; j++) {
               X[i][j] -= X[k][j]*LU[i][k];
            }
         }
      }
      return Xmat;
   }
}
