package lowlevel;

/**
 * This class supports a linked list of function formal parameters.
 *
 * @author Dr. Gallagher
 * @version 1.0
 * Created: 22 Apr 03
 * Summary of Modifications:
 *    29 Apr 03 - DMG - Changed type of method printType to non-static, and
 *                    deleted getTypeAsString() method
 *
 * Description:  In order to maintain the types and names of function formal
 * parameters, this data structure maintains a linked list of parameters.
 * It is very much just the low-level counterpart to the intermediate function
 * parameter list, Param.  The current paradigm for function layout just uses
 * the FUNC_ENTRY tag, which a subsequent pass will need to clarify.  One of the
 * items which will need to be done is to figure out how the parameters will
 * really be passed by the architecture.  Once this is known, this FuncParam
 * list can be used to figure out the parameter layout and thus operations to
 * move parameters from their passed location (e.g., on the stack at positive
 * offsets from FP) into virtual registers can be generated.  At the basic code
 * generation step, virtual registers are assigned to the parameters.  These
 * moves generated in the subsequent pass will move the parameters into the
 * virtual register (e.g., R4 = Load (FP + 8).
 */

public class FuncParam {
    // instance variables
  int dataType;
  String name;
  boolean isArray;
    // FuncParams are maintained in a linked list.  This reference supports
    // the link structure.
  FuncParam nextParam;
/***************************************************************************/
    // constructors


  public FuncParam() {
    this (Data.TYPE_VOID,null,false);
  }
    /**
     * Useful for non-array parameters
     * @param type is the type of the parameter (e.g., Int, Void) as defined in Data class
     * @param newName is the name of the parameter
     */
  public FuncParam(int type, String newName) {
    this (type, newName, false);
  }
    /**
     * Used primarily for array parameters
     * @param type is the type of the parameter (e.g., Int, Void) as defined in Data class
     * @param newName is the name of the parameter
     * @param array is whether or not the parameter is an array variable
     */
  public FuncParam (int type, String newName, boolean array) {
    dataType = type;
    name = newName;
    isArray = array;
    nextParam = null;
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
  public FuncParam getNextParam() {
    return nextParam;
  }
  public void setNextParam(FuncParam param) {
    nextParam = param;
  }
  public boolean getIsArray() {
    return isArray;
  }
/***************************************************************************/
  // support methods
    // this method is used during printing of FuncParams.  It assumes there are
    // only INT and VOID types; it will need to be restructured a bit to allow
    // further types
  public String printType() {
    if (dataType == Data.TYPE_VOID) {
      return "void";
    }
    return "int";
  }
}