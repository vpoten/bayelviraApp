package elvira.learning.classification.supervised.continuous;

import elvira.Continuous;
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

/**
 * Implements a predictor (classifier with continuous class variable) where
 * all the variables, including the class, are of class MTE.
 * The model assumes that the joint distribution is MTE.
 *
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 * @author Antonio Fernandez Alvarez (afalvarez@ual.es)
 * @since 1/06/2007
 */

public class NaiveMTEPredictor {
    
    /**
     * Variables for which the predictor is defined.
     */
    NodeList variables;
    
    /**
     * The index of the class variable in list <code>variables</code>.
     */
    int classVariable;
    
    /**
     * The network that defined the predictor.
     */
    Bnet net;
       
    /**
     * The database from which the network.
     */
    DataBaseCases dbCases;
    
    /**
     * Creates an empty instance of this class.
     */
    
    public NaiveMTEPredictor(){
        
        variables = new NodeList();
        classVariable = -1;
        net = new Bnet();
    }
     
       
    /**
     * Creates a NaiveMTEPredictor from a database.
     *
     * @param db the DataBaseCases from which the predictor will be constructed.
     * @param cv an int indicating the index (column) of the class variable in the database. The first column
     * is labeled as 0.
     * @param intervals the number of intervals into which the domain of the continuous
     * variables will be split.
     */
    
    public NaiveMTEPredictor(DataBaseCases db, int cv, int intervals)  {
        
        MTELearning learningObject;
        NodeList parents, netNodeList, relationVariables;
        Node childVar, classVar;
        ContinuousProbabilityTree t;
        PotentialContinuousPT pot;
        Relation rel;
        Vector netRL;
        Link l;
        LinkList ll;
        
        int i;
        
        variables = db.getVariables().copy();
        classVariable = cv;
        
        classVar = variables.elementAt(cv);
        parents = new NodeList();
       
        dbCases = db;
        
        // The only parent will always be the class variable.
        parents.insertNode(classVar);
        
        // Construct a naive Bayes with all the variables.
        
        learningObject = new MTELearning(db);
        
        
        // The nodelist of the resulting network
        netNodeList = new NodeList();
        netNodeList.insertNode(classVar);
        
        // the relation list of the resulting network
        netRL = new Vector();
        
        // The link list of the resulting network
        ll = new LinkList();
        
        for (i=0 ; i<variables.size() ;  i++) {
            if (i != cv) {
                childVar = variables.elementAt(i);
               // System.out.println("Hijo: " + childVar.getName());
                //parents.printNames();
                t = learningObject.learnConditional(childVar,parents,db,intervals,4);
                netNodeList.insertNode(childVar);
                
                // the variables of the relation corresponding to the conditional distribution
                // for variable childVar.
                relationVariables = new NodeList();
                
                relationVariables.insertNode(childVar);
                relationVariables.insertNode(classVar);
                
                l = new Link(classVar,childVar);
                ll.insertLink(l);
                
                pot = new PotentialContinuousPT(relationVariables,t);
                
                rel = new Relation();
                rel.setVariables(relationVariables);
                rel.setValues(pot);
                
                netRL.addElement(rel);
            }
            else {
                // The marginal for the class variable is estimated separately.
                t = learningObject.learnConditional(classVar,new NodeList(),db,intervals,4);
                relationVariables = new NodeList();
                relationVariables.insertNode(classVar);
                
                pot = new PotentialContinuousPT(relationVariables,t);
                
                rel = new Relation();
                rel.setVariables(relationVariables);
                rel.setValues(pot);
                
                netRL.addElement(rel);
            }
        }
        
        net = new Bnet();
        net.setRelationList(netRL);
        net.setNodeList(netNodeList);
        net.setLinkList(ll);
        
    }
  
    
    /**
     * Obtain a list of the mutual information between each variable and the class.
     *
     * @return a vector with the mutual information for each variable, considering
     * the order given in instance variable <code>variables</code>, except for the
     * class, in which position a -1 is stored.
     */    
    public Vector getListOfMututlinformation() {
        
        int i, n;
        RelationList relations, relVar;
        Relation classPrior = new Relation(), rel;
        Node classVar, featureVar;
        ContinuousProbabilityTree classTree, featureTree;
        double info;
        Vector res = new Vector();
        
        
        classVar = variables.elementAt(classVariable);
        
        relations = net.getInitialRelations();
        
        relVar = relations.getRelationsOf(classVar);
        
        
        for (i=0 ; i<relVar.size() ; i++) {
            rel = relVar.elementAt(i);
            if (rel.getVariables().size() == 1)
                classPrior = rel.copy();
        }
        
        classTree = ((PotentialContinuousPT)classPrior.getValues()).getTree();
        
        n = (int)variables.size();
        
        for (i=0 ; i<n ; i++) {
            if (i != classVariable) {
                featureVar = variables.elementAt(i);
                
                relVar = relations.getRelationsOf(featureVar);
                
                // The feature variable must be just in one relation
                rel = relVar.elementAt(0);
                
                featureTree = ((PotentialContinuousPT)rel.getValues()).getTree();
                
                //System.out.println("Clase");
                //classTree.print();
                //System.out.println("\nOtra\n");
                //featureTree.print();
               
                info = ContinuousProbabilityTree.estimateMutualInformation(classTree,featureTree,5000);
            
                res.addElement(new Double(info));
            }
            else {
                res.addElement(new Double(-1000.0));
            }
        }
        
        return (res);
    }
 
    
    
    /**
     * Computes the rooted mean squared error (RMSE) of the predicted values as well as the
     * linear correlation coefficient (LCC) between the predicted values and the exact ones.
     * @param predictedValues a vector with the predicted values.
     * @param exactValues a vector with the exact values.
     * @return a vector with the RMSE in the first position and the LCC in the second.
     */
    
    public static Vector computeErrors(Vector predictedValues, Vector exactValues) {

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
        
        //System.out.println("mx "+meanPredicted+" my "+meanExact+" scx "+sumCuadX+" scy "+sumCuadY);
        //System.out.println("sxy "+sxy+" sx "+sx+" sy "+sy);
        rmse = Math.sqrt(dif/(double)n);
        lcc = sxy / (sx * sy);
        
        returnVector.addElement(new Double(rmse));
        returnVector.addElement(new Double(lcc));
        
        return (returnVector);
    }
    
    
    /**
     * Computes the bias in the prediction.
     * @param predictedValues a vector with the predicted values.
     * @param exactValues a vector with the exact values.
     * @return the bias of the predictions.
     */
    
    public static double computeBias(Vector predictedValues, Vector exactValues) {

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
     * The prediction is equal to the mean and the median of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is given as argument.
     *
     * The propagation is done using the Penniless method.
     *
     * @param conf the <code>ContinuousConfiguration</code> for which the
     * value of the class will be predicted.
     * @param classVar the class variable.
     * @return a vector with the predicted value in the first position (mean of the
     * posterior distribution), the variance in the second position, the predicted value
     * using the median of the posterior distribution in the third position and the exact value
     * for the class variable in the fourth position.
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
        propagation.propagate(evidence);
        
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
        
       // System.out.println(mean+" ; "+variance+" ; "+median+" ; "+exactValue);
        
        return (returnValues);
    }
    
    
    /**
     * Predicts the value of the class variable for a given configuration.
     * The prediction is equal to the mean and the median of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is given as argument.
     *
     * The propagation is done using the Penniless method.
     *
     * @param conf the <code>ContinuousConfiguration</code> for which the
     * value of the class will be predicted.
     * @param classVar the class variable.
     * @param bias the bias of the prediction.
     * @return @return a vector with the predicted value in the first position (mean of the
     * posterior distribution), the variance in the second position, the predicted value
     * using the median of the posterior distribution in the third position and the exact value
     * for the class variable in the fourth position.
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
     * posterior distribution), the variance in the second position, the median in the third and the exact value
     * for the class variable in the fourth position.
     */
    
    public Vector predictWithMean(DataBaseCases db) {
        
        ContinuousCaseListMem cases;
        ContinuousConfiguration conf;
        NodeList vars;
        Node classVar;
        Vector resultValues, registerValues, vectorMeans,  vectorMedians, vectorVariances;
        Vector vectorExact;
        
        int i, nc;
        
        vars = db.getVariables();
        classVar = vars.elementAt(classVariable);
        
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        resultValues = new Vector();
        vectorMeans = new Vector();
        vectorMedians = new Vector();
        vectorVariances = new Vector();
        vectorExact = new Vector();
        
        nc = cases.getNumberOfCases();
        for (i=1 ; i<nc ; i++) {
            conf = (ContinuousConfiguration)cases.get(i);
            registerValues = predictWithMean(conf,classVar);
            vectorMeans.addElement((Double)registerValues.elementAt(0));
            vectorVariances.addElement((Double)registerValues.elementAt(1));
            vectorMedians.addElement((Double)registerValues.elementAt(2));
            vectorExact.addElement((Double)registerValues.elementAt(3));
        }
        
        resultValues.addElement(vectorMeans);
        resultValues.addElement(vectorVariances);
        resultValues.addElement(vectorMedians);
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
     * posterior distribution), the variance in the second position, the median in the third and the exact value
     * for the class variable in the fourth position.
     */
    
    public Vector predictWithMean(DataBaseCases db, double bias) {
        
        ContinuousCaseListMem cases;
        ContinuousConfiguration conf;
        NodeList vars;
        Node classVar;
        Vector resultValues, registerValues, vectorMeans,  vectorMedians, vectorVariances;
        Vector vectorExact;
        
        int i, nc;
        
        vars = db.getVariables();
        classVar = vars.elementAt(classVariable);
        
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        resultValues = new Vector();
        vectorMeans = new Vector();
        vectorMedians = new Vector();
        vectorVariances = new Vector();
        vectorExact = new Vector();
        
        nc = cases.getNumberOfCases();
        for (i=1 ; i<nc ; i++) {
            conf = (ContinuousConfiguration)cases.get(i);
                        
            conf.removeUndefinedValues();
            
            registerValues = predictWithMean(conf,classVar,bias);
            vectorMeans.addElement((Double)registerValues.elementAt(0));
            vectorVariances.addElement((Double)registerValues.elementAt(1));
            vectorMedians.addElement((Double)registerValues.elementAt(2));
            vectorExact.addElement((Double)registerValues.elementAt(3));
        }
        
        resultValues.addElement(vectorMeans);
        resultValues.addElement(vectorVariances);
        resultValues.addElement(vectorMedians);
        resultValues.addElement(vectorExact);
        
        
        return (resultValues);
    }
    
    /**
     * Saves the network corresponding to the predictor in the given file.
     *
     * @param fineName the name of the file where the net will be written.
     */
    
    public void saveNetwork(String name) throws IOException {
        
        FileWriter f = new FileWriter(name);
        net.saveBnet(f);
        f.close();
    }
    
    
    /**
     * Main for constructing an MTE naive predictor from a data base.
     *
     * Arguments:
     * 1. the dbc  train file.
     * 2. the dbc test file. "CV" for cross-validation.
     * 3. the index of the class variables (starting from 0).
     * 4. the number of intervals into which the domain of the continuous variables
     *    will be split.
     * 5. In case of cross validation, the number of folds.
     */
    
    public static void main(String args[]) throws ParseException,IOException {
        
        double lcc_mean = 0.0, lcc_median = 0.0, rmse_mean = 0.0, rmse_median = 0.0;
        
        if (args[1].compareTo("CV") == 0) { // Cross validation
            FileInputStream f = new FileInputStream(args[0]);
            int k = Integer.valueOf(args[4]).intValue(), i;
            int classv = Integer.valueOf(args[2]).intValue();
            int interv = Integer.valueOf(args[3]).intValue();
            Vector results, errors;
            double bias;
            DataBaseCases dbTrain, dbTest;
            DataBaseCases c = new DataBaseCases(f);
           
        
            
            for (i=0 ; i<k ; i++) {
                System.out.println("ITERATION "+i);
                dbTrain = c.getTrainCV(i,k);
                dbTest = c.getTestCV(i,k);
                
                NaiveMTEPredictor pred = new NaiveMTEPredictor(dbTrain,classv,interv);
                                
                results = pred.predictWithMean(dbTrain);
                bias = pred.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));
                
                results = pred.predictWithMean(dbTest,bias);
                errors = pred.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));
                rmse_mean += ((Double)(errors.elementAt(0))).doubleValue();
                lcc_mean += ((Double)(errors.elementAt(1))).doubleValue();
                
                errors = pred.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));
                rmse_median += ((Double)(errors.elementAt(0))).doubleValue();
                lcc_median += ((Double)(errors.elementAt(1))).doubleValue();
            }
        
            rmse_mean /= k;
            lcc_mean /= k;
            rmse_median /= k;
            lcc_median /= k;
            
            System.out.println(k+"-fold cross validation.");
            System.out.println("\nFinal results:");
            System.out.println("rmse_mean,lcc_mean,rmse_median,lcc_median");
            System.out.println(rmse_mean + "," + lcc_mean + "," + rmse_median + "," + lcc_median);
            System.out.println("\n");

        } else {
            FileInputStream f = new FileInputStream(args[0]);
            FileInputStream f2 = new FileInputStream(args[1]);
            int classv = Integer.valueOf(args[2]).intValue();
            int interv = Integer.valueOf(args[3]).intValue();
            Vector results, errors;
            double bias;
            
            DataBaseCases c = new DataBaseCases(f);
          
            NaiveMTEPredictor pred = new NaiveMTEPredictor(c,classv,interv);
            
            pred.saveNetwork("NB.elv");
            
            results = pred.predictWithMean(c);
            
            bias = pred.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));
            
            DataBaseCases d = new DataBaseCases(f2);
           
            results = pred.predictWithMean(d,bias);
            
            System.out.println("Hold-out validation");
            errors = pred.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));
           // System.out.println("With the mean");
           // System.out.println("rmse = "+((Double)(errors.elementAt(0))).doubleValue()+" ; lcc = "+((Double)(errors.elementAt(1))).doubleValue());
            
            errors = pred.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));
            //System.out.println("With the median");
            //System.out.println("rmse = "+((Double)(errors.elementAt(0))).doubleValue()+" ; lcc = "+((Double)(errors.elementAt(1))).doubleValue());
            
            System.out.println("\nFinal results:");
            System.out.println("rmse_mean,lcc_mean,rmse_median,lcc_median");
            System.out.println(rmse_mean + "," + lcc_mean + "," + rmse_median + "," + lcc_median);
            System.out.println("\n");
        }
    }
}