package main;

import java.util.List;

public class Query {
	
	private List<String> tokens;
	
	public Query(List<String> tokens) {
		this.tokens = tokens;
	}
	
	public List<String> getTokens() {
		return tokens;
	}

}
