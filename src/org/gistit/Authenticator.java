package org.gistit;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class Authenticator {
	private Activity parent;

	public Authenticator(Activity activity) {
		this.parent = activity;
	}

	@SuppressWarnings("deprecation")
	public void fetchGithubAuthTokenUI(final AuthRequestResult callback) {
		final App app = (App) parent.getApplication();
		if (app.token != null)
			callback.allowed(app.token);
		AccountManager service = (AccountManager) parent.getSystemService(Activity.ACCOUNT_SERVICE);
		final String ACCOUNT_TYPE = "com.github";
		Account[] accounts = service.getAccountsByType(ACCOUNT_TYPE);
		if (accounts.length == 0) {
			showInstallGithubDialog();
			return;
		}
		// if(accounts.length > 1)
		// showAccountSelector
		Account account = accounts[0];

		service.getAuthToken(account, ACCOUNT_TYPE, true, new AccountManagerCallback<Bundle>() {

			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				try {
					Bundle result = future.getResult();
					if (result != null) {
						app.token = result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN);
						if (app.token != null) {
							callback.allowed(app.token);
						} else {
							Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
							callback.denied(intent);
						}
					}
				} catch (OperationCanceledException | AuthenticatorException | IOException e) {
					Log.wtf("AUTH", "" + e);
				}
			}
		}, null);
	}

	private void showInstallGithubDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(parent);
		builder.setTitle("No GitHub account configured");
		builder.setMessage("Install GitHub Mobile application");
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String appPackageName = "com.github.mobile";
				try {
					Uri uri = Uri.parse("market://details?id=" + appPackageName);
					parent.startActivity(new Intent(Intent.ACTION_VIEW, uri));
				} catch (android.content.ActivityNotFoundException anfe) {
					Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName);
					parent.startActivity(new Intent(Intent.ACTION_VIEW, uri));
				}

			}

		});
		builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				parent.finish();
			}
		});
		builder.create().show();
	}
}
