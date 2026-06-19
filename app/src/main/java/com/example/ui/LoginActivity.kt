package com.example.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.data.AppData
import com.example.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make sure AppData is seeded on launch
        AppData.initDatabase(this)

        // Check if already logged in
        val sp = getSharedPreferences("eduscore_prefs", Context.MODE_PRIVATE)
        val userEmail = sp.getString("user_email", null)
        val userRole = sp.getString("user_role", null)
        if (userEmail != null && userRole != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.tvLoginError.text = "Error: Please enter both email and password."
                binding.tvLoginError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Validate against AppData.users
            val matchedUser = AppData.users.find { it.email.equals(email, ignoreCase = true) && it.password == password }
            if (matchedUser != null) {
                // Save credentials
                sp.edit().apply {
                    putString("user_email", matchedUser.email)
                    putString("user_role", matchedUser.role)
                    putString("user_id", matchedUser.userId)
                    apply()
                }

                binding.tvLoginError.visibility = View.GONE
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                binding.tvLoginError.text = "Error: Invalid email or password."
                binding.tvLoginError.visibility = View.VISIBLE
            }
        }
    }
}
