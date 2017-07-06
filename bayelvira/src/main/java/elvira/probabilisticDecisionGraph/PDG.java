package elvira.probabilisticDecisionGraph;


import java.io.*;
import java.util.*;
//import elvira.Graph;
import elvira.Bnet;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.Relation;
import elvira.RelationList;
import elvira.Node;
import elvira.NodeList;
//import elvira.inference.clustering.JoinTree;
import elvira.inference.clustering.Triangulation;
//import elvira.parser.ParseException;
import elvira.*;
import elvira.database.*;
import elvira.probabilisticDecisionGraph.tools.*;
import elvira.probabilisticDecisionGraph.*;
import elvira.potential.*;


/**
 * This class implements the Probabilistic Decision Graph model.	
 * 
 * @author dalgaard
 * @since 04/10/07
 */
public class PDG implements PGM {

	/**
	 * The variable forest is represented as a Vector of root nodes.
	 */
	protected Vector<PDGVariableNode> variableForest;
	protected String name = "default";
	protected boolean isUpdated = false;
	
	/**
	 * Constructs an empty PDG model 
	 */
	public PDG(){
		variableForest = new Vector<PDGVariableNode>();
	}
	
	public PDG(Vector<PDGVariableNode> forest){
		variableForest = forest;
	}
	
	public PDG(Vector<PDGVariableNode> f, String modelName){
		variableForest = f;
		name = modelName;
	}
        
        /**
         * Constructs a PDG from a FAN classifier. The resulting PDG
         * has the same number of parameters as the input  FAN. 
         * 
         */
        
        public PDG(Bnet fan, FiniteStates originalClassVar) throws PDGParameterNode.StateNumberException {
        	FiniteStates classVar = (FiniteStates)fan.getNode(originalClassVar.getName());
            PDGVariableNode v;
            PDGParameterNode newParam;
            PDGParameterNode classParam; 
            Relation r;
            Potential pot;
            double[] vals;
            //Configuration conf, parentsConf;
            Vector<Vector<Node>> listOfTrees;
            HashMap<FiniteStates, PDGParameterNode[]> setOfParameterNodes = new HashMap<FiniteStates, PDGParameterNode[]>();
            HashMap<FiniteStates, PDGVariableNode> variableNodes = new HashMap<FiniteStates, PDGVariableNode>();
            
            
            // First we create a variable node for the class, with a parameter
            // node corresponding to the prior of the class in the original FAN.
            v = new PDGVariableNode(classVar);
            variableNodes.put(classVar, v);
            r = fan.getRelation(classVar);

            pot = r.getValues();
            vals = new double[classVar.getNumStates()];
            Configuration conf = new Configuration();
            conf.insert(classVar,0);
            
            for (int i=0 ; i<classVar.getNumStates() ; i++) {
                vals[i] = pot.getValue(conf);
                conf.nextConfiguration();
            }
            
            classParam = new PDGParameterNode(v);
            classParam.setValues(vals);
            PDGParameterNode[] arrayOfPNodes = { classParam };
            setOfParameterNodes.put(classVar, arrayOfPNodes);
            
            
            // Now we create the parameter nodes corresponding to the
            // remaining variables. We will use a HashMap, where for
            // each variable, there will be a vector indexed by the
            // class and the other parent.
            Vector<Node> varsInFan = fan.getNodeList().copy().getNodes();
            varsInFan.removeElement(classVar);
            for (Node n : varsInFan){
            	FiniteStates currentVar = (FiniteStates)n;
                PDGVariableNode currentPDGVarNode = new PDGVariableNode(currentVar);
                variableNodes.put(currentVar,currentPDGVarNode);
                       
                // Get the conditional distribution for nextVar
                r = fan.getRelation(currentVar);
                pot = r.getValues();
                
                // Construct a configuration of the parents
                Configuration parentsConf = new Configuration(pot.getVariables());
                parentsConf.remove(currentVar);
                
                // We make sure that the class variable is the LAST
                // in the configuration.
                parentsConf.remove(classVar); 
                parentsConf.insert(classVar, 0);

                arrayOfPNodes = new PDGParameterNode[parentsConf.possibleValues()];
                
                for (int par=0 ; par<parentsConf.possibleValues() ; par++) {
                    
                    conf = parentsConf.duplicate();
                    conf.insert(currentVar,0);
                    vals = new double[currentVar.getNumStates()];
                    //int pos = conf.indexOf(nextVar);
                    
                    for (int j=0 ; j<currentVar.getNumStates() ; j++) {
                        vals[j] = pot.getValue(conf);
                        conf.nextConfiguration(parentsConf);
                    }
                    
                    newParam = new PDGParameterNode(currentPDGVarNode);
                    newParam.setValues(vals);
                    
                    
                    //generatedNodes.setElementAt(newParam,par);
                    arrayOfPNodes[par] = newParam;
                    parentsConf.nextConfiguration();
                }
                // Insert the generated parameter nodes in the HashMap
                System.out.println("We represent '"+currentVar.getName()+"' by "+currentPDGVarNode.getParameterNodesCopy().size()+" parameter-nodes");
                setOfParameterNodes.put(currentVar,arrayOfPNodes);           
            }
            
            
            // Add the variable nodes in the variable tree
            for (Node n : varsInFan){//int i=0 ; i<varsInFan.size() ; i++) {
                FiniteStates tempX = (FiniteStates)n;//varsInFan.elementAt(i);
                Vector<Node> parentsOfTempX = fan.getLinkList().getParentsInList(tempX);
                
                PDGVariableNode vnX = variableNodes.get(tempX);
                
                for (Node parentOfTempX : parentsOfTempX) {
                	//FiniteStates tpar = (FiniteStates)tpl.elementAt(j);
                    if (parentOfTempX != classVar) {
                        PDGVariableNode tpvn = variableNodes.get(parentOfTempX);
                        if(tpvn == null){
                        	System.out.println("could not find '"+parentOfTempX.getName()+"' in variablesNodes!");
                        }
                        tpvn.addSuccessor(vnX);
                        System.out.println("setting "+vnX.getName()+" as successor of "+tpvn.getName());
                    }
                }
            }
            
      

            // Get the connected componets of the fan without the class,
            // That is, the set of trees beneath the class.
            
            listOfTrees = fan.getConnectedComponentsWithoutNode(classVar);
            FiniteStates y = null;
            for(Vector<Node> tree : listOfTrees){ 
                            
                //find the root of the current connected component
                for (Node n : tree) {
                	Vector<Node> parents = fan.getLinkList().getParentsInList(n);
                	if(parents.size() == 1){
                		y = (FiniteStates)n;
                		// Now, 'y' is the root of the current sub-tree
                		break;
                	}
                }
                
                // We add the root as a child of the class.
                PDGVariableNode yvn = variableNodes.get(y);
                v.addSuccessor(yvn);
                System.out.println("adding "+yvn.getName()+" as successor of "+v.getName());
                
                //depthFirst = fan.directedDepthFirst(y,new Vector());
                Stack<PDGVariableNode> pending = new Stack<PDGVariableNode>();
                for(PDGVariableNode p : v.getSuccessors())
                	pending.push(p);
                while (!pending.isEmpty()) {
                	PDGVariableNode current = pending.pop();
                	
                	for(PDGVariableNode p : current.getSuccessors()){
                		pending.push(p);
                	}
                    
                    // Now we get the list of parameter nodes for current

                    PDGParameterNode[] currentPNodes = setOfParameterNodes.get(current.getFiniteStates());
                    r = fan.getRelation(current.getFiniteStates());
                    pot = r.getValues();
                    
                    // create a configuration over the parents
                    conf = new Configuration(pot.getVariables());
                    conf.remove(current.getFiniteStates());
                    
                    // Again, we make sure that the class variable is the last one.
                    conf.remove(classVar);
                	conf.insert(classVar,0);                    

                                     
                    for (int par=0 ; par<conf.possibleValues() ; par++) {
                       
                        PDGParameterNode tempPar = currentPNodes[par];
                        
                        int valueForC = conf.getValue(classVar);
                        // Now we connect this parameter node appropriately.
                        
                        if (conf.size() == 1) { // only the class is a parent
                            classParam.setSuccessor(tempPar,current,valueForC);
                        }
                        else {
                            FiniteStates parent = conf.getVariable(0);
                            PDGVariableNode parentVN = variableNodes.get(parent);
                            int parentValue = conf.getValue(parent);
                            
                            // Now we need the parameter nodes of the parent
                            PDGParameterNode[] parentParams = setOfParameterNodes.get(parent);
                            if(parentParams == null){
                            	System.out.println("could not find parameternodes for "+parent.getName());
                            }
                            Vector variablesInPotentialOfParent = fan.getRelation(parent).getValues().getVariables();
                            Configuration parentConf = new Configuration(variablesInPotentialOfParent);
                            parentConf.remove(parent);
                            parentConf.remove(classVar); parentConf.insert(classVar, 0);
                            //FiniteStates parentOfFeatureParent = current.predecessor().predecessor().getFiniteStates();
                            //featureParentConf.insert(parentOfFeatureParent, 0);
                            //if(parentOfFeatureParent != classVar) 
                            //	featureParentConf.insert(classVar, 0);
                            
                            for (int op=0 ; op<parentConf.saferPossibleValues() ; op++) {
                                if (valueForC == parentConf.getValue(classVar)) {
                                    PDGParameterNode tp = parentParams[op];
                                    tp.setSuccessor(tempPar,current,parentValue);
                                }
                                parentConf.nextConfiguration();
                            }
                        }
                        conf.nextConfiguration();
                    }
                }
                
            }

            
            // Create the variable forest
            
            variableForest = new Vector<PDGVariableNode>();
            this.addTree(v);
            
        }
        
        
        
	public Vector<PDGVariableNode> getVariableForestCopy(){
		return new Vector<PDGVariableNode>(variableForest);
	}
	
	/**
	 * 
	 * Constructs a PDG model from the given bn model by the
	 * method described by Manfred Jaeger "Probabilistic Decision Graphs -- 
	 * Combining Verification and AI Techniques for Probabilistic Inference" in 
	 * International Journal of Uncertainty, Fuzziness and Knowledge-Based Systems, 
	 * 12:19-42, 2004. In short, we first constructs a Clique Tree model from the BN 
	 * model and then constructs an equivalent PDG model from the Clique Tree that is
	 * bounded in size.
	 * 
	 * 
	 * @param bn - The bn model from which to construct the PDG model.
	 */
	
	/*public PDG(Bnet bn){
		/**
		 * TODO: implement this
		 */
		/*
		//JoinTree jt = new JoinTree(bn);
		Triangulation t = new Triangulation(bn);
		RelationList rl = t.getCliques();
		Enumeration rEnum = rl.elements();
		int i = 1;
		while(rEnum.hasMoreElements()){
			Relation r = (Relation)rEnum.nextElement();
			System.out.println("Relation  :"+ i++);
			System.out.println("Name      : "+r.getName());
			NodeList nl = r.getVariables();
			System.out.println("Variables :"+printVars(nl));
			NodeList pars = r.getParents();
			System.out.println("Parents   :" + printVars(pars));
			System.out.println("================");
		}
		System.out.println("Size       : "+rl.size());
		System.out.println("Sum Size   : "+rl.sumSizes());
		System.out.println("Total Size : "+rl.totalSize());
	}
*/	
        
        
        
        
	public void addTree(PDGVariableNode tree){
		variableForest.add(tree);
	}
	
	protected Stack<PDGVariableNode> getDepthFirstStack(){
		Stack<PDGVariableNode> stack = new Stack<PDGVariableNode>();
		for(PDGVariableNode p : variableForest){
			p.getDepthFirstStack(stack);
		}
		return stack;
	}
	
	public PDG copy(){
		Vector<PDGVariableNode> forestCopy = new Vector<PDGVariableNode>();
		for(PDGVariableNode p : variableForest)
			forestCopy.add(p.copy(null));
		PDG cp = new PDG(forestCopy, new String(name));
		return cp;
	}
	
	public boolean checkReach(int max){
		boolean res = true;
		for(PDGVariableNode p : variableForest){
			res &= p.checkReach(max);
		}
		return res;
	}
	
	/**
	 * 
	 * Returns a Vector of PDGVariableNodes containing all those PDGVariableNodes
	 * that are less than or exactly depth levels from the root, ie. 
	 * getVariableNodesAboveDepth(0) would return a Vector of all root PDGVariableNodes.
	 * 
	 * @param depth
	 * @return
	 */
	protected Vector<PDGVariableNode> getVariableNodesAboveDepth(int depth){
		Vector<PDGVariableNode> vect = new Vector<PDGVariableNode>();
		for(PDGVariableNode v : variableForest)
			v.getVarNodesAboveDepth(vect, depth);
		return vect;
	}
	
	void computeIflOfl(){
		try{
			for(PDGVariableNode t : variableForest){
				t.computeOutFlow();
			}
			for(PDGVariableNode t : variableForest){
				double oflProd = 1.0;
				for(PDGVariableNode tt : variableForest){
					if(t.equals(tt)) continue;
					oflProd *= tt.getParameterRootNode().inFlow;
				}
					
				t.propagateIfl(oflProd);
			}
		} catch(PDGException pdge){
			pdge.printStackTrace();
			System.exit(112);
		}
		isUpdated = true;
	}

	public void updateBeliefs(){
		computeIflOfl();
	}
	
	/**
	 * This method updates the all PDGParameterNodes such that their reference to
	 * data cases that reach them is set correct. That is, all references to data 
	 * cases are cleared, and data is sent through the structure from the root down.
	 * 
	 * This method can be used in different situations:
	 * 
	 * <ol>
	 * <li> during construction of the PDG structure (e.g. in learning), this method should 
	 * be used after performing local structural changes that may have 
	 * changed the path for some data instances through the structure - 
	 * which may be the case for the merge and redirect operations. 
	 * 
	 * <li> in the process of learning parameters from a new dataset.
	 * </ol>
	 * 
	 * To finally get the ML parameters for all parameters you should
	 * run {@link learnParametersFromReach} afterwards.
	 * 
	 * @param data - the data
	 */
	public void updateReach(CaseListMem data) {
		for(PDGVariableNode p : variableForest){
			try{
				p.reComputeReach(data);
			} catch(PDGException pde){
				System.out.println("PDGException in a call to reComputeReach on a 'root'-node in the variable forest.\n" +
						"This can only mean one thing:\n" +
						"Something is wrong with the variable forest of this PDG - at least one of the roots have a non-null predecessor!\n" +
						"\nI do not know how to continue - you should fire up the debugger.");
				System.exit(112);
			}
		}
	}
	
	public double[] getBelief(Node target) throws PDGVariableNotFoundException{
		PDGVariableNode targetNode = getPDGVariableNode(target);
		double[] retval = null;
		if(!isUpdated){
			updateBeliefs();
		}
		retval = targetNode.getMarginal();
		return retval;
	}
	
/*	
 * private double[] getBelief(PDGVariableNode target){
		double[] retval = null;
		if(!isUpdated){
			updateBeliefs();
		}
		retval = target.getMarginal();
		return retval;
	}
	*/
	
	public PDGVariableNode getVariableNodeByName(String name){
		PDGVariableNode retval = null;
		for(PDGVariableNode p : variableForest){
			retval = p.findNodeByName(name);
			if(retval != null) break;
		}
		return retval;
	}
	
	public PDGVariableNode getPDGVariableNode(Node n){
		PDGVariableNode retval = null;
		for(PDGVariableNode t : variableForest){
			retval = t.findPDGVariableNodeByElviraNode(n);
			if(retval != null) break;
		}
		return retval;
	}
	
	public void printNames(){
		for(PDGVariableNode p : variableForest){
			p.printNames();
		}
	}
	
	//public void insertFiniteStateEvidence(String varName, int state) throws PDGException {
	//	PDGVariableNode p = getVariableNodeByName(varName);
	//	if(p != null){ p.insertEvidence(state); }
	//	else{
	//		throw new PDGVariableNotFoundException("PDGVariableNode for '"+varName+"' not found!");
	//	} 
	//	isUpdated = false;
	//}
	
	public boolean insertEvidence(Configuration conf) throws PDGIncompatibleEvidenceException {
		boolean allVarsFound = true;
		this.isUpdated = false;
		for(PDGVariableNode v : variableForest){
			allVarsFound &= v.insertMultipleEvidence(conf);
		}
		return allVarsFound;
	}
	
	public void removeEvidence(){
		for(PDGVariableNode p : variableForest){
			p.removeEvidence();
		}
		isUpdated = false;
	}
	
	public double probabilityOfEvidence(){
		double prob = 1.0;
		if(!isUpdated){ 
			this.computeIflOfl(); 
		}
		for(PDGVariableNode p : variableForest)
			prob *= p.getParameterRootNode().outFlow;
		return prob;
	}
	
	private String printVars(NodeList nl){
		Vector v = nl.getNodes();
		String retval = "";
		for(int i=0;i<v.size();i++){
			Node n = (Node)v.elementAt(i);
			retval += n.getTitle()+"["+n.getName()+"] ";
		}
		return retval;
	}

	public void setName(String n){
		name = new String(n);
	}
	
	public String getName(){
		return name;
	}
	
	public String toString(){
		String retval = null;
		StringBuilder vars = new StringBuilder();
		StringBuilder struct = new StringBuilder();
		for(PDGVariableNode pvn : variableForest){
			pvn.toString(vars, struct);
		}
		retval = "pdg \""+name+"\"{\n"+vars.toString()+
		"\tstructure{\n"+struct.toString()+"\t}\n}\n";
		return retval;
	}
	
	void printMarginals(){
		computeIflOfl();
		for(PDGVariableNode pdgn : variableForest){
			printMarginal(pdgn);
		}
	}
	
	private void printMarginal(PDGVariableNode y){
		System.out.println(y.getName() + ":" + VectorOps.doubleArrayToString(y.getMarginal()));
		for(PDGVariableNode succ : y.getSuccessors()){
			printMarginal(succ);
		}
	}
	
	public NodeList getNodeList(){
		NodeList nl = new NodeList();
		for(PDGVariableNode p : variableForest){
			p.compileNodeList(nl);
		}
		return nl;
	}
	
	public Vector<Node> getNodes(){
		Vector<Node> vect = new Vector<Node>();
		for(PDGVariableNode p : variableForest){
			p.getNodes(vect);
		}
		return vect;
	}
	
	public void setMultipleEvidence(elvira.Configuration conf) throws PDGException{
		for(PDGVariableNode p : variableForest)
			p.insertMultipleEvidence(conf);
	}
	
	public static void main(String argv[]){		
		try {
			//Bnet bn = new Bnet(argv[0]);
			//PDG pdg = new PDG(bn);
			PDG pdg = PDGio.load(argv[0]);
			pdg.printMarginals();
			Vector<Node> nv = pdg.getNodes();
			elvira.Configuration c = new elvira.Configuration(nv);
			int card=c.possibleValues();
			double p, sum=0.0;
			for(int i=0;i<card;i++){
				pdg.setMultipleEvidence(c);
				pdg.computeIflOfl();
				p = pdg.probabilityOfEvidence();
				c.nextConfiguration();
				pdg.removeEvidence();
				sum+=p;
				System.out.println("P("+i+")\t="+p+"\t:"+sum);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (elvira.probabilisticDecisionGraph.ParseException e) {
			e.printStackTrace();
		} catch(PDGException e){
			e.printStackTrace();
		}
	}

	public void resetCounts(){
		for(PDGVariableNode p : variableForest){
			p.getParameterRootNode().resetCounts();
		}
	}
	
	public void learnParameters(CaseList cases, boolean smooth) throws PDGVariableNotFoundException{
		resetCounts();
		for(int i=0;i<cases.getNumberOfCases();i++){
			countConfiguration(cases.get(i));
		}
		updateParameters(smooth);
	}
	
	public void learnParametersFromReach(boolean smooth){
		for(PDGVariableNode p : variableForest)
			p.updateValues(smooth, true);		
	}
	
	public void countConfiguration(Configuration conf) throws PDGVariableNotFoundException {
		for(PDGVariableNode p : variableForest){
			p.getParameterRootNode().countConfiguration(conf);
		}
	}
	
	public void updateParameters(boolean smooth){
		for(PDGVariableNode p : variableForest){
			p.updateValues(smooth);
		}
	}
	
	public CaseList sample(int sampleLength){
		
		CaseList cl = new CaseListMem(this.getNodes());
		Random rnd = new Random(System.currentTimeMillis());
		for(int i=0;i<sampleLength; i++){
			Configuration conf = new Configuration();
			for(PDGVariableNode p : variableForest){
				p.getParameterRootNode().generateSample(conf, rnd);
			}
			cl.put(conf);
		}
		return cl;
	}

	public void synchronizeVariables(Vector<Node> v){
		for(PDGVariableNode p : variableForest){
			p.synchronizeVariables(v);
		}
	}
	
	public int effectiveSize(){
		int es = 0;
		for(PDGVariableNode x : variableForest){
			es += x.effectiveSize();
		}
		return es;
	}
	
	public int numberOfVariables(){
		int retval = 0;
		for(PDGVariableNode p : variableForest)
			retval += p.countVariables();
		return retval;
	}
	
	public int numberOfNodes(){
		int c = 0;
		for(PDGVariableNode p : variableForest)
			c += p.countNodes();
		return c;
	}
	
	public long numberOfIndependentParameters(){
		long c = 0;
		for(PDGVariableNode p : variableForest)
			c += p.countIndependentParameters();
		return c;
	}
	
	public void printStats(){
		System.out.println("effective size      : "+effectiveSize()+"\n"+
				           "number of variables : "+numberOfVariables()+"\n"+
				           "number of nodes     : "+numberOfNodes()+"\n"+
				           "number of parameters: "+numberOfIndependentParameters()+"\n" +
				           "max depth           : "+depth()+"\n" +
				           "max branching       : "+maxbranching());
	}
	
	int maxbranching(){
		int maxbr = 0;
		for(PDGVariableNode p : variableForest){
			int b = p.maxbranching();
			maxbr = (b > maxbr ? b : maxbr);
		}
		return maxbr;
	}
	
	int depth(){
		int maxdepth = 0;
		for(PDGVariableNode p : variableForest){
			int d = p.depth();
			maxdepth = (d > maxdepth ? d : maxdepth);
		}
		return maxdepth;
	}
	
	//public void smoothParameters(boolean smooth){
	//	for(PDGVariableNode p : variableForest){
	//		p.smoothParameters(smooth);
	//	}
	//}
	
	protected void clearCounts(){
		for(PDGVariableNode p : variableForest)
			p.clearCounts();
	}
	
	
	
	//public void learn(DataBaseCases data, int classVarIdx){
	//	elvira.probabilisticDecisionGraph.tools.PDGLearner pdgl = new PDGLearner();
	//	pdgl.setTrain(data);
	//}
	
	public static class ParamLearn{
		public static void main(String argv[]){
			if(argv.length != 3){
				System.out.println("usage : PDG$ParamLearn <model.pdg> <data.dbc> <smooth>");
				System.exit(112);
			}
			try{
				String pdgFile = argv[0];
				String dataFile = argv[1];
				boolean smooth = Boolean.parseBoolean(argv[2]);
				DataBaseCases db = new DataBaseCases(dataFile);
				CaseList cl = db.getCases();
				PDG pdg = PDGio.load(pdgFile);
				pdg.synchronizeVariables(cl.getVariables());
				pdg.learnParameters(cl, smooth);
				PDGio.save(pdg, pdgFile+"2");
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static class Sample{
		
		//public Sample(){}
		
		public static void main(String argv[]){
			if(argv.length!=3){
				System.out.println("usage : PDG$Sample <model.pdg> <sample size> <outputfile>");
				System.exit(112);
			}
			try{
				String pdgFile = argv[0];
				int sampleLength = Integer.parseInt(argv[1]);
				String outputFile = argv[2];
				PDG pdg = PDGio.load(pdgFile);
				
				CaseList cl = pdg.sample(sampleLength);
				DataBaseCases db = new DataBaseCases(pdg.getName(), cl);
				db.saveDataBase(new FileWriter(outputFile));
			} catch(FileNotFoundException fnf){
				fnf.printStackTrace();
			} catch(PDGException pdge){
				pdge.printStackTrace();
			} catch(ParseException pe){
				pe.printStackTrace();
			} catch(IOException ioe){
				ioe.printStackTrace();
			}
		}
	}
	
	protected void initialiseReach(){
		NodeList nl = this.getNodeList();
		for(PDGVariableNode pdgvn : variableForest){
			pdgvn.initializeReach(nl);
		}
	}

	public boolean checkBnetEquivalence(Bnet tanModel, boolean verbose) throws PDGIncompatibleEvidenceException {
		Configuration conf = new Configuration(getNodeList());
		long maxValues = conf.saferPossibleValues();
		if(verbose) System.out.println("checking all "+maxValues+" joint configurations, this may take some time");
		boolean retval = true;
		for(long i=0; i<maxValues;i++){
			//conf.remove(tan.getClassVar());
			//this.classify(conf, trainData.getClassId());
			insertEvidence(conf);
			double p = probabilityOfEvidence();
			double q = tanModel.evaluateFullConfiguration(conf);
			double d = p-q;
			if(-1.0e-10 > d || d > 1.0e-10){
				if(verbose){
					System.out.println("\nPDG ("+p+") and Bnet ("+q+") differs by "+(p-q)+" for configuration :");
					conf.pPrint();System.out.println();
				}
				retval = false;
				break;
			} 
			if(verbose && i != 0 && (i % (maxValues/5) == 0)){
				System.out.println("checked "+i+" configurations");
			}
			conf.nextConfiguration();
		}
		if(verbose) System.out.println("done!");
		return retval;
	}

	public boolean checkStructure(){
		for(PDGVariableNode variableRoot : variableForest){
			if(!variableRoot.testStructure()) return false;
		}
		return true;
	}
	
	public static class TestLoadingSaving{
		public static void main(String argv[]) throws ParseException, PDGException, IOException{
			if(argv.length != 1){
				System.out.println("usage : PDG$TestLoadingSaving <model.pdg>");
				System.exit(112);
			}
			System.out.println("loading .. ");
			PDG pdg = PDGio.load(argv[0]);
			pdg.checkStructure();
			String tmpFile = "/tmp/PDG.TestLoadingSaving"+System.currentTimeMillis()+".pdg";
			PDGio.save(pdg, tmpFile);
			pdg = PDGio.load(tmpFile);
			pdg.checkStructure();
		}
	}
	
	public static class TestMerge{
		public static void main(String argv[]) throws FileNotFoundException, ParseException, PDGException, VectorOpsException{
			PDG pdg = PDGio.load("test.pdg");
			pdg.checkStructure();
			pdg.initialiseReach();
			PDGVariableNode X4VariableNode = pdg.getVariableNodeByName("X4");
			PDGParameterNode p10 = X4VariableNode.getPNodeById(10);
			PDGParameterNode p16 = X4VariableNode.getPNodeById(16);
			p10.safeMerge(p16, false);
		}
	}
}
