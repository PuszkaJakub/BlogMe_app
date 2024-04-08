package com.example.projektpam

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color.parseColor
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.drawerlayout.widget.*
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null,
    val tint: Color? = null
)

class PostsActivity:  ComponentActivity() {
    private val postsCollectionRef = Firebase.firestore.collection("messages")
    private lateinit var userLogin: String
    private val otherUsers = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userLogin = intent.getStringExtra("userLogin").toString()

        val posts = mutableListOf<Post>()
        val pwPosts = mutableListOf<Post>()

        subscribeToRealTimeUpdates(posts, pwPosts)

        setContent {
          DoScreen(posts, pwPosts)
        }
    }


    // Otrzymuj posty
    private fun subscribeToRealTimeUpdates(posts: MutableList<Post>, pwPosts: MutableList<Post>){
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
                    sendNotification("public")
                }
                if(prevPwPostsStatus != 0 && pwPosts.size > prevPwPostsStatus){
                    sendNotification("private")
                }

                setContent {
                    DoScreen(posts, pwPosts)
                }
            }
        }
    }

    // Dodaj post
    private fun sendPost(post: Post) = CoroutineScope(Dispatchers.IO).launch {
        try{
            postsCollectionRef.add(post).await()
            withContext(Dispatchers.Main){
                Toast.makeText(this@PostsActivity, "Post sent", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@PostsActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Usun post
    private fun deletePost(post: Post) = CoroutineScope(Dispatchers.IO).launch{
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
                        Toast.makeText(this@PostsActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else{
            withContext(Dispatchers.Main){
                Toast.makeText(this@PostsActivity, "No product matched the query",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Zaladuj kontakty do listy
    private fun loadContacts(otherUsers: MutableList<String>) = CoroutineScope(
        Dispatchers.IO).launch {
        val loginCollectionRef = Firebase.firestore.collection("users")


        otherUsers.clear()
        otherUsers.add("All users")
        try{
            var querySnapshot = loginCollectionRef
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
                Toast.makeText(this@PostsActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DoScreen(posts: MutableList<Post>, pwPosts: MutableList<Post>) {
        val openAddNewPostDialog = remember { mutableStateOf(false) }
        var currentList: MutableList<Post>

        val items = listOf(
                NavigationItem(
                    title = "All",
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    badgeCount = posts.size
                ),
                NavigationItem(
                    title = "Private messages",
                    selectedIcon = Icons.Filled.Email,
                    unselectedIcon = Icons.Outlined.Email,
                    badgeCount = pwPosts.size
                ),
                NavigationItem(
                    title = "Log Out",
                    selectedIcon = Icons.Filled.ExitToApp,
                    unselectedIcon = Icons.Outlined.ExitToApp,
                )
        )

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            var selectedItemIndex by rememberSaveable {
                mutableStateOf(0)
            }
            ModalNavigationDrawer(
                drawerContent = {
                    ModalDrawerSheet {
                        Spacer(modifier = Modifier.height(16.dp))
                        items.forEachIndexed { index, item ->
                            NavigationDrawerItem(
                                label = {
                                    Text(text = item.title)
                                },
                                selected = index == selectedItemIndex,
                                onClick = {
                                    selectedItemIndex = index
                                    when(selectedItemIndex){
                                        2 -> finish()
                                    }
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (index == selectedItemIndex) {
                                            item.selectedIcon
                                        } else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                },
                                badge = {
                                    item.badgeCount?.let {
                                        Text(text = item.badgeCount.toString())
                                    }
                                },
                                modifier = Modifier
                                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                            )

                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                },
                drawerState = drawerState
            ) {

                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.mediumTopAppBarColors(
                                containerColor = Color(parseColor("#589e57")),
                                titleContentColor = Color.Black,
                            ),
                            title = {
                                Column {
                                    Text("Blog.me")
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open menu"
                                    )
                                }
                            }
                        )
                    },

                    // Przycisk na dole do dodania postu
                    floatingActionButton = {
                        IconButton(
                            onClick = {
                                loadContacts(otherUsers)
                                openAddNewPostDialog.value = true
                            },
                            modifier = Modifier
                                .background(
                                    Color(parseColor("#589e57")),
                                    shape = RoundedCornerShape(44.dp)
                                )
                                .size(56.dp)
                        ){
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add post",
                                tint = Color.White
                            )

                        }
                    }
                ){ padding ->
                    LazyColumn(
                        contentPadding = padding,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ){
                        currentList = if(selectedItemIndex == 0) posts else pwPosts
                        items(currentList.size){
                            val zoneId = ZoneId.of("UTC+1")
                            val instant = Instant.ofEpochMilli(currentList[it].time.seconds * 1000)
                            val formatter = DateTimeFormatter.ofPattern("dd MMMM HH:mm").withLocale(Locale.getDefault()).withZone(zoneId)
                            val formattedDate = formatter.format(instant)

                            Row(
                                modifier = Modifier.padding(10.dp)
                            ){
                                Column(
                                    modifier = Modifier.weight(1f)
                                ){

                                    Text(
                                        text = currentList[it].author,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(parseColor("#174bcf"))
                                    )
                                    Text(
                                        text = formattedDate,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = currentList[it].content,
                                        fontSize = 24.sp
                                    )
                                }

                                if(currentList[it].author == userLogin){
                                    IconButton(
                                        onClick = {
                                            deletePost(currentList[it])

                                        },
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .size(36.dp)
                                            .padding(8.dp),
                                    ){
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Gray
                                        )

                                    }
                                }
                                else{
                                    IconButton(
                                        onClick = {
                                            otherUsers.clear()
                                            otherUsers.add(currentList[it].author)
                                            openAddNewPostDialog.value = true
                                        },
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .size(36.dp)
                                            .padding(8.dp),
                                    ){
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Reply",
                                            tint = Color.Gray
                                        )
                                    }
                                }

                            }
                        }
                    }
                }
            }
            if(openAddNewPostDialog.value){


                var expanded by remember { mutableStateOf(false) }
                var selectedRecipient by remember { mutableStateOf(otherUsers[0]) }

                var newPostContent by remember { mutableStateOf("") }


                AlertDialog(
                    onDismissRequest = { openAddNewPostDialog.value = false },
                    title = { Text(text = "Add new post") },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Text(
                                text = "Select reciepment",
                                fontSize = 10.sp
                            )

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                TextField(
                                    value = selectedRecipient,
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = {
                                        Text(text = "Select user")
                                    },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    otherUsers.forEach { 
                                        DropdownMenuItem(
                                            text = { Text(text = it) },
                                            onClick = {
                                                selectedRecipient = it
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Write content",
                                fontSize = 10.sp
                            )

                            TextField(
                                value = newPostContent,
                                onValueChange = {
                                    newPostContent = it
                                },
                                placeholder = {
                                    Text(text = "Write your thoughts")
                                }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val newPost = Post(
                                    userLogin,
                                    newPostContent,
                                    Timestamp.now(),
                                    selectedRecipient)
                                sendPost(newPost)
                                newPostContent = ""
                                openAddNewPostDialog.value = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(parseColor("#589e57"))
                            )
                        ) {
                            Text (text = "ADD")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openAddNewPostDialog.value = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(parseColor("#589e57"))
                            )
                        ){
                            Text (text = "CANCEL")
                        }
                    }
                )
            }
        }
    }


    private fun sendNotification(notificationType: String){
        val channelID = "10000"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val bitmapLargeIcon: Bitmap = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.logo)

        val builder = NotificationCompat.Builder(applicationContext, channelID)
            .setContentTitle("Blog.me")
            .setSmallIcon(R.drawable.logo)
            .setLargeIcon(bitmapLargeIcon)

        if(notificationType == "public"){
            builder
                .setContentText("New public post")
                .setStyle(NotificationCompat.BigTextStyle().bigText("You just recieved a new public post"))
        }
        else{
            builder
                .setContentText("New private meesage")
                .setStyle(NotificationCompat.BigTextStyle().bigText("You just recieved a new private message"))
        }


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelID, "Blog.me", NotificationManager.IMPORTANCE_DEFAULT).apply{
                description = "New Post recieved"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)


            channel.enableVibration(true)

            builder.setChannelId(channelID)
        }

        val notification = builder.build()
        notificationManager.notify(1000, notification)
    }

}
