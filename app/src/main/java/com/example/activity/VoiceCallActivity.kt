package com.umarfarooq.i210497

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.Constants

class AgoraVoiceCallActivity : AppCompatActivity() {
    private lateinit var rtcEngine: RtcEngine
    private lateinit var receiverUid: String
    private lateinit var callStatusTextView: TextView
    private lateinit var endCallButton: ImageButton
    private lateinit var muteButton: ImageButton
    private lateinit var speakerButton: ImageButton
    
    private var isMuted = false
    private var isSpeakerOn = false
    
    companion object {
        private const val APP_ID = "YOUR_AGORA_APP_ID" // Replace with your Agora App ID
        
        fun createIntent(context: Context, receiverUid: String): Intent {
            return Intent(context, AgoraVoiceCallActivity::class.java).apply {
                putExtra("receiverUid", receiverUid)
            }
        }
    }
    
    // RTC Engine event handler
    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                callStatusTextView.text = "Connected"
            }
        }
        
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                callStatusTextView.text = "Call ended"
                // End call after a delay
                callStatusTextView.postDelayed({
                    finish()
                }, 2000)
            }
        }
        
        override fun onError(err: Int) {
            runOnUiThread {
                Toast.makeText(
                    this@AgoraVoiceCallActivity,
                    "Error: $err", 
                    Toast.LENGTH_SHORT
                ).show()
                
                if (err == Constants.ERR_CONNECTION_INTERRUPTED) {
                    callStatusTextView.text = "Connection interrupted"
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_call)
        
        receiverUid = intent.getStringExtra("receiverUid") ?: return
        
        initViews()
        initializeAgoraEngine()
        setupClickListeners()
        joinChannel()
        sendCallNotification()
    }
    
    private fun initViews() {
        callStatusTextView = findViewById(R.id.callStatusTextView)
        endCallButton = findViewById(R.id.endCallButton)
        muteButton = findViewById(R.id.muteButton)
        speakerButton = findViewById(R.id.speakerButton)
        
        // Get receiver's name from database
        FirebaseDatabase.getInstance().reference.child("users").child(receiverUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("userName").getValue(String::class.java) ?: "User"
                    val userNameTextView = findViewById<TextView>(R.id.remoteUserNameTextView)
                    userNameTextView.text = userName
                    callStatusTextView.text = "Calling $userName..."
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@AgoraVoiceCallActivity,
                        "Failed to load user info",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    
    private fun initializeAgoraEngine() {
        try {
            rtcEngine = RtcEngine.create(baseContext, APP_ID, rtcEventHandler)
            rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            rtcEngine.setEnableSpeakerphone(false)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing Agora SDK: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupClickListeners() {
        endCallButton.setOnClickListener {
            endCall()
        }
        
        muteButton.setOnClickListener {
            isMuted = !isMuted
            rtcEngine.muteLocalAudioStream(isMuted)
            muteButton.setImageResource(
                if (isMuted) R.drawable.ic_mic_off
                else R.drawable.ic_mic_on
            )
        }
        
        speakerButton.setOnClickListener {
            isSpeakerOn = !isSpeakerOn
            rtcEngine.setEnableSpeakerphone(isSpeakerOn)
            speakerButton.setImageResource(
                if (isSpeakerOn) R.drawable.ic_speaker_on
                else R.drawable.ic_speaker_off
            )
        }
    }
    
    private fun joinChannel() {
        // Generate a channel name based on the sender and receiver UIDs
        val channelName = "call_${receiverUid}"
        
        // Join the channel with a null token, channel name, and optional info
        rtcEngine.joinChannel(null, channelName, null, 0)
    }
    
    private fun endCall() {
        rtcEngine.leaveChannel()
        finish()
    }
    
    private fun sendCallNotification() {
        // Send FCM notification to the receiver about the incoming call
        FirebaseDatabase.getInstance().reference.child("notifications").child(receiverUid).push()
            .setValue(
                mapOf(
                    "type" to "call",
                    "callType" to "voice",
                    "callerUid" to com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid,
                    "callerName" to com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName,
                    "timestamp" to System.currentTimeMillis()
                )
            )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        rtcEngine.leaveChannel()
        RtcEngine.destroy()
    }
}
