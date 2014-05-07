package pl.rzymek.gistit;

import java.io.IOException;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidApacheClient;
import retrofit.client.Client;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Application;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

public class App extends Application {
	private static final String ACCOUNT_TYPE = "com.github";
	public GitHubService github;
	protected String token;
	private Callback noop = new Callback() {		
		@Override
		public boolean handleMessage(Message msg) {
			return false;
		}
	};

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
		fetchGithubAuthToken(noop);
	}

	public void fetchGithubAuthToken(final Callback onTokenReady) {
		if(token != null)
			onTokenReady.handleMessage(null);
		AccountManager service = (AccountManager) getSystemService(ACCOUNT_SERVICE);
		Account[] accounts = service.getAccountsByType(ACCOUNT_TYPE);
		if (accounts.length == 0) {
			Log.e("GET_TOKEN", "no github account");
			return;
		}
//		if(accounts.length > 1)
//			showAccountSelector
		Account account = accounts[0];

		@SuppressWarnings("deprecation")
		final AccountManagerFuture<Bundle> future = service.getAuthToken(account, ACCOUNT_TYPE, true, null, null);

		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				try {
					Bundle result = future.getResult();
					return result != null ? result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN) : null;
				} catch (OperationCanceledException | AuthenticatorException | IOException e) {
					e.printStackTrace();
					return null;
				}
			}

			protected void onPostExecute(String result) {
				token = result;
				onTokenReady.handleMessage(null);
			};
		}.execute();

	}
}
