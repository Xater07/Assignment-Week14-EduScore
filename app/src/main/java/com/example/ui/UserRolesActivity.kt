package com.example.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.AppData
import com.example.data.User
import com.example.databinding.ActivityUserRolesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class UserRolesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserRolesBinding
    private lateinit var adapter: UserAdapter
    private var loggedInEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserRolesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Access check
        val sp = getSharedPreferences("eduscore_prefs", Context.MODE_PRIVATE)
        val userRole = sp.getString("user_role", "Lecturer") ?: "Lecturer"
        loggedInEmail = sp.getString("user_email", "") ?: ""

        if (userRole != "Admin") {
            Toast.makeText(this, "Access Denied: Administratve access only.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        NavigationHelper.setupNavigation(this, binding.drawerLayout)

        binding.fabAddUser.setOnClickListener { showAddUserDialog() }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(
            AppData.users,
            onEditClick = { user -> showEditUserDialog(user) },
            onDeleteClick = { user -> handleVerifyAndDeleteUser(user) }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
    }

    private fun showAddUserDialog() {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val tilId = TextInputLayout(context).apply { hint = "User ID (e.g. U003)" }
        val etId = EditText(context).apply {
            val nextNum = AppData.users.map { it.userId.substringAfter("U").toIntOrNull() ?: 2 }.maxOrNull() ?: 2
            setText(String.format("U%03d", nextNum + 1))
        }
        tilId.addView(etId)

        val tilEmail = TextInputLayout(context).apply { hint = "Email Address" }
        val etEmail = EditText(context).apply { inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS }
        tilEmail.addView(etEmail)

        val tilPassword = TextInputLayout(context).apply { hint = "Password (Plain Text)" }
        val etPassword = EditText(context).apply { inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        tilPassword.addView(etPassword)

        val tvRoleLabel = android.widget.TextView(context).apply {
            text = "Select Privilege Role:"
            setPadding(0, 16, 0, 8)
        }
        val spinnerRole = Spinner(context)
        spinnerRole.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listOf("Admin", "Lecturer"))

        layout.addView(tilId)
        layout.addView(tilEmail)
        layout.addView(tilPassword)
        layout.addView(tvRoleLabel)
        layout.addView(spinnerRole)

        MaterialAlertDialogBuilder(context)
            .setTitle("Add User Profile")
            .setView(layout)
            .setPositiveButton("Create") { _, _ ->
                val uId = etId.text.toString().trim()
                val uEmail = etEmail.text.toString().trim()
                val uPass = etPassword.text.toString().trim()
                val uRole = spinnerRole.selectedItem.toString()

                if (uId.isEmpty() || uEmail.isEmpty() || uPass.isEmpty()) {
                    Toast.makeText(context, "Error: All fields are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (AppData.users.any { it.userId.equals(uId, ignoreCase = true) }) {
                    Toast.makeText(context, "Error: User ID $uId already exists.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (AppData.users.any { it.email.equals(uEmail, ignoreCase = true) }) {
                    Toast.makeText(context, "Error: Email $uEmail is already registered.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newUser = User(uId, uEmail, uPass, uRole)
                AppData.users.add(newUser)
                setupRecyclerView()
                Toast.makeText(context, "Success: User profile added.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditUserDialog(user: User) {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val tilId = TextInputLayout(context).apply { hint = "User ID (Read-only)" }
        val etId = EditText(context).apply {
            setText(user.userId)
            isEnabled = false
        }
        tilId.addView(etId)

        val tilEmail = TextInputLayout(context).apply { hint = "Email" }
        val etEmail = EditText(context).apply {
            setText(user.email)
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        tilEmail.addView(etEmail)

        val tilPassword = TextInputLayout(context).apply { hint = "Password" }
        val etPassword = EditText(context).apply {
            setText(user.password)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        tilPassword.addView(etPassword)

        val tvRoleLabel = android.widget.TextView(context).apply {
            text = "Role:"
            setPadding(0, 16, 0, 8)
        }
        val spinnerRole = Spinner(context)
        val listRoles = listOf("Admin", "Lecturer")
        spinnerRole.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listRoles)
        spinnerRole.setSelection(listRoles.indexOf(user.role).coerceAtLeast(0))

        layout.addView(tilId)
        layout.addView(tilEmail)
        layout.addView(tilPassword)
        layout.addView(tvRoleLabel)
        layout.addView(spinnerRole)

        MaterialAlertDialogBuilder(context)
            .setTitle("Modify User Record")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val uEmail = etEmail.text.toString().trim()
                val uPass = etPassword.text.toString().trim()
                val uRole = spinnerRole.selectedItem.toString()

                if (uEmail.isEmpty() || uPass.isEmpty()) {
                    Toast.makeText(context, "Error: Inputs can't be empty.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val idx = AppData.users.indexOfFirst { it.userId == user.userId }
                if (idx != -1) {
                    AppData.users[idx] = User(user.userId, uEmail, uPass, uRole)
                    setupRecyclerView()
                    Toast.makeText(context, "Success: User profile updated.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleVerifyAndDeleteUser(user: User) {
        // ENFORCE CRITICAL RULE: "block deleting your own currently logged-in account"
        if (user.email.equals(loggedInEmail, ignoreCase = true)) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Action Blocked")
                .setMessage("Safety Protection: You are strictly forbidden from deleting your own currently active logged-in administrator profile.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete profile for ${user.email}?")
            .setPositiveButton("Delete") { _, _ ->
                AppData.users.remove(user)
                setupRecyclerView()
                Toast.makeText(this, "Success: User deleted.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
