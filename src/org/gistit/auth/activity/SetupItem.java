package org.gistit.auth.activity;

import org.gistit.App;
import org.gistit.activity.SetupChecklistActivity;

import android.accounts.AccountManager;

public abstract class SetupItem {
	protected final SetupChecklistActivity self;
	public boolean done = false;
	public String label;

	public SetupItem(SetupChecklistActivity setupActivity, String label) {
		this.self = setupActivity;
		this.label = label;
	}

	protected abstract boolean check();

	public void reset() {
	}

	public boolean execute() {
		return done = check();
	}

	@Override
	public String toString() {
		return label;
	}

	protected App app() {
		return (App) self.getApplication();
	}

	protected AccountManager getAccountManager() {
		return AccountManager.get(self);
	}
}