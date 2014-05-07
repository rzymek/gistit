package pl.rzymek.gistit;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

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
			app.fetchGithubAuthToken(new Handler.Callback() {

				@Override
				public boolean handleMessage(Message msg) {
					app.github.listGists(new Callback<List<Gist>>() {

						@Override
						public void success(List<Gist> gists, Response resp) {
							for (Gist gist : gists) {
								adapter.add(gist);
							}
						}

						@Override
						public void failure(RetrofitError err) {
							Log.e("GistIt", "" + err.getCause());
						}
					});
					return false;
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
		Intent intent = getIntent();
		if (intent.hasExtra("msg")) {
			data.putExtra("msg", intent.getStringExtra("msg"));
		}
		setResult(RESULT_OK, data);
		finish();
	}

}
