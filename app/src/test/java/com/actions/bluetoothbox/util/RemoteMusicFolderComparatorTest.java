package com.actions.bluetoothbox.util;

import com.actions.ibluz.manager.BluzManagerData;
import com.google.common.truth.Truth;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by chenxiangjie on 2016/11/22.
 */
public class RemoteMusicFolderComparatorTest {

    private RemoteMusicFolderComparator remoteMusicFolderComparator;
    private ArrayList<BluzManagerData.RemoteMusicFolder> folders;

    @Before
    public void setUp() throws Exception {

        remoteMusicFolderComparator = new RemoteMusicFolderComparator();
        folders = new ArrayList<>();
        folders.add(generateRemoteMusicFolder("1"));
        folders.add(generateRemoteMusicFolder("2"));
        folders.add(generateRemoteMusicFolder("玛德"));
        folders.add(generateRemoteMusicFolder("德玛"));
        folders.add(generateRemoteMusicFolder("MD"));
        folders.add(generateRemoteMusicFolder("DM"));
        folders.add(generateRemoteMusicFolder("~~~1"));
        folders.add(generateRemoteMusicFolder("01 words"));
        folders.add(generateRemoteMusicFolder("01-WORDS"));
        folders.add(generateRemoteMusicFolder("1 words"));
        folders.add(generateRemoteMusicFolder("1_WORDS"));
        folders.add(generateRemoteMusicFolder("1中文"));
        folders.add(generateRemoteMusicFolder("02"));
    }

    @Test
    public void testCharacterList() throws Exception {
        Collections.sort(folders, remoteMusicFolderComparator);
        Truth.assertThat(folders).containsExactly(
                generateRemoteMusicFolder("01 words"),
                generateRemoteMusicFolder("01-WORDS"),
                generateRemoteMusicFolder("02"),
                generateRemoteMusicFolder("1"),
                generateRemoteMusicFolder("1 words"),
                generateRemoteMusicFolder("1_WORDS"),
                generateRemoteMusicFolder("1中文"),
                generateRemoteMusicFolder("2"),
                generateRemoteMusicFolder("DM"),
                generateRemoteMusicFolder("MD"),
                generateRemoteMusicFolder("~~~1"),
                generateRemoteMusicFolder("德玛"),
                generateRemoteMusicFolder("玛德")
        ).inOrder();
    }

    private BluzManagerData.RemoteMusicFolder generateRemoteMusicFolder(String folderName) {
        BluzManagerData.RemoteMusicFolder folder = new BluzManagerData.RemoteMusicFolder();
        folder.name = folderName;
        return folder;
    }
}