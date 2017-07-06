package elvira;

public interface ConditionalIndependence {

    boolean independents(Node x, Node y, NodeList z);
    boolean independents(Node x, Node y, NodeList z, int degree);
    boolean independents(Node x, Node y, NodeList z, double degree);
    double getDep(Node x, Node y, NodeList z);
    NodeList getNodeList();

}
