package org.gistit.activity.base;

import org.gistit.App;
import org.gistit.R;
import org.gistit.rest.RESTCallback;

import retrofit.RetrofitError;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

public abstract class ListSelectionActivity<T> extends ListActivity implements OnItemClickListener {
	protected static final int LAYOUT_ID = R.layout.gist_list_item;
	protected ArrayAdapter<T> adapter;
	protected App app;
	private ProgressBar progress;

	protected abstract class ListRESTCallback<K> extends RESTCallback<K> {
		@Override
		protected void always() {
			stopProgress();
		}

		@Override
		public void error(RetrofitError err) {
			String msg = "GistIt: Unspecified REST error";
			if (err != null) {
				err.printStackTrace();
				msg = err.getMessage() + "\n" + "URL: " + err.getUrl() + "\n" + "Cause:" + err.getCause() + "\n" + "Resp:" + err.getResponse();
				Log.e("REST", msg);
			}
			Toast.makeText(ListSelectionActivity.this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_gist);
		if (savedInstanceState == null) {
			adapter = createAdapter();
			getListView().setOnItemClickListener(this);
			setListAdapter(adapter);
			progress = (ProgressBar) findViewById(R.id.listProgressBar);
			app = (App) getApplication();

			startProgress();
			init();
		}
	}

	protected ArrayAdapter<T> createAdapter() {
		return new ArrayAdapter<T>(this, LAYOUT_ID);
	}

	protected abstract void init();

	protected void setResult(@SuppressWarnings("unchecked") Pair<String, String>... data) {
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = shared.edit();
		for (Pair<String, String> pair : data) {
			edit.putString(pair.first, pair.second);
		}
		edit.commit();
		Intent intent = new Intent();
		for (Pair<String, String> pair : data) {
			intent.putExtra(pair.first, pair.second);
		}
		setResult(RESULT_OK, intent);
		finish();
	}

	protected void startProgress() {
		progress.setVisibility(View.VISIBLE);
		getListView().setEnabled(false);
	}

	protected void stopProgress() {
		progress.setVisibility(View.GONE);
		getListView().setEnabled(true);
	}

}
