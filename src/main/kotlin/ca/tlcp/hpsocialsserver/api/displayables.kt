package ca.tlcp.hpsocialsserver.api

import ca.tlcp.hpsocialsserver.CharacterState
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
        isBot = user.isBot
    )
}

data class PostDetails(
    val id: Long,
    val user: UserDetails?,
    val body: String?,
    val attachedImages: List<PostImage>
) {
    constructor(post: Post, userRepo: UserRepository) : this(
        id = post.id!!,
        user = UserDetails(post.user!!, userRepo),
        body = post.body,
        attachedImages = post.attachedImages ?: emptyList()
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