package parser;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;

import scanner.CMinusScanner;
import scanner.Scanner;
import scanner.Token;
import scanner.TokenType;

/**
 * A Parser implementation for the C- language.
 * @author Josh Abernathy and Luke van der Hoeven
 *
 */
public class CMinusParser implements Parser {
	private Scanner scanner;
	private Token currentToken;
	
	public CMinusParser(Scanner scanner) {
		this.scanner = scanner;
	}

	@Override
	public Program parse() {
		Program program = null;
		
		currentToken = scanner.getNextToken();
		
		try {
			program = parseProgram();
		} catch (ParseErrorException e) {
			System.out.println(e);
		}
		
		return program;
	}

	private Program parseProgram() throws ParseErrorException {
		Program program = new Program();
		
		program.addDeclaration(parseDeclaration());
		
		// parse declarations until we run out
		while(doesMatch(TokenType.INT) || doesMatch(TokenType.VOID)) {
			program.addDeclaration(parseDeclaration());
		}
		
		return program;
	}

	private Declaration parseDeclaration() throws ParseErrorException {
		VariableType varType = null;
		String id;
		
		if(doesMatch(TokenType.INT)) {
			varType = VariableType.INT;
		} else if(doesMatch(TokenType.VOID)) {
			varType = VariableType.VOID;
		}
		
		// grab the ID off the front
		currentToken = scanner.getNextToken();
		id = currentToken.getTokenData().toString();
		match(TokenType.ID);
		
		if(doesMatch(TokenType.LEFT_PAREN)) {
			FunctionDeclaration declaration = new FunctionDeclaration(id, varType);
			parseFunctionDeclaration(declaration);
			return declaration;
		} else if(doesMatch(TokenType.LEFT_SQUARE) || doesMatch(TokenType.SEMICOLON)) {
			VariableDeclaration declaration = new VariableDeclaration(id, varType);
			parsePartialArray(declaration);
			return declaration;
		}
		
		return null;
	}
	
	private void parseFunctionDeclaration(FunctionDeclaration d) throws ParseErrorException {
		match(TokenType.LEFT_PAREN);
		
		d.setParameters(parseParameters());
		
		match(TokenType.RIGHT_PAREN);
		
		d.setStatement(parseCompoundStatement());
	}
	
	private ReturnStmt parseReturnStatement() throws ParseErrorException {
		ReturnStmt stmt = new ReturnStmt();
		
		match(TokenType.RETURN);
		if(!doesMatch(TokenType.SEMICOLON)) {
			stmt = new ReturnStmt(parseExpression());
		}
		
		match(TokenType.SEMICOLON);
		
		return stmt;
	}
	
	private IterationStmt parseWhileStatement() throws ParseErrorException {
		match(TokenType.WHILE);
		match(TokenType.LEFT_PAREN);
		
		Expression e = parseExpression();
		
		match(TokenType.RIGHT_PAREN);
					
		Statement s = parseStatement();
		
		return new IterationStmt(e, s);
	}
	
	private SelectionStmt parseIfStatement() throws ParseErrorException {
		SelectionStmt stmt = new SelectionStmt();
		match(TokenType.IF);
		match(TokenType.LEFT_PAREN);
		
		stmt.expression = parseExpression();
		
		match(TokenType.RIGHT_PAREN);
		
		stmt.ifStmt = parseStatement();
		
		// get the else part if we have one
		if(doesMatch(TokenType.ELSE)) {
			match(TokenType.ELSE);
			
			stmt.elseStmt = parseStatement();
		}
		
		return stmt;
	}
	
	private ExpressionStmt parseExpressionStatement() throws ParseErrorException {
		// we could just have an empty expression
		if(!doesMatch(TokenType.SEMICOLON)) {
			return new ExpressionStmt(parseExpression());
		}
		
		match(TokenType.SEMICOLON);
		
		return new ExpressionStmt();
	}

	private Statement parseStatement() throws ParseErrorException {
		//return statement parsing
		if(doesMatch(TokenType.RETURN)) {
			return parseReturnStatement();
		} else if(doesMatch(TokenType.WHILE)) { 		// while statement parsing
			return parseWhileStatement();
		} else if(doesMatch(TokenType.IF)) {	// if statement parsing
			return parseIfStatement();
		} else if(doesMatch(TokenType.ID) || // parsing expression statement 
				  doesMatch(TokenType.LEFT_PAREN) ||
				  doesMatch(TokenType.NUM) || doesMatch(TokenType.SEMICOLON)) {
			return parseExpressionStatement();
		} else if(doesMatch(TokenType.LEFT_CURLY)){
			return parseCompoundStatement();
		}

		//this would mean we didn't find anything? probably an error
		return null;
	}
	
	private CompoundStatement parseCompoundStatement() throws ParseErrorException {
		CompoundStatement statement = new CompoundStatement();
		
		match(TokenType.LEFT_CURLY);
		
		while(doesMatch(TokenType.INT)) { 
			statement.addVariableDeclaration(parseVariableDeclaration());
		}
		
		while(doesMatch(TokenType.ID) || doesMatch(TokenType.LEFT_PAREN) || 
			  doesMatch(TokenType.NUM) || doesMatch(TokenType.LEFT_CURLY) ||
			  doesMatch(TokenType.IF) || doesMatch(TokenType.WHILE) || 
			  doesMatch(TokenType.RETURN) || doesMatch(TokenType.SEMICOLON)) {
			statement.addStatement(parseStatement());
		}
		
		match(TokenType.RIGHT_CURLY);
				
		return statement;
	}
	
	private void parsePartialArray(VariableDeclaration d) throws ParseErrorException {
		if(doesMatch(TokenType.LEFT_SQUARE)) {
			match(TokenType.LEFT_SQUARE);
			
			int offset = new Integer(currentToken.getTokenData().toString());
			currentToken = scanner.getNextToken();
			
			match(TokenType.RIGHT_SQUARE);
			match(TokenType.SEMICOLON);
			
			d.arraySize = offset;
		} else if(doesMatch(TokenType.SEMICOLON)) {
			match(TokenType.SEMICOLON);
		}
	}

	private VariableDeclaration parseVariableDeclaration() throws ParseErrorException {		
		match(TokenType.INT);
		String id = currentToken.getTokenData().toString();
		match(TokenType.ID);
		
		VariableDeclaration declaration = new VariableDeclaration(id, VariableType.INT);
		parsePartialArray(declaration);
		
		return declaration;
	}
	
	private Expression parseExpression() throws ParseErrorException {
		if(doesMatch(TokenType.ID)) {
			String id = currentToken.getTokenData().toString();
			match(TokenType.ID);
			return parseExpression2(id);
		} else if(doesMatch(TokenType.LEFT_PAREN) || doesMatch(TokenType.NUM)) {
			return parseSimpleExpression(null);
		}
		
		return null;
	}
	
	private Expression parseExpression2(String id) throws ParseErrorException {
		if(doesMatch(TokenType.ASSIGN)) {
			match(TokenType.ASSIGN);
			return new AssignExpression(new VariableExpression(id), parseExpression());
		} else if(doesMatch(TokenType.LEFT_SQUARE)) {
			return parseExpression3(parseVariableExpression(id));
		} else if(doesMatch(TokenType.LEFT_PAREN)) {
			Expression lhs = parseCall(id);
			return parseSimpleExpression(lhs);
		} else if(doesMatch(TokenType.PLUS) || doesMatch(TokenType.MINUS) ||
				  doesMatch(TokenType.MULTIPLY) || doesMatch(TokenType.DIVIDE) ||
				  isRelationalOp()) {
			return parseSimpleExpression(new VariableExpression(id));
		} else if(doesMatch(TokenType.SEMICOLON) || doesMatch(TokenType.RIGHT_PAREN) || 
				  doesMatch(TokenType.COMMA) || doesMatch(TokenType.RIGHT_SQUARE)) {
			return new VariableExpression(id);
		}
		
		return null;
	}
	
	private Expression parseVariableExpression(String id) throws ParseErrorException {
		match(TokenType.LEFT_SQUARE);
		Expression inner = parseExpression();
		match(TokenType.RIGHT_SQUARE);
		
		return new VariableExpression(id, inner);
	}
	
	private Expression parseExpression3(Expression lhs) throws ParseErrorException {
		if(doesMatch(TokenType.ASSIGN)) {
			match(TokenType.ASSIGN);
			return new AssignExpression(lhs, parseExpression());
		} else if(doesMatch(TokenType.PLUS) || doesMatch(TokenType.MINUS) ||
				  doesMatch(TokenType.MULTIPLY) || doesMatch(TokenType.DIVIDE) ||
				  isRelationalOp()) {
			Expression newLhs = parseAdditiveExpression(lhs);
			
			if(isRelationalOp()) {
				Operator op = getOperator(currentToken);
				currentToken = scanner.getNextToken();
				return new BinaryExpression(op, newLhs, parseAdditiveExpression(null));
			}
			
			return newLhs;
		} else if(doesMatch(TokenType.SEMICOLON) || doesMatch(TokenType.RIGHT_PAREN)) {
			return lhs;
		}
		
		return null;
	}

	private Expression parseSimpleExpression(Expression lhs) throws ParseErrorException {
		Expression newLhs = parseAdditiveExpression(lhs);
		
		if(isRelationalOp()) {
			Operator op = getOperator(currentToken);
			currentToken = scanner.getNextToken();
			Expression rhs = parseAdditiveExpression(null);
			return new BinaryExpression(op, newLhs, rhs);
		}
		
		return newLhs;
	}

	private Expression parseAdditiveExpression(Expression lhs) throws ParseErrorException {
		Expression term = parseTerm(lhs);
		
		while(doesMatch(TokenType.PLUS) || doesMatch(TokenType.MINUS)) {
			Operator op = getOperator(currentToken);
			currentToken = scanner.getNextToken();
			term = new BinaryExpression(op, term, parseTerm(null));
		}
		
		return term;
	}
	
	private Expression parseTerm(Expression lhs) throws ParseErrorException {
		Expression exp = (lhs == null) ? parseFactor() : lhs;
		
		while(doesMatch(TokenType.MULTIPLY) || doesMatch(TokenType.DIVIDE)) {
			Operator op = getOperator(currentToken);
			currentToken = scanner.getNextToken();
			exp = new BinaryExpression(op, exp, parseFactor());
		}
		
		return exp;
	}

	private Expression parseFactor() throws ParseErrorException {
		if(doesMatch(TokenType.LEFT_PAREN)) {
			match(TokenType.LEFT_PAREN);
			Expression e = parseExpression();
			match(TokenType.RIGHT_PAREN);
			return e;
		} else if(doesMatch(TokenType.NUM)) {
			Expression e = new LiteralExpression(currentToken.getTokenData().toString());
			match(TokenType.NUM);
			return e;
		} else if(doesMatch(TokenType.ID)) {
			String id = currentToken.getTokenData().toString();
			match(TokenType.ID);
			return parseFactor2(id);
		}
		
		return null;
	}

	private Expression parseFactor2(String id) throws ParseErrorException {
		if(doesMatch(TokenType.LEFT_PAREN)) {
			return parseCall(id);
		} else if(doesMatch(TokenType.LEFT_SQUARE)) {
			return parseVariableExpression(id);
		}
		
		return new VariableExpression(id);
	}

	private CallExpression parseCall(String id) throws ParseErrorException {
		match(TokenType.LEFT_PAREN);
		
		ArrayList<Expression> args = parseArgs();
		
		match(TokenType.RIGHT_PAREN);
		
		return new CallExpression(id, args);
	}
	
	private ArrayList<Expression> parseArgs() throws ParseErrorException {
		ArrayList<Expression> args = new ArrayList<Expression>();
		boolean readMore = true;
		
		while(readMore) {
			if(doesMatch(TokenType.ID) || doesMatch(TokenType.LEFT_PAREN) ||
			   doesMatch(TokenType.NUM)) {
				args.add(parseExpression());
				
				if(!doesMatch(TokenType.COMMA)) {
					readMore = false;
				} else {
					match(TokenType.COMMA);
				}
			} else {
				readMore = false;
			}
		}
		
		return args;
	}
	
	private ArrayList<Variable> parseParameters() throws ParseErrorException {
		ArrayList<Variable> variables = new ArrayList<Variable>();
		
		if(doesMatch(TokenType.INT)) {
			match(TokenType.INT);
			variables.add(parseParameter());
			
			while(doesMatch(TokenType.COMMA)) {
				match(TokenType.COMMA);
				match(TokenType.INT);
				variables.add(parseParameter());
			}
		} else if(doesMatch(TokenType.VOID)) {
			match(TokenType.VOID);
		}
		
		return variables;
	}
	
	private Variable parseParameter() throws ParseErrorException {
		Variable var = new Variable(currentToken.getTokenData().toString());
		currentToken = scanner.getNextToken();
		
		if(doesMatch(TokenType.LEFT_SQUARE)) {
			match(TokenType.LEFT_SQUARE);
			var.isArray = true;
			match(TokenType.RIGHT_SQUARE);
		}
		
		return var;
	}
	
	private void match(TokenType type) throws ParseErrorException {
		if(currentToken.getTokenType() != type) {
			throw new ParseErrorException("Match failed. " + currentToken.getTokenType() + " != " + type);
		}
		
		currentToken = scanner.getNextToken();
	}
	
	private boolean isRelationalOp() {
		return doesMatch(TokenType.LESS_THAN) || doesMatch(TokenType.LT_EQUAL) || 
			   doesMatch(TokenType.EQUAL) || doesMatch(TokenType.GT_EQUAL) ||
			   doesMatch(TokenType.GREATER_THAN);
	}
	
	private Operator getOperator(Token token) {
		Operator op = null;
		
		if(doesMatch(TokenType.PLUS)) {
			op = Operator.ADD;
		} else if(doesMatch(TokenType.MINUS)) {
			op = Operator.SUBTRACT;
		} else if(doesMatch(TokenType.MULTIPLY)) {
			op = Operator.MULTIPLY;
		} else if(doesMatch(TokenType.DIVIDE)) {
			op = Operator.DIVIDE;
		} else if(doesMatch(TokenType.GREATER_THAN)) {
			op = Operator.GREATER_THAN;
		} else if(doesMatch(TokenType.GT_EQUAL)) {
			op = Operator.GT_EQUAL;
		} else if(doesMatch(TokenType.EQUAL)) {
			op = Operator.EQUAL;
		} else if(doesMatch(TokenType.LESS_THAN)) {
			op = Operator.LESS_THAN;
		} else if(doesMatch(TokenType.LT_EQUAL)) {
			op = Operator.LT_EQUAL;
		}
		
		return op;
	}
	
	private boolean doesMatch(TokenType type) {
		return currentToken.getTokenType() == type;
	}
	
	public static void main(String[] args) {
		String testFile = "test4";
    	CMinusScanner scanner = null;
    	try {
			scanner = new CMinusScanner(testFile + ".cm");
		} catch (FileNotFoundException e) {
			System.err.println("CMinus file not found.");
		}
		
		Parser parser = new CMinusParser(scanner);
		Program program = parser.parse();
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(testFile + ".ast"));
			program.printTree(out);
			out.close();
		} catch(Exception e) {
			System.out.println("Print error: " + e);
		}
	}

}
