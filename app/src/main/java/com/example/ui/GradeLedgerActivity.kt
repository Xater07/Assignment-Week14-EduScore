package com.example.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.R
import com.example.data.AppData
import com.example.data.GradeLedger
import com.example.databinding.ActivityGradeLedgerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class GradeLedgerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGradeLedgerBinding
    private lateinit var adapter: GradeAdapter
    private var isLecturer = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGradeLedgerBinding.inflate(layoutInflater)
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
            binding.fabEnterScore.visibility = View.VISIBLE
            binding.fabEnterScore.setOnClickListener { showEnterScoreDialog() }
        } else {
            binding.fabEnterScore.visibility = View.GONE
        }

        // Setup rank chart navigation
        binding.btnViewCharts.setOnClickListener {
            startActivity(Intent(this, ChartActivity::class.java))
        }

        setupRecyclerView()

        // Search text watcher
        binding.etSearchGrade.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterGrades(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = GradeAdapter(
            AppData.grades,
            isLecturer,
            onEditClick = { grade -> showEditScoreDialog(grade) },
            onDeleteClick = { grade -> confirmDeleteGrade(grade) }
        )
        binding.rvGrades.layoutManager = LinearLayoutManager(this)
        binding.rvGrades.adapter = adapter
    }

    private fun filterGrades(query: String) {
        val filtered = AppData.grades.filter { grade ->
            val studentName = AppData.students.find { it.studentId == grade.studentId }?.studentName ?: ""
            studentName.contains(query, ignoreCase = true)
        }
        adapter.updateList(filtered)
    }

    private fun showEnterScoreDialog() {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        // Student selector
        val tvStudentLabel = android.widget.TextView(context).apply {
            text = "Select Student:"
            setPadding(0, 10, 0, 8)
        }
        val spinnerStudent = Spinner(context)
        val studentOptions = AppData.students.map { "${it.studentId} - ${it.studentName}" }
        spinnerStudent.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, studentOptions)

        // Assessment selector
        val tvAssessmentLabel = android.widget.TextView(context).apply {
            text = "Select Assessment Type:"
            setPadding(0, 16, 0, 8)
        }
        val spinnerAssessment = Spinner(context)
        val assessmentOptions = AppData.assessments.map { assess ->
            val course = AppData.courses.find { it.courseId == assess.courseId }
            "${assess.assessmentId} - ${course?.courseName ?: ""} (${assess.assessmentType})"
        }
        spinnerAssessment.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, assessmentOptions)

        // Raw score input
        val tilScore = TextInputLayout(context).apply { hint = "Raw Score (0 - 100)" }
        val etScore = EditText(context).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        tilScore.addView(etScore)

        layout.addView(tvStudentLabel)
        layout.addView(spinnerStudent)
        layout.addView(tvAssessmentLabel)
        layout.addView(spinnerAssessment)
        layout.addView(tilScore)

        MaterialAlertDialogBuilder(context)
            .setTitle("Enter Academic Score")
            .setView(layout)
            .setPositiveButton("Submit") { _, _ ->
                val selectedStudentIdx = spinnerStudent.selectedItemPosition
                val selectedAssessmentIdx = spinnerAssessment.selectedItemPosition
                val rawScoreText = etScore.text.toString().trim()

                if (selectedStudentIdx == -1 || selectedAssessmentIdx == -1 || rawScoreText.isEmpty()) {
                    Toast.makeText(context, "Error: All selection fields are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val targetStudentId = AppData.students[selectedStudentIdx].studentId
                val targetAssessmentId = AppData.assessments[selectedAssessmentIdx].assessmentId
                val rawScore = rawScoreText.toFloatOrNull() ?: -1f

                if (rawScore < 0f || rawScore > 100f) {
                    Toast.makeText(context, "Error: Raw score must fall between 0.0 and 100.0", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                // IMPORTANT BUSINESS RULE check: block duplicate entries for the same student+assessment!
                val exists = AppData.grades.any { it.studentId == targetStudentId && it.assessmentId == targetAssessmentId }
                if (exists) {
                    MaterialAlertDialogBuilder(context)
                        .setTitle("Entry Blocked")
                        .setMessage("Cannot enter score: A grade record already exists for ${AppData.students[selectedStudentIdx].studentName} in this assessment type.")
                        .setPositiveButton("OK", null)
                        .show()
                    return@setPositiveButton
                }

                // Generate ID
                val nextNum = AppData.grades.map { it.gradeId.substringAfter("G").toIntOrNull() ?: 12 }.maxOrNull() ?: 12
                val newGradeId = String.format("G%02d", nextNum + 1)

                val newGrade = GradeLedger(newGradeId, targetStudentId, targetAssessmentId, rawScore)
                AppData.grades.add(newGrade)
                setupRecyclerView()
                binding.etSearchGrade.setText("")
                Toast.makeText(context, "Success: Score recorded successfully.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditScoreDialog(grade: GradeLedger) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        // Show context details as labels
        val studentName = AppData.students.find { it.studentId == grade.studentId }?.studentName ?: "Unknown"
        val assessment = AppData.assessments.find { it.assessmentId == grade.assessmentId }
        val courseName = AppData.courses.find { it.courseId == assessment?.courseId }?.courseName ?: ""
        val detailLabel = android.widget.TextView(context).apply {
            text = "Student: $studentName\nAssignment: $courseName - ${assessment?.assessmentType ?: ""}"
            setPadding(0, 10, 0, 16)
            textSize = 15f
        }

        val tilScore = TextInputLayout(context).apply { hint = "Raw Score (0 - 100)" }
        val etScore = EditText(context).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(grade.rawScore.toString())
        }
        tilScore.addView(etScore)

        layout.addView(detailLabel)
        layout.addView(tilScore)

        MaterialAlertDialogBuilder(context)
            .setTitle("Modify Score Card")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val rawScoreText = etScore.text.toString().trim()
                if (rawScoreText.isEmpty()) {
                    Toast.makeText(context, "Error: Raw score can't be empty.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val score = rawScoreText.toFloatOrNull() ?: -1f
                if (score < 0f || score > 100f) {
                    Toast.makeText(context, "Error: Invalid score value.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val idx = AppData.grades.indexOfFirst { it.gradeId == grade.gradeId }
                if (idx != -1) {
                    AppData.grades[idx] = GradeLedger(grade.gradeId, grade.studentId, grade.assessmentId, score)
                    setupRecyclerView()
                    Toast.makeText(context, "Success: Score updated in ledger.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteGrade(grade: GradeLedger) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this grade score?")
            .setPositiveButton("Delete") { _, _ ->
                AppData.grades.remove(grade)
                setupRecyclerView()
                Toast.makeText(this, "Success: Score deleted from ledger.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
