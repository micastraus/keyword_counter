package jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import crawler.MyFile;
import main.Util;

public class FileJob extends RecursiveTask<Map<String, Integer>> implements Job {
	
	private String corpusName;
	private List<MyFile> corpusFiles;
	int left;
	int right;
	
	private Map<String, Integer> fileJobResult;
	
	public FileJob(String corpusName, List<MyFile> corpusFiles, int left, int right) {
		this.corpusName = corpusName;
		this.corpusFiles = corpusFiles;
		this.left = left;
		this.right = right;
		fileJobResult = new ConcurrentHashMap<>();
	}
	
	@Override
	protected Map<String, Integer> compute() {	
		long corpusSize = 0;
		for (int i = left; i <= right; i++) {
			corpusSize += corpusFiles.get(i).getOriginalFile().length();
		}
		
		if (corpusSize < Util.file_scanning_size_limit || left == right) {
			countWords();
		}
		
		else {
			int mid = left +  (right - left) / 2;
			FileJob subTaskA = new FileJob(corpusName, corpusFiles, left, mid);

			subTaskA.fork();
			
			left = mid  + 1;
			
			Map<String, Integer> taskBresult = compute();
			
			Map<String, Integer> taskAresult = subTaskA.join();
			
			fileJobResult = Stream.concat(taskAresult.entrySet().stream(), taskBresult.entrySet().stream())
					.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));
			
		}
		
		return fileJobResult;
	}
	

	private void countWords() {
		Scanner sc = null;
		String fileName = null;
		try {
			for (int i = left; i <= right; i++) {
				File oridjinaleDatula = corpusFiles.get(i).getOriginalFile();
				fileName = oridjinaleDatula.getName();
				sc = new Scanner(oridjinaleDatula);
				
				while (sc.hasNextLine()) {
					String next = sc.nextLine();
					String[] words = next.split("\\s+");
					for (String word : words) 
					{
						for (String keyword : Util.keywords) {
							if (keyword.equals(word)) {
								if (fileJobResult.get(word) == null) 
								{
									fileJobResult.put(word, 1);
								}
								else 
								{
									fileJobResult.put(word, fileJobResult.get(word) + 1);
								}
							}
						}
					}
					
				}
			}
			
		} catch (FileNotFoundException e) {
			Util.errorMessage(fileName + " doesn't exist.");
//			e.printStackTrace();
		}
		finally {
			if (sc != null) {
				sc.close();
			}
		}
	}
	
	public String getCorpusName() {
		return corpusName;
	}
	
	@Override
	public JobType getType() {
		return JobType.FILE;
	}

	

}
