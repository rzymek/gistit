package org.gistit.activity;

import org.gistit.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public class SelectAccountActivity extends ListActivity implements OnItemClickListener {

	private ArrayAdapter<Account> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_gist);
		if (savedInstanceState == null) {
			adapter = new ArrayAdapter<Account>(this, R.layout.gist_list_item);
			getListView().setOnItemClickListener(this);
			setListAdapter(adapter);
			
			AccountManager service = (AccountManager) getSystemService(Activity.ACCOUNT_SERVICE);
			final String ACCOUNT_TYPE = "com.github";
			Account[] accounts = service.getAccountsByType(ACCOUNT_TYPE);
			adapter.clear();
			for (Account account : accounts) {
				adapter.add(account);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Account account = adapter.getItem(position);
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = shared.edit();
		edit.putString("account.name", account.name);
		edit.commit();
		finish();
	}
}
 