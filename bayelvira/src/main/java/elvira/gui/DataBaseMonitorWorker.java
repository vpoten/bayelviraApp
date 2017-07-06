package elvira.gui;

import javax.swing.SwingUtilities;
import java.io.FileInputStream;
import java.io.File;
import java.util.Vector;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import java.io.FileWriter;
import java.awt.FontMetrics;

import elvira.parser.*;
import elvira.database.DataBaseCases;
import elvira.Bnet;
import elvira.Node;
import elvira.Elvira;
import elvira.learning.preprocessing.FilterMeasures;
import elvira.learning.preprocessing.Discretization;
import elvira.learning.preprocessing.Imputation;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.ConfusionMatrix;
import elvira.learning.classification.unsupervised.discrete.*;
import elvira.learning.classification.supervised.discrete.*;
import elvira.gui.DataBaseMonitor.InformationPane;


/**
 * Class used as a worker in a separate thread that enables the posibility of monitorizing
 * the progress of a long task
 *
 * @since 01/06/04
 * @version 1.17
 * @author Armañanzas, R.
 * @author ISG Group
 *
 */
public final class DataBaseMonitorWorker
{

/**
 * Constraints
 */
  public static int UNSUPERVISED_MODEL = 0;
  public static int SUPERVISED_MODEL = 1;
  public static int NAIVE_MODEL = 0;
  public static int TAN_MODEL = 1;
  public static int KDB_MODEL = 2;
  public static int SELECTIVE_WRAPPER_NB_MODEL = 3;
  public static int SEMI_WRAPPER_NB_MODEL = 4;

/**
 * Indicates if the total task has been done
 */
  private boolean taskEnded;

/**
 * Indicates the task the worker must do
 * <ul>
 * <li><code>0</code> - null
 * <li><code>1</code> - load a dbc file into memory
 * <li><code>2</code> - load a dbc file in Elvira and launch the learning dialog
 * <li><code>3</code> - ...
 * </ul>
 */
  private int invocationJob; // 0-null; 1-DBC loading; 2-Learning probabilist

/**
 * Parameters needed by the tasks. Note that the name and class of them will change depending
 * the work to be done
 */
  private Vector param;

/**
 * Used by the worker for stamping the actual task been doing
 */
  private String actualWork;

/**
 * Stores the dialog from which the worker has been created
 */
  private JDialog invocationDialog;

/**
 * Stores the object result of the task. It can be multiple types of objects, use the semantic of
 * the invocationJob to know the exact casting must be done to this.
 */
  private Object answer;

/**
 * Constructs a <code>null</code> worker
 */
  public DataBaseMonitorWorker()
  {
    taskEnded=true;
    invocationJob = 0;
    param = null;
    actualWork = "";
    invocationDialog = null;
    answer = null;
  }//end DataBaseMonitorWorker()

/**
 * Constructs a worker with any programmed task
 *
 * @param invoker the dialog which creates the worker
 */
  public DataBaseMonitorWorker(JDialog invoker)
  {
    taskEnded=true;
    invocationJob = 0;
    param = null;
    actualWork = "";
    invocationDialog = invoker;
    answer = null;
  }//end DataBaseMonitorWorker(JDialog)

/**
 * Construct a ready-to-work worker
 *
 * @param invoker the dialog which creates the worker
 * @param task the code of the task to be done
 * @param parameters the vector of the parameters for the task
 */
  public DataBaseMonitorWorker(JDialog invoker, int task, Vector parameters)
  {
    taskEnded = false; // We have a task to do
    invocationJob = task;
    param = parameters;
    actualWork = "Initializing";
    invocationDialog = invoker;
    answer = null;
  }//end DataBaseMonitorWorker(JDialog, int, Vector)

/**
 * Executes the task
 */
  public void go() throws Exception
  {
    final SwingWorker worker = new SwingWorker()
    {
      public Object construct()
      {
        return new ActualTask();
      }
    };
    worker.start();
  }//end go()

/**
 * Shows the state of the running task
 *
 * @return <code>true</code> if all the tasks has been yet done; <code>false</code> otherwise
 */
  public boolean done()
  {
    return taskEnded;
  }//end done()

/**
 * Shows the result of the sequence of made operations
 *
 * @return an object that can be from different types
 */
  public Object getResult()
  {
    return answer;
  }//end getResult()

/**
 * Shows the actual task the thread is been doing
 *
 * @return a string with a brief description of the work is being made
 */
  public String getActualWork()
  {
    return actualWork;
  }//end getActualWork()

/**
 * This is the nested class that the SwingWorker uses for doing the desired work
 */
  public class ActualTask
  {
    /**
     * Executes the real work depending on the flag <code>invocationJob</code> and indicate
     * the actual running task in the <code>actualWork</code> string
     */
    public ActualTask()
    { 
      switch(invocationJob)
      {
        case 0: break;
        case 1: // Only a dbc loading
        {
          FileInputStream f;
          try{
            actualWork = "Opening file";
            f = new FileInputStream((String) param.elementAt(0));
            actualWork = "Reading file";
            answer = new DataBaseCases(f);
            taskEnded=true;
          }
          catch (Exception exception)
          {
            Elvira.println("Error: "+ exception.toString());
            invocationDialog.dispose();
          }
          break;
        }//end case1
        case 2:
        {
          break;     
        }//end case2
        case 3: //Filter measures
        {
          FileInputStream f;
          try{
            actualWork = "Opening file";
            f = new FileInputStream((File) param.elementAt(0));
            actualWork = "Reading file";
            DataBaseCases source = new DataBaseCases(f);
            actualWork = "Initializing structures";
            FilterMeasures filter = new FilterMeasures(source);
            actualWork = "Computing filter measure";
            filter.executeFilter(((Integer)param.elementAt(1)).intValue());
            
            Vector filteredVector = filter.getNodesFiltered();
            Vector answer = new Vector(filteredVector.size()+6);
            answer.add(0, ((File)param.elementAt(0)).getPath());
            answer.add(1, 
            ((DataBaseMonitor)invocationDialog).getFilterMeasuresOptions()[((Integer)param.elementAt(1)).intValue()]);
            answer.add(2, new Integer(source.getNodeList().getNodes().size()).toString());
            answer.add(3, new Integer(source.getNumberOfCases()).toString());
            if (param.size() > 2) //save with cut
            {
              int numNodes = new Integer((String)param.elementAt(2)).intValue();
              File outDbc = new File((String)param.elementAt(3));
              answer.add(4, outDbc.getPath());
              actualWork = "Saving output file";
              if (numNodes == 0){
                if (((Integer)param.elementAt(1)).intValue() == 7)
                  numNodes = filter.saveCFSProyection(outDbc);
                else
                  numNodes = filter.saveDBCOptimalProyection(outDbc, ((Integer)param.elementAt(1)).intValue());
              }
              else
                filter.saveDBCProyection(numNodes, outDbc);
              answer.add(5,(new Integer(numNodes).toString()));
            }
            else{ 
              answer.add(4,"");
              answer.add(5,(new Integer(filteredVector.size()/2)).toString());
            }
            
            actualWork = "Preformating results";

            for (int i=0; i<filteredVector.size(); i=i+2)
            {
              answer.add(i+6, ((Node)filteredVector.get(i)).getName());
              answer.add(i+7, filteredVector.get(i+1));
            }
            
            actualWork = "Showing results";
            taskEnded=true;
            ((DataBaseMonitor)invocationDialog).displayFilterResults(answer);
            
          }catch (Exception exception)
            {
            Elvira.println("Error: "+ exception.toString()); invocationDialog.dispose();          
            }
          break;     
        }//end case3 - Filter measures
        case 4: //Massive discretization
        {
          try{
            actualWork = "Initializing structures";
            Discretization discrete = new Discretization();
            Vector options = new Vector(); options.add(new Double(0));
            int intervals = ((Integer)param.get(4)).intValue();           
            DataBaseCases outDiscrete = new DataBaseCases();
            actualWork = "Reading file";
            discrete.LoadData(((File)param.get(0)).getPath());
            actualWork = "Discretizing values";
            discrete.SetMode(Discretization.DISCRETIZE_INDIVIDUALLY);
            discrete.SetOperation(Discretization.MASSIVE_OPERATION);
            switch (((Integer)param.get(3)).intValue())
            {
              case 0:{ discrete.ConfigureIndividual(Discretization.EQUAL_FREQUENCY, intervals, options); break;} 
              case 1:{ discrete.ConfigureIndividual(Discretization.EQUAL_WIDTH, intervals, options); break;}
              case 2:{ discrete.ConfigureIndividual(Discretization.SUM_SQUARE_DIFFERENCES, intervals, options); break;}
              case 3:{ discrete.ConfigureIndividual(Discretization.UNSUPERVISED_MONOTHETIC_CONTRAST, intervals, options); break;}
              case 4:{ discrete.ConfigureIndividual(Discretization.K_MEANS, intervals, options); break;}
            }
            outDiscrete=discrete.apply();
            actualWork = "Writing file";
            outDiscrete.saveDataBase(new FileWriter((String)param.get(1)));
            taskEnded=true;
            //Show an option pane with a note
            ((DataBaseMonitor)invocationDialog).messageDiscretizationDone();
          }catch(Exception e){
            Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();          
            }
            break;
        }//end case 4 - Massive discretization
        case 5: //Load variable discretization data
        {
          try
          {
            actualWork = "Initializing structures";
            Discretization discrete = new Discretization();
            actualWork = "Reading file";
            discrete.LoadData(((File)param.get(0)).getPath());
            actualWork = "Displaying values";
            ((DataBaseMonitor)invocationDialog).tabDiscretizationInitializeNormalDiscretization(discrete);
            taskEnded=true;
          }catch(Exception e){ Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();}
          break;
        }
        case 6: //Normal discretization data
        {
          try
          {
            actualWork = "Initializing structures";
            Discretization discrete = (Discretization)param.elementAt(3);
            discrete.Clear();
            actualWork = "Discretizing values";
            discrete.SetMode(Discretization.DISCRETIZE_INDIVIDUALLY);
            discrete.SetOperation(Discretization.NORMAL_OPERATION);
            //recorrer individualmente cada variable y marcar los argumentos
            for (int i=4; i<param.size(); i=i+4)
            {
              discrete.ConfigureIndividual( ((Integer)param.elementAt(i)).intValue(),
                                                        ((Integer)param.elementAt(i+1)).intValue(), 
                                                        ((Integer)param.elementAt(i+2)).intValue(), 
                                                        (Vector)param.elementAt(i+3) );
            }
            DataBaseCases outDiscrete = new DataBaseCases();
            outDiscrete=discrete.apply();
            actualWork = "Writing file";
            outDiscrete.saveDataBase(new FileWriter((String)param.get(1)));
            taskEnded=true;
            //Show an option pane with a note
            ((DataBaseMonitor)invocationDialog).messageDiscretizationDone();
          }catch(Exception e){ Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();}
          break;
        }//end case 6 - Normal discretization
        case 7: //Imputation
        {
          FileInputStream f;
          FileWriter out;
          try
          {
            actualWork = "Initializing structures";
            Imputation imputation= new Imputation();
            actualWork = "Reading file";
            f = new FileInputStream((File) param.elementAt(0));
            DataBaseCases data = new DataBaseCases(f);
            actualWork = "Imputing values";
            switch(((Integer)param.elementAt(2)).intValue())
            {
              case 0: {imputation.zeroImputation(data); break;}
              case 1: {imputation.averageImputation(data); break;}
              case 2:
              {
                int noVar = data.getVariables().getNodes().size();
                int classVar = ((Integer)param.elementAt(3)).intValue();
                if ( classVar > noVar ) {
                  classVar = noVar;
                  ((DataBaseMonitor)invocationDialog).tabImputationClassNumberControl(classVar);
                }
                imputation.classConditionedMeanImputation(data, classVar-1);
                break;
              }
              case 3:
              {
                imputation.classificationTreeImputation(data, ((Integer)param.elementAt(3)).intValue());
                break;
              }
              case 4:
              {
                int noIter = ((Integer)param.elementAt(3)).intValue();
                if ( noIter < 0 ) {
                  noIter = 1;
                  ((DataBaseMonitor)invocationDialog).tabImputationIterationsNumberControl(noIter);
                }
                imputation.ITER_MPEImputation(data, noIter);
                break;
              }
              case 5:
              {
                imputation.INCR_MPEImputation(data);
                break;
              }

            }
            actualWork = "Writing outfile";
            out = new FileWriter((String)param.elementAt(1));
            data.saveDataBase(out);
            taskEnded=true;
            //Show an option pane with a note
            ((DataBaseMonitor)invocationDialog).messageImputationDone();
          }catch(Exception e){ Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();}
          break;
        }//end case 7 - Imputation
        case 8: //Clustering
        {
          FileInputStream f;
          try
          {
            actualWork = "Reading file";
            f = new FileInputStream((File) param.elementAt(0));
            DataBaseCases data = new DataBaseCases(f);
            actualWork = "Initializing structures";
            double score =0;
            Bnet resultNet = new Bnet();
            long time =0;
            Vector dataToShow = new Vector(); String comment="";
            for(int i=0; i<10;i++){dataToShow.addElement(null);} //Initialize
            dataToShow.add(0, ((File)param.elementAt(0)).getPath());
            dataToShow.add(1, new Integer(UNSUPERVISED_MODEL));
            actualWork = "Computing unsupervised model for clustering";
            switch (((Integer)param.elementAt(2)).intValue())
            {
              case 0:{
                NBayesMLEM clustering = new NBayesMLEM(data,((Integer)param.elementAt(1)).intValue());
                time = System.currentTimeMillis();
                score = clustering.learning(((Boolean)param.elementAt(4)).booleanValue());
                time = System.currentTimeMillis() - time;
                resultNet = clustering.getClassifier();
                dataToShow.add(2, new Integer(NAIVE_MODEL));
                comment="Naïve-bayes EM";
                break;}
              case 1:{
                NBayesMLEMMStart clustering = new NBayesMLEMMStart(data,((Integer)param.elementAt(1)).intValue());
                time = System.currentTimeMillis();
                score = clustering.learning(((Boolean)param.elementAt(4)).booleanValue(),
                                              ((Integer)param.elementAt(3)).intValue());
                time = System.currentTimeMillis() - time;
                resultNet = clustering.getClassifier();
                dataToShow.add(2, new Integer(NAIVE_MODEL));
                comment="Naïve-bayes EM-MultiStart "+ ((Integer)param.elementAt(3)).toString();
                break;}
            }
            actualWork = "Displaying data";
            //-Generate a not used name
            String name = ((File)param.elementAt(0)).getPath();
            name = name.substring(0, name.lastIndexOf(".dbc"))+"-clustering-";
            name = name + Elvira.getElviraFrame().getDesktopPane().getAllFrames().length +".elv";
            //---------
            dataToShow.add(3, new Long(time));
            dataToShow.add(4, param.elementAt(4)); //Laplace correction
            dataToShow.add(5, new Double(score));
            dataToShow.add(9, comment); dataToShow.add(10, name);
            ((DataBaseMonitor)invocationDialog).visualizeBnetGUI(resultNet, name);
            ((DataBaseMonitor)invocationDialog).displayClassifierPanel(dataToShow);
            ((DataBaseMonitor)invocationDialog).displayNaiveClassifier();
            taskEnded=true;
          }catch(Exception e){ Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();}
          break;
        }//end case 8 - Clustering
        case 9: //Supervised learning
        {
          FileInputStream f;
          try
          {
            actualWork = "Reading file";
            f = new FileInputStream((File) param.elementAt(0));
            DataBaseCases data = new DataBaseCases(f);
            actualWork = "Initializing structures";
            Bnet resultNet = new Bnet();
            long time =0;
            Vector dataToShow = new Vector(); String comment="";
            for(int i=0; i<10;i++){dataToShow.addElement(null);} //Initialize
            dataToShow.add(0, ((File)param.elementAt(0)).getPath());
            dataToShow.add(1, new Integer(SUPERVISED_MODEL));
            switch (((Integer)param.elementAt(1)).intValue())
            {
              case 0:{ //NB structure
                switch (((Integer)param.elementAt(2)).intValue())
                {
                  case 0:{  //All vars
                    actualWork = "Computing NB supervised model";
                    time = System.currentTimeMillis();
                    Naive_Bayes model = new Naive_Bayes(data,((Boolean)param.elementAt(3)).booleanValue());
                    model.train();                
                    resultNet = model.getClassifier();
                    comment="Naïve-Bayes supervised - all variables";
                    dataToShow.add(2, new Integer(NAIVE_MODEL));
                    break;}
                  case 1:{  //Selective
                    actualWork = "Computing SelectiveNB supervised model";
                    time = System.currentTimeMillis();
                    if (!((Boolean)param.elementAt(4)).booleanValue()) //Wrapper
                    {
                      if (!((Boolean)param.elementAt(6)).booleanValue()) //Greedy
                      {
                        WrapperSelectiveNaiveBayes model = new WrapperSelectiveNaiveBayes(data,((Boolean)param.elementAt(3)).booleanValue());
                        model.train();
                        resultNet = model.getClassifier();           
                        comment="Naïve-Bayes supervised - Selective wrapper substructure - Greedy learning";                
                        dataToShow.add(2, new Integer(SELECTIVE_WRAPPER_NB_MODEL));
                      }
                      else //UMDA
                      {                      
                      }
                    }//end Wrapper
                    else //Filter
                    {
                      if (!((Boolean)param.elementAt(5)).booleanValue()) // 99%
                      {                        
                      }
                      else // 95%
                      {                        
                      }
                    }//end Filter
                    break;}
                  case 2:{  //Semi
                    actualWork = "Computing SemiNB supervised model";
                    time = System.currentTimeMillis();
                    if (!((Boolean)param.elementAt(4)).booleanValue()) //Wrapper
                    {
                      if (!((Boolean)param.elementAt(6)).booleanValue()) //Greedy
                      {
                        WrapperSemiNaiveBayes model = new WrapperSemiNaiveBayes(data,((Boolean)param.elementAt(3)).booleanValue());
                        model.train();
                        resultNet = model.getClassifier();           
                        comment="Naïve-Bayes supervised - Semi wrapper substructure - Greedy learning";                
                        dataToShow.add(2, new Integer(SEMI_WRAPPER_NB_MODEL));                
                      }
                      else //UMDA
                      {                      
                      }
                    }//end Wrapper
                    else //Filter
                    {
                      if (!((Boolean)param.elementAt(5)).booleanValue()) // 99%
                      {                        
                      }
                      else // 95%
                      {                        
                      }
                    }//end Filter
                    break;}
                }
                time = System.currentTimeMillis() - time;
                break;}
              case 1:{ //TAN structure
                actualWork = "Computing TAN supervised model";
                CMutInfTAN model = new CMutInfTAN(data,((Boolean)param.elementAt(3)).booleanValue());
                time = System.currentTimeMillis();
                model.train();
                time = System.currentTimeMillis() - time;
                resultNet = model.getClassifier();
                //dataToShow.add(new String("TAN")); dataToShow.add(new String("supervised - all variables"));
                dataToShow.add(2, new Integer(TAN_MODEL));
                comment="TAN supervised - all variables";
                break;}
              case 2:{ //KDB structure
                actualWork = "Computing KDB supervised model";
                int maxParents = ((Integer)param.elementAt(7)).intValue();
                CMutInfKDB model = new CMutInfKDB(data,((Boolean)param.elementAt(3)).booleanValue(), maxParents);
                time = System.currentTimeMillis();
                model.train();
                time = System.currentTimeMillis() - time;
                resultNet = model.getClassifier();                              
                //dataToShow.add(new String("KDB")); dataToShow.add(new String("supervised - all variables - K="+maxParents));
                dataToShow.add(2, new Integer(KDB_MODEL));
                dataToShow.add(4, new Integer(maxParents)); //Real index will be 6th
                comment="KDB supervised - all variables - K=" + maxParents;
                break;}
            } 
            actualWork = "Displaying data";
            //-Generate a not used name
            String name = ((File)param.elementAt(0)).getPath();
            name = name.substring(0, name.lastIndexOf(".dbc"))+"-supervised-";
            name = name + Elvira.getElviraFrame().getDesktopPane().getAllFrames().length +".elv";
            //---------
            dataToShow.add(3, new Long(time));
            dataToShow.add(4, param.elementAt(3)); //Laplace correction
            dataToShow.add(9, comment); dataToShow.add(10, name);
            ((DataBaseMonitor)invocationDialog).visualizeBnetGUI(resultNet, name);
            ((DataBaseMonitor)invocationDialog).displayClassifierPanel(dataToShow);
            switch(((Integer)param.elementAt(1)).intValue())
            {
              case 0:{ ((DataBaseMonitor)invocationDialog).displayNaiveClassifier(); break;  }
              case 1:{ ((DataBaseMonitor)invocationDialog).displayTANClassifier(); break;  }
              case 2:{ ((DataBaseMonitor)invocationDialog).displayKDBClassifier(); break;  }
            }
            taskEnded=true;
          }catch(Exception e){ Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();}
          break;
        }//end case 9 - Supervised learning  
        case 10: //CrossValidate a classifier
        {
          actualWork = "Initializing structures";
          FileInputStream f;
          try
          {
            JInternalFrame frame = (JInternalFrame)param.elementAt(1);
            InformationPane info = (InformationPane)frame.getContentPane().getComponent(1);
            if (info.getModelType() == UNSUPERVISED_MODEL)
            { taskEnded=true;
              ((DataBaseMonitor)invocationDialog).messageNoSupervisedClassifierSelected();
            }
            else //Supervised model selected
            {
              actualWork = "Reading data file";
              f = new FileInputStream(info.getDataFile());
              DataBaseCases data = new DataBaseCases(f);
              int classvar = ((NetworkFrame)frame).getEditorPanel().getBayesNet().getNodeList().size()-1;
              ConfusionMatrix matrix = new ConfusionMatrix();
              actualWork = "Performing cross validation";
              switch (info.getStructure())
              {
                case 0: //Naive-model
                {
                  Naive_Bayes c = new Naive_Bayes();
                  c.setClassifier(((NetworkFrame)frame).getEditorPanel().getBayesNet());
                  ClassifierValidator cv = new ClassifierValidator(c, data, classvar);
                  matrix = cv.kFoldSumCrossValidation(((Integer)param.elementAt(2)).intValue());
                  break;
                }
                case 1: //TAN-model
                {
                  CMutInfTAN c = new CMutInfTAN();
                  c.setClassifier(((NetworkFrame)frame).getEditorPanel().getBayesNet());
                  ClassifierValidator cv = new ClassifierValidator(c, data, classvar);
                  matrix = cv.kFoldSumCrossValidation(((Integer)param.elementAt(2)).intValue());
                  break;
                }
                case 2: //KDB-model
                {
                CMutInfKDB c = new CMutInfKDB();
                c.setClassifier(((NetworkFrame)frame).getEditorPanel().getBayesNet());
                ClassifierValidator cv = new ClassifierValidator(c, data, classvar);
                matrix = cv.kFoldSumCrossValidation(((Integer)param.elementAt(2)).intValue());
                break;                                  
                }
                case 3: //Selective Wrapper NB
                {
                WrapperSelectiveNaiveBayes c = new WrapperSelectiveNaiveBayes();
                c.setClassifier(((NetworkFrame)frame).getEditorPanel().getBayesNet());
                ClassifierValidator cv = new ClassifierValidator(c, data, data.getVariables().size()-1);
                matrix = cv.kFoldSumCrossValidation(((Integer)param.elementAt(2)).intValue());
                break;                                  
                }
                case 4: //Semi Wrapper NB
                {
                WrapperSemiNaiveBayes c = new WrapperSemiNaiveBayes();
                c.setClassifier(((NetworkFrame)frame).getEditorPanel().getBayesNet());
                ClassifierValidator cv = new ClassifierValidator(c, data, data.getVariables().size()-1);
                matrix = cv.kFoldSumCrossValidation(((Integer)param.elementAt(2)).intValue());
                break;                                  
                }
              }
              actualWork = "Displaying results";
              taskEnded=true;
              ((DataBaseMonitor)invocationDialog).displayCVResults(info, matrix, 
                                        data.getVariables().elementAt(data.getVariables().size()-1));
            }                      
          }catch(Exception e){ Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();
                               e.printStackTrace();}
          break;          
        }//end case 10 - Crossvalidation
        case 11: //Categorize a new dbc
        {
          actualWork = "Initializing structures";
          FileInputStream f;
          try
          {
            JInternalFrame frame = (JInternalFrame)param.elementAt(1);
            InformationPane info = (InformationPane)frame.getContentPane().getComponent(1);
            if (info.getModelType() == UNSUPERVISED_MODEL)
            { taskEnded=true;
              ((DataBaseMonitor)invocationDialog).messageNoSupervisedClassifierSelected();
            }
            else //Supervised model selected
            {
              //Reading file
              actualWork = "Reading data files";
              f = new FileInputStream(info.getDataFile());
              DataBaseCases data = new DataBaseCases(f);              
              
              DiscreteClassifier c = null;
              switch (info.getStructure())
              {
                case 0: //Naive-model
                { c = new Naive_Bayes(data, info.isCorrected() ); break; }
                case 1: //TAN-model
                { c = new CMutInfTAN(data, info.isCorrected() ); break; }
                case 2: //KDB-model
                { c = new CMutInfKDB(data, info.isCorrected(), info.getMaxNoParents() ); break; }
                case 3: //Selective Wrapper NB-model
                { c = new WrapperSelectiveNaiveBayes(data, info.isCorrected(), info.getMaxNoParents() ); break; }
                case 4: //Semi Wrapper NB-model
                { c = new WrapperSemiNaiveBayes(data, info.isCorrected(), info.getMaxNoParents() ); break; }
              }
              actualWork = "Performing categorize process";
              c.setClassifier(((NetworkFrame)frame).getEditorPanel().getBayesNet());
              c.categorize((String)param.elementAt(2), (String)param.elementAt(2));
              
              taskEnded=true;
              ((DataBaseMonitor)invocationDialog).messageCategorizationDone();
            }                      
          }catch(Exception e){ Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();}
          break;          
        }//end case 11 - Categorize
        case 12: //Test the classifier
        {
          actualWork = "Initializing structures";
          FileInputStream f;
          try
          {
            JInternalFrame frame = (JInternalFrame)param.elementAt(1);
            InformationPane info = (InformationPane)frame.getContentPane().getComponent(1);
            if (info.getModelType() == UNSUPERVISED_MODEL)
            { taskEnded=true;
              ((DataBaseMonitor)invocationDialog).messageNoSupervisedClassifierSelected();
            }
            else //Supervised model selected
            {
              //Reading file
              actualWork = "Reading data files";
              f = new FileInputStream(info.getDataFile());
              DataBaseCases data = new DataBaseCases(f);
              f = new FileInputStream((String)param.elementAt(2));
              DataBaseCases test = new DataBaseCases(f);
              
              DiscreteClassifier c = null;
              switch (info.getStructure())
              {
                case 0: //Naive-model
                { c = new Naive_Bayes(data, info.isCorrected() ); break; }
                case 1: //TAN-model
                { c = new CMutInfTAN(data, info.isCorrected() ); break; }
                case 2: //KDB-model
                { c = new CMutInfKDB(data, info.isCorrected(), info.getMaxNoParents() ); break; }
                case 3: //Selective Wrapper NB-model
                { c = new WrapperSelectiveNaiveBayes(data, info.isCorrected(), info.getMaxNoParents() ); break; }
                case 4: //Semi Wrapper NB-model
                { c = new WrapperSemiNaiveBayes(data, info.isCorrected(), info.getMaxNoParents() ); break; }                
              }
              actualWork = "Performing test process";
              c.setClassifier(((NetworkFrame)frame).getEditorPanel().getBayesNet());
              double accuracy = c.test(test);
              
              taskEnded=true;
              ((DataBaseMonitor)invocationDialog).messageTestSupervisedDone(accuracy);                                        
            }                      
          }catch(Exception e){ Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();}
          break;          
        }//end case 12 - Test the accuracy        
        case 13: // Initialize the networkFrame for the restrictions
        {
          FileInputStream f;
          try{
            actualWork = "Opening file";
            f = new FileInputStream((File)param.elementAt(0));
            actualWork = "Reading file";
            DataBaseCases data = new DataBaseCases(f);
            NetworkFrame netLoaded = new NetworkFrame(((Bnet)data).getTitle());
            netLoaded.getEditorPanel().setBayesNet((Bnet)data);
            taskEnded=true;
            ((DataBaseMonitor)invocationDialog).tabFactorizationLaunchKnowledgeDialog(netLoaded);
          }
          catch (Exception e){ Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();}
          break;
        }//end case13 - Initialize the networkFrame for the restrictions
        case 14: // Factorization of joint probabilities
        {
          FileInputStream f;
          NetworkFrame netFrame;
          try{
            actualWork = "Initializing structures";
            if ((NetworkFrame)param.elementAt(1) == null )  //The NetworkFrame hasn't been previously initialized
            {
              actualWork = "Reading file";
              f = new FileInputStream((File)param.elementAt(0));
              DataBaseCases data = new DataBaseCases(f);
              netFrame = new NetworkFrame(((Bnet)data).getTitle());
              netFrame.getEditorPanel().setBayesNet((Bnet)data);
            }
            else netFrame = (NetworkFrame)param.elementAt(1);
            
            actualWork = "Configuring learning parameters";
            netFrame.getLearningPanel().setLearningMethod(((Integer)param.elementAt(2)).intValue());
            netFrame.getLearningPanel().setParameterMethod(((Integer)param.elementAt(3)).intValue());
            //if (((Integer)param.elementAt(2)).intValue() == 2 || 
            netFrame.getLearningPanel().setMetric(((Integer)param.elementAt(4)).intValue());
            netFrame.getLearningPanel().setParameters((Vector)param.elementAt(5));

            actualWork = "Learning the bayesian structure";
            Bnet b = netFrame.learn(netFrame.getLearningPanel().getLearningMethod(), 
                                    netFrame.getLearningPanel().getParameters());

            netFrame.getEditorPanel().setBayesNet(b);

            actualWork = "Displaying data";
            //-Generate a not used name
            String name = ((File)param.elementAt(0)).getName();
            name = name.substring(0, name.lastIndexOf(".dbc"))+"-learnt-";
            name = name + Elvira.getElviraFrame().getDesktopPane().getAllFrames().length +".elv";
            //---------
            ((DataBaseMonitor)invocationDialog).displayLearntBnet(netFrame, name);
            Elvira.getElviraFrame().setTitle("Elvira - "+ name);
            taskEnded=true;
          }
          catch (Exception e){ Elvira.println("Error: "+ e.toString()); invocationDialog.dispose();}
          break;
        }//end case14 - Factorization of joint probabilities

      }//end switch

    }
  }//end ActualTask

   public abstract class SwingWorker {
      private Object value;  // see getValue(), setValue()
      private Thread thread;

      /** 
       * Class to maintain reference to current worker thread
       * under separate synchronization control.
       */
      private class ThreadVar {
          private Thread thread;
          ThreadVar(Thread t) { thread = t; }
          synchronized Thread get() { return thread; }
          synchronized void clear() { thread = null; }
      }

      private ThreadVar threadVar;

      /** 
       * Get the value produced by the worker thread, or null if it 
       * hasn't been constructed yet.
       */
      protected synchronized Object getValue() { 
          return value; 
      }

      /** 
       * Set the value produced by worker thread
       */
      private synchronized void setValue(Object x) { 
          value = x; 
      }

      /** 
       * Compute the value to be returned by the <code>get</code> method. 
       */
      public abstract Object construct() throws Exception;

      /**
       * Called on the event dispatching thread (not on the worker thread)
       * after the <code>construct</code> method has returned.
       */
      public void finished() {
      }

      /**
       * A new method that interrupts the worker thread.  Call this method
       * to force the worker to stop what it's doing.
       */
      public void interrupt() {
          Thread t = threadVar.get();
          if (t != null) {
              t.interrupt();
          }
          threadVar.clear();
      }

      /**
       * Return the value created by the <code>construct</code> method.  
       * Returns null if either the constructing thread or the current
       * thread was interrupted before a value was produced.
       * 
       * @return the value created by the <code>construct</code> method
       */
      public Object get() {
          while (true) {
              Thread t = threadVar.get();
              if (t == null) {
                  return getValue();
              }
              try {
                  t.join();
              }
              catch (InterruptedException e) {
                  Thread.currentThread().interrupt(); // propagate
                  return null;
              }
          }
      }

      /**
       * Start a thread that will call the <code>construct</code> method
       * and then exit.
       */
      public SwingWorker() {
          final Runnable doFinished = new Runnable() {
             public void run() { finished(); }
          };

          Runnable doConstruct = new Runnable() {
              public void run() {
                  try {
                      setValue(construct());
                  }
                  catch (Exception e){}
                  finally {
                      threadVar.clear();
                  }

                  SwingUtilities.invokeLater(doFinished);
              }
          };

          Thread t = new Thread(doConstruct);
          threadVar = new ThreadVar(t);
      }

      /**
       * Start the worker thread.
       */
      public void start() {
          Thread t = threadVar.get();
          if (t != null) {
              t.start();
          }
      }
  }//end class SwingWorker
 
}//end DataBaseMonitorWorker
