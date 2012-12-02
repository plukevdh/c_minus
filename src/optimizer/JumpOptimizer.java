package optimizer;

import lowlevel.*;

public class JumpOptimizer {

  CodeItem firstItem;
  int optimizationLevel;

  public JumpOptimizer(CodeItem first) {
    this(first, 0);
  }

  public JumpOptimizer(CodeItem first, int level) {
    firstItem = first;
    optimizationLevel = level;
  }

  public void optimize () {
    boolean changesMade = true;
    while (changesMade) {
      for (CodeItem currItem = firstItem; currItem != null;
                                          currItem = currItem.getNextItem()) {
        if (currItem instanceof Data) {
          continue;
        }
        Function func = (Function) currItem;

        changesMade = doEmptyBlockOptimization(func);
        changesMade |= doJmpOptimization(func);
        changesMade |= doUnreachableBlockOptimization(func);
      }
    }
  }

    // looks for a jmp or branch, whose tgt is also a jmp
    // also looks for a jmp/branch whose tgt is the fallthrough block
  public boolean doJmpOptimization (Function func) {

    boolean changesMade = false;
    boolean somethingChanged = true;

    while (somethingChanged) {
      somethingChanged = false;
      for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                      currBlock = currBlock.getNextBlock()) {
        for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                      currOper = currOper.getNextOper()) {
          if ( (currOper.getType() != Operation.OPER_JMP) &&
               (!currOper.isBranchOper()) ) {
            continue;
          }
            // OK, we've found a jmp or branch
            // If the target of the branch is a BB with just a jmp, optimize
          int tgtBlockNum;
          Operand tgtOperand;
          if (currOper.getType() == Operation.OPER_JMP) {
            if (currOper.getSrcOperand(0).getType() != Operand.OPERAND_BLOCK) {
              throw new CodeOptimizationException ("JmpOpti: unexpected jmp operand");
            }
            tgtOperand = currOper.getSrcOperand(0);
            tgtBlockNum = ((Integer)tgtOperand.getValue()).intValue();
          }
          else {
            if (currOper.getSrcOperand(2).getType() != Operand.OPERAND_BLOCK) {
              throw new CodeOptimizationException ("JmpOpti: unexpected branch operand");
            }
            tgtOperand = currOper.getSrcOperand(2);
            tgtBlockNum = ((Integer)tgtOperand.getValue()).intValue();
          }
          BasicBlock tgtBlock = BasicBlock.getBlockFromNum(func, tgtBlockNum);
          Operation tgtOper = tgtBlock.getFirstOper();
          if ( (tgtOper != null) &&
               (tgtOper.getType() == Operation.OPER_JMP) ) {
            if (tgtOper.getSrcOperand(0).getType() != Operand.OPERAND_BLOCK) {
              throw new CodeOptimizationException ("JmpOpti: unexpected jmp operand(2)");
            }
            int tgtBlock2Num = ((Integer)tgtOper.getSrcOperand(0).getValue()).intValue();
              // change the target
            tgtOperand.setValue(new Integer(tgtBlock2Num));
            tgtBlockNum = tgtBlock2Num;
            somethingChanged = true;
            changesMade = true;
          }


            // now we look for jmp/branch to successor block
          if (currBlock.getNextBlock() == null) {
            continue;
          }
          int nextBlockNum = currBlock.getNextBlock().getBlockNum();
          if (nextBlockNum != tgtBlockNum) {
            continue;
          }
            // need to double-check that jmp/branch is indeed last oper
          if (currOper != currBlock.getLastOper()) {
            continue;
          }
            // now can eliminate the jmp/branch
          currBlock.setLastOper(currOper.getPrevOper());
          if (currOper.getPrevOper() == null) {
            currBlock.setFirstOper(null);
          }
          else {
            currOper.getPrevOper().setNextOper(null);
          }
          somethingChanged = true;
          changesMade = true;
        }
      }
    }
    return changesMade;
  }

/*************************************************************************/
    // A block can be removed if it is empty, and if no blocks jmp into it
    // We will look for empty blocks, then change any oper jmp/branching into
    // the empty block to jmp/branch to its successor
  public boolean doEmptyBlockOptimization (Function func) {
    boolean changesMade = false;

      // we will first find and eliminate all empty blocks, recording the
      // new mapping in an array remap[].  If block i is eliminated, remap[i]
      // will contain the number of the successor block
      // After we eliminate all blocks, we will make one pass through the
      // function to change all jmp/branch tgts

      // we just let remap init to zeros, since block 0 will never be tgt
    int []remap = new int[func.getMaxBlockNum()+1];

    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                    currBlock = currBlock.getNextBlock()) {
      if (currBlock.getFirstOper() != null) {
        continue;
      }
        // can't eliminate a block if there is no subsequent block
      if (currBlock.getNextBlock() == null) {
        continue;
      }
      int emptyBlockNum = currBlock.getBlockNum();
      int nextBlockNum = currBlock.getNextBlock().getBlockNum();
      remap [emptyBlockNum] = nextBlockNum;

      changesMade = true;
      func.removeBlock(currBlock);
    }
      // now we search for jmps/branches, and check if they need to be remapped
    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                    currBlock = currBlock.getNextBlock()) {
      for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                    currOper = currOper.getNextOper()) {

        int tgtOperandNum;
        if (currOper.getType() == Operation.OPER_JMP) {
          tgtOperandNum = 0;
        }
        else if (currOper.isBranchOper()) {
          tgtOperandNum = 2;

        }
        else {
          continue;
        }
        int currentTgt =
            ((Integer) currOper.getSrcOperand(tgtOperandNum).getValue()).intValue();
        int newTgt = remap[currentTgt];
        if (newTgt != 0) {
          if (newTgt > func.getMaxBlockNum()) {
            throw new CodeOptimizationException("emptyblockopti: unexpected block num");
          }
          currOper.getSrcOperand(tgtOperandNum).setValue(new Integer(newTgt));
        }
      }
    }
    return changesMade;
  }

/*************************************************************************/
    // Here we look for a block whose previous block ends in a jmp, and who is
    // not the tgt of a jmp; we do it iteratively
  public boolean doUnreachableBlockOptimization (Function func) {
    boolean changesMade = false;

      // we will do 3 passes: first will identify blocks whose previous op is
      // a jmp; second, we will check if the block is a tgt; third, if not a
      // tgt, we will walk through and delete blocks

    boolean somethingChanged = true;

    while (somethingChanged) {
      somethingChanged = false;
        // this array will contain 1 if the previous block ends in jmp; the
        // second pass will reset to 0 if tgt of jmp
      int []possibleBlocks = new int[func.getMaxBlockNum()+1];
        // first walk through all blocks looking for jmp as previous op
        // jmp tgt can't be this block
      for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                      currBlock = currBlock.getNextBlock()) {
        if (currBlock == func.getFirstBlock() ) {
          continue;
        }
        Operation lastOper = currBlock.getPrevBlock().getLastOper();
        if (lastOper == null) {
          continue;
        }
        if ( (lastOper.getType() != Operation.OPER_JMP) &&
             (lastOper.getType() != Operation.OPER_RETURN) )  {
          continue;
        }
          // for jmps, if tgt is next block can't do opti
        if (lastOper.getType() == Operation.OPER_JMP) {
          int lastTgt = ((Integer)lastOper.getSrcOperand(0).getValue()).intValue();
          if (lastTgt == currBlock.getBlockNum()) {
            continue;
          }
        }
        possibleBlocks[currBlock.getBlockNum()] = 1;
      }
        // pass 2 - see if block is a tgt of any jmp/branch
      for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                      currBlock = currBlock.getNextBlock()) {

        for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                    currOper = currOper.getNextOper()) {

          int tgtOperandNum;
          if (currOper.getType() == Operation.OPER_JMP) {
            tgtOperandNum = 0;
          }
          else if (currOper.isBranchOper()) {
            tgtOperandNum = 2;
          }
          else {
            continue;
          }
          int tgtBlock =
              ((Integer) currOper.getSrcOperand(tgtOperandNum).getValue()).intValue();
          possibleBlocks[tgtBlock] = 0;
        }
      }
        // pass 3 - eliminate blocks marked
      for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                      currBlock = currBlock.getNextBlock()) {
        if (possibleBlocks[currBlock.getBlockNum()] != 0) {
          func.removeBlock(currBlock);
          somethingChanged = true;
          changesMade = true;
        }
      }
    }
    return changesMade;
  }


}