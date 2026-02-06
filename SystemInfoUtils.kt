package com.nezha.simple

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.StatFs
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object SystemInfoUtils {
    
    fun getCpuUsage(): Double {
        return try {
            val reader = BufferedReader(FileReader("/proc/stat"))
            val line = reader.readLine()
            reader.close()
            
            if (line.startsWith("cpu ")) {
                val parts = line.split("\\s+".toRegex())
                val idle = parts[4].toDouble()
                val total = (1..8).sumOf { parts[it].toDouble() }
                
                if (total > 0) {
                    100.0 * (total - idle) / total
                } else {
                    0.0
                }
            } else {
                0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }
    
    fun getMemoryInfo(context: Context): Pair<Long, Long> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val totalMB = memoryInfo.totalMem / (1024 * 1024)
        val usedMB = totalMB - (memoryInfo.availMem / (1024 * 1024))
        
        return Pair(usedMB, totalMB)
    }
    
    fun getBatteryInfo(context: Context): Pair<Int, Float> {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val temp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        
        val batteryPercent = if (level >= 0 && scale > 0) {
            level * 100 / scale
        } else {
            -1
        }
        
        return Pair(batteryPercent, temp / 10.0f)
    }
    
    fun getUptime(): Long {
        return try {
            val reader = BufferedReader(FileReader("/proc/uptime"))
            val line = reader.readLine()
            reader.close()
            line.split(" ")[0].toDouble().toLong()
        } catch (e: Exception) {
            -1L
        }
    }
    
    fun getStorageInfo(): Pair<Long, Long> {
        val path = "/data"
        val stat = StatFs(path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        
        val totalMB = totalBlocks * blockSize / (1024 * 1024)
        val availableMB = availableBlocks * blockSize / (1024 * 1024)
        
        return Pair(totalMB, availableMB)
    }
}