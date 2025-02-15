package com.example.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Change the layout to secondscreen.xml
        setContentView(R.layout.seventeenthscreen)
    }
}
