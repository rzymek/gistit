package org.gistit;

import android.content.Intent;

public abstract class AuthRequestResult {
	public abstract void allowed(String token); 
	public void denied(Intent intent){}
}