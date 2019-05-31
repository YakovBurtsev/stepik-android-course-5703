package com.example.myapplication

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        //save in xml file
        getSharedPreferences("preferences", 0).edit().putString("x", "a").apply()

        //get saved data
        getSharedPreferences("preferences", 0).getString("x", "defaultValue")
    }
}