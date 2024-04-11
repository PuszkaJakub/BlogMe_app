package com.example.projektpam

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

fun deletePost(context: Context, postsCollectionRef: CollectionReference, post: Post, userLogin: String) = CoroutineScope(Dispatchers.IO).launch{
    val productQuery = postsCollectionRef
        .whereEqualTo("author", userLogin)
        .whereEqualTo("content", post.content)
        .whereEqualTo("time", post.time)
        .get()
        .await()
    if(productQuery.documents.isNotEmpty()){
        for(document in productQuery){
            try{
                postsCollectionRef.document(document.id).delete().await()
            } catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    else{
        withContext(Dispatchers.Main){
            Toast.makeText(context, "No product matched the query",
                Toast.LENGTH_SHORT).show()
        }
    }
}