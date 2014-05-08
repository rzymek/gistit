package org.gistit.activity;

import org.gistit.App;
import org.gistit.Authenticator;
import org.gistit.R;
import org.gistit.model.Gist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	private static final int PICK_GIST = 1;
	public static final int ACCESS_REQUEST = 3;
	public static final int ACCOUNT_SELECTED = 4;

	private TextView newText;
	private ProgressBar progressBar;
	private String gistId;
	private Authenticator authenticator = new Authenticator(this);

	private boolean checkForAuthOnResume = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gist_it);
		newText = (TextView) findViewById(R.id.newText);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		if (savedInstanceState == null) {
			Runnable uiAction = authenticator.selectAccount(null/*auto*/);
			if (uiAction != null) {
				uiAction.run();
			}
		}
	}

	public void onLoggedIn() {
		checkForAuthOnResume = false;
		App app = (App) getApplication();
		if (app.token == null) {
			Log.wtf("TOKEN", "no token");
			return;
		}
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		gistId = shared.getString("gist.id", null);
		if (gistId == null) {
			startActivityForResult(new Intent(this, PickGistActivity.class), PICK_GIST);
		} else {
			setTitle(shared.getString("gist", "GistIt"));
			maybyProcessIntent();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (checkForAuthOnResume) {
			authenticator.checkForAuthToken();
		}
	}

	private void maybyProcessIntent() {
		String msg = getSharedMessage();
		if (msg != null) {
			updateGistAsync(msg);
			newText.setText(msg);
		}
	}

	private String getSharedMessage() {
		Intent intent = getIntent();
		if (intent != null) {
			if (Intent.ACTION_SEND.equals(intent.getAction())) {
				String value = intent.getStringExtra(Intent.EXTRA_TEXT);
				String message = (value == null ? "" : value);
				String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
				if (TextUtils.isEmpty(subject))
					return message;
				else
					return "[" + subject + "](" + message + ")";
			}
		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case PICK_GIST:
			if (resultCode != RESULT_OK) {
				finish();
			}else{
				this.gistId = data.getStringExtra("gist.id");
				setTitle(data.getStringExtra("gist"));
				maybyProcessIntent();
			}
			break;
		case ACCESS_REQUEST:
			checkForAuthOnResume = true;
			break;
		case ACCOUNT_SELECTED:
			if (resultCode != RESULT_OK) {
				finish();
			}else{
				String accountName = data.getStringExtra("account.name");
				authenticator.selectAccount(accountName);
			}
			break;
		}
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
			updateGistAsync(newText.getText().toString());
			return true;
		} else if (id == R.id.action_cancel) {
			finish();
			return true;
		} else if (id == R.id.action_select_gist) {
			startActivityForResult(new Intent(this, PickGistActivity.class), PICK_GIST);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateGistAsync(String text) {
		new AsyncTask<String, String, String>() {
			@Override
			protected void onPreExecute() {
				progressBar.setVisibility(View.VISIBLE);
				newText.setEnabled(false);
				newText.setFocusable(false);
			};

			@Override
			protected String doInBackground(String... params) {
				App app = (App) getApplication();
				Gist gist = app.github.getGist(gistId);
				String content = gist.getContent();
				if (PickGistActivity.EMPTY_GIST_HACK.equals(content))
					content = "";
				String append = params[0];
				gist.getDefaultFile().content = append + "  \n" + content;
				publishProgress(gist.getDefaultFile().content);
				app.github.update(gistId, gist);
				return append;
			}

			@Override
			protected void onProgressUpdate(String... values) {
				newText.setText(values[0]);
			}

			@Override
			protected void onPostExecute(String result) {
				Toast.makeText(MainActivity.this, "Gist updated", Toast.LENGTH_SHORT).show();
				finish();
			};
		}.execute(text);
	}

}
