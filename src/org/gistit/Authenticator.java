package org.gistit;

import java.io.IOException;

import org.gistit.activity.GistIt;
import org.gistit.activity.SelectAccountActivity;

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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
		Account account = getAccount(service, ACCOUNT_TYPE, false);
		if(account == null)
			return;
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
							showWaitingForPermission(callback);
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
	@SuppressWarnings("deprecation")
	public void fetchGithubAuthSilent(final AuthRequestResult callback) {
		final App app = (App) parent.getApplication();
		if (app.token != null)
			callback.allowed(app.token);
		AccountManager service = (AccountManager) parent.getSystemService(Activity.ACCOUNT_SERVICE);
		final String ACCOUNT_TYPE = "com.github";
		Account account = getAccount(service, ACCOUNT_TYPE, true);
		if(account == null)
			return;//abort, wait for account
		service.getAuthToken(account, ACCOUNT_TYPE, false, new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				try {
					Bundle result = future.getResult();
					if (result != null) {
						app.token = result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN);
						if (app.token != null) {
							callback.allowed(app.token);
						} else {
							callback.denied(null);
						}
					}
				} catch (OperationCanceledException | AuthenticatorException | IOException e) {
					Log.wtf("AUTH", "" + e);
				}
			}
		}, null);
	}

	protected Account getAccount(AccountManager service, final String ACCOUNT_TYPE, boolean silent) {
		Account[] accounts = service.getAccountsByType(ACCOUNT_TYPE);
		if (accounts.length == 0) {
			if(!silent)
				showInstallGithubDialog();
			return null;
		}else if(accounts.length == 1){
			return accounts[0];
		}else  {
			Account account = getRememberedAccount(accounts);
			if(account != null)
				return account;
			if(!silent)
				parent.startActivityForResult(new Intent(parent, SelectAccountActivity.class), GistIt.ACCOUNT_SELECTED);
			return null;			
		}
	}

	protected Account getRememberedAccount(Account[] accounts) {
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(parent);
		String accountName = shared.getString("account.name", null);
		if(accountName == null)
			return null;
		for (Account account : accounts) {
			if(accountName.equals(account.name)) {
				return account;
			}
		}
		return null;
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
	
	private void showWaitingForPermission(final AuthRequestResult callback) {
		AlertDialog.Builder builder = new AlertDialog.Builder(parent);
		builder.setTitle("GistIt needs permision");
		builder.setMessage("GistIt can't connect to GitHub until you grant permission. Retry?");
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				fetchGithubAuthTokenUI(callback);
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
