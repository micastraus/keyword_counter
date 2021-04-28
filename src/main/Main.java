package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import crawler.DirCrawler;
import jobs.Job;
import jobs.PoisonJob;
import jobs.WebJob;
import pools.FileThreadPool;
import pools.WebThreadPool;
import result.ResultRetrieverImpl;

public class Main {
	
	public static void main(String[] args) {
		
		LinkedBlockingQueue<Job> jobQueue = new LinkedBlockingQueue<Job>();
		
		ResultRetrieverImpl resultRetriever = new ResultRetrieverImpl(10);
		FileThreadPool fileThreadPool = new FileThreadPool(resultRetriever);
		WebThreadPool webThreadPool = new WebThreadPool(10, resultRetriever);
		
		JobDispatcher jobDispatcher = new JobDispatcher(jobQueue, fileThreadPool, webThreadPool);
		
		DirCrawler crawler = new DirCrawler(jobQueue);
		
		Thread crawlerThread = new Thread(crawler);
		Thread jobDispatcherThread = new Thread(jobDispatcher);
		
		crawlerThread.start();
		jobDispatcherThread.start();
		
		try {
			Util.getConfigValues();
//			System.out.println(Arrays.toString(Util.keywords));
//			System.out.println(Util.file_corpus_prefix);
//			System.out.println(Util.dir_crawler_sleep_time);
//			System.out.println(Util.file_scanning_size_limit);
//			System.out.println(Util.hop_count);
//			System.out.println(Util.url_refresh_time);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		Scanner sc = new Scanner(System.in);
		
		boolean working = true;
		
		System.out.println("Waiting for input...");
		while (working) {
			try {
				String input = sc.nextLine();
				String[] parameters = input.trim().split(" ");
				
				if (parameters[0].equals("ad")) {
					if (parameters.length != 2) {
						Util.wrongInput();
						continue;
					}
					
					File folder = new File(parameters[1]);
					if (folder.exists() && folder.isDirectory()) {
						if (crawler.getRootFolders().contains(parameters[1])) {
							System.out.println(parameters[1] + " already exists.");
							continue;
						}
						crawler.getRootFolders().add(parameters[1]);
					}
					
					else {
						Util.errorMessage(parameters[1] + " doesn't exist or is not a directory.");
					}
				}
				
				else if (parameters[0].equals("aw")) {
					if (parameters.length != 2) {
						Util.wrongInput();
						continue;
					}
					
					try {
						Jsoup.connect(parameters[1]).get();
						if (Util.hop_count < 0) {
							Util.errorMessage("hop_count {"+Util.hop_count+"} can't be less than 0.");
							continue;
						}
						if (Util.isWebPageVisited(parameters[1])) {
							continue;
						}
						jobQueue.add(new WebJob(parameters[1], Util.hop_count, jobQueue));
			        }
			        catch (Exception e) {
			        	Util.errorMessage("Can't reach url. {" + e.getMessage() + "}");
			        	continue;
			        }
					
				}
				
				else if (parameters[0].equals("get")) {
					String[] fileWebParams = parameters[1].split("\\|");
					
					if (parameters.length != 2 || fileWebParams.length != 2 || !fileWebParams[0].equals("file") && !fileWebParams[0].equals("web")) {
						Util.wrongInput();
						continue;
					}
					
					List<String> tokens = new ArrayList<>();
					tokens.add("get");
					tokens.add(fileWebParams[0]);
					tokens.add(fileWebParams[1]);
					
					Future<Map<String, Map<String, Integer>>> future = resultRetriever.retrieveResult(new Query(tokens));
					System.out.println("Getting the result...");
					Map<String, Map<String, Integer>> result = future.get();
					if (result != null) {
						result.forEach((k, v) -> System.out.println((k + ":" + v)));
					}
				}
				
				else if (parameters[0].equals("query")) {
					String[] fileWebParams = parameters[1].split("\\|");
					
					if (parameters.length != 2 || fileWebParams.length != 2 || !fileWebParams[0].equals("file") && !fileWebParams[0].equals("web")) {
						Util.wrongInput();
						continue;
					}
					
					List<String> tokens = new ArrayList<>();
					tokens.add("query");
					tokens.add(fileWebParams[0]);
					tokens.add(fileWebParams[1]);
					
					resultRetriever.retrieveResult(new Query(tokens));
				}
				
				else if (parameters[0].equals("cfs")) {
					List<String> tokens = new ArrayList<>();
					tokens.add("cfs");
					resultRetriever.retrieveResult(new Query(tokens));
				}
				
				else if (parameters[0].equals("cws")) {
					List<String> tokens = new ArrayList<>();
					tokens.add("cws");
					resultRetriever.retrieveResult(new Query(tokens));
				}
				
				
				else if (parameters[0].equals("stop")) {
					// to do stop
					System.out.println("Terminating app...");
					
					working = false;
					crawler.stop();						
					jobQueue.add(new PoisonJob());
					
					fileThreadPool.getForkPool().shutdown();
					webThreadPool.getPool().shutdown();
					resultRetriever.getPool().shutdown();
					
					// forceSleep(), da ne bi main bespotrebno uzimao taskovima unutar tredPulova procesorsko vreme
					while(! fileThreadPool.getForkPool().isTerminated()) {
//						forceSleep(1000);
					}
					System.out.println("File thread pool shut down.");
					
					while(! webThreadPool.getPool().isTerminated()) {
//						forceSleep(1000);
					}
					System.out.println("Web thread pool shut down.");
					
					while(! resultRetriever.getPool().isTerminated()) {
//						forceSleep(1000);
					}
					System.out.println("ResultRetriever thread pool shut down.");
					
					crawlerThread.join();
					jobDispatcherThread.join();
				}
				
				else {
					Util.wrongInput();
				}
				
			}
			
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		sc.close();
		System.out.println("App terminated.");
	}
	
	
	private static void forceSleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
