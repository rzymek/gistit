package org.gistit.rest;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.util.Log;

public abstract class RESTCallback<T> implements Callback<T> {

	@Override
	public void failure(RetrofitError e) {
		Log.e("REST", "" + e);
		error(e.getCause());
		always();
	}

	@Override
	public void success(T result, Response response) {
		ok(result);
		always();
	}

	protected abstract void ok(T result);

	protected void error(Throwable throwable) {
	}

	protected void always() {
	}

}
