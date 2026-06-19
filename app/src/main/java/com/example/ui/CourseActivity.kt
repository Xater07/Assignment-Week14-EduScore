package com.example.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.AppData
import com.example.data.Course
import com.example.databinding.ActivityCourseBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class CourseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseBinding
    private lateinit var adapter: CourseAdapter
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        NavigationHelper.setupNavigation(this, binding.drawerLayout)

        val sp = getSharedPreferences("eduscore_prefs", Context.MODE_PRIVATE)
        val userRole = sp.getString("user_role", "Lecturer") ?: "Lecturer"
        isAdmin = userRole == "Admin"

        if (isAdmin) {
            binding.fabAddCourse.visibility = View.VISIBLE
            binding.fabAddCourse.setOnClickListener { showAddCourseDialog() }
        } else {
            binding.fabAddCourse.visibility = View.GONE
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = CourseAdapter(
            AppData.courses,
            isAdmin,
            onEditClick = { course -> showEditCourseDialog(course) },
            onDeleteClick = { course -> verifyAndDeleteCourse(course) }
        )
        binding.rvCourses.layoutManager = LinearLayoutManager(this)
        binding.rvCourses.adapter = adapter
    }

    private fun showAddCourseDialog() {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val tilId = TextInputLayout(context).apply { hint = "Course ID (e.g. C007)" }
        val etId = EditText(context).apply {
            val nextNum = AppData.courses.map { it.courseId.substringAfter("C").toIntOrNull() ?: 0 }.maxOrNull() ?: 6
            setText(String.format("C%03d", nextNum + 1))
        }
        tilId.addView(etId)

        val tilName = TextInputLayout(context).apply { hint = "Course Name (e.g. Kotlin Programming)" }
        val etName = EditText(context)
        tilName.addView(etName)

        val tilCredits = TextInputLayout(context).apply { hint = "Credit Hours" }
        val etCredits = EditText(context).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("3")
        }
        tilCredits.addView(etCredits)

        layout.addView(tilId)
        layout.addView(tilName)
        layout.addView(tilCredits)

        MaterialAlertDialogBuilder(context)
            .setTitle("Register New Course")
            .setView(layout)
            .setPositiveButton("Register") { _, _ ->
                val cId = etId.text.toString().trim()
                val cName = etName.text.toString().trim()
                val cCreditsText = etCredits.text.toString().trim()

                if (cId.isEmpty() || cName.isEmpty() || cCreditsText.isEmpty()) {
                    Toast.makeText(context, "Error: All inputs are required.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                if (AppData.courses.any { it.courseId.equals(cId, ignoreCase = true) }) {
                    Toast.makeText(context, "Error: Course ID $cId already exists.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val credits = cCreditsText.toIntOrNull() ?: 3
                val newCourse = Course(cId, cName, credits)
                AppData.courses.add(newCourse)
                setupRecyclerView()
                Toast.makeText(context, "Success: Course added successfully.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditCourseDialog(course: Course) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val tilId = TextInputLayout(context).apply { hint = "Course ID (Read-only)" }
        val etId = EditText(context).apply {
            setText(course.courseId)
            isEnabled = false
        }
        tilId.addView(etId)

        val tilName = TextInputLayout(context).apply { hint = "Course Name" }
        val etName = EditText(context).apply { setText(course.courseName) }
        tilName.addView(etName)

        val tilCredits = TextInputLayout(context).apply { hint = "Credit Hours" }
        val etCredits = EditText(context).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(course.creditHours.toString())
        }
        tilCredits.addView(etCredits)

        layout.addView(tilId)
        layout.addView(tilName)
        layout.addView(tilCredits)

        MaterialAlertDialogBuilder(context)
            .setTitle("Modify Course Information")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val cName = etName.text.toString().trim()
                val cCreditsText = etCredits.text.toString().trim()

                if (cName.isEmpty() || cCreditsText.isEmpty()) {
                    Toast.makeText(context, "Error: Inputs must be valid.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val credits = cCreditsText.toIntOrNull() ?: 3
                val idx = AppData.courses.indexOfFirst { it.courseId == course.courseId }
                if (idx != -1) {
                    AppData.courses[idx] = Course(course.courseId, cName, credits)
                    setupRecyclerView()
                    Toast.makeText(context, "Success: Course modified successfully.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun verifyAndDeleteCourse(course: Course) {
        // Enforce block rule: check if any academic enrollments exist for this course ID
        val enrolledCount = AppData.enrollments.count { it.courseId == course.courseId }
        if (enrolledCount > 0) {
            // Block delete if students are enrolled!
            MaterialAlertDialogBuilder(this)
                .setTitle("Action Blocked")
                .setMessage("Cannot delete course ${course.courseName} because there are currently $enrolledCount student(s) actively enrolled.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete course ${course.courseName}? This deletes its assessment templates.")
            .setPositiveButton("Delete") { _, _ ->
                AppData.courses.remove(course)
                AppData.assessments.removeAll { it.courseId == course.courseId }
                setupRecyclerView()
                Toast.makeText(this, "Success: Course deleted successfully.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
