/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cytoscape.bayelviraapp.internal;

import java.io.File;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import com.csvreader.CsvReader;
import elvira.Bnet;
import elvira.CaseListMem;
import elvira.Configuration;
import elvira.FiniteStates;
import elvira.InvalidEditException;
import elvira.Link;
import elvira.Node;
import elvira.NodeList;
import elvira.database.DataBaseCases;
import elvira.gui.explication.macroExplanation;
import elvira.learning.BDeMetrics;
import elvira.learning.BICMetrics;
import elvira.learning.K2Metrics;
import elvira.learning.Metrics;
import elvira.learning.classification.ConfusionMatrix;
import elvira.learning.classification.supervised.discrete.CMutInfKDB;
import elvira.learning.classification.supervised.discrete.CMutInfTAN;
import elvira.learning.classification.supervised.discrete.DiscreteClassifier;
import elvira.learning.classification.supervised.discrete.Naive_Bayes;
import elvira.learning.classification.supervised.discrete.WrapperSelectiveNaiveBayes;
import elvira.learning.classification.supervised.discrete.WrapperSemiNaiveBayes;
import elvira.learning.classification.supervised.discrete.ClassTreeNaive;
import elvira.learning.classification.supervised.mixed.Gaussian_Naive_Bayes;
import elvira.learning.classification.supervised.mixed.Selective_GNB;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.work.util.ListSingleSelection;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import weka.core.Instance;

/**
 *
 * @author Victor Potenciano
 */
public class ImportBDTask extends AbstractNetworkViewTask {
    
    
    // Tunable variables
    @Tunable(description="Database file (.arff, .csv, .dbc or .elv)", groups={"Input data"}, params="fileCategory=table;input=true"/*, tooltip="Arff or CSV file to import"*/)
    public File data;
    
    @Tunable(description="Database name", groups={"Input data"})
    public String name;
    
    @Tunable(description="Bayes net type", groups={"Input data"})
    public ListSingleSelection<String> algorithms;
    
    @Tunable(description="Class attribute name (default: last attribute)", groups={"Input data"})
    public String classAtt;
    
    @Tunable(description="Export calculated bayes net in .elv format", groups={"Output"})
    public boolean export = false;
    
    @Tunable(description="Output file", groups={"Output"}, params="fileCategory=unspecified", dependsOn="export=true")
    public File outBnet;
    
    CyLayoutAlgorithm layout;
    CyServiceRegistrar serviceRegistrarRef;
    
    // Constants
    protected static final String ALG_NB = "Naive Bayes (discrete)";
    protected static final String ALG_SEMINB = "Semi Naive Bayes (discrete)";
    protected static final String ALG_KDB = "KDB (discrete)";
    protected static final String ALG_TAN = "TAN (discrete)";
    protected static final String ALG_SELNB = "Selective Naive Bayes (discrete)";
    protected static final String ALG_CLTREE = "Class Tree Naive (discrete)";
    protected static final String ALG_GAUS_NB = "Gaussian Naive Bayes (mixed)";
    protected static final String ALG_SEL_GNB = "Selective Gaussian Naive Bayes (mixed)";
    protected static final String [] ALGORITHMS = {ALG_NB, ALG_SEMINB, ALG_KDB, ALG_TAN, 
        ALG_SELNB, ALG_CLTREE, ALG_GAUS_NB, ALG_SEL_GNB};
    
    public static final String CSV_EXT = "csv";
    public static final String ARFF_EXT = "arff";
    public static final String DBC_EXT = "dbc";
    public static final String ELV_EXT = "elv";
    
    public static final String ATT_NODE_TYPE = "node_type";
    public static final String ATT_NODE_LABEL = "name";
    public static final String ATT_EDGE_DIST = "distance";
    public static final String ATT_EDGE_COMPARE = "compare";
    
    public static final HashMap<String,Color> ATT_COMPARE_MAP = new HashMap<String,Color>();
    public static final HashMap<String,NodeShape> ATT_NODE_TYPE_MAP = new HashMap<String,NodeShape>();
    
    static {
        ATT_COMPARE_MAP.put("greater", Color.RED);
        ATT_COMPARE_MAP.put("less", Color.BLUE);
        ATT_COMPARE_MAP.put("equals", new Color(204,102,255));//violet
        ATT_COMPARE_MAP.put("noncomparable", Color.BLACK);
        
        ATT_NODE_TYPE_MAP.put("attribute", NodeShapeVisualProperty.ROUND_RECTANGLE);
        ATT_NODE_TYPE_MAP.put("class", NodeShapeVisualProperty.ELLIPSE);
    }
    
    private static final String DEFAULT_LAYOUT = "force-directed";
    private static final double EDGE_WIDE_MAX = 4.0;
    private static final double TRAIN_TEST_RATE = 0.7;
    
    DiscreteClassifier classif = null;

    /**
     * 
     * @param nv
     * @param layout 
     */
    public ImportBDTask(CyServiceRegistrar serviceRef, CyNetworkView nv) {
        super(nv);
        
        this.serviceRegistrarRef = serviceRef;
        
        final ArrayList<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(ALGORITHMS));
                
        algorithms = new ListSingleSelection<String>(list);
        algorithms.setSelectedValue(ALG_NB);
        
        CyLayoutAlgorithmManager cyLayoutAlgorithmManagerRef = this.serviceRegistrarRef.getService(CyLayoutAlgorithmManager.class);
        this.layout = cyLayoutAlgorithmManagerRef.getLayout(DEFAULT_LAYOUT);
    }
    
    
    /**
     * creates a classifier according selAlg (selected algorythm) variable
     * 
     * @param selAlg
     * @param dbc
     * @return
     * @throws Exception 
     */
    protected DiscreteClassifier createClassifier(String selAlg, DataBaseCases dbc) 
            throws Exception {
        
        if( selAlg.equals(ALG_NB) ){
            return new Naive_Bayes(dbc, true);
        }
        else if( selAlg.equals(ALG_SEMINB) ){
            return new WrapperSemiNaiveBayes(dbc, true);
        }
        else if( selAlg.equals(ALG_TAN) ){
            return new CMutInfTAN(dbc, true);
        }
        else if( selAlg.equals(ALG_KDB) ){
            int nparents = 5; 
            return new CMutInfKDB(dbc, true, nparents);
        }
        else if( selAlg.equals(ALG_SELNB) ){
            return new WrapperSelectiveNaiveBayes(dbc, true);
        }
        else if( selAlg.equals(ALG_CLTREE) ){
            return new ClassTreeNaive(dbc);
        }
        else if( selAlg.equals(ALG_GAUS_NB) ){
            return new Gaussian_Naive_Bayes(dbc, true, getClassIndex(dbc));
        }
        else if( selAlg.equals(ALG_SEL_GNB) ){
            return new Selective_GNB(dbc, true, getClassIndex(dbc));
        }
        
        return null;
    }
        
    
    @Override
    public void run(TaskMonitor tm) throws Exception {
        
        if(this.view == null) {
            return;
        }
        
        tm.setTitle("Create bayesian network.");
        Logger userMessagesLogger = LoggerFactory.getLogger("CyUserMessages");
        
        // load database cases
        DataBaseCases dbc = null;
        Bnet bnet = null;
        
        if( data.getName().toLowerCase().endsWith(CSV_EXT) ){
            dbc = parseCSV();
        }
        else if( data.getName().toLowerCase().endsWith(ARFF_EXT) ){
            dbc = parseArff();
        }
        else if( data.getName().toLowerCase().endsWith(DBC_EXT) ){
            dbc = new DataBaseCases(new FileInputStream(this.data));
        }
        else if( data.getName().toLowerCase().endsWith(ELV_EXT) ){
            bnet = new Bnet(data.getAbsolutePath());
        }
        
        if( dbc!=null ) {
            // divide dataset into train and test sets
            DataBaseCases trainSet = new DataBaseCases();
            DataBaseCases testSet = new DataBaseCases();
            dbc.divideIntoTrainAndTest(trainSet, testSet, TRAIN_TEST_RATE);
            
            //select algorithm
            String selAlg = algorithms.getSelectedValue();
            this.classif = createClassifier(selAlg, trainSet);

            if( this.classif==null ) {
                String msg = "Cannot determine network type.";
                tm.setStatusMessage(msg);
                userMessagesLogger.error(msg);
                return;
            }

            // train classifier
            if( !setClassVar() ){
                String msg = "Class attribute not found.";
                tm.setStatusMessage(msg);
                userMessagesLogger.error(msg);
                return;
            }

            tm.setProgress(0.1);
            tm.setStatusMessage("Performing training and test.");
            this.classif.train();
            this.classif.test(testSet);
            
            //build network
            tm.setProgress(0.7);
            tm.setStatusMessage("Creating network.");
            createNetwork(null, this.classif, this.view.getModel());

            // calc metrics and store it in network table
            tm.setProgress(0.9);
            tm.setStatusMessage("Calculating metrics.");
            calcMetrics(this.classif, trainSet, this.view.getModel());
        }
        else if( bnet!=null ) {
            tm.setProgress(0.7);
            tm.setStatusMessage("Creating network.");
            createNetwork(bnet, null, this.view.getModel());
        }
        else {
            String msg = "Bad table file format.";
            tm.setStatusMessage(msg);
            userMessagesLogger.error(msg);
            return;
        }
        
        if(export){
            //export calculated bayes net
            exportBnet( (dbc!=null) ? this.classif.getClassifier() : bnet );
        }
        
        // Apply layout
        if( this.layout!=null ){
            insertTasksAfterCurrentTask(
                this.layout.createTaskIterator(
                    view, this.layout.createLayoutContext() , new HashSet(this.view.getNodeViews()), null
                )
            );
        }
        
        // add visual style task
        insertTasksAfterCurrentTask( new VisualStyleTask(serviceRegistrarRef, this.view) );
    }
    
    /**
     * 
     * @param bnet
     * @throws IOException 
     */
    protected void exportBnet(Bnet bnet) throws IOException {
        if( export && outBnet!=null ){
            if( !outBnet.getName().endsWith(ELV_EXT) ){
                outBnet = new File(outBnet.getAbsolutePath()+'.'+ELV_EXT);
            }
            
            bnet.saveBnet(new FileWriter(outBnet));
        }
    }
    
    /**
     * 
     * @return
     * @throws IOException
     * @throws InvalidEditException 
     */
    protected DataBaseCases parseCSV() throws IOException, InvalidEditException {
        BufferedReader freader = new BufferedReader(new FileReader(this.data));
        CsvReader reader=new CsvReader(freader);

        reader.readHeaders();
        String [] headers = reader.getHeaders();
        
        // scan variables
        HashMap<Integer,HashSet<Integer>> varStates = new HashMap<Integer,HashSet<Integer>>();
        
        while (reader.readRecord() ) {
            for(int j=0; j<headers.length; j++) {
                if( varStates.get(j)==null ){
                    varStates.put(j, new HashSet<Integer>());
                }
                
                Integer val = Integer.parseInt(reader.get(j));
                varStates.get(j).add(val);
            }
        }
        
        reader.close();
        
        // count variable states
        Vector varList = new Vector();
        
        for (int j = 0; j < headers.length; j++) {
            FiniteStates var = new FiniteStates( varStates.get(j).size() );
            var.setName(headers[j]);
            varList.add(var);
        }
        
        // build the list of Cases
        CaseListMem caseList = new CaseListMem(varList);
        
        // read the file again
        freader = new BufferedReader(new FileReader(this.data));
        reader=new CsvReader(freader);
        reader.readHeaders();
        
        while (reader.readRecord() ) {
            Configuration conf = new Configuration(varList);
            
            for(int j=0; j<headers.length; j++) {
                Integer val = Integer.parseInt(reader.get(j));
                conf.setValue(j, val);
            }
            
            caseList.put(conf);
        }
        
        reader.close();
        
        NodeList vars = new NodeList(varList);
        return new DataBaseCases(this.name, vars, caseList);
    }
    
    
    /**
     * 
     * @return
     * @throws IOException
     * @throws InvalidEditException 
     */
    protected DataBaseCases parseArff() throws IOException, InvalidEditException {
        return parseArff(new FileInputStream(this.data), this.name);
    }
    
    /**
     * 
     * @param stream
     * @param name
     * @return
     * @throws IOException
     * @throws InvalidEditException 
     */
    static protected DataBaseCases parseArff(InputStream stream, String name) throws IOException, InvalidEditException {
        ArffLoader loader = new ArffLoader();
        loader.setSource(stream);
                
        Instances instances = loader.getDataSet();
        
        // Variables
        Vector varList = new Vector();
        
        for (int j = 0; j < instances.numAttributes(); j++) {
            FiniteStates var = new FiniteStates( instances.attribute(j).numValues() );
            var.setName( instances.attribute(j).name() );
            varList.add(var);
        }
        
        // List of Cases
        CaseListMem caseList = new CaseListMem(varList);
        
        for(int i=0; i<instances.numInstances(); i++){
            Configuration conf = new Configuration(varList);
            Instance instance = instances.instance(i);
            
            for (int j=0; j < instances.numAttributes(); j++){
                conf.setValue(j, (int)instance.value(j));
            }
            
            caseList.put(conf);
        }
        
        
        NodeList vars = new NodeList(varList);
        return new DataBaseCases(name, vars, caseList);
    }

    
    /**
     * 
     * @param bnet
     * @param classif
     * @param network 
     */
    static protected void createNetwork(Bnet bnet, DiscreteClassifier classif, CyNetwork network) {
        Logger userMessagesLogger = LoggerFactory.getLogger("CyUserMessages");
        
        String name = "";
        
        if( bnet==null ){
            bnet = classif.getClassifier();
            name = classif.getDataBaseCases().getName();
        }
        else{
            name = bnet.getName();
        }
        
        //set network name
        network.getRow(network).set(CyNetwork.NAME, name);
        
        //add nodes
        TreeMap<String,CyNode> nodeMap = new TreeMap<String,CyNode>();
        
        //add node type attribute column
        network.getDefaultNodeTable().createColumn(ATT_NODE_TYPE, String.class, true);
        
        for( Node node : bnet.getNodeList().getNodes() ) {
            CyNode cynode = network.addNode();
            network.getRow(cynode).set(CyNetwork.NAME, node.getName());
            nodeMap.put(node.getName(), cynode);
            
            // set node type attribute
            network.getDefaultNodeTable().getRow(cynode.getSUID()).set(ATT_NODE_TYPE,
                    getNodeType(node,classif) );
        }
        
        // add edge attributes columns
        network.getDefaultEdgeTable().createColumn(ATT_EDGE_DIST, Double.class, true);
        network.getDefaultEdgeTable().createColumn(ATT_EDGE_COMPARE, String.class, true);
        
        //add links
        int totalNodes = bnet.getNodeList().size();
        userMessagesLogger.debug("Total nodes = {}",totalNodes);
        
        for( Link link : (Vector<Link>)bnet.getLinkList().getLinks() ) {
                
            CyEdge edge = network.addEdge( 
                    nodeMap.get(link.getHead().getName()),
                    nodeMap.get(link.getTail().getName()), 
                    true );

            // set attributes for this edge
            calcLinkStyles(bnet, link, edge, network);
        }
        
    }

    /**
     * 
     * @param node
     * @param classif
     * @return 
     */
    static protected String getNodeType(Node node, DiscreteClassifier classif) {
        if( classif==null ){
            return "attribute";
        }
        
        if( node.getName().equals(classif.getClassVar().getName()) ){
            return "class";
        }
        
        return "attribute";
    }
    
    /**
     * 
     * @param classif
     * @param link
     * @param edge
     * @param network 
     */
    static protected void calcLinkStyles(Bnet bnet, Link link, CyEdge edge, CyNetwork network) {
        String arcTypeComp = "noncomparable";
        double wide = 0.2;
        boolean continuous = false;

        if( (link.getHead() instanceof elvira.Continuous) || 
                (link.getTail() instanceof elvira.Continuous)  ){
            continuous = true;
        }
        
        if ( !continuous && isIncomingToSVNode(link)==false ) {
            double[][][] dist=macroExplanation.greaterdist(bnet, link.getHead(), link.getTail());
            int res=macroExplanation.compare(dist);

            switch (res){
                case 0:
                    arcTypeComp = "greater"; //rojo (greater)
                    break;
                case 1:
                    arcTypeComp = "less"; //azul (less)
                    break;
                case 3:
                    arcTypeComp = "equals"; //violet (equals)
                    break;
            }

            if (res==0 || res==1){
                wide = macroExplanation.influence(dist);
            }
        }
        
        // set dist and compare attribute for this edge
        network.getDefaultEdgeTable().getRow(edge.getSUID()).set(ATT_EDGE_DIST, wide*EDGE_WIDE_MAX);
        network.getDefaultEdgeTable().getRow(edge.getSUID()).set(ATT_EDGE_COMPARE, arcTypeComp);
        
    }
    
    /**
     * 
     * @param link
     * @return 
     */
    static private boolean isIncomingToSVNode(Link link) {
        return (link.getHead().getKindOfNode()==Node.SUPER_VALUE);
    }
    
    /**
     * calculates metrics and stores them in network table
     * 
     * @param classif
     * @param dbc 
     */
    static public void calcMetrics(DiscreteClassifier classif, DataBaseCases dbc, CyNetwork network) {
        
        if( !hasContinuous(dbc) ){
            Metrics met;

            met = (Metrics) new BICMetrics(dbc);
            network.getDefaultNetworkTable().createColumn("BIC score", Double.class, true);
            network.getRow(network).set("BIC score", met.score( classif.getClassifier()) );

            met = (Metrics) new K2Metrics(dbc);
            network.getDefaultNetworkTable().createColumn("K2 score", Double.class, true);
            network.getRow(network).set("K2 score", met.score( classif.getClassifier()) );

            met = (Metrics) new BDeMetrics(dbc);
            network.getDefaultNetworkTable().createColumn("BDe score", Double.class, true);
            network.getRow(network).set("BDe score", met.score( classif.getClassifier()) );
        }
        
        /// add confusion matrix metrics
        ConfusionMatrix confMatrix = classif.getConfusionMatrix();
        
        network.getDefaultNetworkTable().createColumn("Accuracy", Double.class, true);
        network.getRow(network).set("Accuracy", confMatrix.getAccuracy() );
        
        network.getDefaultNetworkTable().createColumn("#Cases", Integer.class, true);
        network.getRow(network).set("#Cases", confMatrix.getCases() );
        
        network.getDefaultNetworkTable().createColumn("Error", Double.class, true);
        network.getRow(network).set("Error", confMatrix.getError() );
        
        // add confusion matrix
        for(int i=0; i<confMatrix.getDimension(); i++){
           for(int j=0; j<confMatrix.getDimension(); j++){
                int val = (int)confMatrix.getValue(i, j);
                String column = i+"_classif_as_"+j;
                
                network.getDefaultNetworkTable().createColumn(column, Integer.class, true);
                network.getRow(network).set(column, val);
            } 
        }
        
        //calculates specificity and sensitivity
        if( confMatrix.getDimension()==2 ){
            // sensitivity = NTP/(NTP+NFN)
            double sens = confMatrix.getValue(1,1)/(confMatrix.getValue(1,1)+confMatrix.getValue(1,0));
            network.getDefaultNetworkTable().createColumn("Sensitivity", Double.class, true);
            network.getRow(network).set("Sensitivity", sens );
            
            // specificity = NTN/(NTN+NFP)
            double spec = confMatrix.getValue(0,0)/(confMatrix.getValue(0,0)+confMatrix.getValue(0,1));
            network.getDefaultNetworkTable().createColumn("Specificity", Double.class, true);
            network.getRow(network).set("Specificity", spec );
        }
    }

    /**
     * 
     */
    private boolean setClassVar() {
        FiniteStates var = null;
        
        if( !classAtt.isEmpty() ) {
            for(Node node : this.classif.getDataBaseCases().getNodeList().getNodes() ){
                if( node.getName().equalsIgnoreCase(this.classAtt) ) {
                    var = (FiniteStates) node;
                    break;
                }
            }
        }
        else{
            var = (FiniteStates) this.classif.getDataBaseCases().getNodeList().lastElement();
        }
        
        if( var==null ){
            return false;
        }
        
        this.classif.setClassVar(var);
        return true;
    }
    
    /**
     * 
     * @param dbc
     * @return 
     */
    private int getClassIndex(DataBaseCases dbc) {
        int index = dbc.getNodeList().size()-1;
        
        if( !classAtt.isEmpty() ) {
            int cont = 0;
            for(Node node : dbc.getNodeList().getNodes() ){
                if( node.getName().equalsIgnoreCase(this.classAtt) ) {
                    index = cont;
                    break;
                }
                
                cont++;
            }
        }
        
        return index;
    }
    
    /**
     * checks whether the given dbc has continous attributes
     * 
     * @param dbc
     * @return 
     */
    static private boolean hasContinuous(DataBaseCases dbc){
        for( Node node : dbc.getNodeList().getNodes() ){
            if( node instanceof elvira.Continuous ){
               return true;
            }
        }
        
        return false;
    }
    
}
