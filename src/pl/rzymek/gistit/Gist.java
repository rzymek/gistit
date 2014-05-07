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
		if (files == null)
			return "";
		Collection<GistFile> values = files.values();
		if (values.isEmpty())
			return "";
		GistFile file = values.iterator().next();
		if (file == null)
			return "";
		return file.content;
	}
}
