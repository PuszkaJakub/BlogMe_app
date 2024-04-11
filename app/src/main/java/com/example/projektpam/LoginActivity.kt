package com.example.projektpam

import android.content.Intent
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.AlignmentSpan
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity: ComponentActivity() {


    private lateinit var loginButton: Button
    private lateinit var loginTextInput: TextInputEditText
    private lateinit var passwordTextInput: TextInputEditText
    private lateinit var registerTextButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login)

        loginButton = findViewById(R.id.loginButton)
        loginTextInput = findViewById(R.id.loginText)
        passwordTextInput = findViewById(R.id.passwordText)
        registerTextButton = findViewById(R.id.textViewWithButton)
        val spannableString = SpannableString("Do not have account yet?\nRegister now")
        spannableString.setSpan(
            AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
            0,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val registerStart = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(registerStart)
                finish()
            }
        }
        spannableString.setSpan(clickableSpan, 0, spannableString.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)


        registerTextButton .text = spannableString
        registerTextButton .movementMethod = LinkMovementMethod.getInstance()


        loginButton.setOnClickListener {
            loginCheck(loginTextInput.text.toString(), passwordTextInput.text.toString())

        }


    }


    private fun loginCheck(userLogin: String, userPassword: String) = CoroutineScope(
        Dispatchers.IO).launch {
        val loginCollectionRef = Firebase.firestore.collection("users")
        var userFound = false
        try{
            val querySnapshot = loginCollectionRef
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val productReceived = document.toObject<User>()
                if (productReceived != null) {
                    if(productReceived.login == userLogin && productReceived.password == userPassword){
                        userFound = true
                        break
                    }
                }
            }

            if(userFound){
                val postsStart = Intent(this@LoginActivity, PostsActivity::class.java)
                postsStart.putExtra("userLogin", userLogin)
                startActivity(postsStart)
                finish()
            }
            else{
                launch(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Incorrect user login or password", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}