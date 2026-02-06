package com.nezha.simple

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    private lateinit var etServerUrl: EditText
    private lateinit var etAgentId: EditText
    private lateinit var etSecretKey: EditText
    private lateinit var btnSave: Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var tvStatus: TextView
    
    private val configManager by lazy { ConfigManager(this) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        loadConfig()
        setupListeners()
    }
    
    private fun initViews() {
        etServerUrl = findViewById(R.id.et_server_url)
        etAgentId = findViewById(R.id.et_agent_id)
        etSecretKey = findViewById(R.id.et_secret_key)
        btnSave = findViewById(R.id.btn_save)
        btnStart = findViewById(R.id.btn_start)
        btnStop = findViewById(R.id.btn_stop)
        tvStatus = findViewById(R.id.tv_status)
    }
    
    private fun loadConfig() {
        val config = configManager.loadConfig()
        etServerUrl.setText(config.serverUrl)
        etAgentId.setText(config.agentId)
        etSecretKey.setText(config.secretKey)
    }
    
    private fun setupListeners() {
        btnSave.setOnClickListener { saveConfig() }
        btnStart.setOnClickListener { startService() }
        btnStop.setOnClickListener { stopService() }
        
        // 长按显示/隐藏密钥
        etSecretKey.setOnLongClickListener {
            if (etSecretKey.inputType == 129) {
                etSecretKey.inputType = 1
            } else {
                etSecretKey.inputType = 129
            }
            true
        }
    }
    
    private fun saveConfig() {
        val serverUrl = etServerUrl.text.toString().trim()
        val agentId = etAgentId.text.toString().trim()
        val secretKey = etSecretKey.text.toString().trim()
        
        if (serverUrl.isEmpty() || agentId.isEmpty() || secretKey.isEmpty()) {
            Toast.makeText(this, "请填写所有配置项", Toast.LENGTH_SHORT).show()
            return
        }
        
        val config = ConfigManager.NezhaConfig(
            serverUrl = serverUrl,
            agentId = agentId,
            secretKey = secretKey,
            reportInterval = 60
        )
        
        if (configManager.saveConfig(config)) {
            Toast.makeText(this, "配置保存成功", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startService() {
        if (!configManager.hasConfig()) {
            Toast.makeText(this, "请先保存配置", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = Intent(this, MonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
        
        tvStatus.text = "服务状态: 运行中"
        Toast.makeText(this, "监控服务已启动", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopService() {
        val intent = Intent(this, MonitoringService::class.java)
        stopService(intent)
        
        tvStatus.text = "服务状态: 已停止"
        Toast.makeText(this, "监控服务已停止", Toast.LENGTH_SHORT).show()
    }
}