package scanner;

public class Token {
	private TokenType tokenType;
	private Object tokenData;

	public Token(TokenType type) {
		this(type, null);
	}
	
	public Token(TokenType type, Object data) {
		tokenType = type;
		tokenData = data;
	}
	
	public TokenType getTokenType() {
		return tokenType;
	}
	
	public Object getTokenData() {
		return tokenData;
	}
	
	public void setTokenType(TokenType type) {
		tokenType = type;
	}
	
	public void setTokenData(Object data) {
		tokenData = data;
	}
	
	/**
	 * Try to translate the identifier into a specific keyword token and 
	 * and clear out their token data.
	 */
	public void translateToKeyword() {
		if(tokenType == TokenType.ID) {
			String id = String.valueOf(tokenData);
			
			if(id.equals("if")) {
				tokenType = TokenType.IF;
				tokenData = null;
			} else if(id.equals("while")) {
				tokenType = TokenType.WHILE;
				tokenData = null;
			} else if(id.equals("else")) {
				tokenType = TokenType.ELSE;
				tokenData = null;
			} else if(id.equals("int")) {
				tokenType = TokenType.INT;
				tokenData = null;
			} else if(id.equals("return")) {
				tokenType = TokenType.RETURN;
				tokenData = null;
			} else if(id.equals("void")) {
				tokenType = TokenType.VOID;
				tokenData = null;
			}
		}
	}
	
	public String toString() {
		if(tokenData == null) return String.valueOf(tokenType);
		else return tokenType + ": " + tokenData;
	}
	
}
