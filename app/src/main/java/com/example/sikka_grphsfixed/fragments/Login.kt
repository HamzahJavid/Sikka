package com.example.sikka_grphsfixed.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sikka_grphsfixed.MainActivity
import com.example.sikka_grphsfixed.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerNowTextView: TextView
    private var database= FirebaseDatabase.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_in) // Make sure your layout filename is login.xml

        FirebaseApp.initializeApp(this)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()



        // Initialize Views
        usernameEditText = findViewById(R.id.email_login)
        passwordEditText = findViewById(R.id.password_login)
        loginButton = findViewById(R.id.login_button)
        registerNowTextView = findViewById(R.id.register_now_text)

        // Set Login Button Click Listener
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(username, password)
            }
        }

        // Set Register Text Click Listener
        registerNowTextView.setOnClickListener {
            // Navigate to Signup screen
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(username: String, password: String) {
        // First, get the user's email from Firebase Database by username
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val uid=auth.currentUser?.uid
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("UserID",uid)
                    Log.d("UserID_Debug", "UserID: $uid")

                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
