package crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Corpus {
	
	private String name;
	private List<MyFile> corpusFiles = new ArrayList<>();
	
	public Corpus(File corpus) {
		name = corpus.getAbsolutePath();
		corpusFiles = new ArrayList<>();
		for (File file : corpus.listFiles()) {
			MyFile myFile = new MyFile(file);
			corpusFiles.add(myFile);
		}
	}
	
	public boolean isModified() {
		boolean modified = false;
		for (MyFile file : corpusFiles) {
			if (file.getOriginalFile().lastModified() != file.getModified()) {
				file.setModified(file.getOriginalFile().lastModified());
				modified = true;
			}
		}
		return modified;
	}
	
	public String getName() {
		return name;
	}
	
	public List<MyFile> getCorpusFiles() {
		return corpusFiles;
	}

}
