/**
 * 这类是网上得来
 * 比较两个中文字符串，按拼音排序
 * Copyright (c) 复制或转载本文，请保留该注释。
 */
package com.actions.bluetoothbox.util;

import java.util.Comparator;

public class PinyinComparator implements Comparator<String> {

    public int compare(String o1, String o2) {
        o1 = Utf8Utils.removeBomHead(o1);
        o2 = Utf8Utils.removeBomHead(o2);
        try {
            for (int i = 0; i < o1.length() && i < o2.length(); i++) {

                int codePoint1 = o1.charAt(i);
                int codePoint2 = o2.charAt(i);

                if (Character.isSupplementaryCodePoint(codePoint1)
                        || Character.isSupplementaryCodePoint(codePoint2)) {
                    i++;
                }

                if (codePoint1 != codePoint2) {
                    if (Character.isSupplementaryCodePoint(codePoint1)
                            || Character.isSupplementaryCodePoint(codePoint2)) {
                        return codePoint1 - codePoint2;
                    }

                    String pinyin1 = PinyinUtils.toPinyin((char) codePoint1);
                    String pinyin2 = PinyinUtils.toPinyin((char) codePoint2);

                    if (pinyin1 != null && pinyin2 != null) { // 两个字符都是汉字
                        if (!pinyin1.equals(pinyin2)) {
                            return pinyin1.compareTo(pinyin2);
                        }
                    } else {
                        return codePoint1 - codePoint2;
                    }
                }
            }
            return o1.length() - o2.length();
        } catch (Exception e) {
            return 0;
        }
    }


}