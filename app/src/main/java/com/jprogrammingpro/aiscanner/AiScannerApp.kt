package com.jprogrammingpro.aiscanner

import android.app.Application
import com.jprogrammingpro.aiscanner.data.AppDatabase
import com.jprogrammingpro.aiscanner.data.DocumentRepository
import com.jprogrammingpro.aiscanner.data.PageStorage
import com.jprogrammingpro.aiscanner.data.SettingsRepository
import com.jprogrammingpro.aiscanner.pro.Entitlements
import com.jprogrammingpro.aiscanner.pro.FreeEntitlements

/** Simple manual dependency container; keeps the app free of a DI framework for now. */
class AppContainer(app: Application) {
    private val database = AppDatabase.create(app)
    val pageStorage = PageStorage(app)
    val documents = DocumentRepository(database.documentDao(), pageStorage)
    val settings = SettingsRepository(app)
    val entitlements: Entitlements = FreeEntitlements
}

class AiScannerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
