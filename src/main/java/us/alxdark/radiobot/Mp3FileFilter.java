package us.alxdark.radiobot;

import java.io.File;

import org.apache.commons.io.filefilter.IOFileFilter;

public class Mp3FileFilter implements IOFileFilter {

	@Override
	public boolean accept(File file) {
		return isValidMp3File(file.getName());
	}

	@Override
	public boolean accept(File dir, String name) {
		return isValidMp3File(name);
	}
	
	private boolean isValidMp3File(String name) {
		boolean suffix = name.endsWith(".mp3") ||  name.endsWith(".MP3");
		// Mac creates these files, they are a real drag. Ignore them.
		boolean prefix = !name.startsWith("._");
		return prefix && suffix;
	}

}
