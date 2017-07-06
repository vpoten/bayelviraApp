/* ArcReversal.java */
package elvira.inference.elimination.ids;

import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.io.*;
import elvira.*;
import elvira.IDiagram;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.potential.Potential;
import elvira.potential.PotentialTable;
import elvira.potential.PotentialFunction;
import elvira.RelationList;
import elvira.tools.Crono;
import elvira.parser.ParseException;

/**  
 * Class <code>ArcReversal</code>. Implements the solution of 
 * a regular ID (Influence Diagram).
 * @author Manuel Gomez 
 * @since 11/9/2000
 */
public class ArcReversal extends elvira.inference.Propagation {

    /**
     * The decision tables associated with the problem.
     */
    RelationList decisionTables;

    /**
     * The influence diagram over which the class operates. This is
     * the copy used to evaluate. So network keeps the IDiagram as
     * it is at the beginning
     */
    protected IDiagram diag;

    /**
     * Crono, to measure the execution times
     */
    protected Crono crono;

    /**
     * Posterior distributions of the chance variables before the removal in
     * arc reversal's methods.
     */
    public ArrayList posteriorDistributions;

    /**
     * Posterior utilities of the chance variables before the removal in
     * arc reversal's methods.
     */
    public ArrayList posteriorUtilities;

    /**
     * Creates a new propagation.
     */
    public ArcReversal() {

        decisionTables = new RelationList();
        crono = new Crono();
        posteriorDistributions = new ArrayList();
        posteriorUtilities = new ArrayList();
    }

    /**
     * Creates a new propagation for a given diagram.
     * @param id the influence diagram to evaluate.
     */
    public ArcReversal(IDiagram id) {

        diag = id;
        network = id.copy();
        decisionTables = new RelationList();
        crono = new Crono();
        posteriorDistributions = new ArrayList();
        posteriorUtilities = new ArrayList();
    }

    /**
     * Program for performing experiments from the command line.
     * The command line arguments are as follows.
     * <ol>
     * <li> Input file: the network.
     * <li> Output file.
     * <li> Evidence file.
     * </ol>
     * If the evidence file is omitted, then no evidences are
     * considered .
     */
    public static void main(String args[]) throws ParseException, IOException {
        ArcReversal eval;
        int evaluationMode;
        boolean evaluable;
        IDiagram diag;
        String base;

        if (args.length < 2) {
            System.out.println("Too few arguments. Arguments are: ElviraFile OutputElviraFile");
            System.exit(-1);
        }

        diag = (IDiagram) Network.read(args[0]);

        // Make a qualitative copy

        IDiagram simpleCopy = diag.qualitativeCopy();

        eval = new ArcReversal(diag);

        // initial chekout about the node.

        evaluable = eval.initialConditions();
        System.out.print("Evaluable : " + evaluable + "\n\n");

        // If the diagram is suitable to be evaluated, then do it.

        if (evaluable == true) {
            // Save the data respect to the evaluation process

            base = args[0].substring(0, args[0].lastIndexOf('.'));
            base = base.concat("_ArcReversal_data");

            // Set the name for the file of results

            eval.statistics.setFileName(base);

            // Transform the initial relations

            eval.evaluateDiagram();
            eval.saveResults(args[1]);

            // Try to store the results as a bnet

            eval.saveResultsAsNetwork(args[1]);
        }
    }

    /**
     * Check whether the diagram is suitable to be evaluated:
     * - only directed links
     * - only one value node
     * - no cycles
     * - directed path between decisions and value node
     *
     * @return <code>true</code> if OK. <code>false</code> in other case.
     */
    public boolean initialConditions() {

        boolean evaluable;
        double finalSize;

        evaluable = diag.directedLinks();

        if (evaluable == false) {
            System.out.print("Influence Diagram with no directed links\n\n");
            return (false);
        }

        evaluable = diag.onlyOneValueNode();

        if (evaluable == false) {
            System.out.print("Influence Diagram with 0 or more than 1 value nodes\n\n");
            return (false);
        }

        evaluable = diag.hasCycles();
        if (evaluable == true) {
            System.out.print("Influence Diagram with cycles\n\n");
            return (false);
        }

        diag.addNonForgettingArcs();

        evaluable = diag.pathBetweenDecisions();
        if (evaluable == false) {
            System.out.print("Influence Diagram with non ordered decisions\n\n");
            return (false);
        }

        // Remove barren nodes

        diag.eliminateRedundancy();

        // Transform the initial relations

        RelationList relations = getInitialRelations();
        ((Network) diag).setRelationList(relations.getRelations());

        // Store the initial size for the diag

        finalSize = diag.getProblemSize();

        // Return true if OK

        return (true);
    }

    /**
     * Evaluates the Influence Diagram associated with the class
     * using Shachter's Algorithm.
     */
    public void evaluateDiagram() {

        Date d;
        double time;
        Node value;
        Potential func;
        Relation rel;
        int i = 0;

        crono.start();

        // First at all put in the vector of operations and sizes
        // the initial situation of the IDiagram

        statistics.addOperation("Start of evaluation: ");
        Vector relations = ((Network) diag).getRelationList();
        RelationList currentRelations = new RelationList();
        currentRelations.setRelations(relations);
        statistics.addSize(currentRelations.sumSizes());
        statistics.addTime(crono.getTime());

        // Now, begin with the evaluation

        value = diag.getValueNode();
        rel = diag.getRelation(value);

        if (rel.getValues() != null) {
            if (rel.getValues().getClass() == PotentialFunction.class) {
                // We have to generate the values using the function

                rel.setValues(((PotentialFunction) rel.getValues()).potentialFunctionToTable());
            }
        }


        // Main loop: while value node has parents

        while (value.hasParentNodes() == true) {
            // First, try to eliminate a chance node

            if (removeChanceNode() == false) {
                // Try it with a decision node

                if (removeDecisionNode() == false) {
                    // Try an arc inversion

                    if (reverseArc() == false) {
                        System.out.print("Error in evaluation algorithm\n");
                        value.print();
                        return;
                    }
                }
            }
        }

        // Now store the final value for the utility

        statistics.setFinalExpectedUtility(rel.getValues());

        // Set the time needed to the evaluation

        statistics.setTime(crono.getTime());

        // Shows the time needed to complete the evaluation

        crono.viewTime();

        // Finally, generates the file with the statistics

        try {
            statistics.printOperationsAndSizes();
        } catch (IOException e) {
        }
        ;
    }

    /**
     * To remove a chance node
     * @return the result of the operation, as a <code>boolean</code>.
     */
    private boolean removeChanceNode() {

        Node util;
        Node candidate;
        NodeList parents;
        String operation;
        int i;

        // Obtain the value node

        util = diag.getValueNode();
        parents = diag.parents(util);

        for (i = 0; i < parents.size(); i++) {

            candidate = parents.elementAt(i);

            if (candidate.getKindOfNode() == candidate.CHANCE) {
                if ((diag.descendantsList(candidate)).size() == 1) {
                    // The node can be removed. Put the node in the
                    // vector of operations

                    operation = "Chance node removal: " + candidate.getName();
                    statistics.addOperation(operation);

                    // The relation of the utility node is modified. In this
                    // case the parents of the node to remove will be parents
                    // of the utility node

                    modifyUtilityRelation(candidate, true);

                    // Calculate the new expected utility
                    getExpectedUtility(util, candidate);

                    // The node is deleted

                    diag.removeNodeOnly(candidate);

                    // Once the node is removed, store the size of the
                    // diagram

                    Vector relations = ((Network) diag).getRelationList();
                    RelationList currentRelations = new RelationList();
                    currentRelations.setRelations(relations);
                    statistics.addSize(currentRelations.sumSizes());
                    statistics.addTime(crono.getTime());

                    // Return true

                    return (true);
                }
            }
        }
        return (false);
    }

    /**
     * To remove a decision node
     * @return the result of the operation,  as a <code>boolean</code>.
     */
    private boolean removeDecisionNode() {

        Node util;
        Node candidate;
        NodeList parents;
        Potential potential;
        String operation;
        int i;

        // Obtain the value node

        util = diag.getValueNode();
        parents = diag.parents(util);

        for (i = 0; i < parents.size(); i++) {

            candidate = parents.elementAt(i);

            if (candidate.getKindOfNode() == candidate.DECISION) {

                if (diag.decisionReadyToRemove(candidate) == true) {

                    // The node can be removed : save operation and size

                    operation = "Decision node removal: " + candidate.getName();
                    System.out.println("Eliminando " + candidate.getName());
                    statistics.addOperation(operation);

                    // Save the potential with the decision function

                    potential = (Potential) diag.getRelation(util).getValues().sendVarToEnd(candidate);
                    results.addElement(potential);
                    //results.addElement((Potential)potential.sendVarToEnd(candidate));

                    // Store the explanation with the importance of each variable
                    // of the decision table

                    statistics.setExplanation(candidate.getName(), potential);

                    // The relation of the utility node is modified. In this
                    // case the parents of the node to remove wont be parents
                    // of the utility node

                    modifyUtilityRelation(candidate, false);

                    // Maximize the utility

                    maximizeUtility(util, candidate, decisionTables);

                    // The node is deleted

                    diag.removeNodeOnly(candidate);

                    // Once the node is removed, store the size

                    Vector relations = ((Network) diag).getRelationList();
                    RelationList currentRelations = new RelationList();
                    currentRelations.setRelations(relations);
                    statistics.addSize(currentRelations.sumSizes());
                    statistics.addTime(crono.getTime());

                    // Return true

                    return (true);
                }
            }
        }

        return (false);
    }

    /**
     * To reversa an arc
     * @return the result of the operation, as a <code>boolean</code>.
     */
    protected boolean reverseArc() {

        Node util;
        Node candidate;
        Node dest;
        NodeList parents;
        NodeList candidateChildren;
        Link link;
        String operation;
        int i, j;

        // we obtain the utility node

        util = diag.getValueNode();
        parents = diag.parents(util);

        for (i = 0; i < parents.size(); i++) {

            candidate = parents.elementAt(i);

            if (candidate.getKindOfNode() == candidate.CHANCE) {
                // We look for a node:
                //   - parent of the utility node
                //   - is not parent of a decision nodo
                //   - parent of another chance node
                //         - only one path between these two nodes

                candidateChildren = diag.children(candidate);
                if (candidate.hasDirectDecisionChild() == false
                        && candidate.isUtilityParent() == true && candidateChildren.size() > 1) {

                    // Consider the childrens of the candidate.
                    // Revert the arc if there is only one
                    // path between them

                    for (j = 0; j < candidateChildren.size(); j++) {
                        dest = candidateChildren.elementAt(j);

                        // Act if is a chance node

                        if (dest.getKindOfNode() == Node.CHANCE) {

                            link = diag.getLink(candidate, dest);

                            // Now we see if there is another link between them

                            if (candidate.moreThanAPath(dest) == false) {
                                // The arc can be reversed : save operation and size

                                operation = "Arc reversal: " + candidate.getName() + "-> " + dest.getName();
                                statistics.addOperation(operation);

                                // We modify the relations of the nodes

                                modifyRelations(candidate, dest);

                                // We get the posterior distributions

                                getPosteriorDistributions(candidate, dest);

                                // Once the operation is done, notes down the size

                                Vector relations = ((Network) diag).getRelationList();
                                RelationList currentRelations = new RelationList();
                                currentRelations.setRelations(relations);
                                statistics.addSize(currentRelations.sumSizes());
                                statistics.addTime(crono.getTime());

                                // Return true

                                return (true);
                            }
                        }
                    }
                }
            }
        }
        return (false);
    }

    /**
     * To change the relation of the utility node.
     * @param toRemove the node to be removed.
     * @param inherit <ul>
     * <li>1 (the parents of the node to remove will be inherited).
     * <li>0 (the parents of the node to remove will not be inherited).
     * </ul>
     */
    public void modifyUtilityRelation(Node toRemove, boolean inherit) {

        Node util;
        Link link;
        NodeList finalVariables, parentNodes, inherited;
        int i;

        util = diag.getValueNode();
        finalVariables = new NodeList();
        finalVariables.insertNode(util);
        parentNodes = diag.parents(util);
        inherited = diag.parents(toRemove);

        finalVariables.merge(parentNodes);

        if (inherit == true) {
            finalVariables.merge(inherited);

            // Add new links

            diag.addLinks(inherited, util);
        }

        // Remove link between toRemove and util

        link = diag.getLink(toRemove, util);
        try {
            diag.removeLink(link);
        } catch (InvalidEditException iee) {
            ;
        }

        // Modify the relation of the value node

        finalVariables.removeNode(toRemove);
        diag.getRelation(util).setVariables(finalVariables);
    }

    /**
     * To change the links of the utility node after a node removal
     * @param toRemove the node to be removed.
     * @param inherit <ul>
     * <li>1 (the parents of the node to remove will be inherited).
     * <li>0 (the parents of the node to remove will not be inherited).
     * </ul>
     */
    public void modifyUtilityLinks(Node toRemove, boolean inherit) {

        Node util;
        Link link;
        NodeList finalVariables, parentNodes, inherited;
        int i;

        util = diag.getValueNode();
        finalVariables = new NodeList();
        finalVariables.insertNode(util);
        parentNodes = diag.parents(util);
        inherited = diag.parents(toRemove);

        finalVariables.merge(parentNodes);

        if (inherit == true) {
            finalVariables.merge(inherited);

            // Add new links

            diag.addLinks(inherited, util);
        }

        // Remove link between toRemove and util

        link = diag.getLink(toRemove, util);
        try {
            diag.removeLink(link);
        } catch (InvalidEditException iee) {
            ;
        }
    }

    /**
     * Modify the relations of the nodes for reverse arc
     * operations.
     */
    public void modifyRelations(Node origin, Node dest) {

        Relation originRel, destRel;
        NodeList originParents, destParents,
                finalDestVariables = new NodeList(),
                finalOriginVariables = new NodeList();
        Link link;
        int i;

        // Get the parents of the origin

        originParents = diag.parents(origin);
        destParents = diag.parents(dest);

        // Get the relations of both nodes

        originRel = diag.getRelation(origin);
        destRel = diag.getRelation(dest);

        // Add links for the dest node

        diag.addLinks(originParents, dest);

        // Remove the link between origin and dest

        link = diag.getLink(origin, dest);
        diag.removeLinkOnly(link);

        // The origin inherites the parents of the dest, and we have
        // to insert the dest as parent

        diag.addLinks(destParents, origin);
        try {
            diag.createLink(dest, origin, true);
        } catch (InvalidEditException iee) {
            ;
        }

        // Modify the variables of the relations
        // First, eliminate origin as parent of dest

        destParents.removeNode(origin);

        // a) for dest node

        finalDestVariables.insertNode(dest);
        finalDestVariables.merge(destParents);
        finalDestVariables.merge(originParents);
        destRel.setVariables(finalDestVariables);

        // b) for origin node

        finalOriginVariables.insertNode(origin);
        finalOriginVariables.merge(originParents);
        finalOriginVariables.merge(destParents);
        finalOriginVariables.insertNode(dest);
        originRel.setVariables(finalOriginVariables);
    }

    /**
     * Modify the links of the nodes for reverse arc
     * operations.
     */
    public void modifyLinks(Node origin, Node dest) {

        NodeList originParents, destParents,
                finalDestVariables = new NodeList(),
                finalOriginVariables = new NodeList();
        Link link;
        int i;

        // Get the parents of the origin

        originParents = diag.parents(origin);
        destParents = diag.parents(dest);

        // Add links for the dest node

        diag.addLinks(originParents, dest);

        // Remove the link between origin and dest

        link = diag.getLink(origin, dest);
        diag.removeLinkOnly(link);

        // The origin inherites the parents of the dest, and we have
        // to insert the dest as parent

        diag.addLinks(destParents, origin);
        try {
            diag.createLink(dest, origin, true);
        } catch (InvalidEditException iee) {
            ;
        }
    }

    /**
     * To get the expected utility after a node deletion
     */
    public void getExpectedUtility(Node util, Node toRemove) {

        Potential expected, func, dist, mixedPotential;
        FiniteStates fict;
        Configuration conf;
        double value;
        int total, i;
        Potential postDist;
        Potential postUtils;
        Potential postUtilsVarAtEnd;

        // Get utility potentialTable

        func = (Potential) diag.getRelation(util).getValues();

        // Get the probability distribution
        dist = (Potential) diag.getRelation(toRemove).getValues();
        postDist = dist.sendVarToEnd(toRemove);

        // Save the potential with the posterior distribution of the chance node
        posteriorDistributions.add(postDist);

        // Get the utilities for the chance node
        postUtils = (Potential) diag.getPotentialOfGlobalUtility().sendVarToEnd(toRemove);

        //Save the potential with the utilities of the chance node
        posteriorUtilities.add(postUtils);

        // Combine these potentials

        expected = func.combine(dist);

        // Marginalize over toRemove

        expected = expected.addVariable((FiniteStates) toRemove);

        // Store the expected utility, after changing it

        expected = transformAfterOperation(expected, true);
        diag.getRelation(util).setValues(expected);
    }

    /**
     * Function to actualize the utility when decision node removal.
     */
    public void maximizeUtility(Node util, Node toRemove, RelationList tables) {

        Vector vars;
        Potential maximum, func, mixedPotential;
        FiniteStates fict;
        Configuration conf;
        int i, total;
        double value;

        // First at all, we store the actual utility function

        //diag.storeDecisionTable(util,toRemove,tables);

        // Get utility potentialTable

        func = (Potential) diag.getRelation(util).getValues();

        // Combine these potentials

        vars = new Vector(func.getVariables());

        // Marginalize maximizing

        vars.removeElement(toRemove);
        maximum = (Potential) func.maxMarginalizePotential(vars);

        // Transform the potential to add the effect of constraints and
        // prune

        maximum = transformAfterOperation(maximum, true);
        diag.getRelation(util).setValues(maximum);
    }

    /**
     * To get the posterior distributions as result of an arc reversal.
     * @param origin the origin node.
     * @param dest the destination node.
     */
    public void getPosteriorDistributions(Node origin, Node dest) {

        Potential initialOrigin, initialDest, finalOrigin, finalDest;

        // To get the initial distributions

        initialOrigin = (Potential) diag.getRelation(origin).getValues();
        initialDest = (Potential) diag.getRelation(dest).getValues();

        // First, get the final distribution for dest

        finalDest = initialDest.combine(initialOrigin);
        finalDest = finalDest.addVariable((FiniteStates) origin);

        // Now, the final distribution for origin

        finalOrigin = initialOrigin.combine(initialDest);
        finalOrigin = finalOrigin.divide(finalDest);

        // Transform the potential, once modified the relations

        finalDest = transformAfterOperation(finalDest, false);
        finalOrigin = transformAfterOperation(finalOrigin, false);

        // Set the final distributions, once transformed

        diag.getRelation(origin).setValues(finalOrigin);
        diag.getRelation(dest).setValues(finalDest);
    }

    /**
     * Transforms one of the original relations in a Potential. In this
     * case no transformation is carried out. This method can be
     * overloaded for special requirements.
     * @param <code>Relation</code> the relation to transform
     * @return <code>Relation</code> the transformed relation
     */

    /*public Relation transformInitialRelation(Relation r) {
    return r;
    }*/
    /**
     * Tranform the relation after a operation on its values
     * data member. This transformation allows the consideration
     * of constraints and prunning, for derivated clasess
     * @param <code>Potential</code> potential to transform
     * @param <code>boolean</code> flag to show if it is a utility
     * @return </code>Potential</code> the transformed potential
     */
    public Potential transformAfterOperation(Potential pot, boolean flag) {
        return (pot);
    }

    /**
     * To store the decision tables. Now it is no used: the tables are
     * added to results
     */
    public void storeDecisionTable(Node util, Node toRemove, RelationList tables) {

        NodeList nodes;
        PotentialTable orderedPotential, actualPotential;
        Configuration conf;
        Relation finalRel;
        double utility;
        int total, i;

        // The dimensions of the ordered potential will be the same
        // as the actual, but ordered: the last will be the decision
        // to remove

        nodes = diag.parents(util);
        nodes.removeNode(toRemove);
        nodes.insertNode(toRemove);

        // We create a potential to store the actual utility function

        orderedPotential = new PotentialTable(nodes);
        total = (int) FiniteStates.getSize(orderedPotential.getVariables());

        // We get the actual potential

        actualPotential = (PotentialTable) diag.getRelation(util).getValues();
        conf = new Configuration(nodes);

        // To order the values. We copy the actual utility function
        // in the ordered potential

        for (i = 0; i < total; i++) {
            utility = actualPotential.getValue(conf);
            orderedPotential.setValue(conf, utility);
            conf.nextConfiguration();
        }

        // This potential (ordered) is stored as final result of the
        // evaluation. For that we create a new Relation

        finalRel = new Relation();
        finalRel.setName(util.getName());
        finalRel.setKind(finalRel.UTILITY);
        finalRel.setVariables(nodes);
        finalRel.setValues(orderedPotential);

        // This relation is inserted in the decisionTables

        tables.insertRelation(finalRel);
    }

    /**
     * To store the decision tables. Now it is no used: the tables are
     * added to results
     */
    public void storeQualitativeDecisionTable(Node util, Node toRemove, RelationList tables) {

        NodeList nodes;
        Configuration conf;
        Relation finalRel;
        double utility;

        // Order the nodes, so the node to remove be the last

        nodes = diag.parents(util);
        nodes.removeNode(toRemove);
        nodes.insertNode(toRemove);

        // Create a new Relation to store the variables implied

        finalRel = new Relation();
        finalRel.setName(util.getName());
        finalRel.setKind(finalRel.UTILITY);
        finalRel.setVariables(nodes);

        // This relation is inserted in the decisionTables

        tables.insertRelation(finalRel);
    }

    /**
     * Method to compare the policies obtaines as a consequence
     * of two evalautions on the same IDiagram
     * @param <code>resultsToCompare</code> evaluation to compare with
     * @result <code>double</code> the distance between policies
     */
    public double comparePolicies(Vector resultsToCompare) {
        Potential result;
        Potential resultToCompare;
        Vector vars;
        Vector varsForConf = new Vector();
        FiniteStates decision;
        Configuration partial, total, totalToCompare;
        double utility, utilityToCompare, max, diff = 0, diffLocal;
        long size, k;
        long cases;
        int i, j, indMax;

        // To compare both policies we must see the expected
        // utility of the proposed policy for the evaluation
        // respect to the evaluation passed as an argument
        // We will only consider the last table with the global
        // policy, ANYWAY, ITS IN A LOOP........

        for (i = 0; i < results.size(); i++) {
            result = (Potential) results.elementAt(i);
            resultToCompare = (Potential) resultsToCompare.elementAt(i);

            size = result.getSize();

            // Make a configuration over the whole set of
            // variables except the last one

            vars = result.getVariables();
            for (j = 0; j < vars.size() - 1; j++) {
                varsForConf.addElement(vars.elementAt(j));
            }
            decision = (FiniteStates) vars.elementAt(j);
            cases = size / (decision.getNumStates());
            partial = new Configuration(varsForConf);
            total = new Configuration(vars);
            totalToCompare = new Configuration(resultToCompare.getVariables());

            // Once the configuration is done, we must go over
            // all of its values, to retrieve the optimal policy
            // for it

            diff = 0;
            for (k = 0; k < cases; k++) {

                // Copy the values from partial configuration

                total.resetConfiguration(partial);

                // Get the optimal policy for each case, in the first table

                max = 0;
                utility = 0;
                for (j = 0, indMax = 0; j < decision.getNumStates(); j++) {
                    total.putValue(decision, j);
                    utility = result.getValue(total);

                    if (j == 0) {
                        max = utility;
                    } else {
                        if (max < utility) {
                            max = utility;
                            indMax = j;
                        }
                    }
                }

                // Set the value for the maximum value in the total
                // configuration

                total.putValue(decision, indMax);

                // Once obtained the maximum for the base table,
                // get this value for the second one

                for (j = 0; j < totalToCompare.size(); j++) {
                    totalToCompare.putValue(total.getVariable(j).getName(), total.getValue(j));
                }

                utilityToCompare = resultToCompare.getValue(totalToCompare);

                // Compute the difference

                diffLocal = max - utilityToCompare;
                ;
                diff += Math.pow(diffLocal, 2);

                // Go to the next configuration

                partial.nextConfiguration();
            }

            // Once finished, divide by the number of cases

            diff = diff / cases;

            // Get square root, as the global diff

            diff = Math.sqrt(diff);

            // As by now we are only interested inf the first
            // table, break the loop

            break;
        }

        return (diff);
    }

    /**
     * To return the list of variables that appear in decision
     * tables
     *
     */
    protected NodeList variablesInDecisionTables() {
        int i;
        NodeList variables = new NodeList();

        for (i = 0; i < decisionTables.size(); i++) {
            variables.merge((decisionTables.elementAt(i)).getVariables());
        }

        // Return the nodelist

        return (variables);
    }

    /**
     * Method to return the list of relations that show the
     * variables related to each of the decisions
     */
    public RelationList getDecisionTables() {
        return (decisionTables);
    }
} // End of class

