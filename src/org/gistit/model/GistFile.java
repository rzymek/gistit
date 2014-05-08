package org.gistit.model;

public class GistFile {
	public String content;

	public GistFile() {
		this("");
	}

	public GistFile(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return content;
	}
}
