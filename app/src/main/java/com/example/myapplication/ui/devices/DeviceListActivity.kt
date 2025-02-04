package com.example.myapplication.ui.devices

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.DeviceListBinding
import com.example.myapplication.network.NetworkService
import com.example.myapplication.ui.chat.ChatActivity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DeviceListActivity : AppCompatActivity() {
    private lateinit var binding: DeviceListBinding
    private lateinit var networkService: NetworkService
    private lateinit var deviceAdapter: DeviceAdapter

    private val permissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.NEARBY_WIFI_DEVICES
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DeviceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkService = NetworkService(this)
        checkPermissions()
    }

    private fun checkPermissions() {
        if (permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            setupDeviceList()
            observeDevices()
        } else {
            requestPermissions(permissions, 1001)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            setupDeviceList()
            observeDevices()
        } else {
            finish()
        }
    }

    private fun setupDeviceList() {
        deviceAdapter = DeviceAdapter { device ->
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra(ChatActivity.EXTRA_DEVICE_ADDRESS, device.address)
            }
            startActivity(intent)
        }
        
        binding.deviceList.apply {
            layoutManager = LinearLayoutManager(this@DeviceListActivity)
            adapter = deviceAdapter
        }
    }

    private fun observeDevices() {
        lifecycleScope.launch {
            networkService.devices.collect { devices ->
                deviceAdapter.submitList(devices)
            }
        }
    }
}