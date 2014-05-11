package org.gistit.auth;


public interface ResultCallback {

	void failed();

	void passed();

	void uiAction(UIAction action);

}
