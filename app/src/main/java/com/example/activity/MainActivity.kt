package com.example.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set first screen layout (activity_main.xml)
        setContentView(R.layout.activity_main)

        // Delay for 5 seconds, then switch to second screen
        Handler(Looper.getMainLooper()).postDelayed({
            showSecondScreen()
        }, 5000) // 5 seconds delay
    }

    // Function to show the second screen
    private fun showSecondScreen() {
        setContentView(R.layout.secondscreen)

        val loginSuccessfulButton = findViewById<Button>(R.id.loginsuccessful)
        val goToRegisterText = findViewById<TextView>(R.id.gotoregister)

        loginSuccessfulButton.setOnClickListener {
            showFourthScreen()
        }

        goToRegisterText.setOnClickListener {
            showThirdScreen()
        }
    }

    // Function to show the third screen
    private fun showThirdScreen() {
        setContentView(R.layout.thirdscreen)

        val goToLoginText = findViewById<TextView>(R.id.gotologin)
        val registerSuccessfulButton = findViewById<Button>(R.id.registersuccessfull)

        goToLoginText.setOnClickListener {
            showSecondScreen()
        }

        registerSuccessfulButton.setOnClickListener {
            showFourthScreen()
        }
    }

    // Function to show the fourth screen
    private fun showFourthScreen() {
        setContentView(R.layout.fourthscreen)

        val sendImage = findViewById<ImageView>(R.id.chats)

        sendImage.setOnClickListener {
            showFifthScreen()
        }
    }

    // Function to show the fifth screen
    private fun showFifthScreen() {
        setContentView(R.layout.fifthscreen)

        // Find the LinearLayout with ID `Henrychat`
        val henryChatLayout = findViewById<LinearLayout>(R.id.Henrychat)

        // Navigate to sixth screen when `Henrychat` LinearLayout is clicked
        henryChatLayout.setOnClickListener {
            showSixthScreen()
        }
    }

    // Function to show the sixth screen
    private fun showSixthScreen() {
        setContentView(R.layout.sixthscreen)

        // Find the ImageView with ID `back` and set its click listener
        val backButton = findViewById<ImageView>(R.id.back)
        backButton.setOnClickListener {
            showFifthScreen() // Navigate back to the fifth screen
        }

        // Find the TextView with ID `vanishmode` and set its click listener
        val vanishModeText = findViewById<TextView>(R.id.vanishmode)
        vanishModeText.setOnClickListener {
            showSeventhScreen() // Navigate to the seventh screen
        }
    }

    // Function to show the seventh screen
    private fun showSeventhScreen() {
        setContentView(R.layout.seventhscreen)

        // Find the ImageView with ID `backtosixthscreen` and set its click listener
        val backToSixthScreen = findViewById<ImageView>(R.id.backtosixthscreen)
        backToSixthScreen.setOnClickListener {
            showSixthScreen() // Navigate back to the sixth screen
        }
    }
}
