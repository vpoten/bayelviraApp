package elvira.learning.classification.supervised.discrete;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import elvira.Bnet;
import elvira.CaseList;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.InvalidEditException;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.fusion.Fusion;
import elvira.gui.explication.CasesList;
import elvira.inference.elimination.VEWithPotentialTree;
import elvira.learning.classification.ClassifierException;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;

/**
 * Implements a discrete incremental classifier where all variables are discrete.
 * The incremental classification consists of creating classifiers related to
 * different datasets of the same problem (same variables as well), and then it 
 * creates a superclass which is parent of each class variable of the 
 * classifiers. The model can be seen in the next picture:
 *
 *         MAIN_C___________________
 *           / \                    \
 *          /   \                    \
 *         /     \                    \
 *        /       \                    \    
 *        C        C_1                 C_i
 *        /\        /\       . . .      /\     
 *       /  \      /  \                /  \
 *      X1  X2   X1_1 X1_2           X1_i X1_i
 *
 *
 * @author Ildikó Flesch (tamtatam@yahoo.com)
 * @since 1/06/2007
 */
public class IncrementSearchNaiveBayes extends DiscreteClassifierDiscriminativeLearning {
    private static final long serialVersionUID = -6386624028949005625L;
    /** Extention string used to make nodename unique. */
    private static final String NODE_NAME_EXTENSION = "_";
    /** The node list of each naive_bayes */
    private final NodeList nodeListNB;
    /** The list of all increments. */
    private final List<Naive_Bayes> increments = new ArrayList<Naive_Bayes>();
    /** The increment. */
    private Naive_Bayes newIncrement = null;
    /** The node of the main classifier. */
    private final FiniteStates mainClassifierNode; 
    
    /**
     * Constructor
     * @param base The input to learn a classifier
     * @param increment The input to learn a classifier
     * @param lap To apply the laplace correction
     * @throws IllegalStateException In case the base is not trained or the base classifier can not be cloned.
     */
    public IncrementSearchNaiveBayes(Naive_Bayes base, Naive_Bayes increment, boolean lap)
            throws elvira.InvalidEditException {
        // start the incremental structure based on a copy of given data
        super(base.cases.copy(), lap);
        // set the title for all case nodes
        for (Enumeration e = cases.getVariables().elements(); e.hasMoreElements();) {
            Node node = (Node)e.nextElement();
            node.setTitle(node.getName());
        }
        // make sure the first base is trained
        if (base.evaluations == 0) {
            throw new IllegalArgumentException("The base must be a trained naive bayes classifier.");
        }
        // add the structure of the naive_bayes as base structure
        classifier = cloneClassifier(base.classifier);
        // set the nodeList that must be used by all the increments
        nodeListNB = classifier.getNodeList().duplicate();
        // remember the last node name
        final String classNodeName = classifier.getNodeList().lastElement().getName();
        // create the incremental classifier by copying the naive bayes classifier
        //   where we copy all the properties except the sets of parents and children
        mainClassifierNode = (FiniteStates)(classifier.getNodeList().lastElement().copy());                      
        // set the name of the incremental classifier
        setNodeName(mainClassifierNode, "MAIN_CLASSIFIER");
        // add the node to the incremental classifier
        classifier.addNode(mainClassifierNode);
        // set the main classifier as the classifier of the incr.search 
        setClassVar();
        // update the number of variables
        this.nVariables += 1;
        // add a link to the main classifier
        linkToMainClassifier(classifier, classNodeName);
        // extend the database cases with the main classifier node
        extendCases(cases, mainClassifierNode, new CaseExtender() {
            public void fill(Configuration oldCase, Configuration newCase) {
                // and set the value to the classifier node value
                int value = newCase.getValue(classNodeName);
                newCase.putValue(mainClassifierNode.getName(), value);
            }});
        // add the base as the first increment
        increments.add(base);                
        // set the first new increment
        addIncrement(increment, lap);
    }

    /**
     * Set the name of the node to the given name.
     * @param node Node to be changed.
     * @param name The new name.
     */
    private void setNodeName(Node node, String name) {
        node.setName(name);
        node.setTitle(name);
    }
    
    /**
     * Link the given classifier network to the main classifier.
     * @param classifier The classifier to add to the incremental network.
     */
    private void linkToMainClassifier(Bnet classifier, String classNodeName) {
        Node classNode = classifier.getNode(classNodeName);
        // make link between naive_bayes classifier and main classifier
        try {
            classifier.createLink(classNode, mainClassifierNode);
        } catch (Throwable t) {
            throw new IllegalArgumentException("The naive_bayes classifier can not" +
                    " be connected to the main classifier: "+t.getMessage());
        }
    }

    /**
     * Add a new increment.
     * It is only possible to add a new increment if the current
     * incremental search network is trained.
     * @param increment The input to learn a classifier.
     * @param lap To apply the laplace correction.
     * @throws IllegalStateException If the current network is not trained yet.
     */
    public void addIncrement(Naive_Bayes increment, boolean lap) {
        synchronized(increments) {
            if (newIncrement != null) {
                throw new IllegalStateException("Previously given increment is not trained yet.");
            }
            // remember new increment
            newIncrement = increment;
            // set the class variable as the last variable in the list
            newIncrement.classVar = (FiniteStates)newIncrement.classifier.getNodeList().lastElement();
        }
        // TODO: inserting a new increment we need to check
        //        - variable elimination
        //        - other conditions
    }

    @Override
    public void structuralLearning() throws InvalidEditException, Exception {
        if (newIncrement == null) {
            throw new IllegalStateException("Add an increment first.");
        }
        // make sure the new increments is trained
        if (newIncrement.evaluations == 0) {
            newIncrement.train();
        }
        // the next evaluation
        evaluations++;

        // merge the classifiers
        List<Node> newNodes = mergeClassifiers();

        // merge the database with the newclassifer nodes and cases
        mergeDataBaseCases(newNodes);
        
        // update the number of variables
        this.nVariables += newNodes.size();

        synchronized(increments) {
            // add the new increment into the increments list
            increments.add(newIncrement);
            newIncrement = null;
        }
        //this.classifier.getNodeList()..removeNode(mainClassifierNode);
        //this.classifier.addNode(mainClassifierNode);
        
        //newNodes.remove(newNodes.indexOf(mainClassifierNode));
        //newNodes.add(mainClassifierNode);       
    }

    /**
     * Merge the current classifier with the new increment's classifier.
     * And make sure the new increment's classifier is linked to the
     * main classifier node.
     * @return the list of the renamed nodes
     * @throws Exception In case something goes wrong.
     */
    private List<Node> mergeClassifiers() throws Exception {
        // make a copy of the new increments classification network
        Bnet newClassifier;
        List<Node> newNodes = new ArrayList<Node>();

        newClassifier = cloneClassifier(newIncrement.classifier);
        // and rename all nodes to make them unique
        for (Iterator iter = newClassifier.getNodeList().getNodes().iterator(); iter.hasNext();) {
            Node node = (Node)iter.next();
            // change the node names in the relation
            Relation r = newClassifier.getRelation(node);
            if (r != null) {
                for (int i = 0; i < r.getVariables().size(); i++) {
                	Node rNode = r.getVariables().elementAt(i);
                    setNodeName(rNode, getNewNodeName(rNode));
                }
            }
            // change the node name itself
            setNodeName(node, getNewNodeName(node));
            newNodes.add(node);
        }
        // remember the class node name
        String classNodeName = newClassifier.getNodeList().lastElement().getName();
        // merge the classifier structure with the new increment
        Fusion classifierUnion = new Fusion(0, 0, classifier, newClassifier);       
        classifier = classifierUnion.getBnet();
        // link to the main classifier
        linkToMainClassifier(classifier, classNodeName);
        return newNodes;
    }

    /**
     * Create a clone of a classifier.
     * @param classifier
     * @return
     * @throws Exception
     */
    private Bnet cloneClassifier(Bnet classifier) throws IllegalArgumentException {
        Bnet newClassifier;
        try {
            // copy the network
            newClassifier = classifier.copyBnet();
            // copy the relations
            Vector<Relation> relationsCopy = new Vector<Relation>();
            for (Enumeration e = classifier.getRelationList().elements(); e.hasMoreElements();) {
                Relation r = (Relation)e.nextElement();
                // copy the relation
                Relation copyR = r.copy();
                // make sure we duplicate the node list (not done in copy)
                copyR.setVariables(r.getVariables().duplicate());
                copyR.getValues().setVariables(copyR.getVariables().getNodes());
                // and set the relation
                relationsCopy.add(copyR);
            }
            newClassifier.setRelationList(relationsCopy);
        } catch (Throwable t) {
            throw new IllegalArgumentException("The classifier can not be cloned: "+t.getMessage());
        }
        return newClassifier;
    }

    /**
     * Merge the new nodes to the database. 
     * @param newNodes The list of new nodes with the new names.
     * @throws InvalidEditException If a new node could not be added.
     */
    private void mergeDataBaseCases(List<Node> newNodes) throws InvalidEditException {
        // extend the existing cases of the database with a new set of variables
        extendExistingCases(newNodes);

        // loop over the existing classifier cases of the new increment
        CaseList newCaseList = (CaseList) newIncrement.cases.getCases();
        for (int i = 0; i < newCaseList.getNumberOfCases(); i++) {
            // and for each case, create a new case for the main classifier network
            Configuration mergedNewCase = createMergedCase(newCaseList.get(i));
            // insert the new sample in the database
            this.cases.getCases().put(mergedNewCase);
        }
        // update the number of cases
        this.cases.setNumberOfCases(this.cases.getNumberOfCases() + newCaseList.getNumberOfCases());
    }

    /**
     * Extend the existing database cases with a the variables of the new increment.
     */
    private void extendExistingCases(final List<Node> newNodes) throws InvalidEditException {
        extendCases(this.cases, newNodes, new CaseExtender() {
            public void fill(Configuration oldCase, Configuration newCase) {
                // create a new classifier case to find the class value
                Configuration classifierCase = new Configuration(nodeListNB);
                // read the values of the existing naive Bayes
                String classNodeNewName = getNewNodeName(newIncrement.getClassVar());
                for (Node node : newNodes) {
                    if ( !node.getName().equals(classNodeNewName) ) {
                        // get the value of the variable in the classifier database
                        String nodeName = getNodeName(node, 0);
                        int value = newCase.getValue(nodeName);
                        // collect the values for the classifier computation
                        classifierCase.setValue(nodeListNB.getId(nodeName), value);
                        //put the value to the new variable in the classifier database
                        newCase.putValue(node.getName(), value);
                    }
                }
                // classifier computation
                int value = getClassifierValue(newIncrement, classifierCase);
                newCase.putValue(getNewNodeName(newIncrement.getClassVar()), value);
            }});
    }

    /**
     * Compute the value of a classifier, given its children 
     * @param classifier is the naive _bayes classifier for which we want to obtain a classification value
     * @param naiveCase contains the values of the children of the classifier in the Naive_Bayes
     * @return the id of the most probable value of the classifier
     */
    private int getClassifierValue(Naive_Bayes classifierNB, Configuration naiveCase)
    { 
        // get the probabilities of the classifier vector (last in the list) 
        Vector classVector =  classifierNB.classify(naiveCase, naiveCase.getVariables().size()-1);
        // find the most probable value for the classifier
        int maxID = 0;
        double maxValue = (Double)classVector.get(0); 
        for (int i = 1; i < classVector.size(); i++) {
            if ((Double)classVector.get(i) > maxValue) {
                maxID = i; 
                maxValue = (Double)classVector.get(i);
            }            
        }
        // return the id of the most probable value of the classifier
        return maxID; 
        
    }
    
    /**
     * Create a new merged case with all variables for the classifier network.
     *
     * @param newIncrementCase One case of the new increment.
     * @return A merged case for the entire classifier network.
     */
    private Configuration createMergedCase(Configuration newIncrementCase) {
        // get a list of all variables
        NodeList nodeList = this.cases.getNodeList();
        // create new case with all variables
        Configuration mergedNewCase = new Configuration(nodeList);
        // loop over the values of the variables in newincrement
        for (Iterator iter = newIncrementCase.getVariables().iterator(); iter.hasNext();) {
            Node newIncrementNode = (Node) iter.next();
            // retrieve the value of the current variable
            int value = newIncrementCase.getValue(newIncrementNode.getName());
            // get the index in the nodeList
            int id = nodeList.getId(getNewNodeName(newIncrementNode));
            // put the value of the variable into the vector
            mergedNewCase.setValue(id, value);

            // fill the values for the nodes of the existing variables
            // except for the classifier variable of the newIncrement            
            if ( !newIncrementNode.getName().equals(getNodeName(newIncrement.getClassVar(), 0))) {    
                // get the value of the node of the new increment
                for (int j = 0; j < increments.size(); j++) {
                    id = nodeList.getId(getNodeName(newIncrementNode, j));
                    mergedNewCase.setValue(id, value);
                }
            } else {
                // the main classifier is set to the value of the
                // newIncrement classifier
                id = nodeList.getId(mainClassifierNode.getName());
                mergedNewCase.setValue(id, value);
                // get the value of the node of the new increment
                for (int j = 0; j < increments.size(); j++) {
                    id = nodeList.getId(getNodeName(newIncrementNode, j));
                    // compute the value of the naive bayes classifier 
                    int classifierValue = getClassifierValue(increments.get(j), newIncrementCase);                    
                    mergedNewCase.setValue(id, classifierValue);
                }
            }            
        }
        return mergedNewCase;
    }
    
    /**
     * Retrieve the unique name of the Node for the entire classifier
     * @param node the node of the newincrement
     * @return the new name
     */
    private String getNewNodeName(Node node) {
        return getNodeName(node, increments.size());
    }

    /**
     * Retrieve the node name for a specific increment.
     * @param node The original node.
     * @param incrementNr The increment nr.
     * @return The name of the node in this increment.
     */
    private String getNodeName(Node node, int incrementNr) {
        String nodeName = node.getName();
        // if this is a node from an increment (already contains -1..n)
        int index = nodeName.lastIndexOf(NODE_NAME_EXTENSION);
        if (index > 0 && nodeName.substring(index+1).matches("(\\d+)")) {
            // strip the -1..n from the name
            nodeName = nodeName.substring(0, index);
        }
        // the nodes of the base increment keeps their own name
        if (incrementNr == 0) {
            return nodeName;
        }
        // for consequent increments the incrementnumber is added to the name of the nodes
        return nodeName+NODE_NAME_EXTENSION+incrementNr;
    }

    /**
     * 
     */
    @Override
    public void parametricLearning() {
        // retrieve the current Potential for the main classifier
        Relation mainClassRelation = classifier.getRelation(mainClassifierNode);
        Potential mainClassPotential = mainClassRelation.getValues();
        
        // get the parent set of the main classifier node
        //  note: they are exactly the classifiers of the naive bayes cases
        NodeList mainClassParentList = mainClassifierNode.getParentNodes();

        // compute the number of cases in the incremental database
        //int nrOfCases = this.cases.getNumberOfCases();
        
        // loop over the possible states of the parents of the classifier
        for (Configuration mainClassParentConfPerm : new ConfigurationPermutation(mainClassParentList)) {
            // get the potentialTable based on the frequency for the current
            // configuration of the main classifier and its parents set
            PotentialTable mainClassPotTableForPerm = this.cases.getPotentialTable(mainClassifierNode, mainClassParentConfPerm);
            
            double nrOfCasesPerm = 0;
            // compute the number of cases of the permutations in the pot.table
            for (int i = 0; i < mainClassifierNode.getNumStates(); i++) {
                nrOfCasesPerm = nrOfCasesPerm + mainClassPotTableForPerm.getValue(i);                
            }
            // compute the probability of the classifiers based on pot.Table
            double[] potentials = new double[mainClassifierNode.getNumStates()];
            for (int i = 0; i < mainClassifierNode.getNumStates(); i++) {
//            	 check for not dealing with zero
                if (nrOfCasesPerm != 0)
                    potentials[i] = mainClassPotTableForPerm.getValue(i) / nrOfCasesPerm;
                else
                    potentials[i] = 0;
            }            
            
            // initialize the nodelist for the variables
            NodeList variables = new NodeList();
            // insert the main classifier into the variables collected
            variables.insertNode(mainClassifierNode);
            // insert the parent set into the variables connected
            for (int i = 0; i < mainClassParentList.size(); i++) {
                variables.insertNode(mainClassParentList.elementAt(i));
            }
            // put the mainClassifier and its parent set into the configuration
            Configuration confMainClassifier = new Configuration(variables);

            // set the values of the current parent permutation
            for (int i = 0; i < mainClassParentList.size(); i++) {
                confMainClassifier.setValue(i + 1, mainClassParentConfPerm.getValue(i));
            }

            // set the mainClassifier state into the configuration
            if(!isNonZero(potentials)){
            	for (int i = 0; i < mainClassifierNode.getNumStates(); i++) {            
                    confMainClassifier.setValue(0, i);
                    mainClassPotential.setValue(confMainClassifier, potentials[i]);//hier gaat het mis
            	}
            }
            //newCase.putValue(getNewNodeName(newIncrement.getClassVar()), value);
            int test2 = 2; // TODO later weggooien
        }
        int test1 = 0; // TODO later weggooien
    }

    /**
     * Check if the given double array has non-zero values
     * @param potentials array of potentials
     * @return the boolean value that is true if all the values in the potential are equal to 0
     */
    private boolean isNonZero(double[] potentials) {        
        
        // check if the values are either all equal to zero or not
        for(int i = 0; i < potentials.length; i++) {
            if(potentials[i] != 0) {
                return false;   
            }
        }                   
        return true;
    }

    /**
     * Create the test database case by computing all values once again.
     * @param dt The test cases of one naive bayes network. 
     */
    @SuppressWarnings("unchecked")
    private void extendTestDataBase(DataBaseCases dt) throws InvalidEditException {
        List<Node> newNodes = new ArrayList<Node>(cases.getNodeList().getNodes());
        
        extendCases(dt, newNodes, new CaseExtender() {
            public void fill(Configuration oldCase, Configuration newCase) {
                // set the values for the nodes into the test database
                for (Iterator iter = nodeListNB.getNodes().iterator(); iter.hasNext();) {
                    Node node = (Node)iter.next();
                    // get the value of the variable in the test database
                    int value = newCase.getValue(node.getName());
                    // assume that the classifier variable is the last variable
                    if (iter.hasNext()) {
                        // copy the value to all increments
                        for (int j = 0; j < increments.size(); j++) {
                            newCase.putValue(getNodeName(node, j), value);
                        }
                    } else {
                        // the main classifier is filled with the current classifier value
                        newCase.putValue(mainClassifierNode.getName(), value);
                        // and compute the classifier value for all increments
                        for (int j = 0; j < increments.size(); j++) {
                            int classValueInc = getClassifierValue(increments.get(j), oldCase);
                            newCase.putValue(getNodeName(node, j), classValueInc); // ??
                        }
                    }
                }
            }});        
    }    
    
    /**
     * Execute variable elimination for Naive_Bayes classifier
     * @param classifier Naive_Bayes classifier for variable elimination
     * @param varElThres threshold value for the potential for elimination
     */
    private static void varElimination(Naive_Bayes classifier, double varElThres) {
        // define variable elimination with potential trees the naive_Bayes classification models
        VEWithPotentialTree veWithPotentialTree = new VEWithPotentialTree(classifier.classifier);
        // set the threshold value for variable elimination
        veWithPotentialTree.setThresholdForPrunning(varElThres);
        // propagate the naive_Bayes network             
        veWithPotentialTree.propagate();
    }    
    
    /**
     * compute the conflict measure of a naive_Bayes given some observations
     * @param naive_Bayes Naive_Bayes classifier
     * @param confObs configuration of the observations of one sample
     * @param denom the denominator of the conflict measure, if it is 0, conflict measure is not
     *                 defined and we have a inconsistent observation
     * @return the value of the conflict measure
     */
    private double getConflictMeasure(Naive_Bayes naive_Bayes, Configuration confObs) {
        double conflictMeasure = 0;
        double numerator = 1, denominator;
                        
        // compute the numerator of the conflict measure        
        numerator = getNumeratorConfMeas(naive_Bayes, confObs, numerator);
        // compute the denominator of the conflict measure
        denominator = getDenominatorConfMeas(naive_Bayes, confObs);        
        // check if the conflict measure is defined
        if (denominator == 0) {
            System.out.println("The conflict measure is not defined, if " +
                    "its denominator is equal to 0");
            System.exit(0);
        }
        // compute the value of the conflict measure
        conflictMeasure = Math.log(numerator / denominator);
                
        return conflictMeasure;                
    }

    private double getNumeratorConfMeas(Naive_Bayes naive_Bayes, Configuration confObs, double numerator) {
        double potential;
        // get the number of observed variables        
        int nrOfObsVariables = confObs.getVariables().size();        
        
        PotentialTable potTable = new PotentialTable(naive_Bayes.classifier.getNodeList());
         
        // loop over all the variables in the configuration
        for(int i = 0; i < nrOfObsVariables; i++) {            
            Configuration confOneObs = new Configuration();
            confOneObs.insert(confObs.getVariable(i), confObs.getValue(i));
            potential = potTable.getValue(confOneObs);            
            // compute the numerator as the product of the probabilities of 
            //    the individual observations of the set of observed random variables                
            numerator = numerator * potential; 
        }
        return numerator;
    }

    /**
     * Check if the observation in the configuration for the naive_bayes classifier
     *         is consistent or not consistent. Consistency is defined, when
     *         the denominator of the ratio of the conflict measure is non-zero.
     * @param naive_Bayes Naive_Bayes classifier
     * @param confObs the configuration of an observed sample
     * @return true if the observation is consistent
     */
    private boolean checkConsistency(Naive_Bayes naive_Bayes, Configuration confObs) {
        
        // get the denominator of the conflict measure
        double denominator = getDenominatorConfMeas(naive_Bayes, confObs);
        
        // if the denominator is non-zero we obtain consistency
        if (denominator != 0) {
            return true;
        }
        else {
            return false;
        }               
    }
    
    /**
     * Compute the denominator of the conflict measure
     * @param naive_Bayes Naive_Bayes classifier
     * @param confObs configuration of a sample
     * @return the denominator of the conflict measure
     */
    private double getDenominatorConfMeas(Naive_Bayes naive_Bayes, Configuration confObs) {
        double denominator;
        // determine if the observation is $P$-inconsistent or $P$-consistent 
        // define the potentialTable
        PotentialTable potTable = new PotentialTable(naive_Bayes.classifier.getNodeList());
        denominator = potTable.getValue(confObs);
        
        return denominator;
    }
    
    private void setClassVar() {
    	this.classVar = mainClassifierNode;
    }
    
    
    /**
     * This method tests the learned classifier given a DataBaseCases.
     * It returns the accuracy of the classifier.
     * It requires the the class variable with assigned values in the test database.
     * @param. DataBaseCases test. The test database of the classifier.
     * @returns double. The accuracy of the classifier on the <code> test <code\> dataset
     */
    public double testIncrement(DataBaseCases test) throws ClassifierException{
      DataBaseCases newtest = test;
  /*    if(!test.getVariables().equals(this.cases.getVariables()))
        newtest = this.projection(this.cases, test.getVariables());
  */

      //Check: the classifier must be trained
      if (this.classifier.isEmpty())
        throw new ClassifierException(0);

      //Check: the number of variables must be the same
      if(this.nVariables != newtest.getVariables().size())
        throw new ClassifierException(1);

      //Check: the variables must have the same number of states
    	//NodeList    nodelistTrain    = this.cases.getVariables();
     	Vector      vectorTrain      = this.cases.getRelationList();
     	Relation    relationTrain    = (Relation)vectorTrain.elementAt(0);
     	CaseListMem caselistmemTrain = (CaseListMem)relationTrain.getValues();

     	//NodeList    nodelistTest    = newtest.getVariables();
     	Vector      vectorTest      = newtest.getRelationList();
     	Relation    relationTest    = (Relation)vectorTest.elementAt(0);
     	CaseListMem caselistmemTest = (CaseListMem)relationTest.getValues();

      int nStatesTrain, nStatesTest;
      FiniteStates varStatesTrain = new FiniteStates();
      FiniteStates varStatesTest = new FiniteStates();
      for(int i= 0; i< this.nVariables; i++) {
        varStatesTrain = (FiniteStates)(caselistmemTrain.getVariables()).elementAt(i);
        varStatesTest  = (FiniteStates)(caselistmemTest.getVariables()).elementAt(i);
        nStatesTrain = varStatesTrain.getNumStates();
        nStatesTest  = varStatesTest.getNumStates();
        if(nStatesTest != nStatesTrain)
          throw new ClassifierException(2);
      }

      int nTest = newtest.getNumberOfCases();

      Vector      vector      = newtest.getRelationList();
      Relation    relation    = (Relation)vector.elementAt(0);
      CaseListMem caselistmem = (CaseListMem)relation.getValues();

      double accuracy = (double)0;      
           
      // test the classifier for all the test cases
      for (int i= 0; i< nTest; i++) {
          // take one test case
    	  Configuration confCaseTest = caselistmem.get(i);    	  
          
        int position  = confCaseTest.indexOf(mainClassifierNode);
        int assignedClass = this.assignClassIncrement(confCaseTest, position);
        
        int classVarPlace = test.getVariables().getId(mainClassifierNode);
        if (assignedClass == (int)caselistmem.getValue(i, classVarPlace))
      		accuracy = accuracy + 1;
        this.confusionMatrix.actualize((int)caselistmem.getValue(i, classVarPlace), assignedClass);
      }
      accuracy = (accuracy / (double) nTest) * (double) 100;

    	return(accuracy);
    }

    
    /**
     * This method assigns a class to the instances of the input instance. It
     * returns the identifer of the assigned class. The class variable is the
     * last variable. It uses the result of Pearl (1987) Evidential Reasoning 
     * Using Stochastic Simulation of Causal Models.
     * It is called by this.test(). 
     * @param. double[] caseTest. A unidimensional array of size: <code>nVariables<code\>. The input intance of the database.
     * @returns int. The identifier of the most probable class.
     */
    public int assignClassIncrement(Configuration confCase, int posMainClassifier) {
      //int maxClass    = -1;
      //double maxProb  = Double.MIN_VALUE;
      
      // Node classNode                     = this.classVar;
      // Relation classRelation             = this.classifier.getRelation(classNode);
      // PotentialTable classPotentialTable = (PotentialTable)classRelation.getValues();     

      Relation classRelation             = this.classifier.getRelation(mainClassifierNode);
      PotentialTable classPotentialTable = (PotentialTable)classRelation.getValues();

      NodeList nodeList = new NodeList();
      nodeList.insertNode(mainClassifierNode);
      NodeList parentNodes = mainClassifierNode.getParentNodes();
      for (int testElv = 0; testElv < parentNodes.size(); testElv++) {
    	  nodeList.insertNode(parentNodes.elementAt(testElv));
      }
      Configuration confMainCl = new Configuration(nodeList);
      for(int i = 0; i < parentNodes.size(); i++) {
    	  Node node = parentNodes.elementAt(i);
    	  int pos = confMainCl.indexOf(node);
    	  int value = confCase.getValue(node.getName());
    	  confMainCl.setValue(pos, value);    	 
      }
      //testElviraConf.setValue(0, 1);      
      int nrOfStatesMainClass = mainClassifierNode.getNumStates();
      double[] potentialsMainCl = new double[nrOfStatesMainClass];
      for (int j = 0; j < nrOfStatesMainClass; j++) {
    	  confMainCl.setValue(0, j);
    	  potentialsMainCl[j] = classPotentialTable.getValue(confMainCl); 
      }
    
      int maxID = 0;
      double maxValue = (Double)potentialsMainCl[0]; 
      for (int i = 1; i < potentialsMainCl.length; i++) {
          if ((Double)potentialsMainCl[i] > maxValue) {
              maxID = i; 
              maxValue = (Double)potentialsMainCl[i];
          }            
      }
      // return the id of the most probable value of the classifier
      return maxID;       
               
    }

    public static void main(String[] args) throws FileNotFoundException,
            IOException, elvira.InvalidEditException,
            elvira.parser.ParseException, Exception {
        /** CHECK THE NUMBER OF ARGUMENTS */
        if (args.length != 5) {
            System.out.println("Usage: file-train1.dbc file-train2.dbc"
                    + "file-test.dbc file-out.elv file-train-allData.dbc");
            System.exit(0);
        }       
        
        /** define if Laplace correction is needed */
        boolean lap = true;  

        /** learn the all database */
        FileInputStream fiAll = new FileInputStream(args[4]);
        System.out.println("The name of the file of all data: " + args[4]);
        DataBaseCases dbAll = new DataBaseCases(fiAll);
        fiAll.close();
        /** define the first naive_Bayes classifier object */
        Naive_Bayes classifierAll = new Naive_Bayes(dbAll, lap);
        classifierAll.train();
              
        /** LEARN THE DATABASE */
        
        /** learn the first database */
        FileInputStream fi1 = new FileInputStream(args[0]);
        System.out.println("The name of the first file: " + args[0]);
        DataBaseCases db1 = new DataBaseCases(fi1);
        fi1.close();
        /** define the first naive_Bayes classifier object */
        Naive_Bayes classifier1 = new Naive_Bayes(db1, lap);
        classifier1.train();
        System.out.println("Classifier for the FIRST database is learned");
        FileWriter fo_nb1 = new FileWriter("naive-bayes_1.elv");
        classifier1.getClassifier().saveBnet(fo_nb1); fo_nb1.close();
        
        
        
        /** learn the second database */
        FileInputStream fi2 = new FileInputStream(args[1]);
        System.out.println("The name of the second file: " + args[1]);
        DataBaseCases db2 = new DataBaseCases(fi2);
        fi2.close();
        
        /** define the first naive_Bayes classifier object */
        Naive_Bayes classifier2 = new Naive_Bayes(db2, lap);
        classifier2.train();
        
        System.out.println("Classifier for the SECOND database is learned");
        FileWriter fo_nb2 = new FileWriter("naive-bayes_2.elv");
        classifier2.getClassifier().saveBnet(fo_nb2); fo_nb2.close();

        
        /** TEST THE INCREMENTAL SEARCH CLASSIFIER */       
        
        FileInputStream ft = new FileInputStream(args[2]);        
        DataBaseCases dt = new DataBaseCases(ft); ft.close();
        System.out.println("The name of the test file: " + args[2]);
        
        double accuracy;
        accuracy = classifierAll.test(dt);        
        System.out.println("Naive Bayes for all data has been tested. Accuracy: " + accuracy);
        
        accuracy = classifier1.test(dt);        
        System.out.println("Naive Bayes1 has been tested. Accuracy: " + accuracy);
        
        accuracy = classifier2.test(dt);        
        System.out.println("Naive Bayes2 has been tested. Accuracy: " + accuracy);
                
        /** CONSTRUCT THE INCREMENTAL SEARCH CLASSIFICATION FOR NAIVE BAYES STRUCTURES */
         
        
        /** define if variable elimination is needed */        
//        boolean varEl = false;
//        double varElThres = 0.01;
        
        // execute variable elimination if it is required
//        if(varEl) {
            // execute variable elimination for the two naive_Bayes classifiers
//            varElimination(classifier1, varElThres);
//            varElimination(classifier2, varElThres);            
//        }                                
        
        /** call the constructor of the incremental search */              
        IncrementSearchNaiveBayes classifier = new IncrementSearchNaiveBayes(classifier1, classifier2, lap);               
        
        /** LEARN THE INCREMENTAL SEARCH INCLUDING STRUCTURE AND PARAMETER LEARNING */        
        classifier.train();
        System.out.println("Classifier has been learned");
        

        
        
        // prepare the test database according to the graphical representation of 
        // the incremental classifier
        DataBaseCases dtTest = dt.copy();
        classifier.extendTestDataBase(dtTest);               
        //compute the accuracy of the incremental classifier       
        accuracy = classifier.testIncrement(dtTest);        
        System.out.println("Incremental classifier has been tested. Accuracy: " + accuracy);
        
//        classifier.getConfusionMatrix().print();
        
        FileWriter fo = new FileWriter(args[3]);
        classifier.getClassifier().saveBnet(fo); fo.close(); 
        // the end of the program 
        System.out.println("The end of the program JOEheeee");
    }

    
    
    /**
     * Create an iterator over all possible configuration of the given
     * list of finite state nodes.
     */
    public static class ConfigurationPermutation implements Iterable<Configuration>, Iterator<Configuration> {
        /** The configuration. */
        private Configuration configuration;

        /**
         * Create the new iterator over the given list of finate statenodes.
         * @param finiteStateNodes The list of finate state nodes.
         */
        public ConfigurationPermutation(NodeList finiteStateNodes) {
            // make sure the nodelist contains only finite state nodes
            for (int i=0 ; i<finiteStateNodes.size(); i++) {
                if (!(finiteStateNodes.elementAt(i) instanceof FiniteStates)) {
                    throw new IllegalArgumentException("Node "+finiteStateNodes.elementAt(i).getName()+" (at postition "+i+" is not a FiniteStates node.");
                }
            }
            // create the base configuration
            configuration = new Configuration(finiteStateNodes);
            // set the last value to -1 (pre init state)
            configuration.setValue(configuration.getVariables().size()-1, -1);
        }

        /**
         * Increase the state for the given node index.
         */
        private void increaseState(int nodeIndex) {
            // increase the state of the last finite state node
            int state = configuration.getValue(nodeIndex);
            int maxState = configuration.getVariable(nodeIndex).getNumStates() - 1;
            // normal case
            if (state < maxState) {
                configuration.setValue(nodeIndex, state + 1);
            } else if (nodeIndex > 0) {
                // reset the state of this node to zero
                configuration.setValue(nodeIndex, 0);
                // and increase the state of the previous node
                increaseState(nodeIndex - 1);
            } else {
                // we reached the highest state for the first
                // node, so we are ready with all permutations
                configuration = null;
            }
        }

        public boolean hasNext() {
            // if there is one value in the configuration
            for (int i=0; i<configuration.getVariables().size(); i++) {
                int state = configuration.getValue(i);
                int maxState = configuration.getVariable(i).getNumStates() - 1;
                // that is not at the maximum state
                if (state != maxState) {
                    // there are more configurations to come
                    return true;
                }
            }
            return false;
        }

        public Configuration next() {
            // increase the state
            increaseState(configuration.getVariables().size()-1);
            // return the configuration
            return configuration;
        }

        public void remove() {
      //      throw new NotImplementedException();
        }

        public Iterator<Configuration> iterator() {
            return this;
        }
    }

    /**
     * Extend the given cases object with the list of nodes.
     * @param dbCases The cases to be extended.
     * @param newNode The new node.
     * @param filler The class that can fill the new nodes in the config. 
     */
    public static void extendCases(DataBaseCases dbCases, Node newNode, CaseExtender filler) throws InvalidEditException {
        List<Node> newNodes = new ArrayList<Node>();
        newNodes.add(newNode);
        extendCases(dbCases, newNodes, filler);
    }

    /**
     * Extend the given cases object with the list of nodes.
     * @param bdCases The cases to be extended.
     * @param newNodes The new nodes.
     * @param extender The class that can fill the new nodes in the config. 
     */
    public static void extendCases(DataBaseCases dbCases, List<Node> newNodes, CaseExtender extender) throws InvalidEditException {
        // loop over the old cases and fill new columns
        CaseList oldCaseList = dbCases.getCases();
        // clone the old variable list, since we will be adding nodes
        oldCaseList.setVariables((Vector)oldCaseList.getVariables().clone());
        // add the missing columns of the new classifier to the database
        for (Node node : newNodes) {
            if (dbCases.getNodeList().getId(node) == -1) {
                dbCases.addNode(node);
            }
        }
        // create a new case list
        CaseList newCaseList = new CaseListMem(dbCases.getVariables());
        for (int i = 0; i < oldCaseList.getNumberOfCases(); i++) {
            Configuration oldCase = oldCaseList.get(i);
            // create a new Configuration with the values of the old case
            Configuration newCase = new Configuration(newCaseList.getVariables());
            newCase.setValues(oldCase, oldCase);
            // call the fill config to fill the other cases
            extender.fill(oldCase, newCase);
            // and this case to the new CaseList
            newCaseList.put(newCase);
        }
        // and overwrite the existing caselist with the new one
        ((Relation)dbCases.getRelationList().elementAt(0)).setValues(newCaseList);
    }

    /**
     * Helper class for the extendCases method.
     */
    private abstract interface CaseExtender {
        /**
         * Fill the values of the new variables in the given new case.
         * Note that the values of the old variables are already copied to the new case.
         * @param oldCase The old case.
         * @param newCase The new case with the new variables.
         */
        public void fill(Configuration oldCase, Configuration newCase);
    }
    
}
