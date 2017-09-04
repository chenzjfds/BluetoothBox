package com.actions.bluetoothbox.ui.remotemusic;


import com.actions.bluetoothbox.ui.base.Presenter;
import com.actions.ibluz.manager.BluzManagerData.RemoteMusicFolder;

/**
 * Created by chenxiangjie on 2015/9/28.
 */
public abstract class RemoteMusicPresenter extends Presenter<RemoteMusicVista> {


    abstract void selectMusic(int musicIndex);

    abstract void selectFolder(RemoteMusicFolder folder);

    abstract void toggleFolderMode();

    abstract void switchLoop();

    abstract void playPause();

    abstract void previous();

    abstract void next();

    //
    abstract void getPListOrRemoteMusicFolders();

    abstract void getCurrentEntry();

    abstract void getLyric();

    abstract void cancelLyric();

    abstract void refreshLyricProgress(long delay);

    abstract void startUpdateMusicProgress();

    abstract void stopUpdateMusicProgress();
}
