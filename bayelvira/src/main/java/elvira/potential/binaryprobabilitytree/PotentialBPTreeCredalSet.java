/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elvira.potential.binaryprobabilitytree;

import elvira.potential.Potential;
import elvira.potential.CredalSet;
import elvira.FiniteStates;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents a Credal sets implemented as a binary probability tree
 * @author acu
 */
public class PotentialBPTreeCredalSet extends PotentialBPTree implements CredalSet {

    /**
     * Creates a new instances of PotentialBPTreeCredalSet
     */
    public PotentialBPTreeCredalSet() {
    }

    /**
     * Creates a new instances of PotentialBPTreeCredalSet from another Potential
     * @param pot the Potential to be converted to PotentialBPTreeCredalSet
     */
    public PotentialBPTreeCredalSet(Potential pot) {
        super(pot);
    }

    /**
     * Combines this potential with the argument. The argument <code>pot</code>
     * can be any class of <code>Potential</code> .
     * @returns a new <code>PotentialBPTreeCredalSet</code> consisting of the combination
     * of <code>pot</code> and this <code>PotentialBPTreeCredalSet</code>.
     */
    @Override
    public Potential combine(Potential pot) {
        PotentialBPTreeCredalSet newPot = new PotentialBPTreeCredalSet();
        newPot.setVariables(elvira.SetVectorOperations.union(getVariables(),
                pot.getVariables()));
        newPot.setTree(getTree().combine(((PotentialBPTreeCredalSet) pot).getTree()));

        return newPot;
    }

    /**
     * Removes the variable var from this PotentialBPTreeCredalSet summing over all
     * its values.
     * @param var a <code>FiniteStates</code> variable.
     * @return a new <code>BinaryPotentialTreeCredalSet</code> with the result of the
     * operation.
     */
    @Override
    public Potential addVariable(elvira.Node var) {
        PotentialBPTreeCredalSet newPot = new PotentialBPTreeCredalSet();
        Vector<FiniteStates> potVariables;

        potVariables = (Vector<FiniteStates>) getVariables().clone();
        potVariables.removeElement(var);
        newPot.setVariables(potVariables);
        newPot.setTree(getTree().addVariable((FiniteStates) var));

        return newPot;
    }

    /**
     * Removes from this potential the variables which does not
     * take part into the BinaryProbabilityTree used to store the values
     */
    public void removeTransNotInTree() {
        // Get the list of transparents which take part into the tree
        Set<FiniteStates> transparents = getTree().getListTransparents();
        // All variables in the potential
        Vector variables = getVariables();
        Vector varsToRemove = new Vector();

        // May be there are not transparents in the tree but in the
        // potential are present
        if (transparents == null){
            transparents=new HashSet<FiniteStates>();
        }         

        // Now remove the transparents in the PotentialTree if they do not
        // appear in the tree. Go on the set of all variables, one by one
        for(int i = 0; i < variables.size(); i++) {
            FiniteStates var = (FiniteStates)variables.elementAt(i);
            // If transparent, consider if it must be removed
            if(var.getTransparency() == FiniteStates.TRANSPARENT) {
                // If there are transparent variables in the tree, 
                // check if it is present, and if not, add it to be 
                // removed
                if(!transparents.contains(var)) {
                    varsToRemove.addElement(var);
                }
            }
        }
        // Removes all transparents not present in the tree from the potential
        for(int i = 0; i < varsToRemove.size(); i++) {
            variables.remove(varsToRemove.elementAt(i));
        }
    }

    /**
     * Gets the size (number of values) of the potential.
     * @return the number of values (size) of the potential.
     */
    @Override
    public long getSize() {
      return this.getNumberOfLeaves();
    }
}
