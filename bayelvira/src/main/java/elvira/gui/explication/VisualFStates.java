/* VisualFStates.java */
package elvira.gui.explication;import java.util.Vector;

/* Clase para representar gráficamente un estado de un nodo */
public class VisualFStates extends Vector{
   /*cada estado estará representado visualmente por una lista de "barras" visuales */

    private int numvfstates; //número de "barras" asociadas a ese estado
    public static final int distestados=5;

    public VisualFStates (){
        super();
        numvfstates=0;
    }

    public void addVfstatesdist(VisualFStatesDistribution vfsd){
        addElement(vfsd);
        numvfstates=size();
    }

    public int posVfstatesdist(VisualFStatesDistribution vfsd){
        return indexOf(vfsd);
    }

    public void removeVfstatesdist(VisualFStatesDistribution vfsd){
        if (posVfstatesdist(vfsd)!=-1)
            removeElement(vfsd);
            numvfstates=numvfstates-1;
    }

    public int getanchura(){
        if (size()==1)
            return 25;
            else return (size()*(VisualFStatesDistribution.weight)+
                        ((size()-1)*(VisualFStatesDistribution.weight))+
                        (distestados-VisualFStatesDistribution.weight));
    }

    public int numVfstetesdist(){
        return numvfstates;
    }
}
