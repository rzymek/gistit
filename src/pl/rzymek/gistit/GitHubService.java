package pl.rzymek.gistit;

import retrofit.http.GET;
import retrofit.http.Path;

public interface GitHubService {
  @GET("/gists/{id}")
  Gist getGist(@Path("id") String id);
}