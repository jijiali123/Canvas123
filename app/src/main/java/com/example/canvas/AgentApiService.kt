package com.example.canvas

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class ChatMessage(val role: String, val content: String)
data class ChatRequest(val messages: List<ChatMessage>)
data class ChatResponse(val response: String)

interface AgentApiService {
    @POST("api/chat")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>
}
