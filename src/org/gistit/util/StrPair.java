package org.gistit.util;

import android.util.Pair;

public class StrPair extends Pair<String, String> {

	public StrPair(String first, String second) {
		super(first, second);
	}

	public static StrPair make(Object first, Object second) {
		return new StrPair(valueOf(first), valueOf(second));
	}

	private static String valueOf(Object o) {
		return o == null ? null : o.toString();
	}
}
