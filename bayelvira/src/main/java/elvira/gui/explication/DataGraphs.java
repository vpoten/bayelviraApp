package elvira.gui.explication;

public class DataGraphs {

	private double prob_previa=0.0;
	private double prob_posterior=0.0;
	private double para_inicial=0.0;
	private double para_final=0.0;
	
	public DataGraphs(){
		prob_previa=0.0;
		prob_posterior=0.0;
		para_inicial=0.0;
		para_final=0.0;
	}
	public DataGraphs(double parini,double parfin,double ppr,double pos){
		para_inicial=(parini*200)+220; //eje X//134 y 40
		para_final=(parfin*200)+220;//eje X
		prob_previa=250-(ppr*200);//eje Y //210 y 134
		prob_posterior=250 -(pos*200);// eje Y
		
	}
	public double getProbPrevia(){// en Pixeles
		return prob_previa;
	}
	public double getProbPosterior(){//En Pixeles
		return prob_posterior;
	}
	public double getParaInicial(){//En Pixeles
	return para_inicial;
	}
	public double getParaFinal(){//En Pixeles
		return para_final;
	}
	
	
	public double getPInicial(){//valor real
		double aux=para_inicial;
		aux=aux-220;
		aux=aux/200;
		return aux;
	}
	public double getPFinal(){//valor real
		double aux=para_final;
		aux=(aux-220)/200;
		return aux;
	}
	public double getPPrevia(){//valor real
		double aux=prob_previa;
		aux=(250-aux)/200;
		return aux;
	}
	public double getPPosterior(){//valor real
		double aux=prob_posterior;
		aux=(250-aux)/200;
		return aux;
	}
	
}	
