/* Discretization.java */
package elvira.learning.preprocessing;

import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.potential.*;
import elvira.learning.preprocessing.*;
import elvira.parser.ParseException;

import java.io.*;
import java.lang.Runtime;
import java.lang.Math;
import java.util.*;

/**
 *
 * Implements several discretization methods for continuous values.It's possible to select which
 * variables to discretize and the operation.
 * For example:
 *<pre>
 *       Vector  Opts;
 *       Opts=new Vector();
 *       Discretization discretization = new Discretization();
 *       discretization.LoadData("mydata.dbc");
 *       discretization.SetMode(Discretization.DISCRETIZE_INDIVIDUALLY);
 *       discretization.SetOperation(MASSIVE_OPERATION);
 *	   discretization.ConfigureIndividual(Discretization.EQUAL_WIDTH,10,Opts);
 *	   discretization.apply();
 *       discretization.SaveData();
 *
 *</pre>
 * Another example where Opts Vector is necessary for Sum Square Differences algorithm
 *<pre>
 *       Vector  Opts;
 *       Opts=new Vector();
 *	   Opts.add( new Double(0.5));
 *       Discretization discretization = new Discretization();
 *       discretization.LoadData("mydata.dbc");
 *       discretization.SetMode(Discretization.DISCRETIZE_INDIVIDUALLY);
 *       discretization.SetOperation(MASSIVE_OPERATION);
 *	   discretization.ConfigureIndividual(Discretization.SUM_SQUARE_DIFFERENCES,10,Opts);
 *	   discretization.apply();
 *       discretization.SaveData();
 *
 *</pre>
 * And other example using NORMAL_OPERATION where each algorithm discretize one variable 
 * with one specific number of intervals.
 *<pre>
 *
 *       Vector  Opts;
 *       Opts=new Vector();
 *	   Opts.add( new Double(0.5));
 *       Discretization discretization = new Discretization();
 *       discretization.LoadData("mydata.dbc");
 *       discretization.SetMode(Discretization.DISCRETIZE_INDIVIDUALLY);
 *       discretization.SetOperation(NORMAL_OPERATION);
 *	   discretization.ConfigureIndividual(0,Discretization.EQUAL_WIDTH                     , 3,Opts);
 *	   discretization.ConfigureIndividual(1,Discretization.EQUAL_FREQUENCY                 ,10,Opts);
 *	   discretization.ConfigureIndividual(2,Discretization.SUM_SQUARE_DIFFERENCES          , 5,Opts);
 *	   discretization.ConfigureIndividual(3,Discretization.K_MEANS                         , 7,Opts);
 *	   discretization.ConfigureIndividual(4,Discretization.UNSUPERVISED_MONOTHETIC_CONTRAST, 8,Opts);
 *	   discretization.apply();
 *       discretization.SaveData();
 *
 *</pre>
 *
 * Note: For large databases and with a big intervals number will produce a OutOfMemory Exception,
 *       to solve this issue, you can use the java interpreter with more memory. For example:
 *<pre>
 *       java -Xms512m -Xmx512m elvira/learning/preprocessing/Discretization
 *</pre>
 * @author fjgc@decsai.ugr.es, jlflores@si.ehu.es
 * @version 0.1
 * @since 22/05/2003
 */
public class Discretization implements Serializable{

    static final long serialVersionUID = 5027046723439634977L;
    
    /* These constants are used to set the discretization method */
    public static final int NO_ALGORITHM                        = -1;
    public static final int EQUAL_FREQUENCY                     =  0;
    public static final int EQUAL_WIDTH                         =  1;
    public static final int SUM_SQUARE_DIFFERENCES              =  2;
    public static final int UNSUPERVISED_MONOTHETIC_CONTRAST    =  3;
    public static final int K_MEANS                             =  4;
    public static final int WD_EDA                              =  5;

    /* These constants are used for configuring the discretization */
    public static final int NO_DISCRETIZE                   = 0;
    public static final int DISCRETIZE_GLOBALLY             = 1;
    public static final int DISCRETIZE_INDIVIDUALLY         = 2;

    /* These constants are used for setting operation in individual discretization */
    public static final int NO_OPERATION                    = 0;
    public static final int NORMAL_OPERATION                = 1;
    public static final int MASSIVE_OPERATION               = 2;
    
    public static final String  ErrorMessage[] = {  "ERROR: Variable previously configured"                     ,
                                                    "ERROR: Mode not valid"                                     ,
                                                    "ERROR: Operation not valid"                                ,
                                                    "ERROR: Algorith not valid"                                 ,
                                                    "ERROR: Variable out bounds"                                ,
                                                    "ERROR: Type of Variable not compatible or out of bounds"   ,
                                                    "ERROR: Intervals number too low or too high"               ,
                                                    "ERROR: Intervals number incorrect"                         ,
                                                    "ERROR: Variable non-existent"                              ,
                                                    "ERROR: Varaible selection not valid"                       ,
                                                    "ERROR: Intervals number too high"                          };

    static final int WD_NORMAL_SIMULATION_POLAR                 = 0;
    static final int WD_NORMAL_SIMULATION_BOX_MULLER            = 1;
    static final int WD_NORMAL_SIMULATION_CENTRAL_THEOREM_LIMIT = 2;
    

    /**
     * Default discretization method.
     */
    int discretizationMethod=NO_ALGORITHM;
    
    /* Members variables for configuring the discretization process */
    int             m_Mode                      = NO_DISCRETIZE         ;
    int             m_Operation                 = NO_OPERATION          ;
    int             m_Algorithm                 = NO_ALGORITHM          ;
    Vector          m_Options                   = new Vector()          ;
    Vector          m_Configuration             = new Vector()          ;

    Vector          m_ContinuousVariablesVector = new Vector()          ;
    DataBaseCases   m_Cases                                             ;
    DataBaseCases   m_DiscretizedCasesDB                                ;
    Vector          m_DBCInformation            = new Vector()          ;
    
    /*---------------------------------------------------------------*/ 
    //Cada posicion contiene un vector con (Node n, Vector intervalos)
    Vector[] IntervalsVectorVar=new Vector[2];
    
    /**
    *  Basic Constructor
    */
    public Discretization() { 
     IntervalsVectorVar[0]=new Vector();
     IntervalsVectorVar[1]=new Vector();
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining the number of cases of the database.
    *  @return <code>int</code> Number of cases of the database.
    */
    private int GetNumberOfCases() {
      	Vector      DataBaseVector  =   m_Cases.getRelationList();
        Relation    RelationList    =   (Relation)DataBaseVector.elementAt(0);
        CaseListMem caselistmem     =   (CaseListMem)RelationList.getValues();
        int         NumberOfCases   =   caselistmem.getNumberOfCases();
        return NumberOfCases;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining the number of variables of the database.
    *  @return <code>int</code> Number of variables of the database.
    */
    private int GetNumberOfVariables() {
        int         i                   ;
        int         NumberOfVariables   ;
        NodeList    Nodes               ;
        
        Nodes=m_Cases.getVariables(); 
        NumberOfVariables=Nodes.size();
        return      NumberOfVariables;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining the number of continuous variables of the database.
    *  @return <code>int</code> Number of continuous variables of the database.
    */
    private int GetNumberOfContinuousVariables() {
        int NumberOfDiscreteVariables;
        NumberOfDiscreteVariables= m_ContinuousVariablesVector.size();
        return NumberOfDiscreteVariables;
    }
    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining a selected continuous variable.
    *  @return <code>int</code> Index of the selected continuous variable.
    */
    private int GetContinuousVariable(int  Index) {
        int variable=((Integer)m_ContinuousVariablesVector.elementAt(Index)).intValue();
        return  variable;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for determining if a variable is discrete or not.
    *  @return <code>boolean</code> Result: true if the variable is discrete
    *          and false if not.
    */
    private boolean IsDiscreteVariable(int Variable) {
        int         i;
        boolean Found;
        Found=true;
        for(i=0;i<GetNumberOfContinuousVariables();i++) {
            if ( Variable == GetContinuousVariable(i) ) {
                return false;
            }
        }
        return true;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for determining if a variable has been configured.
    *  @return <code>int</code> True if the variable is configured and
    *          false if not.
    */
    private boolean IsConfiguredVariable(int Variable)   {
        int         i;
        boolean Found;
        Found=false;
        if ( DISCRETIZE_INDIVIDUALLY == GetMode() ) {
            for(i=0;i<m_Configuration.size()/4;i++) {
                if ( ((Integer)m_Configuration.elementAt(i*4)).intValue() == Variable ) {
                    if ( Found ) {
                        throw new SecurityException("Discretization::IsConfiguredVariable() - " + ErrorMessage[0] );
                    }
                    return true;
                }
            }
        }   else    {
            if ( DISCRETIZE_GLOBALLY != GetMode() ) {
                throw new SecurityException ("Discretization::IsConfiguredVariable() - " + ErrorMessage[1] );
            }   else    {
                for(i=0;i<m_Configuration.size()/2;i++) {
                    if ( Variable == ((Integer)m_Configuration.elementAt(i*2)).intValue() ) {
                        if ( Found ) {
                            throw new SecurityException("Discretization::IsConfiguredvariable() - " + ErrorMessage[0] );
                        }
                        Found = true;
                    }
                }
            }
        }
        return Found;
    }
    /*---------------------------------------------------------------*/ 
    /**
    *  Method for setting the mode: GLOBALLY or INDIVIDUALLY. You should use
    *  INDIVIDUALLY if you want to apply a specific algorithm on a specific
    *  variable.
    *  @return <code>void</code>
    */
    public void SetMode(int Mode)
    {
        if (    NO_DISCRETIZE           == Mode ||
                DISCRETIZE_GLOBALLY     == Mode ||
                DISCRETIZE_INDIVIDUALLY == Mode     ) {
                    m_Mode = Mode;
        } else {
            throw new SecurityException("Discretization::SetMode() - " + ErrorMessage[1]);
        }
        return;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining the configured mode.
    *  @return <code>int</code> Mode configured.
    */
    public int GetMode()
    {
        return m_Mode;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for configuring the discretization operation.
    *  @return <code>void</code>
    */
    public void SetOperation(int Operation)
    {
        if (    DISCRETIZE_INDIVIDUALLY ==  GetMode() ||
                DISCRETIZE_GLOBALLY     ==  GetMode()       ) {
                    
            if (    Operation == NORMAL_OPERATION ||
                    Operation == MASSIVE_OPERATION  )   {
                        m_Operation = Operation;
            }   else    {
                throw new SecurityException("Discretization::SetOperation() - " + ErrorMessage[2] );
            }
        } else {
            throw new SecurityException("Discretization::SetOperation() - " + ErrorMessage[1] );
        }
        return;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining the configured operation.
    *  @return <code>int</code> Operation configured.
    */
    public int GetOperation()
    {
        return  m_Operation;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for configuring an algorithm when the mode is GLOBALLY.
    *  @return <code>void</code>
    */
    public void SetAlgorithm(int Algorithm)
    {
        if ( DISCRETIZE_GLOBALLY == GetMode() ) {
            if (    WD_EDA                     ==  Algorithm   ) {
                    m_Algorithm = Algorithm;
            }   else    {
                throw new SecurityException("Discretization::SetAlgorithm() - " + ErrorMessage[3] );
            }
        }   else    {
            throw new SecurityException("Discretization::SetAlgorithm() - " + ErrorMessage[1]);
        }
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining the configured algorithm when the mode if 
    *  GLOBALLY.
    *  @return <code>int</code> Algorithm configured.
    */
    public int GetAlgorithm()
    {
        if ( DISCRETIZE_GLOBALLY == GetMode() ) {
            return m_Algorithm;
        }   else    {
            throw new SecurityException("Discretization::GetAlgorithm() - " + ErrorMessage[1] );
        }
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for configuring the algorithm when the mode es INDIVUALLY.
    *  @param <code> Variable </code> Algorithm applied to this Variable.
    *  @return <code>int</code> Algorithm configured for this variable.
    */
    public int GetAlgorithm(int Variable)
    {
        int i           ;
        int Algorithm   ;

        Algorithm = NO_ALGORITHM;
        if (    DISCRETIZE_INDIVIDUALLY == GetMode()    ) {
            for (i=0; i<m_Configuration.size()/4; i++) {
                if ( Variable == ((Integer)m_Configuration.elementAt(i*4)).intValue() ) {
                    Algorithm = ((Integer)m_Configuration.elementAt((i+1)*4-3)).intValue();
                }
            }
        } else {
            throw new SecurityException("Discretization::GetAlgorithm() - " + ErrorMessage[1] );
        }
        return Algorithm;
    }    

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for configuring options when the mode is GLOBALLY.
    *  @param <code> Options </code> Options for the algorithm.
    *  @return <code>void</code>
    */
    public void SetGlobalOptions(Vector Options)
    {
        if (    DISCRETIZE_GLOBALLY == GetMode()    ) {
          m_Options = Options;
        } else {
            throw new SecurityException("Discretization::SetGlobalOptions() - " + ErrorMessage[1] );
        }        
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining the number of intervals configured for the variable.
    *  @param <code> Variable </code> Variable selected to know the number of intervals.
    *  @return <code>int</code> Number of intervals selected for this variable.
    */
    public int GetIntervals(int Variable)
    {
        int i           ;        
        int Intervals   ;

        Intervals = -1;
        if ( IsDiscreteVariable(Variable) ) {
            throw new SecurityException("Discretization::GetIntervals() - " + ErrorMessage[8] + "-> " + Variable);
        } else {
            if ( DISCRETIZE_INDIVIDUALLY != GetMode()    &&    
                 DISCRETIZE_GLOBALLY     != GetMode()           ) {
                throw new SecurityException("Discretization::GetIntervals() - " + ErrorMessage[1] );
            } else {
                if ( DISCRETIZE_INDIVIDUALLY != GetMode() ) {
                    Intervals = ((Integer)m_Configuration.elementAt((Variable*2)+1)).intValue();
                } else {
                    for (i=0; i<m_Configuration.size()/4; i++) {
                        if ( Variable == ((Integer)m_Configuration.elementAt(i*4)).intValue() ) {
                            Intervals = ((Integer)m_Configuration.elementAt((i+1)*4-2)).intValue();
                        }
                    }
                }
            }
        }
        return Intervals;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining the options when the mode is GLOBALLY.
    *  @return <code>Vector</code> Options vector.
    */
    public Vector GetOptions()
    {
      Vector  Options;
      Options = new Vector();
      if ( DISCRETIZE_GLOBALLY != GetMode() ) {
        throw new SecurityException("Discretization::GetOptions() - " + ErrorMessage[1] );
      } else {
        Options = m_Options;
      }
      return Options;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining the options when the mode is INDIVIDUALLY.
    *  @param <code> Variable </code> Options of the algorithm applied to Variable
    *  @return <code>Vector</code> Options vector.
    */
    public Vector GetOptions(int Variable)
    {
        int i           ;        
        Vector Options  ;

        Options = new Vector();
        if ( IsDiscreteVariable(Variable) ) {
            throw new SecurityException("Discretization::GetOptions() - " + ErrorMessage[8] + "-> " + Variable);
        } else {
            if ( DISCRETIZE_INDIVIDUALLY != GetMode()    &&    
                 DISCRETIZE_GLOBALLY     != GetMode()           ) {
                throw new SecurityException("Discretization::GetOptions() - " + ErrorMessage[1] );
            } else {
                if ( DISCRETIZE_INDIVIDUALLY != GetMode() ) {
                  throw new SecurityException("Discretization::GetOptions() - " + ErrorMessage[9] );
                } else {
                    for (i=0; i<m_Configuration.size()/4; i++) {
                        if ( Variable == ((Integer)m_Configuration.elementAt(i*4)).intValue() ) {
                            Options = (Vector)m_Configuration.elementAt((i+1)*4-1);
                        }
                    }
                }
            }
        }
        return Options;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining information about the DBC loaded.
    *  @return <code>Vector</code> DBC File Information vector.
    */    
    public Vector getDBCInformation()
    {
      return m_DBCInformation;      
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for obtaining information about the DBC loaded.
    *  @return <code>Vector</code> DBC File Information vector.
    */    
    public void ConfigureGlobal(int Intervals,Vector Options)
    {                
        int i       ;
        int variable;
        for(i=0;i<GetNumberOfContinuousVariables();i++) {
            ConfigureGlobal(GetContinuousVariable(i),Intervals);
        }
        return;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for configuring the discretization when the mode is GLOBALLY.
    *  @param <code>Variable</code> Selected Variable to discretize.
    *  @param <code>Intervals</code> Number of intervals to discretize.
    *  @return <code>Vector</code> DBC File Information vector.
    */    
    public void ConfigureGlobal(int Variable, int Intervals)
    {
        if ( DISCRETIZE_GLOBALLY == GetMode() ) {
            if ( WD_EDA == GetAlgorithm() ) {
                if ( IsDiscreteVariable(Variable) ) {
                    throw new SecurityException("Discretization::ConfigureGlobal() - " + ErrorMessage[5] );
                }   else    {
                    if ( IsConfiguredVariable(Variable) ) {
                        throw new SecurityException("Discretization::ConfigureGlobal() - " + ErrorMessage[0] );
                    }   else    {
                        if ( Intervals <= 1 ) {
                            throw new SecurityException("Discretization::ConfigureGlobal() - " + ErrorMessage[6] );
                        }   else    {
                            m_Configuration.add( new Integer(Variable   ));
                            m_Configuration.add( new Integer(Intervals  ));
                        }
                    }
                }
            }   else    {
                throw new SecurityException("Discretization::ConfigureGlobal() - " + ErrorMessage[3] );
            }
        }
        
        return;
    }


    /*---------------------------------------------------------------*/ 
    /**
    *  Method for configuring the discretization when the mode is INVIDUALLY
    *  and the operation is MASSIVE.
    *  @param <code>Method</code> Selected algorithm to discretize.
    *  @param <code>Intervals</code> Number of intervals to discretize.
    *  @param <code>Options</code> Options of the algorithm.
    *  @return <code>void</code>
    */    
    public void ConfigureIndividual(int Method, int Intervals,Vector Options)
    {
        int i;
        if ( GetMode() != MASSIVE_OPERATION ) {
          throw new SecurityException("Discretization::ConfigureIndividual() - " + ErrorMessage[1]);
        } else {
          m_Configuration.clear();
          for(i=0;i<GetNumberOfContinuousVariables();i++) {
              ConfigureIndividual(GetContinuousVariable(i),Method,Intervals,Options);
          }
        }
        return;
    }

    /*---------------------------------------------------------------*/ 
    /**
    *  Method for configuring the discretization when the mode is INVIDUALLY
    *  and the operation is MASSIVE.
    *  @param <code>Variable</code> Selected algorithm to discretize.
    *  @param <code>Method</code> Selected algorithm to discretize.
    *  @param <code>Intervals</code> Number of intervals to discretize.
    *  @param <code>Options</code> Options of the algorithm.
    *  @return <code>void</code>
    */    
    public void ConfigureIndividual(int Variable,int Method, int Intervals,Vector Options)
    {
        int i;

        if ( IsDiscreteVariable(Variable)   ) {
                    throw new SecurityException("Discretization::ConfigureIndividual() - " + ErrorMessage[5] );
        }   else {
            if (    DISCRETIZE_INDIVIDUALLY != GetMode()    ) {
                throw new SecurityException("Discretization::ConfigureIndividual() - " + ErrorMessage[1]);
            } else {
                 if  (   EQUAL_FREQUENCY                  != Method &&
                         EQUAL_WIDTH                      != Method &&
                         UNSUPERVISED_MONOTHETIC_CONTRAST != Method &&
                         SUM_SQUARE_DIFFERENCES           != Method &&
                         K_MEANS                          != Method   ) {
                       throw new SecurityException("Discretization::ConfigureIndividual() - " + ErrorMessage[3]);   
                 } else {
                     if ( 1 >= Intervals){// || Intervals > GetNumberOfCases() ) {
                         throw new SecurityException("Discretization::ConfigureIndividual() - " + ErrorMessage[6]);
                     } else {
                         if ( IsConfiguredVariable(Variable) ) {
                             throw new SecurityException("Discretization::ConfigureIndividual() - " + ErrorMessage[0]);    
                         }
                         if ( UNSUPERVISED_MONOTHETIC_CONTRAST == Method ) {
                             if ( 2 != Intervals ) {
                                 throw new SecurityException("Discretization::ConfigureIndividual() - " + ErrorMessage[7]);
                             } else {
                                 m_Configuration.add(new Integer(Variable ));
                                 m_Configuration.add(new Integer(Method   ));
                                 m_Configuration.add(new Integer(Intervals));
                                 m_Configuration.add( Options              );
                             }
                         } else {
                             m_Configuration.add(new Integer(Variable ));
                             m_Configuration.add(new Integer(Method   ));
                             m_Configuration.add(new Integer(Intervals));
                             m_Configuration.add( Options              );                            
                         }
                      }
                 }
           }
        }    
        return;
    }    

    /**
    *  Method for clearing the parameters, necessary if you want to do
    *  several discretizations with the same Discretization object
    *
    */
    public void Clear()
    {
      m_Mode             = NO_DISCRETIZE ;
      m_Operation       = NO_OPERATION;
      m_Algorithm       = NO_ALGORITHM;
      m_Options.clear();
      m_Configuration.clear();
      return;
    }

    /**
    *  Method for loading dbc files.
    *  @param <code>Filename</code> DBC file name.
    *  @return <code>void</code>
    */
    public void LoadData(String Filename) throws ParseException, IOException
    {
        int j;

        FileInputStream File = new FileInputStream(Filename);
        m_Cases = new DataBaseCases(File);
        File.close();

        Node        node            ;
        NodeList    nodes           =   m_Cases.getVariables()              ;
      	Vector      vector          =   m_Cases.getRelationList()           ;
        Relation    relation        =   (Relation)vector.elementAt(0)       ;
        CaseListMem caselistmem     =   (CaseListMem)relation.getValues()   ;

        for (j=0 ; j< nodes.size()  ; j++) {
          node =(Node)(caselistmem.getVariables()).elementAt(j);
    	    if (node.getTypeOfVariable()==Node.CONTINUOUS) {
                m_ContinuousVariablesVector.add(new Integer(j));
          }
        }

        m_DBCInformation.add ( Filename                                     );
        m_DBCInformation.add ( new Integer(nodes.size())                    );
        m_DBCInformation.add ( new Integer(caselistmem.getNumberOfCases())  );
        for(j=0 ; j < nodes.size() ; j++ ) {
          node = (Node)(caselistmem.getVariables()).elementAt(j);
          m_DBCInformation.add( new String (node.getName()          ) );
          m_DBCInformation.add( new Integer(node.getTypeOfVariable()) );
        }

        return;
    }

    /**
    *  Method for loading a dbc file.
    *  @param <code>cases</code> a DBC object.
    *  @return <code>void</code>
    */
    public void LoadData(DataBaseCases cases){
        int j;

        
        m_Cases = cases.copy();
        
        m_ContinuousVariablesVector=new Vector();
        m_DBCInformation=new Vector();
        this.m_Configuration=new Vector();
        
        Node        node            ;
        NodeList    nodes           =   m_Cases.getVariables()              ;
      	Vector      vector          =   m_Cases.getRelationList()           ;
        Relation    relation        =   (Relation)vector.elementAt(0)       ;
        CaseListMem caselistmem     =   (CaseListMem)relation.getValues()   ;

        for (j=0 ; j< nodes.size()  ; j++) {
          node =(Node)(caselistmem.getVariables()).elementAt(j);
    	    if (node.getTypeOfVariable()==Node.CONTINUOUS) {
                m_ContinuousVariablesVector.add(new Integer(j));
          }
        }

        m_DBCInformation.add ( "No FileName"                                );
        m_DBCInformation.add ( new Integer(nodes.size())                    );
        m_DBCInformation.add ( new Integer(caselistmem.getNumberOfCases())  );
        for(j=0 ; j < nodes.size() ; j++ ) {
          node = (Node)(caselistmem.getVariables()).elementAt(j);
          m_DBCInformation.add( new String (node.getName()          ) );
          m_DBCInformation.add( new Integer(node.getTypeOfVariable()) );
        }

        
        return;
    }

    /**
    *  Method for saving dbc files.
    *  @param <code>Filename</code> DBC file name.
    *  @return <code>void</code>
    */
    public void SaveData(String Filename) throws IOException
    {
        FileWriter  File = new FileWriter(Filename);
        m_DiscretizedCasesDB.saveDataBase(File);
        File.close();
        return;
    }

    /*---------------------------------------------------------------*/
    /**
    *  Apply a discretization method to a DataBaseCases object. Replacing
    *  the missing values for new computed values.
    *  IMPORTANT: Class Variables should be always at the end of the set 
    *             of variables.
    *  @return <code>DataBaseCases</code> the new Data Base discretized.
    */
    public DataBaseCases apply() throws InvalidEditException,IOException
    {
        int     Algorithm               ;
        Vector  Options                 ;
        int     IntervalsNumber         ;
        Vector  CutPointsVector         ;
        Vector  IntervalsVector         ;
	Vector	ClassesVector		;
        Vector  ContinuousVector        ;
        Vector  CompleteContinuousVector;
        Vector  DiscretizedVector       ;
        double  ContinuousMatrix[][]    ;
        int     DiscretizedMatrix[][]   ;
        int     NumberOfVariables       ;
        int     NumberOfCases           ;
        int     i                       ;
        int     j                       ;
        String  Comment                 ;

        //Data strucutures to go through values and vars
        NodeList    nodes      ;
      	Vector      vector     ;
        Relation    relation   ;
        CaseListMem caselistmem;
      	Node        node       ;

      	//Data structures to go through values and vars for the new DataBase
      	Vector      vector2     ;
        Relation    relation2   ;
        CaseListMem caselistmem2;

      	// Iniatializing variables.
      	nodes       = m_Cases.getVariables();
      	vector      = m_Cases.getRelationList();
      	relation    = (Relation)vector.elementAt(0);
      	caselistmem = (CaseListMem)relation.getValues();

      	m_DiscretizedCasesDB = newDataBase();

      	vector2     = m_DiscretizedCasesDB.getRelationList();
      	relation2   = (Relation)vector2.elementAt(0);
        caselistmem2= (CaseListMem)relation2.getValues();

	ClassesVector		    = new Vector();
        ContinuousVector            = new Vector();
        CompleteContinuousVector    = new Vector();
        DiscretizedVector           = new Vector();

        CutPointsVector =   new Vector();
        IntervalsVector =   new Vector();
        Comment         =   null;
        Options         =   new Vector();

        // Required Checks
      	// 1st: Number of Cases
      	if ( 2 > caselistmem.getNumberOfCases() ) {
          System.out.println("ERROR: There is no enough cases for discretizing\n");
    	    System.exit(0);
        }

        System.out.println("------------------------------------------------------");
        System.out.println("----------- Beginning Discretization Process ---------");
        System.out.println("------------------------------------------------------");
        if ( DISCRETIZE_INDIVIDUALLY == GetMode() ) {
            for(i=0;i<nodes.size();i++) {
                node=(Node)(caselistmem.getVariables()).elementAt(i); 
                System.out.println("Vble:" + i +" : "+node.getName());
                if ( !IsDiscreteVariable(i) && IsConfiguredVariable(i) ) {
                    System.out.println("--> Discretized : Ok");
                    for(j=0;j<caselistmem.getNumberOfCases();j++) {
                        if ( node.undefValue() == caselistmem.getValue(j,i) ) {
                            CompleteContinuousVector.addElement(new Character('?'));
                        } else {
			    ClassesVector.addElement(new Integer((int)caselistmem.getValue(j,nodes.size()-1)));
                            ContinuousVector.addElement(new Double(caselistmem.getValue(j,i)));
                            CompleteContinuousVector.addElement(new Double(caselistmem.getValue(j,i)));                            
                        }
                    }
                    Algorithm       = GetAlgorithm(i);
                    IntervalsNumber = GetIntervals(i);
                    Options         = GetOptions  (i);
                    System.out.println("--> Algorithm   : " + Algorithm );
                    System.out.print  ("--> Intervals   : [" + IntervalsNumber + "]=" );
                    switch (Algorithm)
                    {
                        case    EQUAL_FREQUENCY :   
                                IntervalsVector = EqualFrequency(ContinuousVector, IntervalsNumber);
                                Comment="Equal Frequency ";
                                break;
                        case    EQUAL_WIDTH :   
                                IntervalsVector = EqualWidth(ContinuousVector, IntervalsNumber);
                                Comment="Equal Width ";
                                break;   

                        case    SUM_SQUARE_DIFFERENCES :
                                IntervalsVector = SumSquareDifferences(ContinuousVector, IntervalsNumber, Options );
                                Comment="Sum Squares Differences ";
                                break;

                        case    UNSUPERVISED_MONOTHETIC_CONTRAST :   
                                IntervalsVector = UnsupervisedMonotheticContrast(ContinuousVector, IntervalsNumber);
                                Comment="Unsupervised Montothetic Contrast ";
                                break;
                        case    K_MEANS :   
                                IntervalsVector = K_Means(ContinuousVector, IntervalsNumber );
                                Comment="K Means ";
                                break;                                
                        default                 :   
                                throw new SecurityException("Discretization::ApplyIt - ");
                    }
                    this.IntervalsVectorVar[0].addElement(node);
                    this.IntervalsVectorVar[1].addElement(IntervalsVector);
                    DiscretizedVector   = Discretize(CompleteContinuousVector,IntervalsVector);
                    for(j=0;j<caselistmem.getNumberOfCases();j++) {
                        if ( DiscretizedVector.elementAt(j) instanceof Character ) {
                          	node =(Node)(caselistmem.getVariables()).elementAt(0);
                            caselistmem2.setValue(j,i,-1.0);
                        } else {
                            int Value = ((Integer)DiscretizedVector.elementAt(j)).intValue();
                            caselistmem2.setValue(j,i,Value);
                        }
                    }
                    Comment=((Node)caselistmem2.getVariables().elementAt(i)).getComment()+Comment;
                    ((Node)caselistmem2.getVariables().elementAt(i)).setComment(Comment+" Discretized " + IntervalsVector);
                    
                    CutPointsVector.removeAllElements();
		    ClassesVector.removeAllElements();
                    ContinuousVector.removeAllElements();
                    CompleteContinuousVector.removeAllElements();                    
                    DiscretizedVector.removeAllElements();
                }
            }
        }   else {
            NumberOfVariables=0;
            for(i=0;i<nodes.size();i++) {
                System.out.println("Vble:" + i);
                if ( !IsDiscreteVariable(i) && IsConfiguredVariable(i) ) {
                    IntervalsVector.add(new Integer(GetIntervals(i)));
                    NumberOfVariables++;
                }
            }
            System.out.println("Numero Variables Configuradas-> " + NumberOfVariables);
            System.out.println("Intervalos-> " + IntervalsVector);
            NumberOfCases=caselistmem.getNumberOfCases();
            ContinuousMatrix = new double [NumberOfVariables][NumberOfCases];
            DiscretizedMatrix = new int   [NumberOfVariables][NumberOfCases];
            for(i=0;i<nodes.size();i++) {
                for(j=0;j<NumberOfCases;j++) {
                    ContinuousMatrix[i][j]=caselistmem.getValue(j,i);
                    DiscretizedMatrix[i][j]=-1;
                }
            }

            Algorithm=GetAlgorithm();
            switch(Algorithm)
            {
                case    WD_EDA:   
                        System.out.println("Discretization::Apply - Not yet implemented");
                default     :
                        System.out.println("Discretization::Apply - Algorithm not selected");
            }
            
            for(i=0;i<nodes.size();i++) {
                for(j=0;j<caselistmem.getNumberOfCases();j++) {
                    if ( !IsDiscreteVariable(i) && IsConfiguredVariable(i) ) {
                        caselistmem2.setValue(j,i,DiscretizedMatrix[i][j]);
                    }
                }
            }
        }
        System.out.println("------------------------------------------------------");
        System.out.println("----------- Discretization Process Finalized ---------");
        System.out.println("------------------------------------------------------");
        
        this.m_DiscretizedCasesDB.setNumberOfCases(this.m_Cases.getNumberOfCases());
        return m_DiscretizedCasesDB;
    }    

    /*---------------------------------------------------------------*/
    /**
     *
     *  This method is used to discretize a new DataBaseCases using the same intervals
     *  that a previous discretization process in another DataBaseCases object.Replacing
     *  the missing values for new computed values. 
     *  It is very useful to discretize a test data base cases:
     *
     *  /////////////////////////////////////////////////////////////////////////////
     *  For example: 
     *       Vector  Opts;
     *       Opts=new Vector();
     *       Discretization discretization = new Discretization();
     *       discretization.LoadData("train.dbc");
     *       discretization.SetMode(Discretization.DISCRETIZE_INDIVIDUALLY);
     *       discretization.SetOperation(MASSIVE_OPERATION);
     *	     discretization.ConfigureIndividual(Discretization.EQUAL_WIDTH,10,Opts);
     *	     discretization.apply();
     *       discretization.SaveData();  
     *
     *       //Now, another data base "test.dbc" is discretized using the same intervals:
     *       
     *       discretization.LoadData("test.dbc");
     *       discretization.SetMode(Discretization.DISCRETIZE_INDIVIDUALLY);
     *       discretization.SetOperation(MASSIVE_OPERATION);
     *	     discretization.ConfigureIndividual(Discretization.EQUAL_WIDTH,10,Opts);
     *	     discretization.applyAgain();
     *       discretization.SaveData();  
     *  /////////////////////////////////////////////////////////////////////////////
     *
     *  @return <code>DataBaseCases</code> the new Data Base discretized.
    */
    public DataBaseCases applyAgain() throws InvalidEditException,IOException
    {
        int     Algorithm               ;
        Vector  Options                 ;
        int     IntervalsNumber         ;
        Vector  CutPointsVector         ;
        Vector  IntervalsVector         ;
        Vector  ContinuousVector        ;
        Vector  CompleteContinuousVector;
        Vector  DiscretizedVector       ;
        double  ContinuousMatrix[][]    ;
        int     DiscretizedMatrix[][]   ;
        int     NumberOfVariables       ;
        int     NumberOfCases           ;
        int     i                       ;
        int     j                       ;
        String  Comment                 ;

        //Data strucutures to go through values and vars
        NodeList    nodes      ;
      	Vector      vector     ;
        Relation    relation   ;
        CaseListMem caselistmem;
      	Node        node       ;

      	//Data structures to go through values and vars for the new DataBase
      	Vector      vector2     ;
        Relation    relation2   ;
        CaseListMem caselistmem2;

      	// Iniatializing variables.
      	nodes       = m_Cases.getVariables();
      	vector      = m_Cases.getRelationList();
      	relation    = (Relation)vector.elementAt(0);
      	caselistmem = (CaseListMem)relation.getValues();

      	m_DiscretizedCasesDB = newDataBase();

      	vector2     = m_DiscretizedCasesDB.getRelationList();
      	relation2   = (Relation)vector2.elementAt(0);
        caselistmem2= (CaseListMem)relation2.getValues();

        ContinuousVector            = new Vector();
        CompleteContinuousVector    = new Vector();
        DiscretizedVector           = new Vector();

        CutPointsVector =   new Vector();
        IntervalsVector =   new Vector();
        Comment         =   null;
        Options         =   new Vector();

        // Required Checks
      	// 1st: Number of Cases
/*      	if ( 2 > caselistmem.getNumberOfCases() ) {
          System.out.println("ERROR: There is no enough cases for discretizing\n");
    	    System.exit(0);
        }
*/
/*        System.out.println("------------------------------------------------------");
        System.out.println("----------- Beginning Discretization Process ---------");
        System.out.println("------------------------------------------------------");
  */      if ( DISCRETIZE_INDIVIDUALLY == GetMode() ) {
            int cont=0;
            for(i=0;i<nodes.size();i++) {
                node=(Node)(caselistmem.getVariables()).elementAt(i);                
                //System.out.println("Vble:" + i);
                if ( !IsDiscreteVariable(i) && IsConfiguredVariable(i) ) {
                    //System.out.println("--> Discretized : Ok");
                    for(j=0;j<caselistmem.getNumberOfCases();j++) {
                        if ( node.undefValue() == caselistmem.getValue(j,i) ) {
                            CompleteContinuousVector.addElement(new Character('?'));
                        } else {
                            ContinuousVector.addElement(new Double(caselistmem.getValue(j,i)));
                            CompleteContinuousVector.addElement(new Double(caselistmem.getValue(j,i)));                            
                        }
                    }
                    Algorithm       = GetAlgorithm(i);
                    IntervalsNumber = GetIntervals(i);
                    Options         = GetOptions  (i);
                    //System.out.println("--> Algorithm   : " + Algorithm );
                    //System.out.print  ("--> Intervals   : [" + IntervalsNumber + "]=" );
                    boolean encontrado=false;
                    for (int a=0; a<this.IntervalsVectorVar[0].size(); a++){
                        if (((Node)this.IntervalsVectorVar[0].elementAt(a)).equals(node)){
                            IntervalsVector=(Vector)this.IntervalsVectorVar[1].elementAt(a);
                            encontrado=true;
                            break;
                        }
                    }
                    if (!encontrado){
                        System.out.println("Error in Discretization Error");
                        System.exit(0);
                    }
                    cont++;
                    //System.out.println(IntervalsVector);
                    DiscretizedVector   = Discretize(CompleteContinuousVector,IntervalsVector);
                    for(j=0;j<caselistmem.getNumberOfCases();j++) {
                        if ( DiscretizedVector.elementAt(j) instanceof Character ) {
                          	node =(Node)(caselistmem.getVariables()).elementAt(0);
                            caselistmem2.setValue(j,i,-1.0);
                        } else {
                            int Value = ((Integer)DiscretizedVector.elementAt(j)).intValue();
                            caselistmem2.setValue(j,i,Value);
                        }
                    }
                    Comment=((Node)caselistmem2.getVariables().elementAt(i)).getComment()+Comment;
                    ((Node)caselistmem2.getVariables().elementAt(i)).setComment(Comment+" Discretized " + IntervalsVector);
                    
                    CutPointsVector.removeAllElements();
                    ContinuousVector.removeAllElements();
                    CompleteContinuousVector.removeAllElements();                    
                    DiscretizedVector.removeAllElements();
                }
            }
        }   else {
            NumberOfVariables=0;
            for(i=0;i<nodes.size();i++) {
                //System.out.println("Vble:" + i);
                if ( !IsDiscreteVariable(i) && IsConfiguredVariable(i) ) {
                    IntervalsVector.add(new Integer(GetIntervals(i)));
                    NumberOfVariables++;
                }
            }
            //System.out.println("Numero Variables Configuradas-> " + NumberOfVariables);
            //System.out.println("Intervalos-> " + IntervalsVector);
            NumberOfCases=caselistmem.getNumberOfCases();
            ContinuousMatrix = new double [NumberOfVariables][NumberOfCases];
            DiscretizedMatrix = new int   [NumberOfVariables][NumberOfCases];
            for(i=0;i<nodes.size();i++) {
                for(j=0;j<NumberOfCases;j++) {
                    ContinuousMatrix[i][j]=caselistmem.getValue(j,i);
                    DiscretizedMatrix[i][j]=-1;
                }
            }

            Algorithm=GetAlgorithm();
            switch(Algorithm)
            {
                case    WD_EDA:   
                        System.out.println("Discretization::Apply - Not yet implemented");
                default     :
                        System.out.println("Discretization::Apply - Algorithm not selected");
            }
            
            for(i=0;i<nodes.size();i++) {
                for(j=0;j<caselistmem.getNumberOfCases();j++) {
                    if ( !IsDiscreteVariable(i) && IsConfiguredVariable(i) ) {
                        caselistmem2.setValue(j,i,DiscretizedMatrix[i][j]);
                    }
                }
            }
        }
/*        System.out.println("------------------------------------------------------");
        System.out.println("----------- Discretization Process Finalized ---------");
        System.out.println("------------------------------------------------------");
  */      this.m_DiscretizedCasesDB.setNumberOfCases(this.m_Cases.getNumberOfCases());
        return m_DiscretizedCasesDB;
    }    

    /*---------------------------------------------------------------*/ 
   /*
    * This method builds a new DataBaseCases object using the one is stored
    * in a member variable, with the particularity that all the variables 
    * will be FiniteStates  and the cases will be all initilizated to zero.
    * @return <code>DataBaseCases</code> the new Data Base with only FiniteStates vars.
    */
    private DataBaseCases newDataBase() throws InvalidEditException { 
	
      	//int             i               ;
        //int             j               ;
        //int             k               ;
        Vector          IntervalsVector = new Vector();

        NodeList        nodes           =   m_Cases.getVariables()              ; 
      	DataBaseCases   discretizedDB   =   new DataBaseCases()                 ; 
      	Vector          vector          =   m_Cases.getRelationList()           ;     
        Relation        relation        =   (Relation)vector.elementAt(0)       ;
        CaseListMem     caselistmem     =   (CaseListMem)relation.getValues()   ;
      	Node            node                                                    ;

      	discretizedDB.setName       (m_Cases.getName()        );
      	discretizedDB.setTitle      (m_Cases.getTitle()       );
      	discretizedDB.setComment    (m_Cases.getComment()     );
      	discretizedDB.setAuthor     (m_Cases.getAuthor()      );
      	discretizedDB.setWhoChanged (m_Cases.getWhoChanged()  );
      	discretizedDB.setWhenChanged(m_Cases.getWhenChanged() );
      	discretizedDB.setVersion    ((float)1.0               );

        // Obtenemos un vector de intervalos para cada variable.
        for(int i=0;i<GetNumberOfContinuousVariables();i++) {
            int ContVariable = ((Integer)m_ContinuousVariablesVector.elementAt(i)).intValue();
            if ( IsConfiguredVariable(ContVariable)) {
                IntervalsVector.add(new Integer(GetIntervals(ContVariable)));
            }
        }

        int cont=0;
        for (int j=0 ; j< nodes.size()  ; j++) {
          node =(Node)(caselistmem.getVariables()).elementAt(j);
    	    if (    node.getTypeOfVariable()==Node.CONTINUOUS   &&  IsConfiguredVariable(j)) {
    	        FiniteStates newnode= new FiniteStates(((Integer)IntervalsVector.elementAt(cont)).intValue());
                cont++;

                newnode.setName         (node.getName()+"D"         );
                newnode.setTitle        (node.getTitle()        );
                newnode.setComment      (node.getComment()      );
                newnode.setAxis         (node.getLowerAxis(),node.getHigherAxis());
                newnode.setExpanded     (node.getExpanded()     );
                newnode.setKindOfNode   (node.getKindOfNode()   );
                newnode.setMarked       (node.getMarked()       );
                newnode.setObserved     (node.getObserved()     );
                newnode.setPosX         (node.getPosX()         );
                newnode.setPosY         (node.getPosY()         );
                newnode.setPurpose      (node.getPurpose()      );
                newnode.setRelevance    (node.getRelevance()    );
                newnode.setVisited      (node.getVisited()      );

                discretizedDB.addNode(newnode);
          }   else    {
            		discretizedDB.addNode(node);
          }
        }
        
	  int value=0;
        ContinuousCaseListMem newcaselistmem = new ContinuousCaseListMem(discretizedDB.getVariables());
        ContinuousConfiguration newconf = new ContinuousConfiguration(newcaselistmem.getVariables());
        int[] putIndex=newcaselistmem.getIndexPutQuickly(newconf);
        
      	for (int i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
          Vector v = new Vector(nodes.size());
    	    for (int j=0; j < nodes.size(); j++) {
                node =(Node)(discretizedDB.getVariables()).elementAt(j);
                if ( node.getTypeOfVariable()==Node.CONTINUOUS  &&
                     !IsConfiguredVariable(j)                       ) {
                    v.add(new Double(caselistmem.getValue(i,j)));
                } else {
			  node=(Node)(caselistmem.getVariables()).elementAt(j);
			  if ( node.getTypeOfVariable()==Node.FINITE_STATES ) {
				value=(int)(caselistmem.getValue(i,j));
				v.add(new Integer(value));
			  }else {
	                  v.add(new Integer(0));
			  }
                }
           }
           newconf = new ContinuousConfiguration(newcaselistmem.getVariables(), v);
           newcaselistmem.putQuickly(newconf,putIndex);
      	}

        Relation newrelation = new Relation();
        newrelation.setActive   (relation.getActive()           );
        newrelation.setComment  (relation.getComment()          );
        newrelation.setKind     (relation.getKind()             );
        newrelation.setName     (relation.getName()             );
        newrelation.setValues   (newcaselistmem                 );
        newrelation.setVariables(discretizedDB.getVariables()   );

        discretizedDB.addRelation(newrelation);

        return discretizedDB;
    }    
    
    /*---------------------------------------------------------------*/ 
    /**
    * Equal Frecuency Interval discretization divides a continuous variable into <code>IntervalsNumber</code> bins
    * where (given <code>ContinuousVector</code>).
    * @param <code>ContinuousVector</code> Cases
    * @param <code>IntervalsNumber</code> Number of intervals of the continuous var will be discretizated
    * @return <code>Vector</code> Cut points vector.
    */
    public Vector EqualFrequency(Vector ContinuousVector,int IntervalsNumber)
    {
        int     i                   ;
        int     j                   ;
        double  Intervals   []      ;
        double  SortedValues[]      ;
        Vector  DiscretizedVector   ;
        Vector  IntervalsVector     ;
        int     DiscretizedValue    ;
        
        DiscretizedVector = new Vector();
        IntervalsVector   = new Vector();
        Intervals       =   new double[IntervalsNumber];
        SortedValues    =   new double[ContinuousVector.size()];
        
        for(i=0;i<ContinuousVector.size();i++) {
            SortedValues[i]=((Double)ContinuousVector.elementAt(i)).doubleValue();
        }
        
        java.util.Arrays.sort(SortedValues);
/*        
        for(i=0;i<IntervalsNumber;i++) {
            Intervals[i]=SortedValues[(int)Math.ceil((i*ContinuousVector.size())/IntervalsNumber)];
            IntervalsVector.add(new Double(Intervals[i]));
        }
 */
        int size=ContinuousVector.size();
        
        if (size==0){
            for(i=0;i<Intervals.length;i++) {
                IntervalsVector.add(new Double(0));
            }
            System.out.println(IntervalsVector);
            return IntervalsVector;
        }
        
        int ini=0;
        int interval=0;
        for(i=0;i<Intervals.length;i++) {
            int pos=(int)Math.ceil((interval*size)/IntervalsNumber);
            if ((ini+pos)>=SortedValues.length){
                for (j=i; j<Intervals.length; j++)
                    Intervals[j]=Intervals[i-1];
                break;
            }

            double val=SortedValues[ini+pos];
            if (i!=0 && val==Intervals[i-1]){
                j=ini+pos;
                while(j<SortedValues.length && Intervals[i-1]==SortedValues[j]) 
                    j++;
                if (j==SortedValues.length){
                    for (j=i; j<Intervals.length; j++)
                        Intervals[j]=Intervals[i-1];
                    break;
                }
                size=SortedValues.length-j;
                IntervalsNumber--;
                Intervals[i]=SortedValues[j];
                ini=j+1;
            }else{
                Intervals[i]=val;
                interval++;
            }
            IntervalsVector.add(new Double(Intervals[i]));
        }

        System.out.println(IntervalsVector);
        return IntervalsVector;
    }    

   /*---------------------------------------------------------------*/ 
   /**
    * Equal Width discretization divides continuous variables into <code>IntervalsNumber</code> bins
    * where (given <code>ContinuousVector</code> size) each bin have the same width
    * @param <code>ContinuousVector</code> Cases
    * @param <code>IntervalsNumber</code> Number of intervals of the continuous var will be discretizated
    * @return <code>Vector</code> Cut points vector.
    */    
    public Vector EqualWidth(Vector ContinuousVector,int IntervalsNumber)
    {

        int     i                   ;
        int     j                   ;
        double  Intervals   []      ;
        double  SortedValues[]      ;
        Vector  DiscretizedVector   ;
        Vector  IntervalsVector     ;
        int     DiscretizedValue    ;
        
        DiscretizedVector   =   new Vector();
        IntervalsVector     =   new Vector();
        Intervals           =   new double[IntervalsNumber];
        SortedValues        =   new double[ContinuousVector.size()];
        
        for(i=0;i<ContinuousVector.size();i++) {
            SortedValues[i]=((Double)ContinuousVector.elementAt(i)).doubleValue();
        }
        
        java.util.Arrays.sort(SortedValues);
        
        for(i=0;i<IntervalsNumber;i++) {
            Intervals[i]=SortedValues[0] + 
                            i*(SortedValues[ContinuousVector.size()-1]-SortedValues[0])/IntervalsNumber;
            IntervalsVector.add(new Double(Intervals[i]));
        }
        System.out.println(IntervalsVector);
        return IntervalsVector;
    } 
    
   /*---------------------------------------------------------------*/ 
   /**
    * sum square diferences discretization divides a continuous variable into <code>IntervalsNumber</code>, to 
    * choose those intervals, we calculate the sum of square differences for each intevals and get the max,
    * this is divided in two intervals using the average value; we continue until a number of intervals or 
    * the max sum of square differences were below a threshold.
    * @param <code>ContinuousVector</code> Cases
    * @param <code>IntervalsNumber</code> Number of intervals of the continuous var will be discretizated
    * @param <code>Options</code> Minimum value to stop the discretization.
    * @return <code>Vector</code> Cut points vector.
    */
     public Vector SumSquareDifferences (Vector continuousvector,int intervalsnumber,Vector Options) {

        int     i               ;
        int     j               ;
        int     t               ;
        int     u               ; //Iterators
        int     n               ; //actual intervals number
        boolean cont            ; //continue or not
        int     max             ; //maximum Sum square differences
        double  newinterval     ; //new interval to insert
        int     discretizedvalue; //a discretized value for a cont. value
        int     elements        ;
        double  sumsquare       ;
        double  avg             ;
        double  threshold       ;

        //Data strucutures to go through values
        Vector  discretizedvector   = new Vector();
        double  intervals    []     = new double[intervalsnumber];
        double  sortedvalues []     = new double[continuousvector.size()];
        double  differences  []     = new double [intervalsnumber];
        double  averages     []     = new double [intervalsnumber];

        Vector  IntervalsVector     = new Vector();

        threshold = ((Double)Options.elementAt(0)).doubleValue();

      	//Copy the values for each case of the var and sort them all
      	for(j=0;j<continuousvector.size();j++) {
            sortedvalues[j]=((Double)continuousvector.elementAt(j)).doubleValue();
        }
        java.util.Arrays.sort(sortedvalues);

        //start with one initial intervals
        n=1;
        
        //set the limits of the initial interval
        intervals[0]=sortedvalues[0];
        intervals[1]=sortedvalues[sortedvalues.length-1];
            
        //calculate de stop condition 
        cont= (n>=intervalsnumber)?false:true;
            
        //we continue until get the dessired number of intervals
        while (cont) {
            //Calculate the sum square differences
            for (t=0,j=0;j<n;j++) {
              sumsquare=0;
              avg=0;
              for (elements=0; t < sortedvalues.length && sortedvalues[t]<=intervals[j+1];t++) {
                sumsquare+=sortedvalues[t] * sortedvalues[t];
                avg+=sortedvalues[t];
                elements++;
              }
              averages[j]=avg/(double)elements;
              differences[j]=sumsquare-(elements*averages[j]*averages[j]);
            }
                
            //Search the interval with the maximum Sum square differences
            max=0;
            for (j=1;j<=n;j++)
            if (differences[j] > differences[max]) {
              max=j;
            }
            
            //The new interval is the average of the interval with the maximum Sum square differences
            newinterval=averages[max];
                
            //Insert the new interval
            for (t=0; intervals[t]<newinterval && t<=n; t++);
            for (u=intervalsnumber-1; u>t && t>=0; u--) intervals[u]=intervals[u-1];
            intervals[t]=newinterval;
            n++;
                
            //We see to continue or not
            if (n>=intervalsnumber) cont=false;
            else if (differences[max]<threshold) cont=false;
            else cont=true;
        } //end where
			     
        for (j=0;j<n;j++) 
            IntervalsVector.add(new Double(intervals[j]));
        System.out.println(IntervalsVector);            
        return IntervalsVector;

    }//End sumSquareDifferences discretization method    
    
   /*---------------------------------------------------------------*/ 
   /**
    * K-Means discretization divides continuous variables into <code>IntervalsNumber</code> bins
    * where (given <code>ContinuousVector</code> size) following this classical algorithm.
    * @param <code>ContinuousVector</code> Cases
    * @param <code>IntervalsNumber</code> Number of intervals of the continuous var will be discretizated
    * @return <code>Vector</code> Cut points vector.
    */    
    public Vector K_Means(Vector ContinuousVector,int IntervalsNumber)
    {
        int     i                       ;
        int     j                       ;
        int     RealIntervalsNumber     ;
        double  Intervals   []          ;
        double  Distance                ;
        double  MinimumDistance         ;
        double  ClusterCentroid         ;
        double  ClusterSize             ;
        double  Point                   ;
        double  SortedData  []          ;
        int     Centroid                ;
        int     NumCentroids            ;
        int     DataIndex               ;
        Vector  IntervalsVector         ;
        Vector  CentroidsVector         ;
        Vector  InitialCentroidsVector  ;
        Vector  DataLabelVector         ;
        int     DiscretizedValue        ;
        boolean Changes                 ;
        boolean Exist                   ;
        int     DifferentPoints         ;
        
        IntervalsVector         = new Vector();
        DataLabelVector         = new Vector();
        CentroidsVector         = new Vector();
        InitialCentroidsVector  = new Vector(IntervalsNumber);
        SortedData              = new double[ContinuousVector.size()];

        for(i=0;i<ContinuousVector.size();i++) {
          SortedData[i]=((Double)ContinuousVector.elementAt(i)).doubleValue();
        }
        java.util.Arrays.sort(SortedData);

        DifferentPoints=1;
        for(i=0;i<ContinuousVector.size()-1;i++) {
          if ( SortedData[i] != SortedData[i+1] ) {
            DifferentPoints++;
          }
        }

        if ( DifferentPoints <= IntervalsNumber ) {
          if ( 1 == DifferentPoints ) { 
               RealIntervalsNumber =0;
               System.out.println("Warning. Nodo has an only point");
               IntervalsVector.addElement(new Double(-Double.MAX_VALUE));
               for (int a=0; a<IntervalsNumber; a++){
                IntervalsVector.addElement(new Double(Double.MAX_VALUE));
               }
               return IntervalsVector;
              //throw new SecurityException("Discretization::IsConfiguredVariable() - " + ErrorMessage[10] + " -> Limit:" + DifferentPoints);
          } else {
            RealIntervalsNumber = DifferentPoints - 1;
            System.out.print(">>(Warning - New Intervals Number)=["+RealIntervalsNumber+"]=");
          }
        } else {
          RealIntervalsNumber = IntervalsNumber;
        }
        Intervals = new double[RealIntervalsNumber];

        NumCentroids=0;
        while ( NumCentroids < RealIntervalsNumber ) {
            Centroid=(int)(Math.random()*ContinuousVector.size())-1;
            while ( Centroid < 0 ) Centroid=(int)(Math.random()*ContinuousVector.size())-1;
            Exist=false;
            for(i=0;i<InitialCentroidsVector.size();i++) 
            {
              if ( SortedData[Centroid] == 
                   SortedData[((Integer)InitialCentroidsVector.elementAt(i)).intValue()] ) 
                    {
                      Exist=true;
                    }
            }
            if ( ! Exist ) {
                InitialCentroidsVector.add(new Integer(Centroid));
                NumCentroids++;
            }
        }

        for(i=0;i<RealIntervalsNumber;i++) {
            DataIndex=((Integer)InitialCentroidsVector.elementAt(i)).intValue();
            CentroidsVector.insertElementAt(new Double(SortedData[DataIndex]),i);
        }

        // Initialization of the labels of the data.
        for(i=0;i<ContinuousVector.size();i++) {
            DataLabelVector.insertElementAt(new Integer(0),i);
        }

        Changes=true;
        while ( Changes ) {
          Changes=false;
          for(i=0;i<ContinuousVector.size();i++) {
              Distance        =   0.0;
              MinimumDistance =   java.lang.Double.MAX_VALUE;

              // Calculation of the labels of the data.
              for(j=0;j<RealIntervalsNumber;j++) {
                  Distance=
                    Math.abs(((Double)CentroidsVector.elementAt(j)).doubleValue() - SortedData[i]);
                  if ( Distance < MinimumDistance ) {
                      DataLabelVector.setElementAt(new Integer(j),i);
                      MinimumDistance=Distance;
                  }
              }
          }           
          for(i=0;i<RealIntervalsNumber;i++) {
               ClusterSize=0.0;
               ClusterCentroid=0.0;
               for(j=0;j<ContinuousVector.size();j++) {
                   if ( ((Integer)DataLabelVector.elementAt(j)).intValue() == i ) {
                       ClusterCentroid+=SortedData[j];
                       ClusterSize++;
                   }
               }
               ClusterCentroid/=ClusterSize;
               if ( ClusterCentroid != ((Double)CentroidsVector.elementAt(i)).doubleValue() ) {
                   CentroidsVector.setElementAt(new Double(ClusterCentroid),i);
                   Changes=true;
               }
          }
        }

        IntervalsVector.add(new Double(SortedData[0]));
        for(i=0;i<ContinuousVector.size()-1;i++) {
            if (((Integer)DataLabelVector.elementAt(i)).intValue() != 
                ((Integer)DataLabelVector.elementAt(i+1)).intValue()      ) {
                Point= ( SortedData[i] + SortedData[i+1] ) / 2.0;
                IntervalsVector.add(new Double(Point));
            }
        }
        System.out.println(IntervalsVector);
        return IntervalsVector;
    }
    
   /*---------------------------------------------------------------*/ 
   /**
    * Unsupervised Monthetic Contrast divides continuos variables into binary partitions
    * where the partition has the maximum contrast.
    * @param <code>ContinuousVector</code> Cases
    * @param <code>IntervalsNumber</code> Number of intervals of the continuous var will be discretizated
    * @return <code>Vector</code> Cut points vector.
    */    
    public Vector UnsupervisedMonotheticContrast(Vector ContinuousVector,int IntervalsNumber)
    {
        int     i                   ;
        int     j                   ;
        int     k                   ;
        double  Intervals   []      ;
        double  SortedValues[]      ;
        Vector  IntervalsVector     ;
        int     DiscretizedValue    ;
        double  Contrast            ;
        double  BestContrast        ;
        double  CutPoint            ;
        double  BestCutPoint        ;
        double  A_Mean              ;
        double  B_Mean              ;
        double  A_Cardinal          ;
        double  B_Cardinal          ;
        
        IntervalsVector = new Vector();
        Intervals       =   new double[IntervalsNumber];
        SortedValues    =   new double[ContinuousVector.size()];
        
        for(i=0;i<ContinuousVector.size();i++) {
            SortedValues[i]=((Double)ContinuousVector.elementAt(i)).doubleValue();
        }
        
        java.util.Arrays.sort(SortedValues);
        
        BestCutPoint=SortedValues[0];
        BestContrast=0.0;
        for(i=0;i<ContinuousVector.size()-1;i++) {
            CutPoint=(SortedValues[i]+SortedValues[i+1])/2.0;
            
            A_Cardinal=i+1;
            B_Cardinal=(ContinuousVector.size()-(i+1));
            
            A_Mean=0.0;
            for(k=0;k<=i;k++) {
                A_Mean+=SortedValues[k];
            }
            A_Mean/=A_Cardinal;
            
            B_Mean=0.0;
            for(k=i+1;k<ContinuousVector.size();k++) {
                B_Mean+=SortedValues[k];
            }
            B_Mean/=B_Cardinal;
            
            Contrast=A_Cardinal*B_Cardinal*(A_Mean-B_Mean)*(A_Mean-B_Mean);
            Contrast/=(A_Cardinal+B_Cardinal);
            
            if ( Contrast > BestContrast ) {
                BestContrast=Contrast;
                BestCutPoint=CutPoint;
            }
        }
        
        Intervals[0]=SortedValues[0];
        Intervals[1]=BestCutPoint;
        IntervalsVector.add(new Double(Intervals[0]));
        IntervalsVector.add(new Double(Intervals[1]));

        System.out.println(IntervalsVector);
        return IntervalsVector;
    }    

   /*---------------------------------------------------------------*/ 
   /**
    * Discretize this method discretizes.
    * @param <code>ContinuousVector</code> Cases
    * @param <code>CutPointsVector</code> Points for discretizing.
    * @return <code>Vector</code> the new vector discretized.
    */    
    private Vector  Discretize(Vector ContinuousVector, Vector CutPointsVector)
    {
        int     i                   ;
        int     j                   ;
        Vector  DiscretizedVector   ;   
        int     DiscretizedValue    ;

        DiscretizedVector = new Vector();

        for(i=0;i<ContinuousVector.size();i++) {
            if ( ContinuousVector.elementAt(i) instanceof Character ) {
                DiscretizedVector.add( new Character('?') );
            } else {
                j=0;
                while ( ( j<CutPointsVector.size() ) && 
                        ( ((Double)ContinuousVector.elementAt(i)).doubleValue() > ((Double)CutPointsVector.elementAt(j)).doubleValue()) ) {
                    j++;
                }
                if (( j >= CutPointsVector.size() ) || 
                    ( ((Double)ContinuousVector.elementAt(i)).doubleValue() < ((Double)CutPointsVector.elementAt(j)).doubleValue() )) {
                    if (j==0)
                        j=1;
                    DiscretizedValue = j-1;
                }   else    {
                    DiscretizedValue = j;
                }
                DiscretizedVector.addElement(new Integer(DiscretizedValue));
            }
        }
        return DiscretizedVector;
    }

    /*
     *  This method return a copy of a Discretization object.
     */
    public Discretization copy(){
        Discretization disc=new Discretization();

        disc.discretizationMethod=this.discretizationMethod;

        disc.m_Mode=this.m_Mode;
        disc.m_Operation=this.m_Operation;
        disc.m_Algorithm=this.m_Algorithm;
        disc.m_Options=(Vector)this.m_Options.clone();
        disc.m_Configuration=(Vector)this.m_Configuration.clone();
        disc.m_ContinuousVariablesVector=(Vector)this.m_ContinuousVariablesVector.clone();
        disc.m_Cases=this.m_Cases.copy();
        disc.m_DiscretizedCasesDB=this.m_DiscretizedCasesDB.copy();
        disc.m_DBCInformation=(Vector)this.m_DBCInformation.clone();
        
        for (int i=0;i<this.IntervalsVectorVar[0].size(); i++){
            disc.IntervalsVectorVar[0].addElement(((Node)this.IntervalsVectorVar[0].elementAt(i)).copy());
            disc.IntervalsVectorVar[1].addElement((Vector)((Vector)this.IntervalsVectorVar[1].elementAt(i)).clone());
            
        }
        return disc;
    }
    
    /*
     *  This method return a copy of a Discretization object.
     */
    public Discretization copySerializing(){
        try{
            String objectFile=new String("discretize.x");
            File objectF = new File(objectFile);
            if ( objectF.exists() ) {
                objectF.delete();
            }
            FileOutputStream fos = new FileOutputStream( objectF);
            ObjectOutputStream oos = new ObjectOutputStream( fos );
            oos.writeObject( this );
            oos.close();
            
            FileInputStream fis = new FileInputStream( objectFile );
            ObjectInputStream ois = new ObjectInputStream( fis );
            Discretization retrieved = (Discretization) ois.readObject();
            ois.close();


            return retrieved;

        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    /*---------------------------------------------------------------*/ 
    /*---------------------------------------------------------------*/ 
    /**
    * For performing tests 
    */
    public static void main(String args[]) throws ParseException, IOException, InvalidEditException
    {
        Vector          myVector        ;
        Discretization  myDiscretization;

        if ( args.length == 0 ) {
          System.out.println("USAGE: <program> <input file.dbc> <output file.dbc> <algorithm> <intervals> <options>");
          System.out.println("\n<algorith> :");
          System.out.println("Algorith: 0 => EQUAL FREQUENCY                 ");
          System.out.println("Algorith: 1 => EQUAL WIDTH                     ");
          System.out.println("Algorith: 2 => SUM SQUARE DIFFERENCES          ");
          System.out.println("Algorith: 3 => UNSUPERVISED MONOTHETIC CONTRAST");
          System.out.println("Algorith: 4 => K MEANS                         ");
          System.out.println("\n<intervals> :");
          System.out.println("Integer > 1");        
          System.out.println("\n<options> :");
          System.out.println("Only with Sum Square Differences: Real > 0");        
        } else {
          if ( args.length != 4 && args.length != 5 ) {
            System.out.println("Ejecute el programa sin parametros y obtendra la ayuda");
            System.exit(0);
          }
          myVector = new Vector();
          if ( Integer.valueOf(args[2]).intValue() == 2 ) {
            myVector.add( new Double(Double.valueOf(args[4]).doubleValue() ));
          }          

          myDiscretization = new Discretization();
          myDiscretization.LoadData(args[0]);
          myDiscretization.SetMode(DISCRETIZE_INDIVIDUALLY);
          myDiscretization.SetOperation(MASSIVE_OPERATION);
          myDiscretization.ConfigureIndividual(Integer.valueOf(args[2]).intValue(),Integer.valueOf(args[3]).intValue(),myVector);

          myDiscretization.apply();

          myDiscretization.SaveData(args[1]);
        }
        return;
    }//End main 
}
//End Discretization class)


