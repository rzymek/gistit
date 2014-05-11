package org.gistit.auth.step;

import org.gistit.App;
import org.gistit.activity.SelectAccountActivity;
import org.gistit.activity.SetupChecklistActivity;
import org.gistit.auth.ResultCallback;
import org.gistit.auth.SetupItem;
import org.gistit.auth.UIAction;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class GitHubAccountSelected extends SetupItem {
	private final static String GITHUB_APP = "com.github.mobile";

	private static final UIAction SELECT_ACCOUNT = new UIAction() {

		@Override
		public void run(Activity activity) {
			Intent intent = new Intent(activity, SelectAccountActivity.class);
			activity.startActivityForResult(intent, SetupChecklistActivity.ACCOUNT_SELECTED);
		}
	};

	private final UIAction RUN_GITHUB_APP = new UIAction() {
		@Override
		public void run(final Activity parent) {
			final Intent github = context.app.getPackageManager().getLaunchIntentForPackage(GITHUB_APP);
			if (github == null) {
				ask("Install GitHub ",
						"GistIt uses GitHub accounts. Please install GitHub application to configue one.", parent,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								try {
									Uri uri = Uri.parse("market://details?id=" + GITHUB_APP);
									parent.startActivity(new Intent(Intent.ACTION_VIEW, uri));
								} catch (android.content.ActivityNotFoundException anfe) {
									Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=" + GITHUB_APP);
									parent.startActivity(new Intent(Intent.ACTION_VIEW, uri));
								}
							}
						});
			} else {
				ask("Setup GitHub account", "Please sign in using GitHub application.", parent, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						parent.startActivity(github);
					}
				});
			}
		}
	};

	public GitHubAccountSelected(String label) {
		super(label);
	}

	public void check(ResultCallback result) {
		if (context.selectedAccount != null) {
			result.passed();
			return;
		}
		Account[] accounts = AccountManager.get(context.app).getAccountsByType(SetupChecklistActivity.ACCOUNT_TYPE);
		if (accounts.length == 0) {
			result.uiAction(RUN_GITHUB_APP);
		} else if (accounts.length == 1) {
			context.selectedAccount = accounts[0];
			result.passed();
		} else {
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context.app);
			String accountName = shared.getString(App.ACCOUNT_NAME, null);
			Account account = SetupChecklistActivity.findByName(accounts, accountName);
			if (account != null) {
				context.selectedAccount = account;
				result.passed();
			} else {
				result.uiAction(SELECT_ACCOUNT);
			}
		}
	}

	private void ask(CharSequence title, CharSequence msg, Activity parent, DialogInterface.OnClickListener onConfirm) {
		AlertDialog.Builder builder = new AlertDialog.Builder(parent);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton(android.R.string.ok, onConfirm);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}
}