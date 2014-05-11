package org.gistit.auth;

import org.gistit.auth.step.UIAction;

import android.app.Activity;

public abstract class SetupItem {
	public static final UIAction OK = createNoop();
	public static final UIAction FAILED = createNoop();

	private static UIAction createNoop() {
		return new UIAction() {
			@Override
			public void run(Activity parent) {
			}
		};
	}

	public SetupRunner context;
	public boolean done = false;
	public String label;

	public SetupItem(String label) {
		this.label = label;
	}

	public abstract void check(ResultCallback result);

	public void reset() {
	}

	@Override
	public String toString() {
		return label;
	}

}