package com.example.sikka_grphsfixed

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.example.sikka_grphsfixed.fragments.Login

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash)

        val sikkaText = findViewById<TextView>(R.id.sikkaTextView)
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        sikkaText.startAnimation(rotateAnimation)

        // Move to next screen after delay
        sikkaText.postDelayed({
            startActivity(Intent(this, Login::class.java)) // Your next activity
            finish()
        }, 3000)
    }
}
