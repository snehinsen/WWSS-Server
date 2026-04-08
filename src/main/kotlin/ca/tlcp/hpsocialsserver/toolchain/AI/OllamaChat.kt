package ca.tlcp.hpsocialsserver.toolchain.AI

import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.ollama.OllamaChatModel

class OllamaClient {


    private val model: ChatModel = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")  // Ollama server
        .modelName("bsahane/gemma3:27b")
        .build()

    fun ask(memory: MutableList<ChatMessage>): MutableList<ChatMessage> {
        val request = ChatRequest
            .builder()
            .messages(memory)
            .build()

        val response: ChatResponse = model.chat(request)
        memory.add(response.aiMessage())
        return memory
    }
}
