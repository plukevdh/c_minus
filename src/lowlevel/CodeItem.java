package lowlevel;

import java.io.*;

/**
 * This abstract class is the top-level class in the low-level code heirarchy.
 *
 * @author Dr. Gallagher
 * @version 1.0
 * Created: 22 Apr 03
 * Summary of Modifications:
 *
 * Description:  In our paradigm, a file being compiled is considered a linked
 * list of CodeItem, which can either be global variables (Data) or functions
 * (Function).  Thus, CodeItem acts as the abstract parent for the Data and
 * Function classes.  The only functionality it provides is the next pointer to
 * support the linked list structure.
 */

public abstract class CodeItem {
    // instance variables
  CodeItem nextItem;
    // constructor
  public CodeItem() {
  }
    // accessor methods
  public CodeItem getNextItem() {
    return nextItem;
  }
  public void setNextItem (CodeItem next) {
    nextItem = next;
  }
    // abstract method to support polymorphism during printing
  public void printLLCode(PrintWriter outFile) { }
}
