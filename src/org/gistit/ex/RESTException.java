package org.gistit.ex;

import retrofit.client.Response;

public class RESTException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public final int status;

	public RESTException(Response response) {
		super(response.getReason());
		status = response.getStatus();
	}
}
