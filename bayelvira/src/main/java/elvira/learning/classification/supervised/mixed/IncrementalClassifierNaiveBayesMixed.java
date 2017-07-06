package elvira.learning.classification.supervised.mixed;

import elvira.Evidence;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.PotentialTree;
import elvira.inference.clustering.MTESimplePenniless;
import elvira.inference.elimination.VariableElimination;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import elvira.Bnet;
import elvira.CaseList;
import elvira.CaseListMem;
import elvira.ContinuousCaseListMem;
import elvira.Configuration;
import elvira.ContinuousConfiguration;
import elvira.Continuous;
import elvira.FiniteStates;
import elvira.InvalidEditException;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.fusion.Fusion;
import elvira.inference.elimination.VEWithPotentialTree;
import elvira.learning.classification.ClassifierException;
import elvira.potential.Potential;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.ContinuousProbabilityTree;
import elvira.potential.MixtExpDensity;
import elvira.tools.ContinuousFunction;
import elvira.tools.LinearFunction;
import elvira.tools.QuadraticFunction;
import elvira.potential.PotentialTable;
import elvira.learning.classification.AuxiliarPotentialTable;
import elvira.Link;
import elvira.learning.MTELearning;

/**
 * Implements a mixed incremental classifier where the class variable is 
 * discrete and the others variables can be either continuous or discrete. The 
 * incremental classification consists of creating classifiers related to
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
 * @author Antonio Fernández Álvarez (afalvarez@ual.es)
 * @author Ildikó Flesch (tamtatam@yahoo.com)
 * @since 30/05/2007
 */

public class IncrementalClassifierNaiveBayesMixed extends MixedClassifierDiscriminativeLearning {
    private static final long serialVersionUID = -6386624028949005625L;
    /** Extention string used to make nodename unique. */
    private static final String NODE_NAME_EXTENSION = "_";
    /** The node list of each naive_bayes */
    private final NodeList nodeListNB;
    /** The list of all increments. */
    private final List<MTE_Naive_Bayes> increments = new ArrayList<MTE_Naive_Bayes>();
    /** The increment. */
    private MTE_Naive_Bayes newIncrement = null;
    /** The node of the main classifier. */
    private final FiniteStates mainClassifierNode; 
    /** The index of the class */
    private final int classIndex;
    
    /**
     * Constructor
     * @param base The input to learn a classifier
     * @param increment The input to learn a classifier
     * @param classIndex Position of the class variable
     * @param lap To apply the laplace correction
     * @throws IllegalStateException In case the base is not trained or the base classifier can not be cloned.
     */
    public IncrementalClassifierNaiveBayesMixed(MTE_Naive_Bayes base, MTE_Naive_Bayes increment,int classIndex,boolean lap)
            throws elvira.InvalidEditException {
        
        // start the incremental structure based on a copy of given data
        super(base.getDataBaseCases().copy(), lap);
        this.classIndex=classIndex;

        // set the title for all case nodes
        for (Enumeration e = cases.getVariables().elements(); e.hasMoreElements();) {
            Node node = (Node)e.nextElement();
            node.setTitle(node.getName());
        }
        
        // remember the class node name
        final String classNodeName = base.getClassVar().getName();
               
        // add the structure of the naive_bayes as base structure
        classifier = cloneClassifier(base.getClassifier());
   
        // set the nodeList that must be used by all the increments
        nodeListNB = classifier.getNodeList().duplicate();
                
        // create the incremental classifier by copying the naive bayes classifier                  
        mainClassifierNode = (FiniteStates)(base.getClassVar().copy());                      
   
        // set the name of the incremental classifier
        setNodeName(mainClassifierNode, "MAIN_CLASSIFIER");
      
        // add the node to the incremental classifier
        classifier.addNode(mainClassifierNode);
       
        // create the initial relation of the mainClassifier node 
        classifier.addRelation(mainClassifierNode);
      
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
                int value = newCase.getValue(classNodeName);
                newCase.putValue(mainClassifierNode.getName(), value);
                
            }}    
        );  
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
     * Create a fusion of two Bayesian Networks
     * @param net1 the first bayesian network
     * @param net2 the second bayesian network
     * @return the fusion of the two models
     */
    private Bnet fusion(Bnet net1, Bnet net2) throws Throwable{
        
        Bnet fusionNet=new Bnet();
        Node newNode;     
        Link l;
        Node h, t;
        Relation rel;
        Vector<Relation> netRL = new Vector();
       
        // Nodes fusion
        //----------------------------------------------------------
        for (int i=0;i<net1.getNodeList().size();i++){
            newNode = (Node)net1.getNodeList().elementAt(i).copy();
            fusionNet.addNode(newNode);  
        }
        for (int i=0;i<net2.getNodeList().size();i++){
            newNode = (Node)net2.getNodeList().elementAt(i).copy();
            fusionNet.addNode(newNode);  
        }
        // Links fusion
        //----------------------------------------------------------
        for (int i=0;i<net1.getLinkList().size();i++){
            l = (Link)net1.getLinkList().elementAt(i);
            t = l.getTail();
            h = l.getHead();
            t = net1.getNodeList().getNode(t.getName());
            h = net1.getNodeList().getNode(h.getName());
            try{    
                fusionNet.createLink(t,h,l.getDirected());
            }catch(Exception e){};
        }
        for (int i=0;i<net2.getLinkList().size();i++){
            l = (Link)net2.getLinkList().elementAt(i);
            t = l.getTail();
            h = l.getHead();
            t = net2.getNodeList().getNode(t.getName());
            h = net2.getNodeList().getNode(h.getName());
            try{
                fusionNet.createLink(t,h,l.getDirected());
            }catch(Exception e){};
        }
        // Relations fusion
        //----------------------------------------------------------
        for (int i=0;i<net1.getRelationList().size();i++){
            rel=(Relation)net1.getRelationList().elementAt(i);
            netRL.addElement(rel);            
        }
        for (int i=0;i<net2.getRelationList().size();i++){
            rel=(Relation)net2.getRelationList().elementAt(i);
            netRL.addElement(rel);            
        }
        fusionNet.setRelationList(netRL);   
        //---------------------------------------------------------
        return fusionNet;
    }
    
    /**
     * Link the given classifier network to the main classifier.
     * @param classifier The classifier to add to the incremental network.
     */
    private void linkToMainClassifier(Bnet classifier, String classNodeName) {
        Node classNode = classifier.getNode(classNodeName);
        // make link between main classifier and naive_bayes classifier
        try {
            classifier.createLink(mainClassifierNode, classNode);
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
    public void addIncrement(MTE_Naive_Bayes increment, boolean lap) {
        synchronized(increments) {
            if (newIncrement != null) {
                throw new IllegalStateException("Previously given increment is not trained yet.");
            }
            // remember new increment
            newIncrement = increment;  
        }
    }

    @Override
    public void structuralLearning() throws InvalidEditException, Exception {
        if (newIncrement == null) {
            throw new IllegalStateException("Add an increment first.");
        }
        // make sure the new increments is trained
        if (newIncrement.getEvaluations()== 0) {
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

        newClassifier = cloneClassifier(newIncrement.getClassifier());
   
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
                
                //Only it works with variables with one parent.
                ContinuousProbabilityTree ContPT=((PotentialContinuousPT)r.getValues()).getTree();

                setNodeName(ContPT.getVar(),getNewNodeName(ContPT.getVar()));

                if (r.getVariables().size()>1){  
                    //For each child                
                    for (int i=0;i<ContPT.getChilds().size();i++){
                        ContinuousProbabilityTree child=ContPT.getChild(i);
                       // System.out.println("CHILD="+child.getVar().getName());
                        String nam=getNewNodeName(child.getVar());
                        setNodeName(child.getVar(),nam);         
                        if (child.getVar() instanceof Continuous){
                            for (int j=0;j<child.getChilds().size();j++){
                                Vector <LinearFunction> terms=child.getChild(j).getProb().getTerms();
                                for (int k=0;k<terms.size();k++){
                                    ((Continuous)terms.elementAt(k).getVariables().elementAt(0)).setName(nam);
                                    ((Continuous)terms.elementAt(k).getVariables().elementAt(0)).setTitle(nam);
                                }
                            }
                        }
                    }  
                } 
                // change the node name itself
                setNodeName(node, getNewNodeName(node));
                newNodes.add(node);
           }
        }
       FiniteStates classVarNewIncr = (FiniteStates)(newClassifier.getNode(getNodeName(newIncrement.getClassVar(), increments.size())));

       String classNodeName=classVarNewIncr.getName();
        
        // merge the classifier structure with the new increment
        try{
            classifier=fusion(classifier, newClassifier); 
        }catch(Throwable t){System.out.println("Problems in the fusion.");}
        // link to the main classifier
        
        linkToMainClassifier(classifier, classNodeName);    
        
        return newNodes;
    }

     /**
     * Create a clone of a classifier.
     * @param classifier
     * @return Bnet with the clone of the classifier
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
        CaseListMem newCaseList = (CaseListMem) newIncrement.getDataBaseCases().getCases();
      
        for (int i = 0; i < newCaseList.getNumberOfCases(); i++) {
            // and for each case, create a new case for the main classifier network
            ContinuousConfiguration mergedNewCase = createMergedCase((ContinuousConfiguration)newCaseList.get(i));
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
                int borrar=0;
                ContinuousConfiguration classifierCase = new ContinuousConfiguration(nodeListNB);
                // read the values of the existing naive Bayes
                String classNodeNewName = getNewNodeName(newIncrement.getClassVar());
                int i=0;
                for (Node node : newNodes) {
                    i++;
                    if ( !node.getName().equals(classNodeNewName) ) {
                        // get the value of the variable in the classifier database
                        String nodeName = getNodeName(node, 0);
                        
                        if (node instanceof Continuous){
                            double value=newCase.getContinuousValue((Continuous)nodeListNB.getNode(nodeName));
                            // collect the values for the classifier computation   
                            classifierCase.getContinuousValues().setElementAt(new Double(value),classifierCase.getContinuousIndex(nodeName));
                            newCase.putValue((Continuous)node, value);
                        }
                        if (node instanceof FiniteStates){                
                             int value = newCase.getValue(nodeName);
                             // collect the values for the classifier computation
                             classifierCase.putValue(nodeName,value);
                             //put the value to the new variable in the classifier database
                             newCase.putValue((FiniteStates)node, value);   
                        }           
                    }
                }               
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
    private int getClassifierValue(MTE_Naive_Bayes classifierNB, ContinuousConfiguration naiveCase)
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
    private ContinuousConfiguration createMergedCase(ContinuousConfiguration newIncrementCase) {
        // get a list of all variables
        NodeList nodeList = this.cases.getNodeList();
        // create new case with all variables
        ContinuousConfiguration mergedNewCase = new ContinuousConfiguration(nodeList);
      
        // loop over the values of the variables in newincrement
        
        //FINITE STATES
        //=============================================================================
        for (Iterator iter = newIncrementCase.getVariables().iterator(); iter.hasNext();) {
            
            Node newIncrementNode = (Node) iter.next();
            // retrieve the value of the current variable
            int value = newIncrementCase.getValue(newIncrementNode.getName());
            
            // put the value of the variable into the vector  
            mergedNewCase.putValue(newIncrementNode.getName(),value);
         
            // fill the values for the nodes of the existing variables
            // except for the classifier variable of the newIncrement            
            if ( !newIncrementNode.getName().equals(getNodeName(newIncrement.getClassVar(), 0))) {    
                // get the value of the node of the new increment
                for (int j = 0; j < increments.size(); j++) {
                    mergedNewCase.putValue(getNodeName(newIncrementNode, j), value);
                }
            } else {
                mergedNewCase.putValue(mainClassifierNode.getName(), value);
                // get the value of the node of the new increment
                for (int j = 0; j < increments.size(); j++) {
                    // compute the value of the naive bayes classifier 
                    int classifierValue = getClassifierValue(increments.get(j), newIncrementCase);                    
                    mergedNewCase.putValue(getNodeName(newIncrementNode, j), classifierValue);
                }
            }   
         
        }
        //CONTINUOUS
        //=============================================================================
        for (Iterator iter = newIncrementCase.getContinuousVariables().iterator(); iter.hasNext();) {
            Node newIncrementNode = (Node) iter.next();
            // retrieve the value of the current variable
            //double value = newIncrementCase.getValue(newIncrementNode.getName());
            double value = newIncrementCase.getContinuousValue((Continuous)newIncrementNode);
            // get the index in the nodeList
            Node node = nodeList.getNode(getNewNodeName(newIncrementNode));            
            // put the value of the variable into the vector
            mergedNewCase.putValue((Continuous)(node), value);
            
            for (int j = 0; j < increments.size(); j++) {
                node = nodeList.getNode(getNodeName(newIncrementNode, j));
                mergedNewCase.putValue((Continuous)(node), value);
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
     * Parametric learning of the actual classifier
     */
    @Override
    public void parametricLearning(){

        Vector vector = this.cases.getRelationList();
        Relation relation = (Relation)vector.elementAt(0);
        CaseListMem caselistmem = (CaseListMem)relation.getValues();
        AuxiliarPotentialTable auxPotentialTable;
        PotentialTable  potentialTable;
        PotentialContinuousPT PotContPT,PotContPT1;
        Relation rel;
        NodeList list;
           
        //Learning for each child of MAIN_CLASSIFIER
        //===================================================================================================== 
        NodeList nl=classVar.getChildrenNodes();
        for (int i=0;i<nl.size();i++){   
            
            Node class_i=(Node)nl.getNodes().elementAt(i);
            
            auxPotentialTable = new AuxiliarPotentialTable((FiniteStates)class_i);
            auxPotentialTable.initialize(0);    
              
            for(int j = 0; j< this.cases.getNumberOfCases(); j++){   
                int v_child=(int)caselistmem.getValue(j, classifier.getNodeList().getId(class_i));
                int v_parent=(int)caselistmem.getValue(j, classifier.getNodeList().getId(classVar));
                auxPotentialTable.addCase(v_child, v_parent, 1);
                }
            if (this.laplace) 
               auxPotentialTable.applyLaplaceCorrection();            
             
            Vector vars=new Vector();
            vars.add((FiniteStates)class_i);
            vars.add((FiniteStates)classVar);
            
            potentialTable=new PotentialTable(vars);
            potentialTable.setValues(auxPotentialTable.getPotentialTableCases());  
                    
            PotentialTree potentTree=new PotentialTree(potentialTable);
            PotentialContinuousPT potContPT=new PotentialContinuousPT(potentTree);
            this.classifier.getRelation(class_i).setValues(potContPT);
        }  

        //Learning of MAIN_CLASSIFIER
        //===================================================================================================== 
        auxPotentialTable = new AuxiliarPotentialTable((FiniteStates)classVar);
        auxPotentialTable.initialize(0);
        
        for(int i = 0; i< this.cases.getNumberOfCases(); i++) {
             auxPotentialTable.addCase((int)caselistmem.getValue(i, classifier.getNodeList().getId(classVar)), 0, 1);
        } 
        if (this.laplace) 
            auxPotentialTable.applyLaplaceCorrection();
        
        potentialTable=new PotentialTable(classVar);
        potentialTable.setValues(auxPotentialTable.getPotentialTableCases());
        
        PotentialTree potentTree=new PotentialTree(potentialTable);
        PotentialContinuousPT potContPT=new PotentialContinuousPT(potentTree);
        this.classifier.getRelation(mainClassifierNode).setValues(potContPT);     
        //===================================================================================================== 
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
            public void fill(ContinuousConfiguration oldCase, ContinuousConfiguration newCase) {
                // set the values for the nodes into the test database
                for (int i=0;i<nodeListNB.getNodes().size();i++){
                    Node node=nodeListNB.elementAt(i);
                    if (node instanceof FiniteStates){
                        //get the value of the variable in the test database
                        int value = newCase.getValue(node.getName());
                        if (node.getName().compareTo(nodeListNB.elementAt(classIndex).getName())==0)
                        {    
                             // the main classifier is filled with the current classifier value
                            newCase.putValue(mainClassifierNode.getName(), value);
                            // and compute the classifier value for all increments
                            for (int j = 0; j < increments.size(); j++) {
                                int classValueInc = getClassifierValue(increments.get(j), oldCase);
                                newCase.putValue(getNodeName(node, j), classValueInc); 
                            }   
                        }
                        else 
                        {    
                            // copy the value to all increments
                            for (int j = 0; j < increments.size(); j++) 
                                newCase.putValue(getNodeName(node, j), value);
                        }                            
                    }
                    else if (node instanceof Continuous) {
                       double value = newCase.getContinuousValue((Continuous)node);
                            // copy the value to all increments
                            for (int j = 0; j < increments.size(); j++) {
                                int index=newCase.getContinuousIndex(getNodeName(node, j));
                                newCase.getContinuousValues().setElementAt(new Double(value),index);
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
    public double testIncrement(DataBaseCases test) throws ClassifierException{
   
        DataBaseCases newtest = test;
        int nTest = newtest.getNumberOfCases();
        Vector vector = newtest.getRelationList();
        Relation relation = (Relation)vector.elementAt(0);
        CaseListMem caselistmem = (CaseListMem)relation.getValues();

        double accuracy_test = 0.0; 
           
        // test the classifier for all the test cases
           
        //!!!!!!!!!!!!!!!!!!!!!!!!!!
        //------------------------------------------------------------
        try{
            this.classifier.saveBnet(new FileWriter("BNET.elv"));
            this.classifier=new Bnet(new FileInputStream("BNET.elv"));
        }catch(Exception e){}
        //------------------------------------------------------------
        
        for (int i= 0; i< nTest; i++) {
          // take one test case
    	  ContinuousConfiguration confCaseTest =(ContinuousConfiguration)caselistmem.get(i);  
         
          int position  = confCaseTest.indexOf(mainClassifierNode);
          int assignedClass = this.assignClassIncrement(confCaseTest, position);          
          int classVarPlace = test.getVariables().getId(mainClassifierNode);
         
          if (assignedClass == (int)caselistmem.getValue(i, classVarPlace))
                accuracy_test = accuracy_test + 1;
          //this.confusionMatrix.actualize((int)caselistmem.getValue(i, classVarPlace), assignedClass);
      }
       // this.confusionMatrix.print();
      accuracy_test = (accuracy_test / (double) nTest);
      return(accuracy_test);
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
    
      Relation classRelation = this.classifier.getRelation(mainClassifierNode);
      PotentialContinuousPT classPotentialTable = (PotentialContinuousPT)classRelation.getValues();
        
      ContinuousConfiguration confCaseAux=(ContinuousConfiguration)confCase.copy();
      //Delete class variables and its values for each increments, it's not necesary for inference
      for (int i=0;i<increments.size();i++){
         String nameClass=getNodeName(this.increments.get(i).getClassVar(), i);
         confCaseAux.remove(confCaseAux.indexOf(nameClass));    
      }
      confCaseAux.remove(mainClassifierNode);
      
      Evidence evid=new Evidence(confCaseAux);             
      VariableElimination prop=new VariableElimination(this.classifier,evid);
    
      NodeList nl=new NodeList();
      nl.insertNode(mainClassifierNode);
      prop.setInterest(nl);
      prop.obtainInterest();
      prop.propagate();
     /* try{
        prop.propagate("results.txt");
      }catch(Exception e){}
      */
      
      double d, max=-1.0;
      int imax=0;
      for (int i=0;i<mainClassifierNode.getStates().size();i++){
          
             ContinuousConfiguration conf=new ContinuousConfiguration();
             conf.putValue(mainClassifierNode,i); 
             d = ((PotentialContinuousPT)prop.results.elementAt(0)).getValue(conf);
             if (d>max) {
                 imax=i;
                 max=d;
             }
      }
      return imax;                    
    }

    public static void main(String[] args) throws FileNotFoundException,
            IOException, elvira.InvalidEditException,
            elvira.parser.ParseException, Exception {
        
        // check the number of arguments
        if (args.length != 5) {
            System.out.println("Usage: "+
            "file-train1.dbc file-train2.dbc file-test.dbc file-train-allData.dbc classIndex");
            System.exit(0);
        }       
        
        // laplace correction
        boolean lap = true;  

        // position of the class variable in all the models 
        int classIndex = Integer.valueOf(args[4]).intValue();
        
        // learn the full model
        //----------------------------------------------------------------------------
        FileInputStream fiAll = new FileInputStream(args[3]);
        System.out.println("The name of the file of all data: " + args[3]);
        DataBaseCases dbAll = new DataBaseCases(fiAll); 
        fiAll.close();
        MTE_Naive_Bayes classifierAll = new MTE_Naive_Bayes(dbAll, lap, classIndex);
        classifierAll.train();
        System.out.println("Classifier for the WHOLE database is learned");
        FileWriter fo_nbAll = new FileWriter("naive-bayes_all.elv");
       // classifierAll.getClassifier().saveBnet(fo_nbAll); fo_nbAll.close();
             
        // learn the first model
        //-----------------------------------------------------------------------------
        FileInputStream fi1 = new FileInputStream(args[0]);
        System.out.println("The name of the first file: " + args[0]);
        DataBaseCases db1 = new DataBaseCases(fi1); fi1.close();
        MTE_Naive_Bayes classifier1 = new MTE_Naive_Bayes(db1, lap, classIndex);
        classifier1.train();
        System.out.println("Classifier for the FIRST database is learned");
        FileWriter fo_nb1 = new FileWriter("naive-bayes_1.elv");
        // classifier1.getClassifier().saveBnet(fo_nb1); fo_nb1.close();
        
        // learn the second model
        //-----------------------------------------------------------------------------
        FileInputStream fi2 = new FileInputStream(args[1]);
        System.out.println("The name of the second file: " + args[1]);
        DataBaseCases db2 = new DataBaseCases(fi2); fi2.close();
        MTE_Naive_Bayes classifier2 = new MTE_Naive_Bayes(db2, lap, classIndex);
        classifier2.train();
        System.out.println("Classifier for the SECOND database is learned");
        FileWriter fo_nb2 = new FileWriter("naive-bayes_2.elv");
        //classifier2.getClassifier().saveBnet(fo_nb2); fo_nb2.close();

        // test the incremental search classifier
        //-----------------------------------------------------------------------------
        FileInputStream ft = new FileInputStream(args[2]);        
        DataBaseCases dt = new DataBaseCases(ft); ft.close();
        System.out.println("The name of the test file: " + args[2]);
       
        System.out.println("---------------------------------------------------------------------------");
        double accuracyAll,accuracyNB1,accuracyNB2,accuracyTest;
        accuracyAll = classifierAll.test(dt,classIndex);        
        accuracyNB1 = classifier1.test(dt,classIndex);     
        accuracyNB2 = classifier2.test(dt,classIndex);        
        
        
        
        // construct the incremental search classification for MTE Naive Bayes structures
        IncrementalClassifierNaiveBayesMixed classifier = new IncrementalClassifierNaiveBayesMixed(classifier1, classifier2, classIndex, lap);               
          
        // learn the incremental search including structure and parametric learning
        classifier.train();
        System.out.println("Classifier has been learned");
       
        DataBaseCases dtTest = dt.copy();
       
        classifier.extendTestDataBase(dtTest);        
        
        int posMainClassVar = classifier.classifier.getNodeList().getId("MAIN_CLASSIFIER");
        //compute the accuracy of the incremental classifier    
               
        accuracyTest = classifier.testIncrement(dtTest);  
        
       // classifier.getClassifier().saveBnet(new FileWriter("classifier.elv"));
       // classifier1.getClassifier().saveBnet(new FileWriter("classifier1.elv"));
       // classifier2.getClassifier().saveBnet(new FileWriter("classifier2.elv"));

        
        System.out.println("---------------------------------------------------------------------------");     
        System.out.println("MTE_Naive_Bayes for all data has been tested. Accuracy: " + accuracyAll);
        System.out.println("MTE_Naive_Bayes 1 has been tested. Accuracy: " + accuracyNB1);      
        System.out.println("MTE_Naive_Bayes 2 has been tested. Accuracy: " + accuracyNB2);
        System.out.println("Incremental classifier has been tested. Accuracy: " + accuracyTest);
        System.out.println("---------------------------------------------------------------------------");   
        
        System.out.println("The end of the program");
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
        ContinuousCaseListMem oldCaseList = (ContinuousCaseListMem)dbCases.getCases();
        // clone the old variable list, since we will be adding nodes
        oldCaseList.setVariables((Vector)oldCaseList.getVariables().clone());//????????
        // add the missing columns of the new classifier to the database
        for (Node node : newNodes) {
            if (dbCases.getNodeList().getId(node) == -1) {
                dbCases.addNode(node);
            }
        }
        // create a new case list
        ContinuousCaseListMem newCaseList = new ContinuousCaseListMem(dbCases.getVariables());
        
        for (int i = 0; i < oldCaseList.getNumberOfCases(); i++) {
            ContinuousConfiguration oldCase = (ContinuousConfiguration)oldCaseList.get(i).copy();
            // create a new Configuration with the values of the old case
            ContinuousConfiguration newCase = new ContinuousConfiguration(newCaseList.getVariables());
            
            Configuration conf=new Configuration(oldCase.getVariables());
            conf.setValues(oldCase.getValues());           
            ContinuousConfiguration cont_conf=new ContinuousConfiguration(oldCase.getContinuousVariables());
            cont_conf.setContinuousValues(oldCase.getContinuousValues());          
            newCase.setValues(conf,conf);
            newCase.setContinuousValues(cont_conf,cont_conf);
      
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
    
}


