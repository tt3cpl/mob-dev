package data.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

// проверяет наличие активного интернет-соединения

class NetworkMonitor(context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager // системный сервис Android для мониторинга сети

    fun isCurrentlyOnline(): Boolean { // левая часть ?: return false если чтото null то сразу false фигачим
        val network = connectivityManager.activeNetwork ?: return false // проверяет есть ли сеть вообще
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false // проверяет возможность сети, то есть активна ли сеть
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) // проверяет имеет ли сеть выход в интернет
    }
}
