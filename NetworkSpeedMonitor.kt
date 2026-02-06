package com.nezha.simple

import android.net.TrafficStats

class NetworkSpeedMonitor {
    
    private var lastRxBytes = 0L
    private var lastTxBytes = 0L
    private var lastTime = 0L
    
    init {
        reset()
    }
    
    private fun reset() {
        lastRxBytes = TrafficStats.getTotalRxBytes()
        lastTxBytes = TrafficStats.getTotalTxBytes()
        lastTime = System.currentTimeMillis()
    }
    
    fun getNetworkSpeed(): Pair<Double, Double> {
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()
        val currentTime = System.currentTimeMillis()
        
        val timeDiff = (currentTime - lastTime) / 1000.0
        
        val rxSpeed = if (timeDiff > 0) {
            (currentRxBytes - lastRxBytes) / 1024.0 / timeDiff
        } else {
            0.0
        }
        
        val txSpeed = if (timeDiff > 0) {
            (currentTxBytes - lastTxBytes) / 1024.0 / timeDiff
        } else {
            0.0
        }
        
        lastRxBytes = currentRxBytes
        lastTxBytes = currentTxBytes
        lastTime = currentTime
        
        return Pair(rxSpeed, txSpeed)
    }
}