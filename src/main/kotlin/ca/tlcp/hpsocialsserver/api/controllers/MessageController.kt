package ca.tlcp.hpsocialsserver.api.controllers

import ca.tlcp.hpsocialsserver.api.WebSocketMessageResponse
import ca.tlcp.hpsocialsserver.api.getUserID
import ca.tlcp.hpsocialsserver.db.ChatThreadRepository
import ca.tlcp.hpsocialsserver.db.UserRepository
import ca.tlcp.hpsocialsserver.service.MessageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/messages")
class MessageController {

    @Autowired
    private val messageService: MessageService? = null

    @Autowired
    private val userRepository: UserRepository? = null

    @Autowired
    private val chatThreadRepository: ChatThreadRepository? = null

    /**
     * Get message history for a thread
     */
    @PostMapping("/history")
    fun getMessageHistory(
        @RequestParam threadId: Long,
        @AuthenticationPrincipal loggedInUser: Any
    ): List<WebSocketMessageResponse> {
        return try {
            val email: String = getUserID(loggedInUser)
            val user = userRepository!!.getUserByEmail(email).get()

            // Validate user is part of the thread
            val thread = chatThreadRepository!!.findById(threadId).get()
            val isThreadMember = thread.owner.id == user.id || thread.otherMembers.any { it.id == user.id }

            if (!isThreadMember) {
                return emptyList()
            }

            messageService!!.getMessageHistory(threadId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Delete a message
     */
    @PostMapping("/delete")
    fun deleteMessage(
        @RequestParam messageId: Long,
        @AuthenticationPrincipal loggedInUser: Any
    ): Boolean {
        return try {
            val email: String = getUserID(loggedInUser)
            val user = userRepository!!.getUserByEmail(email).get()

            messageService!!.deleteMessage(messageId, user.id!!)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Edit a message
     */
    @PostMapping("/edit")
    fun editMessage(
        @RequestParam messageId: Long,
        @RequestParam newContent: String,
        @AuthenticationPrincipal loggedInUser: Any
    ): Boolean {
        return try {
            val email: String = getUserID(loggedInUser)
            val user = userRepository!!.getUserByEmail(email).get()

            messageService!!.editMessage(messageId, user.id!!, newContent)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}