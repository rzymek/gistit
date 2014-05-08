package org.gistit.activity;

import org.gistit.App;
import org.gistit.AuthRequestResult;
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

public class GistIt extends ActionBarActivity {
	private static final int PICK_GIST = 1;
	public static final int ACCESS_REQUEST = 3;
	public static final int ACCOUNT_SELECTED = 4;

	private TextView newText;
	private ProgressBar progressBar;
	private String gistId;
	private Authenticator authenticator = new Authenticator(this);

	private boolean showingAccessRequest = false;
	private boolean waitingForAccessConfirm = false;

	@Override
	protected void onPause() {
		super.onPause();
		if (showingAccessRequest)
			waitingForAccessConfirm = true;
		else
			waitingForAccessConfirm = false;
		Log.w("XXX", "pause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.w("XXX", "resume");
		if (waitingForAccessConfirm) {
			waitingForAccessConfirm = false;
			showingAccessRequest = false;
			authenticator.fetchGithubAuthTokenUI(new AuthRequestResult() {
				@Override
				public void denied(Intent intent) {
					finish();
				}

				@Override
				public void allowed(String token) {
					run();
				}
			});
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gist_it);
		newText = (TextView) findViewById(R.id.newText);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		if (savedInstanceState == null) {
			login();
		}
	}

	protected void login() {
		authenticator.fetchGithubAuthTokenUI(new AuthRequestResult() {

			@Override
			public void denied(Intent intent) {
				if (intent != null) {
					startActivityForResult(intent, ACCESS_REQUEST);
				} else {
					finish();
				}
			}

			@Override
			public void allowed(String token) {
				run();
			}
		});
	}

	public void run() {
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
			maybyProcessIntent();
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
				String message = defaultString(intent.getStringExtra(Intent.EXTRA_TEXT), "");
				String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
				String msg = TextUtils.isEmpty(subject) ? message : "[" + subject + "](" + message + ")";
				return msg;
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
				return;
			}
			gistId = data.getStringExtra("gist.id");
			maybyProcessIntent();
			break;
		case ACCESS_REQUEST:
			showingAccessRequest = true;
			Log.w("XXX", "ACCESS_REQUEST");
			break;
		case ACCOUNT_SELECTED:
			if (resultCode != RESULT_OK) {
				finish();
				return;
			}
			login();
		}
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
				Toast.makeText(GistIt.this, "Gist updated", Toast.LENGTH_SHORT).show();
				finish();
			};
		}.execute(text);
	}
}
