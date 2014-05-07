package pl.rzymek.gistit;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.Path;

public interface GitHubService {
  @GET("/gists/{id}")
  Gist getGist(@Path("id") String id);
  @PATCH("/gists/{id}")
  Gist update(@Path("id") String id, @Body Gist gist);
}