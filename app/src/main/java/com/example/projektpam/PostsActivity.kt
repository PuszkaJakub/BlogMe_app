package com.example.projektpam

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject


class PostsActivity:  ComponentActivity(), DoScreenActions {
    private val postsCollectionRef = Firebase.firestore.collection("messages")
    private lateinit var userLogin: String
    private val otherUsers = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userLogin = intent.getStringExtra("userLogin").toString()

        val posts = mutableListOf<Post>()
        val pwPosts = mutableListOf<Post>()

        syncPosts(posts, pwPosts)

        setContent {
            DoScreen(applicationContext, posts, pwPosts, postsCollectionRef, otherUsers, userLogin, this)
        }
    }
    override fun finishActivity() {
        finish()
    }

    private fun syncPosts(posts: MutableList<Post>, pwPosts: MutableList<Post>){
        postsCollectionRef.orderBy("time", Query.Direction.DESCENDING).addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            querySnapshot?.let{
                val prevPostsStatus = posts.size
                val prevPwPostsStatus = pwPosts.size

                posts.clear()
                pwPosts.clear()
                for (document in it) {
                    val productReceived = document.toObject<Post>()
                    if(productReceived.recipient == "All users"){
                        posts.add(productReceived)
                    }
                    else if(productReceived.recipient == userLogin){
                        pwPosts.add(productReceived)
                    }


                }
                if(prevPostsStatus != 0 && posts.size > prevPostsStatus){
                    sendNotification(applicationContext, "public")
                }
                if(prevPwPostsStatus != 0 && pwPosts.size > prevPwPostsStatus){
                    sendNotification(applicationContext, "private")
                }

                setContent {
                    DoScreen(applicationContext, posts, pwPosts, postsCollectionRef, otherUsers, userLogin, this)
                }
            }
        }
    }


}
