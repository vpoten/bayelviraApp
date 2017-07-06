package elvira;

/** This class is defined in order to prevent the user to make some possibles mistakes
  * while he/she is building a graph or, particularly a bayesian network,
  * such as trying to generate a node or a link that already exists in the graph.
  * Since there can be multiples kind of edition errors, the constructor of the class
  * will be called with the kind of error that is held at each moment
  * The class can be augmented with other possible edition mistakes

  @ param number that defines the kind of mistake it could be produced
 */

public class InvalidEditException extends Exception{
    public static final int DUPLICATEDNODE=0;
    public static final int DUPLICATEDLINK=1;
    public static final int DELETENODE=2;
    public static final int INVALIDLINK=3;
    public static final int DELETELINK=4;
    public static final int LINKDAG=5;

    private int code;

    public InvalidEditException(int kind){
      super("Edition Exception");
      code = kind;
      switch (kind){
        case DUPLICATEDNODE: System.out.println("There is already a node equals to this one");
                             break;
        case DUPLICATEDLINK: System.out.println("There is already a link equals to this one");
                             break;
        case DELETENODE: System.out.println("The node to delete doesn't exist in the graph");
                         break;
        case INVALIDLINK: System.out.println("This kind of link doesn't agree with the kind of graph");
                          break;
        case DELETELINK: System.out.println("The link to delete doesn't exist in the graph");
                         break;
        case LINKDAG :   System.out.println("This link doesn't agree with acyclic directed graph");
                          break;


      }
    }

    public int getCode(){
      return code;
    }
}
