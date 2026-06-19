package com.example.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.R
import com.example.data.AppData
import com.example.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Force toolbar setup as central actionBar
        setSupportActionBar(binding.toolbar)
        
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Setup shared navigation listener
        NavigationHelper.setupNavigation(this, binding.drawerLayout)

        // Retrieve logged in details to custom visual welcome title banner
        val sp = getSharedPreferences("eduscore_prefs", Context.MODE_PRIVATE)
        val userEmail = sp.getString("user_email", "User") ?: "User"
        val userRole = sp.getString("user_role", "Lecturer") ?: "Lecturer"

        binding.tvWelcomeTitle.text = "Hello, $userRole!\n($userEmail)"

        // Update statistics cards
        updateStats()
    }

    override fun onResume() {
        super.onResume()
        updateStats()
    }

    private fun updateStats() {
        // 1. Total Students count
        val totalStudents = AppData.students.size
        binding.tvTotalStudents.text = totalStudents.toString()

        // 2. Active courses count (Total registeredcourses in our list of courses)
        val totalCourses = AppData.courses.size
        binding.tvActiveCourses.text = totalCourses.toString()

        // 3. Assessment categories template counts
        val assessmentsCount = AppData.assessments.size
        binding.tvAssessmentsCount.text = assessmentsCount.toString()

        // 4. Calculated average GPA (scale 4.0)
        val averageGpa = AppData.getAverageGpaOfAllStudents()
        binding.tvAverageGpa.text = String.format("%.2f", averageGpa)

        // 5. Active term standing label
        val activeTermText = AppData.getActiveTermName(this)
        binding.tvActiveTerm.text = "$activeTermText (Active)"
    }
}
