package com.flashmob.platonus

import android.app.Application
import com.flashmob.platonus.data.network.ApiClient

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}