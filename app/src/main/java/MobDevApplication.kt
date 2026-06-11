package app.mobdev

// точка входа всего приложение

import android.app.Application
import data.api.RetrofitClient

class MobDevApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
