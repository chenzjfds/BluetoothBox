package com.actions.bluetoothbox.ui.base;

import android.content.Intent;

public abstract class Presenter <T extends Vista> {

    public Intent intent;
    public   T vista;

    public  abstract void onStart();

    public abstract void onStop();

   public void setVista(T vista){
        this.vista =vista;
    }


    public void attachIncomingIntent(Intent intent){
        this.intent =intent;
    }
}
