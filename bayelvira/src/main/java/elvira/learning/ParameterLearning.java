package elvira.learning;

import elvira.*;
import elvira.learning.*;
import elvira.database.*;
import elvira.parser.*;
import java.io.*;

public abstract class ParameterLearning extends Learning {


    public static void main(String args[]) throws ParseException, IOException{ 
      if(args.length < 3){
        System.out.println("Too few arguments. Usage: file.dbc file.elv output.elv");
        System.exit(0);
      }
      else{
        FileInputStream f = new FileInputStream(args[0]);
        DataBaseCases cases = new DataBaseCases(f);
        System.out.println(cases.getNodeList().toString2());
        //try{
           System.out.println("------------> Variables de la base de datos");
           System.out.println("Pulse una tecla ...");
           System.in.read();
        //}catch(IOException e){};

        FileInputStream fnet = new FileInputStream(args[1]);
        Bnet bnet = new Bnet(fnet);
        System.out.println(bnet.getNodeList().toString2());
        //try{
           System.out.println("------------> Variables de la red (deben ser las mismas que la base de datos...");
           System.out.println("Pulse una tecla ...");
           System.in.read();
        //}catch(IOException e){};

        DELearning output = new DELearning(cases,bnet);
        output.learning();
        FileWriter f2 = new FileWriter(args[2]);
        output.getOutput().saveBnet(f2);
        f2.close();              
      }
    }




private DataBaseCases input;



public DataBaseCases getInput(){

    return input;

}

public void setInput(DataBaseCases input){

    this.input = input;

}

public void learning(){

}

}