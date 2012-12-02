package parser;

import java.io.BufferedWriter;
import java.io.IOException;

import lowlevel.BasicBlock;
import lowlevel.Function;
import lowlevel.Operand;
import lowlevel.Operation;

public class SelectionStmt extends Statement {
	protected Expression expression;
	protected Statement ifStmt;
	protected Statement elseStmt;
	
	public void genLLCode(Function f) {
		BasicBlock ifBlock = new BasicBlock(f);
		BasicBlock elseBlock = new BasicBlock(f);
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
		op = new Operand(Operand.OPERAND_BLOCK, (elseStmt == null) ? post.getBlockNum() : elseBlock.getBlockNum());
		oper.setSrcOperand(2, op);
	
		eval.appendOper(oper);
		
		f.appendToCurrentBlock(ifBlock);
		f.setCurrBlock(ifBlock);
		ifStmt.genLLCode(f);
				
		if(elseStmt != null) {
			BasicBlock current = f.getCurrBlock();
			f.appendUnconnectedBlock(elseBlock);
			f.setCurrBlock(elseBlock);
			elseStmt.genLLCode(f);
			
			oper = new Operation(Operation.OPER_JMP, f.getCurrBlock());
			op = new Operand(Operand.OPERAND_BLOCK, post.getBlockNum());
			oper.setSrcOperand(0, op);
			elseBlock.appendOper(oper);
			f.setCurrBlock(current);
		}
		
		f.appendToCurrentBlock(post);
		f.setCurrBlock(post);			
	}

	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "SelectionStmt: if (\n");
		expression.print(tab + "\t", out);
		out.write(tab + " )\n");
		ifStmt.print(tab + "\t", out);
		if(elseStmt != null) {
			out.write("\n" + tab + "else\n");
			elseStmt.print(tab + "\t", out);
		}
	}
}
