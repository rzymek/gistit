package org.gistit.util;

import android.util.Pair;

public class SPair extends Pair<String, String> {

	public SPair(String first, String second) {
		super(first, second);
	}

	public static SPair mk(Object first, Object second) {
		return new SPair(valueOf(first), valueOf(second));
	}

	private static String valueOf(Object o) {
		return o == null ? null : o.toString();
	}
}
