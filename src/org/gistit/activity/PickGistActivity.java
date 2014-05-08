package org.gistit.activity;

import java.util.HashMap;
import java.util.List;

import org.gistit.activity.base.ListSelectionActivity;
import org.gistit.model.Gist;
import org.gistit.model.GistFile;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class PickGistActivity extends ListSelectionActivity<Gist> implements OnItemClickListener {

	protected void init() {
		adapter.clear();
		app.github.listGists(new ListRESTCallback<List<Gist>>() {
			@Override
			public void ok(List<Gist> gists) {
				Gist newPublic = new Gist();
				newPublic.isPublic = true;
				newPublic.files = new HashMap<>();
				newPublic.files.put("links.md", new GistFile());
				newPublic.description = "Create new public gist";

				Gist newSecret = new Gist();
				newSecret.isPublic = false;
				newSecret.files = new HashMap<>();
				newSecret.files.put("links.md", new GistFile());
				newSecret.description = "Create new secret gist";

				adapter.add(newPublic);
				adapter.add(newSecret);
				for (Gist gist : gists) {
					adapter.add(gist);
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Gist item = adapter.getItem(position);
		if (item.id != null) {
			setResult("gist.id", item.id);
		} else {
			progress.setVisibility(View.VISIBLE);
			item.description = "GistIt notes";
			app.github.createGist(item, new ListRESTCallback<Gist>() {
				@Override
				public void ok(Gist result) {
					setResult("gist.id", result.id);
				}

			});
		}
	}

}
