package pl.rzymek.gistit;

import java.io.IOException;

import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class GistIt extends ActionBarActivity {

	private GitHubService github;
	private TextView newText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gist_it);
		newText = (TextView) findViewById(R.id.newText);

		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://api.github.com").setRequestInterceptor(new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				Log.w("header", "" + token);
				request.addHeader("Authorization", "token " + token);
			}
		}).setErrorHandler(new ErrorHandler() {
			@Override
			public Throwable handleError(RetrofitError error) {
				Log.e("RETROFIT", error+"\n"+error.getCause());
				return error.getCause();
			}
		})

		.build();
		github = restAdapter.create(GitHubService.class);
		getGithubAuthToken();

		if (savedInstanceState == null) {
			Intent intent = getIntent();
			if (intent != null) {
				if (Intent.ACTION_SEND.equals(intent.getAction())) {
					String message = defaultString(intent.getStringExtra(Intent.EXTRA_TEXT), "");
					String subject = defaultString(intent.getStringExtra(Intent.EXTRA_SUBJECT), "");
					String msg = TextUtils.isEmpty(subject) ? message : "[" + subject + "](" + message + ")";
					updateGistTask.execute(msg);
					newText.setText(msg);
				}
			}
		}
	}

	private String token;

	private void getGithubAuthToken() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				Log.w("TOKEN", "github login");
				AccountManager service = (AccountManager) getSystemService(ACCOUNT_SERVICE);
				Account[] accounts = service.getAccountsByType("com.github");
				if (accounts.length == 0) {
					return "no github account";
				}
				Account account = accounts[0];
				AccountManagerFuture<Bundle> future = service.getAuthToken(account, "com.github", true, null, null);

				try {
					Bundle result = future.getResult();
					return result != null ? result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN) : null;
				} catch (OperationCanceledException | AuthenticatorException | IOException e) {
					e.printStackTrace();
					return e.toString();
				}
			}

			protected void onPostExecute(String result) {
				token = result;
				Log.w("TOKEN", "" + result);
			};

		}.execute();

	}

	private String defaultString(String value, String defaultValue) {
		return value == null ? defaultValue : value;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gist_it, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_send) {
			sendGist();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void sendGist() {
		updateGistTask.execute(newText.getText().toString());
	}

	private AsyncTask<String, Void, String> updateGistTask = new AsyncTask<String, Void, String>() {
		@Override
		protected String doInBackground(String... params) {
			// String newText = params[0];
			String id = "4299973c43fa6964bce1";
			Gist gist = github.getGist(id);
			gist.getDefaultFile().content += "  \n" + params[0];
			github.update(id, gist);
			return "";
		}

		protected void onPostExecute(String result) {
			finish();
			Toast.makeText(GistIt.this, "Gist updated", Toast.LENGTH_SHORT).show();
		};
	};

}
