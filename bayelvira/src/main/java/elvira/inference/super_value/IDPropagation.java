package elvira.inference.super_value;

/** Basic operations to obtain the optimal strategy of the evaluation of an influence diagram with super-value nodes. */
public abstract class IDPropagation {
	public Strategy optimalStrategy;
	
	public StochasticStrategy stochasticStrategy;

	public StochasticStrategy getStochasticStrategy() {
		return stochasticStrategy;
	}

	public void setStochasticStrategy(StochasticStrategy stochasticStrategy) {
		this.stochasticStrategy = stochasticStrategy;
	}
}
