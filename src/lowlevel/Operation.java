package lowlevel;

import java.io.*;
import dataflow.BitArraySet;

/**
 * This class is the primary low-level abstraction for an assembly instruction
 *
 * @author Dr. Gallagher
 * @version 1.0
 * Created: 22 Apr 03
 * Summary of Modifications:
 *    29 Apr 03 - DMG - New constructor added which just takes type as an arg
 *       We don't need to pass the block anymore, because the appendOper()
 *       method of the BasicBlock will set the Operations block
 *    1 May 03 - DMG  - Fixed bug in new constructor added 29 Apr
 *
 * Description:  An Operation is the basic internal representation of an
 * assembly language instruction.  Early in the back-end, Operations are
 * generic and don't necessarily map 1:1 with assembly.  As the low-level code
 * gets more architecture-specific, Operations should become more and more a 1:1
 * mapping with assembly.  An Operation maintains next/prev references, so a
 * block can contain a list of Operations.  The Operation also maintains sets
 * of Operands.  To keep it general, we employ arrays of src and dest Operands.
 */

public class Operation {

    // Constants defined to allow the # of Operands to be tailored to a specific
    // architecture
  public static final int MAX_DEST_OPERANDS = 2;
  public static final int MAX_SRC_OPERANDS = 4;

    // Currently defined Operation types
  public static final int OPER_UNKNOWN = 0;
  public static final int OPER_FUNC_ENTRY = 1;
  public static final int OPER_FUNC_EXIT = 2;
  public static final int OPER_ASSIGN = 3;

  public static final int OPER_ADD_I = 20;
  public static final int OPER_SUB_I = 21;
  public static final int OPER_MUL_I = 22;
  public static final int OPER_DIV_I = 23;
  public static final int X86_OPER_MUL_I = 24;
  public static final int X86_OPER_DIV_I = 25;

  public static final int OPER_LT = 40;
  public static final int OPER_LTE = 41;
  public static final int OPER_GT = 42;
  public static final int OPER_GTE = 43;
  public static final int OPER_EQUAL = 44;
  public static final int OPER_NOTEQ = 45;

  public static final int OPER_RETURN = 60;
  public static final int OPER_JMP = 61;
  public static final int OPER_BEQ = 62;
  public static final int OPER_BNE = 63;
  public static final int X86_OPER_BEQ = 64;
  public static final int X86_OPER_BNE = 65;
  public static final int X86_OPER_BLT = 66;
  public static final int X86_OPER_BLE = 67;
  public static final int X86_OPER_BGT = 68;
  public static final int X86_OPER_BGE = 69;

  public static final int OPER_PASS = 80;
  public static final int OPER_CALL = 81;

  public static final int OPER_LOAD_I = 100;
  public static final int OPER_STORE_I = 101;
  public static final int X86_OPER_PUSH = 102;
  public static final int X86_OPER_POP = 103;
  public static final int X86_OPER_MOV = 104;
  public static final int X86_OPER_CMP = 105;

/***************************************************************************/
    // instance variables
    // the block containing the Operation
  private BasicBlock block;
    // references to maintain the linked list of Operations within the block
  private Operation prevOper;
  private Operation nextOper;
    // A unique number identifying the Operation; set in constructor
  private int opNum;
    // The type of Operation, as defined in consts above
  private int opType;
    // Arrays of src and destination Operands; Their size is determined by
    // the consts above
  private Operand []dest;
  private Operand []src;
    // the next two variables currently unused
  private int maxSrc;
  private int maxDest;

    // FOR LIVE RANGE
  private BitArraySet liveRange;

    // future use
  private Attribute attr;
/***************************************************************************/
  // constructors
    /**
     * @param currBlock is the block containing the Operation
     */
  public Operation(BasicBlock currBlock) {
    this (OPER_UNKNOWN, currBlock, null);
  }
    /**
     * @param currBlock is the block containing the Operation
     * @param prev is a reference to the previous Operation
     */
  public Operation(BasicBlock currBlock, Operation prev) {
    this (OPER_UNKNOWN, currBlock, prev);
  }
    /**
     * Probably the most common constructor
     * @param type is the Operation type (e.g.,OPER_JMP)
     * @param currBlock is the block containing the Operation
     */
  public Operation (int type, BasicBlock currBlock) {
    this(type, currBlock, null);
  }
    /**
     * @param type is the Operation type (e.g.,OPER_JMP)
     * @param currBlock is the block containing the Operation
     * @param prev is a reference to the previous Operation
     */
  public Operation (int type, BasicBlock currBlock, Operation prev) {
    opNum = currBlock.getFunc().getNewOperNum();
    opType = type;
    block = currBlock;
    prevOper = prev;
    nextOper = null;
    dest = new Operand[MAX_DEST_OPERANDS];
    src = new Operand[MAX_SRC_OPERANDS];
    if (prev != null) {
      prev.setNextOper(this);
    }
    maxSrc = -1;
    maxDest = -1;
  }


/***************************************************************************/
  // accessor methods
  public int getNum () {
    return opNum;
  }
  public void setNum(int newNum) {
    opNum = newNum;
  }
  public int getType () {
    return opType;
  }
  public void setType (int newType) {
    opType = newType;
  }

  public BasicBlock getBlock () {
    return block;
  }
  public void setBlock (BasicBlock newBlock) {
    block = newBlock;
  }

  public Operation getPrevOper () {
    return prevOper;
  }
  public void setPrevOper (Operation prev) {
    prevOper = prev;
  }
  public Operation getNextOper () {
    return nextOper;
  }
  public void setNextOper (Operation next) {
    nextOper = next;
  }

  public Operand getSrcOperand (int index) {
    return src[index];
  }
  public void setSrcOperand (int index, Operand newOperand) {
    src[index] = newOperand;
    if (index > maxSrc) {
      maxSrc = index;
    }
  }
  public Operand getDestOperand (int index) {
    return dest[index];
  }
  public void setDestOperand (int index, Operand newOperand) {
    dest[index] = newOperand;
    if (index > maxDest) {
      maxDest = index;
    }
  }

  public BitArraySet getLiveRange () {
    return liveRange;
  }
  public void setLiveRange (BitArraySet newSet) {
    liveRange = newSet;
  }

  public Attribute getAttribute() {
    return attr;
  }

  public void addAttribute(Attribute newAttr) {
      // just put at head of list
    newAttr.setNext(attr);
    attr = newAttr;
  }

  public boolean hasAttribute (String name) {
    boolean retVal = false;

    for (Attribute currAttr = attr; currAttr != null;
          currAttr = currAttr.getNext() ) {
      if (name.compareTo(currAttr.getName()) == 0) {
        retVal = true;
        break;
      }
    }
    return retVal;
  }

  public String findAttribute (String name) {
      // searches for match of name and returns value or null
    String retVal = null;

    for (Attribute currAttr = attr; currAttr != null;
          currAttr = currAttr.getNext() ) {
      if (name.compareTo(currAttr.getName()) == 0) {
        retVal = currAttr.getValue();
        break;
      }
    }
    return retVal;
  }

/***************************************************************************/
  // support methods

  public void delete() {
    if (getPrevOper() == null) {
      getBlock().setFirstOper(getNextOper());
    }
    else {
      getPrevOper().setNextOper(getNextOper());
    }
    if (getNextOper() == null) {
      getBlock().setLastOper(getPrevOper());
    }
    else {
      getNextOper().setPrevOper(getPrevOper());
    }
  }

  public boolean hasRegDest() {
    if (dest[0] != null) {
      if (dest[0].getType() == Operand.OPERAND_REG) {
        return true;
      }
    }
    return false;
  }

    // converts operation type into a string for printing
  public String printOperType() {
    switch (opType) {
      case OPER_FUNC_ENTRY:
        return "Func_Entry";
      case OPER_FUNC_EXIT:
        return "Func_Exit";
      case OPER_ASSIGN:
        return "Mov";
      case OPER_ADD_I:
        return "Add_I";
      case OPER_SUB_I:
        return "Sub_I";
      case OPER_MUL_I:
        return "Mul_I";
      case OPER_DIV_I:
        return "Div_I";
      case OPER_LT:
        return "LT";
      case OPER_LTE:
        return "LTE";
      case OPER_GT:
        return "GT";
      case OPER_GTE:
        return "GTE";
      case OPER_EQUAL:
        return "EQ";
      case OPER_NOTEQ:
        return "NEQ";
      case OPER_RETURN:
        return "Return";
      case OPER_JMP:
        return "Jmp";
      case OPER_PASS:
        return "Pass";
      case OPER_CALL:
        return "JSR";
      case OPER_BEQ:
        return "BEQ";
      case OPER_BNE:
        return "BNE";
      case OPER_LOAD_I:
        return "Load";
      case OPER_STORE_I:
        return "Store";
      case X86_OPER_PUSH:
        return "Push";
      case X86_OPER_POP:
        return "Pop";
      case X86_OPER_MOV:
        return "Mov";
      case X86_OPER_CMP:
        return "Cmp";
      case X86_OPER_BEQ:
        return "BEQ";
      case X86_OPER_BNE:
        return "BNE";
      case X86_OPER_BLT:
        return "BLT";
      case X86_OPER_BLE:
        return "BLE";
      case X86_OPER_BGT:
        return "BGT";
      case X86_OPER_BGE:
        return "BGE";
      case X86_OPER_MUL_I:
        return "Mul";
      case X86_OPER_DIV_I:
        return "Div";
      default:
        throw new LowLevelException ("Operation: unexpected op type");
    }
  }

  public boolean isBranchOper() {
    return ( (opType == OPER_BEQ) ||
             (opType == OPER_BNE) ||
             (opType == X86_OPER_BEQ) ||
             (opType == X86_OPER_BNE) ||
             (opType == X86_OPER_BLT) ||
             (opType == X86_OPER_BLE) ||
             (opType == X86_OPER_BGT) ||
             (opType == X86_OPER_BGE) );
  }

  public boolean isX86BranchOper() {
    return ( (opType == X86_OPER_BEQ) ||
             (opType == X86_OPER_BNE) ||
             (opType == X86_OPER_BLT) ||
             (opType == X86_OPER_BLE) ||
             (opType == X86_OPER_BGT) ||
             (opType == X86_OPER_BGE) );
  }

    // prints the Operation, recursively calling print on each Operand
  public void printLLCode(PrintWriter outFile) {
    if (outFile == null) {
      System.out.print("    (OPER " + this.getNum() + " " + printOperType() + " [");
      for (int currDest = 0; currDest <= maxDest; currDest++) {
        if (dest[currDest] != null) {
          dest[currDest].printLLCode(outFile);
        }
        else {
          System.out.println("()");
        }
      }
      System.out.print("]  [");
      for (int currSrc = 0; currSrc <= maxSrc; currSrc++) {
        if (src[currSrc] != null) {
          src[currSrc].printLLCode(outFile);
        }
        else {
          System.out.println("()");
        }
      }
      System.out.print("]");
      System.out.println(")");
    }
    else {
      outFile.print("    (OPER " + this.getNum() + " " + printOperType() + " [");
      for (int currDest = 0; currDest <= maxDest; currDest++) {
        if (dest[currDest] != null) {
          dest[currDest].printLLCode(outFile);
        }
        else {
          outFile.println("()");
        }
      }
      outFile.print("]  [");
      for (int currSrc = 0; currSrc <= maxSrc; currSrc++) {
        if (src[currSrc] != null) {
          src[currSrc].printLLCode(outFile);
        }
        else {
          outFile.println("()");
        }
      }
      outFile.print("]");
      outFile.println(")");
    }
  }


}