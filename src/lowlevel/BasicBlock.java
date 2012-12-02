package lowlevel;

import java.io.*;
import java.util.*;
import dataflow.BitArraySet;

/**
 * This class is the primary low-level abstraction for a basic block
 *
 * @author Dr. Gallagher
 * @version 1.0
 * Created: 22 Apr 03
 * Summary of Modifications:
 *     29 Apr 03 - DMG - appendOper modified to set the Operation's block
 *
 * Description:  A Function is made up of BasicBlocks.  A BasicBlock is a
 * sequence of Operations which ends when a JMP or BR operation is encountered,
 * or when the next operation is the tgt of a JMP or BR operation.  A BasicBlock
 * contains a linked list of Operations.
 */

public class BasicBlock {

    // The function in which this BasicBlock resides
  private Function func;
    // The function needs a list of blocks; the next 2 references serve to
    // doubly connect this list
  private BasicBlock prevBlock;
  private BasicBlock nextBlock;
    // The block contains a list of Operations.  These 2 references serve as
    // head/tail pointers for this list
  private Operation firstOper;
  private Operation lastOper;
    // All blocks have a number associated with them, assigned in constructor
  private int blockNum;

    // control flow stuff;
  private LinkedList inEdges;
  private LinkedList outEdges;

    // dataflow stuff
  private BitArraySet defUseGen;
  private BitArraySet defUseKill;
  private BitArraySet defUseIn;
  private BitArraySet defUseOut;
  private BitArraySet livenessUse;
  private BitArraySet livenessDef;
  private BitArraySet livenessIn;
  private BitArraySet livenessOut;

//  private Attribute attr;
/***************************************************************************/
  // constructors
  // probably the first constructor will be the most useful
    /**
     * @param newFunc is the name of the function this block will be in
     */
  public BasicBlock(Function newFunc) {
    this (newFunc, null);
  }
    /**
     * @param newFunc is the name of the function this block will be in
     * @param prev is a reference to the previous BasicBlock
     */
  public BasicBlock (Function newFunc, BasicBlock prev) {
    blockNum = newFunc.getNewBlockNum();
    func = newFunc;
    nextBlock = null;
    firstOper = null;
    lastOper = null;
    prevBlock = prev;
    if (prev != null) {
      prev.setNextBlock(this);
    }

    inEdges = new LinkedList();
    outEdges = new LinkedList();
  }
/***************************************************************************/
  // accessor methods
  public Function getFunc() {
    return func;
  }
  public BasicBlock getPrevBlock( ) {
    return prevBlock;
  }
  public void setPrevBlock(BasicBlock prev) {
    prevBlock = prev;
  }
  public BasicBlock getNextBlock( ) {
    return nextBlock;
  }
  public void setNextBlock(BasicBlock next) {
    nextBlock = next;
  }

  public Operation getFirstOper () {
    return firstOper;
  }
  public void setFirstOper (Operation first) {
    firstOper = first;
  }
  public Operation getLastOper () {
    return lastOper;
  }
  public void setLastOper (Operation last) {
    lastOper = last;
  }

  public int getBlockNum () {
    return blockNum;
  }

/***************************************************************************/
  // dataflow accessor methods

  public LinkedList getInEdges () {
    return inEdges;
  }
  public void setInEdges (LinkedList in) {
    inEdges = in;
  }
  public LinkedList getOutEdges () {
    return outEdges;
  }
  public void setOutEdges (LinkedList out) {
    outEdges = out;
  }

    // dataflow stuff
  public BitArraySet getDefUseGen() {
    return defUseGen;
  }
  public void setDefUseGen(BitArraySet gen) {
    defUseGen = gen;
  }
  public BitArraySet getDefUseKill() {
    return defUseKill;
  }
  public void setDefUseKill(BitArraySet kill) {
    defUseKill = kill;
  }
  public BitArraySet getDefUseIn() {
    return defUseIn;
  }
  public void setDefUseIn(BitArraySet in) {
    defUseIn = in;
  }
  public BitArraySet getDefUseOut() {
    return defUseOut;
  }
  public void setDefUseOut(BitArraySet out) {
    defUseOut = out;
  }

  public BitArraySet getLivenessDef() {
    return livenessDef;
  }
  public void setLivenessDef(BitArraySet def) {
    livenessDef = def;
  }
  public BitArraySet getLivenessUse() {
    return livenessUse;
  }
  public void setLivenessUse (BitArraySet use) {
    livenessUse = use;
  }
  public BitArraySet getLivenessIn() {
    return livenessIn;
  }
  public void setLivenessIn(BitArraySet in) {
    livenessIn = in;
  }
  public BitArraySet getLivenessOut() {
    return livenessOut;
  }
  public void setLivenessOut(BitArraySet out) {
    livenessOut = out;
  }
/***************************************************************************/
  // support methods
    // this method appends the newOper to the present block.  The only thing of
    // concern is to hook up the oper list, and to be sure the head/tail
    // pointers are maintained

  public static BasicBlock getBlockFromNum(Function func, int blockNum) {
    for (BasicBlock curr = func.getFirstBlock(); curr != null;
              curr = curr.getNextBlock() ) {
      if (curr.getBlockNum() == blockNum) {
        return (curr);
      }
    }
    throw new LowLevelException("BasicBlock: no block corresponds to num" +
                                  blockNum);
  }

  public void appendOper (Operation newOper) {
    if (firstOper == null) {
      firstOper = newOper;
      lastOper = newOper;
    }
    else {
      newOper.setPrevOper(lastOper);
      lastOper.setNextOper(newOper);
      lastOper = newOper;
    }
    newOper.setBlock(this);
  }
    // splices the oper out of the list
  public void removeOper (Operation oper) {
    if (oper == firstOper) {
      firstOper = oper.getNextOper();
    }
    else {
      oper.getPrevOper().setNextOper(oper.getNextOper());
    }
    if (oper == lastOper) {
      lastOper = oper.getPrevOper();
    }
    else {
      oper.getNextOper().setPrevOper(oper.getPrevOper());
    }
  }

  public void insertOperAfter(Operation oper, Operation insertOper) {

    insertOper.setNextOper(oper.getNextOper());

    if (oper == lastOper) {
      lastOper = insertOper;
    }
    else {
      insertOper.getNextOper().setPrevOper(insertOper);
    }
    oper.setNextOper(insertOper);
    insertOper.setPrevOper(oper);
  }

  public void insertOperBefore(Operation oper, Operation insertOper) {
    insertOper.setPrevOper(oper.getPrevOper());

    if (oper == firstOper) {
      firstOper = insertOper;
    }
    else {
      insertOper.getPrevOper().setNextOper(insertOper);
    }
    oper.setPrevOper(insertOper);
    insertOper.setNextOper(oper);
  }

  public void addOutEdge(BasicBlock out) {
    if (outEdges == null) {
      throw new LowLevelException("addOutEdge: outEdges not initialized");
    }
    outEdges.add(out);
  }

  public void addInEdge(BasicBlock in) {
    if (inEdges == null) {
      throw new LowLevelException("addInEdge: inEdges not initialized");
    }
    inEdges.add(in);
  }

    // prints the BB, then calls printLLCode on each Operation in the block
  public void printLLCode(PrintWriter outFile) {

    if (outFile == null) {
      System.out.println("  (BB " + this.getBlockNum());
    }
    else {
      outFile.println("  (BB " + this.getBlockNum());
    }
    for (Operation curr = firstOper; curr != null; curr=curr.getNextOper()) {
      curr.printLLCode(outFile);
    }
    if (outFile == null) {
      System.out.println("  )");
    }
    else {
      outFile.println("  )");
    }
  }

}