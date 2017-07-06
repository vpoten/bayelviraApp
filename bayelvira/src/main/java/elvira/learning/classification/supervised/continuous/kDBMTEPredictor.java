package elvira.learning.classification.supervised.continuous;

import elvira.Elvira;
import java.util.*;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.potential.*;

import elvira.*;
import java.util.Enumeration;
import java.util.Vector;
import elvira.database.*;
import elvira.learning.preprocessing.ProjectDBC;
import elvira.learning.MTELearning;
import elvira.inference.elimination.VariableElimination;
import elvira.inference.clustering.MTESimplePenniless;

/*
 * Implements a kDB predictor (classifier with continuous class variable) where
 * all the variables, including the class, are of class MTE. The model assumes 
 * that the joint distribution is MTE.
 *  
 * @author Antonio Fernandez Alvarez (afalvarez@ual.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 * 
 * @since 16/10/2007
 */

public class kDBMTEPredictor{
    
    /**
     * Variables for which the predictor is defined.
     */
    NodeList variables;
    
    /**
     * The  of the class variable in list <code>variables</code>.
     */
    int classVariable;
    
    /**
     * The network that defined the predictor.
     */
    Bnet net;
       

    /**
     * Creates a kDBMTEPredictor from a database (based on Sahami's algorithm 
     * proposed in 1996)
     *
     * @param db the DataBaseCases from which the predictor will be constructed.
     * @param cv an integer indicating the index (column) of the class variable in the database. 
     * The first column is labeled as 0.
     * @param intervals the number of intervals into which the domain of the continuous
     * variables will be split.
     * @param k is the maximum number of predictor variables as parents.
     
     */
    
    public kDBMTEPredictor(DataBaseCases db, int cv, int intervals, int k) {
            
        MTELearning learningObject;
        ContinuousProbabilityTree P_C, P_Xj_C, P_Xi_C, P_Xi_Xj_C, t;
        PotentialContinuousPT pot;
        Relation rel;
        Vector netRL;
        NodeList parents, relationVariables;        
        Node classVar, childVar;
        Link l;
        LinkList completell;
        Graph g;
        int i,j;
        
        Vector vCMI=new Vector();
        double info,value;
              
        NaiveMTEPredictor model = new NaiveMTEPredictor(db, cv, intervals); 
        net=model.net;
        
        variables = db.getVariables();
        classVariable = cv;
        classVar = variables.elementAt(cv);

        learningObject= new MTELearning(db);
        
        System.out.println("------------------------------------------------------------------------");
        System.out.println("Name of DataBaseCase: " + db.getName());
        System.out.println("Class variable: " + classVar.getName());
        System.out.println("Number of variables: " + variables.size());
        System.out.print("Names of variables: "); variables.printNames(); 
        System.out.print(k + "-DB\n");             
        //--------------------------------------------------------------------------------------------
        
        //Compute the mutual information I(Xi,C)
        Vector vMI=model.getListOfMututlinformation();
        Vector vOrderedMI= new Vector(this.OrderedIndexByMutualInformation(vMI));
        
        //--------------------------------------------------------------------------------------------
        //Compute the conditional mutual information
        System.out.println("\nMaking the complete graph with the feature variables ... ");
        completell=CompleteLinkList(variables);     
        
        System.out.println("\nEstimating the conditional mutual information " +
                           "for each link...\n");
        for (i=0;i<completell.size();i++){
         
          //Variable C
          parents = new NodeList();
          parents.insertNode(classVar);
          
          //Learning p(C)
          P_C=learningObject.learnConditional(classVar,new NodeList(),db,intervals,4);  
         
          //Learning p(Xi|C)    
          childVar=completell.elementAt(i).getHead();//Variable Xi       
          
          //Learning p(Xi|C) 
          P_Xi_C=learningObject.learnConditional(childVar,parents, db, intervals,4);
         
          //Learning p(Xj|C) 
          childVar=completell.elementAt(i).getTail();//Variable Xj
          P_Xj_C=learningObject.learnConditional(childVar,parents, db, intervals,4);
          
          //Learning p(Xi|Xj,C) 
          childVar=completell.elementAt(i).getHead();//Variable Xi
          parents.insertNode(completell.elementAt(i).getTail()); //Variable Xj
                 
          P_Xi_Xj_C=learningObject.learnConditional(childVar, parents, db, intervals, 4);
          
          value=ContinuousProbabilityTree.estimateConditionalMutualInformation(P_C, P_Xj_C, P_Xi_Xj_C, P_Xi_C,5000);
       
          vCMI.addElement(value);             
          System.out.print("\nLINK: " + ((Link)completell.getLinks().elementAt(i)).toString()); 
          System.out.println("---------------------------------------------");
          System.out.println("     �(Xi,Xj|C)="+ value);
          System.out.println("---------------------------------------------");
       }

       Vector vOrderedCMI=new Vector(this.OrderedIndexByMutualInformation(vCMI));     
       
       //Creo la lista de nodos S y le inserto el nodo con mas I(Xi,C)
        NodeList S=new NodeList();
        Node aux=variables.elementAt(((Integer)vOrderedMI.elementAt(0)).intValue());        
        S.insertNode(aux); 
        
        //Para el resto de descriptores
        for (i=1;i<variables.size()-1;i++){
                             
            //Next index
            int index=((Integer)vOrderedMI.elementAt(i)).intValue();
            Node Xmax=(Node)variables.elementAt(index);
                                            
            //Number of links added
            int m=Math.min(i,k);        
            
            NodeList NodosOrigen=this.getNodesMaxCMI(m, S,completell, vOrderedCMI, Xmax);
            S.insertNode(Xmax);
            
            //Creo los links
            for (j=0;j<NodosOrigen.size();j++){
                System.out.println("Inserting link "+ NodosOrigen.elementAt(j).getName()+"-->" + Xmax.getName());
                try {      
                    net.createLink(NodosOrigen.elementAt(j), Xmax, true);   
                }  catch (Exception e) { System.out.println("Problems to create the link");};                  
            }
        }
     
       //----------------------------------------------------------------------
       //----------- ENTRENAMIENTO DEL k-DB -----------------------------------
       //----------------------------------------------------------------------
       netRL=new Vector();
       
       System.out.println("\n\n====> Learning kDB <=======");
       
       for (i=0;i<variables.size();i++){
         if (i!=classVariable){
            
            childVar=variables.elementAt(i);
            parents=new NodeList();
            parents=childVar.getParentNodes();
          
            System.out.println("   Learning " + childVar.getName()+" ...");
            t=learningObject.learnConditional(childVar, parents, db, intervals, 4);
            
            relationVariables=new NodeList();
            relationVariables.insertNode(childVar);
            for (j=0;j<parents.size();j++){
                relationVariables.insertNode(parents.elementAt(j));
            }
            pot=new PotentialContinuousPT(relationVariables,t);
            rel=new Relation();
            rel.setVariables(relationVariables);
            rel.setValues(pot); 
            netRL.addElement(rel);
        }
       }
       
       childVar=classVar;
       System.out.println("   Learning " + childVar.getName()+" ... (CLASS VARIABLE)");
       t=learningObject.learnConditional(childVar, new NodeList(), db, intervals, 4);
       
       relationVariables=new NodeList();
       relationVariables.insertNode(classVar); 
       pot=new PotentialContinuousPT(relationVariables,t);
       rel=new Relation();
       rel.setVariables(relationVariables);    
       rel.setValues(pot); 
       netRL.addElement(rel);
      //-------------------------- 
      net.setRelationList(netRL);
      //-----------------------------------------------------------------------
    } 
      
    
   /**
     * Return a vector of m Nodes Xmax (features) included in NodeList S and with 
     * the highest I(Xmax,Xj|C) or I(Xj,Xmax|C) with respect to Xmax given as 
     * parameter.
     *
     * @param m number of Nodes included in returned Vector.
     * @param S is a NodeList that included the computed nodes
     * @param ll is a LinkList of the complete graph among the features
     * @param vOrderedCMI is a vector of ordered indexes about CMI
     * @param Xmax is the destination node.
     * @return NodeList with the m best nodes.
   */
    
    public NodeList getNodesMaxCMI(int m, NodeList S, LinkList ll, Vector vOrderedCMI, Node Xmax){
    
        NodeList vNodes=new NodeList();
               
        int i=0;
        while (vNodes.size()<m){
                     
            int index=((Integer)vOrderedCMI.elementAt(i)).intValue();
            Node head=ll.elementAt(index).getHead();
            Node tail=ll.elementAt(index).getTail();
            
            //if Xmax is in the actual link
            if ((Xmax.equals(head)) || (Xmax.equals(tail)))
            {   
               //If head is included in S
               if (S.getId(head.getName())!=-1) {
                   vNodes.insertNode(variables.getNode(head.getName()));
               }                
               //If tail is included in S
               if (S.getId(tail.getName())!=-1) {
                   vNodes.insertNode(variables.getNode(tail.getName()));
               }      
            }     
            i++;
        }
        return vNodes;
    }
    
    /**
 * Obtains the descending order of the numbers of the input vector
 * @param W <code>Vector</code> contains the mutual informations between
 * each feature variable and the class variable in the same order that 
 * the nodes in NodeList.
 * @return a <code>Vector</code> with the indexes position ordered by 
 * descending.
 *
 */ 
private Vector OrderedIndexByMutualInformation (Vector W){
   
     double max; 
     int index;
     
     Vector ordered_indexes=new Vector();
     Vector weight_aux=(Vector)W.clone();
     
     for (int i=0;i<weight_aux.size();i++){
        max=-1000.0;
        index=0;
        for (int j=0;j<weight_aux.size();j++){
            if (((Double)weight_aux.elementAt(j)).doubleValue()>max){
                max=((Double)weight_aux.elementAt(j)).doubleValue();
                index=j;              
            }
        }
        ordered_indexes.addElement(new Integer(index));
        weight_aux.setElementAt(new Double(-1000.0),index);
     }
     return ordered_indexes;
} 
      
    /**
     * Creates all the posible links between the feature variables
     *  
     * @param vars is a <code>NodeList</code> of the graph
     *
     * @return a <code>LinkList</code> 
     */     
    private LinkList CompleteLinkList(NodeList vars){
        
        LinkList list=new LinkList();
        Link l;
        
        //Making the complete graph of the features variables 
        for (int i=0;i<vars.size()-1;i++) {
            for (int j=i;j<vars.size();j++) {        
              if ((i!=j)&&(i!=classVariable)&&(j!=classVariable)){
                  //System.out.print("("+i+","+j+")");
                  //new undirected link between i and j nodes
                   l = new Link(vars.elementAt(i),vars.elementAt(j),false);                
                   list.insertLink(l);
                }
            }
        }
        return list;
      }
    
    
    /**
     * Saves the network corresponding to the predictor in the given file.
     *
     * @param Name the name of the file where the net will be written.
     */  
    public void saveNetwork(String name) throws IOException {
        
        FileWriter f = new FileWriter(name);    
        net.saveBnet(f);
        f.close();
    }
     
    /**
     * Computes the bias in the prediction.
     * @param predictedValues a vector with the predicted values.
     * @param exactValues a vector with the exact values.
     * @return the bias of the predictions.
     */ 
    public double computeBias(Vector predictedValues, Vector exactValues) {

        int i, n;
        double bias = 0.0, x, y;
        
        n = predictedValues.size();
        
        for (i=0 ; i<n ; i++) {
            x = ((Double)predictedValues.elementAt(i)).doubleValue();
            y = ((Double)exactValues.elementAt(i)).doubleValue();
            bias += (x-y);
        }
        
       
        return (bias/n);
    }
     
    /**
     * Predicts the value of the class variable for a given configuration.
     * The prediction is equal to the mean of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is given as argument.
     *
     * The propagation is  done using the Penniless method.
     *
     * @param conf the <code>ContinuousConfiguration</code> for which the
     * value of the class will be predicted.
     * @param classVar the class variable.
     * @return a vector with the predicted value in the first position (mean of the
     * posterior distribution), the variance in the second position and the exact value
     * for the class variable in the thirs position.
     */
    public Vector predictWithMean(ContinuousConfiguration conf, Node classVar) {
        
        double exactValue, mean, mode, median, variance;
        Evidence evidence;
        MTESimplePenniless propagation;
        Vector results, returnValues;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree tree;
        
        exactValue = conf.getValue((Continuous)classVar);
        
        // Remove the class variable from the configuration
        conf.remove(classVar);
        
        evidence =  new Evidence(conf);
        
        propagation = new MTESimplePenniless(net,evidence,0,0,0,0,0);
        
        // Since the only unobserved variable is the class, after the propagation,
        // in vector results the only potential will be the corresponding
        // to the posterior distribution of the class.

        //evidence.print();

        
        propagation.propagate(evidence);
        
       // try{
       //     propagation.saveResults("resultado.txt");
       // }catch(Exception e){};
        
        results = propagation.getResults();
        
        pot = (PotentialContinuousPT)results.elementAt(0);
        
        //pot.print();
        
        tree = pot.getTree();
        
        mean = tree.firstOrderMoment();
        
        variance = tree.Variance();
        
        //mode = tree.plainMonteCarloMode(5000);
        //mode = tree.gradientMonteCarloMode(50);
        median = tree.median();
        
        returnValues = new Vector();
        
        returnValues.addElement(new Double(mean));
        returnValues.addElement(new Double(variance));
        //returnValues.addElement(new Double(mode));
        returnValues.addElement(new Double(median));
        returnValues.addElement(new Double(exactValue));
        
     //   System.out.println(mean+" ; "+variance+" ; "+median+" ; "+exactValue);
        
        return (returnValues);
    }
      
    /**
     * Predicts the value of the class variable for a given configuration.
     * The prediction is equal to the mean of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is given as argument.
     *
     * The propagation is  done using the Penniless method.
     *
     * @param conf the <code>ContinuousConfiguration</code> for which the
     * value of the class will be predicted.
     * @param classVar the class variable.
     * @param bias the bias of the prediction.
     * @return a vector with the predicted value in the first position (mean of the
     * posterior distribution), the variance in the second position and the exact value
     * for the class variable in the thirs position.
     */  
    public Vector predictWithMean(ContinuousConfiguration conf, Node classVar, double bias) {
        
        double exactValue, mean, mode, median, variance;
        Evidence evidence;
        MTESimplePenniless propagation;
        Vector results, returnValues;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree tree;
        
        exactValue = conf.getValue((Continuous)classVar);
        
        // Remove the class variable from the configuration
        conf.remove(classVar);
        
        evidence =  new Evidence(conf);
        
        propagation = new MTESimplePenniless(net,evidence,0,0,0,0,0);
        
        // Since the only unobserved variable is the class, after the propagation,
        // in vector results the only potential will be the corresponding
        // to the posterior distribution of the class.
        propagation.propagate(evidence);
        
        results = propagation.getResults();
        
        pot = (PotentialContinuousPT)results.elementAt(0);
        
        //pot.print();
        
        tree = pot.getTree();
        
        mean = tree.firstOrderMoment() - bias;
        
        variance = tree.Variance();
        
        //mode = tree.plainMonteCarloMode(5000);
        //mode = tree.gradientMonteCarloMode(50);
        median = tree.median() - bias;
        
        returnValues = new Vector();
        
        returnValues.addElement(new Double(mean));
        returnValues.addElement(new Double(variance));
        //returnValues.addElement(new Double(mode));
        returnValues.addElement(new Double(median));
        returnValues.addElement(new Double(exactValue));
        
      //  System.out.println(mean+" ; "+variance+" ; "+median+" ; "+exactValue);
        
        return (returnValues);
    }
     
    /**
     * Predicts the value of the class variable for all the
     * registers in a given database.
     * The prediction is equal to the mean of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is supposed to be stored in column
     * <code>classVariable</code>.
     *
     * The propagation is  done using the Penniless method.
     *
     * @param db the database with the registers to predict.
     * @return a vector with a vector in each position containing the predicted value
     * in the first position (mean of the
     * posterior distribution), the variance in the second position, tho mode in the third and the exact value
     * for the class variable in the fourth position.
     */  
    public Vector predictWithMean(DataBaseCases db) {
        
        ContinuousCaseListMem cases;
        ContinuousConfiguration conf;
        NodeList vars;
        Node classVar;
        Vector resultValues, registerValues, vectorMeans,  vectorModes, vectorVariances;
        Vector vectorExact;
        
        int i, nc;
        
        vars = db.getVariables();
     
        classVar = vars.elementAt(classVariable);
        
       // System.out.println("***************>>>>>"+classVar.getName());
        
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        resultValues = new Vector();
        vectorMeans = new Vector();
        vectorModes = new Vector();
        vectorVariances = new Vector();
        vectorExact = new Vector();
        
        nc = cases.getNumberOfCases();
       //POR QUE NO ES 0?
        for (i=1 ; i<nc ; i++) {
            conf = (ContinuousConfiguration)cases.get(i);
            //System.out.println("========= PRINT ==============");
            //conf.print();
            //System.out.println("========= CLASSVAR ==============");
            //classVar.print();

            registerValues = predictWithMean(conf,classVar);
            vectorMeans.addElement((Double)registerValues.elementAt(0));
            vectorVariances.addElement((Double)registerValues.elementAt(1));
            vectorModes.addElement((Double)registerValues.elementAt(2));
            vectorExact.addElement((Double)registerValues.elementAt(3));
        }
        
        resultValues.addElement(vectorMeans);
        resultValues.addElement(vectorVariances);
        resultValues.addElement(vectorModes);
        resultValues.addElement(vectorExact);
        
        
        return (resultValues);
    }
       
     /**
     * Predicts the value of the class variable for all the
     * registers in a given database.
     * The prediction is equal to the mean of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is supposed to be stored in column
     * <code>classVariable</code>.
     *
     * The propagation is  done using the Penniless method.
     *
     * @param db the database with the registers to predict.
     * @param bias the bias of the prediction.
     * @return a vector with a vector in each position containing the predicted value
     * in the first position (mean of the
     * posterior distribution), the variance in the second position, tho mode in the third and the exact value
     * for the class variable in the fourth position.
     */ 
    public Vector predictWithMean(DataBaseCases db, double bias) {
        
        ContinuousCaseListMem cases;
        ContinuousConfiguration conf;
        NodeList vars;
        Node classVar;
        Vector resultValues, registerValues, vectorMeans,  vectorModes, vectorVariances;
        Vector vectorExact;
        
        int i, nc;
        
        vars = db.getVariables();
        classVar = vars.elementAt(classVariable);
       // System.out.println("**********************"+ classVariable);
       // System.out.println("**********************"+ classVar.getName());
        //javax.swing.JOptionPane.showInputDialog("HOLA2");
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        resultValues = new Vector();
        vectorMeans = new Vector();
        vectorModes = new Vector();
        vectorVariances = new Vector();
        vectorExact = new Vector();
        
        nc = cases.getNumberOfCases();
        for (i=1 ; i<nc ; i++) {
            conf = (ContinuousConfiguration)cases.get(i);
            registerValues = predictWithMean(conf,classVar,bias);
            vectorMeans.addElement((Double)registerValues.elementAt(0));
            vectorVariances.addElement((Double)registerValues.elementAt(1));
            vectorModes.addElement((Double)registerValues.elementAt(2));
            vectorExact.addElement((Double)registerValues.elementAt(3));
        }
        
        resultValues.addElement(vectorMeans);
        resultValues.addElement(vectorVariances);
        resultValues.addElement(vectorModes);
        resultValues.addElement(vectorExact);
        
        
        return (resultValues);
    } 
       
    /**
     * Computes the rooted mean squared error (RMSE) of the predicted values as well as the
     * linear correlation coefficient (LCC) between the predicted values and the exact ones.
     * @param predictedValues a vector with the predicted values.
     * @param exactValues a vector with the exact values.
     * @return a vector with the RMSE in the first position and the LCC in the second.
     */   
    public Vector computeErrors(Vector predictedValues, Vector exactValues) {

        int i, n;
        double meanPredicted = 0.0, meanExact = 0.0, x, y, sumProd = 0.0, sumCuadX = 0.0;
        double sumCuadY = 0.0, varX, varY, lcc, rmse, dif = 0.0, sx, sy, sxy;
        Vector returnVector = new Vector();
        
        n = predictedValues.size();
        
        for (i=0 ; i<n ; i++) {
            x = ((Double)predictedValues.elementAt(i)).doubleValue();
            y = ((Double)exactValues.elementAt(i)).doubleValue();
            meanPredicted += x;
            meanExact += y;
            //dif += Math.pow((x-y),2);
            dif += ((x-y)*(x-y));
            //System.out.println(y);
        }
        
        
        meanPredicted /= (double)n;
        meanExact /= (double)n;
        
        for (i=0 ; i<n ; i++) {
            x = ((Double)predictedValues.elementAt(i)).doubleValue();
            y = ((Double)exactValues.elementAt(i)).doubleValue();
            //System.out.println("Exacto "+y+" Aproximado "+x);
            //sumCuadX += Math.pow(x-meanPredicted,2);
            sumCuadX += ((x-meanPredicted)*(x-meanPredicted));
            //sumCuadY += Math.pow(y-meanExact,2);
            sumCuadY += ((y-meanExact)*(y-meanExact));
            sumProd += (x-meanPredicted) * (y-meanExact);
        }
        
        sumProd /= (double)n;
        sumCuadX /= (double)n;
        sumCuadY /= (double)n;
        
        sx = Math.sqrt(sumCuadX/(double)n);
        sy = Math.sqrt(sumCuadY/(double)n);
        sxy = sumProd / (double)n;
        
       // System.out.println("mx "+meanPredicted+" my "+meanExact+" scx "+sumCuadX+" scy "+sumCuadY);
       // System.out.println("sxy "+sxy+" sx "+sx+" sy "+sy);
        rmse = Math.sqrt(dif/(double)n);
        lcc = sxy / (sx * sy);
        
        returnVector.addElement(new Double(rmse));
        returnVector.addElement(new Double(lcc));
        
        return (returnVector);
    }
    /**
     * Main for constructing an MTE kDB predictor from a database.
     *
     * Arguments:
     * 0. the dbc train file.
     * 1. the dbc test file. "CV" for cross-validation.
     * 2. the index of the class variable (starting from 0).
     * 3. the number of intervals into which the domain of the continuous 
     *    variables will be split.
     * 4. k = the maximum number of predictor variables as parents (k-DB)
     * 5. In case of cross validation, the number of folds.
     */
    public static void main(String args[]) throws ParseException,IOException {
        
        FileInputStream fTrain = new FileInputStream(args[0]);
        int classv = Integer.valueOf(args[2]).intValue();
        int interv = Integer.valueOf(args[3]).intValue();
        int k = Integer.valueOf(args[4]).intValue(), i;
        int kfold = Integer.valueOf(args[5]).intValue();
        kDBMTEPredictor pred=null;
        double lcc_mean = 0.0, lcc_median = 0.0, rmse_mean = 0.0, rmse_median = 0.0;
        Vector results, errors;
        double bias;
        DataBaseCases dbTrain, dbTest;
        
        if (args[1].compareTo("CV") == 0) { // Cross validation
         
            DataBaseCases c = new DataBaseCases(fTrain);
                
            for (i=0 ; i<kfold ; i++) {
         
                System.out.println("ITERATION "+i);
                dbTrain = c.getTrainCV(i,kfold);
                dbTest = c.getTestCV(i,kfold);
                pred = new kDBMTEPredictor(dbTrain,classv,interv,k); 
                
                pred.saveNetwork("temp.elv");
                
                results = pred.predictWithMean(dbTrain);
                bias = pred.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));
                results = pred.predictWithMean(dbTest,bias);
                errors = pred.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));
                rmse_mean += ((Double)(errors.elementAt(0))).doubleValue();
                lcc_mean += ((Double)(errors.elementAt(1))).doubleValue();
                System.out.println("\n\n\n===================== DATOS DEL MODELO TEMPORAL ============");
                System.out.println("MEAN --->  rmse_M: " + ((Double)(errors.elementAt(0))).doubleValue() + "   lcc_M: "+ ((Double)(errors.elementAt(1))).doubleValue());       
                
                errors = pred.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));
                rmse_median += ((Double)(errors.elementAt(0))).doubleValue();
                lcc_median += ((Double)(errors.elementAt(1))).doubleValue();
                
                System.out.println("MEDIAN ->  rmse_M: " + ((Double)(errors.elementAt(0))).doubleValue() + "   lcc_M: "+ ((Double)(errors.elementAt(1))).doubleValue());
                System.out.println("====================================================================\n\n");
              
            }     
            rmse_mean /= kfold;
            lcc_mean /= kfold;
            rmse_median /= kfold;
            lcc_median /= kfold;
            
            System.out.println("\nFinal results:");
            System.out.println("rmse_mean,lcc_mean,rmse_median,lcc_median");
            System.out.println(rmse_mean + "," + lcc_mean + "," + rmse_median + "," + lcc_median);
            System.out.println("\n");
        }
        else 
        {
           
            FileInputStream fTest = new FileInputStream(args[1]);
         //   double bias, rmse_mean, rmse_median, lcc_mean, lcc_median;
            //Vector results, errors;

            DataBaseCases cTrain = new DataBaseCases(fTrain);

            pred = new kDBMTEPredictor(cTrain,classv,interv,k);   
           

            results=pred.predictWithMean(cTrain); 

            bias = pred.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));

            DataBaseCases cTest=new DataBaseCases(fTest);

            results=pred.predictWithMean(cTest,bias); 

            errors = pred.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));

            rmse_mean=((Double)(errors.elementAt(0))).doubleValue();
            lcc_mean=((Double)(errors.elementAt(1))).doubleValue();

            errors = pred.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));

            rmse_median=((Double)(errors.elementAt(0))).doubleValue();
            lcc_median=((Double)(errors.elementAt(1))).doubleValue();

            System.out.println("\nFinal results:");
            System.out.println("rmse_mean,lcc_mean,rmse_median,lcc_median");
            System.out.println(rmse_mean + "," + lcc_mean + "," + rmse_median + "," + lcc_median);
            System.out.println("\n");
           
        }

        
    }     
        
        
        
        
        
        
        
        
        
        
        
        
        
 /*       
        
        
       //Load input parameters
       FileInputStream fTrain = new FileInputStream(args[0]);   
       int classv=Integer.valueOf(args[1]).intValue();
       int interv= Integer.valueOf(args[2]).intValue();
       int k=Integer.valueOf(args[3]).intValue();
       
       

       DataBaseCases cTrain = new DataBaseCases(fTrain);

       kDBMTEPredictor pred = new kDBMTEPredictor(cTrain,classv,interv,k); 
       
       
       
}  */  
}

















