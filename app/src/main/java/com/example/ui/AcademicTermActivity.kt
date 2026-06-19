package com.example.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.AcademicTerm
import com.example.data.AppData
import com.example.databinding.ActivityAcademicTermBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AcademicTermActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcademicTermBinding
    private lateinit var adapter: AcademicTermAdapter
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcademicTermBinding.inflate(layoutInflater)
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
            binding.fabAddTerm.visibility = View.VISIBLE
            binding.fabAddTerm.setOnClickListener { showAddTermDialog() }
        } else {
            binding.fabAddTerm.visibility = View.GONE
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = AcademicTermAdapter(
            AppData.academicTerms,
            isAdmin,
            onEditClick = { term -> showEditTermDialog(term) },
            onDeleteClick = { term -> handleVerifyAndDeleteTerm(term) }
        )
        binding.rvAcademicTerms.layoutManager = LinearLayoutManager(this)
        binding.rvAcademicTerms.adapter = adapter
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            editText.setText(formattedDate)
        }, year, month, day).show()
    }

    private fun showAddTermDialog() {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val tilId = TextInputLayout(context).apply { hint = "Term ID (e.g. T003)" }
        val etId = EditText(context).apply {
            val nextNum = AppData.academicTerms.map { it.termId.substringAfter("T").toIntOrNull() ?: 2 }.maxOrNull() ?: 2
            setText(String.format("T%03d", nextNum + 1))
        }
        tilId.addView(etId)

        val tilName = TextInputLayout(context).apply { hint = "Academic Term Name" }
        val etName = EditText(context).apply { hint = "Semester 1 2026/2027" }
        tilName.addView(etName)

        val tilStart = TextInputLayout(context).apply { hint = "Start Date (Click to choose)" }
        val etStart = EditText(context).apply {
            inputType = android.text.InputType.TYPE_NULL
            isFocusable = false
            setOnClickListener { showDatePicker(this) }
        }
        tilStart.addView(etStart)

        val tilEnd = TextInputLayout(context).apply { hint = "End Date (Click to choose)" }
        val etEnd = EditText(context).apply {
            inputType = android.text.InputType.TYPE_NULL
            isFocusable = false
            setOnClickListener { showDatePicker(this) }
        }
        tilEnd.addView(etEnd)

        layout.addView(tilId)
        layout.addView(tilName)
        layout.addView(tilStart)
        layout.addView(tilEnd)

        MaterialAlertDialogBuilder(context)
            .setTitle("Add Academic Term")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val tId = etId.text.toString().trim()
                val tName = etName.text.toString().trim()
                val tStart = etStart.text.toString().trim()
                val tEnd = etEnd.text.toString().trim()

                if (tId.isEmpty() || tName.isEmpty() || tStart.isEmpty() || tEnd.isEmpty()) {
                    Toast.makeText(context, "Error: All fields are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (AppData.academicTerms.any { it.termId.equals(tId, ignoreCase = true) }) {
                    Toast.makeText(context, "Error: Term ID $tId already exists.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Check dates logic: start < end
                if (tStart >= tEnd) {
                    Toast.makeText(context, "Error: Start date must be before end date.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val newTerm = AcademicTerm(tId, tName, tStart, tEnd)
                AppData.academicTerms.add(newTerm)
                setupRecyclerView()
                Toast.makeText(context, "Success: Term recorded successfully.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTermDialog(term: AcademicTerm) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val tilId = TextInputLayout(context).apply { hint = "Term ID (Read-only)" }
        val etId = EditText(context).apply {
            setText(term.termId)
            isEnabled = false
        }
        tilId.addView(etId)

        val tilName = TextInputLayout(context).apply { hint = "Term Name" }
        val etName = EditText(context).apply { setText(term.termName) }
        tilName.addView(etName)

        val tilStart = TextInputLayout(context).apply { hint = "Start Date (Click to choose)" }
        val etStart = EditText(context).apply {
            inputType = android.text.InputType.TYPE_NULL
            isFocusable = false
            setText(term.startDate)
            setOnClickListener { showDatePicker(this) }
        }
        tilStart.addView(etStart)

        val tilEnd = TextInputLayout(context).apply { hint = "End Date (Click to choose)" }
        val etEnd = EditText(context).apply {
            inputType = android.text.InputType.TYPE_NULL
            isFocusable = false
            setText(term.endDate)
            setOnClickListener { showDatePicker(this) }
        }
        tilEnd.addView(etEnd)

        layout.addView(tilId)
        layout.addView(tilName)
        layout.addView(tilStart)
        layout.addView(tilEnd)

        MaterialAlertDialogBuilder(context)
            .setTitle("Modify Academic Period")
            .setView(layout)
            .setPositiveButton("Save Changes") { _, _ ->
                val tName = etName.text.toString().trim()
                val tStart = etStart.text.toString().trim()
                val tEnd = etEnd.text.toString().trim()

                if (tName.isEmpty() || tStart.isEmpty() || tEnd.isEmpty()) {
                    Toast.makeText(context, "Error: All inputs are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (tStart >= tEnd) {
                    Toast.makeText(context, "Error: Start date must precede end date.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                val idx = AppData.academicTerms.indexOfFirst { it.termId == term.termId }
                if (idx != -1) {
                    AppData.academicTerms[idx] = AcademicTerm(term.termId, tName, tStart, tEnd)
                    setupRecyclerView()
                    Toast.makeText(context, "Success: Term modified successfully.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleVerifyAndDeleteTerm(term: AcademicTerm) {
        // Enforce rule: "block delete if the term has enrollments"
        val enrolledCount = AppData.enrollments.count { it.termId == term.termId }
        if (enrolledCount > 0) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Term Locked")
                .setMessage("Cannot delete term ${term.termName} because there are actively $enrolledCount course enrollment(s) referencing this academic period.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete ${term.termName}?")
            .setPositiveButton("Delete") { _, _ ->
                AppData.academicTerms.remove(term)
                setupRecyclerView()
                Toast.makeText(this, "Success: Term deleted successfully.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
