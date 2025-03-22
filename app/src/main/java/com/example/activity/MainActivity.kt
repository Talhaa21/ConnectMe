package com.example.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast


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

    private fun showFourthScreen() {
        val user = FirebaseAuth.getInstance().currentUser

        // Delay execution to ensure FirebaseAuth is updated
        user?.reload()?.addOnCompleteListener {
            val updatedUser = FirebaseAuth.getInstance().currentUser
            if (updatedUser != null) {
                setContentView(R.layout.fourthscreen)

                val sendImage = findViewById<ImageView>(R.id.chats)
                sendImage.setOnClickListener { showFifthScreen() }

                val searchImage = findViewById<ImageView>(R.id.search)
                searchImage.setOnClickListener { showFourteenthScreen() }

                val profileImage = findViewById<ImageView>(R.id.profile)
                profileImage.setOnClickListener { showTenthScreen() }

                val contactsImage = findViewById<ImageView>(R.id.contacts)
                contactsImage.setOnClickListener { showEighteenthScreen() }

                val newPostImage = findViewById<ImageView>(R.id.newpost)
                newPostImage.setOnClickListener { showFifteenthScreen() }

            } else {
                Toast.makeText(this, "User not authenticated! Please sign in.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Function to show the fifth screen
    private fun showFifthScreen() {
        setContentView(R.layout.fifthscreen)

        val henryChatLayout = findViewById<LinearLayout>(R.id.Henrychat)
        henryChatLayout.setOnClickListener {
            showSixthScreen()
        }

        val backToHomeButton = findViewById<ImageView>(R.id.backtohome)
        backToHomeButton.setOnClickListener {
            showFourthScreen()
        }
    }

    // Function to show the sixth screen
    private fun showSixthScreen() {
        setContentView(R.layout.sixthscreen)

        val backButton = findViewById<ImageView>(R.id.back)
        backButton.setOnClickListener {
            showFifthScreen()
        }

        val vanishModeText = findViewById<TextView>(R.id.vanishmode)
        vanishModeText.setOnClickListener {
            showSeventhScreen()
        }

        val voiceCallImage = findViewById<ImageView>(R.id.voicecall)
        voiceCallImage.setOnClickListener {
            showEighthScreen()
        }

        val videoCallImage = findViewById<ImageView>(R.id.vediocall)
        videoCallImage.setOnClickListener {
            showNinthScreen()
        }
    }

    // Function to show the seventh screen
    private fun showSeventhScreen() {
        setContentView(R.layout.seventhscreen)

        val backToSixthScreen = findViewById<ImageView>(R.id.backtosixthscreen)
        backToSixthScreen.setOnClickListener {
            showSixthScreen()
        }
    }

    // Function to show the eighth screen (Voice Call)
    private fun showEighthScreen() {
        setContentView(R.layout.eightscreen)

        val endCallButton = findViewById<ImageView>(R.id.endcall)
        endCallButton.setOnClickListener {
            showSixthScreen()
        }
    }

    // Function to show the ninth screen (Video Call)
    private fun showNinthScreen() {
        setContentView(R.layout.ninthscreen)

        val endCallButton = findViewById<ImageView>(R.id.endcall)
        endCallButton.setOnClickListener {
            showSixthScreen()
        }
    }

    // Function to show the tenth screen (Profile)
    private fun showTenthScreen() {
        setContentView(R.layout.tenthscreen)

        val followersText = findViewById<TextView>(R.id.followers)
        followersText.setOnClickListener {
            showEleventhScreen()
        }

        val followingText = findViewById<TextView>(R.id.following)
        followingText.setOnClickListener {
            showTwelfthScreen()
        }

        val goToHome = findViewById<ImageView>(R.id.gotohome)
        goToHome.setOnClickListener {
            showFourthScreen()
        }

        val goToSearch = findViewById<ImageView>(R.id.gotosearch)
        goToSearch.setOnClickListener {
            showFourteenthScreen()
        }

        val goToContacts = findViewById<ImageView>(R.id.gotocontacts)
        goToContacts.setOnClickListener {
            showEighteenthScreen()
        }

        val newPostImage = findViewById<ImageView>(R.id.newpost)
        newPostImage.setOnClickListener {
            showFifteenthScreen()
        }

        val goToEditProfile = findViewById<ImageView>(R.id.editprofile)
        goToEditProfile.setOnClickListener {
            showThirteenthScreen()
        }
    }


    // Function to show the eleventh screen (Followers)
    private fun showEleventhScreen() {
        setContentView(R.layout.eleventhscreen)

        val goToProfile = findViewById<ImageView>(R.id.gotoprofile)
        goToProfile.setOnClickListener {
            showTenthScreen()
        }
    }

    // Function to show the twelfth screen (Following)
    private fun showTwelfthScreen() {
        setContentView(R.layout.twelvethscreen)

        val goToProfile = findViewById<ImageView>(R.id.gotoprofile)
        goToProfile.setOnClickListener {
            showTenthScreen()
        }
    }

    // Function to show the fourteenth screen (Search Page)
    private fun showFourteenthScreen() {
        setContentView(R.layout.fourteenthscreen)

        val goToHome = findViewById<ImageView>(R.id.gotohome)
        val goToProfile = findViewById<ImageView>(R.id.gotoprofile)
        val goToContacts = findViewById<ImageView>(R.id.gotocontacts)

        goToHome.setOnClickListener {
            showFourthScreen()
        }

        goToProfile.setOnClickListener {
            showTenthScreen()
        }

        goToContacts.setOnClickListener {
            showEighteenthScreen()
        }
    }


    // Function to show the eighteenth screen (Contacts Page)
    private fun showEighteenthScreen() {
        setContentView(R.layout.eighteenthscreen)

        val goToHome = findViewById<ImageView>(R.id.gotohome)

        goToHome.setOnClickListener {
            showFourthScreen()
        }
    }


    // Function to show the seventeenth screen (New Post)
    private fun showSeventeenthScreen() {
        setContentView(R.layout.seventeenthscreen)

        val goToHome = findViewById<ImageView>(R.id.gotohome)
        goToHome.setOnClickListener {
            showFourthScreen()
        }
    }

    // Function to show the fifteenth screen
    private fun showFifteenthScreen() {
        setContentView(R.layout.fifteenthscreen)

        val openCamera = findViewById<ImageView>(R.id.opencamera)
        val goToHome = findViewById<ImageView>(R.id.gotohome)

        openCamera.setOnClickListener {
            showSixteenthScreen()
        }

        goToHome.setOnClickListener {
            showFourthScreen()
        }
    }


    // Function to show the sixteenth screen
    private fun showSixteenthScreen() {
        setContentView(R.layout.sixteenthscreen)

        val goToHome = findViewById<ImageView>(R.id.gotohome)

        goToHome.setOnClickListener {
            showFourthScreen()
        }
    }

    // Function to show the thirteenth screen (Edit Profile)
    private fun showThirteenthScreen() {
        setContentView(R.layout.thirteenthscreen)

        val goToProfile = findViewById<TextView>(R.id.gotoprofile)

        goToProfile.setOnClickListener {
            showTenthScreen()
        }
    }



}
