package pools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Pool {
	
	private ExecutorService pool;
	public Pool(int size) {
		pool = Executors.newFixedThreadPool(size);
	}
	
	public ExecutorService getPool() {
		return pool;
	}

}
