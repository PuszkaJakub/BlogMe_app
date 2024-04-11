package com.example.projektpam


import android.content.Context
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoScreen(context: Context, posts: MutableList<Post>, pwPosts: MutableList<Post>, postsCollectionRef: CollectionReference, otherUsers: MutableList<String>, userLogin: String, actions: DoScreenActions) {
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
                                    2 -> actions.finishActivity()
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
                            containerColor = Color(android.graphics.Color.parseColor("#589e57")),
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

                floatingActionButton = {
                    IconButton(
                        onClick = {
                            loadContacts(context, userLogin, otherUsers)
                            openAddNewPostDialog.value = true
                        },
                        modifier = Modifier
                            .background(
                                Color(android.graphics.Color.parseColor("#589e57")),
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
                        val formatter = DateTimeFormatter.ofPattern("dd MMMM HH:mm").withLocale(
                            Locale.getDefault()).withZone(zoneId)
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
                                    color = Color(android.graphics.Color.parseColor("#174bcf"))
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
                                        deletePost(context, postsCollectionRef, currentList[it], userLogin)

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
                            sendPost(context, postsCollectionRef, newPost)
                            newPostContent = ""
                            openAddNewPostDialog.value = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(android.graphics.Color.parseColor("#589e57"))
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
                            containerColor = Color(android.graphics.Color.parseColor("#589e57"))
                        )
                    ){
                        Text (text = "CANCEL")
                    }
                }
            )
        }
    }
}