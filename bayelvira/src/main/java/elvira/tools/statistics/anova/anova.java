package elvira.tools.statistics.anova;import elvira.tools.statistics.analysis.Stat;public class anova {	int [] n;	int dfn,dfd;	double [] means;	double [] v;	double sse,ssb,F,msb,mse,omega,sst;		boolean equalN;	int k;	/**	 * Computes one-way ANOVA statistics: SST, SSB, MSB, SSE, MSE,  F, and omega squared.	 */	 	public anova() {	}	/**		* specifies the data		* @param X   Each row is the data for one group; X[0] is data for first group.		* @param _n   Number of subjects in each group (assumes equal n)		* @param k   Number of groups			*/		public void setData(double [][] X, int _n, int k) {		int n [] = new int[k];		for (int i=0; i<k;i++) {			n[i]=_n;		}		setData(X,n,k);	}		/**		*		* specifies the data		* @param X   Each row is the data for one group; X[0] is data for first group.		*	*/		public void setData(double [][] X) {		int k = X.length;		int [] n = new int[k];		for (int i=0;i<k;i++) {			n[i]=X[i].length;		}		setData(X,n,k);	}	/**		*		* specifies the data		* @param X   Each row is the data for one group; X[0] is data for first group.		* @param n[]   Number of subjects per group		* @param k   Number of groups			*		*/		public void setData(double [][] X,int [] n, int k) {		double s,ssq,GT;		this.n=n;		this.k=k;		dfn=k-1;		GT=0;		int N=0;		ssb=0;		sse=0;		equalN=true;		int lastN=n[0];		means = new double[k];		v= new double[k];		for (int i=0;i<k;i++) {			means[i]=0;			v[i]=0;			for (int j=0;j<n[i];j++) {				means[i]+=X[i][j];				v[i] += X[i][j]*X[i][j];			}			v[i] -= means[i]*means[i]/n[i];			sse+=v[i];			v[i]/= (n[i]-1);			ssb+=means[i]*means[i]/n[i];			GT+=means[i];			means[i]/=n[i];						N+=n[i];			if (n[i] != lastN) equalN=false;			lastN=n[i];		}		dfd=N-k;		//double CF = GT*GT/N;		ssb-=GT*GT/N;		sst=ssb+sse;		msb=ssb/dfn;		mse=sse/dfd;		F = msb/mse;			omega = 	(ssb - dfn*mse)/(sst+mse);	}		public double[] getMeans() {		return means;	}	public double getF() {		return F;	}	public double getSse() {		return sse;	}	public double getMse() {		return mse;	}	public double getMsb() {		return msb;	}			public double getSsb() {		return ssb;	}	public int getDfn () {		return dfn;	}			public int getDfd() {		return dfd;	}	public double getSst() {		return sst;	}	                //From page http://home.ubalt.edu/ntsbarsh/Business-stat/otherapplets/ANOVA.htm        public double getPValue(){            double prob=Stat.fTestProb(this.F,this.dfn,this.dfd);            if (prob>=0)                return prob+0.0005;            else                return prob-0.0005;         }       	/**	*	 * Returns t test for contrast	 * @param c   vector of contrast coefficients	 	 */	 	public double contrast(double [] c) {		double L=0;		double sum=0;				for (int i=0;i<k;i++) {			L+=c[i]*means[i];			sum+=c[i]*c[i];		}		double nn;		int kk;		if(equalN) nn=(double) n[0];		else {			nn=0.0;			kk=0;			for (int i=0;i<k;i++) {				if (c[i] !=0) {					kk++;					nn+=1/(double) n[i];				}			}			nn= (double) (kk)/nn;		}		return  L/Math.sqrt(sum*mse/nn);	}	public double computeRangeTest(int i, int j) {		double nn;		if (n[i]==n[j])  nn = (double) n[i];		else nn = 2.0/(1/(double) n[i] + 1/(double) n[j]);		return (means[i] - means[j])/Math.sqrt(mse/nn);	}	public double getOmegaSquared() {		return omega;	}		}