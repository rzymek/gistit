package org.gistit.auth.step;

import org.gistit.App;
import org.gistit.activity.PickGistActivity;
import org.gistit.auth.ResultCallback;
import org.gistit.auth.SetupItem;
import org.gistit.auth.UIAction;

import android.app.Activity;
import android.content.Intent;

public class GistPicked extends SetupItem {
	private final UIAction SHOW_GIST_PICKER = new UIAction() {
		
		@Override
		public void run(Activity parent) {
			if(!shownPicker) {
				parent.startActivity(new Intent(parent, PickGistActivity.class));
				shownPicker = true;
			}
		}
	};
	private boolean shownPicker = false;

	public GistPicked(String label) {
		super(label);
	}

	@Override
	public void check(ResultCallback result) {
		App app = context.app;
		if (app.gistId != null) {
			result.passed();
		} else {
			app.loadPickedGist();
			if (app.gistId == null) {
				// don't show again on Back (unless reset)
				result.uiAction(SHOW_GIST_PICKER);
			}else{
				result.passed();
			}
		}
	}

	@Override
	public void reset() {
		shownPicker = false;
	}
}