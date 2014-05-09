package org.gistit.activity;

import java.util.HashMap;
import java.util.List;

import org.gistit.R;
import org.gistit.activity.base.ListSelectionActivity;
import org.gistit.model.Gist;
import org.gistit.model.GistFile;
import org.gistit.util.SPair;

import android.graphics.Typeface;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PickGistActivity extends ListSelectionActivity<Gist> implements OnItemClickListener {

	/** Can't create an empty gist. Use this text instead on initial commit (to get gistId). */
	public static final String EMPTY_GIST_HACK = "Created with GistIt";

	protected void init() {
		adapter.clear();
		app.github.listGists(new ListRESTCallback<List<Gist>>() {
			@Override
			public void ok(List<Gist> gists) {
				Gist newPublic = new Gist();
				newPublic.isPublic = true;
				newPublic.description = "Create new public gist";

				Gist newSecret = new Gist();
				newSecret.isPublic = false;
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
	protected ArrayAdapter<Gist> createAdapter() {
		return new ArrayAdapter<Gist>(this, LAYOUT_ID) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				Gist item = getItem(position);
				View view = super.getView(position, convertView, parent);
				TextView text = (TextView) view;
				if (item.id == null) {
					text.setTypeface(text.getTypeface(), Typeface.BOLD);
					view.setBackgroundResource(R.color.newGist);
				} else {
					text.setTypeface(text.getTypeface(), Typeface.NORMAL);
					view.setBackgroundResource(item.isPublic ? R.color.white : R.color.secret);
				}
				return view;
			}
		};
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Gist item = adapter.getItem(position);
		if (item.id != null) {
			setResult(item);
		} else {
			startProgress();
			item = new Gist();
			item.description = "GistIt notes";
			item.files = new HashMap<>();
			item.files.put("links.md", new GistFile(EMPTY_GIST_HACK));
			app.github.createGist(item, new ListRESTCallback<Gist>() {
				@Override
				public void ok(Gist result) {
					setResult(result);
				}

			});
		}
	}

	protected void setResult(Gist item) {
		setResult(SPair.mk("gist.id", item.id), SPair.mk("gist", item));
	}

}
