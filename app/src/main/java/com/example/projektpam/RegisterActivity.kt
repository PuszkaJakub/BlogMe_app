package com.example.projektpam

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

class RegisterActivity : ComponentActivity() {

    private lateinit var registerButton: Button
    private lateinit var loginTextInput: TextInputEditText
    private lateinit var passwordTextInput: TextInputEditText
    private lateinit var passwordTextInput2: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_register)

        registerButton = findViewById(R.id.registerButton)
        loginTextInput = findViewById(R.id.loginText)
        passwordTextInput = findViewById(R.id.passwordText)
        passwordTextInput2 = findViewById(R.id.passwordText2)

        registerButton.setOnClickListener{
            if(passwordTextInput.text.toString() == passwordTextInput2.text.toString()){
                registerNewUser(loginTextInput.text.toString(), passwordTextInput.text.toString())


            }else{
                Toast.makeText(this@RegisterActivity, "Passwords must be identical\nTry again", Toast.LENGTH_SHORT).show()
                passwordTextInput.text = null
                passwordTextInput2.text = null
            }
        }

    }

    private fun registerNewUser(newUserLogin: String, newUserPassword: String) = CoroutineScope(
        Dispatchers.IO).launch {
        val loginCollectionRef = Firebase.firestore.collection("users")
        var usernameTaken = false
        try{
            val querySnapshot = loginCollectionRef
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val productReceived = document.toObject<User>()
                if (productReceived != null) {
                    if(productReceived.login == newUserLogin){
                        usernameTaken = true
                        break
                    }
                }
            }

            if(usernameTaken){
                launch(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Username is already taken", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                val newUser = User(
                    newUserLogin,
                    newUserPassword
                )
                addNewUser(newUser)
                launch(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Please log in", Toast.LENGTH_SHORT).show()

                }
                val loginStart = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(loginStart)
                finish()
            }

        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@RegisterActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addNewUser(user: User) = CoroutineScope(Dispatchers.IO).launch {
        val loginCollectionRef = Firebase.firestore.collection("users")
        try{
            loginCollectionRef.add(user).await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@RegisterActivity, "Account registered", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@RegisterActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}