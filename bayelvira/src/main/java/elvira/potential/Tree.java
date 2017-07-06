/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elvira.potential;

import elvira.FiniteStates;
import elvira.NodeList;

/**
 *
 * @author rcabanas
 */
public interface Tree {

    public NodeList getVarList();

    public double information(FiniteStates y, long potentialSize);

    public double informationUtility(FiniteStates y, long potentialSize);
    
}
