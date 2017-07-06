package elvira.inference.uids;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import elvira.Continuous;
import elvira.FiniteStates;
import elvira.IDWithSVNodes;
import elvira.IDiagram;
import elvira.InvalidEditException;
import elvira.Link;
import elvira.LinkList;
import elvira.Node;
import elvira.NodeList;
import elvira.Relation;
import elvira.UID;
import elvira.UID.AlgorithmsForUID;
import elvira.inference.Propagation;
import elvira.potential.Function;
import elvira.potential.PotentialTable;
import elvira.potential.ProductFunction;
import elvira.potential.SumFunction;
import elvira.potential.UtilityPotential;
import elvira.tools.statistics.analysis.Stat;

public class GeneratorUIDs {
	/**
	 * Genera diagramas de influencia siguiendo el email que envió Marta. Esto es, construye
	 * un simple tree, le añade y quita enlaces, ordena las decisiones, añade los nodos de utilidad
	 * y determina sus padres, genera probabilidades y utilidades, y a partir de ahí, hago
	 * lo especifico para DI con nodos SV: creo la estructura de nodos SV.
	 * @param nNodes Número de nodos (aleatorios y de decisión)
	 * @param decRation Probabilidad de que se escoja que un nodo sea decisión
	 * @param nUtils Número de nodos de utilidad (non-super)
	 * @param nParents Número máximo de padres de los nodos de azar y decisión. Además es el número exacto de padres para los nodos de utilidad
	 * @param iterations Número de iteraciones para el bucle que añade y quita enlaces del árbol simple ordenado.
	 * @param ratioRemoveParentsDecs2 
	 * @return UID generado aleatoriamente
	 */
	public static UID generateUIDVomlelova(int nNodes, double decRation,double obsRation,
			int nUtils, int nParents, int maxNumChildrenDecs,int iterations,double ratioRemoveParentsDecs ) {

		boolean withDecisions=false;
		UID uid = new UID();

		while (withDecisions==false){
			uid = initializeSimpleOrderedTreeWithDecisionsVomlelova(nNodes, decRation,obsRation);
			withDecisions = (uid.getNodesOfKind(Node.DECISION).size()>0);
		}
		
		//I'm going to perform a number of iterations adapted to the number of nodes of the UID
		//I only consider 2*nNodes iterations because when a higher value the GSDAG is linear
		iterations = 2*nNodes;
		
		addAndRemoveLinksVomlelova(uid, nParents, maxNumChildrenDecs,iterations);
		
		generateProbabilities(uid);
		
		//id.orderDecisionsGreedilyIfNotOrdered();
		
		removeSomeParentsOfDecisions(uid, ratioRemoveParentsDecs);
		
		generateUtilityNodes(uid, nUtils, nParents,true);
		
		generateUtilities(uid);

		//generateSVNodes(id, nParentsOfSV);
		//printStatisticsUIDGenerated(uid);
		
		return uid;

	}
	
	
	/**
	 * Genera diagramas de influencia siguiendo el email que envió Marta. Esto es, construye
	 * un simple tree, le añade y quita enlaces, ordena las decisiones, añade los nodos de utilidad
	 * y determina sus padres, genera probabilidades y utilidades, y a partir de ahí, hago
	 * lo especifico para DI con nodos SV: creo la estructura de nodos SV.
	 * @param nNodes Número de nodos (aleatorios y de decisión)
	 * @param numDecs Número de nodos de decisión
	 * @param numObs Número de variables aleatorias observables
	 * @param numObs Número de nodos de utilidad
	 * @param nParentsChanceAndDec Maximum number of parents for chance and decision nodes
	 * @param nParentsChanceAndDec Exact number of the utility nodes
	 * @param iterations Número de iteraciones para el bucle que añade y quita enlaces del árbol simple ordenado.
	 * @param ratioRemoveParentsDecs2 
	 * @return UID generado aleatoriamente
	 */
	public static UID generateUIDVomlelovaNumberNodesEachType(int numDecs,int numObs,int numUnobs,
			int nUtils, int nParentsChanceAndDec, int nParentsUtils, int maxNumChildrenDecs,int iterations,double ratioRemoveParentsDecs, int numStatesDecs ) {
		int nNodes;
		boolean withDecisions=false;
		UID uid = new UID();

		while (withDecisions==false){
			uid = initializeSimpleOrderedTreeWithDecisionsVomlelovaNumberNodesEachType(numDecs,numObs,numUnobs,numStatesDecs);
			withDecisions = (uid.getNodesOfKind(Node.DECISION).size()>0);
		}
		
		nNodes = numDecs + numObs + numUnobs;
		//I'm going to perform a number of iterations adapted to the number of nodes of the UID
		//I only consider 2*nNodes iterations because when a higher value the GSDAG is linear
		iterations = 2*nNodes;
		
		addAndRemoveLinksVomlelova(uid, nParentsChanceAndDec, maxNumChildrenDecs,iterations);
		
		generateProbabilities(uid);
		
		//id.orderDecisionsGreedilyIfNotOrdered();
		
		removeSomeParentsOfDecisions(uid, ratioRemoveParentsDecs);
		
		generateUtilityNodes(uid, nUtils, nParentsUtils,true);
		
		generateUtilities(uid);

		
		//generateSVNodes(id, nParentsOfSV);
		//printStatisticsUIDGenerated(uid);
		
		return uid;

	}
	
	
	
	
	/**
	 * Genera diagramas de influencia siguiendo el email que envió Marta. Esto es, construye
	 * un simple tree, le añade y quita enlaces, ordena las decisiones, añade los nodos de utilidad
	 * y determina sus padres, genera probabilidades y utilidades, y a partir de ahí, hago
	 * lo especifico para DI con nodos SV: creo la estructura de nodos SV.
	 * @param nNodes Número de nodos (aleatorios y de decisión)
	 * @param numDecs Número de nodos de decisión
	 * @param numObs Número de variables aleatorias observables
	 * @param numObs Número de nodos de utilidad
	 * @param nParentsChanceAndDec Maximum number of parents for chance and decision nodes
	 * @param nParentsChanceAndDec Exact number of the utility nodes
	 * @param iterations Número de iteraciones para el bucle que añade y quita enlaces del árbol simple ordenado.
	 * @param ratioRemoveParentsDecs2 
	 * @return UID generado aleatoriamente
	 */
	public static UID generateStructureUIDVomlelovaNumberNodesEachType(int numDecs,int numObs, int numUnobs,
			int nUtils, int nParentsChanceAndDec, int nParentsUtils, int maxNumChildrenDecs,int iterations,double ratioRemoveParentsDecs, int numStatesDecs ) {
		int nNodes;
		boolean withDecisions=false;
		UID uid = new UID();

		while (withDecisions==false){
			uid = initializeSimpleOrderedTreeWithDecisionsVomlelovaNumberNodesEachType(numDecs,numObs, numUnobs, numStatesDecs);
			withDecisions = (uid.getNodesOfKind(Node.DECISION).size()>0);
		}
		
		nNodes = numDecs + numObs + numUnobs;
		//I'm going to perform a number of iterations adapted to the number of nodes of the UID
		//I only consider 2*nNodes iterations because when a higher value the GSDAG is linear
		iterations = 2*nNodes;
		
		addAndRemoveLinksVomlelova(uid, nParentsChanceAndDec, maxNumChildrenDecs,iterations);
		
				
		//id.orderDecisionsGreedilyIfNotOrdered();
		
		removeSomeParentsOfDecisions(uid, ratioRemoveParentsDecs);
		
		generateUtilityNodes(uid, nUtils, nParentsUtils,true);
		
			
		//generateSVNodes(id, nParentsOfSV);
		//printStatisticsUIDGenerated(uid);
		
		return uid;

	}
	


	private static UID initializeSimpleOrderedTreeWithDecisionsVomlelovaNumberNodesEachType(
			int numDecs, int numObs, int numUnobs, int numStatesDecs) {
		// TODO Auto-generated method stub
	
			UID id;
			Random r = new Random();
			int kind;
			double randomNumber;
			String prefix;
			Node auxNode;
			NodeList generatedNodes = new NodeList();
			String newName;
			double randomNumberObs;
			Random rObs = new Random();
			String comment;
			Vector<String> statesOfDec;
			int numNonObs;
			int nNodes;

			id = new UID();

			statesOfDec = new Vector();
			for (int i = 0; i < numStatesDecs; i++) {
				statesOfDec.add("s" + i);
			}

			nNodes = numDecs + numObs + numUnobs;
			
			for (int i = 0; i < nNodes; i++) {
				// Generated a new node
				randomNumber = r.nextDouble();
				if (i < numDecs) {// Decision
					kind = Node.DECISION;
					prefix = "D";
					comment = "";
					//newName = prefix + i;
					//auxNode = new FiniteStates(newName, statesOfDec);
				} else if (i < (numDecs + numObs)) {// Chance
																	// observable

						kind = Node.CHANCE;
						prefix = "X";
						comment = "";

					} else {// Chance non observable
						kind = Node.CHANCE;
						prefix = "H";
						comment = "h";
					}
					newName = prefix + i;
					id.createNode(0, 0, "Helvetica", newName, kind);
					auxNode = id.getNode(newName);
					// We decide if it is observable
					auxNode.setComment(comment);
					if (auxNode.getKindOfNode()==Node.DECISION){
						((FiniteStates)auxNode).setStates(statesOfDec);
					}
				// Add the node to the diagram
				try {
					id.addNode(auxNode);
				} catch (InvalidEditException iee) {
				}
				;

				if (i > 0) {// The root node of the tree can't have any parents.
					// Add a link between from one of the previously generated nodes
					// to the new node
					try {
						id.createLink(chooseRandom(generatedNodes, r), auxNode);
					} catch (InvalidEditException iee) {
					}
					;
				}

				generatedNodes.insertNode(auxNode);

			}

			return id;
	}


	/**
	 * Genera diagramas de influencia siguiendo el email que envió Marta. Esto es, construye
	 * un simple tree, le añade y quita enlaces, ordena las decisiones, añade los nodos de utilidad
	 * y determina sus padres, genera probabilidades y utilidades, y a partir de ahí, hago
	 * lo especifico para DI con nodos SV: creo la estructura de nodos SV.
	 * @param nNodes Número de nodos (aleatorios y de decisión)
	 * @param decRation Probabilidad de que se escoja que un nodo sea decisión
	 * @param nUtils Número de nodos de utilidad (non-super)
	 * @param nParents Número máximo de padres de los nodos de azar y decisión. Además es el número exacto de padres para los nodos de utilidad
	 * @param iterations Número de iteraciones para el bucle que añade y quita enlaces del árbol simple ordenado.
	 * @param ratioRemoveParentsDecs2 
	 * @return UID generado aleatoriamente
	 *//*
	public static UID generateUIDVomlelova(int nNodes, double decRation,double obsRation,
			int nUtils, int nParents, int maxNumChildrenDecs,double ratioRemoveParentsDecs ) {
		int iterations;
		UID uid=null;
		boolean connected = false;
		
		iterations = (int)(6*Math.pow(nNodes,2.0));
		
		while (connected == false){
			uid = generateUIDVomlelova(nNodes,decRation,obsRation,nUtils,nParents,maxNumChildrenDecs,iterations,ratioRemoveParentsDecs);
			connected = (uid.numberOfConnectedComponents()==1);
		}
		return uid; 
		
	}*/
	
	
	
		
	
	public static UID generateUIDTwoParts(){
		return null;
	}
	
	
	private static void printStatisticsUIDGenerated(UID uid) {
		// TODO Auto-generated method stub
		int nonObs;
		System.out.println("nDec="+uid.getNodesOfKind(Node.DECISION).size());
		nonObs = uid.getNonObservablesArrayList().size();
		System.out.println("nObs="+(uid.getNodesOfKind(Node.CHANCE).size()-nonObs));
		System.out.println("nHid="+nonObs);
		System.out.println("nUtil="+uid.getNodesOfKind(Node.UTILITY).size());
		System.out.println("nPath="+uid.getNumberOfPaths());
	}

	static UID generateSpecialUIDThomas(int numberOfDecisions){
		String newNameDec;
		String newNameChance;
		Node hidden;
		
		UID id = new UID();
		
		//Create H
		newNameChance="H";
		id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
		hidden = id.getNode(newNameChance);
		//It's a hidden variable
		hidden.setComment("h");
		
		
		
		for (int i=0;i<numberOfDecisions;i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			
			//Create Xi
			newNameChance="X"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			Node auxChance = id.getNode(newNameChance);
		
			//Link from Di to Xi
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Xi to H
			try{
				id.createLink(auxChance,hidden);
			} catch (InvalidEditException iee) {
			};
			
			
		}
						
		String nameU="U";
		id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
		
//		Link from H to U
		try{
			id.createLink(hidden,id.getNode(nameU));
		} catch (InvalidEditException iee) {
		};
			
		generateQuantitativeInformation(id);
		
		return id;
		
		
	}
	
	/**
	 * It creates a UID according to Thomas' template. 
	 * @param numDecs Number of decisions
	 * @param numStatesDec Number of states of each decision
	 * @param numStatesChance Number of states of each chance variable
	 * @param numStatesHidden Number of states of the hidden variable
	 * @return
	 */
	static UID generateSpecialUIDThomasTemplate(int numDecs,int numStatesDec,int numStatesChance,int numStatesHidden){
		String newNameDec;
		String newNameChance;
		Node hidden;
		
		UID id = new UID();
		
		//Create H
		newNameChance="H";
		id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
		hidden = id.getNode(newNameChance);
		//It's a hidden variable
		hidden.setComment("h");
		((FiniteStates)hidden).setNumStates(numStatesHidden);
		
		
		
		for (int i=0;i<numDecs;i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			((FiniteStates)auxDec).setNumStates(numStatesDec);
			
			//Create Xi
			newNameChance="X"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			Node auxChance = id.getNode(newNameChance);
			((FiniteStates)auxChance).setNumStates(numStatesChance);
		
			//Link from Di to Xi
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Xi to H
			try{
				id.createLink(auxChance,hidden);
			} catch (InvalidEditException iee) {
			};
			
			
		}
						
		String nameU="U";
		id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
		
//		Link from H to U
		try{
			id.createLink(hidden,id.getNode(nameU));
		} catch (InvalidEditException iee) {
		};
			
		generateQuantitativeInformation(id);
		
		return id;
		
		
	}

	
	public static UID generateSpecialUIDMLuque(int numberOfDecisions){
		String newNameDec;
		String newNameChance;
		Node hidden;
		Node nodeU;
		
		UID id = new UID();
		
		//Create U
		String nameU="U";
		id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
		nodeU=id.getNode(nameU);
		
		for (int i=0;i<numberOfDecisions;i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			
			//Create Xi
			newNameChance="X"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			Node auxChance = id.getNode(newNameChance);
		
			//Link from Di to Xi
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Xi to U
			try{
				id.createLink(auxChance,nodeU);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Di to U
			try{
				id.createLink(auxDec,nodeU);
			} catch (InvalidEditException iee) {
			};
			
			
		}
		
		
		
						
		
		generateQuantitativeInformation(id);
		
		return id;
		
		
	}
	
	
	/**
	 * @param numberOfDecisions
	 * @return The structure of a UID according to template1, where the constraints about utility
	 * nodes attached to the decisions are verified.
	 */
	public static UID generateStructureTemplate1(int numberOfDecisions){
		String newNameDec;
		String newNameChance;
		Node hidden;
		Node nodeU;
		
		UID id = new UID();
		
		//Create U
		String nameU="U";
		id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
		nodeU=id.getNode(nameU);
		
		for (int i=0;i<numberOfDecisions;i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			
			//Create Xi
			newNameChance="X"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			Node auxChance = id.getNode(newNameChance);
		
			//Link from Di to Xi
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Xi to U
			try{
				id.createLink(auxChance,nodeU);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Di to U
			try{
				id.createLink(auxDec,nodeU);
			} catch (InvalidEditException iee) {
			};
			
			
		}
		
		GeneratorUIDs.attachAUtilityNodeToEveryDecisionNode(id);
		
		return id;
		
		
	}
	
	/**
	 * @param numberOfDecisions
	 * @return A UID with an anologous topology to uid1-3decs, but having any number of
	 * decisions, not only 3+3 decs.
	 */
	public static UID generateSpecialUIDFinn(int numberOfDecisions){
		String newNameDec;
		String newNameChance;
		Node hidden;
		Node nodeU;
		Node auxChance;
		Node chance0;
		Node utilN_1;
		
		UID id = new UID();
		
			
		//Create Xi
		newNameChance="C0";
		id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
		chance0 = id.getNode(newNameChance);
		
		for (int i=1;i<=numberOfDecisions;i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			
			//Create Xi
			newNameChance="C"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			auxChance = id.getNode(newNameChance);
			
			
//			Create U
			String nameU="U"+i;
			id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
			nodeU=id.getNode(nameU);
		
			//Link from Di to Ci
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to U
			try{
				id.createLink(auxChance,nodeU);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to C0
			try{
				id.createLink(auxChance,chance0);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Di to U
			try{
				id.createLink(auxDec,nodeU);
			} catch (InvalidEditException iee) {
			};
		}
		
//		Create U
		String nameU="U"+(numberOfDecisions+1);
		id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
		utilN_1=id.getNode(nameU);
		
		for (int i=numberOfDecisions+1;i<=(2*numberOfDecisions);i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			
			//Create Xi
			newNameChance="C"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			auxChance = id.getNode(newNameChance);
			
			
			//Link from Di to Ci
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to U
			try{
				id.createLink(auxChance,utilN_1);
			} catch (InvalidEditException iee) {
			};
			
//			Link from C0 to Ci
			try{
				id.createLink(chance0,auxChance);
			} catch (InvalidEditException iee) {
			};
			
		}
		
	
			
		generateQuantitativeInformation(id);
		
		return id;	
			
		}
						
		
		
		
	/**
	 * It generates the quantitative part of an UID whose qualitative part is constructed
	 */
	public static void generateQuantitativeInformation(UID uid) {

		
		
		generateProbabilities(uid);
		
		//id.orderDecisionsGreedilyIfNotOrdered();
		
		generateUtilities(uid);

		//generateSVNodes(id, nParentsOfSV);
		
		

	}
	
	
	/**
	 * It generates the quantitative part of an UID whose qualitative part is constructed
	 */
	public static void generateQuantitativeInformationDifferentMEUInBranches(UID uid) {
		boolean differentMEU=false;
		Propagation prop;
		double epsilon=1.0;
		
		
		while (differentMEU==false){
		generateProbabilities(uid);
		
		//id.orderDecisionsGreedilyIfNotOrdered();
		
		generateUtilities(uid);

		uid.compile(AlgorithmsForUID.DYNAMICUID.ordinal(),null);
		prop= uid.getPropagation();
		differentMEU = (Stat.variance(((DynamicUID)prop).getUtilDecs())>epsilon);
		//differentMEU =thereIsNoTieInBest(((DinamicUID)prop).getUtilDecs());
		}
		
		

	}
	
	
	
	public static boolean thereIsNoTieInBestOption(UID uid){
		boolean differentMEU;
		Propagation prop;
		
		uid.compile(AlgorithmsForUID.DYNAMICUID.ordinal(),null);
		prop = uid.getPropagation();
		//differentMEU = thereIsNoTieInBest(((DinamicUID)prop).getUtilDecs());
		differentMEU = thereIsNoTieInBest(((DynamicUID)prop).getUtilOpts());
		return differentMEU;
		
		
		
	}
	

	private static boolean thereIsNoTieInBest(double[] utilDecs) {
		double aux;
		int count;
		double max;
		//double epsilon = 0.000001;
		//double epsilon = 0.0001;
		double epsilon = 0.01;
		//double epsilon = 10.0;
		
		
		count = 0;
		max = Double.NEGATIVE_INFINITY;
		// TODO Auto-generated method stub
		for (int i=0;i<utilDecs.length;i++){
			aux = utilDecs[i];
			if (aux>(max+epsilon)){
				count=1;
				max=aux;
			}
			else if (Math.abs(aux-max)<epsilon){
				count++;
			}
		}
		return (count==1);
	}

	/**
	 * It removes randomly some parents of the decisions.
	 * Also, it removes all the links from non-observable variables to decisions
	 * @param uid
	 * @param ratioRemoveParentsDecs
	 */
	private static void removeSomeParentsOfDecisions(UID uid,double ratioRemoveParentsDecs) {
		// TODO Auto-generated method stub
		NodeList decisions;
		Node dec;
		LinkList links;
		Random r=new Random();
		double randomNumber;
		Link auxLink;
		Node auxParent;
		boolean removeLink = false;
		NodeList auxParents;
	
		
		decisions = uid.getNodesOfKind(Node.DECISION);
		
		for (int i=0;i<decisions.size();i++){
			dec = decisions.elementAt(i);
			auxParents = dec.getParentNodes();
			for (int j=0;j<auxParents.size();j++){
				 auxParent = auxParents.elementAt(j); 
				if (auxParent.getComment()=="h"){//Non observable variable
					removeLink=true;
				}
				else{
					randomNumber=r.nextDouble();
					removeLink=(randomNumber<ratioRemoveParentsDecs);
				}
				
				if (removeLink){
				try {
					uid.removeLink(auxParent,dec);
				} catch (InvalidEditException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			}
					
		}
		
	}


	/**
	 * @param nNodes
	 * @return
	 * @throws InvalidEditException
	 */
	private static UID initializeSimpleOrderedTreeWithDecisionsVomlelova(
			int nNodes, double decRation, double obsRation) {
		UID id;
		Random r = new Random();
		int kind;
		double randomNumber;
		String prefix;
		Node auxNode;
		NodeList generatedNodes = new NodeList();
		String newName;
		double randomNumberObs;
		Random rObs = new Random();
		String comment;
		Vector<String> statesOfDec;

		id = new UID();

		statesOfDec = new Vector();
		for (int i = 0; i < 2; i++) {
			statesOfDec.add("s" + i);
		}

		for (int i = 0; i < nNodes; i++) {
			// Generated a new node
			randomNumber = r.nextDouble();
			if (randomNumber < decRation) {// Decision
				kind = Node.DECISION;
				prefix = "D";
				comment = "";
				//newName = prefix + i;
				//auxNode = new FiniteStates(newName, statesOfDec);
			} else if (randomNumber < (decRation + obsRation)) {// Chance
																// observable

					kind = Node.CHANCE;
					prefix = "X";
					comment = "";

				} else {// Chance non observable
					kind = Node.CHANCE;
					prefix = "H";
					comment = "h";
				}
				newName = prefix + i;
				id.createNode(0, 0, "Helvetica", newName, kind);
				auxNode = id.getNode(newName);
				// We decide if it is observable
				auxNode.setComment(comment);
				if (auxNode.getKindOfNode()==Node.DECISION){
					((FiniteStates)auxNode).setStates(statesOfDec);
				}
			// Add the node to the diagram
			try {
				id.addNode(auxNode);
			} catch (InvalidEditException iee) {
			}
			;

			if (i > 0) {// The root node of the tree can't have any parents.
				// Add a link between from one of the previously generated nodes
				// to the new node
				try {
					id.createLink(chooseRandom(generatedNodes, r), auxNode);
				} catch (InvalidEditException iee) {
				}
				;
			}

			generatedNodes.insertNode(auxNode);

		}

		return id;

	}
	
	
	
	
	
	private static void addAndRemoveLinksVomlelova(UID uid,int nParentsChanceAndDec, int maxNumChildrenDecs,int iterations){
		NodeList nodes;
		int i,j;
		Link linkIJ;
		Node iNode;
		Node jNode;
		Random r=new Random();
		int nNodes;
		

	
		
		nodes=uid.getNodeList();
		nNodes=nodes.size();
		
		
		for (int k = 0;k<iterations;k++){
			i=r.nextInt(nNodes);
			j=r.nextInt(nNodes);
			if (i!=j){
				iNode=nodes.elementAt(i);
				jNode=nodes.elementAt(j);
				linkIJ=uid.getLink(iNode,jNode);
				if (linkIJ != null) {//Link between i and j exists
					try {
						uid.removeLink(iNode, jNode);
					} catch (InvalidEditException iee) {
						;
					}
					if (uid.connectedComponents().size() > 1){//It isn't connected
															 //without the link
						//Add the link again
						try { 
							uid.createLink(iNode, jNode);
						} catch (InvalidEditException iee) {
							;
						}
					}
					else{
						System.out.println("Removal arc "+iNode.getName()+"->"+jNode.getName());
					}
				}
				else{
					if ((jNode.getParentNodes().size() < nParentsChanceAndDec)&& 
						((iNode.getKindOfNode()!=Node.DECISION)||(jNode.getKindOfNode()==Node.DECISION)||(iNode.getChildren().size()<maxNumChildrenDecs))){
						// limit of in number of parents and children
						if (uid.hasCycle(iNode,jNode)==false){
						try {
							uid.createLink(iNode, jNode);
						} catch (InvalidEditException iee) {
							;
							
							
						}
						System.out.println("Adding arc "+iNode.getName()+"->"+jNode.getName());
						
						}
					}
				}
			}
		}
	}
	
	/**
	 * Generate random probabilities (uniform distribution) for all the potentials of probability.
	 * @param id Influence diagram
	 */
	private static void generateProbabilities(UID id) {
		// TODO Auto-generated method stub
		NodeList listNodes;
		Node node;
		NodeList nodesRel;
		NodeList pa;
		Relation relation;
		PotentialTable potentialTable;
		Random generator=new Random();
		int kind;
		
		listNodes=id.getNodeList();
		
		 for (int i=0 ; i< listNodes.size() ; i++) {
		    nodesRel = new NodeList();
		    node=listNodes.elementAt(i);
		    kind=node.getKindOfNode();
		    if (kind==Node.CHANCE){//We only consider the relations whose main node is CHANCE
			    node = (FiniteStates)listNodes.elementAt(i);
			    //Remove the relation of the node
			    id.removeRelation(node);
			    //Construct the new relation
			    nodesRel.insertNode(node);
			    pa = id.parents(node);
			    nodesRel.join(pa);
			    relation = new Relation();
			    relation.setVariables(nodesRel);
			    relation.setKind(Relation.CONDITIONAL_PROB);
			    //Generate a potental of probability with uniform random numbers
			    potentialTable = new PotentialTable(generator,nodesRel,1);
			    relation.setValues(potentialTable);
			    id.getRelationList().addElement(relation);
		    }
		  }
				 
	}
	
	public static void generateUtilityNodes(UID uid,int nUtils, int nParents, boolean constraintDecisions){
		
			NodeList chanceAndDecNodes;
			NodeList auxParentsU;
			boolean satisfiesCons;
			NodeList decisions;
			Node auxNode;
			NodeList auxUtils;
			Random r;
			int nextUtil;
			String nameU;
			Node auxDec;
			
			r = new Random();
			
			chanceAndDecNodes=uid.getNodesOfKind(Node.CHANCE);
			chanceAndDecNodes.join(uid.getNodesOfKind(Node.DECISION));
			
				
			//Generate utility nodes and their parents
			for (int i=0;i<nUtils;i++){
				
				
				nameU="U"+i;
				uid.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
				auxParentsU=chooseRandom(chanceAndDecNodes,nParents,r);
								
				Node auxU;
				auxU=uid.getNode(nameU);

				//Links from auxParentsU to auxU
				for (int j=0;j<auxParentsU.size();j++){
					try{
					uid.createLink(auxParentsU.elementAt(j),auxU);
					} catch (InvalidEditException iee) {
					};
				}
				
			}
			
			
			if (constraintDecisions){
			
			//See if it verifies the conditions of the decision nodes
			// TODO Auto-generated method stub
					decisions = uid.getNodesOfKind(Node.DECISION);
			
					satisfiesCons = true;
					nextUtil = nUtils;
					for (int i=0;(i<decisions.size());i++){
						auxDec = decisions.elementAt(i);
						satisfiesCons = ((uid.getNodesOfKind(Node.UTILITY).intersection(auxDec.getChildrenNodes())).size()>0);
						if (satisfiesCons==false){
							//Create the utility node
							nameU="U"+nextUtil;
							uid.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
																		
							Node auxU;
							auxU=uid.getNode(nameU);

							//Links from auxParentsU to auxU
								try{
								uid.createLink(auxDec,auxU);
								} catch (InvalidEditException iee) {
								};
							}
							nextUtil = nextUtil+1;
							
						}
				
			}
			
			
		}
	
	
	public static void attachAUtilityNodeToEveryDecisionNode(UID uid){
		NodeList decisions;
		int numExistingUtilities;
		Node auxU;
		Node auxDec;
		String nameU;
	
			decisions = uid.getNodesOfKind(Node.DECISION);
			//The utility nodes will be named Uk where k starts in numExistingUtilities
			numExistingUtilities = uid.getNodesOfKind(Node.UTILITY).size();
		
				
				for (int iDec=0;(iDec<decisions.size());iDec++){
					auxDec = decisions.elementAt(iDec);
					//Create the utility node
					nameU="U"+(numExistingUtilities+iDec);
					uid.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
									
					auxU=uid.getNode(nameU);

						//Links from auxParentsU to auxU
							try{
							uid.createLink(auxDec,auxU);
							} catch (InvalidEditException iee) {
							};
						
						
						
					}
	}
			
		

	
	/**
	 * @param id
	 */
	private static void generateUtilities(UID id) {
		
		NodeList listNodes;
		Node node;
		NodeList nodesRel;
		NodeList pa;
		Relation relation;
		PotentialTable potentialTable;
		Random generator=new Random();
		int kind;
		NodeList nodesPotential;
		
		listNodes=id.getNodeList();
		
		 for (int i=0 ; i< listNodes.size() ; i++) {
		    nodesRel = new NodeList();
		    node=listNodes.elementAt(i);
		    kind=node.getKindOfNode();
		    if (kind==Node.UTILITY){//We only consider the relations whose main node is UTILITY
			    
			    //Remove the relation of the node
			    id.removeRelation(node);
			    //Construct the new relation
			    nodesRel.insertNode(node);
			    pa = id.parents(node);
			    nodesRel.join(pa); //nodesRel= X and pa(X)
			    nodesPotential = nodesRel.copy();
			    nodesPotential.removeNode(node); //nodesPotential= pa(X)
			    relation = new Relation();
			    relation.setVariables(nodesRel);
			    relation.setKind(Relation.UTILITY);
			    //Generate a potental of probability with uniform random numbers
			    potentialTable = new PotentialTable(generator,nodesPotential,100.0);
			    relation.setValues(potentialTable);
			    id.getRelationList().addElement(relation);
		    }
		  }
		
	}


	
	
	public static NodeList chooseRandom(NodeList list, int nNodes,Random r) {
		NodeList auxList;
		
		
		int auxRandom = 0;
		boolean inserted = false;
		Node auxNode;
		int length;
		
		length=list.size();

		if (length <= nNodes) {
			//We copy the list, but the references of the nodes are kept
			auxList = new NodeList();
			for (int i=0;i<list.size();i++){
				auxList.insertNode(list.elementAt(i));
			}
			
		} else {
			auxList = new NodeList();

			for (int i = 0; i < nNodes; i++) {
				inserted = false;

				while (inserted == false){
					auxRandom = r.nextInt(length);
				auxNode = list.elementAt(auxRandom);
				if (auxList.getId(auxNode) == -1) {
					auxList.insertNode(auxNode);
					inserted = true;
				}
				}
			}
		}

		return auxList;

	}
	
	
	/**
	 * @param list List of nodes where we can take several nodes randomlu
	 * @param nNodes Number of nodes to be selected
	 * @param r Seek to generate the random numbers
	 * @return A list with 'nNodes' nodes randomly selected, which will also be removed from 'list'
	 */
	public static NodeList chooseRandomAndRemove(NodeList list, int nNodes,Random r) {
		NodeList auxList;
		
		
		int auxRandom = 0;
		boolean inserted = false;
		Node auxNode;
		int length;
		
		length=list.size();

		if (length <= nNodes) {
			//We copy the list, but the references of the nodes are kept
			auxList = new NodeList();
			for (int i=0;i<list.size();i++){
				auxList.insertNode(list.elementAt(i));
			}
			
		} else {
			auxList = new NodeList();

			for (int i = 0; i < nNodes; i++) {
				inserted = false;

				while (inserted == false){
					auxRandom = r.nextInt(list.size());
				auxNode = list.elementAt(auxRandom);
				if (auxList.getId(auxNode) == -1) {
					//We remove the node of the list of nodes
					list.removeNode(auxRandom);
					//We add the node to the list of selected nodes
					auxList.insertNode(auxNode);
					inserted = true;
					
				}
				}
			}
		}

		return auxList;

	}
	
	public static Node chooseRandom(NodeList list, Random r) {
		
		return chooseRandom(list,1,r).elementAt(0);
	}

	public static UID generateUIDVomlelovaWithNonLinearGSDAG(int auxNNodes, double decRation, double obsRation, int nUtils, int nParents, int maxNumChildrenDecs, int n, double ratioRemoveParentsDecs) {
		UID auxUID = null; 
		boolean linear=true;
		// TODO Auto-generated method stub
		while(linear){
			System.out.println("** Generating a UID");
			auxUID = generateUIDVomlelova(auxNNodes,decRation,obsRation,nUtils,nParents,maxNumChildrenDecs,n, ratioRemoveParentsDecs);
			System.out.println("** Checking if the GS-DAG is linear");
			linear = auxUID.hasLinearGSDAG();
			
		}
		return auxUID;
	}

	
	public static UID generateUIDVomlelovaWithNonLinearGSDAG(int numDecs, int numObs, int numUnobs, int nUtils, int nParentsChanceAndDec, int nParentsUtils, int maxNumChildrenDecs, int n, double ratioRemoveParentsDecs, int numStatesDecs) {
		UID auxUID = null; 
		boolean linear=true;
		// TODO Auto-generated method stub
		while(linear){
			System.out.println("** Generating a UID");
			auxUID = generateUIDVomlelovaNumberNodesEachType(numDecs,numObs,numUnobs,nUtils,nParentsChanceAndDec,nParentsUtils,maxNumChildrenDecs,n, ratioRemoveParentsDecs,numStatesDecs);
			System.out.println("** Checking if the GS-DAG is linear");
			linear = auxUID.hasLinearGSDAG();
			
		}
		return auxUID;
	}
	
	
	
	public static UID generateStructureUIDVomlelovaWithNonLinearGSDAG(int numDecs, int numObs, int numUnobs, int nUtils, int nParentsChanceAndDec, int nParentsUtils, int maxNumChildrenDecs, int n, double ratioRemoveParentsDecs, int numStatesDecs) {
		UID auxUID = null; 
		boolean linear=true;
		// TODO Auto-generated method stub
		while(linear){
			System.out.println("** Generating a UID");
			auxUID = generateStructureUIDVomlelovaNumberNodesEachType(numDecs,numObs,numUnobs,nUtils,nParentsChanceAndDec,nParentsUtils,maxNumChildrenDecs,n, ratioRemoveParentsDecs,numStatesDecs);
			System.out.println("** Checking if the GS-DAG is linear");
			linear = auxUID.hasLinearGSDAG();
			
		}
		return auxUID;
	}
	public static UID generateUIDVomlelovaWithNonLinearGSDAGAndBranchAtBeginning(int auxNNodes, double decRation, double obsRation, int nUtils, int nParents, int maxNumChildrenDecs, int n, double ratioRemoveParentsDecs, int minNumChildrenFirstBranch) {
		// TODO Auto-generated method stub
		boolean isNonLinearAndBranchBeginning=false;
		UID auxUID = null;
		
		while(isNonLinearAndBranchBeginning==false){				
				auxUID = generateUIDVomlelovaWithNonLinearGSDAG(auxNNodes,decRation,obsRation,nUtils,nParents,maxNumChildrenDecs,n, ratioRemoveParentsDecs);
				isNonLinearAndBranchBeginning = auxUID.hasNonLinearGSDAGAndBranchAtBeginning(minNumChildrenFirstBranch);
	}
		return auxUID;
		
	}

	public static UID generateUIDWithManyConstraints(
			int numDecs, int numObs, int numUnobs, int nUtils,
			int nParentsChanceAndDec, int nParentsUtils, int maxNumChildrenDecs, int iterations,
			double ratioRemoveParentsDecs, int minNumChildrenFirstBranch,
			int minNumPaths, int maxNumPaths, int numStatesDecs) throws InterruptedException {
		// TODO Auto-generated method stub
		UID auxUID = null;
		boolean satisfiesConstraints = false;
		int numPaths;

		while (satisfiesConstraints == false) {
			System.out.println("* Generating randomly a UID");
			System.gc();
			auxUID = generateUIDVomlelovaWithNonLinearGSDAG(numDecs, numObs, numUnobs, nUtils, nParentsChanceAndDec, nParentsUtils, maxNumChildrenDecs,
					iterations, ratioRemoveParentsDecs, numStatesDecs);
			System.out.println("* Checking if the generated UID verifies the constraints");
//			satisfiesConstraints = true;
/*			satisfiesConstraints = auxUID
			.hasNonLinearGSDAGAndBranchAtBeginningChildrenOneDec(minNumChildrenFirstBranch);*/
			
			numPaths = auxUID.getNumberOfPaths();

			satisfiesConstraints = auxUID
					.hasNonLinearGSDAGAndBranchAtBeginningChildrenOneDec(minNumChildrenFirstBranch)
					&& (numPaths >= minNumPaths)&&(numPaths<=maxNumPaths)&&isAProperUID(auxUID);
			
			if (Thread.interrupted()){
				throw new InterruptedException();
			}

		}
		return auxUID;

	}

	
	public static UID generateStructureUIDWithManyConstraints(
			int numDecs, int numObs, int numUnobs, int nUtils,
			int nParentsChanceAndDec, int nParentsUtils, int maxNumChildrenDecs, int iterations,
			double ratioRemoveParentsDecs, int minNumChildrenFirstBranch,
			int minNumPaths, int numStatesDecs) {
		// TODO Auto-generated method stub
		UID auxUID = null;
		boolean satisfiesConstraints = false;

		while (satisfiesConstraints == false) {
			System.out.println("* Generating randomly a UID");
			System.gc();
			auxUID = generateStructureUIDVomlelovaWithNonLinearGSDAG(numDecs, numObs, numUnobs, nUtils, nParentsChanceAndDec, nParentsUtils, maxNumChildrenDecs,
					iterations, ratioRemoveParentsDecs, numStatesDecs);
			System.out.println("* Checking if the generated UID verifies the constraints");
//			satisfiesConstraints = true;
/*			satisfiesConstraints = auxUID
			.hasNonLinearGSDAGAndBranchAtBeginningChildrenOneDec(minNumChildrenFirstBranch);*/
			

			satisfiesConstraints = auxUID
					.hasNonLinearGSDAGAndBranchAtBeginningChildrenOneDec(minNumChildrenFirstBranch)
					&& (auxUID.getNumberOfPaths() >= minNumPaths)&&(isAProperUID(auxUID));

		}
		return auxUID;

	}

	
	
	
	/**
	 * This method returns true iff the 
	 * @return
	 */
	public static boolean isAProperUID(UID uid) {
		// TODO Auto-generated method stub
		boolean isProper;
		
		isProper = true;
		isProper = hasNoBarrenUnobservableNodes(uid);
		isProper = isProper&&observableConstraintsProperUID(uid);
		isProper = isProper&&areDecisionsAttachedToUtilityNode(uid);
		//isProper = isProper&&isConnectedAfterRemovingInformationalArcs(uid);
		isProper = isProper&&isConnectedNotRemovingInformationalArcs(uid);
		
		return isProper;
	}


	/*private static boolean isConnectedAfterRemovingInformationalArcs(UID uid) {
		// TODO Auto-generated method stub
		IDiagram duplicate;
		NodeList decisions;
		Node auxDec;
		NodeList parentsDecs;
		
		duplicate = uid.copy();
		decisions = duplicate.getNodesOfKind(Node.DECISION);
		
		//Remove the informational arcs
		for (int i=0;i<decisions.size();i++){
			auxDec = decisions.elementAt(i);
			parentsDecs = auxDec.getParentNodes();
			for (int j=0;j<parentsDecs.size();j++){
				try {
					duplicate.removeLink(parentsDecs.elementAt(j),auxDec);
				} catch (InvalidEditException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return (duplicate.connectedComponents().size()==1);
		
	}
	*/
	
	private static boolean isConnectedNotRemovingInformationalArcs(UID uid) {
		// TODO Auto-generated method stub
				
		return (uid.connectedComponents().size()==1);
		
	}


	private static boolean areDecisionsAttachedToUtilityNode(UID uid) {
		// TODO Auto-generated method stub
		boolean satisfiesCons;
		NodeList decisions;
		Node auxNode;
		NodeList auxUtils;
		
		decisions = uid.getNodesOfKind(Node.DECISION);
		
		satisfiesCons = true;
		for (int i=0;(i<decisions.size())&&satisfiesCons;i++){
			auxNode = decisions.elementAt(i);
			satisfiesCons = ((uid.getNodesOfKind(Node.UTILITY).intersection(auxNode.getChildrenNodes())).size()>0);
			
		}
		return satisfiesCons;
	}


	/**
	 * @param uid 
	 * @return true iff observable variables does not have any children or either have a utility node attached or have a chance node as a parent
	 */
	private static boolean observableConstraintsProperUID(UID uid) {
		// TODO Auto-generated method stub
		Node auxNode;
		NodeList chances;
		boolean satisfiesCons;
		
		chances = uid.getNodesOfKind(Node.CHANCE);
		
		satisfiesCons = true;
		for (int i=0;(i<chances.size())&&satisfiesCons;i++){
			auxNode = chances.elementAt(i);
			//An observable node should have some children or have as parent some chance node
			satisfiesCons = (!(uid.isObservable(auxNode))||(auxNode.getChildren().size()>0)
			||(uid.getNodesOfKind(Node.CHANCE).intersection(auxNode.getParentNodes())).size()>0);
			
		}
		return satisfiesCons;
	}


	private static boolean hasNoBarrenUnobservableNodes(UID uid) {
		// TODO Auto-generated method stub
		return (uid.getBarrenUnobservableNode()==null);
	}


	public static UID tryNotTieInBestOption(UID uid, int attempts) {
		// TODO Auto-generated method stub
		UID auxUID=null;
		boolean tie=true;
		int requiredAttempts;
		
		for (int i=0;(i<attempts)&&tie;i++){
			auxUID = uid.copy();
			GeneratorUIDs.generateQuantitativeInformation(auxUID);
			tie = (GeneratorUIDs.thereIsNoTieInBestOption(auxUID)==false);
			if (tie==false){
				requiredAttempts = i + 1;
			}
		}
		return (tie?null:auxUID);
	}


	
	/**
	 * @param numberOfDecisions
	 * @return The structure of a UID according to template1, where the constraints about utility
	 * nodes attached to the decisions are verified.
	 */
	public static UID generateStructureTemplate2(int numDecsPart1,int numDecsPart2){
		String newNameDec;
		String newNameChance;
		Node hidden;
		Node nodeU;
		Node auxChance;
		Node chance0;
		Node util0;
		String nameU;
		
		UID id = new UID();
		
			
		//Create Xi
		newNameChance="C0";
		id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
		chance0 = id.getNode(newNameChance);
		
		for (int i=1;i<=numDecsPart1;i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			
			//Create Xi
			newNameChance="C"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			auxChance = id.getNode(newNameChance);
			
			
//			Create Ui
			nameU="U"+i;
			id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
			nodeU=id.getNode(nameU);
		
			//Link from Di to Ci
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to Ui
			try{
				id.createLink(auxChance,nodeU);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to C0
			try{
				id.createLink(auxChance,chance0);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Di to Ui
			try{
				id.createLink(auxDec,nodeU);
			} catch (InvalidEditException iee) {
			};
		}
		
//		Create U
		nameU="U0";
		id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
		util0=id.getNode(nameU);
		
		for (int i=numDecsPart1+1;i<=(numDecsPart1+numDecsPart2);i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			
			//Create Xi
			newNameChance="C"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			auxChance = id.getNode(newNameChance);
			
//			Create Ui
			nameU="U"+i;
			id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
			nodeU=id.getNode(nameU);
					
			//Link from Di to Ci
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to U0
			try{
				id.createLink(auxChance,util0);
			} catch (InvalidEditException iee) {
			};
			
//			Link from C0 to Ci
			try{
				id.createLink(chance0,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Di to Ui
			try{
				id.createLink(auxDec,nodeU);
			} catch (InvalidEditException iee) {
			};

			
		}
		
		
		return id;	
		
		
	}
	
	
	
	/**
	 * @param numberOfDecisions
	 * @return The structure of a UID according to template1, where the constraints about utility
	 * nodes attached to the decisions are verified.
	 */
	public static UID generateStructureTemplate3(int numDecsPart1,int numDecsPart2){
		String newNameDec;
		String newNameChance;
		Node hidden;
		Node nodeU;
		Node auxChance;
		Node chance0;
		Node util0;
		Node utilFinal;
		String nameU;
		
		UID id = new UID();
		
			
		//Create Xi
		newNameChance="H0";
		id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
		chance0 = id.getNode(newNameChance);
		//H0 is an unobserved variable
		chance0.setComment("h");
		
		for (int i=1;i<=numDecsPart1;i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			
			//Create Xi
			newNameChance="C"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			auxChance = id.getNode(newNameChance);
			
			
//			Create Ui
			nameU="U"+i;
			id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
			nodeU=id.getNode(nameU);
		
			//Link from Di to Ci
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to Ui
			try{
				id.createLink(auxChance,nodeU);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to C0
			try{
				id.createLink(auxChance,chance0);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Di to Ui
			try{
				id.createLink(auxDec,nodeU);
			} catch (InvalidEditException iee) {
			};
		}
		
//		Create U0
		nameU="U0";
		id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
		util0=id.getNode(nameU);
		
		//Create U(numDecsPart1+numDecsPart2+1)
		
		nameU="U"+(numDecsPart1+numDecsPart2+1);
		id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
		utilFinal=id.getNode(nameU);
		
		//Create decision T
		newNameDec="T";
		id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
		Node decT = id.getNode(newNameDec);
		
		for (int i=numDecsPart1+1;i<=(numDecsPart1+numDecsPart2);i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			
			//Create Xi
			newNameChance="C"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			auxChance = id.getNode(newNameChance);
			
//			Create Ui
			nameU="U"+i;
			id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
			nodeU=id.getNode(nameU);
					
			//Link from Di to Ci
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to U0
			try{
				id.createLink(auxChance,util0);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to T
			try{
				id.createLink(auxChance,decT);
			} catch (InvalidEditException iee) {
			};

			
//			Link from C0 to Ci
			try{
				id.createLink(chance0,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Di to U0
			try{
				id.createLink(auxDec,nodeU);
			} catch (InvalidEditException iee) {
			};
			
			

			
		}
		
		
//		Link from C0 to UFinal
		try{
			id.createLink(chance0,utilFinal);
		} catch (InvalidEditException iee) {
		};

		
//		Link from T to UFinal
		try{
			id.createLink(decT,utilFinal);
		} catch (InvalidEditException iee) {
		};

		
		
		return id;	
		
		
	}
	
	
	
	public static UID generateStructureTemplate4(int numDecs){
		String newNameDec;
		String newNameChance;
		Node hidden;
		
		UID id = new UID();
		
		//Create H
		newNameChance="H";
		id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
		hidden = id.getNode(newNameChance);
		//It's a hidden variable
		hidden.setComment("h");
		
		
		
		for (int i=0;i<numDecs;i++){
			//Create Di
			newNameDec="D"+i;
			id.createNode(0,0,"Helvetica",newNameDec,Node.DECISION);
			Node auxDec = id.getNode(newNameDec);
			
			//Create Ci
			newNameChance="C"+i;
			id.createNode(0,0,"Helvetica",newNameChance,Node.CHANCE);
			Node auxChance = id.getNode(newNameChance);
		
			//Link from Di to Ci
			try{
				id.createLink(auxDec,auxChance);
			} catch (InvalidEditException iee) {
			};
			
//			Link from Ci to H
			try{
				id.createLink(auxChance,hidden);
			} catch (InvalidEditException iee) {
			};
			
			
		}
						
		String nameU="U";
		id.createNode(0,0,"Helvetica",nameU,Node.UTILITY);
		
//		Link from H to U
		try{
			id.createLink(hidden,id.getNode(nameU));
		} catch (InvalidEditException iee) {
		};
		
		GeneratorUIDs.attachAUtilityNodeToEveryDecisionNode(id);
			
	
		return id;
		
		
	}

	
	

	




}
