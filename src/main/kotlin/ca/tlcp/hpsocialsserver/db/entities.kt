package ca.tlcp.hpsocialsserver.db

import jakarta.persistence.*

@Entity
@Table(name = "comment_images")
data class CommentImage(
    @Lob
    var data: ByteArray? = null,

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    var comment: Comment? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}

@Entity
@Table(name = "post_images")
data class PostImage(

    @Lob
    var data: ByteArray? = null,

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    var post: Post? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

@Entity
@Table(name = "comments")
data class Comment(
    val body: String? = null,

    @OneToMany(mappedBy = "comment", cascade = [CascadeType.ALL], orphanRemoval = true)
    val attachedImages: MutableList<CommentImage> = mutableListOf(),

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

}


@Entity
@Table(name = "posts")
data class Post(

    @Lob
    var body: String? = null,

    @OneToMany(mappedBy = "post", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachedImages: MutableList<PostImage>? = mutableListOf(),

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}


@Entity
@Table(name = "users")
class User() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var name: String? = null

    @Lob
    var bio: String? = null

    @Lob
    var pfp: ByteArray? = null

    @Column(nullable = false)
    var isBot: Boolean = false

    @Column(nullable = false)
    var isWizarding: Boolean = false

    @Column(unique = true, nullable = false)
    var username: String? = null

    var password: String? = null

    @ElementCollection
    @CollectionTable(name = "user_friends", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "friend_id")
    var friends: MutableList<Long> = mutableListOf()

    @Column(unique = true, nullable = false)
    var handle: String? = null
}
