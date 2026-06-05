package ca.tlcp.hpsocialsserver.db

import ca.tlcp.hpsocialsserver.CharacterState
import ca.tlcp.hpsocialsserver.ChatType
import jakarta.persistence.*
import java.time.Instant


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

    var attachedMedia: MutableList<String> = mutableListOf() // Images attached to this post
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
@Table(name = "friendRequests")
data class FriendRequest(
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User,
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: User,
    var status: String = "pending" // pending, accepted, rejected
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
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
        tittle: String,
        body: String,
        user: User
    ) {
        this.timeSent = Instant.now()
        this.user = user
        this.title = tittle
        this.body = body
        this.timeSent = Instant.now()
    }
}

@Entity
@Table(name = "chats")
data class ChatThread(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    val owner: User,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "chat_thread_members",
        joinColumns = [JoinColumn(name = "thread_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var otherMembers: MutableList<User> = mutableListOf(),

    var title: String = "",

    @Enumerated(EnumType.STRING)
    val threadType: ChatType,

    @OneToMany(
        mappedBy = "thread",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val messages: MutableList<DMMessage> = mutableListOf()
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}

@Entity
@Table(name = "dm_messages")
data class DMMessage(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id")
    val sender: User,

    @Column(columnDefinition = "TEXT")
    var content: String,

    @ElementCollection
    @CollectionTable(
        name = "dm_message_attachments",
        joinColumns = [JoinColumn(name = "message_id")]
    )
    @Column(name = "attachment_url")
    val attachmentUrls: List<String> = emptyList(),

    var timestamp: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id")
    val thread: ChatThread
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}

@Entity
@Table(name = "character_memories")
data class CharacterMemoru(
    @Column(columnDefinition = "TEXT")
    var content: String = "",
    @ManyToOne
    @JoinColumn(name = "character_id")
    val character: User,
    val label: String = "",
    var timestamp: Instant = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}