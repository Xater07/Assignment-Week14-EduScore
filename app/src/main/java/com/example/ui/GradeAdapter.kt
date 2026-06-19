package com.example.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.data.AppData
import com.example.data.GradeLedger
import com.example.databinding.ItemGradeBinding

class GradeAdapter(
    private var gradesList: List<GradeLedger>,
    private val isLecturer: Boolean,
    private val onEditClick: (GradeLedger) -> Unit,
    private val onDeleteClick: (GradeLedger) -> Unit
) : RecyclerView.Adapter<GradeAdapter.GradeViewHolder>() {

    inner class GradeViewHolder(val binding: ItemGradeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val binding = ItemGradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GradeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
        val grade = gradesList[position]

        // 1. Resolve student name
        val student = AppData.students.find { it.studentId == grade.studentId }
        val studentName = student?.studentName ?: "Unknown Student"
        holder.binding.tvGradeStudentName.text = studentName

        // 2. Resolve Course & Assessment type names
        val assessment = AppData.assessments.find { it.assessmentId == grade.assessmentId }
        val course = AppData.courses.find { it.courseId == assessment?.courseId }
        val detailsString = if (course != null && assessment != null) {
            "${course.courseName} - ${assessment.assessmentType}"
        } else {
            "Assessment details missing"
        }
        holder.binding.tvGradeDetails.text = detailsString

        // 3. Score math calculations
        val raw = grade.rawScore
        val weight = assessment?.weightage ?: 0f
        val weightedScore = raw * (weight / 100f)

        holder.binding.tvRawScore.text = "Raw: $raw"
        holder.binding.tvWeightedScore.text = String.format("Weighted: %.1f%%", weightedScore)

        // 4. Role based button toggles
        if (isLecturer) {
            holder.binding.layoutLecturerActions.visibility = View.VISIBLE
            holder.binding.btnEditGrade.setOnClickListener { onEditClick(grade) }
            holder.binding.btnDeleteGrade.setOnClickListener { onDeleteClick(grade) }
        } else {
            holder.binding.layoutLecturerActions.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = gradesList.size

    fun updateList(newList: List<GradeLedger>) {
        gradesList = newList
        notifyDataSetChanged()
    }
}
