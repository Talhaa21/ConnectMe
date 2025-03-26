package com.umarfarooq.i210497

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var imageButton: ImageButton
    private lateinit var callButton: ImageButton
    private lateinit var videoCallButton: ImageButton
    private lateinit var vanishModeSwitch: ImageButton
    
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = ArrayList<Message>()
    
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var chatReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    
    private lateinit var receiverUid: String
    private lateinit var senderUid: String
    private var isVanishModeEnabled = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        receiverUid = intent.getStringExtra("receiverUid") ?: return
        senderUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        initViews()
        setupFirebase()
        setupRecyclerView()
        setupClickListeners()
        setupScreenshotDetection()
    }
    
    private fun initViews() {
        messageRecyclerView = findViewById(R.id.messageRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        imageButton = findViewById(R.id.imageButton)
        callButton = findViewById(R.id.callButton)
        videoCallButton = findViewById(R.id.videoCallButton)
        vanishModeSwitch = findViewById(R.id.vanishModeSwitch)
        
        // Set the receiver's name in the action bar
        val receiverNameTextView = findViewById<TextView>(R.id.receiverNameTextView)
        val onlineStatusTextView = findViewById<TextView>(R.id.onlineStatusTextView)
        
        // Get receiver's info from database
        FirebaseDatabase.getInstance().reference.child("users").child(receiverUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("userName").getValue(String::class.java) ?: "User"
                    val isOnline = snapshot.child("online").getValue(Boolean::class.java) ?: false
                    
                    receiverNameTextView.text = userName
                    onlineStatusTextView.text = if (isOnline) "Online" else "Offline"
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Failed to load user info", Toast.LENGTH_SHORT).show()
                }
            })
    }
    
    private fun setupFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance()
        
        // Create a unique chat ID for this conversation
        val chatId = if (senderUid < receiverUid) {
            "$senderUid-$receiverUid"
        } else {
            "$receiverUid-$senderUid"
        }
        
        chatReference = firebaseDatabase.reference.child("chats").child(chatId)
        storageReference = FirebaseStorage.getInstance().reference.child("chat_images").child(chatId)
        
        // Listen for messages
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    message?.let {
                        // Handle vanish mode for seen messages
                        if (isVanishModeEnabled && it.senderId != senderUid && it.seen) {
                            // Don't add this message as it should vanish
                        } else {
                            messageList.add(it)
                        }
                    }
                }
                
                chatAdapter.notifyDataSetChanged()
                messageRecyclerView.scrollToPosition(messageList.size - 1)
                
                // Mark received messages as seen
                markMessagesAsSeen()
            }
            
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Failed to load messages", Toast.LENGTH_SHORT).show()
            }
        })
    }
    
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(this, messageList, senderUid) { message, position ->
            // Check if message is within 5 minutes
            val currentTime = System.currentTimeMillis()
            val messageTime = message.timestamp
            val timeDiff = currentTime - messageTime
            val fiveMinutesInMillis = 5 * 60 * 1000
            
            if (message.senderId == senderUid && timeDiff <= fiveMinutesInMillis) {
                // Show edit/delete options
                showMessageOptions(message, position)
            }
        }
        
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageRecyclerView.adapter = chatAdapter
    }
    
    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText, "text")
                messageEditText.text.clear()
            }
        }
        
        imageButton.setOnClickListener {
            // Launch gallery intent
            // Implement image picking and uploading
            // For brevity, this is a placeholder
            Toast.makeText(this, "Image sharing to be implemented", Toast.LENGTH_SHORT).show()
        }
        
        callButton.setOnClickListener {
            // Launch voice call (Agora integration)
            startActivity(
                AgoraVoiceCallActivity.createIntent(
                    this,
                    receiverUid
                )
            )
        }
        
        videoCallButton.setOnClickListener {
            // Launch video call (Agora integration)
            startActivity(
                AgoraVideoCallActivity.createIntent(
                    this,
                    receiverUid
                )
            )
        }
        
        vanishModeSwitch.setOnClickListener {
            isVanishModeEnabled = !isVanishModeEnabled
            vanishModeSwitch.setImageResource(
                if (isVanishModeEnabled) R.drawable.ic_vanish_mode_on 
                else R.drawable.ic_vanish_mode_off
            )
            
            Toast.makeText(
                this,
                if (isVanishModeEnabled) "Vanish mode enabled" else "Vanish mode disabled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun sendMessage(content: String, type: String) {
        val timestamp = System.currentTimeMillis()
        val messageId = chatReference.push().key ?: return
        
        val message = Message(
            messageId = messageId,
            senderId = senderUid,
            receiverId = receiverUid,
            content = content,
            type = type,
            timestamp = timestamp,
            seen = false,
            vanishMode = isVanishModeEnabled
        )
        
        chatReference.child(messageId).setValue(message).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            } else {
                // Send notification to receiver
                sendNotification(receiverUid, "New message from ${FirebaseAuth.getInstance().currentUser?.displayName}")
            }
        }
    }
    
    private fun markMessagesAsSeen() {
        for (message in messageList) {
            if (message.receiverId == senderUid && !message.seen) {
                chatReference.child(message.messageId).child("seen").setValue(true)
            }
        }
    }
    
    private fun showMessageOptions(message: Message, position: Int) {
        val options = arrayOf("Edit", "Delete")
        
        AlertDialog.Builder(this)
            .setTitle("Message Options")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditDialog(message)
                    1 -> deleteMessage(message)
                }
                dialog.dismiss()
            }
            .create()
            .show()
    }
    
    private fun showEditDialog(message: Message) {
        val editText = EditText(this).apply {
            setText(message.content)
        }
        
        AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                val newContent = editText.text.toString().trim()
                if (newContent.isNotEmpty()) {
                    chatReference.child(message.messageId).child("content").setValue(newContent)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    
    private fun deleteMessage(message: Message) {
        chatReference.child(message.messageId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete message", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun setupScreenshotDetection() {
        // This is a placeholder for screenshot detection
        // A complete implementation would involve platform-specific code
        // For Android, you'd need to observe media store changes
        
        // This simulates detection for demonstration purposes
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // In a real app, this would be triggered by actual screenshot detection
            sendNotification(receiverUid, "${FirebaseAuth.getInstance().currentUser?.displayName} took a screenshot of the chat")
        }, 20000) // Simulate after 20 seconds
    }
    
    private fun sendNotification(userId: String, message: String) {
        // Placeholder for FCM notification
        // In a complete implementation, this would send a request to a server
        // that would use FCM to deliver the notification
        
        // Save notification to Firebase
        firebaseDatabase.reference.child("notifications").child(userId).push()
            .setValue(
                mapOf(
                    "message" to message,
                    "timestamp" to System.currentTimeMillis(),
                    "read" to false
                )
            )
    }
    
    override fun onPause() {
        super.onPause()
        // If vanish mode is on, remove all seen messages when leaving
        if (isVanishModeEnabled) {
            for (message in messageList) {
                if (message.seen && message.vanishMode) {
                    chatReference.child(message.messageId).removeValue()
                }
            }
        }
    }
}

// Message data class
data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val type: String = "text", // text, image, etc.
    val timestamp: Long = 0,
    val seen: Boolean = false,
    val vanishMode: Boolean = false
)

// ChatAdapter.kt - Adapter for the RecyclerView
class ChatAdapter(
    private val context: android.content.Context,
    private val messageList: List<Message>,
    private val currentUserId: String,
    private val onMessageLongClick: (Message, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    private val SENT_MESSAGE = 0
    private val RECEIVED_MESSAGE = 1
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SENT_MESSAGE) {
            val view = android.view.LayoutInflater.from(context).inflate(R.layout.item_sent_message, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = android.view.LayoutInflater.from(context).inflate(R.layout.item_received_message, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        
        if (holder.itemViewType == SENT_MESSAGE) {
            (holder as SentMessageViewHolder).bind(message)
        } else {
            (holder as ReceivedMessageViewHolder).bind(message)
        }
        
        holder.itemView.setOnLongClickListener {
            onMessageLongClick(message, position)
            true
        }
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderId == currentUserId) SENT_MESSAGE else RECEIVED_MESSAGE
    }
    
    override fun getItemCount(): Int = messageList.size
    
    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.sentMessageText)
        private val timeText: TextView = itemView.findViewById(R.id.sentMessageTime)
        private val seenIndicator: ImageView = itemView.findViewById(R.id.seenIndicator)
        
        fun bind(message: Message) {
            messageText.text = message.content
            
            // Format timestamp to readable time
            val date = Date(message.timestamp)
            val format = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            timeText.text = format.format(date)
            
            // Show seen indicator
            seenIndicator.visibility = if (message.seen) View.VISIBLE else View.GONE
        }
    }
    
    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.receivedMessageText)
        private val timeText: TextView = itemView.findViewById(R.id.receivedMessageTime)
        
        fun bind(message: Message) {
            messageText.text = message.content
            
            // Format timestamp to readable time
            val date = Date(message.timestamp)
            val format = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
            timeText.text = format.format(date)
        }
    }
}
