package com.gui.http.util;

import java.math.BigInteger;

public class StringUtil {

    public static String toHex(String str) {
        return String.format("%040x", new BigInteger(1, str.getBytes()));
    }
}
