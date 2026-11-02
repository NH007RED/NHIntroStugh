package com.nh.introstudio

import android.app.Application
import com.nh.introstudio.data.MediaImporter
import com.nh.introstudio.data.Repository
import com.nh.introstudio.data.SettingsStore
import com.nh.introstudio.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class NHApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val database by lazy { AppDatabase.get(this) }
    val repository by lazy { Repository(database) }
    val settingsStore by lazy { SettingsStore(this) }
    val importer by lazy { MediaImporter(this) }
}
