package parser;

import java.io.BufferedWriter;
import java.io.IOException;

import compiler.CMinusCompiler;

import lowlevel.Function;
import lowlevel.Operand;
import lowlevel.Operation;

public class AssignExpression extends Expression {
	private Expression lhs;
	private Expression rhs;
	
	public AssignExpression(Expression l, Expression r) {
		lhs = l;
		rhs = r;
	}
	
	public void genLLCode(Function f) {
		lhs.genLLCode(f);
		rhs.genLLCode(f);
		
		Operation oper = new Operation(Operation.OPER_ASSIGN, f.getCurrBlock());
		f.getCurrBlock().appendOper(oper);
		
		Operand op = new Operand(Operand.OPERAND_REG, lhs.regNum);
		oper.setDestOperand(0, op);
		
		if(rhs instanceof LiteralExpression) {
			op = new Operand(Operand.OPERAND_INT, ((LiteralExpression) rhs).getValue());
		} else {
			op = new Operand(Operand.OPERAND_REG, rhs.regNum);
		}
		oper.setSrcOperand(0, op);
		
		// if its a global then we have to store it
		if(CMinusCompiler.globalHash.containsKey(lhs.getID())) {
			Operation store = new Operation(Operation.OPER_STORE_I, f.getCurrBlock());
			f.getCurrBlock().appendOper(store);
			
			store.setSrcOperand(0, new Operand(Operand.OPERAND_REG, lhs.regNum));
			store.setSrcOperand(1, new Operand(Operand.OPERAND_STRING, lhs.getID()));
		}
	}
	
	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "AssignExpression: \n");
		lhs.print(tab + "\t", out);
		out.write("\n" + tab +  "\t=\n");
		rhs.print(tab + "\t", out);
		out.write("\n");
	}
}
