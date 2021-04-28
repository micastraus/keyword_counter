package result;

import java.util.Map;
import java.util.concurrent.Future;

import jobs.Job;
import jobs.JobType;
import main.Query;

public interface ResultRetriever {
	
		public Map<String, Map<String, Integer>> singleResult(Query query);

		public Map<String, Map<String, Integer>> summaryResult(Query query);

		public void addCorpusResult(Job submittedJob, Future<Map<String, Integer>> jobFutureResult);
		
		public void clearSummary(JobType summaryType);

}
