
/* QualitativeArcReversal.java */
package elvira.inference.elimination.ids;

import java.util.Vector;
import java.io.*;
import elvira.*;
import elvira.IDiagram;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.tools.Crono;
import elvira.parser.ParseException;

/**  
 * Class <code>QualitativeArcReversal</code>. Implements the solution of 
 * a regular ID (Influence Diagram). No updates of utilities nor probabilities
 * are made. The objective is to get measures about the evaluation process and
 * determine the variables relevant for decision tables.
 * @author Manuel Gomez 
 * @since 06/02/2002
 */
public class IDQualitativeArcReversal extends ArcReversal {

    /*
     * Vector to store the order of operation
     */
    Vector orderOfElimination;

    /*
     * Vector to store the order of instantiation
     */
    Vector orderOfInstantiation;

    /**
     * Creates a new qualitative propagation
     */
    public IDQualitativeArcReversal(IDiagram id) {

        super(id.qualitativeCopy());
        orderOfElimination = new Vector();

        // After this operation the diag is complete, without any
        // change, so could be used again
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
     * Gives an order of instantiation
     *
     */
    public void produceOrderOfInstantiation() {
        Graph ancestralGraph;
        NodeList variables;
        NodeList variablesInDiagNew;
        IDiagram qualitativeID;
        IDiagram original;

        // First at all, make a copy of the original diagram
        // We must preserve this diagram to get the order of
        // instantiation, once known the relevant variables
        // (those that apper in decision tables)

        original = diag;
        qualitativeID = diag.qualitativeCopy();
        diag = qualitativeID;

        // Now, evaluate the diagram to get the decision tables
        // As diag points to qualitativeID, no change is done on
        // the original diagram, but in its copy

        evaluateDiagram();

        // Get the variables relevant for decision tables. With this way,
        // the name of the variables (pointing to nodes in the qualitative
        // copy used for the qualitative evaluation) are used to point to
        // the proper variables in this diagram

        variables = variablesInDecisionTables();

        // Restore the original diagram

        diag = original;

        // Translate the decision tables obtained for qualitativeID;
        // the variables must be pointed to the relatives in diag

        decisionTables = diag.translateRelations(getDecisionTables());

        // Look for relevant variables. With them build the ancestral
        // graph to look for the order of instantiation

        variablesInDiagNew = diag.insertVariablesIn(variables);

        // These variables are used to get the ancestral graph

        ancestralGraph = diag.ancestral(variablesInDiagNew);

        // From this graph we can get the order of instantiation

        orderOfInstantiation = diag.giveInstantiationOrder(ancestralGraph);

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

        IDQualitativeArcReversal eval;
        Graph ancestralGraph;
        FileInputStream networkFile, networkFile1;
        NodeList variables = new NodeList();
        NodeList variablesInDiagNew;
        NodeList phase;
        Vector relevant, order;
        boolean evaluable;
        IDiagram diag, diagNew, qualitativeID;
        Crono crono = new Crono();
        int i, j;

        crono.start();
        diag = (IDiagram) Network.read(args[0], false);
        crono.viewTime();

        diagNew = diag.qualitativeCopy();
        qualitativeID = diag.qualitativeCopy();

        System.out.println("Segundos para copia de red: ");
        crono.viewTime();

        eval = new IDQualitativeArcReversal(diag);

        // initial chekout about the node.

        evaluable = eval.initialConditions();
        System.out.print("Evaluable : " + evaluable + "\n\n");
        crono.viewTime();

        // If the diagram is suitable to be evaluated, then do it.

        if (evaluable == true) {
            eval.diag.eliminateRedundancy();
            eval.evaluateDiagram();
            eval.decisionTables.print();
            order = eval.getOrderOfElimination();

            // Display the information

            for (i = 0; i < order.size(); i++) {

                // Recorrido de las variables

                System.out.println("FASE = " + i);
                System.out.println(".......................................");
                System.out.println("Eliminacion : " + order.elementAt(i));
                System.out.println(".......................................");
            }


            // Build a NodeList with all the variables in decisionTables

            for (i = 0; i < eval.decisionTables.size(); i++) {
                variables.merge((eval.decisionTables.elementAt(i)).getVariables());
            }

            // Get a NodeList with the variable, but in diagnew

            variablesInDiagNew = diagNew.insertVariablesIn(variables);

            // In it, get all the parents of the nodes in variables

            ancestralGraph = diagNew.ancestral(variablesInDiagNew);
            System.out.println("Para obtener el grafo ancestral.....");
            crono.viewTime();

            // Recorrido de las variables

            // Now, get the order

            eval = new IDQualitativeArcReversal(diagNew);
            order = eval.diag.giveInstantiationOrder(ancestralGraph);
            System.out.println("Para obtener el orden de instanciacion.....");
            crono.viewTime();

            // Display the information

            for (i = 0; i < order.size(); i++) {

                // Recorrido de las variables

                System.out.println("FASE = " + i);
                System.out.println("........................................");
                System.out.println("Instanciacion : " + order.elementAt(i));
                System.out.println("........................................");
            }

            // Check the orden

            System.out.println("Order OK = " + eval.diag.checkInstantiationOrder(order));
        }

        // Tiempo total

        crono.stop();
    }

    /**
     * Evaluates the Influence Diagram associated with the class
     * using Shachter's Algorithm.
     */
    public void evaluateDiagram() {

        Node value;
        Relation rel;
        int i = 0;

        // Get a reference to the value node

        value = diag.getValueNode();

        // Main loop: while value node has parents

        while (value.hasParentNodes() == true) {
            // First, try to eliminate a chance node

            if (removeChanceNode() == false) {
                // Try it with a decision node

                if (removeDecisionNode() == false) {
                    // Try an arc inversion

                    if (reverseArc() == false) {
                        System.out.print("Error en la aplicacion del algoritmo de evaluacion\n");
                        value.print();
                        return;
                    }
                }
            }
        }
    }

    /**
     * To remove a chance node
     * @return the result of the operation, as a <code>boolean</code>.
     */
    private boolean removeChanceNode() {

        Node util;
        Node candidate;
        NodeList parents;
        int i;

        // Obtain the value node

        util = diag.getValueNode();
        parents = diag.parents(util);

        for (i = 0; i < parents.size(); i++) {

            candidate = parents.elementAt(i);

            if (candidate.getKindOfNode() == candidate.CHANCE) {
                if ((diag.descendantsList(candidate)).size() == 1) {
                    // The node can be removed
                    // The relation of the utility node is modified. In this
                    // case the parents of the node to remove will be parents
                    // of the utility node

                    modifyUtilityLinks(candidate, true);

                    // The node is deleted

                    orderOfElimination.addElement(candidate.getName());
                    diag.removeNodeOnly(candidate);

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
        int i;

        // Obtain the value node

        util = diag.getValueNode();
        parents = diag.parents(util);

        for (i = 0; i < parents.size(); i++) {

            candidate = parents.elementAt(i);

            if (candidate.getKindOfNode() == candidate.DECISION) {

                if (diag.decisionReadyToRemove(candidate) == true) {
                    // The node can be removed. Store the relevant variables

                    storeQualitativeDecisionTable(util, candidate, decisionTables);

                    // The relation of the utility node is modified. In this
                    // case the parents of the node to remove wont be parents
                    // of the utility node

                    modifyUtilityLinks(candidate, false);

                    // The node is deleted

                    orderOfElimination.addElement(candidate.getName());
                    diag.removeNodeOnly(candidate);

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
                        && candidate.isUtilityParent() == true
                        && candidateChildren.size() > 1) {

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

                                // We modify the relations of the nodes

                                modifyLinks(candidate, dest);

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
} // End of class

