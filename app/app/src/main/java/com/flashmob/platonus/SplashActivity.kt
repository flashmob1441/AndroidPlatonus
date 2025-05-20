package com.flashmob.platonus

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flashmob.platonus.data.storage.AuthManager
import com.flashmob.platonus.ui.auth.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = AuthManager(this)
        val token = auth.token
        val user = auth.user

        if (token != null && user != null) {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_USER_ID, user.id)
                putExtra(MainActivity.EXTRA_USER_ROLE, user.role.name)
                putExtra(MainActivity.EXTRA_USER_NAME, user.name)
                putExtra(MainActivity.EXTRA_USER_EMAIL, user.email)
                user.course?.let { putExtra(MainActivity.EXTRA_USER_COURSE, it) }
            }
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}