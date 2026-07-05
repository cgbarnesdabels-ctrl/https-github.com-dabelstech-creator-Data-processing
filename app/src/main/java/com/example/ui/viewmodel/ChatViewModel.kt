package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemini.Content
import com.example.gemini.GenerateContentRequest
import com.example.gemini.Part
import com.example.gemini.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.BuildConfig

class ChatViewModel : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun sendMessage(text: String) {
        val userMessage = ChatMessage(text, true)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                val request = GenerateContentRequest(
                    contents = listOf(Content(
                        parts = listOf(Part(text = text))
                    ))
                )
                val response = RetrofitClient.service.generateContent(apiKey, request)
                val botText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response"
                val botMessage = ChatMessage(botText, false)
                _messages.value = _messages.value + botMessage
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Error: ${e.message}", false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)
