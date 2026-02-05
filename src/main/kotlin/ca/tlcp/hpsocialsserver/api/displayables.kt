package ca.tlcp.hpsocialsserver.api

import ca.tlcp.hpsocialsserver.db.*

data class CommentDetails(val comment: Comment) {
    private val id: Long?
    private val username: String?
    private val body: String?
    private val attachedImages: MutableList<CommentImage>

    init {
        this.username = comment.post?.user?.username!!
        this.body = comment.body
        this.attachedImages = comment.attachedImages
        this.id = comment.id
    }
}

data class PostDetails(val post: Post) {
    private val id: Long
    private val username: String?
    private val body: String?
    private val attachedImages: MutableList<PostImage>

    init {
        this.username = post.user?.username
        this.body = post.body
        this.attachedImages = post.attachedImages!!
        this.id = post.id!!
    }
}


data class UserDetails(val user: User) {
    private val name: String?
    private val bio: String?
    private val pfp: ByteArray?
    private val friends: MutableList<Long>
    private val handle: String?

    init {
        friends = user.friends
        name = user.name
        bio = user.bio
        pfp = user.pfp
        handle = user.handle
    }

    override fun toString(): String {
        return "{" +
                "\"name\": \"" + name + "\"," +
                "\"bio\": \"" + bio + "\"," +
                "\"pfp\": \"" + pfp.contentToString() + "\"," +
                "\"friends\": " + friends +
                "\"handle\": " + handle + "," +
                "}"
    }
}
