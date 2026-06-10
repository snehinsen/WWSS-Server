package ca.tlcp.hpsocialsserver.ai

import ca.tlcp.hpsocialsserver.api.FriendRequestDetails
import ca.tlcp.hpsocialsserver.api.NotificationDetails
import ca.tlcp.hpsocialsserver.api.PostDetails
import ca.tlcp.hpsocialsserver.api.UserDetails
import ca.tlcp.hpsocialsserver.api.controllers.AIController
import ca.tlcp.hpsocialsserver.db.*
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import java.time.Instant

open class UniversalToolSet(
    protected val character: User,
    protected val characterMemoryRepository: CharacterMemoryRepository
) {

    @Tool(
        name = "addMemory",
        description = "Add a memory to your character's memory."
    )
    fun addMemory(
        @ToolParam(
            description = "What is the content of the memory."
        )
        content: String,

        @ToolParam(
            description = "A short label for the memory."
        )
        label: String
    ): String {

        val memory = CharacterMemoru(
            character = character,
            label = label,
            content = content
        )

        characterMemoryRepository.save(memory)

        return "Memory added for ${character.firstName} ${character.lastName}"
    }

    @Tool(
        name = "recallMemory",
        description = "Recall a previously stored memory."
    )
    fun recallMemory(
        @ToolParam(
            description = "The label of the memory."
        )
        label: String
    ): MemoryRetrievalResponse {

        val memory =
            characterMemoryRepository.findByCharacterAndLabel(
                character,
                label
            )

        return if (memory != null) {
            MemoryRetrievalResponse(
                content = memory.content,
                label = memory.label,
                time = memory.timestamp
            )
        } else {
            MemoryRetrievalResponse(
                content = "No memory found.",
                label = label,
                time = Instant.EPOCH
            )
        }
    }

    @Tool(
        name = "listMemories",
        description = "List all stored memory labels."
    )
    fun listMemories(): List<String> {
        return characterMemoryRepository
            .findAllByCharacter(character)
            .map { it.label }
    }

    data class MemoryRetrievalResponse(
        val content: String,
        val label: String,
        val time: Instant
    )
}

class PostTools(
    character: User,
    characterMemoryRepository: CharacterMemoryRepository,
    private val post: Post,
    private val ollamaModel: OllamaChatModel,
    private val comfyClient: ComfyClient
) : UniversalToolSet(
    character,
    characterMemoryRepository
) {

    private val chatClient =
        ChatClient.create(ollamaModel)

    @Tool(
        name = "getImage",
        description = "Generate and attach an image to the post."
    )
    fun generateImage(
        @ToolParam(description = "Detailed scene description")
        image: String,

        @ToolParam(description = "People in the image")
        people: List<String>
    ): String {

        return try {

            val path = comfyClient.generateImage(
                prompt = image,
                people = people
            )

            post.attachedMedia.add(path)

            "Image attached successfully."

        } catch (e: Exception) {

            e.printStackTrace()

            "Image generation failed."
        }
    }

    private fun enhance(prompt: String): String {
        return chatClient
            .prompt(prompt)
            .call()
            .chatResponse()
            ?.result
            ?.output
            ?.text
            ?: ""
    }
}

class TaskTools(
    character: User,
    characterMemoryRepository: CharacterMemoryRepository,
    private val notificationRepository: NotificationRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    val taskDetails: AIController.TaskCompletionDetails
) : UniversalToolSet(
    character,
    characterMemoryRepository
) {

    @Tool(
        name = "listNotifications",
        description = "List notifications relevant to the character."
    )
    fun listNotifications(): List<NotificationDetails> {

        return try {
            val result = notificationRepository
                .getNotificationsByUser(character)
                .map {
                    NotificationDetails(it)
                }
            taskDetails.addSuccessful("List notifications")
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            taskDetails.addFailed(
                label = "Failed to list notifications.",
                error = e.stackTraceToString()
            )
            emptyList()
        }
    }

    @Tool(
        name = "clearNotification",
        description = "Clear a notification by its ID. Use to ignore or complete something related to a notification."
    )
    fun clearNotification(@ToolParam(description = "the ID of the notification intended to be cleared") id: Long): Boolean {

        return try {
            notificationRepository.deleteById(id)
            taskDetails.addSuccessful("Clear notification")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            taskDetails.addFailed(
                label = "Failed to clear notification.",
                error = e.stackTraceToString()
            )
            false
        }
    }

    @Tool(
        name = "viewPost",
        description = "view a post by its ID."
    )
    fun viewPost(
        @ToolParam(
            description = "The ID of the post to view the post."
        ) pid: Long
    ): PostDetails? {

        return try {
            val post = postRepository.findById(pid).get()
            taskDetails.addSuccessful("View post successfully.")
            println(post.body)
            return PostDetails(post, userRepository)
        } catch (e: Exception) {
            e.printStackTrace()
            taskDetails.addFailed(
                label = "Failed to view post.",
                error = e.stackTraceToString()
            )
            null
        }
    }

    @Tool(
        name = "listFriendRequests",
        description = "List friend requests received."
    )
    fun listFriendRequests(): List<FriendRequestDetails> {

        return try {
            val result = friendRequestRepository
                .getAllByReceiver(character)
                .map {
                    FriendRequestDetails(
                        it,
                        userRepo = userRepository,
                    )
                }
            taskDetails.addSuccessful("List friend request ")
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            taskDetails.addFailed(
                label = "Failed to list notifications.",
                error = e.stackTraceToString()
            )
            emptyList()
        }
    }

    @Tool(
        name = "acceptFriendRequest",
        description = "accept a friend requests received."
    )
    fun acceptFriendRequests(
        @ToolParam(description = "the ID of the friend request intended to be accepted") id: Long,
    ): Boolean {

        return try {
            val request = friendRequestRepository.findById(id).get()

            val sender = request.sender
            val reciever = request.receiver
            sender.friends.add(reciever.id!!)
            reciever.friends.add(sender.id!!)
            userRepository.save(sender)
            userRepository.save(reciever)
            taskDetails.addSuccessful("Accept friend request")
            friendRequestRepository.deleteById(id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            taskDetails.addFailed(
                label = "Failed to accept friend request.",
                error = e.stackTraceToString()
            )
            false
        }
    }

    @Tool(
        name = "declineFriendRequest",
        description = "decline a friend requests received."
    )
    fun declineFriendRequests(
        @ToolParam(description = "the ID of the friend request intended to be declined") id: Long,
    ): Boolean {

        return try {
            friendRequestRepository.deleteById(id)
            taskDetails.addSuccessful("Decline friend request")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            taskDetails.addFailed(
                label = "Failed to decline friend request.",
                error = e.stackTraceToString()
            )
            false
        }
    }

    @Tool(
        name = "viewUserDetails",
        description = "view user profile info."
    )
    fun viewUserDetails(
        @ToolParam(description = "the handle of the user to view") handle: String
    ): UserDetails? {
        return try {

            println(handle)
            val details = UserDetails(userRepository.getUserByHandle(handle).get(), userRepository)
            println(details)
            taskDetails.addSuccessful("View user details")

            details
        } catch (e: Exception) {
            e.printStackTrace()
            taskDetails.addFailed(
                label = "Failed to view user details.",
                error = e.stackTraceToString()
            )
            null
        }
    }

}