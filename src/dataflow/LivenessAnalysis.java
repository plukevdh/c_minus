package dataflow;

import lowlevel.*;
import java.util.LinkedList;

public class LivenessAnalysis {

  CodeItem firstItem;

  public LivenessAnalysis(CodeItem first) {
    firstItem = first;
  }

  public void performAnalysis() {

    for (CodeItem currItem = firstItem; currItem != null;
                                        currItem = currItem.getNextItem()) {
      if (currItem instanceof Data) {
        continue;
      }
      Function func = (Function) currItem;

        // First, we need to set up necessary data structures

        // We need to make an array which can be used to convert oper nums to
        // oper pointers
      func.makeOperConversionArray();
        // sets up BitArraySets in function and in opers (the same BitArraySet
        // is referenced in both the function and the oper)
        // also sets up BitArraySets in each BasicBlock
      func.setUpLiveness();

        // Next, we need to set up and perform the BasicBlock level analysis

        // We first determine the gen/kill set for each basic block, then we
        // iterate until in/out sets are stable
//      setUpGenKillSets(func);

        // We first determine the def and use sets for each basic block, then we
        // iterate until in/out sets are stable
      setUpDefAndUseSets(func);

      performIterationOnInOutSets (func);

        // to check liveness analysis, we look for things like defs which aren't
        // live out (unused vars), and uses which are live into function
        // (i.e. BB0; undefined vars)
      checkLivenessAnalysis (func);

        // we now have BB granularity
        // to get Oper granularity, we need to do 2 things:
        //    1. use IN/OUT sets to update opers
        //    2. compute local register use (regs that aren't live out)
      updateLivenessForOpers (func);



    }
  }

  private void setUpDefAndUseSets (Function func) {

    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                    currBlock = currBlock.getNextBlock()) {
        // for each block, we walk the block to determine the use/def

        // the DEF set is the set of register definitions within the block,
        // where the definition precedes any use.  To compute, we need to know
        // the set of vars used so far

        // the USE set is the set of register uses that occur before any
        // definitions. To compute, we need to know the set of vars defined
        // so far

      BitArraySet uses = new BitArraySet(func.getMaxRegNum()+1);
      BitArraySet defs = new BitArraySet(func.getMaxRegNum()+1);
      currBlock.setLivenessUse(uses);
      currBlock.setLivenessDef(defs);


      BitArraySet defsSoFar = new BitArraySet(func.getMaxRegNum()+1);
      BitArraySet usesSoFar = new BitArraySet(func.getMaxRegNum()+1);

      for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                    currOper = currOper.getNextOper()) {
          // because an oper reads its sources before defining the reg, we need
          // to eval uses first
        for (int i=0; i < Operation.MAX_SRC_OPERANDS; i++) {
          Operand currOperand = currOper.getSrcOperand(i);
          if ( (currOperand != null) &&
               (currOperand.getType() == Operand.OPERAND_REG) ) {
            int regNum = ( (Integer) currOperand.getValue()).intValue();
            if (!defsSoFar.contains(regNum)) {
              uses.add(regNum);
            }
            usesSoFar.add(regNum);
          }
        }
        for (int i=0; i < Operation.MAX_DEST_OPERANDS; i++) {
          Operand currOperand = currOper.getDestOperand(i);
          if ( (currOperand != null) &&
               (currOperand.getType() == Operand.OPERAND_REG) ) {
            int regNum = ( (Integer) currOperand.getValue()).intValue();
            if (!usesSoFar.contains(regNum)) {
              defs.add(regNum);
            }
            defsSoFar.add(regNum);
          }
        }
      }
//      System.out.println("Def For BB#"+currBlock.getBlockNum());
//      for (int i=0; i< defs.getMaxVal();i++) {
//        if (defs.contains(i)) {
//          System.out.print(i + "  ");
//        }
//      }
//      System.out.println();
//      System.out.println();
//
//      System.out.println("Use For BB#"+currBlock.getBlockNum());
//      for (int i=0; i< uses.getMaxVal();i++) {
//        if (uses.contains(i)) {
//          System.out.print(i + "  ");
//        }
//      }
//      System.out.println();
//      System.out.println();
    }
  }

  private void performIterationOnInOutSets (Function func) {

    boolean somethingChanged = true;

    while (somethingChanged) {
      somethingChanged = false;

        // iterate through all blocks, computing a new out set
        // if the out set changed, annotate that something has changed
        // because we are flowing data up in liveness analysis, walking the
        // blocks in reverse order will likely make it converge quicker.
      for (BasicBlock currBlock = func.getLastBlock(); currBlock != null;
                    currBlock = currBlock.getPrevBlock()) {
          // the Dragon book and another do the IN first, and then the OUT
          // not sure why, but ....

          // IN is (out - def) union use
        BitArraySet temp = currBlock.getLivenessOut().subtract(currBlock.getLivenessDef());
        temp = temp.union(currBlock.getLivenessUse());

        if (! currBlock.getLivenessIn().equals(temp) ) {
          somethingChanged = true;
          currBlock.setLivenessIn(temp);
        }

          // OUT is the union of all of the sucessors
        LinkedList outEdge = currBlock.getOutEdges();
        Object []edges = outEdge.toArray();
        BitArraySet out = currBlock.getLivenessOut();
        BitArraySet newOut = new BitArraySet(func.getMaxRegNum()+1);
        for (int i = 0; i < edges.length; i++) {
          BasicBlock tgt = (BasicBlock) edges[i];
          newOut = newOut.union(tgt.getLivenessIn());
        }
        if (! out.equals(newOut)  ) {
          somethingChanged = true;
          currBlock.setLivenessOut(newOut);
        }
      }
    }
    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                    currBlock = currBlock.getNextBlock()) {
      System.out.println("In For BB#"+currBlock.getBlockNum());
      BitArraySet livenessIn = currBlock.getLivenessIn();
      for (int i=0; i<= livenessIn.getMaxVal();i++) {
        if (livenessIn.contains(i)) {
          System.out.print(i + "  ");
        }
      }
      System.out.println();
      System.out.println();

      System.out.println("Out For BB#"+currBlock.getBlockNum());
      BitArraySet livenessOut = currBlock.getLivenessOut();
      for (int i=0; i<= livenessOut.getMaxVal();i++) {
        if (livenessOut.contains(i)) {
          System.out.print(i + "  ");
        }
      }
      System.out.println();
      System.out.println();
    }
  }

    // things to check for:
    // 1. defs which aren't used.  For every def in the basic block, if it
    //    isn't live out, it is dead code
    // 2. uses which are live into the function.  Any live in to block 0 might
    //    indicate a problem
  private void checkLivenessAnalysis (Function func) {
    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                    currBlock = currBlock.getNextBlock()) {

        // if block 0, check live in - safer to check for no predecessor
      if (currBlock.getPrevBlock() == null) {
        BitArraySet livenessIn = currBlock.getLivenessIn();
        if (! livenessIn.isEmpty() ) {
          System.out.println("Liveness analysis warning: the following vars " +
                              "are live into Block 0");
          System.out.print("  ");
          for (int i=0; i<= livenessIn.getMaxVal();i++) {
            if (livenessIn.contains(i)) {
              System.out.print(i + "  ");
            }
          }
          System.out.println();
          System.out.println();
        }
      }
      BitArraySet livenessOut = currBlock.getLivenessOut();
      BitArraySet unusedDefsSoFar = new BitArraySet(func.getMaxRegNum()+1);
        // for all blocks, look at each definition, and see it it is live out
        // but only if it isn't used subsequently within the block
      for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                    currOper = currOper.getNextOper()) {
          // if used, reset bit
        for (int i=0; i < Operation.MAX_SRC_OPERANDS; i++) {
          Operand currOperand = currOper.getSrcOperand(i);
          if ( (currOperand != null) &&
               (currOperand.getType() == Operand.OPERAND_REG) ) {
            int regNum = ( (Integer) currOperand.getValue()).intValue();
            unusedDefsSoFar.remove(regNum);
          }
        }
        for (int i=0; i < Operation.MAX_DEST_OPERANDS; i++) {
          Operand currOperand = currOper.getDestOperand(i);
          if ( (currOperand != null) &&
               (currOperand.getType() == Operand.OPERAND_REG) ) {
            int regNum = ( (Integer) currOperand.getValue()).intValue();
            unusedDefsSoFar.add(regNum);
          }
        }


      }
      for (int i=0; i<= unusedDefsSoFar.getMaxVal();i++) {
        if (unusedDefsSoFar.contains(i)) {
          if (!livenessOut.contains(i)) {
            System.out.println("Liveness Analysis Warning: In Block " +
                currBlock.getBlockNum() + ", reg " + i + " is defined " +
                "but not live out");
          }
        }
      }
    }
  }

  private void updateLivenessForOpers (Function func) {
      // we will walk through each block, from last oper to first
      // for each oper, if a reg is defined, we remove it from the current
      //    live set
      // if a reg is used by the oper, we add it to the live set
      // then, after updating for defs and uses, we store the current live set
      // as oper's live set

      // the current live set had better equal livenessIn, or we are confused
    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                    currBlock = currBlock.getNextBlock()) {

      BitArraySet currentLiveness = new BitArraySet (currBlock.getLivenessOut());
        // walk backwards through opers
      for (Operation currOper = currBlock.getLastOper(); currOper != null;
                    currOper = currOper.getPrevOper()) {

        for (int i=0; i < Operation.MAX_DEST_OPERANDS; i++) {
          Operand currOperand = currOper.getDestOperand(i);
          if ( (currOperand != null) &&
               (currOperand.getType() == Operand.OPERAND_REG) ) {
            int regNum = ( (Integer) currOperand.getValue()).intValue();
            currentLiveness.remove(regNum);
          }
        }


          // update for uses
        for (int i=0; i < Operation.MAX_SRC_OPERANDS; i++) {
          Operand currOperand = currOper.getSrcOperand(i);
          if ( (currOperand != null) &&
               (currOperand.getType() == Operand.OPERAND_REG) ) {
            int regNum = ( (Integer) currOperand.getValue()).intValue();
            currentLiveness.add(regNum);
          }
        }

          // set oper liveness
        currOper.setLiveRange(new BitArraySet(currentLiveness));

      }
      if (! currentLiveness.equals(currBlock.getLivenessIn())) {
        System.out.println("Liveness Analysis Error: Block " +
             currBlock.getBlockNum() + " liveness In mismatch");
      }
    }
  }


  public void printAnalysis() {

  }
}
