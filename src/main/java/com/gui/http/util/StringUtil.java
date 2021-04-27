package com.gui.http.util;

import java.math.BigInteger;

public class StringUtil {

    public static String LINE_SEPARATOR = System.lineSeparator();

    public static String toHex(String str) {
        return String.format("%040x", new BigInteger(1, str.getBytes()));
    }

}
