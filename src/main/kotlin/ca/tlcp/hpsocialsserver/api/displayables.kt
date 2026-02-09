package ca.tlcp.hpsocialsserver.api

import ca.tlcp.hpsocialsserver.db.*
import ca.tlcp.hpsocialsserver.toolchain.toUserDetails

data class CommentDetails(
    val id: Long,
    val user: UserDetails?,
    val body: String?,
    val attachedImages: List<CommentImage>
) {
    constructor(comment: Comment) : this(
        id = comment.id!!,
        user = UserDetails(comment.post?.user!!),
        body = comment.body,
        attachedImages = comment.attachedImages ?: emptyList()
    )
}

data class UserDetails(
    val name: String?,
    val bio: String?,
    val pfp: ByteArray?,
    val friends: List<Long>,
    val handle: String?,
    val email: String?
) {
    constructor(user: User) : this(
        name = user.name,
        bio = user.bio,
        pfp = user.pfp,
        friends = user.friends,
        handle = user.handle,
        email = user.email
    )
}

data class PostDetails(
    val id: Long,
    val user: UserDetails?,
    val body: String?,
    val attachedImages: List<PostImage>
) {
    constructor(post: Post) : this(
        id = post.id!!,
        user = UserDetails(post.user!!),
        body = post.body,
        attachedImages = post.attachedImages ?: emptyList()
    )
}



