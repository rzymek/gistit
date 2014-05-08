package org.gistit.activity.base;

import org.gistit.App;
import org.gistit.R;
import org.gistit.rest.RESTCallback;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

public abstract class ListSelectionActivity<T> extends ListActivity implements OnItemClickListener {
	protected ArrayAdapter<T> adapter;
	protected App app;
	protected ProgressBar progress;

	protected abstract class ListRESTCallback<K> extends RESTCallback<K> {
		@Override
		protected void always() {
			progress.setVisibility(View.GONE);
		}

		@Override
		public void error(Throwable err) {
			Toast.makeText(ListSelectionActivity.this, err.toString(), Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_gist);
		if (savedInstanceState == null) {
			adapter = new ArrayAdapter<T>(this, R.layout.gist_list_item);
			getListView().setOnItemClickListener(this);
			setListAdapter(adapter);
			progress = (ProgressBar) findViewById(R.id.listProgressBar);
			app = (App) getApplication();

			progress.setVisibility(View.VISIBLE);
			init();
		}
	}

	protected abstract void init();

	protected void setResult(String key, String value) {
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = shared.edit();
		edit.putString(key, value);
		edit.commit();
		Intent data = new Intent();
		data.putExtra(key, value);
		setResult(RESULT_OK, data);
		finish();
	}

}
