package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.devices.DeviceAdapter
import com.example.myapplication.ui.chat.ChatActivity
import com.example.myapplication.network.DeviceInfo
import com.example.myapplication.network.NetworkService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var networkService: NetworkService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        networkService = NetworkService(this)
        setupDeviceList()
    }

    private fun setupDeviceList() {
        deviceAdapter = DeviceAdapter { device: DeviceInfo ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra(ChatActivity.EXTRA_DEVICE_ADDRESS, device.address)
            }
            startActivity(intent)
        }
        
        binding.deviceList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = deviceAdapter
        }
    }
}