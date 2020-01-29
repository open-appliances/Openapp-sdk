package com.openapp.openappsdk

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        java.setOnClickListener { startActivity(Intent(this, com.openapp.openappsdk.java.ChooserActivity::class.java)) }

        kotlin.setOnClickListener { startActivity(Intent(this, com.openapp.openappsdk.kotlin.ChooserActivity::class.java)) }
    }
}