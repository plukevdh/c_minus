package x86codegen;

import lowlevel.*;
import java.io.PrintWriter;

public class X86AssemblyGenerator {

  private static final int UNKNOWN = 0;
  private static final int DATA = 1;
  private static final int FUNCTION = 2;

  private CodeItem firstItem;
  private PrintWriter outFile;

  private int state;

  public X86AssemblyGenerator(CodeItem first, PrintWriter out) {
    firstItem = first;
    outFile = out;
    state = UNKNOWN;
  }

  public void generateAssembly() {

    for (CodeItem currItem = firstItem; currItem != null;
                                        currItem = currItem.getNextItem()) {
      if (currItem instanceof Data) {
        if (state != DATA) {
          outFile.println(".data");
          state = DATA;
        }
        outFile.println(".comm\t" + ((Data) currItem).getName() + ",4,4");
        outFile.println();
      }
      else {
        Function func = (Function) currItem;
        peepholeOpti(func);
        generateFunction(func);
      }
    }
  }

  private void generateFunction(Function func) {

    if (state != FUNCTION) {
      outFile.println(".text");
      outFile.println("\t.align 4");
      state = FUNCTION;
    }
    outFile.println(".globl  " + func.getName());
    outFile.println(func.getName() + ":");

    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
          currBlock = currBlock.getNextBlock()) {
      if (currBlock.getBlockNum() != 0) {
        outFile.println(func.getName() + "_bb"+currBlock.getBlockNum()+":");
      }
      for (Operation currOper = currBlock.getFirstOper(); currOper != null;
          currOper = currOper.getNextOper()) {
        switch (currOper.getType()) {

          case Operation.OPER_ADD_I:
          case Operation.OPER_SUB_I:

          case Operation.X86_OPER_MUL_I:
          case Operation.X86_OPER_DIV_I:
            assembleArithmetic(currOper);
            break;

          case Operation.OPER_RETURN:
            outFile.println("\tret");
            break;

          case Operation.OPER_JMP:
            int tgt = ((Integer) currOper.getSrcOperand(0).getValue()).intValue();
            outFile.println("\tjmp\t" + func.getName() + "_bb" + tgt);
            break;

          case Operation.OPER_PASS:
            outFile.print("\tpushl\t");
            Operand src = currOper.getSrcOperand(0);
            if (src.getType() == Operand.OPERAND_REG) {
              outFile.println("%"+((String)src.getValue()));
            }
            else {
              outFile.println("$"+((Integer) src.getValue()).intValue());
            }
            break;

          case Operation.OPER_CALL:
            outFile.println("\tcall\t"+
                    ((String)currOper.getSrcOperand(0).getValue()));
            break;

          case Operation.OPER_LOAD_I:
              // movl  (%ebx), %eax   or   movl   a, %eax  or movl 4(%eax), %ebx
            outFile.print("\tmovl\t");
            Operand src1 = currOper.getSrcOperand(1);
            if (src1 != null) {
              if (src1.getType() == Operand.OPERAND_INT) {
                outFile.print(((Integer)src1.getValue()).intValue());
              }
              else {
                throw new X86CodegenException("assembleLoad: unexpected src1");
              }
            }
            Operand src0 = currOper.getSrcOperand(0);
            if (src0.getType() == Operand.OPERAND_STRING) {
              outFile.print(((String)currOper.getSrcOperand(0).getValue()));
            }
            else {
              outFile.print("(%" +
                  ((String)currOper.getSrcOperand(0).getValue())+")");
            }
            outFile.println(", %" +
                  ((String)currOper.getDestOperand(0).getValue()));
            break;

          case Operation.OPER_STORE_I:
              // movl   %ebx, (%eax)  or  movl $2, (%eax)  or movl %eax, a
            outFile.print("\tmovl\t");
            src0 = currOper.getSrcOperand(0);
            if (src0.getType() == Operand.OPERAND_INT) {
              outFile.print("$" + ((Integer)src0.getValue()).intValue());
            }
            else {
              outFile.print("%" + ((String)src0.getValue()));
            }
            Operand src2 = currOper.getSrcOperand(2);
            if (src2 != null) {
              if (src2.getType() == Operand.OPERAND_INT) {
                outFile.print(((Integer)src2.getValue()).intValue());
              }
              else {
                throw new X86CodegenException("assembleStore: unexpected src2");
              }
            }
            src1 = currOper.getSrcOperand(1);
            if (src1.getType() == Operand.OPERAND_MACRO) {
              outFile.println(", (%" +
                    ((String)src1.getValue()) + ")");
            }
              // else is global
            else {
              outFile.println(", " +
                    ((String)src1.getValue()));
            }
            break;

          case Operation.X86_OPER_PUSH:
            outFile.print("\tpushl\t");
            src0 = currOper.getSrcOperand(0);
            if (src0.getType() == Operand.OPERAND_INT) {
              outFile.println("$" + ((Integer)src0.getValue()).intValue());
            }
            else {
              outFile.println("%" + ((String)src0.getValue()));
            }
            break;

          case Operation.X86_OPER_POP:
            outFile.print("\tpopl\t");
            Operand dest0 = currOper.getDestOperand(0);
            if (dest0.getType() == Operand.OPERAND_INT) {
              outFile.println("$" + ((Integer)dest0.getValue()).intValue());
            }
            else {
              outFile.println("%" + ((String)dest0.getValue()));
            }
            break;

          case Operation.OPER_ASSIGN:
          case Operation.X86_OPER_MOV:
              // movl $2, %eax    or   movl  $eax, $ebx
            outFile.print("\tmovl\t");
            src0 = currOper.getSrcOperand(0);
            if (src0.getType() == Operand.OPERAND_INT) {
              outFile.print("$" + ((Integer)src0.getValue()).intValue());
            }
            else {
              outFile.print("%" + ((String)src0.getValue()));
            }
            outFile.println(", %" + ((String) currOper.getDestOperand(0).getValue()));
            break;


          case Operation.X86_OPER_CMP:
            outFile.print("\tcmpl\t");
            src1 = currOper.getSrcOperand(1);
            if (src1.getType() == Operand.OPERAND_INT) {
              outFile.print("$" + ((Integer)src1.getValue()).intValue() + ", ");
            }
            else {
              outFile.print("%" + ((String)src1.getValue()) + ", ");
            }
            src0 = currOper.getSrcOperand(0);
            if (src0.getType() == Operand.OPERAND_INT) {
              outFile.println("$" + ((Integer)src0.getValue()).intValue());
            }
            else {
              outFile.println("%" + ((String)src0.getValue()));
            }
            break;

          case Operation.X86_OPER_BEQ:
          case Operation.X86_OPER_BNE:
          case Operation.X86_OPER_BLT:
          case Operation.X86_OPER_BLE:
          case Operation.X86_OPER_BGT:
          case Operation.X86_OPER_BGE:
            assembleBranch(currOper);
            break;

          case Operation.OPER_LT:
          case Operation.OPER_LTE:
          case Operation.OPER_GT:
          case Operation.OPER_GTE:
          case Operation.OPER_EQUAL:
          case Operation.OPER_NOTEQ:
          case Operation.OPER_FUNC_ENTRY:
          case Operation.OPER_FUNC_EXIT:
          case Operation.OPER_BEQ:
          case Operation.OPER_BNE:
          case Operation.OPER_MUL_I:
          case Operation.OPER_DIV_I:
          default:
            throw new X86CodegenException("assembler: unknown oper type " +
                      currOper.getType());
        }
      }
    }
  }

  private void assembleArithmetic(Operation oper) {

    if (oper.getType() == Operation.OPER_ADD_I) {
      outFile.print("\taddl\t");
    }
    else if (oper.getType() == Operation.OPER_SUB_I) {
      outFile.print("\tsubl\t");
    }
    else if (oper.getType() == Operation.X86_OPER_MUL_I) {
      outFile.print("\timull\t");
    }
    else if (oper.getType() == Operation.X86_OPER_DIV_I) {
      outFile.print("\tidivl\t");
    }
    else {
      throw new X86CodegenException("Assembler: unexpected arithmetic");
    }

    Operand src1 = oper.getSrcOperand(1);
    if (src1.getType() == Operand.OPERAND_INT) {
      outFile.print("$" + ((Integer)src1.getValue()).intValue());
    }
    else {
      outFile.print("%" + ((String)src1.getValue()));
    }
    outFile.print(", ");
    Operand src0 = oper.getSrcOperand(0);
    if (src0.getType() == Operand.OPERAND_INT) {
      outFile.println("$" + ((Integer)src0.getValue()).intValue());
    }
    else {
      outFile.println("%" + ((String)src0.getValue()));
    }

  }

//  private void assembleCompare(Operation oper) {
//
//  }

  private void assembleBranch(Operation oper) {
    switch (oper.getType()) {
      case Operation.X86_OPER_BEQ:
        outFile.print("\tje\t");
        break;
      case Operation.X86_OPER_BNE:
        outFile.print("\tjne\t");
        break;
      case Operation.X86_OPER_BLT:
        outFile.print("\tjl\t");
        break;
      case Operation.X86_OPER_BLE:
        outFile.print("\tjle\t");
        break;
      case Operation.X86_OPER_BGT:
        outFile.print("\tjg\t");
        break;
      case Operation.X86_OPER_BGE:
        outFile.print("\tjge\t");
        break;
      default:
        throw new X86CodegenException("assembleBranch: bad oper type");
    }
    outFile.println(oper.getBlock().getFunc().getName() + "_bb" +
        ((Integer) oper.getSrcOperand(0).getValue()).intValue());
  }

  private void peepholeOpti(Function func) {
    removeWorthlessMoves(func);
  }

  private void removeWorthlessMoves(Function func) {
      // here we look for movl %EAX, %EAX
    for (BasicBlock currBlock = func.getFirstBlock(); currBlock != null;
          currBlock = currBlock.getNextBlock()) {
      Operation nextOper;
      for (Operation currOper = currBlock.getFirstOper(); currOper != null;
          currOper = nextOper) {
          // we use nextOper here because we may be deleting currOper and would
          // be unable to follow its next ptr at end of loop
        nextOper = currOper.getNextOper();
        if ( (currOper.getType() != Operation.OPER_ASSIGN) &&
             (currOper.getType() != Operation.X86_OPER_MOV) ) {
          continue;
        }
        if (currOper.getDestOperand(0).getType() != Operand.OPERAND_MACRO) {
          continue;
        }
        if (currOper.getSrcOperand(0).getType() != Operand.OPERAND_MACRO) {
          continue;
        }
        String dest = (String) currOper.getDestOperand(0).getValue();
        String src = (String) currOper.getSrcOperand(0).getValue();
        if (dest.compareTo(src) == 0) {
          currBlock.removeOper(currOper);
        }
      }
    }
  }
}