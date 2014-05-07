package pl.rzymek.gistit;

import java.io.IOException;

import retrofit.RestAdapter;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GistIt extends ActionBarActivity {

	private GitHubService github;
	private PlaceholderFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gist_it);

		if (savedInstanceState == null) {
			fragment = new PlaceholderFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
			Log.i("XXXX", fragment.newText + "");
			Intent intent = getIntent();
			if (intent != null) {
				if (Intent.ACTION_SEND.equals(intent.getAction())) {
					String message = defaultString(intent.getStringExtra(Intent.EXTRA_TEXT), "");
					String subject = defaultString(intent.getStringExtra(Intent.EXTRA_SUBJECT), "");
					fragment.newText.setText(subject + "\n" + message + "  \n");
				}
			}
		}
		RestAdapter restAdapter = new RestAdapter.Builder()
			.setEndpoint("https://api.github.com")
			.build();
		github = restAdapter.create(GitHubService.class);

		getGithubAuthToken();
	}

	private void getGithubAuthToken() {
		new AsyncTask<Void, Void, String>() {

			private String token;

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
		// readGist();
	}

	private void readGist() {
		new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... params) {
				// String newText = params[0];
				Gist gist = github.getGist("4299973c43fa6964bce1");
				if (gist == null)
					return "none";
				return gist.getContent();
			}

			protected void onPostExecute(String result) {
				fragment.newText.setText(result);
			};
		}.execute("");
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		TextView newText;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_gist_it, container, false);
			newText = (TextView) rootView.findViewById(R.id.newText);
			Log.i("XXX", "newText.f=" + newText);
			return rootView;
		}
	}

}
