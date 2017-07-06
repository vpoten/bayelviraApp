package elvira.tools.idiagram;

import elvira.IDiagram;
import elvira.Network;
import elvira.NodeList;
import elvira.Node;
import elvira.Link;
import elvira.FiniteStates;
import elvira.Continuous;
import elvira.Relation;
import elvira.ValuesSet;
import elvira.LogicalNode;
import elvira.InvalidEditException;
import elvira.parser.ParseException;
import elvira.potential.PotentialTable;
import elvira.potential.LogicalExpression;
import elvira.translator.elv2hugin.Elvira2Hugin;

import java.util.ArrayList;
import java.util.Vector;
import java.util.Random;
import java.util.HashMap;
import java.io.IOException;

/**
 * Class for generating random ids, with a method based on the
 * temporal structure of IDs. Nodes are allocated in a grid which
 * dimensions are passed as argument. The number of nodes will be
 * given by the wished percentage of coverage for the grid cells.
 * The number of decision nodes, values nodes, maximum number of
 * parents, maximum number of states and number of constraints
 * are another parameters to fix for this method to work
 */
public class IDRandomGenerator {

   /**
    * Data member for storing the final ID generated
    */
   private IDiagram result;
   
   /**
    * Grid for painting the nodes as they are added
    */
   //private VisualGrid visualGrid;
   
   /**
    * Data member to show the number of cells in X-axis
    */
   private int width;
   /**
    * Data member for number of cells is Y-axis
    */
   private int height;
   /**
    * Grid of cells
    */
   private FiniteStates[][] grid;
   /**
    * Data member for the percentage of density (percentage
    * of cells containing nodes)
    */
   private double percentage;
   
   /**
    * Data member for storing the percentage of constrained
    * respect to the total size of potentials for the ID
    */
   private double percConstraint;

   /**
    * Data member for storing the number of constrained
    * configurations
    */
   private double constrainedConfigurations;
   
   /**
    * Data member for storing the number of decisions
    */
   private int numberDecisionNodes;
   /**
    * Data member for the number of value nodes
    */
   private int numberValueNodes;

   /**
    * Data member for storing the nimber of chance nodes
    */
   private int numberChanceNodes;

   /**
    * Data member for the maximum number of parents
    */
   private int maximumNumberParents;

   /**
    * Data member to store the real number of parents for
    * every node. For chance nodes will be selected at
    * random between 1 and maximumNumberParents
    */
   private int []realNumberParents;

   /**
    * Data member for the maximum number of states
    */
   private int maximumNumberStates;
   /**
    * Data member for the final number of nodes to create
    */
   private int numberNodes;

   /**
    * Private data member for storing the number of stages
    */
   private int numberStages;

   /**
    * Data member for storing the width of a stage for chance
    * nodes. Stages for decision nodes will be one cell width
    */
   private int stageWidth;
   
   /**
    * Private data member for adding to X as suffix for chance
    * nodes
    */
   private int chanceNodesSuffix;

   /**
    * Private data member for fixing a global index for the nodes
    * of the ID
    */
   private int globalNodesIndex;
   
   /**
    * Data member to store the number of chance nodes not created
    * yet
    */
   private int remainingChanceNodes;
   
   /**
    * Array list containing the nodelist with the nodes
    * of the stages
    */
   private ArrayList<NodeList> stages;

   /**
    * ArrayList for storing the value nodes
    */
   private ArrayList<Node> valueNodes;

   /**
    * Data member to store the indexes od the nodes
    */
   private HashMap<String,Integer> indexes;

   /**
    * Array containing the first cells for the stages
    */
   private int firstCell[];

   /**
    * Array containing the last cell for the stage
    */
   private int lastCell[];

   /**
    * Matrix to control adyacent nodes
    */
   private int adyacents[][];
   
   /**
    * Data member for storing the size of the potentials
    */
   private double size;

   /**
    * Random number generator
    */
   private Random random;

   /**
    * Default constructor
    */
   public IDRandomGenerator(){
   }

   /**
    * Constructor
    * @param width of the grid (in cells)
    * @param heoght of the grid (in cell)
    * @param percentage
    * @param numberDecisionNodes
    * @param numberValueNodes
    * @param maxNumberParents maximum number of parents for chance
    *        nodes
    * @param maxNumberStates for nodes
    * @param percConstraint for xonstrained configurations
    */
   public IDRandomGenerator(int width, int height, double percentage, int numberDecisionNodes, 
                            int numberValueNodes, int maxNumberParents, int maxNumberStates,
                            double percConstraint) {
      // Create the random number generator
      random = new Random();
      
      // Initialize stageWidth
      stageWidth=0;
      
      // Set width and height
      this.width=width;
      this.height=height;

      // Set the maximum number of parents and the maximum number of
      // states
      maximumNumberParents=maxNumberParents;
      maximumNumberStates=maxNumberStates;
      
      // Set the percentage of constraint
      this.percConstraint=percConstraint;

      // Set constrainedConfigurations to 0
      constrainedConfigurations=0;

      // Compute the number of nodes to generate
      this.percentage = percentage;

      // Compute the number of chance nodes
      numberNodes = (int) percentage * (width * height) / 100;

      // Check that the number of decisions is bigger than
      // width and finalNumberNodes
      if (numberDecisionNodes > width || numberDecisionNodes > numberNodes) {
         System.out.println("The number of decisions is not OK");
         System.out.println("Must be < " + width + " and < " + numberNodes);
         System.exit(0);
      }

      this.numberDecisionNodes = numberDecisionNodes;

      // Compute the number of stages
      numberStages=2*numberDecisionNodes+1;
System.out.println("Etapas: "+numberStages);

      // If the width have been assigned, reconsider its value for sharing
      // the space between the decision nodes
      if (width != 0){
        reconsiderWidth();
      }

      // Check the number of value nodes: must be less than
      // width and less than finalNumberNodes
      if (numberValueNodes > width || numberValueNodes > numberNodes) {
         System.out.println("The number of value nodes is not OK");
         System.out.println("Must be < " + width + " and < " + numberNodes);
         System.exit(0);
      }

      this.numberValueNodes = numberValueNodes;

      // Compute the number of chance nodes
      numberChanceNodes=numberNodes-numberDecisionNodes-numberValueNodes;

      // Create the hashmap for the indexes
      indexes=new HashMap<String,Integer>();
   }
   
   /**
    * Method for getting the id
    * @return result
    */
   public IDiagram getResult(){
       return result;
   }

   /**
    * Method for reconsidering the width of the grid
    */
   private void reconsiderWidth(){

      // Assign stageWith
      stageWidth=width/(numberStages-numberDecisionNodes);

      // If there is a reminder of the previous division,
      // adjust width
      if (width % (numberStages-numberDecisionNodes) != 0){
        stageWidth++;
      }

      // Set width to the final value
      width=stageWidth*(numberStages-numberDecisionNodes)+numberDecisionNodes;
   }

   /**
    * Method for creating the graphical interface
    */
   private void createInterface(){
      // The same for the visual grid
      javax.swing.SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            //visualGrid=new VisualGrid(height+1,width);
          }
      });

      // Wait for interface construction
      //do{
        //try{
         // Thread.sleep(10);
        //}catch(InterruptedException e){};
      //}while(visualGrid == null);
   }

   /**
    * Method for initializing the data members
    */
   private void initialize() {
      NodeList stageNodes;
      String decisionName;
      
      // Initialize globalNodesIndex
      globalNodesIndex=0;
      
      // Initializa chanceNodesSufix
      chanceNodesSuffix=1;

      // Create the matrix for adyacent nodes
      adyacents=new int[numberNodes][numberNodes];

      // Create the matrix for the real number of parent
      realNumberParents=new int[numberNodes];

      // Initialize the grid: the height will be given one unity more
      // for value nodes
      grid = new FiniteStates[width][height+1];

      // Initialize the array for keeping the stages
      stages = new ArrayList<NodeList>();
      firstCell=new int[numberStages];
      lastCell=new int[numberStages];
      
      // Initialize remainingChanceNodes
      remainingChanceNodes=numberChanceNodes;

      // Initialize them
      for (int i = 0; i < numberStages; i++) {
         stageNodes = new NodeList();
         stages.add(stageNodes);

         // Assign the first and last cell. For even stages (devoted
         // to store chance nodes, the with will be stageWidth). For
         // odd stages will be one (for decision nodes)
         if (i == 0){
           firstCell[i]=0;
           lastCell[i]=stageWidth-1;
         }
         else{
           firstCell[i]=lastCell[i-1]+1;

           if (i % 2 == 0){
             lastCell[i]=firstCell[i]+stageWidth-1;
           }
           else{
             lastCell[i]=firstCell[i];
           }
         }
      }

      // Generate the decisions for the model
      for (int i = 0; i < numberDecisionNodes; i++) {
         generateDecisionNode("D",i+1);
      }
      
      // Now generate the chance nodes for the even stages
      generateChanceNodes();

      // Generate value nodes
      generateValueNodes();
   }
   
   /**
    * Private method for generating a decision node for a given
    * stage
    * @param name
    * @param decisionIndex
    */
   private void generateDecisionNode(String name, int decisionIndex){
      Vector statesNames;
      String decisionName;
      FiniteStates decisionNode;
      NodeList stageNodes;
      int states,stage;
      
         // Compose the name for the node
         decisionName = new String("D" + (decisionIndex));

         // Get the random number of states
         states = generateNumberStates();

         // Compose the names of the states: d_{i+1}{1....states}
         statesNames = generateStatesNames(states, decisionIndex, true);

         // Create de decision with the name D_{i+1}
         decisionNode = new FiniteStates(decisionName, statesNames);
         decisionNode.setKindOfNode(Node.DECISION);
         //decisionNode.setIndex(globalNodesIndex);
         indexes.put(decisionNode.getName(),globalNodesIndex);

         // Set the limit for the number of parents
         realNumberParents[globalNodesIndex]=2*maximumNumberParents;

         // Add one to globalNodesIndex
         globalNodesIndex++;

         // Position the node in the graph in order to be visible
         // with elvira editor
         stage=2*decisionIndex-1;
         givePositionToNode(decisionNode, stage);

         // Insert the node in the array of stages. The index to
         // use will be stages-1
         stageNodes = stages.get(stage);
         stageNodes.insertNode(decisionNode);
   }

   /**
    * Private method for generating a random number of states
    * @return number of states
    */
   private int generateNumberStates() {
      int result;

      // The  number of states must be between 2 and maximumNumberStates
      if (maximumNumberStates == 2) {
         return 2;
      }
      // If this is not the case, get a random number os states
      // The argument is maximumNumberStates-1 because the random
      // generation will produce a value between 0 and
      // argument-1. With this the number will be between 0 and
      // maximumNumberStates-2. And as we add 2, the final number
      // will be between 2 and maximimNumberStates
      result = 2 + random.nextInt(maximumNumberStates - 1);

      // Return the result
      return result;
   }
   
   /**
    * Private method for generating chance nodes for the stages
    * not related to decision nodes
    */
   private void generateChanceNodes(){
      int stagesGenerated[]=new int[numberDecisionNodes+1];
      int stagesToDealWith=numberDecisionNodes+1;
      int stageSelected;
      int stage;
      
      // Deal with a stage randomly selected to obtain the nodes
      // it will contain. This will be needed for all the stages
      // containing chance nodes
      while(stagesToDealWith > 0){
         // Generate a random number between 0 and numberDecisionNodes+1
         // This number, multiplied by two it will give the stage to deal
         // with
         stageSelected=random.nextInt(numberDecisionNodes+1);
         
         // If this stage is not previously considered, generate nodes
         // for it
         if (stagesGenerated[stageSelected] == 0){
            generateChanceNodesForStage(stageSelected*2,stagesToDealWith);
            
            // Mark this stage as completed
            stagesGenerated[stageSelected]=1;
            
            // Decrement the number of stages to deal with
            stagesToDealWith--;
         }
      }
   }
   
   /**
    * Method for generating the nodes for a given stage
    * @param stageIndex
    * @param remainingStages
    */
   private void generateChanceNodesForStage(int stageIndex, int remainingStages){
      int possibleNodes;
      int minimum,maximum;
      int toAdd;

      // In this stage can be added as much nodes as showed
      // by the number of remaining nodes  divided by the number
      // of remaining stages plus a certain random percentage
      // between -20 and +20
      // The minimum number of nodes will be a 20% from the possible
      // number of nodes to be assigned
      if (remainingStages != 1){
        possibleNodes=remainingChanceNodes/remainingStages;
        maximum=possibleNodes+(possibleNodes*20/100);
        minimum=possibleNodes-(possibleNodes*20/100);
        
        // Select the number of nodes to add
        toAdd=minimum+random.nextInt(maximum-minimum+1);
      }
      else{
        // For the last stage consider the complete number
        // of nodes for adding numberChanceNodes. As the
        // set of value nodes must be generated in the future,
        // we must substract this number
        toAdd=remainingChanceNodes;
      }
      
      // Substract the number of nodes to add from remainingChanceNodes
      remainingChanceNodes-=toAdd;
      
      // Generate the selected number of nodes
      createChanceNodesForStage(stageIndex,toAdd);
   }
   
   /**
    * Private method for creating a given number of nodes for a
    * given stage
    * @param stageIndex
    * @param number of nodes to create
    */
   private void createChanceNodesForStage(int stageIndex,int number){
      Vector statesNames;
      String chanceName;
      FiniteStates chanceNode;
      NodeList stageChanceNodes=stages.get(stageIndex);
      int states;
      
      // Create the nodes
      for(int i=0; i < number; i++){
         chanceNode=createChanceNode();

         // Generate the random number of parents for the node
         realNumberParents[globalNodesIndex]=maximumNumberParents;

         // Add 1 to glbalNodesIndex
         globalNodesIndex++;
         
         // Add the chance node to the list of chance nodes for this
         // stage
         stageChanceNodes.insertNode(chanceNode);
      }
      
      // The nodes for the stage must be reordered by layers
      // in order to fix the relationships between them. As
      // the variables belong to a given stage, must be considered
      // they are parents of the next decision
      orderByLayers(stageIndex,stageChanceNodes);
   }

   /**
    * Method for creating a new chance node
    */
   private FiniteStates createChanceNode(){
      Vector statesNames;
      String chanceName;
      FiniteStates chanceNode;
      int states;

      // Compose the name for the node
      chanceName = new String("X" + (chanceNodesSuffix));
         
      // Get the random number of states
      states = generateNumberStates();
         
      // Compose the names of the states: d_{i+1}{1....states}
      statesNames = generateStatesNames(states, chanceNodesSuffix, false);
         
      // Add 1 to chanceNodeSuffix
      chanceNodesSuffix++;
         
      // Create the chance node
      // Create de decision with the name D_{i+1}
      chanceNode = new FiniteStates(chanceName, statesNames);
      chanceNode.setKindOfNode(Node.CHANCE);
      //chanceNode.setIndex(globalNodesIndex);
      indexes.put(chanceNode.getName(),globalNodesIndex);

      // Return chanceNode
      return chanceNode;
   }

   /**
    * Method for reordering the chance nodes of a stage according to its
    * position. This position will be obtained at random
    */
   private void orderByLayers(int index, NodeList nodes){
     Node node;

     // Consider the nodes one by one
     for(int i=0; i < nodes.size(); i++){
       node=nodes.elementAt(i);

       // Get a position for the node
       givePositionToNode((FiniteStates)node,index);
     }
   }

   /**
    * Private method for creating the value nodes of the ID
    */
   private void generateValueNodes(){
     // Initialize the array list for the value nodes
     valueNodes=new ArrayList<Node>();

     // Loop of creation
     for(int i=0; i < numberValueNodes; i++){
       generateValueNode("V",i);
     }
   }

   /**
    * Private method for generating a value node
    * @param name for the node
    * @index of the value node
    */
   private void generateValueNode(String nodeName,int index){
     Node valueNode;
     NodeList stageNodes;
     String finalName;
     int stage;

     // Create the value node
     finalName=new String(nodeName+index);
     valueNode = new Continuous(finalName);
     valueNode.setName(finalName);
     valueNode.setKindOfNode(Node.UTILITY);
     //valueNode.setIndex(globalNodesIndex);
     indexes.put(valueNode.getName(),globalNodesIndex);

     // Set the real number of parents for the node: will be selected
     // at random
     realNumberParents[globalNodesIndex]=(random.nextInt(maximumNumberParents)+2);
System.out.println("Numero de padres para "+finalName+" = "+realNumberParents[globalNodesIndex]);
System.out.println();

     // Add 1 to globalNodesIndex
     globalNodesIndex++;

     // Position the node in the graph in order to be visible
     // with elvira editor
     givePositionToNode(valueNode,0);

     // Insert it into valueNodes data member
     valueNodes.add(valueNode);
   }

   /**
    * Private method for generating the names of the states
    * @param number of states to name
    * @param index of the variable
    * @param flag true if decision, false if chance node
    * @return vector with the names
    */
   private Vector generateStatesNames(int states, int index, boolean flag) {
      Vector result = new Vector();
      String name;
      String base;

      // Compose the base: d for decisions and c for chance
      // nodes
      if (flag == true) {
         base = new String("d");
      } else {
         base = new String("x");
      }

      // Now add the index
      base = base + index;

      // Now compose the names for the states
      for (int i = 0; i < states; i++) {
         name = new String(base + "_" + (i + 1));
         result.add(name);
      }

      // Return the vector of states
      return result;
   }

   /**
    * Private method for positioning a node in the grid
    * @param var to position
    * @param index of the var
    */
   private void givePositionToNode(Node var, int index) {
      int x = 0;
      int y = 0;

      switch (var.getKindOfNode()) {
         case Node.DECISION:
            //It will be in the center of the space for its stage
            y = height / 2;

            // The x position will depend on the index of the node
            x = firstCell[index];

            break;
         case Node.CHANCE:
            while(true){
              // x and y coordinates will be selected at random
              x = random.nextInt(stageWidth)+firstCell[index];
              y = random.nextInt(height);
              break;

              // Check if the position is already in use
              //if (visualGrid.isFull(y,x) == false){
                // Break the loop
                //break;
              //}
              //else{
               // System.out.println("Repetir posicionado por ocupacion de celda");
              //}
            }
            break;
         case Node.UTILITY:
            // It will be at the end of the graphic
            y = height;

            do{
               // The x position will be selected at random 
               x = random.nextInt(width);
               break;

               // Check if the position is free
               //if (visualGrid.isFull(y,x) == false){
                 // Break the loop
                 //break;
               //}
            }while(true);

            break;
      }

      // Set these values to the node
      var.setPosX(x);
      var.setPosY(y);
      
      // Make it visible in the visualGrid
      //visualGrid.addNode(var.getKindOfNode(),y,x);
   }
   
   /**
    * Method for making a complete generation
    * @param name for the id
    * @param check 
    * @param constraints
    * @param info
    */
   public void completeGeneration(String name, boolean check, boolean constraints, boolean info){
       // Generate the ID
       generateID(name);
       
       // Check if needed
       if (check){
           checkResult();
       }
       
       // Generate potentials
       generatePotentials();
       
       // If needed generate constraints
       if (constraints && percConstraint != 0){
          generateConstraints();
       }
       
       // Get information if needed
       if (info){
           getInformation();
       }
   }

   /**
    * Public method for building the ID once all the nodes are
    * created
    * @param name
    */
   public void generateID(String name) {
      Node node;

      // Create the user interface
      createInterface();

      // Create the IDiagram
      result = new IDiagram();

      // Initialize all what is needed
      initialize();

      // Set the name
      result.setName(name);

      // Compose a complete NodeList with the whole set
      // of nodes
      NodeList finalNodes = new NodeList();

      // Consider all the stages and add nodes to finalNodes
      for (NodeList stageNodes : stages) {
         for (int i = 0; i < stageNodes.size(); i++) {
            node = stageNodes.elementAt(i);
            finalNodes.insertNode(node);
         }
      }

      // Add the value nodes
      for(int i=0; i < valueNodes.size(); i++){
        node=valueNodes.get(i);
        finalNodes.insertNode(node);
      }

      // All the nodes are set as the nodes for the id
      result.setNodeList(finalNodes);

      // Set the links between the elements of the problem
      setLinks();

      // Avoid barren nodes
      avoidBarrenNodes();

      // Reconsider value nodes: divorcing needed?
      applyDivorcingParents();
   }

   /**
    * Private method for testing if the result is evaluable
    */
   private void checkResult(){
     boolean cycles;

     cycles=result.hasCycles();
     if (cycles){
       System.out.println("It is not evaluable due to cycles");
       printMatrix(adyacents);
       System.exit(0);
     }
     
     result.addNonForgettingArcs();

     // Finally, eliminate redundancy
     //result.eliminateRedundancy();
   }

   /**
    * Method for randomizing the numbers for a given ID
    * @param id to transform
    */
   public void randomizeID(String diagName){
      try{
         result=(IDiagram)Network.read(diagName);
      }catch(ParseException e){
      }catch(IOException e){};

      // Give it a name
      result.setName("random_"+result.getName());

      // Now generate potentials. Before that retrieve the
      // list of value nodes
      NodeList valNodes=result.getValueNodes();
      valueNodes=new ArrayList<Node>();
      for(int i=0; i < valNodes.size(); i++){
        valueNodes.add(valNodes.elementAt(i));
      }
     
      // Clean the actual list of relations
      Vector newRelations=new Vector();
      result.setRelationList(newRelations);
      generatePotentials();
   }

   /**
    * Private methos for generating the potentials of the ID
    */
   private void generatePotentials(){
     NodeList nodes=result.getNodesOfKind(Node.CHANCE);
     NodeList nodeParents,variablesInRel;
     PotentialTable potential;
     Vector relations=result.getRelationList();
     Relation relation;
     Node node;

     // Consider the chance nodes one by one
     for(int i=0; i < nodes.size(); i++){
        node=nodes.elementAt(i);

        // Make a new NodeList with node
        variablesInRel=new NodeList();
        variablesInRel.insertNode(node);

        // Get the parents of node
        nodeParents=node.getParentNodes();

        // Add parent nodes to variablesInRel
        variablesInRel.join(nodeParents);

        // Make a relation with node and nodeParents
        relation=new Relation();

        // Set the variables in variablesInRel
        relation.setVariables(variablesInRel);
System.out.println("   Pot. de prob: "+variablesInRel.getSize());
{
 System.out.print("   POT(");
 FiniteStates var;
 for(int p=0; p < variablesInRel.size();p++){
   var=(FiniteStates)variablesInRel.elementAt(p);
   System.out.print(var.getName()+ " ");
 }
System.out.println("-------------------------------------------");
}

        // Generate a potentialtable for this relation
        //potential=new PotentialTree(variablesInRel,Relation.CONDITIONAL_PROB);
        potential=new PotentialTable(new Random(),variablesInRel,1);
        
        // Set the potential as the values of the relation
        relation.setValues(potential);

        // La relacion se agrega a la lista de relaciones
        relations.add(relation);
     }

     // Now make the same for the value nodes
     for(int i=0; i < valueNodes.size(); i++){
       node=valueNodes.get(i);

       // Make a new NodeList with node
       variablesInRel=new NodeList();
       variablesInRel.insertNode(node);

       // Get the parents of node
       nodeParents=node.getParentNodes();

       // Add parent nodes to variablesInRel
       variablesInRel.join(nodeParents);

       // Make a relation with node and nodeParents
       relation=new Relation();
       relation.setKind(Relation.UTILITY);

       // Set the variables in variablesInRel
       relation.setVariables(variablesInRel);
System.out.println("   Pot. de util: "+variablesInRel.getSize());

       // Generate a potentialtree for this relation
       //potential=new PotentialTree(nodeParents,Relation.UTILITY);
       potential=new PotentialTable(new Random(),nodeParents,100.0); //ORIGINAL
       //PotentialGenerator gen = new PotentialGenerator(1234);
       //potential = new PotentialTable(gen.getRandomUtilityTable(nodes, 100.0));

       // Set the potential as the values of the relation
       relation.setValues(potential);

       // Add the relation to the list of relations
       relations.add(relation);
     }
   }
   
   /**
    * Private method for generating the constraints over the
    * potentials
    */
   private void generateConstraints(){
      NodeList chanceNodes=result.getNodesOfKind(Node.CHANCE);
      ArrayList<Relation> relationsConsidered=new ArrayList<Relation>();
      double totalConstraints,realPercentage;
      int iter=0;
      
      // First at all compute the complete size of the potential
      size=result.calculateSizeOfPotentials();
      
      // Compute the pecentage
      totalConstraints=(percConstraint*size)/100;

      // Generate constraints between decisions
      constrainedConfigurations=generateConstraintsForDecisions();
      realPercentage=(constrainedConfigurations*100)/size;

      // Keep on generating constraints for relations as long as
      // the limit is not reached
      while(iter < 50 && constrainedConfigurations < totalConstraints){
         constrainedConfigurations+=generateConstraintsForChanceNodes(relationsConsidered);
         realPercentage=(constrainedConfigurations*100)/size;
         // To avoid infinite loops
         iter++;
      }
   }
   
   /**
    * Private method for generating constraints between decision nodes
    */
   private double generateConstraintsForDecisions(){
      NodeList decisions=result.getNodesOfKind(Node.DECISION);
      ValuesSet antecedent,consecuent;
      LogicalNode antNode,conNode;
      LogicalExpression constraint;
      FiniteStates decision1,decision2;
      NodeList constraintVariables=new NodeList();
      Vector values;
      double constrained=0;
      int decision1State,decision2State;
      int index;
      
      // This is possible if there are at least two decision nodes
      if (decisions.size() < 2){
         return 0;
      }
      else{
         if (decisions.size() == 2){
            index=0;
         }
         else{
            // Generate the pair at random
            index=random.nextInt(decisions.size()-1);
         }
      }
      
      // Select both decisions
      decision1=(FiniteStates)decisions.elementAt(index);
      decision2=(FiniteStates)decisions.elementAt(index+1);
      constraintVariables.insertNode(decision1);
      constraintVariables.insertNode(decision2);
      
      // Select a value for decision1 and decision2
      decision1State=random.nextInt(decision1.getNumStates());
      decision2State=random.nextInt(decision2.getNumStates());
      
      // Make a ValuesSet for every decision
      values=new Vector();
      values.add(decision1.getState(decision1State));
      antecedent=new ValuesSet((Node)decision1,values,false);
      values=new Vector();
      values.add(decision2.getState(decision2State));
      consecuent=new ValuesSet((Node)decision2,values,false);
      
      // Make a LogicalNode for 
      antNode=new LogicalNode(antecedent);
      conNode=new LogicalNode(consecuent);
      
      // Make a LogicalExpression
      constraint=new LogicalExpression(antNode,conNode,LogicalNode.IMPLICATION);
      
      // Make a relation for the constraint
      Relation rel=new Relation();
      rel.setVariables(constraintVariables);
      rel.setValues(constraint);
      rel.setKind(Relation.CONSTRAINT);
      
      // Add the rel to the set of relations
      result.addRelation(rel);
      
      // Compute the number of constrained configurations
      constrained=result.computeConstrainedConfigurations(rel);
      return constrained;
   }
   
   /**
    * Method for generating constraints between decision nodes
    * @param relationsConsidered
    * @return number of constrained configurations
    */
   private double generateConstraintsForChanceNodes(ArrayList<Relation> relationsConsidered){
       FiniteStates var1,var2;
       Vector relations=result.getRelationList();
       ValuesSet antecedent,consecuent;
       LogicalNode antNode,conNode;
       LogicalExpression constraint;
       Vector values;
       NodeList varsInRel;
       NodeList constraintVariables=new NodeList();
       Relation sourceRel=null, destRel;
       int iter=0,indexRel,kindRel,indVar1,var1State,var2State;
       double constrained=0;
       boolean considered;
       boolean found=false;

       while(iter < 20){
           // Select a relation at random
           indexRel=random.nextInt(relations.size());
           sourceRel=(Relation)relations.elementAt(indexRel);
           kindRel=sourceRel.getKind();
           considered=relationsConsidered.contains(sourceRel);
           if (!considered && kindRel != Relation.CONSTRAINT && kindRel != Relation.UTILITY && sourceRel.getVariables().size() > 1){
               // The selected relation is ok
               iter=20;
               found=true;
           }
           // Add one to iter
           iter++;
       }

       // If the limit of the loop is reached, return 0
       if (iter == 20 && !found)
         return 0; 
       
       // The variable in the consecuent will be the first variable. The
       // Variable for the antecedent will be selected at random
       relationsConsidered.add(sourceRel);
       varsInRel=sourceRel.getVariables();
       indVar1=random.nextInt(varsInRel.size()-1)+1;
       var1=(FiniteStates)varsInRel.elementAt(indVar1);
       var2=(FiniteStates)varsInRel.elementAt(0);
       
       // Add the variables to constraintVariables
       constraintVariables.insertNode(var2);
       constraintVariables.insertNode(var1);
       
      // Select a value for var1 and var2
      var1State=random.nextInt(var1.getNumStates());
      var2State=random.nextInt(var2.getNumStates());
      
      // Make a ValuesSet for every decision
      values=new Vector();
      values.add(var1.getState(var1State));
      
      // Create the antecedent
      antecedent=new ValuesSet((Node)var1,values,false);
      
      values=new Vector();
      values.add(var2.getState(var2State));
      
      // Create the consecuent
      consecuent=new ValuesSet((Node)var2,values,false);
      
      // Make a LogicalNode for 
      antNode=new LogicalNode(antecedent);
      conNode=new LogicalNode(consecuent);
      
      // Make a LogicalExpression
      constraint=new LogicalExpression(antNode,conNode,LogicalNode.IMPLICATION);
      
      // Make a relation for the constraint
      Relation rel=new Relation();
      rel.setVariables(constraintVariables);
      rel.setValues(constraint);
      rel.setKind(Relation.CONSTRAINT);
      
      // Add the rel to the set of relations
      result.addRelation(rel);
      
      // Compute the number of constrained configurations
      constrained=result.computeConstrainedConfigurations(rel);
       // Return constrained
       return constrained;
   }
   
   
   /**
    * Method for getting information about the network
    */
   public void getInformation(){
      int chanceNodes;
      int decisionNodes;
      int valueNodes;
      int links;
      double sizeOfPotentials;
      
      // Get the information
      chanceNodes=result.numberOfChanceNodes();
      decisionNodes=result.numberOfDecisions();
      valueNodes=result.numberOfValueNodes();
      links=result.numberOfLinks();
      sizeOfPotentials=result.calculateSizeOfPotentials();
			
      // Print the information
      System.out.println("Chance: "+chanceNodes+"  Decision: "+decisionNodes+"  Value: "+valueNodes+"  Links: "+links+"  Size: "+sizeOfPotentials+" Constrained confs: "+constrainedConfigurations);
   }

   /**
    * Private method for saving the result of the geeration
    */
   private void saveResult(){
      // Save the ID as a file
      try {
System.out.println("Salvado en formato de elvira......");
         result.save(result.getName()+".elv");
System.out.println("Fin de salvado en formato de elvira");
System.out.println("Comienza salvado en formato hugin");
         Elvira2Hugin saver=new Elvira2Hugin(result);
         saver.save();
System.out.println("Fin de salvado en formato hugin");

      } catch (IOException e) {
         System.out.println("Problem when saving the generated ID");
         System.exit(0);
      }
   }

   /**
    * Method for setting the links between the nodes
    */
   private void setLinks(){

      // Now link the decision nodes
      linkDecisionNodes();

      // Now set the links between the chance nodes belonging
      // to a stage and the next decision
      linkChanceNodesToNextDecision();

      // There are three operations to perform: 
      // 1) make the decision node of a stage to be parent
      //    of chance nodes from posterior stages of chance
      //    nodes
      // 2) make relations between nodes of the same stage
      //    os chance nodes
      // 3) stablish relations between chance nodes of
      //    diferent stages. These tree operations will be
      //    done at random
      linkDecisionsToFutureStages();
      linkChanceNodesOfSameStage();
      linkChanceNodesOfDifferentStages();

      // Set links to value nodes
      setLinksToValueNodes();

      // At the end force a repaint on visualGrid
      //visualGrid.repaint();
   }

   /**
    * Private method for seting links between decision nodes 
    * and chance nodes from future stages
    */
    private void linkDecisionsToFutureStages(){
      Node decision;
      NodeList stage;

      // Consider the decisions related to decision nodes one by one
      for(int i=0,stageIndex=1; i < numberDecisionNodes; i++){
         // Get the stage i
         stage=stages.get(stageIndex);
         
         // Decision will the the only node
         decision=stage.elementAt(0);

         // Now work with this decision and future stages
         // of chance nodes
         linkDecisionToFutureStages(decision,stageIndex);
         
         // Add 2 to stage
         stageIndex=stageIndex+2;
      }
    }

    /**
     * Private method for making links between a given decision
     * and the chance nodes belonging to future decisions
     * @param decision
     * @param index of the stage related to decision
     */
    private void linkDecisionToFutureStages(Node decision, int index){
       NodeList stage;

       // Consider future decisions of chance nodes
       for(int i=index+1; i < stages.size(); i=i+2){
          // Get the stage
          stage=stages.get(i);

          // Now set links between decision and some nodes 
          // (perhaps 0) of stage
          linkDecisionToChanceNodes(decision,stage);
       }
    }

    /**
     * Private method for setting links between a decision node
     * and chance nodes of a stage
     * @param decision
     * @param stage
     */
    private void linkDecisionToChanceNodes(Node decision, NodeList stage){
      Node chanceNode;
      int chanceNodeIndex;
      int linksAdded=0,linksToAdd=0;

      // Get the number of chance nodes in stage
      int numberChanceNodes=stage.size();

      // Get the number of links to include: from 1 to numberChanceNodes
      if (numberChanceNodes > 0)
         linksToAdd=random.nextInt(numberChanceNodes+1)+1;

      boolean added[]=new boolean[numberChanceNodes];

      // Now select at random the node to link
      for(int i=0; i < linksToAdd; i++){
        chanceNodeIndex=random.nextInt(numberChanceNodes);

        // If this link is added, do nothing
        if (added[chanceNodeIndex] == false){ 
          chanceNode=stage.elementAt(chanceNodeIndex);

          // Add 1 to linksAdded
          linksAdded++;

          // Set the link
          setLink(decision,chanceNode,true,true);     

          // Mark the node as considered
          added[chanceNodeIndex]=true;
        }
      }
    }

    /**
     * Private method for setting links between chance nodes
     * belonging to the same stage
     */
     private void linkChanceNodesOfSameStage(){
       NodeList stage;
       int numberNodes;

       // Consider every stage of chance nodes
       for(int i=0; i < stages.size(); i=i+2){
         stage=stages.get(i);

         // Stablish links
         linkChanceNodesStage(stage);
       }
     } 

    /**
     * Private method for setting links betwen chance nodes
     * of a stage
     * @param stage
     */
     private void linkChanceNodesStage(NodeList stage){
       Node from,to;
       int numberNodes=stage.size();
       int index1=0,index2=0,added=0;
       boolean links[][]=new boolean[numberNodes][numberNodes];
       boolean result;

       // The number of possible links to add is numberNodes*numberNodes/2
       // Get a random number between 0 and the limit
       int numberOfLinks=random.nextInt(numberNodes)+1;

       for(int i=0; i < numberOfLinks; i++){
         // Now, select at random two nodes and try the link
         do{
           index1=random.nextInt(numberNodes);
           index2=random.nextInt(numberNodes);
         }while(index1 == index2 || links[index1][index2] == true);

         // Get the nodes an try the link
         from=stage.elementAt(index1);
         to=stage.elementAt(index2);
         result=setLink(from,to,true,true);
         if (result){
           added++;
           links[index1][index2]=true;
         }
       }
     }

    /*
     * Private method for setting links between chance nodes of
     * diferent stages
     * Brute force is used: consider random pairs of stages and
     * add links
     */
    private void linkChanceNodesOfDifferentStages(){
      NodeList stage;

      for(int i=0; i < stages.size(); i=i+2){
        stage=stages.get(i);

        // Set linkf of this stage respect to another stages
        linkChanceNodesStageWithAnotherStages(stage,i);
      }
    }

    /**
     * Private method for setting links between the nodes of a stage
     * and another stages (of chance nodes)
     * @param stage
     * @param index of the stage
     */
    private void linkChanceNodesStageWithAnotherStages(NodeList stage, int index){
      NodeList targetStage=null;
      int numberStagesWithChanceNodes=numberDecisionNodes+1;
      int targetStageIndex;
      boolean linkedStages[];

      // Select at random the number of stages to consider: max. will be
      // as numberStagesWithChanceNodes-1
      int numberTargetStages=random.nextInt(numberStagesWithChanceNodes);
      linkedStages=new boolean[numberStagesWithChanceNodes];

      // Loop of iteration
      for(int i=0; i < numberTargetStages; i++){
        do{
          // Select a random target stage (not index)
          targetStageIndex=random.nextInt(numberStagesWithChanceNodes);
        }while(targetStageIndex*2 != index && linkedStages[targetStageIndex] == true);

        targetStage=stages.get(targetStageIndex*2);

        // Now make links between stage a tarStageIndex
        linkChanceNodes(stage,targetStage);
        linkedStages[targetStageIndex]=true;
      }
    }

   /**
    * Private method for setting links between two stages of chance nodes
    * @param source stage
    * @param dest stage
    */
    private void linkChanceNodes(NodeList source, NodeList dest){
      Node from,to;
      int fromIndex,toIndex;
      //int numberPossibleLinks=(source.size()+dest.size())/2;
      int numberPossibleLinks=source.size();
      int targetLinks=random.nextInt(numberPossibleLinks);
      boolean links[][]=new boolean[source.size()][dest.size()];

      // Loop of links creation: select at random two nodes: one from
      // source and nother for dest and try the link
      for(int i=0; i < targetLinks; i++){
         do{
           fromIndex=random.nextInt(source.size());
           toIndex=random.nextInt(dest.size());
         }while(links[fromIndex][toIndex] == true || fromIndex == toIndex);

         from=source.elementAt(fromIndex);
         to=dest.elementAt(toIndex);
         links[fromIndex][toIndex]=true;

         // Try the link
         setLink(from,to,true,true);
      }
    }
   
   /**
    * Private method for getting decision nodes linked to each other,
    * forming a linear temporal order
    */
   private void linkDecisionNodes(){
      NodeList decisionNodes=new NodeList();
      Node decision, nextDecision;
      NodeList stage;
      
      // Get the set of decision nodes
      for(int i=0,stageIndex=1; i < numberDecisionNodes; i++){
         // Get the stage i
         stage=stages.get(stageIndex);
         
         // Decision will the the only node
         decision=stage.elementAt(0);
         
         // Add the decision to decisionNodes
         decisionNodes.insertNode(decision);
         
         // Add 2 to stage
         stageIndex=stageIndex+2;
      }
      
      // Now link nodes: decisionD_{i} parent of decision D_{i+1}
      for (int i = 0; i < decisionNodes.size() - 1; i++) {
         decision = decisionNodes.elementAt(i);
         nextDecision = (FiniteStates) decisionNodes.elementAt(i+1);
         
         // Set the link between decision and nextDecision
         setLink(decision,nextDecision,false,true); 
      }
   }

   /**
    * Method for linking the nodes in I_{i} to D_{i}
    */
   private void linkChanceNodesToNextDecision(){
      Node chanceNode,nextDecision;
      NodeList stage,nextStage;
      
      // Get the set of decision nodes
      for(int i=0; i < stages.size()-1; i=i+2){
         // Get the stage i containing chance nodes
         stage=stages.get(i);
        
         // Get the next stage containing the decision
         nextStage=stages.get(i+1);

         // Get the next decision
         nextDecision=nextStage.elementAt(0);

         // Set the links between the nodes in stage and
         // nextDecision
         for(int j=0; j < stage.size(); j++){
            chanceNode=stage.elementAt(j);

            // Set the link
            setLink(chanceNode,nextDecision,true,true);
         }
      }
   }

   /**
    * Method for setting the links to value nodes
    */
   private void setLinksToValueNodes(){
     // Get all the decisions linked to any value node. The
     // path to the value node can be direct or indirect
     linkDecisionsToValueNodes();

     // Now link another nodes to value nodes. The limit for
     // the number of parents is not applicable for value nodes
     linkChanceNodesToValueNodes();

     // Check all value nodes has at least one parent
     checkAllValueNodesHasParents();
   }

   /**
    * Private method for setting links from chance nodes to
    * value nodes. This is done at random
    */
   private void linkChanceNodesToValueNodes(){
     NodeList stage;
     boolean considered[]=new boolean[numberDecisionNodes+1];
     boolean allConsidered=false;
     int stageIndex,randomSelection;

     // Repeat until considering all the stages
     do{
       // Select a random stage not considered previously
       do{
         randomSelection=random.nextInt(numberDecisionNodes+1);
       }while(considered[randomSelection] == true);

       // Mark it as considered
       considered[randomSelection]=true;
       stageIndex=randomSelection*2;

       // Get the stage
       stage=stages.get(stageIndex);

       // Set the links of the nodes from this stage
       // to value nodes
       linkChanceNodesToValueNodes(stage);

       // At the end check if all the stages are considered
       allConsidered=true;
       for(int i=0; i < numberDecisionNodes+1; i++){
         if (considered[i] == false){
           allConsidered=false;
           break;
         }
       }
     }while(!allConsidered);
   }

   /**
    * Private method for setting the links between the nodes
    * of a stage and the value nodes
    * @param stage
    */
   private void linkChanceNodesToValueNodes(NodeList stage){
      Node from,to;
      int fromIndex,toIndex;
      //int numberPossibleLinks=stage.size()*numberValueNodes/2;
      int numberPossibleLinks=stage.size();
      int targetLinks=1+random.nextInt(numberPossibleLinks);
      boolean links[][]=new boolean[stage.size()][numberValueNodes];

      // Loop of links creation: select at random two nodes: one from
      // stage and another from the value nodes and try the link
      for(int i=0; i < targetLinks; i++){
         do{
           fromIndex=random.nextInt(stage.size());
           toIndex=random.nextInt(valueNodes.size());
         }while(links[fromIndex][toIndex] == true);

         from=stage.elementAt(fromIndex);
         to=valueNodes.get(toIndex);
         links[fromIndex][toIndex]=true;

         // Try the link
         setLink(from,to,true,true);
      }
   }

   /**
    * Private method for setting links between decisions and
    * value nodes
    */
   private void linkDecisionsToValueNodes(){
     NodeList stage;
     Node decision,to;
     int toIndex,linksAdded;
     int targetLinks;

     // Consider decision nodes one by one. Begin with the last decision
     // and goes back
     for(int i=stages.size()-2; i > 0; i=i-2){
       stage=stages.get(i);

       // Get the decision node
       decision=stage.elementAt(0);

       //Compute the number of links to add: at least every decision
       //will be related to 1 value node
       targetLinks=random.nextInt(numberValueNodes)+1;

       // Set links from this decision to value nodes
       linksAdded=0;
       for(int j=0; j < targetLinks; ){
         toIndex=random.nextInt(valueNodes.size());
         to=valueNodes.get(toIndex);
         // Check if there is a path from this decision no the selected
         // value node. For that check all the paths
         computeAllPaths();
         //if (adyacents[decision.getIndex()][toIndex] != 1){
         if (adyacents[indexes.get(decision.getName())][toIndex] != 1){
           // Set the link
           setLink(decision,to,true,true);

           // Add one to j and to linksAdded
           j++;
           linksAdded++;
         }
       }
     }
   }

   /**
    * Private method for checking all the value nodes has at least one parent
    */
   private void checkAllValueNodesHasParents(){
     NodeList stage,parents;
     Node valueNode,decisionNode,parentNode;
     int decisionIndex,stageIndex,decisionsAsParents;

     // Consider value nodes one by one
     for(int i=0; i < valueNodes.size(); i++){
       valueNode=valueNodes.get(i);

       // Check the number of parents
       parents=valueNode.getParentNodes();
       if (parents.size() == 0){

         // In this case, select one of the decisions and set a link
         // between a decision and the value node
         decisionIndex=random.nextInt(numberDecisionNodes);
         stageIndex=decisionIndex*2+1;
         stage=stages.get(stageIndex);
         decisionNode=stage.elementAt(0);
System.out.println("Agregando enlace forzado a "+valueNode.getName());
         setLink(decisionNode,valueNode,false,true);
       }

       // Now check that every value node hast at least one decision
       // as antecedent
       // First at all, counts the number of decision nodes parents of
       // every value node
       decisionsAsParents=0;
       for(int j=0; j < parents.size(); j++){
         parentNode=parents.elementAt(j);
         if (parentNode.getKindOfNode() == Node.DECISION){
           decisionsAsParents++;
         }
       }

       // If decisionAsParents is 0, add one decision as direct parent
       if(decisionsAsParents==0){
System.out.println("Agregando enlace forzado a "+valueNode.getName());
         decisionIndex=random.nextInt(numberDecisionNodes);
         stageIndex=decisionIndex*2+1;
         stage=stages.get(stageIndex);
         decisionNode=stage.elementAt(0);
         setLink(decisionNode,valueNode,false,true);
       }
     }
   }

   /**
    * Private method for setting a new link between two nodes
    * @param from
    * @param to
    * @param checkMaxReached check for maximum number of parents
    * @param checkLoops checks for loops
    * @return result
    */
   private boolean setLink(Node from, Node to, boolean checkMaxReached, boolean checkLoops){
     int numberParents;

System.out.println("   Enlace entre: "+from.getName()+" y "+to.getName());
System.out.println("   Padres previos: "+to.getParentNodes().size());
//System.out.println("   Limite: "+realNumberParents[to.getIndex()]);
System.out.println("   Limite: "+realNumberParents[indexes.get(to.getName())]);
      // First at all, check that the limit of parents for chance nodes
      // is not superated
      if (checkMaxReached){
        // Get the number of parents
        numberParents=(to.getParentNodes()).size();

        //if (numberParents >= realNumberParents[to.getIndex()]){
        if (numberParents >= realNumberParents[indexes.get(to.getName())]){
          // Avoid the method to go on
System.out.println("No agregado por maximo alcanzado........");
System.out.println();
          return false;
        }
      }

      // Check the link is not already stablished
      //if (adyacents[from.getIndex()][to.getIndex()] != 0){
      if (adyacents[indexes.get(from.getName())][indexes.get(to.getName())] != 0){
System.out.println("No agregado por existir........");
System.out.println();
        // Return false
        return false;
      }

      // Check loops if needed
      if (checkLoops){
        // Add the link temporarily
        //adyacents[from.getIndex()][to.getIndex()]=1;
        adyacents[indexes.get(from.getName())][indexes.get(to.getName())]=1;

        // Check if there are loops
        int [][] allPaths=computeAllPaths();

        boolean loops=checkLoops(allPaths);

        // If there are loops, return
        // Before, put again a 0 in adyacents
        if (loops){
          //adyacents[from.getIndex()][to.getIndex()]=0;
          adyacents[indexes.get(from.getName())][indexes.get(to.getName())]=0;
System.out.println("No agregado por ciclo........");
System.out.println();
          return false;
        }
      }

      // Stablish the link between the nodes
      try {
         result.createLink(from, to, true);
      } catch (InvalidEditException iee) {
         System.out.println("Class IDRandomGenerator, Method setLink");
         System.out.println("Problem linking decision variables");
         System.exit(0);
      }

      // Create a visual arc between them
      //visualGrid.createArc(from.getPosY(),from.getPosX(),
       //                       to.getPosY(),to.getPosX());

      // Set the position to 1 in the adyacent matrix
      //adyacents[from.getIndex()][to.getIndex()]=1;
      adyacents[indexes.get(from.getName())][indexes.get(to.getName())]=1;
System.out.println("Agregado........");
System.out.println();

      // Return true
      return true;
   }

   /**
    * Private method for setting a new link between two nodes
    * @param from
    * @param to
    */
   private void setLink(Node from, Node to){
     int numberParents;

      // Stablish the link between the nodes
      try {
         result.createLink(from, to, true);
      } catch (InvalidEditException iee) {
         System.out.println("Class IDRandomGenerator, Method setLink");
         System.out.println("Problem linking decision variables");
         System.exit(0);
      }
   }

   /**
    * Private method for computing the adyacency matrix for the diagram
    * @return matrix with all the paths
    */
   private int[][] computeAllPaths(){
     int [][] previousMatrix=copyMatrix(adyacents);
     int [][] nextMatrix=null;
     boolean change=true;

     while(change){
       // Now it is needed to multiply matrix by adyacents
       nextMatrix=multiplyAdding(adyacents,previousMatrix);

       // Check the precense of changes
       change=difference(previousMatrix,nextMatrix);

       // Keep on multiplying if needed
       if (change){
         previousMatrix=nextMatrix;
       }
     }

     // At the end make return nextMatrix
     return nextMatrix;
   }

   /**
    * Private method for multiplying two square matrixes in order to
    * obtain the transitive closure
    * @param base original matrix
    * @param power of the transitive closure
    * @return product
    */
   private int[][] multiplyAdding(int [][] base, int [][] power){
     int [][] product=new int[numberNodes][numberNodes];

     // Loop for all rows
     for(int i=0; i < numberNodes; i++){
       // For every row, consider all columns
       for(int j=0; j < numberNodes; j++){
         // Consider all the values if row i and column j
         for(int k=0; k < numberNodes; k++){
           product[i][j]+=base[i][k]*power[k][j];
         }

         // At the end, add matrix[i][j] to product[i][j]
         if (product[i][j] != 0 || base[i][j] == 1)
            product[i][j]=1;
       }
     }

     // At the end, return product
     return product;
   }

   /**
    * Private method for detecting differences between 2 matrixes
    * @param matrix1
    * @param matrix2
    * @return boolean value
    */
   private boolean difference(int [][] matrix1, int [][] matrix2){
     boolean change=false;

     for(int i=0; i < numberNodes && !change; i++){
       for(int j=0; j < numberNodes && !change; j++){
         if (matrix1[i][j] != matrix2[i][j]){
           change=true;
         }
       }
     }

     // Return change
     return change;
   }

   /**
    * Method for copying a adyacency matrix
    * @param matrix
    * @return copy
    */
   private int[][] copyMatrix(int [][] matrix){
     int result[][]=new int[numberNodes][numberNodes];

     for(int i=0; i < numberNodes; i++){
       for(int j=0; j < numberNodes; j++){
         result[i][j]=matrix[i][j];
       }
     }

     // Return result
     return result;
   }

   /**
    * Method for checking ones on the main diagonal
    * @return result true (some one on diagonal), 0 otherwise
    */
   private boolean checkLoops(int [][] matrix){
     boolean result=false;

     for(int i=0; i < numberNodes && !result; i++){
         if (matrix[i][i] != 0)
           result=true;
     }

     // return result
     return result;
   }

   /**
    * Method for printing the matrix
    * @matrix with paths
    */
   private void printMatrix(int [][] matrix){
     for(int i=0; i < numberNodes; i++){
       for(int j=0; j < numberNodes; j++){
         System.out.print(matrix[i][j]+" ");
       }
       System.out.println();
     }
   }

   /**
    * Private method for avoiding barren chance nodes
    */
   private void avoidBarrenNodes(){
     NodeList nodes=result.getNodeList();
     NodeList valueNodes=result.getValueNodes();
     NodeList childNodes;
     Node node,child,value;
     int selected;
     boolean barren,added;

     // Consider the nodes one by one
     for(int i=0; i < nodes.size(); i++){
        node=nodes.elementAt(i);
System.out.println(" Probando "+node.getName()+" como sumidero..........");

        // Consider chance nodes only
        if (node.getKindOfNode() == Node.CHANCE){
           // Set barren to true
           barren=true;

           // Check if the node has sucessors
           childNodes=node.getChildrenNodes();
System.out.println("                   Hijos : "+childNodes.size());
           for(int j=0; j < childNodes.size() && barren; j++){
             child=childNodes.elementAt(j);
             if (child.getKindOfNode() != Node.DECISION)
               barren=false;
           }

           // If barran is true, add a link between the node and a
           // value node
           if (barren == true){
             added=false;
             for(int k=0; k < valueNodes.size() && !added; k++){
               value=valueNodes.elementAt(k);
System.out.println("Intentando enlace forzado por sumidero "+node.getName()+" a "+value.getName());
               added=addPathToValueNode(node,value);
             }
           }
        }
     }
   }

   /**
    * Method for adding a path from the node to the value node
    * @param node to connect with the value node
    * @param value node
    */
   private boolean addPathToValueNode(Node node, Node valueNode){
     Vector ancestors;
     Node ancestor;
     boolean added=false;

     // Consider the chance nodes being ancestors of valueNode
System.out.println("Determinando antecesores de "+valueNode.getName());
     ancestors=result.ascendants(valueNode);
System.out.println("Antecesores"+ancestors.size());

     for(int i=0; i < ancestors.size(); i++){
       ancestor=(Node)ancestors.elementAt(i);

       // Add a link from node to parentNode. Try with the others
       // if this is not possible. Try only with chance nodes
       if (ancestor.getKindOfNode() == Node.CHANCE){
System.out.println("Probando con: "+ancestor.getName());
         added=setLink(node,ancestor,true,true);
         if (added){
System.out.println("Agregado enlace entre "+node.getName()+" y "+ancestor.getName()+" para evitar sumideros.....");
            return true;
         }
       }
     }

     // If this point is reached, return false
     return false;
   }

   /**
    * Method for divorcing the parents of the utility nodes, if
    * required
    */
   private void applyDivorcingParents(){
     // Consider the set of value nodes
     NodeList valueNodes=result.getValueNodes();
     NodeList parentNodes;
     Node node;
     int factor=2;

     // Consider every value node
     for(int i=0; i < valueNodes.size(); i++){
        node=valueNodes.elementAt(i);
System.out.println("Testeando divordio de padres para "+node.getName());

        // Get the parents of this node
        parentNodes=node.getParentNodes();
System.out.println("Número de padres: "+parentNodes.size());

        // Check the number of parents
        if (parentNodes.size() > factor*maximumNumberParents){
System.out.println("Procede a realizar el divorcio de padres.....");
           divorceParents(node,parentNodes,factor);
        }
     }
   }

   /**
    * Method for divorcing the parents of a value node
    * @param valueNode
    * @param parents
    * @param factor factor of maximumNumberParents for the divorce
    */
    private void divorceParents(Node valueNode, NodeList parents, int factor){
       ArrayList<NodeList> divorcedParents=new ArrayList<NodeList>();
       NodeList group;

       // The parents will be divided into groups of factor*maximumNumberParents
       int groups=parents.size()/(factor*maximumNumberParents);
       int rest=parents.size()%(factor*maximumNumberParents);

       // One new group will contain the rest
       if (rest != 0)
         groups++;

       // Consider every group
       for(int i=0,j=0; i < groups; i++){
          // Create the NodeList
          group=new NodeList();

          // Fill it
          for(int k=0; k < factor*maximumNumberParents && j < parents.size(); k++){
             group.insertNode(parents.elementAt(j));
             j++;
          }

          // Store the group into divorcedParents
          divorcedParents.add(group); 
       }

       // Now consider the groups, remove old links and add new ones
       for(int i=0; i < divorcedParents.size(); i++){
          group=divorcedParents.get(i);

          // Reconsider
          reconnect(valueNode,group);
       }
    }

    /**
     * Private method for reconnecting the parents in group to
     * valueNode, adding a new parent between them
     * @param valueNodes
     * @param parents
     */
    private void reconnect(Node valueNode, NodeList parents){
      Node node;
      Link link;

      // Create the new chance node
      FiniteStates chanceNode=createChanceNode();
System.out.println("    Creado nodo para divorcio de padres: "+chanceNode.getName());

      // Add 1 to globalNodesIndex
      globalNodesIndex++;

      // Add the new var to the list of variables
      try{
         result.addNode(chanceNode);
      }catch(InvalidEditException e){
        System.out.println("Method reconnect, class IDRandomGenerator");
        System.out.println("Problem when adding node");
        System.exit(0);
      }

      // Add the link between this new node and valueNode
      setLink(chanceNode,valueNode);

      // Remove the links between every parent and valueNode
      for(int i=0; i < parents.size(); i++){
         // Delete the link between parent and valueNode
         node=parents.elementAt(i);
         try{
           link=result.getLink(node,valueNode);
           result.removeLink(link);
         }catch(InvalidEditException e){
            System.out.println("Method reconnect, class IDRandomGenerator");
            System.out.println("Problem when removing link");
            System.exit(0);
         }

         // Add the link parent and chanceNode
         setLink(node,chanceNode);
      }
    }

   /**
    * Main method
    */
   public static void main(String[] args) {
      IDRandomGenerator generator;
      String name = "";
      boolean randomize=false;
      double percentage = 0, percConstraint=0.0;
      int width = 0;
      int height = 0;
      int numberDecisionNodes = 0;
      int numberValueNodes = 0;
      int maximumNumberStates = 0;
      int maximumNumberParents = 0;
      int i = 0;

      // Check the number of params emplyed is ok
      if (args.length < 18 && args.length != 2) {
         usage();
      }

      while (i < args.length) {
         // Any other way, parse the arguments
         if (args[i].equals("-w")) {
            width = Integer.parseInt(args[i + 1]);
            i = i + 2;
         } else {
            if (args[i].equals("-h")) {
               height = Integer.parseInt(args[i + 1]);
               i = i + 2;
            } else {
               if (args[i].equals("-p")) {
                  percentage = Double.parseDouble(args[i + 1]);
                  i = i + 2;
               } else {
                  if (args[i].equals("-d")) {
                     numberDecisionNodes = Integer.parseInt(args[i + 1]);
                     i = i + 2;
                  } else {
                     if (args[i].equals("-v")) {
                        numberValueNodes = Integer.parseInt(args[i + 1]);
                        i = i + 2;
                     } else {
                        if (args[i].equals("-mnp")) {
                           maximumNumberParents = Integer.parseInt(args[i + 1]);
                           i = i + 2;
                        } else {
                           if (args[i].equals("-mns")) {
                              maximumNumberStates = Integer.parseInt(args[i + 1]);
                              i = i + 2;
                           } else {
                              if (args[i].equals("-name")) {
                                 name = args[i + 1];
                                 i = i + 2;
                              } else {
                                 if (args[i].equals("-const")) {
                                    percConstraint=Double.parseDouble(args[i+1]);
                                    i=i+2;
                                 }
                                 else{
                                    if (args[i].equals("-net")){
                                       randomize=true;
                                       name=args[i+1];
                                       i=i+2;
                                    }
                                    else{
                                      System.out.println("Incorrect program call");
                                      usage();
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      // Now, make an object with all these data
      if (randomize == false){
        generator = new IDRandomGenerator(width,height,percentage,numberDecisionNodes,
                                                          numberValueNodes,maximumNumberParents,
                                                          maximumNumberStates,percConstraint);

System.out.println("Se genera el diagrama.............:");
        // And now, make the ID
        generator.generateID(name);
        generator.randomizeID(name);
System.out.println("\n\n\n RANDOMIZE \n\n");
       

System.out.println("Se chequea el diagrama..............");
        // Check if the final diagram is evaluable with variable elimination
        generator.checkResult(); 

System.out.println("Se generan los potenciales..............");
        // If evaluable, then generate the potentials
        generator.generatePotentials();
        
System.out.println("Se generan las restricciones.............");
        // Now generate the constraints
        generator.generateConstraints();
      
        // Method for getting information about the network
        generator.getInformation();

        // Save the result
        generator.saveResult();
      }
      else{
        generator=new IDRandomGenerator();
        generator.randomizeID(name);
        generator.saveResult();
      }
   }

   /**
    * Method for showing the correct way to execute the
    * main method
    */
   private static void usage() {
      System.out.println("This class must be called with the following parameters:");
      System.out.println("java IDRandomGenerator ");
      System.out.println("   -name name for the ID to generate");
      System.out.println("   -w number of cells in x axis");
      System.out.println("   -h number of cells in y axis");
      System.out.println("   -p percentage of density (100% a node per cell");
      System.out.println("   -d number of decision nodes");
      System.out.println("   -v number of value nodes");
      System.out.println("   -mnp maximum number of parent nodes for chance nodes");
      System.out.println("   -mns maximum number of states for nodes");
      System.out.println("   -const percentage of constrained configurations");
      System.out.println("--------------------------------------------------");
      System.out.println("java IDRandomGenerator ");
      System.out.println("   -net net to randomize");
      System.exit(0);
   }
}
