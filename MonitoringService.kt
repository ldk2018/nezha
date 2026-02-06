package com.nezha.simple

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MonitoringService : Service() {
    
    private var monitoringJob: Job? = null
    private lateinit var configManager: ConfigManager
    private lateinit var apiClient: ApiClient
    private lateinit var networkMonitor: NetworkSpeedMonitor
    
    override fun onCreate() {
        super.onCreate()
        configManager = ConfigManager(this)
        apiClient = ApiClient()
        networkMonitor = NetworkSpeedMonitor()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!configManager.hasConfig()) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        startForeground(1, createNotification())
        startMonitoring()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "nezha_channel",
                "哪吒监控",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, "nezha_channel")
            .setContentTitle("哪吒监控")
            .setContentText("设备监控运行中...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun startMonitoring() {
        monitoringJob?.cancel()
        
        monitoringJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val config = configManager.loadConfig()
                    
                    // 收集设备信息
                    val (memoryUsed, memoryTotal) = SystemInfoUtils.getMemoryInfo(this@MonitoringService)
                    val (batteryLevel, batteryTemp) = SystemInfoUtils.getBatteryInfo(this@MonitoringService)
                    val (storageTotal, storageAvailable) = SystemInfoUtils.getStorageInfo()
                    
                    val networkSpeed = networkMonitor.getNetworkSpeed()
                    
                    val deviceInfo = DeviceInfo(
                        agentId = config.agentId,
                        timestamp = System.currentTimeMillis(),
                        cpu = SystemInfoUtils.getCpuUsage(),
                        memoryUsed = memoryUsed.toDouble(),
                        memoryTotal = memoryTotal.toDouble(),
                        networkRxSpeed = networkSpeed.first,
                        networkTxSpeed = networkSpeed.second,
                        batteryLevel = batteryLevel,
                        batteryTemperature = batteryTemp,
                        uptime = SystemInfoUtils.getUptime(),
                        androidVersion = Build.VERSION.RELEASE,
                        deviceModel = Build.MODEL,
                        storageTotal = storageTotal,
                        storageAvailable = storageAvailable,
                        isCharging = false // 简化版本
                    )
                    
                    // 上传数据
                    apiClient.uploadDeviceInfo(
                        config.serverUrl,
                        config.secretKey,
                        config.agentId,
                        deviceInfo
                    )
                    
                    delay(configManager.getReportIntervalMillis())
                    
                } catch (e: Exception) {
                    delay(30000) // 出错后等待30秒
                }
            }
        }
    }
    
    override fun onDestroy() {
        monitoringJob?.cancel()
        super.onDestroy()
    }
}