package org.gistit.auth;

import org.gistit.App;
import org.gistit.auth.step.Authorized;
import org.gistit.auth.step.GistPicked;
import org.gistit.auth.step.GitHubAccountSelected;

import android.accounts.Account;

public class SetupRunner implements ResultCallback {
	public final SetupItem[] items = {
			new GitHubAccountSelected("GitHub account selected"),
			new Authorized("Authorized"),
			new GistPicked("Gist picked")
	};

	public final App app;
	public Account selectedAccount;
	public boolean silentMode = true;
	public ResultCallback listener;

	public SetupRunner(App app) {
		this.app = app;
		for (SetupItem item : items) {
			item.context = this;
		}
	}

	private int currentStep = 0;

	public void run(ResultCallback listener) {
		this.listener = listener;
		currentStep = 0;
		next();
	}
	
	private void next() {
		if (currentStep >= items.length) {
			listener.passed();
		} else {
			items[currentStep++].check(this);
		}
	}


	private SetupItem current() {
		return items[currentStep-1];
	}
	@Override
	public void failed() {
		current().done = false;
		listener.failed();
	}

	@Override
	public void passed() {
		current().done = true;
		next();
	}

	@Override
	public void uiAction(UIAction action) {
		listener.uiAction(action);
	}

}
