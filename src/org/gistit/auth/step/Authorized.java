package org.gistit.auth.step;

import java.io.IOException;

import org.gistit.activity.MainActivity;
import org.gistit.activity.SetupChecklistActivity;
import org.gistit.auth.ResultCallback;
import org.gistit.auth.SetupItem;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Authorized extends SetupItem {
	private boolean showRequestAuthDialog = true;

	public Authorized(String label) {
		super(label);
	}

	public void reset() {
		showRequestAuthDialog = true;
	}

	@SuppressWarnings("deprecation")
	public void check(final ResultCallback resultCall) {
		if (context.selectedAccount == null) {
			resultCall.failed();
		} else if (context.app.token != null) {
			resultCall.passed();
		} else {
			AccountManager.get(context.app).getAuthToken(context.selectedAccount, SetupChecklistActivity.ACCOUNT_TYPE,
					false, new AccountManagerCallback<Bundle>() {
						@Override
						public void run(AccountManagerFuture<Bundle> future) {
							try {
								Bundle result = future.getResult();
								if (result != null) {
									context.app.token = result.getString(android.accounts.AccountManager.KEY_AUTHTOKEN);
									if (context.app.token != null) {
										resultCall.passed();
									} else if (showRequestAuthDialog) {
										Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
										if (intent != null) {
											resultCall.uiAction(createAccessRequestAction(intent));
										}
									}
								}
								showRequestAuthDialog = false;
							} catch (OperationCanceledException | AuthenticatorException | IOException e) {
								Log.wtf("AUTH", "" + e);
							}
						}
					}, null);
			resultCall.failed();
		}
	}

	protected UIAction createAccessRequestAction(final Intent intent) {
		return new UIAction() {

			@Override
			public void run(Activity parent) {
				parent.startActivityForResult(intent, MainActivity.ACCESS_REQUEST);
			}
		};
	}
}