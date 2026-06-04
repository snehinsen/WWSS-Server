package ca.tlcp.hpsocialsserver.ai

import ca.tlcp.hpsocialsserver.api.controllers.AIController
import ca.tlcp.hpsocialsserver.db.*
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.stereotype.Component

@Component
class ToolFactory(
    private val ollamaModel: OllamaChatModel,
    private val comfyClient: ComfyClient,
    private val characterMemoryRepository: CharacterMemoryRepository,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val friendRequestRepository: FriendRequestRepository
) {

    fun createPostTools(
        character: User,
        post: Post
    ): PostTools {

        return PostTools(
            character = character,
            characterMemoryRepository = characterMemoryRepository,
            post = post,
            ollamaModel = ollamaModel,
            comfyClient = comfyClient
        )
    }

    fun createTaskTools(
        character: User,
        taskDetails: AIController.TaskCompletionDetails
    ): TaskTools {

        return TaskTools(
            character = character,
            characterMemoryRepository = characterMemoryRepository,
            notificationRepository = notificationRepository,
            taskDetails = taskDetails,
            friendRequestRepository = friendRequestRepository,
            userRepository = userRepository
        )
    }
}