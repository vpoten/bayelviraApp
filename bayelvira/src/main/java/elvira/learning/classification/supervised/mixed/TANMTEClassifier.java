package elvira.learning.classification.supervised.mixed;

import elvira.Elvira;
import java.util.*;
import elvira.tools.VectorManipulator;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.potential.*;
import elvira.learning.classification.ClassifierValidator;
import elvira.learning.classification.ConfusionMatrix;
import elvira.*;
import java.util.Vector;
import elvira.learning.MTELearning;
import elvira.learning.classification.supervised.continuous.*;

/**
 * Class <code>TANMTEClassifier</code> 
 *  
 * @author Antonio Fernandez Alvarez (afalvarez@ual.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 * 
 * @since 15/01/2008
 */

public class TANMTEClassifier extends MixedClassifier {
    
    //number of intervals into which the domain of the continuous variables will be split
    int intervals;
    
    //The other variables are inherited from MixedClassifier
    
     /**
     * Constructor of a TANMTEClassifier class.
     *
     * @param dbctrain the DataBaseCases from which the classifier will be constructed.
     * @param classIndex an integer value indicating the index (column) of the class variable in the database. 
     * The first column is labeled as 0.
     * @param intervals the number of intervals into which the domain of the continuous
     * variables will be split.
     */
    public TANMTEClassifier(DataBaseCases dbtrain, int classindex, int intervals) 
    throws elvira.InvalidEditException{
    
        super(dbtrain, true, classindex);
        this.intervals=intervals; 
    }
    
    /**
     * Creates a full connected undirected graph
     *  
     * @param g is a <code>Graph</code>
     *
     */    
    private void FullConnectedUndirectedGraph(Graph g){
                
        NodeList nl = g.getNodeList();
	for(int i=0;i<nl.size();i++){
                Node Xi = nl.elementAt(i);
		for(int j=i+1;j<nl.size();j++){
			Node Xj = nl.elementAt(j);
			try {
				g.createLink(Xi, Xj, false);
			} catch (InvalidEditException e) {
				e.printStackTrace();
				System.exit(112);
			}
		}
	}
    }
    
    /*
     * Compute the conditional mutual information between two features variables
     * given the class I(Xi,Xj|Y). The variables Xi, Xj and Y can be continuous
     * or discrete or a mixture of them
     *
     */
    private Vector<Double> ConditionalMutualInformation(Graph g){

        NodeList parents;
        Node childVar;
        MTELearning learningObject;
        learningObject= new MTELearning(cases);
        LinkList ll=g.getLinkList();
        ContinuousProbabilityTree P_C, P_Xj_C, P_Xi_C, P_Xi_Xj_C, t;
        Vector<Double> vMutualInformation = new Vector();
        double value;
        
        System.out.println("\nEstimating the CMI for each link...\n");
   
        for (int i=0;i<ll.size();i++){
         
            //Variable C
            parents = new NodeList();
            parents.insertNode(classVar);
          
            //Learning p(C)
            P_C=learningObject.learnConditional(classVar,new NodeList(),cases,intervals,4);  
         
            //Learning p(Xi|C)    
            childVar=ll.elementAt(i).getHead();//Variable Xi       
            P_Xi_C=learningObject.learnConditional(childVar,parents, cases, intervals,4);
         
            //Learning p(Xj|C) 
            childVar=ll.elementAt(i).getTail();//Variable Xj
            P_Xj_C=learningObject.learnConditional(childVar,parents, cases, intervals,4);
          
            //Learning p(Xi|Xj,C) 
            childVar=ll.elementAt(i).getHead();//Variable Xi
            parents.insertNode(ll.elementAt(i).getTail()); //Variable Xj                  
            P_Xi_Xj_C=learningObject.learnConditional(childVar, parents, cases, intervals, 4);
       
            value=ContinuousProbabilityTree.estimateConditionalMutualInformation(P_C, P_Xj_C, P_Xi_Xj_C, P_Xi_C,5000);
            vMutualInformation.addElement(value);             
          
            System.out.print("\nLINK: " + ((Link)ll.getLinks().elementAt(i)).toString()); 
            System.out.println("---------------------------------------------");
            System.out.println("     I(Xi,Xj|C)="+ value);
            System.out.println("---------------------------------------------");
       }
       
       return vMutualInformation;
    }
    /**
     * 
     * Creates a directed link between the class variable and each feature variable.
     * The "net" variable is updated
     *
     */
    private void CreateDirectedLinkClassFeatures(){
                
       for (int i=0;i<nVariables;i++)
         if (i!=this.classIndex)
             try {
                 classifier.createLink(classifier.getNodeList().elementAt(classIndex), classifier.getNodeList().elementAt(i), true);
             }
             catch (Exception e) { System.out.println("Problems to create the link");}          
    }
   /**
     *
     * Creates a directed links between the features variables. Root is the 
     * first visited node (by Jens D. Nielsen)
     * @param LinkList ll
     * @param Node root
     *
     */ 
    private void directTree(Graph tree, Node root) {
		Stack<Node> pending = new Stack<Node>();
		pending.push(root);
		Vector<Link> undirectedLinks = new Vector<Link>(tree.getLinkList().getLinks());
		
		//LinkList directedLinks = new LinkList();
		Node tail;
		
		// prepare the tree to conatin directed and undirected links
		tree.setKindOfGraph(Graph.MIXED);
		// direct the links
		while(!pending.isEmpty()){
			tail = pending.pop();
			Vector<Link> directedLinks = new Vector<Link>();
			for(Link nextLink : undirectedLinks){
				Node head = null;
				if(nextLink.getHead() == tail){ head = nextLink.getTail(); }
				else if(nextLink.getTail() == tail){ head = nextLink.getHead();}
				if(head != null){ 
					pending.push(head);
					try{
						tree.removeLink(nextLink);
					} catch(InvalidEditException iee){
						iee.printStackTrace();
						System.out.println("Could not remove link!!!");
					}
					try{
						tree.createLink(tail, head, true);
					} catch(InvalidEditException iee){
						iee.printStackTrace();
						System.out.println("It seems that the tree givin to directTree method was not really a tree - that is, it contained undirected cycles!!!");
					}
					directedLinks.add(nextLink);
				}
			}
			undirectedLinks.removeAll(directedLinks);
		}	
	}
	
    /**
     * Compute the structural learning of a TAN structure stored in variable 
     * "classifier".
     */    
    public void structuralLearning() {
        
       
        System.out.println("\nSTRUCTURAL LEARNING ...\n");
        //Compute the mutual information I(Xi,Y) needed to get the best root of 
        //the tree constructed among the predicting variables.
        NaiveMTEPredictor auxNB= new NaiveMTEPredictor(cases, this.classIndex, intervals);
        System.out.println(auxNB.getListOfMututlinformation().toString());
  
        int root_index=VectorManipulator.findMaxDoubles(auxNB.getListOfMututlinformation());    
             
        NodeList nl = new NodeList(cases.getVariables().getNodes());                
        Node best_root = nl.elementAt(root_index);
        nl.removeNode(classIndex);    
         
        Graph Tree = new Graph(Graph.UNDIRECTED);
        Tree.setNodeList(nl);
        FullConnectedUndirectedGraph(Tree); 
        Vector<Double> vMutualInformation = ConditionalMutualInformation(Tree);    
        MaximumSpanningTree MaxTree=new MaximumSpanningTree(Tree,vMutualInformation);
           
        Graph FinalTree=MaxTree.getMST();
        directTree(FinalTree,best_root);
        
        classifier = new Bnet();
        classifier.setNodeList(nl);
        classifier.setLinkList(FinalTree.getLinkList());

        nl.getNodes().insertElementAt(classVar, classIndex);
        CreateDirectedLinkClassFeatures();
    }
 
    /**
     * Compute the parametric learning of a TAN structure stored in variable 
     * "classifier"
     */ 
    public void parametricLearning() {
        
        MTELearning learningObject;
        ContinuousProbabilityTree t;
        PotentialContinuousPT pot;
        Relation rel;
        Vector netRL;
        NodeList parents, relationVariables;        
        Node  childVar;
       
        learningObject= new MTELearning(cases);  
        
        netRL=new Vector();
       
        System.out.println("\n\n====> Learning TAN <=======");
       
        for (int i=0;i<nVariables;i++){
         if (i!=classIndex){
            
            childVar=classifier.getNodeList().elementAt(i);
            parents=new NodeList();
            parents=childVar.getParentNodes();
          
            System.out.println("   Learning " + childVar.getName()+" ...");
            t=learningObject.learnConditional(childVar, parents, cases, intervals, 4);
            
            relationVariables=new NodeList();
            relationVariables.insertNode(childVar);
            for (int j=0;j<parents.size();j++){
                relationVariables.insertNode(parents.elementAt(j));
            }
            pot=new PotentialContinuousPT(relationVariables,t);
            rel=new Relation();
            rel.setVariables(relationVariables);
            rel.setValues(pot); 
            netRL.addElement(rel);
        }
       }
       
       childVar=classVar;
       System.out.println("   Learning " + childVar.getName()+" ... (CLASS VARIABLE)");
       t=learningObject.learnConditional(childVar, new NodeList(), cases, intervals, 4);
       
       relationVariables=new NodeList();
       relationVariables.insertNode(classVar); 
       pot=new PotentialContinuousPT(relationVariables,t);
       rel=new Relation();
       rel.setVariables(relationVariables);    
       rel.setValues(pot); 
       netRL.addElement(rel);
       
       classifier.setRelationList(netRL);

    } 
       
    /**
     * Main for constructing an MTE TAN classifier from a data base.
     *
     * Arguments:
     * 1. the dbc train file.
     * 2. the dbc test file. "CV" for cross-validation.
     * 3. the index of the class variables (starting from 0).
     * 4. the number of intervals into which the domain of the continuous 
     *    variables will be split.
     * 5. In case of cross validation, the number of folds.
     */
    public static void main(String args[]) throws Exception {
        
        FileInputStream fTrain = new FileInputStream(args[0]);
        int classIndex = Integer.valueOf(args[2]).intValue();
        int interv = Integer.valueOf(args[3]).intValue();
            
        DataBaseCases dbcTrain = new DataBaseCases(fTrain);
        
        TANMTEClassifier classif = new TANMTEClassifier(dbcTrain,classIndex,interv);
                
        classif.structuralLearning();
        classif.parametricLearning();
        classif.saveModelToFile("TAN");
        
        if (args[1].compareTo("CV") == 0) { //k-folds cross validation
            
            int k = Integer.valueOf(args[4]).intValue();

            ClassifierValidator validator=new ClassifierValidator(classif, dbcTrain, classIndex);
            ConfusionMatrix cm=validator.kFoldCrossValidation(k);
            System.out.println(k +"-folds Cross-Validation. Accuracy="+(1.0-cm.getError())+"\n\n");
        }
        else //Specific test set
        {
            FileInputStream fTest = new FileInputStream(args[1]);
            DataBaseCases dbcTest=new DataBaseCases(fTest);
            
            double accuracy = classif.test(dbcTest,classIndex);
            System.out.println("Classifier tested. Train accuracy: " + accuracy);   
        }
    }     
}    


















