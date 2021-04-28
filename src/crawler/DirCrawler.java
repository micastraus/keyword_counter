package crawler;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import jobs.FileJob;
import jobs.Job;
import main.Util;

public class DirCrawler implements Runnable {

	private LinkedBlockingQueue<Job> jobQueue;
	
	private Map<String, Corpus> corpusMap = new HashMap<>();
	private List<String> rootFolders = new CopyOnWriteArrayList<>();
	private boolean working = true;
	
	public DirCrawler(LinkedBlockingQueue<Job> jobQueue) {
		this.jobQueue = jobQueue;
	}
	
	@Override
	public void run() {
		while (working) {
			Iterator<String> foldIterator = rootFolders.listIterator();
			
			// iteriramo kroz root foldere (ad "putanjaDoFoldera")
			while (foldIterator.hasNext()) {
				searchFolders(foldIterator.next());
			}
			
			try {
				Thread.sleep(Util.dir_crawler_sleep_time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("DirCrawler terminated.");
	}
	
	private void searchFolders(String dirPath) {
		File rootDir = new File(dirPath);
		
//		System.out.println(rootDir.getAbsolutePath());
		
		for (File file : rootDir.listFiles()) {
			if (file.isDirectory()) {
				if (file.getName().startsWith(Util.file_corpus_prefix)) {
					// pretpostavimo da je u pitanju korpus
					Corpus corp = corpusMap.get(file.getName());
					if (corp == null) {
						corp = new Corpus(file);
						corpusMap.put(file.getName(), corp);
					}
					
					if (corp.isModified()) {
//						System.out.println("Traversing corpus: " + corp.getName());
						// ako je barem jedan fajl u tom korpusu izmenjen, opet obidji taj korpus
						jobQueue.add(new FileJob(file.getName(), corp.getCorpusFiles(), 0, file.listFiles().length-1));
					}
					
				}
				else {
					searchFolders(file.getAbsolutePath());
				}
			}
		}
	}
	
	public void stop() {
		working = false;
	}
	
	public List<String> getRootFolders() {
		return rootFolders;
	}
	
}
