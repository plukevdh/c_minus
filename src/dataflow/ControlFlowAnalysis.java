package dataflow;

import lowlevel.*;
import java.util.*;
import java.io.*;

public class ControlFlowAnalysis {

  private CodeItem firstItem;

  public ControlFlowAnalysis(CodeItem first) {
    firstItem = first;
  }

    // this routine simply annotates each basic block with its incoming and
    // outgoing control arcs  - changes inEdges and outEdges
  public void performAnalysis () {

    for (CodeItem currItem = firstItem; currItem != null;
                                        currItem = currItem.getNextItem()) {
      if (currItem instanceof Data) {
        continue;
      }
      Function func = (Function) currItem;
      for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
           currBlock = currBlock.getNextBlock()) {
          // for each block, we first find where it goes, and add its tgts to
          // its outEdges.  For each tgt, we add this block to its inEdges

          // we look for branches and jmps; if last oper is not jmp, we also
          // grab the following BB as tgt
        for (Operation currOper = currBlock.getFirstOper(); currOper != null;
             currOper = currOper.getNextOper() ) {
          if ( (currOper.getType() == Operation.OPER_JMP) ||
               (currOper.isBranchOper()) ) {
            int tgtNum;
            BasicBlock tgt;
            if ( (currOper.getType() == Operation.OPER_JMP) ||
                 (currOper.isX86BranchOper()) ) {
              tgtNum = ( (Integer) currOper.getSrcOperand(0).getValue()).intValue();
              tgt = BasicBlock.getBlockFromNum(func,tgtNum);
            }
            else {
              tgtNum = ( (Integer) currOper.getSrcOperand(2).getValue()).intValue();
              tgt = BasicBlock.getBlockFromNum(func,tgtNum);
            }
          currBlock.addOutEdge(tgt);
          tgt.addInEdge(currBlock);
          }
          if (currOper == currBlock.getLastOper()) {
            if ( (currOper.getType() != Operation.OPER_JMP) &&
                 (currOper.getType() != Operation.OPER_RETURN) ) {
              BasicBlock next = currBlock.getNextBlock();
              if (next != null) {
                currBlock.addOutEdge(next);
                next.addInEdge(currBlock);
              }
            }
          }
        }
      }
    }
  }

  public void printAnalysis (PrintWriter outFile) {
    for (CodeItem currItem = firstItem; currItem != null;
                                        currItem = currItem.getNextItem()) {
      if (currItem instanceof Data) {
        continue;
      }
      Function func = (Function) currItem;
      for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
           currBlock = currBlock.getNextBlock()) {
        Object []outs = currBlock.getOutEdges().toArray();
        Object []ins = currBlock.getInEdges().toArray();
        if (outFile == null) {
          System.out.println("Basic Block #" + currBlock.getBlockNum());
          System.out.print("  out: ");
          for (int i=0; i < outs.length; i++) {
            System.out.print(((BasicBlock) outs[i]).getBlockNum() + "   ");
          }
          System.out.println();
          System.out.print("  in:  ");
          for (int i=0; i < ins.length; i++) {
            System.out.print(((BasicBlock) ins[i]).getBlockNum() + "   ");
          }
          System.out.println();
        }
        else {
          outFile.println("Basic Block #" + currBlock.getBlockNum());
          outFile.print("  out: ");
          for (int i=0; i < outs.length; i++) {
            outFile.print(((BasicBlock) outs[i]).getBlockNum() + "   ");
          }
          outFile.println();
          outFile.print("  in:  ");
          for (int i=0; i < ins.length; i++) {
            outFile.print(((BasicBlock) ins[i]).getBlockNum() + "   ");
          }
          outFile.println();
        }
      }
    }
  }
}