package com.example.projektpam

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

fun sendPost(context: Context, postsCollectionRef: CollectionReference, post: Post) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            postsCollectionRef.add(post).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Post sent", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
