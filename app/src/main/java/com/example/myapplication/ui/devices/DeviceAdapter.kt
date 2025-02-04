package com.example.myapplication.ui.devices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemDeviceBinding
import com.example.myapplication.network.DeviceInfo

class DeviceAdapter(private val onDeviceClick: (DeviceInfo) -> Unit) :
    ListAdapter<DeviceInfo, DeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding, onDeviceClick)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeviceViewHolder(
        private val binding: ItemDeviceBinding,
        private val onDeviceClick: (DeviceInfo) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: DeviceInfo) {
            binding.deviceName.text = device.name
            binding.deviceAddress.text = device.address
            binding.root.setOnClickListener { onDeviceClick(device) }
        }
    }
}

class DeviceDiffCallback : DiffUtil.ItemCallback<DeviceInfo>() {
    override fun areItemsTheSame(oldItem: DeviceInfo, newItem: DeviceInfo) = 
        oldItem.address == newItem.address

    override fun areContentsTheSame(oldItem: DeviceInfo, newItem: DeviceInfo) = 
        oldItem == newItem
}
