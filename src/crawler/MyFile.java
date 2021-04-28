package crawler;

import java.io.File;

public class MyFile {
	
	private File originalFile;
	private long modified;
	
	public MyFile(File originalFile) {
		this.originalFile = originalFile;
		this.modified = 0;
	}
	
	public File getOriginalFile() {
		return originalFile;
	}
	
	public long getModified() {
		return modified;
	}
	
	public void setModified(long modified) {
		this.modified = modified;
	}
	
}
