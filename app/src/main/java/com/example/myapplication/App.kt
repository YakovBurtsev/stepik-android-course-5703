package com.example.myapplication

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        val realmConfig = RealmConfiguration.Builder()
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .build()

        Realm.setDefaultConfiguration(realmConfig)

        //save in xml file
        getSharedPreferences("preferences", 0).edit().putString("x", "a").apply()

        //get saved data
        getSharedPreferences("preferences", 0).getString("x", "defaultValue")
    }
}