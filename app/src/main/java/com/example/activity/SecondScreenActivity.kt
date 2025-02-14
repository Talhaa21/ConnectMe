package com.example.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SecondScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.secondscreen) // This should match your secondscreen.xml file
    }
}
