package result;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jobs.FileJob;
import jobs.Job;
import jobs.JobType;
import jobs.ResultJob;
import jobs.WebJob;
import main.Query;
import main.Util;

public class ResultRetrieverImpl extends ResultRetrieverThreadPool implements ResultRetriever {
	
	private Map<String, Future<Map<String, Integer>>> corpusResults = new ConcurrentHashMap<>();
	private Map<String, Future<Map<String, Integer>>> webPageResults = new ConcurrentHashMap<>();
	
	private Map<String, Map<String, Integer>> summaryCorpusResults = new ConcurrentHashMap<>();
	private Map<String, Map<String, Integer>> summaryWebResults = new ConcurrentHashMap<>();
	private Map<String, Map<String, Integer>> domainWebResults = new ConcurrentHashMap<>();
	
	public ResultRetrieverImpl(int size) {
		super(size);
	}
	
	public Future<Map<String, Map<String, Integer>>> retrieveResult(Query query) {
		return getPool().submit(new ResultJob(this, query));
	}
	

	@Override
	public void clearSummary(JobType summaryType) {
		if (summaryType == JobType.FILE) {
//			summaryCorpusResults = new ConcurrentHashMap<>();
			summaryCorpusResults.clear();
		}
		else if (summaryType == JobType.WEB) {
			summaryWebResults.clear();
		}
	}
	
	// get file|corpus_putanja
	// query file|corpus_putanja
	// get web|domen
	// query web|domen

	@Override
	public Map<String, Map<String, Integer>> singleResult(Query query) {
		if (query.getTokens().size() != 3) {
			Util.errorMessage("singleResult() dobio pogresan query. {" + Arrays.asList(query.getTokens()) + "}");
			return null;
		}
		
		String queryGetToken = query.getTokens().get(0);
		String fileWebToken = query.getTokens().get(1);
		String pathToken = query.getTokens().get(2);
		
		// get file|misa/pera/nekiCorpus
		if (fileWebToken.equals("file")) {
			if (corpusResults.containsKey(pathToken)) {
				if (queryGetToken.equals("query")) {
					if (! corpusResults.get(pathToken).isDone()) {
						System.out.println(pathToken + " isn't done...");
						return null;
					}
				}
				try {
					Map<String, Integer> map = corpusResults.get(pathToken).get();
					Map<String, Map<String, Integer>> resultMap = new ConcurrentHashMap<>();
					resultMap.put(pathToken, map);
					if (queryGetToken.equals("query")) {
						System.out.println(resultMap);
					}
					return resultMap;
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println(pathToken + " corpus doesn't exist.");
			}
		}
		
		else if (fileWebToken.equals("web")) {
			
			Map<String, Map<String, Integer>> resultMap = new ConcurrentHashMap<>();
			
			if (domainWebResults.get(pathToken) != null) {
				resultMap.put(pathToken, domainWebResults.get(pathToken));
				if (queryGetToken.equals("query")) {
					System.out.println(resultMap);
				}
				return resultMap;
			}
			
			Map<String, Integer> domainSumMap = new ConcurrentHashMap<>();
			
			boolean domainExists = false;
			for (String key : webPageResults.keySet()) {
				try {
					URL url = new URL(key);
					String domain = url.getHost();
					if (pathToken.equals(domain)) {
						domainExists = true;
						if (queryGetToken.equals("query")) {
							if (! webPageResults.get(key).isDone()) {
								System.out.println(domain + " isn't done...");
								return null;
							}
						}
						
						domainSumMap = Stream.concat(domainSumMap.entrySet().stream(), webPageResults.get(key).get().entrySet().stream())
									.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));
						
					}
				} catch (MalformedURLException | InterruptedException | ExecutionException e) {
					System.err.println(key + " {" + e.getMessage() +"}");
				}
			}
			
			if (! domainExists) {
				System.out.println("Domain {" + pathToken + "} doesn't exist.");
				return null;
			}
			
			domainWebResults.put(pathToken, domainSumMap);
			
			
			resultMap.put(pathToken, domainSumMap);
			
			if (queryGetToken.equals("query")) {
				System.out.println(resultMap);
			}
			
			return resultMap;
			
		}
		
		return null;
	}

	@Override
	public Map<String, Map<String, Integer>> summaryResult(Query query) {
		if (query.getTokens().size() != 2) {
			Util.errorMessage("summaryResult() dobio pogresan query. {" + Arrays.asList(query.getTokens()) + "}");
			return null;
		}
		
		String queryGetToken = query.getTokens().get(0);
		String fileWebToken = query.getTokens().get(1);
		
		if (fileWebToken.equals("file")) {
			if (! summaryCorpusResults.isEmpty()) {
				if (queryGetToken.equals("query")) {
					summaryCorpusResults.forEach((k, v) -> System.out.println((k + ":" + v)));
				}
				return summaryCorpusResults;
			}
			else {
				if (queryGetToken.equals("query")) {
					for (String key : corpusResults.keySet()) {
						if (! corpusResults.get(key).isDone()) {
							System.out.println("File Summary not ready.");
							return null;
						}
					}
				}
				for (String key : corpusResults.keySet()) {
					try {
						summaryCorpusResults.put(key, corpusResults.get(key).get());
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
				if (queryGetToken.equals("query")) {
					summaryCorpusResults.forEach((k, v) -> System.out.println((k + ":" + v)));
				}
				return summaryCorpusResults;
			}
			
		}
		
		else if (fileWebToken.equals("web")) {
			
			if (! summaryWebResults.isEmpty()) {
				if (queryGetToken.equals("query")) {
					summaryWebResults.forEach((k, v) -> System.out.println(k + " : " + v));
				}
				return summaryWebResults;
			}
			
			for (String key : webPageResults.keySet()) {
				if (queryGetToken.equals("query")) {
					if (! webPageResults.get(key).isDone()) {
						System.out.println(key + " isn't done...");
						return null;
					}
				}
			}
			
			for (String key : webPageResults.keySet()) {
				try {
					URL url;
					url = new URL(key);
					String domain = url.getHost();
					if (summaryWebResults.get(domain) == null) {
						summaryWebResults.put(domain, webPageResults.get(key).get());
					}
					else {
						Map<String, Integer> domainMap = summaryWebResults.get(domain);
						domainMap = Stream.concat(domainMap.entrySet().stream(), webPageResults.get(key).get().entrySet().stream())
								.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));
						summaryWebResults.put(domain, domainMap);
					} 
				} 
				catch (MalformedURLException | InterruptedException | ExecutionException e) {
					System.err.println(key + " {" + e.getMessage() +"}");
				}
			}
			
			if (queryGetToken.equals("query")) {
				summaryWebResults.forEach((k, v) -> System.out.println(k + " : " + v));
			}
			
			return summaryWebResults;
		}
		
		return null;
	}
	
	
	
	@Override
	public void addCorpusResult(Job submittedJob, Future<Map<String, Integer>> jobFutureResult) {
		if (submittedJob.getType() == JobType.FILE) {
			FileJob fileJob = (FileJob)submittedJob;
			corpusResults.put(fileJob.getCorpusName(), jobFutureResult);
		}
		else if (submittedJob.getType() == JobType.WEB) {
			WebJob webJob = (WebJob)submittedJob;
			webPageResults.put(webJob.getUrl(), jobFutureResult);
		}
	}
	
}
