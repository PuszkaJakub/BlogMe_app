package com.example.projektpam

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {

    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_entry)

        loginButton = findViewById(R.id.entryLoginButton)
        loginButton.setOnClickListener {
            val loginStart = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(loginStart)
        }

        registerButton = findViewById(R.id.entryRegisterButton)
        registerButton.setOnClickListener {
            val registerStart = Intent(this@MainActivity, RegisterActivity::class.java)
            startActivity(registerStart)
        }
    }
}