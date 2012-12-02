package scanner;

public enum TokenType {
	// keywords
	ELSE,
	IF,
	INT,
	RETURN,
	VOID,
	WHILE,
	
	// operands
	PLUS,
	MINUS,
	MULTIPLY,
	DIVIDE,
	ASSIGN,
	
	// comparators
	LESS_THAN,
	LT_EQUAL,
	GREATER_THAN,
	GT_EQUAL,
	EQUAL,
	NOT_EQUAL,
	
	// matching pairs
	LEFT_PAREN,
	RIGHT_PAREN,
	LEFT_SQUARE,
	RIGHT_SQUARE,
	LEFT_CURLY,
	RIGHT_CURLY,
	START_COMMENT,
	END_COMMENT,
	
	// const literals
	ID,
	NUM,
	
	// misc
	SEMICOLON,
	COMMA,
	EOF,
	UNKNOWN
}
