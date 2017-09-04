package com.actions.bluetoothbox.util;

import com.actions.bluetoothbox.BuildConfig;
import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by chenxiangjie on 2016/11/21.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class PinyinComparatorTest {

    private PinyinComparator pinyinComparator;
    private ArrayList<String> strings;

    @Before
    public void setUp() throws Exception {

        pinyinComparator = new PinyinComparator();
        strings = new ArrayList<>();
        strings.add("1");
        strings.add("2");
        strings.add("~~~1");
        strings.add("01 words");
        strings.add("01-WORDS");
        strings.add("1 words");
        strings.add("1_WORDS");
        strings.add("1中文");
        strings.add("02");
        strings.add("英文");
    }

    @Test
    public void testCharacterList() throws Exception {
        Collections.sort(strings, pinyinComparator);
        Truth.assertThat(strings).containsExactly("01 words", "01-WORDS", "02", "1", "1 words", "1_WORDS", "1中文", "2", "~~~1", "英文").inOrder();
    }

    @Test
    public void testCharacterChinese() throws Exception {

        strings.clear();
        strings.add("么得");
        strings.add("玛德");
        strings.add("摸得");
        strings.add("摸多");
        strings.add("母的");
        strings.add("母多");

        Collections.sort(strings, pinyinComparator);
        Truth.assertThat(strings).containsExactly("玛德", "么得", "摸得", "摸多", "母的", "母多").inOrder();
    }

}