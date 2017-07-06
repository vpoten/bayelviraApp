package elvira.learning.classification.supervised.continuous;

import elvira.Elvira;
import java.util.*;
import elvira.tools.VectorManipulator;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.potential.*;

import elvira.*;
import java.util.Enumeration;
import java.util.Vector;
import elvira.database.*;
import elvira.learning.preprocessing.ProjectDBC;
import elvira.learning.MTELearning;
import elvira.inference.elimination.VariableElimination;
import elvira.inference.clustering.MTESimplePenniless;
import elvira.learning.classification.supervised.mixed.*;
import elvira.learning.classification.supervised.validation.ClassifierEvaluator;


/**
 * Class <code>TANMTEPredictor</code> 
 *  
 * @author Antonio Fernandez Alvarez (afalvarez@ual.es)
 * @author Antonio Salmeron (Antonio.Salmeron@ual.es)
 * 
 * @since 27/04/2007
 */

public class TANMTEPredictor {
    
    /**
     * Variables for which the predictor is defined.
     */
    NodeList variables;
    
    /**
     * The  of the class variable in list <code>variables</code>.
     */
    int classVariable;
    
    /**
     * The network that defined the predictor.
     */
    Bnet net;
       
    /**
     * Creates an empty instance of this class.
     */
   
    public TANMTEPredictor(){
        
        variables = new NodeList();
        classVariable = -1;
        net = new Bnet();
    }
    
    /**
     * Creates a TANMTEPredictor from a database.
     *
     * @param db the DataBaseCases from which the predictor will be constructed.
     * @param cv an int indicating the index (column) of the class variable in the database. 
     * The first column is labeled as 0.
     * @param intervals the number of intervals into which the domain of the continuous
     * variables will be split.
     * @param root_index index of the root in the tree of the features variables
     */
    
    public TANMTEPredictor(DataBaseCases db, int cv, int intervals, int root_index) {
            
        MTELearning learningObject;
        ContinuousProbabilityTree P_C, P_Xj_C, P_Xi_C, P_Xi_Xj_C, t;
        PotentialContinuousPT pot;
        Relation rel;
        Vector netRL;
        NodeList parents, relationVariables;        
        Node classVar, childVar;
        Link l;
        LinkList ll;
        Graph g;
        int i,j;
        
        Vector vMutualInformation=new Vector();
        double info,value;
        
        
        //variables = db.getVariables();
        
        variables = db.getVariables().copy();
        
        classVariable = cv;
        classVar = variables.elementAt(cv);

        learningObject= new MTELearning(db);
        
        System.out.println("------------------------------------------------------------------------");
        System.out.println("Name of DataBaseCase: " + db.getName());
        System.out.println("Class variable: " + classVar.getName());
        System.out.println("Number of variables: " + variables.size());
        System.out.print("Names of variables: "); variables.printNames();      
                
        System.out.println("\nMaking the complete graph with the feature variables ... ");
        ll=CompleteLinkList(variables);     
        
        System.out.println("\nEstimating the conditional mutual information " +
                           "for each link...\n");
        for (i=0;i<ll.size();i++){
         
          //Variable C
          parents = new NodeList();
          parents.insertNode(classVar);
          
          //Learning p(C)
          P_C=learningObject.learnConditional(classVar,new NodeList(),db,intervals,4);  
         
          //Learning p(Xi|C)    
          childVar=ll.elementAt(i).getHead();//Variable Xi       
          //For example Xi=head and Xj=tail in the Link; it's the same because
          //the links aren't still directed 
          P_Xi_C=learningObject.learnConditional(childVar,parents, db, intervals,4);
         
          //Learning p(Xj|C) 
          childVar=ll.elementAt(i).getTail();//Variable Xj
          P_Xj_C=learningObject.learnConditional(childVar,parents, db, intervals,4);
          
          //Learning p(Xi|Xj,C) 
          childVar=ll.elementAt(i).getHead();//Variable Xi
          parents.insertNode(ll.elementAt(i).getTail()); //Variable Xj
       
//          System.out.println("CHILDVAR: "+childVar.getName());
//          System.out.println("PARENTS: ");
//          parents.printNames();
                  
          P_Xi_Xj_C=learningObject.learnConditional(childVar, parents, db, intervals, 4);
       
          //Random r=new Random();
          //value=r.nextInt(9);
         //value=2.0;
          
          //Estimate I(Xi,Xj|C)
          
          //P_C.print(1);
          //P_Xj_C.print(1);
         
          //P_Xi_Xj_C.print(1);
          //System.out.println("CUT "+P_Xi_Xj_C.getCutPoints().toString());
          
         // P_Xi_C.print(1);
          
          value=ContinuousProbabilityTree.estimateConditionalMutualInformation(P_C, P_Xj_C, P_Xi_Xj_C, P_Xi_C,5000);
        //  value=(double)getRandomIndex();
          vMutualInformation.addElement(value);             
          System.out.print("\nLINK: " + ((Link)ll.getLinks().elementAt(i)).toString()); 
          System.out.println("---------------------------------------------");
          System.out.println("     �(Xi,Xj|C)="+ value);
          System.out.println("---------------------------------------------");
          
       }
          
     
       variables.removeNode(cv);
       
       
       g=new Graph();
       g.setKindOfGraph(1);
       g.setNodeList(variables);
       g.setLinkList(ll);

       
       MaximumSpanningTree MaxTree=new MaximumSpanningTree(g,vMutualInformation);
       System.out.println("\nRdo del MST:");
       MaxTree.printLinks();
       variables=db.getNodeList();
       
       //Randomly it selects a root node among the feature variables
       //int root_index=3;
       //OfertaOpt => 3
       //NotaAcceso => 8
       
       //int root_index=getRandomIndex();       
       //System.out.println("\nRoot variable selected among the feature variables: "
       //       + variables.elementAt(root_index).getName());
            
       System.out.println("\nCreating directed links among the feature variables ...");
     
       net=new Bnet();
       
       net.setNodeList(variables);

       //this.printNodesLinksTAN();  
       //this.printParents();
   
       
       MaxTree.printLinks();
        
        System.out.println("\nLink List: ");
       
       CreateDirectedLinksFeatures(MaxTree.getMST().getLinkList(), db.getNodeList().elementAt(root_index));
       
      // printNodesLinksTAN();
      // this.printParents();
       
       System.out.println("\nCreating directed links Class --> Features ...");
       CreateDirectedLinkClassFeatures();
     
       printNodesLinksTAN();
       this.printParents();
       
     //----------------------------------------------------------------------
       //----------- ENTRENAMIENTO DEL TAN ------------------------------------
       //----------------------------------------------------------------------
       netRL=new Vector();
       
       System.out.println("\n\n====> Learning TAN <=======");
       for (i=0;i<net.getNodeList().size();i++){
         if (i!=classVariable){
            
            childVar=net.getNodeList().elementAt(i);
            parents=new NodeList();
            parents=childVar.getParentNodes();
          
            System.out.println("   Learning " + childVar.getName()+" ...");
            t=learningObject.learnConditional(childVar, parents, db, intervals, 4);
            
            relationVariables=new NodeList();
            relationVariables.insertNode(childVar);
            for (j=0;j<parents.size();j++){
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
       t=learningObject.learnConditional(childVar, new NodeList(), db, intervals, 4);
       
       relationVariables=new NodeList();
       relationVariables.insertNode(classVar); 
       pot=new PotentialContinuousPT(relationVariables,t);
       rel=new Relation();
       rel.setVariables(relationVariables);    
       rel.setValues(pot); 
       netRL.addElement(rel);
      //-------------------------- 
      net.setRelationList(netRL);
      //-----------------------------------------------------------------------
    } 
   
    
     /**
     * 
     * Creates a directed link between the class variable and each feature variable.
     * The "net" variable is updated
     *
     */
    public void CreateDirectedLinkClassFeatures(){
    
     //Creo una arista dirigida desde la variable clase hasta todas las 
     //variables predictoras (n-1 variables predictoras)
            
       for (int i=0;i<variables.size();i++){
         if (i!=classVariable){
             try {
                //net.createLink(variables.elementAt(classVariable), variables.elementAt(i), true);
                 net.createLink(net.getNodeList().elementAt(classVariable), net.getNodeList().elementAt(i), true);
             }
             catch (Exception e) { System.out.println("Problems to create the link");};          
         }
       }
       
     }
    
 
    
    /**
     * Creates all the posible links between the feature variables
     *  
     * @param vars is a <code>NodeList</code> of the graph
     *
     * @return a <code>LinkList</code> 
     */     
    private LinkList CompleteLinkList(NodeList vars){
        
        LinkList list=new LinkList();
        Link l;
        
        //Making the complete graph of the features variables 
        for (int i=0;i<vars.size()-1;i++) {
            for (int j=i;j<vars.size();j++) {        
              if ((i!=j)&&(i!=classVariable)&&(j!=classVariable)){
                  //System.out.print("("+i+","+j+")");
                  //new undirected link between i and j nodes
                   l = new Link(vars.elementAt(i),vars.elementAt(j),false);                
                   list.insertLink(l);
                }
            }
        }
        return list;
      }
    
   
/**
     *
     * Creates a directed links between the features variables. Root is the 
     * first visited node (random).
     * @param LinkList ll
     * @param Node root
     *
     */ 
    private void CreateDirectedLinksFeatures(LinkList lll, Node root){
        //System.out.println("ll INICIO: "+ lll.toString());
        //System.out.println("ROOT ES:"+ root.getName());
        LinkList llaux=new LinkList();
        Node cola, cabeza;
        for (int i=0;i<lll.size();i++){    
          //  System.out.println("iinicial="+i);
            cola=lll.elementAt(i).getTail();
            cabeza=lll.elementAt(i).getHead();       
            if (cola.equals(root)){
               llaux=lll.copy();
               lll.removeLink(i);  
               //i--;
                try { 
                   // System.out.println("COLA: "+cola.getName());
                   // System.out.println("CABEZA: "+cabeza.getName());
                    net.createLink(cola, cabeza);
                    //System.out.println("net LINKS: "+ net.getLinkList().toString());
                } catch (Exception e){}; 
              // System.out.println("Llamada recursiva con: "+cabeza.getName());
               CreateDirectedLinksFeatures(lll, cabeza);
               lll=llaux;
            }            
            if (cabeza.equals(root)){      
                llaux=lll.copy();
                
                lll.removeLink(i);    
                //i--;
                try { 
                    net.createLink(cabeza, cola);
                    //System.out.println("net LINKS: "+ net.getLinkList().toString());
                } catch (Exception e){};     
                //System.out.println("Llamada recursiva con: "+cola.getName());
                CreateDirectedLinksFeatures(lll, cola);
                 lll=llaux;
                
            }
            //System.out.println("-------------------------------");
            
            //System.out.println("ll LINKS: "+ lll.toString());
            
           // System.out.println("-------------------------------");
           // System.out.println("ifinal="+i);
         }
    }  
    
    /**
     * Saves the network corresponding to the predictor in the given file.
     *
     * @param Name the name of the file where the net will be written.
     */  
    public void saveNetwork(String name) throws IOException {
        
        FileWriter f = new FileWriter(name);
        
        net.saveBnet(f);
        f.close();
    }
      /**
     * Creates a copy of the TAN model
     */
    public TANMTEPredictor copy_model(TANMTEPredictor model){
        
        TANMTEPredictor copy_model=new TANMTEPredictor();
   
        
        copy_model.classVariable=0;
        copy_model.variables=model.variables.copy();
        copy_model.net.setNodeList(model.net.getNodeList());
        copy_model.net.setRelationList(model.net.getRelationList());
        
        LinkList ll=new LinkList();
        ll.setLinks(model.net.getLinkList().getLinks());
        copy_model.net.setLinkList(ll);

        return copy_model;
    }
     
    /**
     * Return a random index between the feature variables
     *
     * @return int value
     */
    public int getRandomIndex(){
       //Con el bucle evitamos que el indice aleatorio sea la variable clase. 
       //Debemos quedarnos con una variable descriptora
       int index;
       do {
           Random r=new Random();
           index=r.nextInt(variables.size());
       } while (index==classVariable);
       return index;
    }
     
    
  
    
    
    /**
     * Computes the rooted mean squared error (RMSE) of the predicted values as well as the
     * linear correlation coefficient (LCC) between the predicted values and the exact ones.
     * @param predictedValues a vector with the predicted values.
     * @param exactValues a vector with the exact values.
     * @return a vector with the RMSE in the first position and the LCC in the second.
     */   
    public Vector computeErrors(Vector predictedValues, Vector exactValues) {

        int i, n;
        double meanPredicted = 0.0, meanExact = 0.0, x, y, sumProd = 0.0, sumCuadX = 0.0;
        double sumCuadY = 0.0, varX, varY, lcc, rmse, dif = 0.0, sx, sy, sxy;
        Vector returnVector = new Vector();
        
        n = predictedValues.size();
        
        for (i=0 ; i<n ; i++) {
            x = ((Double)predictedValues.elementAt(i)).doubleValue();
            y = ((Double)exactValues.elementAt(i)).doubleValue();
            meanPredicted += x;
            meanExact += y;
            //dif += Math.pow((x-y),2);
            dif += ((x-y)*(x-y));
            //System.out.println(y);
        }
        
        
        meanPredicted /= (double)n;
        meanExact /= (double)n;
        
        for (i=0 ; i<n ; i++) {
            x = ((Double)predictedValues.elementAt(i)).doubleValue();
            y = ((Double)exactValues.elementAt(i)).doubleValue();
            //System.out.println("Exacto "+y+" Aproximado "+x);
            //sumCuadX += Math.pow(x-meanPredicted,2);
            sumCuadX += ((x-meanPredicted)*(x-meanPredicted));
            //sumCuadY += Math.pow(y-meanExact,2);
            sumCuadY += ((y-meanExact)*(y-meanExact));
            sumProd += (x-meanPredicted) * (y-meanExact);
        }
        
        sumProd /= (double)n;
        sumCuadX /= (double)n;
        sumCuadY /= (double)n;
        
        sx = Math.sqrt(sumCuadX/(double)n);
        sy = Math.sqrt(sumCuadY/(double)n);
        sxy = sumProd / (double)n;
        
       // System.out.println("mx "+meanPredicted+" my "+meanExact+" scx "+sumCuadX+" scy "+sumCuadY);
       // System.out.println("sxy "+sxy+" sx "+sx+" sy "+sy);
        rmse = Math.sqrt(dif/(double)n);
        lcc = sxy / (sx * sy);
        
        returnVector.addElement(new Double(rmse));
        returnVector.addElement(new Double(lcc));
        
        return (returnVector);
    }
     
    /**
     * Computes the bias in the prediction.
     * @param predictedValues a vector with the predicted values.
     * @param exactValues a vector with the exact values.
     * @return the bias of the predictions.
     */ 
    public double computeBias(Vector predictedValues, Vector exactValues) {

        int i, n;
        double bias = 0.0, x, y;
        
        n = predictedValues.size();
        
        for (i=0 ; i<n ; i++) {
            x = ((Double)predictedValues.elementAt(i)).doubleValue();
            y = ((Double)exactValues.elementAt(i)).doubleValue();
            bias += (x-y);
        }
        
       
        return (bias/n);
    }
     
    /**
     * Predicts the value of the class variable for a given configuration.
     * The prediction is equal to the mean of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is given as argument.
     *
     * The propagation is  done using the Penniless method.
     *
     * @param conf the <code>ContinuousConfiguration</code> for which the
     * value of the class will be predicted.
     * @param classVar the class variable.
     * @return a vector with the predicted value in the first position (mean of the
     * posterior distribution), the variance in the second position and the exact value
     * for the class variable in the thirs position.
     */
    public Vector predictWithMean(ContinuousConfiguration conf, Node classVar) {
        
        double exactValue, mean, mode, median, variance;
        Evidence evidence;
        MTESimplePenniless propagation;
        Vector results, returnValues;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree tree;
        
        exactValue = conf.getValue((Continuous)classVar);
        
        // Remove the class variable from the configuration
        conf.remove(classVar);
        
        evidence =  new Evidence(conf);
        
        propagation = new MTESimplePenniless(net,evidence,0,0,0,0,0);
        
        // Since the only unobserved variable is the class, after the propagation,
        // in vector results the only potential will be the corresponding
        // to the posterior distribution of the class.

        //evidence.print();

        
        propagation.propagate(evidence);
        
       // try{
       //     propagation.saveResults("resultado.txt");
       // }catch(Exception e){};
        
        results = propagation.getResults();
        
        pot = (PotentialContinuousPT)results.elementAt(0);
        
        //pot.print();
        
        tree = pot.getTree();
        
        mean = tree.firstOrderMoment();
        
        variance = tree.Variance();
        
        //mode = tree.plainMonteCarloMode(5000);
        //mode = tree.gradientMonteCarloMode(50);
        median = tree.median();
        
        returnValues = new Vector();
        
        returnValues.addElement(new Double(mean));
        returnValues.addElement(new Double(variance));
        //returnValues.addElement(new Double(mode));
        returnValues.addElement(new Double(median));
        returnValues.addElement(new Double(exactValue));
        
     //   System.out.println(mean+" ; "+variance+" ; "+median+" ; "+exactValue);
        
        return (returnValues);
    }
      
    /**
     * Predicts the value of the class variable for a given configuration.
     * The prediction is equal to the mean of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is given as argument.
     *
     * The propagation is  done using the Penniless method.
     *
     * @param conf the <code>ContinuousConfiguration</code> for which the
     * value of the class will be predicted.
     * @param classVar the class variable.
     * @param bias the bias of the prediction.
     * @return a vector with the predicted value in the first position (mean of the
     * posterior distribution), the variance in the second position and the exact value
     * for the class variable in the thirs position.
     */  
    public Vector predictWithMean(ContinuousConfiguration conf, Node classVar, double bias) {
        
        double exactValue, mean, mode, median, variance;
        Evidence evidence;
        MTESimplePenniless propagation;
        Vector results, returnValues;
        PotentialContinuousPT pot;
        ContinuousProbabilityTree tree;
        
        exactValue = conf.getValue((Continuous)classVar);
        
        // Remove the class variable from the configuration
        conf.remove(classVar);
        
        evidence =  new Evidence(conf);
        
        propagation = new MTESimplePenniless(net,evidence,0,0,0,0,0);
        
        // Since the only unobserved variable is the class, after the propagation,
        // in vector results the only potential will be the corresponding
        // to the posterior distribution of the class.
        propagation.propagate(evidence);
        
        results = propagation.getResults();
        
        pot = (PotentialContinuousPT)results.elementAt(0);
        
        //pot.print();
        
        tree = pot.getTree();
        
        mean = tree.firstOrderMoment() - bias;
        
        variance = tree.Variance();
        
        //mode = tree.plainMonteCarloMode(5000);
        //mode = tree.gradientMonteCarloMode(50);
        median = tree.median() - bias;
        
        returnValues = new Vector();
        
        returnValues.addElement(new Double(mean));
        returnValues.addElement(new Double(variance));
        //returnValues.addElement(new Double(mode));
        returnValues.addElement(new Double(median));
        returnValues.addElement(new Double(exactValue));
        
      //  System.out.println(mean+" ; "+variance+" ; "+median+" ; "+exactValue);
        
        return (returnValues);
    }
     
    /**
     * Predicts the value of the class variable for all the
     * registers in a given database.
     * The prediction is equal to the mean of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is supposed to be stored in column
     * <code>classVariable</code>.
     *
     * The propagation is  done using the Penniless method.
     *
     * @param db the database with the registers to predict.
     * @return a vector with a vector in each position containing the predicted value
     * in the first position (mean of the
     * posterior distribution), the variance in the second position, tho mode in the third and the exact value
     * for the class variable in the fourth position.
     */  
    public Vector predictWithMean(DataBaseCases db) {
        
        ContinuousCaseListMem cases;
        ContinuousConfiguration conf;
        NodeList vars;
        Node classVar;
        Vector resultValues, registerValues, vectorMeans,  vectorModes, vectorVariances;
        Vector vectorExact;
        
        int i, nc;
        
        vars = db.getVariables();
     
        classVar = vars.elementAt(classVariable);
        
       // System.out.println("***************>>>>>"+classVar.getName());
        
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        resultValues = new Vector();
        vectorMeans = new Vector();
        vectorModes = new Vector();
        vectorVariances = new Vector();
        vectorExact = new Vector();
        
        nc = cases.getNumberOfCases();
       //POR QUE NO ES 0?
        for (i=1 ; i<nc ; i++) {
            conf = (ContinuousConfiguration)cases.get(i);
            //System.out.println("========= PRINT ==============");
            //conf.print();
            //System.out.println("========= CLASSVAR ==============");
            //classVar.print();

            registerValues = predictWithMean(conf,classVar);
            vectorMeans.addElement((Double)registerValues.elementAt(0));
            vectorVariances.addElement((Double)registerValues.elementAt(1));
            vectorModes.addElement((Double)registerValues.elementAt(2));
            vectorExact.addElement((Double)registerValues.elementAt(3));
        }
        
        resultValues.addElement(vectorMeans);
        resultValues.addElement(vectorVariances);
        resultValues.addElement(vectorModes);
        resultValues.addElement(vectorExact);
        
        
        return (resultValues);
    }
       
    /**
     * Predicts the value of the class variable for all the
     * registers in a given database.
     * The prediction is equal to the mean of the posterior MTE
     * distribution of the class variable.
     *
     * The class variable is supposed to be stored in column
     * <code>classVariable</code>.
     *
     * The propagation is  done using the Penniless method.
     *
     * @param db the database with the registers to predict.
     * @param bias the bias of the prediction.
     * @return a vector with a vector in each position containing the predicted value
     * in the first position (mean of the
     * posterior distribution), the variance in the second position, tho mode in the third and the exact value
     * for the class variable in the fourth position.
     */ 
    public Vector predictWithMean(DataBaseCases db, double bias) {
        
        ContinuousCaseListMem cases;
        ContinuousConfiguration conf;
        NodeList vars;
        Node classVar;
        Vector resultValues, registerValues, vectorMeans,  vectorModes, vectorVariances;
        Vector vectorExact;
        
        int i, nc;
        
        vars = db.getVariables();
        classVar = vars.elementAt(classVariable);
       // System.out.println("**********************"+ classVariable);
       // System.out.println("**********************"+ classVar.getName());
        //javax.swing.JOptionPane.showInputDialog("HOLA2");
        cases = (ContinuousCaseListMem)db.getCaseListMem();
        
        resultValues = new Vector();
        vectorMeans = new Vector();
        vectorModes = new Vector();
        vectorVariances = new Vector();
        vectorExact = new Vector();
        
        nc = cases.getNumberOfCases();
        for (i=1 ; i<nc ; i++) {
            conf = (ContinuousConfiguration)cases.get(i);
            registerValues = predictWithMean(conf,classVar,bias);
            vectorMeans.addElement((Double)registerValues.elementAt(0));
            vectorVariances.addElement((Double)registerValues.elementAt(1));
            vectorModes.addElement((Double)registerValues.elementAt(2));
            vectorExact.addElement((Double)registerValues.elementAt(3));
        }
        
        resultValues.addElement(vectorMeans);
        resultValues.addElement(vectorVariances);
        resultValues.addElement(vectorModes);
        resultValues.addElement(vectorExact);
        
        
        return (resultValues);
    }  
    public void printNodesLinksTAN(){
         
       System.out.println("\n----------------------------------------------------");
       System.out.println("                 T A N ");
       System.out.println("\nNodes: " + net.getNodeList().toString2());       
       System.out.println("\nLinks: " + net.getLinkList().size()+"\n"+ net.getLinkList().toString());
       System.out.println("----------------------------------------------------");
       System.out.println();
    }
    public void print_prob(){
        for (int i=0;i<net.getRelationList().size();i++)
        ((Relation)net.getRelationList().elementAt(i)).print();
    }  
    public void printParents(){
        System.out.print("\nPADRES DE CADA NODO\n");
       for (int i=0;i<net.getNodeList().size();i++){
           System.out.print("\nNODO: " + net.getNodeList().elementAt(i).getName()+" <--");
           for (int j=0;j<net.getNodeList().elementAt(i).getParentNodes().size();j++){
                System.out.print(" " + net.getNodeList().elementAt(i).getParentNodes().elementAt(j).getName());
           }
       }
    }   
    public void print_resultVector(Vector r){
        
            System.out.println("Means Vector:");
            for (int j=0;j<((Vector)r.elementAt(0)).size();j++){
                System.out.println(((Vector)r.elementAt(0)).elementAt(j).toString());
            }
            System.out.println("Variances Vector:");
            for (int j=0;j<((Vector)r.elementAt(1)).size();j++){
                System.out.println(((Vector)r.elementAt(1)).elementAt(j).toString());
            }
            System.out.println("Medians Vector :");
            for (int j=0;j<((Vector)r.elementAt(2)).size();j++){
                System.out.println(((Vector)r.elementAt(2)).elementAt(j).toString());
            }
            System.out.println("Exact Vector:");
            for (int j=0;j<((Vector)r.elementAt(3)).size();j++){
                System.out.println(((Vector)r.elementAt(3)).elementAt(j).toString());
            }
    }

    /**
     * Main for constructing an MTE TAN predictor or classifier from a data base.
     *
     * Arguments:
     * 1. the dbc train file.
     * 2. the dbc test file. "CV" for cross-validation.
     * 3. the index of the class variables (starting from 0).
     * 4. the number of intervals into which the domain of the continuous 
     *    variables will be split.
     * 5. In case of cross validation, the number of folds.
     */
    public static void main(String args[]) throws ParseException,IOException {
        
        FileInputStream fTrain = new FileInputStream(args[0]);
     
        int classv = Integer.valueOf(args[2]).intValue();
        int interv = Integer.valueOf(args[3]).intValue();
            
        
        
        DataBaseCases c = new DataBaseCases(fTrain);
        
        NaiveMTEPredictor auxNB= new NaiveMTEPredictor(c, classv, interv);
        System.out.println(auxNB.getListOfMututlinformation().toString());
            
        int root_index=VectorManipulator.findMaxDoubles(auxNB.getListOfMututlinformation());
            
            System.out.println("ROOT INDEX: "+ root_index);
        Node classvar=c.getVariables().elementAt(classv);
        

          if (args[1].compareTo("CV") == 0) { // Cross validation
            
            Vector results, errors;
            double bias;
            DataBaseCases dbTrain, dbTest;
            
            double lcc_mean = 0.0, lcc_median = 0.0, rmse_mean = 0.0, rmse_median = 0.0;
            int k = Integer.valueOf(args[4]).intValue(), i;
            
            for (i=0 ; i<k ; i++) {
                
                TANMTEPredictor pred=new TANMTEPredictor();
                
                
              /*  NodeList nl=c.getVariables().copy();
                nl.removeNode(15);
                nl.removeNode(14);
                nl.removeNode(13);
                nl.removeNode(12);
                nl.removeNode(11);
                
                c.projection(nl);*/
                System.out.println("ITERATION "+i);
                dbTrain = c.getTrainCV(i,k);
                dbTest = c.getTestCV(i,k);
                pred = new TANMTEPredictor(dbTrain,classv,interv, root_index); 
                
                pred.saveNetwork("temp.elv");
                
                 
                //NaiveMTEPredictor pred = new NaiveMTEPredictor(dbTrain,classv,interv);
                results = pred.predictWithMean(dbTrain);
                bias = pred.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));
                //System.out.println("BIAS:"+bias);
                results = pred.predictWithMean(dbTest,bias);
                errors = pred.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));
                rmse_mean += ((Double)(errors.elementAt(0))).doubleValue();
                lcc_mean += ((Double)(errors.elementAt(1))).doubleValue();
                System.out.println("\n\n\n===================== DATOS DEL MODELO TEMPORAL ============");
                System.out.println("MEAN --->  rmse_M: " + ((Double)(errors.elementAt(0))).doubleValue() + "   lcc_M: "+ ((Double)(errors.elementAt(1))).doubleValue());       
                
                errors = pred.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));
                rmse_median += ((Double)(errors.elementAt(0))).doubleValue();
                lcc_median += ((Double)(errors.elementAt(1))).doubleValue();
                
                System.out.println("MEDIAN ->  rmse_M: " + ((Double)(errors.elementAt(0))).doubleValue() + "   lcc_M: "+ ((Double)(errors.elementAt(1))).doubleValue());
                System.out.println("====================================================================\n\n");
              
            }     
            rmse_mean /= k;
            lcc_mean /= k;
            rmse_median /= k;
            lcc_median /= k;
            
            System.out.println("\nFinal results:");
            System.out.println("rmse_mean,lcc_mean,rmse_median,lcc_median");
            System.out.println(rmse_mean + "," + lcc_mean + "," + rmse_median + "," + lcc_median);
            System.out.println("\n");
        }
        else 
        {
            
            FileInputStream fTest = new FileInputStream(args[1]);
           
            double bias, rmse_mean, rmse_median, lcc_mean, lcc_median;
            Vector results, errors;

            DataBaseCases cTrain = new DataBaseCases(fTrain);

            TANMTEPredictor pred = new TANMTEPredictor(cTrain,classv,interv, root_index);   
            pred.saveNetwork("TAN.elv");
            
            

            results=pred.predictWithMean(cTrain); 

            bias = pred.computeBias((Vector)results.elementAt(0),(Vector)results.elementAt(3));

            DataBaseCases cTest=new DataBaseCases(fTest);

            results=pred.predictWithMean(cTest,bias); 

            errors = pred.computeErrors((Vector)results.elementAt(0),(Vector)results.elementAt(3));

            rmse_mean=((Double)(errors.elementAt(0))).doubleValue();
            lcc_mean=((Double)(errors.elementAt(1))).doubleValue();

            errors = pred.computeErrors((Vector)results.elementAt(2),(Vector)results.elementAt(3));

            rmse_median=((Double)(errors.elementAt(0))).doubleValue();
            lcc_median=((Double)(errors.elementAt(1))).doubleValue();
            
            System.out.println("\nFinal results:");
            System.out.println("rmse_mean,lcc_mean,rmse_median,lcc_median");
            System.out.println(rmse_mean + "," + lcc_mean + "," + rmse_median + "," + lcc_median);
            System.out.println("\n");
         }    
        }//End of continuous class variable
            


        
      /*
      DataBaseCases trainDBC=new DataBaseCases();
      DataBaseCases testDBC=new DataBaseCases();
      
      cTrain.divideIntoTrainAndTest(trainDBC,testDBC,0.7);
      try{
            trainDBC.saveDataBase(new FileWriter("c:\\buenas\\bupa2_train.dbc"));
            testDBC.saveDataBase(new FileWriter("c:\\buenas\\bupa2_test.dbc"));

      }catch(Exception e){};
      */
       
}    


















