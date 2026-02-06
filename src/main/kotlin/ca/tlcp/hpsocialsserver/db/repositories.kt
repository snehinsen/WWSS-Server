package ca.tlcp.hpsocialsserver.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CommentRepository : JpaRepository<Comment, Long> {
    fun getAllByPost(post: Post?): MutableList<Comment>?
}

@Repository
interface PostRepository : JpaRepository<Post, Long>

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun getUserByUsername(username: String?): Optional<User?>?
    fun getUserById(id: Long): Optional<User?>?
    fun getUserByHandle(handle: String?): Optional<User?>?
    fun existsUserByUsername(username: String?): Boolean
}
