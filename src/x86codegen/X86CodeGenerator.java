package x86codegen;

import lowlevel.*;

public class X86CodeGenerator {

  private static final int FIRST_PARAM_OFFSET = 8;

  private CodeItem firstItem;

  public X86CodeGenerator(CodeItem first) {
    firstItem = first;
  }

  public void convertToX86() {
    x86ConvertFuncEntryAndExit();
    x86ConvertReturnReg();
    x86ConvertOperations();
  }

  private void x86ConvertFuncEntryAndExit() {

    boolean foundEntry = false;
    boolean foundExit = false;
    for (CodeItem currItem = firstItem; currItem != null;
                                          currItem = currItem.getNextItem()) {
      if (currItem instanceof Data) {
        continue;
      }
      Function func = (Function) currItem;
      for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                      currBlock = currBlock.getNextBlock()) {
        if ( (currBlock.getFirstOper() != null) &&
             (currBlock.getFirstOper().getType() == Operation.OPER_FUNC_ENTRY) ) {
          x86ConvertFuncEntry(currBlock);
          foundEntry = true;
        }
        else if ( (currBlock.getFirstOper() != null) &&
                  (currBlock.getFirstOper().getType() == Operation.OPER_FUNC_EXIT)) {
          x86ConvertFuncExit(currBlock);
          foundExit = true;
        }
      }
    }

    if (!foundEntry) {
      throw new X86CodegenException ("convertFuncEntryExit: entry not found");
    }
    if (!foundExit) {
      throw new X86CodegenException ("convertFuncEntryExit: exit not found");
    }
  }

  private void  x86ConvertFuncEntry(BasicBlock block) {

    int frameSize = 0;
      // remove Func_Entry oper
    block.removeOper(block.getFirstOper());
      // build stack frame:  Push EBP; EBP = ESP  ESP -= framesize
    Operation oper1 = new Operation(Operation.X86_OPER_PUSH,block);
    Operand src0 = new Operand(Operand.OPERAND_MACRO, "EBP");
    oper1.setSrcOperand(0,src0);
    if (block.getFirstOper() == null) {
      block.appendOper(oper1);
    }
    else {
      block.insertOperBefore(block.getFirstOper(), oper1);
    }

    Operation oper2 = new Operation(Operation.X86_OPER_MOV,block);
    src0 = new Operand(Operand.OPERAND_MACRO, "ESP");
    oper2.setSrcOperand(0,src0);
    Operand dest0 = new Operand(Operand.OPERAND_MACRO, "EBP");
    oper2.setDestOperand(0,dest0);
    block.insertOperAfter(oper1, oper2);

    Operation currOper = oper2;

    if (frameSize > 0) {
      Operation oper3 = new Operation(Operation.OPER_SUB_I,block);
      src0 = new Operand(Operand.OPERAND_MACRO, "ESP");
      oper3.setSrcOperand(0,src0);
      Operand src1 = new Operand(Operand.OPERAND_INT, new Integer(frameSize));
      oper3.setSrcOperand(1,src1);
      dest0 = new Operand(Operand.OPERAND_MACRO, "ESP");
      oper3.setDestOperand(0,dest0);
      block.insertOperAfter(oper2, oper3);
    }
      // now move params from memory into register

    Function func = block.getFunc();
    int paramOffset = FIRST_PARAM_OFFSET + frameSize;
    for (FuncParam currParam = func.getfirstParam(); currParam != null;
                          currParam = currParam.getNextParam() ) {
      String name = currParam.getName();
      int regNum = ((Integer) func.getTable().get(name)).intValue();
      Operation loadOper = new Operation(Operation.OPER_LOAD_I,block);
      src0 = new Operand(Operand.OPERAND_MACRO, "ESP");
      loadOper.setSrcOperand(0,src0);
      Operand src1 = new Operand(Operand.OPERAND_INT, new Integer(paramOffset));
      loadOper.setSrcOperand(1,src1);
      dest0 = new Operand(Operand.OPERAND_REG, new Integer(regNum));
      loadOper.setDestOperand(0,dest0);
      block.insertOperAfter(currOper, loadOper);
      currOper = loadOper;

      paramOffset += 4;
    }
  }


  private void  x86ConvertFuncExit(BasicBlock block) {
      // remove Func_Exit oper
    block.removeOper(block.getFirstOper());
      // ESP=EBP   Pop EBP

    if (block.getFirstOper() == null) {
      throw new X86CodegenException("funcExit: no return oper found");
    }

    Operation oper1 = new Operation(Operation.X86_OPER_MOV,block);
    Operand src0 = new Operand(Operand.OPERAND_MACRO, "EBP");
    oper1.setSrcOperand(0,src0);
    Operand dest0 = new Operand(Operand.OPERAND_MACRO, "ESP");
    oper1.setDestOperand(0,dest0);
    block.insertOperBefore(block.getFirstOper(), oper1);

    Operation oper2 = new Operation(Operation.X86_OPER_POP,block);
    dest0 = new Operand(Operand.OPERAND_MACRO, "EBP");
    oper2.setDestOperand(0,dest0);
    block.insertOperAfter(oper1, oper2);
  }

  private void x86ConvertReturnReg() {
    for (CodeItem currItem = firstItem; currItem != null;
                                        currItem = currItem.getNextItem()) {
      if (currItem instanceof Data) {
        continue;
      }
      Function func = (Function) currItem;
      for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                      currBlock = currBlock.getNextBlock()) {
        for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                      currOper = currOper.getNextOper()) {

          for (int i=0; i < Operation.MAX_DEST_OPERANDS; i++) {
            Operand currOperand = currOper.getDestOperand(i);
            if ( (currOperand != null) &&
                 (currOperand.getType() == Operand.OPERAND_MACRO) ) {
              if ( ((String)currOperand.getValue()).compareTo("RetReg") == 0) {
                currOperand.setValue(new String("EAX"));
              }
            }
          }

            // update for uses
          for (int i=0; i < Operation.MAX_SRC_OPERANDS; i++) {
            Operand currOperand = currOper.getSrcOperand(i);
            if ( (currOperand != null) &&
                 (currOperand.getType() == Operand.OPERAND_MACRO) ) {
              if ( ((String)currOperand.getValue()).compareTo("RetReg") == 0) {
                currOperand.setValue(new String("EAX"));
              }
            }
          }
        }
      }
    }
  }
  private void x86ConvertOperations() {
    for (CodeItem currItem = firstItem; currItem != null;
                                        currItem = currItem.getNextItem()) {
      if (currItem instanceof Data) {
        continue;
      }
      Function func = (Function) currItem;
      for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
                      currBlock = currBlock.getNextBlock()) {
        for (Operation currOper = currBlock.getFirstOper(); currOper != null;
                      currOper = currOper.getNextOper()) {
          switch (currOper.getType()) {
            case Operation.OPER_ASSIGN:
              x86ConvertAssignOper(currOper);
              break;
            case Operation.OPER_LT:
            case Operation.OPER_LTE:
            case Operation.OPER_GT:
            case Operation.OPER_GTE:
            case Operation.OPER_EQUAL:
            case Operation.OPER_NOTEQ:
              x86ConvertComparisonOper(currOper);
              break;
            case Operation.OPER_BEQ:
            case Operation.OPER_BNE:
              x86ConvertBranchOper(currOper);
              break;
            case Operation.OPER_PASS:
              x86ConvertPassOper(currOper);
              break;
            case Operation.OPER_MUL_I:
              x86ConvertMulOper(currOper);
              break;
            case Operation.OPER_DIV_I:
              x86ConvertDivOper(currOper);
              break;
            case Operation.OPER_ADD_I:
              x86ConvertAddOper(currOper);
              break;
            case Operation.OPER_SUB_I:
              x86ConvertSubOper(currOper);
              break;
            case Operation.OPER_CALL:
              x86ConvertCallOper(currOper);
              break;
            case Operation.OPER_UNKNOWN:
            case Operation.OPER_FUNC_ENTRY:
            case Operation.OPER_FUNC_EXIT:
            case Operation.OPER_RETURN:
            case Operation.OPER_JMP:
            case Operation.OPER_LOAD_I:
            case Operation.OPER_STORE_I:
            case Operation.X86_OPER_PUSH:
            case Operation.X86_OPER_POP:
            case Operation.X86_OPER_MOV:
            case Operation.X86_OPER_CMP:
            case Operation.X86_OPER_BEQ:
            case Operation.X86_OPER_BNE:
            case Operation.X86_OPER_BLT:
            case Operation.X86_OPER_BLE:
            case Operation.X86_OPER_BGT:
            case Operation.X86_OPER_BGE:
            case Operation.X86_OPER_MUL_I:
            case Operation.X86_OPER_DIV_I:
              break;
            default:
              throw new X86CodegenException("convertOper: unexpected op type");
          }
        }
      }
    }
  }

  public void x86ConvertAssignOper(Operation oper) {
      // Simply change to a MOV oper
    oper.setType(Operation.X86_OPER_MOV);
  }

  public void x86ConvertComparisonOper(Operation oper) {
      // here we convert the comparison to a CMP
      // and then convert the subsequent branch to the correct type
    BasicBlock block = oper.getBlock();
    int type = oper.getType();
    Operation cmp = new Operation(Operation.X86_OPER_CMP, block);

    if (oper.getSrcOperand(0).getType() == Operand.OPERAND_INT) {
      Operation mov = new Operation(Operation.X86_OPER_MOV, block);
      Operand src = new Operand(oper.getSrcOperand(0));
      mov.setSrcOperand(0,src);
      int regNum = block.getFunc().getNewRegNum();
      Operand dest = new Operand(Operand.OPERAND_REG,new Integer(regNum));
      mov.setDestOperand(0,dest);
      block.insertOperBefore(oper,mov);

      cmp.setSrcOperand(0, new Operand(mov.getDestOperand(0)));
    }
    else {
      cmp.setSrcOperand(0, new Operand(oper.getSrcOperand(0)));
    }

    cmp.setSrcOperand(1, new Operand(oper.getSrcOperand(1)));
    Operand dest = new Operand(Operand.OPERAND_MACRO,"Flags");
    cmp.setDestOperand(0, dest);

    block.insertOperAfter(oper, cmp);
    block.removeOper(oper);

      // the next operation should be the branch
    Operation oldBranch = cmp.getNextOper();
    if (oldBranch == null) {
      throw new X86CodegenException ("convertCompare: next op null");
    }

    Operation newBranch;

    if (oldBranch.getType() == Operation.OPER_BEQ) {
      newBranch = new Operation(x86GetReverseBranchTypeFromCmp(type), block);
    }
    else if (oldBranch.getType() == Operation.OPER_BNE) {
      newBranch = new Operation(x86GetBranchTypeFromCmp(type), block);
    }
    else {
      throw new X86CodegenException ("convertCompare: next op not branch");
    }
    Operand flags = new Operand(Operand.OPERAND_MACRO,"Flags");
    newBranch.setSrcOperand(0,oldBranch.getSrcOperand(2));
    newBranch.setSrcOperand(1,flags);
    block.insertOperAfter(cmp, newBranch);
    block.removeOper(oldBranch);
  }


  public void x86ConvertBranchOper(Operation oper) {
      // Most branches will have been converted to x86 form when the
      // comparison was discovered.  However, some don't have comparison
      // operations associated with them, and we fix them here.
      // We also need to be sure operands are correct

      // We first generate the correct CMP oper, then convert the branch
    BasicBlock block = oper.getBlock();

    Operation cmp = new Operation(Operation.X86_OPER_CMP, block);
    if (oper.getSrcOperand(0).getType() == Operand.OPERAND_INT) {
      Operation mov = new Operation(Operation.X86_OPER_MOV, block);
      Operand src = new Operand(oper.getSrcOperand(0));
      mov.setSrcOperand(0,src);
      int regNum = block.getFunc().getNewRegNum();
      Operand dest = new Operand(Operand.OPERAND_REG,new Integer(regNum));
      mov.setDestOperand(0,dest);
      block.insertOperBefore(oper,mov);

      cmp.setSrcOperand(0, new Operand(mov.getDestOperand(0)));
    }
    else {
      cmp.setSrcOperand(0, new Operand(oper.getSrcOperand(0)));
    }
    cmp.setSrcOperand(1, new Operand(oper.getSrcOperand(1)));
    Operand dest = new Operand(Operand.OPERAND_MACRO,"Flags");
    cmp.setDestOperand(0, dest);
    block.insertOperBefore(oper,cmp);

      // finally we convert branch to a x86 version
    if (oper.getType() == Operation.OPER_BEQ) {
      oper.setType(Operation.X86_OPER_BEQ);
    }
    else if (oper.getType() == Operation.OPER_BNE) {
      oper.setType(Operation.X86_OPER_BNE);
    }
    oper.setSrcOperand(0,oper.getSrcOperand(2));
    oper.setSrcOperand(1, new Operand(Operand.OPERAND_MACRO, "Flags"));
    oper.setSrcOperand(2, null);
  }

  public int x86GetBranchTypeFromCmp(int type) {
    switch (type) {
      case Operation.OPER_LT:
        return Operation.X86_OPER_BLT;
      case Operation.OPER_LTE:
        return Operation.X86_OPER_BLE;
      case Operation.OPER_GT:
        return Operation.X86_OPER_BGT;
      case Operation.OPER_GTE:
        return Operation.X86_OPER_BGE;
      case Operation.OPER_EQUAL:
        return Operation.X86_OPER_BEQ;
      case Operation.OPER_NOTEQ:
        return Operation.X86_OPER_BNE;
      default:
        throw new X86CodegenException("BranchTypeFromCmp: unexpected type");
    }
  }

  public int x86GetReverseBranchTypeFromCmp(int type) {
    switch (type) {
      case Operation.OPER_LT:
        return Operation.X86_OPER_BGE;
      case Operation.OPER_LTE:
        return Operation.X86_OPER_BGT;
      case Operation.OPER_GT:
        return Operation.X86_OPER_BLE;
      case Operation.OPER_GTE:
        return Operation.X86_OPER_BLT;
      case Operation.OPER_EQUAL:
        return Operation.X86_OPER_BNE;
      case Operation.OPER_NOTEQ:
        return Operation.X86_OPER_BEQ;
      default:
        throw new X86CodegenException("BranchTypeFromCmp: unexpected type");
    }
  }
  public void x86ConvertPassOper(Operation oper) {
      // Simply change PASS to PUSH
    oper.setType(Operation.X86_OPER_PUSH);
  }

  public void x86ConvertMulOper(Operation oper) {
      // The multiply uses particular regs.  In particular, the src0 must be
      // EAX and the dest must be EAX/EDX
      // Also, the src1 must be a reg
    BasicBlock block = oper.getBlock();
      // x86 muls have different form, so change type
    oper.setType(Operation.X86_OPER_MUL_I);

    Operation preMov1 = new Operation(Operation.X86_OPER_MOV, block);
    preMov1.setSrcOperand(0,oper.getSrcOperand(0));
    preMov1.setDestOperand(0, new Operand(Operand.OPERAND_MACRO, "EAX"));
    block.insertOperBefore(oper, preMov1);
      // src1 must be a reg, so add mov if necessary
    if (oper.getSrcOperand(1).getType() == Operand.OPERAND_INT) {
      Operation preMov2 = new Operation(Operation.X86_OPER_MOV, block);
      preMov2.setSrcOperand(0,oper.getSrcOperand(1));
      int regNum = block.getFunc().getNewRegNum();
      preMov2.setDestOperand(0, new Operand(Operand.OPERAND_REG, new Integer(regNum)));
      block.insertOperBefore(oper, preMov2);
      oper.setSrcOperand(1,new Operand(preMov2.getDestOperand(0)));
    }
    oper.setSrcOperand(0, new Operand(Operand.OPERAND_MACRO, "EAX"));
      // now move EAX to original dest reg
    Operation postMov = new Operation(Operation.X86_OPER_MOV, block);
    postMov.setDestOperand(0, oper.getDestOperand(0));
    postMov.setSrcOperand(0, new Operand(Operand.OPERAND_MACRO, "EAX"));
    block.insertOperAfter(oper,postMov);
    oper.setDestOperand(0, new Operand(Operand.OPERAND_MACRO, "EAX"));
    oper.setDestOperand(1, new Operand(Operand.OPERAND_MACRO, "EDX"));
  }

  public void x86ConvertDivOper(Operation oper) {
      // x86 divides take the EAX:EDX combo as source
      // The quotient goes to EAX and the REM goes to EDX
      // Since we deal with 32 bit nums only, we will zero out EDX first

    BasicBlock block = oper.getBlock();
    oper.setType(Operation.X86_OPER_DIV_I);
      // first, zero EDX
    Operation zero = new Operation(Operation.X86_OPER_MOV, block);
    zero.setSrcOperand(0,new Operand(Operand.OPERAND_INT, new Integer(0)));
    zero.setDestOperand(0, new Operand(Operand.OPERAND_MACRO, "EDX"));
    block.insertOperBefore(oper, zero);
      // again, src0 must be EAX
    Operation preMov1 = new Operation(Operation.X86_OPER_MOV, block);
    preMov1.setSrcOperand(0,oper.getSrcOperand(0));
    preMov1.setDestOperand(0, new Operand(Operand.OPERAND_MACRO, "EAX"));
    block.insertOperBefore(oper, preMov1);
      // src1 must be a reg, so add mov if necessary
    if (oper.getSrcOperand(1).getType() == Operand.OPERAND_INT) {
      Operation preMov2 = new Operation(Operation.X86_OPER_MOV, block);
      preMov2.setSrcOperand(0,oper.getSrcOperand(1));
      int regNum = block.getFunc().getNewRegNum();
      preMov2.setDestOperand(0, new Operand(Operand.OPERAND_REG, new Integer(regNum)));
      block.insertOperBefore(oper, preMov2);
      oper.setSrcOperand(1,new Operand(preMov2.getDestOperand(0)));
    }
    oper.setSrcOperand(0, new Operand(Operand.OPERAND_MACRO, "EAX"));
      // must show EDX a source also, in src2
    oper.setSrcOperand(2, new Operand(Operand.OPERAND_MACRO, "EDX"));
      // now move EAX to original dest reg
    Operation postMov = new Operation(Operation.X86_OPER_MOV, block);
    postMov.setDestOperand(0, oper.getDestOperand(0));
    postMov.setSrcOperand(0, new Operand(Operand.OPERAND_MACRO, "EAX"));
    block.insertOperAfter(oper,postMov);
    oper.setDestOperand(0, new Operand(Operand.OPERAND_MACRO, "EAX"));
    oper.setDestOperand(1, new Operand(Operand.OPERAND_MACRO, "EDX"));
  }

  public void x86ConvertAddOper(Operation oper) {
      // x86 adds must have dest and src0 the same.  Src1 can be either reg
      // or immediate.  If src1 matches dest, then we can swap operands
    boolean fixed = false;

    BasicBlock block = oper.getBlock();

    Operand dest = oper.getDestOperand(0);

    if (dest == null) {
      throw new X86CodegenException("convertSub: dest is null");
    }
    if (dest.getType() == Operand.OPERAND_REG) {
      int destReg = ( (Integer) oper.getDestOperand(0).getValue()).intValue();
      // if dest == src0, no mod required
      if (oper.getSrcOperand(0).getType() == Operand.OPERAND_REG) {
        if (destReg == ((Integer) oper.getSrcOperand(0).getValue()).intValue()) {
          return;
        }
      }
        // if src1 == dest, then swap src0 and src1
      if ( (oper.getSrcOperand(1).getType() == Operand.OPERAND_REG) &&
           (destReg == ((Integer) oper.getSrcOperand(1).getValue()).intValue()) ) {
        Operand temp = oper.getSrcOperand(0);
        oper.setSrcOperand(0,oper.getSrcOperand(1));
        oper.setSrcOperand(1, temp);
        return;
      }
    }
    else if (dest.getType() == Operand.OPERAND_MACRO) {
      String destMacro = (String) oper.getDestOperand(0).getValue();
      // if dest == src0, no mod required
      if (oper.getSrcOperand(0).getType() == Operand.OPERAND_MACRO) {
        if (destMacro.equals((String) oper.getSrcOperand(0).getValue())) {
          return;
        }
      }
        // if src1 == dest, then swap src0 and src1
      if ( (oper.getSrcOperand(1).getType() == Operand.OPERAND_MACRO) &&
           (destMacro.equals((String) oper.getSrcOperand(1).getValue()) ) ) {
        Operand temp = oper.getSrcOperand(0);
        oper.setSrcOperand(0,oper.getSrcOperand(1));
        oper.setSrcOperand(1, temp);
        return;
      }
    }
    else {
      throw new X86CodegenException("convertSub: unexpected dest type");
    }

      // otherwise, we have a problem that requires a mov to be inserted
      // R1 = R2 + R3  =>  R1 = R2; R1 = R1 + R3
    Operation preMov1 = new Operation(Operation.X86_OPER_MOV, block);
    preMov1.setSrcOperand(0,oper.getSrcOperand(0));
    int regNum = block.getFunc().getNewRegNum();
    preMov1.setDestOperand(0, new Operand(oper.getDestOperand(0)));
    block.insertOperBefore(oper, preMov1);
      // now fix src0
    oper.setSrcOperand(0,new Operand(oper.getDestOperand(0)));
  }

  public void x86ConvertSubOper(Operation oper) {
      // x86 subs must have dest and src0 the same.  Src1 can be either reg
      // or immediate.  Unlike adds, we can't swap src0 and src1

    BasicBlock block = oper.getBlock();

    Operand dest = oper.getDestOperand(0);
    if (dest == null) {
      throw new X86CodegenException("convertSub: dest is null");
    }
    if (dest.getType() == Operand.OPERAND_REG) {
      int destReg = ( (Integer) oper.getDestOperand(0).getValue()).intValue();
      // if dest == src0, no mod required
      if (oper.getSrcOperand(0).getType() == Operand.OPERAND_REG) {
        if (destReg == ((Integer) oper.getSrcOperand(0).getValue()).intValue()) {
          return;
        }
      }
    }
    else if (dest.getType() == Operand.OPERAND_MACRO) {
      String destMacro = (String) oper.getDestOperand(0).getValue();
      // if dest == src0, no mod required
      if (oper.getSrcOperand(0).getType() == Operand.OPERAND_MACRO) {
        if (destMacro.equals((String) oper.getSrcOperand(0).getValue())) {
          return;
        }
      }
    }
    else {
      throw new X86CodegenException("convertSub: unexpected dest type");
    }
      // we have a problem that requires a mov to be inserted
      // R1 = R2 - R3  =>  R1 = R2; R1 = R1 - R3

    Operation preMov1 = new Operation(Operation.X86_OPER_MOV, block);
    preMov1.setSrcOperand(0,oper.getSrcOperand(0));
    int regNum = block.getFunc().getNewRegNum();
    preMov1.setDestOperand(0, new Operand(oper.getDestOperand(0)));
    block.insertOperBefore(oper, preMov1);
      // now fix src0
    oper.setSrcOperand(0,new Operand(oper.getDestOperand(0)));
  }

  private void x86ConvertCallOper(Operation currOper) {
      // here we need to insert the add oper to move the SP back into place
      // the number of parametes is in the Attribute of the oper
    String attrValue = currOper.findAttribute("numParams");
    if (attrValue == null) {
      throw new X86CodegenException("convertCall: no numParms attr found");
    }
    int numParams = Integer.parseInt(attrValue);
    int offset = numParams << 2;
    if (numParams > 0) {
      BasicBlock currBlock = currOper.getBlock();
      Operation newOper =
          new Operation(Operation.OPER_ADD_I,currBlock);
      Operand src0 = new Operand(Operand.OPERAND_MACRO, "ESP");
      newOper.setSrcOperand(0,src0);
      Operand src1 = new Operand(Operand.OPERAND_INT, new Integer(offset));
      newOper.setSrcOperand(1,src1);
      Operand dest0 = new Operand(Operand.OPERAND_MACRO, "ESP");
      newOper.setDestOperand(0,dest0);
      currBlock.insertOperAfter(currOper, newOper);
    }
  }

}