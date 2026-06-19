package com.example.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.AppData
import com.example.data.Student
import com.example.databinding.ActivityStudentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding
    private lateinit var adapter: StudentAdapter
    private var filteredList = mutableListOf<Student>()
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Shared navigation setup
        NavigationHelper.setupNavigation(this, binding.drawerLayout)

        // Check user role
        val sp = getSharedPreferences("eduscore_prefs", Context.MODE_PRIVATE)
        val userRole = sp.getString("user_role", "Lecturer") ?: "Lecturer"
        isAdmin = userRole == "Admin"

        // FAB visibility based on role
        if (isAdmin) {
            binding.fabAddStudent.visibility = View.VISIBLE
            binding.fabAddStudent.setOnClickListener { showAddStudentDialog() }
        } else {
            binding.fabAddStudent.visibility = View.GONE
        }

        // Init recyclerview
        setupRecyclerView()

        // Text listener for filter search bar
        binding.etSearchStudent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterStudents(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        filteredList.clear()
        filteredList.addAll(AppData.students)

        adapter = StudentAdapter(
            filteredList,
            isAdmin,
            onEditClick = { student -> showEditStudentDialog(student) },
            onDeleteClick = { student -> confirmDeleteStudent(student) }
        )

        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = adapter
    }

    private fun filterStudents(query: String) {
        val filtered = AppData.students.filter {
            it.studentName.contains(query, ignoreCase = true) ||
                    it.programme.contains(query, ignoreCase = true) ||
                    it.studentId.contains(query, ignoreCase = true)
        }
        adapter.updateList(filtered)
    }

    private fun showAddStudentDialog() {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val tilId = TextInputLayout(context).apply { hint = "Student ID (e.g. S006)" }
        val etId = EditText(context).apply {
            // Pre-fill next ID recommendation
            val nextNum = AppData.students.map { it.studentId.substringAfter("S").toIntOrNull() ?: 0 }.maxOrNull() ?: 5
            setText(String.format("S%03d", nextNum + 1))
        }
        tilId.addView(etId)

        val tilName = TextInputLayout(context).apply { hint = "Full Name" }
        val etName = EditText(context)
        tilName.addView(etName)

        val tilProgramme = TextInputLayout(context).apply { hint = "Programme (e.g. Computer Science)" }
        val etProgramme = EditText(context)
        tilProgramme.addView(etProgramme)

        // State Spinner for Active/Inactive status
        val statusSpinnerLabel = android.widget.TextView(context).apply {
            text = "Semester Admission Status:"
            setPadding(0, 16, 0, 8)
        }
        val spinnerStatus = Spinner(context)
        spinnerStatus.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Active", "Inactive")
        )

        layout.addView(tilId)
        layout.addView(tilName)
        layout.addView(tilProgramme)
        layout.addView(statusSpinnerLabel)
        layout.addView(spinnerStatus)

        MaterialAlertDialogBuilder(context)
            .setTitle("Register New Student")
            .setView(layout)
            .setPositiveButton("Add") { dialog, _ ->
                val sId = etId.text.toString().trim()
                val sName = etName.text.toString().trim()
                val sProg = etProgramme.text.toString().trim()
                val sStatus = spinnerStatus.selectedItem.toString()

                if (sId.isEmpty() || sName.isEmpty() || sProg.isEmpty()) {
                    Toast.makeText(context, "Error: All fields are required.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                // Block duplicates
                if (AppData.students.any { it.studentId.equals(sId, ignoreCase = true) }) {
                    Toast.makeText(context, "Error: Student with ID $sId already exists.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val newStudent = Student(sId, sName, sProg, sStatus)
                AppData.students.add(newStudent)
                setupRecyclerView()
                binding.etSearchStudent.setText("")
                Toast.makeText(context, "Success: Student registers successfully.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditStudentDialog(student: Student) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        // Student ID is read-only
        val tilId = TextInputLayout(context).apply { hint = "Student ID (Read-only)" }
        val etId = EditText(context).apply {
            setText(student.studentId)
            isEnabled = false
        }
        tilId.addView(etId)

        val tilName = TextInputLayout(context).apply { hint = "Full Name" }
        val etName = EditText(context).apply { setText(student.studentName) }
        tilName.addView(etName)

        val tilProgramme = TextInputLayout(context).apply { hint = "Programme" }
        val etProgramme = EditText(context).apply { setText(student.programme) }
        tilProgramme.addView(etProgramme)

        val statusSpinnerLabel = android.widget.TextView(context).apply {
            text = "Status:"
            setPadding(0, 16, 0, 8)
        }
        val spinnerStatus = Spinner(context)
        val listOptions = listOf("Active", "Inactive")
        spinnerStatus.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            listOptions
        )
        spinnerStatus.setSelection(listOptions.indexOf(student.semesterStatus).coerceAtLeast(0))

        layout.addView(tilId)
        layout.addView(tilName)
        layout.addView(tilProgramme)
        layout.addView(statusSpinnerLabel)
        layout.addView(spinnerStatus)

        MaterialAlertDialogBuilder(context)
            .setTitle("Modify Student Details")
            .setView(layout)
            .setPositiveButton("Save Changes") { _, _ ->
                val sName = etName.text.toString().trim()
                val sProg = etProgramme.text.toString().trim()
                val sStatus = spinnerStatus.selectedItem.toString()

                if (sName.isEmpty() || sProg.isEmpty()) {
                    Toast.makeText(context, "Error: Non-empty values are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val idx = AppData.students.indexOfFirst { it.studentId == student.studentId }
                if (idx != -1) {
                    AppData.students[idx] = Student(student.studentId, sName, sProg, sStatus)
                    setupRecyclerView()
                    binding.etSearchStudent.setText("")
                    Toast.makeText(context, "Success: Student information modified.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteStudent(student: Student) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Student")
            .setMessage("Are you sure you want to delete ${student.studentName}? This action deletes their corresponding enrollments and grade scores.")
            .setPositiveButton("Remove") { _, _ ->
                // Clean up references in AppData
                AppData.students.remove(student)
                AppData.enrollments.removeAll { it.studentId == student.studentId }
                AppData.grades.removeAll { it.studentId == student.studentId }

                setupRecyclerView()
                binding.etSearchStudent.setText("")
                Toast.makeText(this, "Success: Student removed successfully.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
