package com.example.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.data.User
import com.example.databinding.ItemUserBinding

class UserAdapter(
    private var usersList: List<User>,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = usersList[position]
        holder.binding.tvUserEmail.text = user.email
        holder.binding.tvUserId.text = "UserId: ${user.userId}"

        holder.binding.chipRole.text = user.role
        if (user.role.equals("Admin", ignoreCase = true)) {
            holder.binding.chipRole.setBackgroundColor(0xFF1E3A5F.toInt()) // Navy
        } else {
            holder.binding.chipRole.setBackgroundColor(0xFF4A90D9.toInt()) // Accent/Blue
        }

        holder.binding.btnEditUser.setOnClickListener { onEditClick(user) }
        holder.binding.btnDeleteUser.setOnClickListener { onDeleteClick(user) }
    }

    override fun getItemCount(): Int = usersList.size

    fun updateList(newList: List<User>) {
        usersList = newList
        notifyDataSetChanged()
    }
}
