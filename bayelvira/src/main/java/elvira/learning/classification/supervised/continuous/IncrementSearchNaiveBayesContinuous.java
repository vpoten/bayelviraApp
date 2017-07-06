
package elvira.learning.classification.supervised.continuous;

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
import elvira.Continuous;
import elvira.ContinuousCaseListMem;
import elvira.ContinuousConfiguration;
import elvira.Evidence;
import elvira.FiniteStates;
import elvira.InvalidEditException;
import elvira.Link;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.inference.clustering.MTESimplePenniless;
import elvira.learning.MTELearning;
import elvira.learning.classification.ClassifierException;
import elvira.potential.ContinuousProbabilityTree;
import elvira.potential.MixtExpDensity;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.PotentialTable;
import elvira.tools.ContinuousFunction;
import elvira.tools.LinearFunction;
import elvira.tools.QuadraticFunction;

/**
 * Implements a continuous incremental classifier where all variables are continuous.
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
public class IncrementSearchNaiveBayesContinuous extends ContinuousClassifierDiscriminativeLearning {
    private static final long serialVersionUID = -6386624028949005625L;
    /** Extention string used to make nodename unique. */
    private static final String NODE_NAME_EXTENSION = "_";
    /** The node list of each naive_bayes */
    private final NodeList nodeListNB;
    /** The list of all increments. */
    private final List<NaiveMTEPredictor> increments = new ArrayList<NaiveMTEPredictor>();
    /** The increment. */
    private NaiveMTEPredictor newIncrement = null;
    /** The node of the main classifier. */
    private final Continuous mainClassifierNode;
    /** The number of discretisation intervals of the continuous variables*/
    private static final int intervals = 6;
    
    /**
     * Constructor
     * @param base The input to learn a classifier
     * @param increment The input to learn a classifier
     * @param lap To apply the laplace correction
     * @throws IllegalStateException In case the base is not trained or the base classifier can not be cloned.
     */
    
    public IncrementSearchNaiveBayesContinuous(NaiveMTEPredictor base, NaiveMTEPredictor increment, boolean lap)
            throws elvira.InvalidEditException {
        // start the incremental structure based on a copy of given data
        super(base.dbCases.copy(), lap);
        // set the title for all case nodes
        for (Enumeration e = cases.getVariables().elements(); e.hasMoreElements();) {
            Node node = (Node)e.nextElement();
            node.setTitle(node.getName());
        }
        // make sure the first base is trained
        //   IT IS NOT POSSIBLE NOW IN THE NaiveMTEPredictor
        // add the structure of the naive_bayes as base structure
        classifier = cloneClassifier(base.net);
        // set the nodeList that must be used by all the increments
        nodeListNB = classifier.getNodeList().duplicate();
        // remember the last node name  
        final Continuous classVarBase = (Continuous)(base.variables.elementAt(base.classVariable));
        final String classNodeName = classVarBase.getName(); 
        // create the incremental classifier by copying the naive_bayes classifier
        //   where we copy all the properties except the sets of parents and children
        mainClassifierNode = (Continuous)(classVarBase.copy());
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
            public void fill(ContinuousConfiguration oldCase, ContinuousConfiguration newCase) {
                // and set the value to the classifier node value          
                double value = newCase.getContinuousValue(classVarBase);
                newCase.putValue(mainClassifierNode, value);
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
    public void addIncrement(NaiveMTEPredictor increment, boolean lap) {
        synchronized(increments) {
            if (newIncrement != null) {
                throw new IllegalStateException("Previously given increment is not trained yet.");
            }
            // remember new increment
            newIncrement = increment;
        }        
    }

    //@Override
    public void structuralLearning() throws InvalidEditException, Exception {
        if (newIncrement == null) {
            throw new IllegalStateException("Add an increment first.");
        }
        
        // make sure the new increments is trained TODO
        //    IT IS NOT POSSIBLE NOW IN THE NaiveMTEPredictor
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

        newClassifier = cloneClassifier(newIncrement.net);
        
        for (Iterator iter = newClassifier.getNodeList().getNodes().iterator(); iter.hasNext();) {
            Node node = (Node)iter.next();
            // change the node names in the relation
            Relation r = newClassifier.getRelation(node);
            if (r != null) {
                for (int i = 0; i < r.getVariables().size(); i++) {                	
                	Node rNode = r.getVariables().elementAt(i);
                    setNodeName(rNode, getNewNodeName(rNode));
                }
                // set the new node names in case there is a potential tree
                if (r.getValues() instanceof PotentialContinuousPT) {
                	setNewNodeNames(((PotentialContinuousPT)r.getValues()).getTree());
                }
            }
            // change the node name itself
            setNodeName(node, getNewNodeName(node));
            newNodes.add(node);
        }        
        
        // remember the class node name
        Node nodeClassVar = newIncrement.variables.elementAt(newIncrement.classVariable);
        Continuous classVarNewIncr = (Continuous)(newClassifier.getNode(getNodeName(nodeClassVar, increments.size())));
        String classNodeName = classVarNewIncr.getName();
        // merge the classifier structure with the new increment
        try {
        	classifier = fusion(classifier, newClassifier);        	
        }
        catch (Throwable t) {
            throw new IllegalArgumentException("The fusion of two continuous Bayesian" +
            		"is not applicable"+t.getMessage());
        }
        // link the new increment to the main classifier
        linkToMainClassifier(classifier, classNodeName);        
        return newNodes;
    }

    /**
     * Set the new node names in the continuous probability tree.
     * @param contPT The continuous probability tree.
     */
	private void setNewNodeNames(ContinuousProbabilityTree contPT) {
		// update the variable name of this variable
		setNewNodeName(contPT.getVar());
		// if there is a mixt exp density probability set
		if (contPT.getProb() != null) {
			MixtExpDensity density = contPT.getProb();
			// run through all the functions
			for (int i=0; i < density.getNumberOfExp(); i++ ) {
				ContinuousFunction function = density.getExponent(i);
				// and update the variable names in the coefficients
				for (int j=0; j<function.coefficients.size(); j++) {
					setNewNodeName(function.getVar1(j));
					setNewNodeName(function.getVar2(j));
				}
			}
		}
		// recurse for each child
		for (int i=0;i<contPT.getChilds().size();i++){
		    setNewNodeNames(contPT.getChild(i));
		}
	}

	/**
	 * Set the new node name in the node.
	 * @param node The node to retrieve the new name.
	 */
	private void setNewNodeName(Node node) {
		if (node != null) {
			String name = getNewNodeName(node);
			setNodeName(node, name);
		}
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
                NodeList duplicate = r.getVariables().duplicate();
                copyR.setVariables(duplicate);
                copyR.getValues().setVariables(duplicate.getNodes());
                // in case this is a classifier with continues nodes
                if (copyR.getValues() instanceof PotentialContinuousPT) {
                    // make sure we duplicate the case vars
	                cloneContinuousPotential(((PotentialContinuousPT)copyR.getValues()).getTree());
                }
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
     * Duplicate the nodes in the probability tree.
     * @param contPT The probability tree.
     */
	private void cloneContinuousPotential(ContinuousProbabilityTree contPT) {
		// make a copy of the variable
		if (contPT.getVar() != null) {
			contPT.setVar(contPT.getVar().copy());
		}
		// make a duplicate of the value
		if (contPT.getProb() != null) {
			MixtExpDensity densityClone = contPT.getProb().duplicate();
			contPT.assignProb(densityClone);
			// run through all the functions (a.k.a. terms)
			for (int i=0; i < densityClone.getTerms().size(); i++ ) {
				ContinuousFunction function = (ContinuousFunction)densityClone.getTerms().elementAt(i);
				//TODO: THIS IS EXTREMELY DANGEROUS AND UGLY. PLEASE MAKE SURE THAT
				//      LinearFunction.duplicate() AND QuadraticFunction.duplicate() 
				//      MAKE A *** DUPLICATE *** OF OF THE VARIABLES.
				if (function instanceof LinearFunction) {
					LinearFunction uglyHack = (LinearFunction)function;
					Vector nodeClone = new NodeList(uglyHack.getVariables()).duplicate().getNodes();
					function = new LinearFunction(nodeClone, uglyHack.coefficients);
				} else if (function instanceof QuadraticFunction) {
					QuadraticFunction uglyHack = (QuadraticFunction)function;
					Vector nodeClone1 = new NodeList(uglyHack.getVariables()).duplicate().getNodes();
					Vector nodeClone2 = new NodeList(uglyHack.getVariables2()).duplicate().getNodes();
					function = new QuadraticFunction(nodeClone1, nodeClone2, uglyHack.coefficients);
				}
				densityClone.getTerms().set(i, function);
			}
		}
		// for each child
		for (int i=0;i<contPT.getChilds().size();i++){
			// copy the child because child.copy() at the root does not copy the children
			ContinuousProbabilityTree childCopy = contPT.getChild(i).copy();
			contPT.setChild(childCopy, i);
			cloneContinuousPotential(childCopy);
		}
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
        CaseList newCaseList = (CaseList) newIncrement.dbCases.getCases();
        for (int i = 0; i < newCaseList.getNumberOfCases(); i++) {
            // and for each case, create a new case for the main classifier network
        	ContinuousConfiguration mergedNewCase = createMergedCase((ContinuousConfiguration)(newCaseList.get(i)));
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
            public void fill(ContinuousConfiguration oldCase, ContinuousConfiguration newCase) {
                // create a new classifier case to find the class value
                ContinuousConfiguration classifierCase = new ContinuousConfiguration(nodeListNB);
                // read the values of the existing naive Bayes
                Continuous classVarNewIncr = (Continuous)(newIncrement.variables.elementAt(newIncrement.classVariable));
                String classNodeNewName = getNewNodeName(classVarNewIncr);
                for (Node node : newNodes) {
                    if ( !node.getName().equals(classNodeNewName) ) {
                        // get the value of the variable in the classifier database
                        String nodeName = getNodeName(node, 0);
                        Continuous nodeBases = (Continuous)nodeListNB.getNode(nodeName);
                        double value = newCase.getContinuousValue(nodeBases);
                        // collect the values for the classifier computation                        
                        classifierCase.putValue((Continuous)(nodeListNB.getNode(nodeName)), value);
                        // put the value to the new variable in the classifier database
                        newCase.putValue((Continuous)(node), value);
                    }
                }
                // classifier computation
                double value = getClassifierValue(newIncrement, classifierCase);
                // get the renamed classnode of the new increment
                classVarNewIncr = (Continuous)(newIncrement.variables.elementAt(newIncrement.classVariable));
                // copy the continuous variable and rechange the name of the copy to 
                // to find it in hte newCase.
                // we only need to do this, because otherwise there are not enough 
                // methods to find the classifier with hte new name in the newCase
                Continuous tmpNode = (Continuous)classVarNewIncr.copy();
                tmpNode.setName(getNewNodeName(classVarNewIncr));                
                int indextmp = newCase.indexOf(tmpNode);
                Node node = newCase.getContinuousVariable(indextmp);
                // set the value of the new classifier into the configuration
                newCase.putValue((Continuous)(node), value);
            }});
    }

    /**
     * Compute the value of a classifier, given its children 
     * @param classifier is the naive _bayes classifier for which we want to obtain a classification value
     * @param naiveCase contains the values of the children of the classifier in the Naive_Bayes
     * @return the id of the most probable value of the classifier
     */
    private double getClassifierValue(NaiveMTEPredictor classifierNB, ContinuousConfiguration naiveCase)
    { 
    	//change the ordering of the variables of the configuration
    	//according to the ordering of the variables in the ClassifierNB
    	ContinuousConfiguration naiveCaseMTE = new ContinuousConfiguration(classifierNB.variables);
    	// put the values of the naiveCase into naiveCaseMTE
    	for (int i = 0; i < classifierNB.variables.size(); i++) {
            Continuous node = (Continuous)(classifierNB.variables.elementAt(i));
            double value = naiveCase.getContinuousValue(node);
            naiveCaseMTE.putValue(node, value);
    	}
    	Continuous classVarClassNB = (Continuous)(classifierNB.variables.elementAt(classifierNB.classVariable));
    	// make a copy of the naiveCaseMTE, because in naiveMTEPredictor the classvariable
    	// is removed
    	ContinuousConfiguration naiveCaseMTECopy = (ContinuousConfiguration)naiveCaseMTE.copy();
    	Vector classVector =  classifierNB.predictWithMean(naiveCaseMTECopy, classVarClassNB);
                
    	// get the exact value of the class variable that is at the first position
        return (Double)(classVector.get(0));       
    }
    
    /**
     * Create a new merged case with all variables for the classifier network.
     *
     * @param newIncrementCase One case of the new increment.
     * @return A merged case for the entire classifier network.
     */
    private ContinuousConfiguration createMergedCase(ContinuousConfiguration newIncrementCase) {
        // get a list of all variables
        NodeList nodeList = this.cases.getNodeList();
        // create new case with all variables
        ContinuousConfiguration mergedNewCase = new ContinuousConfiguration();//new ContinuousConfiguration(nodeList);
        // define Node object node
        Node node;
        // loop over the values of the variables in newincrement
        for (Iterator iter = newIncrementCase.getContinuousVariables().iterator(); iter.hasNext();) {
            Node newIncrementNode = (Node) iter.next();
            // retrieve the value of the current variable
            double value = newIncrementCase.getContinuousValue((Continuous)newIncrementNode);
            // get the index in the nodeList
            node = nodeList.getNode(getNewNodeName(newIncrementNode));            
            // put the value of the variable into the vector
            mergedNewCase.putValue((Continuous)(node), value);
            
            // fill the values for the nodes of the existing variables
            // except for the classifier variable of the newIncrement            
            Continuous classVarNewIncr = (Continuous)(newIncrement.variables.elementAt(newIncrement.classVariable));
            if ( !newIncrementNode.getName().equals(getNodeName(classVarNewIncr, 0))) {    
                // get the value of the node of the new increment
                for (int j = 0; j < increments.size(); j++) {
                	node = nodeList.getNode(getNodeName(newIncrementNode, j));
                    mergedNewCase.putValue((Continuous)(node), value);
                }
            } else {
                // the main classifier is set to the value of the
                		// newIncrement classifier
            	node = nodeList.getNode(mainClassifierNode.getName());
            	mergedNewCase.putValue((Continuous)(node), value);
                // get the value of the node of the new increment
                for (int j = 0; j < increments.size(); j++) {
                	node = nodeList.getNode(getNodeName(newIncrementNode, j));
                    // compute the value of the naive bayes classifier 
                    double classifierValue = getClassifierValue(increments.get(j), newIncrementCase);                    
                    mergedNewCase.putValue((Continuous)(node), classifierValue);
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
    //@Override
    public void parametricLearning() {    	        	       
        // get the parent set of the main classifier node
        //  note: they are exactly the classifiers of the increments cases
        NodeList mainClassParentList = mainClassifierNode.getParentNodes();

        // create a MTELearning object for learning         
        MTELearning learningObject = new MTELearning(this.cases);    	
    	// learn the density of the mainClassifier given its parentlist
        ContinuousProbabilityTree probTree;
        probTree = learningObject.learnConditional(mainClassifierNode, mainClassParentList, this.cases, 2, 4);
        
    	// initialize the nodelist for the variables
        NodeList variables = new NodeList();
        // insert the main classifier into the variables collected
        variables.insertNode(mainClassifierNode);
        // insert the parent set into the variables connected
        for (int i = 0; i < mainClassParentList.size(); i++) {
            variables.insertNode(mainClassParentList.elementAt(i));
        }
                
        // get the continuous potential table of the mainclassifier and its parents
        PotentialContinuousPT pot = new PotentialContinuousPT(variables, probTree);
        
        // add the obtained relation of the classifier into the relationlist
        classifier.getRelation(mainClassifierNode).setValues(pot);//removeRelation(mainClassifierNode);                          
    }

 
    /**
     * Create the test database case by computing all values once again.
     * @param dt The test cases of one naive bayes network. 
     */
    @SuppressWarnings("unchecked")
    private void extendTestDataBase(DataBaseCases dt) throws InvalidEditException {
        List<Node> newNodes = new ArrayList<Node>(cases.getNodeList().getNodes());
        
        extendCases(dt, newNodes, new CaseExtender() {
            public void fill(ContinuousConfiguration oldCase, ContinuousConfiguration newCase) {
                // set the values for the nodes into the test database
                for (Iterator iter = nodeListNB.getNodes().iterator(); iter.hasNext();) {
                    Node node = (Node)iter.next();
                    // get the value of the variable in the test database
                    double value = newCase.getContinuousValue((Continuous)node);
                    // assume that the classifier variable is the last variable
                    
                    if (!node.equals(increments.get(0).variables.elementAt(increments.get(0).classVariable))) {
                    	// copy the value to all increments
                        for (int j = 0; j < increments.size(); j++) {                        	
                            Continuous tmpNode = (Continuous)node.copy();
                            tmpNode.setName(getNodeName(node, j));
                            int tmpIndex = newCase.indexOf(tmpNode);
                        	Node nodeCont = newCase.getContinuousVariable(tmpIndex);
                            newCase.putValue((Continuous)(nodeCont), value);
                        }
                    } else {
                        // the main classifier is filled with the current classifier value
                    	newCase.putValue(mainClassifierNode, value);
                        // and compute the classifier value for all increments
                        for (int j = 0; j < increments.size(); j++) {                        	
                            double classValueInc = getClassifierValue(increments.get(j), oldCase);
                            
                            Continuous tmpNode = (Continuous)node.copy();
                            tmpNode.setName(getNodeName(node, j));
                            int tmpIndex = newCase.indexOf(tmpNode);
                        	Node nodeCont = newCase.getContinuousVariable(tmpIndex);
                            newCase.putValue((Continuous)(nodeCont), classValueInc); 
                        }
                    }
                }
            }});        
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
    public Vector testIncrement(DataBaseCases test, double bias, int posClassVar) throws ClassifierException{
      
      Vector results = predictWithMeanIncr(test, bias, posClassVar);
      
      return results;
    }

    /**
     * Computes the bias in the prediction for the incremental structure.
     * @param predictedValues a vector with the predicted values.
     * @param exactValues a vector with the exact values.
     * @return the bias of the predictions.
     */
    
    public double computeBiasIncr(Vector predictedValues, Vector exactValues) {

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
    
    public Vector predictWithMeanIncr(DataBaseCases db, int posClassVar) {
        
        ContinuousCaseListMem cases;
        ContinuousConfiguration conf;
        NodeList vars;
        Node classVar;
        Vector resultValues, registerValues, vectorMeans,  vectorModes, vectorVariances;
        Vector vectorExact;
        
        int i, nrOfCases;
        
        vars = db.getVariables();
        classVar = vars.elementAt(posClassVar);
        
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        resultValues = new Vector();
        vectorMeans = new Vector();
        vectorModes = new Vector();
        vectorVariances = new Vector();
        vectorExact = new Vector();
        
        
        nrOfCases = cases.getNumberOfCases();
        for (i=1 ; i<nrOfCases ; i++) {
            conf = (ContinuousConfiguration)cases.get(i);
            registerValues = predictWithMeanIncr(conf,classVar);
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
    
    public Vector predictWithMeanIncr(DataBaseCases db, double bias, int posClassVar) {
        
        ContinuousCaseListMem cases;
        ContinuousConfiguration conf;
        NodeList vars;
        Node classVar;
        Vector resultValues, registerValues, vectorMeans,  vectorMedians, vectorVariances;
        Vector vectorExact;
        
        int i, nrOfCases;
        
        vars = db.getVariables();
        classVar = vars.elementAt(posClassVar);
        
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        resultValues = new Vector();
        vectorMeans = new Vector();
        vectorMedians = new Vector();
        vectorVariances = new Vector();
        vectorExact = new Vector();
        
        nrOfCases = cases.getNumberOfCases();
        for (i=1 ; i<nrOfCases ; i++) {
            conf = (ContinuousConfiguration)cases.get(i);
            registerValues = predictWithMeanIncr(conf, classVar, bias);
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

    
    public Vector predictWithMeanIncr(ContinuousConfiguration conf, Node classVar, double bias) {
        
        double exactValue, mean, median, variance;
        Evidence evidence;
        MTESimplePenniless propagation;
        Vector results, returnValues;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree tree;
        
        exactValue = conf.getValue((Continuous)classVar);
        
        // Remove the class variable from the configuration
        conf.remove(classVar);
        
        evidence =  new Evidence(conf);
        
        propagation = new MTESimplePenniless(classifier, evidence, 0, 0, 0, 0, 0);
        
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
        
        
        returnValues = putElements(mean, variance, median, exactValue);        
      //  System.out.println(mean+" ; "+variance+" ; "+median+" ; "+exactValue);        
        return (returnValues);
    }
    
    /* Set the given arguments into a vector
     * 
     */
    public Vector putElements(double mean, double variance, double median, double exactValue){
    	Vector returnValues;

    	returnValues = new Vector();
        
    	returnValues.addElement(new Double(mean));
        returnValues.addElement(new Double(variance));
        //returnValues.addElement(new Double(mode));
        returnValues.addElement(new Double(median));
        returnValues.addElement(new Double(exactValue));
    	
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
     * @return a vector with the predicted value in the first position (mean of the
     * posterior distribution), the variance in the second position and the exact value
     * for the class variable in the thirs position.
     */              
    public Vector predictWithMeanIncr(ContinuousConfiguration conf, Node classVar) {
        
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
        
        propagation = new MTESimplePenniless(this.classifier, evidence, 0, 0, 0, 0, 0);
        
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
        
        
        returnValues = putElements(mean, variance, median, exactValue);        
       // System.out.println(mean+" ; "+variance+" ; "+median+" ; "+exactValue);        
        return (returnValues);
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
    public int assignClassIncrement(ContinuousConfiguration confCase, int posMainClassifier) {
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
      ContinuousConfiguration confMainCl = new ContinuousConfiguration(nodeList);
      for(int i = 0; i < parentNodes.size(); i++) {
    	  Node node = parentNodes.elementAt(i);
    	  int pos = confMainCl.indexOf(node);
    	  double value = confCase.getValue(node.getName());
    	  confMainCl.putValue((Continuous)(node), value);    	 
      }
      //testElviraConf.setValue(0, 1);      
      //int nrOfStatesMainClass = mainClassifierNode.getNumStates();
      //double[] potentialsMainCl = new double[nrOfStatesMainClass];
      double[] potentialsMainCl = new double[intervals];
      for (int j = 0; j < intervals; j++) {
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

    /**
     * Computes the rooted mean squared error (RMSE) of the predicted values as well as the
     * linear correlation coefficient (LCC) between the predicted values and the exact ones.
     * @param predictedValues a vector with the predicted values.
     * @param exactValues a vector with the exact values.
     * @return a vector with the RMSE in the first position and the LCC in the second.
     */
    
    public static Vector computeErrorsIncr(Vector predictedValues, Vector exactValues) {

        int i, n;
        double meanPredicted = 0.0, meanExact = 0.0, x, y, sumProd = 0.0, sumCuadX = 0.0;
        double sumCuadY = 0.0, lcc, rmse, dif = 0.0, sx, sy, sxy;
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
     * This method builds a new classifier.
     * First, it makes a structural learning of the classifier.
     * Finally, it factorizes the probability distributions of the database.
     */
    public void train() throws elvira.InvalidEditException, Exception{
        this.structuralLearning();
        this.parametricLearning();
    }

    
    public static void printResultsNaiveMTEPredictor(NaiveMTEPredictor naiveMTEPredictor, Vector results, String textToPrint) {
        
    	Vector errors;
    	
    	errors = naiveMTEPredictor.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));
        System.out.println("mean of " + textToPrint);
        System.out.println("rmse = "+((Double)(errors.elementAt(0))).doubleValue()+" ; lcc = "+((Double)(errors.elementAt(1))).doubleValue());
             
        errors = naiveMTEPredictor.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));
        System.out.println("median of " + textToPrint);
        System.out.println("rmse = "+((Double)(errors.elementAt(0))).doubleValue()+" ; lcc = "+((Double)(errors.elementAt(1))).doubleValue());
    	
    }

    
    public static void printResultsIncremental(Vector results, String textToPrint) {     

    	Vector errors;

    	errors = computeErrorsIncr((Vector)results.elementAt(0),(Vector)results.elementAt(3));
    	System.out.println("mean of " + textToPrint);
    	System.out.println("rmse = "+((Double)(errors.elementAt(0))).doubleValue()+" ; lcc = "+((Double)(errors.elementAt(1))).doubleValue());
        
    	errors = computeErrorsIncr((Vector)results.elementAt(2),(Vector)results.elementAt(3));
    	System.out.println("median of " + textToPrint);
    	System.out.println("rmse = "+((Double)(errors.elementAt(0))).doubleValue()+" ; lcc = "+((Double)(errors.elementAt(1))).doubleValue());
    }
    
    public static Vector trainTestNaiveMTEPredictor(String nameTrainFile, String nameTestFile, int posClassVar) 
    throws FileNotFoundException,
    IOException, elvira.InvalidEditException,
    elvira.parser.ParseException, Exception {
    	
    	// learn the database 
        FileInputStream fi = new FileInputStream(nameTrainFile);
        DataBaseCases db = new DataBaseCases(fi);
        fi.close();
        
        // define the first naive_Bayes classifier object         
        NaiveMTEPredictor naiveMTEPredictor = new NaiveMTEPredictor(db, posClassVar, intervals);
        
        // read the test file
        FileInputStream ft = new FileInputStream(nameTestFile);        
        DataBaseCases dt = new DataBaseCases(ft); ft.close();

        // test the accuracy of the first and second Naive Bayes classifier
        Vector results;
        double bias;

        results = naiveMTEPredictor.predictWithMean(db);
        bias = naiveMTEPredictor.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));                               
        results = naiveMTEPredictor.predictWithMean(dt,bias);
    	
    	Vector returnObjects = new Vector();
    	returnObjects.add(naiveMTEPredictor);
    	returnObjects.add(results);
    
        return returnObjects;
    }

    public static void main(String[] args) throws FileNotFoundException,
            IOException, elvira.InvalidEditException,
            elvira.parser.ParseException, Exception {
        
    	/** CHECK THE NUMBER OF ARGUMENTS */
        if (args.length != 5) {
            System.out.println("Usage: file-train1.dbc file-train2.dbc"
                    + "file-test.dbc file-out.elv file-all-data.dbc");
            System.exit(0);
        }       
                              
        // define if Laplace correction is needed 
        boolean lap = true;          
        // give the position of the classifier in the database
        int posClassVar = 11; // TODO to change this                      

        // read the test file
        FileInputStream ft = new FileInputStream(args[2]);        
        DataBaseCases dt = new DataBaseCases(ft); ft.close();
        
        // learn the first database 
        FileInputStream fi1 = new FileInputStream(args[0]);
        DataBaseCases db1 = new DataBaseCases(fi1);
        fi1.close();
                
        // define the first naive_Bayes classifier object         
        NaiveMTEPredictor naiveMTEPredictor1 = new NaiveMTEPredictor(db1, posClassVar, intervals);
        
        // learn the second database
        FileInputStream fi2 = new FileInputStream(args[1]);
        DataBaseCases db2 = new DataBaseCases(fi2);
        fi2.close();
       
        // define the second naive_Bayes classifier object 
        NaiveMTEPredictor naiveMTEPredictor2 = new NaiveMTEPredictor(db2, posClassVar, intervals);
        
        
        // test the accuracy of the first and second Naive Bayes classifier
        Vector results1;
        Vector results2;
        double bias1, bias2;

        results1 = naiveMTEPredictor1.predictWithMean(db1);
        bias1 = naiveMTEPredictor1.computeBias((Vector)results1.elementAt(0),(Vector)results1.elementAt(3));                               
        results1 = naiveMTEPredictor1.predictWithMean(dt,bias1);
        
        results2 = naiveMTEPredictor2.predictWithMean(db2);
        bias2 = naiveMTEPredictor2.computeBias((Vector)results2.elementAt(0),(Vector)results2.elementAt(3));                               
        results2 = naiveMTEPredictor2.predictWithMean(dt,bias2);
             
        
        /*Vector returnObjects1 = trainTestNaiveMTEPredictor(args[0], args[2], posClassVar);
        NaiveMTEPredictor naiveMTEPredictor1 = (NaiveMTEPredictor) returnObjects1.get(0);
        Vector results1 = returnObjects1.get(0);
        
        Vector returnObjects2 = trainTestNaiveMTEPredictor(args[0], args[2], posClassVar);
        NaiveMTEPredictor naiveMTEPredictor2 = (NaiveMTEPredictor) returnObjects2.get(0);
        Vector results2 = (Vector) returnObjects2.get(0);
        */
        
        /** CONSTRUCT THE INCREMENTAL SEARCH CLASSIFICATION FOR NAIVE BAYES STRUCTURES */                
        IncrementSearchNaiveBayesContinuous classifier = new IncrementSearchNaiveBayesContinuous(naiveMTEPredictor1,  naiveMTEPredictor2, lap);
        
        /** LEARN THE INCREMENTAL SEARCH INCLUDING STRUCTURE AND PARAMETER LEARNING */        
        // learn the incremental classifier and
        classifier.train();        
        // save the learned incremental classifier into a file
        FileWriter fo = new FileWriter(args[3]);
        classifier.getClassifier().saveBnet(fo); fo.close(); 
        
        /** TEST THE INCREMENTAL SEARCH CLASSIFIER */       
        Vector results;
        double bias;

        // compute the bias of the incremental network
        DataBaseCases dbExtended = classifier.getDataBaseCases().copy();
        
        
        // prepare the test database according to the graphical representation of 
		// the incremental classifier
        DataBaseCases dtTest = dt.copy();
        classifier.extendTestDataBase(dtTest); 
        
        // get the position of the main classifier node
        int posMainClassVar = classifier.classifier.getNodeList().getId("MAIN_CLASSIFIER");
        // compute the accuracy of the classifier
        results = classifier.predictWithMeanIncr(dbExtended, posMainClassVar);        
        bias = classifier.computeBiasIncr((Vector)results.elementAt(0),(Vector)results.elementAt(3));

        // compute the accuracy of the incremental classifier
        Vector resultsTest = classifier.testIncrement(dtTest, bias, posMainClassVar);
        
        //classifier.getConfusionMatrix().print();
        
        /** Get the accuracy of one NaiveMTE for all the database*/        
        FileInputStream fiAll = new FileInputStream(args[4]);
        DataBaseCases dbAll = new DataBaseCases(fiAll);
        fiAll.close();
        
        /** define the naive_Bayes MTE classifier object */
        NaiveMTEPredictor naiveMTEPredictorAll = new NaiveMTEPredictor(dbAll, posClassVar, intervals);
        System.out.println("Classifier for all the data is learned");                       
        
        // test the accuracy of the first and second Naive Bayes classifier
        Vector resultsAll;
        double biasAll;

        // compute the accuracy of the model
        resultsAll = naiveMTEPredictorAll.predictWithMean(dbAll);
        biasAll = naiveMTEPredictorAll.computeBias((Vector)resultsAll.elementAt(0),(Vector)resultsAll.elementAt(3));                               
        resultsAll = naiveMTEPredictorAll.predictWithMean(dt,biasAll);              
        
        // print the mean and the median for:
        // 		the first naive MTE 
        printResultsNaiveMTEPredictor(naiveMTEPredictor1, results1, "the first naiveMTE");       
        // 		the second naive MTE 
        printResultsNaiveMTEPredictor(naiveMTEPredictor2, results2, "the second naiveMTE");
        //		the incremental naive MTE 
        printResultsIncremental(resultsTest, "the INCREMENTAL MTE");
        // 		the naive MTE for the entire data
        printResultsNaiveMTEPredictor(naiveMTEPredictorAll, resultsAll, "the NaiveMTE of all the data");
        
    }

    
    
    /**
     * Create an iterator over all possible configuration of the given
     * list of finite state nodes.
     */
    public static class ContinuousConfigurationPermutation implements Iterable<ContinuousConfiguration>, Iterator<ContinuousConfiguration> {
        /** The configuration. */
        private ContinuousConfiguration configuration;

        /**
         * Create the new iterator over the given list of finate statenodes.
         * @param finiteStateNodes The list of finate state nodes.
         */
        public ContinuousConfigurationPermutation(NodeList continuousNodes) {
            // make sure the nodelist contains only continuous state nodes
            for (int i=0 ; i<continuousNodes.size(); i++) {
                if (!(continuousNodes.elementAt(i) instanceof FiniteStates)) {
                    throw new IllegalArgumentException("Node "+continuousNodes.elementAt(i).getName()+" " +
                    		"(at postition "+i+" is not a Continuous node.");
                }
            }
            // create the base configuration
            configuration = new ContinuousConfiguration(continuousNodes);
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

        public ContinuousConfiguration next() {
            // increase the state
            increaseState(configuration.getVariables().size()-1);
            // return the configuration
            return configuration;
        }

        public void remove() {
        //    throw new NotImplementedException();
        }

        public Iterator<ContinuousConfiguration> iterator() {
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
        //CaseList oldCaseList = dbCases.getCases();
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
        //CaseList newCaseList = new CaseListMem(dbCases.getVariables());
        ContinuousCaseListMem newCaseList = new ContinuousCaseListMem(dbCases.getVariables());
        
        for (int i = 0; i < oldCaseList.getNumberOfCases(); i++) {
        	//Configuration test = oldCaseList.get(i);
        	ContinuousConfiguration oldCase = (ContinuousConfiguration)(oldCaseList.get(i));
            // create a new Configuration with the values of the old case
        	ContinuousConfiguration newCase = new ContinuousConfiguration(newCaseList.getVariables());        	
        	
        	//Vector variables = oldCase.getVariables();
        	Vector variables = oldCase.getContinuousVariables();        	
            for(int j = 0; j < variables.size(); j++) {
            	Continuous node = (Continuous)(variables.get(j));            	
            	//double value = oldCase.getValue(node.getName());
            	double value = oldCase.getContinuousValue(node);
            	newCase.putValue(node, value);            	
            }               
            //newCase .setValues(oldCase, oldCase); 
            //newCase.setValueConfiguration(oldCase, oldCase);
            //newCase.putValue(mainClassifierNode.getName(), value)
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
        public void fill(ContinuousConfiguration oldCase, ContinuousConfiguration newCase);
    }
 
    /**
     * Create a fusion of two Bayesian Networks
     * @param net1 the first bayesian network
     * @param net2 the second bayesian network
     * @return the fusion of the two models
     */

    public Bnet fusion(Bnet net1, Bnet net2) throws Throwable{
        
        Bnet fusionNet=new Bnet();
        Node newNode;     
        Link l;
        Node h, t;
        Relation rel;
        Vector<Relation> netRL = new Vector();
       
        // Nodes fusion
        //----------------------------------------------------------
        for (int i=0;i<net1.getNodeList().size();i++){
            newNode = (Node)(net1.getNodeList()).duplicate().elementAt(i);
            fusionNet.addNode(newNode);  
        }
        for (int i=0;i<net2.getNodeList().size();i++){
            newNode = (Node)(net2.getNodeList()).duplicate().elementAt(i);
            fusionNet.addNode(newNode);  
        }
        // Links fusion
        //----------------------------------------------------------
        for (int i=0;i<net1.getLinkList().size();i++){
            l = (Link)net1.getLinkList().elementAt(i);
            t = l.getTail();
            h = l.getHead();
            t = (net1.getNodeList()).duplicate().getNode(t.getName());
            h = (net1.getNodeList()).duplicate().getNode(h.getName());
            try{    
                fusionNet.createLink(t,h,l.getDirected());
            }catch(Exception e){};
        }
        for (int i=0;i<net2.getLinkList().size();i++){
            l = (Link)net2.getLinkList().elementAt(i);
            t = l.getTail();
            h = l.getHead();
            t = (net2.getNodeList()).duplicate().getNode(t.getName());
            h = (net2.getNodeList()).duplicate().getNode(h.getName());
            try{
                fusionNet.createLink(t,h,l.getDirected());
            }catch(Exception e){};
        }
        // Relations fusion
        //----------------------------------------------------------
        for (int i=0;i<net1.getRelationList().size();i++){
            rel=(Relation)net1.getRelationList().elementAt(i);
            netRL.addElement(rel.copy());            
        }
        for (int i=0;i<net2.getRelationList().size();i++){
            rel=(Relation)net2.getRelationList().elementAt(i);
            netRL.addElement(rel.copy());            
        }
        fusionNet.setRelationList(netRL);   
        //---------------------------------------------------------
        return fusionNet;
    }
    
    
    /*
     * This merge two Bayesian networks into one.
     */
    public Bnet FusionContinuous(Bnet bnet1, Bnet bnet2) {
    	Bnet returnBnet;   
    	Node newNode, node;
    	
    	try {
    		// copy the first Bayesian network into the resulting Bayesian network
    		returnBnet = cloneClassifier(bnet1);     			
    		
    		// copy the nodes from the second Bayesian network into the resulting Bayesian network
    		for (int i = 0 ; i < bnet2.getNodeList().size() ; i++) {
    			node = (Node)bnet2.getNodeList().elementAt(i);
    		    newNode = node.copy();
    		    returnBnet.addNode(newNode);
    		}
    		
    		// copy the links of the second Bayesian network into the resulting Bayesian network
    		Link link;
    		Node head, tail;
    		for (int j = 0 ; j < bnet2.getLinkList().size() ; j++) {
    		   link = (Link)bnet2.getLinkList().elementAt(j);
    		   tail = link.getTail();
    		   head = link.getHead();
    		   
    		   tail = returnBnet.getNodeList().getNode(tail.getName());
    		   head = returnBnet.getNodeList().getNode(head.getName());
    		   returnBnet.createLink(tail, head, link.getDirected());
    		}
    		
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
        } catch (Throwable t) {
            throw new IllegalArgumentException("The fusion of two continuous Bayesian" +
            		"is not applicable"+t.getMessage());
        }
    	
    	return returnBnet;
    }
    
    
}
