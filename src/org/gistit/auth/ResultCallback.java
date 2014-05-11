package org.gistit.auth;

import org.gistit.auth.step.UIAction;

public interface ResultCallback {

	void failed();

	void passed();

	void uiAction(UIAction action);

}
