package com.actions.bluetoothbox.util;

import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by chenxiangjie on 2016/11/23.
 */
public class Utf8UtilsTest {

    private byte[] utf16LE;
    private byte[] utf16LEWithBom;

    @Before
    public void setUp() throws Exception {
        utf16LE = new byte[]{ 49, 0, 49, 0, 0, 0};
        utf16LEWithBom = new byte[]{-1,-2, 49, 0, 49, 0};
    }



    @Test
    public void testBomGenerate() throws Exception {


        String withBom = new String(utf16LEWithBom, 0, 6, "UTF-16LE");

        byte[] bytesWithBom = withBom.getBytes();
        Truth.assertThat(bytesWithBom).isEqualTo(new byte[]{-17, -69, -65,49, 49});


        String withoutBom = new String(utf16LE, 0, 4, "UTF-16LE");

        byte[] bytesWithoutBom = withoutBom.getBytes();
        Truth.assertThat(bytesWithoutBom).isEqualTo(new byte[]{49, 49});

    }

    @Test
    public void testBomRemove() throws Exception {


        String withBom = new String(utf16LEWithBom, 0, 6, "UTF-16LE");

        byte[] bytesWithBom = withBom.getBytes();
        Truth.assertThat(bytesWithBom).isEqualTo(new byte[]{-17, -69, -65,49, 49});

        String result = Utf8Utils.removeBomHead(withBom);
        Truth.assertThat(result.getBytes()).isEqualTo(new byte[]{49, 49});

    }
}