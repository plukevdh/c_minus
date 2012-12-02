package lowlevel;

import java.io.*;

/**
 * This class provides the low-level abstraction for global variables.
 *
 * @author Dr. Gallagher
 * @version 1.0
 * Created: 22 Apr 03
 * Summary of Modifications:
 *
 * Description:  Global variables must be maintained in the low-level code, and
 * will eventually be converted into assembly statements such as ".comm 4".
 * This class is the primary data structure to maintain these global variables.
 * It extends CodeItem, which provides a linked structure for chaining Data and
 * Functions.
 */

public class Data extends CodeItem {
    // constants to define the data types available
  public static final int TYPE_VOID = 0;
  public static final int TYPE_INT = 1;

    // instance variables
  int dataType;
  String name;
  boolean isArray;
  int arraySize;

    // constructors
  public Data() {
    this (TYPE_VOID,null,false, 0);
  }
    /**
     * Used primarily for non-array parameters
     * @param type is the type of the parameter (e.g., Int, Void) as defined in Data class
     * @param newName is the name of the parameter
     */
  public Data(int type, String newName) {
    this (type, newName, false, 0);
  }
    /**
     * Used primarily for array parameters
     * @param type is the type of the parameter (e.g., Int, Void) as defined in Data class
     * @param newName is the name of the parameter
     * @param array is whether or not the parameter is an array variable
     * @param size is the size of the array
     */
  public Data (int type, String newName, boolean array, int size) {
    dataType = type;
    name = newName;
    isArray = array;
    arraySize = size;
  }
/***************************************************************************/
    // accessor methods
  public int getType () {
    return dataType;
  }
  public void setType (int newType) {
    dataType = newType;
  }
  public String getName () {
    return name;
  }
  public void setname (String newName) {
    name = newName;
  }

  public static String printType(int type) {
    if (type == TYPE_VOID) {
      return "void";
    }
    return "int";
  }

/***************************************************************************/
    // support methods
  public void printLLCode(PrintWriter outFile) {
    if (outFile == null) {
      System.out.println("(DATA  " + getName() + ")");
    }
    else {
      outFile.println("(DATA  " + getName() + ")");
    }
      // CodeItems are in a linked list.  This ensures next CodeItem (either
      // Function or Data) is printed
    if (this.getNextItem() != null) {
      this.getNextItem().printLLCode(outFile);
    }
  }
}