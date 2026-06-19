package com.example.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.data.Student
import com.example.databinding.ItemStudentBinding

class StudentAdapter(
    private var studentsList: List<Student>,
    private val isAdmin: Boolean,
    private val onEditClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    inner class StudentViewHolder(val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = studentsList[position]
        holder.binding.tvStudentName.text = student.studentName
        holder.binding.tvStudentIdProgramme.text = "${student.studentId} • ${student.programme}"

        // Set colored background chip
        holder.binding.chipStatus.text = student.semesterStatus
        if (student.semesterStatus.equals("Active", ignoreCase = true)) {
            holder.binding.chipStatus.setBackgroundColor(0xFF2E7D32.toInt()) // Active green
        } else {
            holder.binding.chipStatus.setBackgroundColor(0xFF9E9E9E.toInt()) // Inactive grey
        }

        // Hide administrative actions if current user is not an Admin
        if (isAdmin) {
            holder.binding.layoutAdminActions.visibility = View.VISIBLE
            holder.binding.btnEditStudent.setOnClickListener { onEditClick(student) }
            holder.binding.btnDeleteStudent.setOnClickListener { onDeleteClick(student) }
        } else {
            holder.binding.layoutAdminActions.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = studentsList.size

    fun updateList(newList: List<Student>) {
        studentsList = newList
        notifyDataSetChanged()
    }
}
