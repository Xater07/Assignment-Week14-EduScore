package com.example.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.R
import com.example.data.AppData
import com.example.data.Assessment
import com.example.databinding.ActivityAssessmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class AssessmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssessmentBinding
    private lateinit var adapter: AssessmentAdapter
    private var selectedCourseId: String = ""
    private var isLecturer = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssessmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        NavigationHelper.setupNavigation(this, binding.drawerLayout)

        val sp = getSharedPreferences("eduscore_prefs", Context.MODE_PRIVATE)
        val userRole = sp.getString("user_role", "Lecturer") ?: "Lecturer"
        isLecturer = userRole == "Lecturer"

        if (isLecturer) {
            binding.fabAddAssessment.visibility = View.VISIBLE
            binding.fabAddAssessment.setOnClickListener { showAddAssessmentDialog() }
        } else {
            binding.fabAddAssessment.visibility = View.GONE
        }

        setupCourseFilter()
    }

    private fun setupCourseFilter() {
        val coursesList = AppData.courses
        if (coursesList.isEmpty()) return

        // Format selector names: "Course ID - Course Name"
        val courseDisplayStrings = coursesList.map { "${it.courseId} - ${it.courseName}" }
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courseDisplayStrings).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerCoursesFilter.adapter = arrayAdapter

        binding.spinnerCoursesFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCourseId = coursesList[position].courseId
                reloadAssessments()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Set initial
        if (coursesList.isNotEmpty()) {
            selectedCourseId = coursesList[0].courseId
            reloadAssessments()
        }
    }

    private fun reloadAssessments() {
        val assessmentsForCourse = AppData.assessments.filter { it.courseId == selectedCourseId }

        // Set running weight total
        val totalWeight = assessmentsForCourse.map { it.weightage }.sum()
        binding.tvTotalWeightageLabel.text = "Total Weightage: $totalWeight%"

        if (totalWeight == 100f) {
            binding.tvTotalWeightageLabel.setTextColor(0xFF2E7D32.toInt()) // success green
            binding.tvTotalWeightageSubtitle.text = "Weightage sums to exactly 100% (Complete)"
            binding.ivWeightageStatusIcon.setImageResource(android.R.drawable.checkbox_on_background)
            binding.ivWeightageStatusIcon.setColorFilter(0xFF2E7D32.toInt())
        } else {
            binding.tvTotalWeightageLabel.setTextColor(0xFFDC2626.toInt()) // error red
            binding.tvTotalWeightageSubtitle.text = "Status: INCOMPLETE! Total weight must equal 100%"
            binding.ivWeightageStatusIcon.setImageResource(android.R.drawable.stat_sys_warning)
            binding.ivWeightageStatusIcon.setColorFilter(0xFFDC2626.toInt())
        }

        // Setup recyclerview
        adapter = AssessmentAdapter(
            assessmentsForCourse,
            isLecturer,
            onEditClick = { assessment -> showEditAssessmentDialog(assessment) },
            onDeleteClick = { assessment -> deleteAssessment(assessment) }
        )
        binding.rvAssessments.layoutManager = LinearLayoutManager(this)
        binding.rvAssessments.adapter = adapter
    }

    private fun showAddAssessmentDialog() {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val tilId = TextInputLayout(context).apply { hint = "Assessment ID (e.g. A019)" }
        val etId = EditText(context).apply {
            val nextNum = AppData.assessments.map { it.assessmentId.substringAfter("A0").toIntOrNull() ?: it.assessmentId.substringAfter("A").toIntOrNull() ?: 0 }.maxOrNull() ?: 18
            setText(String.format("A%03d", nextNum + 1))
        }
        tilId.addView(etId)

        val tilType = TextInputLayout(context).apply { hint = "Assessment Type (e.g. Quizzes, Projects)" }
        val etType = EditText(context)
        tilType.addView(etType)

        val tilWeight = TextInputLayout(context).apply { hint = "Weightage Percentage (0 - 100)" }
        val etWeight = EditText(context).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        tilWeight.addView(etWeight)

        layout.addView(tilId)
        layout.addView(tilType)
        layout.addView(tilWeight)

        MaterialAlertDialogBuilder(context)
            .setTitle("Add Assessment Type")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val aId = etId.text.toString().trim()
                val aType = etType.text.toString().trim()
                val aWeightText = etWeight.text.toString().trim()

                if (aId.isEmpty() || aType.isEmpty() || aWeightText.isEmpty()) {
                    Toast.makeText(context, "Error: All fields are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (AppData.assessments.any { it.assessmentId.equals(aId, ignoreCase = true) }) {
                    Toast.makeText(context, "Error: Assessment ID $aId already exists.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val inputWeight = aWeightText.toFloatOrNull() ?: 0f
                val activeAssessments = AppData.assessments.filter { it.courseId == selectedCourseId }
                val currentWeightSum = activeAssessments.sumOf { it.weightage.toDouble() }.toFloat()

                // Business logic check: block adding if total weightage would exceed 100%!
                if (currentWeightSum + inputWeight > 100f) {
                    Toast.makeText(context, "Blocked: Total weightage would exceed 100% (Sum: ${currentWeightSum + inputWeight}%)", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val newAssessment = Assessment(aId, selectedCourseId, aType, inputWeight)
                AppData.assessments.add(newAssessment)
                reloadAssessments()
                Toast.makeText(context, "Success: Assessment type defined.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditAssessmentDialog(assessment: Assessment) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val tilId = TextInputLayout(context).apply { hint = "Assessment ID (Read-only)" }
        val etId = EditText(context).apply {
            setText(assessment.assessmentId)
            isEnabled = false
        }
        tilId.addView(etId)

        val tilType = TextInputLayout(context).apply { hint = "Assessment Type" }
        val etType = EditText(context).apply { setText(assessment.assessmentType) }
        tilType.addView(etType)

        val tilWeight = TextInputLayout(context).apply { hint = "Weightage" }
        val etWeight = EditText(context).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(assessment.weightage.toString())
        }
        tilWeight.addView(etWeight)

        layout.addView(tilId)
        layout.addView(tilType)
        layout.addView(tilWeight)

        MaterialAlertDialogBuilder(context)
            .setTitle("Modify Assessment Parameters")
            .setView(layout)
            .setPositiveButton("Save Changes") { _, _ ->
                val aType = etType.text.toString().trim()
                val aWeightText = etWeight.text.toString().trim()

                if (aType.isEmpty() || aWeightText.isEmpty()) {
                    Toast.makeText(context, "Error: Standard inputs required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val inputWeight = aWeightText.toFloatOrNull() ?: 0f
                val activeAssessments = AppData.assessments.filter { it.courseId == selectedCourseId && it.assessmentId != assessment.assessmentId }
                val currentWeightSum = activeAssessments.sumOf { it.weightage.toDouble() }.toFloat()

                // Check total limits
                if (currentWeightSum + inputWeight > 100f) {
                    Toast.makeText(context, "Blocked: Total weightage would exceed 100% (Sum: ${currentWeightSum + inputWeight}%)", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val idx = AppData.assessments.indexOfFirst { it.assessmentId == assessment.assessmentId }
                if (idx != -1) {
                    AppData.assessments[idx] = Assessment(assessment.assessmentId, selectedCourseId, aType, inputWeight)
                    reloadAssessments()
                    Toast.makeText(context, "Success: Assessment rules saved.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAssessment(assessment: Assessment) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Assessment Template")
            .setMessage("Are you sure you want to delete ${assessment.assessmentType}? Corresponding records in Grade Ledger will be cleared.")
            .setPositiveButton("Remove") { _, _ ->
                AppData.assessments.remove(assessment)
                // Cascading delete score files
                AppData.grades.removeAll { it.assessmentId == assessment.assessmentId }
                reloadAssessments()
                Toast.makeText(this, "Success: Assessment template deleted.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
