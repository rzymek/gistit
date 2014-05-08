package org.gistit.activity;

import java.util.List;

import org.gistit.App;
import org.gistit.R;
import org.gistit.model.Gist;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class PickGistActivity extends ListActivity implements OnItemClickListener {

	private ArrayAdapter<Gist> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_gist);
		if (savedInstanceState == null) {
			adapter = new ArrayAdapter<Gist>(this, R.layout.gist_list_item);
			getListView().setOnItemClickListener(this);
			setListAdapter(adapter);
			final App app = (App) getApplication();
			adapter.clear();
			app.github.listGists(new Callback<List<Gist>>() {

				@Override
				public void success(List<Gist> gists, Response resp) {
					for (Gist gist : gists) {
						adapter.add(gist);
					}
				}

				@Override
				public void failure(RetrofitError err) {
					Toast.makeText(PickGistActivity.this, err.toString(), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Gist item = adapter.getItem(position);
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = shared.edit();
		edit.putString("gist.id", item.id);
		edit.commit();
		Intent data = new Intent();
		data.putExtra("gist.id", item.id);
		setResult(RESULT_OK, data);
		finish();
	}

}
