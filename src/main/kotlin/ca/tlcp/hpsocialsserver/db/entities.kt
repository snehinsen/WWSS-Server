package ca.tlcp.hpsocialsserver.db

import ca.tlcp.hpsocialsserver.CharacterState
import jakarta.persistence.*
import java.time.Instant


@Entity
@Table(name = "images")
data class PostImage(

    @Lob
    var data: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}


@Entity
@Table(name = "posts")
data class Post(
    @Column(columnDefinition = "text")
    var body: String? = null,

    @ManyToOne
    @JoinColumn(name = "parent_post_id") // Foreign key linking to the parent post, can be null if it's a regular post
    var parent: Post? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,

    @OneToMany
    @JoinColumn(name = "images_id")
    var attachedImages: MutableList<PostImage> = mutableListOf() // Images attached to this post
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

@Entity
@Table(name = "users")
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var firstName: String? = null

    @Column(nullable = false)
    var lastName: String? = null

    @Column(columnDefinition = "text")
    var bio: String? = null

    @Column(columnDefinition = "text")
    var pfp: String? = null

    @Column(nullable = false)
    var isBot: Boolean = false

    @Column(nullable = false)
    var isWizarding: Boolean = false

    @Column(unique = true, nullable = false)
    var email: String? = null

    var password: String? = null

    @ElementCollection
    @CollectionTable(name = "user_friends", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "friend_id")
    var friends: MutableList<Long> = mutableListOf()

    @Column(unique = true, nullable = false)
    var handle: String? = null

    constructor(
        firstName: String?,
        lastName: String? = "",
        bio: String?,
        pfp: String?,
        isBot: Boolean,
        isWizarding: Boolean,
        email: String?,
        password: String?
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.bio = bio
        this.pfp = pfp
        this.isBot = isBot
        this.isWizarding = isWizarding
        this.email = email
        this.password = password
        handle = firstName!!.replace(" ", "") + lastName!!.replace(" ", "")
    }

    constructor()

    constructor(
        firstName: String?,
        lastName: String?,
        bio: String?,
        pfp: String?,
        isBot: Boolean,
        isWizarding: Boolean,
        email: String?,
        password: String?,
        handle: String?
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.bio = bio
        this.pfp = pfp
        this.isBot = isBot
        this.isWizarding = isWizarding
        this.email = email
        this.password = password
        this.handle = handle
    }


    override fun toString(): String {
        return "{id:$id, firstName:$firstName, lastName:${lastName}, bio={in database}, pfp={masked}, isBot=$isBot, isWizarding=$isWizarding, email=$email, password=$password, friends=$friends, handle=$handle)"
    }
}

@Entity
@Table(name = "characterState")
data class AICharacterState(
    @OneToOne
    @JoinColumn("user_id")
    val user: User,
    var status: CharacterState = CharacterState.busy,
    var energy: Float = 1.0f,
    var mood: String = "Happy",
    var currentlyDoing: String = ""
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}

@Entity
@Table(name = "notifications")
class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var timeSent: Instant = Instant.now()

    var title: String = ""
    var body: String = ""

    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User? = null

    constructor() {
        this.timeSent = Instant.now()
    }

    constructor(
        title: String,
        body: String,
        user: User
    ) {
        this.timeSent = Instant.now()
        this.user = user
        this.title = title
        this.body = body
        this.timeSent = Instant.now()
    }
}