package org.gistit.rest;

import java.util.List;

import org.gistit.model.Gist;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;

public interface GitHubService {
	@GET("/gists/{id}")
	Gist getGist(@Path("id") String id);

	@PATCH("/gists/{id}")
	Gist update(@Path("id") String id, @Body Gist gist);

	@GET("/gists")
	void listGists(Callback<List<Gist>> cb);

	@POST("/gists")
	void createGist(@Body Gist item, Callback<Gist> cb);
}