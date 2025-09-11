package com.djowda.djowdageminimap;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;


public class DjowdaApplication extends Application {

    private static DjowdaApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        instance = this;

        FirebaseApp.initializeApp(/*context=*/ this);

    }

    public static DjowdaApplication getInstance() {
        return instance;
    }


}
