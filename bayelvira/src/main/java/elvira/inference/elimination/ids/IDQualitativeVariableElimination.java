
/* QualitativeVariableElimination.java */
package elvira.inference.elimination.ids;

import java.util.Vector;
import java.io.*;
import elvira.*;
import elvira.IDiagram;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList;
import elvira.tools.idiagram.pairtable.IDPairTable;
import elvira.parser.ParseException;

/**  
 * Class <code>QualitativeArcReversal</code>. Implements the solution of 
 * a regular ID (Influence Diagram). No updates of utilities nor probabilities
 * are made. The objective is to get measures about the evaluation process and
 * determine the variables relevant for decision tables.
 * @author Manuel Gomez 
 * @since 06/02/2002
 */
public class IDQualitativeVariableElimination extends elvira.inference.elimination.VariableElimination {

    /*
     * Vector to store the order of operation
     */
    Vector orderOfElimination;

    /*
     * Vector to store the order of instantiation
     */
    Vector orderOfInstantiation;

    /*
     * List of relations to store decision tables
     */
    RelationList decisionTables;

    /**
     * Creates a new qualitative propagation
     * The ID should be clean (without redundancy) or not. The second flag
     * shows this condition
     * @param id Influence diagram
     */
    public IDQualitativeVariableElimination(IDiagram id) {
        super(id.qualitativeCopyWithRelations());
        orderOfElimination = new Vector();
        decisionTables = new RelationList();
        ((IDiagram) network).addNonForgettingArcs();
        ((IDiagram) network).eliminateRedundancy();
        currentRelations = network.getInitialRelations();
    }

    /**
     * Creates a new qualitative propagation
     * The ID should be clean (without redundancy) or not. The second flag
     * shows this condition
     * @param id Influence diagram
     * @param flag to show if ot is required the elimination of redundancy
     */
    public IDQualitativeVariableElimination(IDiagram id, boolean toClean) {
        super(id.qualitativeCopyWithRelations());
        orderOfElimination = new Vector();
        decisionTables = new RelationList();
        if (toClean == true) {
            ((IDiagram) network).addNonForgettingArcs();
            ((IDiagram) network).eliminateRedundancy();
        }
        currentRelations = network.getInitialRelations();
    }

    /**
     * Return the vector with the names of the removed variables
     */
    public Vector getOrderOfElimination() {
        return (orderOfElimination);
    }

    /**
     * Return the vector with the names of the nodes, in the
     * order they have to be instantiated
     */
    public Vector getOrderOfInstantiation() {
        return (orderOfInstantiation);
    }

    /**
     * Return the list of decision tables
     */
    public RelationList getDecisionTables() {
        return decisionTables;
    }

    /**
     * To return the list of variables that appear in decision
     * tables
     *
     */
    private NodeList variablesInDecisionTables() {
        int i;
        NodeList variables = new NodeList();

        for (i = 0; i < decisionTables.size(); i++) {
            variables.merge((decisionTables.elementAt(i)).getVariables());
        }

        // Return the nodelist
        return (variables);
    }

    /**
     * Gives an order of instantiation
     *
     */
    public void produceOrderOfInstantiation() {
        Graph ancestralGraph;
        NodeList variables;
        NodeList variablesInDiagNew;
        IDiagram copy;

        // First at all, make a copy of the original diagram
        // We must preserve this diagram to get the order of
        // instantiation, once known the relevant variables
        // (those that apper in decision tables)

        copy = ((IDiagram) network).qualitativeCopyWithRelations();

        // Now, evaluate the diagram to get the decision tables
        // As diag points to qualitativeID, no change is done on
        // the original diagram, but in its copy

        getPosteriorDistributionsID();

        // Get the variables relevant for decision tables. With this way,
        // the name of the variables (pointing to nodes in the qualitative
        // copy used for the qualitative evaluation) are used to point to
        // the proper variables in this diagram

        variables = variablesInDecisionTables();

        // Translate the decision tables obtained for qualitativeID;
        // the variables must be pointed to the relatives in diag

        decisionTables = ((IDiagram) network).translateRelations(decisionTables);

        // Look for relevant variables. With them build the ancestral
        // graph to look for the order of instantiation

        variablesInDiagNew = copy.insertVariablesIn(variables);

        // These variables are used to get the ancestral graph

        ancestralGraph = copy.ancestral(variablesInDiagNew);

        // From this graph we can get the order of instantiation

        orderOfInstantiation = copy.giveInstantiationOrder(ancestralGraph);

        // After this operation the diag is complete, without any
        // change, so could be used again
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
        IDiagram diag;
        IDQualitativeVariableElimination eval;
        int i, j;

        // Create the diagram

        diag = (IDiagram) Network.read(args[0]);
        eval = new IDQualitativeVariableElimination(diag);
        eval.getPartialOrder();
        eval.produceOrderOfInstantiation();

        // Show information
        System.out.println("--------------------- Informacion -------------------------");
        for (i = 0; i < eval.orderOfInstantiation.size(); i++) {
            System.out.println("Instanciacion [" + i + "] = " + eval.orderOfInstantiation.elementAt(i));
        }

        for (i = 0; i < eval.orderOfElimination.size(); i++) {
            System.out.println("Eliminacion [" + i + "] = " + eval.orderOfElimination.elementAt(i));
        }

        System.out.println("----------------------- Tablas de decision ------------------------");

        for (i = 0; i < eval.decisionTables.size(); i++) {
            Relation rel = eval.decisionTables.elementAt(i);
            NodeList vars = rel.getVariables();
            FiniteStates decision = (FiniteStates) vars.elementAt(vars.size() - 1);
            System.out.println("Tabla para " + decision.getName());
            for (j = 0; j < vars.size() - 1; j++) {
                FiniteStates node = (FiniteStates) vars.elementAt(j);
                System.out.print(node.getName() + " ");
            }
            System.out.println("");
            System.out.println("--------------------------------------------------------");
        }
    }

    /**
     * Evaluates the Influence Diagram associated with the class
     * using VariableElimination method and produce the partial
     * order between the variables
     */
    public void getPartialOrder() {
        NodeList decisions = ((IDiagram) network).getDecisionList();
        NodeList phase, newPhase;
        Node decisionConsidered;
        int i, j;

        // Now, as first step, get all nodes without decisions as sucessors

        phase = ((IDiagram) network).getChanceNodesWithoutDecisionsAsSucessors();

        // Now, add all of these nodes to the vector giving the
        // order of evaluation

        for (i = 0; i < phase.size(); i++) {
            orderOfElimination.addElement(phase.elementAt(i).getName());
        }

        // Now, go on the list of decisions

        for (i = decisions.size() - 1; i >= 0; i--) {

            // Get the decision considered in this iteration

            decisionConsidered = decisions.elementAt(i);

            // Add this decision to orderOfElimination

            orderOfElimination.addElement(decisionConsidered.getName());

            // Add it to phase

            phase.insertNode(decisionConsidered);

            // Now, get all chance nodes with sucessors in phase

            newPhase = ((IDiagram) network).getChanceNodesWithSucessorsInSet(phase);

            // Add all nodes in newPhase to orderOfElimination

            for (j = 0; j < newPhase.size(); j++) {
                orderOfElimination.addElement(newPhase.elementAt(j).getName());
                phase.insertNode(newPhase.elementAt(j));
            }
        }
    }

    /**
     * Gets the expected utility tables for the decision nodes in
     * an influence diagram
     */
    public void getPosteriorDistributionsID() {
        NodeList notRemoved;
        Node x;
        RelationList rLtemp;
        Relation valueRel;
        IDPairTable pt = null;
        int i, j, s;
        Vector vars = null;
        String operation;
        String base;
        boolean combined;

        // Initializes the crono

        crono.start();

        // Insert in notRemoved all not observed nodes

        notRemoved = getNotObservedNodes();

        // Make a pair table

        pt = new IDPairTable((IDiagram) network, observations);

        // Consider the impact of observations on relations

        restrictCurrentRelationsToObservations();

        // Loop to eliminate the variables

        for (i = notRemoved.size(); i > 0; i--) {

            // Select next variable to remove

            x = pt.nextToRemoveID();

            // Delete this node of notRemoved and pairTable

            notRemoved.removeNode(x);
            pt.removeVariable((FiniteStates) x);

            // Combine the potentials of this node

            //combined=combineRelationsOfNode((FiniteStates)x,pt);
            if (x.getKindOfNode() == Node.CHANCE) {
                combinePotentialsToRemoveChanceNode((FiniteStates) x, pt);
            } else {
                if (x.getKindOfNode() == Node.DECISION) {
                    combinePotentialsToRemoveDecisionNode((FiniteStates) x, pt);
                }
            }
        } // end for
    } // end method

    /**
     * Method to make the operations requiered to remove a chance node
     * @param node Node to remove
     * @param pt PairTable to store the relations of the nodes
     * @return <code>boolean</code> to show if the operation was made
     */
    private void combinePotentialsToRemoveChanceNode(FiniteStates node, IDPairTable pt) {
        Relation relC = null;
        Relation relU = null;

        // First at all, combine the probability potentials related to this node
        relC = combineProbabilityRelations(node, pt);
        // Combine the utility potentials related to node
        relU = combineUtilityRelations(node, pt);
        // Combine both of them
        if (relC != null && relU != null) {
            relU = combineRelations(relC, relU);
        }

        // Remove from them the node itself
        if (relC != null) {
            removeVariableOfRelation(relC, node);
        }

        if (relU != null) {
            removeVariableOfRelation(relU, node);
        }

        // We have to make relations for the new potentials
        if (relC != null) {
            currentRelations.insertRelation(relC);
        }
        if (relU != null) {
            relU.setKind(Relation.UTILITY);
            currentRelations.insertRelation(relU);
        }
    }

    /**
     * Method to make the operations requiered to remove a decision node
     * @param node Node to remove
     * @param pt PairTable to store the relations of the nodes
     * @return <code>boolean</code> to show if the operation was made
     */
    private void combinePotentialsToRemoveDecisionNode(FiniteStates node, IDPairTable pt) {
        Relation relC = null;
        Relation relU = null;
        Relation relEU;
        Vector vars = null;

        // First at all, combine the probability potentials related to this node
        relC = combineProbabilityRelations(node, pt);
        // Combine the utility potentials related to node
        relU = combineUtilityRelations(node, pt);
        // Combine both of them
        if (relC != null && relU != null) {
            relU = combineRelations(relC, relU);
        }
        // Remove from them the node itself, maxMarginalizing
        if (relC != null) {
            removeVariableOfRelation(relC, node);
        }

        // Finally, divide them to get the final utility
        if (relC != null && relU != null) {
            relU = combineRelations(relU, relC);
        }

        // Before removing the node itself, determine the variables present
        // at decision tables
        getExpectedUtility(node, relU);
        if (relU != null) {
            removeVariableOfRelation(relU, node);
        }

        // We have to make relations for the new potentials
        if (relC != null) {
            currentRelations.insertRelation(relC);
        }
        if (relU != null) {
            relU.setKind(Relation.UTILITY);
            currentRelations.insertRelation(relU);
        }
    }

    /**
     * Private method to combine the relations of probability related
     * to a given node
     * @param node Node to consider
     * @param pt PairTable containing the relations for every node
     * @return the final relation after combination
     */
    private Relation combineProbabilityRelations(FiniteStates node, IDPairTable pt) {
        RelationList relations = new RelationList();
        Relation relC = null;
        Relation relation;
        int i;

        // Get the relations related to node
        if (currentRelations != null) {
            relations = currentRelations.getRelationsOfAndRemove(node, Relation.POTENTIAL);
        }
        // Consider all the probability potentials where node
        // takes part and combine them
        for (i = 0; i < relations.size(); i++) {
            // Get the relation
            relation = relations.elementAt(i);
            // Consider it if it is not an utility potential
            if (relation.getKind() != Relation.UTILITY) {
                // Combine the potential
                relC = combineRelations(relC, relation);
                // Remove this relation from pt
                pt.removeRelation(relation);
            }
        }

        // Return the potential
        return relC;
    }

    /**
     * Private method to combine the relations of utility related
     * to a given node
     * @param node Node to consider
     * @param pt PairTable containing the relations for every node
     * @return the final relation after combination
     */
    private Relation combineUtilityRelations(Node node, IDPairTable pt) {
        RelationList relations = new RelationList();
        Relation relU = null;
        Relation relation;
        int i;

        // Get the relations related to node
        if (currentRelations != null) {
            relations = currentRelations.getRelationsOfAndRemove(node, Relation.UTILITY);
        }
        // Consider all the probability potentials where node
        // takes part and combine them
        for (i = 0; i < relations.size(); i++) {
            // Get the relation
            relation = relations.elementAt(i);
            // Consider it if it is an utility potential
            if (relation.getKind() == Relation.UTILITY) {
                // Combine the relations
                relU = combineRelations(relU, relation);
                // Remove this relation from pt
                pt.removeRelation(relation);
            }
        }

        // Return the potential
        return relU;
    }

    /**
     * Method to get the expected utility for a decision table
     * @param Node decision node related to the decision table
     * @param Relation utility directly related to the decision
     */
    private void getExpectedUtility(Node node, Relation util) {
        Relation aux = null;
        Relation relC = null;
        Relation table;
        Relation rel;
        NodeList vars;
        Node utilVar;
        int kindOfRel;
        boolean added = false;
        int i;

        // Assign aux to util
        aux = util;
        // Consider the utility relations
        for (i = 0; i < currentRelations.size(); i++) {
            rel = currentRelations.elementAt(i);
            if (rel.isInRelation(node) == false && rel.getKind() == Relation.UTILITY) {
                aux = combineRelations(aux, rel);
                added = true;
            }
        }

        //Get the set of variables present in aux
        vars = aux.getVariables();
        // Get sure node is the last variable in the potential
        sendVarToEnd(aux, node);

        // The table to store does not include value nodes. For that reason we have
        // to explore all variables and delete utility variables (these variables are
        // in relations but not in potentials)

        table = aux.copy();
        table.setKind(Relation.POTENTIAL);
        vars = table.getVariables();
        for (i = 0; i < vars.size(); i++) {
            utilVar = vars.elementAt(i);
            if (utilVar.getKindOfNode() == Node.UTILITY) {
                removeVariableOfRelation(table, utilVar);
            }
        }

        decisionTables.insertRelation(table);
    }

    /**
     * Method to combine the variables of two relations
     * The result is a new Relation
     */
    private Relation combineRelations(Relation relA, Relation relB) {
        Relation result;
        NodeList varsInA, varsInB;
        NodeList finalVars = new NodeList();
        Node var;
        int i;

        if (relA == null) {
            result = relB;
            return result;
        } else {
            varsInA = relA.getVariables();
            if (relB == null) {
                result = relA;
                return result;
            } else {
                varsInB = relB.getVariables();
            }
        }

        // Any other way, merge the relations

        result = new Relation();
        finalVars.merge(varsInA);
        finalVars.merge(varsInB);

        for (i = 0; i < finalVars.size(); i++) {
            var = finalVars.elementAt(i);

            if (var.getKindOfNode() == Node.UTILITY) {
                finalVars.removeNode(var);
            }
        }

        // Remove all the value nodes from the relation

        result.setVariables(finalVars);

        // Return result

        return result;
    }

    /**
     * Method to remove a variable from a relation
     */
    private void removeVariableOfRelation(Relation rel, Node node) {
        NodeList variables = rel.getVariables();

        variables.removeNode(node);
    }

    /**
     * Method to send a variable to the end in the list of variables
     */
    private void sendVarToEnd(Relation rel, Node node) {
        NodeList variables = rel.getVariables();

        variables.removeNode(node);
        variables.insertNode(node);
    }
} // End of class

