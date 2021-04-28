package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Util {
	
	public static String keywords[];
	public static String file_corpus_prefix;
	public static long dir_crawler_sleep_time;
	public static long file_scanning_size_limit;
	public static int hop_count;
	public static long url_refresh_time;
	
	public static Map<String, Long> webPageTimestamps = new ConcurrentHashMap<>();
	
	public static void wrongInput() {
		System.err.println("Invalid input. Try another command...");
	}
	
	public static void errorMessage(String message) {
		System.err.println(message);
	}
	
	public static void getConfigValues() throws IOException {
			InputStream input = new FileInputStream("configuration");
			Properties prop = new Properties();
			prop.load(input);
 
			String str = prop.getProperty("keywords");
			keywords = str.split(",");
			
			file_corpus_prefix = prop.getProperty("file_corpus_prefix");
			dir_crawler_sleep_time = Long.parseLong(prop.getProperty("dir_crawler_sleep_time"));
			file_scanning_size_limit = Long.parseLong(prop.getProperty("file_scanning_size_limit"));
			hop_count = Integer.parseInt(prop.getProperty("hop_count"));
			url_refresh_time = Long.parseLong(prop.getProperty("url_refresh_time"));
	}
	
	public static synchronized boolean isWebPageVisited(String url) {
		if (webPageTimestamps.get(url) != null && System.currentTimeMillis() - webPageTimestamps.get(url) < Util.url_refresh_time) {
			System.out.println("ALREADY VISITED <" + url + ">.");
			return true;
		}
		Util.webPageTimestamps.put(url, System.currentTimeMillis());
		return false;
	}

}
