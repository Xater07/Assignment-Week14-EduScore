package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import com.example.R
import com.google.android.material.button.MaterialButton

object NavigationHelper {
    fun setupNavigation(activity: Activity, drawerLayout: androidx.drawerlayout.widget.DrawerLayout) {
        val sp = activity.getSharedPreferences("eduscore_prefs", Context.MODE_PRIVATE)
        val userRole = sp.getString("user_role", "Lecturer") ?: "Lecturer"

        // Find navigation drawer buttons
        val btnDashboard = activity.findViewById<MaterialButton>(R.id.btn_nav_dashboard)
        val btnStudents = activity.findViewById<MaterialButton>(R.id.btn_nav_students)
        val btnCourses = activity.findViewById<MaterialButton>(R.id.btn_nav_courses)
        val btnGradeLedger = activity.findViewById<MaterialButton>(R.id.btn_nav_grade_ledger)
        val btnAssessments = activity.findViewById<MaterialButton>(R.id.btn_nav_assessments)
        val btnTerms = activity.findViewById<MaterialButton>(R.id.btn_nav_terms)
        val btnUsers = activity.findViewById<MaterialButton>(R.id.btn_nav_users)
        val btnLogout = activity.findViewById<MaterialButton>(R.id.btn_nav_logout)

        // Highlight selected nav state by checking active class
        val activeColor = 0xFF4A90D9.toInt() // Selected Accent color
        val inactiveColor = 0xFFFFFFFF.toInt() // White color

        btnDashboard?.setTextColor(if (activity is DashboardActivity) activeColor else inactiveColor)
        btnStudents?.setTextColor(if (activity is StudentActivity) activeColor else inactiveColor)
        btnCourses?.setTextColor(if (activity is CourseActivity) activeColor else inactiveColor)
        btnGradeLedger?.setTextColor(if (activity is GradeLedgerActivity) activeColor else inactiveColor)
        btnAssessments?.setTextColor(if (activity is AssessmentActivity) activeColor else inactiveColor)
        btnTerms?.setTextColor(if (activity is AcademicTermActivity) activeColor else inactiveColor)
        btnUsers?.setTextColor(if (activity is UserRolesActivity) activeColor else inactiveColor)

        // RBAC: Roles rules - hide "User & Roles" if role is Lecturer
        if (userRole == "Lecturer") {
            btnUsers?.visibility = View.GONE
        } else {
            btnUsers?.visibility = View.VISIBLE
        }

        // On clicks
        btnDashboard?.setOnClickListener {
            if (activity !is DashboardActivity) {
                activity.startActivity(Intent(activity, DashboardActivity::class.java))
                activity.finish()
            }
            drawerLayout.closeDrawers()
        }

        btnStudents?.setOnClickListener {
            if (activity !is StudentActivity) {
                activity.startActivity(Intent(activity, StudentActivity::class.java))
                activity.finish()
            }
            drawerLayout.closeDrawers()
        }

        btnCourses?.setOnClickListener {
            if (activity !is CourseActivity) {
                activity.startActivity(Intent(activity, CourseActivity::class.java))
                activity.finish()
            }
            drawerLayout.closeDrawers()
        }

        btnGradeLedger?.setOnClickListener {
            if (activity !is GradeLedgerActivity) {
                activity.startActivity(Intent(activity, GradeLedgerActivity::class.java))
                activity.finish()
            }
            drawerLayout.closeDrawers()
        }

        btnAssessments?.setOnClickListener {
            if (activity !is AssessmentActivity) {
                activity.startActivity(Intent(activity, AssessmentActivity::class.java))
                activity.finish()
            }
            drawerLayout.closeDrawers()
        }

        btnTerms?.setOnClickListener {
            if (activity !is AcademicTermActivity) {
                activity.startActivity(Intent(activity, AcademicTermActivity::class.java))
                activity.finish()
            }
            drawerLayout.closeDrawers()
        }

        btnUsers?.setOnClickListener {
            if (activity !is UserRolesActivity) {
                activity.startActivity(Intent(activity, UserRolesActivity::class.java))
                activity.finish()
            }
            drawerLayout.closeDrawers()
        }

        btnLogout?.setOnClickListener {
            sp.edit().clear().apply()
            activity.startActivity(Intent(activity, LoginActivity::class.java))
            activity.finishAffinity()
        }
    }
}
