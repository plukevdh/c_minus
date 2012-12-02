package compiler;

import parser.*;
import scanner.CMinusScanner;
import lowlevel.*;
import java.util.*;
import java.io.*;

import optimizer.*;
import x86codegen.*;
import dataflow.*;

public class CMinusCompiler {

  public static HashMap globalHash = new HashMap();
  public static String filePrefix;

  public CMinusCompiler() {
  }

  public static void main(String[] args) throws IOException {
    filePrefix = "test5";
    String fileName = filePrefix + ".c";
    Parser myParser = new CMinusParser(new CMinusScanner(fileName));

    BufferedWriter out = new BufferedWriter(new FileWriter(filePrefix + ".ast"));
    
    Program parseTree = myParser.parse();
    parseTree.printTree(out);
    out.close();

    CodeItem lowLevelCode = parseTree.genLLCode();

    fileName = filePrefix + ".ll";
    PrintWriter outFile =
        new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
    lowLevelCode.printLLCode(outFile);
    outFile.close();

    int optiLevel = 2;
    LowLevelCodeOptimizer lowLevelOpti =
          new LowLevelCodeOptimizer(lowLevelCode, optiLevel);
    lowLevelOpti.optimize();

    fileName = filePrefix + ".opti";
    outFile =
        new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
    lowLevelCode.printLLCode(outFile);
    outFile.close();

    X86CodeGenerator x86gen = new X86CodeGenerator(lowLevelCode);
    x86gen.convertToX86();

    fileName = filePrefix + ".x86";
    outFile =
        new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
    lowLevelCode.printLLCode(outFile);
    outFile.close();

//    lowLevelCode.printLLCode(null);

      // simply walks functions and finds in and out edges for each BasicBlock
    ControlFlowAnalysis cf = new ControlFlowAnalysis(lowLevelCode);
    cf.performAnalysis();
//    cf.printAnalysis(null);

      // performs DU analysis, annotating the function with the live range of
      // the value defined by each oper (some merging of opers which define
      // same virtual register is done)
//    DefUseAnalysis du = new DefUseAnalysis(lowLevelCode);
//    du.performAnalysis();
//    du.printAnalysis();

    LivenessAnalysis liveness = new LivenessAnalysis(lowLevelCode);
    liveness.performAnalysis();
    liveness.printAnalysis();

    int numRegs = 7;
    X86RegisterAllocator regAlloc = new X86RegisterAllocator(lowLevelCode, numRegs);
    regAlloc.performAllocation();

    lowLevelCode.printLLCode(null);

    fileName = filePrefix + ".s";
    outFile =
        new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
    X86AssemblyGenerator assembler =
        new X86AssemblyGenerator(lowLevelCode, outFile);
    assembler.generateAssembly();
    outFile.close();
  }

}