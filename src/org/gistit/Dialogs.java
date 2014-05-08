package org.gistit;

import org.gistit.activity.MainActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

public class Dialogs {
	private MainActivity main;
	private AlertDialog installGithubDialog;
	private AlertDialog waitingForPermission;

	public Dialogs(MainActivity main) {
		this.main = main;
	}

	private AlertDialog createInstallGithubDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(main);
		builder.setTitle("GitHub");
		builder.setMessage("Install GitHub application and setup at least one account.");
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			private final static String GITHUB_APP = "com.github.mobile";

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
						main.authenticator.selectAccount(null);
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
		return builder.create();
	}

	private AlertDialog createWaitingForPermission() {
		AlertDialog.Builder builder = new AlertDialog.Builder(main);
		builder.setTitle("GistIt needs permision");
		builder.setMessage("GistIt can't connect to GitHub until you grant permission. Retry?");
		builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				main.authenticator.onAccountSelected(main.authenticator.selectedAccount);// retry
			}

		});
		builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				main.finish();
			}
		});
		return builder.create();
	}

	public AlertDialog getInstallGithubDialog() {
		if (installGithubDialog == null) {
			installGithubDialog = createInstallGithubDialog();
		}
		return installGithubDialog;
	}

	public AlertDialog getWaitingForPermission() {
		if (waitingForPermission == null) {
			waitingForPermission = createWaitingForPermission();
		}
		return waitingForPermission;
	}
}
