package com.umarfarooq.i210497

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.SurfaceView
import android.widget.FrameLayout
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
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.Constants

class AgoraVideoCallActivity : AppCompatActivity() {
    private lateinit var rtcEngine: RtcEngine
    private lateinit var receiverUid: String
    private lateinit var callStatusTextView: TextView
    private lateinit var localVideoContainer: FrameLayout
    private lateinit var remoteVideoContainer: FrameLayout
    private lateinit var endCallButton: ImageButton
    private lateinit var switchCameraButton: ImageButton
    private lateinit var muteButton: ImageButton
    private lateinit var videoOffButton: ImageButton
    
    private var isMuted = false
    private var isVideoEnabled = true
    
    companion object {
        private const val APP_ID = "YOUR_AGORA_APP_ID" // Replace with your Agora App ID
        
        fun createIntent(context: Context, receiverUid: String): Intent {
            return Intent(context, AgoraVideoCallActivity::class.java).apply {
                putExtra("receiverUid", receiverUid)
            }
        }
    }
    
    // RTC Engine event handler
    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                setupRemoteVideo(uid)
                callStatusTextView.text = "Connected"
            }
        }
        
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                removeRemoteVideo()
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
                    this@AgoraVideoCallActivity,
                    "Error: $err", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        
        receiverUid = intent.getStringExtra("receiverUid") ?: return
        
        initViews()
        initializeAgoraEngine()
        setupClickListeners()
        joinChannel()
        sendCallNotification()
    }
    
    private fun initViews() {
        callStatusTextView = findViewById(R.id.callStatusTextView)
        localVideoContainer = findViewById(R.id.localVideoContainer)
        remoteVideoContainer = findViewById(R.id.remoteVideoContainer)
        endCallButton = findViewById(R.id.endCallButton)
        switchCameraButton = findViewById(R.id.switchCameraButton)
        muteButton = findViewById(R.id.muteButton)
        videoOffButton = findViewById(R.id.videoOffButton)
        
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
                        this@AgoraVideoCallActivity,
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
            rtcEngine.enableVideo()
            
            setupLocalVideo()
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing Agora SDK: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupLocalVideo() {
        val surfaceView = SurfaceView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        localVideoContainer.addView(surfaceView)
        
        rtcEngine.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        rtcEngine.startPreview()
    }
    
    private fun setupRemoteVideo(uid: Int) {
        if (remoteVideoContainer.childCount > 0) {
            remoteVideoContainer.removeAllViews()
        }
        
        val surfaceView = SurfaceView(baseContext)
        remoteVideoContainer.addView(surfaceView)
        rtcEngine.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }
    
    private fun removeRemoteVideo() {
        remoteVideoContainer.removeAllViews()
    }
    
    private fun setupClickListeners() {
        endCallButton.setOnClickListener {
            endCall()
        }
        
        switchCameraButton.setOnClickListener {
            rtcEngine.switchCamera()
        }
        
        muteButton.setOnClickListener {
            isMuted = !isMuted
            rtcEngine.muteLocalAudioStream(isMuted)
            muteButton.setImageResource(
                if (isMuted) R.drawable.ic_mic_off
                else R.drawable.ic_mic_on
            )
        }
        
        videoOffButton.setOnClickListener {
            isVideoEnabled = !isVideoEnabled
            rtcEngine.muteLocalVideoStream(!isVideoEnabled)
            
            videoOffButton.setImageResource(
                if (!isVideoEnabled) R.drawable.ic_video_off
                else R.drawable.ic_video_on
            )
            
            localVideoContainer.visibility = if (isVideoEnabled) View.VISIBLE else View.INVISIBLE
        }
    }
    
    private fun joinChannel() {
        // Generate a channel name based on the sender and receiver UIDs
        val channelName = "video_call_${receiverUid}"
        
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
                    "callType" to "video",
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
