package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ChatMessage

@Composable
fun ChatDialog(
    viewModel: ChatViewModel,
    onDismissRequest: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Gemini Health Assistant") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(messages) { message ->
                        Text(
                            text = (if (message.isUser) "You: " else "Bot: ") + message.text,
                            color = if (message.isUser) Color.Blue else Color.Black
                        )
                    }
                }
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ask Gemini...") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                viewModel.sendMessage(inputText)
                inputText = ""
            }) {
                Text("Send")
            }
        }
    )
}
