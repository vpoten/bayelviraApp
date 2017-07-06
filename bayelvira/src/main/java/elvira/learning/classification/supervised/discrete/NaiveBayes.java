/*
 * Class.java
 *
 * Created on 18 /02/2003, 10:13
 */
package elvira.learning.classification.supervised.discrete;

import elvira.Elvira;
import java.util.*;
import java.io.*;
import elvira.database.DataBaseCases;
import elvira.parser.ParseException;
import elvira.potential.PotentialTable;
import elvira.potential.*;

import elvira.*;
import java.util.Enumeration;
import elvira.database.*;

/**
 *
 * @author  fsoler
 */
public class NaiveBayes {
    
    public Bnet net;
    
    public NaiveBayes(){
        net = new Bnet();
    }
    
    /** Creates a new instance of Class */
    public static void main(String args[]) throws ParseException, IOException {
        
        
        
        //If the number of arguments is incorrect
        //Arguments: file.dbc, variable a clasificar, file.elv
        
        if((args.length < 3)){
            System.out.println("too few arguments: Usage: file.dbc variable file.elv ");
            System.exit(0);
        }
        
        else if((args.length > 3)){
            System.out.println("too much arguments: Usage: file.dbc variable file.elv ");
            System.exit(0);
        }
        
        
        
        Date initialTime=new Date();
        
        
        //Getting the dbc file
        FileInputStream f = new FileInputStream(args[0]);
        //Getting the databasecases from dbc file
        DataBaseCases cases = new DataBaseCases(f);
        //The final net
        Bnet naivebayesnet = new Bnet();
        //File where the net will be saved
        FileWriter f2;
        f2 = new FileWriter(args[2]);
        
        //Making the node list
        NodeList nodelist=new NodeList();
        nodelist=cases.getNodeList();//Getting the nodes from the dbc
        //Making the NaiveBayes nodelist
        LinkList linklist=new LinkList();
        
        //Assigning the nodes to the net
        naivebayesnet.setNodeList(nodelist);
        
        Relation relation=new Relation();
        
        Link l;
        
        int nodetoclasify= Integer.parseInt(args[1]);
        
        Node nod=nodelist.elementAt(nodetoclasify);
        String name=nod.getName();
        System.out.println("");
        System.out.println("----------------------------------------------------------------------------");
        System.out.println("Wait...making NaiveBayes classifier with variable "+name);
        System.out.println("----------------------------------------------------------------------------");
        System.out.println("Node list: ");
        //Making the links
        for(int i=0;i<nodelist.size();i++) {
            
            if(nodetoclasify != i)//No link with itself
            {
                l=new Link(nodelist.elementAt(nodetoclasify),nodelist.elementAt(i));
                linklist.insertLink(l);
            }
        }
        
        //Setting the new link list
        naivebayesnet.setLinkList(linklist);
        
        
        
        
        int i;
        
        PotentialTable potential;
        FiniteStates nodei;
        NodeList vars,varsDb,pa;
        
        
        naivebayesnet.getRelationList().removeAllElements();
        for(i=0 ; i< naivebayesnet.getNodeList().size() ; i++){
            nodei = (FiniteStates) naivebayesnet.getNodeList().elementAt(i);
            pa=new NodeList();
            
            
            pa.insertNode(nodelist.elementAt(nodetoclasify));
            
            vars = new NodeList();
            vars.insertNode(nodei);
            
            
            vars.join(pa);
            
            relation = new Relation(vars.toVector());
            varsDb = cases.getNodeList().intersectionNames(vars).sortNames(vars);
            potential = cases.getPotentialTable(varsDb);
            potential.LPNormalize();
            
            if(vars.size()>1){
                nodei = (FiniteStates) varsDb.elementAt(0);
                potential =(PotentialTable) potential.divide(potential.addVariable(nodei));
            }
            potential.setVariables(vars.toVector());
            relation.setValues(potential);
            naivebayesnet.getRelationList().addElement(relation);
            
        }
        
        
        naivebayesnet.saveBnet(f2);
        
        f2.close();
        
        Date finalTime=new Date();
        long time=finalTime.getTime()-initialTime.getTime();
        
        System.out.println("----------------------------------------------------------------------------");
        System.out.println("Success: Output file '"+args[2]+"' in: "+time+"ms");
        System.out.println("----------------------------------------------------------------------------");
        
        
        
        
        
        
        
        
    }
    
    
    
    public Bnet Naive(DataBaseCases archivoDBC, int vClasificar) {
        
        Date initialTime=new Date();
        
        
        
        
        DataBaseCases cases = new DataBaseCases();
        cases=archivoDBC;
        //The final net
        Bnet naivebayesnet = new Bnet();
        
        
        
        //Making the node list
        NodeList nodelist=new NodeList();
        nodelist=cases.getNodeList();//Getting the nodes from the dbc
        //Making the NaiveBayes nodelist
        LinkList linklist=new LinkList();
        
        //Assigning the nodes to the net
        naivebayesnet.setNodeList(nodelist);
        
        Relation relation=new Relation();
        
        Link l;
        
        
        int nodetoclasify= vClasificar;
        
        Node nod=nodelist.elementAt(nodetoclasify);
        String name=nod.getName();
        
        System.out.println("Wait...making NaiveBayes classifier with variable "+name);
        
        
        //Making the links
        for(int i=0;i<nodelist.size();i++) {
            
            if(nodetoclasify != i)//No link with itself
            {
                l=new Link(nodelist.elementAt(nodetoclasify),nodelist.elementAt(i));
                linklist.insertLink(l);
            }
        }
        
        //Setting the new link list
        naivebayesnet.setLinkList(linklist);
        
        
        
        
        int i;
        
        PotentialTable potential;
        FiniteStates nodei;
        NodeList vars,varsDb,pa;
        
        
        naivebayesnet.getRelationList().removeAllElements();
        for(i=0 ; i< naivebayesnet.getNodeList().size() ; i++){
            nodei = (FiniteStates) naivebayesnet.getNodeList().elementAt(i);
            pa=new NodeList();
            
            pa.insertNode(nodelist.elementAt(nodetoclasify));
            
            vars = new NodeList();
            vars.insertNode(nodei);
            
            vars.join(pa);
            
            relation = new Relation(vars.toVector());
            varsDb = cases.getNodeList().intersectionNames(vars).sortNames(vars);
            potential = cases.getPotentialTable(varsDb);
            potential.LPNormalize();
            
            if(vars.size()>1){
                nodei = (FiniteStates) varsDb.elementAt(0);
                potential =(PotentialTable) potential.divide(potential.addVariable(nodei));
            }
            potential.setVariables(vars.toVector());
            relation.setValues(potential);
            naivebayesnet.getRelationList().addElement(relation);
            
        }
        
        
        
        Date finalTime=new Date();
        long time=finalTime.getTime()-initialTime.getTime();
        
        System.out.println("PROCESADO: Archivo de salida  en un time de: "+time+"ms");
        return naivebayesnet;
        
        
    }
    
    
    
    
}
