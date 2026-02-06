package com.nezha.simple

import com.google.gson.annotations.SerializedName

data class DeviceInfo(
    @SerializedName("agent_id") val agentId: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("cpu") val cpu: Double,
    @SerializedName("memory_used") val memoryUsed: Double,
    @SerializedName("memory_total") val memoryTotal: Double,
    @SerializedName("network_rx_speed") val networkRxSpeed: Double,
    @SerializedName("network_tx_speed") val networkTxSpeed: Double,
    @SerializedName("battery_level") val batteryLevel: Int,
    @SerializedName("battery_temperature") val batteryTemperature: Float,
    @SerializedName("uptime") val uptime: Long,
    @SerializedName("android_version") val androidVersion: String,
    @SerializedName("device_model") val deviceModel: String,
    @SerializedName("storage_total") val storageTotal: Long,
    @SerializedName("storage_available") val storageAvailable: Long,
    @SerializedName("is_charging") val isCharging: Boolean
)