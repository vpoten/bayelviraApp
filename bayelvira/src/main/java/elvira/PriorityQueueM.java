/* PriorityQueueM */

package elvira;

import elvira.NodeQueueM;

/**
 * Implements a binary heap for elements of
 * class <code>NodeQueueM</code>.
 *
 * @since 22/9/2000
 */

public class PriorityQueueM {

  
/**
 * Number of elements in the queue.
 */
private int currentSize;

/**
 * The heap array.
 */
private NodeQueueM [] array;

/**
 * Default capacity.
 */
private static final int DEFAULT_CAPACITY = 11;


/**
 * Creates a new <code>PriorityQueueM</code>.
 * @param infinity a value grater than any other one.
 */

public PriorityQueueM(NodeQueueM infinity) {
  
  currentSize = 0;
  getArray(DEFAULT_CAPACITY);
  array[0] = infinity;
}
  

/**
 * Inserts a node into the queue keeping order. Duplicates are allowed.
 * @param x the element to insert.
 */

public void insert(NodeQueueM x) {
  
  checkSize();
  
  int hole = ++currentSize;
  for ( ; x.greaterThan(array[hole/2]); hole /=2)
    array[hole] = array[hole/2];
  array[hole] = x;
}


/**
 * Finds the item with highest priority.
 * @return the highest priority item.
 */

public NodeQueueM findMax() {
  
  return array[1];
}


/**
 * Removes the element with highest priority.
 */

public NodeQueueM deleteMax() {
  
  NodeQueueM maxItem = findMax();
  
  array[1] = array[currentSize--];
  percolateDown(1);
  
  return maxItem;
}


/**
 * Detects whether the queue is empty.
 * @return <code>true</code> if the queue is empty,
 * <code>false</code> otherwise.
 */

public boolean isEmpty() {
  
  return currentSize == 0;
}



/**
 * Makes the queue be empty.
 */

public void makeEmpty() {
  
  currentSize = 0;
}


/**
 * Gives the number of elements in the heap.
 * @return the number of elements in the queue.
 */

public int size() {
  
  return currentSize;
}


/**
 * Allocates the binary heap array.
 * Includes an extra cell for the centinel (infinity).
 * @param newMaxSize the capacity of the heap.
 */

private void getArray(int newMaxSize) {
  
  array = new NodeQueueM[newMaxSize+1];
}


/**
 * Private method that doubles the heap array if full.
 */

private void checkSize() {
  
  if (currentSize == array.length -1) {
    NodeQueueM [] oldArray = array;
    
    getArray(currentSize * 2);
    for (int i = 0 ; i < oldArray.length ; i++)
      array[i] = oldArray[i];
  }
}


/**
 * Internal method to percolate down in the tree.
 * @param hole the index at which the percolate begins.
 */

private void percolateDown(int hole) {
  
  int child;
  NodeQueueM tmp = array[hole];
  
  for ( ; hole * 2 <= currentSize ; hole = child) {
    child = hole * 2;
    if (child != currentSize && array[child+1].greaterThan(array[child]))
      child++;
    if (array[child].greaterThan(tmp))
      array[hole] = array[child];
    else
      break;
  }
  array[hole] = tmp;
}

} // End of class.