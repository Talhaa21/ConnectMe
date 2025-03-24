package com.example.activity


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage


class SecondActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fifteenthscreen) // Ensure the correct layout file

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                findViewById<ImageView>(R.id.addstory).setImageURI(uri)
            }
        }


        findViewById<Button>(R.id.upload).setOnClickListener {
            if (selectedImageUri != null) {
                uploadStoryToFirebase(selectedImageUri!!)
            } else {
                Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadStoryToFirebase(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val storageRef = FirebaseStorage.getInstance().reference.child("stories/$userId/${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveStoryDataToDatabase(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveStoryDataToDatabase(imageUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().reference.child("Stories").child(userId)

        val storyId = databaseRef.push().key ?: return
        val storyData = mapOf(
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis()
        )

        databaseRef.child(storyId).setValue(storyData).addOnSuccessListener {
            Toast.makeText(this, "Story uploaded!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload story!", Toast.LENGTH_SHORT).show()
        }
    }
}

