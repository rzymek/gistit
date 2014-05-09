package org.gistit.auth;

import java.io.IOException;

import org.gistit.App;
import org.gistit.activity.MainActivity;
import org.gistit.activity.SelectAccountActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class Authenticator {
	final String ACCOUNT_TYPE = "com.github";
	private MainActivity main;
	Account selectedAccount;
	public Dialogs dialogs;

	public Authenticator(MainActivity activity) {
		this.main = activity;
		dialogs = new Dialogs(main);
	}

	public boolean isLoggedIn() {
		return getApp().token != null;
	}

	public void selectAccount(String accountName) {
		AccountManager manager = getAccountManager();
		Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
		if (accounts.length == 0) {
			AlertDialog dialog = dialogs.getInstallGithubDialog();
			ensureVisible(dialog);
			// main.onResumeAction = new Runnable() {
			// @Override
			// public void run() {
			// selectAccount(null);
			// }
			// };
		} else if (accounts.length == 1) {
			selectedAccount = accounts[0];
			grabAuthToken();
		} else {
			Account account = getRememberedAccount(accounts, accountName);
			if (account != null) {
				selectedAccount = accounts[0];
				grabAuthToken();
			} else {
				// main.onResumeAction = new Runnable() {
				// @Override
				// public void run() {
				main.startActivityForResult(new Intent(main, SelectAccountActivity.class),
						MainActivity.ACCOUNT_SELECTED);
				// }
				// };
			}
		}
	}

	protected void ensureVisible(AlertDialog dialog) {
		dialogs.dismissAll();
		dialog.show();
	}

	@SuppressWarnings("deprecation")
	public void askForPermission() {
		boolean notifyAuthFailure = true;
		getAccountManager().getAuthToken(selectedAccount, ACCOUNT_TYPE, notifyAuthFailure,
				new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						try {
							Bundle result = future.getResult();
							if (result != null) {
								getApp().token = result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN);
							}
						} catch (OperationCanceledException | AuthenticatorException | IOException e) {
							Log.wtf("AUTH", "" + e);
						}
					}
				}, null);
	}

	@SuppressWarnings("deprecation")
	public void grabAuthToken() {
		if (selectedAccount == null)
			return;
		boolean dontNotifyAuthFailure = false;
		getAccountManager().getAuthToken(selectedAccount, ACCOUNT_TYPE, dontNotifyAuthFailure,
				new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						try {
							Bundle result = future.getResult();
							if (result != null) {
								getApp().token = result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN);
							}
							if (getApp().token == null) {
								AlertDialog dialog = dialogs.getWaitingForPermission();
								ensureVisible(dialog);
								Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
								if (intent != null) {
									main.startActivityForResult(intent, MainActivity.ACCESS_REQUEST);
								} else {
									main.finish();
								}
							}
						} catch (OperationCanceledException | AuthenticatorException | IOException e) {
							Log.wtf("AUTH", "" + e);
						}
					}
				}, null);
	}

	protected Account getRememberedAccount(Account[] accounts, String accountName) {
		if (accountName == null) {
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(main);
			accountName = shared.getString("account.name", null);
		}
		if (accountName == null)
			return null;
		for (Account account : accounts) {
			if (accountName.equals(account.name)) {
				return account;
			}
		}
		return null;
	}

	private AccountManager getAccountManager() {
		return (AccountManager) main.getSystemService(Activity.ACCOUNT_SERVICE);
	}

	private App getApp() {
		return (App) main.getApplication();
	}

}
