package com.example.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.thirdscreen)

        auth = FirebaseAuth.getInstance()

        val usernameEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val signUpButton = findViewById<Button>(R.id.registersuccessfull)

        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isNotEmpty() && password.length >= 6) {
                registerUser(username, password)
            } else {
                Toast.makeText(this, "Enter valid email and password (min 6 characters)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(username: String, password: String) {
        val fakeEmail = "$username@example.com"
        auth.createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Registration successful! UID: ${user?.uid}", Toast.LENGTH_SHORT).show()

                    // Navigate to Fourth Screen
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("showFourthScreen", true)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }


}
