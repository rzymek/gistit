package org.gistit.model;

import java.util.Collection;
import java.util.Map;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class Gist {
	public String id;
	public String description;
	@SerializedName("public")
	public boolean isPublic;
	public Map<String, GistFile> files;

	@Override
	public String toString() {
		String msg = TextUtils.isEmpty(description) ? "" : description;
		if (files == null || files.isEmpty())
			return msg;
		return files.keySet().iterator().next()+": "+msg;
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
