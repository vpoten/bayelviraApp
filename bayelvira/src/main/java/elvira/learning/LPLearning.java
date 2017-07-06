
package elvira.learning;

import elvira.*;
import elvira.database.*;
import elvira.learning.*;
import elvira.potential.*;
import java.io.*;

public class LPLearning extends ParameterLearning {

public LPLearning(DataBaseCases input, Bnet net){

   setInput(input);
   setOutput(net);

}


public void learning(){

  int i;
  Relation relation;
  PotentialTable potential;
  FiniteStates nodei;
  NodeList vars,varsDb,pa;
  
  getOutput().getRelationList().removeAllElements();
  for(i=0 ; i< getOutput().getNodeList().size() ; i++){
    nodei = (FiniteStates) getOutput().getNodeList().elementAt(i);
    pa = getOutput().parents(nodei);
    vars = new NodeList();
    vars.insertNode(nodei);
    vars.join(pa);
    relation = new Relation(vars.toVector());
    varsDb = getInput().getNodeList().intersectionNames(vars).sortNames(vars);
    potential = getInput().getPotentialTable(varsDb);
    potential.LPNormalize();
    if(vars.size()>1){
      nodei = (FiniteStates) varsDb.elementAt(0);
      potential =(PotentialTable) potential.divide(potential.addVariable(nodei));
    }
    potential.setVariables(vars.toVector());
    relation.setValues(potential);
    getOutput().getRelationList().addElement(relation);
    //potential.print();
    //try{System.in.read();}catch(IOException iee){};
  }


}

}