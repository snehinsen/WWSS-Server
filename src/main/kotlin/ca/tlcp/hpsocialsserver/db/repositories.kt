package ca.tlcp.hpsocialsserver.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import kotlin.time.Instant

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
interface NotificationRepository: JpaRepository<Notification, Long> {
    fun getNotificationsByUser(user: User): MutableList<Notification>
}

@Repository
interface AIStateRepository : JpaRepository<AICharacterState, Long> {


    fun getAICharacterStateByUser(user: User): Optional<AICharacterState>
}