package dev.k410.a25ia

import android.app.Application

class A25IaApplication : Application() {
    val container: AppContainer by lazy {
        AppContainer(applicationContext)
    }
}
