package org.gistit.activity;

import org.gistit.App;
import org.gistit.R;
import org.gistit.auth.Authorized;
import org.gistit.auth.GistPicked;
import org.gistit.auth.GitHubAccountSelected;
import org.gistit.auth.base.SetupItem;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SetupChecklistActivity extends ListActivity {
	public final static String ACCOUNT_TYPE = "com.github";
	public final static int ACCOUNT_SELECTED = 1;

	public Account selectedAccount;

	private SetupItem[] items = {
			new GitHubAccountSelected(this, "GitHub account selected"),
			new Authorized(this, "Authorized"),
			new GistPicked(this, "Gist picked")
	};
	private ArrayAdapter<SetupItem> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new ArrayAdapter<SetupItem>(this, R.layout.setup_checklist_item, R.id.setup_item_text) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view.findViewById(R.id.setup_item_text);
				SetupItem item = getItem(position);
				textView.setCompoundDrawablesWithIntrinsicBounds(item.done ? R.drawable.ic_ok : R.drawable.ic_fail, 0, 0, 0);
				return view;
			}
		};
		for (SetupItem item : items) {
			adapter.add(item);
		}
		setListAdapter(adapter);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode != RESULT_OK)
			return;
		if(requestCode == ACCOUNT_SELECTED) {
			Account[] accounts = AccountManager.get(this).getAccounts();
			selectedAccount = findByName(accounts, data.getStringExtra(App.ACCOUNT_NAME));
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
		for (SetupItem item : items) {
			if (!item.execute())
				return;
		}
		finish();
		startActivity(new Intent(this, MainActivity.class));
	}

	public static Account findByName(Account[] accounts, String accountName) {
		if(accountName == null)
			return null;
		for (Account account : accounts) {
			if (accountName.equals(account.name)) {
				return account;
			}
		}
		return null;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		SetupItem item = adapter.getItem(position);
		item.reset();
		onResume();
	}

}
