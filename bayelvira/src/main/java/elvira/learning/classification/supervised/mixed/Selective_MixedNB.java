/*
 * Selective_MixedNB.java
 *
 * Created on 26 de julio de 2004, 10:13
 */

package elvira.learning.classification.supervised.mixed;

import elvira.Bnet;
import elvira.CaseListMem;
import elvira.ContinuousCaseListMem;
import elvira.Configuration;
import elvira.ContinuousConfiguration;
import elvira.FiniteStates;
import elvira.Continuous;
import elvira.Evidence;
import elvira.Link;
import elvira.LinkList;
import elvira.Node;
import elvira.potential.Potential;
import elvira.NodeList;
import elvira.Relation;
import elvira.database.DataBaseCases;
import elvira.learning.classification.supervised.validation.ClassifierDBC;
import elvira.potential.PotentialTable;
import elvira.potential.ContinuousProbabilityTree;
import elvira.potential.PotentialContinuousPT;
import elvira.potential.MixtExpDensity;
import elvira.inference.elimination.VariableElimination;
import elvira.inference.abduction.*;
import elvira.learning.*;
import elvira.learning.classification.*;
import elvira.learning.classification.supervised.mixed.MixedClassifier;
//import elvira.tools.ParameterManager;
import java.io.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.Enumeration;
import elvira.learning.classification.supervised.validation.AvancedConfusionMatrix;

/**
 * This abstract class implements a MixedClassifier with feature selection.The implemented
 * feature selection procedure is a wrapper search. It was designed to be the
 * parent of all the selective mixed classifers to implement that uses a wrapper search.
 * @author  andrew
 */
public abstract class Selective_MixedNB extends MixedClassifier{
    
    static final long serialVersionUID = -355675122002782017L;

    Vector order=new Vector();    
    
    public Vector nodeLists=new Vector();
    
    int contStop;

    public static int KEvaluation=10;
    
    public int LIMIT_STOP=2;
    
    public Vector nodeLists2=new Vector();
    
    /** Creates a new instance of Selective_MixedNB */
    public Selective_MixedNB() {
    }
    /** Creates a new instance of Selective_MixedNB */
    public Selective_MixedNB(DataBaseCases data, boolean lap, int classIndex) throws elvira.InvalidEditException{
        super(data,lap,classIndex);
    }

    
  
  /**
   *  When there are in nodeLists vector several selected variables subsets due to
   *  cross validation mehthod, then this method finds the most probable variable subset 
   *  and trains this classifier with this subset.
   */
  public void setMaximumProbabilityConfiguration(DataBaseCases dbcCases) throws Exception{
    
    
    NodeList nl = new NodeList();
    
    
/*    for (int i=0; i<nodeLists.size(); i++){
        NodeList nlTemp=(NodeList)nodeLists.elementAt(i);   
        for (int j=0; j<nlTemp.size(); j++)
            if (nl.getId(nlTemp.elementAt(j))==-1)
                nl.insertNode(nlTemp.elementAt(j));
    }
  */    

    for (int k=0; k<dbcCases.getVariables().size(); k++){
        for (int i=0; i<nodeLists.size(); i++){
            NodeList nlTemp=(NodeList)nodeLists.elementAt(i);   
            for (int j=k; j<(k+1) && j<nlTemp.size(); j++)
                if (nl.getId(nlTemp.elementAt(j))==-1)
                    nl.insertNode(nlTemp.elementAt(j));
        }
    }


    Vector nodes=new Vector();
    Vector state=new Vector();
    state.addElement("0");
    state.addElement("1");
    for  (int i=0; i<nl.size(); i++)
        nodes.addElement(new FiniteStates(nl.elementAt(i).getName(),state));

    NodeList newnl=new NodeList(nodes);
      
    CaseListMem casesL=new CaseListMem(newnl); 
    
     for (int i=0; i<nodeLists.size(); i++){
         NodeList nlTemp=(NodeList)nodeLists.elementAt(i);   
         Vector vals=new Vector();
         for (int k=0; k<nl.size(); k++)
                 if (nlTemp.getId(nl.elementAt(k))!=-1)
                    vals.addElement(new Integer(1));
                 else
                    vals.addElement(new Integer(0));                     

         Configuration conf = new Configuration(newnl.toVector(),vals);
         casesL.put(conf);
     }
     
    DataBaseCases casesDB=null; 
    try{
     DataBaseCases cases2=new DataBaseCases("GPC",newnl,casesL);
     //System.out.println("Cases2: "+cases2.getNumberOfCases());
    
     FileWriter f = new FileWriter("tmp.dbc");
     cases2.saveDataBase(f);
     f.close();
     
     FileInputStream fi = new FileInputStream("tmp.dbc");
     casesDB = new DataBaseCases(fi);
     fi.close();
     }catch(Exception e){
        System.out.println("Error .........");
     };
     
     //System.out.println("Cases: "+casesDB.getNumberOfCases());
     //casesDB.getVariables().print();

     K2Learning k2=new K2Learning(casesDB,casesDB.getVariables(),100,(Metrics) new K2Metrics(casesDB));
    k2.learning();
    DELearning outputNet3 = new DELearning(casesDB,k2.getOutput());
    outputNet3.learning();    
    double d = casesDB.getDivergenceKL(outputNet3.getOutput());
    
    NodeList nodel = k2.getOutput().getNodeList().copy();

    AbductiveInferenceNilsson ab= new AbductiveInferenceNilsson(k2.getOutput(),new Evidence(),"tables");
    ab.setExplanationSet(new NodeList());
    ab.setNExplanations(20);
    ab.setPropComment("total");
    ab.propagate("tmp.out");
    try{
        ab.saveResults("tmp.out");
    }catch(Exception e){}

    double aciertosF=Double.MAX_VALUE, aciertosP=0.0;
    int tamF=-1, tamP=-1, indiceF=-1;
    
    indiceF=0;
    for (int m=0; m<0; m++){
        
        Explanation exp = (Explanation)(ab.getKBest().elementAt(m));
        Configuration conf = exp.getConf();
        NodeList nl2=nl.copy();
        for (int i=0; i<newnl.size(); i++){
            if (conf.getValue((FiniteStates)newnl.elementAt(i))==0)
                nl2.removeNode(nl2.getId(newnl.elementAt(i).getName()));
        }

        DataBaseCases db = dbcCases.copy();//new DataBaseCases(fi);
        if (nl2.getId(this.classVar)==-1)
            nl2.insertNode(this.classVar);

        db.projection(nl2);
        
        if (nl2.size()>1){
            aciertosP=((AvancedConfusionMatrix)this.evaluationLOO(db,db.getVariables().getId(this.classVar))).getLoglikelihood();
        }else
            aciertosP=Double.MAX_VALUE;
        
        if (aciertosP<aciertosF){
            indiceF=m;
            aciertosF=aciertosP;
            tamF=tamP;
        }else if (aciertosP==aciertosF && tamF<tamP){
            indiceF=m;
            aciertosF=aciertosP;
            tamF=tamP;
        }
    }
     
    Explanation exp = (Explanation)(ab.getKBest().elementAt(indiceF));
    System.out.println("Probabilidad: "+exp.getProb());
    Configuration conf = exp.getConf();
    NodeList nl2=nl.copy();
    for (int i=0; i<newnl.size(); i++){
        if (conf.getValue((FiniteStates)newnl.elementAt(i))==0)
            nl2.removeNode(newnl.elementAt(i));
    }

    DataBaseCases train=dbcCases.copy();
    if (nl2.getId(this.classVar)==-1)
        nl2.insertNode(this.classVar);
    train.projection(nl2);
    
   
    this.cases=train;
    MixedClassifier mcl=getNewClassifier(train);
    mcl.train();
    setClassifier(mcl.getClassifier());
    this.nVariables=this.cases.getVariables().size();
    System.out.println("Fin Maximum Probability.\nNodos Seleccionados:");
    for (int i=0; i<this.cases.getVariables().size();i++)
        System.out.println("Nodo "+i+": "+this.cases.getVariables().elementAt(i).getName());
    
    this.nodeLists2.add(this.cases.getVariables().copy());
  }

  /*
   *  This method learn the structure of classifier. Moreover, it's used
   *  a wrapper method to find a minimal and optimal subset of variables.
   *  @param Vector order, a Integer vector with the order selection of the 
   *  variables.
   */ 
  
   public DataBaseCases selectiveStructuralLearning(DataBaseCases dbc, Vector order) throws Exception{

    dbc=((ClassifierDBC)dbc).anovaFilter(0.4);
       
    
       
    Vector vNodes = new Vector();
    vNodes.addElement(classVar);
    NodeList nodeList = new NodeList(vNodes); //add the class to the NodeList;

    //at the begining all the nodes are out of the classifier except the class variable
    Vector includedNodes = new Vector();
    Vector excludedNodes = new Vector();
    int indice;

    for(int i= 0; i< dbc.getVariables().size()-1; i++){
        indice=((Integer)order.elementAt(i)).intValue();
        excludedNodes.addElement(dbc.getVariables().elementAt(indice));
    }
    

    //double bestAccuracy = -Double.MAX_VALUE;
    ConfusionMatrix bestAccuracy = new ConfusionMatrix();
    DataBaseCases bestData = new DataBaseCases();
    boolean stop = false;
    this.contStop=0;
    
    while(!stop) {
      //double bestInclusionAccuracy    = -Double.MAX_VALUE;
      ConfusionMatrix bestInclusionAccuracy    = new ConfusionMatrix();
      DataBaseCases bestInclusionData = new DataBaseCases();
      Node bestInclusionNode          = new Continuous();
      //First, look for the best inclusion of a node in the model
      for(int i= 0; i< excludedNodes.size(); i++) {
        try{
        Node includedNode           = ((Node)excludedNodes.elementAt(i)).copy();
        DataBaseCases inclusionData = this.generateDbcInclude(dbc,nodeList, includedNode);
        
        int ind = inclusionData.getVariables().getId((Node)classVar);

        ConfusionMatrix inclusionAccuracy=new ConfusionMatrix();
        
        /*Gaussian_Naive_Bayes nb                = new Gaussian_Naive_Bayes(inclusionData,this.laplace,ind);
        ClassifierValidator validator = new ClassifierValidator(nb, inclusionData, ind);
        
        try{
            inclusionAccuracy      = (1 - validator.kFoldCrossValidation(10).getError());            
        }catch(Exception e){};
        */

        inclusionAccuracy= this.evaluationKFC(inclusionData,ind);
        //inclusionAccuracy= this.evaluationLOO(inclusionData,ind);
        
        
        this.evaluations ++;

        //if (inclusionAccuracy.getAccuracy() > bestInclusionAccuracy.getAccuracy()) {
        if (this.selectCondition(i,bestInclusionAccuracy,inclusionAccuracy)){
          bestInclusionAccuracy = inclusionAccuracy;
          bestInclusionData = inclusionData;
          bestInclusionNode = includedNode;
        }
        if(bestInclusionAccuracy.getAccuracy()==100.0){
            System.out.println("Hola5");
            break;
        }
        
        }catch(Exception e){ 
            if (bestInclusionAccuracy.getDimension()==0 && i==(excludedNodes.size()-1))
                bestInclusionAccuracy=bestAccuracy;
            e.printStackTrace();
            System.out.println("ERROR en evaluationKFC");
        };

      }

      if (bestData.getVariables().size()==0)
            System.out.println();
      
      
      if (!stopCondition(nodeList.size(),bestAccuracy,bestInclusionAccuracy) || includedNodes.size()==0){
            bestAccuracy = bestInclusionAccuracy;
            bestData     = bestInclusionData.copy();
            nodeList     = bestData.getNodeList().copy();
            includedNodes.addElement(bestInclusionNode);
            excludedNodes.removeElement(bestInclusionNode);
            excludedNodes=this.updateOrder(excludedNodes,nodeList,dbc);
       }else if (this.contStop>this.LIMIT_STOP){
           stop = true;
       }else{
            nodeList.insertNode(bestInclusionNode);
            includedNodes.addElement(bestInclusionNode);
            excludedNodes.removeElement(bestInclusionNode);
       }
       if (excludedNodes.size()==0){
           stop=true;
       }

       if(bestInclusionAccuracy.getAccuracy()==100.0){
           System.out.println("Hola5");
           stop=true;
       }

    }
    DataBaseCases result=dbc.copy();
    result.projection(bestData.getVariables());
    this.cases=result.copy();
    this.classifier = this.getNewClassifier(this.cases).getClassifier();
    this.nVariables=this.cases.getVariables().size();
    
    this.nodeLists.addElement(bestData.getVariables().copy());
    System.out.println("Genes Seleccionados: "+bestData.getVariables().size());
    return result;
   }
   
   public void parametricLearning(){
   }

  /**
   * generateDbcInclude returns a DataBaseCases built from a input NodeList
   * and one node. This Node isn't in the input NodeList.
   * The output DataBaseCases has one node more than the NodeList
   * @param NodeList nodeList. The current NodeList
   * @param Node node. The node to be included in data.
   */
  public DataBaseCases generateDbcInclude(DataBaseCases dbc, NodeList nodeList, Node node){
    ClassifierDBC output = new ClassifierDBC();
    output.setName(dbc.getName());
    output.setTitle(dbc.getTitle());

    //add node to the nodeList and set nodeList to the ouput DataBase
    FiniteStates classVariable =(FiniteStates)this.classVar;//nodeList.lastElement().copy();
    Vector vNodeList = new Vector();
    for(int i= 0; i< nodeList.size(); i++) //The last element is the classVariable
      if (!nodeList.elementAt(i).equals(classVariable))
        vNodeList.addElement(nodeList.elementAt(i).copy());
    
    vNodeList.addElement(node.copy());
    vNodeList.addElement(classVariable);
    NodeList newNodeList = new NodeList(vNodeList);
    output.setNodeList(newNodeList);

   	Vector      vector       = dbc.getRelationList();
  	Relation    relation     = (Relation)vector.elementAt(0);
   	CaseListMem caselistmem  = (CaseListMem)relation.getValues();
    NodeList    realNodeList = dbc.getNodeList();

    ContinuousCaseListMem newCaseList = new ContinuousCaseListMem(newNodeList);
    ContinuousConfiguration newConf   = new ContinuousConfiguration(newNodeList);

    Vector vCases = ((CaseListMem)((Relation)dbc.getRelationList().get(0)).getValues()).getCases();
    //int[] instance = new int[this.nVariables];
    //double[] instance2= new double[this.nVariables];

    //add the changed instances
    for(int l= 0; l< dbc.getNumberOfCases(); l++) {
      ContinuousConfiguration auxConf = new ContinuousConfiguration(newNodeList);
      Node n;
      int index;
      for(int j=0; j< newNodeList.size(); j++) {
        n=(Node)newNodeList.getNodes().elementAt(j);
        index=((Relation)dbc.getRelationList().get(0)).getVariables().getId(n);
        if (n.getClass()==FiniteStates.class){
            auxConf.putValue((FiniteStates)n, (int)((double[])vCases.elementAt(l))[index]);
        }else{
            auxConf.putValue((Continuous)n, ((double[])vCases.elementAt(l))[index]);
        }
            
            
      }

      newConf.setValues(auxConf.getValues());
      newConf.setContinuousValues(auxConf.getContinuousValues());
      newCaseList.put(newConf);
    }

    Vector vRelation   = new Vector();
    Relation rRelation = new Relation();

    rRelation.setVariables(newNodeList);
    rRelation.setValues(newCaseList);
    vRelation.addElement(rRelation);
    output.setRelationList(vRelation);
    output.setNumberOfCases(dbc.getNumberOfCases());

    if (dbc.getClass()==ClassifierDBC.class)
        output=new ClassifierDBC(output,output.getVariables().getId(this.classVar));
//    output.setCFM(new ContinuousFilterMeasures(output,output.getVariables().getId(classVariable)));
    return(output);
  }

public boolean selectCondition(int n, ConfusionMatrix rateBest, ConfusionMatrix rateAct){
    if (n==0)
        return true;

    double[] icB=((AvancedConfusionMatrix)rateBest).getLogLikelihoodIC(0.95);
    double[] icA=((AvancedConfusionMatrix)rateAct).getLogLikelihoodIC(0.95);


    if (icA[1]<icB[1])
        return true;
    else
        return false;

    //if (rateAct.getLoglikelihood()<rateBest.getLoglikelihood())
    /*if (rateAct.getError()<rateBest.getError())
        return true;
    else
        return false;*/
    
}
public boolean stopCondition(int n, ConfusionMatrix rate1, ConfusionMatrix rate2){
    if (n<=1)
        return false;
    
    double[] icB=((AvancedConfusionMatrix)rate1).getLogLikelihoodIC(0.95);
    double[] icA=((AvancedConfusionMatrix)rate2).getLogLikelihoodIC(0.95);

    System.out.println("ICA: "+icB[1]+", "+icA[1]+", "+this.contStop+", "+rate1.getAccuracy());    
    //if (icA[1]<icB[1]){
    //if (rate1.getLoglikelihood()>rate2.getLoglikelihood()){
    if (rate1.getError()>rate2.getError()){
        this.contStop=0;
        return false;
    }else{
        this.contStop++;
        return true;
    }
}
  
/*
 * This method set the stop condition for the wrapper method implemented in
 * selectiveStructuralLearning method.
 * @param int n, number of nodes selected actually.
 * @param double rate1, the previous accuracy train rate.
 * @param double rate2, the current accuracy train rate.
 */

  public boolean stopCondition(int n, double rate1, double rate2){
      
      double incRate=rate2-rate1;
      int n1=15;
      int n2=20;
      double u1=0.1;
      
      double u2=0.95;
      
      if ((incRate>(n-n1)*(u1/(n2-n1))) && (rate2<(n*((u2-1)/n2)+1)))
          return false;
      else
         return true;
  }
  
  public void structuralLearning() throws elvira.InvalidEditException, Exception {
/*    
      this.nodeLists=new Vector();
      for (int j=0; j<1;j++){
         ClassifierValidator cv = new ClassifierValidator(new Selective_MixedNB(),this.cases.copy(),this.cases.getVariables().getId(this.classVar));
          cv.splitCases(4);
          Vector subDBCs=cv.getSubSetsDbc();
          for (int i=0; i<subDBCs.size(); i++){
              DataBaseCases data=cv.mergeCases(i).copy();
              //DataBaseCases data = (DataBaseCases)subDBCs.elementAt(i);
              this.selectiveStructuralLearning(data,this.getOrder(data));
          }
      }
      this.setMaximumProbabilityConfiguration(this.cases);
 */
      //((ClassifierDBC)this.cases).filter1(1000);
      //if (ParameterManager.getParameter(this, 3)!=-1.0)
      //     this.cases=((ClassifierDBC)this.cases).anovaFilter(ParameterManager.getParameter(this,3));

      System.out.println("Structural");
      if (this.cases.getClass()!=ClassifierDBC.class)
          this.cases=new ClassifierDBC(this.cases, this.cases.getVariables().getId(this.classVar));
      this.selectiveStructuralLearning(this.cases,this.getOrder());
  }
  
   public Vector updateOrder(Vector antOrder,NodeList includedNodes,DataBaseCases dbc){
      return antOrder;
   }

   public Vector getOrder(DataBaseCases data){
        //System.out.println("order:"+data.getVariables().getId(this.classVar));
        ClassifierDBC cfm=new ClassifierDBC(data,data.getVariables().getId(this.classVar));
        NodeList nl = cfm.getSortAnovaNodes().getNodeList();
        Vector order=new Vector();
        for (int i=0; i<nl.size(); i++)
              order.addElement(new Integer(data.getVariables().getId(nl.elementAt(i))));   

        return order;
    }
    
    public Vector getOrder(){
        NodeList nl = this.getClassifierDBC().getSortAnovaNodes().getNodeList();
        Vector order=new Vector();
        for (int i=0; i<nl.size(); i++)
              order.addElement(new Integer(this.cases.getVariables().getId(nl.elementAt(i))));   

        return order;
    }

      public DataBaseCases testDBCPreprocessing(DataBaseCases data){
          data.projection(this.cases.getVariables());  
          return this.getNewClassifier(this.cases).testDBCPreprocessing(data);
      }

}
