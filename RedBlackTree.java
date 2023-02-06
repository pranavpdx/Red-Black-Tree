// --== CS400 Fall 2022 File Header Information ==--
// Name: Pranav Sharma
// Email: pnsharma@wisc.edu
// Team: DS red
// TA: 
// Lecturer: Florian 
// Notes to Grader: 
/*** JUnit imports ***/
// We will use the BeforeEach and Test annotation types to mark methods in
// our test class.
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// The Assertions class that we import from here includes assertion methods like assertEquals()
// which we will used in test1000Inserts().
import static org.junit.jupiter.api.Assertions.assertEquals;
// More details on each of the imported elements can be found here:
// https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/package-summary.html
/*** JUnit imports end ***/

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Stack;

/**
 * Red-Black Tree implementation with a Node inner class for representing the nodes of the tree.
 * Currently, this implements a Binary Search Tree that we will turn into a red black tree by
 * modifying the insert functionality. In this activity, we will start with implementing rotations
 * for the binary search tree insert algorithm. You can use this class' insert method to build a
 * regular binary search tree, and its toString method to display a level-order traversal of the
 * tree.
 */
public class RedBlackTree<T extends Comparable<T>> {

  /**
   * This class represents a node holding a single value within a binary tree the parent, left, and
   * right child references are always maintained.
   */
  protected static class Node<T> {
    public int blackHeight;
    public T data;
    public Node<T> parent; // null for root node
    public Node<T> leftChild;
    public Node<T> rightChild;

    public Node(T data) {
      this.data = data;
      blackHeight = 0;
    }

    /**
     * @return true when this node has a parent and is the left child of that parent, otherwise
     *         return false
     */
    public boolean isLeftChild() {
      return parent != null && parent.leftChild == this;
    }

  }

  protected Node<T> root; // reference to root node of tree, null when empty
  protected int size = 0; // the number of values in the tree

  /**
   * Performs a naive insertion into a binary search tree: adding the input data value to a new node
   * in a leaf position within the tree. After this insertion, no attempt is made to restructure or
   * balance the tree. This tree will not hold null references, nor duplicate data values.
   * 
   * @param data to be added into this binary search tree
   * @return true if the value was inserted, false if not
   * @throws NullPointerException     when the provided data argument is null
   * @throws IllegalArgumentException when the newNode and subtree contain equal data references
   */
  public boolean insert(T data) throws NullPointerException, IllegalArgumentException {
    // null references cannot be stored within this tree
    if (data == null)
      throw new NullPointerException("This RedBlackTree cannot store null references.");

    Node<T> newNode = new Node<>(data);
    if (root == null) {
      root = newNode;
      size++;
      this.root.blackHeight = 1;
      return true;
    } // add first node to an empty tree
    else {
      boolean returnValue = insertHelper(newNode, root); // recursively insert into subtree
      if (returnValue)
        size++;
      else
        throw new IllegalArgumentException("This RedBlackTree already contains that value.");
      this.root.blackHeight = 1;
      return returnValue;
    }
  }

  /**
   * Recurisvely moves through the path of the node added to fix any red black tree violations
   * 
   * @param node the node that has been added to the tree
   */
  protected void enforceRBTreePropertiesAfterInsert(Node<T> node) {
    // if the parent is null then this is the root
    if (node.parent == null) {
      node.blackHeight = 1;
      return;

      // if grandfather is null we know parent is root and we make it black
    } else if (node.parent.parent == null) {
      if (node.parent.blackHeight == 0 && node.blackHeight == 0) {
        node.parent.blackHeight = 1;
      }

      // if node has a red parent
    } else if (node.parent.blackHeight == 0) {
      Node<T> aunt = null;
      if (node.parent.isLeftChild()) {
        aunt = node.parent.parent.rightChild;
      } else {
        aunt = node.parent.parent.leftChild;
      }
      // case 1: node has a red uncle
      if (aunt != null && aunt.blackHeight == 0) {
        node.parent.blackHeight = 1;
        aunt.blackHeight = 1;
        node.parent.parent.blackHeight = 0;

        enforceRBTreePropertiesAfterInsert(node.parent.parent);

        // case 2: node has a black parent
      } else {
        // left left
        if (node.isLeftChild() && node.parent.isLeftChild()) {
          // swap parent and grandparent color
          int temp = node.parent.blackHeight;
          node.parent.blackHeight = node.parent.parent.blackHeight;
          node.parent.parent.blackHeight = temp;
          // rotate parent and grandfather
          rotate(node.parent, node.parent.parent);
        }

        // left right
        else if (node == node.parent.leftChild && node.parent == node.parent.parent.rightChild) {
          rotate(node, node.parent);
          // swap node and parent color
          int temp = node.blackHeight;
          node.blackHeight = node.parent.blackHeight;
          node.parent.blackHeight = temp;
          // rotate child and parent
          rotate(node, node.parent);
        }

        // right right
        else if (node == node.parent.rightChild && node.parent == node.parent.parent.rightChild) {
          // swap parent and grandparent color
          int temp = node.parent.blackHeight;
          node.parent.blackHeight = node.parent.parent.blackHeight;
          node.parent.parent.blackHeight = temp;
          // rotate around parent and grandparent
          rotate(node.parent, node.parent.parent);
        }

        // right left
        else if (node == node.parent.rightChild && node.parent.isLeftChild()) {
          rotate(node, node.parent);
          // swap node and parent color
          int temp = node.blackHeight;
          node.blackHeight = node.parent.blackHeight;
          node.parent.blackHeight = temp;
          // rotate child and parent
          rotate(node, node.parent);
        }
      }
    }
  }

  /**
   * Recursive helper method to find the subtree with a null reference in the position that the
   * newNode should be inserted, and then extend this tree by the newNode in that position.
   * 
   * @param newNode is the new node that is being added to this tree
   * @param subtree is the reference to a node within this tree which the newNode should be inserted
   *                as a descenedent beneath
   * @return true is the value was inserted in subtree, false if not
   */
  private boolean insertHelper(Node<T> newNode, Node<T> subtree) {
    int compare = newNode.data.compareTo(subtree.data);
    // do not allow duplicate values to be stored within this tree
    if (compare == 0)
      return false;

    // store newNode within left subtree of subtree
    else if (compare < 0) {
      if (subtree.leftChild == null) { // left subtree empty, add here
        subtree.leftChild = newNode;
        newNode.parent = subtree;
        enforceRBTreePropertiesAfterInsert(newNode);
        return true;
        // otherwise continue recursive search for location to insert
      } else
        return insertHelper(newNode, subtree.leftChild);
    }

    // store newNode within the right subtree of subtree
    else {
      if (subtree.rightChild == null) { // right subtree empty, add here
        subtree.rightChild = newNode;
        newNode.parent = subtree;
        enforceRBTreePropertiesAfterInsert(newNode);
        return true;
        // otherwise continue recursive search for location to insert
      } else
        return insertHelper(newNode, subtree.rightChild);
    }
  }

  /**
   * Performs the rotation operation on the provided nodes within this tree. When the provided child
   * is a leftChild of the provided parent, this method will perform a right rotation. When the
   * provided child is a rightChild of the provided parent, this method will perform a left
   * rotation. When the provided nodes are not related in one of these ways, this method will throw
   * an IllegalArgumentException.
   * 
   * @param child  is the node being rotated from child to parent position (between these two node
   *               arguments)
   * @param parent is the node being rotated from parent to child position (between these two node
   *               arguments)
   * @throws IllegalArgumentException when the provided child and parent node references are not
   *                                  initially (pre-rotation) related that way
   */
  private void rotate(Node<T> child, Node<T> parent) throws IllegalArgumentException {
    // TODO: Implement this method.
    if (parent == null || child == null) {
      throw new IllegalArgumentException();
    }
    if (parent.leftChild == child) {
      // right rotation
      // attaches right child of child to parent if not null
      parent.leftChild = child.rightChild;
      if (child.rightChild != null) {
        child.rightChild.parent = parent;
      }
      // connect child to grandfather if not null
      if (parent.parent != null) {
        child.parent = parent.parent;
      }
      // if parent is the root, make the root the child
      if (parent.parent == null) {
        this.root = child;
        this.root.parent = null;
        // make grandfather replace parent with child
      } else if (parent == parent.parent.leftChild) {
        parent.parent.leftChild = child;
      } else if (parent == parent.parent.rightChild) {
        parent.parent.rightChild = child;
      }
      // swaps parent and child
      child.rightChild = parent;
      parent.parent = child;


    } else if (parent.rightChild == child) {
      // left rotation
      parent.rightChild = child.leftChild;
      // attaches left child of child to parent if not null
      if (child.leftChild != null) {
        child.leftChild.parent = parent;
      }
      // connect child to grandfather if not null
      if (parent.parent != null) {
        child.parent = parent.parent;
      }

      // if parent is the root, make the root the child
      if (parent.parent == null) {
        this.root = child;
        this.root.parent = null;
        // make grandfather replace parent with child
      } else if (parent == parent.parent.leftChild) {
        parent.parent.leftChild = child;
      } else if (parent == parent.parent.rightChild) {
        parent.parent.rightChild = child;
      }
      // swaps parent and child
      child.leftChild = parent;
      parent.parent = child;

    } else {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Get the size of the tree (its number of nodes).
   * 
   * @return the number of nodes in the tree
   */
  public int size() {
    return size;
  }

  /**
   * Method to check if the tree is empty (does not contain any node).
   * 
   * @return true of this.size() return 0, false if this.size() > 0
   */
  public boolean isEmpty() {
    return this.size() == 0;
  }

  /**
   * Checks whether the tree contains the value *data*.
   * 
   * @param data the data value to test for
   * @return true if *data* is in the tree, false if it is not in the tree
   */
  public boolean contains(T data) {
    // null references will not be stored within this tree
    if (data == null)
      throw new NullPointerException("This RedBlackTree cannot store null references.");
    return this.containsHelper(data, root);
  }

  /**
   * Recursive helper method that recurses through the tree and looks for the value *data*.
   * 
   * @param data    the data value to look for
   * @param subtree the subtree to search through
   * @return true of the value is in the subtree, false if not
   */
  private boolean containsHelper(T data, Node<T> subtree) {
    if (subtree == null) {
      // we are at a null child, value is not in tree
      return false;
    } else {
      int compare = data.compareTo(subtree.data);
      if (compare < 0) {
        // go left in the tree
        return containsHelper(data, subtree.leftChild);
      } else if (compare > 0) {
        // go right in the tree
        return containsHelper(data, subtree.rightChild);
      } else {
        // we found it :)
        return true;
      }
    }
  }


  /**
   * This method performs an inorder traversal of the tree. The string representations of each data
   * value within this tree are assembled into a comma separated string within brackets (similar to
   * many implementations of java.util.Collection, like java.util.ArrayList, LinkedList, etc). Note
   * that this RedBlackTree class implementation of toString generates an inorder traversal. The
   * toString of the Node class class above produces a level order traversal of the nodes / values
   * of the tree.
   * 
   * @return string containing the ordered values of this tree (in-order traversal)
   */
  public String toInOrderString() {
    // generate a string of all values of the tree in (ordered) in-order
    // traversal sequence
    StringBuffer sb = new StringBuffer();
    sb.append("[ ");
    sb.append(toInOrderStringHelper("", this.root));
    if (this.root != null) {
      sb.setLength(sb.length() - 2);
    }
    sb.append(" ]");
    return sb.toString();
  }

  private String toInOrderStringHelper(String str, Node<T> node) {
    if (node == null) {
      return str;
    }
    str = toInOrderStringHelper(str, node.leftChild);
    str += (node.data.toString() + ", ");
    str = toInOrderStringHelper(str, node.rightChild);
    return str;
  }

  /**
   * This method performs a level order traversal of the tree rooted at the current node. The string
   * representations of each data value within this tree are assembled into a comma separated string
   * within brackets (similar to many implementations of java.util.Collection). Note that the Node's
   * implementation of toString generates a level order traversal. The toString of the RedBlackTree
   * class below produces an inorder traversal of the nodes / values of the tree. This method will
   * be helpful as a helper for the debugging and testing of your rotation implementation.
   * 
   * @return string containing the values of this tree in level order
   */
  public String toLevelOrderString() {
    String output = "[ ";
    if (this.root != null) {
      LinkedList<Node<T>> q = new LinkedList<>();
      q.add(this.root);
      while (!q.isEmpty()) {
        Node<T> next = q.removeFirst();
        if (next.leftChild != null)
          q.add(next.leftChild);
        if (next.rightChild != null)
          q.add(next.rightChild);
        output += next.data.toString();
        if (!q.isEmpty())
          output += ", ";
      }
    }
    return output + " ]";
  }

  public String toString() {
    return "level order: " + this.toLevelOrderString() + "\nin order: " + this.toInOrderString();
  }


  // Implement at least 3 boolean test methods by using the method signatures below,
  // removing the comments around them and addind your testing code to them. You can
  // use your notes from lecture for ideas on concrete examples of rotation to test for.
  // Make sure to include rotations within and at the root of a tree in your test cases.
  // If you are adding additional tests, then name the method similar to the ones given below.
  // Eg: public static boolean test4() {}
  // Do not change the method name or return type of the existing tests.
  // You can run your tests by commenting in the calls to the test methods

  static RedBlackTree<Integer> tree = null;

  @BeforeEach
  public void createInstance() {
    tree = new RedBlackTree<Integer>();
  }

  /**
   * tests adding a child to a red node with a black aunt (left right)
   * 
   * @return true if test passes, false otherwise
   */
  @Test
  public void test1() {
    tree.insert(23);
    tree.insert(7);
    tree.insert(41);
    tree.insert(37);
    tree.insert(30);
    String output = "[ ";
    if (tree.root != null) {
      LinkedList<Node<Integer>> q = new LinkedList<>();
      q.add(tree.root);
      while (!q.isEmpty()) {
        Node<Integer> next = q.removeFirst();
        if (next.leftChild != null)
          q.add(next.leftChild);
        if (next.rightChild != null)
          q.add(next.rightChild);
        output += next.blackHeight;
        if (!q.isEmpty())
          output += ", ";
      }
    }
    output = output + " ]";
    assertEquals(tree.toLevelOrderString(), "[ 23, 7, 37, 30, 41 ]");
    assertEquals(output, "[ 1, 1, 1, 0, 0 ]");
  }

  /**
   * Tests adding a child to a red node with a red aunt
   * 
   * @return true if test passes, false otherwise
   */
  @Test
  public void test2() {
    tree.insert(23);
    tree.insert(15);
    tree.insert(30);
    tree.insert(33);
    tree.insert(14);
    tree.insert(10);
    tree.insert(9);
    String output = "[ ";
    if (tree.root != null) {
      LinkedList<Node<Integer>> q = new LinkedList<>();
      q.add(tree.root);
      while (!q.isEmpty()) {
        Node<Integer> next = q.removeFirst();
        if (next.leftChild != null)
          q.add(next.leftChild);
        if (next.rightChild != null)
          q.add(next.rightChild);
        output += next.blackHeight;
        if (!q.isEmpty())
          output += ", ";
      }
    }
    output = output + " ]";
    
    assertEquals(tree.toLevelOrderString(), "[ 23, 14, 30, 10, 15, 33, 9 ]");
    assertEquals(output, "[ 1, 0, 1, 1, 1, 0, 0 ]");
  
  }

  /**
   * Tests adding a child to a red node with a black aunt (right left)
   * 
   * @return true if test passes, false otherwise
   */
  @Test
  public void test3() {
    tree.insert(23);
    tree.insert(15);
    tree.insert(30);
    tree.insert(33);
    tree.insert(14);
    tree.insert(10);
    tree.insert(16);
    tree.insert(11);
    tree.insert(12);
    String output = "[ ";
    if (tree.root != null) {
      LinkedList<Node<Integer>> q = new LinkedList<>();
      q.add(tree.root);
      while (!q.isEmpty()) {
        Node<Integer> next = q.removeFirst();
        if (next.leftChild != null)
          q.add(next.leftChild);
        if (next.rightChild != null)
          q.add(next.rightChild);
        output += next.blackHeight;
        if (!q.isEmpty())
          output += ", ";
      }
    }
    output = output + " ]";

     assertEquals(tree.toLevelOrderString(), "[ 23, 14, 30, 11, 15, 33, 10, 12, 16 ]");
     assertEquals(output, "[ 1, 0, 1, 1, 1, 0, 0, 0, 0 ]");
  }

  
  /**
   * Main method to run tests. Comment out the lines for each test to run them.
   * 
   * @param args
   */
  public static void main(String[] args) {

  }

}
