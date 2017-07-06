package elvira.learning.classification.unsupervised.mixed;


import java.io.*;
import java.util.*;

import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.parser.ParseException;
import elvira.potential.*;
import elvira.inference.clustering.MTESimplePenniless;
import elvira.inference.elimination.VariableElimination;


/**
 * Implements an unsupervised naive bayes where the class variable is discrete
 * and the other variables are discrete or continuous.
 *
 * @author Rafael Rumi (rrumi@ual.es)
 * @author Antonio.Salmeron@ual.es
 * @author Jose A. Gamez (jgamez@info-ab.uclm.es)
 * @since 8/3/2007
 */

public class UnsupervisedMTENaiveBayes {
    
    
    /**
     * The network that will contain the classifier.
     */
    
    Bnet classifier;
    
    /**
     * The training cases from which the classifier will be constructed.
     */
    
    DataBaseCases train;
    
    /**
     * The test cases for measuring the likelihood.
     */
    
    DataBaseCases test;
    
    
    // CONSTRUCTORS
    
    
    /**
     * Creates an empty classifier.
     */
    
    public UnsupervisedMTENaiveBayes() {
        
        classifier = new Bnet();
        train = new DataBaseCases();
        test = new DataBaseCases();
    }
    
    
    // METHODS
    
    /**
     * Updates the parameters of a given bnet using the EM algorithm.
     * The hidden variable must be the last column.
     *
     * @param net The network whose parameters (MTE densities) will be updated.
     * The networ is modified.
     */
    
    public void EMAlgorithm(Bnet net) throws IOException {
        
        int i,j,k;
        Vector classes, results;
        NodeList varsCases, varsNet, varClass, varsRel, parents;
        FiniteStates classNode;
        Node tempNode;
        ContinuousConfiguration conf, conf2;
        //MTESimplePenniless prop;
        VariableElimination prop;
        MTELearning learningObject;
        Evidence evidence;
        PotentialContinuousPT pot, updatedPotential = new PotentialContinuousPT();
        ContinuousProbabilityTree cpt, cpt1, updatedTree;
        double prob, max, initialLikelihood, finalLikelihood, u, acum;
        double[] sim;
        int maxK, intervals = 4;
        String sta;
        Relation rel;
        boolean found;
        
        
        learningObject = new MTELearning(train);
        
        initialLikelihood = test.logLikelihood(net);
        //initialLikelihood = -1e20;
        
        varsCases = train.getVariables();
        
        classNode = (FiniteStates)varsCases.elementAt(varsCases.size()-1);
        // Now in classNode we have the hidden variable (the class)
        sim = new double[classNode.getNumStates()];
        
        ContinuousCaseListMem casesListTrain = (ContinuousCaseListMem)train.getCaseListMem();
        ContinuousCaseListMem casesListTest = (ContinuousCaseListMem)test.getCaseListMem();
        
        i = 0;
        // Stopping criterion: 100 iterations or decrease of likelihood
        while (i<100){
            System.out.println("Iteration "+i);
            
            // E-step: we obtain the most probable value of the hidden variable for each record.
            
            for (j=0 ; j<train.getNumberOfCases() ; j++) {
                
                conf = (ContinuousConfiguration)casesListTrain.get(j);
                conf2 = (ContinuousConfiguration)conf.copy();
                conf2.remove(classNode);
                
                evidence = new Evidence(conf2);
                //prop = new MTESimplePenniless(net,evidence,0,0,0,0,0);
                prop = new VariableElimination(net,evidence);
                //prop.propagate(evidence);
                NodeList temp = new NodeList();
                temp.insertNode(classNode);
                prop.setInterest(temp);
                prop.propagate();
                results = prop.getResults();
                pot = (PotentialContinuousPT)results.elementAt(0);
                // Out of pot, we get the state of the hidden variable with highest probability.
                cpt = pot.getTree();
                
                /*max = 0.0; // Highest probability value found so far.
                maxK = 0; // Value of the hidden variable that corresponds to the maximum.
                for (k=0 ; k<classNode.getNumStates() ; k++) {
                    conf2 = new ContinuousConfiguration();
                    conf2.insert(classNode,k);
                    prob = ((MixtExpDensity)(cpt.getProb(conf2))).getIndependent();
                    if (prob > max) {
                        max = prob;
                        maxK = k;
                    }
                }//End of for
                 */
                
                for (k=0 ; k<classNode.getNumStates() ; k++) {
                    conf2 = new ContinuousConfiguration();
                    conf2.insert(classNode,k);
                    sim[k] = ((MixtExpDensity)(cpt.getProb(conf2))).getIndependent();
                }//End of for
                
                // Now, simulate a value for the class
                u = Math.random();
                k = 0;
                found = false;
                acum = 0.0;
                maxK = 0;
                while (!found) {
                    acum += sim[k];
                    if ((u<=acum) || (k==(classNode.getNumStates()-1))) {
                        maxK = k;
                        found = true;
                    } else k++;
                }
                
                
                // Now we replace the obtained value in the train database.
                conf.putValue(classNode,maxK);
                //System.out.println("Asignamos valor "+maxK+" a la clase");
                
                casesListTrain.replaceCase(conf,j);
            }
            
            
            // Now we do the same for the test database.
            
            for (j=0 ; j<test.getNumberOfCases() ; j++) {
                
                conf = (ContinuousConfiguration)casesListTest.get(j);
                conf2 = (ContinuousConfiguration)conf.copy();
                conf2.remove(classNode);
                
                evidence = new Evidence(conf2);
                
                prop = new VariableElimination(net,evidence);
                
                NodeList temp = new NodeList();
                temp.insertNode(classNode);
                prop.setInterest(temp);
                prop.propagate();
                results = prop.getResults();
                pot = (PotentialContinuousPT)results.elementAt(0);
                // Out of pot, we get the state of the hidden variable with highest probability.
                cpt = pot.getTree();
                
                
                for (k=0 ; k<classNode.getNumStates() ; k++) {
                    conf2 = new ContinuousConfiguration();
                    conf2.insert(classNode,k);
                    sim[k] = ((MixtExpDensity)(cpt.getProb(conf2))).getIndependent();
                }//End of for
                
                // Now, simulate a value for the class
                u = Math.random();
                k = 0;
                found = false;
                acum = 0.0;
                maxK = 0;
                while (!found) {
                    acum += sim[k];
                    if ((u<=acum) || (k==(classNode.getNumStates()-1))) {
                        maxK = k;
                        found = true;
                    } else k++;
                }
                
                
                // Now we replace the obtained value in the database.
                conf.putValue(classNode,maxK);
                //System.out.println("Asignamos valor "+maxK+" a la clase");
                
                casesListTest.replaceCase(conf,j);
            }
            
            // M-step. Recompute the MTE densities in net.
            
            // The relations in the net are supposed to have the child variable
            // as first in the list of variables.
            varsNet = net.getNodeList();
            
            parents = new NodeList();
            // The parent variable will always be the hidden variable.
            parents.insertNode(classNode);
            
            for (j=0 ; j<varsNet.size() ; j++) {
                tempNode = varsNet.elementAt(j);
                rel = net.getRelation(tempNode);
                //varsRel = rel.getVariables().copy();
                varsRel = rel.getVariables();
                
                //varsRel.removeNode(tempNode);
                
                if (varsRel.size() == 1) { // Root node
                    updatedTree = learningObject.learnConditional(tempNode,new NodeList(),train,intervals,4);
                } else { // child node
                    updatedTree = learningObject.learnConditional(tempNode,parents,train,intervals,4);
                }
                
                
                // Now we update the potential in rel
                updatedPotential = new PotentialContinuousPT(varsRel,updatedTree);
                rel.setValues(updatedPotential);
                //System.out.println("Updated relation ");
                //rel.print();
            }
            
            finalLikelihood = test.logLikelihood(net);
            if (finalLikelihood > initialLikelihood) {
                initialLikelihood = finalLikelihood;
                i++;
            } else { // The likelihood is not improved
                i = 1001; // In order to get out of the while loop
            }
            
            System.out.println("New likelihood "+finalLikelihood);
            
        }//End of while
        
        
        
    }
    
    /**
     * Adds an state to the hidden variable. The distributions in the network are updated.
     * The probability of the new state in the class variable will be equal the probability
     * of the former last state divided by two, and that other probability is also updated.
     * In a child variable, the conditional distribution given the
     * added state will  be uniform copies from the neighbour child.
     *
     * @param net the network to update.
     * @param classVar the hidden variable.
     */
    
    public void addComponent(Bnet net, FiniteStates hiddenVar) throws IOException {
        
        int i, j, newStates, pos;
        NodeList varsNet, varsRel;
        Node tempNode;
        Relation rel;
        PotentialContinuousPT pot, newPot;
        ContinuousProbabilityTree tree, newTree, newChild;
        MixtExpDensity d, newD;
        FiniteStates newHiddenVar;
        Vector s;
        
        // Increase the number of states.
        newStates = hiddenVar.getNumStates() + 1;
        newHiddenVar = new FiniteStates();
        newHiddenVar.setNumStates(newStates);
        newHiddenVar.setName(new String(hiddenVar.getName()));
        newHiddenVar.setStates((Vector)(hiddenVar.getStates()).clone());
        s = newHiddenVar.getStates();
        s.addElement((new Integer(newStates-1)).toString());
        newHiddenVar.setStates(s);
        
        System.out.println("Adding component "+newStates);
        // The relations in the net are supposed to have the child variable
        // as first in the list of variables.
        varsNet = net.getNodeList();
        
        for (i=0 ; i<varsNet.size() ; i++) {
            tempNode = varsNet.elementAt(i);
            rel = net.getRelation(tempNode);
            
            // The next 5 lines are common to root node and leaves, because the root
            // of the probability tree is in any case the hidden variable
            pot = (PotentialContinuousPT)rel.getValues();
            tree = pot.getTree();
            newTree = tree.copy();
            newTree.setVar(newHiddenVar);
            pos = hiddenVar.getNumStates()-1;
            
            if (tempNode.equals(hiddenVar)) { // root node
                d = tree.getChild(pos).getProb();
                // d must be a real number
                newD = new MixtExpDensity(d.getIndependent() / 2);
                d = new MixtExpDensity(d.getIndependent() / 2);
                
                // Replace the last child
                newChild = new ContinuousProbabilityTree(d);
                newTree.setChild(newChild,pos);
                
                // Insert the new child
                newChild = new ContinuousProbabilityTree(newD);
                newTree.insertChild(newChild);
                varsNet.setElementAt(newHiddenVar,i);
            } else { // child variables
                newTree.insertChild(tree.getChild(pos).copy());
            }
            
            // Finally, replace the potential in the relation
            varsRel = rel.getVariables();
            pos = varsRel.getId(hiddenVar);
            varsRel.setElementAt(newHiddenVar,pos);
            newPot = new PotentialContinuousPT(varsRel,newTree);
            rel.setValues(newPot);
        }
        
        // Finally, modify the hidden var
        hiddenVar.setNumStates(newStates);
        s = hiddenVar.getStates();
        s.addElement((new Integer(newStates-1)).toString());
        hiddenVar.setStates(s);
        
        System.out.println("Component added");
        System.out.println("New hidden var states"+newHiddenVar.getStates().size());
        System.out.println("\nHidden var states"+hiddenVar.getStates().size());
        
    }
    
    
    /**
     * Learns the initial model. The hidden variable will have two states, with equal probability.
     * The hidden variables will be the first in the list of the resulting network. The database is modified
     * to include the hidden variable.
     *
     * The databases train and test are modified to include the hidden variable with value equal to 0.
     *
     * @eturn the initial bnet.
     */
    
    public Bnet learnInitialModel() {
        
        FiniteStates hiddenVar;
        Bnet net;
        NodeList variables, oldVariables, parents, netNodeList, relationVariables;
        MTELearning learningObject;
        Vector netRL;
        LinkList ll;
        Link l;
        int i, intervals = 4;
        Node childVar;
        ContinuousProbabilityTree t, t2;
        PotentialContinuousPT pot;
        Relation rel;
        MixtExpDensity d1, d2;
        Vector s = new Vector();
        ContinuousCaseListMem c, d;
        ContinuousConfiguration conf;
        
        hiddenVar = new FiniteStates();
        hiddenVar.setNumStates(2);
        hiddenVar.setName("Hidden");
        hiddenVar.setTitle("Hidden");
        
        s.addElement((new Integer(0)).toString());
        s.addElement((new Integer(1)).toString());
        hiddenVar.setStates(s);
        
        
        variables = train.getVariables().copy();
        
        // The list of parents will be empty.
        parents = new NodeList();
        
        
        // Construct a naive Bayes with all the variables.
        
        learningObject = new MTELearning(train);
        
        
        // The nodelist of the resulting network. The hidden variables is always the first in the list.
        netNodeList = new NodeList();
        netNodeList.insertNode(hiddenVar);
        
        // the relation list of the resulting network
        netRL = new Vector();
        
        // The link list of the resulting network
        ll = new LinkList();
        
        for (i=0 ; i<variables.size() ;  i++) {
            childVar = variables.elementAt(i);
            t2 = learningObject.learnConditional(childVar,parents,train,intervals,4);
            netNodeList.insertNode(childVar);
            
            t = new ContinuousProbabilityTree(hiddenVar);
            t.setChild(t2.copy(),0);
            t.setChild(t2,1);
            
            // the variables of the relation corresponding to the conditional distribution
            // for variable childVar.
            relationVariables = new NodeList();
            
            relationVariables.insertNode(childVar);
            relationVariables.insertNode(hiddenVar);
            
            l = new Link(hiddenVar,childVar);
            ll.insertLink(l);
            
            pot = new PotentialContinuousPT(relationVariables,t);
            
            
            rel = new Relation();
            rel.setVariables(relationVariables);
            rel.setValues(pot);
            
            netRL.addElement(rel);
        }
        
        // The marginal for the class variable is estimated separately.
        
        d1 = new MixtExpDensity(0.5);
        d2 = new MixtExpDensity(0.5);
        t = new ContinuousProbabilityTree(hiddenVar);
        t.setChild(new ContinuousProbabilityTree(d1),0);
        t.setChild(new ContinuousProbabilityTree(d2),1);
        
        relationVariables = new NodeList();
        relationVariables.insertNode(hiddenVar);
        
        pot = new PotentialContinuousPT(relationVariables,t);
        
        rel = new Relation();
        rel.setVariables(relationVariables);
        rel.setValues(pot);
        
        netRL.addElement(rel);
        
        // End of marginal for hidden variable
        
        net = new Bnet();
        net.setRelationList(netRL);
        net.setNodeList(netNodeList);
        net.setLinkList(ll);
        
        // Now, modify the database.
        oldVariables = variables.copy();
        
        variables.insertNode(hiddenVar);
        
        c = (ContinuousCaseListMem)train.getCaseListMem();
        
        train.setNodeList(variables);
        
        
        for (i=0 ; i<c.getNumberOfCases() ; i++) {
            c.setVariables(oldVariables.getNodes());
            conf = (ContinuousConfiguration)c.get(i);
            conf.insert(hiddenVar,0);
            c.setVariables(variables.getNodes());
            c.replaceCase(conf,i);
        }
        
        d = (ContinuousCaseListMem)test.getCaseListMem();
        
        test.setNodeList(variables);
        
        
        for (i=0 ; i<d.getNumberOfCases() ; i++) {
            d.setVariables(oldVariables.getNodes());
            conf = (ContinuousConfiguration)d.get(i);
            conf.insert(hiddenVar,0);
            d.setVariables(variables.getNodes());
            d.replaceCase(conf,i);
        }
        
        return (net);
    }
    
    
    /**
     * Learns the model from the train database. The learnt model will be stored in the instance
     * variable <code>classifier</code>.
      */
    
    public void learnModel() throws IOException {
        
        Bnet net, net2;
        double initialLikelihood, newLikelihood;
        boolean done = false, firstIteration = true;
        FiniteStates hiddenVar;
        
        System.out.println("\nLearning initial model...\n");
        net = learnInitialModel();
        System.out.println("\nInitial model learnt.\n");
        
        FileWriter f = new FileWriter("initial.elv");
        net.saveBnet(f);
        f.close();
        
        initialLikelihood = test.logLikelihood(net);
        //initialLikelihood = -1e20;
        System.out.println("Initial likelihood: "+initialLikelihood);
        
        hiddenVar = (FiniteStates)net.getNodeList().elementAt(0);
        
        // By default, the maximum number of states of the hidden
        // variable is set to 50
        int comp = hiddenVar.getNumStates();
        while ((!done) && (hiddenVar.getNumStates()<51)) {
            
            net2 = new Bnet();
            net2.setNodeList(net.getNodeList().copy());
            net2.setLinkList(net.getLinkList().copy());
            net2.setRelationList(net.getRelationList());
            
            //addComponent(net2,hiddenVar);
            
            if (!firstIteration) {
                addComponent(net2,hiddenVar);
            }
            else {
                firstIteration = false;
            }
            
            System.out.println("\nNumber of components "+hiddenVar.getNumStates());
            
            EMAlgorithm(net2);
            
            classifier = net2;
            
            this.saveNetwork("bestnet"+hiddenVar.getNumStates()+"comp.elv");
            
            //addComponent(net2,hiddenVar);
            
            newLikelihood = test.logLikelihood(net2);
            System.out.println("Likelihood best net with "+hiddenVar.getNumStates()+" components: "+newLikelihood);
            
            if (newLikelihood < initialLikelihood) {
                if (comp > 5)
                    done = true;
            } else {
                initialLikelihood = newLikelihood;
                net = net2;
            }
            
            comp++;
        }
        
        classifier = net;
    }
    
    
    /**
     * Sets the training set.
     *
     * @param t the training set.
     */
    
    public void setTrain(DataBaseCases t) {
        
        train = t;
    }
    
    
     /**
     * Sets the test set.
     *
     * @param t the test set.
     */
    
    public void setTest(DataBaseCases t) {
        
        test = t;
    }
    
    
    /**
     * Saves the network corresponding to the classifier in the given file.
     *
     * @param fineName the name of the file where the net will be written.
     */
    
    public void saveNetwork(String name) throws IOException {
        
        FileWriter f = new FileWriter(name);
        classifier.saveBnet(f);
        f.close();
    }
    
    
    /**
     * Main for constructing an MTE unsupervised classifier from a database.
     *
     * Arguments:
     * 1. the train dbc file
     * 2. the name of the file where the learnt network will be saved.
     * 3. the test dbc file.
     * 4. the number of intervals into which the domain of the continuous variables
     *    will be split.
     */
    
    public static void main(String args[]) throws ParseException,IOException {
        
        int interv;
        FileInputStream f1 = new FileInputStream(args[0]);
        FileInputStream f2 = new FileInputStream(args[2]);
        Vector results, errors;
        double bias;
        NodeList nl;
        DataBaseCases tr = new DataBaseCases(f1), te = new DataBaseCases(f2);
        
        //interv = Integer.valueOf(args[3]).intValue();
        
        
        
        UnsupervisedMTENaiveBayes nb = new UnsupervisedMTENaiveBayes();
        
        nb.setTrain(tr);
        nb.setTest(te);
        
        nb.learnModel();
        
        nb.saveNetwork(args[1]);
        
    } // End of main
    
    
    
}//end of class
