package com.example.sikka_grphsfixed.fragments
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sikka_grphsfixed.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        usernameEditText = findViewById(R.id.username)
        signUpButton = findViewById(R.id.signup_button)

        // Set Button Click Listener
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password, username)
            }
        }
    }

    private fun registerUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User created successfully
                    val userId = auth.currentUser?.uid

                    val database = FirebaseDatabase.getInstance()
                    val usersRef = database.getReference("users")

                    val userMap = HashMap<String, Any>()
                    userMap["username"] = username
                    userMap["email"] = email

                    if (userId != null) {
                        usersRef.child(userId).setValue(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                // Optionally: move to another screen
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
