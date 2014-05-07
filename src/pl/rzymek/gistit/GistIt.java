package pl.rzymek.gistit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class GistIt extends ActionBarActivity {

	private static final int PICK_GIST = 1;
	private TextView newText;
	private ProgressBar progressBar;
	private String gistId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gist_it);
		newText = (TextView) findViewById(R.id.newText);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		if (savedInstanceState == null) {

			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
			gistId = shared.getString("gist.id", null);
			String msg = getSharedMessage();
			if (gistId == null) {
				Intent intent = new Intent(this, PickGistActivity.class);
				if (msg != null) {
					intent.putExtra("msg", msg);
				}
				startActivityForResult(intent, PICK_GIST);
			} else {
				maybyProcessIntent(msg);
			}
		}
	}

	private void maybyProcessIntent(String msg) {
		if (msg != null) {
			updateGistTask.execute(msg);
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
		if (resultCode != RESULT_OK) {
			finish();
			return;
		}
		if (requestCode == PICK_GIST) {
			gistId = data.getStringExtra("gist.id");
			if (data.hasExtra("msg")) {
				maybyProcessIntent(data.getStringExtra("msg"));
			}
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
			updateGistTask.execute(newText.getText().toString());
			return true;
		} else if (id == R.id.action_cancel) {
			finish();
			return true;
		}else if(id == R.id.action_select_gist){
			startActivityForResult(new Intent(this, PickGistActivity.class), PICK_GIST);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private AsyncTask<String, Void, Void> updateGistTask = new AsyncTask<String, Void, Void>() {
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
			newText.setEnabled(false);
			newText.setFocusable(false);
		};

		@Override
		protected Void doInBackground(String... params) {
			App app = (App) getApplication();
			Gist gist = app.github.getGist(gistId);
			gist.getDefaultFile().content += "  \n" + params[0];
			app.github.update(gistId, gist);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(GistIt.this, "Gist updated", Toast.LENGTH_SHORT).show();
			finish();
			progressBar.setVisibility(View.GONE);
			newText.setEnabled(true);
			newText.setFocusable(true);
		};
	};

}
