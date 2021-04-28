package pools;

import result.ResultRetriever;

public class WebThreadPool extends Pool {
	
	private ResultRetriever resultRetriever;
	
	public WebThreadPool(int size, ResultRetriever resultRetriever) {
		super(size);
		this.resultRetriever = resultRetriever;
	}
	
	public ResultRetriever getResultRetriever() {
		return resultRetriever;
	}

}
