package elvira.translator.elv2hugin;

import elvira.Network;
import elvira.Bnet;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.FiniteStates;
import elvira.Configuration;
import elvira.IDiagram;
import elvira.potential.Potential;

import java.io.*;
import java.util.Vector;

public class Elvira2Hugin {

    /**
     * Net to translate
     */
    private Bnet toTranslate;

    /**
     * Class constructor
     * @param net
     */
    public Elvira2Hugin(Bnet net) {
        toTranslate = net;
    }

    /**
     * Translation method
     */
    public void save() throws IOException {
        System.out.println("Saving to : " + toTranslate.getName() + ".net file");
        FileWriter file = new FileWriter(toTranslate.getName() + ".net");
        PrintWriter p = new PrintWriter(file);
        NodeList nodes;
        Vector relations;
        Relation relation;
        Potential pot;
        Node node;
        boolean utility;

        // Print the header: net nameOfId{}. There will be no options
        // related to graphical mode, etc
        p.println("net {}\n");

        // Print the list of nodes: for chance nodes use the key word
        // "node", for decision nodes "decision" and for utility nodes
        // "utility". After that the name and between braces the position
        // (if defined) and the set od states
        nodes = toTranslate.getNodeList();

        for (int i = 0; i < nodes.size(); i++) {
            node = nodes.elementAt(i);
            utility = false;

            switch (node.getKindOfNode()) {
                case Node.CHANCE:
                    p.println("node " + node.getName() + "{");
                    break;
                case Node.DECISION:
                    p.println("decision " + node.getName() + "{");
                    break;
                case Node.UTILITY:
                    p.println("utility " + node.getName() + "{");
                    utility = true;
                    break;
            }

            // Print the states if it is not and utility
            if (!utility) {
                p.print("   states = (");

                for (int j = 0; j < ((FiniteStates) node).getNumStates(); j++) {
                    p.print("\"" + ((FiniteStates) node).getState(j) + "\"");
                    if (j < ((FiniteStates) node).getNumStates() - 1) {
                        p.print(" ");
                    }
                }
                p.println(");");
            } else {
                // Print the label for the utility: the name of the node
                p.println("   label = \"" + node.getName() + "\";");
            }

            // Anyway, print }
            p.println("}\n");
        }

        // Now print the information about the relations
        relations = toTranslate.getRelationList();
        for (int i = 0; i < relations.size(); i++) {
            relation = (Relation) relations.elementAt(i);

            // Consider if it is not a constraint relation
            if (relation.getKind() != Relation.CONSTRAINT) {
                // Print the identification of potential
                p.print("potential (");

                // Print the variables related
                nodes = relation.getVariables();
                for (int j = 0; j < nodes.size(); j++) {
                    node = nodes.elementAt(j);
                    p.print(node.getName());
                    if (nodes.size() > 1 && j == 0) {
                        p.print(" |");
                    }

                    // Print the separation between names
                    if (j < nodes.size() - 1) {
                        p.print(" ");
                    }
                }

                // At the end, print the final brace
                p.print("){\n   data = (");

                // Now print the values
                pot = relation.getValues();

                if (relation.getKind() == Relation.UTILITY) {
                    // Get the variables of the potential and make a configuration
                    // with them
                    Configuration conf = new Configuration(pot.getVariables());
                    for (int j = 0; j < conf.possibleValues(); j++) {
                        if (j % 6 == 0) {
                            p.println();
                            p.print("        ");
                        }
                        p.print(pot.getValue(conf) + " ");

                        // Jump to the next configuration
                        conf.nextConfiguration();
                    }
                } else {
                    // It is a probability potential
                    Vector vars = pot.getVariables();
                    FiniteStates first = (FiniteStates) vars.elementAt(0);
                    Configuration complete = new Configuration(vars);
                    Configuration confForRest = new Configuration(pot.getVariables(), first.getName());
                    for (int j = 0; j < confForRest.possibleValues(); j++) {
                        p.print("        ");
                        complete.resetConfiguration(confForRest);
                        for (int k = 0; k < first.getNumStates(); k++) {
                            complete.putValue(first.getName(), k);
                            p.print(pot.getValue(complete) + " ");
                        }
                        p.println();
                        confForRest.nextConfiguration();
                    }
                }

                // Print the final brace
                p.println(");\n}\n");
            }
        }

        // It is needed to introduce artificial relations for representing
        // the links to decision nodes
        if (toTranslate.getClass() == IDiagram.class) {
            NodeList decisions = ((IDiagram) toTranslate).getDecisionList();
            NodeList parents;
            Node decision, parent;
            for (int i = 0; decisions != null && i < decisions.size(); i++) {
                decision = decisions.elementAt(i);
                parents = decision.getParentNodes();
                p.print("\npotential ( " + decision.getName());
                if (parents.size() != 0) {
                    p.print(" | ");
                    for (int j = 0; j < parents.size(); j++) {
                        parent = parents.elementAt(j);
                        p.print(parent.getName() + " ");
                    }
                }

                // Print the final brace
                p.println(")\n{}\n");
            }
        }

        // Finally close the file and the writer
        p.close();
        file.close();
    }

    // Main method for testing
    public static void main(String args[]) throws Exception{
        Network net=Network.read(args[0]);
        // Crwates a Elvira2Hugin object
        Elvira2Hugin translator=new Elvira2Hugin((Bnet)net);
        // Translate it
        translator.save();
    }
}
