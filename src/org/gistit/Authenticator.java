package org.gistit;

import java.io.IOException;

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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class Authenticator {
	private final static String GITHUB_APP = "com.github.mobile";
	final String ACCOUNT_TYPE = "com.github";
	private MainActivity main;
	private Account selectedAccount;

	public Authenticator(MainActivity activity) {
		this.main = activity;
	}

	public boolean isLoggedIn() {
		return getApp().token != null;
	}

	public Runnable selectAccount(String accountName) {
		AccountManager manager = getAccountManager();
		Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
		if (accounts.length == 0) {
			return new Runnable() {
				@Override
				public void run() {
					Intent github = main.getPackageManager().getLaunchIntentForPackage(GITHUB_APP);
					if (github != null) {
						main.startActivity(github);
					} else {
						showInstallGithubDialog();
					}
					main.onResumeAction = new Runnable() {
						@Override
						public void run() {
							main.onResumeAction = selectAccount(null);
						}
					};
				}
			};
		} else if (accounts.length == 1) {
			onAccountSelected(accounts[0]);
			return null;
		} else {
			Account account = getRememberedAccount(accounts, accountName);
			if (account != null) {
				onAccountSelected(account);
				return null;
			} else {
				return new Runnable() {
					@Override
					public void run() {
						main.startActivityForResult(new Intent(main, SelectAccountActivity.class), MainActivity.ACCOUNT_SELECTED);
					}
				};
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void onAccountSelected(Account account) {
		this.selectedAccount = account;
		boolean notifyAuthFailure = true;
		getAccountManager().getAuthToken(account, ACCOUNT_TYPE, notifyAuthFailure, new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				try {
					Bundle result = future.getResult();
					if (result != null) {
						getApp().token = result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN);
						if (getApp().token != null) {
							main.onLoggedIn();
						} else {
							showWaitingForPermission();
							Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
							if (intent != null) {
								main.startActivityForResult(intent, MainActivity.ACCESS_REQUEST);
							} else {
								main.finish();
							}
						}
					}
				} catch (OperationCanceledException | AuthenticatorException | IOException e) {
					Log.wtf("AUTH", "" + e);
				}
			}
		}, null);
	}

	@SuppressWarnings("deprecation")
	public void checkForAuthToken() {
		boolean dontNotifyAuthFailure = false;
		getAccountManager().getAuthToken(selectedAccount, ACCOUNT_TYPE, dontNotifyAuthFailure, new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				try {
					Bundle result = future.getResult();
					if (result != null) {
						getApp().token = result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN);
						if (getApp().token != null) {
							main.onLoggedIn();
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

	private void showInstallGithubDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(main);
		builder.setTitle("GitHub");
		builder.setMessage("Install GitHub application and setup at least one account.");
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					Uri uri = Uri.parse("market://details?id=" + GITHUB_APP);
					main.startActivity(new Intent(Intent.ACTION_VIEW, uri));
				} catch (android.content.ActivityNotFoundException anfe) {
					Uri uri = Uri.parse("http://play.google.com/store/apps/details?id=" + GITHUB_APP);
					main.startActivity(new Intent(Intent.ACTION_VIEW, uri));
				}
				main.onResumeAction = new Runnable() {
					@Override
					public void run() {
						Authenticator.this.selectAccount(null);
					}
				};
			}
		});
		builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				main.finish();
			}
		});
		builder.create().show();
	}

	private void showWaitingForPermission() {
		AlertDialog.Builder builder = new AlertDialog.Builder(main);
		builder.setTitle("GistIt needs permision");
		builder.setMessage("GistIt can't connect to GitHub until you grant permission. Retry?");
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				onAccountSelected(selectedAccount);// retry
			}

		});
		builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				main.finish();
			}
		});
		builder.create().show();
	}

	private AccountManager getAccountManager() {
		return (AccountManager) main.getSystemService(Activity.ACCOUNT_SERVICE);
	}

	private App getApp() {
		return (App) main.getApplication();
	}

}
