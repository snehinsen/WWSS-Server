package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.WebSocketMessageRequest
import ca.tlcp.hpsocialsserver.api.getUserID
import ca.tlcp.hpsocialsserver.db.ChatThreadRepository
import ca.tlcp.hpsocialsserver.db.UserRepository
import ca.tlcp.hpsocialsserver.service.MessageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class WebSocketController {

    @Autowired
    private val messageService: MessageService? = null

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val chatThreadRepository: ChatThreadRepository? = null

    @Autowired
    private val messagingTemplate: SimpMessagingTemplate? = null

    /**
     * Handle incoming messages from clients
     * Client sends message to /app/sendMessage
     * Message is broadcast to all subscribers of /topic/thread/{threadId}
     */
    @MessageMapping("/sendMessage")
    fun handleMessage(
        @Payload request: WebSocketMessageRequest,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        return try {
            val email: String = getUserID(headerAccessor.user!!)
            val user = userRepository!!.getUserByEmail(email).get()

            // Validate user is part of the thread
            val thread = chatThreadRepository!!.findById(request.threadId).get()
            val isThreadMember = thread.owner.id == user.id || thread.otherMembers.any { it.id == user.id }

            // This should only trigger if there is a bug in the front end UI. Otherwise never.
            if (!isThreadMember) {
                messagingTemplate!!.convertAndSendToUser(
                    email,
                    "/queue/errors",
                    mapOf("error" to "You are not a member of this thread")
                )
                return
            }

            // Save message to database
            val savedMessage = messageService!!.saveMessage(
                sender = user,
                threadId = request.threadId,
                content = request.content,
                attachmentUrls = request.attachmentUrls
            )

            if (savedMessage != null) {
                // Broadcast to all subscribers
                messageService!!.broadcastMessage(savedMessage)
            } else {
                println("Failed to send message")
                messagingTemplate!!.convertAndSendToUser(
                    email,
                    "/queue/errors",
                    mapOf("error" to "Failed to save message")
                )
            }
        } catch (e: Exception) {
            println("Failed to send message")
            messagingTemplate!!.convertAndSend(
                "/topic/thread/${request.threadId}",
                mapOf("error" to "Failed to save message"),
                null,
                null
            )
            e.printStackTrace()
        }
    }

    /**
     * Handle typing indicators
     * Client sends to /app/typing
     * Broadcast to all subscribers of /topic/thread/{threadId}
     */
    @MessageMapping("/typing")
    fun handleTypingIndicator(
        @Payload typingData: Map<String, Any>,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        return try {
            val email: String = getUserID(headerAccessor.user!!)
            val user = userRepository!!.getUserByEmail(email).get()
            val threadId = (typingData["threadId"] as Number).toLong()
            val isTyping = typingData["isTyping"] as Boolean

            messageService!!.broadcastTypingIndicator(
                threadId,
                user.id!!,
                user.handle!!,
                isTyping
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

