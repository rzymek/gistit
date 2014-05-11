package org.gistit;

import org.gistit.ex.RESTException;
import org.gistit.rest.GitHubService;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidApacheClient;
import retrofit.client.Client;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class App extends Application {
	public GitHubService github;
	public String token;
	public String gistId;
	public String gistName = "GistId";
	public String msg;
	public static final String ACCOUNT_NAME = "account.name";

	@Override
	public void onCreate() {
		super.onCreate();

		Client client = new AndroidApacheClient();
		RestAdapter restAdapter = new RestAdapter.Builder()
			.setEndpoint("https://api.github.com")
			.setRequestInterceptor(new RequestInterceptor() {
				@Override
				public void intercept(RequestFacade request) {
					if (token != null)
						request.addHeader("Authorization", "token " + token);
				}
			})
			.setErrorHandler(new ErrorHandler() {
				@Override
				public Throwable handleError(RetrofitError error) {
					if(error.getCause() != null) {
						return error.getCause();			 			
					}
					return new RESTException(error.getResponse());
				}
			})
			.setClient(client)
			.build();

		github = restAdapter.create(GitHubService.class);
		loadPickedGist();
	}

	public void loadPickedGist() {
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		gistId = shared.getString("gist.id", null);
		gistName = shared.getString("gist.name", "GistIt");
	}

}
