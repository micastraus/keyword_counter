package jobs;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import main.Util;

public class WebJob implements Callable<Map<String, Integer>>, Job {

	private String url;
	private int hopCount;
	private BlockingQueue<Job> jobQueue;
	private Map<String, Integer> webPageResult;
	
	public WebJob(String url, int hopCount, BlockingQueue<Job> jobQueue) {
		this.url = url;
		this.hopCount = hopCount;
		this.jobQueue = jobQueue;
		webPageResult = new ConcurrentHashMap<>();
	}
	
	@Override
	public Map<String, Integer> call() throws Exception {				
	        Elements links = null;
	        try {
		        Document doc = Jsoup.connect(url).get();
		        links = doc.select("a[href]");
		        if (hopCount > 0) {
			        for (Element link : links) {
			        	String tmpUrl = link.attr("abs:href").trim();
			        	
			        	if (tmpUrl.equals("") || !tmpUrl.startsWith("http") || 
			        			tmpUrl.endsWith(".pdf") || tmpUrl.endsWith(".jpg") || tmpUrl.endsWith(".png") || tmpUrl.endsWith(".php") ||
			        			Util.isWebPageVisited(tmpUrl)) {
			        		continue;
			        	}
			        	
			        	jobQueue.add(new WebJob(tmpUrl, hopCount - 1, jobQueue));
			        }
		        }
		        
		        String[] text = doc.text().split("\\s+");
		        for (String word : text) {
		        	for (String keyword : Util.keywords) {
		        		if (word.equals(keyword)) {
		        			if (webPageResult.get(word) == null) {
		        				webPageResult.put(word, 1);
							}
							else {
								webPageResult.put(word, webPageResult.get(word) + 1);
							}
		        		}
		        	}
		        }
		        
	        }
	        catch (Exception e) {
	        	Util.errorMessage("Can't open url <" + url + "> {reason:  " + e.getMessage() + "}");
	        	// Ovde sam imao problem kad sam vracao null -> prilikom get() nad future-om, u ResultRetrieveru bi zabolo, i nece da dohvati rezultate
//	        	return new HashMap<>();
	        }

		return webPageResult;
	}
	
	public String getUrl() {
		return url;
	}
	
	public int getHopCount() {
		return hopCount;
	}
	
	@Override
	public JobType getType() {
		return JobType.WEB;
	}
	
}
