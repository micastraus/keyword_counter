package pools;

import java.util.concurrent.ForkJoinPool;

import result.ResultRetriever;

public class FileThreadPool {
	
	private ForkJoinPool forkPool;
	private ResultRetriever resultRetriever;
	
	public FileThreadPool(ResultRetriever resultRetriever) {
		forkPool = new ForkJoinPool();
		this.resultRetriever = resultRetriever;
	}
	
	public ForkJoinPool getForkPool() {
		return forkPool;
	}
	
	public ResultRetriever getResultRetriever() {
		return resultRetriever;
	}

}
