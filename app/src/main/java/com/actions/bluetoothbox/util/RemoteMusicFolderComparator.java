package com.actions.bluetoothbox.util;

import com.actions.ibluz.manager.BluzManagerData.RemoteMusicFolder;

import java.util.Comparator;

public class RemoteMusicFolderComparator implements Comparator<RemoteMusicFolder> {
 
    public int compare(RemoteMusicFolder folder1, RemoteMusicFolder folder2) {
        return new PinyinComparator().compare(folder1.name, folder2.name);
    } 
 
} 