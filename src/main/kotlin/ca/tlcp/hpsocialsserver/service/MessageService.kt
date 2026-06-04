package ca.tlcp.hpsocialsserver.service

import ca.tlcp.hpsocialsserver.api.WebSocketMessageResponse
import ca.tlcp.hpsocialsserver.db.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MessageService {

    @Autowired
    private val dmMessageRepository: DMMessageRepository? = null

    @Autowired
    private val chatThreadRepository: ChatThreadRepository? = null

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val messagingTemplate: SimpMessagingTemplate? = null

    /**
     * Save a new message to the database
     */
    fun saveMessage(sender: User, threadId: Long, content: String, attachmentUrls: List<String> = emptyList()): DMMessage? {
        return try {
            val thread = chatThreadRepository!!.findById(threadId).get()
            val message = DMMessage(
                sender = sender,
                content = content,
                attachmentUrls = attachmentUrls,
                timestamp = Instant.now(),
                thread = thread
            )
            dmMessageRepository!!.save(message)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Broadcast a message to all subscribers of a thread
     */
    fun broadcastMessage(message: DMMessage) {
        try {
            val response = WebSocketMessageResponse(message, userRepository!!)
            messagingTemplate!!.convertAndSend("/topic/thread/${message.thread.id}", response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Broadcast typing indicator to all subscribers of a thread
     */
    fun broadcastTypingIndicator(threadId: Long, userId: Long, userHandle: String, isTyping: Boolean) {
        try {
            val typingData = mapOf(
                "threadId" to threadId,
                "userId" to userId,
                "userHandle" to userHandle,
                "isTyping" to isTyping,
                "messageType" to "TYPING"
            )
            messagingTemplate!!.convertAndSend("/topic/thread/$threadId", typingData, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Broadcast user join/leave events
     */
    fun broadcastMemberUpdate(threadId: Long, userId: Long, userHandle: String, action: String) {
        try {
            val updateData = mapOf(
                "threadId" to threadId,
                "userId" to userId,
                "userHandle" to userHandle,
                "action" to action,
                "messageType" to "MEMBER_UPDATE"
            )
            messagingTemplate!!.convertAndSend("/topic/thread/$threadId", updateData, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get message history for a thread (last 50 messages)
     */
    fun getMessageHistory(threadId: Long): List<WebSocketMessageResponse> {
        return try {
            val thread = chatThreadRepository!!.findById(threadId).get()
            dmMessageRepository!!.findTop50ByThreadOrderByTimestampDesc(thread)
                .reversed()
                .map { WebSocketMessageResponse(it, userRepository!!) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get all messages for a thread
     */
    fun getAllMessagesForThread(threadId: Long): List<WebSocketMessageResponse> {
        return try {
            val thread = chatThreadRepository!!.findById(threadId).get()
            dmMessageRepository!!.findAllByThreadOrderByTimestampAsc(thread)
                .map { WebSocketMessageResponse(it, userRepository!!) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Delete a message by ID (if user is the sender)
     */
    fun deleteMessage(messageId: Long, userId: Long): Boolean {
        return try {
            val message = dmMessageRepository!!.findById(messageId).get()
            if (message.sender.id == userId) {
                dmMessageRepository!!.deleteById(messageId)
                messagingTemplate!!.convertAndSend("/topic/thread/${message.thread.id}", mapOf(
                    "messageType" to "MESSAGE_DELETED",
                    "messageId" to messageId,
                    "threadId" to message.thread.id
                ))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Edit a message (if user is the sender)
     */
    fun editMessage(messageId: Long, userId: Long, newContent: String): Boolean {
        return try {
            val message = dmMessageRepository!!.findById(messageId).get()
            if (message.sender.id == userId) {
                message.copy(content = newContent).let {
                    dmMessageRepository!!.save(it)
                }
                messagingTemplate!!.convertAndSend("/topic/thread/${message.thread.id}", mapOf(
                    "messageType" to "MESSAGE_UPDATED",
                    "messageId" to messageId,
                    "threadId" to message.thread.id,
                    "newContent" to newContent
                ))
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

