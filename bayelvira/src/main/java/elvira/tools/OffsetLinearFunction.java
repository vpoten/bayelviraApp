package elvira.tools;
import elvira.Continuous;
import java.util.Vector;

/**
 * Implement a linear function with offset, i.e.
 * a + a_1 x_1 + ... + a_n x_n
 *
 * @since Apr. 29 2013
 * @author asc
 */
public class OffsetLinearFunction {

    // The offset of the function
    double offset;
    // The part of the function that involves variables
    LinearFunction lf;

    /**
     * Creates an empty <code>OffsetLinearFunction</code>.
     */
    public OffsetLinearFunction() {

        offset = 0.0;
        lf = new LinearFunction();
    }

    /**
     * Creates an <code>OffsetLinearFunction</code>
     * for the given arguments.
     * @param o the offset
     * @param f the LinearFunction
     */
    public OffsetLinearFunction(double o, LinearFunction f) {

        offset = o;
        lf = f;
    }


    public LinearFunction getLinearFunction() {
        return(lf);
    }

    public double getOffset() {
        return(offset);
    }

    public void setLinearFunction(LinearFunction l) {
        lf = l;
    }

    public void setOffset(double o) {
        offset = o;
    }

    /**
     * Isolates a variable from a linear equation.
     * For instance, if we have a = b X + c Y, the result of
     * isolating X will be a/b = c/b Y
     * It is used to substitute X in other equations.
     * @param x the variable to isolate
     * @return the OffsetLinearFunction after isolating X
     */
    public OffsetLinearFunction isolate(Continuous x) {

        OffsetLinearFunction isolated = new OffsetLinearFunction();

        LinearFunction f = (LinearFunction)this.getLinearFunction().duplicate();

        int posX = f.indexOf(x);
        double coeffX = f.getCoefficient(posX);

        f.removeVariable(x);
        f.multiply(1.0/coeffX);

        isolated.setOffset(this.offset / coeffX);
        isolated.setLinearFunction(f);

        return (isolated);
    }

    /**
     * Replaces a variable by a linear function. The object is modified.
     *
     *
     */

    public void replace(Continuous x, OffsetLinearFunction f) {

        int posX = lf.indexOf(x);

        double coeffX = lf.getCoefficient(posX);

        this.setOffset(offset - f.getOffset());

        lf.removeVariable(x);

        LinearFunction temp = f.getLinearFunction();
        temp.multiply(coeffX);



        lf = (LinearFunction)lf.sumFunctions(temp);

        lf.simplifyT();
    }
}
