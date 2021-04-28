package main;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

import jobs.FileJob;
import jobs.Job;
import jobs.JobType;
import jobs.WebJob;
import pools.FileThreadPool;
import pools.WebThreadPool;

public class JobDispatcher implements Runnable {
	
	
	private BlockingQueue<Job> jobQueue;
	private FileThreadPool fileThreadPool;
	private WebThreadPool webThreadPool;
	
	public JobDispatcher(BlockingQueue<Job> jobQueue, FileThreadPool fileThreadPool, WebThreadPool webThreadPool) {
		this.jobQueue = jobQueue;
		this.fileThreadPool = fileThreadPool;
		this.webThreadPool = webThreadPool;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				Job job = jobQueue.take();
				
				if (job.getType() == JobType.FILE) {
					FileJob fileJob = (FileJob)job;
					System.out.println("Starting file job: " + fileJob.getCorpusName());
					Future<Map<String, Integer>> corpusResult = fileThreadPool.getForkPool().submit(fileJob);
					fileThreadPool.getResultRetriever().addCorpusResult(fileJob, corpusResult);
				}
				
				
				else if (job.getType() == JobType.WEB) {
					WebJob webJob = (WebJob)job;
					System.out.println("Starting web job: " + webJob.getUrl());
					Future<Map<String, Integer>> webPageResult = webThreadPool.getPool().submit(webJob);
					webThreadPool.getResultRetriever().addCorpusResult(webJob, webPageResult);
				}
				
				else if (job.getType() == JobType.POISON) {
					// Terminating...
					break;
				}
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Job dispatcher terminated.");
	}

}
