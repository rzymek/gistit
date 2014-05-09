package org.gistit.auth.setup;

import org.gistit.App;
import org.gistit.activity.PickGistActivity;
import org.gistit.activity.SetupChecklistActivity;

import android.content.Intent;

public class GistPicked extends SetupItem {
	private boolean shownPicker = false;

	public GistPicked(SetupChecklistActivity setupActivity, String label) {
		super(setupActivity, label);
	}

	protected boolean check() {
		App app = app();
		if (app.gistId != null)
			return true;
		app.loadPickedGist();
		if (app.gistId == null) {
			//don't show again on Back (unless reset)
			if (!shownPicker) {
				self.startActivity(new Intent(self, PickGistActivity.class));
			}
			shownPicker = true;
			return false;
		}
		return true;
	}

	public void reset() {
		shownPicker = false;
	}
}