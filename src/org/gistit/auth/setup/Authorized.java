package org.gistit.auth.setup;

import java.io.IOException;

import org.gistit.activity.MainActivity;
import org.gistit.activity.SetupChecklistActivity;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Authorized extends SetupItem {
	private boolean showRequestAuthDialog = true;

	public Authorized(SetupChecklistActivity setupActivity, String label) {
		super(setupActivity, label);
	}

	public void reset() {
		showRequestAuthDialog = true;
	}

	@SuppressWarnings("deprecation")
	protected boolean check() {
		if (self.selectedAccount == null)
			return false;
		if (app().token != null)
			return true;
		getAccountManager().getAuthToken(self.selectedAccount, SetupChecklistActivity.ACCOUNT_TYPE, false, new AccountManagerCallback<Bundle>() {
			@Override
			public void run(AccountManagerFuture<Bundle> future) {
				try {
					Bundle result = future.getResult();
					if (result != null) {
						app().token = result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN);
						if (app().token != null) {
							self.onResume();
						} else if (showRequestAuthDialog) {
							Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
							if (intent != null) {
								self.startActivityForResult(intent, MainActivity.ACCESS_REQUEST);
							}
						}
					}
					showRequestAuthDialog = false;
				} catch (OperationCanceledException | AuthenticatorException | IOException e) {
					Log.wtf("AUTH", "" + e);
				}
			}
		}, null);
		return false;
	}
}