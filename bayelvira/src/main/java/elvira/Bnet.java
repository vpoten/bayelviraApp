/* Bnet.java */

package elvira;


import java.util.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import elvira.inference.*;
import elvira.inference.clustering.*;
import elvira.inference.elimination.*;
import elvira.inference.approximate.*;
import elvira.inference.abduction.*;
import elvira.parser.*;
import elvira.potential.*;
import elvira.gui.KmpesDialog;
import elvira.tools.SampleGenerator;

/**
 * This class implements the structure for storing and
 * manipulating the Bayesian Networks.
 *
 * @version 0.1
 * @since 18/06/2007
 */


public class Bnet extends Network implements Serializable{
    
    static final long serialVersionUID = 4206351367322891828L;
    /**
     * Frequently used values.
     */
    public static final String ABSENT = "Absent";
    public static final String PRESENT = "Present";
    
    /**
     * <code>true</code> if the network has been compiled or <code>false</code>
     * otherwise.
     */
    private boolean isCompiled = false;
    
    
    /**
     * Program to check the performance from the command line.
     */
    
    public static void main(String args[]) throws elvira.parser.ParseException, IOException {
        
        Bnet b;
        int nparents, nnodes, ncases;
        Random generator;
        FileWriter f;
        
       /* if (args.length == 2){
            FileInputStream f1 =new FileInputStream(args[0]);
            FileInputStream f2 =new FileInputStream(args[1]);
            Bnet  b1 = new Bnet(f1);
            Bnet  b2 = new Bnet(f2);
            System.out.println("Distancia KL red2 a red1 " + b1.KL(b2) )  ;
            System.out.println("Distancia KL red1 a red2 " + b2.KL(b1) )  ;
        }
         
         
        else{
            if (args.length < 4) {
                System.out.print("Too few arguments. Arguments are: file,number of nodes,number of cases,number of parents");
            }
            else {
                f = new FileWriter(args[0]);
                nnodes = (Integer.valueOf(args[1])).intValue();
                ncases = (Integer.valueOf(args[2])).intValue();
                nparents = (Integer.valueOf(args[3])).intValue();
                generator = new Random();
                b = new Bnet(generator,nnodes,nparents,ncases,true,1,0.009);
                b.setName(args[0].substring(0,args[0].length()-4));
                b.saveBnet(f);
                f.close();
            }
        }*/
        
        /*   Para comprobar generacion de evidencias
        Evidence ev;
        
        b = new Bnet("asia.elv");
        ev = b.generateEvidenceByForwardSampling(b.getNodeList());
        f = new FileWriter("generada.evi");
        ev.save(f);
        f.close(); */
        
        //  Para convertir redes con formato .elv en .xbif
         
        b = new Bnet("asia.elv");
        f = new FileWriter("asia.xbif");
        
        //with an evidence file
        Evidence ev = b.generateEvidenceByForwardSampling(b.getNodeList());
        FileWriter fevi =  new FileWriter("asia.evi");
        ev.save(fevi);
        fevi.close();
        b.saveBnet2XBIF(f, ev);   
        
        //without evidence file
        //b.saveBnet2XBIF(f, new Evidence());
        f.close();
        
    }
    
    /**
     * Creates a new empty <code>Bnet</code> object.
     */
    
    public Bnet() {
        
        super(); // Calls a constructor of <code>Network</code>
    }
    
    
    /**
     * Creates a Bayesian network parsing it from a file.
     * @param f file that contains the network.
     */
    
    public Bnet(FileInputStream f) throws elvira.parser.ParseException ,IOException {
        
        BayesNetParse parser = new BayesNetParse(f);
        parser.initialize();
        
        parser.CompilationUnit();
        translate(parser);
    }
    
    /**
     * Creates a Bayesian network parsing it from a file. From the file
     * it is taken thw information about it, but probabilisitic information
     * is changed and converted to probability intervals, following the
     * imprecise Dirichlet model
     * @param nameOfFile file containing the network
     * @param n number of samples to use
     * @param s value to compute imprecise probabilities
     */
    
    public Bnet(String nameOfFile, int n, int s) throws elvira.parser.ParseException, IOException{
        FileInputStream f=new FileInputStream(nameOfFile);
        BayesNetParse parser = new BayesNetParse(f);
        parser.initialize();
        parser.CompilationUnit();
        translate(parser);
        
        // Now, change the probabilities to imprecise values
        changeProbabilities(n,s);
    }
    
    /**
     * Creates a Bayesian network parsing it from a file. The structure is
     * taken directly from the file, but the values are generated as a convex
     * set, given the extreme points. The arguments used in this method are:
     * @param nameOfFile file containing the structure of the network
     * @param n the number of extreme points to generate for every value of
     * probability
     * @param range the amplitude of the interval used to generate the extreme
     * points
     */
    
    public Bnet(String nameOfFile, int n, double range) throws elvira.parser.ParseException, IOException{
        FileInputStream f=new FileInputStream(nameOfFile);
        BayesNetParse parser = new BayesNetParse(f);
        parser.initialize();
        parser.CompilationUnit();
        translate(parser);
        
        // Now, get the extreme points
        changeProbabilities(n,range);
    }

    /**
     * Creates a Bayesian network parsing it from a file. The structure is
     * taken directly from the file, but the values are generated as a convex
     * set, given the extreme points. The arguments used in this method are:
     * @param nameOfFile file containing the structure of the network
     * @param percentage of configurations to have n extreme points. The rest
     *        will be related to one extreme point
     * @param n the number of extreme points to generate for every value of
     * probability
     * @param range the amplitude of the interval used to generate the extreme
     * points
     * @param keepCeros shows if ceros must be kept unaltered
     */
    
    public Bnet(String nameOfFile, double percentage, int n, double range, boolean keepCeros) throws elvira.parser.ParseException, IOException{
        FileInputStream f=new FileInputStream(nameOfFile);
        BayesNetParse parser = new BayesNetParse(f);
        parser.initialize();
        parser.CompilationUnit();
        translate(parser);
        
        // Now, get the extreme points
        changeProbabilities(percentage,n,range,keepCeros);
    }
    
    /**
     * Create a <code>Bnet</code> object. The nodes for the network as well as the
     * links (parents and children of the nodes) are specified in the NodeList
     *
     * @param nodeList NodeList that contain the nodes of the network
     */
    public Bnet(NodeList nodeList) {
        super();
        
        int i,j;
        this.nodeList = nodeList;
        //Create the link list.
        Iterator sNodes = nodeList.getNodes().iterator();
        for(i=0;i<nodeList.size();i++) {
            LinkList parents = ((FiniteStates)sNodes.next()).getParents();
            //Iterator sLinks = parents.getLinks().iterator();
            Iterator sLinks = parents.iterator();
            for(j=0;j<parents.size();j++) {
                this.linkList.insertLink((Link)sLinks.next());
            }
        }
        // insert the relations.
        sNodes = nodeList.getNodes().iterator();
        for(i=0;i<nodeList.size();i++) {
            this.addRelation((FiniteStates)sNodes.next());
        }
    }
    
    /**
     * Creates a Bayesian network parsing it from a file.
     * @param name name of the file that contains the network.
     */
    
    public Bnet(String name) throws elvira.parser.ParseException ,IOException{
        FileInputStream file=new FileInputStream(name);
        
        // Check if the file can be opened
        
        if (file == null){
            System.out.println("File "+file+" can not be opened");
            System.exit(-1);
        }
        
        BayesNetParse parser = new BayesNetParse(file);
        parser.initialize();
        parser.CompilationUnit();
        translate(parser);
    }
    
    /**
     * Creates a new <code>Bnet</code> using the file given in a URL.
     * @param url location of the file.
     * @see BayesNetParse#initialize
     * @see BayesNetParse#CompilationUnit
     */
    
    public Bnet(URL url) throws IOException, elvira.parser.ParseException {
        
        InputStream istream = url.openStream();
        BayesNetParse parser = new BayesNetParse(istream);
        parser.initialize();
        
        parser.CompilationUnit();
        translate(parser);
    }
    
    
    /**
     * This constructor generates a random network with discrete nodes of finite
     * states. It generates relations taking uniform random numbers
     * raised to argument <code>degreeOfExtreme</code>. With high values of
     * <code>degreeOfExtreme</code> we obtain very extreme probability values.
     * With <code>degreeOfExtreme</code> equal to 1, we obtain probability values
     * based on an uniform distribution.
     * @see Graph#Graph(Random,int,double,boolean)
     * @param generator a random number generator.
     * @param numberOfNodes number of nodes in the new network.
     * @param nParents average number f parents of a node.
     * @param nStates average number of states for each node.
     * @param con <code>true</code> for a connected network.
     * @param degreeOfExtreme integer number to transform each uniform random
     * number x, in  x^degreeOfExtreme.
     */
    
    public Bnet(Random generator, int numberOfNodes, double nParents,
    double nStates, boolean con, int degreeOfExtreme) {
        
        super(generator,numberOfNodes,nParents,con);
        PotentialTable potentialTable;
        Relation relation;
        NodeList pa, nodes;
        FiniteStates node;
        Vector states;
        int i;
        
        for (i=0 ; i< getNodeList().size() ; i++) {
            node = (FiniteStates)getNodeList().elementAt(i);
            states = exactStates(nStates);
            node.setStates(states);
            node.setTitle("");
            node.setComment("");
            node.setPosX(0);
            node.setPosY(0);
            node.setTypeOfVariable("finite-states");
            node.setKindOfNode("chance");
        }
        
        for (i=0 ; i< getNodeList().size() ; i++) {
            nodes = new NodeList();
            node = (FiniteStates)getNodeList().elementAt(i);
            nodes.insertNode(node);
            pa = parents(node);
            nodes.join(pa);
            relation = new Relation();
            relation.setVariables(nodes);
            relation.setKind(Relation.CONDITIONAL_PROB);
            potentialTable = new PotentialTable(generator,nodes,degreeOfExtreme);
            
            potentialTable.print();
            relation.setValues(potentialTable);
            getRelationList().addElement(relation);
        }
        
        setName("");
        setTitle("");
        setComment("");
        setAuthor("");
        setWhoChanged("");
        setWhenChanged("");
        setLocked(false);
        setVersion((float)1.0);
        setFSDefaultStates(FiniteStatesDefaultStates);
    }
    
    
    /**
     * This constructor generates a random network with discrete nodes of finite
     * states. It generates relations taking uniform random numbers
     * raised to argument <code>degreeOfExtreme</code>. With high values of
     * <code>degreeOfExtreme</code> we obtain very extreme probability values.
     * With <code>degreeOfExtreme</code> equal to 1, we obtain probability values
     * based on an uniform distribution.
     * Then the relations are transformed in probability trees with a degree
     * of approximation. The higher this degree of approximation the smaller
     * and more asymmetrical trees we will obtain.
     * @see Graph#Graph(Random,int,double,boolean)
     * @param generator a random number generator.
     * @param numberOfNodes number of nodes in the new network.
     * @param nParents average number f parents of a node.
     * @param nStates average number of states for each node.
     * @param con <code>true</code> for a connected network.
     * @param degreeOfExtreme integer number to transform each uniform random
     * number x, in  x^degreeOfExtreme.
     * @param approximation a float to approximate the probability tables by
     * probability trees.
     */
    
    public Bnet(Random generator, int numberOfNodes, double nParents,
    double nStates, boolean con, int degreeOfExtreme,
    double approximation) {
        
        super(generator,numberOfNodes,nParents,con);
        PotentialTable potentialTable;
        PotentialTree potentialTree;
        Relation relation;
        NodeList pa, nodes;
        FiniteStates node;
        Vector states;
        int i;
        
        for (i=0 ; i< getNodeList().size() ; i++) {
            node = (FiniteStates)getNodeList().elementAt(i);
            states = exactStates(nStates);
            node.setStates(states);
            node.setTitle("");
            node.setComment("");
            node.setPosX(0);
            node.setPosY(0);
            node.setTypeOfVariable("finite-states");
            node.setKindOfNode("chance");
        }
        
        for (i=0 ; i< getNodeList().size() ; i++) {
            nodes = new NodeList();
            node = (FiniteStates)getNodeList().elementAt(i);
            nodes.insertNode(node);
            pa = parents(node);
            nodes.join(pa);
            relation = new Relation();
            relation.setVariables(nodes);
            relation.setKind(Relation.CONDITIONAL_PROB);
            potentialTable = new PotentialTable(generator,nodes,degreeOfExtreme);
            potentialTree = potentialTable.toTree();
            potentialTree = potentialTree.sort();
            potentialTree.limitBound(approximation);
            potentialTree.print();
            relation.setValues(potentialTree);
            getRelationList().addElement(relation);
        }
        
        setName("");
        setTitle("");
        setComment("");
        setAuthor("");
        setWhoChanged("");
        setWhenChanged("");
        setLocked(false);
        setVersion((float)1.0);
        setFSDefaultStates(FiniteStatesDefaultStates);
    }
    
    
    /**
     * Stores the network in the file given as parameter.
     * @param f file where the network will be saved.
     * @see Network#save
     */
    
    public void saveBnet(FileWriter f) throws IOException {
        
        PrintWriter p;
        
        p = new PrintWriter(f);
        
        super.save(p);
        p.close();
    }
    
     /**
     * Stores the network in the XBIF file format given as parameter. Also, 
     * an evidence can be stored in the file, since it is permited.
     * @param f file where the network will be saved.
     * @param evid evidence of a set of variables.
     */
    public void saveBnet2XBIF(FileWriter f, Evidence evid) throws IOException {
       
        Node currentNode;
        PrintWriter p = new PrintWriter(f);
        double probValue;
       
        p.print("<xbif version=\"0.3a\">\n");
        p.print("\t<network>\n");
        p.print("\t\t<name>"+ this.getName()+"</name>\n");
        p.print("\n\t\t<!-- Variables -->\n");
        for (int i=0;i<this.getNodeList().size();i++){
            currentNode=this.getNodeList().elementAt(i);
            p.print("\t\t<variable type=\"discrete\">\n");
            p.print("\t\t\t<name>"+ currentNode.getName()+"</name>\n");
            p.print("\t\t\t<values>"+ ((FiniteStates)currentNode).getNumStates()+"</values>\n");
            
            int value=evid.getValue((FiniteStates)currentNode);
            if (value!=-1)//variable evidenced
               p.print("\t\t\t<observed>"+ value +"</observed>\n");
            
            p.print("\t\t</variable>\n\n");
        }
        p.print("\t\t<!-- Probabilities -->\n");
        for (int i=0;i<this.getNodeList().size();i++){
             currentNode=this.getNodeList().elementAt(i);
             p.print("\t\t<probability>\n");
             p.print("\t\t\t<for>"+ currentNode.getName() +"</for>\n");
             
             for (int j=0;j<currentNode.getParentNodes().size();j++)
                p.print("\t\t\t<given>"+ ((Node)this.getRelation(currentNode).getValues().getVariables().get(j+1)).getName() +"</given>\n");    
             p.print("\t\t\t<table>");
             
             //Some operations to obtain the configurations in a right order to save in XBIF format
             Configuration conf = new Configuration(this.getRelation(currentNode).getValues().getVariables());
             FiniteStates aux_var=(FiniteStates)conf.getVariable(0).copy(); 
             int aux_val=conf.getValue(0);
             conf.remove(0);
             conf.insert(aux_var, aux_val);
             
             NodeList nl = this.getRelation(currentNode).getParents().copy();
             nl.insertNode(currentNode.copy());
             int probabilityTableSize = (int)FiniteStates.getSize(nl);
             
             for (int k=0;k<probabilityTableSize;k++){
                  if (k%((FiniteStates)currentNode).getNumStates()==0)
                     p.print("\n\t\t\t\t");
                  probValue = this.getRelation(currentNode).getValues().getValue(conf);
                  p.print(probValue+" ");
                  conf.nextConfiguration();
             }
             p.print("\n\t\t\t</table>\n");
             p.print("\t\t</probability>\n\n");
        }
        p.print("\t</network>\n");
        p.print("</xbif>\n");
        p.close();
    }
    
    /**
     * Stores the header of the network in the stream given as parameter.
     * @param p stream where the network will be saved.
     */
    
    public void saveHead(PrintWriter p) throws IOException {
        
        p.print("// Bayesian Network\n");
        p.print("//   Elvira format \n\n");
        p.print("bnet  \""+getName()+"\" { \n\n");
    }
    
    
    /**
     * Checks whether the addition of a new link yields a cycle.
     * @param tail tail of the new link.
     * @param head head of the new link.
     * @return <code>true</code> if there a cycle is created or
     * <code>false</code> in other case.
     */
    
    public boolean hasCycle(Node tail, Node head) {
        
        Graph  g = duplicate();
        try{
            tail = g.getNodeList().getNode(tail.getName());
            head = g.getNodeList().getNode(head.getName());
            g.createLink(tail, head);
            return (!(g.isADag()));
        }
        catch (InvalidEditException iee){return true;}
    }
    
    
    /**
     * Creates a list of names of states for all the nodes in the
     * network, starting with character 's' followed by an integer
     * starting from 0.
     * @param nStates the number of names to create.
     * @return a <code>Vector</code> with the names.
     */
    
    private Vector exactStates(double nStates) {
        
        Vector aux;
        int i;
        
        aux = new Vector();
        
        for (i=0 ; i< nStates ; i++) {
            String nameState = new String("s"+i);
            aux.addElement(nameState);
        }
        
        return aux;
    }
    
    
    /**
     *
     */
    
    private Vector randomStates(Random generator,double nStates) {
        
        int x, i;
        Vector aux;
        double Xi, suma, tem, TR, den, div;
        
        suma = 0.0;
        tem = (double) (-1.0/nStates);
        den = generator.nextDouble();
        Xi = tem * Math.log(den);
        suma = suma + Xi;
        
        for (x=1 ; suma<1 ; x++) {
            den = generator.nextDouble();
            Xi = tem * Math.log(den);
            suma = suma + Xi;
        }
        
        if ((x-1) < 2)
            x = 3;
        aux = new Vector();
        
        for (i=0 ; i<x-1 ; i++) {
            String nameState = new String("s"+i);
            aux.addElement(nameState);
        }
        return aux;
    }
    
    
    /**
     * Compiles the network (performs a propagation).
     * @param index indicates the propagation method.
     * @param parameters parameters passed to the propagation methods.
     * @param fileNames files used by the propagation algorithms.
     * @param abductiveValues data used by abductive inference algorithms
     */
    
    public void compile(int index, Vector parameters, Vector filesNames,
    Vector abductiveValues) {
        
        Evidence e = new Evidence();
        double[] errors = new double[2];
        double g, mse, timePropagating = 0;
        Date date = new Date();
        FileWriter f;
        PrintWriter p;
        
        switch (index) {
            case 0: HuginPropagation hp = new HuginPropagation(this,e,"tables");
            hp.obtainInterest();
            hp.propagate(hp.getJoinTree().elementAt(0),"no");
            showResults(hp);
            break;
            case 1: HuginPropagation hp2 = new HuginPropagation(this,e,"trees");
            hp2.obtainInterest();
            hp2.propagate(hp2.getJoinTree().elementAt(0),"no");
            showResults(hp2);
            break;
            case 2: ApproximateHuginPropagation ahp = new
            ApproximateHuginPropagation(this,e,
            ((Double)parameters.elementAt(1)).doubleValue(),
            ((Integer)parameters.elementAt(0)).intValue());
            ahp.obtainInterest();
            ahp.propagate(ahp.getJoinTree().elementAt(0),"no");
            showResults(ahp);
            break;
            case 3: VariableElimination ve = new VariableElimination(this,e);
            ve.obtainInterest();
            ve.propagate();
            showResults(ve);
            break;
            
            case 4: VEWithPotentialTree veWithPT = new VEWithPotentialTree(this,e);
            veWithPT.obtainInterest();
            veWithPT.propagate();
            showResults(veWithPT);
            break;
            
            case 5: ImportanceSamplingTable ist =
            new ImportanceSamplingTable(this,e,
            getElement(parameters, 0),
            getElement(parameters, 1), 1);
            ist.obtainInterest();
            ist.propagate();
            showResults(ist);
            break;
            
            case 6: ImportanceSamplingTree isTree =
            new ImportanceSamplingTree(this,e,
            getFloatElement(parameters, 2),
            getElement(parameters, 0),
            getElement(parameters, 1), 1);
            isTree.obtainInterest();
            isTree.propagate();
            showResults(isTree);
            break;
            
            case 7: ImportanceSamplingFunctionTree isft =
            new ImportanceSamplingFunctionTree(this,e,
            getFloatElement(parameters, 2),
            getElement(parameters, 0),
            getElement(parameters, 1), 1);
            isft.obtainInterest();
            isft.propagate();
            showResults(isft);
            break;
            
            case 8: ImportanceSamplingTreeAV istav =
            new ImportanceSamplingTreeAV(this,e,
            getFloatElement(parameters, 2),
            getElement(parameters, 0),
            getElement(parameters, 1), 1);
            istav.obtainInterest();
            istav.propagate();
            showResults(istav);
            break;
            
            case 9: SystematicSamplingTable sst =
            new SystematicSamplingTable(this, e,
            getElement(parameters, 0),
            getElement(parameters, 1));
            try {
                sst.propagate((String) filesNames.elementAt(0),
                (String) filesNames.elementAt(1));
            }
            catch (Exception ex1) {}
            showResults(sst);
            break;
            
            case 10: SystematicSamplingTree sstree =
            new SystematicSamplingTree(this, e,
            getElement(parameters, 0),
            getElement(parameters, 1), 1);
            try {
                sstree.propagate((String) filesNames.elementAt(0),
                (String) filesNames.elementAt(1));
            }
            catch (Exception ex2) {}
            showResults(sstree);
            break;
            
            case 11: SimpleLazyPenniless slp =
            new SimpleLazyPenniless(this,e,
            getElement(parameters,4),
            getFloatElement(parameters,3),
            getFloatElement(parameters,1),
            getElement(parameters,0),
            ((Boolean) parameters.elementAt(2)).booleanValue(),
            ((Boolean) parameters.elementAt(5)).booleanValue(),
            getElement(parameters,5));
            
            try {
                timePropagating = (double)date.getTime();
                slp.propagate((String) filesNames.elementAt(0));
                timePropagating = ((double)date.getTime()-timePropagating) / 1000;
                slp.readExactResults((String) filesNames.elementAt(1));
                slp.computeError(errors);
            }
            catch (Exception ex4) {}
            
            break;
            
            case 12: AbductiveInferenceNilsson ain= new
            AbductiveInferenceNilsson(this,e,"tables");
            if ( !((Boolean)abductiveValues.elementAt(0)).booleanValue() ){
                ain.setPartial(true);
                if (parameters==null) ain.setPropComment("size");
                else ain.setPropComment((String)parameters.elementAt(0));
                ain.setExplanationSet((NodeList)abductiveValues.elementAt(1));
                System.out.println("\nExpSet = " +
                ain.getExplanationSet().toString());
            }
            else {
                ain.setPartial(false);
                ain.setExplanationSet(ain.getExplanationSet()); //empty nodeList
            }
            
            ain.setNExplanations(
            ((Integer)abductiveValues.elementAt(2)).intValue());
            
            ain.propagate("maxprobexpot.pot");
            ain.results=((Explanation)ain.getKBest().elementAt(0)).
            toPosteriorProbability(this.getNodeList(),e);
            
            System.out.println("nexp = " + ain.getNExplanations());
            if (ain.getNExplanations()>1){
                KmpesDialog kmpeDialog = new KmpesDialog(
                ain.getKBest());
            }
            
            showResults(ain);
            break;
            
            case 13: AbductiveInferenceNilsson ain2= new
            AbductiveInferenceNilsson(this,e,"tables");
            if ( !((Boolean)abductiveValues.elementAt(0)).booleanValue() ){
                ain2.setPartial(true);
                if (parameters==null) ain2.setPropComment("size");
                else ain2.setPropComment((String)parameters.elementAt(0));
                ain2.setExplanationSet((NodeList)abductiveValues.elementAt(1));
            }
            else {
                ain2.setPartial(false);
                ain2.setExplanationSet(ain2.getExplanationSet()); //empty nodeList
            }
            
            ain2.setNExplanations(
            ((Integer)abductiveValues.elementAt(2)).intValue());
            
            ain2.propagate("maxprobexpot.pot");
            ain2.results=((Explanation)ain2.getKBest().elementAt(0)).
            toPosteriorProbability(this.getNodeList(),e);
            
            showResults(ain2);
            break;
            
            case 14: ApproximateAbductiveInferenceNilsson aain= new
            ApproximateAbductiveInferenceNilsson(this,e,
            ((Double)parameters.elementAt(1)).doubleValue(),
            ((Integer)parameters.elementAt(0)).intValue());
            
            if ( !((Boolean)abductiveValues.elementAt(0)).booleanValue() ){
                aain.setPartial(true);
                if (parameters==null) aain.setPropComment("size");
                else aain.setPropComment((String)parameters.elementAt(2));
                aain.setExplanationSet((NodeList)abductiveValues.elementAt(1));
            }
            else {
                aain.setPartial(false);
                aain.obtainInterest();
                aain.setExplanationSet(aain.interest);
            }
            
            aain.setNExplanations(
            ((Integer)abductiveValues.elementAt(2)).intValue());
            
            aain.propagate("maxprobexpot.pot");
            aain.results=((Explanation)aain.getKBest().elementAt(0)).
            toPosteriorProbability(this.getNodeList(),e);
            
            showResults(aain);
            
            break;
            
            case 15: LikelihoodWeighting lw = new LikelihoodWeighting(this,e);
            lw.obtainInterest();
            Integer simStep = new Integer(parameters.elementAt(0).toString());
            try {
                lw.propagate(simStep.toString(),"potential.pot");
            } catch(ParseException pe) {}
            catch(IOException ioe) {System.out.println("No se han grabado ");}
            showResults(lw);
            break;
            
        }
        
        g = errors[0];
        mse = errors[1];
        
        System.out.println("Time propagating (secs) : "+timePropagating);
        System.out.println("G : "+g);
        System.out.println("MSE : "+mse);
        
        setCompiled(true);
    }
    
    
    /**
     * Gets the element in a given position of the given network, as
     * a <code>int</code>.
     * @param parameters the <code>Vector</code> of parameters.
     * @param i the position of the parameter to retrieve.
     * @return the element at position <code>i</code> in <code>parameters</code>.
     */
    
    public int getElement(Vector parameters, int i) {
        
        Integer element = (Integer) parameters.elementAt(i);
        return element.intValue();
    }
    
    /**
     * Gets the element in a given position of the given network, as
     * a <code>double</code>.
     * @param parameters the <code>Vector</code> of parameters.
     * @param i the position of the parameter to retrieve.
     * @return the element at position <code>i</code> in <code>parameters</code>.
     */
    
    public double getFloatElement(Vector parameters, int i) {
        
        Double element = (Double) parameters.elementAt(i);
        return element.doubleValue();
    }
    
    
    /**
     * Shows the results of a propagation.
     * @param p the propagation.
     */
    
    public void showResults(Propagation p) {
        
        setCompiledPotentialList(p.results);
        for (int i=0 ; i<p.results.size() ; i++) {
            System.out.print("Resultados ");
            ((Potential)p.results.elementAt(i)).showResult();
            System.out.println();
        }
    }
    
    
    /**
     * Determines whether the network has been compiled or not.
     * @return <code>true</code> if the network has been compiled,
     * <code>false</code> otherwise.
     */
    
    public boolean getIsCompiled() {
        
        return isCompiled;
    }
    
    
    /**
     * Marks the network as compiled or not.
     * @param b <code>true</code> if the network has been compiled,
     * <code>false</code> otherwise.
     */
    
    public void setCompiled(boolean b) {
        
        isCompiled = b;
    }
    
    /**
     * Method to change te probabilities of the network to imprecise
     * probabilities
     * @param n number of samples to consider
     * @param s value to apply imprecise Dirichlet model
     */
    
    private void changeProbabilities(int n, int s){
        Iterator sRelations = getRelationList().iterator();
        Relation rel;
        Potential values;
        Potential result;
        
        while(sRelations.hasNext()){
            // Get a relation
            rel=(Relation)sRelations.next();
            
            // Get the values from this relation
            values=rel.getValues();
            
            // Modify the potential to be an imprecise distribution
            result=values.toImpreciseDirichletModel(n,s);
            
            // This values are added to the relation
            rel.setValues(result);
            rel.print();
        }
    }
    
    /**
     * Method to change te probabilities of the network to imprecise
     * probabilities, using extreme points
     * @param n number of extreme points to generate
     * @param range amplitude of the interval used to generate the
     * extreme points
     */
    
    private void changeProbabilities(int n, double range){
        Iterator sRelations = getRelationList().iterator();
        Relation rel;
        Potential values;
        Potential result;
        
        while(sRelations.hasNext()){
            // Get a relation
            
            rel=(Relation)sRelations.next();
            
            // Get the values from this relation
            
            values=rel.getValues();
            
            // Modify the potential to be an imprecise distribution
            
            result=values.toConvexSetModel(n,range);
            
            // This values are added to the relation
            
            rel.setValues(result);
            //rel.print();
        }
    }

    /**
     * Method to change te probabilities of the network to imprecise
     * probabilities, using extreme points
     * @param percentage of configurations to have n extreme points.
     *        The rest will be related to one extreme point
     * @param n number of extreme points to generate
     * @param range amplitude of the interval used to generate the
     * extreme points
     * @param keepCeros shows if it is needed to keep ceros unaltered
     */
    
    private void changeProbabilities(double percentage, int n, double range, boolean keepCeros){
        Iterator sRelations = getRelationList().iterator();
        Relation rel;
        Potential values;
        Potential result;
        
        while(sRelations.hasNext()){
            // Get a relation
            rel=(Relation)sRelations.next();
            
            // Get the values from this relation
            values=rel.getValues();
            
            // Modify the potential to be an imprecise distribution
            result=values.toConvexSetModel(percentage,n,range,keepCeros);
            
            // This values are added to the relation
            rel.setValues(result);
        }
    }
    
    
    /**
     * Transform a relation refered to other bnet to a relation refered to this bnet
     * @param r Relation refered to other bnet
     * @return A copy of the original relation, but refered to the variables of this bnet.
     * The new relation is not added to the list of relations
     */
    public Relation translateRelation(Relation r){
        NodeList varsInOriginal;
        Vector varsInNew;
        Node node;
        Relation rNew;
        Potential ptOriginal;
        Potential ptNew;
        
        
        // Get the variables of this relation, and get references
        // to these variables, but for the new list of nodes
        
        varsInOriginal=r.getVariables();
        varsInNew=new Vector();
        
        for(int i=0; i < varsInOriginal.size(); i++){
            node=getNode(((Node)varsInOriginal.elementAt(i)).getName());
            
            // This node is inserted in vars
            varsInNew.addElement(node);
        }
        
        // Create the new relation. Use copy method to initialize
        // all data fields, but now change variables in the same
        // relation and in its potential
        
        rNew=r.copy();
        rNew.setVariables(varsInNew);
        
        // Now, copy its values, if present
        
        ptOriginal=r.getValues();
        if (ptOriginal != null){
            ptNew=ptOriginal.copy();
            
            // Now set the list of original variables. If the relation
            // is a UTILITY relation, the initial variable (for the
            // utility node should not appear in the potential)
            
            if((r.getKind() == Relation.UTILITY)||(r.getKind()==Relation.UTILITY_COMBINATION)){
                varsInNew.removeElementAt(0);
            }
            
            ptNew.setVariables(varsInNew);
            
            // Set this potential to the new relation
            
            rNew.setValues(ptNew);
        }
        
        return rNew;
        
    }
    
    /**
     * Method to positionate the nodes of a Bnet when their positions make it isn't displayed
     * well because they are put on top.
     */
    public void positionNodes(){
        
        NodeList auxChildren;
        NodeList nodesPreviousLevel;
        NodeList nodesActualLevel;
        Vector listOfLevels=new Vector();
        int level;
        Node auxNode;
        
        
        NodeList remainingNodes=getNodeList().copy();
        
        
        //Compute Level 0
        listOfLevels.add(new NodeList());
        nodesActualLevel=(NodeList)(listOfLevels.elementAt(0));
        for (int i=0;i<remainingNodes.size();i++){
            auxNode=remainingNodes.elementAt(i);
            if (auxNode.getParents().size()==0){
                nodesActualLevel.insertNode(auxNode);
            }
            
        }
        
        //Remove the nodes of the level 0 from the list of remaining nodes
        for (int i=0;i<nodesActualLevel.size();i++){
            remainingNodes.removeNode(nodesActualLevel.elementAt(i));
        }
        
        
        //Compute the rest of levels
        while (remainingNodes.size()>0){
            listOfLevels.add(new NodeList());
            level=listOfLevels.size();
            nodesPreviousLevel=(NodeList)(listOfLevels.elementAt(level-2));
            nodesActualLevel=(NodeList)(listOfLevels.elementAt(level-1));
            
            //The children of the nodes of the previous level are going to be the nodes of the actual level
            for (int i=0;i<nodesPreviousLevel.size();i++){
                auxChildren=nodesPreviousLevel.elementAt(i).getChildrenNodes();
                for (int j=0;j<auxChildren.size();j++){
                    Node iChild=auxChildren.elementAt(j);
                    if (iChild.getParentNodes().intersection(remainingNodes).size()==0){
                        if (nodesActualLevel.getId(iChild)==-1){
                            nodesActualLevel.insertNode(iChild);
                        }
                    }
                }
            }
            
            //Remove the nodes of the actual level from the list of remaining nodes
            for (int i=0;i<nodesActualLevel.size();i++){
                remainingNodes.removeNode(nodesActualLevel.elementAt(i));
            }
        }
        
        //Positionate the nodes of each level
        int width=1000;
        for (int i=0;i<listOfLevels.size();i++){
            NodeList nodesLevel;
            
            nodesLevel=(NodeList)(listOfLevels.elementAt(i));
            int posY=50+i*120;
            int sizeLevel=nodesLevel.size();
            for (int j=0;j<sizeLevel;j++){
                int posX;
                if (sizeLevel==1) posX=(int) Math.round(width/2)+50;
                else posX = (int) Math.round((width/(sizeLevel-1))*j)+50;
                auxNode=nodesLevel.elementAt(j);
                auxNode.setPosX(posX);
                auxNode.setPosY(posY);
            }
        }
        
        
    }
    
    
    /**
     * Generates randomly a mixed MTE network.
     *
     * @param n The number of nodes
     * @param d The number of discrete nodes
     * @param min The minimum value of the continuous nodes.
     * @param max The maximum value of the continuous nodes.
     *
     */
    
    public Bnet generateMixedMTENetwork(int numNodes, int d, double min, double max){
        
        int i, dis, nStates, nSplits, nTerms,j, numberNode,nParents;
        double a, meanExpDiscrete, meanExpIndep, meanExpCoef, meanNormalExponent, sdevNormalExponent;
        Vector discrete;
        Random alea = new Random();
        String nameNode;
        boolean isDiscrete;
        Continuous contVar;
        FiniteStates discVar;
        Node var;
        NodeList randomNodeList = new NodeList();
        Bnet randomBnet = new Bnet();
        Relation randomRelation;
        PotentialContinuousPT randomPotentialContinuousPT;
        ContinuousProbabilityTree randomCPT;
        MixtExpDensity randomMTE;
        Link randomLink;
        LinkList randomLinkList;
        SampleGenerator samGen = new SampleGenerator();
        
        System.out.println("We are generating a random Bnet");
        
        // These are the vector I will select (randomly) some parameters from
        // The number of states of the discrete variables
        Vector discValues = new Vector();
        // The probablities of the former states
        Vector discProb = new Vector();
        
        // The number of splits of the range of a continuous variable in a particular tree
        Vector nSplitsValues = new Vector();
        // The probabilities of the former splits
        Vector nSplitsProb = new Vector();
        
        // The number of terms in a mixtExpDensity
        Vector nTermsValues = new Vector();
        // The probabilities fo the former terms
        Vector nTermsProb = new Vector();
        
        // The vector where we will keep the parents of a node
        Vector parents = new Vector();
        
        discValues.addElement(new Integer(2));
        discValues.addElement(new Integer(3));
        discValues.addElement(new Integer(4));
        
        discProb.addElement(new Double(0.3333333));
        discProb.addElement(new Double(0.3333333));
        discProb.addElement(new Double(0.3333333));
        
        nSplitsValues.addElement(new Integer(1));
        nSplitsValues.addElement(new Integer(2));
        nSplitsValues.addElement(new Integer(3));
        //nSplitsValues.addElement(new Integer(4));
        
        nSplitsProb.addElement(new Double(0.2));
        nSplitsProb.addElement(new Double(0.4));
        nSplitsProb.addElement(new Double(0.4));
        //nSplitsProb.addElement(new Double(0.125));
        
        nTermsValues.addElement(new Integer(0));
        nTermsValues.addElement(new Integer(1));
        nTermsValues.addElement(new Integer(2));
        
        nTermsProb.addElement(new Double(0.05));
        nTermsProb.addElement(new Double(0.75));
        nTermsProb.addElement(new Double(0.20));
        
        randomLinkList = new LinkList();
        
        // First we will decide how many of these nodes will be discrete.
        // To do so We select d random numbers from 1 to n.
        
        discrete = new Vector();
        i= 0;
        while (i< d){
            
            dis =  (alea.nextInt(numNodes));
            if(!discrete.contains(new Integer(dis))){
                discrete.addElement(new Integer(dis));
                i++;
            }
            
        }
        
        // Now we will create the n nodes (variables)
        
        for(i=0 ; i < numNodes ; i++){
            isDiscrete = false;
            for(j=0 ; j < discrete.size() ; j++){
                if(i == ((Integer)discrete.elementAt(j)).intValue()){
                    isDiscrete = true;
                    System.out.println("The variable is discrete");
                }
            }
            if(!isDiscrete){// The variable is continuous
                System.out.println("The variable is continuous");
                var = new Continuous();
                var.setTypeOfVariable(0);
                ((Continuous)var).setMax(max);
                ((Continuous)var).setMin(min);
            }
            else{// The variable is discrete
                System.out.println("The variable is discrete");
                System.out.println("Now I will decide how many states it will have:");
                nStates = samGen.getRandomValue(discValues, discProb);
                System.out.println("The variable will have "+nStates+" states.");
                var = new FiniteStates(nStates);
                var.setTypeOfVariable(1);
                // Now we have to select how many states it will have.
                // To do so we have to use the disc Vectors
                
                
                //((FiniteStates)var).setNumStates(nStates);
            }
            var.setName("X"+i);
            randomNodeList.insertNode(var);
        }// End of for (creating the nodes)
        
        randomBnet.setNodeList(randomNodeList);
        
        // The nodes are already created, now we must create the links an the potentials related to the links.
        
        meanExpDiscrete = 0.5;
        meanExpIndep = 0.01;
        meanExpCoef = 1;
        meanNormalExponent = 0;
        sdevNormalExponent = 1;
        
        for(i = 0 ; i<numNodes ; i++){
            nParents = 0;
            System.out.println("We are deciding how many parents each node has.");
            var = randomNodeList.elementAt(i);
            parents = new Vector();
            if(i>0){
                //nParents = 0;
                //while(nParents == 0)
                nParents = samGen.randomPoisson(0.8);
                if(nParents > i)
                    nParents = i;
                j = 0;
                System.out.println("nParents is "+nParents);
                while (j< nParents){
                    numberNode =  (alea.nextInt(i));
                    System.out.println("This is the value nextInt has returned: "+numberNode);
                    System.out.println("i vale: "+i);
                    System.out.println("This node will be a parent: "+numberNode);
                    System.out.println("Now we check if it is not repeated");
                    if(!parents.contains(randomNodeList.elementAt(numberNode))){
                        System.out.println("We insert the node number "+numberNode);
                        parents.addElement(randomNodeList.elementAt(numberNode));
                        j++;
                    }
                }
            }//End of if (i>0)
            System.out.println("This variable ");
            var.print();
            System.out.println("has "+nParents+" parents.");
            
            // Now I create the links
            
            for (j=0 ; j< parents.size() ; j++){
                
                randomLink = new Link((Node)parents.elementAt(j),var);
                randomLinkList.insertLink(randomLink);
                var.getParents().insertLink(randomLink);
                ((Node)parents.elementAt(j)).getChildren().insertLink(randomLink);
            }
            
            //Now I must create the relation.
            
            // First I create the CPT, after that I will create the potential
            System.out.println();
            System.out.println("Now we are creating the relation for this variable");
            System.out.println("***************************************************");
            System.out.println("Now we decide how many splits there will be in this potential.");
            nSplits = samGen.getRandomValue(nSplitsValues,nSplitsProb);
            System.out.println("In this potential there will be "+nSplits+" splits.");
            System.out.println("Now we decide how many terms there will be in this potential.");
            nTerms = samGen.getRandomValue(nTermsValues, nTermsProb);
            System.out.println("In this potential there will be "+nTerms+" terms.");
            
            randomCPT = new ContinuousProbabilityTree();
            randomCPT = randomCPT.createFullyExpandedCPTIterative(parents, var, nSplits, nTerms, meanExpDiscrete, meanExpIndep, meanExpCoef, meanNormalExponent, sdevNormalExponent);
            
            randomPotentialContinuousPT = new PotentialContinuousPT();
            randomPotentialContinuousPT.setTree(randomCPT);
            randomPotentialContinuousPT.setNumSplits(nSplits);
            randomPotentialContinuousPT.setNumTerms(nTerms);
            
            // Now I must create a relation to insert it in the list of relations
            
            randomRelation = new Relation();
            randomRelation.setValues(randomPotentialContinuousPT);
            //randomRelation.setNumTerms(nTerms);
            //randomRelation.setNumSplits(nSplits);
            
            // Now I must include this relation in the list of relations of the Bnet
            
            randomBnet.addRelation(randomRelation);
            System.out.println("Finished  creating the relation for this variable");
            System.out.println("***************************************************");
            System.out.println();
            // ******
            parents.insertElementAt(var,0);
            randomRelation.setVariables(parents);
        }//End of for
        
        randomBnet.setLinkList(randomLinkList);
        
        randomBnet.simplify();
        
        return randomBnet;
        
    }// End of method
    
    
    
    /**
     *  This method discretises the current Network, that is, it removes all the exponential terms.
     *
     *
     */
    
    public Bnet getDiscreteNetwork(){
        
        int i,nsplits;
        Bnet discreteNet = new Bnet();
        Vector relVecCont, relVecDisc ;
        LinkList links = new LinkList();
        NodeList nodes = new NodeList();
        NodeList nodesPot;
        Relation contRel, discRel;
        PotentialContinuousPT contPot;
        ContinuousProbabilityTree contCPT, discCPT;
        Continuous var;
        
        
        links = getLinkList();
        nodes = getNodeList();
        relVecCont = new Vector();
        relVecCont = getRelationList();
        
        discreteNet.setNodeList(nodes);
        discreteNet.setLinkList(links);
        
        relVecDisc = new Vector();
        
        // Now it is time to change the potentials
        for(i=0 ; i<relVecCont.size() ; i++){
            contRel = (Relation)relVecCont.elementAt(i);
            if(((Node)((NodeList)contRel.getVariables()).firstElement()).getTypeOfVariable() == 0){
                var = (Continuous)((NodeList)contRel.getVariables()).firstElement();
                contPot = (PotentialContinuousPT)contRel.getValues();
                //System.out.println("Numero de splits del original continuo: "+contPot.getNumSplits());
                nsplits = contPot.getNumSplits();
                nodesPot = contRel.getVariables();
                contCPT = contPot.getTree();
                //Changing the potential
                
                contCPT.getDiscretePotential(contCPT,var);
                //Creating new PotentialContinuousPT and  Relation
                contPot = new PotentialContinuousPT(nodesPot,contCPT);
                
                discRel = new Relation();
                
                discRel.setVariables(nodesPot);
                contPot.setNumSplits(nsplits);
                contPot.setNumTerms(1);
                //System.out.println("Numero de terminos: "+contPot.getNumTerms());
                //System.out.println("Numero de splits: "+contPot.getNumSplits());
                discRel.setValues(contPot);
                
                relVecDisc.addElement(discRel);
                
            }else{
                relVecDisc.addElement(contRel);
            }
            
        }// End of for
        
        discreteNet.setRelationList(relVecDisc);
        
        
        return discreteNet;
        
    } // End of method
    
    
    
    /**
     *  This method simplifies the current Network, removing those nodes without children or parents.
     *
     *
     */
    
    public void simplify(){
        
        int i = 0, tam;
        Node var;
        
        tam = getNodeList().size();
        
        while(i < tam ){
            // Para cada variable miro a ver si tiene o no hijos y/o padres.
            System.out.println("Ahora tam es : "+tam);
            System.out.println("Ahora i es : "+i);
            var = getNodeList().elementAt(i);
            
            if(children(var).size() == 0)// No tiene hijos
                if(parents(var).size() == 0){// No tiene padres tampoco, asi que hay que eliminarlo
                    System.out.println("Eliminamos la variable: "+var.getName());
                    removeNode(var);
                    tam--;
                }else
                    i++;
            else i++;
            
        }
        
    }
    
    
    /**
     * Generates evidence with positive probability using forward sampling.
     * It only works with FiniteStates variables.
     *
     * @param nl a NodeList with the variables for which the evidence
     * will be generated.
     * @return the generated evidence.
     */
    
    public Evidence generateEvidenceByForwardSampling(NodeList nl) {
        
        Evidence ev;
        NodeList simulationOrder;
        int i, j, nStates;
        FiniteStates v;
        Configuration conf = new Configuration();
        Configuration eviConf;
        double[] table;
        double r, total;
        PotentialTable pot;
        Relation rel;

        
        simulationOrder = this.topologicalOrder();
        
        for (j=0 ; j < simulationOrder.size() ; j++) {
            v = (FiniteStates)simulationOrder.elementAt(j);
            rel = this.getRelation(v);
            pot = (PotentialTable)rel.getValues();
            nStates = v.getNumStates();
            table = new double[nStates];
            for (i=0 ; i<nStates ; i++) {
                conf.putValue(v,i);
                table[i] = pot.getValue(conf);
            }
            
            r = Math.random();
            for (i=0,total=0.0 ; i<nStates ; i++) {
                total += table[i];
                if (r <= total) break;
            }
            conf.putValue(v,i);
         }
        
        // Now create a configuration just with the variables of interest
        eviConf = new Configuration(conf,nl);
        
        ev = new Evidence(eviConf);
        
        return ev;
    }
    
    
    
    /**
     *  This method generates randomly Evidence for this network..
     *
     * @param n The number of observed variables
     */
    
    public Evidence generateContinuousEvidence(int n ){
        
        int i, k, observadas = 0, nStatesVar, generadoInt;
        double generado, al, max, min, prob;
        ContinuousConfiguration conf = new ContinuousConfiguration();
        boolean nuevo;
        Node X;
        NodeList nl = getNodeList();
        Random alea = new Random();
        Vector x = new Vector();
        Vector y = new Vector();
        SampleGenerator s = new SampleGenerator();
        Evidence e;
        
        k = alea.nextInt(nl.size());
        System.out.println("La primera vble observada es la "+k);
        while (observadas < n){
            
            X = nl.elementAt(k);
            
            // Ahora pasamos a genrerar un valor de la variable, discreto o continuo.
            
            
            if(X.getTypeOfVariable() == 0){ // La variable es continua
                al = Math.random();
                max = ((Continuous)X).getMax();
                min = ((Continuous)X).getMin();
                
                generado = min + al*(max-min);
                
                conf.insert((Continuous)X,generado);
                observadas++;
                
            }else{// La variable es discreta
                
                nStatesVar = ((FiniteStates)X).getNumStates();
                prob = Math.pow((double)nStatesVar,-1);
                x = new Vector();
                y = new Vector();
                for(i=0 ; i < nStatesVar ; i++){
                    
                    x.addElement(new Integer(i));
                    y.addElement(new Double(prob));
                }
                
                generadoInt = s.getRandomValue(x,y);
                conf.insert((FiniteStates)X,generadoInt);
                observadas++;
                
            }
            
            nuevo = false;
            while(!nuevo){
                
                k = alea.nextInt(nl.size());
                System.out.println("intento la "+k);
                if(conf.indexOf(nl.elementAt(k)) == (-1)){
                    nuevo = true;
                    System.out.println("Esta no la hemos observado antes (la "+k+")");
                }else
                    nuevo = false;
            }
            
            
        }
        
        // Ya estan todas las variables observadas, asi que ahora me creo el objeto Evidence
        
        e = new Evidence(conf);
        return e;
    }
    
    
/* It computes the expected log-likelihood of belief network b
 * when samples are generated according to the present network.
 * Useful as an absolute measure of agreemt betwen the true network
 * and the learned network (b).
 * @param b the network
 */
    
    
    public double expLogLike(Bnet b){
        
        
        NodeList bNodes = b.getNodeList();
        NodeList vars,paXi,parentsXi;
        FiniteStates Xi,bXi;
        Node Thisnode;
        Relation r;
        Configuration confXi,confPaXi,confAux;
        int nConf,i,j,k,pos,n;
        double valXi,valPaXi,val,suma,x,y;
        Potential pot,potres;
        Evidence e;
        PotentialTable pot1,pot2;
        NodeList DBNodes = getNodeList();
        VariableElimination propagation;
        Vector result;
        
        suma = 0.0;
        
        
        
        for(i=0 ; i< bNodes.size() ; i++){
            
            Xi = (FiniteStates) bNodes.elementAt(i);
            // System.out.println("Nodo considerado ");
            // Xi.print();
            n = Xi.getNumStates();
            paXi = b.parents(Xi);
            
            
            
            r = b.getRelation(Xi);
            pot = r.getValues();
            
            
            //   System.out.println("Potencial de padres");
            //   pot.print();
            //   System.out.println("Padres");
            parentsXi = new NodeList();
            for(j=0; j<paXi.size(); j++) {
                // paXi.elementAt(j).print();
                Thisnode = this.getNodeList().getNode(paXi.elementAt(j).getName());
                parentsXi.insertNode(Thisnode);
            }
            
            
            nConf = (int) FiniteStates.getSize(parentsXi);
            //   System.out.println("Numero de configuraciones " + nConf);
            confAux = new Configuration(parentsXi);
            bXi = (FiniteStates) this.getNodeList().getNode(Xi.getName());
            for(j=0 ; j< nConf ; j++){
                //      System.out.println(" j " + j + "Configuration ");
                confAux.print();
                e = new Evidence(confAux);
                
                propagation = new VariableElimination(this,e,true);
                
                
                propagation.getPosteriorDistributionOf(bXi);
                
                
                result= propagation.getResults();
                pot1 = (PotentialTable) result.elementAt(0);
                // pot1.print();
                
                
                potres = (Potential) pot.restrictVariable(confAux);
                
                if (potres.getClass() == PotentialTree.class) {
                    pot2 = new PotentialTable((PotentialTree)potres);}
                else {pot2 = (PotentialTable) potres;}
                
                
                for(k=0;k<n;k++){
                    x = pot1.getValue(k);
                    
                    y = pot2.getValue(k);
                    //  System.out.println("Valor real "+x+"Valor calculado" + y);
                    if (y>0.0)  {suma+= (x*Math.log(y));}
                    else {if ((x>0.0)&&(y==0.0)) {suma+= Double.NEGATIVE_INFINITY;}}
                    
                    
                }
                confAux.nextConfiguration();
            }
            
        }
        
        
        return suma;
        
    }
    
    
/* It computes the KL distance of network b
 * to this network
 * @param b the network
 */
    
    
    public double KL(Bnet b){
        
        double x,y;
        
        x = expLogLike(this);
        
        y = expLogLike(b);
        
        // System.out.println("equal " + x + " different " + y);
        return (x-y);
        
    }
   
 
 /**
  * Returns a copy of this Bnet.
  *
  * @returns a copy of this Bnet.
  */
    
 public Bnet copyBnet(){

 Bnet co = new Bnet();
 Node newNode, n;

  try{
    for (int posn=0 ; posn<nodeList.size() ; posn++) {
      n = (Node)nodeList.elementAt(posn);
      newNode = n.copy();
      co.addNode(newNode);
    }
    Link l;
    Node h, t;
    for (int posl=0 ; posl<linkList.size() ; posl++) {
      l = (Link)linkList.elementAt(posl);
      t = l.getTail();
      h = l.getHead();
      t = co.getNodeList().getNode(t.getName());
      h = co.getNodeList().getNode(h.getName());
      co.createLink(t,h,l.getDirected());
    }
    return co;
  }
  catch (InvalidEditException iee){
    return null;
  }

}
 
 /**
  * Returns a shallow copy of this Bnet - that is, nodes are not copied.
  *
  * @returns a shallow copy of this Bnet.
  */
    
 public Bnet shallowCopyBnet(){

 Bnet co = new Bnet();
 Node newNode, n;
 co.setNodeList(this.getNodeList().copy());
  try{
    Link l;
    Node h, t;
    for (int posl=0 ; posl<linkList.size() ; posl++) {
      l = (Link)linkList.elementAt(posl);
      t = l.getTail();
      h = l.getHead();
      t = co.getNodeList().getNode(t.getName());
      h = co.getNodeList().getNode(h.getName());
      co.createLink(t,h,l.getDirected());
    }
    return co;
  }
  catch (InvalidEditException iee){
    return null;
  }

}
 
 
  /**
  * Returns a copy of this Bnet, including the RelatioList.
  *
  * @returns a copy of this Bnet.
  */
    
 public Bnet copyBnetIncludingRelations() throws java.lang.Throwable{

 Bnet co = new Bnet();
 Node newNode, n;

  try{
    for (int posn=0 ; posn<nodeList.size() ; posn++) {
      n = (Node)nodeList.elementAt(posn);
      newNode = n.copy();
      co.addNode(newNode);
    }
    Link l;
    Node h, t;
    for (int posl=0 ; posl<linkList.size() ; posl++) {
      l = (Link)linkList.elementAt(posl);
      t = l.getTail();
      h = l.getHead();
      t = co.getNodeList().getNode(t.getName());
      h = co.getNodeList().getNode(h.getName());
      co.createLink(t,h,l.getDirected());
    }
    Relation rel;
    Vector rList = new Vector();
    
    for (int posr=0 ; posr<getRelationList().size() ; posr++) {
        rel = (Relation)this.getRelationList().elementAt(posr);
        rel = rel.copy();
        rList.addElement(rel);
    }
    co.setRelationList(rList);
    return co;
  }
  catch (InvalidEditException iee){
    return null;
  }

}

    

 private static long getCard(Vector<Node> nodes){
	long card = 1;
	for(Node n : nodes){
		switch(n.getTypeOfVariable()){
		case Node.FINITE_STATES :
			card *= ((FiniteStates)n).getNumStates();
			break;
		case Node.CONTINUOUS :
			System.out.println("WARNING: Continous node encountered in getCard, we default to the value 2 but this may not be what you want!!");
			card *= 2;
			break;
		case Node.INFINITE_DISCRETE :
			System.out.println("WARNING: Node of type INTINITE_DISCRETE encountered in getCard - we ignore this node in this computation!");
			break;
		case Node.MIXED :
			System.out.println("WARNING: Node of type MIXED encountered in getCard - we ignore this node in this computation!");
			break;
		default :
			System.out.println("ERROR : a node of unknown type in getCard!");
			System.exit(112);
			break;
		}
	}
	return card;
 }
 
  public long getNumberOfFreeParameters(){
	  long size = 0;
	  for(Node n : this.nodeList.getNodes()){
		  Vector<Node> parents = n.getParentNodes().getNodes();
		  switch(n.getTypeOfVariable()){
		  case Node.FINITE_STATES : 
			  FiniteStates fn = (FiniteStates)n;
			  size += (fn.getNumStates() - 1)*getCard(parents);
			  break;
		  case Node.CONTINUOUS :
				System.out.println("WARNING: Continous node encountered in getNumberOfFreeParameters, we default to the value 2 but this may not be what you want!!");
				size += getCard(parents);
			  break;
		  case Node.INFINITE_DISCRETE :
			  System.out.println("WARNING: Node of type INTINITE_DISCRETE encountered in getNumberOfFreeParameters - we ignore this node in this computation!");
			  break;
		  case Node.MIXED :
			  System.out.println("WARNING: Node of type MIXED encountered in getNumberOfFreeParameters - we ignore this node in this computation!");
			  break;
		  default :
			  System.out.println("ERROR : a node of unknown type in getNumberOfFreeParameters!");
		  	  System.exit(112);
			  break;
		  }
	  }
	  return size;
  }  
  
  /**
   * Computes the probability of a full configuration.
 * @param conf - the configuration
 * @return the probability
 */
public double evaluateFullConfiguration(Configuration conf){
	  double p = 1.0;
	  for (int i = 0; i < nodeList.size(); i++) {
          Node var = nodeList.elementAt(i);
          Relation rel = getRelation(var);
          Potential pot = rel.getValues();
          //System.out.print("Bnet : from potential of "+var.getName()+" => "+pot.getValue(conf)+"\n");
          p *= pot.getValue(conf);
	  }
	  return p;
  }
    
} // End of class:1
