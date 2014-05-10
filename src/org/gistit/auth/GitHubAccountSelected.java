package org.gistit.auth;

import org.gistit.App;
import org.gistit.activity.SelectAccountActivity;
import org.gistit.activity.SetupChecklistActivity;
import org.gistit.auth.base.SetupItem;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class GitHubAccountSelected extends SetupItem {
	private final static String GITHUB_APP = "com.github.mobile";

	public GitHubAccountSelected(SetupChecklistActivity setupActivity, String label) {
		super(setupActivity, label);
	}

	public boolean check() {
		if (self.selectedAccount != null)
			return true;
		Account[] accounts = getAccountManager().getAccountsByType(SetupChecklistActivity.ACCOUNT_TYPE);
		if (accounts.length == 0) {
			final Intent github = self.getPackageManager().getLaunchIntentForPackage(GITHUB_APP);
			if (github == null) {
				ask("Install GitHub ", "GistIt uses GitHub accounts. Please install GitHub application to configue one.", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						try {
							Uri uri = Uri.parse("market://details?id=" + GITHUB_APP);
							self.startActivity(new Intent(Intent.ACTION_VIEW, uri));
						} catch (android.content.ActivityNotFoundException anfe) {
							Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=" + GITHUB_APP);
							self.startActivity(new Intent(Intent.ACTION_VIEW, uri));
						}
					}
				});
			}else{
				ask("Setup GitHub account", "Please sign in using GitHub application.", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						self.startActivity(github);
					}
				});
			}
			return false;
		} else if (accounts.length == 1) {
			self.selectedAccount = accounts[0];
			return true;
		} else {
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(self);
			String accountName = shared.getString(App.ACCOUNT_NAME, null);
			Account account = SetupChecklistActivity.findByName(accounts, accountName);
			if (account != null) {
				self.selectedAccount = account;
				return true;
			} else {
				self.startActivityForResult(new Intent(self, SelectAccountActivity.class), SetupChecklistActivity.ACCOUNT_SELECTED);
				return false;
			}
		}
	}

	private void ask(CharSequence title, CharSequence msg, DialogInterface.OnClickListener onConfirm) {
		AlertDialog.Builder builder = new AlertDialog.Builder(self);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setPositiveButton(android.R.string.ok, onConfirm);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.create().show();
	}
}