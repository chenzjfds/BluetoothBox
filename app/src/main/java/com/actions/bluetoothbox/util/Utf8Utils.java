package com.actions.bluetoothbox.util;

/**
 * Created by chenxiangjie on 2016/11/22.
 */

public class Utf8Utils {

    private final static int UTF8_BOM_LENGTH = 3;

    public static String removeBomHead(String originalString) {

        byte[] bytes = originalString.getBytes();

        if (isUtf8WithBom(bytes)) {
            byte[] resultArrays = new byte[bytes.length - UTF8_BOM_LENGTH];
            System.arraycopy(bytes, UTF8_BOM_LENGTH, resultArrays, 0, resultArrays.length);
            return new String(resultArrays);
        }

        return originalString;
    }

    private static boolean isUtf8WithBom(byte[] bytes) {
        if (bytes.length > 3 && (bytes[0] & 0xFF) == 0xEF &&
                (bytes[1] & 0xFF) == 0xBB &&
                (bytes[2] & 0xFF) == 0xBF) {
            return true;
        }
        return false;
    }
}
