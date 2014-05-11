package org.gistit.activity;

import org.gistit.App;
import org.gistit.R;
import org.gistit.auth.ResultCallback;
import org.gistit.auth.SetupItem;
import org.gistit.auth.SetupRunner;
import org.gistit.auth.UIAction;

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

public class SetupChecklistActivity extends ListActivity implements ResultCallback {
	public final static String ACCOUNT_TYPE = "com.github";
	public final static int ACCOUNT_SELECTED = 1;

	private ArrayAdapter<SetupItem> adapter;
	private SetupRunner runner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		runner = app().setupRunner;
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
		for (SetupItem item : runner.items) {
			adapter.add(item);
		}
		setListAdapter(adapter);
		setTitle("GistIt setup");

	}

	private App app() {
		return (App) getApplication();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode != RESULT_OK)
			return;
		if(requestCode == ACCOUNT_SELECTED) {
			Account[] accounts = AccountManager.get(this).getAccounts();
			runner.selectedAccount = findByName(accounts, data.getStringExtra(App.ACCOUNT_NAME));
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		runner.run(this);
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
		runner.run(this);
	}

	@Override
	public void failed() {
		adapter.notifyDataSetChanged();
	}

	@Override
	public void passed() {		
		adapter.notifyDataSetChanged();
		finish();
		startActivity(new Intent(this, MainActivity.class));
	}

	@Override
	public void uiAction(UIAction action) {
		action.run(this);
	}

}
