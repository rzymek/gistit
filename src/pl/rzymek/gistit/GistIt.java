package pl.rzymek.gistit;

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

public class GistIt extends ActionBarActivity {

	private TextView newText;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gist_it);
		newText = (TextView) findViewById(R.id.newText);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);		
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			if (intent != null) {
				if (Intent.ACTION_SEND.equals(intent.getAction())) {
					String message = defaultString(intent.getStringExtra(Intent.EXTRA_TEXT), "");
					String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
					String msg = TextUtils.isEmpty(subject) ? message : "[" + subject + "](" + message + ")";
					updateGistTask.execute(msg);
					newText.setText(msg);
				}
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
		}
		return super.onOptionsItemSelected(item);
	}

	private AsyncTask<String, Void, Void> updateGistTask = new AsyncTask<String, Void, Void>() {
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
			newText.setEnabled(false);
		};

		@Override
		protected Void doInBackground(String... params) {
			App app = (App) getApplication();
			String id = "4299973c43fa6964bce1";
			Gist gist = app.github.getGist(id);
			gist.getDefaultFile().content += "  \n" + params[0];
			app.github.update(id, gist);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Toast.makeText(GistIt.this, "Gist updated", Toast.LENGTH_SHORT).show();
			finish();
			progressBar.setVisibility(View.INVISIBLE);
			newText.setEnabled(true);
		};
	};

}
