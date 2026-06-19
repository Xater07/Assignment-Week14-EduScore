package com.example.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.data.AcademicTerm
import com.example.databinding.ItemAcademicTermBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AcademicTermAdapter(
    private var termsList: List<AcademicTerm>,
    private val isAdmin: Boolean,
    private val onEditClick: (AcademicTerm) -> Unit,
    private val onDeleteClick: (AcademicTerm) -> Unit
) : RecyclerView.Adapter<AcademicTermAdapter.TermViewHolder>() {

    inner class TermViewHolder(val binding: ItemAcademicTermBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TermViewHolder {
        val binding = ItemAcademicTermBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TermViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TermViewHolder, position: Int) {
        val term = termsList[position]
        holder.binding.tvTermName.text = term.termName
        holder.binding.tvTermIdDates.text = "${term.termId} • ${term.startDate} to ${term.endDate}"

        // Calculate dynamic active status chip based on today's local date
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val isActive = todayStr >= term.startDate && todayStr <= term.endDate

        if (isActive) {
            holder.binding.chipTermStatus.text = "Active"
            holder.binding.chipTermStatus.setBackgroundColor(0xFF2E7D32.toInt()) // Active green
        } else {
            holder.binding.chipTermStatus.text = "Inactive"
            holder.binding.chipTermStatus.setBackgroundColor(0xFF9E9E9E.toInt()) // Inactive grey
        }

        // Administrative tools
        if (isAdmin) {
            holder.binding.layoutAdminActions.visibility = View.VISIBLE
            holder.binding.btnEditTerm.setOnClickListener { onEditClick(term) }
            holder.binding.btnDeleteTerm.setOnClickListener { onDeleteClick(term) }
        } else {
            holder.binding.layoutAdminActions.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = termsList.size

    fun updateList(newList: List<AcademicTerm>) {
        termsList = newList
        notifyDataSetChanged()
    }
}
