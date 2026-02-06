package com.nezha.simple

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ApiClient {
    
    private val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    
    suspend fun uploadDeviceInfo(
        serverUrl: String,
        secretKey: String,
        agentId: String,
        deviceInfo: DeviceInfo
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(deviceInfo)
            
            val url = "$serverUrl/api/v1/agent/report"
            val requestBody = json.toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $secretKey")
                .header("X-Agent-ID", agentId)
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
            
        } catch (e: Exception) {
            false
        }
    }
}