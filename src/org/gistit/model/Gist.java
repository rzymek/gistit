package org.gistit.model;

import java.util.Collection;
import java.util.Map;

import android.text.TextUtils;

public class Gist {
	public String id;
	public String description;
	public Map<String, GistFile> files;

	@Override
	public String toString() {
		if (files == null || files.isEmpty())
			return description;
		else 
			return files.keySet().iterator().next() + (TextUtils.isEmpty(description) ? "" : ": " + description);
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
