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
import android.os.Bundle;
import android.util.Log;

public class Authenticator {
	private Activity activity;

	public Authenticator(Activity activity) {
		this.activity = activity;
	}
	@SuppressWarnings("deprecation")
	public void fetchGithubAuthTokenUI(final AuthRequestResult callback) {
		final App app = (App) activity.getApplication();
		if (app.token != null)
			callback.allowed(app.token);
		AccountManager service = (AccountManager) activity.getSystemService(Activity.ACCOUNT_SERVICE);
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
					Log.wtf("AUTH",""+e);
				}
			}
		}, null);
	}

	@SuppressWarnings("deprecation")
	public void fetchGithubAuthToken(final Runnable onTokenReady) {
		final App app = (App) activity.getApplication();
		if (app.token != null)
			onTokenReady.run();
		AccountManager service = (AccountManager) activity.getSystemService(Activity.ACCOUNT_SERVICE);
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
						onTokenReady.run();
					}
				} catch (OperationCanceledException | AuthenticatorException | IOException e) {
					e.printStackTrace();
				}
			}
		}, null);
	}

	private void showInstallGithubDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("No GitHub account configured");
		builder.setMessage("Install GitHub Mobile application");
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// go to play store
			}

		});
		builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.finish();
			}
		});
		builder.create().show();
	}

}
