package jobs;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

import main.Query;
import main.Util;
import result.ResultRetrieverImpl;

public class ResultJob implements Callable<Map<String, Map<String, Integer>>> {

	private ResultRetrieverImpl resultRetriever;
	private Query query;
	
	public ResultJob(ResultRetrieverImpl resultRetriever, Query query) {
		this.resultRetriever = resultRetriever;
		this.query = query;
	}
	
	@Override
	public Map<String, Map<String, Integer>> call() throws Exception {
		String firstToken = query.getTokens().get(0);
		
		if (firstToken.equals("get") || firstToken.equals("query")) {
			String thirdToken = query.getTokens().get(2);
			
			if (thirdToken.equals("summary")) {
				return resultRetriever.summaryResult(new Query(query.getTokens().subList(0, 2)));
			}
			else {
				return resultRetriever.singleResult(query);
			}
		}
		
		else if (firstToken.equals("cfs")) {
			resultRetriever.clearSummary(JobType.FILE);
		}
		
		else if (firstToken.equals("cws")) {
			resultRetriever.clearSummary(JobType.WEB);
		}
		
		else {
			Util.errorMessage("Neispravan query prosledjen ResultJob-u. {" + Arrays.asList(query.getTokens()) + "}");
			return null;
		}
		
		return null;
	}

}
