package parser;

import java.io.BufferedWriter;
import java.io.IOException;

import lowlevel.BasicBlock;
import lowlevel.Function;
import lowlevel.Operand;
import lowlevel.Operation;

public class IterationStmt extends Statement {
	private Expression expression;
	private Statement statement;
	
	public IterationStmt(Expression e, Statement s) {
		expression = e;
		statement = s;
	}
	
	public void genLLCode(Function f) {

		BasicBlock whileLoop = new BasicBlock(f);
		BasicBlock post = new BasicBlock(f);
		
		BasicBlock eval = new BasicBlock(f);
		f.appendToCurrentBlock(eval);
		f.setCurrBlock(eval);
		expression.genLLCode(f);

		Operation oper = new Operation(Operation.OPER_BNE, f.getCurrBlock());
		Operand op = new Operand(Operand.OPERAND_REG, expression.regNum);
		oper.setSrcOperand(0, op);
		op = new Operand(Operand.OPERAND_INT, 0);
		oper.setSrcOperand(1, op);
		op = new Operand(Operand.OPERAND_BLOCK, post.getBlockNum());
		oper.setSrcOperand(2, op);

		eval.appendOper(oper);
		
		f.appendToCurrentBlock(whileLoop);
		f.setCurrBlock(whileLoop);
		statement.genLLCode(f);
		oper = new Operation(Operation.OPER_JMP, f.getCurrBlock());
		op = new Operand(Operand.OPERAND_BLOCK, eval.getBlockNum());
		oper.setSrcOperand(0, op);
		whileLoop.appendOper(oper);
				
		f.appendToCurrentBlock(post);
		f.setCurrBlock(post);
		
	}
	
	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "IterationStmt: \n");
		expression.print(tab + "\t", out);
		statement.print(tab + "\t", out);
		out.write("\n");
	}
}
