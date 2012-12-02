package dataflow;

import lowlevel.*;

public class DefUseAnalysis {

  CodeItem firstItem;

  public DefUseAnalysis(CodeItem first) {
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
      func.setUpDefUse();

        // Next, we need to set up and perform the BasicBlock level analysis

        // We first determine the gen/kill set for each basic block, then we
        // iterate until in/out sets are stable
//      setUpGenKillSets(func);

        // We first determine the def and use sets for each basic block, then we
        // iterate until in/out sets are stable
      setUpDefAndUseSets(func);

      performIterationOnInOutSets (func);

    }
  }

  public void setUpDefAndUseSets (Function func) {

      // to support this, we need the set of operations which define a
      // particular register
      // we walk each oper to add it to the set
    BitArraySet []defs = new BitArraySet[func.getMaxRegNum()+1];
    for (int i=0; i < defs.length; i++) {
      defs[i] = new BitArraySet(func.getMaxOperNum()+1);
    }

    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                    currBlock = currBlock.getNextBlock()) {
      for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                    currOper = currOper.getNextOper()) {
        if (currOper.hasRegDest()) {
          int regNum = ( (Integer) currOper.getDestOperand(0).getValue()).intValue();
          defs[regNum].add(currOper.getNum());
        }
      }
    }
    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                    currBlock = currBlock.getNextBlock()) {
        // for each block, we walk the block to determine the gen/kill

        // the GEN set is the set of register definitions within the block,
        // minus those definitions whose register is overridden in the block
        // to compute, we simply subtract all other defs of this register from
        // the GEN set, then add the current one

        // the KILL set is the set of register definitions from other opers
        // which are terminated by a definition in the block.  For each
        // reg definition, we add all other definitions to the kill set, then
        // delete this one

      BitArraySet gen = new BitArraySet(func.getMaxOperNum()+1);
      BitArraySet kill = new BitArraySet(func.getMaxOperNum()+1);
      currBlock.setDefUseGen(gen);
      currBlock.setDefUseKill(kill);

      for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                    currOper = currOper.getNextOper()) {

        if (currOper.hasRegDest()) {
          int regNum = ( (Integer) currOper.getDestOperand(0).getValue()).intValue();
            // remove all defs of this reg from gen set (to get rid of those
            // which were earlier in the block), then add this def
          gen = gen.subtract(defs[regNum]);
          gen.add(currOper.getNum());
            // add all defs of this reg to kill set, except for the current def
          kill = kill.union(defs[regNum]);
          kill.remove(currOper.getNum());
        }
      }
    }
  }

  public void setUpGenKillSets (Function func) {

      // to support this, we need the set of operations which define a
      // particular register
      // we walk each oper to add it to the set
    BitArraySet []defs = new BitArraySet[func.getMaxRegNum()+1];
    for (int i=0; i < defs.length; i++) {
      defs[i] = new BitArraySet(func.getMaxOperNum()+1);
    }

    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                    currBlock = currBlock.getNextBlock()) {
      for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                    currOper = currOper.getNextOper()) {
        if (currOper.hasRegDest()) {
          int regNum = ( (Integer) currOper.getDestOperand(0).getValue()).intValue();
          defs[regNum].add(currOper.getNum());
        }
      }
    }
    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                    currBlock = currBlock.getNextBlock()) {
        // for each block, we walk the block to determine the gen/kill

        // the GEN set is the set of register definitions within the block,
        // minus those definitions whose register is overridden in the block
        // to compute, we simply subtract all other defs of this register from
        // the GEN set, then add the current one

        // the KILL set is the set of register definitions from other opers
        // which are terminated by a definition in the block.  For each
        // reg definition, we add all other definitions to the kill set, then
        // delete this one

      BitArraySet gen = new BitArraySet(func.getMaxOperNum()+1);
      BitArraySet kill = new BitArraySet(func.getMaxOperNum()+1);
      currBlock.setDefUseGen(gen);
      currBlock.setDefUseKill(kill);

      for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                    currOper = currOper.getNextOper()) {

        if (currOper.hasRegDest()) {
          int regNum = ( (Integer) currOper.getDestOperand(0).getValue()).intValue();
            // remove all defs of this reg from gen set (to get rid of those
            // which were earlier in the block), then add this def
          gen = gen.subtract(defs[regNum]);
          gen.add(currOper.getNum());
            // add all defs of this reg to kill set, except for the current def
          kill = kill.union(defs[regNum]);
          kill.remove(currOper.getNum());
        }
      }

//      System.out.println("Gen For BB#"+currBlock.getBlockNum());
//      for (int i=0; i< gen.getMaxVal();i++) {
//        if (gen.contains(i)) {
//          System.out.print(i + "  ");
//        }
//      }
//      System.out.println();
//      System.out.println();
//
//      System.out.println("Kill For BB#"+currBlock.getBlockNum());
//      for (int i=0; i< kill.getMaxVal();i++) {
//        if (kill.contains(i)) {
//          System.out.print(i + "  ");
//        }
//      }
//      System.out.println();
//      System.out.println();

    }
  }

  public void performIterationOnInOutSets (Function func) {

    boolean somethingChanged = true;

    while (somethingChanged) {
      somethingChanged = false;

        // iterate through all blocks, computing a new out set
        // if the out set changed, annotate that something has changed


    }
  }

  public void printAnalysis() {

  }


}