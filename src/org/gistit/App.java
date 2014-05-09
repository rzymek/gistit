package org.gistit;

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
import android.util.Log;

public class App extends Application {
	public GitHubService github;
	public String token;
	public String gistId;
	public String gistName;
	public String msg;
	public static final String ACCOUNT_NAME = "account.name";

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
				Log.e("RETROFIT", error + "\n" + error.getMessage()+"\n"+error.getCause()+"\n"+error.getResponse().getReason());
				return error.getCause();
			}
		}).setClient(client).build();

		github = restAdapter.create(GitHubService.class);
		loadPickedGist();
	}

	public void loadPickedGist() {
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		gistId = shared.getString("gist.id", null);
		gistName = shared.getString("gist.name", "GistIt");
	}

	public boolean isConfigured() {
		return token != null && gistId != null;
	}
}
