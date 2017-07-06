/**
 * Class <code>ReductionAndAR</code>. Implements the solution of
 * an IDWithSVNodes (Influence Diagram With Super-Value Nodes).
 * @author Manuel Luque
 * @since 30/1/2004
 */

package elvira.inference.super_value;


import elvira.potential.Potential; 
import elvira.potential.PotentialTable; 
import elvira.potential.PotentialFunction;
import elvira.NodeList;
import elvira.Relation;
import elvira.RelationList; 
import elvira.tools.Crono; 
import elvira.parser.ParseException;
import elvira.IDWithSVNodes;
import elvira.inference.elimination.ids.ArcReversal;
import elvira.*;
import elvira.potential.UtilityPotential;
import elvira.potential.SumFunction;
import java.io.IOException;
import java.util.Vector;
import elvira.inference.Propagation;

/**
 *
 * @author  Manuel Luque Gallego
 */
public class ReductionAndEvalID extends elvira.inference.Propagation {
    
    /**
 * The decision tables associated with the problem.
 */
RelationList decisionTables;

/**
 * The influence diagram over which the class operates.
 */
IDWithSVNodes diag;

/**
 * Crono, to measure the execution times
 */
Crono crono;

/**
 * An integer indicating the algorithm (VE, ArcReversal, etc.)
 */
int algorithm;

/**
 * Parameters of the propagation algorithm
 */
Vector parameters;


    /** Creates a new instance of ReductionAndAR */
    public ReductionAndEvalID(IDWithSVNodes idwsv, int iDAlgorithm, Vector params) {
        diag=idwsv;
        network=idwsv;
        algorithm=iDAlgorithm;
        parameters=params;
        
        //Reduction of the diagram to an ID
        //reducediag();
                
    }
 
/* Está por hacer: Ver qué condiciones para ser evaluable ha de tener el IDWSVNodes */
public boolean initialConditions() {
 return true;    
}

/**  
 * Evaluates the Influence Diagram With SV Nodes associated with the class
 * applying an algorithm for influence diagrams to the ID reduced from the IDWithSVNodes.  
 */  

public void propagate() {  
    
    ArcReversal eval;
    boolean evaluable;
    IDiagram id;
    IDiagram originalID;
    Propagation propReduced;
    
	//Reduction of the IDWithSVNodes to IDiagram
	switch (algorithm) {

		/* Variable Elimination */
		case 0 :

			/* Variable Elimination, Potential Trees */
		case 1 :

			/* Variable Elimination, Potential Trees and Constraints */
		case 2 :
			reducediag(true);
			break;

			/* Reduction and Arc Reversal */
		case 3 :

			/* Reduction and Arc Reversal, Potential Tress */
		case 4 :

			/* Reduction and Arc Reversal, Potential Trees and Constraints */
		case 5 :
			reducediag(false);
			break;
	}

        
    //Compile diag with 'compile' of class IDiagram (invoked by compileID in class IDWithSVNodes)
    diag.compileID(algorithm,parameters);
        
    //Set the output of the propagation on the reduced diagram as output of this propagation
    propReduced=diag.getPropagation();
    results=propReduced.getResults();
    statistics=propReduced.getStatistics();
    
}



/**  
 * Reduce the Influence Diagram With SV Nodes to an IDiagram (without SV Nodes)
 * The resulting ID can have one or more utility nodes.
 * If allowSeveralUtilities is true the resulting ID can have several utilities
 * If allowSeveralUtilities is false the resulting ID must have one utility node
 */

public void reducediag(boolean allowSeveralUtilities){
    
    Relation relationSV;
    RelationList rl;
    UtilityPotential potentialSV;
    Node nodeSV;
    PotentialTable aggregate;
    NodeList nodesPotentialSV=new NodeList();
    NodeList nlAux;
    NodeList grandChildrenSV;
    Node nodeAux;
    int i;
    
    rl=new RelationList();
    rl.setRelations(diag.getRelationList());
	if (!allowSeveralUtilities){
		relationSV=findReducibleSVRelation(rl);
	}
	else{
		relationSV=findReducibleSVRelationSeveralU(rl);
	}
    
    
    while (relationSV!=null){
    	
  		//Reduce the node of the relation
    	reduceNode(diag,relationSV.getVariables().elementAt(0));
           
        //Find a new reducible super-value relation
        if (!allowSeveralUtilities){
			relationSV=findReducibleSVRelation(rl);
        }
        else{
        	relationSV=findReducibleSVRelationSeveralU(rl);
        }
       
    }//while
    
    if (allowSeveralUtilities) removeSumNodes();   
}



public void removeSumNodes(){
	NodeList nl;
	Node nodeAux;
	UtilityPotential auxPot;
	int i;
	
	nl=diag.getNodeList();
	
	for (i=0;i<nl.size();i++){
		nodeAux=nl.elementAt(i);
		if (nodeAux.getKindOfNode()==Node.SUPER_VALUE){
			auxPot=(UtilityPotential)(diag.getRelation(nodeAux).getValues());
			if (auxPot.getFunction().getClass()==SumFunction.class){
				diag.removeNode(nodeAux);
			}
			else{
				System.out.println("Influence diagram mustn't have sv-nodes that aren't SUM after the reduction");
			}
		}
	}
	
}






/**  
 * As the potential of the argument that corresponds to 'n' in his child isn't consistent, it must be set to
 * the potential 'argument'.
 */
public static void updateArgumentChild(IDWithSVNodes id,Node n,PotentialTable argument){
        NodeList childrenSV;
        Relation relationChild;
        Node child;
        UtilityPotential potentialChild;
        boolean updated;
        int i;
        
        childrenSV=n.getChildrenNodes();
        if (childrenSV.size()==0) return;
        else if (childrenSV.size()==1){
            child=childrenSV.elementAt(0);
            relationChild=id.getRelation(child);
            potentialChild=(UtilityPotential)relationChild.getValues();
            updated=false;
            for (i=0;((i<potentialChild.getArgumentsSize())&&!updated);i++){
                if (potentialChild.getStrArgument(i).equals(n.getName())){
                    potentialChild.setArgumentAt(argument,i);
                    updated=true;
                }
            }
        }
        else{
            System.out.println("Utility and super-value nodes mustn't have more than one child");
            System.exit(0);
        }
}
    
/**  
 * As the potential of the argument that corresponds to 'n' in his child 'child'
 */
public static void updateArgumentChild(
	IDWithSVNodes id,
	Node n,
	Node child) {

	Relation relationChild;
	UtilityPotential potentialChild;
	Relation relationN;
	Potential potentialN;
	boolean updated;
	int i;

	relationChild = id.getRelation(child);
	potentialChild = (UtilityPotential) relationChild.getValues();
	relationN = id.getRelation(n);
	potentialN = relationN.getValues();
	updated = false;
	for (i = 0;((i < potentialChild.getArgumentsSize()) && !updated); i++) {
		if (potentialChild.getStrArgument(i).equals(n.getName())) {
			potentialChild.setArgumentAt(potentialN, i);
			updated = true;
		}
	}
}


/**  
 * It finds a relation of kind utility-combination that can be reduced
 * because the parents of the relation are only utility-nodes and it hasn't got
 * any super-value nodes.
 */
public Relation findReducibleSVRelation(RelationList rl){
    boolean found=false;
    Relation search=null;
    Relation rel;
    int i;
    
    for (i=0;(i<rl.size())&&!found;i++){
        rel=rl.elementAt(i);
        if (isReducibleRelation(rel)){
            search=rel;
            found=true;
        }
    }
    return search;
}


public Relation findReducibleSVRelationSeveralU(RelationList rl){
	
	boolean found=false;
		Relation search=null;
		Relation rel;
		int i;
    
		for (i=0;(i<rl.size())&&!found;i++){
			rel=rl.elementAt(i);
			if (isReducibleRelationVE(rel)){
				search=rel;
				found=true;
			}
		}
		return search;
}

/**  
 * It returns true iff 'r' is a relation of kind utility-combination
 */
public boolean isSVRelation(Relation r){
    if (r==null) return false;
    else return (r.getKind()==Relation.UTILITY_COMBINATION);
}


/**  
 * It return true iff the relation 'r' is reducible, considering the influence
 * diagram will be solved with variable elimination method, so some relations
 * of kind SUM that would reducible are not reduced. This makes the reduction
 * is optimal. A relation of kind utility-combination is reducibleVE iff the
 * relation is 'reducible', and its kind is PRODUCT or its kind is SUM but it
 * has got a descendant that is of kind PRODUCT.
 */
public boolean isReducibleRelationVE(Relation r){
	Node sv;
	NodeList desc;
	
	//Find out if the super-value relation has only parents utilities.
	if (isReducibleRelation(r)==false) return false;
	else{
		//Reducible relations of kind Product are always reducibleVE
		if (((UtilityPotential)(r.getValues())).getFunction().getClass()!=SumFunction.class)
			return true;
		else{
			//Find out if all the descendants of the sv-node are SUM
			//In this case the sv-node isn't reducibleVE
			boolean descendantsAreSum=true;
			Node nodeAux;
			
			sv=r.getVariables().elementAt(0);
			desc=diag.descendantsList(sv);
			for (int i=0;(i<desc.size())&&descendantsAreSum;i++){
				nodeAux=desc.elementAt(i);
				if (nodeAux.getKindOfNode()!=Node.SUPER_VALUE){
					System.out.println("It mustn't have children of super-value nodes that are not super-value nodes");
					descendantsAreSum=false;
				}
				else{
					UtilityPotential auxPot;
					
					auxPot=(UtilityPotential)(diag.getRelation(nodeAux).getValues());
					if (auxPot.getFunction().getClass()!=SumFunction.class){
						descendantsAreSum=false;
					}
				}
			}
			
			if (descendantsAreSum) return false;
			else return true;
		}
	} 
}
						


/**  
 * It computes the reduction of the super value structure whose root is 'node'
 * in a utility 'node' that combines all the potentials of utilities of
 * the super value structure. The value nodes that are parents of 'node'
 * are removed of 'id'.
 */

public static void reduceNode(IDWithSVNodes id,Node node){
		int typeOfNode;
		Relation relationSV;
		UtilityPotential potentialSV;
		NodeList nodesPotentialSV=new NodeList();
		int i;
		PotentialTable aggregate;
		NodeList grandChildrenSV;
		Node auxNode;
		NodeList auxNodeList;
		
		typeOfNode=node.getKindOfNode();
		
		if (typeOfNode==Node.UTILITY){
			return;
		}
		else if (typeOfNode==Node.SUPER_VALUE){
			//Relation of the node SV, the potential and the parents
			//(utility nodes, because they are reduced)
			relationSV=id.getRelation(node);
   			potentialSV=(UtilityPotential)(relationSV.getValues());
   			nodesPotentialSV.setNodes(potentialSV.getVariables());
   			
			//Reduce the parents of 'node'
			for (i=0;i<nodesPotentialSV.size();i++){
				auxNode=nodesPotentialSV.elementAt(i);
				reduceNode(id,auxNode);
				updateArgumentChild(id,auxNode,node);
				
			}
			
			//Compute the aggregate potential of the parents of the super value node.
			 aggregate=(PotentialTable)(potentialSV.getArgumentAt(0));
			 for (i=1;i<potentialSV.getArguments().size();i++){
				   aggregate=aggregate.combine((PotentialTable)(potentialSV.getArgumentAt(i)),potentialSV.getFunction());
			 }
			 
			//Compute the list of the grandchildren of the sv node
			grandChildrenSV=new NodeList();
			for (i=0;i<nodesPotentialSV.size();i++){
			 	auxNode=nodesPotentialSV.elementAt(i);
				auxNodeList=auxNode.getParentNodes();
				grandChildrenSV.merge(auxNodeList);
			}
			
			//Removal of the links between utility nodes and super-value node
			 for (i=0;i<nodesPotentialSV.size();i++){
				 auxNode=nodesPotentialSV.elementAt(i);
				 try{
					 id.removeLink(auxNode,node);
				 }catch(InvalidEditException iee){;}
			 }
			 
			//Change the kind of the node: Node.SUPER_VALUE ---> Node.UTILITY
			node.setKindOfNode(Node.UTILITY);
			
			//Redirect the parents of the utility nodes to the new utility node (old sv node)
			for (i=0;i<grandChildrenSV.size();i++){
				 auxNode=grandChildrenSV.elementAt(i);
				 try{
					 id.createLink(auxNode,node);
				 }catch(InvalidEditException iee){;}
			}
        
			 //Removing of the utility nodes that were parents of the super-value node
			 for (i=0;i<nodesPotentialSV.size();i++){
				 auxNode=nodesPotentialSV.elementAt(i);
				 id.removeNode(auxNode);
        
			 }

			 boolean sameOrder = checkIfVariablesInSameOrder(id.getRelation(node),aggregate);
			 //Set the aggregate potential table of the utility node
			 
			 //id.getRelation(node).setValues(aggregate);
			 Relation rel = id.getRelation(node);
			 NodeList varsRel = rel.getVariables().copy();
			 varsRel.removeNode(0);
			 rel.setValues(new PotentialTable(varsRel,aggregate));
        
									
		}
		else{
			System.out.print("Chance or decision nodes can't be reduced\n\n");  
			return; 
		}
}
		

private static boolean checkIfVariablesInSameOrder(Relation relation,
		PotentialTable aggregate) {
	// TODO Auto-generated method stub
	int sizeVarsRel;
	boolean areSame=true;
	NodeList varsRel = relation.getVariables();
	Vector varsPot = aggregate.getVariables();
	sizeVarsRel = varsRel.size();
	if (sizeVarsRel-1==varsPot.size()){
		for(int i=1;(i<sizeVarsRel)&&areSame;i++){
			areSame = (varsRel.elementAt(i)==varsPot.elementAt(i-1));
		}
	}
	else{
		areSame = false;
	}
	return areSame;
		
}

/**  
 * It computes the reduction of the super value structure whose root is 'node'
 * in a utility 'node'. This method is similar to method reduceNode, but it only
 * modifies the links of the utility node to reduce. The relations are not considered.
 * It is useful for qualitative evaluations.
 */

public static void reduceNodeQualitatively(IDWithSVNodes id,Node node){
		int typeOfNode;
		NodeList nodesPotentialSV;
		int i;
		NodeList grandChildrenSV;
		Node auxNode;
		NodeList auxNodeList;
		
		typeOfNode=node.getKindOfNode();
		
		if (typeOfNode==Node.UTILITY){
			return;
		}
		else if (typeOfNode==Node.SUPER_VALUE){
			//Relation of the node SV, the potential and the parents
			//(utility nodes, because they are reduced)
			nodesPotentialSV = node.getParentNodes().copy();
   			
			//Reduce the parents of 'node'
			for (i=0;i<nodesPotentialSV.size();i++){
				auxNode=nodesPotentialSV.elementAt(i);
				reduceNode(id,auxNode);
				
			}
			
			 
			//Compute the list of the grandchildren of the sv node
			grandChildrenSV=new NodeList();
			for (i=0;i<nodesPotentialSV.size();i++){
			 	auxNode=nodesPotentialSV.elementAt(i);
				auxNodeList=auxNode.getParentNodes();
				grandChildrenSV.merge(auxNodeList);
			}
			
			//Removal of the links between utility nodes and super-value node
			 for (i=0;i<nodesPotentialSV.size();i++){
				 auxNode=nodesPotentialSV.elementAt(i);
				 try{
					 id.removeLink(auxNode,node);
				 }catch(InvalidEditException iee){;}
			 }
			 
			//Change the kind of the node: Node.SUPER_VALUE ---> Node.UTILITY
			node.setKindOfNode(Node.UTILITY);
			
			//Redirect the parents of the utility nodes to the new utility node (old sv node)
			for (i=0;i<grandChildrenSV.size();i++){
				 auxNode=grandChildrenSV.elementAt(i);
				 try{
					 id.createLink(auxNode,node);
				 }catch(InvalidEditException iee){;}
			}
        
			 //Removing of the utility nodes that were parents of the super-value node
			 for (i=0;i<nodesPotentialSV.size();i++){
				 auxNode=nodesPotentialSV.elementAt(i);
				 id.removeNode(auxNode);
        
			 }

			
									
		}
		else{
			System.out.print("Chance or decision nodes can't be reduced\n\n");  
			return; 
		}
}
				


/**  
 * It return true iff the relation 'r' is reducible. A relation of kind
 * utility-combination is reducible iff the parents of the relation are
 * only utility-nodes and it hasn't got any super-value nodes.
 */
public boolean isReducibleRelation(Relation r){
    boolean isReducible=true;
    NodeList nl;
    int i;
    
    if (r==null) isReducible=false;
    else{
        if (isSVRelation(r)){
            nl=r.getVariables();
            if (nl.size()==1) return false;  //The svnode hasn't any parents, so it isn't reducible
            else{
                for(i=1;(i<nl.size())&&isReducible;i++){
                    if (nl.elementAt(i).getKindOfNode()==Node.SUPER_VALUE){
                        isReducible=false;
                    }
                }
            }
        }
        else isReducible=false;
    }
    return isReducible;
}
    
 public static void main(String args[]) throws ParseException, IOException
    {
        Node from;
        Node to;
        NodeList nodes;
        IDiagram diagram;
        IDWithSVNodes idwsv;
        int i,j;
        String pathFilesElvira;
        ReductionAndEvalID reduction;
        String name;
        
        
        //Build the ID
        //idwsv=new IDWithSVNodes(args[0]);
        idwsv=new IDWithSVNodes("D:\\Proyectos Java\\Rendimiento SV\\Diagramas para Artículo Revista\\ID_Random_New_11_9.elv");
         //Retrieve the nodes.
        nodes=idwsv.getNodeList();
        System.out.println("*** DIAGRAMA ANTES DE LA REDUCCIÓN ***");
        for(i=0;i<nodes.size();i++){
            for (j=0;j<nodes.size();j++){
                if (i!=j)
                {
                    from=nodes.elementAt(i);
                    to=nodes.elementAt(j);
                    System.out.println("De "+from.getName()+" to "+to.getName());
                    System.out.println(" Max:"+from.maximalDistanceBetweenNodes(to));
                    System.out.println(" Min:"+from.minimalDistanceBetweenNodes(to));
                }
            }
        }
        
                
        reduction=new ReductionAndEvalID(idwsv,0,null);
        reduction.reducediag(true);
        reduction.diag.save("reduced.elv");
        System.out.println("*** DIAGRAMA DESPUÉS DE LA REDUCCIÓN ***");
        NodeList nodesIDiagramSV;
        nodesIDiagramSV=reduction.diag.getNodeList();
        for(i=0;i<nodesIDiagramSV.size();i++){
            for (j=0;j<nodesIDiagramSV.size();j++){
                if (i!=j) {
                    from=nodesIDiagramSV.elementAt(i);
                    to=nodesIDiagramSV.elementAt(j);
                    System.out.println("De "+from.getName()+" to "+to.getName());
                    System.out.println(" Max:"+from.maximalDistanceBetweenNodes(to));
                    System.out.println(" Min:"+from.minimalDistanceBetweenNodes(to));
                }
            }
        }
        reduction.propagate();
        
		 
        
        

    }


}
