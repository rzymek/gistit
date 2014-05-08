package org.gistit;

import android.content.Intent;

public interface AuthRequestResult {
	public void allowed(String token);
	public void denied(Intent intent);
}