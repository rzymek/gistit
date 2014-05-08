package org.gistit;

import org.gistit.rest.GitHubService;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidApacheClient;
import retrofit.client.Client;
import android.app.Application;
import android.util.Log;

public class App extends Application {
	public GitHubService github;
	public String token;

	@Override
	public void onCreate() {
		super.onCreate();

		Client client = new AndroidApacheClient();
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://api.github.com").setRequestInterceptor(new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				if (token != null)
					request.addHeader("Authorization", "token " + token);
			}
		}).setErrorHandler(new ErrorHandler() {
			@Override
			public Throwable handleError(RetrofitError error) {
				Log.e("RETROFIT", error + "\n" + error.getCause());
				return error.getCause();
			}
		}).setClient(client).build();

		github = restAdapter.create(GitHubService.class);
	}	
}
