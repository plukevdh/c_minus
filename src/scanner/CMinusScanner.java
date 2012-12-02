package scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * A C- Scanner.
 * @author Josh Abernathy and Luke van der Hoeven
 *
 */
public class CMinusScanner implements Scanner {

    private BufferedReader inFile = null;
    private Token nextToken = null;
    private char nextCharacter = 65535;

    public CMinusScanner (BufferedReader file) {
        inFile = file;
        
        getNextCharacter();
        nextToken = scanToken();
    }
    
    public CMinusScanner (String filename) throws FileNotFoundException {
    	this(new BufferedReader(new FileReader(filename)));
    }
    
    /**
     * Get the next token from the file.
     */
    public Token getNextToken () {
        Token returnToken = nextToken;
        
        if(returnToken.getTokenType() == TokenType.START_COMMENT) {
        	while(returnToken.getTokenType() != TokenType.END_COMMENT)
        		returnToken = scanToken();
        	returnToken = scanToken();
        }
        
        if (nextToken.getTokenType() != TokenType.EOF)
            nextToken = scanToken();
        
        return returnToken;
    }
    
    /**
     * View the next token but do not advance in the file.
     */
    public Token viewNextToken () {
        return nextToken;
    }
    
    /**
     * Get the next character in the file.
     * @return
     */
    private char getNextCharacter() {
    	char c = nextCharacter;
    	
    	try {
    		nextCharacter = (char) inFile.read();
		} catch (IOException e) {
			System.err.println("Character file read failed.");
		}
		
		return c;
    }
    
    /**
     * View the next character but do not advance in the file.
     * @return
     */
    private char viewNextCharacter() {
    	return nextCharacter;
    }
     
    /**
     * Create an identifier token starting from character <code>c</code>.
     * @param c
     * @return
     */
    private Token createIDToken(char c) {
    	StringBuilder word = new StringBuilder();
    	Token token = new Token(TokenType.ID);
    	
		word.append(c);

		while(isLetter(viewNextCharacter())) {
			word.append(getNextCharacter());
		}
		
		// if we find numbers then it's no longer an ID
		while(isNumber(viewNextCharacter())) {
			token.setTokenType(TokenType.UNKNOWN);
			word.append(getNextCharacter());
		}
		
		token.setTokenData(word);
		
		// try to translate the token into a keyword token
		token.translateToKeyword();
		
		return token;
    }
    
    /**
     * Create a literal token starting from character <code>c</code>.
     * @param c
     * @return
     */
    private Token createLiteralToken(char c) {
    	StringBuilder word = new StringBuilder();
    	Token token = new Token(TokenType.NUM);
    	
		word.append(c);
		
		while(isNumber(viewNextCharacter())) {
			word.append(getNextCharacter());
		}
		
		// if we find letters then it's no longer a number
		while(isLetter(viewNextCharacter())) {
			token.setTokenType(TokenType.UNKNOWN);
			word.append(getNextCharacter());
		}
		
		token.setTokenData(word);
		
		return token;
    }
    
    /**
     * Create symbol token starting from character <code>c</code>.
     * @param c
     * @return
     */
    private Token createSymbolToken(char c) {
    	Token token = new Token(TokenType.UNKNOWN);
    	char next = viewNextCharacter();
		
		switch(c) {
    		case '{':
    			token.setTokenType(TokenType.LEFT_CURLY);
    			break;
    		case '}':
    			token.setTokenType(TokenType.RIGHT_CURLY);
    			break;
    		
    		case '[':
    			token.setTokenType(TokenType.LEFT_SQUARE);
    			break;
    		case ']':
    			token.setTokenType(TokenType.RIGHT_SQUARE);
    			break;
    			
    		case '(':
    			token.setTokenType(TokenType.LEFT_PAREN);
    			break;
    		case ')':
    			token.setTokenType(TokenType.RIGHT_PAREN);
    			break;
    			
    		case '<':
    			token.setTokenType(TokenType.LESS_THAN);
    			
    			if(next == '=') {
    				token.setTokenType(TokenType.LT_EQUAL);
    				getNextCharacter();
    			}
    			break;
    		case '>':
    			token.setTokenType(TokenType.GREATER_THAN);
    			
    			if(next == '=') {
    				token.setTokenType(TokenType.GT_EQUAL);
    				getNextCharacter();
    			}
    			break;
    		
    		case '=':
    			token.setTokenType(TokenType.ASSIGN);
    			
    			if(next == '=') {
    				token.setTokenType(TokenType.EQUAL);
    				getNextCharacter();
    			}
    			break;
    			
    		case '/':
    			token.setTokenType(TokenType.DIVIDE);
    			
    			if(next == '*') {
    				token.setTokenType(TokenType.START_COMMENT);
    				getNextCharacter();
    			}
    			break;
    		
    		case '*':
    			token.setTokenType(TokenType.MULTIPLY);
    			
    			if(next == '/') {
    				token.setTokenType(TokenType.END_COMMENT);
    				getNextCharacter();
    			}
    			break;
    		
    		case '+':
    			token.setTokenType(TokenType.PLUS);
    			break;
    			
    		case '-':
    			token.setTokenType(TokenType.MINUS);
    			break;
    			
    		case '!':
    			token.setTokenType(TokenType.UNKNOWN);
    			
    			if(next == '=') {
    				token.setTokenType(TokenType.NOT_EQUAL);
    				getNextCharacter();
    			}
    			break;
    			
    		case ';':
    			token.setTokenType(TokenType.SEMICOLON);
    			break;
    			
    		case ',':
    			token.setTokenType(TokenType.COMMA);
    			break;
		}
		
		return token;
    }
    
    /**
     * Parse out the next token.
     * @return
     */
    private Token scanToken() {
    	Token token = new Token(TokenType.UNKNOWN);
    	
    	char c = getNextCharacter();
    	
    	// whitespace doesn't count as a token so waste any leading whitespace
    	while(isWhitespace(c)) {
    		c = getNextCharacter();
    	}
    	
    	if(isEOF(c)) {
    		token = new Token(TokenType.EOF);
    	} else if(isLetter(c)) {
    		token = createIDToken(c);
    	} else if(isNumber(c)) {
    		token = createLiteralToken(c);
    	} else {
    		token = createSymbolToken(c);
    	}
    	
    	return token;
    }
    
    private boolean isLetter(char c) {
    	String s = String.valueOf(c).toLowerCase();
    				
    	return 	s.equals("a") || s.equals("b") || s.equals("c") || s.equals("d") || s.equals("e") || s.equals("f") || 
    			s.equals("g") || s.equals("h") || s.equals("i") || s.equals("j") || s.equals("k") || s.equals("l") ||
    			s.equals("m") || s.equals("n") || s.equals("o") || s.equals("p") || s.equals("q") || s.equals("r") ||
    			s.equals("s") || s.equals("t") || s.equals("u") || s.equals("v") || s.equals("w") || s.equals("x") ||
    			s.equals("y") || s.equals("z");
    }
    
    private boolean isNumber(char c) {
    	return 	c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' ||
    	 		c == '7' || c == '8' || c == '9';
    }
    
    private boolean isWhitespace(char c) {
    	return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }
    
    private boolean isEOF(char c) {
    	// Java docs say EOF is -1 but testing shows it is 65535
    	return c == -1 || c == 65535;
    }
    
    public static void main(String[] args) {
    	String testFile = "test4";
    	CMinusScanner scanner = null;
    	try {
			scanner = new CMinusScanner(testFile + ".cm");
		} catch (FileNotFoundException e) {
			System.err.println("CMinus file not found.");
		}
		
		PrintStream out = null;
		try {
			out = new PrintStream(new File(testFile + ".tok"));
		} catch (FileNotFoundException e) {
			System.err.println("Could not create token output file.");
		}
		
		Token t = scanner.getNextToken();
		while(t.getTokenType() != TokenType.EOF) {
			out.println(t);
			
			t = scanner.getNextToken();
		}
    }
    
}
