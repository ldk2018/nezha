package com.nezha.simple

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class ConfigManager(private val context: Context) {
    
    data class NezhaConfig(
        var serverUrl: String = "",
        var agentId: String = "",
        var secretKey: String = "",
        var reportInterval: Int = 60,
        var deviceName: String = "",
        var lastConnected: Long = 0
    )
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("nezha_config", Context.MODE_PRIVATE)
    }
    
    private val gson = Gson()
    
    fun saveConfig(config: NezhaConfig): Boolean {
        return try {
            val json = gson.toJson(config)
            prefs.edit().putString("config", json).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun loadConfig(): NezhaConfig {
        val json = prefs.getString("config", null)
        return if (json != null) {
            gson.fromJson(json, NezhaConfig::class.java)
        } else {
            NezhaConfig()
        }
    }
    
    fun hasConfig(): Boolean {
        val config = loadConfig()
        return config.serverUrl.isNotEmpty() && 
               config.agentId.isNotEmpty() && 
               config.secretKey.isNotEmpty()
    }
    
    fun getReportIntervalMillis(): Long {
        val config = loadConfig()
        return (config.reportInterval * 1000L).coerceAtLeast(30000L)
    }
}