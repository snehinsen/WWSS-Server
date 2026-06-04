package ca.tlcp.hpsocialsserver.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    fun getAllByUser(user: User?): MutableList<Post>
    fun getAllByParent(parent: Post?): List<Post>
}

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun getUserById(id: Long): Optional<User?>?
    fun getUserByHandle(handle: String?): Optional<User>
    fun getUserByEmail(email: String?): Optional<User>
    fun getAllByIsBot(value: Boolean): List<User>
    fun existsUserByEmail(email: String?): Boolean
    fun existsUserByHandle(handle: String): Boolean
}

@Repository
interface FriendRequestRepository : JpaRepository<FriendRequest, Long> {
    fun getFriendRequestBySender(sender: User): Optional<FriendRequest>
    fun getFriendRequestByReceiver(receiver: User): Optional<FriendRequest>

    fun existsBySender(sender: User): Boolean
    fun existsByReceiver(receiver: User): Boolean
    fun existsBySenderAndReceiver(sender: User, receiver: User): Boolean

    fun getAllBySender(sender: User): MutableList<FriendRequest>
    fun getAllByReceiver(receiver: User): MutableList<FriendRequest>

    fun getFriendRequestBySenderAndReceiver(sender: User, receiver: User): Optional<FriendRequest>
}

@Repository
interface NotificationRepository: JpaRepository<Notification, Long> {
    fun getNotificationsByUser(user: User): MutableList<Notification>
}

@Repository
interface AIStateRepository : JpaRepository<AICharacterState, Long> {


    fun getAICharacterStateByUser(user: User): Optional<AICharacterState>
}

@Repository
interface ChatThreadRepository : JpaRepository<ChatThread, Long> {

    @Query("""
        SELECT DISTINCT ct
        FROM ChatThread ct
        LEFT JOIN ct.otherMembers member
        WHERE ct.owner = :user
        OR member = :user
    """)
    fun findAllUserThreads(
        @Param("user") user: User
    ): List<ChatThread>
}

@Repository
interface DMMessageRepository : JpaRepository<DMMessage, Long> {
    fun findAllByThread(thread: ChatThread): List<DMMessage>
    fun findAllByThreadOrderByTimestampAsc(thread: ChatThread): List<DMMessage>
    fun findAllByThreadOrderByTimestampDesc(thread: ChatThread): List<DMMessage>
    fun findTop50ByThreadOrderByTimestampDesc(thread: ChatThread): List<DMMessage>
}

interface CharacterMemoryRepository : JpaRepository<CharacterMemoru, Long> {
    fun findAllByCharacter(character: User): MutableList<CharacterMemoru>
    fun findAllByCharacterAndTimestamp(character: User, timestamp: Instant = Instant.now()): List<CharacterMemoru>
    fun findByCharacterAndLabel(character: User, label: String): CharacterMemoru? // In theory, the AIs should never have the same label, but it's safer just to do a double check to make sure memories don't conflict.

}