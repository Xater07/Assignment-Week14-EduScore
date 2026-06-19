package com.example.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.data.Assessment
import com.example.databinding.ItemAssessmentBinding

class AssessmentAdapter(
    private var assessmentList: List<Assessment>,
    private val isLecturer: Boolean,
    private val onEditClick: (Assessment) -> Unit,
    private val onDeleteClick: (Assessment) -> Unit
) : RecyclerView.Adapter<AssessmentAdapter.AssessmentViewHolder>() {

    inner class AssessmentViewHolder(val binding: ItemAssessmentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssessmentViewHolder {
        val binding = ItemAssessmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssessmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssessmentViewHolder, position: Int) {
        val assessment = assessmentList[position]
        holder.binding.tvAssessmentType.text = assessment.assessmentType
        holder.binding.tvAssessmentId.text = "Assessment ID: ${assessment.assessmentId}"
        holder.binding.tvWeightageBadge.text = "Weightage: ${assessment.weightage}%"

        // Lecturer based logic
        if (isLecturer) {
            holder.binding.layoutLecturerActions.visibility = View.VISIBLE
            holder.binding.btnEditAssessment.setOnClickListener { onEditClick(assessment) }
            holder.binding.btnDeleteAssessment.setOnClickListener { onDeleteClick(assessment) }
        } else {
            holder.binding.layoutLecturerActions.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = assessmentList.size

    fun updateList(newList: List<Assessment>) {
        assessmentList = newList
        notifyDataSetChanged()
    }
}
