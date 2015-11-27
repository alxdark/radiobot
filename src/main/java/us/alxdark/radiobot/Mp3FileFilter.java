package us.alxdark.radiobot;

import java.io.File;

import org.apache.commons.io.filefilter.IOFileFilter;

public class Mp3FileFilter implements IOFileFilter {

	@Override
	public boolean accept(File file) {
		String name = file.getName();
		return isValidMp3File(name);
	}

	@Override
	public boolean accept(File dir, String name) {
		return isValidMp3File(name);
	}
	
	
	private boolean isValidMp3File(String name) {
		boolean suffix = name.endsWith(".mp3") ||  name.endsWith(".MP3") || name.endsWith(".m4a") || name.endsWith(".M4A");
		// Mac creates these files, they are a real drag. Ignore them.
		boolean prefix = !name.startsWith("._");
		return prefix && suffix;
	}

}
