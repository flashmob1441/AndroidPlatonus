package com.flashmob.platonus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.flashmob.platonus.data.repository.AuthRepository
import com.flashmob.platonus.databinding.ActivityLoginBinding
import com.flashmob.platonus.MainActivity
import com.flashmob.platonus.util.ViewModelFactory
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels {
        ViewModelFactory {
            LoginViewModel(AuthRepository(this))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(binding.root, "Пожалуйста, заполните все поля", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            loginViewModel.login(email, password)
        }
    }

    private fun setupObservers() {
        loginViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBarLogin.visibility = if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.buttonLogin.isEnabled = !isLoading
        }

        loginViewModel.loginResult.observe(this) { result ->
            result.success?.let { user ->
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_USER_ID, user.id)
                intent.putExtra(MainActivity.EXTRA_USER_ROLE, user.role.name)
                intent.putExtra(MainActivity.EXTRA_USER_NAME, user.name)
                intent.putExtra(MainActivity.EXTRA_USER_EMAIL, user.email)
                user.course?.let {
                    intent.putExtra(MainActivity.EXTRA_USER_COURSE, it)
                }
                startActivity(intent)
                finish()
            }
            result.error?.let {
                Log.e("LoginActivity", it)
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}