package com.example.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.*
import android.widget.EditText


// Message data class remains the same
data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isVanishMode: Boolean = false,
    var isDeleted: Boolean = false
)


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            showSecondScreen()
        }, 5000) // 5 seconds delay
    }

    // Function to show the second screen
    private fun showSecondScreen() {
        setContentView(R.layout.secondscreen)

        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginsuccessful)
        val goToRegisterText = findViewById<TextView>(R.id.gotoregister)

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            loginUser(email, password)
        }

        goToRegisterText.setOnClickListener {
            showThirdScreen()
        }
    }

    // Function to show the third screen
    private fun showThirdScreen() {
        setContentView(R.layout.thirdscreen)

        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val registerButton = findViewById<Button>(R.id.registersuccessfull)
        val goToLoginText = findViewById<TextView>(R.id.gotologin)

        registerButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            registerUser(email, password)
        }

        goToLoginText.setOnClickListener {
            showSecondScreen()
        }
    }

    private fun registerUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    showFourthScreen()
                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginUser(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    showFourthScreen()
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Function to show the fourth screen
    private fun showFourthScreen() {
        setContentView(R.layout.fourthscreen)

        val sendImage = findViewById<ImageView>(R.id.chats)
        sendImage.setOnClickListener {
            showFifthScreen()
        }

        val searchImage = findViewById<ImageView>(R.id.search)
        searchImage.setOnClickListener {
            showFourteenthScreen()
        }

        val profileImage = findViewById<ImageView>(R.id.profile)
        profileImage.setOnClickListener {
            showTenthScreen()
        }

        val contactsImage = findViewById<ImageView>(R.id.contacts)
        contactsImage.setOnClickListener {
            showEighteenthScreen()
        }

        val newPostImage = findViewById<ImageView>(R.id.newpost)
        newPostImage.setOnClickListener {
            showFifteenthScreen()
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

        openCamera.setOnClickListener {
            showSixteenthScreen()
        }

        setContentView(R.layout.fifteenthscreen)

        val selectImageButton = findViewById<Button>(R.id.selectImageButton)
        val imageView = findViewById<ImageView>(R.id.selectedImageView)
        val goToHome = findViewById<ImageView>(R.id.gotohome)

        selectImageButton.setOnClickListener {
            openGallery()
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


    private fun loadStories() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().reference.child("Stories").child(userId)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val storiesList = mutableListOf<String>()
                for (storySnapshot in snapshot.children) {
                    val imageUrl = storySnapshot.child("imageUrl").value.toString()
                    storiesList.add(imageUrl)
                }
                // Update RecyclerView with storiesList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private val PICK_IMAGE_REQUEST = 1
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            val imageView = findViewById<ImageView>(R.id.selectedImageView)
            imageView.setImageURI(selectedImageUri)
        }
    }

    override fun onStop() {
        super.onStop()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference("users").child(userId).child("status")
                .setValue("offline")
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference("users").child(userId).child("status")
                .setValue("online")
        }
    }



}
