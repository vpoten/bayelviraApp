/* DataBaseCases.java */

package elvira.database;

import elvira.*;
import elvira.tools.*;
import elvira.parser.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import elvira.potential.*;
import elvira.learning.*;
import elvira.tools.statistics.analysis.MathMAE;
import elvira.tools.statistics.analysis.Stat;
import java.lang.Math;
import java.util.regex.*;
import java.util.Random;

/**
 * This class implements a data base of cases of the variables in a Bayesian
 * network.
 *
 * Created: Thu May 6 19:57:19 1999
 *
 * @since 25/4/2008
 * @author Proyecto Elvira
 * @version 1.0
 */

public class DataBaseCases extends Bnet implements ConditionalIndependence,
        Serializable {
    
        /*
         * This field especifies the version number of this class. This field
         * changes when the class is modified. So, this version number is saved with
         * the serialized object and it is cheked when this object is read.
         */
    static final long serialVersionUID = 954144080920651171L;
    
    /**
     * Number of cases in the data base.
     */
    int numberOfCases;
    
    /**
     * Generates a random database from a Bnet file, and the selected number of
     * cases
     */
    
    public static void main(String[] args) throws ParseException,
            FileNotFoundException, IOException {
        
                /*
                 * if (args.length < 4) { System.out.println("too few arguments, the
                 * arguments are: file.elv foutput.dbc number-of-cases true|false");
                 * System.exit(0); }
                 *
                 *
                 * FileInputStream f = new FileInputStream(args[0]); Bnet net = new
                 * Bnet(f); FileWriter f3 = new FileWriter(args[1]); DataBaseCases
                 * dataBase = new DataBaseCases(net,f3,
                 * Integer.valueOf(args[2]).intValue(),
                 * Boolean.valueOf(args[3]).booleanValue()); f3.close();
                 */
        
        FileInputStream f = new FileInputStream(args[0]);
        DataBaseCases dataBase = new DataBaseCases(f);
        
        DataBaseCases tr = new DataBaseCases();
        DataBaseCases ts = new DataBaseCases();
        
        dataBase.divideIntoTrainAndTest(tr, ts, 0.7);
        
        FileWriter ftr = new FileWriter("train.dbc");
        FileWriter fts = new FileWriter("test.dbc");
        
        tr.saveDataBase(ftr);
        ts.saveDataBase(fts);
        
                /*
                 * FiniteStates varClass=(FiniteStates)(dataBase.getNode("SampleType"));
                 * int nStatesClass=varClass.getNumStates(); System.out.println("N
                 * states class: "+nStatesClass); double[][][]
                 * predist=dataBase.getPreDistributionsNB(varClass); for(int i=0;i<predist.length;i++){
                 * System.out.println("VARIABLE " + i +
                 * ":"+dataBase.getNodeList().elementAt(i+1).getName());
                 * if(dataBase.getNodeList().elementAt(i+1) instanceof Continuous){
                 * Continuous node=(Continuous)dataBase.getNodeList().elementAt(i+1);
                 * for(int j=0; j<nStatesClass; j++){ System.out.println(" ESTADO "+j+ "
                 * de variable clase"); System.out.println(" " + predist[i][j][0] + " " +
                 * predist[i][j][1] + " " + predist[i][j][2]); } } }
                 */
                /*
                 * double[]
                 * meanAndVariance=dataBase.averageAndVarianceEstimators(null,(Continuous)(dataBase.getNode("GENE1836X")));
                 * System.out.println("La media es:"+meanAndVariance[0]);
                 * System.out.println("La varianza es:"+meanAndVariance[1]);
                 * System.out.println("Numero elementos
                 * validos:"+(int)meanAndVariance[2]);
                 */
                /*
                 * String S1=new String("e:\\andres\\elvira\\datasets\\Kent Ridge
                 * Bio-medical Data Set Repository\\primarydata\\Breastcancer_total.x");
                 * try{ DataBaseCases dbc=DataBaseCases.readDBC(S1); dbc.setMinMax();
                 * }catch(Exception e){};
                 */
        
                /*
                 * Date date = new Date(); double time = (double) date.getTime();
                 *
                 * try{ String s= new
                 * String("g:\\andres\\elvira\\esqaru\\dbcs\\wgaT.dbc"); DataBaseCases
                 * dbc=new DataBaseCases(new FileInputStream(s));
                 * dbc.writeDBC("g:\\andres\\elvira\\esqaru\\dbcs\\wgaT.x");
                 * }catch(Exception e){};
                 */
                /*
                 *
                 * try { String s= new String("c:\\andres\\elvira\\wright\\WGA.dbc");
                 * DataBaseCases dbc=new DataBaseCases(s);
                 *
                 * date=new Date(); time = (((double) date.getTime()) - time)/1000;
                 * System.out.println("Tiempo consumido: "+time);
                 *
                 *
                 * File objectFile = new File("c:\\andres\\elvira\\wright\\WGA.x"); if (
                 * objectFile.exists() ) { objectFile.delete(); } FileOutputStream fos =
                 * new FileOutputStream( objectFile); ObjectOutputStream oos = new
                 * ObjectOutputStream( fos ); oos.writeObject( dbc ); oos.close();
                 *
                 * date=new Date(); time = (double) date.getTime();
                 *
                 * FileInputStream fis = new FileInputStream( objectFile );
                 * ObjectInputStream ois = new ObjectInputStream( fis ); DataBaseCases
                 * retrieved = (DataBaseCases) ois.readObject(); ois.close();
                 * System.out.println( retrieved );
                 *
                 *
                 * date=new Date(); time = (((double) date.getTime()) - time)/1000;
                 * System.out.println("Tiempo consumido: "+time);
                 *  } catch ( OptionalDataException x ) { System.out.println( x );
                 * x.printStackTrace(); } catch ( ClassNotFoundException x ) {
                 * System.out.println( x ); x.printStackTrace(); } catch ( IOException x ) {
                 * System.out.println( x ); x.printStackTrace(); }
                 */
    }
    
    /**
     * Constructor for a data base from a data base file .dbc
     *
     * @param f
     *            the complete file name.
     */
    
    public DataBaseCases(String file) throws IOException, ParseException,
            FileNotFoundException {
        
        FileInputStream f = new FileInputStream(file);
        BufferedInputStream bf = new BufferedInputStream(f, 10000);
        DataBaseParse parser = new DataBaseParse(bf);
        parser.initialize();
        parser.CompilationUnit();
        translate(parser);
        f.close();
        if(this.numberOfCases != this.getCases().getNumberOfCases()){
        	System.out.println("Warning: the number of cases ("+numberOfCases+") field read from the file '"+file+"' does not agree with the actual number of cases found ("+this.getCases().getNumberOfCases()+") - updating the field in DataBaseCases!");
        	this.numberOfCases = this.getCases().getNumberOfCases();
        }
    }
    
    /**
     * Constructor for a data base from a data base file .dbc The second
     * argument shows if the data base must be reorganised, so the variables in
     * the database be randomly reordered
     *
     * @param f
     *            the complete file name.
     * @param reordered
     *            reorganization or not
     */
    
    public DataBaseCases(FileInputStream f, boolean reordered)
    throws IOException, ParseException, FileNotFoundException {
        CaseList cases, casesNew;
        Configuration confNew;
        Configuration confOld;
        int i, j, k;
        String nodeName;
        Relation relation;
        Vector relations;
        
        // Anyway, read the database
        DataBaseParse parser = new DataBaseParse(f);
        parser.initialize();
        parser.CompilationUnit();
        translate(parser);
        f.close();
        
        if (reordered == true) {
            NodeList vars = getVariables();
            
            // The nodelist will be reorganized so as the order of the
            // variables be changed
            NodeList reorderedNodeList = vars.randomOrder();
            
            // Consider now case by case and change them according to
            // this order for the variables
            cases = getCases();
            casesNew = new CaseListMem(reorderedNodeList);
            casesNew.setVariables(reorderedNodeList.getNodes());
            for (i = 0; i < numberOfCases; i++) {
                confOld = cases.get(i);
                confNew = new Configuration(reorderedNodeList);
                for (j = 0; j < reorderedNodeList.size(); j++) {
                    nodeName = vars.elementAt(j).getName();
                    k = confOld.getValue(nodeName);
                    confNew.putValue(nodeName, k);
                }
                casesNew.put(confNew);
            }
            
            // Set the reordered vars to the database
            setNodeList(reorderedNodeList);
            relation = new Relation();
            relation.setVariables(reorderedNodeList);
            relation.setValues(casesNew);
            relations = new Vector();
            relations.addElement(relation);
            setRelationList(relations);
        }
    }
    
    /**
     * Constructor for a data base from a data base file .dbc
     *
     * @param f
     *            the data base file.
     */
    
    public DataBaseCases(FileInputStream f) throws IOException, ParseException,
            FileNotFoundException {
        
        DataBaseParse parser = new DataBaseParse(f);
        parser.initialize();
        parser.CompilationUnit();
        translate(parser);
        f.close();
        
    }
    
    /**
     * Creates a new empty <code>DataBaseCases</code> object.
     */
    
    public DataBaseCases() {
        numberOfCases = 0;
    }
    
    /**
     * This constructor carries out a logic samplic of a <code>Bnet</code> and
     * stores the obtained sample in a .dbc file.
     *
     * @param network
     *            the <code>Bnet</code> to sample.
     * @param f
     *            the file for storing the data base.
     * @param n
     *            the number of cases in the data base.
     * @param memory
     *            <code>true</code> if we want store the data base in main
     *            memory.
     */
    
    public DataBaseCases(Bnet network, FileWriter f, int n, boolean memory)
    throws IOException {
        
        ContinuousConfiguration conf, confAux;
        int i;
        boolean ok;
        RelationList samplingRelationList;
        Random generator = new Random();
        String fileName = network.getName() + "db.bin";
        CaseList cases;
        Relation relation;
        Vector relations;
        NodeList variables;
        PrintWriter fv = new PrintWriter(f);
        
        fv.print("// Data Base. Elvira Format\n\n");
        fv.print("data-base " + network.getName() + " {\n");
        fv.print("number-of-cases = " + n + ";\n\n\n");
        variables = network.topologicalOrder();
        setNodeList(variables);
        setLinkList(null);
        variables.save(fv);
        numberOfCases = n;
        fv.print("relation  {\n\n");
        fv.print("memory = " + memory + ";\n\n");
        
        if (memory)
            cases = (CaseList) new ContinuousCaseListMem(variables);
        else
            cases = (CaseList) new CaseListOutMem(variables, fileName);
        
        relation = new Relation();
        relation.setVariables(variables);
        relation.setValues(cases);
        relations = new Vector();
        relations.addElement(relation);
        setRelationList(relations);
        
        fv.print("cases = (\n");
        
        samplingRelationList = getOrderInSimulation(network);
        
        for (i = 0; i < n; i++) {
            conf = simulateConfiguration(samplingRelationList, generator);
            confAux = reorder(conf);
            confAux.save(fv, variables);
            fv.print("\n");
            cases.put(confAux);
        }
        fv.print(");\n\n");
        fv.print("}\n}");
        fv.close();
    }
    
    /**
     * Constructor for a data base from a CaseList
     *
     * @param name
     *            name for the data base
     * @param cases
     *            the CaseList with the variables and the cases
     */
    
    public DataBaseCases(String name, CaseList cases) {
        int i;
        
        // get the variables from the CaseList
        NodeList vars = new NodeList(cases.getVariables());
        
        // set Data Base properties
        this.setName(name);
        this.setTitle(name);
        this.setVersion((float) 1.0);
        
        // add the variables of the dbc
        this.setNodeList((NodeList) vars);
        
        // Build the Relation with the cases
        Relation newrelation = new Relation();
        
        // add the cases
        newrelation.setValues(cases);
        newrelation.setVariables(this.getVariables());
        
        // Add the relation with the cases
        this.addRelation(newrelation);
        
        // set the number of cases
        this.numberOfCases = cases.getNumberOfCases();
    }
    
    /**
     * Constructor for a data base from a CaseList and a NodeList
     *
     * @param name
     *            name for the data base
     * @param vars
     *            the NodeList with the variables
     * @param cases
     *            the CaseList with the cases
     */
    
    public DataBaseCases(String name, NodeList vars, CaseList cases)
    throws InvalidEditException {
        
        int i;
        
        // set Data Base properties
        this.setName(name);
        this.setTitle(name);
        this.setVersion((float) 1.0);
        
        // add the variables of the dbc
        this.setNodeList((NodeList) vars);
        
        // Build the Relation with the cases
        Relation newrelation = new Relation();
        
        // add the cases
        newrelation.setValues(cases);
        newrelation.setVariables(this.getVariables());
        
        // Add the relation with the cases
        this.addRelation(newrelation);
        
        // set the number of cases
        this.numberOfCases = cases.getNumberOfCases();
    }
    
    /**
     * Constructor a random data base from the number of variables and the
     * number of cases. Random values are between 0 and 1.
     *
     * @param name
     *            name for the data base
     * @param numVars
     *            number of variables
     * @param numCases
     *            number of cases
     */
    
    public DataBaseCases(String name, int numVars, int numCases) {
        
        int i, j;
        Vector varList = new Vector();
        FiniteStates var;
        
        // Variables
        
        for (i = 0; i < numVars; i++) {
            var = new FiniteStates(2);
            var.setName("Variable" + i);
            varList.insertElementAt(var, i);
        }
        
        NodeList vars = new NodeList(varList);
        
        // List of Cases
        
        CaseListMem caseList = new CaseListMem(varList);
        Vector cases = caseList.getCases();
        
        for (i = 0; i < numCases; i++) {
            cases.insertElementAt(new int[numVars], i);
            caseList.setCases(cases);
            for (j = 0; j < numVars; j++) {
                double valor = (int) (Math.random() * 2);
                caseList.setValue(i, j, valor);
            }
        }
        
        // Initialise the data base
        
        // set Data Base properties
        this.setName(name);
        this.setTitle(name);
        this.setVersion((float) 1.0);
        
        // add the variables of the dbc
        this.setNodeList((NodeList) vars);
        
        // Build the Relation with the cases
        Relation newrelation = new Relation();
        
        // add the cases
        newrelation.setValues(caseList);
        newrelation.setVariables(this.getVariables());
        
        // Add the relation with the cases
        this.addRelation(newrelation);
        
        // set the number of cases
        this.numberOfCases = caseList.getNumberOfCases();
        
    }
    
    /**
     * Replaces the cases in the database by a given list of new cases.
     *
     * @param newCases
     *            the list of new cases.
     */
    
    public void replaceCases(CaseListMem newCases) {
        
        Vector newRelationList = new Vector();
        
        // Build the Relation with the cases
        Relation newRelation = new Relation();
        
        // add the cases
        newRelation.setValues(newCases);
        newRelation.setVariables(this.getVariables());
        
        // Add the relation with the cases
        newRelationList.addElement(newRelation);
        
        this.setRelationList(newRelationList);
        
        // set the number of cases
        this.numberOfCases = newCases.getNumberOfCases();
    }
    
    /**
     * Performs the conditional entropy of the variable X conditioned to a set
     * of variables Y through the Dirichlet Method
     */
    
    public double conditionalEntropyDr(FiniteStates nodeX, NodeList nodesY) {
        NodeList nodes = this.getVariables();
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseListMem caselistmem = (CaseListMem) relation.getValues();
        int n = caselistmem.getNumberOfCases();
        int i, j;
        Configuration configXY;
        Configuration configY = new Configuration(nodesY);
        double n_yi;
        double valor[];
        double partialEntropy, entropy = 0;
        double maxEntropy = 100;
        double menor;
        double numMenor = 1;
        double aSumar;
        double totalASumar = 1;
        
        for (i = 0; i < configY.possibleValues(); i++) {
            configY.nextConfiguration();
            // System.out.println("Configuraci�n: "+i+": "+configY.toString());
            n_yi = caselistmem.getValue(configY);
            partialEntropy = 0;
            configXY = configY.duplicate();
            configXY.insert(nodeX, 0);
            valor = new double[nodeX.getNumStates()];
            
            menor = n_yi + 1;
            numMenor = 1;
            for (j = 0; j < nodeX.getNumStates(); j++) {
                configXY.putValue(nodeX, j);
                valor[j] = caselistmem.getCompatibleValue(configXY);
                if (valor[j] < menor) {
                    menor = valor[j];
                    numMenor = 1;
                } else if ((int) valor[j] == menor) {
                    numMenor++;
                }
            }
            aSumar = totalASumar / numMenor;
            for (j = 0; j < nodeX.getNumStates(); j++) {
                // System.out.println("Valor es: "+valor+" de
                // "+config.toString());
                if ((int) valor[j] == menor) {
                    valor[j] = valor[j] + aSumar;
                }
                if (valor[j] > 0) {
                    valor[j] = valor[j] / (n_yi + totalASumar);
                    partialEntropy = partialEntropy + valor[j]
                            * Math.log(valor[j]);
                }
                // System.out.println("Entropia es: "+entropy);
            }
            
            partialEntropy = -partialEntropy;
            if (partialEntropy < maxEntropy)
                maxEntropy = partialEntropy;
            entropy = entropy + n_yi * partialEntropy;
        }
        entropy = (entropy + maxEntropy) / (n + 1);
        // System.out.println("La entrop�a Condicionada de Dirichlet de
        // "+nodeX.getName()+" a una serie de variables es: "+entropy);
        
        return entropy;
        
    }
    
    /**
     * Performs the entropy of the variable X through the Dirichlet Method
     */
    
    public double entropyDr(FiniteStates nodeX) {
        NodeList nodes = this.getVariables();
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseListMem caselistmem = (CaseListMem) relation.getValues();
        int n = caselistmem.getNumberOfCases();
        int i;
        NodeList nodelist = new NodeList();
        nodelist.insertNode(nodeX);
        Configuration config_inic = new Configuration(nodelist);
        Configuration config = new Configuration(nodelist);
        int valoresPosibles = config_inic.possibleValues();
        double[] valor = new double[valoresPosibles];
        double menor = valoresPosibles + 1;
        double numMenor = 1;
        double entropy = 0;
        double aSumar;
        double totalASumar = 1;
        
        for (i = 0; i < valoresPosibles; i++) {
            config.nextConfiguration();
            valor[i] = caselistmem.getCompatibleValue(config);
            if (valor[i] < menor) {
                menor = valor[i];
                numMenor = 1;
            } else if ((int) valor[i] == menor) {
                numMenor++;
            }
        }
        
        aSumar = totalASumar / numMenor;
        for (i = 0; i < valoresPosibles; i++) {
            // System.out.println("Valor es: "+valor+" de "+config.toString());
            if ((int) valor[i] == menor) {
                valor[i] = valor[i] + aSumar;
            }
            if (valor[i] > 0) {
                valor[i] = valor[i] / (n + totalASumar);
                entropy = entropy + valor[i] * Math.log(valor[i]);
            }
            // System.out.println("Entropia es: "+entropy);
        }
        entropy = -entropy;
        // System.out.println("La entrop�a de Dirichlet de "+nodeX.getName()+"
        // es: "+entropy);
        
        return entropy;
        
    }
    
    /**
     * Performs the entropy of the variable X
     */
    
    public double entropy(FiniteStates nodeX) {
        NodeList nodes = this.getVariables();
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseListMem caselistmem = (CaseListMem) relation.getValues();
        int n = caselistmem.getNumberOfCases();
        int i;
        NodeList nodelist = new NodeList();
        nodelist.insertNode(nodeX);
        Configuration config_inic = new Configuration(nodelist);
        Configuration config = new Configuration(nodelist);
        double valor;
        double entropy = 0;
        
        for (i = 0; i < config_inic.possibleValues(); i++) {
            config.nextConfiguration();
            valor = caselistmem.getCompatibleValue(config);
            // System.out.println("Valor es: "+valor+" de "+config.toString());
            if (valor > 0) {
                valor = valor / n;
                entropy = entropy + valor * Math.log(valor);
            }
            // System.out.println("Entropia es: "+entropy);
        }
        
        entropy = -entropy;
        
        return entropy;
        
    }
    
    /**
     * Performs the conditional entropy of the variable X conditioned to a set
     * of variables Y
     */
    
    public double conditionalEntropy(FiniteStates nodeX, NodeList nodesY) {
        NodeList nodes = this.getVariables();
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseListMem caselistmem = (CaseListMem) relation.getValues();
        int n = caselistmem.getNumberOfCases();
        int i, j;
        Configuration configXY;
        Configuration configY = new Configuration(nodesY);
        double valor, n_yi;
        double partialentropy, entropy = 0;
        
        for (i = 0; i < configY.possibleValues(); i++) {
            configY.nextConfiguration();
            n_yi = caselistmem.getValue(configY);
            partialentropy = 0;
            configXY = configY.duplicate();
            configXY.insert(nodeX, 0);
            for (j = 0; j < nodeX.getNumStates(); j++) {
                configXY.putValue(nodeX, j);
                valor = caselistmem.getValue(configXY);
                if (valor > 0) {
                    valor = valor / n_yi;
                    partialentropy = partialentropy + valor * Math.log(valor);
                }
            }
            partialentropy = -partialentropy;
            entropy = entropy + n_yi * partialentropy;
        }
        entropy = entropy / n;
        return entropy;
        
    }
    
    /**
     * Removes a set of variables specified by their indexes from the database
     */
    
    public void removeVariables(Vector vectorVars) {
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseListMem caselistmem = (CaseListMem) relation.getValues();
        
        caselistmem.deleteVariables(vectorVars);
        relation.setVariables(caselistmem.getVariables());
        this.setNodeList(new NodeList(caselistmem.getVariables()));
    }
    
    /**
     * Performs the cross entropy of the variable X and the set of variables Y
     */
    
    public double crossEntropy(FiniteStates nodeX, NodeList nodesY) {
        return (entropy(nodeX) - conditionalEntropy(nodeX, nodesY));
    }
    
    /**
     * Performs the cross entropy of the variable X and the set of variables Y
     * conditioned to the set of variables Z
     */
    
    public double crossConditionedEntropy(FiniteStates nodeX, NodeList nodesY,
            NodeList nodesZ) {
        nodesY.join(nodesZ);
        return (conditionalEntropy(nodeX, nodesZ) - conditionalEntropy(nodeX,
                nodesY));
    }
    
    /**
     * Performs the cross entropy of the variable X and the set of variables Y
     */
    
    public double crossEntropyDr(FiniteStates nodeX, NodeList nodesY) {
        return (entropyDr(nodeX) - conditionalEntropyDr(nodeX, nodesY));
    }
    
    /**
     * Performs the cross entropy of the variable X and the set of variables Y
     * conditioned to the set of variables Z
     */
    
    public double crossConditionedEntropyDr(FiniteStates nodeX,
            NodeList nodesY, NodeList nodesZ) {
        nodesY.join(nodesZ);
        return (conditionalEntropyDr(nodeX, nodesZ) - conditionalEntropyDr(
                nodeX, nodesY));
    }
    
    public CaseListMem getCaseListMem() {
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        return (CaseListMem) relation.getValues();
    }
    
    /**
     * Returns the next case in a Configuration objet. The cases are obtained
     * sequentially and efficiently
     *
     * @return Configuration
     */
    public Configuration getNext() {
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseListMem clm = (CaseListMem) relation.getValues();
        
        return clm.getNext();
    }// getNext()
    
    /**
     * Returns true if there are more elemnets left to be sequentially obtained.
     */
    public boolean hasNext() {
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseListMem clm = (CaseListMem) relation.getValues();
        
        return clm.hasNext();
    }
    
    /**
     * Initialize the class CaseListMem in order to obtain the cases
     * sequentially and in an efficient way
     */
    public void initializeIterator() {
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseListMem clm = (CaseListMem) relation.getValues();
        
        clm.initializeIterator();
    }
    
    /**
     * This method stores a DataBaseCases object in a .dbc file.
     *
     * @param f
     *            the file for storing the data base.
     */
    
    public void saveDataBase(FileWriter f) throws IOException {
        int i, j;
        Bnet network = (Bnet) this;
        NodeList variables;
        PrintWriter fv = new PrintWriter(f);
        NodeList nodes = this.getVariables();
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseList caselistmem = (CaseList) relation.getValues();
        int n = caselistmem.getNumberOfCases();
        
        // Save variables
        fv.print("// Data Base. Elvira Format\n\n");
        fv.print("data-base " + network.getName() + " {\n");
        fv.print("number-of-cases = " + n + ";\n\n\n");
        variables = this.getNodeList();
        variables.save(fv);
        
        // Save cases
        fv.print("relation  {\n\n");
        fv.print("memory = true;\n\n");
        fv.print("cases = (\n");
        
        DecimalFormat format = (DecimalFormat) DecimalFormat
                .getInstance(Locale.ENGLISH);
        format.setGroupingUsed(false);
        Pattern pattern = Pattern.compile("(\\p{Digit}+)");
        
        // go through each case
        for (i = 0; i < caselistmem.getNumberOfCases(); i++) {
            fv.print("[ ");
            for (j = 0; j < nodes.size(); j++) {
                Node node = (Node) (caselistmem.getVariables()).elementAt(j);
                // Look for undef values
                if (node.undefValue() == caselistmem.getValue(i, j))
                    fv.print("?");
                else if (node.getTypeOfVariable() == Node.CONTINUOUS) {
                    double data = caselistmem.getValue(i, j);
                    fv.print(format.format(data));
                } else {
                    FiniteStates nodefn = (FiniteStates) (caselistmem
                            .getVariables()).elementAt(j);
                    fv.print(nodefn.getPrintableState((int) caselistmem
                            .getValue(i, j)));
                }
                
                if ((variables.elementAt(j)).getName() != node.getName())
                    System.out
                            .println("ERROR: different order. The cases variables order and net variables order don't match.Data Base is Corrupted !!!");
                
                if (j < nodes.size() - 1)
                    fv.print(", ");
            }
            fv.print(" ]\n");
        }
        fv.print(");\n\n");
        fv.print("}\n}");
        fv.close();
    }
    
    /**
     * This method translates the parse variables to instance variables.
     *
     * @param a
     *            parser the parser containing the data base.
     */
    
    public void translate(DataBaseParse parser) {
        
        setNodeList(parser.Nodes);
        setComment(parser.Comment);
        setTitle(parser.Title);
        setAuthor(parser.Author);
        setName(parser.Name);
        setRelationList(parser.Relations);
        setNumberOfCases(parser.casesNumber);
    }
    
    /**
     * This method sorts a configuration taking into account the order of the
     * variables.
     *
     * @param conf
     *            the configuration to be sorted.
     * @return the configuration sorted.
     */
    
    private ContinuousConfiguration reorder(ContinuousConfiguration conf) {
        
        ContinuousConfiguration confAux = new ContinuousConfiguration();
        int i, value;
        double value2;
        Node node;
        NodeList variables = getNodeList();
        
        for (i = 0; i < variables.size(); i++) {
            node = (Node) variables.elementAt(i);
            if (node.getClass() == FiniteStates.class) {
                value = conf.getValue((FiniteStates) node);
                confAux.insert((FiniteStates) node, value);
            } else if (node.getClass() == Continuous.class
                    && node.getTypeOfVariable() == Node.CHANCE) {
                value2 = conf.getValue((Continuous) node);
                confAux.insert((Continuous) node, value2);
            }
            
        }
        return confAux;
    }
    
    /**
     * This method computes the initial order among the relations to be
     * simulated.
     *
     * @param net
     *            The network to be simulated.
     * @return the relations sorted to be similated.
     */
    
    private RelationList getOrderInSimulation(Bnet net) {
        
        NodeList variables = getNodeList(), varOfRelation;
        ContinuousConfiguration confAux1 = new ContinuousConfiguration(
                variables), conf = new ContinuousConfiguration();
        Vector relationList;
        RelationList relationListOrd = new RelationList();
        boolean inOrder = true;
        int i, j, pos;
        Relation relation;
        Node node;
        
        relationList = (Vector) (net.getRelationList()).clone();
        while (confAux1.size() != 0) {
            for (i = 0; i < relationList.size(); i++) {
                inOrder = true;
                relation = (Relation) relationList.elementAt(i);
                varOfRelation = relation.getVariables();
                for (j = 1; j < varOfRelation.size(); j++) {
                    node = (Node) varOfRelation.elementAt(j);
                    if (conf.indexOf(node) == -1) {
                        inOrder = false;
                        break;
                    }
                }
                if (inOrder) {
                    node = (Node) varOfRelation.elementAt(0);
                    if (node.getClass() == Continuous.class)
                        conf.insert((Continuous) node, -1);
                    else if (node.getClass() == FiniteStates.class)
                        conf.insert((FiniteStates) node, -1);
                    confAux1.remove(node);
                    relationListOrd.insertRelation(relation);
                    pos = relationList.indexOf(relation);
                    relationList.removeElementAt(pos);
                    break;
                }
            }
        }
        
        return relationListOrd;
    }
    
    /**
     * Simulates a configuration.
     *
     * @param generator
     *            a random number generator.
     * @return <code>true</code> if the simulation was ok.
     */
    
    private ContinuousConfiguration simulateConfiguration(
            RelationList samplingDistributions, Random generator) {
        
        Node variableX;
        Relation relation;
        NodeList variableList;
        int i, s, v;
        double v2;
        ContinuousConfiguration currentConf = new ContinuousConfiguration();
        
        s = samplingDistributions.size();
        
        for (i = 0; i < s; i++) {
            relation = (Relation) samplingDistributions.elementAt(i);
            variableList = relation.getVariables();
            variableX = (Node) variableList.elementAt(0);
            if (variableX.getClass() == FiniteStates.class) {
                v = simulateValue((FiniteStates) variableX, relation,
                        (Configuration) currentConf, generator);
                currentConf.insert((FiniteStates) variableX, v);
            } else if (variableX.getClass() == Continuous.class
                    && variableX.getTypeOfVariable() == Node.CHANCE) {
                v2 = simulateValue((Continuous) variableX, relation,
                        currentConf, generator);
                currentConf.insert((Continuous) variableX, v2);
            }
        }
        
        return currentConf;
    }
    
    /**
     * Simulates a value for a given variable.
     *
     * @param variableX
     *            a <code>FiniteStates</code> variable to be generated.
     * @param pos
     *            the position of <code>variableX</code> in the current
     *            configuration.
     * @param relation
     *            the conditional distribution of <code>variableX</code>.
     * @param generator
     *            a random number generator.
     * @param currentConf
     *            the configuration of the variables already simulated.
     * @return the value simulated. -1 if the valuation is 0.
     */
    
    private int simulateValue(FiniteStates variableX, Relation relation,
            Configuration currentConf, Random generator) {
        
        int i, j, nv, v = -1, pos;
        double checksum = 0.0, r, cum = 0.0, value;
        Configuration conf = new Configuration();
        PotentialTable potential = null;
        PotentialTree pot2;
        Potential pot;
        
        conf.insert(variableX, 0);
        nv = variableX.getNumStates();
        
        pot = relation.getValues();
        
        // First of all, restrict the potential to the configuration already
        // simulated
        pot = pot.restrictVariable(currentConf);
        
        // Now, convert the potential into a PotentialTable
        if (pot.getClass() == PotentialContinuousPT.class) {
            pot2 = new PotentialTree((PotentialContinuousPT) pot);
            potential = new PotentialTable(pot2);
        } else if (pot.getClass() == PotentialTree.class) {
            potential = new PotentialTable((PotentialTree) pot);
        } else if (pot.getClass() == PotentialTable.class) {
            potential = (PotentialTable) pot;
            // (PotentialTable)relation.getValues();
        } else {
            System.out
                    .println("Error in DataBaseCases.simulateValue(FiniteStates, Relation,"
                    + "Configuration, Random): class of pot is "
                    + pot.getClass());
            System.exit(1);
        }
        
        // Compute the normalisation value of the table
        for (i = 0; i < nv; i++) {
            conf.putValue(variableX, i);
            pos = conf.getIndexInTable();
            value = potential.getValue(pos);
            checksum += value;
        }
        
        if (checksum == 0.0) {
            System.out.println("Zero valuation");
            return -1;
        }
        
        // Now, simulate a value from the table
        r = generator.nextDouble();
        for (i = 0; i < nv; i++) {
            conf.putValue(variableX, i);
            pos = conf.getIndexInTable();
            value = potential.getValue(pos);
            cum += (value / checksum);
            if (r <= cum) {
                v = i;
                break;
            }
        }
        
        return v;
    }
    
    /**
     * Simulates a value for a given variable.
     *
     * @param variableX
     *            a <code>Continuous</code> variable to be generated.
     * @param relation
     *            the conditional distribution of <code>variableX</code>.
     * @param generator
     *            a random number generator.
     * @param currentConf
     *            the configuration of the variables already simulated.
     * @return the value simulated. -1 if the valuation is 0.
     */
    
    private double simulateValue(Continuous variableX, Relation relation,
            ContinuousConfiguration currentConf, Random generator) {
        
        double val = generator.nextGaussian(); // Normal distribution: mean 0,
        // dev. 1.
        PotentialContinuousPT pot;
        MixtExpDensity exp;
        
        pot = (PotentialContinuousPT) ((PotentialContinuousPT) relation
                .getValues()).restrictVariable((Configuration) currentConf,
                (Node) variableX);
        
        exp = pot.getTree().getProb();
        
        if (exp != null) {
            if (exp.getTerms().size() > 0) {
                // Check whether the density is a normal
                if (exp.getExponent(0).getClass() == QuadraticFunction.class) {
                    double dev = exp.getDesviation(0);
                    double mean = exp.getMean(0);
                    if (dev != -1.0) {
                        val *= dev;
                        val += mean;
                    } else {
                        System.out
                                .println("ERROR in DataBaseCases: simulateValue");
                        System.exit(0);
                    }
                } else
                    val = pot.simulateValue();
            } else
                val = pot.simulateValue();
        } else
            val = pot.simulateValue();
        
        return val;
    }
    
    /**
     * This method remove the cases that contain a missing value for an specific
     * variable
     *
     * @param pos represents the position of a variable in the list of 
     * variables
     *
     */
    
    public void removeCasesMissingValue(int pos){   
       
       for (int i=0;i<this.numberOfCases;i++){
          if (new Double(this.getCaseListMem().getValue(i,pos)).isNaN()){//missing
            this.getCaseListMem().getCases().removeElementAt(i);
            i--;   
            numberOfCases--;
            this.getCaseListMem().setNumberOfCases(numberOfCases);
          }
       }
    }
       
    
   /**
     * This method insert missing values (UNDEFVALUE) in the database randomly.
     *
     * @param percent is the percentage of missing values
     *
     */
    
    public void setMissingValues(double percent){

       int numcases = this.getNumberOfCases();
       int numvars = this.getNodeList().size();
       Node n = new Continuous();
       for (int i=0;i<numcases;i++)
          for (int j=0;j<numvars;j++)
             if (Math.random()<percent)
                this.getCaseListMem().setValue(i, j, n.undefValue());
     }
    
    /**
     * This method insert missing values in the data base, at random.
     *
     * @param numMissingCases
     *            number of cases with missing values
     * @param numMissingCells
     *            number of missing values for each case
     * @return a vector with real values and their position, respect the set of
     *         missing cases
     */
    
    public java.util.Vector generateMissing(int numMissingCases,
            int numMissingCells) {
        
        int i, j, k, m, n, p;
        int numCases, numVars, numMissing;
        int posCase, posCase2, posCell, posCell2;
        numCases = numberOfCases;
        numVars = this.getVariables().size();
        numMissing = numMissingCases * numMissingCells;
        posCase = 0;
        posCell = 0;
        
        double value, undefValue, currentValue;
        double[] values = new double[numMissing];
        Double posCaseDouble;
        Double posCaseDouble2;
        Double posCellDouble;
        Double posCellDouble2;
        
        boolean repetido;
        
        Vector randomPosCases = new Vector();
        Vector randomPosCells = new Vector();
        Vector pos, pos2;
        Vector realValues = new Vector();
        Vector realPosVal;
        
        CaseListMem caseList = (CaseListMem) this.getCases();
        CaseListMem missingCases = new CaseListMem();
        CaseListMem completeCases = new CaseListMem();
        
        FiniteStates var = new FiniteStates();
        undefValue = var.undefValue();
        
        // Generate random vector of cases's positions
        
        for (k = 0; k < numMissingCases; k++) {
            // to avoid repeated positions
            repetido = true;
            while (repetido) {
                repetido = false;
                posCase = (int) (Math.random() * numCases);
                for (m = 0; (m < k) && (!repetido) && (k != 0); m++) {
                    posCase2 = ((Double) randomPosCases.elementAt(m))
                    .intValue();
                    if (posCase == posCase2)
                        repetido = true;
                }
            }
            randomPosCases.insertElementAt(new Double(posCase), k);
        }// end for k
        
        // Sort random vector of cases's positions
        MTELearning l = new MTELearning();
        l.sort(randomPosCases);
        
        // Generate random vector of cells's positions for each missing case
        
        for (k = 0; k < numMissingCases; k++) {
            
            // random vector of cells's positions for the case
            // in the position: randomPosCases[k]
            pos = new Vector();
            for (n = 0; n < numMissingCells; n++) {
                // to avoid repeated positions
                repetido = true;
                while (repetido) {
                    repetido = false;
                    posCell = (int) (Math.random() * numVars);
                    for (m = 0; (m < n) && (!repetido) && (n != 0); m++) {
                        posCell2 = ((Double) pos.elementAt(m)).intValue();
                        if (posCell == posCell2)
                            repetido = true;
                    }
                }
                
                pos.insertElementAt(new Double(posCell), n);
                
            }// end for n
            
            // Sort random vector of cells's positions
            l.sort(pos);
            
            randomPosCells.insertElementAt(pos, k);
            
        }// end for k
        
        // For each random position of the missing values, insert missing value
        // and store real values
        
        m = 0;
        for (k = 0; k < numMissingCases; k++) {
            // position of the case
            i = ((Double) randomPosCases.elementAt(k)).intValue();
            pos = (Vector) randomPosCells.elementAt(k);
            for (n = 0; n < numMissingCells; n++) {
                // position of the cell
                j = ((Double) pos.elementAt(n)).intValue();
                // store real values
                value = caseList.getValue(i, j);
                values[m] = value;
                // insert missing value
                caseList.setValue(i, j, undefValue);
                m++;
            }
        }
        
        // store position of the missing values in the set of missing cases
        
        caseList.separateMissingValues(missingCases, completeCases);
        
        m = 0;
        for (i = 0; (i < numCases) && (m < numMissing); i++) {
            for (j = 0; (j < numVars) && (m < numMissing); j++) {
                currentValue = missingCases.getValue(i, j);
                if (currentValue == undefValue) {
                    value = values[m];
                    
                    // Store position of the missing value
                    pos2 = new Vector();
                    pos2.insertElementAt(new Integer(i), 0);
                    pos2.insertElementAt(new Integer(j), 1);
                    // Store real value and position
                    realPosVal = new Vector();
                    realPosVal.insertElementAt(pos2, 0);
                    realPosVal.insertElementAt(new Integer((int) value), 1);
                    
                    realValues.insertElementAt(realPosVal, m);
                    m++;
                }// end if
            }// end j
        }// end i
        
        this.replaceCases(caseList);
        
        return realValues;
        
    }
    
    /** ****** access methods ******************* */
    
    /**
     * Gets the number of cases in the data base.
     *
     * @return the number of cases in the data base.
     */
    
    public int getNumberOfCases() {
        
        return numberOfCases;
    }
    
    /**
     * Gets the variables in the data base.
     *
     * @return the variables in the data base.
     */
    
    public NodeList getVariables() {
        
        return getNodeList();
    }
    
    /**
     * Returns the reference to the variables (that is, nodes) in this database in
     * a newly created Vector<Node> object.
     * 
     * @return a Vector<Node> of the Nodes in this database.
     */
    public Vector<Node> getNewVectorOfNodes(){
    	Vector<Node> vNode = new Vector<Node>(this.nodeList.size());
    	for(int i=0;i<this.nodeList.size();i++){
    		vNode.insertElementAt(this.nodeList.elementAt(i), i);
    	}
    	return vNode;
    }
    
    /**
     * Gets the cases in the data base
     *
     * @return the cases in the data base.
     */
    
    public CaseList getCases() {
        // Get the cases
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        return (CaseList) relation.getValues();
    }
    
    /**
     * Gets the N first cases in the data base
     *
     * @param N
     *            number of cases to get
     * @return the first N cases in the data base
     */
    
    public CaseListMem getNFirstCases(int N) {
        
        CaseListMem caseList = (CaseListMem) this.getCases();
        
        CaseListMem newCaseList = new CaseListMem(caseList.getVariables());
        newCaseList.setCases(new Vector());
        
        for (int i = 0; i < N; i++) {
            Configuration currentCase = caseList.get(i);
            newCaseList.put(currentCase);
        }
        
        return newCaseList;
        
    }
    
    /**
     * Gets N random cases in the data base
     *
     * @param N
     *            number of cases to get
     * @return N cases in the data base
     */
    
    public CaseListMem getNRandomCases(int N) {
        Random r = new Random();
        return getNRandomCases(N, r);
    }
    
    /**
     * Gets N random cases in the data base
     *
     * @param N
     *            number of cases to get
     * @param r
     *            random generator
     * @return N cases in the data base
     */
    
    public CaseListMem getNRandomCases(int N, Random r) {
        
        CaseListMem caseList = (CaseListMem) this.getCases();
        
        CaseListMem newCaseList = new CaseListMem(caseList.getVariables());
        newCaseList.setCases(new Vector());
        
        // random array for the order of returned cases
        int m = getNumberOfCases();
        Vector list1 = new Vector();// vector for positions
        Vector list2 = new Vector();// vector for contents
        int order[] = new int[m]; // permutation
        
        // Initializate the vectors
        for (int i = 0; i < m; i++) {
            list1.add(new Integer(i));
            list2.add(new Integer(i));
        }
        
        // Generate a random permutation
        while (list1.size() > 0) {
            // get 2 random positions
            int pos1 = r.nextInt(list1.size());
            int pos2 = r.nextInt(list2.size());
            
            // Get the position and the content
            int pos = ((Integer) list1.elementAt(pos1)).intValue();
            int con = ((Integer) list2.elementAt(pos2)).intValue();
            
            // remove both elements from the vectors
            list1.removeElementAt(pos1);
            list2.removeElementAt(pos2);
            
            // add the element to the solution
            order[pos] = con;
        }
        
        // get the N first cases using the random permutation
        for (int i = 0; i < N; i++) {
            Configuration currentCase = caseList.get(order[i]);
            newCaseList.put(currentCase);
        }
        
        return newCaseList;
    }
    
    /**
     * This method sets the number of cases to be considered.
     *
     * @param numberOfCases
     *            the number of cases to consider.
     */
    
    public void setNumberOfCases(int numberOfCases) {
        
        //CaseList cases = (CaseList) ((Relation) getRelationList().elementAt(0)).getValues();
       // if (numberOfCases <= cases.getNumberOfCases())
            this.numberOfCases = numberOfCases;
    }
    
    /** ******************************************************************** */
    /* Completa las hojas con frecuencias 0.0 de un arbol de frecuencias */
    /** ******************************************************************** */
    
    public static void setProbTreeFull(ProbabilityTree tree) {
        
        ProbabilityTree treeAux = tree;
        
        if (treeAux.isEmpty()) {
            treeAux.assignProb(0.0);
            return;
        } else {
            if (treeAux.isVariable()) {
                FiniteStates node = treeAux.getVar();
                int nStates = node.getNumStates();
                // System.out.println("Numero de estados: "+nStates+" de la var:
                // "+node.getName());
                Vector child = treeAux.getChild();
                // System.out.println("Numero de hijos: "+child.size());
                for (int i = 0; i < nStates; i++) {
                    treeAux = (ProbabilityTree) child.elementAt(i);
                    setProbTreeFull(treeAux);
                }
            } else
                return;
        }
        return;
    }
    
    /**
     * This method returns a table with the absolute frequencies for a subset of
     * variables in the data base.
     *
     * @param vars
     *            a set of variables.
     * @return a <code>PotentialTable</code> with the frequencies.
     */
    
    public PotentialTable getPotentialTable(NodeList vars) {
        
        PotentialTable pot = new PotentialTable(vars);
        int pos;
        double increment = (double) (1.0);
        CaseList cases;
        int[] indexOfvars, values;
        double [] weights;
        int nv = vars.size(); // Number of variables.
        int nc = getNumberOfCases(); // Number of variables.
        values = new int[nv];
        indexOfvars = new int[nv];
        NodeList allVars=this.getVariables();
        allVars.getIndexOfVars(indexOfvars,vars);
        //all cases
        cases = (CaseList)((Relation)getRelationList().elementAt(0)).getValues();
        weights = vars.getWeights();

        for (int cas=0 ; cas < nc ; cas++) {
            if (!((CaseListMem)cases).getValues(cas,indexOfvars,values)) {
                pos = PotentialTable.getIndexInTable(values, weights);
                pot.incValue(pos, increment);
            }
        }
        return pot;
    }
    
    
    /**
     * This method returns a table with the absolute frequencies for a subset of
     * variables in the data base for those cases in the database compatible
     * which condition
     *
     * @param vars
     *            a set of variables.
     * @return a <code>PotentialTable</code> with the frequencies.
     */
    
public PotentialTable getPotentialTable(NodeList vars, Configuration condition) {
        
        Configuration conf;
        boolean missingValues;
        PotentialTable pot = new PotentialTable(vars);
        FiniteStates node;
        int i, nState, indexTable;
        double increment = (double) (1.0);
        CaseList cases;
        int[] indexOfvars;
        
        indexOfvars = new int[vars.size()];
        for (i = 0; i < vars.size(); i++) {
            int pos = getVariables().getId(vars.elementAt(i));
            indexOfvars[i] = pos;
        }
        
        cases = (CaseList) ((Relation) getRelationList().elementAt(0))
        .getValues();
        for (int cas = 0; cas < getNumberOfCases(); cas++) {
            conf = cases.get(cas);
            if (condition.isCompatibleWeak(conf)) {
                conf = cases.get(cas, indexOfvars);
                missingValues = false;
                for (i = 0; i < conf.size(); i++) {
                    nState = conf.getValue(i);
                    if (nState == -1) {
                        missingValues = true;
                        break;
                    }
                }
                if (!missingValues) {
                    {
                        indexTable = conf.getIndexInTable();
                        pot.incValue(indexTable, increment);
                    }
                }
            }
        }
        
        return pot;
    }
    
    /**
     * This method returns a table with the absolute frequencies for a variable
     * but considering only the cases compatible with a given configuration.
     * Registers with missing values are not considered.
     *
     * @param X
     *            a finite states node.
     * @param conf
     *            the configuration to which the database is restricted
     * @return a <code>PotentialTable</code> with the frequencies.
     */
    
    public PotentialTable getPotentialTable(FiniteStates X, Configuration conf) {
        
        PotentialTable pot = new PotentialTable(X);
        
        int i, nState, pos, k;
        Configuration confi;
        double increment = (double) (1.0);
        CaseList cases;
        boolean missingValues;
        NodeList vars;
        
        pot = new PotentialTable(X);
        cases = getCases();
        vars = getVariables();
        pos = vars.getId(X);
        
        for (int cas = 0; cas < getNumberOfCases(); cas++) {
            confi = cases.get(cas);
            missingValues = false;
            for (i = 0; i < confi.size(); i++) {
                nState = confi.getValue(i);
                if (nState == -1) {
                    missingValues = true;
                    break;
                }
            }
            if (!missingValues) {
                if (conf.isCompatibleWeak(confi)) {
                    k = confi.getValue(pos);
                    
                    pot.incValue(k, increment);
                }
                
            }
        }
        
        return pot;
    }
    
    /**
     * This method returns a table with the absolute counts for a subset of
     * variables in the data base.
     *
     * @param indexOfvars
     *            index of a subset of variables in data base variables.
     * @param vars
     *            the subset of variables to which index in
     *            <code>indexOfvars</code> refer.
     * @return a <code>PotentialTable</code> with the counts.
     */
    
    public PotentialTable getPotentialTable(NodeList vars, int[] indexOfvars) {
        
        Configuration conf;
        boolean missingValues;
        PotentialTable pot = new PotentialTable(vars);
        FiniteStates node;
        int i, nState, indexTable;
        double increment = (double) (1.0);
        CaseList cases;
        
        cases = (CaseList) ((Relation) getRelationList().elementAt(0))
        .getValues();
        for (int cas = 0; cas < getNumberOfCases(); cas++) {
            conf = cases.get(cas, indexOfvars);
            missingValues = false;
            for (i = 0; i < conf.size(); i++) {
                nState = conf.getValue(i);
                if (nState == -1) {
                    missingValues = true;
                    break;
                }
            }
            if (!missingValues) {
                indexTable = conf.getIndexInTable();
                pot.incValue(indexTable, increment);
            }
        }
        
        return pot;
    }
    
    /**
     * This method computes the absolute counts of a configuration given as
     * parameter.
     *
     * @param conf
     *            the configuration which count will be calculated.
     * @return the counts.
     */
    
    public double getTotalPotential(Configuration conf) {
        
        int ncases;
        CaseList cases;
        double val;
        
        cases = (CaseList) ((Relation) getRelationList().elementAt(0))
        .getValues();
        ncases = cases.getNumberOfCases();
        cases.setNumberOfCases(getNumberOfCases());
        val = cases.totalPotential(conf);
        cases.setNumberOfCases(ncases);
        
        return val;
    }
    
    /**
     * This method carry out a conditional independence test I(x,y|z) over data
     * in DB. To make this tests a chi-square tests is achieved.
     *
     * @param Node
     *            x.
     * @param Node
     *            y.
     * @param NodeList
     *            z. the conditionating set.
     * @param int
     *            degreeOfAccuracy. [0..4] zAlpha[]={1.28,1.64,1.96,2.33,2.58}.
     * @return boolean.
     */
    
    public boolean independents(Node x, Node y, NodeList z, int degreeOfAccuracy) {
        NodeList varsxyz;
        PotentialTable pxyz;
        int i, nStatesx, nStatesy, nStatesz, degreesOfFreedom;
        double dxyz, chiSquare, test;
        double[] zAlpha;
        zAlpha = new double[5];
        zAlpha[0] = 1.28;
        zAlpha[1] = 1.64;
        zAlpha[2] = 1.96;
        zAlpha[3] = 2.33;
        zAlpha[4] = 2.58;
        int[] indexOfvars;
        
        varsxyz = new NodeList();
        varsxyz.insertNode(x);
        varsxyz.insertNode(y);
        for (i = 0; i < z.size(); i++)
            varsxyz.insertNode((FiniteStates) z.elementAt(i));
        
        indexOfvars = new int[varsxyz.size()];
        for (i = 0; i < varsxyz.size(); i++) {
            int pos = getVariables().getId(varsxyz.elementAt(i));
            indexOfvars[i] = pos;
        }
        
        pxyz = getPotentialTable(varsxyz);
        pxyz.normalize();
        dxyz = pxyz.crossEntropyPotential();
        chiSquare = ((double) 2.0 * (double) getNumberOfCases()) * dxyz;
        
        if (z.size() != 0) {
            degreesOfFreedom = ((int) FiniteStates.getSize(z.getNodes()))
            * ((((FiniteStates) x).getNumStates() - 1) * (((FiniteStates) y)
            .getNumStates() - 1));
        } else {
            degreesOfFreedom = ((((FiniteStates) x).getNumStates() - 1) * (((FiniteStates) y)
            .getNumStates() - 1));
        }
        
        if (degreesOfFreedom <= 0)
            degreesOfFreedom = 1;
        
        test = (Math.pow((zAlpha[degreeOfAccuracy] + Math
                .sqrt((2.0 * degreesOfFreedom) - 1.0)), 2.0)) / 2.0;
        
        // System.out.println("Grados de Libertad: "+degreesOfFreedom);
        // System.out.println("Tabla x2 = "+test);
        // System.out.println("Estadistico = "+chiSquare);
        
        if (chiSquare > test)
            return (false);
        else
            return (true);
        
    }
    
    /** ************************************************************************** */
    /* This functions computes the value of the independence test for nodes */
        /*
         * x and y given z. /* It can be used as a measure of the strength of the
         * dependency of x and y given z
         */
    /* @return a double value t. For degrees of accuracy larger than this value */
    /* variables should be considered as independent */
    /** ************************************************************************** */
    
    public double testValue(Node x, Node y, NodeList z) {
        NodeList varsxyz;
        PotentialTree pxyz;
        int i, nStatesx, nStatesy, nStatesz;
        long degreesOfFreedom;
        double dxyz, chiS, test;
        
        varsxyz = new NodeList();
        varsxyz.insertNode(x);
        varsxyz.insertNode(y);

             
  
        for (i = 0; i < z.size(); i++)
            varsxyz.insertNode((FiniteStates) z.elementAt(i));
        

        pxyz = getPotentialTree(varsxyz);
    
        pxyz.normalize();
        /** ******************************************************* */
        dxyz = pxyz.crossEntropyPotential();
        chiS = ((double) 2.0 * (double) getNumberOfCases()) * dxyz;
        /** ****************************************************** */
        if (z.size() != 0) {
            degreesOfFreedom = ((long) FiniteStates.getSize(z.getNodes()))
            * ((((FiniteStates) x).getNumStates() - 1) * (((FiniteStates) y)
            .getNumStates() - 1));
        } else {
            degreesOfFreedom = ((((FiniteStates) x).getNumStates() - 1) * (((FiniteStates) y)
            .getNumStates() - 1));
        }
        // System.out.println("Grados de Libertad: "+degreesOfFreedom);
        long potSize = pxyz.getSize();

        // System.out.println("T. Pot: "+potSize);
        if (potSize < degreesOfFreedom)
            degreesOfFreedom = potSize;
        if (degreesOfFreedom <= 0)
            degreesOfFreedom = 1;
        double aux1 = (double) degreesOfFreedom;
        /** ******************************************************* */
        test = LogFactorial.chiSquare(chiS, aux1);
        /** ******************************************************* */
        // System.out.println("Grados de Libertad: "+degreesOfFreedom);
        // System.out.println("Tabla x2 = "+test);
        // System.out.println("Estadistico = "+chiS);
        // System.out.println("Level of conf = "+degreeOfAccuracy);
        /** ******************************************************* */
        return (test);
        /** ******************************************************* */

        
    }
       
    /*****************************************************************************/
    /*        This functions computes the value of the independence test for nodes */
/*          x and y given configuration c.
/*        It can be used as a measure of the strength of the dependency of x and y given c */
    /*        @return a double value t. For degrees of accuracy larger than this value  */
    /*                variables should be considered as independent                  */
    /*****************************************************************************/

    public double testValue(Node x, Node y, Configuration c){
        NodeList varsxyz;
        PotentialTable pxyz;
        int i,nStatesx,nStatesy,nStatesz;
        long degreesOfFreedom;
        double dxyz,chiS,test,totalsize;
        
        varsxyz = new NodeList();
        varsxyz.insertNode(x);
        varsxyz.insertNode(y);
     
        
        pxyz = getPotentialTable(varsxyz,c);
        totalsize = pxyz.totalPotential();
        pxyz.normalize();
        /**********************************************************/
        dxyz = pxyz.crossEntropyPotential();
        chiS = ((double)2.0*totalsize)*dxyz;
        /*********************************************************/
       {
            degreesOfFreedom =((((FiniteStates)x).getNumStates()-1)*
            (((FiniteStates)y).getNumStates()-1));
        }
        //System.out.println("Grados de Libertad: "+degreesOfFreedom);
      
        //System.out.println("T. Pot: "+potSize);
      
        if(degreesOfFreedom <= 0) degreesOfFreedom = 1;
        double aux1 = (double)degreesOfFreedom;
        /**********************************************************/
        test = LogFactorial.chiSquare(chiS,aux1);
        /**********************************************************/
        //System.out.println("Grados de Libertad: "+degreesOfFreedom);
        //System.out.println("Tabla x2 = "+test);
        //System.out.println("Estadistico = "+chiS);
        //System.out.println("Level of conf = "+degreeOfAccuracy);
        
        /**********************************************************/
        return(test);
        /**********************************************************/
        
    }
     
   
    /** ************************************************************************** */
    /* It carries out a test of independence of x and y given z at */
    /* at degreeOfAccuracy level */
    /** ************************************************************************** */

    
    public boolean independents(Node x, Node y, NodeList z,
            double degreeOfAccuracy) {
        double test;
        
        test = testValue(x, y, z);
        
        // System.out.println("Grados de Libertad: "+degreesOfFreedom);
        // System.out.println("Tabla x2 = "+test);
        // System.out.println("Estadistico = "+chiS);
        // System.out.println("Level of conf = "+degreeOfAccuracy);
        
        /** ******************************************************* */
        if (degreeOfAccuracy >= test)
            return (true);
        else
            return (false);
        /** ******************************************************* */
        
    }
    
    public PotentialTree getPotentialTree(NodeList vars) {
        
        Configuration conf;
        boolean missingValues;
        PotentialTree pot = new PotentialTree(vars);
        FiniteStates node;
        int i, nState;
        CaseList cases;
        int[] indexOfvars;
        
        // System.out.println("Estoy en getPotentialTree con vars=
        // "+vars.toString2());
        indexOfvars = new int[vars.size()];
        for (i = 0; i < vars.size(); i++) {
            // FiniteStates no = (FiniteStates)vars.elementAt(i);
            // for(int h=0 ; h < no.getNumStates() ; h++)
            // System.out.println(no.getStringStates()[h]);
            int pos = getVariables().getId(vars.elementAt(i));
            indexOfvars[i] = pos;
        }
        
        cases = (CaseList) ((Relation) getRelationList().elementAt(0))
        .getValues();
        for (int cas = 0; cas < getNumberOfCases(); cas++) {
            conf = cases.get(cas, indexOfvars);
            // conf.print();
            // System.out.print(" "+cas);
            missingValues = false;
            for (i = 0; i < conf.size(); i++) {
                nState = conf.getValue(i);
                if (nState == -1) {
                    missingValues = true;
                    break;
                }
            }
            if (!missingValues) {
                double val = pot.getValue(conf);
                // System.out.println(" "+val);
                if (val < 0.0)
                    val = 0.0;
                val += 1.0;
                pot.setValue(conf, val);
            }
        }
        ProbabilityTree tree = pot.getTree();
        setProbTreeFull(tree);
        tree.updateSize();
        pot = new PotentialTree(vars);
        pot.setTree(tree);
        return pot;
        
    }
    
    /**
     * This method carries out a conditional independence test I(x,y|z) over
     * data in data base, to check the conditional independence between two
     * variables given a list of variables. By default the degree of accuracy is
     * 4. To this end, a chi-square test is used.
     *
     * @param x
     *            a variable.
     * @param y
     *            a variable.
     * @param z
     *            the conditionating set.
     * @return <code>true</code> if <code>x</code> and <code>y</code> are
     *         independent given <code>z</code>.
     * @see this#independents (Node x, Node y, NodeList z, int degreeOfAccuracy)
     *      by default the degree of accuracy is 4.
     */
    
    public boolean independents(Node x, Node y, NodeList z) {
        
        return independents(x, y, z, 0.99);
    }
    

     
      /*****************************************************************************/
    /*        This function computes a degree of the dependency between  */
/*          x and y given z.
/*      It simply computes the p-value of the chi-square test minus 0.99*/
    /*        @return a double value t.                                     */
    /*                variables should be considered as independent if this value is lower than 0 (at 0.99 confidence)               
     ***********************************************************************/

    public double getDep(Node x, Node y, NodeList z){
        
        return(testValue(x,y,z)-0.99);
        
    }
    
    /*****************************************************************************/
    /*                 Obtiene una transformacion de KL(PG,PDB)                  */
    /*****************************************************************************/
    
  
    public double getDivergenceKL(Bnet b) {

        
        NodeList bNodes = b.getNodeList();
        NodeList vars, paXi;
        FiniteStates Xi;
        Configuration confXi, confPaXi, confAux;
        int nConf, i, j, pos;
        double valXi, valPaXi, val, suma;
        PotentialTable pot, potXi, potPaXi;
        NodeList DBNodes = getNodeList();
        
        suma = 0.0;
        
        for (i = 0; i < bNodes.size(); i++) {
            Xi = (FiniteStates) bNodes.elementAt(i);
            paXi = b.parents(Xi);
            if (paXi.size() > 0) {
                vars = new NodeList();
                vars.insertNode(Xi);
                vars.join(paXi);
                vars = DBNodes.intersectionNames(vars);
                pot = getPotentialTable(vars);
                pot.normalize();
                Xi = (FiniteStates) DBNodes.getNode(Xi.getName());
                paXi = DBNodes.intersectionNames(paXi);
                potPaXi = (PotentialTable) pot.addVariable(Xi);
                potXi = (PotentialTable) pot.addVariable(paXi.toVector());
                nConf = (int) FiniteStates.getSize(vars);
                confAux = new Configuration(vars);
                for (j = 0; j < nConf; j++) {
                    val = pot.getValue(confAux);
                    confXi = new Configuration(confAux, paXi.toVector());
                    confPaXi = new Configuration(paXi.toVector(), confAux);
                    valXi = potXi.getValue(confXi);
                    valPaXi = potPaXi.getValue(confPaXi);
                    if ((valXi > 0.0) && (valPaXi > 0.0) && (val > 0.0)) {
                        suma += (val * (Math.log((val / (valXi * valPaXi)))));
                    }
                    if (Double.isNaN(suma)) {
                        System.out.println("vars: " + vars.toString2());
                        System.out.println("Xi: " + Xi.getName());
                        System.out.println("PaXi: " + paXi.toString2());
                        pot.print();
                        potXi.print();
                        potPaXi.print();
                        System.out.println("Valor: " + val);
                        System.out.println("ValorXi: " + valXi);
                        System.out.println("ValorPaXi: " + valPaXi);
                        confAux.print();
                        confXi.print();
                        confPaXi.print();
                        System.exit(0);
                    }
                    confAux.nextConfiguration();
                }
            }
        }
        
        return suma;
        
    }
    
    /**
     * Gets the average value for the continuous variable <code>var</code> in
     * this database, taking into account only cases satisfying Configuration
     * <code>varClass</code>. This configuration can be for example a
     * concrete value for the class in a classification problem.
     *
     * @param varClass
     *            a configuration of finite states variables of this database
     *            (for example, the class, in a classification problem). This
     *            parameter can be <code>null</code>. In this case an empty
     *            Configuration is supposed.
     * @param var
     *            the continuous variable in which we calculate the average
     *            value.
     * @return the average of values for the continuous variable
     *         <code>var</code> in the given Configuration of this database.
     *         Undef values are not considered to calculated the average.
     */
    
    public double average(Configuration varClass, Continuous var) {
        Configuration confDiscreteVars;
        int nelements = 0;
        double sumValues = 0.0;
        int ncases = getNumberOfCases();
        ContinuousCaseListMem cases = null;
        double value;
        
        cases = (ContinuousCaseListMem) getCases();
        if (!(cases instanceof ContinuousCaseListMem)) {
            System.out
                    .println("Error in DataBaseCases.average(Configuration,Continuous): "
                    + "This database has not any continuous variable");
            System.exit(1);
        }
        
        if (varClass != null) {
            for (int i = 0; i < ncases; i++) {
                confDiscreteVars = new Configuration(varClass.getVariables(),
                        cases.get(i));
                if (varClass.isCompatible(confDiscreteVars)) {
                    value = ((ContinuousConfiguration) (cases.get(i)))
                    .getValue(var);
                    if (value != var.undefValue()) {
                        nelements++;
                        sumValues += value;
                    }
                }
            }
        } else {
            for (int i = 0; i < ncases; i++) {
                value = ((ContinuousConfiguration) (cases.get(i)))
                .getValue(var);
                if (value != var.undefValue()) {
                    nelements++;
                    sumValues += value;
                }
            }
        }
        return sumValues / nelements;
    }
    
    /**
     * Gets the average and variance values for the continuous variable
     * <code>var</code> in this database, taking into account only cases
     * satisfying Configuration <code>varClass</code>. This configuration can
     * be for example a concrete value for the class in a classification
     * problem. This method also returns the number of valid cases (sample
     * size).
     *
     * @param varClass
     *            a configuration of finite states variables of this database
     *            (for example, the class, in a classification problem). This
     *            parameter can be <code>null</code>. In this case an empty
     *            Configuration is supposed.
     *
     * @param var
     *            the continuous variable in which we calculate the average
     *            value.
     * @return an array of tree doubles with the average, variance values for
     *         the continuous variable <code>var</code> in the given
     *         Configuration of this database. The average is in the position 0
     *         of the array and the variance in the position 1. The number of
     *         valid cases is in position 2. Undef values are not considered to
     *         calculated the average and variance
     */
    
    public double[] averageAndVariance(Configuration varClass, Continuous var) {
        Configuration confDiscreteVars;
        int nelements = 0;
        double sumValues = 0.0, sumSquareValues = 0.0;
        int ncases = getNumberOfCases();
        ContinuousCaseListMem cases = null;
        double value;
        double[] meanAndVariance = new double[3];
        
        cases = (ContinuousCaseListMem) getCases();
        if (!(cases instanceof ContinuousCaseListMem)) {
            System.out
                    .println("Error in DataBaseCases.averageAndVariance(Configuration,Continuous): "
                    + "This database has not any continuous variable");
            System.exit(1);
        }
        
        if (varClass != null) {
            for (int i = 0; i < ncases; i++) {
                confDiscreteVars = new Configuration(varClass.getVariables(),
                        cases.get(i));
                if (varClass.isCompatible(confDiscreteVars)) {
                    value = ((ContinuousConfiguration) (cases.get(i)))
                    .getValue(var);
                    if (value != var.undefValue()) {
                        nelements++;
                        sumValues += value;
                        sumSquareValues += (value * value);
                    }
                }
            }
        } else {
            for (int i = 0; i < ncases; i++) {
                value = ((ContinuousConfiguration) (cases.get(i)))
                .getValue(var);
                if (value != var.undefValue()) {
                    nelements++;
                    sumValues += value;
                    sumSquareValues += (value * value);
                }
            }
        }
        System.out.println("suma de los cuadrados" + sumSquareValues);
        meanAndVariance[0] = sumValues / nelements;
        meanAndVariance[1] = sumSquareValues / nelements - meanAndVariance[0]
                * meanAndVariance[0];
        meanAndVariance[2] = nelements;
        return meanAndVariance;
    }
    
    /**
     * Gets the average and variance estimator values for the continuous
     * variable <code>var</code> in this database, taking into account only
     * cases satisfying Configuration <code>varClass</code>. This
     * configuration can be for example a concrete value for the class in a
     * classification problem. This method also returns the number of valid
     * cases (sample size).
     *
     * @param varClass
     *            a configuration of finite states variables of this database
     *            (for example, the class, in a classification problem). This
     *            parameter can be <code>null</code>. In this case an empty
     *            Configuration is supposed.
     * @param var
     *            the continuous variable in which we calculate the average
     *            value.
     * @see averageAndVariance(Configuration,Continuous)
     * @return an array of tree doubles with the average and variance estimator
     *         values for the continuous variable <code>var</code> in the
     *         given Configuration of this database. The average is in the
     *         position 0 of the array and the variance in the position 1. The
     *         number of valid cases is in position 2. Undef values are not
     *         considered to calculate the average and variance
     */
    
    public double[] averageAndVarianceEstimators(Configuration varClass,
            Continuous var) {
        double[] values;
        
        values = averageAndVariance(varClass, var);
        values[1] = (((int) values[2]) * values[1]) / (((int) values[2]) - 1);
        return values;
    }
    
    /**
     * Gets an array of doubles. The first index correspond to each one of the
     * variables of this DataBaseCases (except the variable varClass). The
     * second index correspond with the different cases of the parameter
     * varClass (FiniteStates). If the variable is FiniteStates then the third
     * parameter correspond with the different cases of the current variable
     * (see first index). In this case, the calculated array will contain in
     * this position the absolute frecuency for the state of the current
     * FiniteState variable and the current state of varClass (note that
     * undefined values are not considered). If the variable is Continuous then
     * the third parameter contains tree positions: The first one contains the
     * sum of the values for the current Continuous Variable in the current
     * state of varClass. The second one contains the sum of the square of the
     * values ... The third one contains the number of cases the Continous
     * variable if defined in the current state of varClass.
     *
     * @param varClass
     *            a FiniteStates corresponding to the variable of
     *            classification.
     * @return a tridimensional array with the calculated frecuencies and sums
     */
    
    public double[][][] getPreDistributionsNB(FiniteStates varClass) {
        NodeList nList = getNodeList();
        int nVars = nList.size() - 1;
        double[][][] preDistributions = new double[nVars][][];
        Node node;
        CaseListMem cases;
        ContinuousConfiguration caseElement;
        int valueClass, valueInt;
        double valueDouble;
        int ncases = getNumberOfCases();
        int nValuesClass = varClass.getNumStates();
        int i, j;
        
        // System.out.println("Alocating memory for the array");
        i = 0;
        Enumeration enumerator = nList.elements();
        while (enumerator.hasMoreElements()) { // Allocate the tridimensional
            // array
            node = (Node) enumerator.nextElement();
            if (!varClass.equals(node)) {
                if (node instanceof FiniteStates) {
                    preDistributions[i] = new double[nValuesClass][((FiniteStates) node)
                    .getNumStates()];
                    for (int m = 0; m < nValuesClass; m++)
                        for (int n = 0; n < ((FiniteStates) node)
                        .getNumStates(); n++)
                            preDistributions[i][m][n] = 0;
                } else if (node instanceof Continuous) {
                    preDistributions[i] = new double[nValuesClass][3];
                    for (int m = 0; m < nValuesClass; m++)
                        for (int n = 0; n < 3; n++)
                            preDistributions[i][m][n] = 0;
                    
                } else {
                    System.out
                            .println("Error in DataBaseCases.getPreDistributionsNB(FiniteStates):"
                            + " Nodes must be FiniteStates or Continuous");
                    System.exit(1);
                }
                i++;
            }
        }
        
        // System.out.println("Calculating componets in the array");
        if (getCases().getClass() == ContinuousCaseListMem.class) {
            cases = (ContinuousCaseListMem) getCases();
        } else {
            cases = (CaseListMem) getCases();
        }
                /*
                 * if(!(cases instanceof ContinuousCaseListMem)){
                 * System.out.println("Error in
                 * DataBaseCases.getPreDistributionsNB(FiniteStates):"+ "This database
                 * has not any continuous variable"); System.exit(1); }
                 */
        for (i = 0; i < ncases; i++) {
            j = 0;
            if (cases.get(i).getClass() == Configuration.class)
                caseElement = new ContinuousConfiguration((Configuration) cases.get(i));
            else
                caseElement = (ContinuousConfiguration) cases.get(i);
            valueClass = caseElement.getValue(varClass);
            enumerator = nList.elements();
            while (enumerator.hasMoreElements()) {
                node = (Node) enumerator.nextElement();
                if (!varClass.equals(node)) {
                    if (node instanceof FiniteStates) {
                        valueInt = caseElement.getValue((FiniteStates) node);
                        if (valueInt != node.undefValue()) {
                            preDistributions[j][valueClass][valueInt] += 1;
                        }
                    } else if (node instanceof Continuous) {
                        valueDouble = caseElement.getValue((Continuous) node);
                        
                        if (valueDouble != node.undefValue()) {
                            // System.out.println("Encontrado "+valueDouble+".
                            // Lo pongo en "+j+ ":"+valueClass+".!");
                            preDistributions[j][valueClass][0] += valueDouble;
                            preDistributions[j][valueClass][1] += (valueDouble * valueDouble);
                            preDistributions[j][valueClass][2] += 1;
                        }
                    } else {
                        System.out
                                .println("Error in DataBaseCases.getPreDistributionsNB(FiniteStates)"
                                + ": Found a node nor FiniteStates nor Continuous");
                        System.exit(1);
                    }
                    j++;
                } // end if(!varClass.equals(node))
            } // end while
        } // end for
        return preDistributions;
    }
    
    /**
     * Removes a set of variables specified by a Node list.
     */
    public void removeVariables(NodeList nodes) {
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseListMem caselistmem = (CaseListMem) relation.getValues();
        
        caselistmem.deleteVariables(nodes);
        relation.setVariables(caselistmem.getVariables());
        this.setNodeList(new NodeList(caselistmem.getVariables()));
    }
    
        /*
         * This method projects a database object above the Node list.
         */
    public void projection(NodeList nodes) {
        
        NodeList newNodeList = new NodeList();// this.getVariables().copy();
        for (int i = 0; i < nodes.size(); i++)
            newNodeList.insertNode(this.getVariables().getNode(
                    nodes.elementAt(i).getName()).copy());
        
        Vector vector = this.getRelationList();
        Relation relation = (Relation) vector.elementAt(0);
        CaseListMem caselistmem = (CaseListMem) relation.getValues();
        
        ContinuousCaseListMem c = caselistmem.projection(nodes.copy());
        relation.setValues(c);
        relation.setVariables(c.getVariables());
        this.setNodeList(new NodeList(c.getVariables()));
        
    }

    
    
    /*
     * This is an auxiliar method for hierarchical classifiers, to transform a database
     * in order to change the global class variable, by the local class variable in a leaf
     * of a tree. Only the cases corresponding to boolean vector of members are considered
     * and the values of the new variable are assigned according to these   different members.
     *
     *
     **/ 
     
    public DataBaseCases transform(FiniteStates classVar, HierarchyBnet tree) {
       int[] mapping;
       boolean[] members;
       int current;
       int i,n,pos,total,j,nc;
       FiniteStates node,localclass;
       CaseListMem newcases,oldcases;
       Configuration newconf,oldconf;
       DataBaseCases result;

       members = tree.getMembers();
       localclass =  tree.getAuxVar(); 
       n = classVar.getNumStates();
       current = 0;
       mapping = new int[n];
       for (i=0; i<n; i++){
          if (tree.isLeaf()) { 
              if (members[i]) {
               mapping[i] = current;
               current++;
           }
          }
          else { 
            nc = localclass.getNumStates();
            for(j=0; j<nc; j++) {  
              if (tree.getChildat(j).getMembers()[i]) {
              mapping[i] = j;
          }
           
           
       }
          }
       }
          
       pos=0;
        NodeList newNodeList=new NodeList();//this.getVariables().copy();
        for ( i=0; i<getNodeList().size();i++){
            node = (FiniteStates) this.getNodeList().elementAt(i);
            if (node.equals(classVar)){
                pos=i;
                 newNodeList.insertNode(localclass);
            }
            else{
              newNodeList.insertNode(node);
            }
        }
        
        oldcases = (CaseListMem) this.getCases();
        newcases = new CaseListMem(newNodeList);
        
        oldcases.initializeIterator();
        
        
        
        while (oldcases.hasNext()){
           oldconf = oldcases.getNext();
         
           if (members[oldconf.getValue(pos)]){
               newconf = oldconf.duplicate();
               newconf.getVariables().setElementAt( localclass,pos);
               newconf.getValues().setElementAt( new Integer (mapping[ ((Integer) oldconf.getValues().elementAt(i)).intValue()    ]),pos);
               newcases.put(newconf);
           }
                   
            
            
        }
      
        result = new DataBaseCases(classVar.getName() ,  newcases);
        return(result);
        
    }
    
    
    

    
        /*
         * This method return a copy of a DataBaseCases object.
         */
    
    public DataBaseCases copy() {
        
        DataBaseCases db = new DataBaseCases(new String(getName()),
                (CaseList) getCases().copy());
        return db;
    }
    
        /*
         * This method read from a file a serialized object. @param objectFile --> A
         * string with the name of the file.
         */
    public static DataBaseCases readSerialization(String objectFile)
    throws java.io.FileNotFoundException, java.io.IOException,
            java.lang.ClassNotFoundException {
        
        System.out.println("deserializing...");
        
        FileInputStream fis = new FileInputStream(objectFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        DataBaseCases retrieved = (DataBaseCases) ois.readObject();
        ois.close();
        
        System.out.println("deserialized...");
        
        return retrieved;
        
    }
    
        /*
         * This method read from a InputStream a serialized object. @param
         * objectFile --> A string with the name of the file.
         */
    public static DataBaseCases readSerialization(InputStream fis)
    throws java.io.FileNotFoundException, java.io.IOException,
            java.lang.ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(fis);
        DataBaseCases retrieved = (DataBaseCases) ois.readObject();
        ois.close();
        return retrieved;
    }
    
        /*
         * This method saves the object that invokes it, using serialization, in a
         * file. So this file contains a serialized DataBaseCase object. @param
         * objectFile --> A string with the name of the file.
         */
    public void writeSerialization(String objectFile)
    throws java.io.FileNotFoundException, java.io.IOException,
            java.lang.ClassNotFoundException {
        System.out.println("serializing...");
        
        File objectF = new File(objectFile);
        if (objectF.exists()) {
            objectF.delete();
        }
        FileOutputStream fos = new FileOutputStream(objectF);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.flush();
        oos.close();
        oos.flush();
        fos.close();
        
        System.out.println("serialized...");
    }
    
    /**
     * The min and max field of all Continuous nodes of this data base are set
     * to its mininum and maximum real value respectively present in this data
     * set. The missing values for Continuous nodes are accordingly updated.
     */
    public void setMinMax() {
        
        NodeList nl = this.getVariables().copy();
        for (int i = 0; i < nl.size(); i++) {
            if (nl.elementAt(i).getClass() == Continuous.class) {
                double min = Double.MAX_VALUE;
                double max = -Double.MAX_VALUE;
                
                Continuous node = (Continuous) nl.elementAt(i);
                double prevUndef = node.undefValue();
                
                int idNode = this.getVariables().getId(node);
                
                if (i % 1000 == 0)
                    System.out.print(", " + i / 1000);
                for (int j = 0; j < this.getNumberOfCases(); j++) {
                    double value = ((ContinuousCaseListMem) this.getCases())
                    .getValue(j, idNode);
                    if (value != node.undefValue()) {
                        if (value < min)
                            min = value;
                        if (value > max)
                            max = value;
                    }
                }
                
                ((Continuous) this.getVariables().elementAt(idNode))
                .setMin(min);
                ((Continuous) this.getVariables().elementAt(idNode))
                .setMax(max);
                
                // Corregimos los valores perdidos que se almacenan como min-1,
                // al actual valor de min-1.
                for (int j = 0; j < this.getNumberOfCases(); j++) {
                    double value = ((ContinuousCaseListMem) this.getCases())
                    .getValue(j, idNode);
                    if (value == prevUndef) {
                        ((ContinuousCaseListMem) this.getCases()).setValue(j,
                                idNode, min - 1);
                    }
                }
                
            }
        }
        
    }
    
    /**
     * The min field of the Continuous 'node' is set to 'min'. The missing
     * values for this node are accordingly updated.
     */
    public void setNodeMin(Continuous node, double min) {
        node = (Continuous) this.getVariables().elementAt(
                this.getVariables().getId(node));
        double prevUndef = node.undefValue();
        (((Continuous) this.getVariables().getNode(node.getName())))
        .setMin(min);
        // Corregimos los valores perdidos que se almacenan como min-1, al
        // actual valor de min-1.
        int idNode = this.getVariables().getId(node);
        for (int j = 0; j < this.getNumberOfCases(); j++) {
            double value = ((ContinuousCaseListMem) this.getCases()).getValue(
                    j, idNode);
            if (value == prevUndef) {
                ((ContinuousCaseListMem) this.getCases()).setValue(j, idNode,
                        min - 1);
            }
        }
    }
    
    /**
     * The max field of the Continuous 'node' is set to 'max'.
     */
    public void setNodeMax(Continuous node, double max) {
        (((Continuous) this.getVariables().getNode(node.getName())))
        .setMax(max);
    }
    
    /**
     * Return the data for all the cases of a Continuous variable. The returned
     * array doesn't contain missing values. So the length of the returned array
     * is not equal to the number of cases, if there are missing values for this
     * variable.
     */
    public double[] getDataAll(Continuous variable) {
        int indexVar = this.getVariables().getId(variable);
        
        Vector values = new Vector();
        for (int i = 0; i < this.getNumberOfCases(); i++) {
            double value = ((ContinuousCaseListMem) this.getCaseListMem())
            .getValue(i, indexVar);
            if (value != variable.undefValue())
                values.addElement(new Double(value));
        }
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = ((Double) values.elementAt(i)).doubleValue();
        }
        return result;
    }
    
    /**
     * This method returns if a Continuous node follow a Normal Distribution
     * using the Kolmogorov-Smirnov test.
     */
    public boolean KSNormalityTest(Continuous node) {
        if (!MathMAE.nr_ksoneNormal(this.getDataAll(node)))
            return false;
        else if (MathMAE.probKS > MathMAE.P_THRESHOLD)
            return true;
        else
            return false;
    }
    
    /**
     * This method returns if a Continuous node follow a LogNormal Distribution
     * using the Kolmogorov-Smirnov test. The followed procedure for this test
     * is: - A logarithm transformation for the values of this node: for each
     * value of this node: if (value>0) value=log(value); if (value<0)
     * value=-log(-value); if (value==0) "this data is not consider in the
     * test". - The KSNormalitity test for this transformed data.
     */
    public boolean KSLogNormalityTest(Continuous node) {
        
        if (!MathMAE.nr_ksoneLogNormal(this.getDataAll(node)))
            return false;
        else if (MathMAE.probKS > MathMAE.P_THRESHOLD)
            return true;
        else
            return false;
        
    }
    
    /**
     * Return the mininum value of the all Continuous node in the data base.
     */
    public double globalMinContinuousValue() {
        
        double min = Double.MAX_VALUE;
        for (int i = 0; i < this.getVariables().size(); i++) {
            if (this.getVariables().elementAt(i).getClass() == Continuous.class) {
                Continuous node = (Continuous) this.getVariables().elementAt(i);
                for (int j = 0; j < this.getNumberOfCases(); j++) {
                    double value = ((ContinuousCaseListMem) this
                            .getCaseListMem()).getValue(j, i);
                    if (value != node.undefValue()) {
                        if (value < min) {
                            min = value;
                        }
                    }
                }
            }
        }
        return min;
    }
    
    /**
     * This method carries out a logarithm transformation of the values of the
     * Continous nodes of this data base. The logartihm transformation is: for
     * each value of this node: if (value>0) value=log(value); if (value<0)
     * value=-log(-value); if (value==0) "this data is set to a missing value".
     */
    public void logABSTransformation() {
        
        for (int i = 0; i < this.getVariables().size(); i++) {
            if (this.getVariables().elementAt(i).getClass() == Continuous.class) {
                Continuous node = (Continuous) this.getVariables().elementAt(i);
                for (int j = 0; j < this.getNumberOfCases(); j++) {
                    double value = ((ContinuousCaseListMem) this
                            .getCaseListMem()).getValue(j, i);
                    if (value != node.undefValue()) {
                        if (value < 0)
                            value = -Math.log(-value);
                        if (value > 0)
                            value = Math.log(value);
                        if (value == 0)
                            value = node.undefValue();
                        ((ContinuousCaseListMem) this.getCaseListMem())
                        .setValue(j, i, value);
                    }
                }
            }
        }
    }
    
    /**
     * This method carries out a normalization procedure (mean to 0 and standard
     * deviation to 1) of the values of the Continuous nodes of this data base.
     *
     * @return a matrix where R[this.getVariables().getId(node)][0] and
     *         R[this.getVariables().getId(node)][0] contains the mean and the
     *         standard deviation of the values of this node respectively.
     */
    public double[][] normalization() {
        
        double[][] statistics = new double[this.getVariables().size()][2];
        for (int i = 0; i < this.getVariables().size(); i++) {
            if (this.getVariables().elementAt(i).getClass() == Continuous.class) {
                if (i % 100 == 0)
                    System.out.print(i + ", ");
                Continuous node = (Continuous) this.getVariables().elementAt(i);
                double[] data = this.getDataAll(node);
                statistics[i][0] = Stat.mean(data);
                statistics[i][1] = Stat.standardDeviation(data);
                for (int j = 0; j < this.getNumberOfCases(); j++) {
                    double value = ((ContinuousCaseListMem) this
                            .getCaseListMem()).getValue(j, i);
                    if (value != node.undefValue()) {
                        value = Stat.normalization(value, statistics[i][0],
                                statistics[i][1]);
                        ((ContinuousCaseListMem) this.getCaseListMem())
                        .setValue(j, i, value);
                    }
                }
                // this.setNodeMin(node,Stat.normalization(node.getMin(),statistics[i][0],statistics[i][1]));
                // this.setNodeMax(node,Stat.normalization(node.getMax(),statistics[i][0],statistics[i][1]));
            }
        }
        return statistics;
    }
    
    public double[][] normalization(double[][] statistics) {
        
        for (int i = 0; i < this.getVariables().size(); i++) {
            if (this.getVariables().elementAt(i).getClass() == Continuous.class) {
                Continuous node = (Continuous) this.getVariables().elementAt(i);
                for (int j = 0; j < this.getNumberOfCases(); j++) {
                    double value = ((ContinuousCaseListMem) this
                            .getCaseListMem()).getValue(j, i);
                    if (value != node.undefValue()) {
                        value = Stat.normalization(value, statistics[i][0],
                                statistics[i][1]);
                        ((ContinuousCaseListMem) this.getCaseListMem())
                        .setValue(j, i, value);
                    }
                }
                // this.setNodeMin(node,Stat.normalization(node.getMin(),statistics[i][0],statistics[i][1]));
                // this.setNodeMax(node,Stat.normalization(node.getMax(),statistics[i][0],statistics[i][1]));
            }
        }
        return statistics;
    }
    
    /**
     * Computes the log-likelihood of the database given the net over the data.
     *
     * @param net
     *            the Bnet with respect to which the log-likelihood is computed.
     * @return the obtained logLikelihood.
     */
    
    public double logLikelihood(Bnet net) {
        
        double logLikelihood = 0;
        double aux, value;
        int numVars = getVariables().size();
        int numCases = getNumberOfCases();
        Node var;
        Relation rel;
        Potential pot;
        CaseList caseList = getCases();
        Configuration conf;

        for (int j = 0; j < numCases; j++) {
            conf = caseList.get(j);
            
            for (int i = 0; i < numVars; i++) {
                var = getVariables().elementAt(i);
                rel = net.getRelation(var);
                pot = rel.getValues();
                value = pot.getValue(conf);
                
                if (value < 0) {
                    System.out
                            .println("*************** log de un n. negativo ***************");
                }
                
                if (value == 0) {
                    System.out
                            .println("*************** log de 0 ***************");
                }
                
                // if it's 0, I assign the smallest double
                if (value == 0) {
                    System.out.println("sale un valor 0");
                    value = 0 - Double.MAX_VALUE;
                } else
                    value = Math.log(value);
                
                if ((new Double(value)).isNaN())
                    System.out.println("Un valor tiene log NaN");
                
                logLikelihood += value;
            }
        }
        
        return logLikelihood;
    }
    
    /**
     * Divides a database into two parts. Useful for train and test procedures.
     *
     * @param train
     * 			the database that should contain the training set. It should be created
     * 			by the caller.
     * @param test
     * 			the database that should contain the test set. It should be created
     * 			by the caller.
     * @param rate
     * 			a double between 0.0 and 1.0 indicating the proportion of cases that should
     * 			be assigned to the training set.
     */
    public void divideIntoTrainAndTest(DataBaseCases train, DataBaseCases test, double rate){
        divideIntoTrainAndTest(train, test, rate, new Random());
    }
    
    /**
     * Divides a database into two parts. Useful for train and test procedures.
     *
     * @param train
     *            a database that must be previously created. It is modified.
     * @param test
     *            a database that must be previously created. It is modified.
     * @param rate
     *            a value between 0 and 1 indicating the proportion of records
     *            in the original database that will be stored in
     *            <code>train</code>. The rest will be stored in
     *            <code>test</code>.
     * @param rnd
     * 			 a Random object to be used for the shuffling of the data cases.
     */
    public void divideIntoTrainAndTest(DataBaseCases train, DataBaseCases test,
            double rate, Random rnd) {
        CaseListMem c = this.getCaseListMem(), ctest, ctrain;
        int i, nc = this.getNumberOfCases();
        Configuration conf;
        
        // the following was added in order to aboid
        // expeptions from the CaseListMem.put(Configuration conf) method
        // when conf contains continous variables
        if(c instanceof ContinuousCaseListMem){
            ctrain = new ContinuousCaseListMem(this.getVariables().copy());
            ctest = new ContinuousCaseListMem(this.getVariables().copy());
        } else {
            ctrain = new CaseListMem(this.getVariables().copy());
            ctest = new CaseListMem(this.getVariables().copy());
        }
        
        c.randomize(rnd);
        
        for (i = 0; i < ((int) (rate * nc)); i++) {
            conf = c.get(i);
            ctrain.put(conf);
        }
        
        for (i = ((int) (rate * nc)); i < nc; i++) {
            conf = c.get(i);
            ctest.put(conf);
        }
        
        train.setNodeList(this.getVariables().copy());
        test.setNodeList(this.getVariables().copy());
        
        train.replaceCases(ctrain);
        test.replaceCases(ctest);
        train.setName(this.getName() + "_train");
        test.setName(this.getName() + "_test");
    }
    
    
    /**
     *  Used for k-fold cross validation.
     * @param fold the number of training set to return.
     * @param k the number of folds.
     * @return The fold-th set of cases to train the model.
     */
    public DataBaseCases getTrainCV(int fold, int k){
    	DataBaseCases retval = null;
    	if(this.getCaseListMem().getClass() == ContinuousCaseListMem.class){
    		retval = this.getContinuousTrainCV(fold, k);
    	} else {
    		retval = new DataBaseCases();
    		retval.setNodeList(this.getVariables().copy());
    		CaseListMem retCases = this.getCaseListMem().getTrainCV(fold,k);
    		retval.replaceCases(retCases);
    	}
    	return retval;
    }
    
    private DataBaseCases getContinuousTrainCV(int fold, int k) {
        
        ContinuousCaseListMem returnCases, thisCases = (ContinuousCaseListMem)this.getCaseListMem();
        DataBaseCases returnDatabase = new DataBaseCases();
        
        if ((k<2) || (k>thisCases.getNumberOfCases()))
            return null;
        
        returnCases = (ContinuousCaseListMem)thisCases.getTrainCV(fold,k);
        
        returnDatabase.setNodeList(this.getVariables().copy());
        returnDatabase.replaceCases(returnCases);
        
        return(returnDatabase);
    }
    
    
    /**
     *  Used for k-fold cross validation.
     * @param fold the number of test set to return.
     * @param k the number of folds.
     * @return The fold-th set of cases to test the model.
     */
    public DataBaseCases getTestCV(int fold, int k) {
    	DataBaseCases retval = null;
    	if(this.getCaseListMem().getClass() == ContinuousCaseListMem.class){
    		retval = this.getContinuousTestCV(fold, k);
    	} else {
    		retval = new DataBaseCases();
    		retval.setNodeList(this.getVariables().copy());
    		CaseListMem retCases = this.getCaseListMem().getTestCV(fold,k);
    		retval.replaceCases(retCases);
    	}
    	return retval;
    }
    
    private DataBaseCases getContinuousTestCV(int fold, int k){
        ContinuousCaseListMem returnCases, thisCases = (ContinuousCaseListMem)this.getCaseListMem();
        DataBaseCases returnDatabase = new DataBaseCases();
        
        if ((k<2) || (k>thisCases.getNumberOfCases()))
            return null;
        
        returnCases = (ContinuousCaseListMem)thisCases.getTestCV(fold,k);
        
        returnDatabase.setNodeList(this.getVariables().copy());
        returnDatabase.replaceCases(returnCases);
        
        return(returnDatabase);
    }
    /**
     * This method compares two DataBaseCases objects.
     *
     * @return boolean, true is they contains the same data base, and false if
     *         otherwise.
     */
    public boolean equals(Object anObject) {
        if (anObject instanceof DataBaseCases) {
            DataBaseCases db = (DataBaseCases) anObject;
            if (this.getNumberOfCases() != db.getNumberOfCases())
                return false;
            if (!this.getVariables().equals(db.getVariables()))
                return false;
            for (int i = 0; i < this.getNumberOfCases(); i++)
                for (int j = 0; j < this.getVariables().size(); j++)
                    if (this.getCaseListMem().getValue(i, j) != this
                    .getCaseListMem().getValue(i, j))
                        return false;
            return true;
            
        } else
            return false;
    }
    
    /**
     * Change the order of two nodes and update the data base properly.
     *
     * @param i,
     *            the index of the first node.
     * @param j,
     *            the index of the second node.
     */
    public DataBaseCases changeNodeOrder(int actualP, int newP) {
        
        NodeList nodes = new NodeList();
        for (int i = 0; i < this.getVariables().size(); i++)
            if (i == actualP)
                nodes.insertNode(this.getVariables().elementAt(newP).copy());
            else if (i == newP)
                nodes.insertNode(this.getVariables().elementAt(actualP).copy());
            else
                nodes.insertNode(this.getVariables().elementAt(i).copy());
        
        CaseListMem casesTemp = this.getCaseListMem();
        Vector dataDBC = new Vector();
        if (casesTemp.getClass() == CaseListMem.class) {
            CaseListMem cases = casesTemp;
            for (int i = 0; i < this.getNumberOfCases(); i++) {
                int[] cas = cases.getCase(i);
                int[] cas2 = new int[cas.length];
                for (int j = 0; j < cas2.length; j++)
                    cas2[j] = cas[j];
                int tmp = cas2[actualP];
                cas2[actualP] = cas2[newP];
                cas2[newP] = tmp;
                dataDBC.addElement(cas2);
            }
            CaseListMem clm = new CaseListMem(nodes);
            clm.setCases(dataDBC);
            return new DataBaseCases(this.getName(), clm);
        } else {
            ContinuousCaseListMem cases = (ContinuousCaseListMem) casesTemp;
            for (int i = 0; i < this.getNumberOfCases(); i++) {
                double[] cas2 = new double[this.getVariables().size()];
                for (int j = 0; j < cas2.length; j++)
                    cas2[j] = cases.getValue(i, j);
                double tmp = cas2[actualP];
                cas2[actualP] = cas2[newP];
                cas2[newP] = tmp;
                dataDBC.addElement(cas2);
            }
            ContinuousCaseListMem clm = new ContinuousCaseListMem(nodes);
            clm.setCases(dataDBC);
            return new DataBaseCases(this.getName(), clm);
        }
        
    }
    
    /**
     * Change the position of two columns (x and y) in this DataBaseCases. The cases
     * and nodelist are updated accordingly. It is almost equivalent to changeNodeOrder(x,y), however 
     * swapColumns works on this object and do not create a new DataBaseCases object.
     *
     *@author dalgaard 
     *
     *@param x - the index of the column that will be swapped to index y
     *@param y - the index of the column that will be swapped to index x
     */
    public void swapColumns(int x, int y) {
        
        Vector<Node> newNodes = this.getVariables().getNodes();
        // get the node references
        Node nX = newNodes.elementAt(x);
        Node nY = newNodes.elementAt(y);

        // swap the nodes
        newNodes.setElementAt(nX, y);
        newNodes.setElementAt(nY, x);
        
        // now swap the corresponding values in the cases
        this.setNodeList(new NodeList(newNodes));
        CaseListMem myCases = this.getCaseListMem();
        myCases.setVariables(newNodes);
        int numCases = myCases.getNumberOfCases();
        if (myCases.getClass() == CaseListMem.class) {
            for (int i = 0; i < numCases; i++) {
                int[] inst = myCases.getCase(i);
                int valX = inst[x];
                inst[x] = inst[y];
                inst[y] = valX;
            }
        } else {
            ContinuousCaseListMem myContCases = (ContinuousCaseListMem)myCases;
            for (int i = 0; i < numCases; i++) {
            	double valX = myContCases.getValue(i, x);
            	myContCases.setValue(i, x, myContCases.getValue(i, y));
            	myContCases.setValue(i, y, valX);
            }
        }        
    }
    
    public int getClassId(){
    	NodeList nl = this.nodeList;
		int i = nl.getId("class");
		if(i==-1){
			i=nl.getId("Class");
		}
		if(i == -1){
			i = nl.size() -1;
		}
		return i;
	}
    
} // End of class.

