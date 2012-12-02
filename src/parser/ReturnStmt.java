package parser;

import java.io.BufferedWriter;
import java.io.IOException;

import lowlevel.Function;
import lowlevel.Operand;
import lowlevel.Operation;

public class ReturnStmt extends Statement {
	private Expression expression;

	public ReturnStmt() {}
	
	public ReturnStmt(Expression exp) {
		this.expression = exp;
	}
	
	public void genLLCode(Function f) {
		expression.genLLCode(f);
		Operation assign = new Operation(Operation.OPER_ASSIGN, f.getCurrBlock());
		Operand op = new Operand(Operand.OPERAND_REG, expression.regNum);
		assign.setSrcOperand(0, op);
		op = new Operand(Operand.OPERAND_MACRO, "RetReg");
		assign.setDestOperand(0, op);
		f.getCurrBlock().appendOper(assign);
	}
	
	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "ReturnStmt: \n");
		expression.print(tab + "\t", out);
	}
}
