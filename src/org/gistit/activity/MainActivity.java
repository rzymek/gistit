package org.gistit.activity;

import org.gistit.App;
import org.gistit.R;
import org.gistit.auth.ResultAdapter;
import org.gistit.auth.SetupRunner;
import org.gistit.ex.RESTException;
import org.gistit.model.Gist;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		saveSharedMessage();
		final MainActivity self = this;
		setContentView(R.layout.activity_gist_it);
		newText = (TextView) findViewById(R.id.newText);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		new SetupRunner(app()).run(new ResultAdapter(){
			@Override
			public void passed() {
				setEnabled(true);
				setTitle(app().gistName);
				maybyProcessIntent();				
			}
			@Override
			public void failed() {
				startActivity(new Intent(self, SetupChecklistActivity.class));
				finish();
			}
		});

	}

	protected App app() {
		return (App) getApplication();
	}

	private void maybyProcessIntent() {
		String msg = app().msg;
		if (msg != null) {
			newText.setText(msg);
			updateGistAsync(msg);
		}
	}

	private void saveSharedMessage() {
		Intent intent = getIntent();
		if (intent != null) {
			if (Intent.ACTION_SEND.equals(intent.getAction())) {
				String value = intent.getStringExtra(Intent.EXTRA_TEXT);
				String message = (value == null ? "" : value);
				String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
				if (TextUtils.isEmpty(subject))
					app().msg = message;
				else
					app().msg = "[" + subject + "](" + message + ")";
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICK_GIST && resultCode == RESULT_OK) {
			this.app().gistId = data.getStringExtra("gist.id");
			setTitle(data.getStringExtra("gist"));
			maybyProcessIntent();
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
			showGistPicker();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void updateGistAsync(String text) {
		new AsyncTask<String, String, String>() {
			private RESTException lastError;

			@Override
			protected void onPreExecute() {
				lastError = null;
				progressBar.setVisibility(View.VISIBLE);
				setEnabled(false);
			};

			@Override
			protected String doInBackground(String... params) {
				try {
					App app = app();
					Gist gist = app.github.getGist(app().gistId);
					String content = gist.getContent();
					if (PickGistActivity.EMPTY_GIST_HACK.equals(content))
						content = "";
					String append = params[0];
					gist.getDefaultFile().content = append + "  \n" + content;
					publishProgress(gist.getDefaultFile().content);
					app.github.update(app().gistId, gist);
					return append;
				} catch (RESTException ex) {
					lastError = ex;
					return null;
				}
			}

			@Override
			protected void onProgressUpdate(String... values) {
				newText.setText(values[0]);
			}

			@Override
			protected void onPostExecute(String result) {
				if (lastError != null) {
					if(lastError.status == 404) {
						toast("Previously selected Gist not found. ");
						showGistPicker();
						return;
					}
					toast("Error: "+lastError);
					return;
				}
				toast("Gist updated");
				app().msg = null;
				finish();
			};
		}.execute(text);
	}

	protected void showGistPicker() {
		startActivityForResult(new Intent(this, PickGistActivity.class), PICK_GIST);
	}

	protected void toast(String msg) {
		Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
	}

	protected void setEnabled(boolean enabled) {
		newText.setEnabled(enabled);
		newText.setFocusable(enabled);
	}

}
