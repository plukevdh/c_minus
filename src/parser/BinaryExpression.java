package parser;

import java.io.BufferedWriter;
import java.io.IOException;

import lowlevel.Function;
import lowlevel.Operand;
import lowlevel.Operation;

public class BinaryExpression extends Expression {
	private Operator operator;
	private Expression lhs;
	private Expression rhs;
	
	public BinaryExpression(Operator op, Expression l, Expression r) {
		operator = op;
		lhs = l;
		rhs = r;
	}
	
	public void genLLCode(Function f) {
		lhs.genLLCode(f);
		rhs.genLLCode(f);
		
		Operation oper = new Operation(convertOp(), f.getCurrBlock());
		f.getCurrBlock().appendOper(oper);
		
		regNum = f.getNewRegNum();
		Operand op = new Operand(Operand.OPERAND_REG, regNum);
		oper.setDestOperand(0, op);
		
		if(lhs instanceof LiteralExpression) {
			op = new Operand(Operand.OPERAND_INT, ((LiteralExpression) lhs).getValue());
		} else {
			op = new Operand(Operand.OPERAND_REG, lhs.regNum);
		}
		oper.setSrcOperand(0, op);
		
		if(rhs instanceof LiteralExpression) {
			op = new Operand(Operand.OPERAND_INT, ((LiteralExpression) rhs).getValue());
		} else {
			op = new Operand(Operand.OPERAND_REG, rhs.regNum);
		}
		oper.setSrcOperand(1, op);
	}
	
	private int convertOp() {
		if(operator == Operator.ADD)
			return Operation.OPER_ADD_I;
		else if(operator == Operator.SUBTRACT)
			return Operation.OPER_SUB_I;
		else if(operator == Operator.MULTIPLY)
			return Operation.OPER_MUL_I;
		else if(operator == Operator.DIVIDE)
			return Operation.OPER_DIV_I;
		else if(operator == Operator.EQUAL)
			return Operation.OPER_EQUAL;
		else if(operator == Operator.GREATER_THAN)
			return Operation.OPER_GT;
		else if(operator == Operator.LESS_THAN)
			return Operation.OPER_LT;
		else if(operator == Operator.GT_EQUAL)
			return Operation.OPER_GTE;
		else if(operator == Operator.LT_EQUAL)
			return Operation.OPER_LTE;
		else if(operator == Operator.NOT_EQUAL)
			return Operation.OPER_NOTEQ;
		else
			throw new ParseErrorException("Error: convertOp failed");
	}

	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "BinaryExpression: \n");
		lhs.print(tab + "\t", out);
		out.write("\n" + tab + "\t" + operator.toString() + "\n");
		rhs.print(tab + "\t", out);
		out.write("\n");
	}
}
