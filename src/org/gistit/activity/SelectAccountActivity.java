package org.gistit.activity;

import org.gistit.activity.base.ListSelectionActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectAccountActivity extends ListSelectionActivity<Account> implements OnItemClickListener {


	@Override
	protected void init() {
		AccountManager service = (AccountManager) getSystemService(Activity.ACCOUNT_SERVICE);
		final String ACCOUNT_TYPE = "com.github";
		Account[] accounts = service.getAccountsByType(ACCOUNT_TYPE);
		adapter.clear();
		for (Account account : accounts) {
			adapter.add(account);
		}
		progress.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Account account = adapter.getItem(position);
		setResult(Pair.create("account.name", account.name));
	}
}
 