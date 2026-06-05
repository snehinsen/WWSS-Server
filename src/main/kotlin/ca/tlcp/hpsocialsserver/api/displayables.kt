package ca.tlcp.hpsocialsserver.api

import ca.tlcp.hpsocialsserver.CharacterState
import ca.tlcp.hpsocialsserver.ChatType
import ca.tlcp.hpsocialsserver.db.*
import java.time.Instant

data class UserDetails(
    val firstName: String?,
    val lastName: String?,
    val bio: String?,
    val pfp: String?,
    val friends: List<Long>,
    val handle: String?,
    val email: String?,
    val isWizarding: Boolean?,
    val isBot: Boolean?,
    var role: String = "USER",
    val id: Long
) {
    constructor(user: User, userRepository: UserRepository) : this(
        firstName = user.firstName,
        lastName = user.lastName,
        bio = user.bio,
        pfp = user.pfp,
        friends = user.friends.map { userID: Long ->
            userRepository.getUserById(userID)!!.get().id!!
        },
        handle = user.handle,
        email = user.email,
        isWizarding = user.isWizarding,
        isBot = user.isBot,
        id = user.id!!
    )
}

data class FriendRequestDetails(
    val id: Long,
    val sender: UserDetails,
    val receiver: UserDetails,
    val timeSent: Instant = Instant.now(),
) {
    constructor(request: FriendRequest, userRepo: UserRepository) : this(
        id = request.id!!,
        sender = UserDetails(request.sender!!, userRepo),
        receiver = UserDetails(request.receiver!!, userRepo),
    )
}

data class PostDetails(
    val id: Long,
    val user: UserDetails?,
    val body: String?,
    val attachedMedia: List<String>
) {
    constructor(post: Post, userRepo: UserRepository) : this(
        id = post.id!!,
        user = UserDetails(post.user!!, userRepo),
        body = post.body,
        attachedMedia = post.attachedMedia ?: emptyList()
    )
}

data class NotificationDetails(
    val id: Long,
    val body: String?,
    val timeSent: Instant = Instant.now(),
    val title: String,
) {
    constructor(notification: Notification) : this(
        id = notification.id!!,
        timeSent = notification.timeSent,
        body = notification.body,
        title = notification.title,
    )
}

data class CharacterStateDetails(
    val energy: Float,
    val mood: String,
    val state: CharacterState,
    val currentlyDoing: String,
) {
    constructor(state: AICharacterState?) : this(
        energy = state?.energy ?: 0.0f,
        mood = state?.mood ?: "Unknown",
        state = state?.status ?: CharacterState.free,
        currentlyDoing = state?.currentlyDoing ?: "Nothing",
    )

    override fun toString(): String {
        return """
        {
            "energy": $energy,
            "mood": "$mood",
            "state": "${state.name}",
            "currentlyDoing": "$currentlyDoing"
        }
        """
    }
}

data class ChatThreadDetails(
    val id: Long,
    val owner: UserDetails,
    var otherMembers: MutableList<UserDetails> = mutableListOf(),
    var title: String = "",
    val threadType: ChatType
) {
    constructor(chatThread: ChatThread, userRepo: UserRepository) : this(
        owner = UserDetails(chatThread.owner, userRepo),
        id = chatThread.id!!,
        title = chatThread.title,
        otherMembers = chatThread.otherMembers.map { member: User ->
            UserDetails(member, userRepo)
        } as MutableList<UserDetails>,
        threadType = chatThread.threadType,

        )
}

// WebSocket Message DTOs
data class WebSocketMessageRequest(
    val content: String,
    val attachmentUrls: List<String> = emptyList(),
    val threadId: Long
)

data class WebSocketMessageResponse(
    val id: Long,
    val senderId: Long,
    val senderHandle: String,
    val senderName: String,
    val senderPfp: String?,
    val threadId: Long,
    val content: String,
    val attachmentUrls: List<String> = emptyList(),
    val timestamp: Instant = Instant.now(),
    val messageType: String = "MESSAGE" // MESSAGE, TYPING, JOINED, LEFT
) {
    constructor(message: DMMessage, userRepo: UserRepository) : this(
        id = message.id!!,
        senderId = message.sender.id!!,
        senderHandle = message.sender.handle!!,
        senderName = "${message.sender.firstName} ${message.sender.lastName}",
        senderPfp = message.sender.pfp,
        threadId = message.thread.id!!,
        content = message.content,
        attachmentUrls = message.attachmentUrls,
        timestamp = message.timestamp
    )
}

data class TypingIndicator(
    val threadId: Long,
    val userId: Long,
    val userHandle: String,
    val isTyping: Boolean
)

data class ThreadMemberUpdate(
    val threadId: Long,
    val userId: Long,
    val userHandle: String,
    val action: String // JOINED, LEFT
)
