package com.example.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.data.AppData
import com.example.data.Course
import com.example.databinding.ItemCourseBinding

class CourseAdapter(
    private var coursesList: List<Course>,
    private val isAdmin: Boolean,
    private val onEditClick: (Course) -> Unit,
    private val onDeleteClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = coursesList[position]
        holder.binding.tvCourseName.text = course.courseName
        holder.binding.tvCourseCode.text = course.courseId
        holder.binding.tvCreditHours.text = "Credits: ${course.creditHours} hrs"

        // Count enrolled students from AppData
        val enrollmentCount = AppData.enrollments.count { it.courseId == course.courseId }
        holder.binding.badgeEnrolledCount.text = "$enrollmentCount Enrolled"

        // Display actions based on role
        if (isAdmin) {
            holder.binding.layoutAdminActions.visibility = View.VISIBLE
            holder.binding.btnEditCourse.setOnClickListener { onEditClick(course) }
            holder.binding.btnDeleteCourse.setOnClickListener { onDeleteClick(course) }
        } else {
            holder.binding.layoutAdminActions.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = coursesList.size

    fun updateList(newList: List<Course>) {
        coursesList = newList
        notifyDataSetChanged()
    }
}
