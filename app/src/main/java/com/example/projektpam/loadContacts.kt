package com.example.projektpam

import android.content.Context
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

 fun loadContacts(context: Context, userLogin: String, otherUsers: MutableList<String>) = CoroutineScope(
    Dispatchers.IO).launch {
    val loginCollectionRef = Firebase.firestore.collection("users")

    otherUsers.clear()
    otherUsers.add("All users")
    try{
        val querySnapshot = loginCollectionRef
            .get()
            .await()

        for (document in querySnapshot.documents) {
            val productReceived = document.toObject<User>()
            if (productReceived != null) {
                if(productReceived.login != userLogin){
                    otherUsers.add(productReceived.login)
                }
            }
        }

    } catch (e: Exception){
        withContext(Dispatchers.Main){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }
}