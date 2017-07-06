package elvira.gui.explication;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JTable;

import elvira.Bnet;
import elvira.Evidence;
import elvira.FiniteStates;
import elvira.Node;
import elvira.NodeList;
import elvira.tools.DSeparation;

public class Sensitive_Group {

	private String variable;
	private String variable_interes;
	private String [] hallazgos;
	private Node nodo_var;// Nodo de la variable pasada al constructor
	private Node nodo_var_i; // Nodo de la variable de interes
	private boolean es_sensible;
	private Bnet red;
	private ArrayList <String> aux_interes= new ArrayList<String>();
	private ArrayList <String> aux_variable= new ArrayList<String>();
	private ArrayList <String> aux_variable_descendientes= new ArrayList<String>();
	private int contador;
	private JTable evidencia;
	//private Case actual;
	
	
		//Constructor
	public  Sensitive_Group(String var,Bnet net,String var_i,String [] obs,JTable tabla) throws Throwable{
		variable=var;
		variable_interes=var_i;
		red=net.copyBnetIncludingRelations();
		contador=0;
		es_sensible=false;
		evidencia=tabla;
		for(int h=0;h<obs.length;h++){
			if(obs[h]!=null){
			contador++;
			}
		}
		hallazgos= new String[contador];
		for(int h=0;h<obs.length;h++){
			if(obs[h]!=null){
				hallazgos[h]=obs[h];
			}
		}
		
	}
	
	public boolean Es_Sensible(){
		NodeList padres_variable;
		red.compile(0,null,null,null);
		Case actual=new Case(red);
		
		for(int i=0;i<hallazgos.length;i++){
		FiniteStates fm= (FiniteStates)red.getNodeList().getNodeString(hallazgos[i],true);
		actual.setAsFinding(fm, (Integer)evidencia.getValueAt(i,2));
		}
		actual.propagate();		
		Evidence evidencia_actual= actual.getEvidence();//Es la evidencia del caso actual
		System.out.println("Evidencia en el caso"+evidencia_actual.toString());
		DSeparation d_s= new DSeparation(red,evidencia_actual);//Objeto de "D-Separation" para implementar metodos de "Separacion-d"
		
		nodo_var=red.getNodeList().getNodeString(variable,true);//Nodo que se le pasa por Analisis1
		nodo_var_i=red.getNodeList().getNodeString(variable_interes,true);//Nodo de interés
		
		aux_interes=nodo_var_i.getAllParents2();//Contiene antecesores de la variable de interés
		aux_interes.add(nodo_var_i.getNodeString(true));//Unimos la variable de interes a los antecesores de ésta
		
		padres_variable=nodo_var.getParentNodes(); //Contiene los padres de la variable pasada desde analisis1
		for(int p=0;p<padres_variable.size();p++){
			aux_variable.add(padres_variable.elementAt(p).getNodeString(true));
		}
		aux_variable.add(nodo_var.getNodeString(true));//Es la union de esta variable con sus padres
		
		aux_variable_descendientes=nodo_var.getAllChildren2();//Contiene descendientes de la variable pasada al constructor desde Analisis1
		aux_variable_descendientes.add(nodo_var.getNodeString(true));//Unimos la variable pasada desde al analisis1 a sus correspondientes descendientes
		
		
		if(Insen1(d_s)==true){
		
			return es_sensible=true;//El nodo es sensible
		}	
		
		if(Insen2(d_s)==true){
				if(Insen3(d_s)==true){
					return es_sensible=true;//Si es_sensible==true: nodo_var ,ésta pertenece al conjunto sensible
				}else{
					return es_sensible=false;		
				}
		}		
		return es_sensible=false;
	}
	
	//Metodo-//REGLA DEFINIDA 1 conjunto Insen1(Vr,O)
	public boolean Insen1(DSeparation d_s){
	 Vector var_afectadas= new Vector();//Almacena las variables que afectan a la variable procesada
	 boolean es_sensible=false;	
		for(int i=0;i<aux_interes.size();i++){
			if(nodo_var.getNodeString(true).compareTo(aux_interes.get(i))==0){ //¿Pertenece la variable a los antecesores de la variable de interés?
				var_afectadas = d_s.allAffecting(nodo_var_i);//Devuelve un vector con los nodos que afectan a la variable
				///////////
				System.out.println("VARIABLES AFECTADAS POR "+ nodo_var_i.getName());
				System.out.println(var_afectadas.toString());
				///////////////////////////////
				for(int f=0;f<var_afectadas.size();f++){
					System.out.println(var_afectadas.get(f).toString());
					if(nodo_var.getNodeString(true).compareTo(var_afectadas.get(f).toString())==0){
						System.out.println("¿paso por aqui 1_1?  "+ nodo_var.getNodeString(true)+" "+ "true");
						return es_sensible=true; //La variable NO esta en Insen1
					}else{
						System.out.println("¿paso por aqui 1_2?  "+ nodo_var.getNodeString(true)+" "+ "false");
						 es_sensible=false;// La variable esta en Insen1
					}
				}
			}
		
		}
		System.out.println("¿paso por aqui 1_3?  "+ nodo_var.getNodeString(true)+" "+ es_sensible);
		return es_sensible;//La variable no esta en Insen1 pero puede estar en Insen2 o Insen3
	}	
	
//	Metodo-//REGLA DEFINIDA 2 conjunto Insen2(Vr,O)
	public boolean Insen2(DSeparation d_s){
		boolean es_sensible=true;
		Vector var_afectadas= new Vector();
		for(int i=0;i<aux_interes.size();i++){
			if(!(nodo_var.getNodeString(true).compareTo(aux_interes.get(i))==0)){//¿No pertenece la variable a los antecesores del nodo de interés?
				var_afectadas = d_s.allAffecting(nodo_var_i);//Devuelve un vector con los nodos que se ven afectados
				for(int f=0;f<var_afectadas.size();f++){
					if(!(nodo_var.getNodeString(true).compareTo(var_afectadas.get(f).toString())==0)){//Se cumple el criterio Separacion-D
						for(int g=0;g<aux_variable_descendientes.size();g++){
							for(int j=0;j<hallazgos.length;j++){
								if((aux_variable_descendientes.get(g).compareTo(hallazgos[j])==0)){
									System.out.println("¿paso por aqui 2_1?  "+ nodo_var.getNodeString(true)+" "+ "false");
									return es_sensible=false;//La variable esta en Insen2 
								}else{
									System.out.println("¿paso por aqui 2_2?  "+ nodo_var.getNodeString(true)+" "+ "true");
									return es_sensible=true;//No esta en Insen2 pero puede estar en Insen3
								}
							}
							
						}
					}
				}
			}
		}	
		System.out.println("¿paso por aqui 2_3?  "+ nodo_var.getNodeString(true)+" "+ es_sensible);
		return es_sensible;
	}
	
	
//	Metodo-//REGLA DEFINIDA 3 conjunto Insen3(Vr,O)
	public boolean Insen3(DSeparation d_s){
		boolean es_sensible=true;
		for(int i=0;i<aux_interes.size();i++){
			if(!(nodo_var.getNodeString(true).compareTo(aux_interes.get(i))==0)){
				for(int f=0;f<aux_variable_descendientes.size();f++){
					for(int y=0;y<hallazgos.length;y++){
						if(aux_variable_descendientes.get(f).compareTo(hallazgos[y])==0){
							System.out.println("¿paso por aqui 3_1?  "+ nodo_var.getNodeString(true)+" "+ "true");
							return es_sensible= true;//La variable NO esta en Insen3, esta variable pertence al conjunto sensible
						}else{
							System.out.println("¿paso por aqui 3_2?  "+ nodo_var.getNodeString(true)+" "+ "false");
							return es_sensible=false;// La variable esta en Insen3
						}
					}
				}
			}
		}	
		System.out.println("¿paso por aqui 3_3?  "+ nodo_var.getNodeString(true) + " "+ es_sensible);
		return es_sensible;
	}
}	
	
