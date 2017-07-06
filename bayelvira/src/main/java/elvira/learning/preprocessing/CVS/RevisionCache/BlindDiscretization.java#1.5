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

public class BlindDiscretization 
{

  private Vector  m_Politic;

  public BlindDiscretization() 
  {
    m_Politic = new Vector();
  }

  public boolean  isConfigured(int Variable) 
  { 
    int     content   ;
    boolean configured;

    if ( Variable > m_Politic.size()-1 ) {
      configured = false;
    } else {
      content = ((Integer)((Vector)m_Politic.elementAt(0)).elementAt(Variable)).intValue();
      if ( -1 == content ) { 
        configured = false;
      } else {
        configured = true;
      }
    }
    return configured;
  }

  public  int getIntervals(int Variable)
  {
    int value;
    int content;
    if ( isConfigured(Variable) ) { 
      value = ((Vector)m_Politic.elementAt(Variable+1)).size()+1;
    } else {
      value = -1;
    }
    return value;
  }

  public double getVariableBound(int Variable,int bound)
  {
    double  boundValue;
    boundValue = ((Double)((Vector)m_Politic.elementAt(Variable+1)).elementAt(bound)).doubleValue();
    return boundValue;
  }

  private void Discretize(String InputFilename, String OutputFilename) 
    throws ParseException, IOException, InvalidEditException
  {
    Vector          Intervals ;
    FileInputStream InputFile ;
    DataBaseCases   Cases     ;
   	int             i         ;
    int             j         ;
    int             k         ;

    // Variables for report
    FileOutputStream      myFOS           ;
    BufferedOutputStream  myBOS           ;
    DataOutputStream      myDOS           ;    
    int                   continuous      ;
    int                   finite_states   ;
    int                   missingValues[] ;
    int                   values[]        ;

    myFOS = new FileOutputStream("Discretization-report.info");
    myBOS = new BufferedOutputStream(myFOS);
    myDOS = new DataOutputStream(myBOS);
    continuous    = 0 ;
    finite_states = 0 ;

    // 1st STEP: Loading DATA ...
    InputFile = new FileInputStream(InputFilename);
    Cases = new DataBaseCases(InputFile);
    InputFile.close();

    myDOS.writeChars("\n------------------------------------------------------");
    myDOS.writeChars("\n------------ Discretization REPORT -------------------");    
    myDOS.writeChars("\n------------------------------------------------------");

    // 2nd STEP: Creating new discretized database
    NodeList        nodes           =   Cases.getVariables()              ; 
  	DataBaseCases   discretizedDB   =   new DataBaseCases()               ; 
   	Vector          vector          =   Cases.getRelationList()           ;     
    Relation        relation        =   (Relation)vector.elementAt(0)     ;
    CaseListMem     caselistmem     =   (CaseListMem)relation.getValues() ;
  	Node            node                                                  ;

   	discretizedDB.setName       (Cases.getName()        );
   	discretizedDB.setTitle      (Cases.getTitle()       );
   	discretizedDB.setComment    (Cases.getComment()     );
   	discretizedDB.setAuthor     (Cases.getAuthor()      );
   	discretizedDB.setWhoChanged (Cases.getWhoChanged()  );
   	discretizedDB.setWhenChanged(Cases.getWhenChanged() );
   	discretizedDB.setVersion    ((float)1.0               );

    for (j=0 ; j< nodes.size()  ; j++) {
      node =(Node)(caselistmem.getVariables()).elementAt(j);
      if (    node.getTypeOfVariable()==Node.CONTINUOUS   &&
              isConfigured(j)                                 ) {
        continuous++;
        FiniteStates newnode= new FiniteStates(getIntervals(j));

        newnode.setName         (node.getName()         );
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
        if ( node.getTypeOfVariable()==Node.CONTINUOUS ) {
          continuous++;
        } else {
          finite_states++;
        }        
        discretizedDB.addNode(node);
      }
    }

    int value=0;
    ContinuousCaseListMem newcaselistmem = new ContinuousCaseListMem(discretizedDB.getVariables());
    ContinuousConfiguration newconf = new ContinuousConfiguration();
  	for (i=0 ; i< caselistmem.getNumberOfCases() ; i++) {
      Vector v = new Vector(nodes.size());
      for (j=0; j < nodes.size(); j++) {
        node =(Node)(discretizedDB.getVariables()).elementAt(j);
        if ( node.getTypeOfVariable()==Node.CONTINUOUS  ) {
          v.add(new Double(caselistmem.getValue(i,j)));
        } else {
          node=(Node)(caselistmem.getVariables()).elementAt(j);
          if ( node.getTypeOfVariable()==Node.FINITE_STATES ) {
            value=(int)(caselistmem.getValue(i,j));
            v.add(new Integer(value));
          } else { 
            v.add(new Integer(0));
          }
        }
      }
      newconf = new ContinuousConfiguration(newcaselistmem.getVariables(), v);
      newcaselistmem.put(newconf);
   	}

    Relation newrelation = new Relation();
    newrelation.setActive   (relation.getActive()           );
    newrelation.setComment  (relation.getComment()          );
    newrelation.setKind     (relation.getKind()             );
    newrelation.setName     (relation.getName()             );
    newrelation.setValues   (newcaselistmem                 );
    newrelation.setVariables(discretizedDB.getVariables()   );

    discretizedDB.addRelation(newrelation);

    myDOS.writeChars("\n\n*****Section: GENERAL");
    myDOS.writeChars("\n\tName     : " + InputFilename);    
    myDOS.writeChars("\n\tVariables: " + nodes.size());     
    myDOS.writeChars("\n\t -> Continuous    = ");
    myDOS.writeChars(String.valueOf(continuous));
    myDOS.writeChars("\n\t -> Finite States = ");
    myDOS.writeChars(String.valueOf(finite_states));
        
    // 3rd STEP: Discretize database
    myDOS.writeChars("\n\n*****Section: DETAILS\n");    
    Vector        vector2     = discretizedDB.getRelationList();
    Relation      relation2   = (Relation)vector2.elementAt(0)      ;
    CaseListMem   caselistmem2= (CaseListMem)relation2.getValues()  ;

    for (i=0; i< nodes.size(); i++) {
      node =(Node)(caselistmem.getVariables()).elementAt(i);
      int NoC = caselistmem.getNumberOfCases();
      
      if ( node.getTypeOfVariable()==Node.CONTINUOUS &&
           isConfigured(i)                                ) {
        missingValues = new int[nodes.size()];
        values = new int[getIntervals(i)];

        for(k=0;k<nodes.size();k++) {
          missingValues[k]=0;
        }
        for(k=0;k<getIntervals(i);k++) {
          values[k]=0;
        }

        for(j=0;j<NoC;j++) {
          if ( node.undefValue() == caselistmem.getValue(j,i) ) {
             caselistmem2.setValue(j,i,-1.0);
             missingValues[i]++;
          } else {
             int      DiscretizedValue;
             double   Value;
             Value=caselistmem.getValue(j,i);
             k=0;
             while ( ( k < getIntervals(i)-1 ) && 
                     ( Value >= ( getVariableBound(i,k) ))) {
              k++;
             }
             DiscretizedValue=0;
             if ( k >= getIntervals(i)-1 ) {
                 DiscretizedValue = getIntervals(i)-1;
             } else {
               if ( Value <= getVariableBound(i,k) ) {
                 DiscretizedValue = k;
              }  
             }
             caselistmem2.setValue(j,i,DiscretizedValue);  
             values[DiscretizedValue]++;
          }
          
        }
        myDOS.writeChars("\n\tVariable : " + node.getName());    
        myDOS.writeChars("\n        *Continuous => Finite States");
        myDOS.writeChars("\n        *Missing Values :" + String.valueOf(missingValues[i]));       
        for(int z=0;z<getIntervals(i);z++) {
          myDOS.writeChars("\n          -> State " + z + " : " );
          myDOS.writeChars(String.valueOf(values[z]));
        }
     } else { 
       myDOS.writeChars("\n\tVariable : " + node.getName());    
       if ( node.getTypeOfVariable() == node.CONTINUOUS ) {
        myDOS.writeChars("\n        *Continous     (ORIGINAL)");
       } else { 
        myDOS.writeChars("\n        *Finite States (ORIGINAL)");
       }
     }
    }
    FileWriter  OutputFile = new FileWriter(OutputFilename);
    discretizedDB.saveDataBase(OutputFile);
    OutputFile.close();                

    myDOS.close();
    myBOS.close();
    myFOS.close();
  return;
  }

  public void loadDiscretizationPolitic(String Filename) 
    throws FileNotFoundException, IOException
  {
    BufferedReader  inFile      ;
    String          Line        ;
    String          Token       ;
    Vector          VariableFlag;
    Vector          CutPoints   ;
    int             LineNumber  ;
    int             TokenCount  ;
    StringTokenizer STEngine    ;

    VariableFlag  = new Vector();
    CutPoints     = new Vector();

    inFile = new BufferedReader(new FileReader(Filename));
    Line = inFile.readLine();
    LineNumber =0;
    m_Politic.add(VariableFlag);
    while ( Line != null ) {
      STEngine = new StringTokenizer(Line," :");
      LineNumber++;
      TokenCount=0;
      while (STEngine.hasMoreElements()) {
        Token = STEngine.nextToken();
        if ( TokenCount == 0 ) {
          if ( Token.equalsIgnoreCase("D") ) {
            ((Vector)m_Politic.elementAt(0)).add(new Integer(LineNumber));
          } else { 
            ((Vector)m_Politic.elementAt(0)).add(new Integer(-1));
            CutPoints.add(new Double(0.0));
          }
        } else { 
          CutPoints.add(new Double(Double.parseDouble(Token)));
        }
        TokenCount++;
      }
      m_Politic.add(CutPoints);
      Line = inFile.readLine();
      CutPoints = new Vector();
    }
    inFile.close();
    return;
  }

  public static void main(String[] args) throws ParseException, IOException, InvalidEditException
  {
    String  InputFilename   ;
    String  OutputFilename  ;
    String  PoliticFilename ;
    int     i               ;
    
    BlindDiscretization myBlindDiscretization = new BlindDiscretization();
    
    if ( args.length != 3 ) {
      System.out.println("USAGE: <program> <input file.dbc> <output file.dbc> <politic.dat> ");
    } 
    else 
    {
      InputFilename   = args[0];
      OutputFilename  = args[1];
      PoliticFilename = args[2];

      System.out.print("Discretizing and Reporting ...\n");
      myBlindDiscretization.loadDiscretizationPolitic(PoliticFilename);
      myBlindDiscretization.Discretize(InputFilename,OutputFilename);  
      System.out.println("Blind Discretization...Ok.\n");
    }    
  }
}
