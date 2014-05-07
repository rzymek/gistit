package pl.rzymek.gistit;

import java.util.Collection;
import java.util.Map;

public class Gist {
	public Map<String, GistFile> files;

	@Override
	public String toString() {
		return "" + files;
	}

	public String getContent() {
		GistFile file = getDefaultFile();
		if (file == null)
			return "";
		return file.content;
	}

	public GistFile getDefaultFile() {
		if (files == null)
			return null;
		Collection<GistFile> values = files.values();
		if (values.isEmpty())
			return null;
		return values.iterator().next();
	}
}
