package com.example.myapplication.ui.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityChatBinding
import com.example.myapplication.network.NetworkService
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var networkService: NetworkService
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var targetDeviceAddress: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetDeviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS) ?: return
        networkService = NetworkService(this)
        setupMessageList()
        setupSendButton()
        observeMessages()
    }

    private fun setupMessageList() {
        messageAdapter = MessageAdapter()
        binding.messageList.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = messageAdapter
        }
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString()
            if (message.isNotEmpty()) {
                networkService.sendMessage(message, targetDeviceAddress)
                binding.messageInput.text.clear()
            }
        }
    }

    private fun observeMessages() {
        lifecycleScope.launch {
            networkService.messages.collect { messages ->
                messageAdapter.submitList(messages)
            }
        }
    }

    companion object {
        const val EXTRA_DEVICE_ADDRESS = "device_address"
    }
}