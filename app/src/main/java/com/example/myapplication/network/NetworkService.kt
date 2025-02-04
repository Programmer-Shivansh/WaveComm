package com.example.myapplication.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class NetworkService(private val context: Context) {
    private val serverPort = 8888
    private val serviceName = "LanChat"
    private val serviceType = "_lanchat._tcp."
    private lateinit var nsdManager: NsdManager
    
    private val _devices = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val devices: StateFlow<List<DeviceInfo>> = _devices
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    init {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        startDiscovery()
        startServer()
    }

    private fun startDiscovery() {
        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) {
                _devices.value = emptyList() // Clear existing devices
            }
            
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                if (serviceInfo.serviceType != serviceType) return
                
                nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(service: NsdServiceInfo, error: Int) {
                        // Retry resolution after delay
                        Thread.sleep(1000)
                        nsdManager.resolveService(service, this)
                    }
                    
                    override fun onServiceResolved(service: NsdServiceInfo) {
                        val device = DeviceInfo(
                            name = service.serviceName,
                            address = service.host.hostAddress ?: return
                        )
                        if (!_devices.value.any { it.address == device.address }) {
                            _devices.value = _devices.value + device
                        }
                    }
                })
            }
            
            override fun onServiceLost(service: NsdServiceInfo) {
                _devices.value = _devices.value.filter { it.name != service.serviceName }
            }
            
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
        }
        
        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    private fun startServer() {
        Thread {
            val serverSocket = ServerSocket(serverPort)
            while (true) {
                val socket = serverSocket.accept()
                handleClientConnection(socket)
            }
        }.start()
    }

    fun sendMessage(message: String, deviceAddress: String) {
        Thread {
            try {
                val socket = Socket(deviceAddress, serverPort)
                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println(message)
                socket.close()
                
                val chatMessage = ChatMessage("Me", message)
                _messages.value = _messages.value + chatMessage
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun handleClientConnection(socket: Socket) {
        Thread {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val message = reader.readLine()
                val chatMessage = ChatMessage(socket.inetAddress.hostAddress, message)
                _messages.value = _messages.value + chatMessage
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}

data class DeviceInfo(val name: String, val address: String)
data class ChatMessage(val sender: String, val content: String)