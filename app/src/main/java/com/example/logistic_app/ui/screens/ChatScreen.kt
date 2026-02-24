package com.example.logistic_app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.logistic_app.data.model.ChatMessage
import com.example.logistic_app.ui.theme.*
import com.example.logistic_app.ui.viewmodel.AuthViewModel
import com.example.logistic_app.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel = viewModel(),
    onBack: () -> Unit
) {
    val activeDispatch by authViewModel.activeDispatch.collectAsState()
    val personnel by authViewModel.personnel.collectAsState()
    val user by authViewModel.user.collectAsState()
    
    val messages by chatViewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(activeDispatch?.id) {
        activeDispatch?.id?.let { id ->
            chatViewModel.startListening(id)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = NavyBlue
            ) {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                "Support Chat", 
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            activeDispatch?.let { 
                                Text(
                                    "Dispatch ID: ${it.dispatchId}", 
                                    fontSize = 12.sp, 
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack, 
                                contentDescription = "Back", 
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        bottomBar = {
            if (activeDispatch != null) {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = SurfaceWhite,
                            shadowElevation = 2.dp
                        ) {
                            TextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                placeholder = { 
                                    Text(
                                        "Type a message...", 
                                        color = TextSecondary,
                                        fontSize = 15.sp
                                    ) 
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        FilledIconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    val senderName = personnel?.fullName ?: user?.displayName ?: "Personnel"
                                    chatViewModel.sendMessage(
                                        dispatchId = activeDispatch!!.id,
                                        senderId = user?.uid ?: "",
                                        senderName = senderName,
                                        text = messageText
                                    )
                                    messageText = ""
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = NavyBlue,
                                contentColor = Color.White,
                                disabledContainerColor = NavyBlue.copy(alpha = 0.5f)
                            ),
                            enabled = messageText.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.Send, 
                                contentDescription = "Send",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFEBEFF2)) // Subtle blue-gray background
        ) {
            // Subtle dot pattern overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotRadius = 1.dp.toPx()
                val spacing = 24.dp.toPx()
                val dotColor = Color.Black.copy(alpha = 0.04f)
                
                var x = spacing / 2
                while (x < size.width) {
                    var y = spacing / 2
                    while (y < size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                        y += spacing
                    }
                    x += spacing
                }
            }
            
            if (activeDispatch == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send, 
                            contentDescription = null, 
                            modifier = Modifier.size(48.dp),
                            tint = TextDisabled
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No active dispatch for chat", color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(message, currentUserId = user?.uid ?: "")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, currentUserId: String) {
    val isFromMe = message.senderId == currentUserId
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeString = message.timestamp?.toDate()?.let { timeFormat.format(it) } ?: ""

    val bubbleShape = if (isFromMe) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
    ) {
        if (!isFromMe) {
            Text(
                text = if (message.isAdmin) "Admin • ${message.senderName}" else message.senderName,
                fontSize = 12.sp,
                color = if (message.isAdmin) EmergencyRed else NavyBlue,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                fontWeight = FontWeight.Bold
            )
        }
        
        Surface(
            color = if (isFromMe) NavyBlue else SurfaceWhite,
            shape = bubbleShape,
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (message.imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(message.imageUrl),
                        contentDescription = "Attached Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = message.text,
                    color = if (isFromMe) Color.White else TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeString,
                        fontSize = 10.sp,
                        color = (if (isFromMe) Color.White else TextSecondary).copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
