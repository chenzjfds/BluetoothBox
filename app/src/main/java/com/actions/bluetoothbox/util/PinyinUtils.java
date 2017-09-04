package com.actions.bluetoothbox.util;

import net.sourceforge.pinyin4j.PinyinHelper;

/**
 * Created by chenxiangjie on 2016/11/22.
 */

public class PinyinUtils {


    /**
     * 字符的拼音，多音字就得到第一个拼音。不是汉字，就return null。
     */
    public static String toPinyin(char c) {
        String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c);
        if (pinyins == null) {
            return null;
        }
        return pinyins[0];
    }
}
